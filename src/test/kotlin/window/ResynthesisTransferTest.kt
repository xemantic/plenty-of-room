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
 * `T-25` re-runs `C-0016`'s window and `C-0017`'s verdict against iteration 4.
 *
 * Every test here is a gate on a **transfer**: the number this task carries, checked against
 * the number the claim that produced it published — reproduced from that claim's own result
 * file rather than transcribed. `C-0026` was caught taking the wrong record because it keyed
 * on too few dimensions, so every reader below keys on **every** dimension its sweep varied
 * and the tests assert that a partial key throws rather than silently returning the first
 * match.
 */
class ResynthesisTransferTest {

    private val inputs = ResynthesisInputs.read(File("gpd/results"))

    // --- gate 1, dimensional consistency -----------------------------------------------

    @Test
    fun `gate 1 - k_es is minus the electrostatic force over its own decay length, at every state`() {
        // C-0017 defines k_es = |F| dln|F|/dh and l = -1/(dln|F|/dh), so k_es = -|F|/l is an
        // IDENTITY. It is the load-bearing one here: it is why a multiplier on |F_es| at a
        // pinned operating point cannot reach k_es at all.
        inputs.couplingRequirements.forEach { record ->
            val implied = -record.electrostaticForceAtTarget / record.forceDecayLengthAtTarget
            // 5e-9 is T-16's own 9-significant-digit serialisation floor, not a physical
            // departure: the identity is exact and what is measured here is the rounding
            assert(implied.isCloseTo(record.electrostaticStiffnessAtTarget, 1e-8))
        }
    }

    @Test
    fun `gate 1 - the edge multiplier is dimensionless and its collar is a quarter of an edge`() {
        // T-3b emits both a force FRACTION and an effective collar width; a collar w on a
        // square tile of edge L is worth 4w/L in force. Reproducing one from the other says
        // T-25 is reading the quantity C-0022 emitted, and fixes the SIGN convention: the
        // emitted fraction is a DEFICIT, so the multiplier is 1 - fraction.
        inputs.edgeProfiles.forEach { profile ->
            val fromCollar = 1.0 + 4.0 * profile.effectiveCollarWidth / GEN1_TILE_EDGE
            assert(fromCollar.isCloseTo(1.0 - profile.edgeForceFractionAdditive, 1e-7))
            assert(edgeForceMultiplier(profile.edgeForceFractionMinMargin) > 0.0)
        }
    }

    @Test
    fun `gate 1 - a stability margin and a window width are both dimensionless ratios`() {
        inputs.couplingRequirements.filter { it.stabilityFloor > 0.0 }.forEach { record ->
            val margin = MANDATED_COUPLING_STIFFNESS / record.stabilityFloor
            assert(margin.isCloseTo(record.stabilityMargin!!, 1e-6))
        }
    }

    @Test
    fun `gate 1 - unphysical arguments throw rather than returning a number`() {
        assertFailsWith<IllegalArgumentException> { descentUnderHoldDown(-1.0, 33.3, 1.0) }
        assertFailsWith<IllegalArgumentException> { descentUnderHoldDown(1.0, -33.3, 1.0) }
        assertFailsWith<IllegalArgumentException> { collarLogGradient(1.0, 1.1, 5.0, 5.0) }
    }

    // --- gate 2, limiting cases ---------------------------------------------------------

    @Test
    fun `gate 2 - with every correction set to identity the re-run reproduces C-0016 exactly`() {
        val baseline = resynthesisedWindows(inputs, CorrectionSet.IDENTITY)
        val published = mapOf(
            7.0 to (0.02955 to 0.04960),
            10.0 to (0.01163 to 0.26015)
        )
        published.forEach { (height, edges) ->
            val window = baseline.first { it.layerHeight == height }
            assert(!window.empty)
            assert(window.lowestGraftingDensity!!.isCloseTo(edges.first, 1e-3))
            assert(window.highestGraftingDensity!!.isCloseTo(edges.second, 1e-3))
        }
        assert(baseline.first { it.layerHeight == 5.0 }.empty)
    }

    @Test
    fun `gate 2 - a zero collar gradient and a zero bias shift leave the stability floor alone`() {
        inputs.couplingRequirements.forEach { record ->
            val corrected = correctedStabilityFloor(
                brushStiffness = record.brushStiffnessAtHeldGap,
                targetForce = record.electrostaticForceAtTarget,
                decayLength = record.forceDecayLengthAtTarget,
                collarLogGradient = 0.0,
                decayLengthShift = 0.0
            )
            assert(corrected.isCloseTo(record.stabilityFloor, 1e-7))
        }
    }

