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
import com.xemantic.nano.plentyofroom.electrostatics.BluntEndStacking
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * `T-152` — **what clearance should the collinear slot carry?**
 *
 * `C-0053`'s footprint convention charges one duplex girth, **2.69 nm**, between two consecutive
 * collinear elements on the same host row, and `C-0069`'s whole `Q5` verdict lives inside the
 * **0.0256 nm** that convention leaves. [`C-0079`] and [`CH-0093`] show the slot is **coaxial**,
 * that it holds no body at all, and that what it must prevent is a blunt-end **stacking bond**.
 *
 * ## Three things this file adds, and each of them is arithmetic
 *
 * 1. **The slot is an AXIAL gap, so it is quantised at the rise** — and so are the other two terms
 *    of the margin. [collinearMarginBasePairs] is therefore an **integer**, and the published
 *    0.02561 nm is the residue of comparing a *transverse* SAXS lattice constant (7.912 rises) and
 *    an elastica root (24.013 rises) against an integer pitch. `CLAUDE.md` records *"a preload is a
 *    LENGTH and DNA quantises it at 0.34 nm"*; this is the same statement about a clearance, and it
 *    turns a knife edge into a count.
 * 2. **The requirement is a two-state energy balance, not a distance.** Both faces are covalently
 *    tethered to the same sheet at a fixed pitch, so a bond forms only if the material strains to
 *    close the gap. [stackSuppressionGap] balances the elastic work of that closure against
 *    Woo & Rothemund's measured stack, and [stackOccupancyAtGap] is its exact inverse.
 * 3. **The two widths in `C-0053`'s packer are two different quantities** — a *transverse* body
 *    width and an *axial* clearance — and [placeCollinearRootedArray] separates them. Fed one
 *    number for both it reproduces [placeRootedOutputElement] exactly, which is gate 3.
 *
 * ## Conventions, restated rather than inherited
 *
 * - Lengths **nm**, forces **pN**, stiffness **pN/nm**, energies **pN·nm** and `k_BT`;
 *   `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**; `1 kcal/mol = 6.94769 pN·nm`.
 * - `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive
 *   upward**. Origin at the tile centre.
 * - **The rise, 0.34 nm, is the design quantum along `x`**, and every axial length on a duplex is
 *   an integer count of it.
 * - **A stacking free energy is negative** (it binds) and the work done to close a gap is
 *   **positive**; the bond is suppressed when their sum is positive.
 * - A **clearance** is the axial gap between two duplex **end faces**; a **body width** is the
 *   transverse girth of a duplex in plan. They are never the same number and never interchanged.
 */

// ---------------------------------------------------------------- the lattice

/**
 * The oxDNA2 coaxial-stacking radial **minimum**, in nm — **CITED, read directly**, 3.4072 Å
 * (LAMMPS `pair_oxdna2` / Henrich et al., *EPJE* **41**:57), and the separation two stacked
 * terminal base pairs sit at. It is one base-pair rise to 0.2 %, which is not a coincidence: a
 * coaxial stack *is* a continued helix.
 */
const val OXDNA2_STACK_SITE_MINIMUM: Double = 0.34072

/**
 * The smallest whole number of base-pair rises that reaches [length] — `ceil`, with an absolute
 * tolerance so that a length already on the lattice is not rounded up by a last-ulp residue.
 */
fun basePairsForLength(
    length: Double,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    tolerance: Double = 1.0e-9
): Int {
    require(length >= 0.0) { "length must not be negative, was: $length" }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    require(tolerance >= 0.0) { "tolerance must not be negative, was: $tolerance" }
    return ceil(length / rise - tolerance / rise).toInt()
}

/** The largest whole number of base-pair rises that **fits inside** [length] — `floor`. */
fun basePairsWithin(
    length: Double,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    tolerance: Double = 1.0e-9
): Int {
    require(length >= 0.0) { "length must not be negative, was: $length" }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    require(tolerance >= 0.0) { "tolerance must not be negative, was: $tolerance" }
    return floor(length / rise + tolerance / rise).toInt()
}

/**
 * The plan budget a rooted element has, `pitch − clearance` — [rowOfThreeLengthCeiling] with the
 * clearance named for what it is and with **zero** admitted, which is the free limiting case
 * `rowOfThreeLengthCeiling` cannot express because it requires a positive body width.
 */
fun collinearBudget(pitch: Double, clearance: Double): Double {
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    require(clearance >= 0.0) { "clearance must not be negative, was: $clearance" }
    require(clearance < pitch) { "a clearance of $clearance nm does not fit in a $pitch nm pitch" }
    return pitch - clearance
}

