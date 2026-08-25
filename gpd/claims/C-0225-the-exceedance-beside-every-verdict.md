# C-0225 — **`87` of the corpus's `106` positive flatness verdicts could not be tested against their own sampling error, and the datum was being COMPUTED AND THROWN AWAY one line before the verdict was written. Carried, `99` of the `106` are testable and `31` are `UNDETERMINED` against `C-0223`'s `7` — and `31` of the `32` at `p90 ≥ 0.0975` are, including SIX that sit at EXACTLY `400` of `4 000`, the tolerance itself.** `C-0212`'s `22 of 32` survives at **`19` DETERMINED** and `C-0215`'s `27 of 48` at **`26`**, which is what both deliverables lead with. The sweep is **purely additive at `7 of 7` files** — `0` moved leaves, including at both SEARCH studies, where `CLAUDE.md` says a descent lands on a manifold — and the cheap bound's `5` join-recovered readings are confirmed **exactly** by the runs

| | |
|---|---|
| **Task** | [`T-337`](../tasks/T-337-the-exceedance-beside-every-verdict.md) — the row [`C-0223`](C-0223-the-resolution-of-the-flatness-census.md) (`T-327`) §4b opened as a **recorded refusal** |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (seven coupled-cell studies re-emitted, `2 h 35 m` of study time) **+ logical** (the determinacy re-read is exact arithmetic on a proportion — no solve, no JVM, no third-party package) |
| **Verdict** | **PASS on `P1`–`P13`. One of the twelve declared falsifiers fired — `F6`, and it is a finding** (§4b): a SECOND arm of `T-327`'s emitter went red, and the arm's own subject got **stronger** rather than breaking. `F2`, which the Plan named as the largest risk in the row and expected to fire, did **not**, at `7` of `7` files. `F4` and `F5` were declared open and expected not to fire, and did not. The gate that **was** predicted to fire did; both are repaired under [`CH-0294`](../challenges/CH-0294-a-staleness-detector-cannot-be-a-gate.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** No solve, no seed, no tolerance, no geometry and no design changed. What moved is what a record **carries** |
| **Provenance** | [`gpd/results/T-337-the-exceedance-beside-every-verdict.json`](../results/T-337-the-exceedance-beside-every-verdict.json), written by [`tools/T-337-emit-result.py`](../../tools/T-337-emit-result.py) (**new**, `17` named arms) on [`tools/T-337-verdict-exceedance-census.py`](../../tools/T-337-verdict-exceedance-census.py) (**new**, **`38` gate-named self-tests written FIRST and watched fail** against `NotImplementedError` stubs). **Mutation-tested** by [`tools/T-337-mutation-test.py`](../../tools/T-337-mutation-test.py) (**new**, registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts`): **`14` mutations, `0` survivors, `0` anchor defects**, over a **subtracted** baseline (`CH-0237`) — and **`0` corpus-dependent**, measured by running the whole table a second time against an emptied corpus. The Kotlin gate is [`src/test/kotlin/coupling/VerdictExceedanceTest.kt`](../../src/test/kotlin/coupling/VerdictExceedanceTest.kt) (**new**, five gates), watched to fail at **`731`** records before a line of the carry was written. The corpus gate is `tools/T-337-verdict-exceedance-census.py --check`, wired in [`tools/verify.sh`](../../tools/verify.sh), **scoped** with its residue **printed ungated** (`C-0083`, `C-0129`). The cheap bound and the two verification instruments are retained and runnable with no JVM in [`gpd/data/T-337-cheap-bound/`](../data/T-337-cheap-bound/README.md). Kotlin edited: the seven studies' record classes and their `T<NNN>Graded` holders, plus a **hand-added** `T_337` handle in `structure/ResultInputs.kt` (never through the generator). Shared files edited in hunks textually disjoint from the concurrent agent's: `build.gradle.kts`, `tools/P-31-harness-census.py`, `tools/verify.sh`, `tools/T-327-emit-result.py` |
| **Conditions** | `T-5b`'s tolerance **`0.10`**, a **convention** and not a physical threshold. **`n = 4 000`** Bernoulli staple-dropout realisations at `C-0087`'s measured incorporation — **backed out** of `exceedance` and `exceedanceStandardError` at every record and reading `4 000` at every one of them, never assumed. Determinacy at the exact two-sided **`95 %`** Clopper-Pearson interval, swept over `90 / 95 / 99 %`; the undetermined band at `n = 4 000` is **`[363, 438]`** realisations. The BEFORE census is at **`a83171d`**, the commit this task's Formulate and Plan were committed at; the AFTER reading has no commit at emit time and is pinned **by content**, one `sha256` per input file |
| **Consumes** | [`C-0223`](C-0223-the-resolution-of-the-flatness-census.md) — the identity `flatAtP90 ⟺ exceedance ≤ 0.10`, its stated resolution, and its `87` and their per-file split, **reproduced member for member** at its own ref before anything moved; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) — the two recovered cells five further records transcribe; [`C-0087`](C-0087-position-dependent-staple-dropout.md) — the incorporation, whose own uncertainty is named and not priced; [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md) and [`C-0215`](C-0215-route-b-coupled-on-its-own-stations.md) — the two positive counts the deliverables lead with; [`C-0110`](C-0110-device-b-tall-gap.md), [`C-0117`](C-0117-reemission-order.md), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md), [`C-0161`](C-0161-mechanics-on-an-imported-design.md), [`C-0195`](C-0195-the-discriminating-input.md) |
| **Raises** | [`CH-0291`](../challenges/CH-0291-a-prior-is-not-a-bound.md) — `C-0223` §4b's *"a **bound** in the safe direction"* is a distributional **prior**, and this task measured both why it is not a bound and why it cannot be calibrated where it is wanted — and [`CH-0294`](../challenges/CH-0294-a-staleness-detector-cannot-be-a-gate.md) — a wired arm asserting that today's tree equals a pinned corpus, **raised and repaired in the same iteration**. Opens **`T-341`** (the second defect this task's own gate found: `680` records corpus-wide emit an exceedance and **not** its standard error; `384` repaired here, `296` residue) and **`T-342`** (`T-323`, the declared cut) |

---

## 1. The answer

Over `C-0223`'s own eighteen files, at the exact two-sided `95 %` Clopper-Pearson interval:

| | BEFORE, at `a83171d` | AFTER |
|---|---|---|
| positive flatness verdicts | `106` | `106` |
| **testable from their own record** | **`19`** | **`99`** |
| untestable — no exceedance emitted | **`87`** | **`7`** |
| **`UNDETERMINED`** | **`7`** | **`31`** |
| `DETERMINED` | `12` | `68` |
| marginal, `p90 ≥ 0.0975` | `32` | `32` |
| ... of which `UNDETERMINED` | `7` | **`31`** |

The seven that remain untestable are all `T-323`'s, the declared cut (§6), and **none of them is marginal**.

Swept over the confidence level the count is `31 / 31 / 34` at `90 / 95 / 99 %` — monotone, as nested intervals require (`F9`).

### 1a. Six positive verdicts sit at exactly the tolerance

The tightest reading a `flatAtP90` verdict admits is `exceedance = 0.10` **exactly** — `400` of `4 000` — because the identity is `≤` and not `<`. Six of the corpus's positive verdicts are there, one-sided binomial `p = 0.513312282`:

| file | record | `p90` |
|---|---|---|
| `T-297` | `/cells/12`, `/cells/15` | `0.099902616`, `0.099935374` |
| `T-303` | `/ladder/4`, `/ladder/10` | `0.099902616`, `0.099935374` |
| `T-303` | `/cells/173`, `/cells/273` | `0.099902616`, `0.099935374` |

Two physical solves, graded six times. **One realisation of four thousand separates each of them from *not flat*.**

### 1b. `C-0212`'s and `C-0215`'s counts, which is what the deliverables lead with

| | positive | `DETERMINED` | `UNDETERMINED` |
|---|---|---|---|
| `C-0212`, `T-316/cells` — *"`22 of 32`"* | `22` | **`19`** | `3` |
| `C-0215`, `T-322/cells` — *"`27 of 48`"* | `27` | **`26`** | `1` |
| `T-316/rungs` | `5` | `5` | `0` |
| `T-322/rungs` | `6` | `6` | `0` |

**Both counts survive**, and both now carry a third state. That is `T-338`'s hand-off and it is reported, not edited (§7).

### 1c. And `C-0223`'s asymmetry is upheld and sharpened

`31` of `99` positives are `UNDETERMINED` — **`31.3 %`** — against **`16`** of **`1 832`** testable negative **records**, **`0.87 %`**: a factor of **`36`**. Both sides are emitted, so the ratio needs no second file. `C-0223` measured `7 of 19` against `1 of 1 421` on a fifth of the population; the larger population says the same thing.

---

## 2. THE CHEAP BOUND, AND THE FIVE READINGS IT ANSWERED BEFORE ANY STUDY RAN

Retained in [`gpd/data/T-337-cheap-bound/`](../data/T-337-cheap-bound/README.md), no JVM, no third-party package. It reproduces `C-0223` §4b exactly — `2 678` booleans, `1 238` in a record with no exceedance, `106` positive, `87` of them population C, and the seven-file split `27 / 33 / 7 / 8 / 8 / 2 / 2` — **by construction rather than by accident**, because it imports `tools/T-327-flatness-resolution.py`'s own predicates instead of rewriting them.

Its load-bearing question was the scoping one: **how much is recoverable with no re-emission?**

- **Within the record: nothing.** Population C carries no second order statistic of the same sample.
- **Across the corpus: five.** A `p90` is a nine-digit continuous quantity, so an exact equality with a donor record that *does* carry an exceedance identifies the same solve. Joined over **all `1 934`** donor records at the baseline ref, **`5` of the `87`** match — all five also agreeing on `nominalOverStroke`, **`0`** ambiguous — and **all five read `UNDETERMINED`**. They are `C-0180`'s two recovered cells, graded again in `T-297` and `T-303`.

So **`82` needed a re-emission and `5` did not**, and `T-337`'s own emitter re-derives that pair (`5`, `82`) at the pinned ref as a named self-test.

**And the runs confirmed all five exactly** (`F7`, [`join-confirmed.py`](../data/T-337-cheap-bound/join-confirmed.py)): `0.0995`, `0.0980`, `0.0995`, `0.0980`, `0.0995` — predicted before the studies ran and matched to the last digit. **A cheap bound that predicts five values and is then validated by the expensive calculation is the strongest form the discipline takes.**

### 2a. The expected yield, measured before the budget was committed

`25` of the `87` sat at `p90 ≥ 0.0975`, and **all `20` positives of the four cheap studies were there and nothing else was** — which inverted the naive cost ordering. Calibrated on the `928` donors carrying both quantities: `7` of `7` in that band were inside the undetermined interval, `0` of `7` in `[0.09, 0.0975)`, and only **`5`** donors existed below `p90 = 0.09` against `48` readings there. The low-end transfer was therefore **refused on sample size** rather than offered — which is `CH-0291`.

Measured after: **`31` of the `32`** marginal positives are `UNDETERMINED`. The single exception, `T-322/cells/25`, has `p90 = 0.098214626` and an exceedance of `355 / 4 000` — **which is exactly why the `p90` is a prior and not a bound.**

---

## 3. `F2` WAS THE LARGEST RISK AND IT DID NOT FIRE, AT `7` OF `7` — INCLUDING BOTH SEARCHES

`CLAUDE.md` records that a descent lands on a **manifold** rather than on a point, and three of the seven studies are searches. So *"only the new fields moved"* was a **measurement** here, taken file by file with [`additive-diff.py`](../data/T-337-cheap-bound/additive-diff.py), which fails on any moved pre-existing leaf, any unexpected key, any removal and any list whose length moved:

| file | moved leaves | added | unexpected | removed | seconds |
|---|---|---|---|---|---|
| `T-279` | **`0`** | `384` | `0` | `0` | `371` |
| `T-299` | **`0`** | `432` | `0` | `0` | `953` (two runs) |
| `T-284` | **`0`** | `384` | `0` | `0` | `208` |
| `T-297` | **`0`** | `108` | `0` | `0` | `87` |
| `T-303` | **`0`** | `1 044` | `0` | `0` | `486` |
| **`T-316`** (search) | **`0`** | `111` | `0` | `0` | `3 123` |
| **`T-322`** (search) | **`0`** | `162` | `0` | `0` | `4 074` |

`F10` was discharged by an **actual second run**: `T-297` re-run in the same snapshot and `cmp`-ed against the first run's copy **outside** the study — byte-identical.

---

## 4. THE SECOND DEFECT, WHICH THIS TASK'S OWN GATE FOUND AND NOBODY HAD NAMED

Gate 3 of the Kotlin test demands that the ensemble size **back out** of the record rather than being assumed. On its first full-suite run it failed at **`384` records of `T-299`, which carry an `exceedance` and no `exceedanceStandardError`** — so `C-0223`'s instrument had been defaulting their `n` to `4 000` instead of reading it.

Censused corpus-wide: **`680` records in `6` files** emit an exceedance without its standard error.

| file | records |
|---|---|
| `T-299` | **`384`** — repaired here |
| `T-304` | `256` |
| `T-197` | `16` |
| `T-162` | `14` |
| `T-206` | `8` |
| `T-327` | `2` |

`DropoutSummary`'s own KDoc says *"a probability without one is not a result"*, and `C-0223` §5 rests part of its argument on that field being emitted beside the exceedance. **`384` are repaired here and `296` are the residue, with a row (`T-341`).** The repair cost one further run of `T-299` and moved `0` pre-existing leaves.

---

## 4b. `F6` FIRED, AND WHAT IT FOUND WAS A COUNT RATHER THAN A CLAIM

The Plan declared `F6` — *"a gate other than the PREDICTED `T-327-emit` working-tree control arm goes red"* — open and expected it not to fire. **It fired**, in the same emitter, at

```
ok("...the exact verdict and the normal approximation agree at every record",
   document["exactAgainstNormal"]["disagreements"] == 0
   and document["exactAgainstNormal"]["recordsCompared"] == 1184)
```

whose second conjunct is **the population as it stood at `86b3bbd`, asserted as an invariant**. Carrying the datum into `491` further records takes the comparison to **`1 931`** and the arm goes red — while `C-0223`'s own finding **gets stronger**: the exact Clopper-Pearson verdict and the normal approximation still disagree at **`0`** records, now over `1 931` instead of `1 184`.

**A count broke and a claim did not.** `CH-0182`'s *a census is dated by its premise set*, met on a **named test**; the repair keeps `disagreements == 0` as the gate and turns the frozen size into a **direction** (`>= 1184`), so a growing population passes and a shrinking one — a record that lost its exceedance, which would be a real defect — still fails. It is §4b of `CH-0294`, and the two arms are the same defect by two mechanisms.

**It is also the honest reading of what a falsifier on *"a gate nobody predicted"* is for**: the Plan predicted one arm of one file and the run found two.

---

## 5. WHAT THE STALE CENSUS WOULD HAVE GOT WRONG, AND THE CONSTRAINT NO CENSUS COULD SEE

`tools/reemission-order.py` reads the **committed** reader census, which was stale by **seven** studies — three of them this task's own. Derived fresh, the constraints inside the seven are **four**, not two:

`T-279 → T-284`, `T-297 → T-303`, **`T-316 → T-322`**, **`T-316 → T-323`**

and over all ten the graph is very nearly a chain. The stale census would have permitted `T-322` before `T-316`.

**And one constraint no census could have shown**: `T-303`'s `routeB` block **transcribes** `T-299`'s verdicts rather than grading its own, so `T-299` became a hard prerequisite for `T-303` carrying a proportion at all. `T-299` joined the committed set for that reason and **not for its yield — it has none**. Its transcription is asserted equal to `T-299`'s own `linkStiffness` block at `16` of `16` rows on all four fields.

### 5a. The consumers, contained by measurement rather than by assurance

Eleven studies read one of the ten files. **All eleven read with `parseToJsonElement` and a named `getValue`; `0` use `decodeFromString`.** A new key is therefore invisible to every consumer *by construction* — a containment argument about the consumers' **inputs**, which `CH-0246` admits, and not an assurance about their outputs. `T-303`'s transcription is the one consumer that had to change, and it did.

---

## 6. WHAT WAS CUT, AND THE RATE THAT CUT IT — **no silent caps**

**`T-323` was the declared elastic and it was cut.** The measured rates:

| | positives | of which marginal | seconds | positives per minute |
|---|---|---|---|---|
| the five cheap studies | `20` | **`20`** | `2 105` | `0.57` |
| `T-316` | `27` | `3` | `3 123` | `0.52` |
| `T-322` | `33` | `2` | `4 074` | `0.49` |
| **`T-323`** | **`7`** | **`0`** | ≤ `18 000` declared | **`≤ 0.023`** |

The two searches alone were **`7 197` s of the `9 302` s** spent, and the five cheap studies **`2 105`** —
so the whole marginal population, which is where every determinacy flip is, cost under a quarter of the time. `T-323` declares a five-hour budget of its own, carries seven positives and **none** of the `32` at `p90 ≥ 0.0975` — the worst yield per minute in the set by two orders of magnitude. It is `T-342`, and its `7` verdicts are printed in the gate's residue on every run rather than being quietly absent.

`T-291`, `T-299`'s zero-positive siblings and `T-310` were the second elastic; `T-299` was pulled **in** by §5's transcription constraint and `T-291` and `T-310` were not run. Their `474` records carry `0` positive verdicts between them.

---

## 7. Deliverable hand-off — reported, never edited

This task owns no deliverable. Handed over, with what was verified:

- **`ANSWERS.md` and `DECISIONS-FOR-NDI.md` quote `C-0212`'s `22 of 32` and `C-0215`'s `27 of 48`.** Both counts **stand**; each now has a third state beside it — `19` and `26` `DETERMINED`. Verified: the counts are unchanged in the re-emitted files, and the determinacy is read from those files by the emitted result.
- **`C-0223`'s *"`87` … cannot be tested at all"* is superseded**: it is now `7`, all of them `T-323`'s. Verified by grep: the sentence appears in **neither** deliverable, so this is a correction at its source only.
- `T-338` is the row that applies all of this to the two documents, and it is deliberately downstream.

---

## 8. Validity range

- The binomial error priced here is the ensemble's error **at a given staple incorporation**. `C-0087`'s incorporation carries its own, strictly larger, uncertainty. Named and not priced — `C-0223`'s condition, inherited unchanged.
- The instrument applies to a verdict read as a claim about the **population** the `4 000` draws estimate. `C-0223` §5 argues that reading; it is an argument, not a measurement.
- **`T-323`'s `7` positive verdicts remain untestable and nothing here says whether they are flat.**
- **`296` records elsewhere in the corpus still emit an exceedance without its standard error**, so their `n` is assumed rather than backed out (`T-341`).
- The BEFORE census is dated by `a83171d`; the AFTER reading has no commit and is pinned by content. A verdict written outside a result record is invisible to both.
- `OrigamiGrillage`'s square-lattice studies are outside the ten and were not touched. Their `p90` verdicts are built by the same `summariseDropoutDishing`, so the same defect should exist there — that is an argument and not a measurement.
- The marginal band `p90 ≥ 0.0975` is a **convenience** for reporting expected yield. `T-322/cells/25` is inside it and `DETERMINED`, which is the band's own counter-example and is stated rather than hidden.

---

## 9. Open questions

- **`T-341`** — the `296` remaining records emitting an exceedance without its standard error, in five files. One-field carries, exactly as this row was.
- **`T-342`** — `T-323`, the cut: `7` positive verdicts, none marginal, one study run.
- **`T-338`** — the deliverables, with `UNDETERMINED` as a third state. It is now worth `31` verdicts where before this task it was worth `7`.
- Whether the six verdicts at exactly `400 / 4 000` should be reported as *flat* at all. The identity is `≤` because `sorted[3599] < 0.10` holds at `x = 400`; that is arithmetic, and whether a design that fails one realisation in four thousand short of the tolerance is a design NDI would accept is a question only NDI holds the column for.
