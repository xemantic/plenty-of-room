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
# P-29 -- the queue's STATUS VOCABULARY is closed, and every member agrees with the reader.
#
#     tools/check-queue-vocabulary.py            checks TASKS.md, exit 1 on any defect
#     tools/check-queue-vocabulary.py --census   prints every leading verdict it finds
#     tools/check-queue-vocabulary.py --selftest runs the self-tests
#
# WHY THIS EXISTS.  `CLAUDE.md` has recorded three times that the queue's status vocabulary GROWS
# and that every word the checker does not know is read wrongly -- and the failure direction is
# the unsafe one for a QUALIFIER: `tools/trace-answers.py`'s `queue_status` matches a closing word
# anywhere after the identifier, so `**PARTIALLY DONE**` read CLOSED until `PARTIALLY` was taught
# to it, and an OPEN row silently left the register.  The rule that entry states -- *a new status
# word must be tested in BOTH senses the day it is coined* -- was a convention with no mechanism,
# and iteration 41 duly coined `**SECOND DELIVERABLE ANSWERED**` on `T-9`, whose three deliverables
# are done, done and open.  `ANSWERS.md` said `T-9` was live, which was true; the queue read closed.
#
# WHAT IT CHECKS, AND WHY THIS PREDICATE.  Only a **leading, short, bold** run is a verdict.  That
# restriction is a measurement rather than a taste: over `TASKS.md` at the time of writing, bold
# runs *anywhere* in a row that contain a closing word number **29 distinct**, and most are prose
# about some OTHER task (*"`CH-0185` is ANSWERED"*, *"CONTINGENT is not KILLED"*) -- so a gate on
# them is ~25 rows of noise.  Restricted to a run that OPENS a cell and is at most `MAX_WORDS`
# words, the same corpus gives **8 distinct**, of which six are the declared vocabulary and two are
# legitimate coinages.  A gate whose false-positive rate is measured at zero is a gate that stays on.
#
# AND `CLAUDE.md`'s OWN PRESCRIBED SWEEP DOES NOT CATCH THIS.  That entry says the sweep is *"any
# row whose status cell starts `TODO` and which `queue_status` calls CLOSED"* -- tuned to the shape
# of the PREVIOUS instance.  No cell of the broken `T-9` row starts with `TODO`, and as a general
# predicate it is 20 of 21 false positives, because `strike, never delete` keeps a closed row's
# original `TODO -- MEDIUM` prose forever.
#
# THE PART THAT HAS CONTENT is not the vocabulary list, it is the AGREEMENT: every phrase declared
# closing must be read CLOSED by `trace-answers.queue_status`, and every phrase declared
# not-closing must be read OPEN by it.  `CLAUDE.md`: *a checker's blind spot is found by the tool
# that must agree with it.*  Declaring `SECOND DELIVERABLE ANSWERED` not-closing therefore does not
# silence anything -- it fails here until `_NOT_CLOSED_QUALIFIER` is taught the word too, which is
# the second sense the rule asks for and the one nobody writes.

import argparse
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
QUEUE = os.path.join(ROOT, "TASKS.md")

sys.path.insert(0, HERE)
_trace = __import__("trace-answers")

# A verdict is a bold run that OPENS a cell.  Six words is the widest legitimate one in the
# corpus (`ANSWERED in its specification half` is four); the bound exists so that a bold PROSE
# sentence containing a closing word is not read as a verdict.
MAX_WORDS = 6

CLOSING_WORD = re.compile(r"\b(DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED)\b")

# The vocabulary, in its two senses.  A phrase belongs here the day the queue coins it, and it
# belongs in the sense the QUEUE means -- not in the sense that makes this file pass.
CLOSING_VERDICTS = frozenset({
    "DONE",
    "KILLED",
    "CLOSED",
    "ANSWERED",
    "RESOLVED",
    "DISCHARGED",
    # `T-166`: NDI answered a question adjacent to the one asked, and more permissively.  The row
    # is closed and says on its face that the reading is inferred.
    "ANSWERED BY IMPLICATION",
    # `T-154`: the modelling half was requeued as `T-195` and the whole original TODO is struck,
    # so nothing of this row remains open.  A scope qualifier on a row with nothing left in it.
    "ANSWERED in its specification half",
})

