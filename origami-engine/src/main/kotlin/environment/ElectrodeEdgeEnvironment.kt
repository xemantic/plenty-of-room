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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.electrostatics.DEFAULT_RIM_STANDOFF
import com.xemantic.nano.plentyofroom.electrostatics.EdgeSolution
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannEdge
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.quantities.ScreeningLength

/**
 * A finite charged slab of stated half-width above a biased planar electrode, in 0.5–10 mM MgCl₂ —
 * the **2-D** nonlinear 2:1 Poisson-Boltzmann edge solve of `T-3b`, as an [Environment].
 *
 * The second of the two things nothing in the field does. A 1-D gap gives a uniform load; what a
 * plate on a foundation responds to is the **lateral profile** of that load, and the only way to
 * get it is to solve the two-dimensional problem with the obstacle in it. The result reversed this
 * project's own assumption: a finite charged plate's edge **adds** force rather than losing it —
 * fringing raises a capacitor's capacitance — and the Gen-1 gaps carry +14.7 % more total force
 * than a 1-D pressure over the same footprint.
 *
 * Nothing here knows what a duplex is. The body is a half-width, a thickness and three signed
 * surface charge densities.
 *
 * ## Geometry, and what the reference area is
 *
 * The solve is a **cross-section**: `x = 0` is the body's centre-line, `x = a` its rim, and every
 * extensive quantity comes out *per unit length of edge*. So [referenceArea] is a strip
 * `halfWidthNm × edgeLengthNm`, one nanometre of edge by default, and [force] is the total normal
 * force on that strip. [pressure] is therefore the **width-averaged** normal pressure over the
 * half-width, which is not the same number as the load at any one place — the load at the
 * centre-line is [centrelinePressure], and the whole profile is on the [EdgeSolution].
 *
 * ## Sign
 *
 * [EdgeSolution] reports a **downward load**, positive toward the electrode, which is the sign
 * `C-0006`'s plate consumes. This class reports the [Environment] convention — positive pushes the
 * two bodies apart — so it negates, once, here.
 *
 * @param buffer the 2:1 reservoir.
 * @param faceChargeDensity the **signed** charge on the body's gap-facing and far faces, `e/nm²`.
 * @param halfWidthNm half the body's lateral extent.
 * @param thicknessNm the body's thickness.
 * @param referenceGapNm the separation [decayLength] is read at.
 * @param referenceBiasVolts the applied bias [decayLength] is read at.
 * @param rimChargeDensity the rim's own charge. Zero by default, and that is **forced** rather
 *          than assumed: a uniformly charged slab has exactly the exterior field of two sheets of
 *          `ρt/2`, so a boundary-smeared model whose faces carry `ρt/2` has nowhere left to put
 *          the rim's charge. An uncharged rim also exerts *exactly* zero vertical force, because
 *          the traction on a vertical wall is `ε E_z E_x` and `E_x` there is fixed by the wall's
 *          own Neumann condition.
 * @param edgeLengthNm how much edge [force] is quoted over; one nanometre by default.
 * @param refinement the mesh multiplier. `T-3b` swept 1/2/4 and produced at 3.
 */
