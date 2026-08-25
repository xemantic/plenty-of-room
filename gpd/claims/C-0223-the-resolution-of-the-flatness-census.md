# C-0223 — **A `flatAtP90` VERDICT IS NOT A REAL NUMBER NEAR A THRESHOLD, IT IS EXACTLY THE BINOMIAL STATEMENT `exceedance ≤ 0.10` — AT `1 440` OF `1 440` COMMITTED BOOLEANS, `0` DISAGREEING — SO ITS RESOLUTION IS THAT PROPORTION'S SAMPLING ERROR AND NOT THE DISCRETISATION DEPARTURE THE CORPUS QUOTES.** At the exact two-sided `95 %` Clopper-Pearson interval **`7` of the `19`** flatness verdicts in this corpus that read **flat** are `UNDETERMINED` and **`1` of the `1 421`** that read *not flat* is: **the positive verdicts are the unresolved ones and the negative ones are robust**, which matters because this programme's answer to §3 row (g) is a positive verdict. `C-0180`'s two recovered cells — the `2 of 64` four claims and both deliverables carry — are **`392` and `398` of `4 000` against `400`**, one-sided binomial `p` of **`0.349`** and **`0.471`**; the same margins are **`9.3497×`** and **`11.9677×`** the discretisation departure, which is what *"and it is converged"* reports. **AND THE ORDERING SURVIVES WHERE THE LEVEL DOES NOT**: the corpus's own `paired` blocks put the recovering comparisons at **`3 854`** and **`3 478` of `4 000`** paired realisations, sign tests below the double-precision floor. **AND THE `T-327` ROW'S OWN PREMISE IS A UNIT ERROR THE COORDINATOR PROPAGATED** — `C-0180`'s `4.57E-4` is a departure *relative to the value*, entered on a `\|v − 0.10\|/0.10` axis as `4.57E-3`, so **`99` is `2`** — and the `1 146` counts **leaves**, `366` of them diagnostics no boolean is written on (`CH-0288`). **On the axis the row names the answer is nearly EMPTY, and that is a result**: of `145` nominal readings in range, **`131`** have no nominal discretisation axis in their own file at all and **`0`** of the remaining `14` are undetermined. **`87` of the corpus's `106` positive flatness verdicts cannot be tested at all** because their record emits no exceedance — a recorded refusal with a row (`T-337`), never a withdrawal

