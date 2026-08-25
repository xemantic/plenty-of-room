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
# T-336 -- A SELF-DESCRIBING COUNT THE DELIVERABLE *PRINTS* AGAINST THE ONE A RESULT FILE *PINS*.
#
#     tools/T-336-pinned-count-census.py                 # the registry and both sides of it
#     tools/T-336-pinned-count-census.py --check         # the git-free gated arms
#     tools/T-336-pinned-count-census.py --prose         # the prose residue, printed
#     tools/T-336-pinned-count-census.py --prose --strict  # ... as a GATE (see below)
#     tools/T-336-pinned-count-census.py --rederive      # needs .git; skips visibly without it
#     tools/T-336-pinned-count-census.py --self-test
#
# WHY THIS EXISTS, and why it is NOT the gate `C-0222` refused.
#
# `C-0222` retired the WIRING half of a recurring defect and said so in its own validity range:
# *"nothing yet checks that what `DECISIONS-FOR-NDI.md` prints agrees with what the tool derives.
# That is `T-336`."*  It also refused one shape explicitly -- *"a gate parsing `18 + 21 + 12` =
# FIFTY-ONE out of prose would be a gate on a NUMERAL, which is the class of predicate this task
# exists to retire."*
#
# That refusal is right, and it turns on the COMPARAND rather than on the numeral.  A gate that
# compares prose against a LIVE DERIVATION AT `HEAD` is unsatisfiable by construction for a census
# of the corpus that contains the census (`CH-0182`), and the deliverable says so itself -- *"its
# own finished tree reads 248 / 215"*.  A gate that can never come clean is not a gate (`C-0083`).
# HAVING NOTHING STABLE TO COMPARE AGAINST IS WHY SUCH A GATE DEGENERATES INTO PARSING NUMERALS:
# the pattern-matching is the symptom, and the missing fixed point is the disease.
#
# So this tool compares against a PINNED thing instead of a MOVING one.  Three emitters already
# write `(quantity, value, resolvedRef)` triples into committed result files and nobody had ever
# read one back.  A sha does not move when the corpus grows, so the equality is PERMANENT; and the
# comparison *prose against committed JSON* needs no `git` at all -- which is the only reason it
# can be wired, `tools/snapshot.sh` excluding `./.git` and `tools/verify.sh` running every check
# inside that snapshot.  A git-dependent gate would skip silently where it is wired, which is
# `C-0177`'s *a gate that cannot fail*.
#
#     axis                | the refused gate            | this one
#     --------------------+-----------------------------+-----------------------------------------
#     anchor              | a numeral pattern           | a DECLARED registry entry; prose is read
#                         |                             | only to LOCATE, and an undeclared
#                         |                             | quantity REFUSES (`C-0182`)
#     comparand           | a live derivation at HEAD   | a committed JSON leaf at that file's own
#                         |                             | resolved `baselineRef`
#     can it come clean?  | no, by construction         | yes, permanently
#     predicate           | does this equal today?      | is this a value some record PINS?
#     object class        | prose against a program     | two committed artifacts, sharing no code
#     git                 | required                    | not required
#
# WHAT IT DELIBERATELY DOES NOT REACH, printed on every run because `C-0209` requires it:
#
#   * a corpus quantity no committed tool derives on every run -- outside the registry BY
#     DECLARATION, and the registry refuses rather than defaults
#   * the RE-DERIVATION arm, which needs `git` and prints a visible stderr skip without it
#   * figures inside STRUCK spans (`C-0071`: strike, never delete)
#   * unpinned records that nothing quotes -- legal, listed, and the input to `T-340`
#   * a prose figure carrying no declared anchor -- counted and listed as UNREACHED, never as clean
#
# WHY `--prose` IS NOT WIRED BUILD-FAILING.  It is RED at `HEAD`: `ANSWERS.md` line 1385 prints
# `247 / 214 / 461` and `248 / 215`, five values that NO committed record pins and that occur at
# NONE of the repository's commits.  Its own pass's file records `246 / 213 / 459` at `d7b7074`,
# unquoted.  The task that wrote this tool may not edit `ANSWERS.md`, so the arm ships as a
# printed residue inside the wired `--check` and as a gate under `--prose --strict` -- `C-0129`'s
# idiom, *wire the gate on what can be made clean and print the residue beside it, ungated*.
# `T-339` flips `PROSE_ARM_IS_GATED` once the substitution lands.
"""A count a deliverable prints, against the one a committed result file pins."""

import argparse
import glob
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = "gpd/results"
DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")

#: `T-339` flips this to True once `ANSWERS.md` carries the pinned reading.  It is ONE constant on
#: purpose: a promotion that needs a rewrite is a promotion nobody performs.
PROSE_ARM_IS_GATED = False


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


t334 = _load("t336_t334", "T-334-gate-census.py")
Tree = t334.Tree


# --- the registry -------------------------------------------------------------------------------
#
# A quantity is DECLARED or it is refused.  Each row carries the tool that derives it on every
# default `tools/verify.sh` run (checked against `T-334`'s own union, so a quantity cannot be
# declared against a tool nothing runs), the anchors a prose figure must sit beside, and a
# derivation used only by the git-dependent `--rederive` arm.

class Quantity(object):
    """One declared corpus count.

    `anchors` are `(derivation, unit)` pairs.  The DERIVATION is what a figure must stand beside --
    a tool path, a shell command, or the corpus directory whose listing IS the population.  The
    UNIT is a phrase the figure must be followed by, and it is required wherever one tool prints
    TWO of the registry's quantities: `tools/check-corpus-identifiers.py` prints both a dangling
    count and *"N claims and challenges exist"*, so an anchor on the tool alone attributes every
    nearby number to both.  A derivation unique to the quantity carries `None`.

    `noun` is used only by the reverse sense, where a SPELLED headline stands in front of its own
    directory ("Two hundred and forty-seven CHALLENGES in `gpd/challenges/`").  That sense has no
    verb to lean on, so without the noun it degenerates into *the last number before this anchor*,
    which is the unbounded numeral surface `C-0222` refused.
    """

    def __init__(self, name, label, deriver, anchors, record_leaf, derive, noun=None):
        self.name = name
        self.label = label
        self.deriver = deriver
        self.anchors = anchors
        self.record_leaf = record_leaf
        self.derive = derive
        self.noun = noun


def _count_paths(tree, directory, prefix):
    if tree.ref is None:
        names = os.listdir(os.path.join(tree.root, directory))
    else:
        listing = subprocess.run(
            ["git", "-C", tree.root, "ls-tree", "--name-only", tree.ref, directory + "/"],
            capture_output=True, text=True, check=True,
        ).stdout.split()
        names = [os.path.basename(path) for path in listing]
    return sum(1 for name in names if name.startswith(prefix) and name.endswith(".md"))


def _challenges(tree):
    return _count_paths(tree, "gpd/challenges", "CH-")


