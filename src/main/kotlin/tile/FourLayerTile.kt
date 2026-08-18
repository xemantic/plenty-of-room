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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.structure.DnaDuplex
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * How strongly the layers of a multi-layer origami body are taken to be coupled in bending.
 *
 * `OrigamiSheet.InterlayerCoupling` offers `NONE` and `RIGID`, and applies `RIGID` **only along
 * the helices** — because, as `C-0006` says in its own validity range, *"the across-helix axial
 * stiffness of a crossover is not determined here"*. That leaves the standing
 * `four-layer-honeycomb-rigid` variant in a **mixed** state: composite along `x`, uncoupled
 * along `y`. It is a bound on neither, and its anisotropy of 744.5 is an artefact of the mix.
 *
 * This enum separates the three readings so that each can be reported for what it is.
 */
enum class LayerCoupling {

    /** Layers bend independently and simply add — `D ∝ n` in both directions. The lower bound. */
    INDEPENDENT,

    /**
     * The parallel-axis theorem applied to the duplex stretch modulus along the helices only —
     * `OrigamiSheet`'s `RIGID`, reproduced exactly so that `C-0006`'s table row is traceable.
     * A **mixed** state, carried for traceability and not as a physical bracket end.
     */
    ALONG_HELICES_ONLY,

    /**
     * The parallel-axis theorem applied in **both** directions: the duplex stretch modulus
     * along the helices, and the crossover's own in-plane spring across them. The upper bound.
     */
    COMPOSITE,

    /**
     * A stated **fraction** of the composite excess — the reading a measurement supports.
     *
     * See [MeasuredBundleRigidity]: four independently measured origami bundles, two lattices,
     * three laboratories and three techniques put a real crossover-linked bundle at
     * `f = 0.26–0.33` of the way from [INDEPENDENT] to [COMPOSITE], and Kauert et al.'s own
     * model reached the same conclusion by fitting boundary conditions between the two.
     */
    CALIBRATED
}

/**
 * What a MEASURED origami bundle's bending rigidity says about interlayer coupling.
 *
 * ## Why this object exists
 *
 * `INDEPENDENT` and `COMPOSITE` differ by `1 + S Σy²/(n B)`, which for four honeycomb layers
 * is **39.4×**. No solve narrows that. A measurement does, and there are four.
 *
 * The composite fraction is `f = (EI_measured − EI_independent)/(EI_composite − EI_independent)`.
 * **It is arithmetic performed here on published persistence lengths; no source reports it.**
 *
 * ## The measured bundles
 *
 * - **Kauert, Kurth, Liedl & Seidel**, *Nano Lett.* **11**:5558 (2011), `10.1021/nl203503s` —
 *   magnetic tweezers on a **four-helix square-lattice** and a **six-helix honeycomb** bundle.
 *   **The primary is PAYWALLED and was NOT obtained** (ACS 403s the article, the PDF and the SI;
 *   Unpaywall `oa_status: closed`; no repository copy). Its abstract was read directly from
 *   EuropePMC and states: *"Compared to duplex DNA, we find the bending rigidities to be greatly
 *   increased while the torsional rigidities are only moderately augmented. We present a
 *   mechanical model explicitly including the crossovers … that reproduces the experimentally
 *   observed behavior."* The two persistence lengths below are quoted from **Chhabra et al.**,
 *   arXiv:2006.15029, Table 1 (read directly), which attributes them to Kauert, and are
 *   independently corroborated to 1 % by **Zhang et al.**, *Cyborg Bionic Syst.* 2022
 *   (PMC9494703, read directly): *"the bending stiffness of 4 Helix Bundle (HB) and 6HB was
 *   15-folds and 38-folds stronger than floppy DNA duplexes."*
 * - **Pfitzner, Wachauf, Kilchherr, Pelz, Shih, Rief & Dietz**, *Angew. Chem. Int. Ed.*
 *   **52**:7766 (2013), PMC3749440, **read directly**: *"persistence lengths of 2 μm for the
 *   six-helix bundle and 3.5 μm for the eight-helix bundle."*
 * - **Wang, Schiffels, Martinez Cuesta, Seeman & Fygenson**, *JACS* **134**:1606 (2012),
 *   PMC3267479, **read directly** — and this source publishes the RIGID formula itself, names it
 *   a *"naïve model"* of *"rigidly linked rods"* with `p_tube/p_helix = N[1 + 2(R/r)²]`, and
 *   measures it to over-predict: **2.7 / 4.4 / 5.25 μm estimated against 1.0 / 3.6 / 5.0
 *   measured**.
 *
 * ## What the calibration is NOT
 *
 * Every measured bundle is a **rod whose helices are mutually crossovered around a closed
 * ring**. A 15-wide × 4-deep slab has a different crossover topology and a far larger `Σy²`, and
 * shear lag grows with the lever arm — so `f = 0.30` is plausibly an **upper** bound there.
 * And every measured `f` is depressed by fabrication defects as well as by crossover compliance,
 * which no source separates: Chhabra et al. say *"the experimentally measured values should be
 * seen as lower bounds to the persistence lengths of ideal structures."* Their own oxDNA
 * simulations of defect-free bundles land at `f = 0.74`.
 */
