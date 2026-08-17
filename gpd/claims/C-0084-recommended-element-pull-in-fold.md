# C-0084 — **The element the programme recommends has NO pull-in fold at 2 mM, at 6 of 6 layer models** — where `C-0018`'s affine mandate folds at 6 of 6 with a bias margin of 1.007–1.032 and `C-0032`'s strain-softening flexure collapses to 1.0000–1.0019. §6 task 4 is now discharged **for the recommended device**, `CH-0083` is **RESOLVED**, `C-0071`'s failure route `R7` **does not fire** — and `C-0032`'s escalation of 0.5 mM from a preference to a **requirement** does not transfer, which is a materially different answer to `DECISIONS-FOR-NDI` decision 1. **The cheap bound predicted all of it, at 11 of 11 gradable states, from one evaluation of each law**

| | |
|---|---|
| **Task** | [`T-149`](../tasks/T-149.md) — *"the recommended element's pull-in fold has never been searched"* |
| **Leaf** | **`A2.2`** (*"electrostatic softening and pull-in: the maximum usable bias with margin"*), with **`A8.2`** for the coupling law the fold is read on |
| **Verification type** | **in-silico** (`C-0018`'s stroke-parametrised equilibrium path, **solver unchanged**, with `C-0071`'s recommended 34-arm array substituted for the affine `R = 33.3333 s` over the same `(height, model, buffer)` grid — 108 fold searches — graded against the tangency identity `k_c(s_fold) + k_eff(s_fold) = 0`) **+ logical** (a cheap sign bound evaluated at the **baseline** fold, where the coupled tangent vanishes by construction, so the composition is exact and the slope term costs one evaluation of each law) |
| **Verdict** | **PASS, and the acceptance predicate is met in the favourable direction at the recommended device.** At the **10 nm layer in 2 mM** — `C-0071`'s own device — the recommended element's equilibrium path has **no fold at any of `C-0003`'s six models**, and the binding bias ceiling changes owner to `C-0002`'s `φ = 0.2` (2 models) and to the element model's own branch end (4 models). The bias margin rises from `C-0018`'s **1.007–1.032** to **1.3877–2.5764**, and §3's 3 nm target is on the stable side at **6 of 6**. At **0.5 mM** it is **1.8706–3.4699**, also with no fold. The **fold's own stroke** — the axis `C-0032` showed decides it — moves from **3.4104–4.1248 nm** on the affine line to **past 7.9097 nm at every model**, i.e. past the deepest stroke the element model describes and **2.64× §3's target**. **The cheap bound ran first and was right everywhere it could be graded**: `Δk_c = k_Q5(s*) − 33.3333` at `C-0018`'s own fold stroke is **strictly positive, 0.582–22.015 pN/nm**, predicting a deeper fold at all 11 states where a baseline fold exists, and the solve returned `DEEPER` at 4 and `FOLD REMOVED` at 7 — **11 of 11 agreements**, including **5 states where the omitted level term `ΔR` runs the OTHER way** (−3.93 to −1.96 pN) and the slope term still won. **The declared falsifier did not fire.** The **second, weaker falsifier fired in a bounded way and is reported as such**: the recommended arm is inextensible and `C-0039`'s shooting solve enumerates only the small-rotation branch, so the path is truncated at **7.9097 nm** of stroke, and *"no fold"* means *"no fold below 7.9097 nm"* — 2.64× §3's target and 1.92× the deepest affine fold, but **not** a statement about the large-rotation branch. **One device is worse, and it is not this one**: at **7 nm / 10 mM** the recommended element folds at **2.023–2.688 nm**, still **shallower than §3's 3 nm** at 4 of 6, and its tangent minimum of 30.03 pN/nm is below `\|k_eff\|` = 38.45–77.25 there at 5 of 6 — `C-0018`'s verdict for that device survives the substitution, which is the control this claim needed. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, AND THE MOTIF IS NOT DEMONSTRATED.** `C-0055`'s literature finding stands unchanged and is upstream of the element itself: a free lever held to a single-layer sheet by **one** crossover was not found in 62 recorded queries. Every force inherits `C-0008`'s mean field in full, and `C-0005`'s one-loop correction is **123–214 %** of the leading term across this gap range — larger than every margin in this claim. |
| **Provenance** | `gpd/results/T-149-recommended-element-fold.json`, produced by `stability.RecommendedElementFoldStudyKt`; model in `src/main/kotlin/stability/RecommendedElementFold.kt`; **2 coupling records, 54 cheap-bound records, 108 fold records, 18 device records, 65 upstream checks, 17 convergence records**; **20 gate-named tests in `src/test/kotlin/stability/RecommendedElementFoldTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 16 m 52 s — the whole suite, on the whole working tree with NOTHING dropped**, including its result-reader census and Markdown-table gates; the result file re-run through `tools/study.sh`, which reported *"no result file changed"*, and diffed **byte-for-byte identical** — and the one thing that was **not** identical between two earlier runs, the wall-clock inner-loop timing, was removed from the file for exactly that reason |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂ at 0.5 / 2 / 10 mM**; 40 × 40 nm Manning-renormalised tile, footprint 1600 nm²; PEG layer **5 / 7 / 10 nm** at `σ` = 0.092 / 0.045 / 0.024 nm⁻², all six `C-0003` models; Stern capacitance 20 µF/cm²; 2000-node Poisson-Boltzmann mesh; **two load lines, both placed at 100 pN over §3's acceptable 3 nm stroke**; the recommended element is `C-0069`'s `Q5` — `EI` = 230 pN·nm², arm **8.16439083 nm**, root **13.5294118** pN·nm/rad, tip **78.2352941** pN·nm/rad, **34** in parallel |
| **Consumes** | [`C-0018`](C-0018-maximum-usable-bias.md) (the equilibrium-path solver, **re-used unchanged**, and its published band, **reproduced**), [`C-0032`](C-0032-softening-coupling-stability.md) (the `StrokeLoadLine` abstraction, the affine reference and its fold strokes, **CITED and reproduced**), [`C-0071`](C-0071-output-element-recommendation.md)/[`C-0069`](C-0069-output-element-placement.md) (the recommended element, **re-derived** from `C-0039`'s and `C-0034`'s libraries, not read from a result file), [`C-0039`](C-0039-two-spring-elastica.md) (the exact elastica and its small-rotation branch), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (a requirement is owed over `[0, s*]`), [`C-0064`](C-0064-robust-distribution.md)/[`C-0068`](C-0068-range-robust-placement.md) (a state is a device; the layer selects the phase), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate and the six floors), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md)/[`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0002`](C-0002-peg-material-parameters.md), `C-0033` (the collar composition at a fold — **not** carried) |
| **Resolves** | [`CH-0083`](../challenges/CH-0083-the-pull-in-verdict-is-quoted-for-a-load-line-the-recommendation-does-not-use.md) — **UPHELD in its grounds and DISCHARGED in its consequence.** It was right that a fold does not transfer between load lines; the fold that had never been searched has now been searched, and it moves the **favourable** way |
| **Raises** | [`CH-0098`](../challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md) (against `DECISIONS-FOR-NDI` decision 1 and `C-0032`'s escalation of 0.5 mM to a **requirement**), [`CH-0099`](../challenges/CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md) (against `C-0018`'s three-candidate **ceiling list**, which assumes a load line with no kinematic ceiling) |

---

## The claim, in one line

**The one §6 task `C-0071` could not discharge for its own recommendation is discharged, and it is discharged the good way: the element that clears `C-0017`'s six static floors also removes the pull-in fold that made the affine device's margin the thinnest number in the programme — and the sign of that was available from one subtraction, `k_Q5(s*) − 33.3333`, before a single field solve.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**, rotation **rad**,
  potential **V**, concentration **mM**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous MgCl₂ at the stated concentration.
- `z` normal to the electrode, positive **away** from it; the tile-electrode gap **is** the layer height (`C-0012`).
- The **stroke** `s = L₀ − h` is positive **downward**; `L₀` is a **force-onset** height at a defining load of 1.0 pN over the tile (`C-0011`, `CH-0010`).
- A **load line** `R(s)` is positive **upward**, in pN over the **whole array**.
  `C-0017`'s 33.3333 pN/nm is a **sum**, so **the path count does not enter the load line at all** — it enters the per-path allowables, which `C-0071` already discharges.
  Both lines here pass through the *same* operating point, 100 pN at 3 nm, and differ only in how they leave it.
- The fold is `max_s V_eq(s)`, and differentiating the balance at `V′(s) = 0` gives `k_c(s) + k_eff(s) = 0` **exactly** —
  so the argmax *is* the tangency point, and **for a nonlinear line `k_c` is the tangent at that stroke and not a constant**.
- **A device is one buffer, one layer, one bias and `L₀ → L₀ − s*`** (`C-0064`, `C-0068`).
  States belonging to different devices are **not** intersected.
  `C-0071`'s device is the **10 nm** layer, because `C-0068` shows the layer selects the crossover phase and 24 is the 10 nm one.
- **A requirement on a coupling law is owed over `[0, s*]`**, the strokes the device traverses (`C-0049`), and `s* = 3 nm`.
- **A bias ceiling belongs to a `(bias, load line)` pair** (`CH-0015`), and a fold is quoted with **both** its bias and its stroke,
  because a nonlinear load line moves a fold in its stroke far more than in its bias (`C-0032`).

---

## The cheap bound, which ran first

`CLAUDE.md`: *"at a fold the composition of two corrections is EXACT, not first order, because the baseline coupled tangent vanishes there by construction."*
Substituting one **placed** load line for another is exactly two corrections on `C-0018`'s baseline at its own fold stroke `s*`:

| | correction | cost | at 10 nm / 2 mM | at 7 nm / 10 mM |
|---|---|---|---|---|
| **slope** | `Δk_c = k_Q5(s*) − 33.3333` | **one evaluation of each law**, no field solve | **+11.60 to +22.02 pN/nm** | **+0.58 to +4.94** |
| **level** | `ΔR = R_Q5(s*) − 33.3333 s*` | a re-solve of the whole path | +3.88 to +15.59 pN | **−3.93 to −1.96 pN** |

At `s*` the baseline satisfies `k_old(s*) + k_eff(s*) = 0` exactly, so the new coupled tangent there is `Δk_c + Δk_eff` and the sign of the **free** term is the prediction: `Δk_c > 0` means that stroke is now on the **stable** side, so the fold is **deeper**.

> **11 of 11 gradable states agreed**, and the interesting half is the second column: at 7 nm / 10 mM the omitted level term runs the **other way** at 5 of the 11, and the slope term still decided the direction. **The prediction survives its own opposition.**
>
> The measured cost ratio justifies the choice: over four runs one elastica reaction is **13.66–13.96 ms** and one path point is ~42 Poisson-Boltzmann solves at **7.56–8.13 ms** each, so the field is **23.2–24.7×** the element per path point. `CH-0083` assumed the elastica was the expensive half and proposed a tabulate-and-interpolate route; **it is the cheap half, so the interpolation was not taken** and no interpolation error enters this claim.
>
> **Those three numbers are printed and deliberately NOT emitted.** A wall-clock timing is not reproducible, and `gpd/README.md` requires that a re-run which changes nothing produces no diff: the first pair of runs agreed on all 108 folds and every one of the 65 reproductions, and differed on exactly these three fields. `CLAUDE.md`'s *"emit nothing that counts steps"*, one step further out — a timing is **less** reproducible than a step count, not more. What the file carries is the deterministic half: the solve **count** per path point.

**`CH-0083`'s second cheap bound was declined and the reason is recorded**: bracketing `Q5`'s fold between `C-0018`'s affine and `C-0032`'s stiffening reading needs a monotonicity of the fold in the tangent that `C-0032` observed and did not prove — and, decisively, `Q5`'s tangent is **not** between theirs at the fold stroke, only at zero stroke.

---

## Deliverable 1 — the fold, at the device the programme recommends

**10 nm layer, `σ` = 0.024 nm⁻², placed at 100 pN over 3 nm. Six `C-0003` models per row.**

| buffer | load line | folds | pull-in bias | **fold's own stroke** | **bias margin** | target on the stable side | binding ceiling |
|---|---|---|---|---|---|---|---|
| **2 mM** | `C-0018`'s affine mandate | **6 of 6** | 0.1300–0.1836 V | **3.4104–4.1248 nm** | **1.0071–1.0317** | 6 of 6 | pull-in, 6 of 6 |
| **2 mM** | *`C-0030`'s softening flexure* (`C-0032`, **CITED**) | 6 of 6 | — | *2.80–3.17 nm* | *1.0000–1.0019* | 4 of 6 | pull-in |
| **2 mM** | **`C-0071`'s recommended arm** | **0 of 6** | **none** | **> 7.9097 nm** | **1.3877–2.5764** | **6 of 6** | `φ = 0.2` ×2, element model ×4 |
| **0.5 mM** | affine mandate | 0 of 6 | none | — | 1.2917–2.3644 | 6 of 6 | `φ = 0.2`, point-ion |
| **0.5 mM** | **recommended arm** | **0 of 6** | **none** | **> 7.9097 nm** | **1.8706–3.4699** | **6 of 6** | `φ = 0.2` ×2, element model ×4 |
| **10 mM** | affine mandate | 0 of 6 | none | — | — | **0 of 6** | `φ = 0.2` — the operating point is already past it |
| **10 mM** | **recommended arm** | **0 of 6** | none | — | **0 of 6** | — | `φ = 0.2` — unchanged |

**Three readings, and each is on the axis it is controlled on.**

1. **On the STROKE axis** — `C-0032`'s decisive one — the substitution moves the fold from 3.41–4.12 nm to **beyond 7.91 nm**, i.e. **out of the model's range**, where `C-0030`'s moved it *back* to 2.80–3.17 and through §3's target.
2. **On the BIAS axis** the margin goes **1.007–1.032 → 1.388–2.576**, a factor of **1.38–2.50**. `CLAUDE.md`: *a stiffness margin is not a bias margin*; this is quoted on the bias.
3. **On the STIFFNESS axis** the element's tangent minimum over the traversed `[0, 3]` is **30.0288 pN/nm** against `C-0017`'s worst 2 mM floor of 27.9133 — **1.0758×**, `C-0071`'s `THIN`, and unchanged by this claim. **The fold margin and the static margin are different quantities and they disagree about how comfortable the design is by a factor of two.**

---

## Deliverable 2 — the element's own boundary, which `C-0018`'s load line did not have

The recommended arm is **inextensible** and `C-0039`'s shooting solve enumerates only the **small-rotation branch**, so its law has a stroke ceiling that no layer model imposes:

| quantity | value |
|---|---|
| contour (inextensibility, a hard bound) | **8.16439 nm** |
| refusal — the largest stroke at which **both** the reaction and the tangent close | **7.9197 nm** |
| branch validity — the largest stroke at which the **reaction** closes with `max_s\|φ\| < π/2` | **7.9205 nm** |
| `max_s\|φ\|` at the branch ceiling | **1.4799 rad** (0.942 of `π/2`) |
| `max_s\|φ\|` at the 3 nm placement point | **0.4334 rad** |
| the stroke ceiling the paths were run to | **7.9097 nm** = 2.64× §3's acceptable stroke |

**The branch ends by FOLDING, not by turning past a right angle** — the reaction still closes at 7.9205 nm with the arm at 0.94 of `π/2` and refuses immediately above, so `π/2` is never reached at all. And the **tangent refuses first**, 0.0008 nm earlier, because it is a forward difference of the same law: a path's stroke ceiling must take the **smaller** of the two, which is what the study does (less a 0.01 nm safety, giving 7.9097 nm). Both statements are asserted as tests — the first two in gate 5's *"the rotation limit is where `C-0039`'s own branch ends"*, and the monotonicity that makes the ceiling a threshold at all in gate 3.

**This is a model boundary and it is reported as one.** At **12 of 108** states the branch ended there rather than at a fold or at the field's own ceiling, and at **8** of those the branch-end bias is the binding ceiling. Letting `C-0018`'s inherited three-candidate list bind instead would have quoted `CH-0007`'s 1.0 V at a stroke the element model does not describe, inflating the margin by up to **3.74×** (11.55 against 3.09 at 10 nm / 0.5 mM). Both readings are carried in the result file — `biasMargin` and `biasMarginIgnoringElementBoundary` — and the headline uses the conservative one. This is [`CH-0099`](../challenges/CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md).

---

## Deliverable 3 — the control: the device where the answer is still bad

**7 nm layer, 10 mM, `σ` = 0.045 nm⁻².** `C-0018` reports the fold there at 1.92–2.68 nm, shallower than §3's 3 nm. The substitution moves it **deeper, and not deep enough**:

| model | affine fold stroke | **recommended fold stroke** | affine margin | **recommended margin** |
|---|---|---|---|---|
| alexander-box(two-body) | 1.9183 | **2.0228** | 1.1138 | 1.0785 |
| alexander-box(virial) | 2.1851 | **2.3272** | 1.0605 | 1.0326 |
| alexander-box(des-Cloizeaux) | 2.0059 | **2.1003** | 1.1266 | 1.0898 |
| strong-stretching(two-body) | 2.3608 | **2.6877** | 1.0216 | 1.0034 |
| strong-stretching(virial) | *no fold* | *no fold* | 1.4411 | 1.4741 |
| strong-stretching(des-Cloizeaux) | 2.6822 | **removed** | 1.0038 | 1.3197 |

**4 of 6 still fold shallower than 3 nm**, and at 5 of 6 the element's 30.03 pN/nm tangent minimum is below `|k_eff|` = 38.45–77.25 there. **The substitution is not a universal repair, and this row is what proves the 10 nm result is a result rather than an artefact of the solver.** Note also that the bias margin **falls** at three of these models while the fold stroke rises — `C-0033`'s lesson that *a margin is a ratio of two biases read at two different gaps* and moves with the sign of `3 nm − s_fold`, reproduced here on a different perturbation.

---

## Deliverable 4 — what this does to §6, to `C-0071` and to `DECISIONS-FOR-NDI`

| | before | after |
|---|---|---|
| **§6 task 4** | **PASS**, both branches, *"each for a different load line"* — and `C-0071` records that neither is the recommended one | **PASS for the recommended device too.** At 10 nm / 2 mM the usable bias is **0.2176–0.3895 V** against an operating bias of **0.1283–0.1804 V**, a margin of **1.3877–2.5764**, and the ceiling is `C-0002`'s `φ = 0.2` or the element model — **not** pull-in. §6's own second branch, *"or a demonstration that the osmotic divergence removes the instability"*, is literally what happens at 2 of the 6 models |
| **`C-0071` open item 1** | *"the largest single thing the recommendation does not know"* | **closed.** The recommendation's premise ledger loses nothing and gains a margin |
| **`C-0071` failure route `R7`** | *"this element's own pull-in fold collapsing at 2 mM as `C-0030`'s did → removes a premise"* | **does not fire.** The route is **discharged**: 9 routes become 8, and the count of routes decided by *calculation* falls from 3 to 2 |
| **`C-0071`'s `T-63` conditional** | **BINDING**, *"for a new reason … its own pull-in fold has never been computed"* | **the new reason is gone.** 0.5 mM remains a **preference** (margin 1.87–3.47 against 1.39–2.58, and `C-0017`'s floors are 3.86–15.94 against 23.41–27.91) and stops being a **requirement**. [`CH-0098`](../challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md) |
| **`DECISIONS-FOR-NDI` decision 1** | *"cost of deferring: the device sits **on** its own pull-in fold at 2 mM"* | **that sentence is about a withdrawn element.** For the recommended one there is **no fold at 2 mM at any model**. The recommendation *"adopt 0.5 mM"* survives on its other five routes and loses the one that made it a requirement |

---

## The five verification gates

Executed as **20 gate-named tests** in `src/test/kotlin/stability/RecommendedElementFoldTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a reaction over a stroke **is** the secant identically; the path count multiplies reaction, secant and tangent alike; five unphysical entry points throw (a zero path count, a negative stroke, a secant at zero stroke, a descending bracket, a negative baseline fold stroke) | **PASS** |
| **2 — limiting cases** | the placed array delivers **exactly** 100 pN at 3 nm; the tangent at vanishing stroke **is** `C-0034`'s closed-form `c(ρ_n, ρ_f) EI/L³`, which shares no code with the elastica integrator; the element is strain-**stiffening** so its tangent minimum over `[0, 3]` is a **boundary** minimum, not `CH-0042`'s interior one; an inextensible arm asked past its own contour throws; substituting a line into **itself** moves nothing; a softer substitute predicts shallower and a stiffer one deeper | **PASS** |
| **3 — symmetry and conservation** | **placement is an identity** — both lines locate the same operating bias, departure **0.0** over 48 comparisons in the study and `1e−12` in the suite; the **tangency identity** `k_c(s) + k_eff(s) = 0` at the located fold, worst relative residual **9.404e−6** over **15 interior folds** (and **no** residual reported at a boundary maximum — `CLAUDE.md`); refusal is **monotone** in the stroke, so the element ceiling is a threshold and not a register; **the declared falsifier as an executable test**, on a synthetic field whose fold is `R′(s)/R(s) = 1/λ` in closed form | **PASS** |
| **4 — numerical convergence** | elastica RK4 steps 100 → 800: **7.8e−12 → 8.9e−16**; tangent-minimum scan 64 → 1024: **0.0**; Poisson-Boltzmann mesh 1000 → 4000: **3.6e−5 → 0.0**; fold coarse scan 8 → 24: **1.5e−10 → 0.0**; golden-section stroke bracket `1e−2` → `1e−6`: **7.7e−8 → 0.0**, i.e. it **stops improving**, exactly as `CLAUDE.md` says a golden-section maximum floored by the search underneath it must. **The fold axes are read at 7 nm / 10 mM**, the one state where the *recommended* line still folds — a convergence axis on a quantity that does not exist converges on `null` | **PASS** |
| **5 — literature and upstream** | **65 reproductions, worst departure 2.238e−3, and every one of the largest is the cited value's own quoted precision** — `C-0018`'s 0.184 V against 0.183588, `C-0032`'s 4.13 nm against 4.12483, `C-0017`'s 3.86 pN/nm against 3.8552, `C-0018`'s margin 1.032 against 1.0317. `C-0069`'s `Q5` row (8.16439083 nm, 24.0129142 bp, secant 33.3333333, tangent 40.8120233, minimum 30.028762) and `C-0055`'s 13.5294118 and `C-0034`'s 78.2352941 are **re-derived from their own libraries**, not read from a result file | **PASS** |

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | **the cheap sign bound disagreeing with the solved fold**, which would withdraw *"the composition at a fold is exact"* from nonlinear load lines | **no** | **11 of 11** gradable states agreed, and 5 of them agreed *against* their own omitted level term |
| **F2** | **the question not being answerable** — the element's kinematic ceiling truncating the path before any fold, leaving §6 task 4 open | **partially, and it is bounded** | The path **is** truncated, at 7.9097 nm. But that is **2.64×** §3's target stroke and **1.92×** the deepest affine fold, so *"no fold at 2 mM"* is a bounded negative and not an unbounded one. **The large-rotation branch is not enumerated and this claim does not speak for it** |