    @Test
    fun `gate 2 - a vanishing descent makes the delivered window the baseline window`() {
        val zeroDescent = resynthesisedWindows(
            inputs, CorrectionSet.IDENTITY.copy(holdDown = HoldDownReading.NONE)
        )
        val baseline = resynthesisedWindows(inputs, CorrectionSet.IDENTITY)
        assert(zeroDescent.map { it.lowestIndex } == baseline.map { it.lowestIndex })
        assert(zeroDescent.map { it.highestIndex } == baseline.map { it.highestIndex })
    }

    @Test
    fun `gate 2 - a larger descent never widens a window`() {
        val heights = listOf(5.0, 7.0, 10.0)
        val light = resynthesisedWindows(
            inputs, CorrectionSet.IDENTITY.copy(holdDown = HoldDownReading.TETHERLESS)
        )
        val heavy = resynthesisedWindows(
            inputs, CorrectionSet.IDENTITY.copy(holdDown = HoldDownReading.TETHERED)
        )
        heights.forEach { height ->
            val a = light.first { it.layerHeight == height }
            val b = heavy.first { it.layerHeight == height }
            if (!a.empty && !b.empty) assert(b.highestIndex!! <= a.highestIndex!!)
        }
    }

    // --- gate 3, symmetry and conservation ----------------------------------------------

    @Test
    fun `gate 3 - the target electrostatic force is independent of the buffer, which is the theorem`() {
        // |F_es| at the operating point is 100 pN + P(g)A: mechanics, with no field in it.
        // That is exactly why CH-0026's multiplier is absorbed into the bias and cannot
        // reach k_es. Asserted across the three buffers at fixed (model, height).
        inputs.couplingRequirements.groupBy { it.model to it.layerHeight }.forEach { (_, group) ->
            val forces = group.map { it.electrostaticForceAtTarget }
            assert(abs(forces.max() - forces.min()) / forces.max() < 1e-5)
        }
    }

    @Test
    fun `gate 3 - the collar gradient is positive at every gap pair, so the correction is favourable`() {
        // mu rises with the gap (the collar is sub-Debye and the tile is finite), so
        // 1/l_2D < 1/l_1D and |k_es| FALLS. CH-0026 predicts the opposite direction; this
        // test is what decides between them, and it is a property of C-0022's own numbers.
        assert(inputs.collarGradients.isNotEmpty())
        inputs.collarGradients.forEach { gradient -> assert(gradient.logGradient > 0.0) }
    }

    @Test
    fun `gate 3 - the intersection does not depend on the order the constraints are applied in`() {
        val forward = resynthesisedWindows(inputs, CorrectionSet.FULL)
        val reversed = resynthesisedWindows(inputs, CorrectionSet.FULL, reverseConstraintOrder = true)
        assert(forward.map { it.lowestIndex } == reversed.map { it.lowestIndex })
        assert(forward.map { it.highestIndex } == reversed.map { it.highestIndex })
    }

    // --- gate 4, numerical convergence ---------------------------------------------------

    @Test
    fun `gate 4 - the collar gradient is bracketed by three difference schemes, not asserted`() {
        inputs.collarGradients.groupBy { it.concentration to it.gapHeight }.forEach { (_, group) ->
            val values = group.map { it.logGradient }
            // the spread between forward, backward and central differences is what is
            // reported as the gradient's uncertainty; it must be finite and it must not
            // straddle zero, or the direction of the correction would be undecided
            assert(values.min() > 0.0)
            assert(values.max() / values.min() < 10.0)
        }
    }

    @Test
    fun `gate 4 - every window edge is a grid point, located to one grid ratio and no better`() {
        val grid = inputs.graftingDensityGrid
        val ratios = grid.zipWithNext { low, high -> high / low }
        assert(ratios.max() / ratios.min() < 1.0 + 1e-6)
        resynthesisedWindows(inputs, CorrectionSet.FULL).filter { !it.empty }.forEach { window ->
            assert(grid[window.lowestIndex!!].isCloseTo(window.lowestGraftingDensity!!, 1e-12))
            assert(grid[window.highestIndex!!].isCloseTo(window.highestGraftingDensity!!, 1e-12))
        }
    }

    // --- gate 5, upstream cross-checks ---------------------------------------------------

    @Test
    fun `gate 5 - C-0018's pull-in margin at 10 nm and 2 mM is reproduced from T-4's own file`() {
        val folds = inputs.usableBiasCeilings.filter {
            it.loadLine == "coupled" && it.layerHeight == 10.0 && it.concentration == 2.0
        }
        assert(folds.size == 6)
        assert(folds.all { it.bindingCeiling.contains("pull-in") })
        assert(folds.minOf { it.margin!! }.isCloseTo(1.007, 1e-2))
        assert(folds.maxOf { it.margin!! }.isCloseTo(1.032, 1e-2))
    }

