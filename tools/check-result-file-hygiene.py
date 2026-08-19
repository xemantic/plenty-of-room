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
"""Three censuses over `gpd/results/`, of which exactly ONE is a gate.  `T-208`/`C-0129`.

`tools/check-kotlin-format-strings.py` reads **source** and models `String.format` call sites.
This reads **output** and models nothing, and **the catch sets are strictly different**: a raw
conversion can reach a result file by routes the static check does not model — a `settles`
string assembled in one function and formatted in another, a field written by a Python emitter
in `tools/`, a hand-edited JSON.  `C-0127` repaired **13 fields carrying 23 raw conversions
across 7 result files**, every one of which had been committed and read at least once.

Three predicates, three exit policies:

- ``--conversions`` (the default) is a **GATE**.  It fires on a Java format conversion in any
  string of any committed result file, and exits 1.  The tree reads clean under it, which is
  why it is wired; `C-0083`: *a gate that cannot come clean is not a gate*.
- ``--departures`` is an **AUDIT**.  `C-0093`'s rule is that a departure — a dimensionless
  difference or ratio of two nearly equal numbers — is emitted at **two** significant digits,
  because `RESULT_ABSOLUTE_FLOOR` is a claim in the locked units and does not reach a ratio.
  **222 fields in 29 files broke it before `T-208`; 199 in 27 still do**, so it cannot
  be a gate without a tree-wide re-emission.  It exits 0 and reports.
- ``--saturated`` is an **AUDIT**.  A symmetric binomial standard error on a **saturated**
  proportion is identically zero for every sample count and therefore measures nothing.
  **302 records in 7 files carried one before `T-208`; 277 in 6 still do.**  A record
  is repaired by emitting the one-sided bound BESIDE the symmetric error, not by
  removing it.  It exits 0 and reports.

Distinguishing a **conversion** from a **percent sign** is the whole difficulty, and it is done
by refusing the Java **space flag**.  `% d` is a legal Java conversion; it is also how every one
of this repository's 310 prose percentages is written (`"84 % of"`, `"+14.7 % more"`).  The
false-positive cost dominates — `CLAUDE.md`: *a drift checker's FALSE positives cost more than
its true ones, because the tool exists in order to be believed* — and no `String.format` literal
in `src/` uses the space flag.  The conversion letter is further restricted to Java's own set,
which is a strict tightening: a conversion that leaked out of a `String.format` is by
construction a valid one.

Usage:

    tools/check-result-file-hygiene.py                  # the gate, over gpd/results/
    tools/check-result-file-hygiene.py --departures     # the departure-precision census
    tools/check-result-file-hygiene.py --saturated      # the saturated-proportion census
    tools/check-result-file-hygiene.py --census         # all three, exit 0
    tools/check-result-file-hygiene.py --self-test

Exit status is 1 if the **gate** finds a defect, 0 otherwise.  The two audits never exit 1.
"""

import json
import os
import re
import sys
from decimal import Decimal

# Java's own conversion set (java.util.Formatter): general, character, integral, floating point,
# date/time and line separator.  Deliberately WITHOUT the space flag -- see the module docstring.
CONVERSION = re.compile(r"%[-#+0,(]*[0-9]*(?:\.[0-9]+)?[bBhHsScCdoxXeEfgGaAtTn]")

# The files whose RECORD is the defect.  Same design decision as `check-markdown-tables.py`
# excluding `third-party/`: an invariant that forbids fixing something must be taught to the
# checker, or the checker decays into a warning.
ALLOWLIST = {
    # `C-0127`'s record of the thirteen strings it repaired, quoted verbatim as evidence.
    # This is the ONLY entry, deliberately: an allowlist is a hole in the gate, so `T-208`'s own
    # result file describes the catch set in words instead of quoting a conversion, and is
    # therefore checked like every other file.  Checked: it carries none.
    "T-207-format-string-repair.json",
}

RESULTS = "gpd/results"

# `C-0093`'s rule, and the two record types `C-0101` found it had not carried to.
DEPARTURE_DIGITS = 2
STRICT_DEPARTURE_PARENTS = ("reproductions", "convergence")

