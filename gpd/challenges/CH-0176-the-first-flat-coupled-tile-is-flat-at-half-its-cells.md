# CH-0176 — **`C-0118`'s nine flat cells of sixteen are FOUR of sixteen at the corrected honeycomb cross-section, and NONE of them is on `15 × 4`.** `10 × 6` falls from *"all eight"* to **four of eight**, its best cell from **0.0278431488** to **0.0680677948**, and `15 × 4`'s single flat cell — 0.0882933461 at 75 paths with the rim grading, the whole of that cross-section's coupled evidence — is **0.145354102** and fails. The cross-section's worth on the statistic falls from **3.17109774×** to **2.13543134×**. **The claim's design CONCLUSION survives and is strengthened; every number it is stated in does not**

| | |
|---|---|
| **Against** | [`C-0118`](../claims/C-0118-coupled-four-layer.md) — its title (*"Of 16 graded cells, **9 are flat**; on `10 × 6` **all eight** are, best **0.0278431488** … and on `15 × 4` **one of eight** is, at **0.0882933461**. The cross-section is worth **3.17109774×**"*), its §2 table, its §3 table and its `Verdict` row (*"decisively on `10 × 6` (8 of 8 cells, at every path count and both distributions) and marginally on `15 × 4` (1 of 8, needing 75 paths *and* the rim grading)"*) |
| **Raised by** | [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) / [`T-232`](../tasks/T-232-coupled-cells-at-the-honeycomb-cross-section.md), result [`gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json`](../results/T-232-coupled-cells-at-the-honeycomb-cross-section.json) |
| **Grounds** | **geometric, and already conceded upstream.** [`CH-0174`](CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md) establishes that the cross-section `C-0118`'s cells are graded on is not a honeycomb — every `edgeY` is exactly 1.5× too small — and `C-0141` §9 states in as many words that *"`C-0118`'s 16-cell dropout grading is **NOT** re-run, so no 90th-percentile number is produced here and its verdict is neither reproduced nor overturned"*. This challenge is that re-run |
| **Status** | **raised.** All sixteen of `C-0118`'s standing numbers reproduce at `1.4e−10 … 4.4e−9` in the same process that produces the corrected ones, so the movement is the geometry and nothing else |

---

## 1. What moves

Same machinery, same seed, same 4 000-realisation common stream, same `C-0087` dropout, same
`C-0017` mandate, same `T-5b` tolerance. **Only `edgeY`, the in-plane duplex pitch and the layer
spacing move**, to `C-0141`'s `3d/2` and `d√3/2`.

| | `C-0118`, as published | corrected honeycomb, `f = 0.30` |
|---|---|---|
| flat cells of sixteen | **9** | **4** |
| flat on `10 × 6` | **8 of 8** | **4 of 8** |
| flat on `15 × 4` | **1 of 8** | **0 of 8** |
| best cell overall | 0.0278431488 | **0.0680677948** |
| best on `15 × 4` | 0.0882933461 | **0.145354102** |
| cross-section worth | 3.17109774× | **2.13543134×** |

And the whole `15 × 4` line is gone at the **adverse** end of the measured interlayer-coupling band
as well: at `f = 0.26` it is **0 of 8** and `10 × 6` is **4 of 8**.

## 2. Why the direction was not obvious, and what the cheap bound said

The correction moves the free-tile reference **1.69×** on `15 × 4` (0.0577199433 → 0.0978155002)
and **2.75×** on `10 × 6` (0.00874363524 → 0.0240648102) — so a naive proportional transfer of
`C-0118`'s own cells lands `10 × 6` at 0.076–0.171, **straddling the tolerance**, which is why the
run was necessary rather than a formality. Measured, the paired per-realisation cost is **1.63–1.79×**
on `15 × 4` and **2.15–3.82×** on `10 × 6`: the *sparser* tile pays more, because the correction
widens the attachment pitch by 1.5× and a wider pitch costs a sparse coupling more than a dense one.

## 3. What survives, and it is the part the claim was written for

- **A coupled four-layer tile IS flat at the 90th percentile under the measured folding statistics.**
  Four cells clear `T-5b`, at both ends of the measured band, and `C-0118` remains the first claim in
  this programme to reach that.
- ***"What delivers it is the CROSS-SECTION, not the coupling"* is STRENGTHENED.** The cross-section
  still dominates the distribution (2.14× against what either distribution buys inside one tile), and
  it now separates a cross-section that works from one that does not, rather than two that both do.
- **`C-0109`'s *"every coupled cell is worse than the uncoupled tile"* reproduces**, at 16 of 16
  corrected cells.

## 4. What the correction asks of the claim

The title, the §2 table, the §3 table and the `Verdict` row all quote numbers this challenge moves.
`C-0142` re-states them; the claim should be annotated in place per `C-0071`'s *strike, never delete*.
Nothing in `C-0118`'s §1 framing (the mandate is an equality on the SUM, the uncoupled tile is a
reference and never a design) is touched — that is an argument, not a number, and it is what made
the re-grading well posed.