| | |
|---|---|
| **Task** | [`T-327`](../tasks/T-327-the-resolution-of-the-flatness-census.md) — the row [`C-0221`](C-0221-the-fit-and-the-sample-in-one-reconstruction.md) (`T-326`) §5 opened |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (the identity is derived from `coupling/DropoutRobustPlacement.kt`'s own three lines and then checked against every committed record that carries both sides) **+ in-silico** (an exact Clopper-Pearson census over the eighteen files, with **no solve, no JVM and no third-party package** — the binomial tail is the regularised incomplete beta by continued fraction) |
| **Verdict** | **PASS on all twelve predicates.** `P1`–`P12` all met. `F9` was declared **CLOSED** and holds. **`F5` was declared OPEN and expected NOT to fire, and it did not** — that is §3, and it is why the answer is a stated resolution rather than a recorded refusal. `F1`, `F2`, `F3`, `F4`, `F6`, `F7`, `F8`, `F12`, `F13`, `F14` did not fire. **`F10` and `F11` were discharged by measurement rather than by assurance**: a `sha256` census over `gpd/results/` before and after the run reports **`0`** pre-existing files moved, `1` added, `0` removed; and two independent emissions are byte-identical, `cmp`-ed **outside** the emitter. **`CH-0288` is raised**; **`T-337` and `T-338` are opened** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Nothing here re-runs a study, moves a committed reading or changes a design. Every number is read out of the committed corpus and every instrument is exact arithmetic on a proportion |
| **Provenance** | [`gpd/results/T-327-the-resolution-of-the-flatness-census.json`](../results/T-327-the-resolution-of-the-flatness-census.json), written by [`tools/T-327-emit-result.py`](../../tools/T-327-emit-result.py) (**new**) on [`tools/T-327-flatness-resolution.py`](../../tools/T-327-flatness-resolution.py) (**new**). **`61` gate-named self-tests written FIRST and watched fail** — the stub run and the full suite against stubs are both recorded in the task file's Execute section — all passing on the first real run of the implementation. **Mutation-tested** by [`tools/T-327-mutation-test.py`](../../tools/T-327-mutation-test.py) (**new**, registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts`): **`21` mutations, `0` survivors, `0` anchor defects and — measured, not asserted — `0` corpus-dependent (`C-0195`)**, over a **subtracted** baseline (`CH-0237`) that runs the unmutated copy first and prints its named failures. Its first run had **`3`** defects, and `tools/T-295-mutation-input-census.py` then **refused** it for a fourth; all four are retained in §8: two anchors that occur twice because a 12-space line is a substring of a 16-space one, and one **surviving** mutation that found a real gap in the tests. Shared files edited, each in a hunk textually disjoint from the concurrent agent's: `tools/P-31-harness-census.py` (one `HARNESSES` row), `build.gradle.kts` (three `Exec` registrations and one `dependsOn` line), `src/main/kotlin/structure/ResultInputs.kt` (a `T_327` handle **by hand**, never through the generator). The cheap bound is retained and runnable with no JVM in [`gpd/data/T-327-cheap-bound/`](../data/T-327-cheap-bound/README.md). `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-result-path-references.py`, `check-cold-start-note.py`, `check-kotlin-format-strings.py`, `check-queue-vocabulary.py`, `cli_guard.py --check`, `check-entry-points.py`, `T-334-gate-census.py --check`, `P-31-harness-census.py --check` (`wired: 33 of 33`, `0` unresolved anchors), `T-336-pinned-count-census.py --check`, **`T-295-mutation-input-census.py --check`** (`472` mutations over `20` harnesses, **`472` fixture-backed, `0` corpus-dependent, `0` survivors, `0` revived**, `0` defects), `trace-answers.py` and `check-result-file-hygiene.py` (base, `--prose`, `--departures`, `--saturated`) are all clean. `./gradlew compileKotlin compileTestKotlin` is **BUILD SUCCESSFUL**, and all three wired `Exec` tasks run green by name **in the checkout and inside a `.git`-less snapshot**, which is the state `tools/verify.sh` runs them in and the one that caught this task's own defect (§8) |
| **Conditions** | The eighteen committed result files carrying a `HoneycombDeflection` dishing, at **`baselineRef` `86b3bbd`** — the commit this task's Formulate and Plan were committed at, **pinned rather than defaulted to `HEAD`** (`CH-0246`), with the working-tree reading emitted beside it as a control and **agreeing**. `T-5b`'s tolerance `0.10`, a **convention** and not a physical threshold. `n = 4 000` Bernoulli staple-dropout realisations at `C-0087`'s measured incorporation, **backed out** of `exceedance` and `exceedanceStandardError` rather than assumed and reading `4 000` at every record that states one. Resolution stated at a two-sided `95 %` exact Clopper-Pearson interval, swept over `90 / 95 / 99 %` |
| **Consumes** | [`C-0221`](C-0221-the-fit-and-the-sample-in-one-reconstruction.md) §5 — its census predicate **verbatim**, its `1 146`, its tightest reading and five of its six channels **reproduced exactly** before the sixth is corrected; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — its two recovered cells, its `0.426 %` margin and its `9.3`, all **read out of its own result file** rather than transcribed; [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md); [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md) and [`C-0215`](C-0215-route-b-coupled-on-its-own-stations.md) — named as **population C** and explicitly **not** withdrawn; [`C-0087`](C-0087-position-dependent-staple-dropout.md) — the incorporation whose own uncertainty is named and not priced; [`C-0169`](C-0169-crossover-vertical-compliance.md) — a penalty is a binary (`C-0100`'s rule, measured there), which is why a penalty axis may not enter a resolution |
| **Constrains** | **A stated resolution for the flatness census, in three populations with three different rules** (§4). **`7` of `19`** positive verdicts and **`1` of `1 421`** negative ones are `UNDETERMINED` and must be quoted as such. **No committed number moves and no design changes.** `C-0180`'s mechanism finding, its median-of-ratios discipline and its `1.12×` correction of `CH-0227` are untouched; what is withdrawn is a threshold **crossing**. **`CH-0288`** is raised against `C-0221` §5 and the five other carriers of its `99`. **`T-337`** (emit an exceedance beside every `p90` verdict — one field the studies already compute, converting `87` untestable positive verdicts into testable ones) and **`T-338`** (re-read the deliverables' marginal verdicts at the stated resolution) are opened |

---

## 1. THE CHEAP BOUND — four readings over the committed files, no solve, and two of them move the row's own premise

Retained and runnable with no JVM and no third-party package in [`gpd/data/T-327-cheap-bound/`](../data/T-327-cheap-bound/README.md), with its output at the commit this task was formulated at.

### 1a. The transferred threshold is a factor of ten out

`C-0180` §5 states *"a worst departure of `4.57e−4` against a margin of `0.00426` of the tolerance — a factor of 9.3"*, so `4.57e−4` is a departure **relative to the value**. Read out of `T-279`'s own convergence record rather than out of the sentence: `coarseValue = 0.0995744767`, `fineValue = 0.0996199888`, quotient `4.570659e−4`.

`C-0221` §5's census enters it, on an axis of `|v − 0.10| / 0.10`, as **`4.57e−3`**.

| threshold on the census's own axis | count |
|---|---|
| `4.57e−3`, as published | **99** |
| `4.57e−4`, commensurate | **2** |

**Visible in the census's own channel ordering with no code at all**: as published it places the departure *above* the flip margin `4.2724e−3`, i.e. the margin at **`0.935`** of the departure, where `C-0180`'s own sentence places it at `9.3`. `CH-0288`.

### 1b. And `1 146` counts leaves

The predicate's boolean test is on the **parent record**. `366` of the `1 146` are `medianOverStroke` (139), `worstSingleRemovalOverStroke` (139), `uncoupledDishingOverStroke` (66), `p95OverStroke` (11) and `worstSinglePathRemovalOverStroke` (11) — on none of which any boolean of their own record is written. The second-tightest reading, `T-304/cells/97/medianOverStroke`, sits in a record whose three booleans are `flatAtNominal`, `flatAtP90` and `beatsUncoupledAtP90`.

### 1c. The identity, which is the whole instrument

`coupling/DropoutRobustPlacement.kt`, three lines:

```
exceedance = sample.count { it > tolerance }.toDouble() / sample.size
p90        = orderStatistic(sample, 0.90)          // sorted[ceil(0.9 n) - 1]
flatAtP90  = p90 < tolerance
```

At `n = 4 000`, `sorted[3599] < 0.10` holds iff at least `3 600` realisations are below the tolerance, i.e. iff at most `400` are above it, i.e. **iff `exceedance ≤ 0.10`** — an equality, not an approximation, ties being measure zero on a continuous field.

**Checked rather than argued: `1 440` of `1 440` booleans over `1 184` records in five files, `0` disagreeing**, including both of `T-294`'s two booleans against its one exceedance.

### 1d. The ordering, from the corpus's own paired blocks

`fractionTiedIsWorse` over the **same** `4 000` realisations is exactly a paired sign test's input. At `C-0180`'s two recovering comparisons it is `0.0365` and `0.1305`, i.e. **`3 854`** and **`3 478`** wins of `4 000`, two-sided sign tests below the double-precision floor.

---

## 2. WHAT A `flatAtP90` VERDICT IS A FUNCTION OF, AND WHAT THAT COSTS

The verdict is a hypothesis test on a binomial proportion against `p₀ = 0.10`, whose standard error at the threshold is `√(0.1 × 0.9 / 4000) = 4.74341649e−3` — **`18.973666` realisations of `4 000`**.

| | cell 69, 30 paths | cell 109, 50 paths |
|---|---|---|
| `p90OverStroke` | `0.0995744767` | `0.0998791032` |
| margin, of the stroke | `0.0004255233` | `0.0001208968` |
| realisations over the tolerance | **`392` of `4 000`** | **`398` of `4 000`** |
| against | `400` | `400` |
| exact one-sided binomial `p` | **`0.348514492`** | **`0.471274768`** |
| exact two-sided binomial `p` | `0.697028984` | `0.942549536` |
| exact `95 %` Clopper-Pearson | `[0.088957408, 0.107634528]` | `[0.090393804, 0.109195854]` |
| margin over the **discretisation** departure | **`9.3497×`** | **`11.9677×`** |
| margin over the **binomial σ** | **`0.421637021`** | **`0.105409255`** |
| **determinacy** | **`UNDETERMINED`** | **`UNDETERMINED`** |

**The recovery that reverses `C-0167`'s `0 of 64` is eight realisations out of four thousand at one cell and two at the other.**

The last three rows are the finding and none of them needs a density estimate: on the **discretisation** axis the margin is `9.35×` and `11.97×` the noise and the cell is converged, exactly as `C-0180` says; on the **sampling** axis the same margin is `0.42 σ` and `0.11 σ`. `ANSWERS.md` line 320's *"and it is converged"* is true of the first axis and is a statement about the smaller of two uncertainties.

### 2a. The two axes, compared without a density

A reading's distance from the tolerance can be counted in its own noise on **either** axis, and those two counts are dimensionless, so their **ratio is a density-free comparison of resolving power** — which is the only form in which the two axes may be compared at all, the conversion between a `p90` movement and an exceedance movement needing the tail density.

Over the **`11`** records that carry both a discretisation step matched to their own `p90` and an exceedance:

| | |
|---|---|
| the sampling axis is the **worse resolved** one | **`11` of `11`** |
| how much worse | **`6.68608355×`** to **`458.035463×`** |
| at `C-0180`'s cell 69 | `9.3497` departures against `0.421637021 σ` — **`22.174699×`**, i.e. **`22.2×`** |
| at `C-0180`'s cell 109 | `11.9677` departures against `0.105409255 σ` — **`113.535845×`**, i.e. **`113.5×`** |

`F7` was declared open and did not fire: there is no record in this corpus where the sampling axis is the better-resolved one.
**The rounded pair `22.2×` and `113.5×` is stated here deliberately**, because that is the precision a deliverable sentence wants and `C-0080`'s third drift class is a number a synthesis quotes that no claim owns — `tools/trace-answers.py` duly read `113.5` as `ABSENT` in `ANSWERS.md` on the first run after this claim's own hand-off was applied, which is the check working on its author. **The corpus has been quoting the smaller of two uncertainties, everywhere it quotes one at all.**

And the readable form may be quoted beside the exact one: the exact Clopper-Pearson verdict and the normal-approximation verdict `|p̂ − 0.10| > 1.96 σ` agree at **`1 184` of `1 184`** records, `0` disagreeing (`F4`).

---

## 3. THE ASYMMETRY IS THE FINDING, AND IT RUNS THE WAY THAT MATTERS

At the exact two-sided `95 %` Clopper-Pearson interval, over the `1 440` booleans testable from their own record:

| | count | `UNDETERMINED` |
|---|---|---|
| reads **flat** | `19` | **`7`** |
| reads **not flat** | `1 421` | **`1`** |

The eight, by name, with the exact tails:

| file | record | over the tolerance | one-sided `p` | reads |
|---|---|---|---|---|
| `T-263` | `/cells/135` | `394` | `0.388` | flat |
| `T-263` | `/cells/137` | `411` | `0.288` | not flat |
| `T-263` | `/cells/155` | `372` | `0.072` | flat |
| `T-263` | `/cells/157` | `385` | `0.223` | flat |
| `T-279` | `/cells/69` | `392` | `0.349` | flat |
| `T-279` | `/cells/109` | `398` | `0.471` | flat |
| `T-299` | `/cells/205` | `392` | `0.349` | flat |
| `T-299` | `/cells/325` | `398` | `0.471` | flat |

**A negative flatness verdict is robust and a positive one is not**, and that is structural rather than a coincidence of this corpus: a design that fails does so by a wide margin, and a design that passes passes by a hair, because the passing ones are the ones somebody optimised up to the threshold. **It is the direction that matters to the customer**: this programme's answer to §3 row (g) is a **positive** verdict, and the `2 of 64` is the whole of it.

The count is stable across the declared convergence axis: **`7` positive undetermined at all three of `90 / 95 / 99 %`**, with the total moving `8 / 8 / 10` as the negatives come in.

**But the ORDERING survives.** `C-0180`'s mechanism finding — that the raster's own 59 turn ties make the tile flatter — is resolved at `3 854 / 4 000` and `3 478 / 4 000` paired realisations on the same stream. What is withdrawn is a **threshold crossing**, not a mechanism, and the honest summary of the corpus's coupled flatness answer becomes *"the ties help, measurably; no coupled cell is demonstrably flat"*.

---

## 4. THE STATED RESOLUTION, IN THREE POPULATIONS

`CLAUDE.md` is emphatic that *convergence is a property of the quantity* — and of the cell, and of the axis — so the granularity was declared in the Plan and not chosen after the run.

| | what | the resolution | granularity |
|---|---|---|---|
| **A** | an ensemble order statistic whose record emits its own `exceedance` | the exact two-sided Clopper-Pearson interval on that record's own exceedance must **exclude** `0.10`; at `n = 4 000` and `95 %` the undetermined band is **`[363, 438]`** realisations, i.e. an exceedance in `[0.09075, 0.1095]` | **per record**, at that record's own `n` |
| **B** | a nominal, zero-defect reading — the axis the `T-327` row itself names | its distance from the tolerance, relative to its own value, must exceed its own file's worst **DISCRETISATION** departure **on a NOMINAL quantity** | **per `(file, quantity)`**, never a per-file maximum |
| **C** | a `flatAt*P90` verdict whose record emits **no** exceedance | **none is derivable from the file** — a recorded refusal | — |

**Three axis kinds are excluded from a resolution by kind, with the reason stated, and the exclusion is executable rather than editorial.** Of the eighteen files' `115` convergence axes, `101` are `DISCRETISATION`, `11` `SEARCH` (a training- or screening-realisation count, a percentile descent's sweeps — the *search's* variance, not the verdict's), `2` `PARAMETER` (the composite fraction, a forced stagger — a physical **bracket**, not a departure) and `1` `PENALTY` (a constraint's value: `C-0100`'s binary). **`0` are `UNCLASSIFIED`**, and an unrecognised axis would be refused rather than guessed.

