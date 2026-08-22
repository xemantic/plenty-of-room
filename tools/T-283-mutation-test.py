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
# T-283 -- the code-span blanking and the promoted residue gate, mutation-tested.
#
#     tools/T-283-mutation-test.py
#
# Every mutation is a WHOLESALE TEXT REPLACEMENT in a throwaway copy of `tools/`, never a widening
# to `original|mutant`: `C-0177` measured that trap at 9 of 22 rows of `C-0176`'s first table, and
# `C-0179`'s coordinator at 2 of 6.  A mutation that fails NO named test is the finding, not a gap
# in the list (`C-0161`).
#
# The predicate carries an EXCLUSION -- a closing word inside a code span is not residue -- so it
# has TWO directions and both are mutated: the blanking is removed (the old, narrow predicate
# restored) and the blanking is widened until it hides text it must not.
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

VERDICTS = "queue_verdicts.py"
GATE = "check-queue-vocabulary.py"

MUTATIONS = [
    # --- direction 1: the exclusion is REMOVED, i.e. `P-30`'s own predicate restored
    (
        "the blanking is a no-op — the pre-`T-283` predicate restored, and `T-261` fires again",
        VERDICTS,
        '    return _CODE_SPAN.sub(lambda m: " " * len(m.group(0)), text)',
        "    return text",
    ),
    (
        "the residue scans the UNBLANKED body, so the blanking exists and is not used",
        GATE,
        "        body = _verdicts.blank_code_spans(_verdicts.blank_struck(match.group(2)))",
        "        body = _verdicts.blank_struck(match.group(2))",
    ),
    (
        "the blanking stops preserving length, so offsets below a span are wrong",
        VERDICTS,
        '    return _CODE_SPAN.sub(lambda m: " " * len(m.group(0)), text)',
        '    return _CODE_SPAN.sub("", text)',
    ),
    (
        "the DOUBLE-backtick form is dropped, so ``ANSWERED`` survives the blanking",
        VERDICTS,
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`")',
        r'_CODE_SPAN = re.compile(r"`[^`\n]*`")',
    ),
    (
        "the alternation is REORDERED, so the single form eats the double one's opening backticks",
        VERDICTS,
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`")',
        r'_CODE_SPAN = re.compile(r"`[^`\n]*`|``[^\n]*?``")',
    ),
    # --- direction 2: the exclusion is WIDENED until it hides text it must not
    (
        "the span is allowed to contain backticks, so two spans MERGE and swallow the prose "
        "between them",
        VERDICTS,
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`")',
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^\n]*`")',
    ),
    (
        "an UNCLOSED backtick blanks the rest of the row",
        VERDICTS,
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`")',
        r'_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`?")',
    ),
    (
        "the blanking is applied to the VERDICT as well as the scan, so a backticked verdict "
        "stops being one",
        VERDICTS,
        "    for cell in blank_struck(row_body).split(\"|\"):",
        "    for cell in blank_code_spans(blank_struck(row_body)).split(\"|\"):",
    ),
    # --- the promotion itself: the residue must now COUNT as a defect
    (
        "the residue stops counting as a defect — advisory again",
        GATE,
        '''            "            as DATA — put it in `backticks`, which this scan blanks".format(
                identifier, phrase, scanned
            )
        )
        defects += 1''',
        '''            "            as DATA — put it in `backticks`, which this scan blanks".format(
                identifier, phrase, scanned
            )
        )''',
    ),
    (
        "the residue's message drops both repairs, so a refusal says only that it refused",
        GATE,
        '''            "RESIDUE     {}  leads with {!r} and its PROSE carries a closing word: a whole-row\\n"
            "            scan reads {}. The queue writes verdicts in bold UPPER CASE and prose in\\n"
            "            lower, so either lower-case the word, or — if it is a status token quoted\\n"
            "            as DATA — put it in `backticks`, which this scan blanks".format(
                identifier, phrase, scanned
            )''',
        '''            "RESIDUE     {} {!r} {}".format(identifier, phrase, scanned)''',
    ),
    (
        "the residue compares the scan against the verdict PHRASE rather than its SENSE",
        GATE,
        "        if scanned != verdicts[0][1]:",
        "        if scanned != verdicts[0][0]:",
    ),
    (
        "the residue reads EVERY verdict of a row rather than the leftmost, so the queue's "
        "preserved-priority column fires",
        GATE,
        "        scanned = \"CLOSED\" if _verdicts.UNQUALIFIED_CLOSING_WORD.search(body) else \"OPEN\""
        "\n        if scanned != verdicts[0][1]:",
        "        scanned = \"CLOSED\" if _verdicts.UNQUALIFIED_CLOSING_WORD.search(body) else \"OPEN\""
        "\n        if scanned != verdicts[-1][1]:",
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
    result = subprocess.run(
        [sys.executable] + argv, cwd=directory, capture_output=True, text=True, timeout=600
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
    """`<tmp>/tools/*.py` beside `<tmp>/TASKS.md`.

    The layout matters: the gate resolves its queue as `dirname(dirname(__file__))/TASKS.md`, so a
    FLAT copy of `tools/` points it at `/tmp/TASKS.md` and every one of its corpus tests fails
    identically -- which the baseline subtraction would then hide, leaving a mutation table that
    looks full and is empty.  `T-283`'s first run was exactly that: 12 mutations, 12 survivors.
    """
    tools = os.path.join(directory, "tools")
    os.makedirs(tools)
    for source in os.listdir(TOOLS):
        if source.endswith(".py"):
            shutil.copy2(os.path.join(TOOLS, source), tools)
    shutil.copy2(os.path.join(ROOT, "TASKS.md"), os.path.join(directory, "TASKS.md"))
    return tools


def _baseline():
    """Named tests that fail in an UNMUTATED copy; subtracted from every killer count."""
    directory = tempfile.mkdtemp(prefix="T-283-baseline.")
    try:
        _populate(directory)
        return set(_failures(directory, ["tools/" + GATE, "--selftest"])) | set(
            _failures(directory, ["tools/test-trace-answers.py"])
        )
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    if argv:
        print("usage: T-283-mutation-test.py")
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
        directory = tempfile.mkdtemp(prefix="T-283-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            gate = [
                f for f in _failures(directory, ["tools/" + GATE, "--selftest"]) if f not in base
            ]
            reader = [
                f for f in _failures(directory, ["tools/test-trace-answers.py"]) if f not in base
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
