#!/usr/bin/env python3
"""T-299 — the mutation test for the raster-turn TETHER element.

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

MOVED INTO `tools/` BY `T-305`, from `gpd/data/T-299-mutation/mutate.py`.
`tools/P-31-harness-census.py` discovers any `tools/*mutation-test.py` and **fails the build** on
one that is not declared in its own `HARNESSES` table — which is exactly the mechanism that keeps
that registry from being a census that stopped, and it is why this file waited outside `tools/`
for an iteration in which somebody owned that table. Its row declares the fifth adapter shape
(`id_file_old_new_what`, five fields where the Python harnesses have three or four) and the
`BY-HAND` sentinel, because this harness takes a snapshot directory: it mutates **Kotlin**, so one
mutation is one Gradle `test` run — about a minute against the 0.7 s a Python harness takes — and
it must not edit a shared checkout. It is wired on its own Gradle task and is deliberately NOT in
`:test`'s dependency chain.

Usage:
    tools/T-299-mutation-test.py <snapshot-dir>

It edits files inside <snapshot-dir> and restores them afterwards. Give it a snapshot, never the
checkout.
"""
import os
import re
import shutil
import subprocess
import sys
import glob
import xml.etree.ElementTree as ET

TESTS = "*HoneycombRasterTurnTethersTest*"

TETHERS = "src/main/kotlin/tile/HoneycombRasterTurnTethers.kt"
GRILLAGE = "src/main/kotlin/tile/HoneycombGrillage.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", TETHERS,
     "return 1.0 / 3.0 - u2 / 15.0 + 2.0 * u2 * u2 / 189.0 - u2 * u2 * u2 / 675.0",
     "return 1.0 / 3.0",
     "the Langevin derivative's small-argument series is truncated to its constant term"),
    ("M02", TETHERS,
     "if (u > LANGEVIN_DERIVATIVE_LARGE) return 1.0 / (u * u)",
     "if (u > LANGEVIN_DERIVATIVE_LARGE) return 2.0 / (u * u)",
     "the large-argument branch is scaled by two"),
    ("M03", TETHERS,
     "secantStiffness = tension / span",
     "secantStiffness = tension / contour",
     "the secant is taken over the contour instead of over the span"),
    ("M04", TETHERS,
     "tangentStiffness = thermalEnergy / kuhnLength / (contour * langevinDerivative(u))",
     "tangentStiffness = thermalEnergy / kuhnLength / contour",
     "the tangent drops the Langevin derivative"),
    ("M05", TETHERS,
     "        val state = if (turns[index].atHighEnd) highRimState else lowRimState",
     "        val state = lowRimState",
     "the per-rim assignment collapses to one chain"),
    ("M06", TETHERS,
     "            tension = if (withPreload) state.tension else 0.0",
     "            tension = state.tension",
     "withPreload no longer suppresses the preload"),
    ("M07", GRILLAGE,
     "        tether.tangentStiffness * unitZ * unitZ + tether.secantStiffness * unitY * unitY",
     "        tether.tangentStiffness * unitY * unitY + tether.secantStiffness * unitZ * unitZ",
     "the chain's own decomposition swaps its two directions"),
    ("M08", GRILLAGE,
     "    val axialStiffness: Double = tether.secantStiffness",
     "    val axialStiffness: Double = tether.tangentStiffness",
     "the transverse slip is charged the tangent instead of the secant"),
    ("M09", GRILLAGE,
     """            scatter(
                intArrayOf(dof(node, a, U), dof(node, a, THETA), dof(node, b, U), dof(node, b, THETA)),
                Array(4) { i -> DoubleArray(4) { j -> axial * slipGradient[i] * slipGradient[j] } }
            )""",
     "            // MUTATED: the tether's axial term is dropped",
     "the tether's axial term is dropped from the assembly"),
    ("M10", GRILLAGE,
     """            val magnitude = element.tether.tension * element.unitZ
            if (magnitude == 0.0) return@forEach""",
     """            val magnitude = element.tether.tension
            if (magnitude == 0.0) return@forEach""",
     "the preload loses its unitZ projection, so the nine in-plane turns are loaded"),
    ("M11", GRILLAGE,
     """            load[dof(element.node, b, W)] -= magnitude
            load[dof(element.node, b, PHI)] += magnitude * armY""",
     """            load[dof(element.node, b, W)] -= magnitude
            load[dof(element.node, b, PHI)] -= magnitude * armY""",
     "the preload's second roll arm changes sign, breaking frame indifference"),
    ("M12", GRILLAGE,
     "            emptyMap(), scaffoldTurnTies.map { it.copy(prestrainRadians = 0.0) },\n"
     "            scaffoldTurnTethers.map { it.copy(tension = 0.0) }",
     "            emptyMap(), scaffoldTurnTies.map { it.copy(prestrainRadians = 0.0) },\n"
     "            scaffoldTurnTethers",
     "withoutPrestrain keeps the tether preload"),
    ("M13", GRILLAGE,
     "        -element.unitZ * tetherLinkResidual(field, element)",
     "        element.unitZ * tetherLinkResidual(field, element)",
     "the chain extension loses its sign, so the preload appears to lengthen the chain"),
    ("M14", GRILLAGE,
     """            scaffoldTurnTethers.none { it.tension != 0.0 }
        ) {""",
     """            true
        ) {""",
     "withoutPrestrain returns `this` even when a tether carries a preload"),
]


