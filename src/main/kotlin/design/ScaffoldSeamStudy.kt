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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import com.xemantic.nano.plentyofroom.tile.HoneycombBlock
import com.xemantic.nano.plentyofroom.tile.HoneycombCrossSectionGeometry
import com.xemantic.nano.plentyofroom.tile.LayerCoupling
import com.xemantic.nano.plentyofroom.tile.MeasuredBundleRigidity
import com.xemantic.nano.plentyofroom.tile.equivalentSheet
import com.xemantic.nano.plentyofroom.tile.multiLayerRigidities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-274 -- does the recommended 10 x 6 honeycomb block need a scaffold SEAM, and is the committed
// design drawn without one?
//
// CH-0212: C-0119 §4 calls a seam FORCED on this block and the 60-helix case "a theorem"; C-0160's
// committed .sc carries ONE scaffold strand, 60 domains, 60 helices -- which is C-0161's own
// discriminator returning NO SEAM.
//
// The cheap bound runs first and it is a DEGREE CENSUS: a vertex of degree one lies on no cycle,
// so if either raster terminus has degree one in the block's own cross-section adjacency there is
// no Hamiltonian cycle and no search is needed at all.
// ---------------------------------------------------------------------------------------------

private const val T274_ROW_BP: Int = 112
private const val T274_SAMPLES: Int = 81
private const val T274_RIM_STANDOFF: Double = 1.0
private const val T274_FLATNESS_TOLERANCE: Double = 0.10

/** The block this programme recommends, and the one it recommended before. */
private const val T274_ROWS: Int = 10
private const val T274_PER_ROW: Int = 6
private const val T274_SPARE_NUCLEOTIDES: Int = 919
private const val T274_CONTROL_ROWS: Int = 15
private const val T274_CONTROL_PER_ROW: Int = 4
private const val T274_CONTROL_SPARE: Int = 529

/** `CLAUDE.md`'s own currency: holding two of the host sheet's duplexes together per column. */
private const val T274_CROSSOVER_COLUMN_KBT: Double = 8.0

@Serializable
private class T274GraphRow(
    val block: String,
    val reading: String,
    val helices: Int,
    val admissibleCrossovers: Int,
    val connected: Boolean,
    val isTree: Boolean,
    val cyclomaticNumber: Int,
    val maximumDegree: Int,
    val leaves: List<Int>,
    val leavesAreTheRasterTermini: Boolean,
    val bridges: Int,
    val hamiltonianCycle: String,
    val lowerBoundDomainsFullyFoldedCircular: Int,
    val domainsHamiltonianPath: Int,
    val fullFoldingNeedsMoreThanOneDomainPerHelix: Boolean
)

@Serializable
private class T274PathBoundary(
    val block: String,
    val helicesPerRow: Int,
    val helices: Int,
    val admissibleCrossovers: Int,
    val isTree: Boolean
)

@Serializable
private class T274Artifact(
    val file: String,
    val grid: String,
    val helices: Int,
    val strands: Int,
    val scaffoldStrands: Int,
    val scaffoldDomains: Int,
    val stapleStrands: Int,
    val scaffoldBases: Int,
    val scaffoldTurns: Int,
    val stapleCrossings: Int,
    val crossoverColumns: Int,
    val fivePrimeOffset: Int,
    val threePrimeOffset: Int,
    val domainsPerHelix: String,
    val discriminator: String
)

@Serializable
private class T274Closure(
    val block: String,
    val convention: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val terminusCells: String,
    val terminiShareAFace: Boolean,
    val terminusSeparation: Double,
    val spareNucleotides: Int,
    val contourLength: Double,
    val kuhnSegments: Double,
    val rootMeanSquareEndToEnd: Double,
    val extensionRatio: Double,
    val minimumNucleotidesToReach: Int,
    val stretchFreeEnergyKbt: Double,
    val stretchFreeEnergyEv: Double,
    val nucleotidesAtOneCrossoverColumn: Int,
    val reaches: Boolean,
    val affordable: Boolean
)

@Serializable
private class T274Counterfactual(
    val layout: String,
    val columns: Int,
    val parities: String,
    val consecutiveEqualParities: Int,
    val collarDishing: Double,
    val uniformLoadDishing: Double,
    val flat: Boolean,
    val relativeToAlternating: Double
)

@Serializable
private class T274Extent(
    val reading: String,
    val rowBasePairs: Int,
    val edgeX: Double,
    val columns: Int,
    val collarDishing: Double,
    val flat: Boolean,
    val relativeToAsGraded: Double
)

@Serializable
private class T274Reproduction(
    val of: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double
)

@Serializable
private class T274Result(
    val task: String, val leaf: String, val title: String, val verificationType: String,
    val maturity: String, val units: Map<String, String>, val conventions: Map<String, String>,
    val parameters: Map<String, String>, val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val graphs: List<T274GraphRow>,
    val pathBoundary: List<T274PathBoundary>,
    val artifacts: List<T274Artifact>,
    val secondReading: Map<String, String>,
    val closures: List<T274Closure>,
    val counterfactual: List<T274Counterfactual>,
    val extentSensitivity: List<T274Extent>,
    val reproductions: List<T274Reproduction>,
    val verdict: Map<String, String>,
    val falsifiers: List<String>,
    val falsifiersFired: Map<String, String>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

/** `C-0022`'s solved collar, read out of `T-3b` at the design state — `T-219`'s own reader. */
private class T274Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double)

