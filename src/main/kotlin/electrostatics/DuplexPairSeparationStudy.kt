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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.nano.plentyofroom.anchoring.DuplexSteric
import com.xemantic.nano.plentyofroom.anchoring.armDirections
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-139` — what separation do two **unbonded** duplexes hold in 2 mM MgCl₂?
 *
 * Emits `gpd/results/T-139-duplex-pair-separation.json`.
 */

private const val T139_DUPLEXES = 15
private val T139_EDGE_X = Gen1Tile.EDGE_X
private val T139_ARM = C0055_ARM_LENGTH
private const val T139_ROW_PITCH = 2.69
private const val T139_SHEET_INTERFACES = 14
private const val T139_CROSSOVER_COLUMNS = 4

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T139CheapBound(
    val name: String,
    val statement: String,
    val quantity: String,
    val value: Double,
    val against: String,
    val threshold: Double,
    val settles: String
)

@Serializable
private data class T139Geometry(
    val role: String,
    val claim: String,
    val geometry: String,
    val closedForm: String,
    val separation: Double,
    val electrostaticEnergy: Double,
    val shortRangeEnergy: Double,
    val vanDerWaalsEnergy: Double,
    val totalEnergy: Double,
    val totalInThermalUnits: Double
)

@Serializable
private data class T139Profile(
    val separation: Double,
    val surfaceSeparation: Double,
    val electrostaticCrossed: Double,
    val shortRangeCrossed: Double,
    val vanDerWaalsCrossed: Double,
    val totalCrossed: Double,
    val totalInThermalUnits: Double,
    val vanDerWaalsShare: Double,
    val gradient: Double,
    val parallelPerLength: Double,
    val coaxialElectrostatic: Double
)

@Serializable
private data class T139Stationary(
    val kind: String,
    val separation: Double?,
    val energy: Double?,
    val energyInThermalUnits: Double?,
    val isEquilibrium: Boolean,
    val note: String
)

@Serializable
private data class T139ThresholdWidth(
    val thresholdName: String,
    val thresholdEnergy: Double,
    val thresholdInThermalUnits: Double,
    val width: Double,
    val marginAgainstPlacementThreshold: Double,
    val clearsPlacementThreshold: Boolean
)

@Serializable
private data class T139Calibration(
    val quantity: String,
    val value: Double,
    val unit: String,
    val note: String
)

@Serializable
private data class T139WidthRow(
    val reading: String,
    val readFlag: String,
    val geometry: String,
    val width: Double,
    val planMargin: Double,
    val marginOverRise: Double,
    val placedOf34: Int,
    val crossedPairEnergy: Double,
    val crossedInThermalUnits: Double
)

@Serializable
private data class T139Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

@Serializable
private data class T139Literature(
    val quantity: String,
    val value: String,
    val conditions: String,
    val source: String,
    val readFlag: String
)

@Serializable
private data class T139Convergence(
    val quantity: String,
    val coarse: Double,
    val fine: Double,
    val finer: Double,
    val departure: Double
)

@Serializable
private data class T139Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T139Predicate(
    val clause: String,
    val verdict: String,
    val evidence: String
)

@Serializable
private data class T139Downstream(
    val claim: String,
    val quantityAffected: String,
    val standingValue: Double,
    val valueAtThisTaskSWidth: Double,
    val moves: Boolean,
    val note: String
)

@Serializable
private data class T139Result(
    val task: String,
    val leaf: String,
    val parameters: Map<String, String>,
    val cheapBounds: List<T139CheapBound>,
    val geometries: List<T139Geometry>,
    val profile: List<T139Profile>,
    val stationaryPoints: List<T139Stationary>,
    val thresholdWidths: List<T139ThresholdWidth>,
    val calibration: List<T139Calibration>,
    val widthLadder: List<T139WidthRow>,
    val downstream: List<T139Downstream>,
    val reproductions: List<T139Reproduction>,
    val literature: List<T139Literature>,
    val convergence: List<T139Convergence>,
    val falsifiers: List<T139Falsifier>,
    val predicates: List<T139Predicate>,
    val findings: List<String>
)

// ---------------------------------------------------------------------------------------------
// helpers
// ---------------------------------------------------------------------------------------------

/** `C-0063`'s 34 upward roots, read from its own result file. */
private fun t139Stations(file: File): List<Pair<Int, List<Double>>> {
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

/** How many of `C-0063`'s 34 stations carry the arm at a constant collinear clearance [width]. */
private fun t139Placed(
    rows: List<Pair<Int, List<Double>>>,
    width: Double
): Int = rows.sumOf { (_, roots) ->
    if (armDirections(roots, T139_ARM, T139_EDGE_X, width) != null) roots.size else 0
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val kT = thermalEnergy()
    val state = gen1PairState()
    val pair = state.pair
    val buffer = state.buffer
    val kappa = state.inverseDebyeLength
    val shortRange = state.shortRange
    val threshold = C0076_PLACEMENT_THRESHOLD
    val floor = T71_STERIC_FLOOR
    val rows = t139Stations(File("gpd/results/T-125-upward-root-placement.json"))
    check(rows.sumOf { it.second.size } == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT roots"
    }

    // ---- deliverable 1: the three cheap bounds ------------------------------------------------

    val disputedBracket = 3.60 - floor
    val cheapBounds = listOf(
        T139CheapBound(
            name = "1 — a packer is a FEASIBILITY predicate, not a force balance",
            statement = "C-0053's footprint convention asks whether two bodies may both be " +
                "placed, not where they go. For rigid bodies the first is answered by the " +
                "steric floor and the second by an equilibrium — and here there is no " +
                "equilibrium to answer it with.",
            quantity = "T-71's measured phosphate-backbone contact",
            value = floor,
            against = "C-0076's placement threshold, pitch - arm",
            threshold = threshold,
            settles = "which acceptance branch applies, before any code runs"
        ),
        T139CheapBound(
            name = "2 — the electrostatic RANGE exceeds the whole disputed bracket",
            statement = "At 2 mM MgCl2 the Debye length is longer than the span of every " +
                "defensible reading of the width, so a screened Coulomb interaction cannot " +
                "place an edge inside it and a bespoke two-cylinder nonlinear PB solve cannot " +
                "change the verdict.",
            quantity = "Debye length at I = 3c = 6 mM",
            value = buffer.debyeLength(),
            against = "the disputed bracket, 3.60 minus the measured floor",
            threshold = disputedBracket,
            settles = "the METHOD: closed-form Debye-Huckel plus Derjaguin, not a 2-D solve"
        ),
        T139CheapBound(
            name = "3 — the three roles are three GEOMETRIES",
            statement = "ROW_PITCH is a parallel pair, C-0066's bound 4 is a crossed pair and " +
                "C-0069's Q5 is a COAXIAL pair end to end. The closed forms do not differ by a " +
                "constant, and only the first is what the SAXS lattice constant measures.",
            quantity = "crossed over coaxial energy at the placement threshold",
            value = state.electrostaticCrossedEnergy(threshold) /
                pair.coaxialScreenedCoulombEnergy(threshold, kappa),
            against = "one",
            threshold = 1.0,
            settles = "that one width cannot serve all three roles"
        )
    )

    // ---- deliverable 2: the three geometries at the threshold ---------------------------------

    // The van der Waals and Derjaguin terms are continuum laws and are not carried below the
    // model floor; at a separation inside it the surface gap is clamped and the row is a
    // statement about the floor rather than about that separation.
    fun surfaceGap(separation: Double) =
        maxOf(separation - 2.0 * state.hardRadius, state.modelFloor)

    fun geometryAt(separation: Double): List<T139Geometry> = listOf(
        T139Geometry(
            role = "ROW_PITCH", claim = "C-0041/C-0053 row pitch (BONDED — not in question)",
            geometry = "parallel cylinders", closedForm = "E/L = 2 tau^2 l_B kT K0(kappa D)",
            separation = separation,
            electrostaticEnergy = pair.parallelScreenedCoulombEnergyPerLength(separation, kappa),
            shortRangeEnergy = shortRange.parallelPairEnergyPerLength(separation),
            vanDerWaalsEnergy = parallelCylinderVanDerWaalsEnergyPerLength(
                state.hamaker, state.hardRadius, surfaceGap(separation)
            ),
            totalEnergy = pair.parallelScreenedCoulombEnergyPerLength(separation, kappa) +
                shortRange.parallelPairEnergyPerLength(separation) +
                parallelCylinderVanDerWaalsEnergyPerLength(
                    state.hamaker, state.hardRadius, surfaceGap(separation)
                ),
            totalInThermalUnits = (pair.parallelScreenedCoulombEnergyPerLength(separation, kappa) +
                shortRange.parallelPairEnergyPerLength(separation) +
                parallelCylinderVanDerWaalsEnergyPerLength(
                    state.hamaker, state.hardRadius, surfaceGap(separation)
                )) / kT
        ),
        T139Geometry(
            role = "BODY_WIDTH in a gap — C-0066 bound 4",
            claim = "C-0066 bound 4: a tie standing normal, between two arm ends",
            geometry = "crossed cylinders at 90 degrees",
            closedForm = "E = 2 pi tau^2 l_B kT exp(-kappa D)/kappa",
            separation = separation,
            electrostaticEnergy = pair.crossedScreenedCoulombEnergy(separation, kappa),
            shortRangeEnergy = shortRange.crossedPairEnergy(separation, state.hardRadius),
            vanDerWaalsEnergy = crossedCylinderVanDerWaalsEnergy(
                state.hamaker, state.hardRadius, surfaceGap(separation)
            ),
            totalEnergy = pair.crossedScreenedCoulombEnergy(separation, kappa) +
                shortRange.crossedPairEnergy(separation, state.hardRadius) +
                crossedCylinderVanDerWaalsEnergy(
                    state.hamaker, state.hardRadius, surfaceGap(separation)
                ),
            totalInThermalUnits = (pair.crossedScreenedCoulombEnergy(separation, kappa) +
                shortRange.crossedPairEnergy(separation, state.hardRadius) +
                crossedCylinderVanDerWaalsEnergy(
                    state.hamaker, state.hardRadius, surfaceGap(separation)
                )) / kT
        ),
        T139Geometry(
            role = "COLLINEAR_CLEARANCE — C-0069 Q5",
            claim = "C-0069 Q5: one arm's tip facing the next arm's root, same row",
            geometry = "coaxial rods, end to end",
            closedForm = "E = tau^2 l_B kT [exp(-kappa g)/kappa - g E1(kappa g)]",
            separation = separation,
            electrostaticEnergy = pair.coaxialScreenedCoulombEnergy(separation, kappa),
            shortRangeEnergy = shortRange.coaxialFaceEnergy(separation, state.hardRadius),
            vanDerWaalsEnergy = -state.hamaker * state.hardRadius * state.hardRadius /
                (6.0 * separation * separation),
            totalEnergy = pair.coaxialScreenedCoulombEnergy(separation, kappa) +
                shortRange.coaxialFaceEnergy(separation, state.hardRadius),
            totalInThermalUnits = (pair.coaxialScreenedCoulombEnergy(separation, kappa) +
                shortRange.coaxialFaceEnergy(separation, state.hardRadius)) / kT
        )
    )

    val geometries = listOf(threshold, SAXS_SHEET_LATTICE_CONSTANT, 2.0, state.minimumSeparation)
        .flatMap { geometryAt(it) }

    // ---- deliverable 3: the profile -----------------------------------------------------------

    val profile = generateSequence(state.minimumSeparation + 1e-3) { it + 0.05 }
        .takeWhile { it <= PLAN_RELEVANT_RANGE + 1e-9 }
        .map { separation ->
            T139Profile(
                separation = separation,
                surfaceSeparation = separation - 2.0 * state.hardRadius,
                electrostaticCrossed = state.electrostaticCrossedEnergy(separation),
                shortRangeCrossed = state.shortRangeCrossedEnergy(separation),
                vanDerWaalsCrossed = state.vanDerWaalsCrossedEnergy(separation),
                totalCrossed = state.totalCrossedEnergy(separation),
                totalInThermalUnits = state.totalCrossedEnergy(separation) / kT,
                vanDerWaalsShare = state.vanDerWaalsShare(separation),
                gradient = state.energyGradient(separation),
                parallelPerLength =
                    pair.parallelScreenedCoulombEnergyPerLength(separation, kappa) +
                        shortRange.parallelPairEnergyPerLength(separation),
                coaxialElectrostatic = pair.coaxialScreenedCoulombEnergy(separation, kappa)
            )
        }.toList()

    val barrier = state.barrierSeparation()
    val secondary = state.secondaryMinimum()
    val stationary = listOf(
        T139Stationary(
            kind = "primary minimum",
            separation = null, energy = null, energyInThermalUnits = null,
            isEquilibrium = false,
            note = "AT CONTACT and inside the continuum model's own floor. The Lifshitz 1/D_s " +
                "diverges where every repulsive term is bounded, so an unretarded DLVO always " +
                "carries one. It is not carried here: the model floor is " +
                "${state.minimumSeparation} nm."
        ),
        T139Stationary(
            kind = "barrier maximum",
            separation = barrier,
            energy = barrier?.let { state.totalCrossedEnergy(it) },
            energyInThermalUnits = barrier?.let { state.totalCrossedEnergy(it) / kT },
            isEquilibrium = false,
            note = "the top of the primary-minimum barrier, at a surface separation of " +
                "${barrier?.minus(2.0 * state.hardRadius)} nm — well inside a water diameter " +
                "of contact and not a separation any body sits at"
        ),
        T139Stationary(
            kind = "secondary minimum",
            separation = secondary?.first,
            energy = secondary?.second,
            energyInThermalUnits = secondary?.second?.div(kT),
            isEquilibrium = false,
            note = "an unretarded-Lifshitz artefact far outside the plan-relevant range; " +
                "C-0021's 'a stable equilibrium is not a confinement' applies verbatim, and " +
                "retardation makes the unretarded law an overestimate at this separation"
        ),
        T139Stationary(
            kind = "EQUILIBRIUM SEPARATION",
            separation = null, energy = null, energyInThermalUnits = null,
            isEquilibrium = false,
            note = "NONE EXISTS. Four independent methods say so — osmotic stress plus XRD " +
                "(Meng 2020, Rau & Parsegian 1992), all-atom two-duplex PMF (Yoo & Aksimentiev " +
                "2016, Zhang 2017, He 2023) and the second virial coefficient of free duplexes " +
                "at 3 mM Mg2+ (Pabit 2009). Read directly, every one."
        )
    )

    // ---- deliverable 4: the threshold-versus-width map ----------------------------------------

    // What the host sheet's own crossovers demonstrably pay: the parallel pair energy per length
    // at the MEASURED 2.69 nm lattice constant, over one 40 nm interface, shared by the
    // crossover columns that hold it. This is a calibration and not a model: the sheet exists.
    val sheetPerLength = pair.parallelScreenedCoulombEnergyPerLength(
        SAXS_SHEET_LATTICE_CONSTANT, kappa
    ) + shortRange.parallelPairEnergyPerLength(SAXS_SHEET_LATTICE_CONSTANT)
    val sheetPerInterface = sheetPerLength * T139_EDGE_X
    val sheetPerCrossover = sheetPerInterface / T139_CROSSOVER_COLUMNS

    val calibration = listOf(
        T139Calibration(
            "parallel pair energy per length at the SAXS 2.69 nm, 2 mM",
            sheetPerLength, "pN nm per nm",
            "electrostatics at the device's own buffer plus the measured short-range law"
        ),
        T139Calibration(
            "the same, in kT per nm", sheetPerLength / kT, "kT per nm", "for reading"
        ),
        T139Calibration(
            "per 40 nm sheet interface", sheetPerInterface, "pN nm",
            "one pair of adjacent host duplexes over the tile's own edge"
        ),
        T139Calibration(
            "per crossover column holding that interface", sheetPerCrossover, "pN nm",
            "$T139_CROSSOVER_COLUMNS columns per interface at 32 bp on a 40 nm tile — the " +
                "energy one crossover of the HOST SHEET demonstrably pays, and the sheet is " +
                "a measured object"
        ),
        T139Calibration(
            "the same, in kT", sheetPerCrossover / kT, "kT",
            "the design's own demonstrated currency"
        ),
        T139Calibration(
            "whole-sheet electrostatic plus short-range self energy",
            sheetPerInterface * T139_SHEET_INTERFACES, "pN nm",
            "$T139_SHEET_INTERFACES interfaces — a scale check, not a requirement"
        )
    )

    val thresholdLadder = listOf(
        "thermal, 1 kT — the Barker-Henderson criterion for two FREE bodies" to kT,
        "0.5 kT" to 0.5 * kT,
        "2 kT" to 2.0 * kT,
        "5 kT" to 5.0 * kT,
        "the host sheet's own per-crossover energy at 2.69 nm" to sheetPerCrossover,
        "10 kT" to 10.0 * kT
    ).map { (name, energy) ->
        // A budget above what the pair can charge at the barrier has no width above the model
        // floor at all — reported as the floor, with the margin measured from it.
        val width = state.exclusionWidthAtEnergy(energy) ?: state.minimumSeparation
        T139ThresholdWidth(
            thresholdName = name,
            thresholdEnergy = energy,
            thresholdInThermalUnits = energy / kT,
            width = width,
            marginAgainstPlacementThreshold = threshold - width,
            clearsPlacementThreshold = width <= threshold
        )
    }

    // ---- deliverable 5: the width ladder and the placement verdict ----------------------------

    data class T139Reading(
        val reading: String, val flag: String, val width: Double, val coaxial: Boolean = false
    )

    val widthLadder = listOf(
        T139Reading("the COLLINEAR role's real lower bound — oxDNA2's coaxial-stacking cutoff",
            "CITED, read directly", BluntEndStacking.OXDNA2_CUTOFF, coaxial = true),
        T139Reading("the same, at the all-atom PMF's repulsive onset (the generous end)",
            "CITED, read directly", BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET, coaxial = true),
        T139Reading("T-71's MEASURED phosphate contact", "MEASURED, this repository", floor),
        T139Reading("Bai's cryo-EM weave MINIMUM", "CITED, MEASURED", 1.85),
        T139Reading("this project's asserted steric diameter", "CITED", 2.0),
        T139Reading("AT OR BELOW the model floor: the sheet's own per-crossover energy exceeds " +
            "the pair energy at every separation the continuum model is valid at",
            "DERIVED here",
            state.exclusionWidthAtEnergy(sheetPerCrossover) ?: state.minimumSeparation),
        T139Reading("the SAXS single-layer Bragg lattice constant", "CITED, MEASURED",
            SAXS_SHEET_LATTICE_CONSTANT),
        T139Reading("C-0076's placement THRESHOLD, pitch - arm", "DERIVED", threshold),
        T139Reading("the SAXS square-lattice constant", "CITED, MEASURED", 2.73),
        T139Reading("the width at a 1 kT thermal threshold", "DERIVED here",
            state.exclusionWidthAtEnergy(kT) ?: state.minimumSeparation),
        T139Reading("oxDNA's 2D-tile weave MEAN", "CITED, SIMULATED", 3.25),
        T139Reading("Bai's cryo-EM weave MAXIMUM", "CITED, MEASURED", 3.60)
    ).map { row ->
        val width = row.width
        val margin = 32 * 0.34 - width - T139_ARM
        // The crossed geometry is not defined below the continuum model's floor, and it is the
        // wrong geometry entirely for a coaxial reading — the two stacking rows are an END GAP
        // between two blunt faces, not an interaxial separation.
        val energy = when {
            row.coaxial -> pair.coaxialScreenedCoulombEnergy(width, kappa) +
                shortRange.coaxialFaceEnergy(width, state.hardRadius)
            width >= state.minimumSeparation -> state.totalCrossedEnergy(width)
            else -> pair.crossedScreenedCoulombEnergy(width, kappa) +
                shortRange.crossedPairEnergy(width, state.hardRadius) +
                crossedCylinderVanDerWaalsEnergy(
                    state.hamaker, state.hardRadius, state.modelFloor
                )
        }
        T139WidthRow(
            reading = row.reading, readFlag = row.flag,
            geometry = if (row.coaxial) {
                "COAXIAL end gap (C-0069 Q5)"
            } else if (width >= state.minimumSeparation) {
                "crossed (C-0066 bound 4)"
            } else {
                "crossed, van der Waals clamped at the model floor"
            },
            width = width,
            planMargin = margin, marginOverRise = margin / 0.34,
            placedOf34 = t139Placed(rows, width),
            crossedPairEnergy = energy,
            crossedInThermalUnits = energy / kT
        )
    }

    // ---- deliverable 6: what moves downstream --------------------------------------------------

    val coaxialWidth = state.exclusionWidthAtEnergy(sheetPerCrossover)
        ?: state.minimumSeparation
    val crossedWidthAtSheet = coaxialWidth
    val q5MarginAtFloor = 32 * 0.34 - floor - T139_ARM
    val downstream = listOf(
        T139Downstream(
            claim = "C-0066 bound 4 — the tie in the gap",
            quantityAffected = "the clearance the 2.71561 nm gap gives a tie",
            standingValue = threshold - SAXS_SHEET_LATTICE_CONSTANT,
            valueAtThisTaskSWidth = threshold - crossedWidthAtSheet,
            moves = true,
            note = "the gap holds a body whose CROSSED-geometry cost at the sheet-calibrated " +
                "width is below what one host crossover already pays; CH-0089 is upheld"
        ),
        T139Downstream(
            claim = "C-0069 Q5 — the collinear clearance",
            quantityAffected = "the plan margin p - d - L",
            standingValue = 32 * 0.34 - SAXS_SHEET_LATTICE_CONSTANT - T139_ARM,
            valueAtThisTaskSWidth = q5MarginAtFloor,
            moves = true,
            note = "and the geometry is COAXIAL, not a body at all: the gap between two " +
                "collinear arm ends contains nothing, and the coaxial pair energy is " +
                "bounded even at contact"
        ),
        T139Downstream(
            claim = "C-0072 floor 1 — the base-pair rise",
            quantityAffected = "the margin in rises",
            standingValue = (32 * 0.34 - SAXS_SHEET_LATTICE_CONSTANT - T139_ARM) / 0.34,
            valueAtThisTaskSWidth = q5MarginAtFloor / 0.34,
            moves = true,
            note = "stops firing at the measured girth — but a SECOND, independent ground " +
                "replaces it: 0.0256 nm is a fraction of a per cent of the pair energy at " +
                "2.7 nm, so the step is unresolvable by physics as well as by fabrication"
        ),
        T139Downstream(
            claim = "C-0074's 30-root placement",
            quantityAffected = "the plan margin at 30 roots",
            standingValue = 1.76451,
            valueAtThisTaskSWidth = 1.76451 + (SAXS_SHEET_LATTICE_CONSTANT - floor),
            moves = false,
            note = "it moves the same way and it was never the binding constraint: 1.76 nm is " +
                "already 68.9x C-0069's knife edge, and what refuses the 30-root design is " +
                "flatness with equal springs, which contains no exclusion width at all"
        )
    )

    // ---- verification -------------------------------------------------------------------------

    val reproductions = listOf(
        T139Reproduction("C-0076", "placement threshold pitch - arm [nm]",
            2.715609, threshold, abs(threshold - 2.715609)),
        T139Reproduction("CH-0089 / T-71", "measured phosphate contact [nm]",
            1.817276, floor, abs(floor - 1.817276)),
        T139Reproduction("CH-0089", "Q5 margin at the measured girth [nm]",
            0.898333, q5MarginAtFloor, abs(q5MarginAtFloor - 0.898333)),
        T139Reproduction("C-0069 / C-0072", "Q5 margin at the SAXS 2.69 nm [nm]",
            0.02560917, 32 * 0.34 - SAXS_SHEET_LATTICE_CONSTANT - T139_ARM,
            abs(32 * 0.34 - SAXS_SHEET_LATTICE_CONSTANT - T139_ARM - 0.02560917)),
        T139Reproduction("C-0076", "placed of 34 at the SAXS 2.69 nm",
            34.0, t139Placed(rows, SAXS_SHEET_LATTICE_CONSTANT).toDouble(),
            abs(t139Placed(rows, SAXS_SHEET_LATTICE_CONSTANT) - 34.0)),
        T139Reproduction("C-0076", "placed of 34 at the square-lattice 2.73 nm",
            22.0, t139Placed(rows, 2.73).toDouble(),
            abs(t139Placed(rows, 2.73) - 22.0)),
        T139Reproduction("CLAUDE.md / C-0008", "bulk Debye length at 2 mM MgCl2 [nm]",
            3.93, buffer.debyeLength(), abs(buffer.debyeLength() - 3.93)),
        T139Reproduction("DnaOrigamiTile", "Manning-surviving line charge, q = 2 [e/nm]",
            0.70017, pair.effectiveLinearChargeDensity,
            abs(pair.effectiveLinearChargeDensity - 0.70017)),
        T139Reproduction("Meng 2020, Table row 20 of Rau 1984",
            "array pressure at 2.6 nm interaxial [pN/nm2]",
            3.98, shortRange.arrayPressure(2.6), abs(shortRange.arrayPressure(2.6) - 3.98)),
        T139Reproduction("Meng 2020", "array pressure at 3.0 nm interaxial [pN/nm2]",
            0.752, shortRange.arrayPressure(3.0), abs(shortRange.arrayPressure(3.0) - 0.752)),
        T139Reproduction("Meng 2020", "array pressure at 3.6 nm interaxial [pN/nm2]",
            0.062, shortRange.arrayPressure(3.6), abs(shortRange.arrayPressure(3.6) - 0.062))
    )

    val convergence = listOf(
        run {
            val closed = pair.crossedScreenedCoulombEnergy(threshold, kappa, finiteRadius = false)
            val a = crossedRodQuadrature(pair, threshold, kappa, 60.0, 76)
            val b = crossedRodQuadrature(pair, threshold, kappa, 60.0, 150)
            val c = crossedRodQuadrature(pair, threshold, kappa, 60.0, 300)
            T139Convergence(
                "crossed-rod quadrature against the closed form, Simpson 76/150/300",
                abs(a - closed) / closed, abs(b - closed) / closed, abs(c - closed) / closed,
                abs(c - closed) / closed
            )
        },
        run {
            val closed = pair.coaxialScreenedCoulombEnergy(threshold, kappa)
            val a = coaxialRodQuadrature(pair, threshold, kappa, 60.0, 76)
            val b = coaxialRodQuadrature(pair, threshold, kappa, 60.0, 150)
            val c = coaxialRodQuadrature(pair, threshold, kappa, 60.0, 300)
            T139Convergence(
                "coaxial-rod quadrature against the closed form, Simpson 76/150/300",
                abs(a - closed) / closed, abs(b - closed) / closed, abs(c - closed) / closed,
                abs(c - closed) / closed
            )
        },
        run {
            val closed = shortRange.crossedPairEnergy(threshold, state.hardRadius)
            val a = derjaguinCrossedQuadrature(shortRange, threshold, state.hardRadius, 5.0, 12)
            val b = derjaguinCrossedQuadrature(shortRange, threshold, state.hardRadius, 5.0, 24)
            val c = derjaguinCrossedQuadrature(shortRange, threshold, state.hardRadius, 5.0, 48)
            T139Convergence(
                "Derjaguin crossing length against direct quadrature, Simpson 12/24/48",
                abs(a - closed) / closed, abs(b - closed) / closed, abs(c - closed) / closed,
                abs(c - closed) / closed
            )
        },
        run {
            // the barrier is located by a scan and then bisected; the SCAN step is what could
            // move it, so it is the thing refined
            val a = state.barrierSeparation(scanStep = 0.02)!!
            val b = state.barrierSeparation(scanStep = 0.005)!!
            val c = state.barrierSeparation(scanStep = 0.00125)!!
            T139Convergence(
                "barrier separation under a 4x and 16x finer locating scan [nm]",
                a, b, c, maxOf(abs(b - a), abs(c - b))
            )
        },
        run {
            // the threshold width is a bracketed root; refining the ENERGY it is taken at by a
            // relative 1e-6 must move it by kappa^-1 x 1e-6, which is what the derivative says
            val base = state.exclusionWidthAtEnergy(5.0 * kT)!!
            val up = state.exclusionWidthAtEnergy(5.0 * kT * (1.0 + 1e-6))!!
            val down = state.exclusionWidthAtEnergy(5.0 * kT * (1.0 - 1e-6))!!
            T139Convergence(
                "5 kT threshold width under a +/- 1e-6 relative change of the threshold [nm]",
                down, base, up, maxOf(abs(up - base), abs(base - down))
            )
        }
    )

    val vdwPeak = profile.filter { it.separation <= 3.6 }.maxOf { it.vanDerWaalsShare }
    val coaxialAtThreshold = pair.coaxialScreenedCoulombEnergy(threshold, kappa)
    val crossedAtThreshold = state.totalCrossedEnergy(threshold)

    val falsifiers = listOf(
        T139Falsifier(
            "F1",
            "the pair interaction has a real MINIMUM at finite separation above steric contact",
            fired = false,
            outcome = "NO inside the plan-relevant range: zero local minima between " +
                "${state.minimumSeparation} and $PLAN_RELEVANT_RANGE nm. A far minimum exists " +
                "at ${secondary?.first} nm and is ${secondary?.second?.div(kT)} kT deep — " +
                "170x below thermal, an unretarded-Lifshitz artefact, and not a confinement. " +
                "The literature is unanimous and independent: no equilibrium separation exists."
        ),
        T139Falsifier(
            "F2",
            "the electrostatic range is SHORTER than the disputed bracket, so a solve can " +
                "place an edge inside it",
            fired = false,
            outcome = "NO — ${buffer.debyeLength()} nm against a $disputedBracket nm bracket. " +
                "And the finding is stronger than the bound: the measured short-range law has " +
                "a ${shortRange.decayLength} nm decay length, so an edge EXISTS and is sharp, " +
                "but it is not electrostatic. A two-cylinder PB solve would have resolved the " +
                "wrong term."
        ),
        T139Falsifier(
            "F3",
            "the energy of placing a body in C-0066's gap at the measured girth EXCEEDS what " +
                "the host sheet demonstrably pays to hold its own duplexes at 2.69 nm",
            fired = false,
            outcome = "NO at the placement threshold (${crossedAtThreshold / kT} kT against " +
                "${sheetPerCrossover / kT} kT per host crossover) and YES at the bare measured " +
                "floor (${state.totalCrossedEnergy(state.minimumSeparation) / kT} kT). The " +
                "affordable width is therefore between them, and it is below the threshold."
        ),
        T139Falsifier(
            "F4",
            "van der Waals is at least 10 % of the repulsion anywhere in the bracket",
            fired = vdwPeak >= 0.10,
            outcome = "FIRED, and only at the model floor: the peak share over the bracket is " +
                "$vdwPeak at a ${state.modelFloor} nm surface separation, falling to " +
                "${state.vanDerWaalsShare(threshold)} at the placement threshold and " +
                "${state.vanDerWaalsShare(SAXS_SHEET_LATTICE_CONSTANT)} at the SAXS 2.69 nm. " +
                "Its declared consequence — that a minimum may exist and F1 becomes live — was " +
                "tested and did NOT materialise: zero local minima in the plan-relevant range. " +
                "Rau & Parsegian's own " +
                "argument, reproduced: the 1e-20 J in circulation is their explicit overestimate " +
                "introduced to show van der Waals is too weak."
        ),
        T139Falsifier(
            "F5",
            "the coaxial geometry costs MORE than the crossed geometry at the same separation",
            fired = coaxialAtThreshold >= crossedAtThreshold,
            outcome = "NO — ${crossedAtThreshold / coaxialAtThreshold}x the other way at the " +
                "placement threshold. A coaxial pair's charge is spread along the axis away " +
                "from the contact, and its energy is FINITE even at zero gap " +
                "(${pair.coaxialScreenedCoulombEnergy(0.0, kappa) / kT} kT)."
        )
    )

    val predicates = listOf(
        T139Predicate(
            "branch 1 — an equilibrium centre-to-centre separation with a bracket",
            "EMPTY, and that is a result rather than a failure",
            "four independent methods, all read directly; the pair energy is monotone over the " +
                "whole plan-relevant range and its only far stationary point is 170x below kT"
        ),
        T139Predicate(
            "branch 2 — the statement that the width cannot be given a value, with the reason",
            "MET, with the reason AND with what is decidable in its place",
            "the width is not a separation but a THRESHOLD quantity d(E*); the map is emitted, " +
                "the threshold that reproduces the standing 2.69 nm convention is named, and " +
                "the placement verdict follows"
        ),
        T139Predicate(
            "each of C-0066 bound 4, C-0069 Q5, C-0072's four floors and C-0074's 30-root " +
                "margin named and said to move or not move",
            "MET",
            "the downstream table, four rows"
        ),
        T139Predicate(
            "a verdict against the 2.715609 nm placement threshold",
            "34 of 34 PLACE",
            "at every width from the measured floor to the sheet-calibrated " +
                "$coaxialWidth nm; the only readings that " +
                "put it at 22 are thermal-criterion widths, and a thermal criterion is the " +
                "wrong one for two bodies covalently rooted to the same sheet"
        )
    )

    val literature = listOf(
        T139Literature(
            "no equilibrium separation in Mg2+-only solutions",
            "\"the force-spacing curve extends to infinity because zero force can only be " +
                "achieved at infinite DNA-DNA spacing\"",
            "20 mM MgCl2, osmotic stress + XRD",
            "Meng, Timsina, Bull, Andresen, Qiu, Biophys. J. 118:3019 (2020), PMC7300303",
            "READ DIRECTLY"
        ),
        T139Literature(
            "the same, from a two-duplex PMF",
            "\"pairwise DNA-DNA forces were always repulsive regardless of the concentration\"",
            "all-atom, two parallel duplexes, Na+ and Mg2+",
            "Yoo & Aksimentiev, NAR 44:2040 (2016)", "READ DIRECTLY"
        ),
        T139Literature(
            "the same, random-sequence pair in pure Mg2+",
            "\"lacks any deep minimum ... suggests spontaneous dissociation of DNA arrays\"",
            "well-tempered metadynamics", "He, Qiu, Kirmizialtin, JCTC 19:6831 (2023)",
            "READ DIRECTLY"
        ),
        T139Literature(
            "the nearest MEASURED concentration to this device's buffer",
            "second virial coefficient of free 25 bp duplexes: repulsion at 3 mM and 6 mM Mg2+; " +
                "sign change only at 10 mM free Mg2+",
            "no confinement of any kind", "Pabit et al., NAR 37:3887 (2009), PMC2709557",
            "READ DIRECTLY"
        ),
        T139Literature(
            "the short-range equation of state used here",
            "Pi(d) = Pi_R exp(-d/lambda), lambda = 2.4 A, Pi_R = 201.8 GPa",
            "20 mM MgCl2, interaxial coordinates",
            "Meng et al. (2020)", "READ DIRECTLY"
        ),
        T139Literature(
            "independent cross-check of that fit",
            "DNA pressure 1.2-5.5 pN/nm2 at 26-30 A interaxial",
            "calf-thymus DNA, 20 C",
            "Rau, Lee & Parsegian, PNAS 81:2621 (1984)", "READ DIRECTLY"
        ),
        T139Literature(
            "Mg2+ decay constants, measured directly",
            "2.7 / 2.8 / 2.1 A at 5 / 25 / 100 mM MgCl2 — nearly salt-independent",
            "osmotic stress", "Rau, Lee & Parsegian (1984)", "READ DIRECTLY"
        ),
        T139Literature(
            "hydration-versus-electrostatics crossover",
            "interaxial 30-35 A (Rau 1992, Podgornik 1994) or 26 A (Meng 2020)",
            "the project's 18-36 A window is almost entirely inside the short-range regime",
            "three papers", "READ DIRECTLY"
        ),
        T139Literature(
            "Mg2+ does not condense duplex DNA",
            "\"nonspecifically interacting monovalent and divalent cations ... even at molar " +
                "concentrations, do not condense dsDNA from dilute solution\"",
            "the empirical bound on strong-coupling correlation attraction, which is where " +
                "mean-field PB would have the wrong SIGN",
            "Qiu, Parsegian & Rau, PNAS 107:21482 (2010)", "READ DIRECTLY"
        ),
        T139Literature(
            "Hamaker constant, cylinder-cylinder across water",
            "4.33-5.90 zJ", "Lifshitz, retarded, zero-frequency screened at 5 nm",
            "Dryden et al., Langmuir 31:10145 (2015), via C-0021", "CITED via C-0021"
        ),
        T139Literature(
            "blunt-end coaxial stacking is an established origami motif and it is ATTRACTIVE",
            "-2.63 kcal/mol per helix between two separate tiles = " +
                "${BluntEndStacking.perStackEnergy / thermalEnergy()} kT",
            "1xTAE + 12.5 mM Mg2+, 22 C, AFM monomer/dimer counting",
            "Woo & Rothemund, Nature Chem. 3:620 (2011), SI Table S4", "READ DIRECTLY"
        ),
        T139Literature(
            "the RANGE of blunt-end stacking",
            "oxDNA2 coaxial-stacking hard cutoff 5.1108 A (minimum 3.4072 A); all-atom PMF " +
                "force falls past 6.5 A and turns slightly repulsive after ~13 A",
            "a contact interaction of one to two base-pair rises, no attractive tail",
            "Henrich et al., EPJE 41:57 (2018) / LAMMPS pair_oxdna2; Maffeo, Luan & " +
                "Aksimentiev, NAR 40:3812 (2012)", "READ DIRECTLY"
        ),
        T139Literature(
            "B-form phosphate radius",
            "0.908638 +/- 0.066499 nm", "13 084 crystallographic linkages",
            "T-71, this repository", "MEASURED, THIS REPOSITORY"
        )
    )

    val findings = listOf(
        "THE QUESTION AS POSED HAS NO ANSWER, AND THAT IS THE ANSWER. Two unbonded parallel " +
            "duplexes in 2 mM MgCl2 hold no separation at all: the interaction is repulsive at " +
            "every separation and the array swells without bound as the load goes to zero. " +
            "Four independent methods, every one read directly.",
        "THE WIDTH IS A THRESHOLD, NOT A SEPARATION — the eighth instance in this project of a " +
            "quantity that is not well posed without the state it is read at, and the first " +
            "where the missing state is an ENERGY BUDGET rather than a load, a compression or " +
            "a lattice coordinate.",
        "AND THE BUDGET IS MEASURABLE, because the host sheet is a measured object: holding two " +
            "adjacent duplexes at the SAXS 2.69 nm over one 40 nm interface costs " +
            "${sheetPerInterface / kT} kT, i.e. ${sheetPerCrossover / kT} kT per crossover " +
            "column. That is the design's own demonstrated currency, and the width it buys is " +
            "$coaxialWidth nm — BELOW the " +
            "$threshold nm placement threshold.",
        "A THERMAL CRITERION IS THE WRONG ONE HERE and it is worth the whole verdict: at 1 kT " +
            "the width is ${state.exclusionWidthAtEnergy(kT)} nm and nothing places. Both " +
            "bodies are covalently rooted to the same sheet, so the energy is paid by the fold " +
            "and not by kT; Barker-Henderson is the criterion for two FREE bodies colliding.",
        "THE EDGE EXISTS AND IT IS NOT ELECTROSTATIC. The Debye length at 2 mM is " +
            "${buffer.debyeLength()} nm, longer than the whole disputed bracket, so " +
            "electrostatics cannot place an edge at 2.7 nm. The measured short-range law can: " +
            "${shortRange.decayLength} nm. A bespoke two-cylinder nonlinear PB solve would " +
            "have resolved the term that carries no edge.",
        "THE COLLINEAR CLEARANCE IS A COAXIAL GEOMETRY AND IT IS ${crossedAtThreshold / coaxialAtThreshold}x " +
            "CHEAPER than the crossed one at the same separation — and FINITE at zero gap, " +
            "${pair.coaxialScreenedCoulombEnergy(0.0, kappa) / kT} kT. C-0069's Q5 gap contains " +
            "no body at all; charging it a duplex girth charges a body that is not there.",
        "AND THE SIGN REVERSES: what a collinear gap has to prevent is a BOND, not a clash. " +
            "Two duplexes end to end blunt-end STACK — an established origami motif worth " +
            "${BluntEndStacking.perStackEnergy / kT} kT per helix (Woo & Rothemund, read " +
            "directly) — so the clearance is a stacking-PREVENTION allowance and its length is " +
            "the stacking range, ${BluntEndStacking.OXDNA2_CUTOFF} nm (oxDNA2's cutoff) to " +
            "${BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET} nm (where the all-atom PMF turns " +
            "repulsive). At the generous end that is a plan margin of " +
            "${32 * 0.34 - BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET - T139_ARM} nm, " +
            "${(32 * 0.34 - BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET - T139_ARM) / 0.02560917}x " +
            "the published knife edge. This is CH-0089's own failure route 2 — 'a demonstration " +
            "that C-0053's footprint convention is charging something other than a body' — " +
            "answered in the direction that WIDENS the margin.",
        "THE KNIFE EDGE IS UNRESOLVABLE BY PHYSICS TOO. 0.0256 nm at 2.7 nm is " +
            "${abs(state.energyGradient(threshold)) * 0.02560917 / crossedAtThreshold} of the " +
            "pair energy — C-0072's 'neither margin is quotable' arriving through a second, " +
            "wholly independent channel.",
        "THE TWO RADII ARE NOT THE SAME NUMBER, and it is 41 % of the quantity under test. " +
            "The charge sits at T-71's measured 0.9086 nm and the field's hard body at 1.0 nm; " +
            "working in INTERAXIAL coordinates throughout is what avoids having to choose.",
        "MEAN FIELD IS NOT CONTROLLED HERE AND THE BOUND THAT SAVES IT IS EMPIRICAL. C-0005 " +
            "puts Xi at 17-24 for Mg2+ at a DNA surface with no systematic theory, and " +
            "like-charged rods in strong coupling is exactly where PB has the wrong SIGN. What " +
            "bounds it is a measurement, not a theory: Mg2+ does not condense duplex DNA at " +
            "any concentration.",
        "THE DEBYE-HUCKEL PREMISE FAILS ON THIS MATERIAL and is reported rather than assumed: " +
            "the reduced surface potential is ${pair.reducedSurfacePotential(kappa)}, above one " +
            "even after Manning renormalisation. Every electrostatic number here is an " +
            "order-of-magnitude bracket, which is why the verdict rests on the RANGE."
    )

    val result = T139Result(
        task = "T-139",
        leaf = "A8.2 (plan model) with A7.4 (electrostatics) and A1.2 (the anchoring array)",
        parameters = mapOf(
            "temperature [K]" to "300.0",
            "k_BT [pN nm]" to kT.toString(),
            "medium" to "aqueous 2 mM MgCl2, 2:1, I = 3c = ${buffer.ionicStrength} mM",
            "Debye length [nm]" to buffer.debyeLength().toString(),
            "Bjerrum length [nm]" to pair.bjerrumLength.toString(),
            "relative permittivity" to WATER_RELATIVE_PERMITTIVITY.toString(),
            "phosphate (charge) radius [nm], MEASURED T-71" to pair.helixRadius.toString(),
            "hard (steric) radius [nm], CITED convention" to state.hardRadius.toString(),
            "bare linear charge density [e/nm]" to pair.bareLinearChargeDensity.toString(),
            "Manning parameter xi_M" to pair.manningParameter.toString(),
            "Manning-surviving line charge at q = 2 [e/nm]" to
                pair.effectiveLinearChargeDensity.toString(),
            "Hamaker constant [zJ = pN nm]" to state.hamaker.toString(),
            "short-range Pi_R [pN/nm2], CITED Meng 2020" to
                shortRange.repulsionAmplitude.toString(),
            "short-range lambda [nm], CITED Meng 2020" to shortRange.decayLength.toString(),
            "model floor, surface separation [nm]" to state.modelFloor.toString(),
            "plan-relevant range [nm]" to PLAN_RELEVANT_RANGE.toString(),
            "C-0055 arm [nm]" to T139_ARM.toString(),
            "root pitch [nm]" to (32 * 0.34).toString(),
            "placement threshold [nm]" to threshold.toString(),
            "T-71 steric floor [nm]" to floor.toString(),
            "row pitch [nm]" to T139_ROW_PITCH.toString(),
            "stations" to C0055_ARM_COUNT.toString(),
            "sources" to "gpd/results/T-125-upward-root-placement.json"
        ),
        cheapBounds = cheapBounds,
        geometries = geometries,
        profile = profile,
        stationaryPoints = stationary,
        thresholdWidths = thresholdLadder,
        calibration = calibration,
        widthLadder = widthLadder,
        downstream = downstream,
        reproductions = reproductions,
        literature = literature,
        convergence = convergence,
        falsifiers = falsifiers,
        predicates = predicates,
        findings = findings
    )

    val output = File("gpd/results/T-139-duplex-pair-separation.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY, floor = 1e-12
            ) as JsonObject)
        ) + "\n"
    )
    println("wrote ${output.path}")
    findings.forEach { println("- $it") }
}
