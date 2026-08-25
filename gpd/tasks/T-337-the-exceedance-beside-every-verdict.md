# T-337 — the exceedance beside every verdict, and the `87` positive flatness readings that cannot be tested without one

| | |
|---|---|
| **Leaf** | **`A8.2`** |
| **Raised by** | [`C-0223`](../claims/C-0223-the-resolution-of-the-flatness-census.md) (`T-327`) §4b — *population C is refused, with numbers, and is not withdrawn* |
| **Claim** | `C-0225` (reserved) |
| **Challenges** | `CH-0291`, `CH-0294` (reserved) |
| **New rows** | `T-341`, `T-342` (reserved) |
| **Verification type** | **in-silico** (seven coupled-cell studies re-emitted with one extra field per verdict-bearing record) **+ logical** (the exact Clopper-Pearson re-read, which is arithmetic on a proportion and needs no solve) |

---

## 0. Locked units, geometry and sign conventions

- SI throughout; lengths **nm**, forces **pN**, stiffness **pN/nm**, energies **k_BT** with `k_BT = 4.142 pN·nm` at **300 K**, aqueous buffer with the Mg²⁺ each study already names.
- **Dishing is dimensionless**, quoted as a fraction of the free stroke (`peakDishing / freeStroke`), and `T-5b`'s tolerance is **`0.10`** — a **convention**, not a physical threshold. Every one of the seven studies spells it `T<NNN>_TOLERANCE = 0.10`, verified below.
- **`n = 4 000`** Bernoulli staple-dropout realisations at `C-0087`'s measured incorporation. Every one of the seven studies declares `t<nnn>Realisations = 4000` with a `T<NNN>_SMOKE=1` switch dropping it to `150`, verified below. The realisation count is **backed out** of `exceedance` and `exceedanceStandardError` at every re-emitted record rather than assumed (`P4`).
- **`exceedance`** is `#{s > tolerance} / n`, a proportion in `[0, 1]`; `exceedanceStandardError` is `√(p̂(1−p̂)/n)`; both are already returned by `coupling/DropoutRobustPlacement.kt`'s `summariseDropoutDishing` and both are already emitted by five of the eighteen files.
- **Sign convention for a verdict**: `flatAtP90` is TRUE when the design is flat, i.e. when the `p90` order statistic is **below** the tolerance. `C-0223` §1c derives, and checks at `1 440` of `1 440` committed booleans with `0` disagreeing, that this is **exactly** `exceedance ≤ 0.10`.
- **Determinacy convention**, from `C-0223` §4 population A: a verdict is `DETERMINED` iff the exact two-sided Clopper-Pearson interval on its own record's exceedance **excludes** `0.10`. At `n = 4 000` and `95 %` the undetermined band is **`[363, 438]`** realisations, re-derived here from the census tool and agreeing.

---

## 1. The statement

**`87` of the corpus's `106` positive flatness verdicts cannot be tested against their own sampling error, because their record emits no `exceedance` — and the studies that wrote them already compute it and throw it away.**

`C-0223` established that a `flatAt*P90` verdict *is* the binomial statement `exceedance ≤ 0.10`, so its resolution is that proportion's sampling error. It then found that **`7` of the `19`** positive verdicts it could test are `UNDETERMINED` against **`1` of `1 421`** negative ones — the positives are the unresolved ones — and recorded a **refusal** for the other `87`, whose records carry no exceedance at all. That refusal is what this row lifts.

It matters because the `87` are not a tail. They contain [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md)'s **`22 of 32`** and [`C-0215`](../claims/C-0215-route-b-coupled-on-its-own-stations.md)'s **`27 of 48`**, which is what both deliverables now **lead** with — the first non-empty *flat and admissible* conjunction this corpus has had. Until an exceedance sits beside them, nobody can say whether those counts are `55` verdicts or `55` coin flips.

---

## 2. THE CHEAP BOUND — five readings over the committed files, no JVM and no solve

Every number in this section is read out of the committed corpus with `tools/T-327-flatness-resolution.py`'s own predicates, so the population is `C-0223`'s and not a new one. It is retained and runnable with no JVM and no third-party package in [`gpd/data/T-337-cheap-bound/`](../data/T-337-cheap-bound/README.md), with its output beside it.

