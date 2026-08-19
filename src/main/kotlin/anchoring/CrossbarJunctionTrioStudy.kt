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

import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Task `T-117` / leaf `A8.2` — whether **three** 90° junctions close on **one** crossbar duplex.
 *
 * ```shell
 * tools/study.sh anchoring.CrossbarJunctionTrioStudyKt
 * ```
 *
 * Emits `gpd/results/T-117-crossbar-junction-trio.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val DEGREES = 180.0 / PI

/** The groove convention the closure search runs on, as `C-0029` and `C-0042` both adopt. */
private val NOMINAL = DuplexBackbone()

/** The hard, convention-free 180° chord the mechanics runs on, as `C-0037` and `C-0048` adopt. */
private val HARD = DuplexBackbone(minorGrooveAngle = 180.0)

/** `C-0042`'s solved base azimuth: the one that puts the base chord exactly on the flexure axis. */
private val BASE_AZIMUTH = 2.0 * PI - 0.5 * NOMINAL.minorGrooveAngle * PI / 180.0

// ---------------------------------------------------------------------------------------------

@Serializable
data class T117BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val note: String
)

@Serializable
data class T117GeometryRecord(
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val crossbarLength: Double,
    val lengthDemand: Double,
    val minimumBasePairs: Int,
    val legRimClearance: Double,
    val legSeatContact: Double,
    val flexureSeatContact: Double,
    val legFlexureClearance: Double,
    val rigidHeight: Double,
    val junctionCount: Int,
    val covalentLinkCount: Int
)

@Serializable
data class T117TwistRecord(
    val steps: Int,
    val legLength: Double,
    val flexureHeight: Double,
    val relativeChordDegrees: Double,
    val capBudgetDegrees: Double,
    val coupleFraction: Double,
    val relativeNaturalDegrees: Double,
    val capBudgetNaturalDegrees: Double,
    val note: String
)

@Serializable
data class T117ClosureRecord(
    val mode: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val legSteps: Int,
    val closes: Boolean,
    val allCovalent: Boolean,
    val worstResidual: Double,
    val worstGap: Double,
    val worstMisalignmentDegrees: Double,
    val unpairedNucleotides: Int,
    val distinctTargets: Boolean,
    val minimumTerminusSeparation: Double,
    val minimumSeatContact: Double,
    val helicalPhaseDegrees: Double,
    val axialPhase: Double,
    val lateralOffset: Double,
    val targets: List<String>,
    val note: String
)

@Serializable
data class T117DesignRecord(
    val id: String,
    val legSteps: Int,
    val legLength: Double,
    val flexureHeight: Double,
    val separationBasePairs: Int,
    val baseMisalignmentDegrees: Double,
    val capMisalignmentDegrees: Double,
    val flexureMisalignmentDegrees: Double,
    val capBudgetDegrees: Double,
    val frameCouple: Double,
    val span: Double,
    val spanBasePairs: Double,
    val tangent: Double,
    val supplyToDemand: Double,
    val duty: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val marginCanDo: Double,
    val marginFields: Double,
    val verdict: String
)

@Serializable
data class T117StabilityRecord(
    val basePairs: Int,
    val sequence: String,
    val stepFreeEnergy: Double,
    val freeEnergy: Double,
    val freeEnergyKt: Double,
    val basePairsForAverage: Int,
    val note: String
)

@Serializable
data class T117SensitivityRecord(
    val axis: String,
    val label: String,
    val closes: Boolean,
    val worstMisalignmentDegrees: Double,
    val minimumSeatContact: Double,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
data class T117ConvergenceRecord(
    val axis: String,
    val level: String,
    val closes: Boolean,
    val worstResidual: Double,
    val worstMisalignmentDegrees: Double,
    val departure: Double
)

@Serializable
data class T117ReproductionRecord(
    val name: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val source: String
)

@Serializable
data class T117LiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class T117Result(
    val task: String,
    val leaf: String,
    val temperatureKelvin: Double,
    val kbT: Double,
    val units: String,
    val bounds: List<T117BoundRecord>,
    val geometry: List<T117GeometryRecord>,
    val twist: List<T117TwistRecord>,
    val closures: List<T117ClosureRecord>,
    val designs: List<T117DesignRecord>,
    val stability: List<T117StabilityRecord>,
    val sensitivities: List<T117SensitivityRecord>,
    val convergence: List<T117ConvergenceRecord>,
    val reproductions: List<T117ReproductionRecord>,
    val literature: List<T117LiteratureRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

/** The leg's own azimuth at its **head**, carried up the leg from `C-0042`'s solved base. */
private fun lockedLegAzimuth(steps: Int, halfTurn: Boolean = false): Double =
    BASE_AZIMUTH + steps * NOMINAL.twistPerBasePair + if (halfTurn) PI else 0.0

private fun searchFor(
    crossbarBasePairs: Int,
    separationBasePairs: Int,
    legSteps: Int? = null,
    flexureAzimuth: Double? = null,
    azimuthSteps: Int = 180,
    phaseSteps: Int = 360,
    axialSteps: Int = 8,
    lateralSeats: List<Double> = listOf(-0.4, -0.2, 0.0, 0.2, 0.4),
    backbone: DuplexBackbone = NOMINAL,
    halfTurns: Pair<Boolean, Boolean> = false to false
): CrossbarTrioSearch = CrossbarTrioSearch(
    backbone = backbone,
    crossbarBasePairs = crossbarBasePairs,
    separationBasePairs = separationBasePairs,
    junctions = TrioJunctionSpec.cap(separationBasePairs, backbone.risePerBasePair),
    azimuthSteps = azimuthSteps,
    phaseSteps = phaseSteps,
    axialSteps = axialSteps,
    lateralSeats = lateralSeats,
    lockedAzimuths = legSteps?.let {
        listOf(
            lockedLegAzimuth(it, halfTurns.first),
            lockedLegAzimuth(it, halfTurns.second),
            // the flexure's own azimuth stays FREE: its other end is on a different crossbar, so
            // nothing on THIS crossbar fixes it
            flexureAzimuth
        )
    }
)

private fun closureRecord(
    mode: String,
    crossbarBasePairs: Int,
    separationBasePairs: Int,
    legSteps: Int,
    closure: CrossbarTrioClosure?,
    note: String
): T117ClosureRecord = T117ClosureRecord(
    mode = mode,
    crossbarBasePairs = crossbarBasePairs,
    separationBasePairs = separationBasePairs,
    legSteps = legSteps,
    closes = closure != null && closure.allCovalent,
    allCovalent = closure?.allCovalent ?: false,
    worstResidual = closure?.worstResidual ?: -1.0,
    worstGap = closure?.worstGap ?: -1.0,
    worstMisalignmentDegrees = (closure?.worstMisalignment ?: -1.0) * DEGREES,
    unpairedNucleotides = closure?.unpairedNucleotides ?: -1,
    distinctTargets = closure?.distinctTargets ?: false,
    minimumTerminusSeparation = closure?.minimumTerminusSeparation ?: -1.0,
    minimumSeatContact = closure?.minimumSeatContact ?: -1.0,
    helicalPhaseDegrees = (closure?.helicalPhase ?: -1.0) * DEGREES,
    axialPhase = closure?.axialPhase ?: -1.0,
    lateralOffset = closure?.lateralOffset ?: 0.0,
    targets = closure?.placements?.map {
        "${it.name}: s${it.firstTarget.strand}b${it.firstTarget.index} " +
                "%.3f nm, s${it.secondTarget.strand}b${it.secondTarget.index} %.3f nm"
            .format(it.firstGap, it.secondGap)
    } ?: emptyList(),
    note = note
)

// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod")
private fun main0(): T117Result {

    // ------------------------------------------------------------------ the four cheap bounds

    val recommended = CrossbarGeometry(13, 7)
    val bestSteps = bestChordPairSteps(12..26, 0.5 * PI, NOMINAL)

    val bounds = listOf(
        T117BoundRecord(
            "bound 1 — the leg's rim clearance on C-0048's own 13 bp crossbar",
            recommended.legRimClearance, "nm",
            "the crossbar's own end is this far outboard of the leg's footprint; negative would " +
                    "mean the leg overhangs the seat entirely"
        ),
        T117BoundRecord(
            "bound 1 — the leg's TRUNCATED seat contact at 13 bp",
            recommended.legSeatContact, "nm",
            "C-0042's floor is 1.60 nm and its unbounded value here is 2.00; the rim is what " +
                    "decides whether the exclusion binds"
        ),
        T117BoundRecord(
            "bound 2 — the leg-to-flexure solid clearance at the 7 bp row",
            recommended.legFlexureClearance, "nm",
            "against the 0.69 nm surface gap a 2.69 nm single-layer sheet keeps and the 0.54 nm " +
                    "a 2.54 nm honeycomb keeps"
        ),
        T117BoundRecord(
            "bound 3 — the chord budget of C-0037's own 21-step leg",
            chordPairMisalignment(21, 0.5 * PI, NOMINAL) * DEGREES, "degrees",
            "how far a 21-step leg falls short of presenting its base chord along the flexure " +
                    "axis AND its cap chord across it — the quantity C-0048 assumes is zero"
        ),
        T117BoundRecord(
            "bound 3 — the best chord budget available in the 12–26 step envelope",
            chordPairMisalignment(bestSteps, 0.5 * PI, NOMINAL) * DEGREES, "degrees",
            "at $bestSteps steps = ${"%.2f".format(bestSteps * RISE)} nm of leg"
        ),
        T117BoundRecord(
            "bound 4 — a 13 bp crossbar at the sequence-averaged unified NN step",
            duplexFreeEnergy(13, UnifiedNearestNeighbour.AVERAGE), "kcal/mol",
            "SantaLucia (1998) Table 1, 1 M NaCl, no salt correction applied — the OPTIMISTIC end"
        ),
        T117BoundRecord(
            "bound 4 — the same crossbar at the weakest step in the table",
            duplexFreeEnergy(13, UnifiedNearestNeighbour.WEAKEST), "kcal/mol",
            "an all-TA/AT 13-mer: the spread over sequence is 4.3x and dwarfs any salt correction"
        ),
        T117BoundRecord(
            "the chord budget of the FLEXURE at C-0048's own span",
            chordPairMisalignment(
                ((28.2512884 - 2.0 * BForm.DUPLEX_RADIUS) / RISE).roundToInt() - 1, 0.0, NOMINAL
            ) * DEGREES, "degrees",
            "bound 3 applied to the other two-junction body in the design: both ends of one " +
                    "flexure want a VERTICAL chord, so their demand is parallel chords"
        )
    )

    // ------------------------------------------------------------------ the plan geometry

    val geometry = (6..12).flatMap { separation ->
        val minimum = CrossbarGeometry(separation + 6, separation).minimumBasePairs
        listOf(minimum, minimum + 2, minimum + 4).map { basePairs ->
            val g = CrossbarGeometry(basePairs, separation)
            T117GeometryRecord(
                crossbarBasePairs = basePairs,
                separationBasePairs = separation,
                crossbarLength = g.length,
                lengthDemand = g.minimumLength,
                minimumBasePairs = g.minimumBasePairs,
                legRimClearance = g.legRimClearance,
                legSeatContact = g.legSeatContact,
                flexureSeatContact = g.flexureSeatContact,
                legFlexureClearance = g.legFlexureClearance,
                rigidHeight = g.rigidHeight,
                junctionCount = g.junctionCount,
                covalentLinkCount = g.covalentLinkCount
            )
        }
    }

    // ------------------------------------------------------------------ bound 3, in full

    val natural = DuplexBackbone(basePairsPerTurn = BForm.BASE_PAIRS_PER_TURN_HONEYCOMB)
    val twist = (12..26).map { steps ->
        T117TwistRecord(
            steps = steps,
            legLength = steps * RISE,
            flexureHeight = steps * RISE + BForm.DUPLEX_RADIUS,
            relativeChordDegrees = relativeChordAzimuth(steps, NOMINAL) * DEGREES,
            capBudgetDegrees = chordPairMisalignment(steps, 0.5 * PI, NOMINAL) * DEGREES,
            coupleFraction = couplePhaseProjection(chordPairMisalignment(steps, 0.5 * PI, NOMINAL)),
            relativeNaturalDegrees = relativeChordAzimuth(steps, natural) * DEGREES,
            capBudgetNaturalDegrees = chordPairMisalignment(steps, 0.5 * PI, natural) * DEGREES,
            note = when {
                chordPairMisalignment(steps, 0.5 * PI, NOMINAL) < 0.05 &&
                        chordPairMisalignment(steps, 0.5 * PI, natural) < 0.3 ->
                    "delivers C-0048's azimuth pair on BOTH twist readings"

                chordPairMisalignment(steps, 0.5 * PI, NOMINAL) > 1.2 ->
                    "lands on C-0048's WORSE corner — the cap chord along the flexure axis"

                else -> ""
            }
        )
    }

    // ------------------------------------------------------------------ the closure search

    val closures = ArrayList<T117ClosureRecord>()

    // the acceptance question: every junction's azimuth free
    (13..19).forEach { basePairs ->
        val closure = searchFor(basePairs, 7).best()
        closures.add(
            closureRecord(
                "FREE", basePairs, 7, 0, closure,
                "every junction's own azimuth searched — the acceptance question as posed"
            )
        )
    }
    (6..12).forEach { separation ->
        val basePairs = CrossbarGeometry(separation + 6, separation).minimumBasePairs + 2
        val closure = searchFor(basePairs, separation).best()
        closures.add(
            closureRecord(
                "FREE", basePairs, separation, 0, closure,
                "the row swept over C-0042's own admissible band"
            )
        )
    }

    // the design question: the legs' azimuths carried up from C-0042's solved base
    listOf(false to false, true to false, false to true, true to true).forEach { halfTurns ->
        (12..26).forEach { steps ->
            val closure = searchFor(15, 7, legSteps = steps, halfTurns = halfTurns).best()
            closures.add(
                closureRecord(
                    "LOCKED", 15, 7, steps, closure,
                    "leg azimuths fixed by C-0042's base placement plus ${steps} steps of twist" +
                            ", half turns ${halfTurns.first}/${halfTurns.second}"
                )
            )
        }
    }

    // ------------------------------------------------------------------ the design, quantised

    val designs = ArrayList<T117DesignRecord>()
    designs.add(
        designRecord(
            "C0048", capDesign(7.00, 7, 0.0, 0.0, 0.0),
            chordPairMisalignment(21, 0.5 * PI, NOMINAL)
        )
    )
    designs.add(
        designRecord(
            "C0048along", capDesign(7.00, 7, 0.0, 0.5 * PI, 0.0),
            chordPairMisalignment(21, 0.5 * PI, NOMINAL)
        )
    )
    (12..26).forEach { steps ->
        designs.add(
            designRecord(
                "Q$steps", quantisedCapDesign(steps, 7),
                chordPairMisalignment(steps, 0.5 * PI, NOMINAL)
            )
        )
    }

    // ------------------------------------------------------------------ is the crossbar a duplex

    val target = duplexFreeEnergy(13, UnifiedNearestNeighbour.AVERAGE)
    val stability = ArrayList<T117StabilityRecord>()
    listOf(
        Triple("sequence-averaged", UnifiedNearestNeighbour.AVERAGE, 13),
        Triple("weakest step (all TA/AT)", UnifiedNearestNeighbour.WEAKEST, 13),
        Triple("strongest step (all GC/CG)", UnifiedNearestNeighbour.STRONGEST, 13),
        Triple("sequence-averaged", UnifiedNearestNeighbour.AVERAGE, 15),
        Triple("sequence-averaged", UnifiedNearestNeighbour.AVERAGE, 21)
    ).forEach { (name, step, basePairs) ->
        stability.add(
            T117StabilityRecord(
                basePairs = basePairs,
                sequence = "1 M NaCl (SantaLucia), $name",
                stepFreeEnergy = step,
                freeEnergy = duplexFreeEnergy(basePairs, step),
                freeEnergyKt = duplexFreeEnergy(basePairs, step) * KCAL_PER_MOL_IN_KT,
                basePairsForAverage = basePairsForFreeEnergy(target, step),
                note = "the unified parameters at their OWN 1 M NaCl reference — the optimistic " +
                        "end for a 2 mM MgCl2 buffer, and carried only as the cross-check"
            )
        )
    }
    // the buffer this project actually runs in, on measured magnesium parameters
    listOf(
        "sequence-averaged" to MagnesiumNearestNeighbour.AVERAGE,
        "weakest step (all TA/AT)" to MagnesiumNearestNeighbour.WEAKEST,
        "strongest step (all GC/CG)" to MagnesiumNearestNeighbour.STRONGEST
    ).forEach { (name, step) ->
        val factor = if (name.startsWith("sequence")) MagnesiumNearestNeighbour.AVERAGE_SALT_FACTOR
        else MagnesiumNearestNeighbour.saltFactor(step)
        listOf(true, false).forEach { natural ->
            val corrected = saltCorrectedStepFreeEnergy(step, factor, 0.002, natural)
            listOf(13, 15, 21).forEach { basePairs ->
                stability.add(
                    T117StabilityRecord(
                        basePairs = basePairs,
                        sequence = "2 mM MgCl2 (Huguet), $name, " +
                                (if (natural) "ln" else "log10"),
                        stepFreeEnergy = corrected,
                        freeEnergy = duplexFreeEnergy(
                            basePairs, corrected, MagnesiumNearestNeighbour.INITIATION
                        ),
                        freeEnergyKt = duplexFreeEnergy(
                            basePairs, corrected, MagnesiumNearestNeighbour.INITIATION
                        ) * KCAL_PER_MOL_IN_KT,
                        basePairsForAverage = basePairsForFreeEnergy(
                            duplexFreeEnergy(
                                13,
                                saltCorrectedStepFreeEnergy(
                                    MagnesiumNearestNeighbour.AVERAGE,
                                    MagnesiumNearestNeighbour.AVERAGE_SALT_FACTOR, 0.002, natural
                                ),
                                MagnesiumNearestNeighbour.INITIATION
                            ),
                            corrected, MagnesiumNearestNeighbour.INITIATION
                        ),
                        note = "Huguet et al. (2017) Table 1 at 298 K, salt-corrected to 2 mM on " +
                                "the ${if (natural) "NATURAL" else "DECIMAL"} logarithm; the base " +
                                "of that logarithm is an IMAGE in the source and is NOT read, so " +
                                "both are carried and the natural one is the pessimistic end"
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------ sensitivities

    val reference = searchFor(15, 7).best()
    val sensitivities = ArrayList<T117SensitivityRecord>()
    fun sensitivity(axis: String, label: String, closure: CrossbarTrioClosure?, note: String) {
        sensitivities.add(
            T117SensitivityRecord(
                axis = axis,
                label = label,
                closes = closure != null && closure.allCovalent,
                worstMisalignmentDegrees = (closure?.worstMisalignment ?: -1.0) * DEGREES,
                minimumSeatContact = closure?.minimumSeatContact ?: -1.0,
                verdictMoves = (closure != null && closure.allCovalent) !=
                        (reference != null && reference.allCovalent),
                note = note
            )
        )
    }
    sensitivity(
        "groove convention", "wide 154°",
        searchFor(15, 7, backbone = DuplexBackbone(minorGrooveAngle = 154.0)).best(),
        "C-0029's other reading of the backbone separation"
    )
    sensitivity(
        "lateral seat", "the crossbar's own axis only",
        searchFor(15, 7, lateralSeats = listOf(0.0)).best(),
        "the seat C-0042's own sweep found best"
    )
    sensitivity(
        "lateral seat", "out to ±0.8 nm",
        searchFor(15, 7, lateralSeats = listOf(-0.8, -0.4, 0.0, 0.4, 0.8)).best(),
        "a wider sweep, still above the 1.60 nm contact floor only where the rim permits"
    )
    sensitivity(
        "crossbar length", "C-0048's own minimum, 13 bp",
        searchFor(13, 7).best(),
        "the length C-0048 derives, with the rim 1.02 nm from each leg's axis"
    )
    sensitivity(
        "crossbar length", "19 bp",
        searchFor(19, 7).best(),
        "overhang is mechanically free — 12EI/w and 4C/w both carry the ROW, not the crossbar"
    )
    sensitivity(
        "twist reading", "natural 10.5 bp/turn",
        searchFor(15, 7, backbone = DuplexBackbone(basePairsPerTurn = 10.5)).best(),
        "a free crossbar is not lattice-constrained, so its own twist need not be the square " +
                "lattice's 10.67"
    )

    // ------------------------------------------------------------------ convergence

    val convergence = ArrayList<T117ConvergenceRecord>()
    listOf(90, 180, 360).forEach { steps ->
        val closure = searchFor(15, 7, azimuthSteps = steps).best()
        convergence.add(
            T117ConvergenceRecord(
                axis = "azimuth grid", level = "$steps steps",
                closes = closure != null && closure.allCovalent,
                worstResidual = closure?.worstResidual ?: -1.0,
                worstMisalignmentDegrees = (closure?.worstMisalignment ?: -1.0) * DEGREES,
                departure = abs((closure?.worstMisalignment ?: 0.0) -
                        (reference?.worstMisalignment ?: 0.0)) * DEGREES
            )
        )
    }
    listOf(90, 180, 360).forEach { steps ->
        val closure = searchFor(15, 7, phaseSteps = steps).best()
        convergence.add(
            T117ConvergenceRecord(
                axis = "crossbar helical phase", level = "$steps steps",
                closes = closure != null && closure.allCovalent,
                worstResidual = closure?.worstResidual ?: -1.0,
                worstMisalignmentDegrees = (closure?.worstMisalignment ?: -1.0) * DEGREES,
                departure = abs((closure?.worstMisalignment ?: 0.0) -
                        (reference?.worstMisalignment ?: 0.0)) * DEGREES
            )
        )
    }
    listOf(4, 8, 16).forEach { steps ->
        val closure = searchFor(15, 7, axialSteps = steps).best()
        convergence.add(
            T117ConvergenceRecord(
                axis = "crossbar axial phase", level = "$steps steps per rise",
                closes = closure != null && closure.allCovalent,
                worstResidual = closure?.worstResidual ?: -1.0,
                worstMisalignmentDegrees = (closure?.worstMisalignment ?: -1.0) * DEGREES,
                departure = abs((closure?.worstMisalignment ?: 0.0) -
                        (reference?.worstMisalignment ?: 0.0)) * DEGREES
            )
        )
    }
    listOf(200, 800, 3200).forEach { iterations ->
        val leg = SolidCylinder(
            org.openrndr.math.Vector3(1.19, 0.0, -1.0),
            org.openrndr.math.Vector3(0.0, 0.0, -1.0), 1.0
        )
        val flexure = SolidCylinder(
            org.openrndr.math.Vector3(0.0, -1.0, 0.0),
            org.openrndr.math.Vector3(0.0, -1.0, 0.0), 1.0
        )
        convergence.add(
            T117ConvergenceRecord(
                axis = "alternating projection", level = "$iterations iterations",
                closes = true,
                worstResidual = minimumSolidSeparation(leg, flexure, iterations),
                worstMisalignmentDegrees = 0.0,
                departure = abs(
                    minimumSolidSeparation(leg, flexure, iterations) -
                            minimumSolidSeparation(leg, flexure, 20000)
                )
            )
        )
    }

    // ------------------------------------------------------------------ upstream reproductions

    val reproduced = capDesign(7.00, 7, 0.0, 0.0, 0.0)
    val along = capDesign(7.00, 7, 0.0, 0.5 * PI, 0.0)
    val reproductions = listOf(
        T117ReproductionRecord(
            "C-0048 Sy7 frame couple", 71.3131298, reproduced.frameCouple,
            abs(reproduced.frameCouple - 71.3131298) / 71.3131298, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 span", 28.2512884, reproduced.span,
            abs(reproduced.span - 28.2512884) / 28.2512884, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 tangent at 3 nm", 30.9319239, reproduced.tangent,
            abs(reproduced.tangent - 30.9319239) / 30.9319239, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 supply/demand", 1.81394143, reproduced.supplyToDemand,
            abs(reproduced.supplyToDemand - 1.81394143) / 1.81394143, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 duty at 10 nm", 4.59624041, reproduced.duty,
            abs(reproduced.duty - 4.59624041) / 4.59624041, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 loaded critical load", 8.94739526, reproduced.loadedCriticalLoad,
            abs(reproduced.loadedCriticalLoad - 8.94739526) / 8.94739526, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 free critical load", 9.23647708, reproduced.freeCriticalLoad,
            abs(reproduced.freeCriticalLoad - 9.23647708) / 9.23647708, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 margin on CanDo", 1.94667695, reproduced.marginCanDo,
            abs(reproduced.marginCanDo - 1.94667695) / 1.94667695, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sy7 margin on Fields", 1.46344598, reproduced.marginFields,
            abs(reproduced.marginFields - 1.46344598) / 1.46344598, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sx7 free critical load (the other corner)", 6.20025918,
            along.freeCriticalLoad, abs(along.freeCriticalLoad - 6.20025918) / 6.20025918,
            "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0048 Sx7 margin on CanDo", 1.48333669, along.marginCanDo,
            abs(along.marginCanDo - 1.48333669) / 1.48333669, "T-106 result file"
        ),
        T117ReproductionRecord(
            "C-0029 ceiling on a two-link chord", 78.2352941,
            maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS),
            abs(maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS) - 78.2352941) / 78.2352941,
            "C-0029 / C-0042"
        ),
        T117ReproductionRecord(
            "C-0042 conserved chord budget", 91.7647059, chordBaseAxes(HARD, 0.0).total,
            abs(chordBaseAxes(HARD, 0.0).total - 91.7647059) / 91.7647059, "C-0042 gate 3"
        ),
        T117ReproductionRecord(
            "C-0042 solved base azimuth, degrees", 300.0, BASE_AZIMUTH * DEGREES,
            abs(BASE_AZIMUTH * DEGREES - 300.0) / 300.0,
            "C-0042's reported placement, DERIVED here from the chord identity"
        ),
        T117ReproductionRecord(
            "C-0042 steric floor, base pairs", 6.0, pairStericFloorBasePairs().toDouble(), 0.0,
            "C-0042 cheap bound 1"
        ),
        T117ReproductionRecord(
            "C-0048 crossbar length in base pairs", 13.0,
            recommended.minimumBasePairs.toDouble(), 0.0, "C-0048's cap geometry"
        ),
        T117ReproductionRecord(
            "C-0029 azimuthal quantum, degrees per bp", 33.7394564,
            NOMINAL.twistPerBasePair * DEGREES,
            abs(NOMINAL.twistPerBasePair * DEGREES - 33.7394564) / 33.7394564, "C-0029 bound 3"
        )
    )

    // ------------------------------------------------------------------ literature

    val literature = listOf(
        T117LiteratureRecord(
            "What is the free energy of a short Watson-Crick duplex?",
            "SantaLucia's unified nearest-neighbour parameters: ten step energies from −0.58 " +
                    "(TA/AT) to −2.24 (GC/CG) kcal/mol at 37 °C in 1 M NaCl, average −1.42, with " +
                    "initiation +0.98 (terminal G·C) and +1.03 (terminal A·T). Verbatim from the " +
                    "paper's Table 1, with ΔH°/ΔS° from its Table 2",
            "read directly",
            "SantaLucia, PNAS 95:1460 (1998), PMC19045, article HTML fetched and the tables read"
        ),
        T117LiteratureRecord(
            "And its salt dependence at 2 mM MgCl₂?",
            "NOT READ. The salt-correction equation is rendered as an IMAGE in that article and " +
                    "no verbatim form was obtained, so every free energy here is quoted at the " +
                    "parameters' own 1 M NaCl — the OPTIMISTIC end for this buffer. What the task " +
                    "needs from the parameters is the SPREAD over sequence (4.3x) and the LENGTH " +
                    "threshold, and a per-base-pair salt term largely cancels in both",
            "not found",
            "the equations of PMC19045 are figures; no open-access verbatim restatement was fetched"
        ),
        T117LiteratureRecord(
            "Is there a published crossbar hosting three perpendicular duplex junctions?",
            "NOT FOUND, over the queries recorded below, on top of C-0048's 10, C-0042's 11, " +
                    "C-0037's ~72 and C-0029's ~110",
            "not found",
            "EuropePMC REST search, ~9 s apart"
        ),
        T117LiteratureRecord(
            "Is there a published rule relating a duplex's LENGTH in base pairs to the relative " +
                    "azimuth of its two ends' junctions?",
            "NOT FOUND. The helical phase of an origami duplex is a standard design consideration " +
                    "for crossover placement — that is C-0015's 32 bp lattice — but no source was " +
                    "found stating it for the two ENDS of a free duplex carrying two 90° junctions",
            "not found",
            "EuropePMC REST search; the positive statement here is arithmetic and needs no source"
        ),
        T117LiteratureRecord(
            "How close do packed origami duplexes actually come, surface to surface?",
            "0.69 nm on a single-layer sheet and 0.54 nm on a honeycomb lattice, from the SAXS " +
                    "interhelical distances 2.69 and 2.54 nm less the 2.00 nm duplex diameter",
            "read directly, via C-0009",
            "Fischer et al. (2016), the measurement C-0009 already carries"
        )
    )

    val queries = listOf(
        "ABSTRACT:\"DNA origami\" AND ABSTRACT:\"crossbar\" AND ABSTRACT:\"junction\" (0)",
        "ABSTRACT:\"DNA origami\" AND ABSTRACT:\"three junctions\" (0)",
        "ABSTRACT:\"DNA\" AND ABSTRACT:\"blunt end\" AND ABSTRACT:\"perpendicular\" AND " +
                "ABSTRACT:\"duplex\" (0)",
        "ABSTRACT:\"DNA origami\" AND ABSTRACT:\"short duplex\" AND ABSTRACT:\"stability\" AND " +
                "ABSTRACT:\"melting\" (0)",
        "ABSTRACT:\"DNA origami\" AND ABSTRACT:\"staple\" AND ABSTRACT:\"binding domain\" AND " +
                "ABSTRACT:\"length\" (0)",
        "ABSTRACT:\"DNA nanostructure\" AND ABSTRACT:\"13 base pair\" (0)",
        "ABSTRACT:\"origami\" AND ABSTRACT:\"helical phase\" AND ABSTRACT:\"azimuth\" (0)",
        "ABSTRACT:\"DNA\" AND ABSTRACT:\"duplex\" AND ABSTRACT:\"two junctions\" AND " +
                "ABSTRACT:\"twist\" (0)",
        "AUTH:\"SantaLucia J\" AND PUB_YEAR:1998 AND TITLE:\"nearest-neighbor\" (4 — the unified " +
                "paper found and read)",
        "TITLE:\"unified view\" AND TITLE:\"nearest-neighbor\" (1 — the same paper)"
    )

    // ------------------------------------------------------------------ the findings

    val freeAtMinimum = closures.first { it.mode == "FREE" && it.crossbarBasePairs == 13 }
    val freeBest = closures.filter { it.mode == "FREE" }.filter { it.closes }
    val lockedBest = closures.filter { it.mode == "LOCKED" && it.closes }
        .minByOrNull { it.worstMisalignmentDegrees }
    val quantised = designs.filter { it.id.startsWith("Q") }
    val worstQuantised = quantised.first { it.legSteps == 21 }

    val findings = mapOf(
        "closure" to (
                "THREE 90 degree junctions %s on one lone crossbar duplex. At C-0048's own 13 bp " +
                        "the trio %s; the shortest crossbar that admits it is %d bp, and the " +
                        "binding constraint is %s. Every closing trio has ZERO unpaired " +
                        "nucleotides and six distinct targets."
                ).format(
                if (freeBest.isNotEmpty()) "DO close" else "do NOT close",
                if (freeAtMinimum.closes) "closes" else "does not close",
                freeBest.minOfOrNull { it.crossbarBasePairs } ?: -1,
                if (freeAtMinimum.closes) "not the rim" else "the axial rim, not the phosphate reach"
            ),
        "quantisation" to (
                "A LEG IS ONE BODY WITH TWO JUNCTIONS, so its base chord and its cap chord are " +
                        "not independent: they differ by steps x 33.74 degrees. C-0048's " +
                        "recommended 7.00 nm leg rounds to 21 steps, whose budget is %.1f " +
                        "degrees — it lands on C-0048's OWN WORSE CORNER, and it does so on all " +
                        "three twist readings. The envelope does contain lengths that deliver " +
                        "the recommended azimuth pair: %d steps = %.2f nm, budget %.2f degrees."
                ).format(
                chordPairMisalignment(21, 0.5 * PI, NOMINAL) * DEGREES,
                bestSteps, bestSteps * RISE,
                chordPairMisalignment(bestSteps, 0.5 * PI, NOMINAL) * DEGREES
            ),
        "conservation" to
                "The base and the cap misalignment trade one for one: rotating the leg about " +
                        "its own axis moves both chords together, so only their DIFFERENCE is " +
                        "quantised and the design chooses where to spend the budget, not " +
                        "whether to. That is C-0042's rank-one identity one level up.",
        "design" to (
                "EVERY quantised leg length still PASSES all nine predicates: over the 12-26 step " +
                        "envelope the margin runs %.2f-%.2f on CanDo and %.2f-%.2f on Fields. And " +
                        "the constrained optimum at C-0048's OWN 21 steps is %.2f / %.2f against " +
                        "its recommended %.2f / %.2f — the quantisation forces the design OFF " +
                        "C-0048's azimuth pair and the pair it is forced onto is BETTER, because " +
                        "C-0048 picked the cap azimuth to maximise the FREE plane while the " +
                        "LOADED plane governs at that leg length. The optimum balances the two."
                ).format(
                quantised.minOf { it.marginCanDo }, quantised.maxOf { it.marginCanDo },
                quantised.minOf { it.marginFields }, quantised.maxOf { it.marginFields },
                worstQuantised.marginCanDo, worstQuantised.marginFields,
                designs.first { it.id == "C0048" }.marginCanDo,
                designs.first { it.id == "C0048" }.marginFields
            ),
        "locked" to (
                "With the legs' azimuths carried up from C-0042's solved base the trio %s, at a " +
                        "worst chord misalignment of %.1f degrees."
                ).format(
                if (lockedBest != null) "still closes" else "does NOT close",
                lockedBest?.worstMisalignmentDegrees ?: -1.0
            ),
        "stability" to (
                "A 13 bp crossbar is %.1f kcal/mol = %.0f k_BT at the sequence-averaged unified " +
                        "step and only %.1f kcal/mol at the weakest — a 4.3x spread, so SEQUENCE " +
                        "and not length is the dominant lever. And the remedy is nearly free: " +
                        "the crossbar's mechanics carry the ROW pitch (12EI/w, 4C/w) and not the " +
                        "crossbar's length, so overhang costs plan area and nothing else."
                ).format(
                duplexFreeEnergy(13, UnifiedNearestNeighbour.AVERAGE),
                duplexFreeEnergy(13, UnifiedNearestNeighbour.AVERAGE) * KCAL_PER_MOL_IN_KT,
                duplexFreeEnergy(13, UnifiedNearestNeighbour.WEAKEST)
            ),
        "queries" to queries.joinToString("; ")
    )

    return T117Result(
        task = "T-117",
        leaf = "A8.2",
        temperatureKelvin = 300.0,
        kbT = 4.141947,
        units = "nm, pN, pN·nm, pN·nm/rad, pN/nm, kcal/mol",
        bounds = bounds,
        geometry = geometry,
        twist = twist,
        closures = closures,
        designs = designs,
        stability = stability,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = literature,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Nothing here is measured. The closure test is C-0029's and inherits its " +
                    "caveat three times over: a phosphate pair inside the measured [0.60, 0.70] " +
                    "nm step with no van der Waals overlap. No backbone torsion angle is checked " +
                    "and no sequence is designed, so a 'closes' verdict is an UPPER bound on " +
                    "buildability. T-71.",
            "The chord-twist quantisation is ARITHMETIC and inherits nothing: it needs only that " +
                    "a duplex end has two termini (C-0029's count) and that a helix twists. It " +
                    "is carried on the square lattice's 10.67 bp/turn AND on 10.5, because a " +
                    "free leg is not lattice-constrained; the recommended lengths are the ones " +
                    "that survive both.",
            "The leg's rotation sweep is over [0, pi/4] in the base's misalignment, because past " +
                    "a half right angle a two-link base's two axes exchange and C-0037's " +
                    "TwoLinkBase invariant cannot represent it. A declared modelling boundary.",
            "The crossbar is modelled as an IDEAL duplex of the same rise, radius and twist as " +
                    "every other duplex here. Its own end-fraying is not modelled, and a real " +
                    "13-mer frays from both ends.",
            "The free energies are at the unified parameters' own 1 M NaCl. The salt correction " +
                    "is NOT read and the numbers are therefore the OPTIMISTIC end for 2 mM MgCl₂.",
            "The bodies are hard cylinders with flat end faces. A real duplex has grooves and a " +
                    "real end face has a rim, so the leg-to-flexure clearance is a MODEL number " +
                    "and is reported against the surface gaps packed origami actually keeps.",
            "The search is a grid and its optimum is A solution, not THE solution. Every grid was " +
                    "doubled and the verdict did not move, but a grid cannot prove a " +
                    "non-existence finer than itself.",
            "The flexure is taken to butt the crossbar's SIDE at the crossbar's own axis height, " +
                    "which is C-0048's e = R. A flexure sitting ON the crossbar would double it.",
            "The two legs are taken to be the SAME length in base pairs. Legs of different " +
                    "lengths would tilt the crossbar and are not modelled.",
            "k_s is C-0020's DERIVED, unmeasured construction, and every junction constant here " +
                    "rests on it, exactly as in C-0028, C-0029, C-0037, C-0042 and C-0048."
        ),
        openQuestions = listOf(
            "The backbone torsion of the trio. T-71, now with three junctions rather than two.",
            "Whether the flexure's own two ends can be quantised at no cost to the placement " +
                    "condition — the same bound applied to the other two-junction body, where " +
                    "the length is an OUTPUT of the placement rather than a free choice.",
            "The salt correction to the crossbar's free energy at 2 mM MgCl₂.",
            "Whether the plan view admits 180 legs, 90 crossbars and 45 flexures with the " +
                    "overhangs this task recommends. T-96 / T-116.",
            "k_s. T-9."
        ),
        citedNumbers = listOf(
            "phosphate radius 1.00 nm — CITED, READ DIRECTLY (Hedley et al., 2024) via C-0029",
            "intrastrand phosphodiester step 0.60-0.70 nm — CITED, MEASURED (Bosco et al., 2014) " +
                    "via C-0029. A WINDOW",
            "duplex steric radius 1.00 nm — CITED, the standard 2 nm diameter",
            "rise per base pair 0.34 nm — CITED (Douglas et al., 2009)",
            "base pairs per turn 10.67 square / 10.5 — CITED; both carried",
            "interhelical distance 2.69 nm — CITED, MEASURED by SAXS (Fischer et al., 2016)",
            "duplex EI 230 pN·nm² — CITED, a CanDo MODEL INPUT (Kim et al., 2012)",
            "Fields et al.'s implied rigidity 172.9 pN·nm² — CITED, MEASURED (2013)",
            "k_bond,θ 6.765 pN·nm/rad — CITED+FITTED (Chen et al., 2014) via C-0009",
            "k_bond,s 32.35 pN/nm — DERIVED (C-0020), NOT measured",
            "unified nearest-neighbour ΔG°37 parameters — CITED, READ DIRECTLY (SantaLucia, " +
                    "PNAS 95:1460, 1998, Table 1, PMC19045)",
            "the salt correction to those parameters — NOT READ, and flagged everywhere it matters",
            "§3 targets 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED"
        )
    )
}

private fun designRecord(id: String, point: CapDesignPoint, budget: Double): T117DesignRecord =
    T117DesignRecord(
        id = id,
        legSteps = point.legSteps,
        legLength = point.legLength,
        flexureHeight = point.flexureHeight,
        separationBasePairs = point.separationBasePairs,
        baseMisalignmentDegrees = point.baseMisalignment * DEGREES,
        capMisalignmentDegrees = point.capMisalignment * DEGREES,
        flexureMisalignmentDegrees = point.flexureMisalignment * DEGREES,
        capBudgetDegrees = budget * DEGREES,
        frameCouple = point.frameCouple,
        span = point.span,
        spanBasePairs = point.spanBasePairs,
        tangent = point.tangent,
        supplyToDemand = point.supplyToDemand,
        duty = point.duty,
        loadedCriticalLoad = point.loadedCriticalLoad,
        freeCriticalLoad = point.freeCriticalLoad,
        criticalLoad = point.criticalLoad,
        governingPlane = point.governingPlane,
        marginCanDo = point.marginCanDo,
        marginFields = point.marginFields,
        verdict = point.verdict
    )

fun main() {
    val result = main0()
    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-117-crossbar-junction-trio.json")
    file.parentFile?.mkdirs()
    file.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY
        )) + "\n"
    )

    println("T-117 — do three 90 degree junctions close on one crossbar duplex?")
    println()
    println("cheap bounds")
    result.bounds.forEach {
        println("  %-70s %10.4f %s".format(it.name.take(70), it.value, it.unit))
    }
    println()
    println("the chord twist (steps, leg nm, relative, budget, cos2, natural budget)")
    result.twist.forEach {
        println(
            "  %3d %5.2f %8.2f %8.2f %7.4f %8.2f  %s".format(
                it.steps, it.legLength, it.relativeChordDegrees, it.capBudgetDegrees,
                it.coupleFraction, it.capBudgetNaturalDegrees, it.note
            )
        )
    }
    println()
    println("closures (mode, bp, w, legSteps, closes, worst gap, misalignment, contact)")
    result.closures.forEach {
        println(
            "  %-7s %3d %3d %3d %6s %7.4f %8.2f %7.3f".format(
                it.mode, it.crossbarBasePairs, it.separationBasePairs, it.legSteps, it.closes,
                it.worstGap, it.worstMisalignmentDegrees, it.minimumSeatContact
            )
        )
    }
    println()
    println("designs (id, steps, leg, h, base°, cap°, budget°, span, tangent, supply, Pc, margin)")
    result.designs.forEach {
        println(
            "  %-7s %3d %5.2f %5.2f %6.2f %6.2f %6.2f %6.2f %6.2f %5.2f %5.2f %5.2f %5.2f %s".format(
                it.id, it.legSteps, it.legLength, it.flexureHeight, it.baseMisalignmentDegrees,
                it.capMisalignmentDegrees, it.capBudgetDegrees, it.span, it.tangent,
                it.supplyToDemand, it.criticalLoad, it.marginCanDo, it.marginFields, it.verdict
            )
        )
    }
    println()
    println("stability (bp, sequence, dG kcal/mol, k_BT, bp needed for the average)")
    result.stability.forEach {
        println(
            "  %3d %-52s %7.3f %8.2f %8.1f %4d".format(
                it.basePairs, it.sequence, it.stepFreeEnergy, it.freeEnergy, it.freeEnergyKt,
                it.basePairsForAverage
            )
        )
    }
    println()
    println("sensitivities")
    result.sensitivities.forEach {
        println(
            "  %-20s %-34s %6s %8.2f %7.3f %6s".format(
                it.axis, it.label, it.closes, it.worstMisalignmentDegrees, it.minimumSeatContact,
                it.verdictMoves
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-26s %-22s %6s %10.3e %8.3f %10.3e".format(
                it.axis, it.level, it.closes, it.worstResidual, it.worstMisalignmentDegrees,
                it.departure
            )
        )
    }
    println()
    println("reproductions (worst departure %.2e)".format(result.reproductions.maxOf { it.departure }))
    result.reproductions.forEach {
        println("  %-50s %12.6f %12.6f %10.2e".format(
            it.name.take(50), it.published, it.reproduced, it.departure
        ))
    }
    println()
    result.findings.forEach { (key, value) -> println("[$key] $value"); println() }
}