def named_failures(tree):
    names = set()
    for path in glob.glob(os.path.join(tree, "build/test-results/test/*Tether*.xml")):
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
        # LOWER CASE and at the start of a line, because `tools/T-295-mutation-input-census.py`
        # DERIVES its `BY HAND` third state from `^usage:` -- the docstring's own capitalised
        # `Usage:` does not match it, and the census would have read this harness as a REFUSAL.
        # `T-306`'s third collision, met before it could happen: the harness moves, not the parser.
        print("usage: tools/T-299-mutation-test.py <snapshot-dir>", file=sys.stderr)
        sys.exit(__doc__)
    tree = sys.argv[1]
    if os.path.exists(os.path.join(tree, ".git")):
        sys.exit("refusing to mutate a real checkout: give me a snapshot")

    # the stray-copy check (`C-0190`)
    for source in {m[1] for m in MUTATIONS}:
        base = os.path.basename(source)
        hits = subprocess.run(["find", "src", "-name", base], cwd=tree,
                              capture_output=True, text=True).stdout.split()
        print("FIND  %-44s %d path(s): %s" % (base, len(hits), " ".join(hits)))
        if len(hits) != 1:
            sys.exit("a mutation subject must exist at exactly one path")

    # every anchor must occur exactly once
    for ident, source, anchor, _new, _what in MUTATIONS:
        text = open(os.path.join(tree, source), encoding="utf-8").read()
        if text.count(anchor) != 1:
            sys.exit("%s: anchor occurs %d times in %s" % (ident, text.count(anchor), source))

    # `T-306`: the local is NAMED `baseline_failures` because `tools/P-31-harness-census.py`
    # derives `measuresBaseline` from a harness's own identifiers, and `base_failures` did not
    # carry the word -- so the census read `base: NO` about a harness that does measure one.
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
        verdict = "killed by %d" % len(killers) if killers else (
            "BUILD BROKEN" if not compiled else "SURVIVES")
        rows.append((ident, verdict, what, killers))
        print("%-5s %-14s %s" % (ident, verdict, what))
        for name in killers:
            print("            %s" % name)

    survivors = [r for r in rows if r[1] == "SURVIVES"]
    broken = [r for r in rows if r[1] == "BUILD BROKEN"]
    print("\n# %d mutation(s), %d survivor(s), %d that did not compile"
          % (len(rows), len(survivors), len(broken)))
    sys.exit(1 if survivors or broken else 0)


if __name__ == "__main__":
    main()
