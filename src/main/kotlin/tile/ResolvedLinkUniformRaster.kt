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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * `T-315` — route B's **uniform** raster carried at `C-0208`'s **resolved per-bond** link.
 *
 * ## Why this file exists beside `T-307`'s
 *
 * [UniformRasterTethers.lattice] exposes the link as **one scalar**, which is what
 * `HoneycombGrillage` carried until `C-0208`. `C-0207` therefore graded route B's `756` cells at
 * `HoneycombGrillage.RIGID_LINK_STIFFNESS` — a numerical **penalty** `39.2452209×` above every
 * rung a crossover connector can supply, on a lattice whose staple bonds carry it exactly as
 * route A's do (`CH-0265`).
 *
 * `C-0208` resolved a bond's normal link by the bond's own line of centres,
 * `k_radial·unitZ² + k_transverse·unitY²`, and gave `HoneycombGrillage` a `radialLinkStiffness`
 * parameter. [UniformRasterTethers.latticeAtRung] is the one line of plumbing that lets route B's
 * per-turn tethered lattice reach it. It is built the way [UniformRasterTethers.lattice] builds
 * its own — one bare lattice to read `nodesPerBeam` off, then one carrying the elements — so at
 * [ResolvedLinkRung.isSingleScalar] nothing but the link can differ, which is asserted rather
 * than claimed.
 *
 * ## What a rung is, and what it is not
 *
 * A [ResolvedLinkRung] is **two constants**, not one. `HoneycombGrillage`'s `linkStiffness` is
 * read as the **transverse** one, which is what an in-plane bond sees (`unitZ = 0`); the radial
 * one is a resistance to a change of the interhelical **separation** and reaches only the bonds
 * that run through the thickness (`unitZ² = ¾`). A rung with `radialLinkStiffness = null` is the
 * standing single-scalar object **by identity**, because `unitY² + unitZ²` is not exactly one in
 * floating point — `C-0208`'s own reason for branching `linkStiffnessAt`.
 *
 * The resolution reaches **bonds and ties**. It does not reach a **tether**, whose own
 * `HoneycombTetherElement.normalStiffness` already resolves the chain's tangent and secant by the
 * same expression; that is `T-315`'s `F10` and it is a named test.
 *
 * Lengths in nm, stiffnesses in pN/nm; angles nowhere.
 */

/** The unit line of centres of a honeycomb bond that runs **through the thickness**. */
private val THROUGH_THICKNESS_UNIT_Y: Double = 0.5

private val THROUGH_THICKNESS_UNIT_Z: Double = sqrt(3.0) / 2.0

/**
 * One rung of the crossover normal link — a **transverse** constant and an optional **radial**
 * one, in pN/nm.
 *
 * @param name a short label the result file carries.
 * @param ground where the two constants come from, carried so a reading is never quoted without
 *   the state it is read at.
 * @param transverseLinkStiffness what an **in-plane** bond reads, exactly.
 * @param radialLinkStiffness the resistance to a change of separation; `null` is the standing
 *   single-scalar object, and then both readings are [transverseLinkStiffness] **by identity**.
 */
class ResolvedLinkRung(
    val name: String,
    val ground: String,
    val transverseLinkStiffness: Double,
    val radialLinkStiffness: Double?
) {

    init {
        require(transverseLinkStiffness > 0.0 && transverseLinkStiffness.isFinite()) {
            "transverseLinkStiffness must be positive and finite, was: $transverseLinkStiffness"
        }
        if (radialLinkStiffness != null) {
            require(radialLinkStiffness > 0.0 && radialLinkStiffness.isFinite()) {
                "radialLinkStiffness must be positive and finite, was: $radialLinkStiffness"
            }
        }
    }

    /** Whether this rung is the standing one-scalar link, in which case it is bit-identical. */
    val isSingleScalar: Boolean get() = radialLinkStiffness == null

    /** What an in-plane bond's normal link is, in pN/nm. */
    val inPlaneLinkStiffness: Double =
        if (radialLinkStiffness == null) transverseLinkStiffness
        else resolvedLinkStiffness(radialLinkStiffness, transverseLinkStiffness, 1.0, 0.0)

    /** What a through-thickness bond's normal link is, in pN/nm. */
    val throughThicknessLinkStiffness: Double =
        if (radialLinkStiffness == null) transverseLinkStiffness
        else resolvedLinkStiffness(
            radialLinkStiffness,
            transverseLinkStiffness,
            THROUGH_THICKNESS_UNIT_Y,
            THROUGH_THICKNESS_UNIT_Z
        )
}

