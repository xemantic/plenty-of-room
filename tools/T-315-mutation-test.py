#!/usr/bin/env python3
"""T-315 — the mutation test for route B's uniform raster at the resolved per-bond link.

`C-0161`'s standard on a Kotlin subject: **every mutation must fail a NAMED test**, with the
UNMUTATED copy run first so that its own failures are subtracted (`CH-0237`).

The subject is `tile/ResolvedLinkUniformRaster.kt` — one builder and one census, which is all
`T-315` adds. `C-0208` already built the per-bond link inside `HoneycombGrillage`, and
`tools/T-310-mutation-test.py` mutation-tests that; this harness holds open the two things
`T-315` itself owns:

  * the **builder**, whose defaults must be `UniformRasterTethers.lattice`'s bit for bit, so a
    penalty rung reproduces `C-0207`'s 756 cells and the re-grade is against the same object;
  * the **census**, which is the only evidence that the `116 bp` block's `135 / 300` does not
    transfer to `92 / 98 / 106 bp` and that an in-plane bond reads the transverse constant.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-304`, `T-307` and `T-310` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-315-mutation-test.py <snapshot-dir>

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

TESTS = "*ResolvedLinkUniformRasterTest*"

SUBJECT = "src/main/kotlin/tile/ResolvedLinkUniformRaster.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "        else resolvedLinkStiffness(radialLinkStiffness, transverseLinkStiffness, 1.0, 0.0)",
     "        else resolvedLinkStiffness(radialLinkStiffness, transverseLinkStiffness, 0.0, 1.0)",
     "the in-plane reading takes the RADIAL constant, so an in-plane bond is priced on the "
     "coordinate it has no component along"),
    ("M02", SUBJECT,
     "            THROUGH_THICKNESS_UNIT_Y,\n            THROUGH_THICKNESS_UNIT_Z",
     "            THROUGH_THICKNESS_UNIT_Z,\n            THROUGH_THICKNESS_UNIT_Y",
     "the through-thickness reading is one quarter radial and three quarters transverse -- the "
     "resolution inverted"),
    ("M03", SUBJECT,
     "private val THROUGH_THICKNESS_UNIT_Y: Double = 0.5",
     "private val THROUGH_THICKNESS_UNIT_Y: Double = 1.0",
     "the honeycomb's line of centres stops being a unit vector, so the two readings no longer "
     "partition the link"),
    ("M04", SUBJECT,
     "    val isSingleScalar: Boolean get() = radialLinkStiffness == null",
     "    val isSingleScalar: Boolean get() = radialLinkStiffness != null",
     "a resolved rung claims to be the standing single-scalar object and the penalty rung "
     "claims not to be"),
    ("M05", SUBJECT,
     "    val throughThicknessLinkStiffness: Double =\n"
     "        if (radialLinkStiffness == null) transverseLinkStiffness\n"
     "        else resolvedLinkStiffness(\n"
     "            radialLinkStiffness,",
     "    val throughThicknessLinkStiffness: Double =\n"
     "        resolvedLinkStiffness(\n"
     "            radialLinkStiffness ?: transverseLinkStiffness,",
     "the null default stops being returned by IDENTITY, so a penalty rung's two readings are "
     "equal-but-not-identical -- unitY^2 + unitZ^2 is not exactly one in floating point, which "
     "is C-0208's own reason for branching linkStiffnessAt"),
    ("M06", SUBJECT,
     "        require(transverseLinkStiffness > 0.0 && transverseLinkStiffness.isFinite()) {",
     "        require(transverseLinkStiffness.isFinite()) {",
     "a non-positive transverse constant is admitted"),
    ("M07", SUBJECT,
     "            require(radialLinkStiffness > 0.0 && radialLinkStiffness.isFinite()) {",
     "            require(radialLinkStiffness > -1e9) {",
     "a negative or non-finite radial constant is admitted"),
    ("M08", SUBJECT,
     "        radialLinkStiffness = rung.radialLinkStiffness,",
     "        radialLinkStiffness = null,",
     "the builder ignores its own rung's radial constant, so every resolved re-grade is "
     "silently taken at the penalty"),
    ("M09", SUBJECT,
     "        linkStiffness = rung.transverseLinkStiffness,",
     "        linkStiffness = HoneycombGrillage.RIGID_LINK_STIFFNESS,",
     "the builder ignores its own rung's transverse constant"),
    ("M10", SUBJECT,
     "        subdivisions = subdivisions,",
     "        subdivisions = 1,",
     "the builder drops its subdivision count, so the convergence axis measures nothing"),
    ("M11", SUBJECT,
     "        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,",
     "        foundationStiffness = 2.0 * Gen1Tile.FOUNDATION_SECANT,",
     "the builder's defaults are no longer UniformRasterTethers.lattice's, so the penalty rung "
     "is a different object from the one C-0207 graded"),
    ("M12", SUBJECT,
     "    private val inPlane = lattice.bonds.filter { it.inPlane }",
     "    private val inPlane = lattice.bonds.filter { !it.inPlane }",
     "the census calls the through-thickness bonds in-plane"),
    ("M13", SUBJECT,
     "        if (through.isEmpty()) 0.0 else through.sumOf { it.unitZ * it.unitZ } / through.size",
     "        if (through.isEmpty()) 0.0 else through.sumOf { it.unitZ } / through.size",
     "the census reports the MEAN unitZ rather than the mean square, which the two opposite "
     "azimuths cancel to nearly zero"),
    ("M14", SUBJECT,
     "    val totalBonds: Int = lattice.bonds.size",
     "    val totalBonds: Int = inPlane.size",
     "the census's total is not the lattice's bond count, so the 116 bp block's own 435 is not "
     "reproduced"),
    ("M15", SUBJECT,
     "        .map { Math.round(lattice.linkStiffnessOf(it) / 1e-9) }\n"
     "        .distinct()\n"
     "        .size",
     "        .map { 0 }\n"
     "        .distinct()\n"
     "        .size + 1",
     "the distinct-link count is a constant, so a one-valued penalty census cannot be told from "
     "a two-valued resolved one"),
    ("M16", SUBJECT,
     "        abs(lattice.linkStiffnessOf(it) - rung.inPlaneLinkStiffness) / "
     "rung.inPlaneLinkStiffness",
     "        abs(lattice.linkStiffnessOf(it) - rung.throughThicknessLinkStiffness) / "
     "rung.throughThicknessLinkStiffness",
     "the in-plane departure is measured against the WRONG one of the rung's two readings"),
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
        # DERIVES its `BY HAND` third state from `^usage:` (`T-305`, `T-306`).
        print("usage: tools/T-315-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
