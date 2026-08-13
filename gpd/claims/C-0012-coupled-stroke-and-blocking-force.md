# C-0012 — The coupled stroke and blocking force of the Gen-1 actuator, and the operating point the §6 predicate actually lands on

| | |
|---|---|
| **Task** | [`T-3`](../tasks/T-3-stroke-and-blocking-force.md) |
| **Leaf** | `A2.2` |
| **Verification type** | in-silico (nonlinear Poisson-Boltzmann force curve × crossover-valid grafted-layer free energy, coupled by a bracketed 1-D root find) + logical |
| **Verdict** | **PASS on all three acceptance clauses — and the answer is a conditional one.** See "The verdict, stated carefully". |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** And *within* mean field: `C-0005` puts the one-loop correction at 123–214 % of the leading term across this gap range, so every force here inherits an error that is not bounded by its own expansion. |
| **Provenance** | `gpd/results/T-3-stroke-and-blocking-force.json`, produced by `actuator.StrokeAndBlockingForceStudyKt`; 810 coupled operating points, 90 threshold records, 54 layer-medium records, 12 convergence records; 29 new `actuator` tests, 608 in the suite; the result file re-run and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at 0.5 / 1 / 2 / 5 / 10 mM; 40 × 40 × 10 nm Manning-renormalised tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` models |
| **Consumes** | [`C-0008`](C-0008-electrostatic-force-and-decay-length.md), [`C-0003`](C-0003-crossover-valid-layer-response.md), [`C-0004`](C-0004-poroelastic-drainage.md), [`C-0005`](C-0005-mean-field-screening-validity.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md), [`C-0007`](C-0007-solvent-quality-vs-salt.md) |
| **Raises** | [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md) against `C-0008` |

---

## The claim

**§6 task 3's predicate is met, at a bias between 0.08 and 0.37 V — five to twenty-five times below §3's ceiling
and inside `CH-0007`'s ~1 V point-ion boundary. But it is met at an operating point that is *statically unstable
against a dead load* at 7 and 10 nm, and the unloaded actuator does not sit anywhere near it: it snaps to
near-contact. The Gen-1 stack as specified is a bistable switch with a stiffness-dependent hold, not the
proportional linear actuator §1 describes.**

Three subsidiary statements, each of which replaces an inherited one:

1. **The blocking force is not the peak output force.** `dW/dh = k_eff`, so wherever the field softens the layer
   the force-displacement characteristic *rises* with stroke. `W(0)` understates what the actuator delivers.
2. **`k_es < 0` is not universal.** It reverses below the force maximum, at gaps `C-0008` did not sample, and
   that reversal is the mechanism that arrests the collapse. `CH-0011`.
3. **Bandwidth is not the constraint; static stability is.** At the 5 nm design point the loaded drainage corner
   is 1.1–2.3 MHz against a 1 kHz requirement; at 7 and 10 nm there is no corner at all, because `k_eff < 0`.

---

## The numbers

### The design points, and the layer they carry

`L₀` is reproduced to 1e−4 nm at every model — the height relation is inverted, not assumed.

| `L₀` | `σ` [nm⁻²] | `N` (six-model bracket) | `φ(L₀)` | `k(L₀)` [pN/nm] | `k(0.8 L₀)` [pN/nm] |
|---|---|---|---|---|---|
| 5 nm | 0.092 | 67.1 – 119.6 | 0.0745 – 0.1328 | **0 or 117.9 – 184.5** | 83.9 – 353.6 |
| 7 nm | 0.045 | 123.6 – 212.5 | 0.0480 – 0.0825 | **0 or 32.5 – 47.1** | 23.1 – 86.2 |
| 10 nm | 0.024 | 224.9 – 374.4 | 0.0326 – 0.0542 | **0 or 9.8 – 13.8** | 7.0 – 24.0 |

The three strong-stretching models open with **exactly zero** stiffness at `L₀`. That is `C-0001`'s surprise `S-1`,
still standing after two changes of free energy, and it is why a stroke here cannot be a force divided by a stiffness.

### Blocking force — identical across all six models, by construction

`F_block = |F_es(L₀, V)|`, and every model is built to have the same `L₀`, so the six models agree **exactly**.
All disagreement in this claim is in the stroke.

| buffer | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| 2 mM, 0.10 V | 167.2 pN | 86.7 pN | 34.5 pN |
| 2 mM, 0.25 V | 490.4 pN | 214.7 pN | 73.6 pN |
| 2 mM, 2.0 V | 938.2 pN | 352.7 pN | 109.2 pN |
| **0.5 mM, 1.0 V** | **1552.7 pN** | **670.8 pN** | **254.2 pN** |
| 10 mM, 1.0 V | 287.4 pN | 75.2 pN | **12.2 pN** |

`C-0008`'s 2 mM table is reproduced to the digit — −490 / −215 / −74 pN at 0.25 V and −938 / −353 / −109 pN at 2 V.
That is the gate-5 cross-check, and it is exact rather than approximate because the same solver was re-run.

### Free stroke — the six-model bracket, and where it leaves validity

2 mM `MgCl₂`, free bulk buffer, no external load:

| bias | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| 0.05 V | 0.39 – 1.69 nm | 0.89 – 3.29 nm | 1.42 – 5.94 nm |
| **0.10 V** | **0.93 – 2.77 nm** | **2.24 – 5.02 nm** | **4.42 – 8.16 nm** |
| 0.25 V | 2.51 – 4.03 nm | 4.69 – 6.18 nm | 7.71 – 9.24 nm |
| 1.0 V | 3.32 – 4.20 nm | 5.42 – 6.28 nm | 8.44 – 9.31 nm |
| 2.0 V | 3.32 – 4.17 nm | 5.42 – 6.25 nm | 8.44 – 9.27 nm |

**The stroke saturates above ~0.5 V** — a factor of four in bias from 0.5 to 2 V moves it by under 1 % —
because the force saturates (`C-0008`) and because by then the layer is nearly incompressible.

And it saturates *at near-contact*. At 2 mM and 1 V the free operating height is **1.11 nm** (5 nm layer),
**1.01 nm** (7 nm) and **0.98 nm** (10 nm), at volume fractions of **0.33–0.34**.
Those points are outside three upstream validity ranges at once:

- below `C-0005`'s **1.46 nm** lateral counterion spacing, where mean-field PB cannot produce the physics *at all*;
- above `C-0002`'s **φ ≈ 0.2** semidilute→concentrated crossover, where the des Cloizeaux `9/4` exponent is no longer the one the layer is entitled to — §2's own second caveat;
- and at 2.6–3.0 × the layer's dry thickness, where a mean-field local free energy is a strong extrapolation.

**Only 272 of the 810 free operating points in the sweep are inside both boundaries.**

### The largest stroke that any model can defend

The largest free stroke reached at an operating point inside **both** upstream validity ranges:

| buffer | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| 0.5 mM | 0.94 – 2.30 nm | 2.23 – 4.38 nm | **4.52 – 7.41 nm** |
| 2 mM | 0.93 – 2.02 nm | 2.24 – 3.96 nm | **4.42 – 6.91 nm** |
| 10 mM | 0.73 – 2.88 nm | 0.92 – 5.11 nm | 1.60 – 8.17 nm |

so, model-independently at 2 mM:

- **5 nm cannot reach 3 nm of stroke inside validity, under any of the six models.**
- **7 nm straddles the 3 nm target** — three models reach it, three do not.
- **10 nm reaches it under every model**, with 1.5× to 2.3× of margin.

That ordering — thick layer easy, thin layer impossible — is the same direction `C-0003` found for the mechanical
design window and the *opposite* direction `C-0008` found for pull-in margin. The two constraints pull against each other.

### The two halves of the predicate need biases that differ by up to 30×, in opposite directions

2 mM, six-model bracket on the bias:

| | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| 100 pN **blocking** | **0.065 V** (exact, model-free) | **0.116 V** | **0.699 V** |
| 3 nm **free stroke** | 0.127 – 0.405 V | 0.045 – 0.147 V | 0.024 – 0.076 V |
| **100 pN at a 3 nm stroke** (simultaneous) | **0.122 – 0.368 V** | **0.082 – 0.155 V** | **0.134 – 0.192 V** |

The blocking clause gets *harder* with layer height and the stroke clause gets *easier*, by a factor of ten each.
At 10 nm they differ by up to 30× in bias. **Quoting "the bias needed" as one number hides both facts**, and
the simultaneous clause — the only one that describes a working device — is not the maximum of the other two:
at 10 nm it sits between them, at 0.13–0.19 V.

### The verdict, stated carefully

| clause | verdict at 2 mM | bias | inside `CH-0007`'s 1 V? |
|---|---|---|---|
| **(a)** `\|F_es(L₀, V)\| ≥ 100 pN`, `V ≤ 2 V` | **PASS** at every height | 0.065 / 0.116 / 0.699 V | yes |
| **(b)** free stroke `≥ 3 nm`, `V ≤ 2 V` | **PASS** at every height | 0.024 – 0.405 V | yes |
| **(b′)** free stroke `≥ 3 nm` **inside model validity** | **FAIL at 5 nm; straddles at 7 nm; PASS at 10 nm** | ≤ 0.10 V | yes |
| **(c)** 100 pN **at** a 3 nm stroke, `V ≤ 2 V` | **PASS** at every height | 0.082 – 0.368 V | yes |
| **(c′)** …and that operating point is stable against a dead load | **PASS at 5 nm; FAIL at 7 and 10 nm** | — | — |
| **(d)** bandwidth ≥ 1 kHz at the operating point | **PASS at 5 nm (1.1–2.3 MHz); vacuous at 7 and 10 nm** | — | — |
| ~10 nm *desired* stroke inside validity | **unreachable everywhere** | — | — |

**So the §6 answer is: reachable, comfortably, at a fifth of the bias budget — and the operating point it is
reachable at is not one the device can be held at without a stiff output coupling.**

### `k_eff` at the operating point — the number `T-4` needs and `C-0008` could not supply

`C-0008` handed over `k_es` at the **resting** height. The quantity that decides stability is `k_eff` at the
**operating** height, and the two are very different because `k_brush` rises steeply under compression.

At the **loaded** operating point — the tile held at the 3 nm stroke the §6 predicate is about — 2 mM,
strong-stretching / des Cloizeaux:

| `L₀` | bias | `k_brush` | `k_es` | `k_eff` | `k_eff/k_brush` | drainage corner |
|---|---|---|---|---|---|---|
| 5 nm | 0.10 V | 1430.4 | −196.9 | **+1233.5** | 0.862 | 2.07 MHz |
| 5 nm | 0.25 V | 1430.4 | −772.4 | **+657.9** | 0.460 | 1.10 MHz |
| 7 nm | 0.10 V | 125.4 | −82.8 | **+42.6** | 0.340 | 204 kHz |
| 7 nm | 0.25 V | 125.4 | −348.2 | **−222.8** | −1.776 | **none — unstable** |
| 10 nm | 0.05 V | 19.2 | −11.3 | **+7.9** | 0.413 | 98 kHz |
| 10 nm | 0.10 V | 19.2 | −27.7 | **−8.5** | −0.439 | **none — unstable** |

(pN/nm.) **428 of the 810 loaded operating points have `k_eff ≤ 0`.**

The consequence is sharp and it is a design constraint, not a caveat:

> **At 7 and 10 nm the bias that delivers 100 pN at a 3 nm stroke is *above* the bias at which the loaded
> operating point loses static stability. The actuator can reach the §6 target only against a load that supplies
> its own positive stiffness — a spring-loaded lever, not a constant force.** At 5 nm the ordering is the other
> way round and the point is stable with 46–86 % of the layer's own stiffness left.

That inversion across the layer-height range is the same one `C-0008` warned about from the resting height,
sharpened by a factor of a few and moved to the place where the device actually operates.

### The bandwidth, and why §4(d) stays discharged for a different reason than `C-0004` gave

`C-0004` verifies `τ ∝ 1/k_layer` **exactly**, which is what licenses substituting `k_eff` into its corner.
It evaluated its 186 / 130 / 91 kHz at `k_brush(L₀)` **with no electrostatics in the model at all**.

At the loaded operating point the corner is **higher**, not lower — 98 kHz to 2.3 MHz — because `k_brush` under
compression exceeds `k_brush(L₀)` by one to two orders of magnitude, far more than the electrostatic softening
takes away. Two things follow:

1. **§4(d) stays discharged, and by a wider margin than `C-0004` claimed**, at every operating point where a
   corner exists at all;
2. **where a corner does not exist, the problem is not bandwidth.** A `k_eff < 0` operating point does not settle
   slowly; it does not settle. Substituting `k_eff` into a first-order relaxation and reporting a negative
   frequency would be arithmetic, not physics, and it is reported as "no corner" instead.

### `k_es` changes sign, and that is what stops the collapse

§1 states `k_es < 0` and `C-0008` confirms it "everywhere" — over a gap sweep whose smallest point is 3 nm.
`|F_es(h)|` is **not monotone** below that: it rises to a maximum and then falls toward the sign change
`C-0008` already found at zero bias. Past the maximum `k_es > 0` and the electrostatics **stiffens** the layer.

**386 of the 810 free operating points in this sweep sit on that branch**, and the electrostatic force turns
outright repulsive below a gap of 0.55–1.58 nm, depending on buffer and bias — 1.11 nm at 2 mM and 0.02 V,
0.55 nm at 2 mM and 0.05 V. That is an **electrostatic stopper**,
and together with the layer's osmotic divergence it is why no operating point in the entire sweep failed to
converge and only 4 of 810 showed a second equilibrium below the first.

This is filed as [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md), because it inverts a
conclusion `T-4` was about to inherit, and it inverts it in the **favourable** direction — which is the direction
in which an error survives longest.

### The blocking force is not the peak output force

`dW/dh = k_eff` exactly. Wherever `|k_es|` approaches `k_brush` the characteristic is flat, and wherever it
exceeds it the characteristic **rises** with stroke. So `W` is maximal at a finite displacement, not at zero,
and `W(0)` — the blocking force, the conventional actuator figure of merit — **understates** what the device
can deliver. At 2 mM, 10 nm and 0.25 V, `W(3 nm) = 188 pN` against a blocking force of **73.6 pN**: a factor of 2.6 at that
one displacement, and the maximum over the characteristic is larger still.

Across the whole 2 mM sweep at 0.25 V the peak output force exceeds the blocking force by

| | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| `max W / W(0)` | 1.05 – 2.56× | 2.17 – 6.55× | **6.12 – 20.16×** |

and the ratio grows with layer height for the same reason the pull-in margin shrinks with it: a softer layer lets
the field win over a longer stretch of the characteristic. This is the electrostatic-softening signature in the
force-displacement plane — the same physics as pull-in, read on a different axis — and it is asserted as a test
rather than observed. **The conventional actuator figure of merit understates this device by up to twenty times,
and every one of those extra piconewtons is delivered at an operating point that needs an external stiffness to
hold.**

### The number an output coupling has to supply

The loaded operating point is stable if the coupling supplies at least `|k_eff|` of its own stiffness where
`k_eff < 0`. At 2 mM, six-model bracket:

| bias | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| 0.10 V | **0 — stable** (`k_eff` = +520 to +3977) | 11.2 pN/nm (one model of six) | **5.3 – 16.0 pN/nm** |
| 0.25 V | 55.3 pN/nm (one model of six) | **85.6 – 276.6 pN/nm** | **47.6 – 71.5 pN/nm** |

This is a design requirement `T-3` can state and no upstream task could: **an output lever for the 10 nm design
point must be stiffer than ~16 pN/nm at the tile, or the actuator does not have an equilibrium at its own target
stroke.** For scale, that is comparable to the whole layer's own stiffness at first contact (9.8–13.8 pN/nm).

### Leaf `A2.2`'s low-screening condition, quantified

`A2.2` adds a condition §6 does not: *"Evaluate stroke at the low-screening operating point (Mg²⁺-free /
low-Mg + crosslink), not 10-20 mM Mg²⁺."* The sweep was extended a factor of four below §3's lowest buffer.

**The buffer sets the force and barely touches the free stroke.** At 1 V and 10 nm the blocking force runs
254.2 / 173.0 / 104.6 / 39.3 / 12.2 pN over 0.5 / 1 / 2 / 5 / 10 mM — **a factor of 21** — while the free
operating height moves by under 1 %, because at that operating point the *layer* sets the height and `C-0007`
shows the layer's mechanics are buffer-independent to ≤ 0.4 %.

The condition is therefore vindicated **on the force clause, decisively**:

| | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| bias for 100 pN, 0.5 mM | 0.048 V | 0.075 V | **0.141 V** |
| bias for 100 pN, 2 mM | 0.065 V | 0.116 V | 0.699 V |
| bias for 100 pN, 10 mM | 0.153 V | **not reached ≤ 2 V** | **not reached ≤ 2 V** |

At 10 mM the §3 force target is **unreachable at any bias** at 7 and 10 nm. At 0.5 mM it is reached at 0.141 V
even at 10 nm — a factor of five better than at 2 mM. `A2.2` is right, and the mechanism is that `F_es` carries
the buffer while the layer does not.

### The PEG layer in the gap raises the force and the stroke

Carrying `C-0005`'s partition coefficients and Maxwell-Garnett permittivity through the coupled solve, at 2 mM:

| quantity | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| blocking-force amplification | 1.207 – 1.397× | 1.178 – 1.350× | 1.175 – 1.335× |
| **stroke** amplification | 1.066 – 1.205× | 1.035 – 1.104× | 1.020 – 1.064× |

`C-0008`'s force amplification of 1.15–1.60× is reproduced (1.175–1.397× over this narrower set), and the new
number is the second column: **the layer amplifies the stroke far less than it amplifies the force**, by
2 to 9×, because `C-0003`'s layer stiffens under compression faster than the amplification grows. That is the
coupled version of `C-0003`'s `k ∝ K^(1/(m+1))` weak-sensitivity result, and it is the reason `P-8` — which could
invert the sign of the amplification — moves the stroke by only a few per cent even if it inverts the force by 40 %.

### Whose stroke

Every stroke above is the **tile mean**. Under a perfectly uniform load that is also every point's, exactly, and
that is the *only* load case for which the tile is rigid (`C-0006`, `CH-0005`). Applying `C-0006`'s own ratios
to the 2 mM, 1 V operating points:

| output coupling | lever-point stroke, relative to the tile mean |
|---|---|
| continuous, uniform load | ×1.00 (exact) |
| 49 attachments | ×1.11 |
| 9 attachments | ×1.64 |
| 4 attachments | ×2.41 |
| 1 concentrated lever | ×4.69 |

At the 10 nm design point, 2 mM, 1 V, that turns a 9.02 nm tile-mean stroke into a lever travel of 10.0 nm
(49 attachments) to **42.3 nm** (one lever). The last figure is not a prediction — it is `C-0006`'s
*"no discrete attachment scheme is flat"* restated in the coupled numbers: **any coupling with fewer than about
16 attachments demands a lever travel larger than the whole layer height**, which is impossible, so the coupling
deforms the tile instead of moving it.

and an area-averaging charge sensor reads the tile mean **plus** a systematic Debye-weighting offset of
`δ²/(2λ_D)` = **0.206 nm at 2 mM**, which is 3–5 % of the strokes here. So a loop closed between a point-coupled
lever and an area-averaging sensor is comparing two numbers that differ by 11 % to 369 % of the stroke.
`C-0006`'s ratios are **cited** and were computed at `C-0001`'s foundation stiffness, which `C-0003` supersedes;
they are applied as ratios and flagged as such.

---

## Validity range

Respected downstream, and enforced in code where enforceable.

- **The free operating point leaves three upstream validity ranges above ~0.1 V** — `C-0005`'s 1.46 nm
  correlation band, `C-0002`'s φ ≈ 0.2 concentrated crossover, and the domain over which `C-0003`'s osmometry-anchored
  free energy was fitted. **538 of 810** points are above φ = 0.2 and **411 of 810** are inside the correlation band.
  Per §7 this is stated rather than absorbed: *the numbers above 0.1 V are extrapolations of models outside their own domains,*
  and they are reported because the saturation behaviour is itself the finding.
- **Mean field**, inherited whole from `C-0008` and `C-0005`: the one-loop correction is 123–214 % of the leading
  term over the entire 5–10 nm range for Mg²⁺, and for the *oppositely charged* tile-electrode pair no published
  result gives even the direction. This is the largest single uncertainty and it is not reducible by a better
  mean-field solve.
- **No force above ~1 V of applied bias is trustworthy** (`CH-0007`). Every threshold reported here is below 0.7 V
  and most are below 0.2 V, so the verdict does not depend on the untrustworthy end. The 2 V column is reported
  because §3 asks for it, and it is also outside the aqueous electrochemical window (`T-11`).
- **Zero bias is not computed.** `C-0008` shows it is a sign-changing near-cancellation under 4 pN for which no
  single number is defensible, so the sweep starts at 0.02 V and the resting height is taken as `L₀` exactly.
- **The layer's profile model is not settled.** `C-0003` states that the spread between its two profile families is
  a *lower* bound on the profile uncertainty, because the strong-stretching premise is not met anywhere in the box.
  `T-1d` may move every stroke here.
- **Every osmotic input is a bulk property applied to a brush** (`C-0003`, `C-0007`, `P-9`).
- **The tile is not a rigid plate** (`C-0006`, `CH-0005`). The 1-D balance solved here is the tile mean under a
  uniform load — the one case where the tile is rigid, and it is rigid there exactly.
- **The layer-local salt term is bounded, not modelled.** `C-0007`: Mg²⁺ inside the layer goes as `1/h`, 33 mM at a
  10 nm gap and 66 mM at 5 nm, worth ≤ 1.7 % of the modulus and hence ≤ 0.5 % of the stroke through
  `k ∝ K^(4/13)`. It is the only positive-feedback term downstream and it is **not** in the balance.
- **1-D.** No edge, no fringing, no lateral load profile, hence no dishing amplitude of its own — `C-0006`'s are cited as ratios.
- **Mg²⁺-free is not computed.** Only low-Mg, to 0.5 mM. See "Still open".
- **The output work assumes a uniform load.** `C-0006` shows no discrete attachment scheme is flat, so the work
  delivered through a real coupling is lower by an amount this task cannot compute.
- **Nothing here is measured.**

## Numbers that are cited rather than derived

| number | value | why it is cited, and what it moves |
|---|---|---|
| `C-0004`'s drainage corners and their stiffnesses | 186 / 130 / 91 kHz at 111.0 / 27.1 / 7.39 pN/nm | **CITED FROM `C-0004`**, together with its verified `τ ∝ 1/k_layer`. Drainage is not re-derived. Moves only the bandwidth statement, which has 100–2300× of margin. |
| `C-0004`'s composite worst case | 5.6 kHz | **CITED FROM `C-0004`.** |
| `C-0006`'s dishing-over-stroke ratios | 0.11 / 0.34 / 0.64 / 1.41 / 3.69 | **CITED FROM `C-0006`**, computed there at `C-0001`'s foundation stiffness, which `C-0003` supersedes. Applied as ratios. |
| `C-0006`'s thermal dishing RMS | 1.272 nm | **CITED FROM `C-0006`.** Sets the sensor offset `δ²/(2λ_D)`. |
| `C-0005`'s lateral counterion spacing | `a_⊥` = 1.46 nm | **CITED FROM `C-0005`.** Used as the validity floor on the gap. |
| `C-0002`'s concentrated crossover | φ ≈ 0.2 | **CITED FROM `C-0002`** and read as a ceiling, per §2's second caveat. The 0.2–0.3 band means the floor of that ceiling is used. |
| Manning surviving fraction | 11.90 % | **CITED FROM `C-0005` via `C-0008`.** The tile is charge-saturated, so a factor of three here is 7 % in `σ_eff`. |
| Stern capacitance | ~20 µF/cm² | **CITED.** Load-bearing for the bias mapping (`CH-0007`), not for the force above ~0.5 V. |
| `ε_r` of water at 300 K | 78 | **CITED.** ~3 % on `F_es`. Moves no verdict. |
| `A₂`, `A₃`, `α` | 1.9e−3, 2.0e−2, 0.49 | **CITED FROM `C-0003`/`C-0002`**, which flag `A₂` as read in a re-tabulation. |
| `C-0007`'s buffer-independence of the layer | ≤ 0.4 % over 2–10 mM | **CITED FROM `C-0007`.** It is what licenses holding the layer mechanics fixed across the buffer sweep. |

Everything else — the geometry relation, `L₀` and `N` at every model, the interpolant, `F_es(h, V)` re-run through
`C-0008`'s solver, the force balance, the stroke, `k_eff` at the operating point, the acceptance thresholds, the
layer amplification and the stroke readings — is derived here.

## Cross-checks passed

1. **Gate 1** — the footprint is an area, the electrostatic gap is the layer height (asserted as an identity
   function, not a formula), `ℓ = F_es/k_es` is `pN/(pN/nm) = nm` at every gap, `k_eff = k_brush + k_es` additively
   to 1e−9, the output work is a force times a length; unphysical geometry, non-ascending grids, repulsive
   samples and out-of-range gaps all throw.
2. **Gate 2** — a pure exponential force is reproduced **exactly**, value and derivative, at 1e−12, because
   `ln|F|` is then linear and the scheme is exact on linear data; a vanishing field leaves the tile at `L₀`;
   a stronger field gives monotonically larger stroke and smaller operating height; the box model opens with
   finite stiffness and the strong-stretching model with none; a 16× change in interaction strength moves the
   stroke by under 2×, reproducing `C-0003`'s exact `k ∝ K^(1/(m+1))`; the interpolant is monotone with no
   overshoot on a 0.01 nm sweep; a repulsive small-gap tail is trimmed and its stopper reported.
3. **Gate 3** — at the operating point the layer load equals `|F_es|` to 1e−9 of itself and the output force
   vanishes there; **the first equilibrium below `L₀` has `k_eff > 0`**, asserted over fifteen (amplitude, decay
   length) combinations, which is a theorem about the construction rather than a property of the answer;
   `k_es < 0` and `ℓ > 0` above the force maximum and **both reverse below it**, with the identity `ℓ = F_es/k_es`
   surviving the sign change; the output force is asserted to peak at a **finite** stroke.
4. **Gate 4** — three independent convergence axes over the whole pipeline, all at **1.6e−7**: force-curve
   samples 18 → 144, Poisson-Boltzmann mesh 1000 → 8000 nodes, force-balance scan 300 → 9600 steps. The
   interpolant's own worst error over the range falls by more than 5× per halving of the spacing and reaches
   < 1e−6. **The result file was re-run and diffed byte-for-byte.**
5. **Gate 5** — **`C-0008`'s force table is reproduced to the digit** (−490 / −215 / −74 pN at 0.25 V,
   −938 / −353 / −109 pN at 2 V, 2 mM), because the same solver was re-run rather than a table copied;
   **`C-0003`'s dead-load stroke is reproduced from the other side** — a constant 100 pN curve through the coupled
   solver returns `heightUnderLoad`'s answer to 1e−8 by an independent bracketing route, and lands inside
   `C-0003`'s published 3.83–6.01 nm bracket at the 10 nm point; and **§3's own parameter table is checked against
   itself** — a 10 nm tile on §3's three layer heights with a 5 nm lever attachment puts the effort point at
   exactly 20 / 22 / 25 nm, which is §3's stated 20–25 nm band at both ends.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **Mg²⁺-free is not computed and cannot be by this solver.** `IonModel` is a 2:1 electrolyte by construction and
   a salt-free gap has no screening length; the crosslinking that would replace Mg²⁺ structurally is outside every
   model in this project. What is computed is **low-Mg**, to 0.5 mM, and the trend to lower salt is monotone and
   strongly favourable for the force clause. A 1:1 or salt-free solver in `electrostatics/` would close it; this
   task does not own that package.
2. **The bias at which the loaded operating point folds is bracketed by the bias sample spacing, not located.**
   That is `T-4`'s task. This claim hands it `k_eff` **at the operating point**, which is the quantity `C-0008`
   could not supply, and the sign reversal of `k_es` that decides whether the fold is a runaway or a stop.
3. **The lateral load profile is not computed and a 1-D treatment cannot compute it** (`C-0008`). It is what
   converts `C-0006`'s exactly-linear dishing result into an amplitude, hence what decides whether the lever and
   the sensor readings quoted here are separated by 11 % or by 369 %. `T-3b`.
4. **Whether the 10 nm design point exists at all is decided by the profile model** (`C-0003`), and `T-1d` has not
   landed. Every 10 nm number here — which is where the stroke verdict is most favourable — is conditional on that.
5. **What happens between 0.1 V and 0.25 V** is not resolved: that is where the free operating point crosses from
   inside every validity range to outside all of them, and the bias grid has no sample between them. A refined
   sweep would locate the crossing but not make the far side computable.
6. **Poisson-Nernst-Planck and FEM were not run.** The Plan says what each would buy: PNP buys the ionic transient
   (~10 ns, four orders below `C-0004`'s drainage time, hence not bandwidth-limiting) and reduces to the PB
   problem already solved at steady state; FEM buys dimensionality, which is `T-3b`'s deliverable. Neither
   repairs the mean-field error, which dominates.

## Challenges

[`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md) is raised **by** this claim, against
`C-0008`'s statement that `k_es < 0` everywhere.
None stands against this claim.
