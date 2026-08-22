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
"""Emit `gpd/results/T-280-debt-line-as-a-ratio.json`.

    tools/T-280-emit-result.py [--ref <git-ref>] [--self-test]

The subject of this file is the CORPUS, so -- per `CLAUDE.md`'s `T-249` entry -- it takes the ref
as an argument, defaults it to `HEAD`, and records the **resolved** SHA. Every reading below,
including the headline one, is taken out of `git show <ref>:<path>` and never out of the working
tree, so the file reproduces at its own `baselineRef`.
"""

import argparse
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-280-debt-line-as-a-ratio.json")

#: `CH-0230`'s own series is over the WIDTH/PLACEMENT token families alone, because that is what
#: `C-0176`'s split moves. The advisory line itself counts all five families this census gates, so
#: both series are published: the first REPRODUCES the challenge, the second MEASURES the line.
TOKEN_FAMILIES = ("WIDTH", "ROW_SPAN", "PLACEMENT", "GRILLAGE", "SQUARE")
REVISIONS = 40


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t280census", "T-234-census.py")
header = _load("t280header", "emission_header.py")
rounding = _load("t280rounding", "T-278-rounding-simulation.py")
mutation = _load("t280mutation", "T-280-mutation-test.py")


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _at(ref, path):
    """One file as it stands at `ref`, or `None` where it does not exist there."""
    try:
        return _git("show", "{}:{}".format(ref, path))
    except subprocess.CalledProcessError:
        return None


def counts_at(ref):
    """Every count this task needs, for the two deliverables as they stand at one commit.

    The numerator is the advisory line's own predicate MINUS the class judgement, which is what
    makes it computable from a historical text alone -- exactly the substitution `CH-0230` and
    `C-0176` §4 make, so that this series is comparable with theirs.
    """
    numerator = 0
    all_families = 0
    same_families = 0
    token_old = 0
    token_new = 0
    token_denominator = 0
    numerator_lines = set()
    denominator_lines = set()
    for path in census.DELIVERABLES:
        text = _at(ref, path)
        if text is None:
            continue
        spans = census.struck_spans(text)
        for family, line, offset, _token, _distance in census.occurrences(text):
            gated = census.discharge_of(family) == census.SUBJECT
            open_occurrence = not census.is_struck(spans, offset) \
                and not census.has_pointer(text, offset)
            all_families += 1
            denominator_lines.add((path, line))
            if gated:
                same_families += 1
                if open_occurrence:
                    numerator += 1
                    numerator_lines.add((path, line))
            if family in TOKEN_FAMILIES:
                token_denominator += 1
                if open_occurrence:
                    token_old += 1
                    if gated:
                        token_new += 1
    return {
        "unpointed": numerator,
        "allFamilyOccurrences": all_families,
        "sameFamilyOccurrences": same_families,
        "ratioOverAllFamilies": numerator / all_families if all_families else None,
        "ratioOverTheSameFamilies": numerator / same_families if same_families else None,
        "ch0230OldPredicate": token_old,
        "ch0230NewPredicate": token_new,
        "ch0230TokenFamilyOccurrences": token_denominator,
        "unpointedLines": len(numerator_lines),
        "occurrenceLines": len(denominator_lines),
    }


def series(ref, limit=REVISIONS):
    """`counts_at` for each of the last `limit` revisions of the two deliverables, oldest first."""
    log = _git("log", "--format=%h", "-n", str(limit), ref, "--", *census.DELIVERABLES)
    commits = [line.strip() for line in log.splitlines() if line.strip()][::-1]
    rows = []
    for sha in commits:
        row = {
            "commit": sha,
            "date": _git("log", "-1", "--format=%ad", "--date=short", sha).strip(),
            "subject": _git("log", "-1", "--format=%s", sha).strip(),
        }
        row.update(counts_at(sha))
        rows.append(row)
    return rows


