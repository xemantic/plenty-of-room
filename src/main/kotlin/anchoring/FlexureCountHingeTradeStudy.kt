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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * Task `T-99` / leaf `A8.2` — **does a coupling of FEWER, LONGER flexures close where 45 short
 * ones do not?**
 *
 * ```shell
 * tools/study.sh anchoring.FlexureCountHingeTradeStudyKt
 * ```
 *
 * Emits `gpd/results/T-99-flexure-count-hinge-trade.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val DESIRED_PLACEMENT = TARGET_FORCE / DESIRED_STROKE
private const val COMPLIANT_CEILING = 40.0
private const val UNZIP_ALLOWABLE = 10.0
private const val STEPS = 400

/** `C-0015`'s crossover inventory: 56 at the ten eight-column phases, 49 at the other 22. */
private const val INVENTORY_BEST = 56
private const val INVENTORY_WORST = 49

/** `C-0017`'s stability floor at the 10 nm layer in 2 mM MgCl₂ — `|k_eff|`, CITED. */
private const val STABILITY_FLOOR_LOW = 23.41
private const val STABILITY_FLOOR_HIGH = 27.91

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val HINGE = Gen1Tile.crossoverHingeStiffness()
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val TILE = Gen1Tile.EDGE_X

/** The Gen-1 sheet's duplex count — `C-0015`'s 15 rows at the SAXS 2.69 nm pitch. */
private const val DUPLEXES = 15

/** `C-0034`'s adopted `A2` anchorage — the arm's own duplex end, two strand termini. */
private val FAR = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS).rotationalStiffness

// ---------------------------------------------------------------------------- records

@Serializable
data class T99BoundRecord(
    val name: String,
    val value: Double,
    val statement: String
)

@Serializable
data class T99LedgerRecord(
    val pathCount: Int,
    val hingeCount: Int,
    val crossoverDemand: Int,
    val crossoverSupplyBest: Int,
    val crossoverSupplyWorst: Int,
    val lineDemand: Double,
    val lineSupply: Double,
    val admittedByInventory: Boolean,
    val admittedByCensus: Boolean
)

@Serializable
data class T99TradeRecord(
    val placement: String,
    val pathCount: Int,
    val hingeCount: Int,
    val crossoverDemand: Int,
    val hingeLineLength: Double,
    val armLength: Double,
    val armLengthBasePairs: Double,
    val rigidHingeCap: Double,
    val rigidArmBound: Double,
    val secantAtWorking: Double,
    val tangentAtWorking: Double,
    val secantAtTarget: Double,
    val tangentAtTarget: Double,
    val forcePerPathAtWorking: Double,
    val forcePerPathAtTarget: Double,
    val hingeBondForceAtWorking: Double,
    val usableStroke: Double,
    val reachesTargetGeometrically: Boolean,
    val insideCeilingAtWorking: Boolean,
    val insideCeilingOverStroke: Boolean,
    val bindingAtWorkingPointReading: List<String>,
    val bindingAtWholeStrokeReading: List<String>,
    val feasibleAtWorkingPointReading: Boolean,
    val feasibleAtWholeStrokeReading: Boolean
)

@Serializable
data class T99RegionRecord(
    val placement: String,
    val ceilingReading: String,
    val candidatePoints: Int,
    val feasiblePoints: Int,
    val bestUsableStroke: Double,
    val bestPathCount: Int,
    val bestHingeCount: Int,
    val bindingConstraintAtTheBest: List<String>,
    val statement: String
)

@Serializable
data class T99ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val value: Double,
    val result: Double,
    val departure: Double
)

@Serializable
data class T99ReproductionRecord(
    val name: String,
    val expected: Double,
    val obtained: Double,
    val departure: Double
)

@Serializable
data class T99Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val parameters: Map<String, Double>,
    val bounds: List<T99BoundRecord>,
    val ledger: List<T99LedgerRecord>,
    val trade: List<T99TradeRecord>,
    val regions: List<T99RegionRecord>,
    val convergence: List<T99ConvergenceRecord>,
    val reproductions: List<T99ReproductionRecord>,
    val verdict: Map<String, String>
)

// ---------------------------------------------------------------------------- helpers

private fun constraints(
    reading: CeilingReading,
    targetStroke: Double,
    targetStiffness: Double,
    inventory: Int = INVENTORY_BEST
) = TradeConstraints(
    inventory = inventory,
    maximumHingeLineCount = maximumHingeCount(TILE, perInterfacePitch()),
    lineSupply = interfaceLineSupply(DUPLEXES, TILE),
    unzipAllowable = UNZIP_ALLOWABLE,
    ceiling = COMPLIANT_CEILING,
    ceilingReading = reading,
    targetStroke = targetStroke,
    stabilityFloor = STABILITY_FLOOR_LOW,
    targetStiffness = targetStiffness
)

private fun tradeRecord(
    placement: String,
    point: TradePoint,
    targetStroke: Double,
    targetStiffness: Double
): T99TradeRecord {
    val atWorkingPoint = bindingConstraints(
        point, constraints(CeilingReading.WORKING_POINT, targetStroke, targetStiffness)
    )
    val overStroke = bindingConstraints(
        point, constraints(CeilingReading.WHOLE_STROKE, targetStroke, targetStiffness)
    )
    return T99TradeRecord(
        placement = placement,
        pathCount = point.pathCount,
        hingeCount = point.hingeCount,
        crossoverDemand = point.crossoverDemand,
        hingeLineLength = point.hingeLineLength,
        armLength = point.armLength,
        armLengthBasePairs = point.armLengthBasePairs,
        rigidHingeCap = point.rigidHingeCap,
        rigidArmBound = point.rigidArmBound,
        secantAtWorking = point.secantAtWorking,
        tangentAtWorking = point.tangentAtWorking,
        secantAtTarget = point.secantAtTarget,
        tangentAtTarget = point.tangentAtTarget,
        forcePerPathAtWorking = point.forcePerPathAtWorking,
        forcePerPathAtTarget = point.forcePerPathAtTarget,
        hingeBondForceAtWorking = point.hingeBondForceAtWorking,
        usableStroke = point.usableStroke,
        reachesTargetGeometrically = point.reachesTargetGeometrically,
        insideCeilingAtWorking = point.insideCeilingAtWorking,
        insideCeilingOverStroke = point.insideCeilingOverStroke,
        bindingAtWorkingPointReading = atWorkingPoint,
        bindingAtWholeStrokeReading = overStroke,
        feasibleAtWorkingPointReading = atWorkingPoint.isEmpty(),
        feasibleAtWholeStrokeReading = overStroke.isEmpty()
    )
}

private fun f(value: Double, digits: Int = 3): String = "%.${digits}f".format(value)

// ---------------------------------------------------------------------------- the study

fun main() {
    val started = System.nanoTime()
    val pitch = perInterfacePitch()
    val lineSupply = interfaceLineSupply(DUPLEXES, TILE)
    val censusCeiling = maximumHingeCount(TILE, pitch)

    // ------------------------------------------------------------ cheap bounds, first

    val floorAtDesired = minimumPathCountForAllowable(MANDATE, DESIRED_STROKE, UNZIP_ALLOWABLE)
    val floorAtAcceptable = minimumPathCountForAllowable(MANDATE, ACCEPTABLE_STROKE, UNZIP_ALLOWABLE)
    val hingeAtTheFloor = maximumHingeCountForInventory(floorAtDesired, INVENTORY_BEST)
    val hingeSupplied = hingeSuppliedArmCeiling(INVENTORY_BEST, HINGE, MANDATE, ACCEPTABLE_STROKE)
    val hingeSuppliedSmall = smallRotationArmCeiling(INVENTORY_BEST * HINGE, MANDATE)
    val stabilityStroke = strokeCeilingFromStability(TARGET_FORCE, STABILITY_FLOOR_LOW)
    val stabilityStrokeTight = strokeCeilingFromStability(TARGET_FORCE, STABILITY_FLOOR_HIGH)

    val bounds = listOf(
        T99BoundRecord(
            "hinge count admitted at CH-0029's path-count floor", hingeAtTheFloor.toDouble(),
            "n h <= 56 intersected with n >= $floorAtDesired at the desired stroke leaves h = " +
                    "$hingeAtTheFloor. Fewer paths are forbidden by the allowable and longer " +
                    "hinge lines by the inventory, so the trade has ONE admissible hinge count " +
                    "at the desired stroke, before any elastica runs"
        ),
        T99BoundRecord(
            "hinge-supplied arm ceiling, exact rotation [nm]", hingeSupplied,
            "the arm the WHOLE crossover inventory can place if the far anchorage carries " +
                    "nothing. A function of the PRODUCT n h and of nothing else, so it is " +
                    "CONSTANT along the whole trade curve — the degeneracy this task exists to " +
                    "test — and it is below §3's desired 10 nm stroke"
        ),
        T99BoundRecord(
            "the same at small rotation [nm]", hingeSuppliedSmall,
            "sqrt(N k_theta/k_target), the delta -> 0 limit, which the exact reading bounds " +
                    "from above"
        ),
        T99BoundRecord(
            "combined rigid-arm ceiling at 45 paths [nm]",
            combinedArmCeiling(INVENTORY_BEST, HINGE, 45, FAR, MANDATE, ACCEPTABLE_STROKE),
            "the same bound with C-0034's A2 anchorage restored: the restraint is " +
                    "N k_theta + n k_far, which grows with the PATH count. It is NOT degenerate, " +
                    "and it is what says which way the trade runs"
        ),
        T99BoundRecord(
            "combined rigid-arm ceiling at 56 paths [nm]",
            combinedArmCeiling(INVENTORY_BEST, HINGE, 56, FAR, MANDATE, ACCEPTABLE_STROKE),
            "and at the largest path count the inventory admits at h = 1"
        ),
        T99BoundRecord(
            "collinear interface supply [nm]", lineSupply,
            "14 interior interfaces of a 15-duplex sheet plus its 2 free edges, at 40 nm each"
        ),
        T99BoundRecord(
            "the stroke C-0017's stability floor alone permits at 100 pN [nm]", stabilityStroke,
            "placement is k_c = F/delta and stability is k_c > |k_eff| = " +
                    "$STABILITY_FLOOR_LOW-$STABILITY_FLOOR_HIGH pN/nm at the 10 nm layer in " +
                    "2 mM, so the two together cap the stroke at " +
                    "${f(stabilityStrokeTight)}-${f(stabilityStroke)} nm with no reference to " +
                    "what the coupling is made of"
        )
    )

    // ------------------------------------------------------------ the two ledgers

    val pathCounts = listOf(8, 10, 12, 14, 15, 19, 25, 28, 34, 45, 50, 56, 60)
    val hingeCounts = listOf(1, 2, 3, 4, 6)

    val ledger = pathCounts.flatMap { paths ->
        hingeCounts.map { hinges ->
            T99LedgerRecord(
                pathCount = paths,
                hingeCount = hinges,
                crossoverDemand = hingeCrossoverDemand(paths, hinges),
                crossoverSupplyBest = INVENTORY_BEST,
                crossoverSupplyWorst = INVENTORY_WORST,
                lineDemand = collinearLineDemand(paths, hinges, pitch),
                lineSupply = lineSupply,
                admittedByInventory = hingeCrossoverDemand(paths, hinges) <= INVENTORY_BEST,
                admittedByCensus = hinges <= censusCeiling
            )
        }
    }

    // ------------------------------------------------------------ the joint sweep

    // Every point the ledgers admit, plus the two upstream design points as reproductions.
    val sweepPoints = ledger
        .filter { it.admittedByInventory && it.admittedByCensus }
        .map { it.pathCount to it.hingeCount }
    val reproductionPoints = listOf(45 to 16, 15 to 16)

    val unplaceable = mutableListOf<String>()

    val trade = buildList {
        (sweepPoints + reproductionPoints).forEach { (paths, hinges) ->
            val point = runCatching {
                tradePoint(
                    paths, hinges, FAR, MANDATE, ACCEPTABLE_STROKE, DESIRED_STROKE,
                    HINGE, EI, COMPLIANT_CEILING, pitch, STEPS
                )
            }.getOrNull()
            if (point == null) unplaceable += "P3 ($paths, $hinges)"
            else add(
                tradeRecord(
                    "P3 — secant 33.3333 pN/nm at 3 nm (the standing convention)",
                    point, DESIRED_STROKE, MANDATE
                )
            )
        }
        sweepPoints.forEach { (paths, hinges) ->
            val point = runCatching {
                tradePoint(
                    paths, hinges, FAR, DESIRED_PLACEMENT, DESIRED_STROKE, DESIRED_STROKE,
                    HINGE, EI, COMPLIANT_CEILING, pitch, STEPS
                )
            }.getOrNull()
            if (point == null) unplaceable += "P10 ($paths, $hinges)"
            else add(
                tradeRecord(
                    "P10 — secant 10 pN/nm at 10 nm (C-0017's arithmetic on §3's desired clause)",
                    point, DESIRED_STROKE, DESIRED_PLACEMENT
                )
            )
        }
    }

    // ------------------------------------------------------------ the regions

    fun region(placement: String, reading: CeilingReading): T99RegionRecord {
        val candidates = trade.filter { it.placement.startsWith(placement) }
        val feasible = candidates.filter {
            if (reading == CeilingReading.WORKING_POINT) it.feasibleAtWorkingPointReading
            else it.feasibleAtWholeStrokeReading
        }
        val best = candidates.maxByOrNull { it.usableStroke }
            ?: return T99RegionRecord(
                placement, reading.name, 0, 0, T99_UNREACHABLE, 0, 0, listOf("no placement"),
                "no point of this placement admits an arm at all"
            )
        return T99RegionRecord(
            placement = placement,
            ceilingReading = reading.name,
            candidatePoints = candidates.size,
            feasiblePoints = feasible.size,
            bestUsableStroke = best.usableStroke,
            bestPathCount = best.pathCount,
            bestHingeCount = best.hingeCount,
            bindingConstraintAtTheBest =
                if (reading == CeilingReading.WORKING_POINT) best.bindingAtWorkingPointReading
                else best.bindingAtWholeStrokeReading,
            statement = if (feasible.isEmpty())
                "EMPTY at §3's desired 10 nm stroke; the best point reaches " +
                        "${f(best.usableStroke)} nm inside the ceiling at " +
                        "${best.pathCount} paths and ${best.hingeCount} crossovers"
            else "${feasible.size} of ${candidates.size} points reach the desired stroke"
        )
    }

    val regions = listOf(
        region("P3", CeilingReading.WORKING_POINT),
        region("P3", CeilingReading.WHOLE_STROKE),
        region("P10", CeilingReading.WORKING_POINT),
        region("P10", CeilingReading.WHOLE_STROKE)
    )

    // and the same question at §3's ACCEPTABLE stroke, which is a different acceptance clause
    val acceptableFeasible = trade
        .filter { it.placement.startsWith("P3") && it.crossoverDemand <= INVENTORY_BEST }
        .filter { it.hingeCount <= censusCeiling }
        .filter { it.usableStroke >= ACCEPTABLE_STROKE && it.forcePerPathAtWorking <= UNZIP_ALLOWABLE }

    // ------------------------------------------------------------ convergence

    val convergence = buildList {
        listOf(200, 400, 800).forEach { steps ->
            val point = tradePoint(
                45, 1, FAR, MANDATE, ACCEPTABLE_STROKE, DESIRED_STROKE,
                HINGE, EI, COMPLIANT_CEILING, pitch, steps
            )
            add(
                T99ConvergenceRecord(
                    "the placed arm at 45 paths on one crossover", "RK4 steps", steps.toDouble(),
                    point.armLength, abs(point.armLength - 9.0) / 9.0
                )
            )
            add(
                T99ConvergenceRecord(
                    "the usable stroke there", "RK4 steps", steps.toDouble(),
                    point.usableStroke, 0.0
                )
            )
        }
        val beam = TwoSpringElastica(EI, 9.5, HINGE, FAR, STEPS)
        val reference = usableStrokeInsideCeiling(beam, 45, COMPLIANT_CEILING, DESIRED_STROKE, 240)
        listOf(30, 60, 90, 120, 240).forEach { samples ->
            val value = usableStrokeInsideCeiling(
                beam, 45, COMPLIANT_CEILING, DESIRED_STROKE, samples
            )
            add(
                T99ConvergenceRecord(
                    "the usable stroke of a 9.5 nm arm", "scan samples", samples.toDouble(),
                    value, abs(value - reference) / reference
                )
            )
        }
        // the rigid-arm ceiling against its own defining equation
        listOf(757.6, 4278.2, 20000.0).forEach { restraint ->
            val arm = rigidArmCeiling(restraint, MANDATE, ACCEPTABLE_STROKE)
            val secant = rigidArmSecant(restraint, arm, ACCEPTABLE_STROKE)
            add(
                T99ConvergenceRecord(
                    "the rigid-arm ceiling reproduces the mandate", "restraint [pN nm/rad]",
                    restraint, secant, abs(secant - MANDATE) / MANDATE
                )
            )
        }
    }

    // ------------------------------------------------------------ upstream reproductions

    fun reproduce(name: String, expected: Double, obtained: Double) = T99ReproductionRecord(
        name, expected, obtained, abs(obtained - expected) / abs(expected)
    )

    val adopted = trade.first { it.placement.startsWith("P3") && it.pathCount == 45 && it.hingeCount == 16 }
    val fifteen = trade.first { it.placement.startsWith("P3") && it.pathCount == 15 && it.hingeCount == 16 }

    val reproductions = listOf(
        reproduce("C-0039's E5a16 arm [nm]", 12.7198, adopted.armLength),
        reproduce("C-0039's tangent at the acceptable stroke", 36.44, adopted.tangentAtWorking),
        reproduce("C-0039's secant at the desired stroke", 69.94, adopted.secantAtTarget),
        reproduce("C-0039's tangent at the desired stroke", 264.24, adopted.tangentAtTarget),
        reproduce("C-0039's usable stroke [nm]", 3.877, adopted.usableStroke),
        reproduce("C-0039's 15-path arm [nm]", 8.40, fifteen.armLength),
        reproduce("C-0039's arm cap at 45 paths [nm]", 13.648, adopted.rigidHingeCap),
        reproduce(
            "C-0040's hinge-line census on a 40 nm line", 4.0,
            maximumHingeCount(TILE, pitch).toDouble()
        ),
        reproduce(
            "C-0040's line demand for sixteen crossovers [nm]", 163.2,
            collinearLineDemand(1, 16, pitch)
        ),
        reproduce(
            "C-0041's / CH-0029's path-count floor at the desired stroke", 34.0,
            floorAtDesired.toDouble()
        ),
        reproduce("C-0009's crossover hinge constant [pN nm/rad]", 13.5294118, HINGE),
        reproduce("C-0029's two-terminus couple [pN nm/rad]", 78.2352941, FAR),
        reproduce("C-0017's mandate [pN/nm]", 100.0 / 3.0, MANDATE),
        reproduce(
            "C-0015's inventory at the ten eight-column phases", 56.0,
            INVENTORY_BEST.toDouble()
        )
    )

    // ------------------------------------------------------------ the verdict

    val p3Whole = regions.first { it.placement == "P3" && it.ceilingReading == "WHOLE_STROKE" }
    val p3Working = regions.first { it.placement == "P3" && it.ceilingReading == "WORKING_POINT" }
    val p10Whole = regions.first { it.placement == "P10" && it.ceilingReading == "WHOLE_STROKE" }
    val p10Working = regions.first { it.placement == "P10" && it.ceilingReading == "WORKING_POINT" }
    val bestAcceptable = acceptableFeasible.maxByOrNull { it.usableStroke }

    val verdict = buildMap {
        put(
            "the trade is degenerate on its own axis",
            "The hinge-supplied arm ceiling depends on the PRODUCT n h and on nothing else, so " +
                    "it is ${f(hingeSupplied)} nm at EVERY point of the n h = $INVENTORY_BEST " +
                    "trade curve — a flexure array cannot buy arm length by moving crossovers " +
                    "from one hinge to another. What is NOT degenerate is C-0034's far " +
                    "anchorage, whose couple is per-FLEXURE: the array's restraint is " +
                    "N k_theta + n k_far, so the placed arm GROWS with the path count and the " +
                    "trade runs OPPOSITE to the premise of the task. Fewer, longer flexures are " +
                    "strictly worse than more, shorter ones."
        )
        put(
            "the desired stroke, at both readings of C-0023's ceiling",
            "P3 (the standing placement): ${p3Working.feasiblePoints} of " +
                    "${p3Working.candidatePoints} points at the WORKING-POINT reading and " +
                    "${p3Whole.feasiblePoints} of ${p3Whole.candidatePoints} at the " +
                    "WHOLE-STROKE reading. P10 (§3's own arithmetic for the desired clause): " +
                    "${p10Working.feasiblePoints} and ${p10Whole.feasiblePoints}. The best " +
                    "point anywhere reaches ${f(p3Whole.bestUsableStroke)} nm inside the " +
                    "ceiling. T-107's question does not move this verdict: the desired stroke " +
                    "fails at BOTH readings, and it fails on the geometry and the allowable " +
                    "before the ceiling is consulted."
        )
        put(
            "why P10 is not an escape",
            "Placing for §3's desired clause at its own force gives k_c = " +
                    "${f(DESIRED_PLACEMENT)} pN/nm, which is " +
                    "${f(STABILITY_FLOOR_LOW / DESIRED_PLACEMENT, 2)}-" +
                    "${f(STABILITY_FLOOR_HIGH / DESIRED_PLACEMENT, 2)}x BELOW C-0017's " +
                    "stability floor at the 10 nm layer in 2 mM. Stability and placement " +
                    "together cap the stroke at ${f(stabilityStrokeTight)}-" +
                    "${f(stabilityStroke)} nm at 100 pN, with no reference to the coupling's " +
                    "construction at all."
        )
        put(
            "points at which the placement cannot be discharged at all",
            if (unplaceable.isEmpty()) "none"
            else "${unplaceable.size}: ${unplaceable.joinToString(", ")} — the array is softer " +
                    "than its own placement target even at the shortest admissible arm, which " +
                    "is a statement about the path count and not a solver failure"
        )
        put(
            "what the branch DOES deliver",
            if (bestAcceptable == null)
                "no point of the trade clears §3's acceptable stroke either"
            else "§3's ACCEPTABLE 3 nm stroke is met with margin: " +
                    "${acceptableFeasible.size} points of the ledger-admitted sweep clear it, " +
                    "the best at ${bestAcceptable.pathCount} paths and " +
                    "${bestAcceptable.hingeCount} crossovers with a usable stroke of " +
                    "${f(bestAcceptable.usableStroke)} nm, an arm of " +
                    "${f(bestAcceptable.armLength)} nm = " +
                    "${f(bestAcceptable.armLengthBasePairs, 1)} bp and " +
                    "${f(bestAcceptable.forcePerPathAtWorking)} pN per path against the 10 pN " +
                    "unzip allowable."
        )
    }

    val result = T99Result(
        task = "T-99",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K, k_BT = 4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to "40 x 40 nm single-layer square-lattice Rothemund sheet, 15 duplexes",
            "element" to "C-0034's E5a on C-0039's inextensible two-spring elastica, free to " +
                    "draw in (H = 0), far anchorage A2 = the arm's own duplex end",
            "maturity" to "TRL 1-3; nothing here is measured and the motif is not demonstrated"
        ),
        parameters = mapOf(
            "targetForce" to TARGET_FORCE,
            "acceptableStroke" to ACCEPTABLE_STROKE,
            "desiredStroke" to DESIRED_STROKE,
            "mandate" to MANDATE,
            "desiredPlacement" to DESIRED_PLACEMENT,
            "complianceCeiling" to COMPLIANT_CEILING,
            "unzipAllowable" to UNZIP_ALLOWABLE,
            "hingeStiffness" to HINGE,
            "farStiffness" to FAR,
            "bendingRigidity" to EI,
            "perInterfacePitch" to pitch,
            "interfaceLineSupply" to lineSupply,
            "crossoverInventoryBest" to INVENTORY_BEST.toDouble(),
            "crossoverInventoryWorst" to INVENTORY_WORST.toDouble(),
            "censusCeiling" to censusCeiling.toDouble(),
            "pathCountFloorAtDesiredStroke" to floorAtDesired.toDouble(),
            "pathCountFloorAtAcceptableStroke" to floorAtAcceptable.toDouble(),
            "stabilityFloorLow" to STABILITY_FLOOR_LOW,
            "stabilityFloorHigh" to STABILITY_FLOOR_HIGH
        ),
        bounds = bounds,
        ledger = ledger,
        trade = trade,
        regions = regions,
        convergence = convergence,
        reproductions = reproductions,
        verdict = verdict
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-99-flexure-count-hinge-trade.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(
            digitsByKey = DEPARTURE_DIGITS_BY_KEY
        )) + "\n"
    )

    // ---------------------------------------------------------------- console

    println("=== T-99 — do FEWER, LONGER flexures close where 45 short ones do not? ".padEnd(112, '='))
    println()
    println("--- the cheap bounds, which ran first ".padEnd(112, '-'))
    bounds.forEach { println("%58s %12.4f".format(it.name.take(58), it.value)) }
    println()
    println("--- the trade, at the standing placement P3 ".padEnd(112, '-'))
    println(
        "%5s %5s %7s %8s %8s %9s %9s %9s %8s %s".format(
            "n", "h", "n*h", "arm", "bp", "tan(3)", "sec(10)", "tan(10)", "usable", "binding"
        )
    )
    trade.filter { it.placement.startsWith("P3") }.forEach {
        println(
            "%5d %5d %7d %8.3f %8.1f %9.2f %9.2f %9.1f %8.3f %s".format(
                it.pathCount, it.hingeCount, it.crossoverDemand, it.armLength,
                it.armLengthBasePairs, it.tangentAtWorking, it.secantAtTarget,
                it.tangentAtTarget, it.usableStroke,
                it.bindingAtWholeStrokeReading.joinToString("; ").take(44)
            )
        )
    }
    println()
    println("--- the trade, at the desired-clause placement P10 ".padEnd(112, '-'))
    trade.filter { it.placement.startsWith("P10") }.forEach {
        println(
            "%5d %5d %7d %8.3f %8.1f %9.2f %9.2f %9.1f %8.3f %s".format(
                it.pathCount, it.hingeCount, it.crossoverDemand, it.armLength,
                it.armLengthBasePairs, it.tangentAtWorking, it.secantAtTarget,
                it.tangentAtTarget, it.usableStroke,
                it.bindingAtWholeStrokeReading.joinToString("; ").take(44)
            )
        )
    }
    println()
    println("--- the regions ".padEnd(112, '-'))
    regions.forEach {
        println(
            "%4s %14s  %2d of %2d feasible; best %6.3f nm at (%d, %d)".format(
                it.placement, it.ceilingReading, it.feasiblePoints, it.candidatePoints,
                it.bestUsableStroke, it.bestPathCount, it.bestHingeCount
            )
        )
    }
    println()
    println("--- upstream reproductions ".padEnd(112, '-'))
    reproductions.forEach {
        println("%56s %12.6f %12.6f %10.2e".format(
            it.name.take(56), it.expected, it.obtained, it.departure
        ))
    }
    println()
    verdict.forEach { (key, value) -> println("$key: $value"); println() }
    println("written: ${output.path} in %.1f s".format((System.nanoTime() - started) / 1e9))

    // ------------------------------------------------- the falsifiers, as runtime checks
    check(hingeAtTheFloor <= 2) {
        "declared falsifier 1: if the inventory admitted three or more crossovers per flexure " +
                "at CH-0029's path-count floor, the trade would be a real two-dimensional " +
                "optimisation rather than a degenerate one"
    }
    check(hingeSupplied < DESIRED_STROKE) {
        "declared falsifier 2: if the whole crossover inventory could place an arm longer than " +
                "§3's desired stroke, the hinge budget would not bind the geometry at all"
    }
    check(abs(adopted.armLength - 12.7198) / 12.7198 < 1e-4) {
        "declared falsifier 3: the sweep must reproduce C-0039's adopted E5a16 placement, or " +
                "the pipeline is not the one whose verdict is being extended"
    }
    check(
        trade.filter { it.placement.startsWith("P3") }
            .filter { it.hingeCount == 1 }
            .sortedBy { it.pathCount }
            .zipWithNext()
            .all { (a, b) -> b.armLength > a.armLength }
    ) {
        "the placed arm must grow with the path count at a fixed hinge count, or the direction " +
                "of the trade is not established"
    }
    check(regions.all { it.feasiblePoints == 0 }) {
        "if any point of the swept region reached §3's desired stroke this claim's headline " +
                "would be the opposite one, and the region record would have to name it"
    }
    check(convergence.filter { it.parameter == "scan samples" }.all { it.departure < 1e-5 }) {
        "the usable stroke must be independent of the scan sample count"
    }
}
