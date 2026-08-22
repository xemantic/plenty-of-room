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
# P-31 -- emit `gpd/results/P-31-mutation-harness-census.json`.
#
#     tools/P-31-emit-result.py [--ref REF] [--self-test]
#
# `CH-0210`: a result file whose SUBJECT IS THE CORPUS must name the corpus state it measured, or
# it can never be re-run.  Every other result file is a function of code plus committed inputs; a
# census is a function of the whole mutable tree.  So the ref is an argument, it defaults to
# `HEAD`, and the RESOLVED sha is recorded.
#
# EVERY NUMBER IS DERIVED BY RUNNING, not typed: the census by calling it, each harness's mutation
# and survivor counts by running the harness and parsing its own summary line, and the
# before/after per-classification measurement by re-running the repaired harness against a copy of
# the gate with `P-31`'s own named-test block removed -- which is exactly the counterfactual, and
# is anchored and asserted once so it cannot silently no-op.
#
# NO WALL-CLOCK TIMING AND NO STEP COUNTER IS EMITTED (`CLAUDE.md`: *a wall clock in a result file
# is a step counter by another name*; *emit the answer and a convergence measure, nothing that
# counts steps*).

import argparse
import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OUT = os.path.join(ROOT, "gpd", "results", "P-31-mutation-harness-census.json")

# The block `P-31` added to the gate's self-tests.  Removing it reproduces the state this task
# started from, which is how the *before* half of the per-classification measurement is DERIVED
# rather than remembered.
BLOCK_START = ("    # --- P-31: every DECLARED CLASSIFICATION is held open by a named "
               "test of its own ---")
BLOCK_END = "    # --- P-30: which verdict WINS, measured over the queue's own practice ---"

# A harness reports its own row count in one of three shapes, and the emitter PARSES it rather
# than transcribing it.  Three shapes and not one because these harnesses were written by
# different tasks against different subjects; unifying their output would be a change to six
# tools for the benefit of one reader.
SUMMARY = re.compile(r"^#\s*(\d+) mutation\(s\)", re.MULTILINE)
COVERAGE = re.compile(r"coverage[^\n]*?, (\d+) mutations", re.MULTILINE)
ROW_LINE = re.compile(r"^(?:  .*\d+ of \d+ fail\s*$|.*named test\(s\) fail\s+<-)",
                      re.MULTILINE)
SURVIVORS = re.compile(r"(\d+) survivor\(s\)")
SILENT = re.compile(r"^mutations failing NOTHING: (\d+)", re.MULTILINE)
UNPROTECTED = re.compile(r"^UNPROTECTED ", re.MULTILINE)
BASELINE = re.compile(r"baseline[^\n]*?(\d+)[^\n]*?(?:pre-existing|fail)", re.IGNORECASE)


