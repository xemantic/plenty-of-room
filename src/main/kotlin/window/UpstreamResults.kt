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

package com.xemantic.nano.plentyofroom.window

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File

/**
 * The readers `T-2` consumes its inputs through.
 *
 * `T-2` is a **synthesis** task: every physical quantity it needs already exists, verified,
 * in a claim that carries its own validity range. It therefore reads the **emitting study's
 * own machine-readable result file** rather than re-typing a table out of the claim's prose.
 * A number copied from a claim's prose is a transcription risk; a number read from the file
 * the claim was written from is not, and the gate-5 tests check the two agree by reproducing
 * each claim's published figures from its own file.
 *
 * Nothing here computes physics. Everything here is parsing.
 */

private val reader = Json { ignoreUnknownKeys = true }

/** One `(profile × interaction)` layer response at one design point of `T-1d`'s sweep. */
@Serializable
data class ScfResponse(
    val profile: String,
    val interaction: String,
    val monomersPerChain: Double,
    val chainMolarMass: Double,
    val meanVolumeFraction: Double,
    val peakVolumeFraction: Double,
    val firstMomentHeight: Double,
    val idealEndToEnd: Double,
    val stretchingRatio: Double,
    val coilOverlap: Double,
    val equilibriumStiffness: Double,
    val stiffnessAtFourFifths: Double,
    val stiffnessAtSevenTenths: Double,
    val strokeUnderTargetForce: Double,
    val secantStiffness: Double,
    val positionalRms: Double
)

/** One `(layer height, grafting density)` point of `T-1d`'s 183-point sweep. */
@Serializable
data class ScfDesignPoint(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val responses: List<ScfResponse>
) {

    /** The three measurement-anchored interaction laws, at the solved profile. */
    val solved: List<ScfResponse> get() = responses.filter { it.profile == "scf" }

}

/** `T-1d`'s own emitted window, per `(height, interaction, criteria)`. */
@Serializable
data class ScfStrokeWindow(
    val layerHeight: Double,
    val profile: String,
    val interaction: String,
    val requiredStroke: Double,
    val requiredStretchingRatio: Double,
    val requiredCoilOverlap: Double,
    val lowestGraftingDensity: Double? = null,
    val highestGraftingDensity: Double? = null,
    val empty: Boolean
)

/** Everything `T-2` reads out of `gpd/results/T-1d-scf-density-profile.json`. */
data class ScfResults(
    val designPoints: List<ScfDesignPoint>,
    val strokeWindows: List<ScfStrokeWindow>,
    val restingLoad: Double,
    val monomerVolume: Double,
    val tileArea: Double
)

/** Reads `T-1d`'s result file — `C-0011`'s provenance. */
fun readScfResults(file: File): ScfResults {
    val root = reader.parseToJsonElement(file.readText()).jsonObject
    val parameters = root.getValue("parameters").jsonObject
    return ScfResults(
        designPoints = root.getValue("designPoints").jsonArray
            .map { reader.decodeFromJsonElement(ScfDesignPoint.serializer(), it) },
        strokeWindows = root.getValue("strokeWindows").jsonArray
            .map { reader.decodeFromJsonElement(ScfStrokeWindow.serializer(), it) },
        restingLoad = parameters.scalar("restingLoad"),
        monomerVolume = parameters.scalar("monomerVolume"),
        tileArea = parameters.scalar("tileArea")
    )
}

/** One `T-3` threshold record — one `(layer model, height, buffer)` triple. */
@Serializable
data class ActuatorThreshold(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val biasForHundredPiconewtonBlocking: Double? = null,
    val biasForThreeNanometreStroke: Double? = null,
    val biasForSimultaneousTarget: Double? = null,
    val largestModelValidBias: Double,
    val largestModelValidStroke: Double,
    val strokeAtLargestModelValidBias: Double,
    val blockingForceAtLargestModelValidBias: Double,
    val loadedStiffnessRatioAtSimultaneousTarget: Double? = null
)

/** One `T-3` layer design point — the layer `C-0003` model it coupled the field to. */
@Serializable
data class ActuatorDesignPoint(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val chainMolarMass: Double,
    val restingVolumeFraction: Double,
    val stiffnessAtRest: Double,
    val stiffnessAtFourFifths: Double,
    val strokeUnderHundredPiconewtonDeadLoad: Double
)

/** One `T-3` coupled operating point, reduced to the stability quantities `T-2` needs. */
@Serializable
data class ActuatorOperatingPoint(
    val medium: String,
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val loadedOperatingHeight: Double,
    val loadedVolumeFraction: Double,
    val loadedBrushStiffness: Double,
    val loadedElectrostaticStiffness: Double,
    val loadedEffectiveStiffness: Double
)

