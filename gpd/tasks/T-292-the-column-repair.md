# T-292 — twenty-one queue rows render their verdict under the wrong heading, and the repair has to preserve every word

| | |
|---|---|
| **Raised by** | [`C-0188`](../claims/C-0188-a-verdict-in-the-wrong-column.md) / [`T-289`](T-289-a-verdict-in-the-wrong-column.md), which measured the predicate, read **21**, and deliberately left the queue edit to a row of its own |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation test, and a token-multiset preservation proof over the 21 repaired rows |
| **Units** | none; this is a document-integrity task, and every value below is an integer count or a name |

## Formulate

`C-0188` established a **header-aware** predicate —
*the status column of a table is the index of the header cell reading `status`, and a verdict opening any other cell of a task row of that table is a defect* —
measured it at **0 false positives over 140 revisions of `TASKS.md`** (615 row-instances, 24 distinct rows, every one classified by hand),
and then declined to wire it, because it reads **21** on the queue it lands on and `C-0083`'s rule is that *a gate that cannot come clean is not a gate*.

The 21 are two shapes:

- **eleven rows of the five-column science table** (`| ID | Task | Acceptance | Leaf | Status |`), whose record renders under **Leaf**;
- **ten rows of the four-column process table** (`| ID | Task | Status | Notes |`), whose acceptance renders under **Status** and whose verdict renders under **Notes**.

`tools/check-markdown-tables.py` is clean on every one of them, because their cell **count** matches the header.
A cell-count check and a column-semantics check are different questions, and only the second one was ever asked.

**Numeric target.**
All 21 rows repaired so that every verdict opens its table's own status cell;
the repair proved **content-preserving** as a multiset of non-whitespace tokens, row by row, with every deliberate addition enumerated;
the arm promoted from advisory to a **gate** if and only if it then reads **0**;
and the promotion mutation-tested in **both** directions.

**Acceptance predicates, falsifiable.**

- **F1 — the repair is a COLUMN repair and not a rewrite.** For each of the 21 rows the multiset of non-whitespace tokens of the row's cell contents is **unchanged**, up to a per-row enumerated set of deliberate additions. The evidence is the diff itself — the added and removed token lists, per row, emitted into the result file — not an assurance that nothing was lost.
- **F2 — the leaf is DERIVED, not supplied.** For each of the eleven science rows the value of the `Leaf` cell is derived rather than chosen, and its derivation is stated. A row whose leaf genuinely cannot be determined gets `—` and says so.
- **F3 — the repair is a RULE, not a hand list.** The 21 rows are located by running `C-0188`'s own predicate over the file, and each row's repair is computed from its own text by a rule that a self-test holds open. No row's replacement text is typed by hand.
- **F4 — the register does not move.** `tools/trace-answers.py`'s `queue_status` returns the identical OPEN/CLOSED reading for **every** row of the queue before and after. A row whose reading moves must be named with the reason it should have.
- **F5 — the gate is promoted only on its own reading, and the reading is recorded.** The arm becomes a build failure only if `tools/check-queue-vocabulary.py` reads **0** miscolumned verdicts on the tree this lands on, and the claim records the gate's own reading rather than a suite count (`C-0158`).
- **F6 — the mutation test, in BOTH directions.** Every rule of the repair fails at least one **named** test when mutated wholesale; the promotion of the arm is mutated **back** to advisory and must also fail a named test; the number of mutations that fail **nothing** is reported; and the harness reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md` with a measured, subtracted baseline (`CH-0237`).
- **F7 — the harness is not orphaned.** The new harness is declared in `tools/P-31-harness-census.py`'s table and wired in `build.gradle.kts` the way the eleven existing ones are, and `tools/P-31-harness-census.py --check` reads 0 unresolved.
- **F8 — the census is dated.** The result file takes a `--ref` defaulting to `HEAD` and records the **resolved SHA**, because its subject is a mutable corpus (`CH-0182`, and `C-0174`'s rule for a corpus-subject result file).

**What would falsify the approach.**
A row of either shape whose text cannot be repaired without losing or inventing a word —
for instance a science row whose Leaf cell holds a record and **no** leaf token, so that a leaf would have to be supplied from outside the row —
would make this a rewrite rather than a column repair, and the honest outcome would then be to repair the rows that can be repaired and leave the rest as a named residue.

## Plan

**Cheap bound first, and here it settles the whole of the eleven-row half.**

`C-0188` §4 and [`CH-0241`](../challenges/CH-0241-the-preserved-priority-idiom-is-a-dropped-cell.md) both describe the science-table shape as a row that has **dropped the `Leaf` cell**.
If that were so, eleven leaf values would have to be found — from each row's task file, claim, or NDI leaf ID — before a single row could be repaired, and that is most of the task.

The cheap bound is one `split()`:
**read the last whitespace-delimited token of each of the eleven Leaf cells.**
It costs no history walk and no reading of any claim, and it is run before anything else.

**Then the ten-row half is a choice between two repairs, and it is decided by measurement rather than by taste.**
The four-column table is headed `Status` third; its ten defective rows are written in the *five*-column schema's semantics.
Either the ten rows move, or the header is retitled.
The discriminator is how many rows each choice puts outside the header, counted from the file:
if the rows that **follow** the header outnumber the rows that do not, the header is the majority reading and the rows are repaired;
if the reverse, the header is the defect.
The corpus also carries two precedents in this very table — `P-12` and `P-20` — and what they did is data.

**How the repair is applied.**
A retained tool, `tools/T-292-column-repair.py`:

1. locates the rows by calling `queue_verdicts.miscolumned_verdicts` — the same predicate the gate prints, so the repair cannot be about a different set of rows than the gate is;
2. classifies each firing by **shape**, from the column the verdict stands in relative to the status column, not from a list of identifiers;
3. rewrites the row by a per-shape rule;
4. asserts the token multiset before and after, per row, and refuses to write if any row loses a token;
5. `--check` re-reads the repaired file and requires the predicate to read 0.

**Why a tool and not an editor.**
Twenty-one rows of up to five kilobytes each, moved between cells: a hand edit is exactly `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op while the commit message describes it*, one level up.
The token assertion is the only thing that can prove the claim `F1` makes, and it has to run on every row, every time.

**What this costs.** No compute. No Kotlin. No result file moves; the only JSON emitted is this task's own census.