class ElectrodeEdgeEnvironment(
    override val buffer: MagnesiumChlorideBuffer,
    val faceChargeDensity: Double,
    val halfWidthNm: Double,
    val thicknessNm: Double,
    val referenceGapNm: Double,
    val referenceBiasVolts: Double = 0.0,
    val rimChargeDensity: Double = 0.0,
    val edgeLengthNm: Double = 1.0,
    val refinement: Int = 1,
    val rimStandoffNm: Double = DEFAULT_RIM_STANDOFF,
    val lowestGapNm: Double = DEFAULT_LOWEST_GAP,
    val highestGapNm: Double = DEFAULT_HIGHEST_GAP,
    val lowestBiasVolts: Double = 0.0,
    val highestBiasVolts: Double = DEFAULT_HIGHEST_BIAS,
    val sternCapacitance: Double = DEFAULT_STERN_CAPACITANCE,
    val medium: GapMedium = GapMedium(),
    val maximumIonDensity: Double = Double.POSITIVE_INFINITY,
    val bjerrumLengthNm: Double = bjerrumLength(),
    val biasSearchNodes: Int = DEFAULT_BIAS_SEARCH_NODES,
    val temperatureKelvin: Double = ROOM_TEMPERATURE,
    val bandwidthHz: Double? = null
) : ElectrolyteEnvironment {

    init {
        require(halfWidthNm > 0.0) { "halfWidthNm must be positive, was: $halfWidthNm" }
        require(thicknessNm > 0.0) { "thicknessNm must be positive, was: $thicknessNm" }
        require(edgeLengthNm > 0.0) { "edgeLengthNm must be positive, was: $edgeLengthNm" }
        require(refinement >= 1) { "refinement must be at least 1, was: $refinement" }
    }

    /** The gap [decayLength] is read at. */
    override val referenceHeightNm: Double get() = referenceGapNm

    /** A strip of the half-width, one nanometre of edge long unless [edgeLengthNm] says otherwise. */
    override val referenceArea: Double get() = halfWidthNm * edgeLengthNm

    override val name: String
        get() = "electrode edge: ${buffer.concentration} mM MgCl2, half-width $halfWidthNm nm"

    override val respondsToBias: Boolean get() = true

    override val regime: Regime by lazy {
        Regime.magnesiumChloride(
            name = name,
            concentrationMillimolar = buffer.concentration,
            lowestHeightNm = lowestGapNm,
            highestHeightNm = highestGapNm,
            lowestBiasVolts = lowestBiasVolts,
            highestBiasVolts = highestBiasVolts,
            temperatureKelvin = temperatureKelvin,
            bandwidthHz = bandwidthHz
        )
    }

    private val gap = ElectrodeGapEnvironment(
        buffer = buffer,
        wallChargeDensity = faceChargeDensity,
        referenceGapNm = referenceGapNm,
        referenceBiasVolts = referenceBiasVolts,
        lowestGapNm = lowestGapNm,
        highestGapNm = highestGapNm,
        lowestBiasVolts = lowestBiasVolts,
        highestBiasVolts = highestBiasVolts,
        sternCapacitance = sternCapacitance,
        maximumIonDensity = maximumIonDensity,
        bjerrumLengthNm = bjerrumLengthNm,
        biasSearchNodes = biasSearchNodes,
        temperatureKelvin = temperatureKelvin
    )

    private val solutions = LinkedHashMap<Pair<Double, Double>, EdgeSolution>(
        EDGE_SOLUTION_CACHE_ENTRIES, 0.75f, true
    )

    /**
     * The diffuse-layer drop the Stern series produces — the **1-D** inversion, unchanged.
     *
     * `T-3b` inverts the bias on the one-dimensional problem and imposes the result as the 2-D
     * electrode's Dirichlet datum, because a 2-D Stern inversion would cost ~34 edge solves for a
     * boundary condition the interior of the tile already fixes. That is a stated approximation and
     * it is this environment's, not a detail of its implementation.
     */
    fun diffuseLayerPotential(gapHeightNm: Double, biasVolts: Double): Double =
        gap.diffuseLayerPotential(gapHeightNm, biasVolts)

    /** The converged 2-D profile at [gapHeightNm] and applied bias [biasVolts]. */
    fun solve(gapHeightNm: Double, biasVolts: Double): EdgeSolution {
        regime.requireAdmits(gapHeightNm, biasVolts)
        val key = gapHeightNm to biasVolts
        solutions[key]?.let { return it }
        val diffuse = diffuseLayerPotential(gapHeightNm, biasVolts)
        val solution = PoissonBoltzmannEdge(
            gapHeight = gapHeightNm,
            ionModel = IonModel(buffer.magnesiumNumberDensity, maximumIonDensity),
            medium = medium,
            bjerrumLength = bjerrumLengthNm,
            tileHalfWidth = halfWidthNm,
            tileThickness = thicknessNm,
            refinement = refinement,
            temperature = temperatureKelvin
        ).solve(
            diffuse / thermalVoltage(temperatureKelvin),
            faceChargeDensity,
            faceChargeDensity,
            rimChargeDensity
        )
        if (solutions.size >= EDGE_SOLUTION_CACHE_ENTRIES) solutions.remove(solutions.keys.first())
        solutions[key] = solution
        return solution
    }

    /**
     * The **width-averaged** normal pressure over the half-width at zero applied bias, `pN/nm²`.
     *
     * Averaged, because the load of a finite body is not laterally uniform and there is therefore
     * no single *"the pressure"* to report. What the interior carries is [centrelinePressure],
     * which the solve asserts equals the 1-D answer; what the rim adds is the whole finding.
     */
    override fun pressure(heightNm: Double): Double = force(heightNm, 0.0) / referenceArea

    override fun force(heightNm: Double, biasVolts: Double): Double =
        -solve(heightNm, biasVolts).verticalForcePerUnitEdge * edgeLengthNm

    /** The normal pressure deep under the body, `pN/nm²` — the 1-D answer, and asserted to be. */
    fun centrelinePressure(heightNm: Double, biasVolts: Double): Double =
        -solve(heightNm, biasVolts).centrelineLoad

    override val bulkScreeningLength: ScreeningLength get() = gap.bulkScreeningLength

    /**
     * The **lateral** length the edge enhancement decays on, inward from the rim.
     *
     * This is not a Debye length and it is not on the same axis as one. `C-0031`'s closed form for
     * a slit gives the transverse eigenvalue `q₀² ≥ κ² + (π/2h)²` — 0.62–0.84 `λ_D` at 2 mM, and it
     * **narrows as the gap closes**, which no bulk screening length does. What is reported here is
     * the measured centroid of the load deficit, the first moment of the fitted raised-cosine
     * taper, taken outside [rimStandoffNm] because the traction at a 90° re-entrant corner is
     * mesh-**divergent** rather than mesh-dependent.
     */
    override val decayLength: ScreeningLength by lazy {
        val fit = solve(referenceGapNm, referenceBiasVolts).taperFit(rimStandoffNm)
        ScreeningLength(
            nanometres = fit.decayLength,
            where = ScreeningLength.CONFINED_GAP,
            axis = ScreeningLength.LATERAL,
            readAt = linkedMapOf(
                "concentrationMillimolar" to buffer.concentration.toString(),
                "gapNm" to referenceGapNm.toString(),
                "biasVolts" to referenceBiasVolts.toString(),
                "halfWidthNm" to halfWidthNm.toString(),
                "refinement" to refinement.toString(),
                "rimStandoffNm" to rimStandoffNm.toString()
            )
        )
    }

}

private const val EDGE_SOLUTION_CACHE_ENTRIES = 8
