# T-283 — can the queue's residue line become a gate, or is a quoted status word permanently in the way?

| | |
|---|---|
| **Raised by** | [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md) §5 (`P-30`) |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation test, and a false-positive measurement over every revision of `TASKS.md` |
| **Units** | none; this is a document-integrity task |

## Formulate

`P-30` left `tools/check-queue-vocabulary.py` printing an **advisory residue**:
a row whose **prose** carries a closing word that is not its verdict.
It fell from **4 rows to 1** under the document repair, and the survivor is `T-261`,
whose acceptance criterion quotes three of `gpd/challenges/README.md`'s own status words **as data** —
lower-casing them would falsify the quotation.

`CLAUDE.md`'s rule for that shape is *wire the gate on what can be made clean and print the residue beside it, ungated*,
and `C-0083`'s is that **a gate that cannot come clean is not a gate**.
Both are satisfied by the advisory line; neither says the residue must stay advisory **forever**.
What decides it is whether a predicate exists that the corpus's own idiom can satisfy — and whether **that** predicate's false-positive rate is low enough to gate.

**The candidate is one line: blank inline code spans before the whole-row scan.**
Backticks are already this corpus's idiom for quoting a token as data,
and `T-261`'s three words are already backticked in the row as committed.
The question is not whether the candidate clears `T-261` — it plainly does — but what **else** it clears, and what it then still refuses.

**Numeric target.**
The code-blanking residue predicate reads **0** rows at the commit this lands on;
its firings over **every** revision of `TASKS.md` are enumerated and each distinct row is classified;
and the measured false-positive rate is **0**, or the line stays advisory and the measurement says why.

**Acceptance predicates, falsifiable.**

- **F1 — the candidate clears the standing counter-example.** With inline code spans blanked, `T-261` is not residue, and the residue at `HEAD` is **0 rows**.
- **F2 — the blanking touches the SCAN and never the VERDICT.** A row's leading verdict is computed from the **unblanked** body; only the whole-row scan is blanked. So a row whose verdict is itself inside a code span is unaffected, and blanking can never turn a closed row open.
- **F3 — the false-positive rate is measured over the queue's own history.** Every revision of `TASKS.md` (`git log --format=%H -- TASKS.md`) is scanned with today's predicate, in both readings — with and without the blanking — and every distinct row that fires is classified by hand as a **true** positive (the row really carries an upper-case closing word in prose, and a lower-casing or a backticking repairs it without falsifying anything) or a **false** positive (the row as written is correct and no repair exists). The gate is promoted **only** if the false-positive count is 0.
- **F4 — the blanking's own false negatives are named.** Every row the blanking **removes** from the residue is inspected: a removal is correct only where the closing word is genuinely quoted data. A removal that hides a live inconsistency falsifies the candidate.
- **F5 — the mutation test.** Each rule of the blanking fails at least one **named** test when mutated; mutations **replace** a rule wholesale rather than widening it (`C-0177`'s measured trap); the count of mutations that fail **nothing** is reported.
- **F6 — the gate's own reading is recorded.** `C-0158`: a claim that wires a gate records the gate's reading the way every other claim records a suite result, and the control for a document gate is `tools/verify.sh --committed`. Nothing new is wired into `tools/verify.sh` — `tools/check-queue-vocabulary.py` is already there, and what grows is the coverage of what is wired.
- **F7 — the measurement is inside its own scope.** This row and this task's own text live in `TASKS.md`, which is the corpus the residue measures. `CH-0182`: *a claim about a census is inside that census's own scope.* The residue is therefore reported **before and after** this iteration's own edits to the queue, and the row is written in the queue's own idiom so that it does not fire on itself — which the first draft of the `T-283` row demonstrably did, taking the advisory line from 1 row to 2.

**What would falsify the approach.**
A single revision of `TASKS.md` carrying a row whose prose **must** hold an upper-case closing word outside a code span —
a proper name, a verbatim heading, a quotation that backticking would misrepresent —
would make the predicate a permanent one-defect gate and the honest answer would be *the line stays advisory*.
That is the outcome the row was written expecting, and the measurement is what decides it.

## Plan

**Cheap bound first, and it costs one pass.**
`tools/P-30-history.py` already scripts the walk over every revision of `TASKS.md`, so the whole measurement is that walk with a second predicate beside the first.
Before writing any gate: count the firings under both readings, and list the **distinct rows**.
If the distinct-row list is long, the classification is expensive and the answer is probably *advisory*;
if it is short, the classification is a handful of readings and the answer is decidable.
Measured on the queue's 139 revisions the list is **8 rows** under the current predicate and **7** under the blanking, which is a morning's reading turned into ten minutes.

**Why the blanking and not a wider vocabulary change.**
Two narrowings were considered and rejected before code:
scoping the scan to the status **cell** would drop the whole point of the residue (the closing words are in the goal and subject cells);
and exempting a row by identifier is `C-0176`'s own recorded failure — *a set membership silences a symptom; it does not repair a predicate*.
Blanking a code span is neither: it is a statement about **Markdown**, it is what the reader's own struck-span blanking already does one rule earlier, and it gives every future row an escape that costs two characters and falsifies nothing.

**The classification is data, not taste.**
The per-row verdicts live in `tools/T-283-residue-history.py` as a table with a reason each, and a row that fires and is **not** in the table makes the tool exit 1 —
so the measurement cannot silently grow a new unexamined firing, which is `C-0176`'s `--check` discipline applied to a history walk.

**TDD.** The blanking's tests go into `tools/check-queue-vocabulary.py --selftest` first and are watched to fail, and the mutation table is written against them.
`tools/check-queue-vocabulary.py` is already wired into `tools/verify.sh` and its self-test into `build.gradle.kts`, so promoting the residue changes **no** wiring and needs no Gradle edit.
