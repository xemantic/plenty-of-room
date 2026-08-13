# C-0010 — The Gen-1 tile's positional variance at 300 K, by mode, in band, and against 3.0 nm

| | |
|---|---|
| **Task** | [`T-8`](../tasks/T-8-tile-positional-variance.md) |
| **Leaf** | `A1.2`, with `A1.1` as its bound table |
| **Verification type** | in-silico (analytic multi-mode equipartition on a Rayleigh-Ritz orthotropic plate functional; **exact** for a harmonic functional) |
| **Verdict** | **PASS** on §6 task 8's predicate at the operating point, on the declared acceptance quantity, across the whole `C-0003` stiffness bracket. **Leaf `A1.2` is only PARTLY discharged** — see the section that says which half and why. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-8-tile-positional-variance.json`, produced by `structure.TilePositionalVarianceStudyKt`; 26 `TilePositionalVarianceTest` tests green |
| **Conditions** | T = 300 K, aqueous buffer 2/5/10 mM MgCl₂, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile; 10 nm layer at `σ = 0.024 nm⁻²`; §3 target force 100 pN |
| **Consumes** | [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (structure), [`C-0003`](C-0003-crossover-valid-layer-response.md) (stiffness bracket), [`C-0004`](C-0004-poroelastic-drainage.md) (bandwidth), [`C-0002`](C-0002-peg-material-parameters.md) (material), [`C-0009`](C-0009-discrete-lattice-tile.md) (the plate-to-lattice correction, as a cited factor) |
| **Raises** | [`CH-0009`](../challenges/CH-0009-worst-point-is-not-the-centre.md) against `C-0006` |

---

## Claim, in one line

**At the operating point the Gen-1 tile's positional RMS is 0.87–0.96 nm broadband and 0.069–0.110 nm inside the ≥ 1 kHz band,
against a 3.0 nm predicate — but it is dominated by the tile's *shape* modes rather than its position mode,
by 2.8–3.0×, it exceeds the predicate at the tile's *corners* in every state softer than the working point,
and the *lateral* coordinate is not confined by the layer at all.**

---

## The modal budget

Every degree of freedom the tile has against the layer, at the nominal working point
(strong-stretching / des Cloizeaux, `k = 59.31 pN/nm`, the tangent under the §3 100 pN load):

| mode | RMS [nm] | stiffness against it | what it is |
|---|---|---|---|
| **piston** (rigid normal translation) | **0.264** | `k_f A` = 59.31 pN/nm | the tile's **mean height** — the only mode with a non-zero area average |
| **two tilts** (rigid rotation about in-plane axes) | 0.374 combined | `k_f A/3` each, exactly | zero area average; full lever at the corners |
| **shape / dishing modes** | **0.768** | `k_f A + D q⁴ A` per mode | the tile's own bending. **The dominant term.** |
| **area RMS** (the declared acceptance quantity) | **0.894** | — | `√(piston² + tilt² + dishing²)` — a typical material point |
| centre point | 0.701 | — | the *stiffest* point of the footprint |
| edge midpoint | 1.155 | — | |
| **corner point** | **1.917** | — | the **worst** point; `√7 × piston` even for a rigid tile |
| **lateral / yaw** | **unbounded** | **exactly zero** | see below — not a variance problem at all |

`C-0001` reported **0.28 nm** at this design point. That is the piston mode of a rigid plate,
and it is **7.3× smaller than the tile's worst point** and 3.4× smaller than its area RMS.
`CH-0005` had already said the column was one-degree-of-freedom; this quantifies the gap at the working point.

## Across the `C-0003` stiffness bracket, at four stated compressions

**Stiffness is not a single number at the resting height** — `C-0001` surprise `S-1`, upheld by `C-0003` —
so nothing below is quoted "at `L₀`" without saying so.
Six models (box / strong-stretching × two-body / virial / des Cloizeaux),
all **re-derived** from the measured PEG/water virials rather than copied from `C-0003`'s table
and asserted against it as a gate-5 test.

| compression | `k` [pN/nm] | piston | area RMS | corner | dish/piston | verdict vs 3.0 nm |
|---|---|---|---|---|---|---|
| **first contact, `h = L₀`** | 9.83 – 13.81 *(box models only)* | 0.55 – 0.65 | 1.46 – **1.64** | 2.84 – **3.13** | 1.84 – 2.02 | area **PASS**, corner **FAIL** |
| first contact, strong stretching | **exactly 0** | — | — | — | — | **UNDEFINED** |
| `h = 0.9 L₀` | 3.31 – 17.72 | 0.48 – **1.12** | 1.34 – **2.46** | 2.65 – **4.38** | 1.35 – 2.15 | area **PASS**, corner **FAIL** |
| `h = 0.8 L₀` | 6.99 – 24.00 | 0.42 – 0.77 | 1.21 – **1.85** | 2.44 – **3.46** | 1.67 – 2.33 | area **PASS**, corner **FAIL** |
| **working point** (tangent at 100 pN) | **47.68 – 64.15** | 0.25 – 0.30 | 0.87 – **0.96** | 1.88 – **2.03** | 2.76 – **2.96** | **PASS on every quantity** |

**Margin at the operating point: 3.1–3.4× on the area RMS, 1.48–1.60× at the worst point.**
Worst anywhere in the bracket: area RMS **2.46 nm** (82 % of the predicate), corner **4.38 nm** (146 %).

### Two things this table says that a single number could not

**1. Actuating the tile quiets it.**
The piston RMS falls from 0.48–1.12 nm unbiased to 0.25–0.30 nm under load,
because the layer stiffens by 4–14× as it is squeezed.
The actuator is mechanically quietest exactly where it works.
This is one-sided: it is the *mechanical* channel only, and §1's electrostatic spring is **negative**,
so `T-4` owns the correction that runs the other way.

**2. The stroke and the noise are set by different stiffnesses, and the difference is 1.6× in amplitude.**
The **secant** (16.6–26.1 pN/nm, `C-0003`) sets the stroke; the **tangent at the working point**
(47.7–64.1 pN/nm) sets the fluctuation. Substituting one for the other overstates σ_RMS by ~1.6×.

---

## The unbiased state is not a well-posed question

Three of `C-0003`'s six models — every strong-stretching one — have **exactly zero** stiffness at first contact,
because the strong-stretching disjoining pressure vanishes quadratically at `L₀`
(the brush's outer edge is diffuse). Their positional variance there is not large; it is **undefined**.
Reported as `undefinedCases` in the result file with the reason, rather than as a large number.

And the far side is worse: a **non-adsorbing** layer exerts no *upward* force above `L₀` at all,
so an unbiased free tile is unconfined in **both** directions.
Whatever holds the tile down at zero bias is not in the §3 stack.

**Consequence for the predicate:** §6 task 8 is answerable *only at a stated compression*,
and meaningfully only at the working point, which is where the actuator operates.
The `T-8` verdict is quoted there, and every softer state is reported alongside as a bracket rather than as an answer.

---

## The dominant mode: `C-0006`'s finding survives, and strengthens

`C-0006` reported dishing/piston = **1.70** at `C-0001`'s stiffness at first contact,
and that the ratio *grows* as the foundation stiffens.
Both survive the change of stiffness bracket, and the second is stronger than `C-0006` could see:

| state | dish/piston |
|---|---|
| `C-0001` at rest (`C-0006`'s point, reproduced) | 1.70 |
| first contact, `C-0003` box models | 1.84 – 2.02 |
| `h = 0.8 L₀` | 1.67 – 2.33 |
| **working point** | **2.76 – 2.96** |

The mechanism is unchanged and is the reason the trend is monotone:
the dishing modes are stiffened by `D q⁴` **and** by the foundation, the piston mode only by the foundation,
so a stiffer foundation shrinks the piston mode faster than the shape modes.
Asserted as a gate-2 monotonicity test over `k_f` spanning 64×, not observed at three points.

**The tile's shape is noisier than its position, and it is nearly three times noisier at the design point.**

### And this is *not* sensitive to `T-9`

The single largest open premise under `C-0006` is `k_θ`, the crossover hinge constant,
whose `1/100` is borrowed from CanDo's *nick* softening. Swept over Chen et al.'s full admissible `α ∈ [0.6, 1.2]`:

| variant | `D_⊥` [pN·nm] | area RMS [nm] | corner [nm] | dish/piston |
|---|---|---|---|---|
| `α = 1.2` (stiff) | 4.014 | 0.886 | 1.901 | 2.87 |
| **nominal** | 3.345 | 0.894 | 1.917 | 2.91 |
| `α = 0.6` (soft) | 2.007 | 0.918 | 1.965 | 3.01 |

**A 2× change in `D_⊥` moves the answer by 2.5 %**, because the shape modes are foundation-dominated,
not rigidity-dominated, at `ℓ/L ≈ 0.2–0.5`. `T-8` does not inherit `T-9`'s open premise;
`T-5`, `T-5b` and `T-10` still do.

---

## Bandwidth: 99.4 % of the variance is above the measurement band

A variance without a bandwidth is the `f → ∞` limit, and for this system that is not the operating quantity.
`C-0004` establishes the tile is overdamped by six orders (`Q = 7 × 10⁻⁴`),
so each mode is first-order and its spectrum is a single Lorentzian,
`S(f) = 4k_BTγ/(k² + (2πfγ)²)`, whose integral below `f` is `(2/π) arctan(f/f_c)`.

The drag is `C-0004`'s squeeze-out `η G A / T` plus broadside Stokes,
recomputed at **each case's compressed height and volume fraction** on the least permeable of `C-0004`'s three models.

| | corner frequency | fraction of variance < 1 kHz | in-band area RMS | in-band corner RMS |
|---|---|---|---|---|
| nominal working point | **105.6 kHz** | **0.603 %** | **0.069 nm** | 0.149 nm |
| working point, over the bracket | 45.5 – 115.1 kHz | 0.55 – 1.40 % | ≤ 0.110 | ≤ 0.232 |
| **worst anywhere in the bracket** | **20.7 kHz** | **3.07 %** | **≤ 0.430** | **≤ 0.767** |

**Inside the ≥ 1 kHz band the predicate passes everywhere, by 3.9× at the very worst point of the bracket
and by 43× at the nominal working point — including at the compressions where the broadband corner figure fails.**
Bandwidth is worth up to **13× in amplitude**, which is more than the entire model bracket.

Using the piston mode's corner for the whole budget is conservative and provably so:
for a mode of wavelength `L/n` the drainage path shortens as `L/n` (`γ_n ∝ n⁻²`)
while the modal stiffness only rises, so every other mode has a **higher** corner,
and the in-band fraction is monotone decreasing in the corner frequency (gate 2).

---

## The lateral mode: not bounded by the layer, and not given a number it has not earned

**The layer's lateral restoring stiffness is EXACTLY ZERO, by symmetry rather than by smallness.**
The free energy of a laterally homogeneous grafted layer under a laterally homogeneous non-adsorbing tile
is invariant under lateral translation of the tile, so the mean lateral force vanishes identically.
The same argument kills the yaw mode.

Equipartition does not apply to an unconfined coordinate. What applies is diffusion:

| quantity | value | provenance |
|---|---|---|
| Brinkman screening length at the working `φ` | 0.650 nm | **DERIVED** from `C-0004`'s free-draining segment model |
| lateral drag `γ_∥ = η A coth(h/√k)/√k` | 2.103 × 10⁻⁶ pN·s/nm | **DERIVED** — Brinkman Couette between electrode and tile |
| lateral diffusivity `D = k_BT/γ` | 1.969 × 10⁶ nm²/s | **DERIVED** |
| excursion in 1 µs | 2.0 nm | |
| **excursion in 1 ms (one 1 kHz period)** | **62.8 nm** | **21× the predicate, 1.6 tile widths** |
| excursion in 1 s | 1985 nm | 50 tile widths |

So the lateral coordinate is bounded by the **anchoring scheme**, which §3 does not specify.
The requirement it must meet is leaf `A1.1`'s own bound:

&nbsp;&nbsp;&nbsp;&nbsp;**`k_lateral ≥ k_BT/(3 nm)² = 0.4602 pN/nm`.**

Whether that is reachable is stated as a **bracket, not as a design**:
a clamped 10 nm duplex strut gives `3EI/L³ = 0.69 pN/nm` and clears it by 1.5×;
a 20 nm one gives 0.086 pN/nm and misses by 5.4×;
a flexible single-stranded tether gives essentially nothing at zero tension,
because a chain's transverse stiffness is `F/L` and vanishes with the tension.
**Short and stiff, or not at all.** No lateral stiffness is asserted here.

Adding four normal anchors of total stiffness equal to the layer's own barely touches the *normal* budget
(area RMS 0.894 → 0.826 nm, corner 1.917 → 1.905 nm) — anchors are needed for the lateral mode,
and `C-0006` shows they cost flatness to provide it.

---

## The verdict, and what it is a verdict on

The predicate is **σ_RMS ≤ 3.0 nm**, and "σ_RMS" is not defined by §6 task 8.
Four readings are available and they differ by 7×, so the reading is declared rather than assumed:

| quantity | worst in the bracket | at the working point | verdict |
|---|---|---|---|
| piston (tile mean height) | 1.12 nm | 0.25–0.30 nm | **PASS**, 2.7× margin worst case |
| **area RMS (declared acceptance quantity)** | **2.46 nm** | **0.87–0.96 nm** | **PASS**, 1.22× margin worst case, 3.1–3.4× at the operating point |
| centre point | 1.81 nm | 0.69–0.75 nm | **PASS** |
| **worst point (corner)** | **4.38 nm** | **1.88–2.03 nm** | **PASS at the working point** (1.48–1.60×); **FAIL** at every softer compression |
| in-band (< 1 kHz), any of the above | 0.767 nm | ≤ 0.232 nm | **PASS**, 3.9× margin worst case |
| lateral | unbounded | unbounded | **NOT BOUNDED BY THE LAYER** — a requirement on the anchoring, not a result |

**Overall: PASS.** The tile holds position at 300 K well inside the requirement in the state it operates in
and in the band it operates over. The margin is **not** the 11× that `C-0001`'s 0.28 nm implied,
and it is **not** the 46–75 % of the predicate that `C-0006` projected either — it is 29–32 % broadband
at the operating point, and 2.3–3.7 % in band.

Two qualifications travel with the PASS and must not be dropped downstream:

1. **the worst point of the tile fails the predicate in every state softer than the working point**, and
2. **the lateral coordinate is not part of this PASS at all.**

---

## Which parts of leaf `A1.2` are discharged, and which are not

Per §7 — *"where a question can't be answered with the available methods, that is stated plainly."*

**Discharged:**

- σ_RMS ≤ 3.0 nm, evaluated for the nominal Gen-1 tile at 300 K in the stated medium;
- reported for **every** degree of freedom the tile has against the layer, not one;
- across a stated stiffness bracket (six `C-0003` models) at four stated compressions, plus the `k_θ` sweep;
- against a stated measurement bandwidth, from `C-0004`'s drainage corner rather than an assumed one;
- consistent with leaf `A1.1`'s bound table, which is reproduced from `k_BT` alone as a gate-5 test.

**NOT discharged, and not substituted for:**

- **"Simulated", in the sense of the leaf's own tool column — a coarse-grained/MD (oxDNA/Martini) ensemble.**
  This is an analytic multi-mode equipartition result, exact within its model.
- **A 95 % confidence interval.** There is no sampling here, so there is no sampling interval.
  A CI on an exact analytic result is a category error; the bracket above is a **model range**
  and reporting it as a CI would imply a statistical meaning it does not have.
- **The lateral mode**, which the layer does not confine at all.
- **The lever.** The leaf says *lever* positional variance; this is the **tile's**.
  `C-0006` shows the two differ by the local dishing wherever the coupling is not effectively continuous,
  and that it cannot be made continuous with fewer than **64** attachment points against the **56 crossovers**
  the tile contains (`C-0006`'s 55 as corrected by `C-0009`).

**Why the named method was not run**, costed per §5:
oxDNA models the origami and **not the polymer layer it rests on**, and the tile's positional variance
is set by that layer. Run as specified it would return a confidence interval on the fluctuation of a free
origami sheet in buffer — a different quantity. To answer the right question the layer would have to enter
as an external potential, and that potential is exactly the `C-0003` stiffness used here directly.
It is a days-scale job on this box and a `T-9` prerequisite sits in front of it.
The costed proposal for what *would* discharge it is in the [task file](../tasks/T-8-tile-positional-variance.md#what-would-discharge-the-rest-of-leaf-a-12-costed).
The cheap partial substitute — `T-10`'s discrete-lattice check of the same premise — **has already run**
([`C-0009`](C-0009-discrete-lattice-tile.md)) and bounds the error at +11–20 % on the dishing modes,
so an ensemble would now buy only the confidence interval itself.

---

## Validity range

Respected downstream, and enforced in code where it can be.

- **Harmonic response only.** Each case is the *tangent* stiffness at a stated compression.
  Cases where the piston RMS would exceed the layer height are emitted as `undefinedCases`, not as numbers.
- **The unbiased state is outside the model**, in both directions — zero stiffness at `L₀` for the
  strong-stretching profiles, and no upward force above `L₀` for any non-adsorbing layer.
- **No electrostatics.** Under bias `k_es < 0` (§1), so every amplitude here is a **LOWER bound under bias**.
  This is the one correction that runs the wrong way, and `T-4` owns it.
  At the working point the corner-point margin is only 1.48–1.60×, so a bias that halves `k_eff`
  puts the worst point over the predicate.
- **Kirchhoff plate on a linear Winkler foundation.** `C-0009` (`T-10`, concurrent) has now run the
  discrete-lattice check, so this is a **cited correction rather than an argument**:
  for the **thermal** case specifically the grillage dishes **1.113–1.199×** the plate,
  rising with `k_f`, so every dishing amplitude here is an **underestimate by 11–20 %**.
  `C-0009` also shows the direction is *load-dependent* and that `C-0006`'s blanket
  "a lattice has more shape freedom" is refuted for smooth and anchor-reacted loads —
  but `k_BT` populates every mode including the flat ones, which is exactly the case where it holds.
  Scaling the whole dishing component by **1.20** is an upper bound on the correction:
  the working-point area RMS becomes **1.03 nm** and the corner at most **2.30 nm**.
  **No verdict in this claim moves.**
- **The corner is a mathematical point.** A real attachment has finite size and would average the
  fluctuation over it, so `cornerRms` is the strictest reading available rather than a prediction about a
  particular tether. It is converged to < 0.01 % in the Ritz basis (1.9169 nm at degrees 12/16/20).
- **The drag is the piston mode's**, applied to the whole budget. Conservative: every other mode has a
  higher corner frequency, so the in-band fraction is an upper bound.
- **Permeability is uncertain by 40×** (`C-0004`), and the slowest model is used throughout,
  which is the direction that maximises the in-band variance.
- **The layer is non-adsorbing and laterally homogeneous.** Both are what make the lateral stiffness
  exactly zero. Any specific adsorption, or any lateral patterning of the grafting, would create a
  lateral corrugation potential — and that is a *design lever* nobody has costed.
- **Linear PEG.** A PS→PEG block copolymer (§3) is not the material any of this was built on.
- **Nothing here is measured about this tile.** `PASS` means model-consistent and traceable.

## Numbers that are cited rather than derived

Flagged per §7.

- **`EI = 230 pN·nm²`, `GJ = 460 pN·nm²`** — **CITED**, the CanDo parameter set (Kim et al., *NAR* **40**:2862, 2012).
  `EI` enters twice here: through `D_∥`, and through the 10 nm duplex-strut lateral bound.
- **`k_θ = 2αB/(100a)`, `α ∈ [0.6, 1.2]`** — **CITED, fitted to measurement**, Chen et al., *JACS* **136**:6995 (2014) SI.
  The `1/100` is a modelling assumption carried over from CanDo's nick softening. **Swept, and shown not to matter here** (2.5 %).
- **Interhelical distance 2.69 nm** — **CITED, MEASURED** (SAXS), Fischer et al., *Nano Lett.* **16**:4282 (2016).
- **Crossover spacing 32 bp per interface** — **CITED**, Rothemund, *Nature* **440**:297 (2006).
- **`A₂ = 1.9 × 10⁻³`, `A₃ = 2.0 × 10⁻²`, `α = 0.49`, `b = 1.1 nm`** — via `C-0002`/`C-0003`, with their own flags;
  the stiffness bracket is **DERIVED** from them here rather than copied from `C-0003`'s table,
  and asserted against that table to < 0.1 % as a gate-5 test.
- **The permeability models and the squeeze-out drag** — via `C-0004`, with its own flags
  (the Jackson–James fibre correlation is cited and its primary source was not obtained; it is used here
  because it is the *slowest*, i.e. the conservative direction for this task).
- **`η(300 K) = 8.541 × 10⁻⁴ Pa·s`** — via `C-0004`'s correlation, cross-checked against IAPWS.
- **The 3.0 nm predicate, the 1 kHz bandwidth, the 100 pN force, the 40 × 40 nm footprint, the 10 nm layer** — §3 and §6.
- **Leaf `A1.1`'s bound table** — **CITED** from the NDI task map, and **reproduced** from `k_BT` alone rather than accepted.

Everything else is derived from these in code.

## Challenges

**Raises [`CH-0009`](../challenges/CH-0009-worst-point-is-not-the-centre.md) against `C-0006`.**
None stands against this claim.

**Consumed as a correction:** [`C-0009`](C-0009-discrete-lattice-tile.md) / [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md),
which landed concurrently and measures the plate-to-lattice thermal ratio at **1.113–1.199**.
That was the largest exposure this claim would otherwise have carried as an argument,
and it is now a number: +11–20 % on the dishing modes, no verdict moved.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological
grounds rather than overwriting it. The most likely such result is `T-4`'s:
`k_es < 0`, so every amplitude here is a lower bound under bias, and the corner-point margin at the
working point is only 1.48–1.60× before the lattice correction and ~1.3× after it.
