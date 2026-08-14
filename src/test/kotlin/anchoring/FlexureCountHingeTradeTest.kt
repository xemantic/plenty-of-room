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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-99` — whether a coupling of **fewer, longer** flexures closes where 45 short ones do not.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that the path count and the hinge count are **not independent**: a
 * hinge line lies on one interface, and the tile's interfaces carry a counted number of
 * crossovers, so `n · h ≤ N_inv`. "Fewer" and "longer" are the same currency spent twice.
 */
class FlexureCountHingeTradeTest {

    private val pitch = perInterfacePitch()

    private val hinge = Gen1Tile.crossoverHingeStiffness()

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val mandate = 100.0 / 3.0

    private val acceptable = 3.0

    private val desired = 10.0

    /** `C-0034`'s adopted `A2` anchorage — the arm's own duplex end, two strand termini. */
    private val far = 78.2352941176

    /** `C-0015`'s inventory at the ten eight-column phases. */
    private val inventory = 56

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the line demand is a length, linear in the pitch and in the count`() {
        // (h - 1) p, a length in nm
        assert(collinearLineDemand(1, 4, pitch).isCloseTo(3.0 * pitch))
        assert(collinearLineDemand(1, 4, 2.0 * pitch).isCloseTo(2.0 * 3.0 * pitch))
        assert(collinearLineDemand(7, 4, pitch).isCloseTo(7.0 * 3.0 * pitch))
        // a single crossover needs no line at all
        assert(collinearLineDemand(45, 1, pitch).isCloseTo(0.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the crossover demand is a pure count and the supply a pure count`() {
        assert(hingeCrossoverDemand(45, 16) == 720)
        assert(hingeCrossoverDemand(14, 4) == 56)
        // 15 duplexes give 14 interior interfaces plus 2 free edges
        assert(interfaceLineSupply(15, 40.0).isCloseTo(640.0))
        assert(interfaceLineSupply(15, 80.0).isCloseTo(1280.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the small-rotation arm ceiling is a square root of a restraint over a stiffness`() {
        val restraint = inventory * hinge
        val ceiling = smallRotationArmCeiling(restraint, mandate)
        assert(ceiling.isCloseTo(sqrt(restraint / mandate)))
        // four times the restraint doubles it; four times the target halves it
        assert(smallRotationArmCeiling(4.0 * restraint, mandate).isCloseTo(2.0 * ceiling))
        assert(smallRotationArmCeiling(restraint, 4.0 * mandate).isCloseTo(0.5 * ceiling))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { hingeCrossoverDemand(0, 4) }
        assertFailsWith<IllegalArgumentException> { hingeCrossoverDemand(4, 0) }
        assertFailsWith<IllegalArgumentException> { collinearLineDemand(4, 4, -1.0) }
        assertFailsWith<IllegalArgumentException> { interfaceLineSupply(1, 40.0) }
        assertFailsWith<IllegalArgumentException> { maximumHingeCountForInventory(0, 56) }
        assertFailsWith<IllegalArgumentException> { smallRotationArmCeiling(-1.0, mandate) }
        assertFailsWith<IllegalArgumentException> { rigidArmCeiling(100.0, mandate, 0.0) }
        assertFailsWith<IllegalArgumentException> { minimumPathCountForAllowable(mandate, 10.0, 0.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the two ledgers are exact inverses of each other`() {
        (1..56).forEach { paths ->
            val most = maximumHingeCountForInventory(paths, inventory)
            if (most > 0) {
                assert(hingeCrossoverDemand(paths, most) <= inventory)
                assert(hingeCrossoverDemand(paths, most + 1) > inventory)
                assert(maximumPathCountForInventory(most, inventory) >= paths)
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the inventory admits exactly one crossover per flexure at 45 paths and four at fourteen`() {
        assert(maximumHingeCountForInventory(45, inventory) == 1)
        assert(maximumHingeCountForInventory(14, inventory) == 4)
        assert(maximumHingeCountForInventory(56, inventory) == 1)
        assert(maximumHingeCountForInventory(57, inventory) == 0)
        // and at the twenty-two seven-column phases the inventory is 49
        assert(maximumHingeCountForInventory(14, 49) == 3)
    }

    @Test
    fun `gate 2 limiting cases - the exact-rotation ceiling exceeds the small-rotation one and tends to it`() {
        val restraint = inventory * hinge
        val small = smallRotationArmCeiling(restraint, mandate)
        val exact = rigidArmCeiling(restraint, mandate, acceptable)
        assert(exact > small)
        // as the working displacement vanishes the geometry disappears
        val vanishing = rigidArmCeiling(restraint, mandate, 1.0e-4)
        assert(abs(vanishing - small) / small < 1.0e-6)
    }

    @Test
    fun `gate 2 limiting cases - the rigid-arm ceiling satisfies its own force balance`() {
        val restraint = inventory * hinge + 45 * far
        val arm = rigidArmCeiling(restraint, mandate, acceptable)
        // theta from the geometry alone
        val theta = kotlin.math.asin(acceptable / arm)
        // and the assembled secant a rigid arm on that restraint presents
        val assembled = restraint * theta / (arm * arm * sin(theta) * kotlin.math.cos(theta))
        assert(assembled.isCloseTo(mandate, 1.0e-9))
    }

    @Test
    fun `gate 2 limiting cases - the hinge-supplied ceiling is a function of the PRODUCT alone`() {
        // every split of the same inventory gives the same hinge-supplied arm
        val reference = hingeSuppliedArmCeiling(inventory, hinge, mandate, acceptable)
        listOf(1 to 56, 2 to 28, 4 to 14, 7 to 8, 8 to 7, 14 to 4, 28 to 2, 56 to 1).forEach {
            val (h, n) = it
            assert(hingeCrossoverDemand(n, h) == inventory)
            assert(hingeSuppliedArmCeiling(n * h, hinge, mandate, acceptable).isCloseTo(reference))
        }
    }

    @Test
    fun `gate 2 limiting cases - a flexure that cannot reach the target stroke geometrically is reported as such`() {
        val point = tradePoint(45, 1, far, mandate, acceptable, desired, hinge, ei)
        assert(!point.reachesTargetGeometrically)
        assert(point.armLength < desired)
    }

    // ---------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the placed arm grows monotonically along the trade curve toward MORE paths`() {
        // n h = 56 exactly, so the crossover ledger is spent identically at every point
        val arms = listOf(4 to 14, 7 to 8, 8 to 7, 14 to 4, 28 to 2, 56 to 1).map {
            val (paths, hinges) = it
            tradePoint(paths, hinges, far, mandate, acceptable, desired, hinge, ei).armLength
        }
        assert(arms.zipWithNext().all { (a, b) -> b > a })
    }

    @Test
    fun `gate 3 conservation - the assembled secant at the working point is the placement target, at every point`() {
        listOf(14 to 4, 28 to 2, 45 to 1, 56 to 1).forEach {
            val (paths, hinges) = it
            val point = tradePoint(paths, hinges, far, mandate, acceptable, desired, hinge, ei)
            assert(point.secantAtWorking.isCloseTo(mandate, 1.0e-7))
        }
    }

    @Test
    fun `gate 3 conservation - the rigid-arm bound strictly bounds the placed arm from above`() {
        listOf(14 to 4, 28 to 2, 45 to 1, 56 to 1).forEach {
            val (paths, hinges) = it
            val point = tradePoint(paths, hinges, far, mandate, acceptable, desired, hinge, ei)
            assert(point.armLength < point.rigidArmBound)
        }
    }

    // ---------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 convergence - the placed arm is independent of the RK4 step count`() {
        val coarse = tradePoint(45, 1, far, mandate, acceptable, desired, hinge, ei, steps = 200)
        val fine = tradePoint(45, 1, far, mandate, acceptable, desired, hinge, ei, steps = 800)
        assert(fine.armLength.isCloseTo(coarse.armLength, 1.0e-5))
        assert(fine.usableStroke.isCloseTo(coarse.usableStroke, 1.0e-4))
    }

    @Test
    fun `gate 4 convergence - the usable stroke is independent of the scan sample count`() {
        val beam = TwoSpringElastica(ei, 12.7198, 16 * hinge, far, 400)
        val coarse = usableStrokeInsideCeiling(beam, 45, 40.0, desired, samples = 60)
        val fine = usableStrokeInsideCeiling(beam, 45, 40.0, desired, samples = 240)
        assert(fine.isCloseTo(coarse, 1.0e-6))
    }

    @Test
    fun `gate 4 convergence - the rigid-arm ceiling bisection converges to its own defining equation`() {
        listOf(200.0, 757.6, 4278.2, 20000.0).forEach { restraint ->
            val arm = rigidArmCeiling(restraint, mandate, acceptable)
            val theta = kotlin.math.asin(acceptable / arm)
            assert((theta * kotlin.math.tan(theta)).isCloseTo(
                mandate * acceptable * acceptable / restraint, 1.0e-10
            ))
        }
    }

    // ---------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream - C-0039's adopted design reproduces at 45 paths and 16 crossovers`() {
        val point = tradePoint(45, 16, far, mandate, acceptable, desired, hinge, ei)
        assert(point.armLength.isCloseTo(12.7198, 1.0e-4))
        assert(point.tangentAtWorking.isCloseTo(36.44, 1.0e-3))
        assert(point.usableStroke.isCloseTo(3.877, 1.0e-3))
        assert(point.secantAtTarget.isCloseTo(69.94, 1.0e-3))
        assert(point.tangentAtTarget.isCloseTo(264.24, 1.0e-3))
    }

    @Test
    fun `gate 5 upstream - C-0039's fifteen-path arm reproduces`() {
        val point = tradePoint(15, 16, far, mandate, acceptable, desired, hinge, ei)
        assert(point.armLength.isCloseTo(8.40, 1.0e-3))
    }

    @Test
    fun `gate 5 upstream - C-0040's census and its sixteen-crossover line demand reproduce`() {
        assert(maximumHingeCount(Gen1Tile.EDGE_X, pitch) == 4)
        assert(collinearLineDemand(1, 16, pitch).isCloseTo(163.2, 1.0e-9))
        assert(hingeLineCensus(Gen1Tile.EDGE_X).all { it.largest == 4 })
    }

    @Test
    fun `gate 5 upstream - CH-0029's path-count floor on the mandate secant is 34`() {
        assert(minimumPathCountForAllowable(mandate, desired, 10.0) == 34)
        // and at §3's acceptable stroke the same reading gives ten
        assert(minimumPathCountForAllowable(mandate, acceptable, 10.0) == 10)
    }

    @Test
    fun `gate 5 upstream - the widened placement reproduces C-0039's own solver above its floor`() {
        // above 1.5 x the working displacement the two search domains coincide, so the two
        // solvers must agree to the last digit; below it only the widened one exists
        listOf(15 to 16, 45 to 16, 45 to 1, 56 to 1).forEach {
            val (paths, hinges) = it
            val widened = elasticaPlacement(hinge, hinges, far, ei, paths, mandate, acceptable)
            val standing = elasticaArmForStiffness(
                hinge, hinges, far, ei, paths, mandate, acceptable
            )
            assert(widened.isCloseTo(standing, 1.0e-9))
            assert(widened > 1.5 * acceptable)
        }
    }

    @Test
    fun `gate 2 limiting cases - the placement at §3's desired clause needs an arm C-0039's floor excludes`() {
        // placing at 10 pN/nm and a 10 nm stroke, the arm sits between the stroke and 1.5x it
        // at the smallest path counts, which is exactly why the search floor had to be widened
        val arm = elasticaPlacement(hinge, 1, far, ei, 8, 100.0 / desired, desired)
        assert(arm > desired)
        assert(arm < 1.5 * desired)
    }

    // ---------------------------------------------- gate 3 — the binding constraint

    @Test
    fun `gate 3 conservation - the constraint report names the inventory at a point that violates it`() {
        val point = tradePoint(45, 16, far, mandate, acceptable, desired, hinge, ei)
        val binding = bindingConstraints(
            point,
            TradeConstraints(
                inventory = inventory,
                maximumHingeLineCount = 4,
                lineSupply = 640.0,
                unzipAllowable = 10.0,
                ceiling = 40.0,
                ceilingReading = CeilingReading.WHOLE_STROKE,
                targetStroke = desired,
                stabilityFloor = 23.41,
                targetStiffness = mandate
            )
        )
        assert(binding.any { it.startsWith("crossover inventory") })
        assert(binding.any { it.startsWith("hinge-line census") })
        assert(binding.any { it.startsWith("collinear interface") })
    }

    @Test
    fun `gate 3 conservation - a placement below the stability floor is refused whatever the point does`() {
        val point = tradePoint(14, 4, far, 100.0 / desired, desired, desired, hinge, ei)
        val binding = bindingConstraints(
            point,
            TradeConstraints(
                inventory = inventory,
                maximumHingeLineCount = 4,
                lineSupply = 640.0,
                unzipAllowable = 10.0,
                ceiling = 40.0,
                ceilingReading = CeilingReading.WHOLE_STROKE,
                targetStroke = desired,
                stabilityFloor = 23.41,
                targetStiffness = 100.0 / desired
            )
        )
        // it reaches the stroke, it holds the ceiling, and it is refused anyway
        assert(point.reachesTargetGeometrically)
        assert(point.insideCeilingOverStroke)
        assert(binding.any { it.startsWith("C-0017 stability floor") })
    }

    @Test
    fun `gate 2 limiting cases - the two ceiling readings agree on a stiffening element that clears both`() {
        // C-0023's ceiling read at the working point and over the whole stroke are the same
        // question whenever the element is monotone in its tangent, which E5a is
        val point = tradePoint(56, 1, far, mandate, acceptable, acceptable, hinge, ei)
        assert(point.insideCeilingAtWorking == point.insideCeilingOverStroke)
    }

    @Test
    fun `gate 5 upstream - C-0017's stability floor refuses the desired-stroke placement`() {
        // C-0017: |k_eff| = 23.41-27.91 pN/nm at the 10 nm layer, 2 mM. Placement for a 10 nm
        // stroke at §3's own 100 pN is 10 pN/nm, which is BELOW that floor.
        val placementForDesired = 100.0 / desired
        assert(placementForDesired < 23.41)
        assert(strokeCeilingFromStability(100.0, 23.41).isCloseTo(100.0 / 23.41))
    }
}
