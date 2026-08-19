# T-225 — the departure rule's own SPELLING set: which names are the rule's quantity, and the proof that the logarithms are not

**Raised by** [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`) and
[`CH-0169`](../challenges/CH-0169-four-spellings-of-eleven-and-four-implementations.md).
**Claim** `C-0150`. **Challenges reserved** `CH-0192`, `CH-0193`.
**Result** `gpd/results/T-225-departure-spelling-set.json`.
**Leaf** none — a **process** task protecting the machine-readable artifact of every leaf.
**Verification type** **logical** (a census over the committed corpus, a per-key classification with a
stated ground, an offline simulation of the widening over every committed result file, and checker and
emitter self-tests) **+ in-silico** (every affected study re-run in one topological order and diffed
field by field against its **committed** version read out of `git`).

## What is outstanding

`DEPARTURE_SPELLINGS`'s KDoc says *"Every spelling the corpus uses for a departure **inside** a
`DEPARTURE_RECORDS` record"* and enumerates **four**. `CH-0169` measured **seven more** — 62 fields in
6 files — and **refused to sweep them**, because the residue is not mechanical: two of the nine are
`log₁₀` of a residual, and two significant digits on a logarithm is a different statement entirely.

So this task is not *"widen the set"*. It is:

1. **classify every candidate** as *a residual between two refinements of one solve* (in),
   *an answer of the study* (out) or *a logarithm of one* (out), each with a stated ground;
2. **prove the exclusions numerically**, not by assertion;
3. widen the set to the in-scope names, in the layer every study goes through;
4. re-emit the affected files in **one** `tools/reemission-order.py` order;
5. leave the residue **measured and published**, not narrowed away.

## Locked units and conventions

Nothing physical is computed. Units unchanged and untouched: nm, pN, pN/nm, pressure in pN/nm² = 1 MPa
exactly, `k_BT = 4.141947 pN·nm` at 300 K in aqueous buffer with stated Mg²⁺.

A **departure** is what `C-0093` means by it and what `structure/ResultRounding.kt` encodes: a
**dimensionless** difference, ratio or spread of two or more nearly equal numbers that would be *equal in
exact arithmetic*, emitted inside one of `DEPARTURE_RECORDS`. **Dimensionless** and **would-be-equal** are
both part of the definition — the first is what `RESULT_ABSOLUTE_FLOOR` cannot reach (`P-18`) and what
`CH-0154` measured on `T-193`'s volts; the second is what separates a residual from a *level*.

---

## Numeric target and acceptance predicates

**P1 (cheap bound, runs first).** The census is re-derived from the corpus rather than inherited, and the
derivation states the **shape** it searches for rather than a list of names. If it finds more than
`CH-0169`'s nine, that is a finding about the census instrument, not about the corpus.

**P2 (the exclusions are proved, not asserted).** For every key classified **out**, the claim carries a
number: how far the rule would move the quantity it stands for, and what in the corpus that movement
would contradict. An exclusion argued only from a name is not an exclusion.

**P3 (the widening is by construction).** The in-scope names enter `DEPARTURE_SPELLINGS`, so every study
obeys the widened rule through `roundedForResult`'s baseline. The one rounding implementation
`C-0138` left un-delegated — `brush/ScfDensityProfileStudy.kt`'s private one — is delegated, closing
`CH-0169`'s Ground 2 at 6 of 6.

**P4 (the blast radius is bounded offline first).** The widening is simulated in Python over **every**
committed result file before any study is re-run, and the predicted `(file, field)` set is exactly what
the sweep moves.

**P5 (one topological order).** Every affected file is re-emitted in one `tools/reemission-order.py`
order over the whole set, through `tools/study-batch.sh`, and the order is retained as a file.

**P6 (nothing stale, as an identity).** Every reproduction and convergence residual in every re-emitted
file is exactly the two-significant-digit rounding of its own committed value, counted, with **0**
unexplained.

**P7 (what moved, by kind).** The diff is classified: departure fields / other numeric / verdicts /
wording / booleans / fields added or removed. Anything but the first is a finding.

**P8 (the gate is mutation-tested in both directions).** The widened predicate is held open by named
self-tests, and the count that **fails if it narrows back** and the count that **fails if an excluded key
is swept in** are both measured, not asserted (`C-0127`).

**P9 (the residue is published with its own cost).** Whatever stays outside the rule is named, counted
and priced (`CH-0168`).

---

## Plan — method, and the cost it is justified against

**Cheap bound before expensive work.** Three bounds run before a single study:

| bound | cost | what it decides |
|---|---|---|
| the **shape** census over all committed files | seconds | how many candidates there really are |
| the per-key **classification** against its emission site | minutes of reading | how many files must be re-emitted at all |
| the **offline simulation** of the widened rule | seconds | the exact `(file, field)` blast radius |

The expensive half is the re-emission: `T-1d` is an SCF solve and `T-3a`/`T-3b`/`T-60` are
Poisson-Boltzmann field solves. Every key ruled **out** by the classification removes a file from that
list, so the classification is worth far more than it costs.

**What would falsify this approach.** If the classification cannot be made from the emission site and the
corpus — if a key's status genuinely needs a new measurement — then the right output is a recorded
decision that the name stays outside the rule, with the measurement queued. If the offline simulation
predicts a movement in a field that is **not** a departure, the widening is wrong and must be narrowed
before anything is re-run.

## Falsifiers

- **F1** — a re-emitted file moves a **non-departure** numeric field, a verdict, a boolean, or a word.
- **F2** — the offline simulation's predicted `(file, field)` set differs from what the sweep moves.
- **F3** — a re-emitted file's residuals are *not* the two-digit rounding of their committed values.
- **F4** — an excluded key can be shown, from the corpus, to be a residual between two refinements after
  all; or an included key can be shown to be a level or an answer.
- **F5** — the widened gate does not come clean, or the mutation test finds a direction in which the
  predicate can be changed without failing a named test.
