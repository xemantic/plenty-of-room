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
# Self-test for tools/trace-answers.py (task T-131).
#
#     tools/test-trace-answers.py
#
# The tracer decides which numbers in the repository's primary deliverable are unsupported,
# so a false ABSENT would send an agent to "correct" a number that is perfectly good and a
# false CITED would let a drifted one through.  Both failure modes are silent, which is
# exactly the case for executable tests.  Fixtures are in-memory; nothing here reads the
# checkout.
import shutil
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import importlib.util

_spec = importlib.util.spec_from_file_location(
    "trace_answers", os.path.join(os.path.dirname(os.path.abspath(__file__)), "trace-answers.py")
)
trace_answers = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(trace_answers)

_failures = []


def check(name, actual, expected):
    if actual != expected:
        _failures.append("{}: expected {!r}, got {!r}".format(name, expected, actual))
        print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
    else:
        print("ok   {}".format(name))


# --- normalisation -------------------------------------------------------------------------
check(
    "en dash folds to hyphen",
    trace_answers.normalise("1.19–1.42×"),
    "1.19-1.42x",
)
check(
    "U+2212 minus folds to hyphen",
    trace_answers.normalise("−8.40"),
    "-8.40",
)

# --- identifier stripping ------------------------------------------------------------------
check(
    "claim IDs are not quantities",
    trace_answers.tokens("`C-0051` and `CH-0077` and `T-131` and `A2.1` and §4(g)"),
    [],
)
check(
    "a leaf ID next to a real number leaves only the number",
    trace_answers.tokens("`A2.1` gives 33.333 pN/nm"),
    ["33.333"],
)

# --- tokenising ----------------------------------------------------------------------------
check(
    "a range yields both ends",
    trace_answers.tokens("the margin is 1.19–1.42×"),
    ["1.19", "1.42"],
)
check(
    "single digits are dropped by default",
    trace_answers.tokens("3 nm of stroke at 45 attachments"),
    ["45"],
)
check(
    "single digits are kept at min_digits=1",
    trace_answers.tokens("3 nm of stroke", min_digits=1),
    ["3"],
)
check(
    "duplicates collapse in order",
    trace_answers.tokens("0.0753 then 0.0544 then 0.0753"),
    ["0.0753", "0.0544"],
)
check(
    "exponent notation survives",
    trace_answers.tokens("a residual of 2.4e-15"),
    ["2.4e-15"],
)

# --- citation extraction -------------------------------------------------------------------
check(
    "citations are collected in order without duplicates",
    trace_answers.citations("(`C-0021`, `C-0023`, `CH-0027`, `C-0021`)"),
    ["C-0021", "C-0023", "CH-0027"],
)

# --- substring guard -----------------------------------------------------------------------
check(
    "45 does not match inside 1.45",
    trace_answers.contains("the value is 1.45 pN", "45"),
    False,
)
check(
    "45 does not match inside 450",
    trace_answers.contains("the value is 450 pN", "45"),
    False,
)
check(
    "45 matches standing alone",
    trace_answers.contains("45 attachments", "45"),
    True,
)
check(
    "0.0706 matches inside a sentence",
    trace_answers.contains("dishes **0.0706** of the stroke", "0.0706"),
    True,
)

# --- blocking ------------------------------------------------------------------------------
_answers = "\n".join(
    [
        "# heading",
        "",
        "a paragraph citing `C-0001` with 12.5 in it",
        "and its second line with 99.9 in it",
        "",
        "| a | table row citing `C-0002` with 7.75 | ",
    ]
)
_blocks = trace_answers.blocks(_answers)
check("blocks: heading, paragraph, table row", len(_blocks), 3)
check("table row is its own block", _blocks[2][1].startswith("|"), True)
check(
    "a paragraph's lines are one block",
    trace_answers.tokens(_blocks[1][1]),
    ["12.5", "99.9"],
)

# --- end-to-end tracing --------------------------------------------------------------------
_sources = {
    "C-0001": "the layer stiffness is 12.5 pN/nm at the working point",
    "C-0002": "an unrelated claim quoting 99.9 and 7.75",
}
_records = trace_answers.trace(_answers, _sources)
_by_token = {token: (status, owners) for _, token, status, _, owners in _records}
check("a number in the cited claim is CITED", _by_token["12.5"][0], "CITED")
check("a number only in another claim is ELSEWHERE", _by_token["99.9"][0], "ELSEWHERE")
check("its owner is reported", _by_token["99.9"][1], ["C-0002"])
check(
    "a table row's own citation is honoured",
    _by_token["7.75"][0],
    "CITED",
)

_absent = trace_answers.trace("a paragraph citing `C-0001` with 42.42 in it", _sources)
check("a number in no claim at all is ABSENT", _absent[0][2], "ABSENT")

# --- status drift --------------------------------------------------------------------------
#
# `C-0067` found the worst drift is not in the VALUE of a number but in the STATUS of an
# answer: three entries of "what we cannot answer" that the programme had answered, one of
# them seven iterations earlier.  The numeric tracer above cannot see that class at all — a
# stale "`T-129`, open" contains no number — so it is checked separately, against `TASKS.md`,
# which is the register that knows whether a task is open.

_queue = "\n".join(
    [
        "| T-45 | a question | acceptance | A1.2 | **ANSWERED as far as published measurement allows** |",
        "| T-95 | another | acceptance | A8.2 | TODO — it is a question, not a task |",
        "| T-129 | a third | acceptance | A8.2 | **DONE** (iteration 13) — claim `C-0068` |",
        "| T-136 | a fourth | acceptance | A8.2 | TODO — high |",
        "| T-137 | a fifth | acceptance | A8.2 | **IN PROGRESS** (iteration 15) |",
    ]
)

check(
    "an open task reads open",
    trace_answers.queue_status(_queue)["T-95"],
    "OPEN",
)
check(
    "a DONE task reads closed",
    trace_answers.queue_status(_queue)["T-129"],
    "CLOSED",
)
check(
    "an ANSWERED task reads closed",
    trace_answers.queue_status(_queue)["T-45"],
    "CLOSED",
)
check(
    "IN PROGRESS is not open — it is being answered right now",
    trace_answers.queue_status(_queue)["T-137"],
    "IN PROGRESS",
)
check(
    "a task the queue does not carry is UNKNOWN",
    trace_answers.queue_status(_queue).get("T-999", "UNKNOWN"),
    "UNKNOWN",
)

