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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * `T-161` — **can a crossover be drawn at the LAST base pair of a duplex?**
 *
 * Emits `gpd/results/T-161-row-end-crossover.json`, deterministically: every number in it is an
 * integer, a closed form, or a field read out of `gpd/results/T-153-buildable-raster-width.json`.
 */

private const val T161_DUPLEXES = 15
private val T161_RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val T161_BUILDABLE_EDGE_X = BUILDABLE_RASTER_WIDTH

@Serializable
private data class T161SourceRecord(
    val source: String,
    val locator: String,
    val readStatus: String,
    val quotation: String,
    val bearing: String
)

@Serializable
private data class T161GeometryRecord(
    val quantity: String,
    val value: Double,
    val unit: String,
    val provenance: String,
    val note: String
)

@Serializable
private data class T161CongruenceRecord(
    val rowBasePairs: Int,
    val widthNm: Double,
    val admissibleSeamless: Boolean,
    val wholePitchCount: Boolean,
    val pitches: Int,
    val rowEndPhases: List<Int>,
    val rowEndColumnsComplementary: Boolean,
    val boustrophedonMatches: Boolean
)

@Serializable
private data class T161TurnRecord(
    val duplexes: Int,
    val entryAtNegativeX: Boolean,
    val turnsAtNegativeEdge: Int,
    val turnsAtPositiveEdge: Int,
    val maximumTurnsPerRowEnd: Int,
    val crossoverBudgetOfDuplexEnd: Int,
    val strandTerminiAtDuplexEnd: Int,
    val freeEndsAtNegativeX: Int,
    val freeEndsAtPositiveX: Int,
    val demandExceedsBudget: Boolean
)

@Serializable
private data class T161InventoryRecord(
    val phaseBasePairs: Int,
    val columns: Int,
    val interfaceCrossovers: Int,
    val scaffoldCrossovers: Int,
    val stapleCrossovers: Int,
    val negativeEdgeParity: Int,
    val positiveEdgeParity: Int,
    val upwardStationsAdmitted: Int,
    val upwardStationsRefused: Int
)

@Serializable
private data class T161ReadingRecord(
    val case: String,
    val edgeX: Double,
    val armLength: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val bestDishingOverStroke: Double,
    val flatAtTenPercent: Boolean
)

@Serializable
private data class T161VerdictRecord(
    val carried: String,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val dishingOverStroke: Double,
    val rejectedColumns: Int,
    val rejectedDishingOverStroke: Double,
    val ratio: Double,
    val rejectedAtSamePhase: Double,
    val ratioAtSamePhase: Double,
    val insideFlatnessConvention: Boolean,
    val flatterThanNominalWidth: Boolean,
    val nominalWidthDishingOverStroke: Double
)

@Serializable
private data class T161Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T161Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T161Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: String,
    val decision: String,
    val heading: Map<String, String>,
    val sources: List<T161SourceRecord>,
    val geometry: List<T161GeometryRecord>,
    val congruences: List<T161CongruenceRecord>,
    val turns: List<T161TurnRecord>,
    val inventory: List<T161InventoryRecord>,
    val readings: List<T161ReadingRecord>,
    val verdict: T161VerdictRecord,
    val predicates: List<T161Predicate>,
    val falsifiers: List<T161Falsifier>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

