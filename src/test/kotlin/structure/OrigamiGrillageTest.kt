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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The `T-10` grillage, gate by gate.
 *
 * Every test is named for the verification gate it discharges, as `T-5`/`T-5b` established.
 * The gate-2 tests are the ones that decide whether the model may be used at all: a lattice
 * whose long-wavelength limit does **not** reproduce `C-0006`'s `D_∥`, `D_⊥` and `D_k` is not
 * a discrete version of `C-0006`'s plate, and any discrepancy it then reports would be a
 * difference in parameterisation rather than in functional form.
 */

private val nominalSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

/** `C-0001`'s secant foundation stiffness over the tile, in pN/nm³. */
private const val FOUNDATION = 0.012625625

private const val TARGET_FORCE = 100.0

/**
 * A lattice stiff enough that only the foundation is left compliant, for the gate-2 and
 * gate-3 limiting cases. `10⁸` rather than `10¹²`: the ratio to the real rigidities is
 * already `10⁶`, which is rigid to six digits, and pushing further only costs conditioning
 * in the Cholesky factorisation.
 */
private val rigidSheet = nominalSheet.copy(
    duplex = nominalSheet.duplex.copy(bendingRigidity = 1e8, torsionalRigidity = 1e8),
    crossoverHingeStiffness = 1e8
)

private fun grillage(
    foundationStiffness: Double = FOUNDATION,
    subdivisions: Int = 2,
    supports: List<PointSupport> = emptyList(),
    beamCount: Int = 15,
    columns: Int = 8,
    linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS,
    sheet: OrigamiSheet = nominalSheet,
    lengthX: Double = Gen1Tile.EDGE_X
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = lengthX,
    beamCount = beamCount,
    foundationStiffness = foundationStiffness,
    crossoverColumns = columns,
    subdivisions = subdivisions,
    linkStiffness = linkStiffness,
    supports = supports
)

class OrigamiGrillageTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a rigid translation should store exactly half kf A per unit deflection`() {
        val lattice = grillage()
        val piston = lattice.pistonMode
        // bending, torsion, hinges and links all vanish on a rigid translation, so the whole
        // energy is the foundation's, and it is k_f times the footprint area
        val foundationEnergy = 0.5 * FOUNDATION * lattice.area
        assert(abs(lattice.structuralEnergy(piston)) < 1e-9 * foundationEnergy)
        assert(
            (lattice.structuralEnergy(piston) + lattice.foundationEnergy(piston))
                .isCloseTo(foundationEnergy, 1e-10)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the area Gram form should return one for a unit piston`() {
        val lattice = grillage()
        // (1/A) integral of 1^2 over the footprint is 1, whatever the mesh
        assert(
            lattice.areaInnerProduct(lattice.pistonMode, lattice.pistonMode).isCloseTo(1.0, 1e-12)
        )
        // and (1/A) integral of x^2 is Lx^2/12, which is the mesh reproducing a linear field exactly
        assert(
            lattice.areaInnerProduct(lattice.tiltXMode, lattice.tiltXMode)
                .isCloseTo(lattice.lengthX * lattice.lengthX / 12.0, 1e-12)
        )
        assert(
            lattice.areaInnerProduct(lattice.tiltYMode, lattice.tiltYMode)
                .isCloseTo(lattice.lengthY * lattice.lengthY / 12.0, 1e-12)
        )
        // the three rigid modes are orthogonal in the area inner product on a centred mesh
        assert(abs(lattice.areaInnerProduct(lattice.pistonMode, lattice.tiltXMode)) < 1e-12)
        assert(abs(lattice.areaInnerProduct(lattice.pistonMode, lattice.tiltYMode)) < 1e-12)
        assert(abs(lattice.areaInnerProduct(lattice.tiltXMode, lattice.tiltYMode)) < 1e-12)
    }

    @Test
    fun `gate 1 dimensional consistency - the lattice geometry should reproduce the sheet counts`() {
        val lattice = grillage()
        // 15 duplexes at 2.69 nm tile 40.35 nm of width
        assert(lattice.lengthY.isCloseTo(15 * Gen1Tile.INTERHELICAL_SHEET))
        // crossover columns are spaced half the per-interface spacing, because crossovers
        // alternate between a helix's two neighbours
        assert(
            (lattice.columnX[1] - lattice.columnX[0])
                .isCloseTo(nominalSheet.crossoverSpacing / 2.0, 1e-12)
        )
        // and one interface therefore sees every other column
        assert(lattice.crossovers.count { it.lowerBeam == 0 } == 4)
        assert(lattice.crossovers.size == 14 * 4)
    }

    // ---------------------------------------------------------------- gate 2

    /**
     * **The gate that licenses the whole comparison.** Imposing the exact across-helix
     * bending field `w = ½κy²` on the lattice must cost the continuum plate's `½ D_⊥ κ² A`,
     * with `D_⊥ = k_θ d / p` — otherwise the lattice is not a discrete version of `C-0006`'s
     * plate and any discrepancy it reports is a change of parameters rather than of form.
     *
     * The lattice cost is `½ k_θ (κd)²` per crossover, **exactly**, so the only thing that
     * separates it from the plate at long wavelength is that a finite lattice holds an
     * *integer* number of crossovers where the plate assumes the areal density `1/(dp)`.
     * Both statements are asserted, and the ratio is shown to go to one as the lattice grows.
     */
    @Test
    fun `gate 2 limiting cases - pure across-helix curvature should cost exactly the plate D perp`() {
        val lattice = grillage()
        val curvature = 0.001
        val field = lattice.curvatureFieldAcrossHelices(curvature)
        val hinge = nominalSheet.crossoverHingeStiffness
        val perCrossover = 0.5 * hinge *
                (curvature * nominalSheet.interhelicalDistance).let { it * it }
        val energy = lattice.structuralEnergy(field)
        // the hinges are the whole compliance, and they are the only thing that engages
        assert(energy.isCloseTo(perCrossover * lattice.crossovers.size, 1e-9))
        // and the vertical crossover links carry no energy at all: a smooth curvature is
        // exactly compatible with the discrete link constraint
        assert(lattice.linkEnergy(field) < 1e-9 * energy)
        // against the continuum plate the ratio is exactly the integer crossover count over
        // the continuum's areal density — nothing else differs
        val plate = 0.5 * nominalSheet.acrossHelixRigidity * curvature * curvature * lattice.area
        val continuumCount = lattice.area /
                (nominalSheet.interhelicalDistance * nominalSheet.crossoverSpacing)
        assert((energy / plate).isCloseTo(lattice.crossovers.size / continuumCount, 1e-9))
        assert(abs(energy / plate - 1.0) < 0.02)
    }

    @Test
    fun `gate 2 limiting cases - the lattice D perp should converge on the plate as the lattice grows`() {
        val curvature = 0.001
        val counts = listOf(15 to 8, 24 to 12, 36 to 18)
        val ratios = counts.map { (beams, columns) ->
            // a footprint of exactly `columns` half-spacings holds exactly `columns/2`
            // crossovers per interface, so the only residual is the interface count
            val lengthX = columns * nominalSheet.crossoverSpacing / 2.0
            val lattice = grillage(
                beamCount = beams, columns = columns, lengthX = lengthX, subdivisions = 1
            )
            val energy = lattice.hingeEnergy(lattice.curvatureFieldAcrossHelices(curvature))
            energy / (0.5 * nominalSheet.acrossHelixRigidity *
                    curvature * curvature * lattice.area)
        }
        // the residual is exactly the free-edge deficit: a lattice of n duplexes has n-1
        // interfaces where the continuum assumes n, so it is softer across the helices by 1/n
        counts.forEachIndexed { i, (beams, _) ->
            assert(ratios[i].isCloseTo((beams - 1.0) / beams, 1e-9))
        }
        assert(ratios == ratios.sorted())
    }

    @Test
    fun `gate 2 limiting cases - pure along-helix curvature should cost exactly the plate D parallel`() {
        val lattice = grillage()
        val curvature = 0.001
        val field = lattice.curvatureFieldAlongHelices(curvature)
        val expected = 0.5 * nominalSheet.alongHelixRigidity * curvature * curvature * lattice.area
        assert(lattice.structuralEnergy(field).isCloseTo(expected, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - pure twist should cost exactly the plate twisting rigidity`() {
        val lattice = grillage()
        val twist = 0.001
        val field = lattice.twistField(twist)
        // the Huber energy density is 2 D_k w_xy^2, so a uniform twist costs 2 D_k tau^2 A
        val expected = 2.0 * nominalSheet.twistingRigidity * twist * twist * lattice.area
        assert(lattice.structuralEnergy(field).isCloseTo(expected, 1e-9))
    }

    /**
     * The falsifier `T-5` wired in and this task inherits: a **uniform** load on a **uniform**
     * foundation translates a free structure exactly, whatever its rigidity and whatever its
     * connectivity. A lattice that dishes under a uniform load has a broken assembly.
     */
    @Test
    fun `gate 2 limiting cases - a uniform load should produce no dishing at all in the lattice either`() {
        listOf(0.1, 1.0, 100.0).forEach { hingeScale ->
            val lattice = grillage(
                sheet = nominalSheet.copy(
                    crossoverHingeStiffness = nominalSheet.crossoverHingeStiffness * hingeScale
                )
            )
            val pressure = TARGET_FORCE / lattice.area
            val solution = lattice.solve(uniformPressure(pressure))
            assert(solution.meanDeflection.isCloseTo(pressure / FOUNDATION, 1e-10))
            assert(solution.dishingRms < 1e-9)
            assert(solution.peakDishing() < 1e-9)
            // and therefore no member of the lattice carries anything
            assert(solution.peakCrossoverForce < 1e-9)
            assert(solution.peakDuplexShear < 1e-9)
        }
    }

    @Test
    fun `gate 2 limiting cases - a rigid lattice should translate under a point load`() {
        val lattice = grillage(sheet = rigidSheet, linkStiffness = 1e10)
        val solution = lattice.solve(
            pointLoads = listOf(PointLoad(8.0, -5.0, TARGET_FORCE))
        )
        val translation = TARGET_FORCE / (FOUNDATION * lattice.area)
        assert(solution.meanDeflection.isCloseTo(translation, 1e-5))
        assert(solution.peakDishing() < 1e-4 * translation)
    }

    @Test
    fun `gate 2 limiting cases - a stiffer foundation should reduce the deflection proportionally`() {
        val pressure = TARGET_FORCE / grillage().area
        val soft = grillage(foundationStiffness = FOUNDATION).solve(uniformPressure(pressure))
        val stiff = grillage(foundationStiffness = 4.0 * FOUNDATION).solve(uniformPressure(pressure))
        assert(stiff.meanDeflection.isCloseTo(soft.meanDeflection / 4.0, 1e-9))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the foundation and the anchors should carry exactly the applied force`() {
        val lattice = grillage(
            supports = listOf(
                PointSupport(-10.0, -10.0, 5.0),
                PointSupport(10.0, -10.0, 5.0),
                PointSupport(-10.0, 10.0, 5.0),
                PointSupport(10.0, 10.0, 5.0)
            )
        )
        val pressure = TARGET_FORCE / lattice.area
        val solution = lattice.solve(
            pressure = uniformPressure(pressure),
            pointLoads = listOf(PointLoad(0.0, 0.0, 25.0))
        )
        assert(solution.appliedForce.isCloseTo(TARGET_FORCE + 25.0, 1e-10))
        assert(
            (solution.foundationForce + solution.supportForces.sum())
                .isCloseTo(solution.appliedForce, 1e-8)
        )
    }

    /**
     * The lattice's symmetry group is **not** the plate's, and this test records which one
     * it is rather than assuming the plate's.
     *
     * A Rothemund crossover pattern alternates between a helix's two neighbours, so a mirror
     * in `x` maps the columns of one interface onto those of its neighbour, and a mirror in
     * `y` swaps interface parities too. Neither mirror is a symmetry on its own; their
     * **product**, the point inversion `(x, y) → (−x, −y)`, is one exactly. A continuum plate
     * has the full rectangular group and therefore cannot represent this at all.
     */
    @Test
    fun `gate 3 symmetry - a symmetric load case should produce a centro-symmetric deflected shape`() {
        val lattice = grillage(
            supports = listOf(
                PointSupport(-10.88, -10.76, 5.0),
                PointSupport(10.88, -10.76, 5.0),
                PointSupport(-10.88, 10.76, 5.0),
                PointSupport(10.88, 10.76, 5.0)
            )
        )
        val solution = lattice.solve(uniformPressure(TARGET_FORCE / lattice.area))
        listOf(0.0 to 5.38, 5.44 to 8.07, 16.0 to 2.69).forEach { (x, y) ->
            assert(solution.deflection(x, y).isCloseTo(solution.deflection(-x, -y), 1e-8))
        }
        // and the four anchor forces come in two equal centro-symmetric pairs
        val forces = solution.supportForces
        assert(forces[0].isCloseTo(forces[3], 1e-8))
        assert(forces[1].isCloseTo(forces[2], 1e-8))
    }

    @Test
    fun `the alternating crossover pattern should break the mirror symmetry a plate cannot lose`() {
        val lattice = grillage()
        val solution = lattice.solve(
            pointLoads = listOf(PointLoad(0.0, 0.0, TARGET_FORCE))
        )
        // a plate would be exactly mirror-symmetric under a centred point load; the lattice
        // is not, and the asymmetry is a real feature of the crossover topology
        val left = solution.deflection(-5.44, 8.07)
        val right = solution.deflection(5.44, 8.07)
        assert(abs(left - right) / max(left, right) > 1e-3)
        assert(solution.deflection(-5.44, 8.07).isCloseTo(solution.deflection(5.44, -8.07), 1e-8))
    }

    /**
     * Equipartition, gate 3, in the form §5 of the problem definition names it: `σ² = k_BT/k`.
     * A lattice stiff enough to be rigid has one soft degree of freedom left, the piston
     * against the foundation, of stiffness `k_f A` — and the two rigid tilts, each of
     * stiffness `k_f A / 3`, whose area-averaged contribution is exactly `√2` pistons.
     */
    @Test
    fun `gate 3 equipartition - a rigid lattice should fluctuate exactly as a single piston mode`() {
        val lattice = grillage(sheet = rigidSheet, linkStiffness = 1e10)
        val fluctuation = lattice.thermalFluctuation(ROOM_TEMPERATURE)
        val expected = sqrt(thermalEnergy() / (FOUNDATION * lattice.area))
        assert(fluctuation.pistonRms.isCloseTo(expected, 1e-5))
        assert(fluctuation.tiltRms.isCloseTo(sqrt(2.0) * expected, 1e-4))
        assert(fluctuation.dishingRms < 1e-2 * expected)
    }

    @Test
    fun `gate 3 conservation - the crossovers on one interface should carry the shear crossing it`() {
        val lattice = grillage()
        val solution = lattice.solve(
            pressure = uniformPressure(TARGET_FORCE / lattice.area),
            pointLoads = listOf(PointLoad(0.0, 0.0, 40.0))
        )
        // equilibrium of everything above the interface between beams 10 and 11
        val interfaceIndex = 10
        val transmitted = solution.crossoverForces
            .filter { it.lowerBeam == interfaceIndex }
            .sumOf { it.verticalForce }
        assert(
            transmitted.isCloseTo(solution.shearAcrossInterface(interfaceIndex), 1e-6)
        )
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the point-load deflection should converge in the element subdivision`() {
        // nested refinements only: 1 ⊂ 2 ⊂ 4. A non-nested mesh (3) moves the load point
        // from a node to mid-element and the monotonicity theorem no longer applies
        val deflections = listOf(1, 2, 4).map { subdivisions ->
            grillage(subdivisions = subdivisions)
                .solve(pointLoads = listOf(PointLoad(0.0, 0.0, TARGET_FORCE)))
                .deflection(0.0, 0.0)
        }
        // a displacement-based finite element is too stiff, so refining can only soften it
        assert(deflections == deflections.sorted())
        assert(abs(deflections[2] - deflections[1]) / deflections[2] < 0.01)
    }

    @Test
    fun `gate 4 numerical convergence - the crossover force should converge as the link stiffens`() {
        val forces = listOf(1e2, 1e3, 1e4, 1e5, 1e6).map { link ->
            grillage(linkStiffness = link)
                .solve(pointLoads = listOf(PointLoad(0.0, 0.0, TARGET_FORCE)))
                .peakCrossoverForce
        }
        // the vertical crossover link is a constraint, not a spring: the transmitted force
        // must stop depending on the penalty once the penalty is stiff
        assert(abs(forces[4] - forces[3]) / forces[4] < 0.01)
        assert(abs(forces[3] - forces[2]) / forces[3] < 0.05)
    }

    // ---------------------------------------------------------------- gate 5

    /**
     * Gate 5 in its internal form: **where the continuum plate's own validity criterion is
     * satisfied, the lattice must agree with the plate.** Softening the foundation by 200×
     * lifts `ℓ_∥/p` above 3 and `ℓ_⊥/d` above 5, i.e. into the regime the plate reduction
     * assumes; the two models must then give the same point-load deflection.
     *
     * If they did not, the discrepancy this task reports at the *working* stiffness could
     * not be attributed to discreteness.
     */
    @Test
    fun `gate 5 cross-check - lattice and plate should agree where the continuum criterion holds`() {
        val soft = FOUNDATION / 200.0
        val lattice = grillage(foundationStiffness = soft, subdivisions = 2)
        val plate = OrthotropicPlate(
            lengthX = lattice.lengthX,
            lengthY = lattice.lengthY,
            rigidityX = nominalSheet.alongHelixRigidity,
            rigidityY = nominalSheet.acrossHelixRigidity,
            twistingRigidity = nominalSheet.twistingRigidity
        )
        val load = listOf(PointLoad(0.0, 0.0, TARGET_FORCE))
        val fromLattice = lattice.solve(pointLoads = load).peakDishing()
        val fromPlate = PlateOnFoundation(plate, soft, basisDegree = 12)
            .solve(pointLoads = load).peakDishing()
        assert(abs(fromLattice - fromPlate) / fromPlate < 0.10)
    }

    // ---------------------------------------------------------------- the physics under test

    @Test
    fun `a discrete anchor should concentrate force in the crossovers around it`() {
        val lattice = grillage(
            supports = listOf(PointSupport(0.0, 0.0, FOUNDATION * grillage().area))
        )
        val solution = lattice.solve(uniformPressure(TARGET_FORCE / lattice.area))
        val anchorForce = solution.supportForces.single()
        assert(anchorForce > 1.0)
        // the crossovers nearest the anchor carry more than the ones far from it
        val near = solution.crossoverForces
            .filter { abs(it.x) < 6.0 && abs(it.y) < 6.0 }
            .maxOf { abs(it.verticalForce) }
        val far = solution.crossoverForces
            .filter { abs(it.x) > 14.0 || abs(it.y) > 14.0 }
            .maxOf { abs(it.verticalForce) }
        assert(near > far)
    }

    @Test
    fun `a softer crossover hinge should soften the across-helix response and leave the along-helix one alone`() {
        val soft = nominalSheet.copy(
            crossoverHingeStiffness = nominalSheet.crossoverHingeStiffness / 4.0
        )
        val stiffField = grillage().curvatureFieldAcrossHelices(0.001)
        val softLattice = grillage(sheet = soft)
        assert(
            softLattice.structuralEnergy(stiffField)
                .isCloseTo(grillage().structuralEnergy(stiffField) / 4.0, 1e-9)
        )
        val alongField = grillage().curvatureFieldAlongHelices(0.001)
        assert(
            softLattice.structuralEnergy(alongField)
                .isCloseTo(grillage().structuralEnergy(alongField), 1e-9)
        )
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a non-physical lattice should be rejected on construction`() {
        assertFailsWith<IllegalArgumentException> { grillage(beamCount = 1) }
        assertFailsWith<IllegalArgumentException> { grillage(foundationStiffness = 0.0) }
        assertFailsWith<IllegalArgumentException> { grillage(subdivisions = 0) }
        assertFailsWith<IllegalArgumentException> { grillage(columns = 1) }
        assertFailsWith<IllegalArgumentException> { grillage(linkStiffness = -1.0) }
        // the crossover columns have to fit strictly inside the footprint
        assertFailsWith<IllegalArgumentException> { grillage(columns = 40) }
    }

}
