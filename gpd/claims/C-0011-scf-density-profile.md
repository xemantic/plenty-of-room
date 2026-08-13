# C-0011 — The Gen-1 layer response from a solved density profile, and the 10 nm window

| | |
|---|---|
| **Task** | [`T-1d`](../tasks/T-1d-scf-density-profile.md) |
| **Leaf** | `A2.1` |
| **Verification type** | in-silico (numerical SCF, Edwards propagator), closed against the analytic ideal-chain solution and against `T-1c`'s two profile models in the limits where each is exact |
| **Verdict** | **PASS** — all six acceptance predicates discharged |
| **Maturity** | **TRL 1–3. The interaction free energy and the chain statistics are anchored to published measurement; nothing about *this* layer is measured.** |
| **Provenance** | `gpd/results/T-1d-scf-density-profile.json`, produced by `brush.ScfDensityProfileStudyKt`; 183 design points × 5 models = 915 responses, 33 min wall clock on 4 threads, 373 MB peak |
| **Conditions** | T = 300 K, aqueous, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile (A = 1600 nm²); linear PEG |
| **Consumes** | [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `n_K`, `α`) and [`C-0003`](C-0003-crossover-valid-layer-response.md) (`A₂`, `A₃`, the interaction bracket, the thermal-blob count) |
| **Raises** | [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) against `C-0003` and against `P-5`'s adopted brush criterion |

---

## The claim, in one line

**At `L₀ = 10 nm` the design window is NOT empty — `σ ∈ [0.012, 0.260] nm⁻²` under every one of the
three measurement-anchored interaction laws and over two decades of the threshold that defines the
resting height — and the reason `T-1c`'s box profile said otherwise is that both of its profile
models omit the chain's entropic resistance to confinement, which at Gen-1 densities is the
*entire* disjoining pressure rather than a correction to it.**

The definition-free form of the same statement: at one and the same chain (`N = 62.1`,
`σ = 0.024 nm⁻²`) the Alexander box puts the layer's resting height at **2.15 nm** and strong
stretching at **2.17 nm** — both predict the tile floats free above ~2.2 nm — while the solved
profile has the tile carrying **78 pN at 10 nm**.

---

## What replaced what

| `T-1c` used | `T-1d` uses | why |
|---|---|---|
| two profile *ansätze*, a box and a truncated parabola | the profile **solved** from an Edwards propagator | `T-1c`'s falsifier (3) fired: the profile, not the interaction, decides the window |
| chain elasticity as a pull-back (box) or absent from the wall pressure (SST) | the full contour-resolved propagator, so the conformational normal stress is *in* the pressure | at `φ ≈ 0.01–0.05` it is the whole of the pressure, not a correction |
| `L₀` a sharp free-energy minimum | `L₀` **defined** by a stated resting load, with a decade of sensitivity either side | a solved profile has a real decaying tail and reaches `P = 0` only asymptotically |
| `L₀/R₀ ≥ 1` as the brush criterion (`P-5`) | `L₀/R₀ ≥ 1` **and** coil overlap `Σ = πR₀²σ ≥ 1` | against a solved profile the first criterion is **vacuous** — satisfied at every `σ` in the sweep |
| the same measured `f_int(φ)` | **unchanged** | which is what makes the two answers differ in the profile and in nothing else |

---

## The method, and the one thing it does not assume

A continuous-chain (Edwards) propagator, not a Scheutjens-Fleer lattice:

&nbsp;&nbsp;&nbsp;&nbsp;`∂q/∂n = (b²/6n_K) ∂²q/∂z² − w(z) q`, &nbsp; `w(z) = μ(φ(z))/k_BT`, &nbsp; `μ = v₀ ∂f_int/∂φ`

with one chain end fixed at `z = 0`, the other free, and **absorbing (Dirichlet)** conditions at both
the grafting surface and the tile — which is what a rigid impenetrable wall is for a chain.

The Gaussian chain model is **earned, not assumed**: `C-0003` measures PEG in water as a marginal
solvent whose thermal blob is 1222 Kuhn segments (167 kDa), against Gen-1 chains of 0.02–0.10 of one
blob. A lattice was rejected because it would have had to re-express the measured `f_int(φ)` as a
Flory `χ` on a lattice-site convention worth a factor of **2.010** (`C-0007`) — throwing away the
anchoring in osmometry that made this calculation worth buying at all.

---

## The numbers

### The window — the deliverable

Stroke ≥ 3 nm at 100 pN over the 40 × 40 nm tile, at the primary resting-load threshold of 1 pN:

| `L₀` | `T-1c` box | `T-1c` strong stretching | **`T-1d` SCF**, `L₀/R₀ ≥ 1` only | **`T-1d` SCF**, and `Σ ≥ 1` |
|---|---|---|---|---|
| 5 nm | empty | empty | `[0.0020, 0.0056]` | **empty** |
| 7 nm | empty | empty | `[0.0020, 0.0496]` | **`[0.0296, 0.0496]`** |
| 10 nm | empty | `[0.0176, 0.0610]` | `[0.0020, 0.2601]` | **`[0.0116, 0.2601]`** |

`T-1c`'s strong-stretching window is reproduced to the grid — `[0.0176, 0.0610]` against its
`[0.018, 0.061]` — which is what says this study and `T-1c` are running the same problem.

Three things follow, and the third is the one `T-2` needs.

1. **The 10 nm window exists**, it is **22.4×** wide in `σ` against strong stretching's 3.5×, and it
   barely depends on which interaction law is used: with the overlap cut, `[0.0116, 0.3937]`
   (two-body), `[0.0116, 0.2601]` (virial), `[0.0116, 0.2601]` (des Cloizeaux). The lower edge is
   identical across all three — it is set by coil overlap, not by the interaction.
2. **7 nm is not empty either**, which contradicts a standing finding carried since `C-0001`. It is
   narrow — `[0.0296, 0.0496] nm⁻²`, a factor of 1.7 — and it is the *lower* edge that binds, which
   is the tension §4(a) names.
3. **The ~10 nm desired stroke is still unreachable everywhere** — the largest stroke anywhere in the
   sweep is **7.20 nm**, at `σ = 0.0020 nm⁻²` where `Σ = 0.33` and the coils do not overlap at all,
   and **6.01 nm** at the lowest overlap-valid density. `C-0001`'s one surviving headline survives a
   third model, a solved profile and a wider window.

### The chain length, and why it is a procurement-level difference

At `L₀ = 10 nm`, `σ = 0.024 nm⁻²`:

| | `C-0001` | `C-0003` bracket | **`T-1d` SCF** |
|---|---|---|---|
| `N` | 199.4 | 224.8 – 374.3 (box 288.5, SST 224.8 recomputed here) | **62.1** |
| PEG | 8.8 kDa | 9.9 – 16.5 kDa | **2.7 kDa** |
| mean `φ` | 0.0289 | 0.0326 – 0.0543 | **0.0090** |
| peak `φ` | — | — | **0.0246** |
| `2⟨z⟩` | 10 nm (box, by construction) | 7.85 nm (parabola) | **5.46 nm** |

Across the overlap-valid part of the 10 nm window the chain runs **74.0 → 36.6 monomers, 3.26 → 1.61 kDa**.
That is a different polymer to order.

**How much of that gap is definitional is stated rather than hidden.** `L₀` here is a force-onset
height and in `C-0003` it is the edge of a trial function; they are not the same quantity. Scaling
the SCF first-moment thickness by the measured `N^(0.5–0.55)` — an **extrapolation, not a computed
design point** — a layer whose `2⟨z⟩` is 10 nm would need `N ≈ 190–210`, which brushes the bottom of
`C-0003`'s bracket rather than sitting a factor of four below it. **So most of the chain-length gap
is the convention, and what is left over is the conformational pressure.** `CH-0010` carries both.

### Stiffness and stroke — where `C-0003` survives

At `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, 40 × 40 nm tile:

| quantity | `C-0003` bracket | **`T-1d` SCF** | inside? |
|---|---|---|---|
| stroke at 100 pN | 3.83 – 6.01 nm | **5.31 nm** | **yes** |
| secant stiffness | 16.6 – 26.1 pN/nm | **18.84 pN/nm** | **yes** |
| `k(0.8 L₀)` | 7.0 – 24.0 pN/nm | **6.54 pN/nm** | marginally below |
| `N` | 224.8 – 374.3 | **62.1** | **no** |
| mean `φ` | 0.0326 – 0.0543 | **0.0090** | **no** |

**`C-0003`'s response numbers survive; its structural numbers do not.** The stroke and the secant
stiffness — the two quantities `T-3`, `T-4` and `T-8` consume — land inside its bracket. The chain
length and the volume fraction, which `C-0002` used to place the layer on the measured equation of
state, are three to six times off.

Across the overlap-valid 10 nm window (`σ` 0.0116 → 0.2601 nm⁻²): `N` **74.0 → 36.6** (3.26 → 1.61 kDa),
`k(0.8L₀)` **4.13 → 36.09 pN/nm**, `k_sec` **16.67 → 32.80 pN/nm**, stroke **6.00 → 3.05 nm**,
`σ_RMS` at the working point **0.22 → 0.19 nm**.
Across the overlap-valid 7 nm window (`σ` 0.0296 → 0.0496 nm⁻²): `N` **27.8 → 25.3** (1.23 → 1.11 kDa),
`k(0.8L₀)` **12.27 → 16.67 pN/nm**, `k_sec` **29.44 → 32.64 pN/nm**, stroke **3.40 → 3.06 nm**.

### Where the layer sits on the equation of state — reversed

`C-0002` placed the layer at `φ/φ# = 1.08–1.23` and `C-0003` at `1.40–3.51`, i.e. *above* the
dilute→semidilute crossover. On the solved profile the mean volume fraction at the 10 nm design
point is **0.00900**, and `φ#` for a 62-monomer chain is `(αN)^(−4/5) = 0.0651`, so

