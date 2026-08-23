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
# Trace every number in ANSWERS.md back to the claim that owns it (task T-131).
#
#     tools/trace-answers.py [--answers DOC [DOC ...]] [--claims gpd/claims] [--challenges gpd/challenges]
#
# With no --answers it checks DEFAULT_DOCUMENTS: ANSWERS.md and DECISIONS-FOR-NDI.md (T-184).
#
# ANSWERS.md is the repository's primary deliverable and is explicitly "a synthesis, not a
# source": every number in it is supposed to belong to a claim.  Nothing checked that.  This
# script is the mechanical half of the check — it cannot decide whether a number MEANS what
# ANSWERS.md says it means, but it can decide whether the number appears anywhere in the claim
# the passage cites, which is where the drift shows up.
#
# Output is one record per numeric token, tab-separated, with a status:
#
#   CITED     the token appears in a claim/challenge the same block cites   (traced)
#   ELSEWHERE the token appears in some claim/challenge, but not a cited one (check by hand)
#   ABSENT    the token appears in no claim or challenge at all             (untraceable)
#
# The three statuses are deliberately coarse.  ABSENT is the interesting one and it is not
# automatically an error: arithmetic performed IN the synthesis (a ratio of two claim numbers,
# a percentage) is legitimately absent, so every ABSENT is adjudicated by hand.  What the tool
# guarantees is that none of them is adjudicated by ACCIDENT.
#
# Verified by tools/test-trace-answers.py.
import argparse
import os
import re
import sys
from collections import namedtuple

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
# `P-30`: the "what is this row's verdict" predicate lives in ONE module, which this reader and
# `tools/check-queue-vocabulary.py` both import.  The dependency runs one way -- the gate imports
# the reader, so the shared predicate cannot live in the gate without making the reader depend on
# the thing that checks it.
import queue_verdicts as _verdicts

# ANSWERS.md is typographically rich: en dashes for ranges, U+2212 for minus, thin spaces.
# Every comparison below runs on the normalised form, on both sides.
_DASHES = {
    "–": "-",  # en dash
    "—": "-",  # em dash
    "−": "-",  # minus sign
    " ": " ",  # nbsp
    " ": " ",  # narrow nbsp
    " ": " ",  # thin space
    "×": "x",  # multiplication sign
}

# Identifiers that LOOK numeric and are not quantities.  Stripped before tokenising.
_ID_PATTERNS = [
    re.compile(r"\bCH-\d{4}\b"),
    re.compile(r"\bC-\d{4}\b"),
    re.compile(r"\bP-\d{1,4}\b"),
    re.compile(r"\bT-\d{1,4}[a-z]?\b"),
    re.compile(r"\bS-\d{1,4}\b"),
    re.compile(r"\bA\d(?:\.\d)?\b"),  # NDI leaf IDs: A2.1, A8.2
    re.compile(r"§\s*\d(?:\([a-z]\))?"),  # section references
    re.compile(r"\bTRL\s*\d(?:\s*-\s*\d)?\b"),
    re.compile(r"\b\d{4}-\d{2}-\d{2}\b"),  # dates
    re.compile(r"\(\d{4}\)"),  # citation years
    re.compile(r"\bhttps?://\S+"),
    re.compile(r"\]\([^)]*\)"),  # markdown link targets
]

# The documents this tracer checks by default.  `C-0067` built it for `ANSWERS.md`; `T-184`
# found that `DECISIONS-FOR-NDI.md` -- the document NDI actually reads -- was checked by nothing
# at all, and that the tool needed no new logic to reach it, only a default.  Both are outward
# facing and both are syntheses rather than sources, which is the property every check here
# assumes.  Pinned by tools/test-trace-answers.py.
DEFAULT_DOCUMENTS = ["ANSWERS.md", "DECISIONS-FOR-NDI.md"]
# `TOOLING-NOVELTY.md` is a THIRD outward-facing document (iteration 37) and is DELIBERATELY
# NOT in this list, which is the opposite of the `T-184` decision one file over in
# `check-corpus-links.py`. The two gates want different things. Its CLAIM SLUGS are corpus
# references and belong in the link gate; its NUMBERS are not this corpus's at all — it is a
# survey of external tooling, so its tokens are arXiv identifiers and reference numbers, and
# the numeric half reports **9 ABSENT on one line of citations**. Wiring it here would buy nine
# permanent false positives, and `CLAUDE.md` is explicit that a drift checker's FALSE
# positives cost more than its true ones. Run it by hand with `--answers` if the document ever
# starts quoting this repository's own numbers; the STATUS half already reads clean there.


_NUMBER = re.compile(r"\d+(?:\.\d+)?(?:e-?\d+)?")

_CITATION = re.compile(r"\b(CH-\d{4}|C-\d{4})\b")


def normalise(text):
    """Fold the typography ANSWERS.md and the claims use inconsistently."""
    for src, dst in _DASHES.items():
        text = text.replace(src, dst)
    return text


def strip_identifiers(text):
    """Remove claim/task/leaf/section IDs so their digits are not read as quantities."""
    for pattern in _ID_PATTERNS:
        text = pattern.sub(" ", text)
    return text


def tokens(text, min_digits=2):
    """The numeric tokens of a passage, in order, deduplicated.

    `min_digits` drops bare small integers ("two", "3 nm" is kept as 3 only if
    min_digits <= 1).  Counts like 45 and 16 have two digits and are kept; a lone
    "1" or "5" carries no tracing signal and is dropped by default.
    """
    stripped = strip_identifiers(normalise(text))
    seen = []
    for match in _NUMBER.finditer(stripped):
        token = match.group(0)
        digits = sum(1 for character in token if character.isdigit())
        if digits < min_digits:
            continue
        if token not in seen:
            seen.append(token)
    return seen


def citations(text):
    """The claim and challenge IDs a passage names."""
    seen = []
    for match in _CITATION.finditer(text):
        if match.group(1) not in seen:
            seen.append(match.group(1))
    return seen


def load_sources(*directories):
    """{ID: normalised text} for every claim and challenge file."""
    sources = {}
    for directory in directories:
        if not os.path.isdir(directory):
            continue
        for name in sorted(os.listdir(directory)):
            if not name.endswith(".md"):
                continue
            identifier = name.split("-")
            if name.startswith("CH-"):
                key = "-".join(identifier[:2])
            elif name.startswith("C-"):
                key = "-".join(identifier[:2])
            else:
                continue
            with open(os.path.join(directory, name), encoding="utf-8") as handle:
                sources[key] = normalise(handle.read())
    return sources


