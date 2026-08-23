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
# T-292 -- every rule of the COLUMN REPAIR, mutation-tested.
#
#     tools/T-292-mutation-test.py
#
# WHY.  `C-0127`'s standard is that restoring the old, narrow rule must fail a NAMED test;
# `C-0138`'s addition is that a predicate carrying exclusions has TWO directions and the widening
# one is never written; and `C-0177` measured the trap that makes a mutation table look full and
# be empty -- a mutation that WIDENS a rule to `original|mutant` leaves the original matching
# everything it used to.  Every mutation here is a WHOLESALE TEXT REPLACEMENT in a throwaway copy
# of `tools/`, which cannot widen by construction: the old rule is gone.
#
# A mutation that fails NO named test is the finding, not a gap in the list (`C-0161`).
#
# BOTH DIRECTIONS, on the rules that carry an exclusion.  The leaf grammar is mutated NARROW (a
# row the queue really does carry stops being repairable) and WIDE (any trailing word becomes a
# leaf); the strike is mutated to nothing and to everything; the sentence boundary to any period
# and to none.  A rule mutated in one direction only is a rule half held open.
#
# `CH-0237`: the LAYOUT is a premise of the measurement.  The copy is `<tmp>/tools/*.py` beside
# `<tmp>/TASKS.md`, because both the repair tool and the gate resolve their queue as
# `dirname(dirname(__file__))/TASKS.md`, and the baseline in an UNMUTATED copy is measured and
# SUBTRACTED from every killer count -- so a harness defect cannot read as a kill.
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

REPAIR = "T-292-column-repair.py"
GATE = "check-queue-vocabulary.py"

