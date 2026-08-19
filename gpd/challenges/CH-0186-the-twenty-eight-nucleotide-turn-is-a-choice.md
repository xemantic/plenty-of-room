# CH-0186 — the built block's **28 nt turn loop is 4.67× its own reach bound**, so `CH-0173`'s *"the widest four-layer tile is 92 bp = 31.28 nm"* is a ceiling on **one published allowance** and not on route B

| | |
|---|---|
| **Against** | [`CH-0173`](CH-0173-the-built-block-turns-on-loops-not-crossovers.md) item 1 — its table's *"60 helices \| **8 400 nt** \| M13mp18 … **1 151 nt SHORT**"* and the sentence *"At the built allowance the widest four-layer honeycomb tile is **92 bp = 31.28 nm** from M13 (`−21.80 %`)"* — and, through it, [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) §5's *"route B (uniform, loops) does not fit M13 at 112 bp at all"* |
| **Raised by** | [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) (`T-230`) |
| **Kind** | **substantive** — a ceiling derived by transcribing one design's *choice* as if it were a requirement, where the requirement is measurable and is **4.67× smaller** |
| **Status** | **OPEN.** `CH-0173`'s own text says *"This is evidence, not a requirement"*, so the item is honest about its ground; what moves is the number a reader takes away, and the verdict it supports survives on a **different** and stronger reason |

---

## The ground

A raster turn is a single strand running from the last paired base of one helix to the first paired
base of its neighbour. `n` unpaired nucleotides between those two anchoring phosphates make
**`n + 1`** phosphodiester steps, so the greatest span the chain can reach is `(n + 1) × step` with
`step` the **measured** intrastrand P···P distance — `T-71`, 13 084 crystallographic linkages,
**0.664481 ± 0.036163 nm** at C2′-endo.

The furthest apart the two anchors can be is `d + 2r_P` with the honeycomb's own SAXS lattice
constant and `T-71`'s measured phosphate radius:

&nbsp;&nbsp;&nbsp;&nbsp;`2.536 + 2 × 0.908638 = **4.35327572 nm**`, &nbsp; which **6** nucleotides reach.

The built blocks spend **28**. That is **4.66666667×** the bound, and at the azimuth-averaged span
(2.70321445 nm) the bound is **4**.

**The same arithmetic checks itself at `n = 0`.** A scaffold *crossover* is a turn with no unpaired
nucleotides at all, so its span must be one phosphodiester step — and
`d − 2r_P = **0.718724283 nm**` sits at `+1.49997857 σ` of the measured step and inside its 99th
percentile, 0.756745. Nothing here is fitted: the honeycomb's lattice constant and a
crystallographic survey agree, unprompted, that a crossover closes and that it closes tightly.

## What moves

| | `CH-0173` as written | at the reach bound |
|---|---|---|
| unpaired per helix | **28 nt**, the built design's | **6 nt**, the worst relative azimuth |
| 60 helices at a 112 bp row | 8 400 nt, **1 151 SHORT** on M13 | 7 080 nt, **169 nt spare** |
| widest uniform row on M13 | **92 bp = 31.28 nm** (`−21.80 %`) | **114 bp = 38.76 nm** (`−3.10 %`) |
| widest uniform row on p8064 | 106 bp = 36.04 nm | 128 bp = 43.52 nm |

`C-0147` reproduces `CH-0173`'s 92 / 98 / 106 bp at departure **`0.0`** — the disagreement is not
arithmetic, it is about which allowance the ceiling belongs to.

## What does **not** move, and why the verdict is safer than it was

Route B is not thereby comfortable. M13 affords `60 × (112 + L) ≤ 7 249`, i.e. **`L ≤ 8` nt**, and
at the worst azimuth an 8 nt turn sits at **0.777–0.837 of its own contour**, carries
**6.54–12.11 pN** and stores **2.36–3.74 `k_BT`** — at or past `Gen1Tile`'s 10 pN unzip allowable
at the tight end of the ssDNA Kuhn bracket, and **139–220 `k_BT`** of stored strain over the
raster's 59 turns. The built 28 nt, by contrast, carries **1.00–1.47 pN** and stores
**0.52–0.76 `k_BT`**: sub-thermal, about one piconewton, and **1.27–1.75×** the one-`k_BT` bound.

So `C-0140`'s recommendation of route A stands, and the reason improves: not that route B *does
not fit*, but that **it fits only strained**. A negative result whose failing element is 4.67×
away from the bound is weaker than one whose failing element is at it.

## What it does **not** touch

`CH-0173` item 2 — the p7560 identification, `60 × 126 = 7 560` exactly — is untouched and is
reproduced here. `C-0140`'s turn-sense derivation, its two-length raster and its 112 / 108
recommendation are untouched by this challenge (see [`CH-0187`](CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md) for the latter).