# The queue's real rows are long prose.  Two substring traps are live in it:
# "left undone" contains DONE, and a row can discuss having "answered" something in passing.
# The queue writes its own verdicts in BOLD UPPERCASE and its prose in lower case, so the
# closing words are matched case-sensitively and on whole words.
check(
    "'left undone' in prose does not close a row",
    trace_answers.queue_status(
        "| T-200 | t | a | A2.1 | TODO — high. Left undone: the window is unsynthesised. |"
    )["T-200"],
    "OPEN",
)
check(
    "lower-case 'answered' in prose does not close a row",
    trace_answers.queue_status(
        "| T-201 | t | a | A2.1 | TODO — this cannot be answered without a measurement. |"
    )["T-201"],
    "OPEN",
)
# The queue's status vocabulary GROWS.  `SESSION-PROMPT.md` declares DONE and KILLED; iteration 14
# wrote ANSWERED for `T-45` and iteration 17 wrote DISCHARGED for `T-95`/`T-102` — a status that is
# neither "answered" nor "abandoned" but "stopped applying", which is a distinction `C-0071` had to
# invent and which this project needs.  Every new word is silently read as OPEN until it is added
# here, so a closed task keeps reading open, which is the exact drift this checker exists to catch.
check(
    "DISCHARGED closes a row — a question that stopped applying is not open",
    trace_answers.queue_status(
        "| T-95 | q | a | A8.2 | **DISCHARGED** (iteration 14) — by `C-0071` |"
    )["T-95"],
    "CLOSED",
)
check(
    "'DONE' inside a word does not close a row",
    trace_answers.queue_status("| T-202 | t | a | A2.1 | TODO — see ABANDONED branch |")["T-202"],
    "OPEN",
)

# `PARTIALLY DONE` — iteration 35's coinage, for a task whose deliverable list is partly
# discharged and partly live (`T-9`: the hinge constant measured, the vertical compliance and the
# in-plane shear still open).  The failure direction is the UNSAFE one, so it needs a test in both
# senses: the qualifier must not close, and it must not stop a plain closing word from closing.
check(
    "'PARTIALLY DONE' leaves a row OPEN",
    trace_answers.queue_status(
        "| T-9 | hinge | a | A1.2 | **PARTIALLY DONE** (iteration 35) — claim `C-0157`; two of "
        "three deliverables remain |"
    )["T-9"],
    "OPEN",
)
check(
    "'PARTLY DONE' leaves a row OPEN too",
    trace_answers.queue_status("| T-9 | hinge | a | A1.2 | **PARTLY DONE** — `C-0157` |")["T-9"],
    "OPEN",
)
check(
    "a plain DONE still closes, beside the qualifier",
    trace_answers.queue_status("| T-9 | hinge | a | A1.2 | **DONE** (iteration 35) |")["T-9"],
    "CLOSED",
)
# RESTATED by `P-30`.  This fixture used to assert CLOSED, on the whole-row scan: a `PARTIALLY
# DONE` qualifier must not stop a LATER bare closing word from closing.  `queue_status` now reads
# the row's FIRST leading verdict where it has one, so the row reads OPEN — and that is the
# reading the queue's own practice supports, because the file REPLACES a leading verdict when a
# task closes (`P-29`'s own row reads `**DONE** (iteration 42)`) rather than appending beside it.
# The direction is the safe one: a row still leading with a qualifier stays IN the register.
# What the original fixture was protecting is kept below, on the FALLBACK path, where it belongs.
check(
    "a row LEADING with a qualifier stays open, whatever it says later",
    trace_answers.queue_status(
        "| T-9 | hinge | a | A1.2 | **PARTIALLY DONE**, and now **RESOLVED** in full |"
    )["T-9"],
    "OPEN",
)
check(
    "on the fallback path the qualifier still does not swallow a later closing word",
    trace_answers.queue_status(
        "| T-9 | hinge | a | A1.2 | partially done so far, and now **RESOLVED** in full |"
    )["T-9"],
    "CLOSED",
)

# --- P-30: the row's LEADING verdict decides, and the reader sees every row -------------------
#
# `queue_status` used to search the WHOLE row after the identifier, so a closing word about some
# OTHER object — a challenge, a deliverable, a candidate of a remedy, or the row's own title —
# closed the row.  Four live rows were outside the register for exactly that reason.  The rule is
# now: the FIRST leading verdict wins; a row with none falls back to the whole-row scan.
check(
    "a closing word in the row's own TITLE does not close it",
    trace_answers.queue_status(
        "| T-261 | **A synthesis rests a number on a challenge the corpus has since ANSWERED** | "
        "an arm of the tracer | — | TODO — **MEDIUM**, raised by `CH-0203` |"
    )["T-261"],
    "OPEN",
)
check(
    "an unbolded leading TODO is a verdict — the bold carries the PRIORITY",
    trace_answers.queue_status("| T-1 | t | a | — | TODO — **MEDIUM**, raised by `C-1` |")["T-1"],
    "OPEN",
)
check(
    "a closing word about a CHALLENGE does not close the row",
    trace_answers.queue_status(
        "| T-268 | t | a | **PARTIALLY DONE** (iteration 39) — `CH-0207` **CLOSED and REPAIRED** |"
        " **TODO — HIGH VALUE, HIGHEST COST** |"
    )["T-268"],
    "OPEN",
)
check(
    "a closing word about a DELIVERABLE does not close the row",
    trace_answers.queue_status(
        "| T-272 | t | a | **PARTIALLY DONE** (iteration 41) — `P2` is DISCHARGED over the whole "
        "corpus | **TODO — HIGH** |"
    )["T-272"],
    "OPEN",
)
check(
    "a closing word about a CANDIDATE of the remedy does not close the row",
    trace_answers.queue_status(
        "| T-280 | t | a | — | **TODO — LOW.** Candidate 1 is **DONE**; this is candidate 2 |"
    )["T-280"],
    "OPEN",
)
# The queue's own practice: the live verdict is written to the LEFT and the original priority
# note is preserved to its right.  Nine rows of the committed queue are that shape, so the last
# verdict cannot be the one that wins.
check(
    "a live DONE to the LEFT beats a preserved TODO note to its right",
    trace_answers.queue_status(
        "| T-263 | t | a | **DONE** (iteration 40) — claim `C-0167` | TODO — **MEDIUM-HIGH** |"
    )["T-263"],
    "CLOSED",
)
# A row with no leading verdict at all still falls back to the whole-row scan, which is how the
# oldest rows in the queue are written (`| P-1 | ... | DONE | Iteration 1 |`).
check(
    "a bare unbolded DONE in a cell of its own still closes, on the fallback path",
    trace_answers.queue_status("| P-1 | the GPD loop skeleton | DONE | Iteration 1 |")["P-1"],
    "CLOSED",
)
check(
    "a leading bold run too long to be a verdict falls back to the whole-row scan",
    trace_answers.queue_status(
        "| T-45 | q | a | A1.2 | **ANSWERED as far as published measurement allows** |"
    )["T-45"],
    "CLOSED",
)

