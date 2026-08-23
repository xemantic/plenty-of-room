# T-261 — a synthesis rests a number on a challenge the corpus has since answered

| | |
|---|---|
| **Leaf** | none — a **process** task, protecting the two customer-facing documents |
| **Raised by** | [`CH-0203`](../challenges/CH-0203-a-specification-question-was-posed-on-a-threshold-its-own-iteration-withdrew.md) / [`C-0155`](../claims/C-0155-tenth-answers-synthesis.md) (`T-257`) |
| **Verification type** | **logical** — a predicate over the two deliverables and the challenge corpus, with its false-positive rate measured by hand over every hit |
| **Units** | none; every value is an integer count, a line number or a name |

---

## Formulate

`C-0080` established that a synthesis drifts by keeping its **answers**, and `C-0088` mechanised
the **task** half.
The **challenge** half is where a **price** lives, and `CH-0203`'s live instance is decision **8**,
put to NDI priced at `CH-0185`'s *"six flat cells of eight against three"* in the same iteration
[`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) answered `CH-0185`.

The queue row states the gap precisely:

> The tracer **already parses challenge statuses** … so the existing arm catches *"`CH-0185` is
> open"* and not *"worth six cells (`CH-0185`)"*.

### The acceptance predicate

**PASS** iff all of:

- **`F1`** an executable arm exists that flags a deliverable passage resting on a challenge the
  corpus records as adjudicated, with named self-tests;
- **`F2`** its false-positive rate is **measured by hand over every hit**, not asserted — and if it
  cannot come clean it ships as an **audit**, printed and ungated (`C-0129`);
- **`F3`** the tracer's own exit code is **0** on both deliverables when the task closes, and every
  live defect it finds is **repaired** rather than suppressed (`C-0115`);
- **`F4`** the *reason* nothing caught the live instance is stated and tested, not guessed.

**What would falsify this approach**: a false-positive rate on the gated half above zero — a drift
checker's false positives cost more than its true ones, because the tool exists in order to be
believed (`C-0080`) — or a repair that silences the check rather than correcting the sentence.

---

## Plan

**The cheap bound runs first: count the hits before writing the arm.**
Under the naive reading — a number anywhere near a reference to an adjudicated challenge — the two
documents give **34** hits.
Reading them decides the design, and they are almost all **corrections**, for `CH-0230`'s reason:
a correcting sentence has to **name** the challenge in order to withdraw it.
So the predicate the acceptance names cannot be a gate, and the cheap bound says so before a line
is written.

**Then ask why the live instance was missed**, which is the half a hit count cannot answer.
Two candidate causes, both checkable in minutes:

1. the arm's **word list** — `stale_challenge_statuses` inherited `_OPEN_WORD_ASSERTION`, written
   for a *task*'s vocabulary;
2. the arm's **authority** — a challenge's own `**Status**` row, which nothing requires a later
   claim to update.

**Then TDD**: named tests first, for whichever cause is real, with the paired controls that stop a
"does not fire" test being satisfied by a predicate that never fires.

**Then repair what is found**, in the documents, `C-0071`-style: strike, never delete.

**Cost**: minutes. No solve, no Gradle, no re-emission of any physical study.
