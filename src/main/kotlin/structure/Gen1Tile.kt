/*
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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable

/**
 * The §3 Gen-1 tile as a structural object, with every input number carrying its provenance.
 *
 * Shared by the `T-5` and `T-5b` study entry points, which are one structural model
 * evaluated for two different outputs.
 *
 * ## Provenance of every number below
 *
 * `CITED` means taken from a named source and not re-derived here; `DERIVED` means computed
 * from more primitive inputs in code. Rupture forces are additionally flagged with their
 * loading rate, because a rupture force without one is not a material constant.
 */
object Gen1Tile {

    // ------------------------------------------------------------------ §3 geometry and targets

    /** §3 tile footprint along the helices, in nm. */
    const val EDGE_X: Double = 40.0

    /** §3 tile footprint across the helices, in nm. */
    const val EDGE_Y: Double = 40.0

    /** §3 test-tile footprint, in nm. */
    const val TEST_EDGE_X: Double = 100.0

    /** §3 test-tile footprint, in nm. */
    const val TEST_EDGE_Y: Double = 70.0

    /** §3 target force, in pN. */
    const val TARGET_FORCE: Double = 100.0

    /** §3 acceptable stroke, in nm. */
    const val ACCEPTABLE_STROKE: Double = 3.0

    /** §3 desired stroke, in nm. */
    const val DESIRED_STROKE: Double = 10.0

    /** §3 Debye length at 2 mM Mg²⁺, in nm. */
    const val DEBYE_LENGTH: Double = 4.0

    // ------------------------------------------------------------------ duplex elasticity

    /**
     * `EI` of a B-form duplex in `pN·nm²` — **CITED**, Kim, Kilchherr, Dietz & Bathe,
     * *Nucleic Acids Res.* **40**:2862 (2012), the CanDo parameter set:
     * "stretching (1100 pN), bending (230 pN nm²), and torsional (460 pN nm²) stiffness".
     *
     * Corresponds to a bending persistence length of 55.5 nm at 300 K. Measured values for
     * free DNA are lower — 47 nm in 10 mM Na⁺ and ~40 nm with Mg²⁺
     * (Wang et al., *Biophys. J.* **72**:1335, 1997) — so this is the **stiff** end and is
     * swept downward in the study.
     */
    const val DUPLEX_BENDING_RIGIDITY: Double = 230.0

    /** Bending persistence length in nm measured with Mg²⁺ — **CITED**, Wang et al. (1997). */
    const val DUPLEX_BENDING_PERSISTENCE_MAGNESIUM: Double = 40.0

    /** `GJ` of a B-form duplex in `pN·nm²` — **CITED**, CanDo (2012). */
    const val DUPLEX_TORSIONAL_RIGIDITY: Double = 460.0

    /**
     * Torsional persistence length in nm — **CITED**, two independent magnetic-tweezers
     * measurements: 103 ± 4 nm (Kriegel et al., *Nucleic Acids Res.* **45**:5920, 2017)
     * and 97 ± 4 nm (Kauert et al., *Nano Lett.* **11**:5558, 2011, SI).
     */
    const val DUPLEX_TORSIONAL_PERSISTENCE: Double = 100.0

    /** Stretch modulus in pN — **CITED**, Wang et al. (1997), "elastic modulus of approximately 1100 pN". */
    const val DUPLEX_STRETCH_MODULUS: Double = 1100.0

    /** Axial rise per base pair in nm — **CITED**, Douglas et al., *Nature* **459**:414 (2009). */
    const val RISE_PER_BASE_PAIR: Double = 0.34

    // ------------------------------------------------------------------ sheet geometry

    /**
     * Interhelical centre-to-centre distance of a **single-layer 2D sheet**, in nm —
     * **CITED, MEASURED** by small-angle X-ray scattering:
     * Fischer et al., *Nano Lett.* **16**:4282 (2016), "for the one layer sheet, we obtain an
     * inter-helical distance of 26.9 ± 0.2 Å", at ≥ 10 mM Mg²⁺.
     *
     * Note this supersedes the ~3.0 nm that Rothemund's 1 nm interhelical gap implies.
     */
    const val INTERHELICAL_SHEET: Double = 2.69