### 4a. On the row's own axis the answer is nearly empty, and that is a result

| | |
|---|---|
| nominal readings in `[0.09, 0.11]` carrying a `flatAtNominal` verdict | **`145`** |
| with **no** nominal discretisation axis in their own file | **`131`** — `T-294` 83, `T-299` 48 |
| testable | `14`, all `T-263`, against its worst nominal departure `0.00011` |
| **`UNDETERMINED`** | **`0`** |

The census's own tightest reading, `T-294/cells/92/nominalCorrectedOverStroke = 0.10000102`, is one of the `131`: `T-294`'s three convergence records are **all** on *"the corrected `p90` of the dropout ensemble"* and none is on a nominal. Read against the `p90`'s `2.2e−4` anyway it is `21×` inside it — and **that is exactly the transfer §1a caught, one cell across**, which is why the declared granularity refuses it.

### 4b. Population C is refused, with numbers, and is not withdrawn

Of the corpus's **`2 678`** `flatAt*P90` booleans, **`1 238`** carry no exceedance, **`87`** of them positive: `T-316` 27, `T-322` 33, `T-323` 7, `T-297` 8, `T-303` 8, `T-279` 2, `T-284` 2. Those include `C-0212`'s **`22 of 32`** and `C-0215`'s **`27 of 48`**, which are live in both deliverables.

