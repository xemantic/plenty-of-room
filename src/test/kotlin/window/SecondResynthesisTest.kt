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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-118` — the five gates of the second window re-synthesis.
 *
 * Same discipline as `T-25`'s `ResynthesisTransferTest`: **nothing here is re-derived**, so
 * gate 5 is not decoration but the substance — every number this task carries is reproduced
 * from the result file of the study that emitted it, and the departure is asserted.
 */
class SecondResynthesisTest {

    private val resultsDirectory = File("gpd/results")

    private val inputs = ResynthesisInputs.read(resultsDirectory)

    private val second = SecondResynthesisInputs.read(resultsDirectory)

    // --- gate 1, dimensional consistency ------------------------------------------------

    @Test
    fun `gate 1 - a stroke ceiling is a length and is homogeneous of degree one in the layer`() {
        // at fixed volume fraction the dry thickness is a fixed FRACTION of the layer, so
        // both of C-0050's ceilings scale exactly with it
        val ceiling = kinematicStrokeCeiling(10.0, 0.05)
        assert(ceiling.isCloseTo(9.5, 1e-12))
        assert(kinematicStrokeCeiling(20.0, 0.05).isCloseTo(2.0 * ceiling, 1e-12))
        val validity = validityStrokeCeiling(10.0, 0.05, 0.2)!!
        assert(validity.isCloseTo(7.5, 1e-12))
        assert(validityStrokeCeiling(20.0, 0.05, 0.2)!!.isCloseTo(2.0 * validity, 1e-12))
    }

    @Test
    fun `gate 1 - a per-path secant ceiling times its own stroke is a force, identically`() {
        // C-0049's identity: the ceiling is a bound on a FORCE, so as a stiffness it is one
        // power of the stroke away and the product is the force back again
        listOf(1.0, 3.0, 4.5, 10.0).forEach { stroke ->
            listOf(8, 15, 34, 45).forEach { count ->
                assert(
                    (perPathSecantCeiling(10.0, count, stroke) * stroke)
                        .isCloseTo(count * 10.0, 1e-12)
                )
            }
        }
    }

    @Test
    fun `gate 1 - the declared ceiling is exactly 1_2 times the mandate and carries its stroke`() {
        assert(declaredComplianceCeilingFromMandate(100.0, 3.0).isCloseTo(40.0, 1e-12))
        assert(declaredComplianceCeilingFromMandate(100.0, 10.0).isCloseTo(12.0, 1e-12))
        assert(declaredComplianceCeilingFromMandate(200.0, 3.0).isCloseTo(80.0, 1e-12))
    }

    @Test
    fun `gate 1 - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { kinematicStrokeCeiling(-1.0, 0.05) }
        assertFailsWith<IllegalArgumentException> { kinematicStrokeCeiling(10.0, 1.5) }
        assertFailsWith<IllegalArgumentException> { validityStrokeCeiling(10.0, 0.05, 0.0) }
        assertFailsWith<IllegalArgumentException> { perPathSecantCeiling(10.0, 0, 3.0) }
        assertFailsWith<IllegalArgumentException> { perPathSecantCeiling(10.0, 45, 0.0) }
        assertFailsWith<IllegalArgumentException> { compressedVolumeFraction(0.05, 10.0, 10.0) }
        assertFailsWith<IllegalArgumentException> {
            foldTangentIncrement(-1.0, 0.018, 26.0, 0.9, 24.0, MANDATED_COUPLING_STIFFNESS)
        }
    }

    // --- gate 2, limiting cases ---------------------------------------------------------

    @Test
    fun `gate 2 - a vanishing layer occupies its whole height and a melt occupies none of it`() {
        assert(kinematicStrokeCeiling(10.0, 0.0).isCloseTo(10.0, 1e-12))
        assert(abs(kinematicStrokeCeiling(10.0, 1.0)) < 1e-12)
        // the validity ceiling tends to the kinematic one as the crossover tends to a melt
        assert(
            validityStrokeCeiling(10.0, 0.05, 1.0)!!
                .isCloseTo(kinematicStrokeCeiling(10.0, 0.05), 1e-12)
        )
        // and it does not exist at all where the layer already sits past the crossover
        assert(validityStrokeCeiling(10.0, 0.25, 0.2) == null)
        assert(abs(validityStrokeCeiling(10.0, 0.2, 0.2)!!) < 1e-12)
    }

    @Test
    fun `gate 2 - the three-channel increment vanishes exactly when every channel is off`() {
        val increment = foldTangentIncrement(
            electrostaticForce = 200.0,
            collarLogGradient = 0.0,
            brushStiffnessAtFold = 26.0,
            brushMultiplier = 1.0,
            couplingTangentAtFold = MANDATED_COUPLING_STIFFNESS,
            mandatedStiffness = MANDATED_COUPLING_STIFFNESS
        )
        assert(abs(increment.collar) < 1e-12)
        assert(abs(increment.fluctuation) < 1e-12)
        assert(abs(increment.softening) < 1e-12)
        assert(abs(increment.total) < 1e-12)
    }

    @Test
    fun `gate 2 - compression concentrates the layer and zero stroke leaves it alone`() {
        assert(compressedVolumeFraction(0.05, 10.0, 0.0).isCloseTo(0.05, 1e-12))
        assert(compressedVolumeFraction(0.05, 10.0, 5.0).isCloseTo(0.10, 1e-12))
    }

    @Test
    fun `gate 2 - the identity correction set still reproduces C-0016's own four edges`() {
        val baseline = resynthesisedWindows(inputs, CorrectionSet.IDENTITY)
        assert(baseline.single { it.layerHeight == 5.0 }.empty)
        val seven = baseline.single { it.layerHeight == 7.0 }
        assert(seven.lowestGraftingDensity!!.isCloseTo(0.029552, 1e-3))
        assert(seven.highestGraftingDensity!!.isCloseTo(0.049602, 1e-3))
        val ten = baseline.single { it.layerHeight == 10.0 }
        assert(ten.lowestGraftingDensity!!.isCloseTo(0.011634, 1e-3))
        assert(ten.highestGraftingDensity!!.isCloseTo(0.260150, 1e-3))
    }

    // --- gate 3, symmetry and conservation ----------------------------------------------

    @Test
    fun `gate 3 - the three channels are additive, and each carries its own sign`() {
        val increment = foldTangentIncrement(
            electrostaticForce = 200.0,
            collarLogGradient = 0.018,
            brushStiffnessAtFold = 26.0,
            brushMultiplier = 0.90584,
            couplingTangentAtFold = 24.0,
            mandatedStiffness = MANDATED_COUPLING_STIFFNESS
        )
        assert(
            increment.total
                .isCloseTo(increment.collar + increment.fluctuation + increment.softening, 1e-12)
        )
        assert(increment.collar.isCloseTo(200.0 * 0.018, 1e-12))
        assert(increment.fluctuation < 0.0)
        assert(increment.softening < 0.0)
    }

    @Test
    fun `gate 3 - the intersection is order independent under the full correction set`() {
        val forward = resynthesisedWindows(inputs, CorrectionSet.FULL)
        val reversed =
            resynthesisedWindows(inputs, CorrectionSet.FULL, reverseConstraintOrder = true)
        forward.zip(reversed).forEach { (a, b) ->
            assert(a.empty == b.empty)
            assert(a.lowestIndex == b.lowestIndex)
            assert(a.highestIndex == b.highestIndex)
        }
    }

    @Test
    fun `gate 3 - the polymer volume per unit area is conserved under compression`() {
        // phi h = N sigma v0 is the conserved quantity, and it is what makes both of
        // C-0050's ceilings statements about the SAME number at every compression
        listOf(0.0, 1.0, 3.0, 6.0).forEach { stroke ->
            val phi = compressedVolumeFraction(0.05, 10.0, stroke)
            assert((phi * (10.0 - stroke)).isCloseTo(0.05 * 10.0, 1e-12))
        }
    }

    // --- gate 4, numerical convergence --------------------------------------------------

    @Test
    fun `gate 4 - every window edge is a grid point located to one grid ratio and no better`() {
        val grid = inputs.graftingDensityGrid
        assert(grid.size == 61)
        grid.zipWithNext { low, high -> assert((high / low).isCloseTo(1.10913, 1e-4)) }
    }

    @Test
    fun `gate 4 - a movement below the grid ratio is reported as sub-grid, not as zero`() {
        val grid = inputs.graftingDensityGrid
        assert(edgeMovementInGridSteps(grid[10], grid[10], grid) == 0)
        assert(edgeMovementInGridSteps(grid[10], grid[11], grid) == 1)
        assert(edgeMovementInGridSteps(grid[14], grid[10], grid) == -4)
        // a value between two grid points rounds to the nearer and never invents a step
        assert(edgeMovementInGridSteps(grid[10], grid[10] * 1.02, grid) == 0)
    }

    @Test
    fun `gate 4 - the sigma-resolved candidates are evaluated at every point of the grid`() {
        val ceilings = strokeCeilingsAcrossTheWindow(inputs, second.crossoverFractions)
        assert(
            ceilings.count { it.layerHeight == 10.0 } == 61 * second.crossoverFractions.size
        )
        ceilings.forEach { record ->
            assert(record.kinematicCeiling < record.layerHeight)
            assert(record.kinematicCeiling > 0.0)
        }
    }

    // --- gate 5, literature and upstream cross-check ------------------------------------

    @Test
    fun `gate 5 - C-0027's own published window edges reproduce from its result file`() {
        val published = second.publishedWindows.filter { it.corrections == "T-25 re-synthesis" }
        assert(published.size == 3)
        assert(published.single { it.layerHeight == 5.0 }.empty)
        assert(
            published.single { it.layerHeight == 7.0 }.highestGraftingDensity!!
                .isCloseTo(0.049602, 1e-3)
        )
        assert(
            published.single { it.layerHeight == 10.0 }.highestGraftingDensity!!
                .isCloseTo(0.288540, 1e-3)
        )
    }

    @Test
    fun `gate 5 - the re-run on the repaired solver moves no edge of C-0027's six windows`() {
        val reproductions = windowEdgeReproductions(inputs, second)
        assert(reproductions.size == 6)
        reproductions.forEach {
            assert(it.movedGridSteps == 0)
            assert(!it.ownerChanged)
        }
    }

    @Test
    fun `gate 5 - C-0033's collar-only fold tangent reproduces from T-60's own file`() {
        val folds = second.collarDecompositions
            .filter { it.state == "10 nm / 2 mM" && it.loadLine == "coupled" }
        assert(folds.size == 6)
        assert(folds.minOf { it.foldTangentCollarOnly!! }.isCloseTo(2.604, 1e-3))
        assert(folds.maxOf { it.foldTangentCollarOnly!! }.isCloseTo(4.994, 1e-3))
        // and the fluctuation channel is exactly what T-60 already carries beside it
        folds.forEach { record ->
            assert(record.foldTangentCollarAndFluctuation!! < record.foldTangentCollarOnly!!)
        }
    }

    @Test
    fun `gate 5 - C-0032's realised element reproduces from C-0030's own library`() {
        val element = realisedCouplingLaw(pathCount = 45)
        assert(element.assembledTangent(3.0).isCloseTo(25.227, 1e-4))
        assert(element.assembledSecant(3.0).isCloseTo(MANDATED_COUPLING_STIFFNESS, 1e-6))
        assert(element.span.isCloseTo(31.821, 1e-4))
    }

    @Test
    fun `gate 5 - C-0041's buildable path count is read from its own design table`() {
        assert(second.packingLimitedPaths == 15)
        // and at that count §3's acceptable clause is inside the per-path allowable while
        // its desired clause is not — C-0049's ceiling, evaluated at C-0041's own count
        assert(perPathSecantCeiling(UNZIP_ALLOWABLE_PN, 15, ACCEPTABLE_STROKE_NM) > MANDATED_COUPLING_STIFFNESS)
        assert(perPathSecantCeiling(UNZIP_ALLOWABLE_PN, 15, DESIRED_STROKE_NM) < MANDATED_COUPLING_STIFFNESS)
    }

    @Test
    fun `gate 5 - C-0050's best kinematic ceiling reproduces from T-108's own file`() {
        assert(second.reachRecords.isNotEmpty())
        assert(second.reachRecords.maxOf { it.kinematicCeiling }.isCloseTo(9.790, 1e-3))
        assert(
            second.reachBounds.single { it.name.startsWith("kinematic ceiling") }
                .value.isCloseTo(9.78969263, 1e-8)
        )
    }

    @Test
    fun `gate 5 - a key that does not identify a unique upstream record throws`() {
        assertFailsWith<IllegalArgumentException> {
            second.publishedWindow(layerHeight = 10.0, corrections = "no such correction set")
        }
        assertFailsWith<IllegalArgumentException> {
            second.baselineFold(model = "no such model", state = "10 nm / 2 mM")
        }
    }

    @Test
    fun `gate 5 - the crossover licence departs at the 10 nm upper edge, and says so`() {
        val licences = crossoverLicenceChecks(inputs, second)
        val ten = licences.single { it.layerHeight == 10.0 }
        // the SOLVED layer at the top of C-0027's 10 nm window is far below the crossover
        assert(ten.solvedVolumeFractionAtUpperEdge < 0.2)
        assert(ten.solvedCeilingExists)
        // while C-0003's trial-function models at the same sigma are 2-5x denser and two of
        // them have no validity ceiling at all — so C-0050's bound 3 is not licensed here
        assert(ten.trialFunctionCeilingsMissing > 0)
        assert(ten.ratioHigh > 2.0)
        assert(!ten.licensed)
    }

    @Test
    fun `gate 5 - the composed fold increment is negative at every 10 nm 2 mM fold`() {
        val channels = foldChannels(second)
        assert(channels.size == 6)
        channels.forEach { record ->
            assert(record.increment.collar > 0.0)
            assert(record.increment.fluctuation < 0.0)
            assert(record.increment.softening < 0.0)
            assert(record.increment.total < 0.0)
            assert(!record.foldMovesDeeper)
            // the softening channel alone outweighs the collar channel that was supposed to
            // rescue it — C-0033 and C-0032 published on the same margin and neither carried
            // the other
            assert(abs(record.increment.softening) > abs(record.increment.collar))
        }
    }
}
