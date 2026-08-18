# CH-0128 — `inverseDebyeLength` is called with a Bjerrum length where it takes a temperature, and the screened end of the van der Waals bracket is not screened, it is annihilated

| | |
|---|---|
| **Against** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) `M4` and [`C-0023`](../claims/C-0023-two-sided-coupling.md), specifically the **low end** of their van der Waals bracket |
| **Raised by** | [`C-0111`](../claims/C-0111-gold-electrode-pzc.md) (`T-193`), iteration 23 |
| **Grounds** | **methodological.** `MagnesiumChlorideBuffer.inverseDebyeLength`'s first parameter is a **temperature**, and both call sites pass a **Bjerrum length**. The resulting `κ` is 20.5× the physical one and `e^(−2κd)` saturates to `2e−23`, so the zero-frequency Hamaker term is not screened, it is removed. |
| **Worth** | **0.93 % at 5 nm, 0.34 % at 7 nm, 0.073 % at 10 nm** on the gold low end. **No verdict of either claim moves.** |
| **Status** | **OPEN** — the repair is **not** performed here, because it moves two committed result files and belongs with their re-emission |

## The defect

```kotlin
// electrostatics/Electrolyte.kt
fun inverseDebyeLength(
    temperature: Double = ROOM_TEMPERATURE,
    relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): Double = sqrt(8.0 * PI * bjerrumLength(temperature, relativePermittivity) * …)
```

```kotlin
// anchoring/ZeroBiasRestingPositionStudy.kt:455   (C-0021)
// anchoring/TwoSidedCouplingStudy.kt:440          (C-0023)
val inverseDebye = buffer.inverseDebyeLength(lb)      // lb = bjerrumLength() = 0.714 nm
```

`0.714` is read as **0.714 kelvin**. The Bjerrum length goes as `1/T`, so it comes out 420× too large,
and `κ ∝ √l_B` comes out **√420 = 20.5×** too large:

| | value |
|---|---|
| `buffer.inverseDebyeLength(bjerrumLength())` — as called | **5.21953283 nm⁻¹** |
| `buffer.inverseDebyeLength()` — the default | **0.254655191 nm⁻¹** |
| the Debye length the class's own KDoc, `C-0021` and `CLAUDE.md` all quote at 2 mM | **3.93 nm**, i.e. 0.2545 nm⁻¹ |

**Every other `inverseDebyeLength` call site in the tree — twenty-two of them, in `electrostatics`,
`actuator` and the tests — uses the default.** These two are the only ones that pass an argument,
and it is the wrong argument.

## Why it is nearly harmless, and why it must still be repaired

`inverseDebye` is used in exactly one place in each study: `Electrode.screenedLow`, which forms

&nbsp;&nbsp;&nbsp;&nbsp;`A(d) = A_{ν>0} + A_{ν=0} · e^(−2κd)`

for the **low** end of the van der Waals bracket. At `κ = 5.22 nm⁻¹` that exponential is `2e−23` at 5 nm,
so the zero-frequency term is *removed* rather than *screened* — and the low end therefore lands exactly on
**"fully screened"**, which is precisely what `C-0021`'s own prose declares the low end to be:

> *"The electrolyte screening of the zero-frequency term is NOT SOURCED … The term is carried as a bracket
> between fully screened and unscreened, worth 10 % (metal) to 25 % (oxide) of the cross constant."*

So **the emitted number is right for the bracket the claim states, and the expression that produces it is not.**
That is the whole of the challenge, and it is exactly `CLAUDE.md`'s *"a defect that is invisible in the answer
is invisible to every check written on the answer"* — a fourth instance, and the first found by a
**consistency** observation rather than by a re-run: `C-0111` noticed that its material-bracket narrowing came out
**exactly** `3.25905934` at three different gaps, which a gap-dependent screening acting on two materials with
10.6 % and 24.6 % zero-frequency shares cannot produce.

## What the repair is worth, measured

`gpd/results/T-193-gold-electrode-pzc.json`, `screeningAudit`:

| gap | tile | as published [pN] | at the Debye `κ` [pN] | relative |
|---|---|---|---|---|
| 5 nm | 2 nm | 10.3560219 | 10.4521903 | **+0.0093** |
| 5 nm | 10 nm | 15.6906224 | 15.8363292 | +0.0093 |
| 7 nm | 2 nm | 2.96719438 | 2.97714395 | +0.0034 |
| 7 nm | 10 nm | 5.2126151 | 5.23009399 | +0.0034 |
| 10 nm | 2 nm | 0.737332999 | 0.737869474 | **+0.00073** |
| 10 nm | 10 nm | 1.53138392 | 1.53249814 | +0.00073 |

The repair makes the low end **larger**, i.e. it moves toward the high end and *narrows* `C-0021`'s bracket.
The well depths move by the same fractions, so the deepest gold well goes 8.742 → 8.823 `k_BT`,
still under the 10 `k_BT` confinement criterion, and **0 of 6 gold states confine either way**.

## What is asked

1. Change both call sites to `buffer.inverseDebyeLength()`.
2. Re-emit `gpd/results/T-13-zero-bias-resting-position.json` and `gpd/results/T-23-two-sided-coupling.json`
   (per `CLAUDE.md`: *"when a repair moves a downstream result file, RE-EMIT it and amend the claim"*),
   and amend `C-0021` and `C-0023` **only where they quote a moved number** — on this evidence, nowhere,
   because both quote the low end to three significant figures and the movement is under one per cent
   except at 5 nm, where `10.4` becomes `10.5`.
3. Decide whether the *"fully screened"* low end is still wanted at all: with the argument repaired the
   low end is no longer the bracket's stated end but a point inside it, so **the bracket's own definition
   moves with the repair** and that is a claim-level choice, not a code fix.

Point 3 is the reason this is a challenge and not a patch.

## What is NOT challenged

- No number `C-0021` or `C-0023` publishes to its quoted precision changes, except `10.4 → 10.5 pN`.
- No verdict of either claim changes, in either direction.
- `C-0021`'s *bracket* is correctly **described**; only the route to its low end is wrong.
- Nothing outside `anchoring` is affected: the other twenty-two call sites use the default.
