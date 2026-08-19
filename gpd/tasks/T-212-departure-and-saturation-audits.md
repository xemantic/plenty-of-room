# T-212 / T-213 — finishing the two audits `C-0129` measured, published, and declined to gate

**Raised by** [`C-0129`](../claims/C-0129-result-file-hygiene.md) (`T-209` and `T-210`), and by
[`CH-0153`](../challenges/CH-0153-a-statistical-power-gate-discharged-by-a-statistic-that-is-identically-zero.md).
**Claim** `C-0131`. **Challenges reserved** `CH-0154`, `CH-0155`.
**Result** `gpd/results/T-212-departure-and-saturation-audits.json`.
**Leaf** none for `T-212` (a **process** task); `A8.2` for `T-213`, whose six files are all flatness studies.
**Verification type** **logical** (two static censuses over the committed corpus, before and after) **+ in-silico** (every affected study re-run and diffed field by field against its **committed** version read out of `git`, with a control re-run wherever a non-departure field moves).

## Why these are one task

Not because they are both hygiene — that is `C-0129`'s reason for grouping `T-208`/`T-209`/`T-210`, and it is the weak one.
They are one task because **they are one re-emission sweep**.

1. **The file sets OVERLAP.** `T-212` owes 27 files and `T-213` owes 6; three files —
   `T-155`, `T-162`, `T-191` — are in **both**. Run separately, each of those three is re-run twice,
   and the second run's diff would then have to be separated from the first's.
2. **The order is a topological sort of ONE graph.** `tools/reemission-order.py` sorts the reader census
   (`CH-0131`: a re-emission sweep is not a list). Two sweeps over overlapping sets sorted independently
   can put a consumer of one sweep before a producer of the other.
3. **The cost is dominated by a set both sweeps sit inside.** The six saturated files are the six
   **Monte Carlo** studies, and they are also the expensive half of `T-212`'s 27. The affordability question
   — the one `C-0129` left open — is the same question for both.
4. **`CH-0152`'s lesson is shared.** Each is a number *and* the sentence written around it.
   `T-148`'s statistic was computed correctly, emitted correctly, unit-tested — and misdescribed in prose.
   Repairing a statistic without repairing its prose repairs nothing a reader can see.

## Locked units and conventions

Nothing physical is computed. Units unchanged and untouched: nm, pN, pN/nm, pressure in pN/nm² = 1 MPa exactly,
`k_BT = 4.141947 pN·nm` at 300 K in aqueous buffer with stated Mg²⁺.

A **departure** means what `C-0093` means by it: a **dimensionless** difference or ratio of two nearly equal numbers,
emitted as a `convergence[*].departure`, `reproductions[*].departure`, `*.relativeDeparture`,
`*.departureFromFinest` or `*.relativeDepartureInStroke` field. **Dimensionless is part of the definition** and it is
what `RESULT_ABSOLUTE_FLOOR` cannot reach (`P-18`).

A proportion `p̂` is **saturated** when it is exactly `0.0` or exactly `1.0`.

---

## T-212 — the departure audit, wired or explicitly declined

### Numeric target and acceptance predicates

**P1.** The cheap bound runs first and is published: for each of the 27 files, the producing study, the
number of strict fields, and a **measured** re-run cost — partitioned into *cheap* and *heavy* — before any file is re-run.

**P2.** Every file the sweep can afford is re-emitted in `tools/reemission-order.py`'s order over the **whole**
union set (`T-212` ∪ `T-213`), and each is diffed field by field against `git HEAD`. Every file that is **not**
re-emitted is named with the reason and the cost.

**P3.** For every re-emitted file, **only departure fields move**. Any non-departure numeric movement is
resolved by a **control re-run of identical code** before it is attributed to the repair (`C-0129` `F3`).

**P4.** No verdict, boolean, percentile or computed physical quantity moves, in any file.
A prose diff is classified **with the digits stripped** — `C-0127` measured 23 of 57 apparent decision changes
to be `Double.toString()` moving in its sixteenth digit.

**P5.** `tools/check-result-file-hygiene.py --departures` is re-measured. The `--departures` predicate is
promoted from an audit to a **gate** **if and only if** the tree reads clean under it (`C-0083`:
*a gate that cannot come clean is not a gate*). If it does not, the residue is named per file with its reason,
and the gate is **not** wired — that is a result, not a failure.