# The four spellings the corpus uses for the quantity, mirroring
# `structure/ResultRounding.kt`'s `DEPARTURE_DIGITS_BY_KEY`.  A gate test in
# `ResultRoundingTest` asserts the Kotlin side; this list is what the emitter classifies on.
DEPARTURE_KEYS = (
    "departure", "relativeDeparture", "departureFromFinest", "relativeDepartureInStroke"
)

# the three test tables, written before the implementation
CONVERSION_TESTS = [
    ("%.4f", 1, "a bare float conversion"),
    ("%d", 1, "a bare integer conversion"),
    ("%s", 1, "a bare string conversion"),
    ("%,d", 1, "a grouped integer conversion"),
    ("%08.2f", 1, "flags, width and precision"),
    ("%.0f", 1, "a zero-precision float conversion"),
    ("C-0017's %.7f pN/nm as a SUM", 1, "the live T-207 shape, inside prose"),
    ("a %d and a %.2f", 2, "two conversions in one string"),
    ("9.1 % of the load", 0, "a percent sign followed by a space -- prose"),
    ("48 %", 0, "a trailing percent sign -- prose"),
    ("the mean is 84 %, and the worst 48 %", 0, "two prose percent signs"),
    ("+14.7 % more total force", 0, "a signed prose percentage"),
    ("100%% sure", 0, "%% is an escape, not a conversion"),
    ("100%%d of the paths", 0,
     "an escaped percent ABUTTING a conversion letter -- the escape must be stripped first"),
    ("%%%.2f", 1, "an escape followed by a real conversion: one defect, not two"),
    ("50%", 0, "a percent sign at end of string"),
    ("a 10 %/decade slope", 0, "a percent sign before punctuation"),
    ("%i and %l and %w and %r", 0, "letters that are not Java conversions"),
    ("% d", 0, "the SPACE flag is deliberately not modelled -- see the module docstring"),
    ("%x", 1, "a hexadecimal conversion"),
    ("%B", 1, "an upper-case boolean conversion"),
    ("%n", 1, "a line-separator conversion"),
    ("", 0, "an empty string"),
    ("no percent sign at all", 0, "clean prose"),
]

DEPARTURE_TESTS = [
    ("1.9e-09", 2, "two significant digits, the rule"),
    ("5.36821841e-06", 9, "nine significant digits, the defect"),
    ("0.000987654321", 9, "nine digits without an exponent"),
    ("0.0", 0, "an exact zero is exactly representable at every precision"),
    ("2.2", 2, "an order-one departure at two digits"),
    ("2.20588235", 9, "an order-one departure at nine digits"),
    ("1.0", 1, "a single significant digit"),
    ("0.00039083", 5, "five digits"),
    ("100.0", 1, "trailing zeros before the point are not significant here"),
    ("1.000001e-06", 7, "seven digits, an interior zero run"),
    ("9.17e-06", 3, "three digits"),
]

SATURATION_TESTS = [
    ({"exceedance": 1.0, "exceedanceStandardError": 0.0}, 1, "p-hat = 1, symmetric error"),
    ({"exceedance": 0.0, "exceedanceStandardError": 0.0}, 1, "p-hat = 0, symmetric error"),
    ({"exceedance": 0.5, "exceedanceStandardError": 0.005}, 0, "an unsaturated proportion"),
    ({"exceedanceAtDesignState": 1.0, "exceedanceStandardError": 0.0}, 1,
     "the T-148 shape: the sibling carries a suffix"),
    ({"exceedanceStandardError": 0.0}, 0, "a standard error with no sibling proportion"),
    ({"cells": [{"exceedance": 1.0, "exceedanceStandardError": 0.0}]}, 1, "nested in an array"),
    ({"a": {"exceedance": 1.0, "exceedanceStandardError": 0.0},
      "b": {"exceedance": 1.0, "exceedanceStandardError": 0.0}}, 2, "two nested records"),
    ({"exceedance": 1.0, "exceedanceOneSidedLowerBound": 0.9997}, 0,
     "a one-sided bound is the right instrument and is not a defect"),
    ({"exceedance": 1.0, "exceedanceStandardError": 0.0,
      "exceedanceOneSidedBound": 0.9997}, 0,
     "REPAIRED: the symmetric error may STAY, so long as the instrument is beside it"),
    ({"exceedance": 1.0, "exceedanceStandardError": 0.0, "otherOneSidedBound": 0.9997}, 1,
     "a one-sided bound on a DIFFERENT quantity does not repair this record"),
]


