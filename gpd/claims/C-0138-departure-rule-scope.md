# C-0138 — **the departure gate is now the rule**: `351` fields in `31` files closed, on the `scope` line as well as the `GATE` line, and the repair is **one line in the layer every study goes through** rather than 31 emission sites. `C-0131` refused exactly that default on a measurement — and the measurement had been repaired **in the same task**, by `C-0131` itself, in the sentence after the refusal

| | |
|---|---|
| **Task** | [`T-214`](../tasks/T-214-departure-rule-scope.md), raised by [`C-0131`](C-0131-departure-and-saturation-audits.md) (`T-212`) and [`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md) |
| **Leaf** | none — a **process** claim protecting the machine-readable artifact of every leaf |
| **Verification type** | **logical** (a static census over the committed corpus before and after, an offline simulation of the mechanism change over every committed result file, 67 checker self-tests and 11 emitter self-tests) **+ in-silico** (31 studies re-run in **one** topological order through **one** snapshot and diffed field by field against their **committed** version read out of `git`, with two independent control re-runs of `HEAD`'s own code where a non-departure field moved) |
| **Verdict** | **PASS on all nine predicates.** `F2`, `F3`, `F4` and `F5` did **not** fire; **`F1` FIRED on two files of 31** and three control re-runs attribute both to `HEAD`'s own code — a **descent manifold** in one (two identical runs disagree in 217 fields) and **deterministic staleness** in the other (`HEAD` reproduces the same 11 movements) — so this sweep moves **zero** of either |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a diagnostic precision |
| **Provenance** | `gpd/results/T-214-departure-rule-scope.json`, emitted by [`tools/T-214-emit-result.py`](../../tools/T-214-emit-result.py) (11 self-tests) from [`tools/T-214-costs.json`](../../tools/T-214-costs.json) and [`tools/T-214-body.json`](../../tools/T-214-body.json); the retained order in [`tools/T-214-reemission-order.txt`](../../tools/T-214-reemission-order.txt); the mechanism in `src/main/kotlin/structure/ResultRounding.kt` with the delegations in `coupling/CouplingResultRounding.kt`, `window/WindowResultRounding.kt` and `brush/FluctuationCorrectionStudy.kt`; the widened gate and its `GATE_TESTS` in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py) (**67** self-tests) |
| **Conditions** | The tree at `HEAD` of iteration 31 plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0131`](C-0131-departure-and-saturation-audits.md)/[`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md) (the residue, the record-qualified mechanism and the refusal that expired), [`C-0129`](C-0129-result-file-hygiene.md) (the gate and its predicate), [`C-0093`](C-0093-shared-body-coupling.md)/[`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (the two-digit rule and the re-emission discipline), [`C-0117`](C-0117-reemission-order.md)/`CH-0131` (a sweep is a topological sort), [`C-0127`](C-0127-format-string-repair.md) (the digits-stripped prose classifier), [`C-0135`](C-0135-descent-manifold-width.md) (the descent manifold `F1` landed on), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), `P-18` (`RESULT_ABSOLUTE_FLOOR` is a claim in the locked units) |
| **Raises** | [`CH-0168`](../challenges/CH-0168-a-residue-published-without-its-own-cost.md) — a residue published without its own cost is priced against the nearest table — and [`CH-0169`](../challenges/CH-0169-four-spellings-of-eleven-and-four-implementations.md) — *"every spelling the corpus uses"* is four of eleven, and the rule could not reach **four** of the tree's six rounding implementations |

---

## The claim, in one line

**A rule that has survived five correct repairs is not repaired a sixth time; it is moved into the layer every study already goes through** —
and the reason it had not been is a refusal whose own measurement `C-0131` had repaired, in the same task, one sentence earlier.

---

## 1. The cheap bounds, and one of them changed what the task believed it was doing

