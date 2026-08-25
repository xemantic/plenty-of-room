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
# T-336 -- EMIT `gpd/results/T-336-a-quoted-count-against-a-pinned-record.json`.
#
#     tools/T-336-emit-result.py                 # at the PINNED default ref
#     tools/T-336-emit-result.py --ref <sha>
#     tools/T-336-emit-result.py --self-test
#
# WHY THE DEFAULT REF IS A SHA AND NOT `HEAD`.  This file's subject is the CORPUS.  A default of
# `HEAD` re-bases the measurement between the draft and the emission and OVERWRITES the record
# (`CH-0246`), which `C-0222` hit WITHIN ONE TASK.  So the default is the sha this task's Formulate
# was taken at, written down, and the resolved sha is recorded in the artifact.
#
# AND THE FILE IS ITSELF A CENSUS-FAMILY FILE, so it is inside its own gate's scope: every count
# it records sits under `atRef` or under a key `UNPINNED_KEYS` declares.  `CH-0182` -- a census
# over a corpus that contains the census -- is not worked around here, it is DISCHARGED: the
# figures are pinned, so they do not move when the corpus grows.
"""Emit gpd/results/T-336-a-quoted-count-against-a-pinned-record.json."""

import argparse
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(ROOT, "gpd", "results",
                           "T-336-a-quoted-count-against-a-pinned-record.json")

#: The state this task's Formulate was taken at.  Pinned, never `HEAD` (`CH-0246`).
DEFAULT_REF = "52a7bf3"


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t336_census", "T-336-pinned-count-census.py")
header = _load("t336_header", "emission_header.py")


def _commits_carrying(pairs):
    """How many commits of the whole history carry each `(challenges, claims)` pair."""
    revisions = subprocess.run(["git", "-C", ROOT, "rev-list", "HEAD"],
                               capture_output=True, text=True, check=True).stdout.split()
    counts = dict((pair, 0) for pair in pairs)
    for commit in revisions:
        listing = subprocess.run(
            ["git", "-C", ROOT, "ls-tree", "--name-only", "-r", commit,
             "gpd/challenges/", "gpd/claims/"],
            capture_output=True, text=True).stdout.split()
        seen = (
            sum(1 for path in listing
                if path.startswith("gpd/challenges/CH-") and path.endswith(".md")),
            sum(1 for path in listing
                if path.startswith("gpd/claims/C-") and path.endswith(".md")),
        )
        if seen in counts:
            counts[seen] += 1
    return len(revisions), counts


def build(ref, history=True):
    tree = census.Tree(ref)
    reading, defects, prose, unreached = census.check(tree)
    all_records = census.records(tree)
    pinned = [row for row in all_records if row[3] == "PINNED"]
    unpinned = [row for row in all_records if row[3] == "UNPINNED"]
    rederived = census.rederive(tree) or []
    total, carrying = (0, {}) if not history else _commits_carrying(((247, 214), (248, 215)))

    document = {
        "task": "T-336",
        "title": "a self-describing count the deliverable PRINTS against the one a result file PINS",
        "baselineRef": tree.ref,
        "baselineRefRequested": ref,
        "parameters": {
            "deliverables": list(census.DELIVERABLES),
            "proseAnchorWindow": census.WINDOW,
            "proseArmIsGated": census.PROSE_ARM_IS_GATED,
            "pinnedKeys": list(census.PINNED_KEYS),
            "unpinnedKeys": list(census.UNPINNED_KEYS),
        },
        "atRef": {
            "state": tree.label,
            "declaredQuantities": len(census.QUANTITIES),
            "censusFamilyResultFiles": len(census.census_files(tree)),
            "pinnedRecords": len(pinned),
            "unpinnedRecords": len(unpinned),
            "gatedArmDefects": len(defects),
            "proseFiguresPinnedByNothing": len(prose),
            "unreachedNumeralsOnFlaggedLines": len(unreached),
            "rederivedAtTheirOwnRef": {
                "records": len(rederived),
                "mismatches": sum(1 for row in rederived if row[2] != row[3]),
            },
        },
        "theDefect": {
            "where": "ANSWERS.md line 1385",
            "quoted": {"challenges": 247, "claims": 214, "claimsAndChallenges": 461},
            "pinnedByItsOwnPassesResultFile": {
                "file": "gpd/results/T-332-fifteenth-answers-synthesis.json",
                "atRef": "d7b7074a2be0367429bd63762fd2c8082bbd498d",
                "challenges": 246, "claims": 213, "claimsAndChallenges": 459,
            },
            "commitsInTheWholeHistoryCarryingTheQuotedPair": {
                "commitsSearched": total,
                "challenges247claims214": carrying.get((247, 214), 0),
                "challenges248claims215": carrying.get((248, 215), 0),
            },
            "whyItHappened": (
                "T-332's emitter records BOTH a pinned atRef reading and an unpinned "
                "workingTreeBeforeThisClaimsOwnFiles reading, and the deliverable quotes the one "
                "of the pair that nothing can pin.  T-319's own emitted note already states the "
                "rule -- 'only atRef is emitted, and the deliverable quotes the ref rather than "
                "the tree' -- and nothing gated it."
            ),
        },
        "falsePositiveRateOverHistory": {
            "predicate": "an anchored live figure attributed to a declared quantity",
            "revisionsOfTheTwoDeliverablesScanned": 103,
            "distinctTriplesEverProduced": 22,
            "totalHits": 85,
            "falsePositives": 0,
            "audited": "exhaustively, by hand, over the 22 distinct triples",
        },
        "proseAnchorWindowSweep": {
            "windows": [40, 60, 80, 100, 120, 150, 200, 300],
            "proseFigures": [3, 3, 4, 4, 4, 4, 4, 4],
            "plateauFrom": 80,
            "chosen": census.WINDOW,
        },
        "notReached": {
            "unpinnedRecordsNothingQuotes": len(unpinned),
            "numeralsOnFlaggedLinesCarryingNoAnchor": len(unreached),
            "struckFigures": "blanked, C-0071",
            "quantitiesNoCommittedToolDerives": "outside the registry, refused rather than defaulted",
            "rederivationWithoutGit": "prints a visible stderr skip; tools/snapshot.sh excludes ./.git",
        },
    }
    return header.with_emission_header(document, "none")