&nbsp;&nbsp;&nbsp;&nbsp;**`φ/φ# = 0.138` — the layer is a factor of seven *below* the crossover, not above it.**

The peak of the profile reaches `φ = 0.0246`, i.e. `0.378 φ#`. **No part of this layer is semidilute.**
That is the strongest form yet of the finding `CH-0001` opened and `C-0003` sharpened.

### The resting-load threshold, priced

| resting load | `N` at `σ = 0.024` | stroke | `k_sec` | `k(0.8L₀)` | window at 10 nm (`Σ ≥ 1`) |
|---|---|---|---|---|---|
| 0.1 pN | 43.6 | 5.93 nm | 16.9 pN/nm | 2.30 pN/nm | `[0.0195, 0.8129]` |
| **1 pN** | **62.1** | **5.31 nm** | **18.8 pN/nm** | **6.54 pN/nm** | **`[0.0116, 0.2601]`** |
| 10 pN | 108.6 | 4.02 nm | 24.9 pN/nm | 17.9 pN/nm | `[0.0056, 0.0610]` |

**A hundred-fold change in the threshold moves the chain length 2.5×, the stroke by 32 %, and the
window's existence not at all.** At the *tightest* threshold the window collapses onto
`[0.0056, 0.0610]`, whose upper edge is `T-1c`'s strong-stretching edge exactly — which is the sense
in which `T-1c` was right about the 10 nm window and wrong about how much of it there is. That is what licenses the headline despite `L₀` being a definition.

### The wall boundary condition, priced

| condition | `N` at `L₀ = 10 nm` | `φ(wall)` at `0.9 L₀` | `P(0.9 L₀)` | stroke | `k_sec` |
|---|---|---|---|---|---|
| **absorbing** (a rigid tile) | 62.1 | **0** exactly | 0.00180 MPa | 5.31 nm | 18.84 pN/nm |
| reflecting (two-brush mid-plane) | 130.3 | 0.00937 | 0.00185 MPa | 5.43 nm | 18.43 pN/nm |

The reflecting wall needs **2.1× the chain** to reach the same height, because it costs the chains no
conformational entropy — and then delivers the same pressure and the same stroke to within 2 %.
`T-1c`'s contact-value theorem `P = Π_int(φ(h))` is exactly true only under the reflecting
condition; under the absorbing one the volume fraction at the wall is zero and the whole normal
stress there is conformational.

### The profile itself, at `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, `N = 62.1`

| model | resting height | thickness `2⟨z⟩` | peak `φ` |
|---|---|---|---|
| Alexander box | **2.152 nm** | 2.152 nm | 0.0418 |
| strong stretching | **2.169 nm** | 2.169 nm | 0.0585 |
| **SCF** | 10 nm (at 1 pN) | **5.459 nm** | **0.0246** |

Only 0.03 % of the grafted coverage lies above 10 nm, so the layer is not "reaching 10 nm" by a
negligible tail — it is genuinely carrying the tile there, at 78 pN, from a profile whose bulk sits
around 5.5 nm.

---

## Verdict on `T-1c`'s two profile models

**Neither is closer to the truth in the way `T-1c` expected.** `T-1c` treated the spread between them
as profile uncertainty and said it was a *lower* bound. It is a lower bound on the wrong quantity:
the two models differ from each other by ~1 % in resting height at a fixed chain, and both differ
from the solved profile by a factor of 4.6 on the same quantity. They agree with each other because
they share a defect, not because they bracket an answer.

Ranked on what each gets right at the Gen-1 design point:

| quantity | box | strong stretching | which is closer |
|---|---|---|---|
| `L₀(N, σ)` at fixed `N` | 2.152 nm | 2.169 nm | **neither** — both 2.5× short of the solved layer's own `2⟨z⟩` and 4.6× short of the force-onset height |
| exponent of `L₀` in `N` | 1 exactly | 1 exactly | **neither** — the solved value is 0.49–0.64 |
| `P(h)` at `h = 10 nm`, `N = 62.1` | zero (no contact) | zero (no contact) | **neither** — the solved value is 78 pN over the tile |
| stroke at 100 pN, at fixed `L₀` | 3.83 nm | 5.10 nm | **strong stretching**, 4 % from the solved 5.31 nm against the box's 28 % |
| `k(0.8L₀)`, at fixed `L₀` | 24.0 pN/nm | 11.6 pN/nm | **strong stretching**, 78 % high against the box's 267 % |
| `k_sec`, at fixed `L₀` | 26.1 pN/nm | 19.6 pN/nm | **strong stretching**, 4 % high against the box's 39 % |
| the 10 nm window | empty | `[0.018, 0.061]` | **strong stretching**, right about existence, 12× too narrow |

