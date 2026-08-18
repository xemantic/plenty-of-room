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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable

/**
 * Task `T-107` — **which stroke is `C-0023`'s 40 pN/nm compliance ceiling owed at?** Leaf `A8.2`.
 *
 * ## The question, and why it is not pedantry
 *
 * `C-0023` filed a design ceiling — *"tangent at that point **≤ 40 pN/nm**"* — and three
 * iterations have consumed it without the qualifier this project demands of every other
 * quantity. `CLAUDE.md` records seven instances of the same discipline (stiffness-with-a-
 * compression, variance-with-a-bandwidth, rupture-force-with-a-loading-rate, `k_es`-with-a-gap,
 * flatness-count-with-a-load-case, ceiling-with-a-load-line, `c`-with-a-stroke); this is the
 * eighth, and it is the first applied to an **acceptance clause** rather than to a model.
 *
 * `C-0039` reads the ceiling at §3's **desired** 10 nm stroke and rejects `E5a16` by 6.6×.
 * `C-0040` reads it at §3's **acceptable** 3 nm stroke and rejects the buildable hinge counts by
 * 1.05–1.35×. Those are two different tests, and nothing had said which is owed.
 *
 * ## What the number actually is
 *
 * 40 = `1.2 × (100 pN / 3 nm)`. It is [DECLARED_CEILING_FACTOR] times
 * [mandatedCouplingStiffness] evaluated at §3's **acceptable** clause, so
 *
 * 1. it is a **construction on the placement mandate**, not an independent physical bound; and
 * 2. it carries the placement stroke inside it, so the *same construction* at §3's **desired**
 *    clause is `1.2 × (100 pN / 10 nm)` = **12 pN/nm**, not 40 — the ceiling **falls** by 10/3
 *    when it is moved to the stroke `C-0039` reads it at.
 *
 * ## What `C-0017` and `C-0018` actually require, which is the acceptance predicate
 *
 * There are three requirements on a coupling law and **none of them is a tangent ceiling**:
 *
 * | | requirement | kind | read at |
 * |---|---|---|---|
 * | 1 | the **secant** at the placement stroke equals the mandate (`C-0017`'s `P1`) | equality | `s*` |
 * | 2 | the **tangent** exceeds `\|k_eff\|` (`C-0017`'s stability floor, `C-0018`'s fold) | floor | `[0, s*]` |
 * | 3 | the **reaction per load path** stays under `C-0006`'s unzip allowable | ceiling | the largest stroke traversed |
 *
 * Requirement 2 runs the *favourable* way in stiffness — `C-0032` measures the strain-stiffening
 * load line raising `C-0018`'s 10 nm / 2 mM bias margin from 1.007–1.032 to 1.020–1.774 — so a
 * stiffer tangent is never penalised by stability. **Requirement 3 is the only genuine ceiling
 * in the stack, it is a bound on a FORCE, and it therefore weakens as `1/s`**: see
 * [perPathSecantCeiling]. At 45 paths it is 150 pN/nm at the acceptable stroke and 45 pN/nm at
 * the desired one; at `C-0041`'s buildable 15 paths it is 50 and **15**.
 *
 * So `C-0023`'s 40 pN/nm is a **declared linearity tolerance on the placement discharge, owed at
 * the placement stroke and nowhere else** — and the derived ceiling that replaces it beyond the
 * working point is tighter, not looser.
 */

// ---------------------------------------------------------------- the declared ceiling

/**
 * The factor `C-0023` declared its compliance ceiling as — 1.2 × the placement mandate.
 *
 * A design tolerance, **not a measurement**: it says how far a coupling's tangent may exceed the
 * secant it was placed on before its law stops being usefully linear at the working point.
 */
const val DECLARED_CEILING_FACTOR: Double = 1.2

/**
 * `C-0023`'s compliance ceiling in `pN/nm`, re-derived rather than quoted:
 * [factor] × [mandatedCouplingStiffness]`(targetForce, targetStroke)`.
 *
 * **The stroke is an argument, because the ceiling has one.** At §3's acceptable clause
 * (100 pN, 3 nm) it returns exactly 40; at §3's desired clause (100 pN, 10 nm) it returns 12.
 */
fun declaredComplianceCeiling(
    targetForce: Double,
    targetStroke: Double,
    factor: Double = DECLARED_CEILING_FACTOR
): Double {
    require(factor > 0.0) { "factor must be positive, was: $factor" }
    return factor * mandatedCouplingStiffness(targetForce, targetStroke)
}

// ---------------------------------------------------------------- the derived ceilings

/** The reaction in pN carried by one of [pathCount] parallel load paths sharing [reaction] pN. */
fun perPathReaction(reaction: Double, pathCount: Int): Double {
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    return reaction / pathCount
}

/**
 * The largest **secant** in `pN/nm` an assembled coupling of [pathCount] paths may present at
 * [stroke] without any one path exceeding [allowable] pN — `n·allowable/s`.
 *
 * This is the ceiling `C-0017`/`C-0006` actually imply, and it is the mirror image of `C-0023`'s
 * own `F_req = k_req·σ` identity: **the per-path force ceiling and the per-path stiffness ceiling
 * are one statement, one power of the STROKE apart.** Unlike the declared ceiling it tightens as
 * the stroke grows, which is the direction a stroke-reserve requirement has to run.
 */