# ---------------------------------------------------------------------------------------------
# the three predicates
# ---------------------------------------------------------------------------------------------

def conversions_in(text):
    """Every Java format conversion in `text`, with `%%` escapes removed first."""
    return CONVERSION.findall(text.replace("%%", ""))


def significant_digits(literal):
    """The significant digits of a JSON number **literal**, counted on the text as written.

    Counted on the literal rather than on a round-tripped float: `repr` returns the *shortest*
    string that round-trips, which can be fewer digits than the file actually carries, and the
    question here is what the file carries.  `Decimal` preserves the written digits exactly.
    """
    digits = Decimal(literal).as_tuple().digits
    trimmed = list(digits)
    while len(trimmed) > 1 and trimmed[-1] == 0:
        trimmed.pop()
    while len(trimmed) > 1 and trimmed[0] == 0:
        trimmed.pop(0)
    if trimmed == [0]:
        return 0
    return len(trimmed)


def saturated_records(document, path=""):
    """Every `*StandardError` field whose sibling proportion is exactly 0.0 or 1.0."""
    found = []
    if isinstance(document, dict):
        for key, value in document.items():
            if key.endswith("StandardError"):
                base = key[: -len("StandardError")]
                siblings = [
                    other for other in document
                    if other != key and other.lower().startswith(base.lower())
                    and isinstance(document[other], (int, float))
                    and not isinstance(document[other], bool)
                ]
                # A record that carries the RIGHT instrument beside the degenerate one is
                # repaired, not defective: the symmetric error may stay -- it is uninformative
                # rather than wrong, and removing it would break every reader of the schema.
                repaired = any(
                    other.lower().startswith(base.lower()) and "onesided" in other.lower()
                    for other in document
                )
                if siblings and not repaired:
                    proportion = document[siblings[0]]
                    if proportion in (0.0, 1.0):
                        found.append((f"{path}/{key}", siblings[0], proportion, document[key]))
        for key, value in document.items():
            found += saturated_records(value, f"{path}/{key}")
    elif isinstance(document, list):
        for index, value in enumerate(document):
            found += saturated_records(value, f"{path}/{index}")
    return found


# ---------------------------------------------------------------------------------------------
# walking the committed files
# ---------------------------------------------------------------------------------------------

def _strings(document, path=""):
    if isinstance(document, dict):
        for key, value in document.items():
            yield from _strings(value, f"{path}/{key}")
    elif isinstance(document, list):
        for index, value in enumerate(document):
            yield from _strings(value, f"{path}/{index}")
    elif isinstance(document, str):
        yield path, document


def _numbers(document, path=""):
    """Yields `(path, parents, key, Decimal)` for every number, keeping the literal digits."""
    if isinstance(document, dict):
        for key, value in document.items():
            yield from _numbers(value, f"{path}/{key}")
    elif isinstance(document, list):
        for index, value in enumerate(document):
            yield from _numbers(value, f"{path}/*")
    elif isinstance(document, Decimal):
        yield path, document


def _load(path, keep_literals=False):
    with open(path, encoding="utf-8") as handle:
        if keep_literals:
            return json.load(handle, parse_float=Decimal)
        return json.load(handle)


def result_files(root=RESULTS):
    return sorted(
        os.path.join(root, name) for name in os.listdir(root) if name.endswith(".json")
    )


def check_conversions(root=RESULTS, allowlist=ALLOWLIST):
    defects = []
    for path in result_files(root):
        if os.path.basename(path) in allowlist:
            continue
        for pointer, text in _strings(_load(path)):
            for conversion in conversions_in(text):
                defects.append(f"{path}{pointer}: raw format conversion {conversion!r}")
    return defects


def check_departures(root=RESULTS):
    """Returns `(strict, wide)` lists of over-precise departure fields."""
    strict, wide = [], []
    for path in result_files(root):
        for pointer, value in _numbers(_load(path, keep_literals=True)):
            key = pointer.rsplit("/", 1)[-1]
            if "departure" not in key.lower():
                continue
            digits = significant_digits(str(value))
            if digits <= DEPARTURE_DIGITS:
                continue
            entry = (path, pointer, str(value), digits)
            wide.append(entry)
            parent = pointer.split("/")
            if key == "departure" and len(parent) >= 3 and parent[-3] in STRICT_DEPARTURE_PARENTS:
                strict.append(entry)
    return strict, wide


