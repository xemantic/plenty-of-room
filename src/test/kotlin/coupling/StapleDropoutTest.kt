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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-148` — the position-dependent staple dropout, gate by gate.
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-148`
 * declares are asserted rather than argued:
 *
 * - **`F2`** — the zero-dropout limit must reproduce the standing pipeline exactly;
 * - **`F3`** — the uncoupled tile under a **uniform** load must dish exactly zero, which is
 *   `CLAUDE.md`'s own falsifier for a plate-on-foundation solve and is the one a dropout
 *   realisation cannot change, because a realisation does not touch the load case.
 *
 * The disciplines from `CLAUDE.md` that govern this file: a random stream that a result file
 * depends on must be **bit-reproducible from its seed**; a percentile is an **order statistic**
 * and must not depend on the order the sample arrived in; and two quantities that are both meant
 * to be zero are compared **absolutely**.
 */
class StapleDropoutTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s design point, transcribed as `NonUniformCouplingTest` does. */
    private val solvedField = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun lattice(subdivisions: Int = 2) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        supports = emptyList()
    )

    private val grid = attachmentGrid(3, duplexes, edgeX, edgeY)

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - a Bernoulli relative scatter is dimensionless and reproduces CH-0084's readings`() {
        assert(bernoulliRelativeScatter(1.0 - 0.84).isCloseTo(0.43644, 1e-4))
        assert(bernoulliRelativeScatter(1.0 - 0.48).isCloseTo(1.04083, 1e-4))
        assert(bernoulliRelativeScatter(1.0 - 0.95).isCloseTo(0.22942, 1e-4))
        assert(bernoulliRelativeScatter(0.0) == 0.0)
    }

    @Test
    fun `gate 1 - the detection-to-incorporation offset is additive in percentage points`() {
        val offset = StapleDropoutLiterature.DETECTION_TO_INCORPORATION_OFFSET
        assert(
            (StapleDropoutLiterature.DETECTION_MINIMUM + offset)
                .isCloseTo(StapleDropoutLiterature.INCORPORATION_EDGE, 1e-12)
        )
        assert(
            (StapleDropoutLiterature.DETECTION_MAXIMUM + offset)
                .isCloseTo(StapleDropoutLiterature.INCORPORATION_CENTRE, 1e-12)
        )
        assert(
            (StapleDropoutLiterature.DETECTION_MEAN + offset)
                .isCloseTo(StapleDropoutLiterature.INCORPORATION_MEAN, 1e-12)
        )
    }

    @Test
    fun `gate 1 - every field returns a probability inside the unit interval`() {
        val fields = listOf(
            uniformIncorporation(0.84),
            flatBandIncorporation(edgeX, edgeY, 4.93),
            exponentialIncorporation(edgeX, edgeY, 3.0),
            latticeRingIncorporation(edgeX, edgeY, 2.69, 16 * 0.34)
        )
        fields.forEach { field ->
            for (i in 0..20) for (j in 0..20) {
                val x = -edgeX / 2.0 + edgeX * i / 20.0
                val y = -edgeY / 2.0 + edgeY * j / 20.0
                val p = field.at(x, y)
                assert(p >= 0.0 && p <= 1.0)
            }
        }
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { uniformIncorporation(-0.1) }
        assertFailsWith<IllegalArgumentException> { uniformIncorporation(1.4) }
        assertFailsWith<IllegalArgumentException> { flatBandIncorporation(0.0, edgeY, 1.0) }
        assertFailsWith<IllegalArgumentException> { flatBandIncorporation(edgeX, edgeY, -1.0) }
        assertFailsWith<IllegalArgumentException> { exponentialIncorporation(edgeX, edgeY, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            latticeRingIncorporation(edgeX, edgeY, 0.0, 1.0)
        }
        assertFailsWith<IllegalArgumentException> { bernoulliRelativeScatter(-0.1) }
        assertFailsWith<IllegalArgumentException> { bernoulliRelativeScatter(1.0) }
        assertFailsWith<IllegalArgumentException> {
            expectedTotalStiffness(emptyList(), emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            expectedTotalStiffness(listOf(1.0), listOf(0.5, 0.5))
        }
        assertFailsWith<IllegalArgumentException> { binomialStandardError(0.5, 0) }
        assertFailsWith<IllegalArgumentException> { orderStatistic(doubleArrayOf(), 0.5) }
        assertFailsWith<IllegalArgumentException> { orderStatistic(doubleArrayOf(1.0), 1.5) }
        assertFailsWith<IllegalArgumentException> { compensatedStiffnesses(listOf(1.0), listOf(0.0)) }
        assertFailsWith<IllegalArgumentException> {
            renormalisedSurvivors(listOf(1.0, 2.0), listOf(true, true), 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            renormalisedSurvivors(listOf(1.0, 2.0), listOf(false, false), 1.0)
        }
    }

    @Test
    fun `gate 1 - the fitted band and decay length are lengths in nm and physically bounded`() {
        val band = bandWidthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95)
        assert(band > 0.0 && band < 64.56 / 2.0)
        val decay = decayLengthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95)
        assert(decay > 0.0 && decay.isFinite())
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - a zero band and a vanishing decay give the constant centre field`() {
        assert(flatBandIncorporation(edgeX, edgeY, 0.0).at(0.0, 0.0).isCloseTo(0.95, 1e-12))
        assert(flatBandIncorporation(edgeX, edgeY, 0.0).at(19.999, 0.0).isCloseTo(0.95, 1e-12))
        assert(exponentialIncorporation(edgeX, edgeY, 1e-6).at(0.0, 0.0).isCloseTo(0.95, 1e-9))
    }

    @Test
    fun `gate 2 - a band wider than the half tile depresses every station`() {
        assert(flatBandIncorporation(edgeX, edgeY, 100.0).at(0.0, 0.0).isCloseTo(0.48, 1e-12))
    }

    @Test
    fun `gate 2 - both fitted fields take the measured edge value exactly at the rim`() {
        assert(
            exponentialIncorporation(edgeX, edgeY, 3.0).at(edgeX / 2.0, 0.0).isCloseTo(0.48, 1e-12)
        )
        assert(
            flatBandIncorporation(edgeX, edgeY, 4.0).at(edgeX / 2.0, 0.0).isCloseTo(0.48, 1e-12)
        )
    }

    @Test
    fun `gate 2 - the lattice ring grades by the number of missing neighbour directions`() {
        val field = latticeRingIncorporation(edgeX, edgeY, 2.69, 16 * 0.34)
        assert(field.at(0.0, 0.0).isCloseTo(0.95, 1e-12))
        assert(field.at(0.0, edgeY / 2.0 - 1.0).isCloseTo(0.715, 1e-12))
        assert(field.at(edgeX / 2.0 - 1.0, 0.0).isCloseTo(0.715, 1e-12))
        assert(field.at(edgeX / 2.0 - 1.0, edgeY / 2.0 - 1.0).isCloseTo(0.48, 1e-12))
    }

    @Test
    fun `gate 2 - presence at probability one keeps every path and at zero keeps none`() {
        val random = DropoutRandom(1L)
        assert(bernoulliPresence(List(45) { 1.0 }, random).all { it })
        assert(bernoulliPresence(List(45) { 0.0 }, random).none { it })
    }

    @Test
    fun `gate 2 - the area mean of a constant field is the constant`() {
        assert(exponentialAreaMean(64.56, 102.68, 1.0, 0.5, 0.5).isCloseTo(0.5, 1e-12))
        assert(flatBandAreaMean(64.56, 102.68, 5.0, 0.5, 0.5).isCloseTo(0.5, 1e-12))
        assert(flatBandAreaMean(60.0, 100.0, 0.0, 0.48, 0.95).isCloseTo(0.95, 1e-12))
        assert(flatBandAreaMean(60.0, 100.0, 30.0, 0.48, 0.95).isCloseTo(0.48, 1e-12))
    }

    @Test
    fun `gate 2 - F2 - a realisation that keeps every path reproduces the full-coupling solve`() {
        val surrogate = latticeInfluenceSurrogate(lattice(), grid, solvedField, 81)
        val stiffnesses = List(grid.size) { mandate / grid.size }
        val full = surrogate.solve(stiffnesses).peakDishing
        val kept = surrogate.solveWithDropout(stiffnesses, List(grid.size) { true }).peakDishing
        // `solve` IS this call at full presence, so the two run identical arithmetic — and they
        // still land one unit in the last place apart, because the JIT recompiles the dishing
        // reduction between the two invocations and changes its summation order. `CLAUDE.md`
        // records exactly that, and it is why a result file is rounded at the serialisation
        // boundary rather than asserted bit-for-bit.
        assert(kept.isCloseTo(full, 1e-14))
        assert(abs(kept - full) < 1e-12)
    }

    @Test
    fun `gate 2 - a realisation that keeps no path returns the free tile exactly`() {
        val surrogate = latticeInfluenceSurrogate(lattice(), grid, solvedField, 81)
        val free = lattice().solve(solvedField).peakDishing(81)
        val none = surrogate
            .solveWithDropout(List(grid.size) { mandate / grid.size }, List(grid.size) { false })
        assert(none.peakDishing.isCloseTo(free, 1e-12))
        assert(none.supportForces.all { it == 0.0 })
    }

    @Test
    fun `gate 2 - dropping a path equals solving over the surviving stations alone`() {
        val surrogate = latticeInfluenceSurrogate(lattice(), grid, solvedField, 81)
        val present = List(grid.size) { it % 4 != 0 }
        val stiffnesses = List(grid.size) { mandate / grid.size }
        val dropped = surrogate.solveWithDropout(stiffnesses, present).peakDishing
        val survivors = grid.indices.filter { present[it] }
        val reduced = latticeInfluenceSurrogate(
            lattice(), survivors.map { grid[it] }, solvedField, 81
        ).solve(survivors.map { stiffnesses[it] }).peakDishing
        assert(dropped.isCloseTo(reduced, 1e-9))
    }

    // ------------------------------------------------------------ gate 3: symmetry, conservation

    @Test
    fun `gate 3 - F3 - the uncoupled tile under a uniform load dishes exactly zero`() {
        val surrogate = latticeInfluenceSurrogate(
            lattice(), grid, uniformPressure(interiorPressure), 81
        )
        val none = surrogate
            .solveWithDropout(List(grid.size) { mandate / grid.size }, List(grid.size) { false })
        assert(abs(none.peakDishing) < 1e-9)
    }

    @Test
    fun `gate 3 - the flat band area fraction is the exact rectangle complement`() {
        val w = 60.0
        val l = 100.0
        val band = 7.0
        val expected = 1.0 - (w - 2 * band) * (l - 2 * band) / (w * l)
        assert(flatBandAreaFraction(w, l, band).isCloseTo(expected, 1e-12))
        assert(
            flatBandAreaMean(w, l, band, 0.48, 0.95)
                .isCloseTo(0.95 - (0.95 - 0.48) * expected, 1e-12)
        )
    }

    @Test
    fun `gate 3 - the exponential area mean matches an independent midpoint quadrature`() {
        val w = 60.0
        val l = 100.0
        val lambda = 4.0
        val n = 800
        var total = 0.0
        for (i in 0 until n) for (j in 0 until n) {
            val x = -w / 2.0 + w * (i + 0.5) / n
            val y = -l / 2.0 + l * (j + 0.5) / n
            val d = minOf(w / 2.0 - abs(x), l / 2.0 - abs(y))
            total += 0.95 - (0.95 - 0.48) * exp(-d / lambda)
        }
        assert(abs(exponentialAreaMean(w, l, lambda, 0.48, 0.95) - total / (n * n)) < 5e-4)
    }

    @Test
    fun `gate 3 - the fitted band and decay reproduce the mean they were fitted to`() {
        val band = bandWidthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95)
        assert(flatBandAreaMean(64.56, 102.68, band, 0.48, 0.95).isCloseTo(0.84, 1e-9))
        val decay = decayLengthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95)
        assert(exponentialAreaMean(64.56, 102.68, decay, 0.48, 0.95).isCloseTo(0.84, 1e-9))
    }

    @Test
    fun `gate 3 - the expected total and its deviation are the closed forms`() {
        val k = listOf(0.9, 0.2, 0.5, 0.1)
        val p = listOf(0.5, 0.9, 0.84, 0.48)
        assert(
            expectedTotalStiffness(k, p).isCloseTo(k.indices.sumOf { k[it] * p[it] }, 1e-12)
        )
        assert(
            totalStiffnessDeviation(k, p).isCloseTo(
                sqrt(k.indices.sumOf { k[it] * k[it] * p[it] * (1.0 - p[it]) }), 1e-12
            )
        )
    }

    @Test
    fun `gate 3 - a Bernoulli sample reproduces its own expected total`() {
        val k = List(45) { mandate / 45.0 }
        val p = List(45) { 0.84 }
        val random = DropoutRandom(20260817L)
        val samples = 40000
        var total = 0.0
        repeat(samples) {
            val present = bernoulliPresence(p, random)
            total += k.indices.sumOf { if (present[it]) k[it] else 0.0 }
        }
        val expected = expectedTotalStiffness(k, p)
        val error = totalStiffnessDeviation(k, p) / sqrt(samples.toDouble())
        assert(abs(total / samples - expected) < 4.0 * error)
    }

    @Test
    fun `gate 3 - the random stream is bit-reproducible from its seed`() {
        val first = List(64) { DropoutRandom(7L).nextDouble() }
        val again = DropoutRandom(7L).let { r -> List(64) { r.nextDouble() } }
        val other = DropoutRandom(8L).let { r -> List(64) { r.nextDouble() } }
        val stream = DropoutRandom(7L).let { r -> List(64) { r.nextDouble() } }
        assert(stream == again)
        assert(stream != other)
        assert(first.all { it == first[0] })
        assert(stream.all { it >= 0.0 && it < 1.0 })
    }

    @Test
    fun `gate 3 - the incorporation field is symmetric under a point reflection`() {
        listOf(
            flatBandIncorporation(edgeX, edgeY, 4.93),
            exponentialIncorporation(edgeX, edgeY, 3.1),
            latticeRingIncorporation(edgeX, edgeY, 2.69, 5.44)
        ).forEach { field ->
            for (i in 1..9) for (j in 1..9) {
                val x = -edgeX / 2.0 + edgeX * i / 10.0
                val y = -edgeY / 2.0 + edgeY * j / 10.0
                assert(field.at(x, y).isCloseTo(field.at(-x, -y), 1e-12))
            }
        }
    }

    @Test
    fun `gate 3 - compensation makes the expected total exactly the nominal total`() {
        val k = listOf(0.9, 0.2, 0.5, 0.1)
        val p = listOf(0.5, 0.9, 0.84, 0.48)
        assert(expectedTotalStiffness(compensatedStiffnesses(k, p), p).isCloseTo(k.sum(), 1e-12))
    }

    @Test
    fun `gate 3 - renormalising the survivors restores the mandate exactly`() {
        val k = listOf(0.9, 0.2, 0.5, 0.1)
        val present = listOf(true, false, true, true)
        val renormalised = renormalisedSurvivors(k, present, mandate)
        assert(
            renormalised.indices.filter { present[it] }.sumOf { renormalised[it] }
                .isCloseTo(mandate, 1e-12)
        )
        assert(renormalised.indices.filter { !present[it] }.all { renormalised[it] == 0.0 })
    }

    // ------------------------------------------------------------ gate 4: convergence, sampling

    @Test
    fun `gate 4 - an order statistic is the nearest rank and does not mutate its sample`() {
        val sample = doubleArrayOf(5.0, 1.0, 4.0, 2.0, 3.0)
        assert(orderStatistic(sample, 0.0) == 1.0)
        assert(orderStatistic(sample, 1.0) == 5.0)
        assert(orderStatistic(sample, 0.5) == 3.0)
        assert(orderStatistic(sample, 0.9) == 5.0)
        assert(sample[0] == 5.0)
    }

    @Test
    fun `gate 4 - the binomial standard error falls as one over the square root of the count`() {
        assert(
            (binomialStandardError(0.5, 2500) / binomialStandardError(0.5, 10000))
                .isCloseTo(2.0, 1e-12)
        )
        assert(binomialStandardError(0.0, 100) == 0.0)
        assert(binomialStandardError(1.0, 100) == 0.0)
    }

    @Test
    fun `gate 4 - the fitted band converges under a finer bisection`() {
        val coarse = bandWidthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95, tolerance = 1e-4)
        val fine = bandWidthForAreaMean(64.56, 102.68, 0.84, 0.48, 0.95, tolerance = 1e-12)
        assert(abs(coarse - fine) < 1e-3)
    }

    @Test
    fun `gate 4 - the empirical dropout rate converges on the field it was drawn from`() {
        val field = flatBandIncorporation(edgeX, edgeY, 4.93)
        val probabilities = grid.map { (x, y) -> field.at(x, y) }
        val mean = probabilities.average()
        val random = DropoutRandom(4242L)
        val draws = 20000
        var kept = 0
        repeat(draws) { kept += bernoulliPresence(probabilities, random).count { it } }
        val rate = kept.toDouble() / (draws * grid.size)
        assert(abs(rate - mean) < 4.0 / sqrt((draws * grid.size).toDouble()))
    }

    // ------------------------------------- gate 5: the measured map, and its own three checks

    @Test
    fun `gate 5 - the map holds 168 probed cells of 192, unprobed only in the interior`() {
        val probed = StrausIncorporationMap.probedCells()
        assert(probed.size == StapleDropoutLiterature.PROBED_STAPLES)
        assert(StrausIncorporationMap.COLUMNS * StrausIncorporationMap.ROWS - probed.size == 24)
        // every unprobed cell is at columns C, G, K, O and rows 2-4 or 9-11 (1-based)
        for (row in 0 until StrausIncorporationMap.ROWS) {
            for (column in 0 until StrausIncorporationMap.COLUMNS) {
                if (StrausIncorporationMap.DETECTION_PER_CENT[row][column].isNaN()) {
                    assert(column % 4 == 2)
                    assert(row in listOf(1, 2, 3, 8, 9, 10))
                    assert(!StrausIncorporationMap.onPerimeter(column, row))
                }
            }
        }
    }

    @Test
    fun `gate 5 - the map reproduces the paper's own printed minimum, maximum and mean`() {
        val detection = StrausIncorporationMap.probedCells().map {
            it.third - StapleDropoutLiterature.DETECTION_TO_INCORPORATION_OFFSET
        }
        assert(detection.min().isCloseTo(StapleDropoutLiterature.DETECTION_MINIMUM, 2e-2))
        assert(detection.max().isCloseTo(StapleDropoutLiterature.DETECTION_MAXIMUM, 3e-3))
        assert(detection.average().isCloseTo(StapleDropoutLiterature.DETECTION_MEAN, 1e-2))
        val incorporation = StrausIncorporationMap.probedCells().map { it.third }
        assert(incorporation.average().isCloseTo(StapleDropoutLiterature.INCORPORATION_MEAN, 1e-2))
    }

    @Test
    fun `gate 5 - every measured cell is a multiple of one over 186, which fixes the denominator`() {
        val detection = StrausIncorporationMap.probedCells().map {
            it.third - StapleDropoutLiterature.DETECTION_TO_INCORPORATION_OFFSET
        }
        fun residual(structures: Int) = detection.map {
            val counts = it * structures
            abs(counts - kotlin.math.round(counts))
        }.average()
        // n = 186 is the Methods' own count and is the only denominator the digits fit: the mean
        // distance to the nearest whole count is 0.067 there against 0.23 at either neighbour,
        // which is a 3.5x discrimination and is what certifies the transcription.
        assert(residual(186) < 0.10)
        assert(residual(185) > 0.20)
        assert(residual(187) > 0.20)
        assert(residual(372) > residual(186))
        assert(residual(185) / residual(186) > 3.0)
        assert(residual(187) / residual(186) > 3.0)
    }

    @Test
    fun `gate 5 - the 48 percent is ONE CORNER and the perimeter MEAN is far above it`() {
        val cells = StrausIncorporationMap.probedCells()
        val perimeter = cells.filter { StrausIncorporationMap.onPerimeter(it.first, it.second) }
        val interior = cells.filter { !StrausIncorporationMap.onPerimeter(it.first, it.second) }
        assert(perimeter.size == 52)
        assert(interior.size == 116)
        // the single lowest cell is the bottom-right corner, and it IS CH-0084's 48 %
        val lowest = cells.minByOrNull { it.third }!!
        assert(lowest.first == StrausIncorporationMap.COLUMNS - 1 && lowest.second == 0)
        assert(lowest.third.isCloseTo(StapleDropoutLiterature.INCORPORATION_EDGE, 2e-2))
        // but the perimeter MEAN is nowhere near it
        assert(perimeter.map { it.third }.average() > 0.77)
        assert(interior.map { it.third }.average() > 0.86)
        assert(perimeter.count { it.third > interior.map { c -> c.third }.average() } == 6)
    }

    @Test
    fun `gate 5 - how many measured positions are inside the standing scatter thresholds`() {
        // A threshold on a Bernoulli relative scatter is a threshold on an incorporation:
        // sigma_rel = sqrt((1 - p)/p) = t  <=>  p = 1/(1 + t^2).
        fun incorporationFor(threshold: Double) = 1.0 / (1.0 + threshold * threshold)
        assert(incorporationFor(0.346).isCloseTo(0.89306, 1e-4))
        assert(incorporationFor(0.316).isCloseTo(0.90923, 1e-4))
        assert(incorporationFor(0.170).isCloseTo(0.97193, 1e-4))
        val cells = StrausIncorporationMap.probedCells().map { it.third }
        assert(cells.count { it >= incorporationFor(0.346) } == 30)
        assert(cells.count { it >= incorporationFor(0.316) } == 20)
        assert(cells.count { it >= incorporationFor(0.170) } == 0)
    }

    @Test
    fun `gate 5 - the measured depth table is total and reproduces the map's own mean`() {
        val table = measuredDepthTable()
        assert(table.alongClasses == 8)
        assert(table.acrossClasses == 6)
        for (j in 0 until table.alongClasses) for (k in 0 until table.acrossClasses) {
            val value = table.at((j + 0.5) * table.alongPitch, (k + 0.5) * table.acrossPitch)
            assert(value > 0.0 && value <= 1.0)
        }
        // the deepest class is the interior and the shallowest the perimeter
        assert(table.at(0.0, 0.0) < table.at(100.0, 100.0))
        assert(
            table.cellWeightedMean.isCloseTo(StapleDropoutLiterature.INCORPORATION_MEAN, 1e-2)
        )
    }

    @Test
    fun `gate 5 - the measured field is a probability, point-symmetric, and clamped outside`() {
        val field = measuredDepthIncorporation(edgeX, edgeY)
        for (i in 0..12) for (j in 0..12) {
            val x = -edgeX / 2.0 + edgeX * i / 12.0
            val y = -edgeY / 2.0 + edgeY * j / 12.0
            val p = field.at(x, y)
            assert(p > 0.0 && p <= 1.0)
            assert(p.isCloseTo(field.at(-x, -y), 1e-12))
        }
    }

    // ------------------------------------------------- gate 5: the upstream station arithmetic

    @Test
    fun `gate 5 - the 3 x 15 grid's rim mask is C-0058's 34 and 11`() {
        val mask = rimMask(grid, edgeX, edgeY, 6.7)
        assert(mask.count { it } == 34)
        assert(mask.count { !it } == 11)
    }

    @Test
    fun `gate 5 - CH-0084's uniform reading is a 16 percent shortfall on the mandate`() {
        val k = List(45) { mandate / 45.0 }
        val p = List(45) { 0.84 }
        assert(expectedTotalStiffness(k, p).isCloseTo(28.0, 1e-4))
        assert((1.0 - expectedTotalStiffness(k, p) / k.sum()).isCloseTo(0.16, 1e-9))
    }

    // ---------------------------------------------------------------------------------------
    // `T-210`/`C-0129` — a SATURATED statistic is the resolution of nothing
    // ---------------------------------------------------------------------------------------

    @Test
    fun `gate 4 statistical power - the symmetric error carries no information at saturation`() {
        // The instrument `T-148`'s convergence note calls "the resolution the verdict is quoted
        // to" is IDENTICALLY zero at every one of the five sample counts it was read over, so it
        // cannot distinguish 1250 draws from 20000. The one-sided bound can, and does.
        assert(binomialStandardError(1.0, 1250) == binomialStandardError(1.0, 20000))
        assert(saturatedProportionBound(1.0, 1250) < saturatedProportionBound(1.0, 20000))
    }

    @Test
    fun `gate 5 literature cross-check - the rule of three approximates the exact bound`() {
        // Hanley & Lippman-Hand: at zero observed events in n trials the 95 % upper bound is
        // about 3/n. It is the large-n form of the exact Clopper-Pearson bound implemented here.
        // Compared ABSOLUTELY: the two bounds are 1 - x and x of each other, so a RELATIVE
        // comparison flatters the p-hat = 1 end by exactly the factor the p-hat = 0 end is
        // penalised by (4.7e-7 either way, but 4.7e-7 against 1e-3 relative).
        val n = 10000
        assert(abs(saturatedProportionBound(1.0, n) - (1.0 - 3.0 / n)) < 5e-7)
        assert(abs(saturatedProportionBound(0.0, n) - 3.0 / n) < 5e-7)
    }

    @Test
    fun `gate 3 symmetry - the bound is exact by construction`() {
        // `bound^n` is the probability of observing n successes in a row at the bound, which IS
        // the definition of a one-sided Clopper-Pearson limit: it must return 1 - confidence.
        val n = 137
        val bound = saturatedProportionBound(1.0, n)
        assert(bound.pow(n).isCloseTo(0.05, 1e-12))
        assert(saturatedProportionBound(0.0, n).isCloseTo(1.0 - bound, 1e-12))
    }

    @Test
    fun `gate 2 limiting case - a single draw bounds the proportion at the confidence level`() {
        assert(saturatedProportionBound(1.0, 1).isCloseTo(0.05, 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - the bound refuses an unsaturated proportion`() {
        // At 0 < p-hat < 1 the symmetric error IS the instrument; asking for a one-sided bound
        // there is asking the wrong question, and a silent answer would hide that.
        assertFailsWith<IllegalArgumentException> { saturatedProportionBound(0.5, 100) }
        assertFailsWith<IllegalArgumentException> { saturatedProportionBound(1.0, 0) }
    }
}
