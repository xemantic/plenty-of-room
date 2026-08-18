# CH-0149 — the format-defect census `C-0125` published is **7 files, 7 studies and 7 result files**, not *"6 files from 5 studies … 8 result files"*, and **one of the fourteen is the checker's own false positive**

| | |
|---|---|
| **Against** | [`C-0125`](../claims/C-0125-scaffold-remainder.md), its *"Spawned"* paragraph and the `T-195` row of `TASKS.md`: *"its sweep of `src/` finds **14 defects in 6 files from 5 studies, all committed**, with raw `%` conversions in **8 result files**"* |
| **Raised by** | [`C-0127`](../claims/C-0127-format-string-repair.md) (`T-207`), iteration 28 |
| **Grounds** | **arithmetic** — a census stated as a count, checkable in one pass, and wrong in all three of its counts. Plus **a tool defect**: one of the fourteen rows is the checker's, not the tree's |
| **Status** | **OPEN** |
| **What moves** | Three counts in a spawning paragraph, and the *"14"* that `T-207`'s own row inherits. **No verdict of `C-0125` moves** — the scaffold-remainder result, its bounds and its bias re-read are untouched; the checker's existence, its 17 self-tests and the live instance it caught before `T-195`'s run are all exactly as recorded |

## The charge

`C-0125` retained `tools/check-kotlin-format-strings.py` and reported its sweep.
Re-run against the same commit, the same script reports:

| | `C-0125` | measured at `HEAD` |
|---|---|---|
| defects | 14 | **14** ✓ |
| source files | 6 | **7** |
| studies | 5 | **7** |
| result files reached | 8 | **7** |
| of the 14, real | (not stated) | **13** |

The seven source files, one study each, are
`anchoring/PathCountConsistencyStudy.kt`, `anchoring/RangeRobustPlacementStudy.kt`,
`anchoring/StandoffBaseJointStudy.kt`, `anchoring/TrussCapStudy.kt`,
`anchoring/TwoPerRowPlacementStudy.kt`, `coupling/PathCountFixedGeometryStudy.kt`
and `coupling/StapleDropoutStudy.kt`.
The seven result files they emit — `T-138`, `T-129`, `T-40`, `T-106`, `T-136`, `T-163`, `T-148` —
are exactly the files that carry a raw `%` conversion, and they are the complete set:
a `%[-#+0,(]*[0-9]*(\.[0-9]+)?[a-zA-Z]` sweep over **all** of `gpd/results/` returns those seven and nothing else.

## And one of the fourteen is the checker's

`StandoffBaseJointStudy.kt:875` is reported as *"5 conversions, 4 arguments"* and its **emitted string is correct**:

> *"… its buckling margin runs **1.85x** at 6 nm to **1.06x** at 10 nm. The recommended design is B2 at 7-9 nm, whose margin stays above **1.22x**; … margin **1.0007x** …"*

Four conversions, four arguments.
The fifth is a **nested** `"%.0f".format(it)` inside a `${…}` template whose body carries braces —
`${lengths.joinToString(", ") { "%.0f".format(it) }}` —
consumed by its own call long before the outer one runs.
The checker's template stripper was the regular expression `\$\{[^{}]*\}`, which cannot match past an inner brace,
so the inner conversion was counted against the **outer** argument list.

`C-0127` repairs it with a balanced-brace walk, **two self-tests written first**
(one failing, one already passing as the guard against over-stripping), 17 → **19**.

## Why it matters, and why it is small

It matters because a gate was going to be wired in on this census.
`CLAUDE.md`: *"a drift checker's FALSE positives cost more than its true ones, because the tool exists in order to be believed"* —
a 1-in-14 false-positive rate on a gate that fails a build is exactly the rate at which the gate gets switched off.
The repair had to precede the wiring, and it did.

It is small because **nothing `C-0125` concluded depends on any of the four numbers**.
The claim's own finding — that `CLAUDE.md` prescribes this check five times and nothing was running it — is upheld,
and so is the live instance the checker caught in `T-195`'s own study before the run,
which is the evidence the tool was retained on.

## Proposed resolution

Amend `C-0125`'s spawning paragraph and the `T-195` row of `TASKS.md` to
**7 files, 7 studies, 7 result files, 13 real defects and 1 checker false positive**, struck rather than overwritten.
No other part of `C-0125` is touched.