fun perPathSecantCeiling(allowable: Double, pathCount: Int, stroke: Double): Double {
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return pathCount * allowable / stroke
}

/**
 * The stiffness in `pN/nm` at which the per-anchor **thermal** force `√(k_BT k)/n` reaches
 * [allowable] pN over [pathCount] paths — `(n·allowable)²/k_BT`.
 *
 * `C-0014`'s *"over-stiffening an anchor is not free"*, inverted. It is a genuine ceiling and it
 * is nowhere near binding here: 48 890 pN/nm at 45 paths against a 10 pN unzip allowable.
 */
fun thermalForceStiffnessCeiling(
    allowable: Double,
    pathCount: Int,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    val force = pathCount * allowable
    return force * force / thermalEnergy(temperature)
}

/**
 * The stroke interval `[0, s*]` a device placed at [placementStroke] actually traverses, and
 * therefore the interval every requirement on its coupling law is owed over.
 *
 * `CH-0047` is the reason this is a function rather than a convention: at `s = 0` the tile sits
 * at `L₀`, the layer carries nothing, the bias is zero and `k_eff = 0`, so the stability floor is
 * **identically zero** at the lower endpoint. A `min` taken over an interval whose endpoint
 * carries no requirement measures the interval. The same is true of a ceiling read at a stroke
 * the device never reaches.
 */
fun traversedStrokeRange(placementStroke: Double): Pair<Double, Double> {
    require(placementStroke > 0.0) {
        "placementStroke must be positive, was: $placementStroke"
    }
    return 0.0 to placementStroke
}

// ---------------------------------------------------------------- the census

/** Whether a requirement fixes a value, bounds it from below, or bounds it from above. */
enum class RequirementKind { EQUALITY, FLOOR, CEILING }

/**
 * One requirement on a coupling law, **with the stroke it is read at** — which is the whole point
 * of `T-107`.
 *
 * @property quantity the property of the law the requirement is written on: `secant`, `tangent`
 *   or `reaction per path`. `C-0017`'s theorem is that placement is written on the first and
 *   stability on the second, and they are the same number only for an affine line.
 */
@Serializable
data class StrokeBoundRequirement(
    val name: String,
    val kind: RequirementKind,
    val quantity: String,
    val readAtStrokeLow: Double,
    val readAtStrokeHigh: Double,
    val value: Double,
    val owner: String,
    val derivedHere: Boolean,
    val note: String
)

/**
 * The complete census of what a Gen-1 output coupling is required to do, each entry carrying the
 * stroke it is owed at.
 *
 * @param stabilityFloor `|k_eff|` at the held operating point, from `C-0017` — **cited**, because
 *   it costs a Poisson-Boltzmann solve per state and this task does not move it.
 */
fun couplingRequirements(
    targetForce: Double,
    placementStroke: Double,
    pathCount: Int,
    unzipAllowable: Double,
    stabilityFloor: Double,
    largestStrokeTraversed: Double = placementStroke
): List<StrokeBoundRequirement> {
    require(largestStrokeTraversed >= placementStroke) {
        "largestStrokeTraversed must not be below the placement stroke, " +
                "was: $largestStrokeTraversed against $placementStroke"
    }
    val range = traversedStrokeRange(placementStroke)
    return listOf(
        StrokeBoundRequirement(
            name = "placement",
            kind = RequirementKind.EQUALITY,
            quantity = "secant",
            readAtStrokeLow = placementStroke,
            readAtStrokeHigh = placementStroke,
            value = mandatedCouplingStiffness(targetForce, placementStroke),
            owner = "C-0017 P1",
            derivedHere = true,
            note = "F/delta, preload-free and physics-free; an EQUALITY at one stroke, " +
                    "and the only requirement that fixes a value rather than bounding one"
        ),
        StrokeBoundRequirement(
            name = "static stability",
            kind = RequirementKind.FLOOR,
            quantity = "tangent",
            readAtStrokeLow = range.first,
            readAtStrokeHigh = range.second,
            value = stabilityFloor,
            owner = "C-0017, C-0018, C-0032",
            derivedHere = false,
            note = "|k_eff| at the held operating point; identically ZERO at s = 0 (CH-0047), " +
                    "so the requirement is owed over the traversed range and not at a point"
        ),
        StrokeBoundRequirement(
            name = "per-path unzip allowable",
            kind = RequirementKind.CEILING,
            quantity = "secant",
            readAtStrokeLow = largestStrokeTraversed,
            readAtStrokeHigh = largestStrokeTraversed,
            value = perPathSecantCeiling(unzipAllowable, pathCount, largestStrokeTraversed),
            owner = "C-0006, CH-0029",
            derivedHere = true,
            note = "n x allowable / s — the ONLY genuine ceiling in the stack, a bound on a " +
                    "force, and therefore one that TIGHTENS as the stroke grows"
        ),
        StrokeBoundRequirement(
            name = "per-anchor thermal force",
            kind = RequirementKind.CEILING,
            quantity = "tangent",
            readAtStrokeLow = range.first,
            readAtStrokeHigh = range.second,
            value = thermalForceStiffnessCeiling(unzipAllowable, pathCount),
            owner = "C-0014",
            derivedHere = true,
            note = "sqrt(k_BT k)/n against the same allowable; three orders from binding"
        ),
        StrokeBoundRequirement(
            name = "declared compliance ceiling",
            kind = RequirementKind.CEILING,
            quantity = "tangent",
            readAtStrokeLow = placementStroke,
            readAtStrokeHigh = placementStroke,
            value = declaredComplianceCeiling(targetForce, placementStroke),
            owner = "C-0023 (DECLARED, not measured)",
            derivedHere = true,
            note = "1.2 x the placement mandate — a linearity tolerance on the placement " +
                    "discharge, carrying the PLACEMENT stroke inside it"
        )
    )
}

