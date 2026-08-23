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
# T-289 -- every rule of the COLUMN predicate, mutation-tested.
#
#     tools/T-289-mutation-test.py
#
# WHY.  `C-0127`'s standard is that restoring the old, narrow predicate must fail a NAMED test;
# `C-0138`'s addition is that a predicate carrying exclusions has TWO directions and the widening
# one is never written; and `C-0177` measured the trap that makes a mutation table look full and
# be empty -- 9 of 22 rows of its first table failed nothing, eight of them because the mutation
# WIDENED a pattern to `original|mutant` instead of replacing it.  Every mutation here is
# therefore a WHOLESALE TEXT REPLACEMENT in a throwaway copy of `tools/`, which cannot widen by
# construction: the old rule is gone.
#
# A mutation that fails NO named test is the finding, not a gap in the list (`C-0161`).
#
# `CH-0237`: the LAYOUT is a premise of the measurement.  The copy is `<tmp>/tools/*.py` beside
# `<tmp>/TASKS.md`, because the gate resolves its queue as `dirname(dirname(__file__))/TASKS.md`,
# and the baseline in an UNMUTATED copy is measured and SUBTRACTED from every killer count -- so
# a harness defect cannot read as a kill.  The three historical named tests read `TASKS.md` out of
# git and the scratch copy is not a repository, so they are absent from BOTH the baseline and the
# mutants and cannot flatter a row.
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

VERDICTS = "queue_verdicts.py"
GATE = "check-queue-vocabulary.py"

# (name, file, text replaced, text it is replaced BY).  The `old` text must occur EXACTLY once,
# which is asserted -- a mutation that silently fails to apply is a survivor for the wrong reason.
MUTATIONS = [
    (
        "NARROW the status column back to the LAST cell — the cheap census restored",
        VERDICTS,
        """    for index, cell in enumerate(header_cells):
        if _heading(cell) == STATUS_HEADING:
            return index
    return None""",
        """    return len(header_cells) - 1 if header_cells else None""",
    ),
    (
        "NARROW the status column to a fixed THIRD cell — positional, and right about one schema",
        VERDICTS,
        """    for index, cell in enumerate(header_cells):
        if _heading(cell) == STATUS_HEADING:
            return index
    return None""",
        """    return 2 if len(header_cells) > 2 else None""",
    ),
    (
        "the heading comparison stops stripping emphasis, so `**Status**` is not a status column",
        VERDICTS,
        '    return _HEADING_EMPHASIS.sub("", cell).strip().lower()',
        "    return cell.strip().lower()",
    ),
    (
        "the heading comparison stops folding case, so `STATUS` is not a status column",
        VERDICTS,
        '    return _HEADING_EMPHASIS.sub("", cell).strip().lower()',
        '    return _HEADING_EMPHASIS.sub("", cell).strip()',
    ),
    (
        "the status heading becomes `leaf`, which inverts the whole rule",
        VERDICTS, 'STATUS_HEADING = "status"', 'STATUS_HEADING = "leaf"',
    ),
    (
        "WIDEN: a table with no status column is checked anyway, against its last cell",
        VERDICTS,
        """        status = status_column(header)
        if status is None:
            continue""",
        """        status = status_column(header)
        if status is None:
            status = len(header) - 1""",
    ),
    (
        "cells are split naively, so an escaped pipe opens a column — `C-0083`'s defect restored",
        VERDICTS,
        "    return _tables.cells(row)",
        '    return [cell.strip() for cell in row.strip().strip("|").split("|")]',
    ),
    (
        "struck spans are no longer blanked, so a WITHDRAWN verdict is miscolumned",
        VERDICTS,
        "            cells = split_cells(blank_struck(row))",
        "            cells = split_cells(row)",
    ),
    (
        "WIDEN the row filter to any first cell, so every table row is a task row",
        VERDICTS,
        '            if not cells or not _IDENTIFIER_CELL.match(cells[0].strip("*` ")):',
        "            if not cells:",
    ),
    (
        "NARROW to verdicts LEFT of the status column, so a row's NOTES cell may carry one",
        VERDICTS,
        "                if verdict and index != status:",
        "                if verdict and index < status:",
    ),
    (
        "a header no longer has to be followed by a SEPARATOR, so any pipe line opens a table",
        VERDICTS,
        '        if line.strip().startswith("|") and _tables.is_separator(lines[index + 1]):',
        '        if line.strip().startswith("|"):',
    ),
    (
        "a table no longer ENDS at the first non-pipe line, so rows join the wrong header",
        VERDICTS,
        '            while row_index < len(lines) and lines[row_index].strip().startswith("|"):',
        "            while row_index < len(lines):",
    ),
    (
        # RE-ANCHORED by `T-292`, which repaired the 21 rows and promoted the arm.  The mutation
        # is INVERTED with it: the rule under test used to be *this arm does not gate* and is now
        # *this arm gates*, so the mutation that has to fail a named test is the one that puts it
        # back.  A mutation table whose rows outlive the rule they mutate measures nothing.
        "the arm reverts to ADVISORY, so a verdict in the wrong column stops failing the build",
        GATE,
        """                identifier, line, phrase, heading, status_heading
            )
        )
        defects += 1""",
        """                identifier, line, phrase, heading, status_heading
            )
        )""",
    ),
    (
        "the advisory stops naming the repair, so a refusal says nothing about what to do",
        GATE,
        """        print(
            "            move the record into the status cell, striking any verdict it supersedes.\\n"
            "            The register reads a row's LEFTMOST verdict, so a row in this shape is\\n"
            "            right only while the leftmost cell happens to hold the LIVE one --\\n"
            "            `T-276` held the SUPERSEDED one there and a live HIGH row read CLOSED"
        )""",
        '        print("            MISCOLUMNED")',
    ),
    (
        "the advisory names neither heading, so the reader cannot see which column is wrong",
        GATE,
        """            "MISCOLUMN   {:<6} line {}: {!r} renders under {!r}, not under {!r}".format(
                identifier, line, phrase, heading, status_heading
            )""",
        """            "MISCOLUMN   {} line {}".format(identifier, line)""",
    ),
    (
        "the count line is dropped, so a residue that reads zero cannot be seen to",
        GATE,
        """    print(
        "# miscolumned verdicts (GATED since T-292, at a measured false-positive rate of 0 over"
        " 140 revisions): {} verdict(s) rendering under a heading that is not their table's"
        " status column".format(len(miscolumned))
    )""",
        "    pass",
    ),
]


