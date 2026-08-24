#!/usr/bin/env python3
"""T-303 — the mutation test for the crossover link-stiffness routes and the bisector.

`C-0161`'s standard applied to Kotlin: **every mutation must fail a NAMED test**, each mutation
replaces a rule **wholesale** (`C-0176`: an alternation with the original is a dead mutation), and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`).

Two further checks this harness owes and makes:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file it mutates
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving is a
    statement about the build, not about the tests).
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

It was retained in `gpd/data/` while `T-303` ran, because `tools/P-31-harness-census.py` refuses
an undeclared `tools/*mutation-test.py` and the registry was another agent's that iteration.
`T-309` moved it here and declared it, which is the queue item that move was always owed to --
never a deletion.

usage: tools/T-303-mutation-test.py <snapshot-dir>

It edits files inside <snapshot-dir> and restores them afterwards. Give it a snapshot, never the
checkout.
"""
import glob
import os
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

TESTS = "*CrossoverLinkStiffnessTest*"
MODEL = "src/main/kotlin/tile/CrossoverLinkStiffness.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", MODEL,
     "return 12.0 * reducedEndStiffness / (6.0 + reducedEndStiffness)",
     "return 12.0 * reducedEndStiffness / (12.0 + reducedEndStiffness)",
     "the end-condition continuum's denominator, so c(6) is no longer 6"),
    ("M02", MODEL,
     "return 12.0 * reducedEndStiffness / (6.0 + reducedEndStiffness)",
     "return 12.0",
     "the end-condition factor ignores rho, so a pinned end is as stiff as a clamped one"),
    ("M03", MODEL,
     "return endConditionFactor * persistenceLength * thermalEnergy / (span * span * span)",
     "return endConditionFactor * persistenceLength * thermalEnergy / (span * span)",
     "the bending stiffness' power of the span, so it no longer scales as 1/lambda^2"),
    ("M04", MODEL,
     "return endConditionFactor * persistenceLength * thermalEnergy / (span * span * span)",
     "return persistenceLength * thermalEnergy / (span * span * span)",
     "the bending stiffness drops the end-condition factor, so a pin is not free"),
    ("M05", MODEL,
     "fun transverseSoftenedBondLinkStiffness(alpha: Double = 1.0): Double =\n"
     "    Gen1Tile.crossoverInPlaneStiffness(alpha)",
     "fun transverseSoftenedBondLinkStiffness(alpha: Double = 1.0): Double =\n"
     "    Gen1Tile.crossoverHingeStiffness(alpha)",
     "the softened-bond route reads the HINGE, so it is no longer k_theta-independent"),
    ("M06", MODEL,
     "    return -repulsiveForcePerLength * contactLength / separation",
     "    return repulsiveForcePerLength * contactLength / separation",
     "the central pair term's sign, so a repulsive pair appears to stiffen the link"),
    ("M07", MODEL,
     "        ceiling = maxOf(tension, bond) + stiff",
     "        ceiling = maxOf(tension, bond) + soft",
     "the ceiling takes the SOFTEST connector instead of the stiffest"),
    ("M08", MODEL,
     "        ceiling = maxOf(tension, bond) + stiff",
     "        ceiling = minOf(tension, bond) + stiff",
     "the ceiling takes the smaller of the two displacement routes"),
    ("M09", MODEL,
     "        floor = tension,",
     "        floor = bond,",
     "the floor is no longer the pure-tension route"),
    ("M10", MODEL,
     "    require(atLow > 0.0 != atHigh > 0.0) {",
     "    require(atLow.isFinite() && atHigh.isFinite()) {",
     "the bisector accepts a bracket that does not straddle"),
    ("M11", MODEL,
     "        if ((here > 0.0) == lowIsPositive) lo = mid else hi = mid",
     "        if ((here > 0.0) == lowIsPositive) hi = mid else lo = mid",
     "the bisector keeps the wrong half, so it converges on the wrong endpoint"),
    ("M12", MODEL,
     "    return 10.0.pow(0.5 * (lo + hi))",
     "    return 10.0.pow(lo)",
     "the bisector returns a bracket endpoint instead of its midpoint"),
    ("M13", MODEL,
     "        linkStiffness = linkStiffness,\n        scaffoldTurnTies = ties",
     "        linkStiffness = HoneycombGrillage.RIGID_LINK_STIFFNESS,\n"
     "        scaffoldTurnTies = ties",
     "the lattice builder ignores its own link-stiffness argument"),
    ("M14", MODEL,
     "    return if (!tied) bare else build(",
     "    return if (false) bare else build(",
     "the lattice builder ties an untied lattice"),
    ("M15", MODEL,
     "    require(reducedEndStiffness >= 0.0) {",
     "    require(reducedEndStiffness >= -1e9) {",
     "the end-condition factor no longer refuses a negative rho"),
]


