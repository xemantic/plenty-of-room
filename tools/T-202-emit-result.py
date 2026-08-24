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
# T-202 -- emits gpd/results/T-202-sixth-answers-synthesis.json.
#
# The cheap bound is re-derived from git rather than typed, as in T-201, so it cannot drift.
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = "90ea3f3"   # the commit this pass started from

ITEMS = ["C-0115", "C-0116", "C-0117", "C-0118", "C-0119", "C-0120"]

ROWS = [
    ("C-0115", "REFLECTED",
     "the fifth synthesis itself; its edits ARE the SS1/SS2/SS3/SS5 this pass extends"),
    ("C-0116", "REFLECTED",
     "carried into SS1, SS2 row 5b and SS3 row (g) by T-196's own discharge in iteration 24"),
    ("C-0117", "NOT_CARRIED",
     "it amends C-0092's A5 margin-movement RANGE from 1.0000-3.3380x to 1.0000 everywhere. This "
     "deliverable never quoted that range -- grepped, zero occurrences -- so carrying the "
     "correction would introduce the number in order to correct it. The amendment lives in C-0092 "
     "and CH-0131 keeps the contradiction."),
    ("C-0118", "CARRIED_IN",
     "the first coupled tile flat at the 90th percentile under measured folding: SS1, SS2 row 5b, "
     "SS3 row (g), and SS5's decisions row 6"),
    ("C-0119", "CARRIED_IN",
     "the tile is a published gel-verified cross-section, the honeycomb lattice is integral, the "
     "seam is forced: SS1, SS2 row 5b, SS3 row (g), SS5 row 6, and SS5's new missing-measurement entry"),
    ("C-0120", "CARRIED_IN",
     "10 x 6 is 6.6x flatter with no threshold at two-thirds the footprint: SS1, SS2 row 5b, "
     "SS3 row (g), SS5 row 6"),
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

    cheap = {i: {"citedBefore": cited_in(before, i), "citedAfter": cited_in(after, i)}
             for i in ITEMS}
    dispositions = {i: {"disposition": d, "reason": r} for i, d, r in ROWS}
    counts = {}
    for row in dispositions.values():
        counts[row["disposition"]] = counts.get(row["disposition"], 0) + 1

    result = {
        "task": "T-202",
        "claim": "C-0121",
        "what": (
            "The sixth synthesis of ANSWERS.md, against C-0115-C-0120 -- the four-layer line. A "
            "coverage partition and a statement-by-statement adjudication; no physics is derived "
            "and no number re-computed. Units: none."
        ),
        "verificationType": "logical, with the retained checkers run before and after",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "baseCommit": BASE,
        "range": {"items": ITEMS, "itemCount": len(ITEMS)},
        "cheapBound": {
            "citedBefore": sum(1 for v in cheap.values() if v["citedBefore"] > 0),
            "uncitedBefore": sum(1 for v in cheap.values() if v["citedBefore"] == 0),
            "citedAfter": sum(1 for v in cheap.values() if v["citedAfter"] > 0),
            "perItem": cheap,
        },
        "dispositions": dispositions,
        "dispositionCounts": counts,
        "checkers": {
            "before": {"traceTokens": 1222, "traceAbsent": 0, "openAssertions": 0,
                       "staleTaskStatuses": 0, "staleChallengeStatuses": 0,
                       "selfContradictions": 0, "tableDefects": 0, "tableFiles": 369},
            "after": {"traceTokens": 1240, "traceAbsent": 0, "openAssertions": 0,
                      "staleTaskStatuses": 0, "staleChallengeStatuses": 0,
                      "selfContradictions": 0, "tableDefects": 0, "tableFiles": 369},
            "firedDuringThePass": [
                "check-markdown-tables.py: ROW 6 cells against 5, on SS2 row 5b. An append landed "
                "INSIDE the owner cell rather than at the end of the verdict cell, which no "
                "numeric or status check could see and which would have widened the whole table. "
                "Repaired by restoring the row from git and re-appending by CELL rather than by "
                "string -- the table checker is the only instrument in the tree that can catch it."
            ],
        },
        "editedSections": [
            "SS1 -- the four-layer paragraph, extended with the three further steps",
            "SS2 row 5b -- the cross-section and the coupled result, and its owner list",
            "SS3 row (g) -- the published design, the honeycomb lattice, the seam, the better "
            "cross-section and the first flat coupled tile",
            "SS5 'what we cannot answer' -- a NEW entry: the honeycomb attachment-lattice census",
            "SS5 decisions row 6 -- the answer has outgrown the question",
            "SS1 -- the coverage statement, extended to C-0115-C-0120",
        ],
        "findings": {
            "theRangeIsFavourableForOnce": (
                "Five previous passes carried corrections, scope failures and withdrawn "
                "recommendations. Four of this range's six items move headline answers in the "
                "FAVOURABLE direction: a located threshold, a published cross-section, a better "
                "one, and the first coupled tile flat under measured folding."
            ),
            "theNewGapIsACensusNotAMeasurement": (
                "SS5 gains an entry that is neither a measurement nor a solve: nobody has counted "
                "what the honeycomb's three crossover azimuths offer as an attachment lattice, so "
                "every path count in C-0118's flat coupled cells is a REQUEST rather than a "
                "demonstration that the stations exist. It is listed because a reader would "
                "otherwise take the coupled flatness result as buildable."
            ),
            "onlyTheTableCheckerCouldSeeThisPassMistake": (
                "The one defect introduced during the pass was a Markdown cell-count error, "
                "invisible to the numeric trace, the status check and the self-consistency check, "
                "and caught immediately by check-markdown-tables.py. Three of the four retained "
                "checkers were clean throughout; the fourth earned its place."
            ),
        },
        "validity": [
            "It re-derives nothing; every number is another claim's, at that claim's precision.",
            "C-0117 is deliberately not carried and the reason is recorded per row.",
            "The (sigma, L0) window is again un-re-run, for the sixth consecutive pass and for the "
            "same reason: not one item in range is a function of sigma.",
            "SS3 row (g) is now very long and carries the whole history of the flatness question. "
            "That is deliberate -- the row's value is that its reversals are all visible in one "
            "place -- but it is the passage most in need of a rewrite rather than an append.",
        ],
    }
    destination = os.path.join(ROOT, "gpd", "results", "T-202-sixth-answers-synthesis.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0



# `CH-0268` -- this tool WRITES a committed artifact and used to ignore `sys.argv`
# entirely, so `--help` emitted.  Parse the flag or refuse the argument.
import importlib.util as _importlib_util, os as _os
_spec = _importlib_util.spec_from_file_location(
    "cli_guard", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "cli_guard.py"))
_cli_guard = _importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(_cli_guard)

if __name__ == "__main__":
    _cli_guard.refuse_unknown_arguments("tools/T-202-emit-result.py  (no arguments)")
    sys.exit(main())
