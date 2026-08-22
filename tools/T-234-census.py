#!/usr/bin/env python3
"""T-234 — census of the premises `C-0140` and `C-0141` withdrew, across the corpus.

Iteration 33 filed two structural corrections and neither swept what it moved:

  * `C-0141` — the cross-section every four-layer claim is written on is **not a honeycomb**
    (every four-layer `edgeY` is exactly 1.5x too small, the footprint ordering between
    `15 x 4` and `10 x 6` REVERSES, and `C-0116`'s interlayer-coupling threshold for
    `15 x 4` moves inside the measured band); and a honeycomb face carries exactly ONE
    rooting azimuth per helix, at 30 degrees, with NO perpendicular root anywhere.
  * `C-0140` — a honeycomb x-raster carries BOTH turn senses, so there is no uniform
    honeycomb row length at all, and design (i) is a p7560 design.

`CLAUDE.md`: *a discharge is invisible to whoever files the removal.*  This tool is the
mechanical half of the sweep.  It

  * reproduces the census by regular expression (`--census`), over five named premise
    families, so the denominator is derived rather than remembered;
  * carries the per-occurrence classification as retained data
    (`tools/T-234-classification.json`), because the class is a READING and must be
    inspectable and falsifiable one occurrence at a time;
  * and gates it (`--check`): every occurrence classified `MOVED` or `DISCHARGED` must
    carry a pointer to the claim or challenge that moved it, within `POINTER_WINDOW`
    characters, or be struck.

The five families:

  FOOTPRINT   a four-layer cross-section's plan geometry, or a dishing/threshold number
              solved on it.  `C-0141` moves every one of them.
  WIDTH       a honeycomb row length asserted as a uniform tile width.  `C-0140` shows
              no uniform honeycomb row length exists.
  AZIMUTH     a perpendicular rooting azimuth on a honeycomb face, or the 60-degree
              azimuth, or `CH-0151`'s 132/90 census.  `C-0141` withdraws all three.
  SCAFFOLD    the scaffold design (i) is folded from.  `C-0140` derives p7560 where the
              corpus carries p8064.
  PLACEMENT   a statement that this repository has no honeycomb station lattice, plan
              ceiling or placement family.  `C-0141` supplies all three.

The four classes:

  MOVED        the passage asserts a premise the two claims withdrew.  **Strike or point.**
  DISCHARGED   the passage asserts the ABSENCE of something `C-0141` now supplies.
               **Also strike or point** -- the same repair, the opposite direction.
  RECORD       a synthesis claim recording what a past pass carried, or a verbatim source
               quotation.  The sentence was true when written and is a historical record.
               **Leave alone, with the reason recorded.**
  CORRECT      the occurrence is inside the correcting claim or challenge itself, or it
               states the corrected value.  **Leave alone.**
  OUT_OF_SCOPE the token matched and the statement is about something else -- a
               square-lattice width, a junction-routing azimuth, a format string.
               **Leave alone, with the reason recorded.**

Verified by `--self-test`.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
CLASSIFICATION = os.path.join(HERE, "T-234-classification.json")

#: The claims and challenges that withdrew the premises.  A pointer to any of them, within
#: POINTER_WINDOW characters after the occurrence, discharges it.
POINTERS = (
    "C-0140", "C-0141", "CH-0172", "CH-0173", "CH-0174", "CH-0175", "CH-0180", "CH-0181", "T-233",
    "T-234",
)
POINTER_WINDOW = 900

#: The discharge this census is ABOUT.  A census is defined by the discharge it is about, and a
#: token that spans two discharges belongs to two censuses -- which is the whole of `T-260`.
SUBJECT = "C-0140/C-0141"

#: Every discharge a family of this census can belong to.  `C-0141` supplied the station lattice,
#: the plan ceiling and the placement family; it did **not** supply a grillage, because
#: `OrigamiGrillage` never reads `layers` (`C-0154`, `T-253`) -- so the ONE token
#: `single-layer square-lattice` carries two statements with two correcting claims and two dates.
#: A family whose discharge is `None` is not a debt at all: it is the RESTORED reading, or the
#: token used of an object that genuinely is a single-layer square-lattice sheet.
DISCHARGES = {
    SUBJECT: POINTERS,
    "C-0154/C-0167": ("C-0154", "C-0167", "CH-0213", "T-253", "T-263"),
    None: (),
}

#: A claim whose HEADLINE carries an annotation banner is a repaired claim: a reader cannot reach
#: any number in it without passing the banner.  Requiring a pointer within POINTER_WINDOW of every
#: occurrence instead would demand sprinkling pointers through result tables, which is not how this
#: corpus annotates and which damages the tables it would be sprinkled through.  So a headline
#: pointer discharges the whole file -- and the window is deliberately short, so that a pointer
#: buried in a body section cannot pass as one.
HEADLINE_WINDOW = 3000

CLASSES = (
    "MOVED", "DISCHARGED", "RECORD", "CORRECT", "OUT_OF_SCOPE", "SURVIVING", "RESTATED",
)
#: The classes the gate requires a pointer for.
ADDRESSED = ("MOVED", "DISCHARGED")
#: The classes admissible on a family that belongs to another discharge, or to none.  `SURVIVING`
#: is `T-260`'s: the half of a partially discharged premise the correcting claim did NOT supply,
#: live when written and the subject of a different census.  `RESTATED` is `T-262`'s: the token in
#: its RESTORED reading, which is the correcting statement itself rather than the debt.
NON_SUBJECT_CLASSES = ("SURVIVING", "RESTATED", "RECORD", "CORRECT", "OUT_OF_SCOPE")

#: The two outward-facing documents.  `T-233` owns them; this task produces their list and
#: does not edit them, so their debt is reported on its own line and does NOT fail the gate.
#: `CLAUDE.md`: a check that can never come back clean cannot be a gate.
DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")

#: Identifiers that look numeric and are not quantities.  Blanked before matching, so that
#: `T-132` cannot be read as `CH-0151`'s 132-station census.  Length-preserving, or every
#: reported offset below the first identifier would be wrong.
_ID_PATTERNS = [
    re.compile(r"\b(?:CH|C|P|T|S)-\d{1,4}[a-z]?\b"),
]

STRIKE = re.compile(r"~~.*?~~", re.DOTALL)

#: Context a match must ALSO satisfy, on its own line, for the families whose token is
#: ambiguous.  `112 bp` is the square lattice's buildable width as well as the honeycomb's
#: nominal row, and `perpendicular` is a junction-routing word.
_HONEYCOMB = r"honeycomb|four-layer|four layer|15 . 4|10 . 6"
_AZIMUTH_CTX = r"honeycomb|azimuth|oblique|top face|top-face|station|sublattice|15 . 4|10 . 6"

#: Markdown emphasis, which sits between a token and the noun that governs it often enough that a
#: predicate written without stripping it reads `**single-layer square-lattice**\n number` as a
#: bare predicate rather than as an attributive use.
_EMPHASIS = re.compile(r"[*`~_]+")

#: How far either side of a token a governing word is looked for.  A refinement is a statement
#: about the SENTENCE the token stands in, and a sentence in this corpus is shorter than this.
REFINE_WINDOW = 300

#: The structural-model test is a PROXIMITY test, not a phrase test, so it needs its own and much
#: tighter radius: at `REFINE_WINDOW` it reached a *"coupled cell"* **253 characters away, in a
#: different sentence*, and read `ANSWERS.md`'s own *"every plan ceiling, station lattice, crossover
#: phase and placement in this corpus is single-layer square-lattice"* as a grillage statement.
#: Swept, the split is a PLATEAU -- 17/13/8 at 80, 100, 120 and 150, drifting only at 200 (18/12/8)
#: and 300 (19/11/8) -- so 120 is the middle of a flat region rather than a fitted number.
STRUCTURAL_WINDOW = 120

#: `T-262`'s second instance.  A `TASKS.md` row is a paragraph on one physical line, so the
#: line-scoped honeycomb context is no context at all there: the `T-9` row's nearest honeycomb word
#: is over three thousand characters from its `112 bp`, which is a SQUARE-lattice oxDNA design.
#: Sentence-scoping the context test would drop 56 of 103 WIDTH occurrences, most of them genuine,
#: so the tool measures the distance, REPORTS it, and refuses to guess; the per-occurrence hand
#: override in `tools/T-234-classification.json` is where a reader settles it.
CONTEXT_REMOTE = 1000

#: `T-260`.  The structural model -- the half of *"single-layer square-lattice"* that `C-0141` did
#: NOT supply and `C-0154`/`C-0167` did.
_STRUCTURAL_MODEL = re.compile(
    r"grillage|OrigamiGrillage|CrossoverLayout|smeared|coupled cell|lattice machinery"
    r"|crossover combinatorics|equivalent sheet|which results are",
    re.I,
)
#: The token used ATTRIBUTIVELY of an object that genuinely is a single-layer square-lattice one.
#: Not an assertion about the corpus's inventory at all, so not a debt in either direction.
_ATTRIBUTIVE = re.compile(
    r"single-layer\s+square-lattice\s+(?:sheet|tile|number|design|question|d\s*=)", re.I
)

#: `T-262`.  What the token GOVERNS: a row span (`C-0146`'s restored reading) or a tile width
#: (`C-0140`'s withdrawn one).  Nearest wins, because the restoring sentences name both.
_ROW_WORDS = re.compile(r"\bspans?\b|\bspanned\b|\brows?\b|x-raster|per row|interface window", re.I)
_WIDTH_WORDS = re.compile(
    r"\bwidths?\b|\bextent\b|footprint|edgeX|\bnominal\b|[×x]\s*4\b|\bacross\b|bounding box", re.I
)
#: `C-0151`'s drawable RASTER against `C-0119`'s *"drawable at a uniform width"*.  `drawable` is not
#: a length, so the governing-noun rule cannot apply to it and it takes its own two-way test.
_DRAWABLE_RASTER = re.compile(
    r"102 . 109|drawable(?:\s+\S+){0,3}\s+raster|drawable one|drawable pair|closing raster"
    r"|closes", re.I
)


def plain(text: str) -> str:
    """Markdown emphasis removed, length NOT preserved.  Used only inside a refinement window."""
    return _EMPHASIS.sub("", text)


def _window(text: str, start: int, end: int, radius: int = None) -> str:
    radius = REFINE_WINDOW if radius is None else radius
    return plain(text[max(0, start - radius): end + radius])


def _nearest(pattern, text: str, at: int) -> int:
    return min((abs(m.start() - at) for m in pattern.finditer(text)), default=10 ** 9)


def refine_placement(text: str, start: int, end: int) -> str:
    """`PLACEMENT` | `GRILLAGE` | `SQUARE` -- which of two discharges, or neither."""
    window = _window(text, start, end)
    if _STRUCTURAL_MODEL.search(_window(text, start, end, STRUCTURAL_WINDOW)):
        return "GRILLAGE"
    if _ATTRIBUTIVE.search(window):
        return "SQUARE"
    return "PLACEMENT"


def refine_width(text: str, start: int, end: int) -> str:
    """`WIDTH` | `ROW_SPAN` -- the withdrawn uniform tile width, or the restored row span."""
    window = _window(text, start, end)
    if text[start:end] == "drawable":
        return "ROW_SPAN" if _DRAWABLE_RASTER.search(window) else "WIDTH"
    at = len(plain(text[max(0, start - REFINE_WINDOW): start]))
    row = _nearest(_ROW_WORDS, window, at)
    width = _nearest(_WIDTH_WORDS, window, at)
    return "ROW_SPAN" if row < width else "WIDTH"


#: name, pattern, line context, refinement, discharge.  The refinement may rename the family, and
#: the RENAMED family carries its own discharge -- which is how a partial discharge is represented.
FAMILIES = (
    (
        "FOOTPRINT",
        r"38\.08 [x×] 38\.04|38\.08 [x×] 25\.36|1 448\.5632|965\.7088|0\.666666667"
        r"|third of the footprint|38 [x×] 25 nm|0\.0577199433|0\.00874363524"
        r"|0\.0788618807|3\.29690337",
        None,
        None,
    ),
    ("WIDTH", r"\bdrawable\b|\b119 bp\b|\b40\.46\b|\b112 bp\b", _HONEYCOMB, refine_width),
    (
        "AZIMUTH",
        r"perpendicular root|perpendicular azimuth|oblique versus perpendicular"
        r"|±60°|k_z\(60°\)|\b132\b|perpendicular one",
        _AZIMUTH_CTX,
        None,
    ),
    ("SCAFFOLD", r"p8064", None, None),
    (
        "PLACEMENT",
        r"single-layer square-lattice|single-layer\n\s*square-lattice"
        r"|no station lattice, no plan ceiling|never priced an oblique",
        None,
        refine_placement,
    ),
)

#: Which discharge each family belongs to.  A family absent from this map belongs to `SUBJECT`.
FAMILY_DISCHARGE = {
    "GRILLAGE": "C-0154/C-0167",
    "SQUARE": None,
    "ROW_SPAN": None,
}


def discharge_of(family: str):
    """The discharge a family belongs to, or `None` where the family is not a debt at all."""
    return FAMILY_DISCHARGE.get(family, SUBJECT)


def gated_families() -> set:
    """Every family this census GATES -- exactly those belonging to its own subject discharge."""
    families = {f[0] for f in FAMILIES} | set(FAMILY_DISCHARGE)
    return {f for f in families if discharge_of(f) == SUBJECT}


#: How much of an occurrence's own NEIGHBOURHOOD identifies it.  A `TASKS.md` row is a paragraph on
#: one line, so a line prefix identifies the row and not the occurrence; a window centred on the
#: token identifies the occurrence and survives an edit elsewhere in the same row.
SNIPPET_CHARS = 40


def snippet(text: str, start: int, token: str) -> str:
    """The `SNIPPET_CHARS` either side of a token, whitespace collapsed.  An occurrence's identity."""
    window = text[max(0, start - SNIPPET_CHARS): start + len(token) + SNIPPET_CHARS]
    return " ".join(window.split())


