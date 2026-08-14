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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.CrossoverHingeFlexure
import com.xemantic.nano.plentyofroom.anchoring.FlexureEndCondition
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.TransverseDuplexFlexure
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.offsetForPreload
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.synthesis.perPathSecantCeiling
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-122` — can a **5:1 per-path coupling stiffness ratio** be BUILT?
 *
 * Every test is named for the verification gate it discharges. The disciplines from
 * `CLAUDE.md` that govern this file:
 *
 * - **a quantised design variable is a trap only if its quantum is comparable with the
 *   requirement** — `C-0023`'s mounting offset asked for 0.041 nm against a 0.34 nm quantum and
 *   failed by 9.3×. Gate 5 re-derives that number so this task's answer is graded against the
 *   one case in the corpus where the same question came out negative;
 * - **exit a bisection on the BRACKET WIDTH, never on a residual** — the scatter threshold does,
 *   and it reports its own bracket;
 * - **never emit `Infinity` or `NaN`**: a scatter threshold that is never reached is reported as
 *   *not reached* with the scan's own ceiling, not as an infinite tolerance;
 * - a ladder is **enumerated**, so nothing here has a convergence parameter except the dishing
 *   sampling grid and the threshold bracket.
 */
class BuildableStiffnessRatioTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    /** `C-0017`'s mandate, as a SUM — 33.3333 pN/nm. */
    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0015`'s 3 × 15 grid, on which `C-0058`'s flat design lives. */
    private val grid = attachmentGrid(3, duplexes, Gen1Tile.EDGE_X, lengthY)

    /** `C-0058`'s best one-parameter collar. */
    private val collar = 6.7

    /** `C-0028`'s `B2` base under an 8 nm standoff — `C-0030`'s recommended joint. */
    private val flexibility = standoffTipFlexibility(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, 8.0,
        StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness
    )

    private fun coupledPerPathStiffness(span: Double): Double = CoupledJointFlexure(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, span, flexibility
    ).strokeSecantStiffness(Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE)

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 a bending ladder falls as the cube of the span and is dimensionless in its granularity`() {
        val ladder = buildableLadder(
            quantum = DesignQuanta.BASE_PAIR_RISE,
            units = 30..200
        ) { span -> 48.0 * Gen1Tile.DUPLEX_BENDING_RIGIDITY / (span * span * span) }
        val short = ladder.first { it.units == 50 }
        val long = ladder.first { it.units == 100 }
        assert(long.parameter.isCloseTo(2.0 * short.parameter))
        assert((short.stiffness / long.stiffness).isCloseTo(8.0))
    }

    @Test
    fun `gate 1 the power-law granularity is the exponent times the quantum over the parameter`() {
        assert(powerLawGranularity(3.0, 30.0, DesignQuanta.BASE_PAIR_RISE).isCloseTo(0.034))
        assert(powerLawGranularity(2.0, 4.0, DesignQuanta.BASE_PAIR_RISE).isCloseTo(0.17))
    }

    @Test
    fun `gate 1 the exact ladder granularity tends to the power-law bound as the quantum shrinks`() {
        val span = 30.0
        fun granularityAt(quantum: Double): Double {
            val units = (span / quantum).toInt()
            val ladder = buildableLadder(quantum, (units - 2)..(units + 2)) { p ->
                Gen1Tile.DUPLEX_BENDING_RIGIDITY / (p * p * p)
            }
            return nearestBuildable(
                ladder, Gen1Tile.DUPLEX_BENDING_RIGIDITY / (span * span * span)
            ).relativeGranularity
        }
        val coarse = granularityAt(0.34) / powerLawGranularity(3.0, span, 0.34)
        val fine = granularityAt(0.034) / powerLawGranularity(3.0, span, 0.034)
        assert(abs(coarse - 1.0) < 0.06)
        assert(abs(fine - 1.0) < 0.006)
        assert(abs(fine - 1.0) < abs(coarse - 1.0))
    }

    @Test
    fun `gate 1 a hinge stiffness quarters at double the arm`() {
        fun hinge(arm: Double) = CrossoverHingeFlexure(
            Gen1Tile.crossoverHingeStiffness(1.0), arm,
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, 1, 3.0
        ).hingeTermStiffness
        assert((hinge(4.0) / hinge(8.0)).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            buildableLadder(0.0, 1..10) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            buildableLadder(0.34, 10..1) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            buildableLadder(0.34, 0..10) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            nearestBuildable(emptyList(), 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            nearestBuildable(buildableLadder(0.34, 1..10) { 1.0 / it }, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            powerLawGranularity(3.0, 0.0, 0.34)
        }
        assertFailsWith<IllegalArgumentException> {
            populationOverlapScatter(0.5)
        }
        assertFailsWith<IllegalArgumentException> {
            twoLevelStiffnesses(listOf(true, false), 1.0, -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            scatteredStiffnesses(listOf(1.0, 1.0), 1, ScatterPattern.ALTERNATING_ROWS, 1.0)
        }
    }

    // ------------------------------------------------------------------ gate 2, limiting cases

    @Test
    fun `gate 2 a uniform ratio request reproduces C-0017's 45-path design exactly`() {
        val uniform = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, 1.0), mandate
        )
        assert(uniform.size == 45)
        assert(uniform.all { it.isCloseTo(mandate / 45.0) })
        // and the element that realises it is C-0030's own recommended design
        val span = coupledFlexureSpan(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, 45, mandate,
            Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(span.isCloseTo(31.82, 1e-3))
        val perPath = coupledFlexureSpan(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, 1, mandate / 45.0,
            Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(perPath.isCloseTo(span, 1e-9))
    }

    @Test
    fun `gate 2 an infinitely fine quantum returns the target exactly`() {
        val target = 0.921
        val ladder = buildableLadder(1e-5, 2_800_000..2_900_000) { p ->
            92.5 * Gen1Tile.DUPLEX_BENDING_RIGIDITY / (p * p * p)
        }
        val verdict = nearestBuildable(ladder, target)
        assert(verdict.relativeError < 1e-6)
        assert(verdict.bracketed)
    }

    @Test
    fun `gate 2 a ladder of one setting has no granularity and says so`() {
        val ladder = buildableLadder(0.34, 60..60) { p -> 1.0 / p }
        val verdict = nearestBuildable(ladder, 1.0 / 20.0)
        assert(!verdict.bracketed)
        assert(verdict.relativeGranularity == 0.0)
    }

    @Test
    fun `gate 2 zero scatter returns the nominal distribution identically`() {
        val nominal = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, 5.0), mandate
        )
        ScatterPattern.entries.forEach { pattern ->
            val scattered = scatteredStiffnesses(nominal, 3, pattern, 0.0)
            assert(scattered.indices.all { scattered[it] == nominal[it] })
        }
    }

    @Test
    fun `gate 2 a two-level design at a unit ratio is the uniform one`() {
        val mask = rimMask(grid, Gen1Tile.EDGE_X, lengthY, collar)
        val share = mandate / 45.0
        val built = twoLevelStiffnesses(mask, share, share)
        assert(built.all { it.isCloseTo(share) })
    }

    // ------------------------------------------------------- gate 3, symmetry and conservation

    @Test
    fun `gate 3 a two-level design's total is exactly its two counts times its two levels`() {
        val mask = rimMask(grid, Gen1Tile.EDGE_X, lengthY, collar)
        assert(mask.count { it } == 34)
        assert(mask.count { !it } == 11)
        val built = twoLevelStiffnesses(mask, 0.921, 0.184)
        assert(built.sum().isCloseTo(34 * 0.921 + 11 * 0.184))
    }

    @Test
    fun `gate 3 a balanced scatter pattern preserves the total and an unbalanced one does not`() {
        val nominal = List(30) { 1.0 }
        val balanced = scatteredStiffnesses(
            nominal, 2, ScatterPattern.ALTERNATING_COLUMNS, 0.2
        )
        assert(balanced.sum().isCloseTo(nominal.sum()))
        val single = scatteredStiffnesses(nominal, 2, ScatterPattern.SINGLE_OUTLIER, 0.2)
        assert(!single.sum().isCloseTo(nominal.sum()))
        assert(single.sum().isCloseTo(nominal.sum() + 0.2))
    }

    @Test
    fun `gate 3 the total's relative granularity is the per-path one divided by the path count`() {
        val perPathStiffness = mandate / 45.0
        val step = 0.04 * perPathStiffness
        assert(
            relativeTotalGranularity(step, mandate)
                .isCloseTo(0.04 * perPathStiffness / mandate)
        )
        assert(relativeTotalGranularity(step, mandate).isCloseTo(0.04 / 45.0))
    }

    @Test
    fun `gate 3 a point-reflected two-level design dishes identically on the lattice`() {
        val lattice = OrigamiGrillage(
            sheet = sheet,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = duplexes,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
            subdivisions = 2
        )
        val load = uniformPressure(interiorPressure)
        val surrogate = latticeInfluenceSurrogate(lattice, grid, load, 41)
        val mask = rimMask(grid, Gen1Tile.EDGE_X, lengthY, collar)
        val built = twoLevelStiffnesses(mask, 0.921, 0.184)
        val direct = surrogate.solve(built).peakDishing
        val reflected = surrogate.solve(built.reversed()).peakDishing
        assert(reflected.isCloseTo(direct, 1e-9))
    }

    @Test
    fun `gate 3 the mandate trim never worsens the total and never moves a path far`() {
        val ladder = buildableLadder(DesignQuanta.BASE_PAIR_RISE, 40..200) {
            coupledPerPathStiffness(it)
        }
        val mask = rimMask(grid, Gen1Tile.EDGE_X, lengthY, collar)
        val nominal = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, 5.0), mandate
        )
        val trimmed = trimmedToTotal(nominal, ladder, mandate)
        assert(trimmed.settings.size == 45)
        assert(trimmed.relativeError <= trimmed.relativeErrorBeforeTrimming)
        assert(trimmed.total.isCloseTo(trimmed.settings.sumOf { it.stiffness }))
        // no path is more than one rung from its own nearest
        trimmed.settings.forEachIndexed { index, setting ->
            val nearest = nearestBuildable(ladder, nominal[index]).nearest
            assert(abs(setting.units - nearest.units) <= 1)
        }
        // and the two levels are still distinguishable after the trim
        val rim = trimmed.settings.filterIndexed { i, _ -> mask[i] }.minOf { it.stiffness }
        val interior = trimmed.settings.filterIndexed { i, _ -> !mask[i] }.maxOf { it.stiffness }
        assert(rim > interior)
    }

    @Test
    fun `gate 2 the mandate trim on an infinitely fine ladder returns the targets`() {
        // 20 to 60 nm at 1e-4 nm, so that BOTH levels lie strictly inside the ladder
        val ladder = buildableLadder(1e-4, 200_000..600_000) { p ->
            92.5 * Gen1Tile.DUPLEX_BENDING_RIGIDITY / (p * p * p)
        }
        val nominal = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, 5.0), mandate
        )
        val trimmed = trimmedToTotal(nominal, ladder, mandate)
        assert(trimmed.relativeError < 1e-6)
    }

    @Test
    fun `gate 1 the mandate trim refuses unphysical arguments`() {
        val ladder = buildableLadder(0.34, 1..10) { 1.0 / it }
        assertFailsWith<IllegalArgumentException> { trimmedToTotal(emptyList(), ladder, 1.0) }
        assertFailsWith<IllegalArgumentException> { trimmedToTotal(listOf(1.0), ladder, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            trimmedToTotal(listOf(1.0), ladder, 1.0, maximumSteps = -1)
        }
        assertFailsWith<IllegalArgumentException> { trimmedToTotal(listOf(-1.0), ladder, 1.0) }
    }

    @Test
    fun `gate 2 the realised ratio of two LINEAR elements is constant over the stroke`() {
        val strokes = listOf(0.5, 1.0, 3.0, 10.0)
        val linear = realisedSecantRatio({ 0.921 }, { 0.184 }, strokes)
        assert(linear.all { it.isCloseTo(0.921 / 0.184) })
        // and the coupled flexure's is NOT — CH-0042's strain softening, on two different spans
        val coupled = realisedSecantRatio(
            { s ->
                CoupledJointFlexure(Gen1Tile.DUPLEX_BENDING_RIGIDITY, 29.58, flexibility)
                    .strokeSecantStiffness(s, FlexureOrientation.FAVOURABLE)
            },
            { s ->
                CoupledJointFlexure(Gen1Tile.DUPLEX_BENDING_RIGIDITY, 52.36, flexibility)
                    .strokeSecantStiffness(s, FlexureOrientation.FAVOURABLE)
            },
            strokes
        )
        assert(!coupled.last().isCloseTo(coupled.first(), 1e-3))
        assert(coupled.last() < coupled.first())
    }

    @Test
    fun `gate 1 the realised ratio refuses an empty or non-positive stroke list`() {
        assertFailsWith<IllegalArgumentException> {
            realisedSecantRatio({ 1.0 }, { 1.0 }, emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            realisedSecantRatio({ 1.0 }, { 1.0 }, listOf(0.0))
        }
        assertFailsWith<IllegalArgumentException> {
            realisedSecantRatio({ 1.0 }, { -1.0 }, listOf(1.0))
        }
    }

    // ------------------------------------------------------------ gate 4, numerical convergence

    @Test
    fun `gate 4 the scatter threshold exits on its bracket width and never returns infinity`() {
        val threshold = scatterThreshold(maximum = 1.0, scanSteps = 32, limit = 0.5) { it }
        assert(threshold.reachesTheLimit)
        assert(threshold.threshold.isCloseTo(0.5, 1e-6))
        assert(threshold.bracketWidth < 1e-6)
        assert(threshold.threshold.isFinite())
    }

    @Test
    fun `gate 4 a threshold that is never reached is reported as not reached at the ceiling`() {
        val threshold = scatterThreshold(maximum = 1.0, scanSteps = 32, limit = 5.0) { it }
        assert(!threshold.reachesTheLimit)
        assert(threshold.threshold.isCloseTo(1.0))
        assert(threshold.threshold.isFinite())
    }

    @Test
    fun `gate 4 the threshold takes the FIRST crossing and does not assume monotonicity`() {
        // a metric that crosses the limit, falls back below it, and crosses again
        val threshold = scatterThreshold(maximum = 3.0, scanSteps = 64, limit = 1.0) { e ->
            if (e < 1.5) e else 3.0 - e
        }
        assert(threshold.reachesTheLimit)
        assert(threshold.threshold.isCloseTo(1.0, 1e-5))
    }

    // ------------------------------------------------------- gate 5, upstream cross-check

    @Test
    fun `gate 5 C-0058's rim design is 0_921 and 0_184 pN per nm at the mandated total`() {
        val built = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collar, 5.0), mandate
        )
        val mask = rimMask(grid, Gen1Tile.EDGE_X, lengthY, collar)
        val rim = built.filterIndexed { index, _ -> mask[index] }.distinct().single()
        val interior = built.filterIndexed { index, _ -> !mask[index] }.distinct().single()
        assert(rim.isCloseTo(0.921, 1e-3))
        assert(interior.isCloseTo(0.184, 2e-3))
        assert((rim / interior).isCloseTo(5.0))
    }

    @Test
    fun `gate 5 C-0023's preload quantum is the one case where quantisation defeated a requirement`() {
        val offset = offsetForPreload(
            preload = com.xemantic.nano.plentyofroom.thermalEnergy() / 3.0,
            targetForce = Gen1Tile.TARGET_FORCE,
            targetStroke = Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(offset.isCloseTo(0.0409, 2e-3))
        assert((DesignQuanta.BASE_PAIR_RISE / offset).isCloseTo(8.3, 2e-2))
    }

    @Test
    fun `gate 5 C-0049's per-path ceiling and C-0058's admissible ratio are reproduced`() {
        assert(
            perPathSecantCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, 45, Gen1Tile.ACCEPTABLE_STROKE)
                .isCloseTo(150.0)
        )
        assert(
            admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, 45
            ).isCloseTo(4.5)
        )
    }

    @Test
    fun `gate 5 C-0023's own two elements are reproduced at their published design parameters`() {
        val flexure = TransverseDuplexFlexure(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, 24.61, FlexureEndCondition.PINNED_ENDS, false
        )
        assert((45.0 * flexure.bendingStiffness).isCloseTo(33.333, 2e-3))
        val hinge = CrossoverHingeFlexure(
            Gen1Tile.crossoverHingeStiffness(1.0), 4.11,
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, 1, 3.0
        )
        assert((45.0 * hinge.stiffness).isCloseTo(33.333, 3e-3))
    }

    @Test
    fun `gate 5 the population overlap threshold is the ratio's own R minus one over R plus one`() {
        assert(populationOverlapScatter(5.0).isCloseTo(2.0 / 3.0))
        assert(populationOverlapScatter(1.0).isCloseTo(0.0))
        assert(populationOverlapScatter(20.0).isCloseTo(19.0 / 21.0))
    }

    // ------------------------------------------------------------ the cheap bound, as a test

    @Test
    fun `the cheap bound the falsifier is written on the granularity is far inside the flat window`() {
        val rimSpan = coupledFlexureSpan(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, 1, 0.921,
            Gen1Tile.ACCEPTABLE_STROKE
        )
        val interiorSpan = coupledFlexureSpan(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, 1, 0.184,
            Gen1Tile.ACCEPTABLE_STROKE
        )
        val rimGranularity = powerLawGranularity(3.0, rimSpan, DesignQuanta.BASE_PAIR_RISE)
        val interiorGranularity =
            powerLawGranularity(3.0, interiorSpan, DesignQuanta.BASE_PAIR_RISE)
        // one step of either parameter moves the ratio by far less than the flat window's width
        val ratioStep = rimGranularity + interiorGranularity
        assert(ratioStep < 0.10)
        // and the realised stiffness at the rounded setting is inside half a step of the target
        val rim = nearestBuildable(
            buildableLadder(DesignQuanta.BASE_PAIR_RISE, 40..200) { coupledPerPathStiffness(it) },
            0.921
        )
        assert(rim.relativeError <= 0.5 * rim.relativeGranularity * 1.001)
    }
}
