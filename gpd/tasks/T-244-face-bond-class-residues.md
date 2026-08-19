# T-244 — read caDNAno's face-sublattice crossover bond-class residues, and settle the inter-row ladder offset

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`CH-0184`](../challenges/CH-0184-the-inter-row-offset-stops-being-free.md), from [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) (`T-235`) |
| **Claim number reserved** | `C-0148` |
| **Challenge numbers reserved** | `CH-0188`, `CH-0189` |
| **Verification type** | **logical** — exact integer arithmetic on the honeycomb lattice, plus a **literature reading** of the design rule from the primary source |

## Formulate

[`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) §9 records the inter-row
ladder offset as a convention this repository *"cannot yet say which"*, adding that **no answer
here depends on the choice**.
`CH-0184` withdraws the scope of that sentence: at `C-0140`'s two-length raster the choice moves
the `10 × 6` face's station count **55 against 50** of 60 at one phase, and exactly **one** of
42 `(phase, offset)` pairs keeps all sixty — phase 11 at the **14 bp** offset.

### Numeric target

Which of **7 or 14 bp** the inter-row ladder offset is, in the row indexing
`TwoLengthRaster.stationLattice` uses (even rows carry `basePhase`, odd rows
`basePhase + offset`), derived from the caDNAno crossover rule read from the primary source
rather than swept — together with the resulting station census on both 60-helix cross-sections.

### Acceptance predicates

- **`P1`** — the crossover rule is **quoted verbatim** from the primary source already in
  `gpd/data/`, with a read-directly / abstract-only / not-found flag, and the bond-class residue
  map is derived from it and from the handedness convention this repository already states.
- **`P2`** — the two face sublattices are shown to carry the **same number** of free azimuths and
  the same number of residues per azimuth, so that a parity is not asked to justify a count
  (`CLAUDE.md`, `CH-0151`).
- **`P3`** — the offset is delivered as a **single value**, and its invariance under the raster's
  own free relabellings (traversal mirror, first axial sign, which face is counted, block
  translation) is measured rather than assumed.
- **`P4`** — the ladder **phase** is examined on the same footing as the offset: either it is
  shown free, or it is determined and the determining rule is stated.
- **`P5`** — the census is re-read at whatever `(phase, offset)` the derivation fixes, on both
  cross-sections, and `CH-0184`'s published table is **reproduced** by the same machinery before
  anything new is read off it.

## Plan

**Cheap bound first, and it is the whole task.** The caDNAno rule is one paragraph, already
fetched (`gpd/data/T-151-sources/PMC2731887-*`); the honeycomb residue map is `residue = b₀ + 7c`
with `c` the neighbour class the repository's `neighbourClassDifference` already defines; and the
census is an integer count over 21 phases. No solve, no mesh, no sampling.

**What would falsify the approach.** If the residue map did not reproduce `C-0136`'s published
row-length rule `N ≡ 7Δ + {0, 10, 11} (mod 21)` term for term, the map would be wrong and nothing
downstream of it could be trusted. If `CH-0184`'s 42-cell table were not reproduced, the census
machinery would be a different object from the one the challenge was written on.

### Falsifiers, declared

- **`F1`** — the derived offset is **not** invariant under the raster mirror, the first axial sign
  or the choice of counted face, i.e. it is a convention after all.
- **`F2`** — the residue map fails to reproduce `C-0136`'s `{0, 10, 11}` length rule.
- **`F3`** — `CH-0184`'s census table is not reproduced.
- **`F4`** — the ladder phase turns out to be free, so `C-0141`'s 21-phase sweep is a sweep over
  buildable designs.
- **`F5`** — `C-0140`'s recommended `112 / 108 bp` raster satisfies the scaffold-crossover rule at
  every one of its raster crossovers.