### 2a. The population reproduces exactly, before anything is changed

Over `C-0223`'s eighteen files, with `_flat_p90_booleans` and `_records` **verbatim**:

| | this reading | `C-0223` §4b |
|---|---|---|
| `flatAt*P90` booleans | **`2 678`** | `2 678` |
| in a record carrying **no** exceedance | **`1 238`** | `1 238` |
| positive verdicts | **`106`** | `106` |
| positive **and** carrying no exceedance | **`87`** | `87` |

and per file, `T-316` **`27`**, `T-322` **`33`**, `T-323` **`7`**, `T-297` **`8`**, `T-303` **`8`**, `T-279` **`2`**, `T-284` **`2`** — `C-0223`'s list, member for member.

### 2b. Where they live, which is what prices the sweep

Population C is **seven** files with positives and **three** with none:

| file | records with no exceedance | positive | block |
|---|---|---|---|
| `T-316` | 37 | **27** | `/cells/*` 22, `/rungs/*` 5 |
| `T-322` | 54 | **33** | `/cells/*` 27, `/rungs/*` 6 |
| `T-323` | 17 | **7** | `/corners/*` |
| `T-297` | 36 | **8** | `/cells/*` |
| `T-303` | 348 | **8** | `/cells/*` 4, `/ladder/*` 4 |
| `T-279` | 128 | **2** | `/prestrained/*` |
| `T-284` | 128 | **2** | `/cells/*` |
| `T-291` | 128 | 0 | `/cells/*` |
| `T-299` | 16 | 0 | `/linkStiffness/*` |
| `T-310` | 346 | 0 | `/cells/*`, `/ladder/*`, `/routeB/*` |

**All ten studies already call `summariseDropoutDishing`** — `HoneycombTiedRegradeStudy` 4 call sites, `RasterTurnPrestrainSignStudy` 4, `RasterTurnTwistPriceStudy` 5, `CommonModeLinkStudy` 2, `HoneycombTetheredRegradeStudy` 5, `LinkStiffnessThresholdStudy` 2, `RadialLinkResolutionStudy` 2, `SearchedDistributionStudy` 2, `RouteBCoupledStudy` 2, `JointPlacementDistributionStudy` 2 — so the row's premise holds and the repair is a carry, not a computation. `SearchedDistributionStudy`'s is the clearest case: `t316Grade` builds a full `DropoutSummary` and returns `T316Graded(nominal, summary.p90, summary.flatAtP90)`, discarding the exceedance on the next line.

**All ten declare `TOLERANCE = 0.10` and `4000` grading realisations**, with a `SMOKE` switch, so the band `[363, 438]` applies uniformly and the rate is measurable in minutes before the budget is committed.

### 2c. **`5` of the `87` are recoverable with NO re-emission, and every one of them is `UNDETERMINED`**

The scoping paragraph asks whether the exceedance is *recoverable* from what a record already carries. Two routes were tried and the second works, thinly.

**Within the record: nothing.** Population C's records carry no second order statistic of the same sample — no `p95`, no worst, no count. The only exact implication of a committed `p90 < 0.10` is `x ≤ 400`, which **is** the boolean. So `C-0223` §4's *"none is derivable from the file"* is upheld at the record level.

**Across the corpus: five.** A record's `p90` is a nine-digit continuous quantity, so an exact equality between a population-C reading and a donor record that *does* carry an exceedance identifies the same solve. Joined over **every** committed result file (`1 934` donor records, `1 362` distinct `p90`), **`5` of the `87`** match, all five also agreeing on `nominalOverStroke`:

| population-C record | `p90OverStroke` | donors | `x` of `4 000` | determinacy |
|---|---|---|---|---|
| `T-297/cells/21` | `0.099879103` | `T-279/cells/109`, `T-299/cells/325` | `398` | **`UNDETERMINED`** |
| `T-303/ladder/5` | `0.099574477` | `T-279/cells/69`, `T-299/cells/205` | `392` | **`UNDETERMINED`** |
| `T-303/ladder/11` | `0.099879103` | `T-279/cells/109`, `T-299/cells/325` | `398` | **`UNDETERMINED`** |
| `T-303/cells/177` | `0.099574477` | `T-279/cells/69`, `T-299/cells/205` | `392` | **`UNDETERMINED`** |
| `T-303/cells/277` | `0.099879103` | `T-279/cells/109`, `T-299/cells/325` | `398` | **`UNDETERMINED`** |

