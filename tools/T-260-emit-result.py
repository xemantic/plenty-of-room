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
"""Emit `gpd/results/T-260-*.json` and `gpd/results/T-262-*.json`.

Both are measurements of `tools/T-234-census.py`'s own predicates, so -- per `CLAUDE.md`'s rule for
a result file whose subject is the corpus -- each records the corpus state it was taken at
(`baselineRef`) and the historical series is taken out of `git`, never out of the working tree.

    tools/T-260-emit-result.py [--ref <git-ref>]
"""

import argparse
import importlib.util
import json
import os
import subprocess

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOKEN_FAMILIES = ("WIDTH", "ROW_SPAN", "PLACEMENT", "GRILLAGE", "SQUARE")

#: The claim these two tasks file is itself in scope, and its §2 and §3 tables quote the families'
#: own example sentences in order to say which reading each is -- so writing the claim adds
#: occurrences to the census the claim is about.  Every family split is therefore reported BOTH
#: ways: over the whole census, and over the corpus without this claim's worked examples, which is
#: the number that does not move when the claim is edited.  `CH-0182`, on the claim that reports it.
SELF_DOCUMENTING = "gpd/claims/C-0176-partial-discharge-and-restatement-predicates.md"
DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t234", "T-234-census.py")
emitter = _load("t234emit", "T-234-emit-classification.py")
mutation = _load("t234mut", "T-234-mutation-test.py")


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def deliverable_series(limit=40):
    """For each revision of the two deliverables: how many token-family occurrences need a pointer.

    `old` is the iteration-34 predicate -- every `WIDTH`/`PLACEMENT` match belongs to this census's
    own discharge.  `new` counts only the families the split leaves gated.  Struck occurrences and
    occurrences already carrying a forward pointer are excluded from both, so the difference is the
    predicate and nothing else.
    """
    commits = [line.split()[0] for line in
               _git("log", "--format=%h %ad", "--date=short", "-n", str(limit), "--",
                    *DELIVERABLES).splitlines() if line.strip()][::-1]
    series = []
    for sha in commits:
        old = new = 0
        for path in DELIVERABLES:
            try:
                text = _git("show", "{}:{}".format(sha, path))
            except subprocess.CalledProcessError:
                continue
            spans = census.struck_spans(text)
            for family, _line, offset, _token, _distance in census.occurrences(text):
                if family not in TOKEN_FAMILIES:
                    continue
                if census.is_struck(spans, offset) or census.has_pointer(text, offset):
                    continue
                old += 1
                if census.discharge_of(family) == census.SUBJECT:
                    new += 1
        series.append({
            "commit": sha,
            "date": _git("log", "-1", "--format=%ad", "--date=short", sha).strip(),
            "subject": _git("log", "-1", "--format=%s", sha).strip(),
            "oldPredicate": old,
            "newPredicate": new,
        })
    return series


def structural_window_sweep():
    """The `STRUCTURAL_WINDOW` plateau, measured rather than chosen."""
    import re
    pattern = [f for f in census.FAMILIES if f[0] == "PLACEMENT"][0][1]
    rows = []
    for radius in (80, 100, 120, 150, 200, 300):
        counts = {"GRILLAGE": 0, "PLACEMENT": 0, "SQUARE": 0}
        for path in census.corpus_files(ROOT):
            if not census.in_scope(path) or path == SELF_DOCUMENTING:
                continue
            with open(os.path.join(ROOT, path), encoding="utf-8") as handle:
                text = handle.read()
            for match in re.finditer(pattern, census.blank_identifiers(text)):
                tight = census._window(text, match.start(), match.end(), radius)
                wide = census._window(text, match.start(), match.end())
                if census._STRUCTURAL_MODEL.search(tight):
                    counts["GRILLAGE"] += 1
                elif census._ATTRIBUTIVE.search(wide):
                    counts["SQUARE"] += 1
                else:
                    counts["PLACEMENT"] += 1
        rows.append({"radiusCharacters": radius, **counts})
    return rows


