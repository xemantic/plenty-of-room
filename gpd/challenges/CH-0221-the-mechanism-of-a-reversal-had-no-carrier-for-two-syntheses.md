# CH-0221 — **`C-0154` measured the mechanism of iteration 40's reversal in iteration 38, and TWO synthesis passes carried it into NEITHER deliverable — because a claim about a MODEL has no verdict sentence to attach itself to.** `grep` for `C-0154`, `24/7`, `dimer`, `acrossHelixRigidity` and `0.0449400126` over `ANSWERS.md` and `DECISIONS-FOR-NDI.md` at `9e35853` returns **0 hits in 2 files**, while both documents state coupled-flatness verdicts that the same `24/7` overstatement makes wrong

| | |
|---|---|
| **Against** | [`C-0155`](../claims/C-0155-tenth-answers-synthesis.md) (the tenth pass, `T-260`) and [`C-0165`](../claims/C-0165-eleventh-answers-synthesis.md) (the eleventh, `T-271`) — neither consumes [`C-0154`](../claims/C-0154-honeycomb-grillage.md), and neither deliverable cites it |
| **Raised by** | [`C-0171`](../claims/C-0171-twelfth-answers-synthesis.md) (`T-276`), the twelfth pass, while carrying the reversal `C-0154` predicted |
| **Grounds** | **a census, and it costs one `grep`.** `C-0154` was filed in commit `49b1a01`, iteration 38, in the *same commit* as `C-0155`. It measures that `OrigamiSheet.acrossHelixRigidity` reproduces a honeycomb block's `D_∥` at `2.8e−15` and overstates its `D_⊥` by `24/7 = 3.42857×`, and that its `10 × 6` block is **outside** `T-5b` at enhancement 1.0 (`0.127358454`). Both facts are premises of statements standing in both deliverables — the coupled-cell counts, and `10 × 6`'s *"no coupling-fraction threshold at all, flat even at `f = 0`"*. Two passes read the corpus for carriers and found none, because **`C-0154` states no verdict about the device**: it states a property of a *function*. `C-0167` converted it into a verdict in iteration 40 and the carriers became visible instantly |
| **Status** | **RAISED, and REPAIRED in the same pass.** Both deliverables now carry `C-0154`, and the drift class is named. **No number of `C-0155` or `C-0165` is disputed** — every reading either pass recorded was correct at the time it was taken; what is challenged is the **search**, which is keyed on verdicts |

---

## 1. What the two passes did, and why it was not enough

Both passes ran the retained checkers on both documents, ran a challenge-status cross-check the
tracer cannot do, and re-derived the self-describing counts. All of that is right, and all of it is
keyed on **statements**: a number with an owner, a task with a status, a challenge with a status.

`C-0154` produces none of those. Its subject is `structure/OrigamiSheet.kt`, its verdict is *"this
formula does not describe this lattice"*, and the sentence a synthesis would have to write is not a
correction to any existing sentence — it is a **new** validity range on every sentence downstream of
that formula. There is no token to `grep`, no status to compare, and no passage that contradicts it.

## 2. Why it matters, measured rather than asserted

`C-0167` re-graded the cells two iterations later. What the two passes were carrying meanwhile:

| carried in both deliverables | what `C-0154` had already implied |
|---|---|
| *"a COUPLED four-layer tile is flat under the measured folding statistics"* | the lattice those cells were solved on has `24/7` too much across-helix rigidity |
| *"`10 × 6` has **no** threshold at all, flat even at `f = 0`"* | at enhancement 1.0 the honeycomb block dishes **0.127358454** and is **not** flat |
| *"it **removes** the last unmeasured dependency in the flatness verdict"* | it **bounds** it; the dependency is load-bearing on `10 × 6` too |

The second and third of those are settled by `C-0154`'s own published table and needed no re-grade at
all. A synthesis that had carried `C-0154` would have withdrawn them in iteration 38.

## 3. The general form

**A claim whose subject is a MODEL rather than a design has no carrier passage, so a synthesis keyed
on verdicts cannot see it until a later claim converts it into a verdict.** The detector is cheap
and it is the same shape as `C-0088`'s: for each claim filed since the last pass, ask *what sentence
in the deliverables would be different if this claim were true* — and where the answer is *"none,
yet"*, record it as a **pending premise** rather than as nothing.

## 4. What this does NOT establish

- Nothing about `C-0154`'s own correctness, which is not in question.
- No number of `C-0155`, `C-0165` or either deliverable is disputed on its own terms.
- The repair here is textual and per-pass; **mechanising the detector is not attempted**, because a
  *"which sentence would change"* test is a judgement and this repository's own standard is that an
  unmeasured false-positive rate is what makes a checker stop being believed.
