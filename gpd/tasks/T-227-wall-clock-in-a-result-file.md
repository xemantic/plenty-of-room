# T-227 — the corpus's one wall clock: `T-172`'s `parameters/elapsedSeconds`

**Raised by** [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`), which re-emitted `T-172`
and watched the field move **1.1 %** for no reason but the clock.
**Claim** `C-0150` (shared with `T-225`). **Challenges reserved** `CH-0192`, `CH-0193`.
**Result** `gpd/results/T-225-departure-spelling-set.json` (this task's deliverables are recorded there
under `wallClock`, the two tasks sharing one sweep and one topological order).
**Leaf** none — a **process** task protecting the machine-readable artifact of every leaf.
**Verification type** **logical** (a corpus census for other wall clocks, and a reader-census proof that
the removal is schema-safe) **+ in-silico** (`T-172` re-emitted and diffed, then its three readers
re-emitted after it and diffed).

## What is outstanding

`CLAUDE.md` records the rule verbatim:

> **A WALL CLOCK in a result file is a step counter by another name.** … `CLAUDE.md`'s own
> *"emit the answer and a convergence measure; emit nothing that counts steps"* covers it and it was
> still emitted — and **one such field makes the whole file permanently un-diffable**, which is the
> check the rounding layer exists to enable. Put the timing in the console log, never in the JSON.

`structure/RowEndPrestrainStudy.kt` emits `"elapsedSeconds" to elapsed` into `parameters`.
`C-0138` did not repair it, because **removing a field is a schema change to a file with three readers**.

## Locked units and conventions

Nothing physical is computed. Units unchanged and untouched.

---

## Numeric target and acceptance predicates

**P1 (the census is exhaustive).** Every numeric field in the corpus whose name or value could be a wall
clock is enumerated and classified; the claim states how many there are and why each non-instance is not
one. `C-0138` names two near-misses (`T-119`'s configured pause, `T-7`'s viscosity) and they are re-checked.

**P2 (the schema change is proved safe before it is made).** For each of the three readers, the exact
block it reads out of `T-172` is quoted, and it is shown that `parameters` is not it.

**P3 (`T-172` is re-emitted and diffed).** The only field that disappears is `parameters/elapsedSeconds`;
nothing else moves.

**P4 (the three readers are re-emitted AFTER it, in `tools/reemission-order.py`'s order, and diffed).**
`C-0110`: a **proof** that a change is invisible is not a substitute for running the consumers, because
the run also checks everything the proof was not about.

**P5 (the timing survives, in the console).** The measurement is worth making — `CLAUDE.md` says so — so
the elapsed time is printed, not emitted, and the claim quotes it with its spread over the runs made here.

---

## Plan

One line of Kotlin, one lattice-solve re-run, and three consumer re-runs. The cheap bound — the reader
census plus a `grep` of the three call sites — runs first and decides between *removal* and a *sentinel*:
if any reader deserialised `parameters` into a closed schema, removal would break it and a frozen
sentinel would be the right repair instead.

**What would falsify this approach.** A reader that reads `parameters` as a whole map, or a test that
asserts the field's presence: then removal is a breaking change and the sentinel is the answer.

## Falsifiers

- **F6** — a reader of `T-172` fails, or moves a number, after the field is removed.
- **F7** — the corpus census finds a second wall clock, i.e. `C-0138`'s *"exactly one"* is wrong.
