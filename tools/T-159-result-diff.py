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
# The downstream diff of task T-159 (claim C-0096).
#
#     tools/T-159-result-diff.py --baseline <dir> --rerun <dir> --emit gpd/data/T-159-downstream-diff.json
#
# T-159 repairs `anchoring/TwoSpringElastica.kt`, a shared main source that produces published
# results.  `C-0031`/`P-15` set the discipline for that: re-run everything downstream, diff it, and
# CLASSIFY every movement -- one ulp, a declared tolerance, a quantity that is identically zero, or
# a real change -- rather than reporting that a diff exists.  This is the tool that does the
# classifying, and its output is an INPUT to `stability.DoublingLadderRepairStudyKt`, so the claim
# quotes numbers it read rather than numbers it retyped.
#
# The classification thresholds are the same ones `stability/DoublingLadderRepair.kt` applies in
# `classifyMovement`, and a gate test asserts the two agree.  They are chosen from the EMISSION
# precision and not from the model's accuracy (`CLAUDE.md`): every result file in this tree is
# rounded at the serialisation boundary, so a diff that appears at all is already at least one unit
# in the last emitted significant digit.
#
# Non-numeric fields -- strings, booleans, integers -- are classified as `a decision`, because they
# are not rounded and cannot move by a last-ulp jitter.  A moved decision is a moved verdict, and a
# moved verdict is a challenge, not a diff.
import argparse
import json
import os
import re
import sys

EMITTED_FIELD_MOVEMENT = 1.0e-8
VANISHING_FIELD_MOVEMENT = 1.0e-9
SOLVER_TOLERANCE = 1.0e-4
ABSOLUTE_FLOOR = 1.0e-12

DIGITS = re.compile(r"[0-9.eE+-]+")


def classify(published, rerun):
    """Classify one moved field.  Mirrors `classifyMovement` in DoublingLadderRepair.kt."""
    if isinstance(published, bool) or isinstance(rerun, bool):
        return "a decision"
    if isinstance(published, (int, float)) and isinstance(rerun, (int, float)):
        difference = abs(float(rerun) - float(published))
        scale = max(abs(float(published)), abs(float(rerun)))
        if difference == 0.0:
            return "identical"
        if scale <= ABSOLUTE_FLOOR:
            return "a quantity that is identically zero"
        if difference / scale <= EMITTED_FIELD_MOVEMENT:
            return "one unit in the last emitted significant digit"
        if difference <= VANISHING_FIELD_MOVEMENT:
            return "a residual of a quantity that vanishes by construction"
        if difference / scale <= SOLVER_TOLERANCE:
            return "inside a declared solver tolerance"
        return "a real change"
    if isinstance(published, str) and isinstance(rerun, str):
        # a number emitted as a STRING is not rounded (CLAUDE.md), so a prose field carrying a
        # Double.toString() moves at the last ulp while saying exactly the same thing
        if DIGITS.sub("#", published) == DIGITS.sub("#", rerun):
            return "a number carried inside an unrounded string"
        return "a decision"
    return "a decision"


def magnitudes(published, rerun):
    if isinstance(published, (int, float)) and isinstance(rerun, (int, float)) \
            and not isinstance(published, bool) and not isinstance(rerun, bool):
        difference = abs(float(rerun) - float(published))
        scale = max(abs(float(published)), abs(float(rerun)))
        return (difference / scale if scale > 0 else 0.0), difference
    return 0.0, 0.0


def walk(published, rerun, path, moved, counted):
    """Compare two parsed JSON documents, appending every moved leaf to `moved`."""
    if isinstance(published, dict) and isinstance(rerun, dict):
        for key in sorted(set(published) | set(rerun)):
            if key not in published or key not in rerun:
                moved.append((path + "." + key, published.get(key), rerun.get(key),
                              "a decision", 0.0, 0.0))
                continue
            walk(published[key], rerun[key], path + "." + key, moved, counted)
        return
    if isinstance(published, list) and isinstance(rerun, list):
        if len(published) != len(rerun):
            moved.append((path + ".length", len(published), len(rerun), "a decision", 0.0, 0.0))
            return
        for index, (one, other) in enumerate(zip(published, rerun)):
            walk(one, other, "%s[%d]" % (path, index), moved, counted)
        return
    counted[0] += 1
    if published == rerun:
        return
    relative, absolute = magnitudes(published, rerun)
    moved.append((path, published, rerun, classify(published, rerun), relative, absolute))


