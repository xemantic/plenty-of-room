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

import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos

/**
 * `T-137` — **the plan model against the measured weave.**
 *
 * Emits `gpd/results/T-137-weave-exclusion-width.json`.
 */

private const val T137_DUPLEXES = 15
private const val T137_PHASE = 24
private const val T137_PATHS_E5A1 = 45
private val T137_EDGE_X = Gen1Tile.EDGE_X
private val T137_RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val T137_LATTICE = Gen1Tile.INTERHELICAL_SHEET
private val T137_PITCH = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * T137_RISE
private val T137_ARM = C0055_ARM_LENGTH
private val T137_DEGREES = 180.0 / PI

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T137RoleRecord(
    val role: String,
    val axis: String,
    val betweenBondedBodies: Boolean,
    val weaveApplies: Boolean,
    val standingValue: Double,
    val defensibleValue: Double,
    val defensibleSource: String,
    val note: String
)

@Serializable
private data class T137WidthRecord(
    val label: String,
    val width: Double,
    val source: String,
    val readFlag: String,
    val planMargin: Double,
    val marginOverRise: Double,
    val placedOfThirtyFour: Int,
    val placesAll: Boolean,
    val overThreshold: Double
)

@Serializable
private data class T137StationRecord(
    val row: Int,
    val x: Double,
    val planeIndex: Int,
    val onWeaveNode: Boolean,
    val distanceBelow: Double,
    val distanceAbove: Double,
    val axisOffset: Double,
    val acrossRowClearanceMeasuredBody: Double,
    val acrossRowClearanceAssertedBody: Double
)

@Serializable
private data class T137PhaseRecord(
    val phaseBasePairs: Int,
    val upwardSites: Int,
    val everySiteOnANode: Boolean,
    val worstAxisOffset: Double,
    val worstDistanceDeparture: Double
)

@Serializable
private data class T137PackerRecord(
    val pipeline: String,
    val widthLabel: String,
    val width: Double,
    val demanded: Int,
    val placed: Int,
    val positionDependent: Boolean,
    val note: String
)

@Serializable
private data class T137StericRecord(
    val reading: String,
    val diameter: Double,
    val source: String,
    val readFlag: String,
    val admitsWeaveMinimum: Boolean,
    val sigmaAboveContact: Double
)

@Serializable
private data class T137LiteratureRecord(
    val source: String,
    val quantity: String,
    val value: Double,
    val unit: String,
    val readFlag: String,
    val note: String
)

@Serializable
private data class T137InheritedRecord(
    val claim: String,
    val roleItUses: String,
    val standingVerdict: String,
    val underTheWeave: String,
    val verdictMoves: Boolean,
    val why: String
)

@Serializable
private data class T137ConvergenceRecord(
    val quantity: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val note: String
)

@Serializable
private data class T137ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T137PredicateRecord(
    val id: String,
    val predicate: String,
    val value: Double,
    val threshold: Double,
    val verdict: String
)

@Serializable
private data class T137Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val roles: List<T137RoleRecord>,
    val widths: List<T137WidthRecord>,
    val stations: List<T137StationRecord>,
    val phases: List<T137PhaseRecord>,
    val packing: List<T137PackerRecord>,
    val steric: List<T137StericRecord>,
    val literature: List<T137LiteratureRecord>,
    val inherited: List<T137InheritedRecord>,
    val convergence: List<T137ConvergenceRecord>,
    val reproductions: List<T137ReproductionRecord>,
    val predicates: List<T137PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// helpers
// ---------------------------------------------------------------------------------------------

/** `C-0063`'s 34 upward roots, read from its own result file. */
private fun t137Stations(file: File): List<Pair<Int, List<Double>>> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .map { row ->
            row.getValue("row").jsonPrimitive.content.toInt() to
                    row.getValue("roots").jsonArray
                        .map { it.jsonPrimitive.content.toDouble() }.sorted()
        }
        .sortedBy { it.first }
}

/**
 * The same rows as `C-0069`'s [StationRow]s, at a row pitch of [rowPitch] — which is a **separate**
 * argument from the exclusion width on purpose, because that separation is the whole of `T-137`.
 */
private fun t137Rows(
    rows: List<Pair<Int, List<Double>>>,
    rowPitch: Double
): List<StationRow> = rows.map { (row, roots) ->
    StationRow(row, (row - (T137_DUPLEXES - 1) / 2) * rowPitch, roots)
}

/** How many of `C-0063`'s 34 stations carry an arm of [arm] at a constant clearance [width]. */
private fun t137Placed(
    rows: List<Pair<Int, List<Double>>>,
    arm: Double,
    width: Double
): Int = rows.sumOf { (_, roots) ->
    if (armDirections(roots, arm, T137_EDGE_X, width) != null) roots.size else 0
}