def _claims(tree):
    return _count_paths(tree, "gpd/claims", "C-")


def _both(tree):
    return _challenges(tree) + _claims(tree)


def _union(tree):
    return len(t334.census(tree)["union"])


def _naming(tree):
    return len(t334.census(tree)["namingPredicate"])


#: The anchors are DERIVATIONS -- a tool path, a shell command, or the corpus directory whose
#: listing IS the population.  Not subject words: `C-0176`'s *a rule whose name does not carry its
#: lattice will be applied to every lattice*, met on a census.
QUANTITIES = (
    Quantity(
        "challenges", "challenge files in gpd/challenges/", "check-challenge-index.py",
        (("tools/check-challenge-index.py", r"challenge file"),
         ("ls gpd/challenges/CH-*.md", None),
         ("gpd/challenges/", None)),
        ("selfDescribingCounts", "challenges", "atRef"), _challenges, noun="challenges",
    ),
    Quantity(
        "claims", "claim files in gpd/claims/", "check-corpus-identifiers.py",
        (("ls gpd/claims/C-*.md", None),
         ("gpd/claims/", None)),
        ("selfDescribingCounts", "claims", "atRef"), _claims, noun="claims",
    ),
    Quantity(
        "claimsAndChallenges", "claims and challenges together",
        "check-corpus-identifiers.py",
        (("tools/check-corpus-identifiers.py", r"claims and challenges"),),
        ("selfDescribingCounts", "claimsAndChallenges", "atRef"), _both,
    ),
    Quantity(
        "gateCensusUnion", "distinct tools that can fail a default tools/verify.sh run",
        "T-334-gate-census.py",
        (("tools/T-334-gate-census.py", r"distinct tool|tool\(s\)|can fail"),),
        ("atBaselineRef", "distinctToolsThatCanFailADefaultVerifyShRun"), _union,
    ),
    Quantity(
        "namingPredicate", "tools matching ls tools/check-*.py tools/trace-answers.py",
        "T-334-gate-census.py",
        (("ls tools/check-*.py tools/trace-answers.py", None),),
        ("atBaselineRef", "theFourPredicatesThisReplaces", "namingPrefix", "count"), _naming,
    ),
)

QUANTITY_BY_NAME = dict((quantity.name, quantity) for quantity in QUANTITIES)


def quantity(name):
    """The declared quantity, or a REFUSAL -- never a default (`C-0182`)."""
    if name not in QUANTITY_BY_NAME:
        raise KeyError(
            "T-336 refuses the undeclared quantity %r: declare it in QUANTITIES with the tool "
            "that derives it on every run, or it is not a quantity this census knows" % (name,)
        )
    return QUANTITY_BY_NAME[name]


# --- pinned and unpinned records ------------------------------------------------------------------

#: A key under which a recorded value is PINNED to the file's own resolved `baselineRef`.
PINNED_KEYS = ("atRef", "atBaselineRef")

#: A key under which a recorded value names NO state that resolves.  Declared, so that such a
#: record is legal and listed rather than silently read as pinned -- and so the prose arm can
#: refuse a figure that only ever appears here.
UNPINNED_KEYS = (
    # A reading of the pass's OWN tree.  Legal, useful (it is how `CH-0182`'s self-destruction is
    # recorded), and NOT quotable: the tree it names is uncommitted and resolves nowhere.
    "atThisPassesTree",
    "atThisPassesWorkingTree",
    "workingTreeBeforeThisClaimsOwnFiles",
    "workingTree",
    "thisPassesTree",
    # A reading attributed to ANOTHER pass, in words rather than in a sha.  `T-332` records four
    # counts `atC0210sOwnRef` and the file carries no sha for that ref, so nothing in the artifact
    # can resolve it -- which is the same defect as naming no state at all, and is why the cure is
    # a recorded sha and not a better sentence.
    "atC0210sOwnRef",
    "sameQuantityAtC0210sOwnRef",
    "asWrittenBeforeThisPass",
    # A figure QUOTED from a predecessor in order to correct it.  It is a transcription, not a
    # measurement of this file's own state, and `C-0092`'s *a repair must leave the defect
    # measurable* is why it is retained rather than removed.
    "asPublishedByC0210",
)

#: A result file carrying one of these anywhere is a census-family file and must be classifiable.
CENSUS_MARKERS = (
    "selfDescribingCounts",
    "checkerCensus",
    "distinctToolsThatCanFailADefaultVerifyShRun",
)

_SHA = re.compile(r"^[0-9a-f]{40}$")


def _walk(node, path=()):
    if isinstance(node, dict):
        for key, value in node.items():
            for item in _walk(value, path + (key,)):
                yield item
    elif isinstance(node, list):
        for index, value in enumerate(node):
            for item in _walk(value, path + (str(index),)):
                yield item
    else:
        yield path, node


#: A key whose NAME announces a state.  `T-276` writes `checkerCensus/atRef/...` and `T-319` writes
#: `checkerCensus/namingPredicate/count` -- the second names no state and is pinned by its FILE's
#: own resolved `baselineRef`, which is what `gpd/README.md` means by a result file being
#: reproducible from itself.  So the DEFAULT is pinned-by-file, and the REFUSAL is narrower and
#: sharper: a key that LOOKS like a state declaration and is in neither vocabulary.  That is where
#: `C-0182`'s *an absence read as an answer* actually bites -- a new unpinned key invented next
#: pass will be state-shaped, because that is what such a key is for.
_STATE_SHAPED = re.compile(r"^(at[A-Z]|as[A-Z]|working[A-Z]|this[A-Z]|before[A-Z])|"
                           r"([Tt]ree|Ref|Pass|PassesTree)$")


def classify(path):
    """PINNED, UNPINNED or UNDECLARED for a JSON path inside a census-family file."""
    # LEFT TO RIGHT, first match wins.  An `atRef` nested INSIDE an `atThisPassesTree` block is
    # not pinned: the outer key says the whole subtree sits at a state that resolves nowhere, and
    # a rule that checked PINNED_KEYS over the whole path first would read it as pinned -- which
    # is the one direction this gate must not fail in.
    for key in path:
        if key in PINNED_KEYS:
            return "PINNED"
        if key in UNPINNED_KEYS:
            return "UNPINNED"
    for key in path:
        if _STATE_SHAPED.search(key):
            return "UNDECLARED"
    return "PINNED"


def _result_names(tree):
    """The result-file basenames of a state.  A stub tree may supply its own, so that every arm is
    reachable from a named test without touching the filesystem (`C-0161`: construct the state)."""
    if hasattr(tree, "result_names"):
        return sorted(tree.result_names())
    if tree.ref is None:
        return sorted(os.path.basename(p)
                      for p in glob.glob(os.path.join(tree.root, RESULTS, "*.json")))
    listing = subprocess.run(
        ["git", "-C", tree.root, "ls-tree", "--name-only", tree.ref, RESULTS + "/"],
        capture_output=True, text=True, check=True,
    ).stdout.split()
    return sorted(os.path.basename(p) for p in listing if p.endswith(".json"))


