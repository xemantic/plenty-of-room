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
"""Mutation coverage of `--prose`, the `T-249` line of `tools/check-result-file-hygiene.py`.

`C-0127`'s standard is that restoring the old narrow predicate must FAIL a **named** test, and
`C-0150` raised it: a predicate that can only ever be **widened** has stopped being a judgement
and become a pattern, so the tests must bite in **both** directions.  Six mutations are applied
to the `--prose` predicate, each one a plausible way of writing it, and each must fail at least
one named row of `PROSE_TESTS`.

The second measurement is the one a whole-predicate count cannot make: **is every row of the
table load-bearing?**  A table can carry a large failure count on two popular rows while the rest
are decoration.  So each row is also asked whether *some* mutation flips it; a row no mutation
reaches is a row that tests nothing.

    tools/T-249-mutation-test.py
"""

import importlib.util
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))


def _module():
    spec = importlib.util.spec_from_file_location(
        "hygiene", os.path.join(HERE, "check-result-file-hygiene.py")
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _digits_of(module, literal):
    return module.significant_digits(literal)


# Each mutation is `(name, predicate)`, where the predicate has the signature of
# `unrounded_numbers_in` and is written the way a plausible author would have written it.
def mutations(module):
    ceiling = module.RESULT_DIGITS
    pattern = module.PROSE_NUMBER

    def widened_threshold(text, digits=ceiling):
        # "round everything to six, like a solved height" -- fires on a compliant nine-digit number
        return [m for m in pattern.findall(text) if _digits_of(module, m) > 6]

    def narrowed_threshold(text, digits=ceiling):
        # "only a full seventeen-digit `toString` is a defect" -- the narrowing C-0127 forbids
        return [m for m in pattern.findall(text) if _digits_of(module, m) > 17]

    no_lookbehind = re.compile(r"(\d+\.\d+(?:[eE][+-]?\d+)?)(?!\w)(?!\.\d)")
    no_word_guard = re.compile(r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?!\.\d)")
    no_dotted_guard = re.compile(r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?!\w)")
    symmetric_guard = re.compile(r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?![\w.])")
    integers_too = re.compile(r"(?<![\w.])(\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)(?!\w)(?!\.\d)")

    def dropped_lookbehind(text, digits=ceiling):
        return [m for m in no_lookbehind.findall(text) if _digits_of(module, m) > ceiling]

    def dropped_word_guard(text, digits=ceiling):
        return [m for m in no_word_guard.findall(text) if _digits_of(module, m) > ceiling]

    def dropped_dotted_guard(text, digits=ceiling):
        return [m for m in no_dotted_guard.findall(text) if _digits_of(module, m) > ceiling]

    def symmetric_trailing_guard(text, digits=ceiling):
        # the FIRST DRAFT of this predicate, and the reason the table has an end-of-sentence row
        return [m for m in symmetric_guard.findall(text) if _digits_of(module, m) > ceiling]

    def integral_tokens_too(text, digits=ceiling):
        return [m for m in integers_too.findall(text) if _digits_of(module, m) > ceiling]

    naive = re.compile(r"\d[\d.]*")

    def no_threshold(text, digits=ceiling):
        # "any decimal in prose is a defect" -- forgets that nine digits IS the rule
        return pattern.findall(text)

    def naive_digit_run(text, digits=ceiling):
        # the first thing anybody writes: a run of digits and dots, no guards, no threshold
        return naive.findall(text)

    def length_not_significance(text, digits=ceiling):
        # counting CHARACTERS rather than significant digits -- the commonest way to get this
        # wrong, and it fires on trailing zeros, which carry no information
        return [m for m in pattern.findall(text) if len(m) > ceiling]

    return [
        ("threshold widened to six digits", widened_threshold),
        ("threshold narrowed to seventeen digits", narrowed_threshold),
        ("the leading lookaround dropped", dropped_lookbehind),
        ("the trailing word guard dropped", dropped_word_guard),
        ("the dotted-token guard dropped", dropped_dotted_guard),
        ("the symmetric trailing guard of the FIRST DRAFT restored", symmetric_trailing_guard),
        ("bare integers matched as well", integral_tokens_too),
        ("no digit threshold at all", no_threshold),
        ("a naive digit-run pattern, no guards and no threshold", naive_digit_run),
        ("character count instead of significant digits", length_not_significance),
    ]


def main():
    module = _module()
    tests = ([(text, count, description) for text, count, description in module.PROSE_TESTS]
             + [(text, tokens, description) for text, tokens, description
                in module.PROSE_TOKEN_TESTS])
    rows = mutations(module)
    print(f"-- T-249 mutation coverage of --prose, over {len(tests)} named tests "
          f"({len(module.PROSE_TESTS)} count rows + {len(module.PROSE_TOKEN_TESTS)} token rows) --")
    failures_by_test = {index: 0 for index in range(len(tests))}
    total_bad = 0
    for name, predicate in rows:
        failed = []
        for index, (text, expected, description) in enumerate(tests):
            found = predicate(text)
            actual = found if isinstance(expected, list) else len(found)
            if actual != expected:
                failed.append(description)
                failures_by_test[index] += 1
        if not failed:
            total_bad += 1
        print(f"  {len(failed):3d} named test(s) fail  <-  {name}")
        for description in failed:
            print(f"          {description}")
    silent = [tests[index][2] for index, count in failures_by_test.items() if count == 0]
    print(f"-- {len(tests) - len(silent)} of {len(tests)} rows are reached by some mutation --")
    for description in silent:
        print(f"     UNREACHED: {description}")
    if total_bad:
        print(f"MUTATION TEST FAILED — {total_bad} mutation(s) pass every named test")
    return 1 if total_bad else 0


if __name__ == "__main__":
    sys.exit(main())