    @Test
    fun `gate 5 - C-0017's stability margin bracket is reproduced from T-16's own file`() {
        val at = inputs.couplingRequirements.filter {
            it.layerHeight == 10.0 && it.concentration == 2.0
        }
        assert(at.minOf { it.stabilityMargin!! }.isCloseTo(1.194, 1e-2))
        assert(at.maxOf { it.stabilityMargin!! }.isCloseTo(1.424, 1e-2))
    }

    @Test
    fun `gate 5 - CH-0026's plus 14 point 7 per cent is reproduced at the record it belongs to`() {
        // and NOT at the record a partial key would have returned: 14.7 % is the RESTING
        // height of a 10 nm layer, while the operating point is the HELD gap of 7 nm, where
        // the same file says 10.3 %. Keying on too few dimensions is C-0026's own trap.
        val resting = inputs.edgeProfile(2.0, 10.0, "C-0012 simultaneous-target bias, stiffest layer model")
        assert(edgeForceMultiplier(resting.edgeForceFractionMinMargin).isCloseTo(1.1471, 1e-3))
        val held = inputs.edgeProfile(2.0, 7.0, "held at the 3 nm stroke below L0 = 10.0 nm")
        assert(edgeForceMultiplier(held.edgeForceFractionMinMargin).isCloseTo(1.1032, 1e-3))
    }

    @Test
    fun `gate 5 - a key that does not identify a unique upstream record throws`() {
        assertFailsWith<IllegalArgumentException> { inputs.edgeProfile(2.0, 7.0, "no such bias source") }
    }

    @Test
    fun `gate 5 - C-0019's licensed brackets are reproduced from T-1f's own file`() {
        assert(inputs.strokeMultiplier(10.0).isCloseTo(1.0196, 2e-3))
        assert(inputs.strokeMultiplier(7.0).isCloseTo(1.0138, 2e-3))
        assert(inputs.brushStiffnessMultiplier(10.0).isCloseTo(0.906, 5e-3))
        assert(inputs.brushStiffnessMultiplier(7.0).isCloseTo(0.949, 5e-3))
    }

    @Test
    fun `gate 5 - CH-0024's delivered stroke bracket is reproduced from T-13's own file`() {
        val tethered = inputs.descentBracket(HoldDownReading.TETHERED)
        assert(tethered.values.minOf { it.first }.isCloseTo(0.0717, 1e-3))
        assert(tethered.values.maxOf { it.second }.isCloseTo(0.3815, 1e-3))
    }

    @Test
    fun `gate 5 - CH-0034's flatness floor is reproduced from T-17's own file`() {
        assert(inputs.flatnessSaturation.dishingAtFortyFive.isCloseTo(0.218, 5e-3))
        assert(inputs.flatnessSaturation.dishingAtSaturation.isCloseTo(0.149, 5e-3))
        assert(!inputs.flatnessSaturation.reachesTolerance)
    }

    @Test
    fun `gate 5 - the per-point descent transfer is checked against T-13's own bracket`() {
        // the window needs d at every grafting density; T-13 solved it at one per height.
        // The first-order transfer d = F_down/(k_c + k_layer) is checked AT the shared design
        // points and reported as licensed or not, exactly as C-0016 checks T-3's transfer.
        val licences = inputs.descentTransferLicence(HoldDownReading.TETHERLESS)
        assert(licences.size == 3)
        assert(licences.first { it.layerHeight == 10.0 }.licensed)
        assert(licences.first { it.layerHeight == 7.0 }.licensed)
    }

    // --- gate 4, the fold-gap interpolation ----------------------------------------------

    @Test
    fun `gate 4 - the collar multiplier interpolates and never extrapolates silently`() {
        // the fold sits at a gap T-3b did not sample (5.9-6.6 nm at 10 nm), so the collar
        // there is interpolated between the gaps it did. Outside the sampled range the
        // accessor throws rather than extrapolating, which is what CH-0026's own falsifier 3
        // would otherwise hide.
        val atFive = inputs.collarMultiplierAt(2.0, 5.0)
        val atSeven = inputs.collarMultiplierAt(2.0, 7.0)
        val between = inputs.collarMultiplierAt(2.0, 6.0)
        assert(between > atFive)
        assert(between < atSeven)
        assertFailsWith<IllegalArgumentException> { inputs.collarMultiplierAt(2.0, 12.0) }
    }

