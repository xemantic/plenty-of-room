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
# T-278 -- what adding a rounding call to `CH-0223`'s seven emitters MOVES, simulated over the
# committed files before a single JVM starts.
#
#     tools/T-278-rounding-simulation.py                 the seven, predicted field by field
#     tools/T-278-rounding-simulation.py --all           every committed result file
#     tools/T-278-rounding-simulation.py --selftest
#
# WHY THIS EXISTS. `C-0138` proved a one-line widening of `digitsByKey` could move only fields
# whose key and record matched -- 351 fields in 31 files -- "checked in Python before a JVM
# started", and `C-0150` did the same for the prose sweep. A rounding change is a PURE FUNCTION of
# the committed document, so the blast radius is derivable rather than discoverable: the re-run is
# then a CONFIRMATION, and any field that moves and is not on this list is a finding about the
# study rather than about the rounding.
#
# This mirrors `structure/ResultRounding.kt` -- the parameter-record exemption, the departure
# `record/spelling` map, the integral-number rendering and the absolute floor -- and the mirror is
# held to the Kotlin by self-tests taken from that file's own KDoc examples. It is the same
# mirroring `tools/emission_header.py` makes of `structure/ResultEmission.kt`, and for the same
# reason: an emission rule reaches only the emitters written in its own language.
import json
import math
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")

RESULT_SIGNIFICANT_DIGITS = 9
RESULT_ABSOLUTE_FLOOR = 1e-9
DEPARTURE_SIGNIFICANT_DIGITS = 2
DEPARTURE_RECORDS = ("reproductions", "convergence")
DEPARTURE_SPELLINGS = (
    "departure", "relativeDeparture", "departureFromFinest", "relativeDepartureInStroke",
    "relativeError", "relativeSpread", "relativeMovement", "multiplierDeparture",
    "gradientDeparture", "firstIntegralRelativeSpread", "firstIntegralCoreSpread",
    "centrelineRouteSpread",
)
PARAMETER_RECORDS = ("parameters", "runParameters", "citedInputs", "emission")

DEPARTURE_DIGITS_BY_KEY = {
    "%s/%s" % (record, spelling): DEPARTURE_SIGNIFICANT_DIGITS
    for record in DEPARTURE_RECORDS
    for spelling in DEPARTURE_SPELLINGS
}

# The seven of `CH-0223`, with the precision each is given and the ground for it. The ground is a
# SENTENCE and not a number, because the number is the easy half.
SEVEN = {
    "T-1-layer-stiffness.json": RESULT_SIGNIFICANT_DIGITS,
    "T-1c-crossover-valid-layer-response.json": RESULT_SIGNIFICANT_DIGITS,
    "T-6-mean-field-screening-validity.json": RESULT_SIGNIFICANT_DIGITS,
    "T-7-poroelastic-drainage.json": RESULT_SIGNIFICANT_DIGITS,
    "P-3-peg-material-parameters.json": RESULT_SIGNIFICANT_DIGITS,
    "P-6-solvent-quality-vs-salt.json": RESULT_SIGNIFICANT_DIGITS,
    "P-9-grafted-chi.json": RESULT_SIGNIFICANT_DIGITS,
}


def round_for_result(value, digits=RESULT_SIGNIFICANT_DIGITS, floor=RESULT_ABSOLUTE_FLOOR):
    """`structure/ResultRounding.kt`'s `roundForResult`, to the branch."""
    if not (1 <= digits <= RESULT_SIGNIFICANT_DIGITS):
        raise ValueError("digits must be within 1..%d, was: %d" % (RESULT_SIGNIFICANT_DIGITS, digits))
    if floor < 0.0:
        raise ValueError("floor must not be negative, was: %r" % floor)
    if not math.isfinite(value):
        return value
    if value == 0.0:
        return 0.0
    if abs(value) < floor:
        return 0.0
    # THE SCALE IS THE DECIMAL LITERAL, NOT `10.0 ** k`. Kotlin's `10.0.pow(k)` goes through
    # `Math.pow`, which is correctly rounded for an integral exponent; Python's `**` is not, and
    # over the range this corpus reaches they disagree at EXACTLY ONE exponent, `k = 23`:
    # `10.0 ** 23` is `1.0000000000000001e+23` and `float("1e23")` is `1e+23`. That one ulp is
    # enough to move the answer -- `2.1000000000000002e-15` rounds to `2.1e-15` under the first
    # and to itself under the second, which is what `T-190` actually emits. `float("1e%d")` is
    # the correctly-rounded decimal and agrees with the Kotlin at every exponent.
    scale = float("1e%d" % (digits - 1 - math.floor(math.log10(abs(value)))))
    # Kotlin's `Double.roundToLong()` rounds TIES TOWARDS POSITIVE INFINITY, which is
    # `floor(x + 0.5)` at every sign -- not half-even (Python's `round`, which sends 2.5 to 2)
    # and not half-away-from-zero (which sends -2.5 to -3). The distinction is reachable: a value
    # sitting within half a unit in the last place of the nine-digit boundary is exactly
    # `C-0162`'s `T-14` mechanism, and this mirror exists to predict such a file.
    scaled = value * scale
    return math.floor(scaled + 0.5) / scale


