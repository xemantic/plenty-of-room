#!/usr/bin/env python3
"""T-307 — the mutation test for route B's PER-TURN uniform-raster tether census.

`C-0161`'s standard, applied to a Kotlin subject: **every mutation must fail a NAMED test**, and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`: a harness
whose baseline is broken reports every row `killed` or every row `SURVIVES` off one and the same
unrelated error, and no reading of the table can see it).

Two further checks this harness owes and makes:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file it mutates.
    `C-0190` records a stray copy of a main source inside the *test* source set making every
    mutation of the original invisible — the tell being a green baseline plus every mutation of
    one file surviving.
  * every mutation's anchor must occur **exactly once** in its subject, so a refactor that moves
    the text orphans the row loudly instead of silently (`C-0185`).

It is the FOURTH Kotlin-subject harness in this corpus, after `T-297`'s, `T-299`'s and `T-304`'s,
and it is declared in `tools/P-31-harness-census.py`'s own table with the same
`id_file_old_new_what` adapter shape and the same `BY-HAND` sentinel: one mutation is one Gradle
`test` run, so it is wired on its own Gradle task and deliberately NOT reachable from `:test`.

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed, so
a document gate added tomorrow is excluded by construction (`C-0194`, `T-297`).

Usage:
    tools/T-307-mutation-test.py <snapshot-dir>

It edits files inside <snapshot-dir> and restores them afterwards. Give it a snapshot, never the
checkout.
"""
import glob
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

TESTS = "*UniformRasterTetherSpansTest*"

SPANS = "src/main/kotlin/tile/UniformRasterTetherSpans.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SPANS,
     "                    if (turns[k].atHighEnd) highRimNucleotides else lowRimNucleotides,",
     "                    if (turns[k].atHighEnd) lowRimNucleotides else highRimNucleotides,",
     "the two rim chains are exchanged, so a high-rim turn takes the low rim's nucleotide count"),
    ("M02", SPANS,
     "    val turnsInsideTheAlignedHalf: Int get() = spans.count { it < interhelicalDistance }",
     "    val turnsInsideTheAlignedHalf: Int get() = spans.count { it > interhelicalDistance }",
     "the aligned-half criterion is inverted, so the census disagrees with T-304's own"),
    ("M03", SPANS,
     "        require(classZeroResidue in 0 until UNIFORM_RASTER_PERIOD) {",
     "        require(classZeroResidue in -100 until UNIFORM_RASTER_PERIOD) {",
     "a lattice phase outside the 21 bp period is admitted"),
    ("M04", SPANS,
     "        require(pairedRowBasePairs > 0) {",
     "        require(pairedRowBasePairs > -100) {",
     "a non-positive paired row length is admitted"),
    ("M05", SPANS,
     "                node = tie.node,\n                secantStiffness = state.secantStiffness,",
     "                node = 0,\n                secantStiffness = state.secantStiffness,",
     "every tether is put at the low rim node, so a high-rim turn sits at the wrong end"),
    ("M06", SPANS,
     "                tension = if (withPreload) states[k].tension else 0.0",
     "                tension = states[k].tension",
     "the preload cannot be dropped, so no influence function can be taken (C-0104)"),
    ("M07", SPANS,
     "                tension = if (k == turnIndex) 1.0 else 0.0",
     "                tension = 1.0",
     "a bank column loads every turn at once, so the triangle-inequality ceiling is not a "
     "per-turn influence at all"),
    ("M08", SPANS,
     "            senseTwoBasePairs = pairedRowBasePairs,",
     "            senseTwoBasePairs = pairedRowBasePairs + 1,",
     "the raster stops being uniform, so the span census is of a different design"),
    ("M09", SPANS,
     "                secantStiffness = state.secantStiffness,\n"
     "                tangentStiffness = state.tangentStiffness,",
     "                secantStiffness = state.tangentStiffness,\n"
     "                tangentStiffness = state.secantStiffness,",
     "the chain's transverse and along-chain stiffnesses are exchanged"),
    ("M10", SPANS,
     "            anchorOffsetBasePairs = anchorOffsetBasePairs,",
     "            anchorOffsetBasePairs = 0,",
     "the anchor offset stops being carried into the azimuth census"),
    ("M11", SPANS,
     "    val turns: List<HoneycombRasterTurn> = honeycombRasterTurnList(block, firstAxialSign)",
     "    val turns: List<HoneycombRasterTurn> = honeycombRasterTurnList(block, 1)",
     "the traversal sense stops reaching the turn census, so which rim a turn is at is fixed"),
    ("M12", SPANS,
     "            val state = if (stiffness == null) states[k] else states[k]"
     ".withStiffness(stiffness)",
     "            val state = states[k]",
     "a stiffness override is ignored, so the bank's lattice is not the untied one"),
]


