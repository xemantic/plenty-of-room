# C-0092 — **`C-0084`'s branch end is a FORCE-LADDER ARTEFACT, and once the branch is continued there is no fold anywhere the recommended arm can reach.** The stroke is bounded by an integral of a bounded function — `δ = ∫sin φ < L` on **every** branch, with no solver — so the whole question was 0.2447 nm wide; the continuation closes **0.2414** of it and finds **no fold at 12 of 12** states of the recommended device, leaving **0.0033 nm**, one hundredth of a base-pair rise

| | |
|---|---|
| **Task** | [`T-157`](../tasks/T-157.md) — *"does the recommended arm fold on the LARGE-rotation branch?"* |
| **Leaf** | **`A2.2`** (*"electrostatic softening and pull-in: the maximum usable bias with margin"*), with `A8.2` for the element whose branch is enumerated |
| **Verification type** | **logical** (an inextensibility bound that holds on every branch, at every tip force, with no solver and no convergence parameter) **+ in-silico** (a scan for **every** sign change of `C-0039`'s far-end moment residual; a continuation of the branch connected to the unloaded state, truncated where the **first integral** stops being conserved; `C-0018`'s equilibrium path, solver unchanged, re-run over the extended domain at the recommended device) |
| **Verdict** | **PASS on `A1`–`A5`, and the bounded negative becomes an essentially unbounded one — in the favourable direction.** **`A1`**: `δ = ∫₀^L sin φ ds < L = 8.16439083 nm` strictly, on every branch, at every force, because `φ ≡ π/2` contradicts `EI φ′(0) = k_n φ(0)`. So **no equilibrium and no fold exists at or above the arm's own contour**, and `C-0084`'s open question is **0.2447 nm** wide before any code runs. **`A2`**: the residual has **one** root to ~50 pN of tip force and **15 at 1000 pN** on a 4000-cell scan; at most one is on the small-rotation branch and wherever the scan resolves it, it is the **deepest** — the curled branches **retreat** from the stroke, because `∫sin φ` cancels. **`A3`**: the continued branch reaches **8.1610821 nm**, with `max_s\|φ\| = 1.5707924 rad` — 0.999997 of a right angle and still below it. **`A4`**: over `[0, 8.1511 nm]`, **0.9984 of the contour and 2.72× §3's acceptable stroke, there is NO FOLD at 12 of 12 states** of the recommended device (10 nm, 0.5 and 2.0 mM, six `C-0003` models). The window still open is **0.0033087 nm**, **0.97 % of one base-pair rise**. **`A5`**: `C-0084`'s **7.9197 nm is not a branch end** — `C-0039`'s doubling force ladder loses the branch there, 0.2414 nm early. `CH-0099`'s **candidate stands and its value does not**: with the domain corrected the *"element model branch end"* ceiling binds at **0 of 12** states, and ~~the margins move by **1.0000–3.3380×**~~ **the margins do not move at all — 1.0000 at every fold, worst departure 3.0e−09 (AMENDED 2026-08-18, `CH-0131`/`C-0117`: the range measured a stale `T-149`, re-emitted after its own consumer)**. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, AND THE MOTIF IS NOT DEMONSTRATED.** `C-0055`'s 62 recorded queries stand and are upstream of the element itself. Every force inherits `C-0008`'s mean field, whose one-loop correction is **123–214 %** of the leading term (`C-0005`) — larger than every margin here. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.) |
| **Provenance** | `gpd/results/T-157-large-rotation-arm-branch.json`, produced by `stability.LargeRotationArmBranchStudyKt`; model in `src/main/kotlin/stability/LargeRotationArmBranch.kt`; **8 enumeration records, 64 branch records, 12 fold records, 12 convergence records, 3 falsifiers**; **16 gate-named tests in `src/test/kotlin/stability/LargeRotationArmBranchTest.kt`**; the result file re-run through `tools/study.sh` and diffed **identical in every one of its records** — the only field that moved between the two runs is one `findings` string that was edited between them, and the diff was taken on the parsed document rather than on the bytes for exactly that reason ; `tools/verify.sh` **exit 0, `BUILD SUCCESSFUL in 18m`** on the whole working tree with nothing dropped, including its result-reader census (45 self-tests, 82 studies) and Markdown-table gates (0 defects in 286 files) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂ at 0.5 and 2.0 mM**; 40 × 40 nm Manning-renormalised tile, footprint 1600 nm²; PEG layer **10 nm** at `σ` = 0.024 nm⁻², all six `C-0003` models; Stern capacitance 20 µF/cm²; 2000-node Poisson-Boltzmann mesh; the element is `C-0069`'s `Q5` — `EI` = 230 pN·nm², contour **8.16439083 nm** re-derived through `C-0039`'s own placement solve, root **13.5294118** pN·nm/rad, tip **78.2352941**, **34** in parallel, placed at 100 pN over §3's acceptable 3 nm stroke |
| **Consumes** | [`C-0039`](C-0039-two-spring-elastica.md) (the two-spring elastica, its geometry and its shooting solve — **the object this claim corrects**), [`C-0069`](C-0069-output-element-placement.md)/[`C-0071`](C-0071-output-element-recommendation.md) (the recommended element, **re-derived** through `C-0039`, not read from a file), [`C-0084`](C-0084-recommended-element-pull-in-fold.md) (the fold search this bounds, and its margins, **read from its result file**), [`C-0018`](C-0018-maximum-usable-bias.md) (`EquilibriumPath`, `bindingCeiling`, `biasMargin` — **re-used unchanged**), [`C-0034`](C-0034-guided-arm-anchorage.md) (the closed-form end-condition factor, this integrator's vanishing-load limit), [`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0002`](C-0002-peg-material-parameters.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md) |
| **Resolves** | [`C-0084`](C-0084-recommended-element-pull-in-fold.md)'s *"still open"* item 1 — *"a multi-branch elastica would say whether a fold exists beyond it; a shooting solve cannot."* **It does, and there is none.** |
| **Raises** | [`CH-0107`](../challenges/CH-0107-the-branch-end-is-a-force-ladder-artefact.md) against `C-0084`'s Deliverable 2 and `CH-0099`'s quoted boundary |

---

## The claim, in one line

**The recommended element's law does not end at 7.92 nm — a doubling ladder ends there — and once it is continued to within three picometres of its own contour, `C-0084`'s *"no fold below 7.9097 nm"* becomes *"no fold at any stroke this element can reach"*, which is the strongest statement §6 task 4 can carry for the Gen-1 device.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**, rotation **rad**,
  potential **V**, concentration **mM**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous MgCl₂.
- **`C-0039`'s geometry, restated.** Arc length `s ∈ [0, L]` from the hinge; `φ(s)` the tangent angle from
  the undeformed axis toward the stroke; `x(s) = ∫cos φ`, `z(s) = ∫sin φ`;
  `EI φ″ = −F cos φ`, `EI φ′(0) = k_n φ(0)`, `EI φ′(L) = −k_f φ(L)`.
  The conserved first integral is `½ EI φ′² + F sin φ`, and **its measured spread along a sweep is the
  integrator's own error** — which is what this claim's branch is truncated on.
- One arm's stroke is `δ = z(L)`; **`δ = r sin θ`, not `r θ`** (`CH-0040`). The array's stroke is the same
  `δ`, and its reaction is 34 times one arm's tip force, because `C-0017`'s mandate is a **sum**.
- The device's stroke `s = L₀ − h` is positive downward; `L₀` is a **force-onset** height at a defining load
  of 1.0 pN over the tile (`C-0011`, `CH-0010`).
- **A fold is `max_s V_eq(s)`**, and at it `k_c(s) + k_eff(s) = 0` exactly. **A boundary maximum is not a
  stationary point** and no tangency residual is reported at one (`CLAUDE.md`).
- The **recommended device** is the 10 nm layer at `σ` = 0.024 nm⁻² (`C-0071`, `C-0068`); the 5 nm and 7 nm
  devices are not this element's and are **not** run here (`C-0064`).

---

## Deliverable 1 — the bound, which needs no solver and holds on every branch

`δ = z(L) = ∫₀^L sin φ(s) ds ≤ L`, because `sin` is bounded by one.
Equality needs `φ ≡ π/2` almost everywhere, hence `φ′ ≡ 0`, hence — by the near-end condition
`EI φ′(0) = k_n φ(0)` — `φ(0) = 0 ≠ π/2` for any finite `k_n > 0`.

> **`δ < L` strictly, on every branch of the boundary-value problem, at every tip force, at every rotation.**

| quantity | value |
|---|---|
| the arm's contour `L` | **8.164390826631303 nm** |
| `C-0084`'s path ceiling / refusal | 7.9096867 / **7.9196867 nm** |
| **the open window the bound leaves** | **0.24470408 nm** |
| the window this claim closes | **0.24139539 nm** |
| **the window still open** | **0.00330868 nm** = **0.97 % of one base-pair rise** |
| cost | **zero** |

**This is the whole reason the expensive half was affordable.** It converts *"does a fold exist at a larger
stroke"* — an open question on an unbounded axis — into *"close a quarter of a nanometre"*, and it does so
before any code runs. It is also the only statement here that carries **no** validity range: it is a bound on
an integral of a bounded function.

---

## Deliverable 2 — the branch structure, and why the large-rotation branches are the wrong place to look

**Every sign change of the far-end moment residual, on a 4000-cell scan of `[0, 4π]`:**

| tip force per arm | roots found | on the small-rotation branch | deepest stroke | as a fraction of the contour |
|---|---|---|---|---|
| 1 pN | 1 | 1 | 1.116933 nm | 0.1368 |
| 10 | 1 | 1 | 6.171087 | 0.7559 |
| 50 | **1** | 1 | 7.883531 | 0.9656 |
| 100 | **2** | 1 | 8.045735 | 0.9855 |
| 200 | 7 | 1 | 8.114291 | 0.9939 |
| 500 | 10 | 1 | 8.149012 | 0.9981 |
| 1000 | **15** | 1 | 8.158334 | **0.9993** |
| 5000 | **39** | **0** | 6.447929 | 0.7898 |

**Three readings.**

1. **The multiplicity starts at ~100 pN, far below the right angle `C-0039`'s KDoc warns about.** That is the
   whole mechanism of the artefact: a bracket found by doubling can land on either root long before the arm
   turns past `π/2`.
2. **The large-rotation branches RETREAT from the stroke, they do not extend it.** A curled shape's
   `∫sin φ` cancels against itself, so every one of them reaches a *smaller* `δ` than the small-rotation
   root at the same force. **The place a deeper stroke could have hidden is the place it demonstrably does
   not.**
3. **The `F = 5000` row is the honest one.** The scan finds **no** small-rotation root there, not because the
   branch has ended but because at high force the primary root sits within one scan cell of a curled
   neighbour. `CLAUDE.md`: *a search over a continuum returns a density*. The **count** is a lower bound and
   the row is reported as it came out; what resolves the branch is the **continuation**, not a finer scan.

---

## Deliverable 3 — the continued branch, and `C-0084`'s ceiling

Marching the **near-end rotation** upward in 2 mrad steps and taking, at each, the **first** sign change of
the residual in the tip force above the previous one — `CLAUDE.md`'s *"scan for the first sign change and
bisect on that bracket"*, applied **along** a branch rather than across one:

| quantity | value |
|---|---|
| the deepest stroke the branch reaches | **8.1610821 nm** |
| the tip force there | 1550.92 pN per arm (**52 731 pN** over the 34-arm array) |
| `max_s\|φ\|` there | **1.5707924 rad**, i.e. **0.999997** of `π/2` — **still below it** |
| the first-integral spread there | **0.0** at the emitted precision |
| `C-0084`'s ladder refusal | **7.9196867 nm** |
| **the branch runs past it by** | **0.2413954 nm** |

> **`C-0084`'s 7.9197 nm is a property of the solver, not of the elastica.** `C-0039`'s
> `forceForDisplacement` **doubles** a tip force from `0.5 δ k_small` until the stroke reaches its target,
> and `stateAtForce` brackets the shooting parameter by doubling from a seed. Both are exact while the
> residual has one root. Once it has two, a doubling step can put the bracket around the wrong one, the
> stroke reported at that force collapses, the force ladder never reaches its target, and the routine
> throws. **A doubling ladder does not report a branch end; it reports having lost the branch.**

The arm **never turns past a right angle on this branch**: `max_s|φ|` rises monotonically toward `π/2` and
the reaction diverges as `δ → L`. So `C-0084`'s *"the branch ends by FOLDING, not by turning past a right
angle"* was right about the right angle and wrong about the ending.

---

## Deliverable 4 — the answer, at the device the programme recommends

**10 nm layer, `σ` = 0.024 nm⁻², placed at 100 pN over 3 nm, path run to 8.1511 nm of stroke —
0.9984 of the arm's contour and 2.72× §3's acceptable stroke.**

| model | buffer | folds? | binding ceiling | bias margin | `C-0084`'s | movement |
|---|---|---|---|---|---|---|
| alexander-box(two-body) | 0.5 | **no** | `φ = 0.2` | 1.8706 | 1.8706 | 1.0000 |
| alexander-box(two-body) | 2.0 | **no** | `φ = 0.2` | **1.3877** | 1.3877 | 1.0000 |
| alexander-box(virial) | 0.5 | **no** | `φ = 0.2` | 2.7969 | 2.7969 | 1.0000 |
| alexander-box(virial) | 2.0 | **no** | `φ = 0.2` | 2.0279 | 2.0279 | 1.0000 |
| alexander-box(des-Cloizeaux) | 0.5 | **no** | `φ = 0.2` | 3.0204 | 3.0181 | 1.0007 |
| alexander-box(des-Cloizeaux) | 2.0 | **no** | `φ = 0.2` | 2.1609 | 2.1591 | 1.0008 |
| strong-stretching(two-body) | 0.5 | **no** | `φ = 0.2` | 3.5824 | 3.0885 | 1.1599 |
| strong-stretching(two-body) | 2.0 | **no** | `φ = 0.2` | 2.6534 | 2.2834 | 1.1620 |
| strong-stretching(virial) | 0.5 | **no** | point-ion 1.0 V | **10.9072** | 3.4699 | **3.1433** |
| strong-stretching(virial) | 2.0 | **no** | point-ion 1.0 V | 7.3137 | 2.5764 | 2.8387 |
| strong-stretching(des-Cloizeaux) | 0.5 | **no** | point-ion 1.0 V | 10.7287 | 3.2141 | **3.3380** |
| strong-stretching(des-Cloizeaux) | 2.0 | **no** | point-ion 1.0 V | 7.1784 | 2.3679 | 3.0316 |

**`NO FOLD at 12 of 12`.** And **the *"element model branch end"* ceiling binds at 0 of 12 states** once the
domain is right: at eight states `C-0002`'s `φ = 0.2` binds and at four `CH-0007`'s point-ion 1.0 V does.

**What this does to `C-0084`'s headline.** At 10 nm / 2 mM the bias margin band becomes **1.3877–7.3137**
against its published **1.3877–2.5764**; the *worst* model is unchanged to the last digit, and the band's top
rises 2.84×. `CLAUDE.md`'s discipline applies to the reader too: **the number that governs is the minimum,
and it did not move.**

---

## AMENDED, 2026-08-18 (iteration 24) — `A5`'s margin-movement RANGE measured a stale input

**What changes:** `A5`'s *"the margins move by **1.0000–3.3380×**"* becomes **1.0000 at every one of the
12 folds**, worst absolute departure from unity **3.0e−09**.
**What does not change:** the candidate, the verdict and the arithmetic. The *"element model branch end"*
ceiling still binds at **0 of 12** states, `A1`–`A4` are untouched, and `C-0084`'s 7.9197 nm is still not a
branch end.

**Why it was wrong.** `T-157` reads `T-149` at run time, and
[`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) re-emitted **`T-157` before `T-149`** inside one
commit. So the `T-157` this clause was read from reproduces the *pre-`C-0101`* `T-149` margins digit for
digit, and the 3.3380× was the difference `C-0101` had already absorbed — measured twice over rather than
once.

**How it was found, and it was not by looking.** An unrelated repair to a shared main source
(`actuator/PullInStability.kt`, an endpoint overshooting its own ceiling by three ulp) prompted a
coordinator to ask for a **measurement** rather than the author's proof that the change was invisible. Four
of the five consumers came back byte-identical; `T-157` moved 17 fields, and a controlled A/B with
`PullInStability.kt` restored to `HEAD` returned a `T-157` **byte-identical to the repaired run** — proving
the movement belonged to the *input*, not the repair.
**A proof that a change is invisible is not a substitute for running the consumers, because the run also
checks everything the proof was not about.**

Filed as [`CH-0131`](../challenges/CH-0131-t-157-was-re-emitted-before-its-own-input.md); amended here by
[`C-0117`](C-0117-reemission-order.md) (`T-200`), which verifies the corrected reading off the re-emitted
file, checks the **second** dependency edge among `C-0101`'s eleven (`T-138` reads `T-136` — **clean**, so
one edge of two was violated rather than the sweep being systematically wrong), and ships
[`tools/reemission-order.py`](../../tools/reemission-order.py) so the class cannot recur.

---

## The five verification gates

Executed as **16 tests** in `src/test/kotlin/stability/LargeRotationArmBranchTest.kt`, each named for the gate or falsifier it serves.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the contour bound is a **length** and it is the arm's own, re-derived through `C-0039`'s placement solve; six unphysical entry points throw (a negative force, one scan cell, a zero shooting ceiling, a negative stroke, a stroke **at** the contour, a stroke past it) | **PASS** |
| **2 — limiting cases** | at vanishing load the branch **is** `C-0034`'s closed-form `c(ρ_n, ρ_f) EI/L³`, which shares no code with this integrator; the branch reproduces `C-0069`'s placement — 34 arms present exactly 33.3333 pN/nm as a secant at 3 nm, to `1e−6`; **one** root at small load and **more than one** at large load | **PASS** |
| **3 — symmetry and conservation** | **the theorem**, asserted at every enumerated root at five forces *and* at every one of the continued branch's rows: `δ < L`; the **first integral** conserved to `1e−9` at every row; the beam's own **moment equilibrium** `EI(φ′(0) − φ′(L)) = F x(L)` closing at every row; `max_s\|φ\| < π/2` at every row, and above `0.999 π/2` at the last; the stroke **and** the force strictly increasing along the branch | **PASS** |
| **4 — numerical convergence** | RK4 steps 200 → 1600 at a 7.5 nm stroke: the tip force agrees **below the emission floor** at every count; the continuation step 8 → 4 → 2 → 1 mrad moves the supremum by **2.77e−4 nm**, four decades below the 0.0033 nm window it bounds; the shooting scan 1000 → 8000 cells takes the root count 13 → 15 → 15 → 15, i.e. it **saturates** — and the count is reported as a **lower bound** rather than a converged number | **PASS** |
| **5 — literature and upstream** | **`C-0084`'s refusal is reproduced**, `7.9196867` against its published `7.91968584`, on the *same object* through the *same* `loadLineStrokeCeiling` — so the difference between it and this claim's supremum is a property of the **solver** and not of two different arms; `C-0084`'s twelve bias margins at the recommended device are **read from its result file** and each is compared against the re-run; `C-0069`'s 8.16439083 nm is re-derived, not transcribed | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | **the refusal being real** — the branch genuinely ending near 7.92 nm, which would leave `C-0084` and `CH-0099` standing as written | **no, and it is the finding** | The branch reaches 8.1610821 nm, **0.2413954 nm past** the refusal, with `max_s\|φ\|` still below a right angle |
| **F2** | **a fold in the extension** — which would make `C-0084`'s *"no fold"* wrong at some model rather than merely bounded | **no** | 12 of 12 states have no fold below 8.1511 nm of stroke, 0.9984 of the contour |
| **F3** | **a branch reaching past the contour** — impossible by the bound, and the strongest falsifier available precisely because it **cannot** fire | **no** | Every root at every enumerated force is below the contour; the deepest any branch reaches is **0.999258** of it |

**A result that was not anticipated.** The task was formulated to enumerate the *large-rotation* branch, on
`C-0084`'s own reading that the small-rotation one had ended. It had not. The large-rotation branches exist,
there are up to 39 of them, and **they are all shorter**: the answer came from continuing the branch
`C-0084` thought it had exhausted, not from finding a new one.

**A second one.** The margin movement is **not** `CH-0099`'s predicted 2.57–3.74× at all eight states it
priced. At four of them `C-0002`'s `φ = 0.2` steps in as the next ceiling and the margin barely moves
(1.0007–1.1620×); at the other four the point-ion boundary binds and the movement is 2.84–3.34×.
**A taxonomy gap's price is set by whichever candidate is *second*, and `CH-0099` priced it against the last.**

---

## Validity range

- **TRL 1–3. Nothing here is measured and the motif is not demonstrated.** `C-0055`'s 62 recorded queries stand.
- **The contour bound needs no validity range.** It is a bound on an integral of a bounded function; no
  branch, rotation or force escapes it. Everything else here does.
- **Mean field, inherited whole.** `C-0005`'s one-loop correction is 123–214 % over this gap range. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **The branch continuation is truncated on the FIRST INTEGRAL**, a measured property of the RK4 sweep, not of
  the elastica. A stiffer integrator would extend it; **nothing can extend it past the contour.**
- **The root COUNT is a sampling statistic** and is a lower bound. Existence of a root is monotone under
  refinement and is safe; the count is not, and the `F = 5000` row shows the scan missing one.
- **Only the 10 nm layer is run.** The 5 nm and 7 nm devices are not this element's (`C-0064`, `C-0068`) and
  10 mM has no operating point at all (`C-0084`).
- **1-D, static, and `C-0033`'s collar is not composed** — the same choices `C-0018`, `C-0032` and `C-0084`
  made. A bias step faster than drainage can carry the tile past a fold a quasi-static ramp stops at.
- **The load line is the tile MEAN under a uniform load**; a real 34-attachment coupling dishes the tile.
- **The disconnected curled branches are enumerated and NOT priced.** They are not reachable by quasi-static
  loading from the unloaded state; whether a dynamic bias step could reach one is outside a static treatment.
- **The reaction at the deep end is enormous** — 52 731 pN over the array at 8.161 nm of stroke, which is
  three orders past every per-path allowable in the programme. The branch is *defined* there; the **device**
  is destroyed there, and that is a separate statement this claim does not make.

---

## Numbers that are CITED rather than DERIVED here

| number | value | flag |
|---|---|---|
| `C-0069`'s `Q5` contour | 8.16439083 nm | **RE-DERIVED** through `C-0039`'s own placement solve |
| `C-0055`'s crossover and `C-0034`'s `A2` | 13.5294118; 78.2352941 pN·nm/rad | **CITED** |
| `C-0084`'s ladder refusal | 7.91968584 nm | **CITED**, and reproduced here at 7.9196867 |
| `C-0084`'s twelve bias margins at 10 nm | see Deliverable 4 | **READ** from `gpd/results/T-149-recommended-element-fold.json` |
| `C-0002`'s `φ = 0.2`, `CH-0007`'s 1.0 V, `C-0005`'s 123–214 % | — | **CITED** |
| duplex `EI` | 230 pN·nm² | **CanDo MODEL INPUT, not a measurement** |
| §3's targets | 100 pN, 3 nm, 40 × 40 nm, 10 nm layer, 0.5/2 mM | **CITED** |

---

## Still open — named, not answered

1. **The last 0.0033087 nm below the contour**, which this integrator cannot resolve at 1600 steps. The bound
   excludes a fold *at or above* the contour; it does not by itself exclude one inside that sliver. It is
   **0.97 % of one base-pair rise**, i.e. below the resolution of the design language (`CLAUDE.md`).
2. **The curled branches under a dynamic bias step.** Static only.
3. **`C-0033`'s collar** at the extended ceilings — one evaluation per state, not done.
4. **`C-0084`'s other 96 states.** Only the 12 of the recommended device are re-run, and `CH-0099`'s
   inflation table is repriced only for those.
5. **The per-path allowable at the deep end of the branch.** The element's *law* is defined to 8.161 nm; the
   *device* is not, and nothing here says where between 3 nm and 8.16 nm it stops being buildable.

---

## Challenges

**Raises [`CH-0107`](../challenges/CH-0107-the-branch-end-is-a-force-ladder-artefact.md)** against `C-0084`'s
Deliverable 2 and against the boundary value `CH-0099` prices its inflation from.

**Resolves `C-0084`'s open item 1.**

**None stands against this claim.** The four ways it would fail:

1. **The continuation losing the branch too**, in the other direction — landing on a *different* branch that
   happens to be monotone. Guarded by the first integral, the moment balance, the strict monotonicity of both
   the stroke and the force, and `max_s|φ| < π/2` at every row.
2. **A fold inside the 0.0033 nm sliver.** Not excluded, and stated.
3. **The element being removed.** `C-0071` counts five routes that do that; two are inside published brackets.
4. **`C-0005`'s one-loop correction**, larger than every margin here and evaluable by nobody in this programme.
