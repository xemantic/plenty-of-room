# C-0127 — **THE CLASS IS REAL, THE CENSUS WAS WRONG, AND THE CLAIMS WERE ALL RIGHT.** Of the 14 `String.format` defects `C-0125`'s checker found, **exactly one** put a wrong number in front of a reader — `T-163`'s *"the SAXS-measured **40.35** nm"* where the answer is **2.69** — **twelve** printed a raw `%.4f` or `%d` into a committed result file, and **one was the checker's own false positive**. All thirteen real ones are repaired and all seven affected result files re-emitted in dependency order: **13 prose fields moved, 0 verdict or boolean fields moved, 0 numeric fields moved by the repair**, and **not one of the seven claims had inherited a defective number**. The census is `7` files, `7` studies and `7` result files, not `C-0125`'s *"6 files from 5 studies … 8 result files"* (`CH-0149`); and *"Java silently ignores extra arguments, so this class does not throw"* explains **1 of 14** — the other twelve were as loud as a defect can be and survived anyway, because **a result file is read once, while the claim is being written, and never again** (`CH-0150`). The checker is now wired into `./gradlew test` and `tools/verify.sh`

| | |
|---|---|
| **Task** | [`T-207`](../tasks/T-207-format-string-repair.md), raised by [`C-0125`](C-0125-scaffold-remainder.md) |
| **Leaf** | none. This is a **process repair**; it protects the emitted prose of every leaf |
| **Verification type** | **logical** (a static count of `%` conversions against top-level commas, per `CLAUDE.md`'s own prescription) **+ in-silico** (every affected study re-run through `tools/study.sh` and diffed field by field against its **committed** version, read out of `git`) |
| **Verdict** | **PASS on all six predicates.** The task's own falsifier — *"any moved `verdict`, `decision` or `falsifierFired` field"* — **did not fire**: **0 of 7** files moved one. Two of the four falsifiers in §10 **did** fire and are answered rather than argued away: the checker's false-positive rate (repaired to 0 in 13 **before** the wiring) and eight numeric movements (shown by a control re-run of identical code to be the studies' own descent irreproducibility) |
| **Maturity** | **TRL 1–3, and below it: no physics changed.** Every number this task moved was already computed correctly and printed wrongly, or was never printed at all |
| **Provenance** | `gpd/results/T-207-format-string-repair.json`, emitted by `tools/T-207-emit-result.py`; the repaired checker `tools/check-kotlin-format-strings.py` (**19** self-tests, was 17); seven repaired studies re-emitted through `tools/study.sh` in the order `tools/reemission-order.py` printed. `tools/check-markdown-tables.py` and `tools/check-corpus-links.py` clean |
| **Conditions** | The tree at `HEAD` of iteration 27. Units unchanged and untouched: nm, pN, pN/nm, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Consumes** | [`C-0125`](C-0125-scaffold-remainder.md) (the checker and its sweep), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (re-emission is a topological sort, not a list) |
| **Raises** | [`CH-0149`](../challenges/CH-0149-the-format-defect-census-is-off-by-a-file-a-study-and-a-result.md), [`CH-0150`](../challenges/CH-0150-twelve-of-the-fourteen-were-visibly-broken-and-the-silent-failure-diagnosis-does-not-explain-them.md) |

---

## The claim, in one line

**This defect class damages the machine-readable artifact and spares the human one** —
which is the opposite of the direction the standing diagnosis implies,
and it is exactly why repairing all thirteen moved thirteen sentences, zero verdicts and zero claims.

---

## 1. The cheap bound, before any repair, and it partitioned all fourteen

A mis-bound `.format` puts a **wrong number** in front of a reader **only if the mis-bound receiver literal itself carries at least one conversion** —
that conversion then eats the **first** argument, which belonged to an earlier literal.
Where the receiver carries **zero** conversions, every argument is silently dropped and the whole concatenation emits its `%` conversions **raw**.

The receiver conversion count is already a column of the checker's own output, so the partition cost **one pass and no run**:

| class | count | what a reader of the JSON sees |
|---|---|---|
| **`WRONG_NUMBER`** | **1** | a grammatical sentence, **15.0×** out |
| **`RAW_CONVERSIONS`** | **12** | `"C-0017's %.7f pN/nm as a SUM …"` |
| **`CHECKER_FALSE_POSITIVE`** | **1** | nothing — the string is correct |

That partition **predicted the whole blast radius before anything ran**: a dropped argument cannot change a computed quantity, so no numeric field could move.
The re-emission is what turned the prediction into a measurement, and it held.

## 2. The one that mattered

`coupling/PathCountFixedGeometryStudy.kt:1067`, `T-163`'s conditions block:

```kotlin
"tile" to "40.0 x %.2f nm single-layer square-lattice sheet, %d duplexes at the " +
        "SAXS-measured %.2f nm".format(lengthY, T163_DUPLEXES, sheet.interhelicalDistance)
```

`+` binds tighter than `.format()`, so only `"SAXS-measured %.2f nm"` was formatted,
and its single surviving conversion ate `lengthY` — the argument meant for the **first** literal.

| | emitted | correct |
|---|---|---|
| before | `40.0 x %.2f nm … %d duplexes at the SAXS-measured **40.35** nm` | |
| after | | `40.0 x 40.35 nm … 15 duplexes at the SAXS-measured **2.69** nm` |

**15.0× out, in a grammatical sentence, in the conditions block of a committed result file.**

## 3. Fourteen minus one: the checker's own

`anchoring/StandoffBaseJointStudy.kt:875` is reported as *"5 conversions, 4 arguments"* and its **emitted string is correct**.
The fifth conversion is a **nested** `"%.0f".format(it)` inside a `${…}` template whose body carries braces,
consumed by its own call long before the outer one runs;
the stripper was the regular expression `\$\{[^{}]*\}`, which cannot match past an inner brace.

Repaired in the **checker**, TDD: **two self-tests written first**, one failing (`expected 0, found 1`) and one already passing as the guard against over-stripping.
`_strip_templates()` is a balanced-brace walk and the regex is gone. **17 → 19** self-tests, and the sweep at `HEAD` falls **14 → 13**.

`CLAUDE.md`: *"a drift checker's FALSE positives cost more than its true ones, because the tool exists in order to be believed."*
A 1-in-14 false-positive rate on a gate that fails a build is the rate at which the gate gets switched off, so **the repair had to precede the wiring**.

## 4. What moved

`tools/reemission-order.py T-40 T-106 T-129 T-136 T-138 T-148 T-163` printed **four** dependency constraints inside the set
(`T-136` before `T-138`, `T-148` and `T-163`; `T-138` before `T-163`) and the order
**`T-106`, `T-129`, `T-136`, `T-40`, `T-138`, `T-148`, `T-163`**, which is the order the studies were re-run in.

| result file | prose fields moved | numeric fields moved | verdict/boolean moved | fields carrying a raw `%` conversion |
|---|---|---|---|---|
| `T-106-truss-cap.json` | 3 | **0** | 0 | 3 → **0** |
| `T-129-range-robust-placement.json` | 1 | 7 (**not the repair** — §5) | 0 | 1 → **0** |
| `T-136-two-per-row-placement.json` | 1 | 1 (**not the repair** — §5) | 0 | 1 → **0** |
| `T-40-standoff-base-joint.json` | 1 | **0** | 0 | 1 → **0** |
| `T-138-path-count-consistency.json` | 2 | **0** | 0 | 2 → **0** |
| `T-148-staple-dropout.json` | 3 | **0** | 0 | 3 → **0** |
| `T-163-path-count-fixed-geometry.json` | 2 | **0** | 0 | 2 → **0** |
| **total** | **13** | **8, none of them the repair** | **0** | **13 → 0** (23 conversion occurrences → 0) |

A `%[-#+0,(]*[0-9]*(\.[0-9]+)?[a-zA-Z]` sweep over all **117** result files now returns **one** file —
`T-207`'s own, which **quotes** the defective strings as its record.

## 5. The eight numeric movements are the studies' own irreproducibility, and that is MEASURED, not asserted

A repair that only parenthesises a prose concatenation cannot move a computed quantity.
Rather than assert it, the repaired `T-129` was run a **second** time, unchanged, and the two runs diffed against each other:

| comparison | numeric fields moved | worst relative departure | worst field |
|---|---|---|---|
| run A vs `HEAD` | 14 | **0.006** | `ranges[1].minimaxPeakRatio` |
| **run A vs run B — identical code** | 11 | **0.006** | `ranges[1].minimaxPeakRatio` |
| run B vs `HEAD` (run B is the file retained) | 7 | **0.00086** | `subsets[2].minimaxWorstOverStroke` |

**Two runs of identical code move the same field by the same amount as the repair did**,
and the second run lands back on `HEAD`'s whole `ranges[1]` block **and** `HEAD`'s own `P2` verdict string, where the first did not.
Every moved field is a minimax optimum or a quantity read off one.
`CLAUDE.md` records the mechanism verbatim: *"a descent on an optimal MANIFOLD has no isolated answer to be reproducible about"*, with Polak-Ribière as the amplifier.

The eighth movement is `T-136`'s `reproductions[2].departure`, `5.36821841e−6 → 5.3682184e−6` (**1.9e−9** relative):
a **reproduction departure** — a difference of two nearly equal numbers — emitted at **nine** significant digits where `CLAUDE.md`'s own rule says **two**.
It is a live instance of the trap `C-0093` found and cured on its *convergence* axis and did not carry to its *reproduction* records.
**Not repaired here**: it is a rounding-rule change with its own blast radius, and it is **spawned rather than smuggled into a printing repair** — not queued here because it needs its own ID.

## 6. Not one claim had inherited a defective number

Every moved fragment was grepped out of `gpd/claims/`, `gpd/challenges/`, `ANSWERS.md`, `DECISIONS-FOR-NDI.md` and `JOURNAL.md`.

| the result file printed | the claim states | |
|---|---|---|
| `T-163`: *"the SAXS-measured **40.35** nm"* | `C-0103`: *"15 duplexes at the SAXS-measured **2.69 nm**"* | **right** |
| `T-40`: `EI = %.1f`, `%.1f nm`, `%.0f %%` | `C-0028`: **`172.9 pN·nm²`**, **`41.7 nm`**, **`25 %`** | **right, all three** |
| `T-138`: *"is %.4f pN/nm … present %.2fx it"* | `CH-0087`: **`75.556 pN/nm`**, **`2.267×`** | **right** |

**Amendments owed: zero.**
That is the finding a grep was needed to establish and it could not have been assumed —
and it is the evidence behind `CH-0150`.
The prose field is written **for** the reader of the JSON; the claim is written **by** somebody who has the numbers in front of them.

## 7. Two things the repair exposed that the raw conversions had hidden

- **`T-148`'s binomial standard error is exactly zero.** `"the binomial standard error at 10 000 draws is 0.0000, which is the resolution the verdict is quoted to"` — because the exceedance probability against `T-5b`'s 0.10 is **1.0 at every one of the five sample counts**. A saturated statistic has zero binomial standard error and it is the resolution of nothing; the right instrument at `p̂ = 1` is a one-sided bound (the rule of three, `3/n = 3e−4`). The `%.4f` had hidden a **degenerate diagnostic**, not merely a number.
- **`T-138`'s second bound prints `0.53x`**, not a number above one — the 45-path row read on its own placed count **under**-delivers the mandate, where the 15-path row over-delivers at `2.27×`. Both rows fire their falsifier and both did before; but the sentence a reader would have taken away was blank.

## 8. The gate is wired, and only now

- `./gradlew test` — `tasks.register<Exec>("testFormatStrings")` runs `--self-test`, added to `test`'s `dependsOn` beside `testHarness`, `testDeliverableTracer`, `testMarkdownTables` and `testCorpusLinks`.
- `tools/verify.sh` — the **sweep** over `src/` in the `checks` block, beside the reader census, the table checker and the link checker, and therefore skipped under `--drop`/`--drop-file` like the others.

`C-0083`'s rule, applied literally: the wiring went in **after** the sweep reported `0 defect(s) over src`, not before.

## 9. Validity, and what this gate cannot see

- The checker is **static**. It counts conversions against top-level commas and **cannot** know whether an argument's *type* matches its conversion — a `%d` fed a `Double` still throws at run time and this gate will not see it.
- It **cannot** see a correctly balanced call whose arguments are in the wrong **order**. The one `WRONG_NUMBER` here was caught only because the receiver's own conversion count was 1 against 3.
- It sees Kotlin `.format` receivers recovered by a backward walk. A format string built by any other route — `buildString`, a helper taking a `String` — is invisible to it.
- **The `RAW_CONVERSIONS` class needs a different instrument**: one `grep` over `gpd/results/`, which is cheaper than the static checker and catches a strictly different set. It is not wired here. **Spawned, and NOT queued here because it needs its own ID.**
- The comparison baseline is `git HEAD` at the time of the run; re-running `tools/T-207-emit-result.py` after these files are committed will correctly report every departure as zero.
- **Incidental, and the same family.** `tools/check-corpus-links.py` lists its corpus with `git ls-files`, so **run directly in the checkout it skips an uncommitted claim**: the two broken relative links this claim's own `Consumes` row carried at first passed a run reporting *"0 broken link(s) in 376 file(s)"* and were found by hand. The **gate** is sound — `tools/verify.sh` runs it inside a snapshot with **no `.git`**, where `C-0083`'s `os.walk` fallback sees everything, and it duly failed on this task's own two challenges while the claim they cite was still unwritten. The blind instrument is the one an agent uses to check its **own** work mid-iteration.

## 10. Falsifiers

| # | falsifier | fired? |
|---|---|---|
| 1 | any moved `verdict`, `decision` or `falsifierFired` field in a re-emitted result file | **no** — 0 of 7 files |
| 2 | a defect whose intended sentence cannot be recovered from the surrounding code | **no** — all thirteen recovered, three of them confirmed against a claim that already stated the number |
| 3 | a re-emission that moves a number **other** than the repaired prose | **fired, and then explained**: 8 fields, all shown by a control re-run of identical code to be the study's own descent irreproducibility (§5) |
| 4 | a checker false-positive rate high enough that the gate would be a nuisance | **fired at 1 in 14, and repaired to 0 in 13 before the wiring** |
