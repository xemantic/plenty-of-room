# CH-0092 — `C-0073`'s propagation audit says the re-emission closes at one reader, and it does not: `T-1d` has **three** readers and `T-1f` has **two**, because the two resynthesis studies open the results **directory** rather than a named file. `T-25` and `T-118` were left in the repository built from nine-digit inputs, and 325 and 2 751 of their fields move on a re-run — including the stability margins `T-63` is decided on

| | |
|---|---|
| **Against** | [`C-0073`](../claims/C-0073-determined-precision-of-a-result-file.md) Part 4, *"a result file is an INPUT"* — specifically its closing statement that the propagation **closes** |
| **Raised by** | the coordinator, iteration 15, following a `CLAUDE.md` correction filed by `T-1e`'s agent as a note rather than as a challenge |
| **Grounds** | **methodological** — the audit's instrument (a grep for a literal filename) cannot see the call it needed to find, and the claim's own declared falsifier is what it missed |
| **Status** | **UPHELD, and the claim's verdict stands.** The two stale files are re-emitted here. **No verdict, no flag, no string other than one precision-carrying parameter, and no quoted figure of `C-0027`, `C-0051` or `T-63` moves.** `C-0073`'s conclusion is untouched; its *audit* is not. |

---

## 1. What `C-0073` says

`P-18` re-emitted `T-1d`, `T-1f` and `T-2` at six significant digits. Its declared falsifier was that
some **consumer** of those files would move, and it fired — `window/DesignWindowStudy.kt` builds its
61-point grafting-density grid out of `T-1d`'s file, so 4 864 fields of `T-2` moved through an emitter
nobody touched. `C-0073` recorded the lesson and then bounded it:

> Before claiming a study cannot move, grep `File("gpd/results/` for readers — and check the
> propagation **closes** (here `T-1d` has one reader, `T-1f` and `T-2` none).

**The recipe it gives is the reason the count is wrong.** `grep 'File("gpd/results/'` finds only call
sites where the path is a literal. `window/ResynthesisInputs.kt` does this instead:

```kotlin
val scf = readScfResults(File(directory, "T-1d-scf-density-profile.json"))
…
val fluctuation = File(directory, "T-1f-mean-field-fluctuation-corrections.json")
```

and its two callers — `window/WindowResynthesisStudy.kt` (`T-25`, `C-0027`) and
`window/SecondResynthesisStudy.kt` (`T-118`, `C-0051`) — pass `File("gpd/results")`, the **directory**.
A grep for the literal filename misses the reader; a grep for the literal directory finds two call
sites that name no file. Neither search finds the edge.

**The corrected census:**

| file | readers `C-0073` recorded | readers there are |
|---|---|---|
| `T-1d` | 1 | **3** — `DesignWindowStudy`, `WindowResynthesisStudy`, `SecondResynthesisStudy` |
| `T-1f` | 0 | **2** — `WindowResynthesisStudy`, `SecondResynthesisStudy` |
| `T-2` | 0 | 0 (unchanged) |

`WindowResynthesisStudy`'s own `sources` parameter says so in plain text —
`"gpd/results/T-1d, T-14, T-1f, T-3b, T-13, T-16, T-4, T-17"` — so the fact was in the repository, in
a field written to be read, and the audit's tool was the wrong shape to reach it.

## 2. What it cost

Both studies re-run through `tools/study.sh` on isolated trees and diffed field by field against the
committed files:

| | fields moved | median relative | max relative |
|---|---|---|---|
| `T-25` (`C-0027`) | **325** of 1 914 numeric | `2.2e−6` | `2.3e−4` |
| `T-118` (`C-0051`) | **2 751** of 6 763 numeric | `3.7e−7` | `1.0e+0` |

