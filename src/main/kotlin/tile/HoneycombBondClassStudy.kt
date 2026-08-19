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

import com.xemantic.nano.plentyofroom.structure.HONEYCOMB_BOND_OFFSETS
import com.xemantic.nano.plentyofroom.structure.HoneycombSublattice
import com.xemantic.nano.plentyofroom.structure.honeycombAzimuthDegrees
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

// ---------------------------------------------------------------------------------------------
// T-244 -- the honeycomb face's crossover BOND-CLASS RESIDUES, read from caDNAno's own rule.
//
// CH-0184 charges that C-0141 section 9's "no answer here depends on the choice" of the 7-or-14 bp
// inter-row ladder offset has stopped being true: at C-0140's two-length raster the choice moves
// the 10 x 6 face's station count 55 against 50 of 60, and exactly one of 42 (phase, offset) pairs
// keeps all sixty. The cheap bound is the whole task -- three numbers out of one paragraph of the
// caDNAno paper, and integer arithmetic on the lattice this repository already builds.
// ---------------------------------------------------------------------------------------------

private const val T244_PERIOD: Int = 21

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T244BondClass(
    val sublattice: String,
    val azimuthDegrees: Double,
    val neighbourClass: Int,
    val stapleResidueAtClassZeroFive: Int,
    val scaffoldResiduesAtClassZeroFive: List<Int>,
    val partnerSublattice: String,
    val partnerAzimuthDegrees: Double,
    val partnerNeighbourClass: Int
)

@Serializable
private class T244Closure(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val crossSection: String,
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val rasterCrossovers: Int,
    val distinctReducedResidues: List<Int>,
    val closes: Boolean,
    val classZeroResidueCandidates: List<Int>,
    val offRuleCrossovers: Int
)

@Serializable
private class T244Offset(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val crossSection: String,
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val faceNormalX: Int,
    val properTransformation: Boolean,
    val interRowOffsetBasePairs: Int
)

@Serializable
private class T244Determined(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val classZeroResidue: Int,
    val blockLowBasePairs: Int,
    val blockHighBasePairs: Int,
    val ladderPhaseBasePairs: Int,
    val interRowOffsetBasePairs: Int,
    val stationsPerRow: List<Int>,
    val stationsOnFace: Int,
    val stationsAtSaturation: Int,
    val sparsestRow: Int,
    val bestOverThePhaseSweep: Int,
    val determinedPhaseIsOptimal: Boolean
)

@Serializable
private class T244Census(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val interRowOffsetBasePairs: Int,
    val basePhaseBasePairs: Int,
    val stationsOnFace: Int
)

@Serializable
private class T244Reproduction(
    val what: String,
    val published: String,
    val here: String,
    val relativeDeparture: String,
    val reproduced: Boolean
)

@Serializable
private class T244Falsifier(val name: String, val statement: String, val fired: Boolean, val note: String)