| bound | measured | what it decided |
|---|---|---|
| **1** — what does `C-0131`'s published `costPartition` cover? | 35 tags, **disjoint** from the 31-file residue | the residue has **no** published cost, and the `T-214` queue row priced it out of a table about the files `T-212` closed (`CH-0168`). The residue's own cost is measured here |
| **2** — which rounding implementation does each of the 31 go through? | 22 `structure/`, 6 `coupling/`, 1 `window/`, 1 `brush/`, 1 `actuator/` | **the shape of the repair**: eight files could not obey the rule at their own emission sites, and one needed no code edit at all |
| **3** — is `C-0131`'s refusal of the default still grounded? | the map it refused was leaf-keyed; the map that exists is `record/spelling` | the one-line repair is available and safe, and the refusal expired inside the task that made it |
| **4** — what can the mechanism change move? | simulated offline over all committed result files | the blast radius is bounded **before** the expensive step, and it is exactly the checker's own scope line |
| **5** — what order must the 31 run in? | 6 constraints, all into `T-118` | one topological sort over the whole set, retained as a file |

Bounds 1 and 3 both ran against the **upstream claim's own artifact**, and both found a sentence that had outlived its measurement.

---

## 2. Why the repair is to the mechanism and not to 31 emission sites

`C-0131` measured the reason and did not apply it to its own residue.

`C-0093` cured the trap on its own **convergence** axis.
`C-0101` cured it in the **reproduction** records of the eleven files it was re-emitting.
`C-0127` then found `T-136` still carrying `reproductions[2].departure` at nine digits.
`C-0129` re-keyed it into a named constant, `DEPARTURE_DIGITS_BY_KEY`, and wrote *"it now lives once, by name"*.
`C-0131` re-keyed that constant again — correctly, on `record/spelling` — and re-emitted 35 files.

Five correct repairs, and the thirty-first file was left for the sixth.
Each of the five was applied **per file** or **per call site**, which is exactly the shape `C-0129`'s own §3 diagnoses as the reason the class survives.
A sixth per-file repair here would have closed the same census and left the same hole:
a study written next iteration, calling `roundedForResult()` with no arguments — which is what 22 of these 31 do — would carry nine digits again, and the gate would find it.

So the repair is one line in the layer every study already goes through.

---

## 3. The refusal that expired inside the task that made it

`C-0131`'s cheap bound 2 is the strongest thing in that claim, and it is the reason this one exists.
It refused precisely the change made here:

> the one-line alternative — `DEPARTURE_DIGITS_BY_KEY` as the *default* of `roundedForResult`,
> 66 files instead of 27 — is **refused on a measurement**, and the mechanism is re-keyed on `record/spelling`

The refusal is right, and it is right about a map that no longer exists.
Its ground is `gpd/results/T-193-gold-electrode-pzc.json`:

```json
{"versusStandardHydrogen": 0.51, "derivedVersusStandardHydrogen": 0.511420712,
 "departure": 0.001420712, "readStatus": "READ DIRECTLY — Adnan et al., PCCP 26:21419 (2024) …"}
```

A **leaf**-keyed default reaches that field, and two significant digits on a difference of two electrode potentials in volts discards determined information about a literature comparison.
A `record/spelling`-keyed default cannot reach it at all: the parent is `potentialOfZeroCharge`, which is not a `DEPARTURE_RECORDS` member, and the qualified key `potentialOfZeroCharge/departure` is not in the map.
`C-0131` performed that re-keying **in the same task, in the same file, in the sentence after the refusal** — and the refusal was carried forward anyway, because a bound is written before the work and re-read as a conclusion after it.

The assertion is not inherited here. It is a test:
`ResultRoundingTest` asserts that the **baseline** leaves `potentialOfZeroCharge/departure`, `upstreamChecks/departure` and `stationLattice/departure` untouched at nine digits, with no map passed by the caller.

---

## 4. The reach: `CH-0154` named one package and the defect stood in four

`CH-0154`'s second ground is that `roundedForActuatorResult()` takes **no arguments at all**,
so the six files on that path could not have obeyed the rule *by any edit at their own emission sites*.
It repaired `actuator/` and did not ask how many other entry points are in that position.

