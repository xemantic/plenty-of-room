#!/usr/bin/env python3
"""Classify the difference between two emissions of `T-323`'s result file.

`F23` — *two independent runs of the study do not produce a byte-identical result file* — cannot be
asserted by a run about itself, so the measurement is external and is only reproducible if the runs
survive. This is the reader `C-0216` §14 and `C-0217` §3 both take their tables from; point it at
any two of the four retained emissions.

    gpd/data/T-323-reproducibility/diff.py <before.json> <after.json>

It prints the leaf count, the moved-leaf count, the count of moved BOOLEANS (which is what makes an
irreproducibility cosmetic or not — `CLAUDE.md`: *"0 unclassified and 0 verdicts"* is what no scalar
can say), and every moved leaf with both readings.
"""
import json
import sys


def walk(node, path=""):
    if isinstance(node, dict):
        for key, value in node.items():
            yield from walk(value, path + "/" + key)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from walk(value, path + "/" + str(index))
    else:
        yield path, node


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        return 2
    before, after = (dict(walk(json.load(open(p)))) for p in sys.argv[1:3])
    added = sorted(set(after) - set(before))
    removed = sorted(set(before) - set(after))
    shared = [k for k in before if k in after]
    moved = [k for k in shared if before[k] != after[k]]
    booleans = [k for k in moved if isinstance(before[k], bool)]
    print("%d leaves before, %d after" % (len(before), len(after)))
    print("%d moved, %d added, %d removed" % (len(moved), len(added), len(removed)))
    print("%d of %d booleans moved"
          % (len(booleans), sum(1 for k in shared if isinstance(before[k], bool))))
    for key in added:
        print("  + " + key)
    for key in removed:
        print("  - " + key)
    for key in moved:
        one, two = before[key], after[key]
        if isinstance(one, str):
            print("  ~ %s\n      before: %s\n      after:  %s" % (key, one[:220], two[:220]))
        else:
            print("  ~ %-64s %s  ->  %s" % (key, one, two))
    return 1 if (moved or added or removed) else 0


if __name__ == "__main__":
    sys.exit(main())
