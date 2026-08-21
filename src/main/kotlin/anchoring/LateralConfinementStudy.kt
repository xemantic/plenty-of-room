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
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Task `T-12` / leaf `A1.2` (with `A1.1` as its bound table and `A8.2` for the joint budget) —
 * lateral confinement of the Gen-1 tile.
 *
 * ```shell
 * ./gradlew study -Pstudy=anchoring.LateralConfinementStudyKt -PbuildDirectory=build-t12
 * ```
 *
 * Emits `gpd/results/T-12-lateral-confinement.json`, deterministically.
 *
 * `C-0010` established that the layer's lateral restoring stiffness is **exactly zero by
 * symmetry** and declined to specify a scheme. This study either names one or shows that none
 * compatible with §3 exists — and the answer turns on a single dimensionless number, the
 * **anisotropy ratio** of an anchor, which is decided by the anchor's *orientation* and not by
 * its material.
 */

// ---------------------------------------------------------------- the §3 design point

/** §3 tile edge, nm. */
private const val TILE_EDGE = 40.0

/** §3 target force, pN. */
private const val TARGET_FORCE = 100.0

/** §3 acceptable and desired strokes, nm. */
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0

/** The half-diagonal of the footprint — the tile's worst material point. */
private val CORNER_RADIUS = LateralRequirement.cornerRadius(TILE_EDGE, TILE_EDGE)

/**
 * `C-0003`'s stroke bracket at each §3 layer height, from which the layer's **secant**
 * stiffness follows as `k = F/δ` — derived here rather than copied, because `C-0003` tabulates
 * the secant only at the 10 nm point.
 */
private data class LayerPoint(val height: Double, val strokeMin: Double, val strokeMax: Double) {
    val secantStiff: Double get() = TARGET_FORCE / strokeMin
    val secantSoft: Double get() = TARGET_FORCE / strokeMax
    val storedEnergyMin: Double get() = 0.5 * TARGET_FORCE * strokeMin
    val storedEnergyMax: Double get() = 0.5 * TARGET_FORCE * strokeMax
}

private val layerPoints = listOf(
    LayerPoint(5.0, 0.47, 1.53),
    LayerPoint(7.0, 1.55, 3.21),
    LayerPoint(10.0, 3.83, 6.01)
)

/** The nominal design point: the 10 nm layer, which is the only one with a §3-acceptable stroke. */
private val nominalLayer = layerPoints.last()

// ---------------------------------------------------------------- result records

@Serializable
private data class RequirementRecord(
    val positionalBound: Double,
    val thermalEnergy: Double,
    val perCoordinateStiffness: Double,
    val radialStiffness: Double,
    val worstPointStiffness: Double,
    val cornerRadius: Double,
    val footprintRadius: Double,
    val yawStiffnessAtCorner: Double,
    val yawStiffnessAtFootprintRadius: Double,
    val yawAngleBound: Double,
    val cornerRmsAtBareBound: Double,
    val declaredAcceptanceQuantity: String,
    val note: String
)

@Serializable
private data class TheoremRecord(
    val statement: String,
    val samples: List<TheoremSample>,
    val worstRatio: Double,
    val holds: Boolean,
    val consequence: String
)

@Serializable
private data class TheoremSample(
    val tension: Double,
    val extension: Double,
    val transverseStiffness: Double,
    val tangentStiffness: Double,
    val secantOverTangent: Double
)

@Serializable
private data class SchemeRecord(
    val id: String,
    val name: String,
    val topology: String,
    val layerHeight: Double,
    val elementLength: Double,
    val anchorCount: Int,
    val endCondition: String,
    val bendingRigidity: Double,
    val axialStiffness: Double,
    val transverseStiffness: Double,
    val lateralStiffness: Double,
    val yawStiffness: Double,
    val normalStiffness: Double,
    val anisotropyRatio: Double,
    val lateralMargin: Double,
    val yawMargin: Double,
    val cornerInPlaneRms: Double,
    val strokeRetainedSoftLayer: Double,
    val strokeRetainedStiffLayer: Double,
    val strokeLostPercentWorst: Double,
    val perAnchorLateralForce: Double,
    val peakPathForce: Double,
    val cableEntryPathForce: Double,
    val bucklingLoad: Double,
    val compressionPerAnchor: Double,
    val buckles: Boolean,
    val verdict: String,
    val bindingConstraint: String
)

@Serializable
private data class TetherDesignRecord(
    val layerHeight: Double,
    val anchorCount: Int,
    val kuhnLength: Double,
    val kuhnProvenance: String,
    val maximumAdmissibleContour: Double,
    val maximumAdmissibleNucleotides: Double,
    val contourLength: Double,
    val contourNucleotides: Double,
    val extensionFraction: Double,
    val tensionPerTether: Double,
    val totalPreload: Double,
    val preloadFractionOfTarget: Double,
    val lateralStiffness: Double,
    val normalStiffness: Double,
    val anisotropyRatio: Double,
    val strokeRetainedSoftLayer: Double,
    val strokeLostPercentSoftLayer: Double,
    val yawStiffness: Double,
    val yawMargin: Double,
    val peakPathForce: Double,
    val verdict: String
)

@Serializable
private data class CableRecord(
    val stroke: Double,
    val tetherLength: Double,
    val tension: Double,
    val normalForcePerTether: Double,
    val normalForceTotal: Double,
    val normalSecantStiffness: Double,
    val strokeRetainedSoftLayer: Double,
    val tensionOverShearAllowable: Double,
    val tensionOverCeiling: Double,
    val concentratedPathForce: Double,
    val verdict: String
)

@Serializable
private data class MinimumLengthRecord(
    val stroke: Double,
    val allowable: String,
    val allowableForce: Double,
    val concentrationFactor: Double,
    val minimumTetherLength: Double,
    val lateralStiffnessAtThatLength: Double,
    val lateralMargin: Double
)

@Serializable
private data class SeriesRecord(
    val variant: String,
    val element: String,
    val stiffness: Double,
    val complianceShare: Double,
    val provenance: String
)

@Serializable
private data class JointBudgetRecord(
    val variant: String,
    val elements: List<SeriesRecord>,
    val perAnchorStiffness: Double,
    val fourAnchorStiffness: Double,
    val margin: Double,
    val dominantComplianceTerm: String,
    val maximumSpacerNucleotides: Double
)

