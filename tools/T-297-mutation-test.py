#!/usr/bin/env python3
"""T-297 — mutation test for `tile/CrossoverCommonMode.kt` and the three link-offset methods
`HoneycombGrillage.turnLinkExtension` / `turnLinkOffsetLoad` / `turnLinkOffsetResponse`.

Every mutation must fail at least one NAMED test of `CrossoverCommonModeTest`.

Three warnings from `CLAUDE.md` are honoured explicitly, because each of them has already turned
a mutation table into a table that is full and empty:

*   the UNMUTATED tree is run first and its named failures are subtracted, so a `killed` row
    means what the column says (`CH-0237`);
*   every mutation REPLACES a rule wholesale rather than widening it to `original|mutant`, so a
    surviving original cannot keep the mutant alive (`C-0176`);
*   the subject sources are located with a tree-wide search and their count asserted to be one,
    because a stray copy inside the *test* source set makes every mutation of the original
    invisible and reports it as `SURVIVES` (`C-0190` §8b).

This is the corpus's first harness whose subjects are **Kotlin** rather than Python modules in
`tools/`, which is why `tools/P-31-harness-census.py` learned to resolve a declared subject path
from the tree root.

WHY IT IS NOT WIRED INTO THE BUILD.  `C-0185` wired every Python harness into `./gradlew test`
because each is a pure-Python self-test that runs in seconds.  This one drives **Gradle** — one
incremental Kotlin compile and one test run per mutation — so wiring it into the build would make
the build recursive and turn a ten-second task into a ten-minute one.  It therefore takes a
snapshot directory as an argument and is run by hand, which `tools/P-31-harness-census.py --check`
reports as `NOWHERE — runs only when somebody remembers`.  That report is correct and the reason
is this paragraph.

Usage:

    tools/T-297-mutation-test.py <snapshot-dir>

The snapshot must be a full checkout (see `tools/snapshot.sh`); the harness edits sources inside
it and restores them afterwards, so it must NOT be pointed at the shared working tree.
"""

import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

MODEL = "src/main/kotlin/tile/CrossoverCommonMode.kt"
GRILLAGE = "src/main/kotlin/tile/HoneycombGrillage.kt"
TEST_CLASS = "com.xemantic.nano.plentyofroom.tile.CrossoverCommonModeTest"

# (name, file, old, new) -- `P-31`'s `name_file_old_new` shape. Each `old` occurs exactly once.
MUTATIONS = [
    (
        "M1 CH-0242's ratio drops the factor two in 1 + 2 r_P/g", MODEL,
        "    interhelicalDistance / crossoverSpanFloor(interhelicalDistance, phosphateRadius)",
        "    1.0 + phosphateRadius / crossoverSpanFloor(interhelicalDistance, phosphateRadius)",
    ),
    (
        "M2 the frame-indifferent ratio drops its one-half", MODEL,
        "            (2.0 * crossoverSpanFloor(interhelicalDistance, phosphateRadius) * phosphateRadius)",
        "            (crossoverSpanFloor(interhelicalDistance, phosphateRadius) * phosphateRadius)",
    ),
    (
        "M3 the geometric term is inverted", MODEL,
        "    return interhelicalDistance / (2.0 * phosphateRadius)\n}",
        "    return phosphateRadius / (2.0 * interhelicalDistance)\n}",
    ),
    (
        "M4 the implied bond tension drops its factor two", MODEL,
        "    return 2.0 * hingeStiffness / phosphateRadius",
        "    return hingeStiffness / phosphateRadius",
    ),
    (
        "M5 the span-derived link stiffness divides by d instead of by g", MODEL,
        "): Double = impliedCrossoverBondTension(hingeStiffness, phosphateRadius) /\n"
        "        crossoverSpanFloor(interhelicalDistance, phosphateRadius)",
        "): Double = impliedCrossoverBondTension(hingeStiffness, phosphateRadius) /\n"
        "        interhelicalDistance",
    ),
    (
        "M6 the lattice common-mode stiffness drops its quarter", MODEL,
        "    return linkStiffness * arm * arm / 4.0",
        "    return linkStiffness * arm * arm",
    ),
    (
        "M7 the frame-indifferent connector arm becomes d/3", MODEL,
        "    return interhelicalDistance / 2.0\n}",
        "    return interhelicalDistance / 3.0\n}",
    ),
    (
        "M8 the rigid-roll residual drops the factor two on the arm", MODEL,
        "    return rollRadians * bondUnitY * (2.0 * connectorArm - interhelicalDistance)",
        "    return rollRadians * bondUnitY * (connectorArm - interhelicalDistance)",
    ),
    (
        "M9 the link offset uses the arm rather than twice it", MODEL,
        "    return interhelicalDistance * bondUnitY * rollRadians\n}",
        "    return interhelicalDistance / 2.0 * bondUnitY * rollRadians\n}",
    ),
    (
        "M10 the link offset load is applied on the RELATIVE coordinate", GRILLAGE,
        "            load[dof(node, b, PHI)] += magnitude * armY",
        "            load[dof(node, b, PHI)] -= magnitude * armY",
    ),
    (
        "M11 the link offset load drops the penalty, so the field cannot converge", GRILLAGE,
        "            val magnitude = linkStiffness * offset",
        "            val magnitude = offset",
    ),
    (
        "M12 the turn link extension reads the RELATIVE roll instead of the common one", GRILLAGE,
        "        return field[dof(element.node, element.tie.lowerBeam, W)] +\n"
        "                arm * field[dof(element.node, element.tie.lowerBeam, PHI)] -\n"
        "                field[dof(element.node, element.tie.upperBeam, W)] +\n"
        "                arm * field[dof(element.node, element.tie.upperBeam, PHI)]",
        "        return field[dof(element.node, element.tie.lowerBeam, W)] +\n"
        "                arm * field[dof(element.node, element.tie.lowerBeam, PHI)] -\n"
        "                field[dof(element.node, element.tie.upperBeam, W)] -\n"
        "                arm * field[dof(element.node, element.tie.upperBeam, PHI)]",
    ),
    (
        "M13 the link offset accepts a turn index outside the tie list", GRILLAGE,
        "            require(index in turnElements.indices) {",
        "            require(index >= -1000) {",
    ),
]