    /** Interhelical distance of a **honeycomb** lattice, in nm — same source, "25.36 ± 0.03 Å". */
    const val INTERHELICAL_HONEYCOMB: Double = 2.536

    /**
     * Crossover spacing along **one helix-helix interface** of a Rothemund single-layer sheet,
     * in base pairs — **CITED**, Rothemund, *Nature* **440**:297 (2006): crossovers recur every
     * 1.5 turns (16 bp) along a helix but **alternate between its two neighbours**, so a given
     * adjacent pair is linked every 32 bp. Quoting 16 bp here would double the rigidity.
     */
    const val CROSSOVER_SPACING_SHEET_BP: Double = 32.0

    /**
     * Crossover spacing along one helix-helix interface of a honeycomb lattice, in base pairs —
     * **CITED**, Douglas et al., *Nature* **459**:414 (2009): "the local crossover density along
     * any helix-helix interface at roughly one per 21 base pairs".
     */
    const val CROSSOVER_SPACING_HONEYCOMB_BP: Double = 21.0

    // ------------------------------------------------------------------ crossover compliance

    /**
     * The bending spring constant of **one antiparallel crossover**, in `pN·nm/rad` —
     * **CITED, fitted to measurement**: Chen, Weng, Riccitelli, Cui, Irudayaraj & Choi,
     * *J. Am. Chem. Soc.* **136**:6995 (2014), Supporting Information §S2.
     *
     * Their model is `k₂ = α B/(100 a)` per **crossover phosphate bond**, with `B` the duplex
     * bending rigidity, `a` the rise per base pair, and the factor 100 carried over from CanDo's
     * nick softening. An antiparallel crossover has **two** such bonds in parallel, so
     * `k_θ = 2 α B/(100 a)`. `α` is bracketed experimentally: "This inequality can be satisfied
     * only when α is between 0.6 and 1.2. For simplicity, we choose α=1."
     *
     * The same source states plainly why this number has no competitor in the literature:
     * "CanDo treats crossovers as rigid constraints … In a multilayer structure, the crossover
     * bending degree-of-freedom is largely prohibited … even though it does not consider the
     * significant bending flexibility of crossovers."
     */
    fun crossoverHingeStiffness(alpha: Double = 1.0): Double =
        2.0 * alpha * DUPLEX_BENDING_RIGIDITY / (100.0 * RISE_PER_BASE_PAIR)

    /** The lower bound of Chen et al.'s experimentally admissible `α`. */
    const val CROSSOVER_ALPHA_MIN: Double = 0.6

    /** The upper bound of Chen et al.'s experimentally admissible `α`. */
    const val CROSSOVER_ALPHA_MAX: Double = 1.2

    /**
     * The inter-crossover duplex-twist stiffness per nm of interface, in `pN·nm/rad per nm` —
     * **CITED**, the second, series element of Chen et al.'s spring network:
     * `k_t = 0.3772 k₁` per helix row with `k₁ = T/a = 460/0.34 pN·nm/rad`, over their
     * 224 bp (76.16 nm) wide tile.
     *
     * Omitting it makes `D_⊥` an **upper bound**; including it lowers `D_⊥` by ~16%.
     */
    const val INTERFACE_TWIST_STIFFNESS: Double =
        0.3772 * (DUPLEX_TORSIONAL_RIGIDITY / RISE_PER_BASE_PAIR) / (224.0 * RISE_PER_BASE_PAIR)

    // ------------------------------------------------------------------ foundation, from C-0001

    /**
     * The polymer-layer stiffness per unit area in `pN/nm³` at the `C-0001` design point —
     * the 10 nm layer at the lower edge of the surviving window, `σ = 0.024 nm⁻²`,
     * divided by the 1600 nm² footprint.
     *
     * **Three different numbers, because `C-0001` gate 2 found that "the stiffness of the layer"
     * is not well posed at a single compression.** All three are carried:
     *
     * - `AT_REST` — `k(L₀) = 7.402 pN/nm`, the tangent at first contact, which governs the
     *   *unloaded* thermal fluctuation;
     * - `SECANT` — `F/(L₀−h) = 20.201 pN/nm`, which governs the *stroke* and is the nominal
     *   value for every loaded case here;
     * - `AT_WORKING_POINT` — `k(h) = 53.337 pN/nm`, the tangent under the target force, which
     *   governs the fluctuation *under bias*.
     *
     * All three are **lower bounds** per `CH-0001`, and `T-1c` is re-deriving them, which is why
     * every conclusion in `T-5`/`T-5b` is emitted across a sweep rather than at one value.
     */
    const val FOUNDATION_AT_REST: Double = 7.402 / 1600.0

