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
# T-281 -- every rule of the discharge registry, mutation-tested.
#
#     tools/T-281-mutation-test.py
#
# WHY.  `C-0127`'s standard is that restoring the old, narrow rule must fail a NAMED test;
# `C-0138`'s addition is that a predicate carrying exclusions has TWO directions and the widening
# one is never written; and `C-0177` measured the trap that makes a mutation table look full and
# be empty -- 9 of 22 rows of `C-0176`'s first table failed nothing, eight of them because the
# mutation WIDENED a rule to `original|mutant` instead of replacing it.  Every mutation here is a
# WHOLESALE TEXT REPLACEMENT in a throwaway copy of `tools/`, so the old rule is gone by
# construction.
#
# A mutation that fails NO named test is the finding, not a gap in the list (`C-0161`).
#
# The two named-test suites a mutation has to get past are `tools/census_discharges.py
# --self-test` (the REGISTRY, 45 tests) and `tools/T-234-census.py --self-test` (the CENSUS that
# consumes it, which runs the registry's tests too), and the killer counts are reported per suite
# because a rule only one of them reaches is worth knowing about.
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")

MODULE = "census_discharges.py"
CENSUS = "T-234-census.py"


def _live_block(basename, opening, closing="\n}"):
    """The block of `tools/<basename>` from `opening` to the next `closing`, verbatim.

    `T-300`/`C-0202` recorded the trap and repaired two anchors in its OWN harness; this is the
    third, in a harness that also transcribes `FAMILY_DISCHARGE` and which the ninth family
    (`FORWARD_BUDGET`) duly orphaned — `P-31` caught it as `ORPHAN ... occurs 0 times`.  Quoting
    the block from the file keeps the mutation's MEANING (replace this rule wholesale) while
    letting the map grow, and `main`'s exactly-once assertion still holds it honest.
    """
    source = open(os.path.join(TOOLS, basename), encoding="utf-8").read()
    start = source.index(opening)
    return source[start: source.index(closing, start) + len(closing)]

