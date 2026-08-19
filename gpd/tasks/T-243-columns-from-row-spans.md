# T-243 — derive the crossover-column count from the ROW spans, not from `edgeX`

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`CH-0185`](../challenges/CH-0185-a-bounding-box-crossover-column.md), from [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) (`T-235`) |
| **Claim number reserved** | `C-0148` |
| **Challenge numbers reserved** | `CH-0188`, `CH-0189` |
| **Verification type** | **logical** (integer axial windows and one division) **+ in-silico** (the uncoupled four-layer dishing at the three width/count readings, as a reproduction gate) |

## Formulate

Every four-layer honeycomb study in this corpus takes its crossover-column count as
`floor((edgeX − 2·EDGE_MARGIN)/pitch) + 1` with `edgeX` the block's **bounding box**.
At `C-0140`'s buildable extent, 116 bp = 39.44 nm, that clears eleven honeycomb pitches by
**0.07 nm** — one fifth of a base-pair rise — so the standing 0.05 nm guard admits a **twelfth**
column and half a rise refuses it, worth **6 flat cells of 8 against 3**.

`C-0146` observes that **every x-raster row of that block is 112 bp**, and that the 116 bp extent
is a 4 bp inter-row **stagger**. A crossover column serves an **interface between two rows**, so
its inventory belongs to a row window and not to a bounding box.

### Numeric target

The crossover-column count of the `10 × 6` and `15 × 4` four-layer honeycomb blocks derived from
the **axial windows** `C-0140`'s level walk gives each helix, swept over the three `EDGE_MARGIN`
conventions (0.05 nm, half a rise, one rise) — and the slack `(window − 2m) mod pitch` quoted
beside it, which is what decides whether the guard is inert.

### Acceptance predicates

- **`P1`** — the column count is derived from **windows the raster produces**, at three stated
  readings (row span, interface = intersection of two adjacent row spans, and the strictest
  all-helix intersection), never from `edgeX` alone.
- **`P2`** — the guard is **swept** at all three conventions at every reading, and the verdict
  *inert / decisive* is emitted per reading rather than asserted.
- **`P3`** — the bounding-box reading is reproduced at `12 / 11 / 11`, so that the new derivation
  and `C-0146`'s published table are demonstrably the same arithmetic on different windows.
- **`P4`** — the flatness consequence is stated by **selecting among cells `C-0146` already
  graded**, and the uncoupled dishing at the selected geometry is **re-solved** here and
  reproduced against `C-0146`'s emitted value.
- **`P5`** — `EDGE_MARGIN`'s KDoc is replaced by the condition under which it is inert, expressed
  in the quantity that decides it, rather than by a sentence about a 40 nm tile.

## Plan

**Cheap bound first.** The windows are integers `C-0140`'s level walk already emits; the count is
one floor division per reading per guard. That is the whole of `P1`–`P3` and it runs in
microseconds. Only `P4` needs a solve, and it is a single linear plate solve per geometry — no
Monte Carlo, because the dropout grading is `C-0146`'s and is re-read rather than re-run.

**What would falsify the approach.** If the row-derived and interface-derived counts disagreed
with each other, or if either moved across the three guard conventions, then the row reading
would be as convention-bound as the box reading and the objection would not be settled.

### Falsifiers, declared

- **`F1`** — a row-derived column count **moves** across the three `EDGE_MARGIN` conventions, so
  the guard is decisive there too.
- **`F2`** — the row-span and interface readings disagree.
- **`F3`** — the bounding-box reading does not reproduce `C-0146`'s `12 / 11 / 11`.
- **`F4`** — the uncoupled dishing re-solved here does not reproduce `C-0146`'s emitted value at
  the same geometry.
