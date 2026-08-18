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
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import kotlin.math.ln
import kotlin.math.log10

/**
 * `T-193` — **the electrode's potential of zero charge, and what the Gen-1 gap model's
 * "applied bias" actually is.**
 *
 * ## The identification this file exists to make
 *
 * `diffusePotentialOfAppliedBias` solves `V = ψ_d + σ_e(ψ_d)/C_S` — the diffuse drop plus the
 * compact drop, in series. Set `V = 0` with no tile present and the whole interfacial drop
 * vanishes, which is the *definition* of the electrode carrying no free charge. So the model's
 * `appliedBias` is not a potential against any reference electrode at all: **it is the
 * rational potential `E − E_σ=0`**, the electrode's potential measured from its own potential
 * of zero charge. `C-0021`'s contact-potential table is therefore a table of *rational*
 * potentials, and answering it needs a `E_pzc`, not a specification of the drive electronics.
 *
 * That matters because the two are numerically nowhere near each other: `C-0021` needs
 * 0.9–5.1 mV and the measurement below is 460–510 mV.
 *
 * ## Why gold, and why Au(111)
 *
 * NDI answered decision 3 on 2026-08-18 — *"Defaulting to template stripped gold for initial
 * experiments"*. A template-stripped film is not a single crystal, but its (111) crystallites
 * dominate: Avedian, Trang & Inkpen, *ACS Nanosci. Au* **5**:269 (2025) attribute the gold
 * oxidation feature of their template-stripped electrodes to Au(111) and write that
 * *"crystallites with this orientation are expected to dominate the Au_TS surface"*
 * (**READ DIRECTLY**). The remaining facets are not free, though — see
 * [LITERATURE_AU110_VERSUS_SHE].
 */
object GoldPotentialOfZeroCharge {

    /**
     * The molarity of the electrolyte the measurement below was taken in — **1 mM HClO₄**,
     * argon-saturated, no supporting electrolyte, 20 mV/s.
     *
     * This is the closest published match to §3's *"mM ionic strength aqueous"* that exists
     * for a gold single crystal. It is **not** MgCl₂ and that is a real exposure: see the
     * validity notes of `C-0111`.
     */
    const val ELECTROLYTE_MOLARITY: Double = 1.0e-3

    /**
     * `E_pzc` of **thermally reconstructed** Au(111), volt versus the standard hydrogen
     * electrode — **CITED, MEASURED, READ DIRECTLY**: Adnan, Behjati, Félez-Guerrero, Ojha &
     * Koper, *Phys. Chem. Chem. Phys.* **26**:21419 (2024), DOI `10.1039/d4cp02133a`:
     * *"The first cycle has a capacitance minimum at 0.69 V vs. RHE (0.51 vs. SHE)"*.
     */
    const val THERMALLY_RECONSTRUCTED_VERSUS_SHE: Double = 0.51

    /**
     * `E_pzc` of the **potential-induced reconstruction** of Au(111) — same source:
     * *"we conclude that the PZC of the potential-induced surface reconstruction of Au(111)
     * is 0.674 V vs. RHE (0.497 V vs. SHE)"*.
     */
    const val POTENTIAL_INDUCED_RECONSTRUCTION_VERSUS_SHE: Double = 0.497

    /**
     * `E_pzc` of **unreconstructed** Au(111), the surface carrying the adatom islands left
     * when the reconstruction lifts — same source: *"whereas the PZC of a Au(111) (with
     * islands resulting from the lifting of the reconstruction) is 0.64 V vs. RHE (0.46 V vs.
     * SHE)"*.
     *
     * This is the **lowest** of the three and therefore the one every bound here is written
     * on, because it is the one an electrode that has been cycled positive will carry.
     */
    const val UNRECONSTRUCTED_VERSUS_SHE: Double = 0.46

    /** The same three readings on the RHE scale the source measured them against. */
    val readingsVersusReversibleHydrogen: Map<String, Double> = mapOf(
        "Au(111), thermally reconstructed" to 0.69,
        "Au(111), potential-induced reconstruction" to 0.674,
        "Au(111), unreconstructed, with adatom islands" to 0.64
    )

