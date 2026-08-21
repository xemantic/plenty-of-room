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
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.design.HONEYCOMB_BLOCK_DESIGN
import com.xemantic.nano.plentyofroom.design.SQUARE_SHEET_DESIGN
import com.xemantic.nano.plentyofroom.design.ScadnanoDesign
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-267` — the mechanics on a design rather than on a set of constants, gate by gate.
 *
 * The two gates that carry the claim are `P3`'s reproduction and `P4`'s import. `CLAUDE.md` fixes
 * how the first must be taken: **bit-identity is assertable on `assembleLoad`**, a fixed-order
 * scatter-add, and **not on a solved field**, where two identically constructed grillages differ
 * by ~4 ulp inside one JVM. So the load vector is compared with `==` and the solved field at
 * `1e-10`, on a load case that is *not* a bare uniform pressure — under one of those the free
 * tile's dishing is its own conditioning noise and the comparison tests nothing.
 */

/** `C-0001`'s secant foundation stiffness over the §3 tile, in pN/nm³ — `T-10`'s nominal case. */
private const val T267_FOUNDATION = 0.012625625

/** `T-10`'s uniform interior pressure: §3's 100 pN over its own 40 × 40.35 nm footprint. */
private const val T267_PRESSURE = 0.0619578686

/** `gpd/results/T-10-discrete-lattice-tile.json`, `cases[2].sources[1].latticePeak`. */
private const val T267_COMMITTED_EDGE_TAPER_PEAK = 1.28014255

private val t267Mechanics = DuplexMechanics.gen1()

private val t267CrossSection = SheetCrossSection(
    duplexes = 15,
    interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
    source = "T-10's own beam count and the SAXS single-layer interhelical distance"
)

/** `T-10`'s own lattice, built the way `T-10` builds it — from `Gen1Tile`'s constants. */
private fun t267ConstantsGrillage(): OrigamiGrillage = OrigamiGrillage(
    sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP),
    lengthX = Gen1Tile.EDGE_X,
    beamCount = 15,
    foundationStiffness = T267_FOUNDATION,
    crossoverColumns = 8,
    subdivisions = 2
)

/** The same lattice, built from a **lattice plus a cross-section**. */
private fun t267SpecificationGrillage(): OrigamiGrillage = centredSheetSpecification(
    name = "T-10's tile, from the lattice and the cross-section",
    lattice = SquareCrossoverLattice,
    crossSection = t267CrossSection,
    mechanics = t267Mechanics,
    lengthX = Gen1Tile.EDGE_X,
    crossoverColumns = 8,
    source = "T-267 reproduction of T-10"
).grillage(foundationStiffness = T267_FOUNDATION, subdivisions = 2)

/** A well-conditioned, **non-uniform** load case — `T-10`'s own electrostatic edge taper. */
private fun t267Taper(
    lengthY: Double,
    lengthX: Double = Gen1Tile.EDGE_X
): PressureField = edgeTaperedPressure(
    pressure = T267_PRESSURE,
    plate = OrthotropicPlate(
        lengthX = lengthX,
        lengthY = lengthY,
        rigidityX = 1.0,
        rigidityY = 1.0,
        twistingRigidity = 1.0
    ),
    edgeWidth = Gen1Tile.DEBYE_LENGTH,
    depth = 0.5
)

class DesignedGrillageTest {

    // ------------------------------------------------------------------ gate 1: dimensions

    @Test
    fun `gate 1 - the crossover spacing is the LATTICE's period, not a Gen1Tile constant`() {
        val square = latticeCrossoverSpacing(SquareCrossoverLattice, t267Mechanics)
        assert(square.isCloseTo(Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR))
        val honeycomb = latticeCrossoverSpacing(HoneycombCrossoverLattice, t267Mechanics)
        assert(
            honeycomb.isCloseTo(
                Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * Gen1Tile.RISE_PER_BASE_PAIR
            )
        )
    }

    @Test
    fun `gate 1 - the specification's sheet is origamiSheet's, to the last bit`() {
        val specified = centredSheetSpecification(
            name = "n", lattice = SquareCrossoverLattice, crossSection = t267CrossSection,
            mechanics = t267Mechanics, lengthX = Gen1Tile.EDGE_X, crossoverColumns = 8,
            source = "s"
        ).sheet
        val built = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        assert(specified == built)
    }

    // ------------------------------------------------------ P2: the existing constructor stays

    @Test
    fun `P2 - the Gen1Tile constructor is untouched and still builds T-10's lattice`() {
        val lattice = t267ConstantsGrillage()
        assert(lattice.beamCount == 15)
        assert(lattice.crossoverColumns == 8)
        assert(lattice.crossovers.size == 56)
        assert(lattice.degreesOfFreedom == 855)
    }

    // ------------------------------------------------------------------ P3: the reproduction

    @Test
    fun `P3 - the load vector of the specified lattice is BIT-IDENTICAL to T-10's`() {
        val constants = t267ConstantsGrillage()
        val specified = t267SpecificationGrillage()
        val field = t267Taper(constants.lengthY)
        val point = listOf(PointLoad(3.1, -4.7, 17.0))
        val a = constants.assembleLoad(field, point)
        val b = specified.assembleLoad(field, point)
        assert(a.length == b.length)
        assert((0 until a.length).all { a[it] == b[it] })
    }

    @Test
    fun `P3 - the solved field of the specified lattice agrees with T-10's at 1e-10`() {
        val constants = t267ConstantsGrillage()
        val specified = t267SpecificationGrillage()
        val field = t267Taper(constants.lengthY)
        val a = constants.solve(field)
        val b = specified.solve(field)
        val scale = (0 until a.coefficients.length).maxOf { abs(a.coefficients[it]) }
        val departure = (0 until a.coefficients.length)
            .maxOf { abs(a.coefficients[it] - b.coefficients[it]) } / scale
        assert(departure < 1e-10)
    }

    @Test
    fun `P3 - the specified lattice reproduces T-10's committed edge-taper peak dishing`() {
        val specified = t267SpecificationGrillage()
        val peak = specified.solve(t267Taper(specified.lengthY)).peakDishing()
        assert(abs(peak - T267_COMMITTED_EDGE_TAPER_PEAK) < 5e-9)
    }

    // -------------------------------------------------- P1/P4: the design is the constructor

    @Test
    fun `P1 - the committed square sheet imports, and its lattice is the design's own`() {
        val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val import = design.grillageImport("gen1-sheet-square-15x112")
        assert(import.refusals.isEmpty())
        assert(import.duplexes == 15)
        assert(import.axialWindowBasePairs == 112)
        assert(import.lengthX.isCloseTo(38.08))
        assert(import.columnsAsDrawn == 7)
        assert(import.designCrossovers == 49)
        assert(import.latticeCrossovers == 49)
        assert(import.absentCrossovers.isEmpty())
    }

    @Test
    fun `P1 - the imported grillage builds EXACTLY the crossovers the design draws`() {
        val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val import = design.grillageImport("gen1-sheet-square-15x112")
        val lattice = import.specification!!.grillage(foundationStiffness = T267_FOUNDATION)
        assert(lattice.crossovers.size == design.crossoverCount())
        // and every built crossover is one the file carries, keyed by (lower helix, offset)
        val drawn = design.crossovers().map { it.lowerHelix to it.offset.toDouble() }.toSet()
        val built = lattice.crossovers.map {
            it.lowerBeam to import.columnBasePairs[it.column]
        }.toSet()
        assert(built == drawn)
    }

    @Test
    fun `P1 - the interhelical distance is READ FROM THE FILE, not inherited`() {
        val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        assert(design.interhelicalDistanceNm()!!.isCloseTo(Gen1Tile.INTERHELICAL_SHEET))
        assert(design.risePerBasePairOrNull()!!.isCloseTo(Gen1Tile.RISE_PER_BASE_PAIR))
    }

    @Test
    fun `P4 - a design that states no geometry is REFUSED rather than given a default`() {
        val design = ScadnanoDesign.fromFile(File(THIRD_PARTY_RECTANGLE_DESIGN))
        assert(design.interhelicalDistanceNm() == null)
        val import = design.grillageImport("scadnano-origami-rectangle")
        assert(import.specification == null)
        assert(import.refusals.any { "interhelical" in it })
    }

    @Test
    fun `P4 - the reference implementation's own rectangle is exactly representable`() {
        val design = ScadnanoDesign.fromFile(File(THIRD_PARTY_RECTANGLE_DESIGN))
        val import = design.grillageImport(
            "scadnano-origami-rectangle",
            interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET
        )
        assert(import.refusals.isEmpty())
        assert(import.duplexes == 16)
        assert(import.axialWindowBasePairs == 128)
        assert(import.columnsAsDrawn == 12)
        assert(import.columnsAsJunctions == 6)
        assert(import.designCrossovers == 90)
        assert(import.latticeCrossovers == 90)
        assert(import.absentCrossovers.isEmpty())
    }

    @Test
    fun `P4 - the rectangle's SEAM breaks the parity alternation every phase sweep assumes`() {
        val design = ScadnanoDesign.fromFile(File(THIRD_PARTY_RECTANGLE_DESIGN))
        val import = design.grillageImport(
            "scadnano-origami-rectangle",
            interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
            reading = AdjacentCrossingReading.ONE_JUNCTION
        )
        assert(import.columnParities == listOf(0, 1, 0, 1, 1, 0))
        assert(!import.columnParitiesAlternate)
        // and no centred or phased layout can produce it: both alternate by construction
        assert(CrossoverLayout.centred(6, 5.44).parities == listOf(0, 1, 0, 1, 0, 1))
    }

    @Test
    fun `P4 - reading the adjacent crossings as ONE junction halves the crossover census`() {
        val design = ScadnanoDesign.fromFile(File(THIRD_PARTY_RECTANGLE_DESIGN))
        val junctions = design.grillageImport(
            "r", interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
            reading = AdjacentCrossingReading.ONE_JUNCTION
        )
        assert(junctions.latticeCrossovers == 45)
        assert(junctions.designJunctions == 45)
        assert(junctions.absentCrossovers.isEmpty())
    }

    @Test
    fun `P1 - the imported column set is this corpus's own, at the phase whose PARITIES match`() {
        val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val columns = design.grillageImport("gen1-sheet-square-15x112").specification!!.columns
        val sheet = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        val corpus = rasterColumnLayout(16, sheet, 38.08)
        assert(corpus.parities == columns.parities)
        assert(corpus.positions.size == columns.positions.size)
        assert(corpus.positions.indices.all {
            abs(corpus.positions[it] - columns.positions[it]) < 1e-12
        })
        // and the MIRROR phase is the same positions with every parity exchanged, which is
        // C-0090's pair met from the file side: a phase integer does not determine the sheet
        val mirror = rasterColumnLayout(0, sheet, 38.08)
        assert(mirror.positions.indices.all {
            abs(mirror.positions[it] - columns.positions[it]) < 1e-12
        })
        assert(mirror.parities.indices.all { mirror.parities[it] != columns.parities[it] })
    }

    @Test
    fun `P3 - two CONSTRUCTIONS of one column set are not bit-identical, and that is the rule`() {
        // CLAUDE.md's discipline one level out: bit-identity is assertable where the arithmetic
        // is the same. `(offset - 56) x rise` and `phase + k x pitch` are the same seven numbers
        // and not the same seven Doubles, so the load vectors differ and the fields agree at
        // 1e-10 — which is why the reproduction gate is taken on T-10's own construction.
        val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val imported = design.grillageImport("gen1-sheet-square-15x112").specification!!
            .grillage(T267_FOUNDATION, subdivisions = 2)
        val sheet = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        val corpus = OrigamiGrillage(
            sheet = sheet,
            lengthX = 38.08,
            beamCount = 15,
            foundationStiffness = T267_FOUNDATION,
            columns = rasterColumnLayout(16, sheet, 38.08),
            subdivisions = 2
        )
        assert(imported.crossovers.size == corpus.crossovers.size)
        val departure = imported.columnX.indices.maxOf {
            abs(imported.columnX[it] - corpus.columnX[it])
        }
        assert(departure > 0.0)
        assert(departure < 1e-12)
        val field = t267Taper(imported.lengthY, imported.lengthX)
        val a = imported.solve(field).coefficients
        val b = corpus.solve(field).coefficients
        val scale = (0 until a.length).maxOf { abs(a[it]) }
        assert((0 until a.length).maxOf { abs(a[it] - b[it]) } / scale < 1e-10)
    }

    @Test
    fun `P1 - a design SPARSER than its own parity lattice is exact, as consumedCrossovers`() {
        // The mechanism that makes an import exact rather than approximate has no test on the
        // designs this repository owns, because on all of them the parity lattice's surplus is
        // EMPTY — a mutation that replaced it with `emptySet()` failed no named test. So the
        // surplus is constructed: drop one staple carrying exactly one crossing, which leaves all
        // seven columns standing and removes exactly one site.
        val committed = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val victim = committed.staples().first { staple ->
            staple.domains.zipWithNext().count { (a, b) -> a.helix != b.helix } == 1
        }
        val sparse = ScadnanoDesign(
            grid = committed.grid,
            helixCount = committed.helixCount,
            strands = committed.strands.filter { it !== victim },
            helices = committed.helices,
            geometry = committed.geometry,
            version = committed.version
        )
        val import = sparse.grillageImport("gen1-sheet-square-15x112, one staple short")
        assert(import.refusals.isEmpty())
        assert(import.columnsAsDrawn == 7)
        assert(import.designCrossovers == 48)
        assert(import.latticeCrossovers == 49)
        assert(import.absentCrossovers.size == 1)
        // and the built lattice is the design, not the parity lattice
        val grillage = import.specification!!.grillage(foundationStiffness = T267_FOUNDATION)
        assert(grillage.crossovers.size == 48)
        val drawn = sparse.crossovers().map { it.lowerHelix to it.offset.toDouble() }.toSet()
        val built = grillage.crossovers.map {
            it.lowerBeam to import.columnBasePairs[it.column]
        }.toSet()
        assert(built == drawn)
    }

    // ----------------------------------------------------- the refusals, which are the content

    @Test
    fun `a honeycomb design is REFUSED by the sheet grillage, naming C-0154's reason`() {
        val design = ScadnanoDesign.fromFile(File(HONEYCOMB_BLOCK_DESIGN))
        val import = design.grillageImport("gen1-block-honeycomb-10x6-102-109")
        assert(import.specification == null)
        assert(import.refusals.any { "path" in it })
    }

    @Test
    fun `a specification on a lattice whose interfaces are not a path is refused outright`() {
        assertFailsWith<IllegalArgumentException> {
            centredSheetSpecification(
                name = "n", lattice = HoneycombCrossoverLattice,
                crossSection = t267CrossSection, mechanics = t267Mechanics,
                lengthX = Gen1Tile.EDGE_X, crossoverColumns = 8, source = "s"
            )
        }
    }

    @Test
    fun `a lattice declares whether its single-layer interfaces form a path`() {
        assert(SquareCrossoverLattice.singleLayerInterfacesFormAPath)
        assert(!HoneycombCrossoverLattice.singleLayerInterfacesFormAPath)
    }

    // ---------------------------------------------------- the state a specification is read at

    @Test
    fun `two specifications on different cross-sections REFUSE each other with a reason`() {
        val a = centredSheetSpecification(
            name = "fifteen", lattice = SquareCrossoverLattice, crossSection = t267CrossSection,
            mechanics = t267Mechanics, lengthX = Gen1Tile.EDGE_X, crossoverColumns = 8,
            source = "s"
        )
        val b = centredSheetSpecification(
            name = "sixteen", lattice = SquareCrossoverLattice,
            crossSection = t267CrossSection.copy(duplexes = 16),
            mechanics = t267Mechanics, lengthX = Gen1Tile.EDGE_X, crossoverColumns = 8,
            source = "s"
        )
        assert(a.reasonToRefuse(b) != null)
        assert(a.reasonToRefuse(a) == null)
    }
}
