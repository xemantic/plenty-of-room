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
# T-9 -- the crossover hinge constant `k_theta` from oxDNA, and what the same run could NOT settle.
#
#     tools/T-9-emit-result.py [--build build-oxdna] [--check]
#
# The run itself lives in tools/oxdna/ (README.md carries the build, the environment and the five
# traps; RESULTS.md carries the reading and its validity range).  This script does not simulate
# anything: it reads the run's own derived JSON and emits gpd/results/T-9-crossover-hinge-constant.json
# so that the corpus's checkers can reach the result at all.  RESULTS.md is a document; no gate in
# this repository reads it.
#
# TWO KINDS OF NUMBER, AND THE FILE KEEPS THEM APART.
#
#   * DERIVED    recomputed here from build-oxdna/*.json, so a reader can check the arithmetic and
#                a re-run of this script reproduces it byte for byte.
#   * RECORDED   measured by the run and NOT recomputable from the retained data, because the raw
#                trajectories (649 MB of .dat) were pruned after the analysis.  Every such field is
#                marked, and its provenance is tools/oxdna/RESULTS.md plus the run logs retained
#                beside the JSON.
#
# The distinction is the point.  `CLAUDE.md`'s standing rule is that a defect invisible in the
# answer is invisible to every check written on the answer; a file that mixed a recomputable
# reading with a transcribed one would let a transcription error hide behind an arithmetic check.
import argparse
import json
import math
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# --- the corpus side of the comparison, from the sources the claim cites --------------------------

CORPUS_K_THETA = 13.529411773382902      # T-10: 2 alpha B / (100 a); the 1/100 is CanDo's NICK factor
CORPUS_D_PERPENDICULAR = 3.34504758      # k_theta d / p
CORPUS_D_PARALLEL = 85.5018587           # EI / d, CanDo EI = 230 pN nm^2
CORPUS_D_K = 42.7509294                  # GJ / (4 d), CanDo GJ = 460 pN nm^2
CORPUS_ANISOTROPY = CORPUS_D_PARALLEL / CORPUS_D_PERPENDICULAR
CANDO_EI = 230.0                         # pN nm^2
INTERHELICAL_SAXS = 2.69                 # Fischer et al. 2016, single-layer lattice constant
BAI_AT_CROSSOVER = 1.85                  # Bai et al. 2012, cryo-EM, the sawtooth minimum
BAI_MIDWAY = 3.60                        # Bai et al. 2012, cryo-EM, the sawtooth maximum
CROSSOVER_SPACING_BP = 32                # p, the per-INTERFACE spacing (CLAUDE.md)
RISE_NM = 0.34

BOLTZMANN_PNNM = 4.141947                # k_B T at 300 K, the corpus's locked value

# --- rounding, matching structure/ResultRounding.kt ----------------------------------------------

RESULT_SIGNIFICANT_DIGITS = 9
RESULT_ABSOLUTE_FLOOR = 1e-9
DEPARTURE_SIGNIFICANT_DIGITS = 2
DEPARTURE_SPELLINGS = {"departure", "relativeDeparture", "departureFromFinest", "relativeError"}


def round_significant(value, digits):
    if not isinstance(value, float) or not math.isfinite(value) or value == 0.0:
        return value
    if abs(value) < RESULT_ABSOLUTE_FLOOR:
        return 0.0
    exponent = math.floor(math.log10(abs(value)))
    factor = 10.0 ** (digits - 1 - exponent)
    return round(value * factor) / factor


def rounded(node, key=None):
    """Round every float in `node`, at two digits under a departure key and nine elsewhere."""
    if isinstance(node, dict):
        return {k: rounded(v, k) for k, v in node.items()}
    if isinstance(node, list):
        return [rounded(v, key) for v in node]
    if isinstance(node, bool) or isinstance(node, int) or isinstance(node, str) or node is None:
        return node
    digits = DEPARTURE_SIGNIFICANT_DIGITS if key in DEPARTURE_SPELLINGS else RESULT_SIGNIFICANT_DIGITS
    return round_significant(float(node), digits)


