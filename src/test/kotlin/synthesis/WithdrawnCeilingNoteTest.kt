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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-169` — a study's emitted prose cites the ceiling `C-0049` withdrew.
 *
 * `C-0101` §4 recorded that re-emitting the elastica study on the repaired solver wrote
 * *"places, but past the **40 pN/nm** ceiling at the desired stroke"* into 26 of its 34 placement
 * rows. `C-0049` withdrew that reading: 40 pN/nm is `1.2 × (100 pN / 3 nm)`, a declared linearity
 * tolerance that carries the **acceptable** clause's stroke inside it, and the same construction
 * at §3's **desired** clause is **12 pN/nm**. Reading 40 at a 10 nm stroke is not conservative —
 * it is the wrong clause's number and 3.33× too generous.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class WithdrawnCeilingNoteTest {

    // -------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 — a clause reading carries a stiffness on every one of its three ceilings`() {
        val reading = clauseCeilingReading(
            targetForce = 100.0,
            placementStroke = 3.0,
            stroke = 10.0,
            pathCount = 45,
            unzipAllowable = 10.0
        )
        // pN / nm on all three, and the stroke it is read at carried beside them
        assert(reading.stroke.isCloseTo(10.0))
        assert(reading.declaredCeilingAtThisClause.isCloseTo(12.0))
        assert(reading.declaredCeilingAtPlacementClause.isCloseTo(40.0))
        assert(reading.perPathSecantCeiling.isCloseTo(45.0))
    }

    @Test
    fun `gate 1 — an unphysical clause reading throws rather than returning a number`() {
        assertFailsWith<IllegalArgumentException> {
            clauseCeilingReading(100.0, 3.0, 0.0, 45, 10.0)
        }
        assertFailsWith<IllegalArgumentException> {
            clauseCeilingReading(100.0, 0.0, 10.0, 45, 10.0)
        }
        assertFailsWith<IllegalArgumentException> {
            clauseCeilingReading(100.0, 3.0, 10.0, 0, 10.0)
        }
    }

    // -------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 — at the placement stroke the two declared readings are the same number`() {
        val reading = clauseCeilingReading(100.0, 3.0, 3.0, 45, 10.0)
        assert(
            reading.declaredCeilingAtThisClause
                .isCloseTo(reading.declaredCeilingAtPlacementClause)
        )
        assert(reading.declaredCeilingAtThisClause.isCloseTo(40.0))
    }

    @Test
    fun `gate 2 — the note does not quote the withdrawn ceiling at a stroke it is not owed at`() {
        val reading = clauseCeilingReading(100.0, 3.0, 10.0, 45, 10.0)
        val note = pastClauseCeilingNote(tangent = 264.24, secant = 69.94, reading = reading)
        assert(!note.contains("40 pN/nm ceiling at the desired stroke"))
        assert(note.contains("C-0049"))
        assert(note.contains("12.0"))
    }

    @Test
    fun `gate 2 — a row inside every ceiling is not described as past one`() {
        val reading = clauseCeilingReading(100.0, 3.0, 3.0, 45, 10.0)
        val note = pastClauseCeilingNote(tangent = 36.44, secant = 33.3333, reading = reading)
        assert(note.contains("inside"))
    }

    // ------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 — the declared ceiling is exactly the mandate scaled, at every stroke`() {
        listOf(0.5, 1.0, 3.0, 7.0, 10.0).forEach { stroke ->
            val reading = clauseCeilingReading(100.0, 3.0, stroke, 45, 10.0)
            assert(
                reading.declaredCeilingAtThisClause
                    .isCloseTo(DECLARED_CEILING_FACTOR * 100.0 / stroke)
            )
        }
    }

    @Test
    fun `gate 3 — the per-path ceiling tightens as one over the stroke where the declared one does too`() {
        val near = clauseCeilingReading(100.0, 3.0, 3.0, 45, 10.0)
        val far = clauseCeilingReading(100.0, 3.0, 10.0, 45, 10.0)
        // both are 1/s, so their RATIO is stroke-free — the statement C-0049 rests on
        assert(
            (near.declaredCeilingAtThisClause / near.perPathSecantCeiling)
                .isCloseTo(far.declaredCeilingAtThisClause / far.perPathSecantCeiling)
        )
    }

    // ------------------------------------------------------- gate 4: numerical convergence

    @Test
    fun `gate 4 — the withdrawn and the clause-correct reading agree on every T-79 placement`() {
        // every placing row of `T-79`'s catalogue, tangent at the desired stroke in pN/nm
        val tangentsAtDesired = listOf(
            200.9334, 170.4486, 161.6215, 159.1647,
            2027.9666, 835.5967, 445.4369, 348.5317, 264.2398, 242.6681, 236.8550,
            8147.3835, 1480.5916, 607.6449, 444.1630, 316.6317, 286.0603, 277.9541,
            203.2905, 172.0955, 163.0843, 160.5779,
            201.0121, 170.5036, 161.6704, 159.2120
        )
        val reading = clauseCeilingReading(100.0, 3.0, 10.0, 45, 10.0)
        assert(tangentsAtDesired.size == 26)
        tangentsAtDesired.forEach { tangent ->
            // withdrawn reading, clause-correct reading: the same verdict, 26 of 26
            assert((tangent > reading.declaredCeilingAtPlacementClause) ==
                    (tangent > reading.declaredCeilingAtThisClause))
        }
    }

    @Test
    fun `gate 4 — the clause-correct reading is the STRICTER of the two, by exactly ten thirds`() {
        val reading = clauseCeilingReading(100.0, 3.0, 10.0, 45, 10.0)
        assert(
            (reading.declaredCeilingAtPlacementClause / reading.declaredCeilingAtThisClause)
                .isCloseTo(10.0 / 3.0)
        )
    }

    // ------------------------------------------------------ gate 5: literature and upstream

    @Test
    fun `gate 5 — C-0049's own two rows are reproduced from the library, not quoted`() {
        assert(declaredComplianceCeiling(100.0, 3.0).isCloseTo(40.0))
        assert(declaredComplianceCeiling(100.0, 10.0).isCloseTo(12.0))
        assert(perPathSecantCeiling(10.0, 45, 3.0).isCloseTo(150.0))
        assert(perPathSecantCeiling(10.0, 45, 10.0).isCloseTo(45.0))
        assert(perPathSecantCeiling(10.0, 15, 3.0).isCloseTo(50.0))
        assert(perPathSecantCeiling(10.0, 15, 10.0).isCloseTo(15.0))
    }

}
