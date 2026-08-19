# C-0036 — The semidilute→concentrated crossover is a one-parameter family, its physical value for this material is 0.004–0.032, and the des Cloizeaux exponent the whole band was guarding is one the Gen-1 layer was never entitled to

| | |
|---|---|
| **Task** | [`T-21`](../tasks/T-21-concentrated-crossover.md) |
| **Leaf** | `A2.1` (premise), consumed by `A2.2` |
| **Verification type** | **logical** (a closed-form crossover family on `C-0002`'s measured parameters, and an exact window identity) **+ in-silico** (`C-0018`'s 162 bias ceilings re-read on `C-0018`'s own `EquilibriumPath` + `PoissonBoltzmannGap` pipeline at ten candidate crossovers) |
| **Verdict** | **PASS on `P1`–`P4`.** The acceptance predicate is answered, and the answer is that the question has no interior solution: the des Cloizeaux window is empty for every Gen-1 chain. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The crossover family is a scaling construction on measured parameters; only the equation of state's fitted range is a measured boundary |
| **Provenance** | `gpd/results/T-21-concentrated-crossover.json`, produced by `crossover.ConcentratedCrossoverStudyKt`; 21 gate-named `crossover` tests; the family, 18 windows, 4 corners, 1620 ceiling cells, 10 census rows and 7 reproductions |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous, and the crossover itself is **salt-free** (`C-0007`: the buffer is worth ≤ 3.3 % of the excluded volume); the propagation is at `MgCl₂` 0.5 / 2 / 10 mM on `C-0001`'s three design points |
| **Consumes** | [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `M_K`, `V̄`), [`C-0003`](C-0003-crossover-valid-layer-response.md) (`A₂`), [`C-0007`](C-0007-solvent-quality-vs-salt.md) (`χ(T)`, the second excluded-volume route), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md)/[`CH-0020`](../challenges/CH-0020-thermal-blob-coarse-graining.md) (`n_K²`, the thermal-blob normalisation bracket), [`C-0018`](C-0018-maximum-usable-bias.md) (the ceiling table and the pull-in bias) |
| **Raises** | [`CH-0048`](../challenges/CH-0048-the-good-solvent-premise-was-checked-on-monomers.md) against `C-0007`; [`CH-0049`](../challenges/CH-0049-the-cited-band-is-a-reduced-density-and-the-fit-range-is-wrong.md) against `C-0002` |
| **Answers** | `C-0018`'s open item 2 — *"the `φ = 0.2` ceiling is doing most of the work and it is a cited number"* |

---

## THE CONVENTIONS — read these before any number below

- **A volume fraction is the PHYSICAL one**, `φ = c v₀ = c_K v_K = N σ v₀ / h`. Three conventions are
  in circulation and they differ by fixed factors:
  the Alexander-de Gennes reduced density `n a³` is **1.408× smaller** (`C-0002`);
  the **Kuhn** reduced density `c_K b³` used by every textbook statement of this crossover is
  **7.09× larger**, that factor being `C-0002`'s own Kuhn aspect ratio `b³/v_K`.
  **Every number below is physical, and the conversion is asserted as a test.**
- Excluded volume is a **pair** quantity: `v_K = n_K² v_m`, never `n_K v_m` (`CH-0020`).
- **A crossover is a convention until its definition is named.** Every value below is emitted with
  the number of Kuhn segments per correlation blob that selects it.
