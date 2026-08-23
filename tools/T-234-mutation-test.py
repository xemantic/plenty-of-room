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
"""Mutation coverage of the `T-260`/`T-262` predicates, in BOTH directions.

`C-0127`'s standard is that restoring the old narrow predicate must FAIL a **named** test;
`C-0150` raised it, because a predicate that can only ever be **widened** has stopped being a
judgement and become a pattern.  So every mutation here is one of two kinds:

  NARROW  the predicate as it stood before `T-260`/`T-262`, or with one of its rules disabled --
          the state that produced the false positives those tasks exist to remove;
  WIDEN   the same rule swept in by pattern, which is how a judgement decays into a catch-all.

Each mutation is applied to the SOURCE of `tools/T-234-census.py` or
`tools/T-234-emit-classification.py`, the mutated module's own `--self-test` is run in process, and
the NAMED tests it fails are collected.  A mutation that fails nothing is a rule nothing asserts.

The second measurement is the one a whole-predicate count cannot make: **is every named test
load-bearing?**  A table can carry a large failure count on two popular rows while the rest are
decoration, so each row is also asked whether *some* mutation flips it.

    tools/T-234-mutation-test.py
"""

import ast
import importlib.util
import io
import os
import re
import sys
from contextlib import redirect_stdout

HERE = os.path.dirname(os.path.abspath(__file__))
CENSUS = os.path.join(HERE, "T-234-census.py")
EMITTER = os.path.join(HERE, "T-234-emit-classification.py")

NEVER = 'r"(?!x)x"'
ALWAYS = 'r""'


def _run_self_test(source, path, name):
    """Exec `source` as if it were `path`, run its `self_test`, return the NAMED failures."""
    module = importlib.util.module_from_spec(
        importlib.util.spec_from_loader(name, loader=None)
    )
    module.__file__ = path
    sys.modules[name] = module
    buffer = io.StringIO()
    try:
        with redirect_stdout(buffer):
            exec(compile(source, path, "exec"), module.__dict__)
            module.self_test()
    except Exception as error:                                  # a mutation may not even import
        return ["<raised {}: {}>".format(type(error).__name__, error)]
    finally:
        sys.modules.pop(name, None)
    return [
        line[len("FAIL  "):] for line in buffer.getvalue().splitlines()
        if line.startswith("FAIL  ")
    ]


def _named_tests(source):
    """Every `ok("...")` name in a self-test, in source order.

    Parsed rather than matched.  `T-285`: the regular expression this replaces captured only the
    FIRST string literal of a name, so every test whose name is written as adjacent literals across
    two source lines was recorded TRUNCATED -- while `_run_self_test` reports the whole,
    concatenated name.  The two then never compare equal, and the `UNREACHED` report below duly
    listed as unreached seven tests that a mutation had demonstrably killed.  `ast` folds implicit
    concatenation into one constant, which is the same thing the interpreter does at the call.
    """
    tree = ast.parse(source)
    found = []
    for node in ast.walk(tree):
        if (isinstance(node, ast.Call) and isinstance(node.func, ast.Name)
                and node.func.id == "ok" and node.args
                and isinstance(node.args[0], ast.Constant)
                and isinstance(node.args[0].value, str)):
            found.append((node.lineno, node.args[0].value))
    return [name for _line, name in sorted(found)]


def _self_check():
    """The name extractor is itself a predicate, and this is the case it used to get wrong."""
    sample = 'def f():\n    ok(\n        "a name split "\n        "across two lines",\n        True,\n    )\n'
    recovered = _named_tests(sample)
    if recovered != ["a name split across two lines"]:
        print("SELF-CHECK FAILED: _named_tests recovered %r" % (recovered,))
        return 1
    if _named_tests('def f():\n    ok("one", True)\n') != ["one"]:
        print("SELF-CHECK FAILED: _named_tests lost a single-literal name")
        return 1
    return 0


