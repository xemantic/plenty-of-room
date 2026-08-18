# T-201 — The fifth synthesis of `ANSWERS.md`

**Leaf:** — (process; it audits the deliverable that reports every leaf)
**Raised by:** three of the four agents of iteration 23, **independently**
**Verification type:** logical, with the three retained checkers run before and after
**Units:** none — this task moves text, not physical quantities

---

## Formulate

`ANSWERS.md` is the primary deliverable and it drifts by keeping its **answers**, not by mis-copying its
numbers (`C-0067`).
Four passes have been made at it.
This one faces the largest debt any of them has, and the debt was reported by **three different agents of one
iteration, none of which could see the others' reports** — which is itself the finding: a synthesis debt is
visible from inside every branch and from nowhere else.

RANGE: `C-0106`–`C-0114` and `CH-0121`–`CH-0131`.

### Acceptance predicate

1. Every item in range is partitioned **REFLECTED / CARRIED IN / DELIBERATELY NOT CARRIED**, with a reason
   per row, in a machine-readable record.
2. The cheap bound — which items the deliverable cited **before** the pass — is reported, and reported as
   under-reporting, per `C-0106`.
3. All three retained checkers are run **before and after**, and both readings are quoted. A firing is
   reported with its cause, never suppressed.
4. Every number the pass writes into the deliverable traces to the claim that owns it, at the precision that
   claim states it.
5. What is deliberately **not** carried is named with its reason.

**Falsifier.**
If the checkers were **not** clean before the pass, then the drift was of a kind a machine can already see,
and this pass is a repair rather than a synthesis — a materially different (and much weaker) result.
`C-0106` established the expectation that they *are* clean, because a determination with no passage is
invisible to every check in the tree.

---

## Plan

**Cheap bound first, and it runs before anything is opened**: one `grep` per ID over the deliverable.
Derived from `git show <base>:ANSWERS.md` rather than typed, so it cannot drift.
Cost: seconds, against a pass whose value depends entirely on whether it has a product or is a re-read.

**Method.** Read every claim and challenge in range, then adjudicate the deliverable statement by statement,
editing in place and keeping superseded readings **struck rather than deleted** — the repository's standing
rule, because a list that only grows is not a record and one that silently shrinks is worse.

**Justification against cost.** The alternative is to let the debt accumulate to a sixth pass. `C-0080` and
`C-0106` both found that the third drift class — a superseded standing value whose owner still states it — is
unmechanisable at statement granularity without a corpus convention change, so the only instrument is a pass.

**What would falsify the approach.**
That the deliverable's answers are all still correct and only its numbers moved — in which case the retained
numeric tracer would already have caught it and no pass would be needed.
The cheap bound tests this directly: 3 of 20 items cited means a product, not a re-read.
