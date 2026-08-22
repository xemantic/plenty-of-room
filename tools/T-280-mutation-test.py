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
# T-280 -- every rule of the advisory line's RATIO, mutated WHOLESALE and in both directions.
#
#     tools/T-280-mutation-test.py
#
# WHY WHOLESALE. `C-0176`'s own first mutation table had **9 of 22 rows failing nothing**, and
# eight of the nine were the table rather than the tool: each mutation had been written as an
# ALTERNATION with the original, which is a no-op because the original still matches everything it
# used to. Every substitution below REPLACES a rule; none widens one to `original|mutant`.
#
# WHY BOTH DIRECTIONS. A ratio has two rules that can be wrong in opposite ways -- what enters the
# NUMERATOR and what enters the DENOMINATOR -- and `C-0150`'s addition to `C-0127` is that the
# second direction is the one that never gets written. A mutation that fails NO named test is the
# finding, not a gap in the list (`C-0161`).
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CENSUS = os.path.join(ROOT, "tools", "T-234-census.py")


def _named_tests(source, module_name):
    """Run a mutated source's own `--self-test` in process; return the NAMED tests it fails."""
    import importlib.util
    import io
    import contextlib
    import types

    module = types.ModuleType(module_name)
    module.__file__ = CENSUS
    module.__dict__["__name__"] = module_name
    buffer = io.StringIO()
    try:
        with contextlib.redirect_stdout(buffer):
            exec(compile(source, CENSUS, "exec"), module.__dict__)
            module.self_test()
    except Exception as failure:  # a mutation that makes a named test THROW is killed by it
        return ["RAISED {}: {}".format(type(failure).__name__, failure)]
    finally:
        del importlib
    return [line[len("FAIL  "):] for line in buffer.getvalue().splitlines()
            if line.startswith("FAIL  ")]


def mutations():
    """(direction, name, [(old, new)]).  Each substitution must occur EXACTLY once in the source."""
    return [
        # ---------------------------------------------------------------- the NUMERATOR's rules
        ("WIDEN", "the numerator counts every class, not only the two this census gates",
         [('        if r.get("class") in ADDRESSED\n', "        if True\n")]),
        ("WIDEN", "the numerator counts a POINTED occurrence, so a repair never leaves it",
         [('        and not r.get("pointer") and not r.get("struck")'
           ' and not r.get("headlinePointer")',
           '        and not r.get("struck") and not r.get("headlinePointer")')]),
        ("WIDEN", "the numerator counts a STRUCK occurrence",
         [('        and not r.get("pointer") and not r.get("struck")'
           ' and not r.get("headlinePointer")',
           '        and not r.get("pointer") and not r.get("headlinePointer")')]),
        ("WIDEN", "the numerator ignores a HEADLINE pointer",
         [('        and not r.get("pointer") and not r.get("struck")'
           ' and not r.get("headlinePointer")',
           '        and not r.get("pointer") and not r.get("struck")')]),
        ("WIDEN", "the numerator counts a family belonging to ANOTHER discharge",
         [('    same = [r for r in deliverables if r.get("discharge") == SUBJECT]',
           "    same = list(deliverables)")]),
        # ------------------------------------------------------------- the DENOMINATOR's rules
        ("NARROW", "the all-family denominator is narrowed to CH-0230's same-family one",
         [('        "ratioOverAllFamilies": len(unpointed) / len(deliverables) if deliverables'
           " else None,",
           '        "ratioOverAllFamilies": len(unpointed) / len(same) if same else None,')]),
        ("WIDEN", "the same-family denominator is widened to every family, so the two coincide",
         [('        "ratioOverTheSameFamilies": len(unpointed) / len(same) if same else None,',
           '        "ratioOverTheSameFamilies":'
           " len(unpointed) / len(deliverables) if deliverables else None,")]),
        ("NARROW", "the denominator drops the struck and pointed occurrences, so it IS the count",
         [('    deliverables = [r for r in records if r.get("deliverable")]',
           '    deliverables = [r for r in records if r.get("deliverable")\n'
           '                    and not r.get("pointer") and not r.get("struck")]')]),
        ("WIDEN", "the denominator leaves the two deliverables and counts the whole corpus",
         [('    deliverables = [r for r in records if r.get("deliverable")]',
           "    deliverables = list(records)")]),
        ("NARROW", "an empty denominator is a zero ratio rather than an absent one",
         [('        "ratioOverAllFamilies": len(unpointed) / len(deliverables) if deliverables'
           " else None,",
           '        "ratioOverAllFamilies":'
           " len(unpointed) / len(deliverables) if deliverables else 0.0,")]),
        # ------------------------------------------------------------------ the OUTPUT's rules
        ("NARROW", "the report drops the denominator's name, leaving the number unattributed",
         [('        "  denominator: " + DEBT_DENOMINATOR,', '        "  denominator: named",')]),
        ("NARROW", "the report drops CH-0230's own reading, so only the flattering one is printed",
         [('        "  CH-0230\'s own reading -- {} -- is {} of {} = {}, and that is the reading'
           ' which does NOT"\n        " fall when the documents are corrected".format(\n'
           "            DEBT_DENOMINATOR_NAMED_BY_CH0230,\n"
           '            debt["unpointed"], debt["sameFamilyOccurrences"],\n'
           '            ratio_text(debt["ratioOverTheSameFamilies"]),\n        ),\n', "")]),
        ("NARROW", "the report drops the count and prints the ratio alone",
         [('        "T-233 debt {} of {} occurrence(s) = {} in the two deliverables, which this'
           ' task does NOT"\n        " edit".format(\n'
           '            debt["unpointed"], debt["allFamilyOccurrences"],\n',
           '        "T-233 debt = {} in the two deliverables, which this task does NOT"\n'
           '        " edit".format(\n')]),
        ("NARROW", "the report drops the standing sentence that the count is not a debt measure",
         [('        "  -- and the COUNT alone is NOT a measure of debt.',
           '        "  -- the count.')]),
        ("WIDEN", "an empty census reports a ratio anyway, dividing by nothing",
         [('    if not debt["allFamilyOccurrences"]:\n        return [\n'
           '            "T-233 debt no occurrence of any family in the two deliverables, so there'
           ' is no ratio"\n            " to quote"\n        ]\n', "")]),
        ("NARROW", "the ratio is rendered at TWO significant digits, not nine",
         [("DEBT_RATIO_DIGITS = 9", "DEBT_RATIO_DIGITS = 2")]),
        ("WIDEN", "an absent ratio renders as a number rather than as `null`",
         [('    if value is None:\n        return "null"\n',
           "    if value is None:\n        return \"0\"\n")]),
        ("NARROW", "the numerator demands a pointer instead of refusing one",
         [('        and not r.get("pointer") and not r.get("struck")'
           ' and not r.get("headlinePointer")',
           '        and r.get("pointer") and not r.get("struck")'
           ' and not r.get("headlinePointer")')]),
        ("NARROW", "the denominator counts only the classes this census gates, so a RECORD and a "
                   "RESTATED sentence stop counting as the corpus discussing the premise",
         [('    deliverables = [r for r in records if r.get("deliverable")]',
           '    deliverables = [r for r in records if r.get("deliverable")\n'
           '                    and r.get("class") in ADDRESSED]')]),
        ("NARROW", "the same-family denominator keeps only the pointed occurrences, so the "
                   "numerator can exceed it",
         [('    same = [r for r in deliverables if r.get("discharge") == SUBJECT]',
           '    same = [r for r in deliverables\n'
           '            if r.get("discharge") == SUBJECT and r.get("pointer")]')]),
        ("WIDEN", "a claim file joins the deliverables, so a claim's own worked examples enter "
                  "the line and C-0176's two readings stop coinciding",
         [('DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")',
           'DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md",\n'
           '                "gpd/claims/C-0176-partial-discharge-and-restatement-predicates.md")')]),
        ("NARROW", "the two denominators are given ONE name, so the reading being quoted cannot "
                   "be told from the output",
         [("DEBT_DENOMINATOR_NAMED_BY_CH0230 = (\n"
           '    "every occurrence of the same families -- the ones this census gates, and only'
           ' those"\n)',
           "DEBT_DENOMINATOR_NAMED_BY_CH0230 = DEBT_DENOMINATOR")]),
        ("NARROW", "the denominator name no longer says EVERY family",
         [('"every occurrence this census finds in the two deliverables, of every family --'
           ' the families "',
           '"every occurrence this census finds in the two deliverables, of the gated ones --'
           ' the families "')]),
    ]


