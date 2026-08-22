# P-30 — the register reads a row's LEADING verdict, and it sees every row

| | |
|---|---|
| **Raised by** | the coordinator of iteration 43, from a measurement over `TASKS.md` |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests and a mutation test |
| **Units** | none; this is a document-integrity task |

## Formulate

`P-29` (iteration 42) left two predicates in the tree that are **about the same thing and are not the same predicate**.

- `tools/trace-answers.py`'s `queue_status()` decides a row OPEN or CLOSED by searching the **whole row after the identifier** for a closing word (`DONE|KILLED|CLOSED|ANSWERED|RESOLVED|DISCHARGED`, with a `PARTIALLY|PARTLY` lookbehind).
- `tools/check-queue-vocabulary.py` gates the queue's vocabulary on a **leading, short, bold** run only — a restriction it justifies by measurement, because bold runs anywhere give ~29 distinct hits and most are prose about some *other* task.

**The residue between the two is unguarded**, and today it holds four open rows the register reads CLOSED — each by a closing word that is not about the task:

| row | its real status | the closing word the reader sees |
|---|---|---|
| `T-261` | `TODO — **MEDIUM**` | its own title (*"a challenge the corpus has since ANSWERED"*) and a quoted criterion (`` `ANSWERED` ``, `` `UPHELD` ``, `` `RESOLVED` `` as **data**) |
| `T-268` | `**PARTIALLY DONE**` … `**TODO — HIGH VALUE, HIGHEST COST**` | *"`CH-0207` **CLOSED and REPAIRED**"* (a **challenge**) and *"`P1` was found ALREADY DISCHARGED"* (a **deliverable**) |
| `T-272` | `**PARTIALLY DONE**` … `**TODO — HIGH**` | *"`P2` is DISCHARGED over the whole corpus"* (a **deliverable**) |
| `T-280` | `**TODO — LOW**` | *"Candidate 1 … is **DONE**"* (a **candidate of the remedy**) |

`T-268` is a **HIGH VALUE** row, and the loop picks its next task from this register: an open task silently outside it is a process blocker, which `SESSION-PROMPT.md` ranks above any cheap win.

**And there is a second, distinct defect.** `_QUEUE_ROW` is `^\|\s*(T-\d{1,4}[a-z]?|P-\d{1,4})\s*\|(.*)\|\s*$` — it requires a **trailing pipe**. The `T-182` row has none, so that row is **invisible to the reader entirely**: 272 rows in the file, **271** seen. `tools/check-markdown-tables.py` is clean on it, because GFM does not require the trailing pipe — so the reader's format assumption is asserted **nowhere**. `T-182` happens to be DONE, so nothing is wrong today; the **coverage** is what is unguarded.

**Numeric target.** `queue_status()` reads all **272** rows and reads the four rows OPEN; a tested gate refuses the next instance of either defect; and the repair moves **nothing** in either deliverable's own status check.

**Acceptance predicates, falsifiable.**

