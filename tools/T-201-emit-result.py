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
# T-201 -- emits gpd/results/T-201-fifth-answers-synthesis.json.
#
#     tools/T-201-emit-result.py
#
# The audit rows are the content of a synthesis claim, so they are recorded rather than remembered.
# The cheap bound (which items the deliverable cited BEFORE the pass) is re-derived from git rather
# than typed, so it cannot drift: `git show <base>:ANSWERS.md` is the file as it stood.
#
# No wall-clock timing and no step count (`CLAUDE.md`).
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = "9ed4fdc"  # the commit this pass started from: iteration 24's number reservation

CLAIMS = ["C-0106", "C-0107", "C-0108", "C-0109", "C-0110", "C-0111", "C-0112", "C-0113", "C-0114"]
CHALLENGES = [
    "CH-0121", "CH-0122", "CH-0123", "CH-0124", "CH-0125",
    "CH-0126", "CH-0127", "CH-0128", "CH-0129", "CH-0130", "CH-0131",
]

# One row per item. `disposition` is REFLECTED (already in the file before this pass), CARRIED_IN
# (added by this pass) or NOT_CARRIED (deliberately left out, with the reason).
ROWS = [
    ("C-0106", "REFLECTED", "the fourth synthesis itself; its edits ARE the file's SS2/SS3/SS4/SS5 as they stood"),
    ("C-0107", "CARRIED_IN", "the derived row-end prestrain 17.15-24.98 deg, the Snodin scope clause and the reversal of C-0099's oxDNA recommendation -- absent from the file entirely before this pass"),
    ("C-0108", "REFLECTED", "the count/phase interaction was carried into SS1 in iteration 22"),
    ("C-0109", "CARRIED_IN", "the four-layer tile: SS1's flat-tile closure, SS2 row 5b, SS3 row (g), SS5's tile-thickness caveat and SS5's decisions row 6"),
    ("C-0110", "CARRIED_IN", "the tall-gap reach threshold: SS1's NDI paragraph, SS2 Task 2, SS5 decisions rows 2 and 4"),
    ("C-0111", "CARRIED_IN", "the gold PZC and its SIGN: SS5's 'what we cannot answer' PZC bullet, rewritten from STILL OPEN to answered-with-a-different-residue"),
    ("C-0112", "CARRIED_IN", "the interior/row-end decomposition: SS3 row (g) and SS5's prestrain missing-measurement bullet"),
    ("C-0113", "NOT_CARRIED", "a process claim about a checker; ANSWERS.md reports physics, and no answer of SS6 depends on it. Its output IS reported -- the four checker lines quoted in C-0115"),
    ("C-0114", "CARRIED_IN", "the one reserve and its single remaining claimant: SS1's NDI paragraph"),
    ("CH-0121", "REFLECTED", "carried in iteration 22"),
    ("CH-0122", "CARRIED_IN", "the secant-vs-triangle-inequality correction to C-0104's threshold: SS3 row (g) and SS5's prestrain bullet"),
    ("CH-0123", "REFLECTED", "carried in iteration 22"),
    ("CH-0124", "CARRIED_IN", "C-0006's four-layer variant is a mixed state: SS3 row (g), with the 0.160153834 it would have read"),
    ("CH-0125", "CARRIED_IN", "C-0093's brick is mis-specified in three ways, net not signed: SS3 row (g)"),
    ("CH-0126", "CARRIED_IN", "SS3's effort-point row is a CEILING not permission: SS2 Task 2 and SS5 decisions row 2"),
    ("CH-0127", "CARRIED_IN", "the displacement/force split on C-0050's escape: SS1, SS2 Task 2, SS5 rows 2 and 4"),
    ("CH-0128", "CARRIED_IN", "inverseDebyeLength called with a Bjerrum length: SS5's PZC bullet, with its 0.93 % worth"),
    ("CH-0129", "CARRIED_IN", "the published prestrain comparison differences three factors: SS3 row (g)"),
    ("CH-0130", "CARRIED_IN", "the overall corrugation sign is undetermined, raised AND discharged on the number: SS3 row (g) and SS5's prestrain bullet"),
    ("CH-0131", "NOT_CARRIED", "a result-file ordering defect inside C-0101's re-emission sweep. It moves no answer of SS6 and no number this file quotes; T-200 owns the amendment to C-0092, and until that lands the deliverable would be quoting a correction whose own claim does not exist"),
]


def cited_in(text, identifier):
    return len(re.findall(r"`" + re.escape(identifier) + r"`", text))


