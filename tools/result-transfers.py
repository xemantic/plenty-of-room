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
# Find numbers that two result files are BOTH quoting, i.e. one study's output re-appearing as
# another's (task T-158, generalising C-0091's hand-found transfer detection).
#
#     tools/result-transfers.py [--results gpd/results] [--min-length N] [--quiet]
#
# `C-0091` audited the six claims said to independently recommend 0.5 mM MgCl2 and found the six
# were THREE: two of them were the other three, read again.  `T-2`'s bias figure IS `T-3`'s own
# number at 15 of 15 states at departure 0.0, and `T-25` carries `T-16`'s and `T-4`'s extrema at
# 20 of 20 at 2.66e-8.
#
# Three things make that worth mechanising:
#
#   1. **A synthesis that reads CLAIMS cannot see it.**  Each of the six claims states its route
#      truthfully; only the result JSONs show two of them are one number.
#   2. **An equality test would have missed it.**  One file prints eight significant digits where
#      the other prints nine, so `==` says "not a transfer".  The comparison must be a tolerance,
#      and the departure must be REPORTED rather than swallowed.
#   3. **It changes a count that is in front of the customer.**  "Six independent routes" and
#      "three" are different statements about how well supported a recommendation is.
#
# This is the value-level companion to `tools/result-reader-census.py`, which finds dependencies
# that flow through CODE.  A transfer found here flowed through a PERSON, and nothing else in the
# repository can see it.
#
# The unit of comparison is a SERIES — the values a repeated leaf key takes across an array —
# never a single number.  A lone coincidence between two solved numbers is meaningless in a corpus
# of this size; a whole array matching elementwise is not.  Series are additionally filtered for
# DISTINCTIVENESS, because physical constants and layout conventions (2.69, 0.34, 300, 4.141947,
# 45, 34) recur legitimately everywhere and a detector that reports them is one nobody reads.
#
# Output is advisory, never a gate: a transfer is not a defect.  It is a defect only when someone
# COUNTS the two files as independent evidence, and no tool can see that.
#
# Verified by tools/test-result-transfers.py.
import argparse
import json
import os
import sys
from collections import namedtuple, OrderedDict

Transfer = namedtuple("Transfer", "left_file left_key right_file right_key length departure")

# Two emitted fields agree if they agree to what the emission precision can express.  `C-0073`
# set six significant digits for the SCF-derived files and nine elsewhere; `EMITTED_FIELD_SLACK`
# on the Kotlin side is 5e-5.  The same figure is used here, and it is deliberately loose: this
# tool reports a candidate for a human to adjudicate, so a miss costs more than a false alarm.
DEFAULT_TOLERANCE = 5e-5

# A series must be at least this long before a match means anything.
DEFAULT_MINIMUM_LENGTH = 5


def series(document, minimum_length=DEFAULT_MINIMUM_LENGTH):
    """[(key path, [values])] for every repeated numeric leaf key, in document order.

    An array index is collapsed to `[]`, so `rows[0].bias` and `rows[1].bias` accumulate into one
    series under `/rows[]/bias`.  That is the shape a study emits: one record per state, the same
    fields in each.
    """
    collected = OrderedDict()

    def walk(node, path):
        if isinstance(node, dict):
            for key, value in node.items():
                walk(value, path + "/" + str(key))
        elif isinstance(node, list):
            for value in node:
                walk(value, path + "[]")
        elif isinstance(node, bool):
            return  # a bool is an int in Python and is not a measurement
        elif isinstance(node, (int, float)):
            collected.setdefault(path, []).append(float(node))

    walk(document, "")
    return [(key, values) for key, values in collected.items() if len(values) >= minimum_length]


def is_distinctive(values):
    """Whether a series carries enough information for a match to mean anything.

    Two filters, both learned from what this corpus actually contains:

      * a **constant** series says only that a convention was used twice;
      * a series of **round** numbers — every value a small multiple of 0.5 — is a grid, an index
        or a count, and grids coincide across studies by design rather than by transfer.
    """
    if len(set(values)) < 2:
        return False
    if all(abs(value * 2.0 - round(value * 2.0)) < 1e-9 for value in values):
        return False
    return True


def _matches(left, right, tolerance):
    """The worst relative departure if two series match elementwise, else None."""
    if len(left) != len(right):
        return None
    worst = 0.0
    for a, b in zip(left, right):
        scale = max(abs(a), abs(b))
        departure = abs(a - b) if scale == 0.0 else abs(a - b) / scale
        if departure > tolerance:
            return None
        worst = max(worst, departure)
    return worst


def transfers(documents, minimum_length=DEFAULT_MINIMUM_LENGTH, tolerance=DEFAULT_TOLERANCE):
    """[Transfer] for every distinctive series two different documents both carry."""
    indexed = {
        name: [(key, values) for key, values in series(document, minimum_length)
               if is_distinctive(values)]
        for name, document in documents.items()
    }
    names = sorted(indexed)
    found = []
    for i, left_name in enumerate(names):
        for right_name in names[i + 1:]:
            for left_key, left_values in indexed[left_name]:
                for right_key, right_values in indexed[right_name]:
                    departure = _matches(left_values, right_values, tolerance)
                    if departure is None:
                        continue
                    found.append(
                        Transfer(left_name, left_key, right_name, right_key,
                                 len(left_values), departure)
                    )
    return found


