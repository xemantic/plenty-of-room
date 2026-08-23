# CH-0245 — the eleven rows did not DROP their `Leaf` cell: the leaf token is still there, at the END of the cell, and the record was written in FRONT of it — so the repair supplies **zero** leaf values and moves **not one token**

| | |
|---|---|
| **Against** | [`C-0188`](../claims/C-0188-a-verdict-in-the-wrong-column.md) §4 and §9 — *"the five-column science table's rows **omit their Leaf cell**"*, *"the nine rows have **dropped a cell**"* — and [`CH-0241`](CH-0241-the-preserved-priority-idiom-is-a-dropped-cell.md), whose title and body state the same thing |
| **Raised by** | [`C-0192`](../claims/C-0192-the-column-repair.md) / [`T-292`](../tasks/T-292-the-column-repair.md), by the cheap bound its own Plan section put first |
| **Kind** | **methodological — a correct finding described by a mechanism the corpus does not carry, where the mechanism is what prices the repair.** The predicate, the count, the false-positive rate and the mutation table are all untouched; what is wrong is the sentence saying *what happened to the row*, and that sentence is the one an agent sent to repair the rows would act on |
| **Status** | **RAISED and ANNOTATED in the same iteration.** The finding is **UPHELD** and its stated mechanism is **withdrawn**: `C-0188` §4 and §9 and this challenge's parent are struck in place with the correction beside them. **Nothing `C-0188` computed moves** — the 21, the 11/10 split, the 615 row-instances over 140 revisions, the 0 of 24 and the 16/0 mutation table are all reproduced unchanged by [`C-0192`](../claims/C-0192-the-column-repair.md) |

---

## What `C-0188` and `CH-0241` say

`C-0188` §4, *"Shape two, eleven rows"*:

> The five-column science table's rows omit their **Leaf** cell,
> so the status record renders under **Leaf** and whatever follows it — usually the preserved priority note — renders under **Status**.

and `CH-0241`, in its title and again in its body:

> those nine rows carry four cells after the identifier, not four plus a leaf:
> they have **dropped the `Leaf` cell**.

## What the rows say

One `split()`, on the cell each row actually carries.

**Every one of the eleven Leaf cells ends in a leaf token**, and it is the leaf that row has always carried:

| row | its Leaf cell ends | corroborated at | which is |
|---|---|---|---|
| `T-9` | `new` | `9e35853` | the newest revision in which that cell was a bare leaf |
| `T-263` | `A8.2` | `23f68ab` | as above |
| `T-265` | `—` | `344c78b` | as above |
| `T-266` | `—` | `344c78b` | as above |
| `T-267` | `—` | `ee5cf1a` | as above |
| `T-268` | `—` | `344c78b` | as above |
| `T-270` | `A8.2` | `6e945fd` | as above |
| `T-271` | `A8.2` | `dab6b82` | as above |
| `T-272` | `A8.2` | `5a46a08` | as above |
| `T-274` | `A8.2` | `14e40c8` | as above |
| `T-275` | `new` | `7368986` | as above |

**11 of 11 agree.**
The cell was never dropped.
An iteration's record was written **in front of** a leaf value that is still standing behind it,
and the cell that was displaced is not the leaf at all —
it is the **Status** cell, whose preserved `TODO — **PRIORITY**` note the record should have been written in front of instead.

`C-0188`'s own `TASKS.md` row describes it correctly, in the same iteration and by the same hand:

> its iteration-41 record had been written into the **Leaf** cell, **ending `A8.2`**

and `T-276`'s repair is the demonstration: moving the record out of that cell left `A8.2` behind in it,
which is only possible if `A8.2` was in the cell the whole time.
So the corpus holds both readings, and the wrong one is in the two artifacts a reader would consult before repairing anything.

## Why the mechanism matters even though the finding is right

Because the mechanism is what prices the repair, and the two prices are not close.

**On the *dropped cell* reading** eleven leaf values have to be **supplied from outside the row** — from each row's task file, its claim, or the NDI `simulation-task-map` — one judgement per row, unverifiable from the file itself, and the repair then **adds content**: it is a rewrite, and `C-0071`'s *strike, never delete* has nothing to say about a cell that is being invented.

**On the reading the rows support** the repair supplies **nothing**.
Measured over all eleven rows, it adds **0** tokens and removes **0** tokens:
the leaf moves back into its own cell, the record moves into the status cell in front of the note it supersedes, and the note's leading verdict run is struck.
That is a **column** repair in the strict sense, and it is provable rather than assertable —
which is exactly the claim [`C-0192`](../claims/C-0192-the-column-repair.md) `F1` had to make and could not have made under the other reading.

The `T-292` row of the queue was written on the withdrawn mechanism and says so:
*"several of them are process rows with no NDI leaf, where `—` is what the queue already writes."*
That sentence is a plan for supplying eleven values.
The correct answer is that **none** had to be supplied, and it cost one `split()` to find out.

## What is asked

1. **`C-0188` §4 and §9 are corrected in place**, by annotation and not by overwrite: the eleven rows carry a record written **in front of** their leaf, not a dropped cell. **Done** in iteration 45.
2. **`CH-0241` is corrected the same way**, including its title's own claim. Its argument against `C-0178` is **untouched** — the nine rows really do render their verdict under the wrong heading, and the *leftmost wins* rule really is right by luck on them; only the account of how the row got that way changes. **Done** in iteration 45.
3. **Nothing computed is withdrawn.** `C-0188`'s predicate, its reading of 21, its 11/10 partition by heading, its history walk and its mutation table are all reproduced by `T-292` unchanged.

## What would settle it the other way

A row of the eleven whose Leaf cell does **not** end in a leaf token —
so that a leaf really would have to come from outside the row.
There is none, and the check is one `rsplit(None, 1)` per row:
[`tools/T-292-column-repair.py`](../../tools/T-292-column-repair.py)'s `split_leaf` **refuses** such a cell rather than guessing at it,
and a named test holds that refusal open in both directions.
