# C-0073 — The tree does not round to nine digits, it rounds to nine digits **six independent times**; and the direction `CH-0043` called honest turns out to cost almost nothing and still be the wrong one

| | |
|---|---|
| **Task** | [`P-18`](../tasks/P-18.md) |
| **Leaf** | none — numerics infrastructure under `A2.1`, consumed by every leaf |
| **Verification type** | **in-silico** (four probes on the `T-1f` pipeline at one Gen-1 design point, plus a re-emission and field-by-field diff of every file that can move, and four more re-emitted as the falsifier) **+ logical** (which files can move under this change, established by construction — and **falsified**, see Part 4) |
| **Verdict** | **PASS — direction (a), the rounding carried DOWN, and the reason is not the one either direction was ranked on.** Tightening `HEIGHT_TOLERANCE` from `1e−6` to `1e−9` costs **1.02×** the SCF solves, so `CH-0043`'s "costs compute" is refuted; it is rejected anyway because the residual the height bracket works on is **discontinuous** — `M = round(h/Δz)` — with a measured jump worth **`7.9e−5`–`9.9e−5`** in the root, so the ninth digit it would buy sits **two decades below the fourth digit the discretisation already destroys**, and buying it would move every SCF-derived number and force exactly the hand re-adjudication `CH-0043` exists to describe. |
| **Maturity** | **TRL 1–3.** A property of this repository's own solvers, measured against their own **declared** tolerances. Nothing here is measured against an experiment, and no physics is re-derived. |
| **Provenance** | `gpd/results/P-18-determined-precision.json`, produced by `brush.DeterminedPrecisionStudyKt`; model changes in `src/main/kotlin/structure/ResultRounding.kt`, `src/main/kotlin/brush/SelfConsistentField.kt`, `src/main/kotlin/brush/FluctuationCorrectionStudy.kt`, `src/main/kotlin/brush/ScfDensityProfileStudy.kt`; **21 gate-named tests** in `src/test/kotlin/structure/DeterminedPrecisionRoundingTest.kt` (11) and `src/test/kotlin/brush/DeterminedPrecisionTest.kt` (10); re-emission through `tools/study.sh` on isolated trees |
| **Conditions** | IEEE 754 binary64. One Gen-1 design point: `L₀ = 10 nm`, `σ = 0.0240 nm⁻²`, des Cloizeaux interaction, production grid `Δz = 0.2 nm`, `Δn = 1/2`, SCF field tolerance `1e−11` in `w`, `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with Mg²⁺, 40 × 40 nm tile, §3's 100 pN. |
| **Consumes** | [`CH-0043`](../challenges/CH-0043-a-reproducibility-certificate-that-certifies-the-path.md) (the question), [`C-0031`](C-0031-bracketed-root-repair.md) (the measured `T-1d`/`T-1f` movement distribution, consumed as data and **not** re-measured) |
| **Constrains** | [`C-0011`](C-0011-scf-density-profile.md), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md) and — through `T-1d`'s file, which `T-2` **reads** — [`C-0016`](C-0016-design-window.md). Three files re-emitted. **No verdict, no flag and no quoted figure of any of them moves.** |
| **Raises** | [`CH-0085`](../challenges/CH-0085-a-window-edge-quoted-at-a-tie.md), against `C-0011`'s and `C-0016`'s **four-figure rendering** of the 10 nm upper edge — `0.260150` is exactly a tie at four significant figures, so *"0.2601"* was the side of it the pre-`P-18` file happened to land on. No number of either claim fails. Four rounding sites remain over-printed and are queued as `P-19` with their measured determined precision attached, in the same spirit as `C-0031`'s `P-17`: changing code that produces published results costs a re-run of everything downstream, and that price is worth paying only where the over-print can change an adjudication. |

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
falsifier". **The declared falsifier FIRED**, and the construction argument is wrong for a reason
worth having found: a result file is also an **input**. See Part 4.

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

The two edited studies re-run through `tools/study.sh` on isolated trees, against `HEAD`'s committed
`gpd/results/`. The comparison is **not** byte-for-byte, because a byte-for-byte diff is exactly
the certificate `CH-0043` showed to be the wrong instrument. It is field-by-field, and the test is
the strong one: **the change removes digits, so every moved field must equal the old value
re-rounded.** Anything else is a finding.

| | `T-1f` (`C-0019`) | `T-1d` (`C-0011`) |
|---|---|---|
| numeric fields changed | **714** | **18 007** |
| relative movement min / median / max | `4.8e−9` / `6.4e−7` / `4.7e−6` | `1.0e−9` / `8.2e−7` / **`4.7e−3`** |
| **string (findings prose) changes** | **0** | **0** |
| **boolean flags changed** | **0** | **0** |
| **structural changes** | **0** | **0** |
| **not explained by re-rounding** | **0** | **9 of 18 007** — see below |

**No verdict moves, nowhere.** `T-1d`'s `insideStandingBracket` flags are unchanged at every entry
even though the comparison now runs at the emission precision, and `T-1f` carries no flags. The
findings prose of both is byte-identical — it is computed before the serialisation boundary, so a
change of emission precision cannot reach it.

**`T-1f`'s maximum of `4.7e−6` is the arithmetic maximum of a six-digit rounding** — `5×10⁻⁶` at a
leading mantissa of 1 — so the whole file is *exactly* digits removed and nothing else.

**`T-1d`'s `4.7e−3` is the per-key three-digit rounding and only that.** 1 817 of its 18 007 moved
fields exceed `1e−5`, and **every single one of them is `stiffnessAtNineTenths` (911) or
`stiffnessAtSevenTenths` (906)** — the two keys the per-key override applies to, out of 915
responses. Every other key's largest movement is `≤ 4.99e−6`, again the six-digit maximum. The
largest movements are `1054.96208 → 1050.0` and `102.476178 → 102.0`.

**`T-1d`'s own `strokeWindows` block** — the one `C-0016` was built from — moves only in the seventh
figure (`0.00563453823 → 0.00563454`).

### The nine fields that are not a plain re-rounding — double rounding at a tie

Nine of 18 007 differ from a naive "re-round the nine-digit file" by **exactly one unit in the last
printed place**, and every one of them is a tie:

```
workingVolumeFraction   0.06166695 → 0.0616669   (naive re-rounding says 0.061667)
tangentStiffness        159.6655   → 159.665      (naive says 159.666)
firstMomentHeight       2.250075   → 2.25008      (naive says 2.25007)
```

The study rounds the **raw double**; the check rounds the **nine-digit print of it**. When the
sixth significant digit falls on an exact half, those two disagree, because the tie is decided by
the binary representation of a different number. **Rounding a rounded number is not rounding the
original.** It fires at a rate of `5e−4` here, it is bounded by one unit in the last place by
construction, and it is a property of the *check*, not of the file — which is why the check is
reported with its failure rate rather than used as a pass/fail gate.

### Every other result file, by construction and by falsifier

`roundedForResult` is called at **exactly one place per study**, and the shared
`structure/ResultRounding.kt` gained only *defaulted* parameters — `roundedForResult()` with no
arguments is the identical function it was. So a study whose emission call was not edited **cannot**
emit a different byte, and only `T-1d` and `T-1f` were edited.

**The declared falsifier for that argument:** re-emit a sample of studies whose emitter was *not*
touched, one from each of the affected rounding sites and one that consumes the SCF through a
different package. If any moves, the construction argument is wrong and the full re-emission of all
65 files is owed.

| study | file | rounding site | result |
|---|---|---|---|
| `material.PegMaterialStudyKt` | `P-3-peg-material-parameters` | shared | byte-identical |
| `structure.TileLoadDistributionStudyKt` | `T-5-load-distribution` | shared | byte-identical |
| `structure.TilePositionalVarianceStudyKt` | `T-8-tile-positional-variance` | shared | byte-identical |
| `window.DesignWindowStudyKt` | `T-2-design-window` | `window/` | **MOVED — 4 864 fields** |

**The falsifier fired, and the construction argument is wrong in a way worth having found.**

**A result file is an INPUT.** `window/DesignWindowStudy.kt:292` reads
`gpd/results/T-1d-scf-density-profile.json` and builds its 61-point grafting-density grid out of it,
so rounding the *producer* down moved every value in the *consumer* — an emitter that was never
touched. The by-construction argument holds only for studies whose inputs are all *models*; for a
study whose input is another study's **file**, the rounding propagates.

The propagation is bounded and closed, and that was checked rather than assumed: `T-1d` is read by
exactly one study, `T-1f` by none, and `T-2`'s own file by none. **The affected set is exactly
`{T-1d, T-1f, T-2}` and it is complete.**

`T-2`'s movement is 4 864 numeric fields at a median `8.4e−7` and a maximum `8.7e−6`, **0 boolean
flags, 0 structural changes** — the six-digit rounding of its inputs, arriving one level down. Every
window edge moves by `≤ 5.3e−6`; the edges are the same **grid indices** and only the grid values
were re-quantised.

### The one visible consequence, and it is a tie at the fourth figure

`T-2`'s 10 nm upper edge goes `0.260149602 → 0.26015 nm⁻²`, a movement of `1.5e−6` — and its
findings string, which prints edges at **four** significant figures, therefore flips:

> *"…a non-empty sigma window at 10 nm (0.01163-**0.2601** nm⁻², 22.36x wide)"* →
> *"…(0.01163-**0.2602** nm⁻², 22.36x wide)"*

**`0.260150` is exactly a tie at four significant figures.** `0.2601496` renders as `0.2601` and
`0.26015` renders as `0.2602`, and the two differ by `1.5e−6`. So *"0.2601"* was never a determined
four-figure number: it was the side of a tie the pre-`P-18` file happened to land on.

**No claim's number fails.** `C-0016`, `C-0027`, `C-0051` and `C-0019` all quote the edge at five or
six figures — `0.26015`, `0.260150` — which is exactly what the file now carries; the width stays
`22.36×`, the constraint attribution stays *coil overlap `Σ ≥ 1`* / *3 nm stroke at 100 pN*, and
`5 nm` stays empty. What flips is a **four-figure rendering** in `C-0011`'s and `C-0016`'s prose,
and that is `CH-0085`.

**`T-2` is kept as re-emitted**, on `C-0031`'s precedent: the tree should record what the current
code emits, and leaving the old file would guarantee a diff on the next re-run carrying no
information.

### A number emitted as a STRING is not rounded, by construction

`T-2`'s `parameters/graftingDensityGridRatio` is `(grid[1]/grid[0]).toString()`, and
`roundedForResult` passes strings through untouched — correctly, it cannot know which strings are
numbers. It moved `1.109130975 → 1.10913`, i.e. it was carrying **ten** significant digits in a file
that declares nine, straight from a `Double.toString()`. A rounding applied at the serialisation
boundary is only as complete as the type it dispatches on.

### And this study's own file

`P-18-determined-precision.json` is emitted at six digits with the movement keys at three and a
`1e−18` floor, and re-run through `tools/study.sh`: **byte-identical**. It contains no argmin, and
it emits **no wall clock** — `C-0066` had to delete a `runtimeSeconds` field to get a byte-identical
re-emission, so the deterministic unit of cost here is the SCF **solve count**.

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
