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
# P-30 -- ONE definition of "what is this queue row's verdict", shared by the reader and its gate.
#
# WHY A THIRD MODULE.  `tools/trace-answers.py` READS the queue; `tools/check-queue-vocabulary.py`
# GATES what the reader is allowed to be surprised by, and already imports the reader.  Putting the
# shared predicate in the gate would make the reader depend on its own gate, which is a cycle and,
# worse, a gate that defines the thing it checks.  So it lives here and both import it, one way:
#
#     queue_verdicts  <--  trace-answers  <--  check-queue-vocabulary
#
# WHAT A VERDICT IS, AND WHY THIS PREDICATE.  `P-29` measured it: a bold run ANYWHERE in a row that
# contains a closing word is 29 distinct phrases over `TASKS.md`, and most are prose about some
# OTHER object -- a challenge, a deliverable, a candidate of a remedy, or the row's own title.
# Restricted to a run that OPENS a cell and is at most `MAX_WORDS` words, the same corpus gives 8.
# `P-30` adds the one shape that census could not see, because it carries no closing word at all:
# a bare, unbolded leading `TODO`, which is how `T-261`'s status cell is written --
# `TODO -- **MEDIUM**`, where the BOLD carries the priority and the verdict does not.
#
# WHICH VERDICT WINS.  A row can carry several.  Measured over the committed queue: 12 rows carry a
# leading `TODO` and 9 of them are CLOSED, because the file writes the live verdict into an EARLIER
# cell and preserves the original `TODO -- **PRIORITY**` note in a LATER one (`C-0071`'s *strike,
# never delete*, applied to a whole column).  So the LEFTMOST verdict is the live one; taking the
# last, or taking "any TODO opens", is 9 false positives.  Taking the first reproduces the old
# whole-row reader on 262 of 262 rows that carry a verdict, moving exactly the four it was wrong
# about.
#
# THE DIRECTION THIS ERRS IN IS OPEN, which `CLAUDE.md` records four times as the safe one: an
# unknown word reads OPEN, a task stays in the register, and the loop can still pick it up.

import re

# Six words is the widest legitimate verdict in the corpus (`ANSWERED in its specification half`
# is four).  The bound exists so that a bold PROSE sentence containing a closing word is not read
# as a verdict; it does NOT apply to a `TODO`, which is decided by its first word alone.
MAX_WORDS = 6

# The queue's closing words, matched CASE-SENSITIVELY and on WHOLE WORDS, because the queue writes
# its verdicts in bold upper case and its prose in lower ("left undone" contains DONE).
CLOSING_WORD = re.compile(r"\b(DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED)\b")

# A closing word that is not qualified away.  `PARTIALLY DONE` is iteration 35's coinage and is a
# NEGATIVE; any further qualifier the queue coins belongs here, with a test, the day it is written.
NOT_CLOSED_QUALIFIER = r"(?<!PARTIALLY )(?<!PARTLY )"
UNQUALIFIED_CLOSING_WORD = re.compile(NOT_CLOSED_QUALIFIER + CLOSING_WORD.pattern)

# A queue row: `| T-129 | ... ` -- the identifier may be wrapped in backticks or bold, and the
# TRAILING pipe is OPTIONAL.  GFM does not require it and one committed row omits it, which made
# that row invisible to the reader entirely.
# NO `^` here, deliberately: the anchoring is carried by `.match()` at every call site.  With
# both, a mutation of EITHER is a no-op, which is `P-29`'s own recorded trap on `_LEADING_BOLD`
# -- its mutation test found it by surviving twice, and this pattern reproduced it exactly.
TASK_ROW = re.compile(r"\|\s*\**`?([TP]-\d{1,4}[a-z]?)`?\**\s*\|(.*)$")

# A bold run that OPENS a cell.  NO `^` here, deliberately: the anchoring is carried by `.match()`
# at the call sites.  With both, a mutation of EITHER is a no-op -- which `P-29`'s mutation test
# found by surviving twice.
_LEADING_BOLD = re.compile(r"\s*\*\*([^*]{1,120}?)\*\*")

