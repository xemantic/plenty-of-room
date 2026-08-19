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
# Task T-226 (claim C-0139) -- the pairwise emission diff of `T-113`.
#
#     tools/T-226-emission-diff.py <a.json> <b.json> [<c.json> ...] [--json <out>]
#     tools/T-226-emission-diff.py --self-test
#
# `C-0138` s8 measured that two runs of identical code emit `T-113` with 217 fields different, and
# that the moving block is always ONE descent record and its transfers.  This tool measures that
# ensemble-wise rather than pair-wise: over N emissions it reports, per field, the WIDTH (the
# spread over the ensemble) and a class, using `C-0135`'s own three-way classification.
#
#   VALUE   an objective a descent minimises -- `objective`, `peakDishing`, `worstDishing`, ...
#   POINT   a functional of a descent's ARGMIN -- a per-path stiffness, a peak ratio, a force
#           derived from `max_i k_i`, a distribution's own reported spread
#   OTHER   anything else.  `C-0135`'s `F1`: a closed-form field that moves falsifies the manifold
#           reading immediately, so `OTHER` must be 0 and it is the whole point of the classifier.
#
# A moved BOOLEAN or STRING is reported separately and never as a width: it is a decision, and a
# moved decision is a challenge rather than a diff (`tools/T-159-result-diff.py`, same discipline).
import argparse
import json
import re
import sys

#: field-name leaves that are an objective a descent reports
VALUE_KEYS = (
    "objective",
    "peakdishing",
    "worstdishing",
    "dishing",
    "dishingoverstroke",
    "minimaxworstoverstroke",
    "uniformoverstroke",
    "objectiveoverfloor",
    "improvementoveruniform",
    "lastsweepimprovementtotwodigits",
)

#: field-name leaves that are a functional of a descent's argmin
POINT_KEYS = (
    "stiffness",
    "stiffnesses",
    "peakpathstiffness",
    "shareoftheuniformpath",
    "forceatacceptablestroke",
    "thermalforce",
    "peakratio",
    "minimaxpeakratio",
    "peakthermalforce",
    "peakpathforceatacceptablestroke",
    "latticeoverplate",
    "maximumstiffness",
    "minimumstiffness",
    "stiffnessratio",
    # every one of these is read OFF a distribution, so at a descent's record it is a functional of
    # that descent's argmin. They were `OTHER` in this tool's first run and every one of the eleven
    # sat inside the minimax record -- the instrument was incomplete, not the study (`C-0139` F2).
    "maximumoverminimumstiffness",
    "pathsbelowatenthoftheuniformshare",
    "pathscarryinghalftheuniformshare",
    "peakcrossoverforce",
    "peakduplexshear",
    "peaksupportforce",
    "unzipmarginatacceptablestroke",
    "latticepeakdishing",
    "platepeakdishing",
    "latticeexcesspercent",
    "overtolerance",
)


def leaves(node, path=""):
    if isinstance(node, dict):
        for key, value in node.items():
            yield from leaves(value, f"{path}/{key}")
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from leaves(value, f"{path}/{index}")
    else:
        yield path, node


def leaf_name(path):
    return path.rsplit("/", 1)[-1].lower()


def classify(path):
    name = leaf_name(path)
    if name.isdigit():                       # an element of a bare array; use its owner's name
        name = path.rsplit("/", 2)[-2].lower()
    if name in POINT_KEYS:
        return "POINT"
    if name in VALUE_KEYS:
        return "VALUE"
    return "OTHER"


def block(path):
    """The record a field belongs to: `/distributions/24`, `/paths/181`, ..."""
    parts = path.split("/")
    if len(parts) >= 3 and parts[2].isdigit():
        return f"/{parts[1]}/{parts[2]}"
    return f"/{parts[1]}" if len(parts) >= 2 else path


def spread(values):
    numeric = [v for v in values if isinstance(v, (int, float)) and not isinstance(v, bool)]
    if len(numeric) != len(values) or not numeric:
        return None
    lo, hi = min(numeric), max(numeric)
    scale = max(abs(lo), abs(hi))
    if scale == 0.0:
        return 0.0
    return (hi - lo) / scale


