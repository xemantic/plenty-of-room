# T-194 — The buffer and the tall layer are ONE reserve

**Leaf:** `A2.2`
**Raised by:** NDI's answers to decisions 1 and 2, 2026-08-18
**Verification type:** logical — a ranking assembled from two upstream result files
**Units:** nm for gaps and heights, mM for concentration, dimensionless for ratios

---

## Formulate

NDI answered decisions 1 and 2 separately and **both answers name the same reserve**:

> *"pushing a parameter hard that I've been reserving for additional operating margin"* (decision 1)
> *"an interesting regime we've been reserving, **again**, for low MgCl₂ concentrations we'd buy with
> additional work on stabilizing DNA origami at low salt"* (decision 2)

So the buffer and the tall layer are two claimants on one budget line and NDI can spend it once.
**This programme asked as two what can only be spent once** —
which is `C-0091`'s own finding, *counting routes that are one number*, arriving in the **deliverable**
rather than in the corpus.

### Acceptance predicate

A single decision entry in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) replacing decisions 1 and 2,
which:

1. states what **each spend buys**, in the corpus's own numbers, each read at the state the device occupies;
2. states the **common-mode qualifier** on the buffer routes explicitly rather than by implication —
   three surviving routes, all smaller than the one-loop correction that is common mode to all of them;
3. states plainly **which column this programme can rank and which it cannot**, and does not let a
   statement about the physics stand in for a statement about the cost;
4. and quotes no number that cannot be grepped out of a result file.

**Falsifier.**
If the two spends turn out **not** to draw on one reserve — if NDI's two *"reserving"* clauses name
different budgets — the re-issue is wrong and the two decisions stay separate.
Second falsifier, and the one that fired: **if either spend's value moves between the queue row and the
measurement, the ranking is not a re-issue but a correction**, and it must be labelled as one.

---

## Plan

**Cheap bound first, and it is a division rather than a solve.**
Both spends are already priced somewhere in the corpus:
the buffer in `C-0091`'s census (`T-156`), the layer in `C-0050` — and, as of this same iteration,
in `C-0110` (`T-192`), which is the first claim ever to evaluate the **bias** at a tall gap.
So the whole task is a read, and its only real risk is quoting a number the corpus has since moved.

**Method.** A retained emitter, [`tools/T-194-emit-result.py`](../../tools/T-194-emit-result.py),
that derives every number from the two upstream result files **at run time**, so nothing is transcribed
and the ranking can be re-derived by whoever reads it next.
Then the decision entry is rewritten from that file.

**Justification against cost.**
The alternative is to write the ranking from memory of the two claims,
which is exactly how `C-0067` found 414 numbers traceable and three *answers* stale:
**a synthesis drifts by keeping its answers, not by mis-copying its numbers.**

**What would falsify the approach.**
That the ranking is not decidable on this programme's numbers at all —
which is the honest outcome if the two spends buy incommensurable things.
It is not: one of them buys nothing.