def contains(source_text, token):
    """Whether a claim states this token, as a number rather than as a substring.

    "45" must not match inside "1.45" or "450"; the guard is that the character
    either side of the match is not a digit and not a decimal point.
    """
    for match in re.finditer(re.escape(token), source_text):
        start, end = match.start(), match.end()
        before = source_text[start - 1] if start > 0 else " "
        after = source_text[end] if end < len(source_text) else " "
        if before.isdigit() or before == ".":
            continue
        if after.isdigit() or after == ".":
            continue
        return True
    return False


def blocks(answers_text):
    """Split ANSWERS.md into blocks: table rows one each, paragraphs one each."""
    result = []
    paragraph = []
    for number, line in enumerate(answers_text.splitlines(), start=1):
        if line.startswith("|"):
            if paragraph:
                result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
                paragraph = []
            result.append((number, line))
        elif line.strip() == "":
            if paragraph:
                result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
                paragraph = []
        else:
            paragraph.append((number, line))
    if paragraph:
        result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
    return result


# --- status drift ----------------------------------------------------------------------------
#
# The numeric tracer above cannot see the drift class `C-0067` found to be the worst: an entry
# of "what we cannot answer" that the programme HAS answered.  A stale "`T-129`, open" contains
# no number, so no numeric check can reach it, and a reviewer's instinct is to check assertions
# rather than disclaimers — which is why three of them stood for up to seven iterations.
#
# The check below is mechanical and one-directional: it finds every place the deliverable
# ASSERTS a task is open, and asks `TASKS.md` — the register that knows — whether it is.  It
# deliberately does not try the converse (a task closed in the deliverable and open in the
# queue), because a synthesis is entitled to summarise a partial answer.

# A queue row is `| T-129 | task | acceptance | leaf | status |`.  The pattern lives in
# `tools/queue_verdicts.py` (`P-30`) because the vocabulary gate has to share it, and it is
# deliberately tolerant of a MISSING TRAILING PIPE: this pattern used to require one, GFM does not,
# and one committed row omits it -- so that row was invisible to this reader entirely, 271 of 272.
_QUEUE_ROW = _verdicts.TASK_ROW

# Words in a status cell that mean the task is no longer open.
#
# THE VOCABULARY GROWS, and every word missing from this tuple is silently read as OPEN — so a
# closed task keeps reading open, which is the exact drift this checker exists to catch.
# `SESSION-PROMPT.md` declares DONE and KILLED; iteration 14 wrote ANSWERED for `T-45`; iteration
# 17 wrote DISCHARGED for `T-95`/`T-102`, a status that is neither "answered" nor "abandoned" but
# **stopped applying** — a distinction `C-0071` had to invent and that this project needs, because
# a question raised by a branch that was later removed is not a question anybody owes an answer to.
# Add the word here, with a test, whenever the queue coins one.
#
# Matched CASE-SENSITIVELY and on WHOLE WORDS, because the queue writes its verdicts in bold
# upper case and its prose in lower case, and two substring traps are live in the real file:
# "Left undone" contains DONE, and several rows discuss having "answered" something in passing.
# Upper-casing the row before matching — the obvious implementation — closes both of them.
#
# `PARTIALLY DONE` is iteration 35's coinage (`T-9`, `C-0157`) and it is a NEGATIVE: a task whose
# deliverable list is partly discharged and partly live.  It has to be excluded explicitly, because
# the failure direction here is the unsafe one — the row contains a closing word, so without the
# guard a genuinely open item disappears from the register, which is `CLAUDE.md`'s own "a closing
# word about another task closes the row it sits in" met from the inside of one row.  Any further
# qualifier the queue coins belongs in `_NOT_CLOSED_QUALIFIER`, with a test, the day it is written.
# `P-30`: ONE definition, in `tools/queue_verdicts.py`, so that the reader and the gate cannot
# disagree about what a closing word is.  It was written out twice here and once there.
_NOT_CLOSED_QUALIFIER = _verdicts.NOT_CLOSED_QUALIFIER
_CLOSED = _verdicts.UNQUALIFIED_CLOSING_WORD
_IN_PROGRESS = re.compile(r"\bIN PROGRESS\b")


def queue_status(queue_text):
    """{task ID: OPEN | CLOSED | IN PROGRESS} read out of TASKS.md's own rows.

    The row's FIRST LEADING VERDICT decides (`P-30`).  A verdict is a run that OPENS a cell —
    a short bold run carrying a closing word, or a `TODO` — and the leftmost one is the live one,
    because the queue writes a new verdict into an earlier cell and PRESERVES the original
    `TODO — **PRIORITY**` note in a later one.  Nine committed rows are that shape.

    This used to be a scan of the WHOLE row after the identifier, and that scan closed four open
    rows on a closing word that was not about the task at all: the row's own title, a challenge,
    a deliverable, and a candidate of a remedy.  A row that carries no leading verdict at all —
    the oldest rows are written `| P-1 | ... | DONE | Iteration 1 |` — still falls back to it.
    `TODO` is not required: on the fallback path, absence of a closing word IS open.
    """
    statuses = {}
    for line in queue_text.splitlines():
        match = _QUEUE_ROW.match(line.strip())
        if not match:
            continue
        identifier, rest = match.group(1), match.group(2)
        verdicts = _verdicts.row_verdicts(rest)
        if _IN_PROGRESS.search(rest):
            statuses[identifier] = "IN PROGRESS"
        elif verdicts:
            statuses[identifier] = verdicts[0][1]
        elif _CLOSED.search(rest):
            statuses[identifier] = "CLOSED"
        else:
            statuses[identifier] = "OPEN"
    return statuses


