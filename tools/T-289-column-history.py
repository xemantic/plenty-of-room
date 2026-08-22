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
# T-289 -- the COLUMN predicate's false-positive rate over the queue's own history.
#
#     tools/T-289-column-history.py [--json] [--limit N] [--ref REF]
#
# `CLAUDE.md` is explicit that a drift checker's FALSE positives cost more than its true ones and
# that this is a RATE, so the measurement is the task and the arm is only its conclusion.  Every
# revision of `TASKS.md` is scanned with today's predicate, and every distinct row that fires is
# classified BY HAND here, with its reason.  A row that fires and is in neither table makes this
# tool exit 1 -- `C-0176`'s `--check` discipline applied to a history walk, so the measurement
# cannot silently grow a new unexamined firing.
#
# THE CHEAP CENSUS IS REPORTED BESIDE IT, and it is not the predicate.  *The first verdict is not
# in the last cell* fires on 46 rows of the committed queue where the header rule fires on 21; the
# two agree on 12.  The `P-*` table has four columns and the science table five, so *last cell* is
# the status cell in one of them and the notes cell in the other -- which is the whole reason the
# rule has to read the header.  A contrast measured in BOTH directions is what separates a
# predicate from a heuristic that happened to fire on the instance that prompted it.
#
# WHAT MAKES THE CLASSIFICATION EVIDENCE RATHER THAN TASTE.  Three of the 24 distinct rows that
# have ever fired no longer fire, and all three were repaired in exactly the direction this
# predicate prescribes -- by three different hands, in three different iterations, with no rule
# written down anywhere.  `P-12` folded its acceptance away, `P-20` merged its acceptance into the
# task cell, and `T-276` moved its record into the status cell with the superseded verdict struck.
# The corpus's own practice is the false-positive measurement.
import argparse
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

sys.path.insert(0, HERE)
import queue_verdicts as _verdicts

TRUE_POSITIVE = "TRUE"
FALSE_POSITIVE = "FALSE"

#: The four-column process table is headed `| ID | Task | Status | Notes |`, and from `P-11`
#: onwards the rows are written in the FIVE-column science schema's semantics -- an acceptance in
#: column 3 and the verdict in column 4.  So the row renders its acceptance under *Status* and its
#: verdict under *Notes*.  21 of the table's 31 rows follow the header and 10 do not, so the
#: repair is on the row rather than on the heading; either repair clears the arm.
_SHAPE_P = (
    "the four-column process table is headed `| ID | Task | Status | Notes |` and this row is "
    "written `| ID | Task | Acceptance | Status |`, so its verdict renders under *Notes* and an "
    "acceptance criterion renders under *Status*. 21 of that table's 31 rows follow the header"
)

#: The five-column science table is headed `| ID | Task | Acceptance | Leaf | Status |`, and these
#: rows omit the **Leaf** cell -- so the status record renders under *Leaf* and whatever follows
#: it, usually the preserved `TODO -- **PRIORITY**` note, renders under *Status*.  `T-276` is the
#: same shape with the live verdict and the superseded one exchanged, and there it cost the
#: register a live HIGH row.
_SHAPE_T = (
    "the five-column science table is headed `| ID | Task | Acceptance | Leaf | Status |` and "
    "this row omits its **Leaf** cell, so its status record renders under *Leaf* and the "
    "preserved note behind it renders under *Status*. `T-276` is this shape with the live "
    "verdict and the superseded one exchanged, and there the register read a live HIGH row CLOSED"
)

CLASSIFICATION = {
    identifier: (TRUE_POSITIVE, _SHAPE_P)
    for identifier in (
        "P-11", "P-12", "P-20", "P-21", "P-22", "P-25", "P-26", "P-27", "P-28", "P-29",
        "P-30", "P-31",
    )
}
CLASSIFICATION.update({
    identifier: (TRUE_POSITIVE, _SHAPE_T)
    for identifier in (
        "T-9", "T-263", "T-265", "T-266", "T-267", "T-268", "T-270", "T-271", "T-272",
        "T-274", "T-275", "T-276",
    )
})

#: Rows that fired and no longer do, with how the repair was made.  Nobody was following a rule:
#: there was none.  This table is the measurement that the predicate agrees with the queue's own
#: practice, and a row that stops firing for some OTHER reason belongs here with that reason.
REPAIRED = {
    "P-12": (
        "the acceptance cell was folded away, leaving `| P-12 | Task | **DONE** (iteration 4) | "
        "notes |` -- the verdict under *Status*, which is what the header says",
    ),
    "P-20": (
        "the acceptance was merged into the TASK cell (`**title** -- acceptance`), which moved "
        "the verdict one column left into *Status*",
    ),
    "T-276": (
        "iteration 43, by hand and by a reader rather than by a gate: the record was moved into "
        "the status cell with its superseded verdict struck, and the register went 65 open rows "
        "to 66",
    ),
}


