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
    # NOTE for whoever next owns this tool: two of C-0152's flags are a DIFFERENT false positive,
    # and registering the file hides rather than fixes them. C-0141 discharged only HALF of
    # "single-layer square-lattice" -- it supplied the station lattice, plan ceiling and placement
    # family, and did NOT supply a grillage, because OrigamiGrillage still never reads `layers`
    # (T-253). A sentence about the GRILLAGE is live and correct and needs no discharge pointer.
    # A PARTIAL discharge is exactly where a token pattern stops being able to tell, and this wants
    # a tested predicate rather than a set membership. Queued as T-260.
    "gpd/claims/C-0151-closing-raster-selection.md",
    "gpd/claims/C-0152-forced-scaffold-crossover-price.md",
    "gpd/claims/C-0153-unrounded-prose-interpolations.md",
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
OUT_OF_SCOPE_FILES = {
    "gpd/claims/C-0081-seam-weave-congruence.md",   # a single-layer square-lattice Conditions row
    "gpd/claims/C-0127-format-string-repair.md",    # a quoted Kotlin format string
}

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


def main():
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        statuses = tracer.queue_status(handle.read())
    records = census.census(ROOT)
    table = {}
    for record in records:
        cls = classify_one(record, statuses)
        table.setdefault(record["file"], {})[str(record["index"])] = {
            "class": cls,
            "why": WHY[cls],
            "family": record["family"],
            "token": record["token"],
            "line": record["line"],
        }
    destination = os.path.join(ROOT, "tools", "T-234-classification.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(table, handle, indent=1, ensure_ascii=False, sort_keys=True)
        handle.write("\n")
    counts = Counter(v["class"] for entries in table.values() for v in entries.values())
    print("wrote {}".format(destination))
    print("  ".join("{} {}".format(k, counts[k]) for k in census.CLASSES))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
