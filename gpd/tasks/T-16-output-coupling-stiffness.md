# T-16 — The minimum output-coupling stiffness, and what a DNA-origami lever can actually deliver

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A2.2` (the operating point the budget is written at) and `A1.1`/`A1.2` (the allowables and the lateral bound the same anchors must also satisfy) |
| **Verification type** | **in-silico** (the coupled force balance of `C-0012` re-solved against a *load line* rather than against zero, on a refined bias grid) **+ logical** (a load-line argument that fixes the required stiffness from §3 alone, before any solve) |
| **Raised by** | [`C-0016`](../claims/C-0016-design-window.md) — *"the constraint that decides it is the output coupling. `T-16`. It is cheap, it is unstarted, and it is the single number that decides whether Gen-1 has a design window at all"* — and by [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md), which says the two questions are the same question |
| **Consumes** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) (the characteristic, re-run not tabulated), [`C-0014`](../claims/C-0014-lateral-confinement.md) (the element mechanics, the convexity theorem, the lateral and yaw bounds), [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (45 attachments as 3 × 15, the exact-zero load path), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) (the concentration factor, the 56 crossovers, the *uncited* vertical crossover constraint), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the per-path allowables), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md)/[`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the layer and the field, consumed as libraries) |

---

## Formulate

### The question, restated so that it is answerable

`C-0012` reports a table headed *"the number an output coupling has to supply"*:

> 0 at 5 nm; 11.2 pN/nm at 7 nm / 0.10 V rising to 85.6–276.6 at 0.25 V;
> 5.3–16.0 pN/nm at 10 nm / 0.10 V rising to 47.6–71.5 at 0.25 V.

Every entry is `|k_eff|` at the held gap `L₀ − 3 nm` — a **stability** threshold.
A stability threshold is a *lower* bound on the coupling's stiffness and says nothing about **where the equilibrium sits**.
Those are two different requirements and a coupling has to meet both, so the task is formulated on both from the start.

**The geometry and the sign convention, restated rather than inherited.**

