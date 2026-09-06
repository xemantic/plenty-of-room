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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.CholeskyDecomposition
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.gaussLegendreRule
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-17` — the non-uniformity budget of the "one attachment row per duplex" output coupling.
 *
 * ## What this file is for
 *
 * `C-0015` found that an attachment grid with one row per duplex makes the peak per-load-path
 * **crossover** force *exactly* zero under a uniform load, and stated in the same breath that
 * the zero is *"exact only for a uniform load and a uniform foundation … any load
 * non-uniformity, thermal excitation or attachment-stiffness scatter restores a finite
 * crossover force in proportion to the non-uniformity"*. `C-0017` committed the programme's
 * output coupling to that grid and recorded the exposure as its own open question 4.
 *
 * This is the machinery that costs it. It **reads** `structure`'s lattice and `structure`'s
 * plate and edits neither; nothing here is a third lattice.
 *
 * ## The one thing that had to be written rather than reused
 *
 * `structure.edgeTaperedPressure` requires `depth ∈ [0, 1]` — it was written for `C-0006`'s
 * *assumed* taper, which loses load at the rim. `C-0022` solved the field and found the
 * opposite sign: the Gen-1 rim **gains** load, by up to 1.88× about a nanometre inside it. An
 * edge *enhancement* is a negative depth, so [edgeCollarPressure] admits one — and is asserted
 * equal to `edgeTaperedPressure` wherever both are defined.
 */

// ---------------------------------------------------------------------------- the load

/**
 * One raised-cosine collar term of an edge load profile.
 *
 * @param depth the fraction by which the load is reduced *at the rim*, dimensionless.
 *          **Negative for an enhancement**, which is what `C-0022` solved, and **outside
 *          `[0, 1]` in magnitude** for its rim residual term, whose depths run from `−3.52` to
 *          `+1.60`. A magnitude above one means the load *reverses sign* somewhere inside the
 *          collar, which is exactly what `C-0022` reports within about half a nanometre of a
 *          sharp 90° rim; `structure.edgeTaperedPressure` cannot represent it, which is the
 *          second reason this field exists.
 * @param width the width of the collar in nm, over which the profile relaxes to its interior
 *          value.
 */
@Serializable
data class CollarTerm(
    val depth: Double,
    val width: Double
) {

    init {
        require(width > 0.0) { "width must be positive, was: $width" }
        require(depth.isFinite()) { "depth must be finite, was: $depth" }
    }

}

/**
 * The pressure field of an interior value [interiorPressure] modified by a superposition of
 * raised-cosine collars [terms] around the rim of a [lengthX] × [lengthY] footprint.
 *
 * Identical in construction to `structure.edgeTaperedPressure`, extended in exactly two ways:
 * several terms superpose (`C-0022` reduces its solved profile to a smooth term **plus** a rim
 * residual, and applies both), and a term's depth may be negative.
 */
fun edgeCollarPressure(
    interiorPressure: Double,
    lengthX: Double,
    lengthY: Double,
    terms: List<CollarTerm>
): PressureField {
    require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    return PressureField { x, y ->
        val margin = minOf(lengthX / 2.0 - abs(x), lengthY / 2.0 - abs(y))
        var factor = 1.0
        terms.forEach { term ->
            if (margin < term.width) {
                factor -= term.depth * 0.5 * (1.0 + cos(PI * margin / term.width))
            }
        }
        interiorPressure * factor
    }
}

// ---------------------------------------------------------------------------- the cheap bound

/**
 * The downward load in pN each duplex's **tributary strip** carries under [pressure].
 *
 * Integrated over the strip `[y_i − d/2, y_i + d/2] × [−L_x/2, L_x/2]`, never sampled on the
 * duplex axis: `T-15` recorded that sampling on the axis broke its own sum rule by 130 %, and
 * the whole point here is a *difference between strips* of a field that varies fastest exactly
 * at the rim strips.
 */
fun tributaryStripLoads(
    lattice: OrigamiGrillage,
    pressure: PressureField,
    panels: Int = STRIP_PANELS
): List<Double> {
    require(panels >= 1) { "panels must be at least one, was: $panels" }
    val half = lattice.interhelicalDistance / 2.0
    return lattice.beamY.map { axis ->
        rectangleIntegral(
            -lattice.lengthX / 2.0, lattice.lengthX / 2.0, axis - half, axis + half,
            panels, pressure
        )
    }
}

/** The total downward load in pN over the whole footprint under [pressure]. */
fun footprintLoad(
    lattice: OrigamiGrillage,
    pressure: PressureField,
    panels: Int = STRIP_PANELS
): Double = rectangleIntegral(
    -lattice.lengthX / 2.0, lattice.lengthX / 2.0,
    -lattice.lengthY / 2.0, lattice.lengthY / 2.0,
    panels, pressure
)

/**
 * The transverse force in pN crossing each of the `N − 1` helix-helix interfaces of a
 * **rigid** tile carried by a one-attachment-row-per-duplex coupling — the closed form the
 * lattice is graded against, and `T-17`'s cheap bound.
 *
 * ## The derivation, in three lines
 *
 * Cut between duplex `j` and duplex `j + 1`. Everything above the cut carries its own share of
 * the applied load, `Σ_{i>j} Q_i`, and is held by the polymer foundation and by the coupling.
 * If the tile is rigid both reactions are the **same on every duplex** — the foundation because
 * its deflection is uniform, the coupling because one row per duplex puts the same springs at
 * the same stations on every row — so each is `Q̄ = Q_total/N` per duplex. Hence
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`V_j = Σ_{i>j} (Q_i − Q̄)`.
 *
 * Three consequences, each free:
 *
 * - **a load that varies only along the helices restores nothing**, because `Q_i` is then the
 *   same for every duplex and every term of the sum vanishes;
 * - `V_j` is **exactly linear** in a collar depth, because `Q_i` is;
 * - `V_j` vanishes identically for a uniform load, which is `C-0015`'s exact zero recovered
 *   without a matrix.
 *
 * The lattice is needed only for what a rigid tile cannot have: the **concentration** of `V_j`
 * onto the few crossovers of that interface, and the tile's own compliance.
 */
fun rigidTileInterfaceForces(stripLoads: List<Double>): List<Double> {
    require(stripLoads.size >= 2) { "stripLoads must hold at least two strips" }
    val mean = stripLoads.sum() / stripLoads.size
    return (0 until stripLoads.size - 1).map { cut ->
        (cut + 1 until stripLoads.size).sumOf { stripLoads[it] - mean }
    }
}

// ---------------------------------------------------------------------------- the coupling

/**
 * [count] equal springs to ground totalling [totalStiffness] pN/nm, one at each point of
 * [grid], optionally with a multiplicative [scatter] applied path by path.
 *
 * The scatter is a **design tolerance**, not a random variable: a builder controls the ssDNA
 * spacer's contour length in nucleotides and the flexure's span in base pairs, and what it
 * cannot control is how equal two nominally identical paths are. It is applied as a
 * deterministic pattern so that the result file stays reproducible.
 */
fun couplingSupports(
    grid: List<Pair<Double, Double>>,
    totalStiffness: Double,
    scatter: (Int) -> Double = { 1.0 }
): List<PointSupport> {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    val perPath = totalStiffness / grid.size
    return grid.mapIndexed { index, (x, y) ->
        val factor = scatter(index)
        require(factor > 0.0) { "scatter must be positive at $index, was: $factor" }
        PointSupport(x, y, perPath * factor)
    }
}

/** The named deterministic scatter patterns swept, each of relative amplitude `epsilon`. */
enum class ScatterPattern(val label: String) {

    /** Alternating row by row — the pattern that breaks the across-helix symmetry directly. */
    ALTERNATING_ROWS("alternating rows (+/- epsilon duplex by duplex)"),

    /** Alternating column by column — along the helices, which the symmetry does not see. */
    ALTERNATING_COLUMNS("alternating columns (+/- epsilon station by station)"),

    /** One path stiff and the rest nominal — a single mis-assembled attachment. */
    SINGLE_OUTLIER("one attachment off by epsilon, the rest nominal"),

    /** The whole first row off — one duplex's attachments all mis-assembled together. */
    ONE_ROW_OFF("one whole duplex row off by epsilon");

    /** The multiplier of path [index] on a [columns] × [rows] grid at amplitude [epsilon]. */
    fun multiplier(index: Int, columns: Int, epsilon: Double): Double {
        val row = index / columns
        val column = index % columns
        return when (this) {
            ALTERNATING_ROWS -> if (row % 2 == 0) 1.0 + epsilon else 1.0 - epsilon
            ALTERNATING_COLUMNS -> if (column % 2 == 0) 1.0 + epsilon else 1.0 - epsilon
            SINGLE_OUTLIER -> if (index == 0) 1.0 + epsilon else 1.0
            ONE_ROW_OFF -> if (row == 0) 1.0 + epsilon else 1.0
        }
    }

}

/**
 * The same lattice with [supports] assembled into it.
 *
 * `OrigamiGrillage` takes its supports at construction, so a scheme is a new lattice and a new
 * factorisation. That is the honest cost of asking about attachment *stiffness*: a rank-one
 * update covers one anchor, and a 45-path coupling is rank 45.
 */
fun OrigamiGrillage.withSupports(supports: List<PointSupport>): OrigamiGrillage =
    OrigamiGrillage(
        sheet = sheet,
        lengthX = lengthX,
        beamCount = beamCount,
        foundationStiffness = foundationStiffness,
        columns = columns,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        supports = supports
    )

// ---------------------------------------------------------------------------- the crossover force

/**
 * The linear functional `c` for which `c · q` is the transverse force one [crossover] carries
 * in the nodal field `q`.
 *
 * `OrigamiGrillage` computes this force internally as `−k_link · linkExtension`, and the
 * extension is linear in exactly four nodal degrees of freedom. Reconstructing the functional
 * here — from the lattice's own **public** indexing (`nodesPerBeam`, `DOF_PER_NODE`, `W`,
 * `PHI`) — is what makes the *thermal* variance computable at all, because equipartition needs
 * the force as a vector and not as a number.
 *
 * The reconstruction is not asserted, it is **checked**: gate 1 dots it into a solved field and
 * compares against the lattice's own `verticalForce`, at a zero and at a non-zero load case.
 */
fun crossoverForceFunctional(
    lattice: OrigamiGrillage,
    crossover: OrigamiGrillage.Crossover
): org.jetbrains.bio.viktor.F64Array {
    val functional = org.jetbrains.bio.viktor.F64Array(lattice.degreesOfFreedom)
    val half = lattice.interhelicalDistance / 2.0
    fun dof(beam: Int, component: Int): Int =
        (beam * lattice.nodesPerBeam + crossover.node) * OrigamiGrillage.DOF_PER_NODE + component
    functional[dof(crossover.lowerBeam, OrigamiGrillage.W)] = -lattice.linkStiffness
    functional[dof(crossover.lowerBeam, OrigamiGrillage.PHI)] = -lattice.linkStiffness * half
    functional[dof(crossover.lowerBeam + 1, OrigamiGrillage.W)] = lattice.linkStiffness
    functional[dof(crossover.lowerBeam + 1, OrigamiGrillage.PHI)] = -lattice.linkStiffness * half
    return functional
}

/**
 * The root-mean-square transverse force in pN each crossover carries at [temperature], from
 * equipartition on the assembled lattice: `Var(F) = k_BT · cᵀ K⁻¹ c` with `c` the functional of
 * [crossoverForceFunctional].
 *
 * ## This quantity does not converge, and that is the finding
 *
 * `C-0009` chose the crossover's vertical link as a **penalty** whose value *"the answer must
 * not depend on"*, and its gate 4 shows the **static** transmitted force has stopped moving by
 * `10⁴ pN/nm`. The thermal force has not and cannot: a spring of stiffness `k` in thermal
 * equilibrium stores `½k_BT`, so its extension variance is `k_BT/k` and its **force** variance
 * is `k·k_BT` — which grows without bound as the constraint is stiffened. The rigid-constraint
 * limit of a *static* force exists; the rigid-constraint limit of a *fluctuating* one does not.
 *
 * So this is not a load the scheme puts on a joint. It is `√(k_BT k_v)` with `k_v` the
 * crossover's own vertical stiffness — the number `T-9` has not produced — and it is therefore
 * a property of the **joint**, common to every attachment scheme, and not a discriminator
 * between them. Reported as such, with the divergence measured rather than hidden.
 */
fun thermalCrossoverForceRms(
    lattice: OrigamiGrillage,
    temperature: Double = ROOM_TEMPERATURE
): List<Double> {
    val energy = thermalEnergy(temperature)
    val factorisation = CholeskyDecomposition(lattice.stiffness)
    return lattice.crossovers.map { crossover ->
        val forward = factorisation.forwardSolve(crossoverForceFunctional(lattice, crossover))
        sqrt(max(0.0, energy * forward.dot(forward)))
    }
}

// ---------------------------------------------------------------------------- quadrature

private fun rectangleIntegral(
    fromX: Double,
    toX: Double,
    fromY: Double,
    toY: Double,
    panels: Int,
    pressure: PressureField
): Double {
    val rule = gaussLegendreRule(STRIP_QUADRATURE_POINTS)
    val stepX = (toX - fromX) / panels
    val stepY = (toY - fromY) / panels
    var total = 0.0
    for (i in 0 until panels) {
        val x0 = fromX + i * stepX
        for (p in 0 until rule.points) {
            val x = x0 + stepX * (rule.nodes[p] + 1.0) / 2.0
            val weightX = rule.weights[p] * stepX / 2.0
            for (j in 0 until panels) {
                val y0 = fromY + j * stepY
                for (r in 0 until rule.points) {
                    val y = y0 + stepY * (rule.nodes[r] + 1.0) / 2.0
                    total += weightX * rule.weights[r] * stepY / 2.0 * pressure.at(x, y)
                }
            }
        }
    }
    return total
}

/**
 * Panels per direction over a tributary strip.
 *
 * The collar is a raised cosine with a kink where it meets the interior, so the integrand is
 * only `C⁰` and Gauss-Legendre does not integrate it exactly at any order; the panels are what
 * resolves it, and gate 4 refines them.
 */
const val STRIP_PANELS: Int = 24

private const val STRIP_QUADRATURE_POINTS: Int = 6