def census_files(tree):
    """`{relative path: parsed json}` for every result file carrying a census marker."""
    out = {}
    for name in _result_names(tree):
        text = tree.read(os.path.join(RESULTS, name))
        if text is None or not any(marker in text for marker in CENSUS_MARKERS):
            continue
        out[os.path.join(RESULTS, name)] = json.loads(text)
    return out


def records(tree):
    """Every numeric leaf of every census-family file, with its pinning verdict.

    A record is `(file, dotted path, value, verdict, ref)`.  `ref` is the file's own resolved
    `baselineRef` for a PINNED record and `None` otherwise -- `CH-0246`: a corpus-subject
    measurement's state has to be recordable, and a value under a key naming no state is not a
    value anything can check.
    """
    out = []
    for name, document in sorted(census_files(tree).items()):
        base = document.get("baselineRef")
        pinned_ref = base if isinstance(base, str) and _SHA.match(base) else None
        for path, value in _walk(document):
            if not isinstance(value, int) or isinstance(value, bool):
                continue
            if not any(marker in path for marker in CENSUS_MARKERS) \
                    and not any(key in path for key in PINNED_KEYS + UNPINNED_KEYS):
                continue
            verdict = classify(path)
            out.append((name, "/".join(path), value,
                        verdict if verdict != "PINNED" or pinned_ref else "UNPINNED",
                        pinned_ref if verdict == "PINNED" else None))
    return out


def pinned_values(tree, quantities=QUANTITIES):
    """`{quantity name: {value: [(file, ref)]}}` -- what a deliverable is allowed to quote."""
    out = dict((q.name, {}) for q in quantities)
    for name, document in sorted(census_files(tree).items()):
        base = document.get("baselineRef")
        if not (isinstance(base, str) and _SHA.match(base)):
            continue
        for path, value in _walk(document):
            if not isinstance(value, int) or isinstance(value, bool):
                continue
            for q in quantities:
                if tuple(path[-len(q.record_leaf):]) == q.record_leaf \
                        and classify(path) == "PINNED":
                    out[q.name].setdefault(value, []).append((name, base))
    return out


# --- the prose side -------------------------------------------------------------------------------

def blank_struck(text):
    """Blank `~~struck~~` spans, length- and newline-preserving.

    `C-0071`'s *strike, never delete* is what makes a historical reading safe to leave in place, so
    a checker that cannot read a strike PENALISES the repair discipline it exists to support
    (`C-0115`).  Length- and newline-preserving because otherwise every reported line number below
    the first strike is wrong.
    """
    out = list(text)
    # A link TARGET is a filename and a filename asserts nothing (`C-0196`, `C-0210`): blank the
    # target, keep the label.  Without this every `gpd/results/T-276-....json` puts a `276` on the
    # line and `CH-0014` a `14`, and the residue reads as a census of the corpus's own numbering.
    for match in re.finditer(r"\]\(([^)\s]*)\)", text):
        for index in range(match.start(1), match.end(1)):
            if out[index] != "\n":
                out[index] = " "
    for match in re.finditer(r"~~(.+?)~~", text, re.S):
        for index in range(match.start(), match.end()):
            if out[index] != "\n":
                out[index] = " "
    return "".join(out)


_ONES = {
    "zero": 0, "one": 1, "two": 2, "three": 3, "four": 4, "five": 5, "six": 6, "seven": 7,
    "eight": 8, "nine": 9, "ten": 10, "eleven": 11, "twelve": 12, "thirteen": 13,
    "fourteen": 14, "fifteen": 15, "sixteen": 16, "seventeen": 17, "eighteen": 18,
    "nineteen": 19,
}
_TENS = {"twenty": 20, "thirty": 30, "forty": 40, "fifty": 50, "sixty": 60, "seventy": 70,
         "eighty": 80, "ninety": 90}

_WORD = r"(?:%s)" % "|".join(sorted(list(_ONES) + list(_TENS), key=len, reverse=True))
#: `[<ones> hundred [and]] (<tens>[-<ones>] | <ones-or-teens>)`, case-insensitive.  Capped at
#: hundreds because every quantity in the registry is a file count well below a thousand; a
#: four-digit corpus would need this widened, and the widening is one clause.
SPELLED = re.compile(
    r"\b(?:(%s)\s+hundred(?:\s+and)?\s+)?(%s)(?:-(%s))?\b" % (_WORD, _WORD, _WORD), re.I
)


def spelled_value(text):
    """The integer an English numeral phrase denotes, or None."""
    match = SPELLED.fullmatch(text.strip())
    if not match:
        return None
    hundreds, first, second = match.group(1), match.group(2).lower(), match.group(3)
    total = 0
    if hundreds:
        head = _ONES.get(hundreds.lower())
        if head is None:
            return None
        total += 100 * head
    if first in _TENS:
        total += _TENS[first]
        if second:
            tail = _ONES.get(second.lower())
            if tail is None or not 1 <= tail <= 9:
                return None
            total += tail
    elif first in _ONES:
        if second:
            return None
        total += _ONES[first]
    else:
        return None
    return total


#: A digit sequence that is part of an IDENTIFIER is not a figure -- `T-295`, `C-0195`,
#: `T-334-gate-census.py`.  `C-0196`: a name cannot govern a token.  Without the guards the
#: predicate reads the corpus's own numbering back as a census of itself, measured at 4 false
#: positives over the two deliverables before they were added.
#: The TRAILING guard is `(?!\w)(?!-)(?!\.\d)` and NOT the symmetric mirror of the leading
#: one: `CLAUDE.md` records that `(?![\w.])` refuses every number at the END OF A SENTENCE,
#: which in this corpus is where a figure most often sits.  Written symmetrically first, and
#: a named test caught it on its first run -- the third recorded instance of that exact trap.
_NUMBER = re.compile(r"(?<![\w.\-])\d{1,4}(?![\w-])(?!\.\d)|" + SPELLED.pattern, re.I)
#: Report verbs only.  `is`/`are`/`=` were measured and dropped: they attribute any nearby
#: number to any nearby derivation, which is the numeral-pattern anchor this tool exists not to be.
_VERB = r"(?:reports?|returns?|reads?|prints?|gives?|shows?|finds?)"

#: Swept 60 / 80 / 100 / 120 / 150 / 200 / 300 in Execute; the classification is flat from 100 and
#: drifts at 200.  `C-0202`: take the PLATEAU, and never a window carried over from another
#: predicate.
WINDOW = 120


def _anchor_pattern(anchor):
    """A declared derivation, as a regular expression.

    `gpd/challenges/` and `gpd/claims/` are anchors only when BARE -- followed by no identifier --
    because otherwise every `[CH-0286](gpd/challenges/CH-0286-....md)` link in the corpus is one,
    which is several hundred false anchors and the reason a subject-word predicate was refused.
    """
    if anchor.endswith("/"):
        return re.escape(anchor) + r"(?![A-Za-z0-9])"
    return re.escape(anchor)


