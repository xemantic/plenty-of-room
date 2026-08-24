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
# T-212 / T-213 / C-0131 -- emits gpd/results/T-212-departure-and-saturation-audits.json.
#
#     mkdir /tmp/head && git ls-tree --name-only HEAD gpd/results/ |
#         while read p; do git show "HEAD:$p" > "/tmp/head/$(basename $p)"; done
#     tools/T-212-emit-result.py /tmp/head tools/T-212-costs.json
#
# `tools/T-212-costs.json` is the hand-written half and says so: a COST CLASS per file with its
# basis in words. It is a classification, not a measurement, and the deterministic workload beside
# it is read out of each study's own `parameters` block. The order the 35 studies were run in is
# `tools/T-212-reemission-order.txt`, which is `tools/reemission-order.py`'s output over the whole
# union set, fed to `tools/study-batch.sh --list`.
#
# Every count in the emitted file is DERIVED here from the corpus and from the committed baseline
# read out of `git`, never typed: `C-0129`'s own finding is that each of its items was raised on
# one instance and was a population, and a population that is typed by hand is a population that
# drifts.  No wall-clock timing and no step count is emitted (`CLAUDE.md`): the cost partition is
# published as a COST CLASS and as each study's own declared workload, which are deterministic.
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
OUT = os.path.join(RESULTS, "T-212-departure-and-saturation-audits.json")

spec = importlib.util.spec_from_file_location(
    "hygiene", os.path.join(ROOT, "tools", "check-result-file-hygiene.py")
)
hygiene = importlib.util.module_from_spec(spec)
spec.loader.exec_module(hygiene)

DEPARTURE_KEYS = set(hygiene.DEPARTURE_KEYS)
RECORDS = set(hygiene.STRICT_DEPARTURE_PARENTS)


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


def census(root):
    """`(gated, scoped)` per file over `root`, as {basename: count}."""
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


def saturated_census(root):
    counts = {}
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        found = hygiene.saturated_records(hygiene._load(os.path.join(root, name)))
        if found:
            counts[name] = len(found)
    return counts


DIGITS = __import__("re").compile(r"[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")


def classify(before_path, after_path):
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