def _apply(directory, filename, old, new):
    path = os.path.join(directory, "tools", filename)
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    occurrences = text.count(old)
    if occurrences != 1:
        raise AssertionError(
            "anchor occurs %d times in %s: %r" % (occurrences, filename, old[:70])
        )
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text.replace(old, new))


def _failures(directory, argv):
    """The named tests a suite reports as failing, run in the mutated copy."""
    result = subprocess.run(
        [sys.executable] + argv, cwd=directory, capture_output=True, text=True, timeout=300
    )
    lines = [
        line.strip()
        for line in (result.stdout + result.stderr).splitlines()
        if line.startswith("FAIL ") or line.startswith("SELFTEST FAIL:")
    ]
    if not lines and result.returncode != 0:
        tail = (result.stderr.strip().splitlines() or ["exit %d" % result.returncode])[-1]
        lines = ["FAIL (raised) %s" % tail]
    return lines


def _populate(directory):
    """`<tmp>/tools/*.py` beside `<tmp>/TASKS.md` — `CH-0237`'s layout premise."""
    tools = os.path.join(directory, "tools")
    os.makedirs(tools)
    for source in os.listdir(TOOLS):
        if source.endswith(".py"):
            shutil.copy2(os.path.join(TOOLS, source), tools)
    shutil.copy2(os.path.join(ROOT, "TASKS.md"), os.path.join(directory, "TASKS.md"))


def _baseline():
    """Named tests that fail in an UNMUTATED copy, subtracted from every killer count below."""
    directory = tempfile.mkdtemp(prefix="T-289-baseline.")
    try:
        _populate(directory)
        return set(_failures(directory, ["tools/" + GATE, "--selftest"])) | set(
            _failures(directory, ["tools/test-trace-answers.py"])
        )
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    if argv:
        print("usage: T-289-mutation-test.py")
        return 2
    base = _baseline()
    print(
        "# baseline in an unmutated copy: %d pre-existing failure(s), SUBTRACTED from every "
        "killer count below" % len(base)
    )
    for line in sorted(base):
        print("#   %s" % line[:110])
    survivors = []
    for name, filename, old, new in MUTATIONS:
        directory = tempfile.mkdtemp(prefix="T-289-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            gate = [
                f for f in _failures(directory, ["tools/" + GATE, "--selftest"]) if f not in base
            ]
            reader = [
                f for f in _failures(directory, ["tools/test-trace-answers.py"])
                if f not in base
            ]
        finally:
            shutil.rmtree(directory, ignore_errors=True)
        if gate or reader:
            print("killed  gate %2d  reader %2d   %s" % (len(gate), len(reader), name))
            for killer in (gate + reader)[:2]:
                print("                            %s" % killer[:110])
        else:
            print("SURVIVED                    %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
