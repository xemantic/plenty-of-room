# C-0192 — a COLUMN repair is provable where a rewrite is not: **21 queue verdicts moved into their own table's status cell, 11 rows adding and removing NOT ONE token and 10 adding exactly one em dash**, the register moving **0 of 286 rows**, and the arm `C-0188` could not gate now reading **0** — and the eleven rows never dropped a cell at all, which one `split()` said before any code ran

| | |
|---|---|
| **Task** | [`T-292`](../tasks/T-292-the-column-repair.md), opened by [`C-0188`](C-0188-a-verdict-in-the-wrong-column.md) (`T-289`), which measured the predicate, read 21, and left the queue edit to a row of its own |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, a mutation test in both directions, and a token-multiset preservation proof emitted row by row |
| **Verdict** | **PASS on `F1`–`F8`, and the arm is NOW WIRED.** `tools/check-queue-vocabulary.py` reads **0 defects**, **0** miscolumned verdicts and exits 0; the miscolumned arm is **GATED**, which is the promotion condition `C-0188` §6 wrote down |
| **Maturity** | TRL 1–3 process artifact. Nothing here is a measurement of the physics; it is a repair of the register the loop picks its work from, and a proof that the repair took nothing out of it |
| **Provenance** | `tools/T-292-column-repair.py` (the repair, its rules and **28** named self-tests), `tools/T-292-mutation-test.py` (**21** mutations, **0** survivors, over a measured and subtracted baseline of **0**), `tools/T-292-emit-result.py`; `tools/check-queue-vocabulary.py` (the promoted arm, **94** self-tests run, 0 failures) and `tools/T-289-mutation-test.py` (**16** mutations, 0 survivors, with its gate row **inverted**); result `gpd/results/T-292-the-column-repair.json`, `baselineRef` `7f7957d8adf0821922775b6565297831fbd43cc4` |
| **Conditions** | Documents and tools only — `TASKS.md`, four Python files in `tools/` and two Gradle task registrations. No Kotlin source is touched, no result file other than this task's own is emitted, and **no physical number in the corpus moves** |
| **Consumes** | [`C-0188`](C-0188-a-verdict-in-the-wrong-column.md) (`T-289`, the predicate this repairs the reading of), [`C-0178`](C-0178-leading-verdict-and-row-coverage.md) (`P-30`, the leftmost-verdict rule the register reads by), [`C-0183`](C-0183-residue-as-a-gate.md) (`T-283`, the residue arm whose own mutation this repair made unobservable), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`, the harness census this task's harness is registered in), [`C-0071`](C-0071-output-element-recommendation.md) (*strike, never delete*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*), [`C-0158`](C-0158-prose-gate-red.md) (a claim landing a gate records the gate's own reading) |
| **Constrains** | every future `TASKS.md` row: a verdict outside its table's status column now fails the build |
| **Raises** | [`CH-0245`](../challenges/CH-0245-the-leaf-cell-was-never-dropped.md), against [`C-0188`](C-0188-a-verdict-in-the-wrong-column.md) §4/§9 and [`CH-0241`](../challenges/CH-0241-the-preserved-priority-idiom-is-a-dropped-cell.md) — the eleven rows did not **drop** a cell, and the difference is the whole price of the repair |

---

## The claim, in one line

`C-0188` proved that a verdict can be in the right row and the wrong column and left twenty-one of them standing,
because the repair is a queue edit and a claim that cannot come clean cannot gate;
this repairs all twenty-one **without moving a word** — 11 rows at zero tokens added and zero removed, 10 at one em dash each —
so the arm becomes a gate, and the register reads the file exactly as it read it before.

## 1. The cheap bound ran first, and it settled the larger half before any code was written

`C-0188` §4 and `CH-0241` both describe the eleven science-table rows as rows that have **dropped their `Leaf` cell**.
Read that way, eleven leaf values have to be **supplied from outside the row** — from each row's task file, its claim, or the NDI `simulation-task-map` — before one row can be repaired,
and the repair then adds content, which is a rewrite.

The cheap bound is one `rsplit(None, 1)` per row: **read the last whitespace-delimited token of each of the eleven Leaf cells.**

| row | leaf token at the end of its Leaf cell | the newest revision in which that cell was a bare leaf | agrees |
|---|---|---|---|
| `T-9` | `new` | `9e35853` | yes |
| `T-263` | `A8.2` | `23f68ab` | yes |
| `T-265` | `—` | `344c78b` | yes |
| `T-266` | `—` | `344c78b` | yes |
| `T-267` | `—` | `ee5cf1a` | yes |
| `T-268` | `—` | `344c78b` | yes |
| `T-270` | `A8.2` | `6e945fd` | yes |
| `T-271` | `A8.2` | `dab6b82` | yes |
| `T-272` | `A8.2` | `5a46a08` | yes |
| `T-274` | `A8.2` | `14e40c8` | yes |
| `T-275` | `new` | `7368986` | yes |

**11 of 11. Zero leaf values had to be supplied from outside the row.**
The cell was never dropped: an iteration's record was written **in front of** a leaf that is still standing behind it,
which is why `T-276`'s own repair, made by hand in iteration 43, left `A8.2` in that cell.

That is [`CH-0245`](../challenges/CH-0245-the-leaf-cell-was-never-dropped.md).
The finding `C-0188` made is untouched and the mechanism it named is withdrawn,
and the mechanism is what prices the work: eleven judgements against none.

## 2. The four-column half is a choice between two repairs, and the file decides it

The process table is headed `| ID | Task | Status | Notes |`, and from `P-11` on ten of its rows are written in the *five*-column table's semantics —
an acceptance in column 3, the verdict in column 4.
Either the ten rows move, or the header is retitled to `Acceptance | Status`.

| table | task rows | follow the header | carry no verdict | carry a verdict elsewhere |
|---|---|---|---|---|
| `\| ID \| Task \| Status \| Notes \|` | 31 | **19** | 2 | **10** |
| `\| ID \| Task \| Acceptance … \| Leaf \| Status \|` | 255 | 236 | 8 | 11 |

**Retitling the header would put 19 rows outside it against the 10 that are outside it now**, so the rows are repaired and the header stands.
The corpus also carries two precedents **in this very table**, and both moved the row rather than the header:
`P-12` folded its acceptance cell away, and `P-20` merged its acceptance into the **task** cell, which moved the verdict one column left.
`P-20`'s is the repair applied here to all ten.

## 3. The repair is a rule, and the rows are the gate's own

`tools/T-292-column-repair.py` locates its rows by calling `queue_verdicts.miscolumned_verdicts` —
the predicate `tools/check-queue-vocabulary.py` prints — so the repair cannot be about a different set of rows than the gate is.
It then classifies each firing by the column the verdict stands in **relative to the status column**, never from a list of identifiers, and applies one rule per shape:

- **the verdict one column LEFT of the status column** (`| ID | Task | Acceptance | Leaf | Status |`): the leaf is split off the end of the cell and left there alone, and the record moves into the status cell **in front of** the note it supersedes, whose leading verdict run is struck;
- **the verdict one column RIGHT of it** (`| ID | Task | Status | Notes |`): the acceptance is folded into the task cell after an em dash, the verdict clause becomes the status cell and the finding becomes the notes cell, split at the first period followed by a space — which is where all 19 conforming rows of that table already put the boundary.

A row in any other shape is **reported unrepairable**, not half repaired; a Leaf cell that does not end in a leaf token is **refused**, not guessed at.
Both refusals are named tests, and both are mutation-tested in both directions.

## 4. The preservation proof, which is the evidence and not an assurance

For every row the multiset of non-whitespace tokens of the row's cell contents is compared before and after.
The tool refuses to write if any row loses a token.

| | rows | tokens added | tokens removed |
|---|---|---|---|
| the eleven five-column rows | 11 | **0** | **0** |
| the ten four-column rows | 10 | **one em dash each**, 10 in total | **0** |
| any row that lost a token | **0** | — | — |

Every cell **count** is unchanged, so `tools/check-markdown-tables.py` reads what it read before: **0 table defects**, over a file count that moves only as the corpus grows.
Strike markers are normalised away on **both** sides of the comparison, because they are the repair's own deliberate addition, and they are enumerated separately:
**eleven** superseded verdict runs are struck, one per five-column row, each to the end of its own leading bold run —
`TODO — **MEDIUM-HIGH**` on `T-263`, `**PARTIALLY DONE** (iteration 35)` on `T-9`, and so on.
That is the queue's own majority idiom for a superseded note, and `T-276`'s hand repair struck exactly `~~**DONE** (iteration 41)~~` and left its record's prose live.

**No case was folded.** `P-20`'s repair lower-cased the first word of the acceptance it merged; this one does not,
because an exact token multiset is a stronger statement than one carrying eight enumerated exceptions, and an em dash before a capitalised clause reads correctly.

## 5. The register does not move, and that is the point

Measured **on the repair alone** — the baseline ref's own file against the baseline ref's own file with the twenty-one rows moved, so that nothing else differs:

| | before | after |
|---|---|---|
| rows `trace-answers.queue_status` reads | 286 | 286 |
| rows read `OPEN` | 68 | 68 |
| rows whose reading moved | — | **0** |
| leading (live) verdicts | 287 | **276** |

The eleven that disappear are the eleven superseded notes now struck, and none of them was ever the row's *leftmost* verdict.
`CH-0241`'s point read forwards: the register was right on all 21 **by luck**, and the repair is what removes the luck.

The **working tree** at the end of the iteration reads two movements and neither is the repair's:
`T-292` is this claim's own row and is deliberately closed by it,
and `T-276` is a concurrent agent's row, which this task neither owns nor touches.
Two rows are present in the tree and not at the ref (`T-294`, `T-295`) and a row that did not exist at the ref has not had its reading moved by anything.
The result file carries all three readings separately, because folding them together is how a repair takes credit or blame for somebody else's edit.

## 6. The gate's own reading, on the tree this lands on

`C-0158`: a claim landing a gate records the gate's reading, and a suite count is not a gate reading.

| | |
|---|---|
| `tools/check-queue-vocabulary.py` defects | **0** — exits 0 |
| its miscolumned arm | **0**, and **GATED** (it was 21 and advisory) |
| `tools/check-queue-vocabulary.py --selftest` | **94** self-tests, 0 failures |
| `tools/T-292-column-repair.py --self-test` | **28** named tests, 0 failures |
| `tools/P-31-harness-census.py --check` | **0** unresolved over **193** anchors and **33** symbols across **12** harnesses, **wired 12 of 12** |
| `tools/check-markdown-tables.py` | **0** defects |
| `tools/T-234-census.py --check` | 0 defects before and after; every `TASKS.md` occurrence byte-identical; the debt line **24 of 88** unchanged |

Both directions of the promotion are named tests: *a miscolumned verdict FAILS the gate*, and *a queue whose verdicts all stand in their own status column PASSES it*.
`T-289`'s mutation row *"the arm becomes a GATE"* is **inverted** to *"the arm reverts to ADVISORY"* rather than deleted —
a mutation row that outlives the rule it mutates measures nothing.

## 7. The mutation table, and two survived the first run

**21 mutations, 0 survivors**, over a measured and subtracted baseline of **0**.
Every mutation **replaces** its rule wholesale rather than widening it to `original|mutant`, which is `C-0177`'s measured trap.
The harness reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md`, which is `CH-0237`'s layout premise.