@Serializable
private data class PlacementRecord(
    val arrangement: String,
    val anchorRadius: Double,
    val budgetRadius: Double,
    val yawOverTranslationMargin: Double,
    val note: String
)

@Serializable
private data class PadRecord(
    val layerHeight: Double,
    val healingLength: Double,
    val storedEnergyMin: Double,
    val storedEnergyMax: Double,
    val stiffnessCeilingMin: Double,
    val stiffnessCeilingMax: Double,
    val marginMin: Double,
    val marginMax: Double,
    val energyThreshold: Double,
    val thresholdInThermalUnits: Double,
    val verdict: String
)

@Serializable
private data class ElectrodeRecord(
    val layerHeight: Double,
    val forceDecayLength: Double,
    val interactionEnergy: Double,
    val gapScreeningLength: Double,
    val optimalPeriod: Double,
    val stiffnessAtFullModulation: Double,
    val marginAtFullModulation: Double,
    val thresholdModulationDepth: Double,
    val rippleTransferAtOptimum: Double,
    val dishingFractionOfStrokeAtThreshold: Double,
    val verdict: String
)

@Serializable
private data class LateralConfinementResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val requirement: RequirementRecord,
    val anisotropyTheorem: TheoremRecord,
    val noSchemeBaseline: Map<String, String>,
    val schemes: List<SchemeRecord>,
    val entropicTetherDesigns: List<TetherDesignRecord>,
    val cableNonlinearity: List<CableRecord>,
    val minimumTetherLengths: List<MinimumLengthRecord>,
    val jointBudget: List<JointBudgetRecord>,
    val placementRule: List<PlacementRecord>,
    val graftingPad: List<PadRecord>,
    val patternedElectrode: List<ElectrodeRecord>,
    val validity: List<String>,
    val citedNotDerived: List<String>,
    val verdict: Map<String, String>
)

// ---------------------------------------------------------------- the schemes

private val requiredLateral = LateralRequirement.translationalStiffness()
private val requiredYaw = LateralRequirement.yawStiffness(radius = CORNER_RADIUS)

private fun scheme(
    id: String,
    name: String,
    topology: String,
    layerHeight: Double,
    elementLength: Double,
    count: Int,
    endCondition: BeamEndCondition,
    bendingRigidity: Double,
    axial: Double,
    transverse: Double,
    inPlane: Boolean,
    tangential: Boolean = false,
    compressionPerAnchor: Double = 0.0,
    layer: LayerPoint = nominalLayer
): SchemeRecord {
    val links = if (inPlane) {
        radialInPlaneLinks(axial, transverse, CORNER_RADIUS, count).let { radial ->
            if (!tangential) radial
            else radial.map { it.copy(azimuth = it.azimuth + PI / 2.0) }
        }
    } else {
        verticalLinks(axial, transverse, CORNER_RADIUS, count)
    }
    val assembly = AnchorAssembly(links)
    val lateral = assembly.weakestLateralStiffness
    val yaw = assembly.yawStiffness
    val normal = assembly.normalStiffness
    val critical = eulerBucklingLoad(bendingRigidity, elementLength, endCondition)
    val buckles = !inPlane && compressionPerAnchor > critical
    val lateralMargin = lateral / requiredLateral
    val yawMargin = yaw / requiredYaw
    val perAnchor = lateral * sqrt(thermalEnergy() / lateral) / count
    val retainedSoft = strokeRetainedFraction(normal, layer.secantSoft)
    val retainedStiff = strokeRetainedFraction(normal, layer.secantStiff)
    // an in-plane tether must ALSO stretch to let the tile descend, and that geometric tension
    // is checked here rather than in a separate table, because it is a property of the scheme
    val cableEntryForce = if (inPlane) peakPathForce(
        cableTension(AnchorMaterials.DUPLEX_STRETCH_MODULUS, elementLength, ACCEPTABLE_STROKE),
        PerPathAllowables.CONCENTRATION_FACTOR_MAX
    ) else 0.0
    val binding = when {
        buckles -> "BUCKLING: each anchor carries ${compressionPerAnchor.roundedForProse()} pN against a " +
                "critical load of ${critical.roundedForProse()} pN, and a buckled column has " +
                "no lateral stiffness at all"
        lateralMargin < 1.0 -> "lateral stiffness, short by ${(1.0 / lateralMargin).roundedForProse()}x"
        yawMargin < 1.0 -> "yaw stiffness, short by ${(1.0 / yawMargin).roundedForProse()}x"
        retainedSoft < 0.9 -> "the stroke: the anchors take " +
                "${(100.0 * (1.0 - retainedSoft)).roundedForProse()} % " +
                "of it at the soft end of the C-0003 secant bracket, against a declared 10 % budget"
        cableEntryForce > PerPathAllowables.SHEAR ->
            "the CABLE tension: ${cableEntryForce.roundedForProse()} pN enters the tile at a " +
                    "3 nm stroke once " +
                    "C-0009's concentration factor is applied, against a 48 pN shear allowable"
        else -> "none — passes lateral, yaw, the 10 % stroke budget and the cable check together"
    }
    return SchemeRecord(
        id = id,
        name = name,
        topology = topology,
        layerHeight = layerHeight,
        elementLength = elementLength,
        anchorCount = count,
        endCondition = endCondition.name,
        bendingRigidity = bendingRigidity,
        axialStiffness = axial,
        transverseStiffness = transverse,
        lateralStiffness = lateral,
        yawStiffness = yaw,
        normalStiffness = normal,
        anisotropyRatio = anisotropyRatio(lateral, normal),
        lateralMargin = lateralMargin,
        yawMargin = yawMargin,
        cornerInPlaneRms = LateralRequirement.pointRms(lateral, yaw, CORNER_RADIUS),
        strokeRetainedSoftLayer = retainedSoft,
        strokeRetainedStiffLayer = retainedStiff,
        strokeLostPercentWorst = 100.0 * (1.0 - retainedSoft),
        perAnchorLateralForce = perAnchor,
        peakPathForce = peakPathForce(perAnchor, PerPathAllowables.CONCENTRATION_FACTOR_MAX),
        cableEntryPathForce = cableEntryForce,
        bucklingLoad = critical,
        compressionPerAnchor = compressionPerAnchor,
        buckles = buckles,
        verdict = if (binding.startsWith("none")) "PASS" else "FAIL",
        bindingConstraint = binding
    )
}

