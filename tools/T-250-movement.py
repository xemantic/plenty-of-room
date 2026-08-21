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
"""What a re-emission sweep moved, BY KIND, against each file's COMMITTED version (`T-250`).

`C-0153` reported one file's movement by kind and confirmed the staleness identity by hand.
`T-250` re-emits 47, so the same report has to be a tool.  For every result file named on the
command line it reads the working-tree copy and `git show HEAD:<path>`, flattens both to JSON
pointers, and classifies every difference:

  prose            a STRING leaf whose digits moved and whose non-numeric skeleton did not
  wording          a STRING leaf whose skeleton moved -- a verdict change, and a finding
  departure        a NUMERIC leaf under a departure spelling in a reproductions/convergence record
  numeric          any other NUMERIC leaf -- `F1`, and a finding
  boolean          a BOOLEAN leaf -- a decision, and a finding
  added / removed  a pointer present in only one of the two

and then checks the STALENESS IDENTITY on every moved prose token: the new token's VALUE must be
exactly `roundForResult(old value, d)` for one of the declared precisions `d`.  The comparison is
on the VALUE and not on the text, because Kotlin's `Double.toString` and Python's `repr` disagree
about exponent spelling (`8.755985E-4` against `0.0008755985`) and that is a rendering.

    tools/T-250-movement.py gpd/results/T-12-*.json ...
    tools/T-250-movement.py --all            # every file the T-249 census flagged
    tools/T-250-movement.py --json <out>
    tools/T-250-movement.py --self-test
"""

