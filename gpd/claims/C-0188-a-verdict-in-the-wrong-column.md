# C-0188 — a verdict can be in the right ROW and the wrong COLUMN, and the register is then right only by luck: **21 verdicts on the queue this lands on render under a heading that is not their table's status column**, **0 false positives over 140 revisions**, and the shape `C-0178` reads as the queue's *preserved-priority idiom* is **9 rows that dropped a cell**

| | |
|---|---|
| **Task** | [`T-289`](../tasks/T-289-a-verdict-in-the-wrong-column.md), opened by the coordinator in iteration 43 after repairing the live instance by hand |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, a mutation test, and a false-positive measurement over every revision of `TASKS.md` |
| **Verdict** | **PASS on `F1`–`F8`, and the arm is DELIBERATELY NOT WIRED.** The predicate reads **21** verdicts over 21 rows at the commit this lands on, **every one of them genuine**, so it prints as an ungated residue beside the gate's four gated arms; `tools/check-queue-vocabulary.py` reads **0 defects** and exits 0 |
| **Maturity** | TRL 1–3 process artifact. Nothing here is a measurement of the physics; it is a measurement of the register the loop picks its work from |
| **Provenance** | `tools/queue_verdicts.py` (the predicate), `tools/check-queue-vocabulary.py` (the arm and its named tests, **91** self-tests run, 0 failures), `tools/T-289-column-history.py` (the history walk and its hand classification), `tools/T-289-mutation-test.py` (**16** mutations, **0** survivors, over a measured and subtracted baseline of **0**), `tools/T-289-emit-result.py`; result `gpd/results/T-289-a-verdict-in-the-wrong-column.json`, `baselineRef` `2c043809540a68c1e4e8be356b5b736ea75a6ce9` |
| **Conditions** | Documents and tools only — `TASKS.md`, five Python files in `tools/` and one Gradle task registration. No Kotlin source is touched, no result file other than this task's own is emitted, and **no physical number in the corpus moves** |
| **Consumes** | [`C-0178`](C-0178-leading-verdict-and-row-coverage.md) (`P-30`, the leftmost-verdict rule and the shared predicate module), [`C-0177`](C-0177-queue-status-vocabulary.md) (`P-29`, the vocabulary gate and its measured predicate), [`C-0183`](C-0183-residue-as-a-gate.md) (`T-283`, the residue's own history walk, which this one is written to), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`, the harness census this task's harness is registered in), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*, and the escaped-pipe rule), [`C-0158`](C-0158-prose-gate-red.md) (a claim landing a gate records the gate's own reading) |
| **Constrains** | every future reading of a `TASKS.md` row by `tools/trace-answers.py`, and therefore every *"is this task open"* statement in `ANSWERS.md` and `DECISIONS-FOR-NDI.md` |
| **Corrected by** | [`CH-0245`](../challenges/CH-0245-the-leaf-cell-was-never-dropped.md) / [`C-0192`](C-0192-the-column-repair.md), iteration 45 — the eleven science rows did **not** drop their `Leaf` cell; the record was written in **front** of a leaf that is still there, 11 of 11. Nothing computed here moves; §4 and §9 are struck and corrected in place |
| **Repaired by** | [`C-0192`](C-0192-the-column-repair.md) / [`T-292`](../tasks/T-292-the-column-repair.md), iteration 45 — all 21 rows repaired, the arm reads **0** and is now **GATED** |
| **Raises** | [`CH-0241`](../challenges/CH-0241-the-preserved-priority-idiom-is-a-dropped-cell.md), against [`C-0178`](C-0178-leading-verdict-and-row-coverage.md) — its *leftmost verdict wins* rule is upheld and the **ground** it is stated on is not |

---

## The claim, in one line

`P-29` gates which **words** a queue row may use and `P-30` decides which of a row's verdicts is **live**;
neither reads a **column**, and the queue renders in columns —
so a verdict written into the wrong cell agrees with both gates,
and the register is then correct only while the leftmost cell happens to hold the live verdict.

## 1. The live instance, and why nothing could see it

`T-276` is the twelfth `ANSWERS.md` synthesis and was a **HIGH** row.
Its iteration-41 record had been written into the fourth cell of a five-column table whose fourth column is the leaf,
so the row carried a leading `**DONE**` in column 4 and its live `**TODO — HIGH**` in column 5.
`queue_status` takes the row's **first** leading verdict, which is `C-0178`'s measured rule and is right,
and duly read the row `CLOSED`.

Read out of git rather than reconstructed as a fixture, at `9620d3e` — which is `P-30`'s **own** commit:

| | reading on the `T-276` row at `9620d3e` |
|---|---|
| `trace-answers.queue_status` | `CLOSED` — and the row was live |
| `check-queue-vocabulary` UNDECLARED | none |
| `check-queue-vocabulary` ROW (per-row agreement) | none |
| `check-queue-vocabulary` RESIDUE | none |
| **this predicate** | fires — `'DONE'` renders under `leaf`, not under `status` |

All three existing arms read zero **because both readings agree with a verdict that is really there**.
The agreement check `C-0177` built — every declared phrase must be read by the reader in the sense it is declared in —
is a statement about **words**, and this defect is not about a word.
Those five rows are a named test of the gate, so the instrument is checked against the instance that motivated it.

## 2. The predicate, and why it reads the header

**The status column of a table is the index of the header cell reading `status`,** with emphasis stripped and case folded;
a verdict opening any **other** cell of a task row of that table is a defect.

Two things had to be measured before that could be written.

**`TASKS.md` carries two schemas.**
`| ID | Task | Status | Notes |` for the process table and `| ID | Task | Acceptance (abridged …) | Leaf | Status |` for the science table,
so the status column is the **third** in one and the **fifth** in the other.
A rule that counts from either end is wrong about one of them, and the row that opened this task says so:
*the first verdict is not in the last cell* fires on **46** rows.

**A majority vote over a table's own rows was rejected.**
It is self-confirming on exactly the schema drift this looks for.
The header is one hand-written line, its width is checked by `tools/check-markdown-tables.py`,
and it is the only thing in the file that says what a column **means**.

**Cells are split on UNESCAPED pipes.**
`C-0083`'s rule is that the only literal pipe a GFM cell can carry is `\|`, and this corpus uses it —
a naive `split("|")` gives `T-60`'s four-column row **six** cells, so a column index computed that way is not a column index.
The split is `tools/check-markdown-tables.py`'s **own**, so *what is a cell* has one definition in this tree and it is the one the renderer's check counts widths with.

## 3. The cheap bound, run first, and it is wrong in BOTH directions

| | rows |
|---|---|
| the cheap census — *the first verdict is not in the last cell* | **47** |
| the header-aware rule | **21** |
| the two agree | 12 |
| the header rule **clears** — the four-column table's own correct rows, whose status cell is not their last | **35** |
| the header rule **finds** and the cheap census misses | **9** |

A contrast measured in one direction would have left the cheap census looking like a conservative superset.
It is not a superset at all: it and the predicate disagree about **44** of the **56** rows in their union.

**These two counts are a reading of a MOVING file and the result file records the ref they were taken at** (`CH-0182`).
The row this task's row opened on said **46**; a sibling's row landed during the iteration and the cheap census reads **47**.
What does not move is the **9** the header rule finds and the cheap census misses, the **12** they agree on, and the predicate's own **21**.

## 4. What the predicate reads, and it is two shapes

**21 verdicts, 21 rows**, at the commit this lands on — 11 under the heading `leaf` and 10 under `notes`.

| table | task rows | verdict only in the status column | no verdict | a verdict somewhere else |
|---|---|---|---|---|
| `\| ID \| Task \| Status \| Notes \|` | 31 | 19 | 2 | **10** |
| `\| ID \| Task \| Acceptance \| Leaf \| Status \|` | 255 | 236 | 8 | **11** |

**Shape one, ten rows.** The four-column process table is headed `Status` third, and from `P-11` onwards its rows are written in the *five*-column schema's semantics — an acceptance in column 3 and the verdict in column 4.
So the row renders an acceptance criterion under **Status** and its verdict under **Notes**.
**19 of 31** rows follow the header, so the header is the majority reading and the rows are the minority one.

The science table's row count includes the **two** rows this task adds and a concurrent claim's, all of which are written in the queue's own idiom and none of which fire —
`CH-0182`, on a claim whose subject is the file it stands in.

**Shape two, eleven rows.** ~~The five-column science table's rows omit their **Leaf** cell,~~
~~so the status record renders under **Leaf** and whatever follows it — usually the preserved priority note — renders under **Status**.~~
That is the `T-276` shape exactly, with the live verdict and the superseded one exchanged.

**CORRECTED, iteration 45 ([`CH-0245`](../challenges/CH-0245-the-leaf-cell-was-never-dropped.md), [`C-0192`](C-0192-the-column-repair.md)): the cell was never dropped.**
Every one of the eleven Leaf cells **ends in its own leaf token** — `A8.2` on seven, `new` on two, `—` on two —
and each agrees with the newest revision of `TASKS.md` in which that cell was a bare leaf, **11 of 11**.
The record was written **in front of** a leaf that is still standing behind it.
The finding is unchanged and its price is not: on the withdrawn reading eleven leaf values have to be supplied from outside the row,
and on the true one the repair supplies **none** and moves **not one token**.
This claim's own `TASKS.md` row says it correctly — *"written into the **Leaf** cell, **ending `A8.2`**"* — and this sentence did not.

**The register is right on all 21 today, and by luck.**
Nine of the eleven carry the live verdict in the leftmost cell, which is what `queue_status` takes.
On the tenth the leftmost cell held the superseded one.

## 5. The false-positive measurement, and the corpus's own practice is what settles it

Every revision of `TASKS.md` scanned with today's predicate:

| | |
|---|---|
| revisions | **140** |
| revisions that fire | **129** |
| row-instances | **615** |
| distinct rows | **24** |
| classified **TRUE** by hand, with a reason each | **24** |
| classified **FALSE** | **0** |
| false-positive rate | **0** |

The classification is data, not taste: it lives in `tools/T-289-column-history.py` with a reason per row,
and a row that fires and is in neither hand table makes the tool exit 1 —
`C-0176`'s `--check` discipline applied to a history walk, so the measurement cannot silently grow a new unexamined firing.

**Three of the 24 no longer fire, and all three were repaired in the direction this predicate prescribes.**

| row | how it was repaired |
|---|---|
| `P-12` | its acceptance cell was folded away, leaving the verdict in the status cell |
| `P-20` | its acceptance was merged into the **task** cell, which moved the verdict one column left |
| `T-276` | iteration 43, by hand and by a reader: the record moved into the status cell with the superseded verdict struck |

Three hands, three iterations, **no rule written down anywhere**.
That is the false-positive argument made empirically rather than by taste,
and it is stronger than the hand classification because nobody making those repairs was following this predicate.

## 6. Why the arm is NOT wired, and what would wire it

`C-0083`: **a gate that cannot come clean is not a gate**, and `CLAUDE.md`'s form of the same rule is
*print an ungated residue beside a gated arm rather than narrowing the predicate until the tree is clean*.

The predicate reads **21 genuine rows**, and the repair for every one of them is an edit to a queue row —
which is a queue edit and not a tooling one, and it is 21 rows this task does not own.
Narrowing the predicate until it reads zero would be the other thing that entry forbids.
So the arm prints, with a per-row line naming the heading the verdict renders under and the heading it belongs under, and a repair sentence;
and the count is printed **even when it is zero**, so the residue cannot go quiet.

**The gate's own reading at the commit this lands on** (`C-0158`: a suite count is not a gate reading):

| | |
|---|---|
| `tools/check-queue-vocabulary.py` defects | **0** — exits 0 |
| its miscolumned advisory | **21** |
| `tools/P-31-harness-census.py --check` | **0** unresolved over **11** harnesses, **wired 11 of 11** |

When the advisory count reaches 0 the arm becomes a gate by deleting a word.
The repair is queued as a row of its own.

## 7. The mutation table, and three rows survived the first run

**16 mutations, 0 survivors**, over a measured and subtracted baseline of **0**.
Every mutation **replaces** its rule wholesale rather than widening it to `original|mutant`, which is `C-0177`'s measured trap.
The harness reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md`, which is `CH-0237`'s layout premise.

**Three survived the first run and every one was a fixture that was not discriminating** — which is the finding, not a gap in the list:

- *a wholly struck verdict in the wrong column* — a cell opening `~~` is refused by the leading-bold rule whether or not anything is blanked, so the fixture held the struck-span rule open **nowhere**. Replaced by a verdict **behind** a struck prefix, which is the shape *strike, never delete* actually produces.
- *widen the row filter to any first cell* — the fixture put its verdict in the **status** column, where widening the row filter changes nothing.
- *a header no longer has to be followed by a separator* — no fixture carried a pipe line that is **not** a header, so dropping the requirement changed no reading. Replaced by a stray pipe line in front of a real header, where the mutation reads the row against the wrong schema and **names the wrong column**, which is the failure a column rule must not have.

**A fourth harness broke, loudly, and that is `C-0185` working.**
The predicate imports the width gate's own cell reader, and `tools/test-check-queue-vocabulary.py` enumerates the files it copies into its fixture.
The new import edge invalidated that list, and instead of *12 mutations, 12 survivors* the harness printed
*the UNMUTATED copy crashed, so nothing below is a measurement* — the baseline assertion `P-31` had added one iteration earlier.
The fixture tuple is a **dependency declaration** and now says so.

## 8. Acceptance predicates

| | |
|---|---|
| **F1** — header-aware, not positional, with the cheap census reported as the contrast | **PASS**, §2 and §3 |
| **F2** — cells split on unescaped pipes | **PASS**, §2; the split is the width gate's own and a named test asserts the reuse |
| **F3** — catches the instance it was written for | **PASS**, §1; and **9 of 9** of `C-0178`'s preserved-priority rows fire at `C-0178`'s **own** baseline ref (§9) |
| **F4** — false-positive rate measured over the queue's history, hand-classified, `--check` on an unclassified firing | **PASS**, §5: 0 of 24 |
| **F5** — wiring conditional on the reading, and the reading recorded | **PASS**, §6: **not wired**, and both readings recorded |
| **F6** — mutation test, wholesale replacements, survivors reported | **PASS**, §7: 16 / 0, three on the first run |
| **F7** — the harness is not orphaned | **PASS**: declared in `tools/P-31-harness-census.py`, wired in `build.gradle.kts`, **0 unresolved of 172 anchors and 33 symbols over 11 harnesses** |
| **F8** — the measurement is inside its own scope | **PASS**: the reading is recorded at the baseline ref and on the working tree, and this task's own rows are written so that they do not fire |

## 9. What this says about `C-0178`, and it raises a challenge

`C-0178` §2 justifies *the leftmost verdict is the live one* by measurement:
twelve rows carry a leading `TODO` and nine of them are legitimately closed, because
*"the file writes the live verdict into an EARLIER cell and preserves the original `TODO — **PRIORITY**` note in a LATER one — `C-0071`'s strike, never delete applied to a whole column."*

At `C-0178`'s **own** baseline ref, **9 of those 9 rows are miscolumned**, and 20 rows fire in all.
There is no *"whole column"* to apply anything to:
~~the nine rows have **dropped a cell**, so~~ the record renders under the wrong heading and the note lands in the status cell behind it.
(**Corrected, iteration 45, [`CH-0245`](../challenges/CH-0245-the-leaf-cell-was-never-dropped.md)**: the record was written **in front of** the leaf, which is still in the cell — no cell was dropped.)

**The rule is upheld and its ground is withdrawn.**
Taking the leftmost verdict is still right — it is right on 9 of the 9 — and it is right for a reason nobody stated:
those rows happen to carry the live verdict first.
`T-276` is the tenth row of the same shape with the order reversed, and there the rule returns the **superseded** verdict.

[`CH-0241`](../challenges/CH-0241-the-preserved-priority-idiom-is-a-dropped-cell.md), **raised and annotated in the same iteration**:
the clause is struck in `C-0178` §2 with the correction beside it, and the same sentence in `tools/queue_verdicts.py`'s header comment is replaced.
Nothing `C-0178` computed moves, and the three mutation harnesses that read that module were re-run against the edited comment — **24, 16 and 12 mutations, 0 survivors**.

## 10. Validity range

This is a statement about **`TASKS.md`'s two table schemas** and about a predicate over them.
It says nothing about any other Markdown table in the corpus:
claims, challenges and task files carry `| | |` metadata tables with no header text at all,
and the predicate correctly does not check a table with no status column.
It is a **logical** artifact at TRL 1–3 and it is not a measurement of anything physical.
