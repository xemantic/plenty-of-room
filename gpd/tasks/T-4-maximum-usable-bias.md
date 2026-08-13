# T-4 — Electrostatic softening and pull-in: the maximum usable bias, and the three ceilings it is made of

| | |
|---|---|
| **Leaf** | **`A2.2`** (*"stroke and blocking force versus bias, including ionic screening"* — the bias axis of it), with `A7.4` (the field the softening comes from) |
| **Verification type** | **in-silico** (the equilibrium path of `C-0012`'s coupled balance parametrised by the **stroke**, its fold located as the maximum of the bias along it, and graded against the tangency condition `k_c + k_eff = 0` computed independently by finite difference at fixed applied bias) **+ logical** (which ceiling belongs to which load line, before any solve) |
| **Re-formulated by** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) — *"the question is no longer whether `k_eff` reaches zero: it does"* — and by [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md), [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md) and [`CH-0016`](../challenges/CH-0016-coupling-requirement-is-quoted-off-operating-point.md) |
| **Consumes** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) (the characteristic, **re-run not tabulated**), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the coupling the device carries, 33.333 pN/nm), [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the field and its solver), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (the six layer models), [`C-0005`](../claims/C-0005-mean-field-screening-validity.md) (the correlation band and the one-loop error), [`C-0002`](../claims/C-0002-peg-material-parameters.md) (the concentrated crossover) |

---

## Formulate

### What the question is now, and what it is not

§6 task 4 asks for *"either a maximum usable bias with margin to the operating point, or a demonstration that the osmotic divergence removes the instability"*.

`C-0012` has already answered the *whether*: `k_eff = k_brush + k_es` **does** reach zero, at the predicate's own operating point, crossing between 0.05 and 0.10 V at 10 nm and between 0.10 and 0.25 V at 7 nm.
So the deliverable is the **number**, and three separate things have been called "the maximum usable bias" in this programme without being distinguished:

1. a **static-stability** ceiling — the bias at which the held equilibrium folds;
2. an **upstream-validity** ceiling — the bias at which the operating point leaves `C-0005`'s correlation band or `C-0002`'s concentrated crossover, which `C-0012` reported at ~0.1 V;
3. an **electrochemical** ceiling — 1.23 V, `T-11`.

They are not alternatives and they are not comparable as quoted, because **the first two are properties of a `(bias, load line)` pair and were evaluated at different load lines**.
That is `CH-0015`'s point, and this task is formulated on it from the start.

### The conventions, restated rather than inherited

- `z` is normal to the electrode, positive **away** from it; the electrode surface is `z = 0`.
- The layer is grafted at `z = 0` and the tile's bottom face rests at `z = h`, so **the electrostatic gap is the layer height, exactly** (`C-0012`'s convention, unchanged).
- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.
- **`L₀` is a FORCE-ONSET height** — the height at which the layer carries 1.0 pN over the 40 × 40 nm tile (`C-0011`, `CH-0010`).
- The **load line** `R(s)` is positive **upward**. Three of them are read, and the same actuator is read against all three:

  | line | `R(s)` | what it is |
  |---|---|---|
  | **free** | `0` | the unloaded tile — `C-0012`'s own operating point, the one `CH-0015` says the device never occupies |
  | **dead-load** | `100 pN` | a constant-force load — the point `C-0012`'s `k_eff < 0` table describes |
  | **coupled** | `33.333 s` | `C-0017`'s output coupling, the device the programme has decided to build |

  The **coupled and dead-load lines pass through the same operating point** — 100 pN at 3 nm — and differ only in slope. That is the whole of the comparison.
