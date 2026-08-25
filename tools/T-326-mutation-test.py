#!/usr/bin/env python3
"""T-326 — the mutation test for the two reconstructions of the face field, and the third.

`C-0161`'s standard on a Kotlin subject: **every mutation must fail a NAMED test**, with the
UNMUTATED copy run first so that its own failures are subtracted (`CH-0237`).

The subject is `tile/HoneycombGrillage.kt` — the shared class this task owns, and every line
added to it is new, so nothing here can move a committed number. What these rows hold open:

  * the **closed form** of the fit/sample gap, term by term. Its three coefficients `d^2/16`,
    `d^3/32` and the bond midpoint `ybar` are mutated separately, because each is a different
    part of the derivation and a table that moved them together could hide two errors that
    cancel; the sign of the relative roll is mutated too, because the whole content of the form
    is that the bond's two members enter with OPPOSITE sign;
  * the **bond census** it is written on: taking the `2d` gaps instead of the `d` gaps, and
    taking every consecutive pair, both of which leave a plausible-looking dual;
  * the **split**, in both directions — a split that does nothing reinstates the 18 % quadrature
    error `CH-0285` measures, and one that also splits a band with no interior boundary would
    change a smooth integral that must not move;
  * the **rectangle** convention's own domain, because reading it over the tributary strips
    instead makes convention C collapse onto convention B and the `0 : 1 : 6` collinearity, the
    diagonal Gram and the whole *dissolves-CH-0282* argument go with it;
  * the **inertness** of the addition: a mutation that repoints `faceRigidCoefficients` at the
    new fit must fail, because `P9`'s whole claim is that the eighteen committed files are
    unmoved by construction.

The two checks this harness owes and makes, as `T-294`, `T-297`, `T-299`, `T-310`, `T-315` and
`T-330` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-326-mutation-test.py <snapshot-dir>

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

TESTS = "*FaceReconstructionTest*"

SUBJECT = "src/main/kotlin/tile/HoneycombGrillage.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "        val rollScale = d * d / 16.0",
     "        val rollScale = d * d / 8.0",
     "the relative-roll coefficient of the closed form is doubled, so the predicted gap is "
     "twice the measured one at every mode and every raster-row count"),
    ("M02", SUBJECT,
     "        val meanRollScale = d * d * d / 32.0",
     "        val meanRollScale = d * d * d / 16.0",
     "the MEAN-roll coefficient of the tiltY form is doubled -- the one term the piston and "
     "tiltS forms do not carry, so only the tiltY closed form and the pure-y limiting case move"),
    ("M03", SUBJECT,
     "            val midY = (beamY[lower] + beamY[upper]) / 2.0",
     "            val midY = beamY[lower]",
     "the tiltY form is written about the bond's LOWER axis instead of its midpoint, which is "
     "exactly the asymmetry the pairing removes"),
    ("M04", SUBJECT,
     "                            addRoll(upper, weightS * rollScale)\n"
     "                            addRoll(lower, -weightS * rollScale)",
     "                            addRoll(upper, weightS * rollScale)\n"
     "                            addRoll(lower, weightS * rollScale)",
     "the bond's two members enter with the SAME sign, so the dual reads a mean roll where the "
     "whole content of the closed form is that it reads a RELATIVE one -- and it then fails to "
     "annihilate the rigid modes"),
    ("M05", SUBJECT,
     "            abs(gap - d) < 1e-9\n        }\n    }",
     "            abs(gap - 2.0 * d) < 1e-9\n        }\n    }",
     "the census takes the 2d gaps -- the UNBONDED pairs -- instead of the face's own vertical "
     "bonds, which is a plausible-looking dual over the wrong pairs"),
    # The first draft of this row inserted `true || ` after the lambda arrow, which is a SYNTAX
    # ERROR --- and the harness reported it as a SURVIVOR, because a build that never runs a test
    # produces no named failure. That is `CLAUDE.md`'s own "a crash is not a named test, and an
    # exit code cannot tell them apart", inverted: there an unfinished suite reads as a kill,
    # here a broken build reads as a survivor. The harness now refuses a run in which no test
    # executed at all, and the row mutates the PREDICATE instead.
    ("M06", SUBJECT,
     "            abs(gap - d) < 1e-9\n        }\n    }",
     "            true\n        }\n    }",
     "every consecutive pair is called a vertical bond, so the census is m-1 rather than m/2 "
     "and the closed form double-counts the 2d gaps"),
    ("M07", SUBJECT,
     "        faceNearestBoundaries.forEach { if (it > low && it < high) cuts += it }",
     "        faceNearestBoundaries.forEach { if (false) cuts += it }",
     "the split does nothing, so every 'exact' integral reverts to the whole-strip Gauss rule "
     "and carries CH-0285's 18 % error in the term this task is about"),
    ("M08", SUBJECT,
     "    fun integrateOverFaceRectangle(field: (Double, Double) -> Double): Double =\n"
     "        integrateBand(-lengthY / 2.0, lengthY / 2.0, field)",
     "    fun integrateOverFaceRectangle(field: (Double, Double) -> Double): Double =\n"
     "        integrateOverFaceSplit(field)",
     "convention C is read over the tributary strips instead of the face rectangle, so it "
     "collapses onto convention B: the 0 : 1 : 6 collinearity, the diagonal Gram at every m and "
     "the whole dissolves-CH-0282 argument go with it"),
    ("M09", SUBJECT,
     "    val faceSampledGram: List<List<Double>> by lazy { gramOf(::faceSampledInnerProduct) }",
     "    val faceSampledGram: List<List<Double>> by lazy { gramOf(::areaInnerProduct) }",
     "convention C's Gram is taken in the shipped inner product, so its diagonality becomes a "
     "parity again and P2's 'at every m' is false"),
    ("M10", SUBJECT,
     "    fun faceRigidCoefficients(field: F64Array): List<Double> =\n"
     "        if (faceRigidModesAreOrthogonal) listOf(",
     "    fun faceRigidCoefficients(field: F64Array): List<Double> =\n"
     "        if (false) listOf(",
     "the shipped decomposition is repointed at the new fit, which is exactly the adoption this "
     "task REFUSES to take in code -- P9's inertness claim, and the eighteen committed files, "
     "rest on it not happening"),
    ("M11", SUBJECT,
     "        return (0 until cuts.size - 1).map { cuts[it] to cuts[it + 1] }",
     "        return listOf(cuts.first() to cuts.last())",
     "yPieces returns one piece spanning the whole band, so the boundaries are computed and "
     "then discarded -- a split that looks present and is not"),
    ("M12", SUBJECT,
     "    private val faceNearestBoundaries: List<Double> by lazy {\n"
     "        faceBeams.zipWithNext { lower, upper -> (beamY[lower] + beamY[upper]) / 2.0 }",
     "    private val faceNearestBoundaries: List<Double> by lazy {\n"
     "        faceBeams.zipWithNext { lower, _ -> beamY[lower] }",
     "the nearest-beam boundary is put on an axis instead of at the midpoint between two, which "
     "is where evaluate's reconstruction actually jumps"),
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
    """The NAMED tests that failed, and how many ran at all.

    The count matters: a mutation that does not COMPILE produces no test result whatever, so a
    harness reading only the exit code and the failure set records it as a SURVIVOR. `M06`'s
    first draft was exactly that. `CLAUDE.md` has the same trap in the other direction --- a
    crashed suite reading as a kill --- and the cure is the same, which is to require that the
    run actually executed something.
    """
    names = set()
    executed = 0
    for path in glob.glob(os.path.join(root, "build/test-results/test/*.xml")):
        for case in ET.parse(path).getroot().iter("testcase"):
            executed += 1
            if case.find("failure") is not None or case.find("error") is not None:
                names.add(case.get("name"))
    return names, executed


def run(root):
    for path in glob.glob(os.path.join(root, "build/test-results/test/*.xml")):
        os.remove(path)
    completed = subprocess.run(
        ["./gradlew", "test", "--tests", TESTS] + gradle_exclusions(root) + ["--console=plain"],
        cwd=root, capture_output=True, text=True,
    )
    names, executed = failing_tests(root)
    return completed.returncode, names, executed


def main():
    if len(sys.argv) != 2:
        # LOWER CASE and at the start of a line, because `tools/T-295-mutation-input-census.py`
        # DERIVES its `BY HAND` third state from `^usage:` (`T-305`, `T-306`).
        print("usage: tools/T-326-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
    baseline_code, baseline_failures, baseline_executed = run(root)
    print("baseline: exit " + str(baseline_code) + ", " + str(baseline_executed) +
          " test(s) executed, " + str(len(baseline_failures)) +
          " named failure(s) " + str(sorted(baseline_failures)))
    if baseline_executed == 0:
        print("REFUSED: the unmutated copy ran no test at all")
        return 2
    if baseline_code != 0 and not baseline_failures:
        print("REFUSED: the unmutated copy failed with no NAMED test failure; nothing below "
              "would be a measurement")
        return 2

    survivors = []
    broken = []
    for identifier, target, old, new, what in MUTATIONS:
        path = os.path.join(root, target)
        shutil.copyfile(path, path + ".orig")
        try:
            text = open(path).read()
            open(path, "w").write(text.replace(old, new))
            code, failures, executed = run(root)
            killers = sorted(failures - baseline_failures)
            if executed == 0:
                broken.append(identifier)
                print(identifier + "  BROKEN -- the mutated tree ran no test at all, so this "
                      "row is a build failure and not a measurement (exit " + str(code) + ")")
                print("      [" + what + "]")
            elif killers:
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
          str(survivors) + (", " + str(len(broken)) + " broken " + str(broken) if broken else ""))
    return 1 if (survivors or broken) else 0


if __name__ == "__main__":
    sys.exit(main())
