# C-0120 — **The cross-section its own source recommends on YIELD grounds is also the one that removes this programme's last unmeasured dependency on FLATNESS grounds — and it costs a third of the footprint.** `10 × 6` dishes **0.00874363524** of the stroke against `15 × 4`'s **0.0577199433**, 6.6× flatter at the **same 60 helices** — and it has **no composite-fraction threshold at all**: its dishing never reaches `T-5b`'s 0.10 anywhere in `f ∈ [0, 1]`, **including `f = 0`, where the layers are fully independent**. So it is flat *without* depending on the interlayer-coupling calibration, which `C-0116` identifies as the one unmeasured number the `15 × 4` verdict rests on. **The stronger cross-section is stronger by REMOVING a dependency, not by widening a margin.** The cost is a specification one: at the fixed 112 bp span it is **38.08 × 25.36 nm**, **0.667** of the footprint, against §3's ~40 × 40

| | |
|---|---|
| **Task** | [`T-199`](../tasks/T-199-cross-section-comparison.md) — is 10 × 6 a better tile than 15 × 4? |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** (beam-and-hinge grillage, `C-0022`'s solved collar) **+ logical** (the `(n² − 1)` second-moment bound, asserted as a test before any plate is solved) |
| **Maturity** | **TRL 1–3** for the flatness. **Both cross-sections are above it for FOLDING** — Douglas et al. designed, folded, gel-analysed and imaged them, and this claim reports that rather than demonstrating it. |
| **Verdict** | **PASS on all four predicates. `F1` and `F3` did not fire; `F2` reproduced at `3.2e−10` or better.** 10 × 6 is flatter *and* threshold-free, so the paper's yield recommendation and this programme's flatness criterion **agree** — which was not the expected outcome and is the reason the falsifier was declared. All four cross-sections solved are flat at the measured coupling. |
| **Provenance** | [`gpd/results/T-199-cross-section-comparison.json`](../results/T-199-cross-section-comparison.json), produced by `tile.CrossSectionComparisonStudyKt`; model additions in [`tile/CompositeFractionThreshold.kt`](../../src/main/kotlin/tile/CompositeFractionThreshold.kt), tests [`tile/CrossSectionComparisonTest.kt`](../../src/test/kotlin/tile/CrossSectionComparisonTest.kt) (6, written first and watched to fail). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb at 10.5 bp/turn, 21 bp per interface, `d` = 2.536 nm. Span fixed at 112 bp for every cross-section. `C-0022`'s collar at 2 mM / 10 nm / 0.192 V. Uncoupled throughout. |
| **Consumes** | [`C-0119`](C-0119-honeycomb-raster-width.md) (the published cross-sections and the yield ordering), [`C-0109`](C-0109-four-layer-tile.md) (the rigidity machinery), [`C-0116`](C-0116-composite-fraction-threshold.md) (the threshold, re-read per cross-section), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0086`](C-0086-seamless-scaffold-routing.md) |
| **Constrains** | Nothing numerically — `C-0109`'s and `C-0116`'s numbers reproduce and are unmoved. **No claim is contradicted and no challenge is raised.** What this adds is a design choice with evidence on both sides of it. |

---

## 1. The cheap bound, which predicted the direction before any plate was solved

`Σy²` for `n` layers at spacing `d` is `n(n² − 1)d²/12`, so the parallel-axis **excess** scales as `(n² − 1)`:
a pure integer function of the layer count, with no material constant in it.

**Six layers carry `35/15 = 2.3333×` the excess of four, at the same 60 helices.**

Asserted as a test — the ratio of the two factors' excesses equals `(6²−1)/(4²−1)` to `1e-9` — before the
grillage was run.

---

## 2. The four cross-sections

All 60 helices, all at the same 112 bp span, all under the same solved collar, all uncoupled:

| | `Σy²` | factor | `D_∥` | `D_⊥` | dishing | flat | threshold `f*` | tile |
|---|---|---|---|---|---|---|---|---|
| **15 × 4** | | **39.4479652** | 4 547.2 | 240.93 | **0.0577199433** | yes | **0.0788618807** | 38.08 × 38.04 nm |
| **10 × 6** | | **90.7119188** | 15 189.6 | 804.82 | **0.00874363524** | yes | **none** | 38.08 × 25.36 nm |
| 6 × 10 | | 254.75657 | 69 949.5 | 3 706.26 | 0.000637505981 | yes | none | 38.08 × 15.22 nm |
| 3 × 20 | | 1023.71587 | 558 339.1 | 29 583.49 | 0.0000262401216 | yes | none | 38.08 × 7.61 nm |

---

## 3. The threshold does not improve — it disappears

This is the result, and it is a different *kind* of statement from a better margin.

`C-0116` established that `15 × 4`'s flatness is conditional: the dishing crosses `T-5b`'s 0.10 at
`f = 0.0788618807`, and the measured interlayer coupling 0.26–0.33 clears it by **3.2969×**. That margin rests
on a calibration measured on **rods**, and `C-0116` states plainly that a 15-wide × 4-deep **slab** plausibly
realises less — *"it would have to fall below 30 % of the least-coupled measured bundle for the verdict to
fail"*. **That is the one unmeasured number the whole four-layer result now depends on.**

**`10 × 6` has no threshold at all.** Its free-tile dishing never reaches 0.10 anywhere in `f ∈ [0, 1]` —
**including `f = 0`, where the layers do not couple at all** and simply add. So the flatness of that
cross-section does not depend on the interlayer-coupling calibration in any way, and the slab-versus-rod
question stops being load-bearing for it.

**A null threshold is the ABSENCE of a requirement, not a zero margin.** `CLAUDE.md` records exactly this —
*"a margin of Infinity is not a margin, it is the absence of a requirement; record it as `null`, not as a
number"* — and the first run of this study got it wrong in the derived boolean, coalescing the null to `0.0`
and reporting the **stronger** cross-section as the weaker one. Repaired, with the reasoning in the code.

---

## 4. What it costs, and it is a specification cost rather than a physical one

At the fixed 112 bp span, changing `m` changes the tile's **other** side:

| | footprint | against §3's ~40 × 40 nm |
|---|---|---|
| **15 × 4** | 38.08 × 38.04 = **1 448.5632 nm²** | essentially square, essentially §3's |
| **10 × 6** | 38.08 × 25.36 = **965.7088 nm²** | **0.666666667** of it |

**§3's 100 pN is specified over that footprint**, and `C-0022`'s collar was solved on the square. So the
flatter cross-section is a **smaller tile**, and this is a trade for NDI rather than a free improvement:
a third less area, at the same scaffold, for a flatness that no longer depends on an unmeasured coupling.

**And Kirchhoff degrades as the block thickens.** Thickness over span goes `0.252 → 0.386 → 0.652 → 1.318`
across the four; `C-0109` already flagged 0.252 as past where transverse shear is safe, so every `D_∥` here is
an upper bound and the thicker cross-sections are **weaker** upper bounds. The dishing ordering is not
threatened by that — six layers are stiffer than four however the shear is treated — but the *magnitudes* are.

---

## 5. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | rigidities in pN·nm, reach and footprint in nm, dishing and fraction dimensionless |
| **limiting cases** | one layer has `factor = 1` exactly and **no** threshold is defined on it — `fractionForEnhancement` refuses, rather than dividing by zero |
| **symmetry / conservation** | both rigidities scale by the same enhancement (the identity `C-0116` rests on), and the `(n²−1)` excess law is asserted against the solved factors at `1e-9` |
| **numerical convergence** | mesh subdivisions 1/2/4 nested, and the dishing grid 41/81/161, both reported per the emitted file |
| **literature cross-check** | the cross-sections and the yield ordering are `C-0119`'s, read directly from Douglas et al.; nothing new is cited |

**Reproductions**, licensing every comparison: `15 × 4` dishing at **5.9e−10**, its threshold at **3.2e−10**,
its parallel-axis factor at **4.4e−10**.

---

## 6. Validity range, and what this does NOT establish

- **Only the UNCOUPLED tile is solved.** Whether a coupling helps any cross-section is `T-197`, and `C-0109`
  already reports every coupled cell as worse than uncoupled on `15 × 4`.
- **`C-0022`'s collar is read unchanged on a tile of a different aspect ratio.** It was solved on a 40 nm
  square. That is the load-side limitation of this comparison and it is not repaired here — a re-solve at
  38.08 × 25.36 nm is the successor.
- **The yield ordering is a fabrication measurement and is not re-derived.** This claim cannot check it; it
  reports it.
- **Every cross-section is a smeared equivalent sheet**, as in `C-0109` and `C-0116`; the grillage never reads
  `layers`.
- **6 × 10 and 3 × 20 are flatter still and are NOT recommended by anything.** Both were folded by Douglas et
  al. and neither produced a sharp monomer band, so folding excludes them where flatness does not — which is
  the clearest illustration in this claim that the two criteria are independent and both are needed.