# The phrasings the deliverable uses to assert a task is still open.  The task ID and the word
# must be within a short window of each other, so that "settled by `T-129`" three clauses away
# from an unrelated "open" is not a hit.
_OPEN_WINDOW = 24
# `T-183` found this declared TWICE at module level -- here, and again below for the
# self-consistency check -- so the second silently shadowed the first and `open_assertions` has
# been running on the WIDER verdict list since `C-0080` wrote it.  Both give 0 on the committed
# `ANSWERS.md`, so nothing published moves; but the shadow is not inert in principle, and the
# direction it happens to run is FAVOURABLE -- the wider list contains "unmeasured", which is the
# exact word of `C-0080`'s own live instance (*"(`T-45` is still unmeasured)"*).  So the two are
# now named apart and the assertion check keeps the wider list DELIBERATELY, with a test on each.
# Same family as `CLAUDE.md`'s Kotlin note that a `private` top-level declaration does not scope to
# its file: a redeclaration is silent in both languages and the compiler is the only difference.
_OPEN_WORD_ASSERTION = re.compile(
    r"\b(open|unmeasured|unanswered|unresolved|undetermined|still\s+to\s+do|still\s+missing"
    r"|not\s+yet\s+answered|not\s+determined|TODO)\b",
    re.IGNORECASE,
)
#: `T-261`.  The CHALLENGE half's own open word, and it is NOT in the task list above.
#
# `_OPEN_WORD_ASSERTION` was written for a TASK's status vocabulary -- `open`, `unmeasured`,
# `TODO` -- and `stale_challenge_statuses` inherited it wholesale.  A challenge's own open state
# is **RAISED**, which appears in neither list, so a deliverable calling an UPHELD challenge
# *raised* was invisible to a wired gate: four such passages stood in the two documents at
# iteration 46 while the tool reported `0 open assertion(s) contradicted`.
#
# A SEPARATE object rather than a widening of the shared one, for the reason `T-183` already
# pinned about `_OPEN_WORD_ASSERTION` and `_OPEN_WORD_VERDICT`: `raised by` is how this corpus
# states a challenge's PROVENANCE and `TASKS.md` is full of it, so widening the shared list would
# put a provenance idiom into the task half and manufacture false positives there.
#
# The guard is the provenance idiom itself.  Measured over the two deliverables, `raised` near a
# reference to a CLOSED challenge fires four times and the guard removes none of them -- it is
# precautionary, and it is what keeps the predicate honest as the corpus grows.
_CHALLENGE_RAISED = r"\braised\b(?!\s+(?:by|in|as|at|and|against))"
_CHALLENGE_OPEN_ASSERTION = re.compile(
    "(?:" + _OPEN_WORD_ASSERTION.pattern + ")|(?:" + _CHALLENGE_RAISED + ")",
    re.IGNORECASE,
)
_TASK_REFERENCE = re.compile(r"`(T-\d{1,4}[a-z]?|P-\d{1,4})`")

# `T-183`.  The self-consistency check needs to reach CHALLENGE identifiers too: `C-0088` scoped
# the whole class to task ids explicitly, and `T-175` then found by hand that 2 of its 12
# third-class instances are a challenge with two statuses -- `CH-0083` read *open* in the SS2
# verdict table and `RESOLVED` twelve lines below, and both halves passed every check here.  The
# corpus carries 111 challenge files and the deliverable makes 123 references to them.
#
# It is deliberately a SECOND pattern rather than a widening of the first: `open_assertions`'s
# corpus authority is `TASKS.md`, which has no challenge rows, so widening `_TASK_REFERENCE` would
# make every challenge reference an UNKNOWN lookup in a check that cannot adjudicate it.
_SUBJECT_REFERENCE = re.compile(r"`(T-\d{1,4}[a-z]?|P-\d{1,4}|CH-\d{1,4})`")

# Two cancellations, both found by running this against the real deliverable.  "open SINCE
# iteration 3" is a duration — a statement about how long a task WAS open, which the very same
# sentence then closes — and an answering word anywhere in the window means the passage is
# reporting the closure rather than asserting the gap.  A false positive here sends an agent to
# "correct" a passage that is already right, which is the failure a drift checker can least
# afford: the tool exists in order to be believed.
# `T-183` widened this by one alternative.  `ANSWERS.md` writes a challenge's history as
# *"raised open in iteration 16 and **RESOLVED in iteration 17**"* -- a duration and its own
# closure in one clause, which is `C-0088`'s guard 2 in a phrasing the queue only uses for
# challenges.  Without it that sentence asserts both verdicts by itself and every genuine
# contradiction `CH-0083` takes part in becomes unreadable.
_HISTORICAL = re.compile(
    r"\b(open\s+(since|for|from|in\s+iteration)|was\s+(open|TODO)\s+(since|until|for))\b",
    re.IGNORECASE,
)
_ANSWERING = re.compile(
    r"\b(answered|answers|resolved|resolves|closed|closes|settled|settles|discharged)\b",
    re.IGNORECASE,
)


# `C-0071`'s discipline is *strike, never delete*, so a withdrawn statement stays in the file
# inside `~~ ~~`.  The three STATUS checks below must therefore not read it: otherwise repairing a
# stale *"`T-191` is open"* by striking it -- the only repair this project permits -- leaves the
# flag exactly where it was, and the checker penalises the discipline it exists to support.
# `T-184` found this while repairing `DECISIONS-FOR-NDI.md`, where every correction is a strike.
#
# The blanking preserves length and newlines, so every reported LINE NUMBER is still the file's.
# The NUMERIC trace uses it too, for the same reason: `~~` means WITHDRAWN, and a withdrawn number
# is exactly the one a repair should not have to keep traceable.  `T-184` found the live case -- a
# cheap bound of `9.61 nm` superseded by `C-0109`'s measured 9.608: struck, correct, and ABSENT
# forever, because the claim owns the successor and not the value it replaced.
_STRUCK = re.compile(r"~~.+?~~", re.DOTALL)


def strip_struck(text):
    """Blank every `~~struck~~` span, preserving length and line breaks."""

    def blank(match):
        return "".join("\n" if character == "\n" else " " for character in match.group(0))

    return _STRUCK.sub(blank, text)


def open_assertions(answers_text):
    """[(line, task, phrase)] for every place the text asserts a task is open."""
    found = []
    for number, line in enumerate(strip_struck(answers_text).splitlines(), start=1):
        for reference in _TASK_REFERENCE.finditer(line):
            task = reference.group(1)
            start = max(0, reference.start() - _OPEN_WINDOW)
            end = min(len(line), reference.end() + _OPEN_WINDOW)
            window = line[start:end]
            word = _OPEN_WORD_ASSERTION.search(window)
            if not word:
                continue
            if _HISTORICAL.search(window) or _ANSWERING.search(window):
                continue
            # The word must FOLLOW the reference or sit immediately before it; "settled by
            # `T-129` … and the electrode question is open" is excluded by the window, and
            # "open questions: `T-95`" by neither, which is the intent.
            found.append((number, task, window.strip()))
    return found


