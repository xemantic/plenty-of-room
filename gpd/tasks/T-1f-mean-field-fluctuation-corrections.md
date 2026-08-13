# T-1f — Bounding the mean-field fluctuation corrections to the Gen-1 grafted layer at `φ ≈ 0.01`

| | |
|---|---|
| **Leaf** | `A2.1` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §2 (the premises), §3 (parameters), §6 task 1 and task 2, §4(a) |
| **Raised by** | [`C-0011`](../claims/C-0011-scf-density-profile.md), which states plainly that it does **not** bound them; carried forward by [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) as an outstanding item; promoted by [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) |
| **Verification type** | in-silico (closed-form Ginzburg/one-loop evaluation on the measured material parameters, then the solved SCF layer re-run over the range those corrections licence) + logical (which expansion each correction belongs to) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md), raising [`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) and [`CH-0020`](../challenges/CH-0020-thermal-blob-coarse-graining.md) |

---

## Formulate

### The question, as a numeric target

Every mechanical number in this programme comes from a **mean field**.
`C-0011` solves the Edwards propagator exactly — the ground-state-dominance approximation is *not* made —
but the field it solves in is `w(z) = μ(φ(z))/k_BT`, a functional of the *mean* local volume fraction,
and concentration fluctuations about it are absent.
`C-0011`'s own validity range says so:

> **Mean field.** No fluctuation corrections, no correlation hole, no lateral inhomogeneity. …
> at `φ ≈ 0.01` the fluctuation corrections are **not bounded here**.

`φ ≈ 0.01` is where that matters most.
The layer sits *below* `φ#`, in the dilute→semidilute crossover, which is exactly where the
Ginzburg criterion for a polymer solution is marginal — the regime the theory is *least* able to
speak about from inside itself.

Deliver **a multiplicative bracket on the layer's osmotic pressure and stiffness at the Gen-1
design points**, or declare the correction unbounded and name the missing method.
Propagate whichever survives to the two numbers the programme's verdicts rest on:
`C-0017`'s 1.19–1.42× coupling margin at 10 nm, and `C-0016`'s window edges.

### The scoping question, which must be answered before any number

`TASKS.md` promotes this task on the grounds that

> the 10 nm coupling margin is 1.19–1.42× and it sits *inside* `C-0005`'s 123–214 % one-loop correction,
> so the window's survival is **not excluded rather than established** until this is bounded.

**Those are two different mean-field expansions and the sentence silently identifies them.**

- `C-0005`'s 123–214 % is the **electrostatic** loop expansion: the strong-coupling parameter
  `Ξ = q²l_B/μ_GC` at the *charged* tile and electrode surfaces, `Ξ ∝ q³`, evaluated for divalent
  Mg²⁺ in the tile-electrode gap. It corrects `k_es`.
- `T-1f` as formulated is the **polymer** loop expansion: concentration fluctuations of a *neutral*
  chain in the Edwards model at `φ ≈ 0.01`, the Ginzburg criterion of a polymer solution. It corrects
  `k_brush`.

They act on the two different terms of `k_eff = k_brush + k_es` and neither bounds the other.
**Establishing which one the coupling margin actually sits inside is a deliverable of this task,
and if the promotion rationale conflated them that is a challenge, not a silent fix.**

### Acceptance predicate

`TASKS.md` states it as:

> The correction bounded, or declared unbounded with the missing method named.

Tightened here, and discharged only when **all six** hold:

1. **The expansion parameter is derived, not asserted** — a Ginzburg number for *this* material at
   *this* volume fraction, built from `C-0002`'s and `C-0003`'s measured `v`, `b`, `n_K`, `v₀`, with
   its convention (monomer / Kuhn segment; physical volume fraction / reduced density) stated
   explicitly, per `CLAUDE.md`'s standing warnings about both.
2. **The verdict on the perturbative route is stated in the same language `C-0005` uses** —
   controlled, marginal, or broken — and if it is broken, this task does **not** then quote a
   one-loop number as if it were a correction.
3. **Every fluctuation channel is separated and each is bounded or declared unbounded on its own**:
   the *interaction* channel (fluctuations of the concentration field, which correct `Π_int`), the
   *conformational* channel (intrachain excluded-volume swelling, which corrects the term `C-0011`
   says is the entire disjoining pressure), and the *translational* channel (which grafting removes).
4. **The bound, where one exists, is obtained non-perturbatively** — by re-running the solved layer
   over the whole range the broken expansion licences, rather than by adding a one-loop term to a
   free energy whose expansion has failed.
5. **Premises checked against PEG/water at 300 K**, not against the textbook: a correction derived
   for a swollen semidilute solution is invoked outside its premise here, and the thermal-blob count
   that decides this is re-derived rather than inherited.
6. **The propagation is to named downstream numbers** — `C-0017`'s 10 nm stability floor and margin,
   and `C-0016`'s window edges — with the statement of *which* quantity each bound applies to.

### Units, locked

SI, scaled: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly).
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer** (2–10 mM MgCl₂, not entering this task).

