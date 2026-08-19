# T-235 — re-grade the corrected `10 × 6` coupled cells at `C-0140`'s two-length raster

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) §8, *"the ROW LENGTH is carried unchanged at 112 bp, and that is a fourth moved input this claim does NOT move"* |
| **Claim number reserved** | `C-0146` |
| **Challenge numbers reserved** | `CH-0184`, `CH-0185` |
| **Verification type** | **logical** (an exact integer lattice construction of the two-length raster's axial levels, station windows and station census) **+ in-silico** (the same influence surrogate and Monte Carlo dropout grading `C-0142` runs, on one common stream) |

## Formulate

`C-0142` re-graded `C-0118`'s sixteen coupled cells at `C-0141`'s corrected honeycomb
cross-section and found **four flat, all on `10 × 6`**, with `15 × 4` **0 of 8** at both ends of
the measured coupling band.
It carried `edgeX` at **112 bp = 38.08 nm** unchanged, **deliberately**, so that the movement it
reported was attributable to the cross-section and nothing else — and it named the row length as
the fourth moved input it does not move.

`C-0140` is what moved it.
A honeycomb x-raster carries **both** turn senses, so **no uniform row length exists at all**;
its recommendation is a two-length raster at **112 / 108 bp**, whose axial extent is
**116 bp = 39.44 nm**, `+3.57 %` on the width `C-0142` graded.

The margin at risk is small and the binding cells are close:
`10 × 6` at 3 columns rim-graded is **0.0954158305** and at 5 columns equal springs
**0.0900369**, against `T-5b`'s **0.10**.
`C-0142` says in as many words that this is *"not obviously free"*.

And the row length is not only a constant.
A two-length raster makes the 21 bp station ladder **row-dependent**: each rooting helix carries
its own length and its own axial window, so `C-0141`'s single-`rowBasePairs`
`honeycombStationLattice` is no longer the lattice.

### Numeric target

`C-0142`'s eight `10 × 6` cells — four path counts × two distributions, at `C-0017`'s mandated
total, under `C-0087`'s measured per-site incorporation, **4 000 realisations on one common
stream** — re-read at the two-length raster, at **both** ends of the measured composite-fraction
band, on the **abstract grid** and on the **two-length station lattice**, with enough of the
`15 × 4` cells to see the direction, and the 90th-percentile verdicts restated.

### Acceptance predicates

- **`P1`** — the two-length raster's axial geometry is **derived** from `C-0140`'s own path,
  turn-sense and level machinery rather than re-derived: per raster row, its face helix's
  effective sense, its row length and its axial window; and the block's own extent.
- **`P2`** — every `10 × 6` cell is re-graded at the two-length width and reported at its 90th
  percentile, with `C-0129`'s **one-sided** bound wherever the exceedance is saturated, and the
  **uncoupled** tile reported as the reference at every geometry and at both ends of the band.
- **`P3`** — the width `C-0142` graded is re-run **in the same process** and reproduces its own
  published cells, so the movement is the row length and nothing else; and the two are compared
  **per realisation** on the common stream, never between two summaries.
- **`P4`** — the station census of the two-length raster is delivered at **both** admissible
  inter-row offsets and **all 21** ladder phases, and every graded placement is stated to be
  available on it or refused by it.
- **`P5`** — the crossover-column count the grillage derives from `edgeX` is swept over
  `CrossoverLayout.EDGE_MARGIN`, because a `+3.57 %` width lands the count within **0.07 nm** of
  the guard (`CLAUDE.md`: *"a numerical guard becomes a physical assertion the moment the lattice
  lands on it"*).

### Conventions, locked

T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm.
Honeycomb bond length `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch
`d√3/2` = 2.19624042 nm; 21 bp per interface; rise 0.34 nm/bp.
Two-length raster **112 bp at effective sense 1, 108 bp at effective sense 2** (`C-0140`: 112 is
residue 7 of the sense-1 triple `{7, 17, 18}`, 108 is residue 3 of the sense-2 triple
`{3, 4, 14}`), first axial sign `+1`, unmirrored.
`C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V. `C-0001`'s secant foundation.
`C-0017`'s mandate, §3's **acceptable** clause, 100 pN / 3 nm = 33.3333 pN/nm, an **equality on
the SUM**. `T-5b`'s 0.10. Seed 197197, 4 000 realisations, 81 × 81 dishing grid, 2 beam
subdivisions.
`x` runs **along** the helices, `y` **across** them, origin at the tile centre.
Axial positions in **base pairs on one global `z`**, per `C-0140`'s own convention that all
helices are parallel to a global `z` and every crossover position is absolute.

## Plan

### The cheap bound, first, and it is exact integer arithmetic

`C-0140`'s `honeycombXRasterPath`, `honeycombRasterTurns` and its level walk are already in the
tree and are exact.
Walking them over `10 × 6` and `15 × 4` at 112 / 108 bp costs microseconds and settles four
things before any Monte Carlo:

1. **which** rooting helix carries which length, and whether it alternates with the row parity —
   which decides whether the station lattice is still a two-phase object;
2. the **block's** axial extent against each **raster row's** own span, which decides what
   `edgeX` a smeared plate is owed;
3. the **station census** per row at both offsets and all 21 phases, which decides whether the
   graded column counts are still realisable;
4. the **crossover-column count** at the wider `edgeX`, and its distance to `EDGE_MARGIN`.

If (2) says every raster row is still 112 bp, the width question splits into two readings and the
row-faithful one must reproduce `C-0142` **exactly**, which is a far stronger check than a
re-grade.

### The grading

Reuse `tile/HoneycombCoupledTile.kt`, `tile/HoneycombFaceLattice.kt` and the coupling package
unmodified; the only new model is the two-length raster's own axial geometry and the station
lattice that follows from it. Its limiting case is exact: **equal lengths must return
`honeycombStationLattice`**, position for position.

Grade on **one** common dropout stream, restricted per cell, and read the width's cost **per
realisation** — `C-0142` measured a 14.2 % gap between that and a ratio of two order statistics
on this very grading.

### What would falsify this approach

- **`F1`** — the 112 bp reading does not reproduce `C-0142`'s eight published `10 × 6` cells, in
  which case nothing here is a re-reading of that claim.
- **`F2`** — the two-length lattice does not reduce to `C-0141`'s `honeycombStationLattice` when
  both lengths are equal, in which case the generalisation is not one.
- **`F3`** — the recommended design loses `T-5b` at the two-length raster, at either end of the
  measured band, in which case `C-0142`'s recommendation does not survive its own buildable
  width.
- **`F4`** — some graded column count is **refused** by the two-length station lattice, in which
  case the two-length raster costs a path count and not only a width.
- **`F5`** — the crossover-column verdict moves with `EDGE_MARGIN`, in which case a numerical
  guard is deciding a flatness reading.
- **`F6`** — the equal-spring advantage `C-0142` measured on the lattice-snapped cells does not
  persist, in which case a distribution rule read at one width does not transfer.
- **`F7`** — the paired and unpaired readings of the width's cost disagree in **sign**.

`F3`, `F5` and `F6` are declared **open**: nothing here predicts them.
