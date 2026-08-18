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

# --- summary -------------------------------------------------------------------------------
if _failures:
    print("\n{} check(s) FAILED".format(len(_failures)))
    sys.exit(1)
print("\nall checks passed")
