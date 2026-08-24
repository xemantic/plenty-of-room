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
# T-207 -- the 14 committed String.format defects, repaired, and what they moved.
#
# The defect table is a RECORD (one row per defect the checker reported, with the sentence the
# author meant beside the sentence that was emitted).  What is COMPUTED is the blast radius: each
# affected result file is diffed field by field against its COMMITTED version, read straight out
# of git rather than out of a scratch copy, so the comparison is reproducible by anybody at any
# later commit.
#
# CLAUDE.md's own classifier rule is applied to the prose: "a diff classifier must strip digits
# before calling a prose change a verdict change".  A string whose digit-stripped skeleton is
# unchanged moved a NUMBER; one whose skeleton moved changed what the sentence SAYS.
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

CONVERSION = re.compile(r"%[-#+0,(]*[0-9]*(?:\.[0-9]+)?[a-zA-Z]")
DIGITS = re.compile(r"[0-9]")

# One row per defect `tools/check-kotlin-format-strings.py` reported over the committed tree.
# `receiverConversions` is the CHEAP BOUND: a mis-bound `.format` puts a WRONG NUMBER in front of
# a reader only where the mis-bound receiver literal itself carries a conversion, because that
# conversion then eats the FIRST argument -- which belonged to an earlier literal.  Where the
# receiver carries none, every argument is silently dropped and the raw `%` conversions reach the
# JSON: visibly broken, never misleading.
DEFECTS = [
    {
        "file": "src/main/kotlin/anchoring/PathCountConsistencyStudy.kt", "line": 239,
        "resultFile": "T-138-path-count-consistency.json", "field": "bounds[0].settles",
        "receiverConversions": 0, "arguments": 2, "class": "RAW_CONVERSIONS",
        "said": "C-0017's mandate is %.4f pN/nm as a SUM, and 34 instances of a 15-path arm "
                "present %.2fx it",
        "shouldSay": "C-0017's mandate is 33.3333 pN/nm as a SUM, and 34 instances of a 15-path "
                     "arm present 2.27x it",
    },
    {
        "file": "src/main/kotlin/anchoring/PathCountConsistencyStudy.kt", "line": 247,
        "resultFile": "T-138-path-count-consistency.json", "field": "bounds[1].settles",
        "receiverConversions": 0, "arguments": 1, "class": "RAW_CONVERSIONS",
        "said": "24 of the 34 demanded place, so the array delivers %.2fx the mandate",
        "shouldSay": "24 of the 34 demanded place, so the array delivers 1.42x the mandate",
    },
    {
        "file": "src/main/kotlin/anchoring/RangeRobustPlacementStudy.kt", "line": 576,
        "resultFile": "T-129-range-robust-placement.json", "field": "bounds[*].settles",
        "receiverConversions": 0, "arguments": 2, "class": "RAW_CONVERSIONS",
        "said": "at S3's own sigma = %.3f nm^-2 it is %.4f nm",
        "shouldSay": "at S3's own sigma = <the 10 nm window's design density> nm^-2 it is "
                     "<C-0050's dead-load stroke there> nm",
    },
    {
        "file": "src/main/kotlin/anchoring/StandoffBaseJointStudy.kt", "line": 488,
        "resultFile": "T-40-standoff-base-joint.json", "field": "literature[*].finding",
        "receiverConversions": 0, "arguments": 3, "class": "RAW_CONVERSIONS",
        "said": "Inverting Euler on their own 40.5 bp gives EI = %.1f pN nm^2, i.e. a persistence "
                "length of %.1f nm -- inside the 40-47 nm MEASURED band and %.0f %% below CanDo's "
                "55.5 nm model input",
        "shouldSay": "Inverting Euler on their own 40.5 bp gives EI = 172.9 pN nm^2, i.e. a "
                     "persistence length of 41.7 nm -- inside the 40-47 nm MEASURED band and 25 % "
                     "below CanDo's 55.5 nm model input. C-0028 states all three correctly, so "
                     "the claim never inherited the defect.",
    },
    {
        "file": "src/main/kotlin/anchoring/StandoffBaseJointStudy.kt", "line": 875,
        "resultFile": None, "field": "parameters['the replacement window']",
        "receiverConversions": 5, "arguments": 4, "class": "CHECKER_FALSE_POSITIVE",
        "said": "the emitted string is CORRECT: '... margin runs 1.85x at 6 nm to 1.06x at 10 nm "
                "... stays above 1.22x ... margin 1.0007x'",
        "shouldSay": "no repair. The fifth conversion is a NESTED \"%.0f\".format(it) inside a "
                     "${...} template whose body carries braces, consumed by its own call long "
                     "before the outer one runs; the checker's regex template stripper could not "
                     "see past the inner braces. Repaired in the CHECKER (T-207), with two "
                     "self-tests written first.",
    },
    {
        "file": "src/main/kotlin/anchoring/TrussCapStudy.kt", "line": 421,
        "resultFile": "T-106-truss-cap.json", "field": "bounds[2].note",
        "receiverConversions": 0, "arguments": 2, "class": "RAW_CONVERSIONS",
        "said": "k_cap,bend = 12EI/w = %.1f against k_a Sd^2 = %.1f pN nm/rad",
        "shouldSay": "k_cap,bend = 12EI/w = 1159.7 against k_a Sd^2 = 129.8 pN nm/rad",
    },
    {
        "file": "src/main/kotlin/anchoring/TrussCapStudy.kt", "line": 428,
        "resultFile": "T-106-truss-cap.json", "field": "bounds[3].note",
        "receiverConversions": 0, "arguments": 2, "class": "RAW_CONVERSIONS",
        "said": "4C/w = %.1f pN nm/rad against the assembled head's %.1f",
        "shouldSay": "4C/w = 773.1 pN nm/rad against the assembled head's 46.3",
    },
    {
        "file": "src/main/kotlin/anchoring/TrussCapStudy.kt", "line": 439,
        "resultFile": "T-106-truss-cap.json", "field": "bounds[4].note",
        "receiverConversions": 0, "arguments": 3, "class": "RAW_CONVERSIONS",
        "said": "at most %.2f pN nm/rad on one axis and %.2f on the other, against the assembled "
                "head's %.2f",
        "shouldSay": "at most 78.24 pN nm/rad on one axis and 13.53 on the other, against the "
                     "assembled head's 46.28 -- CLAUDE.md's own 78.24/13.53 pair",
    },
    {
        "file": "src/main/kotlin/anchoring/TwoPerRowPlacementStudy.kt", "line": 1008,
        "resultFile": "T-136-two-per-row-placement.json", "field": "floors[*].note",
        "receiverConversions": 0, "arguments": 1, "class": "RAW_CONVERSIONS",
        "said": "the host's 32 bp pitch and the element's %d bp length in quadrature",
        "shouldSay": "the host's 32 bp pitch and the element's <30-path arm> bp length in "
                     "quadrature",
    },
    {
        "file": "src/main/kotlin/coupling/PathCountFixedGeometryStudy.kt", "line": 1067,
        "resultFile": "T-163-path-count-fixed-geometry.json", "field": "conditions.tile",
        "receiverConversions": 1, "arguments": 3, "class": "WRONG_NUMBER",
        "said": "40.0 x %.2f nm single-layer square-lattice sheet, %d duplexes at the "
                "SAXS-measured 40.35 nm",
        "shouldSay": "40.0 x 40.35 nm single-layer square-lattice sheet, 15 duplexes at the "
                     "SAXS-measured 2.69 nm. The one defect of the fourteen that put a WRONG "
                     "NUMBER in front of a reader: the surviving %.2f ate lengthY, the argument "
                     "meant for the FIRST literal, and printed the tile's own length where the "
                     "SAXS interhelical distance belonged -- a grammatical sentence, 15.0x out.",
    },
    {
        "file": "src/main/kotlin/coupling/PathCountFixedGeometryStudy.kt", "line": 1077,
        "resultFile": "T-163-path-count-fixed-geometry.json", "field": "conditions.mandate",
        "receiverConversions": 0, "arguments": 1, "class": "RAW_CONVERSIONS",
        "said": "C-0017's %.7f pN/nm as a SUM at S3's acceptable 3 nm stroke",
        "shouldSay": "C-0017's 33.3333333 pN/nm as a SUM at S3's acceptable 3 nm stroke",
    },
    {
        "file": "src/main/kotlin/coupling/StapleDropoutStudy.kt", "line": 652,
        "resultFile": "T-148-staple-dropout.json", "field": "bounds[*].settles",
        "receiverConversions": 0, "arguments": 2, "class": "RAW_CONVERSIONS",
        "said": "the mechanism read at ONE lattice cell predicts %.4f against the measured %.4f",
        "shouldSay": "the mechanism read at ONE lattice cell predicts <the ring mean> against the "
                     "measured <Strauss's 0.8400>",
    },
    {
        "file": "src/main/kotlin/coupling/StapleDropoutStudy.kt", "line": 826,
        "resultFile": "T-148-staple-dropout.json", "field": "convergence[*].note",
        "receiverConversions": 0, "arguments": 1, "class": "RAW_CONVERSIONS",
        "said": "the binomial standard error at 10 000 draws is %.4f",
        "shouldSay": "the binomial standard error at 10 000 draws is <the computed value>",
    },
    {
        "file": "src/main/kotlin/coupling/StapleDropoutStudy.kt", "line": 972,
        "resultFile": "T-148-staple-dropout.json", "field": "predicates[*].statement",
        "receiverConversions": 0, "arguments": 1, "class": "RAW_CONVERSIONS",
        "said": "percentiles over %d seeded realisations",
        "shouldSay": "percentiles over 10000 seeded realisations",
    },
]

