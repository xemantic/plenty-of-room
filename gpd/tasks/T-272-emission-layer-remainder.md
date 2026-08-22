# T-272 — the rest of the emission layer: typed input handles, a lattice tag and a regime block on every record

| | |
|---|---|
| **Leaf** | none — the remainder of step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | [`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) (`T-268`), which discharged `P1`, `P6` and `P7` and stopped at a **measured** cost rather than shrinking `P5` |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### What is left, and why it is one task rather than three

`T-268` carries seven predicates. `P1` was found **already discharged** — the six Kotlin rounding entry points
were delegated in iterations 36–38 — and `P6`/`P7` were discharged in full over the set that was swept.
What remains is `P2`, `P3` and `P4`:

| | predicate |
|---|---|
| **P2** | studies declaring **typed input handles** rather than reading `gpd/results/` by path, with `tools/result-reader-census.py` asserted to be a **superset** of the grep, as it already is |
| **P3** | a **`lattice` tag** on every emitted record — square / honeycomb / none — so *"which results are single-layer square-lattice"* is a query rather than an audit |
| **P4** | a **`regime` block** (buffer, valency, gap, bandwidth) on every record, so consuming a result outside the range it was solved in is a **gate** rather than a reading |

They are one task because **the content of `P3` and `P4` already exists** — `lattice/CrossoverLattice` and
`environment/Regime`, built in iteration 39 by `T-265` and the layer-2 work — and what is missing is putting it
on every record. Putting anything on every record **is** the full corpus sweep, so doing `P3` and `P4` in
separate passes pays for that sweep twice. `C-0162` declined to land a third unused declaration for exactly
that reason.

### The cost, measured rather than estimated

| set | files | measured | untimed | one pass |
|---|---|---|---|---|
| the whole corpus, which is what `P3` and `P4` require | 148 | **411 min** over 71 of 124 emitting studies | **53** studies | **≥ 7 h** |
| with the `P6`/`P7` by-kind control, which needs a second pass | | | | **≥ 14 h** |

This is the largest single compute item this programme has left, and it is **schedulable rather than
uncertain**: the 53 untimed studies are untimed and not unknown — `P-28` completed the `Entry points` table, so
every one of them has a command.

### Acceptance predicates

| | predicate |
|---|---|
| **A1** | `P2`, `P3` and `P4` discharged over the **whole** corpus, in one `tools/reemission-order.py` topological order whose dependency-constraint count is asserted **non-zero** before the order is trusted |
| **A2** | movement reported **by kind** against `git show HEAD:<path>`, with `numeric` still meaning *a finding* and every numeric mover controlled by a `--committed` re-run before it is called staleness |
| **A3** | the sixteen **Python** emitters reached, or a stated reason they are out of scope — an emission rule reaches only the emitters written in its own language, and no rule in the Kotlin layer reaches any of them |
| **A4** | every claim that **quotes** a moved number amended; never a stale file kept as *"the record of what the claim was written on"*, which git already holds |

### Units and conventions

Unchanged. Numbers will move, and every movement must be attributable to the emission rule and not to physics.

---

## 2. Plan

### Sequencing

**One pass, whole corpus, sorted.** The thing this task must not do is what `C-0162` refused to do: sweep part
of the corpus in an order it had not sorted. `C-0101` made that mistake inside its own sweep, running `T-157`
before `T-149`, and it left a consumer stale for six iterations at the cost of a whole deliverable clause.

### Two things that make it cheaper than it looks, and both are `C-0162`'s

- **A reader-graph closure is a ceiling, and the challenge that names the readers usually collapses it.**
  `CH-0207`'s closure of 83 files (~5 h) fell to **42** on one table, because six of its seven
  parameter-reading call sites read **strings** and a seventh reads an integral literal.
- **`tools/T-250-movement.py` now has a `parameter` kind**, so a precision repair to an input no longer shows
  up as `numeric` and `numeric` keeps meaning *a finding*. That is what made `T-268`'s 333/14 split readable
  at all.

### What would falsify this approach

- **A movement cannot be attributed to the emission rule.** Then something else changed; stop and find it,
  with the third run at `HEAD`'s own code that says whose defect it is (`C-0129`'s `T-136` control).
- **The typed handles and the census disagree.** The handles are the declaration and the census the
  derivation; a disagreement is a real edge one of them cannot see, and it is a finding about the graph rather
  than a bug in either.
- **A `regime` block cannot be stated for some record.** Then that record is a result whose solved range
  nobody can name, which is a finding about the study and not about the schema.