    /** See [FOUNDATION_AT_REST]. */
    const val FOUNDATION_SECANT: Double = 20.201 / 1600.0

    /** See [FOUNDATION_AT_REST]. */
    const val FOUNDATION_AT_WORKING_POINT: Double = 53.337 / 1600.0

    /** The `C-0001` grafting density at the design point, in `nm⁻²`. */
    const val GRAFTING_DENSITY: Double = 0.024

    /** The `C-0001` layer height at the design point, in nm. */
    const val LAYER_HEIGHT: Double = 10.0

    /** The multipliers `k_f` is swept by, `CH-0001` having made `C-0001`'s numbers lower bounds. */
    val FOUNDATION_SWEEP: List<Double> = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

    // ------------------------------------------------------------------ per-load-path allowables

    /**
     * The shear rupture force of a single 30 bp duplex domain, in pN — **CITED, MEASURED**,
     * Morfill et al., *Biophys. J.* **93**:2400 (2007), "most probable rupture force Fmax of
     * 65 pN" at a loading rate of **2697 pN/s**; and Strunz et al., *PNAS* **96**:11277 (1999),
     * "48 ± 2 pN for the unbinding of the 30 base pairs" at ~50 nm/s retraction.
     *
     * **Loading-rate dependent.** The lower figure is used as the allowable, because a 100 pN
     * static bias is a quasi-static load, i.e. below the slowest rate either paper measured.
     */
    const val DUPLEX_SHEAR_ALLOWABLE: Double = 48.0

    /**
     * The unzipping force of a duplex, in pN — **CITED, MEASURED**, Essevaz-Roulet, Bockelmann
     * & Heslot, *PNAS* **94**:11935 (1997), near-equilibrium microneedle: "opening are in the
     * range of 10–15 pN". A load path presented in unzip geometry is effectively not load-bearing.
     */
    const val DUPLEX_UNZIP_ALLOWABLE: Double = 10.0

    /**
     * The hard ceiling on any duplex load path, in pN — **CITED, MEASURED**, van Mameren et al.,
     * *PNAS* **106**:18231 (2009): overstretching begins at 65 pN when nicks or free ends are
     * present, and every origami helix is nicked at every staple boundary.
     */
    const val OVERSTRETCHING_CEILING: Double = 65.0

}

/** The sheet variants the studies evaluate, each a named reading of §3's "single-layer honeycomb". */
@Serializable
data class SheetVariant(
    val name: String,
    val note: String,
    val interhelicalDistance: Double,
    val crossoverSpacingBasePairs: Double,
    val layers: Int,
    val crossoverAlpha: Double,
    val includeInterfaceTwist: Boolean,
    val interlayerCoupling: String,
    val thickness: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val twistingRigidity: Double,
    val anisotropy: Double,
    val duplexesAcrossTile: Double,
    val crossoversAlongInterface: Double
)

/** Builds the [OrigamiSheet] a [SheetVariant] describes. */
fun origamiSheet(
    interhelicalDistance: Double,
    crossoverSpacingBasePairs: Double,
    layers: Int = 1,
    crossoverAlpha: Double = 1.0,
    includeInterfaceTwist: Boolean = false,
    interlayerCoupling: InterlayerCoupling = InterlayerCoupling.NONE,
    duplexBendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY
): OrigamiSheet = OrigamiSheet(
    duplex = DnaDuplex(
        bendingRigidity = duplexBendingRigidity,
        torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS
    ),
    interhelicalDistance = interhelicalDistance,
    crossoverSpacing = crossoverSpacingBasePairs * Gen1Tile.RISE_PER_BASE_PAIR,
    crossoverHingeStiffness = Gen1Tile.crossoverHingeStiffness(crossoverAlpha),
    interfaceTwistStiffness = if (includeInterfaceTwist) Gen1Tile.INTERFACE_TWIST_STIFFNESS
    else Double.POSITIVE_INFINITY,
    layers = layers,
    layerSpacing = interhelicalDistance,
    interlayerCoupling = interlayerCoupling
)