def named_failures(tree):
    names = set()
    for path in glob.glob(
        os.path.join(tree, "build/test-results/test/*CrossoverLinkStiffness*.xml")
    ):
        root = ET.parse(path).getroot()
        for case in root.iter("testcase"):
            if list(case.iter("failure")) or list(case.iter("error")):
                names.add(case.get("name"))
    return names


def run(tree):
    for path in glob.glob(os.path.join(tree, "build/test-results/test/*.xml")):
        os.remove(path)
    proc = subprocess.run(
        ["./gradlew", "test", "--tests", TESTS, "-q"],
        cwd=tree, capture_output=True, text=True,
    )
    compiled = "Compilation error" not in proc.stdout + proc.stderr
    return proc.returncode, named_failures(tree), compiled


def main():
    if len(sys.argv) != 2:
        print("usage: tools/T-303-mutation-test.py <snapshot-dir>", file=sys.stderr)
        sys.exit(__doc__)
    tree = sys.argv[1]
    if os.path.exists(os.path.join(tree, ".git")):
        sys.exit("refusing to mutate a real checkout: give me a snapshot")

    for source in {m[1] for m in MUTATIONS}:
        base = os.path.basename(source)
        hits = subprocess.run(["find", "src", "-name", base], cwd=tree,
                              capture_output=True, text=True).stdout.split()
        print("FIND  %-40s %d path(s): %s" % (base, len(hits), " ".join(hits)))
        if len(hits) != 1:
            sys.exit("a mutation subject must exist at exactly one path")

    for ident, source, anchor, _new, _what in MUTATIONS:
        text = open(os.path.join(tree, source), encoding="utf-8").read()
        if text.count(anchor) != 1:
            sys.exit("%s: anchor occurs %d times in %s" % (ident, text.count(anchor), source))

    code, baseline_failures, compiled = run(tree)
    print("\nBASELINE  exit=%d  compiled=%s  named failures: %s"
          % (code, compiled, sorted(baseline_failures) or "none"))
    if baseline_failures or not compiled:
        sys.exit("the UNMUTATED copy does not pass: nothing below is a measurement")

    rows = []
    for ident, source, anchor, new, what in MUTATIONS:
        path = os.path.join(tree, source)
        shutil.copy(path, path + ".orig")
        text = open(path, encoding="utf-8").read()
        open(path, "w", encoding="utf-8").write(text.replace(anchor, new, 1))
        code, failures, compiled = run(tree)
        shutil.move(path + ".orig", path)
        killers = sorted(failures - baseline_failures)
        verdict = "killed by %d named test(s)" % len(killers) if killers else (
            "BUILD BROKEN" if not compiled else "SURVIVES  no named test failed")
        rows.append((ident, verdict, what, killers))
        print("%-5s %-30s %s" % (ident, verdict, what))
        for name in killers:
            print("            %s" % name)

    survivors = [r for r in rows if r[1].startswith("SURVIVES")]
    broken = [r for r in rows if r[1] == "BUILD BROKEN"]
    print("\n# %d mutation(s), %d survivor(s), %d that did not compile"
          % (len(rows), len(survivors), len(broken)))
    sys.exit(1 if survivors or broken else 0)


if __name__ == "__main__":
    main()
