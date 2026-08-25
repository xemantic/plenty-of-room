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

import com.xemantic.nano.plentyofroom.coupling.DishingSolution
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.influenceSurrogate
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-294` — the four things that do **not** transfer between this programme's two 60-helix
 * honeycomb cross-sections, each derived from the block and asserted rather than carried over.
 *
 * ## Why a file for four quotients
 *
 * `C-0180` re-graded `C-0167`'s coupled cells on the **tied** lattice at `10 × 6` only, and
 * `C-0186` §1 records the consequence: one comparison passage of `DECISIONS-FOR-NDI.md` is left
 * un-annotated because giving one cross-section its tied numbers while the other has none would
 * read as a measured ordering when only one side moved. Closing that with a measurement means
 * building `15 × 4` in the same state — and the standing hazard is not the solve, it is the
 * **census**: `CH-0270` and `CH-0276` are both a count transferred onto a tile that does not
 * have it, and `C-0175`'s own lesson is that a lattice census must be asserted against the
 * **bond graph** rather than against the raster path.
 *
 * So this file derives, from `HoneycombBlock` alone:
 *
 * | | `10 × 6` | `15 × 4` |
 * |---|---|---|
 * | [honeycombBondCensus] | `435` = `135` in plane + `300` through | `410` = `140` + `270` |
 * | [honeycombTieCensus] | `59` = `9` in plane + `50` through | `59` = `14` + `45` |
 * | [honeycombCompositeEnhancement] at `f = 0.30` | `21.1851817` | `9.65079217` |
 * | [crossSectionNormalisation]'s free stroke at `116 bp` | `5.27921926 nm` | `3.5194795 nm` |
 *
 * The turn **count** is `H − 1 = 59` at both, because both blocks carry sixty helices; the
 * **split** is `m(n − 1)` through the thickness and `m − 1` in plane, and it is the one number
 * the queue row for this task predicts will not transfer.
 *
 * The last row is a theorem rather than a measurement and it is the premise of this task's own
 * acceptance clause that the lattice refuses: the interior pressure is `F/(edgeX · edgeY)`,
 * `edgeX` is shared between the two blocks, and `edgeY = m · 3d/2` — so the free stroke `p/k_f`
 * goes as `1/m` and the ratio is `10/15` **exactly**. `T-5b`'s `0.10` is a fraction of that
 * stroke, so in absolute nm the same tolerance is `1.5×` tighter on the taller block, which is
 * why [CrossSectionNormalisation] carries both readings and a study must quote both.
 */

/** The bond census of an assembled lattice, split by the bond's own direction (`C-0208`). */
data class HoneycombBondCensus(

    /** Every staple bond of the lattice. */
    val bonds: Int,

    /** Those whose line of centres lies **in the plane** of the face, where `unitZ = 0`. */
    val inPlane: Int,

    /** Those that run **through the thickness**, where `unitZ² = 0.75`. */
    val throughThickness: Int
)

/** [lattice]'s own bonds, counted by direction — a reading of the object, not of the block. */
fun honeycombBondCensus(lattice: HoneycombGrillage): HoneycombBondCensus =
    HoneycombBondCensus(
        bonds = lattice.bonds.size,
        inPlane = lattice.bonds.count { it.inPlane },
        throughThickness = lattice.bonds.count { !it.inPlane }
    )

/**
 * The raster's own turn census of [block] — the elements `C-0175` adds and the staple ladder
 * does not contain.
 *
 * [everyTurnIsBonded] is the assertion that matters and it costs one set membership per turn:
 * a census keyed on the **traversal** cannot see a turn landing on a pair the honeycomb does not
 * bond at all, and `C-0175` found exactly that on its first run because `HoneycombBlock` and
 * `HoneycombCell` use opposite vertical-bond parities.
 */
data class HoneycombTieCensus(

    /** `H − 1`, one per raster turn. */
    val turns: Int,

    /** Turns that step through the block's thickness — `m(n − 1)` of them. */
    val throughThickness: Int,

    /** Turns that step in plane, at a raster row transition — `m − 1` of them. */
    val inPlane: Int,

    /** Turns sitting at `s = +L/2`. */
    val atHighRim: Int,

    /** Turns sitting at `s = −L/2`. */
    val atLowRim: Int,

    /** Whether every turn joins a pair the lattice actually **bonds**. */
    val everyTurnIsBonded: Boolean
)

/**
 * [block]'s raster turns, censused against its own bond graph.
 *
 * @param firstAxialSign the direction the scaffold traverses the first helix in, `+1` or `−1`;
 *   it exchanges [HoneycombTieCensus.atHighRim] with [HoneycombTieCensus.atLowRim] and moves
 *   nothing else.
 */
fun honeycombTieCensus(
    block: HoneycombBlock,
    firstAxialSign: Int = 1
): HoneycombTieCensus {
    val turns = honeycombRasterTurnList(block, firstAxialSign)
    return HoneycombTieCensus(
        turns = turns.size,
        throughThickness = turns.count { !it.inPlane },
        inPlane = turns.count { it.inPlane },
        atHighRim = turns.count { it.atHighEnd },
        atLowRim = turns.count { !it.atHighEnd },
        everyTurnIsBonded = honeycombTurnsNotBonded(block, turns).isEmpty()
    )
}

/**
 * Those of [turns] that join a pair [block] does **not** bond.
 *
 * Taken as an explicit argument rather than derived, so that the check has a fixture that can
 * discriminate: on a real honeycomb raster the answer is always empty, and a test that asserts
 * only that cannot tell this function from one that returns `emptyList()` unconditionally
 * (`C-0161`, and a mutation of exactly that shape survived this file's first harness run). Handed
 * the pair `C-0175` found on ITS first run — the naive `c = x` identification, which joins two
 * helices `2d` apart — it must report it.
 */
fun honeycombTurnsNotBonded(
    block: HoneycombBlock,
    turns: List<HoneycombRasterTurn>
): List<HoneycombRasterTurn> {
    val bonded = honeycombBondPairs(block)
        .map { (a, b) -> minOf(a, b) to maxOf(a, b) }
        .toSet()
    return turns.filter { (it.lowerBeam to it.upperBeam) !in bonded }
}

/**
 * The composite hinge-stiffness enhancement of [block] at [compositeFraction], at the honeycomb's
 * **own** layer spacing.
 *
 * `multiLayerRigidities` defaults its layer spacing to the interhelical distance, which
 * `CLAUDE.md` records overstates `Σy²` by `4/3` on a honeycomb array; and its `layers` argument
 * is the block's **thickness** count, `helicesPerRow`, not its raster-row count. Both mistakes
 * are silent — they return a plausible number — so the two arguments are bound here once and a
 * study cannot get them the wrong way round.
 */
fun honeycombCompositeEnhancement(
    block: HoneycombBlock,
    compositeFraction: Double
): Double =
    // No guard on `compositeFraction` here. `multiLayerRigidities` already refuses anything
    // outside `[0, 1]`, immediately and with the same message, so a second `require` would be a
    // guard whose only observable behaviour is duplicated downstream — one no mutation of it can
    // reach, which `CLAUDE.md` records and which this file's first mutation run duly reproduced.
    // The refusal is still asserted, at both ends, as a property of THIS function's contract.
    multiLayerRigidities(
        layers = block.helicesPerRow,
        interhelicalDistance = block.bondLength,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = compositeFraction,
        layerSpacing = HoneycombCrossSectionGeometry.columnPitch(block.bondLength)
    ).realisedEnhancement

/**
 * How a cross-section normalises a flatness verdict — and it is the half of a comparison that
 * a fractional column silently hides.
 *
 * @param absoluteToleranceNm `T-5b`'s fraction of **this** tile's own stroke, in nm.
 */
data class CrossSectionNormalisation(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val edgeX: Double,
    val edgeY: Double,
    val area: Double,
    val interiorPressure: Double,
    val freeStroke: Double,
    val absoluteToleranceNm: Double
)

/**
 * [block] at [rowBasePairs], normalised the way every coupled census in this corpus normalises.
 *
 * `edgeY` is the **plate** convention `m · 3d/2` — `HoneycombBlock.plateEdgeY`, which is what
 * `HoneycombGrillage.lengthY` uses — and **not** the block envelope, which is one duplex diameter
 * larger and is `C-0141` §2's `56.524 / 37.504 nm`. The two are both correct and `2.00 nm` apart.
 */
fun crossSectionNormalisation(
    block: HoneycombBlock,
    rowBasePairs: Int,
    targetForce: Double = Gen1Tile.TARGET_FORCE,
    foundationStiffness: Double = Gen1Tile.FOUNDATION_SECANT,
    fractionalTolerance: Double = 0.10
): CrossSectionNormalisation {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(targetForce > 0.0 && targetForce.isFinite()) {
        "targetForce must be positive and finite, was: $targetForce"
    }
    require(foundationStiffness > 0.0 && foundationStiffness.isFinite()) {
        "foundationStiffness must be positive and finite, was: $foundationStiffness"
    }
    require(fractionalTolerance > 0.0 && fractionalTolerance < 1.0) {
        "fractionalTolerance must lie strictly inside (0, 1), was: $fractionalTolerance"
    }
    val edgeX = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY = block.plateEdgeY
    val area = edgeX * edgeY
    val pressure = targetForce / area
    val stroke = pressure / foundationStiffness
    return CrossSectionNormalisation(
        rasterRows = block.rasterRows,
        helicesPerRow = block.helicesPerRow,
        edgeX = edgeX,
        edgeY = edgeY,
        area = area,
        interiorPressure = pressure,
        freeStroke = stroke,
        absoluteToleranceNm = fractionalTolerance * stroke
    )
}

// -------------------------------------------------------------------------------------------
// The face's rigid basis, and why a dishing needs one.
//
// `HoneycombDeflection` removes its best-fit rigid plane by THREE INDEPENDENT PROJECTIONS --
// `⟨piston,u⟩/A`, `⟨tiltS,u⟩/‖tiltS‖²`, `⟨tiltY,u⟩/‖tiltY‖²` -- which is the least-squares fit
// if and only if the three modes are mutually orthogonal under the face inner product.
//
// `⟨piston,tiltS⟩ = ∫s dA = 0` because the axial range is symmetric, and `⟨tiltS,tiltY⟩ = 0` with
// it. `⟨piston,tiltY⟩ = ∫y dA` is zero only when the face's tributary set is symmetric about its
// own datum -- and a honeycomb face is CORRUGATED, its rooting helices sitting at alternating
// `±d/4` about the `3d/2` ladder, so the gap sequence is `d, 2d, d, 2d, …`. That sequence is
// palindromic when the raster-row count `m` is EVEN and is not when `m` is ODD.
//
// Every block this corpus has graded has `m = 10`. The `15 × 4` block has `m = 15`, and there a
// perfectly UNIFORM solved field -- the standing falsifier's own state -- reports `0.062` of the
// stroke as dishing, because the fit hands part of a constant to the tilt-Y mode and calls the
// remainder curvature. `CH-0282`.
// -------------------------------------------------------------------------------------------

/**
 * The three rigid face modes of [lattice] and their Gram matrix under its own face inner product.
 *
 * Built **once per lattice**: the Gram is a property of the geometry and not of a load case, so
 * an influence bank of several hundred fields pays for it once.
 */
class FaceRigidBasis(private val lattice: HoneycombGrillage) {

    /** `piston`, `tiltS`, `tiltY`, in that order. */
    val modes: List<F64Array> =
        listOf(lattice.pistonMode, lattice.tiltSMode, lattice.tiltYMode)

    /** `G[i][j] = ⟨mode_i, mode_j⟩` over the face, in nm². */
    val gram: List<List<Double>> =
        modes.map { a -> modes.map { b -> lattice.areaInnerProduct(a, b) } }

    /**
     * The largest off-diagonal of [gram] relative to the geometric mean of its two diagonals —
     * `0` for an orthogonal basis, and the size of the defect for one that is not.
     */
    val worstNonOrthogonality: Double = (0..2).flatMap { i ->
        (0..2).mapNotNull { j ->
            if (i == j) null else abs(gram[i][j]) / sqrt(gram[i][i] * gram[j][j])
        }
    }.max()

    /** Whether the three modes are mutually orthogonal to within `1e-12`. */
    val modesAreOrthogonal: Boolean = worstNonOrthogonality < 1e-12

    /** Whether [other] is the lattice this basis was built on — an identity, not an equality. */
    fun belongsTo(other: HoneycombGrillage): Boolean = other === lattice

    /** [field] with its **least-squares** rigid plane removed, in this inner product. */
    fun dishingOf(field: HoneycombDeflection): CorrectedFaceDishing {
        val rhs = modes.map { lattice.areaInnerProduct(it, field.coefficients) }
        val c = solveSymmetricThreeByThree(gram, rhs)
        val residual = field.coefficients.copy()
        for (i in 0..2) residual -= modes[i] * c[i]
        return CorrectedFaceDishing(lattice, field, residual, c)
    }
}

/**
 * A face field with its least-squares rigid plane removed — `HoneycombDeflection.dishing` where
 * the three modes are orthogonal, and the corrected reading where they are not.
 */
class CorrectedFaceDishing internal constructor(
    private val lattice: HoneycombGrillage,
    private val field: HoneycombDeflection,
    val coefficients: F64Array,

    /** The fitted `(piston, tiltS, tiltY)` coefficients. */
    val rigidCoefficients: List<Double>
) : DishingSolution {

    override fun deflectionAt(x: Double, y: Double): Double = field.deflection(x, y)

    override fun dishingAt(x: Double, y: Double): Double = lattice.evaluate(coefficients, x, y)

    /** The largest absolute corrected dishing over a [samples] × [samples] grid, in nm. */
    fun peakDishing(samples: Int = 81): Double {
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
        var peak = 0.0
        for (i in 0 until samples) {
            val s = -lattice.lengthS / 2.0 + lattice.lengthS * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -lattice.lengthY / 2.0 + lattice.lengthY * j / (samples - 1)
                peak = max(peak, abs(dishingAt(s, y)))
            }
        }
        return peak
    }
}

/** A symmetric positive-definite `3 × 3` solve, by elimination with partial pivoting. */
internal fun solveSymmetricThreeByThree(
    matrix: List<List<Double>>,
    rhs: List<Double>
): List<Double> {
    require(matrix.size == 3 && matrix.all { it.size == 3 }) { "the matrix must be 3 x 3" }
    require(rhs.size == 3) { "the right-hand side must carry three entries" }
    val a = Array(3) { i -> DoubleArray(4) { j -> if (j < 3) matrix[i][j] else rhs[i] } }
    for (column in 0..2) {
        var pivot = column
        for (row in column + 1..2) if (abs(a[row][column]) > abs(a[pivot][column])) pivot = row
        require(abs(a[pivot][column]) > 0.0) { "the matrix is singular at column $column" }
        val swap = a[column]; a[column] = a[pivot]; a[pivot] = swap
        for (row in 0..2) {
            if (row == column) continue
            val factor = a[row][column] / a[column][column]
            for (j in column..3) a[row][j] -= factor * a[column][j]
        }
    }
    return (0..2).map { a[it][3] / a[it][it] }
}

/** The two dishing conventions over one lattice and one grid, out of **one** set of solves. */
class DualConventionSurrogates internal constructor(

    /** `HoneycombDeflection.dishing` — the reading `C-0167`, `C-0180` and `C-0208` published. */
    val standing: InfluenceSurrogate,

    /** The least-squares reading, which annihilates a uniform field at every `m`. */
    val corrected: InfluenceSurrogate
)

/**
 * `C-0058`'s exact Woodbury coupling surrogate over [lattice], in **both** dishing conventions.
 *
 * The lattice solves are the expensive part and they are shared, so the second convention costs
 * one extra `81 × 81` sampling per influence function and no factorisation. `C-0104`'s rule is
 * kept structural: the free field is taken on the lattice **as built** and every influence
 * function on `withoutPrestrain`.
 *
 * [DualConventionSurrogates.standing] is `honeycombTiedSurrogate` bit for bit, which is asserted
 * rather than argued — so nothing `C-0180` or `C-0208` published can move, and the corrected
 * reading travels beside it rather than instead of it (`C-0092`).
 */
fun crossSectionSurrogates(
    lattice: HoneycombGrillage,
    basis: FaceRigidBasis,
    grid: List<Pair<Double, Double>>,
    pressure: PressureField,
    samples: Int = 81
): DualConventionSurrogates {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(basis.belongsTo(lattice)) { "the basis must be the lattice's own" }
    val structure = lattice.withoutPrestrain
    val free = lattice.solve(pressure)
    val influence = grid.map { (s, y) ->
        structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
    }
    fun build(adapt: (HoneycombDeflection) -> DishingSolution) = influenceSurrogate(
        grid, lattice.lengthS / 2.0, lattice.lengthY / 2.0, samples,
        adapt(free), influence.map(adapt)
    )
    return DualConventionSurrogates(
        standing = build { field ->
            object : DishingSolution {
                override fun deflectionAt(x: Double, y: Double) = field.deflection(x, y)
                override fun dishingAt(x: Double, y: Double) = field.dishing(x, y)
            }
        },
        corrected = build { basis.dishingOf(it) }
    )
}