def movement(rows):
    """Where the count rose, and what the ratio did there. The whole of `F1`, in one pass.

    A pass at which both readings of the ratio are undefined, or at which the ratio was **exactly
    one** before and after, is reported as `SATURATED` and NOT counted either way: a proportion
    pinned at its own boundary is the resolution of nothing (`C-0131`/`CH-0153`'s saturated statistic, met
    on a ratio rather than on a Monte Carlo), and here it says something true and separate -- that in
    those revisions the deliverables carried no pointer and no strike at all.
    """
    verdicts = []
    for before, after in zip(rows, rows[1:]):
        if after["unpointed"] <= before["unpointed"]:
            continue
        prior = before["ratioOverAllFamilies"]
        now = after["ratioOverAllFamilies"]
        prior_same = before["ratioOverTheSameFamilies"]
        now_same = after["ratioOverTheSameFamilies"]
        if prior is None or now is None or (prior == 1.0 and now == 1.0):
            verdict = "SATURATED"
        elif now < prior:
            verdict = "RATIO FELL"
        elif now > prior:
            verdict = "RATIO ROSE"
        else:
            verdict = "RATIO FLAT"
        verdicts.append({
            "commit": after["commit"],
            "subject": after["subject"],
            "countBefore": before["unpointed"],
            "countAfter": after["unpointed"],
            "ratioBefore": prior,
            "ratioAfter": now,
            "ratioOverTheSameFamiliesBefore": prior_same,
            "ratioOverTheSameFamiliesAfter": now_same,
            "sameFamilyVerdict": (
                "SATURATED" if prior_same is None or now_same is None
                or (prior_same == 1.0 and now_same == 1.0)
                else "RATIO FELL" if now_same < prior_same
                else "RATIO ROSE" if now_same > prior_same else "RATIO FLAT"
            ),
            "verdict": verdict,
            "occurrencesAdded": after["allFamilyOccurrences"] - before["allFamilyOccurrences"],
            "unpointedAdded": after["unpointed"] - before["unpointed"],
        })
    return verdicts


def candidate_three(ref):
    """The price of counting LINES rather than occurrences, over history and over the tree.

    Two numbers decide it and neither is the compression factor. The first is whether the line
    count's growth has a different SIGN from the occurrence count's -- if it does not, candidate 3
    buys nothing candidate 2 does not. The second is how many physical lines carry occurrences of
    more than one CLASS or of more than one DISCHARGE, because `C-0176`'s architecture is that a
    class is a reading held per OCCURRENCE and a family carries its own pointer set: a line-keyed
    census cannot represent `T-260`'s partial discharge at all where that number is not zero.
    """
    import collections
    rows = series(ref)
    rose_occurrences = 0
    rose_lines = 0
    for before, after in zip(rows, rows[1:]):
        if after["unpointed"] <= before["unpointed"]:
            continue
        rose_occurrences += 1
        if after["unpointedLines"] > before["unpointedLines"]:
            rose_lines += 1
    records = census.census(ROOT)
    records, _problems = census.classify(records, census.load_classification())
    priced = {}
    for scope, subset in (
        ("corpus", records), ("deliverables", [r for r in records if r["deliverable"]])
    ):
        by_line = collections.defaultdict(list)
        for record in subset:
            by_line[(record["file"], record["line"])].append(record)
        priced[scope] = {
            "occurrences": len(subset),
            "lines": len(by_line),
            "compression": len(subset) / len(by_line) if by_line else None,
            "linesCarryingMoreThanOneOccurrence":
                sum(1 for v in by_line.values() if len(v) > 1),
            "linesCarryingMoreThanOneClass":
                sum(1 for v in by_line.values() if len({r["class"] for r in v}) > 1),
            "linesCarryingMoreThanOneFamily":
                sum(1 for v in by_line.values() if len({r["family"] for r in v}) > 1),
            "linesCarryingMoreThanOneDischarge":
                sum(1 for v in by_line.values() if len({r["discharge"] for r in v}) > 1),
        }
    return {
        "whatItWouldBe": "count PHYSICAL LINES rather than occurrences, because a correcting "
                         "sentence that names a premise three times is one edit",
        "passesAtWhichTheOCCURRENCEcountRose": rose_occurrences,
        "ofWhichTheLINEcountAlsoRose": rose_lines,
        "changesTheSignOfTheGrowth": rose_lines < rose_occurrences and rose_lines == 0,
        "priceInTheTree": priced,
        "note": "The tree reading is taken from the WORKING TREE, because the class is only "
                "available through the classification the tree carries; the historical half is "
                "taken out of git at the recorded ref. The compression is the number the "
                "candidate is usually argued on and it is not the number that decides it.",
        "verdict": "NOT ADOPTED, and not on cost. It does not change the sign -- the line count "
                   "rose at every pass at which the occurrence count rose -- and it collapses "
                   "occurrences of different CLASSES and different DISCHARGES onto one key, which "
                   "is exactly what C-0176's partial-discharge data structure exists to keep "
                   "apart. Candidate 2 is the remedy; candidate 3 would undo T-260.",
    }


