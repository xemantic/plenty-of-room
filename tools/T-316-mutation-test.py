#!/usr/bin/env python3
"""T-316 — the mutation test for a distribution SEARCHED at the resolved per-bond link.

`C-0161`'s standard, applied to a Kotlin subject: **every mutation must fail a NAMED test**, and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`).

One subject, `tile/SearchedDistribution.kt`, and it is new: nothing outside `T-316` reads it. The
shared lattice is NOT mutated here — this task edits no shared source, and `HoneycombGrillage`'s
own per-bond branch is `T-310`'s subject and is mutation-tested there.

What the rows hold open is the three things a searched answer rests on: that the smoothed bank is
the same object the grading reads (and is taken on `withoutPrestrain`, which is `C-0104`'s trap),
that the percentile objective is the order statistic it says it is and is rounded at the decision
precision, and that the record's own fields report the quantities they are named for — because a
design reads the POINT a descent reached and not only its value.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-304`, `T-307`, `T-310` and
`T-315` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-316-mutation-test.py <snapshot-dir>

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

TESTS = "*SearchedDistributionTest*"

SUBJECT = "src/main/kotlin/tile/SearchedDistribution.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "    return stiffnesses.max() / stiffnesses.min()",
     "    return stiffnesses.min() / stiffnesses.max()",
     "the ratio is inverted, so C-0060's window is read on the wrong quantity"),
    ("M02", SUBJECT,
     "    require(stiffnesses.all { it > 0.0 && it.isFinite() }) {\n"
     "        \"every path stiffness must be positive and finite, were: $stiffnesses\"\n"
     "    }",
     "    require(stiffnesses.all { it.isFinite() }) {\n"
     "        \"every path stiffness must be positive and finite, were: $stiffnesses\"\n"
     "    }",
     "a zero or negative path stiffness is admitted into a ratio, which then reports a "
     "meaningless number rather than refusing"),
    ("M03", SUBJECT,
     "    val structure = lattice.withoutPrestrain",
     "    val structure = lattice",
     "the smoothed bank's influences are taken on the PRESTRAINED lattice, so each is that "
     "influence plus the prestrain's own response and the Woodbury matrix stops being a "
     "compliance (C-0104)"),
    ("M04", SUBJECT,
     "            val s = -halfS + 2.0 * halfS * i / (samples - 1)",
     "            val s = -halfY + 2.0 * halfY * i / (samples - 1)",
     "the smoothed bank samples the face on the wrong half-extent, so it is not the field the "
     "grading reads"),
    ("M05", SUBJECT,
     "            sampled { x, y -> free[s].dishing(x, y) }",
     "            sampled { x, y -> free[s].deflection(x, y) }",
     "the smoothed bank's free field is a DEFLECTION where the grading reads a DISHING"),
    ("M06", SUBJECT,
     "        searchDecision(orderStatistic(sample, fraction) / freeStroke)",
     "        searchDecision(orderStatistic(sample, 0.5) / freeStroke)",
     "the search minimises the MEDIAN where the verdict is read at the 90th percentile"),
    ("M07", SUBJECT,
     "        searchDecision(orderStatistic(sample, fraction) / freeStroke)",
     "        searchDecision(orderStatistic(sample, fraction))",
     "the objective is a dishing in nm where every graded quantity in this corpus is a ratio of "
     "the free stroke"),
    ("M08", SUBJECT,
     "    return { stiffnesses ->\n"
     "        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)\n"
     "        searchDecision(orderStatistic(sample, fraction) / freeStroke)\n"
     "    }",
     "    return { stiffnesses ->\n"
     "        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)\n"
     "        orderStatistic(sample, fraction) / freeStroke\n"
     "    }",
     "the decision precision is dropped, so an ulp of jitter in a hot reduction can flip a line "
     "search and move the answer into a neighbouring basin (C-0135, C-0177)"),
    ("M09", SUBJECT,
     "        ratio = stiffnessRatio(searched.stiffnesses),",
     "        ratio = stiffnessRatio(nominal.stiffnesses),",
     "the reported ratio belongs to a distribution the census does not grade, so the "
     "buildability threshold is read at the wrong point"),
    ("M10", SUBJECT,
     "        nominalObjective = nominal.worstDishing / freeStroke,",
     "        nominalObjective = nominal.worstDishing,",
     "the smoothed search's own objective is emitted in nm rather than as a fraction of the "
     "stroke"),
    ("M11", SUBJECT,
     "        bestTransferredTrainingObjective = transferred.minOf { objective(it) }",
     "        bestTransferredTrainingObjective = transferred.maxOf { objective(it) }",
     "the in-sample comparand is the WORST transferred rule, so every in-sample gain is "
     "overstated"),
    ("M12", SUBJECT,
     "        trainingObjective = objective(searched.stiffnesses),",
     "        trainingObjective = objective(nominal.stiffnesses),",
     "the reported training objective is not the reported distribution's"),
    ("M13", SUBJECT,
     "    require(transferred.all { it.size == paths }) {",
     "    require(transferred.all { it.size > 0 }) {",
     "a start of the wrong length reaches the descent"),
    ("M14", SUBJECT,
     "    require(percentileSweeps >= 1) {",
     "    require(percentileSweeps >= 0) {",
     "a zero-sweep descent is admitted, which is a search that never searches"),
    ("M15", SUBJECT,
     "    require(totalStiffness > 0.0) {",
     "    require(totalStiffness.isFinite()) {",
     "C-0017's mandate is an EQUALITY on a positive sum and a non-positive one is admitted"),
    ("M16", SUBJECT,
     "    require(transferred.isNotEmpty()) { \"at least one transferred distribution is required\" }",
     "    require(transferred.size >= 0) { \"at least one transferred distribution is required\" }",
     "a search with NO comparand is admitted, so the composition's own in-sample guarantee -- "
     "that it is seeded from the rules it is measured against -- is gone"),
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
        print("usage: tools/T-316-mutation-test.py <snapshot-dir>", file=sys.stderr)
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