### Conventions, fixed before deriving

Restated rather than referenced, because two of them are exactly the traps `CLAUDE.md` warns about.

- A **volume fraction** is always the physical one, `φ = c v₀` with `v₀ = 0.0604 nm³` the monomer
  volume. Never a reduced density `n a³`.
- **The excluded volume `v` is a pair quantity and it does not coarse-grain linearly.** Written on
  monomers it is `v_m = B v₀ = 12.25 Å³` (`C-0003`). Written on Kuhn segments — which is what a
  formula containing `b` requires — it is `v_K = n_K² v_m`, **not** `n_K v_m`, because the
  interaction is `(v/2)∫c²` and `c_K = c_m/n_K`. This is checked in both conventions and the
  cross-check is a gate.
- `z` normal to the electrode, chains grafted at `z = 0`, tile a rigid non-adsorbing wall at `h`,
  `P > 0` when the layer pushes the tile along `+z`, `k = −A ∂P/∂h`. Unchanged from `T-1`/`T-1c`/`T-1d`.
- **`L₀` is a force-onset height** at 1 pN over the 40 × 40 nm tile (`C-0011`). Every `N(L₀)` here
  inherits it.
- A fluctuation correction is quoted as a **multiplicative bracket on a named response quantity at a
  named compression**, never as "the correction to the layer stiffness" — the standing finding of
  `C-0001`'s `S-1`.

### What is deliberately excluded

- **Electrostatics.** `C-0005` owns the electrostatic loop expansion, it reports it as *uncontrolled*
  across the entire working range, and nothing here narrows it. That is the point of `CH-0019`.
- Lateral inhomogeneity, polydispersity, tile compliance, adsorption, poroelasticity.
- **A simulation of the layer.** See the cost table.

---

## Plan

### The cheap bound first, per §5, and it is the whole of the perturbative half

The **Ginzburg number of the polymer solution**, in closed form, from measured parameters.

The Edwards model's random-phase structure factor at semidilute concentrations is
`S⁻¹(q) = b²q²/(12c) + v`, so the concentration correlation length is `ξ_E = b/√(12 v c)`, and the
Gaussian-fluctuation (one-loop) correction to the free-energy density has the Debye-Hückel form

&nbsp;&nbsp;&nbsp;&nbsp;`Δf = −k_BT/(12π ξ_E³)`, &nbsp;&nbsp; hence &nbsp;&nbsp; `ΔΠ = c ∂Δf/∂c − Δf = ½Δf = −k_BT/(24π ξ_E³)`

because `Δf ∝ c^{3/2}`. Against the mean-field two-body term `Π_MF = ½ v c²` that is

&nbsp;&nbsp;&nbsp;&nbsp;**`Gi(φ) ≡ |ΔΠ|/Π_MF = (12)^{3/2}/(12π) · √(v_K/(c_K b⁶))`**

— dimensionless, **independent of `N`**, and negative in sign (fluctuations *reduce* the pressure,
exactly as the ionic correlation term does in Debye-Hückel). `Gi = 1` defines a concentration
`φ**`, which is the thermal-blob concentration with a computed prefactor rather than a `~`.

This costs microseconds and it decides whether the expensive half is worth buying, which is
precisely the rule `SESSION-PROMPT.md` §5 sets.

### The cost table, stated before the choice

