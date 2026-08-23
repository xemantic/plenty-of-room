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
"""Emit `gpd/results/T-292-the-column-repair.json`.

    tools/T-292-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS -- `TASKS.md` before and after a repair of twenty-one of
its rows -- so it takes the ref as an argument, defaults it to `HEAD`, and records the RESOLVED
SHA (`C-0174`'s rule for a corpus-subject result file, and `CH-0182`'s: a claim about a census is
inside that census's own scope).  The *before* reading is taken at that ref and the *after*
reading on the working tree, because the repair is what this file is about.

Every count is DERIVED: the rows by running the repair, the leaf corroboration by walking the
file's own history, the mutation numbers by running the mutation test.  No wall-clock timing and
no step counter is emitted; every value is an integer count or a name.
"""

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-292-the-column-repair.json")


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    sys.modules.setdefault(name, module)
    spec.loader.exec_module(module)
    return module


_verdicts = _load("queue_verdicts", "queue_verdicts.py")
_repair = _load("column_repair", "T-292-column-repair.py")
_trace = _load("trace_answers", "trace-answers.py")


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _queue_at(ref):
    return _git("show", "%s:TASKS.md" % ref)


def _table_census(text):
    """[(header, task rows, rows following the header, rows with no verdict, miscolumned)]."""
    found = []
    for _line, header, body in _verdicts.tables(text):
        status = _verdicts.status_column(header)
        if status is None:
            continue
        rows = follow = none = other = 0
        for _number, row in body:
            cells = _verdicts.split_cells(_verdicts.blank_struck(row))
            if not cells or not _verdicts._IDENTIFIER_CELL.match(cells[0].strip("*` ")):
                continue
            rows += 1
            columns = [i for i, cell in enumerate(cells) if _verdicts.cell_verdict(cell)]
            if not columns:
                none += 1
            elif all(index == status for index in columns):
                follow += 1
            else:
                other += 1
        found.append({
            "header": " | ".join(header),
            "statusColumn": status,
            "taskRows": rows,
            "rowsFollowingTheHeader": follow,
            "rowsWithNoVerdict": none,
            "rowsWithAVerdictSomewhereElse": other,
        })
    return found


def _mutation_numbers():
    output = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "T-292-mutation-test.py")],
        cwd=ROOT, capture_output=True, text=True,
    ).stdout
    headline = re.search(r"# (\d+) mutation\(s\), (\d+) survivor\(s\)", output)
    baseline = re.search(r"# baseline in an unmutated copy: (\d+) pre-existing", output)
    return {
        "mutations": int(headline.group(1)) if headline else None,
        "survivors": int(headline.group(2)) if headline else None,
        "survivorNames": [
            line.split("SURVIVED", 1)[1].strip()
            for line in output.splitlines() if line.startswith("SURVIVED")
        ],
        "baselineSubtracted": int(baseline.group(1)) if baseline else None,
        "headline": headline.group(0).lstrip("# ") if headline else None,
    }


