# C-0031 — `bracketedRoot` had two defects, not one, and the one nobody reported is the one that was always active

| | |
|---|---|
| **Task** | [`P-15`](../tasks/P-15-bracketed-root-repair.md) |
| **Leaf** | none — numerics infrastructure under `A2.1`, consumed by `A2.1` and `A2.2` |
| **Verification type** | **in-silico** (nine executable tests, five failing before the repair) **+ logical** (the interiority theorem that turns `S-143`'s observation into a diagnosis) **+ literature** (Dowell & Jarratt 1971, read directly from the PDF) |
| **Verdict** | **PASS. Branch (b) in letter, branch (a) in substance:** nine result files move and **no verdict, no window edge and no quoted figure moves anywhere**. Every movement is one ulp, a solve's own declared tolerance, or a quantity that is identically zero — except `T-1d`'s deep-compression stiffness (1.5 %), which is checked in Part 4 rather than folded in. `C-0016`'s window edges are **byte-identical**. |
| **Maturity** | **TRL 1–3.** A solver property, established by test and by proof. Nothing here is measured. |
| **Provenance** | `src/main/kotlin/brush/GraftedLayer.kt` (`bracketedRoot`); `src/test/kotlin/brush/BracketedRootTest.kt` (10 tests); `tools/P-15-rerun.sh`; full suite via `tools/verify.sh` |
| **Conditions** | IEEE 754 binary64 throughout. The smallest normal double is `≈2.225e-308`, so a product of two residuals underflows once both are below `≈1.49e-154`. |
| **Consumes** | [`C-0019`](C-0019-mean-field-fluctuation-corrections.md) (`S-143`, the observation) |
| **Constrains** | [`C-0003`](C-0003-crossover-valid-layer-response.md), [`C-0011`](C-0011-scf-density-profile.md), [`C-0016`](C-0016-design-window.md), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md), [`C-0027`](C-0027-window-resynthesis.md) — every height and chain-length inversion in the programme |

---

## The claim, in one line

**`bracketedRoot` carried two independent defects: the product-based sign test `C-0019` reported,
which is real but reachable only below `1.5e-154` and therefore never fired at this project's
scales; and an *unconditional* Illinois halving, which nobody reported, was active at every scale
and every call, and had silently degraded the method into something **worse than the bisection it
was chosen over** — 52 evaluations against bisection's ~52 on `x² − 2`, where correct Illinois takes
11.**

---

## Part 1 — the reported defect: real, and not the one that was costing anything

Both sign tests were written on a **product**.

- **The step test**, `atLeft * atEstimate < 0.0`. When both factors are tiny and of opposite sign
  the product underflows to `−0.0`; `−0.0 < 0.0` is `false`; the branch that should move the *right*
  endpoint moves the *left* one, and moves it to a value of the **same sign as the right**. The
  bracket is gone.
- **The entry test**, `require(atLeft * atRight <= 0.0)`. The same underflow in the other direction:
  two tiny residuals of the *same* sign multiply to `+0.0`, which satisfies `<= 0.0`, so a bracket
  containing no root is **accepted**. `C-0019` did not name this half.

Neither is a tolerance question and no iteration budget repairs either. Every individual residual is
exactly representable; only their product is not, which is why the failure is invisible in the
residual — the quantity anyone would inspect.

### Why an escape implies this defect — the interiority theorem

`C-0019` observed the *symptom*: "an evaluation a fifth of the way below the dry thickness". The
step from symptom to cause is a one-line proof, and it is asserted as a test rather than argued:

> A secant through two points whose ordinates have **opposite signs** crosses zero *between* them.
> The interpolated abscissa is the convex combination of `left` and `right` with weights
> `|f_right|/(|f_left|+|f_right|)` and `|f_left|/(|f_left|+|f_right|)`, both in `[0, 1]`.

So **while the bracket holds, an escape is arithmetically impossible**. An evaluation outside
`[low, high]` therefore *implies* the bracket was lost, and the only thing in this routine that can
lose it is the sign test. That closes the causal chain without reproducing the run.

### And the honest qualification, which changes how the defect should be ranked