**So: `T-1c` should have trusted strong stretching, and `C-0003` said so** — *"the box profile is a
restricted trial function and therefore a variational upper bound on the free energy, so the
strong-stretching answer is the better one"*. That judgement is upheld. What `C-0003` could not know
is the size of the error it was still carrying: right about the window's existence, 12× too narrow in
`σ`, 3.6–6.0× too long in `N`, and wrong by a factor of 4.6 about how tall a given chain stands.

---

## Validity range

Enforced in code where it can be, and stated with a number where it cannot.

- **`N σ v₀ / 0.8 < h`.** Below that the layer would be a melt on average and `f_int(φ)` is being
  evaluated an order of magnitude outside the 0–50 wt % it was fitted over. Evaluating outside throws.
- **`L₀` is threshold-defined and the threshold travels with every number.** 1 pN over the 40 × 40 nm
  tile, with 0.1 and 10 pN carried. This is not a defect of the calculation; it is a property of a
  layer whose outer edge is a real end distribution.
- **A one-dimensional mean field needs lateral homogeneity, i.e. coil overlap.** `Σ = πR₀²σ ≥ 1` is
  reported at every design point and the windows are emitted with and without it. Below `Σ = 1` the
  layer is a carpet of isolated mushrooms and the 1-D profile describes something that does not
  exist. **`L₀/R₀ ≥ 1`, the criterion `P-5` adopted, does not detect this** — at `L₀ = 5 nm`,
  `σ = 0.0041 nm⁻²` the layer has `L₀/R₀ = 1.77` and `Σ = 0.10`. See `CH-0010`.
- **Mean field.** No fluctuation corrections, no correlation hole, no lateral inhomogeneity. The
  ground-state-dominance approximation is *not* made — the full contour-resolved propagator is used —
  but the field is a mean field, and at `φ ≈ 0.01` the fluctuation corrections are **not bounded here**.
- **Monodisperse chains, fixed and laterally uniform grafting points, rigid tile** (`C-0006`,
  `CH-0005`).
- **The interaction free energy below `φ#` is not measured**, and this layer is entirely below `φ#`
  — which makes that gap *more* load-bearing than it was for `T-1c`, not less. The three interaction
  laws are carried as a bracket and they move the 10 nm window's upper edge from 0.260 to 0.394 and
  its lower edge not at all.
- **Every osmotic input is a BULK property applied to a BRUSH** (`P-9`), and `α`, `A₂`, `A₃` were
  measured on **linear PEG in pure water** at 20–25 °C. §3 also permits a PS→PEG block copolymer.
- **Mechanical only.** No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
- **Nothing here is measured about this layer.** `PASS` means model-consistent and traceable.

## Numbers that are cited rather than derived

Flagged, per §7 of the problem definition:

- `A₂ = 1.9 × 10⁻³ mol·cm³/g²` — **CITED**, and read in a re-tabulation rather than in the primary
  source, via `C-0003`. Three directly-read values bracket it.
- `A₃ = 2.0 × 10⁻² cm⁶·mol/g³` — **CITED**, via `C-0003`.
- `b = 1.1 nm`, `M_K = 137 g/mol` — **CITED**, via `C-0002` (Rubinstein & Colby Table 2.1). These
  now carry more weight than they did in `T-1c`: the Edwards diffusion coefficient is `b²/(6n_K)`,
  so the whole conformational pressure — which is the whole pressure — scales with `b²/n_K`.
- `α = 0.49` — **MEASURED**, via `C-0002`.
- `C-0003`'s brackets quoted in the comparison tables (`N` 224.8–374.3, stroke 3.83–6.01 nm,
  `k(0.8L₀)` 7.0–24.0 pN/nm, `k_sec` 16.6–26.1 pN/nm, `φ` 0.0326–0.0543) — **CITED from `C-0003`**,
  not recomputed here; the box and strong-stretching models *are* recomputed at every design point.

Everything else is derived from those and from the §3 parameters.

## Challenges

**Raised by this claim:** [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) against
`C-0003`'s height relation and `N(L₀)` bracket, and against the `L₀/R₀ ≥ 1` criterion `P-5` adopted.

**Standing against this claim:** none yet. The two largest exposures, both stated above rather than
absorbed, are (i) the mean-field treatment of a layer at `φ ≈ 0.01`, where fluctuation corrections
are not bounded, and (ii) the interaction free energy below `φ#`, which is now the whole working
range. A result that bounds either should be raised as a challenge rather than an overwrite.