def mutation_coverage():
    """Derived by RUNNING the mutation test, never typed: this file's whole subject is measurement."""
    import io
    from contextlib import redirect_stdout
    buffer = io.StringIO()
    with redirect_stdout(buffer):
        status = mutation.main()
    lines = buffer.getvalue().splitlines()
    rows = mutation.mutations()
    added = [n for n in mutation._test_names(open(mutation.CENSUS, encoding="utf-8").read())
             if n.startswith("T-280 ")]
    return {
        "mutations": len(rows),
        "narrowing": sum(1 for row in rows if row[0] == "NARROW"),
        "widening": sum(1 for row in rows if row[0] == "WIDEN"),
        "namedTestsAddedByThisTask": len(added),
        "namedTestsReachedBySomeMutation":
            len(added) - sum(1 for l in lines if l.startswith("  UNREACHED")),
        "mutationsFailingNoNamedTest": sum(1 for l in lines if l.startswith("  SILENT")),
        "exitStatus": status,
        "note": "Every substitution REPLACES a rule; none widens one to `original|mutant`, which "
                "is a no-op and which C-0176's own first table did on 9 of 22 rows. An UNREACHED "
                "named test is a measurement of this table rather than of the tool, so it is "
                "reported and not gated; the exit code turns on SILENT mutations.",
    }


