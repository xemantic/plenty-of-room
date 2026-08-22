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
import inspect
import io
import os
import re
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
QUEUE = os.path.join(ROOT, "TASKS.md")

sys.path.insert(0, HERE)
_trace = __import__("trace-answers")
import queue_verdicts as _verdicts

# `P-30` moved the predicate itself into `tools/queue_verdicts.py`, so that this gate and the
# reader it gates cannot drift apart -- which is exactly what they had done: this file gated a
# LEADING, short, BOLD run and the reader scanned the WHOLE row, and the residue between the two
# held four open rows the register read CLOSED.  The names are re-exported here because the
# module's whole subject is the vocabulary and its predicate.
MAX_WORDS = _verdicts.MAX_WORDS
CLOSING_WORD = _verdicts.CLOSING_WORD
blank_struck = _verdicts.blank_struck

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
    # `P-30`.  `TODO` carries no closing word, so `P-29`'s census could not see it at all -- and
    # it is the shape `T-261`'s status cell is written in, `TODO -- **MEDIUM**`, where the BOLD
    # carries the PRIORITY and the verdict is bare.  Every `TODO ...` opening normalises to this
    # one token, because the queue writes a different priority tail on each of them and a
    # vocabulary that enumerated the tails would be a vocabulary of prose.
    "TODO",
})

_ROW = _verdicts.TASK_ROW


def _queue_at(ref):
    """`TASKS.md` as it stands at one commit, or None where git cannot supply it.

    `T-289`.  A gate that cannot report the instance that motivated it is an argument and not an
    instrument, and the instance is in the COMMITTED past -- so it is read out of git rather than
    reconstructed as a fixture, which would assert the fixture.
    """
    import subprocess
    result = subprocess.run(
        ["git", "-C", ROOT, "show", "%s:TASKS.md" % ref],
        capture_output=True, text=True, errors="replace",
    )
    return result.stdout if result.returncode == 0 else None


def leading_verdicts(queue_text):
    """[(task id, verdict phrase)] for every run that OPENS a cell and is a verdict."""
    return [(i, p) for i, p, _ in _verdicts.leading_verdicts(queue_text)]


def first_verdicts(queue_text):
    """[(task id, phrase, sense)] -- the LEFTMOST verdict of each row, which is the live one."""
    found = []
    for line in queue_text.splitlines():
        match = _ROW.match(line.strip())
        if not match:
            continue
        verdicts = _verdicts.row_verdicts(match.group(2))
        if verdicts:
            found.append((match.group(1), verdicts[0][0], verdicts[0][1]))
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


def unseen_rows(queue_text):
    """[identifier] for every task row the READER does not see (`P-30`, F1).

    `tools/queue_verdicts.task_rows` is written independently of the reader's own regular
    expression and is deliberately more permissive -- a coverage check that shares the reader's
    pattern cannot see the reader's own format assumption, and that assumption is what hid the
    `T-182` row: `_QUEUE_ROW` required a TRAILING PIPE, GFM does not, and one committed row omits
    one.  272 rows in the file, 271 seen, and `tools/check-markdown-tables.py` clean throughout.
    """
    seen = set(_trace.queue_status(queue_text))
    return [i for i, _ in _verdicts.task_rows(queue_text) if i not in seen]


def row_disagreements(queue_text):
    """[(id, phrase, declared sense, sense the reader reads)] over the REAL rows (`P-30`, F5).

    `disagreements()` above puts each declared phrase through a SYNTHETIC one-row queue; this is
    the same question asked of the queue itself.  It is not tautological: the declared sense comes
    from the hand-maintained sets in this file and the read sense from the reader's own regular
    expression, so a phrase declared in the sense that makes this file comfortable rather than in
    the sense the queue means fires here, on the row that means it.
    """
    statuses = _trace.queue_status(queue_text)
    out = []
    for identifier, phrase, _ in first_verdicts(queue_text):
        if phrase in CLOSING_VERDICTS:
            expected = "CLOSED"
        elif phrase in NOT_CLOSING_VERDICTS:
            expected = "OPEN"
        else:
            continue  # undeclared() owns this one
        read = statuses.get(identifier, "OPEN")
        if read != expected:
            out.append((identifier, phrase, expected, read))
    return out


