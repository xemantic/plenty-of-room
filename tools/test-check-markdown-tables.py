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
# Self-test for tools/check-markdown-tables.py (task P-23).
#
#     tools/test-check-markdown-tables.py
#
# The checker decides which of this repository's tables do not render, and both of its failure
# modes are silent: a missed defect leaves a claim's evidence table mangled for a reader who has
# no way to know, and a false positive sends an agent to "fix" a table that is already correct.
# Fixtures are in-memory; nothing here reads the checkout.
import sys
import os
import importlib.util

_spec = importlib.util.spec_from_file_location(
    "check_markdown_tables",
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "check-markdown-tables.py"),
)
tables = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(tables)

_failures = []


def check(name, actual, expected):
    if actual != expected:
        _failures.append(name)
        print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
    else:
        print("ok   {}".format(name))


# --- cell splitting ---------------------------------------------------------------------------
check("a plain row splits on its pipes", tables.cells("| a | b | c |"), ["a", "b", "c"])
check("leading and trailing pipes are optional", tables.cells("a | b"), ["a", "b"])
check("cells are stripped", tables.cells("|  a  |  b  |"), ["a", "b"])
check(
    "an ESCAPED pipe does not split — this is the correct way to write one",
    tables.cells(r"| `Gi = \|ΔΠ\|/Π_MF` | 0.30 |"),
    [r"`Gi = \|ΔΠ\|/Π_MF`", "0.30"],
)
check(
    "a BARE pipe inside inline code DOES split, because GFM splits it too",
    tables.cells("| `ℓ = |F_es|/|k_es|` | x |"),
    ["`ℓ =", "F_es", "/", "k_es", "`", "x"],
)

# --- separator recognition --------------------------------------------------------------------
check("a separator row is recognised", tables.is_separator("|---|---|---|"), True)
check("with alignment colons too", tables.is_separator("| :--- | ---: | :---: |"), True)
check("a data row is not a separator", tables.is_separator("| a | b |"), False)
check("an empty line is not a separator", tables.is_separator(""), False)

# --- table discovery --------------------------------------------------------------------------
_good = "\n".join(
    ["intro text", "", "| h1 | h2 |", "|---|---|", "| a | b |", "| c | d |", "", "outro"]
)
check("a well-formed table reports no defect", tables.defects(_good), [])

_two = "\n".join(
    ["| a | b |", "|---|---|", "| 1 | 2 |", "", "| x | y | z |", "|---|---|---|", "| 1 | 2 | 3 |"]
)
check("a blank line starts a new table, so widths do not leak", tables.defects(_two), [])

_prose = "\n".join(["a paragraph", "| h1 | h2 |", "|---|---|", "| a | b |"])
check("a table need not be preceded by a blank line", tables.defects(_prose), [])

# --- the three real defect kinds ----------------------------------------------------------------
_body = "\n".join(["| h1 | h2 | h3 |", "|---|---|---|", "| a | b |"])
check("a MISSING cell is reported", [d.line for d in tables.defects(_body)], [3])
check("with both widths", [(d.width, d.header_width) for d in tables.defects(_body)], [(2, 3)])
check("and the kind names the row", [d.kind for d in tables.defects(_body)], ["ROW"])

_bare = "\n".join(["| h1 | h2 |", "|---|---|", "| `|x|` | b |"])
check("a BARE pipe in a body cell is reported", [d.line for d in tables.defects(_bare)], [3])

# A bare pipe in the HEADER is the nastiest kind: the header widens, so every correct body row
# is reported instead of the one line that is wrong.  The checker must blame the header.
_hdrbare = "\n".join(["| `ℓ = |F_es|` | h2 |", "|---|---|", "| a | b |", "| c | d |"])
_d = tables.defects(_hdrbare)
check("a bare pipe in the HEADER is blamed on the header", [x.kind for x in _d], ["HEADER"])
check("the header defect points at the header line", [x.line for x in _d], [1])
check(
    "and it does not also report every body row",
    len([x for x in _d if x.kind == "ROW"]),
    0,
)
check(
    "the header defect reports the separator's width as the truth",
    # `| `ℓ = |F_es|` | h2 |` splits into four: "`ℓ =", "F_es", "`", "h2" — the two bare pipes
    # each add a cell, so a single `|F_es|` widens a two-column table to four.
    [(x.width, x.header_width) for x in _d],
    [(4, 2)],
)

# --- what must NOT be reported --------------------------------------------------------------
_pipe_in_fence = "\n".join(
    ["```", "| this is code, not a table |", "| and neither is this |", "```"]
)
check("a fenced code block is not a table", tables.defects(_pipe_in_fence), [])

_no_sep = "\n".join(["| just | some | pipes |", "| more | pipes | here |"])
check(
    "a pipe block with no separator row is not a GFM table and is skipped",
    tables.defects(_no_sep),
    [],
)

# --- what is deliberately out of scope ---------------------------------------------------------
#
# `third-party/` holds the problem definition AS RECEIVED, unmodified — that is a standing
# invariant of this repository, not a preference.  Its table has a defect and must keep it, so a
# checker that reports it can never come back clean and therefore can never be used as a gate.
check("third-party is excluded by default", tables.is_excluded("third-party/x.md"), True)
check("a nested third-party path too", tables.is_excluded("a/third-party/x.md"), False)
check("the corpus is not excluded", tables.is_excluded("gpd/claims/C-0001.md"), False)
check("nor the deliverable", tables.is_excluded("ANSWERS.md"), False)

# A verification SNAPSHOT has no `.git`, so `tracked_markdown()` falls back to a tree walk, and
# `os.walk(".")` yields `./third-party/…`.  The leading `./` defeated the exclusion and made the
# gate report the one defect it is forbidden to fix — found by the agent wiring this into
# `tools/verify.sh`, which is exactly where the fallback is the live path.
check("a ./-prefixed walk path is still excluded", tables.is_excluded("./third-party/x.md"), True)
check("and a redundant ./ elsewhere does not confuse it", tables.is_excluded("./ANSWERS.md"), False)
check("nor does a doubled separator", tables.is_excluded("third-party//x.md"), True)
check("an absolute path is matched on its tail, not its root", tables.is_excluded("/etc/x.md"), False)

# --- summary ------------------------------------------------------------------------------------
if _failures:
    print("\n{} check(s) FAILED".format(len(_failures)))
    sys.exit(1)
print("\nall checks passed")
