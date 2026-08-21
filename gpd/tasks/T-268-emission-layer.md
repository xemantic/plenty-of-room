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
  at their own call sites (`CH-0132`).
- **a dependency graph that has to be derived**, because studies read result files by **path**.
  `tools/result-reader-census.py` exists to recover what a typed input handle would have declared, and the
  convention that would have made the graph self-maintaining is used by **2 of 72** studies.
- **records that do not say what they are about**, which is what made the honeycomb correction an **audit**
  across five claims and a census rather than a query.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | one rounding implementation, or the six delegating to it with their constants **asserted equal** as a test, and the integral-number rendering carried as a parameter (`CH-0133`: three of the six coerce `45` to `45.0`, three do not, and the committed files are frozen either way) |
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