def gradle_exclusions(root):
    """The `-x` flags, DERIVED from `build.gradle.kts`'s own `dependsOn` block."""
    text = open(os.path.join(root, "build.gradle.kts")).read()
    start = text.index('tasks.named("test") {')
    block = text[start:text.index("}\n\ndependencies", start)]
    block = re.sub(r"//[^\n]*", "", block)
    flags = []
    for name in re.findall(r'"([A-Za-z0-9_]+)"', block):
        if name != "test":
            flags += ["-x", name]
    return flags


def failing_tests(root):
    """The NAMED tests that failed, from the JUnit XML the run leaves behind."""
    names = set()
    for path in glob.glob(os.path.join(root, "build/test-results/test/*.xml")):
        for case in ET.parse(path).getroot().iter("testcase"):
            if case.find("failure") is not None or case.find("error") is not None:
                names.add(case.get("name"))
    return names


def run(root):
    for path in glob.glob(os.path.join(root, "build/test-results/test/*.xml")):
        os.remove(path)
    completed = subprocess.run(
        ["./gradlew", "test", "--tests", TESTS] + gradle_exclusions(root) + ["--console=plain"],
        cwd=root, capture_output=True, text=True,
    )
    return completed.returncode, failing_tests(root)


def main():
    if len(sys.argv) != 2:
        # LOWER CASE and at the start of a line, because `tools/T-295-mutation-input-census.py`
        # DERIVES its `BY HAND` third state from `^usage:`; a capitalised `Usage:` does not match
        # it and the census would read this harness as a REFUSAL (`T-305`, `T-306`).
        print("usage: tools/T-307-mutation-test.py <snapshot-dir>", file=sys.stderr)
        print(__doc__)
        return 2
    root = os.path.abspath(sys.argv[1])
    if os.path.exists(os.path.join(root, ".git")):
        print("REFUSED: give this harness a SNAPSHOT, never the checkout")
        return 2

    # C-0190's stray-copy trap: a second copy of a mutated source makes every mutation invisible.
    for target in {m[1] for m in MUTATIONS}:
        base = os.path.basename(target)
        found = [p for p in glob.glob(os.path.join(root, "src/**", base), recursive=True)]
        if len(found) != 1:
            print("REFUSED: " + base + " resolves to " + str(len(found)) + " paths: " + str(found))
            return 2

    # C-0185's anchor check: an orphaned anchor is a silent no-op.
    for identifier, target, old, _new, _what in MUTATIONS:
        text = open(os.path.join(root, target)).read()
        if text.count(old) != 1:
            print("REFUSED: " + identifier + "'s anchor occurs " + str(text.count(old)) +
                  " times in " + target)
            return 2

    # CH-0237: the UNMUTATED copy first, and its failures subtracted.
    baseline_code, baseline_failures = run(root)
    print("baseline: exit " + str(baseline_code) + ", " + str(len(baseline_failures)) +
          " named failure(s) " + str(sorted(baseline_failures)))
    if baseline_code != 0 and not baseline_failures:
        print("REFUSED: the unmutated copy failed with no NAMED test failure; nothing below "
              "would be a measurement")
        return 2

    survivors = []
    for identifier, target, old, new, what in MUTATIONS:
        path = os.path.join(root, target)
        shutil.copyfile(path, path + ".orig")
        try:
            text = open(path).read()
            open(path, "w").write(text.replace(old, new))
            code, failures = run(root)
            killers = sorted(failures - baseline_failures)
            if killers:
                print(identifier + "  killed by " + str(len(killers)) + " named test(s)")
                for killer in killers[:2]:
                    print("      " + killer[:110])
                print("      [" + what + "]")
            else:
                survivors.append(identifier)
                print(identifier + "  SURVIVED (exit " + str(code) + ")")
                print("      [" + what + "]")
        finally:
            shutil.move(path + ".orig", path)

    print("# " + str(len(MUTATIONS)) + " mutation(s), " + str(len(survivors)) + " survivor(s) " +
          str(survivors))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main())
