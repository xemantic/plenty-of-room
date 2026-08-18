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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.electrostatics.DEFAULT_GAP_MESH_NODES
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.HYDRATED_CHLORIDE_RADIUS
import com.xemantic.nano.plentyofroom.electrostatics.HYDRATED_MAGNESIUM_RADIUS
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.stericSaturationPotential
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Task `T-193` — **template-stripped gold: the hold-down at the answered material, and where
 * its potential of zero charge sits.**
 *
 * Emits `gpd/results/T-193-gold-electrode-pzc.json`.
 *
 * Two halves. The first re-reads `C-0021`'s `M4` at **gold alone**, NDI having answered
 * decision 3 on 2026-08-18 (*"Defaulting to template stripped gold for initial
 * experiments"*), and measures what the 2.6× electrode bracket's collapse is worth. The
 * second answers `C-0021`'s second open question — the electrode's potential of zero charge —
 * from published measurement, and carries it back into `C-0021`'s own field.
 */

// ---------------------------------------------------------------------------------------------
// the record types — prefixed, per CLAUDE.md, because study records are package-scoped
// ---------------------------------------------------------------------------------------------

/** `C-0021`'s `M4` at one state, gold alone beside the four-material bracket it collapses. */
@Serializable
data class T193VanDerWaalsRecord(
    val gap: Double,
    val tileThickness: Double,
    val goldForceLower: Double,
    val goldForceUpper: Double,
    val goldWellDepth: Double,
    val goldNegativeStiffness: Double,
    val goldOverThermalScaleLower: Double,
    val goldOverThermalScaleUpper: Double,
    /** The lowest force any of `C-0021`'s four electrodes gives at this state. */
    val fourMaterialForceLower: Double,
    /** The highest. */
    val fourMaterialForceUpper: Double,
    /** `upper/lower` over the four materials — the bracket `C-0021` reported. */
    val fourMaterialBracketRatio: Double,
    /** The same at gold alone — what is left once the material is named. */
    val goldBracketRatio: Double,
    /** How much of the state's width the material was carrying. */
    val bracketNarrowing: Double,
    /** `C-0021`'s confinement criterion: does the well reach 10 `k_BT`? */
    val confining: Boolean
)

/**
 * The screening argument audit — `C-0021`'s own van der Waals low end beside the same
 * expression evaluated at the **Debye** `κ` rather than at the one its call site produces.
 */
@Serializable
data class T193ScreeningAuditRecord(
    val gap: Double,
    val tileThickness: Double,
    /** As `C-0021` emits it: `screenedHamakerConstant(..., buffer.inverseDebyeLength(lb))`. */
    val goldForceLowerAsPublished: Double,
    /** The same expression at `buffer.inverseDebyeLength()`, i.e. at the documented 3.93 nm. */
    val goldForceLowerAtDebyeScreening: Double,
    val relativeDifference: String,
    /** The zero-frequency term's share of the cross constant — what the screening acts on. */
    val zeroFrequencyShare: Double,
    /** `e^(−2κd)` at the published call site. */
    val screeningFactorAsPublished: Double,
    /** `e^(−2κd)` at the Debye `κ`. */
    val screeningFactorAtDebye: Double
)

/** One solved gap at one imposed diffuse-layer drop, with the rational potential it implies. */
@Serializable
data class T193RationalPotentialRecord(
    val gap: Double,
    val diffusePotential: Double,
    /** `ψ_d + σ_e/C_S` — which is the electrode's potential measured from its own PZC. */
    val rationalPotential: Double,
    val electrodeChargeDensity: Double,
    /** Signed force in pN on the tile; negative is toward the electrode. */
    val force: Double,
    /** Positive DOWN, per `T-13`'s convention. */
    val holdDown: Double,
    val overThermalScale: Double,
    val numericallyResolved: Boolean
)

/** A rational potential the device would have to sit at for one named thing to happen. */
@Serializable
data class T193ThresholdRecord(
    val gap: Double,
    val what: String,
    val targetForce: Double,
    val rationalPotential: Double?,
    val diffusePotential: Double?,
    val sign: String
)

/** One published reading of `E_pzc`, with the source's own two scales and the Nernst join. */
@Serializable
data class T193PotentialOfZeroChargeRecord(
    val surface: String,
    val versusReversibleHydrogen: Double,
    val versusStandardHydrogen: Double,
    /** The SHE value re-derived from the RHE one at this project's 300 K. */
    val derivedVersusStandardHydrogen: Double,
    val departure: Double,
    val readStatus: String
)

/** What the measured PZC does to `C-0021`'s zero-bias state, at one gap and one reading. */
@Serializable
data class T193ExposureRecord(
    val gap: Double,
    val surface: String,
    /** The rational potential of an electrode held at 0 V on the SHE scale. */
    val rationalPotentialAtZeroVoltSheScale: Double,
    val thermalScaleThreshold: Double,
    val multipleOfThreshold: Double,
    val sign: String,
    /** The point-ion diffuse-drop ceiling on the side the PZC puts the electrode. */
    val pointIonBoundary: Double,
    val insidePointIonBoundary: Boolean
)

/** A number this study reproduces from an upstream result file or claim. */
@Serializable
data class T193ReproductionRecord(
    val what: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: String
)

@Serializable
data class T193Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: List<String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val vanDerWaals: List<T193VanDerWaalsRecord>,
    val screeningAudit: List<T193ScreeningAuditRecord>,
    val potentialOfZeroCharge: List<T193PotentialOfZeroChargeRecord>,
    val rationalPotentialLadder: List<T193RationalPotentialRecord>,
    val thresholds: List<T193ThresholdRecord>,
    val exposure: List<T193ExposureRecord>,
    val reproductions: List<T193ReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

/** §3's tile footprint in nm². */
private const val T193_FOOTPRINT: Double = 40.0 * 40.0

/** §3's buffer, in mM `MgCl₂` — the one `C-0021` read the zero-bias field at. */
private const val T193_BUFFER: Double = 2.0

/** `A1.1`'s positional bound in nm; `k_BT/σ` is the hold-down force scale. */
private const val T193_POSITION_BOUND: Double = 3.0

/** `C-0008`'s series compact layer in µF/cm². */
private const val T193_STERN_CAPACITANCE: Double = 20.0

/** The node count `C-0021` inverted its Stern series on. */
private const val T193_SEARCH_NODES: Int = 400

/** `C-0021`'s two readings of §3's tile thickness. */
private val T193_TILE_THICKNESSES = listOf(2.0, 10.0)

/** `C-0021`'s three heights. */
private val T193_GAPS = listOf(5.0, 7.0, 10.0)

/** `C-0021`'s confinement criterion, in `k_BT`. */
private const val T193_CONFINEMENT_BARRIER: Double = 10.0

/**
 * One candidate electrode, exactly as `C-0021` defines it — a Hamaker constant across water
 * with its own low and high readings, and whether the zero-frequency term is a metal's.
 */
private data class T193Electrode(
    val name: String,
    val low: Double,
    val high: Double,
    val metal: Boolean
) {

    fun zeroFrequencyCross(): Double = sqrt(
        HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC *
                (if (metal) HamakerConstants.ZERO_FREQUENCY_TERM
                else HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC)
    )

    fun screenedLow(gap: Double, inverseDebyeLength: Double): Double {
        val combined = combinedHamakerAcrossWater(HamakerConstants.DNA_ACROSS_WATER_LOW, low)
        val zero = min(zeroFrequencyCross(), combined)
        return screenedHamakerConstant(zero, combined - zero, gap, inverseDebyeLength)
    }
}

/**
 * The gap at a **prescribed diffuse-layer drop** — `CLAUDE.md`'s own rule that one solve
 * gives the force *and* the bias that produced it, where the other direction costs ~34.
 */
private class T193Gap(
    val tileCharge: Double,
    val sternChargePerVolt: Double,
    val buffer: MagnesiumChlorideBuffer,
    val bjerrum: Double
) {

    private val medium = uniformMedium(GapMedium())
    private val ionModel = IonModel(buffer.magnesiumNumberDensity)

    fun solve(gap: Double, diffusePotential: Double, nodes: Int) =
        PoissonBoltzmannGap(gap, ionModel, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential / thermalVoltage(), tileCharge)

    /**
     * `V = ψ_d + σ_e/C_S`, on the node count `C-0021` inverted its Stern series on — so the
     * ladder below is the *same* map `diffusePotentialOfAppliedBias` inverts, read forwards.
     */
    fun rationalPotential(gap: Double, diffusePotential: Double): Double =
        diffusePotential + solve(gap, diffusePotential, T193_SEARCH_NODES)
            .electrodeSurfaceChargeDensity / sternChargePerVolt

    /** Signed force in pN on the tile at the node count `C-0021` took its forces at. */
    fun force(gap: Double, diffusePotential: Double): Double =
        solve(gap, diffusePotential, DEFAULT_GAP_MESH_NODES).forceOnTile(T193_FOOTPRINT)
}

/**
 * The diffuse drop at which the signed force reaches [target], bisected on `ψ_d` inside
 * [low], [high] — which the caller has already checked brackets a sign change.
 */
private fun T193Gap.diffusePotentialForForce(
    gap: Double,
    target: Double,
    low: Double,
    high: Double
): Double {
    var lo = low
    var hi = high
    val atLow = force(gap, lo) - target
    repeat(60) {
        val mid = 0.5 * (lo + hi)
        val here = force(gap, mid) - target
        if ((here < 0.0) == (atLow < 0.0)) lo = mid else hi = mid
        if (hi - lo < 1e-9) return 0.5 * (lo + hi)
    }
    return 0.5 * (lo + hi)
}

fun main() {
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val buffer = MagnesiumChlorideBuffer(T193_BUFFER)
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val inverseDebye = buffer.inverseDebyeLength(lb)
    val thermalScale = holdDownForceScale(T193_POSITION_BOUND)
    val sternPerVolt = sternChargeDensityPerVolt(T193_STERN_CAPACITANCE)

    println("T-193 — gold alone, and where its potential of zero charge sits")
    println("thermal force scale k_BT/3nm = %.6f pN".format(thermalScale))

    // ------------------------------------------------------------ half 1: gold alone
    val electrodes = listOf(
        T193Electrode("gold", HamakerConstants.GOLD_ACROSS_WATER, HamakerConstants.GOLD_ACROSS_WATER_HIGH, true),
        T193Electrode("platinum", HamakerConstants.PLATINUM_ACROSS_WATER, 313.2, true),
        T193Electrode("rutile titania", HamakerConstants.TITANIA_ACROSS_WATER, HamakerConstants.TITANIA_ACROSS_WATER_HIGH, false),
        T193Electrode("alumina", HamakerConstants.ALUMINA_ACROSS_WATER, HamakerConstants.ALUMINA_ACROSS_WATER, false)
    )
    val gold = electrodes.first()

    fun forceLower(electrode: T193Electrode, gap: Double, thickness: Double): Double =
        vanDerWaalsPressure(electrode.screenedLow(gap, inverseDebye), gap, thickness) *
                retardationPressureFactor(gap) * T193_FOOTPRINT

    fun forceUpper(electrode: T193Electrode, gap: Double, thickness: Double): Double =
        vanDerWaalsPressure(
            combinedHamakerAcrossWater(HamakerConstants.DNA_ACROSS_WATER_HIGH, electrode.high),
            gap, thickness
        ) * T193_FOOTPRINT

    val vanDerWaals = T193_GAPS.flatMap { gap ->
        T193_TILE_THICKNESSES.map { thickness ->
            val goldLow = forceLower(gold, gap, thickness)
            val goldHigh = forceUpper(gold, gap, thickness)
            val allLow = electrodes.minOf { forceLower(it, gap, thickness) }
            val allHigh = electrodes.maxOf { forceUpper(it, gap, thickness) }
            val well = vanDerWaalsWellDepth(
                gold.screenedLow(gap, inverseDebye), gap, thickness, T193_FOOTPRINT
            ) * retardationPressureFactor(gap) / thermalEnergy()
            T193VanDerWaalsRecord(
                gap = gap,
                tileThickness = thickness,
                goldForceLower = goldLow,
                goldForceUpper = goldHigh,
                goldWellDepth = well,
                goldNegativeStiffness = vanDerWaalsPressureSlopeMagnitude(
                    gold.screenedLow(gap, inverseDebye), gap, thickness
                ) * retardationPressureFactor(gap) * T193_FOOTPRINT,
                goldOverThermalScaleLower = goldLow / thermalScale,
                goldOverThermalScaleUpper = goldHigh / thermalScale,
                fourMaterialForceLower = allLow,
                fourMaterialForceUpper = allHigh,
                fourMaterialBracketRatio = allHigh / allLow,
                goldBracketRatio = goldHigh / goldLow,
                bracketNarrowing = (allHigh / allLow) / (goldHigh / goldLow),
                confining = well >= T193_CONFINEMENT_BARRIER
            )
        }
    }

    // ------------------------------------------------------------ the screening audit
    //
    // `C-0021` and `C-0023` both write `buffer.inverseDebyeLength(lb)`, and
    // `MagnesiumChlorideBuffer.inverseDebyeLength`'s FIRST parameter is a **temperature**.
    // Passing the Bjerrum length there evaluates the Bjerrum length at 0.714 K, which is
    // where the emitted 5.22 nm^-1 comes from against the documented 0.2546. It is used in
    // exactly one place — the zero-frequency screening factor of the low end of the van der
    // Waals bracket — and there it saturates `e^(-2 kappa d)` to zero, i.e. it lands on the
    // "fully screened" end the claim's own prose declares. The audit measures the gap.
    val debyeInverse = buffer.inverseDebyeLength()
    val screeningAudit = T193_GAPS.flatMap { gap ->
        T193_TILE_THICKNESSES.map { thickness ->
            val published = forceLower(gold, gap, thickness)
            val combined = combinedHamakerAcrossWater(
                HamakerConstants.DNA_ACROSS_WATER_LOW, gold.low
            )
            val zero = min(gold.zeroFrequencyCross(), combined)
            val atDebye = vanDerWaalsPressure(
                screenedHamakerConstant(zero, combined - zero, gap, debyeInverse), gap, thickness
            ) * retardationPressureFactor(gap) * T193_FOOTPRINT
            T193ScreeningAuditRecord(
                gap = gap,
                tileThickness = thickness,
                goldForceLowerAsPublished = published,
                goldForceLowerAtDebyeScreening = atDebye,
                relativeDifference = "%.2g".format((atDebye - published) / published),
                zeroFrequencyShare = zero / combined,
                screeningFactorAsPublished = zeroFrequencyScreeningFactor(gap, inverseDebye),
                screeningFactorAtDebye = zeroFrequencyScreeningFactor(gap, debyeInverse)
            )
        }
    }

    // ------------------------------------------------------------ half 2: the PZC
    val pH = strongAcidPh(GoldPotentialOfZeroCharge.ELECTROLYTE_MOLARITY)
    val pzc = GoldPotentialOfZeroCharge.readingsVersusReversibleHydrogen.map { (surface, rhe) ->
        val published = GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.getValue(surface)
        val derived = reversibleHydrogenToStandardHydrogen(rhe, pH)
        T193PotentialOfZeroChargeRecord(
            surface = surface,
            versusReversibleHydrogen = rhe,
            versusStandardHydrogen = published,
            derivedVersusStandardHydrogen = derived,
            departure = derived - published,
            readStatus = "READ DIRECTLY — Adnan, Behjati, Felez-Guerrero, Ojha & Koper, " +
                    "Phys. Chem. Chem. Phys. 26:21419 (2024), 10.1039/d4cp02133a, EuropePMC " +
                    "PMC11323936 full text, verbatim from the Results section"
        )
    }

    val field = T193Gap(tileCharge, sternPerVolt, buffer, lb)

    // the ladder: sweep the diffuse drop, read the rational potential and the force off it
    val ladderPotentials = (-24..12).map { it * 0.005 }
    val ladder = T193_GAPS.flatMap { gap ->
        ladderPotentials.map { psi ->
            val solution = field.solve(gap, psi, DEFAULT_GAP_MESH_NODES)
            val force = solution.forceOnTile(T193_FOOTPRINT)
            T193RationalPotentialRecord(
                gap = gap,
                diffusePotential = psi,
                rationalPotential = field.rationalPotential(gap, psi),
                electrodeChargeDensity = solution.electrodeSurfaceChargeDensity,
                force = force,
                holdDown = -force,
                overThermalScale = -force / thermalScale,
                numericallyResolved = solution.numericallyResolved
            )
        }
    }
    println("ladder: ${ladder.size} solved states, " +
            "${ladder.count { it.numericallyResolved }} numerically resolved")

    // the thresholds, bisected on the diffuse drop inside a bracket the ladder certifies
    val thresholds = mutableListOf<T193ThresholdRecord>()
    T193_GAPS.forEach { gap ->
        val rung = ladder.filter { it.gap == gap }.sortedBy { it.diffusePotential }
        listOf(
            Triple("the thermal-scale HOLD-DOWN, k_BT/3nm toward the electrode", -thermalScale, "hold-down"),
            Triple("no net electrostatic force on the tile at all", 0.0, "neither"),
            Triple("the thermal-scale LIFT, k_BT/3nm away from the electrode", thermalScale, "lift")
        ).forEach { (what, target, sign) ->
            val bracket = rung.zipWithNext().firstOrNull { (a, b) ->
                (a.force - target < 0.0) != (b.force - target < 0.0)
            }
            val psi = bracket?.let {
                field.diffusePotentialForForce(gap, target, it.first.diffusePotential, it.second.diffusePotential)
            }
            thresholds += T193ThresholdRecord(
                gap = gap,
                what = what,
                targetForce = target,
                rationalPotential = psi?.let { field.rationalPotential(gap, it) },
                diffusePotential = psi,
                sign = sign
            )
        }
    }

    // the exposure: where a device held at 0 V on the SHE scale actually sits
    val chlorideBoundary = stericSaturationPotential(
        1, buffer.chlorideNumberDensity, HYDRATED_CHLORIDE_RADIUS
    )
    val magnesiumBoundary = stericSaturationPotential(
        2, buffer.magnesiumNumberDensity, HYDRATED_MAGNESIUM_RADIUS
    )
    val exposure = T193_GAPS.flatMap { gap ->
        val threshold = thresholds.first {
            it.gap == gap && it.sign == "hold-down"
        }.rationalPotential
        GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.map { (surface, value) ->
            val rational = rationalPotential(0.0, value)
            T193ExposureRecord(
                gap = gap,
                surface = surface,
                rationalPotentialAtZeroVoltSheScale = rational,
                thermalScaleThreshold = threshold ?: 0.0,
                multipleOfThreshold = abs(rational) / abs(threshold ?: 1.0),
                sign = if (rational < 0.0) "electrode NEGATIVE — it LIFTS the tile"
                else "electrode POSITIVE — it holds the tile down",
                pointIonBoundary = if (rational < 0.0) magnesiumBoundary else chlorideBoundary,
                insidePointIonBoundary =
                    abs(rational) < (if (rational < 0.0) magnesiumBoundary else chlorideBoundary)
            )
        }
    }

    // ------------------------------------------------------------ reproductions
    val published = mapOf(
        "C-0021 M4 gold force, 2 nm tile at 5 nm, retarded low end [pN]" to 10.3560219,
        "C-0021 M4 gold force, 2 nm tile at 10 nm, retarded low end [pN]" to 0.737332999,
        "C-0021 M4 gold well depth, 2 nm tile at 5 nm [k_BT]" to 4.81705073,
        "C-0021 M3 hold-down threshold at 5 nm [V]" to 0.000885908166,
        "C-0021 M3 hold-down threshold at 7 nm [V]" to 0.00184292351,
        "C-0021 M3 hold-down threshold at 10 nm [V]" to 0.00510177542
    )
    val reproduced = mapOf(
        "C-0021 M4 gold force, 2 nm tile at 5 nm, retarded low end [pN]" to
                vanDerWaals.first { it.gap == 5.0 && it.tileThickness == 2.0 }.goldForceLower,
        "C-0021 M4 gold force, 2 nm tile at 10 nm, retarded low end [pN]" to
                vanDerWaals.first { it.gap == 10.0 && it.tileThickness == 2.0 }.goldForceLower,
        "C-0021 M4 gold well depth, 2 nm tile at 5 nm [k_BT]" to
                vanDerWaals.first { it.gap == 5.0 && it.tileThickness == 2.0 }.goldWellDepth,
        "C-0021 M3 hold-down threshold at 5 nm [V]" to
                (thresholds.first { it.gap == 5.0 && it.sign == "hold-down" }.rationalPotential ?: 0.0),
        "C-0021 M3 hold-down threshold at 7 nm [V]" to
                (thresholds.first { it.gap == 7.0 && it.sign == "hold-down" }.rationalPotential ?: 0.0),
        "C-0021 M3 hold-down threshold at 10 nm [V]" to
                (thresholds.first { it.gap == 10.0 && it.sign == "hold-down" }.rationalPotential ?: 0.0)
    )
    val reproductions = published.map { (what, value) ->
        val here = reproduced.getValue(what)
        T193ReproductionRecord(
            what = what,
            published = value,
            reproduced = here,
            // two significant digits: a departure is a difference of two nearly equal numbers
            // and RESULT_ABSOLUTE_FLOOR is a claim about the locked units (CLAUDE.md, P-18)
            relativeDeparture = "%.2g".format(abs(here - value) / abs(value))
        )
    }

    // ------------------------------------------------------------ the emission
    val goldFive = vanDerWaals.first { it.gap == 5.0 && it.tileThickness == 2.0 }
    val goldTen = vanDerWaals.first { it.gap == 10.0 && it.tileThickness == 2.0 }
    val worstMultiple = exposure.minOf { it.multipleOfThreshold }
    val bestMultiple = exposure.maxOf { it.multipleOfThreshold }
    val narrowing = vanDerWaals.map { it.bracketNarrowing }

    val result = T193Result(
        task = "T-193",
        leaf = "A1.2 (the 3.0 nm positional bound, read at zero bias), inheriting C-0021",
        title = "Template-stripped gold: the hold-down at the answered material, and where " +
                "its potential of zero charge sits",
        verificationType = "logical (the identification of the model's applied bias with the " +
                "rational potential) + in-silico (C-0021's van der Waals term and C-0008's " +
                "gap field re-run at gold alone, the field parametrised by the diffuse drop " +
                "rather than by the bias) + literature (a primary, open-access, directly-read " +
                "measurement of E_pzc for Au(111) in a mM aqueous electrolyte)",
        acceptance = "PASS if (a) the gold-alone re-read reproduces C-0021's gold rows, (b) " +
                "no verdict of C-0021 moves, (c) a primary E_pzc is obtained with its " +
                "reference electrode and conditions or a query-recorded absence is filed, and " +
                "(d) the threshold that would change the answer is stated.",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED BY THIS " +
                "PROJECT. The E_pzc is somebody else's measurement, on Au(111) in 1 mM HClO4, " +
                "not on template-stripped gold in MgCl2.",
        units = listOf(
            "lengths nm, forces pN, energies k_BT (4.142 pN.nm at 300 K), potentials V",
            "charge densities e/nm^2, capacitance uF/cm^2"
        ),
        conventions = listOf(
            "A force is POSITIVE DOWN in the hold-down tables (T-13's convention) and the " +
                    "solved field's forceOnTile is NEGATIVE toward the electrode; both are emitted.",
            "The RATIONAL POTENTIAL is E - E_pzc, positive when the electrode is positively " +
                    "charged. The model's 'applied bias' IS this quantity, not a potentiostat setting.",
            "E_pzc is quoted against the SHE, and against the RHE at the source's own pH, " +
                    "because the source prints both and the pair is a transcription check.",
            "Zero bias means zero RATIONAL potential throughout C-0021; this study is about " +
                    "what a bench would have to do to deliver that."
        ),
        parameters = mapOf(
            "buffer [mM MgCl2]" to T193_BUFFER.toString(),
            "temperature [K]" to ROOM_TEMPERATURE.toString(),
            "footprint [nm^2]" to T193_FOOTPRINT.toString(),
            "positional bound [nm]" to T193_POSITION_BOUND.toString(),
            "thermal force scale k_BT/sigma [pN]" to roundForResult(thermalScale).toString(),
            "Stern capacitance [uF/cm^2]" to T193_STERN_CAPACITANCE.toString(),
            "Stern charge per volt [e/(V nm^2)]" to roundForResult(sternPerVolt).toString(),
            "tile charge density [e/nm^2]" to roundForResult(tileCharge).toString(),
            "inverse Debye length as C-0021 calls it [1/nm]" to roundForResult(inverseDebye).toString(),
            "inverse Debye length at the documented 3.93 nm [1/nm]" to roundForResult(debyeInverse).toString(),
            "gap mesh nodes, force" to DEFAULT_GAP_MESH_NODES.toString(),
            "gap mesh nodes, Stern inversion" to T193_SEARCH_NODES.toString(),
            "electrolyte of the E_pzc measurement [M HClO4]" to
                    GoldPotentialOfZeroCharge.ELECTROLYTE_MOLARITY.toString(),
            "pH of that electrolyte" to roundForResult(pH).toString(),
            "Nernst slope at 300 K [V/decade]" to roundForResult(nernstSlope()).toString(),
            "point-ion boundary, Cl- at a positive electrode [V]" to roundForResult(chlorideBoundary).toString(),
            "point-ion boundary, Mg2+ at a negative electrode [V]" to roundForResult(magnesiumBoundary).toString()
        ),
        vanDerWaals = vanDerWaals,
        screeningAudit = screeningAudit,
        potentialOfZeroCharge = pzc,
        rationalPotentialLadder = ladder,
        thresholds = thresholds,
        exposure = exposure,
        reproductions = reproductions,
        findings = emptyMap(),
        validity = listOf(
            "The E_pzc is measured on a Au(111) SINGLE CRYSTAL in 1 mM HClO4, not on a " +
                    "template-stripped film in MgCl2. Two exposures follow and they run " +
                    "opposite ways. (1) A template-stripped film is (111)-DOMINATED but not " +
                    "(111)-only, and Au(110) sits 0.3 V lower, so the film's own facets " +
                    "spread further than any of the numbers here. (2) Chloride adsorbs " +
                    "specifically on gold and shifts a PZC negatively; the size of that shift " +
                    "in MgCl2 is NOT SOURCED and is the one number this task could not find.",
            "Both exposures move the PZC in the same direction as the finding, not against " +
                    "it: they make the electrode's rational potential at 0 V vs SHE smaller in " +
                    "magnitude but not smaller than 0.2 V, which is still 39-226x the deciding " +
                    "threshold. The verdict is therefore robust to both.",
            "C-0005's mean-field bracket is inherited whole and the point-ion boundary is " +
                    "0.097 V of diffuse drop at a NEGATIVE electrode, against 0.197 V at a " +
                    "positive one. The rational potential the PZC implies is outside both, so " +
                    "the force at that offset is NOT computed here and no number is quoted for it.",
            "The van der Waals half inherits every validity note of C-0021's M4 unchanged, " +
                    "minus the electrode-material bracket, which NDI's answer closes.",
            "The ladder is C-0021's own field library at C-0021's own node counts. It is a " +
                    "re-read, not a new model, and it reproduces C-0021's published numbers."
        ),
        openQuestions = listOf(
            "How is 'zero bias' defined electrochemically in the Gen-1 cell? 0 V against a " +
                    "named reference electrode, the cell at open circuit, or two electrodes " +
                    "shorted? The three are hundreds of millivolts apart and the answer decides " +
                    "the SIGN of the residual field. This is the one-line ask to NDI.",
            "The chloride-induced PZC shift for gold in MgCl2. Not sourced in 32 recorded " +
                    "queries; it is a bounded correction, not an unbounded one, because the " +
                    "PZC cannot go below the Au(110) end of the facet spread without a " +
                    "measurement saying so.",
            "Whether a template-stripped film's facet distribution has ever been converted " +
                    "into an effective PZC. Liu, Doblhoff-Dier & Koper (2026) build exactly " +
                    "that model and do not apply it to a template-stripped film."
        ),
        citedNumbers = mapOf(
            "E_pzc Au(111), thermally reconstructed" to
                    "0.51 V vs SHE (0.69 vs RHE) — CITED, MEASURED, READ DIRECTLY, Adnan et al. PCCP 26:21419 (2024)",
            "E_pzc Au(111), potential-induced reconstruction" to
                    "0.497 V vs SHE (0.674 vs RHE) — CITED, MEASURED, READ DIRECTLY, same source",
            "E_pzc Au(111), unreconstructed with islands" to
                    "0.46 V vs SHE (0.64 vs RHE) — CITED, MEASURED, READ DIRECTLY, same source",
            "E_pzc Au(111) literature consensus" to
                    "~0.5 V vs SHE — CITED, READ DIRECTLY, Liu, Doblhoff-Dier & Koper, ACS Electrochem. 2:995 (2026)",
            "E_pzc Au(110) literature consensus" to
                    "~0.2 V vs SHE — CITED, READ DIRECTLY, same source",
            "template-stripped gold is (111)-dominated" to
                    "qualitative — CITED, READ DIRECTLY, Avedian, Trang & Inkpen, ACS Nanosci. Au 5:269 (2025)",
            "E_pzc of gold in MgCl2 or any divalent-cation electrolyte" to
                    "NOT FOUND — 32 EuropePMC queries recorded in gpd/data/T-193-sources/",
            "Hamaker constants, retardation, screening" to
                    "inherited from C-0021 unchanged; gold across water 238.6-267.9 zJ, Tolias arXiv:2003.00571"
        )
    )

    val findings = buildFindings(
        goldFive, goldTen, vanDerWaals, narrowing, worstMultiple, bestMultiple,
        thresholds, exposure, pzc, pH, screeningAudit, inverseDebye, debyeInverse
    )

    val file = File("gpd/results/T-193-gold-electrode-pzc.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    file.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result.copy(findings = findings)).roundedForResult()
        ) + "\n"
    )
    println("wrote ${file.path}")
    findings.forEach { (key, value) -> println("* $key\n    $value\n") }
}