The donors are `C-0180`'s **two recovered cells** — the `2 of 64` four claims and both deliverables carry. So those two physical solves are graded **nine** times across the corpus (four donor records plus these five), and every one of the nine is `UNDETERMINED`. **`82` of the `87` need a re-emission and `5` do not**, and the five are already answered by this section.

### 2d. The expected yield, measured before the budget is committed

A negative or null result over a sweep needs its expected yield (`CLAUDE.md`), so the `87` are partitioned by the `p90` their own boolean is read on — identified per record rather than assumed, at **`0`** identification failures over all `87`:

| `p90` band | positives | which files |
|---|---|---|
| `≥ 0.0975` | **`25`** | `T-297` 8, `T-303` 8, `T-279` 2, `T-284` 2, `T-316` 3, `T-322` 2 |
| `[0.0950, 0.0975)` | `6` | `T-316` 2, `T-322` 4 |
| `[0.0900, 0.0950)` | `8` | `T-316` 4, `T-322` 3, `T-323` 1 |
| `< 0.0900` | `48` | `T-316` 18, `T-322` 24, `T-323` 6 |

**Every one of the four cheap studies' `20` positives is marginal, and `48` of the `67` in the three expensive ones are not** — `T-297`, `T-303`, `T-279` and `T-284` contribute `20` of the `25` readings at `p90 ≥ 0.0975` and contribute **nothing** below it, which inverts the naive ordering by cost.

Calibrated on the `928` donor records that carry **both** a `p90OverStroke` and an exceedance:

| donor band | donors | inside the undetermined band `[0.09075, 0.1095]` |
|---|---|---|
| `p90 ∈ [0.0975, 0.10)` | `7` | **`7`** |
| `p90 ∈ [0.09, 0.0975)` | `7` | `0` |
| `p90 < 0.09` | **`5`** | `0`, max exceedance `0.0535` |

So the prior is sharp at the top of the range and **refused at the bottom on sample size**: `5` donors is not a calibration for `48` readings, and this task will not offer one. That refusal is itself a finding and is `CH-0291`'s ground.

### 2e. What the cheap bound cannot decide

- Whether the re-emitted exceedance **agrees with the committed boolean** at these seven studies. `C-0223` checked the identity at five files and argued — did not measure — that it should hold elsewhere. `P3`/`F1`.
- Whether a re-run **reproduces** the committed file in every pre-existing field. Three of the seven are **searches** (`T-316`, `T-322`, `T-323`) and `CLAUDE.md` records that a descent lands on a manifold. `P5`/`F2` — the single largest risk in this row.
- What the `82` actually read. That needs the runs.

---

## 3. Numeric targets

| | target | state |
|---|---|---|
| **`P1`** | `C-0223` §4b's population C reproduces before anything moves: `2 678` / `1 238` / `106` / `87`, and the seven-file split `27 / 33 / 7 / 8 / 8 / 2 / 2` | **met in §2a**, re-asserted as a test |
| **`P2`** | The recoverable-without-re-emission count is **stated with the routes tried**: `0` within-record, **`5`** by whole-corpus `p90` join, so **`82`** need a re-emission | **met in §2c**, re-asserted as a test |
| **`P3`** | `flatAtP90 ⟺ exceedance ≤ 0.10` at **every** newly carried exceedance, `0` disagreeing | OPEN |
| **`P4`** | `n` **backed out** of `exceedance` and `exceedanceStandardError` reads `4 000` at every re-emitted record, and the undetermined band is taken at each record's own `n` | OPEN |
| **`P5`** | A field-by-field diff of every re-emitted file against its committed version reports **`0`** moved pre-existing leaves and **only additions** | OPEN |
| **`P6`** | Every positive verdict reached is marked `DETERMINED` or `UNDETERMINED` at exact two-sided `95 %`, swept at `90 / 95 / 99 %`, with the count published per file | OPEN |
| **`P7`** | **Predicted before the run**: at least **`18`** of the `25` positives at `p90 ≥ 0.0975` read `UNDETERMINED` | OPEN |
| **`P8`** | `C-0212`'s `22 of 32` and `C-0215`'s `27 of 48` are re-read and the determined subcount published — that is what both deliverables lead with | OPEN |
| **`P9`** | Byte-identity of one re-emitted study over **an actual second run, diffed outside the study** | OPEN |
| **`P10`** | The corpus-wide restatement: of the `106` positive flatness verdicts, how many are now testable and how many are `UNDETERMINED` | OPEN |
| **`P11`** | Every downstream consumer of a re-emitted file is **re-run** (`C-0110`: a proof is not a substitute) and moves nothing but its own new fields | OPEN |
| **`P12`** | The five join-recovered readings are **confirmed by the re-emission** — the joined exceedance and the emitted one agree exactly | OPEN |
| **`P13`** | The residue is published with the per-file list: how many `flatAt*P90` booleans still carry no exceedance, and how many of those are positive | OPEN |