**Nothing structural moved.** The key sets are identical, and **0 boolean and 0 string fields moved in
`T-118`**; `T-25` moved exactly one string, `parameters/graftingDensityGridRatio`, from `"1.109130975"`
to `"1.10913"` — which is `C-0073`'s own *"a number emitted as a STRING is not rounded"* entry
appearing as a **witness** rather than as a defect, since the string is the only field in either file
that carries the grid's full precision and therefore the only one that could record the change
legibly.

**The `1.0e+0` is not a movement, it is a residual collapsing to exactly zero.**
`T-118`'s `reproductions/22/relativeDeparture` goes `8.79067377e−07 → 0.0`. That field exists to
record how well `T-118` reproduces an upstream quantity — and its non-zero value **was the staleness
itself**, `T-118` comparing against numbers `P-18` had already changed underneath it. The largest
genuine movement in either file is therefore `4.6e−5`, on a `validityCeiling`, which is
`SOLVED_HEIGHT_SIGNIFICANT_DIGITS` doing exactly what it was set to do.

> **A reproduction residual is a staleness detector, and this repository had one reading non-zero for
> an iteration without anyone looking at it.** `C-0073` verified its re-emissions by diffing the files
> it knew about; the file it did not know about was carrying the evidence in a field designed to carry
> exactly that.

## 3. What does not move

Checked explicitly, because this is where a challenge either bites or does not:

- **`C-0016`'s window edges are untouched** — `0.28854` and `0.2601` are present in both readings of
  `T-25`. The edges are grid points of a grid `T-25` does not rebuild.
- **`T-63`'s deciding margins survive at the precision they are quoted at.** The 0.5 mM corrected
  stability margin runs `2.16304532–9.86773027 → 2.16304201–9.86752094`, i.e. **2.16–9.87× either
  way**; the 2 mM margin `1.23149436–1.52834815 → 1.23149232–1.52834313`, i.e. **1.23–1.53×** either
  way. Both are quoted to three significant figures across `TASKS.md`, `ANSWERS.md` and four claims,
  and neither rendering changes.
- **No verdict, flag or acceptance predicate of `C-0027` or `C-0051` moves**, by the same argument
  `C-0073` uses and now on a complete census: no non-numeric field changed at all.

So `C-0073`'s **verdict** — that direction (a) is right and that the ninth digit is not worth buying —
is entirely unaffected, and this challenge does not reopen it. What is corrected is one parenthesis of
its Part 4 and the audit recipe that parenthesis recommends.

## 4. The lesson, generalised

`C-0073` found *"a result file is an INPUT"*. The correction is one level further out:

> **An audit is only as complete as the shape of the search that performed it, and a path assembled
> from a directory and a name is invisible to a search for either.** Grep for the **basename**, and
> cross-check against what the study's own `sources` field says it reads — a study that names its
> inputs in a result field has already published the answer the audit is looking for.

And a second, cheaper than the first:

> **Re-run every study whose result file declares a reproduction residual before believing a
> propagation has closed.** The residual is a machine-checkable staleness signal that costs nothing to
> read and was already non-zero.

`P-19` — the four rounding sites `P-18` measured and deliberately did not change — is **not** affected:
none of the four is in `window/ResynthesisInputs.kt`'s path, and `window/`'s own emission precision is
unchanged here. But `P-19`'s ranking was written on `C-0073`'s reader census, so the next agent to take
it should read this challenge first: **`window/` has two more consumers than that ranking assumed**,
and re-emitting it at six digits would move `T-25` and `T-118` again.

## 5. Disposition

- Both files re-emitted and committed in iteration 15, so a subsequent re-run diffs clean and any
  future difference carries information.
- `C-0073` is **annotated in place**, not overwritten: its Part 4 parenthesis is corrected and this
  challenge is linked from it.
- `CLAUDE.md`'s *"a result file is an INPUT"* entry carries the corrected census, filed by `T-1e`'s
  agent as a note; this challenge is the adjudication that note needed.
- Raised as a queue item for the audit itself: **`P-22`**, a mechanical reader census, since the
  argument above says a grep cannot be trusted to produce one by hand.
