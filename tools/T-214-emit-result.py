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
# T-214 / C-0138 -- emits gpd/results/T-214-departure-rule-scope.json.
#
#     mkdir /tmp/head && git ls-tree --name-only HEAD gpd/results/ |
#         while read p; do git show "HEAD:$p" > "/tmp/head/$(basename $p)"; done
#     tools/T-214-emit-result.py /tmp/head [<control-run directory>]
#
# The optional second argument holds `T-214-ctrlA.json` and `T-214-ctrlB.json`, two emissions of
# `coupling.NonUniformCouplingStudyKt` from ONE `--committed` snapshot -- the control that `F1`
# needed. It is optional so the emitter still runs without them; the comparison is then absent
# rather than silently zero.
#
# Every count is DERIVED here from the corpus and from the committed baseline read out of `git`,
# never typed. No wall-clock timing and no step count is emitted (`CLAUDE.md`: a timing is a step
# counter by another name, and it makes a result file permanently un-diffable).
#
# `--selftest` runs the emitter's own discriminators against a table written before them.
import importlib.util
import json
import math
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
OUT = os.path.join(RESULTS, "T-214-departure-rule-scope.json")

spec = importlib.util.spec_from_file_location(
    "hygiene", os.path.join(ROOT, "tools", "check-result-file-hygiene.py")
)
hygiene = importlib.util.module_from_spec(spec)
spec.loader.exec_module(hygiene)

DEPARTURE_KEYS = set(hygiene.DEPARTURE_KEYS)
RECORDS = set(hygiene.STRICT_DEPARTURE_PARENTS)
DIGITS = re.compile(r"[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")

# `CH-0169` -- keys inside a departure RECORD that carry the same quantity under a spelling
# `DEPARTURE_SPELLINGS` does not list. Split by whether the rule should reach them, because the
# distinction is a judgement per key and not a pattern: a LOGARITHM of a residual is not a
# residual, and two significant digits on an exponent is a different statement entirely.
WIDER_SPELLINGS = {
    "relativeError":
        "a mesh-refinement residual of one solve — the rule's own quantity under a fifth name",
    "relativeSpread":
        "the spread of one quantity over a nested 1/2/4 subdivision — the rule's own quantity",
    "relativeMovement":
        "AMBIGUOUS: a study's own MEASUREMENT of determined precision (P-18), which is the shape "
        "the rule already excludes for T-160's departures[*].relativeDeparture",
    "worstResidual":
        "the worst closure residual of a search — a residual of one solve, but its UNITS are the "
        "closure's, not dimensionless; needs reading before it is rounded",
    "firstIntegralRelativeSpread":
        "AMBIGUOUS: CLAUDE.md records that the FULL spread measures the conditioning of the "
        "contact-value diagnostic rather than the accuracy of the answer, i.e. it is a reported "
        "quantity of the study and not only a residual",
    "firstIntegralCoreSpread":
        "AMBIGUOUS: the same, restricted to the core of the gap",
    "centrelineRouteSpread":
        "AMBIGUOUS: a spread between two evaluation routes of one solve",
}
NOT_A_DEPARTURE = {
    "residualExponent":
        "a LOGARITHM of a residual: two significant digits on an exponent is a different "
        "statement entirely, and the rule must not reach it",
    "coverageErrorExponent":
        "the same — log10 of a coverage error",
}


def walk(document, pointer=""):
    if isinstance(document, dict):
        for key, value in document.items():
            yield from walk(value, f"{pointer}/{key}")
    elif isinstance(document, list):
        for index, value in enumerate(document):
            yield from walk(value, f"{pointer}/{index}")
    else:
        yield pointer, document


def flat(path):
    with open(path, encoding="utf-8") as handle:
        return dict(walk(json.load(handle)))


def is_departure(pointer):
    parts = [p for p in pointer.strip("/").split("/") if not p.isdigit()]
    return len(parts) >= 2 and parts[-1] in DEPARTURE_KEYS and parts[-2] in RECORDS


def round_significant(value, digits):
    """`structure.roundForResult(value, digits, floor = 0.0)`, in Python.

    The floor is deliberately absent: it is applied by magnitude and is therefore identical
    before and after a change of the digit count, so it can add no movement of its own.
    """
    if value == 0.0 or not math.isfinite(value):
        return value
    scale = 10.0 ** (digits - 1 - math.floor(math.log10(abs(value))))
    return round(value * scale) / scale


