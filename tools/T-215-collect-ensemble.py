#!/usr/bin/env python3
"""T-215 — collect an ensemble of runs of one study into a reduced, retained input file.

    tools/T-215-collect-ensemble.py <out.json> <label>=<path-or-git-rev> ...

A member given as `<label>=<rev>:<path>` is read out of `git`, so the three committed versions
of `gpd/results/T-129-range-robust-placement.json` need not be duplicated in the tree; a member
given as `<label>=<file>` is a fresh run.

Why a reduction rather than the raw files: the ensemble is nine 60 kB result files of which
1016 of 1042 fields are identical in every member.  What the study needs is the members'
*varying* fields plus the counts that make "1016 identical" checkable, and that is 3 kB.

Two disciplines are built in, both `CLAUDE.md`'s:

  * a moved STRING is not necessarily a moved decision — strings are compared twice, once as
    written and once with every digit stripped, so a verdict whose only movement is a rendered
    number is not reported as a verdict change;
  * booleans and lists are counted separately from numbers, because a flatness verdict is a
    boolean and a width says nothing about it.
"""
import json
import re
import subprocess
import sys

# Java's own conversion characters, as `tools/check-result-file-hygiene.py --conversions` reads
# them: a string that still carries one is a `+`-binds-tighter-than-`.format()` defect, not a
# verdict. Iteration 28 repaired exactly one such field of this file, so a varying string has a
# third possibility besides "a decision moved" and "a rendered number moved".
# NOTE the flag class has NO SPACE in it: a space IS a legal Java flag, so "% of" would
# match and every prose percentage in the corpus would read as a defect
# (`CLAUDE.md`, `C-0127`). This is the checker's own pattern, verbatim.
RAW_CONVERSION = re.compile(r"%[-#+0,(]*[0-9]*(?:\.[0-9]+)?[bBhHsScCdoxXeEfgGaAtTn]")


def walk(node, path=""):
    if isinstance(node, dict):
        for key, value in node.items():
            yield from walk(value, f"{path}/{key}")
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from walk(value, f"{path}[{index}]")
    else:
        yield path, node


def load(spec):
    if ":" in spec:
        rev, path = spec.split(":", 1)
        text = subprocess.run(
            ["git", "show", f"{rev}:{path}"], check=True, capture_output=True, text=True
        ).stdout
        return json.loads(text), f"git {rev}:{path}"
    with open(spec) as handle:
        return json.load(handle), spec


def strip_digits(text):
    return "".join(character for character in text if not character.isdigit())


def main():
    out = sys.argv[1]
    members = []
    for argument in sys.argv[2:]:
        label, spec = argument.split("=", 1)
        document, provenance = load(spec)
        members.append((label, provenance, dict(walk(document))))

    common = set(members[0][2])
    for _, _, fields in members[1:]:
        common &= set(fields)
    common = sorted(common)

    def kind(values):
        if all(isinstance(v, bool) for v in values):
            return "boolean"
        if all(isinstance(v, (int, float)) for v in values):
            return "number"
        if all(isinstance(v, str) for v in values):
            return "string"
        return "mixed"

    counts = {"number": 0, "boolean": 0, "string": 0, "mixed": 0}
    varying = {"number": 0, "boolean": 0, "string": 0, "mixed": 0}
    varying_paths = []
    rendered_only = 0
    for path in common:
        values = [fields[path] for _, _, fields in members]
        this = kind(values)
        counts[this] += 1
        if len(set(map(repr, values))) == 1:
            continue
        varying[this] += 1
        record = {"path": path, "kind": this, "values": values}
        if this == "string":
            stripped = {strip_digits(v) for v in values}
            record["digitsOnly"] = len(stripped) == 1
            record["carriesRawConversion"] = [
                bool(RAW_CONVERSION.search(v)) for v in values
            ]
            if len(stripped) == 1:
                rendered_only += 1
        varying_paths.append(record)

    json.dump(
        {
            "task": "T-215",
            "source": "gpd/results/T-129-range-robust-placement.json",
            "members": [
                {"label": label, "provenance": provenance} for label, provenance, _ in members
            ],
            "fieldsCompared": len(common),
            "fieldsComparedByKind": counts,
            "fieldsVarying": len(varying_paths),
            "fieldsVaryingByKind": varying,
            "stringFieldsVaryingInDigitsOnly": rendered_only,
            "stringFieldsVaryingWithARawConversion": sum(
                1 for record in varying_paths
                if record["kind"] == "string" and any(record["carriesRawConversion"])
            ),
            "varying": varying_paths,
        },
        open(out, "w"),
        indent=1,
    )
    print(f"{len(members)} member(s); {len(common)} common field(s); "
          f"{len(varying_paths)} varying ({varying}); wrote {out}")


if __name__ == "__main__":
    main()