def stale_statuses(answers_text, queue_text):
    """[(line, task, queue status)] for every open assertion the queue contradicts."""
    statuses = queue_status(queue_text)
    stale = []
    for line, task, _phrase in open_assertions(answers_text):
        status = statuses.get(task, "UNKNOWN")
        if status in ("CLOSED", "IN PROGRESS"):
            stale.append((line, task, status))
    return stale


# --- self-consistency: does the deliverable agree with ITSELF? ---------------------------------
#
# `C-0080` found a THIRD drift class and, with it, the blind spot both checks above share.  They
# compare the deliverable against the CORPUS — is this number owned, is this open assertion still
# true.  Neither can see a document that contradicts ITSELF.  The live instance: `ANSWERS.md`
# called `T-45` *"answered from published measurement — and the answer is a failure"* in §1 and
# *"(`T-45` is still unmeasured)"* in §3, and BOTH halves passed, because §1's sentence has an
# owner and reads CITED while §3's parenthesis carries no number at all and no "open" either.
#
# So this third check compares the document with itself, per task ID.  It is deliberately
# one-sided in what counts as evidence: only an explicit status word does, so a task that is
# merely cited stays silent.  A false positive here would send an agent to "reconcile" two
# sentences that are both correct, which is the failure a drift checker can least afford.

# `T-183`.  A verdict attaches to the identifier it is NEAR, not to every identifier in the
# sentence.  The original unit was the whole sentence, and a Markdown TABLE ROW is one line
# carrying several independent statements -- `ANSWERS.md` line 512 puts *"**DISCHARGED FOR THE
# RECOMMENDED DEVICE**"* (a verdict on SS6 task 4) and *"`CH-0083` charged that neither..."*
# (provenance) in one cell of one row, and whole-sentence attribution read the task's verdict onto
# the challenge.  That is a FALSE POSITIVE, and `C-0080`'s finding is that a drift checker's false
# positives cost more than its true ones because the tool exists in order to be believed.
#
# 80 characters, chosen by measurement rather than taste: every phrasing the deliverable actually
# uses puts the verdict inside ~30 characters of its subject (*"`T-45` is still unmeasured"*,
# *"`CH-0083` is RESOLVED"*), and the misattribution that had to be excluded sits 180 away.  The
# sweep is in `T-183`'s result file: with the history guard beside it the misattribution survives
# at 200, 400 and unbounded, and dies at 120.  80 is that crossing with margin, and the sweep is
# emitted at every rung so the choice can be re-audited without re-running anything.
_VERDICT_WINDOW = 80

_SelfContradiction = namedtuple("SelfContradiction", "task verdicts mentions")

# `not answered`, `cannot be answered`, `no answer` — the negation carries the whole meaning, and
# it is exactly the phrasing `C-0071` used for a discharged question.  Matched BEFORE the positive
# words, and the positive matcher then refuses any hit that a negation already consumed.
_NEGATED_SETTLED = re.compile(
    r"\b(not|cannot|can\s?not|never|no|without|un)\s*(be\s+)?(answered|answer|resolved|settled|measured)\b",
    re.IGNORECASE,
)
_SETTLED_WORD = re.compile(
    r"\b(answered|answers|resolved|settled|closed|measured|established|demonstrated)\b",
    re.IGNORECASE,
)

# `T-183`.  The CHALLENGE vocabulary, which is a different word list from a task's and had to be
# measured rather than assumed: 81 of the 111 challenge files carry a `**Status**` row, 30 do not,
# and the `README.md` index covers 65 -- so it is not a controlled vocabulary and every word below
# was read out of the corpus.
#
# Two of the words are also the commonest verbs in this repository's prose.  "the recommendation
# stands" and "raised by `C-0107`" appear in almost every challenge reference in the deliverable
# and neither is a verdict, so both are matched CASE-SENSITIVELY: the corpus writes an adjudication
# in upper case and its prose in lower, which is the same discipline `queue_status` already runs
# under for `DONE` inside "Left undone".
#
# STANDS is a SETTLED verdict and that is not obvious.  In this corpus a challenge that "STANDS"
# has been adjudicated and holds against the claim it attacks -- `CH-0100` is the type case -- so
# it is a closed state.  A challenge nobody has adjudicated reads RAISED, which is the open one.
_CHALLENGE_SETTLED_WORD = re.compile(r"\b(UPHELD|WITHDRAWN|STANDS|OVERTURNED)\b")
_CHALLENGE_OPEN_WORD = re.compile(r"\bRAISED\b")
# The negation guard has to reach the new words too, or "not upheld" reads as settled.
_NEGATED_CHALLENGE = re.compile(
    r"\b(not|never|no|without)\s+(been\s+)?(upheld|withdrawn|stands|overturned|raised)\b",
    re.IGNORECASE,
)
# `TODO` is the QUEUE's own status word, and `T-195` showed the document side did not know it:
# `C-0124` wrote *"`T-195` ... is the one still `TODO`"*, `T-195` closed hours later, and the
# checker reported 0 contradicted assertions throughout. `CLAUDE.md` records the inverse failure --
# *"a status vocabulary GROWS, and every word your checker does not know is silently read as
# OPEN"* -- and this is the costlier direction: an unknown word read as NOTHING lets a stale
# assertion pass. Added to BOTH vocabularies, because a document can assert it and contradict
# itself with it.
_OPEN_WORD_VERDICT = re.compile(
    r"\b(open|unmeasured|unanswered|unresolved|undetermined|still\s+missing|not\s+determined"
    r"|TODO)\b",
    re.IGNORECASE,
)
_DISCHARGED_WORD = re.compile(r"\bdischarged\b", re.IGNORECASE)


