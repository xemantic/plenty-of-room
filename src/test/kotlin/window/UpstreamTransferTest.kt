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
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import java.io.File
import kotlin.math.PI
import kotlin.math.pow
import kotlin.test.Test

/**
 * `T-2` holds fifteen claims at once, and the only defensible way to do that is to
 * reproduce each one's published figures **from its own result file** before consuming it.
 *
 * Every test here is a gate on a *transfer*: the number this task will carry, checked
 * against the number the claim that produced it published. Where a transfer is not licensed,
 * the test says so and the study reports it as an exposure rather than absorbing it.
 */
class UpstreamTransferTest {

    private val scf = readScfResults(File("gpd/results/T-1d-scf-density-profile.json"))
    private val actuator = readActuatorResults(File("gpd/results/T-3-stroke-and-blocking-force.json"))
    private val layout = readLayoutResults(File("gpd/results/T-14-crossover-phase-and-registration.json"))
    private val peg = PegWater()

    // --- gate 1, dimensional consistency -----------------------------------------------

    @Test
    fun `gate 1 - the grafting spacing is the inverse square root of the grafting density`() {
        scf.designPoints.forEach { point ->
            assert(
                point.graftingSpacing.isCloseTo(
                    point.graftingDensity.pow(-0.5), EMITTED_FIELD_SLACK
                )
            )
        }
    }

    @Test
    fun `gate 1 - coil overlap is a dimensionless area ratio, pi R0 squared times sigma`() {
        // Sigma = pi R0^2 sigma is the number of coils whose footprint covers one coil's:
        // an area times an inverse area. Reproduced from the file's own R0 and sigma, which
        // is what says T-2 is reading the same quantity C-0011 emitted.
        scf.designPoints.forEach { point ->
            point.solved.forEach { response ->
                val overlap = PI * response.idealEndToEnd * response.idealEndToEnd *
                        point.graftingDensity
                assert(response.coilOverlap.isCloseTo(overlap, EMITTED_FIELD_SLACK))
            }
        }
    }

    @Test
    fun `gate 1 - the mean volume fraction is the dry polymer volume per unit area over the height`() {
        // phi = N sigma v0 / h — a volume over a volume. This is the identity that makes the
        // held-point volume fraction a conservation statement rather than a model.
        scf.designPoints.forEach { point ->
            point.solved.forEach { response ->
                val expected = response.monomersPerChain * point.graftingDensity *
                        scf.monomerVolume / point.layerHeight
                assert(response.meanVolumeFraction.isCloseTo(expected, 1e-5))
            }
        }
    }

    @Test
    fun `gate 1 - the layout foundation modulus recovers C-0001's secant stiffness over the nominal tile`() {
        assert(layout.referenceLayerStiffness.isCloseTo(20.201, 1e-4))
        assert(layout.latticeTileArea.isCloseTo(1614.0))
    }

    // --- gate 2, limiting cases ---------------------------------------------------------

    @Test
    fun `gate 2 - the dead-load stroke falls monotonically with grafting density at every height`() {
        // This is what licenses a ONE-SIDED transfer of T-3's stroke clause, which was
        // evaluated at a single sigma per height, onto the lower-sigma part of the window:
        // wherever the stroke clause passes at T-3's own sigma, it passes below it too.
        listOf(5.0, 7.0, 10.0).forEach { height ->
            val strokes = scf.designPoints
                .filter { it.layerHeight == height }
                .sortedBy { it.graftingDensity }
                .map { point -> point.solved.minOf { it.strokeUnderTargetForce } }
            strokes.zipWithNext { low, high -> assert(high < low) }
        }
    }

    @Test
    fun `gate 2 - the secant stiffness rises monotonically with grafting density at every height`() {
        // the same statement read on the stiffness: which is why stability runs the OTHER
        // way across the window from stroke, and why the two edges are owned by different
        // constraints rather than by one
        listOf(5.0, 7.0, 10.0).forEach { height ->
            val stiffnesses = scf.designPoints
                .filter { it.layerHeight == height }
                .sortedBy { it.graftingDensity }
                .map { point -> point.solved.minOf { it.secantStiffness } }
            stiffnesses.zipWithNext { low, high -> assert(high > low) }
        }
    }

