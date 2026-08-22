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
# T-281 -- a census must NAME the claim that discharges each family, and REFUSE a family whose
# discharge it cannot name.
#
#     tools/census_discharges.py --self-test
#
# WHY THIS EXISTS.  `CH-0182` established that a census is dated by its PREMISE SET.  `CH-0229` is
# the same observation on the other axis: a census is dated by its DISCHARGE, and a discharge can
# be PARTIAL -- `C-0141` supplied the honeycomb station lattice, the plan ceiling and the placement
# family and did NOT supply a grillage, which `C-0154`/`C-0167` supplied two iterations later.  One
# token therefore carries two statements with two correcting claims and TWO DATES, and the census
# inherited the earlier one.
#
# `C-0176` supplied the REPRESENTATION -- the second date can now be written down -- and explicitly
# not the DISCOVERY.  What was left was one line:
#
#     def discharge_of(family):
#         return FAMILY_DISCHARGE.get(family, SUBJECT)
#
# A family absent from the map silently becomes a debt of this census's own subject: AN ABSENCE IS
# READ AS AN ANSWER.  At the time this module was written `tools/T-234-census.py` emitted EIGHT
# family names and declared THREE, so five of eight were answered by a default nobody wrote down --
# and the two that later needed a split, `PLACEMENT` and `WIDTH`, were among them for eight
# iterations.
#
# `CLAUDE.md` records FOUR times that a convention is not a mechanism, and once that a convention
# written down TWICE still drifted.  So this is a rule that FAILS, not a sentence: a registry that
# cannot be CONSTRUCTED with a discharge naming no claim, and cannot be ASKED about a family naming
# no discharge.
#
# THE GETTER/REPORT SPLIT is `CLAUDE.md`'s own, read twice.  *A getter must refuse and a report must
# not*, and *a report then needs a THIRD verdict state* -- an empty violation list on an
# unanswerable family is indistinguishable from a clean one.  `discharge_of` raises; `report`
# returns `DECLARED`, `VACUOUS` or `UNDECLARED` and never raises.  The other half of the same entry
# is the trap on the far side: *a rule with an EMPTY DOMAIN is VACUOUS, not withheld*.  A family
# declared and matching nothing is CLEAN, and reporting it as unanswerable would make a correct
# declaration look like a defect -- the direction that gets a gate switched off.
import collections
import sys


class UndeclaredFamily(LookupError):
    """A family was asked about whose discharge nobody has named.  `CH-0229`."""


class UndeclaredDischarge(ValueError):
    """A discharge was named that names no claim, or that the registry does not define."""


class _Unknown(object):
    """The discharge slot of an UNDECLARED report row.

    NOT `None`: `None` is a legitimate declared discharge -- *this family is not a debt at all* --
    and a report in which *"nobody said"* and *"somebody said: none"* render alike is the very
    conflation this module exists to remove.
    """

    def __repr__(self):
        return "<undeclared>"


UNKNOWN = _Unknown()

DECLARED = "DECLARED"
VACUOUS = "VACUOUS"
UNDECLARED = "UNDECLARED"
#: The report's three states, and there are exactly three.
VERDICTS = (DECLARED, VACUOUS, UNDECLARED)

FamilyVerdict = collections.namedtuple(
    "FamilyVerdict", ("family", "verdict", "discharge", "occurrences")
)

#: What a refusal says.  A refusal that does not say what to do is a traceback with a nicer name --
#: and the sentence has to carry `CH-0229`'s question, because naming ONE claim is the easy half and
#: asking whether it is one claim is the half that was missing.
_REFUSAL = (
    "name the claim that discharges it, in this census's discharge map -- and check that it is ONE "
    "claim: a premise can be withdrawn in HALVES (CH-0229), and a family that spans two discharges "
    "belongs to two censuses"
)


