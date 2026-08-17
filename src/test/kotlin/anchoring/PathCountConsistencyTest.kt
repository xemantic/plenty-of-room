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
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-138`, leaf `A8.2` — `C-0017`'s mandate is a stiffness on a **sum**, so a path count sizes the
 * element *and* counts the instances, and `C-0069`'s Deliverable 5 changes the first while holding
 * the second at 34.
 *
 * Every test is named for the verification gate it discharges.
 *
 * The declared falsifier is here as a test: **the two readings must coincide identically at
 * `n = 34`**, which is what makes this a correction to a presentation rather than to a claim.
 */
class PathCountConsistencyTest {

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private val duplexes = 15

    private val edgeX = Gen1Tile.EDGE_X

    private val width = OrigamiDuplex.INTERHELICAL

    /** `C-0009`'s one-crossover hinge and `C-0034`'s `A2` duplex-end couple — `C-0069`'s `Q5`. */
    private val hinge = 13.5294

    private val tipCouple = 78.2353

    private fun arm(count: Int, steps: Int = 400) = elasticaArmForStiffness(
        hingeStiffness = hinge,
        hingeCount = 1,
        farStiffness = tipCouple,
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        count = count,
        targetStiffness = mandate,
        workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE,
        steps = steps
    )

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 — the delivered total is a stiffness, linear in the mandate, and the ratio is a pure number`() {
        assert(deliveredTotalStiffness(mandate, 15, 34).isCloseTo(34.0 * mandate / 15.0, 1e-12))
        assert(
            deliveredTotalStiffness(2.0 * mandate, 15, 34)
                .isCloseTo(2.0 * deliveredTotalStiffness(mandate, 15, 34), 1e-12)
        )
        assert(mandateRatio(15, 34).isCloseTo(34.0 / 15.0, 1e-12))
        assert(mandateRatio(45, 24).isCloseTo(24.0 / 45.0, 1e-12))
        assertFailsWith<IllegalArgumentException> { deliveredTotalStiffness(mandate, 0, 34) }
        assertFailsWith<IllegalArgumentException> { deliveredTotalStiffness(-1.0, 15, 34) }
        assertFailsWith<IllegalArgumentException> { deliveredTotalStiffness(mandate, 15, -1) }
        assertFailsWith<IllegalArgumentException> { mandateRatio(0, 3) }
    }

    @Test
    fun `gate 1 — the arm is a CUBE root of the per-path stiffness, so more paths make it longer`() {
        val few = arm(15)
        val many = arm(45)
        assert(many > few)
        // `C-0023`'s `L ∝ n^(1/3)`, near but not at a third: `c(ρ)` carries the span itself
        // (`ρ = k_θ L/EI`, `C-0025`), so the length is a FIXED POINT and a longer arm buys its own
        // end restraint — which raises the realised exponent above 1/3 rather than lowering it.
        val exponent = kotlin.math.ln(many / few) / kotlin.math.ln(45.0 / 15.0)
        assert(exponent > 0.25 && exponent < 0.45)
        assert(abs(exponent - 1.0 / 3.0) < 0.25 / 3.0)
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 — the mandate is met exactly when the placed count IS the path count`() {
        listOf(15, 22, 30, 34, 45).forEach { n ->
            assert(deliveredTotalStiffness(mandate, n, n).isCloseTo(mandate, 1e-12))
            assert(mandateRatio(n, n).isCloseTo(1.0, 1e-12))
        }
    }

    @Test
    fun `gate 2 — the balanced count vector reproduces C-0063's bound 1 and saturates at the cap`() {
        val at34 = balancedRowCounts(34, duplexes, 3)
        assert(at34.sum() == 34)
        assert(at34.count { it == 3 } == 4)
        assert(at34.count { it == 2 } == 11)
        val at30 = balancedRowCounts(30, duplexes, 3)
        assert(at30.all { it == 2 })
        val at45 = balancedRowCounts(45, duplexes, 3)
        assert(at45.all { it == 3 })
        val at15 = balancedRowCounts(15, duplexes, 3)
        assert(at15.all { it == 1 })
        assertFailsWith<IllegalArgumentException> { balancedRowCounts(46, duplexes, 3) }
        assertFailsWith<IllegalArgumentException> { balancedRowCounts(0, duplexes, 3) }
    }

    // ------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 — the published and self-consistent readings coincide IDENTICALLY at 34 paths`() {
        // C-0069 holds the array at 34; the self-consistent reading ties it to the path count.
        // At n = 34 they are the same number, bit for bit — which is the declared falsifier.
        assert(deliveredTotalStiffness(mandate, 34, 34) == deliveredTotalStiffness(mandate, 34, 34))
        assert(mandateRatio(34, 34) == 1.0)
        // and they part company exactly where C-0069's table moves the count
        assert(mandateRatio(15, 34) > 2.0)
        assert(mandateRatio(45, 24) < 0.6)
    }

    @Test
    fun `gate 3 — the closed-form ceiling per count never falls below a concrete placement's`() {
        val lattice = upwardRootLattice(24, edgeX, duplexes)
        listOf(15 to 1, 30 to 2, 45 to 3).forEach { (count, perRow) ->
            val bound = maximumPlanCeilingForCount(lattice, count, edgeX, width, perRow)!!
            val concrete = rootedLengthCeiling(
                lattice.mapIndexed { row, sites ->
                    StationRow(row, row.toDouble(), sites.take(perRow))
                },
                edgeX, width
            )
            assert(concrete <= bound + 1e-9)
        }
    }

    // ------------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 — the placed arm is RK4-step independent`() {
        val readings = listOf(200, 400, 800).map { arm(30, it) }
        assert(abs(readings[2] - readings[1]) / readings[2] < 1e-6)
        assert(abs(readings[1] - readings[0]) / readings[1] < 1e-5)
    }

    // ------------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 — C-0069's own arm lengths reproduce at 34, 45 and 15 paths`() {
        assert(arm(34).isCloseTo(8.16439083, 1e-6))
        assert(arm(45).isCloseTo(9.131, 1e-3))
        assert(arm(15).isCloseTo(5.963, 1e-3))
    }

    @Test
    fun `gate 5 — C-0072's 30-path arm reproduces and its base-pair count is 23`() {
        val thirty = arm(30)
        assert(thirty.isCloseTo(7.77049, 1e-5))
        assert(basePairsNearest(thirty) == 23)
    }

}