def walk(node, digits, digits_by_key, floor, record=None, inside_parameters=False, path="",
         moved=None):
    """Rounds `node` as `roundedForResult` would, appending every moved leaf to `moved`."""
    if isinstance(node, dict):
        return {
            key: walk(
                value,
                # Most specific wins, exactly as the Kotlin does: a `record/key` entry beats a
                # bare `key` one, which beats the precision inherited from the enclosing subtree.
                (digits_by_key.get("%s/%s" % (record, key)) if record else None)
                or digits_by_key.get(key) or digits,
                digits_by_key, floor, record=key,
                inside_parameters=inside_parameters or key in PARAMETER_RECORDS,
                path="%s/%s" % (path, key), moved=moved,
            )
            for key, value in node.items()
        }
    if isinstance(node, list):
        return [
            walk(value, digits, digits_by_key, floor, record, inside_parameters,
                 "%s/%d" % (path, index), moved)
            for index, value in enumerate(node)
        ]
    if isinstance(node, bool) or node is None or isinstance(node, str):
        return node
    if isinstance(node, int):
        return node
    if inside_parameters:
        return node
    # Kotlin tests the JSON TOKEN for `.`, `e` or `E`, so only a bare integer literal passes
    # through unrounded -- and `json` has already given those to the `int` branch above. A float
    # written `1.0E-5` carries both a `.` and an `E`, so it is rounded like any other.
    rounded = round_for_result(node, digits, floor)
    if rounded != node and moved is not None:
        moved.append((path, node, rounded, digits))
    return rounded


def simulate(document, digits, floor=RESULT_ABSOLUTE_FLOOR):
    moved = []
    walk(document, digits, DEPARTURE_DIGITS_BY_KEY, floor, moved=moved)
    return moved


# The commit `CH-0223` was filed on, and the state its 41 369 / 41 297 / 70 were measured at.
CH_0223_COMMIT = "b853b85"


def _committed(ref, path):
    """`git show <ref>:<path>` parsed, or None where the object is unavailable."""
    import subprocess
    try:
        result = subprocess.run(
            ["git", "-C", ROOT, "show", "%s:%s" % (ref, path)],
            capture_output=True, text=True, check=False,
        )
    except OSError:
        return None
    if result.returncode != 0 or not result.stdout:
        return None
    try:
        return json.loads(result.stdout)
    except ValueError:
        return None


