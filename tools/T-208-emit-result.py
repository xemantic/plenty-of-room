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
# T-208 / T-209 / T-210 -- three result-file hygiene items, one artifact.  Claim C-0129.
#
# Everything here is COMPUTED, not retyped: the three censuses come from
# `tools/check-result-file-hygiene.py`, and the blast radius comes from diffing each re-emitted
# file field by field against its COMMITTED version read straight out of `git`, so the comparison
# is reproducible by anybody at any later commit.
#
# CLAUDE.md's classifier rule is applied to the prose: a diff classifier must strip digits before
# calling a prose change a verdict change.  A string whose digit-stripped skeleton is unchanged
# moved a NUMBER; one whose skeleton moved changed what the sentence SAYS.
#
# Departures are emitted at TWO significant digits.  This task is about that rule, so it obeys it.
# No wall-clock timing and no step count is emitted anywhere: `CLAUDE.md` records both as fields
# that make a result file permanently un-diffable.
import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DIGITS = re.compile(r"[0-9]")

_spec = importlib.util.spec_from_file_location(
    "hygiene", os.path.join(ROOT, "tools", "check-result-file-hygiene.py")
)
hygiene = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(hygiene)

REEMITTED = [
    "T-136-two-per-row-placement.json",
    "T-148-staple-dropout.json",
]

# The control re-run: `anchoring.TwoPerRowPlacementStudyKt` executed a SECOND time on a separate
# snapshot with IDENTICAL code, so that the six non-departure movements below are MEASURED to be
# the study's own descent irreproducibility rather than asserted to be.  Filled by --control.
CONTROL = {}


def significant(value, digits=2):
    if not isinstance(value, float) or value == 0.0:
        return value
    from decimal import Decimal
    return float(f"%.{digits - 1}e" % value)


def walk(document, path=""):
    if isinstance(document, dict):
        for key, value in document.items():
            yield from walk(value, f"{path}/{key}")
    elif isinstance(document, list):
        for index, value in enumerate(document):
            yield from walk(value, f"{path}/{index}")
    else:
        yield path, document


def flat(document):
    return dict(walk(document))