---

## 4. Falsifiers, declared before the run

| | statement | expectation |
|---|---|---|
| **`F1`** | The identity `flatAtP90 ⟺ exceedance ≤ 0.10` fails at any re-emitted record → `C-0223`'s instrument does not transfer to these studies, and the deliverable becomes a refusal naming the disagreement | **OPEN**, expected **not** to fire |
| **`F2`** | A re-emitted file moves any **pre-existing** leaf → the change is not a one-field carry, the study is not reproducible, and the `87` I re-read are not the committed verdicts | **OPEN**, and the one most likely to fire, on `T-316` / `T-322` / `T-323` |
| **`F3`** | Any re-emitted record backs out `n ≠ 4 000` → the band `[363, 438]` is not uniform and every reading must carry its own | **OPEN**, expected not to fire |
| **`F4`** | Fewer than **`12`** of the `25` marginal positives read `UNDETERMINED` → the `7 of 7` donor prior does not transfer and `n = 7` was not a calibration | **OPEN** |
| **`F5`** | **`0`** of the readings reached are `UNDETERMINED` → the sweep buys nothing, `C-0223`'s refusal should have stood, and this row's premise is wrong | **OPEN**, expected not to fire |
| **`F6`** | A gate other than the predicted `T-327-emit` working-tree control arm (§5e) goes red → the record-shape change has a consequence nobody predicted | **OPEN** |
| **`F7`** | Any of the five join-recovered exceedances disagrees with its re-emitted value → the `p90` join is not an identity and §2c is unsound | **OPEN**, expected not to fire |
| **`F8`** | A downstream consumer moves a field that is not one of the new ones | **OPEN** |
| **`F9`** | The undetermined count is **not** monotone non-decreasing in the confidence level | **CLOSED** by nested intervals, asserted anyway |
| **`F10`** | Two emissions of one re-emitted study are not byte-identical | **OPEN** |
| **`F11`** | The new census tool cannot come clean over its own declared scope → per `C-0083` it is not a gate, and it ships as an audit with the residue printed | **OPEN** |
| **`F12`** | The new tool fails inside a `.git`-less copy → the last-iteration red gate, repeated | **OPEN**, expected not to fire (the census reads `gpd/results/` and needs no repository) |

---

## 5. Method, and its justification against cost

### 5a. What is built

1. **The carry.** In each re-emitted study, the `DropoutSummary` that already exists at the point the `p90` and the boolean are taken is threaded three fields further — `exceedance`, `exceedanceStandardError`, and `exceedanceOneSidedBound` where the summary supplies one — into the same record that writes the `flatAt*P90` boolean. No solve changes, no seed changes, no tolerance changes.
2. **`tools/T-337-verdict-exceedance-census.py`** — the census and the re-read: population C before and after, the identity check, the backed-out `n`, the exact Clopper-Pearson determinacy per record, the sweep over `90 / 95 / 99 %`, the whole-corpus `p90` join of §2c, and `--check`, scoped to the files actually re-emitted, **with the residue printed ungated** (`C-0083`, `C-0129`). Reuses `tools/T-327-flatness-resolution.py`'s `clopper_pearson`, `determinacy`, `_records` and `_flat_p90_booleans` by import rather than reimplementing them.
3. **`tools/T-337-emit-result.py`** — the result file. Its subject is the **corpus**, so per `CH-0246` it must name the state it measured; but the state it measures does not exist as a commit at emit time, so it is named by **content**: a `sha256` per input result file, recorded in the document, with `baselineRef` explicitly `null` and the reason stated. That is strictly stronger than a ref and it works with no `.git`.
4. **`tools/T-337-mutation-test.py`** — registered in `tools/P-31-harness-census.py`, wired in `build.gradle.kts`, with a **subtracted baseline** (`CH-0237`), a printed row count and labels that are prefixes of its own mutation names (`C-0206`).
5. A hand-added `T_337` handle in `src/main/kotlin/structure/ResultInputs.kt`. **Never** through `tools/T-272-emit-result-inputs.py`.

