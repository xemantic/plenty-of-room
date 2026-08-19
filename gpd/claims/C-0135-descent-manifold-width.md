# C-0135 — **`T-129` reproduces; what does not is a two-valued descent manifold, and the iteration-13 tree run today lands on the reading iteration 13 did not emit.** Of 1042 fields over ten independent emissions, **1015 are identical in every member**; the 27 that move are **10 minimax objectives, 5 functionals of a minimax argmin, 10 deliberate roundings and 2 prose renderings**, and **not one boolean, `bindingStates` list or verdict moves**. The **VALUE** spans `2.62e−3` and the **POINT** `5.95e−3` — but read on a *different* functional of the same argmin the POINT spans `7.55e−4`, so the headline **0.60 %** is a property of **which field the file happens to emit**

| | |
|---|---|
| **Task** | [`T-215`](../tasks/T-215-descent-manifold-width.md), raised by [`C-0131`](C-0131-departure-and-saturation-audits.md) (`T-212`), whose `F1` needed it settled |
| **Leaf** | none — a **process** claim about the reproducibility of an emitted artifact |
| **Verification type** | **logical** (the git history of the study, its libraries and its three inputs, and a structural classification of every moving field) **+ in-silico** (six fresh runs of `anchoring.RangeRobustPlacementStudyKt` at `HEAD`, one run of the **iteration-13 tree** `git archive`d and unmodified, and a direct measurement of the optimiser's own degeneracy) |
| **Verdict** | **PASS on P1–P6.** `F1`, `F2`, `F3` and `F4` did **not** fire; **`F5` FIRED**, and what it excluded is the *input* channel — which is how the mechanism was found |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Nothing here moves a physical quantity, a flatness verdict or a design recommendation |
| **Provenance** | `gpd/results/T-215-descent-manifold-width.json`, emitted by `coupling.DescentManifoldWidthStudyKt`; the instrument in `src/main/kotlin/coupling/DescentManifoldWidth.kt` (14 tests in `src/test/kotlin/coupling/DescentManifoldWidthTest.kt`); the retained ensemble input `gpd/data/T-215-ensemble.json` built by [`tools/T-215-collect-ensemble.py`](../../tools/T-215-collect-ensemble.py) and drawn by [`tools/T-215-ensemble.sh`](../../tools/T-215-ensemble.sh) |
| **Conditions** | `T = 300 K`, `k_BT = 4.141947 pN·nm`; the ensemble is **ten** independent emissions of `gpd/results/T-129-range-robust-placement.json` — three read out of `git` (`cf7de13`, `d1ff95e`, `ce11aaf`), six fresh runs at `HEAD` in two snapshots, and one run of the tree at `cf7de13`. The degeneracy measurement is a 45-path multi-state minimax on `C-0022`'s solved collars at `C-0017`'s `33.3333 pN/nm` mandate |
| **Consumes** | [`C-0131`](C-0131-departure-and-saturation-audits.md) §6 (the observation and its three runs), [`C-0129`](C-0129-result-file-hygiene.md)/[`C-0093`](C-0093-shared-body-coupling.md) (the two-digit departure rule, and the decision-precision discipline the descent already carries), [`C-0064`](C-0064-robust-distribution.md)/`T-129` (the file under test), [`C-0127`](C-0127-format-string-repair.md) (the raw-conversion discriminator this task reuses verbatim) |
| **Raises** | [`CH-0162`](../challenges/CH-0162-three-agreeing-runs-are-a-draw-not-a-verdict.md) against `C-0131` §6, and [`CH-0163`](../challenges/CH-0163-a-worst-field-movement-is-an-emission-choice.md) against the corpus-wide practice of measuring a re-run difference by its worst field |

---

## The claim, in one line

**The file reproduces up to a two-valued manifold in one of its four ranges and nine of its thirty-one subsets, and everything else in it — 1015 of 1042 fields, every boolean and every verdict — is bit-stable across ten emissions spanning seventeen iterations.**

---

## 1. The cheap bound ran first, and it excluded every alternative before a solve

`gpd/results/T-129-range-robust-placement.json` has **three** committed versions, and between them the study source changed **twice**:

| commit | iteration | what changed in `RangeRobustPlacementStudy.kt` | can it move a computed number? |
|---|---|---|---|
| `cf7de13` | 13 | — (first emission) | — |
| `d1ff95e` | 28 | a `+`-binds-tighter-than-`.format()` repair inside one `settles` **prose** field | **no** |
| `ce11aaf` | 30 | `digitsByKey = DEPARTURE_DIGITS_BY_KEY` at the **serialisation boundary** | **no** |

So the three committed versions are a **free three-member ensemble of one computation**, readable with `git show` and no run at all.

The inputs and the libraries close the alternatives:

- the study reads exactly three files — `T-3b`, `T-108` and `T-125`. `T-3b` and `T-108` have not been committed since long before iteration 28; `T-125` moved at iteration 30 in **departure fields only**.
- the seven declarations the descent runs on — `minimaxStiffnessDistribution`, `MultiStateSurrogate`, `normalisedStiffnesses`, `perPathStiffnessCeiling`, `rimStiffenedWeights`, `admissibleStiffnessRatio`, `perPathThermalForces`, and the three internal helpers `searchDecision`, `cappedStiffnesses`, `optimiseStiffnessDistribution` — live in `coupling/RobustDistribution.kt` and `coupling/NonUniformCoupling.kt`, **neither of which changed between iterations 28 and 30**.
- `roundForResult` itself did not change either, and the study's only computational use of it is in the placement sweep, not in `ranges`.

**An input change and a library change are therefore excluded before any solve**, and the only alternative left to the manifold reading is a defect.

### The structural bound, also free

**A closed-form field that moved would falsify the manifold reading immediately**, so the classification ran next.
Over the ten-member ensemble, **1015 of 1042 fields are identical in every member**, and every one of the 27 that move is a descent output or a deliberate rounding:

| class | fields | widest | what it is |
|---|---|---|---|
| **VALUE** | 10 | `4.5745e−3` (`subsets[2]`) | a minimax objective, `minimaxWorstOverStroke` |
| **POINT** | 5 | `5.9522e−3` (`ranges[1]`) | a functional of a minimax argmin |
| **ROUNDING** | 10 | — | `T-212`'s deliberate two-significant-digit departure rule |
| **RENDERING** | 1 | — | `predicates[1].verdict`, differing in **digits only** |
| **REPAIR** | 1 | — | iteration 28's format-string repair; one member still carries a raw `%.3f` |
| **OTHER** | **0** | — | **`F1` did not fire** |

---

## 2. The archaeology run settles it: the tree is not responsible

The remaining worry was that the movement clusters in **time** — iterations 13 and 28 emit one reading, iteration 30 and `C-0131`'s three runs the other — which reads like something in the repository having changed.

`git archive cf7de13 | tar -x` into a fresh snapshot, and run it unmodified.
**The iteration-13 tree, today, emits `0.0365712568` — the reading iteration 13 did *not* emit.**

And a fresh run at `HEAD` closed the loop from the other side: of six drawn in two snapshots, **`B2` landed on `0.0364754519`**, the iteration-13/28 reading.
The census over the whole ensemble:

| `ranges[1].minimaxWorstOverStroke` | members |
|---|---|
| `0.0365712568` | 7 — iteration 30, fresh runs `A1`, `A2`, `A3`, `B1`, `B3`, and the iteration-13 tree run today |
| `0.0364754519` | 3 — iteration 13, iteration 28, fresh run `B2` |

**The optimal set at `ranges[1]` is two-valued and a fresh run draws from both.**

---

## 3. Is the 0.60 % the same phenomenon as the `8.6e−4`? Yes, and the subsets are the *worse* half in VALUE

This is the question `T-215` was set, and the answer is not an inference — it is one pair of runs.

**`A1` and `B2`, identical code, same `HEAD`, differ in exactly twelve fields**: the six of `ranges[1]`, five `subsets[*]`, and `predicates[1].verdict` rendering the first of them — worst `5.9522e−3`.
The two things `C-0131` reported as separate — *"up to 0.60 % against the committed file"* and *"7 subsets at ≤ `8.6e−4`"* — are **both present between those two runs**.

The sizes line up once VALUE and POINT are kept apart:

| quantity | width over ten members |
|---|---|
| `ranges[1]` **VALUE** (`minimaxWorstOverStroke`) | `2.6197e−3` |
| worst `subsets[*]` **VALUE** | **`4.5745e−3`** — *wider than `ranges[1]`'s* |
| `ranges[1]` **POINT** on `max_i k_i` | `5.9522e−3` |
| `ranges[1]` **POINT** on the peak **support** force | `7.5482e−4` |

`ranges[1]` only looks like the outlier because it is the **only block that emits a POINT at all**: the 31 subsets carry `minimaxWorstOverStroke` and nothing else.
Nine of them move, `subsets[8]` taking **7 distinct readings** over ten members and `subsets[2]` **6**, against `ranges[1]`'s **2**.

**The `0.60 %` and the `8.6e−4` are one phenomenon read on two functionals, and the larger number is the one measured on the argmin.**

---

## 4. The four `max_i k_i` fields are one quantity, and that identity is the check

`minimaxPeakRatio`, `peakPathStiffness`, `peakPathForceAtAcceptableStroke` and `peakThermalForce` are `max_i k_i` rescaled by `count/mandate`, by the acceptable stroke and by `√(k_BT k)` — so they must move by the **identical** relative amount.
Measured: `5.952153e−3`, `5.952148e−3`, `5.952152e−3`, `5.952151e−3` — agreeing to one part in a million.
A file whose five POINT fields moved by five different amounts would not be reporting one argmin.

---

## 5. No verdict moves, and that is what makes this cosmetic

Of **253 boolean** and **313 string** fields compared, **0 booleans vary**.
Two strings vary: `predicates[1].verdict`, which differs in **digits only** (`"0.0365"` against `"0.0366"` — `CLAUDE.md`'s *"a moved STRING is not necessarily a moved decision"*), and `bounds[3].settles`, where two members still carry the **raw** `%.3f` iteration 28 repaired.
**Zero unaccounted. `F3` did not fire.**

Every `bindingStates` list is identical in all ten members, including at `ranges[1]`.
The moving subsets sit at `0.024–0.113` of the stroke against `T-5b`'s `0.10`, and the widest of them moves by `4.6e−3` — three orders from any threshold.

---

## 6. `F5` FIRED, and that is how the mechanism was found

`F5` was declared as: *a whole-ulp perturbation moving neither the value nor the point would mean the optimiser is locally unique and the movement has another cause.*

Measured on a 45-path multi-state minimax: advancing **one weight of one start** by whole units in the last place, holding everything else, moves the answer by `1e−16` to `1e−15` — floored to zero, because a width below `1e−12` is a difference of two `Double`s and not a reading.
**The input channel is excluded.** The descent is not chaotic in its own argument.

What it *is* sensitive to is one line of `minimaxStiffnessDistribution`:

```kotlin
val bestStart = results.minWithOrNull(compareBy({ searchDecision(it.third) }, { it.first }))!!
```

Every start is run to its own terminal point and **one strict comparison** picks the winner.
Run each start alone and measure that set:

| problem | starts within 1 % | VALUE width | POINT width | amplification |
|---|---|---|---|---|
| two anti-parallel 10 nm states | 3 of 3 | `0.0058` | **`0.021`** | 3.5 |
| three states, the 2 nm state included | 2 of 3 | `0.0072` | `0.012` | 1.7 |

The near-optimal starts agree in VALUE to under a per cent and disagree in POINT by two per cent.
`T-129`'s own file reports `startsWithinOnePartInAMillion = 1` at every range — the search **is** start-limited, and it says so.
**A jitter that flips that one comparison moves the file by the POINT width of the near-optimal set, and the size the file shows is inside it.**

---

## 7. What this claim does *not* establish

- **It does not make the file reproducible.** It measures the width instead, which is what `T-215` offered as the second branch. Removing the manifold would mean changing the descent's terminal selection — which moves published numbers in `T-129`, `C-0064` and everything downstream, for a movement that changes no verdict.
- **It does not measure `T-129`'s own surrogate.** The degeneracy and near-optimal-spread measurements run on a 45-path attachment grid and `C-0022`'s transcribed collars, not on `C-0063`'s 34 upward roots. They establish that the optimiser has this property on a problem of the same class and size; the ten-member ensemble is what establishes it on `T-129` itself.
- **It does not bound the manifold from above.** A width over ten members is a **maximum over a sample**, monotone non-decreasing in the member count. Its convergence record settles from member 3 on, and that is a statement about this sample, not a proof that no third basin exists.
- **The basin census is not a probability.** 7:3 is ten draws under uncontrolled machine load; nothing here identifies what selects the basin beyond *"the arithmetic path"*.

## 8. Validity range

The ensemble is the file `gpd/results/T-129-range-robust-placement.json` at commits `cf7de13`, `d1ff95e` and `ce11aaf` and at `HEAD` on OpenJDK 25.0.3, Gradle 9.7.0.
The classification is exact for this file's field names and would have to be re-derived for another study's.
`T-215`'s own result file is byte-identical over two runs — **after** its `spreads[*]` widths were emitted at two significant digits, because they inherit the very irreproducibility they measure (up to `8e−5` between runs) and its band test was moved to the decision precision.