def check_saturated(root=RESULTS):
    found = []
    for path in result_files(root):
        for pointer, sibling, proportion, error in saturated_records(_load(path)):
            found.append((path, pointer, sibling, proportion, error))
    return found


# ---------------------------------------------------------------------------------------------
# self-tests
# ---------------------------------------------------------------------------------------------

def self_test():
    failures = 0
    for text, expected, description in CONVERSION_TESTS:
        found = len(conversions_in(text))
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — conversions, {description}: "
                  f"expected {expected}, found {found} in {text!r}")
    for literal, expected, description in DEPARTURE_TESTS:
        found = significant_digits(literal)
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — digits, {description}: "
                  f"expected {expected}, found {found} for {literal!r}")
    for document, expected, description in SATURATION_TESTS:
        found = len(saturated_records(document))
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — saturation, {description}: "
                  f"expected {expected}, found {found}")
    # The allowlist is itself a claim and is tested in both directions.
    fixture = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "build-selftest-T208")
    os.makedirs(fixture, exist_ok=True)
    with open(os.path.join(fixture, "T-207-format-string-repair.json"), "w") as handle:
        handle.write('{"note": "carries a %.4f as its record"}')
    with open(os.path.join(fixture, "T-999-ordinary.json"), "w") as handle:
        handle.write('{"note": "carries a %.4f by mistake"}')
    allowlisted = check_conversions(fixture)
    if len(allowlisted) != 1:
        failures += 1
        print(f"SELF-TEST FAILED — allowlist: expected 1 defect, found {len(allowlisted)}")
    unallowlisted = check_conversions(fixture, allowlist=set())
    if len(unallowlisted) != 2:
        failures += 1
        print("SELF-TEST FAILED — allowlist off: "
              f"expected 2 defects, found {len(unallowlisted)}")
    for name in os.listdir(fixture):
        os.remove(os.path.join(fixture, name))
    os.rmdir(fixture)
    total = len(CONVERSION_TESTS) + len(DEPARTURE_TESTS) + len(SATURATION_TESTS) + 2
    print(f"{total - failures} of {total} self-tests pass")
    return failures


# ---------------------------------------------------------------------------------------------
# reporting
# ---------------------------------------------------------------------------------------------

def report_departures():
    strict, wide = check_departures()
    print("-- departure precision (AUDIT, never a gate; C-0093's two-significant-digit rule) --")
    print(f"  strict (reproductions[*].departure, convergence[*].departure): "
          f"{len(strict)} field(s) in {len({row[0] for row in strict})} file(s)")
    print(f"  wide   (any leaf key containing 'departure'):                  "
          f"{len(wide)} field(s) in {len({row[0] for row in wide})} file(s)")
    counts = {}
    for path, _, _, _ in strict:
        counts[os.path.basename(path)] = counts.get(os.path.basename(path), 0) + 1
    for name in sorted(counts, key=lambda n: (-counts[n], n)):
        print(f"    {counts[name]:4d}  {name}")
    return strict, wide


def report_saturated():
    found = check_saturated()
    print("-- saturated proportions carrying a symmetric standard error (AUDIT) --")
    counts = {}
    for path, _, _, _, _ in found:
        counts[os.path.basename(path)] = counts.get(os.path.basename(path), 0) + 1
    print(f"  {len(found)} record(s) in {len(counts)} file(s)")
    for name in sorted(counts, key=lambda n: (-counts[n], n)):
        print(f"    {counts[name]:4d}  {name}")
    return found


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    census = "--census" in argv
    if census or "--departures" in argv:
        report_departures()
    if census or "--saturated" in argv:
        report_saturated()
    if census or ("--departures" not in argv and "--saturated" not in argv):
        defects = check_conversions()
        for defect in defects:
            print(defect)
        print(f"-- raw format conversions (GATE): {len(defects)} defect(s) over "
              f"{len(result_files())} result file(s), "
              f"{len(ALLOWLIST)} allowlisted --")
        if not census:
            return 1 if defects else 0
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