T149 = "T-149-recommended-element-fold.json"

T149_ROW_FIELDS = [
    "model", "layerHeight", "concentration", "loadLine", "layerStrokeCeiling", "strokeCeiling",
    "strokeCeilingOwner", "bindingCeiling", "biasMargin", "biasMarginIgnoringElementBoundary",
    "pullInStroke", "verdict",
]


def main():
    parser = argparse.ArgumentParser(description="T-159's classified downstream diff")
    parser.add_argument("--baseline", required=True, help="directory of the inherited result files")
    parser.add_argument("--rerun", required=True, help="directory of the re-run result files")
    parser.add_argument("--emit", required=True, help="the JSON to write")
    parser.add_argument("--only", nargs="*", default=None, help="restrict to these file names")
    arguments = parser.parse_args()

    names = sorted(arguments.only) if arguments.only else sorted(
        name for name in os.listdir(arguments.rerun) if name.endswith(".json")
    )
    files = []
    movements = []
    for name in names:
        baseline_path = os.path.join(arguments.baseline, name)
        rerun_path = os.path.join(arguments.rerun, name)
        if not os.path.exists(baseline_path) or not os.path.exists(rerun_path):
            continue
        with open(baseline_path) as handle:
            published = json.load(handle)
        with open(rerun_path) as handle:
            rerun = json.load(handle)
        moved = []
        counted = [0]
        walk(published, rerun, "", moved, counted)
        if not moved:
            continue
        classifications = {}
        for _, _, _, kind, _, _ in moved:
            classifications[kind] = classifications.get(kind, 0) + 1
        worst_relative = max((row[4] for row in moved), default=0.0)
        worst_absolute = max((row[5] for row in moved), default=0.0)
        files.append({
            "file": name,
            "task": name.split("-")[0] + "-" + name.split("-")[1],
            "comparedFields": counted[0],
            "movedFields": len(moved),
            "worstRelative": worst_relative,
            "worstAbsolute": worst_absolute,
            "classifications": classifications,
            "verdict": "a decision moved" if "a decision" in classifications
            else ("a real change" if "a real change" in classifications
                  else ("inside a declared solver tolerance"
                        if "inside a declared solver tolerance" in classifications
                        else "beneath the emitted precision")),
        })
        for path, was, now, kind, relative, absolute in moved:
            movements.append({
                "file": name,
                "path": path,
                "published": was if not isinstance(was, (dict, list)) else str(was),
                "rerun": now if not isinstance(now, (dict, list)) else str(now),
                "classification": kind,
                "relative": relative,
                "absolute": absolute,
            })

    rows = []
    rerun_t149 = os.path.join(arguments.rerun, T149)
    if os.path.exists(rerun_t149):
        with open(rerun_t149) as handle:
            for row in json.load(handle)["folds"]:
                rows.append({field: row.get(field) for field in T149_ROW_FIELDS})

    document = {
        "producedBy": "tools/T-159-result-diff.py",
        "task": "T-159",
        "what": "every study that consumes anchoring/TwoSpringElastica.kt, re-run against the "
                "repaired branch continuation and diffed field by field against the result file "
                "this iteration inherited",
        "thresholds": {
            "oneUnitInTheLastEmittedDigit": EMITTED_FIELD_MOVEMENT,
            "declaredSolverTolerance": SOLVER_TOLERANCE,
            "absoluteFloor": ABSOLUTE_FLOOR,
        },
        "filesCompared": len(names),
        "filesMoved": len(files),
        "files": files,
        "movements": movements,
        "t149Rows": rows,
    }
    with open(arguments.emit, "w") as handle:
        json.dump(document, handle, indent=2, sort_keys=False)
        handle.write("\n")
    for entry in files:
        print("%-48s %4d/%-6d moved  worst %.3e  %s" % (
            entry["file"], entry["movedFields"], entry["comparedFields"],
            entry["worstRelative"], entry["verdict"]))
    print("wrote %s (%d files moved of %d compared, %d movements, %d T-149 rows)" % (
        arguments.emit, len(files), len(names), len(movements), len(rows)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
