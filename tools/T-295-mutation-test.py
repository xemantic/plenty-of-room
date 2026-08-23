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
# T-295 -- the fixture-or-corpus census, mutation-tested.
#
#     tools/T-295-mutation-test.py
#
# Every mutation is a WHOLESALE TEXT REPLACEMENT in a throwaway copy of `tools/`, never a widening
# to `original|mutant` (`C-0176`: 9 of 22 rows dead that way).  A mutation that fails NO named
# test is the finding, not a gap in the list (`C-0161`).
#
# `CH-0237` AND `C-0185`: THE UNMUTATED COPY RUNS FIRST and its named failures are subtracted, so
# a `killed` row means *this mutation broke something* rather than *the fixture cannot start*.
#
# THE FIXTURE IS A DEPENDENCY DECLARATION.  `tools/` is copied whole, because the census imports
# `P-31-harness-census.py` and shells out to every harness; `TASKS.md` is copied beside it because
# the reconstruction's own scratch trees are built from this repository's `TASKS.md` and from
# `git show`.  A row that needs the reconstruction is declared in `NEEDS_RECONSTRUCTION`: the
# other rows run `--self-test --fast`, which omits six harness runs they do not target.

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

CENSUS = "T-295-mutation-input-census.py"

MUTATIONS = [
    # --- T-301: the THIRD state, in both directions ---
    (
        "the by-hand state removed — a harness that takes an argument reads as a REFUSAL again, "
        "which is a defect a gate cannot come clean on",
        CENSUS,
        'BY_HAND_USAGE = re.compile(r"^usage:", re.MULTILINE)',
        'BY_HAND_USAGE = re.compile(r"(?!x)x")',
    ),
    (
        "the by-hand state fires on ANY output — every harness reads as by-hand and the census "
        "censuses nothing at all, which is the widening direction",
        CENSUS,
        'BY_HAND_USAGE = re.compile(r"^usage:", re.MULTILINE)',
        'BY_HAND_USAGE = re.compile(r"")',
    ),
    (
        "the usage line need not OPEN a line, so prose mentioning usage excuses a harness",
        CENSUS,
        'BY_HAND_USAGE = re.compile(r"^usage:", re.MULTILINE)',
        'BY_HAND_USAGE = re.compile(r"usage:")',
    ),
    # --- the classification: the four verdicts, one at a time ---
    (
        "CORPUS is never returned — killed in the control is enough, which is the pre-T-295 "
        "reading and the whole defect",
        CENSUS,
        '    if control_kills > 0 and treatment_kills > 0:\n        return "FIXTURE"\n'
        '    if control_kills > 0:\n        return "CORPUS"',
        '    if control_kills > 0:\n        return "FIXTURE"',
    ),
    (
        "a SURVIVOR is classified as FIXTURE, so a mutation nothing holds open reads clean",
        CENSUS,
        '    if treatment_kills > 0:\n        return "REVIVED"\n    return "SURVIVOR"',
        '    if treatment_kills > 0:\n        return "REVIVED"\n    return "FIXTURE"',
    ),
    (
        "a REVIVED row is classified as FIXTURE, so the treatment arm need not be a subset",
        CENSUS,
        '    if treatment_kills > 0:\n        return "REVIVED"',
        '    if treatment_kills > 0:\n        return "FIXTURE"',
    ),
    # --- what counts as the corpus ---
    (
        "the SUBJECT is emptied too — tools/ treated as a committed artifact",
        CENSUS,
        'KEEP_DIRECTORIES = ("tools", "gradle")',
        'KEEP_DIRECTORIES = ("gradle",)',
    ),
    (
        "gpd/ is carved out of the corpus, so the treatment arm keeps the artifacts",
        CENSUS,
        'KEEP_DIRECTORIES = ("tools", "gradle")',
        'KEEP_DIRECTORIES = ("tools", "gradle", "gpd")',
    ),
    (
        "the build files are treated as corpus, so a neutralised tree cannot start a Gradle run",
        CENSUS,
        'KEEP_FILES = ("build.gradle.kts", "settings.gradle.kts", "gradle.properties",\n'
        '              "gradlew", "gradlew.bat")',
        'KEEP_FILES = ()',
    ),
    (
        "a JSON artifact is emptied to nothing, so its reader raises and the whole suite fails "
        "identically in the treatment arm — which the baseline subtraction would then hide",
        CENSUS,
        '    return "{}\\n" if basename.endswith(".json") else ""',
        '    return ""',
    ),
    (
        "build output is copied into the arms, so a harness could tell them apart by it",
        CENSUS,
        "                             if name not in SKIP_DIRECTORIES "
        'and not name.startswith("build-")]',
        "                             if name not in SKIP_DIRECTORIES]",
    ),
    # --- reading a harness's own rows ---
    (
        "the `killed by N` shape demands exactly one space, so one harness's rows vanish",
        CENSUS,
        r'("killed-by", re.compile(r"^killed by\s+(\d+)\s+named test\(s\)\s+(.*)$"), "count-first")',
        r'("killed-by", re.compile(r"^killed by (\d+) named test\(s\)\s+(.*)$"), "count-first")',
    ),
    (
        "the two-suite shape reads only the FIRST count, so a mutation killed only by the "
        "second suite reads as a survivor",
        CENSUS,
        '            if shape == "pair":\n'
        "                rows.append((match.group(3).strip(),\n"
        "                             int(match.group(1)) + int(match.group(2))))",
        '            if shape == "pair":\n'
        "                rows.append((match.group(3).strip(), int(match.group(1))))",
    ),
    (
        "the SURVIVED shape is dropped, so a survivor is not a row at all",
        CENSUS,
        '    ("survived", re.compile(r"^SURVIVED\\s+(.*)$"), "zero"),\n',
        "",
    ),
    (
        "the SURVIVES shape is dropped",
        CENSUS,
        '    ("survives", re.compile(r"^SURVIVES\\s+(.*?)\\s+no named test failed\\s*$"), "zero"),\n',
        "",
    ),
    (
        "the NARROW/WIDEN direction is dropped from the label, so two rows of one table can "
        "collide",
        CENSUS,
        '            elif shape == "kind":\n'
        '                rows.append((match.group(1) + " " + match.group(2).strip(),\n'
        "                             int(match.group(3))))",
        '            elif shape == "kind":\n'
        "                rows.append((match.group(2).strip(), int(match.group(3))))",
    ),
    (
        "the `N of M fail` shape is read count-first, so its label and its count swap",
        CENSUS,
        r'("of-row", re.compile(r"^\s{2}(\S.*?)\s{2,}(\d+) of \d+ fail\s*$"), "label-first")',
        r'("of-row", re.compile(r"^\s{2}(\S.*?)\s{2,}(\d+) of \d+ fail\s*$"), "count-first")',
    ),
    (
        "the `N of M fail` shape loses its indentation guard, so a summary line becomes a row",
        CENSUS,
        r'("of-row", re.compile(r"^\s{2}(\S.*?)\s{2,}(\d+) of \d+ fail\s*$"), "label-first")',
        r'("of-row", re.compile(r"^\s*(\S.*?)\s+(\d+) of \d+ fail\s*$"), "label-first")',
    ),
    (
        "a RETIRED row is not subtracted from the stated count, so a harness that retires one "
        "is refused for a reason that is not there",
        CENSUS,
        "    return total - (int(retired.group(1)) if retired else 0)",
        "    return total",
    ),
    (
        "a harness that states no count reads ZERO rather than None, so every such harness is "
        "refused",
        CENSUS,
        "    if summary is None and coverage is None:\n        return None",
        "    if summary is None and coverage is None:\n        return 0",
    ),
    # --- the reconciliation ---
    (
        "the stated-count cross-check is dropped, so a harness that changes its output silently "
        "loses rows instead of refusing",
        CENSUS,
        "    if stated is not None and stated != len(control_rows):\n"
        '        refusals.append("the harness states %d mutations and this census read %d rows"\n'
        "                        % (stated, len(control_rows)))",
        "    if stated is not None and stated == -1:\n"
        '        refusals.append("unreachable")',
    ),
    (
        "row labels are not compared, so two different tables are paired by position",
        CENSUS,
        "        if label != other:\n"
        '            return [], ["row labels drift between the two arms: %r against %r"\n'
        "                        % (label[:40], other[:40])]",
        "        if False:\n"
        '            return [], ["row labels drift between the two arms: %r against %r"\n'
        "                        % (label[:40], other[:40])]",
    ),
    (
        "an empty control reading is not a refusal, so a harness this census cannot read at all "
        "reports a clean table of nothing",
        CENSUS,
        "    if not control_rows:\n"
        '        refusals.append("the control run printed no per-mutation row this census can read")',
        "    if not control_rows:\n        pass",
    ),
    # --- the gate ---
    (
        "an undeclared CORPUS row stops being a defect — the gate that cannot fail",
        CENSUS,
        '                if key not in declared:\n                    found.append((\n'
        '                        "CORPUS", row["harness"], mutation["label"],',
        '                if False:\n                    found.append((\n'
        '                        "CORPUS", row["harness"], mutation["label"],',
    ),
    (
        "a STALE declaration is not reported, so an exemption outlives what it exempted",
        CENSUS,
        "    for key in declared:\n        if key not in seen:",
        "    for key in []:\n        if key not in seen:",
    ),
    (
        "a SURVIVOR is not a defect of this census either, so an unclassifiable row is silent",
        CENSUS,
        '            elif mutation["verdict"] == "SURVIVOR":\n'
        '                found.append(("SURVIVOR", row["harness"], mutation["label"],',
        '            elif False:\n'
        '                found.append(("SURVIVOR", row["harness"], mutation["label"],',
    ),
    (
        "a REFUSAL is not a defect, so a harness that could not be read passes the gate",
        CENSUS,
        '        for refusal in row["refusals"]:\n'
        '            found.append(("REFUSED", row["harness"], "", refusal))',
        '        for refusal in []:\n'
        '            found.append(("REFUSED", row["harness"], "", refusal))',
    ),
    (
        "--check reports and never gates: the exit code is 0 whatever was found",
        CENSUS,
        "    return 1 if (defect_count and checking) else 0",
        "    return 0",
    ),
    (
        "the exit code is the defect COUNT, which sys.exit truncates modulo 256",
        CENSUS,
        "    return 1 if (defect_count and checking) else 0",
        "    return defect_count if checking else 0",
    ),
    # --- the harness list is P-31's, and is not allowed to become a second copy of it ---
    (
        "the harness list is a hand-written copy that has fallen one behind P-31's",
        CENSUS,
        "    del tree\n    return [row[0] for row in _p31().HARNESSES]",
        "    del tree\n    return [row[0] for row in _p31().HARNESSES][:-1]",
    ),
    (
        "the census reads the TREE for undeclared harnesses as well, so a sibling's in-flight "
        "harness makes this census refuse a file P-31 has already refused",
        CENSUS,
        "    del tree\n    return [row[0] for row in _p31().HARNESSES]",
        "    return ([row[0] for row in _p31().HARNESSES]\n"
        "            + _p31().undeclared_harnesses(tree))",
    ),
    # --- the reconstruction ---
    (
        "the fixture excision no longer asserts its anchors, so it can silently no-op",
        CENSUS,
        "    if gate_source.count(FIXTURE_BLOCK_START) != 1 "
        "or gate_source.count(FIXTURE_BLOCK_END) != 1:\n"
        '        raise AssertionError("the reconstruction\'s fixture-block anchors do not resolve")',
        "    if False:\n"
        '        raise AssertionError("the reconstruction\'s fixture-block anchors do not resolve")',
    ),
    (
        "the historical check reads HEAD instead of a PINNED commit, so it expires the moment "
        "the defect it asserts is repaired",
        CENSUS,
        'RECONSTRUCTION_REF = "7f7957d"',
        'RECONSTRUCTION_REF = "HEAD"',
    ),
    (
        "the PRE-REPAIR queue fixture carries ONE verdict, so the reconstruction's third state "
        "has nothing to discriminate on and the demonstration is vacuous",
        CENSUS,
        '    "| T-1 | a | b | **DONE** (iteration 3) | TODO — **HIGH** |\\n"',
        '    "| T-1 | a | b | — | **DONE** (iteration 3) |\\n"',
    ),
    (
        "the REPAIRED queue fixture carries TWO verdicts, so the first two states stop being "
        "about a repaired queue at all",
        CENSUS,
        '    "| T-1 | a | b | — | **DONE** (iteration 3) |\\n"\n)',
        '    "| T-1 | a | b | **DONE** (iteration 3) | TODO — **HIGH** |\\n"\n)',
    ),
    (
        "the reconstruction driver does not restrict the harness's table, so it runs every "
        "mutation and the one under study is not isolated",
        CENSUS,
        "module.MUTATIONS = rows",
        "module.MUTATIONS = module.MUTATIONS",
    ),
]

