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
# P-30 -- every rule of the queue's verdict predicate, mutation-tested.
#
#     tools/P-30-mutation-test.py
#
# WHY.  `C-0127`'s standard is that restoring the old, narrow predicate must fail a NAMED test;
# `C-0138`'s addition is that a predicate carrying exclusions has TWO directions and the widening
# one is never written; and `C-0177` measured the trap that makes a mutation table look full and
# be empty -- **9 of 22 rows of its first table failed nothing**, eight of them because the
# mutation WIDENED a pattern to `original|mutant` instead of replacing it, so the original still
# matched everything it used to.  Every mutation here is therefore a WHOLESALE TEXT REPLACEMENT
# in a throwaway copy of `tools/`, which cannot widen by construction: the old rule is gone.
#
# A mutation that fails NO named test is the finding, not a gap in the list (`C-0161`).
#
# The two named-test suites a mutation has to get past are `tools/test-trace-answers.py` (the
# READER) and `tools/check-queue-vocabulary.py --selftest` (the GATE), and a rule that only one of
# them reaches is worth knowing about, so the killer counts are reported per suite.
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

VERDICTS = "queue_verdicts.py"
READER = "trace-answers.py"
GATE = "check-queue-vocabulary.py"

# (name, file, text replaced, text it is replaced BY).  The `old` text must occur EXACTLY once,
# which is asserted -- a mutation that silently fails to apply is a survivor for the wrong reason
# and is exactly `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op*.
MUTATIONS = [
    (
        "MAX_WORDS is widened, so a bold PROSE sentence becomes a verdict",
        VERDICTS, "MAX_WORDS = 6", "MAX_WORDS = 60",
    ),
    (
        "MAX_WORDS is narrowed to one word, so a two-word qualifier stops being a verdict",
        VERDICTS, "MAX_WORDS = 6", "MAX_WORDS = 1",
    ),
    (
        "the closing-word set is narrowed to the two words SESSION-PROMPT.md declares",
        VERDICTS,
        r'CLOSING_WORD = re.compile(r"\b(DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED)\b")',
        r'CLOSING_WORD = re.compile(r"\b(DONE|KILLED)\b")',
    ),
    (
        "the closing-word match loses its word boundaries, so ABANDONED contains DONE",
        VERDICTS,
        r'CLOSING_WORD = re.compile(r"\b(DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED)\b")',
        r'CLOSING_WORD = re.compile(r"(DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED)")',
    ),
    (
        "the PARTIALLY/PARTLY qualifier is dropped, so a qualified row closes",
        VERDICTS,
        'NOT_CLOSED_QUALIFIER = r"(?<!PARTIALLY )(?<!PARTLY )"',
        'NOT_CLOSED_QUALIFIER = r""',
    ),
    (
        "TASK_ROW requires a trailing pipe again — the `T-182` defect restored",
        VERDICTS,
        r'TASK_ROW = re.compile(r"\|\s*\**`?([TP]-\d{1,4}[a-z]?)`?\**\s*\|(.*)$")',
        r'TASK_ROW = re.compile(r"\|\s*\**`?([TP]-\d{1,4}[a-z]?)`?\**\s*\|(.*)\|\s*$")',
    ),
    (
        "TASK_ROW loses its tolerance of a backticked or bold identifier",
        VERDICTS,
        r'TASK_ROW = re.compile(r"\|\s*\**`?([TP]-\d{1,4}[a-z]?)`?\**\s*\|(.*)$")',
        r'TASK_ROW = re.compile(r"\|\s*([TP]-\d{1,4}[a-z]?)\s*\|(.*)$")',
    ),
    (
        "the row match is widened from `.match` to `.search`, so a mid-line table starts a row",
        READER,
        "        match = _QUEUE_ROW.match(line.strip())",
        "        match = _QUEUE_ROW.search(line.strip())",
    ),
    (
        "a leading TODO stops being a verdict — `P-29`'s predicate restored",
        VERDICTS, r'_LEADING_TODO = re.compile(r"TODO\b")', r'_LEADING_TODO = re.compile(r"(?!)")',
    ),
    (
        "the leading-TODO test loses its word boundary, so TODOISH opens a row",
        VERDICTS, r'_LEADING_TODO = re.compile(r"TODO\b")', r'_LEADING_TODO = re.compile(r"TODO")',
    ),
    (
        "the leading-TODO test is widened from `.match` to `.search`, so a TODO anywhere opens",
        VERDICTS,
        'if _LEADING_TODO.match(stripped.lstrip("*").lstrip()):',
        'if _LEADING_TODO.search(stripped.lstrip("*").lstrip()):',
    ),
    (
        "the bold-run test is widened from `.match` to `.search`, so a bold run anywhere leads",
        VERDICTS, "bold = _LEADING_BOLD.match(stripped)", "bold = _LEADING_BOLD.search(stripped)",
    ),
    (
        "the bold run stops having to carry a closing word",
        VERDICTS,
        "if len(phrase.split()) > MAX_WORDS or not CLOSING_WORD.search(phrase):",
        "if len(phrase.split()) > MAX_WORDS:",
    ),
    (
        "struck spans are no longer blanked, so a withdrawn verdict counts",
        VERDICTS,
        '    return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)',
        "    return text",
    ),
    (
        "blanking a struck span stops preserving its length",
        VERDICTS,
        '    return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)',
        '    return re.sub(r"~~.*?~~", "", text, flags=re.DOTALL)',
    ),
    (
        "`task_rows` shares the reader's own pattern, so coverage becomes tautological",
        VERDICTS,
        '        if re.match(r"^[TP]-\\d{1,4}[a-z]?$", head):',
        '        if TASK_ROW.match(stripped):\n            head = TASK_ROW.match(stripped).group(1)',
    ),
    (
        "`task_rows` accepts any first cell, so every table row is a task row",
        VERDICTS,
        '        if re.match(r"^[TP]-\\d{1,4}[a-z]?$", head):',
        "        if head:",
    ),
    (
        "the reader takes the LAST verdict instead of the leftmost one",
        READER, "statuses[identifier] = verdicts[0][1]", "statuses[identifier] = verdicts[-1][1]",
    ),
    (
        "the reader ignores the verdict and scans the whole row — the `P-30` defect restored",
        READER,
        "        elif verdicts:\n            statuses[identifier] = verdicts[0][1]\n",
        "",
    ),
    (
        "the reader loses its fallback, so a row with no verdict is always open",
        READER,
        "        elif _CLOSED.search(rest):\n            statuses[identifier] = \"CLOSED\"\n",
        "",
    ),
    (
        "the vocabulary loses `TODO`, so every open row is an undeclared coinage",
        GATE, '    "TODO",\n', "",
    ),
    (
        "the per-row agreement check reads EVERY verdict rather than the leftmost",
        GATE,
        "            found.append((match.group(1), verdicts[0][0], verdicts[0][1]))",
        "            found += [(match.group(1), p, s) for p, s in verdicts]",
    ),
    (
        "the per-row agreement check compares against the wrong sense",
        GATE, '            expected = "CLOSED"', '            expected = "OPEN"',
    ),
    (
        "the residue is computed on the verdict PHRASE rather than on its sense",
        GATE, "        if scanned != verdicts[0][1]:", "        if scanned != verdicts[0][0]:",
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
        # A mutation that makes a suite THROW is killed by whichever test reached it.
        tail = (result.stderr.strip().splitlines() or ["exit %d" % result.returncode])[-1]
        lines = ["FAIL (raised) %s" % tail]
    return lines


def _populate(directory):
    """`<tmp>/tools/*.py` beside `<tmp>/TASKS.md`.

    `T-283`/`CH-0237`.  The layout is a PREMISE of the measurement, not a convenience.  This
    harness used to copy `tools/` FLAT, and the gate resolves its queue as
    `dirname(dirname(__file__))/TASKS.md` -- harmless while no self-test read the queue, and the
    moment `T-283` added self-tests that do, every mutation was `killed` by one and the same
    `FileNotFoundError: /tmp/TASKS.md`.  A table that reads *24 mutations, 0 survivors* and kills
    every row for a reason that is not about the row is exactly `C-0177`'s trap -- full and empty
    -- reached from the SUBJECT's side rather than from the table's.
    """
    tools = os.path.join(directory, "tools")
    os.makedirs(tools)
    for source in os.listdir(TOOLS):
        if source.endswith(".py"):
            shutil.copy2(os.path.join(TOOLS, source), tools)
    shutil.copy2(os.path.join(ROOT, "TASKS.md"), os.path.join(directory, "TASKS.md"))


def _baseline():
    """Named tests that fail in an UNMUTATED copy, subtracted from every killer count below.

    Without the subtraction a harness defect is indistinguishable from a kill, which is the
    failure this file exists to avoid one level down.
    """
    directory = tempfile.mkdtemp(prefix="P-30-baseline.")
    try:
        _populate(directory)
        return set(_failures(directory, ["tools/test-trace-answers.py"])) | set(
            _failures(directory, ["tools/check-queue-vocabulary.py", "--selftest"])
        )
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main():
    base = _baseline()
    print(
        "# baseline in an unmutated copy: %d pre-existing failure(s), SUBTRACTED from every "
        "killer count below" % len(base)
    )
    for line in sorted(base):
        print("#   %s" % line[:110])
    survivors = []
    for name, filename, old, new in MUTATIONS:
        directory = tempfile.mkdtemp(prefix="P-30-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            killers = [
                f for f in _failures(directory, ["tools/test-trace-answers.py"])
                if f not in base
            ]
            gate = [
                f for f in _failures(
                    directory, ["tools/check-queue-vocabulary.py", "--selftest"]
                )
                if f not in base
            ]
        finally:
            shutil.rmtree(directory, ignore_errors=True)
        if killers or gate:
            print(
                "killed  reader %2d  gate %2d   %s" % (len(killers), len(gate), name)
            )
            for killer in (killers + gate)[:2]:
                print("                          %s" % killer[:110])
        else:
            print("SURVIVED                  %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main())
