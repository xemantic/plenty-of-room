# `T-258` — the ragged face at the relief the DRAWABLE raster carries

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0151`](../claims/C-0151-closing-raster-selection.md) §9 item 2 |
| **Verification type** | **logical** (exact integer lattice arithmetic on the rise and on the honeycomb cross-section, plus three closed forms already in the corpus — the plate ripple transfer function, the square wave's fundamental, and the slit's transverse eigenvalue) |
| **Units** | lengths **nm**, rise **0.34 nm/bp**, rigidities **pN·nm**, stiffness **pN/nm**, `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂ |

---

## Formulate

[`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) (`T-231`) priced the
front/rear surface relief of a two-length honeycomb raster on two channels, and it priced it at
**4 bp = 1.36 nm** front and **8 bp = 2.72 nm** rear, because that is what
[`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md)'s `112 / 108` recommendation carries.

[`C-0151`](../claims/C-0151-closing-raster-selection.md) then showed that `112 / 108` **does not
close** on caDNAno's published `±5 bp` scaffold rule, that every drawable two-length honeycomb
raster has `L₁ − L₂ ≡ 14 (mod 21)`, and that the recommended drawable pair is **`102 / 109`** — a
**7 bp** stagger. Its relief is therefore **7 bp = 2.38 nm** front and **14 bp = 4.76 nm** rear,
**1.75×** what `C-0147` priced.

`C-0147`'s verdict is quoted at 4 bp. **The margin is ample by inspection and what is owed is the
number** — and one of the two channels is a statement about an *axis*, which does not scale at
all, while the other is a *bound*, which scales linearly with the relief. Those are different
kinds of statement and re-running them at 7 / 14 bp is what says so.

**Numeric target.** The two channels of `C-0147` re-read at the drawable raster's own relief:

1. the coefficient of the raggedness on §3's gap-facing flatness field, which `C-0147` proved is
   **exactly zero**, *verified at 7 / 14 bp rather than assumed to carry over*; and
2. the residual rim-modulation bound — a number, in fractions of the free stroke — together with
   its margin against the headroom the **current** flatness state leaves.

**Acceptance predicates.**

- `P1` — the relief is **re-derived** from the raster machinery at both pairs rather than
  transcribed: the model reproduces `C-0147`'s **4 and 8 bp** at `112 / 108` at departure `0.0`
  and emits the drawable pair's own spreads, at **both** 60-helix cross-sections and at both
  assignments of the two lengths to the two turn senses.
- `P2` — the **axis** verdict is re-taken at the drawable relief, with the premise it rests on
  stated as a **testable** proposition about the block's coordinates and not as an inherited
  sentence: the gap-facing surface is one *column* of the cross-section and the relief is an
  *axial* extent, so the two are orthogonal. The test is that the set of helices whose end level
  differs is disjoint from no column at all — i.e. that the raggedness is a property of the
  **rim** at every column, gap-facing or buried — and that it therefore cannot enter `w(x, y)`.
