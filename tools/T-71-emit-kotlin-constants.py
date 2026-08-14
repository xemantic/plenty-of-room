#!/usr/bin/env python3
"""T-71 — emit `src/main/kotlin/anchoring/MeasuredBackbone.kt` from the survey JSON.

Generated rather than transcribed: every constant in the Kotlin torsion check comes from
`gpd/data/T-71-bdna-backbone-survey.json`, which comes from crystallographic coordinates, and
nothing is retyped by hand.  Re-run after `tools/T-71-bdna-backbone-survey.py`.

    python3 tools/T-71-emit-kotlin-constants.py
"""

from __future__ import annotations

import argparse
import json

HEADER = '''/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.nano.plentyofroom.anchoring

/**
 * `T-71` — **the measured B-DNA backbone**, and every number in it comes from crystallographic
 * coordinates rather than from a citation or a force field.
 *
 * GENERATED — do not edit by hand. Produced by `tools/T-71-emit-kotlin-constants.py` from
 * `gpd/data/T-71-bdna-backbone-survey.json`, which is produced by
 * `tools/T-71-bdna-backbone-survey.py`. Both scripts are retained in this repository so the
 * derivation is reproducible and inspectable.
 *
 * ## Provenance
 *
 * RCSB search: X-ray, polymer composition **DNA only**, resolution ≤ {RESOLUTION} Å,
 * {ENTRIES_RETURNED} entries returned and {ENTRIES_USED} carrying usable deoxyribonucleotides.
 * {RESIDUES} residues, {LINKAGES} phosphodiester linkages, {STEPS} helical steps.
 *
 * The **local frame** of a residue is `(ê_r, ê_t, ê_z)` anchored on its own phosphorus, with `ê_z`
 * the helical axis of a **three-residue window** oriented 5′→3′, `ê_r` radially outward to the
 * phosphorus and `ê_t = ê_z × ê_r`. A single dinucleotide step is a poor estimator of a helical
 * axis, which is why the window is three residues and why steps whose window superposes worse than
 * {RMSD} Å are excluded — a step that is not a helix cannot define a helical frame.
 *
 * The two **templates** are not averages. Averaging local coordinates over a population whose
 * frames carry noise contracts every internal bond, so the template emitted is the population
 * **medoid** — one real, measured nucleotide, whose internal covalent geometry is a molecule's.
 *
 * Lengths are in **nm** (the survey works in Å; the conversion is applied here, once). Angles are
 * in **degrees**, IUPAC sign convention.
 */
object MeasuredBackbone {
'''


