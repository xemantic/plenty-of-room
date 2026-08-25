#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# T-334 -- the gate census, emitted as a result file.
#
#     tools/T-334-emit-result.py [--ref <git-ref>] [--out <path>] [--self-test]
#
# WHY IT TAKES A `--ref`.  This file's subject is the CORPUS, so it is a function of a mutable
# object rather than of committed inputs and `gpd/README.md`'s *"reproducible from it alone"* rule
# holds only if the state is named: the ref is an argument and the RESOLVED sha is recorded as
# `baselineRef`.  `CH-0246` forbids re-running this class of file as a control on a later change --
# the re-run re-bases the measurement onto today's corpus and OVERWRITES the record.
#
# WHY IT EMITS TWO STATES.  Wiring the census into `build.gradle.kts` adds two `tools/` scripts to
# the very set it counts, so the pass MOVES its own answer -- `CH-0182`, and the third artifact of
# this task to run into it.  Both readings are emitted and each is named: `atBaselineRef` is the
# corpus before this task, `atThisPassesTree` is after it.
"""Emit gpd/results/T-334-the-gate-census-by-reachability.json."""

import argparse
import importlib.util
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(ROOT, "gpd", "results", "T-334-the-gate-census-by-reachability.json")


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


gate = _load("t334_gate", "T-334-gate-census.py")
header = _load("t334_header", "emission_header.py")

#: The refs the three published figures were taken at, so the drift is a table and not a memory.
#: `71d126e` is `C-0210`'s own baseline and `d7b7074` is `C-0220`'s.
PUBLISHED = (
    ("71d126e", 37, False, "C-0210",
     "16 + 1 + 20 -- verify.sh's own gates plus its fixture plus the LITERAL Gradle invocations "
     "carrying no --self-test flag; the helper shape is not counted at all"),
    ("71d126e", 43, True, "CH-0286",
     "17 + 20 + 6 -- the same, re-derived, plus the HELPER-wired harnesses the literal pattern "
     "cannot see"),
    ("d7b7074", 50, True, "C-0220", "18 + 21 + 11, at its own ref"),
    ("d9a3522", 51, True, "C-0220 / CH-0286",
     "18 + 21 + 12 -- the figure in print in DECISIONS-FOR-NDI.md, reconstructed at the committed "
     "state its 'this pass's tree' corresponds to"),
)


def _reading(tree):
    reading = gate.census(tree)
    return {
        "state": reading["state"],
        "ref": reading["ref"],
        "routeA": {
            "predicate": "a command word of a comment-stripped line of tools/verify.sh",
            "count": len(reading["routeA"]),
            "names": reading["routeA"],
        },
        "routeB": {
            "predicate": ("named inside a balanced commandLine(...) span of an Exec task that "
                          ":test depends on; BOTH the literal and the helper wiring"),
            "count": len(reading["routeBReachable"]),
            "names": reading["routeBReachable"],
        },
        "overlap": {
            "count": len(reading["overlap"]),
            "names": reading["overlap"],
            "why": ("C-0210 asserted the two invocation sets are DISJOINT, which is true only "
                    "under its --self-test filter; without that filter they are not, so the sum "
                    "is not a sum"),
        },
        "distinctToolsThatCanFailADefaultVerifyShRun": len(reading["union"]),
        "union": reading["union"],
        "notCounted": {
            "execTasksUnreachableFromTest": {
                "count": len(reading["routeBUnreachable"]),
                "names": reading["routeBUnreachable"],
                "why": ("Gradle never executes them; build.gradle.kts's own comment says they are "
                        "runnable by name and NOT reachable from :test"),
            },
            "reachedOnlyByImport": {
                "count": len(reading["importOnlyResidue"]),
                "names": reading["importOnlyResidue"],
                "why": "a defect in one fails the run and no invocation names it",
            },
            "sourcedByVerifySh": {
                "count": len(reading["sourcedNotInvoked"]),
                "names": reading["sourcedNotInvoked"],
                "why": "the command word is `source`",
            },
            "theKotlinSuite": "not a tools/ script, and it is most of what can fail the run",
            "nonDefaultInvocations": ("--no-checks and any --drop set checks=no and delete route A "
                                      "entirely; the trailing \"$@\" reaches ./gradlew test, so "
                                      "-x <task> can delete a route-B member"),
        },
        "theFourPredicatesThisReplaces": {
            "namingPrefix": {
                "command": "ls tools/check-*.py tools/trace-answers.py",
                "count": len(reading["namingPredicate"]),
                "datedBy": "CH-0222, CH-0243",
            },
            "verifyShOwnInvocations": {"count": len(reading["routeA"]), "datedBy": "C-0210"},
            "gradleLiteralNoSelfTest": {
                "count": len(reading["gradleLiteralNoSelfTest"]), "datedBy": "CH-0286",
            },
            "gradleHelper": {"count": len(reading["gradleHelper"]), "datedBy": "this task"},
        },
        "armOne": {
            "invariant": ("the Exec tasks unreachable from :test EQUAL the harnesses "
                          "P-31-harness-census.py declares BY-HAND, in both directions"),
            "unreachable": len(reading["routeBUnreachable"]),
            "declaredByHand": len(reading["declaredByHand"]),
            "equal": set(reading["declaredByHand"]) == set(reading["routeBUnreachable"]),
        },
        "missing": reading["missing"],
        "notExecutable": reading["notExecutable"],
    }