**They are not withdrawn and they are not endorsed.** What can be said without the datum is a bound in the safe direction: a `p90` far below the tolerance implies an exceedance far below it, so the readings at risk are the marginal ones. `T-337` is the one-field repair — the studies already **compute** the exceedance; `summariseDropoutDishing` returns it and the searched-distribution studies do not carry it into their own cell records.

---

## 5. THE READING QUESTION, WHICH DECIDES WHETHER `7 OF 19` IS A CORRECTION OR A CATEGORY ERROR

A sampling error is an uncertainty only if the verdict is a claim about the **population** the sample estimates. If `flatAtP90` is a claim about *this* seeded 4 000-draw sample, then `X = 392` is not an estimate of anything, it is a fact, and there is no error to price.

**The corpus has been using the population reading, and nothing else is defensible.** Three arguments, and the third is the one that settles it.

1. **The ensemble is a model of fabrication, not a property of the model.** `C-0087`'s incorporation is a *measured distribution* over staples that are present or absent in a folded structure. A verdict about the sample would be a verdict about seed `197197` — a statement no bench can act on and no design owns.
2. **The corpus already prices sampling error on the same ensemble, on a different statistic.** `exceedanceStandardError` is `√(p̂(1−p̂)/n)`, emitted beside `1 440` of these very records, and its KDoc says *"a probability without one is not a result"*. `T-213`/`CH-0153` went further and added an exact one-sided Clopper-Pearson bound for the **saturated** case. So the machinery, the instrument and the intent are all present — applied to the exceedance as a *reported probability* and never to the exceedance as the *thing the verdict is a function of*.
3. **And the corpus's own practice is the paired reading.** `C-0180` compares two designs by the **median of the per-realisation ratio** over a common stream and explicitly refuses the ratio of two order statistics (`CLAUDE.md`: *a ratio of two ORDER STATISTICS is not the order statistic of the ratio*). Pairing is only worth doing if the ensemble carries noise that cancels — which is precisely the population reading. **A corpus that pairs its comparisons has already conceded that its levels have a sampling error**; it had simply never read one against a threshold.