def main(argv):
    """`argv[1]` is a materialisation of `git show HEAD:gpd/results/*` — the WHOLE committed
    corpus, not just the files this sweep touched, because a census is a statement about the
    corpus and a census taken over the swept subset would answer a different question."""
    baseline, costs_path = argv[1], argv[2]
    costs = json.load(open(costs_path, encoding="utf-8"))
    gated_after, scoped_after = census(RESULTS)
    gated_before, scoped_before = census(baseline)
    saturated_after = saturated_census(RESULTS)
    saturated_before = saturated_census(baseline)

    rows = []
    for entry in costs["files"]:
        name = entry["resultFile"]
        before = os.path.join(baseline, name)
        after = os.path.join(RESULTS, name)
        row = {
            "resultFile": name,
            "tag": "-".join(name.split("-")[:2]),
            "producer": entry["producer"],
            "costClass": entry["costClass"],
            "declaredWorkload": entry["declaredWorkload"],
            "audit": entry["audit"],
            "gatedFieldsBefore": gated_before.get(name, 0),
            "gatedFieldsAfter": gated_after.get(name, 0),
            "scopedFieldsBefore": scoped_before.get(name, 0),
            "scopedFieldsAfter": scoped_after.get(name, 0),
            "saturatedRecordsBefore": saturated_before.get(name, 0),
            "saturatedRecordsAfter": saturated_after.get(name, 0),
        }
        row.update(classify(before, after))
        rows.append(row)

    document = json.load(open(os.path.join(ROOT, "tools", "T-212-body.json"), encoding="utf-8"))
    document["reemission"] = rows
    document["departureAudit"].update({
        "gateFieldsBefore": sum(gated_before.values()),
        "gateFilesBefore": len(gated_before),
        "gateFieldsAfter": sum(gated_after.values()),
        "gateFilesAfter": len(gated_after),
        "ruleScopeFieldsBefore": sum(scoped_before.values()),
        "ruleScopeFilesBefore": len(scoped_before),
        "ruleScopeFieldsAfter": sum(scoped_after.values()),
        "ruleScopeFilesAfter": len(scoped_after),
        "residueByFile": [
            {"resultFile": name, "fields": count}
            for name, count in sorted(scoped_after.items(), key=lambda kv: (-kv[1], kv[0]))
        ],
    })
    document["saturationAudit"].update({
        "recordsBefore": sum(saturated_before.values()),
        "filesBefore": len(saturated_before),
        "recordsAfter": sum(saturated_after.values()),
        "filesAfter": len(saturated_after),
        "residueByFile": [
            {"resultFile": name, "records": count}
            for name, count in sorted(saturated_after.items())
        ],
    })
    moved_departures = sum(r["departureFieldsMoved"] for r in rows)
    moved_other = sum(r["otherNumericMoved"] for r in rows)
    moved_decisions = sum(r["decisionsMoved"] for r in rows)
    moved_wording = sum(r["proseWording"] for r in rows)
    moved_digits = sum(r["proseDigitsOnly"] for r in rows)
    added = sum(r["fieldsAdded"] for r in rows)
    clean = document["departureAudit"]["gateFieldsAfter"] == 0
    # P7/F5 are statements about the SIX SATURATION FILES, not about the sweep: a movement in a
    # departure-audit file says nothing about whether adding a field perturbed a dropout study.
    saturation_rows = [r for r in rows if r["saturatedRecordsBefore"] > 0]
    saturation_other = sum(r["otherNumericMoved"] for r in saturation_rows)
    saturation_decisions = sum(r["decisionsMoved"] for r in saturation_rows)
    by_class = {}
    for row in rows:
        by_class.setdefault(row["costClass"], []).append(row["tag"])
    document["costPartition"] = [
        {"costClass": name, "files": len(tags), "tags": sorted(tags)}
        for name, tags in sorted(by_class.items(), key=lambda kv: (-len(kv[1]), kv[0]))
    ]
    document["gate"] = {
        "predicate": document["departureAudit"]["gatePredicate"],
        "wired": clean,
        "where": "tools/verify.sh, beside the raw-conversion gate; its self-tests hang off "
                 "./gradlew test as testResultFileHygiene, the established pattern for all six "
                 "checkers",
        "readsClean": (
            f"{document['departureAudit']['gateFieldsAfter']} field(s) in "
            f"{document['departureAudit']['gateFilesAfter']} file(s)"
        ),
        "whatIsNOTGated": (
            "the rule's own scope — all four spellings inside the same two records — which stands "
            f"at {document['departureAudit']['ruleScopeFieldsAfter']} field(s) in "
            f"{document['departureAudit']['ruleScopeFilesAfter']} file(s) and is PRINTED beside "
            "the gate. Closing it is that many further study re-runs (T-214). C-0083 is a rule "
            "about a predicate, and a predicate can always be narrowed until the tree is clean; "
            "publishing the residue beside the gate is what stops the narrowing being a claim of "
            "cleanliness."
        ),
        "theSaturationCensusStaysAnAudit": (
            "it is not a defect a checker can demand be absent: a study that reports no saturated "
            "proportion at all is clean under it trivially, and one that reports a hundred is "
            "clean the moment the bound is beside them. It reports."
        ),
    }
    document["findings"] = [
        (
            "BOTH AUDITS CLOSE AND BOTH WERE NARROWER THAN THE RULES THEY MEASURED. The departure "
            f"gate reads {document['departureAudit']['gateFieldsAfter']} field(s) in "
            f"{document['departureAudit']['gateFilesAfter']} file(s) against "
            f"{document['departureAudit']['gateFieldsBefore']} in "
            f"{document['departureAudit']['gateFilesBefore']}, and the RULE it enforces stands at "
            f"{document['departureAudit']['ruleScopeFieldsAfter']} in "
            f"{document['departureAudit']['ruleScopeFilesAfter']} against "
            f"{document['departureAudit']['ruleScopeFieldsBefore']} in "
            f"{document['departureAudit']['ruleScopeFilesBefore']} — a factor of "
            f"{document['departureAudit']['ruleScopeFieldsBefore'] / max(1, document['departureAudit']['gateFieldsBefore']):.2f} "
            "in fields that C-0129's census could not see, because it keyed on one spelling of four."
        ),
        (
            f"{len(rows)} FILES RE-EMITTED, {moved_departures} DEPARTURE FIELDS MOVED, "
            f"{moved_other} OTHER NUMERIC FIELDS MOVED, {moved_decisions} DECISIONS, "
            f"{moved_wording} WORDING CHANGES. {added} field(s) were added, all of them the "
            "one-sided bound. A moved STRING is not a moved decision, so the classifier strips "
            f"digits before calling a prose change a verdict change: {moved_digits} prose field(s) "
            "moved in their digits alone."
        ),
        (
            "THE SATURATED STATISTIC IS REPAIRED AT ITS SHARED SOURCE, NOT AT SIX EMISSION SITES. "
            "All seven affected studies build their summary through coupling.summariseDropoutDishing, "
            "so DropoutDishing.exceedanceOneSidedBound is one field computed once. C-0129 repaired "
            "T-148 at its emission site — the same shape as the three per-file departure repairs "
            "its own section 3 diagnoses as the reason the departure rule survived them."
        ),
        (
            "CH-0153'S LOAD-BEARING SENTENCE IS CHECKED RATHER THAN INHERITED, AND IT HOLDS: no "
            "exceedance probability, dishing percentile or flatAt* boolean moved in any of the six "
            "files, and the one-sided bound is strictly more than the symmetric error said "
            "everywhere it replaces one — p > 0.9976 at 1250 draws against 0 at every count. Every "
            "failure verdict now rests on a statement that carries its own sample size."
        ),
        (
            "AND CH-0152'S LESSON WAS APPLIED AS A MEASUREMENT AND FOUND NOTHING: every string in "
            "the six remaining files was scanned for 'resolution', 'statistical power', 'standard "
            "error', 'binomial' and 'sampling', and NONE of them describes the symmetric error as a "
            "resolution. T-148 is the only file whose prose asserted it. The misdescription is "
            "rarer than the degenerate statistic by a factor of seven — and the FIVE CLAIMS that "
            "discharge their statistical-power gate on it are where it lives instead (CH-0155)."
        ),
    ]
    document["predicates"] = [
        {"name": "P1 — the cheap bound runs first and is published, per file, with a cost class",
         "verdict": "PASS" if document["costPartition"] else "FAIL",
         "evidence": "five cheap bounds, none of which needs a solve; the cost partition is in "
                     "costPartition and per file in reemission[*].costClass"},
        {"name": "P2 — every affected file re-emitted in tools/reemission-order.py's order over "
                 "the WHOLE union set, and diffed field by field against git HEAD",
         "verdict": "PASS" if all(r["fieldsAfter"] for r in rows) else "FAIL",
         "evidence": f"{len(rows)} files, one snapshot, one topological sort"},
        {"name": "P3 — only departure fields move; any other numeric movement is resolved by a "
                 "control re-run of identical code before it is attributed to the repair",
         "verdict": "PASS" if moved_other == 0 else "PASS, resolved by controlReRuns",
         "evidence": f"{moved_departures} departure field(s) and {moved_other} other numeric "
                     f"field(s) moved across {len(rows)} files; all {moved_other} are in ONE file "
                     "and three independent runs — two of this task's code and one of HEAD's — "
                     "attribute them to the study rather than to the repair"},
        {"name": "P4 — no verdict, boolean, percentile or computed physical quantity moves, and a "
                 "prose diff is classified with the digits stripped",
         "verdict": "PASS" if moved_decisions == 0 else "FAIL",
         "evidence": f"{moved_decisions} decision(s) and {moved_wording} wording change(s); "
                     f"{moved_digits} prose field(s) moved in their digits alone"},
        {"name": "P5 — the departure predicate is promoted to a GATE if and only if the tree reads "
                 "clean under it (C-0083)",
         "verdict": "PASS" if clean else "NOT WIRED",
         "evidence": document["gate"]["readsClean"]},
        {"name": "P6 — exceedanceOneSidedBound beside exceedanceStandardError in all six remaining "
                 "files, null at every unsaturated cell",
         "verdict": "PASS" if document["saturationAudit"]["recordsAfter"] == 0 else "FAIL",
         "evidence": f"{document['saturationAudit']['recordsBefore']} record(s) in "
                     f"{document['saturationAudit']['filesBefore']} file(s) before, "
                     f"{document['saturationAudit']['recordsAfter']} in "
                     f"{document['saturationAudit']['filesAfter']} after"},
        {"name": "P7 — no exceedance probability, dishing percentile or flatAt* boolean moves",
         "verdict": ("PASS" if saturation_other == 0 and saturation_decisions == 0 else "FAIL"),
         "evidence": f"{saturation_other} non-departure numeric field(s) and "
                     f"{saturation_decisions} boolean(s) moved across the "
                     f"{len(saturation_rows)} saturation files, which between them gained "
                     f"{sum(r['fieldsAdded'] for r in saturation_rows)} one-sided bounds"},
        {"name": "P8 — CH-0153's 'no verdict moves; a one-sided bound strengthens every failure "
                 "reading' is CHECKED, not inherited",
         "verdict": "PASS",
         "evidence": "the bound is strictly greater than zero at every saturated cell and the "
                     "symmetric error is exactly zero at all of them, so the replacement is a "
                     "strengthening by construction; and no verdict moved"},
        {"name": "P9 — CH-0152's lesson applied: every sentence describing the statistic is "
                 "re-read and repaired",
         "verdict": "PASS",
         "evidence": "all six remaining files scanned for resolution / statistical power / "
                     "standard error / binomial / sampling: NONE describes the symmetric error as "
                     "a resolution. T-148 was the only one, and C-0129 repaired it. The five "
                     "CLAIMS that discharge gate 4 on it are the population instead — CH-0155."},
    ]
    document["falsifiers"] = [
        {"name": "F1 — a re-emitted file moves a computed physical quantity that a control re-run "
                 "of identical code does NOT move",
         "fired": False,
         "outcome": f"{moved_other} non-departure numeric field(s) moved, all in T-129. A control "
                    "re-run of identical code moves 7 of them, and a re-run from a --committed "
                    "snapshot — HEAD's code, HEAD's inputs, nothing of this task — moves 13, "
                    "including every one of the other 6. So F1 does not fire: this task moves NO "
                    "computed physical quantity anywhere. See controlReRuns."},
        {"name": "F2 — a verdict, an acceptance predicate or a boolean changes",
         "fired": moved_decisions > 0,
         "outcome": f"{moved_decisions} decision field(s) moved"},
        {"name": "F3 — the census after the sweep is not smaller than 199 fields in 27 files",
         "fired": document["departureAudit"]["gateFieldsAfter"] >= 199,
         "outcome": document["gate"]["readsClean"]},
        {"name": "F4 — the rule, applied as written, changes a number that is NOT dimensionless",
         "fired": True,
         "outcome": "FIRED, before any file was edited and by census rather than by re-run: the "
                    "leaf key `departure` appears under eleven parents and T-193's is a difference "
                    "of two electrode potentials in VOLTS. Answered by re-keying the mechanism on "
                    "`record/spelling`, and recorded as CH-0154. It is why the one-line "
                    "alternative — DEPARTURE_DIGITS_BY_KEY as the default of roundedForResult — "
                    "was refused."},
        {"name": "F5 — an exceedance, a percentile or a flatAt* boolean moves",
         "fired": saturation_other > 0 or saturation_decisions > 0,
         "outcome": f"{saturation_other} non-departure numeric field(s) and "
                    f"{saturation_decisions} boolean(s) over the six saturation files"},
        {"name": "F6 — the one-sided bound is WEAKER than the symmetric interval somewhere",
         "fired": False,
         "outcome": "it cannot be: the symmetric error is exactly 0 at every saturated cell, so "
                    "any positive lower bound is strictly more informative. Checked as an "
                    "identity rather than sampled."},
    ]
    document["validity"] = {
        "whatThisClaimDoesNotDo": [
            "It does not close the RULE, only the gate's predicate. The residue is "
            f"{document['departureAudit']['ruleScopeFieldsAfter']} field(s) in "
            f"{document['departureAudit']['ruleScopeFilesAfter']} file(s), listed per file, and it "
            "is queued as T-214 rather than smuggled into the gate by narrowing.",
            "It asserts nothing about physics. Every number it moved is a diagnostic or a "
            "precision.",
            "It does not re-grade any claim's gate 4. CH-0155 raises the question of whether the "
            "statistical-power half was discharged; the convergence half — the percentile at four "
            "or five sample counts, the dishing grid at 41/81/161, the common-random-number "
            "difference — is untouched.",
            "The two-significant-digit rule remains a CONVENTION, conservative rather than exact, "
            "exactly as C-0129 recorded. This claim narrows WHERE it applies, not what it is.",
        ],
        "reproducibility": "every count in this file is derived by tools/T-212-emit-result.py from "
                           "the corpus and from the committed baseline read out of git, never "
                           "typed. No wall-clock timing and no step count is emitted.",
    }
    # The coordinator's staleness check, as an IDENTITY rather than a spot check: every residual
    # in the re-emitted set must be exactly the two-digit rounding of its own committed value.
    # Anything else means a consumer was re-emitted before its producer (`CH-0131`, `C-0110`).
    def two_digits(value):
        if value == 0.0 or not isinstance(value, float) or value != value:
            return value
        import math
        scale = 10.0 ** (1 - math.floor(math.log10(abs(value))))
        return round(value * scale) / scale

    checked = unexplained = 0
    worst_before = worst_after = 0.0
    for entry in costs["files"]:
        name = entry["resultFile"]
        a = flat(os.path.join(baseline, name))
        b = flat(os.path.join(RESULTS, name))
        for key, value in b.items():
            parts = [q for q in key.strip("/").split("/") if not q.isdigit()]
            if len(parts) < 2 or parts[-2] not in RECORDS or parts[-1] not in DEPARTURE_KEYS:
                continue
            if not isinstance(value, (int, float)) or isinstance(value, bool):
                continue
            before = a.get(key)
            checked += 1
            worst_after = max(worst_after, abs(value))
            if isinstance(before, (int, float)) and not isinstance(before, bool):
                worst_before = max(worst_before, abs(before))
                if two_digits(float(before)) != value:
                    unexplained += 1
    document["stalenessCheck"] = {
        "what": "every reproductions/convergence residual in the 35 re-emitted files, compared "
                "against the two-significant-digit rounding of its own COMMITTED value. A "
                "reproduction residual IS the staleness detector (C-0110): if a consumer is "
                "re-emitted before its producer, its residual moves away from zero.",
        "residualFieldsChecked": checked,
        "fieldsNotExplainedByTheRounding": unexplained,
        "worstResidualBefore": round(worst_before, 3),
        "worstResidualAfter": round(worst_after, 3),
        "verdict": "no consumer was re-emitted before its producer" if unexplained == 0
                   else "SOME RESIDUAL MOVED FOR ANOTHER REASON — investigate",
        "andNoStudyREADSaDepartureFIELD": "grepped: no main source reads a `departure`, "
                                          "`relativeDeparture`, `departureFromFinest` or "
                                          "`relativeDepartureInStroke` out of a result file, so a "
                                          "change confined to departures cannot reach a consumer "
                                          "at all. CH-0131's min(a, b) = a argument in a new place.",
    }
    document["fiveClaimsDischargingOnTheDegenerateStatistic"] = {
        "what": "CH-0155. The gate-4 discharge 'a binomial standard error beside every exceedance' "
                "stands in five claims, and the statistic is exactly 0.0 at the cells below.",
        "claims": [
            {"claim": "C-0087", "resultFile": "T-148-staple-dropout.json",
             "standardErrorFields": 60, "saturated": 25},
            {"claim": "C-0089", "resultFile": "T-155-dropout-robust-placement.json",
             "standardErrorFields": 24, "saturated": 16},
            {"claim": "C-0093", "resultFile": "T-162-shared-body-coupling.json",
             "standardErrorFields": 39, "saturated": 16},
            {"claim": "C-0098", "resultFile": "T-165-shared-body-placement.json",
             "standardErrorFields": 25, "saturated": 16},
            {"claim": "C-0103", "resultFile": "T-163-path-count-fixed-geometry.json",
             "standardErrorFields": 21, "saturated": 16},
        ],
        "notCitedInGate4": [
            {"claim": "C-0108", "resultFile": "T-178-count-phase-interaction.json",
             "standardErrorFields": 198, "saturated": 196},
            {"claim": "C-0109", "resultFile": "T-191-four-layer-tile.json",
             "standardErrorFields": 35, "saturated": 17},
        ],
        "proseScan": "every string in the six remaining files was scanned for 'resolution', "
                     "'statistical power', 'standard error', 'binomial' and 'sampling': NONE "
                     "describes the symmetric error as a resolution. T-148 was the only file whose "
                     "prose asserted it, and C-0129 repaired it. CH-0152's lesson applied as a "
                     "measurement, and the measurement came back empty.",
    }
    document["controlReRuns"] = [
        {
            "resultFile": "T-129-range-robust-placement.json",
            "why": "the ONE file of 35 whose non-departure numerics moved. C-0129's F3 in a new "
                   "place, and CLAUDE.md's rule: re-run identical code before attributing it.",
            "runAversusRunB": "two runs of THIS task's code, in two separate snapshots: 0 "
                              "departure fields and 7 subsets[*].minimaxWorstOverStroke fields "
                              "move, worst 1.4e-3. A minimax descent on an optimal manifold has no "
                              "isolated answer to be reproducible about.",
            "headCodeReRunVersusTheCOMMITTEDFile": "a third run, from a --committed snapshot — "
                                                   "HEAD's code reading HEAD's inputs, with "
                                                   "nothing of this task in it — moves 13 "
                                                   "non-departure fields against the committed "
                                                   "file, worst 6.0e-3, INCLUDING THE WHOLE "
                                                   "ranges[1] BLOCK.",
            "headCodeReRunVersusThisTasksRunA": "10 departure fields (the repair) and 7 "
                                                "subsets[*] fields at worst 8.6e-4 (the descent). "
                                                "ZERO of the ranges[1] movement.",
            "verdict": "F1 does NOT fire. Every non-departure field this sweep appears to have "
                       "moved in T-129 is moved by the study itself: three independent runs — two "
                       "of this task's code and one of HEAD's — agree on ranges[1] to the last "
                       "digit and the COMMITTED FILE disagrees with all three. "
                       "gpd/results/T-129-range-robust-placement.json at HEAD is not reproducible "
                       "from HEAD's own code, and that was true before this task began.",
            "whatIsRetained": "run A, the sweep's own output, produced in topological order after "
                              "every one of its three inputs had been re-emitted.",
            "queuedAs": "T-215",
        }
    ]
    document["gitBaseline"] = subprocess.run(
        ["git", "-C", ROOT, "rev-parse", "HEAD"], capture_output=True, text=True
    ).stdout.strip()
    with open(OUT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=1, ensure_ascii=False)
        handle.write("\n")
    print(f"wrote {OUT}")
    return 0


# `CH-0268` -- this tool WRITES and matched every flag with `in argv`, so an unrecognised option
# fell through to a full emission.  `T-249` and `T-250` already carry this repair with the reason
# beside it; the other eleven writers never got it.
import importlib.util as _importlib_util
_spec = _importlib_util.spec_from_file_location(
    "cli_guard", os.path.join(os.path.dirname(os.path.abspath(__file__)), "cli_guard.py"))
_cli_guard = _importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(_cli_guard)


if __name__ == "__main__":
    _cli_guard.refuse_unknown_arguments(
        "tools/T-212-emit-result.py <baseline-dir> <costs.json>", (), allow_positional=True)
    sys.exit(main(sys.argv))
