# T-327 — the resolution of the flatness census, and whether a verdict inside the model's own noise is a verdict

**Leaf** `A8.2`.
**Raised by** [`C-0221`](../claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md) (`T-326`) §5, which measured the census and opened this row.
**Claim to be filed** `C-0223`. **Challenge numbers reserved** `CH-0288`, `CH-0291`. **Queue rows reserved** `T-337`, `T-338`.
**Owned this iteration** `tools/T-327-*.py`, [`gpd/data/T-327-cheap-bound/`](../data/T-327-cheap-bound/README.md), the `T-327` result file Execute writes, and one hand-added handle in [`structure/ResultInputs.kt`](../../src/main/kotlin/structure/ResultInputs.kt).
*(The result path is deliberately not spelled here: `tools/check-result-path-references.py` is a pre-push gate on a NAMED artifact, and naming a file before Execute writes it turns a correct gate red for the length of an iteration.)*
**Re-emits nothing.** Every committed result file is READ. If the answer implies a re-emission it is `T-337`/`T-338`, not this turn's work.

---

## 0. Locked units, geometry and sign conventions

SI throughout, `k_BT = 4.142 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
Lengths nm, forces pN, stiffness pN/nm, pressure pN/nm² (= 1 MPa exactly).

Dishing is a **fraction of that tile's own free stroke**, on an `81 × 81` face grid, against `T-5b`'s `0.10` — a **convention**, not a physical threshold ([`C-0134`](../claims/C-0134-buildable-width-count-phase.md), and [`T-5b`](T-5b-tile-flatness.md)).
The corpus's coupled readings are taken over `4 000` Bernoulli staple-dropout realisations at `C-0087`'s measured incorporation, on **one common stream** per study, seed `197197` in the `10 × 6` family.

Two normalisations appear below and they must never be mixed, because mixing them is the defect §2a reports:

| symbol | definition | what it is |
|---|---|---|
| `margin` | `\|v − 0.10\|` | a distance in **stroke fractions** |
| `rel` | `\|v − 0.10\| / 0.10` | that distance **relative to the tolerance** — the census's own axis |
| `departure` | `\|fine − coarse\| / coarse` | a convergence step's movement **relative to the value** — every `convergence` record in the corpus |

For a reading `v` in `[0.09, 0.11]` the second and the third are commensurate to within `v/0.10 ∈ [0.9, 1.1]`.
The **first** is `0.10 ×` either of them.

---

## 1. The statement

`C-0221` §5 censused the eighteen committed result files carrying a `HoneycombDeflection` dishing and found **`1 146`** verdict-bearing readings in `[0.09, 0.11]`, of which **`99`** sit *"closer to `T-5b` than the convergence departure `C-0180` measured on this very lattice"*.
The row asks for one of two things:

- a **stated resolution** — the smallest margin at which a `p90OverStroke` verdict may be quoted, derived from the convergence departure of the quantity it is read on, with every reading inside it re-read as **undetermined**; or
- a **recorded refusal** saying why a verdict inside the model's own noise is still a verdict.

This task delivers the **resolution**, and it delivers it in a shape neither the row nor `C-0221` anticipated, because the cheap bound says the question is on the wrong axis:

> **`flatAtP90` is not a statement about a real number near a threshold. It is EXACTLY a binomial statement — *fewer than one realisation in ten exceeds the tolerance* — and its resolution is the sampling error of that proportion, which the corpus ALREADY EMITS beside every such cell as `exceedanceStandardError` and has never once compared against the threshold.**

Three things follow, and all three are decided by §2 with no solve at all.

---

## 2. THE CHEAP BOUND — four readings over the committed files, no JVM, no solve

Retained and runnable with no JVM and no third-party package in [`gpd/data/T-327-cheap-bound/`](../data/T-327-cheap-bound/README.md); its output at the commit this task was formulated at is beside it.

### 2a. The transferred threshold is a factor of ten out, and the census's own channel ordering says so

`C-0180` states its convergence result as *"a worst departure of `4.57e−4` against a margin of `0.00426` of the tolerance — **a factor of 9.3**"*.
Both numbers are in `rel`-like units: cell 69's margin is `0.10 − 0.0995744767 = 4.25523e−4` of the stroke, i.e. `4.2552e−3` of the tolerance, and its emitted subdivision `departure` is `0.00046` — from `(0.0996199888 − 0.0995744767)/0.0995744767 = 4.5701e−4`.
So `4.57e−4` is a departure **relative to the value**, not `4.57e−4` *of the stroke*.

`gpd/data/T-326-cheap-bound/margin-census.py` reads it as *"`4.57E-4` of the stroke"* and therefore enters it on the `rel` axis as **`4.57e−3`** — ten times too generous.
The mis-scaling is visible in the channel list itself with no code run at all: it places the convergence departure (`4.57e−3`) just **above** the flip margin (`4.2724e−3`), i.e. a ratio of `0.93`, where `C-0180`'s own sentence says the ratio is **`9.3`**.

Re-run commensurately, over the identical predicate and the identical eighteen files:

| threshold on `rel` | `C-0221` §5 | corrected |
|---|---|---|
| `4.57e−3` (as published) | **99** | — |
| `4.57e−4` (commensurate) | — | **2** |

**`99` is `2`.** The alarm as stated is a unit error, and `C-0221` §5's own headline sentence, the `T-327` queue row that quotes it, and the two channel rows of its table go with it (`CH-0288`).

### 2b. And `1 146` counts leaves, not verdicts

The census's predicate is *"a numeric leaf whose key ends `OverStroke` or contains `ishing`, **in a JSON object that also carries at least one boolean**"*.
The boolean test is on the **parent record**, not on the reading, so a diagnostic sitting beside a verdict is counted as one.
Broken down by leaf key, the `1 146` contains **`139` `medianOverStroke`**, **`139` `worstSingleRemovalOverStroke`**, **`66` `uncoupledDishingOverStroke`**, **`11` `p95OverStroke`** and **`11` `worstSinglePathRemovalOverStroke`** — none of which is the argument of any boolean in its own record.
The census's *tightest* reading after the corrected `T-294/cells/92` is `T-304/cells/97/medianOverStroke = 0.100029341`, and that cell's booleans are `flatAtNominal`, `flatAtP90` and `beatsUncoupledAtP90` — **not one of them is written on the median**.

### 2c. The resolution is the wrong QUANTITY, and the right one is already in the file

`coupling/DropoutRobustPlacement.kt` defines, on one sample of `n` realisations:

```
exceedance   = count { it > tolerance } / n
p90          = orderStatistic(sample, 0.90) = sorted[ceil(0.9n) − 1]
flatAtP90    = p90 < tolerance
```

For `n = 4 000` that is `sorted[3599]`, so `flatAtP90` holds **iff at least `3 600` realisations are below the tolerance**, i.e. **iff `exceedance ≤ 400/4000 = 0.10`** exactly (ties are measure zero on a continuous field).
Checked against the corpus rather than argued: the identity holds at **`1 440` of `1 440`** records that carry both, in five files, including **both** of `T-294`'s two booleans.

So the verdict is a hypothesis test on a binomial proportion, `X ~ Binomial(4 000, p_exceed)` against `p₀ = 0.10`, and it needs **no density estimate, no quantile convention and no solve**.
Its standard error at the threshold is `√(0.1 × 0.9/4000) = 4.743416e−3` in exceedance units, i.e. **`18.97` realisations** out of `4 000` — so a two-sided `95 %` interval is `400 ± 37.2` counts.

Read at `C-0180`'s two recovered cells — the cells the `2 of 64` headline, four claims and both customer documents rest on:

| | cell 69 (30 paths) | cell 109 (50 paths) |
|---|---|---|
| `p90OverStroke` | `0.0995744767` | `0.0998791032` |
| margin, of the stroke | `4.25523e−4` | `1.20897e−4` |
| emitted `exceedance` | `0.098` | `0.0995` |
| realisations over tolerance | **`392` of `4 000`** | **`398` of `4 000`** |
| against | `400` | `400` |
| exact one-sided binomial `p` | **`0.349`** | **`0.471`** |
| exact two-sided binomial `p` | `0.697` | `0.943` |
| emitted subdivision `departure` | `4.57e−4` (`4.55e−5` of the stroke) | `1.0e−4` (`1.01e−5` of the stroke) |
| margin **over the discretisation departure** | **`9.3497×`** — `C-0180`'s own factor of `9.3` | **`11.9677×`** |
| margin **over the binomial σ** | **`0.422`** | **`0.105`** |

**The recovery that reverses `C-0167`'s `0 of 64` is eight realisations out of four thousand at one cell and two at the other.**
The last two rows are the whole finding and neither needs a density estimate: on the **discretisation** axis the margin is `9.3497×` and `11.9677×` the noise and the cell is converged, exactly as `C-0180` says; on the **sampling** axis the same margin is `0.4 σ` and `0.1 σ`.
`ANSWERS.md` line 320 reads *"the tightest clears the tolerance by 0.426 % … and it is converged"* — true on the first axis, and a statement about the smaller of two uncertainties.

### 2d. But the ORDERING is resolved, and the corpus already emits the datum that says so

`T-279`'s own `paired` block carries `fractionTiedIsWorse` over the **same** `4 000` realisations, which is exactly a paired sign test.
At cell 69's comparison it is **`0.0365`** — the tie is flatter at **`3 854` of `4 000`** paired realisations — and at cell 109's it is `0.1305`, i.e. `3 478 of 4 000`.
Both are significant beyond any conventional level, where the *levels* are at `p = 0.349` and `p = 0.471`.

> **The ordering "the 59 raster turn ties make the tile flatter" survives; the level "the tied tile is flat" does not.**

That is the distinction the deliverable has to carry, and `C-0180`'s mechanism finding — the tie's worth, its median-of-ratios discipline, its `1.12×` correction of `CH-0227` — is untouched by any of this. What is withdrawn is a threshold crossing.

### 2e. What the cheap bound CANNOT decide

- **The 87 positive verdicts whose file carries no exceedance.** Of the corpus's **`2 678`** `flatAt*P90` booleans, **`1 440`** sit beside an emitted `exceedance` and **`1 238`** do not; of the **`106`** that read **true**, only **`19`** are testable from the file and **`87`** are not — among them `C-0212`'s `22 of 32` and `C-0215`'s `27 of 48`, which are live in both deliverables. For those the answer is a **recorded refusal plus a queue row**, and a partition by how close the `p90` sits to the tolerance, so the refusal does not over-claim.
- **Whether `C-0087`'s measured incorporation is itself resolved.** The binomial error priced here is the error of the ensemble at a *given* incorporation. The incorporation is a measurement with its own uncertainty and that is a strictly larger, separate term. It is named, not priced.
- **The nominal population, which is where the row's own axis does bite — and where the granularity rule of §5c bites back.** `T-294/cells/92/nominalCorrectedOverStroke = 0.10000102` is a zero-defect deterministic reading, so its resolution genuinely IS a discretisation departure. But `T-294`'s three convergence records are **all** on *"the corrected `p90` of the dropout ensemble"* and **none** is on the nominal, so the departure that would test it does not exist in the file. Read against the p90's `2.2e−4` anyway it is `21×` inside it — and that reading is exactly the transfer §2a caught, one cell across. What population B can be told at all, and on which readings, is `P10`, and it may come back mostly empty; an empty answer there is a **result**, not a gap.

---

## 3. Numeric targets

| | target |
|---|---|
| **P1** | the identity `flatAt*P90 ⟺ exceedance ≤ 0.10` is **derived** from `DropoutRobustPlacement.kt` and **checked** at every committed record carrying both — expected `1 440 of 1 440` agreeing, `0` disagreeing, over `T-263`, `T-279`, `T-294` (both booleans), `T-299`, `T-304` |
| **P2** | the realisation count `n` behind every such record is **backed out** of `exceedance` and `exceedanceStandardError` rather than assumed, and every record that omits the standard error is resolved against its study's own stated parameter; the census refuses a record whose `n` cannot be established |
| **P3** | `C-0221` §5's channel table is reproduced **exactly** at its own published thresholds — total `1 146`, tightest `0.100001020`, and `0 / 2 / 96 / 99 / 126 / 484` at `1e−5 / 5e−4 / 4.2724e−3 / 4.57e−3 / 6.7e−3 / 4.02e−2` — and then **re-stated commensurately**: the `4.57e−3` channel becomes `4.57e−4`, exactly `10×` tighter, and its count falls from **`99`** to **`2`** |
| **P4** | the `1 146` is partitioned **by leaf key**, and the count of readings that are the argument of a boolean in their own record is emitted beside it (the five diagnostic keys above total `366`) |
| **P5** | the corpus census of `flatAt*P90` booleans: `2 678` total, `1 440` with an emitted exceedance, `1 238` without; `106` reading **true**, `19` testable, `87` not |
| **P6** | for every testable record, the **exact Clopper-Pearson** two-sided interval on its exceedance and the **exact one-sided binomial p-value** against `p₀ = 0.10`; the normal-approximation `z` beside each, with their worst disagreement emitted |
| **P7** | **the stated resolution**, in the census's own emitted units: a `flatAtP90` verdict is `DETERMINED` iff the two-sided `95 %` Clopper-Pearson interval on its own exceedance excludes `0.10`, i.e. at `n = 4 000` iff the count is outside `[368, 432]` — with the count of `UNDETERMINED` readings emitted at `90 %`, `95 %` and `99 %` as the declared convergence axis |
| **P8** | at that resolution: how many of the `19` testable positive verdicts are `UNDETERMINED` (expected **`7`**), and by name, with **both** of `C-0180`'s recovered cells expected among them at one-sided `p = 0.349` and `p = 0.471`; and how many of the `1 421` negative ones (expected **`1`**). The declared sweep is expected to read `8 / 8 / 10` undetermined booleans at `90 / 95 / 99 %` |
| **P9** | the **paired** reading beside every level reading it exists for: `fractionTiedIsWorse` / `fractionSubjectIsWorse` / `realisationsWhereTheNumeratorWins` converted to an exact two-sided sign-test `p`, with `C-0180`'s two recovering comparisons expected at `3 854 / 4 000` and `3 478 / 4 000` — so the claim can state *ordering resolved, level not* with two numbers rather than an assertion |
| **P10** | the **nominal** population read on its own axis: every zero-defect `OverStroke` reading in `[0.09, 0.11]` against its own study's worst emitted **discretisation** departure on that quantity at that cell, with the granularity stated — a per-`(file, quantity, cell, axis)` match, never a per-file maximum, because `T-279`'s own worst is `0.018` on `C-0167`'s convergence cell, which `C-0180` explicitly says is not the deciding quantity |
| **P11** | the ratio *sampling error over discretisation error* at every record where both exist, with its range and with the caveat that converting between the two normalisations needs the tail density and is therefore the **only** number here that rests on the secant of §5a; expected `≈ 35×` and `≈ 113×` at `C-0180`'s two cells, and `> 1` everywhere. The density-free form of the same statement — margin over each noise separately, `9.3497× / 11.9677×` against `0.422 σ / 0.105 σ` — is the one the claim headlines |
| **P12** | the **refusal**, quantified: the `87` untestable positive verdicts partitioned by `\|p90 − 0.10\|/0.10`, so the claim says which of them could conceivably be at risk and which certainly are not, and `T-337` carries the one-field repair |

---

## 4. Falsifiers, declared before the run

| | falsifier | state |
|---|---|---|
| **F1** | `flatAt*P90 ⟺ exceedance ≤ 0.10` disagrees at **any** committed record — then §2c's identity is wrong, the binomial instrument does not apply, and the whole answer is void | **OPEN** |
| **F2** | the commensurate recount is **not** `2` at `4.57e−4`, or the published `99` is **not** reproduced at `4.57e−3` — then §2a has not located the defect and `CH-0288` must not be filed | **OPEN** |
| **F3** | some record's realisation count is **not** `4 000`, or cannot be backed out — then the single-`n` resolution of `P7` is not a corpus-wide constant and must be stated per record | **OPEN** |
| **F4** | the exact Clopper-Pearson verdict and the normal-approximation verdict **disagree** at any record — then the readable form may not be quoted and only the exact one may | **OPEN** |
| **F5** | **no** positive verdict is `UNDETERMINED` at `95 %` — then the corpus's flatness verdicts are resolved, the answer is the **recorded refusal**, and §2c's alarm is manufactured | **OPEN**, and it is **expected NOT to fire** |
| **F6** | the `UNDETERMINED` count is **not non-decreasing** in the confidence level over `90 / 95 / 99 %` — then the resolution is not a threshold on a nested family and the sweep is measuring something else | **OPEN** |
| **F7** | the sampling error is **smaller** than the discretisation departure at any record where both exist — then *"the corpus quotes the smaller of two uncertainties"* is false as a general statement and must be quoted per record | **OPEN** |
| **F8** | a paired sign test is **also** undetermined at one of `C-0180`'s two recovering comparisons — then the ordering does not survive either, and the finding is strictly worse than stated | **OPEN** |
| **F9** | the resolution as stated would mark a reading `UNDETERMINED` that its own study **also** reports as such — i.e. the corpus already does this somewhere and the row is asking for something that exists | **declared CLOSED**: `exceedanceStandardError` is emitted at `1 440` records and compared against `0.10` at **none**; `exceedanceOneSidedBound` is `null` at every unsaturated record by construction. Asserted as a named test over the corpus rather than argued |
| **F10** | any committed result file changes — this task must **re-emit none**, and the check is a checksum over `gpd/results/` taken before and after the run | **OPEN** |
| **F11** | two independent emissions of the `T-327` result file at a **pinned** `--ref` are not byte-identical, diffed **outside** the emitter (`CH-0281`, `CH-0246`) | **OPEN** |
| **F12** | any mutation of the new census predicate or of the binomial arm survives every named test, over a **subtracted** baseline (`CH-0237`), with the harness registered in `tools/P-31-harness-census.py`, wired, and stating its own row count (`C-0206`) | **OPEN** |
| **F13** | the `1 146` partition by leaf key does not sum to `1 146`, or the diagnostic classification disagrees with the booleans actually present in those records | **OPEN** |
| **F14** | the resolution rule, applied to the **negative** verdicts, marks more than a handful `UNDETERMINED` — then the asymmetry *"negatives are robust, positives are not"* is not the finding and the claim must be restated | **OPEN** |

---

## 5. Method, and its justification against cost

### 5a. There is no solve in this task, and that is the point

Every number above comes out of committed JSON plus arithmetic.
The three instruments — the binomial identity, the exact Clopper-Pearson interval and the exact sign test — are distribution-free and need neither a density estimate nor a re-run.
A route that *looked* obligatory and is refused on cost and on honesty: estimating the density at the 90th percentile from `(p95 − p90)` to get `SE(p90) = √(pq/n)/f`.
It works (it gives `1.577e−3` of the stroke at cell 69, `3.7×` the margin) and it is a **secant on a right-skewed tail**, so it under-estimates `f` and over-states `SE` — conservative in the *alarming* direction, which is the wrong direction for an alarm.
It is retained in the cheap-bound directory as a cross-check and no headline rests on it.

### 5b. What is built

| | | cost |
|---|---|---|
| `tools/T-327-flatness-resolution.py` | the census and the three instruments, with `--self-test`; **TDD, tests first** | ~2 h |
| `tools/T-327-emit-result.py` | the emitter, writing `T-327-the-resolution-of-the-flatness-census.json`; takes `--ref`, records the resolved sha as `baselineRef`, per `CH-0246` — this file's subject is the **corpus** | ~45 min |
| `tools/T-327-mutation-test.py` | the harness; registered in `P-31`'s `HARNESSES`, wired, subtracted baseline, row count stated | ~1 h |
| `gpd/data/T-327-cheap-bound/` | the three §2 readings, runnable with no JVM, with their output at the commit this was formulated at | ~30 min |
| one `T_327` handle in `structure/ResultInputs.kt` | **by hand**, never through the generator | 5 min |
| `gpd/claims/C-0223-*.md`, `gpd/challenges/CH-0288-*.md` | the claim and the challenge against `C-0221` §5's unit error | ~1.5 h |

**Shared-file hazard, stated before the edit.** Agent **V** is concurrently in `tools/`. `P-31`'s `HARNESSES` tuple is a shared edit and `build.gradle.kts` a shared registration; both go in **textually disjoint** hunks, and `git diff --cached --stat` is read before any commit (`CLAUDE.md`: *a bare `git commit` commits the whole index*).

### 5c. The granularity, stated before the run

`CLAUDE.md` is emphatic that *convergence is a property of the quantity* — and of the cell, and of the axis.
A resolution derived from one study's departure and applied to every reading is exactly the transfer this corpus keeps catching, and §2a is that transfer failing once already.
So the granularity is declared here and not chosen afterwards:

1. **Population A — ensemble order statistics.** Resolution = the binomial sampling error of that record's **own** exceedance at that record's **own** `n`. Per record. It happens to be one number because `n = 4 000` everywhere, and `P2`/`F3` make that a measurement rather than an assumption.
2. **Population B — nominal, zero-defect readings.** Resolution = that study's worst **discretisation** departure on **that quantity at that cell**. Per `(file, quantity, cell, axis)`. Never a per-file maximum.
3. **Population C — ensemble readings with no emitted exceedance.** **No resolution is derivable from the file.** Recorded as a refusal and partitioned by distance, not silently folded into A.

And three axes are excluded from the resolution by kind, with the reason stated: a **training-realisation** axis (`T-316`, `T-322`, `T-323`) is the *search*'s variance and not the verdict's; a **composite-fraction** axis (`T-322`) is a physical **bracket**, not a departure; a **penalty** axis (`T-297`) is a constraint's value and `C-0100`'s binary. Folding any of them into a numerical resolution is a category error.

### 5d. What would falsify this approach as a whole

If `flatAtP90` turns out **not** to be a function of the exceedance — a study using a different tolerance, a different quantile convention, or a weighted ensemble — the binomial instrument is inapplicable and the task falls back to the density route of §5a with its own uncertainty. `F1` is that test and it runs first.

If the corpus's verdicts are claims about **this sample** rather than about the population the sample estimates, the sampling error is not an uncertainty at all and the answer is the recorded refusal. That is a reading question, not a measurement, and it is argued in the claim against the corpus's own language (`C-0087`'s incorporation is a *measured distribution*; a design that is flat only on seed `197197` is not a design).

### 5e. Where this is expected to land, and what would change it

**Expected:** a stated resolution, not a refusal; the `99` corrected to `2` on the axis the row names; and a *different*, larger and one-sided finding on the axis the row does not name — that the corpus's positive flatness verdicts are the unresolved ones and its negative ones are robust.
The net effect on the programme is a **tightening**, not a reversal: everything that fails still fails, the two things that pass become *undetermined*, and every ordering stands.

**What would change it:** `F5` not firing (then the refusal is the answer); `F1` firing (then the instrument is wrong); or `F8` firing (then the ordering goes too and the finding is worse than stated).

---

## 6. Deliverable hand-off — reported, never edited

`ANSWERS.md` and `DECISIONS-FOR-NDI.md` are **not** edited by this task.
If the run lands where §5e expects, the following carry a level verdict this task withdraws to *undetermined*, and each is reported to the coordinator as an exact substitution with the sentence it replaces:

- `ANSWERS.md` line 320 — *"The tightest clears the tolerance by 0.426 % … and it is converged"*.
- `DECISIONS-FOR-NDI.md` line 1102 — **(1) The margin is 0.426 % of the tolerance**.
- `DECISIONS-FOR-NDI.md` lines 1005–1007 and 912 — the `2 of 64` recovery and its two cells.
- Decision row **7** and the two coupled-cell tables at lines 1178 and 1379, wherever a **positive** flat count is quoted.

`C-0212`'s `22 of 32` and `C-0215`'s `27 of 48` are **population C** and are reported as *not testable from the file*, with `T-337` carrying the repair — never as withdrawn.

---

## 7. Execute

Filed as [`C-0223`](../claims/C-0223-the-resolution-of-the-flatness-census.md).
Result: [`gpd/results/T-327-the-resolution-of-the-flatness-census.json`](../results/T-327-the-resolution-of-the-flatness-census.json), at `baselineRef` **`86b3bbd`** — the commit this Formulate and Plan were committed at, pinned rather than defaulted to `HEAD` (`CH-0246`), with the working-tree control **agreeing**.

### TDD, and the two watched failures

The tests were written before the implementation, in two passes, and both failures are recorded here
because a claim that says *"written first and watched fail"* is a statement about a run.

1. **The stub.** `tools/T-327-flatness-resolution.py` was first written with `binom_cdf` and three
   siblings as `raise NotImplementedError` and one self-test calling `binom_cdf(4000, 4000, 0.1)`.
   `--self-test` exited **1** with `NotImplementedError` from line 33.
2. **The whole suite against stubs.** All sixteen functions were then stubbed and the complete
   self-test block written against them — the census predicate, the identity, the realisation
   count, the determinacy, the axis kinds, the paired ordering and the three live-corpus arms.
   `--self-test` exited **1** at the first assertion, again `NotImplementedError`.

Only then was the implementation written, and all of it passed on its first real run — which is
unusual here and is explained the same way `C-0221` explains its own: the cheap bound had already
produced every constant the tests assert, in Python, before a line of the tool existed. Four
functions were added later (`nominal_population`, `_worst_nominal_discretisation`, and the emitter's
`_cross_axis` and `_exact_against_normal`), each the same way: tests first, `NameError`, then the
body.

### The twelve targets

| | |
|---|---|
| **P1** | **MET.** `flatAt*P90 ⟺ exceedance ≤ 0.10`, derived from `coupling/DropoutRobustPlacement.kt` and checked at **`1 440` of `1 440`** booleans over `1 184` records in five files, **`0`** disagreeing, both of `T-294`'s booleans included |
| **P2** | **MET.** Backed out of `exceedance` and `exceedanceStandardError` at every record that states one — `{4000}`, a single value — and a record stating none yields `None` rather than a guess |
| **P3** | **MET.** `1 146`, `0.100001020` at `T-294/cells/92`, and `0 / 2 / 96 / 99 / 126 / 484` reproduced exactly at `C-0221` §5's own thresholds; the `4.57e−3` channel restated at `4.57e−4`, exactly `10×` tighter, count **`2`** |
| **P4** | **MET.** `25` distinct leaf keys; the five diagnostic keys total **`366`** |
| **P5** | **MET.** `2 678` `flatAt*P90` booleans, `1 440` with an exceedance and `1 238` without; `106` reading true, `19` testable, `87` not |
| **P6** | **MET.** Exact Clopper-Pearson and exact one- and two-sided binomial `p` per record, computed without inverting the interval — `p₀ ≥ low ⟺ P(X ≥ x \| p₀) ≥ α`, asserted equal to the inverted form at seven points |
| **P7** | **MET.** The resolution is §4 of the claim; the undetermined band at `n = 4 000` and `95 %` is **`[363, 438]`** realisations. The sweep reads **`8 / 8 / 10`** undetermined booleans at `90 / 95 / 99 %` |
| **P8** | **MET.** **`7` of `19`** positive and **`1` of `1 421`** negative, by name, with both of `C-0180`'s cells among them at one-sided `p = 0.349` and `0.471` |
| **P9** | **MET.** The paired sign test beside every level reading it exists for: `3 854` and `3 478` of `4 000` at the two recovering comparisons, `p` below the double-precision floor and flagged as such rather than emitted as a bare `0.0` |
| **P10** | **MET, and nearly EMPTY, which is the result.** `145` nominal readings in range, **`131`** with no nominal discretisation axis in their own file, **`0`** of the remaining `14` undetermined |
| **P11** | **MET**, with the caveat carried: the ratio needs the tail density, so the claim headlines the density-free form — `9.3497×` and `11.9677×` against `0.422 σ` and `0.105 σ` |
| **P12** | **MET.** `87` untestable positive verdicts partitioned by file, `T-337` raised, and `C-0212`/`C-0215` named as refused rather than withdrawn |

### The fourteen falsifiers

`F9` was declared **CLOSED** and holds. `F5` was declared **OPEN and expected not to fire**, and did not — `7 of 19`, which is why the deliverable is a resolution and not a refusal. `F1`, `F2`, `F3`, `F6`, `F8`, `F13`, `F14` did not fire; **`F4`** (exact against the normal approximation) is `0` disagreements over `1 184` records, **`F7`** (is the sampling axis ever the better-resolved one) is `0` of the `11` records carrying both, and **`F12`** is `21` mutations with `0` survivors over a subtracted baseline. **`F10` and `F11` were discharged by measurement**: `0` pre-existing result files moved (`197 → 198`, one added, none removed), and two independent emissions byte-identical at `sha256 5eb07163…`, `cmp`-ed outside the emitter.

### What the run cost, and what it did not need

No JVM, no solve, no study run and no third-party package. `61` self-tests in `0.5 s`; the emitter's `14` in `6 s`; the mutation table's `21` rows in under a minute. The one thing the Plan predicted and got wrong in size is the mutation table: it needed **three** repairs on its first run and a **fourth** after `tools/T-295-mutation-input-census.py` refused it, and all four are retained in `C-0223` §8 — two are a fact about `str.count`, the third is a real gap in the tests, and the fourth is `C-0195`'s question answered: `1` of the `21` was held open by the corpus rather than by a fixture, and is now held open by a constructed one.

### The defect only the assembled-tree run could see

`tools/verify.sh` at the assembled `HEAD` came back **RED** on `:testFlatnessResolutionEmitter`, and it was mine.
The wiring was right and the self-test passed in the checkout; it failed **inside a snapshot**, because
`tools/snapshot.sh` excludes `./.git` and the self-test builds its document at a **pinned ref**.
`git rev-parse 86b3bbd` exits `128` where there is no repository.

Both remedies were open. **Degrading visibly was taken and unwiring refused, on a count**: only **2** of the
emitter's 18 named arms need the ref, so unwiring the task would have retired 16 live checks to avoid 2.
The two are skipped with a `stderr` notice — `stderr` because `--self-test > /dev/null` swallows stdout
(`C-0195`) — the document is built from the working tree instead, and the arm count drops `18 → 16`, which is
itself a signal. **The `--emit` path is deliberately not degraded**: it exits `2` rather than write a
corpus-subject file with a `null` `baselineRef` (`CH-0246`).

Verified by running, not by reading, in a `.git`-less snapshot built the way `tools/snapshot.sh` builds one:

| | in the checkout | in a `.git`-less snapshot |
|---|---|---|
| `T-327-flatness-resolution.py --self-test` | `61` arms, exit `0` | `61` arms, exit `0` — **no dependency** |
| `T-327-emit-result.py --self-test` | `18` arms, exit `0` | **`16` arms, exit `0`, 2 skipped loudly** |
| `T-327-mutation-test.py` | `21 / 0` survivors, exit `0` | `21 / 0` survivors, exit `0` — **no dependency** |

**The other two tasks were checked the same way rather than argued about, and neither has the
dependency**: the census reads `gpd/results/` and never `git`, and the harness copies `tools/` and
`gpd/results/` into its own fixture. The census does carry a visible skip of its own, but it fires
on a different condition — an **emptied** corpus, which is `tools/T-295-mutation-input-census.py`'s
treatment arm (`58` arms there) — and a snapshot has the corpus, only not the repository. **Two
skips, two conditions; neither stands in for the other**, which is why both were run rather than
reasoned about.
**And the guard's own named test failed on its first run, on a defect in the guard**: `git rev-parse <40-hex>`
validates an object name's *syntax* and not its existence, echoing an all-zero sha back at exit `0`, so the
first draft would have moved the crash to `git archive` rather than preventing it. Both the guard and the
resolver now use `rev-parse --verify --quiet <ref>^{commit}`. The emitted result file is **byte-identical**
across the whole repair.

### What was cut

The density route of §5a — `SE(p90) = √(pq/n)/f` with `f` estimated from `(p95 − p90)` — is retained in the cheap-bound directory as a cross-check and no headline rests on it, exactly as the Plan said. Nothing else was cut.
