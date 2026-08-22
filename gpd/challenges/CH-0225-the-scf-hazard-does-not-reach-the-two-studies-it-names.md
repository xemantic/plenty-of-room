# CH-0225 — **`CH-0223`'s stated ground is wrong about the only two studies it names: neither `T-1` nor `T-1c` is downstream of a solved SCF height, and the judgement it declines to make is not the DIGIT COUNT but the FLOOR**

| | |
|---|---|
| **Against** | [`CH-0223`](CH-0223-seven-emitters-call-no-rounding-function.md) §4 — *"the digit count is a **judgement per study**: `T-1c` and `T-1` are downstream of a solved SCF height and are determined to `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` or fewer (`P-18`, `CH-0043`), not to nine"* |
| **Raised by** | [`T-278`](../tasks/T-278-emission-header-residue.md), which had to make that judgement in order to repair the seven |
| **Grounds** | **methodological** — a rule invoked against a solver the studies do not call, and a per-study judgement located on the wrong axis |
| **Status** | **CLOSED by the same task.** The count `CH-0223` publishes is right and is untouched; its ground and its axis are both replaced, and the replacement is what the repair was made on |

---

## 1. The claim, and why it is checkable rather than arguable

*"Downstream of a solved SCF height"* is a statement about a **call graph**. `SOLVED_HEIGHT_SIGNIFICANT_DIGITS`
is documented in `structure/ResultRounding.kt` as the precision of a quantity downstream of
`SelfConsistentFieldLayer.heightAtPressure`, whose `HEIGHT_TOLERANCE` is `1e-6`. So the question is
whether either study reaches that solver.

Neither does. At `b853b85`, the commit `CH-0223` was filed on:

| source | occurrences of `SelfConsistentField` or `heightAtPressure` |
|---|---|
| `brush/BrushStiffnessStudy.kt` (`T-1`) | **0** |
| `brush/CrossoverLayerStudy.kt` (`T-1c`) | **0** |
| `brush/LayerDesignPoint.kt` — everything `T-1` evaluates | **0** |
| `brush/BrushCompression.kt` — its three compression models | **0** |
| `brush/PolymerBrush.kt` | **0** |

`T-1` evaluates `DeGennesScaling(9/4)`, `DeGennesScaling(3)` and `MilnerWittenCates`, whose
equilibrium heights are closed forms, and its one iteration is `heightUnderLoad` — **a hundred
bisection halvings** of a bracket `[L₀·1e−12, L₀]`, which the function's own KDoc describes as
driving *"the bracket to machine precision"*. `T-1c` builds `AlexanderBoxLayer` and
`StrongStretchingLayer`, whose roots close through `bracketedRoot` at its default **`1e-15`** and
whose `solveLambda` exits on `CONVERGENCE = 1e-15`; `InteractionFreeEnergy`'s inversion carries the
same constant.

By `P-18`'s own conventions block — *"PROVENANCE of an emitted number is the loosest solver
tolerance on any path from a model input to it. Nine digits is defensible only where that is
≤ 1e−9"* — both studies are the **shared** rounding site, `P-18`'s own first row: *"analytic models
and closed-form geometry, looseTolerance 1e−15, determinedDigits 9, overPrintedBy 0."*

The same is true of the other five. `T-6`'s two boundary searches are 300 geometric halvings over
eleven decades; `T-7`'s bandwidth contour and `P-3`'s des Cloizeaux reach are 200 halvings each;
`P-6` and `P-9` run **no solver at all**.

## 2. Why the wrong ground was reachable, and it is `CLAUDE.md`'s own trap

`SOLVED_HEIGHT_SIGNIFICANT_DIGITS` is a real rule and it binds three real sites — `T-1d`, `T-1f`
and `window/`, all named in `P-18`'s `roundingSites` table. What it does not bind is *"the `brush`
package"*. `T-1` and `T-1c` are polymer-layer studies in that package and the inference from
**subject** to **solver** is the one this file already warns about in another register: *a rule
whose name does not carry its lattice will be applied to every lattice.* Here a rule whose name
carries a **solver** was applied to a **topic**.

## 3. The judgement is real, and it is on the other axis

`CH-0223` is right that a per-study judgement is owed and right to refuse a blanket answer. It puts
the judgement on the **digit count**, and measured over the seven the digit count is the same
number for all of them. What is **not** the same is `RESULT_ABSOLUTE_FLOOR`, and `P-18` states why:
*an absolute floor is a claim about units, and it does not travel.*

Simulated over the committed files (`tools/T-278-rounding-simulation.py`), the default floor of
`1e-9` — documented as *"no force below a nanopiconewton is of interest"* — would flatten **370 of
the 41 297 fields to exactly `0.0`**, and the two studies it reaches are opposite cases:

| | |
|---|---|
| `T-1c`, **274 fields** | every one an `equilibriumStiffness` of a **strong-stretching** layer at its own resting height, `1e−13` to `1e−16 pN/nm`. `CLAUDE.md` records that quantity as **exactly zero** — *"the Milner-Witten-Cates SCF form has exactly zero, because the brush's outer edge is diffuse"* — so the floor states the physics, in the locked units, and is `RESULT_ABSOLUTE_FLOOR`'s own documented case (`T-5`'s zero internal shear) |
| `T-7`, **96 fields** | every one an `inertialTime`, **in seconds**, the smallest `6.96645e−14 s`. Seconds are not piconewtons. The same study's `verticalDrainageTime` clears the floor by half a unit in the first digit at `1.53e−09 s`, and the ratio of the two **is** its own overdamping verdict |

So `T-7` takes `floor = 0.0` (`POROELASTIC_RESULT_FLOOR`, declared with its reason) and the other
six take the default. **One of the seven needed a per-study decision and it was not a digit count.**

## 4. What is NOT challenged

`CH-0223`'s measurement stands entirely: seven studies, one identical write shape, **41 297** of the
corpus's over-precise numeric leaves, **99.83 %**, and `T-1c` at 25 774 of its own 27 272. The
41 297 was reproduced independently here by a Python mirror of `structure/ResultRounding.kt`
(`tools/T-278-rounding-simulation.py`, whose self-test asserts `P-9`'s 70 against the committed
file). Its §6 falsifier — *"a rounding call in any of the seven that this survey missed"* — was
re-run and finds none.

## 5. What would falsify this challenge

A path from either study to `SelfConsistentFieldLayer`, or any solver on either study's path whose
tolerance exceeds `1e-9`. Both are one `grep` and one reading of a `const val`.
