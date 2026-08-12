# C-0005 — Where Poisson-Boltzmann may be used in the Gen-1 stack, and how wrong it is inside that range

| | |
|---|---|
| **Task** | [`T-6`](../tasks/T-6-mean-field-screening-validity.md) |
| **Leaf** | `A7.4` |
| **Verification type** | in-silico (closed-form evaluation of published MC-calibrated criteria) + logical |
| **Verdict** | **PASS** — against the predicate *"Quantified deviation from mean-field, with the boundary stated"* |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The criteria are published and Monte-Carlo-calibrated for a *model* system; nothing about this device is measured. |
| **Provenance** | `gpd/results/T-6-mean-field-screening-validity.json`, produced by `electrostatics.MeanFieldValidityStudyKt`, 73 electrostatics tests green |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `k_BT/e = 25.85 mV`; aqueous MgCl₂ at 2 / 5 / 10 mM; `ε_r = 78`; 40 × 40 × 10 nm honeycomb tile |
| **Raises** | [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) |

---

## The claim

**Mean-field Poisson-Boltzmann is not quantitatively controlled anywhere in the Gen-1 working range,
and it is not qualitatively wrong anywhere in it either.**
The boundary between those two statements is geometric, and both edges of it are far from the working gap —
one far below, one just above.

### The numbers

| quantity | 2 mM | 5 mM | 10 mM |
|---|---|---|---|
| ionic strength `I = 3c` | 6 mM | 15 mM | 30 mM |
| **Debye length `λ_D`** (bulk) | **3.927 nm** | **2.484 nm** | **1.756 nm** |
| `κh` at `h = 5 nm` | 1.273 | 2.013 | 2.847 |
| saturated far-field `σ_eff` (`q = 2`) | 0.0568 e/nm² | 0.0897 e/nm² | 0.1269 e/nm² |
| PB breakdown bias at the electrode (`Cl⁻`) | 0.197 V | 0.173 V | 0.155 V |
| counterion : bulk ions in a 5 nm gap | 33 : 1 | 13 : 1 | 6.6 : 1 |
| **local screening length in the gap, `h = 5 nm`** | **0.836 nm** | 0.836 nm | 0.836 nm |

§3's "~4 nm at 2 mM Mg²⁺" is **confirmed and now derived**: 3.927 nm, 1.8% below the quoted value.
`l_B = 0.7141 nm` at 300 K, `ε_r = 78`, derived from SI constants.

### The tile's charge, derived

Honeycomb packing at 2.6 nm interhelical distance gives 8.78 nm² of cross-section per duplex,
hence 45.6 duplexes in the 40 × 10 nm cross-section, 5359 bp, **10 718 nucleotides** — which fits inside a single
M13 scaffold origami plus staples, the independent bound.

| quantity | value | provenance |
|---|---|---|
| axial charge spacing `b` | 0.170 nm | **DERIVED** — B-form rise / 2 |
| linear charge density `τ` | 5.882 e/nm | **DERIVED** |
| **duplex surface charge density** | **0.9362 e/nm² = 0.1500 C/m²** | **DERIVED** — lands on the textbook B-DNA value |
| single-row projected | 2.264 e/nm² | **DERIVED** |
| whole-tile projected | 6.699 e/nm² | **DERIVED** |
| bare tile charge | 10 718 e | **DERIVED** |

### The control parameters

At the duplex surface, which is the surface that governs the *local* coupling:

| quantity | `Na⁺` (`q = 1`) | **`Mg²⁺` (`q = 2`)** | `Mg²⁺`, hydrated hard core |
|---|---|---|---|
| Gouy-Chapman length `μ_GC` | 0.238 nm | **0.119 nm** | 0.170 nm |
| **coupling parameter `Ξ`** | 3.00 | **24.00** | **16.81** |
| lateral spacing `a_⊥` | 1.034 nm | **1.462 nm** | 1.747 nm |
| plasma parameter `Γ` | 1.22 | 3.46 | 2.90 |
| PB contact density / steric limit | — | **1.75×** | 0.86× |

`μ_GC = 0.119 nm` is **smaller than the radius of a hydrated `Mg²⁺` ion (0.428 nm)**.
The continuum picture has no room to be right inside the layer that carries the physics.

### Manning renormalisation — the charge `T-3` must use