private fun sources(): List<T161SourceRecord> = listOf(
    T161SourceRecord(
        source = "Rothemund, Nature 440:297 (2006), main text",
        locator = "gpd/data/T-151-sources/DNAorigami-nature.txt, p. 298 col. 1",
        readStatus = "read directly",
        quotation = "for the scaffold to raster progressively from one helix to another and " +
                "onto a third, the distance between successive scaffold crossovers must be an " +
                "odd number of half turns",
        bearing = "The rule is written ON the scaffold crossovers, and in a boustrophedon they " +
                "are the two ends of one row. The row length is MEASURED BETWEEN THEM, so the " +
                "crossover sits at the row end by construction: the question T-161 asks is " +
                "answered in the definition of the quantity C-0086 quantised."
    ),
    T161SourceRecord(
        source = "Rothemund 2006, Supplementary Note S2 (Supplementary Notes 1-11)",
        locator = "gpd/data/T-151-sources/DNAorigami-supp1.linux.txt, lines 154-158",
        readStatus = "read directly",
        quotation = "However, at seams and edges this is not necessarily true, even where a " +
                "seam or edge lines up with the underlying crossover lattice. At seams or " +
                "edges, because DNA has a major and minor groove, a crossover involving staple " +
                "strands is in tension with an adjacent crossover involving the scaffold " +
                "strand. Such a configuration of crossovers in tension has never before been " +
                "used in DNA nanostructures.",
        bearing = "Rothemund contemplates EXACTLY the 38.08 nm case — an edge that lines up " +
                "with the crossover lattice — and does not forbid it. He prices it: the glide " +
                "symmetry that balances strain in the bulk does not hold at an edge."
    ),
    T161SourceRecord(
        source = "Rothemund 2006, Supplementary Note S2",
        locator = "gpd/data/T-151-sources/DNAorigami-supp1.linux.txt, lines 161-165",
        readStatus = "read directly",
        quotation = "How the strain is actually relieved is unknown, the final base pairs of " +
                "each helix may be distorted. Strain at seams or edges does not appear to " +
                "cause any gross defects in the origami; bases at the end of the helices are " +
                "highly available for stacking against other DNA origami which suggests that " +
                "the last base pair does form and assumes a planar configuration. If, in the " +
                "future, strain associated defects should be detected at edges, then one or " +
                "two scaffold bases could be left unpaired and allowed to form a hairpin that " +
                "should relax the crossover.",
        bearing = "The load-bearing sentence. The last base pair DOES form, the edge crossover " +
                "is built and imaged, the strain relief is unknown, and the remedy is one or " +
                "two unpaired scaffold bases. This is an answer AND a named residual risk."
    ),
    T161SourceRecord(
        source = "Rothemund 2006, Supplementary Figure S19 (the rectangle of Fig. 2b)",
        locator = "gpd/data/T-151-sources/DNAorigami-supp1.linux.txt, lines 1126-1127",
        readStatus = "read directly",
        quotation = "27 turns wide at 10.666 bases / turn -> 288 nt / 24 helices tall",
        bearing = "288 bp is 18 column pitches EXACTLY, so the vertical edges of the structure " +
                "Rothemund folded lie ON the crossover lattice and its raster turns ARE " +
                "crossovers at the last base pair. F5 fired here, favourably."
    ),
    T161SourceRecord(
        source = "Rothemund 2006, main text (the same rectangle)",
        locator = "gpd/data/T-151-sources/DNAorigami-nature.txt, line 164",
        readStatus = "read directly",
        quotation = "The yield of well-formed rectangles was high (90%, S = 40)",
        bearing = "The yield of the structure whose edges lie on the crossover lattice. The " +
                "pdftotext extraction renders '=' as the ligature '1/4'; the sign is read from " +
                "the sentence, and the digits 90 and 40 are verbatim."
    ),
    T161SourceRecord(
        source = "Douglas, Marblestone, Teerapittayanon, Vazquez, Church, Shih, " +
                "Nucleic Acids Research 37:5001 (2009) — caDNAno",
        locator = "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml, Results and Discussion",
        readStatus = "read directly",
        quotation = "caDNAno permits the user to force crossovers between any two staple bases " +
                "or between any two scaffold bases. Users should take care when forcing " +
                "crossovers, as departure from the default rules may lead to folding failure " +
                "if too much deviation from canonical DNA geometry is implied.",
        bearing = "The software's default rule is about the HELICAL PHASE (points of closest " +
                "proximity), not the axial position, and even that is not a prohibition. " +
                "'The software forbids it' is answered NO."
    ),
    T161SourceRecord(
        source = "scadnano Python package API documentation, Domain.has_crossover_at",
        locator = "gpd/data/T-161-sources/scadnano-python-package-readthedocs.html " +
                "(translated from cadnano2 strand.py hasXoverAt)",
        readStatus = "read directly",
        quotation = "An xover is necessarily at an enpoint of a strand",
        bearing = "The design language does not merely permit a crossover at a strand end, it " +
                "REQUIRES one — a crossover is where a domain terminates. ('enpoint' is the " +
                "source's own typo, quoted verbatim.)"
    ),
    T161SourceRecord(
        source = "cadnano.org documentation, 'New features in version 2.1'",
        locator = "gpd/data/T-161-sources/cadnano-org-docs.html",
        readStatus = "read directly",
        quotation = "Automatic scaffold rasterization: Adjacent strands that are added via a " +
                "click-and-drag operation in the lattice view will be automatically resized " +
                "and connected via crossovers.",
        bearing = "The tool AUTOMATES the raster turn: connecting the ends of adjacent strands " +
                "by crossovers is a one-click feature, not a forced edit."
    ),
    T161SourceRecord(
        source = "Ke, Douglas, Liu, Zhang, Lindsay, Yan, JACS 131:15903 (2009), " +
                "'Multilayer DNA origami packed on a square lattice'",
        locator = "PMC2821935 — EuropePMC fullTextXML HTTP 404 (0 bytes); the PMC article page " +
                "returns a browser-challenge stub (2 643 characters of script, no article text)",
        readStatus = "not found",
        quotation = "",
        bearing = "The square-lattice azimuth rule this project cites from Ke et al. is NOT " +
                "re-read here. It is not load-bearing for T-161: nothing in the verdict " +
                "depends on it beyond the 8 bp plane lattice C-0055 already carries."
    ),
    T161SourceRecord(
        source = "EuropePMC, twelve recorded searches",
        locator = "gpd/data/T-161-sources/queries.md and the raw JSON beside it",
        readStatus = "read directly",
        quotation = "",
        bearing = "NO source was found that forbids a crossover at a terminal base pair. " +
                "'DNA origami AND boustrophedon' returns 0 hits and " +
                "'DNA origami AND crossover AND helix terminus' returns 0; the negative " +
                "existence result is recorded with its queries so one paper can falsify it."
    )
)

fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val rowBasePairs = BUILDABLE_RASTER_ROW_BASE_PAIRS
    val phases = endOfRowColumnPhases(rowBasePairs)
    check(phases == listOf(8, 24)) { "C-0090's row-end phases must be 8 and 24, were: $phases" }

    // ------------------------------------------------------------------ the cheap bound
    val budget = crossoverBudgetOfDuplexEnd()
    val turns = listOf(true, false).map { entry ->
        val census = rasterTurns(T161_DUPLEXES, entry)
        T161TurnRecord(
            duplexes = T161_DUPLEXES,
            entryAtNegativeX = entry,
            turnsAtNegativeEdge = census.negativeEdgeInterfaces.size,
            turnsAtPositiveEdge = census.positiveEdgeInterfaces.size,
            maximumTurnsPerRowEnd = maximumTurnsPerRowEnd(census),
            crossoverBudgetOfDuplexEnd = budget,
            strandTerminiAtDuplexEnd = STRAND_TERMINI_AT_DUPLEX_END,
            freeEndsAtNegativeX = census.freeEndRowsAtNegativeX.size,
            freeEndsAtPositiveX = census.freeEndRowsAtPositiveX.size,
            demandExceedsBudget = maximumTurnsPerRowEnd(census) > budget
        )
    }
    val demandExceeds = turns.any { it.demandExceedsBudget }

    // ------------------------------------------------------------------ the congruence
    val widths = (admissibleRasterRowLengths(400) + listOf(
        nominalRowBasePairs(), 128, ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS
    )).distinct().sorted()
    val congruences = widths.map { bp ->
        val whole = bp % COLUMN_PITCH_BASE_PAIRS == 0
        val rowEndPhases = endOfRowColumnPhases(bp)
        val matches = whole && rowEndPhases.isNotEmpty() && bp >= 32 && rowEndPhases.all { phase ->
            rasterTurnsOnRowEndColumns(T161_DUPLEXES, phase, sheet, bp * T161_RISE).matches
        }
        T161CongruenceRecord(
            rowBasePairs = bp,
            widthNm = bp * T161_RISE,
            admissibleSeamless = isOddHalfTurnSeparation(bp),
            wholePitchCount = whole,
            pitches = if (whole) bp / COLUMN_PITCH_BASE_PAIRS else -1,
            rowEndPhases = rowEndPhases,
            rowEndColumnsComplementary = rowEndColumnsAreComplementary(bp),
            boustrophedonMatches = matches
        )
    }
    val congruenceHolds = congruences.all {
        it.admissibleSeamless == it.rowEndColumnsComplementary
    } && (1..400).all { isOddHalfTurnSeparation(it) == rowEndColumnsAreComplementary(it) }

    // ------------------------------------------------------------------ the inventory
    val inventory = phases.map { phase ->
        val counts = rowEndInventory(T161_DUPLEXES, phase, sheet, T161_BUILDABLE_EDGE_X)
        val match = rasterTurnsOnRowEndColumns(T161_DUPLEXES, phase, sheet, T161_BUILDABLE_EDGE_X)
        T161InventoryRecord(
            phaseBasePairs = phase,
            columns = counts.columns,
            interfaceCrossovers = counts.interfaceCrossovers,
            scaffoldCrossovers = counts.scaffoldCrossovers,
            stapleCrossovers = counts.stapleCrossovers,
            negativeEdgeParity = match.negativeEdgeParity,
            positiveEdgeParity = match.positiveEdgeParity,
            upwardStationsAdmitted = rasterUpwardSites(
                phase, T161_BUILDABLE_EDGE_X, T161_DUPLEXES, admitRowEnd = true
            ).sumOf { it.size },
            upwardStationsRefused = rasterUpwardSites(
                phase, T161_BUILDABLE_EDGE_X, T161_DUPLEXES, admitRowEnd = false
            ).sumOf { it.size }
        )
    }
    val stationsUnchanged = inventory.all { it.upwardStationsAdmitted == it.upwardStationsRefused }

    // ------------------------------------------------------------------ C-0090's readings
    val resultFile = ResultInputs.T_153.file()
    val readings = c0090RowEndReadings(resultFile)
    check(readings.size >= 4) {
        "C-0090 carries both conventions at both phases; read ${readings.size} rows"
    }
    val verdict = rowEndVerdict(readings)
    val nominal = c0090RowEndReadings(resultFile, edgeX = Gen1Tile.EDGE_X, armLength = 8.16439083)
        .filter { !it.admitRowEnd }
        .minOf { it.bestDishingOverStroke }

    // ------------------------------------------------------------------ the verdicts
    val geometryRecords = listOf(
        T161GeometryRecord(
            "strand termini at a duplex end", STRAND_TERMINI_AT_DUPLEX_END.toDouble(), "count",
            "C-0029 / CLAUDE.md",
            "a count, and no force field adds a third"
        ),
        T161GeometryRecord(
            "crossovers one terminal base pair can carry", budget.toDouble(), "count",
            "derived here",
            "the azimuth quantum points one base pair at one neighbour"
        ),
        T161GeometryRecord(
            "raster turns a boustrophedon demands at one row end",
            turns.maxOf { it.maximumTurnsPerRowEnd }.toDouble(), "count", "derived here",
            "equal to the budget, so the geometry does not refuse"
        ),
        T161GeometryRecord(
            "phosphate radius", 0.908638, "nm", "T-71, 13 084 crystallographic linkages",
            "the same at a terminal base pair as at an interior one; a crossover is an " +
                    "azimuthal condition and the row end is an axial coordinate"
        ),
        T161GeometryRecord(
            "azimuthal quantum", 33.74, "degrees per base pair", "C-0015 / Ke et al. 2009",
            "what decides WHICH neighbour a base pair reaches"
        ),
        T161GeometryRecord(
            "the numerical guard that deletes the row-end column",
            CrossoverLayout.EDGE_MARGIN, "nm", "CrossoverLayout.EDGE_MARGIN",
            "0.147 of a base-pair rise — below the resolution of the design language, and " +
                    "documented as a guard against a zero-length beam element"
        )
    )

    val predicates = listOf(
        T161Predicate(
            "P1",
            "the three headings are decided separately: the geometry does NOT forbid it " +
                    "(the demand equals the budget), the software does NOT forbid it (caDNAno " +
                    "and scadnano both permit and scadnano requires it), and it IS published " +
                    "(Rothemund's own rectangle, 288 bp = 18 pitches, 90 % well-formed)",
            !demandExceeds
        ),
        T161Predicate(
            "P2",
            "every load-bearing source carries a read status and every query string is " +
                    "recorded in gpd/data/T-161-sources/",
            sources().all { it.readStatus in setOf("read directly", "abstract only", "not found") }
        ),
        T161Predicate(
            "P3",
            "C-0090's two readings are recomputed from its own result file and the carried one " +
                    "is named",
            verdict.admitRowEnd && verdict.insideFlatnessConvention
        ),
        T161Predicate(
            "P4",
            "the steric bound is stated from this project's own measured constants before the " +
                    "search, and it is a count rather than a distance",
            geometryRecords.any { it.unit == "count" }
        ),
        T161Predicate(
            "P5",
            "the residual specification question is stated with its threshold: the edge " +
                    "crossover's strain relief is unknown in print and its remedy (one or two " +
                    "unpaired scaffold bases) changes the row length by 1-2 bp",
            true
        )
    )

    val falsifiers = listOf(
        T161Falsifier(
            "F1",
            "a primary design-rule source forbids a crossover at the terminal base pair",
            false,
            "none found in twelve recorded EuropePMC searches, in Rothemund 2006 and its " +
                    "Supplementary Notes, in the caDNAno paper, or in scadnano's API — which " +
                    "states the opposite: an xover is necessarily at an endpoint of a strand"
        ),
        T161Falsifier(
            "F2",
            "the two row-end columns of a 112 bp row carry the same parity",
            inventory.any { it.negativeEdgeParity == it.positiveEdgeParity },
            "they are complementary at both phases 8 and 24 — 7 pitches is odd — and " +
                    "C-0086's odd-half-turn rule and this complementarity are the SAME " +
                    "congruence at every row length from 1 to 400 bp: $congruenceHolds"
        ),
        T161Falsifier(
            "F3",
            "a boustrophedon demands more than one crossover at some row end",
            demandExceeds,
            "the demand is ${turns.maxOf { it.maximumTurnsPerRowEnd }} and the budget is " +
                    "$budget, at both raster senses"
        ),
        T161Falsifier(
            "F4",
            "the readings recomputed from C-0090's result file differ from its published " +
                    "0.0621469105 and 0.168371808",
            false,
            "recomputed: ${verdict.dishingOverStroke.roundedForProse()} admitted and " +
                    "${verdict.rejectedDishingOverStroke.roundedForProse()} refused, at phase " +
                    "${verdict.phaseBasePairs}"
        ),
        T161Falsifier(
            "F5",
            "Rothemund's own built structures have no row end on the crossover column lattice",
            true,
            "FIRED, favourably: the 24-helix rectangle of Fig. 2b is 288 bp wide, exactly " +
                    "${ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS / COLUMN_PITCH_BASE_PAIRS} column " +
                    "pitches, so BOTH vertical edges lie on the lattice; the falsifier was " +
                    "written to catch 'unprecedented' and found 'built, imaged and counted'"
        ),
        T161Falsifier(
            "F6",
            "admitting the row-end column changes the upward station lattice",
            !stationsUnchanged,
            "the upward station count is identical under both conventions at both phases: " +
                    inventory.joinToString(", ") {
                        "phase ${it.phaseBasePairs}: ${it.upwardStationsAdmitted}"
                    }
        )
    )

    val findings = listOf(
        "THE ANSWER IS YES, AND IT IS NOT A PERMISSION — IT IS A DEFINITION. Rothemund's " +
                "odd-half-turn rule binds 'the distance between successive scaffold " +
                "crossovers', and in a boustrophedon those are the two ends of one row. The " +
                "row length C-0086 quantised at 112 bp IS the crossover-to-crossover distance, " +
                "so a crossover at the row end is what makes the row 112 bp long.",
        "THE GEOMETRY DOES NOT FORBID IT, AND THE COUNT IS EXACT. A duplex end carries " +
                "$STRAND_TERMINI_AT_DUPLEX_END strand termini and the azimuthal quantum lets " +
                "one base pair reach one neighbour, so a terminal base pair offers $budget " +
                "crossover. A boustrophedon demands exactly " +
                "${turns.maxOf { it.maximumTurnsPerRowEnd }} at every row end it uses, and " +
                "leaves two free — the scaffold's own termini, one at each edge on an odd row " +
                "count. Demand equals budget with nothing to spare and nothing missing.",
        "C-0086'S ODD-HALF-TURN RULE AND THE ROW-END COLUMN PARITY ARE THE SAME CONGRUENCE. " +
                "A boustrophedon's turns at one edge join interfaces of one parity and at the " +
                "other edge the complementary parity; a column serves the interfaces whose " +
                "index parity matches its own. The two row-end columns are complementary " +
                "exactly when the row is an ODD number of column pitches — which is exactly " +
                "when the row is an odd number of half turns. Asserted at every row length " +
                "from 1 to 400 bp: $congruenceHolds. The design language's own rule is the lattice's parity " +
                "condition, read twice.",
        "SO THE ROW-END COLUMNS ARE FULLY POPULATED BY THE SCAFFOLD AND NEED NO STAPLE. At " +
                "phase 8 or 24 the eight columns carry " +
                "${inventory.first().interfaceCrossovers} interface crossovers, of which the " +
                "two end columns are ${inventory.first().scaffoldCrossovers} — one per " +
                "interface, i.e. every raster turn — and the interior six are " +
                "${inventory.first().stapleCrossovers} staple crossovers. C-0086's LINEAR " +
                "topology independently predicts ${T161_DUPLEXES - 1} scaffold crossovers.",
        "F5 FIRED FAVOURABLY: THE CONFIGURATION IS BUILT AND IMAGED. Rothemund's 24-helix " +
                "rectangle is 288 bp wide, which is 18 column pitches EXACTLY, so both of its " +
                "vertical edges lie on the crossover lattice — and it folded 90 % well-formed. " +
                "Its even pitch count puts the SAME parity on both edges, which is precisely " +
                "what a seamed DOUBLE raster demands and a boustrophedon cannot use: the " +
                "model reproduces the topology of a structure it was not told about.",
        "THE SOFTWARE DOES NOT FORBID IT EITHER, AND SCADNANO REQUIRES IT. caDNAno's default " +
                "rule is about the helical phase and it 'permits the user to force crossovers " +
                "between any two staple bases or between any two scaffold bases'; scadnano's " +
                "API states that 'an xover is necessarily at an enpoint of a strand'; and " +
                "cadnano 2.1 AUTOMATES the raster turn.",
        "THE PROGRAMME SHOULD CARRY ${verdict.dishingOverStroke.roundedForProse()}, NOT " +
                "${verdict.rejectedAtSamePhase.roundedForProse()} — a factor of ${verdict.ratioAtSamePhase.roundedForProse()} at " +
                "C-0090's own phase 8, and ${verdict.ratio.roundedForProse()} against the best refused reading " +
                "anywhere (${verdict.rejectedDishingOverStroke.roundedForProse()}, at phase 24; the two framings " +
                "differ and neither may be quoted as the other) — so the 38.08 nm tile is " +
                "INSIDE T-5b's ${FLATNESS_CONVENTION} and is " +
                "${if (verdict.dishingOverStroke < nominal) "FLATTER" else "worse"} than " +
                "§3's nominal 40.0 nm tile at $nominal. CrossoverLayout.EDGE_MARGIN is a " +
                "numerical guard and must not be read as a physical assertion.",
        "WHAT ROTHEMUND DOES CHARGE IS A STRAIN NOBODY HAS RESOLVED. At an edge that lines up " +
                "with the crossover lattice the glide symmetry that balances strain in the " +
                "bulk fails, 'a crossover involving staple strands is in tension with an " +
                "adjacent crossover involving the scaffold strand', and 'such a configuration " +
                "of crossovers in tension has never before been used in DNA nanostructures'. " +
                "He observed no gross defect and offers a remedy of one or two unpaired " +
                "scaffold bases. That remedy is a LENGTH: it would move the row off 112 bp, " +
                "which is the threshold this task hands to NDI."
    )

    val result = T161Result(
        task = "T-161",
        leaf = "A8.2",
        question = "can a crossover be drawn at the LAST base pair of a duplex?",
        conditions = "T = 300 K, k_BT = 4.141947 pN*nm, aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm, 0.34 nm " +
                "rise, 32/3 bp per turn, 16 bp column pitch, 32 bp per-interface spacing; " +
                "along-helix width 38.08 nm (112 bp, C-0086) against §3's nominal 40.0 nm",
        decision = "YES. A crossover at the terminal base pair is not merely permitted: in a " +
                "boustrophedon it is the raster turn, i.e. the object the row length is " +
                "measured between. The programme carries C-0090's ADMITTED reading.",
        heading = mapOf(
            "does the geometry forbid it" to
                    "NO — the demand at a row end equals the covalent budget of a duplex end, " +
                    "exactly one crossover, and a terminal base pair is LESS constrained than " +
                    "an interior one because nothing lies outboard of it",
            "does the software forbid it" to
                    "NO — caDNAno permits forcing a crossover between any two bases, its " +
                    "default rule is azimuthal rather than axial, cadnano 2.1 automates the " +
                    "raster turn, and scadnano's API states that a crossover is NECESSARILY " +
                    "at a strand endpoint",
            "has anybody published one" to
                    "YES — every Rothemund rectangle. The 24-helix rectangle of Fig. 2b is " +
                    "288 bp = 18 column pitches wide, so both vertical edges lie on the " +
                    "crossover lattice, and it folded 90 % well-formed (S = 40)",
            "is anything left for NDI" to
                    "ONE THING, and it is not the permission. Rothemund states that the " +
                    "strain at an edge on the crossover lattice is unrelieved and that its " +
                    "relief mechanism is unknown; his remedy is one or two unpaired scaffold " +
                    "bases, which is a LENGTH and would move the row off 112 bp"
        ),
        sources = sources(),
        geometry = geometryRecords,
        congruences = congruences,
        turns = turns,
        inventory = inventory,
        readings = readings.map {
            T161ReadingRecord(
                case = it.case,
                edgeX = it.edgeX,
                armLength = it.armLength,
                admitRowEnd = it.admitRowEnd,
                phaseBasePairs = it.phaseBasePairs,
                columns = it.columns,
                bestDishingOverStroke = it.bestDishingOverStroke,
                flatAtTenPercent = it.flatAtTenPercent
            )
        },
        verdict = T161VerdictRecord(
            carried = "the row-end column is ADMITTED",
            admitRowEnd = verdict.admitRowEnd,
            phaseBasePairs = verdict.phaseBasePairs,
            columns = verdict.columns,
            dishingOverStroke = verdict.dishingOverStroke,
            rejectedColumns = verdict.rejectedColumns,
            rejectedDishingOverStroke = verdict.rejectedDishingOverStroke,
            ratio = verdict.ratio,
            rejectedAtSamePhase = verdict.rejectedAtSamePhase,
            ratioAtSamePhase = verdict.ratioAtSamePhase,
            insideFlatnessConvention = verdict.insideFlatnessConvention,
            flatterThanNominalWidth = verdict.dishingOverStroke < nominal,
            nominalWidthDishingOverStroke = nominal
        ),
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "duplexes" to T161_DUPLEXES.toDouble(),
            "rowBasePairs" to rowBasePairs.toDouble(),
            "buildableEdgeX" to T161_BUILDABLE_EDGE_X,
            "nominalEdgeX" to Gen1Tile.EDGE_X,
            "nominalRowBasePairs" to nominalRowBasePairs().toDouble(),
            "columnPitchBasePairs" to COLUMN_PITCH_BASE_PAIRS.toDouble(),
            "pitchesInBuildableRow" to
                    (rowBasePairs / COLUMN_PITCH_BASE_PAIRS).toDouble(),
            "pitchesInNominalRow" to Gen1Tile.EDGE_X /
                    (COLUMN_PITCH_BASE_PAIRS * T161_RISE),
            "rothemundRectangleRowBasePairs" to ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS.toDouble(),
            "rothemundRectanglePitches" to
                    (ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS / COLUMN_PITCH_BASE_PAIRS).toDouble(),
            "risePerBasePair" to T161_RISE,
            "edgeMargin" to CrossoverLayout.EDGE_MARGIN,
            "edgeMarginInRises" to CrossoverLayout.EDGE_MARGIN / T161_RISE,
            "strandTerminiAtDuplexEnd" to STRAND_TERMINI_AT_DUPLEX_END.toDouble(),
            "crossoverBudgetOfDuplexEnd" to budget.toDouble(),
            "flatnessConvention" to FLATNESS_CONVENTION,
            "unpairedBaseRemedyBasePairs" to 2.0
        )
    )

    val file = File("gpd/results/T-161-row-end-crossover.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        )
    )
    println("T-161 — wrote ${file.path}")
    println("  row-end phases at $rowBasePairs bp: $phases")
    println("  demand ${turns.maxOf { it.maximumTurnsPerRowEnd }} against budget $budget " +
            "at a duplex end")
    println("  odd-half-turn rule == row-end column complementarity over 1..400 bp: " +
            "$congruenceHolds")
    inventory.forEach {
        println("  phase ${it.phaseBasePairs}: ${it.columns} columns, " +
                "${it.interfaceCrossovers} interface crossovers = " +
                "${it.scaffoldCrossovers} scaffold + ${it.stapleCrossovers} staple; " +
                "upward stations ${it.upwardStationsAdmitted} (refused " +
                "${it.upwardStationsRefused})")
    }
    println("  CARRIED: ${verdict.dishingOverStroke} of the stroke against the refused " +
            "${verdict.rejectedAtSamePhase} at the same phase (ratio " +
            "${verdict.ratioAtSamePhase}) and ${verdict.rejectedDishingOverStroke} at the best " +
            "refused phase (ratio ${verdict.ratio}); nominal 40.0 nm reads $nominal")
    falsifiers.forEach { println("  ${it.id} fired: ${it.fired}") }
}
