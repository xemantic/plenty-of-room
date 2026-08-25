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
# `T-337`'s result file.
#
#     tools/T-337-emit-result.py --emit
#     tools/T-337-emit-result.py --self-test
#
# HOW THIS FILE NAMES THE STATE IT MEASURED, which is the awkward part.  `CH-0246`: a result
# file whose SUBJECT is the corpus is a function of a mutable object, so it must name the state
# it read.  `T-327` pinned a ref, and could, because its subject was the corpus as it already
# stood.  This task's subject is the corpus AFTER its own re-emission, and that state has no
# commit until the iteration closes -- so the answer is named by CONTENT instead: a `sha256`
# per result file read, under `inputDigests`, with `baselineRef` explicitly `null`.  That is
# `T-327`'s own degraded-build vocabulary (*a degraded build names NO baseline ref, so it can
# never be read as a pinned one*), reused rather than replaced, and a content digest is a
# STRONGER pin than a ref: it survives a rebase and it works with no repository at all.
#
# The BEFORE census is pinned in the ordinary way, at `a83171d` -- the commit this task's
# Formulate and Plan were committed at -- so `P1`'s reproduction of `C-0223` is a statement
# about a named corpus state and not about today's tree.

"""`T-337`: the exceedance beside every verdict, and the `87` re-read."""

import argparse
import collections
import hashlib
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(ROOT, "gpd", "results",
                           "T-337-the-exceedance-beside-every-verdict.json")

#: The commit this task's Formulate and Plan were committed at -- the BEFORE state.
BASELINE_REF = "a83171d"


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t337_census", "T-337-verdict-exceedance-census.py")
resolution = _load("t337_resolution", "T-327-flatness-resolution.py")
rounding = _load("t337_rounding", "T-278-rounding-simulation.py")
header = _load("t337_header", "emission_header.py")

_REV_PARSE = ("rev-parse", "--verify", "--quiet")


def _resolve(ref):
    return subprocess.check_output(
        ["git", "-C", ROOT, *_REV_PARSE, "%s^{commit}" % ref]).decode().strip()


def repository_available(ref=BASELINE_REF):
    """Whether `ref` is a commit in a repository at `ROOT`.

    `tools/snapshot.sh` excludes `./.git`, so a wired self-test that resolves a ref cannot run
    inside a snapshot -- the fifth such collision in four iterations (`C-0195`, `C-0223` §8).
    This file degrades VISIBLY rather than passing silently.  `git rev-parse` alone validates
    only the SYNTAX of an object name, so the guard asks for `--verify <ref>^{commit}`.
    """
    try:
        subprocess.check_output(["git", "-C", ROOT, *_REV_PARSE, "%s^{commit}" % ref],
                                stderr=subprocess.DEVNULL)
        return True
    except (subprocess.CalledProcessError, OSError):
        return False


def digest_of(path):
    """The `sha256` of one file -- how the AFTER reading is pinned, since it has no commit yet."""
    with open(path, "rb") as handle:
        return hashlib.sha256(handle.read()).hexdigest()


def input_digests(root=ROOT, tags=census.REPORTED):
    """`{basename: sha256}` over the result files the census reads, in tag order."""
    import glob
    out = collections.OrderedDict()
    for tag in tags:
        matches = sorted(glob.glob(os.path.join(root, "gpd/results/%s-*.json" % tag)))
        if not matches:
            raise SystemExit("no committed result file for %s under %s" % (tag, root))
        out[os.path.basename(matches[0])] = digest_of(matches[0])
    return out


def _documents_at_ref(ref):
    """`({ten}, {eighteen}, sha)` from a `git archive` of `ref`, or `None`s with no repository.

    TWO scopes, deliberately.  `C-0223`'s counts are over its own EIGHTEEN files and this task
    re-emits ten of them, so a reproduction taken over the ten would not be `C-0223`'s number
    and a census taken over the eighteen would include files `T-337` never touches.  Both are
    emitted, each named by its own scope, rather than one being quietly substituted for the
    other -- `CH-0182`, met on a scope instead of on a date.
    """
    if not repository_available(ref):
        return None, None, None, None
    sha = _resolve(ref)
    tree = tempfile.mkdtemp(prefix="T-337-")
    try:
        archive = subprocess.check_output(["git", "-C", ROOT, "archive", sha, "gpd/results"])
        subprocess.run(["tar", "-x", "-C", tree], input=archive, check=True)
        # The donor index for the CHEAP BOUND must be built at the ref as well.  Built from
        # today's tree it would find the exceedances THIS TASK carried and report them as
        # recoverable without a re-emission, which is the measurement inverting itself.
        return (census.documents(tree),
                census.documents(tree, tags=resolution.FILES),
                census.donor_index(resolution, tree), sha)
    finally:
        shutil.rmtree(tree, ignore_errors=True)


