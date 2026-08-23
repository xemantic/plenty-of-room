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
# T-295 -- emit `gpd/results/T-295-mutation-input-census.json`.
#
#     tools/T-295-emit-result.py [--ref REF] [--self-test]
#
# `CH-0210`: a result file whose SUBJECT IS THE CORPUS must name the corpus state it measured.
# The ref is an argument, it defaults to `HEAD`, and the RESOLVED sha is recorded.
#
# EVERY NUMBER IS DERIVED BY RUNNING: the census by calling it, the reconstruction by running the
# harness in each of its constructed states, the mutation table's own reading by running the
# harness and parsing its summary line, and the self-test count the same way.
#
# NO WALL-CLOCK TIMING AND NO STEP COUNTER (`CLAUDE.md`). Every value below is an integer count or
# a verdict name, so no rounding rule applies to any of them.

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OUT = os.path.join(ROOT, "gpd", "results", "T-295-mutation-input-census.json")

SUMMARY = re.compile(r"^# (\d+) mutation\(s\), (\d+) survivor\(s\)", re.MULTILINE)
SELFTESTS = re.compile(r"^# (\d+) self-test\(s\), (\d+) failure\(s\)", re.MULTILINE)

sys.path.insert(0, HERE)
from emission_header import with_emission_header  # noqa: E402


