#!/usr/bin/env python3
"""T-326 cheap bound, prototype: the margin the refusal must be priced against.

`CH-0284` prices the fit/sample gap against `C-0180`'s tightest RECOVERED cell,
which clears `T-5b` by 0.426 % of the tolerance. That is not the corpus's
tightest VERDICT-BEARING reading, and this pass says what is.

Predicate, stated so the count is reproducible: over the eighteen committed
result files carrying a `HoneycombDeflection` dishing, every numeric leaf whose
key ends `OverStroke` or contains `ishing`, sitting in a JSON object that also
carries at least one boolean --- so a verdict is written on it --- with a value
in [0.09, 0.11].

Run:  python3 gpd/data/T-326-cheap-bound/margin-census.py
"""
import glob
import json
import os

TOLERANCE = 0.10

FILES = ["T-253", "T-254", "T-263", "T-267", "T-279", "T-284", "T-291", "T-294",
         "T-297", "T-299", "T-303", "T-304", "T-307", "T-310", "T-315", "T-316",
         "T-322", "T-323"]

CHANNELS = [
    (1.0e-5, "the tightest reading itself"),
    (5.0e-4, "CH-0284's collar channel"),
    (4.2724e-3, "the movement that would flip C-0180's tightest recovered cell"),
    (4.57e-3, "C-0180's own beam-subdivision convergence departure, 4.57E-4 of the stroke"),
    (6.7e-3, "CH-0284's bond-prestrain channel"),
    (4.02e-2, "the prestrain channel at convention C's 6x"),
]


def _leaves(node, path, parent, out):
    if isinstance(node, dict):
        for key, value in node.items():
            _leaves(value, path + "/" + key, node, out)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            _leaves(value, path + "/%d" % index, parent, out)
    elif isinstance(node, float):
        out.append((path, node, parent))


def census(root="."):
    rows = []
    for tag in FILES:
        matches = glob.glob(os.path.join(root, "gpd/results/%s-*.json" % tag))
        if not matches:
            raise SystemExit("no committed result file for %s" % tag)
        out = []
        _leaves(json.load(open(matches[0])), "", None, out)
        for path, value, parent in out:
            key = path.rsplit("/", 1)[-1]
            if not (0.09 <= value <= 0.11):
                continue
            if not isinstance(parent, dict):
                continue
            if not any(isinstance(x, bool) for x in parent.values()):
                continue
            if not (key.endswith("OverStroke") or "ishing" in key):
                continue
            rows.append((abs(value - TOLERANCE) / TOLERANCE, value, tag, path))
    rows.sort()
    return rows


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    rows = census(os.path.join(here, "..", "..", ".."))
    print("verdict-bearing readings in [0.09, 0.11]: %d" % len(rows))
    print("tightest five:")
    for rel, value, tag, path in rows[:5]:
        print("  rel=%.4e  %.9f  %s%s" % (rel, value, tag, path))
    print("counts by channel:")
    for threshold, label in CHANNELS:
        print("  within %-10.5g  %-70s %5d"
              % (threshold, label, sum(1 for r, *_ in rows if r <= threshold)))


if __name__ == "__main__":
    main()
