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
"""T-225 — MEASURES the mutation coverage of the departure gate, in both directions.

    tools/T-225-mutation-test.py
    tools/T-225-mutation-test.py --check    # exit 1 if any classified name is unprotected

`C-0127`'s standard: restoring the old narrow predicate must FAIL a NAMED test.  `C-0138` met it
by measuring 6 of 13 and 4 of 13 rather than asserting coverage.  `T-225` widens the predicate in
one direction and pins six exclusions in the other, so the coverage has to be measured **both**
ways — a predicate that is only ever narrowed becomes a claim of cleanliness (`C-0083`), and a
predicate that is only ever widened becomes a pattern, which is exactly what `CH-0169` refused.

The per-name rows are the useful ones: every classified name must be held open by at least one
named `GATE_TESTS` row, or its classification is an opinion rather than a test.
"""

from __future__ import annotations

import importlib.util
import json
import os
import sys
from decimal import Decimal

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _load():
    spec = importlib.util.spec_from_file_location(
        "hygiene", os.path.join(ROOT, "tools", "check-result-file-hygiene.py")
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def failures_under(mutate=None):
    hygiene = _load()
    if mutate:
        mutate(hygiene)
    failed = []
    for document, expected, description in hygiene.GATE_TESTS:
        parsed = json.loads(json.dumps(document), parse_float=Decimal)
        if len(hygiene.departure_gate(parsed, "fixture")) != expected:
            failed.append(description)
    return failed, len(hygiene.GATE_TESTS)


def _narrow(names):
    return lambda module: setattr(module, "DEPARTURE_KEYS", tuple(names))


def _drop_record(module):
    def patched(document, path):
        gated, scoped = [], []
        for pointer, value in module._numbers(document):
            key = pointer.rsplit("/", 1)[-1]
            if key not in module.DEPARTURE_KEYS:
                continue
            digits = module.significant_digits(str(value))
            if digits <= module.DEPARTURE_DIGITS:
                continue
            entry = (path, pointer, str(value), digits)
            scoped.append(entry)
            if key == "departure":
                gated.append(entry)
        return gated, scoped
    module.departures_in = patched


T212_FOUR = ("departure", "relativeDeparture", "departureFromFinest",
             "relativeDepartureInStroke")


def main(argv):
    hygiene = _load()
    check = "--check" in argv
    baseline, total = failures_under()
    print(f"baseline: {len(baseline)} of {total} GATE_TESTS fail")
    if baseline:
        for description in baseline:
            print(f"  UNEXPECTED: {description}")
        return 1

    rows = [
        ("narrowed back to T-214's four spellings", _narrow(T212_FOUR)),
        ("narrowed back to C-0129's leaf name", _narrow(("departure",))),
        ("the six EXCLUDED names swept in by pattern",
         lambda m: setattr(m, "DEPARTURE_KEYS",
                           tuple(m.DEPARTURE_KEYS) + tuple(m.EXCLUDED_DEPARTURE_KEYS))),
        ("the record qualifier dropped", _drop_record),
    ]
    print("\n-- whole-predicate mutations --")
    for label, mutate in rows:
        failed, total = failures_under(mutate)
        print(f"  {label:48s} {len(failed):2d} of {total} fail")

    unprotected = []
    print("\n-- per-name mutations: each classified name, one at a time --")
    for name in hygiene.DEPARTURE_KEYS:
        failed, total = failures_under(
            lambda m, n=name: setattr(
                m, "DEPARTURE_KEYS", tuple(k for k in m.DEPARTURE_KEYS if k != n))
        )
        print(f"  drop     {name:32s} {len(failed):2d} of {total} fail")
        if not failed:
            unprotected.append(("IN", name))
    for name in hygiene.EXCLUDED_DEPARTURE_KEYS:
        failed, total = failures_under(
            lambda m, n=name: setattr(m, "DEPARTURE_KEYS", tuple(m.DEPARTURE_KEYS) + (n,))
        )
        print(f"  sweep in {name:32s} {len(failed):2d} of {total} fail")
        if not failed:
            unprotected.append(("OUT", name))

    if unprotected:
        print()
        for verdict, name in unprotected:
            print(f"UNPROTECTED {verdict} classification: {name!r} — "
                  f"no GATE_TESTS row fails when it is changed")
        return 1 if check else 0
    print(f"\nevery one of {len(hygiene.DEPARTURE_KEYS)} IN and "
          f"{len(hygiene.EXCLUDED_DEPARTURE_KEYS)} OUT classifications is held open "
          f"by at least one named test")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
