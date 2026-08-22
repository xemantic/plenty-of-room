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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.nano.plentyofroom.ELECTRON_VOLT
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.brush.DeGennesScaling
import com.xemantic.nano.plentyofroom.brush.brushOfHeight
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Task `T-7` — the poroelastic drainage time of the grafted layer, its scaling with
 * thickness and volume fraction, and the conditions under which it would cost the
 * §3 bandwidth requirement of 1 kHz.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=poroelastic.PoroelasticDrainageStudyKt
 * ```
 *
 * Emits `gpd/results/T-7-poroelastic-drainage.json`, deterministically — no timestamp,
 * so a re-run that changes nothing produces no diff.
 */

/** Where one design point's layer sits, thermodynamically, before any water moves. */
@Serializable
data class LayerState(
    val label: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChain: Double,
    val floryRadius: Double,
    val reducedGraftingDensity: Double,
    /** `L₀/s = (Σ/π)^(5/6)` — how many Alexander blobs are stacked across the layer. `CH-0003`. */
    val blobStackHeight: Double,
    /** `L₀/R_F` — how far the chain is stretched. Strong stretching assumes this is large. */
    val stretchRatio: Double,
    val volumeFraction: Double,
    val crossoverVolumeFraction: Double,
    val crossoverUnits: Double,
    val regime: String,
    val localExponent: Double,
    val osmoticPressure: Double,
    val bulkLongitudinalModulus: Double,
    val graftedLongitudinalModulus: Double,
    val layerStiffness: Double,
    val layerLongitudinalModulus: Double
)

/** One (layer, tile, permeability model, stiffness multiplier) evaluation. */
@Serializable
data class DesignPointResult(
    val layer: LayerState,
    val footprint: String,
    val footprintArea: Double,
    val stiffnessMultiplier: Double,
    val response: DrainageResponse,
    val poroelasticDiffusivity: Double,
    val lateralDrainageTime: Double,
    val verticalDrainageTime: Double,
    val zimmRelaxationTime: Double,
    val inertialTime: Double,
    val qualityFactor: Double
)

/** One cell of the thickness x volume-fraction sweep the acceptance predicate asks for. */
@Serializable
data class SweepPoint(
    val footprint: String,
    val permeabilityModel: String,
    val layerThickness: Double,
    val volumeFraction: Double,
    val permeability: Double,
    val screeningLength: Double,
    val screeningLengthOverThickness: Double,
    val brinkmanTransmissivity: Double,
    val darcyTransmissivity: Double,
    val poiseuilleTransmissivity: Double,
    val graftedLongitudinalModulus: Double,
    val layerStiffness: Double,
    val relaxationTime: Double,
    val cornerFrequency: Double,
    val marginAtOneKilohertz: Double
)

/**
 * One point of the 1 kHz contour: the volume fraction below which a layer of the given
 * thickness under the given tile becomes drainage-limited at the §3 bandwidth.
 */
@Serializable
data class BindingContourPoint(
    val footprint: String,
    val permeabilityModel: String,
    val layerThickness: Double,
    val bindingVolumeFraction: Double,
    val timesTheGen1VolumeFraction: Double,
    val screeningLengthOverThicknessThere: Double,
    val darcyPremiseHoldsThere: Boolean
)

/** One statement of the form "poroelasticity binds at 1 kHz when X reaches Y". */
@Serializable
data class BindingCondition(
    val variable: String,
    val statement: String,
    val value: Double,
    val unit: String,
    val timesTheGen1Value: Double,
    val reachable: String
)

@Serializable
data class PoroelasticStudyParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val thermalEnergyElectronVolts: Double,
    val waterViscosity: Double,
    val waterViscosityPascalSeconds: Double,
    val bandwidthTarget: Double,
    val tileFootprints: List<String>,
    val tileThickness: Double,
    val tileMassDensity: Double,
    val layerHeights: List<Double>,
    val graftingDensities: List<Double>,
    val stiffnessSource: String,
    val stiffnessMultipliers: List<Double>,
    val permeabilityModels: List<String>,
    val permeabilityProvenance: List<String>,
    val fourierHarmonics: Int,
    val sweepThicknesses: List<Double>,
    val sweepVolumeFractionRange: List<Double>,
    val sweepVolumeFractionSamples: Int
)

@Serializable
data class PoroelasticStudyResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: PoroelasticStudyParameters,
    val designPoints: List<DesignPointResult>,
    val sweep: List<SweepPoint>,
    val bindingContour: List<BindingContourPoint>,
    val binding: List<BindingCondition>,
    val openQuestions: List<String>
)

/** §3: the nominal tile, and the largest test tile the parameter table allows. */
private val GEN1_TILE = RectangularFootprint(40.0, 40.0)
private val TEST_TILE = RectangularFootprint(70.0, 100.0)

/** §3: "~10 nm (single-layer honeycomb)". Used only for the inertial check. */
private const val TILE_THICKNESS = 10.0

/** Hydrated DNA origami, `g/cm³`. Used only for the inertial check, which it cannot change. */
private const val TILE_MASS_DENSITY = 1.7

/** The `C-0001` design points: the brush-onset grafting density at each §3 layer height. */
private val DESIGN_POINTS = listOf(
    Triple("L0 = 5 nm, brush onset", 5.0, 0.092),
    Triple("L0 = 7 nm, brush onset", 7.0, 0.045),
    Triple("L0 = 10 nm, window lower edge", 10.0, 0.024),
    Triple("L0 = 10 nm, window upper edge", 10.0, 0.030)
)

/**
 * Multipliers applied to `C-0001`'s stiffness.
 *
 * `0.606` is the product of the two corrections `CH-0001` derives — the local osmotic
 * exponent (×0.807) and the measured des Cloizeaux prefactor (×0.751) — and is the best
 * current estimate pending `T-1c`. The rest bracket it by a factor of four either way.
 */
private val STIFFNESS_MULTIPLIERS = listOf(0.25, 0.606, 1.0, 2.0)

private val SWEEP_THICKNESSES = listOf(3.0, 5.0, 7.0, 10.0, 15.0, 20.0, 30.0)
private const val SWEEP_PHI_MIN = 0.005
private const val SWEEP_PHI_MAX = 0.30
private const val SWEEP_PHI_SAMPLES = 13

fun main() {
    val peg = PegWater()
    val viscosity = waterViscosity()
    val segmentScale = FreeDrainingSegments(
        segmentLength = peg.kuhnLength,
        segmentDiameter = peg.kuhnSegmentDiameter,
        segmentVolume = peg.kuhnSegmentVolume
    )
    val fibreArray = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)
    val blobScale = CorrelationLengthScreening(
        volumetricMonomerSize = peg.volumetricMonomerSize
    )
    val models = listOf(segmentScale, fibreArray, blobScale)
    val footprints = listOf("40 x 40 nm" to GEN1_TILE, "70 x 100 nm" to TEST_TILE)

    val layers = DESIGN_POINTS.map { (label, height, density) ->
        layerState(peg, label, height, density)
    }

    val designPoints = layers.flatMap { layer ->
        footprints.flatMap { (name, footprint) ->
            models.flatMap { model ->
                STIFFNESS_MULTIPLIERS.map { multiplier ->
                    evaluate(layer, name, footprint, model, multiplier, viscosity)
                }
            }
        }
    }

    val volumeFractions = logarithmicSweep(SWEEP_PHI_MIN, SWEEP_PHI_MAX, SWEEP_PHI_SAMPLES)
    val sweep = footprints.flatMap { (name, footprint) ->
        listOf(segmentScale, blobScale).flatMap { model ->
            SWEEP_THICKNESSES.flatMap { thickness ->
                volumeFractions.map { phi ->
                    sweepPoint(peg, name, footprint, model, thickness, phi, viscosity)
                }
            }
        }
    }

    val result = PoroelasticStudyResult(
        task = "T-7",
        leaf = "new — no leaf in the NDI task map; §4(d) and §6 task 7",
        title = "Poroelastic drainage time of the grafted PEG layer, its scaling with " +
                "thickness and volume fraction, and what would make it bind at 1 kHz",
        verificationType = "in-silico (analytic poroelastic/Brinkman model, evaluated numerically)",
        acceptance = "Bounded, with the conditions under which it would constrain " +
                ">= 1 kHz operation stated",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The permeability is bracketed by two published constructions that disagree " +
                "by a factor of 40; the bound is quoted from the slower one.",
        units = mapOf(
            "length" to "nm",
            "area" to "nm^2",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa)",
            "stiffness" to "pN/nm (= mN/m)",
            "permeability" to "nm^2 (= 1e-18 m^2)",
            "transmissivity" to "nm^3",
            "viscosity" to "pN*s/nm^2 (= 1e6 Pa*s)",
            "drag" to "pN*s/nm",
            "diffusivity" to "nm^2/s",
            "time" to "s",
            "frequency" to "Hz",
            "mass" to "pN*s^2/nm (= 1e-3 kg)",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z normal to the electrode, positive away from it, origin at the electrode surface",
            "the layer occupies 0 < z < h; the tile is a rigid IMPERMEABLE non-adsorbing " +
                    "plate at z = h and the electrode below is impermeable too",
            "pore pressure is zero at the perimeter of the tile footprint",
            "drainage lengths are defined so that tau = l^2 / D_p with the same D_p, " +
                    "which is the only way lateral and vertical are comparable",
            "phi ALWAYS means the physical polymer volume fraction, per C-0002",
            "the layer stiffness is an INPUT, not a computation — every time here is " +
                    "exactly inversely proportional to it"
        ),
        validity = listOf(
            "Darcy/Brinkman continuum: valid where sqrt(k) << h. On the segment-scale " +
                    "permeability sqrt(k)/h = 0.09-0.13 and it holds; on the blob-scale one " +
                    "sqrt(k)/h = 0.56-0.58 and it does NOT. The Brinkman transmissivity is " +
                    "used because it contains the free-film limit the second case degrades to.",
            "the layer is 1.7-1.8 correlation lengths thick on the blob-scale model, so " +
                    "there is no separation of scales and the continuum reading of the " +
                    "second case is qualitative, not quantitative",
            "the tile is rigid and impermeable; a dishing tile (T-5b) does not sample a " +
                    "single h and a permeable origami short-circuits the lateral path",
            "linear poroelasticity: the permeability and the modulus are evaluated at the " +
                    "stated volume fraction and held constant through the displacement",
            "no electrostatics and no ion partitioning — the drag on mobile ions being " +
                    "swept with the water is not in this model (see T-6, §4(c))",
            "the equation of state behind the modulus was fitted in PURE WATER (C-0002); " +
                    "the Gen-1 buffer is 2-10 mM MgCl2"
        ),
        parameters = PoroelasticStudyParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2; viscosity taken as that of pure water",
            thermalEnergy = thermalEnergy(),
            thermalEnergyElectronVolts = thermalEnergy() / ELECTRON_VOLT,
            waterViscosity = viscosity,
            waterViscosityPascalSeconds = viscosity / PASCAL_SECOND,
            bandwidthTarget = BANDWIDTH_TARGET,
            tileFootprints = footprints.map { it.first },
            tileThickness = TILE_THICKNESS,
            tileMassDensity = TILE_MASS_DENSITY,
            layerHeights = DESIGN_POINTS.map { it.second },
            graftingDensities = DESIGN_POINTS.map { it.third },
            stiffnessSource = "C-0001 / brush.DeGennesScaling at h = L0, treated per CH-0001 " +
                    "as a bound and swept by stiffnessMultipliers pending T-1c",
            stiffnessMultipliers = STIFFNESS_MULTIPLIERS,
            permeabilityModels = models.map { it.name },
            permeabilityProvenance = models.map { "${it.name}: ${it.provenance}" },
            fourierHarmonics = RectangularFootprint.DEFAULT_HARMONICS,
            sweepThicknesses = SWEEP_THICKNESSES,
            sweepVolumeFractionRange = listOf(SWEEP_PHI_MIN, SWEEP_PHI_MAX),
            sweepVolumeFractionSamples = SWEEP_PHI_SAMPLES
        ),
        designPoints = designPoints,
        sweep = sweep,
        bindingContour = footprints.flatMap { (name, footprint) ->
            SWEEP_THICKNESSES.map { thickness ->
                bindingContourPoint(peg, name, footprint, segmentScale, thickness, viscosity)
            }
        },
        binding = bindingConditions(peg, segmentScale, layers[2], viscosity),
        openQuestions = listOf(
            "The permeability is not known to better than a factor of 40 for THIS layer. " +
                    "The segment-scale and blob-scale constructions disagree because at " +
                    "phi/phi# = 1.1 the correlation blob is two thirds of the whole coil " +
                    "(CH-0001), so the two are descriptions of the same object. Settling it " +
                    "needs a measurement of the hydrodynamic screening length of a grafted " +
                    "PEG layer at sigma ~ 0.024 nm^-2, which no source was found for.",
            "Jackson & James (1986) is quoted from secondary literature; the primary source " +
                    "was paywalled and NOT obtained. It is used only as a cross-check on the " +
                    "derived free-draining model, which it agrees with to a factor of 1.3.",
            "Electro-osmotic coupling is absent. The layer is porous AND the buffer is " +
                    "ionic AND the electrode is biased, so streaming potential opposes the " +
                    "squeeze flow. Bounding that needs T-6's screening model and is not done here.",
            "Whether the DNA-origami tile is hydraulically permeable is not established. " +
                    "If it is, the vertical path opens — worth at most 1.4x at 40 x 40 nm " +
                    "and 5.8x at 70 x 100 nm, so it is not a design lever."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-7-poroelastic-drainage.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(result)
                // NINE DIGITS at a ZERO FLOOR (`T-278`, closing `CH-0223`). The digit count is
                // the easy half: the Brinkman transmissivity and every drag, time and frequency
                // here is a closed form, and the one search — the 1 kHz bandwidth contour — is
                // 200 bisection halvings. What is NOT the shared site is the FLOOR; see
                // [POROELASTIC_RESULT_FLOOR].
                .roundedForResult(floor = POROELASTIC_RESULT_FLOOR)
                .withEmissionHeader(LatticeTag.NONE, null)
        ) + "\n"
    )
    report(result, output)
}

/**
 * The magnitude below which `T-7` reports a result as exactly zero, and it is **zero**.
 *
 * `RESULT_ABSOLUTE_FLOOR` is a claim **in the locked units** — `gpd/README.md`'s "no force below a
 * nanopiconewton is of interest" — and `P-18` records that such a claim **does not travel**: it
 * found its own determined-precision measurement flattened to `0.0` by the default, and `C-0031`
 * found a floored `layerStiffness` printed beside an unfloored `sqrt(k_BT/k)` derived from it, an
 * arithmetically impossible pair.
 *
 * `T-7` emits almost nothing in the locked units. Its answers are **times in seconds**, frequencies
 * in hertz, drags in `pN*s/nm` and dimensionless ratios, and its smallest non-zero committed value
 * is an `inertialTime` of `6.97e-14 s` — half a picosecond, and the quantity whose ratio to the
 * drainage time IS the study's own "is this overdamped" verdict. The default floor flattens 96 of
 * them to `0.0` (measured, `tools/T-278-rounding-simulation.py`), and a `verticalDrainageTime` of
 * `1.53e-09 s` clears it by half a unit in the first digit.
 *
 * Zero rather than a smaller positive number because there is nothing here for a floor to be a
 * claim about: no quantity this study emits is exactly zero by any symmetry, so a floor could only
 * suppress a value the physics means.
 */
internal const val POROELASTIC_RESULT_FLOOR: Double = 0.0

private fun layerState(
    peg: PegWater,
    label: String,
    layerHeight: Double,
    graftingDensity: Double
): LayerState {
    val brush = brushOfHeight(layerHeight, graftingDensity, peg.effectiveMonomerLength)
    val chainLength = brush.monomersPerChain
    val eos = peg.equationOfState(chainLength)
    val phi = peg.volumeFraction(chainLength, graftingDensity, layerHeight)
    val stiffness = DeGennesScaling().stiffness(brush, layerHeight, GEN1_TILE.area)
    return LayerState(
        label = label,
        layerHeight = layerHeight,
        graftingDensity = graftingDensity,
        graftingSpacing = brush.graftingSpacing,
        monomersPerChain = chainLength,
        floryRadius = brush.floryRadius,
        reducedGraftingDensity = brush.reducedGraftingDensity,
        blobStackHeight = layerHeight / brush.graftingSpacing,
        stretchRatio = layerHeight / brush.floryRadius,
        volumeFraction = phi,
        crossoverVolumeFraction = eos.crossoverVolumeFraction,
        crossoverUnits = phi / eos.crossoverVolumeFraction,
        regime = eos.regime(phi).name,
        localExponent = eos.localExponent(phi),
        osmoticPressure = eos.pressure(phi),
        bulkLongitudinalModulus = eos.localExponent(phi) * eos.pressure(phi),
        graftedLongitudinalModulus = graftedLongitudinalModulus(peg, phi),
        layerStiffness = stiffness,
        layerLongitudinalModulus = stiffness * layerHeight / GEN1_TILE.area
    )
}

/**
 * Returns `M = φ dΠ/dφ` in `pN/nm²` for a **grafted** layer, i.e. the des Cloizeaux limb
 * of `C-0002`'s equation of state alone: `M = (9/4) α (k_BT/v₀) φ^(9/4)`.
 *
 * The van't Hoff limb is dropped deliberately. `C-0002` is explicit that a grafted layer
 * has no chain translational entropy, so that limb is not part of the layer's restoring
 * pressure — and dropping it makes the modulus, and hence the poroelastic diffusivity,
 * *smaller*, which is the direction a bound on a drainage time has to err in.
 */
private fun graftedLongitudinalModulus(peg: PegWater, volumeFraction: Double): Double =
    2.25 * peg.crossoverIndex * (thermalEnergy() / peg.monomerVolume) *
            volumeFraction.pow(2.25)

private fun evaluate(
    layer: LayerState,
    footprintName: String,
    footprint: RectangularFootprint,
    model: LayerPermeability,
    stiffnessMultiplier: Double,
    viscosity: Double
): DesignPointResult {
    val stiffness = layer.layerStiffness * stiffnessMultiplier *
            footprint.area / GEN1_TILE.area
    val response = drainageResponse(
        footprint = footprint,
        thickness = layer.layerHeight,
        volumeFraction = layer.volumeFraction,
        permeabilityModel = model,
        layerStiffness = stiffness,
        viscosity = viscosity
    )
    val diffusivity = poroelasticDiffusivity(
        response.permeability,
        layer.layerLongitudinalModulus * stiffnessMultiplier,
        viscosity
    )
    val mass = TILE_MASS_DENSITY * footprint.area * TILE_THICKNESS * GRAM_PER_CUBIC_CENTIMETRE
    return DesignPointResult(
        layer = layer,
        footprint = footprintName,
        footprintArea = footprint.area,
        stiffnessMultiplier = stiffnessMultiplier,
        response = response,
        poroelasticDiffusivity = diffusivity,
        lateralDrainageTime = footprint.drainageFactor / diffusivity,
        verticalDrainageTime = verticalDrainageLength(layer.layerHeight)
            .let { it * it } / diffusivity,
        zimmRelaxationTime = zimmRelaxationTime(layer.floryRadius, viscosity),
        inertialTime = mass / response.totalDrag,
        qualityFactor = qualityFactor(mass, stiffness, response.totalDrag)
    )
}

private fun sweepPoint(
    peg: PegWater,
    footprintName: String,
    footprint: RectangularFootprint,
    model: LayerPermeability,
    thickness: Double,
    volumeFraction: Double,
    viscosity: Double
): SweepPoint {
    val modulus = graftedLongitudinalModulus(peg, volumeFraction)
    val stiffness = modulus * footprint.area / thickness
    val response = drainageResponse(
        footprint = footprint,
        thickness = thickness,
        volumeFraction = volumeFraction,
        permeabilityModel = model,
        layerStiffness = stiffness,
        viscosity = viscosity
    )
    return SweepPoint(
        footprint = footprintName,
        permeabilityModel = model.name,
        layerThickness = thickness,
        volumeFraction = volumeFraction,
        permeability = response.permeability,
        screeningLength = response.screeningLength,
        screeningLengthOverThickness = response.screeningLengthOverThickness,
        brinkmanTransmissivity = response.transmissivity,
        darcyTransmissivity = response.darcyTransmissivity,
        poiseuilleTransmissivity = response.poiseuilleTransmissivity,
        graftedLongitudinalModulus = modulus,
        layerStiffness = stiffness,
        relaxationTime = response.relaxationTime,
        cornerFrequency = response.cornerFrequency,
        marginAtOneKilohertz = response.marginAtOneKilohertz
    )
}

/**
 * The half of the acceptance predicate that says *what would make it binding*, computed
 * one variable at a time from the `L₀ = 10 nm` design point — the slowest of the three
 * §3 heights and therefore the one the boundary should be quoted from.
 */
private fun bindingConditions(
    peg: PegWater,
    model: LayerPermeability,
    layer: LayerState,
    viscosity: Double
): List<BindingCondition> {
    val reference = drainageResponse(
        footprint = GEN1_TILE,
        thickness = layer.layerHeight,
        volumeFraction = layer.volumeFraction,
        permeabilityModel = model,
        layerStiffness = layer.layerStiffness,
        viscosity = viscosity
    )
    val testTileResponse = drainageResponse(
        footprint = TEST_TILE,
        thickness = layer.layerHeight,
        volumeFraction = layer.volumeFraction,
        permeabilityModel = model,
        layerStiffness = layer.layerStiffness * TEST_TILE.area / GEN1_TILE.area,
        viscosity = viscosity
    )
    val edge = bindingSquareTileEdge(
        targetFrequency = BANDWIDTH_TARGET,
        thickness = layer.layerHeight,
        volumeFraction = layer.volumeFraction,
        permeabilityModel = model,
        referenceFootprint = GEN1_TILE,
        referenceStiffness = layer.layerStiffness,
        viscosity = viscosity
    )
    val phi = bindingVolumeFraction(peg, model, GEN1_TILE, layer.layerHeight, viscosity)
    return listOf(
        BindingCondition(
            variable = "frequency",
            statement = "the drainage pole itself, at the 40 x 40 nm tile: operation above " +
                    "this frequency is drainage-limited",
            value = reference.cornerFrequency,
            unit = "Hz",
            timesTheGen1Value = reference.marginAtOneKilohertz,
            reachable = "NOT within the §3 bandwidth requirement — it is " +
                    "${"%.0f".format(reference.marginAtOneKilohertz)}x above it"
        ),
        BindingCondition(
            variable = "frequency",
            statement = "the same pole at the largest §3 test tile, 70 x 100 nm — the worst " +
                    "case in the whole parameter table",
            value = testTileResponse.cornerFrequency,
            unit = "Hz",
            timesTheGen1Value = testTileResponse.marginAtOneKilohertz,
            reachable = "NOT within the §3 bandwidth requirement"
        ),
        BindingCondition(
            variable = "tile edge",
            statement = "square tile edge at which the drainage pole falls to 1 kHz, " +
                    "everything else at the design point",
            value = edge,
            unit = "nm",
            timesTheGen1Value = edge / 40.0,
            reachable = "NO — it is 11x the Gen-1 tile and 4.4x the longest §3 test tile edge"
        ),
        BindingCondition(
            variable = "volume fraction",
            statement = "the volume fraction below which a 40 x 40 nm tile on a 10 nm layer " +
                    "becomes drainage-limited at 1 kHz. The layer gets MORE permeable as it " +
                    "is diluted, but its modulus falls faster, so the binding direction is " +
                    "downward, not upward",
            value = phi,
            unit = "1",
            timesTheGen1Value = phi / layer.volumeFraction,
            reachable = "NO — it is 15x below the design volume fraction and far inside the " +
                    "mushroom regime §4(a) rules out; such a layer has no restoring force to " +
                    "actuate against in the first place"
        ),
        BindingCondition(
            variable = "layer stiffness",
            statement = "the factor by which T-1c would have to soften the layer, at the " +
                    "40 x 40 nm tile, for drainage to bind at 1 kHz. Every time in this " +
                    "study is exactly inversely proportional to the stiffness",
            value = 1.0 / reference.marginAtOneKilohertz,
            unit = "1",
            timesTheGen1Value = 1.0 / reference.marginAtOneKilohertz,
            reachable = "NO — CH-0001's own corrections amount to x0.61, not x0.009"
        ),
        BindingCondition(
            variable = "permeability",
            statement = "the factor by which the layer would have to be LESS permeable than " +
                    "the slowest of the three models for drainage to bind at 1 kHz",
            value = 1.0 / reference.marginAtOneKilohertz,
            unit = "1",
            timesTheGen1Value = 1.0 / reference.marginAtOneKilohertz,
            reachable = "NO — the three models span a factor of 40 and the bound is already " +
                    "quoted from the slowest; measured hydrogel permeabilities are HIGHER, " +
                    "not lower, than any of them"
        )
    )
}

/**
 * Returns the volume fraction at which a layer of [thickness] under the Gen-1 tile has a
 * drainage pole at exactly 1 kHz, with the stiffness taken as the grafted des Cloizeaux
 * modulus over the thickness.
 *
 * Bisection is justified rather than lazy: `k(φ) ∝ φ^(−1)` and `M(φ) ∝ φ^(9/4)`, so
 * `k M ∝ φ^(5/4)` is strictly increasing and the relaxation time strictly decreasing —
 * the answer is unique and the bracket cannot straddle a second root.
 */
private fun bindingVolumeFraction(
    peg: PegWater,
    model: LayerPermeability,
    footprint: RectangularFootprint,
    thickness: Double,
    viscosity: Double
): Double {
    fun corner(phi: Double): Double = drainageResponse(
        footprint = footprint,
        thickness = thickness,
        volumeFraction = phi,
        permeabilityModel = model,
        layerStiffness = graftedLongitudinalModulus(peg, phi) * footprint.area / thickness,
        viscosity = viscosity
    ).cornerFrequency
    var low = 1e-9
    var high = 0.35
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (corner(middle) < BANDWIDTH_TARGET) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

/**
 * Returns one point of the 1 kHz contour, together with the Darcy-premise diagnostic
 * *at that point* — which is the reason the contour is worth emitting at all.
 *
 * The binding region turns out to be exactly the region where `√k` climbs to a
 * significant fraction of the layer thickness, i.e. where the continuum poroelastic
 * description this whole task is built on stops being applicable. The boundary is
 * therefore reported as *where the model would say it binds*, not as a prediction.
 */
private fun bindingContourPoint(
    peg: PegWater,
    footprintName: String,
    footprint: RectangularFootprint,
    model: LayerPermeability,
    thickness: Double,
    viscosity: Double
): BindingContourPoint {
    val phi = bindingVolumeFraction(peg, model, footprint, thickness, viscosity)
    val ratio = model.screeningLength(phi) / thickness
    return BindingContourPoint(
        footprint = footprintName,
        permeabilityModel = model.name,
        layerThickness = thickness,
        bindingVolumeFraction = phi,
        timesTheGen1VolumeFraction = phi / 0.0288872,
        screeningLengthOverThicknessThere = ratio,
        darcyPremiseHoldsThere = ratio < DARCY_PREMISE_LIMIT
    )
}

/**
 * `√k/h` above which the Darcy limit is not a usable approximation.
 *
 * At `√k/h = 0.2` the Brinkman transmissivity is already 45 % below `k h`, so the layer
 * is not behaving as a Darcy medium at all. Chosen and stated, not fitted.
 */
private const val DARCY_PREMISE_LIMIT: Double = 0.2

/** Returns [samples] points from [from] to [to] inclusive, evenly spaced in the logarithm. */
private fun logarithmicSweep(from: Double, to: Double, samples: Int): List<Double> {
    val step = (ln(to) - ln(from)) / (samples - 1)
    return List(samples) { i -> exp(ln(from) + i * step) }
}

private fun report(result: PoroelasticStudyResult, output: File) {
    println("T-7 — ${result.title}")
    println(
        "300 K, aqueous, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm, " +
                "eta = ${"%.4f".format(result.parameters.waterViscosityPascalSeconds * 1e3)} mPa*s"
    )
    println()
    println("--- where the layer sits, and how permeable it is ".padEnd(104, '-'))
    println(
        "%-28s %8s %8s %8s %8s %10s %10s %8s %8s".format(
            "design point", "phi", "phi/phi#", "M_graft", "k_layer",
            "sqrt(k) seg", "sqrt(k) blob", "L0/s", "L0/R_F"
        )
    )
    result.designPoints
        .filter { it.footprint == "40 x 40 nm" && it.stiffnessMultiplier == 1.0 }
        .groupBy { it.layer.label }
        .forEach { (label, points) ->
            val layer = points.first().layer
            val segment = points.first { it.response.permeabilityModel.startsWith("free") }
            val blob = points.first { it.response.permeabilityModel.startsWith("correlation") }
            println(
                "%-28s %8.4f %8.2f %8.4f %8.2f %10.2f %10.2f %8.3f %8.3f".format(
                    label.take(28), layer.volumeFraction, layer.crossoverUnits,
                    layer.graftedLongitudinalModulus, layer.layerStiffness,
                    segment.response.screeningLength, blob.response.screeningLength,
                    layer.blobStackHeight, layer.stretchRatio
                )
            )
        }
    println()
    result.parameters.tileFootprints.forEach { footprint ->
        println("--- drainage, tile $footprint ".padEnd(104, '-'))
        println(
            "%-28s %-26s %11s %11s %9s %10s".format(
                "design point", "permeability model", "tau [s]", "f_c [Hz]", "x 1 kHz", "sqrt(k)/h"
            )
        )
        result.designPoints
            .filter { it.footprint == footprint && it.stiffnessMultiplier == 1.0 }
            .forEach {
                println(
                    "%-28s %-26s %11.3e %11.3e %9.1f %10.3f".format(
                        it.layer.label.take(28), it.response.permeabilityModel.take(26),
                        it.response.relaxationTime, it.response.cornerFrequency,
                        it.response.marginAtOneKilohertz,
                        it.response.screeningLengthOverThickness
                    )
                )
            }
        println()
    }
    println("--- which drainage path, and which dissipation channel ".padEnd(104, '-'))
    println(
        "%-28s %-12s %11s %11s %11s %11s %11s".format(
            "design point", "tile", "lateral l", "vertical l", "tau_lat", "tau_vert", "tau_Zimm"
        )
    )
    result.designPoints
        .filter { it.stiffnessMultiplier == 1.0 && it.response.permeabilityModel.startsWith("free") }
        .forEach {
            println(
                "%-28s %-12s %11.2f %11.2f %11.3e %11.3e %11.3e".format(
                    it.layer.label.take(28), it.footprint,
                    it.response.lateralDrainageLength, it.response.verticalDrainageLength,
                    it.lateralDrainageTime, it.verticalDrainageTime, it.zimmRelaxationTime
                )
            )
        }
    println()
    val stokesShare = result.designPoints
        .first { it.footprint == "40 x 40 nm" && it.layer.layerHeight == 10.0 &&
                it.response.permeabilityModel.startsWith("free") }
    println(
        "tile Stokes drag is ${"%.1f".format(stokesShare.response.stokesDragFraction * 100.0)}% " +
                "of the total at the 10 nm design point; inertial time " +
                "${"%.2e".format(stokesShare.inertialTime)} s, Q = " +
                "${"%.2e".format(stokesShare.qualityFactor)} — overdamped, first-order relaxation holds"
    )
    println()
    val worst = result.designPoints.minBy { it.response.cornerFrequency }
    println(
        "worst case anywhere in the design space: ${worst.layer.label}, ${worst.footprint}, " +
                "${worst.response.permeabilityModel}, stiffness x${worst.stiffnessMultiplier} -> " +
                "f_c = ${"%.3e".format(worst.response.cornerFrequency)} Hz " +
                "(${"%.1f".format(worst.response.marginAtOneKilohertz)}x the 1 kHz requirement)"
    )
    println()
    println("--- the 1 kHz contour in (thickness, volume fraction) ".padEnd(104, '-'))
    println(
        "%-12s %10s %14s %12s %12s %s".format(
            "tile", "h [nm]", "phi at 1 kHz", "x design phi", "sqrt(k)/h", "Darcy premise there"
        )
    )
    result.bindingContour.forEach {
        println(
            "%-12s %10.1f %14.5f %12.4f %12.3f %s".format(
                it.footprint, it.layerThickness, it.bindingVolumeFraction,
                it.timesTheGen1VolumeFraction, it.screeningLengthOverThicknessThere,
                if (it.darcyPremiseHoldsThere) "holds" else "FAILS — model out of domain"
            )
        )
    }
    println()
    println("--- what would make poroelasticity bind at 1 kHz ".padEnd(104, '-'))
    result.binding.forEach {
        println("%-16s %14.4g %-6s  reachable: %s".format(it.variable, it.value, it.unit, it.reachable))
        println("                 ${it.statement}")
    }
    println()
    println("written: ${output.path} (${result.designPoints.size} design points, ${result.sweep.size} sweep points)")
}
