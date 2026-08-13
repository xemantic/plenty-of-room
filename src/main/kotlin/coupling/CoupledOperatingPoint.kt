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

package com.xemantic.nano.plentyofroom.coupling

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.max

/**
 * The reader `T-16` consumes `C-0012` through.
 *
 * Per `gpd/README.md`, a number that decides a verdict is read from the **emitting study's own
 * result file**, never re-typed out of a claim's prose. `T-16` disagrees with `C-0012` about
 * the *scope* of one table, so it is doubly important that it reproduces that table from
 * `C-0012`'s own numbers before saying anything about it.
 *
 * Nothing here computes physics. `window/UpstreamResults.kt` reads the same file for `T-2` but
 * projects out different fields and is owned by another task; this is a separate projection,
 * not an import of it.
 */

private val reader = Json { ignoreUnknownKeys = true }

/** One `T-3` operating record, projected onto the quantities the coupling requirement needs. */
@Serializable
data class CoupledOperatingPoint(
    val medium: String,
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val appliedBias: Double,
    val blockingForce: Double,
    val stroke: Double,
    val outputForceAtThreeNanometres: Double? = null,
    val loadedOperatingHeight: Double,
    val loadedBrushStiffness: Double,
    val loadedElectrostaticStiffness: Double? = null,
    val loadedEffectiveStiffness: Double? = null,
    val modelValid: Boolean,
    val withinTrustedBias: Boolean
) {

    /** `max(0, |k_eff|)` at the held gap — `C-0012`'s *"number an output coupling has to supply"*. */
    val stabilityFloor: Double?
        get() = loadedEffectiveStiffness?.let { max(0.0, -it) }
}

/** Reads `gpd/results/T-3-stroke-and-blocking-force.json`. */
fun readCoupledOperatingPoints(file: File): List<CoupledOperatingPoint> =
    reader.parseToJsonElement(file.readText()).jsonObject
        .getValue("operatingPoints").jsonArray
        .map { reader.decodeFromJsonElement(CoupledOperatingPoint.serializer(), it) }

/**
 * One `T-3` threshold record, projected onto the bias this task's whole verdict is read at.
 *
 * `C-0012` obtains [biasForSimultaneousTarget] as an **interpolated first crossing on its own
 * bias grid** — `firstCrossing` between two samples — not as a located root. Its grid is
 * `{…, 0.10, 0.25, …}` at exactly the place the crossing falls, so the interpolation spans a
 * 2.5× interval. `T-16` bisects for the same quantity instead, and the departure between the
 * two is reported rather than absorbed: it is the size of the effect `CH-0016` is about.
 */
@Serializable
data class CoupledThreshold(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val biasForSimultaneousTarget: Double? = null,
    val biasBracketForSimultaneousTarget: String = "none"
)

/** Reads the `thresholds` array of `gpd/results/T-3-stroke-and-blocking-force.json`. */
fun readCoupledThresholds(file: File): List<CoupledThreshold> =
    reader.parseToJsonElement(file.readText()).jsonObject
        .getValue("thresholds").jsonArray
        .map { reader.decodeFromJsonElement(CoupledThreshold.serializer(), it) }
