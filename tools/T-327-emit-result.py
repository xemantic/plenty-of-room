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
# T-327 -- the resolution of the flatness census, emitted as a result file.
#
#     tools/T-327-emit-result.py [--ref <git-ref>] [--out <path>] [--self-test]
#
# WHY IT TAKES A `--ref` AND WHY THE DEFAULT IS PINNED.  This file's subject is the CORPUS, so it
# is a function of a mutable object rather than of committed inputs, and `gpd/README.md`'s
# *"reproducible from it alone"* holds only if the state is named (`CH-0246`).  The default is
# `86b3bbd` -- the commit this task's Formulate and Plan were committed at -- and NOT `HEAD`,
# because on a shared checkout `HEAD` moves while a claim is being drafted and a corpus-subject
# emitter defaulting to it re-bases its own measurement between the draft and the emission.  The
# reading at this pass's tree is emitted beside it as a control, and this task adds no result
# file to `gpd/results/`, so the two must agree.
#
# IT MOVES NO COMMITTED RESULT FILE.  `F10` is a checksum over `gpd/results/` taken before and
# after the run, outside this emitter.
"""Emit the T-327 result file: what a flatness verdict can and cannot resolve."""
import argparse
import collections
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(ROOT, "gpd", "results",
                           "T-327-the-resolution-of-the-flatness-census.json")
DEFAULT_REF = "86b3bbd"


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


resolution = _load("t327_resolution", "T-327-flatness-resolution.py")
rounding = _load("t327_rounding", "T-278-rounding-simulation.py")
header = _load("t327_header", "emission_header.py")

#: The confidence levels the resolution is swept over -- this study's own convergence axis.
CONFIDENCE_LEVELS = (0.90, 0.95, 0.99)

#: What `C-0221` section 5 published, so the reproduction is against its own numbers.
PUBLISHED = {
    "verdictBearingReadings": 1146,
    "tightest": 0.100001020,
    "tightestAt": "T-294/cells/92/nominalCorrectedOverStroke",
    "withinTheConvergenceDepartureAsPublished": 99,
}

#: `C-0180`'s own two recovered cells, addressed by tag and index.  Every NUMBER about them is
#: read out of the committed file below -- `CLAUDE.md`'s *grep a headline number out of the
#: artifact*, met on an emitter rather than on a claim.
DECIDING_CELLS = (
    ("T-279", 69, "the tighter recovered cell: tied, abstract grid, 30 paths, rim-graded 5:1"),
    ("T-279", 109, "the other recovered cell: tied, on the rooting helices, 50 paths"),
)


#: `rev-parse` alone validates the SYNTAX of an object name and not its existence -- it echoes a
#: 40-hex string straight back and exits 0 for a commit that is not in the repository -- so both
#: the resolver and the guard below ask for `--verify <ref>^{commit}`, which requires the object
#: to exist and to be a commit.  Found by the guard's own named test on its first run.
_REV_PARSE = ("rev-parse", "--verify", "--quiet")


def _resolve(ref):
    return subprocess.check_output(
        ["git", "-C", ROOT, *_REV_PARSE, "%s^{commit}" % ref]).decode().strip()


def repository_available(ref=DEFAULT_REF):
    """Whether a `git` repository holding `ref` is reachable from `ROOT`.

    `tools/snapshot.sh` excludes `./.git`, so **inside a snapshot there is no repository at all**
    and every `git`-dependent arm of this file's self-test is unrunnable.  That is a fact about
    the harness, not about the code, and the two honest responses are to degrade visibly or to
    leave the task unwired -- `C-0195`, and agent `V`'s `T-336` took the same decision in the same
    iteration for the same reason.  This file degrades, because only **2** of its 16 arms need the
    ref: the other 14 are properties of a document built from the working tree.
    """
    try:
        subprocess.check_output(["git", "-C", ROOT, *_REV_PARSE, "%s^{commit}" % ref],
                                stderr=subprocess.DEVNULL)
        return True
    except (subprocess.CalledProcessError, OSError):
        return False


def _documents_at(ref):
    """`{tag: document}` from a `git archive` of `ref`, or from the working tree when `ref` is None."""
    if ref is None:
        return resolution.result_documents(ROOT), None
    sha = _resolve(ref)
    tree = tempfile.mkdtemp(prefix="T-327-")
    try:
        archive = subprocess.check_output(
            ["git", "-C", ROOT, "archive", sha, "gpd/results"])
        subprocess.run(["tar", "-x", "-C", tree], input=archive, check=True)
        return resolution.result_documents(tree), sha
    finally:
        shutil.rmtree(tree, ignore_errors=True)


