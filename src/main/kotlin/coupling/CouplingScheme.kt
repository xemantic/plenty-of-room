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

/**
 * Task `T-16` — one candidate output coupling: `n` parallel load paths, each a series chain
 * from the tile to ground.
 *
 * ## The two ways a coupling fails, and only one of them is `C-0014`'s
 *
 * `C-0014` sized an **anchor**, whose only job is to be stiff enough; its failure mode is
 * being too soft. A **load** has the opposite failure mode as well. A coupling stiffer than
 * `W(s*)/s*` puts the operating point *below* §3's 3 nm — at 45 duplex links it is 4950 pN/nm
 * and the stroke is essentially zero — and it concentrates the whole output force onto its
 * own load paths, where `C-0006`'s allowables are 10 pN in unzip geometry and 48–65 pN in
 * shear. Both failure modes are evaluated for every scheme.
 *
 * ## Whether `C-0009`'s concentration factor applies
 *
 * It applies to a load path whose reaction has to travel **through the tile's lattice** to
 * reach the layer that balances it — `C-0009` measures 2.3–7.6× for exactly that geometry.
 * It does **not** apply to a coupling matched to the load it opposes: `C-0015` shows that one
 * attachment row per duplex makes the per-load-path crossover force **exactly zero**, because
 * every beam then carries the identical load and no interface transmits anything. The flag
 * [loadPathCrossesLattice] carries that distinction rather than applying 7.6 everywhere.
 */
@Serializable
data class CouplingPathElement(
    val name: String,
    val stiffness: Double
)

/** One candidate coupling. */
@Serializable
data class CouplingScheme(

    val name: String,

    /** How many parallel load paths the coupling presents to the tile. */
    val attachmentCount: Int,

    /** The series chain of one path, tile to ground, as tangent stiffnesses in `pN/nm`. */
    val path: List<CouplingPathElement>,

    /**
     * Whether the reaction has to cross the tile's own lattice to reach the layer.
     *
     * `true` for a concentrated lever, where `C-0009`'s 2.3–7.6× concentration applies;
     * `false` for a coupling matched one row per duplex, where `C-0015`'s exact zero does.
     */
    val loadPathCrossesLattice: Boolean,

    /**
     * Whether the path loads a crossover **axially**, i.e. in the direction `C-0009` models as
     * a rigid constraint with nothing cited behind it and `T-9` has not produced.
     */
    val dependsOnCrossoverAxialCompliance: Boolean = false,

    /** Whether these can be the same attachments `C-0015`'s flatness count already requires. */
    val reusesFlatnessAttachments: Boolean = false
) {

    init {
        require(attachmentCount > 0) { "attachmentCount must be positive, was: $attachmentCount" }
        require(path.isNotEmpty()) { "path must not be empty" }
    }

    /** The series stiffness of one path in `pN/nm`. */
    val perPathStiffness: Double get() = seriesStiffness(path.map { it.stiffness })

    /** The coupling's stiffness at the tile in `pN/nm`. */
    val stiffness: Double get() = attachmentCount * perPathStiffness

    /** Each element's share of one path's compliance — leaf `A8.2`'s budget. */
    val complianceBudget: List<Double> get() = complianceShares(path.map { it.stiffness })

    /** The element carrying the largest share, with ties broken on position, never on order. */
    val dominantComplianceTerm: String
        get() = dominantCompliance(path.map { it.name to it.stiffness }).first

    /** The static share of [totalForce] carried by one path in pN. */
    fun perPathStaticForce(totalForce: Double): Double = totalForce / attachmentCount

    /**
     * The peak force on one path in pN, with [concentrationFactor] applied **only** where the
     * reaction crosses the lattice.
     */
    fun concentratedPathForce(totalForce: Double, concentrationFactor: Double): Double =
        perPathStaticForce(totalForce) * if (loadPathCrossesLattice) concentrationFactor else 1.0

    /** The thermal force on one path in pN, `√(k_BT k)/N`. */
    val perPathThermalForce: Double get() = perAnchorThermalForce(stiffness, attachmentCount)
}

/**
 * `C-0006`'s per-load-path allowables in pN, **cited** through `C-0009`/`C-0015`.
 *
 * **Not** §4(f)'s 35–60 pN, which is a whole-cross-section disassembly force for a 6–8-helix
 * tube at 5.5 pN/s, and not a per-path allowable at all. And a DNA rupture force without a
 * loading rate is not a material constant: the same origami class runs from ~42 pN at
 * 5.5 pN/s to ~75 pN at 1.8e5 pN/s.
 */
object PerPathAllowable {

    /** Unzip geometry, the geometry to avoid — **CITED, MEASURED**, Essevaz-Roulet et al. (1997). */
    const val UNZIP: Double = 10.0

    /** Single-duplex shear, quasi-static — **CITED, MEASURED**, Strunz et al. (1999). */
    const val SHEAR: Double = 48.0

    /** The hard ceiling: every origami helix is nicked — **CITED**, van Mameren et al. (2009). */
    const val NICKED_CEILING: Double = 65.0
}

/** The verdict on one scheme at one solved actuator state. */
@Serializable
data class CouplingSchemeVerdict(
    val scheme: String,
    val attachmentCount: Int,
    val couplingStiffness: Double,
    val dominantComplianceTerm: String,
    val dominantComplianceShare: Double,
    val mandatedStiffness: Double,
    val stiffnessOverMandated: Double,
    val stabilityFloor: Double,
    val meetsStabilityFloor: Boolean,
    val deliveredStroke: Double?,
    val deliveredForce: Double?,
    val strokeOverTarget: Double?,
    val perPathStaticForce: Double,
    val perPathPeakForce: Double,
    val perPathThermalForce: Double,
    val clearsUnzip: Boolean,
    val clearsShear: Boolean,
    val lateralStiffness: Double,
    val lateralOverBound: Double,
    val yawStiffness: Double,
    val yawOverBound: Double,
    val reusesFlatnessAttachments: Boolean,
    val dependsOnCrossoverAxialCompliance: Boolean,
    val verdict: String
)
