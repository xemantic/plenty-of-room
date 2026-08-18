# T-207 — repair the 14 committed `String.format` defects, and re-emit what they moved

**Leaf:** none (process repair; it protects every leaf's emitted prose).
**Status:** IN PROGRESS (iteration 28, Agent L).
**Reserved IDs:** claim `C-0127`, challenges `CH-0149`, `CH-0150`.
**Result:** `gpd/results/T-207-format-string-repair.json`.

## Formulate

`CLAUDE.md` prescribes the `%`-conversion check five separate times —
*"count the `%` conversions over the whole parenthesised concatenation against the top-level commas
of the `format(...)` argument list, stripping `%%` first"* —
and nothing was running it until `C-0125` (`T-195`) wrote `tools/check-kotlin-format-strings.py`.
Its sweep of `src/` reports **14 defects in 7 files from 7 studies, all committed**,
with raw `%` conversions reaching **7 committed result files**.

Java silently ignores *extra* arguments, so this class never throws.
It emits a grammatical sentence with the wrong numbers in it —
`CLAUDE.md` records exactly that failure —
or, where the mis-bound receiver carries no conversion at all,
it emits the raw `%.4f` / `%d` into the JSON.

### Numeric target and acceptance predicates

- **P1** — every defect the checker reports is either **fixed** or **shown to be a checker false positive**,
  and each fix is checked against *what the sentence was meant to say*, not merely made to balance.
- **P2** — the affected result files are re-emitted **in dependency order** (`tools/reemission-order.py`),
  and the moved fields are reported with their departures at **two significant digits**.
- **P3** — every moved string is grepped out of `gpd/claims/` and `gpd/challenges/`,
  and any claim quoting one is **amended by striking, never overwriting**.
- **P4** — the checker is wired into `tools/verify.sh` and `./gradlew test`
  **only once the tree is clean** (`C-0083`: a gate that cannot come clean is not a gate).
- **P5** — any pattern the checker misses that is found by hand becomes a **self-test first**,
  then a checker change.
- **P6** — no emitted result file contains a raw `%` conversion afterwards
  (`grep -E '%\.?[0-9]*[dfsg]'` over `gpd/results/*.json` returns nothing outside a legitimate literal).

### Units, geometry, conventions

Locked units unchanged (nm, pN, pN/nm, k_BT = 4.141947 pN·nm at 300 K).
This task changes **no physics**; every number it moves is a number that was
already computed correctly and printed wrongly, or a number that was never printed at all.

### Falsifier

If repairing a defect moves a **verdict** — a `PASS`/`FAIL`, a decision string, or a headline
figure a claim rests on — then the defect was not cosmetic and the claim must be amended.
The falsifier for *"this is a printing repair"* is any moved `verdict`, `decision` or
`falsifierFired` field in a re-emitted result file.

## Plan

### Cheap bound, before any repair (one pass, no run)

A mis-bound `.format` puts a **wrong number** in front of a reader only if the mis-bound
receiver literal itself carries at least one conversion — that conversion then eats the
*first* argument, which belonged to an earlier literal.
Where the receiver carries **zero** conversions, every argument is silently dropped and the
whole concatenation emits its `%` conversions raw: visibly broken, but never *misleading*.

That is one column of the checker's own output and it partitions the 14 before anything is run.

### Method

1. Classify the 14 by receiver-conversion count (the cheap bound above).
2. Read each site and write the sentence the author meant; parenthesise the whole concatenation.
3. Extend the checker's self-tests for the one pattern found by hand (a nested
   `"...".format(...)` inside a `${...}` template that contains braces), then fix the checker.
4. Re-emit the affected studies in the order `tools/reemission-order.py` prints,
   through `tools/study.sh`, and diff each result file field by field.
5. Grep every moved string fragment out of `gpd/claims/` and `gpd/challenges/`.
6. Wire the checker into `tools/verify.sh` and the Gradle test suite last.

### What would falsify this approach

- A defect whose *correct* sentence cannot be recovered from the surrounding code —
  i.e. the author's intent is genuinely ambiguous. Then the repair is a guess and must be flagged.
- A re-emission that moves a number **other** than the repaired prose, which would mean the
  study is not reproducible and the repair is riding on an unrelated drift.
- A checker false positive rate high enough that wiring it in as a gate would be a nuisance
  rather than a guard.