def prose_figures(text, quantities=QUANTITIES, window=WINDOW):
    """`[(line, quantity, value, token, sense)]` for every anchored figure in LIVE text."""
    live = blank_struck(text)
    found = []
    for q in quantities:
        for anchor, unit in q.anchors:
            for match in re.finditer(_anchor_pattern(anchor), live):
                # A DIRECTORY does not report anything -- only a tool or a command does -- so the
                # forward sense is restricted to command anchors and the reverse sense to
                # directory ones.  Measured over 103 revisions of the two deliverables: without
                # the split, `gpd/challenges/` forward-attributed a claim count to the challenge
                # quantity; with it, every one of the 23 distinct triples the predicate has ever
                # produced is a genuine self-describing count of its own quantity.
                after = live[match.end():match.end() + window]
                cut = after.find("\n\n")
                if cut >= 0:
                    after = after[:cut]
                verb = None if anchor.endswith("/") else re.search(_VERB, after, re.I)
                if verb:
                    # 15 characters, not 40: a report reads *"reports **247** challenge
                    # file(s)"*, and a 40-character reach lets `reports T-295 widgets, and is a
                    # gate where 606` attribute the 606.  Found by a named test on its first run.
                    tail = after[verb.end():verb.end() + 15]
                    number = _NUMBER.search(tail)
                    # The UNIT is searched in the full window past the number, not in the
                    # truncated tail: the proximity rule is about the FIGURE's distance from the
                    # verb, and the unit is a phrase that follows the figure.  Conflating the two
                    # silently drops every `reports **461** claims and challenges exist`.
                    unit_at = verb.end() + (number.end() if number else 0)
                    if number and (unit is None
                                   or re.search(unit, after[unit_at:unit_at + 40], re.I)):
                        found.append((live[:match.start()].count("\n") + 1, q,
                                      number.group(0), "anchor-then-figure"))
                # The reverse sense is for a SPELLED headline standing immediately in front of
                # its own derivation ("Two hundred and forty-seven challenges in
                # `gpd/challenges/`").  It carries no verb, so it gets a much tighter window --
                # a third of the forward one, swept and measured, because a bare "the last
                # number before this anchor" is exactly the unbounded numeral surface `C-0222`
                # refused.
                if not (q.noun and anchor.endswith("/")):
                    continue
                before = live[max(0, match.start() - window):match.start()]
                cut = before.rfind("\n\n")
                if cut >= 0:
                    before = before[cut:]
                # `_NUMBER` is an ALTERNATION, so it must be wrapped before anything is
                # concatenated to it -- `A|B` + `X` binds as `A | BX`, which silently drops the
                # digit branch and then matches a bare unit word.  Found by a named test on its
                # first run.
                reverse = re.search(
                    r"(?P<figure>" + _NUMBER.pattern + r")\s+" + re.escape(q.noun)
                    + r"[^.;:\n]{0,60}$", before, re.I)
                if reverse:
                    found.append((live[:match.start()].count("\n") + 1, q,
                                  reverse.group("figure"), "figure-then-anchor"))
    out = []
    for line, q, token, sense in found:
        value = int(token) if token.isdigit() else spelled_value(token)
        if value is None:
            continue
        out.append((line, q, value, token, sense))
    return sorted(set(out), key=lambda row: (row[0], row[1].name, row[2], row[3]))


#: A sha named in the figure's own sentence.  `CLAUDE.md`'s rule is *quote it with the state it
#: was read at*, and there are TWO ways to obey it: point at a record that pins the value, or name
#: the state in the sentence.  The second is what a HISTORICAL reading needs -- a paragraph whose
#: whole subject is the drift of a count must keep its old figures, and striking them to satisfy a
#: gate destroys the record the paragraph exists to keep (`C-0071`, from the other side).
#:
#: The GATED arm checks only that a sha is PRESENT and well shaped, because resolving it needs
#: `git` and `tools/snapshot.sh` excludes `./.git`.  Whether it re-derives there is `--rederive`.
_SHA_IN_PROSE = re.compile(r"(?<![0-9a-f])[0-9a-f]{7,40}(?![0-9a-f])")


def sentence_around(text, index):
    """The sentence carrying `index`, bounded by a full stop or a line break."""
    start = max(text.rfind(". ", 0, index), text.rfind("\n", 0, index)) + 1
    stop = min(x for x in (text.find(". ", index), text.find("\n", index), len(text)) if x >= 0)
    return text[start:stop]


def prose_defects(tree, quantities=QUANTITIES, window=WINDOW):
    """Anchored live figures that NO committed record pins and NO sha in their own sentence pins."""
    allowed = pinned_values(tree, quantities)
    defects, unreached = [], []
    for document in DELIVERABLES:
        text = tree.read(document)
        if text is None:
            continue
        figures = prose_figures(text, quantities, window)
        live_text = blank_struck(text)
        lines = live_text.split("\n")
        offsets, running = [], 0
        for row in lines:
            offsets.append(running)
            running += len(row) + 1
        flagged = set()
        for line, q, value, token, sense in figures:
            if value in allowed.get(q.name, {}):
                continue
            where = lines[line - 1].find(token)
            sentence = sentence_around(
                live_text, offsets[line - 1] + (where if where >= 0 else 0))
            if _SHA_IN_PROSE.search(sentence):
                continue
            pins = sorted(allowed.get(q.name, {}))
            defects.append((document, line, q, value, token, sense, pins))
            flagged.add(line)
        # `C-0209`: say what is not reached.  Once a line carries a membership failure, every
        # other numeral on it belongs in the substitution even where no declared anchor stands
        # beside it -- a spelled headline, or a forward reading with no derivation named.
        live = blank_struck(text).split("\n")
        reached = set((line, value) for line, _q, value, _t, _s in figures)
        # The floor is the smallest value any record pins for a quantity flagged ON THIS LINE.  A
        # corpus count is a population size, so a numeral an order of magnitude below the pinned
        # ones is an iteration number or a claim index, not a reading of the quantity -- and a
        # residue that lists those is a census of the corpus's own numbering (`C-0176`: measure a
        # narrowing's false negatives before writing it; measured here at 0 over both documents).
        for line in sorted(flagged):
            floors = [min(allowed[q.name]) for _d, l, q, _v, _t, _s, _p in defects
                      if l == line and allowed.get(q.name)]
            floor = min(floors) if floors else 10
            for match in _NUMBER.finditer(live[line - 1]):
                token = match.group(0)
                value = int(token) if token.isdigit() else spelled_value(token)
                if value is None or value < floor or (line, value) in reached:
                    continue
                unreached.append((document, line, value, token))
    return defects, sorted(set(unreached))


# --- the gate -------------------------------------------------------------------------------------

