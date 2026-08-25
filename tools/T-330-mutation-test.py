#!/usr/bin/env python3
"""T-330 — the mutation test for the face's rigid basis and the parity that decides it.

`C-0161`'s standard on a Kotlin subject: **every mutation must fail a NAMED test**, with the
UNMUTATED copy run first so that its own failures are subtracted (`CH-0237`).

The subject is `tile/HoneycombGrillage.kt` — the shared class this task owns. What these rows
hold open is the whole of `CH-0282`:

  * the **orthogonality predicate**, which is an EXACT integer statement about the face's own
    half-bond ladder and not a tolerance on a quadrature. Both of its constant collapses are
    mutated, because they fail in opposite directions: always-true reinstates the defect at odd
    `m`, always-false destroys the bit-identity that makes 15 of 18 result files provably
    unmoved;
  * the **datum** it is read at — the raw face position, not the centred one, because the datum
    is what the antisymmetry is *about*;
  * the **Gram**, whose leading entry is the face area and whose off-diagonals are what the
    solve exists for;
  * the **branch** itself, in both directions;
  * the **retained** three-projection reading, which is the defect `C-0092` requires stay
    measurable — a mutation making it an alias of the corrected one must fail;
  * the `3 x 3` **solve**, its right-hand side scaling and its singularity guard.

The two checks this harness owes and makes, as `T-294`, `T-297`, `T-299`, `T-310` and `T-315` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-330-mutation-test.py <snapshot-dir>

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

TESTS = "*FaceRigidBasisTest*"

SUBJECT = "src/main/kotlin/tile/HoneycombGrillage.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "        ladder.indices.all { ladder[it] + ladder[ladder.size - 1 - it] == span }",
     "        true",
     "the basis is called orthogonal at every m, which reinstates CH-0282 exactly: an odd "
     "raster-row count takes the three independent projections again and a uniform field is "
     "reported as curvature"),
    ("M02", SUBJECT,
     "        val span = ladder.first() + ladder.last()\n"
     "        ladder.indices.all { ladder[it] + ladder[ladder.size - 1 - it] == span }",
     "        val span = ladder.first() + ladder.last()\n"
     "        span != span",
     "the basis is called non-orthogonal at every m, so an even-m lattice takes the solve "
     "instead of the projections and the bit-identity 15 of 18 result files rest on is gone"),
    ("M03", SUBJECT,
     "        ladder.indices.all { ladder[it] + ladder[ladder.size - 1 - it] == span }",
     "        ladder.indices.all { ladder[it] + ladder[it] == span }",
     "the antisymmetry is read against the wrong partner, so the predicate tests each rung "
     "against itself and no honeycomb face of more than one row can satisfy it"),
    # The first draft of this row mutated the DATUM -- `rawPositions[it].second` to `beamY[it]` --
    # and SURVIVED, which is the finding rather than a gap: `yDatum` is itself a whole number of
    # half bonds at every block this lattice builds, so the centred coordinate lies on the same
    # integer ladder and the predicate is invariant under the choice. What IS load-bearing on
    # that line is the ladder's UNIT.
    ("M04", SUBJECT,
     "        val halfBond = bondLength / 2.0",
     "        val halfBond = bondLength",
     "the ladder is read in whole bonds rather than half bonds, so a corrugated face's rungs are "
     "half-integral, the require fires and the predicate cannot be evaluated at all"),
    ("M05", SUBJECT,
     "        faceRigidModes.map { a -> faceRigidModes.map { b -> areaInnerProduct(a, b) * area } }",
     "        faceRigidModes.map { a -> faceRigidModes.map { _ -> areaInnerProduct(a, a) * area } }",
     "the Gram loses its off-diagonals, so every basis reads as maximally non-orthogonal and "
     "CH-0282's own 0.0358744468 is not reproduced"),
    ("M06", SUBJECT,
     "        faceRigidModes.map { a -> faceRigidModes.map { b -> areaInnerProduct(a, b) * area } }",
     "        faceRigidModes.map { a -> faceRigidModes.map { b -> areaInnerProduct(a, b) } }",
     "the Gram is normalised by the face area on one side only, so its leading entry is 1 where "
     "the class documents nm^4 and the dimensional gate fails"),
    ("M07", SUBJECT,
     "            faceRigidModes.map { areaInnerProduct(it, field) * area }",
     "            faceRigidModes.map { areaInnerProduct(it, field) }",
     "the right-hand side is scaled differently from the Gram, so every fitted coefficient is "
     "wrong by the face area and the fit annihilates nothing"),
    ("M08", SUBJECT,
     "            tiltSDual.dot(field) / tiltSNorm,\n"
     "            tiltYDual.dot(field) / tiltYNorm",
     "            tiltSDual.dot(field) / tiltYNorm,\n"
     "            tiltYDual.dot(field) / tiltSNorm",
     "the two tilt projections divide by each other's norm, so the orthogonal branch stops "
     "reproducing the standing reading and stops annihilating its own basis"),
    ("M09", SUBJECT,
     "            residual -= mode * rigidPlaneCoefficients[index]",
     "            residual -= mode * rigidPlaneCoefficients[0]",
     "every mode is removed with the piston's coefficient, so the fitted plane is not the plane "
     "that was fitted and a uniform load no longer dishes zero"),
    ("M10", SUBJECT,
     "    val independentProjectionDishingCoefficients: F64Array by lazy {\n"
     "        val residual = coefficients.copy()\n"
     "        residual -= lattice.pistonMode * meanDeflection\n"
     "        residual -= lattice.tiltSMode * tiltAlong\n"
     "        residual -= lattice.tiltYMode * tiltAcross\n"
     "        residual\n"
     "    }",
     "    val independentProjectionDishingCoefficients: F64Array by lazy {\n"
     "        dishingCoefficients\n"
     "    }",
     "the retained three-projection reading becomes an alias of the corrected one, so C-0092's "
     "requirement that the defect stay measurable is silently dropped and C-0154's published "
     "numbers stop being reproducible from the shipped class"),
    ("M11", SUBJECT,
     "            for (j in column..3) a[row][j] -= factor * a[column][j]",
     "            for (j in column..2) a[row][j] -= factor * a[column][j]",
     "the elimination never touches the right-hand side, so the 3 x 3 solve returns the wrong "
     "answer for every non-diagonal system"),
    ("M12", SUBJECT,
     "        require(abs(a[pivot][column]) > 0.0) { \"the matrix is singular at column $column\" }",
     "        require(abs(a[pivot][column]) >= 0.0) { \"the matrix is singular at column $column\" }",
     "the singularity guard admits a zero pivot, so a singular Gram returns NaN instead of "
     "refusing"),
    ("M13", SUBJECT,
     "    val faceRigidModes: List<F64Array> by lazy { listOf(pistonMode, tiltSMode, tiltYMode) }",
     "    val faceRigidModes: List<F64Array> by lazy { listOf(pistonMode, tiltYMode, tiltSMode) }",
     "the two tilt modes exchange places in the basis but not in the projections, so the "
     "orthogonal branch removes each tilt with the other's coefficient"),
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
        print("usage: tools/T-330-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
