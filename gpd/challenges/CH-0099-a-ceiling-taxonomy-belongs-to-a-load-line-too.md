# CH-0099 — **`CH-0015` says a bias ceiling belongs to a `(bias, load line)` pair; so does the LIST of candidate ceilings** — `C-0018`'s three candidates silently assume a coupling that can be driven to any stroke the layer admits, and a rotating arm cannot, so at 8 of 108 states the inherited list quotes a margin over a stroke the element model does not describe, inflating it by up to **3.74×**

| | |
|---|---|
| **Raised by** | [`C-0084`](../claims/C-0084-recommended-element-pull-in-fold.md) (`T-149`) |
| **Against** | [`C-0018`](../claims/C-0018-maximum-usable-bias.md)'s **candidate ceiling list** — `{static stability (pull-in), C-0002's φ = 0.2, CH-0007's point-ion 1.0 V}` — as re-used verbatim by [`C-0032`](../claims/C-0032-softening-coupling-stability.md) and, until it was extended, by `C-0084` itself. **Not against any number `C-0018` reports**: on `C-0018`'s own load lines the list is complete, and every one of its 162 rows stands |
| **Grounds** | `C-0018` established the discipline that *"a ceiling belongs to a `(bias, load line)` pair, never to the bias alone"* (`CH-0015`) and then applied it to the **values** of the three candidates while holding the **membership** of the list fixed. The membership is load-line-dependent too. Every load line `C-0018` and `C-0032` ran — an affine mandate, a preloaded dead load, a midspan flexure — is defined at every stroke the layer admits. `C-0071`'s recommended element is an **inextensible rotating arm** whose law stops existing at 7.9197 nm of stroke, and `C-0039`'s shooting solve enumerates only its small-rotation branch. At 12 of `C-0084`'s 108 states the equilibrium path therefore ends on the **element model**, and the inherited list has no candidate for that |
| **Severity** | **a taxonomy gap with a measurable price, not an error.** `C-0084` extends the list with a fourth, explicitly named `element model branch end (C-0039's small-rotation branch)`, and carries **both** readings in every row (`biasMargin` and `biasMarginIgnoringElementBoundary`). The inherited reading is larger at all 8 states where the new candidate binds, by **2.57× to 3.74×** |

---

## What is claimed upstream

`C-0018` reports a maximum usable bias at 162 states, taking at each the smallest of three candidate ceilings, and `CH-0015` is made executable in `actuator/PullInStability.kt`'s `bindingCeiling`, whose KDoc records the tie-break discipline in detail. `C-0032` re-uses the same three candidates unchanged across 216 states.

**Nothing here disputes any of that.** On a load line defined over the whole admissible stroke range the three candidates are exhaustive: either the branch folds, or it reaches a layer-validity boundary, or the field's own point-ion boundary binds first.

## What `C-0084` finds

The recommended arm's law has a ceiling of its own, and it is well below the layer's:

| quantity | value | owner |
|---|---|---|
| the 10 nm layer's own stroke ceiling | ~9.4 nm | the layer (`C-0018`'s convention) |
| the arm's contour, an inextensibility bound | 8.16439 nm | geometry |
| refusal — the stroke past which the tangent's difference no longer closes | **7.9197 nm** | `C-0039`'s shooting solve |
| the stroke ceiling the paths were run to | **7.9097 nm** | the **element model** |

At **12 of 108** states the branch ends there with no fold. At **8** of them the inherited three-candidate list would return a ceiling above the branch end — usually `CH-0007`'s 1.0 V — which is a bias that would put the equilibrium at a stroke the element model does not describe:

| state | margin with the element boundary | margin without it | inflation |
|---|---|---|---|
| 10 nm, 0.5 mM, strong-stretching(two-body) | **3.0885** | 11.5525 | **3.740×** |
| 10 nm, 0.5 mM, strong-stretching(des-Cloizeaux) | 3.2141 | 10.7287 | 3.338× |
| 10 nm, 2.0 mM, strong-stretching(two-body) | **2.2834** | 7.7937 | **3.413×** |
| 10 nm, 0.5 mM, strong-stretching(virial) | 3.4699 | 10.9072 | 3.143× |
| 10 nm, 2.0 mM, strong-stretching(des-Cloizeaux) | 2.3679 | 7.1784 | 3.032× |
| 10 nm, 0.5 mM, alexander-box(des-Cloizeaux) | 3.0181 | 8.7034 | 2.884× |
| 10 nm, 2.0 mM, strong-stretching(virial) | 2.5764 | 7.3137 | 2.839× |
| 10 nm, 2.0 mM, alexander-box(des-Cloizeaux) | 2.1591 | 5.5425 | 2.567× |

**And the inflated states are precisely the headline ones** — the 10 nm layer at 2 mM and 0.5 mM, the device `C-0071` recommends. Had `C-0084` inherited the list unexamined, its headline margin at 2 mM would have read **1.39–7.79** instead of **1.39–2.58**.

## Why this is a general statement and not a quirk of one element

The new candidate is not *"this arm is short"*. It is that **a coupling element has a domain**, and three of the five mechanisms `C-0069`'s census admits have one:

- an **inextensible** bending element cannot stroke past its own contour, and any rotating one stops well before it;
- an **entropic** strand has a contour ceiling of exactly the same kind;
- a **compression member** has an Euler load, past which the load line does not exist either.

Only the axial stretch and the idealised affine mandate are defined everywhere. `C-0018`'s list is complete for the second and for nothing else, and every future claim that substitutes an element into `EquilibriumPath` inherits the gap unless it looks.

## What this does NOT challenge

- **`C-0018`'s 162 rows and `C-0032`'s 216 rows all stand.** Neither load line has a domain ceiling, so the fourth candidate would be `null` at every one of their states.
- **`CH-0015` stands** and is strengthened: this is its own rule applied one level up.
- **`C-0084`'s headline stands** — it is computed with the extended list, and its result file carries the inherited reading beside it so the difference is auditable.
- **The new candidate is a MODEL boundary, not a device ceiling.** It says *"the element model stops here"*, not *"the device cannot be biased further"*. `C-0084` names it that way and its validity range says so.

## What would settle it

1. **Extend `actuator/PullInStability.kt`'s ceiling vocabulary** with a named domain boundary, so the list is built from the load line rather than asserted, and every future substitution inherits the check instead of the omission. It is additive and defaults to current behaviour where a line has no domain ceiling.
2. **A one-line audit of every claim that has used `bindingCeiling`** — `C-0018`, `C-0032`, `C-0084` — asking whether its load line has a domain. Two of the three do not, which is why nobody has looked.
3. **A multi-branch elastica**, which would move the boundary rather than remove it, and would answer the separate open question of whether a fold exists past 7.91 nm at all.

## Status

**OPEN.** Filed with `C-0084` (`T-149`). The evidence is `gpd/results/T-149-recommended-element-fold.json`: `folds` rows with `bindingCeiling` = *"element model branch end (C-0039's small-rotation branch)"*, and the paired fields `biasMargin` / `biasMarginIgnoringElementBoundary`.
