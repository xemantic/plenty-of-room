# C-0033 — `d ln μ/dh` is a derivative now, it is positive everywhere, and the direction it moves `C-0018`'s margin is set by whether the fold is deeper than §3's stroke

| | |
|---|---|
| **Task** | [`T-60`](../tasks/T-60.md), and it closes `T-62` |
| **Leaf** | **`A7.4`**, consumed by `A2.2` |
| **Verification type** | **in-silico** (`C-0022`'s 2-D nonlinear Poisson-Boltzmann edge solver **consumed read-only** and re-run at **fixed applied bias** over a gap sweep, its collar multiplier carried into `C-0018`'s equilibrium-path fold search under **three field variants**) **+ logical** |
| **Verdict** | **PASS on `P1`–`P4`.** `C-0027`'s open item is closed: `d ln μ/dh` is `0.0176–0.0201 nm⁻¹` at the 10 nm folds, converged to 0.11 % in the mesh and 1.6 % across a 6× range of difference step, against the `0.0133–0.0226 nm⁻¹` three-scheme band it had to be bracketed by. **The pull-in bias moves, and it moves in the direction of the fold's own depth.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** And inside mean field: `C-0005` puts the one-loop correction at **123–214 %** of the leading term across this gap range — two orders larger than every margin movement below. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.) |
| **Provenance** | `gpd/results/T-60-collar-on-the-equilibrium-path.json`, produced by `actuator.CollarEquilibriumPathStudyKt`; 48 two-dimensional solves (30 sweep, 6 bias probes, 3 upstream reproductions, 9 mesh-convergence), 30 cheap estimates, 80 gradient records, 108 fold searches, 36 decomposition records; **24 new gate-named tests** in `electrostatics.CollarMultiplierTest` and `actuator.CollarCorrectedFieldTest`, 1208 in the suite, 0 failures; the result file re-run end to end and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at **2 mM** (10 nm layer, `σ = 0.024 nm⁻²`) and **10 mM** (7 nm layer, `σ = 0.045 nm⁻²`); 40 × 40 nm Manning-renormalised tile; all six `C-0003` layer models; three load lines |
| **Consumes** | [`C-0022`](C-0022-tile-edge-load-profile.md) (the solver and the collar, **consumed read-only**), [`CH-0026`](../challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md), [`CH-0035`](../challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md) (the decomposition), [`C-0018`](C-0018-maximum-usable-bias.md) (the folds, **re-run not tabulated**), [`C-0017`](C-0017-output-coupling-stiffness.md) (the coupling), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md) (`k_brush`, cited through `C-0027`), [`C-0027`](C-0027-window-resynthesis.md) (which raised this) |
| **Raises** | [`CH-0051`](../challenges/CH-0051-the-pull-in-bias-falls-too.md) against `C-0027` |

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; **the electrostatic gap is the layer height, exactly** (`C-0012`).
- `x` is lateral, `x = 0` is the tile centre-line, the rim is at `x = 20 nm` (`C-0022`).
- The **stroke** `s = L₀ − h` is positive **downward**; **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`).
- The **collar multiplier** is `μ(h, V) ≡ |F_es,2D(h,V)| / (Π_1D(h,V)·A)` with `A = 1600 nm²`: dimensionless, and **`μ > 1` is an enhancement**. `T-3b` emits a force *deficit* fraction, so `μ = 1 − fraction`; the **minimum-margin** mapping counts each corner once and the **additive** one twice, and the two bracket the unsolved 3-D corner. Every headline below is on the minimum-margin mapping, the conservative one.
- **`d ln μ/dh` is in `nm⁻¹` and is taken at FIXED APPLIED BIAS**, which is the state variable `k_es` is differentiated at — not at fixed diffuse-layer drop, and not along a path on which the bias moves. **That is the whole of the difference between this task and the number `C-0027` had to use.**
- `k_es = |F_es| d ln|F_es|/dh` and `ℓ = −1/(d ln|F_es|/dh)`, so **`k_es = −|F_es|/ℓ` identically**; `k_es < 0` above the force maximum.
- The **load line** `R(s)` is positive **upward**: free (`R = 0`), dead load (`R = 100 pN`), coupled (`R = 33.333 s`, `C-0017`'s mandate). **A ceiling belongs to a `(bias, load line)` pair, never to the bias alone.**
- Lengths nm, forces pN, pressure `pN/nm²` = 1 MPa exactly, stiffness pN/nm, bias V, buffer mM MgCl₂ (2:1, so `I = 3c`), `k_BT = 4.142 pN·nm` at 300 K.

---

## The claim, in one line

**Solved at one fixed applied bias over sixteen gaps, `μ` is a smooth monotone function of the gap alone — `0.977` at 2 nm to `1.162` at 11 nm at 2 mM, crossing one at ≈ 2.8 nm — and its logarithmic gradient is `0.0176–0.0201 nm⁻¹` at `C-0018`'s own fold gaps, converged to 0.11 % in the mesh where `C-0027` could only bracket it 1.7× wide. Carrying it onto the equilibrium path settles the fold's movement three ways: the collar-only tangent at `C-0018`'s own fold is `+2.60` to `+4.99 pN/nm`, *strictly* positive because at a pinned force it is exactly `|F_es| d ln μ/dh`; the fold therefore moves to a deeper stroke everywhere; and at the binding 10 nm / 2 mM state the pull-in margin rises at all six models — to `1.021–1.028` where pull-in still binds, and at four of six models pull-in stops binding at all. But the direction is NOT universal: the margin is the ratio of two biases read at two different gaps, so it moves with the sign of `μ(h_fold) − μ(h_operating)`, and at 7 nm / 10 mM — where the fold is *shallower* than §3's 3 nm — the same correction makes the margin 0.9 to 3.5 % WORSE.**

---

## `P1` — `μ(h)` at fixed applied bias

`C-0022`'s `PoissonBoltzmannEdge` re-run unchanged at refinement 3, the Stern series inverted **per gap** so that the *applied* bias is what is held. The denominator is the solve's **own** centre-line load, not a separate 1-D solve: the two agree to 3e−4 – 0.1 % here and 0.1 % of noise in a ratio differenced over 0.5 nm would be 15 % of the gradient. The independent 1-D solve is kept as a **gate** and agrees to `2.9e−6 – 1.0e−3` here.

### 2 mM, fixed applied bias 0.155 V (the midpoint of `C-0018`'s six-model `V*` bracket at 10 nm)

| gap [nm] | 2.0 | 3.0 | 4.0 | 5.0 | 5.5 | 6.0 | **6.5** | **7.0** | 8.0 | 9.0 | 10.0 | 11.0 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`μ` (min-margin)** | 0.9771 | 1.0062 | 1.0363 | 1.0634 | 1.0755 | 1.0866 | **1.0969** | **1.1063** | 1.1232 | 1.1378 | 1.1506 | 1.1620 |
| `μ` (additive) | 0.9773 | 1.0090 | 1.0420 | 1.0719 | 1.0853 | 1.0977 | 1.1091 | 1.1196 | 1.1385 | 1.1547 | 1.1687 | 1.1808 |
| collar `w` [nm] | −0.228 | 0.090 | 0.420 | 0.719 | 0.853 | 0.977 | 1.091 | 1.196 | 1.385 | 1.547 | 1.687 | 1.808 |

(the sweep also carries 4.5, 6.25 and 6.75 nm, used for the difference-step convergence.)

**`μ` crosses one at a gap of ≈ 2.8 nm** and rises monotonically above it. `C-0022`'s sign reversal at a 2 nm held gap is reproduced here as part of one smooth curve rather than as an isolated exception.

### 10 mM, fixed applied bias 0.22 V (the 7 nm state)

| gap [nm] | 2.0 | 3.0 | 4.0 | 4.75 | 5.0 | 5.5 | 6.0 | 7.0 | 8.0 |
|---|---|---|---|---|---|---|---|---|---|
| **`μ`** | 0.9993 | 1.0243 | 1.0419 | 1.0514 | 1.0541 | 1.0589 | 1.0630 | 1.0696 | 1.0751 |

Stronger screening gives a **narrower** collar (0.79 nm at 8 nm against 1.38 nm at 2 mM) and a **much flatter** one — which is the whole reason the two states move in opposite directions in `P4`.

### `μ` is a function of the gap — measured, not assumed

`T-60`'s Plan declared this as falsifier 1 at a 2 % threshold, because the whole construction needs `μ` to be one-argument.

| state | gap | biases | `μ` | **relative spread** |
|---|---|---|---|---|
| 10 nm / 2 mM | 6.5 nm | 0.128 / 0.155 / 0.184 V | 1.09789 / 1.09686 / 1.09470 | **0.291 %** |
| 7 nm / 10 mM | 4.75 nm | 0.148 / 0.220 / 0.396 V | 1.05322 / 1.05145 / 1.04730 | **0.564 %** |

**The falsifier did not fire**, and the residual dependence is one-signed and monotone (`μ` falls slightly with bias), which is recorded as a validity note rather than modelled.

---

## `P2` — `d ln μ/dh`, as a derivative of one function

### The number

| state | at the operating gap | **at `C-0018`'s own fold gaps** | over the whole solved range |
|---|---|---|---|
| **10 nm / 2 mM** | `0.01646` at 7.0 nm | **`0.01763 – 0.02011 nm⁻¹`** (5.875 – 6.590 nm) | 0.00920 (11 nm) – 0.02939 (3 nm) |
| **7 nm / 10 mM** | `0.01399` at 4.0 nm | **`0.00953 – 0.01244 nm⁻¹`** (4.318 – 5.082 nm) | 0.00456 (8 nm) – 0.02875 (2 nm) |

**It is positive at every gap of both sweeps**, which is `CH-0035`'s falsifier 1 not firing, and it **falls with the gap**, so the collar's own gradient is largest exactly where the tile is closest.

### Why the spread collapsed

| source | `d ln μ/dh` at ~6.5 nm, 2 mM | spread |
|---|---|---|
| `C-0027`, three difference schemes over `T-3b`'s five gaps **at five different biases** | 0.0133 / 0.0171 / 0.0226 | **1.70×** |
| **here**, central differences at ±0.25 / 0.5 / 1.0 / 1.5 nm at **one** bias | 0.017954 / 0.017987 / 0.018084 / 0.018235 | **1.016×** |
| **here**, `C¹` interpolant at the node | 0.017954 | — |

**A 6× change of difference step is worth 1.6 %** where changing the bias with the gap was worth 70 %. The interpolant's node derivative *is* the tightest central difference, by construction (parabolic node slopes on a uniform mesh), so the number reported and the number used are one object.

### The cheap estimate, run before any 2-D solve

`w ≤ 1/q₀`, `q₀² = κ² + (π/2h)²` — `C-0022`'s rigorous width ceiling — taken *as* the collar on the additive mapping gives `d ln μ/dh = (4/L)(dw/dh)/μ` with `dw/dh = (π²/4)h⁻³/q₀³`, in closed form.

| state / gap | cheap estimate | solved | **ratio** |
|---|---|---|---|
| 2 mM, 7 nm | 0.01421 | 0.01646 | **1.16×** |
| 2 mM, 5 nm | 0.02393 | 0.02367 | **1.01×** |
| 10 mM, 5 nm | 0.00622 | 0.00982 | **1.58×** |
| 10 mM, 7 nm | 0.00270 | 0.00557 | 2.06× |

**The Plan predicted "about a factor of two, one-sided in neither direction" and that is what happened** — 1.01–2.06×, and it is *not* one-sided (it overestimates at 5 nm and underestimates elsewhere). It is the first cheap bound in this programme whose declared error was the error it had. It got the sign right, which `C-0022`'s own depth half did not.

### Convergence — of the gradient, not of `μ`

`CLAUDE.md`: *convergence is a property of the quantity.* A gradient is a difference of two quantities that individually converge, so it is checked on its own.

| refinement | `μ(6.5 nm)` | departure | `d ln μ/dh` | **departure** |
|---|---|---|---|---|
| 2 | 1.097360 | 6.4e−4 | 0.018057 | **5.1e−3** |
| 3 (the sweep mesh) | 1.096860 | 1.8e−4 | 0.017987 | **1.1e−3** |
| 4 | 1.096660 | — | 0.017967 | — |

**The gradient converges 8× worse than the multiplier it is a derivative of at refinement 2, and 6.2× worse at refinement 3 — and it still converges to 0.11 %** at the mesh the sweep runs on — 15× inside the 1.6 % difference-step spread it is asked to collapse, and 600× inside `C-0027`'s 70 %. Falsifier 4 did not fire.

---

## `P3` — the fold, re-located; and the level/gradient split, measured rather than argued

Three field variants at every `(state, model, load line)`: `μ ≡ 1`, `μ ≡ μ(h_fold)` (level only), and the solved `μ(h)`.

### The identity that makes the sign free

At `C-0018`'s **own** fold the baseline coupled tangent vanishes by construction, `k_c + k_brush + k_es = 0`. The force there is pinned by the balance, and a multiplier on a pinned force moves only the decay rate, `1/ℓ → 1/ℓ − d ln μ/dh`. So carrying the collar adds **exactly `|F_es| d ln μ/dh`** to the tangent, and

> **the sign of the collar's whole effect at the fold is the sign of `d ln μ/dh`, with no computation at all.**

Measured at the six 10 nm / 2 mM folds: `+2.604` to `+4.994 pN/nm`, against `|F_es| = 143–250 pN` and `g = 0.0176–0.0201 nm⁻¹`. **A positive tangent means the path is still ascending at the old fold, so the fold moves to a deeper stroke — at every model, on every load line, without exception.**

### `C-0027`'s straddle, resolved

| | coupled tangent at `C-0018`'s own fold, 10 nm / 2 mM |
|---|---|
| `C-0027`, collar + `C-0019` over **three difference schemes** | **−2.469 to +4.003 pN/nm** — straddles zero |
| **here, collar alone, resolved gradient** | **+2.604 to +4.994 pN/nm** — *strictly positive* |
| **here, collar + `C-0019`'s `k_brush` degradation** (0.90584, **CITED**) | **−0.813 to +1.156 pN/nm** |

**The numerical straddle is gone. What still straddles zero is a *model* spread — six layer models, not three difference schemes — and it is 3.3× narrower.** That is a different kind of unresolved: it is a statement about which of `C-0003`'s six free energies the layer obeys, which no amount of mesh refinement addresses, and it belongs to `C-0019`'s half of the correction rather than to this one.

### The level and the gradient, separately

10 nm / 2 mM, coupled line, the two models at which pull-in still binds under `μ(h)`:

| model | margin, `μ ≡ 1` | `μ ≡ const` | `μ(h)` | **level worth** | **gradient worth** |
|---|---|---|---|---|---|
| alexander-box(two-body) | 1.00708 | 1.00993 | **1.02065** | +0.283 % | **+1.062 %** |
| strong-stretching(two-body) | 1.01289 | 1.01549 | **1.02783** | +0.256 % | **+1.216 %** |

**The gradient is worth 3.8–4.8× what the level is**, which is `CH-0035`'s identity measured. And the level is not *exactly* zero, as `CH-0035` says of `k_es`: what the level column measures is the **second-order** channel — a constant `μ` still lowers the bias, and a lower bias has a longer `ℓ` — which is precisely `C-0027`'s `decayLengthShift`, here found to be 0.26–0.28 % of the margin.

### Which quantities are force-pinned and which are not

| quantity | pinned? | what the LEVEL of `μ` does to it |
|---|---|---|
| `\|F_es\|` anywhere on an equilibrium path | **pinned** by `\|F_es\| = R(s) + P(h)A` | nothing — it is fixed by mechanics |
| the bias at any point of the path (`V*`, the pull-in bias) | **not** pinned | moves it, and by a lot: `V*` falls **8.5–9.9 %** at 10 nm / 2 mM |
| the **dead-load** ceiling, `C-0008`'s blocking bias at zero stroke | not pinned | **0.6795 → 0.3683 V, −46 %**, for a 15 % force gain — because the force saturates in bias |
| `k_es` at a pinned force | pinned in level | **exactly nothing** to first order; only `d ln μ/dh` and the second-order `ℓ(V)` shift survive |
| the **margin**, a ratio of two biases on the same path | — | almost nothing (0.26 %) where the ceiling keeps its owner |

The 8.5–9.9 % fall in `V*` is an **independent reproduction of `C-0027`'s 8–9 %**, which it obtained from `T-16`'s measured `dV/dF` rather than by re-solving. Two different routes, same number.

---

## `P4` — does `C-0018`'s margin move, and in which direction

### 10 nm / 2 mM — the state the whole question was asked about

| model | `V*` | pull-in | stroke | **margin** | → `V*` | pull-in | stroke | **margin** | binding ceiling |
|---|---|---|---|---|---|---|---|---|---|
| alexander-box(two-body) | 0.1568 | 0.1579 | 3.410 | **1.0071** | 0.1424 | 0.1454 | 3.828 | **1.0207** | pull-in |
| alexander-box(virial) | 0.1789 | 0.1836 | 4.078 | **1.0264** | 0.1612 | none | — | **1.7074** | φ = 0.2 |
| alexander-box(des-Cloizeaux) | 0.1804 | 0.1833 | 3.657 | **1.0160** | 0.1625 | none | — | **1.5073** | φ = 0.2 |
| strong-stretching(two-body) | 0.1283 | 0.1300 | 3.578 | **1.0129** | 0.1175 | 0.1207 | 3.963 | **1.0278** | pull-in |
| strong-stretching(virial) | 0.1367 | 0.1411 | 4.125 | **1.0317** | 0.1249 | none | — | **1.9226** | φ = 0.2 |
| strong-stretching(des-Cloizeaux) | 0.1393 | 0.1432 | 3.952 | **1.0277** | 0.1272 | none | — | **1.7080** | φ = 0.2 |

(left block `μ ≡ 1`, which reproduces `C-0018` exactly — see gate 5; right block the solved `μ(h)`. Biases V, strokes nm.)

**Three statements, and the second is the one nobody predicted:**

1. **The margin rises at every one of the six models.** Where pull-in still binds it is **1.0207–1.0278** against `C-0018`'s 1.0071 and 1.0129 at those same two models. **It does not go below one at any model.**
2. **Pull-in stops being the binding ceiling at four of the six.** The fold moves deep enough that the path meets `C-0002`'s `φ = 0.2` crossover *first*, and past that the branch simply rises until the field can no longer hold the tile — every one of those branches ends **on the field**, at strokes of 7.9–8.7 nm and biases of 0.245–0.377 V, with no maximum at all. That is `C-0018`'s own mechanism for the free tile ("the layer's osmotic divergence removes the instability") arriving at the *coupled* line. **`C-0018`'s "pull-in binds at 11 of 54 coupled states" becomes 6 of 12 at the two states where it bound, not 11 of 12.**
3. **`C-0027`'s `≥ 1.108–1.134` is not a lower bound**, and that is [`CH-0051`](../challenges/CH-0051-the-pull-in-bias-falls-too.md). It was computed by lowering `V*` at an *unchanged* pull-in bias; the same multiplier lowers the pull-in bias by 7.1–8.0 % as well, and the solved margin where pull-in binds is **1.021–1.028**, below `C-0027`'s claimed floor.

### 7 nm / 10 mM — `T-62`'s five states, and the direction reverses

| model | margin `μ ≡ 1` | margin `μ(h)` | fold stroke `μ(h)` | ≥ §3's 3 nm? |
|---|---|---|---|---|
| alexander-box(two-body) | 1.1138 | **1.0868** | 1.980 | **no** |
| alexander-box(virial) | 1.0605 | **1.0311** | 2.301 | **no** |
| alexander-box(des-Cloizeaux) | 1.1267 | **1.0873** | 2.079 | **no** |
| strong-stretching(two-body) | 1.0216 | **1.0125** | 2.466 | **no** |
| strong-stretching(virial) | 1.4411 (φ = 0.2) | 1.5007 (φ = 0.2) | no fold | — |
| strong-stretching(des-Cloizeaux) | 1.0038 | 1.3379 (φ = 0.2) | no fold | — |

**Here the margin FALLS, by 0.9 % to 3.5 %**, at every model where pull-in binds — the opposite direction to 10 nm / 2 mM, from the same correction, with the same positive gradient.

**The reason is structural and it is the most transferable thing in this claim.** The margin is `V_pullin/V*`, and those two biases are read at **two different gaps** — the fold gap and the operating gap. Both are lowered by `μ`, and the one lowered more is the one whose gap has the larger `μ`. So

> **the pull-in margin moves with the sign of `μ(h_fold) − μ(h_operating)`, i.e. with the sign of `h_fold − h_operating`, i.e. with the sign of `3 nm − s_fold`.**

At 10 nm the fold is at 3.4–4.1 nm, *deeper* than §3's 3 nm, so `h_fold < h_operating`, `μ(h_fold) < μ(h_operating)`, `V*` falls more, and the margin rises. At 7 nm / 10 mM the fold is at 1.9–2.7 nm, *shallower*, and every sign reverses. **A one-signed correction to a force does not give a one-signed correction to a bias margin.**

And the second test `C-0018` insists on is unchanged: **the fold stroke at 7 nm / 10 mM stays below §3's 3 nm** — 1.98–2.47 nm with the collar carried against 1.92–2.68 without. The target stroke is still on the unstable side of the fold there, and the collar does not rescue it.

### The other two load lines

- **Dead load.** Every fold is at the branch start, at every model and every variant, exactly as `C-0018` reports: no compressed equilibrium is stable at any bias. The ceiling is `C-0008`'s blocking bias and the collar moves it **0.6795 → 0.3683 V** at 10 nm — a 46 % fall for a 15 % force gain, because `|F_es|` saturates in bias there.
- **Free.** No fold at any model or variant; the binding ceiling is `φ = 0.2` throughout and the margin *rises* 1.95–6.12 → 2.13–6.79. This is the one load line at which `CH-0026`'s original fixed-bias direction is the right one, and it is also the one whose operating point §3 does not specify.

---

## Validity range

- **TRL 1–3. NOTHING HERE IS MEASURED.**
- **MEAN FIELD, inherited whole** from `C-0005` and `C-0008`: 123–214 % of the leading term across this gap range, and for the *oppositely charged* tile-electrode pair no published result gives even the direction. **Every margin movement here is two orders inside that.** The result is a statement about the model's own arithmetic — which is exactly what `C-0027` asked for. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **`C-0022`'s validity range travels whole**: two-dimensional and hence a *straight* edge, the 3-D corner **bracketed by two mappings and not solved** (1.8 percentage points of total force at 40 nm), the rim charge unsourced and worth 1.85× on the *depth*, point ions, free buffer in the gap, a macroscopic electrode, and the Stern series solved in **one** dimension and imposed laterally uniformly.
- **The collar multiplies the FORCE and not the applied bias**, because `C-0022`'s Stern series is 1-D. A laterally resolved compact layer would move the diffuse-drop-to-bias mapping near the rim. Inherited, not repaired.
- **Both mappings are emitted; every headline is the minimum-margin one**, which is the smaller correction for an enhancement and therefore the conservative choice for the 10 nm state — and the *optimistic* one at 7 nm / 10 mM, where the correction hurts. The additive mapping is 1.5–1.8 percentage points larger in `μ` and about 7 % larger in `d ln μ/dh`.
- **`μ` was solved at ONE fixed applied bias per state**, licensed by the measured 0.29 % / 0.56 % bias spread. A state whose operating bias leaves the probed bracket needs its own sweep.
- **The layer is `C-0003`'s at `C-0001`'s single grafting density per height**, not `C-0011`'s solved profile — `C-0017`'s and `C-0018`'s choice, so that the load line is drawn across the same characteristic `C-0012` computed. `C-0016` puts the solved layer 1.22× outside that bracket at 5 nm.
- **`L₀` is a FORCE-ONSET height** at a defining load of 1.0 pN over the tile.
- **The collar curve CLAMPS outside the solved gap range** (2–11 nm at 2 mM, 2–8 nm at 10 mM) and every clamped evaluation is counted: 2–142 per fold search, out of ~40 located biases each, and they occur only at the deep end of the coarse scan. **Every located fold is inside the solved range**, and both the operating gaps (7.0 and 4.0 nm) are interior.
- **The load lines are AFFINE.** `C-0017`'s real coupling strain-stiffens (`C-0023`) and `C-0030`'s realised law strain-*softens* (`C-0032`); the affine line is `C-0018`'s and is kept so the comparison is like for like. **`C-0032`'s softening coupling is not carried here and it moves the same margin the other way** — see "Still open".
- **STATIC only**, quasi-static below ~10 kHz (`C-0004`).
- **Two states only.** `C-0018` reports pull-in as the binding ceiling at 11 of 54 coupled states and these two carry all of them; at the other 43 the ceiling is `C-0002`'s `φ = 0.2`, which no electrostatic correction can move.

## Numbers that are CITED rather than DERIVED

| number | value | why it is cited, and what it moves |
|---|---|---|
| `ε_r` of water at 300 K | 78 | **CITED**, as in `C-0005`/`C-0008`/`C-0022`. ~3 % on the load, ~0 on the ratio |
| Manning surviving fraction | 11.90 % | **CITED FROM `C-0005`** via `C-0008`; the tile is charge-saturated |
| Stern capacitance | ~20 µF/cm² | **CITED**, and load-bearing for the bias mapping; the collar is a *ratio* and moves 0.29 % across the probed bias bracket |
| `C-0018`'s six-model `V*` brackets | 0.128–0.180 V / 0.144–0.373 V | **CITED FROM `C-0018`**, used only to *centre* the fixed bias. That the choice is not load-bearing is measured here |
| `C-0002`'s concentrated crossover | `φ = 0.2` | **CITED**, read as a ceiling. **It is the binding ceiling at 6 of 12 corrected states**, so this claim is now as sensitive to it as `C-0018` was |
| `CH-0007`'s point-ion boundary | 1.0 V | **CITED.** Never binds |
| `C-0017`'s mandated coupling | 33.333 pN/nm | **CITED**, itself derived from §3 alone |
| `C-0019`'s `k_brush` multiplier | 0.90584 (10 nm), 0.94885 (7 nm) | **CITED FROM `C-0027`**'s own result file, and used **only** to recompute `C-0027`'s straddling tangent. Nothing here re-runs `C-0019` |
| §3's targets | 100 pN, 3 nm, 5/7/10 nm | **CITED** |

Everything else — `μ(h)` at fixed bias, its gradient by three routes, the mesh and difference-step convergence, the cheap estimate, every re-located fold, every margin, the level/gradient decomposition and the sign rule — is **derived here**, with `C-0022`'s solver and `C-0018`'s fold search **re-run rather than tabulated**.

## Cross-checks passed

1. **Gate 1 — dimensional.** `μ` is asserted dimensionless and equal to `1 + 4w/L` through the *collar*, which is the form `C-0022` quotes, rather than through the expression under test; `d ln μ/dh` is asserted through its own definition (halving every gap doubles it) and not through any formula; the cheap estimate is asserted against a finite difference of `transverseDecayRateBound`, which shares no code with it. **And a factor-of-two trap fired and was caught**: a central difference divided by `2·step` instead of by the *separation* of its two gaps is exactly half the right answer and passes every dimensional check — `centralLogGradient` now names the separation once, with its own test.
2. **Gate 2 — limiting cases.** `μ → 1` as `1/L` exactly as the tile grows; a zero collar gives `μ = 1.0` and a gradient of `0.0` exactly; a log-linear `μ` is reproduced to `1e−10` in value *and* in gradient; `μ ≡ 1` makes the corrected field **identical** to the uncorrected one, sample for sample; a constant `μ` leaves the fold stroke exactly where it was in the analytic caricature, while a gradient moves it deeper and raises the fold bias.
3. **Gate 3 — symmetry and conservation.** The 2-D charge balance closes to `2.8e−4 – 4.4e−3` at every one of the 48 solves; the two independent reference planes agree on the centre-line load to `9.1e−8 – 3.6e−5` and **48 of 48 are `numericallyResolved`**; the solve's own centre-line load reproduces an *independent* 1-D solve to `2.9e−6 – 1.0e−3`; `μ` at one gap from three biases spans 0.29 % / 0.56 %; and the tangency identity `k_c + k_eff = 0` holds at every one of the **26 interior folds** to a worst relative residual of **1.18e−5**, with `k_es` taken by central difference of the full re-solve at fixed applied bias — a route that shares no code with the path maximisation that located the fold. The 18 boundary maxima (the dead-load line, descending from zero stroke) report **no** residual rather than a meaningless one.
4. **Gate 4 — convergence.** 2-D mesh refinement 2/3/4 on the **gradient** (5.1e−3, 1.1e−3, 0) as well as on `μ` (6.4e−4, 1.8e−4, 0); difference step 0.25/0.5/1.0/1.5 nm (1.6 % over a 6× range); and the fold search's own stroke tolerance, whose noise floor is `√(2t) = 1.41e−5` in the located bias — 630× below the smallest margin movement reported.
5. **Gate 5 — upstream reproductions, by re-running.** `T-3b`'s published `μ` at **its own** three `(gap, bias)` points at 2 mM: 1.049342 / 1.106318 / 1.147081 here against 1.049342 / 1.106318 / 1.147081 published — departures **2.0e−7 to 3.7e−7**, i.e. the rounding of the file it is checked against. And `C-0018`'s twelve coupled margins at `μ ≡ 1`: 1.00708 / 1.02644 / 1.01602 / 1.01289 / 1.03170 / 1.02769 at 10 nm and 1.11384 / 1.06051 / 1.12665 / 1.02162 / 1.44114 / 1.00376 at 7 nm — **every digit `C-0018` published.**

## The declared falsifiers, and which fired

Per §5 and §7, the Plan named five results that would falsify the approach.

| # | falsifier | fired? |
|---|---|---|
| 1 | `μ` depending appreciably on the bias (> 2 %) | **no** — 0.29 % and 0.56 % |
| 2 | `μ` falling with the gap anywhere in range | **no** — positive at all 30 sweep gaps |
| 3 | `μ ≡ 1` failing to reproduce `C-0018` | **no** — every published digit |
| 4 | the gradient failing to converge in the mesh | **no** — 0.11 % at the sweep mesh, 15× inside the spread it collapses |
| 5 | the margin moving by less than its own numerical resolution | **no** — 0.9–3.6 % against a `1.4e−5` floor |

**None of the five fired, and an undeclared sixth did**: the *direction* of the margin movement is not a property of the correction at all but of whether the fold is deeper or shallower than §3's stroke. That is `P4`'s sign rule, and it is what makes this claim more than a tightening.

## Still open — named, not answered

1. **The 3-D corner is still not solved.** Both mappings are emitted; the bracket is 1.8 percentage points of total force at 40 nm and about 7 % of `d ln μ/dh`.
2. **`C-0019`'s half is still a bracket, and it is now the whole of what is unresolved at the fold.** The combined tangent is −0.813 to +1.156 pN/nm and its sign is a *model* choice among `C-0003`'s six free energies. Only a fluctuation-corrected layer (`T-51`) collapses it.
3. **`C-0032`'s strain-softening coupling is not carried here.** It moves the same 10 nm / 2 mM margin to 1.0000–1.0019 on an affine-line comparison; this claim's `+1.4 %` is computed on `C-0018`'s affine line and the two corrections have not been composed. **That composition is the next thing worth doing on this margin**, and it is cheap.
4. **The PEG layer is not in the 2-D solve.** `C-0005`'s partitioning layer amplifies the 1-D force by 1.15–1.60×; whether it moves the collar *ratio* is still not computed and is now the largest unexamined lever on `d ln μ/dh` (`T-3d`).
5. **A finite counter-electrode** would have its own edge and the two would not add — `C-0022`'s own most likely route to a smaller collar, still open.
6. **Only two `(height, buffer)` states.** The other 43 coupled states are bound by `φ = 0.2`, so nothing moves there; but the sign rule of `P4` predicts the direction anywhere and has been tested at two states only.

## Challenges

[`CH-0051`](../challenges/CH-0051-the-pull-in-bias-falls-too.md) is raised **by** this claim against `C-0027`'s pull-in propagation — its `≥ 1.108–1.134` lower bound and its "the operating bias falls, unambiguously" framing, not its measurement or its decomposition.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
