# T-184 — `DECISIONS-FOR-NDI.md` has the same drift class the deliverable had

| | |
|---|---|
| **Leaf** | — (a process task, in the family of `T-131`, `T-147`, `T-172`, `T-175`, `T-183`) |
| **Raised by** | [`C-0106`](../claims/C-0106-fourth-answers-synthesis.md) |
| **Verification type** | **logical** — every assertion is adjudicated against the claim corpus and `TASKS.md`, which are the artifacts |
| **Units** | none are derived here; every quantity quoted is grepped out of its owning claim or result file |
| **Maturity** | **below TRL 1–3: nothing here is physics.** No number is computed. The output is a classification of statements in an outward-facing document |

## Formulate

**The gap.** This programme has built four retained checkers for `ANSWERS.md` —
numbers (`C-0067`/`T-131`), task statuses (`C-0078`), self-consistency (`C-0088`), challenge statuses (`C-0113`/`T-183`) —
because six synthesis passes found the same drift classes recurring.
`tools/trace-answers.py` reads `ANSWERS.md` and nothing else.

**`DECISIONS-FOR-NDI.md` is the document NDI actually reads, and nothing checks it at all.**

**The class.** `C-0067`'s standing finding is that *a synthesis drifts by keeping its ANSWERS, not by mis-copying its numbers*,
and that **a deliverable that under-claims is as wrong as one that over-claims and is far harder to catch**,
because a reviewer's instinct is to check the assertions and not the disclaimers.
A decision file is made almost entirely of disclaimers:
every *"has not been run"*, *"nothing here has evaluated"*, *"cannot be scoped"* is exactly that shape.

**Numeric target.** Every assertion in `DECISIONS-FOR-NDI.md` of the form
*"X has not been done / cannot be answered / nothing has evaluated / T-N is open"*,
plus every internal **count** assertion,
enumerated, adjudicated against the corpus and the queue, and given a verdict.

**Acceptance predicate (falsifiable).**

- **P1** — the cheap-bound grep count is reported **before** anything is read, and the enumerated set is a **superset** of it.
- **P2** — every enumerated assertion carries a verdict in `{STANDS, STALE, SUPERSEDED-IN-FILE, PATCHED-BELOW, SELF-CONTRADICTION}` and a named artifact (claim, task row, or file line) that decides it.
- **P3** — every `STALE` assertion is corrected in the file **struck, never deleted**, per the file's own stated discipline and `C-0071`'s.
- **P4** — the four retained checkers are run before and after and both readings are quoted; none regresses.
- **P5** — a retained checker is either shipped with self-tests, or its absence is priced with a measured or explicitly-unmeasured false-positive rate, per `C-0067`'s standing rule.

**What would falsify the approach.** If the cheap-bound grep returned a handful of assertions all already struck,
this is a re-read and not a product, and the honest output is *"the file is current"*.

## Plan

1. **Cheap bound first.** One `grep -cE` over a declared phrasing set. Report the count. (Result: **31** matching lines.)
2. Enumerate every referenced `T-`/`P-` id and resolve its status from `TASKS.md`.
   That is mechanical and it is the half `C-0078` proved can be mechanised.
3. Adjudicate the corpus assertions by hand against the claims filed since the file was last touched (`C-0109`–`C-0123`).
4. Correct in place, **struck**.
5. Decide the checker question on measured evidence: how many of the enumerated defects would a
   mechanical check have caught, and at what false-positive rate.

**Cost justification.** The whole task is greps and reading. The alternative — leaving it —
is that the outward-facing document tells NDI a route is untested when the programme has tested it,
which is precisely the failure `C-0106` found in question 6 and the reason this task exists.
