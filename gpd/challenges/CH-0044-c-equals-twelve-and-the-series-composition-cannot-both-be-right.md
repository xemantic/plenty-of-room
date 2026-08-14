# CH-0044 — `c = 12` and `C-0023`'s series composition cannot both be right: a guide carries part of the end moment, so `E5g16` is over-placed by 1.64×

| | |
|---|---|
| **Raised by** | [`C-0034`](../claims/C-0034-guided-arm-anchorage.md) ([`T-70`](../tasks/T-70-guided-arm-anchorage.md)) |
| **Against** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md), the `E5g16` design table and the `E5g` family it recommends |
| **Date** | 2026-08-14 |
| **Grounds** | **methodological** — a boundary condition changed without changing the composition the boundary condition belongs to |
| **Severity** | **the design number moves, the verdict does not.** `E5g16`'s arm is 12.24 nm from a composition that is exact only at `c = 3`; solved on the boundary-value problem `c = 12` describes, the same geometry assembles to **54.61 pN/nm against the 33.3333 pN/nm mandate** and past its own **40 pN/nm** compliance ceiling at the **secant**. The corrected arm on the *realised* anchorage is **12.50 nm**, and every acceptance verdict `C-0029` reported still holds |

---

## The statement being challenged

`C-0029` writes `E5`'s assembled stiffness as `C-0023`'s series composition

&nbsp;&nbsp;&nbsp;&nbsp;`1/k = r²/(n k_θ) + r³/(c EI)`

— the hinge's rigid-body rotation about its own spring, in series with the arm's own bending — and
then closes the branch by changing **one letter**:

> *"And the remedy is one letter in the cube root. `c` is the arm's end-condition factor: 3 for a
> cantilever, 12 for a guided arm. That single change lifts the cap from 9.77 to **15.50 nm** and
> reopens the branch."*

## Why the two statements are incompatible

Both terms describe the **same** beam. Write it as the boundary-value problem it is — a beam of
length `r` and rigidity `EI` whose near end sits on the hinge spring `k_n = n k_θ` and whose far end
sits on the anchorage spring `k_f`, its two bodies translating relative to each other and not
rotating. Condensing the two end rotations out of the Euler-Bernoulli element stiffness matrix gives

&nbsp;&nbsp;&nbsp;&nbsp;`(4+ρ_n)θ_A + 2θ_B = 6δ/r`, &nbsp;&nbsp; `2θ_A + (4+ρ_f)θ_B = 6δ/r`,
&nbsp;&nbsp; `ρ = k r/EI`

&nbsp;&nbsp;&nbsp;&nbsp;→ &nbsp; **`k = (12EI/r³)·(ρ_nρ_f + ρ_n + ρ_f)/(ρ_nρ_f + 4ρ_n + 4ρ_f + 12)`**.

Now compare the two readings at the same `ρ_n`:

| far end | the series composition | the boundary-value problem |
|---|---|---|
| **free** (`ρ_f = 0`) | `k = 3ρ_n EI/((ρ_n+3)r³)` | `k = 3ρ_n EI/((ρ_n+3)r³)` — **identical, to the last digit** |
| **guided** (`ρ_f → ∞`) | `k = 12ρ_n EI/((ρ_n+12)r³)` | `k = 12(ρ_n+1) EI/((ρ_n+4)r³)` — **different** |

**The composition is exact at exactly one corner, and it is `C-0023`'s corner.** The reason is
physical and needs no algebra to state: the composition charges the hinge the **whole** tip moment
`F r`, which is true only when the far end carries none. A guide carries part of it — `M_far =
F r ρ_f/(2(1+ρ_f))`, rising to `F r/2` — so **a guide relieves the hinge**, and the series reading
therefore *understates* the stiffness. It is asserted as a gate-3 test that the composition
reproduces the BVP to `1e−12` at `ρ_f = 0` and falls strictly below it at every `ρ_f > 0`.

## What it costs, at `C-0029`'s own design point

| reading | arm [nm] | `c` realised | assembled [pN/nm] | against the 33.3333 mandate |
|---|---|---|---|---|
| **`C-0029`'s `E5g16` as filed** | **12.242** | asserted **12** | **33.3333** by construction | — |
| the same geometry, same `c = 12` boundary condition, on the BVP | 12.242 | **9.681** | **54.61** | **1.64× over-placed**, and past its own 40 pN/nm ceiling at the **secant** |
| **corrected: the BVP placed on the *realised* two-terminus anchorage** | **12.496** | **6.284** | 33.3333 | **the design number** |
| the same anchorage under the large-rotation series composition | 11.028 | 7.356 | 33.3333 | the other end of the bracket |

> **The two errors run opposite ways and very nearly cancel: the realised end condition is *softer*
> than asserted (6.28 against 12) while the composition it was solved with is the *soft* reading
> (retaining 0.607 of the true stiffness). 12.242 nm sits inside the corrected bracket
> 11.03–12.50 nm, which is why `C-0029`'s verdict survives an assertion that does not.**

## What does NOT move

Every acceptance verdict in `C-0029`'s `E5g` table:

- the arm still exceeds §3's **desired** 10 nm stroke, on every reading and at 16 and 32 crossovers;
- the tangent still lands inside `C-0023`'s 40 pN/nm ceiling at both §3 strokes;
- the cap is still above 10 nm — and `C-0034` shows it is above 10 nm for **every two-link**
  anchorage, because `ρ_f` carries the arm length;
- `C-0029`'s counting theorem, its closure search, its `CH-0039` and `CH-0040`, and the whole
  standoff verdict are untouched — this challenge is against one composition, not against the claim.

All eight of `C-0029`'s `E5` rows are reproduced here to `≤ 2.8e−9` before being re-read.

## What would settle it

1. **A large-rotation solution of the two-spring beam** (an elastica with two end springs). Both
   readings here are approximations of it from opposite sides — the series one is small-`ρ_f`-exact
   and large-rotation; the BVP one is `ρ_f`-exact and small-deflection — and the true arm is inside
   their bracket. That is `T-79`.
2. A demonstration that `E5`'s hinge is **not** a rotational spring at the arm's end but something
   else, in which case the composition is not an approximation to this BVP at all. Nothing in
   `C-0023`, `C-0025` or `C-0029` says so.

## Errata noted in passing, not challenged

`C-0029`'s gate-5 row quotes the guided arm ceiling as *"15.5005"*; its own result file carries
**15.5029478**, and the value re-derives here to `2.8e−9`. Every design table in the claim says
15.50, so nothing downstream used the slipped digit.
