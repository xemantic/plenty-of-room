#!/usr/bin/env python3
"""T-323 — the mutation test for the placement and the distribution searched TOGETHER.

`C-0161`'s standard, applied to a Kotlin subject: **every mutation must fail a NAMED test**, and
the UNMUTATED copy is run first so that its own failures are subtracted (`CH-0237`).

One subject, `tile/JointPlacementDistribution.kt`, and it is new: nothing outside `T-323` reads
it. The shared lattice is NOT mutated here — this task edits no shared source — and
`coupling/CountPhaseInteraction.kt`'s `countPhaseSplit` is REUSED rather than copied, so its own
arithmetic is `T-178`'s subject and is mutation-tested there. What is mutated here is the one
line that maps this task's two factors onto it.

What the rows hold open is the four things a joint answer rests on: that the placement family is
the product of its row option sets and enumerates in the order its tie-break is written on; that
the bank SLICE is the placement it names; that every search decision is taken at six significant
digits with a key tie-break, so the answer is a function of the family and not of a traversal;
and that the 2 x 2's two orderings are the two orderings they are labelled as.

`T-328` and `T-329` (`C-0217`) added nine rows and re-anchored two. The re-anchoring is the point:
`M13` and `M15` used to mutate `jointPlacementBetter`'s own body, and the rule now lives once, in
`searchDecisionKey` and `decidesBetter`, so the same two mutations reach every one of the study's
nineteen selection sites instead of the five that used to route through that one function.
`M26`-`M31` hold open the comparator and the argmin those sites now call — including the
once-per-candidate contract, which is what makes the repair affordable at the two sites whose key
is a whole dropout ensemble — and `M32`-`M34` hold open `T-329`'s identity report, which is a
tolerance and a boolean because the residual's true value is zero.

The two checks this harness owes and makes, as `T-297`, `T-299`, `T-304`, `T-307`, `T-310`,
`T-315` and `T-316` do:

  * `find src -name '<file>.kt'` must return **exactly one** path for every file mutated
    (`C-0190`'s stray-copy trap: a green baseline plus every mutation of one file surviving);
  * every anchor must occur **exactly once**, so a refactor orphans the row loudly (`C-0185`).

The `-x` flags are DERIVED from `build.gradle.kts`'s own `dependsOn` block rather than listed.

Usage:
    tools/T-323-mutation-test.py <snapshot-dir>

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

TESTS = "*JointPlacementDistributionTest*"

SUBJECT = "src/main/kotlin/tile/JointPlacementDistribution.kt"

# (id, file, anchor, replacement, what the mutation breaks)
MUTATIONS = [
    ("M01", SUBJECT,
     "        while (index >= 0 && current[index] == size - choose + index) index--",
     "        while (index >= 0 && current[index] == size - choose + index - 1) index--",
     "the ascending-subset enumeration stops one short, so a row's option set is incomplete and "
     "the family is smaller than the lattice offers"),
    ("M02", SUBJECT,
     "    val size: Long = rowOptionCounts.fold(1L) { running, count ->\n"
     "        Math.multiplyExact(running, count.toLong())\n"
     "    }",
     "    val size: Long = rowOptionCounts.fold(0L) { running, count ->\n"
     "        Math.addExact(running, count.toLong())\n"
     "    }",
     "the family size is a SUM of the row option counts where it is their PRODUCT, so the cheap "
     "bound that decides the whole method is wrong"),
    ("M03", SUBJECT,
     "        return rowOffset[row] + station",
     "        return station",
     "the bank index drops its row offset, so every row's stations alias onto row zero's and a "
     "placement is not the station set it names"),
    ("M04", SUBJECT,
     "    val centroSymmetricRowPairs: Int = (0 until rasterRows).count { row ->\n"
     "        val partner = rasterRows - 1 - row",
     "    val centroSymmetricRowPairs: Int = (0 until rasterRows).count { row ->\n"
     "        val partner = row",
     "every row is made its own centro-symmetric partner, so the census measures a mirror in `s` "
     "alone and C-0063's congruence is read on the wrong symmetry"),
    ("M05", SUBJECT,
     "        abs(rowY[row] + rowY[partner]) < 1e-9 &&\n"
     "                rowStations[row].any { s ->\n"
     "                    rowStations[partner].any { abs(it + s) < 1e-9 }\n"
     "                }",
     "        rowStations[row].any { s ->\n"
     "                    rowStations[partner].any { abs(it + s) < 1e-9 }\n"
     "                }",
     "the centro-symmetry census stops requiring the two rows' `y` to be antisymmetric, so a "
     "face whose rows do not mirror is reported as admitting the symmetry"),
    ("M06", SUBJECT,
     "                } >= columns\n    }",
     "                } > 0\n    }",
     "one matched station is taken to admit a centro-symmetric PLACEMENT, where the placement "
     "needs `columns` of them in every row pair"),
    ("M07", SUBJECT,
     "            require(chosen.zipWithNext().all { (a, b) -> b > a }) {\n"
     "                \"row $row's key must be strictly ascending, was: $chosen\"\n"
     "            }",
     "            require(chosen.isNotEmpty()) {\n"
     "                \"row $row's key must be strictly ascending, was: $chosen\"\n"
     "            }",
     "a key that repeats a station or lists two out of order is admitted, which changes the "
     "path COUNT rather than the placement"),
    ("M08", SUBJECT,
     "            require(chosen.size == columns) {\n"
     "                \"row $row's key carries ${chosen.size} stations and the family has $columns\"\n"
     "            }",
     "            require(chosen.isNotEmpty()) {\n"
     "                \"row $row's key carries ${chosen.size} stations and the family has $columns\"\n"
     "            }",
     "a row is allowed a different number of stations from the rest, so the path count is not "
     "`rows x columns` and the mandate is spread over a set nobody declared"),
    ("M09", SUBJECT,
     "            require(columns <= stations.size) {",
     "            require(columns <= stations.size + 1) {",
     "a column count no row's ladder can supply is admitted, which C-0141's own snapping "
     "refuses for the same reason"),
    ("M10", SUBJECT,
     "            var row = rasterRows - 1\n"
     "            while (row >= 0) {\n"
     "                cursor[row]++\n"
     "                if (cursor[row] < rowOptionCounts[row]) break\n"
     "                cursor[row] = 0\n"
     "                row--\n"
     "            }\n"
     "            if (row < 0) break",
     "            var row = 0\n"
     "            while (row < rasterRows) {\n"
     "                cursor[row]++\n"
     "                if (cursor[row] < rowOptionCounts[row]) break\n"
     "                cursor[row] = 0\n"
     "                row++\n"
     "            }\n"
     "            if (row >= rasterRows) break",
     "the enumeration runs in the wrong odometer direction, so it is no longer lexicographic in "
     "the key the tie-break is written on"),
    ("M11", SUBJECT,
     "            require(chosen.distinct().size == columns) {\n"
     "                \"row $row's nearest stations collide, which changes the path count rather \" +\n"
     "                        \"than the placement: $chosen\"\n"
     "            }",
     "            require(chosen.isNotEmpty()) {\n"
     "                \"row $row's nearest stations collide, which changes the path count rather \" +\n"
     "                        \"than the placement: $chosen\"\n"
     "            }",
     "two abstract columns snapping onto one ladder station are admitted, so C-0167's own "
     "placement would enter the family with fewer paths than it has"),
    ("M12", SUBJECT,
     "        val mirrored = grid.map { (s, y) -> -s to -y }",
     "        val mirrored = grid.map { (s, y) -> -s to y }",
     "a placement is tested for a MIRROR in `s` where centro-symmetry is a rotation, which is "
     "C-0009's own distinction between a Rothemund sheet's symmetry and a plate's"),
    ("M13", SUBJECT,
     "fun searchDecisionKey(value: Double): Double = searchDecision(value)",
     "fun searchDecisionKey(value: Double): Double = value",
     "every search decision in the study is taken at sixteen significant digits rather than "
     "six, so an ulp of jitter in a hot reduction moves the answer into a neighbouring basin "
     "(C-0135, C-0177). T-328 put the rule in ONE function precisely so that this mutation "
     "reaches all nineteen selection sites"),
    ("M14", SUBJECT,
     "    return candidateLabel < bestLabel",
     "    return candidateLabel > bestLabel",
     "the tie-break keeps the LARGER key, so the answer depends on which end of the enumeration "
     "a traversal happens to reach first"),
    ("M15", SUBJECT,
     "fun decidesBetter(candidate: Double, incumbent: Double): Boolean =\n"
     "    searchDecisionKey(candidate) < searchDecisionKey(incumbent)",
     "fun decidesBetter(candidate: Double, incumbent: Double): Boolean =\n"
     "    searchDecisionKey(candidate) > searchDecisionKey(incumbent)",
     "the search MAXIMISES the dishing it is meant to minimise"),
    ("M16", SUBJECT,
     "    require(sweeps >= 1) { \"sweeps must be at least 1, was: $sweeps\" }",
     "    require(sweeps >= 0) { \"sweeps must be at least 1, was: $sweeps\" }",
     "a zero-sweep placement descent is admitted, which is a search that never searches"),
    ("M17", SUBJECT,
     "    require(starts.all { it.family === start.family }) {",
     "    require(starts.all { it.family.columns > 0 }) {",
     "a start from a DIFFERENT placement family reaches the descent, so the row options it "
     "substitutes do not belong to the placement being moved"),
    ("M18", SUBJECT,
     "    family.enumerate().forEach { candidate ->",
     "    family.enumerate().take(1).forEach { candidate ->",
     "the EXHAUSTIVE optimum is taken over one member, which is the whole cheap bound that "
     "makes the deciding cell a proof rather than a descent"),
    ("M19", SUBJECT,
     "    private val structure = lattice.withoutPrestrain",
     "    private val structure = lattice",
     "the bank's influences are taken on the PRESTRAINED lattice, so each is that influence plus "
     "the prestrain's own response and the Woodbury matrix stops being a compliance (C-0104)"),
    ("M20", SUBJECT,
     "                field[i * samples + j] = dishing(s, y)",
     "                field[i * samples + j] = dishing(y, s)",
     "the bank samples the face with its two coordinates exchanged, so it is not the field the "
     "grading reads"),
    ("M21", SUBJECT,
     "            stationInfluence = Array(indices.size) { j ->\n"
     "                DoubleArray(indices.size) { k -> stationInfluence[indices[j]][indices[k]] }\n"
     "            },\n"
     "            dishingFree = dishingFree,",
     "            stationInfluence = Array(indices.size) { j ->\n"
     "                DoubleArray(indices.size) { k -> stationInfluence[j][k] }\n"
     "            },\n"
     "            dishingFree = dishingFree,",
     "the SLICE reads the bank's leading block rather than the placement's own indices, so "
     "7 776 evaluations are not evaluations of the placements they are labelled with"),
    ("M22", SUBJECT,
     "            dishingInfluence = Array(indices.size) { dishingInfluence[indices[it]] },\n"
     "            stationFree = arrayOf(DoubleArray(indices.size) { stationFree[indices[it]] }),",
     "            dishingInfluence = Array(indices.size) { dishingInfluence[it] },\n"
     "            stationFree = arrayOf(DoubleArray(indices.size) { stationFree[indices[it]] }),",
     "the smoothed bank's dishing influences are the leading block rather than the placement's, "
     "so the two halves of the composition search two different objects"),
    ("M23", SUBJECT,
     "    fromCountFromPhase = fixedPlacementTransferred,\n"
     "    toCountFromPhase = searchedPlacementTransferred,\n"
     "    fromCountToPhase = fixedPlacementSearched,",
     "    fromCountFromPhase = fixedPlacementTransferred,\n"
     "    toCountFromPhase = fixedPlacementSearched,\n"
     "    fromCountToPhase = searchedPlacementTransferred,",
     "the mapping `count = placement, phase = distribution` is transposed, so every term of the "
     "split is attributed to the other factor"),
    ("M24", SUBJECT,
     "    return if (ratios.size % 2 == 1) ratios[middle]\n"
     "    else 0.5 * (ratios[middle - 1] + ratios[middle])",
     "    return ratios.average()",
     "the paired reading is a MEAN of ratios where it is declared a median, so one adverse "
     "realisation moves it and the statistic is no longer the order statistic it is named"),
    ("M25", SUBJECT,
     "    val ratios = DoubleArray(a.size) { a[it] / b[it] }",
     "    val ratios = DoubleArray(a.size) { b[it] / a[it] }",
     "the paired ratio is inverted, so a design that wins reads as one that loses"),
    # ---- T-328: the decision precision at every call site, and T-329's identity report
    ("M26", SUBJECT,
     "    compareBy({ searchDecisionKey(key(it)) }, { label(it) })",
     "    compareBy({ key(it) }, { label(it) })",
     "the sorting comparator returns to a RAW Double, so the FOUR sites that select by sorting "
     "(the screens' top-K, the finalists, and the screening-convergence axis's two top sets) "
     "are decided by the last ulp again"),
    ("M27", SUBJECT,
     "fun <T> byDecisionThenLabel(label: (T) -> String, key: (T) -> Double): Comparator<T> =\n"
     "    compareBy({ searchDecisionKey(key(it)) }, { label(it) })",
     "fun <T> byDecisionThenLabel(label: (T) -> String, key: (T) -> Double): Comparator<T> =\n"
     "    compareBy({ searchDecisionKey(key(it)) })",
     "the tie-break on the candidate's own label is dropped, so a decision tie is resolved by "
     "the TRAVERSAL order the sort was handed and the answer stops being a property of the "
     "family"),
    ("M28", SUBJECT,
     "    val keys = DoubleArray(candidates.size) { searchDecisionKey(key(candidates[it])) }",
     "    val keys = DoubleArray(candidates.size) { key(candidates[it]) }",
     "the argmin returns to a RAW Double, so the TWELVE sites that select by argmin -- including "
     "the joint corner and the transferred candidate that MOVED between T-323's two runs -- are "
     "decided by the last ulp again"),
    ("M29", SUBJECT,
     "                        label(candidates[index]) < label(candidates[best]))",
     "                        label(candidates[index]) > label(candidates[best]))",
     "the argmin's tie-break keeps the LARGER label, which is the same defect as M14 on the "
     "other of the two rules and would be invisible to a mutation of either alone"),
    ("M30", SUBJECT,
     "        val better = keys[index] < keys[best] ||",
     "        val better = searchDecisionKey(key(candidates[index])) < keys[best] ||",
     "the argmin evaluates its key TWICE per candidate; two of its call sites have a whole "
     "dropout ensemble behind that key, so the once-per-candidate contract is what makes the "
     "repair affordable rather than a re-solve"),
    ("M31", SUBJECT,
     "    require(candidates.isNotEmpty()) { \"an argmin needs at least one candidate\" }",
     "    require(candidates.size >= 0) { \"an argmin needs at least one candidate\" }",
     "an empty candidate list reaches the argmin, which then reads index zero of nothing"),
    ("M32", SUBJECT,
     "    return abs(residual) < tolerance\n"
     "}",
     "    return residual < tolerance\n"
     "}",
     "an identity residual is read SIGNED, so a departure of the wrong sign larger than the "
     "tolerance reports the identity as holding (T-329)"),
    ("M33", SUBJECT,
     "    return abs(residual) < tolerance",
     "    return abs(residual) <= tolerance",
     "the identity is asserted at its tolerance INCLUSIVE, so a residual exactly at the "
     "declared threshold reports as holding where F9 and F10 are declared to fire on it"),
    ("M34", SUBJECT,
     "    require(tolerance > 0.0 && tolerance.isFinite()) {",
     "    require(tolerance.isFinite()) {",
     "a zero or negative tolerance reaches the identity report, which then says every identity "
     "fails -- a threshold that is not a threshold (T-329)"),
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


def run(root, rerun=False):
    """One test run inside <root>.

    `rerun` forces `--rerun-tasks`, and the BASELINE always sets it. A harness restores the
    source it mutated but it cannot restore a COMPILED CLASS: hand this harness a snapshot it
    has already mutated and the baseline binds to the previous invocation's LAST mutant, because
    Kotlin's incremental compiler sees the restored file and the pre-mutation build state as one
    and the same and does nothing. Measured: a re-used snapshot reported the baseline failing
    `gate 1 -- the paired median ratio is not the ratio of two order statistics`, which is
    exactly the test that kills `M25` -- the previous run's last mutation -- and `M24` and `M25`
    were then subtracted into SURVIVORS. `C-0190`'s stray-copy trap has an incremental-build
    twin, and `CH-0237`'s subtracted baseline is what made it loud instead of silent.
    """
    for path in glob.glob(os.path.join(root, "build/test-results/test/*.xml")):
        os.remove(path)
    completed = subprocess.run(
        ["./gradlew", "test", "--tests", TESTS] + gradle_exclusions(root) +
        (["--rerun-tasks"] if rerun else []) + ["--console=plain"],
        cwd=root, capture_output=True, text=True,
    )
    return completed.returncode, failing_tests(root)


def main():
    if len(sys.argv) != 2:
        # LOWER CASE and at the start of a line, because `tools/T-295-mutation-input-census.py`
        # DERIVES its `BY HAND` third state from `^usage:` (`T-305`, `T-306`).
        print("usage: tools/T-323-mutation-test.py <snapshot-dir>", file=sys.stderr)
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

    # CH-0237: the UNMUTATED copy first, and its failures subtracted. It is forced to RECOMPILE,
    # because a snapshot this harness has already been given carries the last mutant's classes.
    baseline_code, baseline_failures = run(root, rerun=True)
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