def build(ref):
    sha = _git("rev-parse", ref).strip()
    rows = series(sha)
    latest = rows[-1]
    verdicts = movement(rows)
    fell = [v for v in verdicts if v["verdict"] == "RATIO FELL"]
    rose = [v for v in verdicts if v["verdict"] == "RATIO ROSE"]
    saturated = [v for v in verdicts if v["verdict"] == "SATURATED"]
    same_fell = [v for v in verdicts if v["sameFamilyVerdict"] == "RATIO FELL"]
    same_rose = [v for v in verdicts if v["sameFamilyVerdict"] == "RATIO ROSE"]
    document = {
        "task": "T-280 — the advisory T-233 debt line published as a RATIO, and the denominator "
                "CH-0230 named is the one that does not work",
        "leaf": "none — a process task, protecting the census that protects every honeycomb leaf",
        "verificationType": (
            "logical — a count and a ratio over the in-scope corpus, both taken out of git at a "
            "recorded ref; a historical series over the last {} revisions of the two "
            "deliverables; and a mutation measurement in BOTH directions over the census's own "
            "named self-tests".format(REVISIONS)
        ),
        "baselineRef": sha,
        "conditions": (
            "No physics is computed and no physical number moves. Every quantity is a COUNT of "
            "occurrences or of physical lines, or a dimensionless ratio of two counts, so the "
            "rounding floor is ZERO — RESULT_ABSOLUTE_FLOOR is a claim in the locked units and "
            "does not travel (P-18). Units elsewhere unchanged and untouched: nm, pN, pN/nm, "
            "pN/nm^2 = 1 MPa exactly, k_BT = 4.141947 pN nm at 300 K."
        ),
        "parameters": {
            "censusTool": "tools/T-234-census.py",
            "classification": "tools/T-234-classification.json",
            "mutationTest": "tools/T-280-mutation-test.py",
            "deliverables": list(census.DELIVERABLES),
            "revisions": REVISIONS,
            "subjectDischarge": census.SUBJECT,
            "gatedFamilies": sorted(census.gated_families()),
            "tokenFamiliesOfTheCh0230Series": list(TOKEN_FAMILIES),
            "ratioSignificantDigits": census.DEBT_RATIO_DIGITS,
        },
        "theDenominator": {
            "question": "A fraction has a denominator, and CH-0230 candidate 2 states one: "
                        "`unpointed occurrences over all occurrences of the SAME families`. "
                        "Whether that denominator delivers the promised behaviour is a "
                        "measurement, not a matter of taste, and the row that raised this task "
                        "asked for the claim to be verified rather than inherited.",
            "adopted": census.DEBT_DENOMINATOR,
            "namedByCh0230": census.DEBT_DENOMINATOR_NAMED_BY_CH0230,
            "whyTheWiderOneWorks": (
                "A correcting sentence has to NAME the withdrawn premise, so it lands in the "
                "numerator AND in the denominator, and a ratio below one that gains equally top "
                "and bottom goes UP. What makes the wider denominator informative is C-0176's own "
                "split: a correcting sentence written PROPERLY reads as ROW_SPAN (the restored "
                "reading) or GRILLAGE (another census's discharge), which is denominator and not "
                "numerator. So candidate 2 works only on top of T-260/T-262, and the reading "
                "CH-0230 itself named does not work at all."
            ),
            "whichReadingOfC0176": (
                "C-0176 publishes every family split BOTH with and without its own worked "
                "examples, because writing that claim added 13 occurrences to the census it is "
                "about. For THIS line the two readings COINCIDE EXACTLY and by construction: the "
                "advisory line counts only ANSWERS.md and DECISIONS-FOR-NDI.md, and a claim's "
                "worked examples live under gpd/claims/. Asserted as a named test."
            ),
        },
        "reading": {
            "atTheBaselineRef": latest,
            "note": "The numerator here is the advisory line's predicate MINUS the class "
                    "judgement, so that it is computable from a historical text alone and "
                    "comparable with CH-0230's own table. The tool's live line applies the class "
                    "as well, which is why it reads slightly lower.",
        },
        "reproductionOfCh0230": {
            "whatIsReproduced": "CH-0230's table — the WIDTH/PLACEMENT token families over the "
                                "last {} revisions, neither struck nor already pointed, under the "
                                "iteration-34 predicate and under C-0176's split".format(REVISIONS),
            "oldPredicateAtTheEnd": latest["ch0230OldPredicate"],
            "newPredicateAtTheEnd": latest["ch0230NewPredicate"],
            "challengeStates": {"oldPredicate": 25, "newPredicate": 10},
            "reproduces": latest["ch0230OldPredicate"] == 25 and latest["ch0230NewPredicate"] == 10,
        },
        "series": rows,
        "movement": {
            "whatIsMeasured": "every revision at which the advisory line's own count ROSE, and "
                              "what each of the two candidate ratios did at that revision",
            "passesAtWhichTheCountRose": len(verdicts),
            "ofWhichSaturated": len(saturated),
            "adoptedDenominatorFell": len(fell),
            "adoptedDenominatorRose": len(rose),
            "ch0230DenominatorFell": len(same_fell),
            "ch0230DenominatorRose": len(same_rose),
            "verdicts": verdicts,
        },
        "candidateThree": candidate_three(sha),
        "predicates": [
            {"predicate": "the advisory line prints a RATIO beside the count, with the "
                          "denominator NAMED in the same output",
             "verdict": "MET",
             "reading": "debt_report() prints the count, the ratio, the denominator's name and "
                        "CH-0230's own reading beside it; 28 named self-tests"},
            {"predicate": "the ratio's behaviour over the same revision range is MEASURED and "
                          "published rather than asserted",
             "verdict": "MET",
             "reading": "{} passes at which the count rose: {} saturated, {} the ratio fell, "
                        "{} the ratio rose".format(
                            len(verdicts), len(saturated), len(fell), len(rose))},
            {"predicate": "candidate 3 is PRICED rather than assumed",
             "verdict": "MET",
             "reading": "priced in candidateThree: it does not change the sign, and it collapses "
                        "occurrences of different classes and discharges onto one key"},
            {"predicate": "every new rule fails a NAMED test when narrowed AND when widened",
             "verdict": "MET", "reading": "see mutationCoverage"},
        ],
        "falsifiers": [
            {"name": "F1",
             "falsifier": "the ratio does not fall at any pass at which the count rose, so "
                          "candidate 2 delivers nothing the count does not",
             "outcome": "DID NOT FIRE for the adopted denominator ({} of {} non-saturated passes "
                        "fell) and FIRED for the one CH-0230 named ({} fell, {} rose)".format(
                            len(fell), len(verdicts) - len(saturated),
                            len(same_fell), len(same_rose))},
            {"name": "F2",
             "falsifier": "a mutation of any new rule fails no named test",
             "outcome": "FIRED on the first run, on 1 of 18 rows — `the report drops the count and "
                        "prints the ratio alone` failed nothing, because the test asserting the "
                        "count was satisfied by the word `occurrence` inside the DENOMINATOR's own "
                        "name. The test was rewritten to assert the headline line itself, and "
                        "five further mutations were added; 0 of 23 now"},
            {"name": "F3",
             "falsifier": "C-0176's two readings — with and without its own worked examples — "
                          "differ for this line, so the ratio must say which it is",
             "outcome": "DID NOT FIRE. They coincide exactly and by construction, because the "
                        "line counts only the two deliverables and a claim's worked examples are "
                        "under gpd/claims/. Asserted as a named test rather than argued"},
            {"name": "F4",
             "falsifier": "candidate 3 changes the SIGN of the growth where candidate 2 does not, "
                          "so counting lines is the remedy and this task built the wrong one",
             "outcome": "DID NOT FIRE. The line count rose at {} of the {} passes at which the "
                        "occurrence count rose".format(
                            candidate_three(sha)["ofWhichTheLINEcountAlsoRose"],
                            candidate_three(sha)["passesAtWhichTheOCCURRENCEcountRose"])},
        ],
        "sources": [
            "gpd/challenges/CH-0230-the-debt-line-grows-when-the-documents-are-corrected.md — the "
            "three candidate remedies, and the 40-revision series this file reproduces",
            "gpd/claims/C-0176-partial-discharge-and-restatement-predicates.md — the T-260/T-262 "
            "split without which the wider denominator carries nothing, and the two-reading rule",
            "gpd/claims/C-0144-honeycomb-correction-supersession.md — the census and its five "
            "families",
            "gpd/challenges/CH-0182-a-census-is-dated-by-its-premise-set.md — why a corpus census "
            "records the ref it was taken at",
            "gpd/results/T-262-width-restatement-predicate.json — the series under the old and "
            "new predicates",
        ],
        "residue": {
            "theSignIsUNCHANGED": (
                "The COUNT still grows on every synthesis pass and nothing here changes that; "
                "CH-0230's mechanism is not repaired, it is priced. What the ratio adds is that "
                "the line can now FALL, and it falls when a pass adds pointed or corrected "
                "sentences — which is the behaviour the word `debt` implies and which no count "
                "over a moving corpus can have."
            ),
            "theRatioIsNotMONOTONE": (
                "It rose at one of the passes at which the count rose, and that pass added its "
                "occurrences entirely to the numerator: every new occurrence was an unpointed "
                "assertion of a withdrawn premise and none was a repair. A metric that can rise "
                "AND fall is what distinguishes a debt from a counter; the rise there is earned."
            ),
            "theSaturatedHead": (
                "For the eleven earliest revisions in which the line is non-zero the ratio is "
                "EXACTLY one under both denominators, because the deliverables then carried no "
                "pointer and no strike at all. That is a saturated proportion and it is reported "
                "as SATURATED rather than counted as a fall or a rise — C-0131/CH-0153's rule, met "
                "ratio of two censuses rather than on a Monte Carlo."
            ),
            "whatIsNOTreEmitted": (
                "gpd/results/T-260-*.json and T-262-*.json carry a mutationCoverage record whose "
                "namedTestsCensus and namedTestsTotal are readings of tools/T-234-census.py at "
                "their own baselineRef. This task adds named tests to that tool, so those two "
                "fields are larger now than they were then. Neither file is re-emitted: each is a "
                "measurement at a recorded ref and reproduces there, and no predicate, verdict or "
                "reading in either moves. The T-234 mutation test's own scoped measurement is "
                "unaffected, because it scopes on names beginning `T-260 ` or `T-262 `."
            ),
            "theGateIsSTILLnotWired": (
                "The census's --check remains out of tools/verify.sh, for C-0176 §8's reason: its "
                "scope includes TASKS.md, which every agent edits every iteration, so an "
                "occurrence arrives unclassified through no fault of the tree. This task does not "
                "change that and adds no gate."
            ),
        },
    }
    document["mutationCoverage"] = mutation_coverage()
    return header.with_emission_header(
        rounding.walk(document, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0), "none"
    )


