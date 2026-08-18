# T-175 — Fourth synthesis of `ANSWERS.md`, against iterations 16–21

| | |
|---|---|
| **Leaf** | — (a process task; it audits the deliverable that reports every leaf) |
| **Predecessor** | [`T-147`](T-147-third-answers-synthesis.md) / [`C-0080`](../claims/C-0080-third-answers-synthesis.md) (iteration 16, the third synthesis, which reached `C-0078`/`CH-0092`) and `T-150` / [`C-0088`](../claims/C-0088-does-the-deliverable-agree-with-itself.md) (iteration 17, which mechanised the self-consistency half and declared the fourth class un-mechanisable) |
| **Verification type** | **logical** (a coverage partition of the range against the deliverable, then a statement-by-statement adjudication with every owning claim read) **+ in-silico** ([`tools/trace-answers.py`](../../tools/trace-answers.py) and [`tools/check-markdown-tables.py`](../../tools/check-markdown-tables.py), unmodified, run before and after) |
| **Status** | **DONE** (iteration 22) — claim [`C-0106`](../claims/C-0106-fourth-answers-synthesis.md), raises [`CH-0121`](../challenges/CH-0121-two-of-the-three-phase-demand-sets-are-identical-not-disjoint.md) |

---

## Formulate

### The question

`C-0080` synthesised `ANSWERS.md` through iteration 15.
Iterations 16–21 added **24 claims** (`C-0081`–`C-0105`, `C-0094` unused) and **24 challenges**
(`CH-0093`–`CH-0120`, four numbers unused): **48 items**.

**Is every one of them either reflected in the deliverable, or recorded as deliberately not carried, with
the reason?**

### What is NOT owed, and why that is the whole shape of the task

The coordinator has edited `ANSWERS.md` incrementally in each of those six iterations, and the three
mechanical checks are clean before this pass begins: **0 ABSENT of 855 tokens, 0 stale statuses, 0
self-contradictions**.
So nothing is owed on **provenance**, and everything owed is owed on **reading**.

`C-0080`'s third drift class is the target — *a number still in the corpus, under a verdict its owning claim
still states, that a LATER claim superseded*.
It reads `CITED` **because** it has an owner. `C-0088` Part 4 establishes that it cannot be mechanised
without a `superseded-by` edge the corpus does not carry, prices the one cheap approximation and declines to
ship it untested. **It is therefore a hand search, and it is the only part of this task a tool cannot do.**

### Acceptance

1. Every item in `C-0081`–`C-0105` / `CH-0093`–`CH-0120` classified `REFLECTED` / `CARRIED IN` /
   `NOT CARRIED`, with a reason, emitted as JSON.
2. Everything classified material and missing is in the deliverable afterwards.
3. A statement of what is deliberately **not** carried and why, so the next pass inherits the judgement
   rather than re-deriving it.
4. The three mechanical checks report **zero** afterwards, and the table checker reports zero defects.
5. A falsifier declared **before** execution and reported either way.
6. `C-0051`'s census travels with any null: how many of the 48 are functions of `σ` at all.

### Units and conventions

None are engaged. Nothing is computed; every quantity is transferred with the unit its claim states, and
every number written into the deliverable is grepped out of its owning claim first
(`SESSION-PROMPT.md` step 9).

---

## Plan

### Cheap bound first

One `grep` per ID over `ANSWERS.md`, before any claim is opened.
It partitions 48 items in seconds and says how much prose there is; reading 48 claims to learn which are
missing costs 48 reads to learn what one `grep` says.
It is only a **bound** — being cited is not being carried correctly, and on `C-0080`'s range four of five
falsifier instances were in **cited** material.

### Then the reading, in the order difficulty rises

1. The **third drift class**, by hand, passage by passage.
2. `C-0078`'s rule — *a verdict that survives can survive on a different reason* — applied to every verdict
   whose premise the range withdrew.
3. *A number's owner is not the claim cited nearest to it*, and *a range that is a min/max over one claim's
   own table is stated by no claim* — quote the construction beside it.

### Declared falsifiers

- **`F1`.** The only drift classes left in this file are the two `C-0088` names — a superseded number that
  still reads `CITED`, and a task carrying two statuses — both of which are defects **inside passages that
  exist**. `F1` **fires** if the deliverable is missing a whole **structural determination** of the corpus:
  not a number needing correction but a finding with **no passage at all**, and specifically one that
  changes an object the file names in its own §2 (the eight tasks), §3 (the open questions) or §5 (the
  questions handed to NDI).
- **`F2`.** No claim in the range is wrong. Every disagreement is the synthesis misreporting the corpus,
  which is what all three prior passes found and is why none of them raised a challenge.

### What would make this approach the wrong one

If the cheap bound came back with most of the range cited and every cited passage correct, the pass would be
a re-read with no product, and the right answer would be to say so and spend the iteration elsewhere.
It did not: **34 of 48 items were uncited**, and two of the uncited clusters change the object the rest of
the file describes.

### Cost

Reading, one file and 48 claim headers, plus targeted full reads of the twelve claims whose numbers are
transferred. No solver, no build, no Gradle. Hours, not machine-hours — which is why it outranks a study
in an iteration where the deliverable is six iterations behind.
