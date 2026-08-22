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
# T-234 -- regenerate tools/T-234-classification.json from the STATED rules below.
#
# The classification is a READING.  It is retained as data so a reader can disagree one
# occurrence at a time -- and it is regenerable from rules rather than typed, so that an
# occurrence added or moved by a later edit is classified by the same rule the rest were,
# instead of by whoever happened to renumber the file.  A rule that is wrong for one
# occurrence is overridden by hand in the JSON and the override survives, because this
# emitter is run deliberately and not as a gate.
import importlib.util
import json
import os
import re
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t234", "T-234-census.py")
tracer = _load("trace_answers", "trace-answers.py")

WHY = {
    "MOVED": "asserts a premise C-0140/C-0141 withdrew -- struck, pointed, or listed for T-233",
    "SURVIVING": "the half of a PARTIALLY discharged premise C-0141 did NOT supply -- the smeared "
                 "single-layer square-lattice structural model, which OrigamiGrillage's inability "
                 "to read `layers` kept true until C-0154/C-0167 (T-253). Live when written, and "
                 "the subject of a different census",
    "RESTATED": "the token in its RESTORED reading -- C-0146's row span, or C-0151's drawable "
                "raster -- which is the correcting statement itself and not the debt",
    "DISCHARGED": "asserts the ABSENCE of a honeycomb station lattice, plan ceiling or placement "
                  "family, which C-0141 supplies",
    "RECORD": "a historical record -- a closed queue row, a synthesis pass's account, a Conditions "
              "row, or a verbatim source quotation. True when written; the correction belongs in "
              "the claim it points to",
    "CORRECT": "inside a correcting claim or an iteration-34 annotation, or stating the corrected "
               "value",
    "OUT_OF_SCOPE": "the token matched and the statement is about something else",
}

#: The two claims that made the corrections.  Every occurrence in them is CORRECT by construction.
CORRECTING = {
    "gpd/claims/C-0140-honeycomb-raster-turn-sense.md",
    "gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md",
    # This task's own claim: it quotes the withdrawn premises in order to withdraw them.
    "gpd/claims/C-0144-honeycomb-correction-supersession.md",
    # Iteration 34, concurrent: C-0142 re-grades C-0118's coupled cells AT the corrected
    # cross-section (T-232), so its occurrences of the superseded numbers are the baseline it is
    # replacing.  It appeared in the census mid-run, which is what `--others` is for.
    "gpd/claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md",
    # Iteration 35, concurrent again -- and this is CH-0182 happening for the second time inside
    # two iterations, which is why the entry says so rather than just listing the files.  C-0146
    # re-grades C-0142's cells at C-0140's two-length raster (T-235) and C-0147 prices that
    # raster's turn loop and ragged face (T-230/T-231); both quote the superseded widths and
    # footprints as the baseline they are replacing.  A census is DATED BY ITS PREMISE SET, so
    # this set goes stale within the iteration that writes it, every time.
    "gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md",
    "gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md",
    # Iteration 36, and CH-0182 for the fourth consecutive time.  C-0148 settles the honeycomb
    # face's bond-class residues and derives the crossover-column count from the ROW spans
    # (T-244/T-243); its `112 bp` is C-0146's restored ROW SPAN, which is the corrected reading
    # and not the withdrawn uniform tile width the WIDTH family cannot tell it apart from.
    "gpd/claims/C-0148-face-bond-class-residues-and-row-span-columns.md",
    # Iteration 37, and CH-0182 for the FIFTH consecutive iteration. C-0151 selects the drawable
    # raster, C-0152 prices the forcing C-0148 found, C-0153 sweeps the prose interpolations; each
    # quotes a superseded width or a single-layer statement as the baseline it acts on.
    # T-260/T-262 (iteration 42) REMOVED `C-0152` and `C-0154` from this set. Both were registered
    # only to hide false positives their own comments name, and a set membership is not a repair:
    # the census now splits `PLACEMENT` into the half `C-0141` discharged and the `GRILLAGE` half
    # it did not, and `WIDTH` into `C-0140`'s withdrawn uniform tile width and `C-0146`'s restored
    # `ROW_SPAN`. Their occurrences are carried by the tested predicate instead.
    "gpd/claims/C-0151-closing-raster-selection.md",
    "gpd/claims/C-0153-unrounded-prose-interpolations.md",
    # Iteration 42, and CH-0182 for the SEVENTH consecutive iteration -- this time on the claim
    # that REPORTS it. C-0176 is a claim whose SUBJECT is this census: its §2 and §3 tables quote
    # the families' own example sentences ("112 bp span", "every coupled cell ... is a smeared
    # single-layer square-lattice solve", "the honeycomb has no station lattice, no plan ceiling")
    # in order to say which reading each is, so 13 occurrences of the census's own patterns entered
    # the census while it was being written. It is registered on the SAME ground as C-0144, whose
    # own #20 is the quotation "`112 bp` needs a honeycomb context" -- a document about the tool,
    # not a design premise -- and NOT on the ground T-260 removed C-0152 and C-0154 for, which was
    # a misclassification of a live statement.
    #
    # The general rule was measured and REJECTED: exempting every occurrence inside a quoted span
    # covers 8 of the 13 and reclassifies 9 EXISTING occurrences (5 MOVED, 3 DISCHARGED, 1
    # SURVIVING) across 6 other files, which would excuse a deliverable that quotes a withdrawn
    # sentence as its own assertion. Measure the cure before writing it.
    "gpd/claims/C-0176-partial-discharge-and-restatement-predicates.md",
}

