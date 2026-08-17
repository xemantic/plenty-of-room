# T-147 — Third synthesis of `ANSWERS.md`, against iterations 8–16

| | |
|---|---|
| **Leaf** | — (a process task; it audits the deliverable that reports every leaf) |
| **Predecessor** | [`T-131`](T-131-answers-reconciliation.md) / [`C-0067`](../claims/C-0067-answers-reconciliation.md) (iteration 12, the first end-to-end reconciliation) and `P-20` / [`C-0078`](../claims/C-0078-status-drift-in-the-deliverable.md) (iteration 15, the status-drift half of the tracer) |
| **Verification type** | **logical** (a coverage audit of the claim corpus against the deliverable, then a statement-by-statement adjudication of every passage the range moves) **+ in-silico** ([`tools/trace-answers.py`](../../tools/trace-answers.py), 42 self-tests, run before and after) |
| **Status** | **DONE** (iteration 16) — claim [`C-0080`](../claims/C-0080-third-answers-synthesis.md) |

---

## Formulate

### The question

`C-0067` closed with a **coverage statement rather than an assumption**:

> The window is not re-synthesised against iterations 8–11. `C-0051` reaches `C-0050`/`CH-0062`.
> Recorded in `ANSWERS.md` as a coverage statement rather than assumed away; it is a task, not an edit.

`C-0078`, three iterations later, found that statement still true and the range grown.
This task discharges it: **is every claim and challenge from `C-0052`/`CH-0065` onward either reflected in
the deliverable, or recorded as deliberately not carried, with the reason?**

### Why a third pass is not a repeat of the first two

The two prior passes each found a different failure class, and each built the tool that finds *its own*:

- `C-0067` found the drift is in the **status** of answers, not the value of numbers — 414 of 415 numeric
  tokens traced, and three entries of *"what we cannot answer"* the programme had **answered**.
  It built a **numeric** tracer.
- `C-0078` found the status class back two iterations later and extended the tracer to read `TASKS.md`.

Both halves now run in one command and report **0 ABSENT and 0 stale** at the start of this task.
So the machine-checkable classes are clean by construction, and **whatever this pass finds is a class
neither tool can see.** Naming that class is the substance of the task.

### Locked units and conventions

Inherited unchanged from `T-131`, which this task extends rather than replaces:

- SI throughout; lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly),
  energies in `k_BT` at `T = 300 K`, `k_BT = 4.142 pN·nm`. Nothing here computes a new quantity.
- A **statement** is one assertion carrying at least one number or one verdict. Statements are the unit of
  adjudication; **numeric tokens** are the unit of the machine check.
- **TRACED / DRIFTED / UNTRACEABLE** as `T-131` defines them.
- A claim in the range is **REFLECTED** if `ANSWERS.md` cites its ID; **MATERIAL** if a reader answering
  NDI's eight tasks, six §4 questions or six specification questions would reach a different answer without
  it; **INTERIOR** otherwise — branch structure the deliverable deliberately does not carry.

### Acceptance predicates

| | predicate | falsifiable how |
|---|---|---|
| **`P1`** | Every claim `C-0052`–`C-0078` and every challenge `CH-0065`–`CH-0092` is classified REFLECTED / MATERIAL-AND-MISSING / INTERIOR, with one line of reason each, and the classification is emitted as JSON. | An item in the range absent from the audit, or a MATERIAL item a reader can show is INTERIOR (or the reverse) from the item's own claim file. |
| **`P2`** | Every MATERIAL-AND-MISSING item is carried into `ANSWERS.md` in place, with the prior wording preserved wherever it is superseded. | A correction that deletes rather than supersedes. |
| **`P3`** | Every INTERIOR item is listed **with its reason**, in the claim, so the next pass inherits the judgement rather than re-deriving it. | An uncarried item whose reason is not stated. |
| **`P4`** | The **ground** of every verdict whose premise was withdrawn in the range is re-checked and reported, per `C-0078`'s own lesson. | A verdict left standing in the file whose only stated ground has been withdrawn. |
| **`P5`** | `tools/trace-answers.py` reports **0 ABSENT and 0 stale** after the edits, as it does before them. | A non-zero count. |
| **`P6`** | `T-139`, in flight this iteration and capable of moving the plan-margin story, is named in the deliverable as open, with what it would move. | The deliverable asserting a plan-margin verdict `T-139` could reverse without saying so. |

### The declared falsifier

**The falsifier is that `C-0067`'s taxonomy is complete** — that the only classes of drift in this file are
(i) a number in no claim and (ii) a task asserted open that the queue says is closed, both of which
`tools/trace-answers.py` now reports at zero.

It **fires** if this pass finds a **third class**: a passage of `ANSWERS.md` carrying a number that is still
in the corpus and therefore still reads `CITED`, and a verdict that is still literally what its owning claim
says, **which a LATER claim in the range has superseded** — a *superseded standing value*, invisible to both
halves of the tracer precisely because the superseded number has an owner.

If no such passage exists, the two tools between them are a sufficient audit of this deliverable and a third
human pass was not needed; that is a reportable negative and it would retire this task class.

**Result: the falsifier FIRED.** See the claim, Part 3.

---

## Plan

### Method, and why this one

Three stages, cheap first, and the cheap one bounds the expensive one.

1. **The mechanical baseline runs first and costs one command.** `tools/trace-answers.py` reports the two
   classes it can see. It reports 613 tokens, 0 ABSENT, 0 stale before any edit. That is not a licence to
   skip the reading; it is what tells us the reading is the whole cost.
2. **The coverage audit is the cheap bound on the prose.** One `grep` per ID over `ANSWERS.md` partitions the
   55-item range into cited and uncited in seconds, and *the uncited set bounds how much prose there is to
   write*. Only then is any claim read in full. Doing it the other way — reading 55 claims and then asking
   which are missing — costs 55 reads to learn what one `grep` says.
3. **The adjudication is the expensive half and only it can find the third class.** For each uncited item,
   read the claim's title, Verdict, `Consumes`, `Constrains` and `Raises` rows, and ask `T-131`'s three
   questions **plus one this task adds**: *does this claim supersede a value `ANSWERS.md` currently carries
   from an earlier claim?* That fourth question is the falsifier.

### Justification against cost

The alternative is to re-run the window intersection against iterations 8–16, which is what `C-0051` did for
iterations 5–7 and what the debt is nominally about. It is the wrong instrument here for the reason `C-0051`
itself established: **exactly one of its twenty claims was a function of `σ` at all**, so the intersection
returned a null and the null was uninformative until the *census* was reported beside it. On inspection the
present range is worse — it is placements, plan views, lattice congruences, torsion closures, a tolerance
model and two repository-numerics claims. A re-intersection would cost a study and return the same null.
**What the range actually contains is a stated recommendation and its price**, and that is a synthesis
problem, not a window problem.

### What is deliberately not done

- **No claim is re-run and no number is re-derived.** Where a claim and `ANSWERS.md` disagree, the claim wins.
- **The `(σ, L₀)` window is not re-intersected**, for the reason above. The census is reported instead, as
  `C-0051` reports one, so a stationary window is not mistaken for a defended one.
- **`T-139`'s answer is not anticipated.** It is in flight in this same iteration and it decides `C-0069`'s
  `Q5` outright. The deliverable states it as open and states what it would move, which is `C-0067`'s own
  practice and the only honest form.
- **`tools/trace-answers.py` is not edited** — a sibling agent is in `tools/` this iteration. Any change it
  needs is reported, not made.