def _load(name):
    # `parse_float=float` keeps the token so an integral literal can be told from `45.0`; json
    # already distinguishes `45` (int) from `45.0` (float), which is what the walk relies on.
    return json.load(open(os.path.join(RESULTS, name), encoding="utf-8"))


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    # `roundForResult`'s own branches, from `structure/ResultRounding.kt`.
    check("nine digits", round_for_result(1.23456789012), 1.23456789)
    check("an exact zero passes at a zero floor", round_for_result(0.0, 9, 0.0), 0.0)
    check("below the floor is zero", round_for_result(1e-12), 0.0)
    check("a zero floor does not flatten", round_for_result(1e-12, 9, 0.0), 1e-12)
    check("a non-finite passes", str(round_for_result(float("inf"))), "inf")
    check("two digits", round_for_result(3.19469867e-11, 2, 0.0), 3.2e-11)
    # Ties go towards POSITIVE INFINITY, which separates the Kotlin from both of the other two
    # conventions a mirror could plausibly have been written with.
    check("a positive tie rounds up, not half-even", round_for_result(2.5, 1, 0.0), 3.0)
    check("a negative tie rounds towards zero, not away from it",
          round_for_result(-2.5, 1, 0.0), -2.0)
    check("and the same one place in", round_for_result(0.125, 2, 0.0), 0.13)
    # The scale is the decimal literal. At `k = 23` -- the only exponent in this corpus's range
    # where Python's `**` and Java's `Math.pow` disagree -- `10.0 ** 23` would return `2.1e-15`
    # here, and `T-190` emits the other value.
    check("the one-ulp scale exponent agrees with the Kotlin",
          round_for_result(2.1000000000000002e-15, 9, 0.0), 2.1000000000000002e-15)
    # The tree walk's three exemptions.
    check("a parameter block is not rounded",
          simulate({"parameters": {"x": 1.23456789012}}, 9), [])
    check("an emission block is not rounded",
          simulate({"emission": {"lattice": "none", "regime": {"gap": 1.23456789012}}}, 9), [])
    check("a result leaf is rounded",
          [row[0] for row in simulate({"x": 1.23456789012}, 9)], ["/x"])
    check("a departure inside a departure record takes two digits",
          [row[3] for row in simulate({"convergence": [{"departure": 1.23456789012e-9}]}, 9, 0.0)],
          [2])
    check("the same spelling outside such a record does not",
          [row[3] for row in simulate({"potentialOfZeroCharge": {"departure": 1.23456789012}}, 9)],
          [9])
    check("an integral JSON number passes through", simulate({"paths": 45}, 9), [])
    check("a float in exponent form is still rounded",
          [row[0] for row in simulate({"x": 1.23456789012e-5}, 9)], ["/x"])
    check("a string passes through", simulate({"finding": "0.123456789012"}, 9), [])
    check("a boolean passes through", simulate({"flat": True}, 9), [])
    check("a null passes through", simulate({"regime": None}, 9), [])
    # `CH-0223`'s own predicate, reproduced on the artifact it was measured on -- AT THE COMMIT IT
    # WAS MEASURED ON. Reading the live file instead makes this assertion fail the moment the
    # defect is repaired, which is the same staleness `CH-0212` records for a result file whose
    # subject is the corpus: a census is a function of a mutable tree, so it must NAME the tree
    # state. `b853b85` is the commit `CH-0223` was filed on; the check is skipped rather than
    # failed where that object is unavailable (a shallow clone, or an export with no `.git`).
    baseline = _committed(CH_0223_COMMIT, "gpd/results/P-9-grafted-chi.json")
    if baseline is not None:
        check("CH-0223's P-9 count at the commit it was filed on",
              len(simulate(baseline, 9, floor=0.0)), 70)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv):
    # `CH-0268`: wired as `--self-test` in `build.gradle.kts` and dispatched on `--selftest`, so
    # the wired "self-test" task ran the FULL CENSUS instead and exited 0 either way.
    if "--selftest" in argv or "--self-test" in argv:
        return _selftest()
    names = sorted(os.listdir(RESULTS)) if "--all" in argv else sorted(SEVEN)
    verbose = "--verbose" in argv
    total = 0
    zeroed = 0
    for name in names:
        if not name.endswith(".json"):
            continue
        try:
            document = _load(name)
        except ValueError:
            print("UNPARSEABLE %s" % name)
            continue
        digits = SEVEN.get(name, RESULT_SIGNIFICANT_DIGITS)
        moved = simulate(document, digits)
        flattened = [row for row in moved if row[2] == 0.0]
        total += len(moved)
        zeroed += len(flattened)
        print("%-46s %6d field(s) move, %4d flattened to 0.0, at %d digits"
              % (name, len(moved), len(flattened), digits))
        if verbose:
            for path, before, after, at in moved[:40]:
                print("    %-60s %-24r -> %-24r (%d)" % (path, before, after, at))
            if len(moved) > 40:
                print("    ... %d more" % (len(moved) - 40))
    print("%d field(s) predicted to move over %d file(s); %d flattened to exactly 0.0"
          % (total, len(names), zeroed))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