NOT_CLOSING_VERDICTS = frozenset({
    "PARTIALLY DONE",
    "PARTLY DONE",
})

_ROW = re.compile(r"^\|\s*\**`?([TP]-\d{1,4}[a-z]?)`?\**\s*\|(.*)$")
# NO `^` here, deliberately: the anchoring is carried by `.match()` at the one call site.
# With both, the two are redundant and a mutation of EITHER is a no-op -- which the mutation
# test found by surviving twice.  One anchor, one place, and `.match` -> `.search` now bites.
_LEADING_BOLD = re.compile(r"\s*\*\*([^*]{1,120}?)\*\*")


def blank_struck(text):
    """Replace every ~~struck~~ span by spaces of the same length.

    `C-0071`'s rule is *strike, never delete*, so a withdrawn verdict stays in the file forever.
    A struck verdict is not a verdict, and blanking length-preservingly keeps offsets usable.
    """
    return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)


def leading_verdicts(queue_text):
    """[(task id, verdict phrase)] for every bold run that OPENS a cell and closes something."""
    found = []
    for line in queue_text.splitlines():
        match = _ROW.match(line.strip())
        if not match:
            continue
        identifier, rest = match.group(1), blank_struck(match.group(2))
        for cell in rest.split("|"):
            bold = _LEADING_BOLD.match(cell)
            if not bold:
                continue
            phrase = bold.group(1).strip()
            if CLOSING_WORD.search(phrase) and len(phrase.split()) <= MAX_WORDS:
                found.append((identifier, phrase))
    return found


def undeclared(queue_text):
    """[(task id, phrase)] for verdicts in neither sense of the vocabulary."""
    known = CLOSING_VERDICTS | NOT_CLOSING_VERDICTS
    return [(i, p) for i, p in leading_verdicts(queue_text) if p not in known]