# COVERAGE.  `_QUEUE_ROW` required a TRAILING pipe, and the `T-182` row has none — so that row was
# invisible to the reader entirely, 271 rows seen of 272.  GFM does not require it, so
# `tools/check-markdown-tables.py` is clean on such a row and the assumption was asserted nowhere.
check(
    "a row with no trailing pipe is still seen",
    trace_answers.queue_status("| T-182 | t | a | A1.2 | **DONE** (iteration 22) — new rows.")
    .get("T-182"),
    "CLOSED",
)
check(
    "a row whose identifier is wrapped in backticks is still seen",
    trace_answers.queue_status("| `T-183` | t | a | A1.2 | TODO — high |").get("T-183"),
    "OPEN",
)
check(
    "a row whose identifier is bold is still seen",
    trace_answers.queue_status("| **T-184** | t | a | A1.2 | TODO — high |").get("T-184"),
    "OPEN",
)
check(
    "a line that is not a table row is still not a row",
    sorted(trace_answers.queue_status("T-185 is not in a table at all")),
    [],
)
check(
    "a table whose first cell is not an identifier is not a queue row",
    sorted(trace_answers.queue_status("| E | `T-201` the fifth synthesis | `C-0115` | **DONE** |")),
    [],
)
check(
    "a pipe table starting mid-line is not a queue row",
    sorted(trace_answers.queue_status("prose about it: | T-186 | t | a | **DONE** |")),
    [],
)
check(
    "a TODO later in a cell does not reopen a row its verdict closed",
    trace_answers.queue_status(
        "| T-187 | t | a | A1.2 | **DONE** (iteration 3) — the TODO it raised is struck |"
    )["T-187"],
    "CLOSED",
)

# The deliverable's own phrasings.  Each of these is a claim that a task is still open.
check(
    "`T-129`, open is detected",
    [line for line, _, _ in trace_answers.open_assertions("and whether it is flat is `T-129`, open.")],
    [1],
)
check(
    "the task is reported with it",
    [task for _, task, _ in trace_answers.open_assertions("and whether it is flat is `T-129`, open.")],
    ["T-129"],
)
check(
    "'`T-50` remains open' is detected",
    [task for _, task, _ in trace_answers.open_assertions("`T-50` remains open at this time")],
    ["T-50"],
)
check(
    "'still open (`T-9`)' is detected",
    [task for _, task, _ in trace_answers.open_assertions("this is still open (`T-9`)")],
    ["T-9"],
)
check(
    "a task merely mentioned is NOT an open assertion",
    trace_answers.open_assertions("this was settled by `T-129` in iteration 13"),
    [],
)
check(
    "the word open far from the task is NOT an assertion",
    trace_answers.open_assertions("`T-129` did this, and separately the electrode question is open"),
    [],
)

# Two false positives the real deliverable produced on the first run.  Both cost an agent a
# trip to "correct" a passage that is already right, which is the failure mode a drift checker
# can least afford: the tool exists to be believed.
check(
    "'open since iteration 3, is answered' is history, not an assertion",
    trace_answers.open_assertions(
        "So **`T-45`, open since iteration 3, is answered from published measurement.**"
    ),
    [],
)
check(
    "an answering word in the same window cancels the assertion",
    trace_answers.open_assertions("the open question `T-60` was resolved by a later claim"),
    [],
)
check(
    "but a bare open assertion still fires beside them",
    [task for _, task, _ in trace_answers.open_assertions("whether it is flat is `T-129`, open.")],
    ["T-129"],
)

_stale = trace_answers.stale_statuses(
    "line one is fine\nand whether it is flat is `T-129`, open.\nand `T-95`, open.", _queue
)
check("a stale open marker is reported", [task for _, task, _ in _stale], ["T-129"])
check("a genuinely open one is not", [task for _, task, _ in _stale if task == "T-95"], [])
check(
    "the queue's own status travels with the finding",
    [status for _, _, status in _stale],
    ["CLOSED"],
)

# --- self-consistency: does the deliverable agree with ITSELF? -------------------------------
#
# `C-0080` found the third drift class and the blind spot that makes it invisible.  The tracer
# above checks the deliverable against the CORPUS, in two directions — is a number owned, is an
# open assertion still true.  Neither can see a document that contradicts ITSELF: `ANSWERS.md`
# called `T-45` "answered from published measurement" in one section and "still unmeasured" in
# another, and BOTH halves passed, because the first sentence has an owner and the second
# parenthesis carries no number at all.
#
# The check is per task ID: collect every claim the deliverable makes about a task's status, and
# report a task called both settled and unsettled.  It is deliberately one-sided in what counts
# as evidence — only explicit status words do, so a task merely mentioned stays silent.

check(
    "a task called answered in one place and unmeasured in another is inconsistent",
    [c.task for c in trace_answers.self_contradictions(
        "`T-45` is answered from published measurement.\n\nand later: (`T-45` is still unmeasured)"
    )],
    ["T-45"],
)
check(
    "both verdicts are reported, so a reader can see which to keep",
    sorted(trace_answers.self_contradictions(
        "`T-45` is answered from published measurement.\n\nand later: (`T-45` is still unmeasured)"
    )[0].verdicts),
    ["OPEN", "SETTLED"],
)
check(
    "consistent mentions are silent",
    trace_answers.self_contradictions(
        "`T-45` is answered.\n\nand `T-45` was answered in iteration 14."
    ),
    [],
)
check(
    "a task merely mentioned carries no verdict",
    trace_answers.self_contradictions("see `T-45` and `T-45` again"),
    [],
)
check(
    "one open mention alone is not a contradiction",
    trace_answers.self_contradictions("`T-63` is still open"),
    [],
)
check(
    "the classifier reads a settled word",
    trace_answers.status_words("this is answered from published measurement"),
    {"SETTLED"},
)
check(
    "and an unsettled one",
    trace_answers.status_words("still unmeasured, and nothing accessible gives it"),
    {"OPEN"},
)
check(
    "and both when a sentence carries both",
    trace_answers.status_words("answered, but the flatness half is still unmeasured"),
    {"SETTLED", "OPEN"},
)
check(
    "a sentence with neither is silent",
    trace_answers.status_words("the margin is 1.42x at the design point"),
    set(),
)
# "not answered" must not read as SETTLED — the negation is the whole meaning, and it is the
# phrasing `C-0071` used for a DISCHARGED question ("it is not answered; it stopped applying").
check(
    "a negated settled word is not settled",
    trace_answers.status_words("it is not answered"),
    {"OPEN"},
)
check(
    "nor is 'cannot be answered'",
    trace_answers.status_words("this cannot be answered without a measurement"),
    {"OPEN"},
)
# A DISCHARGED question is neither settled nor open and must not collide with either.
# "open SINCE iteration 3, is answered" is a duration followed by its own closure — the same
# history the open-assertion check already excludes.  Without the guard the sentence asserts both
# verdicts by itself, and every genuine contradiction it takes part in is unreadable.
check(
    "'open since ... is answered' is settled, not both",
    trace_answers.status_words("`T-45`, open since iteration 3, is answered from measurement"),
    {"SETTLED"},
)
check(
    "but a bare 'still open' beside nothing else is open",
    trace_answers.status_words("`T-63` is still open"),
    {"OPEN"},
)
check(
    "discharged is its own verdict",
    trace_answers.status_words("it is DISCHARGED, not open"),
    {"DISCHARGED"},
)