# Rows whose named test is the reconstruction: they get the full `--self-test`, the rest get
# `--fast`, which omits six harness runs they do not target.
NEEDS_RECONSTRUCTION = {
    "the reconstruction driver does not restrict the harness's table, so it runs every "
    "mutation and the one under study is not isolated",
}


def _populate(directory):
    """`<tmp>/tools/*.py` beside `<tmp>/TASKS.md` — the layout the census's subjects resolve."""
    tools = os.path.join(directory, "tools")
    os.makedirs(tools)
    for source in os.listdir(TOOLS):
        path = os.path.join(TOOLS, source)
        if source.endswith(".py") and os.path.isfile(path):
            shutil.copy2(path, tools)
    shutil.copy2(os.path.join(ROOT, "TASKS.md"), os.path.join(directory, "TASKS.md"))
    return tools


def _apply(directory, filename, old, new):
    path = os.path.join(directory, "tools", filename)
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    occurrences = text.count(old)
    if occurrences != 1:
        raise AssertionError(
            "anchor occurs %d times in %s: %r" % (occurrences, filename, old[:70]))
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text.replace(old, new))


def _failures(directory, arguments):
    result = subprocess.run(
        [sys.executable, os.path.join(directory, "tools", CENSUS)] + arguments,
        cwd=ROOT, capture_output=True, text=True, timeout=1800)
    lines = [line.strip() for line in (result.stdout + result.stderr).splitlines()
             if line.startswith("SELFTEST FAIL:")]
    if not lines and result.returncode != 0:
        tail = (result.stderr.strip().splitlines() or ["exit %d" % result.returncode])[-1]
        lines = ["SELFTEST FAIL: (raised) %s" % tail]
    return lines