- `z` is normal to the electrode, positive **away** from it; the electrode surface is `z = 0`.
- The layer is grafted at `z = 0`; the tile's bottom face rests at `z = h`, so **the electrostatic gap is the layer height, exactly** (`C-0012`'s convention, unchanged).
- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.
- The actuator's **characteristic** is `W(s) = |F_es(L₀−s, V)| − P(L₀−s)·A` — the force left over for an external load — and `dW/ds = −k_eff` exactly.
- The **output coupling** is whatever the tile pushes against: the lever, its joints, and every anchor in parallel with them. Its **reaction** `R(s)` is positive **upward**, i.e. resisting descent. `k_c ≡ dR/ds`.
- **`L₀` is a FORCE-ONSET height** — the height at which the layer carries 1.0 pN over the 40 × 40 nm tile (`C-0011`, and `CH-0010`'s requirement that this is stated in Formulate).

### The two conditions, and the one §3 fixes on its own

**(A) Placement.** The operating point is the first root of `W(s) = R(s)`. A coupling does not merely have to be stiff enough; it has to be the *right* stiffness, because it is the load line the characteristic is read against.

**(B) Stability.** `d/ds[R(s) − W(s)] > 0` at that root, i.e. **`k_c > −k_eff = |k_eff|` where `k_eff < 0`.** This is `C-0012`'s condition, and it is only half of the requirement.

Take the coupling linear over the stroke, `R(s) = R₀ + k_c s`. The **force delivered to the load between the unbiased and the biased state** is `R(s₁) − R(s₀) = k_c (s₁ − s₀) = k_c δ`, whatever the preload `R₀` is. So §3's own two numbers fix the stiffness **without any physics at all**:

> &nbsp;&nbsp;&nbsp;&nbsp;**`k_c* = F_target/δ_target = 100 pN / 3 nm = 33.333… pN/nm`, exactly, and preload-free.**

That is the cheap bound, and it is a *dimensional* statement, not a model result.
It is also **the whole of condition (A)**: a coupling of that stiffness, unpreloaded, puts the operating point at exactly 3 nm at the bias where `W(3 nm) = 100 pN` — which is `C-0012`'s own `biasForSimultaneousTarget`, already computed and already inside `CH-0007`'s trusted range.

The task therefore collapses to **one comparison**:

> &nbsp;&nbsp;&nbsp;&nbsp;**Is `|k_eff|` at `h = L₀ − 3 nm`, evaluated at the bias where `W(3 nm) = 100 pN`, smaller than 33.333 pN/nm?**

`C-0012` never evaluated it there. Its bias grid is `{…, 0.10, 0.25, …}` with **no sample in between** (its own open question 5), and the simultaneous-target bias is **0.082–0.155 V at 7 nm and 0.134–0.192 V at 10 nm** — i.e. *between the two grid points its coupling table is quoted at*. **Neither 0.10 V nor 0.25 V is an operating bias**, so neither column of that table is the requirement.

### The acceptance predicate

Declared before the code, falsifiable both ways.

> **`P1` (the requirement).** At each of §3's three layer heights, at 0.5 / 1 / 2 mM MgCl₂ and under all six of `C-0003`'s layer models, report `|k_eff(L₀ − 3 nm)|` at the bias `V*` where `W(3 nm) = 100 pN`, and the **coupling window** `k_c ∈ [|k_eff|, ∞)` intersected with the placement condition. **PASS** if the window at 7 and 10 nm is decided — i.e. every (height, model, buffer) triple returns either a stiffness the coupling must exceed, or a demonstration that no `k_c` works. Falsified as an *approach* if the six-model bracket straddles `k_c*` so widely that no verdict is model-independent at any height.
>
> **`P2` (the supply).** A DNA-origami output coupling is **identified** that supplies the `P1` stiffness at the tile in the 10 nm configuration, with (i) its per-load-path force checked against `C-0006`'s 10 pN unzip / 48–65 pN shear allowables, (ii) its attachment count stated against `C-0015`'s 45-as-3×15 flatness requirement and `C-0009`'s 56 crossovers, and (iii) the stroke it actually delivers computed as a root of `W(s) = R(s)`, never as a force over a stiffness — **or** it is shown that no coupling compatible with §3 does so, **naming the binding constraint**.
>
> **`P3` (the geometry conflict).** Report whether normal stabilisation and `C-0014`'s lateral confinement want the **same** anchors or **opposite** ones, quantitatively: the lateral stiffness the `P2` scheme supplies as a by-product, against `C-0014`'s 0.4602 pN/nm per-coordinate bound and 368.173 pN·nm/rad yaw bound.
>
> **`P4` (the `T-9` dependency).** State explicitly whether the answer rests on the crossover's **vertical/axial** compliance, which `C-0009` models as a rigid constraint with nothing cited behind it. If it does, `T-9` gates the programme and that is the finding.

**Locked units.** nm, pN, pN/nm (= 1 mN/m exactly), pN·nm and k_BT, pN/nm² (= 1 MPa exactly), V, mM, Hz, K. `k_BT = 4.142 pN·nm` at `T = 300 K` in aqueous MgCl₂.

**Maturity.** TRL 1–3. `PASS` means model-consistent and traceable. **Nothing here is measured**, and no scheme below has been built or expressed as a sequence design.

---

## Plan

### Method, and why this one

**Step 0 — the load-line argument, before any code.** `k_c* = 100/3` is arithmetic on §3's own table. It costs nothing, it is preload-free, and it replaces the question *"how stiff can a DNA lever be?"* — which has no upper bound worth computing, since a duplex in tension is `S/L = 110 pN/nm` at 10 nm and forty-five of them are 4950 — with the question *"can a DNA lever be made **compliant enough**, and is 33.3 pN/nm enough to stabilise?"* **Running this first inverts the task**, and that inversion is the justification against cost: the expensive search over stiff origami levers is not worth running, because the answer is known to be "yes, far too stiff" before it starts.

**Step 1 — the requirement, at the bias the device actually uses.** `C-0012`'s pipeline is re-run, not tabulated: the same `PoissonBoltzmannGap` solver, the same graded gap grid, the same six `C-0003` models, on a **refined bias grid** covering the gap in `C-0012`'s own sampling. Then bisect on bias for `W(3 nm) = 100 pN` and read `k_brush`, `k_es` and `k_eff` at the held gap there.

*Why not read it off `C-0012`'s file?* Because the answer sits between its two samples and the whole point of the task is that the interval was never sampled. Reading it off would be interpolating the quantity the verdict turns on.

*Why not a finer physics?* The mean-field error `C-0005` reports (123–214 % of the leading term) is one to two orders of magnitude larger than the margin this task will find. **A better Poisson-Boltzmann solve buys nothing here and the claim must say so.**

**Step 2 — the supply side, as a series/parallel element budget.** Leaf `A8.2` asks for the *dominant compliance term* and a *stiffness budget at the joints*. The coupling is a series chain — tile → attachment → spacer → lever arm → fulcrum joint → substrate — replicated over `n` parallel attachments. Every element has a closed form already verified in `C-0014`'s `anchoring/` package (`rodAxialStiffness`, `beamTransverseStiffness`, `bundleBendingRigidity`, `FreelyJointedChain`), which is **consumed as a library and not edited**. The series composition is the cheap calculation and the softest element is the answer.

**Step 3 — the stroke each scheme actually delivers.** For each scheme, solve `W(s) = R(s)` for the first root, with `R` the scheme's own (possibly nonlinear) reaction. **Never `F/k`** — `C-0012`'s standing rule, and three of six layer models have exactly zero stiffness at `L₀`.

**Step 4 — the by-products.** The convexity theorem of `C-0014` is applied *in the direction this task needs*: `k_lat/k_norm ≤ 1` means a coupling bought for normal stiffness supplies **at most** as much lateral stiffness, and the question is whether "at most 33.3 pN/nm" clears a 0.4602 pN/nm bound. Yaw follows from `Σ k_i r²` on `C-0015`'s own 3 × 15 grid.

### Cheap bound before expensive calculation — and the closed form that makes the solve a check

Before the sweep, the stability margin has a closed form at the held gap `g = L₀ − 3`:

&nbsp;&nbsp;&nbsp;&nbsp;`k_c* − |k_eff(g)| = k_c* − |F_es(g,V)|/ℓ(g,V) + k_brush(g)`

with `ℓ = |F_es|/|k_es|` the force's own decay length (`C-0008`: 1.8–2.8 nm at the working gap).
Because `|F_es(g,V*)| = 100 + P(g)A` is fixed by §3's force target, **the whole margin is bias-free once the target is imposed**, and it depends on the bias only through `ℓ`. That is the limiting-case check the numeric solve is graded against.

### What would falsify this approach — declared in advance

1. **The load-line reduction is wrong** — i.e. the first root of `W(s) = k_c s` at `V*` is not at `s = 3 nm`, or `dW/ds` there is not `−k_eff`. Either would mean the characteristic is not the object `C-0012` says it is.
2. **`|k_eff|` at `V*` exceeds 33.33 pN/nm at 10 nm.** Then no §3-compliant unpreloaded linear coupling is simultaneously placed and stable, `C-0016`'s `P2` closes **empty** at 10 nm, and — with 5 nm empty by 13.3× already — the Gen-1 stack has no design window. **This is the outcome the task exists to detect and it must not be argued away.**
3. **A six-model bracket that straddles 33.33 pN/nm.** Then the verdict is model-dependent and the task delivers a threshold on the layer model instead of an answer.
4. **The margin, whatever its sign, smaller than the uncertainty it is quoted against.** `C-0005`'s one-loop correction is 123–214 %. A 10 % margin is not a verdict, and reporting one as if it were would be the failure mode `C-0016` warns about — *"a design window is exactly the artifact a reader mistakes for a recommendation"*.
5. **A scheme meeting the stiffness but failing an allowable**, i.e. `P2` passing on stiffness and failing on strength — which is `C-0014`'s falsifier 2 recurring in the normal direction.
6. **The dominant compliance turning out to be the crossover's vertical link**, which `C-0009` does not model and `T-9` has not produced. Then `P4` fires and `T-9` gates the programme.

### What is deliberately *not* done

- **No oxDNA, no MD.** That is `T-9`, costed at days, and it needs the coordinator's go-ahead. Where an answer would need it, `P4` says so instead of guessing.
- **No 2-D field solve.** `T-3b` owns the lateral load profile; this task inherits `C-0012`'s 1-D tile mean and says so.
- **No new layer physics.** `C-0003`'s six models are consumed unchanged, and `C-0011`'s solved profile is *not* substituted, because `C-0012`'s characteristic — the object this task loads — was computed on `C-0003`. Mixing them would compare a load line against a different curve.

---

## Execute

Package `src/main/kotlin/coupling/`, owned by this task; `anchoring/`, `actuator/`, `electrostatics/`, `brush/`, `material/` and `structure/` are consumed as libraries and **not edited**.

| file | what it holds |
|---|---|
| `CouplingRequirement.kt` | the load line, the two conditions, the mandated stiffness, the coupling window, the preload relation, and the operating stroke as a **root** |
| `CouplingElement.kt` | series/parallel composition, the axial link, the ssDNA spacer, the lever ratio, the fulcrum joint, and the compliance share of each |
| `CouplingScheme.kt` | the candidate couplings, each evaluated for stiffness, per-path force, attachment count, delivered stroke, lateral and yaw by-product |
| `CoupledOperatingPoint.kt` | the readers `C-0012` is consumed through — its 810 operating points **and** its 90 threshold records |
| `OutputCouplingStudy.kt` | the `main`, emitting `gpd/results/T-16-output-coupling-stiffness.json` |
| `CouplingResultRounding.kt` | the serialisation-boundary rounding, per `gpd/README.md` and `CLAUDE.md` |

Tests in `src/test/kotlin/coupling/`, each named for the gate it discharges: **39 of them**, in four classes
(`CouplingRequirementTest` 13, `CouplingElementTest` 12, `CoupledCharacteristicTest` 7, `CouplingSchemeTest` 7).

Run with:

```shell
./gradlew study -Pstudy=coupling.OutputCouplingStudyKt -PbuildDirectory=build-t16
```

### What the study emits

54 requirement records (3 heights × 6 models × 3 buffers), 324 scheme records (6 candidates × 54 states),
36 upstream reproductions, 9 spacer designs, 6 lever budgets, 6 convergence records. Run time ~4 minutes.

### What was corrected mid-task, and why

The Verify section below was **found already written, with every row marked `PASS`, before the study had ever
been run** — the failure mode `JOURNAL.md` records `T-2` for and the one this loop exists to prevent. Every
row was re-derived against an actual run. Three rows were **wrong** and are corrected in place with a note;
one is downgraded from a numerical agreement to an identity; the rest stand. Two defects in the study itself
were found in the process:

1. **The convergence records referred both axes to one reference.** The Poisson-Boltzmann mesh departures were
   computed against the *144-sample* margin, which folds the sampling error into the mesh axis and reports a
   convergence the mesh has not demonstrated. Each axis is now referred to **its own** finest setting.
2. **`C-0012`'s own `biasForSimultaneousTarget` was never compared against the located root.** It is a
   `firstCrossing` **interpolation** on `C-0012`'s bias grid, and at 10 nm that grid interval is `[0.1, 0.25]`
   — a 2.5× span. The comparison is now emitted at every state, and it is up to **6.1 %**. That number is the
   size of the effect `CH-0016` is about, so leaving it uncomputed would have left the challenge unquantified.

---

## Verify

*Written after the code ran, from the run. Gate outcomes and falsifier results are as they occurred, including the two falsifiers that fired and the three rows that were wrong.*

**Authoritative suite:** `tools/verify.sh` on the working tree — **BUILD SUCCESSFUL, 685 tests, 0 failures**,
of which 39 are this task's.

### Gate 1 — dimensional consistency

| check | outcome |
|---|---|
| `F/δ` is a stiffness: 100 pN / 3 nm = 33.333… pN/nm, asserted to `1e−12` | **PASS** |
| a load line `R = k_c s` has the units of `W(s)`, and `R − W` vanishes at the root | **PASS** |
| ~~`dW/ds = −k_eff` exactly, by finite difference against the solver's own `k_brush + k_es`, to `1e−4` relative~~ | **CORRECTED — this row overclaimed.** No finite-difference test of the *solver's* characteristic exists. `dW/ds = −k_eff` is an **identity of the construction** (`W(s) = \|F_es(L₀−s)\| − P(L₀−s)A`, differentiated), and what *is* asserted is `k_eff = k_brush + k_es` at every record in `C-0012`'s own file, to `1e−6`. The identity is stated as such rather than measured |
| series compliance adds and parallel stiffness adds; a single element reduces to itself | **PASS** |
| a compliance **share** is dimensionless and the shares sum to 1 | **PASS** |
| `k_yaw = Σ k_i r²` is a stiffness times a squared length | **PASS** |
| unphysical arguments (negative stiffness, zero count, empty chain) throw rather than returning a number | **PASS** |

### Gate 2 — limiting cases

| check | outcome |
|---|---|
| an infinitely stiff coupling delivers **zero** stroke, and a zero-stiffness coupling delivers the **free** stroke, both to `1e−6` | **PASS** |
| the delivered stroke is **monotone decreasing** in `k_c`, over a decade sweep `1 → 300 pN/nm` | **PASS** — on a synthetic characteristic. It is **not** asserted "at every height"; the study's own 54 states show the same ordering but that is data, not a test |
| the preload needed to place the equilibrium at `s*` is `k_c s* − W(s*)`, zero exactly when `k_c = W(s*)/s*` | **PASS** |
| the unpreloaded placement stiffness puts the root **exactly** at the target, to `1e−7` | **PASS** — and reproduced by the study at all 54 solved states: `2.999984 – 3.000001 nm` |
| a load line softer than the characteristic's own slope never crosses it, and returns `null` rather than a number | **PASS** |
| a series chain is softer than its softest element, and never softer than half of it when the other is equal | **PASS** |
| the FJC spacer reduces to `3k_BT/(L_c b)` at vanishing tension and stiffens without bound toward the contour | **PASS** |
| a coupling designed for §3's force target delivers it to `1e−7`, its **secant** equals the mandate and its **tangent exceeds** it | **PASS** — and this is the mechanism `C-0017` turns on |
| ~~the closed-form margin `k_c* + k_brush(g) − \|F_es(g)\|/ℓ(g)` reproduces the solved margin to `1e−6`~~ | **DOWNGRADED — it is an algebraic identity, not a numerical check.** `ElectrostaticForceCurve` defines `k_es = \|F\| d ln\|F\|/dh` and `ℓ = −1/(d ln\|F\|/dh)`, so `\|F\|/ℓ ≡ −k_es` in floating point. The study reports a departure of **exactly 0.0** at all 54 states, which is what an identity does. It verifies that the two accessors are sign-consistent and **nothing else**, and reporting it as agreement would have been an unearned `PASS` |

### Gate 3 — symmetry and conservation

| check | outcome |
|---|---|
| the delivered force `R(s₁) − R(s₀) = k_c(s₁ − s₀)` is **independent of the preload**, asserted over five preloads spanning ±200 pN, to `1e−12` | **PASS** |
| the window is empty **exactly** when the chord is flatter than the tangent, checked on `C-0012`'s own 10 nm / 0.25 V record | **PASS** |
| `k_eff = k_brush + k_es` at every record in `C-0012`'s file, to `1e−6` | **PASS** |
| the convexity bound `k_lat ≤ k_norm` holds for every spacer state the design visits | **PASS** |
| the yaw-to-lateral ratio is the **mean squared radius** of `C-0015`'s own 3 × 15 grid, exactly | **PASS** |
| ~~the balance residual `\|W(s*) − R(s*)\|` is below `1e−9` of `W` at every solved point~~ | **STRUCK — no such assertion exists.** `firstOperatingStroke` exits on the **bracket width** (`1e−14` relative), never on a residual, which is `CLAUDE.md`'s own rule; a residual bound is therefore not the exit criterion and was never checked. What is asserted instead is scan-independence to `1e−9` between 64 and 8192 scan steps |
| ~~the per-anchor thermal force `√(k_BT k)/n` is checked against `C-0014`'s own table~~ | **CORRECTED — it is checked against its own scaling, not against `C-0014`'s table.** The test asserts `√(k_BT k)/n` grows as the square root of the stiffness and falls as `1/n`. `C-0014`'s tabulated per-anchor forces are **cited**, not reproduced |

### Gate 4 — numerical convergence

| check | outcome |
|---|---|
| ~~force-curve samples 36 → 72 → 144: the requirement moves by < 1e−5 relative~~ | **WRONG AS WRITTEN, and corrected: the departure at 36 samples is `4.0e−4`, not < `1e−5`.** The ladder is 36 → `4.0e−4`, 72 → `7.2e−6`, 144 → reference. The study's own value, 72, is converged to `7.2e−6`; **36 is not**, and the row now says so. The margin this moves — 1.39109 against 1.39165 — changes no verdict |
| Poisson-Boltzmann mesh 2000 → 4000 → 8000 nodes | **PASS, at `7.3e−6` then `1.5e−6`** — after the reference was corrected to **this axis's own** finest setting. As originally coded both axes were referred to the 144-sample margin, which understated the 4000-node departure by 5× and is the defect described in Execute |
| the bias bisection exits on **bracket width**, never on a residual (`CLAUDE.md`) | **PASS**, by construction in `biasForForce` and `firstOperatingStroke`, and asserted for the latter as scan-independence to `1e−9` over 64 → 8192 steps |
| ~~the result file is byte-identical on two independent re-runs~~ | **NOW TRUE, AND IT WAS UNBACKED WHEN WRITTEN.** The study had never been run when this row was marked `PASS`. It has now been run twice from a clean invocation and `diff` reports the files **identical**. The `argmin` `CLAUDE.md` warns about is `dominantCompliance`, which compares **already-rounded** compliances with the first index winning ties |

### Gate 5 — literature and upstream cross-check

| check | outcome |
|---|---|
| `C-0012`'s blocking forces at 2 mM reproduced from the re-run solver: 167.2 / 86.7 / 34.5 pN at 0.10 V and 490.4 / 214.7 / 73.6 pN at 0.25 V | **PASS** |
| `C-0012`'s `W(3 nm)` and `k_eff(3 nm)` reproduced at both grid biases | **PASS — worst relative departure `3.82e−9` over all 36 comparisons**, because the same solver was re-run rather than a table copied |
| `C-0012`'s coupling table (5.31–15.99 at 10 nm / 0.10 V; 47.63–71.54 at 0.25 V; 85.57–276.58 at 7 nm / 0.25 V) reproduced as `\|k_eff\|`, confirming that what is challenged is its **scope**, not its arithmetic | **PASS** |
| **new:** `C-0012`'s `biasForSimultaneousTarget` is a `firstCrossing` **interpolation**, and at 10 nm all six models carry its own bracket string `[0.1, 0.25]` | **PASS** — and the located root departs from it by up to **6.1 %** |
| **new:** at 7 nm exactly **one** of the six models puts that crossing below 0.10 V | **PASS** — the first draft of `CH-0016` said *two*, and that is struck there |
| ~~`C-0014`'s `rodAxialStiffness` at 10 nm = 110 pN/nm and its ssDNA design rule reproduced from `anchoring/`~~ | **HALF CORRECT.** The ssDNA rule **is** reproduced: `gaussianContourCeiling` gives 103.4 nm to 0.6 % and on the correct side (below, because `C-0014` solved the full chain and it strain-stiffens). `rodAxialStiffness(1100, 10) = 110 pN/nm` is **used**, not separately asserted here — it is `C-0014`'s own tested function, consumed unchanged |
| `C-0015`'s 45-as-3×15 and `C-0009`'s 56 crossovers used as the attachment budget | **APPLIED, not verified** — this is a consumption of two claims, and it is labelled as such rather than as a check |
| `C-0006`'s 10 / 48 / 65 pN per-path allowables applied, **not** §4(f)'s 35–60 pN whole-cross-section band | **APPLIED, not verified**, and stated in `C-0017`'s cited-inputs table |

### The declared falsifiers, and what actually happened

| # | fired? | outcome |
|---|---|---|
| 1 — the load-line reduction wrong | **no** | the **first** root of `W(s) = 33.333 s` at `V*` is at `3.000000 nm` (spread `2.999984 – 3.000001` over 54 states), and `dW/ds = −k_eff` is the identity the characteristic is built on |
| 2 — `\|k_eff\| > 33.33` at 10 nm | **no, and it is close** | the worst floor in the box is **27.91 pN/nm** — 10 nm, 2 mM, alexander-box(two-body) — a margin of **1.19×**. At 0.5 mM the worst is 15.94, a margin of 2.09× |
| 3 — a straddling six-model bracket | **no** | at every one of the nine `(height, buffer)` pairs all six models fall on the **same** side of 33.333 |
| 4 — margin smaller than its own uncertainty | **YES** | the 10 nm margin at 2 mM is 19–42 % against an inherited mean-field error of 123–214 % (`C-0005`). Reported as **NOT EXCLUDED**, never as established. Only the 0.5 mM margin (2.09–8.65×) clears its own uncertainty |
| 5 — a scheme passing stiffness and failing an allowable | **YES, for the concentrated schemes** | `K5` (440 pN/nm, 190 pN per path) and `K6` (73.3 pN/nm, **760 pN** per path) clear the stability floor and break the 65 pN nicked-duplex ceiling by 2.9× and 11.7×. Both also fail placement, so the allowable is not what excludes them — but the falsifier fired as written |
| 6 — the crossover's vertical compliance dominating | **partly** | it does not gate a *distributed* coupling matched one row per duplex (`C-0015`'s exact zero) and it does gate every *concentrated* one. **`T-9` does not gate the programme; it gates a topology already excluded on three other grounds** |

### What a re-run would have to reproduce

`gpd/results/T-16-output-coupling-stiffness.json`, byte-for-byte, from
`./gradlew study -Pstudy=coupling.OutputCouplingStudyKt -PbuildDirectory=build-t16`.
Headline numbers, all at the located operating bias: stability floor **0 / 0 / 23.41–27.91 pN/nm** at
5 / 7 / 10 nm at 2 mM; mandated stiffness **33.333**; `K2`'s secant **33.333** and tangent **39.010**;
`K2`'s spacer contour **8.612 nm = 13.2 nt** at `b = 2.10 nm`; per-path force **2.222 pN**.

Full result: [`C-0017`](../claims/C-0017-output-coupling-stiffness.md).
Challenge raised: [`CH-0016`](../challenges/CH-0016-coupling-requirement-is-quoted-off-operating-point.md).
