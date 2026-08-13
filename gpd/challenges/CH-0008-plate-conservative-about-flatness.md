# CH-0008 — The continuum plate is not conservative about flatness, and its own validity criterion pairs the wrong two lengths

| | |
|---|---|
| **Challenges** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), its `Validity range` bullet on the continuum plate reduction, and the `ℓ_⊥/p` criterion in its `The governing group` section |
| **Raised by** | [`C-0009`](../claims/C-0009-discrete-lattice-tile.md), task [`T-10`](../tasks/T-10-discrete-lattice-tile.md) |
| **Raised** | 2026-08-12, iteration 4 |
| **Status** | **UPHELD in part.** `C-0006` is not withdrawn and none of its verdicts moves. Two of its statements are wrong: one about the *direction* of its own error, one about *which two lengths* its validity criterion compares. |

---

## The standing statements being challenged

`C-0006`, validity range, first bullet:

> **`ℓ_⊥ < p` across the entire sweep**, so the continuum plate reduction *across* the helices
> is marginal: the bending length is shorter than the crossover spacing. The tile is better
> described as ~15 quasi-independent duplex beams sharing a cushion. **Every conclusion above is
> therefore conservative about flatness: a discrete lattice has *more* shape freedom than the
> plate that approximates it, not less.**

Two claims are packed into that bullet and they are independent:

1. **the criterion** — that `ℓ_⊥` should be compared against `p`;
2. **the direction** — that a discrete lattice necessarily has more shape freedom than its continuum approximation, so the plate's answers are safe-side.

`C-0006` deserves credit for stating (2) as a falsifiable prediction about its own error rather than hiding the breach. `T-10` was raised to test it. It is half right, and the half that is wrong is the half `C-0006` asserted universally.

## The contradicting result

A beam-and-hinge grillage built from `C-0006`'s own ingredients — the same `EI`, `GJ`, `k_θ`, `d`, `p`, the same footprint, the same Winkler foundation, the same `k_f` sweep — and calibrated so that its long-wavelength limit reproduces `D_∥` and `D_k` **identically** and `D_⊥` to 1.5 %.

Peak dishing, nominal `k_f`, lattice against plate:

| load case | plate [nm] | lattice [nm] | ratio | `C-0006`'s prediction |
|---|---|---|---|---|
| uniform | 0.000 | 0.000 | — | exact in both |
| electrostatic edge taper, 50 % | 1.316 | **1.280** | **0.973** | **REFUTED** |
| 4 discrete anchors | 2.472 | **2.252** | **0.911** | **REFUTED** |
| 1 concentrated lever | 18.286 | **22.579** | **1.235** | confirmed |
| thermal, 300 K | 1.274 | **1.467** | **1.151** | confirmed |

Across the whole `k_f` ×[0.25, 4] sweep the anchored ratio runs 0.843–1.039 and the edge-taper ratio 0.944–0.994 — i.e. **the plate overstates the dishing** in both, by up to 16 %.

Provenance: `gpd/results/T-10-discrete-lattice-tile.json`, `structure.DiscreteLatticeTileStudyKt`, 21 gate-named tests green, whole suite green.

## Methodological grounds

### 1. The direction of the error is not a property of discreteness, it is a property of the load

The intuition behind `C-0006`'s bullet — *a lattice has more freedom than the continuum that approximates it* — is not generally true, and the counterexample is elementary.
A continuum plate is free to curve at **every** wavelength, including wavelengths shorter than the objects it is made of.
A single-layer origami sheet is made of 2 nm rods, and a rod does not bend across its own diameter. The lattice's kinematics — `w(x, y) = w_i(x) + φ_i(x)(y − y_i)` on each tributary strip, piecewise linear across `y` — remove that freedom, and removing freedom **stiffens**.

So there are two competing effects, and which wins depends on the load:

- **the lattice has *fewer* short-wavelength shape modes across the helices** than the plate, because a duplex is rigid in cross-section — this stiffens it;
- **the lattice has *softer* modes at the lattice scale** than the continuum `D q⁴` extrapolation predicts, because a discrete acoustic branch flattens toward the zone boundary — this softens it.

Under an **anchor** (a point *reaction* against a smooth load) and under a **smooth** non-uniformity, the first wins and the plate overstates the dishing by 1–16 %.
Under a **concentrated lever** (a point load *entering* one duplex) and under **`k_BT`** (which populates every mode, including the flat ones), the second wins and the plate understates it by 12–38 % and 11–20 %.

`C-0006` stated only the second mechanism and stated it as universal.

### 2. `ℓ_⊥/p` compares an across-helix length to an along-helix spacing

`C-0006`'s validity criterion pairs the bending length for curvature in `y` with the hinge spacing in `x`. Those are different directions and the ratio is not a discreteness criterion for anything.
Direction-matched there are two, and both differ from `C-0006`'s number by more than a factor of two:

| `k_f` × | `C-0006`'s `ℓ_⊥/p` | `ℓ_∥/p` (bending in `x` vs hinge spacing in `x`) | `ℓ_⊥/d` (bending in `y` vs duplex spacing in `y`) |
|---|---|---|---|
| 0.25 | 0.52 | **1.18** | **2.12** |
| **1.00** | **0.37** | **0.83** | **1.50** |
| 4.00 | 0.26 | 0.59 | 1.06 |