/** Everything `T-2` reads out of `gpd/results/T-3-stroke-and-blocking-force.json`. */
data class ActuatorResults(
    val thresholds: List<ActuatorThreshold>,
    val designPoints: List<ActuatorDesignPoint>,
    val operatingPoints: List<ActuatorOperatingPoint>,
    val trustedBiasCeiling: Double,
    val biasCeiling: Double
)

/** Reads `T-3`'s result file — `C-0012`'s provenance. */
fun readActuatorResults(file: File): ActuatorResults {
    val root = reader.parseToJsonElement(file.readText()).jsonObject
    val parameters = root.getValue("runParameters").jsonObject
    return ActuatorResults(
        thresholds = root.getValue("thresholds").jsonArray
            .map { reader.decodeFromJsonElement(ActuatorThreshold.serializer(), it) },
        designPoints = root.getValue("designPoints").jsonArray
            .map { reader.decodeFromJsonElement(ActuatorDesignPoint.serializer(), it) },
        operatingPoints = root.getValue("operatingPoints").jsonArray
            .map { reader.decodeFromJsonElement(ActuatorOperatingPoint.serializer(), it) },
        trustedBiasCeiling = parameters.scalar("trustedBiasCeiling"),
        biasCeiling = parameters.scalar("biasCeiling")
    )
}

/** One load class of one `T-14` foundation state — best and worst layout. */
@Serializable
data class LayoutLoadClass(
    val loadCase: String,
    val jointBestForce: Double,
    val jointWorstForce: Double,
    val jointRatio: Double
)

/** `T-14`'s complete layout sweep at one foundation stiffness. */
@Serializable
data class LayoutFoundationState(
    val label: String,
    val foundationMultiplier: Double,
    val foundationStiffness: Double,
    val crossoverAlpha: Double,
    val loadClasses: List<LayoutLoadClass>
)

/** `T-14`'s flatness optimum, searched over grid *shapes* rather than counts. */
@Serializable
data class LayoutFlatnessMinimum(
    val model: String,
    val squareGridAttachments: Int,
    val bestShape: String,
    val bestAttachments: Int,
    val bestPeakDishingOverStroke: Double,
    val bestForcePerAttachment: Double,
    val bestPeakCrossoverForce: Double,
    val attachmentsPerCrossover: Double,
    val crossovers: Int
)

/** Everything `T-2` reads out of `gpd/results/T-14-crossover-phase-and-registration.json`. */
data class LayoutResults(
    val foundationStates: List<LayoutFoundationState>,
    val flatnessMinima: List<LayoutFlatnessMinimum>,
    val latticeTileArea: Double,
    val unzipAllowableLower: Double,
    val unzipAllowableUpper: Double,
    val shearAllowable: Double
) {

    /**
     * The layer stiffness in pN/nm that `T-14`'s foundation multiplier of 1.0 stands for.
     *
     * `T-14` carries the foundation as a Winkler modulus in pN/nm³, referred to `C-0001`'s
     * secant at the 10 nm design point over the **nominal 40 × 40 nm** footprint, while its
     * own lattice is 40 × 40.35 nm. Recovering the stiffness therefore uses 1600 nm², and
     * the gate-1 test checks the round trip lands on `C-0001`'s published 20.201 pN/nm.
     */
    val referenceLayerStiffness: Double
        get() = foundationStates.first()
            .let { it.foundationStiffness / it.foundationMultiplier } * NOMINAL_TILE_AREA

}

/** Reads `T-14`'s result file — `C-0015`'s provenance. */
fun readLayoutResults(file: File): LayoutResults {
    val root = reader.parseToJsonElement(file.readText()).jsonObject
    val parameters = root.getValue("parameters").jsonObject
    return LayoutResults(
        foundationStates = root.getValue("foundationStates").jsonArray
            .map { reader.decodeFromJsonElement(LayoutFoundationState.serializer(), it) },
        flatnessMinima = root.getValue("flatnessMinimum").jsonArray
            .map { reader.decodeFromJsonElement(LayoutFlatnessMinimum.serializer(), it) },
        latticeTileArea = parameters.scalar("tileArea"),
        unzipAllowableLower = parameters.scalar("duplexUnzipAllowableLower"),
        unzipAllowableUpper = parameters.scalar("duplexUnzipAllowableUpper"),
        shearAllowable = parameters.scalar("duplexShearAllowable")
    )
}

/** §3's nominal tile footprint area in nm², the area every layer force here is referred to. */
const val NOMINAL_TILE_AREA: Double = 1600.0

/**
 * Reads a scalar out of a parameter block, tolerating the two conventions in circulation
 * across the result files: a JSON number, and a number serialised as a string.
 */
private fun JsonObject.scalar(name: String): Double {
    val element = getValue(name)
    val content = element.toString().trim('"')
    return content.toDoubleOrNull()
        ?: error("parameter '$name' is not a scalar: $element")
}