def main():
    with open(os.path.join(ROOT, "ANSWERS.md"), encoding="utf-8") as handle:
        after = handle.read()
    before = subprocess.run(
        ["git", "show", "{}:ANSWERS.md".format(BASE)],
        cwd=ROOT, capture_output=True, text=True, check=True,
    ).stdout

    items = CLAIMS + CHALLENGES
    cheap_bound = {
        identifier: {"citedBefore": cited_in(before, identifier),
                     "citedAfter": cited_in(after, identifier)}
        for identifier in items
    }
    dispositions = {}
    for identifier, disposition, reason in ROWS:
        dispositions[identifier] = {"disposition": disposition, "reason": reason}

    counts = {}
    for row in dispositions.values():
        counts[row["disposition"]] = counts.get(row["disposition"], 0) + 1

    result = {
        "task": "T-201",
        "claim": "C-0115",
        "what": (
            "The fifth synthesis of ANSWERS.md, against C-0106-C-0114 and CH-0121-CH-0131. A coverage "
            "partition and a statement-by-statement adjudication; no physics is derived and no number is "
            "re-computed. Units: none. The cheap bound is re-derived from git rather than typed."
        ),
        "verificationType": "logical, with the three retained checkers run before and after",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "baseCommit": BASE,
        "range": {"claims": CLAIMS, "challenges": CHALLENGES, "itemCount": len(items)},
        "cheapBound": {
            "citedByIdBeforeThePass": sum(
                1 for v in cheap_bound.values() if v["citedBefore"] > 0
            ),
            "uncitedBeforeThePass": sum(
                1 for v in cheap_bound.values() if v["citedBefore"] == 0
            ),
            "citedByIdAfterThePass": sum(
                1 for v in cheap_bound.values() if v["citedAfter"] > 0
            ),
            "perItem": cheap_bound,
            "note": (
                "It under-reports, as C-0106 records: an item can be reflected in substance without being "
                "cited by id, and an item cited by id can be reflected in one section only. Here it "
                "under-reported in the other direction too -- C-0106 itself is uncited because a synthesis "
                "claim describes the file rather than being quoted in it."
            ),
        },
        "dispositions": dispositions,
        "dispositionCounts": counts,
        "checkers": {
            "before": {
                "traceTokens": 1050, "traceCited": 937, "traceElsewhere": 113, "traceAbsent": 0,
                "openAssertions": 0, "staleTaskStatuses": 0,
                "challengeFiles": 119, "challengesWithDeclaredStatus": 86, "staleChallengeStatuses": 0,
                "selfContradictions": 0, "tableDefects": 0, "tableFiles": 357,
            },
            "after": {
                "traceTokens": 1216, "traceCited": 1103, "traceElsewhere": 113, "traceAbsent": 0,
                "openAssertions": 0, "staleTaskStatuses": 0,
                "challengeFiles": 119, "challengesWithDeclaredStatus": 86, "staleChallengeStatuses": 0,
                "selfContradictions": 0, "tableDefects": 0, "tableFiles": 357,
            },
            "firedDuringThePass": [
                "3 ABSENT -- 0.0344, 0.0577 and 0.0910196802, all three the synthesis TRUNCATING a number "
                "its owning claim states at full width (0.0344013403, 0.0577199433, 0.0910197). Repaired by "
                "restoring the owner's own rendering, which is the rule: round a claim's number only where "
                "the precision is not the content.",
                "1 STALE-OPEN and 1 SELF-CONTRADICTION on T-193 -- the new PZC passage put the word 'open' "
                "inside the verdict window of an identifier the queue records as CLOSED, because the "
                "sentence said 'ANSWERED ... and what is open is a different question'. The residue is not "
                "T-193, so the sentence was wrong as written; rephrased rather than suppressed.",
            ],
            "note": (
                "All four checks were clean BEFORE this pass and are clean after, which is the point "
                "C-0106 established: a determination with no passage is invisible to every check in the "
                "tree. Both firings above were caused by this pass's own edits and both were real."
            ),
        },
        "editedSections": [
            "SS1 -- the flat-tile closure (the four-layer scope correction)",
            "SS1 -- the NDI-questions paragraph (both load-bearing answers withdrawn by measurement, and the one reserve)",
            "SS1 -- the coverage statement, extended to C-0081-C-0114 / CH-0093-CH-0131",
            "SS2 row 5b -- reopened and answered from the other side",
            "SS2 Task 2 -- the tall-layer objection, upheld",
            "SS3 row (g) -- the four-layer scope correction and the whole prestrain branch",
            "SS5 'what we cannot answer' -- the PZC bullet and the row-end-prestrain bullet",
            "SS5 the tile-thickness caveat -- prediction confirmed by measurement",
            "SS5 decisions table rows 2, 4 and 6",
        ],
        "findings": {
            "theCensusIsTheWrongSummaryThisTime": (
                "Four passes have reported 'not one of these is a function of sigma' beside an unmoved "
                "window. It is true again. But two of this range's items are not values at all -- they are "
                "SCOPE corrections to what the file's answers were about (a 2 nm tile where SS3 specifies "
                "~10 nm; a displacement where the clause needs a force), and a census of sigma-dependence "
                "cannot see that class."
            ),
            "underClaimingAgain": (
                "For the second time (after C-0080's T-45) the file asserted a question OPEN that the "
                "corpus had answered: the electrode PZC, answered from published measurement in iteration "
                "23. C-0067's rule holds -- a deliverable that under-claims is as wrong as one that "
                "over-claims and is far harder to catch, because a reviewer checks the assertions and not "
                "the disclaimers."
            ),
            "theCheckerCaughtItsOwnAuthor": (
                "For the third consecutive iteration a retained check caught the mistake of the person "
                "using it -- here twice, on three truncated numbers and on a sentence whose phrasing made "
                "an answered task read open. C-0113 shipped that check one iteration earlier."
            ),
            "threeAgentsReportedTheDebtIndependently": (
                "The three items this pass had to carry were reported by three different agents of "
                "iteration 23, none of which could see the others' reports. A synthesis debt is visible "
                "from inside every branch and from nowhere else."
            ),
        },
    }
    destination = os.path.join(ROOT, "gpd", "results", "T-201-fifth-answers-synthesis.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0


if __name__ == "__main__":
    sys.exit(main())