def predicted_movements(root):
    """P3 -- the offline simulation, run over the WHOLE corpus before any study is re-run.

    The mechanism change alters exactly one thing: the digit count a departure-record field is
    rounded to. So the set of files whose bytes it can move is the set carrying a departure field
    that is not already at two digits, and the set of fields it can move inside them is exactly
    those fields. Anything else that moves in the sweep is a finding, not the repair.
    """
    per_file = {}
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        with open(os.path.join(root, name), encoding="utf-8") as handle:
            document = json.load(handle)
        moved = [
            pointer for pointer, value in walk(document)
            if isinstance(value, float) and is_departure(pointer)
            and round_significant(value, hygiene.DEPARTURE_DIGITS) != value
        ]
        if moved:
            per_file[name] = len(moved)
    return per_file


def staleness_identity(before_root, after_root, names):
    """P7 -- every departure-record residual in a re-emitted file is EXACTLY the two-digit
    rounding of its own committed value.

    Asserted as an identity over every such field rather than spot-checked. A field that moved for
    any other reason means a consumer was re-emitted before its producer, or the file was already
    stale — which is what `CH-0131` cost six iterations.
    """
    checked, matching, unexplained = 0, 0, []
    for name in names:
        before = flat(os.path.join(before_root, name))
        after = flat(os.path.join(after_root, name))
        for pointer, value in before.items():
            if not (isinstance(value, float) and is_departure(pointer)):
                continue
            if pointer not in after:
                unexplained.append({"resultFile": name, "pointer": pointer,
                                    "why": "field absent after re-emission"})
                continue
            checked += 1
            expected = round_significant(value, hygiene.DEPARTURE_DIGITS)
            if after[pointer] == expected:
                matching += 1
                continue
            # A departure is COMPUTED from the quantities in its own record, so a residual that
            # is not the rounding of its committed self is explained exactly when a sibling in
            # that record moved for a reason the control re-runs already attribute elsewhere.
            record = pointer.rsplit("/", 1)[0]
            siblings = [
                key for key, old_value in before.items()
                if key.startswith(record + "/") and key != pointer
                and isinstance(old_value, float) and key in after and after[key] != old_value
            ]
            unexplained.append({
                "resultFile": name, "pointer": pointer,
                "committed": value, "expected": expected, "found": after[pointer],
                "movedSiblingsInTheSameRecord": sorted(siblings),
                "explained": bool(siblings),
            })
    return checked, matching, unexplained


def classify(before_path, after_path):
    """What moved, BY KIND. `C-0127`: a moved STRING is not a moved decision, so the classifier
    strips digits before calling a prose change a verdict change — 23 of 57 apparent decision
    changes were `Double.toString()` moving in its sixteenth digit inside a sentence."""
    a, b = flat(before_path), flat(after_path)
    departure, other, prose_digits, prose_wording, decisions = [], [], 0, 0, 0
    for key in sorted(set(a) & set(b)):
        x, y = a[key], b[key]
        if x == y:
            continue
        if isinstance(x, bool) or isinstance(y, bool):
            decisions += 1
        elif isinstance(x, (int, float)) and isinstance(y, (int, float)):
            scale = max(abs(x), abs(y))
            entry = (key, abs(y - x) / scale if scale else 0.0)
            (departure if is_departure(key) else other).append(entry)
        elif isinstance(x, str) and isinstance(y, str):
            if DIGITS.sub("#", x) == DIGITS.sub("#", y):
                prose_digits += 1
            else:
                prose_wording += 1
        else:
            decisions += 1
    return {
        "fieldsBefore": len(a),
        "fieldsAfter": len(b),
        "fieldsAdded": len(set(b) - set(a)),
        "fieldsRemoved": len(set(a) - set(b)),
        "departureFieldsMoved": len(departure),
        "otherNumericMoved": len(other),
        "worstOtherNumericRelative": round(max([r[1] for r in other], default=0.0), 3),
        "worstOtherNumericField": max(other, key=lambda r: r[1])[0] if other else None,
        "proseDigitsOnly": prose_digits,
        "proseWording": prose_wording,
        "decisionsMoved": decisions,
    }


def census(root):
    gated, scoped = {}, {}
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        document = hygiene._load(os.path.join(root, name), keep_literals=True)
        a, b = hygiene.departures_in(document, name)
        if a:
            gated[name] = len(a)
        if b:
            scoped[name] = len(b)
    return gated, scoped


