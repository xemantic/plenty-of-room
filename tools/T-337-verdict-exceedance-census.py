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
# `T-337` -- the exceedance beside every verdict, and the re-read of the positive flatness
# readings that could not be tested without one.
#
#     tools/T-337-verdict-exceedance-census.py            # the census and the re-read
#     tools/T-337-verdict-exceedance-census.py --check    # gate, scoped; residue printed
#     tools/T-337-verdict-exceedance-census.py --self-test
#
# WHY.  `C-0223` derives from `coupling/DropoutRobustPlacement.kt`'s own three lines that a
# `flatAt*P90` verdict is EXACTLY the binomial statement `exceedance <= tolerance`, and checks
# it at `1 440` of `1 440` committed booleans with `0` disagreeing.  A record that writes the
# boolean and withholds the proportion has therefore published a hypothesis test without its
# sample: `87` of the corpus's `106` POSITIVE flatness verdicts could not be tested against
# their own sampling error at all.  `T-337` carries the datum; this is the instrument that
# reads the result.
#
# SCOPE, and why the gate and the report differ.  `C-0083`'s *a gate that cannot come clean is
# not a gate* is a statement about a PREDICATE, so `--check` gates the files this task
# re-emitted and PRINTS, ungated, the residue it does not reach -- with the count and the
# per-file list, which is `C-0129`'s prescription and the only thing that stops a narrow
# predicate becoming a claim of cleanliness.
#
# WHAT IS REUSED.  `tools/T-327-flatness-resolution.py`'s predicates and arithmetic -- the
# record walk, the verdict predicate, the exact Clopper-Pearson interval, the determinacy rule
# and the realisation back-out -- are IMPORTED, not reimplemented.  Reproducing `C-0223`'s
# population by writing the predicate again would reproduce it by accident.

"""`T-337`: the exceedance beside every verdict, and the re-read that becomes possible."""

import argparse
import collections
import glob
import importlib.util
import json
import os
import shutil
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

#: `T-5b`'s tolerance. A CONVENTION, not a physical threshold (`C-0167`).
TOLERANCE = 0.10

#: The files `T-337` re-emitted, and therefore the only ones `--check` may gate.  A literal, so
#: that a file leaving the scope leaves it in a diff rather than dropping out of a glob.
GATED = ("T-279", "T-284", "T-297", "T-299", "T-303", "T-316", "T-322")

#: Every file of `C-0223`'s eighteen that carries a `flatAt*P90` boolean in a record with no
#: exceedance at `C-0223`'s own baseline.  The residue is `REPORTED - GATED`.
REPORTED = ("T-279", "T-284", "T-291", "T-297", "T-299", "T-303", "T-310",
            "T-316", "T-322", "T-323")

#: The field a record's `flatAtP90` verdict is read on, tried in order.  The searched-distribution
#: studies write theirs on `searchedP90`, not on `p90OverStroke`, so a single key is wrong.
P90_KEYS = ("p90OverStroke", "searchedP90", "p90")

#: The confidence levels the determinacy is swept over.  `C-0223` §3 reports the count is stable
#: across these on its own population; whether it is on the larger one is a measurement.
CONFIDENCES = (0.90, 0.95, 0.99)