def _arguments(name):
    """The self-test invocation for one row.

    `--repository ROOT` is the fixture's dependency declaration: the reconstruction reads the
    PRE-REPAIR queue out of git, and a scratch copy of `tools/` has no `.git`.  Without it the
    UNMUTATED copy fails *the reconstruction needs git and git is unavailable*, that failure
    enters the baseline, and all three reconstruction rows read SURVIVED for a reason that is not
    about them -- which is exactly what this harness's first run printed.
    """
    base = ["--self-test", "--repository", ROOT]
    return base if name in NEEDS_RECONSTRUCTION else base + ["--fast"]


def _baseline():
    """Named tests that fail in an UNMUTATED copy, at both argument shapes."""
    directory = tempfile.mkdtemp(prefix="T-295-baseline.")
    try:
        _populate(directory)
        return (set(_failures(directory, ["--self-test", "--repository", ROOT, "--fast"]))
                | set(_failures(directory, ["--self-test", "--repository", ROOT])))
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    if argv:
        print("usage: T-295-mutation-test.py")
        return 2
    base = _baseline()
    print("# baseline in an unmutated copy: %d pre-existing failure(s), SUBTRACTED from every "
          "killer count below" % len(base))
    for line in sorted(base):
        print("#   %s" % line[:110])
    survivors = []
    for name, filename, old, new in MUTATIONS:
        directory = tempfile.mkdtemp(prefix="T-295-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            killers = [f for f in _failures(directory, _arguments(name)) if f not in base]
        finally:
            shutil.rmtree(directory, ignore_errors=True)
        if killers:
            print("killed by %2d named test(s)  %s" % (len(killers), name))
            for killer in killers[:2]:
                print("                            %s" % killer[:112])
        else:
            print("SURVIVED                    %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