- `P3` — the rim-modulation bound is emitted with **every** factor it is a product of (the
  across-helix rigidity, the Winkler bending length, the modulation wavelength, the ripple
  transfer, `CLAUDE.md`'s 50× free-edge penalty, the rim lever) at both cross-sections and at both
  rasters, so the 1.75× is visible as a **linear** scaling of exactly one factor rather than
  asserted.
- `P4` — the **threshold** the bound is compared against is read from the corpus's *current*
  flatness state and named with the state it is read at. `C-0147` compared against `C-0142`'s
  tightest surviving coupled cell; [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md)
  has since re-graded every coupled cell on the honeycomb grillage and finds **`0` of `64`**
  inside `T-5b`, so that comparand no longer exists. Where a comparand has been withdrawn the
  task must say so and quote the bound against every reading that does exist — the **uncoupled**
  four-layer honeycomb tile and the zero-defect recommended cell — rather than silently reusing
  the withdrawn one.
- `P5` — the edge-field channel is re-run: the relief against the slit's transverse decay length
  over §3's three gaps and three buffers, at the drawable relief.
- `P6` — every length is quoted in **rises** as well as nm, and any margin below one rise is
  reported as **not quotable** (`CLAUDE.md`).

**Geometry and sign conventions, fixed before deriving.**

- The tile is a four-layer honeycomb block: helices parallel to `x`; `m` raster rows at the
  **in-plane** pitch `3d/2 = 3.804 nm`; `n` helices per row at the **layer** pitch
  `d√3/2 = 2.19624042 nm`; `d = 2.536 nm` (SAXS, `Gen1Tile.INTERHELICAL_HONEYCOMB`). Both 60-helix
  cross-sections are carried, `15 × 4` and `10 × 6`.
- §3's **gap** is along `z`, the tile normal. The gap-facing surface is one **column** of the
  cross-section — a row of parallel duplex sidewalls. `T-5b`'s flatness is a deflection field
  `w(x, y)` normal to it.
- The two **ragged faces** are the planes `x = 0` and `x = L`, the tile's axial **rim**, where the
  helices terminate. A face's **raggedness** is `max − min` of its own helix end levels, in base
  pairs, exactly as `C-0140` and `C-0147` emit it.
- A **relief** is a difference of two axial extents and is therefore a base-pair **count**; it is
  quoted in rises and in nm, never as a residue.
- The rim modulation is a **square wave** in the raster-row index, entered into a transfer
  function written for a sinusoid through its **fundamental**, `2A/π` for a peak-to-peak `A`.

---

## Plan

**The cheap bound is a question about an axis, and it runs before any arithmetic — but it is not
inherited, it is re-taken.** `C-0147`'s zero is not a small number, it is a statement that the
relief and the flatness field live on orthogonal coordinates: the relief changes where a helix
*ends*, an `x` coordinate; the flatness reads `w`, a `z` coordinate. That statement carries no
magnitude, so a 1.75× relief cannot move it — *provided* the premise is still true, and the
premise is that no amount of relief moves material off the gap-facing **column**. Enumerating the
block's helices by column and by end level tests exactly that, and it costs one pass.

**Where the argument WOULD fail is worth naming, because that is what makes it a test rather than
a restatement.** It fails if the relief ever reached a scale at which a helix left the block —
i.e. if the relief approached the block extent — or if the two-length assignment ever put the
short helices in one column rather than spread over all of them. Both are checkable and both are
false here, and the second is the interesting one: `C-0147` found the *identical* 4 / 8 spread on
`15 × 4` and `10 × 6`, which says the raggedness is a property of the **turn-sense alternation**
and not of the block's shape. Re-run at 7 / 14 bp, the same test says whether that survives.

**Then the one channel that does scale.** The residual bound is the product

> `bound = (2·relief/π) / (edgeX/2) × 1/(1 + (2πℓ/λ)⁴) × 50`

of a **rim lever** (linear in the relief), a **ripple transfer** (a function of the modulation
wavelength `λ` and the across-helix Winkler bending length `ℓ`, neither of which contains the
relief) and `CLAUDE.md`'s measured **50× free-edge penalty** on an infinite-plate transfer
function. So the whole re-run is one factor, and emitting the others beside it is what proves it.
`λ` is **2 raster rows = 7.608 nm**, a *period* set by the turn-sense alternation; that it does
**not** move with the relief is a declared falsifier rather than an assumption.

**Cost.** Both channels are closed forms already in `structure/HoneycombTurnLoop.kt`,
`structure/OrigamiSheet.kt` and `coupling/`; there is no mesh, no sampling and no solve. The
expensive alternative — a per-row row length in a lattice model — **does not exist in this
repository** (`C-0147` *Still open* item 1: `OrigamiGrillage`, `HoneycombCoupledTile` and
`HoneycombGrillage` all take a single `edgeX`/`rowBasePairs`), so the flatness cost is **bounded**
and not measured, and the bound carries the 50× penalty for exactly that reason. Building a
per-helix row length to measure a quantity already bounded at three orders of magnitude below its
threshold would be the wrong spend, and saying so is the cost justification.

**What result would falsify this approach.**

| # | falsifier | if it fires |
|---|---|---|
| `F1` | the drawable relief moves material off the **gap-facing column**, so the raggedness is on the gap-facing surface after all | the zero coefficient is withdrawn and the whole channel needs a per-helix lattice |
| `F2` | the model fails to reproduce `C-0147`'s 4 and 8 bp at `112 / 108`, or fails to return 7 and 14 at `102 / 109` | the geometry is being read wrongly and every number here is void |
| `F3` | the modulation **wavelength** moves with the relief — *declared open* | the transfer is not a one-factor rescaling and the bound must be recomputed from `ℓ` |
| `F4` | the bounded move exceeds the headroom of any state the corpus currently calls flat — *declared open* | the raggedness becomes a design variable on the flatness axis and `C-0151`'s selection must carry it |
| `F5` | the relief falls below the **0.34 nm** design quantum | it is a residue, not a design variable, and cannot be traded |
| `F6` | the relief is **resolvable** by the slit's transverse decay, `relief / (1/q₀) > 1` at any of the nine `(gap, buffer)` states | `C-0022`'s collar cannot be solved on a straight rim and the edge load profile is owed a re-derivation |
| `F7` | the two length-to-sense assignments give **different** spreads, so the relief is not a property of the pair | the selection axis is under-specified and `C-0151` is owed the missing convention |