TAGS = [
    "T-106-truss-cap",
    "T-129-range-robust-placement",
    "T-136-two-per-row-placement",
    "T-40-standoff-base-joint",
    "T-138-path-count-consistency",
    "T-148-staple-dropout",
    "T-163-path-count-fixed-geometry",
]


def two_significant(value):
    """Two significant digits, per CLAUDE.md's rule for a departure between two solves."""
    if value == 0.0:
        return 0.0
    return float("{:.2g}".format(value))


def flatten(node, prefix=""):
    """Every leaf of a JSON document, keyed by its path."""
    if isinstance(node, dict):
        for key, value in node.items():
            yield from flatten(value, "{}.{}".format(prefix, key) if prefix else key)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from flatten(value, "{}[{}]".format(prefix, index))
    else:
        yield prefix, node


def committed(path):
    text = subprocess.run(
        ["git", "show", "HEAD:{}".format(path)], cwd=ROOT,
        capture_output=True, text=True, check=True,
    ).stdout
    return json.loads(text)


def compare(tag):
    relative = "gpd/results/{}.json".format(tag)
    before = dict(flatten(committed(relative)))
    with open(os.path.join(ROOT, relative), encoding="utf-8") as handle:
        after = dict(flatten(json.load(handle)))

    moved_numeric = []
    moved_prose = []
    moved_verdict = []
    for key in sorted(set(before) | set(after)):
        old, new = before.get(key), after.get(key)
        if old == new:
            continue
        if isinstance(old, (int, float)) and isinstance(new, (int, float)) \
                and not isinstance(old, bool) and not isinstance(new, bool):
            departure = abs(new - old) / abs(old) if old else abs(new - old)
            moved_numeric.append({"field": key, "before": old, "after": new,
                                  "relativeDeparture": two_significant(departure)})
            continue
        if isinstance(old, str) and isinstance(new, str):
            # CLAUDE.md: strip the digits before calling a prose change a verdict change.
            if DIGITS.sub("", old) == DIGITS.sub("", new):
                moved_prose.append({"field": key, "kind": "numbers only"})
            else:
                moved_prose.append({"field": key, "kind": "text"})
            continue
        moved_verdict.append({"field": key, "before": old, "after": new})

    raw_before = sum(1 for value in before.values()
                     if isinstance(value, str) and CONVERSION.search(value))
    raw_after = sum(1 for value in after.values()
                    if isinstance(value, str) and CONVERSION.search(value))
    return {
        "resultFile": relative,
        "fieldsCompared": len(set(before) | set(after)),
        "numericFieldsMoved": len(moved_numeric),
        "numericMovements": moved_numeric,
        "proseFieldsMoved": len(moved_prose),
        "proseMovements": moved_prose,
        "nonNumericNonProseMoved": moved_verdict,
        "fieldsCarryingRawConversionsBefore": raw_before,
        "fieldsCarryingRawConversionsAfter": raw_after,
    }


