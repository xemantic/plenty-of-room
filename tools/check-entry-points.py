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
# P-28 -- every study that writes a committed result file has an `Entry points` row in TASKS.md,
# and every row names a study that exists and the file it actually writes.
#
#     tools/check-entry-points.py             checks the tree, exit 1 on any defect
#     tools/check-entry-points.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. `TASKS.md`'s `## Entry points` table is how a cold session learns to RUN
# anything. It is maintained by hand, one row per study, appended by whoever wrote the study --
# so it decays in exactly the direction that hurts: a study that emits a committed result file
# and has no row cannot be re-run from this repository's own documentation, and a result file
# that cannot be re-run is not a result, it is a fossil.
#
# It was measured before it was written: **99 rows against 123 studies that write a committed
# result file**, and **three of the 24 missing** were the emitters of `P-27`'s own eight red files
# -- so the blocking process task of iteration 39 could not be actioned from `TASKS.md`
# alone. That is the same shape as
# `CLAUDE.md`'s standing finding about the status vocabulary: the failure direction is the costly
# one, and a list maintained by hand needs a check or it is an assertion.
#
# THE THREE DEFECT KINDS, and each is a different mistake:
#
#   MISSING-ROW    a study writes `gpd/results/X.json` and no row runs it
#   UNKNOWN-STUDY  a row names `-Pstudy=a.BKt` and no `a/B.kt` declares `fun main`
#   EMITS-MISMATCH a row's `Emits` column names a file the study does not write
#
# The third is the one a reader cannot see: `CLAUDE.md` records that *a defect's LOCATION is a
# number like any other*, and a study's own INPUT path is the commonest wrong answer. Neither end
# of the source is safe -- ten of the twenty-four studies this check was written for read
# `gpd/results/T-3b-...json` BEFORE writing their own, and two (`T-157`, `T-16`) read an upstream
# file a hundred lines AFTER their own `writeText`. A first-match census gets ten wrong and a
# last-match census gets two wrong, both silently and both in the shape of a real finding: the
# first draft of this checker reported those two as EMITS-MISMATCH against rows that are correct.
# So the write path is found by following the BINDING to its `.writeText`, which is the only
# reading that is about writing at all.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

_MAIN = re.compile(r"^fun main\b", re.MULTILINE)
# THE SCOPE IS `gpd/results/`, AND THAT IS A DECISION RATHER THAN AN OVERSIGHT. `T-266` opened a
# second committed-artifact directory, `gpd/designs/`, within an hour of this checker shipping, and
# widening to cover it was tried and REVERTED: a design's staleness is caught by
# `CommittedDesignsTest`, which rebuilds the file and compares it byte for byte, and that is a
# strictly stronger guarantee than a row saying how to run something. A row answers *how do I
# re-run this*; a rebuild-and-compare test answers *is this file what the code produces*. Where the
# second exists the first is documentation, and `gpd/designs/README.md` carries it.
#
# There is a second reason not to widen, and it is the one `CLAUDE.md` already records: that
# emitter assembles its path from a directory constant and a name (`"$DESIGN_DIRECTORY/....sc"`),
# which is invisible to a search for either half — the same shape that made `C-0073`'s reader audit
# miss two thirds of its graph. A checker that silently cannot see a whole emission style should
# not claim that style as its scope.
_ARTIFACT = re.compile(r'"(gpd/results/[^"]+\.json)"')
# `val output = File(` -- the binding's HEAD only. The argument list is then walked to its matching
# parenthesis, because a study may take an output override from the command line and put its own
# committed path on an `else` branch three lines down:
#
#     val output = File(
#         if (arguments.isNotEmpty()) arguments[0]
#         else "gpd/results/T-221-planar-coupling-wall.json"
#     )
#
# A single-line pattern reads that study as emitting NOTHING, which is the silent direction: it is
# then owed no row and the gate cannot see it. One live case, found by reading the exemption list
# rather than the defect list — the exemptions are where a checker's false negatives collect.
_BINDING_HEAD = re.compile(r'\bva[lr]\s+([A-Za-z_][A-Za-z0-9_]*)\s*=\s*File\(')


def _bindings(source):
    """[(name, result path)] for every `val <name> = File(... "gpd/results/X.json" ...)`.

    The last results literal inside the argument list wins, which is the `else` default of a
    command-line override and the plain path of everything else.
    """
    found = []
    for match in _BINDING_HEAD.finditer(source):
        depth, index = 1, match.end()
        while index < len(source) and depth:
            depth += {"(": 1, ")": -1}.get(source[index], 0)
            index += 1
        paths = _ARTIFACT.findall(source[match.end():index])
        if paths:
            found.append((match.group(1), paths[-1]))
    return found


