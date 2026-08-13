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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The `T-14` staple-layout design variables, gate by gate.
 *
 * Two things are under test here and neither exists in `T-10`:
 *
 * 1. the **crossover column phase** — where the column lattice sits relative to the tile
 *    edge, which a staple layout chooses for free and which `C-0009` could only probe by
 *    comparing a seven-column lattice against an eight-column one;
 * 2. the **registration** of an attachment within the one-crossover unit cell, swept over
 *    the cell rather than sampled at the four named placements `C-0009` reports.
 *
 * Every test is named for the verification gate it discharges, as `T-5`, `T-5b` and `T-10`
 * established.
 */

private val t14Sheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

/** The column spacing `p/2` in nm — crossovers alternate, so the columns are twice as dense. */
private val columnSpacing = t14Sheet.crossoverSpacing / 2.0

/** `C-0001`'s secant foundation stiffness over the tile, in `pN/nm³`. */
private const val T14_FOUNDATION = 0.012625625

private const val T14_TARGET_FORCE = 100.0

private fun phasedGrillage(
    phase: Double,
    foundationStiffness: Double = T14_FOUNDATION,
    subdivisions: Int = 2,
    supports: List<PointSupport> = emptyList(),
    sheet: OrigamiSheet = t14Sheet
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = 15,
    foundationStiffness = foundationStiffness,
    columns = CrossoverLayout.phased(phase, sheet.crossoverSpacing / 2.0, Gen1Tile.EDGE_X),
    subdivisions = subdivisions,
    supports = supports
)

/** The continuum plate on the lattice's own footprint, `T-10`'s comparison object. */
private fun t14Plate(): PlateOnFoundation = PlateOnFoundation(
    OrthotropicPlate(
        lengthX = Gen1Tile.EDGE_X,
        lengthY = 15 * t14Sheet.interhelicalDistance,
        rigidityX = t14Sheet.alongHelixRigidity,
        rigidityY = t14Sheet.acrossHelixRigidity,
        twistingRigidity = t14Sheet.twistingRigidity
    ),
    T14_FOUNDATION,
    basisDegree = 12
)

class CrossoverLayoutTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a centred layout should reproduce the T-10 column construction exactly`() {
        val centred = CrossoverLayout.centred(8, columnSpacing)
        assert(centred.size == 8)
        centred.positions.forEachIndexed { i, x ->
            assert(x.isCloseTo((i - 3.5) * columnSpacing, 1e-12))
        }
        // `T-10` assigned an interface its columns by the index within the retained list
        assert(centred.parities == List(8) { it % 2 })
    }

    @Test
    fun `gate 1 dimensional consistency - the nominal T-10 layout is the phase at half a column spacing`() {
        val phased = CrossoverLayout.phased(columnSpacing / 2.0, columnSpacing, Gen1Tile.EDGE_X)
        val centred = CrossoverLayout.centred(8, columnSpacing)
        assert(phased.size == centred.size)
        phased.positions.forEachIndexed { i, x ->
            assert(x.isCloseTo(centred.positions[i], 1e-12))
        }
        assert(phased.parities == centred.parities)
    }

    @Test
    fun `gate 1 dimensional consistency - the base-pair phase should span exactly one interface period`() {
        // a staple layout can only place a crossover at a base pair, so the phase is
        // quantised, and there are exactly `crossoverSpacing` base pairs in one period
        assert(CrossoverLayout.BASE_PAIRS_PER_PERIOD == 32)
        val period = CrossoverLayout.BASE_PAIRS_PER_PERIOD * Gen1Tile.RISE_PER_BASE_PAIR
        assert(period.isCloseTo(t14Sheet.crossoverSpacing, 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - a 40 nm tile should hold seven or eight columns at every phase`() {
        val counts = (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).map { basePairs ->
            CrossoverLayout.atBasePairPhase(basePairs, t14Sheet, Gen1Tile.EDGE_X).size
        }
        assert(counts.all { it == 7 || it == 8 })
        assert(counts.contains(7))
        assert(counts.contains(8))
    }

    @Test
    fun `a non-physical column layout should be rejected on construction`() {
        assertFailsWith<IllegalArgumentException> { CrossoverLayout.centred(1, columnSpacing) }
        assertFailsWith<IllegalArgumentException> { CrossoverLayout.centred(8, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            CrossoverLayout(listOf(0.0, 1.0), listOf(0, 1, 0))
        }
        assertFailsWith<IllegalArgumentException> {
            // positions must ascend, or the node stations they seed are not a mesh
            CrossoverLayout(listOf(1.0, 0.0), listOf(0, 1))
        }
        assertFailsWith<IllegalArgumentException> {
            CrossoverLayout(listOf(0.0, 1.0), listOf(0, 2))
        }
    }

    // ---------------------------------------------------------------- gate 2

    /**
     * **The period of the phase variable is `p = 32 bp`, not `p/2 = 16 bp`** — the single
     * most confusable thing about this design variable, and the exact analogue of the
     * per-helix / per-interface confusion `CLAUDE.md` records for the spacing itself.
     *
     * Shifting the column lattice by one **column** spacing leaves the column positions
     * inside the footprint unchanged but hands every interface the other parity's columns,
     * which is a different structure. Only a shift by two column spacings — one full
     * per-interface spacing — is the identity.
     */
    @Test
    fun `gate 2 limiting cases - the column phase should have period p and not half of it`() {
        listOf(0.0, 0.34, 1.7, 3.4, 5.1).forEach { phase ->
            val base = CrossoverLayout.phased(phase, columnSpacing, Gen1Tile.EDGE_X)
            val fullPeriod = CrossoverLayout.phased(
                phase + 2.0 * columnSpacing, columnSpacing, Gen1Tile.EDGE_X
            )
            assert(fullPeriod.size == base.size)
            base.positions.forEachIndexed { i, x ->
                assert(x.isCloseTo(fullPeriod.positions[i], 1e-9))
            }
            assert(fullPeriod.parities == base.parities)

            val halfPeriod = CrossoverLayout.phased(
                phase + columnSpacing, columnSpacing, Gen1Tile.EDGE_X
            )
            assert(halfPeriod.size == base.size)
            halfPeriod.positions.forEachIndexed { i, x ->
                assert(x.isCloseTo(base.positions[i], 1e-9))
            }
            // the same columns, serving the other parity of interface: a different sheet
            assert(halfPeriod.parities == base.parities.map { 1 - it })
        }
    }

    @Test
    fun `gate 2 limiting cases - a phased lattice should still recover the plate across-helix rigidity`() {
        // the gate that licenses T-10's whole comparison must survive re-phasing: the
        // lattice cost is still exactly half k_theta (kappa d)^2 per crossover, so the only
        // thing the phase can move is the integer number of crossovers the footprint holds
        val curvature = 0.001
        listOf(0.0, 1.36, 2.72, 4.08).forEach { phase ->
            val lattice = phasedGrillage(phase)
            val field = lattice.curvatureFieldAcrossHelices(curvature)
            val perCrossover = 0.5 * t14Sheet.crossoverHingeStiffness *
                    (curvature * t14Sheet.interhelicalDistance).let { it * it }
            assert(
                lattice.structuralEnergy(field)
                    .isCloseTo(perCrossover * lattice.crossovers.size, 1e-9)
            )
        }
    }

    @Test
    fun `gate 2 limiting cases - a uniform load should dish no phased lattice at all`() {
        listOf(0.0, 0.68, 2.72, 4.76).forEach { phase ->
            val lattice = phasedGrillage(phase)
            val solution = lattice.solve(uniformPressure(T14_TARGET_FORCE / lattice.area))
            assert(solution.peakDishing() < 1e-9)
            assert(solution.peakCrossoverForce < 1e-9)
        }
    }

    // ---------------------------------------------------------------- gate 3

    /**
     * The symmetry group depends on the **parity of the column count**, and a lattice with
     * seven columns across fifteen duplexes has no exact symmetry at all.
     *
     * Under the point inversion column `c → n−1−c` and interface `b → N−2−b`, so the parity
     * `c + b` is preserved exactly when `n + N` is odd. `T-10` asserted centro-symmetry for
     * its eight-column lattice and was right; the assertion does not survive re-phasing, and
     * assuming it would be the same class of error as assuming the rectangular group.
     */
    @Test
    fun `gate 3 symmetry - centro-symmetry should hold only when the column and duplex counts sum odd`() {
        fun centroSymmetryResidual(columns: Int): Double {
            val lattice = OrigamiGrillage(
                sheet = t14Sheet,
                lengthX = Gen1Tile.EDGE_X,
                beamCount = 15,
                foundationStiffness = T14_FOUNDATION,
                columns = CrossoverLayout.centred(columns, columnSpacing)
            )
            val solution = lattice.solve(
                pointLoads = listOf(PointLoad(0.0, 0.0, T14_TARGET_FORCE))
            )
            return listOf(3.0 to 5.38, 5.44 to 8.07, 12.0 to 2.69).maxOf { (x, y) ->
                abs(solution.deflection(x, y) - solution.deflection(-x, -y)) /
                        abs(solution.deflection(x, y))
            }
        }
        // 8 + 15 = 23, odd: exactly centro-symmetric, as T-10 asserted
        assert(centroSymmetryResidual(8) < 1e-8)
        // 7 + 15 = 22, even: the inversion maps crossovers onto empty sites, so it is not
        // a symmetry — a seven-column tile has neither mirror nor inversion
        assert(centroSymmetryResidual(7) > 1e-3)
    }

    // ---------------------------------------------------------------- the rank-one anchor

    @Test
    fun `gate 1 dimensional consistency - the rank-one anchor update should equal a re-assembled anchored lattice`() {
        val anchor = PointSupport(1.3, -0.7, T14_FOUNDATION * (40.0 * 15 * 2.69))
        val pressure = uniformPressure(T14_TARGET_FORCE / (40.0 * 15 * 2.69))
        val updated = phasedGrillage(2.72).solveWithAnchor(anchor, pressure)
        val assembled = phasedGrillage(2.72, supports = listOf(anchor)).solve(pressure)
        assert(updated.deflection(0.0, 0.0).isCloseTo(assembled.deflection(0.0, 0.0), 1e-9))
        assert(updated.peakCrossoverForce.isCloseTo(assembled.peakCrossoverForce, 1e-9))
        assert(updated.peakDuplexShear.isCloseTo(assembled.peakDuplexShear, 1e-9))
        assert(
            updated.supportForces.single().isCloseTo(assembled.supportForces.single(), 1e-9)
        )
    }

    @Test
    fun `gate 3 conservation - a rank-one anchored solution should still balance forces`() {
        val area = 40.0 * 15 * 2.69
        val anchor = PointSupport(-4.0, 3.0, 10.0 * T14_FOUNDATION * area)
        val solution = phasedGrillage(2.72)
            .solveWithAnchor(anchor, uniformPressure(T14_TARGET_FORCE / area))
        assert(solution.appliedForce.isCloseTo(T14_TARGET_FORCE, 1e-9))
        assert(
            (solution.foundationForce + solution.supportForces.sum())
                .isCloseTo(solution.appliedForce, 1e-8)
        )
    }

    @Test
    fun `gate 2 limiting cases - a rigid anchor should take the whole load off the foundation`() {
        val area = 40.0 * 15 * 2.69
        val rigid = t14Sheet.copy(
            duplex = t14Sheet.duplex.copy(bendingRigidity = 1e8, torsionalRigidity = 1e8),
            crossoverHingeStiffness = 1e8
        )
        val solution = OrigamiGrillage(
            sheet = rigid,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = 15,
            foundationStiffness = T14_FOUNDATION,
            columns = CrossoverLayout.centred(8, columnSpacing),
            linkStiffness = 1e10
        ).solveWithAnchor(
            PointSupport(0.0, 0.0, 1e8),
            uniformPressure(T14_TARGET_FORCE / area)
        )
        assert(solution.supportForces.single() / T14_TARGET_FORCE > 0.999)
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the extreme phase should converge in the element subdivision`() {
        // the phase that puts a crossover column 0.28 nm from the tile edge is the worst
        // conditioned lattice in the sweep: its edge element is 20x shorter than the rest
        val worst = (0 until CrossoverLayout.BASE_PAIRS_PER_PERIOD).minByOrNull { basePairs ->
            val layout = CrossoverLayout.atBasePairPhase(basePairs, t14Sheet, Gen1Tile.EDGE_X)
            Gen1Tile.EDGE_X / 2.0 - layout.positions.maxOf { abs(it) }
        }!!
        val phase = worst * Gen1Tile.RISE_PER_BASE_PAIR
        val forces = listOf(1, 2, 4).map { subdivisions ->
            phasedGrillage(phase, subdivisions = subdivisions)
                .solve(pointLoads = listOf(PointLoad(0.0, 0.0, T14_TARGET_FORCE)))
                .peakCrossoverForce
        }
        assert(abs(forces[2] - forces[1]) / forces[2] < 0.01)
    }

    // ---------------------------------------------------------------- gate 5

    /**
     * Gate 5 in its internal form: the four anchor placements `C-0009` names must fall out
     * of the registration machinery at the values `C-0009` published, or this task is
     * measuring something else.
     */
    @Test
    fun `gate 5 cross-check - the registration map should reproduce C-0009's four named placements`() {
        val lattice = phasedGrillage(columnSpacing / 2.0)
        val anchorStiffness = T14_FOUNDATION * lattice.area
        val pressure = uniformPressure(T14_TARGET_FORCE / lattice.area)
        val half = t14Sheet.interhelicalDistance / 2.0
        val column = lattice.columnX[4]
        fun peak(x: Double, y: Double): Double = lattice
            .solveWithAnchor(PointSupport(x, y, anchorStiffness), pressure)
            .peakCrossoverForce
        // C-0009: 5.11 on a crossover, 5.56 between duplexes, 5.76 mid-span on a duplex
        // axis, 6.66 on a duplex axis at a crossover column
        assert(peak(column, half).isCloseTo(5.11, 0.01))
        assert(peak(column + columnSpacing / 2.0, half).isCloseTo(5.56, 0.01))
        assert(peak(column + columnSpacing / 2.0, 0.0).isCloseTo(5.76, 0.01))
        assert(peak(column, 0.0).isCloseTo(6.66, 0.01))
    }

    // ---------------------------------------------------------------- commensurability

    @Test
    fun `gate 1 dimensional consistency - the attachment offset spread should vanish on a commensurate row count`() {
        // an attachment row count that divides the duplex count puts every attachment at the
        // same offset from a duplex axis; 10 rows over 15 duplexes puts them all at d/4
        listOf(1, 3, 5, 15).forEach { rows ->
            assert(attachmentOffsetSpread(rows, 15) < 1e-12)
        }
        assert(attachmentOffsetSpread(10, 15) < 1e-12)
        assert(attachmentOffsetSpread(6, 15) < 1e-12)
        // and one that shares no factor with it spreads them across the whole half-cell
        assert(attachmentOffsetSpread(11, 15) > 0.8)
        assert(attachmentOffsetSpread(7, 15) > 0.6)
    }

    @Test
    fun `gate 1 dimensional consistency - the distinct offset count should be the reduced row-to-duplex ratio`() {
        // s/gcd(s, N) distinct offsets: the denominator of the row spacing in duplex units
        assert(distinctAttachmentOffsets(15, 15) == 1)
        assert(distinctAttachmentOffsets(10, 15) == 2)
        assert(distinctAttachmentOffsets(12, 15) == 4)
        assert(distinctAttachmentOffsets(11, 15) == 11)
        assert(distinctAttachmentOffsets(14, 15) == 14)
    }

    @Test
    fun `gate 1 dimensional consistency - a batched registration map should equal one anchor at a time`() {
        val lattice = phasedGrillage(columnSpacing / 2.0)
        val pressure = uniformPressure(T14_TARGET_FORCE / lattice.area)
        val stiffness = T14_FOUNDATION * lattice.area
        val anchors = listOf(
            PointSupport(0.0, 0.0, stiffness),
            PointSupport(2.72, 1.345, stiffness),
            PointSupport(-4.08, -0.7, stiffness)
        )
        val batched = lattice.solveWithEachAnchor(anchors, pressure)
        anchors.forEachIndexed { i, anchor ->
            val single = lattice.solveWithAnchor(anchor, pressure)
            assert(batched[i].peakCrossoverForce.isCloseTo(single.peakCrossoverForce, 1e-12))
            assert(
                batched[i].supportForces.single()
                    .isCloseTo(single.supportForces.single(), 1e-12)
            )
        }
    }

    /**
     * Gate 3: **registration has to be a lattice-periodic variable**, or what is being swept
     * is position-in-tile wearing a lattice's clothes.
     *
     * The comparison is made against the size of the registration effect measured in the
     * same test, so the test carries no tolerance that a later change of parameters could
     * quietly invalidate. Note the translation deliberately does not start at `−v/2`: at this
     * phase the lattice is centro-symmetric, so translating from `−v/2` to `+v/2` would be
     * the point inversion and would return exactly zero whatever the periodicity.
     */
    @Test
    fun `gate 3 symmetry - the registration variable should be periodic in the crossover lattice`() {
        val lattice = phasedGrillage(columnSpacing / 2.0)
        val pressure = uniformPressure(T14_TARGET_FORCE / lattice.area)
        val stiffness = 10.0 * T14_FOUNDATION * lattice.area
        val d = t14Sheet.interhelicalDistance
        val p = t14Sheet.crossoverSpacing
        fun peak(x: Double, y: Double): Double =
            lattice.solveWithAnchor(PointSupport(x, y, stiffness), pressure).peakCrossoverForce
        // (0, 2d) is a lattice vector of the crossover pattern: two duplexes across preserves
        // the parity of every interface. It is also the translation a 40 nm x 40.35 nm tile
        // can afford — along the helices one full `p` is 27 % of the footprint, so the
        // along-helix residual measures the free edge and not the lattice. That is reported
        // in the study rather than asserted here.
        val residuals = listOf(0.0 to 0.0, 1.36 to 0.6725, -2.04 to -1.0).map { (x, y) ->
            abs(peak(x, y + 2.0 * d) - peak(x, y)) / peak(x, y)
        }
        // and the effect the cell itself has, measured the same way
        val cell = listOf(2.72 to d / 2.0, -4.42 to -d / 2.0, p / 4.0 to 0.0, 0.0 to 0.0)
            .map { (x, y) -> peak(x, y) }
        val cellEffect = cell.max() / cell.min() - 1.0
        assert(cellEffect > 0.20)
        assert(residuals.max() < 0.1 * cellEffect)
    }

    /**
     * The mechanism behind the flatness-curve explanation, isolated **against the plate**.
     *
     * Two attachment grids of the same count and the same force each, transposed: `11 × 15`
     * puts every row exactly on a duplex axis, `15 × 11` spreads its rows over the whole
     * interhelical distance. The plate feels the aspect-ratio change too — it is 25× stiffer
     * along the helices than across them, so it always prefers more rows — and only the
     * **excess** over the plate is a statement about the duplex lattice.
     *
     * The control is in the same test: `5 × 15` against `15 × 5` transposes two row counts
     * that **both** sit exactly on duplex axes, so the rule predicts no excess there.
     */
    @Test
    fun `gate 2 limiting cases - rows on the duplex axes should beat an incommensurate row count of equal size`() {
        val lattice = phasedGrillage(columnSpacing / 2.0)
        val plate = t14Plate()
        fun excess(columns: Int, rows: Int): Double {
            fun loads(c: Int, r: Int) = (0 until c).flatMap { i ->
                (0 until r).map { j ->
                    PointLoad(
                        -Gen1Tile.EDGE_X / 2.0 + Gen1Tile.EDGE_X * (i + 0.5) / c,
                        -lattice.lengthY / 2.0 + lattice.lengthY * (j + 0.5) / r,
                        T14_TARGET_FORCE / (c * r)
                    )
                }
            }
            val a = loads(columns, rows)
            val b = loads(rows, columns)
            val latticeRatio = lattice.solve(pointLoads = b).peakDishing() /
                    lattice.solve(pointLoads = a).peakDishing()
            val plateRatio = plate.solve(pointLoads = b).peakDishing() /
                    plate.solve(pointLoads = a).peakDishing()
            return latticeRatio / plateRatio
        }
        // 15 rows sit exactly on the duplex axes; 11 rows spread over the whole spacing
        val incommensurate = excess(11, 15)
        assert(incommensurate > 3.0)
        // the control: 5 rows ALSO sit exactly on the axes, and they are a far bigger
        // reduction in row count than 11 rows are — yet the lattice penalises them much
        // less, which is what rules out "more rows is simply better on a lattice"
        val commensurate = excess(5, 15)
        assert(commensurate < incommensurate / 2.5)
    }

    /**
     * Gate 5, as the **control that decides whether any of this is about the sheet**: the
     * continuum plate has no duplexes and no unit cell, so its flatness-versus-attachment
     * curve must be monotone. The lattice's is not, and that difference is the whole of the
     * effect `C-0009` reported and could not explain.
     */
    @Test
    fun `gate 5 cross-check - the plate flatness curve should be monotone where the lattice's is not`() {
        val lattice = phasedGrillage(columnSpacing / 2.0)
        val plate = t14Plate()
        fun loads(side: Int) = (0 until side).flatMap { i ->
            (0 until side).map { j ->
                PointLoad(
                    -Gen1Tile.EDGE_X / 2.0 + Gen1Tile.EDGE_X * (i + 0.5) / side,
                    -lattice.lengthY / 2.0 + lattice.lengthY * (j + 0.5) / side,
                    T14_TARGET_FORCE / (side * side)
                )
            }
        }
        val sides = 8..12
        val plateCurve = sides.map { plate.solve(pointLoads = loads(it)).peakDishing() }
        val latticeCurve = sides.map { lattice.solve(pointLoads = loads(it)).peakDishing() }
        assert(plateCurve == plateCurve.sortedDescending())
        assert(latticeCurve != latticeCurve.sortedDescending())
    }

    @Test
    fun `a non-physical attachment grid should be rejected`() {
        assertFailsWith<IllegalArgumentException> { attachmentOffsetSpread(0, 15) }
        assertFailsWith<IllegalArgumentException> { attachmentOffsetSpread(4, 0) }
    }

}