# --- challenge statuses (T-183) ------------------------------------------------------------
#
# `C-0088` scoped the self-consistency check to TASK identifiers explicitly, and `T-175` then found
# by hand that 2 of its 12 third-class instances are a CHALLENGE with two statuses: `CH-0083` read
# *open* in `ANSWERS.md`'s SS2 verdict table and `RESOLVED` twelve lines below, and BOTH halves passed
# every existing check.  The corpus carries 111 challenge files and 123 references to them in the
# deliverable, so the class is live rather than hypothetical.
#
# The design constraint is the false-positive budget, not the coverage.  Every guard the task
# checker carries has to carry over, and the challenge vocabulary adds two words that are common in
# ordinary prose -- "stands" and "raised" -- so each is admitted only where it is unambiguous.
check(
    "UPHELD is a settled verdict",
    trace_answers.status_words("`CH-0019` is UPHELD"),
    {"SETTLED"},
)
check(
    "WITHDRAWN is a settled verdict",
    trace_answers.status_words("`CH-0098` is WITHDRAWN"),
    {"SETTLED"},
)
check(
    "RESOLVED already was, and stays, settled",
    trace_answers.status_words("`CH-0003` is RESOLVED by `C-0003`"),
    {"SETTLED"},
)
# "STANDS" is the challenge vocabulary's word for a challenge that holds against the claim it
# attacks -- i.e. it has been adjudicated, which is a CLOSED state, not an open one.  It is also
# the commonest verb in this repository's prose ("the recommendation stands"), so it counts only
# when it is upper case, which is how the corpus writes a verdict.
check(
    "upper-case STANDS is a settled verdict",
    trace_answers.status_words("`CH-0100` STANDS"),
    {"SETTLED"},
)
check(
    "lower-case 'stands' in prose is not a verdict",
    trace_answers.status_words("`CH-0100`'s reading stands behind the recommendation"),
    set(),
)
# "RAISED" is the state a challenge is filed in and nothing has adjudicated yet: open.  Same
# case-sensitivity guard, because "raised by `C-0107`" is provenance and appears in almost every
# challenge reference in the deliverable.
check(
    "upper-case RAISED is open",
    trace_answers.status_words("`CH-0124` is RAISED against `C-0006`"),
    {"OPEN"},
)
check(
    "lower-case 'raised by' is provenance, not a verdict",
    trace_answers.status_words("`CH-0124`, raised by `C-0109`, moves no number"),
    set(),
)
# The negation guard has to reach the new words too, or "not upheld" reads as settled.
check(
    "'not upheld' is open, not settled",
    trace_answers.status_words("`CH-0044` was not upheld"),
    {"OPEN"},
)

# --- the reference pattern reaches challenges -----------------------------------------------
check(
    "a challenge given two verdicts is a self-contradiction",
    [(c.task, sorted(c.verdicts)) for c in trace_answers.self_contradictions(
        "`CH-0083` is still open at this point.\n"
        "Twelve lines below, `CH-0083` is RESOLVED.\n"
    )],
    [("CH-0083", ["OPEN", "SETTLED"])],
)
check(
    "a challenge merely cited asserts nothing",
    trace_answers.self_contradictions(
        "The +14.7 % collar (`CH-0026`) raises the total force.\n"
        "`CH-0026` gives 1.65 nm of collar.\n"
    ),
    [],
)
check(
    "a task and a challenge in one sentence are two separate subjects",
    sorted(c.task for c in trace_answers.self_contradictions(
        "`T-45` is answered and `CH-0083` is answered.\n"
        "`T-45` is unmeasured.\n"
    )),
    ["T-45"],
)

# --- the corpus half: a challenge's own file is the authority --------------------------------
#
# The cheap bound ran before this was written: 81 of the 111 challenge files carry a `**Status**`
# row and 30 do not, and the README index covers 65.  So the vocabulary is NOT controlled, and the
# honest check is "compare where a status is declared, report the coverage" -- an undeclared status
# returns UNKNOWN and is silent, never guessed.
check(
    "a declared UPHELD status is read out of the file body",
    trace_answers.challenge_status_of(
        "# CH-0019 -- something\n"
        "| | |\n|---|---|\n"
        "| **Against** | `C-0017` |\n"
        "| **Status** | **Upheld. The rationale identifies two expansions.** |\n"
    ),
    "CLOSED",
)
check(
    "a declared OPEN status is read out of the file body",
    trace_answers.challenge_status_of(
        "| **Status** | **OPEN, and it does not overturn a verdict.** |\n"
    ),
    "OPEN",
)
check(
    "RAISED is open",
    trace_answers.challenge_status_of("| **Status** | raised. **No number in `C-0023` moves** |\n"),
    "OPEN",
)
check(
    "a file with no Status row is UNKNOWN, never guessed",
    trace_answers.challenge_status_of("# CH-0025 -- something\n\nprose only\n"),
    "UNKNOWN",
)
check(
    "an UNKNOWN status contradicts nothing",
    trace_answers.stale_challenge_statuses(
        "`CH-0025` is still open.\n", {"CH-0025": "UNKNOWN"}
    ),
    [],
)
check(
    "an open assertion against a closed challenge is stale",
    trace_answers.stale_challenge_statuses(
        "`CH-0019` is still open.\n", {"CH-0019": "CLOSED"}
    ),
    [(1, "CH-0019", "CLOSED")],
)
check(
    "an open assertion against an open challenge is fine",
    trace_answers.stale_challenge_statuses(
        "`CH-0122` is still open.\n", {"CH-0122": "OPEN"}
    ),
    [],
)
# `C-0088`'s guard 2, carried over verbatim: a duration is not a status.
check(
    "'open since' is a duration, not an assertion, for challenges too",
    trace_answers.stale_challenge_statuses(
        "`CH-0019`, open since iteration 4, is upheld.\n", {"CH-0019": "CLOSED"}
    ),
    [],
)