/**
 * The block's grillage at this raster's own row length, carrying the 59 per-turn tethers, at
 * [rung]'s resolved link.
 *
 * [UniformRasterTethers.lattice] with the link resolved. Every other argument is that function's,
 * with the same defaults and the same two-step construction.
 */
@Suppress("LongParameterList")
fun UniformRasterTethers.latticeAtRung(
    rung: ResolvedLinkRung,
    enhancement: Double,
    withPreload: Boolean = true,
    stiffness: Double? = null,
    subdivisions: Int = 1,
    tethers: (Int) -> List<HoneycombScaffoldTurnTether> = {
        elements(it, withPreload, stiffness)
    }
): HoneycombGrillage {
    fun build(elements: List<HoneycombScaffoldTurnTether>) = HoneycombGrillage(
        block = block,
        rowBasePairs = pairedRowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        linkStiffness = rung.transverseLinkStiffness,
        radialLinkStiffness = rung.radialLinkStiffness,
        scaffoldTurnTethers = elements
    )
    val bare = build(emptyList())
    return build(tethers(bare.nodesPerBeam))
}

/**
 * What a lattice's own bonds read at [rung] — a census rather than an assumption.
 *
 * `C-0208` took this at the `116 bp` block extent and found `135 / 300`. Route B's uniform rows
 * are shorter and the crossover planes are every `7 bp`, so **the count does not transfer** and
 * this class is what says what it is at each of them.
 */
class ResolvedLinkBondCensus(lattice: HoneycombGrillage, rung: ResolvedLinkRung) {

    private val inPlane = lattice.bonds.filter { it.inPlane }

    private val through = lattice.bonds.filter { !it.inPlane }

    /** How many bonds run in the face plane. */
    val inPlaneBonds: Int = inPlane.size

    /** How many run through the thickness. */
    val throughThicknessBonds: Int = through.size

    /** How many bonds the lattice carries. */
    val totalBonds: Int = lattice.bonds.size

    /** `⟨unitZ²⟩` over the in-plane bonds, which must be exactly zero. */
    val meanSquaredUnitZInPlane: Double =
        if (inPlane.isEmpty()) 0.0 else inPlane.sumOf { it.unitZ * it.unitZ } / inPlane.size

    /** `⟨unitZ²⟩` over the through-thickness bonds, which the honeycomb makes `¾`. */
    val meanSquaredUnitZThroughThickness: Double =
        if (through.isEmpty()) 0.0 else through.sumOf { it.unitZ * it.unitZ } / through.size

    /**
     * The worst relative departure of an in-plane link from [rung]'s own reading.
     *
     * A difference of two nearly equal quantities, so it is carried at **two** significant
     * digits — the precision it is determined to (`CLAUDE.md`).
     */
    val worstInPlaneDeparture: Double = (inPlane.maxOfOrNull {
        abs(lattice.linkStiffnessOf(it) - rung.inPlaneLinkStiffness) / rung.inPlaneLinkStiffness
    } ?: 0.0).roundedForProse(2, floor = 0.0)

    /** The worst relative departure of a through-thickness link from [rung]'s own reading. */
    val worstThroughThicknessDeparture: Double = (through.maxOfOrNull {
        abs(lattice.linkStiffnessOf(it) - rung.throughThicknessLinkStiffness) /
                rung.throughThicknessLinkStiffness
    } ?: 0.0).roundedForProse(2, floor = 0.0)

    /**
     * How many distinct link stiffnesses the bonds take, decided coarser than the arithmetic's
     * own noise — one at a single-scalar rung, two at a resolved one.
     */
    val distinctLinkStiffnessCount: Int = lattice.bonds
        .map { Math.round(lattice.linkStiffnessOf(it) / 1e-9) }
        .distinct()
        .size
}
