# C-0096 — **`C-0039`'s doubling force ladder is repaired as a branch continuation, and 90 of the 96 fold rows `CH-0107` left outstanding could never have moved** — because `min(layer, element)` is unchanged *identically* when only the element argument rises. The 18 that can move were re-read by re-running the whole 108-row study, the *"element model branch end"* ceiling `CH-0099` was raised about now binds at **0 of 108**, and the repair costs **190 sweeps against the ladder's 209**

| | |
|---|---|
| **Task** | [`T-159`](../tasks/T-159.md) — *"repair the doubling force ladder, and reprice `CH-0099` at the other 96 states"* |
| **Leaf** | **`A8.2`** (the coupling element whose domain is repaired), with **`A2.2`** for the fold rows re-read at the corrected domain |
| **Verification type** | **logical** (a containment on `min(layer stroke ceiling, element domain ceiling)` that settles 90 of 96 rows with no solve and no tolerance) **+ in-silico** (the repaired continuation and its own five gates; the whole of `C-0084`'s 108-row study re-run at the corrected domain; **fourteen** downstream studies re-run and diffed field by field, every movement classified) |
| **Verdict** | **PASS on `A1`–`A6`, with the declared falsifier `F3` FIRING in a bounded way and catching two more copies of the same defect.** **`A1`**: of `C-0084`'s 108 fold rows, **54** carry a load line with no elastica in it and **36** have a layer-owned stroke ceiling of 4.33–6.50 nm, so 90 cannot move — not to a tolerance, but because `min(a, b) = a` whenever `a ≤ b ≤ b′`. **18** are element-owned, `C-0092` re-read 12, and the **6** outstanding are the 10 nm layer in 10 mM. **`A2`**: `forceForDisplacement` now **continues** the branch — each force step's shooting root anchored on the previous accepted one, the step **shrinking** rather than doubling when a branch test fails, and a **refusal** where no root below `π/2` exists — and answers to **8.1404072 nm** where the ladder refused at 7.91968584, with `max_s\|φ\| = 1.5688653` rad, **0.998771 of a right angle and still below it**, inside a contour of 8.164390826631301 nm. **`A3`**: the repair takes **190** RK4 sweeps where the ladder took **209** on the same call, asserted as a bounded test. **`A4`**: fourteen studies re-run; **11** result files move at all, **194** moved fields each classified — 85 a real change, 57 a decision, 25 a residual of a vanishing quantity, 23 a number carried inside an unrounded prose string, 4 one unit in the last emitted digit. **`A5`**: the element-model boundary binds at **0 of 108** states against `C-0084`'s 8, so `CH-0099`'s **candidate stands and its 2.567–3.740× price is withdrawn**; the 10 nm bands become **1.3877–7.3137** (2 mM) and **1.8706–10.9072** (0.5 mM), reproducing `C-0092`'s twelve over the whole sweep, **minima unchanged to the last digit**. **`A6`**: `C-0033`'s collar argument is **filed**, and filing it made it conditional on a measurement that `C-0033` itself supplies — **80 of 80** measured `d ln μ/dh` are positive, over gaps of 2.00–11.0 nm against a ceiling gap of 1.8696 nm. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, AND THE MOTIF IS NOT DEMONSTRATED.** `C-0055`'s 62 recorded queries stand and are upstream of the element itself. Every margin re-read inherits `C-0008`'s mean field, whose one-loop correction is **123–214 %** of the leading term (`C-0005`) — larger than every number in this claim. |
| **Provenance** | `gpd/results/T-159-doubling-ladder-repair.json`, produced by `stability.DoublingLadderRepairStudyKt`; model in `src/main/kotlin/stability/DoublingLadderRepair.kt`; the repair in `src/main/kotlin/anchoring/TwoSpringElastica.kt`; the classified diff in `gpd/data/T-159-downstream-diff.json`, produced by `tools/T-159-result-diff.py`; **13 gate-named tests in `src/test/kotlin/stability/DoublingLadderRepairTest.kt`** and **5 more in `src/test/kotlin/anchoring/TwoSpringElasticaTest.kt`**, the first of which is the failing test that demonstrated the defect; the result file **re-run through `tools/study.sh` and diffed byte-for-byte identical**; `tools/verify.sh` **exit 0, `BUILD SUCCESSFUL`** on the whole working tree, including its result-reader census (**85 studies, 60 direct + 27 transitive read edges**) and Markdown-table gate (**0 defects in 303 files**) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂ at 0.5 / 2 / 10 mM**; 40 × 40 nm Manning-renormalised tile, footprint 1600 nm²; PEG layer **5 / 7 / 10 nm** at `σ` = 0.092 / 0.045 / 0.024 nm⁻², all six `C-0003` models; Stern capacitance 20 µF/cm²; 2000-node Poisson-Boltzmann mesh; the element is `C-0069`'s `Q5` — `EI` = 230 pN·nm², contour **8.164390826631301 nm** re-derived through `C-0039`'s own placement solve, root **13.5294118** pN·nm/rad, tip **78.2352941**, **34** in parallel, placed at 100 pN over §3's acceptable 3 nm stroke; 400 RK4 steps unless a convergence row says otherwise |
| **Consumes** | [`C-0039`](C-0039-two-spring-elastica.md) (**the solver this repairs**, and the placement table two of whose rows move), [`C-0092`](C-0092-large-rotation-arm-branch.md)/[`CH-0107`](../challenges/CH-0107-the-branch-end-is-a-force-ladder-artefact.md) (the diagnosis, the contour theorem, and the twelve rows already re-read), [`C-0084`](C-0084-recommended-element-pull-in-fold.md) (the 108 fold rows, **read from its result file** and **re-run** at the corrected domain), [`CH-0099`](../challenges/CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md) (the taxonomy this reprices), [`C-0033`](C-0033-collar-on-the-equilibrium-path.md) (the collar's measured `d ln μ/dh`, **read from its result file**), [`C-0018`](C-0018-maximum-usable-bias.md)/[`C-0069`](C-0069-output-element-placement.md)/[`C-0071`](C-0071-output-element-recommendation.md)/[`C-0031`](C-0031-bracketed-root-repair.md) |
| **Raises** | [`CH-0112`](../challenges/CH-0112-a-lost-branch-is-recorded-as-a-fold-and-as-a-ceiling.md) against `C-0039`'s placement table (2 of 34 rows), `C-0084`'s Deliverable 2 census (12 of 108 and 8 of 108, both now zero), `C-0050`'s reach catalogue (2 refusal notes) and — for a different reason — `C-0046`'s binding-constraint lists (2 of 60 rows, decided by a tie); and `T-164` / `T-165` on the queue |
| **Resolves** | [`C-0092`](C-0092-large-rotation-arm-branch.md)'s *"still open"* items **3** (`C-0033`'s collar at the extended ceilings) and **4** (`C-0084`'s other 96 states); **discharges [`CH-0099`](../challenges/CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md) in its consequence** and upholds it in its grounds |

---

## The claim, in one line

**A bracketing strategy had been written into three published claims in three different vocabularies — as a *fold* in a placement table, as a *model domain* in a ceiling taxonomy, and as a *note* in a reach catalogue — and repairing it removes all three; but 90 of the 96 rows that had to be re-read to find that out could be excluded by a containment identity that costs nothing, and saying which 6 remain is worth more than the 6.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**,
  rotation **rad**, potential **V**, concentration **mM**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous MgCl₂.
- **`C-0039`'s geometry, unchanged.** Arc length `s ∈ [0, L]` from the hinge; `φ(s)` the tangent
  angle from the undeformed axis toward the stroke; `x(s) = ∫cos φ`, `z(s) = ∫sin φ`;
  `EI φ″ = −F cos φ + H sin φ`, `EI φ′(0) = k_n φ(0)`, `EI φ′(L) = M₀ − k_f φ(L)`.
  The conserved first integral at `H = M₀ = 0` is `½ EI φ′² + F sin φ`.
- One arm's stroke is `δ = z(L)`; **`δ = r sin θ`, not `r θ`** (`CH-0040`). The array's reaction is
  34 times one arm's tip force, because `C-0017`'s mandate is a **sum**.
- The device's stroke `s = L₀ − h` is positive downward; `L₀` is a **force-onset** height at a
  defining load of 1.0 pN over the tile (`C-0011`, `CH-0010`).
- **A fold is `max_s V_eq(s)`**, and at it `k_c(s) + k_eff(s) = 0` exactly.
  **A boundary maximum is not a stationary point** and no tangency residual is reported at one.
- **A row's stroke ceiling is `min(layer stroke ceiling, element domain ceiling)`**, and correcting
  the element domain can only **raise** the second argument.
- The **recommended device** is the 10 nm layer at `σ` = 0.024 nm⁻² in 0.5 and 2.0 mM
  (`C-0071`, `C-0068`); the 5 nm and 7 nm rows are coverage parity and are **not** intersected
  with it (`C-0064`).
- **A result file is rounded at the serialisation boundary**, so a diff that appears at all is
  already at least one unit in the last emitted significant digit.

---

## Deliverable 1 — the cheap bound, which runs on a file that already existed

A path is searched over `[0, min(layer, element)]`. Correcting the element domain raises only the
second argument. **So a row can move only where the element owns the `min`** — and where the layer
owns it the corrected ceiling is unchanged *identically*, not to a tolerance.

| class of row | count | can the correction move it? |
|---|---|---|
| `C-0018`'s affine mandate `R = 33.3333 s` | **54** | **no** — the row contains no elastica at all |
| `LQ5`, layer-owned ceiling (4.33–6.50 nm at 5 and 7 nm) | **36** | **no** — `min(a, b) = a` for every `b′ > b ≥ a` |
| `LQ5`, element-owned ceiling (all at the 10 nm layer) | **18** | **yes** |
| — of which `C-0092` already re-read (10 nm, 0.5 and 2.0 mM) | 12 | |
| — **of which outstanding** (10 nm, **10 mM**) | **6** | |

> **`CH-0107`'s 96 outstanding rows are six.** The cost of establishing that is one pass over
> `gpd/results/T-149-recommended-element-fold.json`, against 96 fold searches at ~42
> Poisson-Boltzmann solves per path point.

**And the six carry no operating point.** At 10 nm / 10 mM the target stroke is outside the model
floor, so `biasMargin`, `pullInStroke` and `targetStrokeOnStableSide` are all `null` before the
correction and after it. Re-run, **6 of 6 move their stroke ceiling**, **2 of 6 change which ceiling
binds** (point-ion 1.0 V → `C-0002`'s `φ = 0.2`, at 0.937360912 and 0.554492177 V) and **0 of 6
move a bias margin**, because there is none there to move.

---

## Deliverable 2 — the repair, and exactly what changed

**Three files change, and the arithmetic is in one of them.**
`src/main/kotlin/anchoring/TwoSpringElastica.kt` carries items 1–5 below;
`src/main/kotlin/stability/RecommendedElementFold.kt` gains one **defaulted** `strategy` parameter on
`recommendedArmLine`, which changes no call site's behaviour;
`src/main/kotlin/stability/LargeRotationArmBranch.kt` passes `DOUBLING_LADDER` at the one place that
must, and says in its KDoc why.

1. **`forceForDisplacement` continues rather than doubles.** It marches the tip force keeping the
   previous accepted root as a **shooting floor**; at each force the scan grows geometrically from
   that floor and takes the **first** sign change; a trial whose sweep turns past `π/2` is not a
   candidate and the step is **halved** toward the last accepted parameter instead; and where no
   root below a right angle exists the stroke is **refused**, with the deepest stroke the branch did
   reach in the message. The anchoring is legitimate because at fixed `φ0` the far-end residual is
   *decreasing* in the tip force, and that is **checked at run time** rather than assumed: a floor
   whose residual is not negative returns `null` and the caller shrinks its force step.
2. **`stateAtDisplacement` returns the continuation's own state**, so a caller cannot be handed a
   state from a different branch than the force it was given.
3. **`stateAtForce` is unchanged.** Its direct callers work at forces where the residual has one
   root, and a minimal repair is one that does not move them.
4. **`sweepCount` / `resetSweepCount`** — a diagnostic counter, read by no physics, so the thing the
   strategy is *chosen* for can be asserted (`C-0031`).
5. **`BranchStrategy.DOUBLING_LADDER`** retains `C-0039`'s original blind ladder, **opt-in**, used
   by exactly one caller: `stability.ladderRefusalStroke()`, which is `C-0092`'s measurement of the
   artefact. *A repair that makes the defect it repairs unmeasurable replaces one unfalsifiable
   number with another.*

| quantity | `C-0084`'s ladder | **the repaired continuation** |
|---|---|---|
| refusal — reaction **and** tangent close | 7.91968584 nm | **8.1404072 nm** |
| branch validity — reaction closes with `max_s\|φ\| < π/2` | 7.92047876 | **8.14122168** |
| the path stroke ceiling (`min`, less 0.01 nm) | 7.909685836937754 | **8.1304072** |
| `max_s\|φ\|` at the refusal | — | **1.5688653 rad (0.998771 of `π/2`)** |
| `max_s\|φ\|` at the branch-validity ceiling | 1.47985073 rad (0.9421 of `π/2`) | **1.56901316 rad (0.9988648)** |
| `max_s\|φ\|` at §3's 3 nm placement point | 0.433393641 | **0.433393641**, unchanged |
| the arm's contour, a bound on every branch | 8.164390826631303 | 8.164390826631301 (**2 ulp**) |
| RK4 sweeps for one `forceForDisplacement(3 nm)` on `E5`'s 12.5 nm arm | **209** | **190** |

**The refusal is quoted to seven digits on purpose.** It is located by a bisection on the **bracket
width** at a resolution of `1e−6` nm, and two callers that bracket it differently land 1.5e−7 apart:
this claim's own `recommendedElementDomain()` reads **8.14040721** on `[3.0, L − 1e−9]` and `T-149`'s
re-run reads **8.14040706** on `[0.1, L]`. Both are the same number to the resolution they were
asked for, and quoting either to nine digits would be quoting the bracket.

**The repair is cheaper than the defect.** An anchored scan starts at the previous root, so it
brackets in a few cells where a blind ladder rebuilds its bracket from zero at every rung.

**The sweep count is asserted and deliberately NOT emitted.** `CLAUDE.md`: *emit nothing that counts
steps* — a step count is a property of the **path** and a last-ulp jitter can flip one comparison and
move it. What the suite carries is the **bound**, `1..209`, which fails if the strategy degenerates;
the 190 is a measurement quoted once, here.

---

## Deliverable 3 — the 108 rows, re-read

`stability.RecommendedElementFoldStudyKt` re-run **unchanged** against the repaired element:

| what moved | rows | classification |
|---|---|---|
| `strokeCeiling` 7.90968584 → **8.13040706** | **18** | **a real change** — the repair, and it is what this task is |
| `branchEndStroke` / `branchEndBias` / `branchEndedOnThe*` | 12 | a real change: the branch now ends on the **field**, not on the element model |
| `bindingCeiling` / `usableBias` / `biasMargin` | 10 | a real change |
| `coupledTangentAtFold` and its `tangencyResidual`, at 7 nm / 10 mM | **3** | a quantity that vanishes at a fold by construction, moving by **4.3e−8 to 1.8e−7** relative |
| `cheapBound[…].tangentChange` 0.978184109 → 0.978184108 | 1 | one unit in the last emitted significant digit |
| `upstreamChecks[…].departure` 1.00000053e−09 → 1.00000075e−09 | 2 | a departure that is meant to be zero |
| `runParameters.armLength`, a **string** and therefore unrounded | 1 | **2 ulp** |

> **The containment held at 90 of 90.** Not one row the cheap bound excluded moved its stroke
> ceiling, its ceiling owner, its binding ceiling, its bias margin, its fold stroke or its verdict.
> Three of them moved a *tangency residual* — a diagnostic of a quantity that is zero at a fold —
> and that is the amplification of a last-ulp root by its own near-cancellation, not a moved answer.

**The census the correction produces:**

| | `C-0084` | repaired |
|---|---|---|
| branches ending on the **element model** | 12 of 108 | **0 of 108** |
| the **element-model branch end** as binding ceiling | 8 of 108 | **0 of 108** |
| `LQ5`'s 54 binding ceilings | `φ = 0.2`: 38, pull-in: 4, element: 8, point-ion: 4 | **`φ = 0.2`: 44, pull-in: 4, point-ion: 6** |
| bias margin, 10 nm / 2 mM | 1.3877–2.5764 | **1.3877–7.3137** |
| bias margin, 10 nm / 0.5 mM | 1.8706–3.4699 | **1.8706–10.9072** |

Both bands reproduce `C-0092`'s twelve rows to the digits it published — on the whole sweep rather
than on twelve of it — and **both minima are unchanged**, which is the number that governs.
**No fold appears anywhere.** `C-0084`'s verdict is untouched and its negative is stronger.

---

## Deliverable 4 — `CH-0099`, repriced

`CH-0099` priced the taxonomy gap as the ratio of the margin *ignoring* the element boundary to the
margin *with* it, at the 8 states where the boundary bound: **2.567× to 3.740×**.

> **At the corrected domain the boundary binds nowhere in the 108, so the ratio has no states left
> to be taken over.** `C-0084`'s own paired reading — *"with it 1.3877–2.5764, without it
> 1.3877–7.7937, over 4 of 6 states where it binds"* — becomes *"with it 1.3877–7.3137, without it
> 1.3877–7.3137, over **0 of 6**"*: the two readings have become the same reading.

**`CH-0099`'s grounds stand and are worth keeping.** *A coupling element has a domain, and
`C-0018`'s three-candidate list has no name for it* is true, `C-0069`'s census admits three
mechanisms that have one, and the fourth candidate should stay in `bindingCeiling`'s vocabulary
against the next element that exercises it. What is withdrawn is the **price**, and the reason it
was wrong is `C-0092`'s: the boundary was a solver's, 0.2207 nm too shallow.

**The generalisable sentence is not the numbers.** `C-0092` found that *a taxonomy gap's price is
set by whichever candidate is second*. This claim adds the sharper version:

> **A model boundary that a solver invented can be priced, argued about and challenged for two
> iterations without anybody asking whether it exists.** It took a repair to find out that the
> answer is no — at 108 of 108 states.

---

## Deliverable 5 — the downstream diff, classified

Fourteen studies consume `TwoSpringElastica`; all fourteen were re-run in a snapshot and diffed
field by field against the result files this iteration inherited (`tools/T-159-result-diff.py`).
**Nothing was copied back**: a moved number belongs to the claim that owns it.

| file | moved / compared | worst relative | verdict |
|---|---|---|---|
| `T-149-recommended-element-fold.json` | 142 / 6946 | 7.00e−1 | a decision moved — **intended** |
| `T-79-two-spring-elastica.json` | 19 / 1487 | 1.14e+0 | a decision moved — **`CH-0112` (a)** |
| `T-108-desired-stroke-reach.json` | 6 / 1930 | 2.33e−2 | a decision moved — **`CH-0112` (c)** |
| `T-99-flexure-count-hinge-trade.json` | 8 / 2370 | 9.22e−5 | a decision moved — **`CH-0112` (d)**, a tie |
| `T-157-large-rotation-arm-branch.json` | 7 / 1053 | 1.67e−6 | beneath the emitted precision |
| `T-152-collinear-clearance.json` | 4 / 503 | 5.25e−7 | beneath the emitted precision |
| `T-134-plan-tolerance.json` | 4 / 923 | 2.42e−9 | beneath the emitted precision |
| `T-116`, `T-135`, `T-136`, `T-138` | 1 each | ≤ 5.7e−5 | beneath the emitted precision |
| `T-119`, `T-122`, `T-133` | **0** | — | **byte-identical** |

**194 moved fields, each classified**: 85 a real change (75 of them in `T-149`, which is what this
task is), 57 a decision, 25 a residual of a quantity that vanishes by construction, **23 a number
carried inside an unrounded prose string**, 4 one unit in the last emitted significant digit.

**That fourth class is a finding about the emitter, not about the repair.** A result file is rounded
at the serialisation boundary and *a number emitted as a STRING is not* (`CLAUDE.md`), so 23 apparent
"decisions" are `Double.toString()` moving in its sixteenth digit inside a sentence — `T-152`'s
*"0.6756091733686969 nm"* becoming *"0.6756091733686986 nm"* inside three prose fields and a
`decision` string. A diff classifier that does not strip digits before calling a prose change a
verdict change over-reports moved verdicts by **40 %** here.

**`T-157` reproduces**, and that is why `BranchStrategy.DOUBLING_LADDER` was retained: its seven
moved fields are six unrounded strings and one vanishing residual, and `ladderRefusalStroke()` still
returns 7.9196867493173935 against `C-0092`'s published 7.919686749317395. **Without the retention
the same file moves 53 of 1053 with a real change of 0.91, and two assertions of `C-0092`'s gate 5
become arithmetically false.**

---

## Deliverable 6 — `C-0033`'s collar, filed rather than assumed

`C-0092` records that the collar *"composes exactly at a fold and was not composed"*, and that where
there is no fold it cannot create one — *"a positive increment cannot make a tangent vanish"* — and
says **that argument should be filed rather than assumed.** Filed:

> *No fold on the traversed domain* means `k_c(s) + k_eff(s) > 0` at every stroke the path reaches;
> that is what a fold search failing to find a fold **is**. `C-0033`'s collar enters at a
> **force-pinned** point — the balance fixes `|F_es| = R(s) + P(g)A` — so its whole contribution to
> the coupled tangent is `+|F_es| d ln μ/dh`, the *level* of the force being absorbed into the bias
> (`CH-0069`). Where `d ln μ/dh > 0` that is a strictly positive increment to a strictly positive
> quantity, which cannot vanish. **No fold can appear.**

**And filing it made it conditional on a measurement, which is the point of filing it.** The premise
is not *"the collar is favourable"*, it is *"`d ln μ/dh > 0` over the gaps the path traverses"*, and
`C-0033` measured it at 80 records:

| | |
|---|---|
| `C-0033`'s measured `d ln μ/dh` records | **80** |
| non-positive among them | **0** |
| the range | **0.004555 to 0.029387 per nm** |
| the gaps they were measured over | **2.00 to 11.0 nm** |
| the gap at the corrected 10 nm path ceiling | **1.8696 nm** |
| **the extrapolation the argument needs** | **0.1304 nm below the lowest measured gap** |

So the argument is **sound** and its premise is **measured over all but the last 0.1304 nm** of the
extended domain. That sliver is stated, not hidden — and it is a place the device is destroyed long
before the model is (`C-0092`: 52 731 pN over the array).

---

## The five verification gates

Executed as **13 tests** in `src/test/kotlin/stability/DoublingLadderRepairTest.kt` and **5** in
`src/test/kotlin/anchoring/TwoSpringElasticaTest.kt`, each named for the gate it serves.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the element domain is a set of strokes in nm, ordered, with `pathStrokeCeiling = min(refusal, branch) − 0.01` to `1e−12` and `max_s\|φ\|` a rotation below `π/2`; six unphysical entry points throw (a four-step integrator, an empty row list, a negative ceiling, a zero tolerance, an empty gradient list, a negative gap); and on the element itself, a stroke past the branch is **refused** with *"branch"* in the message rather than answered off another one | **PASS** |
| **2 — limiting cases** | a domain correction of **zero** moves no row at all; a correction past **every** layer ceiling moves the element-owned rows **only**, and no corrected ceiling escapes its own layer; the collar argument **fails** when a measured gradient is not positive and is **conditional** when the ceiling's gap is outside the measured range | **PASS** |
| **3 — symmetry and conservation** | the containment as an identity, row by row: `corrected = min(layer, element)` to `1e−12` and never below the published value; the census **partitions** all 108 into 54 / 36 / 18 and the outstanding 6 are all 10 nm / 10 mM; a repricing of the table **into itself** moves nothing and recovers `CH-0099`'s 8 inflated states; and on the element, the continued branch is **ascending in both coordinates** at eight strokes to 8.0 nm, with the moment balance and the first integral holding at every one | **PASS** |
| **4 — numerical convergence** | the corrected domain at 200 / 400 / 800 / 1600 RK4 steps: the contour agrees to `1e−9` **and so does the located domain**, to `0.0` at the emitted precision — so what sets it is the `1e−6` nm bisection and the continuation's force-step budget, **not the discretisation**, and *asserting that it moves would be asserting on sub-`1e−9` noise*. `max_s\|φ\|` read **at** it does move, by `2.6e−7`, which is `CLAUDE.md`'s *a gradient converges more slowly than what it differences* on a quantity read at a located point. At every resolution the domain is past the ladder's 7.9196867 and inside the contour with `max_s\|φ\| < π/2`; the Kotlin and Python movement classifications agree term by term; **and the sweep count is asserted**, `1..209`, so a strategy that silently degenerated into bisection would fail a test rather than pass one (`C-0031`) | **PASS** |
| **5 — literature and upstream** | `T-149`'s own `elementCeilingSafety` and `armLength` read from its file and reproduced (the contour to `1e−14`); the corrected domain bracketed between `C-0084`'s published refusal and `C-0092`'s contour; `C-0084`'s element-boundary census (8 of 108, 18 element-owned, 12 at the recommended device) reproduced from its own file; and **the retained ladder still measures the artefact**, agreeing with the continuation to `1e−11` at 3 nm and to `1e−9` at 7 nm and refusing at 7.95 nm where the continuation does not | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | **the containment being wrong** — a row the cheap bound excludes moving its stroke ceiling, its ceiling owner, its binding ceiling, its bias margin, its fold stroke or its verdict | **no** | 0 of 90. The whole 108-row study was re-run anyway, precisely so this could be a measurement rather than an argument |
| **F2** | **the repair not being one** — a supremum at or below the ladder's 7.9196867 nm, or at or above the contour | **no** | 8.1404072 nm: **0.2207205 nm past** the ladder and **0.0239836 nm below** the contour, at every RK4 count from 200 to 1600 |
| **F3** | **a headline number moving** beyond its own emission precision | **YES, in a bounded way, and it caught two more copies of the defect** | Two files outside the three this repair is about moved a **decision**: `T-108` (`C-0050`), whose two elastica rows explain a refusal by a fold that is not there, and `T-99` (`C-0046`), whose binding lists are decided by a **tie** at the 10 pN allowable. **No headline figure moved anywhere**, and no verdict moved in either file — both are `CH-0112` |
| **F4** | **a fold in the extension** at the six outstanding states | **no** | No re-run 10 nm row reports a pull-in stroke; at 10 mM there is no operating point at all |
| **F5** | **the collar creating a fold** — a non-positive `d ln μ/dh` anywhere `C-0033` measured one | **no** | 0 of 80, over gaps of 2.00–11.0 nm |

**A result that was not anticipated.** The task was formulated to re-read 96 rows and the answer was
that 90 of them are excluded by a `min`. **The expensive half was then spent anyway** — and it was
worth it, not for the 90, but because it is what turned *"the containment is an argument"* into
*"the containment held at 90 of 90"*, and because it is what found `C-0039`'s two rows. A cheap
bound that removes the need for a calculation is not a reason to skip the calculation when the
calculation is also the only audit of the bound.

**A second one.** The repair is **faster than the defect**, 190 sweeps against 209, because a
continuation starts each rung where the last one ended. The safety was not paid for out of the study
budget; it was a saving.

**A third, and it is what `F3` firing bought.** The lost branch had been written down in **three**
vocabularies, not one — as a *fold* in a placement table, as a *model domain* in a ceiling taxonomy
that a challenge was then raised about, and as a *note* in a reach catalogue — and no reading of any
one of them could have found the other two. A **fourth** record moved for an unrelated reason and is
the sharper lesson: `C-0046`'s binding-constraint lists are decided by a comparison of
`forcePerPathAtWorking = 10.0` against a 10 pN allowable, which `C-0017`'s mandate makes a **tie by
construction**, and a last-ulp change flips it. Two of its sixty rows change which constraints bind
while **no emitted number of either row moves at all**.

---

## Validity range

- **TRL 1–3. Nothing here is measured and the motif is not demonstrated.** `C-0055`'s 62 recorded
  queries stand and are upstream of the element itself.
- **The CONTOUR bound needs no validity range** — it is `C-0092`'s, a bound on an integral of a
  bounded function. Everything else here does.
- **The corrected DOMAIN is a property of the CONTINUATION, not of the elastica and not of the
  integrator.** Over 200 → 1600 RK4 steps it does not move at the emitted precision at all; what
  sets it is the `1e−6` nm bisection `loadLineStrokeCeiling` runs and the force-step budget the
  continuation exhausts as the reaction diverges toward the contour. `C-0092`'s **rotation**-marched
  continuation reaches **8.1610821 nm** where this **force**-marched one reaches 8.1404072 — a
  0.0207 nm difference of method, at a `max_s\|φ\|` of 1.5688653 against 1.5707924, both below a
  right angle and both strictly inside the contour. **Neither is the elastica's own boundary**, and
  the only statement that is, is `C-0092`'s theorem.
- **Only the SMALL-ROTATION branch is continued.** `C-0092` enumerated the curled branches and
  found every one of them at a *smaller* stroke; they are **refused** here rather than returned, and
  that refusal *is* the repair.
- **Mean field, inherited whole.** `C-0005`'s one-loop correction is 123–214 % over this gap range.
- **The re-read rows are `C-0084`'s**, with its layer models, its field, its `L₀` convention and its
  six `C-0003` models unchanged. Nothing here re-opens them.
- **Nothing is copied back.** The re-run result files stay in the snapshot; the committed ones are
  the ones their claims were written on, and amending them is `CH-0112`'s business and a queued task.
- **`C-0033`'s collar is still NOT composed into any path.** What is filed is the argument that it
  cannot create a fold, with its premise checked.
- **1-D, static, tile mean under a uniform load** — the same choices `C-0018`, `C-0032`, `C-0084`
  and `C-0092` made.

---

## Numbers that are CITED rather than DERIVED here

| number | value | flag |
|---|---|---|
| `C-0084`'s ladder refusal and path ceiling | 7.91968584; 7.909685836937754 nm | **CITED**, and the first **re-measured** here through the retained `BranchStrategy.DOUBLING_LADDER` |
| `C-0084`'s 108 fold rows and its `elementCeilingSafety` | — | **READ** from `gpd/results/T-149-recommended-element-fold.json` |
| `C-0092`'s contour bound and continuation supremum | 8.164390826631303; 8.1610821 nm | **CITED**; the contour **RE-DERIVED** here |
| `C-0033`'s measured `d ln μ/dh` | 80 records | **READ** from `gpd/results/T-60-collar-on-the-equilibrium-path.json` |
| `CH-0099`'s 2.567–3.740× at 8 states | — | **CITED**, and recomputed here from `C-0084`'s own paired fields |
| `C-0055`'s crossover and `C-0034`'s `A2` | 13.5294118; 78.2352941 pN·nm/rad | **CITED** |
| duplex `EI` | 230 pN·nm² | **CanDo MODEL INPUT, not a measurement** |
| §3's targets | 100 pN, 3 / 10 nm, 40 × 40 nm, 5–10 nm layer, 0.5/2/10 mM | **CITED** |

---

## Still open — named, not answered

1. **The window between the corrected refusal and the contour**, **0.0239836 nm**. It is a
   **continuation** limit and not a physical one — it does not move with the RK4 step count at all —
   and `C-0092`'s rotation-marched continuation closes 0.0207 nm more of it. What would close the
   rest is a continuation parametrised by something that stays finite as `δ → L`; neither can pass
   the contour.
2. **`stateAtForce` is unchanged** and still brackets its shooting parameter by doubling from a
   linear seed — at 112 pN on the Gen-1 arm that seed is already **8.14 rad**, five times a right
   angle. Its direct callers work where the residual has one root; the trap is still in the file
   for the next one.
3. **`elasticaArmForStiffness` still searches from a floor of `1.5 ×` the working stroke**, which is
   `CLAUDE.md`'s own recorded trap, and this task did not touch it.
4. **The committed result files of `T-79`, `T-149` and `T-157` are now the ones their claims were
   written on and not the ones the code produces.** That is deliberate and it is `CH-0112`'s
   business, but it is a state the repository should not stay in.
5. **The per-path allowable at the deep end of the corrected domain** — `C-0092`'s open item 5,
   unmoved: the element's *law* is defined to 8.14 nm and the *device* is not.
6. **The curled branches under a dynamic bias step.** Static only.

---

## Challenges

**Raises [`CH-0112`](../challenges/CH-0112-a-lost-branch-is-recorded-as-a-fold-and-as-a-ceiling.md).**

**Resolves `C-0092`'s open items 3 and 4**, and **discharges `CH-0099` in its consequence**.

**None stands against this claim.** The four ways it would fail:

1. **The continuation losing the branch in the other direction** — landing on a different branch
   that happens to be monotone. Guarded by the first integral, the moment balance, the strict
   monotonicity of both the stroke and the force, `max_s|φ| < π/2` at every row, and by the
   agreement with the retained ladder wherever the ladder is exact.
2. **A fold inside the last 0.024 nm.** Not excluded, and stated.
3. **The element being removed.** `C-0071` counts five routes that do that; two are inside published
   brackets. If the element goes, so does this repair's subject.
4. **`C-0005`'s one-loop correction**, larger than every margin here and evaluable by nobody in this
   programme.