/** `M = p − d − L`, the margin `C-0069` and `C-0066` publish as 0.02561 nm. */
fun collinearMargin(pitch: Double, clearance: Double, elementLength: Double): Double {
    require(elementLength > 0.0) { "elementLength must be positive, was: $elementLength" }
    return collinearBudget(pitch, clearance) - elementLength
}

/**
 * **The same margin as an integer count of base-pair rises.**
 *
 * A root pitch is an integer count by construction (32 bp), an element built from base pairs is an
 * integer count, and — the step nobody had taken — a clearance between two **end faces on a common
 * axis** is one too. So the margin is `pitch − clearance − element` in base pairs, and it can be
 * `0`, `1`, `2`… rises and **nothing in between**.
 */
fun collinearMarginBasePairs(
    pitchBasePairs: Int,
    clearanceBasePairs: Int,
    elementBasePairs: Int
): Int {
    require(pitchBasePairs > 0) { "pitchBasePairs must be positive, was: $pitchBasePairs" }
    require(clearanceBasePairs >= 0) {
        "clearanceBasePairs must not be negative, was: $clearanceBasePairs"
    }
    require(elementBasePairs > 0) {
        "elementBasePairs must be positive, was: $elementBasePairs"
    }
    return pitchBasePairs - clearanceBasePairs - elementBasePairs
}

// ---------------------------------------------------------------- the closure energetics

/** Springs in series: `1/k = Σ 1/k_i`. Never stiffer than its softest member. */
fun seriesStiffness(stiffnesses: List<Double>): Double {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(stiffnesses.all { it > 0.0 }) {
        "every stiffness must be positive, were: $stiffnesses"
    }
    return 1.0 / stiffnesses.sumOf { 1.0 / it }
}

/** `S/L` — the axial spring constant of a duplex segment of length [length]. */
fun axialSpringConstant(stretchModulus: Double, length: Double): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(length > 0.0) { "length must be positive, was: $length" }
    return stretchModulus / length
}