### 5b. TDD

The tests come first and are watched to fail. Three kinds:

- **Kotlin**, per re-emitted study, in that study's existing test file: the record type carries the three fields and the boolean agrees with the exceedance at a constructed summary.
- **Python**, in the new tool's `--self-test`: the census predicates, the identity, the backed-out `n`, the Clopper-Pearson band, the join, and the `--check` scoping and residue — every one gate-named.
- **A corpus test that fails today**: `every re-emitted file's verdict-bearing records carry an exceedance` fails at `1 238` records before the sweep and at the residue after it, which is the watched failure.

### 5c. The order, derived and not inherited

`tools/reemission-order.py` reads the **committed** census, and that census is **stale** — it reports `7` studies missing, three of them mine (`SearchedDistributionStudy`, `RouteBCoupledStudy`, `JointPlacementDistributionStudy`). `CLAUDE.md` is explicit that a stale `P-22` silently mis-orders a sweep, so the census is **re-derived** before the order is trusted. Against the stale census the order is `T-279 → T-297 → T-316 → T-322 → T-323 → T-284 → T-303`, with two constraints (`T-279` before `T-284`, `T-297` before `T-303`); a direct grep also puts `T-291`'s emitter downstream of `T-284`'s file, which the census does not carry.

### 5d. The budget, the priority and the declared cut — **no silent caps**

Recorded costs: `T-316` ran **`55` minutes** at 32 cells; `T-322` was projected at **`80–90` minutes** at 48; `T-323` declared a **`5`-hour** elastic; `T-279` *"runs in minutes"*. A full sweep of the seven is therefore **`3`–`8` hours**, which does not fit an iteration on a contended box.

Priority is **yield per minute**, and §2d inverts the naive order:

| rank | file | positives | of which `≥ 0.0975` | estimated | status |
|---|---|---|---|---|---|
| 1 | `T-279` | 2 | 2 | minutes | **committed** |
| 2 | `T-297` | 8 | 8 | minutes | **committed** |
| 3 | `T-284` | 2 | 2 | minutes | **committed** |
| 4 | `T-303` | 8 | 8 | minutes | **committed** |
| 5 | `T-316` | 27 | 3 | ~55 min | **committed** |
| 6 | `T-322` | 33 | 2 | ~85 min | **committed** |
| 7 | `T-323` | 7 | 0 | ≤ 300 min | **DECLARED ELASTIC — cut first** |
| 8 | `T-291`, `T-299`, `T-310` | 0 | 0 | unknown | **DECLARED ELASTIC — bonus only** |

Ranks 1–6 reach **`80` of the `87`** and **`25` of the `25`** readings at `p90 ≥ 0.0975`; `T-323`, the cut, carries **`0`** of them. The **rate is measured by a smoke pass** (`T<NNN>_SMOKE=1`) before the full run of each, and the projected total is checked against a **`5`-hour** study budget; whatever is cut is cut **from the bottom of this table**, named in the claim with the measured rate that stopped it, and left as a row (`T-341`).

### 5e. What is expected to break, predicted before the run

`tools/T-327-emit-result.py`'s self-test arm **`T-327-emit the pinned reading and the working-tree control agree`** compares a document built at the pinned ref `86b3bbd` against one built from the **working tree**. A legitimate re-emission makes those disagree, so that wired arm is **predicted to fire** the moment the first file lands — and it is the *only* gate predicted to fire (`F6`).