The underflow needs **both** residuals below `≈1.49e-154`. A direct probe of the *unrepaired*
routine on residuals shaped like this project's — pressures in pN/nm², lengths in nm, over brackets
spanning up to 30 decades — escapes **zero** times in every case tried:

| residual probed on the unrepaired routine | bracket | evaluations | evaluations outside the bracket |
|---|---|---|---|
| `exp(−h/0.35) − 1e−30` | `[1, 30]` | 149 | **0** |
| `h⁻¹² − 1e−12` | `[1, 30]` | 97 | **0** |
| `x⁻⁹ − 1e6` | `[1e−3, 1]` | 124 | **0** |
| `x² − 2` | `[0, 2]` | 52 | **0** |

The escape reproduces at `1e−170` and is fixed there. **Which physical inversion drove `C-0019`'s
residual below `1.5e-154` was not re-identified, and is not claimed to be.** The defect is repaired
because it is a defect; it is not repaired because it was demonstrably corrupting an answer.

---

## Part 2 — the unreported defect: the halving was unconditional, and that was always active

The Illinois halving was applied on **every** step. It should be applied only when the **older**
endpoint is retained. Dowell & Jarratt (*BIT* **11** (1971) 168–174), read directly from the PDF,
state the rule in two lines, and the condition is theirs, not an interpretation:

> i) if `f_{i+1} f_i < 0`, then `(x_{i−1}, f_{i−1})` is replaced by `(x_i, f_i)`
> ii) if `f_{i+1} f_i > 0`, then `(x_{i−1}, f_{i−1})` is replaced by `(x_{i−1}, f_{i−1}/2)`
>
> — *"the introduction of the value `f_{i−1}/2` for `f_{i−1}` is a modification designed to speed
> convergence by preventing the retention of an end point."*

The halving lives in case (ii) alone. Halving unconditionally deflates **both** residuals once the
estimate starts alternating sides, and two deflated residuals of nearly equal magnitude and opposite
sign interpolate to the **midpoint**. The method silently becomes bisection while still paying for a
secant.

The measured cost, on the repaired routine against the broken one:

| residual | bracket | broken (unconditional) | repaired (conditional) | bisection needs | speed-up |
|---|---|---|---|---|---|
| `x² − 2` | `[0, 2]` | 52 | **11** | ~51 | **4.7×** |
| `2 − x²` | `[0, 2]` | 52 | **11** | ~51 | **4.7×** |
| `x⁵ − 1e−8` | `[0, 1]` | 73 | **35** | ~55 | **2.1×** |
| `1/x − 1/300` | `[1, 1e4]` | 61 | **22** | ~55 | **2.8×** |
| `e^x − 10` | `[0, 5]` | 53 | **15** | ~52 | **3.5×** |
| `1e−170 (x³ − 0.027)` | `[0.01, 1]` | 48 | **15** | ~50 | **3.2×** |

The accuracy is equal or better in every row — which is exactly why the defect survived. **The
answer was never wrong. Only the evaluation count was**, and the evaluation count is the entire
reason Illinois was chosen over the bisection `T-1` used: the routine's own doc comment claims
"roughly an eightfold saving", and it was delivering a *loss*.

This is the third instance in this repository of a class the project keeps meeting:

> **A defect that is invisible in the answer is invisible to every check written on the answer.**
> `S-95`'s copy-back reverted a result file and "no number moved"; `S-166`'s rounding made an
> *argmin* irreproducible while every number was identical; and here a solver produced roots correct
> to the last ulp while doing three to five times the work it was selected for.

### The two defects are coupled, and each hid the other

The unconditional halving deflates `atLeft` by `2^−n`. That is precisely what drives a residual
toward the underflow floor the sign test needs — so the performance defect is the *amplifier* of the
correctness defect. Repairing only the sign test would have left the routine slow; repairing only
the halving would have removed the amplifier and made the sign defect even harder to reach, without
removing it. **Neither repair is complete alone, and only one of the two was reported.**

---

## Part 3 — the same pattern elsewhere, ranked rather than fixed

Three sibling sign tests are written on products. All three were checked against the underflow
condition and **none is reachable**, because in each the fixed factor is `O(1)` and a product
underflows only when *both* factors are tiny:

| site | test | reachable? | why |
|---|---|---|---|
| `brush/FluctuationCorrectionStudy.kt:332` | `require(atLow * atHigh < 0.0)` | **no** | residuals are a stroke in nm minus 3 nm, and `ln(π R² σ)` — both `O(1)` |
| `brush/FluctuationCorrectionStudy.kt:339` | `residual(...) * atLow > 0.0` | **no** | `atLow` is fixed at the bracket end and `O(1)`; only one factor ever becomes small |
| `anchoring/CoupledStandoffJoint.kt:276` | `det(next) * atLow <= 0.0` | **no** | a sin/cos determinant, `O(1)`; the *bisection* underneath it already uses a sign flag, correctly |
| `anchoring/StandoffBaseJoint.kt:224` | `det(next) * atLow <= 0.0` | **no** | as above |

They are **not** changed here, and the reason is a rule this project already holds: changing code
that produces published results costs a re-run and a diff of everything downstream, and that cost is
only worth paying for a defect that can fire. Queued as `P-17` with this analysis attached, so the
next agent inherits the ranking rather than the alarm.

---

## Part 4 — the re-run and diff: does any published number move?

`C-0019` set the condition for closing `P-15`, and it is this part, not the code change:
`C-0003`, `C-0011` and `C-0016` all consume the routine, so the repair is not complete until every
result file that consumes it has been re-run and diffed.

**Method.** `tools/P-15-rerun.sh` runs each consuming study in a tree carrying the repair and
**nothing else** on top of `HEAD` — not the working tree, which at the time held three concurrent
agents' in-flight edits and would have folded them into the diff. The baseline is `HEAD`'s own
committed `gpd/results/`, so the broken tree is not re-run: that would reproduce what is already in
git, at twice the compute. Comparison is **byte-for-byte**, which is meaningful only because the
project already rounds the whole result tree at the serialisation boundary.

**Verdict: branch (b) in letter, branch (a) in substance — and the gap between them is the finding.**
Result files move. **No verdict, no window edge, no acceptance figure and no quoted number moves anywhere.**

| study | result file | fields moved | relative movement (min / median / max) | what moved |
|---|---|---|---|---|
| `window.DesignWindowStudyKt` | `T-2-design-window` | **0** | — | byte-identical |
| `window.WindowResynthesisStudyKt` | `T-25-window-resynthesis` | **0** | — | byte-identical |
| `material.PegMaterialStudyKt` | `P-3-peg-material-parameters` | **0** | — | byte-identical |
| `material.SolventQualitySaltStudyKt` | `P-6-solvent-quality-vs-salt` | **0** | — | byte-identical |
| `brush.FluctuationCorrectionStudyKt` | `T-1f-…-fluctuation-corrections` | 589 | `8.4e−9` / **`9.0e−7`** / `4.2e−3` | the SCF's own `HEIGHT_TOLERANCE` |
| `coupling.OutputCouplingStudyKt` | `T-16-output-coupling-stiffness` | 6 | `2.1e−7` / `7.4e−7` / `1.4e−6` | reproduction residuals, `\|value\| ~1e−9` |
| `anchoring.ZeroBiasRestingPositionStudyKt` | `T-13-zero-bias-resting-position` | 6 | `1.3e−9` / `3.2e−9` / `6.2e−9` | the ninth printed digit |
| `anchoring.TwoSidedCouplingStudyKt` | `T-23-two-sided-coupling` | 1 | `8.2e−7` | one reproduction residual, value `1.5e−4` |
| `structure.TilePositionalVarianceStudyKt` | `T-8-tile-positional-variance` | 0 numeric, **1 structural** | — | a `null` became a number — **a real defect, fixed** (below) |
| `brush.CrossoverLayerStudyKt` | `T-1c-…-layer-response` | 11 336 | `1.1e−16` / **`4.3e−16`** / — | **one ulp**, 11 281 of them |
| `actuator.StrokeAndBlockingForceStudyKt` | `T-3-stroke-and-blocking-force` | 4 | `5.6e−9` | the ninth printed digit |
| `actuator.MaximumUsableBiasStudyKt` | `T-4-maximum-usable-bias` | 8 | `3.0e−9` / `5.0e−7` / `3.3e−6` | tangency residuals, `\|value\| ≤ 2.7e−5` |
| `brush.ScfDensityProfileStudyKt` | `T-1d-scf-density-profile` | 10 796 | `1.5e−9` / `8.6e−7` / **`1.5e−2`** | see below — the one file whose largest mover is **not** noise |

