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
# Self-test for tools/result-transfers.py (task T-158).
#
#     tools/test-result-transfers.py
#
# The detector's job is to notice that two claims are quoting ONE number, which is what
# `C-0091` found by hand and what makes a count of "six independent routes" wrong.  Both of its
# failure modes are expensive: a miss lets a corroboration count stand that is not corroborated,
# and a false positive accuses two genuinely independent studies of copying each other, which
# costs an agent a day of disproving it.  Fixtures are in-memory; nothing here reads the checkout.
import sys
import os
import importlib.util

_spec = importlib.util.spec_from_file_location(
    "result_transfers",
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "result-transfers.py"),
)
transfers = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(transfers)

_failures = []


def check(name, actual, expected):
    if actual != expected:
        _failures.append(name)
        print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
    else:
        print("ok   {}".format(name))


# --- flattening to numeric series ---------------------------------------------------------------
_doc = {
    "rows": [
        {"bias": 0.128, "margin": 1.19},
        {"bias": 0.134, "margin": 1.23},
        {"bias": 0.141, "margin": 1.31},
    ],
    "scalar": 42.0,
    "label": "not a number",
}
_series = dict(transfers.series(_doc, minimum_length=3))
check("a repeated leaf key becomes one series", sorted(_series), ["/rows[]/bias", "/rows[]/margin"])
check("in document order", _series["/rows[]/bias"], [0.128, 0.134, 0.141])
check("a lone scalar is not a series", "/scalar" in _series, False)
check("a string leaf is skipped", any("label" in k for k in _series), False)

_short = {"rows": [{"a": 1.0}, {"a": 2.0}]}
check(
    "a series below the minimum length is dropped — two points are a coincidence",
    dict(transfers.series(_short, minimum_length=3)),
    {},
)

# --- distinctiveness ------------------------------------------------------------------------------
# Physical constants and conventions recur legitimately all over this corpus: 2.69, 0.34, 300,
# 4.141947, 100, 45, 34.  A detector that reports them is a detector nobody reads.
check("a constant series is not distinctive", transfers.is_distinctive([2.69, 2.69, 2.69]), False)
check("a round-number series is not distinctive", transfers.is_distinctive([1.0, 2.0, 3.0]), False)
check(
    "a solved series is distinctive",
    transfers.is_distinctive([0.128374, 0.134991, 0.141552]),
    True,
)

# --- matching ---------------------------------------------------------------------------------
_a = {"clauses": [{"v": 0.128374}, {"v": 0.134991}, {"v": 0.141552}]}
_b = {"window": [{"biasForBlocking": 0.128374}, {"biasForBlocking": 0.134991},
                 {"biasForBlocking": 0.141552}]}
_found = transfers.transfers({"T-3": _a, "T-2": _b}, minimum_length=3)
check("one transfer is reported", len(_found), 1)
check("both sides are named", sorted([_found[0].left_file, _found[0].right_file]), ["T-2", "T-3"])
# The pair is reported left-to-right in sorted file order, so assert the PAIR rather than a
# side: which file is "left" is an artefact of the iteration and carries no meaning.
check(
    "with both key paths",
    sorted([_found[0].left_key, _found[0].right_key]),
    ["/clauses[]/v", "/window[]/biasForBlocking"],
)
check("and the worst departure", _found[0].departure, 0.0)

# The live instance had ONE file printing eight significant digits where the other printed nine,
# so an equality test would have called it "not a transfer".  That is the whole point.
_c = {"window": [{"biasForBlocking": 0.12837400}, {"biasForBlocking": 0.13499100},
                 {"biasForBlocking": 0.14155201}]}
_rounded = transfers.transfers({"T-3": _a, "T-2": _c}, minimum_length=3)
check("a last-digit difference is still a transfer", len(_rounded), 1)
check("and the departure is reported, not hidden", _rounded[0].departure > 0.0, True)

_far = {"window": [{"x": 0.9128374}, {"x": 0.9134991}, {"x": 0.9141552}]}
check(
    "genuinely different numbers are not a transfer",
    transfers.transfers({"T-3": _a, "T-2": _far}, minimum_length=3),
    [],
)

_conventional = {"g": [{"d": 2.69}, {"d": 2.69}, {"d": 2.69}]}
check(
    "two files sharing a convention are not accused",
    transfers.transfers({"T-1": _conventional, "T-2": _conventional}, minimum_length=3),
    [],
)

check(
    "a file is not compared with itself",
    transfers.transfers({"T-3": _a}, minimum_length=3),
    [],
)

# A series that appears three times across three files is three pairs, and reporting all three
# is right: the reader needs to know the cluster, not one edge of it.
_three = transfers.transfers({"A": _a, "B": _b, "C": _b}, minimum_length=3)
check("three files carrying one number give three pairs", len(_three), 3)

# --- subset transfers ------------------------------------------------------------------------
#
# The live instance the equal-length matcher MISSES: `T-2` carries 12 values under
# `biasForHundredPiconewtonBlocking` and `T-3` carries 72 under a key of the same name — `T-2`
# selects the states it needs by (height, buffer).  A synthesis quoting a SUBSET of another
# study's output is still quoting that study, and it is the commoner shape of the two.
_long = {"thresholds": [{"b": v} for v in
                        [0.9111117, 0.128374, 0.7222229, 0.134991, 0.5333331, 0.141552]]}
_sub = {"clauses": [{"v": 0.128374}, {"v": 0.134991}, {"v": 0.141552}]}
_s = transfers.subset_transfers({"T-3": _long, "T-2": _sub}, minimum_length=3)
check("a subset is found", len(_s), 1)
check("reported shorter-in-longer", (_s[0].length, _s[0].container_length), (3, 6))
check("with the departure", _s[0].departure, 0.0)
check(
    "a subset that is not contained is not reported",
    transfers.subset_transfers(
        {"T-3": _long, "T-2": {"c": [{"v": 0.1}, {"v": 0.2}, {"v": 0.3}]}}, minimum_length=3
    ),
    [],
)
check(
    "an equal-length match is left to the exact matcher, not double-reported",
    transfers.subset_transfers({"T-3": _sub, "T-2": _sub}, minimum_length=3),
    [],
)
# The container must be genuinely larger for the containment to carry information: a series
# contained in one barely longer is close to an exact match and is reported by the exact matcher.
check(
    "order need not be preserved — a synthesis selects, it does not slice",
    len(transfers.subset_transfers(
        {"T-3": _long, "T-2": {"c": [{"v": 0.141552}, {"v": 0.128374}, {"v": 0.134991}]}},
        minimum_length=3)),
    1,
)

# --- summary ------------------------------------------------------------------------------------
if _failures:
    print("\n{} check(s) FAILED".format(len(_failures)))
    sys.exit(1)
print("\nall checks passed")