| implementation | carried the rule after `T-212`? | residue files on it |
|---|---|---|
| `structure/ResultRounding.kt` | **yes**, the canonical one | 22 |
| `actuator/ActuatorResultRounding.kt` | **yes**, delegated by `T-212` | 1 |
| `coupling/CouplingResultRounding.kt` | **no** | 6 |
| `window/WindowResultRounding.kt` | **no** | 1 |
| `brush/FluctuationCorrectionStudy.kt` (private) | **no** | 1 |
| `brush/ScfDensityProfileStudy.kt` (private) | **no** | 0 |

Eight of the 31 sit on an entry point that could not express the rule, which is `T-60`'s exact position one iteration later.
Three of the four are delegated here.
The fourth is left **and measured**: `T-1d` carries no field under any of the four spellings, so it is clean under the rule —
and it is one of `CH-0169`'s six files under a *fifth* spelling, `convergence[*].relativeError`.
What covers it is the **gate**, which reads output and does not care which implementation wrote it.

---

## 5. What the delegation had to preserve, and why it is a parameter

Three of the six implementations coerce an **integral** JSON number to a `Double` and three pass it through,
so every committed result file already carries its own package's answer:
`T-118` emits 25 bare integers and `T-16`, `T-113` and `T-123` emit none at all, every count being rendered `45.0`.

That is a **rendering** convention frozen by the files, not a precision choice.
Delegating `coupling/` to `structure/` without carrying it would have re-rendered every integer in six files —
a non-departure movement in a sweep whose entire claim is that there are none, and one that no amount of re-running would have explained.
It is carried as `roundIntegralNumbers`, asserted in both directions by a test in each package.

---

## 6. What the gate does and does not enforce now

`tools/check-result-file-hygiene.py --departures` prints four lines and exits 1 on the first:

```
GATE  (all four spellings inside a reproductions/convergence record): 0 field(s) in 0 file(s)
scope (the same predicate — since T-214 the gate IS the rule): 0 field(s) in 0 file(s)
strict (C-0129's leaf-name predicate, now a proper subset of the gate): 0 field(s) in 0 file(s)
wide  (any leaf key containing 'departure'; a ceiling on the class, NOT gated): …
```

The `scope` line is kept, carrying the same number as the gate, because it is the line the residue was published on:
a reader who watched it fall 601 → 351 → 0 is entitled to see it reach zero rather than disappear.
`strict` is kept because a proper subset is the cheapest possible regression test on the widening.

The predicate is held open by 13 new self-tests (`GATE_TESTS`), of which **six** fail if the gate is narrowed back to the leaf name
and **four** fail if the record qualifier is dropped — `T-193`'s volts, `T-160`'s own answer, `T-4`'s carried upstream comparison and a lattice coordinate difference in nm.
`C-0083`'s rule is a statement about a predicate, and a predicate that is only ever narrowed becomes a claim of cleanliness;
a mutation test in both directions is what stops that.

`wide` remains ungated on purpose: it counts `departureRatio` and `plateDeparture`, which are ratios **between two models**
and are determined to the precision of the models themselves.

---

## 7. `CH-0169` — the spelling set is a list, and a list is a census that stopped

`DEPARTURE_SPELLINGS`'s KDoc says *"Every spelling the corpus uses for a departure inside a `DEPARTURE_RECORDS` record."*
A walk of the committed result files finds **seven more** leaf keys of the same kind inside those two records —
**62 fields in 6 files** above two significant digits — and the important part is the last column:

| spelling | fields | file | the rule's quantity? |
|---|---|---|---|
| `firstIntegralRelativeSpread` | 12 | `T-3a` | **ambiguous** — `CLAUDE.md` records that the *full* spread measures the **conditioning of the diagnostic** and not the accuracy of the answer |
| `firstIntegralCoreSpread` | 12 | `T-3a` | **ambiguous** — the same, over the core of the gap |
| `centrelineRouteSpread` | 11 | `T-3b` | **ambiguous** — a spread between two evaluation routes of one solve |
| `relativeError` | 6 | `T-1d` | **yes** |
| `residualExponent` | 6 | `T-1d` | **no** — a `log₁₀` |
| `coverageErrorExponent` | 6 | `T-1d` | **no** — a `log₁₀` |
| `relativeSpread` | 4 | `T-164` | **yes** |
| `worstResidual` | 3 | `T-117` | **needs reading** — its units are the closure's |
| `relativeMovement` | 2 | `T-108` | **ambiguous** — `P-18`'s own determined-precision *measurement* |