# (name, file, text replaced, text it is replaced BY).  The `old` text must occur EXACTLY once,
# which is asserted -- a mutation that silently fails to apply is a survivor for the wrong reason,
# and is exactly `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op*.
MUTATIONS = [
    # --- the leaf grammar, in BOTH directions ---
    (
        "NARROW the leaf grammar to an NDI leaf ID, so the queue's own `new` and `—` are refused",
        REPAIR,
        'LEAF_TOKEN = re.compile(r"^(A\\d+(?:\\.\\d+)*|new|—)$")',
        'LEAF_TOKEN = re.compile(r"^A\\d+(?:\\.\\d+)*$")',
    ),
    (
        "WIDEN the leaf grammar to any word, so the last word of a record becomes a leaf",
        REPAIR,
        'LEAF_TOKEN = re.compile(r"^(A\\d+(?:\\.\\d+)*|new|—)$")',
        'LEAF_TOKEN = re.compile(r"^\\S+$")',
    ),
    (
        "the leaf is read off the FRONT of the cell, where the record is, not off its end",
        REPAIR,
        "    parts = stripped.rsplit(None, 1)\n"
        "    if len(parts) != 2 or not LEAF_TOKEN.match(parts[1]):\n"
        "        return None\n"
        "    return (parts[0].strip(), parts[1])",
        "    parts = stripped.split(None, 1)\n"
        "    if len(parts) != 2 or not LEAF_TOKEN.match(parts[0]):\n"
        "        return None\n"
        "    return (parts[1].strip(), parts[0])",
    ),
    (
        "a cell with no leaf token is repaired anyway, with an empty leaf invented for it",
        REPAIR,
        "    if len(parts) != 2 or not LEAF_TOKEN.match(parts[1]):\n        return None",
        "    if len(parts) != 2 or not LEAF_TOKEN.match(parts[1]):\n        return (stripped, \"—\")",
    ),
    # --- the strike, in BOTH directions ---
    (
        "the strike WIDENS to the whole preserved note, which blanks prose no verdict supersedes",
        REPAIR,
        '    return "~~" + match.group(0) + "~~" + stripped[match.end():]',
        '    return "~~" + stripped + "~~"',
    ),
    (
        "nothing is struck, so a superseded verdict stands live beside the record that closed it",
        REPAIR,
        "    match = LEADING_VERDICT_RUN.match(stripped)\n"
        "    if not match:\n"
        "        return stripped",
        "    match = None\n"
        "    if match is None:\n"
        "        return stripped",
    ),
    (
        "the strike stops taking `(iteration N)` with it, so half a superseded record reads live",
        REPAIR,
        'r"^(?:TODO\\s*—\\s*)?\\*\\*[^*]{1,200}?\\*\\*(?:\\s*\\(iteration\\s+\\d+\\))?"',
        'r"^(?:TODO\\s*—\\s*)?\\*\\*[^*]{1,200}?\\*\\*"',
    ),
    (
        "the strike stops recognising a bare `TODO —` opening, so that idiom is left unstruck",
        REPAIR,
        'r"^(?:TODO\\s*—\\s*)?\\*\\*[^*]{1,200}?\\*\\*(?:\\s*\\(iteration\\s+\\d+\\))?"',
        'r"^\\*\\*[^*]{1,200}?\\*\\*(?:\\s*\\(iteration\\s+\\d+\\))?"',
    ),
    # --- the four-column split, in BOTH directions ---
    (
        "the sentence boundary WIDENS to any period, so a `.md)` link target splits the cell",
        REPAIR,
        'SENTENCE_END = re.compile(r"\\. ")',
        'SENTENCE_END = re.compile(r"\\.")',
    ),
    (
        "the whole cell becomes the status cell and the notes cell is emptied",
        REPAIR,
        "    return (stripped[:match.start() + 1], stripped[match.end():].strip())",
        '    return (stripped, "")',
    ),
    (
        "a cell with no sentence boundary is split at its end rather than refused",
        REPAIR,
        "    match = SENTENCE_END.search(stripped)\n    if not match:\n        return None",
        '    match = SENTENCE_END.search(stripped)\n    if not match:\n        return (stripped, "")',
    ),
    # --- the repair itself ---
    (
        "the NOTES shape SWAPS its two cells, so an acceptance renders under `Notes` instead",
        REPAIR,
        '        repaired[status - 1] = cells[status - 1].strip() + " — " + cells[status].strip()\n'
        "        repaired[status] = verdict\n"
        "        repaired[index] = notes",
        "        repaired[status] = cells[index]\n"
        "        repaired[index] = cells[status]",
    ),
    (
        "the NOTES shape DROPS the acceptance instead of folding it into the task cell",
        REPAIR,
        '        repaired[status - 1] = cells[status - 1].strip() + " — " + cells[status].strip()',
        "        repaired[status - 1] = cells[status - 1].strip()",
    ),
    (
        "the LEAF shape drops the leaf and leaves the cell empty",
        REPAIR,
        "        repaired[index] = leaf",
        '        repaired[index] = ""',
    ),
    (
        "the LEAF shape leaves the superseded note ahead of the record, so the register would "
        "read the row off the wrong verdict",
        REPAIR,
        '        repaired[status] = record + " " + strike_leading_verdict(cells[status])',
        '        repaired[status] = strike_leading_verdict(cells[status]) + " " + record',
    ),
    (
        "a row in an uncovered shape is half repaired instead of reported",
        REPAIR,
        '    return None, "the verdict is neither one column left nor one column right of the status column"',
        "    repaired[status], repaired[index] = repaired[index], repaired[status]\n"
        '    return repaired, "swapped"',
    ),
    # --- the preservation proof itself ---
    (
        "the token key stops distinguishing anything, so the preservation proof proves nothing",
        REPAIR,
        '    return collections.Counter(re.findall(r"\\S+", text.replace("~~", "")))',
        "    return collections.Counter()",
    ),
    (
        "the token key blanks punctuation too, so a lost bracket or dash reads as preserved",
        REPAIR,
        '    return collections.Counter(re.findall(r"\\S+", text.replace("~~", "")))',
        '    return collections.Counter(re.findall(r"[A-Za-z0-9]+", text))',
    ),
    # --- the rows are located by the GATE's predicate, not by a list ---
    (
        "the repair looks only RIGHT of the status column, so the eleven leaf rows are not found",
        REPAIR,
        "                if _verdicts.cell_verdict(cell) and index != status:",
        "                if _verdicts.cell_verdict(cell) and index > status:",
    ),
    (
        "struck spans are no longer blanked when the rows are located, so a repaired row fires "
        "again and the repair is no longer idempotent",
        REPAIR,
        "            cells = _verdicts.split_cells(_verdicts.blank_struck(row))\n"
        '            if not cells or not _verdicts._IDENTIFIER_CELL.match(cells[0].strip("*` ")):',
        "            cells = _verdicts.split_cells(row)\n"
        '            if not cells or not _verdicts._IDENTIFIER_CELL.match(cells[0].strip("*` ")):',
    ),
    # --- the gate, in the direction `T-289`'s own table does not carry ---
    (
        "the gate stops reading the queue's own file, so the repair is proved against nothing",
        REPAIR,
        'QUEUE = os.path.join(ROOT, "TASKS.md")',
        'QUEUE = os.path.join(ROOT, "TASKS.md.absent")',
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


def _suites():
    return (
        ["tools/" + REPAIR, "--self-test"],
        ["tools/" + GATE, "--selftest"],
    )


def _baseline():
    """Named tests that fail in an UNMUTATED copy, subtracted from every killer count below."""
    directory = tempfile.mkdtemp(prefix="T-292-baseline.")
    try:
        _populate(directory)
        found = set()
        for suite in _suites():
            found |= set(_failures(directory, suite))
        return found
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    if argv:
        print("usage: T-292-mutation-test.py")
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
        directory = tempfile.mkdtemp(prefix="T-292-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            killers = []
            for suite in _suites():
                killers += [f for f in _failures(directory, suite) if f not in base]
        finally:
            shutil.rmtree(directory, ignore_errors=True)
        if killers:
            print("killed  %2d   %s" % (len(killers), name))
            for killer in killers[:2]:
                print("             %s" % killer[:112])
        else:
            print("SURVIVED     %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