class DischargeRegistry(object):
    """Which correction each family of a census belongs to, declared rather than defaulted.

    `subject`     the discharge THIS census is about.
    `discharges`  {name: (pointer, ...)}.  A non-`None` name must name at least one claim or
                  challenge; the `None` name is the explicit *not a debt at all* and names none.
    `families`    {family: discharge name}.  EVERY family the census can emit belongs here,
                  including the ones belonging to the subject -- that completeness is the rule.
    """

    def __init__(self, subject, discharges, families):
        self.discharges = dict(discharges)
        self.families = dict(families)
        for name, pointers in sorted(self.discharges.items(), key=lambda kv: str(kv[0])):
            if name is None:
                if tuple(pointers):
                    raise UndeclaredDischarge(
                        "the None discharge is the ABSENCE of one and may not name a claim, "
                        "and it names {!r}".format(tuple(pointers))
                    )
                continue
            if not tuple(pointers):
                raise UndeclaredDischarge(
                    "discharge {!r} names no claim; {}".format(name, _REFUSAL)
                )
        if subject not in self.discharges:
            raise UndeclaredDischarge(
                "the subject discharge {!r} is not one of the discharges this registry "
                "defines".format(subject)
            )
        self.subject = subject
        for family, name in sorted(self.families.items(), key=lambda kv: kv[0]):
            if name not in self.discharges:
                raise UndeclaredDischarge(
                    "family {!r} names discharge {!r}, which this registry does not "
                    "define".format(family, name)
                )

    @property
    def declared(self):
        return frozenset(self.families)

    def discharge_of(self, family):
        """The discharge `family` belongs to.  REFUSES an undeclared family; never defaults."""
        if family not in self.families:
            raise UndeclaredFamily(
                "family {!r} declares no discharge; {}".format(family, _REFUSAL)
            )
        return self.families[family]

    def pointers(self, family):
        """The claims and challenges `family`'s discharge names.  Refuses the same way."""
        return tuple(self.discharges[self.discharge_of(family)])

    def gated(self):
        """Exactly the families belonging to this census's OWN subject discharge."""
        return frozenset(f for f, name in self.families.items() if name == self.subject)

    def report(self, emitted):
        """[FamilyVerdict] over every family declared or emitted, sorted by family.

        `emitted` is {family: occurrence count} or any iterable of family names.  This is the
        REPORT half: it never raises, and it carries the third state so that a clean list and an
        unanswerable family are distinguishable.
        """
        counts = dict(emitted) if hasattr(emitted, "items") else collections.Counter(emitted)
        rows = []
        for family in sorted(set(counts) | set(self.families)):
            occurrences = int(counts.get(family, 0))
            if family not in self.families:
                rows.append(FamilyVerdict(family, UNDECLARED, UNKNOWN, occurrences))
            elif occurrences:
                rows.append(FamilyVerdict(family, DECLARED, self.families[family], occurrences))
            else:
                rows.append(FamilyVerdict(family, VACUOUS, self.families[family], occurrences))
        return rows

    def undeclared(self, emitted):
        """[family] for every emitted family that names no discharge.  The gate's own list."""
        return [row.family for row in self.report(emitted) if row.verdict == UNDECLARED]


def _discharge_text(row):
    if row.discharge is UNKNOWN:
        return "no discharge named"
    if row.discharge is None:
        return "no discharge at all -- declared NOT a debt"
    return row.discharge


def render_report(rows):
    """The printable form: one line per family, plus a line per refusal saying what to do."""
    lines = []
    for row in rows:
        lines.append(
            "  {:<12} {:<10} {:>4} occurrence(s)  {}".format(
                row.family, row.verdict, row.occurrences, _discharge_text(row)
            )
        )
        if row.verdict == VACUOUS:
            lines.append(
                "               -- an EMPTY DOMAIN: this family is declared and the census finds "
                "nothing of it, so the declaration is VACUOUS rather than withheld, and a clean "
                "reading here is evidence about nothing"
            )
        if row.verdict == UNDECLARED:
            lines.append("               -- REFUSED: " + _REFUSAL)
    return lines


# --------------------------------------------------------------------------- self-tests

