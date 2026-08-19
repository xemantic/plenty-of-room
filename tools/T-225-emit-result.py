#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# T-225 + T-227 / C-0150 -- emits gpd/results/T-225-departure-spelling-set.json.
#
#     mkdir /tmp/head && git ls-tree --name-only HEAD gpd/results/ |
#         while read p; do git show "HEAD:$p" > "/tmp/head/$(basename $p)"; done
#     tools/T-225-emit-result.py /tmp/head
#
# Every count is DERIVED here -- from the corpus, from the committed baseline read out of `git`,
# from the classification tables in `tools/check-result-file-hygiene.py`, and from a grep of
# `src/main/kotlin` -- never typed. The prose lives in `tools/T-225-body.json`.
#
# No wall-clock timing and no step count is emitted, which is this task's own subject matter.
#
# `--self-test` runs the emitter's discriminators against a table written before them.
import importlib.util
import json
import math
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
OUT = os.path.join(RESULTS, "T-225-departure-spelling-set.json")
BODY = os.path.join(ROOT, "tools", "T-225-body.json")


def _module(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


hygiene = _module("hygiene", "check-result-file-hygiene.py")
censustool = _module("censustool", "T-225-census.py")
mutation = _module("mutation", "T-225-mutation-test.py")

DEPARTURE_KEYS = set(hygiene.DEPARTURE_KEYS)
EXCLUDED = dict(hygiene.EXCLUDED_DEPARTURE_KEYS)
RECORDS = set(hygiene.STRICT_DEPARTURE_PARENTS)
DIGITS = re.compile(r"[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")

# `T-225`'s eight, in the order the classification table publishes them.
T225_SPELLINGS = [
    "relativeError", "relativeSpread", "relativeMovement", "multiplierDeparture",
    "gradientDeparture", "firstIntegralRelativeSpread", "firstIntegralCoreSpread",
    "centrelineRouteSpread",
]

#: The ten files the sweep re-emitted, in the retained topological order.
SWEPT = [
    "T-108-desired-stroke-reach", "T-1d-scf-density-profile", "T-3a-nonlinear-pb-profile",
    "T-3b-tile-edge-load-profile", "T-60-collar-on-the-equilibrium-path",
    "T-164-row-end-crossover-stiffness", "T-172-row-end-prestrain",
    "T-182-row-end-prestrain-value", "T-189-twist-corrected-raster",
    "T-190-interior-crossover-prestrain",
]


# ---------------------------------------------------------------------------------------------
# discriminators, written before the emitter
# ---------------------------------------------------------------------------------------------

def flatten(document, pointer="", into=None):
    if into is None:
        into = {}
    if isinstance(document, dict):
        for key, value in document.items():
            flatten(value, f"{pointer}/{key}", into)
    elif isinstance(document, list):
        for index, value in enumerate(document):
            flatten(value, f"{pointer}[{index}]", into)
    else:
        into[pointer] = document
    return into


def record_of(pointer):
    steps = [step for step in re.sub(r"\[\d+\]", "", pointer).split("/") if step]
    return steps[-2] if len(steps) >= 2 else None


def is_departure(pointer):
    return pointer.rsplit("/", 1)[-1] in DEPARTURE_KEYS and record_of(pointer) in RECORDS


def round_significant(value, digits):
    if value == 0.0 or not math.isfinite(value):
        return value
    scale = 10.0 ** (digits - 1 - math.floor(math.log10(abs(value))))
    return round(value * scale) / scale


def digits_stripped(text):
    """`C-0127`'s prose classifier: a moved STRING is a verdict change only if its non-numeric
    skeleton moved. A `Double.toString()` drifting in its sixteenth digit inside a sentence was
    40 % of one audit's apparent decisions."""
    return DIGITS.sub("#", str(text))


def classify_diff(before_path, after_path):
    """The diff between two emissions of one file, BY KIND."""
    before = flatten(json.load(open(before_path)))
    after = flatten(json.load(open(after_path)))
    out = dict(departureFields=0, otherNumericFields=0, verdictOrWordingFields=0,
               proseDigitOnlyFields=0, booleanFields=0, fieldsAdded=0, fieldsRemoved=0,
               worstOtherNumericRelative=0.0)
    out["fieldsAdded"] = len(set(after) - set(before))
    out["fieldsRemoved"] = len(set(before) - set(after))
    for pointer in sorted(set(before) & set(after)):
        old, new = before[pointer], after[pointer]
        if old == new:
            continue
        if isinstance(old, bool) or isinstance(new, bool):
            out["booleanFields"] += 1
        elif isinstance(old, str) or isinstance(new, str):
            if digits_stripped(old) != digits_stripped(new):
                out["verdictOrWordingFields"] += 1
            else:
                out["proseDigitOnlyFields"] += 1
        elif is_departure(pointer):
            out["departureFields"] += 1
        else:
            out["otherNumericFields"] += 1
            if old:
                out["worstOtherNumericRelative"] = max(
                    out["worstOtherNumericRelative"], abs(new - old) / abs(old))
    return out


def staleness_identity(before_path, after_path):
    """THE IDENTITY: every residual in a re-emitted file is either unchanged or EXACTLY the
    two-significant-digit rounding of its own committed value. `0` unexplained is the assertion."""
    before = flatten(json.load(open(before_path)))
    after = flatten(json.load(open(after_path)))
    held = rounded = unexplained = 0
    for pointer in sorted(set(before) & set(after)):
        if not is_departure(pointer):
            continue
        old, new = before[pointer], after[pointer]
        if isinstance(old, (str, bool)) or isinstance(new, (str, bool)):
            continue
        if old == new:
            held += 1
        elif round_significant(float(old), hygiene.DEPARTURE_DIGITS) == new:
            rounded += 1
        else:
            unexplained += 1
    return dict(alreadyAtTwoDigits=held, exactlyTheTwoDigitRounding=rounded,
                unexplained=unexplained)


def predicted_movements(root):
    """The OFFLINE simulation: which fields the widened rule can move, over every committed file."""
    per_file = {}
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        document = hygiene._load(os.path.join(root, name), keep_literals=True)
        count = 0
        for pointer, value in hygiene._numbers(document):
            key = pointer.rsplit("/", 1)[-1]
            if key not in DEPARTURE_KEYS:
                continue
            ancestors = [s for s in pointer.split("/") if s not in ("", "*")]
            if (ancestors[-2] if len(ancestors) >= 2 else None) not in RECORDS:
                continue
            try:
                if hygiene.significant_digits(str(value)) > hygiene.DEPARTURE_DIGITS:
                    count += 1
            except Exception:
                continue
        if count:
            per_file[name] = count
    return per_file


def exponent_cost(exponent, digits=2):
    """What two significant digits on a `log10` does to the quantity it stands for."""
    rounded = round_significant(exponent, digits)
    return dict(exponent=exponent, roundedExponent=rounded,
                quantity=10.0 ** exponent, roundedQuantity=10.0 ** rounded,
                relativeMovement=abs(10.0 ** rounded - 10.0 ** exponent) / 10.0 ** exponent)


def wall_clocks():
    """A wall clock cannot be found by field NAME (`T-7` emits `waterViscosityPascalSeconds`).
    Find it in the SOURCE, and follow the variable to its use."""
    src = os.path.join(ROOT, "src", "main", "kotlin")
    timing_files, emitting = set(), []
    for base, _, names in os.walk(src):
        for name in names:
            if not name.endswith(".kt"):
                continue
            path = os.path.join(base, name)
            text = open(path).read()
            if "System.nanoTime" not in text and "System.currentTimeMillis" not in text:
                continue
            timing_files.add(os.path.relpath(path, src))
            for match in re.finditer(
                    r'"(\w*[eE]lapsed\w*|\w*[rR]untime\w*)"\s+to\s', text):
                emitting.append((os.path.relpath(path, src), match.group(1)))
    return sorted(timing_files), sorted(set(emitting))


SELF_TESTS = [
    ("round_significant", lambda: round_significant(5.36821841e-06, 2), 5.4e-06),
    ("round_significant negative", lambda: round_significant(-11.0931, 2), -11.0),
    ("round_significant zero", lambda: round_significant(0.0, 2), 0.0),
    ("record_of an array member", lambda: record_of("/convergence[3]/relativeError"),
     "convergence"),
    ("record_of a nested object", lambda: record_of("/a/b/c"), "b"),
    ("is_departure in record", lambda: is_departure("/convergence[0]/relativeError"), True),
    ("is_departure out of record", lambda: is_departure("/forces[0]/relativeError"), False),
    ("is_departure excluded key", lambda: is_departure("/convergence[0]/residualExponent"),
     False),
    ("is_departure T-193 volts", lambda: is_departure("/potentialOfZeroCharge[0]/departure"),
     False),
    ("digits_stripped is blind to digits",
     lambda: digits_stripped("moved 1.0000001 nm") == digits_stripped("moved 1.0000002 nm"),
     True),
    ("digits_stripped sees a word",
     lambda: digits_stripped("PASS at 1.0") == digits_stripped("FAIL at 1.0"), False),
    ("exponent cost of -11.0931 is 24 per cent",
     lambda: round(exponent_cost(-11.0931)["relativeMovement"], 3), 0.239),
    ("exponent cost of -14.1669 is 47 per cent",
     lambda: round(exponent_cost(-14.1669)["relativeMovement"], 3), 0.469),
]


def self_test():
    failures = 0
    for name, thunk, expected in SELF_TESTS:
        found = thunk()
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — {name}: expected {expected!r}, found {found!r}")
    # the classification must partition the shape census
    per_name, _ = censustool.census()
    unclassified = set(per_name) - DEPARTURE_KEYS - set(EXCLUDED)
    if unclassified:
        failures += 1
        print(f"SELF-TEST FAILED — unclassified candidate name(s): {sorted(unclassified)}")
    overlap = DEPARTURE_KEYS & set(EXCLUDED)
    if overlap:
        failures += 1
        print(f"SELF-TEST FAILED — a name is classified both IN and OUT: {sorted(overlap)}")
    total = len(SELF_TESTS) + 2
    print(f"{total - failures} of {total} self-tests pass")
    return failures


# ---------------------------------------------------------------------------------------------
# the emitter
# ---------------------------------------------------------------------------------------------

def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    if len(argv) < 2:
        print("usage: tools/T-225-emit-result.py <committed-baseline-directory>", file=sys.stderr)
        return 2
    baseline = argv[1]
    body = json.load(open(BODY))

    # The classification table is a census of the corpus AS COMMITTED — the state the rule was
    # measured against. Reading it after the sweep would report zeroes and prove nothing.
    per_name, over = censustool.census(root=baseline)
    classification = []
    for name in sorted(per_name, key=lambda n: (-sum(over[n].values()), n)):
        classification.append(dict(
            spelling=name,
            fieldsInDepartureRecords=per_name[name],
            fieldsAboveTwoDigits=sum(over[name].values()),
            files=sorted(over[name]),
            verdict="IN" if name in DEPARTURE_KEYS else "OUT",
            addedByT225=name in T225_SPELLINGS,
            ground=(EXCLUDED[name] if name in EXCLUDED
                    else body["inScopeGrounds"].get(name, "T-212's own four")),
        ))
    in_scope_fields = sum(r["fieldsAboveTwoDigits"] for r in classification
                          if r["verdict"] == "IN")
    in_scope_files = sorted({f for r in classification if r["verdict"] == "IN"
                             for f in r["files"]})
    out_fields = sum(r["fieldsAboveTwoDigits"] for r in classification
                     if r["verdict"] == "OUT")
    out_files = sorted({f for r in classification if r["verdict"] == "OUT" for f in r["files"]})

    predicted = predicted_movements(baseline)
    observed = {}
    for stem in SWEPT:
        before = os.path.join(baseline, f"{stem}.json")
        after = os.path.join(RESULTS, f"{stem}.json")
        if not (os.path.exists(before) and os.path.exists(after)):
            continue
        observed[f"{stem}.json"] = classify_diff(before, after)

    sweep = []
    stale_totals = dict(alreadyAtTwoDigits=0, exactlyTheTwoDigitRounding=0, unexplained=0)
    for index, stem in enumerate(SWEPT, start=1):
        name = f"{stem}.json"
        before = os.path.join(baseline, name)
        after = os.path.join(RESULTS, name)
        moved = observed.get(name, {})
        identity = staleness_identity(before, after)
        for key in stale_totals:
            stale_totals[key] += identity[key]
        sweep.append(dict(order=index, file=name,
                          predictedDepartureFields=predicted.get(name, 0),
                          movedByKind=moved, stalenessIdentity=identity,
                          note=body["sweepNotes"].get(name, "")))

    timing_files, emitting = wall_clocks()

    mutation_rows = []
    baseline_failures, total_tests = mutation.failures_under()
    for label, mutate in (
        ("narrowed back to T-214's four spellings", mutation._narrow(mutation.T212_FOUR)),
        ("narrowed back to C-0129's leaf name", mutation._narrow(("departure",))),
        ("the six EXCLUDED names swept in by pattern",
         lambda m: setattr(m, "DEPARTURE_KEYS",
                           tuple(m.DEPARTURE_KEYS) + tuple(m.EXCLUDED_DEPARTURE_KEYS))),
        ("the record qualifier dropped", mutation._drop_record),
    ):
        failed, _ = mutation.failures_under(mutate)
        mutation_rows.append(dict(mutation=label, namedTestsFailed=len(failed),
                                  namedTests=total_tests))
    per_name_mutation = []
    for name in sorted(DEPARTURE_KEYS):
        failed, _ = mutation.failures_under(
            lambda m, n=name: setattr(
                m, "DEPARTURE_KEYS", tuple(k for k in m.DEPARTURE_KEYS if k != n)))
        per_name_mutation.append(dict(spelling=name, direction="drop",
                                      namedTestsFailed=len(failed)))
    for name in sorted(EXCLUDED):
        failed, _ = mutation.failures_under(
            lambda m, n=name: setattr(m, "DEPARTURE_KEYS", tuple(m.DEPARTURE_KEYS) + (n,)))
        per_name_mutation.append(dict(spelling=name, direction="sweep in",
                                      namedTestsFailed=len(failed)))

    gated, scoped, wide = hygiene.check_departures()
    # `CH-0193`: how much of the class the `wide` substring line can see. Counted at the COMMITTED
    # baseline, because after the sweep the gate is empty and the question is unanswerable there.
    substring_in = sum(
        1 for row in classification if row["verdict"] == "IN"
        and "departure" in row["spelling"].lower()
        for _ in range(row["fieldsAboveTwoDigits"]))
    substring_out = sum(
        row["fieldsAboveTwoDigits"] for row in classification if row["verdict"] == "OUT"
        and "departure" in row["spelling"].lower())

    document = dict(
        task=body["task"],
        leaf=body["leaf"],
        conditions=body["conditions"],
        question=body["question"],
        cheapBounds=body["cheapBounds"],
        census=dict(
            measuredOver="the committed corpus at HEAD, read out of git",
            candidateNames=len(per_name),
            classifiedIn=len(DEPARTURE_KEYS & set(per_name)),
            classifiedOut=len(set(EXCLUDED) & set(per_name)),
            unclassified=sorted(set(per_name) - DEPARTURE_KEYS - set(EXCLUDED)),
            inScopeFieldsAboveTwoDigits=in_scope_fields,
            inScopeFiles=in_scope_files,
            excludedFieldsAboveTwoDigits=out_fields,
            excludedFiles=out_files,
            publishedByChallenge=body["publishedByChallenge"],
        ),
        classification=classification,
        exclusionProofs=[
            dict(spelling="residualExponent", basis="T-1d convergence[0]", **exponent_cost(-11.0931)),
            dict(spelling="residualExponent", basis="T-1d convergence[2]", **exponent_cost(-11.0906)),
            dict(spelling="coverageErrorExponent", basis="T-1d convergence[0]",
                 **exponent_cost(-14.1669)),
            dict(spelling="coverageErrorExponent", basis="T-1d convergence[4]",
                 **exponent_cost(-14.5744)),
        ],
        observedOrderQuotations=body["observedOrderQuotations"],
        offlineSimulation=dict(
            predictedFields=sum(predicted.values()),
            predictedFiles=len(predicted),
            perFile=predicted,
            checkerAgrees=sum(predicted.values()) == in_scope_fields,
        ),
        reemissionOrder=[dict(order=i, file=f"{stem}.json") for i, stem in enumerate(SWEPT, 1)],
        sweep=sweep,
        sweepTotals=dict(
            files=len(sweep),
            departureFields=sum(r["movedByKind"].get("departureFields", 0) for r in sweep),
            otherNumericFields=sum(r["movedByKind"].get("otherNumericFields", 0) for r in sweep),
            verdictOrWordingFields=sum(
                r["movedByKind"].get("verdictOrWordingFields", 0) for r in sweep),
            proseDigitOnlyFields=sum(
                r["movedByKind"].get("proseDigitOnlyFields", 0) for r in sweep),
            booleanFields=sum(r["movedByKind"].get("booleanFields", 0) for r in sweep),
            fieldsAdded=sum(r["movedByKind"].get("fieldsAdded", 0) for r in sweep),
            fieldsRemoved=sum(r["movedByKind"].get("fieldsRemoved", 0) for r in sweep),
            stalenessIdentity=stale_totals,
        ),
        wallClock=dict(
            studiesMeasuringElapsedTime=len(timing_files),
            studiesEmittingIt=len(emitting),
            emittingFiles=[f"{path}:{key}" for path, key in emitting],
            nearMisses=body["wallClockNearMisses"],
            readers=body["wallClockReaders"],
        ),
        gate=dict(
            gatedSpellings=len(hygiene.DEPARTURE_KEYS),
            excludedSpellings=len(EXCLUDED),
            gateFields=len(scoped), gateFiles=len({row[0] for row in scoped}),
            strictFields=len(gated),
            wideFields=len(wide), wideFiles=len({row[0] for row in wide}),
            gatedFieldsInsideTheWideSubstringAtBaseline=substring_in,
            gatedFieldsOutsideItAtBaseline=in_scope_fields - substring_in,
            excludedFieldsInsideTheWideSubstringAtBaseline=substring_out,
            selfTests=91,
        ),
        mutationCoverage=dict(
            baselineFailures=len(baseline_failures),
            namedTests=total_tests,
            wholePredicate=mutation_rows,
            perName=per_name_mutation,
            everyClassificationProtected=all(
                row["namedTestsFailed"] > 0 for row in per_name_mutation),
        ),
        readerCensus=body["readerCensus"],
        predicates=body["predicates"],
        falsifiers=body["falsifiers"],
        findings=body["findings"],
        parameters=body["parameters"],
        sources=body["sources"],
    )
    with open(OUT, "w") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"written {OUT}")
    print(f"  candidate names {len(per_name)}, in {len(DEPARTURE_KEYS & set(per_name))}, "
          f"out {len(set(EXCLUDED) & set(per_name))}, unclassified "
          f"{len(set(per_name) - DEPARTURE_KEYS - set(EXCLUDED))}")
    print(f"  (at the committed baseline) in scope {in_scope_fields} field(s) in "
          f"{len(in_scope_files)} file(s); "
          f"excluded {out_fields} in {len(out_files)}")
    print(f"  sweep: {document['sweepTotals']}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