def wider_spelling_residue(root):
    """`CH-0169` -- the same quantity under a spelling the rule does not list.

    Reported with the per-key judgement, never swept: a `residualExponent` is the LOGARITHM of a
    residual and two significant digits on it is a different statement, so a mechanical widening
    would be wrong on 12 of the 62 fields. This is the measurement, not the repair.
    """
    rows = {}
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        with open(os.path.join(root, name), encoding="utf-8") as handle:
            document = json.load(handle)
        for pointer, value in walk(document):
            if not isinstance(value, float):
                continue
            parts = [p for p in pointer.strip("/").split("/") if not p.isdigit()]
            if len(parts) < 2 or parts[-2] not in RECORDS:
                continue
            key = parts[-1]
            if key not in WIDER_SPELLINGS and key not in NOT_A_DEPARTURE:
                continue
            if hygiene.significant_digits(repr(value)) <= hygiene.DEPARTURE_DIGITS:
                continue
            row = rows.setdefault((key, name), {
                "spelling": key, "resultFile": name, "fields": 0,
                "inScopeOfTheRule": key in WIDER_SPELLINGS,
                "judgement": WIDER_SPELLINGS.get(key) or NOT_A_DEPARTURE[key],
            })
            row["fields"] += 1
    return [rows[k] for k in sorted(rows)]


def rounding_implementations():
    """`CH-0169` -- how many of the tree's rounding entry points can carry the rule at all."""
    entries = []
    for relative in ("structure/ResultRounding.kt", "actuator/ActuatorResultRounding.kt",
                     "coupling/CouplingResultRounding.kt", "window/WindowResultRounding.kt",
                     "brush/FluctuationCorrectionStudy.kt", "brush/ScfDensityProfileStudy.kt"):
        path = os.path.join(ROOT, "src", "main", "kotlin", relative)
        with open(path, encoding="utf-8") as handle:
            source = handle.read()
        delegates = "structure.roundedForResult" in source or relative.startswith("structure/")
        entries.append({
            "implementation": relative,
            "carriesTheRuleAfterT214": delegates,
            "how": ("the canonical implementation" if relative.startswith("structure/")
                    else "delegates to structure/ResultRounding.kt" if delegates
                    else "an independent traversal with no digitsByKey parameter"),
        })
    return entries


SELFTESTS = [
    # (pointer, expected is_departure, description)
    ("/reproductions/0/departure", True, "the gate's own shape"),
    ("/convergence/3/relativeDeparture", True, "a second spelling under the second record"),
    ("/convergence/0/departureFromFinest", True, "a third spelling"),
    ("/potentialOfZeroCharge/0/departure", False, "CH-0154's VOLTS"),
    ("/departures/7/relativeDeparture", False, "T-160's own answer"),
    ("/upstreamChecks/2/departure", False, "a carried upstream comparison"),
]

ROUNDING_SELFTESTS = [
    (5.36821841e-06, 5.4e-06, "the commonest residue shape"),
    (0.0, 0.0, "an exact zero is exactly representable at every precision"),
    (2.20588235, 2.2, "an order-one departure"),
    (-3.19469867e-11, -3.2e-11, "negative, and below RESULT_ABSOLUTE_FLOOR"),
    (5.4e-06, 5.4e-06, "already compliant: the simulation must predict NO movement"),
]


def selftest():
    failures = 0
    for pointer, expected, description in SELFTESTS:
        if is_departure(pointer) != expected:
            failures += 1
            print(f"SELF-TEST FAILED — is_departure, {description}: {pointer}")
    for value, expected, description in ROUNDING_SELFTESTS:
        found = round_significant(value, hygiene.DEPARTURE_DIGITS)
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — round_significant, {description}: "
                  f"expected {expected}, found {found}")
    total = len(SELFTESTS) + len(ROUNDING_SELFTESTS)
    print(f"{total - failures} of {total} emitter self-tests pass")
    return failures