- The thermal blob carries a **published 9.19× normalisation bracket** (scaling `g_T = (b³/v)²`
  against Yamakawa's exact `z(g_T) = 1`). Both ends are carried; neither is called *the* value.
- `L₀` is a **force-onset** height (`C-0011`, `CH-0010`); the stroke `s = L₀ − h` is positive downward.
- **A bias ceiling belongs to a `(bias, load line)` pair** (`CH-0015`).

---

## The claim, in one line

**The upper crossover is not a number but a one-parameter family, `φ_c(n) = (v_K/b³)·n^(−1/2)`, whose member is fixed by naming how many Kuhn segments the correlation blob must keep; for PEG in water at 300 K the member that ends the des Cloizeaux exponent — `n = g_T` — is `φ** = 0.0041–0.0319`, which lies BELOW the layer's own resting volume fraction of 0.033–0.133 and below the dilute→semidilute crossover, so the window in which the layer is entitled to the `9/4` exponent is EMPTY at all 18 Gen-1 chains and in three of the four corners of the material bracket. Nothing in the family lands in the cited `0.2–0.3` band; the two constructions that do are the same textbook expression read on a monomer instead of a Kuhn segment, wrong by 16.2× and 7.09× respectively. Read as a regime ceiling the derived number is LOWER than 0.2 and the device is worse for it — at `φ = 0.141` the coupled margin falls from 0.563–2.464 to 0.168–1.660 and twenty coupled states drop below unity instead of fifteen. But the role `C-0018` gives the number is not a regime test, and on the axis it actually consumes — where the layer's constitutive law stops being supported by measurement — the replacement is `φ = 0.49–0.63`, at which the polymer ceiling stops binding anywhere and `C-0005`'s 1.46 nm correlation band takes over. The thinnest margin in the programme, 1.007–1.032 at 10 nm in 2 mM, is set by pull-in and does not move under any of this.**

---

## `P1` — the derived crossover, and the family it belongs to

### The derivation

Write the correlation blob as space filling, `n = φ ξ³/v_K` Kuhn segments, and its internal
statistics as Gaussian, `ξ = b√n` — the branch this material is on, `P2`. Eliminating `ξ`:

&nbsp;&nbsp;&nbsp;&nbsp;**`n(φ) = (v_K /(b³ φ))²`** &nbsp;⟺&nbsp; **`φ_c(n) = (v_K/b³) · n^(−1/2)`**

No free parameter, no fitted amplitude, and **naming `n` is naming the convention**. The textbook
form is the special case `v_K = b³`: Rubinstein & Colby's eq (5.36) `φ** ≈ v/b³` with its printed
remark that in the athermal limit `φ** ≈ 1`. Both are reproduced exactly (gate 5).

### The family, on `C-0002`'s measured parameters

| criterion | `n` | **`φ` (physical)** | provenance |
|---|---|---|---|
| `ξ = ξ_T`, Yamakawa exact `g_T` — **the textbook semidilute→concentrated boundary** | 1160.3 | **0.00414** | DERIVED |
| `ξ = ξ_T`, scaling `g_T` | 126.3 | **0.01255** | DERIVED |
| the same on `C-0007`'s Flory-Huggins `v`, Yamakawa / scaling | 179.4 / 19.5 | **0.01053 / 0.03191** | DERIVED |
| `ξ = b` — one Kuhn segment per blob, the blob picture's own floor | 1 | **0.14101** | DERIVED |
| `ξ = v₀^(1/3)` — extrapolated past that floor | — | **0.39544** | DERIVED |
| **`v_m/v₀` — the same criterion on MONOMERS** | — | **0.20291** | DERIVED **and wrong by construction** (`CH-0048`) |
| **`1 − 2χ` — R&C eq (5.36) + eq (5.1) on a monomer lattice** | — | **0.25667** | DERIVED on a forbidden convention |
| equation-of-state support, PEG-8000 (the Gen-1 chain length) | — | **0.49113** | CITED (54 wt %) + DERIVED |
| equation-of-state support, all twelve molecular weights | — | **0.63066** | CITED (67.5 wt %) + DERIVED |
| **the cited band** | — | **0.2 – 0.3** | **CITED, UNTRACED — the number this claim replaces** |

**The adopted answer to the acceptance predicate is the first row.** It is the crossover at which
the correlation blob stops being swollen, which is what "the des Cloizeaux exponent stops being
the one the layer is entitled to" means; it is the boundary Rubinstein & Colby themselves draw
between "semidilute good" and "concentrated"; and it needs no convention beyond the thermal-blob
normalisation, which is quoted at both ends. The one-Kuhn-segment member is reported beside it as
the *lowest* member a reader could defend as a "concentrated" boundary, and it is still below 0.2.

### Where the cited band comes from

Two constructions land in `0.2–0.3` and both are the segment identification `C-0002` exists to
forbid: `v_m/v₀ = 0.203` is the floor of the band to three digits (and is what `C-0007`'s parameter
sheet reports as *"the thermal-blob volume fraction"*, scaled by its own excluded volume);
`1 − 2χ = 0.257` is the middle of it. **No primary source places the PEG/water crossover at
0.2–0.3** — the negative existence claim, with its ~30 recorded queries, is in `CH-0049`.

---

## `P2` — the premise, checked before the picture is used, and it fails in the informative direction

`CLAUDE.md`: *"blob arguments do not apply to Gen-1 chains at all."* Made exact here.

### The identity that decides it

The des Cloizeaux window is `(φ*, φ**)`: above overlap and below the thermal-blob crossover.
Both edges are `φ_c(n)` at a different `n` — overlap is "the blob is the whole chain", `n = N_K` —
so the material prefactor cancels and

&nbsp;&nbsp;&nbsp;&nbsp;**`φ** / φ* = √(N_K/g_T)`, exactly**, for any material and any chain length.

Asserted to 1.5e−16, not observed. **The window is non-empty if and only if the chain is longer
than a thermal blob.**

### At every Gen-1 chain

| `L₀` | `N_K` | `φ*` | `φ**` | width `√(N_K/g_T)` | empty |
|---|---|---|---|---|---|
| 5 nm | 21.6 – 38.5 | 0.0227 – 0.0304 | 0.01255 | 0.413 – 0.552 | **yes** |
| 7 nm | 39.7 – 68.3 | 0.0171 – 0.0224 | 0.01255 | 0.561 – 0.736 | **yes** |
| 10 nm | 72.3 – 120.4 | 0.0129 – 0.0166 | 0.01255 | 0.757 – 0.976 | **yes** |

18 of 18. And the layer's own resting volume fraction, 0.033–0.133, is **2.6× to 26.5×** the
crossover, rising to 80× in the Yamakawa normalisation.

### The four corners — the emptiness is robust in three and survives the fourth

The material bracket is `2 × 2`: (thermal-blob normalisation) × (excluded-volume route), the
latter because this project has **two independent routes to `v_m` differing by 2.5×** —
`C-0003`'s osmometry `v_m = B v₀ = 0.01225 nm³` and `C-0007`'s Flory-Huggins
`v_m = v₀(v₀/v_water)(1 − 2χ) = 0.03114 nm³`.

| `v` route | normalisation | `g_T` | `φ**` | chains with an empty window | layer/`φ**` |
|---|---|---|---|---|---|
| `A₂` osmometry | scaling | 126.3 | 0.01255 | **18/18** | 2.6 – 26.5 |
| `A₂` osmometry | Yamakawa | 1160.3 | 0.00414 | **18/18** | 7.9 – 80.2 |
| Flory-Huggins `χ` | Yamakawa | 179.4 | 0.01053 | **18/18** | 3.1 – 31.5 |
| **Flory-Huggins `χ`** | **scaling** | **19.5** | **0.03191** | **0/18** | **1.0 – 10.4** |

**In the fourth corner the window exists** — up to 2.48× wide in `φ` — **and is still never
entered**: the layer sits at 1.0–4.2× its upper edge at rest and 1.5–10.4× at §3's 3 nm stroke.
This corner is reported rather than averaged away, because a claim that a window is empty must say
where it is not.

### The exponent survives as a measurement; only its warrant falls

This does **not** overturn `C-0002`'s equation of state. `α φ^(9/4)` is *fitted*, to twelve
molecular weights over `φ = 0.012–0.63`, with `r² = 0.9926`; a fit needs data, not a blob. Two
independent readings confirm that the *warrant* and not the *fit* is what moves:

- **from above** — the correlation blob is unswollen everywhere in the design space (this claim);
- **from below** — Hansen et al. (2003, read directly) *measure* the des Cloizeaux onset for
  PEG-5000 at `φ = 0.07–0.09` in their own reduced convention, i.e. **0.10–0.13 physical**, which
  is above the Gen-1 layer's 0.033–0.133 at rest for most of the design space.

Both say "not des Cloizeaux", and they agree with `C-0002`'s own `m_eff = 1.66–1.92`.
**The layer's exponent is a measured crossover exponent between 1 and 9/4, and it never had a
scaling regime to belong to.** (Note for gate-5 readers: the 0.408 "departure" on the Hansen row
of the result file is the reduced→physical conversion `v₀/a³ − 1`, not an error.)

---

## `P3` — the grafted layer inherits the UPPER crossover and does not inherit the lower one

`CH-0001`/`CH-0002` established that the *lower* crossover is a property of the **bulk solution**:
it is where chain translational entropy stops dominating, and grafting removes that term entirely,
so a brush must never carry a bulk `m_eff`.

**`φ_c(n)` contains no chain length at all.** It is a purely local structural statement about the
correlation length at a local concentration — the same structure `C-0019` found in the polymer
Ginzburg parameter, which *"contains no `N`… so a grafted layer carries the same fluctuation
correction as a bulk solution at the same local `φ`"*. The grafted layer therefore inherits the
upper crossover unchanged, evaluated at its own local `φ`.

The one thing that could break the inheritance is chain stretching *inside* the correlation blob.
The Gen-1 chains are extended to **0.076–0.211** of their contour, so the blob's internal
statistics are Gaussian to within the same approximation the layer models already make.

**Consequence for the models.** Three of `C-0003`'s six layer models are des Cloizeaux
constructions and this claim removes their theoretical warrant; the other three are mean-field
virial models, which is what the corrected premise says the material actually has. **None is
withdrawn** — they are a bracket, and a bracket is not an endorsement — but a reader choosing one
model should now know that the two-body/virial half is the warranted half.

---

## `P4` — the propagation, with the unfavourable direction stated first

`C-0018` computes `min(pull-in, C-0005's 1.46 nm gap, the crossover gap, CH-0007's 1.0 V)` on the
equilibrium path of a stated load line. The crossover enters through exactly one channel: the gap
`h_c = N σ v₀/φ_c` at which the layer reaches `φ_c`. **The pull-in bias cannot depend on `φ_c`**,
so it is read from `C-0018`'s result file (flagged CITED) and only the validity biases are
recomputed — on `C-0018`'s own solver, graded by reproducing `C-0018`'s own `φ = 0.2` ceiling to
**4.51e−9** over all 162 states.

### The census, over all 162 states

| `φ_c` | what it is | binds | pull-in | corr. band | **violated at rest** | coupled margin | coupled states < 1 |
|---|---|---|---|---|---|---|---|
| 0.0041 | **derived `φ**`**, Yamakawa | 0 | 41 | 99 | **162** | — | — |
| 0.0125 | **derived `φ**`**, scaling | 0 | 41 | 99 | **162** | — | — |
| **0.1410** | **derived `φ_c(1)`** | **120** | 41 | 1 | 0 | **0.168 – 1.660** | **20** |
| 0.2000 | **the cited floor — `C-0018`'s incumbent** | 121 | 41 | 0 | 0 | 0.563 – 2.464 | 15 |
| 0.2029 | `v_m/v₀`, mis-coarse-grained | 121 | 41 | 0 | 0 | 0.575 – 2.511 | 12 |
| 0.2567 | `1 − 2χ` on a monomer lattice | 90 | 41 | 31 | 0 | 0.773 – 3.401 | 3 |
| 0.3000 | the cited ceiling | 37 | 41 | 62 | 0 | 0.906 – 8.760 | 3 |
| 0.3954 | `ξ = v₀^(1/3)` | 9 | 41 | 90 | 0 | 1.004 – 8.760 | 0 |
| **0.4911** | **EOS support, PEG-8000** | **0** | 41 | 99 | 0 | **1.004 – 8.760** | **0** |
| 0.6307 | EOS support, all twelve | 0 | 41 | 99 | 0 | 1.004 – 8.760 | 0 |

### 1. Read as a regime ceiling, the derived number is LOWER and the device is worse

At `φ_c = 0.141` the usable bias falls from 0.097–0.229 V to 0.049–0.130 V at 5 nm in 0.5 mM,
and the coupled margin table moves against the device everywhere it moves at all:

| `L₀` | buffer | margin at 0.2 | **margin at 0.141** |
|---|---|---|---|
| 5 nm | 0.5 / 2 / 10 mM | 0.563–1.090 / 0.609–1.081 / 0.727–1.061 | **0.284–0.692 / 0.168–0.723 / 0.273–0.795** |
| 7 nm | 0.5 mM | 1.257–2.464 | **0.981–1.617** |
| 7 nm | 2 mM | 1.189–2.165 | **0.986–1.484** |
| 10 nm | 0.5 mM | 1.292–2.364 | **1.155–1.660** |
| **10 nm** | **2 mM** | **1.007–1.032 (pull-in)** | **1.007–1.032 — unchanged** |

**7 nm loses its clearance.** At 0.2 every one of the twelve coupled states at 7 nm in 0.5 and
2 mM had a margin above 1.18; at 0.141 two of them fall below unity. Every 5 nm margin roughly
halves. This is worse than the incumbent and it is reported as such.

### 2. Read as a regime ceiling at the *derived* `φ**`, it is not a ceiling at all

At `φ** = 0.0041–0.0125` the crossover gap is **above the resting height at 162 of 162 states**:
the criterion is violated at zero stroke, so no bias whatever would be usable and the actuator
does not exist. That is the reductio which shows the regime reading is **not the role this number
can play** — and it applies to the incumbent 0.2 just as much, since 0.2 was only ever a worse
estimate of the same quantity.

### 3. On the axis `C-0018` actually consumes, the ceiling saturates and stops mattering

`C-0018` reads the number as *"beyond this the layer model is not valid"*. The layer's
constitutive law is a **fit** plus **measured virial coefficients**; its validity boundary is the
range over which it was measured, `φ ≤ 0.49` (PEG-8000) or `≤ 0.63` (all twelve). At either:

- the crossover binds at **0 of 162** states;
- `C-0005`'s 1.46 nm correlation band takes over at **99**, and `CH-0007`'s 1.0 V at the rest;
- the coupled margin becomes **1.004–8.760 with no state below unity**;
- **and the two values give identical censuses** — above `φ ≈ 0.4` the crossover gap has fallen
  below 1.46 nm at every state, so a further increase in the crossover buys **nothing**.

That saturation is the practically important result: it is the same discipline `CLAUDE.md`
records for the tile's surface charge — *"check for saturation before spending an iteration
resolving a charge model"* — and it says the programme should stop resolving this number.

### 4. What moves, and what does not

| standing result | moves? |
|---|---|
| **`C-0018`'s 1.007–1.032 pull-in margin at 10 nm / 2 mM** | **NO.** Pull-in binds there under every candidate. The thinnest margin in the programme is untouched |
| `C-0018`'s "binding at 121 of 162" | **YES, and away from the polymer**: 121 → 0 on the support reading, 121 → 120 on the regime reading |
| `C-0018`'s usable-bias window 0.097–0.425 V (coupled) | **YES**, to 0.031–0.679 V (regime, `0.141`) or 0.130–1.000 V (support) |
| `C-0018`'s "`C-0005`'s band binds at 0 of 162" | **YES** — it becomes the binding ceiling at 99 of 162 on the support reading |
| **`C-0016`/`C-0027`'s window edges** | **NO.** `CLAUDE.md`: the upper edge is a **dead-load stroke** and the lower a grafting-density bound; neither is a bias ceiling, and `C-0027` records that `T-21`'s number reaches the window only through `C-0018` |
| **`C-0017`'s coupling stiffness and its 1.19–1.42× stability margin** | **NO.** 33.333 pN/nm is fixed by §3's own 100 pN and 3 nm by arithmetic, and the stability floor is `\|k_eff\|`, which contains no crossover |
| `C-0002`'s `α`, `φ#`, `m_eff`, `v₀`, `b`, `n_K` | **NO** — see `CH-0049`, which touches only its *bounding* statements |

---

## Validity range

- **TRL 1–3. NOTHING HERE IS MEASURED.**
- **The crossover family is a scaling construction**: every member carries an unknown O(1)
  prefactor. The family's own spread — 0.004 to 0.63, a factor of 152 — is the honest statement of
  that ignorance, and it is why the conclusion is stated as an *ordering* (`φ** < φ*`, `φ** <` the
  layer's own `φ`) rather than as a value.
- **The layer's `φ` is taken as the MEAN**, `N σ v₀/h`. `C-0011`'s solved profile is not uniform, so
  the local `φ` at the grafting surface exceeds the mean and every crossover here is crossed there
  **first**. This makes the derived ceilings **optimistic**, and by an unquantified amount.
- **The blob construction assumes free chains.** The layer's inheritance of it is argued from the
  absence of `N` in `φ_c(n)` and bounded by the 0.076–0.211 extension ratio, not demonstrated.
- **The equation-of-state support ceiling bounds the INTERACTION term only.** It says nothing about
  whether Gaussian chain elasticity or the Alexander/strong-stretching profile survive to `φ = 0.5`.
  A reader who adopts it inherits that gap.
- **The propagation inherits `C-0018`'s whole mean-field statement**: `C-0005` puts the one-loop
  electrostatic correction at 123–214 % of the leading term, larger than every margin quoted here. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **The equation of state was fitted in pure water at 20 °C**; the propagation is at 300 K in
  0.5–10 mM MgCl₂ (`C-0007`: ≤ 3.3 % of the modulus).

## Numbers that are CITED rather than DERIVED

| number | value | why it is cited, and what it moves |
|---|---|---|
| `A₂` | 1.9e−3 mol·cm³/g² | **CITED FROM `C-0003`/`C-0002`.** Sets `v` and hence `g_T`. `φ_c(1) = 0.141` does not depend on it at all |
| `b`, `M_K` | 1.1 nm, 137 g/mol | **CITED FROM `C-0002`.** The aspect ratio `b³/v_K = 7.09` that fixes the whole family comes from these two and `V̄` |
| `V̄` | 0.825 mL/g | **CITED FROM `C-0002`.** Fixes `v₀` and the weight→volume conversion |
| `χ(T) = 1.156 − 235.3/T` | 0.3717 at 300 K | **CITED FROM `C-0007`.** The second excluded-volume route, and the fourth corner |
| the fitted range of the equation of state | 1.5–67.5 wt % | **CITED**, from the recovered source data of the adopted fit — and it **corrects** `C-0002`'s "0–50 wt %" (`CH-0049`). It is the sole input to the support-ceiling rows, so this claim is more sensitive to it than to anything else |
| `C-0018`'s pull-in and correlation-band biases | per state | **CITED FROM `gpd/results/T-4-*.json`**, because neither can depend on the crossover. Graded by reproducing `C-0018`'s own `φ = 0.2` ceiling to 4.5e−9 |
| `C-0005`'s 1.46 nm band, `CH-0007`'s 1.0 V | — | **CITED**, carried unchanged so the re-ranking is like-for-like |

## Cross-checks passed

1. **Gate 1** — `ξ` in nm and `ξ·φ` a material constant on the ideal branch; `n(φ)` dimensionless
   and the exact inverse of `φ_c(n)`; the weight↔volume conversion inverts to 1e−12; the two
   constructors of the correlation agree to 1e−12.
2. **Gate 2** — the swollen and unswollen branches cross exactly where the blob holds `g_T`
   segments, and the crossing length **is** `ξ_T`; `φ_c(1) = 1/7.0918`, the reciprocal of
   `C-0002`'s aspect ratio, with `ξ(φ_c(1)) = b` exactly; **a space-filling athermal segment
   returns `φ** = 1` exactly**, which is Rubinstein & Colby's printed statement.
3. **Gate 3** — `φ**/φ* = √(N_K/g_T)` to **1.5e−16**, and `isEmpty ⟺ N_K < g_T` at every chain
   length tested; the thermal blob agrees with `CH-0020`'s independently written corrected count
   to 1e−12; `φ_c` is invariant under chain length by construction (`P3`).
4. **Gate 4** — the branch crossing located by bisection on a **logarithmic** bracket reproduces the
   closed form to 1e−9 (`P-15`'s discipline); both branches are exact power laws, so a central
   difference in `ln φ` returns −1 and −3/4 to 1e−12 at every step and Richardson cannot improve
   on it — which is the informative statement and is asserted as such.
5. **Gate 5 — five reproductions, four of them of things written by other people.**

| quantity | here | source | departure |
|---|---|---|---|
| `φ**` in the **reduced** convention | 0.0889807 | R&C eq (5.36) `φ** ≈ v/b³`, p. 180, **read directly** | **0.0** |
| the athermal limit of `φ**` | 1.000000 | R&C, same page, *"since `φ** ≈ 1`"*, **read directly** | **0.0** |
| `C-0018`'s `φ = 0.2` ceiling bias, worst of 162 | 0.101834 V | `C-0018` | **4.5e−9** |
| Kuhn aspect ratio `b³/v_K` | 7.09177 | `C-0002`'s parameter sheet | 2.5e−4 |
| thermal blob, scaling normalisation | 126.301 | `CH-0020` | 1.2e−5 |
| des Cloizeaux window width identity | 0.756858 | `√(N_K/g_T)` | 1.5e−16 |
| measured des Cloizeaux onset, PEG-5000 | 0.1126 physical | Hansen et al. (2003) p. 352, **read directly**, 0.07–0.09 reduced | the 1.408 conversion |

**Every literature number in this claim was read directly from the source**, per the flags in
`CH-0049`. Two negative results are recorded there with their query strings.

**Reproducibility.** The result file is **bit-identical on a re-run** — and the re-run read a
`gpd/results/T-4-*.json` that the coordinator had re-emitted in between, whose only changes were
tangency-residual diagnostics at the 1e−9 level. Nothing this claim consumes from `C-0018` moved.

## Still open — named, not answered

1. **The non-uniform profile is not used.** `C-0011` solves a layer whose local `φ` peaks at the
   grafting surface; the crossing volume fraction of a *solved* layer is a lower and different
   number, and it is not computed here. **This is the largest open item and it runs against the
   device.**
2. **No prefactor for the family is available from measurement.** A compression isotherm on a
   Gen-1-density PEG layer, or a SANS `R_g(φ)` on PEG in water of the kind Cheng et al. did for
   PMMA in chloroform, would collapse the 152× spread to a number. Neither exists for this system.
3. **The two excluded-volume routes still differ by 2.5×,** and that difference is what puts one
   corner of four on the other side of the emptiness verdict. Reconciling `A₂` against `χ` for
   PEG/water is a `C-0007` question, not a `T-21` one, and it is now load-bearing in a second place.
4. **Three of `C-0003`'s six layer models lose their warrant and none is withdrawn.** Whether the
   bracket should now be the virial half alone belongs to `C-0003`.
5. **`C-0002`'s own fit is "in good solvents"** — the adopted paper says so in its title — and
   `C-0003` establishes that PEG/water at 300 K is *marginal*. The fit is empirical and stands;
   the mismatch of premise is recorded, not resolved.
