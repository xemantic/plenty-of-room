#!/usr/bin/env python3
"""T-304 — the mutation test for the raster-turn ANCHOR AZIMUTH derivation.

`C-0161`'s standard, applied to Kotlin rather than to a Python predicate: **every mutation must
fail a NAMED test**, and the UNMUTATED copy is run first so that its own failures are subtracted
(`CH-0237`: a harness whose baseline is broken reports every row `killed` or every row `SURVIVES`
off one and the same unrelated error, and no reading of the table can see it).

Two further checks this harness owes and makes:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file it mutates.
    `C-0190` records a stray copy of a main source inside the *test* source set making every
    mutation of the original invisible — the tell being a green baseline plus every mutation of
    one file surviving.
  * every mutation's anchor must occur **exactly once** in its subject, so a refactor that moves
    the text orphans the row loudly instead of silently (`C-0185`).

MOVED INTO `tools/` BY `T-308`, from `gpd/data/T-304-mutation/mutate.py`.
`tools/P-31-harness-census.py` discovers any `tools/*mutation-test.py` and **fails the build** on
one that is not declared in its own `HARNESSES` table — which is the mechanism that keeps that
registry from being a census that stopped, and it is why this file waited outside `tools/` for an
iteration in which somebody owned that table. Its row declares the `id_file_old_new_what` adapter
shape (five fields where the Python harnesses have three or four) and the `BY-HAND` sentinel,
because this harness takes a snapshot directory: it mutates **Kotlin**, so one mutation is one
Gradle `test` run — about a minute against the 0.7 s a Python harness takes — and it must not edit
a shared checkout. It is wired on its own Gradle task and is deliberately NOT in `:test`'s
dependency chain.

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed, so
a document gate added tomorrow is excluded by construction (`C-0194`, `T-297`).

Usage:
    tools/T-304-mutation-test.py <snapshot-dir>

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

TESTS = "*RasterTurnAnchorAzimuthTest*"

AZIMUTH = "src/main/kotlin/tile/RasterTurnAnchorAzimuth.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", AZIMUTH,
     "(residue - classZeroResidue - EXACT_HALF_TURN_BASE_PAIRS) * AZIMUTH_PER_BASE_PAIR",
     "(residue - classZeroResidue - HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP) * "
     "AZIMUTH_PER_BASE_PAIR",
     "the exact half turn 21/4 is replaced by caDNAno's integer 5, which is CH-0197's trap"),
    ("M02", AZIMUTH,
     """    foldedDegrees(
        (residue - classZeroResidue - EXACT_HALF_TURN_BASE_PAIRS) * AZIMUTH_PER_BASE_PAIR
    )""",
     "    (residue - classZeroResidue - EXACT_HALF_TURN_BASE_PAIRS) * AZIMUTH_PER_BASE_PAIR",
     "the azimuth is no longer folded into (-180, 180]"),
    ("M03", AZIMUTH,
     "    private val datumSign: Int = if (axialReversed) -1 else 1",
     "    private val datumSign: Int = 1",
     "the azimuth constant no longer travels with the axial datum"),
    ("M04", AZIMUTH,
     "                if (levels.getValue(k + 1) >= here) 1 else -1",
     "                if (levels.getValue(k + 1) >= here) -1 else 1",
     "the anchor moves TOWARD the rim instead of away from it -- unobservable in every span, "
     "because the two rims' azimuths are exact negatives, and caught only by the level"),
    ("M05", AZIMUTH,
     """        anchors.map { it.span }
            .distinctBy { Math.round(it / ANCHOR_SPAN_DECISION_NM) }
            .sorted()""",
     "        anchors.map { it.span }.distinct().sorted()",
     "the span comparison is decided at the arithmetic's own noise instead of coarser"),
    ("M06", AZIMUTH,
     "        require(anchorOffsetBasePairs >= 0) {",
     "        require(anchorOffsetBasePairs >= -100) {",
     "an anchor is allowed to sit outboard of its own duplex end"),
    ("M07", AZIMUTH,
     "        require(classZeroResidue in 0 until PERIOD) {",
     "        require(classZeroResidue in -100 until PERIOD) {",
     "a lattice constant outside the 21 bp period is admitted"),
    ("M08", AZIMUTH,
     "                entryAzimuthDegrees = foldedDegrees(exit + STRAIGHT_ANGLE),",
     "                entryAzimuthDegrees = foldedDegrees(exit),",
     "the two anchors stop being antipodal"),
    ("M09", AZIMUTH,
     "                span = forcedCrossoverSpan(interhelicalDistance, phosphateRadius, exit)",
     "                span = forcedCrossoverSpan(interhelicalDistance, phosphateRadius, 0.0)",
     "the span is read at the aligned azimuth instead of at the derived one"),
    ("M10", AZIMUTH,
     "                classZeroResidue = signs.classZeroResidue,",
     "                classZeroResidue = 5,",
     "the derived lattice constant is hardcoded, so a non-closing raster stops refusing"),
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
        # DERIVES its `BY HAND` third state from `^usage:` -- the docstring's own capitalised
        # `Usage:` does not match it, and the census would read this harness as a REFUSAL
        # (`T-305`, `T-306`: the harness moves, not the parser).
        print("usage: tools/T-304-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
    baseline_code, baseline = run(root)
    print("baseline: exit " + str(baseline_code) + ", " + str(len(baseline)) +
          " named failure(s) " + str(sorted(baseline)))
    if baseline_code != 0 and not baseline:
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
            code, failures = run(path and root)
            killers = sorted(failures - baseline)
            if killers:
                # The killers go on CONTINUATION lines and never after the name: `T-306`'s fourth
                # collision was a row whose label picked up whatever else the line held.
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

    print("# " + str(len(MUTATIONS)) + " mutation(s), " + str(len(survivors)) +
          " survivor(s) " + str(survivors))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main())
