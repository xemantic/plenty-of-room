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
# T-298 -- MUTATION COVERAGE FOR THE THREE CHANGES THIS TASK MAKES TO `tools/trace-answers.py`.
#
#     tools/T-298-mutation-test.py
#
# `C-0176`'s standard, in BOTH directions: every rule must fail a NAMED test when it is reverted,
# AND when it is over-widened.  Six of the ten rows below revert a rule and four over-correct it;
# a table that only ever narrows becomes a pattern, which is what a per-rule judgement refuses.
#
# `C-0185`/`CH-0237`'s baseline check runs first and its named failures are subtracted: without it
# a fixture defect reads as `0 survivors` (the quiet direction) or as `12 of 12` (the loud one),
# and the headline column means nothing either way.  The anchor count is asserted at 1 per
# mutation, which is `C-0185`'s other half -- a harness that does not assert its anchors reports
# `killed` off a mutation that never applied.
#
# NOT YET DECLARED IN `tools/P-31-harness-census.py`'s `HARNESSES` TABLE, and `P-31 --check` will
# say so.  That is the census working as designed (*"a harness somebody writes tomorrow and does
# not declare fails the gate rather than being invisible to it"*), and the row it wants is:
#
#     ("T-298-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
#      ("trace-answers.py",)),
#
# `T-298` does not own `tools/P-31-*`, so the row is reported to the coordinator rather than
# written here.  Note that `P-31 --check` is ALREADY red at `HEAD` on an unrelated ORPHAN in
# `tools/T-281-mutation-test.py`, verified with `git show HEAD:`.
"""Mutation coverage for T-298's changes to tools/trace-answers.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "trace-answers.py"
TESTS = "test-trace-answers.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject, or the row is a
#: reference into a source that has been refactored underneath it and reports nothing.
MUTATIONS = (
    (
        "the status reader ignores strikes",
        SUBJECT,
        "cell = strip_struck(match.group(1))",
        "cell = match.group(1)",
    ),
    (
        "the adjudication reader ignores strikes",
        SUBJECT,
        "_ADJUDICATED.search(strip_struck(match.group(1)))",
        "_ADJUDICATED.search(match.group(1))",
    ),
    (
        "the status reader blanks the whole file instead of the cell",
        SUBJECT,
        '    match = _CHALLENGE_STATUS_ROW.search(text)\n'
        '    if not match:\n        return "UNKNOWN"\n'
        "    cell = strip_struck(match.group(1))",
        '    match = _CHALLENGE_STATUS_ROW.search(strip_struck(text))\n'
        '    if not match:\n        return "UNKNOWN"\n'
        "    cell = match.group(1)",
    ),
    (
        "the conditional guard is dropped",
        SUBJECT,
        '_CONDITIONAL = r"(?<!\\bif )(?<!\\bIf )(?<!\\bunless )(?<!\\bUnless )'
        '(?<!\\bwhether )(?<!\\bWhether )',
        '_CONDITIONAL = r"',
    ),
    (
        "the conditional guard is widened until it refuses every site",
        SUBJECT,
        '_CONDITIONAL + r"`(CH-\\d{1,4})`"',
        'r"(?<!x)(?<!\\w)" + r"`(CH-\\d{1,4})`(?!)"',
    ),
    (
        "the clause guard goes back to [^.;|]",
        SUBJECT,
        '_CLAUSE = r"(?:(?!,\\s+(?:and|but|so|yet|or)\\b)[^.;|])"',
        '_CLAUSE = r"(?:[^.;|])"',
    ),
    (
        "the clause guard is widened to break on a relative `, which`",
        SUBJECT,
        "(?:and|but|so|yet|or)\\b",
        "(?:and|but|so|yet|or|which)\\b",
    ),
    (
        "the cancellations are read on the assertion window",
        SUBJECT,
        "            guard = line[\n"
        "                max(0, reference.start() - _VERDICT_WINDOW):\n"
        "                reference.end() + _VERDICT_WINDOW\n            ]\n"
        "            if _HISTORICAL.search(guard) or _ANSWERING.search(guard):",
        "            guard = window\n"
        "            if _HISTORICAL.search(guard) or _ANSWERING.search(guard):",
    ),
    (
        "the cancellations are read on the whole line",
        SUBJECT,
        "            guard = line[\n"
        "                max(0, reference.start() - _VERDICT_WINDOW):\n"
        "                reference.end() + _VERDICT_WINDOW\n            ]\n"
        "            if _HISTORICAL.search(guard) or _ANSWERING.search(guard):",
        "            guard = line\n"
        "            if _HISTORICAL.search(guard) or _ANSWERING.search(guard):",
    ),
    (
        "the residue is printed but not gated",
        SUBJECT,
        "return 1 if failures or unrecorded else 0",
        "return 1 if failures else 0",
    ),
)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", TESTS)],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[5:].split(":")[0] for line in run.stdout.splitlines() if line.startswith("FAIL ")]
    return run.returncode, named


def main():
    work = tempfile.mkdtemp(prefix="T-298-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
        code, named = _run(work)
        if code != 0:
            print("BASELINE IS NOT GREEN -- nothing below is a measurement")
            for name in named:
                print("   baseline failure:", name)
            return 2
        print("baseline: green, 0 named failures")
        sources = {}
        survivors = 0
        for _name, path, old, _new in MUTATIONS:
            sources.setdefault(path, open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())
            count = sources[path].count(old)
            if count != 1:
                print("ANCHOR  %-58s occurs %d times in %s, expected 1" % (_name, count, path))
                survivors += 1
        for name, path, old, new in MUTATIONS:
            source = sources[path]
            if source.count(old) != 1:
                continue
            target = os.path.join(work, "tools", path)
            open(target, "w", encoding="utf-8").write(source.replace(old, new, 1))
            code, named = _run(work)
            open(target, "w", encoding="utf-8").write(source)
            verdict = "killed" if code != 0 else "SURVIVES"
            if code == 0:
                survivors += 1
            print("%-8s %-58s %s" % (verdict, name, "; ".join(named[:3]) or "-"))
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