# (name, file, text replaced, text it is replaced BY).  The `old` text must occur EXACTLY once,
# which is asserted -- a mutation that silently fails to apply is a survivor for the wrong reason
# and is `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op*.
MUTATIONS = [
    # --- the getter.  Direction 1: it stops refusing (the exact defect `T-281` removes).
    (
        "the getter DEFAULTS an undeclared family to the subject -- the `CH-0229` defect restored",
        MODULE,
        '''        if family not in self.families:
            raise UndeclaredFamily(
                "family {!r} declares no discharge; {}".format(family, _REFUSAL)
            )
        return self.families[family]''',
        "        return self.families.get(family, self.subject)",
    ),
    # --- the getter.  Direction 2: it refuses a family that IS declared.
    (
        "the getter refuses a DECLARED family too, so the rule refuses correct work",
        MODULE,
        '        """The discharge `family` belongs to.  REFUSES an undeclared family; never'
        ' defaults."""\n        if family not in self.families:',
        '        """The discharge `family` belongs to.  REFUSES an undeclared family; never'
        ' defaults."""\n        if True:',
    ),
    (
        "a family declared `None` is refused, so `not a debt at all` stops being sayable",
        MODULE,
        '        """The discharge `family` belongs to.  REFUSES an undeclared family; never'
        ' defaults."""\n        if family not in self.families:',
        '        """The discharge `family` belongs to.  REFUSES an undeclared family; never'
        ' defaults."""\n        if family not in self.families or self.families[family] is None:',
    ),
    # --- the construction rules: a discharge must NAME a claim
    (
        "a discharge naming NO claim is accepted, so a date can stand with no claim behind it",
        MODULE,
        '''            if not tuple(pointers):
                raise UndeclaredDischarge(
                    "discharge {!r} names no claim; {}".format(name, _REFUSAL)
                )''',
        "            pass",
    ),
    (
        "the `None` discharge is allowed to name a claim, so `no discharge` and `this discharge` "
        "stop being distinguishable",
        MODULE,
        '''            if name is None:
                if tuple(pointers):
                    raise UndeclaredDischarge(
                        "the None discharge is the ABSENCE of one and may not name a claim, "
                        "and it names {!r}".format(tuple(pointers))
                    )
                continue''',
        "            if name is None:\n                continue",
    ),
    (
        "a family may name a discharge the registry does not define",
        MODULE,
        '''            if name not in self.discharges:
                raise UndeclaredDischarge(
                    "family {!r} names discharge {!r}, which this registry does not "
                    "define".format(family, name)
                )''',
        "            pass",
    ),
    (
        "the subject need not be a defined discharge, so the census's own date names no claim",
        MODULE,
        '''        if subject not in self.discharges:
            raise UndeclaredDischarge(
                "the subject discharge {!r} is not one of the discharges this registry "
                "defines".format(subject)
            )''',
        "        pass",
    ),
    # --- the REPORT half: it must not refuse, and it must carry three states
    (
        "the report RAISES on an undeclared family, so a report becomes a getter",
        MODULE,
        "                rows.append(FamilyVerdict(family, UNDECLARED, UNKNOWN, occurrences))",
        '                raise UndeclaredFamily("no")',
    ),
    (
        "an UNDECLARED row's discharge slot becomes `None`, colliding with `not a debt at all`",
        MODULE,
        "                rows.append(FamilyVerdict(family, UNDECLARED, UNKNOWN, occurrences))",
        "                rows.append(FamilyVerdict(family, UNDECLARED, None, occurrences))",
    ),
    (
        "an EMPTY DOMAIN is reported as UNDECLARED -- vacuous confused with withheld",
        MODULE,
        "                rows.append(FamilyVerdict(family, VACUOUS, self.families[family], "
        "occurrences))",
        "                rows.append(FamilyVerdict(family, UNDECLARED, UNKNOWN, occurrences))",
    ),
    (
        "a declared family with occurrences is reported VACUOUS regardless of the count",
        MODULE,
        "            elif occurrences:",
        "            elif False:",
    ),
    (
        "the report covers only the EMITTED families, so an empty domain is invisible rather than "
        "vacuous",
        MODULE,
        "        for family in sorted(set(counts) | set(self.families)):",
        "        for family in sorted(set(counts)):",
    ),
    (
        "the report is emitted in reverse order, so its output is not stable",
        MODULE,
        "        for family in sorted(set(counts) | set(self.families)):",
        "        for family in sorted(set(counts) | set(self.families), reverse=True):",
    ),
    (
        "`undeclared()` lists every family rather than the undeclared ones",
        MODULE,
        '        return [row.family for row in self.report(emitted) if row.verdict == UNDECLARED]',
        "        return [row.family for row in self.report(emitted)]",
    ),
    (
        "`gated()` returns every family, so another census's discharge is gated by this one",
        MODULE,
        "        return frozenset(f for f, name in self.families.items() if name == self.subject)",
        "        return frozenset(self.families)",
    ),
    (
        "an iterable of names stops being accepted, so a caller with no counts cannot report",
        MODULE,
        '        counts = dict(emitted) if hasattr(emitted, "items") else collections.Counter(emitted)',
        "        counts = dict(emitted)",
    ),
    # --- the refusal's own words: naming ONE claim is the easy half, asking whether it is one
    # claim is the half `CH-0229` says was missing
    (
        "the refusal stops asking `CH-0229`'s question, so the DISCOVERY half is dropped",
        MODULE,
        '''_REFUSAL = (
    "name the claim that discharges it, in this census's discharge map -- and check that it is ONE "
    "claim: a premise can be withdrawn in HALVES (CH-0229), and a family that spans two discharges "
    "belongs to two censuses"
)''',
        '_REFUSAL = "declare it"',
    ),
    (
        "the rendered VACUOUS line drops its explanation, so a clean reading looks like evidence",
        MODULE,
        '''        if row.verdict == VACUOUS:
            lines.append(
                "               -- an EMPTY DOMAIN: this family is declared and the census finds "
                "nothing of it, so the declaration is VACUOUS rather than withheld, and a clean "
                "reading here is evidence about nothing"
            )''',
        "        if False:\n            pass",
    ),
    (
        "the rendered UNDECLARED line drops the repair, so a refusal says only that it refused",
        MODULE,
        '''        if row.verdict == UNDECLARED:
            lines.append("               -- REFUSED: " + _REFUSAL)''',
        "        if False:\n            pass",
    ),
    (
        "the rendered line drops the discharge, so a report cannot be read for the date",
        MODULE,
        '''            "  {:<12} {:<10} {:>4} occurrence(s)  {}".format(
                row.family, row.verdict, row.occurrences, _discharge_text(row)
            )''',
        '''            "  {:<12} {:<10} {:>4} occurrence(s)".format(
                row.family, row.verdict, row.occurrences
            )''',
    ),
    # --- the CENSUS that consumes the registry
    (
        "the census's family map goes back to the PARTIAL one `C-0176` left",
        CENSUS,
        _live_block(CENSUS, "FAMILY_DISCHARGE = {"),
        '''FAMILY_DISCHARGE = {
    "GRILLAGE": "C-0154/C-0167",
    "SQUARE": None,
    "ROW_SPAN": None,
}''',
    ),
    (
        "the census bypasses the registry and defaults again",
        CENSUS,
        "    return REGISTRY.discharge_of(family)",
        "    return FAMILY_DISCHARGE.get(family, SUBJECT)",
    ),
    (
        "`emitted_families` stops respecting scope, so out-of-scope files enter the report",
        CENSUS,
        "        if not in_scope(path):\n            continue\n        try:\n            with open("
        "os.path.join(root, path), encoding=\"utf-8\") as handle:\n                text = "
        "handle.read()\n        except OSError:\n            continue\n        for family, _line, "
        "_offset, _token, _distance in occurrences(text):",
        "        try:\n            with open(os.path.join(root, path), encoding=\"utf-8\") as "
        "handle:\n                text = handle.read()\n        except OSError:\n            "
        "continue\n        for family, _line, _offset, _token, _distance in occurrences(text):",
    ),
    (
        "the census builds its records BEFORE reporting the families, so an undeclared family is a "
        "traceback instead of a defect",
        CENSUS,
        "    family_rows = REGISTRY.report(emitted_families(root))",
        "    records = census(root)\n    family_rows = REGISTRY.report(emitted_families(root))",
    ),
]