| method | cost | role |
|---|---|---|
| **Ginzburg number, closed form** | microseconds | the cheap bound — decides whether one-loop may be *used* |
| **Fixman `z` and the first-order swelling `α² = 1 + 4z/3`** | microseconds | the conformational channel, the one that acts on the term `C-0011` says is the whole pressure |
| **the solved SCF layer re-run over the interaction range the broken expansion licences** | ~20 min | the **non-perturbative** bound; buys a two-sided bracket where the expansion buys none |
| one-loop-corrected SCF (`f_int + Δf` as the field) | ~20 min | **NOT RUN, and the reason is a result** — see below |
| renormalised one-loop / field-theoretic simulation (complex Langevin, FTS) | weeks | the method that *would* bound it; named, costed, not run |
| lattice or off-lattice Monte Carlo of the grafted layer | days–weeks | the measurement-grade route; named, costed, not run |

**Why the one-loop-corrected SCF is not run.** If `Gi ≥ 1` at the layer's own volume fraction, then
adding `Δf` to `f_int` produces a *negative* osmotic pressure over part of the layer — the corrected
free energy is not a usable mean field, it is a signal that the expansion has broken. Running it
anyway and reporting the resulting profile would be quoting a one-loop number as a correction after
having shown the loop parameter exceeds one, which is exactly the error `C-0005` refuses to make for
the electrostatics (*"one cannot say from within the theory in which direction to correct it, or by
how much"*). The task therefore commits **in advance** to a non-perturbative substitute if the cheap
bound comes back marginal.

### The non-perturbative substitute, and why it bounds anything at all

Two facts already owned by the programme make a two-sided bound available even with the expansion broken.

1. **The sign of the correction is not in doubt.** The one-loop term is negative, and the two
   measurement-anchored interaction limbs bracket the same physics from above and below: the
   unscreened two-body `φ²` limb is the *mean-field* limit and the des Cloizeaux `φ^{9/4}` limb is the
   *fluctuation-renormalised* one. So the interaction strength cannot be increased by fluctuations,
   and the worst case is its **total destruction**, `K → 0`.
2. **`K → 0` is not a singular limit for this layer.** `C-0011`'s central finding is that at an
   absorbing wall `Π_int(φ(h)) ≡ 0` and the disjoining pressure is *entirely conformational*. So an
   SCF layer with the interaction switched off still holds the tile up, still has a resting height,
   and still has a stiffness. The `K → 0` layer is therefore a **computable floor**, not an infinity.

That converts an unbounded perturbative question into a bounded computational one:
sweep `K/K₀` from `10⁻⁶` to `4` through the solved layer at the Gen-1 design points — an
**exploratory** range four decades wider than the physics needs — and report the multiplicative
bracket on `N(L₀)`, `k(0.8L₀)`, `k_brush(L₀−3 nm)`, `k_sec` and the stroke over the **licensed**
sub-range `K/K₀ ∈ [0, 1]`, which is the whole of what a negative one-loop term can do.
`C-0003` predicts `k ∝ K^{1/(m+1)}` **exactly** — a relation derived for its two *ansatz* profiles,
where the pressure *is* the interaction. Whether it survives on the solved layer is measured here,
not assumed, and it is the cheapest available falsifier of the whole approach.

### The conformational channel, which nobody has yet costed

The SCF chain is Gaussian *in the mean field*: intrachain excluded-volume correlations — the
correlation hole, the self-avoidance of one chain with itself — are exactly what a mean field omits.
Their first-order effect is the classical Fixman expansion,

&nbsp;&nbsp;&nbsp;&nbsp;`z = (3/2πb²)^{3/2} v_K √n_K,chain`, &nbsp;&nbsp; `α² = 1 + (4/3) z`

with `n` the number of Kuhn segments in the chain, and in the layer the accumulation is cut off at
the Edwards screening length, so the screened value uses `n_ξ = (ξ_E/b)²` in place of `n`.
The correction enters the solved layer as an **effective segment length** `b_eff = α b`, which is
exactly the parameter the Edwards diffusion coefficient `D = b²/6n_K` is built from — and `C-0011`
records that the whole conformational pressure scales with `b²/n_K`. So it is propagated the same
way the interaction channel is: by re-solving, not by scaling an exponent.

**This channel also moves a window edge directly**, and it is the only one that does:
`C-0016`'s lower edge at every height is coil overlap `Σ = πR₀²σ ≥ 1`, and a swollen coil has
`Σ = πα²R₀²σ`, so the edge moves by exactly `1/α²`.

### What would falsify this approach

Stated in advance, before the run:

1. **`Gi ≪ 1` at the Gen-1 volume fractions.** Then mean field is controlled, the one-loop term is a
   small correction that can simply be added, the non-perturbative machinery is unnecessary, and the
   answer is a number rather than a bracket.
2. **`Gi ≫ 1` at the Gen-1 volume fractions *and* the layer response strongly sensitive to `K`.**
   Then no bound exists at all and the honest close is "unbounded, and here is the method that would
   bound it" — which `SESSION-PROMPT.md` accepts and which is worth more than a fabricated number.
3. **`C-0003`'s `k ∝ K^{1/(m+1)}` reproducing on the solved layer.** Then the interaction channel is
   already fully described by a relation the programme owns, this task adds nothing to it, and its
   only new content is the conformational channel.
4. **The `K → 0` layer failing to hold the tile at all** — i.e. `C-0011`'s "the pressure is entirely
   conformational" not surviving its own limit. Then the floor is not computable, the bracket is
   one-sided, and the interaction channel is unbounded below.
5. **The thermal-blob count reproducing `C-0003`'s 1222 Kuhn segments.** Then the premise check adds
   nothing and the chains are as far from swollen as the programme has assumed throughout.
6. **The propagated bracket moving `C-0017`'s 10 nm margin through 1.0, or emptying a `C-0016`
   window.** Then this task has changed a verdict rather than bounded an uncertainty, and it must be
   reported as such.

Outcome:

- **(1) did not fire.** `Gi = 1.302` at the layer's mean `φ` and `0.788` at its peak — marginal to
  broken, and it straddles unity *inside a single profile*. Across the window, 0.304 – 1.714.
- **(2) fired on its first clause and not on its second**, which is the whole claim: `Gi > 1` at the
  design points, and the layer response over the whole licensed range of `K` is nevertheless bounded
  to under ten per cent.
- **(3) FIRED, harder than expected.** The solved layer's measured `d ln k/d ln K` is **0.0647**,
  against `C-0003`'s exact `1/(m+1) = 0.3077` — a factor of **4.75**, not the ~2 the draft guessed.
  The solved layer is nearly *insensitive* to the interaction where the two ansatz models are merely
  weakly sensitive, for the same reason `CH-0010` gives: the pressure is not the interaction.
- **(4) did not fire.** The `K → 0` layer reaches `L₀ = 10 nm` at **`N = 64.57`** and delivers
  **`k(0.8L₀) = 6.101 pN/nm`** and a **5.3895 nm** stroke. A floor, not a singularity.
- **(5) FIRED, and with a twist the draft did not anticipate.** The thermal blob is **126.3 Kuhn
  segments, 392.8 monomers, 17.3 kDa** in the *scaling* normalisation — `n_K² = 9.671` smaller than
  `C-0003`'s 1222, because the excluded volume was coarse-grained linearly. **But in Yamakawa's exact
  normalisation with the corrected `v_K` it is 1160, within 5.3 % of `C-0003`'s number**, because
  `1/0.32992² = 9.187` and `n_K² = 9.671` nearly cancel. `CH-0020` is therefore a challenge to the
  *inference*, not only to the number.
- **(6) did not fire, and the half that was predicted came out differently.** `C-0017`'s 10 nm margin
  stays above one — it degrades from ≥ 1.19× to **≥ 1.07×** — and both `C-0016` windows **widen**,
  by 13.4 % at 10 nm and 1.8 % at 7 nm. But **not at the edge predicted**: the coil-overlap edge moves
  only 0.9 % and 0.3 %, not the `1/α² = 0.87` the identity suggests, because the chain length moves
  against the swelling. The widening is at the *stroke* edge.

---

## Execute

Code, in the `brush` package, tests written first:

- `src/main/kotlin/brush/FluctuationCorrection.kt` — `EdwardsCorrelation` (the screening length, the
  one-loop pressure correction, the Ginzburg number, `φ**`), `ChainSwelling` (Fixman `z`, free and
  screened, and the effective segment length), `kuhnExcludedVolume`, and
  `PegWater.thermalBlobKuhnSegmentsCorrected` — the `n_K²` form, kept beside the incumbent rather
  than overwriting it, per `SESSION-PROMPT.md`'s rule that contradiction raises a challenge.
- `src/main/kotlin/brush/FluctuationCorrectionStudy.kt` — the study entry point.

Tests: `src/test/kotlin/brush/FluctuationCorrectionTest.kt`, gate-named.

```shell
./gradlew test -PbuildDirectory=build-t1f
tools/study.sh brush.FluctuationCorrectionStudyKt
```

Result: [`../results/T-1f-mean-field-fluctuation-corrections.json`](../results/T-1f-mean-field-fluctuation-corrections.json),
**~20 min wall clock**, single-threaded. Deterministic: no timestamp, every floating-point number
rounded to nine significant digits at the serialisation boundary, and there is **no argmin** in the
file, so the trap `CLAUDE.md` records for `T-14` does not apply.

**Two runs were discarded before this one and both are worth recording rather than hiding.** The
first solved the window edges on the coarse grid and was thrown away when gate 4 measured a 23.4 %
grid sensitivity in that quantity. The one before it did not finish at all: a guard that evaluated
the disjoining pressure at the layer's own **saturation height** — where the layer is a melt, the
node spacing collapses to `h/24` and the contour step count goes as `1/Δz²` — ran the solver's
8000-iteration cap over a 10⁵-step contour and burned ninety minutes on a single record. **The
cheapest place to evaluate an SCF layer is never its own floor**, and the fix was to use the layer's
own `heightAtPressure`, which grows its bracket downward only as far as it must.

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.
Full detail in [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md#cross-checks-passed).

### Gate 1 — dimensional consistency

- `Gi` is dimensionless and invariant under the choice of segment: computed on **monomers**
  (`v_m`, `b_m = b/√n_K`, `c_m`) and on **Kuhn segments** (`v_K = n_K²v_m`, `b`, `c_K`) it agrees to
  1e-12. This is the gate that catches the `n_K²`, and it is why `CH-0020` is a fact rather than an
  opinion.
- `ξ_E` is a length in nm; `ΔΠ` is a pressure in pN/nm²; `Δf` has the units of `f_int`.
- `z` is dimensionless; `α` is dimensionless; `b_eff` is a length.

### Gate 2 — limiting cases

- `Gi ∝ φ^{−1/2}` exactly — the observed log-log slope is `−0.5` to **1e-9**.
- `Gi(φ**) = 1`, verified as a root rather than asserted (1e-12).
- `ΔΠ = ½Δf` to 1e-12, which is the statement that `Δf ∝ c^{3/2}`, and `ΔΠ < 0` at every `φ` tested.
- `α → 1` as `v → 0` (theta solvent) to 1e-9; the screened `α` never exceeds the free one, is never
  below 1, and equals the free one exactly at infinite screening length.
- **The `K → 0` SCF layer converges to the interaction-free layer**: `k(0.8L₀)` = 6.10613, 6.10186,
  6.10143, 6.10139 at `K/K₀` = 1e-2, 1e-3, 1e-4, 1e-6 — relative departures **7.8e-4, 7.8e-5,
  7.7e-6** from the last. The resting height at 1e-4 and 1e-6 agrees to 5e-3 (asserted in the suite).

### Gate 3 — symmetry and conservation

- The two-convention identity above (Gate 1) is the conservation statement of this task: a physical
  correction cannot depend on how the chain is chopped into segments.
- `Σ = πR₀²σ` scales exactly as `α²` under `b → αb` **at fixed chain length** (1e-12). This is
  asserted as an identity — and the measured edge shift is **0.9 %** against an `α²` of 1.150, which
  makes the gap a *result* (the chain length moves against the swelling) rather than a discrepancy.
- Scaling an interaction scales its pressure exactly and leaves its exponent untouched (1e-12).
- Grafted coverage `∫φ dz = Nσ` conserved to **1e-9** at `K/K₀` = 1e-4, 1 and 2.

### Gate 4 — numerical convergence

- `d ln k/d ln K` at `Δz` = 0.4 / 0.2 / 0.1 nm is **0.05983 / 0.06387 / 0.06475**, i.e. departures
  of **4.9e-3 then 8.8e-4** from the finest. Falsifier 3's verdict is not a discretisation artefact:
  the coarsest rung is already 4.7× below `C-0003`'s 0.3077.
- The `K → 0` floor is exhibited over four decades, not asserted at one value (see gate 2).
- **The window edge is the one quantity here that is NOT grid-insensitive**, and it is reported with
  its measured sensitivity rather than without: the 10 nm stroke edge is **0.34265 at `Δz` = 0.4 nm
  and 0.27770 at 0.2 nm — a 23.4 % departure.** Every edge quoted in `C-0019` is therefore on the
  0.2 nm grid, and the first run of this study — which solved them on the coarse grid — was discarded.
- **The result file is re-run end to end on a second independent snapshot and diffed byte-for-byte
  identical**, and the full suite is **931 tests in 61 classes, 0 failures**.

### Gate 5 — literature cross-check, premises checked against the material

- `ξ = b/√(12vc)` and `Δf = −k_BT/(12πξ³)` are **read verbatim in the primary literature**, not
  recalled: Wittmer et al., *J. Stat. Phys.* **145**:1017 (2011), arXiv:1107.4454 **Eq. (69)**,
  attributed there to Doi & Edwards Eqs. (5.45)–(5.46), with the criterion `Gz ≪ 1` at **Eq. (48)**.
  The one member of the family with a published numerical check — the inverse osmotic
  compressibility, their **Eq. (99)**, verified against bond-fluctuation simulation over three
  decades — is **reproduced here from `Δf` as a test**, coefficient `3√3/2π`, to 1e-12.
- `z` and `α² = 1 + 4z/3` read in Yamakawa (1971) **Eqs. (13.32)/(13.33)**, with the prefactor
  `(3/2π)^{3/2} = 0.32992` and `β` identified as the *pair* excluded volume at his **Eq. (13.3)**.
- **`C-0011` reproduced by re-running it**: `N = 62.11` against its 62.1, `2⟨z⟩ = 5.459 nm` against
  5.459, `k_sec = 18.836 pN/nm` against 18.84.
- **`C-0016`'s four window edges reproduced by an independent root** — −6.7 %, +6.7 %, −0.7 %,
  +10.9 % — every one inside its own 1.109× grid resolution.
- **The premise is checked against PEG/water at 300 K rather than the textbook**, and it fails in the
  direction that matters: the layer's `φ = 0.00900` is **0.590 of `φ** = 0.015255`**, so the layer is
  *not* in the mean-field-controlled regime, and it is *also* below `φ#`, so it is not in the
  fully-developed des Cloizeaux regime either. **No regime label applies to it.**
- `ξ = 4.215 nm` against `R₀ = 4.916 nm` at the design point, and **1.34 chains per correlation
  area** — the geometric statement of the same marginality.
- The thermal-blob count is re-derived in two conventions; it disagrees with `C-0003` by `n_K² = 9.671`
  in the scaling normalisation and by only 5.3 % in Yamakawa's. `CH-0020`.
- **The direction is corroborated by every simulation-versus-SCF comparison found**: Monte Carlo gives
  a brush compression exponent of 2.73 ± 0.04 against SCF's 2.15 ± 0.05 (Cerdà et al.,
  arXiv:cond-mat/0406075), and MD 2.135 ± 0.032 against strong stretching's 1.743 ± 0.016 at low
  grafting density (Manav et al., arXiv:1811.05089). Correlations reduce the layer's pressure, and
  they bite hardest where Gen-1 sits. **None of them supplies a bound.**

### Not verified, and stated as such

- **No fluctuation-corrected profile is computed.** The bound is a bracket obtained by re-running the
  mean-field solver over the range a broken expansion licences, not a solution of a corrected theory.
- **The interaction channel's floor assumes the correction cannot change the sign of `Π_int`.**
  A net-attractive layer is outside the family of free energies used anywhere in this programme
  (`CLAUDE.md`), and this task inherits that boundary rather than testing it.
- **The conformational channel is first order in `z`.** `z ≤ 0.32` over the whole design space, which
  is inside the usual range for the linear form, but no second-order term is carried.
- **Lateral fluctuations are not treated at all** — the correction computed here is to a 1-D field.
- Nothing here is measured about this layer. `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md), raising
[`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) against the standing rationale that
this task bounds `C-0017`'s exposure, and
[`CH-0020`](../challenges/CH-0020-thermal-blob-coarse-graining.md) against `C-0003`'s thermal-blob count.
