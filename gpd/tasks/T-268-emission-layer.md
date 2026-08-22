# T-268 — one emission layer, typed input handles, and a lattice tag and regime block on every record

| | |
|---|---|
| **Leaf** | none — step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect, restated

Three things that are one thing:

- **six rounding implementations**, two of which disagree on the absolute floor (`1e-9` against `1e-12`), so
  *"the tree rounds to nine significant digits"* was a description and never a design. `C-0129` and `C-0138`
  moved the departure rule into the layer for one of the six; the others could not have obeyed it by any edit
  at their own call sites (~~`CH-0132`~~ — **there is no `CH-0132` either**: it and `CH-0133` were reserved by `T-201` in iteration 24 and **never filed**, and this task cited both as though they were challenges. The finding is [`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md), *"'the rule lives once' was true of one package"*).
- **a dependency graph that has to be derived**, because studies read result files by **path**.
  `tools/result-reader-census.py` exists to recover what a typed input handle would have declared, and the
  convention that would have made the graph self-maintaining is used by **2 of 72** studies.
- **records that do not say what they are about**, which is what made the honeycomb correction an **audit**
  across five claims and a census rather than a query.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | one rounding implementation, or the six delegating to it with their constants **asserted equal** as a test, and the integral-number rendering carried as a parameter (~~`CH-0133`~~ **there is no `CH-0133`** — the corpus's highest challenge is `CH-0209` and no file, index row or source carries that ID; the finding is `T-214`/`C-0138`'s and `CLAUDE.md`'s *"the integral-number rendering is a per-package convention frozen by the committed files"*, `C-0162` §5: three of the six coerce `45` to `45.0`, three do not, and the committed files are frozen either way) |
| **P2** | studies declaring **typed input handles** rather than reading `gpd/results/` by path; the census asserted to be a **superset** of the grep, as it already is |
| **P3** | a **`lattice` tag** on every emitted record — square / honeycomb / none — so *"which results are single-layer square-lattice"* is a query |
| **P4** | a **`regime` block** (buffer, valency, gap, bandwidth) on every record, so consuming a result outside the range it was solved in is a **gate** rather than a reading |
| **P5** | the whole corpus re-emitted in [`tools/reemission-order.py`](../../tools/reemission-order.py)'s topological order over the **whole** set at once, with its dependency-constraint count asserted **non-zero** before the order is trusted |
| **P6** | what moved reported **by kind**, and every claim that **quotes** a moved number amended — never the file left stale as *"the record of what the claim was written on"*, which git already holds |
| **P7** | descent manifolds classified as such and not reported as staleness, separated by a `--committed` control (`T-113`, `T-129`, `C-0135`) |

### Units and conventions

Unchanged. Numbers **will** move — that is what distinguishes this step from `T-265`–`T-267` — and every
movement must be attributable to the rounding rule, not to physics.

---

## 2. Plan

### Sequencing, which is not optional

**`P-27` green first**, then `T-265`–`T-267`, then this.
It re-emits from emitters those three steps change, so running it early is `C-0101`'s
consumer-before-producer error one layer up — and `C-0101` made exactly that mistake **inside its own sweep**,
running `T-157` before `T-149` and leaving it stale for six iterations, which cost a whole deliverable clause.

### Why `P3` and `P4` pay for themselves

`ARCHITECTURE.md` puts it as two sentences and they are the justification for the whole step:
the honeycomb correction of iterations 33–34 would have been a **query** rather than an audit, and consuming a
result outside its solved range would be a **gate** rather than a reading.
Both are schema, and schema is only cheap to add while everything is being re-emitted anyway.

### Cost

The largest in the restructure: every affected result file re-emitted, in topological order, with claims
amended. Read `tools/reemission-order.py` and the reader census before estimating, not after.

### What would falsify this approach

- **A movement cannot be attributed to the rounding rule.** Then something else changed and the sweep is
  carrying a defect; stop and find it (`C-0129`'s `T-136` control, and the third run at `HEAD` code that says
  whose defect it is).
- **The census and the typed handles disagree.** The handles are the declaration and the census is the
  derivation; a disagreement is a real edge one of them cannot see, and it is a finding about the graph.

---

## 3. Execute

### The cheap bound ran first, and it decided the shape of the iteration

`P5` says *"the whole corpus re-emitted in one topological order"*. Measured **before** any of it was
spent, from the run times [`P-28`](../../TASKS.md) had just completed:

| set | files | measured | untimed | one pass |
|---|---|---|---|---|
| the **whole corpus** (`P5` as written) | 148 | **411 min** over 71 of 124 emitting studies | 53 studies | **≥ 7 h** |
| the reader-graph **closure** of what this step moves | 83 | 222 min | 33 | ~4.5–5 h |
| the files the rule **can** move at all | 50 | 86 min | 19 | ~2 h |
| the files the rule **does** move (`≥ 9` significant digits in a parameter) | **42** | — | — | **~2 h** |

`P6` and `P7` need a second pass, so `P5` as written is **≥ 14 h of serial compute** on a box
`CLAUDE.md` records as being at its ceiling with three concurrent agents. It does not fit one
session, and it is reported rather than shrunk.

### `P1` was already discharged, and the defect statement above is stale

All **six** Kotlin rounding entry points already delegate to `structure/ResultRounding.kt` —
`actuator/` by `T-212`/`CH-0154`, `coupling/` and `window/` by `T-214`/`C-0138`,
`brush/FluctuationCorrectionStudy` by `T-214` and `brush/ScfDensityProfileStudy` by `T-225` — the
three public ones assert their two constants equal to the tree's as a test, and `CH-0133`'s
integral-number rendering is already carried as the `roundIntegralNumbers` parameter.
[`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s layer-7 row (*"six implementations; not started"*) is a
description of the tree before iteration 36.

**And the count is wrong in the other direction.** Sixteen **Python** emitters in `tools/` write
committed result files (`T-9`, `T-119`, `T-183`, `T-184`, `T-194`, `T-200`, `T-201`, `T-202`,
`T-211`, `T-212`, `T-214`, `T-225`, `T-234`, `T-249`, `T-250`, `P-22`), and no rule in the Kotlin
layer reaches any of them. They are step 7's material, and four of them sit in the set this step's
rule would otherwise have had to move.

### What was built instead: the rule `CH-0207` asks for

`PARAMETER_RECORDS` in `structure/ResultRounding.kt`, and a **sticky** exemption threaded through
the traversal: a number anywhere below a `parameters`, `runParameters` or `citedInputs` key is
emitted exactly as the study was handed it. It is a **default of the layer**, so all six entry
points obey it with no edit of their own — `C-0138`'s lesson, five per-call-site repairs of the
departure rule before it moved into the one place every study goes through.

## 4. Verify

Gates as tests. `gate 1` the rule preserves the value exactly; `gate 2` the scope boundary in both
directions, and `parameterRecords = emptySet()` restores the pre-repair behaviour bit for bit;
`gate 3` every spelling the census found, the whole subtree, the rendering convention, and the
precedence against the departure rule; `gate 5` `CH-0207`'s own `T-3a` literal.

### The run

`tools/study-batch.sh` over the 42 studies whose result file the rule can move, in
`tools/reemission-order.py`'s order over the whole set at once, its **38** dependency constraints
asserted non-zero first. One snapshot, sequential runs, per-run scoped copy-back. **0 failures, 38
files changed, 4 re-run and unchanged.** Three `--committed` control runs (`T-14`, `T-129`, `T-136`),
each the study twice in a snapshot of `HEAD`.

Verdicts, the by-kind table and the three controls are in
[`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) §6, and the census correction that came
out of re-deriving `CH-0207`'s own table is [`CH-0210`](../challenges/CH-0210-the-parameter-block-census-is-not-reproducible.md).