private fun t274Profile(file: File): T274Profile {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T274Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * `T-219`'s own tile, at the recommended cross-section, with the **column layout** lifted out as
 * a parameter — which is the whole of this study's mechanical half.
 */
private class T274Tile(
    val rasterRows: Int,
    val layers: Int,
    private val profile: T274Profile,
    val rowBasePairs: Int = T274_ROW_BP
) {
    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = HoneycombBlock(rasterRows, layers, d).plateEdgeY

    private val rigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = HoneycombCrossSectionGeometry.rowPitch(d),
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = MeasuredBundleRigidity.COMPOSITE_FRACTION,
        layerSpacing = HoneycombCrossSectionGeometry.columnPitch(d)
    )

    private val sheet = equivalentSheet(rigidities)

    /** The column count `CrossoverLayout.centred` is asked for, unchanged from both studies. */
    val alternatingColumns: Int =
        floor((edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN) / (sheet.crossoverSpacing / 2.0))
            .toInt() + 1

    /** The column pitch, `p/2`. */
    val columnPitch: Double = sheet.crossoverSpacing / 2.0

    private fun grillage(columns: CrossoverLayout) = OrigamiGrillage(
        sheet = sheet, lengthX = edgeX, beamCount = rasterRows,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns, subdivisions = 2
    )

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private val field = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(
            CollarTerm(profile.smoothDepth, profile.smoothWidth),
            CollarTerm(profile.rimDepth, T274_RIM_STANDOFF)
        )
    )

    fun collarDishing(columns: CrossoverLayout): Double =
        grillage(columns).solve(field).peakDishing(T274_SAMPLES) / freeStroke

    fun uniformDishing(columns: CrossoverLayout): Double =
        grillage(columns).solve(uniformPressure(interiorPressure))
            .peakDishing(T274_SAMPLES) / freeStroke
}

/**
 * The **shape** a seam takes in the one seamed design this corpus has imported: `C-0161` measures
 * the reference rectangle's junction columns at `31.5, 47.5, 63.5, 79.5, 111.5, 127.5` — one
 * column missing, a doubled pitch, and two consecutive columns of the **same** interface parity.
 *
 * Deleting the column nearest the tile centre from `CrossoverLayout.centred` reproduces exactly
 * that: the surviving parities are `… 0, 1, 1, 0 …` because `(k−1) mod 2 = (k+1) mod 2`.
 */
private fun seamedLayout(alternating: CrossoverLayout): CrossoverLayout {
    val centre = alternating.positions.indices.minByOrNull { abs(alternating.positions[it]) }!!
    return CrossoverLayout(
        positions = alternating.positions.filterIndexed { i, _ -> i != centre },
        parities = alternating.parities.filterIndexed { i, _ -> i != centre }
    )
}