`ξ_M = l_B/b = 4.2006` (valency-free convention; Naji et al.'s valency-inclusive `ξ` is 4.20 and 8.40).

| counterion | condensed | **surviving** | effective tile charge |
|---|---|---|---|
| `Na⁺` (`q = 1`) | 76.2% | 23.81% | 2552 e |
| **`Mg²⁺` (`q = 2`)** | **88.1%** | **11.90%** | **1276 e** |
| `q = 3` | 92.1% | 7.94% | 851 e |

**Only 11.9% of the bare charge survives.** Using the bare charge would overstate the electrostatic force by **8.4×**.
And beyond Manning there is a second, independent reduction: Gouy-Chapman **charge saturation** caps the
*far-field* effective charge at `σ_eff = κ/(π l_B q) = 0.0568 e/nm²` at 2 mM — 118× below the bare projected
density and 14× below the Manning-renormalised one. The two are different mechanisms and must not be multiplied blindly;
saturation is the binding one in the far field.

**Premise on the saturation number, stated rather than inherited:** `σ_eff = κ/(π l_B q)` is read from the
**symmetric `z:z`** Gouy-Chapman closed form. `MgCl₂` is 2:1 and its exact planar solution is not that
expression. The *saturation* is generic — it follows from `tanh` bounding the effective surface potential in
any electrolyte — but the prefactor carries a 2:1-versus-2:2 error of order tens of per cent. It is therefore
quoted as an **order-of-magnitude ceiling that rules out using the bare charge**, not as a number to compute a
force from. `T-3` should obtain its own effective charge from a 1-D nonlinear PB solve in the actual 2:1 buffer.

### The quantified deviation from mean-field

Defined as the ratio of the one-loop correction (Naji Eq. 19) to the leading PB term (Naji Eq. 13),
`Ξ|P⁽¹⁾|/P_PB` — the loop parameter of the expansion whose saddle point *is* Poisson-Boltzmann:

| gap | `Mg²⁺`, bare `σ` | `Mg²⁺`, hard-core | `Na⁺`, for contrast |
|---|---|---|---|
| 5 nm | **214%** | 199% | 47% |
| 7 nm | **163%** | 152% | 36% |
| 10 nm | **123%** | 114% | 27% |
| 15 nm | 89% | 83% | 19% |
| 20 nm | 70% | 66% | 15% |

**Above 100% the expansion has broken down.** PB is then not merely inaccurate — it is *uncontrolled*:
one cannot say from within the theory in which direction to correct it, or by how much.
The correction term is attractive, so PB **overstates repulsion between like charges**, but the size of that
overstatement is not bounded by this expansion inside the working range.

**It is the divalence, not the surface charge, that does this.** The same surface with a monovalent counterion
sits at 27–47% deviation and inside its own validity range. `Ξ ∝ q³` and `μ ∝ 1/q`.

### THE BOUNDARY

Three bands in the tile-electrode gap, at 2 mM Mg²⁺:

| band | range | status |
|---|---|---|
| **A — qualitative failure** | `gap < a_⊥ = 1.46 nm` (1.75 nm hard-core corrected) | Rouzina-Bloomfield: correlation attraction is possible and **PB cannot produce it at all**. **Not reached** — the polymer layer holds the tile 3.4× to 6.8× outside this band. |
| **B — uncontrolled** | `1.46 nm < gap < 12.9 nm` | The one-loop correction exceeds the leading term. PB is qualitatively right (monotone, no attraction) but quantitatively uncontrolled. **The entire 5–10 nm Gen-1 working range is here.** |
| **C — controlled** | `gap > 12.9 nm` | The loop expansion converges; PB is quantitatively usable with the residual error as tabulated. |

Band C's edge is confirmed twice: **12.91 nm** from the full Eq. (19)/(13) ratio, **13.52 nm** from Naji's own
closed-form criterion Eq. (20) `(Δ/μ)/ln(Δ/μ) > Ξ`. 4.7% apart.

And in bias, at the electrode:

> **Point-ion PB fails above ≈ 0.197 V of diffuse-layer drop** at 2 mM (`Cl⁻` counterion at a positive electrode;
> 0.097 V for `Mg²⁺` at a negative one). The §3 target is **≤ 2 V — a factor of 10 above**.
> The dependence on concentration is logarithmic, so no buffer in the §3 range rescues it.
> Above the threshold the compact layer carries the potential and the electrode charge is
> **Stern-limited at ≈ 1.25 e/nm² per volt** (20 µF/cm²), not Gouy-Chapman.

### Where explicit ions become necessary

**Necessary:**

1. Any quantity depending on the ion distribution **within `a_⊥ ≈ 1.5 nm` of a phosphate** — effective charge
   beyond the Manning estimate, helix-helix forces inside the origami, the local dielectric response.
2. Any bias above ~0.2 V where the compact layer dominates — unless a Stern + size-modified (Bikerman) PB
   treatment is used instead, which is far cheaper and is the recommended next step.
3. Any question about **charge inversion or like-charge attraction**, which PB cannot answer even in principle.

**Not necessary** for the tile-electrode force at 5–10 nm. There the cheap route is PB with a
Manning-renormalised, saturation-capped effective charge and a **stated factor-of-two uncertainty**,
and an explicit-ion simulation would buy a second-digit correction to a number whose first digit is
already uncertain for other reasons (`T-1c`, the layer stiffness).

**Cost, if it is ever wanted:** a primitive-model Monte Carlo of the 40 × 40 nm gap with explicit hydrated
`Mg²⁺` needs ~10³–10⁴ ions with Ewald summation, and to resolve a *pressure* to 10% it needs ~10⁸–10⁹ MC moves
with block averaging. On 8 cores that is **days per state point**, and the boundary would need a sweep of at
least 3 gaps × 3 buffers = 9 state points, i.e. **1–3 weeks of wall-clock**. It is not warranted by anything
in the current queue and **it was not run**. Should it become warranted, the cheaper intermediate step
(size-modified PB, minutes) comes first.

### §4(c) — ion partitioning into the PEG layer

At `C-0002`'s volume fractions, with the PEG Kuhn segment radius (0.233 nm) from `C-0002`,
hydrated ionic radii from Nightingale, Ogston hard-sphere exclusion × Born dielectric penalty,
combined by Donnan for a neutral layer:

| layer (`C-0002`) | `φ` | `ε_eff` | `K(Mg²⁺)` | `K(Cl⁻)` | **`K_salt`** | `λ_in/λ_bulk` | `λ_in` at 2 mM |
|---|---|---|---|---|---|---|---|
| `L₀` = 10 nm, window lower edge | 0.0289 | 74.97 | 0.693 | 0.808 | **0.768** | 1.141 | 4.48 nm |
| `L₀` = 10 nm, window upper edge | 0.0335 | 74.50 | 0.653 | 0.781 | 0.736 | 1.166 | 4.58 nm |
| `L₀` = 7 nm, brush onset | 0.0439 | 73.43 | 0.571 | 0.723 | 0.668 | 1.224 | 4.81 nm |
| `L₀` = 5 nm, brush onset | 0.0708 | 70.72 | 0.401 | 0.590 | **0.519** | 1.388 | 5.45 nm |

**The sign is the finding, and it is opposite to the way §4(c) is phrased.** §4(c) worries that
"mobile ions inside the polymer layer screen the field exactly where we need it". On this bound the layer
**partially protects** the field: it admits only 52–77% of the bulk salt, so the local Debye length is
**1.14× to 1.39× longer** inside the layer than outside, and it lengthens further as the layer is compressed
— i.e. the harder the actuator squeezes, the less the layer screens.

Steric exclusion dominates: at `φ = 0.029` it contributes `K = 0.793` against the Born term's `K = 0.874`.
The dielectric decrement is only 78 → 75.0 (3.9%), because the layer is **97% water** — §4(c)'s premise that the
layer "lowers the local dielectric constant" is true but is a correction, not a mechanism.

### Hand-off to `T-7b` — a cheap bound on the streaming potential, as an *argument*

`T-7` (`C-0004`) could not bound the electro-osmotic coupling that opposes drainage without a screening result.
The gap inventory above supplies one, cheaply. **This is an argument from computed quantities, not a computed
electroviscous coefficient** — it is flagged as such and it does not carry a verification gate.

Three suppressions, all in the same direction:

1. **The PEG layer is electrically neutral.** It has no fixed charge and therefore **no zeta potential of its
   own**. Whatever electrokinetic coupling exists is *borrowed* from the tile and electrode surfaces; it is not
   a property of the porous medium, which is what a standard charged-nanochannel treatment would assume.
2. **Only a thin skin of the layer carries net space charge.** The local screening length in the gap is
   0.836 / 0.989 / 1.182 nm at 5 / 7 / 10 nm, so the charged fraction of the layer thickness is
   **12% to 17%**. The remaining 83–88% is electroneutral: it contributes viscous drag to drainage but no
   streaming current. Electroviscous corrections in a slit scale as `(λ/h)²`, i.e. **0.014 to 0.028** here.
3. **Counterion domination raises the local conductivity by an order of magnitude, which shorts out the
   streaming potential.** Weighting each species by `c z²`: the bulk 2 mM buffer gives `2×4 + 4×1 = 12`
   mM-equivalents; the gap gives the counterion population `66.2×4 = 265` (at 5 nm) or `33.1×4 = 132`
   (at 10 nm) plus `K_salt ≈ 0.77 × 12 ≈ 9` of partitioned bulk salt. That is a conductivity
   **12× to 23× the bulk value at 2 mM** (and ~3–6× at 10 mM, where bulk salt contributes more).
   Since `E_streaming ∝ 1/K`, the streaming potential is suppressed by that factor relative to any estimate
   made at bulk conductivity.

**Conclusion offered to `T-7b`, not established here:** the electroviscous retardation of drainage is
suppressed by a geometric factor of order `10⁻²` **and** a conductivity factor of order `10¹` relative to a
naive charged-nanochannel estimate at bulk ionic strength — jointly ~`10⁻³`. It is therefore very unlikely to
be the binding constraint on the ≥ 1 kHz bandwidth. What `T-7b` still needs, and what `T-6` does **not**
supply: the effective zeta potential of the *tile underside* as seen by the flow (not the same as its
Manning-renormalised charge), and the Brinkman screening length of the layer, which is `T-7`'s.

---

## Validity range

Respected downstream, and enforced in code where enforceable:

- **The criteria are for two uniformly charged planar walls, point counterions, no salt.** Our geometry is a
  charge-patterned slab over a metal electrode, in salt, with a polymer layer between. The transfer is
  **licensed by counterion dominance** (3.3:1 to 33:1, checked as a test) but it is a transfer, and every
  number here is a bound rather than a prediction.
- **The two walls in the model are like charged; under bias ours are oppositely charged.** `Ξ` and `μ_GC` are
  properties of each surface separately and transfer. The **attraction thresholds** (`Ξ > 12` onset,
  `Ξ ≈ 17` first-order unbinding, `Ξ ≈ 30` with image charges) are two-like-walls results and are
  **not transferred** to the tile-electrode pair. They *are* the right frame for tile-tile and helix-helix.
- **`Ξ = 17–24` sits in the intermediate-coupling gap `1 < Ξ < 100`, where — in Naji et al.'s own words —
  neither the loop expansion about mean field nor the virial expansion about strong coupling converges.**
  No systematic theory exists there. This is a statement about the literature, not about this implementation,
  and no closed-form calculation can repair it.
- **Point ions.** The contact-value theorem is exact, and it puts `Mg²⁺` at 6.53 M at the duplex surface against
  a close-packed hydrated limit of 3.74 M — **1.75× past physical possibility, at zero applied bias**.
  Correcting for the hard core (Naji Eq. 30) brings it to 0.86× and drops `Ξ` to 16.8.
- **The hydrated-radius choice straddles a threshold.** Nightingale's 4.28 Å gives `Ξ = 16.8`, just **below**
  the `Ξ = 17` first-order unbinding threshold; the first-shell geometric radius of `Mg(H₂O)₆²⁺` (3.47 Å) gives
  `Ξ = 17.8`, just **above** it. This is reported as a straddle and not resolved.
- **No charge regulation, no dielectric layer, no specific Mg²⁺-phosphate chemistry.** Manning condensation is
  territorial, not chemical; inner-sphere coordination is outside every model used here.
- **The partitioning bound is one-sided** — see below.

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

| number | value | why it is cited, and what it moves |
|---|---|---|
| `ε_r` of water at 300 K | 78 | Literature spans 77.7–78.3. `l_B ∝ 1/ε`, `Ξ ∝ l_B²`, so a 3% spread is 6% in `Ξ`. **Moves no verdict.** |
| `ε_r` of bulk PEO | 5 | Only used inside a mixing rule at `φ ≈ 0.03`, where the whole polymer contribution is 4%. Negligible. |
| B-DNA rise per base pair | 0.34 nm | Everything about the tile's charge is derived from it. A 5% error is 5% in `σ_s` and `Ξ`. |
| B-DNA duplex radius | 1.0 nm | Enters `σ_s` linearly. |
| honeycomb interhelical distance | 2.6 nm | **The largest single uncertainty.** Projected charge goes as its inverse square. It does **not** enter the duplex surface charge density, which is what `Ξ` uses — so the verdict is insensitive to it. |
| hydrated radii (Nightingale 1959) | Mg²⁺ 4.28 Å, Cl⁻ 3.32 Å | Straddles the `Ξ = 17` threshold, as above. Also sets the steric ceiling. |
| Stern capacitance | ~20 µF/cm² | Order-of-magnitude for aqueous electrodes. Enters the electrode charge linearly and is `T-3`'s to sharpen. |
| `Ξ` thresholds 12 / 17 / 30, `Γ_c = 125` | — | **CITED from Naji et al. (2005)**, who obtain them by Monte Carlo. Not re-derived; re-deriving them would be the expensive calculation this task exists to avoid. |
| the Netz/Naji formulas themselves | Eqs. (3)(4)(5)(7)(9)(13)(15)(16)(19)(20)(24)(28)(30) | **CITED formulas, DERIVED evaluations.** Two of them cross-check each other (Eq. 19/13 against Eq. 20), which is the transcription check. |

Everything else — `l_B`, `λ_D`, `σ_s`, `Ξ`, `μ_GC`, `a_⊥`, `Γ`, `ξ_M`, the surviving fraction, the deviation,
the boundary, the partition coefficients, the gap inventory — is derived from the §3 parameters and SI constants.

## Cross-checks passed

1. **Gate 1** — `l_B`'s *defining property* asserted, not its formula; `Ξ = q²l_B/μ` identity; `Γ = sqrt(Ξ/2)` identity; `√π` between the two lateral-spacing conventions; `0.936 e/nm² = 0.150 C/m²`.
2. **Gate 2** — deviation vanishes and is exactly linear as `Ξ → 0`; strong-coupling pressure changes sign at exactly `Δ* = 2μ`; `Ξ ∝ q³`; `K → 1` as `φ → 0`; no Manning condensation below `qξ_M = 1`.
3. **Gate 3** — buffer electroneutrality; Manning condensed + free `= 1` and effective `ξ` exactly `1/q`; the mean-field profile integrates to exactly `σ_s/q` (analytic **and** Simpson + analytic tail, 1e-9); gap electroneutrality counted; Donnan geometric mean.
4. **Gate 4** — the transcendental solved by bisection and verified **by substitution** (residual < 1e-9); the asymptote checked as a **convergence order** (9.973 → 9.997 across decades); the boundary satisfies its own definition and is bracketed; **two independent boundary statements agree to 4.7%**.
5. **Gate 5** — **Naji Table I reproduced at their parameters** (`Ξ = 22.8` vs their 22.4, `μ = 1.25 Å` vs their 1.2 Å); **`Γ_c = 125 ↔ Ξ_c = 31 250`** recovered against their 3.1e4; **`ξ_M = 4.20`** against their 4.1; **0.150 C/m²** textbook B-DNA; **§3's 4 nm reproduced to 1.8%**; and the premise of the invoked criteria checked against this system by counting ions rather than assuming.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **§4(c) is NOT closed by this task.** The bound counts only *exclusion* mechanisms — steric and Born — so it
   is a **lower bound on `K`, i.e. one-sided**. PEG's ether oxygens coordinate cations (the mechanism behind
   PEO polymer electrolytes), which raises `K` and could in principle push it above 1, reversing the sign of
   the answer. No binding constant for Mg²⁺/PEG **in water** was located this iteration. What *is* solid is
   that the dielectric mechanism §4(c) names is negligible at `φ ≈ 0.03`. Queued.
2. **The intermediate-coupling regime has no theory.** `Ξ = 17–24`. Explicit-ion simulation is the only route,
   at the cost stated above, and it is not warranted yet.
3. **Helix-helix attraction inside the origami is unresolved.** `a_⊥ = 1.46 nm` against a 2.6 nm interhelical
   distance is a margin of only 1.8×, smaller than the spread between our surface-charge models — and origami
   folding demonstrably *requires* Mg²⁺, so the effect is not negligible at the folding stage. `T-5` territory.
4. **No published `Ξ` criterion exists for oppositely charged walls**, which is the actuated configuration.
5. **The `Ξ = 17` straddle** on the hydrated radius is not resolved.

## Challenges

[`CH-0004`](../challenges/CH-0004-screening-decay-length.md) is raised **by** this claim, against the
`λ_D`-as-decay-length assumption inherited from §1/§3 of the problem definition.
None stands against this claim.