### `T-1d` is the exception, and it is checked rather than folded in

`T-1d` carries the largest movement in the re-run — **1.48e−2**, a per-cent-scale change in a *stiffness*,
which is not a residual, not a zero and not one ulp. It is therefore checked against what the consuming
claims actually quote, rather than described as noise:

- **`strokeWindows` is byte-identical.** `C-0016`'s window edges — the deliverable of `T-2` — do not move at
  all. Independently confirmed: `window.DesignWindowStudyKt` and `window.WindowResynthesisStudyKt` both
  re-emit **byte-identical** files.
- **`standingClaimCheck` moves by ≤ `4.3e−6`** and **`restingLoadSensitivity` by ≤ `4.6e−6`** — those are
  `C-0011`'s quoted `N(L₀)`, stroke and secant stiffness.
- **Only 122 of 10 796 fields move by more than `1e−3`, and every one of them is
  `stiffnessAtSevenTenths` (43) or `stiffnessAtNineTenths` (79)** — the per-design-point stiffness at *deep
  compression*, inside the 183-point sweep, not a headline of any claim.

The cause is structural rather than accidental: at 0.7–0.9 of `L₀` the SCF layer is stiff and `k` is a
**derivative** of a rapidly varying pressure, so a `1e−6` relative change in the solved height is amplified
by about `10⁴`. That is the same sensitivity `CLAUDE.md` already records from the other side —
*"an SCF window edge is not grid-converged where a stiffness is"*, where the edge moved 23.4 % between two
meshes while `d ln k/d ln K` moved 0.5 %. **Convergence is a property of the quantity, not of the solve**,
and here the ordering happens to be the reverse of that example: the edge is pinned and the deep-compression
stiffness is not.

### The one real defect the re-run exposed, in `T-8` — and it pre-dates the repair

`T-8`'s undefined-case record reported `unconstrainedPistonRms` under the guard `layerStiffness > 0.0`.
The case that record exists for is the strong-stretching profile **at `L₀`**, where the stiffness is exactly
zero — the block's own comment says *"numerically a rounding-level positive, physically nothing"*.
**A sign test on a quantity that is meant to be zero is decided by the noise.**

**`HEAD` was already wrong, in two of its three undefined cases**, and the repair only changed which:

| case (first contact, strong stretching) | `HEAD` | after the repair, before the fix | after the fix |
|---|---|---|---|
| two-body | **1 172 864.7 nm** | 1 172 864.7 nm | `null` |
| virial | `null` | **13 637 236 nm** | `null` |
| des Cloizeaux | **22 522.4 nm** | 22 522.4 nm | `null` |

Against a **10 nm** layer, in a field whose own comment reads *"null, not Infinity … writing one would be a
number where the honest answer is 'not well posed'"*.

**And the emitted file hides it, by a mechanism worth naming.** `roundForResult` applies
`RESULT_ABSOLUTE_FLOOR = 1e-9`, so a rounding-level `layerStiffness` is emitted as **exactly `0.0`** — while
the quantity *derived* from it, `√(k_BT/k)`, **escapes the floor because it is large**. `HEAD` therefore
asserts, on adjacent lines of the same record:

```json
"layerStiffness": 0.0,
"unconstrainedPistonRms": 1172864.7
```

which is not merely implausible but arithmetically impossible — `√(k_BT/0)` is not `1.17e6`. **An absolute
floor is not inherited by what is computed from the floored value, and a reciprocal derivation amplifies
exactly what the floor was hiding.**

Re-guarded on the **physics** instead: an amplitude is reportable only while the linearised fluctuation stays
inside the layer it is fluctuating against. That is the criterion the surrounding block already applies to
call the case undefined at all, it is noise-immune, it needs no tolerance, and it makes the record
self-consistent. Asserted as a test that the reported amplitude **cannot depend on the sign of a zero**.