#: How much of an occurrence's own NEIGHBOURHOOD takes part in a hand override's key -- the census's
#: `snippet`, which is centred on the token.  Long enough that two occurrences of one token in one
#: file are told apart, short enough that a copy-edit elsewhere on a `TASKS.md` row -- which is a
#: paragraph on one physical line -- does not silently drop a reader's call.
OVERRIDE_KEY_CHARS = 100

#: `T-260`/`T-262`.  A family the census does not gate cannot carry a class the census gates.  The
#: coercion runs LAST, so every reading above -- a correcting claim, a synthesis record, a
#: Conditions row, a quotation -- is kept; what it replaces is only the two-way `MOVED`/`DISCHARGED`
#: default, which is the rule that could not represent a partial discharge.
FAMILY_CLASS = {
    "GRILLAGE": "SURVIVING",
    "ROW_SPAN": "RESTATED",
    "SQUARE": "OUT_OF_SCOPE",
}
#: Synthesis claims: an occurrence there records what a past deliverable pass carried in.
SYNTHESIS = {
    "gpd/claims/C-0115-fifth-answers-synthesis.md",
    "gpd/claims/C-0121-sixth-answers-synthesis.md",
    # Iteration 35: the eighth pass, which performed the T-233 restatement and therefore quotes
    # every withdrawn premise beside the value that replaced it.  Added here rather than to
    # CORRECTING because it records what past passes carried in as well as correcting it --
    # and because CH-0182 is exactly the observation that these sets go stale within one iteration.
    "gpd/claims/C-0145-eighth-answers-synthesis.md",
    # Iteration 36: the ninth pass, for the same reason as the eighth -- it quotes p8064 and the
    # withdrawn footprints in order to say what replaced them.  Added here in the same iteration
    # it was written, which is CH-0182's observation happening for the third consecutive time.
    "gpd/claims/C-0149-ninth-answers-synthesis.md",
}
#: Token collisions -- the statement is not about the four-layer line at all.
#: `T-260` emptied this set: both members were `PLACEMENT` occurrences used ATTRIBUTIVELY of an
#: object that genuinely is a single-layer square-lattice sheet -- a Conditions row and a quoted
#: Kotlin format string -- and the census's own `SQUARE` refinement now reads them, by a tested
#: predicate rather than by naming the two files that happened to have one.
OUT_OF_SCOPE_FILES = set()

PLACEMENT_ABSENCE = re.compile(
    r"single-layer\s+square-lattice|no station lattice, no plan ceiling|never priced an oblique"
)
QUEUE_ROW = re.compile(r"^\|\s*(T-\d{1,4}[a-z]?|P-\d{1,4})\s*\|")


def classify_one(record, statuses):
    path, text = record["file"], record["text"]
    if path in CORRECTING:
        return "CORRECT"
    if path in OUT_OF_SCOPE_FILES:
        return "OUT_OF_SCOPE"
    if path in SYNTHESIS:
        return "RECORD"
    if text.startswith(">") and "iteration 34" in text:
        return "CORRECT"
    if text.startswith("| **Conditions**"):
        return "RECORD"
    stripped = text.lstrip("> ")
    if stripped.startswith('*"') or stripped.startswith('"'):
        return "RECORD"
    match = QUEUE_ROW.match(text)
    if path == "TASKS.md" and match and statuses.get(match.group(1)) == "CLOSED":
        return "RECORD"
    if record["family"] == "PLACEMENT" and PLACEMENT_ABSENCE.search(record["token"]):
        return "DISCHARGED"
    return "MOVED"


def coerce(cls, family):
    """A family this census does not gate may not carry a class this census gates."""
    if cls in census.ADDRESSED and family in FAMILY_CLASS:
        return FAMILY_CLASS[family]
    return cls


