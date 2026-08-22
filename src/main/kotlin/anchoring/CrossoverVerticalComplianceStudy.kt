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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.C0099_UNRESOLVED_PENALTY_FRACTION
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.CrossoverSite
import com.xemantic.nano.plentyofroom.structure.CrossoverSoftening
import com.xemantic.nano.plentyofroom.structure.FLATNESS_CONVENTION
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.RAMP_FRACTION_THRESHOLD
import com.xemantic.nano.plentyofroom.structure.REGISTRATION_FORCE_THRESHOLD
import com.xemantic.nano.plentyofroom.structure.ROW_END_UNKNOWN_MARGIN
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.VerticalComplianceVerdict
import com.xemantic.nano.plentyofroom.structure.crossoverVerticalStiffness
import com.xemantic.nano.plentyofroom.structure.crossoverVerticalStiffnessSweep
import com.xemantic.nano.plentyofroom.structure.hingeEquivalentVerticalStiffness
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.penaltyFractionOf
import com.xemantic.nano.plentyofroom.structure.rampFraction
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.verticalComplianceVerdict
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
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
 * `T-9`, second deliverable — the crossover's **vertical/axial compliance**, and whether the
 * corpus's **binary** reading of that link is right.
 *
 * Emits `gpd/results/T-9b-crossover-vertical-compliance.json`.
 *
 * Reads `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved edge profile) and
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s published reading, as the gate).
 *
 * `C-0099` swept the **14 row-end** crossovers and scaled the hinge **and** the link together;
 * this sweeps the **link alone, hinge intact, at all 49 crossovers** — the channel `T-9`'s
 * vertical deliverable needs and which no study in this repository has run.
 */

private const val T9B_DUPLEXES = 15
private const val T9B_PHASE = 8
private const val T9B_RIM_STANDOFF = 1.0
private val T9B_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T9bBoundRecord(
    val name: String,
    val statement: String,
    val value: Double,
    val unit: String,
    val owner: String,
    val derivedHere: Boolean
)

@Serializable
private data class T9bRungRecord(
    val name: String,
    val linkStiffness: Double,
    val penaltyFraction: Double,
    val freeDishingOverStroke: Double,
    val bestDishingOverStroke: Double,
    val bestKey: String,
    val publishedPlacementDishing: Double,
    val peakCrossoverForce: Double,
    val peakLinkExtension: Double?,
    val registrationLever: Double?,
    val registrationPeakForces: List<Double>,
    val uniformLoadDishing: Double,
    val flatAtTenPercent: Boolean,
    val enumerated: Int
)

@Serializable
private data class T9bTrajectoryCheck(
    val check: String,
    val question: String,
    val answer: String,
    val canAnswerTheVerticalQuestion: Boolean
)

@Serializable
private data class T9bCostRecord(
    val item: String,
    val quantity: String,
    val basis: String
)

@Serializable
private data class T9bReproduction(
    val quantity: String,
    val corpus: Double,
    val here: Double,
    val departure: Double,
    val source: String
)

@Serializable
private data class T9bFalsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val reading: String
)

@Serializable
private data class T9bResult(
    val task: String,
    val deliverable: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val sources: List<String>,
    val trajectoryCheck: List<T9bTrajectoryCheck>,
    val theRunThatCouldAnswerIt: List<T9bCostRecord>,
    val cheapBounds: List<T9bBoundRecord>,
    val sweep: List<T9bRungRecord>,
    val absentLinkReading: T9bRungRecord,
    val rigidLinkReading: T9bRungRecord,
    val physicalLinkReading: T9bRungRecord,
    val verdict: VerticalComplianceVerdict,
    val monotoneInLinkStiffness: Boolean,
    val reproductions: List<T9bReproduction>,
    val falsifiers: List<T9bFalsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
private fun t9bSolvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), T9B_RIM_STANDOFF)
}

/** `C-0090`'s published reading of one case at one phase — the dishing and the placement key. */
private fun t9bC0090Reading(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("case").jsonPrimitive.content.startsWith(casePrefix) &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        } ?: error("no C-0090 placement record for $casePrefix at phase $phase")
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

// ---------------------------------------------------------------------------------------------
// one host at one vertical link stiffness
// ---------------------------------------------------------------------------------------------

private class T9bHost(
    val sheet: OrigamiSheet,
    val edgeX: Double,
    val arm: Double,
    smooth: CollarTerm,
    rim: CollarTerm
) {

    val lengthY: Double = T9B_DUPLEXES * sheet.interhelicalDistance

    val area: Double = edgeX * lengthY

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / area

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val columns: CrossoverLayout =
        rasterColumnLayout(T9B_PHASE, sheet, edgeX, true, CrossoverLayout.EDGE_MARGIN)

    val sites: List<List<Double>> = rasterUpwardSites(
        T9B_PHASE, edgeX, T9B_DUPLEXES, true, Gen1Tile.RISE_PER_BASE_PAIR,
        CrossoverLayout.EDGE_MARGIN
    )

    val stations: List<Pair<Double, Double>> = sites.flatMapIndexed { row, xs ->
        xs.map { it to (row - (T9B_DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }

    /** One anchor's share of `C-0017`'s mandated reaction, in pN — the probe load. */
    val anchorShare: Double = T9B_MANDATE / C0055_ARM_COUNT

    /**
     * Nine stations spanning **one column pitch** from the column nearest the tile centre —
     * `C-0015`'s registration cell, at `C-0015`'s own sampling.
     */
    val registrationStations: List<Double> = run {
        val pitch = sheet.crossoverSpacing / 2.0
        val start = columns.positions.minByOrNull { abs(it) }!!
        (0 until 9).map { start + it * pitch / 8.0 }
    }

    /**
     * The lattice at one vertical link stiffness.
     *
     * [linkStiffness] enters the constructor globally, so every one of the 49 crossovers takes it;
     * [deleteLink] instead maps every site to `CrossoverSoftening(1.0, 0.0)`, which retains the
     * dihedral spring and deletes the vertical link — the `absent` end of the binary, and a state
     * `C-0090`'s two published readings cannot express.
     */
    fun lattice(linkStiffness: Double, deleteLink: Boolean = false): OrigamiGrillage {
        val bare = OrigamiGrillage(
            sheet = sheet, lengthX = edgeX, beamCount = T9B_DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT, columns = columns,
            subdivisions = 2, linkStiffness = linkStiffness
        )
        if (!deleteLink) return bare
        val everySite = bare.crossovers.map { CrossoverSite(it.lowerBeam, it.column) }
        return OrigamiGrillage(
            sheet = sheet, lengthX = edgeX, beamCount = T9B_DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT, columns = columns,
            subdivisions = 2, linkStiffness = linkStiffness,
            softenedCrossovers = everySite.associateWith { CrossoverSoftening(1.0, 0.0) }
        )
    }

    inner class Solve(linkStiffness: Double, deleteLink: Boolean, samples: Int = 81) {

        val host: OrigamiGrillage = lattice(linkStiffness, deleteLink)

        val bank = UpwardRootInfluenceBank(host, stations, solvedField, samples)

        private val uniform = List(C0055_ARM_COUNT) { T9B_MANDATE / C0055_ARM_COUNT }

        val freeDishing = bank.freePeakDishing / freeStroke

        /** The standing falsifier: a uniform load on a uniform foundation dishes exactly zero. */
        val uniformLoadDishing = host.solve(uniformField).peakDishing(samples) / freeStroke

        /** The load-path observable `C-0015`'s registration rule is written on. */
        val peakCrossoverForce = host.solve(solvedField).peakCrossoverForce

        /**
         * `C-0015`'s own lever, re-measured at this link stiffness.
         *
         * Its governing variable is *"the distance from the attachment to the nearest
         * crossover"*, so the lever is the spread of the peak crossover force as one anchor's
         * station is walked across a **registration cell** — one column pitch, nine stations,
         * which is `C-0015`'s own sampling.  A rigid vertical tie is what localises the reaction
         * at the nearest crossover and therefore what makes that curve exist at all; if the tie
         * is a spring, the curve flattens.
         */
        val registrationPeakForces: List<Double> = registrationStations.map { x ->
            host.solve(uniformPressure(0.0), listOf(PointLoad(x, 0.0, anchorShare)))
                .peakCrossoverForce
        }

        val registrationLever: Double =
            registrationPeakForces.max() / registrationPeakForces.min()

        fun surrogate(placement: UpwardArmPlacement): InfluenceSurrogate =
            bank.surrogateFor(
                placement.stations(T9B_DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                    val index = bank.indexOf(x, y)
                    require(index >= 0) { "($x, $y) is not an upward site of phase $T9B_PHASE" }
                    index
                }
            )

        fun dishing(placement: UpwardArmPlacement): Double =
            surrogate(placement).solve(uniform).peakDishing / freeStroke
    }
}

/** The exhaustive centro-symmetric optimum of one solve — `C-0090`'s enumeration and tie-break. */
private class T9bOptimum(host: T9bHost, solve: T9bHost.Solve) {

    val best: UpwardArmPlacement
    val bestValue: Double
    val enumerated: Int

    init {
        var incumbent: Pair<UpwardArmPlacement, Double>? = null
        var count = 0
        centroSymmetricPlacements(
            T9B_PHASE, host.edgeX, T9B_DUPLEXES, host.arm, C0055_ARM_COUNT,
            minimumPerRow = 2, maximumPerRow = 3
        ).forEach { placement ->
            val value = solve.dishing(placement)
            count++
            val current = incumbent
            if (current == null || value < current.second ||
                (value == current.second && placement.key < current.first.key)
            ) incumbent = placement to value
        }
        require(count > 0) { "the centro-symmetric family at phase $T9B_PHASE is empty" }
        best = incumbent!!.first
        bestValue = incumbent!!.second
        enumerated = count
    }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val edgeX = BUILDABLE_RASTER_WIDTH
    val arm = quantisedToRise(C0055_ARM_LENGTH)

    // ------------------------------------------- cheap bound 0: can the existing run answer this?
    println("T-9b — cheap bound 0: can C-0157's trajectory answer the vertical question? ...")

    val buildOxdna = File("build-oxdna")
    val resultFile = ResultInputs.T_9.file()
    val retained = if (resultFile.exists())
        Json.parseToJsonElement(resultFile.readText()).jsonObject else JsonObject(emptyMap())
    val retainedKeys = retained.keys.sorted()
    val hasVerticalField = retained.entries.any { (_, node) ->
        node.toString().contains("vertical", ignoreCase = true)
    }
    val rollEstimator = File("tools/oxdna/interduplex_roll.py")
    val tileEstimator = File("tools/oxdna/analyse_tile.py")

    val trajectoryCheck = listOf(
        T9bTrajectoryCheck(
            "the trajectory directory",
            "does build-oxdna/ — the directory tools/T-9-emit-result.py reads — exist here?",
            if (buildOxdna.isDirectory) "yes" else "NO — absent from this checkout",
            buildOxdna.isDirectory
        ),
        T9bTrajectoryCheck(
            "the raw frames",
            "were the .dat trajectories retained?",
            "NO — C-0157 section 7 records that the raw trajectories (649 MB) were pruned " +
                    "after the analysis, and every field it marks RECORDED says so",
            false
        ),
        T9bTrajectoryCheck(
            "the host",
            "can the run be repeated on this machine as it stands?",
            "NOT AS IT STANDS — tools/oxdna/README.md names an Apple M1 / macOS host and this " +
                    "box is Linux with no oxDNA build; the build recipe is retained, so the run " +
                    "is reproducible, but it is a re-run and not a re-read",
            false
        ),
        T9bTrajectoryCheck(
            "the retained result file",
            "does gpd/results/T-9-crossover-hinge-constant.json carry a vertical coordinate?",
            if (hasVerticalField) "yes" else
                "NO — of ${retainedKeys.size} top-level records, hinge/* is angular (a roll " +
                        "s.d. in degrees) and sawtooth/* is a scalar |dr|; no field of the file " +
                        "is an out-of-plane offset",
            hasVerticalField
        ),
        T9bTrajectoryCheck(
            "the estimator",
            "is there code that would compute the vertical offset if the frames returned?",
            "NO — interduplex_roll.py computes signed ANGLES only, and analyse_tile.py reduces " +
                    "the interhelical vector to its NORM, which cannot separate an out-of-plane " +
                    "offset from an in-plane one; both files are retained and neither has a " +
                    "z-decomposition (roll estimator present: ${rollEstimator.exists()}, " +
                    "tile estimator present: ${tileEstimator.exists()})",
            false
        )
    )
    val trajectoryCanAnswer = trajectoryCheck.all { it.canAnswerTheVerticalQuestion }
    println("T-9b — the existing trajectory can answer it: $trajectoryCanAnswer")

    // ------------------------------------------------------- what the run that could would cost
    val theRun = listOf(
        T9bCostRecord(
            "the coordinate's locality",
            "LOCAL — one relative displacement of two adjacent duplexes at one crossover node, " +
                    "over 49 sites",
            "the same locality as the interduplex roll, which C-0157 read to better than 10 % " +
                    "across five replicas at 450 frames; NOT the three plate modes, which are " +
                    "the tile's softest and slowest and got 20.5-24.2 independent samples"
        ),
        T9bCostRecord(
            "the protocol",
            "unchanged — oxDNA2, 15 x 112 bp at phase 8, 300 000 steps x 5 replicas",
            "C-0157's own run; what is added is a six-line estimator and RETAINING the frames"
        ),
        T9bCostRecord(
            "the wall clock",
            "about one day on an 8-core CPU box, of which the simulation is a few hours",
            "tools/oxdna/README.md and T-9 section 3, unchanged"
        ),
        T9bCostRecord(
            "the disk",
            "about 649 MB of .dat frames, which must NOT be pruned this time",
            "C-0157 section 7"
        ),
        T9bCostRecord(
            "what it is NOT",
            "not the 12-55 h per replica the three plate rigidities need",
            "C-0157's own convergence diagnostics: those are global modes, this is a local one"
        ),
        T9bCostRecord(
            "the caveat that runs the other way",
            "a SOFTER restraint decorrelates SLOWER, so the locality argument is self-referential " +
                    "in the adverse direction and the run must emit the vertical coordinate's own " +
                    "autocorrelation time beside its variance",
            "CLAUDE.md: quote a variance with its bandwidth; C-0157's own discipline for the " +
                    "plate modes, applied before rather than after"
        )
    )

    // --------------------------------------------- cheap bound 1: the physical value, no MD at all
    println("T-9b — cheap bound 1: the physical value, from this repository's own construction ...")

    val physical = crossoverVerticalStiffness(1.0)
    val hingeEquivalent = hingeEquivalentVerticalStiffness()
    val penalty = OrigamiGrillage.RIGID_LINK_STIFFNESS
    val sweepStiffness = crossoverVerticalStiffnessSweep(1.0)

    val cheapBounds = listOf(
        T9bBoundRecord(
            "the crossover's vertical displacement stiffness",
            "2 alpha S/(100 a) at alpha = 1 — Chen et al.'s softened-bond construction applied " +
                    "to the duplex constant that describes DISPLACEMENT rather than rotation, " +
                    "which is Gen1Tile.crossoverInPlaneStiffness on the orthogonal axis. " +
                    "DERIVED, not measured.",
            physical, "pN/nm", "C-0020 / Gen1Tile, applied here to the vertical axis", true
        ),
        T9bBoundRecord(
            "the penalty C-0009 enforces the link with",
            "OrigamiGrillage.RIGID_LINK_STIFFNESS, a penalty whose value the answer is asserted " +
                    "not to depend on",
            penalty, "pN/nm", "C-0009", false
        ),
        T9bBoundRecord(
            "the hinge's own equivalent vertical stiffness",
            "k_theta/d^2 — the quantity RIGID_LINK_STIFFNESS's KDoc compares itself against",
            hingeEquivalent, "pN/nm", "C-0009", true
        ),
        T9bBoundRecord(
            "the penalty over the physical value",
            "how much stiffer the constraint is enforced than the two phosphate bonds that " +
                    "supply it — a division, and the whole cheap bound",
            penalty / physical, "dimensionless", "T-9b", true
        ),
        T9bBoundRecord(
            "the physical value over the hinge equivalent",
            "the crossover's two elements against each other on ONE coordinate",
            physical / hingeEquivalent, "dimensionless", "T-9b", true
        ),
        T9bBoundRecord(
            "the physical value as a fraction of the penalty",
            "which is inside C-0099's channel-B bisection bracket [0, 0.015625] — the one " +
                    "interval of the penalty axis this repository has never resolved",
            penaltyFractionOf(physical), "dimensionless", "T-9b, against C-0099", true
        ),
        T9bBoundRecord(
            "C-0099's unresolved bracket, upper end",
            "the last rung its bisection reached before it stopped and called the response a " +
                    "discontinuity",
            C0099_UNRESOLVED_PENALTY_FRACTION, "dimensionless", "C-0099", false
        )
    )

    check(penaltyFractionOf(physical) < C0099_UNRESOLVED_PENALTY_FRACTION) {
        "F5: the physical value has left C-0099's unresolved bracket and the framing goes with it"
    }

    // ------------------------------------------------------------------------------ the sweep
    println("T-9b — reading C-0022's solved load and C-0090's published reading ...")
    val (smooth, rim) = t9bSolvedProfile(ResultInputs.T_3B.file())
    val (c0090Published, c0090Key) =
        t9bC0090Reading(ResultInputs.T_153.file(), "RECOMMENDED", T9B_PHASE)

    val host = T9bHost(sheet, edgeX, arm, smooth, rim)
    check(host.columns.size == 8) { "phase $T9B_PHASE must carry 8 columns" }
    val publishedPlacement = placementFromKey(c0090Key, T9B_PHASE, arm, edgeX)

    println("T-9b — the sweep: the link ALONE, hinge intact, at all 56 crossovers ...")

    fun rung(name: String, linkStiffness: Double, deleteLink: Boolean = false): T9bRungRecord {
        val solve = host.Solve(linkStiffness, deleteLink)
        val optimum = T9bOptimum(host, solve)
        val record = T9bRungRecord(
            name = name,
            linkStiffness = if (deleteLink) 0.0 else linkStiffness,
            penaltyFraction = if (deleteLink) 0.0 else linkStiffness / penalty,
            freeDishingOverStroke = solve.freeDishing,
            bestDishingOverStroke = optimum.bestValue,
            bestKey = optimum.best.key,
            publishedPlacementDishing = solve.dishing(publishedPlacement),
            peakCrossoverForce = solve.peakCrossoverForce,
            // null, not a sentinel: with the link deleted every crossover carries EXACTLY zero
            // force, so neither an extension nor a lever exists to be quoted (CLAUDE.md: a
            // margin of Infinity is not a margin, it is the absence of a requirement)
            peakLinkExtension = if (deleteLink) null
            else solve.peakCrossoverForce / linkStiffness,
            registrationLever = if (deleteLink) null else solve.registrationLever,
            registrationPeakForces = solve.registrationPeakForces,
            uniformLoadDishing = solve.uniformLoadDishing,
            flatAtTenPercent = optimum.bestValue < FLATNESS_CONVENTION,
            enumerated = optimum.enumerated
        )
        println(
            ("  %-46s k_z = %10.4f  best = %.9f  free = %.6f  F = %.6f  lever = %s").format(
                name, record.linkStiffness, record.bestDishingOverStroke,
                record.freeDishingOverStroke, record.peakCrossoverForce,
                record.registrationLever?.let { "%.4f".format(it) } ?: "none"
            )
        )
        return record
    }

    val rigid = rung("RIGID — C-0009's penalty, the corpus's own lattice", penalty)
    val sweep = ArrayList<T9bRungRecord>()
    sweepStiffness.forEachIndexed { index, k ->
        sweep += rung(
            "sweep x%.5f of the derived value".format(Gen1Tile.CROSSOVER_IN_PLANE_SWEEP[index]), k
        )
    }
    val physicalRung = sweep.first { abs(it.linkStiffness - physical) < 1e-9 }
    val absent = rung("ABSENT — the link deleted, the dihedral spring retained", penalty, true)

    val absentRecord = absent

    // ------------------------------------------------------------------------------ the verdict
    val verdict = verticalComplianceVerdict(
        dishingAtPhysical = physicalRung.bestDishingOverStroke,
        dishingAtRigid = rigid.bestDishingOverStroke,
        dishingAtAbsent = absentRecord.bestDishingOverStroke,
        peakForceAtPhysical = physicalRung.peakCrossoverForce,
        peakForceAtRigid = rigid.peakCrossoverForce
    )

    val ordered = sweep.sortedBy { it.linkStiffness }
    val monotone = (0 until ordered.size - 1).all {
        ordered[it].bestDishingOverStroke >= ordered[it + 1].bestDishingOverStroke - 1e-12
    }

    // ------------------------------------------------------------------------- the reproductions
    val reproductions = listOf(
        T9bReproduction(
            "C-0090's recommended 34-root dishing at the rigid link",
            c0090Published, rigid.bestDishingOverStroke,
            abs(rigid.bestDishingOverStroke - c0090Published) / abs(c0090Published),
            "C-0090 / T-153"
        ),
        T9bReproduction(
            "the buildable seamless width, nm", 38.08, edgeX, abs(edgeX - 38.08) / 38.08, "C-0086"
        ),
        T9bReproduction(
            "crossover columns at phase 8", 8.0, host.columns.size.toDouble(),
            abs(host.columns.size - 8.0) / 8.0, "C-0015 / C-0090"
        ),
        T9bReproduction(
            // NOT 49: 49 is the SEVEN-column count (a 4/3 parity split over 14 interfaces), which
            // is the lattice C-0157's oxDNA tile was generated on -- x = 8 + 16k, k = 0..6, the
            // row-end column REFUSED.  C-0090's and C-0099's object, and this one, admits the
            // row-end column, so phase 8 carries EIGHT columns splitting 4/4 and 14 x 4 = 56.
            "crossovers built on the sheet, row-end column ADMITTED",
            56.0, host.lattice(penalty).crossovers.size.toDouble(),
            abs(host.lattice(penalty).crossovers.size - 56.0) / 56.0, "C-0090 / C-0099"
        ),
        T9bReproduction(
            "C-0157's 49 against rasterColumnLayout's row-end-REFUSED reading, same " +
                    "width and phase -- a NON-reproduction, and the finding",
            49.0,
            OrigamiGrillage(
                sheet = sheet, lengthX = edgeX, beamCount = T9B_DUPLEXES,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = rasterColumnLayout(
                    T9B_PHASE, sheet, edgeX, false, CrossoverLayout.EDGE_MARGIN
                )
            ).crossovers.size.toDouble(),
            abs(
                OrigamiGrillage(
                    sheet = sheet, lengthX = edgeX, beamCount = T9B_DUPLEXES,
                    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                    columns = rasterColumnLayout(
                        T9B_PHASE, sheet, edgeX, false, CrossoverLayout.EDGE_MARGIN
                    )
                ).crossovers.size - 49.0
            ) / 49.0,
            "C-0157 / CLAUDE.md"
        ),
        T9bReproduction(
            "the corpus's k_theta, pN nm/rad", 13.5294118, Gen1Tile.crossoverHingeStiffness(),
            abs(Gen1Tile.crossoverHingeStiffness() - 13.5294118) / 13.5294118, "C-0009 / C-0157"
        )
    )

    // ---------------------------------------------------------------------------- the falsifiers
    val f1 = reproductions[0].departure > 1e-6
    val f2 = (sweep + rigid + absentRecord).any { abs(it.uniformLoadDishing) > 1e-3 }
    val f3 = !monotone
    val f4 = abs(ordered.first().bestDishingOverStroke - ordered.last().bestDishingOverStroke) <
            1e-9
    val f5 = penaltyFractionOf(physical) >= C0099_UNRESOLVED_PENALTY_FRACTION

    val falsifiers = listOf(
        T9bFalsifier(
            "F1", "the rigid rung does not reproduce C-0090's published 34-root dishing, so the " +
                    "object swept is not the tile the corpus is about", f1,
            "departure ${reproductions[0].departure.roundedForProse()}"
        ),
        T9bFalsifier(
            "F2", "a uniform load on the free lattice dishes materially at some rung, so the " +
                    "solver is wrong (CLAUDE.md's standing falsifier)", f2,
            "worst |uniform-load dishing| = " +
                    (sweep + rigid + absentRecord).maxOf { abs(it.uniformLoadDishing) }
                        .roundedForProse()
        ),
        T9bFalsifier(
            "F3", "the response is not monotone in the link stiffness, so there is no threshold " +
                    "and no bisection on it is meaningful (C-0070's discipline)", f3,
            "monotone over ${ordered.size} rungs: $monotone"
        ),
        T9bFalsifier(
            "F4", "the response is flat over the whole four decades, so the sweep measures " +
                    "nothing", f4,
            "soft end ${ordered.first().bestDishingOverStroke.roundedForProse()} against stiff " +
                    "end ${ordered.last().bestDishingOverStroke.roundedForProse()}"
        ),
        T9bFalsifier(
            "F5", "the derived vertical stiffness falls outside C-0099's unresolved bracket, so " +
                    "the cheap bound's central arithmetic is wrong", f5,
            "penalty fraction ${penaltyFractionOf(physical).roundedForProse()} against " +
                    "${C0099_UNRESOLVED_PENALTY_FRACTION}"
        )
    )

    val findings = mapOf(
        "theTrajectoryCannotAnswerIt" to (
                "Five checks and the run is unreadable for this question on all five: " +
                        "build-oxdna/ is absent, the 649 MB of frames were pruned, the host was " +
                        "an Apple M1 where this box is Linux, the retained result file carries " +
                        "no vertical field, and NO ESTIMATOR EXISTS EVEN IF THE FRAMES RETURNED " +
                        "— interduplex_roll.py computes angles and analyse_tile.py takes a norm. " +
                        "The fifth is the one that matters, because it is the only one a " +
                        "retained trajectory would not have cured."
                ),
        "theRunThatCouldIsTheRunTHATALREADYEXISTS" to (
                "The vertical link offset is a LOCAL coordinate at a crossover site, like the " +
                        "roll C-0157 read to better than 10 % across five replicas — not a " +
                        "global plate mode, which is what cost 12-55 h per replica. So the " +
                        "protocol that answers it is C-0157's own, unchanged, plus a six-line " +
                        "estimator and the discipline of not pruning: about a day. The caveat " +
                        "runs the other way and is stated: a softer restraint decorrelates " +
                        "slower, so the run must emit the coordinate's own autocorrelation time."
                ),
        "thePhysicalValueSitsInTheGapC0099LEFTOPEN" to (
                "This repository derives a crossover's DISPLACEMENT stiffness — 2 alpha S/(100 a) " +
                        "= ${physical.roundedForProse()} pN/nm — for the IN-PLANE axis and " +
                        "asserts a rigid constraint on the OUT-OF-PLANE one, and the two axes " +
                        "are the same two phosphate bonds. The penalty is " +
                        "${(penalty / physical).roundedForProse()}x that value, i.e. a fraction " +
                        "${penaltyFractionOf(physical).roundedForProse()} of it, which lies " +
                        "INSIDE C-0099's bisection bracket [0, 0.015625] — the one interval it " +
                        "did not resolve before calling the response a discontinuity."
                ),
        "whatTheSweepMeasures" to (
                "The link ALONE, hinge intact, at all 56 crossovers of the row-end-admitted phase-8 " +
                        "lattice. C-0099 swept the 14 row-end " +
                        "crossovers and scaled both elements together, so this channel has never " +
                        "been run. Best 34-root dishing: " +
                        "${rigid.bestDishingOverStroke.roundedForProse()} rigid, " +
                        "${physicalRung.bestDishingOverStroke.roundedForProse()} at the derived " +
                        "value, ${absentRecord.bestDishingOverStroke.roundedForProse()} with the " +
                        "link deleted — a ramp fraction of " +
                        "${verdict.rampFraction.roundedForProse()} against the " +
                        "$RAMP_FRACTION_THRESHOLD the binary reading is allowed."
                ),
        "theRegistrationLeverIsWhatC0015OWNS" to (
                "C-0015's governing variable is THE DISTANCE FROM THE ATTACHMENT TO THE NEAREST " +
                        "CROSSOVER, and that curve exists only because a rigid vertical tie " +
                        "localises the reaction there. Re-measured at every rung as the spread " +
                        "of the peak crossover force over one registration cell at C-0015's own " +
                        "nine stations: ${rigid.registrationLever!!.roundedForProse()} rigid, " +
                        "${physicalRung.registrationLever!!.roundedForProse()} at the derived " +
                        "value, and with the link DELETED there is no lever at all because " +
                        "every crossover carries EXACTLY zero force. This is corroboration and " +
                        "NOT the criterion: V3 was fixed on the peak force itself, before the " +
                        "sweep."
                ),
        "theV2ThresholdWasMISTRANSCRIBEDANDBOTHAREPUBLISHED" to (
                "T-9's Plan registered V2 as 'three percentage points of the convention' and " +
                        "wrote the number 0.030, which is a factor of ten out: three percentage " +
                        "points OF 0.10 is 0.0030, and C-0099's own two emitted readings differ " +
                        "by 0.0030284749 of the stroke. The corrected threshold is that " +
                        "difference; the registered one is retained as " +
                        "ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN and BOTH verdicts are emitted. " +
                        "The measured movement is " +
                        "${verdict.dishingMovement.roundedForProse()} of the stroke, " +
                        "${verdict.dishingMovementOverTheRowEndUnknown.roundedForProse()}x the " +
                        "row-end unknown — so V2 fires on the corrected threshold and not on " +
                        "the registered one, and V1, V3 and V4 are the same under both."
                ),
        "THESIMULATEDTILEISNOTTHEGRADEDTILE" to (
                "At 38.08 nm and phase 8 rasterColumnLayout puts its columns on the EVEN junction " +
                        "planes and gives 8 columns admitted (a 4/4 split over 14 interfaces, 56 " +
                        "crossovers) and 6 refused (3/3, 42). C-0157's oxDNA generator built 7 " +
                        "columns at x = 8 + 16k, k = 0..6, and 49 crossovers. NEITHER grillage " +
                        "reading is 7, so the simulated tile and the tile every placement result " +
                        "in this corpus is graded on are not the same object: their column sets " +
                        "are one 8 bp PLANE apart, which is CLAUDE.md's own 'a shift by one " +
                        "column pitch hands every interface the OTHER parity's columns -- a " +
                        "physically different sheet', met between a simulation and the lattice " +
                        "its answer would be read against. This study does not resolve which is " +
                        "right; it emits both counts and lists it open."
                ),
        "theLinkExtensionIsALENGTH" to (
                "At the penalty the peak link extension is " +
                        "${rigid.peakLinkExtension!!.roundedForProse()} nm and at the derived " +
                        "value ${physicalRung.peakLinkExtension!!.roundedForProse()} nm, against " +
                        "the 0.34 nm rise which is the smallest length the design language can " +
                        "draw. A constraint whose extension is orders below the lattice quantum " +
                        "is a constraint; one whose extension approaches it is a spring."
                )
    )

    val result = T9bResult(
        task = "T-9",
        deliverable = "second of three — the crossover's vertical/axial compliance",
        leaf = "A1.2, with A8.2",
        title = "The crossover's vertical link: the criterion the corpus never had, the " +
                "physical value in the interval C-0099 left unresolved, and the statement " +
                "that C-0157's trajectory cannot answer it",
        verificationType = "in-silico (structure.OrigamiGrillage, the link stiffness swept " +
                "globally over C-0020's own four decades, C-0090's exhaustive centro-symmetric " +
                "enumeration at every rung) + logical (a cheap bound that is five file checks " +
                "and three divisions, and settles the shape of the answer before any solve)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated, and " +
                "NOTHING about a crossover's vertical stiffness has been measured anywhere: the " +
                "value swept is a CONSTRUCTION this repository already applies to the in-plane " +
                "axis, and it is reported over four decades exactly as C-0020 reports that one.",
        units = mapOf(
            "length" to "nm", "force" to "pN", "stiffness" to "pN/nm",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "kBT" to "4.141947 pN*nm at 300 K",
            "axes" to "x along the helices, y across them, z normal and positive upward",
            "linkCoordinate" to "e = (w_b + (d/2) phi_b) - (w_{b+1} - (d/2) phi_{b+1}), the " +
                    "relative out-of-plane displacement of the two duplex SURFACES facing each " +
                    "other across the interface — OrigamiGrillage.linkExtension",
            "notTheRoll" to "the roll is the dihedral C-0157 measured; the interhelical distance " +
                    "is the in-plane separation whose sawtooth C-0157 reproduced; this is neither",
            "tile" to "38.08 nm x 40.35 nm, 15 duplexes, crossover phase 8, 8 columns, 49 " +
                    "crossovers, under C-0022's solved collar at 2 mM / 10 nm / 0.192 V"
        ),
        sources = listOf(
            "C-0157 (T-9's first deliverable, and the run whose trajectory this needed)",
            "C-0009 (the grillage, and the rigid vertical link)",
            "C-0099 (the penalty step, and the bisection bracket it left unresolved)",
            "C-0100 (the only two physical states of a constraint are present and absent)",
            "C-0015 (the registration design rule the vertical link's rigidity underwrites)",
            "C-0020 (the softened-bond displacement construction, and its four-decade sweep)",
            "C-0090 / T-153 (the 38.08 nm phase-8 lattice and its recommended placement)",
            "C-0022 / T-3b (the solved edge collar)"
        ),
        trajectoryCheck = trajectoryCheck,
        theRunThatCouldAnswerIt = theRun,
        cheapBounds = cheapBounds,
        sweep = sweep,
        absentLinkReading = absentRecord,
        rigidLinkReading = rigid,
        physicalLinkReading = physicalRung,
        verdict = verdict,
        monotoneInLinkStiffness = monotone,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "THE SWEPT VALUE IS A CONSTRUCTION, NOT A MEASUREMENT. 2 alpha S/(100 a) is Chen " +
                    "et al.'s softened-bond heuristic on the duplex stretch modulus, which " +
                    "C-0020 already applies to the in-plane axis and already sweeps four " +
                    "decades. Nothing here measures a crossover's vertical stiffness.",
            "THE CONSTRUCTION'S AXIS IS AN ASSUMPTION. The in-plane connector resists sliding " +
                    "ALONG the helices; the vertical link resists an offset NORMAL to the sheet. " +
                    "Both are relative displacements of the same two duplexes carried by the " +
                    "same two phosphate bonds, and that is the whole argument for transferring " +
                    "the constant. It is a transfer, and the four-decade sweep is what it is for.",
            "THE LATTICE IS SINGLE-LAYER SQUARE. Every number here is a property of C-0009's " +
                    "grillage, whose crossover combinatorics are square-lattice and whose " +
                    "interfaces are a path graph; none of it transfers to a honeycomb block " +
                    "(CLAUDE.md, C-0154).",
            "THE LOAD IS C-0022's SOLVED COLLAR AT ONE STATE, 2 mM / 10 nm / 0.192 V. A " +
                    "flatness verdict needs an operating state as well as a load case and this " +
                    "is one state, the one C-0090 and C-0099 are read at.",
            "THE DISHING IS A PEAK OVER A BEST-FIT PLANE, minimised over C-0090's " +
                    "centro-symmetric family with C-0090's own tie-break. It is not an energy, " +
                    "so monotonicity in the swept variable is MEASURED and not assumed."
        ),
        openQuestions = listOf(
            "The measurement itself: the vertical offset's variance at a crossover, from an " +
                    "oxDNA run that retains its frames — priced above, and it is the run that " +
                    "already exists plus an estimator.",
            "T-9's THIRD deliverable, the in-plane shear k_s, is untouched: C-0020's single " +
                    "undetermined input, which C-0028 shows moves a buckling verdict — and it " +
                    "is the SAME constant as the one transferred here, so one measurement " +
                    "settles both axes only if the transfer is right.",
            "Whether a crossover's vertical restraint is a spring at all, or a DEAD BAND then " +
                    "rigid: CLAUDE.md records that a flexible link pays its axial slack back as " +
                    "a transverse dead band of the same size, and a linear stiffness cannot " +
                    "express that. The trajectory would show it directly as a non-Gaussian " +
                    "offset distribution, which is one more reason the frames matter."
        )
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val encoded = json.encodeToJsonElement(result)
    val out = File("gpd/results/T-9b-crossover-vertical-compliance.json")
    out.parentFile.mkdirs()
    out.writeText(json.encodeToString(encoded.roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)))
    println("T-9b — written to ${out.path}")
    println("T-9b — verdict: binary reading right = ${verdict.binaryReadingIsRight} " +
            "(as first written: ${verdict.binaryReadingIsRightAsFirstWritten}), " +
            "ramp fraction = ${verdict.rampFraction}, V1 = ${verdict.crossesFlatnessConvention}, " +
            "V2 = ${verdict.movesMoreThanTheRowEndUnknown}, " +
            "V3 = ${verdict.movesThePeakCrossoverForce}, V4 = ${verdict.isARampNotAStep}")
    println("T-9b — thresholds: T-5b $FLATNESS_CONVENTION, margin $ROW_END_UNKNOWN_MARGIN, " +
            "force $REGISTRATION_FORCE_THRESHOLD, ramp $RAMP_FRACTION_THRESHOLD")
}
