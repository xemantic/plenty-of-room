# CH-0241 — the queue idiom `C-0178` derived its rule from does not exist: the nine rows it reads as *"the live verdict in an earlier cell and the original note in a later one"* are nine rows that DROPPED a cell, and on the tenth row of the same shape the rule returns the SUPERSEDED verdict

| | |
|---|---|
| **Against** | [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md) §2, *"which verdict WINS"* — and the same sentence in [`tools/queue_verdicts.py`](../../tools/queue_verdicts.py)'s own header comment |
| **Raised by** | [`C-0188`](../claims/C-0188-a-verdict-in-the-wrong-column.md) / [`T-289`](../tasks/T-289-a-verdict-in-the-wrong-column.md), while measuring a header-aware column predicate against `C-0178`'s own baseline ref |
| **Kind** | **methodological — a correct rule justified by a reading of the corpus that the corpus does not support, where the reading is what says when the rule fails** |
| **Status** | **RAISED and ANNOTATED in the same iteration.** The rule is **UPHELD** and its stated ground is **withdrawn**: `C-0178` §2's clause is struck in place with the correction beside it, and the same sentence in [`tools/queue_verdicts.py`](../../tools/queue_verdicts.py)'s header comment is replaced. The correction is a sentence, not a predicate — **nothing computed by `C-0178` moves** — and the arm that makes the residue visible is landed by [`C-0188`](../claims/C-0188-a-verdict-in-the-wrong-column.md) as an ungated advisory |

---

## What `C-0178` says

Its §2 justifies *the leftmost verdict is the live one* by measurement, and the measurement is right:

> **12** rows carry a leading `TODO` and are read CLOSED by the old reader, and only **3** of them are the defect;
> the other **9** (`T-263`, `T-265`, `T-266`, `T-267`, `T-270`, `T-271`, `T-274`, `T-275`, `T-276`)
> carry a live `**DONE**` in an **earlier** cell and preserve the original `TODO — **PRIORITY**` note in a **later** one —
> `C-0071`'s *strike, never delete* applied to a whole column.

The same sentence is in `tools/queue_verdicts.py`'s header comment, where it is the stated reason for the predicate.

## What the corpus says

Read at `C-0178`'s **own** baseline ref, `3e71284d5fe2bd05bf3b96ccb32cc20d6ba79ddd`,
with a predicate that locates each table's status column from that table's own header row:
**20 rows carry a verdict outside their status column, and all nine of the nine are among them.**

There is no *"whole column"* for *strike, never delete* to have been applied to.
The science table is headed `| ID | Task | Acceptance (abridged …) | Leaf | Status |` and **those nine rows carry four cells after the identifier, not four plus a leaf**:
they have **dropped the `Leaf` cell**.
So the status record renders under **Leaf**, and the preserved priority note — which really is *strike, never delete* — lands in the **Status** cell behind it.

`tools/check-markdown-tables.py` is clean on every one of them, because their cell **count** matches the header.
Only the **meaning** of the columns is wrong, and no tool in this repository read a column at all.

## Why the ground matters even though the rule is right

Because the ground is what says when the rule **fails**.

*"The live verdict is written left and the superseded note right"* is a claim that the leftmost cell is where a live verdict goes.
If that were the idiom, taking the leftmost verdict would be right by construction.
It is not the idiom: the leftmost cell is simply the cell the row **has**, and what goes in it is whatever was written first.

`T-276` is the **tenth** row of exactly this shape with the two contents exchanged —
its iteration-41 record in the Leaf cell and its live `**TODO — HIGH**` in the Status cell —
and there `queue_status` returned the **superseded** verdict and the register dropped a live **HIGH** row.
That is the failure the stated ground says cannot happen, on a row of the very family the ground was read off.

## What is asked

1. **`C-0178` §2's second paragraph is corrected**, in place and by annotation rather than by overwrite: the *leftmost wins* rule stands on its **measurement** (it reproduces the old reader on 262 of 262 rows that carry a verdict, and moves exactly the four it was wrong about) and **not** on an idiom. The nine rows are mis-columned, not idiomatic. **Done** in iteration 44: the clause is struck and the correction stands beside it.
2. **The same sentence in `tools/queue_verdicts.py`'s header comment is corrected**, because a comment stating a false premise about the corpus is the thing the next reader will believe. **Done** in iteration 44.
3. **Nothing computed is withdrawn.** `C-0178`'s coverage repair, its four recovered rows, its mutation table and its false-positive measurement are untouched, and this challenge asks for no re-run. The three mutation harnesses that read `tools/queue_verdicts.py` were re-run against the edited comment: **24, 16 and 12 mutations, 0 survivors**.

## What would settle it the other way

A statement, anywhere in the corpus, that a task row may deliberately carry its live verdict in the leaf cell —
i.e. that the nine rows are written that way on purpose.
There is none: three rows of the same family (`P-12`, `P-20`, `T-276`) have already been repaired **into** the status cell,
by three different hands in three different iterations, with no rule written down anywhere.