    @Test
    fun `gate 2 - a taller layer at the same grafting density gives a longer stroke`() {
        val byHeight = listOf(5.0, 7.0, 10.0).map { height ->
            scf.designPoints.first { it.layerHeight == height && it.graftingDensity == 0.002 }
                .solved.minOf { it.strokeUnderTargetForce }
        }
        byHeight.zipWithNext { thin, thick -> assert(thick > thin) }
    }

    @Test
    fun `gate 2 - salt exclusion vanishes as the layer becomes pure water and grows with polymer`() {
        assert(saltPartitioning(1e-9, peg).saltPartitionCoefficient.isCloseTo(1.0, 1e-6))
        assert(saltPartitioning(1e-9, peg).debyeLengthRatio.isCloseTo(1.0, 1e-6))
        val dilute = saltPartitioning(0.005, peg)
        val dense = saltPartitioning(0.05, peg)
        assert(dense.saltPartitionCoefficient < dilute.saltPartitionCoefficient)
        assert(dense.debyeLengthRatio > dilute.debyeLengthRatio)
        // the sign of C-0005's answer, which is the opposite of section 4(c)'s premise
        assert(dense.saltPartitionCoefficient < 1.0)
        assert(dense.debyeLengthRatio > 1.0)
    }

    @Test
    fun `gate 2 - a stiffer foundation lowers the peak per-load-path force`() {
        val soft = loadPathForce(layout.foundationStates, ANCHOR_LOAD_CASE, 0.3)
        val stiff = loadPathForce(layout.foundationStates, ANCHOR_LOAD_CASE, 3.0)
        assert(stiff.bestLayoutForce < soft.bestLayoutForce)
        assert(stiff.worstLayoutForce < soft.worstLayoutForce)
        assert(soft.insideSweptRange && stiff.insideSweptRange)
        assert(!loadPathForce(layout.foundationStates, ANCHOR_LOAD_CASE, 0.1).insideSweptRange)
    }

    @Test
    fun `gate 2 - the held volume fraction reduces to the resting one at zero compression`() {
        assert(heldVolumeFraction(0.009, 10.0, 10.0).isCloseTo(0.009))
        assert(heldVolumeFraction(0.009, 10.0, 5.0).isCloseTo(0.018))
    }

    // --- gate 3, symmetry, conservation and invariance -----------------------------------

    @Test
    fun `gate 2 - P-5's stretching criterion is exactly vacuous against a force-onset height`() {
        // CH-0010 / P-5 re-opened: L0/R0 >= 1 cannot bound a window from below once L0 is an
        // ONSET height, because it then measures how far into the coil's tail the threshold
        // sits. Asserted over the whole 183-point grid rather than argued: it admits every
        // single point, including layers at Sigma = 0.06 that are carpets of mushrooms.
        scf.designPoints.forEach { point ->
            point.solved.forEach { assert(it.stretchingRatio >= 1.0) }
        }
        assert(scf.designPoints.minOf { point -> point.solved.minOf { it.coilOverlap } } < 0.07)
    }

    @Test
    fun `gate 2 - section 4d cannot bind - the drainage corner clears 1 kHz at every point`() {
        // C-0004 discharged poroelasticity at four labelled design points; this checks the
        // whole grid, on the SLOWEST of its three permeability models and at the solved
        // layer's own volume fraction and secant stiffness.
        val corners = scf.designPoints.map { point ->
            drainageBound(
                layerHeight = point.layerHeight,
                polymerVolumeFraction = point.solved.maxOf { it.meanVolumeFraction },
                layerStiffness = point.solved.minOf { it.secantStiffness },
                footprint = RectangularFootprint(40.0, 40.0),
                peg = peg
            ).cornerFrequency
        }
        assert(corners.min() > 1000.0)
        // and by a wide margin, so the verdict does not sit on the threshold
        assert(corners.min() > 20_000.0)
    }