def check(tree, quantities=QUANTITIES, window=WINDOW):
    """The git-free arms.  Arm 3's rows are returned separately: it is RED at HEAD (see the header)."""
    defects = []

    # arm 1 -- a recorded count must be PINNED or explicitly declared unpinned, never undeclared,
    # and its file must carry a RESOLVED baselineRef rather than `HEAD` or nothing (`CH-0246`).
    for name, document in sorted(census_files(tree).items()):
        base = document.get("baselineRef")
        if not isinstance(base, str) or not _SHA.match(base):
            defects.append(
                "UNRESOLVED-BASELINE  %s records a self-describing count and its baselineRef is "
                "%r -- a corpus-subject measurement's state must be a resolved 40-hex sha, so "
                "that what it records can be checked" % (name, base)
            )
    for name, path, _value, verdict, _ref in records(tree):
        if verdict == "UNDECLARED":
            defects.append(
                "UNDECLARED-RECORD-KEY  %s /%s -- neither PINNED_KEYS nor UNPINNED_KEYS "
                "classifies this key, so nothing can say whether the value it carries names a "
                "state. Declare it in one of the two, never default it" % (name, path)
            )

    # arm 2 -- a quantity may not be declared against a tool nothing runs.
    reading = t334.census(tree)
    present = tree.tools()
    union = set(reading["union"])
    for q in quantities:
        for anchor, unit in q.anchors:
            if anchor.startswith("tools/") and unit is None:
                defects.append(
                    "TOOL-ANCHOR-WITHOUT-A-UNIT  quantity %r anchors on %s and declares no unit "
                    "-- a tool prints more than one count, so an anchor on the tool alone "
                    "attributes every nearby number to this quantity. Declare the phrase the "
                    "figure must be followed by, or anchor on a command unique to it"
                    % (q.name, anchor)
                )
        if q.deriver not in present:
            defects.append("MISSING-DERIVER  quantity %r declares tools/%s and it does not exist"
                           % (q.name, q.deriver))
        elif not present.get(q.deriver):
            defects.append("NON-EXECUTABLE-DERIVER  quantity %r declares tools/%s and it carries "
                           "no executable bit" % (q.name, q.deriver))
        elif q.deriver not in union:
            defects.append(
                "DERIVER-CANNOT-FAIL-THE-RUN  quantity %r declares tools/%s, which is NOT on "
                "T-334's union of tools that can fail a default tools/verify.sh run -- so the "
                "claim that this count is derived on every run is false" % (q.name, q.deriver)
            )

    prose, unreached = prose_defects(tree, quantities, window)
    return reading, defects, prose, unreached


def rederive(tree):
    """Every PINNED record re-derived at its own recorded ref.  NEEDS `git`.

    `tools/snapshot.sh` excludes `./.git`, so this arm cannot run inside a `tools/verify.sh`
    snapshot.  `C-0195`: a fixture layout is a dependency declaration, and a check that degrades
    silently in a scratch tree is worse than one that refuses -- so the skip is VISIBLE and it goes
    to stderr, because a `--self-test > /dev/null` swallows stdout.
    """
    if not os.path.exists(os.path.join(tree.root, ".git")):
        sys.stderr.write(
            "T-336: SKIPPED the re-derivation arm -- %s carries no .git, so a pinned ref cannot "
            "be resolved. This is expected inside a tools/verify.sh snapshot; run the arm "
            "directly in the checkout.\n" % tree.root
        )
        return None
    rows = []
    for q in QUANTITIES:
        for value, sources in sorted(pinned_values(tree).get(q.name, {}).items()):
            for name, ref in sources:
                rows.append((name, q, value, q.derive(Tree(ref, tree.root)), ref))
    return rows


# --- reporting --------------------------------------------------------------------------------------

def report(tree, window=WINDOW):
    reading, defects, prose, unreached = check(tree, window=window)
    all_records = records(tree)
    pinned = [row for row in all_records if row[3] == "PINNED"]
    unpinned = [row for row in all_records if row[3] == "UNPINNED"]
    print("a count the deliverables PRINT against the one a result file PINS, at %s" % tree.label)
    if tree.ref:
        print("  resolved ref: %s" % tree.ref)
    print()
    print("  %-56s %s" % ("declared quantities", len(QUANTITIES)))
    print("  %-56s %s" % ("census-family result files", len(census_files(tree))))
    print("  %-56s %s" % ("PINNED records (a value + a resolved ref)", len(pinned)))
    print("  %-56s %s" % ("UNPINNED records (a value at no state)", len(unpinned)))
    print()
    print("  the registry -- a quantity is DECLARED or it is refused:")
    for q in QUANTITIES:
        values = sorted(pinned_values(tree).get(q.name, {}))
        print("    %-22s %-30s pinned at %s"
              % (q.name, "tools/" + q.deriver, values if values else "NOTHING"))
    print()
    print("  what the deliverables print, against what is pinned:")
    if not prose:
        print("    every anchored live figure is a value some record pins")
    for document, line, q, value, token, sense, pins in prose:
        print("    NOT-PINNED  %s:%d  %s = %s (%r, %s); pinned values are %s"
              % (document, line, q.name, value, token, sense, pins))
    print()
    print("  NOT reached, and why (a gate that comes clean must say what it does not reach):")
    print("    %-54s %s" % ("numerals on a flagged line carrying no anchor", len(unreached)))
    for document, line, value, token in unreached:
        print("        %s:%d  %s (%r)" % (document, line, value, token))
    print("    %-54s %s" % ("UNPINNED records nothing quotes (T-340)", len(unpinned)))
    for name, path, value, _v, _r in unpinned:
        print("        %s /%s = %s" % (name, path, value))
    print("    %-54s %s" % ("figures inside struck spans", "blanked, C-0071"))
    print("    %-54s %s"
          % ("a quantity no committed tool derives on every run", "outside the registry, refused"))
    print("    %-54s %s" % ("re-derivation at a pinned ref", "--rederive; needs .git"))
    print()
    print("  arm 1 + arm 2 defects: %d.  Arm 3 (prose) is %s: %d membership failure(s)"
          % (len(defects), "GATED" if PROSE_ARM_IS_GATED else "PRINTED, NOT GATED", len(prose)))
    print("# %d declared quantit(ies); %d pinned record(s); %d prose figure(s) pinned by nothing "
          "at %s" % (len(QUANTITIES), len(pinned), len(prose), tree.label))
    return 0


# --- self-tests -----------------------------------------------------------------------------------

