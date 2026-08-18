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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.CrossoverSite
import com.xemantic.nano.plentyofroom.structure.CrossoverSoftening
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-164`, leaf `A8.2` — how stiff is a ROW-END crossover?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The subject is `CH-0111`'s bracket: `C-0090` gives the 14 row-end crossovers of the buildable
 * 38.08 nm tile an interior crossover's dihedral spring **and** vertical link, and its *refused*
 * reading removes both **and the node**. What is tested here is the machinery that lets the
 * dihedral spring be softened **on its own**, which is the state neither reading covers.
 */
class RowEndCrossoverStiffnessTest {

    private val duplexes = 15

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val sheet =
        origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

    private val edgeX = BUILDABLE_RASTER_WIDTH

    private val phase = 8

    private val arm = quantisedToRise(C0055_ARM_LENGTH)

    private fun layout(admitRowEnd: Boolean) =
        rasterColumnLayout(phase, sheet, edgeX, admitRowEnd, CrossoverLayout.EDGE_MARGIN)

    private fun host(
        admitRowEnd: Boolean,
        softened: Map<CrossoverSite, CrossoverSoftening> = emptyMap(),
        consumed: Set<CrossoverSite> = emptySet()
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = layout(admitRowEnd),
        subdivisions = 2,
        consumedCrossovers = consumed,
        softenedCrossovers = softened
    )

    // ---------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - a softening factor is dimensionless and its two elements are separate`() {
        assert(CrossoverSoftening.FULL.hinge == 1.0)
        assert(CrossoverSoftening.FULL.link == 1.0)
        assert(CrossoverSoftening.ABSENT.hinge == 0.0)
        assert(CrossoverSoftening.ABSENT.link == 0.0)
        // the state neither of C-0090's readings covers: no dihedral spring, the link retained
        assert(CrossoverSoftening.ofHinge(0.0).link == 1.0)
        assert(CrossoverSoftening.ofHinge(0.375).hinge == 0.375)
    }

    @Test
    fun `gate 1 - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { CrossoverSoftening(-1e-12, 1.0) }
        assertFailsWith<IllegalArgumentException> { CrossoverSoftening(1.0, Double.NaN) }
        assertFailsWith<IllegalArgumentException> { hingeStiffnessOfBondCount(-1) }
        assertFailsWith<IllegalArgumentException> { hingeStiffnessOfBondCount(2, alpha = 0.0) }
        assertFailsWith<IllegalArgumentException> { rowEndColumnIndices(layout(true), 0.0) }
        assertFailsWith<IllegalArgumentException> {
            rowEndCrossoverSites(layout(true), edgeX, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            bisectedCrossing(1.0, 0.0, 0.1, 4) { it }
        }
        assertFailsWith<IllegalArgumentException> {
            bisectedCrossing(0.0, 1.0, 0.1, 0) { it }
        }
    }

    @Test
    fun `gate 1 - the bond ladder is Chen et al's own construction and carries pN nm per rad`() {
        // k_theta = 2 alpha B / (100 a): TWO softened phosphate bonds in parallel.
        assert(CROSSOVER_PHOSPHATE_BONDS == 2)
        val interior = hingeStiffnessOfBondCount(CROSSOVER_PHOSPHATE_BONDS)
        assert(abs(interior - Gen1Tile.crossoverHingeStiffness()) < 1e-12)
        assert(abs(hingeStiffnessOfBondCount(1) - interior / 2.0) < 1e-12)
        assert(hingeStiffnessOfBondCount(0) == 0.0)
        assert(softeningOfBondCount(1) == 0.5)
        assert(softeningOfBondCount(2) == 1.0)
    }

    // ---------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - an empty softening map is the unmodified lattice, bit for bit`() {
        val field = uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * duplexes * sheet.interhelicalDistance))
        val plain = host(true).solve(field)
        val mapped = host(true, softened = emptyMap()).solve(field)
        assert(plain.peakDishing() == mapped.peakDishing())
        val full = host(
            true,
            softened = rowEndCrossoverSites(layout(true), edgeX, duplexes)
                .associateWith { CrossoverSoftening.FULL }
        ).solve(field)
        assert(plain.peakDishing() == full.peakDishing())
    }

    @Test
    fun `gate 2 - softening BOTH elements to zero is consuming the crossover, bit for bit`() {
        val sites = rowEndCrossoverSites(layout(true), edgeX, duplexes)
        val field = uniformPressure(0.02)
        val loads = listOf(
            PointLoad(3.4, 2.69, 12.0)
        )
        val softened = host(true, softened = sites.associateWith { CrossoverSoftening.ABSENT })
            .solve(field, loads)
        val consumed = host(true, consumed = sites.toSet()).solve(field, loads)
        // two independent code paths — one deletes the element, the other multiplies it by zero
        assert(softened.peakDishing() == consumed.peakDishing())
        assert(softened.peakDeflection() == consumed.peakDeflection())
    }

    @Test
    fun `gate 2 - the row-end columns are the two end ones, and only when admitted`() {
        val admitted = layout(true)
        val refused = layout(false)
        assert(admitted.size == 8)
        assert(refused.size == 6)
        assert(rowEndColumnIndices(admitted, edgeX) == listOf(0, 7))
        assert(rowEndColumnIndices(refused, edgeX).isEmpty())
    }

    @Test
    fun `gate 2 - the row-end columns carry 14 crossovers, one per interface`() {
        val sites = rowEndCrossoverSites(layout(true), edgeX, duplexes)
        assert(sites.size == duplexes - 1)
        assert(sites.map { it.lowerBeam }.sorted() == (0 until duplexes - 1).toList())
        assert(sites.all { it.column == 0 || it.column == 7 })
        // and the whole lattice carries C-0015's 56 at this phase
        assert(host(true).crossovers.size == 56)
        assert(host(true, consumed = sites.toSet()).crossovers.size == 42)
    }

    // -------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - a uniform load on a uniform foundation dishes exactly zero at every softening`() {
        val pressure = Gen1Tile.TARGET_FORCE / (edgeX * duplexes * sheet.interhelicalDistance)
        val field = uniformPressure(pressure)
        val freeStroke = PlateOnFoundation(
            sheet.plate(edgeX, duplexes * sheet.interhelicalDistance),
            Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
        ).solve(field).meanDeflection
        val sites = rowEndCrossoverSites(layout(true), edgeX, duplexes)
        listOf(0.0, 0.5, 1.0).forEach { scale ->
            val dishing = host(
                true,
                softened = sites.associateWith { CrossoverSoftening.ofHinge(scale) }
            ).solve(field).peakDishing() / freeStroke
            assert(dishing < 1.0e-6)
        }
    }

    @Test
    fun `gate 3 - the row-end crossover set is centro-symmetric on the sheet`() {
        val sites = rowEndCrossoverSites(layout(true), edgeX, duplexes)
        val columns = layout(true)
        val mirrored = sites.map { site ->
            CrossoverSite(duplexes - 2 - site.lowerBeam, columns.size - 1 - site.column)
        }
        assert(mirrored.toSet() == sites.toSet())
    }

    // ---------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - the softened lattice converges under nested subdivision`() {
        val sites = rowEndCrossoverSites(layout(true), edgeX, duplexes)
        val softened = sites.associateWith { CrossoverSoftening.ofHinge(0.0) }
        val field = uniformPressure(0.02)
        val loads = listOf(
            PointLoad(0.0, 0.0, 30.0)
        )
        val values = listOf(1, 2, 4).map { subdivisions ->
            OrigamiGrillage(
                sheet = sheet, lengthX = edgeX, beamCount = duplexes,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = layout(true), subdivisions = subdivisions,
                softenedCrossovers = softened
            ).solve(field, loads).peakDishing()
        }
        val spread = (values.max() - values.min()) / values[1]
        assert(spread < 0.05)
    }

    // ------------------------------------------------- gate 5: literature and upstream

    @Test
    fun `gate 5 - C-0090's own optimum reproduces 0_0621469105 at full row-end stiffness`() {
        val record = t153Record("RECOMMENDED", 8)
        val published = record.first
        val placement = placementFromKey(record.second, phase, arm, edgeX)
        assert(placement.count == C0055_ARM_COUNT)
        val value = dishingOf(placement, softening = null)
        assert(abs(value - published) < 1.0e-9)
        assert(abs(value - 0.0621469105) < 1.0e-9)
    }

    @Test
    fun `gate 5 - the refused reading reproduces 0_168371808 on its own six-column host`() {
        val record = t153Record("BRACKET", 8)
        assert(abs(record.first - 0.168371808) < 1.0e-9)
        val placement = placementFromKey(record.second, phase, arm, edgeX)
        val value = dishingOf(placement, softening = null, admitRowEnd = false)
        assert(abs(value - record.first) < 1.0e-9)
    }

    @Test
    fun `gate 5 - softening the dihedral spring alone does NOT reach the refused reading`() {
        val record = t153Record("RECOMMENDED", 8)
        val placement = placementFromKey(record.second, phase, arm, edgeX)
        val full = dishingOf(placement, softening = null)
        val noHinge = dishingOf(placement, softening = CrossoverSoftening.ofHinge(0.0))
        val absent = dishingOf(placement, softening = CrossoverSoftening.ABSENT)
        // the link is a CONSTRAINT and it survives whatever the strain relief does to k_theta
        assert(noHinge > full)
        assert(noHinge < absent)
    }

    // ---------------------------------------------------------------- helpers

    /** The dishing of one placement on the 38.08 nm host under `C-0022`'s solved collar. */
    private fun dishingOf(
        placement: UpwardArmPlacement,
        softening: CrossoverSoftening?,
        admitRowEnd: Boolean = true
    ): Double {
        val lengthY = duplexes * sheet.interhelicalDistance
        val interior = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)
        val (smooth, rim) = solvedCollar()
        val solved = edgeCollarPressure(interior, edgeX, lengthY, listOf(smooth, rim))
        val freeStroke = PlateOnFoundation(
            sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
        ).solve(uniformPressure(interior)).meanDeflection
        val columns = layout(admitRowEnd)
        val map = if (softening == null) emptyMap()
        else rowEndCrossoverSites(columns, edgeX, duplexes).associateWith { softening }
        val lattice = OrigamiGrillage(
            sheet = sheet, lengthX = edgeX, beamCount = duplexes,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = columns, subdivisions = 2, softenedCrossovers = map
        )
        val stations = rasterUpwardSites(
            phase, edgeX, duplexes, admitRowEnd, rise, CrossoverLayout.EDGE_MARGIN
        ).flatMapIndexed { row, xs ->
            xs.map { it to (row - (duplexes - 1) / 2.0) * sheet.interhelicalDistance }
        }
        val bank = UpwardRootInfluenceBank(lattice, stations, solved)
        val indices = placement.stations(duplexes, sheet.interhelicalDistance).map { (x, y) ->
            val index = bank.indexOf(x, y)
            require(index >= 0) { "($x, $y) is not an upward site of phase $phase" }
            index
        }
        val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
        return bank.surrogateFor(indices)
            .solve(List(C0055_ARM_COUNT) { mandate / C0055_ARM_COUNT })
            .peakDishing / freeStroke
    }

    private fun solvedCollar(): Pair<CollarTerm, CollarTerm> {
        val file = File("gpd/results/T-3b-tile-edge-load-profile.json")
        require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
        val record = Json.parseToJsonElement(file.readText())
            .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
            .first {
                fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
                value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                        value("appliedBias") == 0.192
            }
        fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
        return CollarTerm(value("taperDepth"), value("taperWidth")) to
                CollarTerm(value("rimResidualDepth"), 1.0)
    }

    /** `C-0090`'s published best dishing and placement key, read from its own result file. */
    private fun t153Record(casePrefix: String, phaseBasePairs: Int): Pair<Double, String> {
        val file = File("gpd/results/T-153-buildable-raster-width.json")
        require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
        val record = Json.parseToJsonElement(file.readText())
            .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
            .first {
                it.getValue("case").jsonPrimitive.content.startsWith(casePrefix) &&
                        it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() ==
                        phaseBasePairs
            }
        return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
                record.getValue("bestKey").jsonPrimitive.content
    }

}