@Serializable
private class T244Result(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val bondClasses: List<T244BondClass>,
    val closure: List<T244Closure>,
    val offsets: List<T244Offset>,
    val determined: List<T244Determined>,
    val census: List<T244Census>,
    val reproductions: List<T244Reproduction>,
    val falsifiers: List<T244Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

/** The face station census through `C-0141`'s own machinery, at a swept phase and offset. */
private fun sweptCensus(rows: Int, per: Int, a: Int, b: Int, phase: Int, offset: Int): List<Int> =
    TwoLengthRaster(rows, per, a, b).stationLattice(phase, offset).map { it.size }

fun main() {
    val pairs = listOf(112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119)
    val sections = listOf("10 x 6" to (10 to 6), "15 x 4" to (15 to 4))

    println("T-244 - the honeycomb face's crossover bond-class residues")
    println("  CHEAP BOUND - three numbers out of one paragraph of PMC2731887:")
    println("    21 bp per PAIR, 7 bp per class step, +-5 bp for a scaffold crossover")

    // ------------------------------------------------------------------ the residue map
    val bondClasses = HoneycombSublattice.entries.flatMap { sublattice ->
        HONEYCOMB_BOND_OFFSETS.getValue(sublattice).map { (dx, dy) ->
            val azimuth = honeycombAzimuthDegrees(dx, dy)
            val partner =
                if (sublattice == HoneycombSublattice.A) HoneycombSublattice.B
                else HoneycombSublattice.A
            val partnerAzimuth = honeycombAzimuthDegrees(-dx, -dy)
            T244BondClass(
                sublattice = sublattice.name,
                azimuthDegrees = azimuth,
                neighbourClass = honeycombBondClass(sublattice, azimuth),
                stapleResidueAtClassZeroFive = honeycombStapleResidue(sublattice, azimuth, 5),
                scaffoldResiduesAtClassZeroFive =
                    honeycombScaffoldResidues(sublattice, azimuth, 5).sorted(),
                partnerSublattice = partner.name,
                partnerAzimuthDegrees = partnerAzimuth,
                partnerNeighbourClass = honeycombBondClass(partner, partnerAzimuth)
            )
        }
    }

    // ------------------------------------------------------------------ closure
    val closure = pairs.flatMap { (a, b) ->
        sections.flatMap { (name, dims) ->
            listOf(1, -1).flatMap { sign ->
                listOf(false, true).flatMap { mirrored ->
                    listOf(false, true).map { reversed ->
                        val r = HoneycombRasterResidues(
                            dims.first, dims.second, a, b, sign, mirrored, reversed
                        )
                        T244Closure(
                            a, b, name, sign, mirrored, reversed, r.rasterCrossovers,
                            r.distinctReducedResidues, r.closes, r.classZeroResidueCandidates,
                            r.offRuleCrossovers
                        )
                    }
                }
            }
        }
    }
    val closingPairs = pairs.filter { (a, b) -> HoneycombRasterResidues(10, 6, a, b).closes }

    // ------------------------------------------------------------------ the offset
    val offsets = listOf(112 to 108, 102 to 109).flatMap { (a, b) ->
        sections.flatMap { (name, dims) ->
            listOf(1, -1).flatMap { sign ->
                listOf(false, true).flatMap { mirrored ->
                    listOf(false, true).flatMap { reversed ->
                        listOf(1, -1).map { normal ->
                            val r = HoneycombRasterResidues(
                                dims.first, dims.second, a, b, sign, mirrored, reversed
                            )
                            T244Offset(
                                a, b, name, sign, mirrored, reversed, normal,
                                properTransformation = (mirrored == reversed),
                                interRowOffsetBasePairs = r.interRowOffsetBasePairs(normal)
                            )
                        }
                    }
                }
            }
        }
    }
    val properOffsets = offsets.filter { it.properTransformation }.map { it.interRowOffsetBasePairs }
    val improperOffsets =
        offsets.filterNot { it.properTransformation }.map { it.interRowOffsetBasePairs }

    // ------------------------------------------------------------------ the determined lattice
    val determined = closingPairs.flatMap { (a, b) ->
        sections.map { (name, dims) ->
            val r = HoneycombRasterResidues(dims.first, dims.second, a, b)
            val phase = r.determinedLadderPhaseBasePairs(1)
            val offset = r.interRowOffsetBasePairs(1)
            val sweep = (0 until T244_PERIOD).map {
                sweptCensus(dims.first, dims.second, a, b, it, offset).sum()
            }
            T244Determined(
                crossSection = name,
                senseOneBasePairs = a,
                senseTwoBasePairs = b,
                classZeroResidue = r.classZeroResidueCandidates.first(),
                blockLowBasePairs = r.blockWindow.lowBasePairs,
                blockHighBasePairs = r.blockWindow.highBasePairs,
                ladderPhaseBasePairs = phase,
                interRowOffsetBasePairs = offset,
                stationsPerRow = r.stationsPerRow(1),
                stationsOnFace = r.stationsOnFace(1),
                stationsAtSaturation = dims.first * 6,
                sparsestRow = r.stationsPerRow(1).min(),
                bestOverThePhaseSweep = sweep.max(),
                determinedPhaseIsOptimal = r.stationsOnFace(1) == sweep.max()
            )
        }
    }

    // ------------------------------------------------------------------ CH-0184's own table
    val census = sections.flatMap { (name, dims) ->
        listOf(7, 14).flatMap { offset ->
            (0 until T244_PERIOD).map { phase ->
                T244Census(
                    name, 112, 108, offset, phase,
                    sweptCensus(dims.first, dims.second, 112, 108, phase, offset).sum()
                )
            }
        }
    }

    // ------------------------------------------------------------------ reproductions
    fun departure(published: Double, here: Double): Double =
        if (published == 0.0) kotlin.math.abs(here) else kotlin.math.abs(here - published) / kotlin.math.abs(published)

    fun reproduction(what: String, published: Double, here: Double, digits: Int = 9) =
        T244Reproduction(
            what, published.emitted(digits), here.emitted(digits),
            departure(published, here).emitted(2), departure(published, here) < 1e-9
        )

    val ch0184At112Offset7Phase0 =
        census.first { it.crossSection == "10 x 6" && it.interRowOffsetBasePairs == 7 && it.basePhaseBasePairs == 0 }
    val ch0184At112Offset14Phase0 =
        census.first { it.crossSection == "10 x 6" && it.interRowOffsetBasePairs == 14 && it.basePhaseBasePairs == 0 }
    val ch0184Saturating = census.filter { it.stationsOnFace == it.crossSection.let { s -> if (s == "10 x 6") 60 else 90 } }
    val reproductions = listOf(
        reproduction("CH-0184's 10 x 6 census at phase 0, offset 7", 55.0, ch0184At112Offset7Phase0.stationsOnFace.toDouble()),
        reproduction("CH-0184's 10 x 6 census at phase 0, offset 14", 50.0, ch0184At112Offset14Phase0.stationsOnFace.toDouble()),
        reproduction("CH-0184's saturating pairs, of 84 swept cells", 2.0, ch0184Saturating.size.toDouble()),
        reproduction("C-0136's sense-1 row-length residues, summed", 42.0, admissibleRowLengthResidues(1).sum().toDouble()),
        reproduction("C-0136's sense-2 row-length residues, summed", 21.0, admissibleRowLengthResidues(2).sum().toDouble()),
        reproduction("C-0146's block extent at 112 / 108, base pairs", 116.0,
            HoneycombRasterResidues(10, 6, 112, 108).blockWindow.basePairs.toDouble())
    )

    // ------------------------------------------------------------------ falsifiers
    val f1 = properOffsets.toSet().size != 1
    val f2 = admissibleRowLengthResidues(1) != setOf(7, 17, 18) ||
            admissibleRowLengthResidues(2) != setOf(3, 4, 14)
    val f3 = reproductions.take(3).any { !it.reproduced }
    val determinedPhases = determined.map { it.ladderPhaseBasePairs }.toSet()
    val f4 = determinedPhases.size != 1
    val f5 = HoneycombRasterResidues(10, 6, 112, 108).closes

    val falsifiers = listOf(
        T244Falsifier("F1", "the derived offset is not invariant under the raster's proper relabellings", f1,
            "proper readings give " + properOffsets.toSet() + "; the improper mirror gives " +
                    improperOffsets.toSet() + ", which is the reflection of a chiral object with " +
                    "its own handedness left unreflected"),
        T244Falsifier("F2", "the residue map fails to reproduce C-0136's {0, 10, 11} length rule", f2,
            "sense 1 " + admissibleRowLengthResidues(1).sorted() + ", sense 2 " +
                    admissibleRowLengthResidues(2).sorted()),
        T244Falsifier("F3", "CH-0184's census table is not reproduced", f3,
            "55 / 50 at phase 0 and exactly two saturating cells of 84"),
        T244Falsifier("F4", "the ladder phase is free, so C-0141's 21-phase sweep is a sweep over buildable designs", f4,
            "the closing pair fixes the phase at " + determinedPhases),
        T244Falsifier("F5", "C-0140's 112 / 108 raster satisfies the scaffold rule at every crossover", f5,
            HoneycombRasterResidues(10, 6, 112, 108).offRuleCrossovers.toString() +
                    " of " + HoneycombRasterResidues(10, 6, 112, 108).rasterCrossovers +
                    " raster crossovers must be FORCED")
    )

    val findings = LinkedHashMap<String, String>()
    runCatching {
        findings["OFFSET"] = ("The inter-row ladder offset is 14 bp, not 7. A face helix's free " +
                "azimuth is class 0 on sublattice A (330 degrees) and class 1 on B (30 degrees), " +
                "and a class step is 7 bp, so the two face parities' station ladders differ by " +
                "exactly one class step. Which parity leads is fixed by the raster: the +x face " +
                "helix of an EVEN row is B and of an ODD row is A, so odd rows lead even ones by " +
                "-7 = 14 bp in TwoLengthRaster.stationLattice's own parameterisation. The value " +
                "is " + properOffsets.toSet() + " at every proper reading of " + offsets.count { it.properTransformation } +
                " swept -- both cross-sections, both axial signs, both faces, and the proper " +
                "rotation about y. C-0141 section 9's 'this repository cannot yet say which' is " +
                "answered.")
        findings["PHASE"] = ("The ladder PHASE is not free either, and that is the larger half. A " +
                "scaffold crossover sits 5 bp from its pair's staple position, so every raster " +
                "crossover fixes a residue; one lattice constant b0 must serve them all, and " +
                "where it does the station ladder is DETERMINED. At the one closing pair the " +
                "phase is " + determinedPhases + " and the face carries " +
                determined.first { it.crossSection == "10 x 6" }.stationsOnFace + " of 60 " +
                "stations, 5 and 6 alternating. C-0141's and C-0146's 21-phase sweep is " +
                "therefore a sweep over ONE buildable design and twenty that are not.")
        findings["CLOSURE"] = ("C-0140's recommended 112 / 108 bp raster does NOT close: no b0 " +
                "serves it, its reduced residues are " +
                HoneycombRasterResidues(10, 6, 112, 108).distinctReducedResidues +
                " where at most two 10 apart are admissible, and " +
                HoneycombRasterResidues(10, 6, 112, 108).offRuleCrossovers + " of " +
                HoneycombRasterResidues(10, 6, 112, 108).rasterCrossovers + " raster crossovers " +
                "would have to be FORCED -- which caDNAno permits and warns 'may lead to folding " +
                "failure'. Of C-0140's five candidate pairs exactly one closes, " +
                closingPairs.joinToString { it.first.toString() + " / " + it.second } + ", and " +
                "the closure verdict is identical at both cross-sections and all four " +
                "(sign, mirror, datum) conventions.")
        findings["SATURATION_WITHDRAWN"] = ("CH-0184's saturating pair -- phase 11 at the 14 bp " +
                "offset, 60 of 60 and 90 of 90 -- is reproduced here and is NOT a buildable " +
                "design: it lives at 112 / 108, which does not close, and the phase that pair " +
                "would need is not the phase the rule determines (2 or 13 depending on which " +
                "near-miss b0 is taken). A six-column placement therefore does not stand.")
        findings["CARDINALITY"] = ("Both sublattices carry three azimuths and one residue each, so " +
                "nothing here asks a parity to justify a count. The check is CH-0151's own and it " +
                "is asserted rather than assumed.")
    }.getOrElse { failure -> findings["PROSE_FAILED"] = failure.toString() }

    val result = T244Result(
        task = "T-244 - the honeycomb face's crossover bond-class residues, and the inter-row ladder offset",
        leaf = "A8.2",
        units = mapOf(
            "length" to "nm", "axialPosition" to "base pairs on one global z",
            "residue" to "base pairs modulo 21", "angle" to "degrees"
        ),
        conventions = mapOf(
            "handedness" to "B-DNA right-handed; viewed from +z the backbone azimuth increases counter-clockwise with z, so +7 bp advances it by +240 = -120 degrees (HoneycombRasterTurnSense's own sentence)",
            "class" to "the neighbour class increases as the azimuth decreases by 120 degrees; class zero is 330 on sublattice A and 150 on B, which are the two ends of ONE bond",
            "residue" to "a bond of class c carries staple crossovers at b0 + 7c and scaffold crossovers at b0 + 7c +- 5, all modulo 21",
            "face" to "the outward normal is +1 or -1 on x; a station is a crossover position on a free azimuth with a positive component along it",
            "offset" to "TwoLengthRaster.stationLattice's own parameterisation - even raster rows carry basePhase, odd rows basePhase + offset",
            "phase" to "measured from the block's own low axial plane, which is stationLattice's convention",
            "properTransformation" to "mirrored == axialReversed; a cross-section reflection alone is improper for a chiral object"
        ),
        parameters = mapOf(
            "samePairPeriodBasePairs" to T244_PERIOD.toString(),
            "anyAzimuthStepBasePairs" to HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP.toString(),
            "scaffoldOffsetBasePairs" to HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP.toString(),
            "candidatePairs" to pairs.joinToString { it.first.toString() + " / " + it.second },
            "crossSections" to sections.joinToString { it.first },
            "phasesSwept" to T244_PERIOD.toString(),
            "offsetsSwept" to "7, 14",
            "primarySource" to "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml (a LITERATURE source, not a result-file input; this study reads no result file at all)"
        ),
        sources = listOf(
            "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml - Douglas et al., Nucleic Acids Res. 37:5001 (caDNAno). READ DIRECTLY: the 21 bp pair period, the 7 bp class step and the +-5 bp scaffold offset are one paragraph of RESULTS AND DISCUSSION, quoted verbatim in HoneycombBondClassResidues.kt"
        ),
        citedInputs = listOf(
            "C-0140 - the honeycomb x-raster path, its turn senses and its level walk, consumed unmodified",
            "C-0141 - the cross-section, the face census and the 21 bp ladder",
            "C-0136 - the admissible row-length residues N = 7d + {0, 10, 11} mod 21, re-derived here as a cross-check",
            "C-0146 / CH-0184 - the 42-cell (phase, offset) census this reproduces before reading anything new"
        ),
        cheapBound = mapOf(
            "whatItSaid" to ("Three numbers out of one paragraph of the caDNAno paper settle the " +
                    "offset before any sweep: a class step is 7 bp, a pair repeats at 21 bp, and " +
                    "the face's two sublattices carry their free azimuth on classes 0 and 1. The " +
                    "offset is therefore ONE class step, 7 bp of residue, and the raster's own " +
                    "row parity decides its sign: 14 bp. The same paragraph's +-5 bp scaffold " +
                    "rule then over-determines the phase, and that is what showed that C-0140's " +
                    "recommended length pair does not close at all."),
            "cost" to "integer arithmetic; no solve, no mesh, no sampling",
            "offsetBasePairs" to properOffsets.toSet().joinToString(),
            "closingPairsOfFive" to closingPairs.size.toString(),
            "determinedPhase" to determinedPhases.joinToString(),
            "stationsAtTheDeterminedPhase" to
                    determined.first { it.crossSection == "10 x 6" }.stationsOnFace.toString()
        ),
        bondClasses = bondClasses,
        closure = closure,
        offsets = offsets,
        determined = determined,
        census = census,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3. This is a LATTICE statement. No folded object is measured; what is read from the source is the design rule, and the arithmetic follows from it.",
            "The +-5 bp scaffold rule is caDNAno's DEFAULT. The paper states that the user may force crossovers between any two scaffold bases, and warns that departure from the default rules 'may lead to folding failure if too much deviation from canonical DNA geometry is implied'. A non-closing raster is therefore buildable and off-rule, not impossible.",
            "The half turn is 5.25 bp at 10.5 bp per turn and caDNAno writes 5. Every residue here inherits that rounding, which is the source's own.",
            "The offset is a difference of two classes and contains no b0, so it survives a raster that does not close. The PHASE does not: where no b0 serves the raster, no phase is determined.",
            "The census counts ONE face. The opposite face carries the same inventory pointing into the grafted layer.",
            "Nothing here re-grades a tile. The flatness consequence of a station count is C-0142's and C-0146's and is not touched."
        ),
        openQuestions = listOf(
            "Does a closing length pair exist that also saturates the station count? The three closing residue classes are (7, 14), (17, 3) and (18, 4) mod 21; only the third is among C-0140's five candidates, and the family has not been swept for the station census.",
            "C-0147/CH-0187 re-open the length-pair selection on four axes; scaffold closure is a fifth, and it excludes 101 / 109 as well as 112 / 108.",
            "Does the scaffold-crossover closure condition have a closed form in the two lengths? The sweep here is exhaustive over residues mod 21 and is not a proof."
        )
    )

    println("  closure: " + closingPairs.size + " of " + pairs.size + " candidate pairs close")
    println("  offset:  " + properOffsets.toSet() + " over " + properOffsets.size + " proper readings")
    println("  phase:   " + determinedPhases + ", stations " + determined.map { it.stationsOnFace })
    falsifiers.forEach { println("  " + it.name + " fired=" + it.fired) }

    val output = File("gpd/results/T-244-face-bond-class-residues.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9) as JsonObject)
        ) + "\n"
    )
    println("T-244 - wrote " + output.path)
}