# --- T-261: the CHALLENGE half's own open word ------------------------------------------------
#
# `_OPEN_WORD_ASSERTION` was written for TASK status -- `open`, `unmeasured`, `TODO` -- and
# `stale_challenge_statuses` inherited it.  A challenge's own open word is `raised`, which is in
# neither list, so a deliverable calling an UPHELD challenge *raised* was invisible: four such
# passages stood in the two documents while `tools/trace-answers.py` reported 0 defects.
#
# It needs its OWN pattern rather than a widening of the shared one, for the reason the tests
# below already pin about `_OPEN_WORD_ASSERTION` and `_OPEN_WORD_VERDICT`: `raised by` is how
# this corpus states PROVENANCE, and `TASKS.md` is full of it, so widening the shared list would
# put a provenance idiom into the task half.
check(
    "'raised' is an open assertion about a CHALLENGE",
    trace_answers.stale_challenge_statuses(
        "the coordinate is disputed (`CH-0240`, raised)\n", {"CH-0240": "CLOSED"}
    ),
    [(1, "CH-0240", "CLOSED")],
)
check(
    "'raised by' is PROVENANCE, not a status assertion",
    trace_answers.stale_challenge_statuses(
        "`CH-0240`, raised by `C-0187`, moved the coordinate\n", {"CH-0240": "CLOSED"}
    ),
    [],
)
check(
    "'raised in' and 'raised against' are provenance too",
    trace_answers.stale_challenge_statuses(
        "`CH-0240`, raised in iteration 44, and `CH-0242`, raised against `C-0154`\n",
        {"CH-0240": "CLOSED", "CH-0242": "CLOSED"},
    ),
    [],
)
check(
    "'raised' against an OPEN challenge is fine, as for every other open word",
    trace_answers.stale_challenge_statuses(
        "the coordinate is disputed (`CH-0242`, raised)\n", {"CH-0242": "OPEN"}
    ),
    [],
)
check(
    "the challenge half's word list is a SEPARATE object from the task half's",
    trace_answers._CHALLENGE_OPEN_ASSERTION.pattern
    == trace_answers._OPEN_WORD_ASSERTION.pattern,
    False,
)
check(
    "and the task half does NOT gain 'raised', because TASKS.md states provenance with it",
    [(line, task) for line, task, _ in trace_answers.open_assertions(
        "`T-296` raised by `C-0190`\n"
    )],
    [],
)

# --- T-261: the two AUDIT arms, and why neither can be a gate ---------------------------------
#
# The row asks for an arm that flags a deliverable passage citing a challenge as the SOURCE of a
# number where that challenge is adjudicated.  Measured, the naive form is ~100 % false positive
# for `CH-0230`'s reason -- a correcting sentence has to NAME the challenge in order to withdraw
# it -- so both arms ship as RESIDUE LINES, printed unconditionally and NOT counted into the exit
# code, which is `C-0129`'s policy: gate what can be made clean and print the rest beside it.
check(
    "a challenge whose Status row records an adjudication is ADJUDICATED",
    trace_answers.challenge_adjudicated("| **Status** | **UPHELD** by `C-0190` |\n"),
    True,
)
check(
    "a challenge whose Status row says only RAISED is not",
    trace_answers.challenge_adjudicated("| **Status** | **RAISED** |\n"),
    False,
)
check(
    "a Status row that says RAISED and REPAIRED IS adjudicated -- the corpus's commonest form",
    trace_answers.challenge_adjudicated(
        "| **Status** | **RAISED and REPAIRED in the same iteration** |\n"
    ),
    True,
)
check(
    "a file with no Status row is not adjudicated, and is not guessed at either",
    trace_answers.challenge_adjudicated("# CH-0025\n\nprose only\n"),
    False,
)
# --- arm 1: a claim adjudicates a challenge whose own file does not say so
check(
    "a claim binding an adjudication word to a challenge reference is an adjudication",
    trace_answers.adjudications_in_claim("C-0148.md", "**`CH-0185` is ANSWERED** -- the twelfth"),
    [("CH-0185", "C-0148.md")],
)
check(
    "and the passive form, which is how a WITHDRAWN statement is written here",
    trace_answers.adjudications_in_claim("C-0003.md", "**WITHDRAWN by `CH-0010`.** On a solved"),
    [("CH-0010", "C-0003.md")],
)
check(
    "a bare mention is not an adjudication, however close an adjudication word stands",
    trace_answers.adjudications_in_claim(
        "C-0027.md", "the axis is **WITHDRAWN**, and separately `CH-0021` exists"
    ),
    [],
)
check(
    "the adjudication must not cross a sentence boundary",
    trace_answers.adjudications_in_claim("C-0x.md", "`CH-0157`. The bracket has to be withdrawn"),
    [],
)

# --- T-298: the two exclusions, each with a named test in BOTH directions ----------------------
#
# `T-261` measured 17 unrecorded adjudications and named ONE false positive without tuning it
# away, on `C-0176`'s ground that a guard narrowed to one observed case is a test written to the
# shape of the change.  `T-298` read all 17 and found TWO that are not adjudications, and repaired
# both with the false negatives MEASURED FIRST over the whole claims corpus: 46 pattern-1 sites
# before, 43 after, and the three lost are exactly the three sites of the two exclusions.
check(
    "a CONDITIONAL is not an adjudication -- it says the verdict is not in",
    trace_answers.adjudications_in_claim(
        "C-0056.md", "If `CH-0068` is upheld, the design point is `N_ret = 56`"
    ),
    [],
)
check(
    "and the same sentence WITHOUT the conditional still is one",
    trace_answers.adjudications_in_claim(
        "C-0056.md", "`CH-0068` is upheld, so the design point is `N_ret = 56`"
    ),
    [("CH-0068", "C-0056.md")],
)
check(
    "a comma and a coordinating conjunction start a new clause with its own subject",
    trace_answers.adjudications_in_claim(
        "C-0132.md", "That is `CH-0157`, and it is why the bracket has to be withdrawn"
    ),
    [],
)
check(
    "and the same words WITHOUT the conjunction are an adjudication of the challenge",
    trace_answers.adjudications_in_claim("C-0132.md", "That is why `CH-0157` is withdrawn"),
    [("CH-0157", "C-0132.md")],
)
check(
    "a relative `, which` is NOT a clause break -- it keeps the challenge as the subject",
    trace_answers.adjudications_in_claim(
        "C-0182.md", "`CH-0229`, which raised this task, is **ANSWERED** in the half it left"
    ),
    [("CH-0229", "C-0182.md")],
)

# --- T-298: a Status row is read with its struck spans blanked ---------------------------------
#
# `C-0071`'s *strike, never delete* is how an adjudication that supersedes a filing status is
# written here, and without the blanking the discipline and the reader contradict each other:
# `CH-0224`'s cell reads `~~**OPEN.** ...~~ **RESOLVED, iteration 43**` and was reported OPEN.
check(
    "a struck OPEN with a live RESOLVED beside it is CLOSED",
    trace_answers.challenge_status_of(
        "| **Status** | ~~**OPEN.** the key is null on all of them~~ **RESOLVED** by `C-0181` |\n"
    ),
    "CLOSED",
)
check(
    "a live RAISED with a struck clause beside it is still OPEN",
    trace_answers.challenge_status_of(
        "| **Status** | **RAISED.** ~~no verdict of `C-0154` reverses~~ -- it does |\n"
    ),
    "OPEN",
)
check(
    "a struck adjudication word does not make a challenge adjudicated",
    trace_answers.challenge_adjudicated(
        "| **Status** | ~~**UPHELD**~~ **RAISED** -- the verdict was withdrawn |\n"
    ),
    False,
)
check(
    "a struck cell with nothing live left declares nothing, and is not guessed at",
    trace_answers.challenge_status_of("| **Status** | ~~**RAISED**~~ |\n"),
    "UNKNOWN",
)
check(
    "but only the CELL is blanked: a struck block around the row must not delete the row",
    trace_answers.challenge_status_of(
        "~~an earlier note\n| **Status** | **UPHELD** by `C-0190` |\nand its tail~~\n"
    ),
    "CLOSED",
)

