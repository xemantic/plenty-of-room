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
# P-30 -- emit the measurement behind `C-0178`.
#
#     tools/P-30-emit-result.py [--baseline <ref>] [--out gpd/results/P-30-...json]
#
# `CLAUDE.md`: *a result file whose subject is the CORPUS must name the corpus state it measured,
# or it can never be re-run.*  The "before" half of every number here is read out of a named git
# ref (default `HEAD`) and the resolved SHA is recorded; the "after" half is the working tree.
import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")
sys.path.insert(0, TOOLS)
import queue_verdicts as verdicts  # noqa: E402

_trace = __import__("trace-answers")

# The reader's row pattern BEFORE `P-30`: it required a trailing pipe and no emphasis.
BASELINE_ROW = re.compile(r"^\|\s*(T-\d{1,4}[a-z]?|P-\d{1,4})\s*\|(.*)\|\s*$")


def _blob(ref, path):
    return subprocess.check_output(["git", "show", "%s:%s" % (ref, path)], cwd=ROOT, text=True)


def _load(name, source):
    handle, path = tempfile.mkstemp(suffix=".py")
    os.write(handle, source.encode("utf-8"))
    os.close(handle)
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    os.unlink(path)
    return module


def _baseline_status(queue_text):
    """`queue_status` as it read before `P-30`: whole-row scan, trailing pipe required."""
    statuses = {}
    for line in queue_text.splitlines():
        match = BASELINE_ROW.match(line.strip())
        if not match:
            continue
        identifier, rest = match.group(1), match.group(2)
        if re.search(r"\bIN PROGRESS\b", rest):
            statuses[identifier] = "IN PROGRESS"
        elif verdicts.UNQUALIFIED_CLOSING_WORD.search(rest):
            statuses[identifier] = "CLOSED"
        else:
            statuses[identifier] = "OPEN"
    return statuses


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--baseline", default="HEAD")
    parser.add_argument(
        "--out", default=os.path.join(ROOT, "gpd", "results", "P-30-queue-row-coverage.json")
    )
    args = parser.parse_args(argv)

    baseline_sha = subprocess.check_output(
        ["git", "rev-parse", args.baseline], cwd=ROOT, text=True
    ).strip()

    queue_now = open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8").read()
    queue_before = _blob(baseline_sha, "TASKS.md")
    reader_before = _load("trace_answers_baseline", _blob(baseline_sha, "tools/trace-answers.py"))

    now = _trace.queue_status(queue_now)
    before = reader_before.queue_status(queue_before)

    # (1) THE READER EFFECT, isolated: both readers on the SAME queue, the baseline one.  This is
    # the number that is re-derivable from `baselineRef` alone -- it contains none of this task's
    # own document repairs and none of any concurrent agent's edits to the working tree.
    moved = {}
    reader_on_baseline = _trace.queue_status(queue_before)
    for identifier in sorted(set(before) | set(reader_on_baseline)):
        if before.get(identifier, "UNSEEN") != reader_on_baseline.get(identifier, "UNSEEN"):
            moved[identifier] = {
                "before": before.get(identifier, "UNSEEN"),
                "after": reader_on_baseline.get(identifier, "UNSEEN"),
            }

    # (2) which half of the repair carries which row: the document edit, or the reader change.
    # The baseline READER applied to the CURRENT queue isolates the document half.
    prose_only = _baseline_status(queue_now)
    carried_by = {}
    for identifier in sorted(moved):
        if identifier not in now:
            carried_by[identifier] = "the row no longer exists in the working tree"
        elif prose_only.get(identifier, "UNSEEN") == now[identifier]:
            carried_by[identifier] = "the document repair alone"
        else:
            carried_by[identifier] = "the reader change"

    # (3) the coverage defect over the queue's own history
    revisions = subprocess.check_output(
        ["git", "log", "--format=%H", "--", "TASKS.md"], cwd=ROOT, text=True
    ).split()
    hidden_revisions = 0
    hidden_open_revisions = 0
    hidden_instances = 0
    hidden_identifiers = set()
    hidden_open_identifiers = set()
    for sha in revisions:
        text = _blob(sha, "TASKS.md")
        seen = set(_baseline_status(text))
        misses = [(i, b) for i, b in verdicts.task_rows(text) if i not in seen]
        if not misses:
            continue
        hidden_revisions += 1
        hidden_instances += len(misses)
        opens = []
        for identifier, body in misses:
            hidden_identifiers.add(identifier)
            row = verdicts.row_verdicts(body)
            if row and row[0][1] == verdicts.OPEN:
                opens.append(identifier)
                hidden_open_identifiers.add(identifier)
        if opens:
            hidden_open_revisions += 1

    # (4) the deliverables' own status check, before and after
    contained = {}
    for document in ("ANSWERS.md", "DECISIONS-FOR-NDI.md"):
        text = open(os.path.join(ROOT, document), encoding="utf-8").read()
        contained[document] = {
            "staleStatusesBefore": len(reader_before.stale_statuses(text, queue_before)),
            "staleStatusesAfter": len(_trace.stale_statuses(text, queue_now)),
        }

    result = {
        "task": "P-30",
        "claim": "C-0178",
        "subject": "the queue register reads a row's LEADING verdict, and it sees every row",
        "baselineRef": baseline_sha,
        "parameters": {
            "queue": "TASKS.md",
            "reader": "tools/trace-answers.py",
            "gate": "tools/check-queue-vocabulary.py",
            "sharedPredicate": "tools/queue_verdicts.py",
        },
        "coverage": {
            "taskRowsInTheFile": len(verdicts.task_rows(queue_now)),
            "rowsSeenBefore": len(before),
            "rowsSeenAfter": len(now),
        },
        "readingsThatMoved": moved,
        "workingTreeReadings": {
            identifier: now.get(identifier, "UNSEEN") for identifier in sorted(moved)
        },
        "whatCarriesEachRow": carried_by,
        "whatCarriesEachRowNote": (
            "measured against the WORKING TREE at emit time, which on a shared checkout may "
            "carry another agent's edits to the same file: `T-280` was rewritten to **DONE** by "
            "a sibling in this same iteration, so its row is closed for a reason that is not "
            "this task's repair. The reader-effect figures above contain no working tree at all."
        ),
        "openRowCount": {
            "baselineQueueBaselineReader": sum(1 for v in before.values() if v == "OPEN"),
            "baselineQueueRepairedReader": sum(
                1 for v in reader_on_baseline.values() if v == "OPEN"
            ),
            "workingTreeRepairedReader": sum(1 for v in now.values() if v == "OPEN"),
        },
        "coverageDefectOverHistory": {
            "revisionsOfTheQueue": len(revisions),
            "revisionsWithAnInvisibleRow": hidden_revisions,
            "rowInstancesInvisible": hidden_instances,
            "distinctRowsInvisible": sorted(hidden_identifiers),
            "revisionsWithAnInvisibleOPENRow": hidden_open_revisions,
            "distinctOPENRowsInvisible": sorted(hidden_open_identifiers),
        },
        "containment": contained,
        "residue": {
            "note": "reported by the gate and deliberately NOT gated; see `residue()`",
            "rows": [
                {"task": i, "verdict": p, "wholeRowScanReads": s}
                for i, p, s in __import__("check-queue-vocabulary").residue(queue_now)
            ],
        },
    }
    with open(args.out, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(args.out, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
