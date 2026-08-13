# CH-0019 — There are two mean fields in this programme, and the coupling margin sits inside the other one

| | |
|---|---|
| **Challenges** | The standing rationale — recorded in `TASKS.md`'s `T-1f` row and repeated in `C-0011`'s "two largest exposures" framing — that bounding the **polymer** fluctuation correction is what decides whether `C-0017`'s 10 nm coupling margin survives |
| **Raised by** | [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md), task [`T-1f`](../tasks/T-1f-mean-field-fluctuation-corrections.md) |
| **Raised** | 2026-08-13, iteration 4 |
| **Status** | **Upheld. The rationale identifies two different expansions.** `C-0017`'s own text is *not* at fault — it names `C-0005` correctly and calls the correction electrostatic. What is challenged is the inference drawn from it in the queue. |

---

## The standing statement being challenged

`TASKS.md`, the `T-1f` row:

> **now the binding uncertainty in the programme**, promoted by `C-0017`: the 10 nm coupling margin
> is 1.19–1.42× and it sits *inside* `C-0005`'s 123–214 % one-loop correction, so the window's
> survival is **not excluded rather than established** until **this** is bounded.

The first clause is true. The second does not follow from it, because "this" is not the thing the
first clause is about.

## The two expansions, side by side

| | `C-0005`'s correction | `T-1f`'s correction |
|---|---|---|
| what is fluctuating | **ion positions** in a 2:1 electrolyte at a charged wall | **polymer concentration** in a neutral grafted layer |
| the expansion | the loop expansion of the Coulomb field theory whose saddle point *is* Poisson-Boltzmann | the loop expansion of the Edwards field theory whose saddle point *is* the self-consistent field |
| the parameter | `Ξ = q²l_B/μ_GC = 17–24`, and **`Ξ ∝ q³`** — the divalence does it | `Gz = √(v/(c b⁶))`, `Gi = (2√3/π)Gz = 0.44–1.30`, and it is **`N`-free** |
| where it is evaluated | the tile and electrode **surfaces**, and the gap between them | the **interior** of the PEG layer, at its own local `φ` |
| the term it corrects | **`k_es`** | **`k_brush`** |
| its size here | **123–214 %** of the leading term at 5–10 nm | **44–130 %** of the leading *interaction* term |
| who owns it | `C-0005`, `T-6`; unbounded, and no published theory exists in `1 < Ξ < 100` | `C-0019`, `T-1f`; unbounded perturbatively, bounded non-perturbatively |

They are the same *kind* of object — a loop parameter — and they are not the same object. Neither is
computable from the other, neither bounds the other, and they act on the two terms of

&nbsp;&nbsp;&nbsp;&nbsp;`k_eff = k_brush + k_es`

which `C-0012` and `C-0017` add. The similarity of the two numbers (123–214 % against 44–130 %) is a
coincidence of this material and this geometry and carries no information at all.

## Why it matters, quantitatively

At the 10 nm design point in 2 mM `MgCl₂`, `C-0017` reports `k_brush` = 11.7–35.6 pN/nm and
`k_es` = −59.6 to −38.7 pN/nm, giving a stability floor `|k_eff|` = 23.41–27.91 pN/nm against §3's
mandated 33.333 pN/nm — the 1.19–1.42× margin.

**The electrostatic term is the larger of the two and it carries the larger relative error.** So:

1. Bounding the polymer correction **cannot** close `C-0017`'s exposure. Even a total collapse of
   `k_brush` would move the floor by at most 35.6 pN/nm, and that is the *interaction* part of a
   quantity whose fluctuation bracket `C-0019` measures at a small fraction of it.
2. The polymer correction **is already inside** the bracket the margin is quoted over. `C-0017`'s
   six-model bracket is three interaction laws × two profile models, and **the interaction axis of
   that bracket is the fluctuation axis** — the two-body `φ²` limb is the mean-field end of the
   crossover and the des Cloizeaux `φ^{9/4}` limb is the fluctuation-renormalised end. So the
   margin already spans it.
3. What would actually move that margin is a beyond-mean-field treatment of the **divalent,
   oppositely charged, 2–7 nm gap** — which `C-0005` states plainly does not exist in the
   literature and which `C-0017` names in its own "still open" list as item 1.

## What follows, and what does not

**Does not follow.** That `T-1f` was not worth doing. It bounds a real exposure that `C-0011` named
and did not close, it moves a window edge, and it retires a sensitivity exponent (`CH-0020`,
`C-0019`'s falsifier 3).

**Does not follow.** That `C-0017` is wrong. Its validity range says *"mean-field electrostatics,
inherited whole"* and attributes the 123–214 % to `C-0005` explicitly. It is the queue's
paraphrase, not the claim, that fuses the two.

**Does follow.**

1. **`T-1f` does not unblock `C-0017`'s verdict, and no task in the current queue does.** The
   verdict stays **NOT EXCLUDED, never established** at 2 mM for a reason `T-1f` cannot touch.
2. **The recommendation `C-0017` already makes is the one that survives**: operate at
   `A2.2`'s 0.5 mM, where the margin is 2.09–8.65× and clears its own inherited uncertainty. That
   is now the *only* route to establishing the 10 nm point short of explicit-ion simulation.
3. **A new task is owed** — a beyond-mean-field treatment of the actuated gap, at `C-0005`'s own
   costing of 1–3 weeks of primitive-model Monte Carlo. It is the last unbounded exposure on the
   Gen-1 critical path and it should be queued under its own ID rather than left inside `T-1f`'s.
4. **Whenever a "one-loop correction" is quoted in this project it must name the field it is a
   loop expansion of.** Two now exist; a third — the fluctuation correction to the *tile's* elastic
   continuum — does not, and would be a third.

## If this challenge is itself wrong

The way it fails is if the two corrections are not in fact independent — if the polymer layer's
concentration fluctuations couple to the ion distribution strongly enough that one expansion
parameter controls both. `C-0005` and `C-0007` between them argue they do not: the layer is
electrically neutral, it has no zeta potential of its own, ideal mobile salt exerts *exactly* zero
osmotic pressure on it, and the solvent-quality channel through which the buffer could act on the
polymer is ≤ 0.4 % of the modulus over 2–10 mM. That is an argument from three standing claims, not
a calculation, and it is stated here as such.