_FIXTURE_ANSWERS = """Live prose.
tools/alpha.py reports **7** widgets and gpd/challenges/CH-0001-x.md is only a link.
Two hundred and forty-seven challenges in [`gpd/challenges/`](gpd/challenges/), against 9 claims.
~~tools/alpha.py reports **99** widgets~~ is struck and asserts nothing.
`ls gpd/challenges/CH-*.md \\| wc -l` returns **41** and 52 and [a note](notes/9999/x.md) and 4242.
tools/alpha.py reports **4242** widgets at a tree that resolves nowhere.
tools/alpha.py reports T-295 widgets at a tree, and nothing else on this line.
tools/alpha.py is **606** widgets by a verb no report ever uses.
tools/alpha.py reports **3** gadgets, a different quantity printed by the same tool.
`gpd/challenges/` reports 707 and 808 challenges sit in `ls gpd/challenges/CH-*.md` here.
909 things in [`gpd/challenges/`](gpd/challenges/) is the wrong noun entirely.
An unflagged line mentioning 8888 and nothing else at all.
tools/alpha.py names a widget and then a great deal of unrelated prose runs on for well over one
"""

_FIXTURE_DECISIONS = "Nothing self-describing here at all.\n"

_FIXTURE_RESULT = {
    "task": "T-000",
    "baselineRef": "0" * 40,
    "selfDescribingCounts": {
        "widgets": {"atRef": 7, "command": "tools/alpha.py"},
        "gadgets": {"atRef": 3},
        "challenges": {"atRef": 247},
        "asWrittenBeforeThisPass": {"widgets": 4242},
    },
    "atThisPassesTree": {
        "selfDescribingCounts": {"widgets": {"atRef": 4242}},
    },
}


class _StubTree(object):
    """A repository state held in memory, so every arm is reachable from a named test."""

    label = "stub"
    ref = None
    root = os.path.join(os.sep, "nonexistent-T-336-stub")

    def __init__(self, result=None, answers=None, decisions=None, tools=None,
                 baseline_ref="0" * 40, extra_leaf=None):
        self.result = json.loads(json.dumps(_FIXTURE_RESULT if result is None else result))
        if baseline_ref is not _KEEP:
            self.result["baselineRef"] = baseline_ref
        if extra_leaf is not None:
            self.result["selfDescribingCounts"]["atSomeUndeclaredMoment"] = {"count": extra_leaf}
            self.result["selfDescribingCounts"]["plainSubtotal"] = {"count": extra_leaf}
        self.answers = _FIXTURE_ANSWERS if answers is None else answers
        self.decisions = _FIXTURE_DECISIONS if decisions is None else decisions
        self._tools = {"alpha.py": True, "beta.py": True, "delta.py": True,
                       "gamma.py": True} if tools is None else tools

    def result_names(self):
        return ["T-000-stub.json", "T-001-not-a-census.json"]

    def read(self, path):
        if path == t334.BUILD:
            return t334._FIXTURE_BUILD
        if path == t334.VERIFY:
            return t334._FIXTURE_VERIFY
        if path == t334.HARNESS_CENSUS:
            return 'BY_HAND = "BY-HAND"\nHARNESSES = (\n    ("beta.py", "K", "S", "t", (BY_HAND,)),\n)\n'
        if path == os.path.join(RESULTS, "T-000-stub.json"):
            return json.dumps(self.result)
        if path == os.path.join(RESULTS, "T-001-not-a-census.json"):
            return '{"task": "T-001", "peakDishing": 3}'
        if path == "ANSWERS.md":
            return self.answers
        if path == "DECISIONS-FOR-NDI.md":
            return self.decisions
        return None

    def tools(self):
        return dict(self._tools)


_KEEP = object()

_WIDGETS = Quantity("widgets", "widgets", "alpha.py", (("tools/alpha.py", r"widget"),),
                    ("selfDescribingCounts", "widgets", "atRef"), lambda tree: 7)
_STUB_CHALLENGES = Quantity(
    "challenges", "challenges", "delta.py",
    (("ls gpd/challenges/CH-*.md", None), ("gpd/challenges/", None)),
    ("selfDescribingCounts", "challenges", "atRef"), lambda tree: 247, noun="challenges",
)
#: A SECOND quantity on the SAME tool anchor, discriminated only by its unit -- which is what
#: `tools/check-corpus-identifiers.py` does in the corpus (a dangling count and a total).  Without
#: it no fixture can tell whether the unit is load-bearing.
_GADGETS = Quantity("gadgets", "gadgets", "alpha.py", (("tools/alpha.py", r"gadget"),),
                    ("selfDescribingCounts", "gadgets", "atRef"), lambda tree: 3)
_STUB_QUANTITIES = (_WIDGETS, _STUB_CHALLENGES, _GADGETS)


