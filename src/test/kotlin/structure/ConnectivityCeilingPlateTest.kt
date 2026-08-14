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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-120` — is a sheet held together by ONE crossover per interface still a plate?
 *
 * Every test is named for the verification gate it discharges. Three disciplines from
 * `CLAUDE.md` govern the numerics:
 *
 * - **a uniform load on a uniform Winkler foundation must dish exactly zero**, in *all three*
 *   models and at every consumption level — the strongest free falsifier available here;
 * - **zero consumption must reproduce `C-0009`'s published discreteness table exactly**, not
 *   approximately, which is what licenses reading anything off the depleted one;
 * - **mesh monotonicity holds only on nested refinements**, so gate 4 sweeps 1 ⊂ 2 ⊂ 4.
 */
class ConnectivityCeilingPlateTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val inventorySize = 56

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val area = Gen1Tile.EDGE_X * lengthY

    private val interiorPressure = Gen1Tile.TARGET_FORCE / area

    private val alongHelixRigidity = sheet.duplex.bendingRigidity / sheet.interhelicalDistance

    private fun criteria(
        retained: Int,
        foundationMultiplier: Double = 1.0
    ) = latticeDiscreteness(
        retainedCrossovers = retained,
        inventory = inventorySize,
        nominalInterfacePitch = sheet.crossoverSpacing,
        interhelicalDistance = sheet.interhelicalDistance,
        alongHelixRigidity = alongHelixRigidity,
        hingeStiffness = sheet.crossoverHingeStiffness,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT * foundationMultiplier
    )

    private fun lattice(
        consumed: Set<CrossoverSite> = emptySet(),
        supports: List<PointSupport> = emptyList(),
        subdivisions: Int = 2,
        linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        supports = supports,
        consumedCrossovers = consumed
    )

    private val inventory: List<CrossoverSite> = lattice().crossoverSites

    private fun spent(count: Int) =
        consumedSites(inventory, count, ConsumptionPattern.SPREAD)

    /** `C-0022`'s design point (2 mM, 10 nm, 0.192 V), transcribed as `C-0047`'s test does. */
    private val solvedField: PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun supportsOf(columns: Int) = couplingSupports(
        attachmentGrid(columns, duplexes, Gen1Tile.EDGE_X, lengthY),
        Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
    )

    private fun smearedPlate(retained: Int) = sheet.plate(Gen1Tile.EDGE_X, lengthY)
        .let { it.copy(rigidityY = it.rigidityY * retained / inventorySize) }

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - every criterion is a pure ratio and is invariant under a length rescaling`() {
        val lambda = 3.7
        val base = latticeDiscreteness(
            retainedCrossovers = 14, inventory = 56,
            nominalInterfacePitch = 10.88, interhelicalDistance = 2.69,
            alongHelixRigidity = 85.5, hingeStiffness = 13.53,
            foundationStiffness = 0.0126
        )
        // lengths x lambda, rigidities x lambda^4, foundation unchanged: every ell scales
        // by lambda and every ratio here is a length over a length
        val scaled = latticeDiscreteness(
            retainedCrossovers = 14, inventory = 56,
            nominalInterfacePitch = 10.88 * lambda, interhelicalDistance = 2.69 * lambda,
            alongHelixRigidity = 85.5 * pow4(lambda),
            hingeStiffness = 13.53 * pow4(lambda),
            foundationStiffness = 0.0126
        )
        assert(scaled.bendingLengthAlongHelices.isCloseTo(lambda * base.bendingLengthAlongHelices, 1e-12))
        assert(scaled.alongLengthOverPitch.isCloseTo(base.alongLengthOverPitch, 1e-12))
        assert(
            scaled.acrossLengthOverInterhelical
                .isCloseTo(base.acrossLengthOverInterhelical, 1e-12)
        )
        assert(scaled.crossoversInAnchorPatch.isCloseTo(base.crossoversInAnchorPatch, 1e-12))
    }

    @Test
    fun `gate 1 dimensional - the patch count is an area over an area and doubles with the patch`() {
        val one = crossoversInEllipticalPatch(
            listOf(0.0 to 0.0, 3.0 to 0.0, -3.0 to 0.0, 0.0 to 4.0), 0.0, 0.0, 2.0, 2.0
        )
        assert(one == 1)
        val three = crossoversInEllipticalPatch(
            listOf(0.0 to 0.0, 3.0 to 0.0, -3.0 to 0.0, 0.0 to 4.0), 0.0, 0.0, 4.0, 2.0
        )
        assert(three == 3)
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { criteria(0) }
        assertFailsWith<IllegalArgumentException> { criteria(-1) }
        assertFailsWith<IllegalArgumentException> {
            latticeDiscreteness(60, 56, 10.88, 2.69, 85.5, 13.53, 0.0126)
        }
        assertFailsWith<IllegalArgumentException> {
            latticeDiscreteness(14, 56, -10.88, 2.69, 85.5, 13.53, 0.0126)
        }
        assertFailsWith<IllegalArgumentException> {
            latticeDiscreteness(14, 56, 10.88, 2.69, 85.5, 13.53, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            latticeDiscretenessAt(0.0, 56, 10.88, 2.69, 85.5, 13.53, 0.0126)
        }
        assertFailsWith<IllegalArgumentException> {
            crossoversInEllipticalPatch(emptyList(), 0.0, 0.0, -1.0, 1.0)
        }
        assertFailsWith<IllegalArgumentException> { modelSelection(0.0, 1.0, 1.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting - zero consumption reproduces C-0009's discreteness table exactly`() {
        val intact = criteria(inventorySize)
        // the pitch is the lattice constant itself when nothing has been spent
        assert(intact.effectiveInterfacePitch.isCloseTo(sheet.crossoverSpacing, 1e-15))
        // C-0009's own D_perp = k_theta d/p and its published 3.345
        assert(intact.acrossHelixRigidity.isCloseTo(3.345047577854671, 1e-12))
        // C-0009's published row at the nominal foundation: 0.37, 0.83, 1.50, 3.9
        assert(intact.acrossLengthOverPitch.isCloseTo(0.37, 5e-3))
        assert(intact.alongLengthOverPitch.isCloseTo(0.83, 5e-3))
        assert(intact.acrossLengthOverInterhelical.isCloseTo(1.50, 5e-3))
        assert(intact.crossoversInAnchorPatch.isCloseTo(3.9, 5e-2))
        assert(intact.bendingLengthAlongHelices.isCloseTo(9.07, 5e-3))
        assert(intact.bendingLengthAcrossHelices.isCloseTo(4.03, 5e-3))
    }

    @Test
    fun `gate 2 limiting - the two ends of C-0009's foundation sweep are reproduced too`() {
        val soft = criteria(inventorySize, 0.25)
        assert(soft.alongLengthOverPitch.isCloseTo(1.18, 5e-3))
        assert(soft.acrossLengthOverInterhelical.isCloseTo(2.12, 5e-3))
        assert(soft.crossoversInAnchorPatch.isCloseTo(7.9, 5e-2))
        val stiff = criteria(inventorySize, 4.0)
        assert(stiff.alongLengthOverPitch.isCloseTo(0.59, 5e-3))
        assert(stiff.acrossLengthOverInterhelical.isCloseTo(1.06, 5e-3))
        assert(stiff.crossoversInAnchorPatch.isCloseTo(2.0, 5e-2))
    }

    @Test
    fun `gate 2 limiting - at the connectivity ceiling the anchor patch holds less than one crossover`() {
        val ceiling = criteria(inventorySize - maximumConsumedForConnectivity(56, 15))
        assert(ceiling.retainedCrossovers.isCloseTo(14.0, 1e-15))
        assert(ceiling.crossoversInAnchorPatch < 1.0)
        assert(ceiling.crossoversInAnchorPatch.isCloseTo(0.6945, 1e-3))
        // and the two matched criteria disagree about it: the across-helix one still clears 1
        assert(ceiling.acrossLengthOverInterhelical > 1.0)
        assert(ceiling.alongLengthOverPitch < 0.25)
        assert(!ceiling.continuumValidByPatchCount)
    }

    @Test
    fun `gate 2 limiting - every criterion is strictly increasing in the retained count`() {
        val counts = listOf(1, 4, 7, 11, 14, 19, 28, 42, 56)
        val patches = counts.map { criteria(it).crossoversInAnchorPatch }
        val across = counts.map { criteria(it).acrossLengthOverInterhelical }
        val along = counts.map { criteria(it).alongLengthOverPitch }
        assert(patches.zipWithNext().all { (a, b) -> b > a })
        assert(across.zipWithNext().all { (a, b) -> b > a })
        assert(along.zipWithNext().all { (a, b) -> b > a })
    }

    @Test
    fun `gate 2 limiting - a uniform load dishes a free tile exactly zero in all three models`() {
        val uniform = uniformPressure(interiorPressure)
        listOf(0, 14, 28, 42, 56).forEach { consumed ->
            val retained = inventorySize - consumed
            assert(abs(lattice(spent(consumed)).solve(uniform).peakDishing()) < 1e-9)
            assert(
                abs(
                    PlateOnFoundation(
                        smearedPlate(maxOf(retained, 1)), Gen1Tile.FOUNDATION_SECANT,
                        emptyList(), basisDegree = 12
                    ).solve(uniform).peakDishing()
                ) < 1e-9
            )
        }
        // the replacement model: fifteen uncoupled beams on one foundation
        assert(abs(lattice(inventory.toSet()).solve(uniform).peakDishing()) < 1e-9)
    }

    @Test
    fun `gate 2 limiting - the beam array is the lattice with every crossover spent`() {
        val beams = lattice(inventory.toSet())
        assert(beams.crossovers.isEmpty())
        // each duplex is then an independent beam: the sheet has fifteen components
        assert(sheetComponents(beams.crossoverSites, duplexes) == duplexes)
    }

    @Test
    fun `gate 2 limiting - the staggered retention keeps the sheet in one piece and spreads it along x`() {
        val staggered = staggeredRetention(inventory, duplexes, 8)
        assert(staggered.size == duplexes - 1)
        assert(sheetComponents(staggered, duplexes) == 1)
        assert(retainedPerInterface(staggered, duplexes).all { it == 1 })
        // C-0054's own SPREAD pattern reaches the ceiling too, and puts every survivor in
        // the two lowest columns — which is the artefact this retention exists to bracket
        val roundRobin = retainedSites(inventory, 42, ConsumptionPattern.SPREAD)
        assert(sheetComponents(roundRobin, duplexes) == 1)
        assert(roundRobin.map { it.column }.toSet().size <= 2)
        assert(staggered.map { it.column }.toSet().size >= 6)
    }

    @Test
    fun `gate 2 limiting - the staggered retention refuses a sheet whose interface has no crossover`() {
        assertFailsWith<IllegalArgumentException> {
            staggeredRetention(inventory.filter { it.lowerBeam != 3 }, duplexes, 8)
        }
        assertFailsWith<IllegalArgumentException> { staggeredRetention(inventory, 1, 8) }
        assertFailsWith<IllegalArgumentException> { staggeredRetention(inventory, duplexes, 0) }
    }

    // ------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 symmetry - each criterion inverts exactly and the inversion round-trips`() {
        fun at(retained: Double) = latticeDiscretenessAt(
            retained, inventorySize, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelixRigidity, sheet.crossoverHingeStiffness, Gen1Tile.FOUNDATION_SECANT
        )
        val patchCount = retainedForPatchCount(
            1.0, inventorySize, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelixRigidity, sheet.crossoverHingeStiffness, Gen1Tile.FOUNDATION_SECANT
        )
        assert(at(patchCount).crossoversInAnchorPatch.isCloseTo(1.0, 1e-12))
        val acrossCount = retainedForAcrossHelixCriterion(
            1.0, inventorySize, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelixRigidity, sheet.crossoverHingeStiffness, Gen1Tile.FOUNDATION_SECANT
        )
        assert(at(acrossCount).acrossLengthOverInterhelical.isCloseTo(1.0, 1e-12))
        val alongCount = retainedForAlongHelixCriterion(
            1.0, inventorySize, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelixRigidity, sheet.crossoverHingeStiffness, Gen1Tile.FOUNDATION_SECANT
        )
        assert(at(alongCount).alongLengthOverPitch.isCloseTo(1.0, 1e-12))
        // and the along-helix criterion demands MORE crossovers than the sheet owns
        assert(alongCount > inventorySize)
    }

    @Test
    fun `gate 3 symmetry - depleting to a quarter is exactly a fourfold foundation for the across criterion and is not for the along one`() {
        val depleted = criteria(inventorySize / 4)
        val stiffened = criteria(inventorySize, 4.0)
        assert(
            depleted.acrossLengthOverInterhelical
                .isCloseTo(stiffened.acrossLengthOverInterhelical, 1e-12)
        )
        assert(
            depleted.bendingLengthAcrossHelices
                .isCloseTo(stiffened.bendingLengthAcrossHelices, 1e-12)
        )
        // the along-helix criterion separates them by exactly 2 sqrt 2
        assert(
            (stiffened.alongLengthOverPitch / depleted.alongLengthOverPitch)
                .isCloseTo(2.0 * sqrt(2.0), 1e-12)
        )
        assert(
            (stiffened.crossoversInAnchorPatch / depleted.crossoversInAnchorPatch)
                .isCloseTo(2.0 * sqrt(2.0), 1e-12)
        )
    }

    /**
     * The balance is taken on a **uniform** field plus a point load, which the consistent load
     * vector and [OrigamiGrillage.integrateOverFootprint] integrate *identically*. Under
     * `C-0022`'s collar profile the two quadratures differ by ~0.07 % on the 1 nm rim term —
     * a diagnostic limitation of the check, not of the solve, and it is why the solved field is
     * asserted separately and at the tolerance its own quadrature supports.
     */
    @Test
    fun `gate 3 conservation - the applied load is balanced at the ceiling in every model`() {
        val exact = uniformPressure(interiorPressure)
        val point = listOf(PointLoad(3.0, 4.0, 20.0))
        listOf(0, 28, 42, 56).forEach { consumed ->
            val solution = lattice(spent(consumed), supportsOf(3)).solve(exact, point)
            val carried = solution.foundationForce + solution.supportForces.sum()
            assert(carried.isCloseTo(solution.appliedForce, 1e-9))
        }
        val beams = lattice(inventory.toSet(), supportsOf(3)).solve(exact, point)
        assert(
            (beams.foundationForce + beams.supportForces.sum())
                .isCloseTo(beams.appliedForce, 1e-9)
        )
        // and under the solved collar profile, to what the collar quadrature supports
        listOf(0, 42).forEach { consumed ->
            val solution = lattice(spent(consumed), supportsOf(3)).solve(solvedField)
            val carried = solution.foundationForce + solution.supportForces.sum()
            assert(carried.isCloseTo(solution.appliedForce, 1e-3))
        }
    }

    @Test
    fun `gate 3 symmetry - the model selection names the nearer model and is order-independent`() {
        val plateNearer = modelSelection(lattice = 1.0, plate = 1.05, beamArray = 1.5)
        assert(plateNearer.nearerModel == "PLATE")
        assert(plateNearer.plateDeparture.isCloseTo(0.05, 1e-12))
        assert(plateNearer.departureRatio.isCloseTo(0.05 / 0.5, 1e-12))
        val beamsNearer = modelSelection(lattice = 1.0, plate = 1.5, beamArray = 1.05)
        assert(beamsNearer.nearerModel == "BEAM_ARRAY")
        assert(beamsNearer.beamArrayDeparture.isCloseTo(0.05, 1e-12))
        assert(beamsNearer.departureRatio.isCloseTo(0.5 / 0.05, 1e-12))
        // a negative departure is impossible: both are magnitudes
        assert(plateNearer.beamArrayDeparture > 0.0 && beamsNearer.plateDeparture > 0.0)
    }

    @Test
    fun `gate 3 conservation - the hinge energy fraction falls to exactly zero when the last crossover goes`() {
        val field = solvedField
        val intact = lattice().solve(field)
        val ceiling = lattice(spent(42)).solve(field)
        val beams = lattice(inventory.toSet()).solve(field)
        // the beam array stores nothing in hinges, because it has none
        assert(lattice(inventory.toSet()).hingeEnergy(beams.coefficients) == 0.0)
        assert(lattice().hingeEnergy(intact.coefficients) > 0.0)
        assert(lattice(spent(42)).hingeEnergy(ceiling.coefficients) > 0.0)
        // and the across-helix path carries strictly less of the energy as it is depleted
        val intactShare = lattice().hingeEnergy(intact.coefficients) /
                lattice().foundationEnergy(intact.coefficients)
        val ceilingShare = lattice(spent(42)).hingeEnergy(ceiling.coefficients) /
                lattice(spent(42)).foundationEnergy(ceiling.coefficients)
        assert(ceilingShare < intactShare)
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the ceiling lattice is mesh converged on a nested refinement`() {
        val values = listOf(1, 2, 4).map {
            lattice(spent(42), supportsOf(3), subdivisions = it).solve(solvedField).peakDishing()
        }
        val finest = values.last()
        assert(abs(values[1] - finest) / finest < 1e-3)
        assert(abs(values[0] - finest) / finest < 1e-2)
    }

    @Test
    fun `gate 4 convergence - the beam array is penalty independent because it has no links`() {
        val loose = lattice(inventory.toSet(), supportsOf(3), linkStiffness = 1e3)
            .solve(solvedField).peakDishing()
        val tight = lattice(inventory.toSet(), supportsOf(3), linkStiffness = 1e6)
            .solve(solvedField).peakDishing()
        assert(loose.isCloseTo(tight, 1e-14))
    }

    @Test
    fun `gate 4 convergence - the patch inversion lands where the study reports it`() {
        val retained = retainedForPatchCount(
            1.0, inventorySize, sheet.crossoverSpacing, sheet.interhelicalDistance,
            alongHelixRigidity, sheet.crossoverHingeStiffness, Gen1Tile.FOUNDATION_SECANT
        )
        assert(retained.isCloseTo(18.7413, 1e-4))
        assert((inventorySize - retained).isCloseTo(37.2587, 1e-4))
        // and it is INSIDE the region C-0054 declares buildable
        assert(inventorySize - retained < maximumConsumedForConnectivity(56, 15))
    }

    @Test
    fun `gate 3 symmetry - a plate cannot represent the lattice's inhomogeneity at the ceiling`() {
        val staggered = staggeredRetention(inventory, duplexes, 8)
        val model = lattice(inventory.toSet() - staggered)
        val plate = PlateOnFoundation(
            smearedPlate(staggered.size), Gen1Tile.FOUNDATION_SECANT, emptyList(),
            basisDegree = 12
        )
        val stations = listOf(-15.0, -5.0, 5.0, 15.0).map { it to 0.0 }
        fun spreadOf(compliance: (Double, Double) -> Double) = registrationSpread(
            stations.map { (x, y) -> compliance(x, y) }
        ).ratio
        val latticeSpread = spreadOf { x, y ->
            model.solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
        }
        val plateSpread = spreadOf { x, y ->
            plate.solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
        }
        assert(latticeSpread > plateSpread)
        // the intact sheet is nearly homogeneous, which is what makes the ceiling's spread a
        // statement about depletion rather than about the tile's free edges
        val intactSpread = spreadOf { x, y ->
            lattice().solve(pointLoads = listOf(PointLoad(x, y, 1.0))).deflection(x, y)
        }
        assert(latticeSpread > intactSpread)
    }

    // ---------------------------------------------------------------- gate 5 — upstream

    @Test
    fun `gate 5 upstream - C-0009's rigidities and anisotropy are reproduced`() {
        val plate = sheet.plate(Gen1Tile.EDGE_X, lengthY)
        assert(plate.rigidityX.isCloseTo(85.50, 1e-3))
        assert(plate.rigidityY.isCloseTo(3.345, 1e-3))
        assert((plate.rigidityX / plate.rigidityY).isCloseTo(25.56, 1e-3))
    }

    @Test
    fun `gate 5 upstream - C-0054's ceiling and C-0047's dishing under the solved load are reproduced`() {
        assert(maximumConsumedForConnectivity(56, 15) == 42)
        val freeStroke = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val three = lattice(supports = supportsOf(3)).solve(solvedField).peakDishing() / freeStroke
        assert(three.isCloseTo(0.218, 5e-3))
        val one = lattice(supports = supportsOf(1)).solve(solvedField).peakDishing() / freeStroke
        assert(one.isCloseTo(0.695, 5e-3))
        val none = lattice().solve(solvedField).peakDishing() / freeStroke
        assert(none.isCloseTo(0.308, 5e-3))
        // C-0054's own ceiling number, on the same pipeline
        val ceiling = lattice(spent(42), supportsOf(3)).solve(solvedField).peakDishing() / freeStroke
        assert(ceiling.isCloseTo(0.242, 5e-3))
    }
}

private fun pow4(value: Double) = value * value * value * value