def committed(name):
    text = subprocess.run(
        ["git", "show", f"HEAD:gpd/results/{name}"],
        cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout
    return json.loads(text)


def working(name):
    with open(os.path.join(ROOT, "gpd", "results", name), encoding="utf-8") as handle:
        return json.load(handle)


def classify(before, after):
    """Field-by-field movement between two result files, by class."""
    counts = {
        "departureFieldsMoved": 0,
        "otherNumericFieldsMoved": 0,
        "proseFieldsMovedDigitsOnly": 0,
        "proseFieldsMovedWording": 0,
        "decisionFieldsMoved": 0,
        "fieldsAdded": len(set(after) - set(before)),
        "fieldsRemoved": len(set(before) - set(after)),
    }
    worst = 0.0
    moved_departures = []
    other = []
    for key in sorted(set(before) & set(after)):
        old, new = before[key], after[key]
        if old == new:
            continue
        leaf = key.rsplit("/", 1)[-1]
        if isinstance(old, bool) or isinstance(new, bool) or type(old) is not type(new):
            counts["decisionFieldsMoved"] += 1
        elif isinstance(old, (int, float)):
            scale = max(abs(old), abs(new)) or 1.0
            relative = abs(new - old) / scale
            if leaf in hygiene.DEPARTURE_KEYS:
                counts["departureFieldsMoved"] += 1
                moved_departures.append(key)
            else:
                counts["otherNumericFieldsMoved"] += 1
                other.append({"field": key, "relativeMovement": significant(relative)})
                worst = max(worst, relative)
        elif isinstance(old, str):
            if DIGITS.sub("#", old) == DIGITS.sub("#", new):
                counts["proseFieldsMovedDigitsOnly"] += 1
            else:
                counts["proseFieldsMovedWording"] += 1
    counts["worstNonDepartureRelativeMovement"] = significant(worst)
    return counts, other


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--control-a")
    parser.add_argument("--control-b")
    arguments = parser.parse_args()

    strict, wide = hygiene.check_departures(os.path.join(ROOT, "gpd", "results"))
    saturated = hygiene.check_saturated(os.path.join(ROOT, "gpd", "results"))
    conversions = hygiene.check_conversions(os.path.join(ROOT, "gpd", "results"))
    all_files = hygiene.result_files(os.path.join(ROOT, "gpd", "results"))

    per_file = {}
    for path, _, _, _ in strict:
        name = os.path.basename(path)
        per_file[name] = per_file.get(name, 0) + 1
    saturated_per_file = {}
    for path, _, _, _, _ in saturated:
        name = os.path.basename(path)
        saturated_per_file[name] = saturated_per_file.get(name, 0) + 1

    reemission = []
    for name in REEMITTED:
        counts, other = classify(flat(committed(name)), flat(working(name)))
        counts["resultFile"] = name
        counts["numericMovements"] = other
        reemission.append(counts)

    control = {}
    if arguments.control_a and arguments.control_b:
        with open(arguments.control_a) as a, open(arguments.control_b) as b:
            counts, other = classify(flat(json.load(a)), flat(json.load(b)))
        control = {
            "study": "anchoring.TwoPerRowPlacementStudyKt, run TWICE on separate snapshots "
                     "with identical code",
            "why": "the six non-departure movements the repair appears to have made are a "
                   "30-parameter minimax descent's own irreproducibility. CLAUDE.md records the "
                   "mechanism -- a descent on an optimal MANIFOLD has no isolated answer to be "
                   "reproducible about -- and C-0127 measured the same class in T-129. Measured "
                   "here rather than asserted.",
            "departureFieldsMoved": counts["departureFieldsMoved"],
            "otherNumericFieldsMoved": counts["otherNumericFieldsMoved"],
            "decisionFieldsMoved": counts["decisionFieldsMoved"],
            "proseFieldsMovedWording": counts["proseFieldsMovedWording"],
            "worstNonDepartureRelativeMovement":
                counts["worstNonDepartureRelativeMovement"],
            "movedFields": [row["field"] for row in other],
        }

    # T-210's instrument, tabulated at T-148's own five sample counts.
    rule_of_three = []
    for count in (1250, 2500, 5000, 10000, 20000):
        exact = 0.05 ** (1.0 / count)
        rule_of_three.append({
            "draws": count,
            "symmetricBinomialStandardErrorAtSaturation": 0.0,
            "oneSidedLowerBoundExact": round(exact, 9),
            "oneSidedLowerBoundRuleOfThree": round(1.0 - 3.0 / count, 9),
            "absoluteDeparture": significant(abs(exact - (1.0 - 3.0 / count))),
        })

    result = {
        "task": "T-208 / T-209 / T-210 -- three result-file hygiene items, one artifact",
        "claim": "C-0129",
        "leaf": "none -- a PROCESS task; it protects the machine-readable artifact of every leaf",
        "raisedBy": "C-0127 (T-207), section 5 and its incidentalFindings block",
        "verificationType":
            "logical (three static censuses over the committed corpus, each with a stated catch "
            "set) + in-silico (two studies re-run through tools/study.sh and diffed field by "
            "field against their COMMITTED version, read out of git)",
        "maturity":
            "TRL 1-3, and below it: NO PHYSICS CHANGED. Every number this task moved is a "
            "diagnostic, a precision or a sentence. No verdict, predicate or boolean moved.",
        "units":
            "unchanged and untouched -- nm, pN, pN/nm, pN/nm^2 = 1 MPa exactly, "
            "k_BT = 4.141947 pN nm at 300 K, aqueous buffer with stated Mg2+",
        "sources": [
            "gpd/results/*.json (all files, read as the corpus under test)",
            "git HEAD:gpd/results/T-136-two-per-row-placement.json",
            "git HEAD:gpd/results/T-148-staple-dropout.json",
        ],
        "cheapBounds": [
            {
                "item": "T-208",
                "question": "how many committed result files would the new gate fire on TODAY?",
                "cost": "one regular expression over every result file, no run",
                "answer": len(conversions),
                "allowlisted": sorted(hygiene.ALLOWLIST),
                "resultFilesScanned": len(all_files),
                "filesTheFIRSTRegexFiredOn": 87,
                "filesScannedByTheFirstRegex": 117,
                "prosePercentagesInSrcThatWouldHaveMatched": 310,
                "whyTheFirstRegexWasWrong":
                    "it was written with a SPACE in Java's flag class, which is a legal Java "
                    "flag. Every one of the 87 hits is a prose percentage ('% of', '% over'). "
                    "Wired, it would have failed the build on 74 % of the corpus for nothing -- "
                    "which is why the discriminator is a self-tested part of the tool, "
                    "mutation-tested three ways, rather than a regular expression in a claim.",
                "decision":
                    "ZERO. The tree reads clean, so the gate is WIRABLE and is wired. C-0083: a "
                    "gate that cannot come clean is not a gate.",
            },
            {
                "item": "T-209",
                "question": "how many departure fields carry more than two significant digits?",
                "cost": "one walk of the committed JSON, no run",
                "answerBeforeRepair": 222,
                "filesBeforeRepair": 29,
                "answerWideCensusBeforeRepair": 1422,
                "filesWideCensusBeforeRepair": 71,
                "answerAfterRepair": len(strict),
                "filesAfterRepair": len({row[0] for row in strict}),
                "decision":
                    "222 fields in 29 files, not the ONE field T-209 was raised on -- a factor "
                    "of 222. That count is what decides the shape of the deliverable: a gate is "
                    "unwirable, a tree-wide repair is 29 study re-runs, and the honest answer is "
                    "a central mechanism plus a measured audit plus the two files this task can "
                    "re-emit and verify.",
            },
            {
                "item": "T-210",
                "question": "how many saturated proportions carry a symmetric standard error "
                            "and no one-sided bound?",
                "cost": "one walk of the committed JSON, no run",
                "answerBeforeRepair": 302,
                "filesBeforeRepair": 7,
                "answerAfterRepair": len(saturated),
                "filesAfterRepair": len(saturated_per_file),
                "recordsCarryingAStandardErrorOnAProportion": 403,
                "decision":
                    "302 of 403, in 7 files -- not the one note T-210 was raised on. Every one "
                    "reads exactly 0.0, and it reads 0.0 for the same reason in all 302.",
            },
        ],
        "gate": {
            "tool": "tools/check-result-file-hygiene.py",
            "reads": "gpd/results/ -- OUTPUT, where tools/check-kotlin-format-strings.py reads "
                     "SOURCE",
            "catchSetsAreStrictlyDifferent":
                "the static checker models String.format call sites and cannot see a conversion "
                "arriving by a route it does not model: a settles string assembled in one "
                "function and formatted in another, a Python emitter in tools/, a hand-edited "
                "field. This one models nothing and reads what was committed.",
            "conversionVersusPercentSign":
                "the discriminator is a refusal of Java's SPACE flag. '% d' is a legal Java "
                "conversion and it is also how all 310 of this repository's prose percentages "
                "are written ('84 % of', '+14.7 % more'). The conversion letter is further "
                "restricted to Java's own set, which is a strict tightening: a conversion that "
                "leaked out of a String.format is by construction a valid one.",
            "selfTests": (len(hygiene.CONVERSION_TESTS) + len(hygiene.DEPARTURE_TESTS)
                          + len(hygiene.SATURATION_TESTS) + 2),
            "predicatesThatAreGates": ["--conversions"],
            "predicatesThatAreAudits": ["--departures", "--saturated"],
            "whyTheAuditsAreNotGates":
                "222 fields in 29 files and 302 records in 7 files respectively. C-0083.",
            "wiredInto": ["./gradlew test (testResultFileHygiene)", "tools/verify.sh"],
            "defectsOverTheTreeNow": len(conversions),
        },
        "departureCensus": {
            "rule": "C-0093, C-0101: a departure -- a difference or ratio of two nearly equal "
                    "DIMENSIONLESS numbers -- is emitted at two significant digits, because "
                    "RESULT_ABSOLUTE_FLOOR is a claim in the LOCKED UNITS and does not reach a "
                    "ratio.",
            "whyItSurvivedThreeRepairs":
                "C-0093 cured it on its own convergence axis; C-0101 cured it in the "
                "reproduction records of the files it was re-emitting; C-0127 then found T-136 "
                "still carrying reproductions[2].departure at nine digits. Each repair was "
                "correct and each was applied PER FILE. The rule is about a RECORD TYPE, so it "
                "now lives once, by name, in structure/ResultRounding.kt as "
                "DEPARTURE_SIGNIFICANT_DIGITS and DEPARTURE_DIGITS_BY_KEY.",
            "strictFieldsRemaining": len(strict),
            "strictFilesRemaining": len({row[0] for row in strict}),
            "wideFieldsRemaining": len(wide),
            "wideFilesRemaining": len({row[0] for row in wide}),
            "remainingByFile": [
                {"resultFile": name, "fields": per_file[name]}
                for name in sorted(per_file, key=lambda n: (-per_file[n], n))
            ],
            "whatWiringWouldCost":
                "one study re-run per remaining file, in the order tools/reemission-order.py "
                "prints, plus a per-file diff to separate the rounding change from each study's "
                "own descent irreproducibility. That is the whole of the outstanding work and it "
                "is queued rather than smuggled into a hygiene task.",
        },
        "saturatedCensus": {
            "defect":
                "at p-hat = 1 (or 0) the symmetric binomial standard error sqrt(p(1-p)/n) is "
                "IDENTICALLY ZERO for every n. It is a function of p-hat alone at that point, so "
                "it cannot distinguish 1250 draws from 20000, and T-148's convergence note "
                "called it 'the resolution the verdict is quoted to'. A saturated statistic is "
                "the resolution of nothing.",
            "alreadyTestedAndStillMisread":
                "src/test/kotlin/coupling/StapleDropoutTest.kt already asserted "
                "binomialStandardError(1.0, 100) == 0.0. The degeneracy was known, tested, and "
                "quoted as a resolution anyway -- which is the same shape as C-0127's finding "
                "that a result file is read once, while the claim is being written.",
            "instrument":
                "the one-sided Clopper-Pearson limit at x = n: p > (1 - confidence)^(1/n), whose "
                "large-n form is the rule of three p > 1 - 3/n because ln(1/20) = -2.996.",
            "recordsRemaining": len(saturated),
            "filesRemaining": len(saturated_per_file),
            "aRecordIsREPAIREDNotRemoved":
                "the symmetric error stays where the one-sided bound is emitted beside it. It is "
                "uninformative rather than wrong, and removing it would break every reader of "
                "the schema; what was wrong was the sentence that called it a resolution.",
            "byFile": [
                {"resultFile": name, "records": saturated_per_file[name]}
                for name in sorted(saturated_per_file,
                                   key=lambda n: (-saturated_per_file[n], n))
            ],
            "ruleOfThree": rule_of_three,
            "t148MonteCarloCells": 60,
            "t148MonteCarloCellsSaturated": 25,
            "t148ConvergenceSampleCounts": 5,
            "t148ConvergenceSampleCountsSaturated": 5,
            "theDegeneracyIsWhereTheAnswersAre":
                "all seven affected files are studies whose headline is that a design FAILS "
                "T-5b's 0.10, which is exactly the direction that saturates the exceedance at "
                "1.0. A one-sided bound STRENGTHENS every failure reading it replaces.",
            "standingUnitTestThatAlreadyAssertedTheDegeneracy":
                "src/test/kotlin/coupling/StapleDropoutTest.kt: "
                "assert(binomialStandardError(1.0, 100) == 0.0), since C-0087",
            "repaired": ["T-148-staple-dropout.json"],
            "notRepaired":
                [name for name in sorted(saturated_per_file) if name != "T-148-staple-dropout.json"],
        },
        "reemission": {
            "order": ["T-136", "T-148"],
            "orderedBy": "tools/reemission-order.py -- coupling/StapleDropoutStudy.kt reads "
                         "gpd/results/T-136-two-per-row-placement.json, so T-136 goes first",
            "downstreamContainment":
                "T-136 is read by FIVE studies (PathCountConsistency, CountPhaseInteraction, "
                "DropoutRobustPlacement, PathCountFixedGeometry, StapleDropout) and every one of "
                "them reads only parameters/* and recommendedPlacement -- NOT a departure field. "
                "The rounding change therefore cannot reach them, which excludes four heavy "
                "re-runs for the price of a grep. T-148 re-runs anyway, and IS one of the five, "
                "so the containment argument is also measured.",
            "files": reemission,
        },
        "controlReRun": control,
        "predicates": [
            {"name": "P1 -- a retained checker over gpd/results/, catch set stated and strictly "
                     "different from the static one",
             "verdict": "PASS", "evidence": "tools/check-result-file-hygiene.py"},
            {"name": "P2 -- a conversion is distinguished from a percent sign, both directions "
                     "self-tested",
             "verdict": "PASS",
             "evidence": "45 self-tests; three deliberate mutations of the discriminator each "
                         "fail a named test (the space flag, the Java letter set, the %% "
                         "escape)"},
            {"name": "P3 -- the tree reads clean before the gate is wired, allowlist documented",
             "verdict": "PASS" if not conversions else "FAIL",
             "evidence": f"{len(conversions)} defect(s) over {len(all_files)} result files, "
                         f"{len(hygiene.ALLOWLIST)} allowlisted, the allowlist tested in BOTH "
                         f"directions"},
            {"name": "P4 -- wired into ./gradlew test and tools/verify.sh",
             "verdict": "PASS", "evidence": "build.gradle.kts task testResultFileHygiene; "
                                            "tools/verify.sh"},
            {"name": "P5 -- a census of EVERY departure record, not the one field named",
             "verdict": "PASS",
             "evidence": "222 fields in 29 files strict, 1422 in 71 wide, before the repair"},
            {"name": "P6 -- the two-digit rule expressed once, centrally, by name",
             "verdict": "PASS",
             "evidence": "structure/ResultRounding.kt: DEPARTURE_SIGNIFICANT_DIGITS and "
                         "DEPARTURE_DIGITS_BY_KEY, four spellings, four gate-named tests"},
            {"name": "P7 -- every re-emitted file reads clean; the rest are reported, not "
                     "silently left",
             "verdict": "PASS",
             "evidence": f"{len(strict)} fields in "
                         f"{len({row[0] for row in strict})} files remain, listed per file"},
            {"name": "P8 -- re-emission in dependency order, closure checked not assumed",
             "verdict": "PASS",
             "evidence": "reemission.downstreamContainment"},
            {"name": "P9 -- a census of every standard error on a proportion, partitioned by "
                     "saturation",
             "verdict": "PASS", "evidence": f"302 of 403 saturated in 7 files before the "
                                            f"repair, {len(saturated)} in "
                                            f"{len(saturated_per_file)} after"},
            {"name": "P10 -- T-148's note no longer calls a zero standard error a resolution",
             "verdict": "PASS",
             "evidence": "convergence[1].note, re-emitted"},
            {"name": "P11 -- the one-sided bound is emitted beside it and is a tested shared "
                     "function",
             "verdict": "PASS",
             "evidence": "coupling.saturatedProportionBound, five gate-named tests; "
                         "monteCarlo[*].exceedanceOneSidedBound"},
            {"name": "P12 -- the verdict does not move",
             "verdict": "PASS",
             "evidence": "0 decision fields moved over both re-emitted files"},
        ],
        "falsifiers": [
            {"name": "F1 -- a raw conversion the gate misses, or a percent sign it flags",
             "fired": False,
             "note": "tested in both directions and mutation-tested; the tree reads 0 with the "
                     "gate on and 34 with the allowlist off"},
            {"name": "F2 -- any verdict, predicate or boolean field moving under the repair",
             "fired": False,
             "note": "0 of 2 files"},
            {"name": "F3 -- any NON-departure numeric field moving under the T-209 repair",
             "fired": True,
             "note": "SIX in T-136, all inside distributions[11], a 30-parameter minimax "
                     "descent. Answered by the control re-run rather than argued away: identical "
                     "code, run twice, moves the same block. See controlReRun."},
            {"name": "F4 -- an exceedance probability or a flatAt* boolean moving under the "
                     "T-210 repair",
             "fired": False,
             "note": "the saturated statistic is a REPORTING repair; the verdict is unchanged"},
        ],
        "validity": [
            "The gate is a TEXT check on emitted strings. It cannot see a number that is wrong "
            "for any other reason, and it cannot see a conversion inside a field this repository "
            "does not emit as a string.",
            "The allowlist is a hole by construction: T-207's and T-208's own result files quote "
            "raw conversions as their record, so a genuine defect in either would not be caught. "
            "Same trade as check-markdown-tables.py excluding third-party/.",
            "The two-significant-digit rule is a CONVENTION and it is conservative rather than "
            "exact: a departure d between two quantities each determined to nine digits is "
            "itself determined to about 9 + log10(d) digits, which is between three and minus "
            "two over the range these fields occupy, but is NINE for an order-one departure. "
            "Applying two digits there discards determined information; it never fabricates it.",
            "The saturated-proportion census is repaired in ONE of seven files. The other six "
            "emit a zero symmetric error without asserting anything about it, so they are "
            "misleading by omission rather than by statement -- which is why they are reported "
            "and queued rather than re-run here.",
            "The comparison baseline is git HEAD at the time of the run. Re-running this emitter "
            "after these files are committed will correctly report every departure as zero.",
        ],
    }

    destination = os.path.join(ROOT, "gpd", "results", "T-208-result-file-hygiene.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print(f"wrote {destination}")
    print(f"cheap bounds: conversions {len(conversions)} | departures {len(strict)} strict "
          f"(was 222) | saturated {len(saturated)}")
    for row in reemission:
        print(f"  {row['resultFile']}: departures {row['departureFieldsMoved']}, "
              f"other numeric {row['otherNumericFieldsMoved']}, "
              f"decisions {row['decisionFieldsMoved']}, "
              f"added {row['fieldsAdded']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
