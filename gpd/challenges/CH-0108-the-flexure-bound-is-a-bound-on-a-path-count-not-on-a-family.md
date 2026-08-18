# CH-0108 — `C-0069`'s *"refused at every span, every end joint and every placement"* is a bound on the family **at 34 paths**, and the path count is a design variable

| | |
|---|---|
| **Against** | [`C-0069`](../claims/C-0069-output-element-placement.md) cheap bound 3, and [`CH-0081`](CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md) statement 1 |
| **Raised by** | [`C-0093`](../claims/C-0093-shared-body-coupling.md) (`T-162`) |
| **Grounds** | **methodological** — a quantity quoted without the state it is read at, where the state is a **path count** |
| **Status** | **OPEN** |

---

## What the standing claims say

`C-0069`, cheap bound 3, verbatim:

> | **3** | **the two-support flexure's own floor, `(48 EI/k)^(1/3)`** | **22.414 nm** | 8.19 nm | **2.737** | **YES** | **the answer for the whole `E3`/`C-0030` family — refused at every span, every end joint and every placement, which is strictly stronger than `C-0065`'s 12 of 34, which is a count on one placement; this is a bound on the family** |

`CH-0081` restates it:

> 1. **The two-support family is refused at every span, every end joint and every placement.** Its `c` is bounded below by 48 (`C-0025`), so its shortest possible member is `(48 EI/k₁)^(1/3) = 22.414 nm`, **2.74×** the budget. That is strictly stronger than `C-0065`'s 12 of 34, which is a count on one placement; this is a bound on the family.

## The challenge

**`k₁` is not a constant of the family. It is `C-0017`'s mandate DIVIDED BY THE PATH COUNT**, and
the division is a property of the coupling's *topology*, not of the flexure.

`T-162` reproduces the 22.414 nm to `8.6e−6` relative and identifies its input:
`k₁ = 33.3333/34 = 0.980392 pN/nm`, i.e. `C-0075`'s self-consistent 34 paths. Since a bending
element's plan length is `(c EI/k)^(1/3)`, it goes as `n^(1/3)` in the path count, and
`34^(1/3) = 3.24`:

| elements sharing the mandate | per element [pN/nm] | per element [pN] | two-support `(48EI/k)^(1/3)` [nm] | one-support `(12EI/k)^(1/3)` [nm] | inside 8.19 nm? |
|---|---|---|---|---|---|
| **1** | 33.3333333 | 100.0 | **6.91878937** | **4.35856418** | both |
| **2** | 16.6666667 | 50.0 | **8.71712836** | **5.49144676** | one-support only |
| **4** | 8.33333333 | 25.0 | 10.9828935 | **6.91878937** | one-support only |
| **6** | 5.55555556 | 16.6666667 | 12.5722746 | **7.92003673** | one-support only |
| 10 | 3.33333333 | 10.0 | 14.9060798 | 9.39024187 | neither |
| 34 | 0.980392157 | 2.94117647 | **22.4141917** | 14.120056 | **neither — the standing reading** |

So the *whole* `E3` family is refused at 34 paths and admitted at one to six, and nothing in
`C-0069` or `CH-0081` says which is being asserted. The sentence *"a bound on the family"* is
true of `(family, 34 paths)` and false of `family`.

**And a path count of one to six is exactly what a non-array coupling asks for.** `C-0093` shows
that a coupling in which the tile is tied to a shared body puts the mandate on the **body's own
ground** rather than on the 34 tie paths: the placement equality `C-0017` fixes is satisfied by
`series(Σ tᵢ, g)` rather than by `Σᵢ series(tᵢ, gᵢ)`, and the ties then carry no mandate at all.
The ground elements are the ones that must place, and there are one to six of them.

## What this does and does not claim

- It **does not** claim the flexure branch is reopened. `C-0069`'s own bound 5 (a kinematic floor
  of 4.50 nm) and its bound 4 (the normal direction, 112×) are untouched, the per-element force
  at one and two elements is **100 and 50 pN**, past the 65 pN nicked ceiling and the 48 pN
  shear allowable, and only the four-to-six-element rows clear both a plan budget and a force
  allowable.
- It **does not** claim the 8.19 nm budget is the right budget for a ground element. That budget
  is `C-0069`'s **rooted-arm** ceiling on the *tile's* 10.88 nm upward lattice; an element
  grounding a body above the tile is not rooted on the tile, and **no claim in this corpus
  measures the plan budget of the body's own lattice**. The table above is therefore an
  *a fortiori* reading: even against the tile's own tightest budget, four to six elements place.
- It **does** claim that a bound stated as a property of a *family* must carry the path count it
  was read at, in exactly the way this project already requires a stiffness to carry its
  compression, a variance its bandwidth and a flatness verdict its load case and its operating
  state. This is the eighth instance.

## How to settle it

One of:

1. `C-0069` or `CH-0081` restates the bound as `(family, n = 34)` and quotes `n^(1/3)`; then
   nothing else moves and the challenge is a documentation repair.
2. A claim measures the plan budget available to an element that grounds a body **above** the
   tile, which is the number the table above substitutes the tile's own for.
3. A claim shows the ground element count is bounded below by something other than force —
   e.g. by the body's own tilt stability — at a count above six, which would close the window
   from the other side.