**A result that was not anticipated:** the *direction* was known from `C-0032` and the *magnitude* was not — but the magnitude turned out to be qualitative rather than quantitative. The fold does not move deeper; **it leaves the model's range entirely**, at every one of the six layer models, and the ceiling changes owner. A claim that expected to report a number reports a change of which constraint binds.

**A second one:** the ceiling *taxonomy* is load-line-dependent, not just the ceiling. `C-0018`'s three candidates silently assume a coupling that can be driven to any stroke the layer admits; a rotating arm cannot, and the omission is worth up to **3.74×** in a reported margin. That is `CH-0099`, and it is a challenge this claim raises against a claim it otherwise reproduces to the last quoted digit.

---

## Validity range

- **TRL 1–3. Nothing here is measured and the motif is not demonstrated.** `C-0055`'s 62 recorded queries stand, and they are upstream of the element itself.
- **Mean field, inherited whole.** `C-0005`'s one-loop correction is **123–214 %** of the leading term over this whole gap range — larger than the 1.39–2.58 margin this claim reports, and larger than the 1.0758× static margin it leaves untouched.
- **The RECOMMENDED DEVICE is the 10 nm layer.** The 5 nm and 7 nm rows are computed for coverage parity with `C-0018` and `C-0032` and **are not this element's device** (`C-0068`: the layer selects the phase, and the 34-root placement is phase 24). They must not be intersected with the 10 nm rows (`C-0064`).
- **The element model's own branch.** `C-0039`'s shooting solve enumerates only the small-rotation branch and the arm is inextensible, so every *"no fold"* in this claim means *"no fold below 7.9097 nm of stroke"*. A multi-branch elastica could find one deeper; nothing here excludes it.
- **`L₀` is a force-onset height** at a defining load of 1.0 pN over the tile (`C-0011`, `CH-0010`), and the layer is `C-0003`'s at `C-0001`'s single grafting density per height — **not** `C-0011`'s solved SCF profile. Deliberate, and the same choice `C-0017`, `C-0018` and `C-0032` made.
- **The load line is the tile MEAN under a uniform load.** A real 34-attachment coupling dishes the tile (`C-0063`, `C-0068`).
- **Static only.** A bias step faster than drainage can carry the tile past a fold a quasi-static ramp stops at; `C-0004`'s corner is 91 kHz–2.3 MHz.
- **1-D.** `C-0033`'s collar composes **exactly** at a fold and is **not** composed here, exactly as `C-0018` and `C-0032` did not compose it. Its sign at the affine fold was `+2.60` to `+4.99 pN/nm`, i.e. deeper again; whether it stays so at this element's fold gap is one evaluation per state and is not done.
- **The diffuse-layer drop is capped at 0.35 V**, `C-0008`'s own Stern bracket. A state needing more is a branch end, not an extrapolation.

