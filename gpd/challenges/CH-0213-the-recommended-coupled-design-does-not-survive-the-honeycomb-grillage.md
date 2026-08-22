# CH-0213 — **`C-0151`'s *"2 flat cells of 8"* is `0` of 8, and the recommended coupled cell moves `0.0773373597 → 0.145086839` — because every coupled cell in this corpus is graded on a smeared sheet whose across-helix rigidity `C-0154` has measured to be `24/7` too large.**

| | |
|---|---|
| **Against** | [`C-0151`](../claims/C-0151-closing-raster-selection.md)'s headline — *"**2 flat cells of 8** against 3, and **the recommended cell survives at both ends of the measured band** — `0.0773373597` at `f = 0.30` and `0.0821458169` at `f = 0.26`, against `T-5b`'s 0.10"* — and, through the same mechanism, [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md)'s *"6 flat cells of 8 against 3"*, [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md)'s *"four flat cells, all four on `10 × 6`"* and [`C-0118`](../claims/C-0118-coupled-four-layer.md)'s *"nine flat cells of sixteen"* |
| **Raised by** | [`T-263`](../tasks/T-263-honeycomb-grillage-regrade.md) / [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) |
| **Grounds** | **in-silico, and CONTROLLED**: the same 64 cells, the same extent, the same stations, the same mandate, the same normalising stroke and the **same 4 000-realisation dropout stream**, graded on `C-0154`'s honeycomb grillage instead of on an `OrigamiGrillage` over an `equivalentSheet`. The smeared half **reproduces `C-0151`'s four published `p90` values at `≤ 6.3e−10`** in the same process, so the difference is the model and nothing else |
| **Status** | **RAISED, with the re-graded value in hand.** The *flatness* verdicts of all four claims are challenged; **no other verdict of any of them moves**, and `C-0151`'s selection of `102 / 109` — which is arithmetic on the closure lattice — is untouched |

---

## 1. The mechanism, which is `C-0154`'s and not new here

`OrigamiSheet.acrossHelixRigidity = layers × k_θ d / p` reproduces a honeycomb block's `D_∥` at
`2.8e−15` and overstates its `D_⊥` by **`24/7 = 3.42857×`**, because only half the in-plane
adjacent pairs of a honeycomb are bonded and an interlayer bond carries half the lever arm — one
layer of the block is a set of **dimers**, not a sheet, so the across-helix load path necessarily
traverses the thickness. Every coupled cell in this repository is an `OrigamiGrillage` over an
`equivalentSheet` built from that formula.

`C-0154` measured the consequence on the **free** tile and said in as many words that the coupled
one was open. This challenge closes it.

## 2. What the controlled re-grade says

At `C-0151`'s own recommended state — `102 / 109 bp`, `10 × 6`, the 116 bp block extent, the
abstract grid, `1 × 10 = 10` paths, equal springs, `C-0087`'s measured incorporation over 4 000
realisations on one stream:

| | `f = 0.30` | `f = 0.26` |
|---|---|---|
| `C-0151`, smeared sheet — **reproduced here at `6.3e−10` and `4.4e−11`** | 0.0773373597 | 0.0821458169 |
| **the honeycomb grillage** | **0.145086839** | **0.149852804** |
| ratio of the 90th percentiles | **1.87602525** | **1.82422928** |
| **median of the per-realisation ratios** | **1.85737817** | **1.7942757** |
| realisations at which the honeycomb reads worse | **4 000 of 4 000** | **4 000 of 4 000** |

Over the whole 64-cell set: **0 of 64** honeycomb cells clear `T-5b`'s 0.10 at the 90th percentile
against **15 of 64** smeared ones, and **15 of 64** paired cells change their verdict — every one
of them a loss. Restricted to `C-0151`'s own eight `f = 0.30` abstract-grid cells the count is
**0 of 8** against its published **2 of 8**.

## 3. What does NOT move, and it is most of each claim

- **The uncoupled block is still flat**, at both ends of the band: **0.0501417315** and
  **0.0522223659** of the stroke against `T-5b`'s 0.10. What fails is the **coupling**, and
  `CLAUDE.md`'s *"an attachment coupling can be a NET DISHING SOURCE"* is the standing name for it.
- **The recommended cell is still flat with no defects at all** — nominal 0.0626407003 at
  `f = 0.30`. It is the measured **staple dropout** that takes it past the tolerance, which is
  `C-0087`'s statistics acting on a stiffer-graded tile, not a new mechanism.
- **The correction is not a uniform multiplier and it is not monotone in the path count.** The
  per-realisation median ratio runs **1.06375481 to 2.47485493** — 1.380–2.475 at one column,
  1.166–1.555 at two, 1.064–1.208 at three and 1.190–1.462 at five. So *"multiply `C-0151`'s table
  by 1.87"* is not a repair. And at **six** of the 64 cells the ratio of the two 90th percentiles
  falls **below one** (0.946828178–0.96849327) while the median of the **per-realisation** ratios
  on the same draws is **1.06375481–1.13778808**: the unpaired summary reverses the sign, which is
  `CLAUDE.md`'s *"a ratio of two ORDER STATISTICS is not the order statistic of the ratio"*.
- **`C-0151`'s selection of `102 / 109` is untouched**: it is exhaustive integer arithmetic on the
  closure lattice, and the flatness axis was not what selected it.
- **The crossover inventory agrees.** The honeycomb lattice's own 21 bp-per-interface ladder gives
  **5 to 6** columns per interface against the smeared model's **5**, so there is no second
  overstatement hiding behind the first.

## 4. What would settle it the other way

The honeycomb lattice carries **no across-helix parallel-axis term** — the layers' membrane action
across the helices needs an in-plane transverse coordinate it does not have — so its `D_⊥` is the
**independent** one and therefore a **lower** bound, and the enhancement is applied as a smeared
multiplier on `k_θ`. If that term is worth substantially more than the multiplier supplies, the
honeycomb readings move toward the smeared ones. Two things bound how much comfort that is:

- at the lattice's own **lower** end — no enhancement at all — the free tile is **0.132443428**,
  outside the tolerance before any coupling, and **0 of 32** cells clear it; and
- the smeared model is the **upper** end of the same bracket, so the answer is enclosed, and at
  the upper end `C-0151`'s own count is 2 of 8.

The clean resolution is `C-0154` §10's first open item: carry the transverse coordinate and make
the across-helix enhancement an output, as the along-helix one already is.