def gate_task_exclusions(snapshot):
    """`-x` flags for every document gate `test` depends on, DERIVED from `build.gradle.kts`.

    `tasks.named("test") { dependsOn(...) }` hangs ~28 Python gates off the Kotlin test task, so
    without this each mutation would re-run the whole corpus census — and, worse, a gate that is
    unhappy about the snapshot (a harness the snapshot's own census has not been told about, say)
    fails the build before `:test` runs and every row reads `the test class did not run at all`.
    Derived rather than listed, so a gate added tomorrow is excluded by construction.
    """
    text = (snapshot / "build.gradle.kts").read_text(encoding="utf-8")
    marker = 'tasks.named("test") {'
    if marker not in text:
        return []
    block = text[text.index(marker):]
    block = block[:block.index("\n}")]
    flags = []
    for name in re.findall(r'"([A-Za-z][A-Za-z0-9]*)"', block):
        if name == "test":
            continue
        flags += ["-x", name]
    return flags


def run_tests(snapshot, exclusions):
    """Run the test class and return the set of NAMED failing tests."""
    xml = snapshot / "build/test-results/test" / ("TEST-" + TEST_CLASS + ".xml")
    if xml.exists():
        xml.unlink()
    subprocess.run(
        ["./gradlew", "test", "--tests", "*CrossoverCommonModeTest*", "-q"] + exclusions,
        cwd=snapshot, capture_output=True, text=True,
    )
    if not xml.exists():
        return {"<the test class did not run at all>"}
    root = ET.parse(xml).getroot()
    if not root.findall("testcase"):
        return {"<the test class ran no tests>"}
    return {case.get("name") for case in root.findall("testcase")
            if case.find("failure") is not None or case.find("error") is not None}


def main():
    if len(sys.argv) != 2:
        print("usage: tools/T-297-mutation-test.py <snapshot-dir>", file=sys.stderr)
        return True
    snapshot = Path(sys.argv[1]).resolve()
    if (snapshot / ".git").exists():
        print("refusing to mutate a checkout with a .git directory: " + str(snapshot),
              file=sys.stderr)
        return True

    # C-0190 section 8b: a stray copy of a mutated source makes every mutation invisible.
    for name in ("CrossoverCommonMode.kt", "HoneycombGrillage.kt", "CrossoverCommonModeTest.kt"):
        found = sorted(str(p) for p in (snapshot / "src").rglob(name))
        if len(found) != 1:
            print("EXPECTED exactly one " + name + ", found " + str(len(found)) + ": " +
                  ", ".join(found), file=sys.stderr)
            return True
    print("subject sources: one path each", flush=True)

    sources = {rel: (snapshot / rel).read_text() for rel in {m[1] for m in MUTATIONS}}
    for rel in sources:
        shutil.copyfile(snapshot / rel, snapshot / (rel + ".t297.bak"))

    exclusions = gate_task_exclusions(snapshot)
    print("excluding " + str(len(exclusions) // 2) + " document gate task(s) "
          "that `test` depends on", flush=True)
    baseline = run_tests(snapshot, exclusions)
    if baseline:
        print("BASELINE FAILURES (subtracted below): " + ", ".join(sorted(baseline)), flush=True)
    else:
        print("baseline: 0 named failures", flush=True)

    survivors = []
    try:
        for name, rel, old, new in MUTATIONS:
            ident = name.split(" ", 1)[0]
            text = sources[rel]
            count = text.count(old)
            if count != 1:
                print(ident + "  ANCHOR NOT FOUND (" + str(count) + ") in " + rel, flush=True)
                survivors.append(ident)
                continue
            (snapshot / rel).write_text(text.replace(old, new))
            failed = run_tests(snapshot, exclusions) - baseline
            (snapshot / rel).write_text(text)
            if failed:
                print(ident + "  killed by " + str(len(failed)) + " named test(s)  " +
                      name.split(" ", 1)[1], flush=True)
                for failing in sorted(failed):
                    print("        FAIL: " + failing, flush=True)
            else:
                print(ident + "  SURVIVED  " + name.split(" ", 1)[1], flush=True)
                survivors.append(ident)
    finally:
        for rel in sources:
            shutil.move(str(snapshot / (rel + ".t297.bak")), str(snapshot / rel))

    print("# " + str(len(MUTATIONS)) + " mutation(s), " + str(len(survivors)) +
          " survivor(s) over a subtracted baseline of " + str(len(baseline)), flush=True)
    return bool(survivors)


if __name__ == "__main__":
    sys.exit(1 if main() else 0)
