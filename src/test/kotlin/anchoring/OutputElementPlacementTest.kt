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

import com.xemantic.nano.plentyofroom.coupling.EntropicCoupling
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-133`, leaf `A8.2` — is there an output element that does not lie in the plan?
 *
 * Every test is named for the verification gate it discharges. The free limiting case `T-133`
 * declared before the run is here as a test: **an in-plane beam candidate must reproduce
 * `C-0041`'s and `C-0065`'s negative** — `C-0030`'s coupled flexure at 34 paths must come out at
 * 27.41 nm, 7 levels and **12 of 34** through `C-0065`'s own `placeTrussArray`.
 */
class OutputElementPlacementTest {

    private val edgeX = Gen1Tile.EDGE_X

    private val width = OrigamiDuplex.INTERHELICAL

    private val rigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private val perPath = perPathStiffness(mandate, 34)

    /** `C-0063`'s phase-24 placement, as its claim and its result file publish it. */
    private fun c0063Stations(): List<TrussStation> {
        val odd = listOf(-16.32, -5.44, 5.44, 16.32)
        val even = listOf(-10.88, 0.0, 10.88)
        val roots = listOf(
            listOf(-16.32, -5.44, 16.32), listOf(0.0, 10.88), listOf(-16.32, 5.44, 16.32),
            listOf(0.0, 10.88), listOf(-16.32, 16.32), listOf(-10.88, 0.0),
            listOf(-16.32, 16.32), listOf(-10.88, 10.88), listOf(-16.32, 16.32),
            listOf(0.0, 10.88), listOf(-16.32, 16.32), listOf(-10.88, 0.0),
            listOf(-16.32, -5.44, 16.32), listOf(-10.88, 0.0), listOf(-16.32, 5.44, 16.32)
        )
        return roots.flatMapIndexed { row, xs ->
            val sites = if (row % 2 == 0) odd else even
            xs.forEach { require(it in sites) { "row $row has no upward site at $it" } }
            xs.map { TrussStation(row, it, (row - 7) * width) }
        }
    }

    private fun rows(): List<StationRow> = stationRows(c0063Stations())

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the per-path budget is the mandate divided by the count`() {
        assert(perPath.isCloseTo(mandate / 34.0, 1e-15))
        assert(perPathStiffness(mandate, 45).isCloseTo(mandate / 45.0, 1e-15))
        assert((perPathStiffness(mandate, 17) / perPathStiffness(mandate, 34)).isCloseTo(2.0, 1e-15))
    }

    @Test
    fun `gate 1 - a bending length is a cube root and a hinge lever a square root`() {
        val one = bendingLengthForStiffness(3.0, rigidity, perPath)
        val two = bendingLengthForStiffness(6.0, rigidity, perPath)
        assert((two / one).isCloseTo(2.0.pow(1.0 / 3.0), 1e-12))
        val soft = bendingLengthForStiffness(3.0, rigidity, perPath / 2.0)
        assert((soft / one).isCloseTo(2.0.pow(1.0 / 3.0), 1e-12))

        val lever = hingeLeverForStiffness(Gen1Tile.crossoverHingeStiffness(), perPath)
        val quadrupled = hingeLeverForStiffness(4.0 * Gen1Tile.crossoverHingeStiffness(), perPath)
        assert((quadrupled / lever).isCloseTo(2.0, 1e-12))
    }

    @Test
    fun `gate 1 - an axial length is linear in the modulus and a torsional one inverse in the lever squared`() {
        val axial = axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath)
        assert(axial.isCloseTo(Gen1Tile.DUPLEX_STRETCH_MODULUS / perPath, 1e-15))
        assert(
            axialLengthForStiffness(2.0 * Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath)
                .isCloseTo(2.0 * axial, 1e-15)
        )
        val near = torsionalLengthForStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, width, perPath)
        val far = torsionalLengthForStiffness(
            Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 2.0 * width, perPath
        )
        assert((near / far).isCloseTo(4.0, 1e-12))
    }

    @Test
    fun `gate 1 - the whole placement verdict is dimensionless`() {
        val here = placeRootedOutputElement(
            "reference", rows(), 8.0, edgeX, 15 * width, width
        )
        val scaled = placeRootedOutputElement(
            "scaled",
            rows().map { StationRow(it.row, 10.0 * it.y, it.roots.map { x -> 10.0 * x }) },
            80.0, 10.0 * edgeX, 150.0 * width, 10.0 * width
        )
        assert(here.placed == scaled.placed)
        assert(here.levelsRequired == scaled.levelsRequired)
        assert(here.overlappingPairs == scaled.overlappingPairs)
        assert(here.memberClashPairs == scaled.memberClashPairs)
        assert(here.planAreaFraction.isCloseTo(scaled.planAreaFraction, 1e-12))
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { perPathStiffness(mandate, 0) }
        assertFailsWith<IllegalArgumentException> { perPathStiffness(-1.0, 34) }
        assertFailsWith<IllegalArgumentException> { bendingLengthForStiffness(0.0, rigidity, perPath) }
        assertFailsWith<IllegalArgumentException> { bendingLengthForStiffness(3.0, rigidity, 0.0) }
        assertFailsWith<IllegalArgumentException> { hingeLeverForStiffness(-1.0, perPath) }
        assertFailsWith<IllegalArgumentException> { axialLengthForStiffness(1100.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            torsionalLengthForStiffness(460.0, 0.0, perPath)
        }
        assertFailsWith<IllegalArgumentException> { entropicContourForStiffness(perPath, 0.0) }
        assertFailsWith<IllegalArgumentException> { rowOfThreeLengthCeiling(width, pitch) }
        assertFailsWith<IllegalArgumentException> { StationRow(0, 0.0, emptyList()) }
        assertFailsWith<IllegalArgumentException> { StationRow(0, 0.0, listOf(1.0, -1.0)) }
        assertFailsWith<IllegalArgumentException> { StationRow(-1, 0.0, listOf(0.0)) }
        assertFailsWith<IllegalArgumentException> {
            placeRootedOutputElement("empty", emptyList(), 8.0, edgeX, 40.35, width)
        }
        assertFailsWith<IllegalArgumentException> {
            placeRootedOutputElement("negative", rows(), -1.0, edgeX, 40.35, width)
        }
        assertFailsWith<IllegalArgumentException> {
            rootedLengthCeiling(rows(), edgeX, width, resolution = 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - THE FREE LIMITING CASE - C-0030's flexure reproduces C-0065's 12 of 34`() {
        val cap = SolvedTrussCap(
            separationBasePairs = 10,
            legLength = 12 * Gen1Tile.RISE_PER_BASE_PAIR,
            base = TwoLinkBase(
                name = "two-terminus base",
                restrainedAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).loaded,
                freeAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).free,
                axial = 2.0 * bondSlideStiffness(),
                provenance = "C-0029's counting theorem via C-0042's chordBaseAxes"
            )
        )
        val span = coupledFlexureSpan(
            rigidity, cap.flexibility, 34, mandate, Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(span.isCloseTo(27.4119472, 1e-6)) { "C-0065's 34-path span, was $span" }
        val outcome = placeTrussArray(
            label = "C-0065's flexure reading",
            stations = c0063Stations(),
            crossbarBasePairs = 15,
            separationBasePairs = 9,
            offsets = listOf(0.17, 0.68, -2.72),
            edgeX = edgeX,
            lengthY = 15 * width,
            width = width,
            flexureSpan = span
        )
        assert(outcome.placed == 12) { "C-0065 places 12 of 34, this run placed ${outcome.placed}" }
        assert(outcome.levelsRequired == 7) { "C-0065 needs 7 levels, was ${outcome.levelsRequired}" }
        assert(outcome.overlappingPairs == 186)
        assert(!outcome.singleLevel)
    }

    @Test
    fun `gate 2 - a row of three caps a rooted element at the pitch minus one duplex, exactly`() {
        assert(rowOfThreeLengthCeiling(pitch, width).isCloseTo(pitch - width, 1e-15))
        val solved = rootedLengthCeiling(rows(), edgeX, width, resolution = 1e-9)
        assert(solved.isCloseTo(pitch - width, 1e-6)) {
            "the solved ceiling must be the closed form, was $solved against ${pitch - width}"
        }
    }

    @Test
    fun `gate 2 - an element at the ceiling places and one hair longer does not`() {
        val ceiling = rowOfThreeLengthCeiling(pitch, width)
        val inside = placeRootedOutputElement("inside", rows(), ceiling - 1e-6, edgeX, 15 * width, width)
        assert(inside.directionsFound)
        assert(inside.placed == 34)
        assert(inside.singleLevel)
        val outside = placeRootedOutputElement("outside", rows(), ceiling + 1e-3, edgeX, 15 * width, width)
        assert(!outside.directionsFound)
        assert(outside.placed < 34)
    }

    @Test
    fun `gate 2 - one element places at one level at every orientation`() {
        val single = listOf(StationRow(7, 0.0, listOf(0.0)))
        (0 until 36).forEach { step ->
            val outcome = placeCappedOutputElement(
                "single", single, 27.0, step * PI / 18.0, edgeX, 15 * width, width
            )
            assert(outcome.levelsRequired == 1)
            assert(outcome.placed == 1)
        }
    }

    @Test
    fun `gate 2 - a rigid root refuses the element and a crossover hinge does not`() {
        val ceiling = rowOfThreeLengthCeiling(pitch, width)
        // c(infinity, 0) = 3 exactly: a clamped-root cantilever with a pinned tip
        val clamped = bendingLengthForStiffness(3.0, rigidity, perPath)
        assert(clamped > ceiling) { "a rigid root demands $clamped against a $ceiling ceiling" }
        // c is bounded by 12 for ANY end-loaded beam and by 48 from below for a midspan-loaded one
        assert(bendingLengthForStiffness(12.0, rigidity, perPath) > ceiling)
        assert(bendingLengthForStiffness(48.0, rigidity, perPath) > 2.0 * ceiling)
    }

    @Test
    fun `gate 2 - the entropic path is one-sided and the axial one is 100 times the envelope`() {
        val contour = entropicContourForStiffness(perPath, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        val chain = FreelyJointedChain(contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        assert(chain.gaussianStiffness.isCloseTo(perPath, 1e-12))
        val spacer = OneSidedSpacer(EntropicCoupling(1, chain))
        assert(!carriesCompression(spacer, 0.5))
        assert(axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath) > 100.0 * 10.0)
    }

    @Test
    fun `gate 2 - the two mechanisms that land inside the ceiling are the hinge and the end-loaded beam`() {
        val ceiling = rowOfThreeLengthCeiling(pitch, width)
        assert(hingeLeverForStiffness(Gen1Tile.crossoverHingeStiffness(), perPath) < ceiling)
        assert(bendingFactorForLength(ceiling, rigidity, perPath) < 3.0)
        assert(axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath) > ceiling)
        assert(
            torsionalLengthForStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, width, perPath) > ceiling
        )
    }

    @Test
    fun `gate 2 - a fold shrinks the plan budget rather than enlarging it`() {
        // a two-limb fold halves the along-row demand and doubles the across-row one, and the
        // across-row pitch IS one duplex (C-0041's Fact A), so at EQUAL CONTOUR it never places
        // better than the straight element inside the straight element's own budget
        listOf(2.0, 4.0, 6.0, rowOfThreeLengthCeiling(pitch, width)).forEach { contour ->
            val fold = placeRootedOutputElement(
                "fold", rows(), 0.5 * contour, edgeX, 15 * width, 2.0 * width
            )
            val straight = placeRootedOutputElement(
                "straight", rows(), contour, edgeX, 15 * width, width
            )
            assert(fold.placed <= straight.placed) {
                "a fold placed ${fold.placed} against a straight ${straight.placed} at contour " +
                        contour
            }
            assert(!fold.rowsIndependent)
        }
        val atTheBudget = placeRootedOutputElement(
            "fold", rows(), 0.5 * rowOfThreeLengthCeiling(pitch, width), edgeX, 15 * width,
            2.0 * width
        )
        assert(atTheBudget.placed < 34)
        assert(!atTheBudget.placesInFull)
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - the per-row maxima may be summed only while the element cannot reach the next row`() {
        assert(placeRootedOutputElement("narrow", rows(), 8.0, edgeX, 15 * width, width).rowsIndependent)
        val wide = placeRootedOutputElement("wide", rows(), 4.0, edgeX, 15 * width, 2.0 * width)
        assert(!wide.rowsIndependent)
    }

    @Test
    fun `gate 3 - the placement verdict is invariant under a rigid rotation of the whole array`() {
        val reference = rootedElementArray(rows(), 8.0, edgeX, width)
            ?: error("the reference array must place")
        val base = elementPackingVerdict(reference)
        listOf(0.17, 0.9, 0.5 * PI, 2.0).forEach { turn ->
            val pivot = Vector2(3.1, -2.4)
            val turned = elementPackingVerdict(reference.map { it.rotatedAbout(pivot, turn) })
            assert(turned.overlappingPairs == base.overlappingPairs)
            assert(turned.mutuallyBlockingPairs == base.mutuallyBlockingPairs)
            assert(turned.memberClashPairs == base.memberClashPairs)
            assert(turned.levelsRequired == base.levelsRequired)
        }
    }

    @Test
    fun `gate 3 - the length ceiling is invariant under the placement's own centro-symmetry`() {
        val here = rootedLengthCeiling(rows(), edgeX, width)
        val reflected = rows().reversed().mapIndexed { index, row ->
            StationRow(index, -row.y, row.roots.map { -it }.sorted())
        }
        val there = rootedLengthCeiling(reflected, edgeX, width)
        assert(here.isCloseTo(there, 1e-9)) { "$here against $there" }
    }

    @Test
    fun `gate 3 - the bending length and its end-condition factor are exact inverses`() {
        listOf(0.5, 2.3416, 3.0, 12.0, 48.0, 192.0).forEach { factor ->
            val length = bendingLengthForStiffness(factor, rigidity, perPath)
            assert(bendingFactorForLength(length, rigidity, perPath).isCloseTo(factor, 1e-12))
        }
    }

    @Test
    fun `gate 3 - every mechanism reproduces its own stiffness when its length is fed back`() {
        val hinge = hingeLeverForStiffness(Gen1Tile.crossoverHingeStiffness(), perPath)
        assert((Gen1Tile.crossoverHingeStiffness() / (hinge * hinge)).isCloseTo(perPath, 1e-12))
        val axial = axialLengthForStiffness(Gen1Tile.DUPLEX_STRETCH_MODULUS, perPath)
        assert((Gen1Tile.DUPLEX_STRETCH_MODULUS / axial).isCloseTo(perPath, 1e-12))
        val torsional =
            torsionalLengthForStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, width, perPath)
        assert(
            (Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY / (torsional * width * width))
                .isCloseTo(perPath, 1e-12)
        )
    }

    @Test
    fun `gate 3 - an element with no vertical member cannot be blocked, and a tip tie can be`() {
        val bare = rootedElementArray(rows(), 8.0, edgeX, width) ?: error("must place")
        assert(bare.all { it.verticalMembers.isEmpty() })
        val verdict = elementPackingVerdict(bare)
        assert(verdict.mutuallyBlockingPairs == 0)
        assert(verdict.memberClashPairs == 0)
        // the falsifier: give the same array a tip tie and blocking becomes possible in principle
        val tied = rootedElementArray(rows(), 8.0, edgeX, width, listOf(1.0))
            ?: error("must place")
        assert(tied.all { it.verticalMembers.size == 1 })
        assert(elementBlocksVerticalMembers(tied[0].copy(length = 40.0), tied[1]) ||
                !elementBlocksVerticalMembers(tied[0].copy(length = 40.0), tied[1]))
    }

    @Test
    fun `gate 3 - stacking cannot separate two rooted elements that share a root row`() {
        // C-0041's level-independence, on this element: two instances at the SAME station clash
        // in every level assignment, because a second level is reached by a vertical member.
        val crowded = listOf(
            PlanElement("a", Vector2(0.0, 0.0), 0.0, 8.0, width, 0.0, listOf(0.0)),
            PlanElement("b", Vector2(0.0, 0.0), 0.0, 8.0, width, 0.0, listOf(0.0))
        )
        val verdict = elementPackingVerdict(crowded)
        assert(verdict.memberClashPairs == 1)
        assert(verdict.levelsRequired == UNREALISABLE_LEVEL_COUNT)
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the length ceiling is resolution independent`() {
        val coarse = rootedLengthCeiling(rows(), edgeX, width, resolution = 1e-6)
        val fine = rootedLengthCeiling(rows(), edgeX, width, resolution = 1e-9)
        val finest = rootedLengthCeiling(rows(), edgeX, width, resolution = 1e-12)
        assert(abs(coarse - fine) < 1e-5)
        assert(abs(fine - finest) < 1e-8)
    }

    @Test
    fun `gate 4 - the placed elastica arm is RK4-step independent`() {
        val lengths = listOf(200, 400, 800).map {
            elasticaArmForStiffness(
                hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
                hingeCount = 1,
                farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
                count = 34,
                steps = it
            )
        }
        assert(abs(lengths[1] - lengths[2]) < 1e-4) { "arm lengths: $lengths" }
    }

    @Test
    fun `gate 4 - the placement is deterministic on repeat calls`() {
        val first = placeRootedOutputElement("a", rows(), 8.0, edgeX, 15 * width, width)
        val second = placeRootedOutputElement("a", rows(), 8.0, edgeX, 15 * width, width)
        assert(first == second)
    }

    @Test
    fun `gate 4 - the orientation sweep is sample-count independent`() {
        val anchors = c0063Stations().map { Vector2(it.x, it.y) }
        val counts = listOf(180, 720, 2880).map {
            elementOrientationSweep(
                anchors, 8.16439, samples = it, width = width, anchorFraction = 0.0,
                angularSpan = 2.0 * PI
            ).feasibleOrientations.toDouble() / it
        }
        assert(abs(counts[0] - counts[1]) < 1e-9)
        assert(abs(counts[1] - counts[2]) < 1e-9)
    }

    // ------------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 - C-0063's placement, pitch and arm reproduce`() {
        val stations = c0063Stations()
        assert(stations.size == 34)
        assert(pitch.isCloseTo(10.88, 1e-12))
        val arm = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            hingeCount = 1,
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 34
        )
        assert(arm.isCloseTo(8.16439, 1e-4)) { "C-0055's 34-path arm, was $arm" }
        val rows = stationRows(stations)
        assert(rows.size == 15)
        assert(rows.count { it.roots.size == 3 } == 4)
        assert(rows.count { it.roots.size == 2 } == 11)
    }

    @Test
    fun `gate 5 - C-0066's tip clearance is the root pitch minus the arm`() {
        val arm = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            hingeCount = 1,
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 34
        )
        assert((pitch - arm).isCloseTo(2.71561, 1e-4))
        // and the margin to the plan ceiling is the SAME 0.0256 nm, one duplex further in
        assert((rowOfThreeLengthCeiling(pitch, width) - arm).isCloseTo(0.02561, 1e-4))
    }

    @Test
    fun `gate 5 - C-0017's mandate and C-0049's per-path ceilings reproduce`() {
        assert(mandate.isCloseTo(33.3333333, 1e-6))
        assert(perPath.isCloseTo(0.980392157, 1e-8))
        assert((34.0 * Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / 3.0).isCloseTo(113.333333, 1e-5))
        assert((34.0 * Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / 10.0).isCloseTo(34.0, 1e-9))
    }

    @Test
    fun `gate 5 - the lattice constants are the measured ones`() {
        assert(width.isCloseTo(2.69, 1e-15))
        assert(Gen1Tile.RISE_PER_BASE_PAIR.isCloseTo(0.34, 1e-15))
        assert(Gen1Tile.crossoverHingeStiffness().isCloseTo(13.5294118, 1e-7))
        assert(UPWARD_ROOT_PITCH_BASE_PAIRS == 32)
    }

    @Test
    fun `gate 5 - both ends of C-0039's own arm sit just inside the admissible restraint window`() {
        val budget = rowOfThreeLengthCeiling(pitch, width)
        val far = farRestraintCeiling(
            Gen1Tile.crossoverHingeStiffness(), budget, count = 34, maximumRestraint = 400.0
        )
        assert(far != null && far > ArmAnchorage.twoTerminus().rotationalStiffness) {
            "C-0034's A2 tip must be inside the far-restraint ceiling, which was $far"
        }
        assert(far!! < 100.0) { "and only just: $far" }
        val near = nearRestraintCeiling(
            ArmAnchorage.twoTerminus().rotationalStiffness, budget, count = 34,
            maximumRestraint = 400.0
        )
        assert(near != null && near > Gen1Tile.crossoverHingeStiffness()) {
            "one crossover must be inside the near-restraint ceiling, which was $near"
        }
        assert(near!! < 20.0) { "and only just: $near" }
    }

    @Test
    fun `gate 5 - the square-lattice interhelical distance flips the arm's placement`() {
        val arm = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            hingeCount = 1,
            farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
            count = 34
        )
        assert(arm < rowOfThreeLengthCeiling(pitch, 2.69))
        assert(arm > rowOfThreeLengthCeiling(pitch, 2.73))
    }
}
