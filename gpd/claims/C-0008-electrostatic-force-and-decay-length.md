# C-0008 — The electrostatic force on the Gen-1 tile, its stiffness, and the length it actually decays on

| | |
|---|---|
| **Task** | [`T-3a`](../tasks/T-3a-nonlinear-pb-profile.md) |
| **Leaf** | `A7.4` |
| **Verification type** | in-silico (closed-form 2:1 Gouy-Chapman + graded finite-volume Newton solve of the nonlinear mixed-boundary two-point problem) + logical |
| **Verdict** | **PASS** — against the `T-3a` predicate, all six clauses discharged |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** And *within* mean field: `C-0005` puts the one-loop correction at 123–214 % of the leading term across this entire gap range, so every force here is a **mean-field** number whose error is not bounded by its own expansion. |
| **Provenance** | `gpd/results/T-3a-nonlinear-pb-profile.json`, produced by `electrostatics.NonlinearPbProfileStudyKt`; 110 electrostatics tests green (73 inherited, 37 new) |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `k_BT/e = 25.852 mV`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at 2 / 5 / 10 mM; 40 × 40 × 10 nm honeycomb tile, Manning-renormalised |
| **Raises** | [`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md) |
| **Resolves** | [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) |
| **Challenged** | **[`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md) — OPEN**, against the *universal quantifier* in "`k_es < 0` everywhere", not against any number here. See the banner below. |

> ⚠️ **The scope of "`k_es < 0` everywhere" is challenged by [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md) (2026-08-13).**
> Every number in this claim is reproduced to the digit by `T-3` through the same solver and stands.
> What does not stand is the word *everywhere*: this claim's gap sweep starts at **3 nm**, and its validity
> range names no minimum gap. `|F_es(h)|` is **not monotone** below that — it rises to a maximum and then falls
> toward the sign change this claim already reports at zero bias between 4 and 5 nm. **Past that maximum
> `k_es > 0` and the electrostatics stiffens the layer instead of softening it**, at 0.55–1.58 nm depending on
> buffer and bias. `C-0012` finds **386 of 810** coupled free operating points on that reversed branch.
> The consequence is for the `T-4` hand-off below: the softening is **not** unbounded as the gap closes, so
> §1's open question — whether the osmotic divergence removes the instability or merely bounds it — has a
> second arresting mechanism in it that neither §1 nor this claim contains.
> Read `k_es < 0` as holding **above the force maximum**, which is the whole of the sampled range.

---

## The claim

**The electrostatic force is not the limiting factor in the Gen-1 actuator. The §3 target of 100 pN is reached at 0.067–0.15 V — a factor of 13 to 30 below the §3 bias ceiling — at every §3 layer height in 2 mM `MgCl₂`. What binds is not the force but its *gradient*: the same electrostatics that supplies 100 pN cheaply also supplies a negative stiffness that reaches the layer's own stiffness at 0.077–0.26 V in eight of the nine (gap, buffer) combinations tested.**

And three subsidiary statements, each of which replaces an inherited one:

1. `C-0005`'s `σ_eff` ceiling was **24 % high**, by exactly the factor `6 − 3√3`;
2. §1's `|k_es| ≈ F_es/λ_D` **understates** `|k_es|` by up to 2.6× (1.01–2.16× across the §3 working gaps), in the non-conservative direction;
3. the force's decay length is **neither** the bulk `λ_D` **nor** the counterion length `CH-0004` proposed.

### The numbers — `σ_eff` from the asymmetric solve

The 2:1 first integral is `(y'/κ)² = (e^{−2y} + 2e^{y} − 3)/3`, **derived**, not adapted. Its quadrature gives a saturated far-field amplitude of exactly `12 − 6√3` at a **negative** wall (divalent counterion) and exactly `6` at a **positive** one (monovalent counterion). Both are closed forms.

