#!/usr/bin/env python3
"""T-294 — the mutation test for the cross-section re-grade and its corrected dishing.

`C-0161`'s standard on a Kotlin subject: **every mutation must fail a NAMED test**, with the
UNMUTATED copy run first so that its own failures are subtracted (`CH-0237`).

The subject is `tile/CrossSectionTiedRegrade.kt` — the four censuses that do **not** transfer
between this programme's two 60-helix honeycomb cross-sections, plus the least-squares face
basis `F1`'s firing made necessary. `HoneycombGrillage` itself is not mutated here: it is a
shared source this task did not edit, and `tools/T-310-mutation-test.py` already holds its
per-bond link open.

What these rows hold open:

  * the **bond** and **tie** censuses, which are the whole of `F5` — the count `H − 1 = 59`
    transfers between the cross-sections and nothing else does, and a census keyed on the
    raster path rather than on the **bond graph** cannot see a turn landing on a pair the
    honeycomb does not bond (`C-0175`);
  * the **enhancement**, whose two silent mistakes are the ones `multiLayerRigidities` invites —
    `layers` is the block's THICKNESS count and its layer spacing is `d√3/2`, not `d`;
  * the **normalisation**, where `edgeY` is the plate convention and the absolute tolerance is
    `T-5b`'s fraction of *this* tile's own stroke;
  * the **face rigid basis**, which is the only thing standing between an odd raster-row count
    and a uniform field reported as six per cent of curvature;
  * `C-0104`'s trap in the surrogate: the free field on the lattice as built and every influence
    on `withoutPrestrain`.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-310` and `T-315` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-294-mutation-test.py <snapshot-dir>

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

TESTS = "*CrossSectionTiedRegradeTest*"

SUBJECT = "src/main/kotlin/tile/CrossSectionTiedRegrade.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "        inPlane = lattice.bonds.count { it.inPlane },\n"
     "        throughThickness = lattice.bonds.count { !it.inPlane }",
     "        inPlane = lattice.bonds.count { !it.inPlane },\n"
     "        throughThickness = lattice.bonds.count { it.inPlane }",
     "the bond census calls the through-thickness bonds in-plane, so 15 x 4's 140 / 270 and "
     "10 x 6's 135 / 300 both invert"),
    ("M02", SUBJECT,
     "        bonds = lattice.bonds.size,",
     "        bonds = lattice.bonds.count { it.inPlane },",
     "the bond total is not the lattice's own count, so 410 and 435 are neither reproduced nor "
     "the sum of their two directions"),
    ("M03", SUBJECT,
     "        throughThickness = turns.count { !it.inPlane },\n"
     "        inPlane = turns.count { it.inPlane },",
     "        throughThickness = turns.count { it.inPlane },\n"
     "        inPlane = turns.count { !it.inPlane },",
     "the tie split is inverted, so 15 x 4 reads 14 / 45 and 10 x 6 reads 9 / 50 -- the two "
     "numbers F5 is declared on"),
    ("M04", SUBJECT,
     "    return turns.filter { (it.lowerBeam to it.upperBeam) !in bonded }",
     "    return emptyList()",
     "the census stops asserting itself against the BOND GRAPH and becomes a census keyed on "
     "the traversal, which is C-0175's own first-run defect. It SURVIVED this harness's first "
     "run, because a check that never fires on a real raster cannot be told from one that "
     "cannot fire -- the repair was the FIXTURE and the injectable argument that admits it"),
    ("M05", SUBJECT,
     "        atHighRim = turns.count { it.atHighEnd },\n"
     "        atLowRim = turns.count { !it.atHighEnd },",
     "        atHighRim = turns.count { !it.atHighEnd },\n"
     "        atLowRim = turns.count { it.atHighEnd },",
     "the rim split is inverted, so reversing the scaffold's first axial sign no longer "
     "exchanges the two ends"),
    ("M06", SUBJECT,
     "        layers = block.helicesPerRow,",
     "        layers = block.rasterRows,",
     "the enhancement takes the block's IN-PLANE count for its thickness count -- "
     "multiLayerRigidities' first silent mistake, and it returns a plausible number"),
    ("M07", SUBJECT,
     "        layerSpacing = HoneycombCrossSectionGeometry.columnPitch(block.bondLength)",
     "        layerSpacing = block.bondLength",
     "the enhancement stacks the layers at d rather than at d sqrt(3)/2, which CLAUDE.md "
     "records overstates the second moment by exactly 4/3"),
    # `M08` RETIRED, and the retirement is the finding. It read
    #     require(compositeFraction in 0.0..1.0)  ->  require(compositeFraction > -1e9)
    # and SURVIVED, because `multiLayerRigidities` carries the same guard immediately downstream:
    # `CLAUDE.md`'s *a guard whose only observable behaviour is duplicated downstream is a guard
    # no mutation of it can reach*. The duplicate is deleted rather than fixtured, because unlike
    # the case that entry records there is no construction/use split to separate the two. What
    # replaces it mutates something the function DOES own -- which of `LayerCoupling`'s five
    # readings it takes.
    ("M08", SUBJECT,
     "        coupling = LayerCoupling.CALIBRATED,",
     "        coupling = LayerCoupling.COMPOSITE,",
     "the enhancement takes the FULL parallel-axis factor rather than the measured fraction of "
     "it, so C-0116's band stops entering at all"),
    ("M09", SUBJECT,
     "    val edgeY = block.plateEdgeY",
     "    val edgeY = block.envelopeY",
     "the normalisation takes the block ENVELOPE where HoneycombGrillage.lengthY takes the "
     "plate convention -- the two are one duplex diameter apart and C-0141 quotes both"),
    ("M10", SUBJECT,
     "        absoluteToleranceNm = fractionalTolerance * stroke",
     "        absoluteToleranceNm = fractionalTolerance",
     "the absolute tolerance stops being a fraction of THIS tile's own stroke, so the 1.5x "
     "that separates the two cross-sections disappears"),
    ("M11", SUBJECT,
     "    require(fractionalTolerance > 0.0 && fractionalTolerance < 1.0) {",
     "    require(fractionalTolerance > 0.0) {",
     "the tolerance guard is opened at its UPPER end only -- C-0204 section 8's two-sided range"),
    ("M12", SUBJECT,
     "    val gram: List<List<Double>> =\n"
     "        modes.map { a -> modes.map { b -> lattice.areaInnerProduct(a, b) } }",
     "    val gram: List<List<Double>> =\n"
     "        modes.mapIndexed { i, a -> modes.mapIndexed { j, b ->\n"
     "            if (i == j) lattice.areaInnerProduct(a, b) else 0.0 } }",
     "the Gram is forced diagonal, so the least-squares fit degenerates to the three "
     "INDEPENDENT projections the standing decomposition already takes and the odd-m defect "
     "comes back"),
    ("M13", SUBJECT,
     "            if (i == j) null else abs(gram[i][j]) / sqrt(gram[i][i] * gram[j][j])",
     "            if (i == j) null else gram[i][j] / sqrt(gram[i][i] * gram[j][j])",
     "the non-orthogonality is reported SIGNED, so a negative off-diagonal reads as orthogonal"),
    ("M14", SUBJECT,
     "    val modesAreOrthogonal: Boolean = worstNonOrthogonality < 1e-12",
     "    val modesAreOrthogonal: Boolean = worstNonOrthogonality < 1e-1",
     "the orthogonality flag is widened past the defect it exists to detect, so 15 x 4 reads "
     "orthogonal"),
    ("M15", SUBJECT,
     "        for (i in 0..2) residual -= modes[i] * c[i]",
     "        residual -= modes[0] * c[0]",
     "only the piston is removed, so the two tilts stay in the residual and a rigid plane reads "
     "as dishing"),
    ("M16", SUBJECT,
     "        var pivot = column\n"
     "        for (row in column + 1..2) if (abs(a[row][column]) > abs(a[pivot][column])) pivot = row",
     "        val pivot = column",
     "the 3 x 3 solve loses its partial pivoting, so a zero leading entry divides by zero "
     "instead of being refused"),
    ("M17", SUBJECT,
     "        require(abs(a[pivot][column]) > 0.0) { \"the matrix is singular at column $column\" }",
     "        require(true) { \"the matrix is singular at column $column\" }",
     "a singular Gram is admitted and returns NaN rather than refusing"),
    ("M18", SUBJECT,
     "    val structure = lattice.withoutPrestrain\n"
     "    val free = lattice.solve(pressure)",
     "    val structure = lattice\n"
     "    val free = lattice.solve(pressure)",
     "C-0104's trap: the influence bank is taken on the PRESTRAINED lattice, so the Woodbury "
     "matrix stops being a compliance -- silent at exactly the departures that matter"),
    ("M19", SUBJECT,
     "        corrected = build { basis.dishingOf(it) }",
     "        corrected = build { field ->\n"
     "            object : DishingSolution {\n"
     "                override fun deflectionAt(x: Double, y: Double) = field.deflection(x, y)\n"
     "                override fun dishingAt(x: Double, y: Double) = field.dishing(x, y)\n"
     "            }\n"
     "        }",
     "the corrected surrogate is the standing one, so every 15 x 4 cell silently carries the "
     "odd-m defect and the two emitted conventions are one"),
    ("M20", SUBJECT,
     "    require(basis.belongsTo(lattice)) { \"the basis must be the lattice's own\" }",
     "    require(true) { \"the basis must be the lattice's own\" }",
     "a basis built on a DIFFERENT lattice is admitted, so a Gram of one geometry is used to "
     "fit the field of another"),
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
        print("usage: tools/T-294-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
