# CH-0016 — The output-coupling requirement is a stability threshold quoted at biases the device does not operate at, and stability is only half of it

| | |
|---|---|
| **Against** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) — the table headed *"the number an output coupling has to supply"*, and the sentence *"an output lever for the 10 nm design point must be stiffer than ~16 pN/nm at the tile, or the actuator does not have an equilibrium at its own target stroke"*; and the same numbers as they stand in [`C-0016`](../claims/C-0016-design-window.md)'s `P2` and in `TASKS.md`'s standing findings |
| **Raised by** | [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (`T-16`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological, on two counts — (1) a **stability** threshold is quoted where a **placement** condition is what decides the design, and (2) the threshold is evaluated at two grid biases that **bracket** the device's operating bias without sampling it, in an interval `C-0012` itself names as an open question |
| **Direction** | **favourable to the programme, but NOT uniformly** — see "What the run actually found", below. It replaces a 5–277 pN/nm range with one number §3 fixes on its own, and it removes the 85.6–276.6 pN/nm headline entirely; but at 10 nm it **raises** the stability floor above `C-0012`'s 0.10 V column. A favourable direction is the one in which an error survives longest (`CH-0011`'s own words), so every number below is reproduced from `C-0012`'s own result file, and this challenge was **corrected against `T-16`'s run** before it was filed |
| **Status** | **UPHELD on both grounds**, as the index has recorded since, and its own *"favourable direction"* claim is **struck** by its own run: at 10 nm the located floor is 1.5–4.4× *higher* than the column it replaces. ~~raised, and corrected once.~~ **No number in `C-0012` moves**: its blocking force, `W(3 nm)` and `k_eff(3 nm)` are reproduced at both grid biases at 2 mM to a **worst relative departure of 3.82e−9** over 36 comparisons by re-running its own solver. What is challenged is what the table is a requirement *for*. Two statements in the first draft of this challenge were **wrong and have been struck** — they are listed at the end rather than deleted |

---

## What is challenged

`C-0012` establishes, correctly and with 810 solved operating points behind it:

> The loaded operating point is stable if the coupling supplies at least `|k_eff|` of its own stiffness where `k_eff < 0`. At 2 mM, six-model bracket:
>
> | bias | 5 nm | 7 nm | 10 nm |
> |---|---|---|---|
> | 0.10 V | **0 — stable** | 11.2 pN/nm | **5.3 – 16.0 pN/nm** |
> | 0.25 V | 55.3 pN/nm | **85.6 – 276.6 pN/nm** | **47.6 – 71.5 pN/nm** |

**Every one of those numbers is reproduced** in `gpd/results/T-16-output-coupling-stiffness.json`, from
`C-0012`'s own file and by re-running its own solver, and none is disputed.

What is challenged is the two claims wrapped around them.

---

## Ground 1 — stability is necessary, and placement is what decides the design

The output coupling is a **load line** drawn across the actuator's characteristic. With `R(s)` the coupling's
reaction at stroke `s`, positive upward, the operating point is the **first root of `W(s) = R(s)`**, and it is
stable iff `dR/ds > −dW/ds = |k_eff|`.

`C-0012`'s table is the second condition. The first one is missing, and it is the binding one, because it is
what fixes the number:

> **The force delivered to the load over a stroke is `k_c·Δs`, independently of any preload — equivalently,
> an unpreloaded coupling sitting at `s*` is the chord of the characteristic through the origin.
> So §3's own 100 pN and 3 nm fix the output-coupling stiffness at `100/3 = 33.333… pN/nm`,
> by arithmetic, with no physics in it at all.**

That is not an alternative estimate of `C-0012`'s quantity — it is a *different* quantity, and the sentence
*"must be stiffer than ~16 pN/nm at the tile, or the actuator does not have an equilibrium at its own target
stroke"* conflates them. A coupling of exactly 16 pN/nm has an equilibrium and it is **not** at the target
stroke; a coupling of 4950 pN/nm (forty-five 10 nm duplexes in tension, which is what one would first draw)
is stable at every bias and delivers **essentially zero** stroke — `C-0017` measures 0.005 nm for the
buildable version of exactly that scheme.

**The design requirement is therefore two-sided**, and `C-0012` states only the lower half.

## Ground 2 — neither bias the table is quoted at is an operating bias

`C-0012` computes `biasForSimultaneousTarget` — the bias at which the device delivers 100 pN **at** a 3 nm
stroke — and reports it as **0.082–0.155 V at 7 nm** and **0.134–0.192 V at 10 nm** (2 mM, six-model bracket).

Its coupling table is quoted at **0.10 V and 0.25 V**.

- At 10 nm, `W(3 nm)` is **below** 100 pN at 0.10 V and **above** it at 0.25 V under **all six** layer models — asserted as a test in `src/test/kotlin/coupling/CoupledCharacteristicTest.kt`. The operating bias is strictly inside an interval the grid does not sample, and **every one of the six carries `C-0012`'s own bracket string `[0.1, 0.25]`**.
- At 7 nm the crossing falls below 0.10 V for **one** of the six models and inside `(0.10, 0.25)` for the other five. At **neither** grid bias, under **any** model, does `W(3 nm)` come within 10 % of 100 pN.
- `C-0012` obtains that crossing by **interpolating across its own grid**, not by locating a root. Bisecting for the same quantity moves it by **up to 6.1 %** over the 54 states `T-16` solves.

`C-0012`'s own open question 5 says exactly this: *"What happens between 0.1 V and 0.25 V is not resolved […]
the bias grid has no sample between them."* It was raised there as a question about *validity ranges*. It is
also, and more consequentially, a question about **the requirement itself**, because the requirement is
evaluated at the operating point and the operating point is in that gap.

**The 0.25 V column is an over-driven state.** At 10 nm and 0.25 V the device delivers 149.6–198.7 pN at a
3 nm stroke, i.e. **1.5–2.0× §3's own force target**. No one would operate it there, and the 47.6–71.5 pN/nm
and 85.6–276.6 pN/nm figures are the cost of not operating it there.

---

## What the run actually found — and it is not uniformly favourable

`C-0017` re-solves the requirement at the located operating bias. **The direction of the correction is not the
same at the two heights, and the first draft of this challenge asserted that it was.**

| 2 mM, six-model bracket | `C-0012` at 0.10 V | `C-0012` at 0.25 V | **at the operating bias** | direction |
|---|---|---|---|---|
| **5 nm** | 0 — stable | 55.3 | **0 — stable** | unchanged |
| **7 nm** | 11.2 | 85.6 – 276.6 | **0 — stable, all six models** | **the requirement disappears** |
| **10 nm** | 5.3 – 16.0 | 47.6 – 71.5 | **23.41 – 27.91** | **1.5–4.4× HIGHER than the 0.10 V column** |

So the challenge stands on both grounds, but its **consequence** is mixed:

- **At 7 nm the stability requirement is removed entirely** — `k_eff` at the operating bias is `+7.4` to
  `+100.4 pN/nm` under all six models, so the 11.2 and the 85.6–276.6 both describe biases the device does not use.
- **At 10 nm the stability requirement is raised**, from `C-0012`'s 5.3–16.0 to 23.41–27.91 pN/nm — and the
  binding number is still §3's own 33.333, which clears it by only **1.19–1.42×** at 2 mM. That margin sits
  inside `C-0005`'s 123–214 % mean-field error, so the verdict is **NOT EXCLUDED**, never established.
- **The 0.5 mM operating point is where the margin is real** — 2.09–8.65×, because the bias needed is lower
  and the force's decay length longer. That is leaf `A2.2`'s low-screening condition arriving by a third route.

---

## What this changes, and what it does not

| | `C-0012` as written | as `T-16` reads it |
|---|---|---|
| the quantity | `\|k_eff\|` at the held gap | `k_c*` = `F/δ` for placement, **and** `\|k_eff\|` for stability |
| where it is read | 0.10 V and 0.25 V | at the bias where `W(3 nm) = 100 pN`, located by bisection |
| 7 nm | 11.2 → 85.6–276.6 pN/nm | **0 — the point is stable there** |
| 10 nm | 5.3–16.0 → 47.6–71.5 pN/nm | **23.41 – 27.91 pN/nm at 2 mM**, 3.86–15.94 at 0.5 mM |
| what a lever must beat | a range spanning 52× | **one number, 33.333 pN/nm**, and a stability check against it that passes everywhere |

**`C-0012`'s central verdict is untouched and is partly confirmed**: the §6 target is reachable, and at 10 nm
the operating point it is reachable at is not one the device holds by itself. **But `C-0012`'s `(c′)` verdict —
*"FAIL at 7 and 10 nm"* — is only half right: at 7 nm, read at the bias the device actually operates at, the
held point is stable under every one of the six layer models and at every buffer.** What changes is *how much*
stiffness the coupling must bring, *at which heights it must bring any*, and — decisively — that the number is
set by §3 rather than by the layer.

## Why this is a scope error and not a numerical one

`C-0012` did not compute anything wrong. It answered the question it posed — *what stiffness makes the held
point stable?* — at the biases its own grid carried, and reported the answer. The error is in the two words
*"has to supply"*: a table of `|k_eff|` at arbitrary biases is a **map of the stability boundary**, not a
design requirement, and it becomes one only once a bias is fixed by the acceptance clause the device is built to.

This is the **fourth** instance in this programme of a quantity quoted without the condition it was evaluated at:

1. `C-0001` — *"stiffness is not a single number at the resting height; quote it at a stated compression"*;
2. `CH-0011` — *"quote `k_es`'s sign with the gap it applies to"*;
3. `CH-0015` — *"a bias ceiling must be quoted with the load it was evaluated at"*;
4. **this one** — *a coupling stiffness must be quoted with the bias, and with which of the two conditions it discharges.*

## What should change

- **In `C-0012`:** the table should be headed *"the stiffness below which the held point at this bias is unstable"*, with a note that the operating bias lies between its 0.10 V and 0.25 V samples; and the sentence about *"an equilibrium at its own target stroke"* should be split into the placement condition and the stability condition. Its `(c′)` row should read **PASS at 5 and 7 nm, FAIL at 10 nm** once read at the operating bias. A banner to this effect is placed on `C-0012`.
- **In `C-0016`:** axis **(f)** should read *"output-coupling stiffness — fixed by §3 at 33.3 pN/nm, cleared by a 45-attachment ssDNA-tuned coupling, with a stability check against `|k_eff|` at the operating bias that passes at every height"* rather than as a 5–277 pN/nm unknown, and its `P2` closes **non-empty**.
- **In `TASKS.md`'s standing findings:** the sentence *"the §6 target requires an output coupling that supplies 5–72 pN/nm of its own stiffness"* should be replaced by the placement number and the located-bias floor.

## What was wrong in the first draft of this challenge, and is struck

Filed rather than deleted, because the loop is what §7 evaluates.

1. ~~*"At 7 nm the crossing falls below 0.10 V for **two** of the six models."*~~ — **wrong**. `C-0012`'s own
   file has **one** of six (strong-stretching(two-body), 0.0815 V). Asserted as a test now.
2. ~~*"It lowers the binding requirement from a 5–277 pN/nm range to a single number."*~~ — **half wrong**. It
   lowers the 7 nm requirement to zero and it **raises** the 10 nm one from 5.3–16.0 to 23.4–27.9 pN/nm. The
   *binding* requirement is indeed a single number, 33.333 pN/nm, but the stability floor it must clear is
   larger at 10 nm than the column that was being replaced. **The favourable framing was the error, exactly as
   this challenge's own header warns.**

## How this challenge would fail

If `|k_eff|` at the located operating bias had turned out to **exceed** 33.333 pN/nm, then §3's own mandated
coupling would not stabilise the point, the placement and stability conditions would be mutually unsatisfiable
for an unpreloaded linear coupling, and `C-0012`'s larger numbers would become the requirement again — this
time as the stiffness of a **preloaded** coupling, whose preload the layer must carry at zero bias, which is
`T-13`'s unanswered question. **It did not: the worst floor anywhere in the box is 27.91 pN/nm.** `C-0017`
reports the value at every height, model and buffer.
