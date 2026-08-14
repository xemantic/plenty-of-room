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

import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.MultiStateSurrogate
import com.xemantic.nano.plentyofroom.structure.GrillageDeflection
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.max

/**
 * `T-129` — is `C-0063`'s flat placement flat over the states a device actually **traverses**?
 *
 * ## What this adds to `C-0063` and `C-0064`
 *
 * `C-0063` swept 1 144 858 placements of `C-0055`'s 34 upward arm roots and found one that dishes
 * **0.0706** of the free-tile stroke with **equal springs** — at **one** of `C-0022`'s solved
 * states. `C-0064` then established that a one-state flatness verdict does not travel, and that
 * the obstruction between states is a **sign** rather than a magnitude: the 2 nm state's free-tile
 * dishing field is anti-parallel to every 10 nm state's, so a distribution that flattens an edge
 * *enhancement* deepens an edge *deficit*. `CH-0077`'s reading is that `C-0022`'s five headline
 * states are **four devices**, and that the requirement is owed over the states **one device**
 * traverses.
 *
 * Two things are needed to put those together and neither exists upstream.
 *
 * 1. **A multi-state influence bank over every candidate root of one phase**, from which the
 *    [MultiStateSurrogate] of *any* 34-root subset follows by slicing. `C-0064`'s
 *    `multiStateSurrogate` builds one surrogate per station set and pays `n + S` lattice solves
 *    for it; a **placement** sweep asks for a surrogate per *placement*, and there are hundreds of
 *    thousands of them. [MultiStateRootBank] pays `sites + S` solves **once** — superposition is
 *    exact for a linear system, so the slice is not an approximation, and the gate that says so is
 *    a comparison against a surrogate built over the subset alone.
 * 2. **The arithmetic that decides which states one device occupies at all.** A device whose layer
 *    rests at `L₀` occupies a gap `g` only by delivering the stroke `L₀ − g`, and `C-0050` bounds
 *    that stroke by the dead load §3 itself names. [strokeToOccupy] and [gapOccupiable] are that
 *    statement in code, so the exclusion of `C-0022`'s 2 nm state from a 10 nm device's range is a
 *    **physical** bound with a falsifier and not a convenience.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly).
 * `x` runs **along** the helices, `y` **across** them, `z` **normal** and positive **upward** —
 * away from the grafted layer, which lies below the tile. `w` is positive **downward**.
 * **Dishing** is the peak absolute departure from the area-weighted best-fit plane, on the same
 * 81 × 81 grid as `C-0026`, `C-0047`, `C-0058`, `C-0063` and `C-0064`. A **state** is a
 * `(concentration, gap, bias)` triple of `C-0022`'s solved profiles; an **operating range** is the
 * set of states **one device** traverses.
 */

// ------------------------------------------------------------------ what a device can occupy

/**
 * The stroke in nm a device whose layer rests at [restingHeight] must deliver to occupy [gap].
 *
 * `s = L₀ − h`, which is `C-0050`'s identity and the reason its whole answer needs no coupling:
 * the stroke is the layer's own compression, so it is bounded above by the resting height itself.
 */
fun strokeToOccupy(restingHeight: Double, gap: Double): Double {
    require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(gap <= restingHeight) {
        "a gap of $gap nm is above the resting height $restingHeight nm, and a layer under bias " +
                "descends: the state is not on this device's path at all"
    }
    return restingHeight - gap
}

/**
 * Whether a device resting at [restingHeight] can occupy [gap] under a stroke ceiling of
 * [deadLoadStroke] — `C-0050`'s dead-load stroke at §3's own 100 pN, which is the largest stroke
 * the layer concedes to the load the problem definition names.
 *
 * This is the test that makes the exclusion of `C-0022`'s 2 nm state from a **10 nm** device's
 * operating range physical: it demands **8 nm** of stroke against a ceiling of 6.013 nm at the
 * 10 nm design point, the largest of `C-0050`'s six layer models. The same test **admits** the
 * 2 nm state for the **5 nm** device, which reaches it at §3's acceptable 3 nm.
 */