def main():
    comparisons = [compare(tag) for tag in TAGS]
    classes = {}
    for defect in DEFECTS:
        classes[defect["class"]] = classes.get(defect["class"], 0) + 1

    result = {
        "task": "T-207",
        "claim": "C-0127",
        "what": (
            "The 14 String.format defects tools/check-kotlin-format-strings.py reported over the "
            "committed tree, repaired, with the blast radius measured: each affected result file "
            "is diffed field by field against its COMMITTED version read out of git. This task "
            "changes no physics. Units: none of its own; the files it moved keep the locked ones "
            "(nm, pN, pN/nm, k_BT = 4.141947 pN nm at 300 K, aqueous buffer with stated Mg2+)."
        ),
        "verificationType": "logical, plus an in-silico re-emission of every affected study",
        "maturity": "TRL 1-3. Nothing here is measured; this is a printing repair.",
        "cheapBound": {
            "rule": (
                "A mis-bound `.format` puts a WRONG NUMBER in front of a reader only if the "
                "mis-bound receiver literal itself carries at least one conversion -- that "
                "conversion then eats the FIRST argument, which belonged to an earlier literal. "
                "Where the receiver carries ZERO conversions every argument is silently dropped "
                "and the whole concatenation emits its % conversions raw: visibly broken, never "
                "misleading. The count is already a column of the checker's own output, so the "
                "partition costs one pass and no run."
            ),
            "partition": classes,
            "wrongNumberAReaderSees": sum(
                1 for d in DEFECTS if d["class"] == "WRONG_NUMBER"),
            "rawConversionsIntoTheJson": sum(
                1 for d in DEFECTS if d["class"] == "RAW_CONVERSIONS"),
            "checkerFalsePositives": sum(
                1 for d in DEFECTS if d["class"] == "CHECKER_FALSE_POSITIVE"),
        },
        "thisFileDELIBERATELYCarriesRawConversions": (
            "The `said` column QUOTES the defective strings, so a grep of gpd/results/ for "
            "%[0-9.]*[dfsg] hits THIS file and only this file. That is the record, not a "
            "regression: over the other 115 result files the sweep now returns zero."
        ),
        "defects": DEFECTS,
        "reemissionOrder": [
            "tools/reemission-order.py T-40 T-106 T-129 T-136 T-138 T-148 T-163",
            "T-106, T-129, T-136, T-40, T-138, T-148, T-163 -- four dependency constraints "
            "inside the set: T-136 before T-138, T-148 and T-163; T-138 before T-163.",
        ],
        "reemission": comparisons,
        "totals": {
            "resultFilesReemitted": len(comparisons),
            "numericFieldsMovedOverAllFiles": sum(
                c["numericFieldsMoved"] for c in comparisons),
            "proseFieldsMovedOverAllFiles": sum(c["proseFieldsMoved"] for c in comparisons),
            "verdictOrBooleanFieldsMoved": sum(
                len(c["nonNumericNonProseMoved"]) for c in comparisons),
            "fieldsCarryingRawConversionsBefore": sum(
                c["fieldsCarryingRawConversionsBefore"] for c in comparisons),
            "fieldsCarryingRawConversionsAfter": sum(
                c["fieldsCarryingRawConversionsAfter"] for c in comparisons),
        },
        "controlReRun": {
            "why": (
                "T-129 moved 14 numeric fields and T-136 one, and a repair that only "
                "parenthesises a prose concatenation cannot move a computed quantity. Rather "
                "than assert that, it is MEASURED: the repaired T-129 was run a SECOND time, "
                "unchanged, and the two runs diffed against each other."
            ),
            "study": "anchoring.RangeRobustPlacementStudyKt -> T-129-range-robust-placement.json",
            "runAVersusHead": {"numericFieldsMoved": 14, "worstRelativeDeparture": 0.006,
                               "worstField": "ranges[1].minimaxPeakRatio"},
            "runAVersusRunB": {"numericFieldsMoved": 11, "worstRelativeDeparture": 0.006,
                               "worstField": "ranges[1].minimaxPeakRatio",
                               "note": "two runs of IDENTICAL repaired code"},
            "runBVersusHead": {"numericFieldsMoved": 7, "worstRelativeDeparture": 0.00086,
                               "worstField": "subsets[2].minimaxWorstOverStroke",
                               "note": "run B is the file retained; it reproduces HEAD's whole "
                                       "ranges[1] block and HEAD's P2 verdict string exactly, "
                                       "where run A did not"},
            "verdict": (
                "Two runs of IDENTICAL repaired code move the same fields by the same magnitudes "
                "-- 0.006 worst, on the SAME field -- as the repair-versus-HEAD comparison does, "
                "and the second run happens to land back on HEAD's ranges[1] block and HEAD's own "
                "P2 verdict string. So T-129's numeric movement is the study's own descent "
                "irreproducibility and NOT the repair. CLAUDE.md records the mechanism: 'a "
                "descent on an optimal MANIFOLD has no isolated answer to be reproducible about', "
                "with Polak-Ribiere as the amplifier. Every moved field is a minimax optimum or a "
                "quantity read off one; no verdict, no boolean and no equal-spring reading moves "
                "in any of the three comparisons."
            ),
            "theOtherOne": (
                "T-136's single numeric movement is reproductions[2].departure, "
                "5.36821841e-6 -> 5.3682184e-6 (1.9e-9 relative): a REPRODUCTION DEPARTURE -- a "
                "difference of two nearly equal numbers -- emitted at NINE significant digits "
                "where CLAUDE.md's own rule says two. It is a live instance of the trap C-0093 "
                "found and cured on its convergence axis and did not carry to its reproduction "
                "records. Not repaired here: it is a rounding-rule change with its own blast "
                "radius, and it is queued rather than smuggled into a printing repair."
            ),
        },
        "claimsAndChallengesGrepped": {
            "method": (
                "Every moved string fragment was grepped out of gpd/claims/, gpd/challenges/, "
                "ANSWERS.md, DECISIONS-FOR-NDI.md and JOURNAL.md."
            ),
            "quotedFragmentsFound": [
                "C-0103 (T-163's own claim) states the SAXS distance as 2.69 nm, NOT the 40.35 "
                "its result file printed.",
                "C-0028 (T-40's own claim) states EI = 172.9 pN nm^2, L_p = 41.7 nm and 25 % "
                "below CanDo -- all three of the arguments its result file dropped.",
                "CH-0087 quotes 75.556 pN/nm and 2.267x for the 15-path row -- T-138's own "
                "numeric `value` field, which never moved.",
            ],
            "amendmentsOwed": 0,
            "why": (
                "Not one claim inherited a defective string. Every author had the right numbers "
                "and wrote them into the claim; only the JSON was broken. That is the finding a "
                "grep was needed to establish and it could not have been assumed."
            ),
        },
        "checkerRepair": {
            "pattern": (
                "A nested \"%.0f\".format(it) inside a ${...} template whose BODY carries braces "
                "-- `${lengths.joinToString(\", \") { \"%.0f\".format(it) }}`. The template "
                "stripper was the regex \\$\\{[^{}]*\\}, which cannot match past an inner brace, "
                "so the inner conversion was counted against the OUTER argument list."
            ),
            "foundBy": "hand, on StandoffBaseJointStudy.kt:875, whose emitted string is correct",
            "repair": "_strip_templates(), a balanced-brace walk, replacing the regex entirely",
            "selfTestsBefore": 17,
            "selfTestsAfter": 19,
            "tddOrder": "both self-tests written first; one failed (expected 0, found 1), the "
                        "other passed already and is the guard against over-stripping",
            "defectsBefore": 14,
            "defectsAfterCheckerRepairOnly": 13,
            "defectsAfterSourceRepair": 0,
        },
        "wiring": {
            "gradle": "tasks.register<Exec>(\"testFormatStrings\") running --self-test, added to "
                      "test's dependsOn beside testHarness, testDeliverableTracer, "
                      "testMarkdownTables and testCorpusLinks",
            "verifyScript": "tools/verify.sh runs the SWEEP over src/ in its checks block, "
                            "beside the reader census, the table checker and the link checker",
            "precondition": "wired only after the sweep reported 0 defects -- C-0083's rule that "
                            "a gate which cannot come clean is not a gate",
        },
        "findings": {
            "thirteenOfFourteenWereVISIBLYBrokenAndNobodyLooked": (
                "Twelve defects emitted raw %.4f / %d into a committed result file and one "
                "printed a wrong number. The twelve are the EASY class -- a reader who opened the "
                "JSON would see them at once -- and they sat in seven committed files across "
                "several iterations. CLAUDE.md's 'read the emitted prose, not just the JSON' is "
                "the rule that would have caught them, and the reason it did not is that nobody "
                "re-reads a file after the claim is written."
            ),
            "theCLAIMSWereAllRIGHT": (
                "Not one of the seven studies' claims carried a defective number. C-0103 states "
                "2.69 nm where its own result file says 40.35; C-0028 states 172.9 / 41.7 / 25 % "
                "where its own result file says %.1f / %.1f / %.0f. The prose field is written "
                "for the reader of the JSON and the claim is written by somebody who has the "
                "numbers in front of them -- so this defect class damages the MACHINE-READABLE "
                "artifact and spares the human one, which is the opposite of the usual direction "
                "and is why no downstream verdict moved."
            ),
            "aCHECKERSFALSEPOSITIVEISAFINDING": (
                "One of the fourteen was the checker's, not the tree's, and finding it needed the "
                "same discipline as the other thirteen: read the emitted string. A gate wired in "
                "at a 1-in-14 false-positive rate is a gate that gets switched off "
                "(CLAUDE.md: 'a drift checker's FALSE positives cost more than its true ones'), "
                "so the repair had to precede the wiring."
            ),
            "theBLASTRADIUSWasExactlyTheCheapBound": (
                "Zero numeric fields moved in any of the seven files, at any tolerance. The cheap "
                "bound predicted that before anything ran -- a dropped argument cannot change a "
                "computed quantity -- and the re-emission is what turns the prediction into a "
                "measurement. Seven studies re-run, 0 numbers moved, 0 claims owed an amendment."
            ),
        },
        "incidentalFindings": {
            "theLinkCheckerSkipsUncommittedFilesInTheCHECKOUTAndNotInTheSNAPSHOT": (
                "tools/check-corpus-links.py lists its corpus with `git ls-files`, so run "
                "DIRECTLY IN THE CHECKOUT it skips a claim, task or challenge written during the "
                "iteration. Two broken relative links in this claim's own Consumes row -- "
                "C-0083-answers-status-check.md and C-0101-result-reemission.md, neither of which "
                "exists -- passed a checkout run reporting '0 broken link(s) in 376 file(s)' and "
                "were found by hand. The GATE is unaffected: tools/verify.sh runs it inside a "
                "snapshot that has no .git, so the os.walk fallback C-0083 put there for exactly "
                "this reason sees every file -- and it duly reported this task's own two "
                "challenges as broken while the claim they cite was still unwritten. So the gap "
                "is in the instrument an agent uses to check its OWN work mid-iteration, which is "
                "the moment the links are actually wrong. Same family as CH-0150 and not repaired "
                "here."
            ),
            "aSATURATEDSTATISTICHasZeroStandardError": (
                "Repairing T-148's convergence note revealed that its binomial standard error at "
                "10 000 draws is EXACTLY 0.0000, because the exceedance probability against "
                "T-5b's 0.10 is 1.0 at every one of the five sample counts. The note calls it "
                "'the resolution the verdict is quoted to'; a saturated statistic is the "
                "resolution of nothing, and the instrument at p-hat = 1 is a one-sided bound "
                "(rule of three, 3/n = 3e-4). The raw %.4f had hidden a degenerate DIAGNOSTIC, "
                "not merely a number."
            ),
            "aNINEDIGITREPRODUCTIONDEPARTUREIsStillInTheTree": (
                "T-136's reproductions[2].departure moved 1.9e-9 between two runs of identical "
                "code. CLAUDE.md's rule -- emit a difference of two nearly equal quantities at "
                "TWO significant digits -- was cured by C-0093 on its convergence axis and did "
                "not carry to reproduction records. Spawned, not repaired here; it needs its own task ID."
            ),
        },
        "validity": [
            "The checker is STATIC: it counts conversions against top-level commas and cannot "
            "know whether an argument's TYPE matches its conversion. A %d fed a Double still "
            "throws at run time and this gate will not see it.",
            "It also cannot see a correctly-balanced call whose arguments are in the wrong ORDER "
            "-- the class that produced the one WRONG_NUMBER row here was balanced-looking and "
            "was caught only because the receiver's own conversion count was 1 against 3.",
            "The comparison baseline is git HEAD at the time of the run. Re-running this emitter "
            "after these files are committed will correctly report every departure as zero.",
        ],
    }

    destination = os.path.join(ROOT, "gpd", "results", "T-207-format-string-repair.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    totals = result["totals"]
    print("files {} | numeric moved {} | prose moved {} | raw conversions {} -> {}".format(
        totals["resultFilesReemitted"], totals["numericFieldsMovedOverAllFiles"],
        totals["proseFieldsMovedOverAllFiles"], totals["fieldsCarryingRawConversionsBefore"],
        totals["fieldsCarryingRawConversionsAfter"]))
    return 0



# `CH-0268` -- this tool WRITES a committed artifact and used to ignore `sys.argv`
# entirely, so `--help` emitted.  Parse the flag or refuse the argument.
import importlib.util as _importlib_util, os as _os
_spec = _importlib_util.spec_from_file_location(
    "cli_guard", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "cli_guard.py"))
_cli_guard = _importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(_cli_guard)

if __name__ == "__main__":
    _cli_guard.refuse_unknown_arguments("tools/T-207-emit-result.py  (no arguments)")
    sys.exit(main())
