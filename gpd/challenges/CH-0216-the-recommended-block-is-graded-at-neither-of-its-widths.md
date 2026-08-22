# CH-0216 — **the recommended block is graded at `112 bp = 38.08 nm`, which is NEITHER of its two width readings** — the drawable `102 / 109` raster's rows span **109 bp = 37.06 nm** and its bounding box is **116 bp = 39.44 nm** (`C-0151`, `C-0160`, and decision 8 of `DECISIONS-FOR-NDI.md`, which puts the whole question to NDI as a choice between exactly those two). `38.08 nm` is the **square** sheet's own row length (`C-0086`) and the **withdrawn** `112 / 108` honeycomb pair's, and it is what `tile/HoneycombCoupledStudy.kt` and `tile/HoneycombPlacementStudy.kt` set `edgeX` to — including for the headline `10 × 6` free-tile dishing **`0.0240648102`** that `ANSWERS.md` row (g) and `DECISIONS-FOR-NDI.md` decision 6 both carry. **Measured, it is worth at most 3.9 % and no verdict moves**: 0.0231880196 at 109 bp, 0.0240648102 at 112, 0.0231299291 at 116, all three flat at `T-5b`'s 0.10 and **both alternatives better than the value in print**. So this is a scope statement with a price attached, filed rather than repaired, because the repair is a re-emission of two result files

| | |
|---|---|
| **Against** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) (`T-219`, which emits the 0.0240648102) and [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (`T-232`, whose cells are graded at the same `edgeX`) |
| **Raised by** | [`C-0168`](../claims/C-0168-recommended-block-seam.md) (`T-274`), while showing the same two studies are inside `C-0161`'s alternating family |
| **Grounds** | **in-silico, priced** — one grillage grading per extent on the studies' own tile, plus two `grep`s of the study sources |
| **Status** | **RAISED AND PRICED — no verdict moves.** The repair is a re-emission and it belongs with [`T-263`](../tasks/T-263-honeycomb-grillage-regrade.md), which is re-grading the same cells on the honeycomb grillage |

---

## The observation

```
src/main/kotlin/tile/HoneycombCoupledStudy.kt:   private const val T232_ROW_BP: Int = 112
src/main/kotlin/tile/HoneycombPlacementStudy.kt: val rowBasePairs: Int = 112
```

and `edgeX = rowBasePairs × 0.34 nm = 38.08 nm` in both.

`gpd/designs/README.md` carries the block's two width readings and says which is which:
**39.44 nm** on the bounding box (`−1.40 %` of §3's 40 nm) and **37.06 nm** on the row span
(`−7.35 %`). Neither is 38.08.

The 112 bp is not arbitrary — it is `C-0086`'s buildable **seamless square-lattice** row width, and
it was also the row span of `C-0140`'s **112 / 108** honeycomb pair, which `C-0151` **withdrew**
because it does not close on caDNAno's `±5 bp` scaffold rule. So the studies inherited a number whose
two justifications are a different lattice and a withdrawn raster.

## The price, measured

On the studies' own tile — `10 × 6`, honeycomb pitches, `edgeY = 38.04 nm`, `C-0022`'s solved collar
at 2 mM / 10 nm / 0.192 V, `CrossoverLayout.centred`, two subdivisions, 81 samples — sweeping only
the extent:

| reading | bp | `edgeX` | columns | free-tile collar dishing | of as-graded | flat at `T-5b` |
|---|---|---|---|---|---|---|
| the drawable raster's **row span** (`C-0151`) | 109 | 37.06 nm | 11 | **0.0231880196** | 0.963565448 | yes |
| **as graded by both studies** | 112 | 38.08 nm | 11 | **0.0240648102** | 1.0 | yes |
| the drawable raster's **bounding box** (`C-0146`) | 116 | 39.44 nm | 12 | **0.0231299291** | 0.96115153 | yes |

**At most 3.9 %**, and in the favourable direction at both ends. The as-graded value is reproduced at
departure **`0.0`** against `T-219`'s committed file, which is what makes this the same object rather
than a second implementation.

## Why it is filed and not repaired

- The repair is a **re-emission** of `T-219` and `T-232`, and `T-263` is re-grading exactly those
  cells on the **honeycomb grillage** rather than on the smeared equivalent sheet. Re-emitting them
  twice in one iteration is the pattern `C-0117` names — a consumer run before its producer.
- The extent is a **convention question already before the customer**: decision 8 asks NDI which of
  the two widths the tile is specified to, and it explicitly states that no flatness cell turns on the
  answer. This measurement is the first one that checks that statement at the **free tile** rather than
  at a coupled cell, and it upholds it.
- **The corpus does not currently name 38.08 nm as a third reading**, and it should — a study that
  grades at a width neither reading owns is not covered by *"both readings are true of the same
  object"*.

## What is *not* claimed

- **Not** that any published verdict is wrong. All three extents are flat, and both alternatives are
  flatter than the value in print.
- **Not** that 112 bp is unreasonable. It is the buildable square-lattice width and it sits between
  the block's two readings; what is missing is the sentence saying so.
- **Not** a statement about the coupled cells. Only the **free tile** was swept here; the coupled
  cells are `T-263`'s.