def _positive(rows):
    return [row for row in rows if any(row["booleans"].values())]


def _reading(documents_by_tag, gated=census.GATED):
    """The census, the determinacy re-read and the residue, over one corpus state."""
    rows = census.verdict_records(resolution, documents_by_tag)
    withheld = census.population_c(rows)
    positive = _positive(rows)
    testable = [row for row in positive if row["exceedance"] is not None]
    swept = collections.OrderedDict()
    for confidence in census.CONFIDENCES:
        graded = census.determinacy_of(resolution, testable, confidence)
        undetermined = [row for row in graded if row["determinacy"] == "UNDETERMINED"]
        swept["%d" % round(confidence * 100)] = {
            "testable": len(graded),
            "undetermined": len(undetermined),
            "determined": len(graded) - len(undetermined),
        }
    graded = census.determinacy_of(resolution, testable, 0.95)

    # The records that sit at EXACTLY the tolerance.  `flatAtP90` is `exceedance <= 0.10`, so
    # `x = 400` of `4 000` is a FLAT verdict one realisation from not being one -- the tightest
    # reading the identity admits, and a headline the deliverables need.
    at_boundary = [row for row in graded
                   if row["exceedanceCount"] * 10 == row["realisations"]]

    # The two counts both deliverables LEAD with, per block, because a per-file total does not
    # separate a cell from a rung and the deliverables quote the cells.
    import re as _re
    deliverable = collections.OrderedDict()
    for tag, block, owner in (("T-316", "/cells/*", "C-0212's 22 of 32"),
                              ("T-322", "/cells/*", "C-0215's 27 of 48"),
                              ("T-316", "/rungs/*", None),
                              ("T-322", "/rungs/*", None)):
        here = [row for row in graded
                if row["tag"] == tag and _re.sub(r"/\d+", "/*", row["path"]) == block]
        undetermined = [row for row in here if row["determinacy"] == "UNDETERMINED"]
        deliverable["%s%s" % (tag, block)] = {
            "quotedBy": owner,
            "positive": len(here),
            "determined": len(here) - len(undetermined),
            "undetermined": len(undetermined),
        }

    negative = [row for row in rows
                if not any(row["booleans"].values()) and row["exceedance"] is not None]
    negative_graded = census.determinacy_of(resolution, negative, 0.95)

    per_file = collections.OrderedDict()
    for tag in sorted({row["tag"] for row in positive}):
        here = [row for row in graded if row["tag"] == tag]
        per_file[tag] = {
            "positiveVerdicts": sum(1 for row in positive if row["tag"] == tag),
            "testable": len(here),
            "undetermined": sum(1 for row in here if row["determinacy"] == "UNDETERMINED"),
        }
    left = census.residue(rows, gated)
    return {
        "verdictBearingRecords": len(rows),
        "flatAtP90Booleans": sum(len(row["booleans"]) for row in rows),
        "positiveVerdicts": len(positive),
        "positiveVerdictsCarryingNoExceedance": len(_positive(withheld)),
        "recordsCarryingNoExceedance": len(withheld),
        "recordsCarryingNoExceedanceByFile": collections.OrderedDict(
            sorted(collections.Counter(row["tag"] for row in withheld).items())),
        "positiveCarryingNoExceedanceByFile": collections.OrderedDict(
            sorted(collections.Counter(row["tag"] for row in _positive(withheld)).items())),
        "identityDisagreements": len(resolution.identity_disagreements_of(documents_by_tag)),
        "positiveVerdictsTestable": len(testable),
        "determinacySweptOverConfidence": swept,
        "positiveVerdictsByFile": per_file,
        "residueRecords": len(left),
        "residuePositive": len(_positive(left)),
        "residueByFile": collections.OrderedDict(
            sorted(collections.Counter(row["tag"] for row in left).items())),
        "marginalPositives": sum(
            1 for row in positive if row["p90"] and row["p90"][1] >= 0.0975),
        "marginalPositivesUndetermined": sum(
            1 for row in graded
            if row["p90"] and row["p90"][1] >= 0.0975 and row["determinacy"] == "UNDETERMINED"),
        "marginalBand": ("p90 >= 0.0975, a CONVENIENCE for reporting expected yield and not a "
                         "rule: T-322/cells/25 is inside it and DETERMINED, which is the band's "
                         "own counter-example"),
        "atExactlyTheTolerance": {
            "what": ("flatAtP90 is `exceedance <= 0.10`, so x = n/10 is a FLAT verdict one "
                     "realisation from not being one -- the tightest reading the identity "
                     "admits"),
            "records": len(at_boundary),
            "oneSidedP": at_boundary[0]["oneSidedP"] if at_boundary else None,
            "where": ["%s%s" % (row["tag"], row["path"]) for row in at_boundary],
            "p90": [row["p90"][1] if row["p90"] else None for row in at_boundary],
        },
        "deliverableLeadingCounts": deliverable,
        "negativeVerdictsTestable": len(negative_graded),
        "negativeVerdictsUndetermined": sum(
            1 for row in negative_graded if row["determinacy"] == "UNDETERMINED"),
    }


