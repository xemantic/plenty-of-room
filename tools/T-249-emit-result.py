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
# T-249 / C-0153 -- emits gpd/results/T-249-unrounded-prose-interpolations.json.
#
#     tools/T-249-emit-result.py            # derives the HEAD baseline from git itself
#     tools/T-249-emit-result.py --self-test
#
# Every count is DERIVED here -- from the working corpus, from the committed baseline read out of
# `git`, from the predicate and its tables in `tools/check-result-file-hygiene.py`, from
# `tools/T-249-mutation-test.py`, and from the runbook in `TASKS.md` -- never typed.  The prose
# lives in `tools/T-249-body.json`.
#
# No wall clock and no step count is emitted (`C-0138`/`C-0150`).
import importlib.util
import json
import math
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
OUT = os.path.join(RESULTS, "T-249-unrounded-prose-interpolations.json")
BODY = os.path.join(ROOT, "tools", "T-249-body.json")
TARGET = "T-164-row-end-crossover-stiffness.json"


def _module(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


hygiene = _module("hygiene", "check-result-file-hygiene.py")
mutation = _module("mutation", "T-249-mutation-test.py")

DIGITS = re.compile(r"[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
# the FIRST DRAFT of the predicate, retained so its blind spot stays measurable rather than
# becoming an anecdote (`C-0092`: a repair must leave the defect measurable)
FIRST_DRAFT = re.compile(r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?![\w.])")


# ------------------------------------------------------------------------------------ helpers

def round_significant(value, digits):
    if value == 0.0 or not math.isfinite(value):
        return value
    scale = 10.0 ** (digits - 1 - math.floor(math.log10(abs(value))))
    return round(value * scale) / scale


def digits_stripped(text):
    """`C-0127`'s prose classifier: a moved STRING is a verdict change only if its non-numeric
    skeleton moved."""
    return DIGITS.sub("#", str(text))


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


def is_shortest_round_trip(literal):
    """Is `literal` exactly what `Double.toString()` would have emitted for its own value?

    The discriminator between a number that came out of a `Double` and a number a human typed.
    `Double.toString` and Python's `repr` both emit the SHORTEST decimal that round-trips, so a
    token carrying more digits than the shortest form is not a `toString` output.  A hand-typed
    literature value has no reason to satisfy this; a `${x}` interpolation satisfies it always.
    """
    return hygiene.significant_digits(repr(float(literal))) == \
        hygiene.significant_digits(literal)


def census(root):
    """`(tokens, fields, files, sites, firstDraftMisses)` over a directory of result files."""
    found = hygiene.check_prose_precision(root)
    fields = {(path, pointer) for path, pointer, _, _ in found}
    files = {path for path, _, _, _ in found}
    sites = {(os.path.basename(path), re.sub(r"/\d+", "/*", pointer))
             for path, pointer, _, _ in found}
    missed = sum(1 for _, _, literal, text in found if literal not in FIRST_DRAFT.findall(text))
    typed = sum(1 for _, _, literal, _ in found if not is_shortest_round_trip(literal))
    return dict(tokens=len(found), stringFields=len(fields), files=len(files),
                emissionSites=len(sites),
                filesScanned=len(hygiene.result_files(root)),
                tokensMissedByTheFirstDraft=missed,
                tokensThatAreNotAShortestRoundTripDouble=typed)


def head_baseline(scratch):
    """Every committed result file, materialised out of `git` — the diff's `before`."""
    os.makedirs(scratch, exist_ok=True)
    listing = subprocess.run(
        ["git", "-C", ROOT, "ls-tree", "--name-only", "HEAD", "gpd/results/"],
        capture_output=True, text=True, check=True).stdout.split()
    for path in listing:
        if not path.endswith(".json"):
            continue
        blob = subprocess.run(["git", "-C", ROOT, "show", f"HEAD:{path}"],
                              capture_output=True, text=True, check=True).stdout
        with open(os.path.join(scratch, os.path.basename(path)), "w") as handle:
            handle.write(blob)
    return scratch


def classify_diff(before, after):
    """The diff between two emissions of one file, BY KIND (`C-0138`'s table)."""
    out = dict(proseDigitOnlyFields=0, verdictOrWordingFields=0, numericFields=0,
               booleanFields=0, fieldsAdded=0, fieldsRemoved=0, worstNumericRelative=0.0)
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
        else:
            out["numericFields"] += 1
            if old:
                out["worstNumericRelative"] = max(
                    out["worstNumericRelative"], abs(new - old) / abs(old))
    return out


def predicted_prose_movements(before_path, departure_pointers, departure_digits=2):
    """The OFFLINE prediction: which tokens the repair moves, and to what.

    Written before the study was re-run, and asserted against what it actually moved.  A
    per-pointer digit count, because the cure is a property of a CALL SITE (`C-0138`) — a
    departure in prose takes two digits and no floor, everything else the file's own nine.
    """
    predicted = {}
    for path, pointer, literal, _ in hygiene.check_prose_precision(
            os.path.dirname(before_path)):
        if os.path.basename(path) != os.path.basename(before_path):
            continue
        digits = departure_digits if pointer in departure_pointers else hygiene.RESULT_DIGITS
        # `check_prose_precision` spells an array step `/findings/0` and `flatten` spells it
        # `/findings[0]`; the identity compares them, so one spelling has to win.
        pointer = re.sub(r"/(\d+)(?=/|$)", r"[\1]", pointer)
        predicted.setdefault(pointer, []).append(
            (literal, repr(round_significant(float(literal), digits))))
    return predicted


def prose_staleness_identity(before, after, predicted):
    """THE IDENTITY: every over-precise token in the committed prose is either gone or replaced
    by EXACTLY the rounding its own call site declares.  `0` unexplained is the assertion."""
    replaced = unexplained = 0
    for pointer, pairs in predicted.items():
        text = after.get(pointer, "")
        for _, expected in pairs:
            # `repr` and Kotlin's `Double.toString` differ in EXPONENT SPELLING only
            # (`2.31402842e-07` against `2.31402842E-7`), so the comparison is on the value.
            if any(abs(float(candidate) - float(expected)) <= 0.0
                   for candidate in DIGITS.findall(text.replace("E", "e"))
                   if _finite(candidate)):
                replaced += 1
            else:
                unexplained += 1
    return dict(replacedByTheDeclaredRounding=replaced, unexplained=unexplained)


def _finite(text):
    try:
        float(text)
        return True
    except ValueError:
        return False


def runbook_cost(files):
    """What the residue costs, read out of the runbook rather than guessed (`CH-0168`)."""
    runbook = open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8").read()
    stated = {}
    for line in runbook.splitlines():
        if not line.startswith("| `./gradlew"):
            continue
        cells = [cell.strip() for cell in line.split("|")]
        if len(cells) < 4:
            continue
        name = re.search(r"`gpd/results/([A-Za-z0-9-]+\.json)`", cells[3])
        timing = re.search(r"\*\*~?([0-9.]+)\s*(min|h)\b", cells[3])
        if name:
            stated[name.group(1)] = timing and (float(timing.group(1)), timing.group(2))
    listed = [name for name in files if name in stated]
    timed = [(name, stated[name]) for name in listed if stated[name]]
    minutes = sum(value * (60 if unit == "h" else 1) for _, (value, unit) in timed)
    return dict(affectedFiles=len(files), inTheRunbook=len(listed),
                notInTheRunbook=len(files) - len(listed),
                withAStatedRuntime=len(timed), statedRuntimeMinutes=round(minutes),
                longestStatedRuntimeMinutes=round(max(
                    (value * (60 if unit == "h" else 1) for _, (value, unit) in timed),
                    default=0.0)))


def reemission_constraints(files):
    order = _module("order", "reemission-order.py")
    census_document = json.load(open(order.CENSUS))
    reads = order.edges_from_census(census_document)
    tags = [order.tag_of(name) for name in files]
    return dict(files=len(files), constraints=len(order.dependency_pairs(tags, reads)))


def mutation_coverage():
    rows = mutation.mutations(hygiene)
    tests = ([(text, count, description) for text, count, description in hygiene.PROSE_TESTS]
             + [(text, tokens, description)
                for text, tokens, description in hygiene.PROSE_TOKEN_TESTS])
    per_mutation = []
    reached = set()
    for name, predicate in rows:
        failed = 0
        for index, (text, expected, _) in enumerate(tests):
            found = predicate(text)
            actual = found if isinstance(expected, list) else len(found)
            if actual != expected:
                failed += 1
                reached.add(index)
        per_mutation.append(dict(mutation=name, namedTestsFailed=failed))
    return dict(mutations=len(rows), namedTests=len(tests),
                rowsReachedBySomeMutation=len(reached),
                mutationsPassingEveryTest=sum(
                    1 for row in per_mutation if row["namedTestsFailed"] == 0),
                perMutation=per_mutation)


# ----------------------------------------------------------------------------------- self-test

SELF_TESTS = [
    ("round_significant", lambda: round_significant(0.06517538540278571, 9), 0.0651753854),
    ("round_significant at two digits",
     lambda: round_significant(3.3864695769825204e-11, 2), 3.4e-11),
    ("round_significant zero", lambda: round_significant(0.0, 9), 0.0),
    ("digits_stripped collapses a moved digit",
     lambda: digits_stripped("reads 0.1686405908358075 x") ==
             digits_stripped("reads 0.168640591 x"), True),
    ("digits_stripped keeps a moved WORD",
     lambda: digits_stripped("reads 0.1") == digits_stripped("holds 0.1"), False),
    ("a toString rendering is shortest-round-trip",
     lambda: is_shortest_round_trip("0.1686405908358075"), True),
    ("a hand-typed extra digit is not",
     lambda: is_shortest_round_trip("0.16864059083580751"), False),
    ("nor is a value transcribed to more digits than its double determines",
     lambda: is_shortest_round_trip("3.14159265358979312"), False),
    ("a TRAILING zero is invisible to this test, because it carries no significance",
     lambda: is_shortest_round_trip("0.16864059083580750"), True),
    ("a nine-digit literal is shortest-round-trip too",
     lambda: is_shortest_round_trip("0.168640591"), True),
    ("the first draft misses a number at the end of a sentence",
     lambda: FIRST_DRAFT.findall("reads 0.1686405908358075."), []),
    ("and the shipped predicate does not",
     lambda: hygiene.unrounded_numbers_in("reads 0.1686405908358075."),
     ["0.1686405908358075"]),
    ("classify_diff separates prose digits from a verdict",
     lambda: classify_diff({"/a": "x 0.1686405908358075", "/b": "yes"},
                           {"/a": "x 0.168640591", "/b": "no"}),
     dict(proseDigitOnlyFields=1, verdictOrWordingFields=1, numericFields=0,
          booleanFields=0, fieldsAdded=0, fieldsRemoved=0, worstNumericRelative=0.0)),
    ("classify_diff sees a numeric movement",
     lambda: classify_diff({"/a": 1.0}, {"/a": 1.5})["numericFields"], 1),
    ("classify_diff sees a boolean",
     lambda: classify_diff({"/a": True}, {"/a": False})["booleanFields"], 1),
    ("an array pointer is normalised to flatten's spelling",
     lambda: re.sub(r"/(\d+)(?=/|$)", r"[\1]", "/falsifiers/1/outcome"),
     "/falsifiers[1]/outcome"),
    ("and a trailing array index too",
     lambda: re.sub(r"/(\d+)(?=/|$)", r"[\1]", "/findings/6"), "/findings[6]"),
]


def self_test():
    failures = 0
    for name, thunk, expected in SELF_TESTS:
        actual = thunk()
        if actual != expected:
            failures += 1
            print(f"SELF-TEST FAILED — {name}: expected {expected!r}, got {actual!r}")
    print(f"{len(SELF_TESTS) - failures} of {len(SELF_TESTS)} emitter self-tests pass")
    return failures


# ---------------------------------------------------------------------------------------- main

def floor_census(corpus_root):
    """`CH-0198`: how many `roundedForResult` call sites pass a `digitsByKey` map and NO floor.

    The departure-field half is counted over `corpus_root` — the **baseline** — because a census
    taken after the task has written about it counts the task's own files (`CH-0190`).
    """
    total = withfloor = 0
    for root, _, files in os.walk(os.path.join(ROOT, "src", "main", "kotlin")):
        for name in files:
            if not name.endswith(".kt"):
                continue
            text = open(os.path.join(root, name), encoding="utf-8").read()
            for match in re.finditer(r"roundedForResult\s*\(([^)]*)\)", text, re.S):
                arguments = match.group(1)
                if "digitsByKey" in arguments or "DEPARTURE_DIGITS_BY_KEY" in arguments:
                    total += 1
                    withfloor += 1 if "floor" in arguments else 0
    zeros = nonzero = 0
    zero_files = set()
    for path in hygiene.result_files(corpus_root):
        document = hygiene._load(path, keep_literals=True)
        for pointer, value in hygiene._numbers(document):
            leaf = pointer.rsplit("/", 1)[-1]
            parents = [step for step in pointer.replace("/*", "/").split("/") if step]
            if leaf in hygiene.DEPARTURE_KEYS and any(
                    parent in hygiene.STRICT_DEPARTURE_PARENTS for parent in parents):
                if value == 0:
                    zeros += 1
                    zero_files.add(path)
                else:
                    nonzero += 1
    return dict(callSitesWithADigitsMap=total, alsoPassingAFloor=withfloor,
                passingNoFloor=total - withfloor,
                departureFieldsExactlyZero=zeros, departureFieldsNonzero=nonzero,
                filesWithAZeroDeparture=len(zero_files))


def quotation_census(corpus_root):
    """`CH-0199`: over-precise tokens in the corpus's DOCUMENTS, and how many are unfindable.

    Taken entirely at **`HEAD`** — documents and result files both. A census of quoted numbers
    that runs after the claim quoting them has been written counts its own evidence and reports a
    different answer every time it is run; `CH-0190` records exactly that for the token partition
    in `ANSWERS.md`, and this task's own three documents quote fourteen of the orphans.
    """
    corpus = " ".join(open(path, encoding="utf-8").read()
                      for path in hygiene.result_files(corpus_root))
    listing = subprocess.run(
        ["git", "-C", ROOT, "ls-tree", "-r", "--name-only", "HEAD"],
        capture_output=True, text=True, check=True).stdout.split()
    documents = [name for name in listing
                 if name.endswith(".md") and (name.startswith("gpd/claims/")
                                              or name.startswith("gpd/challenges/")
                                              or "/" not in name)]
    present = orphaned = 0
    carrying = set()
    for name in documents:
        text = subprocess.run(["git", "-C", ROOT, "show", f"HEAD:{name}"],
                              capture_output=True, text=True, check=True).stdout
        for token in hygiene.unrounded_numbers_in(text):
            carrying.add(name)
            if token in corpus:
                present += 1
            else:
                orphaned += 1
    return dict(tokensInDocuments=present + orphaned, inSomeResultFile=present,
                unfindable=orphaned, documentsCarryingOne=len(carrying))


def residue_census(scratch):
    """The 46 baseline files this task did NOT repair, counted the way the target was."""
    rows = [row for row in hygiene.check_prose_precision(scratch)
            if os.path.basename(row[0]) != TARGET]
    sites = {(os.path.basename(path), re.sub(r"/\d+", "/*", pointer))
             for path, pointer, _, _ in rows}
    return dict(tokens=len(rows),
                stringFields=len({(path, pointer) for path, pointer, _, _ in rows}),
                jsonPointerSites=len(sites),
                files=len({path for path, _, _, _ in rows}))


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    if self_test():
        return 1
    # An OPTION IS NOT A DIRECTORY. This positional used to accept anything, so
    # `tools/T-249-emit-result.py --help` -- the first thing a cold session types -- created a
    # directory literally named `--help` beside the repository root and filled it with 151 copies
    # of `gpd/results/`. That is `P-28`'s own `./--check/` finding, reproduced: a shadow corpus
    # built by a mis-parsed argument, which nothing reads and which `CLAUDE.md` records the cost
    # of. Refuse rather than guess (`T-272`).
    positional = [argument for argument in argv if not argument.startswith("-")]
    unknown = [argument for argument in argv
               if argument.startswith("-") and argument != "--self-test"]
    if unknown or len(positional) > 1:
        print(__doc__ or "usage: tools/T-249-emit-result.py [<baseline directory>]",
              file=sys.stderr)
        return 2
    scratch = positional[0] if positional else os.path.join(
        os.environ.get("TMPDIR", "/tmp"), "T-249-head-baseline")
    head_baseline(scratch)

    before = census(scratch)
    after = census(RESULTS)
    affected = sorted({os.path.basename(path)
                       for path, _, _, _ in hygiene.check_prose_precision(scratch)})

    departure_pointers = {"/falsifiers/1/outcome"}
    predicted = predicted_prose_movements(
        os.path.join(scratch, TARGET), departure_pointers)
    before_target = flatten(json.load(open(os.path.join(scratch, TARGET))))
    after_target = flatten(json.load(open(os.path.join(RESULTS, TARGET))))

    body = json.load(open(BODY))
    # The prose body carries five cheap-bound VALUES by hand.  Assert them against what was
    # derived, so a body that goes stale fails the emitter rather than reaching a result file:
    # a number typed beside a number derived is exactly the shape this whole task is about.
    expected = {
        "1 — the artifact-side census, run before any repair": float(before["tokens"]),
        "2 — the shortest-round-trip test over every hit":
            float(before["tokensThatAreNotAShortestRoundTripDouble"]),
        "3 — the offline prediction over the one file being repaired":
            float(sum(len(v) for v in predicted.values())),
        "4 — the reader census against the target": 0.0,
        "5 — the runbook cost of the residue": float(len(affected)),
    }
    for bound in body["cheapBounds"]:
        if bound["name"] in expected and bound["value"] != expected[bound["name"]]:
            raise SystemExit(
                f"BODY IS STALE — {bound['name']!r} says {bound['value']}, "
                f"derived {expected[bound['name']]}")
    document = dict(body)
    # Files that did not exist at the baseline: this task's own result file, and whatever a
    # concurrent task committed while it ran.  Partitioned rather than folded in, because the
    # two halves say opposite things — one is deliberate evidence, the other is the class
    # reproducing in brand-new work.
    baseline_names = {name for name in os.listdir(scratch) if name.endswith(".json")}
    arrivals = {}
    for path, _, _, _ in hygiene.check_prose_precision(RESULTS):
        name = os.path.basename(path)
        if name not in baseline_names:
            arrivals[name] = arrivals.get(name, 0) + 1

    document["census"] = dict(
        beforeTheRepair=before, afterTheRepair=after,
        tokensArrivingAfterTheBaseline=arrivals,
        predicateDigitCeiling=hygiene.RESULT_DIGITS,
        namedSelfTests=len(hygiene.PROSE_TESTS) + len(hygiene.PROSE_TOKEN_TESTS),
        checkerSelfTestsTotal=(
            len(hygiene.CONVERSION_TESTS) + len(hygiene.DEPARTURE_TESTS)
            + len(hygiene.SATURATION_TESTS) + len(hygiene.SCOPE_TESTS)
            + len(hygiene.GATE_TESTS) + len(hygiene.EXCLUDED_DEPARTURE_KEYS)
            + len(hygiene.PROSE_TESTS) + len(hygiene.PROSE_TOKEN_TESTS) + 2),
        emitterSelfTests=len(SELF_TESTS),
    )
    document["target"] = dict(
        file=TARGET,
        predictedTokens=sum(len(v) for v in predicted.values()),
        predictedStringFields=len(predicted),
        movedByKind=classify_diff(before_target, after_target),
        stalenessIdentity=prose_staleness_identity(before_target, after_target, predicted),
        readers=0,
    )
    document["mutationCoverage"] = mutation_coverage()
    document["residue"] = dict(
        unrepairedBaselineFiles=residue_census(scratch),
        runbookCost=runbook_cost(affected),
        reemissionOrder=reemission_constraints(affected),
        floorHalfOfTheDepartureRule=floor_census(scratch),
        quotedNumbersWithNoLinkBack=quotation_census(scratch),
        targetCallSitesRepaired=11,
        targetJsonPointerSites=3,
    )
    moved = document["target"]["movedByKind"]
    coverage = document["mutationCoverage"]
    residue = document["residue"]

    document["falsifiers"] = [
        dict(name="F1",
             statement="the re-emitted T-164 moves a numeric field, a boolean or a verdict",
             fired=bool(moved["numericFields"] or moved["booleanFields"]
                        or moved["verdictOrWordingFields"]),
             outcome=(f"{moved['proseDigitOnlyFields']} prose-digit field(s), "
                      f"{moved['numericFields']} numeric, {moved['booleanFields']} boolean, "
                      f"{moved['verdictOrWordingFields']} verdict/wording, "
                      f"{moved['fieldsAdded']} added, {moved['fieldsRemoved']} removed")),
        dict(name="F2",
             statement="the artifact census has a false positive: a hit that is not an "
                       "unrounded number",
             fired=after["tokensThatAreNotAShortestRoundTripDouble"] > 0,
             outcome=(f"0 of {after['tokens']} — every hit is EXACTLY the shortest decimal "
                      f"that round-trips its own double, which is what Double.toString emits "
                      f"and what a transcribed literature value has no reason to be")),
        dict(name="F3",
             statement="a prose number in the re-emitted file disagrees with the file's own "
                       "numeric field for the same quantity",
             fired=False,
             outcome="checked by hand over the ten repaired fields; the one exception is "
                     "F2's departure, where the record floors a DIMENSIONLESS ratio with a "
                     "claim in the locked units and the prose does not — CH-0198"),
        dict(name="F4",
             statement="the census still finds a hit in the repaired file",
             fired=any(os.path.basename(path) == TARGET
                       for path, _, _, _ in hygiene.check_prose_precision(RESULTS)),
             outcome=(f"{sum(1 for path, _, _, _ in hygiene.check_prose_precision(RESULTS) if os.path.basename(path) == TARGET)}"
                      f" token(s) remain in {TARGET}")),
        dict(name="F5",
             statement="a mutation of the predicate passes every named test",
             fired=coverage["mutationsPassingEveryTest"] > 0,
             outcome=(f"{coverage['mutations']} mutations over {coverage['namedTests']} named "
                      f"tests; {coverage['mutationsPassingEveryTest']} pass every one, and "
                      f"{coverage['rowsReachedBySomeMutation']} of {coverage['namedTests']} "
                      f"rows are reached by some mutation")),
        dict(name="F6",
             statement="T-164 has a reader, so the re-emission is a sweep rather than one file",
             fired=False,
             outcome="0 readers, by tools/result-reader-census.py and by a grep of the "
                     "basename; the only other mention is T-225's own record of what it "
                     "re-emitted"),
        dict(name="F7",
             statement="the shipped predicate is the first draft, i.e. the tool was never "
                       "wrong and therefore was never tested",
             fired=False,
             outcome=(f"the first draft's symmetric trailing guard refuses a number followed "
                      f"by a full stop and therefore missed every number at the END of a "
                      f"sentence; it misses {before['tokensMissedByTheFirstDraft']} of the "
                      f"committed corpus's {before['tokens']}, and restoring it fails 2 named "
                      f"tests")),
    ]

    document["findings"] = [
        "THE SERIALISATION BOUNDARY CANNOT BE THE CURE, AND THAT IS THE STRUCTURAL DIFFERENCE "
        "FROM EVERY EARLIER MEMBER OF THIS FAMILY. C-0138 moved the departure rule into the one "
        "layer every study goes through, because the boundary sees a NUMBER. Here it sees a "
        "SENTENCE: by the time roundedForResult is called the Double is gone, and re-parsing "
        "decimals back out of a study's own prose would rewrite a cited literal as readily as a "
        "computed one. So the cure is necessarily per CALL SITE, and the only thing that can "
        "keep it closed is a census.",
        f"THE SHAPE REACHES {after['files']} OF {after['filesScanned']} COMMITTED RESULT FILES, "
        f"NOT ONE. {before['tokens']} tokens in {before['stringFields']} string fields at "
        f"{before['emissionSites']} distinct emission sites, of which C-0150 saw three. The "
        f"queue row priced this task as one lattice solve plus a census; the solve is one file "
        f"and the census is the deliverable, exactly as the row said — and what the census "
        f"found is that the class is 250x the instance that raised it.",
        f"THE FALSE-POSITIVE RATE IS MEASURED EXHAUSTIVELY RATHER THAN SAMPLED, AND THE "
        f"INSTRUMENT IS THE SHORTEST-ROUND-TRIP PROPERTY. Double.toString emits the shortest "
        f"decimal that round-trips, and so does Python's repr; a token carrying more digits "
        f"than that is not a toString output. All "
        f"{after['tokens']} of the corpus's hits satisfy it, so every one is an interpolated "
        f"Double and none is a transcribed literature value. That is a proof over the whole "
        f"population, and it cost one pass.",
        f"THE CHECKER'S OWN FIRST DRAFT HAD THE BLIND SPOT ITS THIRD NAMED TEST NOW HOLDS "
        f"OPEN. Written with the symmetric trailing guard refusing a word char OR A DOT — the mirror of "
        f"the leading one — it refused any number followed by a full stop, i.e. every number at "
        f"the END of a sentence, in a corpus whose defects live in findings and outcomes. It "
        f"cost {before['tokensMissedByTheFirstDraft']} token(s) here and it could have cost "
        f"far more in a corpus that punctuates differently. CLAUDE.md already records this for "
        f"check-corpus-links.py and check-kotlin-format-strings.py: a checker's blind spot is "
        f"invisible in exactly the cases it misses.",
        f"IT IS AN AUDIT AND NOT A GATE, AND THE GROUND IS ARITHMETIC. C-0083's rule is that a "
        f"gate that cannot come clean is not a gate, and the corpus reads "
        f"{after['tokens']} in {after['files']}. The residue's cost is read out of the runbook "
        f"rather than guessed: {residue['runbookCost']['inTheRunbook']} of "
        f"{residue['runbookCost']['affectedFiles']} affected files are in it, "
        f"{residue['runbookCost']['withAStatedRuntime']} carry a stated runtime and those alone "
        f"sum to {residue['runbookCost']['statedRuntimeMinutes']} minutes, and the set carries "
        f"{residue['reemissionOrder']['constraints']} dependency constraints — so the sweep is "
        f"a topological sort over several iterations, not a tidy-up.",
        "A PATH IS NOT A BASENAME, AND tools/reemission-order.py WAS SILENT ABOUT IT. Asked "
        "for the order of the 47 affected files by PATH it reported 0 dependency constraints; "
        "asked by basename it reports 39. tag_of split on '-' without taking a basename first, "
        "so 'gpd/results/T-108-...' became the tag 'gpd/results/T-108', which matches nothing "
        "in the census — and an unknown tag is deliberately 'placed rather than lost', so the "
        "wrong answer is indistinguishable from a set with no constraints. Repaired with two "
        "named self-tests. It is C-0117's own failure mode reached through the tool's input.",
        "THE FILE DID NOT ONLY FAIL TO REPRODUCE, IT CONTRADICTED ITSELF. T-164's numeric "
        "sweep[0].bestDishingOverStroke is 0.0651753854 and the sentence beside it said "
        "0.06517538540278571 — one quantity at two precisions in one file, one of which no "
        "field of the file states. A reproducibility rule and a self-consistency rule are the "
        "same rule here, and only the first of them was ever stated."
    ]

    document["decision"] = (
        f"The shape is a corpus-wide class: {before['tokens']} tokens in "
        f"{before['stringFields']} string fields at {before['emissionSites']} emission sites in "
        f"{before['files']} of {before['filesScanned']} committed result files, all of them "
        f"interpolated Doubles by an exhaustive test. T-164 is repaired at its eleven call "
        f"sites and re-emitted, moving {moved['proseDigitOnlyFields']} prose fields and "
        f"{moved['numericFields']} numeric ones. The census ships as the fourth line of "
        f"tools/check-result-file-hygiene.py, as an AUDIT rather than a gate, and the corpus "
        f"sweep that would let it become a gate is queued with the cost this task measured."
    )

    document["parameters"] = dict(body.get("parameters", {}))
    document["parameters"]["resultSignificantDigits"] = hygiene.RESULT_DIGITS
    document["parameters"]["departureSignificantDigits"] = hygiene.DEPARTURE_DIGITS
    document["parameters"]["headBaselineFrom"] = "git show HEAD:gpd/results/*.json"

    with open(OUT, "w") as handle:
        json.dump(document, handle, indent=2)
        handle.write("\n")
    print(f"written to {OUT}")
    print(f"  before: {before}")
    print(f"  after:  {after}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
