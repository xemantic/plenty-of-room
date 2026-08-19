# T-214 — the departure rule's own scope: the 31 files the gate reports and does not enforce

**Raised by** [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) (`T-212`) and
[`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md).
**Claim** `C-0138`. **Challenges reserved** `CH-0168`, `CH-0169`.
**Result** `gpd/results/T-214-departure-rule-scope.json`.
**Leaf** none — a **process** task protecting the machine-readable artifact of every leaf.
**Verification type** **logical** (a static census over the committed corpus, before and after, plus an
offline simulation of the mechanism change over every one of the 124 committed result files)
**+ in-silico** (every affected study re-run in one topological order and diffed field by field against
its **committed** version read out of `git`).

## What is outstanding, and why it is not 31 hand edits

`C-0131` wired `tools/check-result-file-hygiene.py --departures` as a gate on `C-0129`'s **strict**
predicate — the leaf key `departure` inside a `reproductions` or `convergence` record — and printed the
**rule's** own scope beside it, ungated:

```
GATE  (reproductions[*].departure, convergence[*].departure): 0 field(s) in 0 file(s)
scope (all four spellings in those records; REPORTED, not gated): 351 field(s) in 31 file(s)
```

`C-0083`'s rule is that a gate which cannot come clean is not a gate, and a predicate can always be
narrowed until the tree is clean. Publishing the residue is what stops the narrowing from becoming a
claim of cleanliness. **This task removes the residue and widens the predicate to the rule.**

The obvious execution — edit 31 emission sites to pass `DEPARTURE_DIGITS_BY_KEY` — is the wrong one, and
`C-0131` measured the reason itself without applying it to its own residue: **the rule survives correct
per-file repairs**. `C-0093` cured it on the convergence axis, `C-0101` in the reproduction records of
eleven files, `C-0127` found `T-136` still carrying it, `C-0129` re-keyed it into a constant, `C-0131`
re-emitted 35 files. Five correct repairs, and a thirty-first file will be found by the sixth.

## Locked units and conventions

Nothing physical is computed. Units unchanged and untouched: nm, pN, pN/nm, pressure in pN/nm² = 1 MPa
exactly, `k_BT = 4.141947 pN·nm` at 300 K in aqueous buffer with stated Mg²⁺.

A **departure** is what `C-0093` means by it and what `structure/ResultRounding.kt` now encodes: a
**dimensionless** difference or ratio of two nearly equal numbers, emitted under one of
`DEPARTURE_SPELLINGS` inside one of `DEPARTURE_RECORDS`. Dimensionless is part of the definition, which is
what `RESULT_ABSOLUTE_FLOOR` cannot reach (`P-18`) and what `CH-0154` measured on `T-193`'s volts.

---

## Numeric target and acceptance predicates

**P1.** The published cost partition is **read before anything is run**, and what it actually covers is
reported. It is a cheap bound somebody already paid for; if it does not cover the residue, that is a
finding, not a reason to re-measure it.

**P2.** The mechanism is repaired so that a study obeys the departure rule **by construction** rather than
by remembering to pass a map: `DEPARTURE_DIGITS_BY_KEY` becomes the **baseline** of `roundedForResult`'s
`digitsByKey`, and every remaining independent rounding implementation reaches it.

**P3.** The blast radius of `P2` is bounded **offline, over all 124 committed result files**, before any
study is re-run: the set of files whose bytes the mechanism change can move is exactly the 31 the scope
line names, and the set of fields it can move inside them is exactly the departure fields.

**P4.** All 31 files are re-emitted in **one** `tools/reemission-order.py` order over the whole set, through
**one** `tools/study-batch.sh` snapshot, and the order is retained as a file (`CH-0131`: a re-emission
sweep is a topological sort, not a list).

**P5.** For every re-emitted file, **only departure fields move**. Every non-departure numeric movement is
resolved by a control re-run of identical code before it is attributed to the repair (`C-0129` `F3`), and
classified as staleness, as a descent manifold (`C-0135`), or as a finding.

**P6.** No verdict, boolean, percentile or computed physical quantity moves, in any file. A prose diff is
classified **with the digits stripped** (`C-0127`).

**P7.** **Nothing is stale**, asserted as an identity rather than spot-checked: every
`reproductions`/`convergence` residual in the re-emitted files is exactly the two-significant-digit
rounding of its own committed value, with the count reported and 0 unexplained.

**P8.** `tools/check-result-file-hygiene.py --departures` reads **0 on its `scope` line as well as on its
`GATE` line**, and the gate predicate is then widened to the rule, with self-tests that fail if it narrows
back.

**P9.** `python3 tools/result-reader-census.py --check` passes, `tools/verify.sh` passes, and every
document gate passes.

## Plan — cheap bound first

| step | cost | what it decides |
|---|---|---|
| **1** read `T-212`'s published `costPartition` and `residueByFile` | one `python3` | whether the residue's cost is already known. **It is not**: the partition covers the 35 files `T-212` *closed*, not the 31 it *left* |
| **2** map each of the 31 files to its producer through `P-22`'s census | one `python3` | the run list, and the fact that all 31 resolve (no orphan result file) |
| **3** `grep` which rounding implementation each producer calls | one `grep` | **the shape of the repair.** 22 call `structure/`'s, 6 call `coupling/`'s, 1 `window/`'s, 1 `brush/`'s local one, 1 `actuator/`'s — and the last needs **no code edit at all**, `T-212` having already repaired it |
| **4** simulate the mechanism change offline over all 124 result files | one `python3` | `P3`, before a single JVM starts. If it moves a file outside the 31 the repair is wrong and no re-run would have told us cheaply |
| **5** `tools/reemission-order.py` over the whole set | one `python3` | 6 dependency constraints, all into `T-118`; the order is retained |
| **6** one `tools/study-batch.sh` snapshot, 31 runs | the only expensive step | `P4`–`P7` |

The residue's measured cost partition — **the thing `C-0131` published for the files it closed and not for the files it left**:
8 *closed form*, 5 *lattice solve*, 4 *elastica shooting*, 3 *junction closure search*, 2 each of *placement search*, *plan packing*,
*field solve* and *SCF solve*, and one each of *element catalogue*, *minimax descent* and *window intersection*.
The two heaviest are `T-124` (`~55 min` by its own entry-point row) and `T-123` (`~18 min`); the whole sweep is one snapshot.

**What would falsify this approach** — declared before it is run:

- **`F1`.** A **non-departure numeric field moves** in any re-emitted file, and a control re-run of
  identical code does **not** reproduce the movement. Then the repair is not precision-only.
- **`F2`.** The offline simulation moves a result file **outside** the 31, or moves a non-departure field
  inside them. Then `DEPARTURE_DIGITS_BY_KEY` as a baseline is not equivalent to 31 site edits, and
  `C-0131`'s refusal of the default was right for a second reason it did not state.
- **`F3`.** Any `reproductions`/`convergence` residual in a re-emitted file is **not** the two-digit
  rounding of its committed value. Then a consumer was re-emitted before its producer, or the file was
  already stale.
- **`F4`.** A verdict, boolean or prose wording changes.
- **`F5`.** Widening the gate to the rule leaves it **unable to come clean** — i.e. the scope line does not
  reach 0 after the sweep. Then `C-0083` forbids the widening and the residue must be published instead.