| | 2 mM | 5 mM | 10 mM |
|---|---|---|---|
| bulk `λ_D` (`I = 3c`) | 3.9269 nm | 2.4839 nm | 1.7565 nm |
| `C-0005`'s symmetric `z:z` ceiling `κ/(π l_B q)` | 0.05676 e/nm² | 0.08974 e/nm² | 0.12691 e/nm² |
| **`σ_eff` saturation, negative wall (the tile)** | **0.04562 e/nm²** | **0.07214 e/nm²** | **0.10202 e/nm²** |
| **`σ_eff` saturation, positive wall (the electrode)** | **0.17027 e/nm²** | **0.26922 e/nm²** | **0.38073 e/nm²** |
| asymmetric / symmetric, negative wall | `6 − 3√3` = **0.80385**, exactly, at every concentration | | |
| positive / negative | `2 + √3` = **3.73205**, exactly, at every concentration | | |
| **`σ_eff` of the tile at its Manning charge** | **0.04249 e/nm²** | 0.06449 e/nm² | 0.08711 e/nm² |
| … as a fraction of saturation | 93.1 % | 89.4 % | 85.4 % |

**`C-0005`'s ceiling is confirmed as a ceiling and quantified: it is 24 % high.** That is the "order of tens of per cent" the claim predicted of itself, so this is the expected outcome and **not** a contradiction.

**The tile is charge-saturated**, and that is the result that makes the whole force calculation robust. Across the three defensible readings of the tile's gap-facing charge — bottom helix row, half the tile, the whole tile, a factor of 2.96 — `σ_eff` moves from 0.04108 to 0.04403 e/nm², **7.2 %**. Even the *bare* charge, 25× larger, gives 0.04543 e/nm². `C-0005` could not choose between the readings; saturation means it did not have to.

### The numbers — `F_es(h, V)`

Computed from the osmotic + Maxwell first integral of the converged profile. Nominal tile charge (half the tile, Manning-renormalised, `σ_t = −0.3987 e/nm²`), free bulk buffer, mixed boundary conditions with a 20 µF/cm² compact layer in series at the electrode. **Negative means toward the electrode**, per the sign convention.

`F_es` [pN] over the 40 × 40 nm footprint:

| buffer | gap | 0.10 V | 0.25 V | 1.0 V | 2.0 V |
|---|---|---|---|---|---|
| **2 mM** | 5 nm | −167 | **−490** | −878 | **−938** |
| | 7 nm | −87 | −215 | −335 | −353 |
| | 10 nm | −34 | −74 | −105 | −109 |
| | 15 nm | −7 | −16 | −22 | −22 |
| **5 mM** | 5 nm | | −294 | | −548 |
| | 7 nm | | −105 | | −176 |
| | 10 nm | | −27 | | −41 |
| **10 mM** | 5 nm | | −163 | | −312 |
| | 7 nm | | −46 | | −81 |
| | 10 nm | | −8 | | −13 |

**The bias needed for 100 pN**, and where it sits relative to the point-ion boundary:

| buffer | 5 nm | 7 nm | 10 nm | 15 nm |
|---|---|---|---|---|
| 2 mM | **0.067 V** | **0.113 V** | **0.679 V** | not reached ≤ 2 V (−22 pN) |
| 5 mM | 0.091 V | 0.232 V | not reached (−41 pN) | not reached |
| 10 mM | 0.146 V | not reached (−81 pN) | not reached | not reached |

**Every one of these is inside the point-ion validity range** (`ψ_d ≤ 0.018–0.171 V` against the 0.197 V boundary). The 100 pN answer does **not** depend on the untrustworthy end of the bias range.

**The force saturates in bias.** The compact layer takes 66 % of 0.1 V and **88 % of 2 V**, so the diffuse layer never sees more than 0.235 V; and the diffuse layer's far field saturates. Between 0.25 V and 2 V — a factor of 8 in bias — `|F_es|` grows only **1.91×** at 5 nm and **1.48×** at 10 nm. Above ~0.5 V the actuator is essentially voltage-saturated.

**At `V = 0` the force is a near-cancellation, not a baseline attraction.** The grounded electrode acquires 0.0145 e/nm² of induced countercharge, but that charge also has to charge the compact layer, which pulls `ψ_d` to −0.0116 V — repulsive. The net is under 4 pN at every gap and **changes sign between 4 and 5 nm** (+3.94 pN at 3 nm, −0.41 pN at 5 nm). An ideal constant-potential electrode with no compact layer would give −34.9 pN at 5 nm. The two readings **bracket** it; no single number is defensible. A constant-charge electrode model gives exactly zero and misses the effect entirely.

### The numbers — `k_es` from the solve, and the error in §1's estimate