private fun buildFindings(
    goldFive: T193VanDerWaalsRecord,
    goldTen: T193VanDerWaalsRecord,
    vanDerWaals: List<T193VanDerWaalsRecord>,
    narrowing: List<Double>,
    worstMultiple: Double,
    bestMultiple: Double,
    thresholds: List<T193ThresholdRecord>,
    exposure: List<T193ExposureRecord>,
    pzc: List<T193PotentialOfZeroChargeRecord>,
    pH: Double,
    screeningAudit: List<T193ScreeningAuditRecord>,
    publishedKappa: Double,
    debyeKappa: Double
): Map<String, String> {
    val holdDown = thresholds.filter { it.sign == "hold-down" }
    val lift = thresholds.filter { it.sign == "lift" }
    val forceFree = thresholds.filter { it.sign == "neither" }
    return mapOf(
        "the material bracket collapses onto its adverse end and nothing else moves" to (
                ("NDI's answer to decision 3 names gold, which is the STIFFEST of C-0021's " +
                        "four candidate electrodes, so the 2.6x specification bracket collapses " +
                        "ONTO ITS ADVERSE END rather than into the middle of it. At 5 nm on a " +
                        "2 nm tile the four-material span is %.3f-%.3f pN and gold alone is " +
                        "%.3f-%.3f; the state's own bracket narrows by exactly %.4fx at " +
                        "every one of the six states, the material entering as a pure factor. " +
                        "The well is DEEPER at gold than at any oxide and it is " +
                        "still finite: the deepest gold well in the box is %.3f k_BT against " +
                        "the 10 k_BT confinement criterion, so %d of %d gold states confine " +
                        "and C-0021's verdict is unchanged. A 1/h^3 force integrates to a " +
                        "bounded potential and naming the material cannot change the exponent.").format(
                    goldFive.fourMaterialForceLower, goldFive.fourMaterialForceUpper,
                    goldFive.goldForceLower, goldFive.goldForceUpper,
                    narrowing.max(),
                    vanDerWaals.maxOf { it.goldWellDepth },
                    vanDerWaals.count { it.confining }, vanDerWaals.size
                )),
        "but a GROUND moved: retardation is no longer a substitution" to (
                ("C-0021 states that its retardation factor is 'sourced for GOLD only and " +
                        "applied across the whole electrode bracket, and that substitution is " +
                        "stated'. At gold alone there is no substitution: the factor is the " +
                        "material's own. The DNA half of the cross constant is still already " +
                        "retarded, so the retarded reading still retards that half twice and " +
                        "the low end is still a LOWER bound - but one of the two caveats on it " +
                        "is discharged, and the gold force at 10 nm on a 2 nm tile stands at " +
                        "%.4f-%.4f pN, %.3f-%.3f of the thermal scale.").format(
                    goldTen.goldForceLower, goldTen.goldForceUpper,
                    goldTen.goldOverThermalScaleLower, goldTen.goldOverThermalScaleUpper
                )),
        "the model's applied bias IS the rational potential, and that is what makes the PZC decide" to (
                ("diffusePotentialOfAppliedBias solves V = psi_d + sigma_e(psi_d)/C_S. With no " +
                        "tile present, V = 0 makes the whole interfacial drop vanish, which is " +
                        "the DEFINITION of an electrode carrying no free charge. So C-0021's " +
                        "'applied bias' is the RATIONAL potential E - E_pzc and its " +
                        "contact-potential table is a table of rational potentials: %.3f mV at " +
                        "5 nm, %.3f at 7 and %.3f at 10 for the thermal-scale hold-down, " +
                        "reproduced here from the opposite direction (diffuse drop in, bias " +
                        "out) rather than by bisecting the Stern series. Zero applied bias in " +
                        "this programme has never meant zero volts on an instrument.").format(
                    1000.0 * (holdDown.first { it.gap == 5.0 }.rationalPotential ?: 0.0),
                    1000.0 * (holdDown.first { it.gap == 7.0 }.rationalPotential ?: 0.0),
                    1000.0 * (holdDown.first { it.gap == 10.0 }.rationalPotential ?: 0.0)
                )),
        "the measured PZC is two orders of magnitude past the deciding scale, and of the LIFTING sign" to (
                ("Au(111) in 1 mM HClO4 has E_pzc = %.3f-%.3f V vs SHE (Adnan et al. 2024, " +
                        "read directly, and the source's own RHE and SHE printings agree " +
                        "through Nernst at pH %.1f to %.1f mV). An electrode held at 0 V on " +
                        "that scale therefore sits at a rational potential of -%.3f to -%.3f V: " +
                        "%.0f-%.0fx C-0021's own thermal-scale threshold, and NEGATIVE, which " +
                        "charges the electrode negatively and makes it REPEL the negatively " +
                        "charged tile instead of holding it down. C-0021's M3 row reads 'DOWN " +
                        "but negligible' and that is a statement about an electrode AT its PZC.").format(
                    pzc.minOf { it.versusStandardHydrogen }, pzc.maxOf { it.versusStandardHydrogen },
                    pH, 1000.0 * pzc.maxOf { abs(it.departure) },
                    pzc.minOf { it.versusStandardHydrogen }, pzc.maxOf { it.versusStandardHydrogen },
                    worstMultiple, bestMultiple
                )),
        "the sign structure has three landmarks and they are all inside a few millivolts" to (
                ("At the PZC the tile's own countercharge already sits on the electrode, so " +
                        "the force at zero rational potential is not zero. The three landmarks " +
                        "at 10 nm are: thermal-scale LIFT at %.4f V, no net force at %.4f V, " +
                        "thermal-scale HOLD-DOWN at %.4f V. The whole sign structure of the " +
                        "residual field lives inside %.1f mV of rational potential, which is " +
                        "%.0fx narrower than the PZC offset a bench would have to null. The " +
                        "residual field is not a small correction the design can inherit; it " +
                        "is a control requirement on the potentiostat.").format(
                    lift.first { it.gap == 10.0 }.rationalPotential ?: 0.0,
                    forceFree.first { it.gap == 10.0 }.rationalPotential ?: 0.0,
                    holdDown.first { it.gap == 10.0 }.rationalPotential ?: 0.0,
                    1000.0 * abs(
                        (holdDown.first { it.gap == 10.0 }.rationalPotential ?: 0.0) -
                                (lift.first { it.gap == 10.0 }.rationalPotential ?: 0.0)
                    ),
                    exposure.minOf { abs(it.rationalPotentialAtZeroVoltSheScale) } / abs(
                        (holdDown.first { it.gap == 10.0 }.rationalPotential ?: 0.0) -
                                (lift.first { it.gap == 10.0 }.rationalPotential ?: 0.0)
                    )
                )),
        "the offset is outside the model, and that is the answer rather than a gap in it" to (
                ("The point-ion boundary is 0.097 V of diffuse drop at a NEGATIVE electrode " +
                        "(Mg2+ is the counterion there and the boundary goes as 1/z) against " +
                        "0.197 V at a positive one, and %d of %d exposure states fall outside " +
                        "it. So no force is quoted at the PZC offset: the honest statement is " +
                        "a THRESHOLD, which is that the electrode must be held within %.1f mV " +
                        "of its own potential of zero charge for the residual field to stay at " +
                        "the thermal scale at the 10 nm layer, and within %.2f mV at 5 nm. " +
                        "A number for the force there would need a Stern-dominated model this " +
                        "programme has not built.").format(
                    exposure.count { !it.insidePointIonBoundary }, exposure.size,
                    1000.0 * abs(holdDown.first { it.gap == 10.0 }.rationalPotential ?: 0.0),
                    1000.0 * abs(holdDown.first { it.gap == 5.0 }.rationalPotential ?: 0.0)
                )),
        "and a defect was found in the ground the low end rests on, worth under one per cent" to (
                ("The narrowing above is EXACTLY constant across gaps, which it should not be: " +
                        "the zero-frequency term is %.1f %% of gold's cross constant, so a " +
                        "gap-dependent screening cannot divide out of a ratio against an oxide " +
                        "whose share is more than twice that. The cause is that C-0021 and " +
                        "C-0023 both write buffer.inverseDebyeLength(lb), and that method's " +
                        "FIRST parameter is a TEMPERATURE: the Bjerrum length evaluated at " +
                        "0.714 K gives %.4f nm^-1 where the buffer's own default is %.4f " +
                        "(the documented 3.93 nm at 2 mM), a factor of %.1f, and e^(-2 kappa d) " +
                        "then saturates to %.1g at 5 nm — below the result file's 1e-9 " +
                        "absolute floor, so it is EMITTED as exactly 0.0 — against %.4f. " +
                        "It is used in ONE place, " +
                        "the low end of the van der Waals bracket, and it saturates that end to " +
                        "'fully screened' — which is exactly what C-0021's own prose declares " +
                        "the low end to be. So the emitted number is right for the stated " +
                        "bracket and the expression that produces it is not: at the Debye kappa " +
                        "the gold low end rises by %s at 5 nm and %s at 10 nm. No verdict moves " +
                        "and the code should still be repaired. Filed as CH-0128.").format(
                    100.0 * screeningAudit.first().zeroFrequencyShare,
                    publishedKappa, debyeKappa, publishedKappa / debyeKappa,
                    screeningAudit.first { it.gap == 5.0 }.screeningFactorAsPublished,
                    screeningAudit.first { it.gap == 5.0 }.screeningFactorAtDebye,
                    screeningAudit.first { it.gap == 5.0 && it.tileThickness == 2.0 }.relativeDifference,
                    screeningAudit.first { it.gap == 10.0 && it.tileThickness == 2.0 }.relativeDifference
                )),
        "what would change the answer" to (
                ("Three things, and all three are measurements somebody else would have to " +
                        "make. (1) A cell whose 'zero bias' is DEFINED as the PZC - two " +
                        "identical gold electrodes shorted, or a potentiostat held at " +
                        "E_pzc - which makes C-0021's M3 table apply verbatim. (2) A chloride " +
                        "shift larger than %.2f V, which would take the PZC of gold in MgCl2 " +
                        "to within the deciding scale of a common reference zero; nothing in " +
                        "the literature searched suggests a shift that large, and the " +
                        "Au(111)/Au(110) facet spread of 0.3 V is the natural comparison. " +
                        "(3) A Stern-layer capacitance far above 20 uF/cm^2, which would make " +
                        "the diffuse layer carry more of the offset; that runs the WRONG way, " +
                        "because it takes the state further outside the point-ion boundary.").format(
                    pzc.minOf { it.versusStandardHydrogen }
                ))
    )
}