def context_distance(text: str, start: int, context: str = None) -> int:
    """Characters from the token at `start` to the nearest word of its family's OWN line context.

    `CONTEXT_REMOTE` and above, the line context has said nothing about this token: it admitted the
    match on the strength of a word a kilobyte away, which on a `TASKS.md` row is a different
    subject entirely.
    """
    context = context or _HONEYCOMB
    line_start = text.rfind("\n", 0, start) + 1
    line_end = text.find("\n", start)
    line = text[line_start: line_end if line_end != -1 else len(text)]
    return _nearest(re.compile(context, re.I), line, start - line_start)


def context_distance_of(text: str, token: str) -> int:
    """`context_distance` at the first occurrence of `token`.  For the self-tests."""
    return context_distance(text, text.index(token))


def in_scope(path: str) -> bool:
    return path.startswith("gpd/claims/") or path in ("TASKS.md",) + DELIVERABLES


def blank_identifiers(text: str) -> str:
    """Replace every task/claim/challenge identifier by spaces of the SAME length."""
    out = text
    for pattern in _ID_PATTERNS:
        out = pattern.sub(lambda m: " " * (m.end() - m.start()), out)
    return out


def struck_spans(text: str):
    return [(m.start(), m.end()) for m in STRIKE.finditer(text)]


