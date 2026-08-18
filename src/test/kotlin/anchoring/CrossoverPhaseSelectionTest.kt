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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformCurvatureRigidity
import com.xemantic.nano.plentyofroom.structure.uniformMomentRigidity
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-171`, leaf `A8.2` — the crossover phase is **one** integer and three standing claims want
 * different values of it.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The load-bearing gates are **gate 2** (at 40.00 nm with the row end refused this census must
 * reproduce `C-0098`'s published table exactly — 52/53/60, ten eight-column phases, `{8, 24}`
 * centro-symmetric — or the comparison is on the wrong lattice) and **gate 3** (the phase's period
 * is 32 and not 16, re-asserted on the census rather than on the column lattice).
 */
class CrossoverPhaseSelectionTest {

    private val duplexes = 15
    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
    private val nominal = Gen1Tile.EDGE_X
    private val buildable = BUILDABLE_RASTER_WIDTH
    private val arm = quantisedToRise(C0055_ARM_LENGTH)

    private val nominalCensus = crossoverPhaseCensus(nominal, duplexes, sheet, admitRowEnd = false)
    private val buildableCensus =
        crossoverPhaseCensus(buildable, duplexes, sheet, admitRowEnd = true)

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - a census is one row per phase and every count is a count`() {
        assert(nominalCensus.size == 32)
        assert(nominalCensus.map { it.phaseBasePairs } == (0 until 32).toList())
        nominalCensus.forEach { row ->
            assert(row.columns >= 2)
            assert(row.upwardSites >= 0)
            assert(row.crossoversPerInterface.size == duplexes - 1)
            assert(row.crossoversPerInterface.all { it >= 0 })
            // the per-interface counts are a partition of the interface total
            assert(row.crossoversPerInterface.sum() == row.interfaceCrossovers)
        }
    }

    @Test
    fun `gate 1 - every upward station of every phase lies inside its own footprint`() {
        listOf(nominal to false, buildable to true).forEach { (edgeX, admit) ->
            (0 until 32).forEach { phase ->
                rasterUpwardSites(phase, edgeX, duplexes, admit).forEach { row ->
                    row.forEach { x -> assert(abs(x) <= edgeX / 2.0 + 1e-9) }
                }
            }
        }
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            crossoverPhaseCensus(0.0, duplexes, sheet, admitRowEnd = false)
        }
        assertFailsWith<IllegalArgumentException> {
            crossoverPhaseCensus(nominal, 1, sheet, admitRowEnd = false)
        }
        assertFailsWith<IllegalArgumentException> { severanceProbability(emptyList(), 0.84) }
        assertFailsWith<IllegalArgumentException> { severanceProbability(listOf(4, 4), 1.5) }
        assertFailsWith<IllegalArgumentException> { severanceProbability(listOf(4, -1), 0.84) }
        assertFailsWith<IllegalArgumentException> {
            upwardPlacementFromKey(
                "nonsense", 8, arm, buildable,
                rasterUpwardSites(8, buildable, duplexes, admitRowEnd = true)
            )
        }
        assertFailsWith<IllegalArgumentException> {
            rowEndUpwardStations(-1, buildable, duplexes)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - the census reproduces C-0098's published table at 40 nm`() {
        // C-0098 Bound 1, and CH-0113's table: 52 / 53 / 60 upward EAST sites
        assert(nominalCensus.map { it.upwardSites }.toSortedSet().toList() == listOf(52, 53, 60))
        assert(
            nominalCensus.filter { it.upwardSites == 60 }.map { it.phaseBasePairs } ==
                    listOf(0, 1, 2, 14, 15, 16, 17, 18, 30, 31)
        )
        // C-0015's ten eight-column phases, and its 56 / 49 interface crossovers
        assert(
            nominalCensus.filter { it.columns == 8 }.map { it.phaseBasePairs } ==
                    listOf(6, 7, 8, 9, 10, 22, 23, 24, 25, 26)
        )
        assert(nominalCensus.filter { it.columns == 8 }.all { it.interfaceCrossovers == 56 })
        assert(nominalCensus.filter { it.columns == 7 }.all { it.interfaceCrossovers == 49 })
        // C-0063's two centro-symmetric phases
        assert(
            nominalCensus.filter { it.centroSymmetric }.map { it.phaseBasePairs } == listOf(8, 24)
        )
    }

    @Test
    fun `gate 2 - the demand ledger names the three sets and every intersection`() {
        val ledger = phaseDemandLedger(nominalCensus)
        assert(ledger.richestUpwardInventory == listOf(0, 1, 2, 14, 15, 16, 17, 18, 30, 31))
        assert(ledger.eightColumnHost == listOf(6, 7, 8, 9, 10, 22, 23, 24, 25, 26))
        assert(ledger.centroSymmetric == listOf(8, 24))
        // C-0098: the first two are disjoint and the third is inside the second
        assert(ledger.richestAndColumns.isEmpty())
        assert(ledger.richestAndSymmetry.isEmpty())
        assert(ledger.columnsAndSymmetry == listOf(8, 24))
        assert(ledger.allThree.isEmpty())
    }

    @Test
    fun `gate 2 - the series rigidity is exactly the squared duplex residual on a uniform lattice`() {
        // C-0054's own identity: on a uniform lattice the uniform-moment reading is exactly
        // (D/(D-1))^2 times the uniform-curvature one.
        val perInterface = List(duplexes - 1) { 4 }
        val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET
        val smeared = uniformCurvatureRigidity(
            perInterface.sum(), sheet.crossoverHingeStiffness,
            Gen1Tile.INTERHELICAL_SHEET, nominal * lengthY
        )
        val series = uniformMomentRigidity(
            perInterface, sheet.crossoverHingeStiffness, nominal, lengthY
        )
        val residual = duplexes.toDouble() / (duplexes - 1)
        assert((series / smeared).isCloseTo(residual * residual, 1e-12))
    }

    @Test
    fun `gate 2 - a seven-column sheet splits its interfaces four and three`() {
        val seven = nominalCensus.first { it.columns == 7 }
        assert(seven.crossoversPerInterface.count { it == 4 } == 7)
        assert(seven.crossoversPerInterface.count { it == 3 } == 7)
        val eight = nominalCensus.first { it.columns == 8 }
        assert(eight.crossoversPerInterface.all { it == 4 })
    }

    @Test
    fun `gate 2 - a severance probability is zero at full incorporation and one at none`() {
        assert(severanceProbability(listOf(4, 3, 4), 1.0).isCloseTo(0.0))
        assert(severanceProbability(listOf(4, 3, 4), 0.0).isCloseTo(1.0))
        // one interface, n crossovers: exactly (1 - p)^n
        assert(severanceProbability(listOf(3), 0.84).isCloseTo(0.16 * 0.16 * 0.16, 1e-9))
        // and it is monotone in the interface's own redundancy
        assert(severanceProbability(listOf(3), 0.84) > severanceProbability(listOf(4), 0.84))
    }

    @Test
    fun `gate 2 - an empty interface annihilates the series rigidity and not the smeared one`() {
        val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET
        val emptied = List(duplexes - 1) { if (it == 3) 0 else 4 }
        assert(
            uniformMomentRigidity(emptied, sheet.crossoverHingeStiffness, nominal, lengthY)
                .isCloseTo(0.0)
        )
        assert(
            uniformCurvatureRigidity(
                emptied.sum(), sheet.crossoverHingeStiffness,
                Gen1Tile.INTERHELICAL_SHEET, nominal * lengthY
            ) > 0.0
        )
    }

    // --------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - the phase period is 32 and not 16`() {
        (0 until 32).forEach { phase ->
            val here = crossoverPhaseRow(phase, nominal, duplexes, sheet, admitRowEnd = false)
            val shifted =
                crossoverPhaseRow(phase + 32, nominal, duplexes, sheet, admitRowEnd = false)
            assert(here.columns == shifted.columns)
            assert(here.upwardSites == shifted.upwardSites)
            assert(here.crossoversPerInterface == shifted.crossoversPerInterface)
        }
        // a half-period shift is NOT the identity: it hands every interface the other parity's
        // columns, and on this lattice it moves the upward inventory too
        assert(
            (0 until 32).any { phase ->
                crossoverPhaseRow(phase, nominal, duplexes, sheet, false).upwardSites !=
                        crossoverPhaseRow(phase + 16, nominal, duplexes, sheet, false).upwardSites
            }
        )
    }

    @Test
    fun `gate 3 - the census's centro-symmetric set is C-0063's own congruence`() {
        listOf(nominal to false, buildable to true).forEach { (edgeX, admit) ->
            val fromCensus = crossoverPhaseCensus(edgeX, duplexes, sheet, admit)
                .filter { it.centroSymmetric }.map { it.phaseBasePairs }
            assert(fromCensus == centroSymmetricUpwardPhases(edgeX, duplexes))
        }
    }

    @Test
    fun `gate 3 - a placement round-trips through its own key`() {
        val sites = rasterUpwardSites(8, buildable, duplexes, admitRowEnd = true)
        val placement = UpwardArmPlacement(
            8,
            (0 until duplexes).map { row ->
                val roots = rowRootOptions(sites[row], 2, arm, buildable).first()
                UpwardArmRow(row, roots, armDirections(roots, arm, buildable)!!)
            }
        )
        val parsed = upwardPlacementFromKey(placement.key, 8, arm, buildable, sites)
        assert(parsed.key == placement.key)
        assert(parsed.count == placement.count)
    }

    // ------------------------------------------------------- gate 4 — the row-end census

    @Test
    fun `gate 4 - admitting the row end is worth upward STATIONS at two phases of the buildable width`() {
        // CH-0118. C-0090: "an end plane has an even index ... the row-end crossover can never be
        // an upward site, at ANY phase". True at 8 and 24, where the end plane IS a column; false
        // at 0 and 16, where 19.04 nm is an ODD number of 2.72 nm planes.
        assert(rowEndUpwardStations(8, buildable, duplexes) == 0)
        assert(rowEndUpwardStations(24, buildable, duplexes) == 0)
        assert(rowEndUpwardStations(0, buildable, duplexes) == 15)
        assert(rowEndUpwardStations(16, buildable, duplexes) == 15)
        // and at those two phases it adds no columns at all, which is the exact inverse of the
        // claim's reading
        assert(rowEndColumns(0, buildable, duplexes, sheet) == 0)
        assert(rowEndColumns(8, buildable, duplexes, sheet) == 2)
    }

    @Test
    fun `gate 4 - at 40 nm the row-end convention is inert`() {
        (0 until 32).forEach { phase ->
            assert(rowEndUpwardStations(phase, nominal, duplexes) == 0)
            assert(rowEndColumns(phase, nominal, duplexes, sheet) == 0)
        }
    }

    // ------------------------------------------------- gate 5 — the buildable-width census

    @Test
    fun `gate 5 - at the buildable width all three demands collapse to two phases each`() {
        val ledger = phaseDemandLedger(buildableCensus)
        assert(ledger.richestUpwardInventory == listOf(0, 16))
        assert(ledger.eightColumnHost == listOf(8, 24))
        assert(ledger.centroSymmetric == listOf(8, 24))
        // still disjoint — F1 does not fire
        assert(ledger.richestAndColumns.isEmpty())
        assert(ledger.allThree.isEmpty())
        assert(buildableCensus.filter { it.upwardSites == 60 }.all { it.columns == 7 })
    }
}
