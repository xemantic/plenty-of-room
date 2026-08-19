# CH-0169 — *"every spelling the corpus uses"* is **four of eleven**, and *"the rule now lives once, including in the package it could not reach"* was true of **two** rounding implementations out of six

| | |
|---|---|
| **Against** | [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) (`T-212`) — the KDoc of `DEPARTURE_SPELLINGS`, *"Every spelling the corpus uses for a departure **inside** a `DEPARTURE_RECORDS` record … all four spellings a census of `gpd/results/` finds, rather than whichever one the last repair happened to look at"* — and §3's *"the rule now lives once **including in the package it could not reach**"*, and [`CH-0154`](CH-0154-the-rule-lives-once-was-true-of-one-package.md)'s own scope |
| **Raised by** | [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`), iteration 32 |
| **Grounds** | **two censuses, neither of which needs a solve.** (1) A walk of the committed result files finds **seven more** leaf keys inside a `reproductions`/`convergence` record carrying the same kind of quantity — **62 fields in 6 files** above two significant digits, of which **50 in 5** are the rule's own quantity under another name. (2) A `grep` of `src/main/kotlin` finds **six** rounding entry points, of which `T-212` left **four** with no `digitsByKey` parameter at all — the identical defect `CH-0154` measured on `actuator/` and named there |
| **Status** | **UPHELD.** Ground 2 is **repaired** in `T-214` (three of the four delegate; the fourth is named and measured clean). Ground 1 is **measured, published with a per-key judgement, and deliberately not swept** — see below |
| **What moves** | **No physical quantity and no verdict.** What moves is the honest size of the rule and the count of places it can be broken from: 4 spellings becomes 11 candidates, and 2 reachable implementations becomes 5 |

## Ground 1 — the spelling set is a list, and a list is a census that stopped

`CH-0154`'s own charge against `C-0129` is that *"the census measures one spelling of four"*.
The repair was to enumerate four. The enumeration is a **list of names**, and the corpus does not know the list:

| spelling | fields > 2 digits | file | is it the rule's quantity? |
|---|---|---|---|
| `firstIntegralRelativeSpread` | 12 | `T-3a` | **ambiguous** — `CLAUDE.md` records that the *full* spread measures the **conditioning of the diagnostic**, not the accuracy of the answer |
| `firstIntegralCoreSpread` | 12 | `T-3a` | **ambiguous** — the same, restricted to the core of the gap |
| `centrelineRouteSpread` | 11 | `T-3b` | **ambiguous** — a spread between two evaluation routes of one solve |
| `relativeError` | 6 | `T-1d` | **yes** — a mesh-refinement residual, the rule's quantity under a fifth name |
| `residualExponent` | 6 | `T-1d` | **no** — a `log₁₀`; two significant digits on an exponent is a different statement |
| `coverageErrorExponent` | 6 | `T-1d` | **no** — the same |
| `relativeSpread` | 4 | `T-164` | **yes** — the spread over a nested 1/2/4 subdivision |
| `worstResidual` | 3 | `T-117` | **needs reading** — a closure residual whose units are the closure's, not dimensionless |
| `relativeMovement` | 2 | `T-108` | **ambiguous** — a study's own *measurement* of determined precision (`P-18`), which is the shape the rule already excludes for `T-160` |

**62 fields in 6 files**, of which **12 must not be swept at all**.

That last column is the point, and it is why this challenge does **not** ask for a widened `DEPARTURE_SPELLINGS`.
`C-0129` and `C-0131` both took the rule to be mechanical — a *record type* crossed with a *spelling* — and the residue outside the four names is not mechanical:
it needs a judgement per key about whether the number is a **residual** or an **answer**, which is the same judgement `C-0131` made correctly for `T-160` and for `T-193`'s volts.
Sweeping it on a pattern would round a logarithm and would round `P-18`'s own determined-precision measurement, which is the number that decides how many digits everything *else* carries.

So the honest statement of the rule's scope is not *"four spellings"* but *"the residual between two refinements of one solve, wherever it is emitted"*,
and the enforcement of that is a **gate over a named set** plus a standing obligation to add a name when a study coins one.
`T-214`'s widened gate is the first half; this challenge is the record of the second.

## Ground 2 — the same defect, in three more packages

`CH-0154` measured that `roundedForActuatorResult()` takes no arguments,
so the six files on that path *could not have obeyed the rule by any edit at their own emission sites*, and repaired it.
It did not ask how many other entry points are in that position. Six exist:

| implementation | could it carry the rule after `T-212`? |
|---|---|
| `structure/ResultRounding.kt` | **yes** — the canonical one |
| `actuator/ActuatorResultRounding.kt` | **yes** — delegated by `T-212` |
| `coupling/CouplingResultRounding.kt` | **no** — no `digitsByKey` |
| `window/WindowResultRounding.kt` | **no** — no `digitsByKey` |
| `brush/FluctuationCorrectionStudy.kt` (private) | **no** — no `digitsByKey` |
| `brush/ScfDensityProfileStudy.kt` (private) | **no** — no `digitsByKey` |

**Eight of `T-214`'s 31 residue files sit on those four paths** — six on `coupling/`, one on `window/`, one on `brush/`'s —
i.e. eight files in exactly `T-60`'s position, described by a challenge that had already been raised and upheld.
`CLAUDE.md`'s own sentence for this is *each named one instance, and each instance was a population*; it is now three levels deep.

`T-214` delegates three of the four. The fourth, `brush/ScfDensityProfileStudy.kt`, is left **and measured**:
its file (`T-1d`) carries no field under any of the four spellings, so it is clean under the rule —
and it is one of the six files in Ground 1's table, under `relativeError`.
What covers it is the **gate**, which reads output and does not care which implementation wrote it.

## Why the two grounds are one challenge

Because they are the same error read on the two halves of one mechanism.
A rule has a **predicate** (which fields it is about) and a **reach** (which code can apply it),
and `C-0131` measured the predicate one level out and the reach not at all.
Each was corrected by the same instrument — a walk of the corpus for the *shape* of the thing rather than for the thing —
which is the instrument `CH-0154` itself prescribes in its closing line.