def is_struck(spans, offset: int) -> bool:
    return any(lo <= offset < hi for lo, hi in spans)


def has_pointer(text: str, offset: int, window: int = POINTER_WINDOW) -> bool:
    """Is a pointer to a correcting claim within `window` characters AFTER the occurrence?

    Forward only: an annotation follows the sentence it annotates, and a pointer belonging
    to a PREVIOUS occurrence must not discharge the next one.
    """
    ahead = text[offset : offset + window]
    return any(p in ahead for p in POINTERS)


def headline_pointer(text: str, path: str = "gpd/claims/x.md", window: int = HEADLINE_WINDOW) -> bool:
    """Does the file's own headline block carry a pointer to a correcting claim?

    **Claims only.**  A claim is one argument under one headline, so a banner there is passed by
    every reader of every number in it.  `TASKS.md` is a register of hundreds of independent rows
    and a deliverable is a document of independent sections: a pointer at the top of either
    discharges nothing, and letting it would make the gate vacuous on the two largest files.
    """
    if not path.startswith("gpd/claims/"):
        return False
    return any(p in text[:window] for p in POINTERS)


def occurrences(text: str):
    """(family, line, offset, token) for every match in one file, in file order.

    Identifiers are blanked before matching and the ORIGINAL text supplies the line, so an
    offset reported here indexes the file as it is on disk.
    """
    hunted = blank_identifiers(text)
    lines = text.split("\n")
    found = []
    for family, pattern, context, refine in FAMILIES:
        for match in re.finditer(pattern, hunted):
            line_index = text.count("\n", 0, match.start())
            if context and not re.search(context, lines[line_index], re.I):
                continue
            name = refine(text, match.start(), match.end()) if refine else family
            distance = context_distance(text, match.start(), context) if context else None
            found.append((match.start(), name, line_index + 1, match.group(0), distance))
    found.sort()
    return [(f, l, o, t, d) for o, f, l, t, d in found]


def corpus_files(root: str):
    """Every markdown file in the corpus, tracked **or not yet committed**.

    `CLAUDE.md` records that `tools/check-corpus-links.py` lists its corpus with `git ls-files` and
    therefore **skips uncommitted files in the checkout** — so the run an agent makes on its own
    work is the blind one, and this tool inherited the defect: on its first run it could not see the
    claim that was about to raise it.  `--others --exclude-standard` adds exactly the untracked,
    non-ignored files, which is what an in-progress claim is.  The tree walk remains as the fallback
    for a snapshot with no `.git`.
    """
    try:
        tracked = subprocess.run(
            ["git", "ls-files", "*.md"], cwd=root, capture_output=True, text=True, check=True
        ).stdout.split()
        untracked = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard", "*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        if tracked:
            return sorted(set(tracked) | set(untracked))
    except (OSError, subprocess.CalledProcessError):
        pass
    found = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [
            d for d in dirnames if d not in (".git", "build") and not d.startswith("build-")
        ]
        for name in filenames:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(dirpath, name), root))
    return sorted(found)