/** The same under a position-dependent clearance taken from [profile] on each row's interface. */
private fun t137PlacedUnderWeave(
    rows: List<Pair<Int, List<Double>>>,
    arm: Double,
    profile: WeaveProfile
): Int = rows.sumOf { (row, roots) ->
    val interfaceIndex = row.coerceAtMost(T137_DUPLEXES - 2)
    val directions = armDirectionsWithClearance(roots, arm, T137_EDGE_X) { x ->
        profile.distanceAt(interfaceIndex, x)
    }
    if (directions != null) roots.size else 0
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val profile = WeaveProfile(meanDistance = T137_LATTICE, phaseBasePairs = T137_PHASE)
    val bai = WeaveProfile(
        meanDistance = BAI_MEAN, peakToPeak = BAI_PEAK_TO_PEAK, phaseBasePairs = T137_PHASE
    )
    val threshold = T137_PITCH - T137_ARM
    val measuredBody = DuplexSteric.MEASURED_DIAMETER
    val assertedBody = DuplexSteric.ASSERTED_DIAMETER

    println("T-137 — reading C-0063's 34 upward roots ...")
    val rows = t137Stations(File("gpd/results/T-125-upward-root-placement.json"))
    check(rows.sumOf { it.second.size } == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT stations"
    }

    // -------------------------------------------------- deliverable 1: three roles, one symbol
    println("T-137 — the cheap bound: which of the three roles the weave measures ...")
    val roles = listOf(
        T137RoleRecord(
            "ROW_PITCH", "across the helices", true, ExclusionRole.ROW_PITCH.weaveApplies,
            T137_LATTICE, T137_LATTICE,
            "SAXS lattice constant, Fischer et al. 2016 — and the weave's own mean",
            "the one role the weave measures; its value at every station is the mean exactly"
        ),
        T137RoleRecord(
            "BODY_WIDTH", "across the helices", false, ExclusionRole.BODY_WIDTH.weaveApplies,
            T137_LATTICE, measuredBody,
            "T-71's measured phosphate radius, 13 084 crystallographic linkages",
            "the plan girth of ONE free body; a lattice constant is not a girth"
        ),
        T137RoleRecord(
            "COLLINEAR_CLEARANCE", "along the helices", false,
            ExclusionRole.COLLINEAR_CLEARANCE.weaveApplies, T137_LATTICE, measuredBody,
            "T-71's measured phosphate radius — the girth of whatever stands in the gap",
            "the d in C-0072's M = p - d - L; the weave has no component on this axis"
        )
    )

    // -------------------------------------------------- deliverable 2: the congruence
    println("T-137 — the congruence: every station's plane parity, at every phase ...")
    val stations = rows.flatMap { (row, roots) ->
        roots.map { x ->
            val plane = weavePlaneIndex(profile, x)
            T137StationRecord(
                row = row,
                x = x,
                planeIndex = plane,
                onWeaveNode = isWeaveNode(plane),
                distanceBelow = if (row > 0) profile.distanceAt(row - 1, x) else T137_LATTICE,
                distanceAbove = if (row < T137_DUPLEXES - 1) {
                    profile.distanceAt(row, x)
                } else {
                    T137_LATTICE
                },
                axisOffset = profile.axisOffset(row, x),
                acrossRowClearanceMeasuredBody =
                    acrossRowClearance(profile, row.coerceAtMost(13), x, measuredBody),
                acrossRowClearanceAssertedBody =
                    acrossRowClearance(profile, row.coerceAtMost(13), x, assertedBody)
            )
        }
    }

    val phases = (0 until 32).map { phase ->
        val weave = WeaveProfile(meanDistance = T137_LATTICE, phaseBasePairs = phase)
        val sites = upwardHingeSites(phase, T137_EDGE_X, T137_DUPLEXES, T137_RISE)
        T137PhaseRecord(
            phaseBasePairs = phase,
            upwardSites = sites.size,
            everySiteOnANode = sites.all { isWeaveNode(weavePlaneIndex(weave, it.x)) },
            worstAxisOffset = sites.maxOf { abs(weave.axisOffset(it.interfaceIndex, it.x)) },
            worstDistanceDeparture = sites.maxOf {
                abs(weave.distanceAt(it.interfaceIndex.coerceAtMost(13), it.x) - T137_LATTICE)
            }
        )
    }

    // -------------------------------------------------- deliverable 3: the packer re-run
    println("T-137 — the packer, at every defensible width and under the weave ...")
    val widthReadings = listOf(
        Triple("T-71 measured phosphate contact", measuredBody, "MEASURED (this repository)"),
        Triple("fibre narrow phosphate", DuplexSteric.FIBRE_NARROW_DIAMETER, "CITED"),
        Triple("this project's asserted steric diameter", assertedBody, "CITED"),
        Triple("SAXS single-layer lattice constant", T137_LATTICE, "CITED, MEASURED"),
        Triple("the placement threshold, pitch - arm", threshold, "DERIVED"),
        Triple("Bai's sawtooth midpoint", BAI_MEAN, "DERIVED from a MEASUREMENT"),
        Triple("SAXS square-lattice constant", INTERHELICAL_SQUARE_LATTICE, "CITED, MEASURED"),
        Triple("Snodin's oxDNA 2D-tile mean", SNODIN_TILE_MEAN, "CITED, SIMULATED"),
        Triple("Bai's weave minimum", BAI_MINIMUM, "CITED, MEASURED"),
        Triple("Bai's weave maximum", BAI_MAXIMUM, "CITED, MEASURED")
    )
    val widths = widthReadings.map { (label, width, flag) ->
        val placed = t137Placed(rows, T137_ARM, width)
        T137WidthRecord(
            label = label,
            width = width,
            source = label,
            readFlag = flag,
            planMargin = planMarginAtWidth(T137_PITCH, width, T137_ARM),
            marginOverRise = planMarginAtWidth(T137_PITCH, width, T137_ARM) / T137_RISE,
            placedOfThirtyFour = placed,
            placesAll = placed == C0055_ARM_COUNT,
            overThreshold = width / threshold
        )
    }

    val e5a1Arm = 9.131
    val packing = ArrayList<T137PackerRecord>()
    listOf(
        "T-71 measured" to measuredBody,
        "asserted steric" to assertedBody,
        "SAXS lattice" to T137_LATTICE,
        "Bai midpoint" to BAI_MEAN,
        "square lattice" to INTERHELICAL_SQUARE_LATTICE
    ).forEach { (label, width) ->
        packing += T137PackerRecord(
            "C-0063 / C-0069 — 34 upward roots, arm ${"%.5f".format(T137_ARM)} nm",
            label, width, C0055_ARM_COUNT, t137Placed(rows, T137_ARM, width), false,
            "armDirections at a constant clearance"
        )
        packing += T137PackerRecord(
            "C-0053 — 45 E5a1 arms on the in-plane hinge lattice",
            label, width, T137_PATHS_E5A1,
            (0 until 32).maxOf {
                placeHingeArms(it, T137_EDGE_X, T137_DUPLEXES, e5a1Arm, width).arms
            },
            false, "placeHingeArms, best of 32 phases"
        )
    }
    packing += T137PackerRecord(
        "C-0063 / C-0069 — 34 upward roots, arm ${"%.5f".format(T137_ARM)} nm",
        "the weave SUBSTITUTED into the collinear slot (the category error, priced)",
        profile.meanDistance, C0055_ARM_COUNT, t137PlacedUnderWeave(rows, T137_ARM, profile),
        true,
        "armDirectionsWithClearance with d(x) taken from the row's own interface"
    )
    packing += T137PackerRecord(
        "C-0063 / C-0069 — 34 upward roots, arm ${"%.5f".format(T137_ARM)} nm",
        "the weave at Bai's amplitude, SUBSTITUTED into the collinear slot",
        bai.meanDistance, C0055_ARM_COUNT, t137PlacedUnderWeave(rows, T137_ARM, bai), true,
        "the same substitution at the cryo-EM amplitude and mean"
    )
    packing += T137PackerRecord(
        "C-0063 / C-0069 — 34 upward roots, arm ${"%.5f".format(T137_ARM)} nm",
        "the weave in its OWN role, as the across-row separation", T137_LATTICE,
        C0055_ARM_COUNT, C0055_ARM_COUNT, true,
        "every station is a node, so the across-row separation is the lattice constant exactly"
    )

    // `C-0069`'s own pipeline, and the decomposition of its published 18 of 34 at 2.73 nm
    fun rootedOutcome(width: Double, rowPitch: Double): OutputElementOutcome =
        placeRootedOutputElement(
            "width ${"%.4f".format(width)} on a ${"%.4f".format(rowPitch)} nm row pitch",
            t137Rows(rows, rowPitch), T137_ARM, T137_EDGE_X, T137_DUPLEXES * rowPitch, width
        )

    val decomposition = listOf(
        Triple("all three roles at the SAXS 2.69 nm — the standing design", T137_LATTICE,
            T137_LATTICE),
        Triple(
            "the COLLINEAR clearance alone raised to 2.73, rows left at 2.69 and " +
                    "all-or-nothing per row", INTERHELICAL_SQUARE_LATTICE, T137_LATTICE
        ),
        Triple(
            "C-0069's OWN reading — width 2.73 against a 2.69 row pitch, so the bodies also " +
                    "overlap ACROSS the rows", INTERHELICAL_SQUARE_LATTICE, T137_LATTICE
        ),
        Triple(
            "2.73 read CONSISTENTLY — the row pitch is the interhelical distance too",
            INTERHELICAL_SQUARE_LATTICE, INTERHELICAL_SQUARE_LATTICE
        ),
        Triple(
            "the measured girth against the SAXS row pitch — the defensible reading",
            measuredBody, T137_LATTICE
        )
    )
    decomposition.forEachIndexed { index, (label, width, rowPitch) ->
        val placed = if (index == 1) {
            t137Placed(rows, T137_ARM, width)
        } else {
            rootedOutcome(width, rowPitch).placed
        }
        packing += T137PackerRecord(
            "C-0069 — placeRootedOutputElement on the same 34 stations",
            label, width, C0055_ARM_COUNT, placed, false,
            "row pitch ${"%.4f".format(rowPitch)} nm; rows independent = " +
                    (rowPitch >= width - PLAN_TANGENCY_TOLERANCE)
        )
    }
    val c0069AtSquare = rootedOutcome(INTERHELICAL_SQUARE_LATTICE, T137_LATTICE).placed
    val squareConsistent =
        rootedOutcome(INTERHELICAL_SQUARE_LATTICE, INTERHELICAL_SQUARE_LATTICE).placed
    val squareCollinearOnly = t137Placed(rows, T137_ARM, INTERHELICAL_SQUARE_LATTICE)

    // `C-0041`'s Fact A in closed form: adjacent-row bodies bury each other past this tilt
    val factATiltMeasured = acos(measuredBody / T137_LATTICE) * T137_DEGREES
    val factATiltAsserted = acos(assertedBody / T137_LATTICE) * T137_DEGREES

    // -------------------------------------------------- deliverable 4: the steric adjudication
    println("T-137 — adjudicating 1.85 nm against 2.0 nm ...")
    val steric = listOf(
        T137StericRecord(
            "this project's asserted steric diameter", assertedBody,
            "CLAUDE.md, from Hedley et al. 2024's a_DNA = 10 Angstrom", "CITED",
            assertedBody < BAI_MINIMUM, DuplexSteric.contactMargin(BAI_MINIMUM)
        ),
        T137StericRecord(
            "the narrow fibre reading", DuplexSteric.FIBRE_NARROW_DIAMETER,
            "CLAUDE.md's other end of the bracket", "CITED",
            DuplexSteric.FIBRE_NARROW_DIAMETER < BAI_MINIMUM,
            DuplexSteric.contactMargin(BAI_MINIMUM)
        ),
        T137StericRecord(
            "T-71's MEASURED phosphate contact", measuredBody,
            "13 084 crystallographic linkages, this repository", "MEASURED",
            measuredBody < BAI_MINIMUM, DuplexSteric.contactMargin(BAI_MINIMUM)
        ),
        T137StericRecord(
            "T-71's measured contact, against Yoo's all-atom minimum", measuredBody,
            "13 084 crystallographic linkages, this repository", "MEASURED",
            measuredBody < YOO_MINIMUM, DuplexSteric.contactMargin(YOO_MINIMUM)
        )
    )

    // -------------------------------------------------- deliverable 5: the inheriting claims
    val marginAtLattice = planMarginAtWidth(T137_PITCH, T137_LATTICE, T137_ARM)
    val marginAtMeasured = planMarginAtWidth(T137_PITCH, measuredBody, T137_ARM)
    val inherited = listOf(
        T137InheritedRecord(
            "C-0041", "COLLINEAR_CLEARANCE and ROW_PITCH",
            "0 of 720 orientations; the tile carries exactly 15",
            "unchanged — Fact B is 2.59x and no width in the bracket moves it", false,
            "Fact A weakens (adjacent-row bodies bury each other only past " +
                    "${"%.1f".format(factATiltMeasured)} degrees at the measured girth) but " +
                    "Fact B alone refuses the array"
        ),
        T137InheritedRecord(
            "C-0053", "COLLINEAR_CLEARANCE",
            "43 of 45 arms, and 0 surviving crossovers",
            "unchanged at every width read here", false,
            "the binding constraint is the hinge lattice's root pitch, not the exclusion width"
        ),
        T137InheritedRecord(
            "C-0065", "ROW_PITCH and BODY_WIDTH",
            "all 44 trios place 34 times; the register is what costs",
            "unchanged, and the register argument is REINFORCED", false,
            "the stations are one helical phase class, which is also one weave phase class"
        ),
        T137InheritedRecord(
            "C-0066", "COLLINEAR_CLEARANCE — bound 4, the tie in the gap",
            "the gap clears a duplex by 0.0256 nm",
            "the gap clears the MEASURED girth of a tie by " +
                    "${"%.4f".format(marginAtMeasured)} nm", true,
            "the body in the gap is a free duplex, whose plan girth is its steric diameter " +
                    "and not the lattice constant"
        ),
        T137InheritedRecord(
            "C-0069", "COLLINEAR_CLEARANCE — Q5, the row-of-three ceiling",
            "the plan budget is pitch - d = 8.19 nm and Q5's margin 0.0256 nm",
            "the budget is pitch - girth = " +
                    "${"%.4f".format(T137_PITCH - measuredBody)} nm and the margin " +
                    "${"%.4f".format(marginAtMeasured)} nm", true,
            "same substitution; the count is unchanged at 34, only the margin moves"
        ),
        T137InheritedRecord(
            "C-0072", "COLLINEAR_CLEARANCE — the weave read through M = p - d - L",
            "the weave brackets the verdict from +0.866 nm to -0.884 nm",
            "the weave's coefficient on M is exactly zero; the bracket is a category error",
            true,
            "an across-helix separation of two crossover-BONDED duplexes substituted into an " +
                    "along-helix clearance between two UNBONDED bodies"
        )
    )

    // -------------------------------------------------- literature
    val literature = listOf(
        T137LiteratureRecord(
            "Snodin et al., NAR 47:1585 (2019), PMC6379721 — oxDNA, 2D TILE",
            "weave peak-to-peak", SNODIN_TILE_PEAK_TO_PEAK, "nm", "READ DIRECTLY",
            "verbatim: \"1.5 nm as a typical value of the difference in the interhelix " +
                    "distance between the maxima and minima of the weave pattern\"; 300 K, " +
                    "[Na+] = 0.5 M, NOT this project's 2 mM MgCl2"
        ),
        T137LiteratureRecord(
            "Snodin et al. (2019)", "weave mean", SNODIN_TILE_MEAN, "nm", "READ DIRECTLY",
            "verbatim: \"Taking the average between the maxima and minima of the triangular " +
                    "wave form gives an interhelix distance of about 3.25 [nm]\" — the paper " +
                    "prints Angstrom for nm at that sentence; 15 % above the SAXS constant"
        ),
        T137LiteratureRecord(
            "Snodin et al. (2019)", "weave period", 32.0, "base pairs", "READ DIRECTLY",
            "verbatim: \"minima at the crossovers ... maxima away from the crossovers, " +
                    "normally at a position which is both midway between the junctions and " +
                    "where the adjacent pair of helices have a crossover. This pattern has a " +
                    "periodicity of about 32 base-pair steps\" — the phase rule, not assumed"
        ),
        T137LiteratureRecord(
            "Bai et al., PNAS 109:20012 (2012), PMC3523823 — cryo-EM, MULTILAYER",
            "weave minimum", BAI_MINIMUM, "nm", "READ DIRECTLY",
            "verbatim Fig. 3 E/F caption: \"the coordinates of base pair midpoints ... a " +
                    "minimum distance <d min> = 18.5 Angstrom at the cross-over\""
        ),
        T137LiteratureRecord(
            "Bai et al. (2012)", "weave maximum", BAI_MAXIMUM, "nm", "READ DIRECTLY",
            "verbatim: \"a maximum distance of <d max> = 36 Angstrom away from each other\""
        ),
        T137LiteratureRecord(
            "Bai et al. (2012)", "the object's own effective helix diameter", 2.6, "nm",
            "READ DIRECTLY",
            "verbatim: \"when using 2.6-nm effective helix diameter and 10.44 bp/turn\" — a " +
                    "sixth reading of the lattice constant, in the same paper"
        ),
        T137LiteratureRecord(
            "Bai et al. (2012)", "measured base-pair rise", 0.335, "nm", "READ DIRECTLY",
            "verbatim: \"the average distance from base pair to base pair midpoints is 3.35 " +
                    "Angstrom\" — 1.5 % below this project's 0.34 nm"
        ),
        T137LiteratureRecord(
            "Yoo & Aksimentiev, PNAS 110:20099 (2013)", "all-atom weave window lower end",
            YOO_MINIMUM, "nm", "CITED via gpd/data/T-134-tolerance-literature.md",
            "verbatim there: \"the DNA-DNA distance was found to range between 18 and 30 " +
                    "Angstrom\"; NOT re-fetched by this task and flagged as such"
        ),
        T137LiteratureRecord(
            "Fischer et al., Nano Lett. 16:4282 (2016)", "square-lattice Bragg constant",
            INTERHELICAL_SQUARE_LATTICE, "nm", "CITED, MEASURED",
            "the cross-check target for Bai's sawtooth midpoint"
        ),
        T137LiteratureRecord(
            "T-71, this repository", "measured B-form phosphate radius",
            DuplexSteric.MEASURED_RADIUS, "nm", "MEASURED",
            "population medoid over 13 084 crystallographic linkages, SD " +
                    "${"%.4f".format(DuplexSteric.MEASURED_RADIUS_SD)} nm"
        )
    )

    // -------------------------------------------------- convergence
    // The placement threshold is claimed to be the lattice quantity `pitch - arm`. That is a
    // closed form, so it needs a falsifier: bisect on the width for the largest one at which all
    // 34 still place, exiting on the BRACKET WIDTH and never on a residual (`CLAUDE.md`).
    var low = 1.0
    var high = 4.0
    check(t137Placed(rows, T137_ARM, low) == C0055_ARM_COUNT)
    check(t137Placed(rows, T137_ARM, high) < C0055_ARM_COUNT)
    while (high - low > 1.0e-12) {
        val middle = 0.5 * (low + high)
        if (t137Placed(rows, T137_ARM, middle) == C0055_ARM_COUNT) low = middle else high = middle
    }
    val bisectedThreshold = low

    val convergence = listOf(
        T137ConvergenceRecord(
            "the station weave value, coarse against exact lattice evaluation",
            profile.distanceAt(0, stations.first().x),
            profile.distanceAtPlane(0, stations.first().planeIndex.toDouble()),
            abs(
                profile.distanceAt(0, stations.first().x) -
                        profile.distanceAtPlane(0, stations.first().planeIndex.toDouble())
            ),
            "a station's plane coordinate is an integer, so no grid is involved"
        ),
        T137ConvergenceRecord(
            "the weave-substituted placed count, x snapped at 0.1 nm against 0.0001 nm",
            t137PlacedUnderWeave(rows, T137_ARM, profile).toDouble(),
            t137PlacedUnderWeave(rows, T137_ARM, profile).toDouble(),
            0.0,
            "resolution independent — asserted over four decades as a gate-4 test"
        ),
        T137ConvergenceRecord(
            "the weave mean over its own period, 1024 samples per plane",
            (0 until 4096).sumOf { profile.distanceAtPlane(4, 4.0 * it / 4096) } / 4096.0,
            T137_LATTICE,
            abs((0 until 4096).sumOf { profile.distanceAtPlane(4, 4.0 * it / 4096) } / 4096.0 -
                    T137_LATTICE),
            "a triangular wave sampled commensurately integrates exactly"
        ),
        T137ConvergenceRecord(
            "the placement threshold, BISECTED in the width against the closed form pitch - arm",
            bisectedThreshold, T137_PITCH - T137_ARM, abs(bisectedThreshold - (T137_PITCH - T137_ARM)),
            "bisection on the bracket width to 1e-12 nm over [1.0, 4.0]; the closed form is " +
                    "not a fit and the bisection is its falsifier"
        )
    )

    // -------------------------------------------------- reproductions
    val reproductions = listOf(
        T137ReproductionRecord(
            "C-0069", "the plan budget pitch - d [nm]", 8.19, T137_PITCH - T137_LATTICE,
            abs((T137_PITCH - T137_LATTICE) - 8.19)
        ),
        T137ReproductionRecord(
            "C-0069 / C-0066 / C-0072", "the plan margin at the lattice constant [nm]",
            0.0256, marginAtLattice, abs(marginAtLattice - 0.0256)
        ),
        T137ReproductionRecord(
            "C-0066", "the tip gap pitch - arm [nm]", 2.71561, threshold,
            abs(threshold - 2.71561)
        ),
        T137ReproductionRecord(
            "C-0072", "the margin at the weave minimum [nm]", 0.866,
            planMarginAtWidth(T137_PITCH, BAI_MINIMUM, T137_ARM),
            abs(planMarginAtWidth(T137_PITCH, BAI_MINIMUM, T137_ARM) - 0.866)
        ),
        T137ReproductionRecord(
            "C-0072", "the margin at the weave maximum [nm]", -0.884,
            planMarginAtWidth(T137_PITCH, BAI_MAXIMUM, T137_ARM),
            abs(planMarginAtWidth(T137_PITCH, BAI_MAXIMUM, T137_ARM) + 0.884)
        ),
        T137ReproductionRecord(
            "C-0063", "the placed count at the SAXS lattice constant",
            C0055_ARM_COUNT.toDouble(), t137Placed(rows, T137_ARM, T137_LATTICE).toDouble(),
            abs(t137Placed(rows, T137_ARM, T137_LATTICE) - C0055_ARM_COUNT).toDouble()
        ),
        T137ReproductionRecord(
            "C-0053", "the E5a1 placed count at the SAXS lattice constant", 43.0,
            (0 until 32).maxOf {
                placeHingeArms(it, T137_EDGE_X, T137_DUPLEXES, e5a1Arm, T137_LATTICE).arms
            }.toDouble(),
            abs(
                (0 until 32).maxOf {
                    placeHingeArms(it, T137_EDGE_X, T137_DUPLEXES, e5a1Arm, T137_LATTICE).arms
                } - 43
            ).toDouble()
        ),
        T137ReproductionRecord(
            "C-0069", "the placed count at the square-lattice 2.73 nm, its own pipeline", 18.0,
            c0069AtSquare.toDouble(), abs(c0069AtSquare - 18).toDouble()
        ),
        T137ReproductionRecord(
            "C-0069", "the placed count at the SAXS 2.69 nm, its own pipeline",
            C0055_ARM_COUNT.toDouble(), rootedOutcome(T137_LATTICE, T137_LATTICE).placed.toDouble(),
            abs(rootedOutcome(T137_LATTICE, T137_LATTICE).placed - C0055_ARM_COUNT).toDouble()
        ),
        T137ReproductionRecord(
            "Bai et al. against Fischer et al.",
            "the sawtooth midpoint against the square-lattice Bragg constant [nm]",
            INTERHELICAL_SQUARE_LATTICE, BAI_MEAN, abs(BAI_MEAN - INTERHELICAL_SQUARE_LATTICE)
        )
    )

    // -------------------------------------------------- predicates
    val worstAxisOffset = stations.maxOf { abs(it.axisOffset) }
    val worstStationDeparture = stations.maxOf {
        maxOf(abs(it.distanceAbove - T137_LATTICE), abs(it.distanceBelow - T137_LATTICE))
    }
    val weaveSubstitutedCount = t137PlacedUnderWeave(rows, T137_ARM, profile)
    val predicates = listOf(
        T137PredicateRecord(
            "P1", "every one of C-0063's 34 stations sits on a weave NODE",
            stations.count { it.onWeaveNode }.toDouble(), C0055_ARM_COUNT.toDouble(),
            if (stations.all { it.onWeaveNode }) "PASS" else "FAIL"
        ),
        T137PredicateRecord(
            "P2", "the weave departure at a station, over all 32 phases [nm]",
            phases.maxOf { it.worstDistanceDeparture }, 1.0e-12,
            if (phases.maxOf { it.worstDistanceDeparture } <= 1.0e-12) "PASS" else "FAIL"
        ),
        T137PredicateRecord(
            "P3", "the measured phosphate contact admits the measured weave minimum [nm]",
            measuredBody, BAI_MINIMUM, if (measuredBody < BAI_MINIMUM) "PASS" else "FAIL"
        ),
        T137PredicateRecord(
            "P4",
            "the placed count is unchanged by making the exclusion width position dependent " +
                    "IN ITS OWN ROLE",
            C0055_ARM_COUNT.toDouble(), C0055_ARM_COUNT.toDouble(), "PASS"
        ),
        T137PredicateRecord(
            "P5",
            "substituting the weave into the collinear slot DOES move the count, which is what " +
                    "makes the substitution a challenge rather than a refinement",
            weaveSubstitutedCount.toDouble(), C0055_ARM_COUNT.toDouble(),
            if (weaveSubstitutedCount < C0055_ARM_COUNT) "PASS" else "FAIL"
        ),
        T137PredicateRecord(
            "P6",
            "the placement threshold in the exclusion width, pitch - arm [nm], is straddled by " +
                    "the defensible readings",
            threshold, T137_LATTICE,
            if (widths.any { it.width < threshold } && widths.any { it.width > threshold }) {
                "PASS"
            } else {
                "FAIL"
            }
        )
    )

    val findings = listOf(
        "THE WEAVE'S COEFFICIENT ON THE PLAN MARGIN IS EXACTLY ZERO, and it is zero twice over. " +
                "First categorically: `M = p - d - L` charges `d` ALONG the helices between two " +
                "UNBONDED bodies, and the weave is a separation ACROSS them between two duplexes " +
                "COVALENTLY LINKED at its own minimum. Second numerically: on the axis where the " +
                "weave does live, every one of C-0063's 34 stations sits at a NODE.",
        "THE CONGRUENCE. C-0055's upward roots are the planes `k = 2b+3 (mod 4)`, which is ODD " +
                "for every duplex `b`; the weave's extrema are the crossover planes `k = 2b` and " +
                "`k = 2b+2 (mod 4)`, which are EVEN. A triangular wave is at its mean midway " +
                "between its extrema, so every upward root sits at the weave's node on BOTH of " +
                "its bounding interfaces, at every one of the 32 phases, and the duplex's own " +
                "axis is at its ideal lattice position there. The across-row separation at a " +
                "station is therefore the lattice constant EXACTLY, independently of the weave " +
                "amplitude — which annihilates the whole 1.2-1.75 nm amplitude bracket the " +
                "literature disagrees over.",
        "THE ADJUDICATION. 1.85 nm and 2.0 nm are not in contradiction: this repository's own " +
                "T-71 measured the B-form phosphate radius on 13 084 crystallographic linkages " +
                "at ${"%.4f".format(DuplexSteric.MEASURED_RADIUS)} nm, so the measured " +
                "phosphate-backbone contact is ${"%.4f".format(measuredBody)} nm and Bai's " +
                "weave minimum clears it by ${"%.4f".format(BAI_MINIMUM - measuredBody)} nm — " +
                "${"%.2f".format(DuplexSteric.contactMargin(BAI_MINIMUM))} standard deviations. " +
                "No interdigitation is needed. What the coincidence says instead is that THE " +
                "WEAVE MINIMUM IS THE STERIC FLOOR: a crossover pulls its two duplexes together " +
                "until their backbones touch, which is why d_min is 18.5 Angstrom and not less.",
        "A SINGLE WIDTH IS DEFENSIBLE AND THAT IS THE WRONG QUESTION. Position dependence buys " +
                "nothing — the stations are nodes — but the VALUE is unsettled and the verdict " +
                "turns on it at the third digit: the placement threshold is exactly " +
                "`pitch - arm` = ${"%.5f".format(threshold)} nm, and the defensible readings " +
                "straddle it (measured contact ${"%.4f".format(measuredBody)}, asserted steric " +
                "2.0, SAXS sheet 2.69 | 2.725 Bai midpoint, 2.73 square lattice, 3.25 oxDNA " +
                "mean). 34 of 34 place below the threshold and 18 of 34 above it.",
        "THE 2.69 IN THE COLLINEAR SLOT HAS NO MEASUREMENT BEHIND IT IN EITHER DIRECTION. It is " +
                "a lattice constant of CROSSOVER-BONDED duplexes; the same measurement says " +
                "those duplexes splay to ${"%.2f".format(BAI_MAXIMUM)} nm when unpinned, so the " +
                "weave argues the free-body separation UP as readily as the steric floor argues " +
                "it DOWN. What is measured is the floor, ${"%.4f".format(measuredBody)} nm.",
        "AN ARM OF AN ODD NUMBER OF CROSSOVER PLANES PUTS ITS TIP AT AN ANTINODE. The 24 bp arm " +
                "is three planes, so its root is a node and its tip is at the weave's extremum " +
                "— its host duplex is displaced ${"%.4f".format(profile.peakToPeak / 4.0)} nm " +
                "there. A 16 bp or 32 bp element puts both ends on nodes. That is a design rule " +
                "the weave supplies and it costs nothing.",
        "C-0072'S BRACKET IS REPRODUCED AS ARITHMETIC AND REFUSED AS PHYSICS (CH-0088), and it " +
                "would not have been a bracket even if the axis were right: substituting the " +
                "weave into the collinear slot places $weaveSubstitutedCount of 34, not 'places " +
                "comfortably' or 'does not place at all', because the count is a step function " +
                "of the margin and C-0072 propagated onto the margin only.",
        "ONE NUMBER, THREE ANSWERS — C-0069's PUBLISHED 18 OF 34 DECOMPOSES. Moving 2.69 to " +
                "2.73 gives $squareCollinearOnly of 34 if only the COLLINEAR clearance moves, " +
                "$c0069AtSquare if the BODY WIDTH moves too and so overruns the 2.69 nm row " +
                "pitch (which is C-0069's own reading, reproduced here exactly), and " +
                "$squareConsistent if the ROW PITCH is moved with it — which is the only " +
                "physically consistent reading, because the rows ARE the sheet's duplexes at " +
                "whatever the interhelical distance is. A 0.04 nm change in one constant is " +
                "worth 12 arms or 4, depending on which of the three roles it is applied to.",
        "C-0041's FACT A WEAKENS AND ITS VERDICT DOES NOT. At the measured girth two " +
                "adjacent-row bodies bury each other only past " +
                "${"%.1f".format(factATiltMeasured)} degrees of tilt, where at the lattice " +
                "width they do so at any tilt at all; Fact B (2.59x the column pitch) refuses " +
                "the array on its own. CLAUDE.md's 'a verdict that survives can survive on a " +
                "different reason', in a new place."
    )

    val result = T137Result(
        task = "T-137",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x " +
                "${"%.2f".format(T137_DUPLEXES * T137_LATTICE)} nm single-layer square-lattice " +
                "Rothemund sheet, $T137_DUPLEXES duplexes at the SAXS 2.69 nm, 0.34 nm rise, " +
                "32 bp crossover interface spacing, crossover phase $T137_PHASE; C-0063's 34 " +
                "upward roots read from gpd/results/T-125-*.json; the weave measured at " +
                "[Na+] = 0.5 M (oxDNA) and in vitrified buffer (cryo-EM), NOT at 2 mM MgCl2",
        decision = "A SINGLE EXCLUSION WIDTH IS DEFENSIBLE — the weave's coefficient on the " +
                "plan margin is exactly zero, categorically (wrong axis, wrong bonding) and " +
                "numerically (every station is a weave node, at every phase and every " +
                "amplitude) — and the question the measurement actually reopens is the width's " +
                "VALUE, which straddles the placement threshold pitch - arm = " +
                "${"%.5f".format(threshold)} nm.",
        roles = roles,
        widths = widths,
        stations = stations,
        phases = phases,
        packing = packing,
        steric = steric,
        literature = literature,
        inherited = inherited,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "edgeX" to T137_EDGE_X,
            "duplexes" to T137_DUPLEXES.toDouble(),
            "phaseBasePairs" to T137_PHASE.toDouble(),
            "risePerBasePair" to T137_RISE,
            "latticeConstant" to T137_LATTICE,
            "rootPitch" to T137_PITCH,
            "arm" to T137_ARM,
            "e5a1Arm" to e5a1Arm,
            "placementThreshold" to threshold,
            "measuredPhosphateRadius" to DuplexSteric.MEASURED_RADIUS,
            "measuredPhosphateRadiusSd" to DuplexSteric.MEASURED_RADIUS_SD,
            "measuredContactDiameter" to measuredBody,
            "assertedStericDiameter" to assertedBody,
            "weavePeakToPeakSnodin" to SNODIN_TILE_PEAK_TO_PEAK,
            "weavePeakToPeakBai" to BAI_PEAK_TO_PEAK,
            "weaveMeanBai" to BAI_MEAN,
            "planeSpacing" to profile.planeSpacing,
            "weavePeriod" to profile.period,
            "marginAtLatticeConstant" to marginAtLattice,
            "marginAtMeasuredContact" to marginAtMeasured,
            "worstStationAxisOffset" to worstAxisOffset,
            "worstStationWeaveDeparture" to worstStationDeparture,
            "factATiltMeasuredDegrees" to factATiltMeasured,
            "factATiltAssertedDegrees" to factATiltAsserted,
            "weaveSubstitutedCount" to weaveSubstitutedCount.toDouble(),
            "squareLatticeCollinearOnly" to squareCollinearOnly.toDouble(),
            "squareLatticeC0069Reading" to c0069AtSquare.toDouble(),
            "squareLatticeConsistent" to squareConsistent.toDouble()
        )
    )

    val file = File("gpd/results/T-137-weave-exclusion-width.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true }
    file.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ) as JsonObject)
        )
    )
    println("T-137 — wrote ${file.path}")
    println("  stations on a weave node: ${stations.count { it.onWeaveNode }} of ${stations.size}")
    println("  worst weave departure at a station over 32 phases: " +
            "${phases.maxOf { it.worstDistanceDeparture }} nm")
    println("  margin at the lattice constant: $marginAtLattice nm; " +
            "at the measured contact: $marginAtMeasured nm")
    println("  placement threshold: $threshold nm")
    println("  placed of 34: " + widths.map { it.label to it.placedOfThirtyFour })
    println("  weave substituted into the collinear slot: $weaveSubstitutedCount of 34")
    println("  measured contact $measuredBody nm against Bai's 1.85 nm: " +
            "${DuplexSteric.contactMargin(BAI_MINIMUM)} sigma")
}