That is not a defect in the re-emission and it is not a suppression case: an arm asserting *the working tree agrees with a pinned corpus* is a staleness gate that must fire the first time the corpus legitimately moves, so it cannot be a pass/fail arm at all. `CH-0294` is reserved for it. The repair keeps the reproducibility statement — **two builds at the same pinned ref are byte-identical**, which `F11` there already makes — and demotes the working-tree comparison to recorded **data**, which is what the emitted `baselineControl.agree` field already is. `T-327`'s own committed result file is **not** re-run: it is dated by `86b3bbd` and `CH-0246` forbids re-basing it onto today's corpus.

### 5f. Challenge candidates

- **`CH-0291`** — `C-0223` §4b calls *"a `p90` far below the tolerance implies an exceedance far below it"* **a bound in the safe direction**. It is not a bound; it is a distributional prior. The only exact implication of `p90 < 0.10` is `x ≤ 400`, which is the boolean itself, and the corpus offers **`5`** donor records below `p90 = 0.09` with which to calibrate `48` readings. Measured, not argued.
- **`CH-0294`** — the working-tree control arm above.

### 5h. The one place this row touches agent `X`'s

`T-336`'s arm C classifies a recorded count as `PINNED` or `UNPINNED` by the key it sits under, and `T-339` promotes it to build-failing. This task's answer is **necessarily** a working-tree reading at emit time: its subject is the corpus **after** the re-emission, and that state has no commit until the iteration closes. So the result file emits **two** blocks — an `atRef` census at the Formulate commit, which is `P1`'s `2 678 / 1 238 / 106 / 87` and *is* pinnable, and an `atThisPassesTree` block for the post-sweep re-read, declared unpinned under agent `X`'s own vocabulary rather than dressed as pinned, with a `sha256` per input result file so it is reproducible by content. That is `T-340`'s subject met from the producing side, and it is reported rather than worked around. Beyond `build.gradle.kts`, where both agents register `Exec` tasks in textually disjoint hunks, the two file sets do not meet.

### 5g. What would falsify this approach as a whole

If `F2` fires on `T-316` or `T-322` — if a re-run does not reproduce the committed file outside the new fields — then the re-emission does not carry an exceedance *beside the committed verdict*, it replaces the verdict. The honest deliverable then changes shape: the `87` stay refused, the finding becomes *the searched-distribution studies are not reproducible and their positive counts are one draw of a manifold*, and that is a larger and worse result than the one this row asks for. It would be reported as such and not worked around.

---

## 6. Deliverable hand-off — reported, never edited

This task does not own `ANSWERS.md`, `DECISIONS-FOR-NDI.md`, `TASKS.md`, `JOURNAL.md` or `CLAUDE.md`. Any passage that moves is handed over as an exact substitution with a statement of what was verified. The passages at risk are the ones quoting `C-0212`'s `22 of 32` and `C-0215`'s `27 of 48`, and `C-0223`'s *"`87` … cannot be tested at all"*.

`T-338` is deliberately downstream of this row and is not touched here.

---

## 7. Execute

### 7a. TDD, and the two watched failures