SubsetTransfer = namedtuple(
    "SubsetTransfer", "left_file left_key right_file right_key length container_length departure"
)


def _contains(container, values, tolerance):
    """Worst departure if every value has a distinct match in `container`, else None.

    Greedy nearest-first over a sorted copy, which is exact here because the tolerance is far
    below the spacing of any solved series in this corpus.  Order is NOT required: a synthesis
    SELECTS the states it needs, it does not slice a contiguous window.
    """
    remaining = sorted(container)
    worst = 0.0
    for value in values:
        best_index, best_departure = None, None
        for index, candidate in enumerate(remaining):
            scale = max(abs(value), abs(candidate))
            departure = abs(value - candidate) if scale == 0.0 else abs(value - candidate) / scale
            if departure <= tolerance and (best_departure is None or departure < best_departure):
                best_index, best_departure = index, departure
        if best_index is None:
            return None
        remaining.pop(best_index)
        worst = max(worst, best_departure)
    return worst


def subset_transfers(documents, minimum_length=DEFAULT_MINIMUM_LENGTH,
                     tolerance=DEFAULT_TOLERANCE):
    """[SubsetTransfer] where one file's whole series sits inside another's longer one.

    This is the commoner shape and the one the equal-length matcher misses: `T-2` quotes twelve of
    `T-3`'s seventy-two solved biases, selected by `(height, buffer)`.  A synthesis quoting a
    SUBSET of another study's output is still quoting that study.

    It is a weaker signal than an exact match — a short series can sit inside a long one by luck —
    so the container must be **strictly** longer, and both sides must be distinctive.
    """
    indexed = {
        name: [(key, values) for key, values in series(document, minimum_length)
               if is_distinctive(values)]
        for name, document in documents.items()
    }
    names = sorted(indexed)
    found = []
    for left_name in names:
        for right_name in names:
            if left_name == right_name:
                continue
            for left_key, left_values in indexed[left_name]:
                for right_key, right_values in indexed[right_name]:
                    if len(right_values) <= len(left_values):
                        continue
                    departure = _contains(right_values, left_values, tolerance)
                    if departure is None:
                        continue
                    found.append(
                        SubsetTransfer(left_name, left_key, right_name, right_key,
                                       len(left_values), len(right_values), departure)
                    )
    return found


def load(directory):
    """{task id: parsed document} for every result JSON."""
    documents = {}
    if not os.path.isdir(directory):
        return documents
    for name in sorted(os.listdir(directory)):
        if not name.endswith(".json"):
            continue
        with open(os.path.join(directory, name), encoding="utf-8") as handle:
            try:
                documents[name[: -len(".json")]] = json.load(handle)
            except json.JSONDecodeError as error:
                print("{}: unreadable ({})".format(name, error), file=sys.stderr)
    return documents


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--results", default="gpd/results")
    parser.add_argument("--min-length", type=int, default=DEFAULT_MINIMUM_LENGTH)
    parser.add_argument("--tolerance", type=float, default=DEFAULT_TOLERANCE)
    parser.add_argument("--quiet", action="store_true", help="summary only")
    parser.add_argument(
        "--subsets", action="store_true",
        help="also report a series wholly contained in another file's longer one (slower)",
    )
    arguments = parser.parse_args(argv)

    documents = load(arguments.results)
    found = transfers(documents, arguments.min_length, arguments.tolerance)
    pairs = {(t.left_file, t.right_file) for t in found}
    if not arguments.quiet:
        for t in sorted(found, key=lambda t: (-t.length, t.left_file, t.right_file)):
            print(
                "{}\t{}\t{}\t{}\t{} values\tdeparture {:.3g}".format(
                    t.left_file, t.left_key, t.right_file, t.right_key, t.length, t.departure
                )
            )
    if arguments.subsets:
        contained = subset_transfers(documents, arguments.min_length, arguments.tolerance)
        if not arguments.quiet:
            for t in sorted(contained, key=lambda t: (-t.length, t.left_file)):
                print(
                    "{}\t{}\tSUBSET OF\t{}\t{}\t{} of {}\tdeparture {:.3g}".format(
                        t.left_file, t.left_key, t.right_file, t.right_key,
                        t.length, t.container_length, t.departure
                    )
                )
        sys.stdout.flush()
        print("# {} contained series".format(len(contained)), file=sys.stderr)

    sys.stdout.flush()
    print(
        "# {} shared series over {} file pair(s), from {} result files"
        " — ADVISORY: a transfer is not a defect, it is a defect only where the two are COUNTED"
        " as independent evidence".format(len(found), len(pairs), len(documents)),
        file=sys.stderr,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