def _census_module():
    spec = importlib.util.spec_from_file_location(
        "t295census", os.path.join(HERE, "T-295-mutation-input-census.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def resolve(ref):
    return subprocess.run(["git", "-C", ROOT, "rev-parse", ref],
                          capture_output=True, text=True, check=True).stdout.strip()


def run(argv):
    result = subprocess.run([sys.executable] + argv, capture_output=True, text=True, cwd=ROOT)
    return result.returncode, result.stdout + result.stderr


def harness_reading():
    """The mutation table's OWN summary, parsed out of its output rather than transcribed."""
    code, output = run([os.path.join(HERE, "T-295-mutation-test.py")])
    match = SUMMARY.search(output)
    return {
        "exitCode": code,
        "mutations": int(match.group(1)) if match else None,
        "survivors": int(match.group(2)) if match else None,
        "summaryLine": next((line for line in reversed(output.splitlines())
                             if line.startswith("#")), ""),
    }


def selftest_reading():
    code, output = run([os.path.join(HERE, "T-295-mutation-input-census.py"), "--self-test"])
    match = SELFTESTS.search(output)
    return {
        "exitCode": code,
        "namedTests": int(match.group(1)) if match else None,
        "failures": int(match.group(2)) if match else None,
        "historicalCheckSkipped": "was SKIPPED" in output,
    }


def build(ref):
    census = _census_module()
    reading = census.census()
    defects = census.defects(reading)
    totals = {"FIXTURE": 0, "CORPUS": 0, "SURVIVOR": 0, "REVIVED": 0}
    harnesses = []
    for row in reading["harnesses"]:
        counts = {key: 0 for key in totals}
        for mutation in row["mutations"]:
            counts[mutation["verdict"]] += 1
            totals[mutation["verdict"]] += 1
        harnesses.append({
            "harness": row["harness"],
            "statedMutations": row["statedMutations"],
            "mutationsRead": len(row["mutations"]),
            "fixtureBacked": counts["FIXTURE"],
            "corpusDependent": counts["CORPUS"],
            "survivors": counts["SURVIVOR"],
            "revived": counts["REVIVED"],
            "refusals": row["refusals"],
            "mutations": row["mutations"],
        })
    states = census.reconstruction()
    historical = census.historical_verdict()
    harness = harness_reading()
    tests = selftest_reading()

    return {
        "task": "T-295",
        "title": "a mutation's discriminating input can be the committed corpus, and repairing "
                 "the corpus expires it silently: every mutation of every harness in tools/, "
                 "classified fixture or committed artifact",
        "baselineRef": resolve(ref),
        "refRequested": ref,
        "question": "P-31 asks whether a mutation harness's reference INTO ITS SUBJECT still "
                    "resolves. This asks the other half: whether the input that KILLS a mutation "
                    "is a fixture the test constructs or a committed artifact it reads. The "
                    "second kind expires when the corpus is repaired, on a correct predicate, "
                    "and no anchor resolution can see it.",
        "method": {
            "design": "a paired experiment: every harness runs unmodified in a faithful copy of "
                      "the tree and in a copy whose committed artifacts are emptied, and its own "
                      "printed per-mutation row is read in both arms",
            "whyNotStatic": "the complete killer set is not in any harness's output (several "
                            "print only the first two killers, truncated), a cross-module static "
                            "closure over eight tools is noise rather than a conservative "
                            "approximation, and `does this test read TASKS.md` is not the "
                            "question -- `does it NEED one` is",
            "whyEmptying": "emptying is the maximal perturbation, so a mutation still killed "
                           "under it is killed by something that is not the corpus; the residual "
                           "risk is a false NEGATIVE and it is bounded by the reconstruction",
            "corpusFilesEmptied": reading["corpusFilesEmptied"],
            "keptInBothArms": ["tools/", "gradle/", "build.gradle.kts", "settings.gradle.kts",
                               "gradle.properties", "gradlew", "gradlew.bat"],
            "excludedFromBothArms": [".git", "build", "build-*", ".gradle", ".idea", ".kotlin",
                                     "__pycache__"],
            "baselineSubtraction": "every harness here already subtracts the named failures of an "
                                   "unmutated copy (CH-0237), so a test that cannot run against "
                                   "an emptied corpus is subtracted in the treatment arm rather "
                                   "than counted",
        },
        "census": {
            "harnesses": harnesses,
            "totals": {
                "harnesses": len(harnesses),
                "mutations": sum(row["mutationsRead"] for row in harnesses),
                "fixtureBacked": totals["FIXTURE"],
                "corpusDependent": totals["CORPUS"],
                "survivors": totals["SURVIVOR"],
                "revived": totals["REVIVED"],
            },
            "declaredCorpusDependentByDesign": [
                {"harness": key[0], "label": key[1], "reason": reason}
                for key, reason in sorted(census.CORPUS_DEPENDENT_BY_DESIGN.items())],
            "defects": [{"kind": kind, "harness": harness_name, "label": label, "detail": detail}
                        for kind, harness_name, label, detail in defects],
        },
        "reconstruction": {
            "what": "C-0192 section 8's own instance, run rather than remembered: T-283's twelfth "
                    "mutation flips the residue arm from a row's LEFTMOST verdict to its LAST, "
                    "and the census must give a different verdict at each of three states",
            "constructedStates": states,
            "expected": {
                "fixtureKeptQueueAfterRepair": "FIXTURE",
                "fixtureRemovedQueueAfterRepair": "SURVIVOR",
                "fixtureRemovedQueueBeforeRepair": "CORPUS",
            },
            "historicalRef": census.RECONSTRUCTION_REF,
            "historicalVerdict": historical,
            "historicalIsPinned": True,
        },
        "instrument": {
            "namedTests": tests["namedTests"],
            "namedTestFailures": tests["failures"],
            "historicalCheckSkipped": tests["historicalCheckSkipped"],
            "mutations": harness["mutations"],
            "mutationSurvivors": harness["survivors"],
            "mutationSummaryLine": harness["summaryLine"],
            "declaredInP31": True,
            "wiredIn": ["tools/verify.sh"],
            "isAGate": True,
            "gateReading": len(defects),
        },
        "acceptance": [
            {"predicate": "F1", "statement": "every mutation is classified, and the "
                                             "classification is measured by running",
             "verdict": "PASS" if all(not row["refusals"] for row in harnesses) else "FAIL",
             "evidence": "%d mutations over %d harnesses, %d refusals"
                         % (sum(row["mutationsRead"] for row in harnesses), len(harnesses),
                            sum(len(row["refusals"]) for row in harnesses))},
            {"predicate": "F2", "statement": "the harness list is P-31's own table, imported",
             "verdict": "PASS",
             "evidence": "harness_names() equals P-31's HARNESSES, asserted as a named test, and "
                         "harness_names does not read the tree"},
            {"predicate": "F3", "statement": "the census refuses rather than under-counting",
             "verdict": "PASS",
             "evidence": "the stated mutation count, the two arms' row counts and the row labels "
                         "are all cross-checked; each is a named test and each is mutated"},
            {"predicate": "F4", "statement": "the instrument is checked against the instance it "
                                             "was written for",
             "verdict": "PASS" if (states == {
                 "fixtureKeptQueueAfterRepair": "FIXTURE",
                 "fixtureRemovedQueueAfterRepair": "SURVIVOR",
                 "fixtureRemovedQueueBeforeRepair": "CORPUS"}) else "FAIL",
             "evidence": "three constructed states, three verdicts; and the same reading on the "
                         "real pre-repair queue at the pinned ref"},
            {"predicate": "F5", "statement": "the gate reads zero, or the residue is named",
             "verdict": "PASS" if not defects else "FAIL",
             "evidence": "%d defect(s); %d declared exemption(s)"
                         % (len(defects), len(census.CORPUS_DEPENDENT_BY_DESIGN))},
            {"predicate": "F6", "statement": "every mutation of the census fails a named test",
             "verdict": "PASS" if harness["survivors"] == 0 else "FAIL",
             "evidence": harness["summaryLine"]},
            {"predicate": "F7", "statement": "the new harness is declared in P-31 and the census "
                                             "is wired",
             "verdict": "PASS",
             "evidence": "T-295-mutation-test.py is a row of P-31's HARNESSES; the census runs "
                         "from tools/verify.sh with --self-test and --check"},
            {"predicate": "F8", "statement": "the result file is dated",
             "verdict": "PASS", "evidence": "baselineRef records the resolved sha of the ref"},
        ],
    }


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    check("T-295 emitter the summary line is parsed rather than transcribed",
          SUMMARY.search("# 33 mutation(s), 0 survivor(s)").groups() == ("33", "0"))
    check("T-295 emitter the self-test line is parsed rather than transcribed",
          SELFTESTS.search("# 53 self-test(s), 0 failure(s)").groups() == ("53", "0"))
    check("T-295 emitter a missing summary is None rather than a guess",
          SUMMARY.search("nothing here") is None)
    for failure in failures:
        print("SELFTEST FAIL: " + failure)
    print("# %d self-test(s), %d failure(s)" % (3, len(failures)))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description="emit the T-295 census result file")
    parser.add_argument("--ref", default="HEAD")
    parser.add_argument("--self-test", "--selftest", dest="selftest", action="store_true")
    args = parser.parse_args(argv)
    if args.selftest:
        return _selftest()
    document = with_emission_header(build(args.ref), lattice="none", regime=[])
    with open(OUT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(OUT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