`k_es = −∂F_es,z/∂z`, central-differenced through the full re-solve at fixed applied bias, step-independent to 1e−3.

| buffer | gap | `k_es` at 0.25 V | `k_es` at 2 V | §1 error at 2 V |
|---|---|---|---|---|
| 2 mM | 5 nm | −215 pN/nm | **−516 pN/nm** | §1 low by **2.16×** |
| | 7 nm | −83 pN/nm | −155 pN/nm | low by 1.72× |
| | 10 nm | −24 pN/nm | −38.6 pN/nm | low by 1.39× |
| 5 mM | 5 nm | −160 pN/nm | −344 pN/nm | low by 1.56× |
| 10 mM | 5 nm | −108 pN/nm | −226 pN/nm | low by 1.27× |

**`k_es < 0` everywhere in this claim's 3–30 nm sweep**, as §1 requires — but *not* below the force maximum
at 0.55–1.58 nm, where it reverses ([`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md)). The §1 form `|k_es| ≈ F_es/λ_D` is **wrong in the direction that matters** — it *understates* the softening — over the whole §3 box, by a factor between **1.00 and 2.64** (1.01–2.16 at the 5 / 7 / 10 nm working gaps). The error is largest exactly where it is most dangerous: the smallest gap, the lowest ionic strength, the highest bias.

### The numbers — the decay length, and the settlement of `CH-0004`

`ℓ = −1/(d ln|F_es|/dh) = F_es/k_es`, at 2 mM and 2 V:

| gap | 3 | 4 | 5 | 6 | 7 | 8 | 10 | 12 | 15 | 20 | 25 | 30 nm |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`ℓ` [nm]** | 1.51 | 1.58 | **1.82** | 2.06 | **2.28** | 2.48 | **2.83** | 3.11 | 3.42 | 3.72 | 3.85 | 3.90 |
| `ℓ/λ_D` | 0.38 | 0.40 | 0.46 | 0.52 | 0.58 | 0.63 | 0.72 | 0.79 | 0.87 | 0.95 | 0.98 | **0.99** |

Against the three lengths `CH-0004` distinguishes, at the 5–10 nm working gap:

| candidate | value | verdict |
|---|---|---|
| bulk `λ_D` (§1/§3, and `T-6`) | 3.93 nm | **1.4× to 2.2× too long** at the working gap — but **exactly right** in the far field, which `ℓ → 3.90 nm` at 30 nm confirms |
| gap counterion length (`CH-0004`'s proposal) | 0.84–1.18 nm | **2.4× to 3.4× too short.** Never approached at any gap or bias. |
| in-layer `λ` (`C-0005` §4(c)) | 4.5–5.5 nm | not the force's decay length either; it *lengthens* `ℓ` when the layer is present (2.96–3.18 nm at 10 nm) but does not set it |

**`ℓ` is bias-dependent as well as gap-dependent**, which none of the three candidates is. At zero bias the interaction is the tile against its own image in the conductor, which carries `e^{−2κh}` and hence `ℓ → λ_D/2 = 1.96 nm`; under bias it is the overlap of two far fields, `e^{−κh}`, hence `ℓ → λ_D`. Both limits are reproduced by the nonlinear solve and asserted as tests. In between, nonlinearity shortens `ℓ` further.

**So `CH-0004` is resolved: upheld in its consequence, refuted in its magnitude, and its own escape clause is what fires.** `CH-0004` wrote that it would fail if "the counterions are in fact sequestered at the tile's underside rather than distributed through the gap, [so that] the mid-gap region could be closer to bulk composition than the count suggests". That is what the profile shows.

### The hand-off to `T-4` — offered, not concluded

`T-4` asks whether `k_eff = k_brush + k_es` reaches zero. This claim supplies the `k_es` half and **does not answer the question**, because the `k_brush` half is `C-0001`'s and `C-0001` flags itself as a lower bound pending `T-1c`. The numbers are set down so `T-4` can consume them, and the caveat travels with them.

Applied bias at which `|k_es|` equals `C-0001`'s `k(L₀)`:

| gap | `k_brush` (`C-0001`) | 2 mM | 5 mM | 10 mM | `\|k_es\|/k_brush` at 2 V, 2 mM |
|---|---|---|---|---|---|
| 5 nm | 111.6 pN/nm | **0.158 V** | 0.181 V | 0.259 V | 4.62× |
| 7 nm | 26.9 pN/nm | **0.098 V** | 0.128 V | 0.236 V | 5.76× |
| 10 nm | 7.4 pN/nm | **0.077 V** | 0.139 V | 1.280 V | 5.21× |

Set against the bias needed for 100 pN in the same buffer, the margin **inverts across the layer-height range**:

| gap (2 mM) | bias for 100 pN | bias for `k_eff = 0` | margin |
|---|---|---|---|
| 5 nm | 0.067 V | 0.158 V | **2.4× — force target reached before pull-in** |
| 7 nm | 0.113 V | 0.098 V | 0.87× — pull-in first, marginally |
| 10 nm | 0.679 V | 0.077 V | **0.11× — pull-in an order of magnitude first** |

**`C-0003` landed while this task was running and supersedes `C-0001`'s stiffness**, replacing `k(L₀) = 7.4 pN/nm` at the 10 nm point with a bracket of **0 – 13.8 pN/nm** over six models. The table above is computed against `C-0001`'s number because that is what was standing when the sweep ran; the substitution is easy and it does not change the conclusion. Reading `|k_es|` off the same sweep, the 10 nm cancellation bias moves from 0.077 V to ≈ 0.13 V at the top of `C-0003`'s bracket — still five times below the 0.679 V that 10 nm needs for 100 pN. `T-4` should redo the table against `C-0003`'s bracket rather than quote this one.

**A larger `k_brush` moves every cancellation bias up**, and `C-0003` does move it. So this table is a *warning*, not a verdict, and the direction of the warning is that the thin end of the §3 layer-height range is where the actuator is stable and the thick end is where it is not — the opposite of the direction stroke considerations push.

### The PEG layer in the gap — the sign of §4(c), now with a force attached

The layer is treated as a medium with `C-0005`'s salt partition coefficients and Maxwell-Garnett permittivity, and the disjoining pressure is referenced to the *local* medium so the pure salt-depletion term — which belongs to the layer's own free energy, hence to `T-1`/`T-1c` — is excluded rather than double-counted.

**The layer amplifies the force by 1.15× to 1.60×**, monotonically increasing with ionic strength and with compression:

| `L₀` | gap | `φ` | `K_salt` | `λ_in` (2 mM) | `F` with layer / without |
|---|---|---|---|---|---|
| 10 nm | 10 nm | 0.0289 | 0.768 | 4.48 nm | 1.16× (2 mM), 1.29× (5 mM), **1.52× (10 mM)** |
| 7 nm | 7 nm | 0.0439 | 0.668 | 4.81 nm | 1.17× (2 mM) |
| 5 nm | 5 nm | 0.0708 | 0.519 | 5.45 nm | 1.20× (2 mM) |
| 5 nm | 3 nm | 0.1180 | 0.330 | 6.83 nm | 1.25× (2 mM) |

(2 mM values quoted at 1 V; the spread across 0.25–2 V is under 3 % at every point.)

**This confirms `C-0005`'s sign reversal of §4(c) and converts it from a screening length into a force.** §4(c) worries that ions inside the layer screen the field where it is needed; on this model the layer *protects* the field and the actuator gets **more** force, not less — and increasingly so as it squeezes. The effect is largest at 10 mM, where the bulk screening is strongest and the layer's exclusion therefore buys the most.

### The size-modified (Bikerman) bracket — `T-6b`, folded in

Same solver, one extra term; the point-ion model is exactly its `n_max → ∞` limit and that is asserted as a test.

| | 0.25 V | 1.0 V | 2.0 V |
|---|---|---|---|
| `Mg²⁺` hydrated radius (4.28 Å) | +2.0 % | +10.9 % | **+25.9 %** |
| `Cl⁻` hydrated radius (3.32 Å) | +0.9 % | +4.8 % | +12.4 % |

(2 mM, 7 nm; the full range over all 54 brackets is **+0.8 % to +56 %**.)

**Finite ion size *increases* `|F_es|`, it does not decrease it** — the counterions cannot pack into the contact layer, so the diffuse layer is thicker and the interaction stronger. That is the opposite of what "steric exclusion" suggests, and it means **the point-ion numbers above are a lower bound on `|F_es|` within mean field.** The correction is negligible below 0.25 V, which is where the 100 pN answer lives, and it is largest exactly where the point-ion model is least trustworthy anyway.

---

## Validity range

Respected downstream, and enforced in code where enforceable.

- **Mean field.** `C-0005`: the one-loop correction is 123–214 % of the leading term at 5–10 nm for `Mg²⁺`. PB here is not merely inaccurate but *uncontrolled*. The correction is **attractive between like charges**; for the oppositely charged tile-electrode pair **no published result gives even the direction**, and none is claimed. This is the largest single uncertainty on every force in this claim and it is not reducible by a better mean-field solve.
- **Point ions**, except in the Bikerman bracket. The bracket is one-sided and upward.
- **The 20 µF/cm² compact-layer capacitance is cited and now load-bearing.** It sets how much of an applied bias reaches the diffuse layer. The *force* is insensitive to it above ~0.5 V because of saturation; the **point-ion boundary in applied bias moves with it** (`CH-0007`).
- **2 V is outside the aqueous electrochemical window** (1.23 V thermodynamic). Faradaic current, gas evolution and electrode corrosion are outside every model here. The 2 V column is reported because §3 asks for it, not because it is operable. **Fortunately the 100 pN answer does not need it.**
- **The tile is a uniformly charged plane.** It is a 10 nm slab of duplexes with electrolyte in its interstices. Three readings of its gap-facing charge span a factor of three and all are reported; saturation makes `σ_eff` move by 7 %, which is the only reason a single force can be quoted.
- **1-D.** No edge, no fringing, no lateral structure. The tile is 4–13 gap heights across.
- **No charge regulation** at the tile (phosphate `pKa ≈ 1`, so safe) and none at the electrode beyond the Stern series. No dielectric layer of §1. No specific `Mg²⁺`-phosphate or `Mg²⁺`-PEG chemistry.
- **The layer's partition coefficients are `C-0005`'s one-sided exclusion bound.** If PEG-cation coordination raises `K` above 1 (`P-8`), the layer would screen *more* and the amplification column would invert.
- **10 of 216 force state points are flagged `numericallyResolved = false`** — all in the `V = 0` column at gaps ≥ 12 nm, where `|F_es| < 4e−4 pN`. They are emitted with the flag rather than suppressed.
- **Nothing here is measured.**

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

| number | value | why it is cited, and what it moves |
|---|---|---|
| `ε_r` of water at 300 K | 78 | Literature spans 77.7–78.3. `l_B ∝ 1/ε` and `F_es` goes roughly as `l_B`, so ~3 % on the force. **Moves no verdict.** |
| Manning surviving fraction | 11.90 %, 1276 e | **CITED FROM `C-0005`**, which derived it. Because the tile is saturated, a factor of three here is 7 % in `σ_eff` and under 2× in `F_es`. |
| Stern capacitance | ~20 µF/cm² | Order-of-magnitude for aqueous electrodes. **Load-bearing** for the bias mapping, not for the force above 0.5 V. `T-6b`'s to sharpen. |
| hydrated radii (Nightingale 1959) | `Mg²⁺` 4.28 Å, `Cl⁻` 3.32 Å | Set the Bikerman lattice density and the point-ion boundary. Reported as a two-radius bracket rather than resolved. |
| layer `K₊`, `K₋`, `ε_eff` | `C-0005` §4(c) table | **CITED FROM `C-0005`**, and one-sided (exclusion only). |
| `C-0002` layer volume fractions | 0.0708 / 0.0439 / 0.0289 | **CITED FROM `C-0002`**. |
| `C-0001` brush stiffness | 111.6 / 26.9 / 7.4 pN/nm | **CITED FROM `C-0001`**, and **superseded by `C-0003`** (0 – 13.8 pN/nm at the 10 nm point) while this task was running. Used only for the `T-4` hand-off ratio, never for a force; the substitution is worked through in that section and changes no conclusion. |
| B-DNA geometry, tile packing | via `DnaOrigamiTile` | **CITED FROM `C-0005`**, unchanged. |

Everything else — the 2:1 first integral, the saturation constants `12 − 6√3` and `6`, `σ_eff`, `F_es`, `k_es`, `ℓ`, the Stern series mapping, the Bikerman bracket, the layer amplification — is derived here from the §3 parameters and SI constants.

## Cross-checks passed

1. **Gate 1** — the 2:1 first integral vanishes at `y = 0` and is exactly `y²` there (which pins `κ`), and is asserted **not** to be even in `y`; the Grahame relation round-trips to 1e−9; `σ_eff` asserted through its identity, not its formula; the **contact-value theorem** reproduced from the two-plate solve to 1e−5; unit algebra `k_BT/nm³ → pN/nm² → pN`.
2. **Gate 2** — `σ_eff → σ` in the Debye-Hückel limit, **first order** in the charge; the nonlinear solve reproduces the closed-form linear mixed-BC pressure, **first order** in the amplitude; Bikerman → point ion as `2e4/n_max`; saturation at `12 − 6√3` and `6`, monotone from below, differing by exactly `2 + √3`; the symmetric `z:z` form recovered; zero bias attractive on a grounded conductor; **both** decay limits (`λ_D/2` and `λ_D`) reproduced from the nonlinear solve.
3. **Gate 3** — the first integral constant across the gap, from **three independent evaluations**, converging at exactly 4× per mesh doubling; plate electroneutrality to 1e−6 and gap electroneutrality to 1e−8 by two independent routes; `k_es < 0` asserted at every working gap; no charge inversion in mean field; the **Donnan potential** `(1/3)ln(K₊/K₋)` reproduced to 1e−5, independently re-deriving `C-0005`'s geometric-mean rule.
4. **Gate 4** — second order in the mesh, checked as an **order**; the closed-form profile verified **by substitution**, residual checked as an **order**; the far-field amplitude approached at the **rate** `e^{−κΔz}`; `k_es` step-independent with the residual falling as `O(δ²)`; a 6-mesh × 2-gap convergence table emitted; a per-point resolution flag.
5. **Gate 5** — **`C-0005`'s symmetric ceiling reproduced exactly** (0.0567557 e/nm²), so the 24 % gap is a finding and not a code disagreement; **solver and analytics agree node by node** to 1e−3 and to 1e−6 on the tile surface potential, being genuinely independent implementations; **`T-6`'s `λ_D = 3.92688 nm` recovered**; **`C-0005`'s 0.19657 V point-ion boundary reproduced** and then read correctly (`CH-0007`); and the premise `C-0005` could not check is checked — **the `Mg²⁺` contact density at the Manning-renormalised tile charge is below close packing**, where at the bare duplex charge `C-0005` found it 1.75× past.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **The direction of the correlation correction for oppositely charged walls is unknown.** Every published `Ξ` criterion is a like-charge result. This is the single largest uncertainty on `F_es`, no closed form repairs it, and explicit-ion simulation is the only route — at `C-0005`'s 1–3 week cost, which was not spent.
2. **The lateral load profile is not computed and a 1-D treatment cannot compute it.** `T-5b` needs it, and `C-0006` shows dishing is *exactly linear* in it, so a 2-D solve of the tile edge would convert directly into a dishing amplitude. What this task can hand over is the pressure sensitivity `d ln|P|/dh = −1/ℓ` = **0.550 nm⁻¹ at 5 nm and 0.353 nm⁻¹ at 10 nm** (2 mM, 2 V), which converts any *geometric* edge perturbation into a load non-uniformity.
3. **Whether the electrode can be biased to 1–2 V in aqueous `MgCl₂` at all** is an electrochemistry question this task does not touch. Because the force saturates, the answer barely moves the force numbers — but that is a happy accident, not an argument.
4. **The Stern capacitance is cited and load-bearing** for the bias mapping. `T-6b`'s remaining content.
5. **Whether `P-8` inverts the layer amplification.** The layer's partition coefficients are one-sided.

## Challenges

[`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md) is raised **by** this claim, against `C-0005`'s comparison of a diffuse-layer potential with an applied bias.
[`CH-0004`](../challenges/CH-0004-screening-decay-length.md) is **resolved** by this claim — upheld in consequence, refuted in magnitude.

**Standing against this claim:** [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md), raised by
[`C-0012`](C-0012-coupled-stroke-and-blocking-force.md), against the scope of "`k_es < 0` everywhere". No number here is disputed.
