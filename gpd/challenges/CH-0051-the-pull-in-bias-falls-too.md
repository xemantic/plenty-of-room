# CH-0051 — The operating bias is not the only bias the collar lowers, so `≥ 1.108–1.134` is not a lower bound

| | |
|---|---|
| **Against** | [`C-0027`](../claims/C-0027-window-resynthesis.md) — its **pull-in propagation** (`marginLowerBound`, and the framing "the operating bias falls, unambiguously"), not its window, its axes, its coupling margins or its decomposition |
| **Raised by** | [`C-0033`](../claims/C-0033-collar-on-the-equilibrium-path.md) (`T-60`) |
| **Date** | 2026-08-14 |
| **Grounds** | methodological — a **ratio** of two biases corrected in one of its two arguments, with the other held at the value the correction was applied to change |
| **Direction** | **unfavourable at 10 nm / 2 mM** (1.108–1.134 claimed, 1.021–1.028 solved) and **unfavourable in sign at 7 nm / 10 mM**, where the same correction makes the margin *worse* |
| **Status** | raised. `C-0027`'s own verdict — *"read `C-0018`'s 1.007–1.032 as standing, its movement unresolved"* — is **upheld and superseded**; what is challenged is the number it offered beside that verdict |

---

## What is challenged

`C-0027` §"The pull-in margin, and the one thing this synthesis cannot resolve" states:

> 1. **The operating bias falls, unambiguously** — the enhanced force reaches the pinned target at a bias
>    8–9 % lower, through `T-16`'s own measured `dV/dF`. **At unchanged pull-in bias that raises the margin to
>    ≥ 1.108 – 1.134.**

and emits that number per model as `pullInBounds[].marginLowerBound`.

**The 8–9 % is reproduced here and is right.** `T-60` re-solves the operating bias with the collar carried and
finds it falls **8.5 % to 9.9 %** at the six models — an independent confirmation of a transfer `C-0027` made
through a measured slope rather than a re-solve.

**What is challenged is the clause "at unchanged pull-in bias".** The pull-in bias is a bias on the *same*
equilibrium path, produced by the *same* field, and the same multiplier lowers it too.

---

## Ground 1 — the pull-in bias falls by nearly as much, so a ratio's numerator was frozen

`T-60` re-locates the fold through a collar-corrected field at the two models where pull-in still binds:

| model, 10 nm / 2 mM | `V*` | pull-in | **margin** | `V*` | pull-in | **margin** |
|---|---|---|---|---|---|---|
| | *published (`μ ≡ 1`)* | | | *solved (`μ(h)`)* | | |
| alexander-box(two-body) | 0.15680 | 0.15791 | 1.0071 | 0.14242 | **0.14536** | **1.0207** |
| strong-stretching(two-body) | 0.12831 | 0.12996 | 1.0129 | 0.11745 | **0.12072** | **1.0278** |

`V*` falls 9.2 % and 8.5 %; **the pull-in bias falls 8.0 % and 7.1 %**. The margin is the ratio, so almost the
whole of `C-0027`'s 10 % improvement cancels: the solved margin is **1.021–1.028**, not `≥ 1.108–1.134`.

**The margin does still rise** — by **1.3–1.5 %** rather than 10 % — and `C-0033` reports it as such. The claim
that is wrong is only that 1.108 is a *floor*.

## Ground 2 — the direction is not a property of the correction, and `C-0027` could not have known that

The margin is `V_pullin/V*`, and those two biases are read at **two different gaps**: the fold gap and the
operating gap. A multiplier `μ(h)` lowers each by roughly `1/√μ` at its own gap, so

&nbsp;&nbsp;&nbsp;&nbsp;**the pull-in margin moves with the sign of `μ(h_fold) − μ(h_operating)`,
i.e. with the sign of `3 nm − s_fold`.**

At 10 nm / 2 mM the fold sits at a stroke of 3.4–4.1 nm, *deeper* than §3's 3 nm, so `V*` falls more and the
margin rises. At **7 nm / 10 mM** the fold sits at 1.9–2.7 nm, *shallower*, and every sign reverses:

| model, 7 nm / 10 mM | margin `μ ≡ 1` | **margin `μ(h)`** | move |
|---|---|---|---|
| alexander-box(two-body) | 1.1138 | **1.0868** | **−2.4 %** |
| alexander-box(virial) | 1.0605 | **1.0311** | **−2.8 %** |
| alexander-box(des-Cloizeaux) | 1.1267 | **1.0873** | **−3.5 %** |
| strong-stretching(two-body) | 1.0216 | **1.0125** | **−0.9 %** |

**A one-signed correction to a force does not give a one-signed correction to a bias margin**, and
`C-0027`'s "unambiguously" is a statement about `V*` alone that does not survive being made about the ratio.
These five states are exactly `T-62`'s — the ones `C-0027` could not reach because `T-16` swept
0.5/1/2 mM and `T-4` swept 0.5/2/10, so no coupling record existed at 10 mM to take `dV/dF` from. `T-60`
re-solves rather than transfers, and does not need one.

## Ground 3 — at four of six models pull-in stops binding altogether, so the margin is not a pull-in margin

Carrying the collar moves every fold to a deeper stroke (the collar-only tangent at `C-0018`'s own fold is
`+2.60` to `+4.99 pN/nm`, strictly positive because at a pinned force it is exactly `|F_es| d ln μ/dh`). At
four of the six 10 nm / 2 mM models it moves deep enough that the path meets `C-0002`'s `φ = 0.2` crossover
**first**, and beyond that the branch rises monotonically until the field can no longer hold the tile — every
one of those branches ends *on the field*, at strokes of 7.9–8.7 nm.

So the binding ceiling changes owner from **static stability** to **upstream validity** at four of six models,
and `C-0018`'s "pull-in binds at 11 of 54 coupled states" becomes **6 of 12** at the two states where it
bound. A `marginLowerBound` propagated as if the ceiling were still pull-in is not comparable with the number
that actually binds.

---

## What this does *not* challenge

- **`C-0027`'s measurement of the operating-bias fall.** 8–9 % through `T-16`'s `dV/dF` against 8.5–9.9 %
  re-solved: two independent routes, the same number.
- **`CH-0035`'s decomposition.** It is *confirmed*: the level of `μ` is worth 0.26–0.28 % of the margin and
  the gradient 1.06–1.22 %, so the gradient is 3.8–4.8× the level, and the level's residue is exactly the
  second-order `ℓ(V)` shift `C-0027` modelled as `decayLengthShift`.
- **`C-0027`'s verdict.** It said the movement was *not resolved* and named the calculation that would resolve
  it. That was right, and this challenge exists because the calculation has now been done.
- **`C-0027`'s window, its axis classification, its coupling margins or its stroke clauses.** None is touched.
- **`C-0018`'s 1.007–1.032.** It is correct as what it says — the margin with no finite-tile correction — and
  it is reproduced here to every published digit.

## What would falsify this challenge

1. **A 3-D corner solve** whose contribution reverses the collar's gap-dependence. Both mappings are emitted
   in `T-60`'s result file and they agree in sign and to ~7 % in `d ln μ/dh`; cancelling would need the corner
   to be an order of magnitude outside that bracket.
2. **`C-0019`'s `k_brush` degradation carried on the equilibrium path rather than at the fold.** With it
   applied at `C-0018`'s own fold the combined tangent is `−0.813` to `+1.156 pN/nm` — still straddling zero,
   but now as a *model* spread. If the softened layer moves the fold back to a shallower stroke than 3 nm at
   10 nm / 2 mM, Ground 2's sign rule flips that state too.
3. **A different reading of `C-0002`'s crossover.** Ground 3 rests on `φ = 0.2` as the ceiling; `C-0002` gives
   0.2–0.3, and at 0.3 the crossover bias rises and pull-in may keep the ceiling at more of the six models.
   The margins of Grounds 1 and 2 do not depend on it.