So `7 of 19` is a correction, not a category error. What follows from the sample reading — that a design is "flat" because of the seed — is not a claim this programme has ever made or would want to.

---

## 6. WHAT DOES NOT MOVE

- **No committed result file.** `F10`, measured: a `sha256` census over all `197` files before the run and `198` after reports **`0`** pre-existing files changed, `0` removed, `1` added — this task's own.
- **No design, no geometry, no stiffness, no placement.** Nothing here re-runs a study.
- **`C-0180`'s convergence result.** Its `9.3` is reproduced at `9.3497` from its own artifact, its axes are the right axes for the quantity they were taken on, and `F8`'s *"a moved verdict survives its own convergence axes"* remains correctly not-fired **on the discretisation axis**.
- **Every negative flatness verdict but one**, `1 420` of `1 421`.
- **Every ordering.** The tie's worth, `C-0218`'s `15 × 4` ordering, `C-0212`'s and `C-0215`'s *searched beats transferred* — all of them are comparisons on a common stream and none is touched.

---

## 7. `F5` WAS DECLARED OPEN AND EXPECTED NOT TO FIRE, AND IT DID NOT

The Plan's `F5` reads: *"no positive verdict is `UNDETERMINED` at `95 %` — then the corpus's flatness verdicts are resolved, the answer is the recorded refusal, and §2c's alarm is manufactured."* It was declared **OPEN** and **expected not to fire**, one commit before the run, and it did not: `7` of `19`.

