# C-0019 — The polymer mean field is broken at the Gen-1 layer, and the layer's response is bounded anyway

| | |
|---|---|
| **Task** | [`T-1f`](../tasks/T-1f-mean-field-fluctuation-corrections.md) |
| **Leaf** | `A2.1` |
| **Verification type** | **in-silico** (a closed-form Ginzburg/one-loop evaluation on `C-0002`'s and `C-0003`'s measured parameters, then `C-0011`'s solved SCF layer re-run over the range the broken expansion licences) **+ logical** (which field each loop expansion is an expansion of) |
| **Verdict** | **PASS — and the answer has two halves that must be quoted together.** The correction is **NOT bounded perturbatively** (`Gi = 0.71 – 1.30` at the design points, 0.30 – 1.71 across the window), and the **layer response IS bounded non-perturbatively**: **`k_brush` −9.4 % / +0, stroke +2.0 % / −0** at 10 nm. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** No fluctuation-corrected profile is computed anywhere. |
| **Provenance** | `gpd/results/T-1f-mean-field-fluctuation-corrections.json`, produced by `brush.FluctuationCorrectionStudyKt`; 18 Ginzburg records, 6 thermal-blob records, 18 interaction-sensitivity records, 6 swelling records, 34 propagation records, 9 convergence records; **18 gate-named `brush` tests, 931 in the suite, 0 failures**; ~20 min wall clock, single-threaded; the result file re-run on an independent snapshot and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, aqueous, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile (A = 1600 nm²); linear PEG; `L₀` a force-onset height at 1.0 pN over the tile |
| **Consumes** | [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `n_K`, `α`), [`C-0003`](C-0003-crossover-valid-layer-response.md) (`A₂`, the interaction bracket, the `K` sensitivity theorem), [`C-0011`](C-0011-scf-density-profile.md) (the solved layer, **re-run not tabulated**), [`C-0016`](C-0016-design-window.md) (the window edges, re-solved as roots) |
| **Raises** | [`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) and [`CH-0020`](../challenges/CH-0020-thermal-blob-coarse-graining.md) |

---

## THE CONVENTIONS — read these before any number below

- A **volume fraction** is always the physical one, `φ = c v₀`. Never a reduced density.
- **The excluded volume is a PAIR quantity and does not coarse-grain linearly**: `v_m = B v₀ = 12.245 Å³`
  on monomers, **`v_K = n_K² v_m = 118.43 Å³`** on Kuhn segments. Every formula containing `b` needs the
  Kuhn reading. This is `CH-0020`, and it is asserted as a test rather than argued.
- **`L₀` is a FORCE-ONSET height** at 1.0 pN over the tile (`C-0011`, `CH-0010`).
- **A fluctuation correction is quoted as a multiplicative bracket on a NAMED response quantity at a
  NAMED compression** — never as "the correction to the layer stiffness" (`C-0001`, `S-1`).
- **`Gz` and `Gi` are two different numbers and both are reported.** `Gz = √(v/(c b⁶))` is the bare
  Ginzburg parameter of the literature; `Gi = (2√3/π)·Gz = 1.1027 Gz` is the ratio of the one-loop
  *pressure* correction to the leading one. Quoting one under the other's name is worth 10 %.

---

## The claim, in one line

**The loop expansion whose saddle point is the self-consistent field is *broken* at the Gen-1 layer —
`|ΔΠ|/Π_MF = 1.30` at the design point's mean volume fraction, so the one-loop term would drive the
interaction pressure negative — and the layer's response is bounded to under ten per cent anyway,
because `C-0011`'s disjoining pressure is conformational and not interactional, so the whole
interaction can be destroyed and the layer barely notices.**

---

## Part 1 — the perturbative half: no bound exists, and this is the same sentence `C-0005` writes

`ξ = b/√(12 v c)`, `Δf = −k_BT/(12π ξ³)`, `ΔΠ = ½Δf` (because `Δf ∝ c^{3/2}` exactly), so

&nbsp;&nbsp;&nbsp;&nbsp;**`Gi(φ) = |ΔΠ|/Π_MF = √(φ** over φ)`, &nbsp;&nbsp;`φ** = 12 v_K w_K/(π² b⁶) = 0.015255`**

`Gi` is **independent of the chain length**. Evaluated on the solved profile:

| design point | where | `φ` | `φ/φ**` | `ξ` [nm] | `ξ/R₀` | `Gz` | **`Gi`** | verdict |
|---|---|---|---|---|---|---|---|---|
| **10 nm** | profile mean at `L₀` | 0.00900 | 0.590 | 4.215 | 0.857 | 1.181 | **1.302** | **BROKEN** |
| **10 nm** | profile peak at `L₀` | 0.02459 | 1.612 | 2.549 | 0.519 | 0.714 | **0.788** | marginal |
| **10 nm** | mean at the held gap `L₀ − 3 nm` | 0.01285 | 0.842 | 3.526 | 0.717 | 0.988 | **1.090** | **BROKEN** |
| **7 nm** | profile mean at `L₀` | 0.00998 | 0.654 | 4.001 | 1.265 | 1.121 | **1.236** | **BROKEN** |
| **7 nm** | profile peak at `L₀` | 0.03022 | 1.981 | 2.299 | 0.727 | 0.644 | **0.711** | marginal |
| **7 nm** | mean at the held gap | 0.01747 | 1.145 | 3.025 | 0.956 | 0.848 | **0.935** | marginal |

Across the whole `C-0016` window (edges included) `Gi` runs **0.304 to 1.714**.

Three things follow and the third is the reason no one-loop number is quoted anywhere below.

1. **`Gi` straddles unity inside a single profile.** At the 10 nm design point the *mean* is above the
   crossover in the fluctuation-dominated sense (`Gi = 1.30`) and the *peak* below it (`Gi = 0.79`).
   **There is no regime label for this layer** — it sits below `φ**` at its mean and above it at its
   peak, and it is *also* below `φ#`, so it is not in the fully-developed des Cloizeaux regime either.
2. **The geometric statement of the same marginality**: `ξ = 4.215 nm` against `R₀ = 4.916 nm`, i.e.
   the concentration correlation length *is* the coil, and there are **1.34 chains per correlation
   area**. There is no scale separation for a semidilute description to live in.
3. **Adding `Δf` to `f_int` would make `Π_int` negative below `φ**`.** That is not a correction, it is
   the signature of a broken expansion, and this task **committed in advance** not to run it. The
   language is `C-0005`'s, deliberately: *above 1 the expansion has broken down, and one cannot say
   from within the theory in which direction to correct it or by how much.*

**So the perturbative answer to `T-1f` is: unbounded, and the method that would bound it is named and
costed below.** What follows is a different argument.

---

## Part 2 — the non-perturbative half, which is the deliverable

Two facts the programme already owns make a **two-sided** bound available with the expansion broken.

- **The sign is not in doubt.** The one-loop term is negative (Debye-Hückel-like), so fluctuations can
  only *reduce* the interaction. The licensed range of the interaction strength is `K/K₀ ∈ [0, 1]`.
- **`K → 0` is not a singular limit here.** `C-0011`: at an absorbing wall `Π_int(φ(h)) ≡ 0` and the
  disjoining pressure is *entirely conformational*. So the interaction-free layer still holds the tile,
  still has a resting height, and still has a stiffness. It is a **computable floor**.

### The solved layer over four decades of interaction strength, at `L₀ = 10 nm`, `σ = 0.024 nm⁻²`

| `K/K₀` | `N` | PEG [kDa] | mean `φ` | `2⟨z⟩` [nm] | `k(0.8L₀)` | **`k_brush(L₀−3)`** | `k_sec` | stroke [nm] |
|---|---|---|---|---|---|---|---|---|
| **1e-6** (interaction destroyed) | **64.57** | 2.844 | 0.00935 | 5.417 | **6.101** | **12.661** | 18.555 | **5.3895** |
| 1e-3 | 64.57 | 2.844 | 0.00935 | 5.417 | 6.102 | 12.662 | 18.555 | 5.3894 |
| 0.1 | 64.29 | 2.832 | 0.00931 | 5.422 | 6.149 | 12.770 | 18.584 | 5.3809 |
| 0.5 | 63.25 | 2.786 | 0.00916 | 5.439 | 6.329 | 13.189 | 18.700 | 5.3477 |
| **1.0** (the measured limb) | **62.11** | **2.736** | **0.00900** | **5.459** | **6.538** | **13.678** | **18.836** | **5.3090** |
| 2.0 | 60.20 | 2.652 | 0.00872 | 5.494 | 6.915 | 14.564 | 19.087 | 5.2393 |
| 4.0 | 57.31 | 2.525 | 0.00830 | 5.552 | 7.554 | 16.082 | 19.519 | 5.1233 |
| two-body limb (mean-field end) | 63.04 | 2.777 | 0.00913 | 5.437 | 6.338 | 13.179 | 18.679 | 5.3535 |

`N = 62.11`, `2⟨z⟩ = 5.459 nm` and `k_sec = 18.836 pN/nm` reproduce `C-0011`'s 62.1, 5.459 and 18.84
exactly — which is what says this study and `T-1d` are running the same layer.

> **Over `4 × 10⁶` in interaction strength the chain length moves by 13 % and the stroke by 5 %.**

### The bracket, in the form downstream needs it

**Licensed** = `K/K₀ ∈ [0, 1]`, which is the whole of what a negative one-loop term can do.
**Conformational** = the intrachain swelling channel of Part 3.

| quantity, at the stated compression | baseline | **interaction, licensed** | **conformational** | **combined** |
|---|---|---|---|---|
| `k_brush` at the held gap `L₀ − 3 nm`, 10 nm | 13.678 pN/nm | ×[0.926, 1] | ×[0.979, 1] | **×[0.906, 1] — `−9.4 %`** |
| `k(0.8 L₀)`, 10 nm | 6.538 pN/nm | ×[0.933, 1] | ×[0.981, 1] | **×[0.915, 1]** |
| stroke at 100 pN, 10 nm | 5.309 nm | ×[1, 1.015] | ×[1, 1.004] | **×[1, 1.020] — `+2.0 %`** |
| `N(L₀)`, 10 nm | 62.11 | ×[1, 1.040] | ×[0.860, 1] | **×[0.860, 1.040] → `N` = 53.4 – 64.6, 2.35 – 2.84 kDa** |
| `k_brush` at the held gap, 7 nm | 102.22 pN/nm | ×[0.957, 1] | ×[0.991, 1] | **×[0.949, 1] — `−5.1 %`** |
| stroke at 100 pN, 7 nm | 3.125 nm | ×[1, 1.011] | ×[1, 1.002] | **×[1, 1.014]** |

**Both channels run the same way on the stiffness — softer — and the same way on the stroke — longer.
Neither is large.** For comparison, the entire three-law interaction bracket the programme carries
(two-body against des Cloizeaux) is worth **3.6 %** on `k_brush` at 10 nm and **2.3 %** at 7 nm.

---

## Part 3 — the conformational channel, which nobody had costed

A chain in a self-consistent field is Gaussian *in that field*: its self-avoidance with itself — the
correlation hole — is exactly what a mean field averages away. To first order (Yamakawa Eq. 13.32),
`z = (3/2πb²)^{3/2} v_K √n` and `α² = 1 + 4z/3`, screened at the correlation blob inside the layer.

| `N` | Kuhn segments | `z` | `α` free | `α` screened |
|---|---|---|---|---|
| 25.7 (7 nm design point) | 8.27 | 0.0844 | 1.0548 | 1.0548 |
| **62.1 (10 nm design point)** | **19.97** | **0.1312** | **1.0839** | **1.0724** |
| 199.4 (`C-0001`'s chain) | 64.12 | 0.2351 | 1.1460 | 1.0724 |
| 375 (top of the design space) | 120.58 | 0.3224 | 1.1958 | 1.0724 |

**The Gen-1 chains are 5–20 % swollen**, which is not what "0.06 of a thermal blob" reads as. `CH-0020`.

Entering the solved layer as an effective segment length `b_eff = α b`:

| 10 nm design point | `α` | `b_eff` [nm] | `N` | `k(0.8L₀)` | `k_brush(L₀−3)` | stroke | **overlap edge** | **stroke edge** | width |
|---|---|---|---|---|---|---|---|---|---|
| mean field | 1.0000 | 1.1000 | 62.11 | 6.538 | 13.678 | 5.3090 | 0.010854 | 0.27770 | 25.59 |
| screened swelling | 1.0724 | 1.1796 | 54.53 | 6.427 | 13.419 | 5.3294 | 0.010774 | 0.30725 | 28.54 |
| free-chain swelling | 1.0839 | 1.1923 | 53.44 | 6.413 | 13.385 | 5.3321 | 0.010755 | 0.31191 | 29.00 |

### The finding, and it is not the one the identity predicts

**At fixed chain length, coil overlap `Σ = πR₀²σ` scales exactly as `α²` — asserted as a test — so the
naive propagation says `C-0016`'s lower edge moves by `1/α² = 0.87`, a 15 % widening. It does not.**

> The measured shift is **0.92 %** at 10 nm and **0.34 %** at 7 nm, against an `α²` of 1.150, because
> **the chain length moves against it**: a swollen chain reaches the same height with 12–14 % fewer
> monomers, and a shorter chain has a smaller coil. The two effects cancel to within one per cent.

This is the **third** near-cancellation of this kind in the project — after `C-0003`'s `k ∝ K^{1/(m+1)}`
and this task's own interaction channel — and all three have the same cause: `L₀` is specified and `N`
follows, so any perturbation is partly absorbed by the chain length rather than by the response.

**The windows therefore widen at the TOP, not the bottom**, by **+12.3 %** (10 nm) and **+1.4 %** (7 nm)
in the stroke edge, giving window widths 25.59 → 29.00 and 1.874 → 1.907.

---

## Part 4 — the two propagations the programme's verdicts rest on

### (a) `C-0017`'s 10 nm coupling margin — **it does not move, and `T-1f` is not what could move it**

The stability floor is `|k_eff| = |k_brush + k_es|` with `∂floor/∂k_brush = −1` exactly. Fluctuations
can only *reduce* `k_brush`, which *raises* the floor. Taking `C-0017`'s own worst state (floor 27.91,
`k_brush` ≤ 35.6 pN/nm) and this claim's −9.4 %:

&nbsp;&nbsp;&nbsp;&nbsp;floor ≤ 27.91 + 0.094 × 35.6 = **31.26 pN/nm**, so the margin `33.333/floor` ≥ **1.066×**

> **`C-0017`'s verdict is unchanged: §3's own mandated 33.333 pN/nm still clears the stability floor at
> every one of its 54 states.** The margin degrades from ≥ 1.19× to ≥ 1.07× and stays above one.

**And that is the smaller half of the point.** `CH-0019`: the 123–214 % `C-0017` quotes as its dominant
uncertainty is `C-0005`'s **electrostatic** loop expansion, which corrects `k_es`. This claim's Ginzburg
number is the **polymer** loop expansion, which corrects `k_brush`. They act on the two terms of
`k_eff` and neither bounds the other. **Bounding the polymer correction does not establish `C-0017`'s
verdict, and no task in the queue does.**

**One caveat, stated because it runs against this claim.** The −9.4 % is measured on the **solved** layer.
`C-0017` ran `C-0003`'s two *ansatz* models, whose pressure **is** the interaction and for which
`k ∝ K^{1/(m+1)}` — so on *those* models the same licensed range `K → 0` takes the stiffness to zero
and the bracket is **unbounded below**. The solved layer is the defensible one (`C-0011`, `CH-0010`), but
a reader who prefers `C-0003`'s models gets no bound from this claim at all.

### (b) `C-0016`'s window edges — the windows get **wider**

The edges are re-solved here as **roots** rather than located on `C-0016`'s 61-point grid, and the
baseline reproduces it within its own stated resolution of 1.109× per step:

| | `C-0016` (grid) | this claim (root, baseline) | departure | **with swelling** |
|---|---|---|---|---|
| 10 nm lower | 0.011630 | 0.010854 | −6.7 % | **0.010755** |
| 10 nm upper | 0.260150 | 0.277699 | +6.7 % | **0.311910** |
| 7 nm lower | 0.029550 | 0.029347 | −0.7 % | **0.029248** |
| 7 nm upper | 0.049600 | 0.054987 | +10.9 % | **0.055769** |

**No window closes, no edge changes owner, and both widen** — 10 nm by 13.4 % and 7 nm by 1.8 %.

### (c) What this claim does **NOT** say about `T-21`'s `φ ≈ 0.2` ceiling

`C-0018` makes `C-0002`'s `φ ≈ 0.2` concentrated crossover the binding ceiling on usable bias at 121 of
162 states, and asks whether this task can move it. **It cannot, and the reason is worth recording:**

> **`φ** = 0.0153` and `φ ≈ 0.2` are different objects.** `φ**` is where the *fluctuation* expansion
> crosses unity, and mean field gets **safer** above it — at `φ = 0.2`, `Gi = 0.276`, the most
> controlled the polymer mean field ever is in this device. `C-0002`'s 0.2 is not a theory boundary
> at all; it is the **upper end of the concentration range the equation of state was fitted over**.
> Nothing in a Ginzburg argument licenses moving a data range. `T-21` needs osmometry, not theory.

---

## The declared falsifiers, and what actually happened

| # | fired? | outcome |
|---|---|---|
| 1 — `Gi ≪ 1`, mean field controlled | **no** | `Gi = 0.71 – 1.30` at the design points, 0.30 – 1.71 across the window |
| 2 — `Gi ≫ 1` **and** a strongly `K`-sensitive response, i.e. no bound at all | **half** | `Gi > 1` did fire; the response sensitivity did not, and that is the whole claim |
| 3 — `C-0003`'s `k ∝ K^{1/(m+1)}` reproducing on the solved layer | **YES** | measured `d ln k/d ln K` = **0.0647** against `1/(m+1) = 0.3077` — **4.75× smaller**. `C-0003`'s exponent is a property of its two ansatz profiles and does **not** transfer |
| 4 — the `K → 0` layer failing to hold the tile | **no** | it reaches `L₀ = 10 nm` at `N = 64.57` and delivers `k(0.8L₀) = 6.101 pN/nm` and a 5.39 nm stroke. A floor, not a singularity |
| 5 — the thermal blob reproducing `C-0003`'s 1222 | **YES, and with a twist** | 126.3 Kuhn segments in the scaling normalisation — `n_K² = 9.67` smaller — **but 1160 in Yamakawa's exact one, within 5.3 % of `C-0003`'s number.** Two conventions that nearly cancel. `CH-0020` |
| 6 — a verdict changing rather than an uncertainty bounding | **no** | `C-0017`'s margin stays above one, both `C-0016` windows widen, no edge changes owner |

---

## Validity range

- **TRL 1–3. Nothing here is measured.** `PASS` means model-consistent and traceable.
- **NO fluctuation-corrected profile is computed.** The bound is a bracket obtained by re-running the
  **mean-field** solver over the range a broken expansion licences. Adding the one-loop term to `f_int`
  would drive `Π_int` negative below `φ**`; that is the signature of the breakdown, not a correction.
- **The interaction floor assumes fluctuations cannot change the SIGN of `Π_int`.** A net-attractive
  layer is outside the family of free energies used anywhere in this programme (`CLAUDE.md`); the
  boundary is inherited, not tested.
- **The conformational channel is FIRST ORDER in `z`**, and `z = 0.32` at the top of the design space —
  outside Yamakawa's own `|z| < 0.15` band, where he reports the second- and third-order series
  agreeing only below that. At the two **design points** `z = 0.084` and `0.131`, inside it. The
  swelling is applied as a **uniform** `b_eff`, not as a self-consistent `α(z)`.
- **Lateral fluctuations are not treated at all.** The correction here is to a 1-D field, and lateral
  inhomogeneity is a separate omission `C-0011` also names — one that Lai & Binder report becomes
  *qualitative* for quenched grafting points below the theta point.
- **The electrostatic loop expansion is NOT in scope and is NOT narrowed.** `C-0005` owns it. `CH-0019`.
- **`Gz` is derived for Gaussian chains with a two-body excluded volume**, evaluated on the measured
  PEG/water `v` — but the RPA structure factor behind it is the Edwards model's, not a fit to PEG.
- **The window edges are roots on `C-0011`'s solved layer under the des Cloizeaux limb only.**
  `C-0016`'s edges are grid points over three interaction laws and two brush criteria; the two agree
  to within one grid ratio but they are not the same construction.
- **Every osmotic input is still a BULK property applied to a BRUSH** (`P-9`, `C-0013`).
- **`C-0022`/`CH-0026`'s 5–19 % electrostatic edge enhancement is NOT carried.** It postdates the
  `C-0017` numbers this claim propagates to, and it moves `k_es`, not `k_brush`.

## Numbers that are CITED rather than DERIVED

| number | value | why it is cited, and what it moves |
|---|---|---|
| `Δf = −k_BT/(12πξ³)`, `ξ² = b²/(12vc)` | — | **CITED FORMULA, DERIVED EVALUATION.** Wittmer et al., *J. Stat. Phys.* **145**:1017 (2011), arXiv:1107.4454, **Eq. (69)**, attributed there to Doi & Edwards, *The Theory of Polymer Dynamics* (1986) Eqs. (5.45)–(5.46). Read in the arXiv PDF, not in a summary |
| `Gz ≡ √(vρ)/(ρb³)` and `Gz ≪ 1` as the criterion | — | **CITED**, same paper **Eq. (48)**, and Wittmer et al., *Phys. Rev. E* **76**:011803 (2007), arXiv:0704.1620 **Eq. (6)** |
| `δ(1/g)/(1/g) = −(3√3/2π)Gz` | −0.827 `Gz` | **CITED**, same paper **Eq. (99)** — the one member of this family checked against simulation over three decades. **Reproduced here from `Δf` as a test**, which is this claim's gate-5 handle |
| `z = (3/2πb²)^{3/2} β√n`, `α² = 1 + 4z/3` | prefactor 0.32992 | **CITED**, Yamakawa, *Modern Theory of Polymer Solutions* (1971) **Eqs. (13.32), (13.33)**, with `β` his binary cluster integral (**Eq. 13.3**), i.e. the pair excluded volume. Read in the author-approved Kyoto electronic edition |
| the thermal blob's prefactor is a ~30× convention bracket | `c ≈ 0.1` and `c ≈ 1` | **CITED**, Schroeder, *J. Rheol.* **62**:371 (2018), arXiv:1712.03555 §II.A.5. It is what makes `CH-0020` a challenge to an *inference* rather than only to a number |
| `A₂ = 1.9e-3 mol·cm³/g²` → `B = 0.20291` | — | **CITED via `C-0003`**, itself read in a re-tabulation. Everything in Part 1 scales as `√v` |
| `b = 1.1 nm`, `M_K = 137 g/mol`, `α = 0.49` | — | **CITED via `C-0002`** |
| `C-0017`'s floor 23.41–27.91 and `k_brush` 11.7–35.6 pN/nm | — | **CITED**, and the propagation in Part 4(a) uses them with an **exact** derivative rather than re-running `coupling/` |
| `C-0016`'s window edges | — | **CITED for comparison only**; the edges used here are re-solved as roots |
| `C-0005`'s 123–214 % | — | **CITED.** `CH-0019` exists to stop it being confused with this claim's number |

Everything else — `ξ`, `Gz`, `Gi`, `φ**`, `v_K`, the corrected blob, `z`, `α`, every SCF response in
Parts 2–3, both window edges and every propagation — is **derived here**.

## Cross-checks passed

Executed as **18 gate-named `brush` tests**; detail in [`T-1f`](../tasks/T-1f-mean-field-fluctuation-corrections.md#verify).

- **Gate 1** — `Gi`, `ξ` and `ΔΠ` computed on **monomers** and on **Kuhn segments** agree to **1e-12**,
  which is the executable form of `CH-0020` and holds *only* with `v_K = n_K² v_m`; the mean-field
  denominator is `C-0003`'s own `twoBodyInteraction` to 1e-12; `v_K/(n_K² v_m) = 1` and
  `incumbent/corrected = n_K²` exactly; unphysical arguments throw.
- **Gate 2** — `Gi ∝ φ^{−1/2}` with an observed log-log slope of **−0.5 to 1e-9**; `Gi(φ**) = 1` as a
  root; `ΔΠ = ½Δf` to 1e-12 and negative at every `φ`; swelling vanishes as `v → 0` and the screened
  `α` never exceeds the free one and equals it at infinite screening; the `K → 0` layer converges —
  `k(0.8L₀)` = 6.10613, 6.10186, 6.10143, 6.10139 at `K/K₀` = 1e-2, 1e-3, 1e-4, 1e-6, i.e. **7.8e-4
  then 7.8e-5 then 7.7e-6** relative.
- **Gate 3** — `Σ` scales exactly as `α²` under `b → αb` at fixed `N` (1e-12), which is what makes the
  1 % measured shift a *result* rather than a discrepancy; scaling an interaction scales its pressure
  and leaves its exponent alone; grafted coverage `∫φ dz = Nσ` conserved to 1e-9 at `K/K₀` = 1e-4, 1, 2.
- **Gate 4** — `d ln k/d ln K` at `Δz` = 0.4 / 0.2 / 0.1 nm is **0.05983 / 0.06387 / 0.06475**,
  departures 4.9e-3 then 8.8e-4 from the finest; the `K → 0` floor exhibited over four decades as
  above; **and the window edge is reported with a measured grid sensitivity of 23.4 %** between
  `Δz` = 0.4 and 0.2 nm, which is why every edge quoted here is on the 0.2 nm grid and none on 0.4.
- **Gate 5** — Wittmer's **Eq. (99)** coefficient reproduced from `Δf` (all four ratios, 1e-12); the
  thermal blob agrees between the monomer and Kuhn conventions to 1e-9; `1/0.32992² = 9.18704` against
  `n_K² = 9.67142` asserted as the near-cancellation `CH-0020` rests on, with `incumbent/Yamakawa`
  within 6 %; the layer confirmed to sit **below** `φ**` and `ξ/R₀` inside `[0.5, 2]` at the design
  point; **`C-0011`'s `N = 62.1`, `2⟨z⟩ = 5.459 nm` and `k_sec = 18.84 pN/nm` reproduced by re-running
  it**; `C-0016`'s four window edges reproduced by an independent root to within its own 1.109× grid.
- **Reproducibility** — the study re-run end to end on a second independent snapshot and the result
  file **diffed byte-for-byte identical**, and the full suite is **931 tests, 0 failures**.

**Literature corroboration on the direction, which is not a gate but is worth recording.** Every
comparison of simulation against brush SCF found in the search runs the way this claim's sign does —
correlations *reduce* the layer's pressure at a given density: Cerdà, Sintes & Toral (arXiv:cond-mat/0406075)
report a compression exponent of **2.73 ± 0.04** from Monte Carlo against **2.15 ± 0.05** from SCF, and
attribute it in terms to *"an overestimation of the monomer interactions … the SCF method does not
include the effect of monomer correlations"*; Manav, Ponga & Phani (arXiv:1811.05089) get **2.135 ± 0.032**
from MD against **1.743 ± 0.016** from strong stretching, at **low** grafting density. **The effect is
real, published, and largest exactly where Gen-1 sits.** What none of them supplies is a *bound*.

## Still open — named, not answered

1. **The method that would actually bound this is a field-theoretic simulation** — complex-Langevin
   sampling of the full Edwards functional integral for the grafted layer, which is exact in the
   fluctuations and does not expand about the saddle point at all. **Not run**, and costed at weeks of
   wall clock for a sweep over grafting density on this box. The published near-substitute is Q. Wang's
   fast-lattice Monte Carlo versus lattice SCF programme, whose deviation converges as **1/C** in the
   chain number density — the structure a one-loop correction has — and whose brush paper
   (Zhang & Wang, *J. Chem. Phys.* **140**:044906 (2014)) covers exactly this geometry, an impenetrable
   compressing surface, and **is paywalled with no repository copy**.
2. **No one-loop brush SCFT and no brush Ginzburg criterion exist in print.** Searched by arXiv API,
   Crossref title query and author sweep. The "fluctuations" in Netz & Schick, Matsen and Kim & Matsen
   are conformational fluctuations *within* mean field, not corrections *to* it. **This claim's
   transfer of the bulk `Gz` to the layer is a one-line derivation from Wittmer Eq. (69) — the
   translational term is separate and additive and the one-loop term has no `N` — not a citation.**
3. **`C-0003`'s `k ∝ K^{1/(m+1)}` needs an annotation**, not a withdrawal: it is exact for its two
   ansatz profiles and 4.75× wrong for the solved layer. Falsifier 3.
4. **The swelling is a uniform `b_eff`.** A self-consistent treatment would let `α` vary with `z`,
   since the screening length does — `ξ` runs 2.5 to 4.2 nm across one profile.
5. **`T-21`'s `φ ≈ 0.2` cannot be closed from here**, and Part 4(c) says why.

## Challenges

**Raises** [`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) against the standing rationale
that this task bounds `C-0017`'s exposure, and
[`CH-0020`](../challenges/CH-0020-thermal-blob-coarse-graining.md) against `C-0003`'s thermal-blob count
and the inference built on it.

**None stands against this claim.** The two ways it would fail:

1. **Fluctuations changing the sign of the effective interaction**, which would put the layer outside
   the family of free energies the whole programme uses and make the `K ∈ [0, 1]` floor meaningless.
2. **The `K` sensitivity being an artefact of the absorbing wall.** Everything in Part 2 rests on
   `Π_int(φ(h)) ≡ 0` at the tile. `C-0011` prices the opposite extreme — a reflecting wall — and finds
   it needs 2.1× the chain and delivers the same stroke to 2 %; a weakly *adsorbing* tile is outside
   that bracket and outside this claim.
