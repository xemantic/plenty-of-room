#!/usr/bin/env python3
"""T-310 — the mutation test for the per-bond resolution of a crossover's normal link.

`C-0161`'s standard, applied to a Kotlin subject: **every mutation must fail a NAMED test**, and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`).

Two subjects, and the second is the reason this harness exists rather than only the first:
`tile/CrossoverLinkResolution.kt` is new and nothing but this task reads it, while
`tile/HoneycombGrillage.kt` is the lattice **four claims stand on**, so a mutation of its new
per-bond branch is the only evidence that the branch is load-bearing and that its `null` default
really is the object those claims measured.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-304` and `T-307` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-310-mutation-test.py <snapshot-dir>

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

TESTS = "*CrossoverLinkResolutionTest*"

RESOLUTION = "src/main/kotlin/tile/CrossoverLinkResolution.kt"

GRILLAGE = "src/main/kotlin/tile/HoneycombGrillage.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", RESOLUTION,
     "    return radialStiffness * unitZ * unitZ + transverseStiffness * unitY * unitY",
     "    return radialStiffness * unitY * unitY + transverseStiffness * unitZ * unitZ",
     "the two directions are exchanged, so an in-plane bond takes the radial constant"),
    ("M02", RESOLUTION,
     "    require(radialStiffness > 0.0 && radialStiffness.isFinite()) {",
     "    require(radialStiffness > -1.0 && radialStiffness.isFinite()) {",
     "a non-positive radial constant is admitted"),
    ("M03", RESOLUTION,
     "    require(transverseStiffness > 0.0 && transverseStiffness.isFinite()) {",
     "    require(transverseStiffness > -1e9 && transverseStiffness.isFinite()) {",
     "a negative transverse constant is admitted"),
    ("M04", RESOLUTION,
     "    return amplitude * exp(-separation / lambda) * (separation / lambda - 1.0) * "
     "contactLength",
     "    return amplitude * exp(-separation / lambda) * (1.0 - separation / lambda) * "
     "contactLength",
     "the pair's radial term takes the sign of the FORCE derivative rather than minus it, so a "
     "repulsive pair appears to soften the link"),
    ("M05", RESOLUTION,
     "    val amplitude = equationOfState.repulsionAmplitude / SQRT_THREE",
     "    val amplitude = equationOfState.repulsionAmplitude",
     "the hexagonal array-to-pair conversion is dropped, so the radial term is not the pair's"),
    ("M06", RESOLUTION,
     "    return amplitude * exp(-separation / lambda) * (separation / lambda - 1.0) * "
     "contactLength\n}",
     "    return amplitude * exp(-separation / lambda) * (separation / lambda - 1.0)\n}",
     "the contact length is dropped, so the term is a stiffness per unit length wearing a "
     "stiffness's units"),
    ("M07", RESOLUTION,
     "    return impliedCrossoverBondTension(hingeStiffness, phosphateRadius) / (span - "
     "relaxedStep)",
     "    return impliedCrossoverBondTension(hingeStiffness, phosphateRadius) / span",
     "the implied step stiffness is read over the whole span instead of over the extension, so "
     "it is no longer C-0194's number"),
    ("M08", RESOLUTION,
     "        floor = step + pair,\n        ceiling = duplex + pair",
     "        floor = step,\n        ceiling = duplex",
     "the measured pair term stops entering the radial bracket at all"),
    ("M09", RESOLUTION,
     "        radialLinkStiffness = radialLinkStiffness,\n        scaffoldTurnTies = ties",
     "        radialLinkStiffness = null,\n        scaffoldTurnTies = ties",
     "the resolved builder ignores its own radial argument"),
    ("M10", GRILLAGE,
     "    fun linkStiffnessAt(unitY: Double, unitZ: Double): Double =\n"
     "        if (radialLinkStiffness == null) linkStiffness\n"
     "        else resolvedLinkStiffness(radialLinkStiffness, linkStiffness, unitY, unitZ)",
     "    fun linkStiffnessAt(unitY: Double, unitZ: Double): Double =\n"
     "        if (radialLinkStiffness == null) linkStiffness\n"
     "        else resolvedLinkStiffness(radialLinkStiffness, linkStiffness, unitZ, unitY)",
     "the lattice resolves the link on the WRONG axis, so an in-plane bond takes the radial "
     "constant"),
    ("M11", GRILLAGE,
     "        if (radialLinkStiffness == null) linkStiffness\n"
     "        else resolvedLinkStiffness(radialLinkStiffness, linkStiffness, unitY, unitZ)",
     "        resolvedLinkStiffness(\n"
     "            radialLinkStiffness ?: linkStiffness, linkStiffness, unitY, unitZ\n"
     "        )",
     "the default stops returning the scalar by IDENTITY, so a default lattice is no longer "
     "bit-identical to the object C-0167, C-0180, C-0194, C-0205 and C-0207 measured"),
    ("M12", GRILLAGE,
     "            val link = linkStiffnessOf(bond)",
     "            val link = linkStiffness",
     "the assembled BOND link ignores the resolution, so the matrix is the old one"),
    ("M13", GRILLAGE,
     "            val link = linkStiffnessOf(element)",
     "            val link = linkStiffness",
     "the assembled TIE link ignores the resolution"),
    ("M14", GRILLAGE,
     "        0.5 * bonds.sumOf {\n"
     "            val gap = linkExtension(field, it)\n"
     "            linkStiffnessOf(it) * gap * gap\n"
     "        }",
     "        0.5 * linkStiffness * bonds.sumOf {\n"
     "            val gap = linkExtension(field, it)\n"
     "            gap * gap\n"
     "        }",
     "the link energy stops being the per-bond sum"),
    ("M15", GRILLAGE,
     "            require(radialLinkStiffness > 0.0 && radialLinkStiffness.isFinite()) {",
     "            require(radialLinkStiffness > -1.0 && radialLinkStiffness.isFinite()) {",
     "the lattice admits a non-positive radial constant"),
    ("M16", GRILLAGE,
     "            radialLinkStiffness = radialLinkStiffness,\n            faceColumn = faceColumn,",
     "            radialLinkStiffness = null,\n            faceColumn = faceColumn,",
     "withoutPrestrain drops the radial constant, so every influence bank is taken on a "
     "different lattice from the free field (C-0104)"),
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
        print("usage: tools/T-310-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
