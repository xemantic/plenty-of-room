# C-0078 — The drift `C-0067` named as the worst is the one its own tool cannot see, and two iterations later it was back: of three places `ANSWERS.md` asserts a task is open, one was closed seven weeks of loop-time earlier, and the numeric half found a **headline list** of four figures in which three do not match the claim it cites

| | |
|---|---|
| **Task** | [`P-20`](../../TASKS.md) — raised and closed inside iteration 15 by the coordinator, while `T-136`, `T-137` and `T-1e` ran in parallel |
| **Verification type** | **logical** (a mechanical check of the deliverable against the claim corpus and against the queue, with the checker itself verified by 42 in-memory fixtures) |
| **Verdict** | **PASS, and the finding is a recurrence.** `C-0067` established that the drift in this project's primary deliverable is in the **status** of answers rather than the value of numbers, and built a tracer that checks **numbers**. Two iterations on, of the three places `ANSWERS.md` asserts a task is still open, **one is stale** — `T-129`, closed by [`C-0068`](C-0068-range-robust-placement.md) in iteration 13 — and the numeric half reports **5 ABSENT tokens of 590**, of which **three are one headline list**. Both are now zero, and the status class is checked by machine rather than by memory. |
| **Maturity** | **TRL 1–3**, and lower than that: nothing here is physics. It is a property of this repository's own documents. No number is re-derived and no verdict of any claim is examined — only whether the synthesis reports them. |
| **Provenance** | [`tools/trace-answers.py`](../../tools/trace-answers.py) (extended: `queue_status`, `open_assertions`, `stale_statuses`, and an unconditional status report in `main`); **42 checks** in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py), up from 22, written test-first; corrections to [`ANSWERS.md`](../../ANSWERS.md) §4 row (g) and the iteration-14 recommendation paragraph |
| **Conditions** | The corpus as of commit `7fcaec7`: 73 claims, 85 challenges, 131 queue rows. No Kotlin, no solver, no units — the locked-unit invariant is not engaged. |
| **Consumes** | [`C-0067`](C-0067-answers-reconciliation.md) (the tracer, the finding it is built on, and the drift taxonomy), [`C-0064`](C-0064-robust-distribution.md) and [`C-0068`](C-0068-range-robust-placement.md) and [`C-0071`](C-0071-output-element-recommendation.md) (the numbers found to have drifted) |
| **Constrains** | `ANSWERS.md`. **No claim is touched and none is contradicted** — every disagreement found was the synthesis misreporting the corpus, which is also what `C-0067` found, so nothing is challenged. |

---

## 1. What was checked, and why the checker needed extending first

`C-0067` reconciled `ANSWERS.md` against the claim corpus line by line and reported the result
that shapes this task:

> The arithmetic is near-perfect — **414 of 415** numeric tokens appear in the corpus …
> so **the drift is in the STATUS of answers, not in the value of numbers**. Worst kind, three
> instances: entries of *"what we cannot answer"* that the programme had **answered** and left
> standing for up to **seven iterations** — *a deliverable that under-claims is as wrong as one
> that over-claims and is far harder to catch*, because a reviewer's instinct is to check the
> assertions and not the disclaimers.

It then retained a tool so the next pass would cost one command. **The tool checks numbers.**

That is not a criticism of `C-0067` so much as the point of this claim: the finding and the
instrument were written in the same hour, and the instrument was built for the half of the
finding that was easy to mechanise. The half named as *worst* was left to the next reader's
memory — and a check that depends on someone remembering to perform it is not a check. The
first thing `P-20` did was therefore to close that gap, before looking at the deliverable at
all.

### The status check

A stale *"`T-129`, open"* contains **no number**, so no numeric check can reach it. What can
reach it is `TASKS.md`, which is the register that knows whether a task is open, and which the
loop updates every iteration by its own standing instruction. The extension is three functions:

| function | what it does |
|---|---|
| `queue_status(TASKS.md)` | `{task: OPEN \| CLOSED \| IN PROGRESS}` for every one of the queue's **131** rows |
| `open_assertions(ANSWERS.md)` | every place the deliverable **asserts** a task is still open |
| `stale_statuses(…)` | the intersection: an assertion the queue contradicts |

It runs **unconditionally** in `main`, reporting to stderr beside the token summary, for the
reason above.

### Five traps, all found by running it against the real files

These are in the tests because each of them was live, and because a checker's false modes cost
more than its true ones — a false positive sends an agent to "correct" a passage that is
already right, and the tool exists in order to be believed.

| trap | why it fires | guard |
|---|---|---|
| `"Left undone: the window is unsynthesised"` | contains `DONE` | whole-word, **case-sensitive** matching |
| `"this cannot be answered without a measurement"` | upper-casing the row gives `ANSWERED` | the queue writes verdicts in bold upper case and prose in lower case |
| `"see the ABANDONED branch"` | contains `DONE` | word boundaries |
| `` "`T-45`, open since iteration 3, is answered" `` | *history*, and the same sentence closes it | `open since\|for\|from` cancels |
| `"the open question `T-60` was resolved by a later claim"` | reports a closure | an answering word in the window cancels |

The obvious implementation — upper-case the row, test for substrings — fails the first three.

**22 checks → 42.** Every one was written before the function it tests, and watched to fail.

---

## 2. The status finding

Three places in `ANSWERS.md` assert a task is open. After the two false positives above are
excluded, **two** are genuine assertions and **one of those is stale**:

| line | assertion | queue says | verdict |
|---|---|---|---|
| 51 | `` "`T-45`, open since iteration 3, is answered from published measurement" `` | CLOSED | **not an assertion** — history, correctly written |
| 194 | `` "`T-95`, open" `` | OPEN | **correct**; it is a specification question for NDI |
| 527 | `` "whether it is flat over a range is `T-129`, open" `` | CLOSED | **STALE** |

`T-129` was answered in **iteration 13** by [`C-0068`](C-0068-range-robust-placement.md), and
the answer is a substantive one that the deliverable was therefore not carrying:

- `C-0063`'s placement **is** flat over a range with **equal springs** — **0.0789** over the
  whole range `C-0018`'s placed 2 mM device traverses, **0.0853** at 0.5 mM, **0.0896** at
  10 mM, all three inside `T-5b`'s 0.10 against 0.0706 at the single state. What the range
  costs is the **margin**: 1.42× becomes 1.12×, spent at the compressed end.
- **The exception is the 5 nm device**, whose range owns `C-0022`'s 2 nm state: equal springs
  dish **0.2000** and are worse than no coupling at all at both of its states. A distribution
  recovers all four (0.0291 / 0.0365 / **0.0565** / 0.0382) at peak ratios of only 1.72–2.32 —
  a scope correction rather than an infeasibility ([`CH-0080`](../challenges/CH-0080-the-equal-spring-advantage-belongs-to-the-ten-nanometre-layer.md)).
- **The crossover phase is selected by the LAYER**, not only by the sheet: 0 of 198 288
  centro-symmetric placements at phase 24 beat `C-0063`'s own under a range objective, while
  under the 5 nm device's range nothing at phase 24 clears at all and a **phase-8** placement
  does.

This is `C-0067`'s own diagnosis recurring on its own axis, one iteration after the tool that
was supposed to prevent it. **A deliverable that under-claims is harder to catch than one that
over-claims** — and, it turns out, harder to *stop*, because the thing that goes stale is the
sentence a writer is least likely to re-read.

---

## 3. The numeric finding: a headline list in which three of four figures are wrong

`C-0067` reported **414 of 415** tokens present. The re-run reports **585 of 590** — five
ABSENT, on two lines.

### Line 527 — `C-0064`'s four device-range minimaxes

The deliverable prints, as a bolded headline list:

> the minimax is **0.0372 / 0.0436 / 0.0619 / 0.0500 — all four inside `T-5b`'s 0.10**

[`C-0064`](C-0064-robust-distribution.md)'s own table says:

| device | `C-0064` | `ANSWERS.md` | |
|---|---|---|---|
| 2 mM, `L₀` = 10 nm, 0.192 V | **0.0373** | 0.0372 | the *other* member of the manifold |
| 0.5 mM, `L₀` = 10 nm, 0.134 V | **0.0435** | 0.0436 | last digit |
| 2 mM, `L₀` = 5 nm, 0.368 V | **0.0620** | 0.0619 | last digit |
| 10 mM, `L₀` = 10 nm, 0.192 V | **0.0504** | 0.0500 | **third significant figure** |

**The first is the interesting one, and it is not a transcription error.** `C-0064`'s own
Part 3 records that its optimum is a **manifold** — 2 active constraints against 44 free
directions — and that two builds of the search differing only in their decision precision
returned equally good points at **0.0373** and **0.0372**, whose per-path spreads were 26.0×
and 7.9× and whose two-level projections landed on **opposite sides** of `T-5b`'s line. So
`0.0372` is a real number that this repository really produced; it is simply not the one the
standing claim publishes. A synthesis quoting the run rather than the claim is the failure mode
`SESSION-PROMPT.md` legislates against in as many words — *a subagent's chat report is a
summary written by an agent that may still be working; the claim, the challenge and the result
JSON are the artifacts* — and it survived because **all four numbers are individually
plausible and the verdict they support is unchanged.** All four are inside 0.10 either way.

The verdict is therefore **not** affected, and that is precisely why nothing caught it.

Corrected to the claim's figures, with each labelled by the device it belongs to, since three
of the four were unlabelled and their order was the only thing tying them to a device.

### Line 33 — `C-0071`'s three unmargined quantities

> **no margin at all on 3 of 14 graded quantities** (1.003×, 1.018×, 1.030×)

`C-0071` publishes **1.00314×**, **1.01844×** and **1.02964×**. Rounding a claim's number in
the synthesis is legitimate in general — but not these three, because
[`C-0072`](C-0072-plan-tolerance-model.md)'s whole finding is that they are **one arithmetic**,
`pitch − d − L = 0.0256 nm`, asserted equal to `1e−12`, and a reader cannot see that identity
through three-digit roundings. The precision *is* the content here. Restored to the claim's own
digits.

This is the same lesson as [`CH-0085`](../challenges/CH-0085-a-window-edge-quoted-at-a-tie.md)
one level out: **the renderer, not the writer, chose the precision**, and it chose wrong.

---

## 4. Result

| | before | after |
|---|---|---|
| numeric tokens ABSENT | **5** of 590 | **0** of 605 |
| open assertions contradicted by the queue | **1** of 2 | **0** of 2 |
| checks in `tools/test-trace-answers.py` | 22 | **42** |

**No claim is contradicted and no verdict moves**, at either end: `T-129`'s answer was already
filed and merely unreported, and all four minimax figures are inside `T-5b`'s 0.10 on both
readings. That is the second time in three iterations that a full reconciliation of the primary
deliverable has found **no claim wrong and the synthesis wrong anyway**, which is worth stating
as the standing conclusion: *the corpus is more reliable than the document that summarises it,
and the summary is what NDI reads.*

## 5. What this does not do

- It does **not** check the converse — a task the deliverable reports as closed and the queue
  carries as open. A synthesis is entitled to summarise a partial answer, so that direction has
  legitimate instances and would be noise.
- It does **not** check that a number *means* what the passage says it means. `C-0067` says so
  of the numeric half and it is equally true here: `ELSEWHERE` is still 111 tokens, adjudicated
  by hand or not at all.
- It does **not** cover the **claims** and **challenges** for internal staleness, only the
  deliverable. `C-0071` found two specification questions carried to NDI that had stopped
  applying, and no tool here would have caught that.
- **`ANSWERS.md` remains unsynthesised against iterations 8–15** (`C-0052`–`C-0078`,
  `CH-0065`–`CH-0085`), which `C-0067` recorded as a coverage statement and which this pass
  does not discharge. Raised as `T-139`.