def status_words(text):
    """The status verdicts a passage asserts: any of SETTLED, OPEN, DISCHARGED."""
    verdicts = set()
    if _DISCHARGED_WORD.search(text):
        # Discharged is its own verdict and must not collide with either of the others: a
        # question that stopped applying is neither answered nor owed an answer.
        return {"DISCHARGED"}
    negated = list(_NEGATED_SETTLED.finditer(text))
    negated_challenge = list(_NEGATED_CHALLENGE.finditer(text))
    if negated or negated_challenge:
        verdicts.add("OPEN")
    for match in _CHALLENGE_SETTLED_WORD.finditer(text):
        if any(n.start() <= match.start() < n.end() for n in negated_challenge):
            continue
        verdicts.add("SETTLED")
        break
    if _CHALLENGE_OPEN_WORD.search(text):
        verdicts.add("OPEN")
    for match in _SETTLED_WORD.finditer(text):
        # Skip a settled word that a negation already accounted for.
        if any(n.start() <= match.start() < n.end() for n in negated):
            continue
        verdicts.add("SETTLED")
        break
    for match in _OPEN_WORD_VERDICT.finditer(text):
        # `_HISTORICAL` ("open since/for/from") is a statement about how long a task WAS open,
        # and the same sentence usually closes it — the guard the open-assertion check already
        # carries, applied here too, because without it such a sentence contradicts itself and
        # every genuine contradiction it takes part in becomes unreadable.
        window = text[max(0, match.start() - 8):match.end() + 24]
        if _HISTORICAL.search(window):
            continue
        verdicts.add("OPEN")
        break
    return verdicts


def self_contradictions(answers_text):
    """[SelfContradiction] for every task the document calls both settled and unsettled.

    The unit is the sentence, not the block: a block long enough to hold both verdicts about
    DIFFERENT tasks is common in this deliverable and is not a contradiction.
    """
    by_task = {}
    for number, line in enumerate(strip_struck(answers_text).splitlines(), start=1):
        for sentence in re.split(r"(?<=[.!?])\s+|\n", line):
            for reference in _SUBJECT_REFERENCE.finditer(sentence):
                task = reference.group(1)
                start = max(0, reference.start() - _VERDICT_WINDOW)
                end = min(len(sentence), reference.end() + _VERDICT_WINDOW)
                verdicts = status_words(sentence[start:end])
                if not verdicts:
                    continue
                entry = by_task.setdefault(task, {"verdicts": set(), "mentions": []})
                entry["verdicts"].update(verdicts)
                entry["mentions"].append((number, sentence[start:end].strip()[:120]))
    found = []
    for task, entry in sorted(by_task.items()):
        if len(entry["verdicts"]) > 1:
            found.append(_SelfContradiction(task, entry["verdicts"], entry["mentions"]))
    return found


# --- the corpus half for challenges: a challenge's own file is the authority ------------------
#
# `T-183`.  A task's status is read from a `TASKS.md` row; a challenge has no such register.  The
# `gpd/challenges/README.md` index looks like one and is NOT: it carries 65 rows against 111 files,
# so 46 challenges are absent from it altogether, and its status cell is free prose.  The file
# itself is the authority -- 81 of the 111 carry a `**Status**` row -- and the remaining 30 return
# UNKNOWN and are SILENT.  Guessing at an undeclared status is the one thing this checker must not
# do: a false positive here sends an agent to "correct" a passage that is already right.
_CHALLENGE_STATUS_ROW = re.compile(r"^\|\s*\*\*Status\*\*\s*\|(.*)$", re.MULTILINE)
# Read case-INSENSITIVELY here, unlike `status_words`.  This is a declaration in a known cell of a
# known table, not a word found loose in prose, so the ambiguity the case guard exists to resolve
# does not arise -- and the corpus writes the same verdict as "Upheld", "UPHELD" and "upheld in
# part" in that cell.
_CHALLENGE_CLOSED = re.compile(
    r"\b(upheld|withdrawn|stands|resolved|overturned|discharged|answered|struck)\b", re.IGNORECASE
)
_CHALLENGE_OPEN = re.compile(r"\b(open|raised)\b", re.IGNORECASE)


def challenge_status_of(text):
    """OPEN | CLOSED | UNKNOWN for one challenge file's text.

    `T-298`.  The CELL is read with its struck spans blanked, because `C-0071`'s *strike, never
    delete* is how this corpus records an adjudication that supersedes a filing status -- and
    without the blanking the two are in direct conflict: a row reading
    `~~**OPEN.** ...~~ **RESOLVED, iteration 43**` is a RESOLVED challenge that this function
    reported OPEN, which is `CH-0185`'s own defect one level down.  `CH-0224` was the live
    instance at iteration 46 and nothing else in the corpus moves.  Only the cell is blanked,
    never the whole file: blanking first would erase a wholly struck row's `**Status**` label
    and turn a declared status into an UNKNOWN one, which is the direction this checker must
    not guess in.

    OPEN wins a tie, because a challenge cell routinely records both -- *"raised.  No number in
    `C-0023` moves"* is a RAISED status whose sentence also reports what was adjudicated -- and
    reporting a live challenge as closed is the failure that loses a withdrawn reading.
    """
    match = _CHALLENGE_STATUS_ROW.search(text)
    if not match:
        return "UNKNOWN"
    cell = strip_struck(match.group(1))
    if _CHALLENGE_OPEN.search(cell):
        return "OPEN"
    if _CHALLENGE_CLOSED.search(cell):
        return "CLOSED"
    return "UNKNOWN"


def challenge_statuses(directory):
    """{challenge ID: OPEN | CLOSED | UNKNOWN} over a directory of challenge files."""
    statuses = {}
    if not os.path.isdir(directory):
        return statuses
    for name in sorted(os.listdir(directory)):
        identifier = re.match(r"(CH-\d{1,4})", name)
        if not identifier or not name.endswith(".md"):
            continue
        with open(os.path.join(directory, name), encoding="utf-8") as handle:
            statuses[identifier.group(1)] = challenge_status_of(handle.read())
    return statuses


_CHALLENGE_REFERENCE = re.compile(r"`(CH-\d{1,4})`")