def _join_reading(documents_by_tag, index):
    """The cheap bound's recovery: population-C rows an exact whole-corpus `p90` join answers.

    `index` is the donor index at the SAME corpus state as `documents_by_tag` -- see
    `_documents_at_ref`: a donor index from today's tree would recover what this task carried.
    """
    rows = census.verdict_records(resolution, documents_by_tag)
    withheld = census.population_c(rows)
    joined = census.recoverable_by_join(resolution, withheld, index)
    graded = census.determinacy_of(resolution, joined, 0.95)
    positive = [row for row in graded if any(row["booleans"].values())]
    return {
        "populationC": len(withheld),
        "recoveredByJoin": len(joined),
        "recoveredPositive": len(_positive(joined)),
        "recoveredPositiveUndetermined": sum(
            1 for row in positive if row["determinacy"] == "UNDETERMINED"),
        "ambiguousDonorSets": sum(1 for row in joined if row["ambiguous"]),
        "needingAReEmission": len(_positive(withheld)) - len(_positive(joined)),
        "recovered": [
            {"tag": row["tag"], "path": row["path"],
             "p90": row["p90"][1] if row["p90"] else None,
             "exceedance": row.get("joinedExceedance"),
             "exceedanceCount": row["exceedanceCount"],
             "realisations": row["realisations"],
             "determinacy": row["determinacy"],
             "donors": ["%s%s" % (name.split("-")[0] + "-" + name.split("-")[1], path)
                        for name, path in row["donors"]]}
            for row in positive],
    }


#: What the runs cost and what they moved, written by `gpd/data/T-337-cheap-bound/additive-diff.py`
#: and the run driver.  It is an INPUT to this emitter and not a measurement taken by it -- a
#: wall clock in a result file is a step counter by another name (`CLAUDE.md`), so the seconds
#: are recorded as a COST, once, beside what they bought, and no field of this document is a
#: function of them.
RUNS = os.path.join(ROOT, "tools", "T-337-runs.json")


def _run_record():
    if not os.path.isfile(RUNS):
        return None
    with open(RUNS) as handle:
        record = json.load(handle)
    cheap = [row for row in record["files"] if row["tag"] not in ("T-316", "T-322")]
    record["cheapStudySeconds"] = sum(row["seconds"] for row in cheap)
    record["searchStudySeconds"] = record["totalStudySeconds"] - record["cheapStudySeconds"]
    return record


