# T-326 — the reconstruction the dishing fit is taken in, against the one it is sampled in

**Leaf** `A8.2`.
**Raised by** [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md) §5 and [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) (`T-330`), which **priced it rather than took it**.
**Claim to be filed** `C-0221`. **Challenge numbers reserved** `CH-0285`, `CH-0288`. **Queue rows reserved** `T-327`, `T-335`.
**Owned this iteration** [`src/main/kotlin/tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt) and any result file this task moves.

---

## 0. Locked units, geometry and sign conventions

SI throughout, `k_BT = 4.142 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
Lengths nm, forces pN, stiffness pN/nm, pressure pN/nm² (= 1 MPa exactly).

Honeycomb `d = 2.536 nm` (SAXS).
In-plane raster-row pitch `p = 3d/2 = 3.804 nm`; layer pitch `d√3/2 = 2.19624042 nm`; rise `0.34 nm/bp`.
A block is `m` corrugated x-raster rows of `n` helices; the **face** is the column `faceColumn` (default `0`) the polymer layer confronts.
`s` runs along the helices, `y` across them in the plane of the face, `z` through the thickness; `W` is positive **downward**, `Φ` is the beam roll about its own axis, and the face field off a beam axis is `W + Φ·(y − y_beam)`.
The face `y` datum is `HoneycombGrillage`'s own, `yDatum = (min faceY + max faceY)/2`, and `lengthY = m·p` exactly.

Dishing is reported as a **fraction of the free stroke** on an `81 × 81` face grid, against `T-5b`'s `0.10`.

**The face's own ladder.** `HoneycombBlock.position` puts a face helix at `y_r = r·p + ½d·[(r + faceColumn) even]`, so at `faceColumn = 0` the gap sequence is `d, 2d, d, 2d, …` and the `d`-gaps are exactly the face's own **vertical bonds** — `verticalBondUp` is true on the lower member of each.
That pairing is the whole of what follows.

---

## 1. The statement

A face field lives on beam axes; off an axis the class must reconstruct it, and it has two reconstructions:

| | reconstruction | domain it is integrated over | used by |
|---|---|---|---|
| **A — owning beam** | inside `[y_r − p/2, y_r + p/2]` the field is `W_r + Φ_r(y − y_r)` | the same strips | `assembleLoad`, `faceFunctional`, `pistonDual`/`tiltSDual`/`tiltYDual`, and therefore `meanDeflection`, `tiltAlong`, `tiltAcross` |
| **B — nearest beam** | at `(s, y)` the field is reconstructed from whichever face beam's axis is **nearest** | still the owning strips (`integrateOverFace`) | `evaluate`, and therefore `dishing`, `peakDishing`, `areaInnerProduct` and every number this corpus reports |

At an **even** `m` — 15 of the 18 grillage-emitting result files, and every reading `C-0167`, `C-0180`, `C-0208`, `C-0211`, `C-0212`, `C-0215` and `C-0216` publish — `faceRigidCoefficients` returns the three `faceFunctional` projections, i.e. it **fits in A**; `dishing()` **samples in B**.
The three rigid modes reconstruct identically under both (`1`, `s`, `y` exactly), so the Gram is one object and the whole disagreement is in the right-hand side.

**The owning strips are not a partition.** Consecutive strips are `p = 1.5d` wide and their axes are `d` or `2d` apart, so they **overlap by `d/2`** across every vertical bond and **gap by `d/2`** between every pair of bonds.
Their total measure is exactly `m·p·L_s = L_y·L_s`, which is what makes `CLAUDE.md`'s uniform-load falsifier exact, and it is why `evaluate` cannot be made to use them: at a point in an overlap two beams own the field and at a point in a gap none does.

---

## 2. THE CHEAP BOUND — the gap is a CLOSED FORM in the face's own vertical bonds, and it is exact

Within the owning strip of beam `r`, the **nearest**-beam partition is the owning strip translated by `δ_r = ±d/4`, the sign alternating with the ladder, so each strip is split `5d/4` to its own beam and `d/4` to the partner across its **vertical bond**.
Summing the two members of one bond, the `W` differences cancel identically and what survives is the bond's **relative roll**:

```
piston   <1, B(u)> - <1, A(u)>  =  (d^2/16) * SUM over face vertical bonds of INT_s (phi_upper - phi_lower) ds
tiltS    <s, B(u)> - <s, A(u)>  =  (d^2/16) * SUM over face vertical bonds of INT_s  s*(phi_upper - phi_lower) ds
tiltY    <y, B(u)> - <y, A(u)>  =  SUM over face vertical bonds of INT_s [ (d^2/16)((w_u - w_l) + ybar*(phi_u - phi_l)) - (d^3/32)(phi_u + phi_l) ] ds
```

with `ybar = (y_u + y_l)/2` the bond's own midpoint.
**Verified in Python before this file was written**, against a direct quadrature of both reconstructions, at `m = 4, 6, 10, 14` and both face columns: 8 of 8 configurations agree on random `(W, Φ)`.
*(Amended at Execute: that first pass used a uniform trapezoid grid and agreed only to 9–10 digits, because the nearest-beam reconstruction is **discontinuous** at every cell boundary and a smooth rule is first order there. Integrating each linear piece in closed form takes the same check below `1e-12` — and the same fact, turned on the shipped `areaInnerProduct`, is `CH-0285`.)*
Both forms annihilate a pure piston and a pure `y` exactly, which is the limiting-case check.

**Three consequences, all free:**

1. **The discrepancy is a bond-hinge coordinate.** The `d`-gap pairs *are* the face's covalent vertical bonds, whose hinge is the only thing resisting a relative roll — so the gap is small under any load the bonds resist and large under a load applied **to** a bond. That is a mechanism for `CH-0284`'s own split (`5.0E-4` on the collar against `6.7E-3` on a bond prestrain) and not merely a restatement of it.
2. **The leading order cancels.** For a smooth field `Φ ≈ ∂w/∂y` and the `O(d²·∂w/∂y)` term vanishes identically; what survives is `Δ_bond ≈ (d³/32)·∂²w/∂y²`, so the relative gap on a projection is
   ```
   relative gap  ~  (pi^2/12) * (d / lambda_y)^2
   ```
   with `λ_y` the **dishing** field's across-face wavelength. At `λ_y = 2L_y = 30d` (a face-scale half-cosine) that is `9.1E-4` against `CH-0284`'s measured `4.3E-4`–`5.0E-4`; and the measured prestrain channel `6.7E-3` **inverts** to `λ_y = 11.1d = 0.74 L_y`, a field carrying `2.7×` the collar's across-face curvature. That inversion is an arithmetic consequence of the asymptotic and not evidence for it; `P4` is what tests whether the two independently measured channels lie on one `(d/λ_y)²` line.
3. **There is a THIRD convention and it is the consistent one.** Neither of `CH-0284` §4's first two remedies matches the fit to the sample: remedy 1 is not well posed (§1), and remedy 2 (`areaInnerProduct`) fixes the *reconstruction* while leaving the *measure* the overlapping, gapping tributary sum. The quantity the class **reports** is a sup over the face **rectangle** `[−L_s/2, L_s/2] × [−L_y/2, L_y/2]` of the **nearest**-beam reconstruction, so the fit consistent with it is
   ```
   C  —  nearest-beam reconstruction, integrated over the face rectangle
   ```
   For the piston projection the three conventions are **collinear in one scalar**, the summed bond relative roll, with coefficients
   ```
   A : B : C  =  0 : 1 : 6      (measured 6.000000 at m = 4, 6, 10, 14, 16, faceColumn 0)
   ```
   so the fit/sample gap `CH-0284` prices is **one sixth** of the convention family's own width. At `faceColumn = 1` and at odd `m` the end beams break the collinearity and the ratio is field-dependent (`3.18`–`11.50` over the same sweep); that is reported, not asserted.

### 2b. And convention C DISSOLVES `CH-0282` rather than repairing it

Under C the Gram is `∫∫ over a rectangle symmetric in both coordinates`, so

```
<piston, tiltY> = INT y dA = 0 ,  <piston, tiltS> = INT s dA = 0 ,  <tiltS, tiltY> = INT s y dA = 0
```

**identically, at every `m`, every face column and every row length**, with `G = diag(A, A·L_s²/12, A·L_y²/12)` in closed form.
The parity `C-0219` had to branch on is an artefact of taking the fit in the **tributary** measure, whose `∫y dA = L_s·p·Σ beamY` is what fails to vanish at odd `m`.
If C is adopted, `faceRigidModesAreOrthogonal` is unconditionally true, the integer branch becomes dead, and the three independent projections **are** the least-squares fit again — in a different inner product.

### 2c. The margin the refusal must be priced against is NOT `C-0180`'s, and that is one pass over the corpus

`CH-0284` prices the collar channel as *"inside the margin by a factor of 8.5"* against `C-0180`'s tightest recovered cell, `0.0995744767`, which clears `T-5b` by `0.426 %`.
Censused over all eighteen files — every numeric leaf whose key ends `OverStroke` or contains `ishing`, in a record that also carries a boolean, with a value in `[0.09, 0.11]`, so that a **verdict** is written on it:

| | |
|---|---|
| verdict-bearing readings in the window | **1 146** |
| tightest | **`0.10000102`**, `T-294/cells/92/nominalCorrectedOverStroke`, **`1.02E-5`** relative from `T-5b` |
| within `5.0E-4` — the **collar** channel | **2** |
| within `4.2724E-3` — the movement that would flip `C-0180`'s tightest cell | **96** |
| within `4.57E-3` — `C-0180`'s own measured beam-subdivision convergence departure, `4.57E-4` of the stroke | **99** |
| within `6.7E-3` — the **prestrain** channel | **126** |
| within `4.02E-2` — the prestrain channel at convention C's `6×` | **484** |

So the corpus's tightest verdict-bearing reading sits **`1.020E-6` of the stroke** from `T-5b` against `C-0180`'s **`4.255E-4`** — **417× tighter** than the margin the challenge priced against — and **the collar channel is `49×` outside it** (`5.0E-4` against `1.02E-5`, both relative).
There is therefore **no channel on which adoption is safe**, and the decision cannot be made by showing that the movement is small.
Its twin is as sharp and runs the other way: **99 of the 1 146 sit closer to `T-5b` than the convergence departure `C-0180` measured on this very lattice**, so those verdicts are not determined by the model at all and the fit convention is one more term in the same bucket.

### 2d. AND THE CLASS'S OWN QUADRATURE UNDER-REPORTS THE GAP BY A CONSTANT `1.22×`, BECAUSE IT INTEGRATES A DISCONTINUOUS INTEGRAND WITH A SMOOTH RULE

`areaInnerProduct` is `integrateOverFace { evaluate(a)·evaluate(b) }`, and `integrateOverFace` lays **6-point Gauss-Legendre** (`QUADRATURE_POINTS = 6`) across each whole tributary strip.
But `evaluate` is the **nearest**-beam reconstruction, which is **discontinuous** at every cell boundary — and a boundary falls `d/4` inside each strip's end, at every strip, by construction (§2).
So a smooth rule is being applied to a jump, and the error is not a rounding.

Measured, exact piecewise integration against the class's own rule, on the piston gap:

```
gauss6 / exact  =  0.819694     at every one of 20 readings, m = 4, 6, 10, 14, 15, both face columns
```

Constant to six digits, and it must be: both readings are linear functionals of `(W, Φ)` and the bond pairing reduces each to a multiple of the same `Σ (Φ_upper − Φ_lower)`, so their ratio is a pure number carrying no field.

Two consequences:

1. **`CH-0284`'s own published channel sizes are `1.22×` low.** Its `4.3E-4`–`5.0E-4`, `4.7E-4` and `0.0067` are differences taken against `unconditionalFaceRigidCoefficients`, which reads the `areaInnerProduct` right-hand side; the true fit/sample gap is `1/0.819694 = 1.2200×` larger — about `5.2E-4`–`6.1E-4` on the collar and **`8.2E-3`** on the prestrain. That is a correction to the number the refusal was priced on, and it is candidate **`CH-0285`**.
2. **Whichever convention is adopted, its quadrature must split at the nearest-beam boundaries**, or the adopted fit carries an 18 % error in exactly the term this task is about. The addition in §5a does that by construction; `C-0219`'s odd-`m` corrected coefficients do not, and how far that moves its committed `15 × 4` triple is `P12`.

---

---

## 3. Numeric targets

| | target |
|---|---|
| **P1** | the three closed forms of §2 reproduce the direct difference `B − A` of the two quadratures to `< 1e-10` relative, at every `m` in `3…16`, both face columns, on at least three independent random fields each — and exactly `0.0` on a pure piston and a pure `y` |
| **P2** | convention C's Gram is diagonal to `< 1e-12` relative worst off-diagonal at every `m` in `3…16` and both face columns, and equals the closed form `diag(A, A·L_s²/12, A·L_y²/12)` to `< 1e-9`; `worstFaceNonOrthogonality` under C is `0` where under B it is `0.0358744468` at `m = 15` and `0.0475958489` at `m = 11` |
| **P3** | the piston collinearity `A : B : C = 0 : 1 : 6` holds to `< 1e-6` at every **even** `m` in `4…16` at `faceColumn = 0`; the ratio at `faceColumn = 1` and at odd `m` is **reported** with its spread rather than asserted |
| **P4** | the asymptotic `(π²/12)(d/λ_y)²` reproduces `CH-0284`'s four measured channels within a factor of **3**, and the prestrain/collar **ratio** within a factor of **2** |
| **P5** | the margin census of §2c reproduces exactly — `1 146 / 0.10000102 / 2 / 96 / 99 / 126 / 484` — emitted from the committed files with the predicate stated in the file |
| **P6** | the relative movement of **peak dishing** under B and under C, measured at: `C-0022`'s solved collar at all three `10 × 6` enhancements, a face point load, a unit bond prestrain, and the `15 × 4` free tiles. B's readings reproduce `C-0219`'s `4.3E-4`–`5.0E-4`, `4.7E-4`, `0.0067` |
| **P7** | the movement at **`C-0180`'s two recovered coupled cells** — `p90OverStroke` `0.0995744767` and `0.0998791032`, the tied `abstract grid`, 3 columns, 30 paths, `rim-graded 5:1`, enhancement `21.1851817`, seed `197197`, 4 000 realisations — under B and under C, read **per realisation** on the one common stream, with the untied and tied `p90` reproducing `C-0180` to `< 1e-8` before anything moves |
| **P8** | a **rigorous ceiling** on the movement of any peak dishing of a given field, `\|Δc₀\| + \|Δc₁\|·L_s/2 + \|Δc₂\|·L_y/2`, emitted beside the measured movement at every cell of `P6` and `P7`, with the ceiling never below the measurement |
| **P9** | the code addition is **provably inert**: every accessor added to `HoneycombGrillage` is new, nothing existing is repointed, and at least three `m = 10` result files re-run as controls are **byte-identical** |
| **P11** | the `gauss6 / exact` ratio on the piston gap is a **constant** `0.819694` — independent of `m`, of the face column and of the field — reproduced in Kotlin against the shipped `areaInnerProduct`, and the corrected channel sizes `1.2200×` `CH-0284`'s |
| **P12** | how far the split quadrature moves `C-0219`'s committed odd-`m` triple `0.242196276 / 0.157167743 / 0.150056485`, which was fitted through the unsplit rule — reported whether or not it is adopted |
| **P10** | the decision, with its price: whether the class is repointed at one reconstruction, and if so the movement at every one of the **18** result files; if not, the channel-by-channel price and the follow-up row that carries the adoption |

---

## 4. Falsifiers, declared before the run

| | falsifier | state |
|---|---|---|
| **F1** | any of the three closed forms disagrees with the direct quadrature difference beyond `1e-10` relative at any `(m, faceColumn)` — then §2's derivation is wrong and the whole cheap bound is void | **OPEN** |
| **F2** | convention C's Gram is **not** diagonal at some `m` or face column — then *"C dissolves the parity"* is false and `C-0219`'s branch is not an artefact of the measure | **OPEN** |
| **F3** | the piston ratio `(C − A)/(B − A)` is not `6` at some even `m`, `faceColumn = 0` — then the collinearity is a coincidence of the `m` it was checked at | **OPEN** |
| **F4** | the asymptotic mispredicts a measured channel by more than **10×** — then the `(d/λ_y)²` scaling is not the mechanism and the bond-relative-roll story explains nothing | **OPEN** |
| **F5** | the movement at `C-0180`'s tightest recovered cell exceeds its `0.426 %` margin under **either** convention — then a live verdict moves and the refusal can never be *"it is inside the margin"* | **OPEN**, and it is **expected to fire under C** |
| **F6** | **no** verdict-bearing reading moves at all under either convention — then adoption is free, the refusal has no ground, and shape 1 is mandatory | **OPEN** |
| **F7** | the owning strips **do** form a partition of the face, i.e. `CH-0284` §4's first remedy is well posed | **declared CLOSED** — consecutive strips overlap by `d/2` and gap by `d/2` alternately, which is arithmetic on `p = 1.5d` against gaps of `d` and `2d`; asserted as a named test rather than argued |
| **F8** | a control `m = 10` result file re-run against the additive-only code is **not** byte-identical — then *"provably inert"* is false | **OPEN** |
| **F9** | two independent emissions of `T-326` are not byte-identical, **diffed outside the study** (`CH-0281`; every departure this study emits must reach `DEPARTURE_DIGITS_BY_KEY`) | **OPEN** |
| **F10** | any mutation of the new code survives every named test, over a **subtracted** baseline (`CH-0237`), with the harness registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts` | **OPEN** |
| **F11** | the uniform-load falsifier fails under **any** of the three conventions, at both parities of `m` | **declared CLOSED**; a failure is a defect in the addition |
| **F13** | the `gauss6 / exact` ratio is **not** constant across `m`, face column and field — then §2d's proportionality argument is wrong and the `1.22×` correction to `CH-0284` cannot be carried as a single factor | **OPEN** |
| **F14** | the split quadrature moves `C-0219`'s committed `15 × 4` triple by more than its own emitted precision — then `C-0219`'s numbers need re-emitting whatever this task decides about the convention | **OPEN** |
| **F12** | the reproduction of `C-0180`'s two recovered cells (`P7`, before any convention change) departs by more than `1e-8` — then the deciding cell has not been rebuilt and `P7` measures a different object | **OPEN** |

---

## 5. Method, and its justification against cost

### 5a. What is added, and why nothing existing is repointed

`HoneycombGrillage` gains, all lazy or pure and all **new**:

- `faceVerticalBondPairs: List<Pair<Int, Int>>` — the face's `d`-gap pairs, derived from the ladder, with a `require` that every gap is `d` or `2d`;
- `reconstructionGapFunctional(mode): F64Array` — the sparse dual of §2's closed form, one per rigid mode, so `B − A` on any field is a **dot product**;
- `faceSampledInnerProduct(a, b)` — convention C: `evaluate(a)·evaluate(b)` integrated over the face rectangle, taken cell by cell on the nearest-beam partition clipped to `[−L_y/2, L_y/2]` so the piecewise reconstruction is integrated **exactly** in `y` (§2d — the integrand is discontinuous and a whole-strip Gauss rule is 18 % wrong on the discontinuous part);
- `splitFaceInnerProduct(a, b)` — convention B **with the same split**, so that B and C are compared on quadratures of equal accuracy and the `1.22×` of §2d is separated from the convention itself;
- `faceSampledGram`, `sampledFaceRigidCoefficients(field)`, and `worstSampledFaceNonOrthogonality`.

`dishingCoefficients` is **not** repointed in this step.
That makes the whole addition inert by construction — `P9`, and it is what lets the measurement of a change be taken before the change is made.

### 5b. The three deliverables, in cost order

1. **The closed forms and the convention family** (`P1`–`P4`). No solve: `m = 3…16` at one helix per row, random fields, a dot product against a quadrature. Milliseconds.
2. **The margin census** (`P5`). One pass over eighteen committed JSON files. Seconds, and it is already prototyped — the numbers in §2c are its output.
3. **The deciding cells** (`P6`–`P8`). The expensive part is the influence bank: `n + 1` banded solves at 4 320 unknowns, half-bandwidth 243. **Both conventions share every solve** — the fields are identical and only the fit differs — so measuring B, C and A costs exactly one bank, and the 4 000-realisation ensemble on that bank is cheap. `C-0180`'s deciding cells are fully specified in its own committed result file (`tied`, enhancement `21.1851817`, `abstract grid`, 3 columns, 30 paths, `rim-graded 5:1`), so they are reconstructible without owning `T-279`'s study.

### 5c. Why the movement is affine, and why that gives a ceiling for nothing

Changing the convention changes only the three coefficients, so the dishing field moves by `−(Δc₀ + Δc₁ s + Δc₂ y)` — an **affine** function of position.
Hence `|peak_new − peak_old| ≤ |Δc₀| + |Δc₁|L_s/2 + |Δc₂|L_y/2` **rigorously**, by the reverse triangle inequality on sup norms, and for a linear surrogate bank the ceiling superposes over the bank.
That is `P8`, it needs no extra solve, and it is what makes a statement about cells that are *not* re-run possible at all.

### 5d. The scope gate, stated before the run

**An adoption without the sweep is not admissible.** A repointed `dishingCoefficients` leaves eighteen committed files that their own code no longer reproduces, which is the defect `gpd/README.md`'s re-run rule exists to prevent. So the choice is binary: adopt **and** re-emit all eighteen in `tools/reemission-order.py`'s topological order, or do not adopt.

The gate is `F5`/`F6` plus a measured cost:

- if **`F6` fires** (nothing moves anywhere) the adoption is free and is taken;
- otherwise the adoption is taken **only if** the eighteen re-emissions and their consumers fit the iteration with the verification budget intact. The three heaviest are `T-315` (2.1 MB), `T-307` (1.0 MB) and `T-299` (692 kB), and their wall times will be **measured on the first re-emission**, not guessed;
- if they do not fit, the deliverable is `P1`–`P9` plus a recorded decision with the price, the code left **additive-only**, and the adoption filed as **`T-335`** — a strictly better-specified row than `T-326` was, because it will carry the convention to adopt, the closed form, the ceiling and the deciding-cell measurement.

**What will be cut first if it overruns**, and logged: the `faceColumn = 1` half of `P3`'s sweep, `P6`'s `15 × 4` free tiles, and `P7`'s second recovered cell — in that order. `P1`, `P2`, `P5`, `P7`'s tightest cell and `P9` are not cuttable; without them the task has measured nothing.

### 5e. What would falsify this approach as a whole

If `F2` fires — if convention C's Gram is not diagonal — then C is not the canonical fit, the family is not `0 : 1 : 6`, and the question reverts to `CH-0284`'s own two remedies with only the closed form added.
If `F1` fires the cheap bound is wrong and the task is a measurement with no theory.
If `F5` does **not** fire under either convention, the adoption's risk is bounded by `C-0180`'s margin after all and the case for taking it this iteration is much stronger than §2c suggests.

---

## 6. Deliverable hand-off — reported, never edited

`ANSWERS.md`, `DECISIONS-FOR-NDI.md`, `TASKS.md`, `JOURNAL.md` and `CLAUDE.md` are not edited by this task.
Any passage that moves is reported to the coordinator as an exact substitution, with the file and the line at the `HEAD` it was read at.
`CH-0284`'s own §2 sentence — *"the collar reading is inside that margin by a factor of `8.5`"* — is the first candidate, because §2c prices it against a margin `417×` tighter.

---

## 7. Execute

Filed as [`C-0221`](../claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md), which **ANSWERS** [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md) and raises [`CH-0285`](../challenges/CH-0285-a-smooth-rule-across-a-discontinuous-reconstruction.md).
Result: [`gpd/results/T-326-the-fit-and-the-sample-in-one-reconstruction.json`](../results/T-326-the-fit-and-the-sample-in-one-reconstruction.json), written by `tile/FaceReconstructionStudy.kt` on `tile/HoneycombGrillage.kt`.

**16 gate-named tests written first and watched fail** (`src/test/kotlin/tile/FaceReconstructionTest.kt`), all sixteen passing on their first real run — which is unusual here and is explained by the retained Python prototype having predicted every one of their constants before a line of Kotlin existed.

### The twelve targets

| | result |
|---|---|
| **P1** | met — the closed form holds below the declared `1e-12` at **28 of 28** rows and **252** readings, and exactly zero on both limiting cases |
| **P2** | met — convention C's Gram is diagonal at **28 of 28** readings against the standing convention's **14** |
| **P3** | met — the piston collinearity is exactly **`6.0`** at **5 of 5** even `m` at `faceColumn 0`; `2.04`–`6.11` elsewhere, emitted rather than asserted |
| **P4** | met — the asymptotic `9.1E-4` against a measured collar of `6.1E-4`, well inside the declared factor of 3 |
| **P5** | met — **`1 146`** verdict-bearing readings; tightest **`0.10000102`**; **`1 / 2 / 96 / 99 / 126 / 484`** |
| **P6** | met — on the `10 × 6` face the worst movement is **`0.0501`** on C and **`0.00813`** on B, and over the smooth load cases alone **`0.00365`** on C |
| **P7** | met — both cells reproduce `C-0180` at **`3.5E-10`** and move by at most **`0.00143`**; the verdict moves at **0 of 2** |
| **P8** | met — the affine ceiling holds at **8 of 8** channels and is **tight**, `0.00365` against a measured `0.0036` |
| **P9** | met — **224 insertions, 0 deletions** in `HoneycombGrillage.kt`; **3 of 3** byte-identity controls (`T-253`, `T-267`, `T-263`) |
| **P10** | met — the decision is recorded and the code is left additive only; the adoption is `T-335` |
| **P11** | met — `gauss6/exact` is **`0.819693683`** at all **12** readings; `CH-0284`'s channel sizes are **`1.21997×`** low |
| **P12** | met — the split moves `C-0219`'s triple by at most **`6.33E-5`** relative, and the verdict moves at **0 of 3** |

### The fourteen falsifiers

`F1`, `F2`, `F3`, `F4`, `F6`, `F8`, `F12`, `F13` did not fire; `F7` and `F11` were declared closed and hold.
**`F5` was declared OPEN *and declared expected to fire under C*, and it did not** — that is the finding of `C-0221` §4: an influence function is a **basis element** and a coupled cell is a **state**, and the two differ by `35.0349650×`.
**`F9` FIRED TWICE** (`C-0221` §7, `gpd/data/T-326-reproducibility/`), the second firing refining `CLAUDE.md`'s own rule: an **order** is not a threshold either.
**`F14` FIRED**, at `6.33E-5`, moving no verdict.

### What was cut

Nothing. The whole study runs in **78 seconds**, because all four conventions share every solve — the fields are identical and only the fit differs.
