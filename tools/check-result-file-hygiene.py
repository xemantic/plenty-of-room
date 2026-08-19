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
"""Three censuses over `gpd/results/`, of which TWO are gates.  `T-208`/`C-0129`, `T-212`/`C-0131`.

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
- ``--departures`` is a **GATE** since `T-212`, and since `T-214` the gate IS the rule.
  `C-0093`'s rule is that a departure — a dimensionless difference or ratio of two nearly equal
  numbers — is emitted at **two** significant digits, because `RESULT_ABSOLUTE_FLOOR` is a claim
  in the locked units and does not reach a ratio.  **222 fields in 29 files broke it before
  `T-208`, 199 in 27 after it**, and `T-212` re-emitted those 27 — but its predicate was the leaf
  name `departure`, one spelling of four, so the rule's own scope stood at **351 fields in 31
  files** while the gate read clean (`CH-0154`).  `T-214` re-emitted the 31 and widened the
  predicate, so the gate now fires on **any** of the four spellings inside a `reproductions` or
  `convergence` record.

  The ``scope`` line is retained and now carries the **same** number as the gate: it is the line
  `C-0131` published the residue on, and the reader who watched it fall 601 → 351 → 0 is entitled
  to see it reach zero rather than disappear.  ``strict`` keeps `C-0129`'s original predicate as
  a now-proper subset, and ``wide`` remains an ungated ceiling on the class.

  What holds the predicate open is `C-0083`: *a gate that cannot come clean is not a gate*, and a
  predicate can always be narrowed until the tree is clean.  The mutation test for that is in
  [GATE_TESTS] — narrowing the gate back to the leaf name must FAIL a named self-test.
- ``--saturated`` is an **AUDIT**.  A symmetric binomial standard error on a **saturated**
  proportion is identically zero for every sample count and therefore measures nothing.
  **302 records in 7 files carried one before `T-208`, 277 in 6 after it, and `T-213` repaired
  the remaining six at their shared source** — `coupling.summariseDropoutDishing`, which all six
  studies build their summary through.  A record
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

Exit status is 1 if either **gate** finds a defect, 0 otherwise.  The saturated-proportion audit
never exits 1, and neither does the departure ``wide`` line.
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

# The spellings CLASSIFIED as the quantity, mirroring `structure/ResultRounding.kt`'s
# `DEPARTURE_SPELLINGS`.  A gate test in `ResultRoundingTest` asserts the Kotlin side; this list is
# what the checker gates on.
#
# `T-225`/`CH-0169`: `T-212`'s four were introduced as "every spelling the corpus uses", which is a
# LIST, and a list is a census that stopped.  A shape census of `gpd/results/` finds fourteen more
# leaf keys of the same kind inside those two records; eight are the quantity under another name
# and six are not.  `tools/T-225-census.py --check` is what keeps this complete — it fails if the
# corpus grows a candidate name that appears in NEITHER this tuple nor `EXCLUDED_DEPARTURE_KEYS`.
DEPARTURE_KEYS = (
    # `T-212`'s four
    "departure", "relativeDeparture", "departureFromFinest", "relativeDepartureInStroke",
    # `T-225`'s eight: a RELATIVE comparison of two computations of one quantity
    "relativeError", "relativeSpread", "relativeMovement",
    "multiplierDeparture", "gradientDeparture",
    "firstIntegralRelativeSpread", "firstIntegralCoreSpread", "centrelineRouteSpread",
)

# `T-225` — the names a shape census turns up inside a departure record that are NOT the rule's
# quantity, carried here so that the exclusion is a checkable statement rather than an omission.
# The gate must not fire on any of them, and `GATE_TESTS` says so one name at a time.
EXCLUDED_DEPARTURE_KEYS = {
    "residualExponent": "a log10 of a residual: two digits on -11.0931 is -11, i.e. 1e-11 where "
                        "the solve produced 8.07e-12, and the node-spacing axis collapses",
    "coverageErrorExponent": "the same, on the coverage error",
    "observedOrder": "a logarithm of a RATIO of two residuals, and the answer of the convergence "
                     "axis; CLAUDE.md quotes 2.08-2.32, 1.59 and 1.11 at three digits",
    "worstResidual": "a LENGTH in nm (T-117: distance from the measured 0.60-0.70 nm step) "
                     "carrying the decision `covalent`, beside the record's own `departure`",
    "residual": "an ABSOLUTE residual in the solved quantity's own scale, not a relative "
                "comparison of two computations of it",
    "coverageError": "the same",
}

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

# `T-212` -- the RULE SCOPE, which is wider than the gate's predicate.  `C-0129`'s strict census
# keys on the leaf name `departure` under those two parents; the same record carries three more
# spellings of the same quantity, and 478 `reproductions[*].relativeDeparture` fields in 33 files
# break the same rule.  Reported, never gated: closing it is 36 further study re-runs.
SCOPE_TESTS = [
    ({"reproductions": [{"departure": 5.36821841e-06}]}, 1, 1,
     "the gate's own shape: `departure` under `reproductions`"),
    ({"convergence": [{"relativeDeparture": 5.36821841e-06}]}, 0, 1,
     "the same quantity under another spelling -- IN SCOPE, NOT IN THE GATE"),
    ({"convergence": [{"departureFromFinest": 5.36821841e-06}]}, 0, 1,
     "`departureFromFinest`, 337 of which sit under `convergence`"),
    ({"potentialOfZeroCharge": [{"departure": 0.001420712}]}, 0, 0,
     "T-193's departure in VOLTS -- in neither, and that is the point"),
    ({"departures": [{"relativeDeparture": 0.000123456}]}, 0, 0,
     "T-160's own ANSWER, which the rule must not reach"),
    ({"reproductions": [{"departure": 5.4e-06}]}, 0, 0,
     "already at two digits"),
    ({"upstreamChecks": [{"departure": 5.36821841e-06}]}, 0, 0,
     "a comparison against a carried upstream number is not a residual between two refinements"),
]

# `T-214` -- the GATE's own predicate, tested separately from the census that reports it.
# Each row is (document, expected gate hits, description). `C-0131` refused to gate the wider
# predicate because the tree was not clean under it; it is clean now, so the gate is the rule and
# these rows are what stop it narrowing back, and the mutation coverage is MEASURED rather than
# asserted: **6** of the 13 fail if `departure_gate` is reverted to `C-0129`'s leaf-name predicate
# (row 5 does not, being the one spelling both predicates share) and **4** fail if the record
# qualifier is dropped.  A predicate that is only ever narrowed becomes a claim of cleanliness
# (`C-0083`), so the test has to bite in both directions.
GATE_TESTS = [
    ({"reproductions": [{"departure": 5.36821841e-06}]}, 1,
     "C-0129's own shape, which the widened gate must still catch"),
    ({"reproductions": [{"relativeDeparture": 5.36821841e-06}]}, 1,
     "503 of these exist; the gate must fire"),
    ({"reproductions": [{"departureFromFinest": 5.36821841e-06}]}, 1,
     "`departureFromFinest` under `reproductions`"),
    ({"reproductions": [{"relativeDepartureInStroke": 5.36821841e-06}]}, 1,
     "the fourth spelling, 12 fields in one file"),
    ({"convergence": [{"departure": 5.36821841e-06}]}, 1,
     "the second record type"),
    ({"convergence": [{"relativeDeparture": 5.36821841e-06}]}, 1,
     "the shape T-214 re-emitted 351 of"),
    ({"convergence": [{"departureFromFinest": 5.36821841e-06}]}, 1,
     "337 of these exist"),
    ({"convergence": [{"relativeDepartureInStroke": 5.36821841e-06}]}, 1,
     "the fourth spelling under the second record"),
    ({"potentialOfZeroCharge": [{"departure": 0.001420712}]}, 0,
     "CH-0154's VOLTS: the record qualifier is what keeps the gate off a literature comparison"),
    ({"departures": [{"relativeDeparture": 0.000123456}]}, 0,
     "T-160's own ANSWER, declared at six digits at its own emission site"),
    ({"upstreamChecks": [{"departure": 5.36821841e-06}]}, 0,
     "a comparison against a carried upstream number, 288 fields in 3 files"),
    ({"stationLattice": [{"departure": 0.341234568}]}, 0,
     "a lattice coordinate difference, in nm"),
    ({"reproductions": [{"departure": 5.4e-06, "carried": 1.23456789}]}, 0,
     "a compliant departure beside a nine-digit sibling: the gate must not reach the sibling"),
    # `T-225` -- the eight spellings the shape census classified IN.  One row each, so a narrowing
    # back to `T-214`'s four fails eight NAMED tests rather than shrinking the corpus silently.
    ({"convergence": [{"relativeError": 0.00298087}]}, 1,
     "T-225: T-1d's mesh-refinement residual, |P - P_finest| / P_finest"),
    ({"convergence": [{"relativeSpread": 0.00629921962}]}, 1,
     "T-225: T-164's spread over a nested 1/2/4 subdivision"),
    ({"convergence": [{"relativeMovement": 2.20225957e-05}]}, 1,
     "T-225: T-108's |coarse - fine| / coarse, which T-182 and T-189 already emit at two digits"),
    ({"convergence": [{"multiplierDeparture": 0.000638001852}]}, 1,
     "T-225: T-60's 2-D edge mesh residual on the force multiplier, MISSED by CH-0169's census"),
    ({"convergence": [{"gradientDeparture": 0.00505980955}]}, 1,
     "T-225: the same on d ln mu/dh, which converges more slowly and is the binding one"),
    ({"convergence": [{"firstIntegralRelativeSpread": 0.00605645349}]}, 1,
     "T-225: T-3a's first integral is constant in exact arithmetic"),
    ({"convergence": [{"firstIntegralCoreSpread": 0.000282217766}]}, 1,
     "T-225: the same, over the core of the gap"),
    ({"convergence": [{"centrelineRouteSpread": 0.000430063133}]}, 1,
     "T-225: T-3b's two evaluation routes to one solved load"),
    ({"reproductions": [{"relativeError": 0.00298087}]}, 1,
     "T-225: the widened spellings are gated in BOTH records, not only in `convergence`"),
    # `T-225` -- the six the shape census classified OUT.  One row each, so SWEEPING a name in by
    # pattern fails six NAMED tests.  These are the rows `CH-0169` refused a mechanical widening
    # for, and the refusal is now a test rather than a sentence.
    ({"convergence": [{"residualExponent": -11.0931}]}, 0,
     "T-225: a log10 of a residual -- two digits is -11, i.e. 1e-11 for a solved 8.07e-12"),
    ({"convergence": [{"coverageErrorExponent": -14.1669}]}, 0,
     "T-225: the same, on the coverage error"),
    ({"convergence": [{"observedOrder": 2.07533}]}, 0,
     "T-225: a convergence ORDER -- CLAUDE.md quotes 2.08-2.32 and 1.59 at three digits"),
    ({"convergence": [{"worstResidual": 0.249373451}]}, 0,
     "T-225: T-117's closure residual is a LENGTH in nm and carries the decision `covalent`"),
    ({"convergence": [{"residual": 0.00298087}]}, 0,
     "T-225: an ABSOLUTE residual in the solved quantity's own scale"),
    ({"convergence": [{"coverageError": 0.00298087}]}, 0,
     "T-225: the same"),
    # `T-225` -- the record qualifier still protects the studies that OWN these spellings
    ({"collars": [{"centrelineRouteSpread": 0.000430063133}]}, 0,
     "T-160 declares centrelineRouteSpread at THREE digits and emits it outside both records"),
    ({"quantities": [{"relativeMovement": 3.30000004e-13}]}, 0,
     "P-18's own determined-precision ANSWER, which is what CH-0169 feared a widening would round"),
    ({"forces": [{"firstIntegralCoreSpread": 0.000282217766}]}, 0,
     "T-3a emits the same spread outside the record, where `numericallyResolved` is decided on it"),
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


def departures_in(document, path):
    """`(gated, scoped)` over-precise departure fields of one already-parsed `document`.

    ``gated`` is `C-0129`'s strict predicate — the leaf key `departure` inside a
    [STRICT_DEPARTURE_PARENTS] record — and is what `--departures` exits 1 on.

    ``scoped`` is the **rule**: any [DEPARTURE_KEYS] spelling inside such a record.
    It is a superset of ``gated`` by construction, it is reported and never gated, and the gap
    between the two is `T-212`'s measurement — 199 fields in 27 files against 601 in 63 when the
    audit was closed.  The **record** qualifier is load-bearing in both: `T-193` emits a
    `departure` in volts under `potentialOfZeroCharge`, and `T-160` emits its own *answer* as
    `departures[*].relativeDeparture`.
    """
    gated, scoped = [], []
    for pointer, value in _numbers(document):
        key = pointer.rsplit("/", 1)[-1]
        if key not in DEPARTURE_KEYS:
            continue
        digits = significant_digits(str(value))
        if digits <= DEPARTURE_DIGITS:
            continue
        ancestors = [step for step in pointer.split("/") if step not in ("", "*")]
        record = ancestors[-2] if len(ancestors) >= 2 else None
        if record not in STRICT_DEPARTURE_PARENTS:
            continue
        entry = (path, pointer, str(value), digits)
        scoped.append(entry)
        if key == "departure":
            gated.append(entry)
    return gated, scoped


def departure_gate(document, path):
    """The fields `--departures` exits 1 on — **the rule's own scope** since `T-214`.

    Named separately from [departures_in] so that what the gate enforces is one function rather
    than a choice made at the exit statement, and so that narrowing it back to `C-0129`'s
    leaf-name predicate fails [GATE_TESTS] rather than silently shrinking the corpus it covers.
    """
    _, scoped = departures_in(document, path)
    return scoped


def check_departures(root=RESULTS):
    """Returns `(gated, scoped, wide)` lists of over-precise departure fields.

    ``wide`` keeps `C-0129`'s reported outer bound — **any** leaf key containing `departure` —
    which deliberately includes `departureRatio` and `plateDeparture`, ratios *between two
    models* that the rule does not cover.

    **It is a bound on ONE SPELLING FAMILY, not on the class** (`CH-0193`).  It over-counts in
    one direction and under-counts badly in the other: of the 54 fields `T-225`'s widened
    predicate gates, 7 contain the substring and 47 do not, and of the 21 it classifies OUT,
    none do.  It is retained because it is the census `C-0129` published and a reader who watched
    it fall is entitled to keep watching it; the ceiling on the CLASS is
    `tools/T-225-census.py`, which searches for the shape and requires a classification.
    """
    gated, scoped, wide = [], [], []
    for path in result_files(root):
        document = _load(path, keep_literals=True)
        a, b = departures_in(document, path)
        gated += a
        scoped += b
        for pointer, value in _numbers(document):
            key = pointer.rsplit("/", 1)[-1]
            if "departure" not in key.lower():
                continue
            if significant_digits(str(value)) <= DEPARTURE_DIGITS:
                continue
            wide.append((path, pointer, str(value), significant_digits(str(value))))
    return gated, scoped, wide


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
    for name in EXCLUDED_DEPARTURE_KEYS:
        if name in DEPARTURE_KEYS:
            failures += 1
            print(f"SELF-TEST FAILED — {name!r} is both classified OUT and gated")
    for document, expected, description in GATE_TESTS:
        parsed = json.loads(json.dumps(document), parse_float=Decimal)
        found = len(departure_gate(parsed, "fixture"))
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — gate, {description}: "
                  f"expected {expected}, found {found}")
    for document, gated, scoped, description in SCOPE_TESTS:
        # Parsed the way a committed file is parsed: `_numbers` walks `Decimal` leaves, because
        # the question is what the file CARRIES and `repr` returns the shortest round-trip.
        parsed = json.loads(json.dumps(document), parse_float=Decimal)
        strict, scope = departures_in(parsed, "fixture")
        if len(strict) != gated or len(scope) != scoped:
            failures += 1
            print(f"SELF-TEST FAILED — scope, {description}: expected gate {gated} / scope "
                  f"{scoped}, found {len(strict)} / {len(scope)}")
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
    total = (len(CONVERSION_TESTS) + len(DEPARTURE_TESTS) + len(SATURATION_TESTS)
             + len(SCOPE_TESTS) + len(GATE_TESTS) + len(EXCLUDED_DEPARTURE_KEYS) + 2)
    print(f"{total - failures} of {total} self-tests pass")
    return failures


# ---------------------------------------------------------------------------------------------
# reporting
# ---------------------------------------------------------------------------------------------

def report_departures():
    gated, scoped, wide = check_departures()
    print("-- departure precision (GATE on the RULE's own scope; C-0093's two-digit rule) --")
    print(f"  GATE  ({len(DEPARTURE_KEYS)} classified spellings inside a "
          f"reproductions/convergence record): "
          f"{len(scoped)} field(s) in {len({row[0] for row in scoped})} file(s)")
    print(f"  scope (the same predicate — since T-214 the gate IS the rule): "
          f"{len(scoped)} field(s) in {len({row[0] for row in scoped})} file(s)")
    print(f"  strict (C-0129's leaf-name predicate, now a proper subset of the gate): "
          f"{len(gated)} field(s) in {len({row[0] for row in gated})} file(s)")
    print(f"  wide  (any leaf key containing 'departure'; ONE SPELLING FAMILY, not the class "
          f"— CH-0193; NOT gated): "
          f"{len(wide)} field(s) in {len({row[0] for row in wide})} file(s)")
    for label, rows in (("GATE", scoped), ("scope", scoped), ("strict", gated)):
        counts = {}
        for path, _, _, _ in rows:
            counts[os.path.basename(path)] = counts.get(os.path.basename(path), 0) + 1
        if not counts:
            print(f"    {label}: clean")
            continue
        for name in sorted(counts, key=lambda n: (-counts[n], n)):
            print(f"    {label} {counts[name]:4d}  {name}")
    return scoped, gated, wide


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
    over_precise = None
    if census or "--departures" in argv:
        over_precise, _, _ = report_departures()
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
            return 1 if defects or over_precise else 0
    if not census and over_precise is not None:
        return 1 if over_precise else 0
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