def stale_challenge_statuses(answers_text, statuses):
    """[(line, challenge, status)] for every open assertion the corpus records as closed.

    The same window, the same `_HISTORICAL` duration guard and the same `_ANSWERING` cancellation
    as `open_assertions` -- `C-0088`'s guard 2 carried over rather than re-invented, because the
    two false positives it was written against ("a list of open questions", "reopen") are exactly
    as live in a challenge reference as in a task one.
    """
    stale = []
    for number, line in enumerate(strip_struck(answers_text).splitlines(), start=1):
        for reference in _CHALLENGE_REFERENCE.finditer(line):
            identifier = reference.group(1)
            start = max(0, reference.start() - _OPEN_WINDOW)
            end = min(len(line), reference.end() + _OPEN_WINDOW)
            window = line[start:end]
            if not _CHALLENGE_OPEN_ASSERTION.search(window):
                continue
            # `T-298`.  The two CANCELLATIONS are read on a wider window than the assertion.
            # `_OPEN_WINDOW = 24` exists to BIND an open word to its reference; a cancellation is
            # not bound to anything and reading it through the same 24 characters truncates the
            # sentence that performs it.  `ANSWERS.md` writes *"(`CH-0083`, raised open in
            # iteration 16 and **RESOLVED in iteration 17**, below)"* -- a duration and its own
            # closure, both of which `_HISTORICAL` and `_ANSWERING` already know and neither of
            # which fits in 24 characters.  Annotating `CH-0083` as the corpus says it is would
            # otherwise have fired a build-failing gate on a sentence that is entirely correct.
            # A cancellation can only ever REMOVE a hit, so a wider window is strictly a
            # narrowing; 80 is `_VERDICT_WINDOW`, measured on this same document by `T-183`.
            guard = line[
                max(0, reference.start() - _VERDICT_WINDOW):
                reference.end() + _VERDICT_WINDOW
            ]
            if _HISTORICAL.search(guard) or _ANSWERING.search(guard):
                continue
            if statuses.get(identifier, "UNKNOWN") == "CLOSED":
                stale.append((number, identifier, "CLOSED"))
    return stale


# --- `T-261`: the two AUDIT arms ---------------------------------------------------------------
#
# `T-261` asks for an arm flagging a deliverable passage that cites a challenge as the SOURCE of a
# number where that challenge has been adjudicated.  Both arms below are RESIDUE lines: printed
# unconditionally, counted in no exit code.  `C-0129`'s policy -- gate what can be made clean and
# print the rest beside it -- because the predicate cannot come clean, for a reason that is
# `CH-0230`'s and is structural rather than fixable: **a correcting sentence has to NAME the
# challenge in order to withdraw it**, so the corrections land in the census they would be gated
# by.  Measured at iteration 46: the naive form reads 34 and is almost entirely corrections; the
# tightest form below reads 7, of which 3 are corrections and 2 are cross-references.
#
# THE WORD `RAISED` IS NOT AN ADJUDICATION and `WITHDRAWN` is, which is what separates the two
# states this corpus's Status cells actually carry.  A cell reading *"RAISED and REPAIRED in the
# same iteration"* -- the commonest adjudicated form here -- is adjudicated.
_ADJUDICATION_WORD = (
    r"UPHELD|RESOLVED|ANSWERED|REFUTED|WITHDRAWN|OVERTURNED|DISCHARGED|SUPERSEDED|CLOSED"
    r"|REPAIRED|ANNOTATED|STRUCK"
)
_ADJUDICATED = re.compile(r"\b(?:" + _ADJUDICATION_WORD + r")\b")


def challenge_adjudicated(text):
    """Does this challenge file's own `**Status**` row record an adjudication?

    Case-SENSITIVE, unlike `challenge_status_of`: this corpus writes a verdict in upper case and
    the same words in lower case as ordinary prose (*"the bracket has to be withdrawn"*), and the
    cell is one line of free text in which both occur.
    """
    match = _CHALLENGE_STATUS_ROW.search(text)
    return bool(match and _ADJUDICATED.search(strip_struck(match.group(1))))


def challenge_adjudications(directory):
    """{challenge ID: bool} -- whether each challenge's own file records an adjudication."""
    found = {}
    if not os.path.isdir(directory):
        return found
    for name in sorted(os.listdir(directory)):
        identifier = re.match(r"(CH-\d{1,4})", name)
        if not identifier or not name.endswith(".md"):
            continue
        with open(os.path.join(directory, name), encoding="utf-8") as handle:
            found[identifier.group(1)] = challenge_adjudicated(handle.read())
    return found


#: A link's target carries the challenge's own slug, which is prose to every pattern below, so
#: links are flattened to their label before either form is matched.
_CHALLENGE_LINK = re.compile(r"\[(`CH-\d{1,4}`)\]\([^)]*\)")
#: The adjudication must be BOUND to the reference, not merely near it.  Measured, proximity alone
#: gives 21 sites of which 6 are a verdict about some *other* challenge in the same list.  The
#: clause guard `[^.;|]` is what stops it crossing a sentence or a table cell.
#: `T-298`, narrowing 1 -- a CONDITIONAL is the opposite of an adjudication.  `C-0056` writes
#: *"**If** `CH-0068` is upheld, the design point is `N_ret = 56` … If `CH-0068` is refused, the
#: thresholds bind"*, which says in as many words that the verdict is not in.  Measured over the
#: whole claims corpus before it was written: it removes **1** of 46 pattern-1 sites and that one
#: is the false positive.
_CONDITIONAL = r"(?<!\bif )(?<!\bIf )(?<!\bunless )(?<!\bUnless )(?<!\bwhether )(?<!\bWhether )"
#: `T-298`, narrowing 2 -- the clause guard `[^.;|]` stops at a sentence and a table cell and does
#: NOT stop at a comma followed by a coordinating conjunction, which starts a new clause with its
#: own subject.  `C-0132` writes *"That is `CH-0157`**, and** it is why the bracket has to be
#: withdrawn"*: the thing withdrawn is the **bracket**, and `C-0197` §5 reported this as the one
#: false positive of 17 and declined to tune it away on `C-0176`'s ground.  It is repaired here
#: with the false negatives MEASURED first, which is what `C-0176` actually asks for: over the
#: whole claims corpus the guard removes **2** of 46 sites and both are this same sentence, once
#: in `C-0132` and once where `C-0197` quotes it.  A relative `, which` is deliberately NOT in the
#: list -- it continues the same subject, and `C-0182`'s *"`CH-0229`, which raised this task, is
#: **ANSWERED**"* is a genuine adjudication that an over-wide guard would have lost.
_CLAUSE = r"(?:(?!,\s+(?:and|but|so|yet|or)\b)[^.;|])"
_ADJUDICATES = (
    re.compile(
        _CONDITIONAL + r"`(CH-\d{1,4})`" + _CLAUSE + r"{0,40}?\b(?:is|are|was|were)\b"
        + _CLAUSE + r"{0,40}?\*{0,2}(?:"
        + _ADJUDICATION_WORD + r"|resolved|upheld|answered|withdrawn|discharged)"
    ),
    re.compile(
        r"(?:" + _ADJUDICATION_WORD + r"|resolved|upheld|answered|withdrawn|discharged)"
        r"\*{0,2}\s+by\s+`(CH-\d{1,4})`"
    ),
)


