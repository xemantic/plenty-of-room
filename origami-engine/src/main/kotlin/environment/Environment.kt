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

package com.xemantic.nano.plentyofroom.environment

import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.quantities.ScreeningLength

/**
 * What sits between the two bodies: the layer, the electrolyte and the field — **without a tile**.
 *
 * ## Why this interface exists
 *
 * Reading this codebase against the 2024–2026 DNA-nanotech tooling literature returns one result:
 * **the moat is the regime, not the ingredients.** oxDNA2 does carry salt-dependent electrostatics,
 * and says its parameterisation is *"restricted to salt concentrations of 0.1 M of monovalent salt
 * or greater"*, with magnesium *"not included in the oxDNA model"*; mrDNA applies an external
 * electric field and solves **no electrode boundary** — no diffuse layer, no Stern series turning a
 * diffuse drop into an applied bias, no force balance whose root is an operating point. This
 * device's whole operating range is 0.5–10 mM MgCl₂, below that floor, in the ion that is excluded.
 *
 * So `brush/` and `electrostatics/` are the parts of this tree with no counterpart in the field —
 * and until this interface existed they were reachable only **through** `Gen1Tile`, which meant
 * they could not be validated, cited or handed over on their own. Nothing behind this interface
 * knows what a duplex is.
 *
 * ## Geometry and sign conventions, fixed before deriving
 *
 * - `h` is the **separation** of the two bounding bodies in nm — the wall height for a grafted
 *   layer, the electrode-to-wall gap for a field. It is never a thickness.
 * - [pressure] and [force] are **positive when the environment pushes the two bodies apart**: the
 *   disjoining convention. A grafted layer under compression is positive; a charged wall pulled
 *   toward a driven electrode is negative. That is `GraftedLayerModel.disjoiningPressure`'s
 *   convention and `GapSolution.forceOnTile`'s already, and it is minus the *downward load* the
 *   2-D edge solve reports, which is why [ElectrodeEdgeEnvironment] negates and says so.
 * - a **bias** is an applied potential in volt, in the rational scale `E − E_{σ=0}` — the
 *   electrode's distance from its own potential of zero charge, not a potentiostat setting.
 *
 * ## The three members, and why they are the three
 *
 * [pressure] is the environment's own constitutive law. [force] is what a body of stated footprint
 * feels, and it takes the bias because the field's law is a two-parameter family while the layer's
 * is not. [decayLength] is the length the response falls on — and it is a
 * [com.xemantic.nano.plentyofroom.quantities.ScreeningLength] rather than a `Double` **because
 * this project has three correct values for that quantity** and substituting one for another is
 * `CH-0004`. It is read at [referenceHeightNm], which is stated.
 */
interface Environment {

    /** How this environment is named in a message and in a result file. */
    val name: String

    /** The buffer, valency, separations, biases and band this environment is solved in. */
    val regime: Regime

    /**
     * The footprint in `nm²` that [force] is quoted over.
     *
     * **One square nanometre unless the caller states a footprint.** An environment has no
     * footprint of its own, and importing one is exactly the dependency this layer exists to
     * remove: `1600.0` is a property of §3's tile, not of a polymer layer or an electrolyte.
     */
    val referenceArea: Double

    /** The separation [decayLength] is read at, in nm. */
    val referenceHeightNm: Double

    /**
     * Whether an applied bias moves this environment's answer at all.
     *
     * `false` is a **result**, not a shrug. Ideal mobile salt contributes `f = k_BT n_s φ`, strictly
     * linear in `φ`, and `Π = φ f′ − f` annihilates a linear term — so a neutral grafted layer's
     * osmotic response to the buffer, and therefore to anything the buffer's ions are driven by, is
     * exactly zero. At 10 mM MgCl₂ that cancels a term 3.5× the layer's own osmotic pressure.
     */
    val respondsToBias: Boolean

    /**
     * The normal pressure in `pN/nm²` at separation [heightNm] and **zero applied bias**.
     *
     * Positive pushes the two bodies apart. `pN/nm²` is exactly `MPa`.
     */
    fun pressure(heightNm: Double): Double

    /**
     * The **signed** normal force in `pN` over [referenceArea], at separation [heightNm] and
     * applied bias [biasVolts].
     *
     * Positive pushes the two bodies apart. `force(h, 0) == pressure(h) * referenceArea` on every
     * implementation, and that is a test rather than a definition wherever the two routes differ.
     */
    fun force(heightNm: Double, biasVolts: Double): Double

    /**
     * The length this environment's normal response decays on, read at [referenceHeightNm].
     *
     * `−F/(dF/dh)`, the local logarithmic derivative, which is what `T-3a` emits as
     * `forceDecayLength` and what `CH-0004` is about. It is a
     * [com.xemantic.nano.plentyofroom.quantities.ScreeningLength] so that *which* decay length it
     * is travels with the number: `ratioOf` refuses to divide a confined-gap reading by a bulk one,
     * and `statedRatio` renders both states when the comparison is the finding.
     *
     * @throws IllegalArgumentException where the response **grows** with separation at the
     *          reference state, which is a state near a sign change of the force and not a far
     *          field. Read it somewhere the environment has a far field, or report the state.
     */
    val decayLength: ScreeningLength

}

/**
 * An [Environment] whose response is set by an electrolyte, and which therefore carries **two**
 * screening lengths that must never be confused.
 *
 * `CLAUDE.md`: *"'The Debye length' is three different numbers in this project, and all three are
 * correct in their own place."* [bulkScreeningLength] is the reservoir's, a property of the salt
 * alone and — by Kjellander's dressed-ion theorem — the decay length any surface force must
 * approach at large `κh`, every surface convention entering only the amplitude.
 * [Environment.decayLength] is what the solve actually measures between the two walls. `C-0110`
 * measures them at 3.6–6.4 nm against a counterion-dominated 1.54–1.91 nm uniform-density estimate
 * and a 3.93 nm bulk value, all at once.
 */
interface ElectrolyteEnvironment : Environment {

    /** The buffer this environment is in equilibrium with. */
    val buffer: MagnesiumChlorideBuffer

    /** `λ_D = 1/κ` of the reservoir, `κ = √(8π l_B I)` with `I = 3c` for a 2:1 salt. */
    val bulkScreeningLength: ScreeningLength

}