def main():
    source = open(CENSUS, encoding="utf-8").read()
    baseline = _named_tests(source, "t280_base")
    print("-- T-280 mutation coverage, {} mutations over the census's own named tests --"
          .format(len(mutations())))
    if baseline:
        print("BASELINE FAILS: {}".format(baseline))
        return 1
    silent = []
    reached = set()
    for index, (kind, name, subs) in enumerate(mutations()):
        mutated = source
        for old, new in subs:
            if mutated.count(old) != 1:
                print("BROKEN MUTATION {!r}: anchor occurs {} times".format(
                    name[:60], mutated.count(old)))
                return 1
            mutated = mutated.replace(old, new)
        if mutated == source:
            print("BROKEN MUTATION {!r}: it is a no-op".format(name[:60]))
            return 1
        failed = _named_tests(mutated, "t280_mutant_{}".format(index))
        reached.update(failed)
        if not failed:
            silent.append(name)
        print("{:<7} {:<70} fails {:>2}  {}".format(
            kind, name[:70], len(failed), "; ".join(sorted(failed)[:1]) or "NOTHING"))
    added = [n for n in _test_names(source) if n.startswith("T-280 ")]
    unreached = [n for n in added if n not in reached]
    print()
    print("named tests added by T-280: {}; reached by at least one mutation: {}"
          .format(len(added), len(added) - len(unreached)))
    for name in unreached:
        print("  UNREACHED  {}".format(name))
    print("mutations failing NOTHING: {}".format(len(silent)))
    for name in silent:
        print("  SILENT  {}".format(name))
    # The exit code turns on SILENT MUTATIONS -- a rule nothing asserts. An unreached test is a
    # measurement of this TABLE rather than of the tool, so it is reported and not gated.
    return 1 if silent else 0


def _test_names(source):
    """Every named test in the source, by PARSING rather than by matching.

    A regular expression over the first string literal reads a name written as adjacent literals --
    which is how a long name is written under this project's line-break rule -- as its first
    fragment only, and the coverage table then reports a test as UNREACHED that a mutation plainly
    killed. Python's own parser concatenates adjacent literals into one constant, so `ast` gets the
    whole name for nothing.
    """
    import ast
    names = []
    for node in ast.walk(ast.parse(source)):
        if not isinstance(node, ast.Call) or getattr(node.func, "id", None) != "ok":
            continue
        if node.args and isinstance(node.args[0], ast.Constant) and \
                isinstance(node.args[0].value, str):
            names.append(node.args[0].value)
    return names


if __name__ == "__main__":
    sys.exit(main())