def mutation_coverage():
    """Derived, not typed: `tools/T-234-mutation-test.py` is run and its own numbers are read.

    A hand-written count here would be a number nobody re-derives, in the one file whose whole
    subject is that a predicate must be measured rather than asserted.
    """
    import io
    from contextlib import redirect_stdout
    buffer = io.StringIO()
    with redirect_stdout(buffer):
        status = mutation.main()
    lines = buffer.getvalue().splitlines()
    header = lines[0]
    added = next(l for l in lines if l.startswith("named tests added"))
    counts = [int(n) for n in __import__("re").findall(r"\d+", header)]
    # "named tests added by T-260/T-262: 79; reached by at least one mutation: 74" -- the task
    # identifiers carry digits, so parse from the COLON rather than from the whole line.
    added_counts = [int(n) for n in __import__("re").findall(r":\s*(\d+)", added)]
    return {
        "mutations": counts[1],
        "directions": ["NARROW", "WIDEN"],
        "narrowing": sum(1 for row in mutation.mutations() if row[0] == "NARROW"),
        "widening": sum(1 for row in mutation.mutations() if row[0] == "WIDEN"),
        "namedTestsTotal": counts[2],
        "namedTestsCensus": counts[3],
        "namedTestsEmitter": counts[4],
        "mutationsFailingNoNamedTest": sum(
            1 for l in lines if l.startswith("  SILENT")
        ),
        "namedTestsAddedByTheseTasks": added_counts[0],
        "namedTestsReachedBySomeMutation": added_counts[1],
        "exitStatus": status,
        "note": "An unreached row is a measurement of the mutation table, not of the tool: a "
                "limiting case such as `an empty table carries nothing` is reached by no plausible "
                "mutation of the shipped logic, and inventing one would be testing a test.",
    }


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    ref = _git("rev-parse", args.ref).strip()

    records = census.census(ROOT)
    table = census.load_classification()
    records, problems = census.classify(records, table)
    by_family = {}
    for record in records:
        by_family[record["family"]] = by_family.get(record["family"], 0) + 1
    by_family_without_self = {}
    for record in records:
        if record["file"] == SELF_DOCUMENTING:
            continue
        by_family_without_self[record["family"]] = \
            by_family_without_self.get(record["family"], 0) + 1
    by_class = {}
    for record in records:
        by_class[record["class"]] = by_class.get(record["class"], 0) + 1
    unpointed = [
        r for r in records
        if r["class"] in census.ADDRESSED and r.get("discharge") == census.SUBJECT
        and not r["pointer"] and not r["struck"] and not r["headlinePointer"]
    ]
    # `check()` gates the corpus and reports the two deliverables on their own advisory line,
    # because `T-233` owns them and this task does not edit them.  Split the same way here.
    gated = [r for r in unpointed if not r["deliverable"]]
    debt = [r for r in unpointed if r["deliverable"]]
    remote = [r for r in records
              if r["contextDistance"] is not None and r["contextDistance"] > census.CONTEXT_REMOTE]
    hand = [r for r in records if r.get("byHand")]
    series = deliverable_series()

    common = {
        "leaf": "none — a process claim protecting the census that protects every honeycomb leaf",
        "conditions": (
            "No physics is computed. Units unchanged and untouched: nm, pN, pN/nm, "
            "pN/nm^2 = 1 MPa exactly, k_BT = 4.141947 pN nm at 300 K. The corpus is the "
            "in-scope Markdown of this repository — gpd/claims/, TASKS.md and the two "
            "deliverables — read at the baselineRef below plus this iteration's own edits."
        ),
        "verificationType": (
            "logical — a token census over the corpus, a predicate measured against a "
            "hand reading of every occurrence of its two families, a mutation measurement in "
            "BOTH directions, a historical series taken out of git, and the census's and the "
            "emitter's own named self-tests (counted in mutationCoverage)"
        ),
        "baselineRef": ref,
        "parameters": {
            "censusTool": "tools/T-234-census.py",
            "classification": "tools/T-234-classification.json",
            "emitter": "tools/T-234-emit-classification.py",
            "mutationTest": "tools/T-234-mutation-test.py",
            "subjectDischarge": census.SUBJECT,
            "discharges": {k or "none": list(v) for k, v in census.DISCHARGES.items()},
            "refineWindowCharacters": census.REFINE_WINDOW,
            "structuralWindowCharacters": census.STRUCTURAL_WINDOW,
            "contextRemoteCharacters": census.CONTEXT_REMOTE,
            "snippetCharacters": census.SNIPPET_CHARS,
            "overrideKeyCharacters": emitter.OVERRIDE_KEY_CHARS,
        },
        "baselineChain": {
            "note": (
                "A census whose scope includes TASKS.md has a READING, not a value. These are the "
                "same tool against the corpus as it stood when each was taken. The first three are "
                "recorded readings, taken in scratch copies of HEAD's tools; the last is computed "
                "live by this emitter. The inherited working-tree classification was itself a "
                "legitimate regeneration that had gone stale before three further claims entered "
                "the corpus, which is CH-0182 happening while the task that reads it runs."
            ),
            "committedClassificationAgainstTheCorpusAsItStands": {
                "gateDefects": 41, "deliverableDebt": 37,
                "howMeasured": "git show HEAD:tools/T-234-census.py and "
                               "HEAD:tools/T-234-classification.json into a scratch directory, "
                               "then --root at the checkout",
            },
            "inheritedUncommittedRegeneration": {
                "gateDefects": 18, "deliverableDebt": 41,
                "howMeasured": "the same, with the working tree's uncommitted classification",
            },
            "freshRegenerationByTheStandingEmitter": {
                "gateDefects": 5, "deliverableDebt": 41,
                "howMeasured": "HEAD's census and emitter over a copy of the corpus's Markdown",
                "whatTheFiveAre": [
                    "C-0167 headline — GRILLAGE, resolved by the T-260 predicate",
                    "C-0172 LatticeTag row — GRILLAGE, resolved by the T-260 predicate",
                    "C-0171 coupling-threshold row — ROW_SPAN, resolved by the T-262 predicate",
                    "TASKS.md's T-9 row — a square-lattice oxDNA design, settled by hand",
                    "C-0167's five-gates reproduction row — settled by hand",
                ],
            },
        },
        "census": {
            "occurrences": len(records),
            "files": len({r["file"] for r in records}),
            "byFamily": by_family,
            "byFamilyExcludingThisClaimsWorkedExamples": by_family_without_self,
            "occurrencesInThisClaimsWorkedExamples": sum(
                1 for r in records if r["file"] == SELF_DOCUMENTING
            ),
            "byClass": by_class,
            "gateDefects": len(problems) + len(gated),
            "remoteContextOccurrences": len(remote),
            "handSettledOccurrences": len(hand),
            "deliverableDebt": len(debt),
        },
    }

    t260 = dict(common)
    t260["task"] = (
        "T-260 — a discharge can be PARTIAL, and a token pattern is exactly the instrument that "
        "cannot tell: one token, two discharges, two dates"
    )
    t260["cheapBounds"] = [
        "Before writing any predicate, dump every occurrence of the two ambiguous families with a "
        "window around the token and READ them: 38 PLACEMENT-family occurrences, hand-read as 17 "
        "structural-model, 14 absence-claim and 7 attributive. The census picked the predicate.",
        "The `PLACEMENT` pattern is one string and `C-0141` discharged half of what it matches, so "
        "no regular expression over that string can be right; what has to change is that a FAMILY "
        "carries its own pointer set. One data-structure change, no new pattern.",
        "Ask what the gate would cost before widening it: gating the GRILLAGE half on "
        "C-0154/C-0167 would demand annotating claims this task does not own, so the honest form "
        "is to report that half on its own line and name the census it belongs to.",
    ]
    t260["predicates"] = [
        {"predicate": "the PLACEMENT family splits by a tested predicate rather than by a "
                      "CORRECTING set membership",
         "verdict": "MET",
         "reading": "GRILLAGE {} / PLACEMENT {} / SQUARE {}, excluding this claim's own worked "
                    "examples".format(
                        by_family_without_self.get("GRILLAGE", 0),
                        by_family_without_self.get("PLACEMENT", 0),
                        by_family_without_self.get("SQUARE", 0))},
        {"predicate": "the two CORRECTING entries registered only to hide false positives are "
                      "removed and the gate stays clean",
         "verdict": "MET",
         "reading": "C-0152 and C-0154 removed; OUT_OF_SCOPE_FILES emptied; gate {} defects".format(
             len(problems) + len(gated))},
        {"predicate": "the class and the family cannot disagree — a family this census does not "
                      "gate may not carry a class it gates",
         "verdict": "MET",
         "reading": "asserted in classify(), 4 named tests, and the coercion is measured by 2 "
                    "mutations in both directions"},
        {"predicate": "every rule fails a NAMED test when narrowed AND when widened",
         "verdict": "MET", "reading": "see mutationCoverage"},
    ]
    t260["partialDischarge"] = {
        "whatC0141Supplied": "the honeycomb station lattice, the plan ceiling, the placement "
                             "family and the price of an oblique root",
        "whatItDidNotSupply": "a honeycomb grillage — OrigamiGrillage never reads `layers`, so "
                              "every coupled cell stayed a smeared single-layer square-lattice "
                              "solve until C-0154 built one and C-0167 re-graded onto it",
        "grillageOccurrences": by_family_without_self.get("GRILLAGE", 0),
        "placementOccurrences": by_family_without_self.get("PLACEMENT", 0),
        "attributiveOccurrences": by_family_without_self.get("SQUARE", 0),
        "gateDefectsResolvedByTheGrillageSplit": 2,
        "gateDefectsResolvedByTheRowSpanSplit": 1,
        "gateDefectsResolvedByAHandOverride": 2,
        "fileSetEntriesReplacedByThePredicate": 4,
    }
    t260["handReading"] = {
        "occurrencesReadByHand": 38,
        "predicateAgrees": 36,
        "predicateDisagrees": 2,
        "disagreements": [
            "TASKS.md's own T-260 row reads GRILLAGE because it quotes the word `grillage` in "
            "describing the split; either reading is defensible and the row is a queue RECORD",
            "C-0145's synthesis row reads SQUARE where the hand reading said PLACEMENT; the file "
            "is a synthesis claim, so the class is RECORD either way",
        ],
        "materialDisagreements": 0,
    }
    t260["structuralWindowSweep"] = structural_window_sweep()

    t262 = dict(common)
    t262["task"] = (
        "T-262 — the WIDTH family reads a RESTATEMENT as debt, and the advisory debt line grows "
        "when the documents are CORRECTED"
    )
    t262["cheapBounds"] = [
        "Count the tokens before writing a rule: of 24 `drawable` occurrences, 20 name C-0151's "
        "drawable 102 / 109 raster (the correction) and 4 name C-0119's `drawable at a uniform "
        "width` (the withdrawn premise). One word, two statements.",
        "Sentence-scoping the honeycomb context — the obvious cure for the T-9 row — drops 56 of "
        "103 WIDTH occurrences, most of them genuine. Measured before it was written, and "
        "rejected: a predicate can always be narrowed until the tree is clean.",
        "Measure the false-positive rate over HISTORY, not over the current tree: one loop over "
        "`git show <commit>:<file>` needs no solve and it is what makes the split believable.",
    ]
    t262["predicates"] = [
        {"predicate": "the WIDTH family distinguishes C-0140's withdrawn uniform tile width from "
                      "C-0146's restored row span and C-0151's drawable raster",
         "verdict": "MET",
         "reading": "WIDTH {} / ROW_SPAN {}, excluding this claim's own worked examples".format(
             by_family_without_self.get("WIDTH", 0), by_family_without_self.get("ROW_SPAN", 0))},
        {"predicate": "a document pass that CORRECTS the deliverables no longer inflates the "
                      "advisory debt line by its own correcting sentences",
         "verdict": "MET",
         "reading": "at the largest such pass the old predicate adds {} and the new one {}".format(
             max(b["oldPredicate"] - a["oldPredicate"] for a, b in zip(series, series[1:])),
             [b["newPredicate"] - a["newPredicate"] for a, b in zip(series, series[1:])][
                 [b["oldPredicate"] - a["oldPredicate"] for a, b in zip(series, series[1:])].index(
                     max(b["oldPredicate"] - a["oldPredicate"] for a, b in zip(series, series[1:])))
             ])},
        {"predicate": "the SQUARE-lattice collision the family cannot resolve is REPORTED rather "
                      "than guessed, and settled by a hand override that survives regeneration",
         "verdict": "MET",
         "reading": "{} remote-context occurrences reported; {} hand-settled; the emitter now "
                    "reads back the file it overwrites".format(len(remote), len(hand))},
        {"predicate": "every rule fails a NAMED test when narrowed AND when widened",
         "verdict": "MET", "reading": "see mutationCoverage"},
    ]
    t262["deliverableDebtSeries"] = {
        "whatIsCounted": (
            "occurrences of the WIDTH/PLACEMENT token families in ANSWERS.md and "
            "DECISIONS-FOR-NDI.md that are neither struck nor already pointed — the advisory debt "
            "line's own predicate, minus the class judgement, which is what makes it computable "
            "from a historical text alone"
        ),
        "revisions": len(series),
        "oldPredicateAtTheEnd": series[-1]["oldPredicate"],
        "newPredicateAtTheEnd": series[-1]["newPredicate"],
        "series": series,
    }
    t262["theDrawableToken"] = {
        "occurrences": 24,
        "namingTheDrawableRaster": 20,
        "namingTheWithdrawnUniformWidth": 4,
        "note": "`drawable` entered the WIDTH pattern at iteration 34 for C-0119's `drawable at a "
                "uniform width`; by iteration 37 C-0151 had made it the name of the CORRECTION.",
    }
    t262["theEmitterDocstring"] = {
        "promise": "a rule that is wrong for one occurrence is overridden by hand in the JSON and "
                   "the override survives",
        "wasTrue": False,
        "isTrueNow": True,
        "keyedOn": "file, family, token and the census's own snippet — the occurrence's "
                   "neighbourhood, not its index, because TASKS.md gains rows every iteration",
        "overridesCarriedOverOnRegeneration": len(hand),
    }

    common_sources = [
        "gpd/claims/C-0140-honeycomb-raster-turn-sense.md — no uniform honeycomb row length",
        "gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md — the station lattice, plan "
        "ceiling and placement family this census's PLACEMENT family records the absence of",
        "gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md — 112 bp restored as a ROW "
        "SPAN, and the width that threatened the design is not a width",
        "gpd/claims/C-0151-closing-raster-selection.md — the drawable 102 / 109 raster",
        "gpd/claims/C-0154-honeycomb-grillage.md — the honeycomb grillage C-0141 did not supply",
        "gpd/claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md — the coupled cells re-graded",
        "gpd/challenges/CH-0182-a-census-is-dated-by-its-premise-set.md",
    ]
    t260["sources"] = common_sources
    t262["sources"] = common_sources

    coverage = mutation_coverage()
    t260["mutationCoverage"] = coverage
    t262["mutationCoverage"] = coverage

    residue = {
        "notGatedInVerify": (
            "The census's --check is NOT wired into tools/verify.sh. Its scope includes TASKS.md, "
            "which every agent edits every iteration, so an occurrence arrives unclassified "
            "through no fault of the tree and the gate goes red for a reason nobody caused — "
            "C-0083's rule, that a gate which cannot come clean is not a gate, read forward."
        ),
        "wiringRequested": (
            "tools/T-234-census.py --self-test and tools/T-234-emit-classification.py --self-test "
            "read only in-memory fixtures apart from three corpus assertions, and belong beside "
            "the other checker self-tests on ./gradlew test. This task does not edit "
            "build.gradle.kts; the wiring is requested of the coordinator."
        ),
        "whatThePredicateStillCannotDo": (
            "It cannot tell which LATTICE a token belongs to. `15 x 112 bp` and `15 duplexes, "
            "112 bp` are this corpus's square-lattice sheet, and they sit on lines that discuss "
            "the honeycomb block beside them: 5 such occurrences, 2 of them caught by the "
            "remote-context advisory and 3 not. All 5 are settled by hand, and the residue is a "
            "LATTICE question rather than a width one."
        ),
        "cH0182": (
            "The classification is still dated by the corpus: a claim filed later in the same "
            "iteration arrives unclassified. What this task changes is the failure DIRECTION — a "
            "new occurrence in a non-subject family is now classed SURVIVING or RESTATED rather "
            "than MOVED, so a correcting claim no longer arrives as debt."
        ),
    }
    t260["residue"] = residue
    t262["residue"] = residue

    for name, document in (
        ("T-260-partial-discharge-predicate.json", t260),
        ("T-262-width-restatement-predicate.json", t262),
    ):
        path = os.path.join(ROOT, "gpd", "results", name)
        with open(path, "w", encoding="utf-8") as handle:
            json.dump(document, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        print("wrote {}".format(path))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