def compare(documents):
    tables = [dict(leaves(document)) for document in documents]
    keys = set(tables[0])
    report = {
        "emissions": len(documents),
        "fields": len(keys),
        "keysDiffer": sorted(k for table in tables[1:] for k in set(table) ^ keys),
        "moved": [],
        "decisionsMoved": [],
    }
    for key in sorted(keys):
        values = [table.get(key) for table in tables]
        if all(v == values[0] for v in values[1:]):
            continue
        if any(isinstance(v, (bool, str)) or v is None for v in values):
            report["decisionsMoved"].append({"field": key, "values": values})
            continue
        report["moved"].append(
            {
                "field": key,
                "block": block(key),
                "class": classify(key),
                "width": spread(values),
                "values": values,
            }
        )
    report["movedCount"] = len(report["moved"])
    report["otherCount"] = sum(1 for m in report["moved"] if m["class"] == "OTHER")
    report["blocks"] = sorted({m["block"] for m in report["moved"]})
    report["widestByClass"] = {
        klass: max(
            (m["width"] for m in report["moved"] if m["class"] == klass and m["width"] is not None),
            default=0.0,
        )
        for klass in ("VALUE", "POINT", "OTHER")
    }
    return report


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("files", nargs="*")
    parser.add_argument("--json")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args(argv)
    if args.self_test:
        return self_test()
    if len(args.files) < 2:
        parser.error("at least two emissions are needed")
    documents = [json.load(open(f, encoding="utf-8")) for f in args.files]
    report = compare(documents)
    report["sources"] = args.files
    print(f"{report['emissions']} emission(s), {report['fields']} field(s)")
    print(f"  moved      {report['movedCount']}  (VALUE/POINT/OTHER classified)")
    print(f"  OTHER      {report['otherCount']}")
    print(f"  decisions  {len(report['decisionsMoved'])}")
    print(f"  blocks     {', '.join(report['blocks']) or 'none'}")
    for klass, width in report["widestByClass"].items():
        print(f"  widest {klass:<6} {width:.6g}")
    for entry in report["decisionsMoved"]:
        print(f"  DECISION {entry['field']}  {entry['values']}")
    if args.json:
        json.dump(report, open(args.json, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    return 1 if report["otherCount"] or report["decisionsMoved"] or report["keysDiffer"] else 0


# --------------------------------------------------------------------------- self-tests

def self_test():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    check("leaves flattens", dict(leaves({"a": {"b": 1}})) == {"/a/b": 1})
    check("leaves indexes lists", dict(leaves({"a": [1, 2]})) == {"/a/0": 1, "/a/1": 2})
    check("classify point", classify("/paths/3/stiffness") == "POINT")
    check("classify value", classify("/optimiser/12/objective") == "VALUE")
    check("classify other", classify("/runParameters/mesh") == "OTHER")
    check("an argmin functional is a POINT",
          classify("/distributions/24/maximumOverMinimumStiffness") == "POINT")
    check("a per-record peak dishing is a POINT",
          classify("/distributions/24/latticePeakDishing") == "POINT")
    check("classify a bare array by its owner", classify("/distributions/1/stiffnesses/7") == "POINT")
    check("block of a record", block("/distributions/24/objective") == "/distributions/24")
    check("block of a scalar", block("/temperature") == "/temperature")
    check("spread of equal values is zero", spread([1.0, 1.0]) == 0.0)
    check("spread is relative", abs(spread([1.0, 1.02]) - 0.02 / 1.02) < 1e-12)
    check("spread of zeros is zero", spread([0.0, 0.0]) == 0.0)
    check("spread refuses a string", spread(["a", "b"]) is None)

    a = {"x": 1.0, "s": "same", "b": True, "d": {"objective": 0.5}}
    b = {"x": 1.0, "s": "same", "b": True, "d": {"objective": 0.5}}
    report = compare([a, b])
    check("identical emissions move nothing", report["movedCount"] == 0)
    check("identical emissions move no decision", report["decisionsMoved"] == [])

    c = {"x": 1.0, "s": "same", "b": True, "d": {"objective": 0.6}}
    report = compare([a, c])
    check("a moved objective is one field", report["movedCount"] == 1)
    check("a moved objective is a VALUE", report["moved"][0]["class"] == "VALUE")
    check("a moved objective has no OTHER", report["otherCount"] == 0)

    e = {"x": 1.0, "s": "other", "b": True, "d": {"objective": 0.5}}
    report = compare([a, e])
    check("a moved string is a decision", len(report["decisionsMoved"]) == 1)
    check("a moved string is not a width", report["movedCount"] == 0)

    f = {"x": 2.0, "s": "same", "b": True, "d": {"objective": 0.5}}
    report = compare([a, f])
    check("a moved closed form is OTHER", report["otherCount"] == 1)

    g = {"x": 1.0, "s": "same", "b": False, "d": {"objective": 0.5}}
    report = compare([a, g])
    check("a moved boolean is a decision", len(report["decisionsMoved"]) == 1)

    h = {"x": 1.0, "s": "same", "b": True}
    report = compare([a, h])
    check("a missing key is reported", report["keysDiffer"] == ["/d/objective"])

    three = compare([a, b, c])
    check("three emissions are three", three["emissions"] == 3)
    check("three emissions take the widest", abs(three["widestByClass"]["VALUE"] - 0.1 / 0.6) < 1e-12)

    for failure in failures:
        print(f"FAIL  {failure}")
    print(f"{len(failures)} failure(s) of 24 check(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