def residue(queue_text):
    """[(id, phrase, whole-row reading)] where the row's PROSE contradicts its verdict.

    GATED since `T-283`, and it was advisory before.  `P-30` left it advisory on the ground that it
    *cannot be made clean* -- `T-261`'s acceptance criterion quotes `ANSWERED`, `UPHELD` and
    `RESOLVED` as DATA, and lower-casing them would falsify the quotation.  That ground was right
    about the PREDICATE it had and not about the question: those three words are already
    BACKTICKED in the row as committed, and blanking inline code spans before the scan clears them
    without touching anything the row asserts.

    Measured over all 139 revisions of `TASKS.md`, the blanked predicate fires on 115 row-instances
    across SEVEN distinct rows and every one of the seven is a genuine idiom violation with a
    repair that falsifies nothing -- 0 false positives -- and it reads 0 rows on the queue this
    landed on.  `C-0083`: a gate that cannot come clean is not a gate; this one now can.

    The blanking touches the SCAN and never the VERDICT: `row_verdicts` reads the UNBLANKED body,
    so a backticked verdict is still a verdict and blanking cannot turn a closed row open.
    """
    out = []
    for line in queue_text.splitlines():
        match = _ROW.match(line.strip())
        if not match:
            continue
        body = _verdicts.blank_code_spans(_verdicts.blank_struck(match.group(2)))
        verdicts = _verdicts.row_verdicts(match.group(2))
        if not verdicts:
            continue
        scanned = "CLOSED" if _verdicts.UNQUALIFIED_CLOSING_WORD.search(body) else "OPEN"
        if scanned != verdicts[0][1]:
            out.append((match.group(1), verdicts[0][0], scanned))
    return out