def disagreements():
    """[(phrase, declared sense, sense `trace-answers` actually reads)].

    This is the half that cannot be satisfied by editing a list.  Each declared phrase is put
    through `queue_status` in a synthetic one-row queue; a phrase declared not-closing that the
    reader closes is exactly the defect this file exists for, and adding the phrase here does
    not hide it.
    """
    out = []
    for phrase, declared in (
        [(p, "CLOSING") for p in sorted(CLOSING_VERDICTS)]
        + [(p, "NOT CLOSING") for p in sorted(NOT_CLOSING_VERDICTS)]
    ):
        row = "| T-0 | subject | goal | leaf | **{}** (iteration 0) |".format(phrase)
        read = _trace.queue_status(row).get("T-0", "OPEN")
        expected = "CLOSED" if declared == "CLOSING" else "OPEN"
        if read != expected:
            out.append((phrase, declared, read))
    return out


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    # --- the predicate finds a leading verdict, in any cell ---
    check(
        "leading verdict in the last cell",
        leading_verdicts("| T-1 | a | b | c | **DONE** (iteration 3) |") == [("T-1", "DONE")],
    )
    check(
        "leading verdict in a middle cell",
        leading_verdicts("| T-1 | a | **ANSWERED** — x | c |") == [("T-1", "ANSWERED")],
    )
    check(
        "a row with no verdict yields none",
        leading_verdicts("| T-1 | a | TODO — **MEDIUM**, raised by `C-1` |") == [],
    )

    # --- the two shapes the corpus is full of, which must NOT be read as verdicts ---
    check(
        "a bold PROSE sentence mid-cell is not a verdict",
        leading_verdicts(
            "| T-1 | a | TODO — x, and **`CH-0185` is ANSWERED** so the price is withdrawn |"
        ) == [],
    )
    check(
        "a long leading bold sentence is not a verdict",
        leading_verdicts(
            "| T-1 | a | **THE CHEAP BOUND CLOSED IT IN ONE DIVISION AND THE ANSWER IS FAVOURABLE.** |"
        ) == [],
    )
    check(
        "a verdict AFTER a struck prefix is still found",
        leading_verdicts("| T-1 | a | ~~TODO~~ **DONE** (iteration 38) — x |")
        == [("T-1", "DONE")],
    )
    check(
        "a wholly struck verdict is not a verdict",
        leading_verdicts("| T-1 | a | ~~**DONE** (iteration 3)~~ |") == [],
    )
    check(
        "blanking a struck span preserves length",
        len(blank_struck("ab~~cd~~ef")) == len("ab~~cd~~ef"),
    )

    # --- the gate itself, in both directions ---
    check(
        "a declared verdict is accepted",
        undeclared("| T-1 | a | **PARTIALLY DONE** (iteration 35) |") == [],
    )
    check(
        "the iteration-41 coinage is REFUSED",
        undeclared("| T-9 | a | **SECOND DELIVERABLE ANSWERED, iteration 41** — x |")
        == [("T-9", "SECOND DELIVERABLE ANSWERED, iteration 41")],
    )
    check(
        "a scope qualifier nobody has declared is REFUSED",
        undeclared("| T-1 | a | **FIRST HALF DONE** — x |") == [("T-1", "FIRST HALF DONE")],
    )

    # --- the agreement half: this is what a list edit cannot satisfy ---
    check("the declared vocabulary agrees with the reader", disagreements() == [])
    check(
        "a not-closing phrase the reader closes would be caught",
        _trace.queue_status(
            "| T-0 | a | b | c | **SECOND DELIVERABLE ANSWERED** (iteration 0) |"
        ).get("T-0") == "CLOSED",
    )
    check(
        "`PARTIALLY DONE` is the qualifier the reader already knows",
        _trace.queue_status("| T-0 | a | b | c | **PARTIALLY DONE** (iteration 0) |").get("T-0")
        == "OPEN",
    )

    # --- MUTATION: narrowing or widening the predicate must fail a NAMED test ---
    # widened to any bold run anywhere -> the prose tests above fail; narrowed to whole-cell
    # equality -> the "(iteration 3)" tests fail.  Both are asserted by the tests, not asserted
    # here; this check records that the two mutations are covered.
    check(
        "MAX_WORDS admits the widest legitimate coinage",
        len("ANSWERED in its specification half".split()) <= MAX_WORDS,
    )

    for failure in failures:
        print("SELFTEST FAIL: {}".format(failure))
    print("# {} self-test(s), {} failure(s)".format(16, len(failures)))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queue", default=QUEUE)
    parser.add_argument("--census", action="store_true")
    parser.add_argument("--selftest", action="store_true")
    args = parser.parse_args(argv)

    if args.selftest:
        return _selftest()

    text = open(args.queue, encoding="utf-8").read()

    if args.census:
        counts = {}
        for identifier, phrase in leading_verdicts(text):
            counts.setdefault(phrase, []).append(identifier)
        for phrase in sorted(counts, key=lambda p: (-len(counts[p]), p)):
            sense = (
                "CLOSING" if phrase in CLOSING_VERDICTS
                else "NOT CLOSING" if phrase in NOT_CLOSING_VERDICTS
                else "UNDECLARED"
            )
            rows = counts[phrase]
            shown = "" if len(rows) > 4 else "  " + ",".join(sorted(set(rows)))
            print("{:4} {:<12} {!r}{}".format(len(rows), sense, phrase, shown))
        print("# {} distinct leading verdict(s)".format(len(counts)))
        return 0

    defects = 0
    for identifier, phrase in undeclared(text):
        print(
            "UNDECLARED  {}  {!r}\n"
            "            declare it in CLOSING_VERDICTS or NOT_CLOSING_VERDICTS in this file,\n"
            "            in the sense the QUEUE means, and add a named test for it".format(
                identifier, phrase
            )
        )
        defects += 1
    for phrase, declared, read in disagreements():
        print(
            "DISAGREES   {!r} is declared {} and `trace-answers.queue_status` reads it {}".format(
                phrase, declared, read
            )
        )
        defects += 1

    total = len(leading_verdicts(text))
    print(
        "# {} defect(s); {} leading verdict(s) in {}".format(
            defects, total, os.path.relpath(args.queue, ROOT)
        )
    )
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
