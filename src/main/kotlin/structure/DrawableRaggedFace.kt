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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.tile.HoneycombCrossSectionGeometry

/**
 * `T-258` — the axial relief a two-length honeycomb raster leaves on its two end faces, read at
 * the pair that is actually **drawable**.
 *
 * `C-0147` (`T-231`) priced the relief at `C-0140`'s **4 / 8 bp**; `C-0151` then showed that pair
 * does not close on caDNAno's `±5 bp` scaffold rule and that the drawable one is `102 / 109`,
 * whose relief is **7 / 14 bp**. Everything here is `C-0147`'s own construction re-run at the new
 * lengths — the level walk of [twoLengthRaster], the rim census of [gapFacingRimLevels] and the
 * period of [sequencePeriod] — so nothing is transcribed.
 *
 * ## The axis, which is the half that does not scale
 *
 * A four-layer honeycomb block's **gap-facing** surface is one *column* of the cross-section, a
 * row of duplex sidewalls all parallel to the tile plane; the relief changes where a helix
 * **ends**, which is an `x` coordinate *in* that plane. So the two ragged faces are the tile's
 * axial **rim** at `x = 0` and `x = L`, at **every** column and not only at the gap-facing one —
 * which is what [RaggedFaceRelief.spreadByColumn] measures — and the coefficient of the relief on
 * `T-5b`'s normal-direction flatness field is **exactly zero**. That statement carries no
 * magnitude, so `1.75×` of relief cannot move it.
 */
data class RaggedFaceRelief(

    /** The front face's own level spread, in base pairs. */
    val frontBasePairs: Int,

    /** The rear face's own level spread, in base pairs. */
    val rearBasePairs: Int,

    /** The block's axial extent, in base pairs — `2·max − min` of the two row lengths. */
    val axialExtentBasePairs: Int,

    /** The scaffold this raster spends, in nucleotides. */
    val scaffoldNucleotides: Int,

    /** The period of the gap-facing column's rim level sequence, in raster rows. */
    val gapFacingRimPeriodRows: Int,

    /** The gap-facing column's own rim spread, in base pairs. */
    val gapFacingRimSpreadBasePairs: Int,

    /** The front-face level spread of each cross-section **column**, in base pairs. */
    val spreadByColumn: List<Int>,

    /** The front relief in nm. */
    val frontNm: Double,

    /** The rear relief in nm. */
    val rearNm: Double
) {

    /** The rim modulation's wavelength in nm at the honeycomb's in-plane row pitch. */
    val modulationWavelengthNm: Double
        get() = gapFacingRimPeriodRows * HoneycombCrossSectionGeometry.rowPitch()
}

/**
 * The relief an `[rasterRows] × [helicesPerRow]` honeycomb block carries when turn sense 1 takes
 * [senseOne] base pairs and turn sense 2 takes [senseTwo].
 *
 * @param rasterRows `m`, the block's **in-plane** row count.
 * @param helicesPerRow `n`, its **thickness** count; a raster row's two ends must both carry the
 *   downward vertical bond, so it must be even.
 */
fun raggedFaceRelief(
    rasterRows: Int,
    helicesPerRow: Int,
    senseOne: Int,
    senseTwo: Int
): RaggedFaceRelief {
    require(senseOne > 0 && senseTwo > 0) {
        "both row lengths must be positive, were: $senseOne, $senseTwo"
    }
    val turns = honeycombRasterTurns(honeycombXRasterPath(rasterRows, helicesPerRow))
    val raster = twoLengthRaster(turns, senseOne, senseTwo)
    val rim = gapFacingRimLevels(turns, raster, column = 0)
    val byColumn = (0 until helicesPerRow).map { column ->
        val levels = gapFacingRimLevels(turns, raster, column).map { it.second }
        if (levels.isEmpty()) 0 else levels.max() - levels.min()
    }
    return RaggedFaceRelief(
        frontBasePairs = raster.frontSpreadBasePairs,
        rearBasePairs = raster.rearSpreadBasePairs,
        axialExtentBasePairs = raster.axialExtentBasePairs,
        scaffoldNucleotides = raster.scaffoldNucleotides,
        gapFacingRimPeriodRows = sequencePeriod(rim.map { it.second }),
        gapFacingRimSpreadBasePairs = rim.maxOf { it.second } - rim.minOf { it.second },
        spreadByColumn = byColumn,
        frontNm = raster.frontSpreadBasePairs * Gen1Tile.RISE_PER_BASE_PAIR,
        rearNm = raster.rearSpreadBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    )
}

/**
 * `C-0147`'s residual channel: the fraction of the free stroke a rim relief of [reliefNm] can
 * move the flatness of a plate on a Winkler foundation, as one product.
 *
 * `bound = (2·relief/π) / [halfSpanNm] × 1/(1 + (2π[bendingLengthNm]/[wavelengthNm])⁴) ×
 * [freeEdgePenalty]`
 *
 * The first factor is the rim's own **lever**, entered as the fundamental of the square wave the
 * rim actually is; the second is `C-0006`'s infinite-plate ripple transfer; the third is
 * `CLAUDE.md`'s measured **50×** correction for the fact that the transfer function
 * over-attenuates a *rim* perturbation against a finite-plate solve. **Only the first factor
 * contains the relief**, so the whole of `T-258` on this channel is one linear rescaling — which
 * is exactly why the other two are emitted beside it.
 */
fun rimModulationBound(
    reliefNm: Double,
    halfSpanNm: Double,
    bendingLengthNm: Double,
    wavelengthNm: Double,
    freeEdgePenalty: Double
): Double {
    require(reliefNm >= 0.0) { "reliefNm must not be negative, was: $reliefNm" }
    require(halfSpanNm > 0.0) { "halfSpanNm must be positive, was: $halfSpanNm" }
    require(freeEdgePenalty > 0.0) { "freeEdgePenalty must be positive, was: $freeEdgePenalty" }
    return squareWaveFundamentalAmplitude(reliefNm) / halfSpanNm *
            loadRippleTransmission(bendingLengthNm, wavelengthNm) * freeEdgePenalty
}
