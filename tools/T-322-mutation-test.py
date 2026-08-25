#!/usr/bin/env python3
"""T-322 — the mutation test for route B's own widths graded coupled on derived stations.

`C-0161`'s standard, applied to a Kotlin subject: **every mutation must fail a NAMED test**, and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`).

One subject, `tile/RouteBCoupled.kt`, and it is new: nothing outside `T-322` reads it. The shared
lattice is NOT mutated here — this task edits no shared source, and `HoneycombGrillage`'s own
per-bond branch is `T-310`'s subject and is mutation-tested there.

What the rows hold open is the three things a coupled census read on the WRONG tile silently
inherits, each of which is a function of the row length: the station ladder and the phase rule
that picks a placement on it; the transferred ratio band, which is a PREDICTION carried between
two lattices and therefore owes its own `contains`; and the reader that takes the uncoupled
reference out of `C-0211`'s committed cells at the recommended `b0` and the WORST of its twelve
chain corners. The fourth is `CH-0272`'s conjunction, which is a type here rather than a sentence.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-304`, `T-307`, `T-310`,
`T-315` and `T-316` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-322-mutation-test.py <snapshot-dir>

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

TESTS = "*RouteBCoupledTest*"

SUBJECT = "src/main/kotlin/tile/RouteBCoupled.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "    val derivedPhase: Int = (0 until periodBasePairs).maxByOrNull { phase ->",
     "    val derivedPhase: Int = (0 until periodBasePairs).minByOrNull { phase ->",
     "the derived phase minimises the station count instead of maximising it, so the placement "
     "stands on the sparsest ladder the row offers"),
    ("M02", SUBJECT,
     "    val derivedPhase: Int = (0 until periodBasePairs).maxByOrNull { phase ->\n"
     "        // `maxByOrNull` keeps the FIRST maximum, which is the tie-break the rule states.\n"
     "        minimumStationsAtPhase(phase)\n"
     "    }!!",
     "    val derivedPhase: Int = (0 until periodBasePairs).reversed().maxByOrNull { phase ->\n"
     "        // `maxByOrNull` keeps the FIRST maximum, which is the tie-break the rule states.\n"
     "        minimumStationsAtPhase(phase)\n"
     "    }!!",
     "the tie goes to the LATER phase, where CLAUDE.md's rule is that the earlier candidate wins "
     "-- and on a row where every phase ties, the tie-break is the whole of the answer"),
    ("M03", SUBJECT,
     "    fun minimumStationsAtPhase(phase: Int): Int = stationsAtPhase(phase).min()",
     "    fun minimumStationsAtPhase(phase: Int): Int = stationsAtPhase(phase).max()",
     "the column bound is the BEST row's ladder rather than the worst, where "
     "honeycombSnappedGrid refuses a placement wider than ANY row's -- a change of the path "
     "COUNT wearing a change of position"),
    ("M04", SUBJECT,
     "        require(phase in 0 until periodBasePairs) {",
     "        require(phase in -periodBasePairs until periodBasePairs) {",
     "a phase outside the period reaches the ladder, where floorMod would silently fold it onto "
     "a different lattice"),
    ("M05", SUBJECT,
     "        return columns <= minimumStationsAtPhase(phase)",
     "        return columns <= maximumColumns",
     "carriesColumnsAtPhase ignores the phase it is asked about and answers at the derived one, "
     "so the inherited route-A phase reads as though it carried the derived one's ladder"),
    ("M06", SUBJECT,
     "            rootingHelices, rowBasePairs, phase, interRowOffsetBasePairs,",
     "            rootingHelices, rowBasePairs, phase, 0,",
     "C-0141's FORCED inter-row stagger is dropped, so the two station rows are put in register "
     "-- which no honeycomb face is"),
    ("M07", SUBJECT,
     "    fun carriesColumns(columns: Int): Boolean = carriesColumnsAtPhase(columns, derivedPhase)",
     "    fun carriesColumns(columns: Int): Boolean = carriesColumnsAtPhase(columns, 0)",
     "the placement is checked at phase zero rather than at the derived phase, so a width whose "
     "derived phase is what buys the fifth column reads as refusing it"),
    # M08 and M09 SURVIVED their first run, and both for one reason: `honeycombStationLattice`
    # carries the same two requirements verbatim and `derivedPhase`'s own initialiser reaches
    # it, so a widened guard here still throws -- from downstream (`C-0207` section 8, met on a
    # third object).  The repair is in the FIXTURE and in the guard's own wording: the messages
    # now name this class, and the test asserts them.
    ("M08", SUBJECT,
     "        require(rowBasePairs > 0) {\n"
     '            "a route-B station ladder needs a positive rowBasePairs, was: $rowBasePairs"',
     "        require(rowBasePairs >= 0) {\n"
     '            "a route-B station ladder needs a positive rowBasePairs, was: $rowBasePairs"',
     "a zero-length row is admitted here and refused downstream, so the guard that names THIS "
     "class stops firing and only the wording of the exception says which one refused"),
    ("M09", SUBJECT,
     "        require(rootingHelices >= 1) {\n"
     '            "a route-B station ladder needs at least one rooting helix, was: '
     '$rootingHelices"',
     "        require(rootingHelices >= 0) {\n"
     '            "a route-B station ladder needs at least one rooting helix, was: '
     '$rootingHelices"',
     "a face with no rooting helix is admitted here and refused downstream, the same duplicated "
     "guard one parameter across"),
    ("M10", SUBJECT,
     '        require(low > 0.0 && low.isFinite()) { "the band\'s low end must be positive, '
     'was: $low" }',
     '        require(low.isFinite()) { "the band\'s low end must be positive, was: $low" }',
     "a non-positive transferred ratio is admitted, so the band predicts a negative dishing"),
    ("M11", SUBJECT,
     "        require(high >= low && high.isFinite()) {",
     "        require(high.isFinite()) {",
     "an inverted band is admitted, and every one of its three verdicts is then meaningless"),
    ("M12", SUBJECT,
     "        return RouteBPrediction(low * uncoupled, high * uncoupled, threshold)",
     "        return RouteBPrediction(low, high, threshold)",
     "the band is not applied to the uncoupled reading at all, so the prediction is a ratio "
     "compared against a dishing"),
    ("M13", SUBJECT,
     "    val excludesFlat: Boolean = low > threshold",
     "    val excludesFlat: Boolean = high > threshold",
     "a band that merely REACHES the threshold is reported as excluding a flat reading, which "
     "is the failure direction that turns a cheap bound into a false negative"),
    ("M14", SUBJECT,
     "    val guaranteesFlat: Boolean = high < threshold",
     "    val guaranteesFlat: Boolean = low < threshold",
     "a band that merely reaches below the threshold is reported as guaranteeing flatness"),
    ("M15", SUBJECT,
     "    fun contains(value: Double): Boolean = value >= low && value <= high",
     "    fun contains(value: Double): Boolean = value <= high",
     "F20's own predicate becomes one-sided, so a reading BELOW the predicted band -- the "
     "interesting direction -- reads as inside it"),
    ("M16", SUBJECT,
     "    require(records.isNotEmpty()) {",
     "    require(records.size >= 0) {",
     "an unknown rung returns an empty reference list instead of refusing, so the sweep grades "
     "nothing and says nothing"),
    ("M17", SUBJECT,
     '        val worst = own.maxByOrNull { it.getValue("freeTileWithPreload").toDouble() }!!',
     '        val worst = own.minByOrNull { it.getValue("freeTileWithPreload").toDouble() }!!',
     "the reference is C-0211's BEST chain corner where the recommendation is a minimax over "
     "the twelve, so every coupled cell is judged against a reading its own recommendation "
     "does not make"),
    ("M18", SUBJECT,
     '                it.getValue("bestWorstCornerDishing").toDouble()',
     '                it.getValue("bestPhaseOnDishing").toDouble()',
     "the published comparand is C-0211's phase rather than its dishing, so the gate that ties "
     "this study's reference to C-0211's own published number compares two different quantities"),
    ("M19", SUBJECT,
     "    val flatAndAdmissible: Boolean = flatAtP90 && peakInsideUnzipCeiling",
     "    val flatAndAdmissible: Boolean = flatAtP90 || peakInsideUnzipCeiling",
     "CH-0272's conjunction becomes a disjunction, which is exactly the reading a verdict block "
     "reporting three thresholds separately invites"),
    ("M20", SUBJECT,
     "    val allThreeThresholds: Boolean = flatAndAdmissible && beatsUncoupledAtP90",
     "    val allThreeThresholds: Boolean = flatAndAdmissible",
     "the uncoupled tile drops out of the conjunction, so a coupling worse than doing nothing "
     "counts as satisfying every threshold"),
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
        print("usage: tools/T-322-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
