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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy

/**
 * The elastic constants of one B-form DNA duplex, in the locked units.
 *
 * @param bendingRigidity `EI` in `pN·nm²`, equal to `L_p k_BT` for a persistence length `L_p`.
 * @param torsionalRigidity `GJ` in `pN·nm²`, equal to `L_t k_BT` for a torsional persistence length `L_t`.
 * @param stretchModulus `S` in `pN`.
 */
data class DnaDuplex(
    val bendingRigidity: Double,
    val torsionalRigidity: Double,
    val stretchModulus: Double
) {

    init {
        require(bendingRigidity > 0.0) {
            "bendingRigidity must be positive, was: $bendingRigidity"
        }
        require(torsionalRigidity > 0.0) {
            "torsionalRigidity must be positive, was: $torsionalRigidity"
        }
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    }

}

/**
 * Returns the duplex whose bending and torsional rigidities follow from the
 * persistence lengths [bendingPersistenceLength] and [torsionalPersistenceLength] in nm,
 * via `EI = L_p k_BT`.
 *
 * This is the only place the two are related, so the definition of a persistence length
 * is asserted once rather than restated at every use.
 */
fun duplexOfPersistenceLengths(
    bendingPersistenceLength: Double,
    torsionalPersistenceLength: Double,
    stretchModulus: Double,
    temperature: Double = ROOM_TEMPERATURE
): DnaDuplex {
    val energy = thermalEnergy(temperature)
    return DnaDuplex(
        bendingRigidity = bendingPersistenceLength * energy,
        torsionalRigidity = torsionalPersistenceLength * energy,
        stretchModulus = stretchModulus
    )
}

/**
 * How the layers of a multi-layer bundle are taken to be coupled in bending.
 *
 * The distinction is worth two orders of magnitude in the flexural rigidity, and it is not
 * settled by anything in this task, so both are carried and reported as bounds.
 */
enum class InterlayerCoupling {

    /** Layers bend independently and simply add — the lower bound, `D ∝ n`. */
    NONE,

    /**
     * Layers are rigidly coupled and the parallel-axis theorem applies to the duplex
     * stretch modulus — the upper bound, `D ∝ n³` for large `n`.
     */
    RIGID
}

/**
 * A DNA-origami sheet built from parallel duplexes joined by crossovers,
 * reduced to the rigidities an orthotropic Kirchhoff plate needs.
 *
 * ## Geometry and sign conventions
 *
 * - `x` runs **along** the helices, `y` **across** them, `z` normal to the sheet.
 * - The duplexes are parallel to `x`, spaced [interhelicalDistance] apart in `y`.
 * - Crossovers joining one adjacent pair of helices recur every [crossoverSpacing] along `x`.
 *   That is the **per-interface** spacing, which for a Rothemund single-layer sheet is 32 bp
 *   even though each helix carries a crossover every 16 bp: they alternate between its two
 *   neighbours. Conflating the two halves the rigidity.
 * - Deflection `w` is measured **downward**, toward the electrode, positive when the polymer
 *   layer is compressed.
 *
 * ## The two rigidities, and why they differ
 *
 * Along the helices the sheet is a set of parallel beams: each duplex contributes its own
 * `EI`, and the flexural rigidity per unit width is `EI / interhelicalDistance`.
 *
 * Across the helices no duplex bends at all. The sheet articulates at the crossovers,
 * which act as hinges of stiffness [crossoverHingeStiffness]. A moment `M` per unit width is
 * carried by `1/crossoverSpacing` crossovers per unit length, so each carries
 * `M · crossoverSpacing` and turns through `M · crossoverSpacing / k_θ`; that rotation is
 * spread over one interhelical distance, giving `D_⊥ = k_θ · d / p`.
 *
 * **The compliance across the helices is therefore entirely joint compliance** — which is what
 * leaf `A8.2` asks to be identified — and no duplex elasticity enters it at all.
 *
 * @param interfaceTwistStiffness an optional second hinge compliance **in series** with the
 *          crossovers, in `pN·nm/rad per nm` of interface length: the twisting of the
 *          inter-crossover duplex segments that the crossover geometry forces as the sheet
 *          rolls. Infinite by default, which makes `D_⊥` an upper bound.
 * @param layers the number of stacked duplex layers; 1 for the single-layer sheet of §3.
 * @param layerSpacing the centre-to-centre spacing of the layers in `z`, in nm.
 * @param duplexDiameter the duplex diameter in nm, which sets the thickness of a single layer.
 */