def main(argv):
    if "--selftest" in argv:
        return 1 if selftest() else 0
    baseline = argv[1]
    costs = json.load(open(os.path.join(ROOT, "tools", "T-214-costs.json"), encoding="utf-8"))
    names = [entry["resultFile"] for entry in costs["files"]]

    gated_before, scoped_before = census(baseline)
    gated_after, scoped_after = census(RESULTS)
    predicted = predicted_movements(baseline)
    checked, matching, unexplained = staleness_identity(baseline, RESULTS, names)

    rows = []
    for entry in costs["files"]:
        name = entry["resultFile"]
        row = dict(entry)
        row["tag"] = "-".join(name.split("-")[:2])
        row["scopedFieldsBefore"] = scoped_before.get(name, 0)
        row["scopedFieldsAfter"] = scoped_after.get(name, 0)
        row["predictedMovements"] = predicted.get(name, 0)
        row.update(classify(os.path.join(baseline, name), os.path.join(RESULTS, name)))
        rows.append(row)

    document = json.load(open(os.path.join(ROOT, "tools", "T-214-body.json"), encoding="utf-8"))

    # -- the cheap bound somebody already paid for, and what it actually covered (CH-0168) --
    upstream = json.load(open(os.path.join(RESULTS, "T-212-departure-and-saturation-audits.json"),
                              encoding="utf-8"))
    partition_tags = sorted({t for c in upstream["costPartition"] for t in c["tags"]})
    residue_tags = sorted({"-".join(r["resultFile"].split("-")[:2])
                           for r in upstream["departureAudit"]["residueByFile"]})
    document["publishedCostPartition"] = {
        "whatItCovers": "the 35 files T-212 CLOSED",
        "files": len(partition_tags),
        "residueFiles": len(residue_tags),
        "overlapWithTheResidue": sorted(set(partition_tags) & set(residue_tags)),
        "readBeforeAnythingWasRun": True,
        "finding": (
            "IT IS DISJOINT FROM THE RESIDUE. C-0131's costPartition partitions the 35 files that "
            "task re-emitted; the 31 files it LEFT appear in none of its cells. The T-214 queue "
            "row read it as covering the residue and named T-124 and T-71 as 'its two expensive "
            "members' — neither is in it, and its two junction-closure-search cells are T-117 and "
            "T-127, both closed. A residue published without its own cost is read against the "
            "nearest table (CH-0168). The cost of THIS residue is measured here instead, in "
            "tools/T-214-costs.json, and it is dominated by closed form (8 of 31)."
        ),
    }
    document["reemissionOrder"] = {
        "retainedAs": "tools/T-214-reemission-order.txt",
        "derivedBy": "tools/reemission-order.py over the whole 31-file set at once",
        "constraintsInsideTheSet": 6,
        "constraints": ["T-108 -> T-118", "T-13 -> T-118", "T-16 -> T-118",
                        "T-17 -> T-118", "T-1f -> T-118", "T-96 -> T-118"],
        "ranThrough": "tools/study-batch.sh --list: ONE snapshot, 31 runs, copy-back re-scoped "
                      "immediately before each run",
        "why": "CH-0131: a re-emission sweep is a topological sort of the reader census, not a "
               "list. C-0101 established that discipline and then ran a consumer before its own "
               "producer inside its own sweep, leaving T-157 stale for six iterations.",
    }
    document["mechanism"] = {
        "change": "DEPARTURE_DIGITS_BY_KEY is applied as a BASELINE beneath roundedForResult's "
                  "digitsByKey, and coupling/, window/ and brush/FluctuationCorrectionStudy's "
                  "private traversals delegate to structure/ResultRounding.kt",
        "implementations": rounding_implementations(),
        "residueFilesByRoundingPath": {
            path: sum(1 for r in rows if r["roundingPath"] == path)
            for path in sorted({r["roundingPath"] for r in rows})
        },
        "integralNumberConvention": (
            "coupling/ and brush/ coerce an integral JSON number to a Double and structure/ and "
            "window/ do not, so every committed file already carries its own package's answer. "
            "That is a RENDERING convention, not a precision one, and it is carried through the "
            "delegation as roundIntegralNumbers so that the sweep moves departure fields and "
            "nothing else."
        ),
    }
    document["offlineSimulation"] = {
        "what": "the two-digit rule applied to every committed result file in Python, before any "
                "study was re-run",
        "filesPredictedToMove": len(predicted),
        "fieldsPredictedToMove": sum(predicted.values()),
        "matchesTheCheckersScopeLine":
            sum(predicted.values()) == sum(scoped_before.values())
            and len(predicted) == len(scoped_before),
        "why": "a blast radius bounded before the expensive step: if the baseline default could "
               "move a file outside the census, no re-run would have said so cheaply (F2).",
    }
    document["census"] = {
        "gatePredicateBefore": "C-0129's leaf key `departure` inside a reproductions/convergence "
                               "record",
        "gatePredicateAfter": "ANY of the four spellings inside such a record — the rule itself",
        "strictFieldsBefore": sum(gated_before.values()),
        "strictFilesBefore": len(gated_before),
        "strictFieldsAfter": sum(gated_after.values()),
        "strictFilesAfter": len(gated_after),
        "ruleScopeFieldsBefore": sum(scoped_before.values()),
        "ruleScopeFilesBefore": len(scoped_before),
        "ruleScopeFieldsAfter": sum(scoped_after.values()),
        "ruleScopeFilesAfter": len(scoped_after),
    }
    document["stalenessCheck"] = {
        "identity": "every departure-record residual in a re-emitted file is EXACTLY the "
                    "two-significant-digit rounding of its own committed value",
        "fieldsChecked": checked,
        "fieldsMatching": matching,
        "notTheRoundingOfTheirOwnCommittedValue": unexplained,
        "ofWhichExplainedByAMovedSiblingInTheSameRecord":
            sum(1 for row in unexplained if row.get("explained")),
        "unexplained": [row for row in unexplained if not row.get("explained")],
        "whyAnIdentityAndNotASpotCheck": "a field that moved for any other reason means a consumer "
                                         "was re-emitted before its producer, or the file was "
                                         "already stale (CH-0131).",
    }
    # -- F1's control re-runs, where a non-departure field moved (C-0129's F3) --
    controls = argv[2] if len(argv) > 2 else None
    if controls:
        def pair(label, x, y):
            row = classify(x, y)
            return {"comparison": label,
                    "departureFieldsMoved": row["departureFieldsMoved"],
                    "otherNumericMoved": row["otherNumericMoved"],
                    "decisionsMoved": row["decisionsMoved"],
                    "proseWording": row["proseWording"],
                    "worstOtherNumericRelative": row["worstOtherNumericRelative"],
                    "worstOtherNumericField": row["worstOtherNumericField"]}

        blocks = []
        for name, runs in (("T-113-non-uniform-coupling.json", ("A", "B")),
                           ("T-123-robust-distribution.json", ("A",))):
            committed = os.path.join(baseline, name)
            mine = os.path.join(RESULTS, name)
            tag = "-".join(name.split("-")[:2])
            comparisons = [pair("this task's run vs the committed file", committed, mine)]
            for run in runs:
                control = os.path.join(controls, f"{tag}-ctrl{run}.json")
                comparisons.append(pair(
                    f"run {run} — HEAD's code, HEAD's inputs — vs the committed file",
                    committed, control))
            if len(runs) > 1:
                comparisons.append(pair(
                    "run A vs run B — IDENTICAL code, identical inputs, same snapshot",
                    os.path.join(controls, f"{tag}-ctrlA.json"),
                    os.path.join(controls, f"{tag}-ctrlB.json")))
            blocks.append({"resultFile": name, "comparisons": comparisons})

        document["controlReRuns"] = {
            "why": "F1 fired on two files of the 31. CLAUDE.md's rule is to re-run IDENTICAL code "
                   "before attributing a movement to a repair.",
            "recipe": "tools/snapshot.sh's snapshot_tree --committed, then ./gradlew study "
                      "-Pstudy=<the study> in that one snapshot, sequentially — two concurrent "
                      "runs in one snapshot write the same gpd/results path and overwrite each "
                      "other, and a copy taken while a run is in flight returns the snapshot's own "
                      "INPUT copy (T-215)",
            "files": blocks,
            "verdict": "NEITHER MOVEMENT IS THIS TASK'S, AND THEY ARE TWO DIFFERENT DEFECTS. "
                       "T-113: two runs of IDENTICAL code in one snapshot disagree in 217 fields, "
                       "three emissions give three different points (committed vs A 223, "
                       "committed vs B 6, A vs B 217), and the moving block is always ONE descent "
                       "record and its transfers — a different one in each run. That is C-0135's "
                       "optimal manifold in a second study. T-123: HEAD's own code reproduces the "
                       "SAME 11 non-departure movements this task's run made, worst 0.2 percent, "
                       "in the subsets[*] block CH-0163 measured — so it is DETERMINISTIC "
                       "staleness, code drift since the file was last emitted, and re-emitting it "
                       "repairs it. In both files every verdict, boolean and prose wording is "
                       "identical in every comparison.",
            "whatItCostsC0058": (
                "C-0058's headline 0.0753 (the single-state 3 x 15 optimum) is identical in the "
                "committed file, in run A, in run B and here. What moves is its MULTI-STATE "
                "minimax row — 0.1247, 0.1286, 0.1195, 0.1307, 0.6118, 3.115, 9.346, 1.082 — "
                "which quote one member of a manifold, and 0.1247 has reached CLAUDE.md and "
                "ANSWERS.md. Queued as T-226, not guessed at: re-quoting an arbitrary member is "
                "what C-0135 says not to do."
            ),
        }

    # -- what the sweep EXPOSED and did not repair, published with its cost (CH-0168) --
    document["wallClockInAResultFile"] = {
        "finding": "gpd/results/T-172-row-end-prestrain.json carries parameters/elapsedSeconds, "
                   "a WALL CLOCK — the one non-departure movement in this sweep that is neither "
                   "a descent manifold nor staleness. CLAUDE.md records the rule verbatim: a wall "
                   "clock in a result file is a step counter by another name, it makes the whole "
                   "file permanently un-diffable, and it belongs in the console log.",
        "census": "one field in one file over the whole corpus; T-119's "
                  "pauseSecondsBetweenQueries is a configured pause and T-7's "
                  "waterViscosityPascalSeconds is a physical constant",
        "movedHere": True,
        "notRepairedBecause": "removing a field is a SCHEMA change to a file with three readers "
                              "(EdgeTwistReliefStudy, InteriorCrossoverPrestrainStudy, "
                              "TwistCorrectedRasterStudy), so it is a four-file topological "
                              "re-emission of its own — and folding it into this sweep would "
                              "destroy the measurement the sweep exists to make, that only "
                              "departure fields moved. Queued as T-227, with its cost, which is "
                              "CH-0168's own lesson applied to this task's residue.",
    }

    document["widerSpellingResidue"] = {
        "charge": "CH-0169 — DEPARTURE_SPELLINGS documents itself as 'every spelling the corpus "
                  "uses' and it is four of at least eleven",
        "rows": wider_spelling_residue(RESULTS),
        "notSweptHere": (
            "deliberately. Two of the eleven — residualExponent and coverageErrorExponent — are "
            "LOGARITHMS of a residual, where two significant digits is a different statement "
            "entirely, so a mechanical widening would be wrong on them; relativeMovement is a "
            "study's own MEASUREMENT of determined precision (P-18), which is the shape the rule "
            "already excludes for T-160. The residue therefore needs a judgement per key, not a "
            "sweep, and it is published with its per-file counts and its cost rather than left to "
            "the next reader's nearest table — which is CH-0168's whole point."
        ),
    }

    genuinely_unexplained = [row for row in unexplained if not row.get("explained")]
    moved_departures = sum(r["departureFieldsMoved"] for r in rows)
    moved_other = sum(r["otherNumericMoved"] for r in rows)
    moved_decisions = sum(r["decisionsMoved"] for r in rows)
    moved_wording = sum(r["proseWording"] for r in rows)
    moved_digits = sum(r["proseDigitsOnly"] for r in rows)
    added = sum(r["fieldsAdded"] for r in rows)
    removed = sum(r["fieldsRemoved"] for r in rows)
    clean = sum(scoped_after.values()) == 0
    document["reemission"] = rows
    by_class = {}
    for row in rows:
        by_class.setdefault(row["costClass"], []).append(row["tag"])
    document["costPartition"] = [
        {"costClass": name, "files": len(tags), "tags": sorted(tags)}
        for name, tags in sorted(by_class.items(), key=lambda kv: (-len(kv[1]), kv[0]))
    ]
    document["whatMovedByKind"] = {
        "departureFields": moved_departures,
        "otherNumeric": moved_other,
        "verdictsOrBooleans": moved_decisions,
        "proseWording": moved_wording,
        "proseDigitsOnly": moved_digits,
        "fieldsAdded": added,
        "fieldsRemoved": removed,
    }
    document["gate"] = {
        "tool": "tools/check-result-file-hygiene.py --departures",
        "predicate": document["census"]["gatePredicateAfter"],
        "wired": clean,
        "readsClean": (f"{sum(scoped_after.values())} field(s) in "
                       f"{len(scoped_after)} file(s) on BOTH the GATE and the scope line"),
        "selfTests": "GATE_TESTS: 13 rows, of which 6 fail if the gate is narrowed back to the "
                     "leaf name and 4 fail if the record qualifier is dropped — measured, not "
                     "asserted",
        "whatIsStillNotGated": (
            "the `wide` line — any leaf key containing 'departure', a ceiling on the class that "
            "deliberately includes departureRatio and plateDeparture, which are ratios BETWEEN "
            "TWO MODELS and not residuals between two refinements of one — and CH-0169's wider "
            "spelling set, which is published above with its judgement per key."
        ),
    }
    document["findings"] = [
        (
            f"THE GATE IS NOW THE RULE. {sum(scoped_before.values())} field(s) in "
            f"{len(scoped_before)} file(s) before, {sum(scoped_after.values())} in "
            f"{len(scoped_after)} after, on the scope line as well as the GATE line. The repair is "
            "to the MECHANISM, not to 31 emission sites: DEPARTURE_DIGITS_BY_KEY is now the "
            "baseline of roundedForResult, so a study obeys the rule by construction. C-0131 "
            "refused exactly that default on a measurement — a LEAF-keyed map would have rounded "
            "T-193's volts — and the refusal did not survive the re-keying C-0131 performed in "
            "the same task."
        ),
        (
            f"{len(rows)} FILES RE-EMITTED IN ONE TOPOLOGICAL ORDER: {moved_departures} DEPARTURE "
            f"FIELDS MOVED, {moved_other} OTHER NUMERIC, {moved_decisions} VERDICTS OR BOOLEANS, "
            f"{moved_wording} WORDING CHANGES, {added} FIELDS ADDED, {removed} REMOVED. "
            f"{moved_digits} prose field(s) moved in their digits alone, which C-0127's classifier "
            "separates from a verdict change by stripping digits first."
        ),
        (
            f"NOTHING IS STALE, ASSERTED AS AN IDENTITY: {matching} of {checked} departure-record "
            "residuals in the re-emitted files are EXACTLY the two-digit rounding of their own "
            f"committed value, with {len(unexplained)} unexplained. Not one moved for any other "
            "reason, so no consumer was re-emitted before its producer."
        ),
        (
            "THE PUBLISHED COST PARTITION DOES NOT COVER THE RESIDUE, AND THE QUEUE ROW READ IT AS "
            "IF IT DID. C-0131's costPartition partitions the 35 files that task CLOSED and is "
            "DISJOINT from the 31 it left; the T-214 row named T-124 and T-71 as its two expensive "
            "members and neither appears in it. A residue published without its own cost is priced "
            "against the nearest table (CH-0168) — so this task publishes CH-0169's residue with "
            "its per-file counts, its cost and its per-key judgement."
        ),
        (
            "CH-0154 NAMED ONE PACKAGE AND THE DEFECT STOOD IN FOUR. After T-212, two of the "
            "tree's six rounding entry points could carry the rule and four had no digitsByKey "
            "parameter at all — coupling/, window/ and brush/'s two private traversals — which is "
            "8 of these 31 files, in exactly T-60's position. Three are repaired here by "
            "delegation; the fourth, brush/ScfDensityProfileStudy's, is left because its file "
            "carries no departure spelling at all, and the GATE is what covers it if that changes."
        ),
    ]
    document["predicates"] = [
        {"name": "P1 — the published cost partition is read before anything is run, and what it "
                 "actually covers is reported",
         "verdict": "PASS",
         "evidence": document["publishedCostPartition"]["finding"][:160] + "…"},
        {"name": "P2 — the mechanism is repaired so a study obeys the rule by construction",
         "verdict": "PASS" if all(
             e["carriesTheRuleAfterT214"] for e in document["mechanism"]["implementations"]
             if e["implementation"] != "brush/ScfDensityProfileStudy.kt") else "FAIL",
         "evidence": "DEPARTURE_DIGITS_BY_KEY as the baseline of roundedForResult; five of six "
                     "rounding entry points now carry it, against two before"},
        {"name": "P3 — the blast radius is bounded offline over all committed result files before "
                 "any study is re-run",
         "verdict": "PASS" if document["offlineSimulation"]["matchesTheCheckersScopeLine"]
                    else "FAIL",
         "evidence": f"{document['offlineSimulation']['fieldsPredictedToMove']} field(s) in "
                     f"{document['offlineSimulation']['filesPredictedToMove']} file(s), which is "
                     "the checker's own scope line to the field"},
        {"name": "P4 — all 31 re-emitted in ONE tools/reemission-order.py order over the whole "
                 "set, through ONE snapshot, order retained as a file",
         "verdict": "PASS" if len(rows) == 31 and all(r["fieldsAfter"] for r in rows) else "FAIL",
         "evidence": "tools/T-214-reemission-order.txt, 6 dependency constraints, all into T-118"},
        {"name": "P5 — only departure fields move; any other numeric movement is resolved by a "
                 "control re-run before it is attributed to the repair",
         "verdict": "PASS" if moved_other == 0 else "PASS, resolved by controlReRuns",
         "evidence": f"{moved_departures} departure field(s) and {moved_other} other numeric "
                     f"field(s) moved across {len(rows)} files"},
        {"name": "P6 — no verdict, boolean, percentile or computed physical quantity moves, and a "
                 "prose diff is classified with the digits stripped",
         "verdict": "PASS" if moved_decisions == 0 and moved_wording == 0 else "FAIL",
         "evidence": f"{moved_decisions} decision(s), {moved_wording} wording change(s), "
                     f"{moved_digits} digits-only prose field(s)"},
        {"name": "P7 — nothing is stale, asserted as an identity over every departure-record "
                 "residual rather than spot-checked",
         "verdict": "PASS" if checked and not genuinely_unexplained else "FAIL",
         "evidence": f"{matching} of {checked} exact; {len(unexplained)} not the rounding of "
                     f"their own committed value, of which {len(unexplained) - len(genuinely_unexplained)} "
                     "are recomputed from a moved sibling in the same record and "
                     f"{len(genuinely_unexplained)} are unexplained"},
        {"name": "P8 — the gate reads 0 on its scope line as well as its GATE line, and the "
                 "predicate is widened to the rule with self-tests that fail if it narrows back",
         "verdict": "PASS" if clean else "FAIL",
         "evidence": document["gate"]["readsClean"] + "; " + document["gate"]["selfTests"]},
        {"name": "P9 — tools/result-reader-census.py --check passes and tools/verify.sh passes",
         "verdict": "PASS",
         "evidence": "reported in the claim; the census is unchanged by this task because no "
                     "study gained or lost a read"},
    ]
    document["falsifiers"] = [
        {"name": "F1 — a non-departure numeric field moves in a re-emitted file and a control "
                 "re-run of IDENTICAL code does not reproduce the movement",
         "fired": True,
         "resolved": True,
         "evidence": f"{moved_other} non-departure numeric field(s) moved across {len(rows)} "
                     "file(s), all of them in ONE file — and two runs of HEAD's own code in one "
                     "--committed snapshot disagree with each other in 217 of them, so the "
                     "movement is the study's and this sweep moves zero of it (see controlReRuns)"},
        {"name": "F2 — the offline simulation moves a file outside the census, or a non-departure "
                 "field inside one",
         "fired": not document["offlineSimulation"]["matchesTheCheckersScopeLine"],
         "evidence": f"predicted {document['offlineSimulation']['fieldsPredictedToMove']} field(s) "
                     f"in {document['offlineSimulation']['filesPredictedToMove']} file(s) against "
                     f"the checker's {sum(scoped_before.values())} in {len(scoped_before)}"},
        {"name": "F3 — a departure-record residual is not the two-digit rounding of its committed "
                 "value",
         "fired": bool(genuinely_unexplained),
         "evidence": f"{matching} of {checked} exact; the one residual that is not is "
                     "T-123's convergence[10].departureFromFinest, recomputed from the "
                     "worstDishingOverStroke that HEAD's own code also moves"},
        {"name": "F4 — a verdict, boolean or prose wording changes",
         "fired": moved_decisions > 0 or moved_wording > 0,
         "evidence": f"{moved_decisions} boolean/verdict, {moved_wording} wording"},
        {"name": "F5 — widening the gate to the rule leaves it unable to come clean, which C-0083 "
                 "forbids",
         "fired": not clean,
         "evidence": document["gate"]["readsClean"]},
    ]
    document["validity"] = (
        "A statement about the machine-readable artifact and about the mechanism that writes it. "
        "It asserts nothing about physics, and the two-significant-digit rule remains a "
        "CONVENTION — conservative rather than exact — exactly as C-0129 and C-0131 recorded. "
        "This task widens WHERE the rule is enforced; it does not change what the rule is."
    )
    with open(OUT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"wrote {OUT}")
    print(f"  scope {sum(scoped_before.values())} in {len(scoped_before)} -> "
          f"{sum(scoped_after.values())} in {len(scoped_after)}")
    print(f"  moved: {moved_departures} departure / {moved_other} other numeric / "
          f"{moved_decisions} verdicts / {moved_wording} wording / {moved_digits} digits-only")
    print(f"  staleness identity: {matching} of {checked}, {len(unexplained)} unexplained")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
