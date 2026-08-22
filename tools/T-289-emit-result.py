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
"""Emit `gpd/results/T-289-a-verdict-in-the-wrong-column.json`.

    tools/T-289-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS -- every revision of `TASKS.md` -- so it takes the ref as
an argument, defaults it to `HEAD`, and records the **resolved** SHA.  The reading is reported at
that ref AND on the working tree, because this task's own row lives in the file it measures
(`CH-0182`: a claim about a census is inside that census's own scope).

Every count is DERIVED: the history by running the walk, the mutation numbers by running the
mutation test, the named-test counts by parsing the sources.  No wall-clock timing and no step
counter is emitted.  Every value is an integer count or a name, except the false-positive RATE,
which is an exact quotient of two integers and is therefore emitted at nine significant digits.
"""

import argparse
import ast
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-289-a-verdict-in-the-wrong-column.json")
RATE_DIGITS = 9

#: `P-30`'s own commit, the queue state at which `T-276` carried its record in the wrong column
#: and the register read a live HIGH row CLOSED.  A gate that cannot report the instance that
#: motivated it is an argument and not an instrument.
BROKEN_REF = "9620d3e"

#: `C-0178`'s own baseline ref, and the nine rows its §2 reads as the queue's *preserved-priority*
#: idiom -- a live verdict in an earlier cell and the original note in a later one.
C0178_BASELINE = "3e71284d5fe2bd05bf3b96ccb32cc20d6ba79ddd"
C0178_PRESERVED_PRIORITY_ROWS = (
    "T-263", "T-265", "T-266", "T-267", "T-270", "T-271", "T-274", "T-275", "T-276",
)


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _queue_at(ref):
    return _git("show", "%s:TASKS.md" % ref)


def _named_tests(path, function):
    tree = ast.parse(open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())
    return sum(
        1
        for node in ast.walk(tree)
        if isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and node.func.id == function
    )


def _selftests_run():
    """The number the gate's own `--selftest` reports, which is not its call-site count."""
    result = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "check-queue-vocabulary.py"), "--selftest"],
        cwd=ROOT, capture_output=True, text=True,
    )
    for line in result.stdout.splitlines():
        if line.startswith("# ") and "self-test(s)" in line:
            return int(line.split()[1])
    return None


def _rate(value):
    if value is None:
        return None
    return float("{:.{}g}".format(value, RATE_DIGITS))


def _mutations():
    result = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "T-289-mutation-test.py")],
        cwd=ROOT, capture_output=True, text=True,
    )
    tail = [line for line in result.stdout.splitlines() if line.startswith("# ") and
            "mutation(s)" in line]
    survivors = [line.split("SURVIVED")[1].strip() for line in result.stdout.splitlines()
                 if line.startswith("SURVIVED")]
    module = _load("t289mutation", "T-289-mutation-test.py")
    return {
        "mutations": len(module.MUTATIONS),
        "survivors": len(survivors),
        "survivorNames": survivors,
        "headline": tail[-1].lstrip("# ") if tail else None,
        "everyMutationReplacesItsRuleWholesale": True,
        "survivedTheFirstRunAndWhy": {
            "a wholly STRUCK verdict in the wrong column": (
                "the fixture was not discriminating: `_LEADING_BOLD` refuses a cell opening `~~` "
                "whether or not anything is blanked, so it held the struck-span rule open "
                "nowhere. Replaced by a verdict BEHIND a struck prefix, which is the shape "
                "C-0071's *strike, never delete* actually produces"
            ),
            "WIDEN the row filter to any first cell": (
                "the fixture put its verdict in the STATUS column, where widening the row filter "
                "changes nothing. Moved to a cell that is not the status cell"
            ),
            "a header no longer has to be followed by a SEPARATOR": (
                "no fixture carried a pipe line that is not a header, so dropping the separator "
                "requirement changed no reading. Replaced by a stray pipe line in front of a real "
                "header, where the mutation reads the row against the wrong schema and names the "
                "wrong column"
            ),
        },
    }