def self_test():
    failures = []
    ran = []

    def ok(name, condition):
        ran.append(name)
        if not condition:
            failures.append(name)

    def raises(exception, call):
        try:
            call()
        except exception:
            return True
        except Exception:
            return False
        return False

    subject = "C-0140/C-0141"
    registry = DischargeRegistry(
        subject=subject,
        discharges={
            subject: ("C-0140", "C-0141"),
            "C-0154/C-0167": ("C-0154", "C-0167"),
            None: (),
        },
        families={
            "FOOTPRINT": subject,
            "PLACEMENT": subject,
            "GRILLAGE": "C-0154/C-0167",
            "SQUARE": None,
        },
    )

    # --- F1: the GETTER refuses.  Both directions, because the defect is that an ABSENCE was
    # read as an ANSWER: `FAMILY_DISCHARGE.get(family, SUBJECT)` returned the subject discharge
    # for a family nobody had thought about.
    ok("F1 a declared family returns its discharge",
       registry.discharge_of("FOOTPRINT") == subject)
    ok("F1 a family declared to another discharge returns THAT one",
       registry.discharge_of("GRILLAGE") == "C-0154/C-0167")
    ok("F1 a family declared None returns None -- an EXPLICIT not-a-debt",
       registry.discharge_of("SQUARE") is None)
    ok("F1 an UNDECLARED family raises rather than defaulting to the subject",
       raises(UndeclaredFamily, lambda: registry.discharge_of("NEW_FAMILY")))
    ok("F1 the refusal names the family",
       "NEW_FAMILY" in _message(UndeclaredFamily, lambda: registry.discharge_of("NEW_FAMILY")))
    ok("F1 the refusal asks CH-0229's own question -- is it ONE claim?",
       "CH-0229" in _message(UndeclaredFamily, lambda: registry.discharge_of("NEW_FAMILY")))

    # --- F2: a discharge must NAME a claim.  This is the half `CH-0229` asks for in as many
    # words: *name the claim that discharges each family*.  A discharge with no pointer is a
    # date with no claim behind it.
    ok("F2 a discharge naming no claim is refused at construction",
       raises(UndeclaredDischarge,
              lambda: DischargeRegistry("S", {"S": ()}, {"A": "S"})))
    ok("F2 the None discharge names no claim and IS accepted",
       DischargeRegistry("S", {"S": ("C-1",), None: ()}, {"A": None}).discharge_of("A") is None)
    ok("F2 the None discharge may not name a claim either -- it is the absence of one",
       raises(UndeclaredDischarge,
              lambda: DischargeRegistry("S", {"S": ("C-1",), None: ("C-2",)}, {"A": None})))
    ok("F2 a family naming a discharge the registry does not define is refused",
       raises(UndeclaredDischarge,
              lambda: DischargeRegistry("S", {"S": ("C-1",)}, {"A": "NOT-DEFINED"})))
    ok("F2 the subject must itself be a defined discharge",
       raises(UndeclaredDischarge,
              lambda: DischargeRegistry("S", {"T": ("C-1",)}, {"A": "T"})))
    ok("F2 a well-formed registry constructs",
       registry.subject == subject)
    ok("F2 pointers() returns the claims the family's discharge names",
       registry.pointers("GRILLAGE") == ("C-0154", "C-0167"))
    ok("F2 pointers() of a not-a-debt family is empty",
       registry.pointers("SQUARE") == ())
    ok("F2 pointers() of an undeclared family raises too",
       raises(UndeclaredFamily, lambda: registry.pointers("NEW_FAMILY")))

    # --- gated(): exactly the families belonging to this census's OWN subject
    ok("gated() is exactly the subject families",
       registry.gated() == frozenset({"FOOTPRINT", "PLACEMENT"}))
    ok("gated() excludes another census's discharge", "GRILLAGE" not in registry.gated())
    ok("gated() excludes a not-a-debt family", "SQUARE" not in registry.gated())

    # --- F3: the REPORT does not refuse, and it has a THIRD state.  `CLAUDE.md`: a getter must
    # refuse and a report must not, and an empty violation list on an UNANSWERABLE family is
    # indistinguishable from a clean one.
    rows = registry.report({"FOOTPRINT": 3, "GRILLAGE": 1, "NEW_FAMILY": 2})
    _absent = _AbsentRow()
    by_family = _Rows({row.family: row for row in rows})
    ok("F3 the report does NOT raise on an undeclared family", "NEW_FAMILY" in by_family)
    ok("F3 an emitted declared family reads DECLARED",
       by_family["FOOTPRINT"].verdict == DECLARED)
    ok("F3 an emitted UNDECLARED family reads UNDECLARED",
       by_family["NEW_FAMILY"].verdict == UNDECLARED)
    ok("F3 the occurrence count travels with the row",
       by_family["FOOTPRINT"].occurrences == 3 and by_family["NEW_FAMILY"].occurrences == 2)
    ok("F3 a DECLARED row carries its discharge",
       by_family["GRILLAGE"].discharge == "C-0154/C-0167")
    ok("F3 an UNDECLARED row's discharge is the SENTINEL, not None -- None is a legitimate "
       "discharge and the two must not collide",
       by_family["NEW_FAMILY"].discharge is UNKNOWN)
    ok("F3 the sentinel is not None", UNKNOWN is not None)

    # --- F3, the other half: a rule with an EMPTY DOMAIN is VACUOUS, not withheld
    ok("F3 a declared family the census found NOTHING of reads VACUOUS",
       by_family["PLACEMENT"].verdict == VACUOUS)
    ok("F3 a VACUOUS family is NOT undeclared -- the two must never be confused",
       by_family["PLACEMENT"].verdict != UNDECLARED)
    ok("F3 a VACUOUS row still carries its discharge",
       by_family["PLACEMENT"].discharge == subject)
    ok("F3 a VACUOUS row's occurrence count is zero",
       by_family["PLACEMENT"].occurrences == 0)
    ok("F3 an emitted family with a POSITIVE count is never VACUOUS",
       by_family["FOOTPRINT"].verdict != VACUOUS)
    ok("F3 there are exactly three verdicts", set(VERDICTS) == {DECLARED, VACUOUS, UNDECLARED})
    ok("F3 every reported verdict is one of the three",
       all(row.verdict in VERDICTS for row in rows))
    ok("F3 the report covers declared families the census did not emit",
       set(by_family) == {"FOOTPRINT", "PLACEMENT", "GRILLAGE", "SQUARE", "NEW_FAMILY"})
    ok("F3 the report is sorted by family, so its output is stable",
       [row.family for row in rows] == sorted(row.family for row in rows))
    ok("F3 undeclared() lists exactly the UNDECLARED families",
       registry.undeclared({"FOOTPRINT": 3, "NEW_FAMILY": 2}) == ["NEW_FAMILY"])
    ok("F3 undeclared() is empty when every emitted family is declared",
       registry.undeclared({"FOOTPRINT": 3, "GRILLAGE": 1}) == [])
    ok("F3 an iterable of names is accepted as well as a count map",
       [row.verdict for row in registry.report(["NEW_FAMILY"]) if row.family == "NEW_FAMILY"]
       == [UNDECLARED])
    ok("F3 an empty registry and an empty census give an empty report",
       DischargeRegistry("S", {"S": ("C-1",)}, {}).report({}) == [])

    # --- the rendering: a refusal that does not say what to do is a traceback with a nicer name
    text = " ".join(render_report(rows))
    ok("render names the undeclared family", "NEW_FAMILY" in text)
    ok("render tells the author to NAME the claim", "name the claim" in text.lower())
    ok("render asks CH-0229's question, which is the DISCOVERY half", "CH-0229" in text)
    ok("render says an empty domain is VACUOUS and not withheld",
       "vacuous" in text.lower() and "withheld" in text.lower())
    ok("render names the discharge of a DECLARED family", "C-0154/C-0167" in text)
    ok("render distinguishes the three verdicts",
       all(verdict in text for verdict in VERDICTS))
    clean = DischargeRegistry("S", {"S": ("C-1",)}, {"A": "S"})
    ok("render of a clean registry names no repair",
       "name the claim" not in " ".join(render_report(clean.report({"A": 1}))).lower())

    for failure in failures:
        print("FAIL  " + failure)
    print("self-test: {} test(s), {} failure(s)".format(len(ran), len(failures)))
    return 1 if failures else 0


class _AbsentRow(object):
    """What a lookup of a family the report did not mention returns, so that every named test
    reports its own failure instead of the first one raising a `KeyError` for all of them."""

    family = verdict = discharge = occurrences = None


class _Rows(dict):
    def __missing__(self, key):
        return _AbsentRow()


def _message(exception, call):
    """The text of the refusal `call` raises, or the empty string if it does not raise."""
    try:
        call()
    except exception as error:
        return str(error)
    except Exception:
        return ""
    return ""


def main(argv):
    """`--self-test` is the only thing this module does from the command line.

    The argument is PARSED rather than ignored: `CLAUDE.md` records an emitter that treated an
    unrecognised argument as data and built a 151-file shadow corpus in `./--help/`.
    """
    if argv in ([], ["--self-test"]):
        return self_test()
    print("usage: census_discharges.py [--self-test]")
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