    @Test
    fun `gate 4 - the fold's own movement is inside the collar gradient's scheme spread`() {
        // the bias-axis correction is reported as a LOWER BOUND, and the bound is only valid
        // if the state at C-0018's own fold is still stable once C-0019 softens the layer and
        // CH-0026 lengthens the decay. Asserted rather than assumed.
        val bounds = correctedPullInBounds(inputs)
        assert(bounds.isNotEmpty())
        // the operating bias falls at every state, so the margin rises at unchanged pull-in
        assert(bounds.all { it.operatingBiasShift < 0.0 })
        assert(bounds.all { it.marginLowerBound > it.marginBaseline!! })
        // and the fold's own movement is NOT resolved: C-0019's softening and CH-0026's
        // collar cancel there to within the gradient's own difference-scheme spread, which
        // is a finding and is asserted as one rather than papered over
        assert(bounds.any { !it.boundUnconditional })
        assert(bounds.all { it.foldTangentAtHighGradient > it.foldTangentAtLowGradient })
    }

    // --- the re-synthesised window itself, locked as a regression ------------------------

    @Test
    fun `the T-25 window moves exactly one edge, and it moves outward`() {
        val full = resynthesisedWindows(inputs, CorrectionSet.FULL)
        val baseline = resynthesisedWindows(inputs, CorrectionSet.IDENTITY)
        val moved = full.indices.count { index ->
            full[index].lowestIndex != baseline[index].lowestIndex ||
                    full[index].highestIndex != baseline[index].highestIndex
        }
        assert(moved == 1)
        val ten = full.first { it.layerHeight == 10.0 }
        val tenBefore = baseline.first { it.layerHeight == 10.0 }
        assert(ten.highestIndex!! == tenBefore.highestIndex!! + 1)
        assert(ten.lowestIndex == tenBefore.lowestIndex)
        // and the 7 nm window's two corrections cancel to within the grid
        val seven = full.first { it.layerHeight == 7.0 }
        val sevenBefore = baseline.first { it.layerHeight == 7.0 }
        assert(seven.highestIndex == sevenBefore.highestIndex)
        assert(seven.lowestIndex == sevenBefore.lowestIndex)
    }

    @Test
    fun `removing the substrate tethers is worth four grid steps of window at 10 nm`() {
        // CH-0024's own 2.62-2.93 nm is quoted for C-0021's device, which still carries
        // C-0014's eight substrate tethers. CH-0027 takes them out. That is not a rounding:
        // it is the difference between a window that narrows and one that does not.
        val tethered = resynthesisedWindows(
            inputs, CorrectionSet.FULL.copy(holdDown = HoldDownReading.TETHERED)
        )
        val tetherless = resynthesisedWindows(inputs, CorrectionSet.FULL)
        val tenA = tethered.first { it.layerHeight == 10.0 }
        val tenB = tetherless.first { it.layerHeight == 10.0 }
        assert(tenB.highestIndex!! - tenA.highestIndex!! >= 3)
        val sevenA = tethered.first { it.layerHeight == 7.0 }
        val sevenB = tetherless.first { it.layerHeight == 7.0 }
        assert(sevenB.highestIndex!! > sevenA.highestIndex!!)
        assert(sevenA.widthRatio!! < 1.35)
    }

    @Test
    fun `the combined coupling margin at 10 nm and 2 mM exceeds C-0017's own`() {
        // C-0019 alone degrades it, CH-0026 alone improves it, and the two are of the same
        // size. Quoting either alone is what this task exists to stop.
        val at = correctedMargins(inputs).filter {
            it.layerHeight == 10.0 && it.concentration == 2.0
        }
        assert(at.size == 6)
        assert(at.all { it.marginFluctuationOnly!! < it.marginBaseline!! })
        assert(at.all { it.marginEdgeOnly!! > it.marginBaseline!! })
        assert(at.all { it.marginCombinedLow!! > 1.0 })
        assert(at.minOf { it.marginCombinedLow!! } > at.minOf { it.marginBaseline!! })
        assert(at.all { it.stableAtMandate })
    }

    // --- the falsifier the cheap bound settles -------------------------------------------

    @Test
    fun `the upper window edge is owned by a clause with no field in it`() {
        // CH-0026 asserts that C-0016's upper edge "moves outward, because more force at the
        // same bias is more stroke". The edge is the stroke under a 100 pN DEAD LOAD, so the
        // electrostatic multiplier is not an argument of it at all. Asserted here rather than
        // argued, because it settles three of the four candidate movers before any arithmetic.
        val withEdge = resynthesisedWindows(
            inputs, CorrectionSet.IDENTITY.copy(applyEdgeEnhancement = true)
        )
        val without = resynthesisedWindows(inputs, CorrectionSet.IDENTITY)
        assert(withEdge.map { it.highestIndex } == without.map { it.highestIndex })
        assert(withEdge.map { it.lowestIndex } == without.map { it.lowestIndex })
    }
}
