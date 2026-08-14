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
# Self-test for tools/trace-answers.py (task T-131).
#
#     tools/test-trace-answers.py
#
# The tracer decides which numbers in the repository's primary deliverable are unsupported,
# so a false ABSENT would send an agent to "correct" a number that is perfectly good and a
# false CITED would let a drifted one through.  Both failure modes are silent, which is
# exactly the case for executable tests.  Fixtures are in-memory; nothing here reads the
# checkout.
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import importlib.util

_spec = importlib.util.spec_from_file_location(
    "trace_answers", os.path.join(os.path.dirname(os.path.abspath(__file__)), "trace-answers.py")
)
trace_answers = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(trace_answers)

_failures = []


def check(name, actual, expected):
    if actual != expected:
        _failures.append("{}: expected {!r}, got {!r}".format(name, expected, actual))
        print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
    else:
        print("ok   {}".format(name))


# --- normalisation -------------------------------------------------------------------------
check(
    "en dash folds to hyphen",
    trace_answers.normalise("1.19–1.42×"),
    "1.19-1.42x",
)
check(
    "U+2212 minus folds to hyphen",
    trace_answers.normalise("−8.40"),
    "-8.40",
)

# --- identifier stripping ------------------------------------------------------------------
check(
    "claim IDs are not quantities",
    trace_answers.tokens("`C-0051` and `CH-0077` and `T-131` and `A2.1` and §4(g)"),
    [],
)
check(
    "a leaf ID next to a real number leaves only the number",
    trace_answers.tokens("`A2.1` gives 33.333 pN/nm"),
    ["33.333"],
)

# --- tokenising ----------------------------------------------------------------------------
check(
    "a range yields both ends",
    trace_answers.tokens("the margin is 1.19–1.42×"),
    ["1.19", "1.42"],
)
check(
    "single digits are dropped by default",
    trace_answers.tokens("3 nm of stroke at 45 attachments"),
    ["45"],
)
check(
    "single digits are kept at min_digits=1",
    trace_answers.tokens("3 nm of stroke", min_digits=1),
    ["3"],
)
check(
    "duplicates collapse in order",
    trace_answers.tokens("0.0753 then 0.0544 then 0.0753"),
    ["0.0753", "0.0544"],
)
check(
    "exponent notation survives",
    trace_answers.tokens("a residual of 2.4e-15"),
    ["2.4e-15"],
)

# --- citation extraction -------------------------------------------------------------------
check(
    "citations are collected in order without duplicates",
    trace_answers.citations("(`C-0021`, `C-0023`, `CH-0027`, `C-0021`)"),
    ["C-0021", "C-0023", "CH-0027"],
)

# --- substring guard -----------------------------------------------------------------------
check(
    "45 does not match inside 1.45",
    trace_answers.contains("the value is 1.45 pN", "45"),
    False,
)
check(
    "45 does not match inside 450",
    trace_answers.contains("the value is 450 pN", "45"),
    False,
)
check(
    "45 matches standing alone",
    trace_answers.contains("45 attachments", "45"),
    True,
)
check(
    "0.0706 matches inside a sentence",
    trace_answers.contains("dishes **0.0706** of the stroke", "0.0706"),
    True,
)

# --- blocking ------------------------------------------------------------------------------
_answers = "\n".join(
    [
        "# heading",
        "",
        "a paragraph citing `C-0001` with 12.5 in it",
        "and its second line with 99.9 in it",
        "",
        "| a | table row citing `C-0002` with 7.75 | ",
    ]
)
_blocks = trace_answers.blocks(_answers)
check("blocks: heading, paragraph, table row", len(_blocks), 3)
check("table row is its own block", _blocks[2][1].startswith("|"), True)
check(
    "a paragraph's lines are one block",
    trace_answers.tokens(_blocks[1][1]),
    ["12.5", "99.9"],
)

# --- end-to-end tracing --------------------------------------------------------------------
_sources = {
    "C-0001": "the layer stiffness is 12.5 pN/nm at the working point",
    "C-0002": "an unrelated claim quoting 99.9 and 7.75",
}
_records = trace_answers.trace(_answers, _sources)
_by_token = {token: (status, owners) for _, token, status, _, owners in _records}
check("a number in the cited claim is CITED", _by_token["12.5"][0], "CITED")
check("a number only in another claim is ELSEWHERE", _by_token["99.9"][0], "ELSEWHERE")
check("its owner is reported", _by_token["99.9"][1], ["C-0002"])
check(
    "a table row's own citation is honoured",
    _by_token["7.75"][0],
    "CITED",
)

_absent = trace_answers.trace("a paragraph citing `C-0001` with 42.42 in it", _sources)
check("a number in no claim at all is ABSENT", _absent[0][2], "ABSENT")

# --- summary -------------------------------------------------------------------------------
if _failures:
    print("\n{} check(s) FAILED".format(len(_failures)))
    sys.exit(1)
print("\nall checks passed")