    @Test
    fun `gate 2 - section 4c cannot bind and its sign is the opposite of the question's`() {
        // §4(c) asks how much ion inclusion the layer gives. C-0005's answer is EXCLUSION,
        // so the constraint is one-sided and is satisfied identically across the grid.
        scf.designPoints.forEach { point ->
            val partitioning = saltPartitioning(
                point.solved.maxOf { it.meanVolumeFraction }, peg
            )
            assert(partitioning.saltPartitionCoefficient <= 1.0)
            assert(partitioning.debyeLengthRatio >= 1.0)
        }
    }

    @Test
    fun `gate 3 - no design point extrapolates C-0015's foundation sweep, and none reaches unzip`() {
        val reference = layout.referenceLayerStiffness
        scf.designPoints.forEach { point ->
            val multiplier = point.solved.minOf { it.secantStiffness } / reference
            val force = loadPathForce(layout.foundationStates, ANCHOR_LOAD_CASE, multiplier)
            // silent extrapolation is the failure mode this guards: C-0015 swept x[0.25, 4]
            assert(force.insideSweptRange)
            assert(force.bestLayoutForce < layout.unzipAllowableLower)
        }
    }

    @Test
    fun `gate 3 - the bias for 100 pN of blocking force is exactly independent of the layer model`() {
        // T-2's cheap bound, run before any intersection and CHECKED rather than asserted:
        // F_es is a property of the tile, the electrode, the buffer and the gap, and the gap
        // IS the layer height. So the blocking clause cannot be moved by grafting density —
        // and where it fails, it fails across the whole sigma window at that height.
        actuator.thresholds
            .groupBy { it.layerHeight to it.concentration }
            .forEach { (_, records) ->
                val biases = records.mapNotNull { it.biasForHundredPiconewtonBlocking }
                assert(biases.isEmpty() || biases.size == records.size)
                biases.forEach { assert(it.isCloseTo(biases.first(), 1e-12)) }
            }
    }

    @Test
    fun `gate 3 - the interpolated load-path force reproduces every sampled foundation state exactly`() {
        layout.foundationStates.forEach { state ->
            val sampled = state.loadClasses.first { it.loadCase == ANCHOR_LOAD_CASE }
            val interpolated = loadPathForce(
                layout.foundationStates, ANCHOR_LOAD_CASE, state.foundationMultiplier
            )
            assert(interpolated.bestLayoutForce.isCloseTo(sampled.jointBestForce, 1e-9))
            assert(interpolated.worstLayoutForce.isCloseTo(sampled.jointWorstForce, 1e-9))
        }
    }

    // --- gate 5, cross-check against every claim this task consumes ----------------------

    @Test
    fun `gate 5 - C-0011's published windows are reproduced from T-1d's own design points`() {
        // sigma in [0.0116, 0.2601] at 10 nm, [0.0296, 0.0496] at 7 nm, empty at 5 nm,
        // under Sigma >= 1 and a 3 nm dead-load stroke — the headline T-2 is built on
        val expected = mapOf(
            10.0 to (0.0116342439 to 0.260149602),
            7.0 to (0.0295517813 to 0.049601845)
        )
        expected.forEach { (height, edges) ->
            val window = windowOnGrid(height)!!
            val grid = gridAt(height)
            assert(window.lowest(grid).isCloseTo(edges.first, EMITTED_FIELD_SLACK))
            assert(window.highest(grid).isCloseTo(edges.second, EMITTED_FIELD_SLACK))
        }
        assert(windowOnGrid(5.0) == null)
    }

    @Test
    fun `gate 5 - C-0011's published windows are the same ones T-1d emitted for itself`() {
        // read the other way round: T-1d's own strokeWindows records, which are an
        // independent code path in that study, agree with the intersection performed here
        val emitted = scf.strokeWindows.filter {
            it.profile == "scf" && it.requiredStroke == 3.0 &&
                    it.requiredCoilOverlap == 1.0 && it.requiredStretchingRatio == 1.0
        }
        listOf(7.0, 10.0).forEach { height ->
            val grid = gridAt(height)
            val window = windowOnGrid(height)!!
            val narrowest = emitted.filter { it.layerHeight == height }
                .minBy { it.highestGraftingDensity!! }
            assert(window.lowest(grid).isCloseTo(narrowest.lowestGraftingDensity!!, 1e-9))
            assert(window.highest(grid).isCloseTo(narrowest.highestGraftingDensity!!, 1e-9))
        }
        assert(emitted.filter { it.layerHeight == 5.0 }.all { it.empty })
    }