def _falsifiers(document):
    """The twelve declared in the task file, each with what it did."""
    after = document["afterTheSweep"]["reading"]
    runs = document.get("reEmission") or {}
    moved = sum(row.get("movedLeaves", 0) for row in runs.get("files", []))
    unexpected = sum(row.get("unexpectedKeys", 0) for row in runs.get("files", []))
    backed = runs.get("everyRecordBacksOutFourThousand")
    return [
        {"id": "F1",
         "statement": "the identity flatAtP90 <=> exceedance <= tolerance fails at any "
                      "re-emitted record",
         "fired": after["identityDisagreements"] != 0},
        {"id": "F2",
         "statement": "a re-emitted file moves any PRE-EXISTING leaf, or adds a key that is not "
                      "one of the three carried -- the largest risk in the row, three of the "
                      "studies being searches",
         "fired": bool(moved or unexpected),
         "movedLeaves": moved, "unexpectedKeys": unexpected},
        {"id": "F3",
         "statement": "any re-emitted record backs out an n other than 4 000",
         "fired": backed is False},
        {"id": "F4",
         "statement": "fewer than 12 of the 25 marginal positives (p90 >= 0.0975) read "
                      "UNDETERMINED -- then the 7-of-7 donor prior does not transfer",
         "fired": after["marginalPositivesUndetermined"] < 12,
         "marginalPositives": after["marginalPositives"],
         "undetermined": after["marginalPositivesUndetermined"]},
        {"id": "F5",
         "statement": "ZERO of the readings reached are UNDETERMINED -- then the sweep buys "
                      "nothing and C-0223's refusal should have stood",
         "fired": after["determinacySweptOverConfidence"]["95"]["undetermined"] == 0},
        {"id": "F6",
         "statement": "a gate other than the PREDICTED T-327-emit working-tree control arm goes "
                      "red -- then the record-shape change has a consequence nobody predicted",
         "fired": runs.get("unpredictedGateWentRed", False)},
        {"id": "F7",
         "statement": "any of the join-recovered exceedances disagrees with its re-emitted value "
                      "-- then the p90 join is not an identity and the cheap bound is unsound",
         "fired": runs.get("joinDisagreements", 0) != 0},
        {"id": "F8",
         "statement": "a downstream consumer moves a field that is not one of the new ones",
         "fired": runs.get("consumerMovedFields", 0) != 0},
        {"id": "F9",
         "statement": "the undetermined count is NOT monotone non-decreasing in the confidence "
                      "level -- closed by nested intervals, asserted anyway",
         "fired": not (after["determinacySweptOverConfidence"]["90"]["undetermined"]
                       <= after["determinacySweptOverConfidence"]["95"]["undetermined"]
                       <= after["determinacySweptOverConfidence"]["99"]["undetermined"])},
        {"id": "F10",
         "statement": "two emissions of one re-emitted study are not byte-identical",
         "fired": runs.get("byteIdenticalOnASecondRun") is False},
        {"id": "F11",
         "statement": "the new census cannot come clean over its own declared scope -- then per "
                      "C-0083 it is not a gate and ships as an audit",
         "fired": runs.get("gateIsClean") is False},
        {"id": "F12",
         "statement": "the new tools fail inside a .git-less copy -- last iteration's red gate, "
                      "repeated",
         "fired": runs.get("gitlessGreen") is False},
    ]