def _cheap_census(text):
    """[identifier] for the cheap bound: the row's FIRST verdict is not in its LAST cell.

    Reported as the CONTRAST and never as the answer.  It counts from the end, and the queue has
    two schemas, so it is wrong in both directions -- 34 rows it flags that the header rule
    clears, and 9 it misses that the header rule finds.
    """
    found = []
    for line in text.splitlines():
        match = _verdicts.TASK_ROW.match(line.strip())
        if not match:
            continue
        cells = _verdicts.blank_struck(match.group(2)).split("|")
        trimmed = cells[:-1] if cells and not cells[-1].strip() else cells
        indices = [i for i, cell in enumerate(trimmed) if _verdicts.cell_verdict(cell)]
        if indices and indices[0] != len(trimmed) - 1:
            found.append(match.group(1))
    return found


def walk(limit=0, ref="HEAD"):
    revisions = subprocess.check_output(
        ["git", "log", "--format=%H", ref, "--", "TASKS.md"], cwd=ROOT, text=True
    ).split()
    if limit:
        revisions = revisions[:limit]
    rows = []
    for sha in revisions:
        text = subprocess.check_output(
            ["git", "show", "%s:TASKS.md" % sha], cwd=ROOT, text=True, errors="replace"
        )
        fires = _verdicts.miscolumned_verdicts(text)
        rows.append({
            "commit": sha[:7],
            "fires": [row[0] for row in fires],
            "headings": sorted({row[3] for row in fires}),
            "cheapCensus": sorted(set(_cheap_census(text))),
        })
    return rows


def summarise(rows, working_tree):
    fired = sorted({i for row in rows for i in row["fires"]})
    instances = sum(len(row["fires"]) for row in rows)
    unclassified = [i for i in fired if i not in CLASSIFICATION]
    false_positives = [i for i in fired if CLASSIFICATION.get(i, (None,))[0] == FALSE_POSITIVE]
    stopped = [i for i in fired if i not in {r[0] for r in working_tree}]
    unexplained = [i for i in stopped if i not in REPAIRED]
    return {
        "revisions": len(rows),
        "revisionsThatFire": sum(1 for row in rows if row["fires"]),
        "rowInstances": instances,
        "distinctRows": fired,
        "truePositives": [i for i in fired if CLASSIFICATION.get(i, (None,))[0] == TRUE_POSITIVE],
        "falsePositives": false_positives,
        "falsePositiveRate": (len(false_positives) / len(fired)) if fired else None,
        "unclassified": unclassified,
        "rowsThatStoppedFiring": stopped,
        "unexplainedRepairs": unexplained,
        "onTheWorkingTree": sorted({r[0] for r in working_tree}),
    }


def contrast(text):
    """The cheap census against the header rule on one queue, in BOTH directions."""
    cheap = set(_cheap_census(text))
    header = {row[0] for row in _verdicts.miscolumned_verdicts(text)}
    return {
        "cheapCensus": len(cheap),
        "headerAware": len(header),
        "agree": sorted(cheap & header),
        "cheapOnlyClearedByTheHeaderRule": sorted(cheap - header),
        "headerOnlyMissedByTheCheapCensus": sorted(header - cheap),
    }


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--limit", type=int, default=0, help="check only the newest N revisions")
    parser.add_argument("--ref", default="HEAD", help="the corpus state to walk (default HEAD)")
    args = parser.parse_args(argv)

    queue = open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8").read()
    rows = walk(args.limit, args.ref)
    working = _verdicts.miscolumned_verdicts(queue)
    summary = summarise(rows, working)
    summary["contrastOnTheWorkingTree"] = contrast(queue)

    if args.json:
        print(json.dumps({"revisions": rows, "summary": summary}, indent=2))
    else:
        print(
            "# %d revision(s) of TASKS.md; %d fire; %d row-instance(s) over %d distinct row(s)"
            % (summary["revisions"], summary["revisionsThatFire"], summary["rowInstances"],
               len(summary["distinctRows"]))
        )
        for identifier in summary["distinctRows"]:
            verdict, reason = CLASSIFICATION.get(identifier, ("UNCLASSIFIED", "—"))
            print("#   %-6s %-5s %s" % (identifier, verdict, reason[:110]))
        for identifier in summary["rowsThatStoppedFiring"]:
            print("#   repaired %-6s %s"
                  % (identifier, REPAIRED.get(identifier, ("UNEXPLAINED",))[0][:110]))
        contrasted = summary["contrastOnTheWorkingTree"]
        print(
            "# cheap census %d row(s), header-aware %d; they agree on %d, the header rule clears "
            "%d of the cheap census's and finds %d it misses"
            % (contrasted["cheapCensus"], contrasted["headerAware"], len(contrasted["agree"]),
               len(contrasted["cheapOnlyClearedByTheHeaderRule"]),
               len(contrasted["headerOnlyMissedByTheCheapCensus"]))
        )
        print(
            "# false positives: %d of %d distinct rows; on the working tree: %d verdict(s)"
            % (len(summary["falsePositives"]), len(summary["distinctRows"]), len(working))
        )

    defects = summary["unclassified"] + summary["unexplainedRepairs"]
    for identifier in defects:
        print("UNCLASSIFIED  %s fires or was repaired and is in neither hand table" % identifier)
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