def resolution_module():
    """`tools/T-327-flatness-resolution.py`, imported so its predicates are the ones used.

    Resolved beside THIS file and never under `--root`: `--root` names the CORPUS to read, and a
    fixture that carries a corpus and no `tools/` would otherwise take the module with it.
    """
    path = os.path.join(ROOT, "tools", "T-327-flatness-resolution.py")
    spec = importlib.util.spec_from_file_location("t327_resolution", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _corpus_available(root):
    """Whether `root` holds a corpus the live arms can actually read.

    A tree without one is not a broken corpus, it is `T-295`'s treatment arm; the live arms skip
    there rather than failing, and say so on `stderr` (`C-0195`).

    **Absence has three shapes here and only the third is the one that bites.**  A file can be
    missing; it can be zero bytes; and -- what `T-295` actually does -- a `.json` can be emptied
    to `{}`, which exists, parses, and carries no record.  A guard written on the first two
    shapes passes, the live arms then read an empty document, and the harness's own baseline
    goes red in the treatment arm for a reason that has nothing to do with any mutation.  So the
    condition is the one the arms need: **every reported file must carry a verdict**.
    """
    for tag in REPORTED:
        matches = sorted(glob.glob(os.path.join(root, "gpd/results/%s-*.json" % tag)))
        if not matches or os.path.getsize(matches[0]) == 0:
            return False
        try:
            with open(matches[0]) as handle:
                document = json.load(handle)
        except (ValueError, OSError):
            return False
        found = []
        resolution_module()._records(document, "", found)
        if not any(resolution_module()._flat_p90_booleans(record) for _path, record in found):
            return False
    return True


def documents(root=ROOT, tags=REPORTED):
    """`{tag: document}` over the reported files, refusing rather than skipping a missing one."""
    out = {}
    for tag in tags:
        matches = sorted(glob.glob(os.path.join(root, "gpd/results/%s-*.json" % tag)))
        if not matches:
            raise SystemExit("no committed result file for %s under %s" % (tag, root))
        with open(matches[0]) as handle:
            out[tag] = json.load(handle)
    return out


def verdict_records(resolution, documents_by_tag):
    """Every record carrying a `flatAt*P90` boolean, with its exceedance -- or `None` for it.

    The exceedance is carried as `None` rather than omitted, because an omission and a statement
    of absence read alike in a dictionary and are not the same fact: population C is exactly the
    rows whose value is `None`.
    """
    rows = []
    for tag in sorted(documents_by_tag):
        found = []
        resolution._records(documents_by_tag[tag], "", found)
        for path, record in found:
            booleans = resolution._flat_p90_booleans(record)
            if not booleans:
                continue
            exceedance = record.get("exceedance")
            if isinstance(exceedance, bool) or not isinstance(exceedance, (int, float)):
                exceedance = None
            error = record.get("exceedanceStandardError")
            if isinstance(error, bool) or not isinstance(error, (int, float)):
                error = None
            rows.append({"tag": tag, "path": path, "booleans": booleans,
                         "exceedance": exceedance, "standardError": error,
                         "p90": p90_field(record)})
    return rows


def p90_field(record):
    """The `(key, value)` the record's `flatAtP90` verdict is read on, or `None`.

    Identified against the verdict rather than assumed: the field is the first candidate whose
    value is on the same side of the tolerance as the boolean.  A record whose verdict cannot be
    reconciled with any candidate returns `None` and is reported, never guessed at.
    """
    verdict = record.get("flatAtP90")
    for key in P90_KEYS:
        value = record.get(key)
        if not isinstance(value, float):
            continue
        if not isinstance(verdict, bool):
            return (key, value)
        if (value < TOLERANCE) == verdict:
            return (key, value)
    return None


def population_c(rows):
    """The verdict-bearing records that carry NO exceedance -- `C-0223` §4's population C."""
    return [row for row in rows if row["exceedance"] is None]


def donor_index(resolution, root=ROOT):
    """`{p90: [(file, path, exceedance, standard error)]}` over EVERY committed result file.

    Whole-corpus, deliberately: a population-C reading may be the same physical solve as one
    graded in a file outside `C-0223`'s eighteen, and restricting the donors would understate
    what is recoverable with no re-emission.
    """
    index = collections.defaultdict(list)
    for path in sorted(glob.glob(os.path.join(root, "gpd/results/*.json"))):
        with open(path) as handle:
            document = json.load(handle)
        found = []
        resolution._records(document, "", found)
        for record_path, record in found:
            exceedance = record.get("exceedance")
            if isinstance(exceedance, bool) or not isinstance(exceedance, (int, float)):
                continue
            for key in P90_KEYS:
                value = record.get(key)
                if isinstance(value, float):
                    index[round(value, 12)].append(
                        (os.path.basename(path), record_path, exceedance,
                         record.get("exceedanceStandardError")))
                    break
    return dict(index)


def recoverable_by_join(resolution, rows, index):
    """Population-C rows whose own `p90` is EXACTLY a donor's, so the exceedance transfers.

    A nine-digit continuous quantity does not collide by accident, and a donor set that
    disagrees with itself about the exceedance is reported as ambiguous rather than resolved.
    """
    out = []
    for row in rows:
        if row["exceedance"] is not None or row["p90"] is None:
            continue
        match = index.get(round(row["p90"][1], 12))
        if not match:
            continue
        values = sorted({round(donor[2], 12) for donor in match})
        out.append(dict(row, donors=[(d[0], d[1]) for d in match],
                        joinedExceedance=values[0] if len(values) == 1 else None,
                        joinedStandardError=match[0][3],
                        ambiguous=len(values) > 1))
    return out


def determinacy_of(resolution, rows, confidence=0.95, realisations=4000):
    """`DETERMINED` / `UNDETERMINED` per row, at the row's OWN backed-out realisation count.

    `C-0223` §4 population A: the exact two-sided Clopper-Pearson interval on the record's own
    exceedance must EXCLUDE the tolerance.  The count is backed out of `exceedance` and
    `exceedanceStandardError` rather than assumed, so a record graded at another `n` is read at
    its own -- `CLAUDE.md`'s *quote it with the state it is read at*.
    """
    out = []
    for row in rows:
        exceedance = row.get("joinedExceedance", row["exceedance"])
        if exceedance is None:
            continue
        error = row.get("joinedStandardError", row["standardError"])
        backed = resolution.realisations_of(exceedance, error)
        count = backed if backed else realisations
        x = int(round(exceedance * count))
        low, high = resolution.clopper_pearson(x, count, confidence)
        out.append(dict(
            row,
            exceedanceCount=x,
            realisations=count,
            realisationsWereBackedOut=bool(backed),
            interval=[low, high],
            oneSidedP=resolution.one_sided_binomial_p(
                x, count, TOLERANCE, all(row["booleans"].values())),
            determinacy=resolution.determinacy(x, count, confidence, TOLERANCE),
        ))
    return out


def residue(rows, gated=GATED):
    """Population-C rows OUTSIDE the gated scope -- printed, never gated (`C-0083`, `C-0129`)."""
    return [row for row in population_c(rows) if row["tag"] not in set(gated)]


def gate_defects(rows, disagreements, gated=GATED):
    """What `--check` fails on, as a pure function of the rows, so it is testable off the corpus.

    THREE kinds and not one: a verdict with no exceedance, a verdict whose own `p90` field
    cannot be identified, and a verdict that disagrees with the exceedance beside it. Counting
    only the first would let a re-emission that carries the WRONG proportion come back clean.
    """
    inside = [row for row in rows if row["tag"] in set(gated)]
    return {
        "records": inside,
        "naked": population_c(inside),
        "unidentified": [row for row in inside if row["p90"] is None],
        "disagreeing": list(disagreements),
    }


def gate_defect_count(defects):
    """The gate's own defect count: ALL THREE kinds, summed. A count of one kind is not a gate."""
    return (len(defects["naked"]) + len(defects["unidentified"])
            + len(defects["disagreeing"]))


def _positive(rows):
    return [row for row in rows if any(row["booleans"].values())]


def report(root=ROOT, gated=GATED):
    """The census, the re-read and the residue, on one page."""
    resolution = resolution_module()
    live = documents(root)
    rows = verdict_records(resolution, live)
    booleans = sum(len(r["booleans"]) for r in rows)
    withheld = population_c(rows)
    positive = _positive(rows)
    positive_withheld = _positive(withheld)

    print("T-337 - the exceedance beside every verdict, over %d reported file(s)" % len(live))
    print("  verdict-bearing records                       : %d" % len(rows))
    print("  flatAt*P90 booleans                           : %d" % booleans)
    print("  positive verdicts                             : %d" % len(positive))
    print("  ... in a record carrying NO exceedance        : %d" % len(positive_withheld))
    print("  records carrying no exceedance                : %d" % len(withheld))
    print("  identity disagreements (flatAtP90 <=> e<=tol) : %d"
          % len(resolution.identity_disagreements_of(live)))

    graded = determinacy_of(resolution, [r for r in positive if r["exceedance"] is not None])
    print()
    print("  POSITIVE verdicts testable from their own record: %d" % len(graded))
    for confidence in CONFIDENCES:
        here = determinacy_of(
            resolution, [r for r in positive if r["exceedance"] is not None], confidence)
        undetermined = [r for r in here if r["determinacy"] == "UNDETERMINED"]
        print("    at %2d%% : %3d UNDETERMINED, %3d DETERMINED"
              % (round(confidence * 100), len(undetermined), len(here) - len(undetermined)))

    per_file = collections.Counter(r["tag"] for r in graded)
    per_file_undetermined = collections.Counter(
        r["tag"] for r in graded if r["determinacy"] == "UNDETERMINED")
    print()
    print("  %-8s %10s %14s" % ("file", "positive", "UNDETERMINED"))
    for tag in sorted(per_file):
        print("  %-8s %10d %14d" % (tag, per_file[tag], per_file_undetermined[tag]))

    joined = recoverable_by_join(resolution, withheld, donor_index(resolution, root))
    print()
    print("  recoverable with NO re-emission (whole-corpus p90 join): %d, of which positive %d"
          % (len(joined), len(_positive(joined))))

    left = residue(rows, gated)
    print()
    print("  RESIDUE -- outside the gated scope, printed and NOT gated: %d record(s), %d positive"
          % (len(left), len(_positive(left))))
    for tag, count in sorted(collections.Counter(r["tag"] for r in left).items()):
        positives = sum(1 for r in left if r["tag"] == tag and any(r["booleans"].values()))
        print("    %-8s %4d record(s), %d positive" % (tag, count, positives))
    return rows


def check(root=ROOT, gated=GATED):
    """Gate the gated scope; print the residue beside it, ungated."""
    resolution = resolution_module()
    live = documents(root)
    rows = verdict_records(resolution, live)
    disagreements = resolution.identity_disagreements_of(
        {tag: live[tag] for tag in gated if tag in live})
    defects_by_kind = gate_defects(rows, disagreements, gated)
    inside = defects_by_kind["records"]
    naked = defects_by_kind["naked"]
    unidentified = defects_by_kind["unidentified"]
    disagreeing = defects_by_kind["disagreeing"]

    for row in naked[:8]:
        print("defect: %s%s writes %s and no exceedance"
              % (row["tag"], row["path"], ",".join(sorted(row["booleans"]))))
    for row in unidentified[:8]:
        print("defect: %s%s -- no p90 field reconciles with its own verdict"
              % (row["tag"], row["path"]))
    for tag, path, key, value, exceedance in disagreeing[:8]:
        print("defect: %s%s/%s = %s against an exceedance of %s"
              % (tag, path, key, value, exceedance))

    left = residue(rows, gated)
    print("note: %d verdict-bearing record(s) outside the gated scope carry no exceedance "
          "(%d positive) -- printed, not gated:"
          % (len(left), len(_positive(left))))
    for tag, count in sorted(collections.Counter(r["tag"] for r in left).items()):
        positives = sum(1 for r in left if r["tag"] == tag and any(r["booleans"].values()))
        print("      %-8s %4d record(s), %d positive" % (tag, count, positives))

    defects = gate_defect_count(defects_by_kind)
    print("verdict-exceedance census: %d gated record(s), %d defect(s)" % (len(inside), defects))
    return defects == 0


# ------------------------------------------------------------------------------ the self-tests

def self_test(root=ROOT):
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    resolution = resolution_module()

    # --- gate 1: the population is C-0223's, by construction ----------------------------------
    ok("T-337 the verdict predicate is C-0223's own, imported and not rewritten",
       verdict_records.__globals__["P90_KEYS"] is P90_KEYS
       and resolution._flat_p90_booleans({"flatAtP90": True}) == {"flatAtP90": True})
    ok("T-337 a boolean that does not start `flat` is not a verdict",
       resolution._flat_p90_booleans({"beatsUncoupledAtP90": True}) == {})
    ok("T-337 a `flat` boolean with no p90 in its name is not a verdict",
       resolution._flat_p90_booleans({"flatAndAdmissible": True}) == {})

    # --- gate 2: the p90 a verdict is read on is IDENTIFIED, never assumed --------------------
    ok("T-337 a verdict on p90OverStroke identifies that field",
       p90_field({"p90OverStroke": 0.05, "flatAtP90": True}) == ("p90OverStroke", 0.05))
    ok("T-337 a verdict on searchedP90 identifies THAT field, not a neighbouring p90",
       p90_field({"equalP90": 0.5, "searchedP90": 0.05, "flatAtP90": True})
       == ("searchedP90", 0.05))
    # The candidates are tried in order, so a record carrying an EARLIER candidate on the wrong
    # side of the tolerance is what makes the reconciliation observable at all (`C-0161`).
    ok("T-337 an earlier candidate on the WRONG side of the tolerance is rejected, not taken",
       p90_field({"p90OverStroke": 0.5, "searchedP90": 0.05, "flatAtP90": True})
       == ("searchedP90", 0.05))
    ok("T-337 and the same record with the OPPOSITE verdict identifies the other field",
       p90_field({"p90OverStroke": 0.5, "searchedP90": 0.05, "flatAtP90": False})
       == ("p90OverStroke", 0.5))
    ok("T-337 a record with no p90 field at all returns None rather than guessing",
       p90_field({"flatAtP90": True}) is None)

    # --- gate 3: population C is exactly `a verdict with no exceedance` ------------------------
    withheld = [{"tag": "X", "path": "/c/0", "booleans": {"flatAtP90": True},
                 "exceedance": None, "standardError": None, "p90": ("p90OverStroke", 0.099)}]
    carried = [{"tag": "X", "path": "/c/1", "booleans": {"flatAtP90": True},
                "exceedance": 0.098, "standardError": 0.0047, "p90": ("p90OverStroke", 0.099)}]
    ok("T-337 a record carrying no exceedance is population C",
       [r["path"] for r in population_c(withheld + carried)] == ["/c/0"])
    ok("T-337 a record carrying one is NOT population C, whatever its value",
       population_c(carried) == [])
    ok("T-337 an exceedance of exactly zero is still an exceedance, not an absence",
       population_c([{"tag": "X", "path": "/c/2", "booleans": {"flatAtP90": True},
                      "exceedance": 0.0, "standardError": 0.0,
                      "p90": ("p90OverStroke", 0.01)}]) == [])

    # --- gate 4: the determinacy rule is C-0223 §4 population A, at the record's own n ---------
    ok("T-337 the undetermined band at n = 4000 and 95 % is [363, 438]",
       resolution.resolution_band(4000, 0.95) == (363, 438))
    ok("T-337 a reading inside the band is UNDETERMINED",
       determinacy_of(resolution, [dict(carried[0], exceedance=400 / 4000.0)])[0]["determinacy"]
       == "UNDETERMINED")
    ok("T-337 a reading outside it is DETERMINED, and keeps the verdict's own direction",
       determinacy_of(resolution, [dict(carried[0], exceedance=200 / 4000.0)])[0]["determinacy"]
       == "DETERMINED")
    ok("T-337 the determinacy is taken at the RECORD's own n, backed out of its standard error",
       determinacy_of(
           resolution,
           [dict(carried[0], exceedance=0.1, standardError=(0.1 * 0.9 / 1000.0) ** 0.5)]
       )[0]["realisations"] == 1000)
    ok("T-337 the undetermined count is non-decreasing in the confidence level",
       resolution.resolution_band(4000, 0.90)[0] > resolution.resolution_band(4000, 0.99)[0])

    # --- gate 5: the join is an EQUALITY on the p90 the verdict is read on ---------------------
    index = {round(0.099, 12): [("T-279-x.json", "/cells/9", 0.098, 0.043)]}
    ok("T-337 a population-C reading whose p90 equals a donor's is recovered",
       [r["path"] for r in recoverable_by_join(resolution, withheld, index)] == ["/c/0"])
    ok("T-337 a reading with no donor is not recovered, and is not an error",
       recoverable_by_join(
           resolution,
           [dict(withheld[0], p90=("p90OverStroke", 0.5))], index) == [])
    ok("T-337 a reading with NO p90 field cannot be joined at all",
       recoverable_by_join(resolution, [dict(withheld[0], p90=None)], index) == [])

    # --- gate 6: the gate's SCOPE and its residue are complementary and both non-empty ---------
    mixed = [dict(withheld[0], tag="T-279"), dict(withheld[0], tag="T-323")]
    ok("T-337 the residue is exactly the withheld verdicts OUTSIDE the gated scope",
       [r["tag"] for r in residue(mixed, gated=("T-279",))] == ["T-323"])
    ok("T-337 a gated file's withheld verdict is NOT residue -- it is a gate failure",
       residue([dict(withheld[0], tag="T-279")], gated=("T-279",)) == [])
    ok("T-337 the gated scope is a strict subset of what is reported, so a residue can exist",
       set(GATED) < set(REPORTED))

    # --- gate 6b: a JSON `true` is not a proportion, and falsiness is not absence -------------
    booly = {"cells": [{"exceedance": True, "flatAtP90": True, "p90OverStroke": 0.05}]}
    ok("T-337 an exceedance that is a JSON boolean is read as ABSENT, never as a number",
       verdict_records(resolution, {"X": booly})[0]["exceedance"] is None)

    # --- gate 6c: the join refuses an ambiguous donor set and never overrides a record ---------
    ambiguous = {round(0.099, 12): [("a.json", "/c/0", 0.098, 0.0047),
                                    ("b.json", "/c/1", 0.101, 0.0047)]}
    joined = recoverable_by_join(resolution, withheld, ambiguous)
    ok("T-337 a donor set that disagrees with itself is AMBIGUOUS, not resolved to its first",
       len(joined) == 1 and joined[0]["ambiguous"] and joined[0]["joinedExceedance"] is None)
    ok("T-337 an ambiguous join yields no determinacy rather than a determinacy off one donor",
       determinacy_of(resolution, joined) == [])
    ok("T-337 a record that CARRIES an exceedance is never overridden by a donor",
       recoverable_by_join(resolution, carried, index) == [])

    # --- gate 6d: the gate's three defect kinds, off the corpus -------------------------------
    unident = dict(withheld[0], tag="T-279", exceedance=0.05, p90=None)
    ok("T-337 a verdict whose own p90 field cannot be identified is a gate defect",
       len(gate_defects([unident], [], gated=("T-279",))["unidentified"]) == 1)
    ok("T-337 an identity disagreement is a gate defect, so a WRONG proportion cannot pass",
       len(gate_defects([], [("T-279", "/c/0", "flatAtP90", True, 0.2)],
                        gated=("T-279",))["disagreeing"]) == 1)
    ok("T-337 the gate reads only its own scope, so a residue file's defect is not a failure",
       gate_defects([dict(withheld[0], tag="T-323")], [], gated=("T-279",))["naked"] == [])

    ok("T-337 the gate's defect count sums all THREE kinds, so no kind can be dropped",
       gate_defect_count({"records": [], "naked": [1], "unidentified": [1, 2],
                          "disagreeing": [1, 2, 3]}) == 6)

    # --- gate 6f: the donor index is WHOLE-corpus, and the fixture is CONSTRUCTED --------------
    #
    # `C-0161`/`C-0195`: this rule was held open only by the live corpus, so `T-295`'s emptied
    # arm read it as corpus-dependent. The state is constructed instead -- two result files, one
    # of them named OUTSIDE the `T-3*` family the narrowed predicate would restrict to.
    fixture = tempfile.mkdtemp(prefix="T-337-donors.")
    try:
        os.makedirs(os.path.join(fixture, "gpd", "results"))
        for name, body in (
            ("T-316-searched.json", {"cells": [{"searchedP90": 0.05, "exceedance": 0.02,
                                                "exceedanceStandardError": 0.0022,
                                                "flatAtP90": True}]}),
            ("C-9999-elsewhere.json", {"cells": [{"p90OverStroke": 0.07, "exceedance": 0.03,
                                                  "exceedanceStandardError": 0.0027,
                                                  "flatAtP90": True}]}),
        ):
            with open(os.path.join(fixture, "gpd", "results", name), "w") as handle:
                json.dump(body, handle)
        constructed = donor_index(resolution, fixture)
        constructed_files = {name for matches in constructed.values()
                             for name, _p, _e, _s in matches}
        ok("T-337 the donor index is WHOLE-corpus: a donor named outside the T-3 family is in it",
           constructed_files == {"T-316-searched.json", "C-9999-elsewhere.json"})
        ok("T-337 and it is keyed on the p90 the verdict is read on, whichever field that is",
           sorted(round(key, 12) for key in constructed) == [0.05, 0.07])
    finally:
        shutil.rmtree(fixture, ignore_errors=True)

    # --- gate 6e: a missing file is refused, never skipped -------------------------------------
    refused = False
    try:
        documents(root, tags=("T-999",))
    except SystemExit:
        refused = True
    ok("T-337 a reported file with no committed result REFUSES rather than shrinking the census",
       refused)

    # --- gate 7: the live corpus, which is the arm that must fail before the sweep -------------
    #
    # `C-0195`/`C-0223` §8: `tools/T-295-mutation-input-census.py` runs every harness in a copy
    # of the tree with every committed artifact outside `tools/` EMPTIED, to ask whether a
    # mutation is held open by a fixture or by corpus state.  A subject whose live-corpus arms
    # crash there makes its own baseline not green, and the census can then only REFUSE the
    # harness rather than measure it.  So these arms degrade VISIBLY -- to `stderr`, because
    # `--self-test > /dev/null` swallows stdout -- and the run reads four arms fewer.
    if not _corpus_available(root):
        sys.stderr.write(
            "# SKIPPED 5 of %d arms: no committed result file under %s carries a verdict, so\n"
            "#   the live-corpus arms cannot run.  Every arm reading its own fixture ran.\n"
            % (len(checks) + 5, root))
        failed = [name for name, passed in checks if not passed]
        for name, passed in checks:
            print("%-4s %s" % ("ok" if passed else "FAIL", name))
        print("# %d self-test(s), %d failed" % (len(checks), len(failed)))
        return not failed

    live = documents(root)
    ok("T-337 every reported file resolves to exactly one committed result file",
       sorted(live) == sorted(REPORTED))
    rows = verdict_records(resolution, live)
    ok("T-337 the live corpus carries verdict-bearing records in every reported file",
       {r["tag"] for r in rows} == set(REPORTED))
    ok("T-337 every live verdict's own p90 field is identified, with no guesses",
       all(r["p90"] is not None for r in rows))
    ok("T-337 the identity holds at every live record that carries both",
       resolution.identity_disagreements_of(live) == [])
    donors = donor_index(resolution, root)
    donor_files = {name for matches in donors.values() for name, _p, _e, _s in matches}
    ok("T-337 and on the LIVE corpus it likewise spans files outside the reported ten",
       any(not name.startswith("T-3") for name in donor_files)
       and len(donor_files) > len(REPORTED))

    failed = [name for name, passed in checks if not passed]
    for name, passed in checks:
        print("%-4s %s" % ("ok" if passed else "FAIL", name))
    print("# %d self-test(s), %d failed" % (len(checks), len(failed)))
    return not failed


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--root", default=ROOT)
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return 0 if self_test(arguments.root) else 1
    if arguments.check:
        return 0 if check(arguments.root) else 1
    report(arguments.root)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