**Every movement is one of exactly three things**, and none of the three is a change in an answer:

1. **One ulp.** `T-1c` is the extreme case and the clearest: 11 281 of its 11 336 moved fields shift by a
   median `4.3e−16`, i.e. the last bit. Its models are analytic, so the residual is exact and the repaired
   solver lands on the same root by a shorter path.
2. **The declared tolerance of a solve underneath.** `T-1f` moves by a median `9.0e−7` against
   `SelfConsistentFieldLayer.heightAtPressure`'s `HEIGHT_TOLERANCE = 1e-6`. It moved by exactly its own
   noise floor, which is the signature of a quantity **relocating inside** its tolerance rather than changing.
   This is `CH-0043`.
3. **A quantity that is identically zero.** Every field in `T-1c` that moves by more than a rounding level is
   `equilibriumStiffness` — which `C-0003` establishes is **exactly zero** at `L₀`, because the
   strong-stretching pressure vanishes quadratically there — at absolute values **≤ `2.0e−11` pN/nm**, twelve
   orders below the layer stiffness anything is quoted at. The same class covers `T-16`'s and `T-23`'s
   reproduction departures (`~1e−9`) and `T-4`'s tangency residuals (`≤ 2.7e−5`). This project's own rule
   already names it: *comparing two quantities that are both meant to be zero relatively compares their noise.*

The **single prose change** in the whole tree is in `T-1f`'s findings string, where a percentage rounds
differently at its second decimal: *"0.34 %"* → *"0.33 %"* and *"1.42 %"* → *"1.43 %"*.

### Why the moved files are committed rather than kept at `HEAD`

The repaired solver is the correct one, so the tree should record what the current code emits. Keeping the
old files would leave the next agent to re-run `T-1c` and re-adjudicate a 22 704-line diff carrying no
information. That diff is the price of `P-18`, and it is recorded here rather than avoided.

---

## Verification gates

| gate | how | result |
|---|---|---|
| **1. Dimensional** | scale invariance: multiplying a residual by a positive constant cannot move its root, checked over `1e−170 … 1e80` | **PASS** (failed before the repair — this is the test that states the defect as a property) |
| **2. Limiting cases** | root at either endpoint returned exactly; both orientations of the sign change; a no-sign-change bracket rejected however small its residuals | **PASS** (the last failed before) |
| **3. Symmetry / conservation** | the bracket invariant itself — every argument recorded and asserted inside `[low, high]`; plus the interiority theorem, asserted over five residual scales and a lopsided `1e−6 : 1` bracket | **PASS** (failed before) |
| **4. Numerical convergence** | evaluation budget against bisection's, on a well-conditioned and a strongly convex root; bounds set to fail if the method ever degenerates back | **PASS** (failed before) |
| **5. Literature** | the conditional-halving rule against Dowell & Jarratt 1971, **read directly** from the PDF, not from a search summary or from memory | **PASS** — the rule matches case (ii) exactly; the unconditional form is not any published method (Pegasus is also conditional, and uses `f_i/(f_i+f_{i+1})` rather than `1/2`) |

**Test suite.** 1083 tests. The repair breaks nothing: across two full runs the only failures were
two *different* sibling agents' mid-TDD files (`SofteningCouplingStabilityTest`, `T-76`, and
`GuidedArmAnchorageTest`, `T-70`), each confirmed by dropping it with `P-16`'s new `--drop-file`.

---

## What this claim does NOT establish

- **It does not establish that `S-143`'s escape was harmless in the run that produced it.** It
  establishes that the escape mechanism is real, that it implies a lost bracket, and that no
  published result file changes when it is removed. Whether the observed escape returned a wrong
  intermediate in a record that was later overwritten is not recoverable from the artifacts.
- **It does not re-derive the physics of any consuming claim.** A byte-identical result file is
  evidence that the solver change did not move the answer, and nothing more.
- **It says nothing about the four sibling product tests beyond reachability.** They are ranked, not
  cleared by test.