def _reading(documents, confidence=0.95):
    rows = resolution.margin_census_of(documents)
    partition = resolution.leaf_key_partition(rows)
    records = resolution.ensemble_records(documents)
    census = resolution.determinacy_census(records, confidence)
    nominal = resolution.nominal_population(documents)
    unresolvable = resolution.unresolvable_verdicts(documents)
    return {
        "census": {
            "predicate": ("C-0221 section 5's, verbatim: a numeric leaf whose key ends "
                          "OverStroke or contains ishing, in a JSON object that also carries at "
                          "least one boolean, valued in [0.09, 0.11]"),
            "readings": len(rows),
            "tightest": rows[0].value if rows else None,
            "tightestAt": "%s%s" % (rows[0].tag, rows[0].path) if rows else None,
            "distinctLeafKeys": len(partition),
            "leafKeyPartition": partition,
            "leavesNoBooleanIsWrittenOn": sum(
                partition.get(key, 0) for key in resolution.DIAGNOSTIC_LEAVES),
            "diagnosticLeafKeys": list(resolution.DIAGNOSTIC_LEAVES),
            "channelsAsPublished": [
                {"threshold": threshold, "label": label, "count": count}
                for threshold, label, count
                in resolution.channel_counts(rows, resolution.PUBLISHED_CHANNELS)],
            "channelCommensurate": {
                "threshold": resolution.COMMENSURATE_CHANNEL[0],
                "label": resolution.COMMENSURATE_CHANNEL[1],
                "count": sum(1 for row in rows
                             if row.relative <= resolution.COMMENSURATE_CHANNEL[0]),
            },
        },
        "identity": {
            "statement": ("flatAt*P90 is TRUE iff exceedance <= 0.10, exactly: "
                          "orderStatistic(sample, 0.90) is sorted[ceil(0.9 n) - 1], so at "
                          "n = 4 000 the verdict holds iff at most 400 realisations exceed the "
                          "tolerance"),
            "source": "coupling/DropoutRobustPlacement.kt",
            "recordsCarryingBoth": len(records),
            "booleansTested": sum(len(r["booleans"]) for r in records),
            "disagreements": len(resolution.identity_disagreements_of(documents)),
            "realisationCountsStated": sorted(
                set(resolution.realisation_census(documents))),
            "recordsAssumingTheCount": sum(1 for r in records if r["realisationsAssumed"]),
        },
        "populationA": {
            "what": "an ensemble order statistic whose record emits its own exceedance",
            "resolution": ("the exact two-sided Clopper-Pearson interval on that record's own "
                           "exceedance, at that record's own realisation count"),
            "confidence": confidence,
            "positive": census["positive"],
            "positiveUndetermined": census["positiveUndetermined"],
            "negative": census["negative"],
            "negativeUndetermined": census["negativeUndetermined"],
            "undetermined": census["undetermined"],
        },
        "populationB": {
            "what": "a nominal, zero-defect reading -- the axis the T-327 row itself names",
            "resolution": ("its own file's worst DISCRETISATION departure on a NOMINAL quantity; "
                           "a per-(file, quantity) match and deliberately not a per-file maximum"),
            "readingsInRange": len(nominal),
            "noNominalDiscretisationAxisInTheirOwnFile": sum(
                1 for r in nominal if r["determinacy"] == "NO-AXIS"),
            "undetermined": sum(1 for r in nominal if r["determinacy"] == "UNDETERMINED"),
            "determined": sum(1 for r in nominal if r["determinacy"] == "DETERMINED"),
            "byFileWithNoAxis": dict(collections.Counter(
                r["tag"] for r in nominal if r["determinacy"] == "NO-AXIS")),
            "worstNominalDepartureByFile": {
                tag: value for tag, value in sorted(
                    {r["tag"]: r["worstNominalDiscretisationDeparture"] for r in nominal}.items())
                if value is not None},
        },
        "populationC": {
            "what": "a flatAt*P90 verdict whose record emits NO exceedance",
            "resolution": "NONE is derivable from the file -- a recorded refusal, never a withdrawal",
            "booleans": sum(len(r["booleans"]) for r in unresolvable),
            "positive": sum(1 for r in unresolvable for v in r["booleans"].values() if v),
            "byFile": dict(collections.Counter(
                r["tag"] for r in unresolvable for _ in r["booleans"])),
            "positiveByFile": dict(collections.Counter(
                r["tag"] for r in unresolvable for v in r["booleans"].values() if v)),
        },
    }


def _discretisation_step_for(document, p90):
    """The DISCRETISATION convergence step whose coarse value IS this cell's own `p90`.

    Matched on the number rather than on the cell's prose label, so the pairing is a reading of
    the file and not a transcription of it; a cell with no such step returns `None` rather than
    borrowing another cell's, which is the transfer this whole task is about.
    """
    for record in document.get("convergence") or []:
        if not resolution.enters_a_resolution(resolution.axis_kind(record.get("axis") or "")):
            continue
        coarse = record.get("coarseValue")
        fine = record.get("fineValue")
        if not isinstance(coarse, float) or not isinstance(fine, float):
            continue
        if abs(coarse - p90) <= 1e-12 and coarse != fine:
            return coarse, fine
    return None


