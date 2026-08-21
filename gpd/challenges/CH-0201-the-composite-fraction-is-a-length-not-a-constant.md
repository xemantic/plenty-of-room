# CH-0201 — **The composite fraction is a property of the LENGTH and the LOAD CASE, not of the crossovers, so Kauert et al.'s 0.26–0.33 is not a constant this project may substitute into a 38 nm tile.** In *uniform* bending an infinitely long composite is rigid whatever its connectors — the axial force in each layer is constant and no shear flows — so what a partial composite measures is a **boundary layer at the free ends**. Measured on one honeycomb lattice, one `k_s` and one load case, `f` runs **0.0717149752 → 0.737066133** over a `56 → 448 bp` row; and the **two** 60-helix cross-sections the corpus applies **one** band to fall on **opposite sides** of it — **0.246803583** at `10 × 6` and **0.406535456** at `15 × 4`. Worse, `f` is not one number even on one lattice: read on the **dishing** instead of on the **rigidity**, the same block at the same `k_s` reports **0.940471** against **0.246804**, **3.81×**

| | |
|---|---|
| **Against** | `CLAUDE.md`'s standing entry *"`InterlayerCoupling.RIGID` is a ~3× OVER-prediction, and the fraction is MEASURED … `f = 0.26–0.33` on four measured origami bundles"* **read as transferable**, and with it `LayerCoupling.CALIBRATED`'s use in [`C-0116`](../claims/C-0116-composite-fraction-threshold.md), [`C-0120`](../claims/C-0120-cross-section-comparison.md), [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md), [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) and [`C-0151`](../claims/C-0151-closing-raster-selection.md) |
| **Raised by** | [`C-0154`](../claims/C-0154-honeycomb-grillage.md) / [`T-253`](../tasks/T-253-honeycomb-grillage.md) §4, result [`gpd/results/T-253-honeycomb-grillage.json`](../results/T-253-honeycomb-grillage.json), sections `realisedRigidity`, `lengthDependence` and `interlayerCoupling` |
| **Grounds** | **methodological + in-silico.** The methodological half is a limiting case anybody can check without a solve; the measurement is a Schur complement on the axial coordinates of a honeycomb lattice, swept over the row length and over four decades of `k_s` |
| **Kind** | **a scope correction, and the measurements it rests on are not disputed.** Kauert, Kurth, Liedl & Seidel's `f = 0.26–0.33` is measured, reproducible and correctly quoted here; what is challenged is the claim that it is a **material** constant rather than a reading of a `740 nm – 2 µm` bundle in bending fluctuations |
| **Status** | **raised.** No verdict in any of the six claims is overturned by this task. `C-0116`'s threshold **construction** is untouched; what moves is what a threshold *in `f`* means once `f` is not a scalar the design inherits |

---

## 1. The limiting case, which needs no code

Take a composite of `n` layers connected by shear springs, under a **uniform** moment, and let the
length go to infinity. Each layer's axial force is constant along the span, so the shear flow at
every connector is zero, so no connector slips, so the section behaves **rigidly** — `f → 1` — at
**any** connector stiffness. The partial-composite effect therefore lives entirely in the region
where the axial force must run down to zero at a free end. `f` is a boundary-layer measure.

It follows immediately that `f` carries a **length**, and that two experiments on the same material
at different spans, or under different load cases, must report different fractions.

## 2. The measurement

`HoneycombGrillage` gives every duplex an axial coordinate and every bond Chen et al.'s own slip
spring `k_s = 2αS/(100a)`. Imposing the bending kinematics and relaxing the axial coordinates to
equilibrium — a Schur complement, one factorisation, no iteration — reads the realised along-helix
rigidity between its two limits.

**Against the row length**, at `10 × 6`, one cross-section, one `k_s`, one load case:

| row | 56 bp = 19.04 nm | 112 bp = 38.08 nm | 224 bp = 76.16 nm | 448 bp = 152.32 nm |
|---|---|---|---|---|
| realised `D_∥` | 2113.26686 | 6387.00468 | 11987.8619 | 18353.8233 |
| **`f`** | **0.0717149752** | **0.246803583** | **0.476262276** | **0.737066133** |

A **10.3×** span in `f` over an **8×** span in length, with every crossover identical.

**Against the cross-section**, at the Gen-1 112 bp row:

| | independent | parallel axis | realised | **`f`** | against 0.26–0.33 |
|---|---|---|---|---|---|
| `10 × 6` | 362.776025 | 24771.776 | 6387.00468 | **0.246803583** | **below**, by 5.1 % of the band's low edge |
| `15 × 4` | 241.850683 | 7215.85068 | 3077.02895 | **0.406535456** | **above**, by 23.2 % of its high edge |

**The corpus applies one band to both, and they straddle it.**

## 3. And it is not one number even on one lattice

Read on the *free-tile peak dishing under `C-0022`'s solved collar* rather than on the rigidity, the
same `10 × 6` block at the same nominal `k_s` reports `f = 0.940471`. The reason is visible in the
sweep: over four decades of `k_s` the dishing moves only `0.149649 → 0.125948` of the stroke,
because it is dominated by the **across-helix** compliance, so a fraction defined on it is a ratio
of small differences in a quantity the coupling barely touches. `CLAUDE.md`'s *"quote it with the
state it is read at"*, applied to a calibration rather than to a stiffness — the twelfth instance.

## 4. What it moves, and what would settle it

- At `10 × 6` — the recommended cross-section — the corpus uses `f = 0.30` where this lattice
  measures `0.246803583`, i.e. the standing grading is **optimistic by 17.7 % of `f`**; the
  realised enhancement falls `21.1851817 → 17.6059172` and the free-tile dishing rises
  `0.0449400126 → 0.0477844467`, both still inside `T-5b`'s 0.10.
- At `15 × 4` it runs the other way, `+35.5 %` of `f`, and that cross-section fails `T-5b` on this
  lattice at every coupling anyway.
- **`C-0116`'s threshold is a threshold in `f`.** If `f` is a function of the geometry then a
  threshold in it must be re-read at the geometry, which is a change in what the number *is* rather
  than in its value.
- **What would settle it** is either a published bending-rigidity measurement of a multi-layer
  origami bundle at the Gen-1 span — none was found in this repository's own literature corpus —
  or an in-plane transverse extension of this lattice, which would let the same Schur complement be
  run across the helices and the two channels compared directly.
- **What would falsify this challenge** is a demonstration that the measured 0.26–0.33 is *not* a
  boundary-layer effect — for instance a bundle measurement in which `f` is independent of the
  contour sampled — in which case the number would be a genuine connector property and the length
  sweep above would be an artefact of `k_s` being a construction rather than a measurement.