---

## Numbers that are CITED rather than DERIVED here

| number | value | flag |
|---|---|---|
| `C-0017`'s mandate as a **sum** | 33.3333 pN/nm | **CITED**, derived there from §3 alone |
| `C-0017`'s six 2 mM stability floors at 10 nm | 23.41–27.91 pN/nm (0.5 mM: 3.86–15.94) | **CITED**, and re-derived here as a gate-5 check |
| `C-0018`'s coupled pull-in band and margin at 10 nm / 2 mM | 0.130–0.184 V; 1.007–1.032 | **CITED**, and reproduced here to 2.2e−3, the quoted precision |
| `C-0032`'s affine fold stroke, and its softening-line readings | 3.41–4.13 nm; 2.80–3.17 nm, margin 1.0000–1.0019 | **CITED**; the affine reproduced, the softening line **not re-run** |
| `C-0069`'s `Q5` row | 8.16439083 nm, 33.3333333, 40.8120233, 30.028762 | **CITED**, and every one re-derived from `C-0039`/`C-0034` |
| `C-0055`'s crossover and `C-0034`'s `A2` | 13.5294118; 78.2352941 pN·nm/rad | **CITED** |
| `C-0002`'s `φ = 0.2`, `CH-0007`'s 1.0 V, `C-0005`'s 123–214 % | — | **CITED** |
| duplex `EI` | 230 pN·nm² | **CanDo MODEL INPUT, not a measurement** |
| §3's targets | 100 pN, 3 nm, 40 × 40 nm, 5/7/10 nm, 0.5/2/10 mM | **CITED** |