- **F1 — coverage.** Every `TASKS.md` table row whose first cell is a task or process identifier is seen by `queue_status()`. Measured by a scanner written **independently** of the reader's own regular expression. It reads 271 of 272 before the repair and 272 of 272 after.
- **F2 — the four rows.** `queue_status()` reads `T-261`, `T-268`, `T-272` and `T-280` **OPEN**, and reads `T-182` **CLOSED**.
- **F3 — nothing else moves.** Over the 272 rows, the only readings that change are `T-182` (unseen → CLOSED) and those four (CLOSED → OPEN). Every other row reads exactly what it read before.
- **F4 — the repair is contained.** `stale_statuses` for `ANSWERS.md` and for `DECISIONS-FOR-NDI.md` is **0 before and 0 after** — so no published sentence depends on the four rows having read closed.
- **F5 — per-row agreement.** For every row carrying a leading verdict, `queue_status()`'s reading equals the **declared** sense of that row's first verdict, where the declaration is the hand-maintained vocabulary in `tools/check-queue-vocabulary.py` and the reading is the reader's own regular expression. This is the real-row form of `P-29`'s synthetic agreement check and it is **not** tautological: the two senses come from two artifacts.
- **F6 — the mutation test.** Every rule of the new predicate fails at least one **named** test when mutated; mutations **replace** a rule wholesale rather than widening it to `original|mutant` (`C-0177`'s measured trap — 9 of 22 rows of its first table failed nothing for exactly that reason); and the count of mutations that fail **nothing** is reported.
- **F7 — the false-positive rate is measured, not argued.** The gate is run over every past revision of `TASKS.md` (`git log --format=%H -- TASKS.md`, 137 of them) and every firing is classified by hand as a true or a false positive. It is wired into `tools/verify.sh` **only if it reads 0 at the commit it lands on**, and the gate's own reading is recorded in the claim (`C-0158`: a suite count is not a gate reading).

**What would falsify the approach.** If the queue's live verdict were **not** the leftmost one — if the file's practice were to append the new verdict to the right of the old — then "the first leading verdict wins" would close open rows, which is the unsafe direction, and the approach would have to be abandoned for a column-position rule that the queue's drifting column count cannot support.

## Plan

**Cheap bound first, and it decides the combination rule before any code is written.** A row can carry more than one leading verdict, so the rule has to say which one wins. One pass over `TASKS.md` measures it:

- **12** rows carry a leading `TODO` and are read CLOSED by the current reader. Only **3** of them are the defect (`T-261`, `T-268`, `T-280`; the fourth, `T-272`, is reached through its `**PARTIALLY DONE**` instead). The other **9** — `T-263`, `T-265`, `T-266`, `T-267`, `T-270`, `T-271`, `T-274`, `T-275`, `T-276` — carry a live `**DONE**` in an **earlier** cell and preserve the original `TODO — **PRIORITY**` note in a **later** one. So *"any leading `TODO` opens the row"* is **9 false positives**, and *"the last verdict wins"* is the same 9.
- Taking the **first** leading verdict reproduces the current reader on **262 of 262** rows that carry one, moving exactly the four, and the remaining 10 rows carry no verdict and fall back to the whole-row scan, agreeing with it at 10 of 10.

That measurement is the whole method, it costs one pass with no JVM, and it is what makes the rule *"the leftmost verdict is the live one"* a reading of the file rather than a preference.

**Is an unbolded leading `TODO` a verdict?** It must be, and this is `T-261`'s whole case: its status cell is `TODO — **MEDIUM**`, where the bold carries the **priority** and the verdict is bare. Refusing it leaves `T-261` on the fallback path and unrepaired. The direction it errs in is OPEN, which `CLAUDE.md` records four times as the safe one.

**Where the shared predicate lives.** `tools/check-queue-vocabulary.py` already imports `tools/trace-answers.py`, so the predicate cannot go in the gate — the reader would then depend on its own gate. It goes in a third module, `tools/queue_verdicts.py`, which both import: `queue_verdicts` ← `trace-answers` ← `check-queue-vocabulary`, one-way. It also gives the predicate a name and a file of its own, which is what `CLAUDE.md`'s *"the rule now lives once"* asks for and what six rounding implementations are the standing counter-example to.

**TDD.** The reader's tests are `tools/test-trace-answers.py`; the new cases go there first and are watched to fail. One existing test has to be **restated** rather than deleted — *"a qualified row that LATER carries a bare closing word still closes"*, a synthetic fixture reading `**PARTIALLY DONE**, and now **RESOLVED** in full`. Under a leading-verdict rule that row reads OPEN. The restatement is honest and the direction is safe: the queue's live practice is to **replace** the leading verdict when a task closes (`P-29`'s own row reads `**DONE** (iteration 42)`), and a row still leading with a qualifier reading OPEN keeps an item **in** the register.

**What is gated and what is only reported.** The residue itself — a row whose prose contains a closing word outside its leading verdict — **cannot be made clean**: `T-261`'s acceptance criterion quotes `` `ANSWERED` ``, `` `UPHELD` `` and `` `RESOLVED` `` as **data**, and lower-casing them would falsify the quotation. So the residue is **printed beside the gate, ungated**, which is `CLAUDE.md`'s own rule for exactly this shape (*wire the gate on what can be made clean and print the residue beside it, ungated*); what is gated is coverage and per-row agreement, both of which can.