data class OrigamiSheet(
    val duplex: DnaDuplex,
    val interhelicalDistance: Double,
    val crossoverSpacing: Double,
    val crossoverHingeStiffness: Double,
    val interfaceTwistStiffness: Double = Double.POSITIVE_INFINITY,
    val layers: Int = 1,
    val layerSpacing: Double = interhelicalDistance,
    val duplexDiameter: Double = DUPLEX_DIAMETER,
    val interlayerCoupling: InterlayerCoupling = InterlayerCoupling.NONE
) {

    init {
        require(interhelicalDistance > 0.0) {
            "interhelicalDistance must be positive, was: $interhelicalDistance"
        }
        require(crossoverSpacing > 0.0) {
            "crossoverSpacing must be positive, was: $crossoverSpacing"
        }
        require(crossoverHingeStiffness > 0.0) {
            "crossoverHingeStiffness must be positive, was: $crossoverHingeStiffness"
        }
        require(interfaceTwistStiffness > 0.0) {
            "interfaceTwistStiffness must be positive, was: $interfaceTwistStiffness"
        }
        require(layers >= 1) { "layers must be at least 1, was: $layers" }
        require(layerSpacing > 0.0) { "layerSpacing must be positive, was: $layerSpacing" }
        require(duplexDiameter > 0.0) { "duplexDiameter must be positive, was: $duplexDiameter" }
    }

    /** The geometric thickness of the sheet in nm. */
    val thickness: Double get() = (layers - 1) * layerSpacing + duplexDiameter

    /** The number of duplexes crossing one nm of a cut perpendicular to the helices. */
    val duplexLinearDensity: Double get() = layers / interhelicalDistance

    /** The number of crossovers on one nm of a cut parallel to the helices, all layers. */
    val crossoverLinearDensity: Double get() = layers / crossoverSpacing

    /** The offsets of the layer mid-planes from the sheet mid-plane, in nm. */
    private val layerOffsets: List<Double>
        get() = (0 until layers).map { (it - (layers - 1) / 2.0) * layerSpacing }

    /** `D_∥` in `pN·nm` — the flexural rigidity for bending **along** the helices. */
    val alongHelixRigidity: Double
        get() {
            val independent = layers * duplex.bendingRigidity
            val parallelAxis = when (interlayerCoupling) {
                InterlayerCoupling.NONE -> 0.0
                InterlayerCoupling.RIGID ->
                    duplex.stretchModulus * layerOffsets.sumOf { it * it }
            }
            return (independent + parallelAxis) / interhelicalDistance
        }

    /**
     * The rotational stiffness of one helix-helix interface, per nm of its length,
     * in `pN·nm/rad per nm` — crossover hinges and inter-crossover duplex twist in series.
     */
    val interfaceHingeStiffness: Double
        get() = 1.0 / (crossoverSpacing / crossoverHingeStiffness + 1.0 / interfaceTwistStiffness)

    /**
     * `D_⊥` in `pN·nm` — the flexural rigidity for bending **across** the helices,
     * carried entirely by joint compliance.
     *
     * Multi-layer coupling is deliberately **not** applied here: it would need an
     * across-helix axial stiffness per unit width, which is a crossover property nothing in
     * this task determines. The value is therefore a lower bound for `layers > 1`.
     */
    val acrossHelixRigidity: Double
        get() = layers * interfaceHingeStiffness * interhelicalDistance

    /**
     * `D_k` in `pN·nm`, the twisting rigidity of the Huber orthotropic plate.
     *
     * Contributed by torsion of the duplexes about their own axes. The across-helix
     * counterpart is a crossover torsion nothing here determines and is taken as zero,
     * which makes this a lower bound.
     */
    val twistingRigidity: Double
        get() = layers * duplex.torsionalRigidity / (4.0 * interhelicalDistance)

    /** The number of crossovers, all layers, on a cut of length [cutLength] parallel to the helices. */
    fun crossoversOnCut(cutLength: Double): Double = crossoverLinearDensity * cutLength

    /** The number of duplexes on a cut of length [cutLength] perpendicular to the helices. */
    fun duplexesOnCut(cutLength: Double): Double = duplexLinearDensity * cutLength

    /** Returns this sheet as an orthotropic plate of footprint [lengthX] × [lengthY] nm. */
    fun plate(lengthX: Double, lengthY: Double): OrthotropicPlate = OrthotropicPlate(
        lengthX = lengthX,
        lengthY = lengthY,
        rigidityX = alongHelixRigidity,
        rigidityY = acrossHelixRigidity,
        twistingRigidity = twistingRigidity,
        couplingRigidity = 0.0
    )

    companion object {

        /** The B-DNA duplex diameter in nm. */
        const val DUPLEX_DIAMETER: Double = 2.0
    }

}