fun gapOccupiable(restingHeight: Double, gap: Double, deadLoadStroke: Double): Boolean {
    require(deadLoadStroke >= 0.0) {
        "deadLoadStroke must not be negative, was: $deadLoadStroke"
    }
    if (gap > restingHeight) return false
    return strokeToOccupy(restingHeight, gap) <= deadLoadStroke
}

// ------------------------------------------------------------------ the bank

/**
 * One free solution per load state and one unit-point-load solution per candidate root, sampled
 * once, from which the response of **any** coupling at **any** subset of those roots at **any**
 * subset of those states follows exactly.
 *
 * This is `C-0063`'s [UpwardRootInfluenceBank] carried to several states at once, and it is what
 * makes a placement sweep under a *range* objective cost the same as one under a single-state
 * objective plus one dishing pass per extra state.
 *
 * @param lattice the host, which must carry **no** supports: the coupling is carried here.
 */
class MultiStateRootBank(
    val lattice: OrigamiGrillage,
    val stations: List<Pair<Double, Double>>,
    val states: List<LoadState>,
    val samples: Int = 81
) {

    init {
        require(lattice.supports.isEmpty()) {
            "the bank carries the coupling itself, so the host must be assembled without any " +
                    "supports: it had ${lattice.supports.size}"
        }
        require(stations.isNotEmpty()) { "stations must not be empty" }
        require(states.isNotEmpty()) { "at least one load state is required" }
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
    }

    /** The state labels, in the order every per-state list here uses. */
    val stateNames: List<String> = states.map { it.name }

    private val halfX = lattice.lengthX / 2.0

    private val halfY = lattice.lengthY / 2.0

    private val free: List<GrillageDeflection> = states.map { lattice.solve(it.pressure) }

    private val influence: List<GrillageDeflection> = stations.map { (x, y) ->
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
    }

    private fun sample(dishing: (Double, Double) -> Double): DoubleArray {
        val field = DoubleArray(samples * samples)
        for (i in 0 until samples) {
            val x = -halfX + 2.0 * halfX * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                field[i * samples + j] = dishing(x, y)
            }
        }
        return field
    }

    private val stationFree = Array(states.size) { s ->
        DoubleArray(stations.size) { free[s].deflection(stations[it].first, stations[it].second) }
    }

    private val stationInfluence = Array(stations.size) { j ->
        DoubleArray(stations.size) { k ->
            influence[k].deflection(stations[j].first, stations[j].second)
        }
    }

    private val dishingFree = Array(states.size) { s -> sample { x, y -> free[s].dishing(x, y) } }

    private val dishingInfluence = Array(stations.size) { k ->
        sample { x, y -> influence[k].dishing(x, y) }
    }

    /** The peak dishing in nm of the host under [state]'s load alone — the *no coupling* bar. */
    fun freePeakDishing(state: Int): Double {
        require(state in states.indices) {
            "state must be within 0 until ${states.size}, was: $state"
        }
        var peak = 0.0
        for (value in dishingFree[state]) peak = max(peak, abs(value))
        return peak
    }

    /** `C-0064`'s multi-state surrogate over the subset of stations at [indices], in that order. */
    fun surrogateFor(indices: List<Int>): MultiStateSurrogate {
        require(indices.isNotEmpty()) { "indices must not be empty" }
        require(indices.all { it in stations.indices }) {
            "every index must name a station of this bank, were: $indices"
        }
        require(indices.distinct().size == indices.size) { "the indices must be distinct" }
        return MultiStateSurrogate(
            grid = indices.map { stations[it] },
            samples = samples,
            stateNames = stateNames,
            stationInfluence = Array(indices.size) { j ->
                DoubleArray(indices.size) { k -> stationInfluence[indices[j]][indices[k]] }
            },
            dishingInfluence = Array(indices.size) { dishingInfluence[indices[it]] },
            stationFree = Array(states.size) { s ->
                DoubleArray(indices.size) { stationFree[s][indices[it]] }
            },
            dishingFree = dishingFree
        )
    }

    /** The index of the station at ([x], [y]), or `−1`. */
    fun indexOf(x: Double, y: Double, tolerance: Double = 1e-9): Int =
        stations.indexOfFirst { abs(it.first - x) <= tolerance && abs(it.second - y) <= tolerance }

}
