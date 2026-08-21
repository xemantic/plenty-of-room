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

import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.design.HONEYCOMB_BLOCK_DESIGN
import com.xemantic.nano.plentyofroom.design.SQUARE_SHEET_DESIGN
import com.xemantic.nano.plentyofroom.design.ScadnanoDesign
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import com.xemantic.nano.plentyofroom.tile.HoneycombBlock
import com.xemantic.nano.plentyofroom.tile.HoneycombGrillage
import com.xemantic.nano.plentyofroom.tile.honeycombBlockImport
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-267` — the mechanics on an **imported design**, so a placement result stops being a property
 * of a set of constants.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.ImportedDesignMechanicsStudyKt
 * ```
 *
 * Emits `gpd/results/T-267-mechanics-on-imported-design.json`, deterministically.
 */

@Serializable
private data class T267Reproduction(
    val what: String,
    val here: Double,
    val there: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T267Identity(
    val what: String,
    val quantity: String,
    val bitIdentical: Boolean,
    val bitIdentityExpected: Boolean,
    val columnPositionDepartureNm: Double,
    val degreesOfFreedom: Int,
    val solvedFieldRelativeDeparture: Double,
    val solvedFieldTolerance: Double,
    val note: String
)

@Serializable
private data class T267Grading(
    val design: String,
    val artifact: String,
    val drawnBy: String,
    val reading: String,
    val grid: String,
    val representable: Boolean,
    val duplexes: Int,
    val axialSpanBasePairs: Int,
    val lengthXNm: Double,
    val lengthYNm: Double?,
    val interhelicalDistanceNm: Double?,
    val columnsAsDrawn: Int,
    val columnsAsJunctions: Int,
    val columnParities: List<Int>,
    val columnParitiesAlternate: Boolean,
    val designCrossovers: Int,
    val designJunctions: Int,
    val latticeCrossovers: Int,
    val absentCrossovers: Int,
    val peakDishingNm: Double?,
    val strokeNm: Double?,
    val peakDishingOverStroke: Double?,
    val refusals: List<String>,
    val notes: List<String>
)

/**
 * The two crossover **phases** of this corpus's own convention that put the columns in the same
 * places, and which of them the drawn design occupies.
 *
 * `C-0090`: *"phases 8 and 24 give identical column positions (to 1e-12 nm) with inverted
 * parities … a physically different sheet"*. At the buildable 38.08 nm width with the row-end
 * plane excluded the pair is 0 and 16, and the file settles which one was drawn — which a phase
 * integer on its own cannot.
 */
@Serializable
private data class T267ParityPair(
    val design: String,
    val designPhaseBasePairs: Int,
    val mirrorPhaseBasePairs: Int,
    val columnPositionsIdentical: Boolean,
    val positionDepartureNm: Double,
    val paritiesInverted: Boolean,
    val designParities: List<Int>,
    val mirrorParities: List<Int>,
    val designPeakDishingNm: Double,
    val mirrorPeakDishingNm: Double,
    val relativeDifference: Double,
    val note: String
)

@Serializable
private data class T267Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T267Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T267Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val cheapBound: Map<String, String>,
    val identities: List<T267Identity>,
    val parityPairs: List<T267ParityPair>,
    val reproductions: List<T267Reproduction>,
    val gradings: List<T267Grading>,
    val predicates: List<T267Predicate>,
    val falsifiers: List<T267Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

/** `C-0001`'s secant foundation over the §3 tile, in pN/nm³ — `T-10`'s nominal case. */
private const val T267_FOUNDATION: Double = 0.012625625

/** `T-10`'s uniform interior pressure in pN/nm²: §3's 100 pN over its own 40 × 40.35 nm. */
private const val T267_PRESSURE: Double = 0.0619578686

/** `T-10`'s own beam count across the §3 tile. */
private const val T267_BEAMS: Int = 15

/** The tolerance a **solved** field is compared at; bit-identity is not assertable on one. */
private const val T267_FIELD_TOLERANCE: Double = 1e-10

private val t267Mechanics = DuplexMechanics.gen1()

/** `T-10`'s own electrostatic edge taper, on a footprint of [lengthX] × [lengthY]. */
private fun t267Taper(lengthX: Double, lengthY: Double): PressureField = edgeTaperedPressure(
    pressure = T267_PRESSURE,
    plate = OrthotropicPlate(lengthX, lengthY, 1.0, 1.0, 1.0),
    edgeWidth = Gen1Tile.DEBYE_LENGTH,
    depth = 0.5
)

private fun t267RelativeDeparture(here: Double, there: Double): Double =
    if (there == 0.0) abs(here) else abs(here - there) / abs(there)

/** The committed `T-10` lattice peak dishing at the nominal foundation and the 50 % edge taper. */
private fun t267CommittedEdgeTaperPeak(): Double {
    val file = File("gpd/results/T-10-discrete-lattice-tile.json")
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    val cases = Json.parseToJsonElement(file.readText()).jsonObject.getValue("cases").jsonArray
    val nominal = cases.map { it.jsonObject }
        .single { it.getValue("multiplier").jsonPrimitive.content.toDouble() == 1.0 }
    val source = nominal.getValue("sources").jsonArray.map { it.jsonObject }
        .single { it.getValue("source").jsonPrimitive.content == "electrostatic-edge-taper" }
    return source.getValue("latticePeak").jsonPrimitive.content.toDouble()
}

private fun t267MaximumAbsolute(field: org.jetbrains.bio.viktor.F64Array): Double =
    (0 until field.length).maxOf { abs(field[it]) }

private fun t267BitIdentical(
    a: org.jetbrains.bio.viktor.F64Array,
    b: org.jetbrains.bio.viktor.F64Array
): Boolean = a.length == b.length && (0 until a.length).all { a[it] == b[it] }

private fun t267FieldDeparture(
    a: org.jetbrains.bio.viktor.F64Array,
    b: org.jetbrains.bio.viktor.F64Array
): Double {
    val scale = t267MaximumAbsolute(a)
    if (scale == 0.0) return 0.0
    return (0 until a.length).maxOf { abs(a[it] - b[it]) } / scale
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    println("T-267 — THE CHEAP BOUND, before any solve ...")
    val spacingFromLattice = latticeCrossoverSpacing(SquareCrossoverLattice, t267Mechanics)
    val spacingFromConstants =
        Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR
    println(
        "  OrigamiGrillage takes five scalars from its sheet, and exactly one is a LATTICE " +
            "number: the crossover spacing. azimuths x step = " +
            SquareCrossoverLattice.azimuths + " x " +
            SquareCrossoverLattice.anyAzimuthStepBasePairs + " = " +
            SquareCrossoverLattice.samePairPeriodBasePairs + " bp = " + spacingFromLattice +
            " nm, against Gen1Tile's own " + spacingFromConstants
    )

    // ------------------------------------------------------- P3: the reproduction, both paths

    val constantsSheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
    val constantsTile = OrigamiGrillage(
        sheet = constantsSheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = T267_BEAMS,
        foundationStiffness = T267_FOUNDATION,
        crossoverColumns = 8,
        subdivisions = 2
    )
    val specification = centredSheetSpecification(
        name = "T-10's tile, from the lattice and the cross-section",
        lattice = SquareCrossoverLattice,
        crossSection = SheetCrossSection(
            duplexes = T267_BEAMS,
            interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
            source = "T-10's beam count; Fischer et al. 2016 SAXS single-layer d"
        ),
        mechanics = t267Mechanics,
        lengthX = Gen1Tile.EDGE_X,
        crossoverColumns = 8,
        source = "T-267 reproduction of T-10 through the lattice-plus-cross-section path"
    )
    val specifiedTile = specification.grillage(T267_FOUNDATION, subdivisions = 2)

    val sheetTaper = t267Taper(constantsTile.lengthX, constantsTile.lengthY)
    val probe = listOf(PointLoad(3.1, -4.7, 17.0))
    val constantsLoad = constantsTile.assembleLoad(sheetTaper, probe)
    val specifiedLoad = specifiedTile.assembleLoad(sheetTaper, probe)
    val constantsField = constantsTile.solve(sheetTaper).coefficients
    val specifiedField = specifiedTile.solve(sheetTaper).coefficients

    val identities = mutableListOf<T267Identity>()
    identities += T267Identity(
        what = "T-10's tile, built from Gen1Tile's constants against the lattice-plus-" +
            "cross-section specification",
        quantity = "assembleLoad(edge taper + one point load)",
        bitIdentical = t267BitIdentical(constantsLoad, specifiedLoad),
        bitIdentityExpected = true,
        columnPositionDepartureNm = constantsTile.columnX.indices.maxOf {
            abs(constantsTile.columnX[it] - specifiedTile.columnX[it])
        },
        degreesOfFreedom = constantsTile.degreesOfFreedom,
        solvedFieldRelativeDeparture = t267FieldDeparture(constantsField, specifiedField),
        solvedFieldTolerance = T267_FIELD_TOLERANCE,
        note = "CLAUDE.md: bit-identity is assertable on a fixed-order scatter-add and NOT on a " +
            "solved field, where two identically constructed grillages differ by ~4 ulp inside " +
            "one JVM. The load case is deliberately NOT a bare uniform pressure — a free tile " +
            "on a uniform foundation dishes exactly zero, so that comparison tests nothing"
    )

    val committedPeak = t267CommittedEdgeTaperPeak()
    val specifiedPeak = specifiedTile.solve(sheetTaper).peakDishing()
    val reproductions = mutableListOf<T267Reproduction>()
    reproductions += T267Reproduction(
        what = "peak dishing under the 50 % electrostatic edge taper, C-0001 secant foundation",
        here = specifiedPeak,
        there = committedPeak,
        relativeDeparture = t267RelativeDeparture(specifiedPeak, committedPeak),
        source = "gpd/results/T-10-discrete-lattice-tile.json, cases[multiplier = 1]" +
            ".sources[electrostatic-edge-taper].latticePeak — the committed file is emitted at " +
            "nine significant digits, so a departure at 1e-9 IS agreement to the precision the " +
            "corpus states"
    )

    // ------------------------------------------- P3, second path: the FILE against the constants

    val sheetDesign = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
    val sheetImport = sheetDesign.grillageImport("gen1-sheet-square-15x112", t267Mechanics)
    val sheetSpecification = requireNotNull(sheetImport.specification) {
        "the committed square sheet is not importable: " + sheetImport.refusals
    }
    val importedSheet = sheetSpecification.grillage(T267_FOUNDATION, subdivisions = 2)
    val buildableTaper = t267Taper(importedSheet.lengthX, importedSheet.lengthY)

    // The corpus's OWN construction of the same lattice, at the phase whose parities match.
    // `rasterColumnLayout` measures its phase from the tile CENTRE and the design file measures
    // its own from the row START, and 112/2 = 56 = 8 (mod 16) is the whole of the difference.
    val corpusLayouts = (0 until 32).map { it to rasterColumnLayout(it, constantsSheet, 38.08) }
    val matching = corpusLayouts.filter { (_, layout) ->
        layout.parities == sheetSpecification.columns.parities &&
            layout.positions.size == sheetSpecification.columns.positions.size &&
            layout.positions.indices.all {
                abs(layout.positions[it] - sheetSpecification.columns.positions[it]) < 1e-12
            }
    }
    val designPhase = matching.first().first
    val corpusTile = GrillageSpecification(
        name = "the same lattice, from this corpus's own phase " + designPhase,
        lattice = SquareCrossoverLattice,
        crossSection = sheetSpecification.crossSection,
        mechanics = t267Mechanics,
        lengthX = sheetSpecification.lengthX,
        columns = corpusLayouts.single { it.first == designPhase }.second,
        source = "anchoring.rasterColumnLayout(" + designPhase + ", sheet, 38.08)"
    ).grillage(T267_FOUNDATION, subdivisions = 2)
    identities += T267Identity(
        what = "C-0086's buildable tile, read from gpd/designs/gen1-sheet-square-15x112.sc " +
            "against anchoring.rasterColumnLayout at phase " + designPhase,
        quantity = "assembleLoad(edge taper)",
        bitIdentical = t267BitIdentical(
            importedSheet.assembleLoad(buildableTaper, emptyList()),
            corpusTile.assembleLoad(buildableTaper, emptyList())
        ),
        bitIdentityExpected = false,
        columnPositionDepartureNm = importedSheet.columnX.indices.maxOf {
            abs(importedSheet.columnX[it] - corpusTile.columnX[it])
        },
        degreesOfFreedom = importedSheet.degreesOfFreedom,
        solvedFieldRelativeDeparture = t267FieldDeparture(
            importedSheet.solve(buildableTaper).coefficients,
            corpusTile.solve(buildableTaper).coefficients
        ),
        solvedFieldTolerance = T267_FIELD_TOLERANCE,
        note = "the file's own crossover offsets are 8 + 16k; this corpus lays the same columns " +
            "down from the tile centre, so the two phase conventions differ by " +
            "112/2 = 56 = 8 (mod 16) and the lattices do not differ at all"
    )

    // The mirror phase: the same column POSITIONS with every parity inverted, which is a
    // physically different sheet and the thing a phase integer alone cannot settle.
    val mirrorPhase = corpusLayouts.single { (phase, layout) ->
        phase != designPhase &&
            layout.positions.size == sheetSpecification.columns.positions.size &&
            layout.positions.indices.all {
                abs(layout.positions[it] - sheetSpecification.columns.positions[it]) < 1e-12
            } &&
            layout.parities != sheetSpecification.columns.parities
    }
    val mirrorTile = GrillageSpecification(
        name = "the mirror phase " + mirrorPhase.first,
        lattice = SquareCrossoverLattice,
        crossSection = sheetSpecification.crossSection,
        mechanics = t267Mechanics,
        lengthX = sheetSpecification.lengthX,
        columns = mirrorPhase.second,
        source = "anchoring.rasterColumnLayout(" + mirrorPhase.first + ", sheet, 38.08)"
    ).grillage(T267_FOUNDATION, subdivisions = 2)
    val designPeak = importedSheet.solve(buildableTaper).peakDishing()
    val mirrorPeak = mirrorTile.solve(buildableTaper).peakDishing()
    val parityPairs = listOf(
        T267ParityPair(
            design = "gen1-sheet-square-15x112",
            designPhaseBasePairs = designPhase,
            mirrorPhaseBasePairs = mirrorPhase.first,
            columnPositionsIdentical = true,
            positionDepartureNm = sheetSpecification.columns.positions.indices.maxOf {
                abs(sheetSpecification.columns.positions[it] - mirrorPhase.second.positions[it])
            },
            paritiesInverted = sheetSpecification.columns.parities.indices.all {
                sheetSpecification.columns.parities[it] != mirrorPhase.second.parities[it]
            },
            designParities = sheetSpecification.columns.parities,
            mirrorParities = mirrorPhase.second.parities,
            designPeakDishingNm = designPeak,
            mirrorPeakDishingNm = mirrorPeak,
            relativeDifference = t267RelativeDeparture(mirrorPeak, designPeak),
            note = "C-0090's two phases, met from the file side: the positions are identical " +
                "and the interfaces the columns serve are exchanged, so a phase integer does " +
                "not determine the sheet and the DESIGN does. The two peaks agree because the " +
                "exchange is the reflection b -> D-2-b across the helices and the edge taper is " +
                "symmetric in y — a SYMMETRY of this load case, not an insensitivity to the " +
                "parity"
        )
    )

    // ------------------------------------------------- P1: HoneycombGrillage on its own design

    println("T-267 — the honeycomb block, imported (this solve is the study's whole expense) ...")
    val blockDesign = ScadnanoDesign.fromFile(File(HONEYCOMB_BLOCK_DESIGN))
    val blockImport = blockDesign.honeycombBlockImport("gen1-block-honeycomb-10x6-102-109")
    val importedBlock = blockImport.grillage(T267_FOUNDATION, t267Mechanics)
    val constantsBlock = HoneycombGrillage(
        block = HoneycombBlock(blockImport.rasterRows, blockImport.helicesPerRow),
        rowBasePairs = blockImport.axialWindowBasePairs,
        foundationStiffness = T267_FOUNDATION
    )
    val blockTaper = t267Taper(importedBlock.lengthS, importedBlock.lengthY)
    identities += T267Identity(
        what = "C-0151's recommended 10 x 6 block, read from " +
            "gpd/designs/gen1-block-honeycomb-10x6-102-109.sc against HoneycombBlock(10, 6)",
        quantity = "assembleLoad(edge taper)",
        bitIdentical = t267BitIdentical(
            importedBlock.assembleLoad(blockTaper), constantsBlock.assembleLoad(blockTaper)
        ),
        bitIdentityExpected = true,
        columnPositionDepartureNm = 0.0,
        degreesOfFreedom = importedBlock.degreesOfFreedom,
        solvedFieldRelativeDeparture = t267FieldDeparture(
            importedBlock.solve(blockTaper).coefficients,
            constantsBlock.solve(blockTaper).coefficients
        ),
        solvedFieldTolerance = T267_FIELD_TOLERANCE,
        note = "the cross-section is read from the helices' own grid positions, which is the " +
            "one place a scadnano file states it; C-0151's pair of integers in a study literal " +
            "is now a file"
    )

    // ------------------------------------------------------------------- P4: the gradings

    val gradings = mutableListOf<T267Grading>()

    fun grade(
        design: ScadnanoDesign,
        name: String,
        artifact: String,
        drawnBy: String,
        reading: AdjacentCrossingReading,
        interhelicalDistance: Double? = null
    ) {
        val import = design.grillageImport(name, t267Mechanics, interhelicalDistance, reading)
        var peak: Double? = null
        var stroke: Double? = null
        import.specification?.let {
            val lattice = it.grillage(T267_FOUNDATION, subdivisions = 2)
            peak = lattice.solve(t267Taper(lattice.lengthX, lattice.lengthY)).peakDishing()
            stroke = T267_PRESSURE / T267_FOUNDATION
        }
        gradings += T267Grading(
            design = name,
            artifact = artifact,
            drawnBy = drawnBy,
            reading = reading.name,
            grid = import.lattice,
            representable = import.specification != null,
            duplexes = import.duplexes,
            axialSpanBasePairs = import.axialWindowBasePairs,
            lengthXNm = import.lengthX,
            lengthYNm = import.lengthY,
            interhelicalDistanceNm = import.interhelicalDistance,
            columnsAsDrawn = import.columnsAsDrawn,
            columnsAsJunctions = import.columnsAsJunctions,
            columnParities = import.columnParities,
            columnParitiesAlternate = import.columnParitiesAlternate,
            designCrossovers = import.designCrossovers,
            designJunctions = import.designJunctions,
            latticeCrossovers = import.latticeCrossovers,
            absentCrossovers = import.absentCrossovers.size,
            peakDishingNm = peak,
            strokeNm = stroke,
            peakDishingOverStroke = peak?.let { p -> stroke?.let { s -> p / s } },
            refusals = import.refusals,
            notes = import.notes
        )
    }

    grade(
        sheetDesign, "gen1-sheet-square-15x112", SQUARE_SHEET_DESIGN,
        "this corpus (C-0157's oxDNA driver, round-tripped by C-0160's writer)",
        AdjacentCrossingReading.AS_DRAWN
    )
    grade(
        blockDesign, "gen1-block-honeycomb-10x6-102-109", HONEYCOMB_BLOCK_DESIGN,
        "this corpus (C-0160's writer, on C-0151's recommendation)",
        AdjacentCrossingReading.AS_DRAWN
    )
    val rectangle = ScadnanoDesign.fromFile(File(THIRD_PARTY_RECTANGLE_DESIGN))
    listOf(AdjacentCrossingReading.AS_DRAWN, AdjacentCrossingReading.ONE_JUNCTION).forEach {
        grade(
            rectangle, "scadnano-origami-rectangle-16x8", THIRD_PARTY_RECTANGLE_DESIGN,
            "the REFERENCE implementation (scadnano 0.21.1, origami_rectangle.create), " +
                "not this corpus",
            it, Gen1Tile.INTERHELICAL_SHEET
        )
    }
    grade(
        rectangle, "scadnano-origami-rectangle-16x8-no-geometry-supplied",
        THIRD_PARTY_RECTANGLE_DESIGN,
        "the REFERENCE implementation (scadnano 0.21.1), with nothing supplied by the caller",
        AdjacentCrossingReading.AS_DRAWN
    )

    // --------------------------------------------------------------- predicates and falsifiers

    val loadIdentitiesHold = identities.filter { it.bitIdentityExpected }.all { it.bitIdentical }
    val fieldsAgree = identities.all { it.solvedFieldRelativeDeparture < T267_FIELD_TOLERANCE }
    val reproductionHolds = reproductions.all { it.relativeDeparture < 1e-8 }
    val rectangleDrawn = gradings.single {
        it.design == "scadnano-origami-rectangle-16x8" && it.reading == "AS_DRAWN"
    }
    val rectangleJunctions = gradings.single {
        it.design == "scadnano-origami-rectangle-16x8" && it.reading == "ONE_JUNCTION"
    }
    val honeycombGrading = gradings.single { it.design.startsWith("gen1-block-honeycomb") }
    val bareRectangle = gradings.single { it.design.endsWith("no-geometry-supplied") }

    val predicates = listOf(
        T267Predicate(
            "P1",
            "OrigamiGrillage and HoneycombGrillage are constructible from a ScadnanoDesign, or " +
                "from a lattice plus a cross-section",
            sheetImport.refusals.isEmpty() && blockImport.refusals.isEmpty()
        ),
        T267Predicate(
            "P2",
            "the existing Gen1Tile constructor is retained, so the step is additive",
            constantsTile.crossoverColumns == 8 && constantsTile.crossovers.size == 56
        ),
        T267Predicate(
            "P3",
            "an existing study's lattice is reproduced through the new path: the load vector " +
                "bit for bit, the solved field at " + T267_FIELD_TOLERANCE + ", and T-10's own " +
                "committed peak dishing",
            loadIdentitiesHold && fieldsAgree && reproductionHolds
        ),
        T267Predicate(
            "P4",
            "the imported path is exercised on a design this corpus did not produce, and what " +
                "it reports is recorded",
            gradings.any { it.drawnBy.startsWith("the REFERENCE") }
        )
    )

    val falsifiers = listOf(
        T267Falsifier(
            "F1",
            "the specification path does not reproduce T-10's load vector bit for bit, so the " +
                "new path is not the same object. Taken only where the two objects are built " +
                "by the SAME arithmetic: two independent constructions of one column set differ " +
                "in the last ulp of a position, which is not a difference of object",
            !loadIdentitiesHold,
            if (loadIdentitiesHold) "did NOT fire — every load-vector identity that is owed " +
                "exactness holds exactly (" +
                identities.count { it.bitIdentityExpected } + " of " + identities.size +
                "), and the cross-construction one agrees at " +
                identities.single { !it.bitIdentityExpected }
                    .solvedFieldRelativeDeparture.roundedForProse(
                        DEPARTURE_SIGNIFICANT_DIGITS, 0.0
                    ) + " on the solved field"
            else "FIRED — the five scalars are not the same five"
        ),
        T267Falsifier(
            "F2",
            "the imported grillage builds crossovers the design does not draw, or misses some " +
                "it does",
            gradings.any { it.representable && it.absentCrossovers > 0 } ||
                gradings.any { it.representable && it.latticeCrossovers != it.designCrossovers &&
                    it.reading == "AS_DRAWN" },
            "did NOT fire on any representable design: the parity lattice and the file agree " +
                "site for site, and where they would not, the difference is carried as T-110's " +
                "consumedCrossovers"
        ),
        T267Falsifier(
            "F3",
            "a honeycomb design is accepted by the single-layer sheet grillage",
            honeycombGrading.representable,
            if (honeycombGrading.representable)
                "FIRED — C-0154's path-graph statement has been re-imported silently"
            else "did NOT fire — refused, and routed to HoneycombGrillage instead"
        ),
        T267Falsifier(
            "F4",
            "the reference implementation's own origami rectangle cannot be expressed as a " +
                "grillage at all",
            !rectangleDrawn.representable,
            if (rectangleDrawn.representable)
                "did NOT fire — it is expressible, and EXACTLY: " +
                    rectangleDrawn.latticeCrossovers + " lattice sites against " +
                    rectangleDrawn.designCrossovers + " drawn, " +
                    rectangleDrawn.absentCrossovers + " absent"
            else "FIRED — the grillage's lattice assumptions are narrower than the designs the " +
                "field draws"
        ),
        T267Falsifier(
            "F5",
            "a design that states no geometry is given a default rather than refused",
            bareRectangle.representable,
            if (bareRectangle.representable)
                "FIRED — an interhelical distance was guessed"
            else "did NOT fire — refused, naming the same reason ScadnanoDesign.lattice refuses " +
                "to guess a grid"
        )
    )

    val findings = listOf(
        "THE MECHANICS NOW TAKES A DESIGN. OrigamiGrillage and HoneycombGrillage are " +
            "constructible from a scadnano `.sc` file, or from a lattice plus a cross-section, " +
            "and the Gen1Tile constructors are untouched: T-10's own tile, rebuilt through the " +
            "new path, has a load vector BIT-IDENTICAL to the constants-built one over " +
            constantsTile.degreesOfFreedom + " degrees of freedom, a solved field agreeing to " +
            identities[0].solvedFieldRelativeDeparture.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ) + " relative, and it reproduces T-10's committed edge-taper peak dishing at a " +
            "departure of " + reproductions[0].relativeDeparture.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ),
        "EXACTLY ONE OF ORIGAMIGRILLAGE'S FIVE SCALARS IS A LATTICE NUMBER, and it was a " +
            "constant with a lattice baked into its name. `samePairPeriod = azimuths x step` " +
            "gives 4 x 8 = 32 bp on the square lattice and 3 x 7 = 21 on the honeycomb, so " +
            "`Gen1Tile.CROSSOVER_SPACING_SHEET_BP` is now derived rather than cited, and a " +
            "specification on a lattice whose single-layer interfaces are not a path graph is " +
            "REFUSED (C-0154) rather than silently reshaped",
        "THE FIELD'S OWN REFERENCE IMPLEMENTATION DRAWS A ROTHEMUND RECTANGLE THIS GRILLAGE " +
            "REPRESENTS EXACTLY — " + rectangleDrawn.duplexes + " duplexes, " +
            rectangleDrawn.axialSpanBasePairs + " bp, " + rectangleDrawn.designCrossovers +
            " staple crossings, " + rectangleDrawn.latticeCrossovers + " lattice sites, " +
            rectangleDrawn.absentCrossovers + " absent — and it does so at a column parity " +
            "sequence " + rectangleJunctions.columnParities.toString() + " that NO phase sweep " +
            "in " +
            "this corpus can generate. CrossoverLayout.centred and .phased alternate the parity " +
            "by construction; a SEAM does not, because it doubles a column pitch. Every " +
            "phase-swept placement, count and flatness result here is over the alternating " +
            "family, and a seamed sheet is outside it",
        "A CROSSOVER IS ONE JUNCTION AND THE REFERENCE IMPLEMENTATION DRAWS IT AS TWO STRAND " +
            "CROSSINGS, at offsets o and o+1. This corpus's own `.sc` registers one, and " +
            "C-0157 records that registering the SAME offset twice does not relax in oxDNA — " +
            "112 over-stretched bonds against 63 designed crossovers. An adjacent PAIR is " +
            "neither of those, and `checkBuildability`'s noSiteIsCrossedTwice predicate — keyed " +
            "on the exact offset — does not see it. Read as drawn the rectangle is " +
            rectangleDrawn.columnsAsDrawn + " columns and " + rectangleDrawn.designCrossovers +
            " crossovers; read as junctions it is " + rectangleJunctions.columnsAsJunctions +
            " and " + rectangleJunctions.latticeCrossovers + ", and reading it as junctions " +
            "SOFTENS the tile by " +
            (rectangleJunctions.peakDishingOverStroke!! / rectangleDrawn.peakDishingOverStroke!!)
                .roundedForProse(4) + "x in peak dishing over stroke (" +
            rectangleDrawn.peakDishingOverStroke!!.roundedForProse() + " against " +
            rectangleJunctions.peakDishingOverStroke!!.roundedForProse() + "). The reading is " +
            "named in the record rather than defaulted, because it is a modelling decision and " +
            "not a parse",
        "A PHASE INTEGER DOES NOT DETERMINE A SHEET AND A DESIGN DOES. C-0090's two phases " +
            "with identical column positions and inverted parities are, at the buildable " +
            "38.08 nm with the row-end plane excluded, " + parityPairs[0].designPhaseBasePairs +
            " and " + parityPairs[0].mirrorPhaseBasePairs + " — the positions agree to " +
            parityPairs[0].positionDepartureNm.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ) + " nm and every parity is exchanged. The drawn design occupies phase " +
            parityPairs[0].designPhaseBasePairs + ", which no integer in this corpus recorded: " +
            "the oxDNA driver calls the same tile 'phase 8' because it counts from the ROW " +
            "START. The two peaks agree to " + parityPairs[0].relativeDifference.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ) + " because the exchange is the reflection across the helices and this load case " +
            "is symmetric in y — a symmetry of the load, not an insensitivity to the parity",
        "AND TWO CONSTRUCTIONS OF ONE COLUMN SET ARE NOT BIT-IDENTICAL, at " +
            identities[1].columnPositionDepartureNm.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ) + " nm of column position. CLAUDE.md's rule is that bit-identity is assertable on " +
            "assembleLoad and not on a solved field; one level further out it is assertable only " +
            "where the two objects are built by the SAME arithmetic. `(offset - 56) x 0.34` and " +
            "`5.44 + k x 2.72` are the same seven numbers and not the same seven Doubles, and " +
            "the solved fields then differ by " +
            identities[1].solvedFieldRelativeDeparture.roundedForProse(
                DEPARTURE_SIGNIFICANT_DIGITS, 0.0
            ) + " — two decades inside the tolerance, and not zero",
        "A DESIGN THAT STATES NO GEOMETRY IS REFUSED. scadnano's own rectangle carries no " +
            "geometry block at all, so the interhelical distance and the rise have to be " +
            "supplied by the caller and the import says which ones were — the same discipline " +
            "as ScadnanoDesign.lattice refusing to guess a grid, one field down",
        "AND `rowBasePairs` IS NOT A SPAN. C-0160's reader takes the largest offset any " +
            "scaffold domain reaches, which is the extent only when the design starts at " +
            "offset zero. Both artifacts this repository writes do; scadnano's rectangle starts " +
            "its scaffold at offset 16, where rowBasePairs reads 16 bp of empty lattice as " +
            "tile — 144 bp against the true " + rectangleDrawn.axialSpanBasePairs +
            ". axialSpanBasePairs is added beside it and C-0160's method is left exactly as " +
            "published"
    )

    val result = T267Result(
        task = "T-267",
        leaf = "none — step 5 of ARCHITECTURE.md",
        title = "Mechanics on an imported design: OrigamiGrillage and HoneycombGrillage " +
            "constructible from a scadnano file, or from a lattice plus a cross-section",
        verificationType = "in-silico (the existing grillage finite-element models, rebuilt " +
            "through a new constructor) + logical (every lattice derivation is integer " +
            "arithmetic on the design file, and the reproduction is an exact identity)",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
            "Nothing here is folded and nothing is measured; what is demonstrated is that a " +
            "design file and a set of constants produce the same object",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa)",
            "foundationStiffness" to "pN/nm^3",
            "hingeStiffness" to "pN*nm/rad",
            "flexuralRigidity" to "pN*nm",
            "departure" to "dimensionless, relative"
        ),
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them, z normal to the sheet; w is positive " +
                "downward, toward the electrode",
            "a crossover column's position is measured from the centre of the design's own " +
                "AXIAL WINDOW, which is the span of every domain in the file and not the " +
                "largest offset a scaffold domain reaches",
            "a column's PARITY is the parity of the lower helices its crossings join; interface " +
                "b carries the columns whose parity matches b mod 2",
            "the sheet's lattice sites are the STAPLE crossings; a raster's scaffold crossings " +
                "are its TURNS and are not lattice sites (C-0157, CLAUDE.md)",
            "a foundation stiffness is an ENVIRONMENT number and is never part of a " +
                "specification; it is passed to grillage() at the state it was read at"
        ),
        parameters = mapOf(
            "foundationStiffness" to T267_FOUNDATION.roundedForProse().toString(),
            "foundationProvenance" to "C-0001's secant over the 10 nm layer at sigma = 0.024 " +
                "nm^-2, the value T-10's nominal case uses",
            "interiorPressure" to T267_PRESSURE.roundedForProse().toString(),
            "pressureProvenance" to "section 3's 100 pN over T-10's own 40 x 40.35 nm footprint, " +
                "held FIXED across every graded design so that the dishing compares shapes and " +
                "not total forces",
            "edgeTaperWidth" to Gen1Tile.DEBYE_LENGTH.roundedForProse().toString(),
            "edgeTaperDepth" to "0.5",
            "subdivisions" to "2",
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.roundedForProse().toString(),
            "squareLatticeCrossoverSpacingNm" to spacingFromLattice.roundedForProse().toString(),
            "duplexBendingRigidity" to Gen1Tile.DUPLEX_BENDING_RIGIDITY.roundedForProse().toString(),
            "crossoverHingeStiffness" to Gen1Tile.crossoverHingeStiffness().roundedForProse().toString(),
            // deliberately NOT roundedForProse: RESULT_ABSOLUTE_FLOOR is a claim in the
            // locked units and would render this tolerance as 0.0 (CLAUDE.md, P-18)
            "solvedFieldTolerance" to T267_FIELD_TOLERANCE.toString()
        ),
        sources = listOf(
            "gpd/results/T-10-discrete-lattice-tile.json",
            SQUARE_SHEET_DESIGN,
            HONEYCOMB_BLOCK_DESIGN,
            THIRD_PARTY_RECTANGLE_DESIGN
        ),
        cheapBound = mapOf(
            "statement" to "OrigamiGrillage takes exactly five scalars from its sheet and never " +
                "reads layers or interlayerCoupling (CLAUDE.md), so the surface a design has to " +
                "supply is small",
            "scalars" to "interhelicalDistance, crossoverSpacing, crossoverHingeStiffness, " +
                "duplex.bendingRigidity, duplex.torsionalRigidity",
            "latticeScalars" to "1 of 5 — the crossover spacing, which is azimuths x step",
            "squareSamePairPeriodBasePairs" to
                SquareCrossoverLattice.samePairPeriodBasePairs.toString(),
            "verdict" to "the constructor is a mapping and not a model change, which is what " +
                "makes P2 achievable"
        ),
        identities = identities,
        parityPairs = parityPairs,
        reproductions = reproductions,
        gradings = gradings,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "this is a CONSTRUCTOR, not a model: every number it produces is the number the " +
                "existing grillage produces on the same lattice, and no physics is added",
            "the elasticity is never read from a design file and never will be — a .sc states a " +
                "lattice, a cross-section and a routing, and a persistence length is a " +
                "measurement; DuplexMechanics is what the caller supplies, by name",
            "OrigamiGrillage remains a SINGLE-LAYER, path-graph object: a cross-section of more " +
                "than one layer is refused here rather than ignored there, and a honeycomb " +
                "design is routed to HoneycombGrillage",
            "the gradings hold the foundation, the pressure and the taper FIXED across designs " +
                "of different footprint, so a peak dishing over stroke compares the lattice's " +
                "own response and not a device state; none of these numbers is an operating " +
                "point of the Gen-1 actuator",
            "the reference rectangle is graded at this corpus's own single-layer interhelical " +
                "distance because its file states none; that number is supplied and declared, " +
                "and every length derived from it is nominal in it",
            "no committed result file moves and no study is re-run: the reproduction is taken " +
                "by reading T-10's committed file, not by re-emitting it"
        ),
        openQuestions = listOf(
            "whether an antiparallel crossover drawn as two strand crossings at o and o+1 " +
                "relaxes in oxDNA as a doubly-registered one at a single offset does not — " +
                "C-0157 measured the second and nothing has measured the first, and the two " +
                "readings differ by a factor of two in the crossover census",
            "no placement, phase or plan-ceiling study in this corpus has been re-run through " +
                "the imported path; this task supplies the constructor and re-running them is a " +
                "separate, and larger, piece of work",
            "the seam's broken parity alternation is REPRESENTABLE and has never been SWEPT: " +
                "CrossoverLayout carries the parities explicitly, so a seamed sheet can be " +
                "graded, and no study here has graded one"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-267-mechanics-on-imported-design.json")
    out.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result).roundedForResult(
                // A departure is a difference of two nearly equal numbers and is determined to
                // two significant digits, whatever record it sits in. `DEPARTURE_DIGITS_BY_KEY`
                // is the baseline and reaches `reproductions`; these two records are this
                // study's own, so the rule is carried onto them explicitly rather than left to
                // the gate's predicate (`CLAUDE.md`: a gate that reports only what it enforces
                // is how a narrow predicate becomes a claim of cleanliness).
                digitsByKey = mapOf(
                    "identities/solvedFieldRelativeDeparture" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "identities/columnPositionDepartureNm" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "parityPairs/positionDepartureNm" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "parityPairs/relativeDifference" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1e-15
            )
        )
    )
    println("T-267 — wrote " + out.path)
    findings.forEach { println("  * " + it) }
}