def build(root=ROOT, ref=BASELINE_REF, gated=census.GATED):
    live = census.documents(root)
    after = _reading(live, gated)
    after_eighteen = _reading(census.documents(root, tags=resolution.FILES), gated)
    at_ref_documents, at_ref_eighteen, at_ref_donors, sha = _documents_at_ref(ref)
    before = _reading(at_ref_documents, gated) if at_ref_documents else None
    before_eighteen = _reading(at_ref_eighteen, gated) if at_ref_eighteen else None

    document = collections.OrderedDict()
    document["task"] = "T-337"
    document["leaf"] = "A8.2"
    document["claim"] = "C-0225"
    document["title"] = ("the exceedance beside every verdict, and the positive flatness "
                         "readings that could not be tested without one")
    document["question"] = (
        "C-0223 derives that a flatAt*P90 verdict IS the binomial statement "
        "exceedance <= 0.10, and refused 87 of the corpus's 106 POSITIVE flatness verdicts "
        "because their record emits no exceedance. Carry the datum the studies already "
        "compute, and read those verdicts at C-0223 section 4's stated resolution.")
    document["verificationType"] = (
        "in-silico (coupled-cell studies re-emitted with one carried field per verdict-bearing "
        "record) + logical (the exact Clopper-Pearson re-read, arithmetic on a proportion)")
    document["maturity"] = (
        "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. No solve, seed, "
        "tolerance or geometry changed; what moved is what a record CARRIES.")

    # --- how this file names the state it measured (CH-0246) -----------------------------------
    document["baselineRef"] = None
    document["baselineRefRequested"] = None
    document["howTheStateIsNamed"] = (
        "This file's ANSWER is a reading of the corpus AFTER this task's own re-emission, and "
        "that state has no commit at emit time, so it is pinned BY CONTENT: inputDigests below "
        "carries a sha256 per result file read. baselineRef is explicitly null, which is "
        "T-327's own vocabulary for a reading that is not at a named commit (CH-0246). The "
        "BEFORE census under atBaselineRef IS pinned, at the commit this task's Formulate and "
        "Plan were committed at.")
    document["inputDigests"] = input_digests(root)

    document["parameters"] = collections.OrderedDict([
        ("tolerance", census.TOLERANCE),
        ("toleranceIsA", "T-5b's CONVENTION, not a physical threshold"),
        ("realisations", 4000),
        ("realisationsAreBackedOut",
         "from each record's own exceedance and exceedanceStandardError, never assumed"),
        ("confidenceLevels", list(census.CONFIDENCES)),
        ("determinacyRule",
         "C-0223 section 4 population A: the exact two-sided Clopper-Pearson interval on the "
         "record's own exceedance must EXCLUDE the tolerance"),
        ("undeterminedBandAt4000And95Percent", list(resolution.resolution_band(4000, 0.95))),
        ("reEmittedFiles", list(gated)),
        ("reportedFiles", list(census.REPORTED)),
    ])

    document["citedInputs"] = collections.OrderedDict([
        ("C-0223", "the identity flatAtP90 <=> exceedance <= tolerance, checked at 1 440 of "
                   "1 440 committed booleans with 0 disagreeing, and the stated resolution"),
        ("C-0180", "the two recovered cells whose verdicts five further records transcribe"),
        ("C-0087", "the measured staple incorporation the dropout ensemble is drawn at"),
        ("C-0212", "the searched distribution whose 22 of 32 the deliverables lead with"),
        ("C-0215", "route B coupled on its own stations, and its 27 of 48"),
    ])

    document["afterTheSweep"] = collections.OrderedDict([
        ("scope",
         "the TEN files that carry a flatAt*P90 verdict in a record with no exceedance"),
        ("reading", after),
        ("overTheEighteenFiles", after_eighteen),
    ])
    document["atBaselineRef"] = collections.OrderedDict([
        ("ref", sha),
        ("refRequested", ref if sha else None),
        ("reading", before),
        ("overTheEighteenFiles", before_eighteen),
    ]) if before else None
    document["reEmission"] = _run_record()
    document["falsifiers"] = _falsifiers(document)
    # The cheap bound is a statement about the corpus BEFORE the sweep, so it is emitted only
    # where the ref can be built.  A working-tree reading of it would answer a different
    # question and is refused rather than substituted.
    document["cheapBound"] = (
        _join_reading(at_ref_eighteen, at_ref_donors) if at_ref_eighteen else None)

    rounded = rounding.walk(document, rounding.RESULT_SIGNIFICANT_DIGITS,
                            rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)
    return header.with_emission_header(rounded, "honeycomb", regime=[])