def _deciding_cells(documents):
    out = []
    n = 4000
    for tag, index, label in DECIDING_CELLS:
        record = documents[tag]["cells"][index]
        path = "/cells/%d" % index
        published = record["p90OverStroke"]
        exceedance = record["exceedance"]
        step = _discretisation_step_for(documents[tag], published)
        if step is None:
            raise SystemExit("no discretisation step matches %s%s" % (tag, path))
        coarse, fine = step
        x = int(round(exceedance * n))
        low, high = resolution.clopper_pearson(x, n, 0.95)
        departure = (fine - coarse) / coarse
        margin_relative = (resolution.TOLERANCE - coarse) / coarse
        sigma = (resolution.TOLERANCE * (1.0 - resolution.TOLERANCE) / n) ** 0.5
        out.append({
            "tag": tag,
            "path": path,
            "label": label,
            "p90OverStroke": published,
            "marginOfTheStroke": resolution.TOLERANCE - coarse,
            "exceedance": exceedance,
            "exceedanceCount": x,
            "exceedanceCountAtTheTolerance": int(round(resolution.TOLERANCE * n)),
            "realisations": n,
            "discretisationDeparture": departure,
            "marginOverTheDiscretisationDeparture": margin_relative / departure,
            "marginOverTheBinomialSigma": abs(exceedance - resolution.TOLERANCE) / sigma,
            "oneSidedBinomialP": resolution.one_sided_binomial_p(
                x, n, resolution.TOLERANCE, True),
            "twoSidedBinomialP": resolution.two_sided_binomial_p(x, n, resolution.TOLERANCE),
            "clopperPearsonLow": low,
            "clopperPearsonHigh": high,
            "determinacy": resolution.determinacy(x, n, 0.95),
        })
    return out


_DOCUMENT_CACHE = {}


def _documents_cached(ref):
    if ref not in _DOCUMENT_CACHE:
        _DOCUMENT_CACHE[ref] = _documents_at(ref)
    return _DOCUMENT_CACHE[ref]


def _cross_axis(documents):
    """Every record resolvable on BOTH axes, with its resolving power on each.

    `margin / departure` and `margin / sigma` are both counts of *how many of its own noises away
    from the tolerance a reading sits*, so their ratio is a density-free comparison of the two
    axes' RESOLVING POWER -- which is the only form in which the two may be compared at all, the
    conversion between a p90 movement and an exceedance movement needing the tail density.
    """
    out = []
    for record in resolution.ensemble_records(documents):
        document = documents[record["tag"]]
        found = []
        resolution._records(document, "", found)
        cell = dict(found)[record["path"]]
        step = None
        for key, value in sorted(cell.items()):
            if not (isinstance(value, float) and "p90" in key.lower()
                    and key.endswith("OverStroke")):
                continue
            step = _discretisation_step_for(document, value)
            if step is not None:
                break
        if step is None:
            continue
        coarse, fine = step
        n = record["realisations"]
        sigma = (resolution.TOLERANCE * (1.0 - resolution.TOLERANCE) / n) ** 0.5
        departure = abs(fine - coarse) / coarse
        margin = abs(resolution.TOLERANCE - coarse) / coarse
        in_departures = margin / departure
        in_sigmas = abs(record["exceedance"] - resolution.TOLERANCE) / sigma
        out.append({
            "tag": record["tag"], "path": record["path"],
            "marginInDiscretisationDepartures": in_departures,
            "marginInBinomialSigmas": in_sigmas,
            "discretisationOverSamplingResolvingPower": in_departures / in_sigmas,
            "samplingIsTheWorseResolvedAxis": in_sigmas < in_departures,
        })
    return out


def _exact_against_normal(documents, confidence=0.95):
    """The exact Clopper-Pearson verdict against the readable normal-approximation one."""
    z = {0.90: 1.644853627, 0.95: 1.959963985, 0.99: 2.575829304}[confidence]
    disagreements = []
    records = resolution.ensemble_records(documents)
    for record in records:
        n = record["realisations"]
        x = int(round(record["exceedance"] * n))
        sigma = (resolution.TOLERANCE * (1.0 - resolution.TOLERANCE) / n) ** 0.5
        normal = ("UNDETERMINED"
                  if abs(record["exceedance"] - resolution.TOLERANCE) <= z * sigma
                  else "DETERMINED")
        if resolution.determinacy(x, n, confidence) != normal:
            disagreements.append({"tag": record["tag"], "path": record["path"],
                                  "exceedanceCount": x})
    return {"recordsCompared": len(records), "disagreements": len(disagreements),
            "where": disagreements}