    /** The same three readings on the SHE scale, as the source itself converts them. */
    val readingsVersusStandardHydrogen: Map<String, Double> = mapOf(
        "Au(111), thermally reconstructed" to THERMALLY_RECONSTRUCTED_VERSUS_SHE,
        "Au(111), potential-induced reconstruction" to POTENTIAL_INDUCED_RECONSTRUCTION_VERSUS_SHE,
        "Au(111), unreconstructed, with adatom islands" to UNRECONSTRUCTED_VERSUS_SHE
    )

    /**
     * An **independent** statement of the same quantity — **CITED, READ DIRECTLY**: Liu,
     * Doblhoff-Dier & Koper, *ACS Electrochem.* **2**:995 (2026), DOI
     * `10.1021/acselectrochem.5c00544`: *"E_pzc values in the literature for Au(111) are
     * around 0.5 V vs. SHE"*.
     *
     * It is a different paper reporting a literature consensus rather than a fourth
     * measurement, so it is a **cross-check**, not a fourth reading.
     */
    const val LITERATURE_AU111_VERSUS_SHE: Double = 0.5

    /**
     * The same sentence's value for **Au(110)**: *"while for Au(110) they are around 0.2 V
     * vs. SHE"* — **CITED, READ DIRECTLY**, same source.
     *
     * This is why *"template-stripped gold"* is not by itself an answer: the same paper's
     * whole subject is that a polycrystalline film has a **facet-resolved** `E_pzc`, and the
     * spread between the two low-index faces is **0.3 V**. A film that is (111)-dominated
     * but not (111)-only therefore carries patches of surface hundreds of millivolts apart in
     * zero-charge potential, which is 59–330× `C-0021`'s deciding scale **within one
     * electrode**.
     */
    const val LITERATURE_AU110_VERSUS_SHE: Double = 0.2

}

/**
 * The Nernst slope `(k_BT/e) ln 10` in volt per decade — **59.52643 mV at 300 K**.
 *
 * The familiar 59.16 mV is the 298.15 K value, and every published RHE↔SHE conversion in the
 * electrochemistry literature is taken there; this project's locked temperature is 300 K, so
 * the conversion carries a **0.4 mV per pH unit** offset which is reported rather than
 * absorbed. At pH 3 — the electrolyte the measurement below was taken in — that is 1.2 mV
 * against a source that rounds to the millivolt, and it is why the transcription check in
 * `ElectrodePotentialOfZeroChargeTest` is written at 5 mV rather than at zero.
 */
fun nernstSlope(temperature: Double = ROOM_TEMPERATURE): Double =
    thermalVoltage(temperature) * ln(10.0)

/** `pH = −log₁₀ c` for a fully dissociated monoprotic acid at [molarity] mol/L, ideal. */
fun strongAcidPh(molarity: Double): Double {
    require(molarity > 0.0) { "molarity must be positive, was: $molarity" }
    return -log10(molarity)
}

/**
 * Converts [potentialVersusReversibleHydrogen] volt on the RHE scale at [pH] to the SHE
 * scale: `E(SHE) = E(RHE) − (k_BT/e) ln10 · pH`.
 *
 * The RHE tracks the solution's own proton activity, so the two scales coincide at `pH = 0`
 * and separate by one Nernst slope per pH unit.
 */
fun reversibleHydrogenToStandardHydrogen(
    potentialVersusReversibleHydrogen: Double,
    pH: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double = potentialVersusReversibleHydrogen - nernstSlope(temperature) * pH

/**
 * The **rational potential** `E − E_σ=0` in volt — the electrode potential measured from its
 * own potential of zero charge, and the quantity `diffusePotentialOfAppliedBias` calls an
 * *applied bias*.
 *
 * A one-line function that exists to be named: every "zero bias" statement in this programme
 * is a statement about this quantity and about no other, and three claims read it as a
 * potentiostat setting.
 */
fun rationalPotential(electrodePotential: Double, potentialOfZeroCharge: Double): Double =
    electrodePotential - potentialOfZeroCharge