    @Test
    fun `gate 5 - C-0014's minimum tether lengths are reproduced from the cable relation`() {
        val shear = layout.shearAllowable / LOAD_CONCENTRATION_FACTOR
        val unzip = layout.unzipAllowableLower / LOAD_CONCENTRATION_FACTOR
        assert(minimumTetherLength(3.0, layout.shearAllowable).isCloseTo(10.2, 1e-2))
        assert(minimumTetherLength(3.0, shear).isCloseTo(28.0, 1e-2))
        assert(minimumTetherLength(3.0, unzip).isCloseTo(61.3, 1e-2))
        assert(minimumTetherLength(10.0, shear).isCloseTo(93.3, 1e-2))
        assert(minimumTetherLength(10.0, unzip).isCloseTo(204.0, 1e-2))
    }

    @Test
    fun `gate 5 - C-0015's flatness optimum is read from T-14 rather than transcribed`() {
        val lattice = layout.flatnessMinima.first { it.model == "lattice" }
        assert(lattice.bestAttachments == 45)
        assert(lattice.bestShape == "3 x 15 (columns x rows)")
        assert(lattice.crossovers == 56)
        assert(lattice.squareGridAttachments == 64)
        assert(lattice.attachmentsPerCrossover.isCloseTo(45.0 / 56.0, 1e-6))
        // the design rule: one attachment row per duplex removes the load path entirely
        assert(lattice.bestPeakCrossoverForce == 0.0)
        assert(lattice.bestForcePerAttachment.isCloseTo(100.0 / 45.0, 1e-6))
        // and it is a genuine loosening: 45 attachments against 56 crossovers
        assert(lattice.bestAttachments < lattice.crossovers)
    }

    @Test
    fun `gate 5 - C-0004's drainage corner is reproduced at its own 10 nm design point`() {
        // C-0004 reports 91 kHz at L0 = 10 nm, phi = 0.0289, k = 7.39 pN/nm, 40 x 40 nm
        val bound = drainageBound(
            layerHeight = 10.0,
            polymerVolumeFraction = 0.0288872,
            layerStiffness = 7.39,
            footprint = RectangularFootprint(40.0, 40.0),
            peg = peg
        )
        assert(bound.cornerFrequency.isCloseTo(91_000.0, 0.02))
        assert(bound.marginAtOneKilohertz > 1.0)
    }

    @Test
    fun `gate 5 - C-0005's partition coefficients are reproduced at its own labelled points`() {
        // T-6's "L0 = 10 nm, window lower edge" row: phi = 0.0288872
        val partitioning = saltPartitioning(0.0288872, peg)
        assert(partitioning.saltPartitionCoefficient.isCloseTo(0.7675873793205503, 1e-9))
        assert(partitioning.magnesiumPartitionCoefficient.isCloseTo(0.692757234457628, 1e-9))
        assert(partitioning.debyeLengthRatio.isCloseTo(1.1413953194036115, 1e-9))
        assert(partitioning.effectivePermittivity.isCloseTo(74.97471102435956, 1e-9))
    }

