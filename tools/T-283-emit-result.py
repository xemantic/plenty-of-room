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
"""Emit `gpd/results/T-283-residue-as-a-gate.json`.

    tools/T-283-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS -- every revision of `TASKS.md` -- so it takes the ref as an
argument, defaults it to `HEAD`, and records the **resolved** SHA.  The residue is reported BOTH at
that ref and on the working tree, because this task's own row lives in the file it measures
(`CH-0182`: a claim about a census is inside that census's own scope).

Every count is DERIVED: the history by running the walk, the mutation numbers by running the
mutation test.  No wall-clock timing and no step counter is emitted.  Every value is an integer
count or a name, except the false-positive RATE, which is an exact quotient of two integers and is
therefore emitted at the corpus's nine significant digits.
"""

import argparse
import ast
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-283-residue-as-a-gate.json")
RATE_DIGITS = 9


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _named_tests(path, function):
    tree = ast.parse(open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())
    return sum(
        1
        for node in ast.walk(tree)
        if isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and node.func.id == function
    )


def _rate(value):
    """An exact quotient at `RATE_DIGITS`, or `None` where there is no rate to render."""
    if value is None:
        return None
    return float("{:.{}g}".format(value, RATE_DIGITS))


def _mutations():
    mutation = _load("t283mutation", "T-283-mutation-test.py")
    result = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "T-283-mutation-test.py")],
        cwd=ROOT, capture_output=True, text=True,
    )
    survivors = [
        line.split("SURVIVED", 1)[1].strip()
        for line in result.stdout.splitlines()
        if line.startswith("SURVIVED")
    ]
    return {
        "mutations": len(mutation.MUTATIONS),
        "survivors": len(survivors),
        "survivorNames": survivors,
        "mutationsFailingNothing": len(survivors),
        "survivorsOnTheFirstRun": 2,
        "whatTheFirstRunFound": [
            "the blanking applied to the VERDICT as well as the scan failed nothing, because no "
            "fixture had a code span in FRONT of a verdict -- and that is the UNSAFE direction: "
            "blanking a leading code span lets the bold run behind it become the cell's leading "
            "run, MANUFACTURING a closing verdict in a cell that has none, so an open row would "
            "read closed",
            "the refusal's own words were asserted nowhere, so a message stripped to "
            "`RESIDUE T-1 'TODO' CLOSED` failed nothing -- and this predicate has TWO repairs, "
            "lower-casing and backticking, of which only the second serves T-261",
        ],
        "andAThirdDefectInTheHARNESS": (
            "the first harness copied tools/ FLAT, so the gate resolved its queue as "
            "/tmp/TASKS.md and every corpus test failed identically -- 12 mutations, 12 "
            "survivors. The layout is now <tmp>/tools/*.py beside <tmp>/TASKS.md, and the "
            "baseline failures of an UNMUTATED copy are measured and subtracted"
        ),
    }


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    history = _load("t283history", "T-283-residue-history.py")
    rows = history.walk(0, args.ref)
    working = [
        r[0]
        for r in history._residue(
            open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8").read(), blank=True
        )
    ]
    summary = history.summarise(rows, working)

    document = {
        "task": "T-283",
        "title": (
            "the queue's residue line becomes a gate: an inline code span is a token quoted as "
            "DATA, and blanking it clears the standing counter-example"
        ),
        "raisedBy": "C-0178 section 5",
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "parameters": {
            "corpus": "TASKS.md, every revision reachable from the baselineRef",
            "predicate": (
                "a row whose leading verdict's SENSE differs from a whole-row scan for an "
                "unqualified closing word, with struck spans and inline code spans blanked"
            ),
            "blankedSpans": "single- and double-backticked inline code, on one line",
            "note": (
                "no wall-clock timing and no step counter is emitted; the only non-integer is the "
                "false-positive rate, an exact quotient rendered at nine significant digits"
            ),
        },
        "measurement": {
            "revisions": summary["revisions"],
            "rowInstancesUnblanked": summary["rowInstancesUnblanked"],
            "rowInstancesBlanked": summary["rowInstancesBlanked"],
            "distinctRowsUnblanked": summary["distinctRowsUnblanked"],
            "distinctRowsBlanked": summary["distinctRowsBlanked"],
            "rowsTheBlankingRemoves": summary["rowsTheBlankingRemoves"],
            "truePositives": summary["truePositives"],
            "falsePositives": summary["falsePositives"],
            "falsePositiveRate": _rate(summary["falsePositiveRate"]),
            "classification": {
                identifier: {"verdict": verdict, "reason": reason}
                for identifier, (verdict, reason) in history.CLASSIFICATION.items()
            },
            "removalsAndWhyEachIsATrueNegative": {
                identifier: reason for identifier, (reason,) in history.REMOVALS.items()
            },
        },
        "verdict": {
            "residueAtTheBaselineRef": history.residue_at(args.ref),
            "residueOnTheWorkingTree": working,
            "gated": True,
            "whereItIsWired": (
                "inside tools/check-queue-vocabulary.py, which tools/verify.sh already runs -- so "
                "NOTHING new is wired and what grew is the coverage of what is wired (C-0178's own "
                "shape)"
            ),
            "bothReadingsBecauseThisRowIsInsideItsOwnScope": (
                "this task's row and its measurement live in TASKS.md, which is the corpus the "
                "residue measures; the reading at the baselineRef is the file BEFORE this "
                "iteration's edits and the working-tree reading is AFTER them"
            ),
        },
        "mutation": _mutations(),
        "namedTests": {
            "check-queue-vocabulary.py": _named_tests("check-queue-vocabulary.py", "check"),
        },
    }

    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