def _self_test(root=ROOT):
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    pinned = repository_available()
    if not pinned:
        sys.stderr.write(
            "# SKIPPED 5 of 20 arms: no git repository holding %s under %s, so neither the\n"
            "#   BEFORE census nor the cheap bound can be built, and C-0223's reproduction\n"
            "#   cannot be taken.  The 12 arms that do not need the ref all ran.\n"
            % (BASELINE_REF, ROOT))

    document = build(root)
    ok("T-337-emit the emission header is first and states the honeycomb and no regime",
       list(document)[0] == "emission"
       and document["emission"] == {"lattice": "honeycomb", "regime": []})
    ok("T-337-emit the answer names NO baseline ref, because its state has no commit yet",
       document["baselineRef"] is None and document["baselineRefRequested"] is None)
    ok("T-337-emit the answer is pinned BY CONTENT instead, one digest per file read",
       len(document["inputDigests"]) == len(census.REPORTED)
       and all(len(value) == 64 for value in document["inputDigests"].values()))
    ok("T-337-emit a digest is of the FILE and changes with it",
       digest_of(os.path.join(ROOT, "tools", "T-337-emit-result.py"))
       != digest_of(os.path.join(ROOT, "tools", "T-337-verdict-exceedance-census.py")))
    ok("T-337-emit the identity holds at every record of the reading",
       document["afterTheSweep"]["reading"]["identityDisagreements"] == 0)
    ok("T-337-emit the undetermined count is non-decreasing in the confidence level",
       document["afterTheSweep"]["reading"]["determinacySweptOverConfidence"]["90"]
       ["undetermined"]
       <= document["afterTheSweep"]["reading"]["determinacySweptOverConfidence"]["99"]
       ["undetermined"])
    ok("T-337-emit every positive verdict is either testable or carries no exceedance",
       document["afterTheSweep"]["reading"]["positiveVerdictsTestable"]
       + document["afterTheSweep"]["reading"]["positiveVerdictsCarryingNoExceedance"]
       == document["afterTheSweep"]["reading"]["positiveVerdicts"])
    ok("T-337-emit the residue is reported with its positives, never as a bare count",
       "residueByFile" in document["afterTheSweep"]["reading"]
       and "residuePositive" in document["afterTheSweep"]["reading"])
    ok("T-337-emit the cheap bound is emitted only where the BEFORE state can be built",
       (document["cheapBound"] is not None) == pinned)
    if pinned:
        ok("T-337-emit the cheap bound's recovered readings each name their donors",
           all(row["donors"] for row in document["cheapBound"]["recovered"]))
        ok("T-337-emit the cheap bound reproduces its own retained script: 5 positives "
           "recoverable with no re-emission, 82 needing one",
           document["cheapBound"]["recoveredPositive"] == 5
           and document["cheapBound"]["needingAReEmission"] == 82)
    after = document["afterTheSweep"]["overTheEighteenFiles"]
    ok("T-337-emit the boundary records are emitted with their one-sided tail, not merely counted",
       after["atExactlyTheTolerance"]["records"] == len(after["atExactlyTheTolerance"]["where"])
       and (after["atExactlyTheTolerance"]["records"] == 0
            or after["atExactlyTheTolerance"]["oneSidedP"] is not None))
    ok("T-337-emit the two counts the deliverables lead with are emitted PER BLOCK with an owner",
       [row["quotedBy"] for row in after["deliverableLeadingCounts"].values()][:2]
       == ["C-0212's 22 of 32", "C-0215's 27 of 48"]
       and all(row["determined"] + row["undetermined"] == row["positive"]
               for row in after["deliverableLeadingCounts"].values()))
    ok("T-337-emit the asymmetry is emitted on BOTH sides, so its ratio needs no other file",
       after["negativeVerdictsTestable"] > after["positiveVerdictsTestable"]
       and isinstance(after["negativeVerdictsUndetermined"], int))
    ok("T-337-emit every declared falsifier is present and carries a verdict",
       [row["id"] for row in document["falsifiers"]]
       == ["F%d" % n for n in range(1, 13)]
       and all(isinstance(row["fired"], bool) for row in document["falsifiers"]))
    ok("T-337-emit F9's monotonicity is asserted against the document's own swept counts",
       not [row for row in document["falsifiers"] if row["id"] == "F9"][0]["fired"])
    ok("T-337-emit the document is JSON-serialisable with no non-finite value",
       "Infinity" not in json.dumps(document) and "NaN" not in json.dumps(document))
    if pinned:
        ok("T-337-emit the BEFORE census is pinned at a resolved 40-character sha",
           len(document["atBaselineRef"]["ref"]) == 40
           and document["atBaselineRef"]["refRequested"] == BASELINE_REF)
        eighteen = document["atBaselineRef"]["overTheEighteenFiles"]
        ok("T-337-emit the BEFORE census reproduces C-0223 section 4b over its OWN eighteen "
           "files, member for member",
           eighteen["flatAtP90Booleans"] == 2678
           and eighteen["recordsCarryingNoExceedance"] == 1238
           and eighteen["positiveVerdicts"] == 106
           and eighteen["positiveVerdictsCarryingNoExceedance"] == 87)
        ok("T-337-emit and the per-file split of the 87 is C-0223's own, file by file",
           eighteen["positiveCarryingNoExceedanceByFile"]
           == {"T-279": 2, "T-284": 2, "T-297": 8, "T-303": 8,
               "T-316": 27, "T-322": 33, "T-323": 7})

    failed = [name for name, passed in checks if not passed]
    for name, passed in checks:
        print("%-4s %s" % ("ok" if passed else "FAIL", name))
    print("# %d self-test(s), %d failed" % (len(checks), len(failed)))
    return not failed


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--emit", action="store_true")
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--out", default=DEFAULT_OUT)
    arguments = parser.parse_args(sys.argv[1:] if argv is None else argv)
    if arguments.self_test:
        return 0 if _self_test() else 1
    if arguments.emit:
        document = build()
        with open(arguments.out, "w") as handle:
            json.dump(document, handle, indent=2)
            handle.write("\n")
        print("written to %s" % arguments.out)
        return 0
    print(json.dumps(build(), indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())
