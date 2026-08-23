# CH-0004 — The bulk Debye length is not the decay length in the gap, and `|k_es| ≈ F_es/λ_D` understates the softening

| | |
|---|---|
| **Challenges** | §1 of [the problem definition](../../third-party/2026-08-ndi-gen1-problem-definition.md) — the stiffness estimate `\|k_es\| ≈ F_es/λ_D` "for exponential screening" — and §3's `λ_D ≈ 4 nm` **as used downstream**, which `TASKS.md` carries into `T-3` and `T-4` |
| **Raised by** | [`C-0005`](../claims/C-0005-mean-field-screening-validity.md), task [`T-6`](../tasks/T-6-mean-field-screening-validity.md) |
| **Raised** | 2026-08-12, iteration 3 |
| **Status** | ~~**OPEN** — filed against an *inherited premise*, not against a derived claim. `T-4` is the task that decides it.~~ **RESOLVED** by [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (`T-3a`) — **upheld in its consequence, refuted in its magnitude, and its own escape clause is what fired.** The force's decay length in the gap is a *fourth* number, 1.8–2.8 nm at the working gap and the only bias-dependent one; the counterion length this challenge proposed, 0.84–1.18 nm, is 2.4–3.4× too short and is never approached at any gap or bias. The task that decided it was `T-3a` and not `T-4`, which became [`C-0018`](../claims/C-0018-maximum-usable-bias.md) |

---

## Why this is filed as a challenge rather than a note

`C-0001` and `C-0002` are not contradicted by `T-6`; nothing in `gpd/claims/` is.
What is contradicted is a **premise of the problem definition itself** that no task has yet consumed but that
`T-3` and `T-4` are queued to consume, and §2 of the problem definition explicitly invites exactly this:

> Stated so you know our prior, not to constrain you. If the right answer is that this framework is
> inapplicable, that is a useful result.

Filing it as a challenge rather than a remark is the point of the no-overwrite rule: `T-4` must meet it
before it inherits the estimate, not after.

## The standing statement being challenged

§1 of the problem definition:

> Effective stiffness is `k_eff(z, V) = k_brush(z) + k_es(z, V)`, with `k_es < 0` and
> **`|k_es| ≈ F_es/λ_D` for exponential screening.**

and §3:

> | Debye length | ~4 nm at 2 mM Mg²⁺ |

The sign convention is **not** challenged and is upheld: `k_es < 0`, and `T-6` restates it.
What is challenged is the *magnitude*, through the length that sets it.

## The contradicting result

`λ_D = 3.927 nm` at 2 mM MgCl₂ is **correct as a bulk-reservoir quantity** — `T-6` re-derives it from the 2:1
stoichiometry and confirms §3 to 1.8%. That is not the issue.

The issue is that **it is not the concentration in the gap.**

Counting ions rather than assuming them (`gpd/results/T-6-mean-field-screening-validity.json`, `gaps[]`):

| buffer | gap | counterions the tile's charge requires | `Mg²⁺` the bulk supplies | ratio |
|---|---|---|---|---|
| 2 mM | 5 nm | 319 | 9.6 | **33 : 1** |
| 2 mM | 10 nm | 319 | 19.3 | **17 : 1** |
| 10 mM | 5 nm | 319 | 48.2 | 6.6 : 1 |
| 10 mM | 10 nm | 319 | 96.4 | **3.3 : 1** |

Even taking only the *Manning-renormalised* charge (11.9% of bare) and only the half that faces the gap,
the tile drags in between 3 and 33 times more `Mg²⁺` than the bulk buffer would put there.
The gap is **counterion-dominated everywhere in the §3 box.**

The Debye-Hückel screening length of that counterion population — computed at the *uniform* density, which
makes it an **upper** bound, since the counterions are concentrated near the surfaces where they screen harder —
is **0.836 / 0.989 / 1.182 nm at gaps of 5 / 7 / 10 nm**, independent of the buffer.

**That is 3.3× to 4.7× shorter than the bulk `λ_D` the estimate uses.**

## Methodological grounds

Three, in increasing order of consequence.

### 1. The wrong reservoir was used to set the local ion concentration

`λ_D` is a property of a **bulk electrolyte at its own composition**. A gap bounded by a surface carrying
1276 effective charges is not at bulk composition; its ion content is fixed by electroneutrality with respect
to that surface, not by equilibrium with a reservoir that is three orders of magnitude more dilute.
This is the same class of error as `CH-0001`'s: a quantity was taken from the bulk-solution literature and
applied to a confined, surface-dominated region without checking that the region is in the regime the quantity
describes. Here it is checkable by counting, and the count fails.

### 2. The decay is not exponential in the regime where the force is largest

§1 says "for exponential screening", which is a conditional the estimate quietly drops. In a
counterion-dominated gap the mean-field pressure is the **mid-plane ideal-gas pressure**, which falls
**algebraically** — Naji et al. Eq. (14) gives `P ∝ Δ⁻²` at large separation, not `e^{−κΔ}`.
Exponential screening is recovered only once the potential everywhere falls below `k_BT/(ze) = 12.9 mV`
for `Mg²⁺`, and the saturated apparent surface potential of a strongly charged wall is `4k_BT/(ze) = 51.7 mV`,
four times that. So the exponential regime begins **outside** the double layer, not inside the gap.

An algebraic `Δ⁻²` decay has `|k_es| = 2F_es/Δ`, i.e. a *logarithmic-derivative length* of `Δ/2 = 2.5–5 nm`,
which happens to be numerically comparable to `λ_D` — this is why the error has not been visible.
But the two coincide by accident at one gap and diverge everywhere else, and they scale differently:
`Δ/2` scales with the gap, `λ_D` does not.

**Caveat, stated because it matters:** the `Δ⁻²` law is the *like-charge* two-wall result, and under bias the
tile and electrode are *oppositely* charged, for which no equally clean closed form is quoted here.
The transferable part of this ground is the weaker but sufficient statement: **the conditional "for exponential
screening" is not satisfied in the gap**, because the potential there does not fall below `k_BT/(ze)` anywhere
between the surfaces. Whatever the correct decay is, it is not `e^{−κΔ}` with the bulk `κ`, and §1's estimate
assumes it is.

### 3. The consequence runs in the dangerous direction for `T-4`

`|k_es| ≈ F_es/ℓ` with a **shorter** `ℓ` is a **larger** negative stiffness.
Replacing 3.93 nm by 0.84–1.18 nm multiplies `|k_es|` by **3.3× to 4.7×** at the same electrostatic force.

`T-4`'s question is whether `k_eff = k_brush + k_es` reaches zero — i.e. pull-in.
Understating `|k_es|` by a factor of three to five understates the pull-in risk by the same factor and
would place the maximum usable bias several times too high. This is the one direction in which an error here
is not conservative, and it is why the challenge is filed rather than noted.

## What follows, and what does not

**Does not follow.**

- That §3's `λ_D` is wrong. It is right, and it is now derived rather than cited. It is the correct length for
  the **far field**, outside the double layers — which is where a charge sensor sits, so `A7.1`/`A7.2`/`A7.4`'s
  sensed-charge attenuation is *not* affected by this challenge.
- That the sign convention of §1 is wrong. `k_es < 0` stands and is restated in `T-6`.
- That a specific corrected `|k_es|` is hereby established. `T-6` computes a *screening length*, not a force,
  and computing the force is outside its acceptance predicate. The factor of 3.3–4.7 is a factor **on the
  length**, and it propagates to `|k_es|` only if the `F_es/ℓ` form is retained at all — which ground 2 says
  it should not be.

**Does follow.**

1. **`T-3` must not use `exp(−h/λ_D)` with `λ_D = 4 nm`** for the tile-electrode force at the working gap.
2. **`T-4` must not inherit `|k_es| ≈ F_es/λ_D`** with the bulk `λ_D`. It should differentiate whatever
   pressure law it adopts, rather than dividing a force by a length.
3. **The bulk `λ_D` remains the right length for the sensor question** and for anything outside the gap.
4. The distinction has to be carried explicitly, because "the Debye length" now means two different numbers in
   this project depending on where it is evaluated — 3.93 nm in bulk, ~1 nm in the gap, and 4.5–5.5 nm inside
   the polymer layer (`C-0005`, §4(c)). All three are correct in their own place.

## If this challenge is itself wrong

The way it fails is that the **uniform-density estimate of the local screening length is too crude**.
The counterions are not uniform; they sit in a Gouy-Chapman/Manning layer within `μ_GC = 0.12 nm` of the
phosphates, largely *inside* the origami's own interstices rather than in the open gap. If most of the 319
counterions are in fact sequestered at the tile's underside rather than distributed through the gap, then the
*mid-gap* region could be closer to bulk composition than the count suggests, and the bulk `λ_D` would be
approximately right for the mid-gap decay after all.

That is a real possibility and `T-6` does not settle it: settling it needs the actual PB profile in the gap,
which is a one-dimensional nonlinear PB solve — **minutes of work, not days** — and which is the natural first
piece of `T-3`. The challenge is filed at the level of "the premise has not been checked and the check is
cheap", which is exactly where `CH-0001` was filed and for the same reason.

What would *not* rescue the estimate under either outcome is ground 2: the decay in a counterion-dominated
gap is algebraic rather than exponential regardless of how the counterions are distributed within it.

## Resolution

**Open.** `T-4` is the task that decides it, and `T-3`'s one-dimensional PB profile is the cheap instrument.
Until then, `T-3` and `T-4` must state which length they are using and why.
