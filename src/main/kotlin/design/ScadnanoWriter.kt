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

package com.xemantic.nano.plentyofroom.design

import kotlinx.serialization.json.Json
import java.io.File

/**
 * The scadnano format version this writer declares.
 *
 * It is the version of the file `C-0157`'s oxDNA run was drawn with, and of the reference
 * implementation this repository validates against (`tools/scadnano/validate-sc.py`).
 */
const val SCADNANO_FORMAT_VERSION: String = "0.21.1"

/** How a `.sc` document is rendered: minimal, stable and diffable. */
private val scadnanoJson = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    explicitNulls = false
    encodeDefaults = false
}

/** The design as a scadnano `.sc` document — the exact object [ScadnanoDesign.fromText] parses. */
fun ScadnanoDesign.toScadnanoFile(): ScadnanoFile {
    require(helices.isNotEmpty()) {
        "a design with no helix records cannot be written: a scadnano helix needs a grid " +
            "position, and guessing one lays a honeycomb design out on a square grid — the " +
            "writer refuses it for the same reason the reader refuses to guess a lattice"
    }
    require(helices.size == helixCount) {
        "a design of $helixCount helices carrying ${helices.size} helix records: the helix " +
            "count is what every crossover census in this repository is indexed by, so the two " +
            "cannot differ"
    }
    helices.forEachIndexed { index, helix ->
        require(helix.gridPosition.size == 2) {
            "helix $index carries a grid position of ${helix.gridPosition.size} coordinates; " +
                "scadnano's square and honeycomb grids are both two-dimensional"
        }
    }
    strands.forEach { strand ->
        strand.domains.forEach { domain ->
            require(domain.helix in 0 until helixCount) {
                "a domain on helix ${domain.helix} in a design of $helixCount helices"
            }
        }
    }
    return ScadnanoFile(
        version = version,
        grid = grid,
        geometry = geometry,
        helices = helices,
        strands = strands
    )
}

/**
 * The other half of the interchange boundary: the **writer**.
 *
 * `ScadnanoDesign` reads a `.sc` file and derives the lattice facts this corpus reasons about. A
 * reader alone leaves the asymmetry `ARCHITECTURE.md` records: this programme's recommended tile is
 * a set of Kotlin constants, so it cannot be handed to anybody without a human redrawing it — and a
 * design somebody redraws is not the design that was graded.
 *
 * The gate that makes this an *inverse* of the reader rather than a plausible second implementation
 * is the **round trip**: `read → write → read` must reproduce every derived lattice fact at
 * departure `0.0`, and the second write must be byte-identical to the first. Base-pair counts and
 * helix indices are integers, so no tolerance is admissible on any of them.
 *
 * ## What this writer refuses
 *
 * A grid **position**, exactly as [ScadnanoDesign.lattice] refuses a grid. A design assembled from
 * constants carries no helix records, and inventing them would lay a honeycomb design out on a
 * square grid — the same class of error as inheriting a square-lattice congruence, which is what
 * `C-0141` had to undo. A design whose helix records and helix count disagree is refused for the
 * same reason: the count is what every crossover census is indexed by.
 */
fun ScadnanoDesign.toScadnanoText(): String =
    scadnanoJson.encodeToString(ScadnanoFile.serializer(), toScadnanoFile())

/** Writes the design as a scadnano `.sc` file, creating the parent directory if it does not exist. */
fun ScadnanoDesign.writeTo(file: File): File {
    file.parentFile?.mkdirs()
    file.writeText(toScadnanoText())
    return file
}
