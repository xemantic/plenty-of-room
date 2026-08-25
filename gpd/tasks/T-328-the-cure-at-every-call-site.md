# T-328 — `C-0135`'s descent cure at **every** selection site of `T-323`, and the re-emission that shows the moved fields stop moving

**Leaf:** `A8.2`
**Raised by:** [`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md) (`T-323`) §14(a),
when its `F23` — declared **CLOSED** — **fired**.
**Companion row:** [`T-329`](T-329-an-identity-is-a-threshold-and-a-boolean.md), which is the other of `F23`'s two repairable channels
and is executed in the same commit, on the same two runs.
**Reserved claim:** `C-0217`. **Reserved challenges:** `CH-0280`, `CH-0281`. **Reserved queue rows:** `T-326`, `T-327`.

---

## Formulate

### The standing state

`T-323` emitted its result file twice, in two snapshots, and diffed the two outside the study.
**26 of 1 252 leaves moved.** `C-0216` §14 classifies every one of them by kind —
`0` verdicts, `0` booleans of 250, `0` added, `0` removed, `0` unclassified — and names the cause
in three channels:

| channel | what it is | repairable |
|---|---|---|
| (a) | `C-0135`'s decision-precision cure reached only some of the study's selection sites; the rest compare a **raw** `Double`, and `t323P90` returns an unrounded order statistic, so a tie between two placements is broken by the last ulp | **yes — this row** |
| (b) | two identity residuals whose true value is **zero** were emitted as **values** | **yes — `T-329`** |
| (c) | `C-0135`'s descent **manifold**: where the active constraints are fewer than the free directions the optimal set is a manifold, rounding fixes which **branch** is taken and not the **point** | **no** |

`CLAUDE.md` names the class of (a) exactly: *a cure is a property of a CALL SITE, not of a
repository — grep for the call sites, not for the fix.* The instance was committed **by the author
who quoted that sentence in this study's own plan**, which is what makes it a row rather than a
footnote.

**The source was deliberately not changed after the run.** Repairing it then would have left the
committed emitter unable to reproduce the committed artifact, which is the invariant
`gpd/README.md` rests on. So this row is the repair **and** its re-emission, in that order.

### The question

1. **How many selection sites are there, under a stated predicate, and how many were raw?**
   `C-0216` §14 says *"2 of 14"*; `CLAUDE.md` says a census is dated by its predicate, so the count
   is re-derived here rather than inherited.
2. Does routing every one of them through the decision rule make `F23` **hold** — measured the only
   way it can be, by two fresh runs diffed outside the study?
3. If anything still moves, is it exactly channel (c), the residue for which no rounding supplies an
   answer?

### The census predicate, stated before it is run

A **selection site** is a place in
[`tile/JointPlacementDistribution.kt`](../../src/main/kotlin/tile/JointPlacementDistribution.kt) or
[`tile/JointPlacementDistributionStudy.kt`](../../src/main/kotlin/tile/JointPlacementDistributionStudy.kt)
where the outcome of a `Double` comparison decides **which candidate** a search or a report carries
forward — an argmin or argmax over candidates, a sort used to take a top-`k` or a first, or a
rank/count of candidates below a candidate — and where the compared values come from **one and the
same objective evaluated over a candidate set**.

Excluded, and each exclusion is stated with its ground:

| excluded | ground |
|---|---|
| `JointPlacementFamily.nearest`'s `minByOrNull` over `abs(station − target)` | a **geometric** snap of a fixed grid onto a fixed ladder; no objective, no stream, no candidate set that a solve ranks |
| `min`/`max` that return a **value** (`bestTierTwo`, `oracleP90Floor`, `determinedTrainingBest`, `stiffnesses.max()`, the departure maxima) | they select no candidate; rounding the comparison cannot change the number returned |
| `pairedMedianRatio` and `orderStatistic` | statistics, not selections — an order statistic's instability is channel (c) and is not curable by rounding a comparison |
| `realisationsWhereTheNumeratorWins` | a raw `<` **count**, but of **two different designs at one realisation** rather than of one objective over a candidate set; a tie there would be a physical coincidence, and it moved in `T-323`'s diff as a *functional of* a moved argmin |
| `first { it.label == … }`, `indexOfFirst { … }` | selection by **label**, deterministic |

### Numeric targets

| # | target |
|---|---|
| `P1` | the census: how many selection sites the two sources carry under the predicate above, how many decided at the decision precision before this row, and how many did not — each named, with its `HEAD` line number |
| `P2` | every raw site routed through **one** rule, so that a mutation of the rule is visible from every site rather than from one copy of it (`CLAUDE.md`: *a duplicated rule is invisible to a mutation test of either copy*) |
| `P3` | the two runs, fresh, in two snapshots, diffed **outside** the study, with the moved-leaf count and its classification by kind |
| `P4` | whichever leaves still move, classified — and the statement of whether they are channel (c) and nothing else |
| `P5` | the mutation harness re-anchored and extended, at **0 survivors over a subtracted baseline**, with `--rerun-tasks` forced on the baseline (`CH-0278`) |

### Acceptance predicate

The task passes when `P1`–`P5` are discharged and the claim states plainly, from an actual diff of
two post-repair runs, whether `F23` now holds; and if it does not, exactly which leaves move and
whether every one of them is channel (c).

**A residue is as much a result as a clean diff** — `C-0135` is explicit that the right response to
a manifold is *report the residual rather than asserting byte-identity*.

### Units and conventions — locked before deriving

Unchanged from `T-323` in every respect: the same `10 × 6` cross-section, the same `116 bp` block
extent, the same drawable `102 / 109` raster, the same `C-0022` collar, the same three seeds
(`197197` grading / `316316` training / `323323` screening), the same realisation counts, the same
`81 × 81` dishing grid, the same `T-5b = 0.10`. **No physical input moves in this row.** The
decision precision is `SEARCH_DECISION_DIGITS = 6`; the emission precision stays at nine.

---

## Plan

### The cheap bound runs first, and it decides the shape of the repair

**Bound 1 — the census is a `grep`, and it costs no run.** Under the predicate above the two
sources are one pass with `grep -n 'minByOrNull\|maxByOrNull\|minWithOrNull\|sortedWith\|count {'`.
It is worth doing before anything is written because it settles whether the repair is *fourteen
edits* or *one function plus fourteen call sites* — and the answer decides whether the next
refactor re-opens the defect.

**Bound 2 — the two sites that were already correct show the shape.** `C-0216` §14 identifies them
as the two consuming `T-316`'s `percentileObjective`, which rounds inside itself. That is the
pattern to copy and it is already in the tree, so nothing has to be invented.

**Bound 3 — idempotence makes routing the already-correct sites free.** `searchDecision` is
idempotent, so passing an already-rounded objective through the rule a second time cannot move it.
That is what lets the repair be **structural** — *every* selection site goes through the rule —
rather than a list of the ones that happened to be wrong, which is the state that produced the
defect.

### Method

One rule, four public entry points, in the model source rather than at the call sites:

- `searchDecisionKey(value)` — the quantisation, once;
- `decidesBetter(candidate, incumbent)` — the comparison, for the argmins and for the two **ranks**;
- `byDecisionThenLabel(label, key)` — the comparator, for the sorts;
- `decisionArgmin(candidates, label, key)` — the argmin, evaluating `key` **exactly once** per
  candidate, because two of its call sites have a whole dropout ensemble behind that key.

`jointPlacementBetter` is rewritten in terms of `decidesBetter` so the rule is not written twice.

**Cost.** The repair is compile-time; the measurement is two full runs of a study whose primary arm
is an exhaustive census over 7 776 placements. That is the whole expense and there is no cheaper
instrument: a run cannot assert byte-identity about itself.

### What would falsify this approach

- **If the diff after the repair still carries an argmin whose objective is a lattice-ranked
  placement**, the rule is not reaching a site the census missed, and the census's predicate is
  wrong rather than the repair.
- **If a number the claim's headline rests on moves**, the repair is not cosmetic and `C-0216`'s
  answer has to be re-read, not just re-quoted.
- **If routing the already-rounded sites moves anything at all**, `searchDecision` is not idempotent
  and bound 3 is false.
- **If the moved-leaf count does not fall**, the diagnosis in `C-0216` §14(a) is wrong.

### Declared falsifiers

| id | fires if | verdict |
|---|---|---|
| `G1` | two post-repair runs are not byte-identical | must not fire — and if it does, every moved leaf must be channel (c), named and classified |
| `G2` | any of `C-0216`'s headline numbers moves — the four corners, the total, the interaction at `f = 0.30`, the ordering reversal, the family census, the flat count | **declared OPEN**: either answer is the result, and a move means `C-0216` is amended in substance rather than in provenance |
| `G3` | the eight new named tests pass against the **unrepaired** model | must not fire — they are written first and watched fail |
| `G4` | any mutation of the decision rule or of the identity report survives a named test, over a **subtracted** baseline with `--rerun-tasks` forced | must not fire |
| `G5` | routing the sites that were already rounded moves any emitted number | must not fire — `searchDecision` is idempotent |
| `G6` | the census under this row's stated predicate reproduces `C-0216` §14's *"2 of 14"* | **declared OPEN** — `CLAUDE.md` says a census is dated by its predicate, so a different count is a result about the predicate and not a defect of either reading |

---

## Execute

The repair, the census and the re-emission are recorded in
[`C-0217`](../claims/C-0217-the-cure-at-every-call-site.md), which is filed against the re-emitted
[`gpd/results/T-323-the-placement-and-the-distribution-together.json`](../results/T-323-the-placement-and-the-distribution-together.json)
and the four retained emissions in
[`gpd/data/T-323-reproducibility/`](../data/T-323-reproducibility/README.md).

### `P1` — the census

**`19` selection sites, `5` already at the decision precision and `14` raw.** The full table with
`HEAD` line numbers, and the five exclusions each with its ground, are `C-0217` §1. `G6` was
declared **OPEN** on whether the census reproduces `C-0216` §14's *"2 of 14"*, and it **fired**: the
difference is the predicate, not the reading.

### `P2` — one rule

Five new public functions in `tile/JointPlacementDistribution.kt` — `searchDecisionKey`,
`decidesBetter`, `byDecisionThenLabel`, `decisionArgmin`, `identityHolds` — with
`jointPlacementBetter` rewritten on `decidesBetter`. 18 call sites in the study routed through them;
the nineteenth already called `jointPlacementBetter`. `G3` did not fire: the eight new tests do not
compile against the model at `b5aa97a`.

### `P3` and `P4` — the two runs

`4` of `1 255` leaves move, against `26` of `1 252`. `0` booleans of 251, `0` added, `0` removed,
`0` unclassified. `G1` **fired** and was declared on that condition; all four moved leaves are
`spearmanAgainstSearched` at `f = 0.30` or a sentence rendering it, and `C-0217` §4 shows they are
channel (c) by measurement rather than by assertion — every other quantity built on the **same
thirteen** descent outputs is bit-identical.

`G2` was declared **OPEN** and did **not** fire: every headline number of `C-0216` is bit-identical
across all four emissions.

### `P5` — the mutation harness

**34 mutations, 0 survivors over a subtracted baseline of `0`**, `--rerun-tasks` forced on the
baseline. Two rows re-anchored onto the one function the rule now lives in, nine added. `G4` did not
fire.

---

## Verify

The five gates are `C-0217` §5 and the falsifier verdicts are its header row. In summary:

| id | declared | fired | reading |
|---|---|---|---|
| `G1` | must not fire, and if it does every moved leaf must be channel (c), named and classified | **FIRED** | `4` of `1 255`, all four the same statistic, all four channel (c) — `C-0217` §4 |
| `G2` | **OPEN** | no | every headline number bit-identical across all four emissions |
| `G3` | must not fire | no | the eight new tests do not compile at `b5aa97a` |
| `G4` | must not fire | no | 34 mutations, 0 survivors over a subtracted baseline of 0 |
| `G5` | must not fire | no | the four re-routed already-rounded sites moved nothing; `searchDecisionKey` is idempotent, asserted as a named test |
| `G6` | **OPEN** | **FIRED** | `5 of 19` against `C-0216` §14's `2 of 14`, and the difference is the predicate |

**`T-329`'s four falsifiers** `Q-F1`–`Q-F4` did not fire: no residual whose true value is zero is
printable in the emitted file, both identities still hold at their declared tolerances, a non-finite
residual does not report as holding and a non-positive tolerance is refused, and removing the
bank-slice `convergence` row changed nothing else — the cross-diff's 8 removed leaves are exactly
that record's eight fields.