    @Test
    fun `gate 5 - THE TRANSFER LICENCE - the solved layer lands inside C-0003's response bracket`() {
        // This is the gate that decides whether T-3's coupled verdicts — computed on
        // C-0003's six models — may be carried onto C-0011's SCF window at all. CH-0010
        // upholds C-0003's RESPONSE numbers while rejecting its structural ones, and this
        // checks that claim at the two shared design points rather than assuming it.
        listOf(10.0 to 0.024, 7.0 to 0.045, 5.0 to 0.092).forEach { (height, density) ->
            val bracket = actuator.designPoints.filter {
                it.layerHeight == height && it.graftingDensity == density
            }
            assert(bracket.size == 6)
            val nearest = scf.designPoints
                .filter { it.layerHeight == height }
                .minBy { kotlin.math.abs(it.graftingDensity - density) }
            // the SCF grid does not contain T-3's sigma exactly; it is within 6 %
            assert(kotlin.math.abs(nearest.graftingDensity - density) / density < 0.06)
            val solvedStroke = nearest.solved.map { it.strokeUnderTargetForce }
            val bracketStroke = bracket.map { it.strokeUnderHundredPiconewtonDeadLoad }
            if (height == 5.0) {
                // DECLARED FALSIFIER 3 FIRES HERE, and only here. The solved layer strokes
                // 1.87 nm against C-0003's 0.47-1.53 nm: 1.22x above the top of the bracket,
                // so T-3's coupled verdicts at 5 nm rest on a layer the solved profile does
                // not reproduce. Asserted so the exposure is recorded rather than narrated.
                assert(solvedStroke.min() > bracketStroke.max())
                assert(solvedStroke.min() / bracketStroke.max() < 1.3)
            } else {
                assert(solvedStroke.min() >= bracketStroke.min())
                assert(solvedStroke.max() <= bracketStroke.max())
            }
        }
    }

    @Test
    fun `gate 5 - the chain length gap CH-0010 names is present and is a factor of three to six`() {
        // the structural half of CH-0010: C-0003's N does NOT transfer, and the window must
        // state which height convention it is in because of exactly this
        val solved = scf.designPoints
            .filter { it.layerHeight == 10.0 }
            .minBy { kotlin.math.abs(it.graftingDensity - 0.024) }
        val scfChain = solved.solved.map { it.monomersPerChain }.average()
        val modelChain = actuator.designPoints
            .filter { it.layerHeight == 10.0 }.map { it.monomersPerChain }
        assert(scfChain.isCloseTo(62.1, 2e-2))
        assert(modelChain.min() / scfChain > 3.0)
        assert(modelChain.max() / scfChain < 6.5)
    }

    @Test
    fun `gate 4 - the sigma grid is logarithmic, so a window edge is located to one grid ratio`() {
        val grid = gridAt(10.0)
        assert(grid.size == 61)
        val ratios = grid.zipWithNext { low, high -> high / low }
        ratios.forEach { assert(it.isCloseTo(ratios.first(), EMITTED_FIELD_SLACK)) }
        assert(ratios.first().isCloseTo(1.1091, 1e-3))
    }

    // --- helpers, shared with the study so that the gate tests exercise the same code ----

    private fun gridAt(height: Double): List<Double> =
        scf.designPoints.filter { it.layerHeight == height }
            .map { it.graftingDensity }.sorted()

    private fun windowOnGrid(height: Double): GridInterval? {
        val points = scf.designPoints.filter { it.layerHeight == height }
            .sortedBy { it.graftingDensity }
        val overlap = admissibleInterval(
            points.map { point -> point.solved.all { it.coilOverlap >= 1.0 } }
        )
        val stroke = admissibleInterval(
            points.map { point -> point.solved.all { it.strokeUnderTargetForce >= 3.0 } }
        )
        return intersect(overlap, stroke)
    }

}

/**
 * The relative slack an identity checked on an **emitted** field of `T-1d` or `T-2` must be allowed.
 *
 * `P-18`. These gates recompute an identity from numbers *read out of a result file*, so their
 * residual is bounded by the file's **emission precision**, not by the solver's. `T-1d` now emits
 * at `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` — the precision its own height solve determines — so a
 * single field carries up to `5e-6` relative, and an identity combining a grafting density, a
 * squared end-to-end distance and the emitted overlap carries up to `2e-5`. Five is the same
 * bound reached the other way, through the grid ratios.
 *
 * **Asserting tighter than this is not a stronger test, it is a test of the printed digits.**
 * It was `1e-7` before, and it passed only because the file printed three digits past what it
 * determined — which is exactly what `CH-0043` raised and `C-0073` removed.
 */
private const val EMITTED_FIELD_SLACK = 5e-5
