# CH-0046 — A standoff in tension does not buckle: `P6` is a predicate of the favourable mounting alone

| | |
|---|---|
| **Against** | [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) — the adverse rows of its Deliverable 4 table (`margin, CanDo / Fields` = 2.53/1.90 … 0.75/0.57) and its sensitivity row `mounting: favourable → adverse` reported as `2.18 → 0.99` and `1.64 → 0.74` |
| **Also touches** | [`C-0028`](../claims/C-0028-standoff-base-joint.md), whose `P6` is inherited unchanged |
| **Raised by** | [`C-0035`](../claims/C-0035-flexure-mounting-sense.md), task [`T-75`](../tasks/T-75-flexure-mounting-sense.md) |
| **Grounds** | **methodological** — a stability predicate evaluated on the *magnitude* of a member force whose *sign* the same model determines, and whose sign is opposite in half the cases tabulated |
| **Status** | **RAISED, and it does not move a verdict** — the adverse mounting fails on `P3`, which owns that column. Filed because the number is quoted and would be read. |

---

## The observation

`C-0030`'s `P6` compares the standoff's Euler load against the flexure's own **end shear at the desired stroke**, taken as a magnitude:

```kotlin
fun endShear(displacement: Double): Double = abs(reaction(displacement)) / 2.0
```

The end shear acts **along the standoff's own axis** — that is why it is the buckling duty rather than the sway duty. But its **direction** along that axis is not free: it points toward the standoff's base exactly when the midspan does.

&nbsp;&nbsp;&nbsp;&nbsp;**favourable ⟺ the midspan sags toward the base plane ⟺ the end shear pushes the head toward its base ⟺ COMPRESSION.**
&nbsp;&nbsp;&nbsp;&nbsp;**adverse ⟺ the midspan deflects away ⟺ the end shear pulls the head away ⟺ TENSION.**

So in the adverse mounting the standoff is a **tie**, not a strut, and

&nbsp;&nbsp;&nbsp;&nbsp;**a member in tension has no Euler load.**

`C-0030`'s adverse buckling margins — 2.53 / 1.97 / 1.61 / 1.35 / 1.15 / **0.99** / 0.86 / 0.75 on CanDo's rigidity, and 1.90 … **0.74** … 0.57 on Fields et al.'s — are therefore charged against a member the same model's kinematics puts in tension at every one of those eight lengths. The correct entry is not a number below one; it is **"not applicable"**.

## What the right predicate is in the adverse mounting

The duty does not disappear, it changes kind. A standoff loaded in tension is limited by

- its own **axial stiffness** `S/ℓ` = 137.5 pN/nm at 8 nm — two orders above anything the joint asks;
- its **base joint's** shear/unzip allowable rather than its Euler load — `C-0006`'s 10 pN unzip and 48–65 pN duplex shear, with `CH-0029`'s bonded-length ladder;
- and the **`P_c = 0` mechanism corner** `C-0028` identifies for a pinned base, which is a statement about a *compressed* sway column and likewise does not transfer.

At `ℓ = 8 nm` the adverse duty at the desired stroke is **7.31 pN**, which clears the 10 pN unzip allowable by 1.37× and the 48 pN shear allowable by 6.6×. So the adverse mounting's *stability* is not the problem; **`P3` is** — 44.82 pN/nm against `C-0023`'s 40 pN/nm ceiling, 12 % over, at every length inside `C-0017`'s envelope.

## Why it matters even though no verdict moves

1. **It is quoted where it will be read.** `C-0030`'s own summary says the adverse mounting has *"margin 0.99× / 0.74×"* and that *"`P6` [is] below one from 8 nm up"*, which reads as *two* independent failures where there is one.
2. **It removes a false symmetry.** `C-0030`'s sensitivity table lists `mounting` as the axis that moves the buckling margin from 2.18 to 0.99 — the largest single mover in the table. That axis does not move the buckling margin at all; it **removes the buckling problem** in one direction and creates it in the other. The favourable mounting is the one that has to buckle, because it is the one that supplies the draw-in.
3. **It sharpens `C-0028`'s trade.** *"The standoff's sway IS the flexure's draw-in"* now has a companion: **the standoff's compression is the flexure's draw-in too.** A mounting that removes the compression removes the supply, which is why the adverse tangent is 1.8× the favourable one. `T-66`/`T-72`'s triangulated standoff inherits both halves.

## How this challenge would itself fail

1. **If the midspan tie is one-sided** and goes slack over part of the stroke, the end shear can reverse within one stroke and both senses occur in one mounting. `C-0023`'s two-sidedness forbids it for the flexure as designed.
2. **If the standoff head carries an out-of-plane offset** large enough that the beam's end moment dominates the end shear in setting the axial direction — the `r·θ` term `C-0030` records as unmodelled.
3. **If the standoff is pre-compressed by its own assembly**, a mounting offset `C-0023` prices as a length. A built-in compression would restore an Euler question in the adverse mounting, at a preload the design has to pay for.
