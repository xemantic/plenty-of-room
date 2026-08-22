# P-29 — the queue's status vocabulary, mechanised

| | |
|---|---|
| **Raised by** | the coordinator of iteration 42, from a live `tools/verify.sh` failure |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests |
| **Units** | none; this is a document-integrity task |

## Formulate

`CLAUDE.md` states the rule already:

> **A status vocabulary GROWS, and every word your checker does not know is silently read as OPEN.**
> … Add the word with a test the day the queue coins it.

and, one entry later, its twin in the opposite and **unsafe** direction:

> **A STATUS QUALIFIER THAT CONTAINS A CLOSING WORD READS AS CLOSED** … a new status word must be
> tested in BOTH senses the day it is coined.

Both are conventions with no mechanism. Iteration 41 duly coined a third qualifier —
`**SECOND DELIVERABLE ANSWERED, iteration 41**` on the `T-9` row — and `tools/trace-answers.py`'s
`queue_status`, which matches a closing word anywhere after the identifier, read the row **CLOSED**.
`T-9`'s three deliverables are done (`C-0157`), done (`C-0169`) and **open** (the in-plane shear
`k_s`), and `ANSWERS.md` said so; the queue contradicted it.

**Numeric target.** `tools/trace-answers.py` exits 0 on both deliverables, and a *tested* gate
refuses any leading queue verdict that is in neither sense of a declared vocabulary.

**Acceptance predicate, falsifiable.**

1. `tools/trace-answers.py` and `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md` both exit 0
   against the committed tree — so `tools/verify.sh` is green on this axis.
2. A new checker reads **0 defects** on the repaired `TASKS.md` and **exit 1** on the `TASKS.md` of
   commit `7368986`, which is the commit that introduced the coinage.
3. Every mutation of the checker's predicate — widening **and** narrowing — fails a **named** test,
   measured rather than asserted (`C-0127`'s standard, and `CLAUDE.md`'s note that a predicate
   carrying exclusions has two mutation directions and the widening one is never written).
4. The checker's false-positive rate is **measured over the queue's own history**, not argued.

## Plan

**Cheap bound first.** Before writing anything, census what the predicate would have to be. Over
`TASKS.md`, bold runs *anywhere* in a row containing a closing word are **29 distinct**, and most
are prose about some *other* task (*"`CH-0185` is ANSWERED"*, *"CONTINGENT is not KILLED"*) — so a
gate on them is ~25 rows of noise and would be switched off within an iteration. Restricted to a
bold run that **opens a cell** and is at most six words, the same corpus gives **8 distinct**. That
census decides the predicate, and it costs one pass with no JVM.

**Check `CLAUDE.md`'s own prescribed sweep before writing a new one.** That entry names the remedy:
*"any row whose status cell starts `TODO` and which `queue_status` calls CLOSED"*. Run it before
trusting it.

**The part with content is not the list.** A vocabulary a checker can satisfy by appending a string
is not a check. Require, in addition, that every declared phrase **agrees with the reader**: each is
put through `trace-answers.queue_status` in a synthetic one-row queue, and a phrase declared *not
closing* that the reader closes is a defect of the reader. `CLAUDE.md`: *a checker's blind spot is
found by the tool that must agree with it.*

**What would falsify this approach.** If the leading-and-short predicate fires on legitimate rows of
the queue's own history at any appreciable rate, it is the wrong predicate and the right answer is
to teach `_NOT_CLOSED_QUALIFIER` alone and accept that coinages are found only when a deliverable
happens to contradict one. The measurement is 40 revisions of `TASKS.md`, and it is cheap.

## Deliverables

1. The `T-9` row restored to the queue's **established** vocabulary, keeping the scope as prose.
2. `tools/check-queue-vocabulary.py`, with `--census`, `--selftest` and the agreement check.
3. `tools/test-check-queue-vocabulary.py`, the mutation test, in both directions.
4. The gate wired into `tools/verify.sh`, and its self-test into `./gradlew test`.
5. A challenge on the fact that the gate that caught this was **red at the commit that wired it**.