# `TODO` at the head of a cell, with any bold markers stripped first, so that both of the queue's
# two shapes are one rule: `TODO -- **MEDIUM**` and `**TODO -- HIGH VALUE, HIGHEST COST**`.
_LEADING_TODO = re.compile(r"TODO\b")

OPEN = "OPEN"
CLOSED = "CLOSED"


def blank_struck(text):
    """Replace every ~~struck~~ span by spaces of the same length.

    `C-0071`'s rule is *strike, never delete*, so a withdrawn verdict stays in the file forever.
    A struck verdict is not a verdict, and blanking length-preservingly keeps offsets usable.
    """
    return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)


# `T-283`.  An inline code span, single- or double-backticked, on ONE line -- a queue row is one
# physical line (`C-0083`), and an unclosed backtick is therefore not a span but a stray character.
# The double form is listed FIRST because alternation is ordered: ``ANSWERED`` must be taken whole.
_CODE_SPAN = re.compile(r"``[^\n]*?``|`[^`\n]*`")


def blank_code_spans(text):
    """Replace every `inline code span` by spaces of the same length.

    `T-283`.  A status word inside backticks is a token QUOTED AS DATA, not an assertion about the
    row it stands in -- `T-261`'s acceptance criterion quotes three of `gpd/challenges/README.md`'s
    own status words, and lower-casing them would falsify the quotation.  Backticking is already
    this corpus's idiom for quoting a token, so the escape costs two characters and falsifies
    nothing, which is what lets the residue become a gate instead of a permanent one-defect line.

    Used ONLY by the whole-row scan.  A row's VERDICT is read from the unblanked body, so blanking
    can never turn a closed row open -- asserted by a named test.
    """
    return _CODE_SPAN.sub(lambda m: " " * len(m.group(0)), text)


def cell_verdict(cell):
    """(phrase, sense) for the verdict that OPENS this cell, or None.

    `phrase` is the verdict as written, except that every `TODO ...` opening normalises to the
    single token `TODO` -- the queue writes a different priority tail on every one of them, and a
    vocabulary that had to enumerate the tails would be a vocabulary of prose.
    """
    stripped = cell.strip()
    if not stripped:
        return None
    if _LEADING_TODO.match(stripped.lstrip("*").lstrip()):
        return ("TODO", OPEN)
    bold = _LEADING_BOLD.match(stripped)
    if not bold:
        return None
    phrase = bold.group(1).strip()
    if len(phrase.split()) > MAX_WORDS or not CLOSING_WORD.search(phrase):
        return None
    return (phrase, CLOSED if UNQUALIFIED_CLOSING_WORD.search(phrase) else OPEN)


def row_verdicts(row_body):
    """[(phrase, sense)] for every cell of one row body that opens with a verdict, left to right.

    `row_body` is everything after the identifier cell.
    """
    found = []
    for cell in blank_struck(row_body).split("|"):
        verdict = cell_verdict(cell)
        if verdict:
            found.append(verdict)
    return found


def leading_verdicts(queue_text):
    """[(identifier, phrase, sense)] over a whole queue, every verdict of every row."""
    found = []
    for line in queue_text.splitlines():
        match = TASK_ROW.match(line.strip())
        if not match:
            continue
        for phrase, sense in row_verdicts(match.group(2)):
            found.append((match.group(1), phrase, sense))
    return found


def task_rows(queue_text):
    """[(identifier, row body)] for every table row whose first cell is an identifier.

    Deliberately written without the reader's own row pattern, and deliberately more permissive:
    this is the scanner the coverage gate compares the reader against, and a coverage check that
    shares the reader's regular expression cannot see the reader's own format assumption.  The
    independence is asserted structurally by a named test in `tools/check-queue-vocabulary.py`.
    """
    rows = []
    for line in queue_text.splitlines():
        stripped = line.strip()
        if not stripped.startswith("|") or stripped.count("|") < 2:
            continue
        cells = stripped.split("|")
        head = cells[1].strip().strip("*").strip("`").strip()
        if re.match(r"^[TP]-\d{1,4}[a-z]?$", head):
            rows.append((head, "|".join(cells[2:])))
    return rows