private fun verticalStrutSchemes(): List<SchemeRecord> = buildList {
    layerPoints.forEach { layer ->
        listOf(
            "CanDo EI = 230" to AnchorMaterials.CANDO_BENDING_RIGIDITY,
            "Mg-measured EI = 165.7" to AnchorMaterials.MAGNESIUM_BENDING_RIGIDITY
        ).forEach { (rigidityName, rigidity) ->
            BeamEndCondition.entries.forEach { end ->
                listOf(1, 4).forEach { count ->
                    add(
                        scheme(
                            id = "S1",
                            name = "$count vertical duplex strut(s), $rigidityName, ${end.name}",
                            topology = "through-layer, rigid rod, theta = 0",
                            layerHeight = layer.height,
                            elementLength = layer.height,
                            count = count,
                            endCondition = end,
                            bendingRigidity = rigidity,
                            axial = rodAxialStiffness(
                                AnchorMaterials.DUPLEX_STRETCH_MODULUS, layer.height
                            ),
                            transverse = beamTransverseStiffness(rigidity, layer.height, end),
                            inPlane = false,
                            compressionPerAnchor = TARGET_FORCE / count,
                            layer = layer
                        )
                    )
                }
            }
        }
    }
}

private fun bundleStrutSchemes(): List<SchemeRecord> = buildList {
    val half = AnchorMaterials.INTERHELICAL_DISTANCE / 2.0
    val bundle = bundleBendingRigidity(
        offsets = listOf(-half, -half, half, half),
        helixBendingRigidity = AnchorMaterials.CANDO_BENDING_RIGIDITY,
        stretchModulus = AnchorMaterials.DUPLEX_STRETCH_MODULUS
    )
    layerPoints.forEach { layer ->
        BeamEndCondition.entries.forEach { end ->
            add(
                scheme(
                    id = "S2",
                    name = "4 four-helix-bundle struts, ${end.name}",
                    topology = "through-layer, rigid bundle, theta = 0",
                    layerHeight = layer.height,
                    elementLength = layer.height,
                    count = 4,
                    endCondition = end,
                    bendingRigidity = bundle,
                    axial = 4.0 * rodAxialStiffness(
                        AnchorMaterials.DUPLEX_STRETCH_MODULUS, layer.height
                    ),
                    transverse = beamTransverseStiffness(bundle, layer.height, end),
                    inPlane = false,
                    compressionPerAnchor = TARGET_FORCE / 4.0,
                    layer = layer
                )
            )
        }
    }
}

private fun inPlaneSchemes(): List<SchemeRecord> = buildList {
    listOf(10.0, 20.0, 40.0).forEach { length ->
        BeamEndCondition.entries.forEach { end ->
            listOf(false, true).forEach { tangential ->
                add(
                    scheme(
                        id = if (tangential) "S4t" else "S4",
                        name = "4 surface-parallel duplex tethers of ${length.roundedForProse()} " +
                                "nm to a coplanar " +
                                "fixed frame, ${if (tangential) "tangential" else "radial"}, ${end.name}",
                        topology = "in-plane, theta = 90 degrees",
                        layerHeight = nominalLayer.height,
                        elementLength = length,
                        count = 4,
                        endCondition = end,
                        bendingRigidity = AnchorMaterials.CANDO_BENDING_RIGIDITY,
                        axial = rodAxialStiffness(
                            AnchorMaterials.DUPLEX_STRETCH_MODULUS, length
                        ),
                        transverse = beamTransverseStiffness(
                            AnchorMaterials.CANDO_BENDING_RIGIDITY, length, end
                        ),
                        inPlane = true,
                        tangential = tangential
                    )
                )
            }
        }
    }
}

private fun postAnchoredScheme(): SchemeRecord {
    // S5: the same in-plane tether, but its far end sits on a single-duplex post standing in
    // the layer. The post's bending is in series with the tether's axial stiffness.
    val tetherLength = 20.0
    val postStiffness = beamTransverseStiffness(
        AnchorMaterials.CANDO_BENDING_RIGIDITY, nominalLayer.height, BeamEndCondition.PINNED_HEAD
    )
    val tetherAxial = rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, tetherLength)
    return scheme(
        id = "S5",
        name = "4 surface-parallel tethers of 20 nm anchored on single-duplex posts",
        topology = "in-plane tether in series with a through-layer post",
        layerHeight = nominalLayer.height,
        elementLength = tetherLength,
        count = 4,
        endCondition = BeamEndCondition.PINNED_HEAD,
        bendingRigidity = AnchorMaterials.CANDO_BENDING_RIGIDITY,
        axial = seriesStiffness(tetherAxial, postStiffness),
        transverse = beamTransverseStiffness(
            AnchorMaterials.CANDO_BENDING_RIGIDITY, tetherLength, BeamEndCondition.PINNED_HEAD
        ),
        inPlane = true
    )
}

// ---------------------------------------------------------------- the study