def census(root: str):
    records = []
    for path in corpus_files(root):
        if not in_scope(path):
            continue
        try:
            with open(os.path.join(root, path), encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        lines = text.split("\n")
        spans = struck_spans(text)
        banner = headline_pointer(text, path)
        for index, (family, line, offset, token, distance) in enumerate(occurrences(text)):
            records.append(
                {
                    "file": path,
                    "index": index,
                    "family": family,
                    "discharge": discharge_of(family),
                    "line": line,
                    "token": token,
                    "contextDistance": distance,
                    "pointer": has_pointer(text, offset),
                    "headlinePointer": banner,
                    "struck": is_struck(spans, offset),
                    "deliverable": path in DELIVERABLES,
                    "text": lines[line - 1].strip()[:300],
                    "snippet": snippet(text, offset, token),
                }
            )
    return records


def load_classification(path: str = CLASSIFICATION):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def classify(records, table):
    problems = []
    for record in records:
        entry = table.get(record["file"], {}).get(str(record["index"]))
        if entry is None:
            record["class"] = None
            record["why"] = None
            problems.append(
                "unclassified: {}#{} line {} [{}] {!r}".format(
                    record["file"], record["index"], record["line"], record["family"],
                    record["token"],
                )
            )
            continue
        if entry["class"] not in CLASSES:
            problems.append(
                "unknown class {!r} at {}#{}".format(entry["class"], record["file"], record["index"])
            )
        record["class"] = entry["class"]
        record["why"] = entry.get("why")
        record["byHand"] = bool(entry.get("byHand"))
        owner = record.get("discharge", SUBJECT)
        if owner == SUBJECT and entry["class"] not in ADDRESSED + NON_SUBJECT_CLASSES[2:]:
            problems.append(
                "wrong discharge: {}#{} is {} on family {}, which this census gates".format(
                    record["file"], record["index"], entry["class"], record["family"]
                )
            )
        if owner != SUBJECT and entry["class"] in ADDRESSED:
            problems.append(
                "wrong discharge: {}#{} is {} on family {}, which belongs to {}".format(
                    record["file"], record["index"], entry["class"], record["family"],
                    owner or "no discharge at all",
                )
            )
    seen = {(r["file"], r["index"]) for r in records}
    for path, entries in table.items():
        for index in entries:
            if (path, int(index)) not in seen:
                problems.append("stale classification: {}#{} has no occurrence".format(path, index))
    return records, problems


#: What the advisory line's ratio is a fraction OF, in words, printed beside the number.
#: `CLAUDE.md`: *name the set inside the field* -- a residue published without its own denominator
#: is priced against whatever table it sits next to.
#:
#: `CH-0230` candidate 2 names the OTHER one -- *"unpointed occurrences over all occurrences of the
#: same families"* -- and over the last 40 revisions of the two deliverables that reading rises at
#: every pass where the count rose, because the numerator and the denominator gain the SAME
#: occurrences and a ratio below one that gains equally top and bottom goes UP.  What makes the
#: wider denominator informative is `C-0176`'s own split: a correcting sentence written properly
#: lands in `ROW_SPAN` or `GRILLAGE`, which is denominator and not numerator.  Both readings are
#: printed, because the challenge named one and the measurement chose the other (`T-280`).
DEBT_DENOMINATOR = (
    "every occurrence this census finds in the two deliverables, of every family -- the families "
    "belonging to another discharge, and to none, included, because that is where a CORRECTING "
    "restatement lands"
)
DEBT_DENOMINATOR_NAMED_BY_CH0230 = (
    "every occurrence of the same families -- the ones this census gates, and only those"
)

#: The ratio is an exact quotient of two integers, so it carries no solver noise; it is rendered at
#: the corpus's nine significant digits, and an ABSENT ratio is `null` rather than a sentinel.
DEBT_RATIO_DIGITS = 9


def ratio_text(value) -> str:
    """A ratio at `DEBT_RATIO_DIGITS`, or the word `null` where there is no ratio to render."""
    if value is None:
        return "null"
    return "{:.{}g}".format(value, DEBT_RATIO_DIGITS)


def debt_ratio(records):
    """The advisory line's count, its two candidate denominators, and the two ratios.

    Numerator: the debt line's own predicate -- an occurrence in one of the two deliverables, on a
    family THIS census gates, classified `MOVED` or `DISCHARGED`, and neither struck nor pointed.

    Denominators: `allFamilyOccurrences` is `DEBT_DENOMINATOR`; `sameFamilyOccurrences` is
    `DEBT_DENOMINATOR_NAMED_BY_CH0230`.  Both count struck and pointed occurrences, which is the
    whole point: a repair moves an occurrence out of the numerator and leaves it in the denominator.
    """
    deliverables = [r for r in records if r.get("deliverable")]
    same = [r for r in deliverables if r.get("discharge") == SUBJECT]
    unpointed = [
        r for r in same
        if r.get("class") in ADDRESSED
        and not r.get("pointer") and not r.get("struck") and not r.get("headlinePointer")
    ]
    return {
        "unpointed": len(unpointed),
        "allFamilyOccurrences": len(deliverables),
        "sameFamilyOccurrences": len(same),
        "ratioOverAllFamilies": len(unpointed) / len(deliverables) if deliverables else None,
        "ratioOverTheSameFamilies": len(unpointed) / len(same) if same else None,
        "denominatorName": DEBT_DENOMINATOR,
        "denominatorNamedByCh0230": DEBT_DENOMINATOR_NAMED_BY_CH0230,
    }


def debt_report(debt):
    """The advisory line, as a list of printable lines: the count, the ratio and its denominator."""
    if not debt["allFamilyOccurrences"]:
        return [
            "T-233 debt no occurrence of any family in the two deliverables, so there is no ratio"
            " to quote"
        ]
    return [
        "T-233 debt {} of {} occurrence(s) = {} in the two deliverables, which this task does NOT"
        " edit".format(
            debt["unpointed"], debt["allFamilyOccurrences"],
            ratio_text(debt["ratioOverAllFamilies"]),
        ),
        "  denominator: " + DEBT_DENOMINATOR,
        "  CH-0230's own reading -- {} -- is {} of {} = {}, and that is the reading which does NOT"
        " fall when the documents are corrected".format(
            DEBT_DENOMINATOR_NAMED_BY_CH0230,
            debt["unpointed"], debt["sameFamilyOccurrences"],
            ratio_text(debt["ratioOverTheSameFamilies"]),
        ),
        "  -- and the COUNT alone is NOT a measure of debt. It is a count over a MOVING corpus, and"
        " it GROWS when the deliverables are corrected: over the last 40 revisions of the two"
        " documents every single increase is a synthesis pass, because a correcting sentence has to"
        " NAME the withdrawn premise in order to withdraw it. The T-260/T-262 split cuts the rate"
        " by about three fifths and does not change that sign (CH-0230,"
        " gpd/results/T-262-width-restatement-predicate.json). The RATIO does fall, at 3 of the 4"
        " passes at which the count rose and a ratio was defined, and the fourth is a pass that"
        " added two unpointed assertions and no repair (C-0179,"
        " gpd/results/T-280-debt-line-as-a-ratio.json).",
    ]


def check(root: str) -> int:
    records = census(root)
    records, problems = classify(records, load_classification())
    unpointed = [
        r
        for r in records
        if r["class"] in ADDRESSED
        and r.get("discharge") == SUBJECT
        and not r["pointer"]
        and not r["struck"]
        and not r["headlinePointer"]
    ]
    gate = [r for r in unpointed if not r["deliverable"]]
    debt = [r for r in unpointed if r["deliverable"]]
    counts = {c: sum(1 for r in records if r["class"] == c) for c in CLASSES}
    print(
        "{} occurrence(s) in {} file(s)".format(
            len(records), len({r["file"] for r in records})
        )
    )
    print("  " + "  ".join("{} {}".format(c, counts[c]) for c in CLASSES))
    for family in sorted({f[0] for f in FAMILIES} | set(FAMILY_DISCHARGE)):
        owner = discharge_of(family)
        print(
            "  {:<12} {:<4} {}".format(
                family,
                sum(1 for r in records if r["family"] == family),
                "gated by this census ({})".format(SUBJECT)
                if owner == SUBJECT
                else "belongs to {} -- a different census".format(owner)
                if owner
                else "not a debt: the restored reading, or a token collision",
            )
        )
    remote = [
        r for r in records
        if r["contextDistance"] is not None and r["contextDistance"] > CONTEXT_REMOTE
    ]
    for record in remote:
        print(
            "REMOTE-CONTEXT  {}:{} #{} [{} {}]  nearest honeycomb word {} characters away on the"
            " same line{}".format(
                record["file"], record["line"], record["index"], record["class"],
                record["family"], record["contextDistance"],
                " -- settled by hand" if record.get("byHand") else "",
            )
        )
    print(
        "remote context {} occurrence(s) whose LINE context says nothing about them; the tool"
        " refuses to guess and each is settled by hand in the classification".format(len(remote))
    )
    for problem in problems:
        print("PROBLEM  " + problem)
    for record in gate:
        print(
            "UNPOINTED  {}:{} #{} [{} {}]".format(
                record["file"], record["line"], record["index"], record["class"],
                record["family"],
            )
        )
    for record in debt:
        print(
            "T-233-DEBT  {}:{} #{} [{} {}]  {}".format(
                record["file"], record["line"], record["index"], record["class"],
                record["family"], record["token"],
            )
        )
    for line in debt_report(debt_ratio(records)):
        print(line)
    print("GATE {} defect(s)".format(len(problems) + len(gate)))
    return 1 if problems or gate else 0


def main(argv) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=ROOT)
    parser.add_argument("--census", action="store_true", help="print the raw census as JSON")
    parser.add_argument("--check", action="store_true", help="the gate")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args(argv)
    if args.self_test:
        return self_test()
    if args.census:
        print(json.dumps(census(args.root), indent=2, ensure_ascii=False))
        return 0
    return check(args.root)