- `F_es,z < 0` is attraction toward the electrode; `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it** (`CH-0011`). Every sign below is quoted with the gap it applies to.

### The acceptance predicate

Declared before the code, falsifiable both ways.

> **`P1` (the ceilings).** At each of §3's three layer heights, at 0.5 / 2 / 10 mM MgCl₂, under all six of `C-0003`'s layer models and on all three load lines — 162 states — report the **static-stability** ceiling (the fold of that state's own equilibrium path), the **upstream-validity** ceiling (`C-0005`'s 1.46 nm gap and `C-0002`'s `φ = 0.2`, re-read at *that* load line's operating point), and `CH-0007`'s ~1 V point-ion boundary, with the **binding** one named and the **margin** to the bias that delivers §3's own targets. **PASS** if every state returns a ceiling and a named binding constraint, or a demonstration that it has none below the model's own floor.
>
> **`P2` (the headline).** The **coupled** ceiling — `k_c + k_eff > 0` with `C-0017`'s 33.333 pN/nm, **not** `k_eff > 0` — delivered as the headline, with the **unloaded** ceiling beside it and the difference between them quantified. The 1.23 V electrochemical bound quoted and shown not to bind.
>
> **`P3` (`CH-0011`, as a test rather than as prose).** The claim that the collapse is arrested by `k_es` reversing sign, not by the osmotic divergence, settled by locating **both** stoppers and reporting which the descending tile meets first — the gap at which `|F_es|` stops growing, against the gap at which the layer alone would carry the largest attraction the field can ever exert at that bias.

**Locked units.** nm, pN, pN/nm (= 1 mN/m exactly), pN/nm² (= 1 MPa exactly), V, mM, K. `k_BT = 4.142 pN·nm` at `T = 300 K` in aqueous MgCl₂.

**Maturity.** TRL 1–3. `PASS` means model-consistent and traceable. **Nothing here is measured.**

---

## Plan

### The cheap bound, before any code

Two of the three ceilings are known before the sweep and are what make it worth running:

- **The electrochemical ceiling cannot bind.** `T-11`'s 1.23 V is above `CH-0007`'s ~1 V point-ion boundary, which is itself above every threshold in the programme (`C-0012`: all below 0.7 V). It is quoted as a bound and one line of arithmetic settles it.
- **The coupled ceiling cannot be lower than the dead-load ceiling.** `k_c + k_eff > k_eff` for any `k_c > 0`, so adding `C-0017`'s coupling can only move the fold up. The unloaded ceiling `C-0012` reported is therefore a **lower bound** on the coupled one, and the sweep is measuring how much room the coupling buys, not whether it buys any.

What is *not* cheap, and is the reason for the sweep: whether the coupled ceiling clears the **operating** bias, and by how much, at every model and buffer.

### Method, and why this one

**The fold is a discontinuity in the bias and a smooth maximum in the stroke.** Scanned in bias, the equilibrium jumps from the shallow branch to near-contact, and a bisection cannot find a discontinuity — which is why `C-0012` could only bracket it between two grid samples. Scanned in **stroke** the same object is single-valued and smooth: at each stroke there is exactly one bias that puts an equilibrium there,

&nbsp;&nbsp;&nbsp;&nbsp;`R(s) + P(L₀−s)·A = |F_es(L₀−s, V_eq(s))|`,

and the pull-in bias is `max_s V_eq(s)`. Differentiating the balance along the path at `V′(s) = 0` gives

&nbsp;&nbsp;&nbsp;&nbsp;`k_c = −dW/ds = |k_eff|`, i.e. **`k_c + k_eff = 0`**,

so the argmax *is* the tangency point, and the two routes to it — a maximum of the path and a vanishing coupled tangent — are numerically independent. The study takes the first and grades it against the second.

**The path is parametrised by the diffuse-layer drop, not by the applied bias.** `C-0008`'s applied bias is the diffuse drop plus the compact-layer drop, and recovering the drop from the bias costs 34 Poisson-Boltzmann solves of Stern-series bisection per force evaluation. Run the other way it is free: **one** solve at a given diffuse drop yields the force *and* the applied bias that produced it. Since the path wants a bias per stroke rather than a force per bias, the inversion is not needed at all — a factor of ~35, and the reason a 162-fold sweep runs in minutes instead of hours.

**Why not a finer field model.** `C-0005`'s one-loop correction is 123–214 % of the leading term across this gap range, one to two orders above every margin this task can report. A better Poisson-Boltzmann solve buys nothing here and the claim must say so rather than spend the time.

**Why `k_es` is taken from the solve.** §1's `|k_es| ≈ F_es/λ_D` understates the softening by 1.0–2.6× (`C-0008`), which is the non-conservative direction for pull-in. Every `k_es` here is either the path's own tangency condition or a central difference of the full re-solve at fixed applied bias — `C-0008`'s own method — and never the §1 form.

### What would falsify this approach — declared in advance

1. **The tangency identity fails at the located fold**, i.e. `k_c + k_eff ≠ 0` there. Then the path maximum is not the fold and the whole reduction is wrong.
2. **The coupled ceiling falls below the operating bias** at some height, buffer and model. Then §3's own mandated coupling both places the operating point and destabilises it, `C-0017`'s `P2` re-opens, and the Gen-1 stack has no usable bias at that height. **This is the outcome the task exists to detect and it must not be argued away.**
3. **The six-model bracket straddles the operating bias**, so the verdict is model-dependent and the deliverable is a threshold on the layer model rather than a number.
4. **The margin smaller than the uncertainty it is quoted against.** `C-0005`'s 123–214 % is inherited whole; a 20 % margin is not a verdict, and reporting one as if it were is the failure mode `C-0016` and `C-0017` both warn about.
5. **`CH-0011` is wrong**: the osmotic stopper sits at a *larger* gap than the electrostatic one, so the collapse is arrested by the layer and the challenge's mechanism is not the operative one.
6. **The path is not single-valued** — two biases holding the same stroke — which would mean `|F_es|` is not monotone in the bias at fixed gap and the parametrisation itself is unsound.

### What is deliberately not done

- **No dynamic pull-in.** Everything here is static. A bias step faster than drainage can carry the tile past a fold a quasi-static ramp would stop at; `C-0004`'s corner is 91 kHz–2.3 MHz, so the quasi-static reading is the right one below ~10 kHz, and that is *stated* rather than assumed.
- **No 2-D field solve** — `T-3b` owns the lateral load profile; this task inherits the 1-D tile mean.
- **No new layer physics.** `C-0003`'s six models are consumed unchanged and `C-0011`'s solved profile is deliberately *not* substituted, for `C-0017`'s reason: the load line must be drawn across the same characteristic `C-0012` computed.
- **No preloaded coupling.** That is `T-13`'s question, and it moves the fold.

---

## Execute

Package `src/main/kotlin/actuator/`, **adding** to what `T-3` built and restructuring none of it;
`brush/`, `electrostatics/`, `material/` and `coupling/` are consumed as libraries and not edited.

| file | what it holds |
|---|---|
| `PullInStability.kt` | the diffuse-parametrised field, `holdingBias`, the `EquilibriumPath` and its fold search, the force-maximum and repulsion-onset locators, and the ceiling combination with its deterministic tie-break |
| `MaximumUsableBiasStudy.kt` | the `main`, emitting `gpd/results/T-4-maximum-usable-bias.json` |

Tests in `src/test/kotlin/actuator/`: `PullInStabilityTest` (17, gate-named, on closed forms) and
`ElectrostaticStopperTest` (4, gate-named, on the real Poisson-Boltzmann solver — `CH-0011` as an executable check).

Run with:

```shell
./gradlew study -Pstudy=actuator.MaximumUsableBiasStudyKt -PbuildDirectory=build-t4
# or, at concurrency:
tools/study.sh actuator.MaximumUsableBiasStudyKt
```

### What the study emits

162 ceiling records (3 heights × 6 models × 3 buffers × 3 load lines), 18 small-gap records
(3 buffers × 6 biases), 324 arrest records, 24 upstream checks and 16 convergence records.
Run time ~7 minutes.

### What was corrected mid-task, and why

1. **The tangency residual was reported over boundary maxima as well as interior ones**, and its denominator
   collapsed for the two load lines with `k_c = 0`. The first run therefore printed a worst residual of
   `6.2e13` where the interior folds were at `9.4e−6`. A boundary maximum is not a stationary point; no
   residual is reported for one now, and their count is emitted instead.
2. **The small-gap diagnostics were read only at 0.02–0.25 V**, all of which are *below* the fold. An arrest
   is a statement about the post-fold state, so 0.5 and 1.0 V were added — which is where `CH-0011`'s claim
   had to be tested and was not.
3. **"Usable" was tested on the bias alone.** A state can have `V* ≤ ceiling` and still have its 3 nm target
   stroke **beyond the fold stroke**, which is a different failure and is what happens at 7 nm in 10 mM.
   Both tests are now applied.

---

## Verify

*Written after the code ran, from the run. Every row below was executed; the three rows that were wrong when
first drafted are corrected in place with a note, and the one that could not be checked as written is struck.*

**Authoritative suite:** `tools/verify.sh` on the working tree — **BUILD SUCCESSFUL in 1 m 49 s**, no `--drop`
needed. Counted from a local `./gradlew test -PbuildDirectory=build-t4` on the same tree: **794 tests,
0 failures**, of which **21 are this task's** (`PullInStabilityTest` 17, `ElectrostaticStopperTest` 4).

### Gate 1 — dimensional consistency

| check | outcome |
|---|---|
| a holding bias balances the load exactly: `\|F_es\|` in pN against `R + PA` in pN, to the bisection's own bracket | **PASS** |
| a branch point pairs a stroke with the gap it leaves, `s + h = L₀` in nm | **PASS** |
| `k_c + k_eff` is a stiffness in pN/nm and its residual is dimensionless | **PASS** |
| unphysical arguments — a negative gap, a negative load, a stroke ceiling above `L₀`, a coarse scan of 2 — throw rather than returning a number | **PASS** |

### Gate 2 — limiting cases

| check | outcome |
|---|---|
| the fold of an exponential field against a linear coupling is at a stroke of **exactly one decay length**, and the pull-in bias is `k_c λ e^{(L₀−λ)/λ}` — reproduced to **1e−9 relative** in the bias | **PASS** |
| the fold stroke does not move with the coupling stiffness over a decade, and the pull-in bias is exactly proportional to it | **PASS** |
| the fold stroke follows the decay length, which is what sets it (1.0 / 2.5 / 4.0 nm) | **PASS** |
| a **dead load** against a decaying field with no restoring layer is unstable from the branch start, and the ceiling is then the bias that holds it at zero stroke — the blocking bias | **PASS** |
| a fold beyond the stroke ceiling is reported as **no fold**, with the branch end quoted | **PASS** |
| a load the field cannot reach ends the branch, and that is the osmotic wall | **PASS** |
| the force maximum of a non-monotone attraction is located and its derivative vanishes there; a monotone one returns `null` rather than an endpoint | **PASS** |
| the repulsion onset is the gap where the signed force changes sign, repulsive below and attractive above | **PASS** |

### Gate 3 — symmetry and conservation

| check | outcome |
|---|---|
| **at the located fold the coupled tangent vanishes**, `k_c + k_eff = 0`, by a central difference of the field at fixed applied bias — an independent route to the same point | **PASS**, at `1e−5` of the three stiffnesses on the closed form, and **9.40e−6 worst over the study's 16 INTERIOR folds**. **CORRECTED:** the first run reported `6.2e13`, because the other 25 folds are **boundary** maxima (the dead-load line descends from zero stroke) where the derivative need not vanish and the denominator collapsed. No residual is now reported for those, and the count of them is emitted instead |
| the fold's own bias is the largest along the whole branch, asserted at 60 further strokes | **PASS** |
| the binding ceiling is the smallest one, with ties broken on declaration order and the comparison made on **rounded** values, so the file is reproducible | **PASS** |
| `V*` on the coupled line equals `V*` on the dead-load line — the two lines pass through the same point by construction | **PASS — exactly 0.0 relative departure over 48 records** |
| the tangency residual **falls** when the two brackets are tightened, which is what says the identity is exact rather than approximated | **PASS** — and it exposed that what floors the located stroke is not the golden-section bracket but the **bias bisection's** own noise: at relative bracket `t` the stroke is resolvable only to about `λ√(2t)` |

### Gate 4 — numerical convergence

All four axes are measured **on this task's own quantity** — the located pull-in bias at the 10 nm design
point, 2 mM, strong-stretching(des-Cloizeaux), coupled line — and each is referred to **its own** finest
setting, not to a shared one.

| check | outcome |
|---|---|
| Poisson-Boltzmann mesh 1000 / 2000 / 4000 / 8000 nodes | **PASS** — 6.23e−6, **1.48e−6** (the setting used), 2.94e−7, reference |
| fold coarse scan 8 / 12 / 24 / 48 steps | **PASS** — 8.12e−12, **3.13e−12**, 9.22e−11, reference |
| golden-section stroke bracket 1e−2 / 1e−3 / 1e−4 / 1e−6 nm | **PASS** — 2.48e−8, 6.43e−10, **1.33e−11**, reference |
| diffuse-drop bisection bracket 1e−6 / 1e−8 / 1e−10 / 1e−12 relative | **PASS** — 5.22e−6, 1.17e−8, **3.73e−10**, reference |
| every search exits on a **bracket width**, never on a residual (`CLAUDE.md`) | **PASS**, by construction in `holdingBias`, `EquilibriumPath.fold`, `goldenSectionMaximum` and `repulsionOnsetGap` |
| the result file is byte-identical on two independent re-runs | **PASS** — `tools/study.sh` reported **no `T-4` file changed** on the second run (it copies back only files that differ) |

### Gate 5 — literature and upstream cross-check

| check | outcome |
|---|---|
| `C-0008`'s bias for a 100 pN blocking force at 2 mM — 0.067 / 0.113 / 0.679 V at 5 / 7 / 10 nm | **PASS** — 0.0668 / 0.1128 / 0.6795, departure ≤ **2.3e−3**, which is the rounding of the published table |
| `C-0012`'s blocking force at 2 mM and 0.10 V — 167.2 / 86.7 / 34.5 pN | **PASS** — 167.17 / 86.70 / 34.46, ≤ **1.1e−3** |
| `C-0017`'s located operating bias `V*` at 2 mM, six-model bracket | **PASS** — ≤ **3.5e−3** at all six ends |
| **new:** the dead-load fold, where it sits at the branch start, **is** the blocking bias — two independent constructions | **PASS**, and it is the same three numbers above |
| `C-0005`'s 1.46 nm, `C-0002`'s `φ = 0.2`, `CH-0007`'s 1.0 V, `T-11`'s 1.23 V | **APPLIED, not verified** — consumptions of four claims, labelled as such |
| ~~`C-0012`'s free-stroke thresholds reproduced~~ | **NOT ASSERTED.** The free line's `V*` is computed here (0.114–0.340 / 0.044–0.129 / 0.024–0.079 V at 2 mM) against `C-0012`'s 0.127–0.405 / 0.045–0.147 / 0.024–0.076, i.e. up to **16 %** apart — because `C-0012`'s are `firstCrossing` **interpolations** on its bias grid, as `CH-0016` established. It is reported in the claim as a comparison, **not** as a check that passed |

### The declared falsifiers, and what actually happened

| # | fired? | outcome |
|---|---|---|
| 1 — the tangency identity fails at the fold | **no** | `9.40e−6` worst over 16 interior folds, by an independent finite difference |
| 2 — the coupled ceiling below the operating bias | **YES, at 20 of 54 states** | 15 of them the whole of 5 nm (the operating point is past `C-0002`'s `φ = 0.2` before any instability), and 5 at 7 nm / 10 mM, where the **fold stroke is 1.92–2.68 nm, shallower than §3's 3 nm**. At 10 nm the ceiling clears the operating bias at every model and buffer — but at 2 mM by only **1.007–1.032** |
| 3 — a straddling six-model bracket | **no** | at every `(height, buffer, load line)` cell all six models agree on which ceiling binds |
| 4 — the margin smaller than its own uncertainty | **YES** | 0.7–3.2 % at 10 nm / 2 mM against `C-0005`'s 123–214 %. **Reported as NOT EXCLUDED, never as established.** Only the 0.5 mM margin (1.29–2.36×) is even the size of a model spread |
| 5 — `CH-0011` wrong about the arrest | **YES** | the osmotic stopper is at a **larger** gap than the electrostatic one at **324 of 324** states, by 1.9–5×. `CH-0017` |
| 6 — the path not single-valued | **no** | `\|F_es\|` is monotone in the diffuse drop at every gap the sweep visited; the bracket check would have thrown otherwise |

Full result: [`C-0018`](../claims/C-0018-maximum-usable-bias.md). Challenge raised:
[`CH-0017`](../challenges/CH-0017-collapse-is-arrested-osmotically.md).
