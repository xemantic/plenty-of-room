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
"""T-225 — the SHAPE census behind `DEPARTURE_SPELLINGS`, and the check that keeps it complete.

    tools/T-225-census.py              # the census, with the classification beside it
    tools/T-225-census.py --check      # exit 1 if the corpus carries an UNCLASSIFIED candidate
    tools/T-225-census.py --self-test

WHY THIS EXISTS.  `C-0131` enumerated four spellings and called them *"every spelling the corpus
uses"*.  `CH-0169` upheld that a **list is a census that stopped** and measured seven more names —
and its own census stopped too, missing `T-60`'s `multiplierDeparture` and `gradientDeparture`
(which are spelled with the rule's own word) and `T-1d`/`T-1e`'s `observedOrder`.

The cure is not a wider list.  It is to search for the **shape** — a leaf key inside a
`reproductions` or `convergence` record whose name denotes a *discrepancy* — and to require that
every name the shape turns up be **classified**, in or out, with a ground.  `--check` is the
standing obligation `CH-0169` asks for, mechanised: it fails the moment a study coins a name that
appears in neither `DEPARTURE_KEYS` nor `EXCLUDED_DEPARTURE_KEYS`.

The classification itself is a **judgement**, and it is deliberately NOT derived here.  It lives
beside the predicate it governs, in `tools/check-result-file-hygiene.py` and in
`src/main/kotlin/structure/ResultRounding.kt`, so that a reader who changes one is looking at the
other.  What this tool contributes is the guarantee that the judgement has been made for every
candidate the corpus contains.
"""

from __future__ import annotations

import collections
import importlib.util
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")

#: The SHAPE.  A leaf key whose name denotes a discrepancy between two computations of one
#: quantity — or a transform of one, which is how the `log10` exclusions get *found* rather than
#: being missed.  Deliberately over-inclusive: a false positive costs one line of classification,
#: and a false negative is the whole defect this task exists to close.
DISCREPANCY = re.compile(
    r"departure|spread|error|residual|movement|discrep|deviation|mismatch|exponent|order",
    re.IGNORECASE,
)