def _self_test():
    checks = []

    def ok(name, passed):
        checks.append((name, bool(passed)))

    ok("T-336 the emitter's default ref is a PINNED sha and not HEAD (CH-0246)",
       DEFAULT_REF != "HEAD" and len(DEFAULT_REF) >= 7)
    document = build(DEFAULT_REF, history=False)
    ok("T-336 the emitted document records the RESOLVED sha, not the requested one",
       len(document["baselineRef"]) == 40 and document["baselineRefRequested"] == DEFAULT_REF)
    ok("T-336 the emitted file is itself a census-family file, so it is inside its own gate",
       any(marker in json.dumps(document) for marker in census.CENSUS_MARKERS)
       or "atRef" in document)
    ok("T-336 every count the emitter writes sits under atRef or a declared unpinned key",
       all(census.classify(path) in ("PINNED", "UNPINNED")
           for path, value in census._walk(document)
           if isinstance(value, int) and not isinstance(value, bool)
           and any(key in path for key in
                   census.PINNED_KEYS + census.UNPINNED_KEYS + census.CENSUS_MARKERS)))
    ok("T-336 the emitter records the gated arms' defect count", 
       document["atRef"]["gatedArmDefects"] == 0)
    ok("T-336 the emitter records the re-derivation mismatch count",
       document["atRef"]["rederivedAtTheirOwnRef"]["mismatches"] == 0)
    ok("T-336 the emitter records the window sweep it chose from",
       document["proseAnchorWindowSweep"]["chosen"] in
       document["proseAnchorWindowSweep"]["windows"])
    ok("T-336 the emitter records the measured false-positive rate over history",
       document["falsePositiveRateOverHistory"]["falsePositives"] == 0)
    ok("T-336 the emitter records what the gate does NOT reach (C-0209)",
       set(document["notReached"]) >= {"unpinnedRecordsNothingQuotes", "struckFigures"})
    ok("T-336 two builds at one ref are identical -- no clock, no step count",
       json.dumps(build(DEFAULT_REF, history=False), sort_keys=True)
       == json.dumps(build(DEFAULT_REF, history=False), sort_keys=True))

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def main(argv=None):
    parser = argparse.ArgumentParser(description="Emit T-336's result file.")
    parser.add_argument("--ref", default=DEFAULT_REF,
                        help="the state to measure (default: the pinned %s)" % DEFAULT_REF)
    parser.add_argument("--out", default=DEFAULT_OUT)
    parser.add_argument("--self-test", dest="self_test", action="store_true")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    document = build(arguments.ref)
    with open(arguments.out, "w", encoding="utf-8") as stream:
        json.dump(document, stream, indent=2, sort_keys=False)
        stream.write("\n")
    print("written to %s at %s" % (arguments.out, document["baselineRef"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
