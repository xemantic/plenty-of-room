# T-192 — Device B in the corner all three NDI answers point at: a 10 pN/nm coupling on a 17–26 nm layer at 0.5 mM

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"* — the placement/stability half of it), with `A2.2` (the bias axis) and `A7.4` (the field) |
| **Verification type** | **in-silico** (`C-0008`'s nonlinear Poisson-Boltzmann gap solve and `C-0018`'s stroke-parametrised equilibrium path, both re-run as libraries at heights neither has ever been asked about) **+ logical** (a reachability bound on the force that needs no layer model at all, and runs first) |
| **Raised by** | NDI's answers to `DECISIONS-FOR-NDI.md` decisions **2** and **4** (2026-08-18) |
| **Consumes** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the field, its solver, the three decay lengths, the Stern series), [`C-0018`](../claims/C-0018-maximum-usable-bias.md) (`EquilibriumPath`, the fold as `max_s V_eq(s)`, the ceiling taxonomy), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (placement on the secant, stability on the tangent, the mandate as an equality on a SUM), [`C-0046`](../claims/C-0046-fewer-longer-flexures.md) (the `P10` placement `k_c = 10 pN/nm` and the `δ ≤ F/\|k_eff\|` cap), [`C-0050`](../claims/C-0050-desired-stroke-reach.md) (`s < L₀`), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md)/[`C-0011`](../claims/C-0011-scf-density-profile.md) (the six layer models and their validity ranges), [`C-0002`](../claims/C-0002-peg-material-parameters.md) (the PEG material sheet and the `φ = 0.2` crossover), [`C-0005`](../claims/C-0005-mean-field-screening-validity.md) (the 1.46 nm correlation band, the one-loop error) |

---

## Formulate

### The question, and why it exists

NDI answered decision **4** with *"2 devices"*.
That makes §3's **desired** clause — 100 pN at ~10 nm of stroke — **its own device** rather than a stretch goal read on the *acceptable* clause's coupling.
`C-0017`'s placement arithmetic then gives device B `k_c = 100 pN / 10 nm = 10 pN/nm` exactly,
and `C-0046` shows a hinge-and-arm flexure that builds it (arms of 12.7–18.1 nm at 10–56 paths).
`C-0046` also **refuses** it: `C-0017`'s stability floor is `|k_eff| = 23.41–27.91 pN/nm`, so 10 pN/nm is **2.34–2.79× below it**,
and the composition of the two clauses caps the stroke at `δ ≤ F/|k_eff|` = **3.58–4.27 nm**.

That refusal is read **at the 10 nm layer in 2 mM MgCl₂**.
NDI's answer to decision **2** says that is exactly the state device B need not occupy:

> *"17-26 nm of polymer thickness is beyond the regime I've bothered to examine as the debye length of operation in 2 mM MgCl2 is only about 4 nm … an interesting regime we've been reserving, again, for low MgCl2 concentrations"*

So the corner all three answers point at is: **a 10 pN/nm coupling, on a 17–26 nm layer, at 0.5 mM.**
NDI's own objection to that corner is in the same sentence — a gap of 17–26 nm is **4.3–6.6 bulk Debye lengths** at 2 mM (`λ_D = 3.9269 nm`, `C-0008`) —
and **no claim in this repository has ever evaluated the bias at a gap taller than 15 nm under load, or the fold at one at all.**
The objection stands until this runs.

### The conventions, restated rather than inherited

- `z` is normal to the electrode, positive **away** from it; the electrode surface is `z = 0`.
- The layer is grafted at `z = 0` and the tile's bottom face rests at `z = h`, so **the electrostatic gap IS the layer height, exactly** (`C-0012`'s convention, unchanged).
- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode, and `s < L₀` identically (`C-0050`).
- **`L₀` is a FORCE-ONSET height** — the height at which the layer carries 1.0 pN over the 40 × 40 nm tile (`C-0011`, `CH-0010`).
  It is **not** a first moment; the first-moment thickness of the same layer is 1.71–2.16× smaller (`C-0077`).
  **NDI's "17-26 nm of polymer thickness" is read here in the force-onset convention**, because §3 specifies a distance between two bodies and that is the convention the window belongs in (`C-0077`).