def override_key(entry_or_record):
    """What a hand override is keyed on -- NOT the index.

    `tools/T-234-emit-classification.py`'s docstring has promised since iteration 34 that a hand
    override *"survives"*, and it did not: the emitter built its table from scratch and never read
    the file it overwrote (`T-262`).  It reads it now, and it keys the override on what the
    occurrence IS -- file, family, token, and the opening of its own line -- rather than on the
    index, because `TASKS.md` gains rows every iteration and an index is a dated object.  An
    override whose line has since been rewritten is DROPPED and reported, never silently moved.
    """
    return (
        entry_or_record.get("file"),
        entry_or_record.get("family"),
        entry_or_record.get("token"),
        (entry_or_record.get("snippet") or "")[:OVERRIDE_KEY_CHARS],
    )


def hand_overrides(previous):
    """Every `byHand` entry of a previous table, keyed by `override_key`, and the key collisions.

    Two occurrences of one token closer together than the census's `SNIPPET_CHARS` share a key.
    That is rare and it is not silently resolved: the colliding keys are returned so `main` can
    report them, because a reader's call landing on the wrong occurrence is the one failure this
    mechanism must not have.
    """
    kept, collisions = {}, []
    for path, entries in previous.items():
        for entry in entries.values():
            if not entry.get("byHand"):
                continue
            key = override_key(dict(entry, file=path))
            if key in kept and kept[key]["class"] != entry["class"]:
                collisions.append(key)
            kept[key] = entry
    return kept, collisions


def main():
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        statuses = tracer.queue_status(handle.read())
    records = census.census(ROOT)
    destination = os.path.join(ROOT, "tools", "T-234-classification.json")
    try:
        with open(destination, encoding="utf-8") as handle:
            previous = json.load(handle)
    except (OSError, ValueError):
        previous = {}
    overrides, collisions = hand_overrides(previous)
    table = {}
    used = set()
    for record in records:
        cls = coerce(classify_one(record, statuses), record["family"])
        entry = {
            "class": cls,
            "why": WHY[cls],
            "family": record["family"],
            "token": record["token"],
            "line": record["line"],
        }
        key = override_key(record)
        held = overrides.get(key)
        if held:
            used.add(key)
            entry["class"] = held["class"]
            entry["why"] = held.get("why", WHY[held["class"]])
            entry["byHand"] = True
            entry["snippet"] = record["snippet"][:OVERRIDE_KEY_CHARS]
        table.setdefault(record["file"], {})[str(record["index"])] = entry
    dropped = [
        "hand override {!r} on {} {}/{!r} no longer matches any occurrence".format(
            entry["class"], key[0], key[1], key[2]
        )
        for key, entry in overrides.items()
        if key not in used
    ] + [
        "AMBIGUOUS hand override on {} {}/{!r}: two occurrences share one snippet".format(
            key[0], key[1], key[2]
        )
        for key in collisions
    ]
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(table, handle, indent=1, ensure_ascii=False, sort_keys=True)
        handle.write("\n")
    counts = Counter(v["class"] for entries in table.values() for v in entries.values())
    kept = sum(1 for entries in table.values() for v in entries.values() if v.get("byHand"))
    print("wrote {}".format(destination))
    print("  ".join("{} {}".format(k, counts[k]) for k in census.CLASSES))
    print("hand overrides carried over: {}".format(kept))
    for lost in dropped:
        print("DROPPED  " + lost)
    return 0


# ------------------------------------------------------------------------------- self-tests
#
# Both directions, per `CLAUDE.md`'s standard from `C-0127`/`T-225`: restoring the old narrow rule
# must fail a NAMED test, and widening the new one by pattern must fail one too.  Measured by
# `tools/T-234-mutation-test.py`.

