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
# T-306 -- MUTATION COVERAGE FOR THE HARNESS-OUTPUT CONTRACT.
#
#     tools/T-306-mutation-test.py
#
# Two subjects, because the contract has two halves and they check each other:
# `tools/P-31-harness-census.py` DECLARES what each harness prints, and
# `tools/T-295-mutation-input-census.py` READS it.  A mutation of either must fail a NAMED test
# of one of them, and both suites are run for every row, so a rule that only the other tool
# notices is still held open.
#
# `C-0176`'s standard, in BOTH directions: a table that can only ever narrow becomes a pattern.
# Rows below revert a rule to what it was and rows over-widen it; each replaces the rule
# WHOLESALE, never as an alternation with the original.
#
# `CH-0237`'s baseline runs first and its named failures are subtracted.  Without it a fixture
# defect reads as `0 survivors` (the quiet direction) or as every row killed off one and the same
# unrelated error (the loud one), and the headline means nothing either way.
#
# WHY `src/` IS SYMLINKED RATHER THAN COPIED.  `P-31`'s own named tests resolve every anchor of
# every declared harness, and two of those harnesses mutate KOTLIN under `src/`.  A work tree of
# `tools/` alone would fail thirteen anchors in the BASELINE -- a fixture that cannot discriminate,
# which is the defect this corpus keeps finding in its own harnesses.  `P-31` only reads `src/`,
# so a symlink is enough and 14 MB per mutation is not spent.
"""Mutation coverage for T-306's harness-output contract, over P-31 and T-295."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
P31 = "P-31-harness-census.py"
CENSUS = "T-295-mutation-input-census.py"

#: A killer that fires for ANY text change to either subject, and is therefore evidence of
#: nothing here.  Both subjects are themselves the subjects of mutation harnesses -- this one and
#: `tools/T-295-mutation-test.py` -- whose ANCHORS are literal spans of their source, so replacing
#: any such span orphans an anchor and `P-31`'s own resolution test fails.  Subtracted BY NAME and
#: loudly, because a table in which every row is killed by one and the same unrelated failure is
#: `CH-0237`'s defect wearing the opposite sign: full and empty at once (`C-0177`).  Five of the
#: nineteen rows below were killed by this and by nothing else on the harness's first run.
EXPECTED_COLLATERAL = frozenset({
    "P-31 every declared harness resolves every anchor it declares, in this tree",
})

#: Each suite must print this, or it did not finish.  A mutation that makes a tool CRASH fails no
#: named test: the suite stops, prints a traceback and exits 1, which is indistinguishable from a
#: clean run by exit code alone.  Read the line the suite ends with instead.
COMPLETED = "self-test(s)"

#: (name, file, old, new).  `old` must occur EXACTLY once in its subject, or the row is a
#: reference into a source that has been refactored underneath it and reports nothing (`C-0185`).
MUTATIONS = (
    # --- T-301: `wired_in` is a USE and not a MENTION ------------------------------------------
    (
        "wired_in reverts to a substring test on the build script",
        P31,
        "    if any(basename in span for span in command_line_spans(build_text)):",
        "    if basename in build_text:",
    ),
    (
        "wired_in reverts to a substring test on the shell script",
        P31,
        "    if any(word.endswith(basename) for word in shell_command_words(verify_text)):",
        "    if basename in verify_text:",
    ),
    (
        "the `//` line comment is no longer blanked",
        P31,
        '        if two == "//":',
        '        if two == "/\\x00":',
    ),
    (
        "the `/* */` block comment is no longer blanked",
        P31,
        '        elif two == "/*":',
        '        elif two == "/\\x00":',
    ),
    (
        "the commandLine span is WIDENED to the whole comment-stripped script, so a description "
        "string counts as a use",
        P31,
        "        spans.append(text[start:i])\n        start = text.find(\"commandLine(\", i)\n"
        "    return spans",
        "        spans.append(text[start:i])\n        start = text.find(\"commandLine(\", i)\n"
        "    return [text]",
    ),
    (
        "the shell command word is WIDENED to every word of the line, so an echo counts as a run",
        P31,
        "        word = stripped.split()[0].strip(\"\\\"'\")\n"
        "        if word:\n            words.append(word)",
        "        for word in stripped.split():\n            words.append(word.strip(\"\\\"'\"))",
    ),
    (
        "the shell `#` comment is no longer stripped",
        P31,
        '        elif character == "#":',
        '        elif character == "\\x00":',
    ),
    # --- T-306: the declaration itself ---------------------------------------------------------
    (
        "declared_row_shapes ignores WHICH harness is asked and answers with the first row",
        P31,
        "        if row[0] == basename:\n            return tuple(row[4])",
        "        if True:\n            return tuple(row[4])",
    ),
    (
        "the shape vocabulary is COPIED into P-31 instead of loaded from the census that owns it",
        P31,
        "    return {name for name, _pattern, _shape in module.ROW_SHAPES}",
        '    return {"killed-by", "killed-pair", "killed-n", "kind-row", "arrow", "of-row"}',
    ),
    (
        "the census row stops carrying the declared shapes, so the contract is not published",
        P31,
        '            "rowShapes": list(declared_row_shapes(basename)),',
        '            "rowShapes": [],',
    ),
    # --- T-306: the reading ---------------------------------------------------------------------
    (
        "parse_rows ignores the declared shapes and tries every pattern again",
        CENSUS,
        "            if shapes is not None and _name not in shapes:\n                continue",
        "            if False:\n                continue",
    ),
    (
        "an undeclared shape is not counted, so a changed output reads as `printed no row`",
        CENSUS,
        "    return len(parse_rows(output)) - len(parse_rows(output, shapes))",
        "    return 0",
    ),
    (
        "a DECLARED shape that never prints is no longer a refusal — the stale half of the "
        "registry stops being checked",
        CENSUS,
        "    for name in sorted(set(shapes) - matched_shapes(output, shapes)):",
        "    for name in ():",
    ),
    (
        "a SURVIVOR shape is gated like any other, so a harness with no survivors is refused for "
        "not printing one (the WIDEN direction)",
        CENSUS,
        "        if name in SURVIVOR_ONLY_SHAPES:\n            continue",
        "        if False:\n            continue",
    ),
    (
        "the survivor shapes are a COPIED list rather than derived from the census's own table",
        CENSUS,
        'SURVIVOR_ONLY_SHAPES = {name for name, _pattern, kind in ROW_SHAPES if kind == "zero"}',
        'SURVIVOR_ONLY_SHAPES = {"survived"}',
    ),
    (
        "a harness that states NO row count is admitted again, which is the silent under-count",
        CENSUS,
        '    if stated is None:\n'
        "        # `T-306`.  Without a stated count a PARTIAL shape change drops the same rows "
        "from both\n"
        "        # arms, the two lengths agree, and nothing above can see it -- measured live at "
        "three of\n"
        "        # the fourteen harnesses that run bare.  The count is the only cross-check that "
        "is not a\n"
        "        # comparison of the census with itself.\n"
        '        refusals.append("the harness states no count of its own, so a partial shape '
        'change would "\n'
        '                        "drop the same rows from both arms silently: print a "\n'
        '                        "`# N mutation(s), M survivor(s)` summary line")\n'
        "    elif stated != len(control_rows):",
        "    if stated is not None and stated != len(control_rows):",
    ),
    (
        "a STALE BY-HAND declaration is admitted: the harness runs and the table still says "
        "by hand",
        CENSUS,
        "    if declared and not derived:\n        return [\"is declared BY-HAND",
        "    if False:\n        return [\"is declared BY-HAND",
    ),
    (
        "an UNDECLARED by-hand harness is admitted: it prints a usage line and no row of the "
        "table says so",
        CENSUS,
        "    if derived and not declared:\n        return [\"printed its own usage line",
        "    if False:\n        return [\"printed its own usage line",
    ),
    (
        "the by-hand short circuit fires on the DECLARATION alone, so the derivation stops "
        "being a cross-check",
        CENSUS,
        "    return declared and derived",
        "    return declared",
    ),
    # --- T-306, the FOURTH collision: the label a row carries -----------------------------------
    (
        "the label check is dropped, so a row that carries its killers as well as its name is "
        "read and drifts between the arms",
        CENSUS,
        "    if not names:\n        return []\n    bad = []",
        "    if True:\n        return []\n    bad = []",
    ),
    (
        "the label must EQUAL a declared name, so a harness that pads its name into a column is "
        "refused (the NARROW direction)",
        CENSUS,
        "        if not any(text and any(name.startswith(text) for name in names)\n"
        "                   for text in candidates):",
        "        if not any(text and text in names\n"
        "                   for text in candidates):",
    ),
    (
        "the prefix comparison is taken the other way round, so a name followed by anything at "
        "all is admitted — which is the defect itself (the WIDEN direction)",
        CENSUS,
        "        if not any(text and any(name.startswith(text) for name in names)\n"
        "                   for text in candidates):",
        "        if not any(text and any(text.startswith(name) for name in names)\n"
        "                   for text in candidates):",
    ),
    (
        "the NARROW/WIDEN kind prefix is no longer stripped, so two harnesses' every row is "
        "refused",
        CENSUS,
        'KIND_PREFIXES = ("NARROW ", "WIDEN ")',
        "KIND_PREFIXES = ()",
    ),
    (
        "the empty-label guard is dropped, so a row whose label parsed as nothing matches every "
        "name by prefix",
        CENSUS,
        "        if not any(text and any(name.startswith(text) for name in names)\n"
        "                   for text in candidates):",
        "        if not any(any(name.startswith(text) for name in names)\n"
        "                   for text in candidates):",
    ),
    (
        "the kind prefix is stripped UNCONDITIONALLY, so a harness that names its rows after the "
        "direction they go in has every one of them refused",
        CENSUS,
        "        candidates = [label]\n"
        "        for prefix in KIND_PREFIXES:\n"
        "            if label.startswith(prefix):\n"
        "                candidates.append(label[len(prefix):])",
        "        candidates = [label]\n"
        "        for prefix in KIND_PREFIXES:\n"
        "            if label.startswith(prefix):\n"
        "                candidates = [label[len(prefix):]]",
    ),
    (
        "the drift report truncates both labels again, so it prints two identical strings and "
        "says they differ",
        CENSUS,
        '    return ("row labels differ at character %d: control %r against treatment %r"\n'
        "            % (position + 1, control_label[position:position + 60],\n"
        "               treatment_label[position:position + 60]))",
        '    return ("row labels drift between the two arms: %r against %r"\n'
        "            % (control_label[:40], treatment_label[:40]))",
    ),
    (
        "the drift position is reported zero-based, so the character it names is the last one "
        "that agrees",
        CENSUS,
        "            % (position + 1, control_label[position:position + 60],",
        "            % (position, control_label[position:position + 60],",
    ),
)


def _work_tree():
    """A copy of `tools/` with `src/` and the build script beside it, read-only, as symlinks."""
    work = tempfile.mkdtemp(prefix="T-306-mutation.")
    shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
    os.symlink(os.path.join(ROOT, "src"), os.path.join(work, "src"))
    shutil.copy2(os.path.join(ROOT, "build.gradle.kts"), os.path.join(work, "build.gradle.kts"))
    return work


def _run(work):
    """(named failures, whether every suite FINISHED) for both subjects' own test suites."""
    named, finished = set(), True
    for basename, arguments in ((P31, ["--self-test"]),
                                # `--fast` omits the six-harness reconstruction, which no row
                                # below touches and which costs more than every row together.
                                (CENSUS, ["--self-test", "--fast"])):
        run = subprocess.run(
            [sys.executable, os.path.join(work, "tools", basename)] + arguments,
            capture_output=True, text=True, cwd=work)
        for line in run.stdout.splitlines():
            if line.startswith("SELFTEST FAIL: "):
                named.add(line[len("SELFTEST FAIL: "):])
        if COMPLETED not in run.stdout:
            finished = False
    return named - EXPECTED_COLLATERAL, finished


