# C-0143 — The criterion is scale-covariant, so the whole `16.5×` is the RENORMALISATION and none of it is the geometry; it is owed at the BARE charge, and on the branch this device is actually on it is worth half a per cent of an asymmetry range

| | |
|---|---|
| **Task** | [`T-221`](../tasks/T-221-planar-coupling-wall.md) |
| **Leaf** | **`A7.4`**, consumed by `A2.2` and `A8.2` |
| **Verification type** | **logical** (a scale-covariance identity that reduces Kanduč Eq. (64) to one closed form, and a `2 × 2` decomposition of the disputed factor) **+ in-silico** (both of Kanduč's branches evaluated over six candidate walls, three gaps and the whole admissible asymmetry range) **+ literature** (the criterion's own derivation, read directly, for what its variables are **defined** to be) |
| **Verdict** | **PASS on `P1`–`P6`. The convention is settled on the axis that carries the verdict and bracketed on the axis that does not.** `C-0137`'s straddle is real, and it is 100 % the bare/renormalised axis and 0 % the cylinder/plane axis it was framed as. **All six declared falsifiers were checked and none fired.** No verdict of this programme moves; the repair the task turned up in a shared main source is [`CH-0178`](../challenges/CH-0178-a-criterion-with-no-root-returned-its-own-bracket-floor.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** This settles which **input** a published inequality is owed. It does not compute a correlation correction and it does not make `Ξ = 17–24` tractable — `C-0005`'s *"no systematic theory in the intermediate regime"* stands verbatim. |
| **Provenance** | `gpd/results/T-221-planar-coupling-wall.json`, produced by `electrostatics.PlanarCouplingWallStudyKt`; 6 walls, 18 repulsive readings, 3 closed-form thresholds, 6 validity gaps, 18 domain records, a 4-cell square with its decomposition, 18 attractive readings, 2 convergence records, 10 reproductions, 6 falsifiers; **28 gate-named tests** in `electrostatics.PlanarCouplingWallTest` |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`, `l_B = 0.7141066 nm`, `ε_r = 78`; `q = 2` (`Mg²⁺`); 2 mM `MgCl₂` (2:1, `I = 3c`); the §3 tile, 40 × 40 × 10 nm at 2.6 nm honeycomb pitch; gaps 5 / 7 / 10 nm |
| **Consumes** | [`C-0137`](C-0137-beyond-mean-field-gap.md) (which raised the question; its boundary-channel margin ratios **read from its result file at run time**), [`C-0005`](C-0005-mean-field-screening-validity.md) (the coupling parameters, the saturated charge and the standing band structure, **read as literals and reproduced**), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the **sign** of the gap force, which is what selects the branch), [`gpd/data/T-50-beyond-mean-field-literature.md`](../data/T-50-beyond-mean-field-literature.md) rows 25–30 |
| **Raises** | [`CH-0178`](../challenges/CH-0178-a-criterion-with-no-root-returned-its-own-bracket-floor.md) against `ChargedSurface.loopExpansionValidityGap` and the `T-6` field it emitted, [`CH-0179`](../challenges/CH-0179-exponentially-large-is-not-a-property-of-the-branch.md) against `C-0137` §`P4` and `BeyondMeanFieldGap.kt`'s KDoc |
| **Answers** | `C-0137`'s own §`P4` *"queued, not answered"*, and the `T-221` row of `TASKS.md` |

---

## THE CONVENTIONS — read these before any number below

- `σ` is always a **magnitude** in `e/nm²`; `q = 2`; `l_B = 0.7141066 nm`.
- `μ ≡ μ_GC = 1/(2π q l_B σ)` in nm; `Ξ = q² l_B/μ = 2π q³ l_B² σ`; `D̃ = D/μ`.
- **Kanduč's `σ₁` is the LARGER-magnitude wall**, by his own Eq. (3) (`σ₁ + σ₂ < 0`, `σ₂ > σ₁`), and `ζ = σ₂/σ₁`. Oppositely charged walls are `−1 < ζ < 0`; `ζ = −1` is excluded because it leaves no counterions at all.
- **Eq. (64) is the criterion where the mean-field pressure is REPULSIVE and Eq. (65) where it is ATTRACTIVE.** Which one holds is set by the **sign of `p₀`**, not by the wall.
- A criterion is quoted **with its wall and with its gap**.

---

## The claim, in one line

**`Ξ` and `D̃` are both linear in the wall's charge density, so `Ξ/D̃ = q²l_B/D = 0.408061 nm⁻¹` carries no wall convention at all and Kanduč Eq. (64) is EXACTLY EQUIVALENT to `ln(D/μ) < D/(q²l_B)` — one closed form in which the whole disputed factor appears once, as `ln 16.495 = 2.8031`. Swept as a `2 × 2`, the verdict is constant down the charge column and not across the geometry row: all four BARE readings of the Gen-1 gap-facing wall fail at 5, 7 and 10 nm (`Ξ = 24.00 / 57.99 / 85.85 / 171.70`) and both RENORMALISED ones pass (`2.8564 / 1.4548`), because the criterion at 7 nm admits `Ξ < 4.7317`, i.e. `σ < 0.18459 e/nm²`, and every bare reading is above that number and every renormalised one below it. The rule is therefore BARE — on four properties of Kanduč's own derivation, of which the decisive one is that his Eq. (14) fixes the counterion count by ELECTRONEUTRALITY against `σ₁ + σ₂`, so a renormalised `σ` deletes from the slit exactly the population `Ξ` is counting — and within the bare family the geometry is a `3.58×` BRACKET the verdict is invariant across. But Eq. (64) is the REPULSIVE branch and this device's gap force is an ATTRACTION at every operating state, so what is owed is Eq. (65); over its own domain — attraction needs `D̃ > (1+ζ)/|ζ|`, derived here from Eq. (18) in the vanishing-`α` limit — its bound is minimised AT the branch boundary, sits at `0.94–1.15×` Eq. (64)'s, and excludes only a sliver `0.23–1.10 %` wide next to the `p₀ = 0` locus where the paper says NEITHER criterion applies. And no verdict moves either way: `C-0137` already measured a `16×` sweep of the same effective wall charge as `1.44 %` of `C-0017`'s stability margin.**

---

## `P1` — the cheap bound, and it is one line of algebra

`Ξ = 2π q³ l_B² σ` and `D̃ = 2π q l_B σ D`. Their ratio is

&nbsp;&nbsp;&nbsp;&nbsp;`Ξ/D̃ = q² l_B/D`,

a property of the **gap** and of nothing else. Measured over all six walls and all three gaps, the largest departure from that identity is `2.2e−16`, below the file's own `1e−14` emission floor.

So `Ξ ln D̃ < D̃` is **exactly equivalent** to

&nbsp;&nbsp;&nbsp;&nbsp;**`ln(D/μ) < D/(q² l_B)`**,

with `q² l_B = 2.85643 nm` — the separation at which two `Mg²⁺` ions interact with `k_BT`, and the only material quantity that survives. At `D = 7 nm` the right-hand side is `2.45064`.

**The whole wall convention enters as `ln σ`, once.** The `16.495×` between the bare duplex cylinder and the saturated face is `ln 16.495 = 2.8031` in the criterion's own variable — the naive statement overstates the disagreement by `5.88×` — against a distance to threshold of `1.6237` (bare, failing) and `1.1794` (saturated, passing). **It is smaller than it looks and it still flips the verdict.**

### The threshold, closed form

`μ*(D) = D e^{−D/(q²l_B)}`, `Ξ*(D) = (q²l_B/D) e^{D/(q²l_B)}`, `σ*(D) = 1/(2π q l_B μ*)`, reproduced by an independent bisection to `5.6e−16`:

| gap | `μ*` | **`Ξ*`** | **`σ*`** |
|---|---|---|---|
| 5 nm | 0.868488 nm | **3.28896** | **0.128311 e/nm²** |
| **7 nm** | **0.603684 nm** | **4.73166** | **0.184594 e/nm²** |
| 10 nm | 0.301709 nm | **9.46749** | **0.369351 e/nm²** |

---

## `P2` — the `2 × 2` the question's own wording bundles

*"Bare duplex versus charge-saturated gap face"* moves **two** things at once. Swept as a square, at 7 nm:

| | **bare** | **renormalised** |
|---|---|---|
| **duplex cylinder** | `σ = 0.936206`, **`Ξ = 24.00`** — FAILS | Manning: `σ = 0.111436`, **`Ξ = 2.8564`** — PASSES |
| **smeared gap face** | Gauss `ρt/2`: `σ = 3.349288`, **`Ξ = 85.85`** — FAILS | saturated: `σ = 0.056756`, **`Ξ = 1.4548`** — PASSES |

with two further bare readings carried to bracket the geometry axis at both ends — the single-helix layer (`σ = 2.262443`, `Ξ = 57.99`) and the full projected density (`σ = 6.698576`, `Ξ = 171.70`), which `CLAUDE.md` explicitly refuses for `Ξ` and which is carried only as an end-stop.

> **The verdict is constant down the charge column at 5, 7 AND 10 nm, and not constant across the geometry row.** The geometry axis moves `Ξ` by `7.16×` end to end and moves **no** verdict.

In logarithms the total move is **`−2.8031`**, and the two orderings disagree on the **sign** of the geometry term — `+1.2747` geometry-first against `−0.6747` charge-first, interaction `−1.9495` — while agreeing on the total to `0.0`. `CLAUDE.md`'s *"a two-factor move has a TOTAL and an INTERACTION, never an X term and a Y term"*, holding again: the smeared face is **more** charged than the cylinder when both are bare and **less** charged when both are renormalised, so the split is not a decomposition and the total is the quotable number.

---

## `P3` — the rule, and its ground

> ### Clause 1 — `Ξ` and `μ` are owed at the BARE charge, never at a renormalised one.

Four properties of Kanduč's own derivation, not a preference:

1. **Electroneutrality.** His Eq. (14) fixes the counterion normalisation `λ₀` from `σ₁ + σ₂`, and §II states the model in as many words: *"the charge of both bounding surfaces is compensated by mobile counterions … distributed in between the two surfaces. We thus neglect all coions."* **A renormalised `σ` deletes from the slit exactly the counterion population whose correlations `Ξ` counts.** You cannot concede that part of the population is strongly coupled — which is what a renormalisation is — and then use the remainder to certify that the whole is weakly coupled.
2. **`μ` is a Gauss's-law quantity.** His Eq. (6) defines it as the distance at which a counterion interacts with **the wall** at `k_BT`. That is a near-field statement about a bare surface charge, not a far-field fitting amplitude.
3. **The contact-value theorem.** `ρ(0) = 2π l_B σ²` is **exact beyond mean field** (Naji, after Eq. 9 — the repository's own `ChargedSurface.contactDensity` KDoc already says so) and is written on the bare `σ`. A `σ` that does not reproduce the exact contact density is not the `σ` these variables are defined with.
4. **Circularity.** The saturated `σ_eff = κ/(π l_B q)` is the *nonlinear-Poisson-Boltzmann* far-field amplitude and the Manning fraction is a mean-field two-state association model. Using either as the **input** to a mean-field validity criterion assumes the answer. `CLAUDE.md`'s *"saturation can only price a surface's MAGNITUDE, never its EXISTENCE"* applies here to the criterion's own input.

> ### Clause 2 — within the bare family the geometry is a BRACKET, not a choice.

The Gen-1 tile is **not the model's wall**: Kanduč's `σ₁` sits in a delta sheet at the contact plane (his Eq. 1), and this tile's charge is distributed through 10 nm of duplex lattice. So neither end of the geometry axis is the model's object:

- the **duplex cylinder** (`Ξ = 24.00`) is what a **contact** counterion sits on, at `μ = 0.11903 nm`;
- the **Gauss-partitioned gap face** `σ_face = ρt/2` (`Ξ = 85.85`) is what the slit's field integrates to.

They differ by `3.58×` and **the verdict is invariant across the bracket at 5, 7 and 10 nm.** Carry both; quote the cylinder as the optimistic end — **which is exactly what `CLAUDE.md`'s standing entry already does, and it needs no change.** That entry is about *projected versus cylinder*, i.e. about which end of the bare family to quote; nothing in it is disturbed, and its ground (the far field is where PB works anyway) is the same ground as clause 2's.

### The corpus had already chosen, and the straddle was never symmetric

**Kanduč Eq. (64) is the same closed form as Naji Eq. (20)**, which this repository has implemented as `ChargedSurface.loopExpansionValidityGap` since `T-6` and **emits in `T-6`'s own `surfaces` table at the bare duplex wall** — `13.5176976 nm`, reproduced here to `0.0`. `C-0005`'s whole band structure **is** that evaluation:

> *"band_C_controlled: gap > 12.91 nm (Naji Eq. 20 closed form: 13.52 nm)"*, and *"THE ENTIRE 5-10 nm GEN-1 WORKING RANGE IS IN THIS BAND"* (`band_B_uncontrolled`).

And `C-0005`'s headline `123–214 %` one-loop deviation reproduces **at the bare wall** to `1.4e−3 – 4.7e−3` at all five of its gaps. At the saturated wall the same five gaps would read `UNDEFINED / 0.8741 / 0.6954 / 0.5096 / 0.4044` — the 5 nm entry is not defined at all, because `D̃ = 2.55` there falls below Naji Eq. (19)'s own domain of `π` — and the four that are defined run `40–87 %`.

> **So adopting the saturated reading requires RETRACTING `C-0005`'s `123–214 %`**, which is the number this corpus's entire beyond-mean-field exposure — `CH-0019`, `C-0137`, `CH-0167` — is written on. The two readings were never on an equal footing.

### And the passing reading is outside the criterion's own domain

Eq. (64) is derived *"by employing the closed-form expressions obtained for large separations `D̃ ≫ 1`"*. Three independent domain checks, all free:

- `D̃/ln D̃` has a **global minimum of `e` at `D̃ = e`**. The saturated wall sits at `D̃ = 3.5652`, and the bound it passes against, `2.8045`, is **3.17 % above the smallest value that expression can ever take**.
- `ln D̃` is `1.2712` (saturated) and `1.9459` (Manning) — **order one, not large** — against `58.8` and `210.4` for the bare readings.
- At the 5 nm gap the saturated wall's `D̃ = 2.5466` falls **below `π`**, where Naji's one-loop coefficient changes sign and is not defined at all.

**The reading that fails is inside the criterion's domain; the reading that passes is outside it.**

---

## `P4` — the branch this device is actually on

Eq. (64) is derived for `p₀ > 0`. The Gen-1 walls are **oppositely charged** and `C-0008` solves the gap force as an **attraction** at every operating state — it *is* the actuation force — so `p₀ < 0` and what is owed is Kanduč **Eq. (65)**, `Ξ < (ζ²/|f(ζ)|) e^{−2ζD̃}`.

`C-0137` §`P4` and `BeyondMeanFieldGap.kt`'s KDoc both dispose of it in one clause — *"the right hand side here is exponentially large"* — and neither evaluates it. **The exponential is at fixed interior `ζ`, and this programme has never measured `ζ`.** So it is evaluated here.

### The branch boundary, derived rather than quoted

From Eq. (18), `tan(2αa) = α(ζ+1)μ/(α²μ² − ζ)`, in the `α → 0` limit where `p̃₀ = α̃² → 0`, both sides are linear in `α` and the equality fixes

&nbsp;&nbsp;&nbsp;&nbsp;**`D̃* = (1+ζ)/|ζ|`**, equivalently attraction ⟺ `|ζ|(1 + D̃) > 1`.

Asserted as a gate against Eq. (18) itself: the residual vanishes faster than `α²` as `α → 0` at `ζ = −0.5, −0.25, −0.1`.

### The bound over the branch, at 7 nm

`f(ζ)` is Eq. (61)/(62); the two forms are each other's analytic continuation (`arctan(iy)/(iy) = artanh(y)/y`) and their continuity across `ζ = −√2/2` is the gate that says the transcription is right. The bound diverges at **both** ends of the branch — as `ζ → 0⁻` through the prefactor and as `ζ → −1⁻` because `f → 0` — so it has an infimum, and the infimum is attained **at the branch boundary**:

| wall | `D̃` | `Ξ` | branch boundary `ζ` | Eq. (65) threshold `ζ` | **excluded sliver** | `inf`/Eq. (64) |
|---|---|---|---|---|---|---|
| duplex cylinder, bare | 58.809 | 23.998 | −0.016720 | −0.021618 | **0.4981 %** | 1.1282 |
| single-helix layer, bare | 142.118 | 57.993 | −0.006987 | −0.010009 | **0.3043 %** | 1.1386 |
| Gauss-partitioned face, bare | 210.389 | 85.852 | −0.004731 | −0.007029 | **0.2309 %** | 1.1431 |
| Manning cylinder | 7.000 | 2.856 | −0.125000 | none | **0** | 1.0946 |
| saturated face | 3.565 | 1.455 | −0.219050 | none | **0** | 1.0303 |
| projected, bare | 420.778 | 171.703 | −0.002371 | −0.003724 | **0.1356 %** | 1.1507 |

> **Over all 18 readings the infimum of Eq. (65) sits at `0.9447` to `1.1543` of Eq. (64)'s bound.** The two criteria agree to within a sixth where continuity across `p₀ = 0` requires them to. *"Exponentially large"* is not an operative statement about this device — see [`CH-0179`](../challenges/CH-0179-exponentially-large-is-not-a-property-of-the-branch.md), which also records that the KDoc's *"the criterion below is the conservative one here"* is true at the bare readings (`1.13–1.15×`) and **false** at the renormalised ones (`0.94–1.03×`).

**So on the branch this device occupies, the `16.495×` wall disagreement is worth under one and a fifth per cent of an asymmetry range**, and the excluded sliver sits adjacent to the `p₀ = 0` locus where the paper is explicit that neither criterion applies: *"the leading order term is zero and the fluctuations are dominant at any finite value of Ξ. The convergence of the loop expansion has to be determined in this case by evaluating the higher order terms which we shall not consider in this paper."* At `ζ = −0.5` **every** reading passes.

### The wall does constrain something, and it runs the other way

Attraction requires `|ζ| > 1/(1 + D̃)`: at the 7 nm gap that is **0.01672** at the bare duplex wall and **0.21905** at the saturated one — a factor of **13.10**. Since the gap force **is** an attraction, the reading with the smaller `D̃` makes the stronger implicit claim about how charged the electrode must be. **The bare rule is the permissive one here** — the opposite direction from the one it takes on Eq. (64).

---

## `P5` — does any verdict move? No, and the reason is structural

**The criterion is a validity FLAG, not a term in any answer.** `C-0137` already measured what the same factor is worth where it *is* a term: a sweep of the effective wall charge over `16×` — which **contains** the `16.495×` this convention spans — moves `C-0017`'s stability margin by **at most `1.4399 %`**, read from `C-0137`'s own result file at run time.

> **The flag is maximally sensitive to exactly the factor the answer is insensitive to.**

So: `C-0017`'s 10 nm / 2 mM verdict stays **NOT EXCLUDED, never established**; `C-0005`'s bands stand as published; `CLAUDE.md`'s cylinder entry stands. What changes is that the loop expansion is now **known** to be outside its own validity range at the actuated gap on the repulsive branch and inside it on the attractive one, rather than *queued*.

---

## `P6` — the ceiling and the threshold

`P-6`'s shape, for whoever wants to reopen it:

- The criterion would change verdict at the bare wall if `σ_face` fell below **`0.18459 e/nm²`** at 7 nm — an **`18.14×`** renormalisation of the tile's gap-facing charge, **deeper than Manning condensation under `Mg²⁺` delivers on a duplex** (`0.111436 e/nm²`, `8.40×`).
- Equivalently, the bare readings become controlled at gaps above **13.5177 nm** (duplex cylinder) and **17.9723 nm** (Gauss-partitioned face) — outside §3's 5–10 nm band, and inside the 17–26 nm tall-layer range `C-0110` examined and refused on reach.
- **No published `σ_eff` for an origami face in mM `Mg²⁺` was found, and none is needed**: both ends of the family are in print and the verdict is constant across each.

### A free identity, worth recording

**A Manning-renormalised cylinder's Gouy-Chapman length is EXACTLY the helix radius** — `μ_GC = R`, 1.0000 nm — and its coupling is exactly `q² l_B/R = 2.8564`, **carrying no rise, no linear charge density and no Bjerrum length**. Manning condensation is defined so that the surviving linear density is `1/(q l_B)` per nm, which is precisely what `μ = 1/(2π q l_B σ)` inverts. Asserted over three Bjerrum lengths, three valencies, two rises and two radii. **So the "renormalised duplex" reading contains no DNA chemistry at all, only a geometric radius** — one more reason it cannot be the input to a criterion about counterion correlations.

---

## Verification gates

1. **Dimensional** — `Ξ`, `D̃`, `ζ`, `f(ζ)` and every margin dimensionless; `μ`, `D`, `σ*` in nm and `e/nm²`; `q² l_B` asserted to be `4 l_B`.
2. **Limiting cases** — `Ξ/D̃` independent of `σ` to `< 1e−13` over four decades of `σ`; `D̃/ln D̃` minimal at `D̃ = e` with value `e`; `f(ζ)` continuous across `ζ = −√2/2` to `1e−5` and vanishing as `ζ → 0⁻`; `D̃*(ζ)` reproducing `1, 3, 7` at `ζ = −0.5, −0.25, −0.125`; `attractiveBranchAsymmetryCeiling` `→ −1` at `D̃ = 0` and `→ 0` at `D̃ = 1e6`.
3. **Symmetry and conservation** — the closed-form threshold reproduced by an independent bisection to `5.6e−16`; a wall exactly at `σ*` sitting exactly on the criterion to `1e−9`; the branch boundary asserted against Eq. (18) by the rate at which its residual vanishes; the Manning identity `μ_GC = R` asserted over 36 parameter combinations.
4. **Numerical convergence** — the `ζ` scan reproduces the closed-form infimum at 5 000 / 20 000 / 80 000 samples to `< 1e−12` (the boundary is in the grid at every count **and is the argmin**, which is separately asserted by checking that five interior points at `1e−6` to `0.5` of the way back are all above it).
5. **Literature cross-check** — `C-0005`'s five one-loop deviations reproduced to `1.4e−3 – 4.7e−3`; its `Ξ = 24.0` to `1e−4`; its saturated charge `0.0568 e/nm²` re-derived as `κ/(π l_B q)` to `7.8e−4`; `T-6`'s emitted `loopExpansionValidityGap` to `0.0`; `C-0137`'s two published bounds `14.43` and `2.80` to `2.9e−4` and `1.6e−3`.

---

## Declared falsifiers, and whether they fired

| | statement | fired | what it found |
|---|---|---|---|
| **`F1`** | the **geometry** axis flips Eq. (64)'s verdict at some candidate and gap | **no** | all four bare readings (`Ξ = 24.00, 57.99, 85.85, 171.70`) fail at 5, 7 and 10 nm and both renormalised readings pass at all three; `7.16×` in `Ξ`, zero verdicts |
| **`F2`** | the **charge-convention** axis does NOT flip the verdict | **no** | every bare reading fails and every renormalised one passes: the whole `16.495×` straddle is the renormalisation |
| **`F3`** | Eq. (65)'s infimum exceeds Eq. (64)'s bound by more than `2×`, i.e. *"exponentially large"* is operative | **no** | `0.9447` to `1.1543` over all 18 readings — and the sub-unity end is `CH-0179` |
| **`F4`** | `C-0005`'s `123–214 %` is NOT reproduced by the bare reading | **no** | reproduced to `1.4e−3 – 4.7e−3`, and `T-6`'s own emitted validity gap to `0.0` |
| **`F5`** | adopting the saturated reading moves `C-0017`'s margin by more than 2 % | **no** | `1.4399 %`, which is `C-0137`'s own published 1.44 % |
| **`F6`** | the renormalised readings sit inside Eq. (64)'s asymptotic domain `D̃ ≫ 1` | **no** | `D̃ = 3.5652` and `7.0000`, `ln D̃ = 1.2712` and `1.9459`; the saturated bound is 3.17 % above the smallest value `D̃/ln D̃` can take, and at 5 nm its `D̃ = 2.5466` is below Naji Eq. (19)'s own domain of `π` |

**All six were checked and none fired.** The one thing the task turned up that was not predicted is `CH-0178`, and it was found by reading an emitted field rather than by a falsifier.

---

## Validity range

- **TRL 1–3, nothing measured.** This settles which **input** a published inequality is owed. It does not compute a correlation correction and it does not make `Ξ = 17–24` tractable.
- **Kanduč et al. is COUNTERION-ONLY** — *"neglecting completely the effects of salt"* — so it has no Debye length. Every number here is a statement about **that model**, transferred to this gap on `C-0008`'s counterion-dominance finding. It is a **transfer**, which is why the deliverable is a rule and not a prediction.
- **The device's own `ζ` is NOT measured here.** What is measured is the criterion as a function of `ζ` over the whole admissible range and the width of the excluded sliver. The ratio of the electrode's charge to the tile's has never been computed in this corpus.
- **AT the branch boundary neither criterion applies**, by the paper's own words. The excluded slivers reported here sit adjacent to exactly that locus.
- `σ_face = ρt/2` is **Gauss's law on a uniformly charged slab**. The Gen-1 tile's face is corrugated at the interhelical pitch; the smearing is the criterion's own idealisation and is exact only for the exterior field far from the corrugation. That is why clause 2 is a bracket.
- The tile thickness used is §3's 10 nm, which `DnaOrigamiTile` carries with its own recorded inconsistency against *"single-layer honeycomb"*. **Both readings are carried as separate candidates and both fail.**
- The criterion is read at the **tile**, which is Kanduč's `σ₁` at the operating biases this programme uses. A bias making the electrode the larger-magnitude wall would put `σ₁` on the electrode, where a metal's charge has no bare/renormalised ambiguity and the question does not arise.

## Numbers that are cited rather than derived

| number | value | flag |
|---|---|---|
| `C-0005`'s `Ξ` at the bare duplex cylinder, `Mg²⁺` | 24.0 | **CITED and REPRODUCED to `1e−4`** |
| `C-0005`'s saturated far-field charge at 2 mM | 0.0568 e/nm² | **CITED and RE-DERIVED as `κ/(π l_B q)` to `7.8e−4`** |
| `C-0005`'s five one-loop deviations | 2.14 / 1.63 / 1.23 / 0.89 / 0.70 | **CITED and REPRODUCED to `1.4e−3 – 4.7e−3`** |
| `T-6`'s emitted `loopExpansionValidityGap`, bare duplex, `q = 2` | 13.517697558570946 nm | **CITED as a literal and REPRODUCED to `0.0`** |
| `C-0137`'s two Eq. (64) bounds at 7 nm | 14.43 and 2.80 | **CITED and REPRODUCED to `2.9e−4` and `1.6e−3`** |
| `C-0137`'s boundary-channel margin ratios | — | **READ from its result file at run time** |
| Kanduč Eqs. (3), (5), (6), (7), (14), (18), (61), (62), (64), (65) | — | **CITED formulas, READ DIRECTLY, DERIVED evaluations** |
| B-DNA rise 0.34 nm, duplex radius 1.0 nm, honeycomb pitch 2.6 nm | — | **CITED**, through `DnaOrigamiTile` |

Everything else — the scale-covariance identity, the closed-form thresholds, the `2 × 2`, the branch boundary `D̃* = (1+ζ)/|ζ|`, the Eq. (65) infimum and its slivers, and the Manning `μ_GC = R` identity — is derived here.

## Challenges

Two are raised **by** this claim: [`CH-0178`](../challenges/CH-0178-a-criterion-with-no-root-returned-its-own-bracket-floor.md) and [`CH-0179`](../challenges/CH-0179-exponentially-large-is-not-a-property-of-the-branch.md). None stands against it.