/** Summarises [sheet] as a [SheetVariant] record for the result JSON. */
fun sheetVariant(
    name: String,
    note: String,
    sheet: OrigamiSheet,
    crossoverAlpha: Double,
    includeInterfaceTwist: Boolean
): SheetVariant = SheetVariant(
    name = name,
    note = note,
    interhelicalDistance = sheet.interhelicalDistance,
    crossoverSpacingBasePairs = sheet.crossoverSpacing / Gen1Tile.RISE_PER_BASE_PAIR,
    layers = sheet.layers,
    crossoverAlpha = crossoverAlpha,
    includeInterfaceTwist = includeInterfaceTwist,
    interlayerCoupling = sheet.interlayerCoupling.name,
    thickness = sheet.thickness,
    alongHelixRigidity = sheet.alongHelixRigidity,
    acrossHelixRigidity = sheet.acrossHelixRigidity,
    twistingRigidity = sheet.twistingRigidity,
    anisotropy = sheet.alongHelixRigidity / sheet.acrossHelixRigidity,
    duplexesAcrossTile = sheet.duplexesOnCut(Gen1Tile.EDGE_Y),
    crossoversAlongInterface = sheet.crossoversOnCut(Gen1Tile.EDGE_X)
)

/** The named sheet readings of §3, evaluated by both studies. */
fun gen1SheetVariants(): List<Pair<SheetVariant, OrigamiSheet>> = buildList {
    val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
    add(
        sheetVariant(
            "single-layer-2d-sheet",
            "Rothemund single-layer sheet: d = 2.69 nm (SAXS), 32 bp per interface. " +
                    "The nominal reading of the §3 'single-layer' tile.",
            sheet, 1.0, false
        ) to sheet
    )
    val honeycomb = origamiSheet(
        Gen1Tile.INTERHELICAL_HONEYCOMB, Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP
    )
    add(
        sheetVariant(
            "single-layer-honeycomb",
            "honeycomb crossover rule at one layer: d = 2.536 nm (SAXS), 21 bp per interface. " +
                    "The literal reading of §3's 'single-layer honeycomb', which is a contradiction " +
                    "in terms since a honeycomb lattice is three-dimensional.",
            honeycomb, 1.0, false
        ) to honeycomb
    )
    val withTwist = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        includeInterfaceTwist = true
    )
    add(
        sheetVariant(
            "single-layer-2d-sheet-with-interface-twist",
            "as the nominal, but with Chen et al.'s inter-crossover duplex twist in series, " +
                    "which lowers D_perp by ~16%. The nominal is therefore an upper bound.",
            withTwist, 1.0, true
        ) to withTwist
    )
    val fourLayer = origamiSheet(
        Gen1Tile.INTERHELICAL_HONEYCOMB, Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        layers = 4, interlayerCoupling = InterlayerCoupling.RIGID
    )
    add(
        sheetVariant(
            "four-layer-honeycomb-rigid",
            "the ~10 nm thickness §3 also states, read as four honeycomb layers with rigidly " +
                    "coupled bending. D_par gains the parallel-axis term; D_perp does not, because " +
                    "the across-helix axial stiffness of a crossover is not determined here, so " +
                    "D_perp is a lower bound for this variant.",
            fourLayer, 1.0, false
        ) to fourLayer
    )
    val stiffAlpha = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        crossoverAlpha = Gen1Tile.CROSSOVER_ALPHA_MAX
    )
    add(
        sheetVariant(
            "single-layer-2d-sheet-alpha-max",
            "the stiff edge of Chen et al.'s admissible crossover range, alpha = 1.2.",
            stiffAlpha, Gen1Tile.CROSSOVER_ALPHA_MAX, false
        ) to stiffAlpha
    )
    val softAlpha = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        crossoverAlpha = Gen1Tile.CROSSOVER_ALPHA_MIN
    )
    add(
        sheetVariant(
            "single-layer-2d-sheet-alpha-min",
            "the soft edge of Chen et al.'s admissible crossover range, alpha = 0.6.",
            softAlpha, Gen1Tile.CROSSOVER_ALPHA_MIN, false
        ) to softAlpha
    )
}