- `F_es,z < 0` is attraction toward the electrode; `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it** (`CH-0011`).
- The **load line** `R(s)` is positive **upward**. Four are read, and the same actuator is read against all four:

  | line | `R(s)` | what it is |
  |---|---|---|
  | **free** | `0` | the unloaded tile — the reference, because a coupling can be a net *source* of the thing it is added to remove |
  | **device-B** | `10 s` | **`C-0046`'s `P10`** — §3's desired clause placed on its own arithmetic |
  | **device-A** | `33.333 s` | `C-0017`'s mandated coupling, §3's acceptable clause |
  | **dead-load** | `100 pN` | the constant-force load, which passes through the same point as device-B at `s = 10 nm` |

- A **grafting density** is *not* supplied by NDI's answer, and a layer height alone does not name a layer.
  Two explicit extrapolation rules are carried and both are labelled as extrapolations:

  | rule | `σ(L₀)` | what it holds fixed |
  |---|---|---|
  | **held-density** | `σ = 0.024 nm⁻²`, the §3 10 nm point's own | `φ(L₀) = N σ v₀/L₀`, which is **independent of `N`** at fixed `σ` for a power-law interaction — so the layer's thermodynamics is the one `C-0003` validated and **only the height leaves the range** |
  | **§3-trend** | the least-squares power law `σ = A L₀^p` through `(5, 0.092)`, `(7, 0.045)`, `(10, 0.024)` | the §3 design points' own grafting-spacing-to-height ratio |

### The acceptance predicate

Declared before the code, falsifiable both ways.

> **`P1` (the reachability bound — the cheap one, and it needs no layer).**
> The applied bias that delivers §3's **100 pN** across a tile-electrode gap of 17, 20, 23 and 26 nm, at **0.5, 1 and 2 mM** MgCl₂, on `C-0008`'s own solver and Stern series.
> Reported at **both** readings of *"such a gap"* — the **resting** height `L₀` (the blocking-force reading) and the **held** gap `L₀ − 10 nm` that device B's own duty occupies — because a quantity is not well posed without the state it is read at.
> Where the field cannot reach 100 pN, **`null` and the reason**: the largest attraction available at `CH-0007`'s 1.0 V point-ion boundary and at `T-11`'s 1.23 V electrochemical bound, beside it.
> **PASS** if every cell returns a bias or a null with the ceiling that produced it.
>
> **`P2` (which decay length, demonstrated rather than asserted).**
> `ℓ = |F_es|/|k_es|` **measured on the solve** at every one of those gaps and buffers, against all three of the lengths `CH-0004`/`C-0008` distinguish — the bulk `λ_D`, the gap counterion length, the in-layer `λ` — and against the counterion-dominance **ratio** at that gap, computed rather than transferred from the 5–10 nm value.
> **PASS** if the claim can say which length governs at 17–26 nm and show the measurement that says so.
>
> **`P3` (device B's stability and its fold).**
> `|k_eff| = |k_brush + k_es|` at the device-B operating point, and the pull-in fold `max_s V_eq(s)` under the device-B load line, at 17/20/23/26 nm × {0.5, 2 mM} × all six `C-0003` models × four load lines.
> Against `C-0017`'s stability floor `k_c > |k_eff|` at `k_c = 10 pN/nm`, and against `C-0046`'s composed cap `δ ≤ F/|k_eff|`.
> **PASS** if every state returns a floor, a margin and a named binding ceiling, or a demonstration that it has none.
>
> **`P4` (the validity statement, loudly).**
> Every layer quantity at 17–26 nm is outside every range this programme has established.
> A per-quantity table saying which range, by how far, in which direction, and **what it would take to establish it** — not a footnote.

**Falsifiers, declared before the run.**

| | what would falsify the approach |
|---|---|
| **F1** | the field reaches 100 pN at the **resting** 17–26 nm gap below 1.0 V at 0.5 mM — then NDI's objection is answered on the force clause alone and the held-gap reading is not needed |
| **F2** | `ℓ` at 17–26 nm departs from the bulk `λ_D` by more than 10 % — then this programme's *"the Debye length is three numbers"* answer applies at a tall gap and NDI's objection uses the wrong one |
| **F3** | no fold exists inside device B's own 10 nm stroke at 0.5 mM at any of the six models — then the corner is **open** and `C-0046`'s refusal does not transfer |
| **F4** | a 17–26 nm layer cannot be constructed in any of the six models — then the question is not answerable with the available methods and that is the deliverable |

**Locked units.** nm, pN, pN/nm (= 1 mN/m exactly), pN/nm² (= 1 MPa exactly), V, mM, K. `k_BT = 4.142 pN·nm` at `T = 300 K` in aqueous MgCl₂.

**Maturity.** TRL 1–3. `PASS` means model-consistent and traceable. **Nothing here is measured**, and at 17–26 nm the *layer* is not even model-supported — see `P4`.

---

## Plan

### The cheap bound, which runs first and may settle the whole task

`C-0008`'s own table already carries the shape of the answer: at 2 mM the force over the 40 × 40 nm footprint is
**−22 pN at 15 nm and 2 V**, against −109 pN at 10 nm, and the force **saturates in bias** because the compact
layer takes 88 % of 2 V. Two lines of arithmetic then predict the result before any solve:

- in the far field the force is `∝ e^{−h/λ_D}` with the bulk `λ_D` (`C-0008` measures `ℓ → 3.90 nm` at 30 nm), and
- the saturated far-field amplitude is `∝ n_bulk ∝ c`.

So dropping the buffer 4× to buy `λ_D` a factor of 2 costs the amplitude a factor of 4, and `h/λ_D` at
20 nm / 0.5 mM equals `h/λ_D` at 10 nm / 2 mM **exactly** — where the whole available force is ~109 pN.
**The predicted answer is that the reserve buys the exponent and pays for it in the prefactor, and 100 pN
at a resting 17–26 nm gap is unreachable in both buffers.**
That is a prediction, not a result; it costs 24 solves to check, and it runs before the 288-fold sweep.

The bound that makes the sweep worth running anyway is the other reading of the same clause: **§3 asks for
100 pN *at the stroke*, not at the resting height**, and device B held at `L₀ − 10 nm` sits at a 7–16 nm gap,
which `C-0008` has already sampled. The device-B question is therefore not *"can the field reach across
26 nm"* but *"can the equilibrium path get from `s = 0` to `s = 10 nm` without folding"* — and the field
strengthens by more than a decade along that path, which is the most destabilising configuration in the
programme.

### The method, and why this one

**Re-run, do not tabulate.** `EquilibriumPath` (`C-0018`) is parametrised by the **diffuse-layer drop**, so
one Poisson-Boltzmann solve yields the force *and* the applied bias that produced it. Going the other way
costs 34 solves of Stern-series inversion per force evaluation — a factor of ~35 on a 288-fold sweep, which
is the difference between fifteen minutes and nine hours. The applied-bias direction is used only where the
bias is the **given**: the finite-difference `k_es` at the fold, and `P1`'s reachability ceilings.

**A pull-in bias cannot be bisected for.** It is a discontinuity in the bias and a smooth maximum in the
stroke, so the fold is `max_s V_eq(s)`, located by a coarse scan for the first descent and golden section
inside that bracket, exiting on the **bracket width**. The tangency identity `k_c + k_eff = 0` at an
**interior** maximum is the independent grading route; at a **boundary** maximum it does not hold and no
residual is reported rather than a meaningless one (`C-0018`).

**Two grafting-density rules, not one.** NDI's answer names a thickness and not a layer. Reporting one
extrapolation would be choosing a design the specification does not contain.

### What would falsify this approach

Declared above as `F1`–`F4`. `F4` in particular is a real outcome and not a hedge: if `chainLengthForHeight`
cannot invert a 26 nm height in any of the six models, the honest deliverable is *"the layer models cannot
honestly reach 17–26 nm, and here is what it would take"* — which `SESSION-PROMPT.md` names as a valid answer.

### Cost

`P1`/`P2` are ~24 gaps × buffers at ~150 solves each — under a minute.
`P3` is 4 heights × 2 density rules × 6 models × 2 buffers × 4 load lines = **384 fold searches**;
`T-4` ran 162 in ~7 minutes on the same machinery, so ~17 minutes.
The convergence axes re-run a handful of those. Smoke-run at toy settings first, per `CLAUDE.md`.

---

## Execute

`src/main/kotlin/actuator/TallGapDeviceB.kt` (the library) and
`src/main/kotlin/actuator/TallGapDeviceBStudy.kt` (the entry point), every study-local type prefixed
`TallGapDeviceB`. Tests in `src/test/kotlin/actuator/TallGapDeviceBTest.kt`, written first.

```shell
tools/study.sh actuator.TallGapDeviceBStudyKt
```

Emits `gpd/results/T-192-device-b-tall-gap.json`.

---

## Verify

The five gates, as executable tests.

1. **Dimensional consistency** — `ℓ = |F|/|k_es|` in nm recovered two ways; `λ_D` from `I = ½Σc_i z_i² = 3c`;
   the counterion-dominance ratio dimensionless and reproduced from its own definition; the placement
   identity `k_c = F/δ` exact in pN/nm.
2. **Limiting cases** — `ℓ → λ_D` in the far field; `holdingBias` returns `null` above the field's own
   ceiling rather than a clamped number; the held-density rule at `L₀ = 10 nm` reproduces the §3 design
   point's chain to the solver's own tolerance; a zero-stiffness device-B line is the free line at `s = 0`.
3. **Symmetry and conservation** — the balance residual `|attraction − load|` at every located branch point;
   the tangency residual `k_c + k_eff` at every **interior** fold, scaled by the three stiffnesses that make
   it up; `φ = N σ v₀/h` recovered identically.
4. **Numerical convergence** — mesh nodes 1000/2000/4000 **at a tall gap**, where the graded mesh has the
   most work to do; fold stroke tolerance 1e−3/1e−4/1e−6; fold coarse steps 8/12/24. Departures emitted at
   **two significant digits** (`C-0093`).
5. **Literature cross-check** — `C-0008`'s `λ_D = 3.9269 nm` at 2 mM and its 0.679 V for 100 pN at 10 nm;
   `C-0018`'s *"no fold at all at 10 nm / 0.5 mM"*; and the premises of the invoked scaling laws checked
   against PEG in water with Mg²⁺ **at the tall layer's own working volume fraction** — `N_K` against the
   thermal blob `g_T`, the swelling `α`, the coil overlap `Σ`, and whether the des Cloizeaux window is
   non-empty (`√(N_K/g_T)`).

---

## File — the outcome

Claim [`C-0110`](../claims/C-0110-device-b-tall-gap.md), result `gpd/results/T-192-device-b-tall-gap.json`.
Challenges [`CH-0126`](../challenges/CH-0126-a-tall-layer-breaks-section-3s-own-effort-point.md) and
[`CH-0127`](../challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md), both against `C-0050`.

**`P1`–`P4` all PASS. The corner is empty.** §3's 100 pN stops arriving across a gap of **13.6989 nm at
0.5 mM**, 11.8724 at 1 mM and 10.1299 at 2 mM, so a 17–26 nm layer reaches it at **0 of 12** cells; device B
is admitted at **1 of 96** states in **1 of 6** layer models; and §3's *acceptable* clause is refused at
**96 of 96**. The **uncoupled** tile reaches a 10 nm stroke at 52 of 96 — the escape is real in displacement
and empty in force.

### The declared falsifiers, scored

| | declared | outcome |
|---|---|---|
| **F1** | the field reaches 100 pN at a resting 17–26 nm gap below 1.0 V at 0.5 mM | **did not fire** — 0 of 4 heights; the best is 49.967 pN at 17 nm |
| **F2** | `ℓ` departs from the bulk `λ_D` by more than 10 % | **FIRED, and adversely.** 0.910–0.983 at 2 mM (NDI's number is right) but **0.649–0.819 at 0.5 mM** — the reserve makes NDI's estimate optimistic, not conservative |
| **F3** | no fold exists inside device B's 10 nm stroke at 0.5 mM at any model | **did not fire** — 20 of 96 device-B states fold, all at 5.20–7.70 nm, i.e. 0.52–0.77 of the demanded stroke |
| **F4** | a 17–26 nm layer cannot be constructed in any of the six models | **did not fire** — all six construct at both density rules, at 28–80 kDa chains |

### And the Plan's own prediction was wrong

The Plan predicted that dropping 2 mM → 0.5 mM would *"buy the exponent and pay for it in the prefactor"*.
Measured, the net gain at 17–26 nm is **+4.10× to +9.56×**: the exponential wins outright. The prediction is
recorded as falsified; it did not change the verdict, because the shortfall is 2.00–9.46× either way.
