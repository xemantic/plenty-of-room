# C-0073 — The tree does not round to nine digits, it rounds to nine digits **six independent times**; and the direction `CH-0043` called honest turns out to cost almost nothing and still be the wrong one

| | |
|---|---|
| **Task** | [`P-18`](../tasks/P-18.md) |
| **Leaf** | none — numerics infrastructure under `A2.1`, consumed by every leaf |
| **Verification type** | **in-silico** (four probes on the `T-1f` pipeline at one Gen-1 design point, plus a full re-emission and diff of the two files `CH-0043` named) **+ logical** (which files can move under this change, established by construction) |
| **Verdict** | **PASS — direction (a), the rounding carried DOWN, and the reason is not the one either direction was ranked on.** Tightening `HEIGHT_TOLERANCE` from `1e−6` to `1e−9` costs **1.02×** the SCF solves, so `CH-0043`'s "costs compute" is refuted; it is rejected anyway because the residual the height bracket works on is **discontinuous** — `M = round(h/Δz)` — with a measured jump worth **`7.9e−5`–`9.9e−5`** in the root, so the ninth digit it would buy sits **two decades below the fourth digit the discretisation already destroys**, and buying it would move every SCF-derived number and force exactly the hand re-adjudication `CH-0043` exists to describe. |
| **Maturity** | **TRL 1–3.** A property of this repository's own solvers, measured against their own **declared** tolerances. Nothing here is measured against an experiment, and no physics is re-derived. |
| **Provenance** | `gpd/results/P-18-determined-precision.json`, produced by `brush.DeterminedPrecisionStudyKt`; model changes in `src/main/kotlin/structure/ResultRounding.kt`, `src/main/kotlin/brush/SelfConsistentField.kt`, `src/main/kotlin/brush/FluctuationCorrectionStudy.kt`, `src/main/kotlin/brush/ScfDensityProfileStudy.kt`; **21 gate-named tests** in `src/test/kotlin/structure/DeterminedPrecisionRoundingTest.kt` (11) and `src/test/kotlin/brush/DeterminedPrecisionTest.kt` (10); re-emission through `tools/study.sh` on isolated trees |
| **Conditions** | IEEE 754 binary64. One Gen-1 design point: `L₀ = 10 nm`, `σ = 0.0240 nm⁻²`, des Cloizeaux interaction, production grid `Δz = 0.2 nm`, `Δn = 1/2`, SCF field tolerance `1e−11` in `w`, `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with Mg²⁺, 40 × 40 nm tile, §3's 100 pN. |
| **Consumes** | [`CH-0043`](../challenges/CH-0043-a-reproducibility-certificate-that-certifies-the-path.md) (the question), [`C-0031`](C-0031-bracketed-root-repair.md) (the measured `T-1d`/`T-1f` movement distribution, consumed as data and **not** re-measured) |
| **Constrains** | [`C-0011`](C-0011-scf-density-profile.md), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md) — the two files re-emitted. **No number of either moves.** |
| **Raises** | nothing. Four rounding sites remain over-printed and are queued as `P-19` with their measured determined precision attached, in the same spirit as `C-0031`'s `P-17`: changing code that produces published results costs a re-run of everything downstream, and that price is worth paying only where the over-print can change an adjudication. |

---

## The claim, in one line

**A result file has two precisions and they answer different questions** — the *reproducibility*
precision, which is how many digits survive a change of code path that respects the declared
tolerances, and the *accuracy* precision, which is how many digits survive a change of grid. This
tree printed nine of both. The first is **six** and is what a re-run diff certifies; the second is
**four** and belongs in a validity range, not in a rounding rule.

---

## Part 0 — the cheap bound, which ran first and changed the shape of the answer

Two facts are readable off the tree before any solver runs, and both fired.

**There is no tree-wide nine.** There are **six independent rounding implementations**, and they
do not even agree on the absolute floor:

| site | prints | absolute floor | provenance — the loosest solver tolerance reaching it | determined |
|---|---|---|---|---|
| `structure/ResultRounding.kt` (shared, 43 studies) | 9 | `1e−9` | analytic models, closed-form geometry | **9** |
| `brush/FluctuationCorrectionStudy.kt` (private, `T-1f`) | 9 | **`1e−12`** | `heightAtPressure`, `1e−6` | **6** |
| `brush/ScfDensityProfileStudy.kt` (private, `T-1d`) | 9 | `1e−9` | `heightAtPressure`, `1e−6` | **6** |
| `window/WindowResultRounding.kt` (`T-2`, `T-25`) | 9 | `1e−9` | grid index; **decisions already taken at six** | 6 |
| `actuator/ActuatorResultRounding.kt` (`T-3`, `T-4`) | 9 | `1e−9` | `forceMaximumGap`, `tolerance = 1e−4` | 4 |
| `coupling/CouplingResultRounding.kt` (`T-113`, `T-123`, `T-129`, …) | 9 | `1e−9` | placement / minimax searches at `1e−5` | 5 |

Three consequences, and none of them was visible from `CH-0043`:

1. **A per-provenance rule needs no new mechanism.** The mechanism is already there and merely
   uncalibrated — six packages each chose nine independently, and each can choose otherwise.
2. **`window/` already writes the answer down and then does not use it.** It carries
   `WINDOW_DECISION_SIGNIFICANT_DIGITS = 6` — "*a constraint flag that flipped because a drainage
   corner moved in its twelfth digit would put a `true` in one run and a `false` in the next*" —
   and then emits the numbers those flags were decided from at **nine**. **A decision precision and
   an emission precision that differ mean the flags in a file cannot be reproduced from the numbers
   in the same file.**
3. **A change to one rounding site silently leaves the other five**, which is why "the tree rounds
   to nine significant digits" was true as a description and false as a design.

The second cheap fact bounds the work: `roundedForResult` is called at **exactly one place per
study**, so a study whose call is untouched cannot emit a different byte. That turns "re-emit 65
files" into "re-emit the files whose emitter changed, and re-emit a sample of the others as the
falsifier". **The declared falsifier did not fire** — see Part 4.

---

## Part 1 — the determined precision, measured on four probes

All four run the *same* `T-1f` pipeline — `chainLengthAtRestingHeight` → `equilibriumHeight` →
pressure, stiffness at `0.7/0.8/0.9 L₀`, `heightAtPressure` at §3's 100 pN, stroke, secant
stiffness — at the 10 nm design point. Every fractional height is taken off the **solved** `L₀`,
exactly as `T-1d` takes it, because a stiffness at `0.7 L₀` inherits the height's indeterminacy
through its **evaluation point** as well as through the chain.

| probe | what it varies | largest relative movement | determined digits |
|---|---|---|---|
| **bracket seed** | four starting brackets, **same tolerance**, same residual | **`5.64e−7`** | **6** |
| **tolerance ladder** | `heightTolerance` `1e−6` against `1e−9` | `1.20e−7` | 6 |
| **solve history** | the warm field and the cache, same tolerance | `9.18e−12` | 9 |
| **grid count** | `M = round(h/Δz)` across one node boundary | `7.91e−5` … **`9.89e−5`** in the root | **4** |

Read together they say something none of them says alone.

**The bracket-seed probe is the one `CH-0043` is about, and it is the largest of the three
path probes.** `P-15` did not change a tolerance; it changed the *path* a root finder takes to a
root at a **fixed** tolerance, and `T-1f` moved by a median `9.0e−7`. Reproduced directly here —
one residual, one tolerance, four brackets — the roots spread by `5.64e−7`. **That is the width of
the band the answer is free to sit anywhere inside, and it is the number the rounding must
absorb.** It sits below the declared `1e−6` and is consistent with `C-0031`'s measured median.

**The warm start is not what limits determinacy.** Changing only the solve *history* — a genuinely
different sequence of cached solves and warm fields — moves the resting height by `3.3e−13` and the
worst quantity by `9.2e−12`. The SCF field converges to `1e−11` in `w` and that is enough. This
matters because it isolates the cause: the indeterminacy is the **root finder's stopping band**,
not the field solver.

**And the amplification into a second difference is real but modest here: 27.6×.** The same
history change moves `restingHeight` by `3.32e−13` and `stiffnessAtNineTenths` by `9.18e−12` —
`k/A = ∂²F/∂h²` is a second difference of free energies over a spacing that is itself a function
of the solved height, so the height reaches it twice. At this design point the factor is 27.6; at
the stiff end of `T-1d`'s 183-point sweep `C-0031` measured `10⁴`. **The amplification is a
property of the state, not of the quantity**, which is what defeats a per-key constant — Part 3.

---

## Part 2 — the direction, decided against the reason it was ranked on

`CH-0043` offered two directions and ranked them: *"the second is the honest direction and costs
compute; the first is free and costs nothing but the appearance of precision."* **The cost half of
that is wrong and the ranking survives anyway, for a different reason.**

**Cost, measured.** One design point, the whole pipeline, `heightTolerance` from `1e−6` to `1e−9`:

| height tolerance | SCF solves |
|---|---|
| `1e−4` | 276 |
| **`1e−6`** (standing) | **282** |
| `1e−8` | 288 |
| **`1e−9`** (what nine printed digits need) | **288** |

**1.02×.** Illinois is superlinear on a smooth residual and three extra decades cost six solves out
of 282. The honest direction is essentially **free**, and every argument that rejected it on cost
is void.

**Reachability, measured — and this is what decides it.** The residual `heightAtPressure` brackets
on is **not continuous**. `ScfDiscretisation` documents that "*the actual spacing is `h/M` with
`M = round(h/Δz)`, so that a wall height is represented exactly*", and `round` is a **step**
function of `h`: at every half-integer multiple of `Δz` the node count changes, the spacing changes
with it, and the computed pressure jumps by the difference of two discretisation errors.

| boundary | nodes | `P` below | `P` above | relative jump | `d ln P/d ln h` | implied indeterminacy in `h` |
|---|---|---|---|---|---|---|
| 7.5 nm | 37 → 38 | `7.10546e−3` | `7.10197e−3` | `4.91e−4` | −6.21 | `7.91e−5` |
| 8.5 nm | 42 → 43 | `2.92394e−3` | `2.92191e−3` | `6.95e−4` | −7.87 | `8.83e−5` |
| 9.5 nm | 47 → 48 | `1.07782e−3` | `1.07677e−3` | `9.79e−4` | −9.89 | **`9.89e−5`** |

So the height at which the layer carries a given pressure is uncertain at `1e−4` **because of the
grid**, and no tolerance touches that. Tightening `HEIGHT_TOLERANCE` to `1e−9` would buy digits
seven, eight and nine of a number whose **fourth** digit is already a discretisation artefact —
and it would do so by moving every SCF-derived number in the repository, which is precisely the
hand re-adjudication `CH-0043` was raised to stop paying for.

**The general statement, which is the claim:**

> **Set a result file's rounding by the largest movement a change of PATH can produce, never by the
> largest error the MODEL carries.** Here those are `5.6e−7` and `9.9e−5`, two decades apart and in
> the counter-intuitive order — the model is *less accurate* than the solver is *reproducible*.
> Rounding to the model's accuracy would blind the diff to a real change of the model; rounding to
> the solver's reproducibility makes the diff sensitive to exactly that and to nothing else.

The accuracy figure is not discarded — it belongs in a validity range, where `CLAUDE.md` already
carries its bigger brother: *"an SCF window edge is not grid-converged where a stiffness is — the
10 nm stroke edge moves 23.4 % between `Δz = 0.4` and `0.2 nm`."*

---

## Part 3 — the change

**`RESULT_SIGNIFICANT_DIGITS = 9` becomes a documented ceiling** rather than a tree-wide constant:
it is the right count only where every solver on the path is tighter than `1e−9`, which is
`C-0031`'s one-ulp analytic majority. `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` is added beside it,
measured, and the two SCF studies emit at it. `roundForResult` and `roundedForResult` gain a digit
count and a **per-key** override applied to the whole subtree under a key; both default to the old
behaviour, so the 43 studies on the shared rounding emit **byte-identically**.

### The argmin rule, folded in

Rounding at the serialisation boundary does not make a file reproducible if it contains an argmin,
because an index is not a rounded double — and the same is true of a **flag**. `T-1d`'s
`standingClaimCheck` decided `insideStandingBracket` on the raw solved number and emitted it beside
a rounded one; it now takes the comparison at the file's own emission precision. `T-1d`'s only
`minByOrNull` ranks **exact grid inputs** (`|ln(σ/σ_ref)|` over the swept grafting densities) and is
not at risk; `T-1f` contains no argmin and its `maxBy`/`minBy` separate in the first two digits.

### The absolute floor is a claim about UNITS, and it does not travel

**The first run of this study emitted its own measurement as `0.0`.** `RESULT_ABSOLUTE_FLOOR` is
documented as "*the magnitude **in the locked units** below which a result is reported as exactly
zero … the smallest force of any interest here is `1e−3 pN`" — and a *relative movement* is not in
locked units at all. A measured `3.3e−13` is exactly the kind of number the floor was written to
suppress and exactly the number the study exists to report; the declared SCF field tolerance
`1e−11` was flattened the same way, in the study's own parameter block. `roundForResult` now takes
the floor per call. This is the same shape as `C-0031`'s floored `layerStiffness` sitting beside an
unfloored `√(k_BT/k)`: **a floor stated in one dimension silently reaches quantities of another.**

### What a per-key rule can and cannot do

`T-1d` gets `stiffnessAtSevenTenths` and `stiffnessAtNineTenths` at **three** digits, because those
are the only two keys in the file whose determined precision is below six: `C-0031` re-ran the
study across a solver repair that changed no answer and measured **122 of 10 796** fields moving by
more than `1e−3`, **every one of them one of those two**, to a maximum of `1.5e−2`.

**Three digits absorbs 98.9 % of the file and does not absorb that tail, and no per-key constant
can** — because the amplification runs 27.6× at the design point and `10⁴` at the sweep's stiff
end, while a key is one number. That is the honest limit of the mechanism and it is stated rather
than hidden:

> **A per-key rule is the right granularity for PROVENANCE and the wrong granularity for
> AMPLIFICATION.** Provenance is a property of the *quantity* — which solver produced it — and is
> constant down a column. Amplification is a property of the *state*, and varies by four orders of
> magnitude down the same column. Closing the second needs a precision carried **per record**, and
> that is a larger change than `P-18`.

---

## Part 4 — the re-emission and diff

TO BE FILLED

---

## Verification gates

| gate | how | result |
|---|---|---|
| **1. Dimensional** | a digit count and a relative movement are both dimensionless: rounding commutes with a decade rescaling, checked over `1e−6 … 1e6` at 3, 6 and 9 digits; and a *relative* height tolerance pins two roots four decades apart in pressure to the same relative width | **PASS** |
| **2. Limiting cases** | `digits = 1` keeps one digit and `9` reproduces the standing behaviour exactly; zero rounds to zero at every count; integers, booleans and strings pass through; a per-key override reaches its whole subtree and nothing else; the per-call floor reaches the whole tree | **PASS** |
| **3. Symmetry / conservation** | rounding is odd in the sign of its argument at **every** digit count 1…9; a movement against a zero reference is compared **absolutely**; a quantity rounded to its determined digits is invariant under a perturbation inside its own tolerance (2 000 deterministic probes, < 5 % straddling a rounding boundary at a tenth of the movement) | **PASS** |
| **4. Numerical convergence** | the tolerance ladder itself, six rungs `1e−4 … 1e−9`, with the solve count at each; the bracket-seed spread at fixed tolerance; and the node-count jump exhibited as a test — the two node counts across a boundary really do differ and the pressure really does jump | **PASS** |
| **5. Literature cross-check** | none applies and none is offered. This is a statement about **this repository's** solvers, and the only external reference is their own declared tolerances, which is what every number here is checked against | **N/A, stated** |

---

## What this claim does NOT establish

- **It does not re-derive any physics**, and no verdict, window edge or quoted figure of `C-0011`
  or `C-0019` is re-examined. A digit removed is not a number changed.
- **It does not measure the determined precision of every state.** One design point, one
  interaction, one grid. The digit counts are quoted as a **ceiling on what may be printed**, not
  as the precision of every entry — and Part 3 names the state-dependence that defeats a constant.
- **It does not fix the other four rounding sites.** `window/`, `actuator/` and `coupling/` are
  over-printed by 3, 5 and 4 digits on the measurement in Part 0, and that measurement is a reading
  of their **declared** tolerances rather than a probe of their solvers. Queued as `P-19`.
- **It does not claim the grid figure is the accuracy of the SCF layer.** `9.9e−5` is the
  indeterminacy from a **one-node** change at `Δz = 0.2 nm`; the layer's actual discretisation error
  is a separate, larger quantity that `C-0011`'s own convergence gate owns.
