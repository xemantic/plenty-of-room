#!/usr/bin/env python3
"""T-294 — the two independent emissions, compared BY KIND.

`CH-0281` asks that an externally measured falsifier name the artifact its verdict is taken on.
This is that artifact's reader: it walks the two retained emissions leaf by leaf and classifies
every difference, because `CLAUDE.md` records that *"a count BY KIND beside the scalar"* is what
makes an irreproducibility cosmetic and that no scalar can say it.

Usage:
    python3 gpd/data/T-294-reproducibility/diff.py [run-a.json] [run-b.json]
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))

VERDICT_KEYS = (
    "flatAtNominal", "flatAtP90Standing", "flatAtP90Corrected", "beatsUncoupledAtP90",
    "uncoupledFlatStanding", "uncoupledFlatCorrected", "verdictMoved", "closes", "fired",
    "verdictSurvives", "verdictAtCoarse", "verdictAtFine", "everyTurnIsBonded",
    "faceModesAreOrthogonal", "strokeMatchesClosedForm", "tenBySixFlat", "fifteenByFourFlat",
    "orderingAgreesWithTheFractionalReading", "insideTenBySixMedianBand", "flatStanding",
    "flatCorrected", "verdictMoves", "standingReproducesThePublished",
)

DIGITS = re.compile(r"-?\d+\.?\d*(?:[eE][-+]?\d+)?")


def leaves(node, path=""):
    if isinstance(node, dict):
        for key, value in node.items():
            yield from leaves(value, path + "/" + key)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from leaves(value, path + "/" + str(index))
    else:
        yield path, node


def kind(path, a, b):
    leaf = path.rsplit("/", 1)[-1]
    if leaf in VERDICT_KEYS:
        return "VERDICT"
    if isinstance(a, bool) or isinstance(b, bool):
        return "VERDICT"
    if isinstance(a, str) and isinstance(b, str):
        if DIGITS.sub("", a) == DIGITS.sub("", b):
            return "PROSE RENDERING OF A NUMBER"
        return "PROSE"
    if isinstance(a, (int, float)) and isinstance(b, (int, float)):
        return "NUMBER"
    return "UNCLASSIFIED"


def main():
    one = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "run-a.json")
    two = sys.argv[2] if len(sys.argv) > 2 else os.path.join(HERE, "run-b.json")
    a = dict(leaves(json.load(open(one))))
    b = dict(leaves(json.load(open(two))))
    if set(a) != set(b):
        print("SHAPE DIFFERS: " + str(len(set(a) ^ set(b))) + " leaf path(s) in one and not both")
        return 1
    moved = {p: (a[p], b[p]) for p in a if a[p] != b[p]}
    counts = {}
    for path, (x, y) in sorted(moved.items()):
        label = kind(path, x, y)
        counts[label] = counts.get(label, 0) + 1
    print("# " + str(len(a)) + " leaves compared, " + str(len(moved)) + " moved")
    for label in sorted(counts):
        print("  " + label + ": " + str(counts[label]))
    for path, (x, y) in sorted(moved.items())[:20]:
        print("  " + kind(path, x, y) + "  " + path + "\n      a: " + str(x)[:110] +
              "\n      b: " + str(y)[:110])
    unclassified = counts.get("UNCLASSIFIED", 0) + counts.get("VERDICT", 0)
    print("# " + str(counts.get("VERDICT", 0)) + " verdict(s) and " +
          str(counts.get("UNCLASSIFIED", 0)) + " unclassified")
    return 1 if unclassified else 0


if __name__ == "__main__":
    sys.exit(main())
