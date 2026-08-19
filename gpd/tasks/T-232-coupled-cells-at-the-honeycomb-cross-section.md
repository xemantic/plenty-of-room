# T-232 — re-grade `C-0118`'s sixteen coupled cells at the corrected honeycomb cross-section

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) / [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md) |
| **Claim number reserved** | `C-0142` |
| **Challenge numbers reserved** | `CH-0176`, `CH-0177` |
| **Verification type** | **in-silico** (influence surrogate over the grillage, Monte Carlo dropout on one common stream) **+ logical** (the lattice availability of each cell's placement) |

## Formulate

`C-0118` is the only coupled tile in this programme that clears `T-5b`'s 0.10 at the 90th
percentile under the measured folding statistics, and it is the standing recommendation.
`C-0141` has since shown that the cross-section it was graded on **is not a honeycomb**: a
honeycomb spends `3√3/4 · d² = 8.35449857 nm²` of plan per helix against the model's `d² =
6.431296` (**1.29903811×**), because the in-plane row pitch is `3d/2` and the layer pitch
`d√3/2` and only their **product** is the cell. Every four-layer `edgeY` in this corpus is
therefore exactly **1.5×** too small.

On the corrected geometry the **uncoupled** consequence is already measured (`C-0141` §7):

| | standing free-tile dishing | honeycomb free-tile dishing | `f*` | at the band low `f = 0.26` |
|---|---|---|---|---|
| `15 × 4` | 0.0577199433 | **0.0978155002** | **0.276970522** | **0.101759944 — NOT flat** |
| `10 × 6` | 0.00874363524 | **0.0240648102** | 0.012737738 | 0.0255589305 — flat |

**The coupled, dropout-graded consequence is not measured**, and that is this task.

### Numeric target

The same 16-cell grading — **two cross-sections × four path counts × two distributions**, at
`C-0017`'s mandated total, under `C-0087`'s measured per-site incorporation, **4 000
realisations on one common stream** — re-run with `edgeY`, the in-plane duplex pitch and the
layer spacing at their honeycomb values, and the 90th-percentile verdicts re-stated.

### Acceptance predicates

- **`P1`** — every one of the 16 cells is re-graded at the corrected geometry and reported at
  its 90th percentile, with the exceedance's **one-sided** bound wherever that proportion is
  saturated (`C-0129`: `√(p̂(1−p̂)/n)` is identically zero at `p̂ ∈ {0, 1}` and is the
  resolution of nothing).
- **`P2`** — the standing geometry is re-run in the same process and reproduces `C-0118`'s own
  16 cells, so the movement is the geometry and nothing else; and the two are compared **per
  realisation** on the common stream, never between two summaries.
- **`P3`** — the **uncoupled** tile is reported as a reference at every cross-section and at
  both ends of the measured composite-fraction band, because `C-0116`'s threshold for `15 × 4`
  now sits **inside** 0.26–0.33 and a single-`f` verdict is therefore not a verdict.
- **`P4`** — for every cell, whether its placement is **available on the honeycomb station
  lattice** `C-0141` derives, or whether it is an abstract grid the lattice does not carry.

### Conventions, locked

T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb bond length `d` = 2.536 nm
(SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; 21 bp per
interface; rows of 112 bp (`C-0119`), rise 0.34 nm/bp. `C-0022`'s solved collar at 2 mM /
10 nm / 0.192 V. `C-0001`'s secant foundation. `C-0017`'s mandate, §3's **acceptable** clause,
100 pN / 3 nm = 33.3333 pN/nm, an **equality on the SUM**. `T-5b`'s 0.10. Seed 197197, 4 000
realisations, 81 × 81 dishing grid, 2 beam subdivisions. `x` runs along the helices, `y`
across them, origin at the tile centre.

## Plan

### The cheap bounds, which run before any Monte Carlo

1. **The geometry arithmetic** — `edgeY`, the two pitches, the attachment pitch across the
   helices (`edgeY/rasterRows` is the in-plane pitch **identically**, so it moves 1.5×), and
   the pitch-over-reach ratios. One multiplication each.
2. **The uncoupled reference at both ends of the measured band.** `C-0141` already reports
   `15 × 4` failing at `f = 0.26`. Since `C-0109` finds every coupled cell *worse* than the
   uncoupled tile on `15 × 4`, a failing reference **predicts** failing cells there, and the
   run is then a confirmation rather than a discovery. On `10 × 6` the reference moves
   2.75× (0.00874363524 → 0.0240648102) while `C-0118`'s cells sit at 0.0278–0.0623, so a
   naive proportional transfer lands them at 0.076–0.171 — **straddling the tolerance**. That
   is the reading that says the run is necessary.
3. **The station inventory.** A 112 bp row carries `⌊112/21⌋ + 1 = 6` stations per rooting
   helix on the 21 bp ladder, so `15 × 4` offers 90 and `10 × 6` offers 60. Every requested
   path count (15/30/45/75 and 10/20/30/50) is inside its inventory, so no cell fails on
   **count**; whether it is available on **position** is `P4` and is an integer question.

### Method, and its justification against cost

The machinery is unchanged. `C-0141` lifted `edgeY`, the in-plane pitch and the layer spacing
out of `C-0120`'s construction as parameters (`T219Tile`), and `HoneycombFaceLattice` already
carries `plateEdgeY`, `rowPitch`, `columnPitch` and `honeycombStationLattice`. **Re-deriving
the geometry is how two claims end up disagreeing**, so this task consumes those and adds
nothing geometric of its own.

What is new is small and testable: the **lattice-snapped** column selection (which ladder
positions a `columns`-wide grid would occupy), and a **paired per-realisation** comparator.
`CLAUDE.md`: *"a ratio of two ORDER STATISTICS is not the order statistic of the ratio, and
here it is 5× too big"* — so the geometry's cost is read as the **median of the
per-realisation ratio** on the shared stream, not as a ratio of two 90th percentiles.

Cost: 32 cell gradings (16 corrected + 16 standing) at 4 000 realisations each, plus a
lattice-snapped set. Each is one surrogate build and 4 000 dense solves of order the path
count. `C-0118` ran 16 in one study, so this is ~2–3× that. A **smoke run at 300
realisations** goes first, for the prose paths, the argmin and the serialisation only.

### What would falsify this approach

- **`F1`** — the standing-geometry cells do **not** reproduce `C-0118`'s sixteen numbers, in
  which case nothing here is a re-grading of that claim and no comparison is licensed.
- **`F2`** — the corrected `15 × 4` uncoupled reference does not reproduce `C-0141`'s
  0.0978155002 and the corrected `10 × 6` does not reproduce 0.0240648102, in which case the
  geometry has not been transferred.
- **`F3`** — a cell's 90th percentile is **below** its own nominal (zero-defect) dishing at the
  same geometry, which a removal-only perturbation on a positive-semi-definite coupling should
  not produce and which would indicate the ensemble is not being applied.
- **`F4`** — the median per-realisation ratio between the two geometries has the **opposite
  sign** to the ratio of the 90th percentiles, in which case the summary statistic this claim
  quotes is not describing the sample it was taken from.
- **`F5`** — `10 × 6` loses its flatness at the corrected geometry at the measured `f = 0.30`,
  in which case the cross-section recommendation `C-0120`/`C-0141` build does **not** survive
  the correction that produced it, and the programme has no flat coupled tile at all.

`F5` is the one this task exists to test. It is declared as a falsifier rather than an
expectation because the cheap bound's proportional transfer straddles the tolerance.