Six rules are mutated in **both** directions, because each carries an exclusion:
the leaf grammar narrow (the queue's own `new` and `—` refused) and wide (any trailing word a leaf);
the strike to nothing and to everything;
the sentence boundary to any period and to none.

**Two survived the first run and both are findings rather than gaps in the list:**

- *struck spans are no longer blanked when the rows are located.* The fixture was not discriminating: a **wholly** struck verdict is refused by the leading-bold rule whether or not anything is blanked, so it holds the blanking open nowhere. Replaced by a verdict **behind** a struck prefix — the shape *strike, never delete* actually produces — which `C-0188` §7 had measured one level up and which this harness found again one level down.
- *the tool stops reading the queue's own file.* Five named tests that read `TASKS.md` sat behind an `if os.path.exists(QUEUE)` guard, so moving the path turned five tests into **no** tests rather than into five failures. The guard is now a named test of its own: **the queue this repair is proved against is where the tool says it is.**

## 8. A third harness broke, and it is a consequence of the repair rather than a defect in it

`tools/T-283-mutation-test.py` went from 0 survivors to **1** the moment the queue was repaired, on a mutation that flips the residue arm from a row's **leftmost** verdict to its **last**.

The cause is exact and it is worth recording.
Before the repair, twenty-one rows carried a **second, unstruck** verdict in another cell, and the committed queue was itself the fixture that made `verdicts[0]` and `verdicts[-1]` different objects.
After it, every row of a gate-clean queue carries **exactly one** live verdict — 276 verdicts over 286 rows — so the two are the same object on the real file and the mutation is **unobservable there**.
The rule is unchanged and still binds on any non-conforming input, of which the file's own history is full;
what vanished was the discriminating input.

The repair is `C-0161`'s: **construct the state.**
Two named tests now hold the rule open on a fabricated two-verdict row — the very shape the column gate refuses, which is precisely why it can no longer be found in the file —
and `tools/T-283-mutation-test.py` reads **12 mutations, 0 survivors** again.
It is also `CLAUDE.md`'s *a self-test that reads a mutable artifact expires the moment the defect it asserts is repaired*, in a form that entry does not yet carry:
the artifact here is not the tool's **baseline**, it is the tool's **discriminator**, and repairing the corpus is what expired it.

## 9. Acceptance predicates

| | |
|---|---|
| **F1** — a column repair, not a rewrite, proved as a token multiset per row | **PASS**, §4: 11 rows at `+0/−0`, 10 at `+1` em dash, **0** rows lost a token, and the diff is emitted per row into the result file |
| **F2** — the leaf is DERIVED, not supplied | **PASS**, §1: 11 of 11 read off the row's own cell and corroborated against the newest revision in which that cell was a bare leaf; **0** supplied from outside; a cell without a leaf token is refused, and that refusal is a named test |
| **F3** — a rule, not a hand list | **PASS**, §3: rows located by `queue_verdicts.miscolumned_verdicts`, shape derived from the column index, no replacement text typed by hand |
| **F4** — the register does not move | **PASS**, §5: 286 rows before and after, **0** moved, 68 OPEN both times |
| **F5** — the gate promoted only on its own reading, and the reading recorded | **PASS**, §6: 21 → **0**, gated, and both the gate's reading and the harness census recorded |
| **F6** — mutation-tested in both directions, survivors reported | **PASS**, §7: **21 / 0** at a subtracted baseline of 0, six rules mutated both ways, two survived the first run and both are named |
| **F7** — the harness is not orphaned | **PASS**: declared in `tools/P-31-harness-census.py`, wired in `build.gradle.kts` as `testColumnRepair` and `testColumnRepairMutations`, **0 unresolved of 193 anchors and 33 symbols over 12 harnesses, wired 12 of 12** |
| **F8** — the census is dated | **PASS**: `tools/T-292-emit-result.py --ref` defaults to `HEAD` and records the resolved SHA `7f7957d8adf0821922775b6565297831fbd43cc4`; the *before* reading is taken at that ref and the *after* reading on the working tree |

## 10. Validity range

This is a statement about **`TASKS.md`'s two table schemas** and about a repair over them.
It says nothing about any other Markdown table in the corpus:
claims, challenges and task files carry `| | |` metadata tables with no header text at all,
and the predicate correctly does not check a table with no status column.

Two things travel with it.

**The strike rule is an idiom, not a theorem.** It strikes the leading verdict run of a superseded note, which is what the queue's own majority of struck spans does and what `T-276`'s hand repair did. A row whose superseded note is longer than its opening bold run keeps the rest of that note live, deliberately: striking prose no verdict supersedes is one of the mutations in §7.

**The four-column split rule is calibrated on ten rows.** *The first period followed by a space* is where every conforming row of that table already puts the boundary, and it is refused rather than guessed where a cell has no such boundary — but it is a convention of that table and not a general rule about Markdown.

It is a **logical** artifact at TRL 1–3 and it is not a measurement of anything physical.