// ------------------------------------------------- T-169: the reading a study's prose may quote

/**
 * The three ceilings a coupling row answers to at one [stroke], **each read at the clause that
 * stroke belongs to** — `C-0049` made a value rather than a sentence.
 *
 * `C-0101` §4 found a study writing *"places, but past the 40 pN/nm ceiling at the desired
 * stroke"* into 26 of its 34 placement rows, and 40 pN/nm at a 10 nm stroke is the wrong clause's
 * number. The three fields here are what a row is entitled to be compared against:
 *
 * @property declaredCeilingAtThisClause [declaredComplianceCeiling] evaluated at [stroke] — 12
 *   pN/nm at §3's desired clause, and the reading a note about that stroke must quote.
 * @property declaredCeilingAtPlacementClause the same construction at the stroke the coupling is
 *   PLACED at — `C-0023`'s 40 pN/nm, owed there and, per `C-0049`, nowhere else. Carried so a
 *   reader can check that the two readings agree on the verdict rather than being asked to trust
 *   that they do.
 * @property perPathSecantCeiling `n·allowable/s`, the only ceiling in `C-0017`'s stack that is not
 *   declared; a bound on the **secant**, not on the tangent.
 */
@Serializable
data class ClauseCeilingReading(
    val stroke: Double,
    val declaredCeilingAtThisClause: Double,
    val declaredCeilingAtPlacementClause: Double,
    val perPathSecantCeiling: Double
)

/** [ClauseCeilingReading] for a coupling of [pathCount] paths placed at [placementStroke]. */
fun clauseCeilingReading(
    targetForce: Double,
    placementStroke: Double,
    stroke: Double,
    pathCount: Int,
    unzipAllowable: Double
): ClauseCeilingReading {
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return ClauseCeilingReading(
        stroke = stroke,
        declaredCeilingAtThisClause = declaredComplianceCeiling(targetForce, stroke),
        declaredCeilingAtPlacementClause =
            declaredComplianceCeiling(targetForce, placementStroke),
        perPathSecantCeiling = perPathSecantCeiling(unzipAllowable, pathCount, stroke)
    )
}

/**
 * How a coupling row that discharges placement and then leaves its compliance reading at a stroke
 * it was **not** placed at should be described — **without** quoting `C-0023`'s 40 pN/nm at that
 * stroke, which `C-0049` withdrew.
 *
 * The note names the tolerance read at the row's own clause and, separately, the per-path secant
 * ceiling, so the two independent reasons are not conflated into one number. Where the row is
 * inside both, it says so; the caller is then free to keep the row.
 */
fun pastClauseCeilingNote(
    tangent: Double,
    secant: Double,
    reading: ClauseCeilingReading
): String {
    val pastDeclared = tangent > reading.declaredCeilingAtThisClause
    val pastPerPath = secant > reading.perPathSecantCeiling
    if (!pastDeclared && !pastPerPath) {
        return (
            "places, and stays inside both readings at the %.1f nm stroke: tangent %.4g " +
                    "pN/nm against C-0023's linearity tolerance read at THAT clause, %.4g " +
                    "pN/nm (C-0049), and secant %.4g against C-0006's per-path ceiling %.4g"
            ).format(
                reading.stroke, tangent, reading.declaredCeilingAtThisClause,
                secant, reading.perPathSecantCeiling
            )
    }
    val declaredClause = if (pastDeclared) {
        (
            "tangent %.4g pN/nm against C-0023's linearity tolerance read at THAT clause, " +
                    "%.4g pN/nm — the 40 pN/nm in circulation is 1.2 x (100 pN / 3 nm) and is " +
                    "owed at the PLACEMENT stroke (C-0049)"
        ).format(tangent, reading.declaredCeilingAtThisClause)
    } else null
    val perPathClause = if (pastPerPath) {
        (
            "secant %.4g pN/nm against C-0006's per-path ceiling n x allowable / s = %.4g pN/nm"
        ).format(secant, reading.perPathSecantCeiling)
    } else null
    val reasons = listOfNotNull(declaredClause, perPathClause).joinToString("; and ")
    return "places, but past its compliance reading at the %.1f nm stroke: %s".format(
        reading.stroke, reasons
    )
}