def load(build, *parts):
    path = os.path.join(ROOT, build, *parts)
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--build", default="build-oxdna",
                        help="the run's working directory, pruned to its derived artifacts")
    parser.add_argument("--check", action="store_true",
                        help="exit 1 if any falsifier fires or a reproduction misses")
    args = parser.parse_args()

    design = load(args.build, "gen1_tile-summary.json")
    tile = load(args.build, "run", "tile.json")
    roll = load(args.build, "run", "roll.json")
    duplex = load(args.build, "duplex-lp.json")

    # --- the hinge bracket, both ends derived from ONE measured pair of variances ----------------
    #
    # The roll is the coordinate `k_theta` is written on -- the dihedral of two adjacent duplexes
    # about the interface line -- so equipartition on its variance is the whole measurement.  The
    # two readings differ in ONE assumption and nothing else:
    #
    #   upper   the hinge is the only thing holding that dihedral            k = kT / Var(at)
    #   lower   the hinge is what a crossover ADDS over mid-span    k = kT/Var(at) - kT/Var(off)
    #
    # The truth is between them because the mid-span roll is itself partly held by the crossovers
    # 8 bp away, so the lower reading understates what the hinge alone supplies.
    variance_at = math.radians(roll["rollSdAtCrossoverDeg"]) ** 2
    variance_off = math.radians(roll["rollSdOffCrossoverDeg"]) ** 2
    upper = BOLTZMANN_PNNM / variance_at
    lower = BOLTZMANN_PNNM / variance_at - BOLTZMANN_PNNM / variance_off

    def d_perpendicular(k_theta):
        return k_theta * INTERHELICAL_SAXS / (CROSSOVER_SPACING_BP * RISE_NM)

    inside = lower < CORPUS_K_THETA < upper

    hinge = {
        "coordinate": "the interduplex ROLL, the dihedral of two adjacent duplexes about the "
                      "interface line, which is the coordinate k_theta is written on",
        "frames": roll["frames"],
        "interfaces": roll["interfaces"],
        "crossoverSamples": roll["crossoverSamples"],
        "rollSdAtCrossoverDeg": roll["rollSdAtCrossoverDeg"],
        "rollSdOffCrossoverDeg": roll["rollSdOffCrossoverDeg"],
        "meanRollAtCrossoverDeg": roll["meanRollAtCrossoverDeg"],
        "upperReadingPnNmPerRad": upper,
        "upperAssumption": "the hinge is the ONLY constraint on the dihedral at a crossover",
        "lowerReadingPnNmPerRad": lower,
        "lowerAssumption": "the hinge is what a crossover ADDS over the mid-span roll",
        "bracketWidth": upper / lower,
        "corpusValuePnNmPerRad": CORPUS_K_THETA,
        "corpusValueProvenance": "T-10's 2 alpha B / (100 a); the 1/100 is borrowed from CanDo's "
                                 "NICK softening and has never been measured (TASKS.md, T-9)",
        "corpusValueInsideBracket": inside,
        "perReplicaStability": "better than 10 % across the five replicas, at both readings "
                               "(RECORDED, tools/oxdna/RESULTS.md)",
    }

    derived_rigidity = {
        "note": "D_perpendicular = k_theta d / p with p the PER-INTERFACE spacing of 32 bp, so the "
                "bracket on the hinge maps straight onto the two quantities that carry it",
        "corpusDPerpendicular": CORPUS_D_PERPENDICULAR,
        "dPerpendicularAtLowerReading": d_perpendicular(lower),
        "dPerpendicularAtUpperReading": d_perpendicular(upper),
        "corpusAnisotropy": CORPUS_ANISOTROPY,
        "anisotropyAtUpperReading": CORPUS_D_PARALLEL / d_perpendicular(upper),
        "anisotropyAtLowerReading": CORPUS_D_PARALLEL / d_perpendicular(lower),
        "corpusDPerpendicularInsideBracket":
            d_perpendicular(lower) < CORPUS_D_PERPENDICULAR < d_perpendicular(upper),
    }

    # --- the sawtooth, which is a reproduction of a measurement this corpus had only cited -------
    lattice = tile["lattice"]
    sawtooth = {
        "atCrossoverColumnNm": lattice["interhelicalAtCrossoverNm"],
        "midwayNm": lattice["interhelicalMidwayNm"],
        "meanNm": lattice["interhelicalMeanNm"],
        "sdNm": lattice["interhelicalSdNm"],
        "baiAtCrossoverNm": BAI_AT_CROSSOVER,
        "baiMidwayNm": BAI_MIDWAY,
        "midwayRatioToBai": lattice["interhelicalMidwayNm"] / BAI_MIDWAY,
        "atCrossoverRatioToBai": lattice["interhelicalAtCrossoverNm"] / BAI_AT_CROSSOVER,
        "meanRatioToSaxs": lattice["interhelicalMeanNm"] / INTERHELICAL_SAXS,
        "whyItIsOnlyVisiblePerParity": "each interface must be read at the columns of ITS OWN "
                                       "parity; averaging over all seven columns dilutes the "
                                       "modulation away to a flat 2.92 / 2.90 nm (RECORDED)",
        "basePairsIntact": lattice["basePairsIntact"],
        "basePairsIntactInterior": lattice["basePairsIntactInterior"],
        "basePairsIntactEdge": lattice["basePairsIntactEdge"],
    }

    # --- what the run could NOT settle, quoted with the diagnostics that say so ------------------
    convergence = tile["convergence"]
    per_replica = convergence["perReplica"]

    def spread(key):
        values = [replica[key] for replica in per_replica]
        return max(values) / min(values)

    not_resolved = {
        "whatWasAttempted": "the three orthotropic plate rigidities by equipartition of the "
                            "plate's three constant-curvature Legendre modes -- the estimator that "
                            "matches the plate's own definition and recovers known rigidities to "
                            "1.7 % on synthetic ensembles",
        "pooledDParallel": tile["rigidities"]["D_parallel"],
        "pooledDPerpendicular": tile["rigidities"]["D_perpendicular"],
        "pooledDK": tile["rigidities"]["D_k"],
        "autocorrelationTimeFrames": convergence["autocorrelationTimeFrames"],
        "effectiveSamplesPerMode": convergence["effectiveSamples"],
        "framesPerReplica": tile["replicas"][0],
        "halfSplitRatio": convergence["halfSplitRatio"],
        "perReplicaSpreadDParallel": spread("D_parallel"),
        "perReplicaSpreadDPerpendicular": spread("D_perpendicular"),
        "perReplicaSpreadDK": spread("D_k"),
        "verdict": "NOT CONVERGED, and the direction is the one that matters: those three modes "
                   "are the tile's SOFTEST and therefore its slowest, and an under-sampled soft "
                   "mode reads as STIFFER than it is -- which is the direction that would have "
                   "flattered a large D_perpendicular. The pooled reading is therefore NOT quoted "
                   "as evidence against the corpus's 3.34504758.",
        "costToSettle": "100 independent samples per mode needs 12-55 h per replica on this "
                        "machine at the measured correlation times (RECORDED)",
        "spectralFitRejected": "a maximum-likelihood fit over the whole Legendre bending spectrum "
                               "is strongly degree-dependent -- D_parallel running 33.7 / 24.3 / "
                               "11.0 at degree 2 / 3 / 4 -- so it is retained as a diagnostic and "
                               "not used for a value (RECORDED)",
    }

    # --- the duplex, which is a control rather than a result -------------------------------------
    duplex_block = {
        "bendingRigidityPnNm2": duplex["bendingRigidityPnNm2"],
        "persistenceLengthNm": duplex["persistenceLengthNm"],
        "candoEiPnNm2": CANDO_EI,
        "ratioToCando": duplex["bendingRigidityPnNm2"] / CANDO_EI,
        "fitWindowSweepNm": duplex["persistenceLengthByWindowNm"],
        "verdict": "the NICKED duplex the tile is actually made of, fitted over 10 nm, comes out "
                   "0.92 of CanDo's continuous-duplex EI. The fit is NOT converged in its window "
                   "-- L_p runs 51 -> 247 nm as the window opens 10 -> 60 nm -- so this is a "
                   "consistency check on the constitutive input, not a measurement of it.",
    }

    twist = {
        "meanTwistDegreesOverTile": tile["meanShape"]["meanTwistDegreesOverTile"],
        "perReplicaDegrees": [12.1, 19.2, 27.6, 39.3, 28.3],
        "perReplicaProvenance": "RECORDED -- tools/oxdna/RESULTS.md; s.e. 4.6 deg, all five "
                               "replicas agreeing in SIGN",
        "corpusBoundaryLayerPredictionDeg": [17.0, 25.0],
        "designBasesPerTurn": 10.67,
        "verdict": "the magnitudes agree and these are DIFFERENT FUNCTIONALS of the same strain -- "
                   "the corpus's is a register angle AT A ROW END, this is the tile's mid-surface "
                   "twist ACROSS ITS WIDTH. Read as a consistency check of sign and order, never "
                   "as an identity.",
        "theFalsifierThatWasNotRun": "build C-0133's twist-corrected 110 bp raster and the twist "
                                     "should collapse. Until that control exists a global twist of "
                                     "this size is CONSISTENT with the register mismatch and is "
                                     "not attributed to it.",
    }

    # --- reproductions: the generator asserting the corpus's own lattice counts ------------------
    reproductions = [
        {"quantity": "duplexes in the raster", "corpus": 15, "run": design["helices"],
         "departure": 0.0, "source": "C-0086"},
        {"quantity": "buildable seamless row, bp", "corpus": 112, "run": design["rowBasePairs"],
         "departure": 0.0, "source": "C-0086 -- the only buildable seamless width near 40 nm"},
        {"quantity": "crossover phase", "corpus": 8, "run": design["crossoverPhase"],
         "departure": 0.0, "source": "C-0063 -- one of the two centro-symmetric phases"},
        {"quantity": "crossover columns", "corpus": 7, "run": len(design["crossoverColumns"]),
         "departure": 0.0, "source": "C-0015"},
        {"quantity": "crossovers built", "corpus": 49, "run": design["crossoversBuilt"],
         "departure": 0.0, "source": "CLAUDE.md -- the 4/3 parity split of a seven-column sheet"},
        {"quantity": "tile edge along the helices, nm", "corpus": 38.08, "run": design["edgeXNm"],
         "departure": abs(design["edgeXNm"] - 38.08) / 38.08, "source": "C-0086"},
        {"quantity": "hinge stiffness from the roll variance, pN nm/rad",
         "corpus": roll["hingeStiffnessFromCrossoverRoll"], "run": upper,
         "departure": abs(upper - roll["hingeStiffnessFromCrossoverRoll"])
                      / roll["hingeStiffnessFromCrossoverRoll"],
         "source": "this script against the run's own analysis -- pins k_B T and the equipartition"},
    ]

    falsifiers = {
        "F1": {
            "statement": "the corpus's fitted k_theta lies OUTSIDE the measured bracket, so the "
                         "1/100 nick borrowing is refuted",
            "fired": not inside,
            "reading": "bracket {:.4g} - {:.4g}, corpus {:.4g}".format(lower, upper, CORPUS_K_THETA),
        },
        "F2": {
            "statement": "the design generator does not reproduce the corpus's lattice counts, so "
                         "the object simulated is not the object the corpus is about",
            "fired": any(r["departure"] > 1e-12 for r in reproductions[:6]),
            "reading": "15 duplexes, 112 bp, phase 8, 7 columns, 49 crossovers, 38.08 nm",
        },
        "F3": {
            "statement": "the three plate rigidities ARE converged, so the run settles them and the "
                         "non-resolution is a reporting choice rather than a sampling limit",
            "fired": min(convergence["effectiveSamples"]) > 100.0,
            "reading": "{:.0f}-{:.0f} independent samples per mode against the 100 wanted".format(
                min(convergence["effectiveSamples"]), max(convergence["effectiveSamples"])),
        },
        "F4": {
            "statement": "the structure did not hold together, so no equilibrium average is a "
                         "property of the designed lattice",
            "fired": lattice["basePairsIntact"] < 0.95,
            "reading": "{:.4f} of designed base pairs intact, {:.4f} in the interior".format(
                lattice["basePairsIntact"], lattice["basePairsIntactInterior"]),
        },
        "F5": {
            "statement": "the crossover sawtooth is absent, so the relaxed structure is not the "
                         "weave the corpus's plan models are written on",
            "fired": not (lattice["interhelicalAtCrossoverNm"]
                          < lattice["interhelicalMeanNm"]
                          < lattice["interhelicalMidwayNm"]),
            "reading": "{:.3f} at a column, {:.3f} mean, {:.3f} midway".format(
                lattice["interhelicalAtCrossoverNm"], lattice["interhelicalMeanNm"],
                lattice["interhelicalMidwayNm"]),
        },
    }

    result = {
        "task": "T-9",
        "leaf": "A1.2, with A8.2",
        "title": "The crossover hinge constant k_theta from oxDNA: a 5.1x bracket that CONTAINS "
                 "the corpus's fitted value, a reproduced interhelical sawtooth, and three plate "
                 "rigidities this box cannot settle",
        "verificationType": "in-silico (oxDNA2 coarse-grained MD, 5 replicas) + logical (the "
                            "design generator asserts the corpus's own lattice counts)",
        "maturity": "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated -- a "
                    "coarse-grained model in a substituted buffer, not a folded object.",
        "units": {
            "length": "nm", "angle": "degrees", "hingeStiffness": "pN*nm/rad",
            "rigidity": "pN*nm", "bendingRigidity": "pN*nm^2", "energy": "pN*nm",
        },
        "conventions": {
            "kBT": "4.141947 pN*nm at 300 K, the corpus's locked value",
            "crossoverSpacing": "p = 32 bp, the PER-INTERFACE spacing -- crossovers recur every "
                                "16 bp along a helix but alternate between its two neighbours",
            "roll": "the signed dihedral of two adjacent duplexes about the interface line, "
                    "positive by the right-hand rule along +x",
            "derivedVersusRecorded": "DERIVED fields are recomputed by this script from "
                                     "build-oxdna/*.json and are checkable; RECORDED fields were "
                                     "measured by the run and are NOT recomputable, because the "
                                     "raw trajectories were pruned. Every RECORDED field says so.",
        },
        "parameters": {
            "model": "oxDNA2 (DNA2), sequence-averaged",
            "saltConcentration": "0.5 M monovalent -- the standard origami proxy",
            "temperature": "27 C",
            "thermostat": "John (Langevin, no hydrodynamics)",
            "timestep": 0.005,
            "productionStepsPerReplica": 300000,
            "replicas": len(per_replica),
            "framesAnalysed": tile["frames"],
            "nucleotides": design["totalNucleotides"],
            "relaxation": "temperature-free steepest descent with a harmonic backbone "
                          "(sim_type = min, interaction_type = DNA_relax), then a cold "
                          "capped-force stage, heating and equilibration",
        },
        "sources": [
            "tools/oxdna/README.md -- build, environment, and the five traps this run hit",
            "tools/oxdna/RESULTS.md -- the reading and its validity range",
            "Bai, Martin, Scheres & Dietz, PNAS 109:20012 (2012) -- the cryo-EM sawtooth",
            "Fischer et al. (2016) -- the SAXS single-layer lattice constant",
            "Kim, Kilchherr, Dietz & Bathe, NAR 40:2862 (2012) -- CanDo's duplex constants",
            "Snodin et al., NAR 47:1585 (2019) -- the one published measurement of this coordinate",
        ],
        "citedInputs": [
            "C-0086 (the buildable seamless width)", "C-0063 (the centro-symmetric phase)",
            "C-0015 (the column count)", "C-0006 / C-0009 (what k_theta carries)",
            "T-10 (the three closed-form plate rigidities)",
        ],
        "structure": design,
        "hinge": hinge,
        "derivedRigidity": derived_rigidity,
        "sawtooth": sawtooth,
        "notResolved": not_resolved,
        "duplexControl": duplex_block,
        "globalTwist": twist,
        "reproductions": reproductions,
        "verdict": {
            "kThetaIsBracketedAndTheCorpusValueSurvives": inside,
            "bracketWidth": upper / lower,
            "whatThisSettles": "T-9's FIRST deliverable only. The hinge constant is bracketed to "
                               "about a factor of 2.5 either way and the corpus's fitted value "
                               "sits inside it, so every result that rests on k_theta stands -- "
                               "on a measurement now, not on a borrowed nick factor.",
            "whatThisDoesNotSettle": "T-9's other two deliverables. The crossover's VERTICAL "
                                     "compliance (C-0009 models it as a rigid constraint, and "
                                     "C-0100 shows the only two physical states of a constraint "
                                     "are present and absent) and the in-plane SHEAR k_s "
                                     "(C-0020's single undetermined input, which C-0028 shows "
                                     "moves a buckling verdict) are untouched by this run.",
            "theBracketIsNotSharpened": "its width is set by an ASSUMPTION -- which of two "
                                        "readings of the same roll data is the hinge -- and not by "
                                        "the sampling, so more compute does not narrow it.",
        },
        "falsifiers": {name: entry["statement"] for name, entry in falsifiers.items()},
        "falsifiersFired": {
            name: ("FIRED -- " + entry["reading"]) if entry["fired"]
            else ("did not fire -- " + entry["reading"])
            for name, entry in falsifiers.items()
        },
        "findings": {
            "theCorpusValueSurvivesOnAMeasurement":
                "k_theta = {:.4g} sits inside the measured {:.4g} - {:.4g}, so T-9's standing "
                "assumption is UPHELD rather than overturned -- and the two quantities that carry "
                "it, D_perpendicular and the 25.6x anisotropy, are inside their own brackets with "
                "it.".format(CORPUS_K_THETA, lower, upper),
            "theSawtoothIsREPRODUCED":
                "this repository has cited Bai et al.'s 1.85 -> 3.60 nm deterministic modulation "
                "since C-0076 and had never checked it against a simulation of its OWN tile. The "
                "far end matches to three digits ({:.3f} against 3.60), and the signal is only "
                "visible if each interface is read at the columns of its own "
                "parity.".format(lattice["interhelicalMidwayNm"]),
            "aNonResultIsQuotedAsOne":
                "the three plate rigidities are NOT converged -- {:.0f}-{:.0f} independent samples "
                "per mode, half-split ratios {:.2f}/{:.2f}/{:.2f}, per-replica spread up to "
                "{:.1f}x -- and the direction of the bias is the flattering one, so the pooled "
                "reading is reported and explicitly NOT used.".format(
                    min(convergence["effectiveSamples"]), max(convergence["effectiveSamples"]),
                    convergence["halfSplitRatio"]["D_parallel"],
                    convergence["halfSplitRatio"]["D_perpendicular"],
                    convergence["halfSplitRatio"]["D_k"],
                    max(spread("D_parallel"), spread("D_perpendicular"), spread("D_k"))),
            "theBufferIsASubstitution":
                "oxDNA2 carries no divalent ions and its Debye-Huckel term is parameterised at or "
                "above 0.1 M MONOVALENT salt; SS3's buffer is 2 mM MgCl2. The run uses the field's "
                "standard 0.5 M monovalent proxy. This is the largest single reason a rigidity "
                "here need not equal one measured in the device's own buffer, and it is a property "
                "of the MODEL rather than of this run.",
        },
        "validity": [
            "THE BUFFER IS NOT SS3's. 0.5 M monovalent against 2 mM MgCl2; a stated substitution.",
            "THE TILE IS FREE -- no PEG layer, no electrode. The three rigidities are properties "
            "of the sheet and are the right objects to compare; T-10's THERMAL numbers are not, "
            "because they sit on the Winkler foundation.",
            "CROSSOVER PHASE 8 FORCES 8 nt CORNER DOMAINS. Fourteen staples are lone 8-mers. They "
            "carry no crossover, so their fraying costs the lattice nothing, but they do fray and "
            "the intact fraction is reported beside every result.",
            "AN UNDER-SAMPLED SOFT MODE READS AS STIFFER THAN IT IS, which is the direction that "
            "would flatter the corpus. Every rigidity here is quoted with its convergence.",
            "THE RAW TRAJECTORIES WERE PRUNED (649 MB of .dat). Every DERIVED field is recomputable "
            "from the retained JSON by this script; every RECORDED field is not, and says so. "
            "tools/oxdna/README.md carries the recipe that regenerates the run from scratch.",
        ],
        "openQuestions": [
            "T-9's vertical-compliance half: C-0009 models the crossover as rigid in z, and C-0015 "
            "makes that assumption decide whether the registration design rule exists at all.",
            "T-9's in-plane shear k_s: C-0020's single undetermined input, and C-0028 shows it "
            "moves a buckling verdict.",
            "The twist control: build C-0133's twist-corrected 110 bp raster and the +25.6 deg "
            "should collapse. Cheap, and it was not run.",
            "Whether the bracket's ASSUMPTION can be replaced by a measurement -- an umbrella "
            "sampling of the roll with the duplex torsion constrained would separate the hinge "
            "from what sits in parallel with it, which is the only thing that narrows 5.1x.",
        ],
    }

    destination = os.path.join(ROOT, "gpd", "results", "T-9-crossover-hinge-constant.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(rounded(result), handle, indent=2, ensure_ascii=False)
        handle.write("\n")

    print("wrote {}".format(destination))
    print("k_theta bracket {:.4g} - {:.4g} pN nm/rad, corpus {:.4g}, inside = {}".format(
        lower, upper, CORPUS_K_THETA, inside))
    print("bracket width {:.2f}x; D_perp {:.3g} - {:.3g} against the corpus's {:.4g}".format(
        upper / lower, d_perpendicular(lower), d_perpendicular(upper), CORPUS_D_PERPENDICULAR))
    worst = max(r["departure"] for r in reproductions)
    print("reproductions: {} rows, worst departure {:.2e}".format(len(reproductions), worst))
    problems = 0
    for name, entry in sorted(falsifiers.items()):
        print("  {} {}".format(name, "FIRED" if entry["fired"] else "did not fire"))
        if entry["fired"]:
            problems += 1
    if worst > 1e-9:
        print("  REPRODUCTION MISS: worst departure {:.2e}".format(worst))
        problems += 1
    return 1 if (args.check and problems) else 0


if __name__ == "__main__":
    sys.exit(main())