def _selftest():
    failures = []
    ran = []

    def check(name, condition):
        ran.append(name)
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
        leading_verdicts("| T-1 | a | see the claim, raised by `C-1` |") == [],
    )
    # RESTATED by `P-30`.  This fixture used to assert `[]`: before `TODO` was a verdict, a status
    # cell whose only bold run is the PRIORITY carried nothing the census could see -- which is
    # precisely why `T-261` was on the fallback path and read CLOSED off its own title.
    check(
        "an unbolded leading TODO IS a verdict — the bold carries the priority",
        leading_verdicts("| T-1 | a | TODO — **MEDIUM**, raised by `C-1` |") == [("T-1", "TODO")],
    )
    check(
        "a bolded leading TODO normalises to the same one token",
        leading_verdicts("| T-1 | a | **TODO — HIGH VALUE, HIGHEST COST.** Step 6 of x |")
        == [("T-1", "TODO")],
    )

    # --- the two shapes the corpus is full of, which must NOT be read as verdicts ---
    check(
        "a bold PROSE sentence mid-cell is not a verdict",
        leading_verdicts(
            "| T-1 | a | TODO — x, and **`CH-0185` is ANSWERED** so the price is withdrawn |"
        ) == [("T-1", "TODO")],
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
    # --- P-30: the guards that make the predicate a predicate rather than a substring search ---
    check(
        "a closing word INSIDE a word is not a closing word",
        leading_verdicts("| T-1 | a | **ABANDONED BRANCH** — see `C-1` |") == [],
    )
    check(
        "a leading bold run carrying NO closing word is not a verdict",
        leading_verdicts("| T-6b | a | **Downgraded to low** — folded into `T-3a` |") == [],
    )
    check(
        "a bold closing word LATER in a cell does not lead it",
        leading_verdicts("| T-1 | a | see `C-1`, which is **DONE** |") == [],
    )
    check(
        "`TODO` inside a longer word does not open a cell",
        leading_verdicts("| T-1 | a | TODOS remain, and the row is **DONE** |") == [],
    )
    # `task_rows` is the coverage gate's second opinion, and a second opinion that consults the
    # first is not one.  This asserts the INDEPENDENCE structurally, because it is a structural
    # property: a coverage check sharing the reader's own pattern cannot see the reader's own
    # format assumption, which is exactly what hid the `T-182` row.
    check(
        "the coverage scanner does not consult the reader's own pattern",
        "TASK_ROW" not in inspect.getsource(_verdicts.task_rows),
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
    check(
        "`TODO` is a DECLARED verdict, in the not-closing sense",
        undeclared("| T-1 | a | TODO — **MEDIUM**, raised by `C-1` |") == [],
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

    # --- P-31: every DECLARED CLASSIFICATION is held open by a named test of its own ---
    # The whole-set mutation above (`undeclared` stubbed to `return []`) is killed by two named
    # tests, so the vocabulary was load-bearing as a SET.  Not one of its ELEVEN members was:
    # deleting `"DONE"` from `CLOSING_VERDICTS` failed nothing at all, and so did eight of the
    # other ten.  `T-225`'s per-name standard and `C-0176`'s -- *every classification, in BOTH
    # directions, must fail a named test when changed on its own* -- reached on this vocabulary.
    #
    # The phrases below are LITERALS and are deliberately not derived from the two sets: a test
    # generated from the set under test disappears together with the member it was meant to hold
    # open, which is the tautology `C-0178` already had to remove from the coverage scanner.  The
    # completeness check is what keeps the literal table honest in the other direction -- a phrase
    # declared without a fixture fails it, on the day it is declared.
    DECLARED_ROWS = (
        ("DONE", "CLOSED"),
        ("KILLED", "CLOSED"),
        ("CLOSED", "CLOSED"),
        ("ANSWERED", "CLOSED"),
        ("RESOLVED", "CLOSED"),
        ("DISCHARGED", "CLOSED"),
        ("ANSWERED BY IMPLICATION", "CLOSED"),
        ("ANSWERED in its specification half", "CLOSED"),
        ("PARTIALLY DONE", "OPEN"),
        ("PARTLY DONE", "OPEN"),
        ("TODO", "OPEN"),
    )
    check(
        "P-31 every declared phrase carries a literal fixture row of its own",
        {phrase for phrase, _ in DECLARED_ROWS}
        == CLOSING_VERDICTS | NOT_CLOSING_VERDICTS,
    )
    for _phrase, _sense in DECLARED_ROWS:
        _row = "| T-1 | a | b | c | **{}** (iteration 0) |".format(_phrase)
        check(
            "P-31 declared {!r} is accepted by the gate and reads {}".format(_phrase, _sense),
            undeclared(_row) == [] and first_verdicts(_row) == [("T-1", _phrase, _sense)],
        )

    # --- P-30: which verdict WINS, measured over the queue's own practice ---
    check(
        "the LEFTMOST verdict is the live one, not the last",
        first_verdicts("| T-1 | a | **DONE** (iteration 40) | TODO — **MEDIUM-HIGH** |")
        == [("T-1", "DONE", "CLOSED")],
    )
    check(
        "a row leading with TODO stays open however it ends",
        first_verdicts("| T-1 | a | — | **TODO — LOW.** Candidate 1 is **DONE** |")
        == [("T-1", "TODO", "OPEN")],
    )
    check(
        "a qualifier leads over a later TODO and both mean open",
        [s for _, _, s in first_verdicts("| T-1 | a | **PARTIALLY DONE** — x | **TODO — HIGH** |")]
        == ["OPEN"],
    )

    # --- P-30 F1: coverage.  The reader must see every row a permissive scanner finds ---
    check(
        "a row with no trailing pipe is a task row",
        [i for i, _ in _verdicts.task_rows("| T-182 | t | a | A1.2 | **DONE** (iteration 22).")]
        == ["T-182"],
    )
    check(
        "and the reader now sees it",
        unseen_rows("| T-182 | t | a | A1.2 | **DONE** (iteration 22).") == [],
    )
    check(
        "a table whose first cell is not an identifier is not a task row",
        _verdicts.task_rows("| E | `T-201` the fifth synthesis | `C-0115` | **DONE** |") == [],
    )
    check(
        "a separator row is not a task row",
        _verdicts.task_rows("|---|---|---|---|") == [],
    )
    check(
        "prose naming a task is not a task row",
        _verdicts.task_rows("T-182 is discussed here, and | is not a table") == [],
    )

    # --- P-30 F5: per-row agreement, on the REAL rows rather than synthetic ones ---
    check(
        "a declared row agrees with the reader",
        row_disagreements("| T-1 | a | b | c | **DONE** (iteration 3) |") == [],
    )
    check(
        "a row whose declared sense the reader contradicts is CAUGHT",
        row_disagreements("| T-1 | a | **PARTIALLY DONE** — x | e | **IN PROGRESS** |")
        == [("T-1", "PARTIALLY DONE", "OPEN", "IN PROGRESS")],
    )
    # The queue's preserved-priority shape: a live `**DONE**` to the left and the original
    # `TODO — **PRIORITY**` note to its right.  Nine committed rows are this shape, so the
    # agreement check must read the LEFTMOST verdict and not every verdict of the row.
    check(
        "a preserved TODO note beside a live DONE is not a disagreement",
        row_disagreements("| T-1 | a | **DONE** (iteration 40) | TODO — **MEDIUM-HIGH** |") == [],
    )

    # --- P-30 / T-283: the residue, now a GATE, and the blanking that made it one ---
    check(
        "a closing word in prose beside a TODO verdict is residue",
        residue("| T-1 | a | b | c | TODO — high, and CH-1 is now RESOLVED |")
        == [("T-1", "TODO", "CLOSED")],
    )
    check(
        "a row whose prose agrees with its verdict is not residue",
        residue("| T-1 | a | b | c | TODO — high, and CH-1 is now resolved |") == [],
    )
    # `T-283`.  A status word QUOTED AS DATA is not an assertion about the row, and backticks are
    # already this corpus's idiom for quoting a token.  `T-261`'s acceptance criterion quotes
    # three of `gpd/challenges/README.md`'s own status words, and lower-casing them would falsify
    # the quotation -- so the escape is two characters and it falsifies nothing.
    check(
        "T-283 a closing word inside a CODE SPAN is quoted data, not residue",
        residue("| T-1 | a | flags a challenge whose status is `ANSWERED` | c | TODO — **LOW** |")
        == [],
    )
    check(
        "T-283 a closing word OUTSIDE a code span beside one inside it is still residue",
        residue("| T-1 | a | status `ANSWERED`, and CH-1 is now RESOLVED | TODO — **LOW** |")
        == [("T-1", "TODO", "CLOSED")],
    )
    check(
        "T-283 a double-backticked span is blanked too",
        residue("| T-1 | a | the word ``ANSWERED`` as data | c | TODO — **LOW** |") == [],
    )
    check(
        "T-283 the blanking touches the SCAN and never the VERDICT — a backticked verdict is "
        "still read, so blanking can never turn a closed row open",
        first_verdicts("| T-1 | a | **DONE** (iteration 3) — see `CH-1`, `ANSWERED` |")
        == [("T-1", "DONE", "CLOSED")],
    )
    # The mutation test found this one by SURVIVING: no fixture had a code span in front of a
    # verdict, so *"blanking touches the scan and never the verdict"* was asserted nowhere in the
    # direction that matters.  Blanking a leading code span does not merely hide a word — it lets
    # the bold run BEHIND it become the cell's leading run, MANUFACTURING a closing verdict in a
    # cell that has none.  That is the UNSAFE direction: an open row would read closed.
    check(
        "T-283 blanking must not reach the VERDICT: a cell opening with a code span has no "
        "leading verdict, and blanking it would MANUFACTURE one",
        _verdicts.cell_verdict(" `note` **DONE** (iteration 3) ") is None
        and _verdicts.cell_verdict(
            _verdicts.blank_code_spans(" `note` **DONE** (iteration 3) ")
        ) == ("DONE", "CLOSED"),
    )
    check(
        "T-283 and the row reader does NOT blank, so such a row keeps its real verdict",
        _verdicts.row_verdicts(" `note` **DONE** (iteration 3) | TODO — **LOW** ")
        == [("TODO", "OPEN")],
    )
    check(
        "T-283 blanking a code span preserves length, so offsets stay usable",
        len(_verdicts.blank_code_spans("ab`cd`ef")) == len("ab`cd`ef"),
    )
    check(
        "T-283 blanking a code span removes what is inside it",
        "DONE" not in _verdicts.blank_code_spans("x `DONE` y"),
    )
    check(
        "T-283 blanking leaves text outside the span alone",
        _verdicts.blank_code_spans("keep `DONE` keep").startswith("keep ")
        and _verdicts.blank_code_spans("keep `DONE` keep").endswith(" keep"),
    )
    check(
        "T-283 two code spans do not MERGE — the text between them stays visible",
        residue("| T-1 | a | `CH-1` is now RESOLVED per `C-1` | c | TODO — **LOW** |")
        == [("T-1", "TODO", "CLOSED")],
    )
    check(
        "T-283 an UNCLOSED backtick is not a span — the rest of the row stays visible",
        residue("| T-1 | a | a stray ` and then RESOLVED | c | TODO — **LOW** |")
        == [("T-1", "TODO", "CLOSED")],
    )
    check(
        "T-283 the residue is now a GATE and the real queue reads zero",
        residue(open(QUEUE, encoding="utf-8").read()) == [],
    )
    # The blanking is the GATE's scan and not the READER's.  A row carrying no leading verdict
    # falls back to a whole-row scan in `trace-answers.queue_status`, and that scan is deliberately
    # NOT blanked — changing it would move the register, which is `P-30`'s territory and not this
    # task's.  Measured over the committed queue, blanking it would move NOTHING, and this test is
    # that measurement rather than an argument.
    check(
        "T-283 blanking the reader's own fallback would move no row of the committed queue",
        [
            identifier
            for identifier, body in _verdicts.task_rows(
                open(QUEUE, encoding="utf-8").read()
            )
            if not _verdicts.row_verdicts(body)
            and bool(_trace._CLOSED.search(_verdicts.blank_struck(body)))
            != bool(
                _trace._CLOSED.search(
                    _verdicts.blank_code_spans(_verdicts.blank_struck(body))
                )
            )
        ]
        == [],
    )

    # --- T-283: the gate END TO END, because promoting the residue is a change to `main` and a
    # structural assertion about `main` is not the same thing as running it ---
    def _gate_on(text):
        handle, path = tempfile.mkstemp(prefix="check-queue-vocabulary.", suffix=".md")
        try:
            os.write(handle, text.encode("utf-8"))
            os.close(handle)
            captured = io.StringIO()
            stdout, sys.stdout = sys.stdout, captured
            try:
                code = main(["--queue", path])
            finally:
                sys.stdout = stdout
            return code, captured.getvalue()
        finally:
            os.unlink(path)

    check(
        "T-283 the gate EXITS 1 on a residue row",
        _gate_on("| T-1 | a | b | c | TODO — high, and CH-1 is now RESOLVED |\n")[0] == 1,
    )
    check(
        "T-283 the gate EXITS 0 when the same word is backticked as data",
        _gate_on("| T-1 | a | b | c | TODO — high, and CH-1 is now `RESOLVED` |\n")[0] == 0,
    )
    check(
        "T-283 the gate EXITS 0 when the same word is lower-cased",
        _gate_on("| T-1 | a | b | c | TODO — high, and CH-1 is now resolved |\n")[0] == 0,
    )

    # Also found by a SURVIVING mutation: the refusal's own words were asserted nowhere, so a
    # message stripped to `RESIDUE T-1 'TODO' CLOSED` failed nothing.  A refusal that does not say
    # what to do is a traceback with a nicer name, and this predicate has TWO repairs.
    _residue_message = _gate_on("| T-1 | a | b | c | TODO — high, and CH-1 is now RESOLVED |\n")[1]
    check(
        "T-283 the refusal names the LOWER-CASING repair",
        "lower-case" in _residue_message,
    )
    check(
        "T-283 the refusal names the BACKTICKING repair, which is the one `T-261` needs",
        "backticks" in _residue_message,
    )
    check(
        "T-283 the refusal names the row and what the whole-row scan read",
        "T-1" in _residue_message and "CLOSED" in _residue_message,
    )

    # --- T-289: WHICH COLUMN a verdict stands in, read off the table's own header ---
    # `P-29` gates the vocabulary and `P-30` decides which verdict wins; neither reads a COLUMN,
    # so a verdict written into the LEAF cell agrees with both and the register still reads the
    # row off the wrong cell.  The status column is located by the header because `TASKS.md`
    # carries two schemas and a rule that counts from either end is wrong about one of them.
    check(
        "T-289 the four-column schema's status column is its THIRD",
        _verdicts.status_column(["ID", "Task", "Status", "Notes"]) == 2,
    )
    check(
        "T-289 the five-column schema's status column is its FIFTH",
        _verdicts.status_column(["ID", "Task", "Acceptance", "Leaf", "Status"]) == 4,
    )
    check(
        "T-289 a table with no status column has none, and its rows are not checked",
        _verdicts.status_column(["Agent", "Task", "Claims", "Challenges"]) is None,
    )
    check(
        "T-289 a status heading wrapped in emphasis is still the status heading",
        _verdicts.status_column(["ID", "**Status**"]) == 1,
    )
    check(
        "T-289 a verdict in the status column is not miscolumned",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | A8.2 | **DONE** (iteration 3) |\n"
        ) == [],
    )
    check(
        "T-289 a verdict in the LEAF column IS miscolumned — the `T-276` shape",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | **DONE** (iteration 3) | **TODO — HIGH** |\n"
        ) == [("T-1", 3, "DONE", "leaf", "status")],
    )
    check(
        "T-289 a verdict in the NOTES column of the four-column schema is miscolumned too",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Status | Notes |\n|---|---|---|---|\n"
            "| P-1 | t | an acceptance | **DONE** (iteration 3) |\n"
        ) == [("P-1", 3, "DONE", "notes", "status")],
    )
    check(
        "T-289 a row in a table with NO status column is not checked at all",
        _verdicts.miscolumned_verdicts(
            "| Agent | Task | Claims | Challenges |\n|---|---|---|---|\n"
            "| T-1 | t | **DONE** | — |\n"
        ) == [],
    )
    check(
        "T-289 a row whose first cell is not an identifier is not checked",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Status | Notes |\n|---|---|---|---|\n"
            "| E | the fifth synthesis | an acceptance | **DONE** (iteration 3) |\n"
        ) == [],
    )
    check(
        "T-289 a wholly STRUCK verdict in the wrong column is not a verdict",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | ~~**DONE** (iteration 3)~~ | **TODO — HIGH** |\n"
        ) == [],
    )
    # The DISCRIMINATING struck fixture, and the mutation test is what asked for it: a wholly
    # struck verdict is refused by `_LEADING_BOLD` whether or not anything is blanked, so it holds
    # the blanking open nowhere.  A verdict BEHIND a struck prefix is found only when the strike
    # is blanked, which is the shape `C-0071`'s *strike, never delete* actually produces.
    check(
        "T-289 a verdict behind a STRUCK prefix in the wrong column is still miscolumned",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | ~~TODO~~ **DONE** (iteration 3) | **TODO — HIGH** |\n"
        ) == [("T-1", 3, "DONE", "leaf", "status")],
    )
    # `C-0083`: the only literal pipe a GFM cell can carry is `\|`, and this corpus uses it — a
    # naive `split("|")` gives `T-60`'s four-column row SIX cells, so a column index computed
    # that way is not a column index.  Nine committed rows carry one.
    check(
        "T-289 an ESCAPED pipe is a literal, not a column boundary",
        _verdicts.split_cells(r"| T-1 | a `\|F_es\|` b | c |") == ["T-1", r"a `\|F_es\|` b", "c"],
    )
    check(
        "T-289 and a verdict behind an escaped pipe is still read in its own column",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            + r"| T-1 | t | a `\|F_es\|` b | A8.2 | **DONE** (iteration 3) |" + "\n"
        ) == [],
    )
    check(
        "T-289 the cell split is the WIDTH GATE's own, so one definition of a cell serves both",
        "cells" in inspect.getsource(_verdicts.split_cells),
    )
    # A pipe line that is NOT followed by a separator is not a header, which is the renderer's
    # reading and `tools/check-markdown-tables.py`'s.  Without that rule the stray line above
    # becomes the header and the row is read against the wrong schema — it still fires, and it
    # names the wrong column, which is the failure a column rule must not have.
    check(
        "T-289 a pipe line not followed by a SEPARATOR is not a header",
        _verdicts.miscolumned_verdicts(
            "| ID | Task | Status | Notes |\n"
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | **DONE** (iteration 3) | **TODO** |\n"
        ) == [("T-1", 4, "DONE", "leaf", "status")],
    )
    check(
        "T-289 a row is attributed to the table whose header PRECEDES it",
        [row[0] for row in _verdicts.miscolumned_verdicts(
            "| ID | Task | Status | Notes |\n|---|---|---|---|\n"
            "| P-1 | t | **DONE** (iteration 1) | notes |\n"
            "\n"
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | **DONE** (iteration 3) | **TODO** |\n"
        )] == ["T-1"],
    )
    # THE INSTANCE THIS WAS WRITTEN FOR.  At `9620d3e` -- `P-30`'s own commit -- the `T-276` row
    # carried its iteration-41 record in the LEAF cell and its live `**TODO — HIGH**` in the
    # status cell, `queue_status` read the row CLOSED, and all three existing arms of this gate
    # read zero defects on it.  A gate that cannot report the instance that motivated it is an
    # argument and not an instrument (`P-31`'s own standard, on `P-31`'s own commit).
    _broken = _queue_at("9620d3e")
    if _broken is not None:
        check(
            "T-289 the column rule fires on `T-276` at the commit the register read it CLOSED",
            [row for row in _verdicts.miscolumned_verdicts(_broken) if row[0] == "T-276"]
            == [("T-276", 700, "DONE", "leaf", "status")],
        )
        check(
            "T-289 and at that same commit the register really did read it CLOSED",
            _trace.queue_status(_broken).get("T-276") == "CLOSED",
        )
        check(
            "T-289 and every EXISTING arm of this gate read zero defects on that row — which is "
            "why the class needed a new predicate rather than a wider vocabulary",
            [r for r in undeclared(_broken) if r[0] == "T-276"] == []
            and [r for r in row_disagreements(_broken) if r[0] == "T-276"] == []
            and [r for r in residue(_broken) if r[0] == "T-276"] == [],
        )

    # --- T-289: the arm is ADVISORY, and that is a decision with a measurement behind it ---
    _leaf_row = (
        "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
        "| T-1 | t | a | **DONE** (iteration 3) | **TODO — HIGH** |\n"
    )
    _leaf_code, _leaf_message = _gate_on(_leaf_row)
    check(
        "T-289 a miscolumned verdict does NOT fail the gate — the arm is advisory, because the"
        " predicate reads 21 genuine rows of the queue it lands on and a gate that cannot come"
        " clean is not a gate",
        _leaf_code == 0,
    )
    check(
        "T-289 and it is REPORTED rather than silently tolerated",
        "MISCOLUMN" in _leaf_message and "T-1" in _leaf_message,
    )
    check(
        "T-289 the advisory names the heading it renders under and the one it belongs under",
        "'leaf'" in _leaf_message and "'status'" in _leaf_message,
    )
    check(
        "T-289 the advisory names the REPAIR, because a refusal that does not say what to do is a"
        " traceback with a nicer name",
        "status cell" in _leaf_message and "strik" in _leaf_message,
    )
    check(
        "T-289 the count is printed even when it is zero, so the residue cannot go quiet",
        "miscolumned verdicts" in _gate_on(
            "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
            "| T-1 | t | a | A8.2 | **DONE** (iteration 3) |\n"
        )[1],
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
    print("# {} self-test(s), {} failure(s)".format(len(ran), len(failures)))
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

    for identifier in unseen_rows(text):
        print(
            "UNSEEN      {}  is a task row that `trace-answers.queue_status` does not read\n"
            "            — check the row's shape against `queue_verdicts.TASK_ROW`; a row the\n"
            "            reader cannot see is an item silently outside the register".format(
                identifier
            )
        )
        defects += 1
    for identifier, phrase, expected, read in row_disagreements(text):
        print(
            "ROW         {}  leads with {!r}, declared {}, and the reader reads the row {}".format(
                identifier, phrase, expected, read
            )
        )
        defects += 1

    # GATED since `T-283`, on a measured false-positive rate of 0 over 139 revisions — see
    # `residue()`.  The repair is one of two, and both are in the message because a refusal that
    # does not say what to do is a traceback with a nicer name.
    for identifier, phrase, scanned in residue(text):
        print(
            "RESIDUE     {}  leads with {!r} and its PROSE carries a closing word: a whole-row\n"
            "            scan reads {}. The queue writes verdicts in bold UPPER CASE and prose in\n"
            "            lower, so either lower-case the word, or — if it is a status token quoted\n"
            "            as DATA — put it in `backticks`, which this scan blanks".format(
                identifier, phrase, scanned
            )
        )
        defects += 1

    # `T-289`.  ADVISORY, and it says why in its own output: the predicate reads 21 rows of the
    # queue it lands on and every one of them is genuine, so gating it would be a build failure
    # nobody could clear without editing 21 rows -- `C-0083`'s *a gate that cannot come clean is
    # not a gate*, and `CLAUDE.md`'s *print an ungated residue beside a gated arm rather than
    # narrowing the predicate until the tree is clean*.  The repair is a queue edit and it is
    # queued as a row of its own; when the count reaches 0 this arm becomes a gate by deleting a
    # word.
    miscolumned = _verdicts.miscolumned_verdicts(text)
    for identifier, line, phrase, heading, status_heading in miscolumned:
        print(
            "MISCOLUMN   {:<6} line {}: {!r} renders under {!r}, not under {!r}".format(
                identifier, line, phrase, heading, status_heading
            )
        )
    if miscolumned:
        print(
            "            move the record into the status cell, striking any verdict it supersedes.\n"
            "            The register reads a row's LEFTMOST verdict, so a row in this shape is\n"
            "            right only while the leftmost cell happens to hold the LIVE one --\n"
            "            `T-276` held the SUPERSEDED one there and a live HIGH row read CLOSED"
        )

    total = len(leading_verdicts(text))
    rows = len(_verdicts.task_rows(text))
    print(
        "# {} defect(s); {} leading verdict(s) over {} row(s) in {}".format(
            defects, total, rows, os.path.relpath(args.queue, ROOT)
        )
    )
    print(
        "# residue (GATED since T-283, at a measured false-positive rate of 0 over 139 revisions):"
        " {} row(s) whose prose carries a closing word that is not their verdict".format(
            len(residue(text))
        )
    )
    print(
        "# miscolumned verdicts (T-289, ADVISORY -- 0 false positives over 140 revisions and it"
        " cannot come clean without a queue edit): {} verdict(s) rendering under a heading that is"
        " not their table's status column".format(len(miscolumned))
    )

    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