**The Kotlin gate first.** `src/test/kotlin/coupling/VerdictExceedanceTest.kt` — five gates over the re-emitted files: the datum is there, it agrees with the verdict (`C-0223`'s identity), the realisation count backs out of it, the exceedance is a whole count of the ensemble, and the gate really reads every file it claims to gate. Run before a line of the carry was written it reads **`5` tests completed, `1` failed**, and the failing one says

> `731` verdict-bearing record(s) carry no exceedance, so their verdict cannot be tested against its own sampling error: `T-279/prestrained/0`, …

`731 = 128 + 128 + 36 + 348 + 37 + 54`, which is the cheap bound's own per-file census of the six files gated at that moment.

**The Python census next.** `tools/T-337-verdict-exceedance-census.py` was written with its self-tests and `NotImplementedError` bodies, and its first run stops on the first stub. Implemented, it is **`36` gate-named self-tests, `0` failed**, and `--check` on the un-swept corpus exits **`1`** at **`747`** defects.

### 7b. The order, re-derived rather than inherited

`tools/reemission-order.py` reads the **committed** census, which is stale by **seven studies** — three of them this task's (`SearchedDistributionStudy`, `RouteBCoupledStudy`, `JointPlacementDistributionStudy`). Derived fresh from `tools/result-reader-census.py`'s own `census_of_tree`, the constraints inside the seven are **four**, not two:

`T-279 → T-284`, `T-297 → T-303`, `T-316 → T-322`, `T-316 → T-323`

and over all ten the graph is very nearly a **chain**. The stale census reports two constraints and would have permitted `T-322` before `T-316`.

**And one constraint the census could not have shown until the carry was written**: `T-303`'s `routeB` block *transcribes* `T-299`'s verdicts rather than grading its own, so `T-299` became a hard prerequisite for `T-303` being able to carry a proportion at all. `T-299` joined the committed set for that reason and not for its yield — it has **`0`** positive verdicts.

### 7c. What was run, and what it cost

Two snapshots, run in parallel: the four cheap studies plus `T-299` in one, the two searches in the other. Every re-emission was checked for additivity **before** any number was read out of it.

| | study | seconds | moved leaves | added | unexpected | removed |
|---|---|---|---|---|---|---|
| `T-279` | `tile.HoneycombTiedRegradeStudyKt` | `371` | **`0`** | `384` | `0` | `0` |
| `T-299` | `tile.HoneycombTetheredRegradeStudyKt` | `953` (two runs) | **`0`** | `432` | `0` | `0` |
| `T-284` | `tile.RasterTurnPrestrainSignStudyKt` | `208` | **`0`** | `384` | `0` | `0` |
| `T-297` | `tile.CommonModeLinkStudyKt` | `87` | **`0`** | `108` | `0` | `0` |
| `T-303` | `tile.LinkStiffnessThresholdStudyKt` | `486` | **`0`** | `1 044` | `0` | `0` |
| `T-316` | `tile.SearchedDistributionStudyKt` | `3 123` | **`0`** | `111` | `0` | `0` |
| `T-322` | `tile.RouteBCoupledStudyKt` | `4 074` | **`0`** | `162` | `0` | `0` |

**`9 302` s of study time, `7 197` of it the two searches.** `F2` did not fire anywhere.

**A `SMOKE` switch prices the ENSEMBLE and not the SEARCH.** Grading realisations fall `4 000 → 150` (`26.7×`) and *training* realisations only `120 → 40` (`3×`), so a search study has no cheap plumbing pass: `T-316`'s smoke was cut at `320` s having graded `8` of `32` cells, and the plumbing evidence came from the five studies that did smoke clean (`66 / 98 / 39 / 34 / 88` s) plus the compile. That decision is logged rather than silent.

**A second defect, found by this task's own gate.** Its third gate demands the ensemble size be *backed out* of the record rather than assumed, and it failed at `384` records of `T-299` carrying an `exceedance` and **no** `exceedanceStandardError`. Censused corpus-wide that is **`680` records in `6` files**; `384` are repaired here, `296` are residue with a row (`T-341`).

**`F6` fired**, on an arm nobody predicted — `T-327`'s `recordsCompared == 1184`, a population asserted as an invariant. See `C-0225` §4b and `CH-0294` §4b.

### 7d. What was cut, and the rate that cut it

**`T-323`, the declared elastic, cut from the bottom of §5d's table.**

| | positives | of which `p90 ≥ 0.0975` | seconds | positives per minute |
|---|---|---|---|---|
| the five cheap studies | `20` | **`20`** | `2 105` | `0.57` |
| `T-316` | `27` | `3` | `3 123` | `0.52` |
| `T-322` | `33` | `2` | `4 074` | `0.49` |
| **`T-323`** | **`7`** | **`0`** | ≤ `18 000` declared | **`≤ 0.023`** |

`T-323` declares a five-hour budget of its own, carries seven positive verdicts and **none** of the `32` marginal ones. Cutting it leaves `7` of the `106` positives untestable, all in one file, all printed in the gate's residue on every run. It is `T-342`.

`T-291` and `T-310` were the second elastic and were not run: `474` verdict-bearing records between them and **`0`** positive verdicts. `T-299` was pulled **in** rather than left out, for the transcription constraint in §7b and not for its yield.