#: `(kind, name, path, substitutions)`.  A substitution that does not apply is a defect in this
#: table, not in the tool, so every one is asserted to have applied.
def mutations():
    """`(kind, name, path, substitutions)`.

    A substitution replaces a rule WHOLESALE.  The first draft of this table alternated the mutant
    with the original (`NEVER + "|" + original`), which is a no-op, and eight of its rows duly
    "failed nothing" -- a mutation table can indict itself before it indicts anything else.
    """
    structural = '''_STRUCTURAL_MODEL = re.compile(
    r"grillage|OrigamiGrillage|CrossoverLayout|smeared|coupled cell|lattice machinery"
    r"|crossover combinatorics|equivalent sheet|which results are",
    re.I,
)'''
    attributive = '''_ATTRIBUTIVE = re.compile(
    r"single-layer\\s+square-lattice\\s+(?:sheet|tile|number|design|question|d\\s*=)", re.I
)'''
    row_words = (
        '_ROW_WORDS = re.compile(r"\\bspans?\\b|\\bspanned\\b|\\brows?\\b|x-raster|per row'
        '|interface window", re.I)'
    )
    width_words = '''_WIDTH_WORDS = re.compile(
    r"\\bwidths?\\b|\\bextent\\b|footprint|edgeX|\\bnominal\\b|[×x]\\s*4\\b|\\bacross\\b|bounding box", re.I
)'''
    drawable = '''_DRAWABLE_RASTER = re.compile(
    r"102 . 109|drawable(?:\\s+\\S+){0,3}\\s+raster|drawable one|drawable pair|closing raster"
    r"|closes", re.I
)'''
    # `T-281` completed this map -- a family must NAME its discharge, so the five subject families
    # are declared rather than defaulted.  The mutation's MEANING is unchanged: put every family
    # back on one global pointer set.  Restoring the old three-entry anchor would now be a
    # different mutation (every subject family UNDECLARED), and the anchor would not match either.
    discharge_map = '''FAMILY_DISCHARGE = {
    "FOOTPRINT": SUBJECT,
    "WIDTH": SUBJECT,
    "AZIMUTH": SUBJECT,
    "SCAFFOLD": SUBJECT,
    "PLACEMENT": SUBJECT,
    "GRILLAGE": "C-0154/C-0167",
    "SQUARE": None,
    "ROW_SPAN": None,
}'''
    one_global_pointer_set = '''FAMILY_DISCHARGE = {
    "FOOTPRINT": SUBJECT,
    "WIDTH": SUBJECT,
    "AZIMUTH": SUBJECT,
    "SCAFFOLD": SUBJECT,
    "PLACEMENT": SUBJECT,
    "GRILLAGE": SUBJECT,
    "SQUARE": SUBJECT,
    "ROW_SPAN": SUBJECT,
}'''
    family_class = '''FAMILY_CLASS = {
    "GRILLAGE": "SURVIVING",
    "ROW_SPAN": "RESTATED",
    "SQUARE": "OUT_OF_SCOPE",
}'''
    _SLUG_ANCHOR = (
        'SLUG_FILENAME = re.compile('
        'r"\\b(?:CH|C|P|T|S)-\\d{1,4}[a-z]?-[A-Za-z0-9-]+\\.[A-Za-z0-9]{1,5}\\b")'
    )
    _ORDER_ANCHOR = '''_ID_PATTERNS = [
    SLUG_FILENAME,
    re.compile(r"\\b(?:CH|C|P|T|S)-\\d{1,4}[a-z]?\\b"),
]'''
    return [
        # ------------------------------------------------------------------ T-260, narrowing
        ("NARROW", "the PLACEMENT refinement removed (the iteration-34 predicate)", CENSUS,
         [("        refine_placement,\n    ),", "        None,\n    ),")]),
        ("NARROW", "the structural-model test never fires -- no GRILLAGE half", CENSUS,
         [(structural, "_STRUCTURAL_MODEL = re.compile(" + NEVER + ")")]),
        ("NARROW", "the attributive test never fires -- no SQUARE reading", CENSUS,
         [(attributive, "_ATTRIBUTIVE = re.compile(" + NEVER + ")")]),
        ("NARROW", "one global pointer set again -- no per-family discharge", CENSUS,
         [(discharge_map, one_global_pointer_set)]),
        ("NARROW", "the refinement window shrunk to nothing", CENSUS,
         [("REFINE_WINDOW = 300", "REFINE_WINDOW = 0")]),
        # ------------------------------------------------------------------ T-260, widening
        ("WIDEN", "the structural-model test fires on anything", CENSUS,
         [(structural, "_STRUCTURAL_MODEL = re.compile(" + ALWAYS + ")")]),
        ("WIDEN", "the attributive test fires on the bare token", CENSUS,
         [(attributive,
           '_ATTRIBUTIVE = re.compile(r"single-layer\\s+square-lattice", re.I)')]),
        ("WIDEN", "the refinement window swallows the whole document", CENSUS,
         [("REFINE_WINDOW = 300", "REFINE_WINDOW = 1000000")]),
        ("WIDEN", "the structural-model test gets the WIDE window back -- a sentence away counts",
         CENSUS, [("STRUCTURAL_WINDOW = 120", "STRUCTURAL_WINDOW = REFINE_WINDOW")]),
        ("NARROW", "the structural-model test sees only the token itself", CENSUS,
         [("STRUCTURAL_WINDOW = 120", "STRUCTURAL_WINDOW = 0")]),
        # ------------------------------------------------------------------ T-262, narrowing
        ("NARROW", "the WIDTH refinement removed (the iteration-34 predicate)", CENSUS,
         [("_HONEYCOMB, refine_width)", "_HONEYCOMB, None)")]),
        ("NARROW", "the drawable-raster test never fires -- every `drawable` is a width", CENSUS,
         [(drawable, "_DRAWABLE_RASTER = re.compile(" + NEVER + ")")]),
        ("NARROW", "row nouns never match -- every row length reads as a tile width", CENSUS,
         [(row_words, "_ROW_WORDS = re.compile(" + NEVER + ")")]),
        ("NARROW", "nothing is ever remote -- the line context is trusted at any distance", CENSUS,
         [("CONTEXT_REMOTE = 1000", "CONTEXT_REMOTE = 10 ** 9")]),
        # ------------------------------------------------------------------ T-287
        ("NARROW", "the line context is read from the ORIGINAL line -- a filename supplies it "
                   "again", CENSUS,
         [('    lines = hunted.split("\\n")', '    lines = text.split("\\n")')]),
        ("NARROW", "the context DISTANCE is measured on the unblanked line, so the diagnostic "
                   "contradicts the rule it diagnoses", CENSUS,
         [("    return _nearest(re.compile(context, re.I), blank_identifiers(line), "
           "start - line_start)",
           "    return _nearest(re.compile(context, re.I), line, start - line_start)")]),
        # ------------------------------------------------------------------ T-293
        ("NARROW", "the REFINEMENT window is read from the ORIGINAL text -- a filename decides "
                   "which of two discharges a token takes again", CENSUS,
         [("            name = refine(hunted, match.start(), match.end()) if refine else family",
           "            name = refine(text, match.start(), match.end()) if refine else family")]),
        # ------------------------------------------------------------------ T-262, widening
        ("WIDEN", "row nouns match anything -- every match is a restored row span", CENSUS,
         [(row_words, "_ROW_WORDS = re.compile(" + ALWAYS + ")")]),
        ("WIDEN", "width nouns match anything -- every match is a withdrawn tile width", CENSUS,
         [(width_words, "_WIDTH_WORDS = re.compile(" + ALWAYS + ")")]),
        ("WIDEN", "the drawable-raster test fires on anything", CENSUS,
         [(drawable, "_DRAWABLE_RASTER = re.compile(" + ALWAYS + ")")]),
        ("WIDEN", "everything is remote -- the line context is never trusted", CENSUS,
         [("CONTEXT_REMOTE = 1000", "CONTEXT_REMOTE = 0")]),
        ("WIDEN", "a snippet is the whole line again -- an occurrence loses its identity", CENSUS,
         [("SNIPPET_CHARS = 40", "SNIPPET_CHARS = 100000")]),
        ("NARROW", "a snippet is empty -- an occurrence has no identity at all", CENSUS,
         [("SNIPPET_CHARS = 40", "SNIPPET_CHARS = 0")]),
        # ------------------------------------------------------------------ the emitter
        ("NARROW", "the class coercion removed -- the two-way MOVED/DISCHARGED default", EMITTER,
         [(family_class, "FAMILY_CLASS = {}")]),
        ("NARROW", "hand overrides are not read back -- the docstring's standing promise", EMITTER,
         [('            if not entry.get("byHand"):\n                continue',
           "            if True:\n                continue")]),
        ("NARROW", "`C-0152` and `C-0154` registered as CORRECTING again", EMITTER,
         [('    "gpd/claims/C-0153-unrounded-prose-interpolations.md",\n',
           '    "gpd/claims/C-0153-unrounded-prose-interpolations.md",\n'
           '    "gpd/claims/C-0152-forced-scaffold-crossover-price.md",\n'
           '    "gpd/claims/C-0154-honeycomb-grillage.md",\n')]),
        ("NARROW", "the two-file OUT_OF_SCOPE set restored", EMITTER,
         [("OUT_OF_SCOPE_FILES = set()",
           'OUT_OF_SCOPE_FILES = {"gpd/claims/C-0081-seam-weave-congruence.md",\n'
           '                      "gpd/claims/C-0127-format-string-repair.md"}')]),
        ("WIDEN", "the coercion sweeps the gated families in too", EMITTER,
         [(family_class, family_class[:-1]
           + '    "WIDTH": "RESTATED",\n    "PLACEMENT": "SURVIVING",\n}')]),
        ("WIDEN", "a hand override is keyed on its FILE alone", EMITTER,
         [('        entry_or_record.get("family"),\n        entry_or_record.get("token"),',
           '        None,\n        None,')]),
        # ------------------------------------- the vocabulary and the two-layer agreement itself
        ("WIDEN", "ADDRESSED gains the two new classes -- the split gated after all", CENSUS,
         [('ADDRESSED = ("MOVED", "DISCHARGED")',
           'ADDRESSED = ("MOVED", "DISCHARGED", "SURVIVING", "RESTATED")')]),
        ("NARROW", "CLASSES without SURVIVING and RESTATED -- the old vocabulary", CENSUS,
         [('CLASSES = (\n    "MOVED", "DISCHARGED", "RECORD", "CORRECT", "OUT_OF_SCOPE",'
           ' "SURVIVING", "RESTATED",\n)',
           'CLASSES = ("MOVED", "DISCHARGED", "RECORD", "CORRECT", "OUT_OF_SCOPE")')]),
        ("NARROW", "NON_SUBJECT_CLASSES back to the three old ones", CENSUS,
         [('NON_SUBJECT_CLASSES = ("SURVIVING", "RESTATED", "RECORD", "CORRECT", "OUT_OF_SCOPE")',
           'NON_SUBJECT_CLASSES = ("RECORD", "CORRECT", "OUT_OF_SCOPE")')]),
        ("WIDEN", "every family belongs to this census's own discharge", CENSUS,
         [('    "GRILLAGE": "C-0154/C-0167",\n    "SQUARE": None,\n    "ROW_SPAN": None,',
           '    "GRILLAGE": SUBJECT,\n    "SQUARE": SUBJECT,\n    "ROW_SPAN": SUBJECT,')]),
        ("NARROW", "the two discharges merged into one pointer set", CENSUS,
         [('    "C-0140", "C-0141", "CH-0172",', '    "C-0140", "C-0141", "C-0154", "CH-0172",')]),
        # `T-281` replaced `FAMILY_DISCHARGE.get(family, SUBJECT)` by a registry that REFUSES an
        # undeclared family, so the old anchor no longer exists -- there is no default left to
        # change.  The mutation's intent survives as *swallow the refusal*, which is the same
        # widening expressed against the new rule.
        ("WIDEN", "a family with no entry belongs to NO discharge -- the refusal swallowed", CENSUS,
         [("    return REGISTRY.discharge_of(family)",
           "    try:\n        return REGISTRY.discharge_of(family)\n"
           "    except Exception:\n        return None")]),
        ("NARROW", "the WIDTH family loses its honeycomb line context", CENSUS,
         [("_HONEYCOMB, refine_width)", "None, refine_width)")]),
        ("NARROW", "the subject side of the two-layer agreement removed", CENSUS,
         [("        if owner == SUBJECT and entry[\"class\"] not in ADDRESSED"
           " + NON_SUBJECT_CLASSES[2:]:", "        if False:")]),
        ("NARROW", "a snippet is a LINE PREFIX again, not a window on the token", CENSUS,
         [("    window = text[max(0, start - SNIPPET_CHARS): start + len(token) + SNIPPET_CHARS]",
           "    window = text[:2 * SNIPPET_CHARS + len(token)]")]),
        ("WIDEN", "every previous entry is treated as a hand override", EMITTER,
         [('            if not entry.get("byHand"):\n                continue', "            pass")]),
        ("NARROW", "a hand override is keyed on its family and token, not its file", EMITTER,
         [('        entry_or_record.get("file"),\n        entry_or_record.get("family"),',
           '        None,\n        entry_or_record.get("family"),')]),
        ("WIDEN", "the coercion applies to every class, not only the gated ones", EMITTER,
         [("    if cls in census.ADDRESSED and family in FAMILY_CLASS:",
           "    if family in FAMILY_CLASS:")]),
        ("NARROW", "WHY loses the two new reasons", EMITTER,
         [('    "SURVIVING": "the half of a PARTIALLY discharged premise C-0141 did NOT supply',
           '    "SURVIVING": "",\n    "UNUSED": "the half of a PARTIALLY discharged premise')]),
        ("NARROW", "a hand override is keyed WITHOUT the occurrence's neighbourhood", EMITTER,
         [('        (entry_or_record.get("snippet") or "")[:OVERRIDE_KEY_CHARS],', "        None,")]),
        ("NARROW", "CORRECTING emptied", EMITTER,
         [('CORRECTING = {\n    "gpd/claims/C-0140-honeycomb-raster-turn-sense.md",',
           'CORRECTING = {\n    "unused.md",')]),
        ("NARROW", "the override key shortened below one occurrence's neighbourhood", EMITTER,
         [("OVERRIDE_KEY_CHARS = 100", "OVERRIDE_KEY_CHARS = 4")]),
        # ------------------------------------------------------- T-285, narrowing: the defect back
        # The rule is a WIDENING of an exclusion -- a file NAMED by an identifier is blanked before
        # the families are matched -- so its two directions are: take the widening away, and take
        # it too far.  A silencer is the worse failure of the two.
        ("NARROW", "the filename rule removed -- the pre-T-285 predicate, and a slug fires again",
         CENSUS, [(_ORDER_ANCHOR, _ORDER_ANCHOR.replace("    SLUG_FILENAME,\n", ""))]),
        ("NARROW", "the filename rule runs AFTER the bare identifier, which eats its own prefix "
                   "and leaves the slug behind", CENSUS,
         [(_ORDER_ANCHOR, '''_ID_PATTERNS = [
    re.compile(r"\\b(?:CH|C|P|T|S)-\\d{1,4}[a-z]?\\b"),
    SLUG_FILENAME,
]''')]),
        ("NARROW", "the extension is `.md` alone -- a RESULT filename's token survives", CENSUS,
         [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace(r"\.[A-Za-z0-9]{1,5}", r"\.md"))]),
        ("NARROW", "the slug charset drops the hyphen -- a multi-word slug is not a filename",
         CENSUS, [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace("[A-Za-z0-9-]+", "[A-Za-z0-9]+"))]),
        ("NARROW", "the sub-letter is dropped -- a `T-1d` file is not a filename", CENSUS,
         [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace(r"\d{1,4}[a-z]?-", r"\d{1,4}-"))]),
        ("NARROW", "the blanking stops preserving length -- every offset below a span is wrong",
         CENSUS,
         [('        out = pattern.sub(lambda m: " " * (m.end() - m.start()), out)',
           '        out = pattern.sub("", out)')]),
        # ------------------------------------------------------ T-285, widening: a silencer, not a
        # blanking.  `CLAUDE.md`: a drift checker's false positives cost more than its true ones.
        ("WIDEN", "the extension is not required -- every hyphenated identifier phrase is blanked",
         CENSUS, [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace(r"\.[A-Za-z0-9]{1,5}", ""))]),
        ("WIDEN", "the slug admits whitespace -- the blanking runs from one filename to a LATER "
                  "full stop and swallows the prose between", CENSUS,
         [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace("[A-Za-z0-9-]+", "[A-Za-z0-9 -]+"))]),
        ("WIDEN", "the slug admits whitespace AND the full stop -- one filename then reaches "
                  "the next decimal point in the sentence behind it", CENSUS,
         [(_SLUG_ANCHOR, _SLUG_ANCHOR.replace("[A-Za-z0-9-]+", "[A-Za-z0-9 .-]+"))]),
        ("WIDEN", "the identifier prefix is not required -- any dotted token is a filename, "
                  "a decimal number included", CENSUS,
         [(_SLUG_ANCHOR,
           'SLUG_FILENAME = re.compile(r"\\b[A-Za-z0-9-]+\\.[A-Za-z0-9]{1,5}\\b")')]),
        ("NARROW", "the BARE identifier rule removed -- the filename rule alone, so `T-132` is a "
                   "132-station census again", CENSUS,
         [(_ORDER_ANCHOR, """_ID_PATTERNS = [
    SLUG_FILENAME,
]""")]),
        ("WIDEN", "the rule decays into a CATCH-ALL: an identifier and everything hyphenated, "
                  "spaced or dotted behind it -- C-0150's judgement-becomes-a-pattern", CENSUS,
         [(_SLUG_ANCHOR,
           'SLUG_FILENAME = re.compile(r"\\b(?:CH|C|P|T|S)-\\d{1,4}[a-z]?[- .A-Za-z0-9]+")')]),
        # `T-285` carried this WIDENING here -- *the blanking applied to the line context as
        # well as the match* -- as the boundary of its own stated scope.  `T-287` took that
        # widening, so it is now the BEHAVIOUR and a mutation of it is a no-op; the harness said
        # so, reporting it as the run's only row failing nothing.  It is replaced by the two
        # NARROWINGS above, which restore the original-line reading of the context and of the
        # distance.  A mutation table is dated by the predicate it mutates.
    ]