---

## Still open — named, not answered

1. **The large-rotation branch of the arm.** Every *"no fold"* here is bounded at 7.9097 nm of stroke. A multi-branch elastica would say whether a fold exists beyond it; a shooting solve cannot.
2. **`C-0033`'s collar at THIS element's fold gap.** The composition is exact at a fold and costs one evaluation per state. It was `+2.60` to `+4.99 pN/nm` at the affine fold — the same direction — but where the recommended line has no fold there is no fold to compose at, so the question becomes whether the collar can *create* one, and it cannot: a positive increment to the coupled tangent cannot make it vanish.
3. **The dynamic pull-in.** A stiffening coupling has a different dynamic signature from a softening one and neither is computed.
4. **`T-63` is still a specification question**, and this claim supplies the **seventh** route to it — the first on the element the programme recommends, and the first that points the *other* way at 2 mM.
5. **The 7 nm / 10 mM device is not repaired**, and no element in the catalogue has been shown to repair it.

---

## Challenges

**Raises [`CH-0098`](../challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md)** and **[`CH-0099`](../challenges/CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md)**.

**Resolves [`CH-0083`](../challenges/CH-0083-the-pull-in-verdict-is-quoted-for-a-load-line-the-recommendation-does-not-use.md)** — upheld in its grounds, discharged in its consequence.

**None stands against this claim.** The four ways it would fail:

1. **The large-rotation branch containing a fold below the operating bias.** It cannot be below 7.91 nm of stroke, so it cannot be inside §3's operating range; but a claim that a device is *globally* stable is not made here.
2. **The element itself being removed.** `C-0071` counts five routes that do that, two of them inside published brackets. If the element goes, so does this fold.
3. **`C-0003`'s six models not bracketing the layer.** The verdict is 6 of 6 in one direction, so a seventh model outside the bracket could in principle fold.
4. **`C-0005`'s one-loop correction**, which is larger than the margin and is not a correction anybody has been able to evaluate here.