/** `√(k_BT L/S)` — the equipartition axial breathing of a duplex segment. `C-0072`'s floor 3. */
fun axialThermalSigma(
    stretchModulus: Double,
    length: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double = sqrt(thermalEnergy(temperature) / axialSpringConstant(stretchModulus, length))

/**
 * **The gap at which a blunt-end stacking bond is suppressed**, in nm.
 *
 * The two faces are covalently rooted to the same sheet, so a bond can only form if the material
 * strains to bring them from their built gap `g` to the stacked separation [contactSeparation].
 * That costs `W(g) = ½ k (g − g₀)²` with `k` = [closureStiffness], and the stacked state's free
 * energy relative to the free one is `ΔG_stack + W`. Requiring the equilibrium ratio
 * `exp(−(ΔG_stack + W)/k_BT)` to be at most [occupancy] gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`g ≥ g₀ + √(2(|ΔG_stack| + k_BT ln(1/occupancy))/k)`.
 *
 * At `occupancy = 1` this is the bare *"the elastic price exceeds the bond"* balance, which is the
 * statement to quote because it contains no chosen tolerance. [stackFreeEnergy] is the **positive
 * magnitude** of the bond.
 */
fun stackSuppressionGap(
    stackFreeEnergy: Double,
    closureStiffness: Double,
    contactSeparation: Double,
    occupancy: Double = 1.0,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(stackFreeEnergy >= 0.0) {
        "stackFreeEnergy is a magnitude and must not be negative, was: $stackFreeEnergy"
    }
    require(closureStiffness > 0.0) {
        "closureStiffness must be positive, was: $closureStiffness"
    }
    require(contactSeparation >= 0.0) {
        "contactSeparation must not be negative, was: $contactSeparation"
    }
    require(occupancy > 0.0) { "occupancy must be positive, was: $occupancy" }
    val work = stackFreeEnergy + thermalEnergy(temperature) * ln(1.0 / occupancy)
    require(work >= 0.0) {
        "an occupancy of $occupancy is already reached at contact for this bond"
    }
    return contactSeparation + sqrt(2.0 * work / closureStiffness)
}

/** The exact inverse of [stackSuppressionGap] — the stacked:free ratio at a stated [gap]. */
fun stackOccupancyAtGap(
    stackFreeEnergy: Double,
    closureStiffness: Double,
    contactSeparation: Double,
    gap: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(stackFreeEnergy >= 0.0) {
        "stackFreeEnergy is a magnitude and must not be negative, was: $stackFreeEnergy"
    }
    require(closureStiffness > 0.0) {
        "closureStiffness must be positive, was: $closureStiffness"
    }
    require(gap >= contactSeparation) {
        "a gap of $gap nm is inside the stacked separation $contactSeparation nm"
    }
    val closing = gap - contactSeparation
    val work = 0.5 * closureStiffness * closing * closing
    return exp((stackFreeEnergy - work) / thermalEnergy(temperature))
}

/** The elastic work in `k_BT` that closing [gap] to [contactSeparation] costs. */
fun closureWorkInThermalUnits(
    closureStiffness: Double,
    contactSeparation: Double,
    gap: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(closureStiffness > 0.0) {
        "closureStiffness must be positive, was: $closureStiffness"
    }
    require(gap >= contactSeparation) {
        "a gap of $gap nm is inside the stacked separation $contactSeparation nm"
    }
    val closing = gap - contactSeparation
    return 0.5 * closureStiffness * closing * closing / thermalEnergy(temperature)
}

// ---------------------------------------------------------------- the closure paths

/** One serial chain of compliances the two end faces can close their gap through. */
data class ClosurePath(val label: String, val members: List<Pair<String, Double>>) {

    init {
        require(members.isNotEmpty()) { "a closure path must carry at least one member" }
    }

    /** The path's own spring constant, `pN/nm`. */
    val stiffness: Double get() = seriesStiffness(members.map { it.second })
}

// ---------------------------------------------------------------- the criteria and the sweep

/** One row of the clearance ladder — a criterion, its gap, and the count that reaches it. */
data class SuppressionCriterion(
    val label: String,
    val requiredGap: Double,
    val requiredBasePairs: Int,
    val closureStiffness: Double,
    val occupancy: Double,
    val adopted: Boolean,
    val flag: String,
    val note: String
)

/** One candidate clearance, and everything the plan model reads at it. */
data class ClearanceCandidate(
    val clearanceBasePairs: Int,
    val clearance: Double,
    val budget: Double,
    val margin: Double,
    val marginBasePairs: Int,
    val endFactorCeiling: Double,
    val endFactorHeadroom: Double,
    val stackOccupancy: Double,
    val closureWork: Double
)

/**
 * The `T-152` reference state: `C-0055`/`C-0063`'s hinge-rooted arm on the upward lattice, with the
 * blunt-end stack `C-0079` measured out of the literature.
 *
 * Every default is a constant a standing claim owns, and none is retyped from a result file.
 */
data class CollinearClearanceState(
    val pitchBasePairs: Int = UPWARD_ROOT_PITCH_BASE_PAIRS,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val armLength: Double = gen1HingeRootedArm,
    val perPath: Double = perPathStiffness(
        Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE, 34
    ),
    val bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    val stretchModulus: Double = Gen1Tile.DUPLEX_STRETCH_MODULUS,
    val crossoverInPlane: Double = Gen1Tile.crossoverInPlaneStiffness(),
    val contactSeparation: Double = OXDNA2_STACK_SITE_MINIMUM,
    val suppressedOccupancy: Double = 0.01,
    val temperature: Double = ROOM_TEMPERATURE,
    val maximumBasePairs: Int = 8
) {

    /** The root pitch in nm — 32 bp = 10.88 nm on the upward lattice. */
    val pitch: Double get() = pitchBasePairs * rise

    /** The arm as a whole number of base pairs, rounded **down** so it is buildable. */
    val armBasePairs: Int get() = basePairsWithin(armLength, rise)

    /** `|ΔG_stack|` in pN·nm — Woo & Rothemund's −2.63 kcal/mol per helix, as a magnitude. */
    val stackFreeEnergy: Double get() = -BluntEndStacking.perStackEnergy

    /** The same, in `k_BT` — the number the requirement is stated against. */
    val stackInThermalUnits: Double get() = stackFreeEnergy / thermalEnergy(temperature)

    /**
     * The **softest** path the two faces can close their gap through: the arm's own axial
     * compliance, the host segment between the two roots, and the root crossover's in-plane
     * spring, all in series. `C-0009`'s `k_s` is *"a construction, not a measurement"*, which is
     * why [stiffClosure] is carried beside it and why the study sweeps it.
     */
    val softClosure: ClosurePath get() = ClosurePath(
        "arm stretch + host stretch + crossover in-plane shear",
        listOf(
            "arm axial" to axialSpringConstant(stretchModulus, armLength),
            "host segment axial" to axialSpringConstant(stretchModulus, pitch),
            "root crossover in-plane" to crossoverInPlane
        )
    )

    /** The same without `C-0009`'s constructed crossover spring — the measured-only reading. */
    val stiffClosure: ClosurePath get() = ClosurePath(
        "arm stretch + host stretch",
        listOf(
            "arm axial" to axialSpringConstant(stretchModulus, armLength),
            "host segment axial" to axialSpringConstant(stretchModulus, pitch)
        )
    )

    /** The ladder of criteria, loosest first. Only the [SuppressionCriterion.adopted] ones decide. */
    fun ladder(): List<SuppressionCriterion> {
        fun energy(
            label: String, path: ClosurePath, occupancy: Double, adopted: Boolean, note: String
        ): SuppressionCriterion {
            val gap = stackSuppressionGap(
                stackFreeEnergy, path.stiffness, contactSeparation, occupancy, temperature
            )
            return SuppressionCriterion(
                label, gap, basePairsForLength(gap, rise), path.stiffness, occupancy, adopted,
                "DERIVED", note
            )
        }
        fun range(label: String, gap: Double, flag: String, note: String) = SuppressionCriterion(
            label, gap, basePairsForLength(gap, rise), 0.0, Double.NaN, false, flag, note
        )
        return listOf(
            range(
                "oxDNA2's coaxial-stacking hard cutoff", BluntEndStacking.OXDNA2_CUTOFF,
                "CITED, READ DIRECTLY (via C-0079)",
                "a nominal-position range criterion: beyond it the simulation potential is zero"
            ),
            range(
                "the stacked separation plus one axial thermal sigma",
                contactSeparation + sqrt(
                    axialThermalSigma(stretchModulus, armLength, temperature).let { it * it } +
                            axialThermalSigma(stretchModulus, pitch, temperature).let { it * it }
                ),
                "DERIVED",
                "C-0072's floor 3 read as a closure amplitude rather than as a tolerance"
            ),
            range(
                "the all-atom PMF's repulsive onset", BluntEndStacking.ALL_ATOM_REPULSIVE_ONSET,
                "CITED, READ DIRECTLY (via C-0079)",
                "the generous end of the measured range; still a nominal-position criterion"
            ),
            energy(
                "the elastic price of closing exceeds the bond, stiff closure",
                stiffClosure, 1.0, true,
                "the balance with C-0009's constructed crossover spring left out"
            ),
            energy(
                "the elastic price of closing exceeds the bond, soft closure",
                softClosure, 1.0, true,
                "the same balance on the softest path the design offers"
            ),
            energy(
                "the stacked state held below one per cent, stiff closure",
                stiffClosure, suppressedOccupancy, true, "a suppression rather than a parity"
            ),
            energy(
                "the stacked state held below one per cent, soft closure",
                softClosure, suppressedOccupancy, true,
                "THE ADOPTED CRITERION — the largest of the physically calibrated set"
            )
        )
    }

    /** The clearance the ladder demands, in base pairs — the max over the adopted criteria. */
    fun requiredBasePairs(): Int = ladder().filter { it.adopted }.maxOf { it.requiredBasePairs }

    /** Every integer clearance from one rise to [maximumBasePairs], with the plan model at each. */
    fun integerSweep(): List<ClearanceCandidate> =
        (1..maximumBasePairs).map { candidate(it) }

    /** The candidate at [clearanceBasePairs] rises. */
    fun candidate(clearanceBasePairs: Int): ClearanceCandidate {
        val clearance = clearanceBasePairs * rise
        val budget = collinearBudget(pitch, clearance)
        val factor = bendingFactorForLength(budget, bendingRigidity, perPath)
        val standing = bendingFactorForLength(
            collinearBudget(pitch, OrigamiDuplex.INTERHELICAL), bendingRigidity, perPath
        )
        return ClearanceCandidate(
            clearanceBasePairs = clearanceBasePairs,
            clearance = clearance,
            budget = budget,
            margin = budget - armLength,
            marginBasePairs = collinearMarginBasePairs(
                pitchBasePairs, clearanceBasePairs, armBasePairs
            ),
            endFactorCeiling = factor,
            endFactorHeadroom = factor / standing,
            // a clearance inside the stacked separation IS the stacked state: no work is done
            stackOccupancy = stackOccupancyAtGap(
                stackFreeEnergy, softClosure.stiffness, contactSeparation,
                maxOf(clearance, contactSeparation), temperature
            ),
            closureWork = closureWorkInThermalUnits(
                softClosure.stiffness, contactSeparation,
                maxOf(clearance, contactSeparation), temperature
            )
        )
    }

    /** The recommended clearance — [candidate] at [requiredBasePairs]. */
    fun recommendation(): ClearanceCandidate = candidate(requiredBasePairs())
}

/**
 * `C-0055`/`C-0063`'s hinge-rooted arm, **re-solved** from `C-0039`'s exact elastica rather than
 * transcribed: one crossover root, `C-0034`'s `A2` tip, 34 paths, `C-0017`'s mandate at §3's
 * acceptable stroke. 8.16439083 nm.
 */
val gen1HingeRootedArm: Double by lazy {
    elasticaArmForStiffness(
        hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
        hingeCount = 1,
        farStiffness = ArmAnchorage.twoTerminus().rotationalStiffness,
        bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
        count = 34,
        targetStiffness = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
        workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
    )
}

// ---------------------------------------------------------------- the placement, with two widths

/** What placing a rooted element at every station returns when the two widths are separated. */
data class CollinearArrayOutcome(
    val label: String,
    val length: Double,
    val clearance: Double,
    val bodyWidth: Double,
    val demanded: Int,
    val placed: Int,
    val directionsFound: Boolean,
    val overlappingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val planAreaFraction: Double,
    val verdict: String
) {

    /** Whether the whole demanded array places, in one level. */
    val placesInFull: Boolean get() = placed == demanded && singleLevel && directionsFound
}

/**
 * [placeRootedOutputElement] with `C-0053`'s single `width` split into the two quantities it was
 * always doing the work of: an **axial** [clearance] between consecutive collinear elements, and a
 * **transverse** [bodyWidth] for the overlap and level tests.
 *
 * Fed one number for both it reproduces [placeRootedOutputElement] exactly — asserted as a gate-3
 * test, because a widening that came from a changed packer rather than from a changed convention
 * would be worthless.
 */
fun placeCollinearRootedArray(
    label: String,
    rows: List<StationRow>,
    length: Double,
    clearance: Double,
    bodyWidth: Double,
    edgeX: Double,
    lengthY: Double
): CollinearArrayOutcome {
    require(rows.isNotEmpty()) { "rows must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(clearance > 0.0) { "clearance must be positive, was: $clearance" }
    require(bodyWidth > 0.0) { "bodyWidth must be positive, was: $bodyWidth" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    val demanded = rows.sumOf { it.count }
    val assignment = rows.map { armDirections(it.roots, length, edgeX, clearance) }
    val directionsFound = assignment.all { it != null }
    val elements = ArrayList<PlanElement>()
    rows.forEachIndexed { rowIndex, row ->
        val directions = assignment[rowIndex]
        row.roots.forEachIndexed { index, root ->
            val toward = directions?.get(index)
                ?: (root + length <= 0.5 * edgeX + PLAN_TANGENCY_TOLERANCE ||
                        root - length < -0.5 * edgeX - PLAN_TANGENCY_TOLERANCE)
            elements += PlanElement(
                id = "row ${row.row} station $index",
                anchor = Vector2(root, row.y),
                angle = if (toward) 0.0 else PI,
                length = length,
                width = bodyWidth,
                anchorFraction = 0.0
            )
        }
    }
    // rows may be summed independently only while a body of this width cannot reach the next row
    val independent = rows.size < 2 || rows.zipWithNext().all { (below, above) ->
        abs(above.y - below.y) >= bodyWidth - PLAN_TANGENCY_TOLERANCE
    }
    val placed = if (independent) {
        rows.sumOf { maximumRootedElementsInRow(it.roots, length, edgeX, clearance) }
    } else {
        greedyConflictFreeElements(elements)
    }
    val verdict = elementPackingVerdict(elements)
    return CollinearArrayOutcome(
        label = label,
        length = length,
        clearance = clearance,
        bodyWidth = bodyWidth,
        demanded = demanded,
        placed = placed,
        directionsFound = directionsFound && independent,
        overlappingPairs = verdict.overlappingPairs,
        mutuallyBlockingPairs = verdict.mutuallyBlockingPairs,
        memberClashPairs = verdict.memberClashPairs,
        levelsRequired = verdict.levelsRequired,
        singleLevel = verdict.singleLevel,
        planAreaFraction = demanded * length * bodyWidth / (edgeX * lengthY),
        verdict = if (placed == demanded && directionsFound && independent && verdict.singleLevel) {
            "PLACES — all $demanded instances, one level"
        } else {
            "DOES NOT PLACE — $placed of $demanded"
        }
    )
}