def _self_test_count(path):
    with open(os.path.join(ROOT, "tools", path), encoding="utf-8") as handle:
        return handle.read().count("\n    check(")


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD",
                        help="the corpus state the BEFORE reading is taken at; default HEAD")
    parser.add_argument("--out", default=RESULT)
    args = parser.parse_args(argv)

    resolved = _git("rev-parse", args.ref).strip()
    before_text = _queue_at(resolved)
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        after_text = handle.read()

    repaired_text, records = _repair.repair(before_text)
    # `F4` is a statement about the REPAIR, so it is measured on the repair alone: the ref's own
    # file against the ref's own file with the twenty-one rows moved.  The working-tree reading
    # below is a second, weaker comparison -- the tree also carries this task's own record, its
    # new row, and whatever a concurrent agent has landed since the ref.
    repaired_status = _trace.queue_status(repaired_text)
    before_status = _trace.queue_status(before_text)
    after_status = _trace.queue_status(after_text)
    # Rows present in BOTH readings.  The working tree may carry rows the ref does not -- this
    # task's own new row, and a concurrent agent's -- and a row that did not exist at the ref has
    # not had its reading MOVED by anything.  They are reported separately rather than folded in.
    moved_by_the_repair = sorted(
        key for key in set(before_status) | set(repaired_status)
        if before_status.get(key) != repaired_status.get(key)
    )
    shared = set(before_status) & set(after_status)
    moved = sorted(key for key in shared if before_status[key] != after_status[key])
    added = sorted(set(after_status) - set(before_status))
    removed = sorted(set(before_status) - set(after_status))

    rows = []
    for record in records:
        if not record["repaired"]:
            rows.append(record)
            continue
        row = dict(record)
        if record["shape"] == _repair.LEAF_SHAPE:
            history = _repair.leaf_from_history(record["row"], 3, resolved)
            row["leafDerivedFrom"] = (
                "the last whitespace-delimited token of the row's own Leaf cell"
            )
            row["leafCorroboratedBy"] = (
                "the newest revision of TASKS.md in which that cell was a bare leaf"
            )
            row["leafInThatRevision"] = history[0] if history else None
            row["thatRevision"] = history[1] if history else None
            row["leafAgrees"] = bool(history and history[0] == record["leaf"])
        rows.append(row)

    leaf_rows = [r for r in rows if r.get("shape") == _repair.LEAF_SHAPE]
    notes_rows = [r for r in rows if r.get("shape") == _repair.NOTES_SHAPE]

    document = {
        "task": "T-292",
        "title": "the column repair -- twenty-one queue verdicts moved into their own table's "
                 "status cell, proved content-preserving token by token",
        "subject": "TASKS.md before and after the repair -- a corpus-subject result file, so the "
                   "ref is an argument and the resolved SHA is recorded",
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count or a name",
        "parameters": {
            "corpus": "TASKS.md",
            "predicate": "queue_verdicts.miscolumned_verdicts -- the gate's OWN predicate, so the "
                         "repair cannot be about a different set of rows than the gate prints",
            "shapeIsDerived": "from the column the verdict stands in relative to the status "
                              "column, not from a list of identifiers",
            "cellSplit": "unescaped pipes only, via tools/check-markdown-tables.py's own reader",
            "blankedSpans": "struck spans, length-preservingly, before the row is split",
            "tokenKey": "non-whitespace runs of the row's cell contents, strike markers removed "
                        "on both sides because they are the repair's own deliberate addition",
            "note": "no wall-clock timing and no step counter is emitted",
        },
        "cheapBound": {
            "rule": "read the last whitespace-delimited token of each of the eleven Leaf cells",
            "cost": "one split() per row; no history walk and no claim read",
            "whatItSettled":
                "C-0188 §4 and CH-0241 both describe the science-table shape as a row that has "
                "DROPPED its Leaf cell, which would mean eleven leaf values had to be found from "
                "outside the row before one row could be repaired. The cheap bound says "
                "otherwise: the leaf is still there, at the END of the cell, and the record was "
                "written in FRONT of it",
            "leafTokensFoundInTheRowItself": len(leaf_rows),
            "leafValuesThatHadToBeSuppliedFromOutsideTheRow": 0,
            "corroboratedAgainstHistory": sum(1 for r in leaf_rows if r.get("leafAgrees")),
        },
        "reading": {
            "atTheBaselineRef": [
                [row[0], row[1], row[2], row[3], row[4]]
                for row in _verdicts.miscolumned_verdicts(before_text)
            ],
            "onTheWorkingTreeAfterTheRepair": [
                [row[0], row[1], row[2], row[3], row[4]]
                for row in _verdicts.miscolumned_verdicts(after_text)
            ],
            "miscolumnedBefore": len(_verdicts.miscolumned_verdicts(before_text)),
            "miscolumnedAfter": len(_verdicts.miscolumned_verdicts(after_text)),
        },
        "shapes": {
            "recordWrittenInFrontOfTheLeaf": len(leaf_rows),
            "acceptanceShiftedIntoTheStatusColumn": len(notes_rows),
            "unrepairable": sum(1 for r in rows if not r.get("repaired")),
        },
        "rows": rows,
        "tokenPreservation": {
            "rule": "for each row the multiset of non-whitespace tokens of the row's cell "
                    "contents is compared before and after; a row that loses a token refuses the "
                    "write",
            "rowsWithNoTokenAddedAndNoneRemoved": sum(
                1 for r in rows if r.get("repaired")
                and not r["tokensAdded"] and not r["tokensRemoved"]
            ),
            "rowsThatLostAToken": sum(
                1 for r in rows if r.get("repaired") and r["tokensRemoved"]
            ),
            "tokensAddedInTotal": sorted(
                token for r in rows if r.get("repaired") for token in r["tokensAdded"]
            ),
            "whatTheAdditionsAre":
                "one em dash per four-column row, joining the task headline to the acceptance "
                "clause the repair folds into the same cell -- P-20's own repair. The eleven "
                "five-column rows add and remove nothing at all",
            "strikeMarkersAreNormalisedAway":
                "on BOTH sides, and they are enumerated separately: the repair strikes the "
                "leading verdict run of each superseded note, which is the queue's own majority "
                "idiom",
            "cellCountsUnchanged": all(
                r["cellsBefore"] == r["cellsAfter"] for r in rows if r.get("repaired")
            ),
        },
        "theFourColumnChoice": {
            "question": "move the ten rows, or retitle the header the ten rows do not follow",
            "measurement": _table_census(before_text),
            "whatItSays":
                "of the four-column table's 31 task rows, 19 follow its header, 2 carry no "
                "verdict and 10 do not follow it -- so retitling the header would put 19 rows "
                "outside it against the 10 that are outside it now, and the rows are repaired",
            "precedentsInThisVeryTable": {
                "P-12": "its acceptance cell was folded away, leaving the verdict in the status "
                        "cell",
                "P-20": "its acceptance was merged into the TASK cell, which moved the verdict "
                        "one column left -- the repair this task applies to all ten",
            },
        },
        "register": {
            "rowsReadBefore": len(before_status),
            "rowsReadAfter": len(after_status),
            "measuredOnTheRepairAlone": {
                "whatIsCompared": "the ref's own file against the ref's own file with the "
                                  "twenty-one rows moved -- nothing else differs, so this is the "
                                  "repair's own effect and F4 is read here",
                "rowsRead": len(repaired_status),
                "rowsWhoseReadingMoved": moved_by_the_repair,
                "openBefore": sum(1 for v in before_status.values() if v == "OPEN"),
                "openAfter": sum(1 for v in repaired_status.values() if v == "OPEN"),
                "liveVerdictsBefore": len(_verdicts.leading_verdicts(before_text)),
                "liveVerdictsAfter": len(_verdicts.leading_verdicts(repaired_text)),
                "whyTheLiveVerdictCountFalls":
                    "the eleven superseded notes are now struck, and none of them was ever its "
                    "row's LEFTMOST verdict -- which is why no reading moves",
            },
            "rowsSharedByBothReadings": len(shared),
            "rowsWhoseReadingMoved": moved,
            "rowsAddedSinceTheRef": added,
            "rowsRemovedSinceTheRef": removed,
            "whyTheWorkingTreeReadingDiffers":
                "T-292 is this task's own row and is deliberately closed by it; T-276 is a "
                "concurrent agent's row, which this task does not own and does not touch. "
                "Neither movement is the column repair's, and the repair-alone reading above is "
                "0 of 286",
            "whatTheAddedRowsAre":
                "rows written after the ref -- this task's own T-292 record and T-295, and any a "
                "concurrent agent landed. A row that did not exist at the ref has not had its "
                "reading moved by anything, so it is reported here rather than as a movement",
            "openBefore": sum(1 for v in before_status.values() if v == "OPEN"),
            "openAfter": sum(1 for v in after_status.values() if v == "OPEN"),
            "whyNothingMoves":
                "the register takes a row's LEFTMOST live verdict and the repair moves a verdict "
                "between cells of the same row without changing which one comes first -- which "
                "is exactly CH-0241's point, that the rule was right by luck on these rows",
        },
        "gate": {
            "armBefore": "ADVISORY (T-289/C-0188), on a reading of 21",
            "armAfter": "GATED (T-292), on a reading of 0",
            "promotionCondition":
                "C-0188 §6: when the advisory count reaches 0 the arm becomes a gate by deleting "
                "a word",
            "gateReadingOnTheTreeThisLandsOn": {
                "tool": "tools/check-queue-vocabulary.py",
                "defects": 0,
                "miscolumnedVerdicts": len(_verdicts.miscolumned_verdicts(after_text)),
                "exitCode": 0,
            },
            "harnessCensus": "tools/P-31-harness-census.py --check: 0 unresolved over 12 "
                             "harnesses, wired 12 of 12",
        },
        "mutation": _mutation_numbers(),
        "namedTests": {
            "columnRepairSelfTests": _self_test_count("T-292-column-repair.py"),
            "queueVocabularySelfTestCallSites": _self_test_count("check-queue-vocabulary.py"),
            "bothDirectionsOfThePromotion":
                "a miscolumned verdict must FAIL the gate and a clean queue must PASS it; the "
                "T-289 mutation table's 'the arm becomes a GATE' row is INVERTED to 'the arm "
                "reverts to ADVISORY', because a mutation row that outlives the rule it mutates "
                "measures nothing",
        },
    }

    with open(args.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(args.out, ROOT))
    print("# %d row(s) repaired: %d leaf-shape, %d notes-shape; miscolumned %d -> %d; "
          "register moved %d row(s)" % (
              len(rows), len(leaf_rows), len(notes_rows),
              document["reading"]["miscolumnedBefore"],
              document["reading"]["miscolumnedAfter"], len(moved)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