The breach is **real but milder, and in the other direction**. `ℓ_⊥/d ≥ 1.06` everywhere, so the sheet is always at least marginally a continuum across the helices — which is precisely why `C-0006`'s "~15 quasi-independent duplex beams" picture overstates the freedom, and why the plate turns out to be *unconservative* about across-helix stiffness rather than conservative.

The criterion that does fail is `ℓ_∥/p < 1` for `k_f ≥ 0.5 ×` `C-0001`: the hinges are further apart **along** the helices than the sheet can bridge. Stated without any `ℓ` at all: **an anchor's influence patch contains 2–8 crossovers, and 3.9 at the design point.**

### 3. The equal-sharing figure understates the peak by 2.3–7.6×, and one case reaches an allowable

`C-0006` reported **1.9 pN per crossover** from spreading the anchor force over the 9.3 load paths on an `ℓ`-contour, and declined to give a peak. The lattice gives the peak directly, and the anchor is carried by its **two nearest crossovers and essentially nothing else** — 5.63 pN at 3.03 nm, 2.73 pN at 4.87 nm, 1.12 pN at 8.27 nm.

The concentration factor is **2.3–7.6** over every anchored case in the sweep, and the worst case — `k_f` × 0.25, one anchor at ten times the layer stiffness — puts **11.54 pN on a single crossover**, which **reaches the 10–15 pN single-duplex unzip allowable**.

This is not a contradiction of `C-0006` (which declined the number) but it does make one of `C-0006`'s tables optimistic in use: its "minimum load paths" row *below 10 pN (single-duplex unzip) → 11 paths* is derived from equal sharing, and a discretely anchored design has to divide it by the concentration factor.

## What follows, and what does not

**Does not follow.** That any of `C-0006`'s verdicts changes. The rigid-plate assumption is still rejected; the uniform-load case is still exactly flat in both models; the single-lever case is still dead (and now on five counts rather than three); `no discrete attachment scheme is flat` is still true. The largest discrepancy found is 38 % and `C-0006`'s smallest rejection margin is a factor of 2.7. **`C-0006`'s arithmetic stands and its conclusions stand.**

**Does follow.**

1. **The validity bullet must be rewritten.** "Conservative about flatness" is true for the thermal and concentrated-load cases and **false** for the anchored and smooth-load cases. The direction of the plate's error depends on whether load enters at a point or is reacted at a point, and both directions are bounded here at ≤ 38 %.
2. **`ℓ_⊥/p` should be replaced** by the direction-matched pair `ℓ_∥/p` and `ℓ_⊥/d`, or better by the crossovers-per-anchor-patch count `π ℓ_∥ ℓ_⊥/(dp)`, which needs no direction convention and says the thing plainly.
3. **`C-0006`'s 55 attachments for flatness was a continuum heuristic and is 14 % optimistic.** Solved on both models the answer is **64**, against the tile's **56 crossovers** — 1.14 attachments per crossover. The conclusion is confirmed and strengthened.
4. **Any downstream task that puts a *point* load into the tile must use the lattice, not the plate**, because that is the case where the plate is unconservative by 12–38 % and the error grows with `k_f`. `T-2`'s design window and any lever-coupling design fall under this.
5. **The crossover phase is a design variable nobody owns.** Seven crossover columns instead of eight moves the peak per-path force by 19 %; where the anchor sits within a unit cell moves it by another 30 %. Both are set by the staple layout, i.e. for free.

## Resolution

`C-0006` is **not withdrawn and not overwritten**. Its continuum-plate validity bullet and its `ℓ_⊥/p` criterion are annotated in place with a pointer here, per the no-overwrite rule. The numbers stay; what changes is the stated direction of their error and the criterion by which the model's own validity is judged.

**Outstanding, and queued:**

- **`T-9` should be re-scoped.** `k_θ` does not decide any verdict in `C-0009` — every ratio moves by under 3 % across Chen et al.'s entire admissible `α ∈ [0.6, 1.2]`, and it takes the out-of-range `α = 25.6` isotropic probe to flip the anchored one. What `T-9` is now needed *for* is the per-path force budget, which rises with `k_θ`, and — at the same cost — the crossover's **vertical** compliance, which this lattice models as a rigid constraint and which is the way `C-0009` itself would fail.
- **`T-8` should consume the lattice's point RMS**, 1.433 nm at rest and nominal `k_f` against the plate's 1.364 nm, 2.281 nm against 2.237 nm at the soft end. Still inside the 3.0 nm predicate, 5 % worse than `C-0006` supplied.

## If this challenge is itself wrong

The way it fails is through the **vertical** stiffness of a crossover, which `C-0009` models as a rigid link between the two helix surfaces and verifies only as a converged *penalty*. Ground 1's stiffening mechanism depends on the sheet holding adjacent duplexes at a fixed relative height at every crossover; a crossover soft in `z` would add a load path the lattice does not have, soften the anchored response back toward the plate's, and lower the peak crossover force.

Nothing in the accessible literature measures it. The estimate that it does not matter — the hinge's equivalent vertical stiffness `k_θ/d² ≈ 1.9 pN/nm` against a strand axial stiffness of order `10³ pN/nm`, a ratio of 500 — is an *estimate*, not a measurement, and it is the single assumption in `C-0009` with no citation behind it at all.

**Grounds 2 and 3 survive that entirely.** The criterion-pairing error is a matter of which two lengths are compared and does not depend on any constant; the concentration factor is a statement about how few elements an anchor talks to, and a softer crossover would spread the load over *more* of them, lowering the factor but not restoring the contour average.
