# C-0030 — The standoff's off-diagonal does not soften the joint, it SUPPLIES the draw-in the joint was charged for; the buckling margin rises 1.55×, the window widens to 5–10 nm — and the sign of the whole effect is a mounting choice nobody had written down

| | |
|---|---|
| **Task** | [`T-65`](../tasks/T-65-coupled-standoff-joint.md) (and it closes [`T-41`](../../TASKS.md), which asked the same question one level up) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a 2 × 2 tip flexibility solved into `C-0025`'s beam, with Maxwell-Betti asserted between two independently integrated off-diagonals rather than constructed) **+ logical** (the coupling term is odd where the demand is even, so a single sign for the effect cannot exist — one mounting must gain what the other loses) |
| **Verdict** | **PASS, and `C-0028`'s window survives and WIDENS — conditionally on a design variable that did not previously exist.** `C-0028`'s bound was right and its *sign argument* was wrong in both halves. The off-diagonal is not a bounded correction to the two springs: at `C-0028`'s own design point the head's tilt under the beam's end moment supplies **0.886 nm** of draw-in per end against a chord demand of **0.287 nm** — **3.09×**, and **first order in the stroke where the demand is second order**. So the coupled beam is in axial **compression**, not tension, over the whole of `0 < s < 9.9 nm`; the standoff's duty at the desired stroke falls from **5.113 to 3.313 pN**; and the free-head buckling margin **rises from 1.41× to 2.18×** on CanDo's rigidity and **1.06× to 1.64×** on Fields et al.'s. The joint is not softer: against a *net* demand it is **2.06× stiffer** (`S_eff/S` 0.0144 → 0.0298). **But `Φδ` is odd and `e(δ)` is even, so the coupled law is no longer odd and the sign of everything above is decided by WHICH BODY CARRIES THE STANDOFFS.** Mounted the other way the same design has span 40.14 nm, tangent **44.82 pN/nm** — past `C-0023`'s ceiling — and margin **0.99× / 0.74×**: **`P3` fails at all eight lengths and the window is EMPTY.** **The recommended design is `B2` at `ℓ = 8 nm`, span 31.82 nm = 94 bp, favourable mounting, tangent 25.23 pN/nm, margin 2.18× (1.64× on the measured rigidity); the window is `ℓ = 5–10 nm` on both rigidities**, closed below by the **unzip allowable** — a third different constraint at that edge — and above by `C-0017`'s envelope. **And the favourable mounting has a price the decoupled reading could not see: the midspan sags TOWARD the body its bases stand on, so the standoff length is also a CLEARANCE — 5.31 nm of travel at `ℓ = 8` and 7.31 nm at `ℓ = 10`, so §3's *acceptable* 3 nm stroke is delivered at `ℓ ≥ 6 nm` and its *desired* 10 nm at no length in `C-0017`'s envelope.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED EITHER** — `C-0028`'s literature finding stands unchanged: no duplex has been built standing normal to a single-layer sheet, and every published out-of-plane base is a pin. |
| **Provenance** | `gpd/results/T-65-coupled-standoff-joint.json`, produced by `anchoring.CoupledStandoffJointStudyKt`; **12 flexibility records, 24 design records, 15 base-comparison records, 13 sensitivity records, 17 convergence records, 20 upstream reproductions**; **30 gate-named tests in `CoupledStandoffJointTest`, 223 in `anchoring`, 0 failures**, and the whole suite green on `tools/verify.sh` (`BUILD SUCCESSFUL`); an earlier isolated full-tree run measured **1014 tests, 0 failures** with a concurrent agent's then-half-written `PerpendicularJunctionTest.kt` removed from the snapshot, per `CLAUDE.md`; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; `EI = 230 pN·nm²` (CanDo model input) with **every buckling margin reported also on Fields et al.'s implied 172.9 pN·nm²** |
| **Consumes** | [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase`, the eight motifs, `baseRestraintParameter`, the two series reductions, `swayColumnDeterminant`, `beamHeadRestraint`, `seriesStiffness`, `offDiagonalCorrelation`/`offDiagonalFactor`, the six predicates, the recommended `B2` base and the 7–9 nm window — **re-run as a library**, its whole design table reproduced in the decoupled limit), [`C-0025`](C-0025-flexure-end-joint.md) (`c(ρ)`, `g(β)`, `S_eff`, `PartiallyRestrainedFlexure`, `flexureSpanForJoint` — its `J5-8` design reproduced to **0.0** and its `T(10)` to `1.2e−9`), [`C-0023`](C-0023-two-sided-coupling.md) (the flexure, the chord membrane term, the 40 pN/nm ceiling, the 45 paths, the sidedness test `carriesCompression`), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the envelope, the tangent/secant theorem this claim inverts), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ`, `k_s`, `EI`, `S`, the rise, the SAXS interhelical distance), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN), [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md) (the duty is the element's own) |
| **Raises** | [`CH-0041`](../challenges/CH-0041-the-standoff-supplies-the-draw-in-it-was-charged-for.md) against `C-0025` and `C-0028`, and [`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md) against `C-0017`'s theorem as consumed by `C-0023`, `C-0025` and `C-0028` |

---

## The claim, in one line

**`C-0028` bounded the off-diagonal correctly and read its consequence backwards: the term it dropped is not a compliance that softens the joint but a DRAW-IN THE JOINT SUPPLIES — the beam's own end moment tilts the standoff head inward, by `Φδ` per end, first order in the stroke against a second-order demand and 3.09× larger than it at the design point — so the flexure is in compression rather than tension over §3's whole range, its standoff's duty falls by a third, and the buckling margin `C-0028` feared for rises by 1.55×; and because `Φδ` is odd where the demand is even, the same physics has the OPPOSITE sign if the flexure is mounted the other way up, which turns "which body carries the standoffs" — a question no upstream claim asks and §3 does not answer — into the difference between a 5–10 nm window and no window at all.**

---

## The cheap bound, which ran first and decided the shape of the answer

One division, before any matrix and any root find, on `C-0028`'s own recommended design (`ℓ = 8 nm`, `B2`, span 31.06 nm).

| | quantity | value |
|---|---|---|
| the head's tilt supplies, per end, at the 3 nm placement stroke | `Φ·s`, `Φ = 24EI C12/(L²A)` = 0.2863 | **0.886 nm** |
| the chord geometry demands, per end, at the same stroke | `e(s) = √(a²+s²) − a` | **0.287 nm** |
| | **ratio** | **3.09×** |
| at the 10 nm desired stroke the ratio is | | **1.01×** |

> **The term `C-0025` and `C-0028` dropped is not a correction to the term they kept. It is three times larger than it, and it is FIRST order in the stroke where the demand is second order** — which is why it dominates at the placement point and why the two cross at 9.9 nm. Falsifier 2 (`Φδ ≪ e(δ)`, in which case the task closes on a division) **did not fire**, and the full solve was justified.

---

## Deliverable 1 — the 2 × 2 tip flexibility, and Maxwell-Betti as a test

For a standoff of rigidity `EI` and length `ℓ` on a base rotational spring `k_θb`, in the head coordinates `(u, φ)` work-conjugate to `(H, M)`:

&nbsp;&nbsp;&nbsp;&nbsp;`C11 = ℓ³/3EI + ℓ²/k_θb`, &nbsp; `C12 = C21 = ℓ²/2EI + ℓ/k_θb`, &nbsp; `C22 = ℓ/EI + 1/k_θb`.

| `ℓ` | base | `C11` [nm/pN] | `C12` [1/pN] | `C22` [1/(pN·nm)] | `r` | `1/(1−r²)` | sway, **other load zero** → **rotation fixed** | rotational, **other load zero** → **translation fixed** |
|---|---|---|---|---|---|---|---|---|
| 7 | `B2` | 0.68472 | 0.13332 | 0.034264 | 0.87043 | 4.126 | **1.4605** → 6.0263 | **29.185** → 120.43 |
| **8** | **`B2`** | **0.98708** | **0.16976** | **0.038612** | **0.86957** | **4.101** | **1.0131** → **4.1546** | **25.899** → **106.21** |
| 9 | `B2` | 1.36667 | 0.21055 | 0.042959 | 0.86894 | 4.083 | **0.7317** → 2.9873 | **23.278** → 95.033 |
| 8 | `B0` clamp | 0.74203 | 0.13913 | 0.034783 | **0.866025** | **4.0000** | 1.3477 → 5.3906 | 28.750 → 115.00 |
| 8 | `B1` one crossover | 5.47246 | 0.73043 | 0.108696 | **0.94707** | **9.7039** | 0.1827 → 1.7732 | 9.200 → 89.276 |

> **The two "other load zero" columns ARE `C-0028`'s two springs**, asserted to `1e−12` at every row — so the object is a strict generalisation and not a different model. `C-0028`'s bounds are recovered by the new object: `√3/2` and exactly 4 at a clamped base, **0.94707 and 9.7039** at a crossover base.

**Maxwell-Betti is asserted, not constructed.** `standoffTipFlexibilityByIntegration` computes `C12` as the tip **translation** under a unit tip **moment** — a *double* cumulative-Simpson integration of a constant curvature — and `C21` as the tip **rotation** under a unit tip **force** — a *single* integration of a linear one. Two different quadratures of two different integrands, over three lengths × three base stiffnesses:

&nbsp;&nbsp;&nbsp;&nbsp;**`|C12 − C21|/C12 = 0.0` at every one of the nine pairs**, and the integrated matrix reproduces the closed form entry by entry.

---

## Deliverable 2 — `c` and `S_eff` re-derived on the coupled joint

Closing the beam's two exact kinematic relations `M = 24EIδ/L² − 8EIθ/L` and `P_b = 192EIδ/L³ − 48EIθ/L²` with the joint's `θ = C12 T + C22 M`, `u = C11 T + C12 M` and the axial compatibility `e(δ) = Ta/S + u` gives, in closed form,

&nbsp;&nbsp;&nbsp;&nbsp;**`T(δ) = (e(δ) − Φδ)/G`**, &nbsp;&nbsp; **`R(δ) = c₀ EI δ/L³ + T(δ)·(2δ/r − 2Φ)`**,

&nbsp;&nbsp;&nbsp;&nbsp;`A = 1 + 8EI C22/L`, &nbsp; `c₀ = 48(A+3)/A`, &nbsp; `Φ = 24EI C12/(L²A)`, &nbsp; `G = a/S + C11 − 8EI C12²/(LA)`.

### `c` — the coefficient does not move at all, and that is the result

**`c₀ = 48(A+3)/A` is `C-0025`'s `c(ρ) = 192(ρ+2)/(ρ+8)` to the last digit**, coupled or not, asserted at nine `(ℓ, base)` pairs. The off-diagonal adds a term **proportional to the axial force** instead, so what moves is not `c` but the *concept*: the **effective** end condition becomes a function of the **stroke**.

| `ℓ` | `c` nominal (`C-0028`) | `c_eff` at **0** | `c_eff` at **3 nm** | `c_eff` at **10 nm** |
|---|---|---|---|---|
| 7 | 96.2 | ~127 | **125.2** (+30 %) | 87.7 (−8.8 %) |
| **8** | **92.5** | ~139 | **124.4 (+34 %)** | **92.2 (−0.4 %)** |
| 9 | 89.4 | ~145 | **123.1** (+38 %) | 95.2 (+6.5 %) |

> **`C-0025`'s discipline — "`c` is not a constant of the joint, it carries the span" — has to be extended: under coupling `c` is not a constant of the joint AND the span either. It carries the STROKE.** The sixth instance in this programme of a quantity that is not well posed without the state it is read at.

### `S_eff` — wrong by 2.06× in magnitude and wrong in **sign** over the working range

Two separate corrections, and they run opposite ways.

| | `C-0025`/`C-0028` | this claim | |
|---|---|---|---|
| against a **net** demand, `S_eff = a/G` | `S/(1 + 2S/(k_a L))` = **0.0144 S** | **0.0298 S** | **2.06× STIFFER** (1.97× at 7 nm, 2.14× at 9 nm) |
| the draw-in the joint **supplies**, `Φδ` | **not represented at all** | 0.886 nm per end at 3 nm | 3.09× the demand |
| the effective membrane modulus `aT/e(δ)` | `+0.0144 S`, constant | **negative below a 9.93 nm stroke** | the beam is in **compression** |

> **`S_eff` is not merely 2× wrong. It is a constant where the truth is a function, and it has the wrong sign over 99 % of §3's stroke.** The `−8EI C12²/(LA)` term stiffens the joint (the tension's rotation of the head releases the end moment, which costs draw-in back) and the `−Φδ` term reverses the tension; **`C-0028`'s argued "the coupled joint is softer" is falsified in both halves at once.**

---

## Deliverable 3 — the buckling margin, re-evaluated

`P_c` is `C-0028`'s own two-spring eigenvalue `sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u = 0` unchanged; what moves is the **duty**, which per `CH-0037` is the element's own end shear at the desired stroke and not the mandate secant.

| `ℓ` | model | span [nm] | bp | tangent | duty(10) | `P_c` free | **margin free, CanDo** | **margin free, Fields** | margin at `ρ_h` realised | verdict |
|---|---|---|---|---|---|---|---|---|---|---|
| 7 | decoupled (`C-0028`) | 31.76 | 93 | 37.69 | 5.655 | 9.16 | 1.62 | 1.22 | 2.21 | PASS |
| **7** | **coupled** | **31.76** | **93** | **25.09** | **3.814** | **9.16** | **2.40** | **1.81** | **3.28** | **PASS** |
| 8 | decoupled (`C-0028`) | 31.06 | 91 | 36.51 | 5.113 | 7.21 | **1.41** | **1.06** | 1.99 | PASS |
| **8** | **coupled** | **31.82** | **94** | **25.23** | **3.313** | **7.21** | **2.18** | **1.64** | **3.06** | **PASS** |
| 9 | decoupled (`C-0028`) | 30.51 | 90 | 35.72 | 4.753 | 5.82 | 1.22 | **0.92** | 1.79 | PASS / fails on Fields |
| **9** | **coupled** | **31.86** | **94** | **25.62** | **3.050** | **5.82** | **1.91** | **1.43** | **2.76** | **PASS** |
| 10 | decoupled (`C-0028`) | 30.05 | 88 | 35.17 | 4.506 | 4.80 | 1.06 | **0.80** | 1.63 | PASS / fails on Fields |
| **10** | **coupled** | **31.87** | **94** | **26.11** | **2.916** | **4.80** | **1.65** | **1.24** | **2.38** | **PASS** |

> **The margin rises by exactly the ratio the duty falls, 1.55× at the design point, and the whole of `C-0028`'s stated failure mode — "a ~1.4× softening in sway closes the window with no other number moving" — happens with the opposite sign.**
> **And it survives the measured rigidity.** `C-0028` records that on Fields et al.'s `EI` its 9 and 10 nm rows fall below one; coupled, every window length clears one on the measured rigidity too, at **1.24–1.89×**.

### `CH-0037` re-read: the correction reverses direction, which strengthens the challenge

`CH-0037` found the element's own duty **1.27–1.70×** *larger* than the mandate secant's 3.7037 pN. Coupled:

| `ℓ` | element duty / mandate duty, decoupled | **favourable** | **adverse** |
|---|---|---|---|
| 7 | 1.53 | **1.03** | 2.16 |
| 8 | 1.38 | **0.89** | 1.97 |
| 9 | 1.28 | **0.82** | 1.83 |

> **`CH-0037` is not weakened, it is sharpened: the mandate secant is not a conservative proxy that happens to be low, it is a proxy whose ERROR CHANGES SIGN with a design choice.** Reading a buckling duty off the mandate would now be non-conservative by up to 2× in one mounting and conservative by 20 % in the other.

---

## Deliverable 4 — the verdict on the window

### In the favourable mounting the window **widens**, and the constraint at its lower edge changes for the third time

| `ℓ` | 3 | 4 | **5** | **6** | **7** | **8** | **9** | **10** |
|---|---|---|---|---|---|---|---|---|
| tangent [pN/nm] | 47.77 | 33.97 | **27.84** | **25.64** | **25.09** | **25.23** | **25.62** | **26.11** |
| `T(10)` [pN] | +31.32 | +15.84 | **+7.66** | **+3.46** | **+1.25** | **+0.04** | **−0.64** | **−1.01** |
| margin, CanDo | 2.26 | 2.33 | **2.48** | **2.52** | **2.40** | **2.18** | **1.91** | **1.65** |
| margin, Fields | 1.70 | 1.75 | **1.87** | **1.89** | **1.81** | **1.64** | **1.43** | **1.24** |
| verdict | fail `P3` | **fail `P4`** | PASS | PASS | PASS | PASS | PASS | PASS |

> **The window is `ℓ = 5–10 nm` on CanDo's rigidity AND on Fields et al.'s** — against `C-0028`'s 7–9 nm — and it is closed from below by the **10 pN unzip allowable on the beam's own tension** (`P4`), not by the compliance ceiling that closed `C-0025`'s and not by the buckling that closed `C-0028`'s. **Three claims, three different constraints at the same edge.** Above, it is `C-0017`'s 10 nm envelope, unchanged.

### In the adverse mounting the window is **empty**

| `ℓ` | 3 | 4 | 5 | 6 | 7 | **8** | 9 | 10 |
|---|---|---|---|---|---|---|---|---|
| span [nm] | 48.88 | 46.14 | 44.06 | 42.45 | 41.18 | **40.14** | 39.26 | 38.51 |
| tangent [pN/nm] | 61.04 | 55.63 | 51.65 | 48.72 | 46.52 | **44.82** | 43.47 | 42.38 |
| duty(10) [pN] | 15.15 | 12.24 | 10.28 | 8.94 | 8.00 | **7.31** | 6.79 | 6.38 |
| margin, CanDo / Fields | 2.53 / 1.90 | 1.97 / 1.48 | 1.61 / 1.21 | 1.35 / 1.01 | 1.15 / 0.86 | **0.99 / 0.74** | 0.86 / 0.64 | 0.75 / 0.57 |
| verdict | \multicolumn — **`P3` fails at every one of the eight lengths**, and `P6` below one from 8 nm up | | | | | | | |

> **The same joint, the same base, the same standoff — mounted the other way up — puts the assembled tangent 6–53 % past `C-0023`'s compliance ceiling at every length in `C-0017`'s envelope, and takes the buckling margin below one at the design point.** There is no `ℓ` at which the adverse mounting passes.

### The price of the favourable mounting, reported beside the predicates and not adopted as one

The favourable sense is the one in which the midspan sags **toward** the body its standoff bases stand on — that is the sense in which a sagging beam pulls its supports together, exactly as a cable does. So the standoff length is also a **clearance**:

| `ℓ` [nm] | 5 | **6** | **7** | **8** | **9** | **10** |
|---|---|---|---|---|---|---|
| largest stroke that fits, `ℓ − 2.69 nm` | 2.31 | **3.31** | **4.31** | **5.31** | **6.31** | **7.31** |
| covers §3's **acceptable** 3 nm? | **no** | yes | yes | yes | yes | yes |
| covers §3's **desired** 10 nm? | no | **no** | **no** | **no** | **no** | **no** |

> **So the elastic window `ℓ = 5–10 nm` becomes `ℓ = 6–10 nm` once the clearance is respected, and §3's *desired* 10 nm stroke is unreachable in the favourable mounting at any `ℓ` inside `C-0017`'s 10 nm envelope** — it would need `ℓ ≥ 12.7 nm`. The adverse mounting has unlimited clearance and no window at all.
>
> **This is reported and NOT adopted as a predicate**, for the same reason `C-0025` reported buckling beside its five: it was not declared before the run, and it depends on a fact §3 does not state. If the standoff-carrying body is the solid 40 × 40 nm tile the ceiling is real; if it is `C-0017`'s unspecified superstructure it is a design choice. **A specification gap, not a modelling one** — the fourth in this programme, after §3's electrode material, its loading rate and now its mounting sense.

### The verdict

**The branch survives, and the answer is neither "survives" nor "closes" but "survives conditionally on a variable that was not in the design".** Mounted favourably it clears every predicate at `ℓ = 5–10 nm`, with `ℓ = 6–10 nm` once the clearance is respected, and delivers §3's **acceptable** clause with 2.18× of buckling margin (1.64× on the measured rigidity) and 31 % of compliance headroom. Mounted adversely there is no `ℓ` at which it passes. `T-66`'s triangulated standoff is **not** needed to rescue this branch — but it is still needed for `T-67`'s reason (whether a 90° routing exists at all), which this claim does not touch, and it is now also the only route to §3's **desired** 10 nm stroke, which neither mounting delivers.

---

## The nominal design that results

| | |
|---|---|
| **element** | transverse duplex flexure, tile tied at midspan, 45 on `C-0015`'s 3 × 15 grid |
| **span** | **31.82 nm = 94 bp** (was 31.06 nm = 91 bp) |
| **end joint** | a duplex standing normal to the sheet, **8.0 nm = 24 bp** |
| **base joint** | two antiparallel crossovers laid **ACROSS** the flexure axis (`C-0028`'s `B2`), `k_θb` = 261.2 pN·nm/rad |
| **mounting** | **the standoffs stand on the body the flexure bends AWAY from** — the new, free, and binding design variable |
| **end condition realised** | `c₀ = 92.5`, and `c_eff` = **124.4 at 3 nm**, 92.2 at 10 nm |
| **axial restraint realised** | `S_eff/S` = **0.0298** against a net demand (2.06× `C-0028`'s), and **+0.286 nm of draw-in supplied per nm of stroke per end** |
| **compliance** | tangent **25.23 pN/nm**, `t/s` = **0.757** — **31 % below** `C-0023`'s 40 pN/nm ceiling, and **strain-SOFTENING** |
| **assembled tangent minimum** | **22.88 pN/nm at a 4.55 nm stroke** — the quantity `C-0017`'s stability condition should now be read on |
| **delivered force at the desired stroke** | secant **29.81 pN/nm × 10 nm = 298 pN** (was 460 pN) |
| **beam axial force** | **−1.19 pN (compression) at 3 nm**, **+0.04 pN at 10 nm**; sign reverses at a **9.93 nm** stroke |
| **peak flexure compression / its braced Euler load** | **1.370 / 4.58 pN — 3.34×** (`P7`) |
| **standoff duty** | 1.111 pN at the held point (exact by placement), **3.313 pN at 10 nm** |
| **buckling** | `P_c` = **7.21 pN** free-head (**2.18×**, **1.64× on the measured rigidity**), 10.13 pN at the realised `ρ_h = 0.503` (**3.06×**) |
| **buckling stroke** | the standoff buckles at a **16.15 nm** stroke — 1.62× past §3's desired 10 nm (was 12.55 nm) |
| **transverse support** | 66.7 pN/nm against the beam's 0.741 — **90×**, unchanged (the coupling is in-plane and does not touch it) |
| **stroke clearance** | **5.31 nm** — covers §3's acceptable 3 nm, not its desired 10 nm; reported, not adopted |

---

## Two things `C-0028` asked to be respected rather than optimised away

### "The standoff's sway IS the flexure's draw-in" — upheld, and it is now the mechanism rather than the obstacle

The identity is re-asserted through the 2 × 2 (`1/C11` returns `C-0028`'s sway stiffness exactly) and it is **not** escaped. What the coupled model adds is that the *same* coordinate is driven by the beam's end **moment** as well as its tension, and the moment drives it the **helpful** way. So the head-held reading remains unavailable, the free-head buckling reading remains the predicate, and the design still cannot stiffen the sway without stiffening the draw-in — it simply turns out not to need to.

### Both ends of the membrane trade, checked

| | decoupled | coupled favourable | coupled adverse |
|---|---|---|---|
| tangent at 3 nm [pN/nm] | 36.51 | **25.23** (−31 %) | **44.82** (+23 %) |
| beam tension at 10 nm [pN] | +2.94 | **+0.04** | **+8.43** |
| bonded length that tension demands (`CH-0029`) | 3.9 bp | **none — the beam is in compression** | 12.4 bp |
| standoff duty at 10 nm [pN] | 5.113 | **3.313** | **7.307** |

> **The cable does not merely weaken in the favourable mounting, it inverts; and in the adverse mounting it is 2.9× stronger than `C-0028` credited.** The membrane term is the whole of the spread, and it is a term the two upstream claims computed with one of its two drivers missing.

---

## The five verification gates

Executed as **30 gate-named tests** in `src/test/kotlin/anchoring/CoupledStandoffJointTest.kt`; **223 `anchoring` tests, 0 failures**, and the whole suite green on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the three flexibility entries carry three different powers of the length (doubling `ℓ` multiplies them by exactly **8, 4 and 2** at a clamped base) and equal the textbook `ℓ³/3EI`, `ℓ²/2EI`, `ℓ/EI`; the correlation is **dimensionless and identical at four lengths and two rigidities**; `Φ` is a length ratio and strictly positive for any real standoff; a braced buckling load is `EI` over a squared span and **halving the span quadruples it exactly**; unphysical arguments throw, including a **pinned base**, which is `C-0028`'s mechanism and not a flexibility | **PASS** |
| **2 — limiting cases** | **the two "other load zero" diagonal readings reproduce `C-0028`'s `standoffSwayStiffness` and `standoffHeadRotationalStiffness` at three lengths**; **the decoupled flexure reproduces `C-0025`'s `PartiallyRestrainedFlexure` identically** — reaction, tangent, axial tension **and the negative limb** — at 3 lengths × 3 spans × 4 displacements; **the decoupled span root reproduces `flexureSpanForJoint`**; **`c₀ ≡ c(ρ)` at every restraint, coupled or not**, and strictly inside (48, 192); the braced eigenvalue is **exactly `π` at `ρ = 0` and exactly `2π` at `ρ = ∞`**, monotone between; a clamped base leaves the correlation at `√3/2` and the factor at **exactly 4**, on both springs | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the integrated flexibility matches the closed form to `< 1e−10` at four mesh levels and is **monotone**; the placed span is **exactly scan-step independent** over 64 → 2048 (`2e−16`); the analytic tangent matches a central difference at five deflections and **converges as the step refines** (`1.45e−6 → 1.57e−11`); the braced eigenvalue satisfies its own determinant to `4.9e−15` and is scan-independent over 64 → 1024; the placed span **returns its own target secant** in **both** mountings; **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature and upstream cross-check** | **`C-0025`'s `J5-8` reproduced to `0.0`** (span 31.6403748, `c` 95.6390226, tangent 37.3911226) and its `T(10)` to `1.2e−9`, its pinned-head buckling 8.8672227 to `0.0`; **`C-0028`'s `B2` design reproduced** (span 31.06, `c` 91.8, tangent 36.51, `T(10)` 2.94, duty 5.113, `P_c` 7.21, margin 1.41, `S_eff/S` 0.0141 — worst departure `9.1e−4`, which is the published rounding); **`C-0028`'s own off-diagonal bounds recovered by the new object** — `√3/2` to `1.3e−16`, factor **exactly 4**, and 0.947 / 9.70 at a crossover base to `4e−4` — **and shown equal to `C-0028`'s own two functions**, which are a different construction; **Fields et al.'s implied `EI` = 172.906**; both braced textbook `K` factors to **`0.0`**; the cheap bound asserted as a test (`supply > 2 × demand`, and the beam in compression at the placement point) | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **Maxwell-Betti between two independently integrated off-diagonals.** `C12` from a *double* cumulative-Simpson integration under a unit tip moment; `C21` from a *single* integration under a unit tip force. Nine `(ℓ, k_θb)` pairs, departure **`0.0`** at every one. Nothing in either construction imposes it.
2. **The matrix is positive definite and the correlation strictly below 1**, over four base stiffnesses × three lengths — so the "other-displacement-fixed" factor is a real finite number and not an artefact.
3. **The axial compatibility closes from both sides**, at six deflections including negative ones: the head's inward translation computed from the **joint** (`C11 T + C12 M`) equals what the **beam's** geometry demands minus the beam's own stretch, to `1e−9`. Two different expressions, neither used to derive the other.
4. **The coupled law is still SIGNED but is no longer ODD**, asserted directly: `carriesCompression` passes at every probe (so `C-0023`'s sidedness test survives), `R(s) > 0 > R(−s)`, and `|R(−s)| > |R(s)|` at every stroke — while the *decoupled* reading is asserted to be exactly odd in the same test. **The asymmetry is 1.88× at 3 nm and 3.9× at 10 nm.**
5. **`C-0028`'s "the sway is the draw-in" identity re-asserted through the new object**, and the reaction shown to be exactly the sum of its bending and membrane parts at four deflections.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the coupled model failing to reproduce `C-0025`/`C-0028` at `C12 → 0` | **no** | `C-0025`'s design to **`0.0`**, `C-0028`'s whole table to its own published rounding |
| 2 | `Φδ ≪ e(δ)`, i.e. the off-diagonal being a correction | **no, and inverted** | it is **3.09×** the demand it corrects |
| 3 | Maxwell-Betti failing on the two integrated off-diagonals | **no** | departure `0.0` at nine pairs |
| 4 | the coupling being sign-indifferent | **no — and this is the result** | one mounting widens the window to 5–10 nm and the other empties it |
| 5 | the coupled tangent going negative inside 0–10 nm | **no** | its minimum is **22.88 pN/nm at 4.55 nm**, positive throughout — but it is a *minimum*, which is `CH-0042` |
| 6 | **the coupled joint softening sway by ≥ 1.4×, closing the window** — `C-0028`'s own stated failure mode | **no, with the opposite sign** | against a net demand it is **2.06× stiffer**, and the margin **rises** 1.55× |

**The declared prediction held.** The task file recorded, before the code ran, that a single "softer" could not be right because the supply is odd and the demand even. It is not: the two mountings differ by a whole window.

**A result that was not anticipated:** the constraint at the window's **lower** edge changes for the third time — `C-0025`'s compliance ceiling, `C-0028`'s buckling, and now `C-0024`/`C-0006`'s **10 pN unzip allowable on the beam's own tension** at `ℓ = 4 nm`. And a **new predicate appears that the decoupled reading cannot even state**: the flexure is in axial compression, so it has its own stability condition (`P7`, 3.09–4.18× across the window).

---

## Sensitivities — what moves a verdict and what does not

| axis | range | margin (CanDo) | margin (Fields) | verdict moves? |
|---|---|---|---|---|
| **draw-in model** (`T-43`'s 1.13–1.20× debt) | chord → shape | 2.18 → **1.96** | 1.64 → 1.47 | **no** (10 % on the margin) |
| **`k_s`** (`C-0020`'s four decades, unmeasured) | ×1/32 → ×128 | **1.14** → 2.40 | **0.86** → 1.80 | **yes, at ×1/32 on the measured rigidity only** |
| **`α`** (Chen et al.'s own bracket) | 0.6 → 1.2 | 2.02 → 2.22 | 1.52 → 1.67 | **no** |
| **`EI` everywhere** (standoff *and* flexure) | 230 → 172.9 | 2.18 → **1.74** | 1.64 → 1.74 | **no** |
| **base motif**, coupled, at 8 nm | `B1` / `B2u` / `B2` / `B3` / `B0` | 0.60 / 0.99 / **2.18** / 2.35 / 2.40 | — | **no — `C-0028`'s base verdict is unchanged** |
| **mounting** | favourable → adverse | 2.18 → **0.99** | 1.64 → **0.74** | **YES, and it empties the window** |

> **`k_s` remains verdict-critical but the coupling buys about a factor of 4 in it**: decoupled, `P6` crossed one between `k_s/2` (1.24) and `k_s/8` (0.68); coupled it crosses between `k_s/8` (1.47) and `k_s/32` (1.14, but **0.86 on the measured rigidity**). `T-9` is still the task that settles it, and it is one notch less urgent than `C-0028` made it.
>
> **The base catalogue's verdict does not move at all.** The coupling helps every base by the same ~1.5×, so `B1` (0.36 → 0.60) and `B2u` (0.60 → 0.99) still fail and `B2`/`B3` still pass. **`C-0028`'s orientation finding — 9.65× for free — is untouched, and this claim adds a second free orientation one level up.**

---

## Does `C-0028`'s verdict survive?

**Yes, with its recommended design intact, its window widened, and its one *argued* number reversed.**

| `C-0028` said | this claim finds |
|---|---|
| the off-diagonal is bounded — correlation `√3/2` / 0.947, factor exactly 4 / 9.70 | **reproduced exactly** by the solved matrix, and by `C-0028`'s own two functions |
| the coupled joint is **softer**, so `P3` is conservative and `P6` is not | **falsified in both halves** — [`CH-0041`](../challenges/CH-0041-the-standoff-supplies-the-draw-in-it-was-charged-for.md). Against a net demand it is **2.06× stiffer**, and `P6` is the predicate that **improves** |
| "a ~1.4× softening in sway closes the window with no other number moving" | the effect is **1.55× in the other direction**, and the window widens |
| the recommended design is `B2` at 7–9 nm, span 31.06 nm, tangent 36.51, margin 1.41× | **`B2` at 5–10 nm, span 31.82 nm, tangent 25.23, margin 2.18× (1.64× measured)** — all inside `C-0023`'s brackets |
| the duty is the element's own, 1.27–1.70× the mandate (`CH-0037`) | **confirmed and sharpened**: the correction's **sign** depends on the mounting, 0.82–1.03× favourable and 1.83–2.16× adverse |
| on Fields et al.'s rigidity the 10 nm end falls below one | **it no longer does** — every window length clears one on the measured rigidity, at 1.24–1.89× |
| `k_s` moves a verdict for the first time in this programme | **still true**, but the crossing moves from `k_s/2`–`k_s/8` to `k_s/8`–`k_s/32` |
| the base's **orientation** is worth 9.65× and decides the design | **untouched**, and this claim finds a **second** free orientation one level up, worth a whole window |
| `T-13` still closes | **still closes, and by more** — see below |

### `T-13`, checked rather than inherited

`C-0023`'s zero-bias verdict rests on the coupling being **two-sided** and on `F_req = k_req σ` for a quadratic potential. The coupled law is still two-sided (`carriesCompression` passes at every probe) but **no longer symmetric**, so the well is asymmetric and its variance is set by the **softer** limb. In the favourable mounting the softer limb is the stroke limb, at **21.4–27.3 pN/nm assembled** against `C-0023`'s requirement of **0.4602 pN/nm** — **47–59× over**, where `C-0023` had 72×. **`T-13` closes, with the margin reduced from 72× to 47–59× and the argument now resting on an asymmetric well rather than a quadratic one.** That inheritance is `CH-0042`'s second half and is reported, not assumed.

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the MOTIF is not demonstrated either.** `C-0028`'s literature finding is unchanged and is upstream of every number: no duplex has been built standing normal to a single-layer sheet, every published out-of-plane base is a pin, and the only rigid out-of-plane mounting in print is triangulated (`T-66`, `T-67`).
- **SMALL DEFLECTION.** The standoff head's rotation at §3's **desired** 10 nm stroke is **0.63–0.68 rad** and its translation **32–41 % of its own length**, so every 10 nm column is a linear-theory extrapolation — exactly as `C-0023`, `C-0025` and `C-0028` flag, and no worse here. The **3 nm** columns (head rotation 0.13 rad, translation under 4 % of the length) are inside small deflection, and the placement condition, the tangent and the `P3` verdict all live there.
- **The elastica correction to the supplied draw-in is `(1 − θ²/12)`** — `−3.8 %` at the 10 nm stroke and `−0.15 %` at 3 nm — so `Φδ` is a slight **over**estimate at the desired stroke and essentially exact at the placement point.
- **The beam is solved with LINEAR bending kinematics plus `C-0023`'s chord membrane term**, unchanged. The geometric softening the compression produces is carried only through that chord term, whose implied flexure critical load is `12EI/L²` against the exact `π²EI/L²` — **22 % optimistic** — which is why `P7` is quoted against the **exact braced eigenvalue** and not against the model's own.
- **`T-43`'s 1.13–1.20× draw-in inconsistency is still open and is now more consequential**, because the supply and the demand are compared directly rather than only summed: chord → shape moves the margin 2.18 → 1.96 and the tension at 10 nm +0.04 → +1.46 pN. The **chord** demand is used everywhere a number is quoted, because it is what produces the tension in `C-0023`'s own force law.
- **The buckling head restraint is a bracket, not a number**: free (adopted, conservative) and the beam's own `2EI/L` (`ρ_h` = 0.44–0.57 realised). `C-0028`'s held-head reading remains **unavailable**.
- **Both mountings are reported and NEITHER is asserted to be the one §3 builds.** §3 does not say which body carries the standoffs; nothing upstream does either. This is a **specification gap**, not a modelling one — the third in this programme after §3's electrode material and its loading rate.
- **`k_s` is still `C-0020`'s derived, unmeasured construction** and the two-crossover couple rests on it entirely.
- **The sheet beyond the base crossovers is still treated as rigid** (`T-68`), and a compliant sheet **lowers** `k_θb`, which **raises** the correlation and therefore the coupling — i.e. it moves this claim's effect the favourable way and `C-0028`'s buckling the unfavourable way.
- **One flexure per load path and 45 attachments**, exactly as `C-0023`, `C-0025` and `C-0028` assume. The array is `T-31`'s and the lever `T-33`'s.
- **The favourable mounting's clearance is computed against a solid body one measured interhelical distance away.** It is real if the standoff-carrying body is the tile and a design choice if it is `C-0017`'s unspecified superstructure, so it is **reported beside** the predicates rather than adopted as one — the treatment `C-0025` gave buckling.
- **The out-of-plane offset between the beam's neutral axis and the standoff's tip is not modelled.** A ~1 nm offset would add `r·θ` — a further **+0.13 nm per end at 3 nm**, i.e. 15 % more supplied draw-in, in the favourable direction. Neglecting it is conservative for the favourable mounting and optimistic for the adverse one.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement**; every margin is reported also on Fields et al.'s implied **172.906** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| crossover hinge `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014), via `C-0009` |
| crossover in-plane `k_s = 2αS/(100a)` | 64.71 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, and a verdict still moves at `k_s/32` on the measured rigidity |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| duplex buckling at 40–41 bp under 9 pN | — | **CITED, MEASURED**, Fields, Meyer & Cohen, *NAR* **41**:9881 (2013), used **only** to produce the second rigidity |
| `C-0025`'s `J5-8` and `C-0028`'s `B2` designs | — | **CITED**, and reproduced here as gate-5 tests |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the 2 × 2 and its quadrature, `Φ`, `G`, `c₀`, the coupled force law, the braced eigenvalue, every span, tangent, axial force, duty, critical load, margin and window bound — is **derived here in code**, with `C-0025`'s and `C-0028`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Which body carries the standoffs.** It is free to a builder, it is worth a whole window, and nothing upstream specifies it. `T-75`.
2. **Whether a strain-SOFTENING coupling satisfies `C-0017`'s stability condition.** The assembled tangent's minimum is 21.4–27.3 pN/nm across the window against a secant placed at 33.333; `C-0018`'s fold margins are 19–42 % in `k_c/|k_eff|`. `CH-0042` / `T-76`.
3. **The flexure as a beam-column.** `P7` passes at 3.1–4.2× but is bounded, not solved; the compression is carried by `C-0023`'s chord term, which is 22 % optimistic about the beam's own critical load. `T-77`.
4. **A pre-bowed flexure (`T-42`) is now a different question**, because the built rise interacts with a *linear* supplied draw-in rather than only a quadratic demand.
5. **What the standoff-carrying body is, and therefore whether the favourable mounting's clearance ceiling binds.** `T-78`.
6. **Whether a 90° routing between a sheet duplex and a normal standoff exists at all** (`T-67`) — upstream of every number here, exactly as in `C-0028`.

## Challenges

**Raises [`CH-0041`](../challenges/CH-0041-the-standoff-supplies-the-draw-in-it-was-charged-for.md)** against `C-0025` and `C-0028` (the off-diagonal is not a compliance that softens the joint but a draw-in it supplies, larger than the demand it was set against, and both claims' `S_eff` is wrong in magnitude **and** in sign) and **[`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md)** against `C-0017`'s tangent/secant theorem as consumed by `C-0023`, `C-0025` and `C-0028`.

**No number in `C-0025` or `C-0028` fails to reproduce.** `C-0025`'s `J5-8` design is recovered to `0.0` and `C-0028`'s whole `B2` row to its own published rounding.

**None stands against this claim.** The four ways it would fail:

1. **A specification or a build showing the flexure is mounted adversely.** Then the window is empty and the branch goes to `T-66`. This is the single largest risk and it is a **specification gap**, not a modelling uncertainty.
2. **A demonstration that the standoff head and the beam end are not rigidly continuous** — a pin between them, which is what every published out-of-plane base actually is. `C12` would drop out and the answer would revert to `C-0028`'s, with `P_c = 0` besides.
3. **A large-deflection solve showing the supplied draw-in saturates faster than `(1 − θ²/12)`.** The 3 nm numbers would survive; the 10 nm ones would not.
4. **A measurement of `k_s` below `C-0020`'s construction by more than ~30×**, which takes `P6` below one on the measured rigidity even coupled.
