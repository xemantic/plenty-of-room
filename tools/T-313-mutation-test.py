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
# T-313 -- MUTATION COVERAGE FOR THE WIDENED LINK PREDICATE AND ITS SCOPE LINE.
#
#     tools/T-313-mutation-test.py
#
# `C-0176`'s standard, in BOTH directions: every rule must fail a NAMED test when it is reverted
# AND when it is over-widened.  A table that only ever narrows becomes a pattern, which is what a
# per-rule judgement refuses.  Ten of the rows below revert a rule and eight over-correct it; `T-317` adds twenty-two
# more over the FILE SET, the derived destination and the argument guard, in the same two directions.
#
# `C-0185`/`CH-0237`'s baseline runs FIRST and refuses on a red one: without it a fixture defect
# reads as `0 survivors` (the quiet direction) or as `N of N` (the loud one), and the headline
# column means nothing either way.  The anchor count is asserted at 1 per mutation, which is
# `C-0185`'s other half -- a harness that does not assert its anchors reports `killed` off a
# mutation that never applied.
#
# THE FIXTURE LAYOUT IS A DEPENDENCY DECLARATION (`C-0195`), and this subject needs a bigger one
# than a `tools/`-only copy.  `tools/check-corpus-links.py --selftest` reads the live tree at
# eleven assertions -- a claim slug that exists, one that does not, `TASKS.md`, the five root
# documents, a `.py` target under `tools/`, a directory target -- so the work tree carries
# `tools/` wholesale, every `.md` under `gpd/`, and every root `.md`.  It carries nothing else:
# `src/`, `gpd/data/`'s 91 MB of sources and the build tree are not read by any named test.  Those
# eleven assertions survive `tools/T-295-mutation-input-census.py`'s TREATMENT arm, which EMPTIES
# a corpus file rather than removing it, so an existence check still holds there.
"""Mutation coverage for T-313's widening and T-317's file set, in check-corpus-links.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "check-corpus-links.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject, or the row is a
#: reference into a source that has been refactored underneath it and reports nothing.
MUTATIONS = (
    # --- the widening itself, both directions ---------------------------------------------------
    (
        "the link pattern is narrowed back to `.md` targets only",
        SUBJECT,
        r'_LINK = re.compile(r"\]\(([^)\s]+?)(?:#[^)]*)?\)")',
        r'_LINK = re.compile(r"\]\(([^)\s]+?\.md)(?:#[^)]*)?\)")',
    ),
    (
        "the link pattern is widened until a target may contain whitespace",
        SUBJECT,
        r'_LINK = re.compile(r"\]\(([^)\s]+?)(?:#[^)]*)?\)")',
        r'_LINK = re.compile(r"\]\(([^)]+?)(?:#[^)]*)?\)")',
    ),
    (
        "the anchor fragment stops being stripped from a target",
        SUBJECT,
        r'_LINK = re.compile(r"\]\(([^)\s]+?)(?:#[^)]*)?\)")',
        r'_LINK = re.compile(r"\]\(([^)\s]+?)\)")',
    ),
    # --- the three guards the widening rests on -------------------------------------------------
    (
        "the anchor-only guard the widening needs is dropped",
        SUBJECT,
        'if "://" in link or link.startswith("/") or link.startswith("#"):',
        'if "://" in link or link.startswith("/"):',
    ),
    (
        "the external-URL guard is dropped",
        SUBJECT,
        'if "://" in link or link.startswith("/") or link.startswith("#"):',
        'if link.startswith("/") or link.startswith("#"):',
    ),
    (
        "the absolute-path guard is dropped",
        SUBJECT,
        'if "://" in link or link.startswith("/") or link.startswith("#"):',
        'if "://" in link or link.startswith("#"):',
    ),
    (
        "the guards are widened until every relative link is skipped",
        SUBJECT,
        'if "://" in link or link.startswith("/") or link.startswith("#"):',
        'if True:',
    ),
    # --- code blanking, which is what makes the placeholder class a non-defect ------------------
    (
        "an inline code span stops being blanked",
        SUBJECT,
        "return _CODE_SPAN.sub(blank, _FENCE.sub(blank, text))",
        "return _FENCE.sub(blank, text)",
    ),
    (
        "a fenced block stops being blanked",
        SUBJECT,
        "return _CODE_SPAN.sub(blank, _FENCE.sub(blank, text))",
        "return _CODE_SPAN.sub(blank, text)",
    ),
    # --- the scope line's three shape patterns, both directions ---------------------------------
    (
        "the titled-link shape stops being recognised",
        SUBJECT,
        r'_TITLED = re.compile(r"\]\([^)\s]+\s[^)]*\)")',
        r'_TITLED = re.compile(r"(?!x)x")',
    ),
    (
        "the titled-link shape is widened to every inline link",
        SUBJECT,
        r'_TITLED = re.compile(r"\]\([^)\s]+\s[^)]*\)")',
        r'_TITLED = re.compile(r"\]\([^)]*\)")',
    ),
    (
        "the angle-bracket shape stops being recognised",
        SUBJECT,
        r'_ANGLE_BRACKET = re.compile(r"\]\(<[^>\n]*>\)")',
        r'_ANGLE_BRACKET = re.compile(r"(?!x)x")',
    ),
    (
        "the reference-style shape stops being recognised",
        SUBJECT,
        r'_REFERENCE_STYLE = re.compile(r"\]\[[^\]\n]+\]")',
        r'_REFERENCE_STYLE = re.compile(r"(?!x)x")',
    ),
    (
        "the shape census reads the raw text instead of the code-blanked text",
        SUBJECT,
        "    stripped = _without_code(text)",
        "    stripped = text",
    ),
    # --- the unscanned-file residue, both directions ---------------------------------------------
    (
        "the unscanned residue stops subtracting what the checker does scan",
        SUBJECT,
        "            if relative not in scanned:\n                found.append(relative)",
        "            found.append(relative)",
    ),
    (
        "the unscanned residue is narrowed to nothing",
        SUBJECT,
        "            if relative not in scanned:\n                found.append(relative)",
        "            if False:\n                found.append(relative)",
    ),
    (
        "the scope note stops carrying the unscanned count",
        SUBJECT,
        '                len(unscanned), ", ".join(directories),',
        '                0, ", ".join(directories),',
    ),
    # --- the history measurement's own two predicates, both directions ---------------------------
    (
        "the history scope drops the root documents",
        SUBJECT,
        '    return path.endswith(".md") and ("/" not in path or path.startswith("gpd/"))',
        '    return path.endswith(".md") and path.startswith("gpd/")',
    ),
    (
        "the history scope is widened to every Markdown file in the tree",
        SUBJECT,
        '    return path.endswith(".md") and ("/" not in path or path.startswith("gpd/"))',
        '    return path.endswith(".md")',
    ),
    (
        "the history sweep stops being able to isolate the class the widening ADDS",
        SUBJECT,
        '            if only_non_markdown and link.endswith(".md"):\n                continue',
        '            if False:\n                continue',
    ),
    # --- the output, which no return value can hold open ------------------------------------------
    (
        "the run stops printing its scope beside the count",
        SUBJECT,
        "    print(scope_note(root, shapes=shapes, non_markdown=non_markdown), file=sys.stderr)",
        "    pass",
    ),
    (
        "a broken link stops failing the run",
        SUBJECT,
        "    return 1 if defects else 0",
        "    return 0",
    ),
    # --- `T-317`: THE FILE SET, and the DERIVED destination that makes it cheap -----------------
    # Every rule below is mutated in BOTH directions. The row that matters most is the last pair:
    # the destination rule is the whole reason the widening is not a 100 % false-positive gate,
    # and a mutation that removes it must not pass silently.
    (
        "the excluded root stops being excluded, so the problem definition is scanned",
        SUBJECT,
        '_EXCLUDED_ROOTS = ("third-party",)',
        '_EXCLUDED_ROOTS = ()',
    ),
    (
        "the excluded roots are widened until nothing is relocatable",
        SUBJECT,
        '_EXCLUDED_ROOTS = ("third-party",)',
        '_EXCLUDED_ROOTS = ("third-party", "tools")',
    ),
    (
        "the partition of the unscanned set is inverted",
        SUBJECT,
        "            if relative.split(os.sep)[0] not in _EXCLUDED_ROOTS]",
        "            if relative.split(os.sep)[0] in _EXCLUDED_ROOTS]",
    ),
    (
        "the candidate directories collapse to the repository root",
        SUBJECT,
        "    found = [\"\"]\n    for base, directories, _files in os.walk(root):",
        "    found = [\"\"]\n    for base, directories, _files in []:",
    ),
    (
        "the candidate directories are widened to version control and build output",
        SUBJECT,
        "        directories[:] = [name for name in directories\n"
        "                          if name not in _NOT_A_CORPUS and not name.startswith(\"build\")]\n"
        "        for name in directories:\n"
        "            found.append(os.path.relpath(os.path.join(base, name), root))",
        "        for name in directories:\n"
        "            found.append(os.path.relpath(os.path.join(base, name), root))",
    ),
    (
        "a destination need only resolve SOME of the file's links, not all of them",
        SUBJECT,
        "            if all(present(os.path.normpath(os.path.join(directory, link))) for link in links)]",
        "            if any(present(os.path.normpath(os.path.join(directory, link))) for link in links)]",
    ),
    (
        "a file carrying no links stops keeping its own directory",
        SUBJECT,
        "    if not links:\n        return own_directory\n    resolving = resolving_directories(links, directories, present)",
        "    resolving = resolving_directories(links, directories, present)",
    ),
    (
        "the file's OWN directory stops winning over a derived one",
        SUBJECT,
        "    if own_directory in resolving:\n        return own_directory\n",
        "",
    ),
    (
        "a link resolving NOWHERE stops removing the file's destination",
        SUBJECT,
        "    return resolving[0] if resolving else None",
        "    return resolving[0] if resolving else own_directory",
    ),
    (
        "the diagnostic's tie-break stops preferring the file's own directory",
        SUBJECT,
        "        ordered = ([own] if own in candidates else []) + [d for d in sorted(candidates) if d != own]",
        "        ordered = sorted(candidates)",
    ),
    (
        "the in-place reading the measurement REFUSES becomes the only reading",
        SUBJECT,
        "        candidates = [own] if in_place else directories",
        "        candidates = [own]",
    ),
    (
        "the in-place reading becomes unreachable, so the refusal cannot be re-run",
        SUBJECT,
        "        candidates = [own] if in_place else directories",
        "        candidates = directories",
    ),
    (
        "a failing file names every link instead of only those failing at its best directory",
        SUBJECT,
        "        best = max(ordered,",
        "        best = min(ordered,",
    ),
    (
        "the scope line stops carrying the relocatable link count",
        SUBJECT,
        "                len(census), sum(links for _path, links, _where in census),",
        "                0, 0,",
    ),
    (
        "the scope line stops naming the excluded root",
        SUBJECT,
        '                len(excluded), ", ".join(_EXCLUDED_ROOTS),',
        '                0, ", ".join(_EXCLUDED_ROOTS),',
    ),
    (
        "the history's relocatable scope stops excluding the problem definition",
        SUBJECT,
        '    return (path.endswith(".md") and not _in_scanned_scope(path)\n'
        '            and path.split("/")[0] not in _EXCLUDED_ROOTS)',
        '    return path.endswith(".md") and not _in_scanned_scope(path)',
    ),
    (
        "the history's relocatable scope is widened to the corpus set as well",
        SUBJECT,
        '    return (path.endswith(".md") and not _in_scanned_scope(path)\n'
        '            and path.split("/")[0] not in _EXCLUDED_ROOTS)',
        '    return path.endswith(".md")',
    ),
    (
        "a dangling link in a relocatable file stops failing the run",
        SUBJECT,
        '        print("{}\\tBROKEN-LINK\\t{}\\t-> resolves against no directory of the repository"\n'
        '              .format(relative, link))\n        defects += 1',
        '        print("{}\\tBROKEN-LINK\\t{}\\t-> resolves against no directory of the repository"\n'
        '              .format(relative, link))',
    ),
    (
        "the retained census stops reporting the relocatable files",
        SUBJECT,
        '        for relative, links, where in relocatable_census(root):',
        '        for relative, links, where in []:',
    ),
    (
        "an unrecognised argument stops being refused, so a wired call can be green and inert",
        SUBJECT,
        "    unrecognised = [argument for argument in argv if argument not in _FLAGS]",
        "    unrecognised = []",
    ),
    (
        "the refusal is widened until anything that merely LOOKS like a flag is accepted",
        SUBJECT,
        "    unrecognised = [argument for argument in argv if argument not in _FLAGS]",
        '    unrecognised = [argument for argument in argv if not argument.startswith("--")]',
    ),
    (
        "the retained census starts GATING, which is the one thing an audit must not do",
        SUBJECT,
        '            print("{}\\tEXCLUDED\\t-\\t-> the problem definition as received".format(relative))\n'
        '        return 0',
        '            print("{}\\tEXCLUDED\\t-\\t-> the problem definition as received".format(relative))\n'
        '        return 1',
    ),
)


def _fixture(work):
    """The declared copy: `tools/` wholesale, every `.md` under `gpd/`, every root `.md`."""
    shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
    for name in sorted(os.listdir(ROOT)):
        if name.endswith(".md") and os.path.isfile(os.path.join(ROOT, name)):
            shutil.copy2(os.path.join(ROOT, name), os.path.join(work, name))
    for base, directories, files in os.walk(os.path.join(ROOT, "gpd")):
        directories[:] = [entry for entry in directories if entry != "__pycache__"]
        for name in files:
            if not name.endswith(".md"):
                continue
            source = os.path.join(base, name)
            target = os.path.join(work, os.path.relpath(source, ROOT))
            os.makedirs(os.path.dirname(target), exist_ok=True)
            shutil.copy2(source, target)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", SUBJECT), "--selftest"],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[5:].split(":")[0] for line in run.stdout.splitlines() if line.startswith("FAIL ")]
    return run.returncode, named


def main():
    work = tempfile.mkdtemp(prefix="T-313-mutation.")
    try:
        _fixture(work)
        # `CH-0237`.  The local is NAMED for what it is, because `tools/P-31-harness-census.py`
        # DERIVES `measuresBaseline` from a harness's own identifiers and is deliberately blind to
        # comments and strings.
        baseline_code, baseline_failures = _run(work)
        if baseline_code != 0:
            print("BASELINE IS NOT GREEN -- nothing below is a measurement")
            for name in baseline_failures:
                print("   baseline failure:", name)
            return 2
        print("baseline: green, 0 named failures")
        source = open(os.path.join(ROOT, "tools", SUBJECT), encoding="utf-8").read()
        survivors = 0
        for name, _path, old, _new in MUTATIONS:
            count = source.count(old)
            if count != 1:
                print("ANCHOR  %-70s occurs %d times, expected 1" % (name, count))
                survivors += 1
        for name, _path, old, new in MUTATIONS:
            if source.count(old) != 1:
                continue
            target = os.path.join(work, "tools", SUBJECT)
            open(target, "w", encoding="utf-8").write(source.replace(old, new, 1))
            code, named = _run(work)
            open(target, "w", encoding="utf-8").write(source)
            named = [failing for failing in named if failing not in baseline_failures]
            if code == 0 or not named:
                survivors += 1
                print("%-8s %-70s %s" % ("SURVIVES", name, "no named test failed"))
            else:
                # `T-306`/`C-0206`.  The count is printed and the killers go on their OWN
                # continuation lines: a harness's output is an INTERFACE that
                # `tools/T-295-mutation-input-census.py` reads, the `killed-by` shape captures
                # everything after the count as the LABEL, and anything printed after the name
                # makes the label drift between the census's two arms.
                print("killed by %d named test(s)  %s" % (len(named), name))
                for failing in named[:3]:
                    print("        FAIL: %s" % failing)
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