def build(ref="d9a3522"):
    baseline = gate.Tree(ref)
    working = gate.Tree(None)
    at_ref = _reading(baseline)
    at_tree = _reading(working)
    older = gate.Tree("71d126e")
    document = {
        "task": "T-334",
        "title": ("how many distinct tools can fail a tools/verify.sh run - the census taken by "
                  "REACHABILITY rather than by the shape of a wiring"),
        "baselineRef": baseline.ref,
        "baselineRefRequested": ref,
        "parameters": {
            "predicate": ("a tool under tools/ can fail a DEFAULT tools/verify.sh run iff "
                          "verify.sh runs it as a command word of its own body, or ./gradlew test "
                          "runs it through an Exec task :test depends on"),
            "deliberatelyNoSelfTestFilter": ("a failing self-test fails the same run; the filter "
                                             "separates gates from self-tests, which is a "
                                             "different question"),
            "distinctBy": "os.path.basename",
            "reachabilitySource": 'tasks.named("test") { dependsOn(...) } and nothing else',
            "wiringShapesResolved": [
                'commandLine("$projectDir/tools/x.py")',
                'commandLine(mutationSnapshotArguments("x.py"))',
            ],
            "resolutionImportedFrom": ("tools/P-31-harness-census.py -- strip_kotlin_comments, "
                                       "command_line_spans and shell_command_words, so use-not-"
                                       "mention (C-0206) has ONE implementation"),
            "whyThisRef": ("d9a3522 is HEAD as this task's Formulate began, PINNED rather than "
                           "defaulted: on a shared checkout HEAD moves while a claim is being "
                           "drafted, and a corpus-subject emitter defaulting to HEAD re-bases its "
                           "own measurement between the draft and the emission (CH-0246, met "
                           "within one task rather than across iterations). The reading is "
                           "UNCHANGED at 23e2c58 and 5c0229a, and neither commit touches "
                           "build.gradle.kts or tools/"),
        },
        "atBaselineRef": at_ref,
        "atThisPassesTree": at_tree,
        "theCensusMovesItsOwnAnswer": {
            "atBaselineRef": at_ref["distinctToolsThatCanFailADefaultVerifyShRun"],
            "atThisPassesTree": at_tree["distinctToolsThatCanFailADefaultVerifyShRun"],
            "why": ("wiring this census into build.gradle.kts adds tools/T-334-gate-census.py and "
                    "tools/T-334-mutation-test.py to the set it counts (CH-0182); the task file "
                    "predicted +1 from the TASK count and the answer is a TOOL count"),
        },
        "againstWhatIsInPrint": [],
        "armOneAtThreeRefs": [],
    }
    for published_ref, published, counted_helper, owner, how in PUBLISHED:
        tree = gate.Tree(published_ref)
        split = gate.decomposition(published, gate.census(tree), counted_helper=counted_helper)
        document["againstWhatIsInPrint"].append({
            "ref": tree.ref,
            "refRequested": published_ref,
            "publishedBy": owner,
            "published": published,
            "howItWasBuilt": how,
            "countedTheHelperShape": counted_helper,
            "reachabilityUnion": split["union"],
            "difference": split["difference"],
            "reconstructsPublished": split["reconstructsPublished"],
            "terms": split["terms"],
        })
    for label, tree in (("C-0210 baseline", older), ("C-0220 baseline", gate.Tree("d7b7074")),
                        ("this pass's tree", working)):
        reading = gate.census(tree)
        document["armOneAtThreeRefs"].append({
            "state": label,
            "ref": reading["ref"],
            "unreachable": len(reading["routeBUnreachable"]),
            "declaredByHand": len(reading["declaredByHand"]),
            "equal": set(reading["declaredByHand"]) == set(reading["routeBUnreachable"]),
        })
    return header.with_emission_header(document, "none", regime=[])


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    document = build("71d126e")
    ok("T-334-emit the emission header is first and states no lattice and no regime",
       list(document)[0] == "emission"
       and document["emission"] == {"lattice": "none", "regime": []})
    ok("T-334-emit the resolved sha is recorded beside the ref that was asked for",
       len(document["baselineRef"]) == 40
       and document["baselineRefRequested"] == "71d126e")
    ok("T-334-emit the baseline reading is the ref's and not the working tree's",
       document["atBaselineRef"]["ref"] == document["baselineRef"]
       and document["atThisPassesTree"]["ref"] is None)
    ok("T-334-emit every published figure is decomposed into three terms that sum to the difference",
       all(sum(row["terms"].values()) == row["difference"]
           for row in document["againstWhatIsInPrint"]))
    ok("T-334-emit every published figure is reconstructed from its own two parts, so the "
       "decomposition is about the construction the publisher actually used",
       all(row["reconstructsPublished"] for row in document["againstWhatIsInPrint"]))
    ok("T-334-emit arm 1 is recorded at three states, not asserted at one",
       len(document["armOneAtThreeRefs"]) == 3)
    ok("T-334-emit the file states what it does not count",
       set(document["atBaselineRef"]["notCounted"])
       == {"execTasksUnreachableFromTest", "reachedOnlyByImport", "sourcedByVerifySh",
           "theKotlinSuite", "nonDefaultInvocations"})
    ok("T-334-emit the union is the two routes de-duplicated, never their sum",
       document["atBaselineRef"]["distinctToolsThatCanFailADefaultVerifyShRun"]
       == document["atBaselineRef"]["routeA"]["count"]
       + document["atBaselineRef"]["routeB"]["count"]
       - document["atBaselineRef"]["overlap"]["count"])
    ok("T-334-emit the document is JSON-serialisable with no non-finite and no set",
       json.dumps(document, allow_nan=False))
    ok("T-334-emit two builds at one ref are byte-identical",
       json.dumps(build("71d126e"), indent=2, sort_keys=False)
       == json.dumps(document, indent=2, sort_keys=False))

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def main(argv=None):
    parser = argparse.ArgumentParser(description="Emit T-334's gate census as a result file.")
    parser.add_argument("--ref", default="d9a3522",
                        help="the git ref to measure (default d9a3522, this task's pinned "
                             "baseline; see parameters.whyThisRef)")
    parser.add_argument("--out", default=DEFAULT_OUT, help="where to write the result file")
    parser.add_argument("--self-test", dest="self_test", action="store_true")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    document = build(arguments.ref)
    with open(arguments.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(arguments.out, ROOT))
    print("# %d distinct tool(s) can fail a default tools/verify.sh run at %s (%d at this tree)"
          % (document["atBaselineRef"]["distinctToolsThatCanFailADefaultVerifyShRun"],
             arguments.ref,
             document["atThisPassesTree"]["distinctToolsThatCanFailADefaultVerifyShRun"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
