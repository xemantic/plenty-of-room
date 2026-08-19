# CH-0174 — **The four-layer tile's cross-section is not a honeycomb, its `edgeY` is exactly 1.5× too small at every `m`, and `C-0120`'s footprint ordering therefore REVERSES**

| | |
|---|---|
| **Against** | [`C-0120`](../claims/C-0120-cross-section-comparison.md) — its footprint table (*"`15 × 4` … 38.08 × 38.04 = 1 448.5632 nm², essentially §3's"*; *"`10 × 6` … 38.08 × 25.36 = 965.7088 nm², **0.666666667** of it"*) and the headline clause *"and it costs a third of the footprint"*; and [`C-0109`](../claims/C-0109-four-layer-tile.md)'s cross-section, carried by [`C-0116`](../claims/C-0116-composite-fraction-threshold.md), [`C-0118`](../claims/C-0118-coupled-four-layer.md), [`C-0122`](../claims/C-0122-honeycomb-station-lattice.md), [`C-0123`](../claims/C-0123-collar-aspect-ratio.md) and [`C-0128`](../claims/C-0128-oblique-attachment-root.md) |
| **Raised by** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) / [`T-219`](../tasks/T-219-honeycomb-station-lattice-and-placement.md), result [`gpd/results/T-219-honeycomb-station-lattice-and-placement.json`](../results/T-219-honeycomb-station-lattice-and-placement.json) |
| **Grounds** | methodological, and settled by **one multiplication**: a honeycomb lattice of bond length `d` spends `3√3/4 · d²` of plan area per helix, and the cross-section every four-layer claim here is written on spends `d²`. The two are not the same lattice, and the discrepancy is **not** a layer-spacing convention — the two pitches are wrong in **opposite** directions |
| **Status** | **raised.** The correction is **unfavourable to `15 × 4` and favourable to `10 × 6`**, and it moves a verdict at the low end of the measured coupling band |

---

## 1. What the corpus assumes, and what a honeycomb is

`tile/CrossSectionComparisonStudy` sets `edgeY = rasterRows × Gen1Tile.INTERHELICAL_HONEYCOMB` and
passes the same `d` as `interhelicalDistance` to `multiLayerRigidities`, whose `layerSpacing`
defaults to it. So the assumed cross-section is a **rectangular** array at pitch `d × d`.

A honeycomb lattice has **one** lattice constant and **two** pitches:

| | honeycomb | assumed | ratio |
|---|---|---|---|
| in-plane row pitch | `3d/2` = **3.804 nm** | `d` = 2.536 | **1.5** |
| layer pitch | `d√3/2` = **2.19624042 nm** | `d` = 2.536 | 0.866 |
| plan area per helix | `3√3/4 · d²` = **8.35449857 nm²** | `d²` = 6.431296 | **1.29903811** |

**The assumed cross-section is 1.299× denser than any honeycomb of that bond length can be**, which
is a statement no folded object can satisfy.

**And `CLAUDE.md`'s standing note repairs the wrong half.** It records that *"a true honeycomb array
stacks its rows at `d√3/2`, not `d`, so the default **overstates** `Σy²` by `4/3`"* — correct about
the layer spacing, and applying it **alone** takes the per-helix area to `2.536 × 2.196 = 5.569 nm²`,
which is **1.50×** out where the uncorrected pair is 1.30× out. Only the **product** of the two
pitches is the cell.

## 2. The consequence for the footprint, which is the reversal

`10 × 1.5 = 15` exactly, so the corrected `10 × 6` tile has **bit-for-bit** the `edgeY` the corpus
attributed to `15 × 4`:

| | `C-0120`'s `edgeY` | of §3's 40.35 | **honeycomb `edgeY`** | **of §3's 40.35** | thickness |
|---|---|---|---|---|---|
| `15 × 4` | 38.04 nm | 0.942750929 | **57.06 nm** (envelope 56.524) | **1.40084263** | 8.58872127 nm |
| `10 × 6` | 25.36 nm | 0.62850062 | **38.04 nm** (envelope 37.504) | **0.929467162** | 12.9812021 nm |

So the cross-section `C-0120` calls *"essentially §3's"* is **40 % wider** than §3's, and the one it
charges *"a third of the footprint"* for **is** §3's footprint to within 7 %. `C-0120`'s
*"§3's 100 pN is specified over that footprint"* paragraph is arguing the trade the wrong way round.

## 3. The consequence for the verdict, which is not merely a number

Re-solved with **only** `edgeY`, the in-plane pitch and the layer spacing moved and the rest of
`C-0120`'s own construction bit-identical (its `0.0577199433` and `0.00874363524` reproduce at
departure **`0.0`** at the geometry they were solved on):

| | free-tile dishing | `C-0116`'s `f*` | at the measured band low, `f = 0.26` |
|---|---|---|---|
| `15 × 4` standing | 0.0577199433 | 0.0788618807 | 0.0612595739 — flat |
| **`15 × 4` honeycomb** | **0.0978155002** | **0.276970522** | **0.101759944 — NOT FLAT** |
| `10 × 6` standing | 0.00874363524 | none | 0.00927188486 — flat |
| **`10 × 6` honeycomb** | **0.0240648102** | **0.012737738** | **0.0255589305 — flat** |

**`C-0116`'s threshold moves INSIDE the measured 0.26–0.33 band on `15 × 4`.** At the low end of the
only interlayer-coupling calibration anybody has measured, the corrected `15 × 4` tile fails
`T-5b`'s 0.10. `10 × 6`'s threshold stays **20×** below the band.

**`C-0120`'s central finding survives and is strengthened**: the second cross-section removes the
dependency on the interlayer-coupling calibration, and after this correction it is the *only* one
that does. What is withdrawn is its **cost line** — there is no footprint to pay.

## 4. What this challenge does NOT claim

- **It does not overturn `C-0118`.** Its 16-cell dropout grading is not re-run here, so no
  90th-percentile number moves; the four-layer line's *nominal* flatness survives on both
  cross-sections. What moves is the **margin** on `15 × 4`, and the band verdict at `f = 0.26`.
- **It does not touch the rigidity model.** `multiLayerRigidities` already accepts `layerSpacing`;
  what is new is that `interhelicalDistance` must then be `3d/2` and not `d`, and that the two travel
  together.
- **It is a lattice statement.** No folded object is measured; the cross-section follows from the
  caDNAno paper's own corrugation sentence and Figure 2 nomenclature, both quoted in `C-0141` §2.
- **`C-0022`'s collar is read unchanged at both aspect ratios and both have now moved by 1.5×**,
  which reopens [`C-0123`](../claims/C-0123-collar-aspect-ratio.md)'s question rather than answering
  it.