That is the reason the deliverable is a **stated resolution** and not a refusal. Had it fired, the honest artifact would have been a recorded refusal saying why a verdict inside the model's own noise is still a verdict — and the row explicitly offered that.

**Two falsifiers were discharged by an actual second run rather than by an assurance.** `F11`: two independent emissions of the result file, `cmp`-ed **outside** the emitter, are byte-identical at `sha256 5eb07163…`. `F10`: the checksum census above.

---

## 8. THE MUTATION TABLE'S FIRST RUN HAD THREE DEFECTS AND ALL THREE ARE RETAINED

`21` mutations, and the first run read **`2` anchor defects and `1` survivor**.

**The two anchors are one fact about Python's `str.count`.** The census's range guard `if not 0.09 <= value <= 0.11:` occurs at twelve spaces of indentation in `margin_census_of` and at sixteen in `nominal_population` — and the twelve-space line is a **substring** of the sixteen-space one, so an anchor written without a leading newline matches both. The harness caught it because it asserts `source.count(old) == 1` per row (`C-0185`), which is the half of a mutation harness that a reading of its output cannot supply.

**The survivor is the interesting one.** *"WIDEN alpha to the whole complement, so the interval is one-sided at twice the level"* — `alpha = (1 − c)/2` becoming `alpha = 1 − c` — failed **no** named test, because every reading the suite tested was far enough from the boundary that halving alpha did not move it, and the one test comparing `determinacy` against an inverted Clopper-Pearson interval uses the **same** alpha on both sides and therefore moves with it. The repair is a fixture, not a rule: at `x = 365`, `n = 4 000`, the lower tail is `0.0332` — between `0.025` and `0.05` — so the reading is `UNDETERMINED` at two-sided `95 %` and `DETERMINED` at two-sided `90 %`, and the mutation flips the first. `C-0161`'s *a mutation that fails nothing is the finding*, and `C-0204`'s *ask what makes the rule observable before writing the test*.