def _self_test():
    checks = []

    def ok(name, passed):
        checks.append((name, bool(passed)))

    # --- the English numeral parser, which is what lets a spelled headline be read at all -------
    ok("T-336 spelled_value reads a bare unit", spelled_value("seven") == 7)
    ok("T-336 spelled_value reads a teen", spelled_value("eleven") == 11)
    ok("T-336 spelled_value reads a hyphenated ten", spelled_value("forty-four") == 44)
    ok("T-336 spelled_value reads a bare ten", spelled_value("twenty") == 20)
    ok("T-336 spelled_value reads hundreds with 'and'",
       spelled_value("Two hundred and forty-seven") == 247)
    ok("T-336 spelled_value reads hundreds without 'and'",
       spelled_value("four hundred sixty-one") == 461)
    ok("T-336 spelled_value is case-insensitive, as FIFTY-ONE in a heading is",
       spelled_value("FIFTY-ONE") == 51)
    ok("T-336 spelled_value REFUSES a unit-hyphen-unit form that denotes nothing",
       spelled_value("seven-eight") is None)
    ok("T-336 spelled_value REFUSES a non-numeral word", spelled_value("challenges") is None)

    # --- struck text asserts nothing (`C-0071`) --------------------------------------------------
    blanked = blank_struck("a ~~b\nc~~ d")
    ok("T-336 blank_struck preserves length", len(blanked) == len("a ~~b\nc~~ d"))
    ok("T-336 blank_struck preserves newlines, so a reported line number survives",
       blanked.count("\n") == 1)
    ok("T-336 blank_struck removes the struck text", "b" not in blanked and "c" not in blanked)

    # --- the anchor is a DERIVATION, and a bare directory is not a link --------------------------
    ok("T-336 a bare gpd/challenges/ anchors",
       re.search(_anchor_pattern("gpd/challenges/"), "in (gpd/challenges/)") is not None)
    ok("T-336 gpd/challenges/CH-0001-x.md does NOT anchor, or every corpus link would",
       re.search(_anchor_pattern("gpd/challenges/"),
                 "(gpd/challenges/CH-0001-x.md)") is None)

    # --- classification, and the third state -----------------------------------------------------
    ok("T-336 classify calls atRef PINNED", classify(("selfDescribingCounts", "x", "atRef")) == "PINNED")
    ok("T-336 classify calls atBaselineRef PINNED", classify(("atBaselineRef", "n")) == "PINNED")
    ok("T-336 classify calls a declared tree key UNPINNED",
       classify(("atThisPassesTree", "n")) == "UNPINNED")
    ok("T-336 classify REFUSES a STATE-SHAPED key neither vocabulary declares",
       classify(("selfDescribingCounts", "atSomeUndeclaredMoment", "count")) == "UNDECLARED")
    ok("T-336 an atRef NESTED inside a working-tree block is UNPINNED, first key on the path wins",
       classify(("atThisPassesTree", "selfDescribingCounts", "widgets", "atRef")) == "UNPINNED")
    ok("T-336 a value pinned nowhere but under a nested atRef is not quotable",
       4242 not in pinned_values(_StubTree(), _STUB_QUANTITIES)["widgets"])
    ok("T-336 classify calls a key naming NO state pinned by the file's own baselineRef",
       classify(("checkerCensus", "namingPredicate", "count")) == "PINNED")
    ok("T-336 the refusal is narrow: a plain subtotal is not state-shaped and is not refused",
       classify(("selfDescribingCounts", "plainSubtotal", "count")) == "PINNED")
    ok("T-336 quantity() refuses an undeclared name rather than returning a default",
       _refuses_key(lambda: quantity("notDeclaredAnywhere")))
    ok("T-336 the refusal is a KeyError from the guard, not an incidental lookup miss",
       "T-336 refuses the undeclared quantity" in _refusal_text(lambda: quantity("nope")))
    ok("T-336 quantity() returns a declared one", quantity("claims").name == "claims")

    # --- census-family discovery -------------------------------------------------------------------
    ok("T-336 census_files picks up a file carrying a marker",
       os.path.join(RESULTS, "T-000-stub.json") in census_files(_StubTree()))
    ok("T-336 census_files ignores a result file carrying no marker",
       os.path.join(RESULTS, "T-001-not-a-census.json") not in census_files(_StubTree()))

    # --- what a deliverable may quote ------------------------------------------------------------
    ok("T-336 pinned_values collects a value under a PINNED key",
       247 in pinned_values(_StubTree()).get("challenges", {}))
    ok("T-336 pinned_values collects NOTHING from a file whose baselineRef does not resolve",
       pinned_values(_StubTree(baseline_ref="HEAD")).get("challenges") == {})
    ok("T-336 a value recorded ONLY under an unpinned key is not quotable -- the whole point",
       4242 not in pinned_values(_StubTree()).get("challenges", {}))

    # --- the prose arm -----------------------------------------------------------------------------
    figures = prose_figures(_FIXTURE_ANSWERS, _STUB_QUANTITIES)
    ok("T-336 the anchor-then-figure sense reads `tools/alpha.py reports **7**`",
       any(row[2] == 7 and row[4] == "anchor-then-figure" for row in figures))
    ok("T-336 the figure-then-anchor sense reads a SPELLED headline before gpd/challenges/",
       any(row[2] == 247 and row[4] == "figure-then-anchor" for row in figures))
    ok("T-336 a figure inside a struck span is not read at all",
       not any(row[2] == 99 for row in figures))
    ok("T-336 a glob anchor reads `ls gpd/challenges/CH-*.md ... returns **41**`",
       any(row[2] == 41 for row in figures))

    prose, unreached = prose_defects(_StubTree(), _STUB_QUANTITIES)
    values = set((row[2].name, row[3]) for row in prose)
    reported = set(row[2] for row in unreached)
    ok("T-336 an ANCHORED figure recorded only under an unpinned key is a defect -- the point",
       ("widgets", 4242) in values)
    ok("T-336 a UNIT discriminates two quantities one tool prints: 3 gadgets is not 3 widgets",
       ("widgets", 3) not in values and ("gadgets", 7) not in values)
    ok("T-336 a digit inside an identifier is not a figure -- `reports T-295 widgets`",
       ("widgets", 295) not in values)
    ok("T-336 a weak verb does not attribute a nearby number -- `is a gate where 606 is`",
       ("widgets", 606) not in values)
    ok("T-336 a DIRECTORY does not report: `gpd/challenges/ reports 707` attributes nothing",
       ("challenges", 707) not in values)
    ok("T-336 the reverse sense does not run on a COMMAND anchor -- 808 before an ls",
       ("challenges", 808) not in values)
    ok("T-336 the reverse sense needs its own NOUN -- `909 things in gpd/challenges/` is not one",
       ("challenges", 909) not in values)
    ok("T-336 a link TARGET's digits are not a figure and not a residue member (C-0196)",
       9999 not in reported)
    ok("T-336 the residue floors at the smallest PINNED value, so 52 is not a challenge count",
       52 not in reported)
    ok("T-336 the residue lists only FLAGGED lines -- 8888 on a clean line is not in it",
       8888 not in reported)
    ok("T-336 arm 1 fires on an ABBREVIATED sha, which resolves only against a repository",
       any("UNRESOLVED-BASELINE" in defect
           for defect in check(_StubTree(baseline_ref="d9a3522"), _STUB_QUANTITIES)[1]))
    ok("T-336 a record leaf is matched WHOLE, so a `challenges` atRef does not pin `widgets`",
       set(pinned_values(_StubTree(), _STUB_QUANTITIES)["widgets"]) == {7})
    ok("T-336 a figure a record PINS is clean", not any(row[3] == 7 for row in prose))
    ok("T-336 a figure NO record pins is a defect", any(row[3] == 41 for row in prose))
    ok("T-336 a figure pinned by a SHA NAMED IN ITS OWN SENTENCE is clean -- the other way to obey",
       not any(row[3] == 41 for row in prose_defects(
           _StubTree(answers="`ls gpd/challenges/CH-*.md` returns **41** at 05562ea.\n"),
           _STUB_QUANTITIES)[0]))
    ok("T-336 a sha in a DIFFERENT sentence does not pin the figure",
       any(row[3] == 41 for row in prose_defects(
           _StubTree(answers="`ls gpd/challenges/CH-*.md` returns **41**. Elsewhere: 05562ea.\n"),
           _STUB_QUANTITIES)[0]))
    ok("T-336 a short hex-looking word is not a sha -- seven characters is the floor",
       any(row[3] == 41 for row in prose_defects(
           _StubTree(answers="`ls gpd/challenges/CH-*.md` returns **41** at abcdef.\n"),
           _STUB_QUANTITIES)[0]))
    ok("T-336 the struck figure is not a defect", not any(row[3] == 99 for row in prose))
    ok("T-336 the residue lists an unanchored numeral on a FLAGGED line (C-0209)",
       any(row[2] == 4242 for row in unreached))
    ok("T-336 the residue is empty where no line is flagged",
       prose_defects(_StubTree(answers="tools/alpha.py reports **7** widgets.\n"),
                     _STUB_QUANTITIES)[1] == [])
    ok("T-336 a number beyond the window is not attributed to the anchor",
       not any(row[2] == 7 for row in
               prose_figures("tools/alpha.py names a widget." + " padding" * 40 +
                             " reports 7 widgets\n", (_WIDGETS,), window=40)))

    # --- arm 1 ---------------------------------------------------------------------------------------
    ok("T-336 the gate is CLEAN on a stub whose records are pinned and whose derivers run",
       check(_StubTree(tools={"alpha.py": True, "beta.py": True, "delta.py": True,
                              "gamma.py": True}), _STUB_QUANTITIES)[1] == [])
    ok("T-336 arm 1 fires where a census-family file's baselineRef is not a resolved sha",
       any("UNRESOLVED-BASELINE" in defect
           for defect in check(_StubTree(baseline_ref="HEAD"), _STUB_QUANTITIES)[1]))
    ok("T-336 arm 1 fires where baselineRef is absent altogether",
       any("UNRESOLVED-BASELINE" in defect
           for defect in check(_StubTree(baseline_ref=None), _STUB_QUANTITIES)[1]))
    ok("T-336 arm 1 fires on a STATE-SHAPED key neither vocabulary declares",
       any("UNDECLARED-RECORD-KEY" in defect and "atSomeUndeclaredMoment" in defect
           for defect in check(_StubTree(extra_leaf=5), _STUB_QUANTITIES)[1]))
    ok("T-336 arm 1 does NOT fire on a key that names no state at all",
       not any("plainSubtotal" in defect
               for defect in check(_StubTree(extra_leaf=5), _STUB_QUANTITIES)[1]))
    ok("T-336 arm 1 does NOT fire on a key UNPINNED_KEYS declares",
       not any("UNDECLARED-RECORD-KEY" in defect and "asWrittenBeforeThisPass" in defect
               for defect in check(_StubTree(), _STUB_QUANTITIES)[1]))

    # --- arm 2 ---------------------------------------------------------------------------------------
    ok("T-336 arm 2 fires where a declared deriver does not exist",
       any("MISSING-DERIVER" in defect for defect in
           check(_StubTree(tools={"beta.py": True, "delta.py": True, "gamma.py": True}),
                 _STUB_QUANTITIES)[1]))
    ok("T-336 arm 2 fires where a declared deriver carries no executable bit",
       any("NON-EXECUTABLE-DERIVER" in defect for defect in
           check(_StubTree(tools={"alpha.py": False, "beta.py": True, "delta.py": True,
                                  "gamma.py": True}), _STUB_QUANTITIES)[1]))
    ok("T-336 arm 2 fires where a deriver cannot fail the run -- an unreachable Exec task",
       any("DERIVER-CANNOT-FAIL-THE-RUN" in defect for defect in
           check(_StubTree(), (Quantity("q", "q", "beta.py", (("tools/beta.py", None),),
                                        ("selfDescribingCounts", "widgets", "atRef"),
                                        lambda tree: 0),))[1]))

    # --- the git-dependent arm refuses VISIBLY rather than degrading silently (`C-0195`) ----------
    ok("T-336 rederive SKIPS where the state carries no .git, and says so on stderr",
       _returns(lambda: rederive(_StubTree()), None))
    ok("T-336 the prose arm is not gated until T-339 flips one constant",
       PROSE_ARM_IS_GATED is False)

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def _refusal_text(thunk):
    try:
        thunk()
    except KeyError as failure:
        return str(failure)
    return ""