def self_test():
    failures = []

    def ok(name, condition):
        if not condition:
            failures.append(name)

    # --- the coercion, in both directions
    ok("coerce GRILLAGE off MOVED", coerce("MOVED", "GRILLAGE") == "SURVIVING")
    ok("coerce GRILLAGE off DISCHARGED", coerce("DISCHARGED", "GRILLAGE") == "SURVIVING")
    ok("coerce ROW_SPAN off MOVED", coerce("MOVED", "ROW_SPAN") == "RESTATED")
    ok("coerce SQUARE off DISCHARGED", coerce("DISCHARGED", "SQUARE") == "OUT_OF_SCOPE")
    ok("coerce leaves PLACEMENT alone", coerce("DISCHARGED", "PLACEMENT") == "DISCHARGED")
    ok("coerce leaves WIDTH alone", coerce("MOVED", "WIDTH") == "MOVED")
    ok("coerce leaves FOOTPRINT alone", coerce("MOVED", "FOOTPRINT") == "MOVED")
    ok("coerce does NOT touch a non-gated class", coerce("RECORD", "GRILLAGE") == "RECORD")
    ok("coerce keeps a CORRECT reading", coerce("CORRECT", "ROW_SPAN") == "CORRECT")
    ok(
        "every coerced class is outside ADDRESSED",
        all(c not in census.ADDRESSED for c in FAMILY_CLASS.values()),
    )
    ok(
        "every coerced family is one the census does not gate",
        all(census.discharge_of(f) != census.SUBJECT for f in FAMILY_CLASS),
    )
    ok(
        "every family the census does not gate has a coercion",
        {f for f in census.FAMILY_DISCHARGE} == set(FAMILY_CLASS),
    )
    ok("SURVIVING has a reason", "SURVIVING" in WHY and len(WHY["SURVIVING"]) > 40)
    ok("RESTATED has a reason", "RESTATED" in WHY and len(WHY["RESTATED"]) > 40)
    ok("every CLASS has a reason", set(WHY) == set(census.CLASSES))

    # --- the hand override, which the docstring has promised since iteration 34
    record = {"file": "a.md", "index": 0, "family": "WIDTH", "token": "112 bp",
              "snippet": "oxDNA2, 15 duplexes at 112 bp and phase 8 from this own rules"}
    kept = {"a.md": {"0": {"class": "OUT_OF_SCOPE", "family": "WIDTH", "token": "112 bp",
                           "byHand": True, "why": "a square-lattice design",
                           "snippet": record["snippet"][:OVERRIDE_KEY_CHARS]}}}
    ok("a hand override is found by its key", override_key(record) in hand_overrides(kept)[0])
    ok(
        "and it is found at a DIFFERENT index, because the key is not the index",
        override_key(dict(record, index=57)) in hand_overrides(kept)[0],
    )
    not_hand = {"a.md": {"0": dict(kept["a.md"]["0"], byHand=False)}}
    ok("an entry WITHOUT byHand is not an override", hand_overrides(not_hand)[0] == {})
    other_token = {"a.md": {"0": dict(kept["a.md"]["0"], token="119 bp")}}
    ok(
        "an override on another TOKEN does not apply",
        override_key(record) not in hand_overrides(other_token)[0],
    )
    other_family = {"a.md": {"0": dict(kept["a.md"]["0"], family="ROW_SPAN")}}
    ok(
        "an override on another FAMILY does not apply",
        override_key(record) not in hand_overrides(other_family)[0],
    )
    other_file = {"b.md": {"0": dict(kept["a.md"]["0"])}}
    ok(
        "an override in another FILE does not apply",
        override_key(record) not in hand_overrides(other_file)[0],
    )
    rewritten = dict(record, snippet="something else entirely here now, rewritten by a sibling")
    ok(
        "an override whose NEIGHBOURHOOD was rewritten is dropped",
        override_key(rewritten) not in hand_overrides(kept)[0],
    )
    ok("an empty table carries nothing", hand_overrides({})[0] == {})
    ok("OVERRIDE_KEY_CHARS is a stated constant", OVERRIDE_KEY_CHARS == 100)
    clash = {"a.md": {"0": dict(kept["a.md"]["0"]),
                      "1": dict(kept["a.md"]["0"], **{"class": "RECORD"})}}
    ok("a colliding hand override is REPORTED, not silently resolved", hand_overrides(clash)[1])
    ok(
        "the key is the census's snippet, so it is centred on the token",
        override_key({"file": "a.md", "family": "WIDTH", "token": "112 bp",
                      "snippet": "s"})[3] == "s",
    )

    # --- the two file sets the predicate replaced
    ok("OUT_OF_SCOPE_FILES is empty", OUT_OF_SCOPE_FILES == set())
    ok(
        "C-0152 is no longer registered as CORRECTING",
        "gpd/claims/C-0152-forced-scaffold-crossover-price.md" not in CORRECTING,
    )
    ok(
        "C-0154 is no longer registered as CORRECTING",
        "gpd/claims/C-0154-honeycomb-grillage.md" not in CORRECTING,
    )
    ok(
        "the claim documenting this census is registered, because it quotes the families' own "
        "example sentences",
        "gpd/claims/C-0176-partial-discharge-and-restatement-predicates.md" in CORRECTING,
    )
    ok(
        "the two correcting claims themselves are still registered",
        {"gpd/claims/C-0140-honeycomb-raster-turn-sense.md",
         "gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md"} <= CORRECTING,
    )

    for failure in failures:
        print("FAIL  " + failure)
    print("self-test: {} failure(s)".format(len(failures)))
    return 1 if failures else 0


if __name__ == "__main__":
    import sys
    if "--self-test" in sys.argv[1:]:
        raise SystemExit(self_test())
    raise SystemExit(main())