def _apply(directory, filename, old, new):
    path = os.path.join(directory, filename)
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    occurrences = text.count(old)
    if occurrences != 1:
        raise AssertionError(
            "anchor occurs %d times in %s: %r" % (occurrences, filename, old[:70])
        )
    with open(path, "w", encoding="utf-8") as handle:
        handle.write(text.replace(old, new))


def _failures(directory, argv):
    """The named tests a suite reports as failing, run in the mutated copy."""
    result = subprocess.run(
        [sys.executable] + argv, cwd=directory, capture_output=True, text=True, timeout=600
    )
    lines = [
        line.strip()
        for line in (result.stdout + result.stderr).splitlines()
        if line.startswith("FAIL ")
    ]
    if not lines and result.returncode != 0:
        # A mutation that makes a suite THROW is killed by whichever test reached it.
        tail = (result.stderr.strip().splitlines() or ["exit %d" % result.returncode])[-1]
        lines = ["FAIL (raised) %s" % tail]
    return lines


def _populate(directory):
    for source in os.listdir(TOOLS):
        if source.endswith(".py"):
            shutil.copy2(os.path.join(TOOLS, source), directory)
    shutil.copy2(os.path.join(TOOLS, "T-234-classification.json"), directory)


def _baseline():
    """The named tests that fail in an UNMUTATED copy, which must not be counted as killers.

    A copy of `tools/` in a scratch directory has no corpus beside it, so the census's own
    corpus-listing tests fail there whatever the mutation.  Counting them would make every
    census mutation look killed -- a mutation table that is full and empty, which is exactly the
    failure `C-0177` measured and this file exists to avoid.  So the baseline is measured and
    subtracted, and the subtraction is what makes a `killed` row mean something.
    """
    directory = tempfile.mkdtemp(prefix="T-281-baseline.")
    try:
        _populate(directory)
        return (
            set(_failures(directory, [MODULE, "--self-test"])),
            set(_failures(directory, [CENSUS, "--self-test"])),
        )
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    if argv:
        print("usage: T-281-mutation-test.py")
        return 2
    base_registry, base_census = _baseline()
    print(
        "# baseline in an unmutated copy: registry %d pre-existing failure(s), census %d "
        "(the census's corpus-listing tests, which have no corpus beside a scratch copy of "
        "tools/); these are SUBTRACTED from every killer count below"
        % (len(base_registry), len(base_census))
    )
    for line in sorted(base_census):
        print("#   %s" % line[:110])
    survivors = []
    for name, filename, old, new in MUTATIONS:
        directory = tempfile.mkdtemp(prefix="T-281-mutation.")
        try:
            _populate(directory)
            _apply(directory, filename, old, new)
            registry = [f for f in _failures(directory, [MODULE, "--self-test"])
                        if f not in base_registry]
            census = [f for f in _failures(directory, [CENSUS, "--self-test"])
                      if f not in base_census]
        finally:
            shutil.rmtree(directory, ignore_errors=True)
        if registry or census:
            print("killed  registry %2d  census %2d   %s" % (len(registry), len(census), name))
            for killer in (registry + census)[:2]:
                print("                             %s" % killer[:110])
        else:
            print("SURVIVED                     %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