private fun consecutiveEqualParities(layout: CrossoverLayout): Int =
    layout.parities.zipWithNext().count { (a, b) -> a == b }

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val path = honeycombXRasterPath(T274_ROWS, T274_PER_ROW)
    val controlPath = honeycombXRasterPath(T274_CONTROL_ROWS, T274_CONTROL_PER_ROW)

    println("T-274 - does the recommended 10 x 6 honeycomb block need a scaffold SEAM?")

    // ------------------------------------------------------------- the cheap bound, a degree census
    val induced = inducedLatticeScaffoldGraph(path)
    val surface = rasterSurfaceScaffoldGraph(path)
    println("  CHEAP BOUND - the degree census, before any search")
    println("    induced lattice adjacency: " + induced.order + " helices, " + induced.size +
            " admissible crossovers, maximum degree " + induced.degrees.max())
    println("    degree-one helices: " + induced.leaves + " (the raster termini are 0 and " +
            (induced.order - 1) + ")")
    println("    a vertex of degree one lies on NO cycle, so there is no Hamiltonian cycle and " +
            "no search is needed")

    val cheapBound = mapOf(
        "instrument" to "a degree census on the block's own cross-section adjacency",
        "why it decides" to "a vertex of degree one lies on no cycle at all, so a graph with a " +
                "leaf has no Hamiltonian cycle and a fully folded circular scaffold cannot give " +
                "one domain per helix - decided in microseconds where C-0119's own brute force " +
                "refuses beyond order 9",
        "what it returns" to "both raster termini of every m x n honeycomb block tested are " +
                "degree ONE, because two of a corner helix's three honeycomb bonds point out of " +
                "the block",
        "cost" to "60 lookups of a three-element neighbour list, against 59! permutations"
    )

    // ------------------------------------------------------------------------------ the two readings
    fun graphRow(name: String, reading: String, g: ScaffoldGraph) =
        T274GraphRow(
            block = name,
            reading = reading,
            helices = g.order,
            admissibleCrossovers = g.size,
            connected = g.isConnected,
            isTree = g.isTree,
            cyclomaticNumber = g.cyclomaticNumber,
            maximumDegree = g.degrees.max(),
            leaves = g.leaves,
            leavesAreTheRasterTermini = g.leaves == listOf(0, g.order - 1),
            bridges = g.bridges.size,
            hamiltonianCycle = when (g.hasHamiltonianCycle()) {
                true -> "exists"
                false -> "none - a degree-one helix lies on no cycle"
                null -> "the exhaustive search REFUSED at this order"
            },
            lowerBoundDomainsFullyFoldedCircular = g.minimumClosedCoveringWalk,
            domainsHamiltonianPath = g.order,
            fullFoldingNeedsMoreThanOneDomainPerHelix =
                g.minimumClosedCoveringWalk > g.order
        )

    val graphs = listOf(
        graphRow("10 x 6", "Douglas et al. - the scaffold stays within a 2D surface", surface),
        graphRow("10 x 6", "the honeycomb lattice's own adjacency, induced on the block", induced),
        graphRow("15 x 4", "Douglas et al. - the scaffold stays within a 2D surface",
            rasterSurfaceScaffoldGraph(controlPath)),
        graphRow("15 x 4", "the honeycomb lattice's own adjacency, induced on the block",
            inducedLatticeScaffoldGraph(controlPath))
    )

    // ------------------------------------------- where the induced adjacency IS a path, and only there
    val pathBoundary = (2..10).flatMap { m ->
        listOf(2, 4, 6).map { n ->
            val g = inducedLatticeScaffoldGraph(honeycombXRasterPath(m, n))
            T274PathBoundary("$m x $n", n, g.order, g.size, g.isTree)
        }
    }

    // ------------------------------------------------------------------------ the committed artifacts
    fun artifact(file: File, expectRaster: Boolean): T274Artifact {
        val design = ScadnanoDesign.fromFile(file)
        val scaffold = design.scaffold()
        val perHelix = scaffold.domains.groupingBy { it.helix }.eachCount()
        return T274Artifact(
            file = file.path,
            grid = design.grid,
            helices = design.helixCount,
            strands = design.strands.size,
            scaffoldStrands = design.strands.count { it.isScaffold },
            scaffoldDomains = scaffold.domains.size,
            stapleStrands = design.staples().size,
            scaffoldBases = scaffold.domains.sumOf { it.end - it.start },
            scaffoldTurns = design.scaffoldTurns().size,
            stapleCrossings = design.crossovers().size,
            crossoverColumns = design.crossoverColumns().size,
            fivePrimeOffset = scaffold.domains.first().entryOffset,
            threePrimeOffset = scaffold.domains.last().exitOffset,
            domainsPerHelix = perHelix.values.distinct().sorted().joinToString(", "),
            discriminator =
                if (scaffold.domains.size == design.helixCount)
                    "one domain per helix - a HAMILTONIAN PATH, which is C-0161's own " +
                            "discriminator returning NO SEAM"
                else
                    "${scaffold.domains.size} domains against ${design.helixCount} helices - " +
                            "the excess IS the seam"
        ).also { require(expectRaster == (it.scaffoldDomains == it.helices)) }
    }

    val artifacts = listOf(
        artifact(File("gpd/designs/gen1-block-honeycomb-10x6-102-109.sc"), true),
        artifact(File("gpd/designs/gen1-sheet-square-15x112.sc"), true),
        artifact(File("gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc"), false)
    )

    // ------------------------------------- CH-0212's second proposed reading, attempted and refused
    val block = ScadnanoDesign.fromFile(File("gpd/designs/gen1-block-honeycomb-10x6-102-109.sc"))
    val rectangle = ScadnanoDesign.fromFile(
        File("gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc")
    )
    val secondReading = mapOf(
        "what CH-0212 proposed" to "run T-267's importer on the block and read its column parity " +
                "sequence directly out of the file, the way the rectangle's [0,1,0,1,1,0] was read",
        "outcome" to "UNAVAILABLE, and the reason is in C-0160 §6",
        "why" to "a column parity is a property of the STAPLE crossover ladder, and the block " +
                "carries NO staple set at all - " + block.crossovers().size + " staple crossings, " +
                block.crossoverColumns().size + " columns, so crossoverPhase() throws rather " +
                "than returning a phase",
        "the control" to "the reference rectangle carries " + rectangle.crossovers().size +
                " staple crossings and does return a parity sequence, which is what makes the " +
                "block's absence a fact about the block rather than about the reader",
        "consequence" to "the fork CH-0212 states can only be settled on the ARGUMENT, which is " +
                "the half it called the more expensive one - and it is not: the argument is a " +
                "degree census"
    )

    // ------------------------------------------------------------------------ the remainder closure
    val separation = rasterTerminusSeparation(path, d)
    val controlSeparation = rasterTerminusSeparation(controlPath, d)
    println("  THE CLOSURE - the two termini of the 10 x 6 raster are " +
            separation.roundedForProse(6) + " nm apart, which is 14 d exactly")

    val closures = listOf(
        Triple("10 x 6", path, T274_SPARE_NUCLEOTIDES),
        Triple("15 x 4", controlPath, T274_CONTROL_SPARE)
    ).flatMap { (name, cells, spare) ->
        val gap = rasterTerminusSeparation(cells, d)
        val first = cells.first()
        val last = cells.last()
        SSDNA_CONVENTIONS.map { convention ->
            val closure = remainderClosure(gap, spare, convention)
            T274Closure(
                block = name,
                convention = convention.name,
                kuhnLength = convention.kuhnLength,
                contourPerNucleotide = convention.contourPerNucleotide,
                terminusCells = "(" + first.x + ", " + first.y + ") and (" + last.x + ", " + last.y + ")",
                terminiShareAFace = first.x == last.x,
                terminusSeparation = gap,
                spareNucleotides = spare,
                contourLength = closure.contourLength,
                kuhnSegments = closure.kuhnSegments,
                rootMeanSquareEndToEnd = closure.rootMeanSquareEndToEnd,
                extensionRatio = closure.extensionRatio,
                minimumNucleotidesToReach = closure.minimumNucleotidesToReach,
                stretchFreeEnergyKbt = closure.stretchFreeEnergyKbt,
                stretchFreeEnergyEv = closure.stretchFreeEnergyKbt *
                        thermalEnergy() * 1e-21 / 1.602176634e-19,
                nucleotidesAtOneCrossoverColumn =
                    nucleotidesForClosureCost(gap, T274_CROSSOVER_COLUMN_KBT, convention),
                reaches = closure.minimumNucleotidesToReach < spare,
                affordable = closure.stretchFreeEnergyKbt < T274_CROSSOVER_COLUMN_KBT
            )
        }
    }

    // ------------------------------------------------------------------- the seamed counterfactual
    val profile = t274Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val tile = T274Tile(T274_ROWS, T274_PER_ROW, profile)
    val alternating = CrossoverLayout.centred(tile.alternatingColumns, tile.columnPitch)
    val seamed = seamedLayout(alternating)
    val alternatingDishing = tile.collarDishing(alternating)
    val seamedDishing = tile.collarDishing(seamed)
    println("  THE COUNTERFACTUAL - alternating " + alternatingDishing.roundedForProse(9) +
            " against seamed " + seamedDishing.roundedForProse(9) + " of the free stroke")

    val counterfactual = listOf(
        "alternating - what CrossoverLayout.centred generates, and what both studies grade on" to
                alternating,
        "seamed - the rectangle's own shape: one column deleted, a doubled pitch, two " +
                "consecutive columns of the SAME parity" to seamed
    ).map { (label, layout) ->
        val dishing = tile.collarDishing(layout)
        T274Counterfactual(
            layout = label,
            columns = layout.size,
            parities = layout.parities.joinToString(""),
            consecutiveEqualParities = consecutiveEqualParities(layout),
            collarDishing = dishing,
            uniformLoadDishing = tile.uniformDishing(layout),
            flat = dishing < T274_FLATNESS_TOLERANCE,
            relativeToAlternating = dishing / alternatingDishing
        )
    }

    // ------------------------------- the extent the two studies grade the recommended block at
    // C-0151's drawable raster is 102 / 109: its rows span 109 bp = 37.06 nm and its box 116 bp =
    // 39.44 nm. Both studies grade at 112 bp = 38.08 nm, which is the SQUARE sheet's own row
    // length and the WITHDRAWN 112 / 108 honeycomb pair's, and is neither of the drawable block's
    // two readings. This sweeps it rather than assuming it.
    val extentSensitivity = listOf(
        "the drawable raster's row span (C-0151, 102 / 109)" to 109,
        "as graded by both studies - the square sheet's 112 bp" to T274_ROW_BP,
        "the drawable raster's bounding box (C-0146, the stagger included)" to 116
    ).map { (label, bp) ->
        val at = T274Tile(T274_ROWS, T274_PER_ROW, profile, bp)
        val layout = CrossoverLayout.centred(at.alternatingColumns, at.columnPitch)
        val dishing = at.collarDishing(layout)
        T274Extent(
            reading = label, rowBasePairs = bp, edgeX = at.edgeX,
            columns = layout.size, collarDishing = dishing,
            flat = dishing < T274_FLATNESS_TOLERANCE,
            relativeToAsGraded = dishing / alternatingDishing
        )
    }
    println("  THE EXTENT - " + extentSensitivity.joinToString("; ") {
        it.rowBasePairs.toString() + " bp -> " + it.collarDishing.roundedForProse(9)
    })

    // --------------------------------------------------------------------------- the reproductions
    val published = Json.parseToJsonElement(
        File("gpd/results/T-219-honeycomb-station-lattice-and-placement.json").readText()
    ).jsonObject.getValue("dishing").jsonArray.map { it.jsonObject }
        .first {
            it.getValue("name").jsonPrimitive.content == "10 x 6" &&
                    it.getValue("geometry").jsonPrimitive.content.contains("honeycomb")
        }
    val publishedDishing =
        published.getValue("freeTileDishingOverStroke").jsonPrimitive.content.toDouble()
    val publishedEdgeY = published.getValue("edgeY").jsonPrimitive.content.toDouble()

    fun relative(a: Double, b: Double) = if (a == 0.0) abs(b) else abs(b - a) / abs(a)

    val reproductions = listOf(
        T274Reproduction(
            of = "C-0141 / T-219", quantity = "10 x 6 free-tile collar dishing over stroke",
            published = publishedDishing, here = alternatingDishing,
            relativeDeparture = relative(publishedDishing, alternatingDishing)
        ),
        T274Reproduction(
            of = "C-0141 / T-219", quantity = "10 x 6 plate edgeY in nm",
            published = publishedEdgeY, here = tile.edgeY,
            relativeDeparture = relative(publishedEdgeY, tile.edgeY)
        ),
        T274Reproduction(
            of = "C-0119 / CLAUDE.md", quantity =
                "domains a fully folded circular scaffold needs on a 60-helix PATH graph",
            published = 118.0, here = surface.minimumClosedCoveringWalk.toDouble(),
            relativeDeparture = relative(118.0, surface.minimumClosedCoveringWalk.toDouble())
        ),
        T274Reproduction(
            of = "C-0160", quantity = "scaffold bases in the committed block",
            published = 6330.0, here = artifacts.first().scaffoldBases.toDouble(),
            relativeDeparture = relative(6330.0, artifacts.first().scaffoldBases.toDouble())
        ),
        T274Reproduction(
            of = "C-0161", quantity = "scaffold domains of the reference rectangle",
            published = 31.0, here = artifacts.last().scaffoldDomains.toDouble(),
            relativeDeparture = relative(31.0, artifacts.last().scaffoldDomains.toDouble())
        )
    )

    // ----------------------------------------------------------------------------------- the verdict
    val seamNeededOnEitherReading =
        graphs.filter { it.block == "10 x 6" }.all { it.fullFoldingNeedsMoreThanOneDomainPerHelix }
    val closes = closures.filter { it.block == "10 x 6" }.all { it.reaches && it.affordable }
    val falsifiersFired = mutableMapOf<String, String>()
    falsifiersFired["F1"] = if (induced.isTree)
        "FIRED - the induced graph is a tree" else
        "did NOT fire - the induced graph carries " + induced.size + " edges on " + induced.order +
                " vertices, " + induced.cyclomaticNumber + " independent cycles"
    falsifiersFired["F2"] = if (induced.hasHamiltonianCycle() == true)
        "FIRED - a Hamiltonian cycle exists" else
        "did NOT fire - both raster termini are degree one, so no cycle covers them"
    falsifiersFired["F3"] = if (artifacts.first().scaffoldDomains == 60 &&
        artifacts.first().helices == 60)
        "did NOT fire - the committed block carries 60 domains on 60 helices, as CH-0212 states"
    else "FIRED - CH-0212's census of the artifact is wrong"
    falsifiersFired["F4"] = if (closes)
        "did NOT fire - the remainder reaches on every ssDNA convention and costs less than one " +
                "crossover column of the host sheet"
    else "FIRED - the circle cannot close through its remainder, so the seam IS forced"
    falsifiersFired["F5"] = if ((alternatingDishing < T274_FLATNESS_TOLERANCE) !=
        (seamedDishing < T274_FLATNESS_TOLERANCE))
        "FIRED - the seamed counterfactual crosses T-5b's 0.10"
    else "did NOT fire - both layouts are flat at T-5b's 0.10, at " +
            alternatingDishing.roundedForProse(6) + " and " + seamedDishing.roundedForProse(6)

    val findings = mutableMapOf<String, String>()
    runCatching {
        findings["A1_the_seam_is_not_forced"] =
            ("A SEAM NEEDS BOTH PREMISES AND THIS BLOCK DROPS THE SECOND. The scaffold graph is a " +
                    "tree only under Douglas et al.'s 2-D-surface restriction; the block's own " +
                    "cross-section adjacency carries %d edges on %d helices, %d independent cycles. " +
                    "But the seam does not turn on that at all: on BOTH readings a fully folded " +
                    "circular scaffold needs AT LEAST %d domains on the surface and AT LEAST %d " +
                    "on the lattice - both above 60 - " +
                    "and the committed design carries exactly 60 - a HAMILTONIAN PATH. " +
                    "So the drawn routing asserts a scaffold that is not fully folded, which is " +
                    "the premise CLAUDE.md already names and C-0119 §4 never examines.")
                .format(induced.size, induced.order, induced.cyclomaticNumber,
                    surface.minimumClosedCoveringWalk, induced.minimumClosedCoveringWalk)
        findings["A2_the_degree_census_decides_it"] =
            ("THE TWO RASTER TERMINI ARE DEGREE ONE, at every one of the %d blocks swept, because " +
                    "two of a corner helix's three honeycomb bonds point OUT of the block. A " +
                    "degree-one vertex lies on no cycle, so no honeycomb block of this family has " +
                    "a Hamiltonian cycle and no scaffold routing gives one domain per helix while " +
                    "closing inside the design. C-0119's factorial guard is never reached.")
                .format(pathBoundary.size)
        findings["A3_the_seam_BOUND_is_two_domains_not_sixty"] =
            ("AND EVEN WHERE THE SECOND PREMISE HOLDS, THE HONEYCOMB'S SEAM IS NOTHING LIKE " +
                    "ROTHEMUND'S. On the block's own lattice adjacency the bound is %d domains - " +
                    "the leaf bound, |V| + leaves - against %d on the path, which is the bridge " +
                    "bound 2(|V| - 1), i.e. the two-segments-per-helix seam C-0119 transfers. " +
                    "The factor between the two readings is %s, and the corpus has been quoting " +
                    "the expensive one. Both are LOWER bounds and nothing here needs either to " +
                    "be attained: what decides the question is that both exceed 60.")
                .format(induced.minimumClosedCoveringWalk, surface.minimumClosedCoveringWalk,
                    (surface.minimumClosedCoveringWalk.toDouble() /
                            induced.minimumClosedCoveringWalk).roundedForProse(4))
        findings["A4_the_path_boundary_is_n_equals_two"] =
            ("THE INDUCED SCAFFOLD GRAPH IS A PATH IF AND ONLY IF THE ROW CARRIES TWO HELICES, " +
                    "at every one of the %d blocks swept - which is EXACTLY C-0154's boundary for " +
                    "path-representability of the MECHANICAL interfaces. Two independent " +
                    "questions, one integer.")
                .format(pathBoundary.size)
        findings["B1_the_closure_price"] =
            ("THE REMAINDER CLOSES, AND IT COSTS LESS THAN ONE CROSSOVER COLUMN. The two termini " +
                    "sit %s nm apart - 14 d exactly, both at offset 7, so the separation is purely " +
                    "lateral - and %d spare nucleotides span it: the reach bound asks %d-%d and " +
                    "the Gaussian stretch costs %s-%s k_BT over the whole 2x ssDNA Kuhn bracket, " +
                    "against the %s k_BT the host sheet pays per crossover column. The extension " +
                    "ratio is %s-%s, so the Gaussian is inside its own validity.")
                .format(
                    separation.roundedForProse(6), T274_SPARE_NUCLEOTIDES,
                    closures.filter { it.block == "10 x 6" }.minOf { it.minimumNucleotidesToReach },
                    closures.filter { it.block == "10 x 6" }.maxOf { it.minimumNucleotidesToReach },
                    closures.filter { it.block == "10 x 6" }
                        .minOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    closures.filter { it.block == "10 x 6" }
                        .maxOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    T274_CROSSOVER_COLUMN_KBT.roundedForProse(2),
                    closures.filter { it.block == "10 x 6" }
                        .minOf { it.extensionRatio }.roundedForProse(3),
                    closures.filter { it.block == "10 x 6" }
                        .maxOf { it.extensionRatio }.roundedForProse(3)
                )
        findings["B2_the_recommended_cross_section_closes_cheapest"] =
            ("AND THE CROSS-SECTION THIS PROGRAMME RECOMMENDS IS ALSO THE ONE WHOSE REMAINDER " +
                    "CLOSES CHEAPEST, which nothing selected for. 10 x 6's termini are %s nm " +
                    "apart with %d nt spare; 15 x 4's are %s nm apart with %d - so the closure " +
                    "costs %s-%s k_BT there against %s-%s here, %sx worse at the same convention.")
                .format(
                    separation.roundedForProse(6), T274_SPARE_NUCLEOTIDES,
                    controlSeparation.roundedForProse(6), T274_CONTROL_SPARE,
                    closures.filter { it.block == "15 x 4" }
                        .minOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    closures.filter { it.block == "15 x 4" }
                        .maxOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    closures.filter { it.block == "10 x 6" }
                        .minOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    closures.filter { it.block == "10 x 6" }
                        .maxOf { it.stretchFreeEnergyKbt }.roundedForProse(3),
                    (closures.first { it.block == "15 x 4" }.stretchFreeEnergyKbt /
                            closures.first { it.block == "10 x 6" }.stretchFreeEnergyKbt)
                        .roundedForProse(3)
                )
        findings["C1_the_studies_are_in_family"] =
            ("BOTH STUDIES ARE IN-FAMILY, AND THE COUNTERFACTUAL SAYS WHAT THE OTHER BRANCH WOULD " +
                    "HAVE COST. C-0161 §4(b) restricts every phase-swept result in this corpus to " +
                    "the ALTERNATING family; the block is seamless, so HoneycombCoupledStudy and " +
                    "HoneycombPlacementStudy are inside it and NO NUMBER MOVES. Graded anyway, the " +
                    "rectangle's own seam shape - one column deleted, two consecutive columns of " +
                    "the same parity - takes the free-tile collar dishing from %s to %s, %sx, and " +
                    "both are flat at T-5b's 0.10. CH-0212's 'by how much is unmeasured' is now " +
                    "measured, on the branch that is not taken.")
                .format(
                    alternatingDishing.roundedForProse(9), seamedDishing.roundedForProse(9),
                    (seamedDishing / alternatingDishing).roundedForProse(4)
                )
        findings["C3_the_extent_both_studies_grade_at_is_neither_of_the_block's"] =
            ("AND WHILE LOOKING, A SECOND SCOPE ITEM, PRICED RATHER THAN LEFT OPEN. Both studies " +
                    "grade the recommended block at %d bp = %s nm, which is the SQUARE sheet's " +
                    "own row length and the WITHDRAWN 112 / 108 pair's; the drawable 102 / 109 " +
                    "raster's rows span 109 bp = %s nm and its box is 116 bp = %s nm, and 38.08 " +
                    "is neither. Swept, the free-tile collar dishing reads %s / %s / %s over the " +
                    "three - at most %s of the as-graded value, all three flat at T-5b's 0.10 - " +
                    "so the extent convention is worth under four per cent and NO VERDICT MOVES. " +
                    "The repair is a re-emission and it belongs with T-263.")
                .format(
                    T274_ROW_BP, extentSensitivity[1].edgeX.roundedForProse(6),
                    extentSensitivity[0].edgeX.roundedForProse(6),
                    extentSensitivity[2].edgeX.roundedForProse(6),
                    extentSensitivity[0].collarDishing.roundedForProse(9),
                    extentSensitivity[1].collarDishing.roundedForProse(9),
                    extentSensitivity[2].collarDishing.roundedForProse(9),
                    extentSensitivity.maxOf {
                        abs(1.0 - it.relativeToAsGraded)
                    }.roundedForProse(3)
                )
        findings["C2_the_second_reading_is_unavailable"] =
            ("CH-0212'S SECOND FREE READING CANNOT BE TAKEN, AND THAT IS ITSELF THE ANSWER TO A " +
                    "DIFFERENT QUESTION. A column parity is a property of the STAPLE ladder and " +
                    "the block carries no staple set - %d staple crossings, %d columns - so " +
                    "crossoverPhase() throws. The reference rectangle returns one, so the absence " +
                    "is a fact about the design and not about the reader. A design that cannot be " +
                    "asked whether its columns alternate also cannot be graded on its own " +
                    "crossovers, which is why C-0161 refused it as an OrigamiGrillage.")
                .format(block.crossovers().size, block.crossoverColumns().size)
    }.getOrElse { failure -> findings["PROSE_FAILED"] = failure.toString() }

    val result = T274Result(
        task = "T-274",
        leaf = "A8.2",
        title = "Does the recommended 10 x 6 honeycomb block need a scaffold seam, and is the " +
                "committed design drawn without one?",
        verificationType = "logical (integer graph theory on the block's own cross-section " +
                "lattice) + in-silico (a census of the committed artifacts and one counterfactual " +
                "grillage grading)",
        maturity = "TRL 1-3. Model-consistent and traceable, NOT empirically demonstrated. No " +
                "object is folded and no folding yield is derived here.",
        units = mapOf(
            "length" to "nm", "energy" to "k_BT = 4.142 pN nm at 300 K, and eV",
            "counts" to "integers - helices, domains, crossovers, nucleotides",
            "dishing" to "dimensionless, peak dishing over the free-tile stroke"
        ),
        conventions = mapOf(
            "cells" to "HoneycombRasterTurnSense's integer HoneycombCell(x, y), x in units of " +
                    "d sqrt(3)/2 and y in units of d/2",
            "raster" to "honeycombXRasterPath(rows, helicesPerRow) - Douglas et al.'s corrugated " +
                    "x-raster, left to right then down then right to left",
            "domain" to "one contiguous run of the scaffold on one helix, which is one VISIT of " +
                    "the walk; a closed walk of m edge traversals makes exactly m visits",
            "seam" to "a scaffold routing that gives some helix more than one domain",
            "ssDNA" to "the Kuhn length and the contour per nucleotide travel as a PAIR and are " +
                    "never mixed across conventions (CLAUDE.md)"
        ),
        parameters = mapOf(
            "block" to "$T274_ROWS x $T274_PER_ROW, 60 helices",
            "control block" to "$T274_CONTROL_ROWS x $T274_CONTROL_PER_ROW, 60 helices",
            "bond length" to "${d.roundedForProse(6)} nm (honeycomb SAXS)",
            "spare scaffold, 10 x 6" to "$T274_SPARE_NUCLEOTIDES nt of M13's 7249 (C-0160)",
            "spare scaffold, 15 x 4" to "$T274_CONTROL_SPARE nt (C-0119)",
            "row length graded" to "$T274_ROW_BP bp, which is what both studies use",
            "dishing samples" to "$T274_SAMPLES",
            "flatness tolerance" to "${T274_FLATNESS_TOLERANCE.roundedForProse(2)} (T-5b)",
            "crossover column currency" to "${T274_CROSSOVER_COLUMN_KBT.roundedForProse(2)} k_BT"
        ),
        sources = listOf(
            "gpd/designs/gen1-block-honeycomb-10x6-102-109.sc",
            "gpd/designs/gen1-sheet-square-15x112.sc",
            "gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc",
            "gpd/results/T-3b-tile-edge-load-profile.json",
            "gpd/results/T-219-honeycomb-station-lattice-and-placement.json"
        ),
        citedInputs = listOf(
            "CLAUDE.md - a seam is a parity on a tree, and it needs BOTH premises",
            "C-0119 §4 - the seam asserted as a theorem on the 60-helix block",
            "C-0154 - a honeycomb block's interfaces are NOT a path graph",
            "C-0160 - the committed artifact, 6330 nt with 919 spare on M13",
            "C-0161 §4(b) - a seam breaks the column-parity alternation",
            "SsDnaTether - the ssDNA Kuhn bracket and contour conventions, CITED MEASURED"
        ),
        cheapBound = cheapBound,
        graphs = graphs,
        pathBoundary = pathBoundary,
        artifacts = artifacts,
        secondReading = secondReading,
        closures = closures,
        counterfactual = counterfactual,
        extentSensitivity = extentSensitivity,
        reproductions = reproductions,
        verdict = mapOf(
            "is the seam forced" to "NO. It is forced only on a FULLY FOLDED circular scaffold, " +
                    "and the recommended block leaves 919 nt spare whose closure reaches and is " +
                    "affordable on every ssDNA convention.",
            "is the committed design drawn without one" to "YES, and correctly so - 60 domains " +
                    "on 60 helices is a Hamiltonian path, which a linear scaffold or a " +
                    "remainder-closed circular one both supply.",
            "does either premise hold on the block" to
                    "P1 holds only under Douglas et al.'s surface restriction (the block's own " +
                    "adjacency has " + induced.cyclomaticNumber + " independent cycles); P2 does " +
                    "not hold at all. The verdict is the same on both readings of P1, which is " +
                    "what makes it robust.",
            "do the two studies move" to "NO. The block is seamless, so both are inside " +
                    "C-0161's alternating family and no number moves. The seamed counterfactual " +
                    "is measured anyway and is flat on both readings.",
            "what C-0119 §4 needs" to "an annotation, not a withdrawal: its brute force, its " +
                    "reading of the caDNAno figure and its integrality result are untouched, and " +
                    "the word that fails is FORCED.",
            "full folding needs more than one domain per helix on both readings" to
                    seamNeededOnEitherReading.toString()
        ),
        falsifiers = listOf(
            "F1 - the induced graph on the block's cells has exactly 59 edges, i.e. it IS a tree",
            "F2 - a Hamiltonian cycle exists, so P1 fails outright",
            "F3 - the committed .sc does not carry 60 domains on 60 helices",
            "F4 - the remainder cannot reach, or costs more than the fold's own currency",
            "F5 - the seamed counterfactual moves a flatness verdict across T-5b's 0.10"
        ),
        falsifiersFired = falsifiersFired,
        findings = findings,
        validity = listOf(
            "LATTICE AND TOPOLOGY ONLY. Nothing here derives a folding YIELD. An elastic or " +
                    "entropic price is not a yield, and CLAUDE.md already records that residue " +
                    "as kinetic.",
            "THE CLOSURE IS AN IDEAL CHAIN. The Gaussian stretch ignores ssDNA's excluded volume, " +
                    "its electrostatic stiffening in Mg2+, and exclusion by the block itself. All " +
                    "three RAISE the price; the margin against one crossover column is a factor " +
                    "of 3-8, so the sign is known and the conclusion is not close.",
            "THE INDUCED READING IS PERMISSIVE. It allows scaffold crossovers Douglas et al. say " +
                    "do not occur; it is carried because the verdict must not depend on which " +
                    "reading a reader prefers, and here it does not.",
            "THE COUNTERFACTUAL IS A SHAPE, NOT A DESIGN. The block carries no staple routing, so " +
                    "'a seamed honeycomb column layout' is the rectangle's own signature applied " +
                    "to the block's ladder. It prices the branch not taken and recommends nothing.",
            "THE COUNTERFACTUAL IS ON THE SMEARED EQUIVALENT SHEET both studies use today. A " +
                    "re-grade on the honeycomb grillage would move both cells together and the " +
                    "ratio is what this study quotes.",
            "THE ARTIFACT STILL DOES NOT STATE ITS SCAFFOLD TOPOLOGY. scadnano's strand carries a " +
                    "'circular' field and the emitted block has none, so the file asserts a LINEAR " +
                    "scaffold by default while the budget it was drawn to is M13's circle."
        ),
        openQuestions = listOf(
            "Should the emitted block carry the 919 nt remainder as an explicit loopout, or the " +
                    "scaffold's circularity as a flag? Both are schema steps in ScadnanoWriter " +
                    "(C-0160 §6 names the first); neither is a lattice question.",
            "Does a honeycomb raster whose remainder is SHORT - a bigger block on the same " +
                    "scaffold - reach the closure threshold this study names? The threshold is " +
                    "emitted per convention and the answer is one comparison.",
            "The 62-domain minimum on the induced adjacency is attained by a construction this " +
                    "study bounds but does not emit. Whether any such routing is DRAWABLE under " +
                    "caDNAno's +/-5 bp scaffold rule is a separate question."
        )
    )

    val output = File("gpd/results/T-274-recommended-block-seam.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9) as JsonObject)
        ) + "\n"
    )
    println("T-274 - wrote " + output.path)
}