**And the corpus-dependence census refused it for a fourth reason, correctly.** `tools/T-295-mutation-input-census.py` runs every harness in a faithful copy of the tree and in one with every committed artifact outside `tools/` **emptied**, to ask whether a mutation is held open by a **fixture** or by **committed corpus state** (`C-0195`). This harness copies `gpd/results/` into its fixture, so in the treatment arm `result_documents` raised and the subject's baseline was not green — `control printed 21 rows and treatment 0`, a **REFUSAL** rather than a measurement. Repaired the way `C-0195` prescribes: the subject's three live-corpus arms now **skip visibly to stderr** when the results directory is absent **or empty**, which is the same fix `C-0195` records for a missing `.git`, and the treatment arm then ran. It found **`1` of the `21`** corpus-dependent — *"WIDEN the verdict booleans past the flat-at-p90 family"*, held open only by the live identity arm because no synthetic fixture carried a non-verdict boolean with `p90` in its name. **The repair is a constructed fixture and not a widened rule** (`C-0161`): `{"exceedance": 0.098, "flatAtP90": true, "beatsUncoupledAtP90": false}`, where the correct predicate tests one boolean and the widened one tests two and disagrees.

**And a fifth defect, in this claim's own draft, caught by the rule that exists for it.** Six of the numbers in §2 were typed at nine digits from a scratch run and **not one of those digits was in the artifact**: the one-sided `p` at cell 69 was written `0.348466861` where the emitted value is `0.348514492`. The emitted one is right — checked against an **exact rational sum** over `Fraction` arithmetic, agreeing to `9.4e−13` — so what had been written was a fabricated precision around a correct third digit. **`SESSION-PROMPT.md`'s *grep every headline number out of the artifact* found all six in one pass**, and the tool now carries that exact cross-check as a named test, so the continued fraction is not a third-party package's answer taken on trust either.

**And a sixth, found by `tools/verify.sh` at the assembled tree — the only run that could see it.** `testFlatnessResolutionEmitter` passed in the checkout and **failed inside a snapshot**, because `tools/snapshot.sh` excludes `./.git` and the self-test builds its document at a **pinned ref**: `git rev-parse 86b3bbd` exits 128 where there is no repository. Agent `V`'s `T-336`, in the same iteration and against the same constraint, had put its `git`-requiring arm behind a visible skip and said so in as many words; this emitter took the other branch and nothing caught it until the assembled run — the **fifth** such collision in four iterations. Repaired by degrading **visibly**: `repository_available()` decides, the document is built from the working tree instead, the two arms that need the ref are skipped with a `stderr` notice (so `--self-test > /dev/null` still shows it, `C-0195`), and the run reads `16` arms instead of `18`. Unwiring was the alternative and is the worse trade here: **only 2 of the 18 arms need the ref**, so unwiring would retire 16 live checks to avoid 2. **The `--emit` path is deliberately NOT degraded** — it exits `2` rather than write a corpus-subject file with a `null` `baselineRef`.

