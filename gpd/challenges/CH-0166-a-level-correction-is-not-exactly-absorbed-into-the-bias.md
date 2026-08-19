# CH-0166 — A level correction is absorbed into the bias, and the bias moves the decay length

| | |
|---|---|
| **Challenges** | [`CH-0035`](CH-0035-the-edge-correction-cannot-reach-the-window-edge.md)'s Ground 2, restated in [`C-0033`](../claims/C-0033-collar-on-the-equilibrium-path.md) and inherited by `C-0027`, `C-0091` and `TASKS.md`: *"a multiplier `μ(h)` on the LEVEL of `\|F_es\|` is absorbed entirely into the bias and reaches `k_es` not at all. It is not small; it is exactly zero at the operating point."* |
| **Raised by** | [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md), task [`T-50`](../tasks/T-50-beyond-mean-field-gap.md), falsifier `F2` |
| **Raised** | 2026-08-19, iteration 32 |
| **Status** | **Open. The direction is right, the word *exactly* is not, and the residual is favourable.** No verdict of `C-0033`, `CH-0035` or `C-0027` reverses; one number of `C-0033`'s own decomposition is understated. |

---

## The statement being challenged

`CH-0035` Ground 2 derives, correctly, that `k_es = −|F_es|/ℓ` identically and that the operating point is defined by a force balance, so `|F_es|` is pinned. It concludes:

> **"So a multiplier `μ(h)` on the LEVEL of `|F_es|` is absorbed entirely into the bias and reaches `k_es` not at all.** It is not small; it is exactly zero at the operating point."

`C-0033` restates it as an identity — *"a multiplier on a pinned force moves only the decay rate, `1/ℓ → 1/ℓ − d ln μ/dh`"* — and builds its whole level/gradient decomposition on it, as does `C-0137` itself.

## The premise that is not in the identity

`k_es(h, V) = |F_es(h,V)| · ∂ln|F_es|/∂h |_V`. Pinning `|F_es|` fixes the **first** factor. The second is evaluated **at the bias that delivers the pinned force**, and that bias moves when the level does:

&nbsp;&nbsp;&nbsp;&nbsp;`μ₀ |F_PB(h, V*)| = F_pin` ⟹ `V*` depends on `μ₀`,
&nbsp;&nbsp;&nbsp;&nbsp;`k_es = −F_pin/ℓ_PB(h, V*)`.

**The absorption is exact if and only if `ℓ` is bias-independent.** It is not, and it is not nearly so, because the gap is counterion-dominated (`C-0008`, `C-0110`: 6.4–38.9× at 17–26 nm, 33:1 at 5 nm) and the counterion content is set by the bias. As `V → 0` the profile relaxes toward the bulk Debye length; as `V` rises the counterion accumulation shortens `ℓ`.

## Measured, at `C-0017`'s own binding state

10 nm layer, `σ = 0.024 nm⁻²`, 2 mM `MgCl₂`, held at 7 nm, `F_pin = 143.922 pN`, `k_brush = 22.376 pN/nm`. `C-0008`'s pipeline, the bias re-solved by bisection at each level so the pinned force is delivered:

| level `μ₀` | required bias | **`ℓ`** | floor | margin | leak |
|---|---|---|---|---|---|
| `1/2.23` | 0.7407 V | **2.3227 nm** | 39.587 | 0.8420 | **+11.674 pN/nm** |
| **1.000** | 0.1568 V | **2.8621 nm** | 27.909 | 1.1943 | `−0.0038` |
| `2.23` | 0.0784 V | **3.2264 nm** | 22.232 | 1.4993 | `−5.681` |
| `3.14` | 0.0592 V | **3.2922 nm** | 21.340 | 1.5620 | `−6.573` |

`ℓ` runs **2.2593 → 3.3158 nm** over the ladder — a `1.47×` spread — and the leak reaches **12.585 pN/nm**, which is **2.88×** the gradient threshold `T-50` is written on. The slope is **`−3.61` to `−8.70 pN/nm` per e-fold of level** across the six `C-0003` models.

The null multiplier leaks `3.83e−3 pN/nm`, which is this study's reproduction of `C-0017`'s own floor from its force balance and the scale at which the table should be read.

## What it costs `C-0033`, and it costs it in the favourable direction

`C-0033`'s collar multiplier is `μ = 1.1063` at the 7 nm held gap in 2 mM. On the measured slope at that state (`−6.264 pN/nm` per e-fold) the level channel is worth

&nbsp;&nbsp;&nbsp;&nbsp;`ln(1.1063) × (−6.264) = −0.633 pN/nm`

of floor — taking `C-0017`'s worst 2 mM floor from `27.913` to `27.280` and its margin from `1.1942` to **`1.2219`**, `+2.3 %`. `C-0033` and `CH-0035` recorded that as **exactly zero**.

*(That is arithmetic on `C-0033`'s published `μ` and `C-0137`'s measured slope, not a re-solve of `C-0033`'s equilibrium path. The sign is what matters and it is not in doubt: the floor is monotone decreasing in the level at all six models.)*

## What follows, and what does not

**Does not follow.** That any verdict moves. Every leak measured is favourable for an enhancement, `C-0033`'s collar is an enhancement, and `C-0137`'s own ceiling argument is *strengthened* by the correction, not weakened.

**Does not follow.** That the level/gradient decomposition is wrong. It is the right decomposition and it is what makes `T-50` answerable at all. What is wrong is one word in one sentence.

**Does follow.**

1. **The correct statement is:** *at a force-pinned operating point a level correction reaches the stiffness only through the bias-dependence of the decay length, which is `−3.6` to `−8.7 pN/nm` per e-fold of level at the Gen-1 10 nm / 2 mM state, and it is favourable for an enhancement.* The word **exactly** should be struck wherever it appears.
2. **`C-0033`'s three-variant decomposition has a fourth variant it did not run.** Its `μ ≡ μ(h_fold)` "level only" variant holds the bias fixed and therefore measures zero by construction; the variant that re-solves the bias measures this.
3. **A quantity is not well posed without the state it is read at — and here the state is a BIAS.** `ℓ` is quoted throughout the corpus as a function of gap and buffer; it is a function of the bias too, `1.47×` over this ladder.

## If this challenge is itself wrong

The way it fails is if the operating point is *not* re-solved when the level changes — if the device is driven at a fixed bias rather than held against a load line. `CH-0035` itself names that case (its item 2) and there the level reaches the answer **in full**, which is a larger effect in the same direction, not a smaller one. There is no reading in which the residual is exactly zero.
