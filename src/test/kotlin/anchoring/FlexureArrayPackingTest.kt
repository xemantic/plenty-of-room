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
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-96` — does `C-0035`'s surviving mounting survive `T-31`'s array packing?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0035` prices every aperture **as an area** and says so: 45
 * flexures of ~32 nm span, each with two standoff legs and one tie, have to be **placed** on a
 * body the size of the tile, and the midspans are pinned by the attachment grid.
 */
class FlexureArrayPackingTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    private val duplex = OrigamiDuplex.INTERHELICAL

    /** `C-0028`'s recommended base — two crossovers laid ACROSS the flexure. */
    private val base = StandoffBase.crossovers(2, favourableOrientation = true)

    private val flexibility = standoffTipFlexibility(ei, 8.0, base.rotationalStiffness)

    /** `C-0030`'s recommended design: 45 paths, `ℓ = 8 nm`, placed at §3's acceptable stroke. */
    private val designSpan = coupledFlexureSpan(
        ei, flexibility, 45, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
        Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
    )

    private fun array(columns: Int, angle: Double, span: Double = designSpan) =
        gridFlexureArray(columns, 15, Gen1Tile.EDGE_X, 15 * duplex, span, angle)

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate1 the array plan area is a length squared and is the count times span times pitch`() {
        val flexures = array(3, 0.0)
        val summed = flexures.sumOf { it.body.area }
        assert(summed.isCloseTo(45 * designSpan * duplex))
        assert(arrayPlanArea(45, designSpan, duplex).isCloseTo(summed))
        // doubling the span doubles the area, exactly
        assert(arrayPlanArea(45, 2.0 * designSpan, duplex).isCloseTo(2.0 * summed))
    }

    @Test
    fun `gate1 the packing verdict is dimensionless — scaling every length leaves it unchanged`() {
        val angles = listOf(0.0, 0.1, 0.4, PI / 4, PI / 2)
        angles.forEach { angle ->
            val plain = packingVerdict(array(3, angle))
            val scaled = packingVerdict(
                gridFlexureArray(
                    3, 15, 10.0 * Gen1Tile.EDGE_X, 10.0 * 15 * duplex,
                    10.0 * designSpan, angle, 10.0 * duplex
                )
            )
            assert(scaled.overlappingPairs == plain.overlappingPairs)
            assert(scaled.mutuallyBlockingPairs == plain.mutuallyBlockingPairs)
            assert(scaled.feasibleAtAnyLevelCount == plain.feasibleAtAnyLevelCount)
        }
    }

    @Test
    fun `gate1 unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { PlanRectangle(Vector2.ZERO, 0.0, -1.0, 2.69) }
        assertFailsWith<IllegalArgumentException> { PlanRectangle(Vector2.ZERO, 0.0, 10.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { PlanFlexure("a", Vector2.ZERO, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { packingVerdict(emptyList()) }
        assertFailsWith<IllegalArgumentException> { arrayPlanArea(0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { gridFlexureArray(0, 15, 40.0, 40.35, 10.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            slotLengthForClearance(-1.0, 92.0, 3.0, 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            availableLevelHeights(3.0, maximumLength = 1.0)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate2 a single flexure packs at one level in every orientation`() {
        (0 until 36).forEach { i ->
            val one = listOf(PlanFlexure("solo", Vector2.ZERO, i * PI / 36.0, designSpan))
            val verdict = packingVerdict(one)
            assert(verdict.overlappingPairs == 0)
            assert(verdict.mutuallyBlockingPairs == 0)
            assert(verdict.feasibleAtAnyLevelCount)
            assert(verdict.levelsRequired == 1)
            assert(verdict.singleLevel)
        }
    }

    @Test
    fun `gate2 collinear beams clear each other only past the span PLUS one duplex`() {
        listOf(0.0, PI / 3, PI / 2).forEach { angle ->
            val axis = Vector2(kotlin.math.cos(angle), kotlin.math.sin(angle))
            fun verdict(separation: Double) = packingVerdict(
                listOf(
                    PlanFlexure("a", Vector2.ZERO, angle, designSpan),
                    PlanFlexure("b", axis * separation, angle, designSpan)
                )
            )
            // bodies overlap below the span
            listOf(0.5, 0.9, 0.999).forEach { fraction ->
                assert(verdict(fraction * designSpan).overlappingPairs == 1)
            }
            // between the span and the span plus a duplex the BODIES clear but the standoff FEET
            // do not — and the conflict is mutual, so no level count resolves it
            listOf(designSpan * 1.001, designSpan + duplex - 0.01).forEach { separation ->
                assert(verdict(separation).overlappingPairs == 0)
                assert(verdict(separation).memberClashPairs == 1)
                assert(!verdict(separation).feasibleAtAnyLevelCount)
            }
            // past the span plus a duplex everything clears
            listOf(designSpan + duplex + 0.01, 1.5 * designSpan).forEach { separation ->
                assert(verdict(separation).overlappingPairs == 0)
                assert(verdict(separation).blockingPairs == 0)
                assert(verdict(separation).singleLevel)
            }
        }
    }

    @Test
    fun `gate2 parallel beams tangent at exactly the interhelical distance neither overlap nor block`() {
        val pair = listOf(
            PlanFlexure("a", Vector2.ZERO, 0.0, designSpan),
            PlanFlexure("b", Vector2(0.0, duplex), 0.0, designSpan)
        )
        val verdict = packingVerdict(pair)
        assert(verdict.overlappingPairs == 0)
        assert(verdict.blockingPairs == 0)
        assert(verdict.singleLevel)
    }

    @Test
    fun `gate2 the slot reproduces C-0035's aperture at the standoff clearance`() {
        val restraint = CoupledJointFlexure(ei, designSpan, flexibility, stretch).restraint
        listOf(5.0, 6.0, 8.0, 10.0, 12.7).forEach { length ->
            listOf(Gen1Tile.ACCEPTABLE_STROKE, Gen1Tile.DESIRED_STROKE).forEach { stroke ->
                val published = apertureLength(designSpan, restraint, stroke, length)
                val derived = slotLengthForClearance(
                    designSpan, restraint, stroke, midspanClearance(length)
                )
                assert(derived.isCloseTo(published, 1e-12))
            }
        }
    }

    @Test
    fun `gate2 a stroke inside the clearance needs no slot and a zero clearance needs the whole span`() {
        assert(slotLengthForClearance(designSpan, 92.0, 3.0, 5.31) == 0.0)
        assert(slotLengthForClearance(designSpan, 92.0, 0.0, 1.0) == 0.0)
        assert(slotLengthForClearance(designSpan, 92.0, 3.0, 0.0).isCloseTo(designSpan))
    }

    @Test
    fun `gate2 the level ladder is empty above the envelope and quantised to the rise below it`() {
        val levels = availableLevelHeights(Gen1Tile.ACCEPTABLE_STROKE)
        assert(levels.isNotEmpty())
        levels.forEach { level ->
            assert(abs(level / Gen1Tile.RISE_PER_BASE_PAIR -
                    Math.round(level / Gen1Tile.RISE_PER_BASE_PAIR)) < 1e-9)
            assert(level - duplex >= Gen1Tile.ACCEPTABLE_STROKE - 1e-12)
            assert(level <= 10.0 + 1e-12)
        }
        levels.zipWithNext().forEach { (low, high) ->
            assert(high - low >= OrigamiDuplex.DIAMETER - 1e-12)
        }
        // §3's desired stroke needs `ℓ ≥ 12.69 nm` (`C-0030`), which is outside the envelope
        assert(availableLevelHeights(Gen1Tile.DESIRED_STROKE).isEmpty())
    }

    // ------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate3 the verdict is invariant under a rigid rotation of the whole array`() {
        listOf(0.0, 0.31, 1.0, 2.4).forEach { angle ->
            val plain = packingVerdict(array(3, angle))
            listOf(0.17, 0.9, PI / 2, 2.0).forEach { turn ->
                val turned = packingVerdict(array(3, angle).map { it.rotatedAbout(Vector2.ZERO, turn) })
                assert(turned.overlappingPairs == plain.overlappingPairs)
                assert(turned.blockingPairs == plain.blockingPairs)
                assert(turned.mutuallyBlockingPairs == plain.mutuallyBlockingPairs)
                assert(turned.levelsRequired == plain.levelsRequired)
            }
        }
    }

    @Test
    fun `gate3 covering a tie always implies overlapping the beam that owns it`() {
        var covers = 0
        (0 until 24).forEach { i ->
            val flexures = array(3, i * PI / 24.0)
            flexures.indices.forEach { a ->
                flexures.indices.forEach { b ->
                    if (a != b && blocksTie(flexures[a], flexures[b])) {
                        covers++
                        assert(rectanglesOverlap(flexures[a].body, flexures[b].body))
                    }
                    if (a != b && blocksTie(flexures[a], flexures[b])) {
                        assert(blocksVerticalMembers(flexures[a], flexures[b]))
                    }
                }
            }
        }
        assert(covers > 0)
    }

    @Test
    fun `gate3 blocking between two collinear identical beams is mutual, whichever is called lower`() {
        val a = PlanFlexure("a", Vector2.ZERO, 0.0, designSpan)
        val b = PlanFlexure("b", Vector2(13.3333333, 0.0), 0.0, designSpan)
        assert(blocksVerticalMembers(a, b))
        assert(blocksVerticalMembers(b, a))
    }

    @Test
    fun `gate3 the severance union-find conserves segments and never invents material`() {
        listOf(1, 2, 3).forEach { columns ->
            val holes = array(columns, 0.0).map { it.tiePoint }
            val severance = superstructureSeverance(holes, Gen1Tile.EDGE_X, 15 * duplex, true)
            assert(severance.components in 1..severance.segments)
            assert(severance.segments >= severance.duplexes)
        }
    }

    @Test
    fun `gate3 the packing-limited count is the column limit times the fifteen rows`() {
        val columns = packingLimitedColumns(Gen1Tile.EDGE_X, designSpan)
        assert(columns == floor(Gen1Tile.EDGE_X / (designSpan + duplex)).toInt())
        assert(packingLimitedPathCount(Gen1Tile.EDGE_X, 15, designSpan) == columns * 15)
    }

    // ------------------------------------------------------------ gate 4 — numerical convergence

    @Test
    fun `gate4 the orientation sweep verdict is sample-count independent`() {
        val coarse = orientationSweep(array(3, 0.0).map { it.midspan }, designSpan, 180)
        val fine = orientationSweep(array(3, 0.0).map { it.midspan }, designSpan, 1440)
        assert(coarse.feasibleOrientations == fine.feasibleOrientations)
        assert(coarse.minimumMutuallyBlockingPairs == fine.minimumMutuallyBlockingPairs)
    }

    @Test
    fun `gate4 the slot is scan-step independent`() {
        val restraint = CoupledJointFlexure(ei, designSpan, flexibility, stretch).restraint
        val reference = slotLengthForClearance(designSpan, restraint, 10.0, 5.31, 4096)
        listOf(64, 256, 1024).forEach { steps ->
            assert(slotLengthForClearance(designSpan, restraint, 10.0, 5.31, steps)
                .isCloseTo(reference, 1e-12))
        }
    }

    @Test
    fun `gate4 the re-placed span is scan-step independent at every candidate path count`() {
        listOf(15, 30, 34, 45).forEach { count ->
            val reference = coupledFlexureSpan(
                ei, flexibility, count, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
                Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch,
                DrawInModel.CHORD, 2048
            )
            listOf(64, 256, 1024).forEach { steps ->
                val span = coupledFlexureSpan(
                    ei, flexibility, count, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
                    Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch,
                    DrawInModel.CHORD, steps
                )
                assert(span.isCloseTo(reference, 1e-9))
            }
        }
    }

    @Test
    fun `gate4 the severance verdict does not depend on the crossover phase`() {
        val holes = array(3, 0.0).map { it.tiePoint }
        val spacing = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR
        val counts = (0 until 32).map { phase ->
            superstructureSeverance(
                holes, Gen1Tile.EDGE_X, 15 * duplex, true,
                crossoverSpacing = spacing,
                crossoverPhase = phase * Gen1Tile.RISE_PER_BASE_PAIR
            ).components
        }
        // the component COUNT is a function of the phase, as `C-0015` insists; the VERDICT is not
        assert(counts.all { it > 1 })
    }

    // ------------------------------------------------------------ gate 5 — upstream cross-check

    @Test
    fun `gate5 C-0030's recommended span and tangent are reproduced`() {
        assert(abs(designSpan - 31.82) / 31.82 < 1e-3)
        val flexure = CoupledJointFlexure(ei, designSpan, flexibility, stretch)
        val tangent = 45 * flexure.strokeTangentStiffness(
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
        )
        assert(abs(tangent - 25.23) / 25.23 < 1e-3)
    }

    @Test
    fun `gate5 C-0035's aperture length and both aperture areas are reproduced`() {
        val restraint = CoupledJointFlexure(ei, designSpan, flexibility, stretch).restraint
        val slot = apertureLength(designSpan, restraint, Gen1Tile.DESIRED_STROKE, 8.0)
        assert(abs(slot - 18.37) / 18.37 < 1e-3)
        assert(abs(apertureArea(45, slot) - 2223.0) / 2223.0 < 2e-3)
        assert(abs(tieApertureArea(45) - 326.0) / 326.0 < 2e-3)
    }

    @Test
    fun `gate5 the lattice constants are the cited measured ones`() {
        assert(duplex.isCloseTo(2.69))
        assert(OrigamiDuplex.DIAMETER.isCloseTo(2.0))
        assert(Gen1Tile.CROSSOVER_SPACING_SHEET_BP.isCloseTo(32.0))
        assert(Gen1Tile.RISE_PER_BASE_PAIR.isCloseTo(0.34))
    }

    @Test
    fun `gate5 the placed span follows the cube root of the path count`() {
        val reference = coupledFlexureSpan(
            ei, flexibility, 45, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        listOf(15, 30, 34).forEach { count ->
            val span = coupledFlexureSpan(
                ei, flexibility, count, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
                Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
            )
            val scaling = reference * (count / 45.0).pow(1.0 / 3.0)
            assert(abs(span - scaling) / scaling < 0.06)
        }
    }

    @Test
    fun `gate5 fifteen rows is one attachment row per duplex at every column count`() {
        (1..15).forEach { columns ->
            gridFlexureArray(columns, 15, Gen1Tile.EDGE_X, 15 * duplex, designSpan, 0.0)
                .forEach { flexure ->
                    val index = Math.round(flexure.midspan.y / duplex + 7.0)
                    assert(abs(flexure.midspan.y - (index - 7) * duplex) < 1e-12)
                }
        }
    }

    // ------------------------------------------------------------ the acceptance predicate

    @Test
    fun `P1 the forty-five beam array occupies 2_4 times the tile footprint`() {
        val ratio = arrayPlanArea(45, designSpan, duplex) / (Gen1Tile.EDGE_X * Gen1Tile.EDGE_Y)
        assert(ratio > 2.3 && ratio < 2.5)
    }

    @Test
    fun `P2 no beam orientation packs forty-five flexures in one level`() {
        val sweep = orientationSweep(array(3, 0.0).map { it.midspan }, designSpan, 720)
        assert(sweep.singleLevelOrientations == 0)
    }

    @Test
    fun `P3 the three by fifteen array blocks mutually at every orientation, so no level count helps`() {
        val sweep = orientationSweep(array(3, 0.0).map { it.midspan }, designSpan, 720)
        assert(sweep.feasibleOrientations == 0)
        assert(sweep.minimumMutuallyBlockingPairs > 0)
    }

    @Test
    fun `P4 the Gen-1 tile packs fifteen flexures and not thirty`() {
        assert(packingLimitedPathCount(Gen1Tile.EDGE_X, 15, designSpan) == 15)
        val span15 = coupledFlexureSpan(
            ei, flexibility, 15, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val span30 = coupledFlexureSpan(
            ei, flexibility, 30, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        assert(packingVerdict(array(1, 0.0, span15)).singleLevel)
        assert(span15 <= Gen1Tile.EDGE_X)
        val thirty = orientationSweep(
            gridFlexureArray(2, 15, Gen1Tile.EDGE_X, 15 * duplex, span30, 0.0).map { it.midspan },
            span30, 720
        )
        assert(thirty.feasibleOrientations == 0)
    }

    @Test
    fun `P5 fifteen paths clear the unzip allowable at the acceptable stroke and not at the desired one`() {
        val span = coupledFlexureSpan(
            ei, flexibility, 15, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val flexure = CoupledJointFlexure(ei, span, flexibility, stretch)
        val atAcceptable = flexure.strokeReaction(
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
        )
        val atDesired = flexure.strokeReaction(
            Gen1Tile.DESIRED_STROKE, FlexureOrientation.FAVOURABLE
        )
        assert(atAcceptable.isCloseTo(Gen1Tile.TARGET_FORCE / 15.0, 1e-6))
        assert(atAcceptable < Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
        assert(atDesired > Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
    }

    @Test
    fun `P6 the minimum body area for a desired-stroke design is 1_6 times the Gen-1 footprint`() {
        val count = 34
        val span = coupledFlexureSpan(
            ei, flexibility, count, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val area = arrayPlanArea(count, span + duplex, duplex)
        val ratio = area / (Gen1Tile.EDGE_X * 15 * duplex)
        assert(ratio > 1.7 && ratio < 1.9)
    }

    @Test
    fun `P7 the regular tie grid severs the superstructure into disconnected strips`() {
        val holes = array(3, 0.0).map { it.tiePoint }
        val along = superstructureSeverance(holes, Gen1Tile.EDGE_X, 15 * duplex, true)
        assert(along.severed)
        // three collinear holes cut every one of the fifteen duplexes into four pieces
        assert(along.duplexes == 15)
        assert(along.segments == 60)
        val across = superstructureSeverance(holes, Gen1Tile.EDGE_X, 15 * duplex, false)
        assert(across.severed)
    }

    @Test
    fun `P7 a staggered tie column leaves the superstructure in one piece`() {
        val span = coupledFlexureSpan(
            ei, flexibility, 15, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val stagger = smallestConnectingStagger(15, Gen1Tile.EDGE_X, 15 * duplex, span)
        assert(stagger > 0.0)
        val holes = staggeredTieColumn(15, 15 * duplex, stagger)
        assert(superstructureSeverance(holes, Gen1Tile.EDGE_X, 15 * duplex, true).components == 1)
    }

    @Test
    fun `P4 the single-column array is feasible at exactly one orientation out of 720`() {
        val span = coupledFlexureSpan(
            ei, flexibility, 15, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val sweep = orientationSweep(array(1, 0.0, span).map { it.midspan }, span, 720)
        assert(sweep.feasibleOrientations == 1)
        assert(sweep.singleLevelOrientations == 1)
        assert(sweep.bestAngleDegrees.isCloseTo(0.0))
        // and the one that works is exactly zero — any tilt puts adjacent rows into each other
        assert(packingVerdict(array(1, 0.0, span)).singleLevel)
        listOf(0.001, 0.05, 0.3, 1.0).forEach { angle ->
            assert(!packingVerdict(array(1, angle, span)).feasibleAtAnyLevelCount)
        }
    }

    @Test
    fun `P5 the unzip floor at the desired stroke is bracketed by the mandate and the element`() {
        // `C-0017`'s mandate secant: 33.333 pN/nm x 10 nm / 10 pN = 34 paths (`CH-0029`)
        val mandateFloor = kotlin.math.ceil(
            Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE * Gen1Tile.DESIRED_STROKE /
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        )
        assert(mandateFloor.isCloseTo(34.0))
        // the strain-SOFTENING element delivers less than its own secant, so its floor is lower
        val elementFloor = (1..200).first { count ->
            CoupledJointFlexure(
                ei,
                coupledFlexureSpan(
                    ei, flexibility, count, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
                    Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
                ),
                flexibility, stretch
            ).strokeReaction(
                Gen1Tile.DESIRED_STROKE, FlexureOrientation.FAVOURABLE
            ) <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        }
        assert(elementFloor == 29)
        assert(elementFloor > 15)
    }

    @Test
    fun `P7 the smallest connecting stagger is one duplex pitch, quantised up to eight base pairs`() {
        val span = coupledFlexureSpan(
            ei, flexibility, 15, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE, stretch
        )
        val stagger = smallestConnectingStagger(15, Gen1Tile.EDGE_X, 15 * duplex, span)
        assert(Math.round(stagger / Gen1Tile.RISE_PER_BASE_PAIR) == 8L)
        assert(stagger >= duplex)
        assert(stagger - Gen1Tile.RISE_PER_BASE_PAIR < duplex)
    }

    // ------------------------------------------------------------ the declared falsifiers

    @Test
    fun `the same-row overlap threshold angle is the closed-form arc sine`() {
        val pitch = Gen1Tile.EDGE_X / 3.0
        val threshold = asin(duplex / pitch)
        val pair = { angle: Double ->
            packingVerdict(
                listOf(
                    PlanFlexure("a", Vector2.ZERO, angle, designSpan),
                    PlanFlexure("b", Vector2(pitch, 0.0), angle, designSpan)
                )
            ).overlappingPairs
        }
        assert(pair(threshold * 0.99) == 1)
        assert(pair(threshold * 1.01) == 0)
    }
}