@Suppress("LongMethod")
fun main() {
    val energy = thermalEnergy()

    val requirement = RequirementRecord(
        positionalBound = LateralRequirement.POSITIONAL_BOUND,
        thermalEnergy = energy,
        perCoordinateStiffness = requiredLateral,
        radialStiffness = LateralRequirement.radialStiffness(),
        worstPointStiffness = LateralRequirement.worstPointTranslationalStiffness(),
        cornerRadius = CORNER_RADIUS,
        footprintRadius = LateralRequirement.footprintRadius(TILE_EDGE, TILE_EDGE),
        yawStiffnessAtCorner = requiredYaw,
        yawStiffnessAtFootprintRadius = LateralRequirement.yawStiffness(
            radius = LateralRequirement.footprintRadius(TILE_EDGE, TILE_EDGE)
        ),
        yawAngleBound = LateralRequirement.POSITIONAL_BOUND / CORNER_RADIUS,
        cornerRmsAtBareBound = LateralRequirement.pointRms(
            requiredLateral, requiredYaw, CORNER_RADIUS
        ),
        declaredAcceptanceQuantity = "per-coordinate: sigma_x <= 3.0 nm, which is leaf A1.1's " +
                "own bound table and the reading C-0010 handed down",
        note = "A design sitting exactly on leaf A1.1's bound in both translations and in yaw " +
                "puts sqrt(3) x 3.0 = 5.196 nm on the tile's CORNER. That is CH-0009's finding " +
                "restated in the plane: a spatially varying fluctuation has no single value."
    )

    val theoremChain = FreelyJointedChain(
        contourLength = 40.0, kuhnLength = SsDnaTether.KUHN_LENGTH_ZERO_FORCE
    )
    val theoremSamples = listOf(1e-4, 0.01, 0.1, 1.0, 5.0, 20.0).map { force ->
        TheoremSample(
            tension = force,
            extension = theoremChain.extension(force),
            transverseStiffness = theoremChain.transverseStiffness(force),
            tangentStiffness = theoremChain.tangentStiffness(force),
            secantOverTangent = secantToTangentRatio(theoremChain, force)
        )
    }
    val theorem = TheoremRecord(
        statement = "For any link crossing the layer with a force-extension law f(x), f(0) = 0, " +
                "convex: the lateral stiffness is the secant f(h)/h and the normal stiffness is " +
                "the tangent f'(h), and convexity gives f(h) = integral of f' <= h f'(h). So " +
                "k_lat/k_norm <= 1, with equality only for a linear spring.",
        samples = theoremSamples,
        worstRatio = theoremSamples.maxOf { it.secantOverTangent },
        holds = theoremSamples.all { it.secantOverTangent <= 1.0 + 1e-12 },
        consequence = "NO through-layer load path can buy lateral stiffness more cheaply than " +
                "one-for-one in normal stiffness. A rigid rod is not covered by the theorem and " +
                "does far WORSE than it: its lateral stiffness is bending, 3EI/L^3, against an " +
                "axial S/L, a ratio of 3EI/(S L^2) = 0.0063 at 10 nm. The escape is topological: " +
                "a load path that lies IN the surface does not have to accommodate the stroke " +
                "axially, and the theorem does not apply to it."
    )

    val schemes = verticalStrutSchemes() + bundleStrutSchemes() + inPlaneSchemes() +
            postAnchoredScheme()

    val tetherDesigns = buildList {
        listOf(
            SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY to
                    "Bosco/Ritort (2014) Table 4, 10 mM MgCl2, fitted over 10-40 pN — the STIFF end",
            SsDnaTether.KUHN_LENGTH_ZERO_FORCE to
                    "Chen et al. (2012) SAXS/smFRET at I = 30 mM, zero force — the APPLICABLE end",
            SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR to
                    "Chen et al. (2012) at I = 6 mM (2 mM MgCl2), zero force — the SOFT end"
        ).forEach { (kuhn, provenance) ->
            listOf(4, 8).forEach { count ->
                layerPoints.forEach { layer ->
                    val admissible = entropicTetherContourLength(
                        count = count,
                        span = layer.height,
                        kuhnLength = kuhn,
                        requiredStiffness = requiredLateral
                    )
                    // the solve returns the LONGEST tether that still meets the bound, which
                    // sits exactly on it; the evaluated design point is half that contour, so
                    // that the reported margins are margins and not a last-bit tie
                    val contour = 0.5 * admissible
                    val chain = FreelyJointedChain(contour, kuhn)
                    val tension = chain.tension(layer.height)
                    val lateral = count * chain.transverseStiffness(tension)
                    val normal = count * chain.tangentStiffness(tension)
                    val links = verticalLinks(
                        chain.tangentStiffness(tension),
                        chain.transverseStiffness(tension),
                        CORNER_RADIUS,
                        count
                    )
                    val yaw = AnchorAssembly(links).yawStiffness
                    val retained = strokeRetainedFraction(normal, layer.secantSoft)
                    val perAnchor = lateral * sqrt(energy / lateral) / count
                    add(
                        TetherDesignRecord(
                            layerHeight = layer.height,
                            anchorCount = count,
                            kuhnLength = kuhn,
                            kuhnProvenance = provenance,
                            maximumAdmissibleContour = admissible,
                            maximumAdmissibleNucleotides =
                                admissible / SsDnaTether.CONTOUR_PER_NUCLEOTIDE,
                            contourLength = contour,
                            contourNucleotides = contour / SsDnaTether.CONTOUR_PER_NUCLEOTIDE,
                            extensionFraction = layer.height / contour,
                            tensionPerTether = tension,
                            totalPreload = count * tension,
                            preloadFractionOfTarget = count * tension / TARGET_FORCE,
                            lateralStiffness = lateral,
                            normalStiffness = normal,
                            anisotropyRatio = anisotropyRatio(lateral, normal),
                            strokeRetainedSoftLayer = retained,
                            strokeLostPercentSoftLayer = 100.0 * (1.0 - retained),
                            yawStiffness = yaw,
                            yawMargin = yaw / requiredYaw,
                            peakPathForce = peakPathForce(
                                tension, PerPathAllowables.CONCENTRATION_FACTOR_MAX
                            ),
                            verdict = when {
                                lateral < requiredLateral -> "FAIL — lateral"
                                yaw < requiredYaw -> "FAIL — yaw"
                                retained < 0.9 -> "FAIL — stroke budget"
                                else -> "PASS"
                            }
                        )
                    )
                }
            }
        }
    }

    val cable = buildList {
        listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).forEach { stroke ->
            listOf(10.0, 20.0, 40.0, 80.0).forEach { length ->
                val tension = cableTension(
                    AnchorMaterials.DUPLEX_STRETCH_MODULUS, length, stroke
                )
                val force = cableNormalForce(
                    AnchorMaterials.DUPLEX_STRETCH_MODULUS, length, stroke
                )
                val secant = cableNormalSecantStiffness(
                    AnchorMaterials.DUPLEX_STRETCH_MODULUS, length, stroke
                )
                val total = 4.0 * secant
                val concentrated = peakPathForce(
                    tension, PerPathAllowables.CONCENTRATION_FACTOR_MAX
                )
                add(
                    CableRecord(
                        stroke = stroke,
                        tetherLength = length,
                        tension = tension,
                        normalForcePerTether = force,
                        normalForceTotal = 4.0 * force,
                        normalSecantStiffness = total,
                        strokeRetainedSoftLayer = strokeRetainedFraction(
                            total, nominalLayer.secantSoft
                        ),
                        tensionOverShearAllowable = tension / PerPathAllowables.SHEAR,
                        tensionOverCeiling = tension / PerPathAllowables.OVERSTRETCHING_CEILING,
                        concentratedPathForce = concentrated,
                        verdict = when {
                            tension > PerPathAllowables.OVERSTRETCHING_CEILING ->
                                "FAIL — the tether itself is past the 65 pN nicked-duplex ceiling"
                            tension > PerPathAllowables.SHEAR ->
                                "FAIL — past the 48 pN quasi-static duplex shear allowable"
                            concentrated > PerPathAllowables.SHEAR ->
                                "MARGINAL — the tether holds, but C-0009's out-of-plane " +
                                        "concentration factor applied to the entry load would not"
                            else -> "PASS on both the tether and the concentrated entry load"
                        }
                    )
                )
            }
        }
    }

    val minimumLengths = buildList {
        listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).forEach { stroke ->
            listOf(
                Triple("duplex shear, direct on the tether", PerPathAllowables.SHEAR, 1.0),
                Triple(
                    "duplex shear, with C-0009's concentration factor",
                    PerPathAllowables.SHEAR,
                    PerPathAllowables.CONCENTRATION_FACTOR_MAX
                ),
                Triple(
                    "unzip geometry, with C-0009's concentration factor",
                    PerPathAllowables.UNZIP,
                    PerPathAllowables.CONCENTRATION_FACTOR_MAX
                )
            ).forEach { (name, allowable, factor) ->
                // T = S delta^2/(2 L^2) <= A/factor gives L >= delta sqrt(S factor/(2A))
                val length = stroke * sqrt(
                    AnchorMaterials.DUPLEX_STRETCH_MODULUS * factor / (2.0 * allowable)
                )
                val lateral = 4.0 * rodAxialStiffness(
                    AnchorMaterials.DUPLEX_STRETCH_MODULUS, length
                )
                add(
                    MinimumLengthRecord(
                        stroke = stroke,
                        allowable = name,
                        allowableForce = allowable,
                        concentrationFactor = factor,
                        minimumTetherLength = length,
                        lateralStiffnessAtThatLength = lateral,
                        lateralMargin = lateral / requiredLateral
                    )
                )
            }
        }
    }

    val tetherAxial = rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, 20.0)
    val stiffPost = beamTransverseStiffness(
        bundleBendingRigidity(
            listOf(
                -AnchorMaterials.INTERHELICAL_DISTANCE / 2.0,
                -AnchorMaterials.INTERHELICAL_DISTANCE / 2.0,
                AnchorMaterials.INTERHELICAL_DISTANCE / 2.0,
                AnchorMaterials.INTERHELICAL_DISTANCE / 2.0
            ),
            AnchorMaterials.CANDO_BENDING_RIGIDITY,
            AnchorMaterials.DUPLEX_STRETCH_MODULUS
        ),
        nominalLayer.height,
        BeamEndCondition.GUIDED_HEAD
    )
    val singlePost = beamTransverseStiffness(
        AnchorMaterials.CANDO_BENDING_RIGIDITY, nominalLayer.height, BeamEndCondition.PINNED_HEAD
    )
    val spacer = FreelyJointedChain(
        contourLength = 10.0 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE,
        kuhnLength = SsDnaTether.KUHN_LENGTH_ZERO_FORCE
    ).gaussianStiffness
    val jointVariants = listOf(
        "as anyone would first draw it: a single-duplex post and an ssDNA spacer" to listOf(
            Triple(
                "in-plane duplex tether, 20 nm, axial (S/L)",
                tetherAxial,
                "DERIVED from S = 1100 pN (Wang et al. 1997)"
            ),
            Triple(
                "anchor post, single duplex, 10 nm, PINNED_HEAD",
                singlePost,
                "DERIVED — the same 0.69 pN/nm C-0010 quoted as its strut bracket"
            ),
            Triple(
                "10-nt ssDNA spacer in the load path, Gaussian 3k_BT/(L_c b)",
                spacer,
                "DERIVED, b from Chen et al. (2012)"
            )
        ),
        ("the build the analysis recommends: a bundle post and NO ssDNA in the load path — the " +
                "hybridised joint at the tile is not listed because it adds no axial compliance, " +
                "the backbone being continuous through a nick; only its rupture force is at " +
                "issue, and that is checked against the shear allowable in the cable table") to
                listOf(
                    Triple(
                        "in-plane duplex tether, 20 nm, axial (S/L)",
                        tetherAxial,
                        "DERIVED from S = 1100 pN"
                    ),
                    Triple(
                        "anchor post, four-helix bundle, 10 nm, GUIDED_HEAD",
                        stiffPost,
                        "DERIVED from the parallel-axis bundle EI = 8877 pN*nm^2"
                    )
                )
    )
    val jointBudget = jointVariants.map { (variant, elements) ->
        val totalCompliance = elements.sumOf { 1.0 / it.second }
        val perAnchor = 1.0 / totalCompliance
        JointBudgetRecord(
            variant = variant,
            elements = elements.map { (name, stiffness, provenance) ->
                SeriesRecord(
                    variant = variant,
                    element = name,
                    stiffness = stiffness,
                    complianceShare = (1.0 / stiffness) / totalCompliance,
                    provenance = provenance
                )
            },
            perAnchorStiffness = perAnchor,
            fourAnchorStiffness = 4.0 * perAnchor,
            margin = 4.0 * perAnchor / requiredLateral,
            dominantComplianceTerm = elements.minByOrNull { it.second }?.first ?: "",
            // the longest ssDNA spacer that still leaves four anchors above the bound, from
            // 3 k_BT/(n l_nt b) >= k_req/4
            maximumSpacerNucleotides = 4.0 * 3.0 * energy /
                    (requiredLateral * SsDnaTether.CONTOUR_PER_NUCLEOTIDE *
                            SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        )
    }

    val placement = listOf(
        Triple("4 anchors at the corners, budget at the corner", CORNER_RADIUS, CORNER_RADIUS),
        Triple("4 anchors at the edge midpoints, budget at the corner", 20.0, CORNER_RADIUS),
        Triple("4 anchors at half radius, budget at the corner", CORNER_RADIUS / 2.0, CORNER_RADIUS),
        Triple("4 anchors outside the footprint at 40 nm, budget at the corner", 40.0, CORNER_RADIUS)
    ).map { (name, anchorRadius, budgetRadius) ->
        val assembly = AnchorAssembly(
            radialInPlaneLinks(1.0, 1.0, anchorRadius)
        )
        val translationMargin = assembly.lateralStiffnessX / requiredLateral
        val yawMargin = assembly.yawStiffness /
                LateralRequirement.yawStiffness(radius = budgetRadius)
        PlacementRecord(
            arrangement = name,
            anchorRadius = anchorRadius,
            budgetRadius = budgetRadius,
            yawOverTranslationMargin = yawMargin / translationMargin,
            note = "exactly (r_anchor/r_budget)^2, independent of the stiffness and of the count"
        )
    }

    val pad = layerPoints.map { layer ->
        val ceilingMin = graftingPadStiffnessCeiling(
            layer.storedEnergyMin, TILE_EDGE, layer.height
        )
        val ceilingMax = graftingPadStiffnessCeiling(
            layer.storedEnergyMax, TILE_EDGE, layer.height
        )
        val threshold = graftingPadEnergyThreshold(TILE_EDGE, layer.height, requiredLateral)
        PadRecord(
            layerHeight = layer.height,
            healingLength = layer.height,
            storedEnergyMin = layer.storedEnergyMin,
            storedEnergyMax = layer.storedEnergyMax,
            stiffnessCeilingMin = ceilingMin,
            stiffnessCeilingMax = ceilingMax,
            marginMin = ceilingMin / requiredLateral,
            marginMax = ceilingMax / requiredLateral,
            energyThreshold = threshold,
            thresholdInThermalUnits = threshold / energy,
            verdict = when {
                ceilingMax < requiredLateral ->
                    "EXCLUDED — even at 100 % of the stored energy the ceiling is below the bound"
                ceilingMin < requiredLateral ->
                    "NOT ESTABLISHED — the ceiling straddles the bound, so the scheme depends " +
                            "entirely on a density contrast this task does not solve"
                else ->
                    "NOT EXCLUDED — the ceiling clears the bound, and the realised value needs a " +
                            "laterally resolved layer free energy that is not computed here"
            }
        )
    }

    // C-0008: the force's own decay length at the working gap, and the gap screening length
    // CLAUDE.md records as counterion-dominated. The longest kappa (shortest screening length)
    // is the least attenuating for a lateral modulation, so it is the ceiling-generous choice.
    val forceDecayLengths = mapOf(5.0 to 1.82, 7.0 to 2.28, 10.0 to 2.83)
    val gapScreening = 0.84
    val electrode = layerPoints.map { layer ->
        val decay = forceDecayLengths.getValue(layer.height)
        val interaction = TARGET_FORCE * decay
        val period = optimalElectrodePeriod(TILE_EDGE, gapScreening, layer.height)
        val full = patternedElectrodeStiffness(
            interaction, period, TILE_EDGE, gapScreening, layer.height
        )
        val threshold = requiredLateral / full
        val transfer = rippleTransfer(4.03, period)
        ElectrodeRecord(
            layerHeight = layer.height,
            forceDecayLength = decay,
            interactionEnergy = interaction,
            gapScreeningLength = gapScreening,
            optimalPeriod = period,
            stiffnessAtFullModulation = full,
            marginAtFullModulation = full / requiredLateral,
            thresholdModulationDepth = threshold,
            rippleTransferAtOptimum = transfer,
            dishingFractionOfStrokeAtThreshold = if (threshold <= 1.0) threshold * transfer
            else Double.NaN,
            verdict = when {
                full < requiredLateral ->
                    "EXCLUDED — even 100 % modulation of the whole interaction energy is below " +
                            "the bound"
                threshold * transfer > 0.19 ->
                    "REACHABLE BUT SELF-DEFEATING — the modulation depth it needs costs " +
                            "${(100.0 * threshold * transfer).roundedForProse()} % of the " +
                            "stroke in dishing, and " +
                            "C-0006 rejects the rigid-plate assumption above ~19 %"
                else -> "NOT EXCLUDED — hand to T-3b, which owns the 2-D solve"
            }
        )
    }

    val best = schemes.filter { it.verdict == "PASS" }.maxByOrNull { it.lateralMargin }
    val result = LateralConfinementResult(
        task = "T-12",
        leaf = "A1.2 (the 3.0 nm bound), with A1.1 as its bound table and A8.2 for the joint budget",
        title = "Lateral confinement of the Gen-1 tile: which anchoring topology can supply " +
                "k_lat >= 0.4602 pN/nm without spending the stroke",
        verificationType = "in-silico (closed-form element mechanics assembled into a 4-DOF " +
                "anchor stiffness) + logical (a convexity theorem that decides the topology " +
                "before any number is computed)",
        acceptance = "An anchoring scheme is identified that delivers k_lat >= 0.4602 pN/nm and " +
                "the corresponding yaw stiffness at 300 K in the §3 geometry, with its " +
                "per-anchor force checked against the per-path disassembly allowables and its " +
                "cost to the normal-direction stroke quantified — or it is demonstrated that no " +
                "scheme compatible with §3 does so, naming the binding constraint.",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "No anchoring scheme in this file has been built, and none is proposed as a " +
                "sequence design.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm (k_BT = 4.142 pN*nm at 300 K)",
            "translational stiffness" to "pN/nm (= mN/m)",
            "rotational stiffness" to "pN*nm/rad",
            "bending rigidity" to "pN*nm^2",
            "stretch modulus" to "pN",
            "angle" to "rad"
        ),
        conventions = mapOf(
            "theta" to "the link's polar angle, measured FROM THE SURFACE NORMAL: 0 is a strut " +
                    "standing under the tile, pi/2 a tether lying in the plane",
            "z" to "upward from the electrode; the plate tasks use w positive downward",
            "origin" to "the centre of the 40 x 40 nm footprint",
            "sign" to "a positive lateral stiffness restores; a positive normal stiffness is a COST",
            "anchor" to "a two-node link with an axial stiffness along its axis and a transverse " +
                    "stiffness across it, contributing k_a n n^T + k_t (I - n n^T)"
        ),
        parameters = mapOf(
            "temperature" to "$ROOM_TEMPERATURE K",
            "medium" to "aqueous buffer, 2/5/10 mM MgCl2 (§3)",
            "tile" to "$TILE_EDGE x $TILE_EDGE nm (§3)",
            "layer heights" to "5 / 7 / 10 nm (§3)",
            "target force" to "$TARGET_FORCE pN (§3)",
            "strokes" to "acceptable $ACCEPTABLE_STROKE nm, desired $DESIRED_STROKE nm (§3)",
            "layer secant stiffness" to "derived as F/stroke from C-0003's stroke bracket: " +
                    "65.4-212.8 pN/nm at 5 nm, 31.2-64.5 at 7 nm, 16.6-26.1 at 10 nm",
            "stroke budget for anchors" to "10 % at the soft end of the C-0003 secant bracket, " +
                    "declared in T-12's Plan before any scheme was evaluated",
            "duplex EI" to "230 pN*nm^2 (CanDo) and 165.68 pN*nm^2 (Mg-measured L_p = 40 nm)",
            "duplex S" to "1100 pN (Wang et al. 1997)",
            "ssDNA Kuhn length" to "1.34-2.84 nm, method-systematic bracket (Bosco/Ritort 2014 " +
                    "force spectroscopy vs Chen et al. 2012 zero-force scattering)"
        ),
        requirement = requirement,
        anisotropyTheorem = theorem,
        noSchemeBaseline = mapOf(
            "layerLateralStiffness" to "EXACTLY ZERO, by symmetry (C-0010)",
            "diffusivity" to "1.969e6 nm^2/s (CITED, C-0010, from a Brinkman shear drag)",
            "excursionInOneKilohertzPeriod" to "62.8 nm — 21x the predicate and 1.6 tile widths",
            "consequence" to "the lateral coordinate is bounded by the anchoring scheme alone, " +
                    "and §3 specifies none"
        ),
        schemes = schemes,
        entropicTetherDesigns = tetherDesigns,
        cableNonlinearity = cable,
        minimumTetherLengths = minimumLengths,
        jointBudget = jointBudget,
        placementRule = placement,
        graftingPad = pad,
        patternedElectrode = electrode,
        validity = listOf(
            "Linear response only. Every stiffness is a tangent at a stated configuration; the " +
                    "cable term is reported as a secant over a stated stroke because it is zero " +
                    "in the linearisation and is the dominant cost outside it.",
            "The layer is NOT modelled as a medium the anchors sit in. A through-layer element " +
                    "displaces polymer and is squeezed by the layer's osmotic pressure; both are " +
                    "omitted, and both would stiffen a chain (excluded volume is screened, not " +
                    "absent) rather than soften it. The direction is stated because it is not " +
                    "computed.",
            "The frame in S4 is assumed rigid. It is stroke-free, so its own attachments may be " +
                    "arbitrarily stiff — which is exactly why the anisotropy theorem does not " +
                    "bind it — but a real frame has finite compliance that adds in series.",
            "The in-plane load path into the tile is NOT solved. C-0009's 2.3-7.6x concentration " +
                    "factor is an OUT-OF-PLANE result and is applied here as a conservative " +
                    "bound; the correct treatment is a shear-lag problem on a membrane-loaded " +
                    "lattice and nobody has done it.",
            "The crossover's axial compliance is a rigid constraint in C-0009 and has nothing " +
                    "cited behind it (T-9 is queued to produce it). Any scheme that loads a " +
                    "crossover axially inherits that gap; the schemes that pass here load the " +
                    "tile in its own plane, which is the direction that gap does not govern.",
            "No electrostatics is solved. The patterned-electrode branch is a ceiling built on " +
                    "C-0008's force and decay length, not a 2-D Poisson-Boltzmann result.",
            "Rupture allowables are quasi-static extrapolations of loading-rate-dependent " +
                    "measurements (C-0006), and a static bias is not a 5.5 pN/s ramp.",
            "Nothing here is measured about this tile or any anchor. PASS means " +
                    "model-consistent and traceable."
        ),
        citedNotDerived = listOf(
            "EI = 230 pN*nm^2, GJ = 460 pN*nm^2 — CITED, CanDo model inputs (Kim et al. 2012)",
            "S = 1100 pN — CITED, MEASURED, Wang et al. (1997)",
            "L_p = 40 nm with Mg2+ — CITED, MEASURED, Wang et al. (1997)",
            "interhelical distance 2.69 nm — CITED, MEASURED (SAXS), Fischer et al. (2016)",
            "ssDNA Kuhn length 1.34-1.41 nm — CITED, MEASURED, Bosco, Camunas-Soler & Ritort, " +
                    "Nucleic Acids Res. 42:2064 (2014) Table 4, optical tweezers in MgCl2, " +
                    "fitted over 10-40 pN",
            "ssDNA persistence length 1.05-1.42 nm at I = 6-30 mM — CITED, MEASURED, Chen et al., " +
                    "PNAS 109:799 (2012), SAXS + smFRET at zero force",
            "ssDNA contour 0.65 nm/nt (inextensible convention) — CITED, MEASURED, Sim et al. " +
                    "(2012) and Bosco et al. (2014)",
            "per-path allowables 48 / 10 / 65 pN — CITED via C-0006's literature trace",
            "the 2.3-7.6x anchor concentration factor and the 64-attachment flatness count — " +
                    "CITED, C-0009",
            "the layer stroke bracket and hence its secant stiffness — CITED, C-0003",
            "the force decay length 1.82 / 2.28 / 2.83 nm — CITED, C-0008",
            "the lateral diffusivity and the 62.8 nm excursion — CITED, C-0010",
            "the dishing-per-modulation-depth coefficient and the ripple transfer function — " +
                    "CITED, C-0006",
            "the 3.0 nm bound, the 100 pN, the 40 x 40 nm footprint, the layer heights — §3, §6"
        ),
        verdict = mapOf(
            "doesASchemeExist" to (best?.let {
                "YES — ${it.name}: k_lat = ${it.lateralStiffness.roundedForProse()} pN/nm " +
                        "(${it.lateralMargin.roundedForProse()}x the bound), k_yaw = ${it.yawStiffness.roundedForProse()} pN*nm/rad " +
                        "(${it.yawMargin.roundedForProse()}x), costing ${it.strokeLostPercentWorst.roundedForProse()} % of the stroke " +
                        "at the soft end of the C-0003 bracket"
            } ?: "NO scheme in the evaluated set passes all three simultaneously"),
            "theBindingConstraint" to "ANISOTROPY, and it is topological rather than material. " +
                    "Every load path that crosses the layer must accommodate the stroke axially, " +
                    "and for a convex force-extension law that costs at least as much normal " +
                    "stiffness as it buys laterally; for a rigid rod it costs 160x more. The " +
                    "same duplex laid IN the surface has the ratio the other way up.",
            "whatItCostsTheActuator" to "The passing schemes add of order 1 pN/nm of normal " +
                    "stiffness against C-0003's 16.6-26.1 pN/nm layer secant, i.e. a few per " +
                    "cent of the stroke at 3 nm. The real cost is the CABLE term: an in-plane " +
                    "tether must stretch by delta^2/2L to let the tile descend, and both the " +
                    "tension and the normal force grow as powers of the stroke, so the §3 " +
                    "DESIRED 10 nm stroke needs a much longer tether than the acceptable 3 nm one.",
            "yawBudget" to "Declared in the same currency as translation: the in-plane " +
                    "displacement of the tile's CORNER, at r = ${CORNER_RADIUS.roundedForProse()} " +
                    "nm, held to 3.0 nm. k_yaw >= ${requiredYaw.roundedForProse()} pN*nm/rad. " +
                    "For anchors AT the corner radius " +
                    "this is EXACTLY the translation requirement, independent of the radius; " +
                    "anchors inside it make yaw the binding condition by (r_budget/r_anchor)^2.",
            "sameAnchorsAsFlatness" to "NO. The lateral scheme's anchors sit on the PERIMETER " +
                    "and load the tile in its own plane; C-0009's 64 attachments are an " +
                    "AREA-DISTRIBUTED normal-direction output coupling. They are additional, " +
                    "and that is a T-2 window constraint in its own right.",
            "maturity" to "TRL 1-3. PASS means model-consistent and traceable, never measured."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-12-lateral-confinement.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")

    println("=".repeat(120))
    println("T-12 — lateral confinement of the Gen-1 tile")
    println("=".repeat(120))
    println()
    println("required k_lat  %10.6f pN/nm   (leaf A1.1, sigma = 3.0 nm)".format(requiredLateral))
    println("required k_yaw  %10.4f pN*nm/rad at the corner, r = %.4f nm".format(
        requiredYaw, CORNER_RADIUS
    ))
    println()
    println("--- the anisotropy theorem ".padEnd(120, '-'))
    theoremSamples.forEach {
        println("f = %8.4f pN   x = %7.3f nm   k_lat = %8.4f   k_norm = %8.4f   ratio = %6.4f".format(
            it.tension, it.extension, it.transverseStiffness, it.tangentStiffness,
            it.secantOverTangent
        ))
    }
    println("holds: ${theorem.holds}")
    println()
    println("--- schemes ".padEnd(120, '-'))
    println("%-62s %9s %10s %9s %9s %8s %s".format(
        "scheme", "k_lat", "k_yaw", "k_norm", "aniso", "stroke%", "verdict"
    ))
    schemes.forEach {
        println("%-62s %9.4f %10.1f %9.3f %9.4f %8.1f %s".format(
            it.name.take(62), it.lateralStiffness, it.yawStiffness, it.normalStiffness,
            it.anisotropyRatio, it.strokeLostPercentWorst, it.verdict
        ))
    }
    println()
    println("--- entropic tether designs (the equality case of the theorem) ".padEnd(120, '-'))
    println("%5s %3s %6s %9s %7s %9s %9s %8s %s".format(
        "h", "N", "b", "L_c", "nt", "tension", "k_norm", "stroke%", "verdict"
    ))
    tetherDesigns.forEach {
        println("%5.1f %3d %6.2f %9.2f %7.0f %9.3f %9.4f %8.1f %s".format(
            it.layerHeight, it.anchorCount, it.kuhnLength, it.contourLength,
            it.contourNucleotides, it.tensionPerTether, it.normalStiffness,
            it.strokeLostPercentSoftLayer, it.verdict
        ))
    }
    println()
    println("--- the cable nonlinearity of an in-plane tether ".padEnd(120, '-'))
    cable.forEach {
        println("stroke %5.1f nm, tether %5.1f nm -> T = %7.2f pN, F_z(4) = %6.2f pN, %s".format(
            it.stroke, it.tetherLength, it.tension, it.normalForceTotal, it.verdict
        ))
    }
    println()
    println("--- minimum in-plane tether length ".padEnd(120, '-'))
    minimumLengths.forEach {
        println("stroke %5.1f nm, %-52s -> L >= %7.2f nm (k_lat = %7.2f, %5.1fx)".format(
            it.stroke, it.allowable, it.minimumTetherLength,
            it.lateralStiffnessAtThatLength, it.lateralMargin
        ))
    }
    println()
    println("--- the joint budget, leaf A8.2 ".padEnd(120, '-'))
    jointBudget.forEach { budget ->
        println(budget.variant)
        budget.elements.forEach {
            println("    %-58s k = %11.4f pN/nm   compliance share %5.1f %%".format(
                it.element, it.stiffness, 100.0 * it.complianceShare
            ))
        }
        println("    -> per anchor %8.4f pN/nm, four anchors %8.4f pN/nm, margin %6.2fx".format(
            budget.perAnchorStiffness, budget.fourAnchorStiffness, budget.margin
        ))
        println("    -> dominant compliance term: ${budget.dominantComplianceTerm}")
        println("    -> longest admissible ssDNA spacer in the load path: %.0f nt".format(
            budget.maximumSpacerNucleotides
        ))
    }
    println()
    println("--- the placement rule ".padEnd(120, '-'))
    placement.forEach {
        println("%-62s yaw/translation margin = %.4f".format(
            it.arrangement, it.yawOverTranslationMargin
        ))
    }
    println()
    println("--- the anchorless branches, as ceilings ".padEnd(120, '-'))
    pad.forEach {
        println("grafting pad, h = %4.1f nm: ceiling %6.4f-%6.4f pN/nm (%.2f-%.2fx) — %s".format(
            it.layerHeight, it.stiffnessCeilingMin, it.stiffnessCeilingMax,
            it.marginMin, it.marginMax, it.verdict
        ))
    }
    electrode.forEach {
        println(
            ("patterned electrode, h = %4.1f nm: optimum period %5.1f nm, %6.4f pN/nm at full " +
                    "depth (%.2fx), needs %.1f %% depth, ripple transfer %.3f, dishing %.1f %% " +
                    "of the stroke").format(
                it.layerHeight, it.optimalPeriod, it.stiffnessAtFullModulation,
                it.marginAtFullModulation, 100.0 * it.thresholdModulationDepth,
                it.rippleTransferAtOptimum, 100.0 * it.dishingFractionOfStrokeAtThreshold
            )
        )
        println("    ${it.verdict}")
    }
    println()
    result.verdict.forEach { (key, value) -> println("$key: $value") }
    println()
    println("written: ${output.path}")
}