# An `Entry points` row: the study on the Gradle command line, and the result file it claims.
# The middle cell is `[^|]*` rather than `[^`|]*` on purpose -- several rows carry TWO backticked
# task tags (`T-75` / `T-78`), and a stricter cell silently dropped three rows, which then read as
# three MISSING-ROW defects. A checker's own regex is the first thing its cheap bound must test.
_ROW = re.compile(r"-Pstudy=([A-Za-z0-9_.]+)Kt`\s*\|[^|]*\|\s*`(gpd/[^`]+)`")


def written_result(source):
    """The result path a study source writes, or None if it writes none.

    Follows the BINDING to its `.writeText`, and falls back to the last results path only when no
    binding is written. The naive "last `File(...)` in the source" rule is wrong in both
    directions here: ten studies read `T-3b` before writing their own (so the FIRST match is an
    input), and two studies read an upstream file AFTER writing their own (so the LAST match is
    an input too). `T-157` reads `T-149` a hundred lines below its own `writeText`.
    """
    bindings = _bindings(source)
    written = [name for name, _ in bindings
               if re.search(r"\b" + re.escape(name) + r"\s*\.\s*writeText\b", source)]
    if written:
        return dict(bindings)[written[-1]]
    matches = re.findall(r'File\("(gpd/results/[^"]+\.json)"', source)
    return matches[-1] if matches else None


def studies(root=ROOT):
    """{fully-qualified study name: written result path or None} over src/main/kotlin."""
    found = {}
    base = os.path.join(root, "src", "main", "kotlin")
    for directory, _, names in os.walk(base):
        for name in names:
            if not name.endswith(".kt"):
                continue
            path = os.path.join(directory, name)
            with open(path, encoding="utf-8") as handle:
                source = handle.read()
            if not _MAIN.search(source):
                continue
            relative = os.path.relpath(path, base)[: -len(".kt")]
            found[relative.replace(os.sep, ".")] = written_result(source)
    return found


def entry_point_rows(text):
    """[(study, emitted path)] for every `Entry points` row in a TASKS.md body."""
    return [(match.group(1), match.group(2)) for match in _ROW.finditer(text)]


def defects(text, found):
    """[(kind, study, detail)] -- the whole check, as data, so that it can be named-tested."""
    rows = entry_point_rows(text)
    listed = {}
    reported = []
    for study, emits in rows:
        listed.setdefault(study, emits)
        if study not in found:
            reported.append(("UNKNOWN-STUDY", study, "no src/main/kotlin source declares fun main"))
        elif found[study] is not None and emits != found[study]:
            reported.append(("EMITS-MISMATCH", study,
                             "row says {}, source writes {}".format(emits, found[study])))
    for study, emits in sorted(found.items()):
        # A study that writes no result file is not an entry point this table is about --
        # `HelloWorldApp` is the live case, and a study whose whole output is stdout would be too.
        if emits is not None and study not in listed:
            reported.append(("MISSING-ROW", study, emits))
    return reported