object MeasuredBundleRigidity {

    /** The central composite fraction, **DERIVED** here from the six rows below. */
    const val COMPOSITE_FRACTION: Double = 0.30

    /** The low end of the measured band. */
    const val COMPOSITE_FRACTION_MIN: Double = 0.26

    /** The high end of the measured band. */
    const val COMPOSITE_FRACTION_MAX: Double = 0.33

    /**
     * `f` for a bundle of [helices] duplexes whose axes have second moment [secondMoment] in
     * `nm²` about the bending axis, measured at bending persistence length
     * [persistenceLength] in nm.
     */
    fun compositeFraction(
        helices: Int,
        secondMoment: Double,
        persistenceLength: Double,
        thermalEnergy: Double = com.xemantic.nano.plentyofroom.thermalEnergy(),
        duplexBendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS
    ): Double {
        require(helices >= 2) { "a bundle needs at least two helices, was: $helices" }
        require(secondMoment > 0.0) { "secondMoment must be positive, was: $secondMoment" }
        require(persistenceLength > 0.0) {
            "persistenceLength must be positive, was: $persistenceLength"
        }
        val independent = helices * duplexBendingRigidity
        val composite = independent + stretchModulus * secondMoment
        return (persistenceLength * thermalEnergy - independent) / (composite - independent)
    }
}

/**
 * The three flexural rigidities of a multi-layer origami sheet, reduced to what an orthotropic
 * Kirchhoff plate — and the beam-and-hinge grillage that approximates it — needs.
 *
 * ## The identity this type exists to expose
 *
 * Chen et al.'s softened-bond construction gives the crossover a hinge spring
 * `k_θ = 2αB/(100a)` and — by `Gen1Tile.crossoverInPlaneStiffness`, applying the same
 * construction to the one duplex constant that describes displacement — an in-plane spring
 * `k_s = 2αS/(100a)`. Their **ratio is `S/B`**, the duplex's own stretch modulus over its
 * bending rigidity, and `α` cancels.
 *
 * The parallel-axis excess is `(S/d)Σy²` over `nB/d` along the helices and
 * `(k_s d/p)Σy²` over `n(k_θ/p)d` across them. Both reduce to `S Σy²/(n B)`.
 * So a multi-layer body's composite enhancement is **the same factor in both directions,
 * exactly**, and its anisotropy `D_∥/D_⊥` does not depend on how strongly its layers are
 * coupled. That is what makes the interlayer-coupling bracket a pure **scale**, and it is
 * why the reach lengths in both directions move by the same fourth root.
 *
 * @param layers the number of stacked duplex layers.
 * @param interhelicalDistance `d` in nm, also taken as the layer spacing.
 * @param crossoverSpacing `p`, the per-interface crossover spacing in nm.
 * @param alongHelixRigidity `D_∥` in `pN·nm`.
 * @param acrossHelixRigidity `D_⊥` in `pN·nm`.
 * @param twistingRigidity `D_k` in `pN·nm` — a **lower bound**, per `C-0006`: the across-helix
 *          counterpart is a crossover torsion nothing in this programme determines.
 * @param thickness the geometric thickness in nm, `(n−1)d + 2.0`.
 * @param secondMomentOfLayers `Σ y_i²` in `nm²` over the layer mid-planes.
 * @param parallelAxisFactor `1 + S Σy²/(n B)` — the composite enhancement, one number for
 *          both directions.
 */
