# C-0131 — **both audits close, and both were narrower than the rules they measured.** The departure rule is now a **gate** reading `0 fields in 0 files` over 119 result files — but the mechanism `C-0129` put it in could not express it, in two independent ways, and its census counted **one spelling of four**: 199 fields in 27 files against **601 in 63**. The saturated statistic is repaired in the six remaining files **at their shared source** rather than at six emission sites, and the discharge it invalidates stands in **five claims**, not one

| | |
|---|---|
| **Task** | [`T-212`/`T-213`](../tasks/T-212-departure-and-saturation-audits.md), raised by [`C-0129`](C-0129-result-file-hygiene.md) (`T-209`, `T-210`) and [`CH-0153`](../challenges/CH-0153-a-statistical-power-gate-discharged-by-a-statistic-that-is-identically-zero.md) |
| **Leaf** | none for `T-212` — a **process** claim protecting the machine-readable artifact of every leaf; `A8.2` for `T-213`, whose six files are all flatness studies |
| **Verification type** | **logical** (two static censuses over the committed corpus, before and after, with self-tested discriminators) **+ in-silico** (35 studies re-run through **one** snapshot in `tools/reemission-order.py`'s order and diffed field by field against their **committed** version read out of `git`) |
| **Verdict** | **PASS on all nine predicates.** The departure audit is now a **GATE** and reads **0 fields in 0 files** over 121 result files, against `C-0129`'s 199 in 27; the saturated-proportion census reads **0 records in 0 files** against 277 in 6. **35 files re-emitted in one topological order: 250 departure fields moved, 342 one-sided bounds added, 0 verdicts, 0 wording changes, 0 booleans, and 0 computed physical quantities.** Falsifier **`F4` fired before a line was edited** and is answered by the mechanism change, not by an argument. `F1` did **not** fire: the one file whose non-departure numerics moved is settled by **three** runs — two of this task's code and one from a `--committed` snapshot |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a diagnostic or a precision |
| **Provenance** | `gpd/results/T-212-departure-and-saturation-audits.json`, emitted by `tools/T-212-emit-result.py`; `DEPARTURE_RECORDS`/`DEPARTURE_SPELLINGS`/`DEPARTURE_DIGITS_BY_KEY` and the record-qualified lookup in `src/main/kotlin/structure/ResultRounding.kt`; `roundedForActuatorResult` delegating in `src/main/kotlin/actuator/ActuatorResultRounding.kt`; `DropoutDishing.exceedanceOneSidedBound` in `src/main/kotlin/coupling/DropoutRobustPlacement.kt`; the extended checker `tools/check-result-file-hygiene.py` (**54** self-tests) and the batched runner `tools/study-batch.sh` |
| **Conditions** | The tree at `HEAD` of iteration 29 plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Consumes** | [`C-0129`](C-0129-result-file-hygiene.md) (both audits, both censuses and the re-emission discipline), [`C-0093`](C-0093-shared-body-coupling.md)/[`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (the two-digit rule and its partial applications), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (**a gate that cannot come clean is not a gate**), [`C-0117`](C-0117-reemission-order.md)/`CH-0131` (a re-emission sweep is a topological sort, not a list), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the dropout model and its gate 4), `P-18` (`RESULT_ABSOLUTE_FLOOR` is a claim in the locked units) |
| **Raises** | [`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md) against `C-0129`'s *"the rule now lives once, by name"* and its 27-file census, and [`CH-0155`](../challenges/CH-0155-the-degenerate-discharge-is-in-five-claims.md) against `CH-0153`'s scope |

---

## The claim, in one line

**Both audits close** — and both were **narrower than the rules they measured**,
which is `C-0129`'s own opening sentence (*"each named one instance, and each instance was a population"*)
read one level further out, against `C-0129`.

---

## 1. The cheap bounds, and three of the five changed the method

Every one is a walk of the committed corpus or a `grep` of `src/`. Together they cost under a minute, and they ran before a line was edited.

| bound | measured | what it decided |
|---|---|---|
| **1** — do the two audits' file sets overlap? | 27 files owe a departure repair, 6 owe a saturation repair, **3 are in both** | they are **one** sweep. Run separately, the three overlapping files are re-run twice and two topological sorts over overlapping sets can put a consumer of one before a producer of the other (`CH-0131`) |
| **2** — is the rule the map expresses the rule the claim states? | the leaf key `departure` appears under **eleven** parents; `T-193`'s is in **volts** | the one-line alternative — `DEPARTURE_DIGITS_BY_KEY` as the *default* of `roundedForResult`, 66 files instead of 27 — is **refused on a measurement**, and the mechanism is re-keyed on `record/spelling` |
| **3** — can the six files on the actuator path obey the rule at all? | `roundedForActuatorResult()` takes **no arguments** | `T-60` was not *"one re-run away"*; it was one re-run **and a change to a rounding implementation in another package** away, and that obligation pulls `T-3`, `T-4`, `T-76`, `T-149`, `T-157` into the sweep |
| **4** — how wide is the rule against the gate's predicate? | 199 fields in 27 files against **601 in 63** | the gate is wired on `C-0129`'s own predicate and the wider scope is **printed beside it, ungated** |
| **5** — which re-runs are cheap and which are heavy? | three studies timed before any edit, spanning the classes; the spread is ~170× | the sweep is affordable, and it is **one** snapshot with 35 runs inside it (`tools/study-batch.sh`) rather than 35 snapshots |

**Bound 2 is the one that matters most, because it is a falsifier that fired before the work started.**
`gpd/results/T-193-gold-electrode-pzc.json` carries

```json
{"versusStandardHydrogen": 0.51, "derivedVersusStandardHydrogen": 0.511420712,
 "departure": 0.001420712, "readStatus": "READ DIRECTLY — Adnan et al., PCCP 26:21419 (2024) …"}
```

— a difference of two **electrode potentials**, in the locked units, against a number read directly out of a paper.
Two significant digits there discard determined information about a literature comparison.
`C-0129`'s §7 anticipates the argument for an *order-one* departure and adopts the rule anyway;
it cannot anticipate a departure that is **not dimensionless at all**, because that possibility is invisible from inside a leaf-name framing.

## 2. Why `T-212` and `T-213` are one task, and it is not because they are both hygiene

That is the weak reason and it is `C-0129`'s reason for grouping its own three.
The load-bearing one is that **they are one re-emission sweep**: the file sets overlap in three files,
the order is a topological sort of **one** graph,
and the expensive half of `T-212` — the Monte Carlo studies — **is** the whole of `T-213`.

## 3. `T-212` — the rule now lives once **including in the package it could not reach**

`C-0129` put the rule in `structure/ResultRounding.kt` and wrote *"it now lives once, by name"*.
Two independent measurements say that was true of `structure/` and not of the tree, and both are `CH-0154`.

**It was keyed on a leaf name where the rule is about a record.**
`roundedForResult` applies a `digitsByKey` entry to the whole subtree under that key *wherever it appears*,
so a map keyed on `departure` reaches every one of the eleven parents the corpus uses —
including `T-193`'s volts and `T-160`'s own answer.
The map is now `record/spelling`, with **most specific wins**:

```kotlin
val DEPARTURE_RECORDS = setOf("reproductions", "convergence")
val DEPARTURE_SPELLINGS = setOf("departure", "relativeDeparture",
                                "departureFromFinest", "relativeDepartureInStroke")
val DEPARTURE_DIGITS_BY_KEY = /* the 8 `record/spelling` products */
```

`T-160` is the file where the distinction is not academic: it carries `relativeDeparture` **twice, meaning two different things** —
63 fields in `departures[*]`, which are its **answer** and are declared at six digits at its own emission site,
and 13 in `reproductions`/`convergence`, which are diagnostics.
The qualified key wins inside a departure record and the study's own unqualified `6` wins everywhere else.
There is no other way to write it.

**And `actuator/` could not carry the rule at all.**
`roundedForActuatorResult()` takes **no arguments**;
there is no `digitsByKey` to pass, so `T-3`, `T-4`, `T-60`, `T-76`, `T-149` and `T-157` could not have obeyed the rule by any edit at their own emission sites.
`T-60` is one of `C-0129`'s 27 and was *not* one re-run away.
It now delegates to `structure/`, which removes one of the **six** duplicate rounding implementations `CLAUDE.md` records —
and the delegation is a refactoring rather than a precision change only because the two constants are equal,
which is now **asserted in a test** so the equality cannot lapse silently.

## 4. `T-213` — the repair belongs on the summary, not on six emission sites

All seven affected studies build their dropout summary through **one** function, `coupling.summariseDropoutDishing`, returning **one** type.
So the instrument is one field:

```kotlin
val exceedanceOneSidedBound: Double?   // null where the proportion is not saturated
```

computed from `coupling.saturatedProportionBound` — the exact Clopper-Pearson limit at `x = n`, `p > (1 − c)^(1/n)`.
Six record classes then pass `summary.exceedanceOneSidedBound` and nothing else changes.

**`C-0129` repaired `T-148` at its emission site**, which is precisely the shape its own §3 diagnoses as the reason the departure rule survived three correct repairs.
The saturated statistic is the same class of defect and it was repaired the same way; here it is repaired at the source.

`T-191` is the one file needing a second decision: its **uncoupled reference** row has a deterministic exceedance
(the tile has no attachments to lose, so every order statistic is the nominal) and therefore **no sample to bound**.
It emits `null` with the reason beside it in the source.

## 5. What moved, measured against `git HEAD`

Thirty-five files, one snapshot, one topological order over the **whole** union set — `T-117`, `T-121`, `T-125`, `T-127`, `T-135`, `T-149`, `T-191`, `T-3`, `T-30`, `T-4`, `T-60`, `T-76`, `T-81`, `T-99`, `T-126`, `T-129`, `T-130`, `T-134`, `T-137`, `T-138`, `T-139`, `T-140`, `T-151`, `T-152`, `T-153`, `T-157`, `T-162`, `T-132`, `T-133`, `T-155`, `T-160`, `T-164`, `T-165`, `T-163`, `T-178`.
Every producer precedes every consumer of it; the sweep emitted **exactly** that set and nothing else.

| | departure fields | other numeric | prose (digits only) | prose (wording) | verdict / boolean | fields added |
|---|---|---|---|---|---|---|
| **all 35 files** | **250** | **10, all in one file** | 5 | **0** | **0** | **342** |

The 342 additions are `exceedanceOneSidedBound`, `null` at every unsaturated cell.
The five digits-only prose movements are `Double.toString()` inside a sentence —
`C-0127` measured 23 of 57 apparent decision changes to be exactly that, which is why the classifier strips digits before calling a prose change a verdict change.

### The two censuses, before and after

| census | before | after |
|---|---|---|
| the **gate**: `departure` inside a `reproductions`/`convergence` record | 199 fields in 27 files | **0 in 0** |
| the **rule**: all four spellings inside those records | 601 fields in 63 files | 351 in 31 |
| saturated proportions carrying only a symmetric error | 277 records in 6 files | **0 in 0** |

### And the sweep left nothing stale

The coordinator's cheap check, run as an identity rather than as a spot check:
**all 566** `reproductions`/`convergence` residual fields across the 35 re-emitted files are **exactly** the two-significant-digit rounding of their own committed value.
Not one moved for any other reason, so no consumer was re-emitted before its producer.
The worst residual in the set is unchanged in substance — `2.45` before, `2.5` after — and it is a literature cross-check, not a solver residual.

## 6. `F1` did not fire, and settling it took a **third** run

One file of the 35 — `T-129`, a minimax descent over 31 device subsets — moved 10 non-departure fields, worst **0.60 %**.
`C-0129`'s `F3` in a new place, and `CLAUDE.md`'s rule is to re-run identical code before attributing it.

| comparison | departures | other numeric | worst |
|---|---|---|---|
| run A (this task's code) vs the **committed** file | 10 | 13 | 0.0060 |
| **run A vs run B, identical code, separate snapshots** | **0** | 7 | 0.0014 |
| **a `--committed` snapshot — `HEAD`'s code, `HEAD`'s inputs — vs the committed file** | **0** | **13** | **0.0060** |
| that `HEAD`-code run vs run A | 10 | 7 | 0.00086 |

**Three independent runs agree on `ranges[1]` to the last digit and the committed file disagrees with all three.**
So `gpd/results/T-129-range-robust-placement.json` at `HEAD` **is not reproducible from `HEAD`'s own code**, and was not before this task began;
this sweep moves **zero** of that 0.60 %.
What is left over is 7 `subsets[*].minimaxWorstOverStroke` fields at ≤ `8.6e−4`, which is the descent-manifold irreproducibility `CLAUDE.md` already records.
It is queued as `T-215`; a control re-run that fires on the *baseline* rather than on the change is a finding, not a nuisance.

## 7. The gate, and what it deliberately does not enforce

`tools/check-result-file-hygiene.py --departures` now **exits 1** on its strict predicate, and is wired into `tools/verify.sh` beside the raw-conversion gate (its self-tests hang off `./gradlew test` as `testResultFileHygiene`, the established pattern for all six checkers, now **54** of them).

It prints **three** lines and gates on **one**:

```
GATE  (reproductions[*].departure, convergence[*].departure): 0 field(s) in 0 file(s)
scope (all four spellings in those records; REPORTED, not gated): 351 field(s) in 31 file(s)
wide  (any leaf key containing 'departure'; a ceiling on the class): 1149 field(s) in 46 file(s)
```

`C-0083`'s rule is a statement about a **predicate**, and a predicate can always be narrowed until the tree is clean.
Publishing the residue beside the gate is what stops the narrowing from becoming a claim of cleanliness, and it is why the scope line exists at all.
The 31-file residue is queued as `T-214` with its per-file counts.

**The saturated-proportion census stays an audit**, and for a different reason than `C-0129`'s: it is now clean, but it is not a defect a checker can *demand* be absent —
a study that reports no saturated proportion is clean under it trivially, and one that reports two hundred is clean the moment the bound is beside them.
It reports.

## 8. What this claim does not do

- It does not close the **rule**, only the gate's predicate: 351 fields in 31 files remain, listed per file in the result, queued as `T-214`.
- It does not re-grade any claim's gate 4. `CH-0155` raises whether the *statistical power* half was discharged; the *convergence* half of each of those five gates is untouched and is what they actually rest on.
- It asserts nothing about physics.
- The two-significant-digit rule remains a **convention**, conservative rather than exact, exactly as `C-0129` recorded. This claim narrows **where** it applies; it does not change what it is.