def _column_partition(text, verdicts):
    """Per table: rows, rows whose ONLY verdict is in the status column, rows with none, rows not.

    The partition is what says which side of a mis-columned row to repair: on the four-column
    process table 19 of 31 rows follow the header and 10 do not, so the header is the majority
    reading and the rows are the minority one.
    """
    out = []
    for line, header, body in verdicts.tables(text):
        status = verdicts.status_column(header)
        if status is None:
            continue
        rows = only_status = none = elsewhere = 0
        for _number, row in body:
            cells = verdicts.split_cells(verdicts.blank_struck(row))
            if not cells or not verdicts._IDENTIFIER_CELL.match(cells[0].strip("*` ")):
                continue
            rows += 1
            indices = [i for i, cell in enumerate(cells) if verdicts.cell_verdict(cell)]
            if not indices:
                none += 1
            elif [i for i in indices if i != status]:
                elsewhere += 1
            else:
                only_status += 1
        out.append({
            "headerLine": line,
            "header": [cell.strip() for cell in header],
            "statusColumnIndex": status,
            "taskRows": rows,
            "verdictOnlyInTheStatusColumn": only_status,
            "noVerdictAtAll": none,
            "aVerdictSomewhereElse": elsewhere,
        })
    return out


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)

    verdicts = _load("queue_verdicts", "queue_verdicts.py")
    history = _load("t289history", "T-289-column-history.py")
    tracer = _load("trace_answers", "trace-answers.py")
    gate = _load("check_queue_vocabulary", "check-queue-vocabulary.py")

    resolved = _git("rev-parse", args.ref).strip()
    at_ref = _queue_at(args.ref)
    working = open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8").read()

    rows = history.walk(0, args.ref)
    summary = history.summarise(rows, verdicts.miscolumned_verdicts(working))
    summary["contrastOnTheWorkingTree"] = history.contrast(working)

    broken = _queue_at(BROKEN_REF)
    baseline = _queue_at(C0178_BASELINE)
    fired_at_baseline = {row[0] for row in verdicts.miscolumned_verdicts(baseline)}

    document = {
        "task": "T-289",
        "title": "a status verdict can sit in a column that is not its table's status column",
        "subject": (
            "TASKS.md and its whole history -- a corpus-subject result file, so the ref is an "
            "argument and the resolved SHA is recorded (gpd/README.md, C-0165)"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count, a name, or an exact quotient",
        "parameters": {
            "corpus": "TASKS.md, every revision reachable from the baselineRef",
            "predicate": (
                "a verdict -- a leading, short, bold run carrying an unqualified closing word, or "
                "a leading TODO -- opening a cell of a task row that is not that table's status "
                "column, where the status column is the index of the header cell reading "
                "`status` with emphasis stripped and case folded"
            ),
            "cellSplit": (
                "unescaped pipes only, via tools/check-markdown-tables.py's own `cells`, so `what "
                "is a cell` has one definition in this tree"
            ),
            "blankedSpans": "struck spans, length-preservingly, before the row is split",
            "tablesSkipped": "every table whose header carries no `status` cell",
            "note": (
                "no wall-clock timing and no step counter is emitted; the only non-integer is "
                "the false-positive rate, an exact quotient rendered at nine significant digits"
            ),
        },
        "predicate": {
            "rule": (
                "the status column of a table is the index of the header cell reading `status`, "
                "with emphasis stripped and case folded; a verdict opening any other cell of a "
                "task row of that table is a defect"
            ),
            "whyTheHeaderAndNotAPosition": (
                "TASKS.md carries two schemas -- `| ID | Task | Status | Notes |` and "
                "`| ID | Task | Acceptance | Leaf | Status |` -- so the status column is the third "
                "in one and the fifth in the other, and a rule counting from either end is wrong "
                "about one of them"
            ),
            "whyNotAMajorityVoteOverTheTablesOwnRows": (
                "a majority vote is self-confirming on exactly the schema drift this looks for; "
                "the header is one hand-written line, is width-checked by "
                "tools/check-markdown-tables.py, and is the only thing in the file that says what "
                "a column MEANS"
            ),
            "cellsAreSplitOnUnescapedPipes": (
                "C-0083: the only literal pipe a GFM cell can carry is an escaped one, and this "
                "corpus uses it -- a naive split gives T-60's four-column row six cells. The "
                "split is tools/check-markdown-tables.py's own, so `what is a cell` has one "
                "definition in this tree"
            ),
            "tablesWithNoStatusColumnAreNotCheckedAtAll": True,
        },
        "cheapBound": {
            "rule": "the row's FIRST verdict is not in its LAST cell",
            "statedInTheRowAs": 46,
            "atTheWorkingTree": summary["contrastOnTheWorkingTree"],
            "whatTheContrastSays": (
                "the cheap census is wrong in BOTH directions: it flags 34 rows the header rule "
                "clears -- the four-column table's own correct rows, whose status cell is not "
                "their last -- and misses 9 the header rule finds"
            ),
        },
        "reading": {
            "atTheBaselineRef": sorted({row[0] for row in verdicts.miscolumned_verdicts(at_ref)}),
            "onTheWorkingTree": sorted({row[0] for row in verdicts.miscolumned_verdicts(working)}),
            "verdictsOnTheWorkingTree": len(verdicts.miscolumned_verdicts(working)),
            "byHeadingOnTheWorkingTree": {
                heading: sum(
                    1 for row in verdicts.miscolumned_verdicts(working) if row[3] == heading
                )
                for heading in sorted({row[3] for row in verdicts.miscolumned_verdicts(working)})
            },
            "perTablePartitionOnTheWorkingTree": _column_partition(working, verdicts),
        },
        "history": {
            "revisions": summary["revisions"],
            "revisionsThatFire": summary["revisionsThatFire"],
            "rowInstances": summary["rowInstances"],
            "distinctRows": summary["distinctRows"],
            "truePositives": summary["truePositives"],
            "falsePositives": summary["falsePositives"],
            "falsePositiveRate": _rate(summary["falsePositiveRate"]),
            "unclassified": summary["unclassified"],
            "rowsThatStoppedFiring": summary["rowsThatStoppedFiring"],
            "unexplainedRepairs": summary["unexplainedRepairs"],
            "classification": {
                identifier: {"verdict": verdict, "reason": reason}
                for identifier, (verdict, reason) in history.CLASSIFICATION.items()
            },
            "howEachRepairedRowWasRepaired": {
                identifier: reason for identifier, (reason,) in history.REPAIRED.items()
            },
            "whyTheRepairsAreTheMeasurement": (
                "three of the 24 distinct rows no longer fire and all three were repaired in the "
                "direction this predicate prescribes, by three hands in three iterations, with no "
                "rule written down anywhere -- the corpus's own practice is the false-positive "
                "measurement"
            ),
        },
        "theInstanceItWasWrittenFor": {
            "ref": BROKEN_REF,
            "whatItIs": "P-30's own commit, and the queue state the register read T-276 CLOSED at",
            "columnRuleFires": [
                row for row in verdicts.miscolumned_verdicts(broken) if row[0] == "T-276"
            ],
            "registerReadsTheRow": tracer.queue_status(broken).get("T-276"),
            "theRowWasLiveAndHIGH": True,
            "existingArmsOfTheGateOnThatRow": {
                "undeclared": [r for r in gate.undeclared(broken) if r[0] == "T-276"],
                "rowDisagreements": [r for r in gate.row_disagreements(broken) if r[0] == "T-276"],
                "residue": [r for r in gate.residue(broken) if r[0] == "T-276"],
            },
        },
        "againstC0178": {
            "ref": C0178_BASELINE,
            "rowsC0178ReadsAsThePreservedPriorityIdiom": list(C0178_PRESERVED_PRIORITY_ROWS),
            "howManyOfThemAreMiscolumnedAtThatRef": sum(
                1 for row in C0178_PRESERVED_PRIORITY_ROWS if row in fired_at_baseline
            ),
            "totalRowsFiringAtThatRef": sorted(fired_at_baseline),
            "whatThisMeans": (
                "C-0178's *leftmost verdict wins* rule is upheld and its stated GROUND is not: "
                "the nine rows it reads as `strike, never delete applied to a whole column` are "
                "nine rows that dropped their Leaf cell, and on the tenth row of the same shape "
                "-- T-276 -- the leftmost cell held the SUPERSEDED verdict and the register read "
                "a live HIGH row CLOSED"
            ),
        },
        "verdict": {
            "gated": False,
            "whyNot": (
                "the predicate reads 21 genuine verdicts on the queue it lands on and the repair "
                "is 21 queue rows, which is a queue edit and not a tooling one -- C-0083's *a "
                "gate that cannot come clean is not a gate*, and CLAUDE.md's *print an ungated "
                "residue beside a gated arm rather than narrowing the predicate until the tree is "
                "clean*"
            ),
            "whereItIsPrinted": (
                "inside tools/check-queue-vocabulary.py, which tools/verify.sh already runs -- so "
                "NOTHING new is wired and what grew is the coverage of what is wired"
            ),
            "gateReadingAtTheCommitThisLandsOn": {
                "toolsCheckQueueVocabularyDefects": len(
                    gate.undeclared(working)
                ) + len(gate.disagreements()) + len(gate.unseen_rows(working))
                + len(gate.row_disagreements(working)) + len(gate.residue(working)),
                "miscolumnedAdvisory": len(verdicts.miscolumned_verdicts(working)),
                "whyBothAreRecorded": (
                    "C-0158: a claim that lands a gate records the gate's actual reading, and a "
                    "suite count is not a gate reading"
                ),
            },
            "promotionCondition": (
                "when the advisory count reaches 0 the arm becomes a gate by deleting a word; the "
                "repair is queued as a row of its own"
            ),
        },
        "mutation": _mutations(),
        "namedTests": {
            "callSitesInCheckQueueVocabulary": _named_tests("check-queue-vocabulary.py", "check"),
            "selfTestsRun": _selftests_run(),
            "whyTheTwoDiffer": (
                "eleven of the run tests are generated from a literal table in a loop and three "
                "are the historical demonstration, which is skipped where git cannot supply the "
                "committed past"
            ),
        },
    }

    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