def adjudications_in_claim(name, text):
    """[(challenge, claim file)] for every challenge this claim's text adjudicates."""
    flattened = _CHALLENGE_LINK.sub(r"\1", strip_struck(text))
    found = []
    for pattern in _ADJUDICATES:
        for match in pattern.finditer(flattened):
            pair = (match.group(1), name)
            if pair not in found:
                found.append(pair)
    return sorted(found)


def unrecorded_adjudications(claims_directory, adjudicated):
    """[(challenge, claim file)] a claim adjudicates whose own Status row does not say so.

    The INPUT defect behind `T-261`: `stale_challenge_statuses` reads a challenge's own file as
    the authority (`T-183`), so a challenge answered by a later claim and never annotated is
    reported OPEN by this tool and a deliverable may rest a price on it indefinitely.  The row's
    own live instance, `CH-0185`, is exactly that: `C-0148` says *"`CH-0185` is ANSWERED"* and the
    challenge file still says *raised*.
    """
    found = []
    if not os.path.isdir(claims_directory):
        return found
    for name in sorted(os.listdir(claims_directory)):
        if not name.endswith(".md"):
            continue
        with open(os.path.join(claims_directory, name), encoding="utf-8") as handle:
            text = handle.read()
        for identifier, source in adjudications_in_claim(name, text):
            if not adjudicated.get(identifier, False):
                found.append((identifier, source))
    return found


#: How far either side of a challenge reference a number and a claim citation are looked for.
_PRICE_WINDOW = 200


def prices_on_adjudicated(answers_text, adjudicated):
    """[(line, challenge)] where a number is attributed to an adjudicated challenge, unpointed.

    *Unpointed* is the whole narrowing: naming ANY claim inside the window clears the passage,
    because that is how this corpus writes a correction, and a correction is the very sentence a
    naive predicate flags.  It leaves the shape `CH-0203` describes -- a **price** attributed to a
    challenge with no pointer to whatever adjudicated it -- which is what decision 8 was.
    """
    found = []
    for number, line in enumerate(strip_struck(answers_text).splitlines(), start=1):
        for reference in _CHALLENGE_REFERENCE.finditer(line):
            identifier = reference.group(1)
            if not adjudicated.get(identifier, False):
                continue
            window = line[
                max(0, reference.start() - _PRICE_WINDOW):
                reference.end() + _PRICE_WINDOW
            ]
            if not tokens(window):
                continue
            if _CITATION_CLAIM.search(window):
                continue
            found.append((number, identifier))
    return found


_CITATION_CLAIM = re.compile(r"\bC-\d{4}\b")


def trace(answers_text, sources, min_digits=2):
    """One record per (block, token): (line, token, status, cited, owners).

    Struck text is blanked first: a `~~withdrawn~~` number is not a claim the document makes.
    """
    answers_text = strip_struck(answers_text)
    records = []
    for line, text in blocks(answers_text):
        cited = citations(text)
        for token in tokens(text, min_digits=min_digits):
            owners = [key for key, body in sorted(sources.items()) if contains(body, token)]
            if any(key in cited for key in owners):
                status = "CITED"
            elif owners:
                status = "ELSEWHERE"
            else:
                status = "ABSENT"
            records.append((line, token, status, cited, owners))
    return records


