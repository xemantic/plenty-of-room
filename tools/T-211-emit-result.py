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
# T-211 -- emits gpd/results/T-211-seventh-answers-synthesis.json.
#
# The cheap bound and the row-(g) length are both DERIVED from git rather than typed.
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = "29b0153"

ITEMS = ["C-0122", "C-0124", "C-0125", "C-0126", "C-0127", "C-0128",
         "CH-0147", "CH-0148", "CH-0149", "CH-0150", "CH-0151"]

ROWS = [
    ("C-0122", "CARRIED_IN", "the honeycomb station census: SS3 row (g), and it DISCHARGES SS5's "
                             "own missing-measurement entry raised three iterations earlier"),
    ("C-0124", "NOT_CARRIED", "an audit of DECISIONS-FOR-NDI.md. It corrected that document, not "
                              "this one, and no SS6 answer depends on it. Its finding -- that a "
                              "decisions file's drift is one-signed by construction -- is about a "
                              "document class and belongs in CLAUDE.md, where it is."),
    ("C-0125", "CARRIED_IN", "the scaffold remainder: SS5's decisions row 5, with CH-0147's 840 nt "
                             "and CH-0148's effective-charge correction"),
    ("C-0126", "NOT_CARRIED", "a queue reclassification. It marks eight TASKS.md rows contingent "
                              "on the tile-thickness ruling; ANSWERS.md reports answers, not the "
                              "queue's state, and the ruling itself IS carried, as decision 7."),
    ("C-0127", "NOT_CARRIED", "a source repair. Its own finding is that NOT ONE claim had inherited "
                              "a defective number, so by its own measurement this deliverable owes "
                              "nothing -- and saying so is the reason to record the disposition."),
    ("C-0128", "CARRIED_IN", "the oblique root: SS3 row (g) and SS5's discharged census entry"),
    ("CH-0147", "CARRIED_IN", "the recommended cross-section is folded from p7560: SS5 row 5"),
    ("CH-0148", "CARRIED_IN", "bare against effective charge, 1.66x becoming 5.56-6.82x: SS5 row 5"),
    ("CH-0149", "NOT_CARRIED", "a census correction inside C-0125's process note; no number here"),
    ("CH-0150", "NOT_CARRIED", "a diagnosis correction inside C-0127's process note; no number here"),
    ("CH-0151", "CARRIED_IN", "the station census corrected UPWARD, 90/60 to 132/90: SS3 row (g) "
                              "and SS5's discharged entry"),
]


def cited(text, identifier):
    return len(re.findall(r"`" + re.escape(identifier) + r"`", text))


def row_g_length(text):
    for line in text.split("\n"):
        if line.startswith("| (g) |"):
            return len(line)
    return 0


def main():
    with open(os.path.join(ROOT, "ANSWERS.md"), encoding="utf-8") as handle:
        after = handle.read()
    before = subprocess.run(
        ["git", "show", "{}:ANSWERS.md".format(BASE)],
        cwd=ROOT, capture_output=True, text=True, check=True,
    ).stdout

    dispositions = {i: {"disposition": d, "reason": r} for i, d, r in ROWS}
    counts = {}
    for row in dispositions.values():
        counts[row["disposition"]] = counts.get(row["disposition"], 0) + 1

    result = {
        "task": "T-211",
        "claim": "C-0130",
        "what": (
            "The seventh synthesis of ANSWERS.md, against C-0122-C-0128 and CH-0147-CH-0151, and "
            "the REWRITE of SS3 row (g) that C-0121 asked for. No physics is derived. Units: none."
        ),
        "verificationType": "logical, with the five retained checkers run before and after",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "baseCommit": BASE,
        "cheapBound": {
            "citedBefore": sum(1 for i in ITEMS if cited(before, i) > 0),
            "citedAfter": sum(1 for i in ITEMS if cited(after, i) > 0),
            "items": len(ITEMS),
            "note": "0 of 11 is the strongest product signal any pass has had -- C-0106's was 14 "
                    "of 48 and C-0115's 3 of 20. It under-reports, as every pass records.",
        },
        "dispositions": dispositions,
        "dispositionCounts": counts,
        "rowG": {
            "charactersBefore": row_g_length(before),
            "charactersAfter": row_g_length(after),
            "ratio": (row_g_length(before) / row_g_length(after)) if row_g_length(after) else None,
            "historyPreservedVerbatim": "### Row (g)'s derivation history, preserved verbatim" in after,
            "why": "C-0121 recorded that row (g) was the passage most in need of a REWRITE rather "
                   "than another append: eleven revision markers in one table cell, and a standing "
                   "verdict that had reversed twice, so a reader wanting the current answer had to "
                   "reconstruct it from a chronological accretion. The history is kept verbatim "
                   "below the table rather than deleted, because the reversals are the most "
                   "instructive thing in this deliverable and removing them would leave the answer "
                   "looking inevitable.",
        },
        "findings": {
            "aRewriteIsNotADeletion": (
                "The row is 9.3x shorter and NOTHING was discarded: the cell as it stood is quoted "
                "verbatim below the table. A deliverable whose answers have reversed twice owes a "
                "reader both the answer and the reversals, and those are different jobs for "
                "different places on the page."
            ),
            "aDischargeIsAnEDIT": (
                "SS5's honeycomb-census entry was raised at iteration 25 as the largest gap in the "
                "four-layer line and discharged at 26-28 by C-0122, CH-0151 and C-0128. Struck "
                "rather than deleted, with the original kept, because it is the reasoning the "
                "discharge answers."
            ),
            "threeProcessClaimsAreNotCarriedAndTheReasonsDiffer": (
                "C-0124 corrected a different document; C-0126 reclassified the queue, whose state "
                "this deliverable does not report; and C-0127's own measurement is that no claim "
                "inherited a defective number, so it owes nothing here BY ITS OWN FINDING. "
                "Recording the third is the point -- an absence with a measured reason is not the "
                "same as an omission."
            ),
        },
        "validity": [
            "It re-derives nothing; every number is another claim's, at that claim's precision.",
            "The (sigma, L0) window is un-re-run for the seventh consecutive pass, and again for "
            "the same reason: not one item in range is a function of sigma.",
            "The rewritten row (g) states the CURRENT answer. A reader who wants to know how the "
            "programme got there, or which readings were withdrawn, must read the preserved "
            "history -- and the row says so.",
        ],
    }
    destination = os.path.join(ROOT, "gpd", "results", "T-211-seventh-answers-synthesis.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    print("cheap bound {} of {} -> {} | row (g) {} -> {} chars".format(
        result["cheapBound"]["citedBefore"], len(ITEMS), result["cheapBound"]["citedAfter"],
        result["rowG"]["charactersBefore"], result["rowG"]["charactersAfter"]))
    return 0



# `CH-0268` -- this tool WRITES a committed artifact and used to ignore `sys.argv`
# entirely, so `--help` emitted.  Parse the flag or refuse the argument.
import importlib.util as _importlib_util, os as _os
_spec = _importlib_util.spec_from_file_location(
    "cli_guard", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "cli_guard.py"))
_cli_guard = _importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(_cli_guard)

if __name__ == "__main__":
    _cli_guard.refuse_unknown_arguments("tools/T-211-emit-result.py  (no arguments)")
    sys.exit(main())