def _selftest():
    failures = []

    def check(name, actual, expected):
        ok = actual == expected
        print("{} {}".format("ok  " if ok else "FAIL", name))
        if not ok:
            print("     expected {!r}\n     actual   {!r}".format(expected, actual))
            failures.append(name)

    row = ("| `./gradlew study -Pstudy=tile.FourLayerTileStudyKt` | `T-191` | "
           "`gpd/results/T-191-four-layer-tile.json` |")

    check("a well-formed row parses into its study and its file",
          entry_point_rows(row),
          [("tile.FourLayerTileStudy", "gpd/results/T-191-four-layer-tile.json")])
    check("a row with a run time beside the file still parses",
          entry_point_rows("| `./gradlew study -Pstudy=brush.ScfDensityProfileStudyKt` | `T-1d` | "
                           "`gpd/results/T-1d-scf-density-profile.json` (~33 min) |"),
          [("brush.ScfDensityProfileStudy", "gpd/results/T-1d-scf-density-profile.json")])
    check("prose that merely mentions a study is not a row",
          entry_point_rows("run `./gradlew study -Pstudy=tile.FourLayerTileStudyKt` to reproduce it"),
          [])

    # The write path is the LAST results path, not the first: ten of the twenty-four studies this
    # checker was written for read `T-3b` before writing their own, and a first-match census gets
    # every one of them wrong in the same direction.
    check("the written path is the one that is written, not the first one read",
          written_result('val edge = File("gpd/results/T-3b-tile-edge-load-profile.json")\n'
                         'val output = File("gpd/results/T-199-cross-section-comparison.json")\n'
                         'output.writeText(json)'),
          "gpd/results/T-199-cross-section-comparison.json")
    # The live case that killed the "last match" rule: `T-157` reads `T-149` a hundred lines
    # BELOW its own `writeText`, so the last results path in the source is an input.
    check("nor the last one read, when a study reads an upstream file after writing its own",
          written_result('val output = File("gpd/results/T-157-large-rotation-arm-branch.json")\n'
                         'output.writeText(json)\n'
                         'val file = File("gpd/results/T-149-recommended-element-fold.json")'),
          "gpd/results/T-157-large-rotation-arm-branch.json")
    check("a study that writes no result file writes none",
          written_result('fun main() { println("hello") }'), None)
    # The live false NEGATIVE, found in the exemption list rather than the defect list: a study
    # that takes an output override from the command line puts its own committed path on an
    # `else` branch, and a single-line binding pattern reads it as emitting nothing at all.
    check("a binding whose path is on an `else` branch three lines down is still a write",
          written_result('val output = File(\n'
                         '    if (arguments.isNotEmpty()) arguments[0]\n'
                         '    else "gpd/results/T-221-planar-coupling-wall.json"\n'
                         ')\n'
                         'output.writeText(json)'),
          "gpd/results/T-221-planar-coupling-wall.json")
    check("a row whose task cell carries two tags still parses",
          entry_point_rows("| `./gradlew study -Pstudy=anchoring.FlexureMountingSenseStudyKt` | "
                           "`T-75` / `T-78` | `gpd/results/T-75-flexure-mounting-sense.json` (~3 s) |"),
          [("anchoring.FlexureMountingSenseStudy",
            "gpd/results/T-75-flexure-mounting-sense.json")])

    found = {
        "tile.FourLayerTileStudy": "gpd/results/T-191-four-layer-tile.json",
        "tile.ObliqueRootStudy": "gpd/results/T-206-oblique-root.json",
        "HelloWorldApp": None,
    }
    check("a complete table is clean",
          defects(row + "\n| `./gradlew study -Pstudy=tile.ObliqueRootStudyKt` | `T-206` | "
                        "`gpd/results/T-206-oblique-root.json` |", found),
          [])
    check("a study with no row is MISSING-ROW",
          defects(row, found),
          [("MISSING-ROW", "tile.ObliqueRootStudy", "gpd/results/T-206-oblique-root.json")])
    check("a study that emits nothing is NOT owed a row",
          [d for d in defects(row, found) if d[1] == "HelloWorldApp"], [])
    check("a row naming a study that does not exist is UNKNOWN-STUDY",
          defects("| `./gradlew study -Pstudy=tile.GhostStudyKt` | `T-0` | "
                  "`gpd/results/T-0-ghost.json` |",
                  {"tile.FourLayerTileStudy": "gpd/results/T-191-four-layer-tile.json"}),
          [("UNKNOWN-STUDY", "tile.GhostStudy", "no src/main/kotlin source declares fun main"),
           ("MISSING-ROW", "tile.FourLayerTileStudy", "gpd/results/T-191-four-layer-tile.json")])
    check("a row naming the study's INPUT file is EMITS-MISMATCH",
          defects("| `./gradlew study -Pstudy=tile.CrossSectionComparisonStudyKt` | `T-199` | "
                  "`gpd/results/T-3b-tile-edge-load-profile.json` |",
                  {"tile.CrossSectionComparisonStudy":
                   "gpd/results/T-199-cross-section-comparison.json"}),
          [("EMITS-MISMATCH", "tile.CrossSectionComparisonStudy",
            "row says gpd/results/T-3b-tile-edge-load-profile.json, source writes "
            "gpd/results/T-199-cross-section-comparison.json")])
    # A study may legitimately be listed twice (two tasks re-ran it); the first row wins and the
    # duplicate must not be reported as a defect of its own.
    check("a study listed twice is not a defect",
          defects(row + "\n" + row, {"tile.FourLayerTileStudy":
                                     "gpd/results/T-191-four-layer-tile.json"}),
          [])

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        text = handle.read()
    found = studies()
    reported = defects(text, found)
    for kind, study, detail in reported:
        print("TASKS.md\t{}\t{}\t{}".format(kind, study, detail))
    emitting = sum(1 for path in found.values() if path is not None)
    sys.stdout.flush()
    print("# {} defect(s); {} of {} studies emit a result file, {} rows".format(
        len(reported), emitting, len(found), len(entry_point_rows(text))), file=sys.stderr)
    return 1 if reported else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