**And the guard's own named test failed on its first run, on a defect in the guard.** `git rev-parse <40-hex>` validates the **syntax** of an object name, not its existence: it echoes an all-zero sha straight back and exits `0`. So the first draft of `repository_available` would have reported a repository as available for a commit that is not in it, and the crash would simply have moved to `git archive`. Both the guard and `_resolve` now ask for `rev-parse --verify --quiet <ref>^{commit}`, and the test asserts refusal for a non-existent sha **and** for a malformed name. The emitted result file is **byte-identical** across the repair, so nothing this claim states rests on it.

Repaired, the table is **`21` mutations, `0` survivors, `0` anchor defects and `0` corpus-dependent**, over a subtracted baseline of `60` executed self-tests and `0` named failures.

---

## 9. Validity range

- The binomial error priced here is the error of the ensemble **at a given staple incorporation**. `C-0087`'s incorporation is itself a measurement with its own uncertainty, and that is a strictly **larger**, separate term. It is named and not priced.
- The instrument applies to a verdict read as a claim about the **population** the 4 000 draws estimate. §5 argues that reading against the corpus's own practice; it is an argument, not a measurement.
- **Population C is refused, not estimated.** `87` positive verdicts are untestable from their own files, and nothing in this claim says whether they are flat.
- **`F11`'s byte-identity is a REPOSITORY-DEPENDENT demonstration, and it was shown to be so by an assembled-tree run rather than by reading.** The emitter builds its pinned reading with `git archive`, and `tools/snapshot.sh` excludes `./.git` — so inside a snapshot the pinned build cannot happen at all. In the checkout `F11` compares two **pinned** builds and they are byte-identical; inside a snapshot the same arm compares two **working-tree** builds, which is a weaker statement, and the two arms that need the ref (*the resolved sha is recorded* and *the pinned reading and the working-tree control agree*) are **skipped, loudly, to `stderr`**, taking the run from 18 named arms to 16. So what `F11` demonstrates unconditionally is that the document is a deterministic function of the corpus it reads; that it is also a deterministic function of a **named** corpus state is demonstrated only where a repository is present. The `--emit` path does **not** degrade: it refuses with exit `2` rather than write a corpus-subject file carrying a `null` `baselineRef` (`CH-0246`).
- **The emitter's repository guard is covered by two named tests and by no mutation.** `tools/T-327-mutation-test.py` declares one subject in `tools/P-31-harness-census.py` — the census, `tools/T-327-flatness-resolution.py` — so the emitter's `repository_available` is asserted (in **both** states, so the degradation is asserted and not merely observed the day it fires) and is not mutation-covered. Stated rather than left to be discovered.
- The census is over the eighteen files' **committed state at `86b3bbd`** and is dated by it. A verdict written outside its own record is invisible to it.
- **Population B's match is per `(file, quantity)` and not per cell.** `T-263`'s single nominal departure is read at its own recommended cell and applied to that file's 14 readings. That is the loosest match this claim makes and it is stated rather than hidden; it is also the one that returns `0`, so nothing rests on it.
- The `1 421` negative verdicts include the same physical cell counted more than once where two files grade it (`T-279` and `T-299` share two records). The census is over **records as committed**, which is what a *"the corpus says"* statement is about; it is not a census of distinct physical designs.
- `OrigamiGrillage`'s square-lattice studies are outside the eighteen files and are not examined. Their `p90` verdicts are built by the same `summariseDropoutDishing`, so the identity should hold there too — that is an argument and not a measurement.

---

## 10. Open questions

- **`T-337`** — emit an `exceedance` beside every `p90` verdict. One field the studies already compute, and it converts `87` untestable positive verdicts into testable ones. It is the highest-value row this task opens, because `C-0212`'s and `C-0215`'s positive counts are what the deliverables now lead with.
- **`T-338`** — re-read the two deliverables' marginal verdicts at the stated resolution, carrying `UNDETERMINED` as a **third state** beside flat and not flat. `C-0083`'s *a boolean report cannot say "not applicable"*, met on a verdict rather than on a rule.
- Whether an **ordering** should be this corpus's primary flatness statement, given that it resolves where a level does not. That is a question about what §3 row (g) is asking for, and only NDI holds that column.
- Whether the same asymmetry holds on the square-lattice studies, where the same summary is computed and the same threshold is read.