# --- T-298: the cancellations are read on a WIDER window than the assertion --------------------
#
# `_OPEN_WINDOW = 24` binds an open word to its reference; a cancellation is bound to nothing, and
# reading it through the same 24 characters truncates the sentence that performs it.  The live
# case is `ANSWERS.md`'s *"(`CH-0083`, raised open in iteration 16 and **RESOLVED in iteration
# 17**, below)"*, which annotating `CH-0083` as the corpus says it is would have flagged.
check(
    "a challenge's own history plus its closure, further than 24 characters away, is not stale",
    trace_answers.stale_challenge_statuses(
        "**QUALIFIED, iteration 16** (`CH-0083`, raised open in iteration 16 and "
        "**RESOLVED in iteration 17**, below).\n",
        {"CH-0083": "CLOSED"},
    ),
    [],
)
check(
    "and a bare RAISED with no cancellation anywhere in the wider window still is",
    trace_answers.stale_challenge_statuses(
        "the coordinate is disputed (`CH-0083`, raised)\n", {"CH-0083": "CLOSED"}
    ),
    [(1, "CH-0083", "CLOSED")],
)
check(
    "and a cancellation about a DIFFERENT subject, past 80 characters, does not reach it",
    trace_answers.stale_challenge_statuses(
        "`T-9` is answered and the whole of the flatness question is settled, which is a "
        "statement about a task and about nothing else at all in this row -- the coordinate "
        "is disputed (`CH-0083`, raised)\n",
        {"CH-0083": "CLOSED"},
    ),
    [(1, "CH-0083", "CLOSED")],
)
# --- arm 2: a deliverable prices a number on an adjudicated challenge and names no claim
check(
    "a number attributed to an adjudicated challenge with no claim named is flagged",
    trace_answers.prices_on_adjudicated(
        "worth six cells of eight against three, decided by a 0.07 nm slack (`CH-0185`)\n",
        {"CH-0185": True},
    ),
    [(1, "CH-0185")],
)
check(
    "naming any claim in the window clears it -- a correcting sentence names its claim",
    trace_answers.prices_on_adjudicated(
        "0.07 nm (`CH-0185`) -- **RESTATED** by `C-0148`\n", {"CH-0185": True}
    ),
    [],
)
check(
    "a passage with no number at all is not a price",
    trace_answers.prices_on_adjudicated("see `CH-0185` below\n", {"CH-0185": True}),
    [],
)
check(
    "an unadjudicated challenge is not flagged, however priced",
    trace_answers.prices_on_adjudicated(
        "worth 0.07 nm (`CH-0242`)\n", {"CH-0242": False}
    ),
    [],
)
check(
    "struck text is blanked first, as in every other arm",
    trace_answers.prices_on_adjudicated(
        "~~worth 0.07 nm (`CH-0185`)~~\n", {"CH-0185": True}
    ),
    [],
)

# --- the two guards the real deliverable forced (T-183) ---------------------------------------
#
# Both were written after running the extension against the committed `ANSWERS.md`, which fired
# once -- on `CH-0083`, wrongly.  The task's own falsifier is that a single false positive means
# the extension is not shipped, so each is fixed here and pinned by a test.

# Guard 1: a verdict attaches to the identifier it is NEAR.  A Markdown table row is one line
# carrying several independent statements, and whole-sentence attribution read SS6 task 4's
# DISCHARGED onto the challenge named 180 characters later in the same cell.
check(
    "a verdict far from its subject in one table row does not attach",
    trace_answers.self_contradictions(
        "| 4 | **PASS**, and now **DISCHARGED FOR THE RECOMMENDED DEVICE** -- `C-0018` searched "
        "the affine mandate's load line and `C-0032` a strain-softening flexure's, and `CH-0083` "
        "charged that neither is the recommended one |\n"
        "`CH-0083` is RESOLVED.\n"
    ),
    [],
)
check(
    "but a verdict beside its subject still attaches",
    [(c.task, sorted(c.verdicts)) for c in trace_answers.self_contradictions(
        "`CH-0083` is unresolved.\n`CH-0083` is RESOLVED.\n"
    )],
    [("CH-0083", ["OPEN", "SETTLED"])],
)

# Guard 2: `C-0088`'s duration guard, in the phrasing the queue uses only for challenges.
check(
    "'raised open in iteration 16 and RESOLVED in 17' is history, not a contradiction",
    trace_answers.self_contradictions(
        "(`CH-0083`, raised open in iteration 16 and **RESOLVED in iteration 17**, below).\n"
    ),
    [],
)
check(
    "and the original 'open since' phrasing still is",
    trace_answers.self_contradictions(
        "`T-45`, open since iteration 3, is answered from measurement.\n"
    ),
    [],
)

# --- the shadowed regex T-183 found (and what it was worth) -----------------------------------
#
# `_OPEN_WORD` was declared TWICE at module level, so the second silently shadowed the first and
# `open_assertions` ran on the self-consistency check's wider verdict list.  Both give 0 on the
# committed deliverable, so nothing published moved -- but the two are now named apart and the
# assertion check keeps the wider list DELIBERATELY, because it contains the word of `C-0080`'s
# own live instance.
check(
    "the assertion check sees 'unmeasured', which is C-0080's own live phrasing",
    [(line, task) for line, task, _ in trace_answers.open_assertions(
        "(`T-45` is still unmeasured)\n"
    )],
    [(1, "T-45")],
)
check(
    "and it still sees the narrow list's own words",
    [(line, task) for line, task, _ in trace_answers.open_assertions(
        "`T-63` is still to do\n"
    )],
    [(1, "T-63")],
)
check(
    "the two word lists are separate objects, not one shadowing the other",
    trace_answers._OPEN_WORD_ASSERTION.pattern == trace_answers._OPEN_WORD_VERDICT.pattern,
    False,
)