import importlib.util
import json
import math
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DIGITS = re.compile(r"[0-9]+(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
# For PAIRING -- deliberately more permissive than `check-result-file-hygiene.py`'s
# `PROSE_NUMBER`, whose trailing `(?!\w)` refuses a number abutting a unit letter
# (`1.3339603864734038x`).  That guard is right for a DETECTOR, whose false positives cost
# more than its false negatives, and wrong here, where the job is to line up the tokens of
# two renderings of one sentence.  The gap between the two is measured and reported as
# `tokensBelowTheCensus`: they are real instances the census cannot see (`C-0153` states
# the direction; this measures it).
NUMBER = re.compile(r"(?<![\w.])(-?\d+\.\d+(?:[eE][+-]?\d+)?)(?!\d)(?!\.\d)")

#: the precisions a call site is allowed to declare -- nine for a quantity, two for a departure
DECLARED_DIGITS = (9, 2)


def _module(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


hygiene = _module("hygiene", "check-result-file-hygiene.py")

#: `structure/ResultRounding.kt`'s absolute floor, a claim in the LOCKED units (`P-18`)
RESULT_ABSOLUTE_FLOOR = 1.0e-9


def round_significant(value, digits, floor=0.0):
    if value == 0.0 or not math.isfinite(value):
        return value
    if abs(value) < floor:
        return 0.0
    scale = 10.0 ** (digits - 1 - math.floor(math.log10(abs(value))))
    return round(value * scale) / scale


def skeleton(text):
    """`C-0127`'s prose classifier: a moved STRING is a verdict change only if its
    non-numeric skeleton moved."""
    return DIGITS.sub("#", str(text))


def flatten(document, pointer="", into=None):
    if into is None:
        into = {}
    if isinstance(document, dict):
        for key, value in document.items():
            flatten(value, f"{pointer}/{key}", into)
    elif isinstance(document, list):
        for index, value in enumerate(document):
            flatten(value, f"{pointer}[{index}]", into)
    else:
        into[pointer] = document
    return into


#: `T-249`'s shipped trailing guard, retained so the widening stays MEASURABLE (`C-0092`)
T249_NUMBER = re.compile(r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?!\w)(?!\.\d)")


def _t249_tokens(text):
    return [literal for literal in T249_NUMBER.findall(text)
            if hygiene.significant_digits(literal) > hygiene.RESULT_DIGITS]


def is_departure_pointer(pointer):
    parent = any(f"/{name}[" in pointer or f"/{name}/" in pointer
                 for name in hygiene.STRICT_DEPARTURE_PARENTS)
    leaf = pointer.rsplit("/", 1)[-1]
    return parent and leaf in hygiene.DEPARTURE_KEYS


def explains(old_literal, new_literal):
    """Is `new_literal` exactly the rounding of `old_literal` at one declared precision?

    Returns the digit count that explains it, or `None`.  Both floors are tried: a quantity in
    the locked units carries `RESULT_ABSOLUTE_FLOOR`, and a dimensionless departure carries
    `0.0` (`P-18` -- the floor is a claim about pN and does not reach a ratio).
    """
    old, new = float(old_literal), float(new_literal)
    for digits in DECLARED_DIGITS:
        for floor in (0.0, RESULT_ABSOLUTE_FLOOR):
            if round_significant(old, digits, floor) == new:
                return digits
    return None


def committed(path, ref="HEAD"):
    """The file as it stands at `ref`.

    The ref is a PARAMETER and not `HEAD`, because the moment this sweep is committed `HEAD` is
    the post-repair corpus and every movement this tool reports collapses to zero.  A result file
    whose subject is the corpus must name the corpus state it measured.
    """
    rel = os.path.relpath(os.path.abspath(path), ROOT)
    blob = subprocess.run(["git", "-C", ROOT, "show", f"{ref}:{rel}"],
                          capture_output=True, text=True)
    if blob.returncode != 0:
        return None
    return json.loads(blob.stdout)


def compare(path, ref="HEAD"):
    """One file's movement, by kind, with the staleness identity measured on it."""
    head = committed(path, ref)
    if head is None:
        return dict(file=os.path.basename(path), notCommitted=True)
    now = json.load(open(path, encoding="utf-8"))
    before, after = flatten(head), flatten(now)
    kinds = dict(prose=0, wording=0, departure=0, numeric=0, boolean=0, added=0, removed=0)
    explained, unexplained, tokens, below_census = 0, [], 0, 0
    token_count_changed = 0
    wording_examples, numeric_examples = [], []
    for pointer in sorted(set(before) | set(after)):
        if pointer not in after:
            kinds["removed"] += 1
            continue
        if pointer not in before:
            kinds["added"] += 1
            continue
        old, new = before[pointer], after[pointer]
        if old == new:
            continue
        if isinstance(old, bool) or isinstance(new, bool):
            kinds["boolean"] += 1
            continue
        if isinstance(old, str) and isinstance(new, str):
            if skeleton(old) != skeleton(new):
                kinds["wording"] += 1
                if len(wording_examples) < 4:
                    wording_examples.append(pointer)
                continue
            kinds["prose"] += 1
            old_tokens, new_tokens = NUMBER.findall(old), NUMBER.findall(new)
            if len(old_tokens) != len(new_tokens):
                token_count_changed += 1
            # Against `T-249`'s OWN trailing guard, not the shipped one: the question this
            # field answers is how many of the moved tokens the detector that certified the
            # corpus clean could not see (`T-250` widened it; 31 in 4 files at HEAD).
            seen_by_census = _t249_tokens(old)
            for old_token, new_token in zip(old_tokens, new_tokens):
                if old_token == new_token:
                    continue
                tokens += 1
                if old_token.lstrip("-") not in seen_by_census:
                    below_census += 1
                if explains(old_token, new_token) is None:
                    unexplained.append((pointer, old_token, new_token))
                else:
                    explained += 1
            continue
        if is_departure_pointer(pointer):
            kinds["departure"] += 1
        else:
            kinds["numeric"] += 1
            if len(numeric_examples) < 4:
                numeric_examples.append(pointer)
    return dict(file=os.path.basename(path), **kinds, movedTokens=tokens,
                tokensExplained=explained, tokensUnexplained=len(unexplained),
                tokensBelowTheCensus=below_census, tokenCountChanged=token_count_changed,
                unexplainedExamples=[list(row) for row in unexplained[:6]],
                wordingExamples=wording_examples, numericExamples=numeric_examples)


def self_test():
    failures = 0

    def check(name, condition):
        nonlocal failures
        if not condition:
            failures += 1
            print(f"SELF-TEST FAILED — {name}")

    check("nine significant digits", round_significant(0.06517538540278571, 9) == 0.0651753854)
    check("two significant digits on a departure",
          round_significant(3.3864695769825204e-11, 2, 0.0) == 3.4e-11)
    check("the locked-units floor annihilates a dimensionless departure",
          round_significant(3.3864695769825204e-11, 2, RESULT_ABSOLUTE_FLOOR) == 0.0)
    check("an exact zero passes through", round_significant(0.0, 9) == 0.0)
    check("the identity explains a nine-digit rounding",
          explains("0.06517538540278571", "0.0651753854") == 9)
    check("the identity explains a two-digit departure",
          explains("3.3864695769825204E-11", "3.4E-11") == 2)
    check("the identity refuses an unrelated value",
          explains("0.06517538540278571", "0.0771753854") is None)
    check("the identity accepts a %.9f source token",
          explains("33.333333333", "33.3333333") == 9)
    check("the skeleton hides digits", skeleton("is 0.123456") == skeleton("is 0.1"))
    check("the skeleton exposes wording",
          skeleton("REFUSED at 0.1") != skeleton("ADMITTED at 0.1"))
    check("a departure pointer is recognised",
          is_departure_pointer("/reproductions[3]/departure"))
    check("a volts departure outside the two records is not",
          not is_departure_pointer("/potentialOfZeroCharge[0]/departure"))
    check("a convergence relativeDeparture is recognised",
          is_departure_pointer("/convergence[0]/relativeDeparture"))
    check("flatten reaches a nested leaf",
          flatten({"a": [{"b": 1}]}) == {"/a[0]/b": 1})
    check("NUMBER takes a negative token",
          NUMBER.findall("is -0.005999045832899163 kT") == ["-0.005999045832899163"])
    check("NUMBER takes a token abutting a unit letter, and so does the WIDENED detector",
          NUMBER.findall("short by 1.3339603864734038x") == ["1.3339603864734038"]
          and hygiene.unrounded_numbers_in("short by 1.3339603864734038x")
          == ["1.3339603864734038"])
    check("NUMBER still refuses the head of a dotted token",
          NUMBER.findall("build 1.4142135623730951.2") == [])
    check("T-249's own guard cannot see a token abutting a unit letter",
          _t249_tokens("short by 1.3339603864734038x") == []
          and _t249_tokens("short by 1.3339603864734038 x") == ["1.3339603864734038"])
    total = 18
    print(f"{total - failures} of {total} self-tests pass")
    return failures


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    out = None
    if "--json" in argv:
        out = argv[argv.index("--json") + 1]
        argv = [a for i, a in enumerate(argv)
                if i not in (argv.index("--json"), argv.index("--json") + 1)]
    if "--all" in argv:
        paths = sorted({row[0] for row in hygiene.check_prose_precision(
            os.path.join(ROOT, "gpd", "results"))})
        argv = [a for a in argv if a != "--all"]
    else:
        paths = [a for a in argv if a.endswith(".json")]
    ref = "HEAD"
    if "--ref" in argv:
        ref = argv[argv.index("--ref") + 1]
    rows = [compare(path, ref) for path in paths]
    totals = {}
    for row in rows:
        for key, value in row.items():
            if isinstance(value, int):
                totals[key] = totals.get(key, 0) + value
    for row in sorted(rows, key=lambda r: r["file"]):
        moved = sum(v for k, v in row.items() if isinstance(v, int) and k != "movedTokens"
                    and not k.startswith("tokens"))
        if moved:
            print(f"  {row['file']}: " + ", ".join(
                f"{k}={v}" for k, v in row.items() if isinstance(v, int) and v))
    print(f"-- {len(rows)} file(s) --")
    for key in ("prose", "wording", "departure", "numeric", "boolean", "added", "removed",
                "movedTokens", "tokensExplained", "tokensUnexplained",
                "tokensBelowTheCensus", "tokenCountChanged"):
        print(f"   {key:20s} {totals.get(key, 0)}")
    if out:
        with open(out, "w") as handle:
            json.dump(dict(files=rows, totals=totals), handle, indent=2)
        print(f"written to {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