### Falsifiers

- **`F1`** — a re-emitted file moves a **computed physical quantity**, and a control re-run of identical code does *not* move it.
  Then the rounding change is not confined to departures and the premise of the whole sweep is wrong.
- **`F2`** — a verdict, an acceptance predicate or a boolean changes.
- **`F3`** — the census after the sweep is **not** smaller than 199 fields in 27 files.
- **`F4`** — the rule, applied as written, changes a number that is **not dimensionless**. `DEPARTURE_DIGITS_BY_KEY`
  is keyed on a **leaf name**; if a bare `departure` in some record carries volts or nanometres, then the mechanism
  does not express the rule and a challenge is owed.

### Plan, and the cheap bound that decides the method

The mechanism is already central (`DEPARTURE_SIGNIFICANT_DIGITS` / `DEPARTURE_DIGITS_BY_KEY` in
`structure/ResultRounding.kt`), so the *code* change per file is one argument at one emission site.
**The cost is entirely in the runs.** Three candidate methods, priced before anything was written:

| method | code change | files re-run | risk |
|---|---|---|---|
| **A** — per-site `digitsByKey = DEPARTURE_DIGITS_BY_KEY` at the 27 emission sites | 27 lines | 27 | none beyond the runs |
| **B** — make `DEPARTURE_DIGITS_BY_KEY` the **default** of `roundedForResult` | 1 line | **66** | rounds a `departure` in **volts** (`T-193`) and a study's own **answer** (`T-160`) |
| **C** — one snapshot, `N` studies run inside it, copy-back scoped per run | as A | 27 | one compile instead of 27 |

**B is refused on a measurement, not on taste** — it is the `F4` falsifier, run as a census before any edit.
**C is how A is executed**: `tools/study.sh` snapshots the tree *per study*, so 30 studies is 30 snapshots and
30 cold Gradle builds. The copy-back discipline (`S-95`: scope it to the files *this run* produced) is preserved
by checksumming `gpd/results/` inside the snapshot **before each individual run**, not once for the batch.

**What would falsify the approach**: if the per-file diffs are not confined to departures — if a rounding change
at the serialisation boundary moves an answer — then the emission-site change is not a precision change and
the whole method is wrong.

---

## T-213 — the one-sided bound in the six remaining files

### Numeric target and acceptance predicates

**P6.** `exceedanceOneSidedBound` is emitted **beside** `exceedanceStandardError` in `T-155`, `T-162`, `T-163`,
`T-165`, `T-178` and `T-191`, `null` at every unsaturated cell, and equal to `coupling.saturatedProportionBound`
at every saturated one. The symmetric error **stays**: it is uninformative rather than wrong, and removing it
would break every reader of the schema.

**P7.** **No exceedance probability, dishing percentile or `flatAt*` boolean moves** in any of the six.

**P8.** `CH-0153`'s load-bearing sentence is **checked rather than inherited**: *"no verdict moves; a one-sided
bound strengthens every failure reading it replaces"*. The check is per file — the exceedance saturates at `1.0`
in the direction of **failure**, so the bound is a statement that the design fails with probability above
`0.9976–0.99985`, which is strictly more than the symmetric `0` said.

**P9.** `CH-0152`'s lesson is applied: every **sentence** in the six files that describes the symmetric error as a
resolution, a power or a precision is re-read and repaired. A number repaired under a sentence that still
misdescribes it is not repaired.

### Falsifier

- **`F5`** — an exceedance, a percentile or a `flatAt*` boolean moves. Then adding a field has perturbed the study,
  which it must not.
- **`F6`** — the one-sided bound is *weaker* than the symmetric interval in some file, i.e. `CH-0153`'s
  *"strengthens every failure reading"* is false somewhere.

### Plan

`coupling.saturatedProportionBound` exists and carries five gate-named tests (`C-0129`). Each of the six studies
gains one nullable field on one record class and one call at one construction site — the shape `T-148` already
demonstrates. The runs are the cost, and they are the **same** runs `T-212` is paying for in three of the six.