def main():
    work = _work_tree()
    try:
        sources = {}
        for _name, path, old, _new in MUTATIONS:
            sources.setdefault(
                path, open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())

        anchors = 0
        for name, path, old, _new in MUTATIONS:
            occurrences = sources[path].count(old)
            if occurrences != 1:
                print("ANCHOR  %-58s occurs %d times in %s, expected 1"
                      % (name[:58], occurrences, path))
                anchors += 1
        if anchors:
            print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), anchors))
            return 1

        baseline, finished = _run(work)
        if baseline or not finished:
            print("BASELINE IS NOT GREEN -- nothing below is a measurement")
            for name in sorted(baseline):
                print("   baseline failure:", name)
            return 2
        print("baseline: green, 0 named failures")

        survivors = 0
        for name, path, old, new in MUTATIONS:
            target = os.path.join(work, "tools", path)
            open(target, "w", encoding="utf-8").write(sources[path].replace(old, new, 1))
            failures, finished = _run(work)
            open(target, "w", encoding="utf-8").write(sources[path])
            killers = sorted(failures - baseline)
            if killers:
                # The name, and NOTHING after it: a `killed-by` row's label is everything past
                # the count, so anything else on the line joins the label and drifts between the
                # census's two arms (`T-306`, the fourth collision).
                print("killed by %d named test(s)  %s" % (len(killers), name))
                for killer in killers[:2]:
                    print("        FAIL: %s" % killer)
            else:
                # A mutation that fails nothing is the FINDING, not a gap in the test list
                # (`C-0161`) -- and a suite that did not start is a fixture defect, which is the
                # same verdict for a different reason and must not read as a killer.
                # A CRASH is not a named test.  A mutation that makes a suite stop is reported
                # here rather than counted as a killer, which is the direction that flatters.
                print("SURVIVED  %s%s" % (name, "" if finished else " (a suite did not finish)"))
                survivors += 1
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