def _returns(thunk, expected):
    """True iff `thunk()` returns `expected`.  A mutation that makes it RAISE must fail a NAMED
    test, not stop the suite -- an unfinished suite is a SURVIVOR (`T-306`), which is the quiet
    direction and the one that flatters."""
    try:
        return thunk() == expected
    except Exception:
        return False


def _refuses_key(thunk):
    try:
        thunk()
    except KeyError:
        return True
    return False


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="A count a deliverable prints, against the one a result file pins."
    )
    parser.add_argument("--ref", default=None,
                        help="a git ref to read at (default: the working tree)")
    parser.add_argument("--check", action="store_true",
                        help="the git-free gated arms; exit 1 on any defect")
    parser.add_argument("--prose", action="store_true",
                        help="the prose membership arm, printed")
    parser.add_argument("--strict", action="store_true",
                        help="with --prose, exit 1 on a membership failure (T-339 wires this)")
    parser.add_argument("--rederive", action="store_true",
                        help="re-derive every pinned record at its own ref; needs .git")
    parser.add_argument("--window", type=int, default=WINDOW,
                        help="the prose anchor window, in characters (default: %d)" % WINDOW)
    parser.add_argument("--self-test", dest="self_test", action="store_true",
                        help="named self-tests over in-memory fixtures; reads no repository state")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    tree = Tree(arguments.ref)

    if arguments.rederive:
        rows = rederive(tree)
        if rows is None:
            return 0
        bad = 0
        for name, q, recorded, derived, ref in rows:
            agrees = recorded == derived
            bad += 0 if agrees else 1
            print("%-9s %-40s %-20s recorded=%-5s derived=%-5s %s"
                  % ("OK" if agrees else "MISMATCH", os.path.basename(name), q.name,
                     recorded, derived, ref[:7]))
        print("# %d pinned record(s) re-derived at their own ref; %d mismatch(es)"
              % (len(rows), bad))
        return 1 if bad else 0

    if arguments.prose:
        _reading, _defects, prose, unreached = check(tree, window=arguments.window)
        allowed = pinned_values(tree)
        for document, line, q, value, token, sense, pins in prose:
            print("NOT-PINNED  %s:%d  %s = %s (%r, %s); no record pins it; pinned: %s"
                  % (document, line, q.name, value, token, sense, pins))
        for document, line, value, token in unreached:
            print("UNREACHED   %s:%d  %s (%r) -- on a flagged line, carrying no declared anchor"
                  % (document, line, value, token))
        print("# %d prose figure(s) pinned by nothing, %d unreached numeral(s) on flagged lines; "
              "gated: %s" % (len(prose), len(unreached),
                             PROSE_ARM_IS_GATED or arguments.strict))
        return 1 if prose and (PROSE_ARM_IS_GATED or arguments.strict) else 0

    if arguments.check:
        reading, defects, prose, unreached = check(tree, window=arguments.window)
        for defect in defects:
            print(defect)
        for document, line, q, value, token, sense, pins in prose:
            print("residue (NOT gated until T-339): NOT-PINNED  %s:%d  %s = %s (%r); pinned: %s"
                  % (document, line, q.name, value, token, pins))
        for document, line, value, token in unreached:
            print("residue (NOT gated, NOT reached): %s:%d  %s (%r)" % (document, line, value, token))
        gated = defects + (prose if PROSE_ARM_IS_GATED else [])
        print("# %d defect(s) at %s; %d declared quantit(ies), %d pinned record(s); "
              "prose residue %d figure(s) + %d unreached, %s"
              % (len(gated), reading["state"], len(QUANTITIES),
                 len([r for r in records(tree) if r[3] == "PINNED"]),
                 len(prose), len(unreached),
                 "GATED" if PROSE_ARM_IS_GATED else "PRINTED, NOT GATED (T-339)"))
        return 1 if gated else 0

    return report(tree, arguments.window)


if __name__ == "__main__":
    sys.exit(main())