# --------------------------------------------------------------------------- self-tests

def self_test() -> int:
    failures = []

    def ok(name, condition):
        if not condition:
            failures.append(name)

    # --- blank_identifiers is length preserving, which every offset below depends on
    text = "T-132 and CH-0151 and C-0140 and 132 stations"
    ok("blank keeps length", len(blank_identifiers(text)) == len(text))
    ok("blank removes T-132", "T-132" not in blank_identifiers(text))
    ok("blank keeps the bare 132", "132 stations" in blank_identifiers(text))
    ok("blank removes CH-0151", "CH-0151" not in blank_identifiers(text))
    ok("blank keeps P-22 out", "P-22" not in blank_identifiers("P-22"))

    # --- the AZIMUTH family must not fire on a task identifier
    ok(
        "T-132 is not a 132-station census",
        occurrences("| T-132 | a station census of something |") == [],
    )
    ok(
        "a bare 132 with station context does fire",
        [r[0] for r in occurrences("the top face supplies 132 stations on a honeycomb")]
        == ["AZIMUTH"],
    )

    # --- WIDTH needs honeycomb context: the square lattice's own 112 bp must not fire
    ok(
        "112 bp on the square lattice does not fire",
        occurrences("the buildable seamless square-lattice row is 112 bp = 38.08 nm") == [],
    )
    ok(
        "112 bp on the honeycomb does fire",
        [r[0] for r in occurrences("the honeycomb tile is 15 rows x 4 layers x 112 bp")]
        == ["WIDTH"],
    )

    # --- FOOTPRINT needs no context: its tokens are unique to the four-layer line
    ok(
        "the reversed footprint fires",
        [r[0] for r in occurrences("38.08 × 25.36 nm")] == ["FOOTPRINT"],
    )
    ok(
        "the threshold fires",
        [r[0] for r in occurrences("f = 0.0788618807")] == ["FOOTPRINT"],
    )
    ok("SCAFFOLD fires on p8064", [r[0] for r in occurrences("folded from p8064")] == ["SCAFFOLD"])
    ok(
        "PLACEMENT fires on the absence statement",
        [r[0] for r in occurrences("every placement here is single-layer square-lattice")]
        == ["PLACEMENT"],
    )

    # --- occurrences are returned in FILE ORDER, because the classification is keyed on index
    multi = "0.0788618807 then p8064 then 38.08 × 25.36"
    ok("file order", [r[0] for r in occurrences(multi)] == ["FOOTPRINT", "SCAFFOLD", "FOOTPRINT"])

    # --- lines are 1-based and correct
    ok("line numbers", occurrences("x\ny\np8064")[0][1] == 3)

    # --- the pointer window is FORWARD only
    body = "0.0788618807 is the threshold. Annotated by C-0141."
    ok("forward pointer found", has_pointer(body, 0))
    ok(
        "a pointer BEHIND does not discharge",
        not has_pointer("C-0141 said so. " + " " * 950 + "0.0788618807", 966),
    )
    ok("no pointer is no pointer", not has_pointer("0.0788618807 stands alone", 0))
    ok("window is respected", not has_pointer("0.0788618807" + "x" * 2000 + "C-0141", 0))

    # --- the headline pointer discharges a file, and only from its own headline
    ok("headline pointer found", headline_pointer("# C-0116 - x\n> Annotated: C-0141\n"))
    ok("no headline pointer", not headline_pointer("# C-0116 - x\n> nothing here\n"))
    ok(
        "a pointer past the headline window is not a headline pointer",
        not headline_pointer("# C-0116\n" + "x" * 4000 + "C-0141"),
    )
    ok(
        "TASKS.md gets NO headline discharge",
        not headline_pointer("# TASKS\n> C-0141\n", "TASKS.md"),
    )
    ok(
        "a deliverable gets NO headline discharge",
        not headline_pointer("# ANSWERS\n> C-0141\n", "ANSWERS.md"),
    )
    ok("HEADLINE_WINDOW is short", HEADLINE_WINDOW <= 4000)

    # --- strike detection
    struck = "~~0.0788618807~~ and 0.0788618807"
    spans = struck_spans(struck)
    ok("first is struck", is_struck(spans, 2))
    ok("second is not struck", not is_struck(spans, struck.rindex("0.0788618807")))

    # --- the corpus includes uncommitted files: this tool's own claim was invisible at first
    listing = corpus_files(ROOT)
    ok("corpus is non-empty", len(listing) > 100)
    ok("corpus contains a claim", any(f.startswith("gpd/claims/") for f in listing))
    ok(
        "corpus contains THIS task's own claim, tracked or not",
        "gpd/claims/C-0144-honeycomb-correction-supersession.md" in listing,
    )
    ok("corpus is sorted and unique", listing == sorted(set(listing)))

    # --- scope
    ok("a claim is in scope", in_scope("gpd/claims/C-0116-composite-fraction-threshold.md"))
    ok("TASKS.md is in scope", in_scope("TASKS.md"))
    ok("ANSWERS.md is in scope", in_scope("ANSWERS.md"))
    ok("a task file is NOT in scope", not in_scope("gpd/tasks/T-219-x.md"))
    ok("a challenge is NOT in scope", not in_scope("gpd/challenges/CH-0174-x.md"))
    ok("the journal is NOT in scope", not in_scope("JOURNAL.md"))

    # --- classify reports both an unclassified occurrence and a stale entry
    records = [{"file": "a.md", "index": 0, "line": 1, "family": "SCAFFOLD", "token": "p8064"}]
    _, problems = classify(list(records), {})
    ok("unclassified is reported", any(p.startswith("unclassified") for p in problems))
    _, problems = classify(
        list(records), {"a.md": {"0": {"class": "MOVED", "why": "x"}, "1": {"class": "MOVED"}}}
    )
    ok("stale is reported", any(p.startswith("stale") for p in problems))
    _, problems = classify(list(records), {"a.md": {"0": {"class": "NONSENSE"}}})
    ok("unknown class is reported", any(p.startswith("unknown class") for p in problems))

    # --- ADDRESSED is exactly the two repair classes
    ok("ADDRESSED", set(ADDRESSED) == {"MOVED", "DISCHARGED"})
    ok("every ADDRESSED is a CLASS", all(a in CLASSES for a in ADDRESSED))

    # ----------------------------------------------------------------- T-260: a PARTIAL discharge
    # `C-0141` supplied the station lattice, the plan ceiling and the placement family, and did NOT
    # supply a grillage.  One token, two discharges, two dates.  Each rule is asserted in BOTH
    # directions: the sentence it must fire on, and the sentence it must NOT.

    def sub(text):
        return [r[0] for r in occurrences(text)]

    ok(
        "T-260 the absence statement is PLACEMENT",
        sub("every plan ceiling, phase result and placement in this corpus is"
            " single-layer square-lattice") == ["PLACEMENT"],
    )
    ok(
        "T-260 a coupled cell is GRILLAGE, not PLACEMENT",
        sub("every coupled cell in this corpus is a smeared single-layer square-lattice solve")
        == ["GRILLAGE"],
    )
    ok(
        "T-260 the lattice machinery is GRILLAGE",
        sub("the lattice machinery is single-layer square-lattice, so the question needs a"
            " honeycomb grillage") == ["GRILLAGE"],
    )
    ok(
        "T-260 OrigamiGrillage in the window makes it GRILLAGE",
        sub("a uniform prestrain on all 56 crossovers of a single-layer square-lattice tile at one"
            " placement, and OrigamiGrillage never reads layers") == ["GRILLAGE"],
    )
    ok(
        "T-260 an attributive SHEET is SQUARE, not an absence claim",
        sub("40.0 x 40.35 nm single-layer square-lattice sheet, 15 duplexes") == ["SQUARE"],
    )
    ok(
        "T-260 an attributive NUMBER is SQUARE even under emphasis",
        sub("every register number is therefore a **single-layer square-lattice**\nnumber")
        == ["SQUARE"],
    )
    ok(
        "T-260 an attributive QUESTION is SQUARE",
        sub("a material constant, a single-layer square-lattice question, the harness")
        == ["SQUARE"],
    )
    ok(
        "T-260 `no station lattice, no plan ceiling` stays PLACEMENT",
        sub("the honeycomb has no station lattice, no plan ceiling and no placement family")
        == ["PLACEMENT"],
    )
    # the same sentence carries an AZIMUTH token too, so this asserts the PLACEMENT member of it
    ok(
        "T-260 `never priced an oblique` stays PLACEMENT",
        sub("this corpus has never priced an oblique attachment against a perpendicular one")
        == ["PLACEMENT", "AZIMUTH"],
    )
    ok(
        "T-260 GRILLAGE belongs to a DIFFERENT discharge from PLACEMENT",
        discharge_of("GRILLAGE") != discharge_of("PLACEMENT"),
    )
    ok("T-260 PLACEMENT is this census's own subject", discharge_of("PLACEMENT") == SUBJECT)
    ok("T-260 GRILLAGE is not this census's subject", discharge_of("GRILLAGE") != SUBJECT)
    ok("T-260 SQUARE is no discharge at all", discharge_of("SQUARE") is None)
    far = ("this grillage sentence is far away. " + "y " * (REFINE_WINDOW // 2 + 200)
           + "every plan ceiling and placement in this corpus is single-layer square-lattice")
    ok(
        "T-260 a structural-model word BEYOND the refinement window is not seen",
        [r[0] for r in occurrences(far)] == ["PLACEMENT"],
    )
    # ANSWERS.md line 1147 verbatim, whose nearest structural-model word is a *different sentence*
    # 253 characters away: this is the false positive STRUCTURAL_WINDOW exists to remove.
    sentence_away = (
        "every plan ceiling, station lattice, crossover phase and placement in this corpus is"
        " single-layer square-lattice; the honeycomb has three crossover azimuths at 7 bp rather"
        " than the square lattice's four at 8 bp, and nobody has counted what that offers. So every"
        " path count in the flat coupled cells is a requirement on a lattice nobody has censused."
    )
    ok(
        "T-260 a structural-model word a SENTENCE away does not make it GRILLAGE",
        sub(sentence_away) == ["PLACEMENT"],
    )
    ok(
        "T-260 a structural-model word in the SAME clause still does",
        sub("every coupled cell here is single-layer square-lattice") == ["GRILLAGE"],
    )
    ok("T-260 REFINE_WINDOW is a stated constant", REFINE_WINDOW == 300)
    ok("T-260 STRUCTURAL_WINDOW is tighter than REFINE_WINDOW", STRUCTURAL_WINDOW == 120)
    ok(
        "T-260 the grillage discharge names C-0154/C-0167",
        set(DISCHARGES[discharge_of("GRILLAGE")]) >= {"C-0154", "C-0167", "T-253"},
    )
    ok(
        "T-260 the subject discharge does NOT name C-0154",
        "C-0154" not in DISCHARGES[SUBJECT],
    )
    ok("T-260 only subject families are gated", gated_families() == {"FOOTPRINT", "WIDTH",
                                                                    "AZIMUTH", "SCAFFOLD",
                                                                    "PLACEMENT"})

    # ----------------------------------------------------------------- T-262: a RESTATEMENT
    # `C-0140` withdrew a honeycomb row length asserted as a UNIFORM TILE WIDTH.  `C-0146` restored
    # the same token as a ROW SPAN, and `C-0151` restored `drawable` as the drawable RASTER.

    ok(
        "T-262 a span is ROW_SPAN",
        sub("honeycomb at 10.5 bp/turn, 112 bp span, d = 2.536 nm") == ["ROW_SPAN"],
    )
    ok(
        "T-262 `every x-raster row spans` is ROW_SPAN even beside the word extent",
        sub("on the honeycomb every x-raster row spans 112 bp = 38.08 nm and the 116 bp ="
            " 39.44 nm extent is a stagger") == ["ROW_SPAN"],
    )
    ok(
        "T-262 `rows of 112 bp` is ROW_SPAN",
        sub("honeycomb rows of 112 bp and 119 bp") == ["ROW_SPAN", "ROW_SPAN"],
    )
    ok(
        "T-262 a tile dimension is WIDTH",
        sub("the honeycomb tile is 15 rows x 4 layers x 112 bp") == ["WIDTH"],
    )
    ok(
        "T-262 `is a uniform width` is WIDTH",
        sub("so neither 112 bp nor 119 bp is a uniform honeycomb width") == ["WIDTH", "WIDTH"],
    )
    ok(
        "T-262 a bare honeycomb width-table cell defaults to WIDTH",
        sub("| honeycomb | 112 bp = 38.08 nm | -4.80 % |") == ["WIDTH"],
    )
    ok(
        "T-262 the drawable RASTER is ROW_SPAN",
        sub("at the drawable 102 / 109 honeycomb raster the count is 10") == ["ROW_SPAN"],
    )
    ok(
        "T-262 `drawable` with modifiers before `raster` is still the drawable RASTER",
        sub("the minimum stagger a drawable two-length honeycomb raster can carry is 7 bp")
        == ["ROW_SPAN"],
    )
    ok(
        "T-262 `drawable at a uniform width` stays WIDTH",
        sub("the honeycomb tile is overturned in the reading drawable at a uniform width")
        == ["WIDTH"],
    )
    ok(
        "T-262 a bare `the four-layer tile is drawable` stays WIDTH",
        sub("the four-layer tile is drawable") == ["WIDTH"],
    )
    ok("T-262 WIDTH is this census's own subject", discharge_of("WIDTH") == SUBJECT)
    ok("T-262 ROW_SPAN is no discharge at all", discharge_of("ROW_SPAN") is None)
    ok(
        "T-262 the square lattice's own 112 bp still does not fire at all",
        occurrences("the buildable seamless square-lattice row is 112 bp = 38.08 nm") == [],
    )

    # --- the REMOTE-CONTEXT diagnostic: a TASKS.md row is a paragraph, so a line context is not a
    # context.  The tool reports the distance and refuses to guess; the JSON carries the hand call.
    remote = "the honeycomb block. " + "x" * 2000 + " oxDNA2, 15 duplexes at 112 bp and phase 8"
    ok("T-262 a remote honeycomb word still fires", sub(remote) == ["WIDTH"])
    ok(
        "T-262 the remote distance is measured and large",
        context_distance_of(remote, "112 bp") > CONTEXT_REMOTE,
    )
    near = "the honeycomb tile is 15 rows x 4 layers x 112 bp"
    ok(
        "T-262 a near honeycomb word is not remote",
        context_distance_of(near, "112 bp") < CONTEXT_REMOTE,
    )
    ok("T-262 CONTEXT_REMOTE is a stated constant", CONTEXT_REMOTE == 1000)

    # --- a snippet identifies an OCCURRENCE, not its line: a queue row is a paragraph
    row = "| T-9 | a very long queue row | " + "x" * 900 + " at 112 bp and phase 8 | DONE |"
    ok(
        "T-262 a snippet is centred on the token, not on the line",
        "112 bp" in snippet(row, row.index("112 bp"), "112 bp"),
    )
    ok(
        "T-262 a snippet does not reach the start of a long row",
        "T-9" not in snippet(row, row.index("112 bp"), "112 bp"),
    )
    twice = "a 112 bp here" + " y" * 60 + " and 112 bp there"
    ok(
        "T-262 two tokens far apart on one line get different snippets",
        snippet(twice, 2, "112 bp") != snippet(twice, twice.rindex("112 bp"), "112 bp"),
    )
    close = "a 112 bp and 112 bp"
    ok(
        "T-262 two tokens CLOSER than SNIPPET_CHARS share one -- the collision is reported",
        snippet(close, 2, "112 bp") == snippet(close, close.rindex("112 bp"), "112 bp"),
    )
    ok(
        "a snippet collapses whitespace, so a re-wrap does not move it",
        snippet("x  112 bp\n  y", 3, "112 bp") == snippet("x 112 bp y", 2, "112 bp"),
    )
    ok("T-262 SNIPPET_CHARS is a stated constant", SNIPPET_CHARS == 40)

    # --- the two layers must AGREE: a class that demands a pointer may not sit on a family that
    # belongs to another census.  This is what makes a partial discharge representable rather than
    # remembered -- without it, a stale table silently re-gates the half that was split off.
    grillage = [{"file": "a.md", "index": 0, "line": 1, "family": "GRILLAGE",
                 "discharge": discharge_of("GRILLAGE"), "token": "single-layer square-lattice"}]
    _, problems = classify(list(grillage), {"a.md": {"0": {"class": "DISCHARGED", "why": "x"}}})
    ok(
        "T-260 a subject class on a non-subject family is reported",
        any(p.startswith("wrong discharge") for p in problems),
    )
    _, problems = classify(list(grillage), {"a.md": {"0": {"class": "SURVIVING", "why": "x"}}})
    ok("T-260 SURVIVING on GRILLAGE is accepted", not problems)
    placement = [{"file": "a.md", "index": 0, "line": 1, "family": "PLACEMENT",
                  "discharge": discharge_of("PLACEMENT"), "token": "x"}]
    _, problems = classify(list(placement), {"a.md": {"0": {"class": "DISCHARGED", "why": "x"}}})
    ok("T-260 DISCHARGED on PLACEMENT is accepted", not problems)
    _, problems = classify(list(placement), {"a.md": {"0": {"class": "RESTATED", "why": "x"}}})
    ok(
        "T-260 a non-subject class on a subject family is reported too",
        any(p.startswith("wrong discharge") for p in problems),
    )
    ok("T-260 SURVIVING is not ADDRESSED", "SURVIVING" not in ADDRESSED)
    ok("T-262 RESTATED is not ADDRESSED", "RESTATED" not in ADDRESSED)
    ok("T-260 SURVIVING is a CLASS", "SURVIVING" in CLASSES)
    ok("T-262 RESTATED is a CLASS", "RESTATED" in CLASSES)
    ok(
        "T-260 every non-subject family has a non-subject class",
        set(NON_SUBJECT_CLASSES) == {"SURVIVING", "RESTATED", "RECORD", "CORRECT", "OUT_OF_SCOPE"},
    )

    # ------------------------------------------------------------------ T-280: the line as a RATIO
    # A count over a MOVING corpus is not a debt.  The ratio's denominator is the whole question,
    # and `CH-0230` named the one that does NOT work, so both readings are published and each rule
    # is asserted in BOTH directions: what enters the numerator, and what enters each denominator.

    def deliverable(index, family, cls, pointer=False, struck=False, headline=False,
                    path="ANSWERS.md"):
        return {
            "file": path, "index": index, "line": 1, "family": family,
            "discharge": discharge_of(family), "class": cls, "pointer": pointer,
            "struck": struck, "headlinePointer": headline, "deliverable": path in DELIVERABLES,
            "token": "x",
        }

    debt = debt_ratio([deliverable(0, "WIDTH", "MOVED")])
    ok("T-280 an unpointed MOVED occurrence is the numerator", debt["unpointed"] == 1)
    ok("T-280 it is in both denominators",
       debt["allFamilyOccurrences"] == 1 and debt["sameFamilyOccurrences"] == 1)
    ok("T-280 the ratio is the quotient of the two counts", debt["ratioOverAllFamilies"] == 1.0)

    pointed = debt_ratio([deliverable(0, "WIDTH", "MOVED"),
                          deliverable(1, "WIDTH", "MOVED", pointer=True)])
    ok("T-280 a POINTED occurrence leaves the numerator", pointed["unpointed"] == 1)
    ok("T-280 a POINTED occurrence stays in both denominators",
       pointed["allFamilyOccurrences"] == 2 and pointed["sameFamilyOccurrences"] == 2)
    ok("T-280 adding a pointed occurrence LOWERS the ratio -- the behaviour `debt` implies",
       pointed["ratioOverAllFamilies"] < debt["ratioOverAllFamilies"])

    struck = debt_ratio([deliverable(0, "WIDTH", "MOVED"),
                         deliverable(1, "WIDTH", "MOVED", struck=True)])
    ok("T-280 a STRUCK occurrence leaves the numerator and stays in the denominator",
       struck["unpointed"] == 1 and struck["allFamilyOccurrences"] == 2)
    headlined = debt_ratio([deliverable(0, "WIDTH", "MOVED", headline=True)])
    ok("T-280 a HEADLINE pointer leaves the numerator", headlined["unpointed"] == 0)

    record = debt_ratio([deliverable(0, "WIDTH", "MOVED"), deliverable(1, "WIDTH", "RECORD")])
    ok("T-280 a class this census does not gate is not in the numerator", record["unpointed"] == 1)
    ok("T-280 but it IS in the denominator: it is the corpus discussing the premise",
       record["allFamilyOccurrences"] == 2)

    other = debt_ratio([deliverable(0, "WIDTH", "MOVED"), deliverable(1, "ROW_SPAN", "RESTATED")])
    ok("T-280 a RESTORED reading is in the all-family denominator", other["allFamilyOccurrences"] == 2)
    ok("T-280 and NOT in the same-family one, which is CH-0230's own reading",
       other["sameFamilyOccurrences"] == 1)
    ok("T-280 so the two readings can differ, and both are published",
       other["ratioOverAllFamilies"] < other["ratioOverTheSameFamilies"])
    grillage = debt_ratio([deliverable(0, "WIDTH", "MOVED"),
                           deliverable(1, "GRILLAGE", "SURVIVING")])
    ok("T-280 another census's discharge is in the all-family denominator only",
       grillage["allFamilyOccurrences"] == 2 and grillage["sameFamilyOccurrences"] == 1)

    outside = debt_ratio([deliverable(0, "WIDTH", "MOVED", path="gpd/claims/C-0001-x.md")])
    ok("T-280 an occurrence outside the two deliverables enters neither",
       outside["unpointed"] == 0 and outside["allFamilyOccurrences"] == 0)
    ok("T-280 an empty denominator gives a null ratio, not a division",
       debt_ratio([])["ratioOverAllFamilies"] is None)
    ok("T-280 the numerator never exceeds either denominator",
       all(debt_ratio(rs)["unpointed"] <= debt_ratio(rs)["sameFamilyOccurrences"]
           for rs in ([deliverable(0, "WIDTH", "MOVED")], [deliverable(0, "AZIMUTH", "DISCHARGED")])))
    ok(
        "T-280 the debt line counts only the two deliverables, so a claim's own worked examples "
        "cannot enter it -- C-0176's two readings COINCIDE here",
        not any(d.startswith("gpd/claims/") for d in DELIVERABLES),
    )

    # --- the denominator must be NAMED in the tool's own output
    lines = debt_report(debt_ratio([deliverable(0, "WIDTH", "MOVED"),
                                    deliverable(1, "ROW_SPAN", "RESTATED")]))
    body = " ".join(lines)
    ok(
        "T-280 the report's HEADLINE carries the count as well as the ratio -- a weaker test here "
        "was satisfied by the word `occurrence` inside the denominator's own name, and a mutation "
        "that dropped the count from the line failed nothing",
        lines[0].startswith("T-233 debt 1 of 2 occurrence(s) = 0.5 "),
    )
    ok("T-280 the report NAMES the denominator", DEBT_DENOMINATOR in body)
    ok("T-280 the report names CH-0230's own denominator beside it",
       DEBT_DENOMINATOR_NAMED_BY_CH0230 in body)
    ok("T-280 the two denominators are named differently",
       DEBT_DENOMINATOR != DEBT_DENOMINATOR_NAMED_BY_CH0230)
    ok("T-280 the denominator name says EVERY family", "every family" in DEBT_DENOMINATOR)
    ok("T-280 CH-0230's name says the SAME families", "same families" in
       DEBT_DENOMINATOR_NAMED_BY_CH0230)
    ok("T-280 the report says the line is not a debt measure",
       "not a measure of debt" in body.lower() or "NOT a measure of debt" in body)
    ok("T-280 an empty census reports no ratio rather than a zero one",
       "no occurrence" in " ".join(debt_report(debt_ratio([]))).lower())
    ok("T-280 the ratio is rendered at nine significant digits",
       ratio_text(24.0 / 88.0) == "0.272727273")
    ok("T-280 a null ratio renders as `null`, not as a number", ratio_text(None) == "null")

    for failure in failures:
        print("FAIL  " + failure)
    print("self-test: {} failure(s)".format(len(failures)))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