# --- T-184: the tracer's document set ------------------------------------------------------
#
# `C-0067` built this tool for `ANSWERS.md` and four checks accumulated on it, while
# `DECISIONS-FOR-NDI.md` -- the document NDI actually reads -- was checked by nothing at all.
# `T-184` found 23 assertions in it needing an edit, and the tool needed no new logic to find
# the mechanical ones: it needed a DEFAULT.  These checks pin that default, because a default
# is exactly the kind of thing a later edit silently narrows back.
check(
    "the deliverable is in the tracer's default document set",
    "ANSWERS.md" in trace_answers.DEFAULT_DOCUMENTS,
    True,
)
check(
    "and so is the decision file, which is the document NDI reads",
    "DECISIONS-FOR-NDI.md" in trace_answers.DEFAULT_DOCUMENTS,
    True,
)
check(
    "the default set is exactly the two outward-facing documents",
    len(trace_answers.DEFAULT_DOCUMENTS),
    2,
)
check(
    "an explicit --answers still names one document, so the old invocation is unchanged",
    trace_answers.parse_arguments(["--answers", "X.md"]).answers,
    ["X.md"],
)
check(
    "and several may be named at once",
    trace_answers.parse_arguments(["--answers", "X.md", "Y.md"]).answers,
    ["X.md", "Y.md"],
)
check(
    "with no --answers the default set is used",
    trace_answers.parse_arguments([]).answers,
    trace_answers.DEFAULT_DOCUMENTS,
)

# --- T-184: struck text is withdrawn, not asserted -----------------------------------------
#
# `C-0071`'s standing discipline is *strike, never delete* -- a list that only ever grows is not
# a record and one that silently shrinks is worse -- so every correct repair in this repository
# leaves the withdrawn sentence in place inside `~~ ~~`.  The status checks read it as a live
# assertion, which means the checker PENALISES the discipline the project mandates: repairing a
# stale *"`T-191` is open"* by striking it leaves the flag exactly where it was.  Found by
# `T-184` while repairing `DECISIONS-FOR-NDI.md`.  Line numbers must survive the strip, or every
# reported line is wrong below the first strikethrough.
check(
    "struck text is blanked",
    trace_answers.strip_struck("keep ~~drop~~ keep"),
    "keep" + " " * 10 + "keep",
)
check(
    "and the line length is preserved, so column offsets do not move",
    len(trace_answers.strip_struck("keep ~~drop~~ keep")),
    len("keep ~~drop~~ keep"),
)
check(
    "a multi-line strike is blanked and its newlines survive",
    trace_answers.strip_struck("a ~~b\nc~~ d").splitlines(),
    ["a" + " " * 4, " " * 4 + "d"],
)
check(
    "an unclosed ~~ is left alone, because it is not a strike",
    trace_answers.strip_struck("a ~~b and no end"),
    "a ~~b and no end",
)
check(
    "text with no strike is returned unchanged",
    trace_answers.strip_struck("nothing to do here"),
    "nothing to do here",
)
check(
    "a struck open-assertion is not asserted",
    trace_answers.open_assertions("~~whether it is flat is `T-129`, open.~~"),
    [],
)
check(
    "and an unstruck one on the same line still is",
    [task for _, task, _ in trace_answers.open_assertions(
        "~~`T-129`, open~~ and separately `T-45` is open\n"
    )],
    ["T-45"],
)
check(
    "the line number of an assertion below a multi-line strike is right",
    [line for line, _, _ in trace_answers.open_assertions(
        "~~a\nb~~\nwhether it is flat is `T-129`, open.\n"
    )],
    [3],
)
check(
    "a struck task status cannot be contradicted by the queue",
    trace_answers.stale_statuses(
        "~~and `T-95` is open~~\n",
        "| T-95 | q | a | A8.2 | **DISCHARGED** by `C-0071` |\n",
    ),
    [],
)

check(
    "a struck verdict cannot contradict an unstruck one -- struck text is withdrawn",
    trace_answers.self_contradictions(
        "~~`T-45` is still unmeasured~~ and `T-45` is answered from published measurement\n"
    ),
    [],
)
check(
    "a struck challenge assertion is not asserted either",
    trace_answers.stale_challenge_statuses(
        "~~`CH-0019` is still open~~\n", {"CH-0019": "UPHELD"}
    ),
    [],
)

check(
    "a struck number needs no provenance -- `~~` means withdrawn everywhere in the tool",
    trace_answers.trace("a paragraph citing `C-0001` with ~~42.42~~ in it", _sources),
    [],
)

# --- the vocabulary gap T-195 found, hours after T-184 shipped ------------------------------
#
# `C-0124` corrected `DECISIONS-FOR-NDI.md` to say *"`T-195` ... is the one still `TODO`"*, and
# `T-195` closed the same iteration. The checker reported **0 contradicted assertions** throughout,
# because the queue's own status word -- `TODO` -- was not in the DOCUMENT-side vocabulary. The
# tool knew how to read `TODO` in `TASKS.md` and not how to read a document asserting it.
#
# That is the same shape as `CLAUDE.md`'s "a status vocabulary GROWS, and every word your checker
# does not know is silently read as OPEN" -- inverted: here the unknown word was read as NOTHING,
# so a stale assertion passed. Both failures are silent and this one is the costlier direction.
check(
    "'still TODO' is an open assertion",
    [(line, task) for line, task, _ in trace_answers.open_assertions(
        "`T-195` is the one still `TODO`, and this file never named it\n"
    )],
    [(1, "T-195")],
)
check(
    "and so is a bare TODO beside the id",
    [(line, task) for line, task, _ in trace_answers.open_assertions(
        "`T-50` remains TODO\n"
    )],
    [(1, "T-50")],
)
check(
    "but 'was TODO until' is history, not an assertion",
    trace_answers.open_assertions("`T-195` was TODO until iteration 27, and is answered\n"),
    [],
)
check(
    "TODO is a status word for the self-consistency check too",
    trace_answers.status_words("`T-195` is still TODO"),
    {"OPEN"},
)

# --- P-29: the exit code must carry the TASK-status check ------------------------------------
#
# `C-0173` wired this checker into `tools/verify.sh` under `set -euo pipefail`, which makes the
# exit code the whole of the wiring.  Three of its four checks fed that code and the fourth --
# the task-status contradiction, which is `C-0067`/`C-0078`/`C-0088`'s entire class -- was printed
# and dropped.  The two `STALE-OPEN` lines are printed with the SAME tag, one failing the build and
# one not, so the output could not tell them apart either.  These run the tool end to end, because
# the defect was in the return statement and no unit test of a predicate can reach it.

import subprocess
import tempfile

_TOOL = os.path.join(os.path.dirname(os.path.abspath(__file__)), "trace-answers.py")