**Twelve of the 62 must not be swept at all**, and that is why this is measured and queued (`T-225`) rather than folded in.
Two significant digits on a **logarithm** of a residual is a different statement entirely;
and `relativeMovement` is the number that decides how many digits everything else carries.
Past the mechanical part of the rule — a record type crossed with a name — the residue needs a judgement per key,
which is the judgement `C-0131` made correctly for `T-193`'s volts and for `T-160`'s own answer.

It is published here **with its per-file counts, its cost and its judgement**, which is `CH-0168`'s point applied to this task's own residue.

---

## 8. `F1` fired on two files, and control re-runs settle both in the studies rather than in the repair

Two of the 31 moved non-departure numeric fields. `T-113`, `coupling/NonUniformCouplingStudy.kt`, moved **218**, worst relative `1.0`.
`C-0129`'s `F3` in a new place, and `CLAUDE.md`'s rule is to re-run identical code before attributing it.

| comparison | departure fields | other numeric | verdicts / booleans | wording |
|---|---|---|---|---|
| this task's run vs the **committed** file | 13 | **218** | 0 | 0 |
| a `--committed` snapshot — **`HEAD`'s code, `HEAD`'s inputs** — vs the committed file (**run A**) | **0** | **223** | 0 | 0 |
| a **second run of that same snapshot** (**run B**) vs the committed file | **0** | **6** | 0 | 0 |
| **run A vs run B — identical code, identical inputs, same snapshot** | **0** | **217** | 0 | 0 |

**Two runs of identical code disagree in 217 fields.**
`T-113` is irreproducible run to run at `HEAD`, and was before this task began; this sweep moves **zero** of it.
The committed file has **one** commit, from iteration 9; `NonUniformCoupling.kt` has moved three times since and `OrigamiGrillage.kt` twice,
while its only input, `T-3b-tile-edge-load-profile.json`, has not moved at all — so the drift is not an input change either.

And the moving block is never scattered: in each run it is **one descent record and its transfers**.
In this task's run and in run A it is `distributions[24]`, `optimiser[12]`, `transfers[48…53]` and `paths[181…225]`,
which are all the same row — *"3 × 15 (`C-0015`'s answer), MINIMAX over the five solved states"*, a **45-parameter multi-state minimax**.
In run B it is a different row entirely, `optimiser[5]`/`transfers[5]`.
That is exactly the object `C-0135` measured on `T-129` and `CLAUDE.md` already records:
a descent on an optimal **manifold** has no isolated answer to be reproducible about, and Polak-Ribière is the amplifier.
Every non-descent quantity in the file — the 120-state rim sweep, the load-matched distribution and the cheap bounds — is bit-stable across all four emissions.

**`T-123` moved 11 non-departure fields too, and it is a different defect.**
There `HEAD`'s own code reproduces **the same 11**, worst `0.002` — the `subsets[*]` block `CH-0163` measured —
so it is **deterministic** staleness, code drift since the file was last emitted in iteration 12, and re-emitting it *repairs* it.
Run to run irreproducible on one file, stale on the other; this sweep is the cause of neither, and in both every verdict,
boolean and prose wording is identical in every comparison.

### What that costs `C-0058`, and what it does not

The re-emitted file is the one **`HEAD`'s code produces**; the committed one is what iteration 9's code produced,
and `CLAUDE.md` is explicit — *when a repair moves a downstream result file, RE-EMIT it; a file the code cannot reproduce
destroys the byte-for-byte re-run diff half this repository's claims rest on.*
What it costs is bounded, and it is stated here rather than left to be discovered:

- **No verdict moves, in any of the four emissions.** `decisionsMoved = 0` and `proseWording = 0` everywhere;
  `distributions[24].verdict` reads *"NOT flat, but better than no coupling at all"* before and after.
- **`C-0058`'s headline is untouched.** `0.0753` — the 3 × 15 single-state optimum, the number the design window quotes —
  occurs seven times in the committed file, seven times in run A and seven times here.
- **What moves is `C-0058`'s MULTI-STATE minimax row**: `0.1247`, `0.1286`, `0.1195`, `0.1307`, `0.6118`, `3.115`, `9.346` and `1.082`.
  Those sentences quote **one member of a manifold**, which is a fact about `T-113` and not about this task —
  and `0.1247` has reached `CLAUDE.md` and `ANSWERS.md`.

It is **queued as `T-226` rather than repaired here**, because repairing it means either re-quoting an arbitrary member —
which is what `C-0135` says not to do — or applying `C-0135`'s cure (periodic restarts, a small-difference restart, a lattice snap on the iterate)
to a study this task does not own, in the middle of a hygiene sweep.

---


## 9. What moved, measured against `git HEAD`

Thirty-one files, one snapshot, one topological order over the whole set — the order is
[`tools/T-214-reemission-order.txt`](../../tools/T-214-reemission-order.txt), and the sweep emitted **exactly** that set and nothing else.

| | departure fields | other numeric | prose (digits only) | prose (wording) | verdict / boolean | fields added |
|---|---|---|---|---|---|---|
| **all 31 files** | **351** | **230**, all in **three** files | 3 | **0** | **0** | 0 |

The 230 are `T-113`'s 218 and `T-123`'s 11 — the two `F1` files of §8, neither of them this sweep's doing —
and **one** more: `T-172`'s `parameters/elapsedSeconds`, which is a **wall clock in a result file**.
`CLAUDE.md` records that rule verbatim (*a wall clock is a step counter by another name … put the timing in the
console log, never in the JSON*), and a corpus census finds exactly **one** field in **one** file breaking it.
It is **not repaired here**: removing a field is a *schema* change to a file with three readers, so it is a
four-file topological re-emission of its own, and folding it into this sweep would destroy the measurement the
sweep exists to make. It is queued as `T-227`, **with its cost**, which is `CH-0168`'s lesson applied to this task's own residue.

### And the sweep left nothing stale

The check run as an **identity** rather than as a spot check:
**690 of 691** `reproductions`/`convergence` residual fields across the 31 re-emitted files are **exactly**
the two-significant-digit rounding of their own committed value, and **0** are unexplained.
The one that is not is `T-123`'s `convergence[10].departureFromFinest`, and it is not an exception to the
identity but a consequence of §8: a departure is **computed** from the quantities in its own record, and
that record's `worstDishingOverStroke` is one of the eleven fields `HEAD`'s own code moves too.
The emitter reports moved siblings per non-matching residual rather than asserting the identity blindly,
so no consumer was re-emitted before its producer.

---

## 10. What this claim does not do

- **It does not close the *quantity*, only the *named* rule.** `CH-0169`'s residue — 62 fields in 6 files under seven further spellings, of which 12 must not be rounded at all — stands, published above with its per-file counts and its judgement, and queued as `T-225`.
- **It does not repair `T-113`.** `HEAD`'s code does not reproduce that file and did not before this task; the re-emitted version is a reproducible member of a descent manifold, `C-0058`'s multi-state minimax numbers now quote a member no run reproduces, and settling that is queued as `T-226` rather than guessed at.
- **It does not remove `T-172`'s wall clock.** The sweep found it and re-emitted it with a new value; the census says it is the corpus's only one; removing it is a schema change to a file with three readers and is queued as `T-227` with its cost.
- **It does not re-grade any claim's gate 4**, does not move a verdict, and asserts nothing about physics.
- **The two-significant-digit rule remains a CONVENTION**, conservative rather than exact, exactly as `C-0129` and `C-0131` recorded. This claim widens **where** it is enforced and moves **where** it is applied; it does not change what it is.