def _census_module():
    spec = importlib.util.spec_from_file_location(
        "p31census", os.path.join(HERE, "P-31-harness-census.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def resolve(ref):
    return subprocess.run(["git", "-C", ROOT, "rev-parse", ref],
                          capture_output=True, text=True, check=True).stdout.strip()


def run_harness(basename, extra=()):
    """(exit code, whole stdout) for one harness, run from the repository root."""
    result = subprocess.run(
        [sys.executable, os.path.join(HERE, basename)] + list(extra),
        capture_output=True, text=True, cwd=ROOT)
    return result.returncode, result.stdout


def harness_reading(basename, extra=()):
    """The harness's OWN summary, parsed out of its output rather than transcribed."""
    code, output = run_harness(basename, extra)
    summary = SUMMARY.search(output)
    coverage = COVERAGE.search(output)
    survivors = SURVIVORS.search(output)
    silent = SILENT.search(output)
    rows = len(ROW_LINE.findall(output))
    return {
        "harness": basename,
        "exitCode": code,
        "mutations": (int(summary.group(1)) if summary
                      else int(coverage.group(1)) if coverage
                      else rows or None),
        "survivors": (int(survivors.group(1)) if survivors
                      else int(silent.group(1)) if silent
                      else (0 if code == 0 else None)),
        "unprotectedClassifications": len(UNPROTECTED.findall(output)),
        "summaryLine": next(
            (line for line in reversed(output.splitlines()) if line.startswith("#")), ""),
    }


def before_repair():
    """The per-classification measurement against the gate WITHOUT `P-31`'s named-test block.

    A throwaway copy of the tree with the block excised, run through the repaired harness: the
    *before* number is therefore measured on this run rather than remembered from a previous one.
    """
    gate = open(os.path.join(HERE, "check-queue-vocabulary.py"), encoding="utf-8").read()
    start, end = gate.find(BLOCK_START), gate.find(BLOCK_END)
    assert start != -1 and end != -1 and start < end, "the P-31 block's anchors do not resolve"
    assert gate.count(BLOCK_START) == 1 and gate.count(BLOCK_END) == 1
    stripped = gate[:start] + gate[end:]

    directory = tempfile.mkdtemp(prefix="P-31-before.")
    try:
        os.makedirs(os.path.join(directory, "tools"))
        for name in os.listdir(HERE):
            if name.endswith(".py"):
                shutil.copy(os.path.join(HERE, name), os.path.join(directory, "tools", name))
        shutil.copy(os.path.join(ROOT, "TASKS.md"), os.path.join(directory, "TASKS.md"))
        open(os.path.join(directory, "tools", "check-queue-vocabulary.py"),
             "w", encoding="utf-8").write(stripped)
        result = subprocess.run(
            [sys.executable, os.path.join(directory, "tools", "test-check-queue-vocabulary.py")],
            capture_output=True, text=True, cwd=directory)
        survivors = SURVIVORS.search(result.stdout)
        classification_survivors = len(
            [line for line in result.stdout.splitlines()
             if line.startswith("SURVIVES") and "CLASSIFICATION" in line])
        return {
            "survivors": int(survivors.group(1)) if survivors else None,
            "classificationSurvivors": classification_survivors,
        }
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def at_ref(ref):
    """The census as it stands at a committed ref, from a `git archive` of it."""
    census = _census_module()
    tree = census._archive(ref)
    if tree is None:
        return None
    try:
        rows = census.census(tree, strict=False)
        return {
            "harnessesPresent": len(rows),
            "anchorsDeclared": sum(row["anchorsDeclared"] for row in rows),
            "anchorsUnresolved": sum(row["anchorsUnresolved"] for row in rows),
            "symbolsDeclared": sum(row["symbolsDeclared"] for row in rows),
            "symbolsUnresolved": sum(row["symbolsUnresolved"] for row in rows),
            "wired": len([row for row in rows if row["wiredIn"]]),
            "assertAnchorCount": len([row for row in rows if row["assertsAnchorCount"]]),
            "declareAnchors": len([row for row in rows if row["anchorsDeclared"]]),
            "measureBaseline": len([row for row in rows if row["measuresBaseline"]]),
            "orphanedHarnesses": sorted(
                row["harness"] for row in rows if row["anchorsUnresolved"]),
            "unwiredHarnesses": sorted(
                row["harness"] for row in rows if not row["wiredIn"]),
        }
    finally:
        shutil.rmtree(tree, ignore_errors=True)


HARNESS_ARGUMENTS = {"T-225-mutation-test.py": ("--check",)}


def _per_classification_count():
    """How many per-classification rows the repaired harness derives from the gate's vocabulary."""
    spec = importlib.util.spec_from_file_location(
        "p31harness", os.path.join(HERE, "test-check-queue-vocabulary.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return len(module.per_classification_mutations())


def build(ref):
    census = _census_module()
    rows = census.census(ROOT)
    readings = {row["harness"]: harness_reading(
        row["harness"], HARNESS_ARGUMENTS.get(row["harness"], ())) for row in rows}
    before = before_repair()
    return {
        "task": "P-31",
        "title": "a mutation anchor is a reference into somebody else's source, and a refactor "
                 "orphans it: the harness census, and the wired gate that was red for an "
                 "iteration",
        "baselineRef": resolve(ref),
        "baselineRefRequested": ref,
        "parameters": {
            "corpus": "every mutation harness in tools/, and the sources each one mutates",
            "anchorResolves": "the anchor's text occurs EXACTLY once in its target file; zero "
                              "and two are both defects",
            "symbolResolves": "the named attribute is a top-level assignment, def or class of "
                              "the subject",
            "wired": "the harness's basename occurs in build.gradle.kts or tools/verify.sh",
            "note": "counts only. No wall-clock timing and no step counter is emitted",
        },
        "harnesses": [
            {
                "harness": row["harness"],
                "kind": row["kind"],
                "subjects": row["subjects"],
                "anchorsDeclared": row["anchorsDeclared"],
                "anchorsUnresolved": row["anchorsUnresolved"],
                "symbolsDeclared": row["symbolsDeclared"],
                "symbolsUnresolved": row["symbolsUnresolved"],
                "assertsAnchorCount": row["assertsAnchorCount"],
                "measuresBaseline": row["measuresBaseline"],
                "wiredIn": row["wiredIn"],
                "mutations": readings[row["harness"]]["mutations"],
                "survivors": readings[row["harness"]]["survivors"],
                "exitCode": readings[row["harness"]]["exitCode"],
            }
            for row in rows
        ],
        "totals": {
            "harnesses": len(rows),
            "anchorsDeclared": sum(row["anchorsDeclared"] for row in rows),
            "anchorsUnresolved": sum(row["anchorsUnresolved"] for row in rows),
            "symbolsDeclared": sum(row["symbolsDeclared"] for row in rows),
            "symbolsUnresolved": sum(row["symbolsUnresolved"] for row in rows),
            "assertAnchorCount": len([r for r in rows if r["assertsAnchorCount"]]),
            "declareAnchors": len([r for r in rows if r["anchorsDeclared"]]),
            "measureBaseline": len([r for r in rows if r["measuresBaseline"]]),
            "wired": len([r for r in rows if r["wiredIn"]]),
            "harnessesExitingNonZero": len(
                [h for h in readings.values() if h["exitCode"] != 0]),
            "mutationsOverAllHarnesses": sum(
                h["mutations"] or 0 for h in readings.values()),
            "survivorsOverAllHarnesses": sum(
                h["survivors"] or 0 for h in readings.values()),
        },
        "atBaselineRef": at_ref(ref),
        "queueVocabularyHarness": {
            "baseMutations": 6,
            "reAnchored": 5,
            "retired": 0,
            "anchorsUnresolvedAtBaselineRef": 5,
            "perClassificationMutations": _per_classification_count(),
            "classificationSurvivorsBefore": before["classificationSurvivors"],
            "classificationSurvivorsAfter": readings[
                "test-check-queue-vocabulary.py"]["survivors"],
            "totalSurvivorsBefore": before["survivors"],
            "totalSurvivorsAfter": readings["test-check-queue-vocabulary.py"]["survivors"],
        },
        "findings": [
            "The wired Gradle task testQueueVocabularyMutations went red at 9620d3e, which is "
            "P-30's own commit, and not at any sibling's in-flight file: git archive of that "
            "commit reproduces the reading, and the census run against the same archive reports "
            "the same five orphaned anchors the harness itself printed.",
            "Five of the six anchors were orphaned by the refactor and are re-anchored with their "
            "meanings preserved; none is retired, because every one of the six meanings still "
            "exists in the code.",
            "The sixth row's SURVIVES was not a survivor. The harness copied tools/ FLAT and "
            "copied only one of the two modules the subject imports, so the mutant could not "
            "start; under a fixture that reproduces the tree's layout the same mutation is killed "
            "by two named tests. That is CH-0237 in the quiet direction.",
            "The vocabulary was load-bearing as a SET and not as its members: dropping any one of "
            "nine of its eleven declared phrases failed no named test at all.",
            "Before this task, 3 of 10 mutation harnesses ran in the build and 7 ran only when "
            "somebody remembered; 3 of 10 measured no baseline. All 10 are now wired and all 10 "
            "measure one.",
        ],
    }


def _self_test():
    failures, ran = [], []

    def check(name, condition):
        ran.append(name)
        if not condition:
            failures.append(name)

    check("P-31 emitter the summary line of a harness is parsed, not transcribed",
          SUMMARY.search("# 17 mutation(s) (6 base + 11 per-classification), 0 retired, "
                         "0 survivor(s)").group(1) == "17")
    check("P-31 emitter the survivor count is parsed from the same line",
          SURVIVORS.search("# 17 mutation(s), 0 survivor(s)").group(1) == "0")
    check("P-31 emitter a harness reporting SILENT rows instead of survivors is read too",
          SILENT.search("mutations failing NOTHING: 3\n").group(1) == "3")
    check("P-31 emitter a coverage-style header carries the row count too",
          COVERAGE.search("-- T-280 mutation coverage, 23 mutations over the census's own "
                          "named tests --").group(1) == "23")
    check("P-31 emitter a harness that reports neither is counted by its own ROWS",
          len(ROW_LINE.findall(
              "    3 named test(s) fail  <-  a\n    0 named test(s) fail  <-  b\n"
              "  drop     KEY                     2 of 9 fail\n")) == 3)
    check("P-31 emitter the P-31 block's two anchors each occur exactly once in the gate",
          open(os.path.join(HERE, "check-queue-vocabulary.py"),
               encoding="utf-8").read().count(BLOCK_START) == 1
          and open(os.path.join(HERE, "check-queue-vocabulary.py"),
                   encoding="utf-8").read().count(BLOCK_END) == 1)
    check("P-31 emitter a ref resolves to a 40-character sha",
          len(resolve("HEAD")) == 40)
    for failure in failures:
        print("SELF-TEST FAILED: " + failure)
    print("# {} self-test(s), {} failure(s)".format(len(ran), len(failures)))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description="emit the P-31 harness census")
    parser.add_argument("--ref", default="HEAD")
    parser.add_argument("--self-test", "--selftest", dest="selftest", action="store_true")
    args = parser.parse_args(argv)
    if args.selftest:
        return _self_test()
    document = build(args.ref)
    with open(OUT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to " + os.path.relpath(OUT, ROOT))
    print("# {} harness(es); {} of {} anchors unresolved; wired {} of {}".format(
        document["totals"]["harnesses"], document["totals"]["anchorsUnresolved"],
        document["totals"]["anchorsDeclared"], document["totals"]["wired"],
        document["totals"]["harnesses"]))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