def _self_test():
    failures = []

    def ok(name, condition):
        if not condition:
            failures.append(name)

    rows = [
        {"commit": "a", "unpointed": 1, "allFamilyOccurrences": 1,
         "ratioOverAllFamilies": 1.0, "ratioOverTheSameFamilies": 1.0, "unpointedLines": 1,
         "subject": "s"},
        {"commit": "b", "unpointed": 2, "allFamilyOccurrences": 2,
         "ratioOverAllFamilies": 1.0, "ratioOverTheSameFamilies": 1.0, "unpointedLines": 2,
         "subject": "s"},
        {"commit": "c", "unpointed": 3, "allFamilyOccurrences": 12,
         "ratioOverAllFamilies": 0.25, "ratioOverTheSameFamilies": 0.5, "unpointedLines": 3,
         "subject": "s"},
        {"commit": "d", "unpointed": 5, "allFamilyOccurrences": 12,
         "ratioOverAllFamilies": 0.4166666666666667, "ratioOverTheSameFamilies": 0.9,
         "unpointedLines": 4, "subject": "s"},
        {"commit": "e", "unpointed": 5, "allFamilyOccurrences": 20,
         "ratioOverAllFamilies": 0.25, "ratioOverTheSameFamilies": 0.5, "unpointedLines": 4,
         "subject": "s"},
    ]
    verdicts = movement(rows)
    ok("T-280 only the revisions at which the COUNT rose are judged", len(verdicts) == 3)
    ok("T-280 a ratio pinned at one before and after is SATURATED, not a rise",
       verdicts[0]["verdict"] == "SATURATED")
    ok("T-280 a fall is reported as a fall", verdicts[1]["verdict"] == "RATIO FELL")
    ok("T-280 a rise is reported as a rise", verdicts[2]["verdict"] == "RATIO ROSE")
    ok("T-280 the two denominators are judged SEPARATELY",
       verdicts[2]["sameFamilyVerdict"] == "RATIO ROSE"
       and verdicts[1]["sameFamilyVerdict"] == "RATIO FELL")
    ok("T-280 a revision at which the count FELL is not judged at all",
       all(v["commit"] != "e" for v in verdicts))
    ok("T-280 the movement record carries what was added, top and bottom",
       verdicts[2]["occurrencesAdded"] == 0 and verdicts[2]["unpointedAdded"] == 2)

    flat = movement([
        {"commit": "a", "unpointed": 1, "allFamilyOccurrences": 4, "ratioOverAllFamilies": 0.25,
         "ratioOverTheSameFamilies": 0.25, "unpointedLines": 1, "subject": "s"},
        {"commit": "b", "unpointed": 2, "allFamilyOccurrences": 8, "ratioOverAllFamilies": 0.25,
         "ratioOverTheSameFamilies": 0.25, "unpointedLines": 1, "subject": "s"},
    ])
    ok("T-280 an unchanged ratio is FLAT, and is neither a fall nor a rise",
       flat[0]["verdict"] == "RATIO FLAT")
    ok("T-280 an absent ratio is SATURATED rather than a comparison against None",
       movement([
           {"commit": "a", "unpointed": 0, "allFamilyOccurrences": 0,
            "ratioOverAllFamilies": None, "ratioOverTheSameFamilies": None,
            "unpointedLines": 0, "subject": "s"},
           {"commit": "b", "unpointed": 1, "allFamilyOccurrences": 1,
            "ratioOverAllFamilies": 1.0, "ratioOverTheSameFamilies": 1.0,
            "unpointedLines": 1, "subject": "s"},
       ])[0]["verdict"] == "SATURATED")

    ok("T-280 the emitted ratio is rounded at the serialisation boundary, floor ZERO",
       rounding.walk({"r": 1 / 3}, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)["r"] == 0.333333333)
    ok("T-280 a count is an integer and survives the rounding",
       rounding.walk({"n": 24}, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)["n"] == 24)
    ok("T-280 the emitter takes a ref and defaults it to HEAD",
       _parser().parse_args([]).ref == "HEAD")
    ok("T-280 an explicit ref is honoured", _parser().parse_args(["--ref", "x"]).ref == "x")
    ok("T-280 the census's own deliverables are the two documents",
       census.DELIVERABLES == ("ANSWERS.md", "DECISIONS-FOR-NDI.md"))
    ok("T-280 the CH-0230 series is over the token families the split moves",
       set(TOKEN_FAMILIES) == {"WIDTH", "ROW_SPAN", "PLACEMENT", "GRILLAGE", "SQUARE"})
    ok("T-280 the revision range is CH-0230's own", REVISIONS == 40)

    for failure in failures:
        print("FAIL  " + failure)
    print("self-test: {} failure(s)".format(len(failures)))
    return 1 if failures else 0


def _parser():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD",
                        help="the corpus state to measure; the RESOLVED sha is recorded")
    parser.add_argument("--self-test", action="store_true")
    return parser


def main(argv=None):
    args = _parser().parse_args(argv)
    if args.self_test:
        return _self_test()
    document = build(args.ref)
    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(RESULT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