def fmt(value):
    return repr(float(value))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--survey", default="gpd/data/T-71-bdna-backbone-survey.json")
    parser.add_argument("--out", default="src/main/kotlin/anchoring/MeasuredBackbone.kt")
    args = parser.parse_args()

    with open(args.survey) as handle:
        d = json.load(handle)

    provenance = d["provenance"]
    counts = d["counts"]
    linkage = d["linkageGeometry"]

    out = [
        HEADER.replace("{RESOLUTION}", str(provenance["criteria"]["resolution_combined_max"]))
        .replace("{ENTRIES_RETURNED}", str(provenance["entriesReturned"]))
        .replace("{ENTRIES_USED}", str(provenance["entriesUsed"]))
        .replace("{RESIDUES}", str(counts["residues"]))
        .replace("{LINKAGES}", str(counts["linkages"]))
        .replace("{STEPS}", str(counts["steps"]))
        .replace("{RMSD}", str(provenance["rmsdCeiling"]))
    ]

    def line(text):
        out.append("    " + text if text else "")

    line(f'const val RESOLUTION_CEILING: Double = {fmt(provenance["criteria"]["resolution_combined_max"])}')
    line(f'const val ENTRIES_RETURNED: Int = {provenance["entriesReturned"]}')
    line(f'const val ENTRIES_USED: Int = {provenance["entriesUsed"]}')
    line(f'const val RESIDUES: Int = {counts["residues"]}')
    line(f'const val RESIDUES_SOUTH: Int = {counts["residuesSouth"]}')
    line(f'const val LINKAGES: Int = {counts["linkages"]}')
    line(f'const val STEPS: Int = {counts["steps"]}')
    line(f'const val STEPS_HELICAL: Int = {counts["stepsHelical"]}')
    line("")
    line("// --- the covalent geometry of the phosphodiester linkage, in nm and degrees")
    for key, name, scale in [
        ("o3p", "O3_P_BOND", 0.1),
        ("po5", "P_O5_BOND", 0.1),
        ("c3o3p", "ANGLE_C3_O3_P", 1.0),
        ("o3po5", "ANGLE_O3_P_O5", 1.0),
        ("po5c5", "ANGLE_P_O5_C5", 1.0),
    ]:
        stats = linkage[key]
        line(f'const val {name}: Double = {fmt(stats["mean"] * scale)}')
        line(f'const val {name}_SD: Double = {fmt(stats["sd"] * scale)}')
    line("")
    line("// --- the measured intrastrand P–P step, by sugar pucker, in nm")
    for key, name in [("ppSouth", "STEP_SOUTH"), ("ppNorth", "STEP_NORTH"), ("ppAll", "STEP_ALL")]:
        stats = linkage[key]
        line(f'const val {name}: Double = {fmt(stats["mean"] * 0.1)}')
        line(f'const val {name}_SD: Double = {fmt(stats["sd"] * 0.1)}')
        line(f'const val {name}_P1: Double = {fmt(stats["p1"] * 0.1)}')
        line(f'const val {name}_P99: Double = {fmt(stats["p99"] * 0.1)}')
    line("")

    for tag, prefix in [("templateB", "B_SOUTH"), ("templateA", "A_NORTH")]:
        template = d[tag]
        medoid = template["medoid"]
        line(f'// --- {template["label"]}: medoid {medoid["entry"]} chain {medoid["chain"]} '
             f'residue {medoid["residue"]} ({medoid["name"]}), of {template["n"]} steps')
        line(f'const val {prefix}_POPULATION: Int = {template["n"]}')
        line(f'const val {prefix}_SOURCE: String = "{medoid["entry"]} {medoid["chain"]}'
             f'/{medoid["residue"]} {medoid["name"]}"')
        line(f'const val {prefix}_PHASE: Double = {fmt(medoid["phase"])}')
        line(f'const val {prefix}_TWIST: Double = {fmt(medoid["twist"])}')
        line(f'const val {prefix}_RISE: Double = {fmt(medoid["rise"] * 0.1)}')
        line(f'const val {prefix}_PHOSPHATE_RADIUS: Double = {fmt(medoid["radius"] * 0.1)}')
        line(f'const val {prefix}_POPULATION_PHOSPHATE_RADIUS: Double = '
             f'{fmt(template["phosphateRadius"]["mean"] * 0.1)}')
        line(f'const val {prefix}_POPULATION_PHOSPHATE_RADIUS_SD: Double = '
             f'{fmt(template["phosphateRadius"]["sd"] * 0.1)}')
        line(f'const val {prefix}_POPULATION_TWIST: Double = {fmt(template["twist"]["mean"])}')
        line(f'const val {prefix}_POPULATION_RISE: Double = {fmt(template["rise"]["mean"] * 0.1)}')
        for torsion, value in sorted(medoid["torsions"].items()):
            line(f'const val {prefix}_{torsion.upper()}: Double = {fmt(value)}')
        line(f'val {prefix}_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(')
        for name, atom in sorted(template["atoms"].items()):
            line(f'    "{name}" to Triple('
                 f'{fmt(atom["radial"] * 0.1)}, {fmt(atom["tangential"] * 0.1)}, '
                 f'{fmt(atom["axial"] * 0.1)}),')
        line(")")
        line("// the medoid's SUCCESSOR residue, in its own local frame about the same axis:")
        line("// this is what makes the free limiting case a REAL dinucleotide")
        line(f'const val {prefix}_NEXT_RADIUS: Double = {fmt(medoid["nextRadius"] * 0.1)}')
        line(f'const val {prefix}_NEXT_PHASE: Double = {fmt(medoid["nextPhase"])}')
        line(f'const val {prefix}_STEP_TWIST: Double = {fmt(medoid["stepTwist"])}')
        line(f'const val {prefix}_STEP_RISE: Double = {fmt(medoid["stepRise"] * 0.1)}')
        for torsion, value in sorted(medoid["nextTorsions"].items()):
            line(f'const val {prefix}_NEXT_{torsion.upper()}: Double = {fmt(value)}')
        line(f'val {prefix}_NEXT_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(')
        for name in sorted(template["atoms"].keys()):
            atom = medoid["nextLocal"][name]
            line(f'    "{name}" to Triple('
                 f'{fmt(atom[0] * 0.1)}, {fmt(atom[1] * 0.1)}, {fmt(atom[2] * 0.1)}),')
        line(")")
        line("")

    line("// --- the populated regions, marginal: ten-degree occupancy histograms from -180")
    line(f'const val HISTOGRAM_BINS: Int = {d["histogramBins"]}')
    line("val TORSION_HISTOGRAM: Map<String, List<Int>> = mapOf(")
    for name in ["alpha", "beta", "gamma", "delta", "epsilon", "zeta", "chi"]:
        h = d["histograms"][name]
        line(f'    "{name}" to listOf(' + ", ".join(str(c) for c in h["counts"]) + "),")
    line(")")
    line("val TORSION_HISTOGRAM_TOTAL: Map<String, Int> = mapOf(")
    for name in ["alpha", "beta", "gamma", "delta", "epsilon", "zeta", "chi"]:
        line(f'    "{name}" to {d["histograms"][name]["total"]},')
    line(")")
    line("")
    line("// --- the populated regions, joint: k-means conformer classes over (α, β, γ, δ, ε, ζ, χ)")
    line(f'const val CONFORMER_CLASSES: Int = {len(d["conformers"])}')
    line("val CONFORMERS: List<List<Double>> = listOf(")
    line("    // population, fraction, alpha, beta, gamma, delta, epsilon, zeta, chi,")
    line("    // radius95, radius99, radiusMax")
    for conformer in d["conformers"]:
        centre = conformer["centre"]
        values = [
            float(conformer["population"]),
            conformer["fraction"],
            centre["alpha"], centre["beta"], centre["gamma"], centre["delta"],
            centre["epsilon"], centre["zeta"], centre["chi"],
            conformer["radius95"], conformer["radius99"], conformer["radiusMax"],
        ]
        line("    listOf(" + ", ".join(fmt(v) for v in values) + "),")
    line(")")
    out.append("}\n")

    with open(args.out, "w") as handle:
        handle.write("\n".join(out))
    print(f"wrote {args.out}")


if __name__ == "__main__":
    main()