def _hygiene():
    spec = importlib.util.spec_from_file_location(
        "hygiene", os.path.join(ROOT, "tools", "check-result-file-hygiene.py")
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def candidates(document, records):
    """`{leaf: [literal, ...]}` for every numeric leaf of the SHAPE inside a `records` record.

    The record is the nearest enclosing **object entry** above the leaf, which is exactly
    `structure/ResultRounding.kt`'s definition: an array contributes no key, so
    `convergence[*].relativeError` sees `convergence`.
    """
    found = collections.defaultdict(list)

    def walk(node, parents):
        if isinstance(node, dict):
            for key, value in node.items():
                walk(value, parents + [key])
        elif isinstance(node, list):
            for value in node:
                walk(value, parents)
        else:
            if len(parents) < 2:
                return
            leaf, record = parents[-1], parents[-2]
            if record not in records or not DISCREPANCY.search(leaf):
                return
            try:
                float(str(node))
            except (TypeError, ValueError):
                return
            found[leaf].append(str(node))

    walk(document, [])
    return found


def census(root=RESULTS):
    """`(per_name, per_name_over)` — every candidate name, and its over-precise occurrences."""
    hygiene = _hygiene()
    records = set(hygiene.STRICT_DEPARTURE_PARENTS)
    per_name = collections.Counter()
    over = collections.defaultdict(collections.Counter)
    for name in sorted(os.listdir(root)):
        if not name.endswith(".json"):
            continue
        document = hygiene._load(os.path.join(root, name), keep_literals=True)
        for leaf, literals in candidates(document, records).items():
            per_name[leaf] += len(literals)
            for literal in literals:
                try:
                    digits = hygiene.significant_digits(literal)
                except Exception:  # a non-decimal literal is not a departure
                    continue
                if digits > hygiene.DEPARTURE_DIGITS:
                    over[leaf][name] += 1
    return per_name, over


SELF_TESTS = [
    ({"convergence": [{"relativeError": 1.0}]}, {"relativeError"},
     "a discrepancy name inside a departure record"),
    ({"convergence": [{"pressure": 1.0}]}, set(),
     "a LEVEL inside a departure record is not of the shape"),
    ({"forces": [{"relativeError": 1.0}]}, set(),
     "the shape outside a departure record is not a candidate"),
    ({"convergence": [{"residualExponent": -11.0931}]}, {"residualExponent"},
     "a log10 IS a candidate -- which is how it gets classified OUT rather than missed"),
    ({"convergence": [{"observedOrder": 2.07533}]}, {"observedOrder"},
     "CH-0169 missed this one; the shape finds it"),
    ({"convergence": [{"multiplierDeparture": 0.000638}]}, {"multiplierDeparture"},
     "a COMPOUND of the rule's own word, which an exact-match census cannot see"),
    ({"convergence": [{"relativeError": "not a number"}]}, set(),
     "a string is not a numeric leaf"),
    ({"convergence": {"nested": {"relativeError": 1.0}}}, set(),
     "the record is the NEAREST enclosing object entry, so `nested` shadows `convergence`"),
    ({"convergence": [[{"relativeError": 1.0}]]}, {"relativeError"},
     "an array contributes no key, at any depth"),
    ({"relativeError": 1.0}, set(),
     "a top-level leaf has no record at all"),
]


def self_test():
    failures = 0
    for document, expected, description in SELF_TESTS:
        found = set(candidates(document, {"reproductions", "convergence"}))
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — {description}: expected {sorted(expected)}, "
                  f"found {sorted(found)}")
    # the regex must find every classified name, or the classification is unreachable
    hygiene = _hygiene()
    for name in tuple(hygiene.DEPARTURE_KEYS) + tuple(hygiene.EXCLUDED_DEPARTURE_KEYS):
        if not DISCREPANCY.search(name):
            failures += 1
            print(f"SELF-TEST FAILED — the shape does not match the classified name {name!r}")
    total = len(SELF_TESTS) + len(_hygiene().DEPARTURE_KEYS) + len(_hygiene().EXCLUDED_DEPARTURE_KEYS)
    print(f"{total - failures} of {total} self-tests pass")
    return failures


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    hygiene = _hygiene()
    classified_in = set(hygiene.DEPARTURE_KEYS)
    classified_out = set(hygiene.EXCLUDED_DEPARTURE_KEYS)
    per_name, over = census()
    unclassified = sorted(set(per_name) - classified_in - classified_out)
    if "--check" in argv:
        if unclassified:
            for name in unclassified:
                print(f"UNCLASSIFIED departure-record candidate: {name!r} "
                      f"({per_name[name]} field(s)) — classify it IN "
                      f"(check-result-file-hygiene.DEPARTURE_KEYS and "
                      f"ResultRounding.DEPARTURE_SPELLINGS) or OUT "
                      f"(EXCLUDED_DEPARTURE_KEYS, with a ground)")
            print(f"T-225 census: {len(unclassified)} unclassified candidate name(s)")
            return 1
        print(f"T-225 census ok: {len(per_name)} candidate name(s), "
              f"{len(classified_in & set(per_name))} in, "
              f"{len(classified_out & set(per_name))} out, 0 unclassified")
        return 0
    print("-- T-225: candidate departure spellings, by SHAPE, inside a "
          "reproductions/convergence record --")
    print(f"{'name':34s} {'fields':>7s} {'>2 digits':>10s}  class  files")
    for name in sorted(per_name, key=lambda n: (-sum(over[n].values()), n)):
        verdict = ("IN " if name in classified_in
                   else "OUT" if name in classified_out else "???")
        files = ", ".join(f"{f.split('-')[0]}-{f.split('-')[1]}:{c}"
                          for f, c in sorted(over[name].items()))
        print(f"{name:34s} {per_name[name]:7d} {sum(over[name].values()):10d}  {verdict}    {files}")
    total = sum(sum(over[n].values()) for n in over if n not in classified_out)
    files = {f for n in over if n not in classified_out for f in over[n]}
    print(f"\nin scope and over-precise: {total} field(s) in {len(files)} file(s)")
    excluded = sum(sum(over[n].values()) for n in over if n in classified_out)
    exfiles = {f for n in over if n in classified_out for f in over[n]}
    print(f"classified OUT and left alone: {excluded} field(s) in {len(exfiles)} file(s)")
    if unclassified:
        print(f"UNCLASSIFIED: {unclassified}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