def parse_arguments(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--answers", nargs="+", default=list(DEFAULT_DOCUMENTS))
    parser.add_argument("--claims", default="gpd/claims")
    parser.add_argument("--challenges", default="gpd/challenges")
    parser.add_argument("--queue", default="TASKS.md")
    parser.add_argument("--min-digits", type=int, default=2)
    parser.add_argument("--status", default="", help="only report this status")
    return parser.parse_args(argv)


def main(argv=None):
    arguments = parse_arguments(argv)
    sources = load_sources(arguments.claims, arguments.challenges)
    failures = 0
    for document in arguments.answers:
        failures += check_document(document, arguments, sources)
    # `P-29`.  This read `return 0` from iteration 12's multi-document refactor, which accumulated
    # `failures` into a DEAD local -- so for ~30 commits the tool could not fail on ANY of its four
    # checks, and `C-0173` then wired it into `tools/verify.sh` under `set -euo pipefail` on the
    # stated ground that *"it already returned its defect count"*.  It did not, and nothing had run
    # it to find out: a gate is a claim about a corpus and it is discharged by RUNNING it.
    #
    # A BOOLEAN and not the count, deliberately.  `sys.exit(n)` truncates modulo 256, and
    # `counts["ABSENT"]` is unbounded -- exactly 256 absent tokens would exit 0 and read as clean.
    # The counts are printed; the exit code only has to say whether the corpus is clean.
    # `T-261`'s second residue, PROMOTED TO A GATE by `T-298`.  A challenge a claim has
    # adjudicated whose own `**Status**` row does not say so is read OPEN by `challenge_statuses`
    # above, which is the authority `T-183` chose -- so the arm that DOES gate is blind for as
    # long as the annotation is missing, and `DECISIONS-FOR-NDI` decision 8 duly went to a
    # customer priced on `CH-0185` in the same iteration `C-0148` withdrew that price.
    #
    # `C-0129`'s policy is *gate what can be made clean*: `T-261` measured 17 challenges over 22
    # sites and could not, so it printed.  `T-298` read all 17 against the claim that adjudicates
    # each, annotated 15 and excluded 2 -- `CH-0068` (a CONDITIONAL) and `CH-0157` (a clause
    # crossing), both by a narrowing whose false negatives were measured over the whole claims
    # corpus first -- after which it reads 0 and gates.  It is a statement about the CORPUS rather
    # than about a document, so it is computed once and added to the exit code once.
    adjudicated = challenge_adjudications(arguments.challenges)
    unrecorded = unrecorded_adjudications(arguments.claims, adjudicated)
    for identifier, claim in unrecorded:
        print("{}\tUNRECORDED-ADJUDICATION\t{}\t{}".format(arguments.challenges, identifier, claim))
    sys.stdout.flush()
    print(
        "# {}: {} challenge(s) a claim adjudicates whose own Status row does not say so "
        "(GATED since T-298)".format(
            arguments.challenges, len({identifier for identifier, _ in unrecorded})
        ),
        file=sys.stderr,
    )
    return 1 if failures or unrecorded else 0


def check_document(document, arguments, sources):
    """Run all four checks over one document.  Every line is prefixed with its own name, because
    the tool now reads more than one and an unlabelled row cannot be acted on."""
    with open(document, encoding="utf-8") as handle:
        answers_text = handle.read()
    tag = os.path.basename(document)
    records = trace(answers_text, sources, min_digits=arguments.min_digits)

    counts = {"CITED": 0, "ELSEWHERE": 0, "ABSENT": 0}
    for line, token, status, cited, owners in records:
        counts[status] += 1
        if arguments.status and status != arguments.status:
            continue
        print(
            "{}\t{}\t{}\t{}\t{}\t{}".format(
                tag, line, token, status, ",".join(cited) or "-", ",".join(owners[:6]) or "-"
            )
        )
    # The records go to stdout and the summary to stderr.  When both are piped to one place
    # stdout is block-buffered and stderr is not, so without this flush the summary overtakes the
    # records it summarises and lands in the middle of them — reported as a "missing newline" by
    # `C-0080`, which is what it looks like.  Flush wherever the two streams are interleaved.
    sys.stdout.flush()
    print(
        "# {}: {} tokens: {} CITED, {} ELSEWHERE, {} ABSENT".format(
            tag, len(records), counts["CITED"], counts["ELSEWHERE"], counts["ABSENT"]
        ),
        file=sys.stderr,
    )

    # The status check runs unconditionally and reports to stderr beside the token summary,
    # because `C-0067` found this class of drift outlives the numeric class by iterations and
    # a check nobody remembers to ask for is not a check.
    stale = []
    if os.path.isfile(arguments.queue):
        with open(arguments.queue, encoding="utf-8") as handle:
            queue_text = handle.read()
        assertions = open_assertions(answers_text)
        stale = stale_statuses(answers_text, queue_text)
        for line, task, status in stale:
            print("{}\t{}\tSTALE-OPEN\t{}\t{}".format(tag, line, task, status))
        sys.stdout.flush()
        print(
            "# {}: {} open assertion(s), {} contradicted by {}".format(
                tag, len(assertions), len(stale), arguments.queue
            ),
            file=sys.stderr,
        )

    # `T-183`'s corpus half for challenges.  Reported beside the task one and for the same reason:
    # a challenge's status is as load-bearing as a task's -- an UPHELD challenge has withdrawn
    # something a claim asserts -- and the deliverable makes 123 references to 78 of them.  The
    # coverage is printed because it is not 100 %: a challenge with no declared status is UNKNOWN
    # and silent, and the reader needs to know how many of those there are.
    statuses = challenge_statuses(arguments.challenges)
    stale_challenges = stale_challenge_statuses(answers_text, statuses)
    for line, identifier, status in stale_challenges:
        print("{}\t{}\tSTALE-OPEN\t{}\t{}".format(tag, line, identifier, status))
    sys.stdout.flush()
    declared = sum(1 for value in statuses.values() if value != "UNKNOWN")
    print(
        "# {}: {} challenge(s), {} with a declared status, {} open assertion(s) contradicted".format(
            tag, len(statuses), declared, len(stale_challenges)
        ),
        file=sys.stderr,
    )

    # `T-261`'s residue line.  Printed, NOT counted -- `C-0129`'s policy, because the predicate
    # cannot come clean: a correcting sentence has to NAME the challenge it withdraws, so every
    # repair lands in the census the gate would fire on (`CH-0230`).  What is left after the
    # narrowing is a number attributed to an ADJUDICATED challenge with no claim named beside it.
    adjudicated = challenge_adjudications(arguments.challenges)
    priced = prices_on_adjudicated(answers_text, adjudicated)
    for line, identifier in priced:
        print("{}\t{}\tPRICED-ON-ADJUDICATED\t{}\t-".format(tag, line, identifier))
    sys.stdout.flush()
    print(
        "# {}: {} number(s) priced on an adjudicated challenge with no claim named "
        "(RESIDUE, not gated)".format(tag, len(priced)),
        file=sys.stderr,
    )

    # The third check, and the only one that needs no corpus at all: does the deliverable agree
    # with itself?  Unconditional for the same reason as the second — `C-0080` found the live
    # instance by hand and neither of the other two halves can ever reach it.
    contradictions = self_contradictions(answers_text)
    for contradiction in contradictions:
        print(
            "{}\t{}\tSELF-CONTRADICTION\t{}\t{}".format(
                tag,
                contradiction.mentions[0][0],
                contradiction.task,
                "/".join(sorted(contradiction.verdicts)),
            )
        )
        for line, sentence in contradiction.mentions:
            print("\t\tline {}: {}".format(line, sentence))
    sys.stdout.flush()
    print(
        "# {}: {} task(s) the document contradicts itself about".format(tag, len(contradictions)),
        file=sys.stderr,
    )
    # `P-29`.  `len(stale)` was printed and DROPPED: three of the four checks fed the exit
    # code and the TASK-status one -- `C-0067`/`C-0078`/`C-0088`'s whole class, and the reason
    # this file exists -- did not.  `C-0173` then wired the tool into `tools/verify.sh` under
    # `set -euo pipefail`, where the exit code IS the wiring, so for that class the gate could
    # not fail: a `T-9` contradiction printed on stdout through three commits and two claims
    # recording a green suite.  Worse, the challenge-status check prints the SAME `STALE-OPEN`
    # tag and WAS counted, so the output could not tell a failing line from an inert one.
    return counts["ABSENT"] + len(stale) + len(stale_challenges) + len(contradictions)


if __name__ == "__main__":
    sys.exit(main())
