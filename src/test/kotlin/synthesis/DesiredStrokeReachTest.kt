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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.AnchorMaterials
import com.xemantic.nano.plentyofroom.anchoring.FlexureEndCondition
import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.anchoring.TransverseDuplexFlexure
import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.flexureSpanForStiffness
import com.xemantic.nano.plentyofroom.anchoring.hingeArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.hingeLineCensus
import com.xemantic.nano.plentyofroom.anchoring.packingLimitedPathCount
import com.xemantic.nano.plentyofroom.anchoring.perInterfacePitch
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.layerDesignPoint
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.coupling.LinearCoupling
import com.xemantic.nano.plentyofroom.coupling.OutputCharacteristic
import com.xemantic.nano.plentyofroom.coupling.firstOperatingStroke
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-107` and `T-108` — the stroke a compliance ceiling is owed at, and whether §3's **desired**
 * 10 nm stroke is reachable by anything this programme has.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of both tasks is one discipline `CLAUDE.md` already records seven times: a quantity
 * is not well posed without the state it is read at. `T-107` applies it to an **acceptance
 * clause** rather than to a model, and `T-108` applies it to the stroke itself — the stroke `s`
 * is `L₀ − h`, so it is bounded above by the layer's own resting height, and §3 never names a
 * layer taller than 10 nm.
 */
class DesiredStrokeReachTest {

    private val targetForce = 100.0

    private val acceptableStroke = 3.0

    private val desiredStroke = 10.0

    private val mandate = mandatedCouplingStiffness(targetForce, acceptableStroke)

    private val unzipAllowable = 10.0

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the declared ceiling is a multiple of a force over a length`() {
        // C-0023's ceiling is 1.2 x the mandate, and the mandate is 100 pN over 3 nm
        assert(mandate.isCloseTo(100.0 / 3.0))
        assert(declaredComplianceCeiling(targetForce, acceptableStroke).isCloseTo(40.0))
        // it is linear in the force and inverse in the stroke, exactly as the mandate is
        assert(declaredComplianceCeiling(200.0, acceptableStroke).isCloseTo(80.0))
        assert(declaredComplianceCeiling(targetForce, 6.0).isCloseTo(20.0))
        // and the factor is a declared design tolerance, carried explicitly
        assert(DECLARED_CEILING_FACTOR.isCloseTo(1.2))
        assert(
            declaredComplianceCeiling(targetForce, acceptableStroke, factor = 1.0)
                .isCloseTo(mandate)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the same construction at the DESIRED stroke is 12 pN per nm`() {
        // §3's desired clause is 100 pN at ~10 nm, so its own mandate is 10 pN/nm
        assert(mandatedCouplingStiffness(targetForce, desiredStroke).isCloseTo(10.0))
        // and 1.2x of it is 12, not 40 — the ceiling FALLS with the stroke it is read at
        assert(declaredComplianceCeiling(targetForce, desiredStroke).isCloseTo(12.0))
        assert(
            (declaredComplianceCeiling(targetForce, acceptableStroke) /
                    declaredComplianceCeiling(targetForce, desiredStroke)).isCloseTo(10.0 / 3.0)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the per-path ceilings are a force and a stiffness`() {
        // n x allowable is a force; divided by a stroke it is a stiffness
        assert(perPathReaction(100.0, 45).isCloseTo(100.0 / 45.0))
        assert(perPathSecantCeiling(unzipAllowable, 45, acceptableStroke).isCloseTo(150.0))
        assert(perPathSecantCeiling(unzipAllowable, 45, desiredStroke).isCloseTo(45.0))
        assert(perPathSecantCeiling(unzipAllowable, 15, acceptableStroke).isCloseTo(50.0))
        assert(perPathSecantCeiling(unzipAllowable, 15, desiredStroke).isCloseTo(15.0))
        // the thermal ceiling is (n F)^2 / k_BT, a stiffness
        assert(
            thermalForceStiffnessCeiling(unzipAllowable, 45)
                .isCloseTo(450.0 * 450.0 / thermalEnergy(ROOM_TEMPERATURE))
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the reach ceilings are lengths and unphysical arguments throw`() {
        assert(kinematicStrokeCeiling(10.0, 0.4).isCloseTo(9.6))
        assert(validityStrokeCeiling(10.0, 0.4, 0.2).isCloseTo(8.0))
        assertFailsWith<IllegalArgumentException> { declaredComplianceCeiling(-1.0, 3.0) }
        assertFailsWith<IllegalArgumentException> { perPathSecantCeiling(10.0, 0, 3.0) }
        assertFailsWith<IllegalArgumentException> { kinematicStrokeCeiling(1.0, 2.0) }
        assertFailsWith<IllegalArgumentException> { validityStrokeCeiling(10.0, 0.4, 0.0) }
        assertFailsWith<IllegalArgumentException> { traversedStrokeRange(0.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a LINEAR coupling placed at the mandate is inside the ceiling at EVERY stroke`() {
        // secant = tangent for a linear law, so 33.333 < 40 everywhere: the declared ceiling
        // can only ever bind on a NONLINEAR law, which is what makes it a linearity tolerance
        val linear = LinearCoupling(stiffness = mandate)
        val ceiling = declaredComplianceCeiling(targetForce, acceptableStroke)
        listOf(0.5, 3.0, 6.0, 10.0, 30.0).forEach { s ->
            assert(linear.tangentStiffness(s).isCloseTo(mandate))
            assert(linear.tangentStiffness(s) < ceiling)
        }
    }

    @Test
    fun `gate 2 limiting cases - the validity ceiling tends to the kinematic one as the crossover tends to a melt`() {
        val resting = 10.0
        val dry = 0.4
        assert(validityStrokeCeiling(resting, dry, 1.0).isCloseTo(kinematicStrokeCeiling(resting, dry)))
        // and it vanishes when the crossover sits at the layer's own resting volume fraction
        assert(validityStrokeCeiling(resting, dry, dry / resting).isCloseTo(0.0))
        // monotone in the crossover fraction
        assert(validityStrokeCeiling(resting, dry, 0.1) < validityStrokeCeiling(resting, dry, 0.2))
    }

    @Test
    fun `gate 2 limiting cases - the kinematic ceiling is strictly below the resting height, always`() {
        // this is the whole of T-108's cheap bound: s = L0 - h and h > N sigma v0 > 0
        listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024).forEach { (height, density) ->
            val chain = chainAt(height, density)
            val resting = model.equilibriumHeight(chain)
            assert(chain.occupiedThickness > 0.0)
            assert(kinematicStrokeCeiling(resting, chain.occupiedThickness) < height * 1.000001)
        }
    }

    @Test
    fun `gate 2 limiting cases - the dead-load stroke vanishes at zero load and rises with it`() {
        val chain = chainAt(10.0, 0.024)
        val resting = model.equilibriumHeight(chain)
        val load = { h: Double -> model.load(chain, h, 1600.0) }
        val floor = chain.occupiedThickness * 1.01
        val tiny = deadLoadStroke(resting, floor, 1.0e-6, load = load)
        assert(tiny < 0.05)
        val strokes = listOf(10.0, 30.0, 100.0, 300.0)
            .map { deadLoadStroke(resting, floor, it, load = load) }
        strokes.zipWithNext().forEach { (low, high) -> assert(high > low) }
        // and it never leaves the kinematic ceiling
        assert(strokes.last() < kinematicStrokeCeiling(resting, chain.occupiedThickness))
    }

    @Test
    fun `gate 2 limiting cases - the stability requirement is identically zero at zero stroke`() {
        // CH-0047's identity, which is why the traversed range starts where no coupling is needed
        val range = traversedStrokeRange(acceptableStroke)
        assert(range.first.isCloseTo(0.0))
        assert(range.second.isCloseTo(acceptableStroke))
        // at s = 0 the tile sits at L0: the layer carries nothing and the bias is zero
        val chain = chainAt(10.0, 0.024)
        assert(model.load(chain, model.equilibriumHeight(chain), 1600.0).isCloseTo(0.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - CH-0047's two ranges differ exactly when the minimum is at an endpoint`() {
        // C-0032's L4 adverse line, stylised: a tangent that RISES with the stroke, so the
        // [0, 10] minimum is its zero-stroke boundary value and the [3, 10] minimum is not
        val rising = { s: Double -> 23.515 + 2.13 * s }
        assert(minimumTangent(0.0, 10.0, tangent = rising).isCloseTo(23.515, 1e-6))
        assert(minimumTangent(3.0, 10.0, tangent = rising).isCloseTo(23.515 + 3.0 * 2.13, 1e-6))
        // an INTERIOR minimum, which is C-0030's own case, is the same on both ranges
        val interior = { s: Double -> 22.875 + 0.5 * (s - 4.555) * (s - 4.555) }
        assert(
            minimumTangent(0.0, 10.0, tangent = interior)
                .isCloseTo(minimumTangent(3.0, 10.0, tangent = interior), 1e-6)
        )
        // and over a degenerate range it is the tangent at the point
        assert(minimumTangent(3.0, 3.0, tangent = rising).isCloseTo(rising(3.0)))
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the per-path force ceiling and the per-path secant ceiling are one statement`() {
        // exactly C-0023's `F_req = k_req sigma` identity, one power of the STROKE apart
        listOf(1.0, 3.0, 7.5, 10.0, 25.0).forEach { s ->
            listOf(8, 15, 34, 45).forEach { n ->
                assert(
                    (perPathSecantCeiling(unzipAllowable, n, s) * s)
                        .isCloseTo(n * unzipAllowable)
                )
            }
        }
    }

    @Test
    fun `gate 3 conservation - the reach ceilings scale exactly with the layer`() {
        // under L0 -> lambda L0 and dry -> lambda dry every stroke ceiling scales by lambda
        val lambda = 2.5
        assert(
            kinematicStrokeCeiling(lambda * 10.0, lambda * 0.4)
                .isCloseTo(lambda * kinematicStrokeCeiling(10.0, 0.4))
        )
        assert(
            validityStrokeCeiling(lambda * 10.0, lambda * 0.4, 0.2)
                .isCloseTo(lambda * validityStrokeCeiling(10.0, 0.4, 0.2))
        )
    }

    @Test
    fun `gate 3 conservation - the delivered stroke is monotone DECREASING in the coupling stiffness`() {
        // C-0017's own gate-2 theorem, re-derived here: it is what makes the FREE stroke the
        // supremum over every coupling, and therefore what lets one bound cover the catalogue
        val characteristic = OutputCharacteristic { s -> 400.0 - 60.0 * s }
        val free = 400.0 / 60.0
        val strokes = listOf(1.0, 3.0, 10.0, 33.333, 100.0, 300.0).map { k ->
            firstOperatingStroke(characteristic, LinearCoupling(k), maximumStroke = 20.0)!!
        }
        strokes.zipWithNext().forEach { (soft, stiff) -> assert(stiff < soft) }
        strokes.forEach { assert(it < free) }
    }

    @Test
    fun `gate 3 conservation - the binding constraint is the FIRST failing predicate in declaration order`() {
        val all = ElementPredicates(
            placesAtMandate = true, insideComplianceCeiling = true, insidePerPathAllowable = true,
            latticeSupplies = true, packs = true, stableAtTwoMillimolar = true,
            stableAtHalfMillimolar = true, reachesTheStroke = true
        )
        assert(bindingConstraint(all) == NO_BINDING_CONSTRAINT)
        assert(bindingConstraint(all.copy(packs = false)) == "packing (C-0041)")
        // declaration order is the tie-break, so two failures report the first
        assert(
            bindingConstraint(all.copy(placesAtMandate = false, packs = false)) ==
                    "placement (C-0017)"
        )
    }

    @Test
    fun `gate 3 conservation - the placement predicate is an EQUALITY at the placement stroke and a FORCE clause beyond it`() {
        fun row(secant: Double, mandate: Double, equality: Boolean) = catalogueRow(
            element = "probe", owner = "T-108", readAtStroke = 10.0, pathCount = 45,
            elementSpan = 12.0, secant = secant, tangent = secant, mandate = mandate,
            complianceCeiling = declaredComplianceCeiling(targetForce, acceptableStroke),
            unzipAllowable = unzipAllowable, stabilityFloorTwoMillimolar = 0.0,
            stabilityFloorHalfMillimolar = 0.0, latticeSupplies = true, packs = true,
            reachesTheStroke = true, placementIsEquality = equality
        )
        // §3's desired clause asks for >= 100 pN over ~10 nm, so over-delivery is not a failure
        assert(!row(33.333, 10.0, equality = true).predicates.placesAtMandate)
        assert(row(33.333, 10.0, equality = false).predicates.placesAtMandate)
        assert(!row(9.0, 10.0, equality = false).predicates.placesAtMandate)
    }

    @Test
    fun `gate 3 conservation - a refusal is not a small number`() {
        // C-0039's solver REFUSES a stroke past the arm's own fold rather than approximating it,
        // and a synthesis has to carry the refusal rather than a zero that reads as a stiffness
        val row = infeasibleCatalogueRow(
            element = "E5a1", owner = "C-0039", readAtStroke = 10.0, pathCount = 45,
            elementSpan = 9.131, complianceCeiling = 40.0, unzipAllowable = unzipAllowable,
            reason = "the arm folds before reaching the desired stroke"
        )
        assert(!row.lawEvaluable)
        assert(!row.packingAssessed)
        assert(!row.predicates.clears)
        assert(row.bindingConstraint == "the element's own law does not reach this stroke")
        assert(row.perPathSecantCeiling.isCloseTo(45.0))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the dead-load stroke exits on the bracket width and is scan-independent`() {
        val chain = chainAt(10.0, 0.024)
        val resting = model.equilibriumHeight(chain)
        val load = { h: Double -> model.load(chain, h, 1600.0) }
        val floor = chain.occupiedThickness * 1.01
        val coarse = deadLoadStroke(resting, floor, 100.0, scanSteps = 64, load = load)
        val fine = deadLoadStroke(resting, floor, 100.0, scanSteps = 4096, load = load)
        assert(coarse.isCloseTo(fine, 1e-9))
        // and the solved height reproduces the target load
        assert(load(resting - fine).isCloseTo(100.0, 1e-7))
    }

    @Test
    fun `gate 4 convergence - the resting height solved for a stroke reproduces that stroke`() {
        val target = 10.0
        fun strokeAt(height: Double): Double {
            val chain = chainAt(height, 0.024)
            return deadLoadStroke(
                model.equilibriumHeight(chain), chain.occupiedThickness * 1.01, 100.0
            ) { h -> model.load(chain, h, 1600.0) }
        }
        val solved = restingHeightForStroke(target, low = 10.0, high = 80.0) { strokeAt(it) }
        assert(strokeAt(solved).isCloseTo(target, 1e-6))
        assert(solved > target)
    }

    // ----------------------------------------------------- gate 5 — upstream reproduction

    @Test
    fun `gate 5 upstream - C-0017's mandate and C-0023's declared ceiling`() {
        assert(mandate.isCloseTo(33.33333333333333))
        assert(declaredComplianceCeiling(targetForce, acceptableStroke).isCloseTo(40.0))
        // C-0017's K1: 45 duplex standoffs at 220 pN/nm are 9900, 297x the mandate
        assert((45 * 220.0 / mandate).isCloseTo(297.0))
    }

    @Test
    fun `gate 5 upstream - C-0023's E3a span and E5 arm, re-run as a library`() {
        val span = flexureSpanForStiffness(
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            endCondition = FlexureEndCondition.PINNED_ENDS,
            axiallyRestrained = false,
            stretchModulus = AnchorMaterials.DUPLEX_STRETCH_MODULUS,
            count = 45,
            targetStiffness = mandate,
            workingDisplacement = acceptableStroke
        )
        assert(span.isCloseTo(24.61, 1e-3))
        val arm = hingeArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            armBendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            count = 45,
            targetStiffness = mandate
        )
        assert(arm.isCloseTo(4.11, 1e-2))
    }

    @Test
    fun `gate 5 upstream - C-0039's E5a16 arm, tangents and secant at both strokes`() {
        val arm = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            hingeCount = 16,
            farStiffness = C0034_DUPLEX_END_ANCHORAGE
        )
        assert(arm.isCloseTo(12.7198, 1e-4))
        val element = TwoSpringElastica(
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            length = arm,
            nearStiffness = 16 * Gen1Tile.crossoverHingeStiffness(),
            farStiffness = C0034_DUPLEX_END_ANCHORAGE
        )
        assert((45 * element.secantStiffness(3.0)).isCloseTo(mandate, 1e-6))
        assert((45 * element.tangentStiffness(3.0)).isCloseTo(36.44, 1e-3))
        assert((45 * element.secantStiffness(10.0)).isCloseTo(69.94, 1e-3))
        assert((45 * element.tangentStiffness(10.0)).isCloseTo(264.24, 1e-3))
        // and the force it would take to deliver the desired stroke — 7x §3's own 100 pN
        assert((45 * element.reaction(10.0)).isCloseTo(699.4, 1e-3))
    }

    @Test
    fun `gate 5 upstream - the ONE hinge count 45 paths can be given clears the ceiling on the elastica`() {
        // C-0040 reports 54.11 pN/nm at one crossover, on C-0034's series composition — the
        // pipeline CH-0053 supersedes. On C-0039's own exact elastica the same design places at
        // a 9.13 nm arm and 39.18 pN/nm, INSIDE C-0023's ceiling. That is CH-0058.
        val arm = elasticaArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            hingeCount = 1,
            farStiffness = C0034_DUPLEX_END_ANCHORAGE
        )
        assert(arm.isCloseTo(9.131, 1e-3))
        val element = TwoSpringElastica(
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            length = arm,
            nearStiffness = Gen1Tile.crossoverHingeStiffness(),
            farStiffness = C0034_DUPLEX_END_ANCHORAGE
        )
        val tangent = 45 * element.tangentStiffness(acceptableStroke)
        assert(tangent.isCloseTo(39.18, 1e-3))
        assert(tangent < declaredComplianceCeiling(targetForce, acceptableStroke))
        // C-0040's own number at the same count, and the departure between the two pipelines
        assert((54.1134674 / tangent).isCloseTo(1.3812, 1e-3))
    }

    @Test
    fun `gate 5 upstream - C-0040's hinge line carries four and C-0041's array carries fifteen`() {
        val census = hingeLineCensus(Gen1Tile.EDGE_X)
        assert(census.size == 32)
        assert(census.maxOf { it.largest } == 4)
        assert(census.minOf { it.largest } == 4)
        assert(perInterfacePitch().isCloseTo(32.0 * 0.34))
        assert(packingLimitedPathCount(edgeX = 40.0, rows = 15, span = 21.44) == 15)
    }

    @Test
    fun `gate 5 upstream - C-0001's dead-load stroke at the 10 nm design point, and it is not ten`() {
        val point = layerDesignPoint(
            layerHeight = 10.0,
            graftingDensity = 0.024022488679628622,
            monomerSize = 0.35,
            tileArea = 1600.0,
            targetForce = 100.0
        )
        val strokes = point.responses.map { it.strokeUnderTargetForce }
        assert(strokes.min().isCloseTo(3.497778876010978, 1e-6))
        assert(strokes.max().isCloseTo(5.345682143299053, 1e-6))
        // §3's desired stroke is not reached by ANY of C-0001's four models
        assert(strokes.max() < 10.0)
    }

    @Test
    fun `gate 5 upstream - C-0018's stroke ceiling at 10 nm is the layer's own dry thickness`() {
        // T-4 reports strokeCeiling = L0 - max(1.01 x dry, 0.5 nm); every one of its six
        // models at 10 nm lands in 9.45-9.50 nm, and none of them reaches ten
        listOf(0.325718795, 0.3605226, 0.403234757, 0.417953069, 0.464342769, 0.542244462)
            .forEach { dry ->
                val ceiling = kinematicStrokeCeiling(10.0, maxOf(1.01 * dry, 0.5))
                assert(ceiling >= 9.45)
                assert(ceiling <= 9.5)
                assert(ceiling < 10.0)
            }
    }

    @Test
    fun `gate 5 upstream - C-0032's own interior minimum, and the four floors it clears at the placement stroke`() {
        val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
        val flexibility = standoffTipFlexibility(ei, 8.0, StandoffBase.crossovers(2).rotationalStiffness)
        val span = coupledFlexureSpan(
            ei, flexibility, 45, mandate, acceptableStroke, FlexureOrientation.FAVOURABLE
        )
        val beam = CoupledJointFlexure(ei, span, flexibility)
        fun tangent(s: Double) = 45 * beam.strokeTangentStiffness(s, FlexureOrientation.FAVOURABLE)
        // C-0032 and CH-0047 both report 22.875 pN/nm at an INTERIOR minimum near 4.555 nm
        assert(minimumTangent(3.0, 10.0, 4096, ::tangent).isCloseTo(22.875, 1e-4))
        // and at the stroke the device is actually PLACED at, the tangent is 25.227 — which is
        // above four of C-0017's six 2 mM floors, where the interior minimum is above none
        val floors = listOf(27.9133262, 23.4139164, 24.9042565, 27.0387111, 23.8036442, 23.9527371)
        assert(tangent(acceptableStroke).isCloseTo(25.227, 1e-4))
        assert(floors.count { tangent(acceptableStroke) > it } == 4)
        assert(floors.count { 22.875 > it } == 0)
    }

    @Test
    fun `gate 5 upstream - C-0032's escape is past the ceiling and C-0030's design is inside it`() {
        val ceiling = declaredComplianceCeiling(targetForce, acceptableStroke)
        // C-0030's favourable mounting: 25.227 pN/nm at the working point
        assert(25.227 < ceiling)
        // C-0030's adverse mounting: 44.817, which is C-0032's failed escape
        assert(44.817 > ceiling)
        assert((44.817 / ceiling).isCloseTo(1.120425, 1e-5))
        // C-0028's decoupled reading: 36.508, inside
        assert(36.508 < ceiling)
    }

    // ---------------------------------------------------------------- helpers

    private val peg = PegWater()

    /** One of `C-0003`'s six — the two-body Alexander box, cheapest of the family. */
    private val model = AlexanderBoxLayer(
        twoBodyInteraction(peg.reducedSecondVirialCoefficient(1.9e-3), peg.monomerVolume)
    )

    private fun chainAt(height: Double, density: Double): GraftedChain =
        peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)

    private val C0034_DUPLEX_END_ANCHORAGE = 78.2352941176

}