def build(ref=DEFAULT_REF, out=DEFAULT_OUT):
    documents, sha = _documents_cached(ref)
    working, _ = _documents_cached(None)
    at_ref = _reading(documents)
    at_tree = _reading(working)
    n = 4000
    sigma = (resolution.TOLERANCE * (1.0 - resolution.TOLERANCE) / n) ** 0.5
    axes = resolution.convergence_axes(documents)
    deciding = _deciding_cells(documents)
    orderings = [row for row in resolution.paired_orderings(documents) if row["verdictMoved"]]
    for row in orderings:
        row["signTestBelowDoublePrecisionFloor"] = row["signTestTwoSidedP"] == 0.0

    document = {
        "task": "T-327",
        "claim": "C-0223",
        "leaf": "A8.2",
        "title": ("the resolution of the flatness census -- a flatAtP90 verdict is a binomial "
                  "statement about an exceedance, and its resolution is that proportion's "
                  "sampling error and not the discretisation departure the corpus quotes"),
        "verificationType": ("logical (the identity is derived from the study source and checked "
                             "against every committed record) + in-silico (an exact "
                             "Clopper-Pearson census over the eighteen files, with no solve)"),
        "maturity": ("TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. "
                     "Nothing here re-runs a study or moves a committed reading."),
        "units": ("dimensionless throughout: a dishing is a fraction of that tile's own free "
                  "stroke, an exceedance is a probability, a departure is a relative movement"),
        "conventions": {
            "tolerance": resolution.TOLERANCE,
            "toleranceIs": "T-5b's flatness CONVENTION, not a physical threshold",
            "relativeAxisOfTheCensus": "|v - 0.10| / 0.10",
            "departureAxisOfTheCorpus": "|fine - coarse| / coarse",
            "whyTheyAreCommensurate": ("for a reading in [0.09, 0.11] the two agree to within "
                                       "v / 0.10; a distance in STROKE FRACTIONS is 0.10 times "
                                       "either, and mixing the three is the factor of ten"),
        },
        "baselineRef": sha,
        "baselineRefRequested": ref,
        "parameters": {
            "files": list(resolution.FILES),
            "confidence": 0.95,
            "confidenceLevelsSwept": list(CONFIDENCE_LEVELS),
            "binomialTail": ("the regularised incomplete beta by continued fraction -- no "
                             "third-party package, so the exact interval runs anywhere"),
            "whyThisRef": ("86b3bbd is the commit this task's Formulate and Plan were committed "
                           "at, PINNED rather than defaulted to HEAD: a corpus-subject emitter "
                           "defaulting to HEAD re-bases its own measurement between the draft and "
                           "the emission (CH-0246). This task writes no file into gpd/results/ "
                           "other than its own, so the working-tree control must agree."),
        },
        "sources": [
            "coupling/DropoutRobustPlacement.kt -- summariseDropoutDishing, orderStatistic",
            "gpd/claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md section 5",
            "gpd/claims/C-0180-tied-honeycomb-coupled-regrade.md section 5",
            "gpd/data/T-326-cheap-bound/margin-census.py",
        ],
        "cheapBound": {
            "unitError": {
                "statement": ("C-0180's 4.57e-4 is a departure RELATIVE TO THE VALUE -- its own "
                              "sentence divides a margin of 0.00426 by it and gets 9.3 -- and "
                              "C-0221 section 5's census enters it on a |v - 0.10| / 0.10 axis "
                              "as 4.57e-3"),
                "asPublished": PUBLISHED["withinTheConvergenceDepartureAsPublished"],
                "commensurate": at_ref["census"]["channelCommensurate"]["count"],
                "factor": 10.0,
                "visibleWithNoCodeAtAll": ("as published the census places the flip margin "
                                           "4.2724e-3 at 0.935 of the departure 4.57e-3, where "
                                           "C-0180's own sentence places it at 9.3"),
                "flipMarginOverDepartureAsPublished": 4.2724e-3 / 4.57e-3,
            },
            "leavesNotVerdicts": {
                "statement": ("the predicate's boolean test is on the PARENT record, so a "
                              "diagnostic sitting beside a verdict is counted as one"),
                "readings": at_ref["census"]["readings"],
                "leavesNoBooleanIsWrittenOn": at_ref["census"]["leavesNoBooleanIsWrittenOn"],
            },
        },
        "atBaselineRef": at_ref,
        "atThisPassesTree": at_tree,
        "resolution": {
            "populationA": ("a flatAtP90 verdict may be quoted only when the exact two-sided "
                            "Clopper-Pearson interval on its own exceedance EXCLUDES 0.10; "
                            "inside it the reading is UNDETERMINED and neither flat nor not flat "
                            "may be written on it"),
            "populationB": ("a nominal verdict may be quoted only when its distance from the "
                            "tolerance, relative to its own value, exceeds its own file's worst "
                            "DISCRETISATION departure on a NOMINAL quantity"),
            "populationC": ("no resolution is derivable from the file; the verdict is recorded "
                            "as untestable and T-337 carries the one-field repair"),
            "binomialSigmaAtTheTolerance": sigma,
            "binomialSigmaInRealisations": sigma * n,
            "bandInRealisations": list(resolution.resolution_band(n, 0.95)),
            "bandInExceedance": [resolution.resolution_band(n, 0.95)[0] / n,
                                 resolution.resolution_band(n, 0.95)[1] / n],
            "whyNotADiscretisationDeparture": ("convergence is a property of the quantity, and "
                                               "the quantity a flatAtP90 verdict is a function of "
                                               "is a PROPORTION over a finite ensemble, not a "
                                               "real number the mesh moves"),
        },
        "decidingCells": deciding,
        "exactAgainstNormal": _exact_against_normal(documents),
        "crossAxis": {
            "statement": ("a reading's distance from the tolerance, counted in its OWN noise on "
                          "each axis -- the only density-free way the two may be compared"),
            "records": _cross_axis(documents),
        },
        "ordering": {
            "statement": ("the corpus's own paired blocks are exact sign tests on the SAME "
                          "realisations, so an ORDERING survives where a LEVEL does not"),
            "comparisons": orderings,
        },
        "convergenceAxesRead": {
            "statement": ("every convergence axis of the eighteen files, classified by KIND; "
                          "only a DISCRETISATION axis may enter a resolution"),
            "byKind": dict(collections.Counter(a["kind"] for a in axes)),
            "excludedByKind": {
                "SEARCH": "the search's own variance, not the verdict's",
                "PARAMETER": "a physical bracket, not a departure",
                "PENALTY": "a constraint's value, which is C-0100's binary",
                "UNCLASSIFIED": "refused rather than guessed",
            },
            "total": len(axes),
        },
        "convergence": [],
        "reproductions": [
            {"source": "C-0221 section 5", "quantity": "verdict-bearing readings",
             "published": PUBLISHED["verdictBearingReadings"],
             "here": at_ref["census"]["readings"],
             "reproduced": at_ref["census"]["readings"] == PUBLISHED["verdictBearingReadings"]},
            {"source": "C-0221 section 5", "quantity": "the tightest reading",
             "published": PUBLISHED["tightest"], "here": at_ref["census"]["tightest"],
             "reproduced": at_ref["census"]["tightestAt"] == PUBLISHED["tightestAt"]},
            {"source": "C-0221 section 5",
             "quantity": "readings within the convergence departure AS PUBLISHED",
             "published": PUBLISHED["withinTheConvergenceDepartureAsPublished"],
             "here": at_ref["census"]["channelsAsPublished"][3]["count"],
             "reproduced": (at_ref["census"]["channelsAsPublished"][3]["count"]
                            == PUBLISHED["withinTheConvergenceDepartureAsPublished"])},
            {"source": "C-0180 section 5", "quantity": "the margin over the departure, its '9.3'",
             "published": 9.3,
             "here": deciding[0]["marginOverTheDiscretisationDeparture"],
             "reproduced": abs(
                 deciding[0]["marginOverTheDiscretisationDeparture"] - 9.3) < 0.06},
        ],
        "falsifiers": [],
        "findings": [],
        "validity": [],
        "openQuestions": [],
    }

    for confidence in CONFIDENCE_LEVELS:
        reading = _reading(documents, confidence)["populationA"]
        document["convergence"].append({
            "axis": "the confidence level the resolution is stated at",
            "setting": "%.2f" % confidence,
            "quantity": "undetermined flatAt*P90 booleans",
            "value": reading["positiveUndetermined"] + reading["negativeUndetermined"],
            "positiveUndetermined": reading["positiveUndetermined"],
            "negativeUndetermined": reading["negativeUndetermined"],
        })

    a = at_ref["populationA"]
    b = at_ref["populationB"]
    c = at_ref["populationC"]
    cells = deciding
    document["findings"] = [
        ("The T-327 row's own threshold is a factor of ten out: C-0180's 4.57e-4 is a departure "
         "relative to the value, and read commensurately the count is %d, not %d."
         % (at_ref["census"]["channelCommensurate"]["count"],
            PUBLISHED["withinTheConvergenceDepartureAsPublished"])),
        ("And the 1 146 counts LEAVES: %d of them are medians, worsts, p95s and uncoupled "
         "controls that no boolean of their own record is written on."
         % at_ref["census"]["leavesNoBooleanIsWrittenOn"]),
        ("flatAt*P90 is exactly `exceedance <= 0.10`, at %d of %d committed booleans with %d "
         "disagreeing, so a flatness verdict is a binomial statement and its resolution is that "
         "proportion's sampling error."
         % (at_ref["identity"]["booleansTested"], at_ref["identity"]["booleansTested"],
            at_ref["identity"]["disagreements"])),
        ("At the exact two-sided 95 per cent Clopper-Pearson interval, %d of the %d booleans "
         "reading FLAT are UNDETERMINED and %d of the %d reading NOT FLAT are. The corpus's "
         "positive flatness verdicts are the unresolved ones and its negative ones are robust."
         % (a["positiveUndetermined"], a["positive"],
            a["negativeUndetermined"], a["negative"])),
        ("C-0180's two recovered cells are %d and %d of 4 000 against 400 -- one-sided binomial "
         "p of %.3f and %.3f. The same margins are %.4f and %.4f times the discretisation "
         "departure, which is what 'and it is converged' reports."
         % (cells[0]["exceedanceCount"], cells[1]["exceedanceCount"],
            cells[0]["oneSidedBinomialP"], cells[1]["oneSidedBinomialP"],
            cells[0]["marginOverTheDiscretisationDeparture"],
            cells[1]["marginOverTheDiscretisationDeparture"])),
        ("The ORDERING survives where the LEVEL does not: the corpus's own paired blocks put the "
         "recovering comparisons at %s wins of 4 000, sign tests below the double-precision floor."
         % " and ".join(str(row["wins"]) for row in orderings if row["tag"] == "T-279")),
        ("On the axis the row itself names the answer is nearly EMPTY, which is a result: of %d "
         "nominal readings in range, %d have no nominal discretisation axis in their own file at "
         "all and %d of the remaining %d are undetermined."
         % (b["readingsInRange"], b["noNominalDiscretisationAxisInTheirOwnFile"],
            b["undetermined"], b["readingsInRange"]
            - b["noNominalDiscretisationAxisInTheirOwnFile"])),
        ("And %d of the corpus's %d POSITIVE flatness verdicts cannot be tested at all, because "
         "their record emits no exceedance. That is a recorded refusal with a queue row (T-337), "
         "never a withdrawal."
         % (c["positive"], c["positive"] + a["positive"])),
    ]
    document["validity"] = [
        ("The binomial error priced here is the error of the ensemble at a GIVEN staple "
         "incorporation. C-0087's incorporation is itself a measurement with its own uncertainty, "
         "and that is a strictly larger separate term. It is named, not priced."),
        ("The instrument applies to a verdict read as a claim about the POPULATION the 4 000 "
         "draws estimate. A verdict read as a claim about this one seeded sample has no sampling "
         "error at all -- and no design content either, since the dropout ensemble is a model of "
         "fabrication and not a property of seed 197197."),
        ("Population C is refused, not estimated: 87 positive verdicts, including C-0212's "
         "22 of 32 and C-0215's 27 of 48, are untestable from their own files."),
        ("The census is over the eighteen files' COMMITTED state at the stated baselineRef and is "
         "dated by it. A verdict written outside its own record is invisible to it."),
        ("Population B's match is per (file, quantity) and not per cell. T-263's single nominal "
         "departure is read at the recommended cell and applied to that file's 14 readings; that "
         "is stated rather than hidden, and it is the loosest match this file makes."),
    ]
    document["openQuestions"] = [
        ("T-337 -- emit an exceedance beside every p90 verdict. It is one field the studies "
         "already compute, and it converts 87 untestable positive verdicts into testable ones."),
        ("T-338 -- re-read the marginal verdicts of the two deliverables at the stated "
         "resolution, and carry UNDETERMINED as a third state beside flat and not flat."),
        ("Whether an ORDERING should be the corpus's primary flatness statement, given that it "
         "resolves where a level does not."),
    ]
    document["falsifiers"] = [
        {"id": "F1", "statement": "the identity disagrees at any committed record",
         "fired": at_ref["identity"]["disagreements"] != 0},
        {"id": "F2", "statement": ("the commensurate recount is not 2, or the published 99 is not "
                                   "reproduced"),
         "fired": not (at_ref["census"]["channelCommensurate"]["count"] == 2
                       and at_ref["census"]["channelsAsPublished"][3]["count"] == 99)},
        {"id": "F3", "statement": "some record's realisation count is not 4 000",
         "fired": at_ref["identity"]["realisationCountsStated"] != [4000]},
        {"id": "F5", "statement": ("no positive verdict is UNDETERMINED -- then the answer is the "
                                   "recorded refusal"),
         "fired": a["positiveUndetermined"] == 0},
        {"id": "F6", "statement": "the undetermined count is not non-decreasing in the confidence",
         "fired": any(document["convergence"][i]["value"] > document["convergence"][i + 1]["value"]
                      for i in range(len(document["convergence"]) - 1))},
        {"id": "F8", "statement": "a paired sign test is also undetermined at a recovering pair",
         "fired": any(row["signTestTwoSidedP"] > 0.05 for row in orderings
                      if row["tag"] == "T-279")},
        {"id": "F4", "statement": ("the exact Clopper-Pearson verdict and the normal "
                                   "approximation disagree at any record"),
         "fired": _exact_against_normal(documents)["disagreements"] != 0},
        {"id": "F7", "statement": ("the sampling axis is the BETTER resolved one at some record "
                                   "where both exist -- then the general statement is false"),
         "fired": any(not row["samplingIsTheWorseResolvedAxis"] for row in _cross_axis(documents))},
        {"id": "F13", "statement": "the leaf-key partition does not sum to the census total",
         "fired": sum(at_ref["census"]["leafKeyPartition"].values())
         != at_ref["census"]["readings"]},
        {"id": "F14", "statement": ("more than a handful of NEGATIVE verdicts are undetermined -- "
                                    "then the asymmetry is not the finding"),
         "fired": a["negativeUndetermined"] > 5},
    ]
    document["baselineControl"] = {
        "statement": ("the reading at the pinned ref beside the reading at this pass's tree. "
                      "T-327 itself writes no file into gpd/results/ other than its own, so "
                      "these agreed when this task ran; they DISAGREE as soon as the corpus "
                      "legitimately moves, which is the record working and not a defect "
                      "(CH-0294). The pinned reading is the answer; this is a staleness "
                      "DETECTOR and is reported, never gated."),
        "agree": json.dumps(at_ref, sort_keys=True) == json.dumps(at_tree, sort_keys=True),
        "whyThisIsNotAGate": ("an arm asserting that a working tree agrees with a pinned corpus "
                              "must fire the first time the corpus legitimately moves, so it "
                              "cannot be a pass/fail arm. C-0083's a gate that cannot come "
                              "clean is not a gate, read on a control rather than on a rule."),
    }

    document = rounding.walk(document, rounding.RESULT_SIGNIFICANT_DIGITS,
                             rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)
    return header.with_emission_header(document, "honeycomb", regime=[])


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    # `tools/snapshot.sh` excludes `./.git`, so a wired self-test that resolves a ref cannot run
    # inside a snapshot.  Degrade VISIBLY rather than pass silently -- a check that quietly
    # succeeds when it could not run is the failure direction this corpus most dislikes -- and
    # write the skip to **stderr**, because `--self-test > /dev/null` swallows stdout (`C-0195`).
    pinned = repository_available()
    document = build() if pinned else build(ref=None)
    if not pinned:
        sys.stderr.write(
            "# SKIPPED 2 of 16 arms: no git repository holding %s under %s, so the pinned\n"
            "#   reading cannot be built and the document is built from the WORKING TREE\n"
            "#   instead.  The 14 arms that do not need the ref all ran.\n"
            % (DEFAULT_REF, ROOT))
    ok("T-327-emit the emission header is first and states the honeycomb and no regime",
       list(document)[0] == "emission"
       and document["emission"] == {"lattice": "honeycomb", "regime": []})
    if pinned:
        ok("T-327-emit the resolved sha is recorded beside the ref that was asked for",
           len(document["baselineRef"]) == 40
           and document["baselineRefRequested"] == DEFAULT_REF)
        # `CH-0294`. This WAS `ok(... document["baselineControl"]["agree"])`, and it was a
        # staleness detector wearing a pass/fail hat: `T-337` re-emitted seven of the eighteen
        # files this census reads, and the arm went red at the first legitimate movement of the
        # corpus. What is asserted now is what the control is FOR -- that both readings were
        # built and the comparison was taken -- and the verdict is printed for a reader
        # instead of failing a build.  The reproducibility statement the arm was doing duty
        # for is separately and better made below: two builds at the same ref are identical.
        ok("T-327-emit the pinned reading and the working-tree control are both taken",
           isinstance(document["baselineControl"]["agree"], bool)
           and "whyThisIsNotAGate" in document["baselineControl"])
        if not document["baselineControl"]["agree"]:
            sys.stderr.write(
                "# NOTE: the working-tree reading DIFFERS from the pinned reading at %s.\n"
                "#   That is a corpus that has legitimately moved since this task ran, not a\n"
                "#   defect: the answer is the PINNED reading (CH-0246, CH-0294).\n"
                % DEFAULT_REF)
    # The guard itself, in BOTH states -- these two run whether or not a repository is present,
    # so the degradation is asserted rather than merely observed the day it fires.
    ok("T-327-emit repository_available refuses a ref that does not EXIST, not merely one that "
       "is malformed -- bare `git rev-parse` echoes a 40-hex string back and exits 0",
       not repository_available("0000000000000000000000000000000000000000")
       and not repository_available("no-such-ref-here"))
    ok("T-327-emit a degraded build names NO baseline ref, so it can never be read as a pinned one",
       build(ref=None)["baselineRef"] is None
       and build(ref=None)["baselineRefRequested"] is None)
    ok("T-327-emit the identity is checked and holds",
       document["atBaselineRef"]["identity"]["disagreements"] == 0)
    ok("T-327-emit C-0221 section 5's published channel is reproduced before it is corrected",
       document["atBaselineRef"]["census"]["channelsAsPublished"][3]["count"] == 99)
    ok("T-327-emit the commensurate recount is 2",
       document["atBaselineRef"]["census"]["channelCommensurate"]["count"] == 2)
    cells = document["decidingCells"]
    ok("T-327-emit the deciding cells' numbers are READ from the committed file, not transcribed",
       cells[0]["p90OverStroke"] == 0.0995744767 and cells[0]["exceedance"] == 0.098
       and cells[1]["p90OverStroke"] == 0.0998791032 and cells[1]["exceedance"] == 0.0995)
    ok("T-327-emit each deciding cell's discretisation step is its OWN, matched on its own p90",
       abs(cells[0]["discretisationDeparture"] - 4.570659e-4) < 1e-9
       and abs(cells[1]["discretisationDeparture"] - 1.011413e-4) < 1e-9)
    # `CH-0294`. This arm read `recordsCompared == 1184` -- the population as it stood at
    # `86b3bbd`, asserted as an invariant. `T-337` carried an exceedance into 491 further
    # records and the arm went red while its FINDING got stronger: `0` disagreements now holds
    # at 1 931 of 1 931. What is asserted is therefore the finding and the DIRECTION the
    # population may move in: it may only grow, because a record that LOST its exceedance would
    # be a real defect and must still fail here. `CH-0182`'s *a census is dated by its premise
    # set*, met on a named test.
    ok("T-327-emit the exact verdict and the normal approximation agree at every record, over a "
       "population that may grow and may not shrink",
       document["exactAgainstNormal"]["disagreements"] == 0
       and document["exactAgainstNormal"]["recordsCompared"] >= 1184)
    sys.stderr.write("# T-327-emit compared %d record(s) against the 1184 this arm was written "
                     "at.\n" % document["exactAgainstNormal"]["recordsCompared"])
    ok("T-327-emit the sampling axis is the worse-resolved one at every record carrying both",
       document["crossAxis"]["records"]
       and all(row["samplingIsTheWorseResolvedAxis"]
               for row in document["crossAxis"]["records"]))
    ok("T-327-emit both deciding cells are UNDETERMINED",
       all(cell["determinacy"] == "UNDETERMINED" for cell in cells))
    ok("T-327-emit every reproduction closes",
       all(row["reproduced"] for row in document["reproductions"]))
    ok("T-327-emit F5 did not fire -- the answer is a resolution and not a refusal",
       not [f for f in document["falsifiers"] if f["id"] == "F5"][0]["fired"])
    ok("T-327-emit the undetermined count is non-decreasing in the confidence level",
       [row["value"] for row in document["convergence"]]
       == sorted(row["value"] for row in document["convergence"]))
    ok("T-327-emit the document is JSON-serialisable with no non-finite value",
       json.dumps(document, allow_nan=False))
    ok("T-327-emit two builds at the same ref are byte-identical",
       json.dumps(build() if pinned else build(ref=None), indent=1)
       == json.dumps(document, indent=1))

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default=DEFAULT_REF,
                        help="the git ref whose gpd/results to measure (default %s, pinned; see "
                             "parameters.whyThisRef)" % DEFAULT_REF)
    parser.add_argument("--out", default=DEFAULT_OUT, help="where to write the result file")
    parser.add_argument("--self-test", dest="self_test", action="store_true")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    if not repository_available(arguments.ref):
        sys.stderr.write(
            "refusing to emit: no git repository holding %s under %s.  A corpus-subject result "
            "file must name the state it measured (CH-0246), so this path does NOT degrade the "
            "way --self-test does.\n" % (arguments.ref, ROOT))
        return 2
    document = build(arguments.ref, arguments.out)
    with open(arguments.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=1, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(arguments.out, ROOT))
    reading = document["atBaselineRef"]["populationA"]
    print("# %d of %d POSITIVE flatness verdicts UNDETERMINED at 95%%, %d of %d negative"
          % (reading["positiveUndetermined"], reading["positive"],
             reading["negativeUndetermined"], reading["negative"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
