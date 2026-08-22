# T-289 — a status verdict can sit in the LEAF column, and the register then reads a live row `CLOSED`

| | |
|---|---|
| **Raised by** | the coordinator, iteration 43, reading the queue — not by any gate |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation test, and a false-positive measurement over every revision of `TASKS.md` |
| **Units** | none; this is a document-integrity task |

## Formulate

`P-30` ([`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md)) made the register read a row's **leftmost** verdict,
and `P-29` ([`C-0177`](../claims/C-0177-queue-status-vocabulary.md)) made the gate check that every verdict word the queue coins is declared in both senses.
Neither reads a **column**.

The live instance was `T-276`, the twelfth `ANSWERS.md` synthesis and a **HIGH** row.
Its iteration-41 record had been written into the **Leaf** cell — the cell ends `A8.2` — so the row carried a leading `**DONE**` in column 4 and its live `**TODO — HIGH**` in column 5,
and `queue_status`, which takes the row's first leading verdict, read it `CLOSED`.
`tools/check-queue-vocabulary.py` reads **0 defects** on that row, because both readings agree with *a* verdict that is really there:
**the agreement check cannot see a verdict in the wrong column.**

The failure direction is the unsafe one, and it is the same one `CLAUDE.md` has now recorded four times:
an OPEN row silently leaves the register, and `SESSION-PROMPT.md` says the loop takes its next task from that register.

**Numeric target.**
A header-aware predicate — *the status column of each table is fixed by that table's own header row, and a verdict appearing in any other column is a defect* —
with its reading at `HEAD` enumerated row by row,
its false-positive rate measured over **every** revision of `TASKS.md` and classified by hand,
and the count of **genuine** historical instances it finds.

**Acceptance predicates, falsifiable.**

- **F1 — the predicate is header-aware and not positional.** The status column is located by the **header** of the table each row stands in. `TASKS.md` carries at least two schemas — `| ID | Task | Status | Notes |` and `| ID | Task | Acceptance | Leaf | Status |` — so a rule that counts from either end is wrong about one of them. The cheap census *first verdict is not in the last cell* is measured beside the predicate and reported as the **contrast**, not as the answer.
- **F2 — cells are split on UNESCAPED pipes.** `C-0083`'s rule is that the only literal pipe a GFM cell can carry is `\|`, and this corpus uses it: a naive `split("|")` gives `T-60`'s four-column row **six** cells. A column index computed by a naive split is not a column index.
- **F3 — the predicate catches the instance it was written for.** `T-276` fires at the commit that carried the defect, and the nine rows [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md) §2 cites as *"a live `**DONE**` in an earlier cell and the original `TODO` note in a later one"* are checked against it at `C-0178`'s **own** baseline ref.
- **F4 — the false-positive rate is measured over the queue's own history.** Every revision of `TASKS.md` (`git log --format=%H -- TASKS.md`) is scanned with today's predicate; every distinct row that fires is classified **by hand** as a true positive (the row really renders a verdict under the wrong heading, and a repair exists that falsifies nothing) or a false positive (the row as written is correct). A row that fires and is in neither table makes the measurement exit 1, which is `C-0176`'s `--check` discipline applied to a history walk.
- **F5 — wiring is conditional on the reading, and the reading is recorded.** The arm is promoted to a build failure **only** if it reads 0 at the commit this lands on. Otherwise it prints as an **ungated residue** beside the gated arms, with its count and its per-row list — `C-0083`'s *a gate that cannot come clean is not a gate*, and `CLAUDE.md`'s *print an ungated residue beside a gated arm rather than narrowing the predicate until the tree is clean*. Either way `C-0158` binds: the claim records the arm's actual reading, not a suite count.
- **F6 — the mutation test.** Each rule fails at least one **named** test when mutated; every mutation **replaces** a rule wholesale rather than widening it to `original|mutant` (`C-0177`'s measured trap); the count of mutations that fail **nothing** is reported; and the harness reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md` with a measured, subtracted baseline (`CH-0237`).
- **F7 — the harness is not orphaned.** Any new harness is declared in `tools/P-31-harness-census.py`'s table and wired in `build.gradle.kts` the way the ten existing ones are, and `tools/P-31-harness-census.py --check` reads 0 unresolved.
- **F8 — the measurement is inside its own scope.** This row lives in the file the predicate measures (`CH-0182`), so the reading is reported both at the baseline ref and on the working tree, and this row is written so that it does not fire on itself.

**What would falsify the approach.**
A revision of `TASKS.md` in which a verdict outside the status column is **correct** —
a table with two status columns, a row deliberately recording a superseded verdict in a cell of its own —
would make the predicate a permanent residue rather than a gate.
That is a possible outcome and the measurement is what decides it.

## Plan

**Cheap bound first, and it is explicitly not the predicate.**
The row itself states the cheap census — *first verdict is not in the last cell* — and its reading, **46 rows**.
Reproducing that number is the first step, because it costs one pass and it bounds nothing:
the `P-*` table has four columns and the science table five, so *last cell* is the status cell in one of them and the notes cell in the other.
What the reproduction buys is the **contrast** — how many of the 46 the header rule clears, and how many it finds that the 46 misses —
and a contrast in both directions is what distinguishes a predicate from a heuristic that happened to fire on the instance.

**Why a header and not a majority vote.**
A majority rule over a table's own rows would be self-confirming on the very schema drift this looks for.
The header is written by hand, it is one line, it is checked for width by `tools/check-markdown-tables.py`, and it is what GFM renders the columns under.
It is also the only thing in the file that says what a column **means**.

**The classification is data, not taste.**
Per-row verdicts live in `tools/T-289-column-history.py` with a reason each, exactly as `T-283`'s do,
and the walk exits 1 on a firing that is in neither table.

**TDD.** The predicate's tests go into `tools/check-queue-vocabulary.py --selftest` first and are watched to fail;
the mutation table is written against them.
`tools/check-queue-vocabulary.py` is already wired into `tools/verify.sh` and its self-test into `build.gradle.kts`,
so the arm itself needs no new wiring — only the mutation harness does.