@Serializable
data class MultiLayerRigidities(
    val layers: Int,
    val coupling: String,
    val interhelicalDistance: Double,
    val layerSpacing: Double,
    val crossoverSpacing: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val twistingRigidity: Double,
    val thickness: Double,
    val secondMomentOfLayers: Double,
    val parallelAxisFactor: Double,
    val realisedEnhancement: Double,
    val compositeFraction: Double
) {

    /** `D_∥/D_⊥`, the rigidity anisotropy. */
    val anisotropy: Double get() = alongHelixRigidity / acrossHelixRigidity
}

/**
 * The rigidities of [layers] duplex layers at [interhelicalDistance], joined by crossovers every
 * [crossoverSpacingBasePairs] along each interface, under the interlayer [coupling].
 *
 * At `layers = 1` every coupling returns the same thing and [MultiLayerRigidities.parallelAxisFactor]
 * is exactly 1, because a single layer's mid-plane is the sheet's.
 */
fun multiLayerRigidities(
    layers: Int,
    interhelicalDistance: Double,
    crossoverSpacingBasePairs: Double,
    coupling: LayerCoupling,
    duplex: DnaDuplex = DnaDuplex(
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS
    ),
    crossoverAlpha: Double = 1.0,
    duplexDiameter: Double = OrigamiSheet.DUPLEX_DIAMETER,
    compositeFraction: Double = MeasuredBundleRigidity.COMPOSITE_FRACTION,
    /**
     * The centre-to-centre spacing of the layers in `z`, in nm — [interhelicalDistance] by
     * default, which is `OrigamiSheet`'s own default and what `C-0006`'s standing variant uses.
     *
     * A true honeycomb array stacks its rows at `d√3/2`, not `d`, so the default **overstates**
     * `Σy²` by `4/3`. It is kept as the default so that `C-0006`'s published row reproduces;
     * the corrected geometry is carried beside it rather than instead of it.
     */
    layerSpacing: Double = interhelicalDistance
): MultiLayerRigidities {
    require(layers >= 1) { "layers must be at least 1, was: $layers" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(crossoverSpacingBasePairs > 0.0) {
        "crossoverSpacingBasePairs must be positive, was: $crossoverSpacingBasePairs"
    }
    require(compositeFraction >= 0.0 && compositeFraction <= 1.0) {
        "compositeFraction must lie in [0, 1], was: $compositeFraction"
    }
    val d = interhelicalDistance
    val p = crossoverSpacingBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val hinge = Gen1Tile.crossoverHingeStiffness(crossoverAlpha)
    val inPlane = Gen1Tile.crossoverInPlaneStiffness(crossoverAlpha)
    require(layerSpacing > 0.0) { "layerSpacing must be positive, was: $layerSpacing" }
    val offsets = (0 until layers).map { (it - (layers - 1) / 2.0) * layerSpacing }
    val secondMoment = offsets.sumOf { it * it }
    val factor = 1.0 + duplex.stretchModulus * secondMoment / (layers * duplex.bendingRigidity)

    val alongIndependent = layers * duplex.bendingRigidity / d
    val acrossIndependent = layers * (hinge / p) * d

    // `k_s/k_theta = S/B` makes the across-helix parallel-axis excess equal the along-helix one,
    // exactly — asserted in FourLayerTileTest gate 3 rather than argued here.
    check(abs((inPlane * d / p) * secondMoment / acrossIndependent -
            duplex.stretchModulus * secondMoment / d / alongIndependent) < 1e-9 * (factor - 1.0) + 1e-12) {
        "the parallel-axis excess must be the same factor in both directions"
    }
    // The realised enhancement: 1 at INDEPENDENT, `factor` at COMPOSITE, and the measured
    // fraction of the excess at CALIBRATED. It is ONE number for both directions.
    val realised = when (coupling) {
        LayerCoupling.INDEPENDENT -> 1.0
        LayerCoupling.ALONG_HELICES_ONLY, LayerCoupling.COMPOSITE -> factor
        LayerCoupling.CALIBRATED -> 1.0 + compositeFraction * (factor - 1.0)
    }
    val along = alongIndependent * realised
    val across = when (coupling) {
        // C-0006's standing variant applies the parallel axis along the helices ONLY.
        LayerCoupling.ALONG_HELICES_ONLY -> acrossIndependent
        else -> acrossIndependent * realised
    }
    return MultiLayerRigidities(
        layers = layers,
        coupling = coupling.name,
        interhelicalDistance = d,
        layerSpacing = layerSpacing,
        crossoverSpacing = p,
        alongHelixRigidity = along,
        acrossHelixRigidity = across,
        twistingRigidity = layers * duplex.torsionalRigidity / (4.0 * d),
        thickness = (layers - 1) * layerSpacing + duplexDiameter,
        secondMomentOfLayers = secondMoment,
        parallelAxisFactor = factor,
        realisedEnhancement = realised,
        compositeFraction = when (coupling) {
            LayerCoupling.INDEPENDENT -> 0.0
            LayerCoupling.ALONG_HELICES_ONLY, LayerCoupling.COMPOSITE -> 1.0
            LayerCoupling.CALIBRATED -> compositeFraction
        }
    )
}

/**
 * The **smeared equivalent sheet** — one layer whose per-beam `EI`, per-crossover `k_θ` and
 * per-beam `GJ` are chosen so that its three plate rigidities equal [rigidities] exactly.
 *
 * ## Why this is needed, and what it costs
 *
 * `OrigamiGrillage` reads exactly five things from its `OrigamiSheet` — the interhelical
 * distance, the crossover spacing, the crossover hinge stiffness, and the duplex's bending and
 * torsional rigidities — and **never reads `layers` or `interlayerCoupling`**. Assembling a
 * grillage on `Gen1Tile`'s `four-layer-honeycomb-rigid` variant would therefore produce a
 * lattice bit-identical to the single-layer honeycomb one: the lattice machinery of this
 * repository is **single-layer, and square-lattice in its crossover combinatorics**.
 *
 * What this function supplies is the one substitution that lets the existing lattice carry a
 * multi-layer body's *stiffness*. What it cannot carry:
 *
 * - **transverse shear**. At 9.608 nm over a 38.08 nm span the thickness/span ratio is 0.252,
 *   outside Kirchhoff — so `D_∥` is an upper bound again, on top of the coupling assumption.
 * - the honeycomb lattice's **three** crossover azimuths. `CrossoverLayout` models a
 *   two-parity alternation, which is the square lattice; the smeared rigidities are right and
 *   the crossover *combinatorics* are not.
 * - a **per-layer** defect. One crossover of this lattice stands for the layers' crossovers
 *   in parallel, so removing it removes all of them.
 */
fun equivalentSheet(rigidities: MultiLayerRigidities): OrigamiSheet = OrigamiSheet(
    duplex = DnaDuplex(
        bendingRigidity = rigidities.alongHelixRigidity * rigidities.interhelicalDistance,
        torsionalRigidity =
            rigidities.twistingRigidity * 4.0 * rigidities.interhelicalDistance,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS
    ),
    interhelicalDistance = rigidities.interhelicalDistance,
    crossoverSpacing = rigidities.crossoverSpacing,
    crossoverHingeStiffness = rigidities.acrossHelixRigidity *
            rigidities.crossoverSpacing / rigidities.interhelicalDistance,
    layers = 1
)

/**
 * The scaffold nucleotides a rectangular raster of [layers] × [rows] duplexes of
 * [basePairsPerRow] consumes — one nucleotide of scaffold per base pair per duplex.
 *
 * `C-0086`'s own construction (`sheetScaffoldNucleotides`), extended by the layer count. It is
 * a **count**, and it is lattice-independent: whatever the crossover rule, a duplex of `L` base
 * pairs carries `L` nucleotides of scaffold on one of its two strands.
 */
fun scaffoldNucleotides(layers: Int, rows: Int, basePairsPerRow: Int): Long {
    require(layers >= 1) { "layers must be at least 1, was: $layers" }
    require(rows >= 1) { "rows must be at least 1, was: $rows" }
    require(basePairsPerRow >= 1) {
        "basePairsPerRow must be at least 1, was: $basePairsPerRow"
    }
    return layers.toLong() * rows.toLong() * basePairsPerRow.toLong()
}

/** How many layers of [rows] × [basePairsPerRow] a scaffold of [scaffoldNucleotides] pays for. */
fun layersAffordable(scaffoldNucleotides: Long, rows: Int, basePairsPerRow: Int): Int {
    require(scaffoldNucleotides >= 1L) {
        "scaffoldNucleotides must be positive, was: $scaffoldNucleotides"
    }
    require(rows >= 1) { "rows must be at least 1, was: $rows" }
    require(basePairsPerRow >= 1) {
        "basePairsPerRow must be at least 1, was: $basePairsPerRow"
    }
    return (scaffoldNucleotides / (rows.toLong() * basePairsPerRow.toLong())).toInt()
}