def main():
    if _self_check():
        return 1
    sources = {path: open(path, encoding="utf-8").read() for path in (CENSUS, EMITTER)}
    names = {path: _named_tests(source) for path, source in sources.items()}
    baseline = {
        path: _run_self_test(source, path, "mutant_base_" + os.path.basename(path))
        for path, source in sources.items()
    }
    print("-- T-234 mutation coverage, {} mutations over {} named tests ({} census + {} emitter) --"
          .format(len(mutations()), sum(len(v) for v in names.values()),
                  len(names[CENSUS]), len(names[EMITTER])))
    for path, failures in baseline.items():
        if failures:
            print("BASELINE FAILS on {}: {}".format(os.path.basename(path), failures))
            return 1
    reached = set()
    silent = []
    for index, (kind, name, path, subs) in enumerate(mutations()):
        source = sources[path]
        for old, new in subs:
            if source.count(old) != 1:
                print("BROKEN MUTATION {!r}: anchor {!r} occurs {} times"
                      .format(name, old[:48], source.count(old)))
                return 1
            source = source.replace(old, new)
        failed = _run_self_test(source, path, "mutant_{}".format(index))
        reached.update(failed)
        if not failed:
            silent.append(name)
        print("{:<7} {:<66} fails {:>2}  {}".format(
            kind, name[:66], len(failed), "; ".join(sorted(failed)[:2]) or "NOTHING"))
    # Scope the load-bearing measurement to the tests THIS task added: every emitter test (the
    # emitter had none before) and the census tests these two tasks named.  The census's other 60
    # assertions cover rules no mutation here touches, so listing them as "unreached" would be a
    # measurement of the wrong table -- `CLAUDE.md`'s *ask what a percentage is a percentage OF*.
    scoped = [n for n in names[CENSUS] if n.startswith(("T-260 ", "T-262 "))] + names[EMITTER]
    unreached = [n for n in scoped if n not in reached]
    others = [n for group in names.values() for n in group if n not in scoped]
    print()
    print("named tests added by T-260/T-262: {}; reached by at least one mutation: {}"
          .format(len(scoped), len(scoped) - len(unreached)))
    print("named tests this table does not target (other rules of the same tools): {}"
          .format(len(others)))
    for name in unreached:
        print("  UNREACHED  {}".format(name))
    print("mutations failing NOTHING: {}".format(len(silent)))
    for name in silent:
        print("  SILENT  {}".format(name))
    # The exit code turns on SILENT MUTATIONS -- a rule nothing asserts.  An unreached test is a
    # measurement of this table rather than of the tool, so it is reported and not gated: a
    # limiting case ("an empty table carries nothing") is reached by no plausible mutation of the
    # shipped logic, and inventing one to close the count would be writing a test for a test.
    return 1 if silent else 0


if __name__ == "__main__":
    raise SystemExit(main())