def _run_tool(answers, queue):
    """(exit code, stdout) for the tool over a synthetic one-line deliverable and queue."""
    directory = tempfile.mkdtemp(prefix="trace-answers-exit.")
    try:
        answers_path = os.path.join(directory, "ANSWERS.md")
        queue_path = os.path.join(directory, "TASKS.md")
        open(answers_path, "w", encoding="utf-8").write(answers)
        open(queue_path, "w", encoding="utf-8").write(queue)
        # `T-298`.  The claims and challenges directories are pointed at EMPTY ones, so this
        # helper's exit code is a statement about the synthetic document and nothing else.  With
        # the defaults it read the live corpus, and once `T-298` counted the
        # UNRECORDED-ADJUDICATION residue into the exit code that made a hermetic test depend on
        # a mutable artifact -- `CLAUDE.md`'s own *a self-test that reads a mutable artifact
        # expires the moment the corpus moves*, met from the other side: here the corpus moving
        # made a PASSING test fail while nothing it asserts had changed.
        empty_claims = os.path.join(directory, "claims")
        empty_challenges = os.path.join(directory, "challenges")
        os.mkdir(empty_claims)
        os.mkdir(empty_challenges)
        run = subprocess.run(
            [
                sys.executable, _TOOL, "--answers", answers_path, "--queue", queue_path,
                "--claims", empty_claims, "--challenges", empty_challenges,
            ],
            capture_output=True,
            text=True,
        )
        return run.returncode, run.stdout
    finally:
        shutil.rmtree(directory, ignore_errors=True)


_AGREES = _run_tool(
    "`T-9` is still open.\n",
    "| T-9 | subject | goal | leaf | **PARTIALLY DONE** (iteration 35) |\n",
)
check("a deliverable that AGREES with the queue exits 0", _AGREES[0], 0)

_CONTRADICTS = _run_tool(
    "`T-9` is still open.\n",
    "| T-9 | subject | goal | leaf | **DONE** (iteration 41) |\n",
)
check(
    "a deliverable CONTRADICTED by the queue exits non-zero",
    _CONTRADICTS[0] > 0,
    True,
)
check(
    "and it says which task",
    "STALE-OPEN\tT-9\tCLOSED" in _CONTRADICTS[1],
    True,
)

def _run_corpus(claim, challenge_status):
    """(exit code, stdout) over a synthetic one-claim, one-challenge corpus.  `T-298`."""
    directory = tempfile.mkdtemp(prefix="trace-answers-corpus.")
    try:
        claims = os.path.join(directory, "claims")
        challenges = os.path.join(directory, "challenges")
        os.mkdir(claims)
        os.mkdir(challenges)
        open(os.path.join(claims, "C-0001-x.md"), "w", encoding="utf-8").write(claim)
        open(os.path.join(challenges, "CH-0001-x.md"), "w", encoding="utf-8").write(
            "| **Status** | {} |\n".format(challenge_status)
        )
        answers = os.path.join(directory, "ANSWERS.md")
        queue = os.path.join(directory, "TASKS.md")
        open(answers, "w", encoding="utf-8").write("nothing to trace here.\n")
        open(queue, "w", encoding="utf-8").write("")
        run = subprocess.run(
            [
                sys.executable, _TOOL, "--answers", answers, "--queue", queue,
                "--claims", claims, "--challenges", challenges,
            ],
            capture_output=True,
            text=True,
        )
        return run.returncode, run.stdout
    finally:
        shutil.rmtree(directory, ignore_errors=True)


# `T-298` promoted `T-261`'s second residue to a gate.  `C-0173`/`P-29`'s lesson is that a gate is
# a claim about a corpus and is discharged by RUNNING it, so the exit code gets its own test in
# both directions -- and `C-0177`'s, that a gate which cannot fail is not a gate.
_UNRECORDED = _run_corpus("**`CH-0001` is ANSWERED** by this claim.\n", "**RAISED**")
check("an UNRECORDED adjudication exits non-zero", _UNRECORDED[0] > 0, True)
check(
    "and it names the challenge and the claim",
    "UNRECORDED-ADJUDICATION\tCH-0001\tC-0001-x.md" in _UNRECORDED[1],
    True,
)
_RECORDED = _run_corpus("**`CH-0001` is ANSWERED** by this claim.\n", "**ANSWERED** by `C-0001`")
check("and the same corpus with the annotation present exits 0", _RECORDED[0], 0)

check(
    "an ABSENT number exits non-zero too -- all four checks reach the code",
    _run_tool("The layer stiffness is 123.456789012 pN/nm.\n", "")[0] > 0,
    True,
)

check(
    "a SELF-CONTRADICTION exits non-zero",
    _run_tool(
        "`T-9` is answered.\n\nElsewhere: `T-9` is still open.\n",
        "",
    )[0] > 0,
    True,
)

# `sys.exit(n)` truncates modulo 256, so a raw defect COUNT of exactly 256 exits 0 and reads as a
# clean corpus.  The counts belong in the output; the exit code is a boolean.
_MANY = _run_tool(
    "".join("Value {}.{:09d} appears here.\n".format(i, i) for i in range(300)),
    "",
)
check("300 defects still exit non-zero (256 would truncate to 0)", _MANY[0] > 0, True)
check("and the exit code is exactly 1, not the count", _MANY[0], 1)

# --- `CH-0269`: a link TARGET is a filename, and a filename asserts nothing ------------------
#
# This corpus names a claim after its subject, so 33 of its filenames carry a settled word --
# twelve of them the `*-answers-synthesis.md` family -- and 2 an open one.  Reading a target as
# prose made CITING a synthesis claim beside an open task a self-contradiction, which is exactly
# what a synthesis pass writes.  Both directions are pinned: the target must not assert, and the
# LABEL and surrounding prose must still assert.

check(
    "a settled word inside a LINK TARGET is not a verdict",
    trace_answers.self_contradictions(
        "See ([`C-0191`](gpd/claims/C-0191-thirteenth-answers-synthesis.md), `T-999`) "
        "and its price line is left open.\n"
    ),
    [],
)

check(
    "an open word inside a LINK TARGET is not an assertion either",
    trace_answers.open_assertions(
        "`T-999` is answered by [`C-0197`](gpd/claims/C-0197-the-challenge-halfs-own-open-word.md).\n"
    ),
    [],
)

check(
    "a settled word in PROSE still contradicts an open one",
    [c.task for c in trace_answers.self_contradictions(
        "`T-999` is answered, and its price line is left open.\n"
    )],
    ["T-999"],
)

check(
    "a settled word in a link LABEL still asserts, because a label is prose",
    [c.task for c in trace_answers.self_contradictions(
        "`T-999` is [answered](gpd/claims/C-0001-x.md), and it is left open.\n"
    )],
    ["T-999"],
)

check(
    "blanking a link target preserves length, so reported line and column do not move",
    len(trace_answers.blank_link_targets("a [b](c/d-e.md) f")),
    len("a [b](c/d-e.md) f"),
)

check(
    "blanking a link target preserves line breaks",
    trace_answers.blank_link_targets("a [b](c.md)\nx [y](z.md)\n").count("\n"),
    2,
)

check(
    "a bare parenthesis that is not a link target is untouched",
    trace_answers.blank_link_targets("the answer (see below) is open"),
    "the answer (see below) is open",
)

# --- summary -------------------------------------------------------------------------------
if _failures:
    print("\n{} check(s) FAILED".format(len(_failures)))
    sys.exit(1)
print("\nall checks passed")
