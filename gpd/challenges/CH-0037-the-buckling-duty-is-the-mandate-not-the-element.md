# CH-0037 — The standoff's buckling duty is read on the MANDATE secant, not on the element's own reaction, and the element strain-stiffens: every buckling margin in `C-0025`'s window table is 1.27–1.70× optimistic

| | |
|---|---|
| **Against** | [`C-0025`](../claims/C-0025-flexure-end-joint.md)'s standoff-window table — its `buckling margin (pinned / guided)` column, `17.0 / 68.1×` down to `1.5 / 6.1×` — and the design summary line *"end shear per joint 1.111 pN at 3 nm, **3.70 pN at 10 nm**; standoff buckling 8.87 pN (pinned head) / 35.5 pN (guided)"* |
| **Raised by** | [`C-0028`](../claims/C-0028-standoff-base-joint.md) (`T-40`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — **a load read off the placement condition rather than off the element that carries it**, in a programme whose own `CLAUDE.md` records four times that a secant and a tangent are different quantities |
| **Direction** | **unfavourable, uniformly.** Every margin falls. Nothing in `C-0025` becomes unbuildable at a clamped base — the corrected margins are 1.21–1.85× and still above one — but the **ranking of the constraints changes**, and at the 10 nm end of the window the margin is 1.21× rather than the 1.53× reported |
| **Status** | raised. **No other number in `C-0025` moves** — its span, `c`, tangent, beam tension, both buckling loads and all eight window rows are reproduced in `C-0028` to ≤ 1.2e−9 |

---

## What is challenged

`C-0025` reports, in its design summary and in every row of its standoff-window table:

> **end shear per joint** 1.111 pN at 3 nm, **3.70 pN at 10 nm**; standoff buckling 8.87 pN (pinned head) / 35.5 pN (guided)

and derives the window's `buckling margin` column from that 3.70 pN. The number is the **mandate secant** reading,

&nbsp;&nbsp;&nbsp;&nbsp;`33.3333 pN/nm × 10 nm / 45 paths / 2 ends = 3.7037 pN`,

and it is **identical for every design in the table** — for the crossover joint `J1`, for the 2-crossover clamp, for the ssDNA hinge and for all eight standoff lengths — which is the visible symptom. The code says so directly:

```kotlin
endShearAcceptable = flexure.endShear(ACCEPTABLE_STROKE),   // the element's own
endShearDesired    = shareDesired / 2.0,                    // the MANDATE's
```

## Why it is wrong

**The flexure is a strain-stiffening element, and `C-0025` says so itself.** Its whole `t/s` column exists because the tangent exceeds the secant; its own theorem (via `C-0017`) is that *"the whole `tangent/secant` ratio is free stability margin at zero placement cost"*. A strain-stiffening law placed at 3 nm therefore delivers **more** than `k·s` at 10 nm:

&nbsp;&nbsp;&nbsp;&nbsp;`R(10) = c EI (10)/L³ + 2·cableNormalForce(S_eff, L/2, 10) > 33.3333 × 10/45`.

At 3 nm the two readings **coincide exactly**, because the placement condition *defines* the secant there — which is precisely why the error is invisible in the `endShearAcceptable` column and why it survived `C-0025`'s twenty-six tests.

## The size of it

Recomputed in `C-0028` from `C-0025`'s own `PartiallyRestrainedFlexure`, at the clamped base `C-0025` assumes:

| `ℓ` [nm] | mandate duty [pN] | **element duty** [pN] | ratio | `C-0025`'s margin | **corrected margin** |
|---|---|---|---|---|---|
| 3 | 3.7037 | **18.081** | **4.882** | 17.03× | **3.49×** |
| 5 | 3.7037 | 9.542 | 2.576 | 6.13× | 2.38× |
| **7** | 3.7037 | **6.277** | **1.695** | **3.13×** | **1.85×** |
| **8** | 3.7037 | **5.518** | **1.490** | **2.39×** | **1.61×** |
| **9** | 3.7037 | **5.027** | **1.357** | **1.89×** | **1.39×** |
| **10** | 3.7037 | **4.697** | **1.268** | **1.53×** | **1.21×** |

> **Over `C-0025`'s own 7–10 nm window the correction is 1.27–1.70×, and below it — where the membrane term is large — it reaches 4.88×.**

The ratio *falls* with `ℓ` because a longer standoff releases more draw-in, which collapses `S_eff` and with it the membrane term that produces the excess. So the error is largest exactly where `C-0025`'s table looks safest.

## What it changes, and what it does not

**It does not unbuild anything at a clamped base.** All four corrected margins stay above one, so `C-0025`'s reported *verdicts* are unaffected — and `C-0025` was careful to report the buckling margin *beside* its five predicates rather than as one of them, which is why no `PASS` in its table depends on it.

**What it changes is the ordering.** `C-0025` reads the window as closed above by `C-0017`'s 10 nm envelope, with buckling a comfortable 1.5–3.1× reserve. On the corrected duty the 10 nm end has **1.21×**, and once the base is modelled (`CH-0038`) buckling becomes the **binding** constraint at the top of the window rather than a reserve. A 1.21× margin on an unmeasured rigidity that a direct measurement puts **25 % lower** (Fields et al., *NAR* **41**:9881, 2013, read directly) is not a reserve at all.

## The remedy

**Read a buckling duty off the element, never off the mandate**, and quote it with the stroke. `C-0028` does this throughout, and goes one step further: it reports the **stroke at which the standoff buckles**, `s_buckle`, which needs no margin convention at all — 11.5 to 14.2 nm across `C-0025`'s window at a clamped base, and 3.0 to 8.1 nm at a single-crossover base.

## The general lesson

`CLAUDE.md` already records that *"the stroke and the noise use different stiffnesses — the secant sets the stroke, the tangent at the working point sets the fluctuation"*, and that *"placement is written on the coupling's SECANT and stability on its TANGENT"*. This is the **fourth** appearance of the same split and the first in which the *load on a sub-component* was taken from the secant. The rule generalises:

> **A secant is a statement about the design point and nothing else. Any quantity evaluated away from that point — a member force, a duty, a reaction — must come from the element's own law.**

## What would overturn this challenge

A demonstration that the standoff's compression is set by the **assembled coupling's** reaction rather than by its own flexure's — i.e. that the 45 elements share load through a superstructure so that each end shear is the mean rather than the local value. That is `T-31`'s question, and it is open. It would not restore 3.70 pN, because the *assembled* reaction at 10 nm is also the element law's and also exceeds the secant reading; it would only redistribute it.
