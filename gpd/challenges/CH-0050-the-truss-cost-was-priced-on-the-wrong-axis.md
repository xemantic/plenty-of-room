# CH-0050 — "A triangulated head cannot sway, and sway is the draw-in" prices the truss on the wrong axis: the axis the standoff BUCKLES about is orthogonal to the axis it draws in on, so a truss can restrain one without touching the other

| | |
|---|---|
| **Against** | [`C-0028`](../claims/C-0028-standoff-base-joint.md) (open question 3, and the *"the sway IS the draw-in"* identity as applied to a truss) and [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) (the closing paragraph of *"the design that results"*, and its way 2 of failing) |
| **Raised by** | [`C-0037`](../claims/C-0037-triangulated-standoff.md), task [`T-72`](../tasks/T-72-triangulated-standoff.md) (covering `T-66`) |
| **Grounds** | **logical** — a frame couple is a rank-one tensor on the leg offsets, so it acts on **one** axis and is identically zero on the orthogonal one — **plus in-silico**, the whole of `C-0030`'s pipeline re-run on an assembled truss head |
| **Status** | **RAISED.** Both claims' *identities* are upheld to the last digit; what is challenged is a **consequence** each of them states in one sentence and neither computes |

---

## What the two claims say

`C-0028` establishes an identity that is not in question here:

> **"The standoff's head translation in the flexure's plane is the column's *sway* and the flexure's *draw-in* under two names."**

and draws from it, in its open question 3:

> *"Whether the design should abandon the single-duplex standoff for a TRIANGULATED one … **It would remove the buckling problem and cost the draw-in release the standoff exists for.**"*

`C-0029` inherits that reading and makes it the reason `T-66` might not rescue its own closed branch:

> *"The restrained-axis reading is available only if a **second element** restrains the free axis. That is a truss — `T-66` — and `C-0028` already priced what it costs: **a triangulated head cannot sway, and sway is the draw-in**, which is the whole reason the standoff exists."*

`CLAUDE.md` carries the same sentence as a design rule:

> *"**The standoff's sway and the flexure's draw-in are the SAME coordinate.** So a bending standoff cannot be made to release the draw-in without being made to buckle in it … Two requirements in direct opposition on one degree of freedom."*

## What is wrong with it

**The identity is about one coordinate. The buckling is not.**

`C-0029`'s own counting theorem is what breaks the syllogism, and neither claim followed it through:

1. a two-link base restrains rotation about the chord's perpendicular bisector and leaves the orthogonal axis with `2 k_bond,θ` = 13.53 pN·nm/rad;
2. **a column buckles about its softest axis**, so the standoff buckles in the plane the base leaves free;
3. the design lays the chord so that the *strong* axis is the loaded one — so **the plane the column buckles in is the plane orthogonal to the draw-in**.

The draw-in lives on `(u_x, φ_y)`. The buckling that closes the branch lives on `(u_y, φ_x)`. A truss whose legs are offset in `y` adds a frame couple `k_a Σy_i²` to `φ_x` and, **identically and not approximately**, nothing to `φ_y`:

&nbsp;&nbsp;&nbsp;&nbsp;for a two-leg row of separation `w` at azimuth `θ` to the flexure axis,
&nbsp;&nbsp;&nbsp;&nbsp;`Σx_i² = (w²/2)cos²θ`, `Σy_i² = (w²/2)sin²θ`, and at `θ = π/2` the first is **exactly zero**.

So *"a triangulated head cannot sway"* is true of a head triangulated **along** the flexure axis and false of one triangulated **across** it — and only the second one restrains the axis that was failing.

## What it is worth

`C-0037`'s table, at `ℓ = 8 nm` on `C-0029`'s own realisable hard-chord base, `C-0030`'s coupled beam, the favourable mounting and 45 paths:

| | single standoff | **two legs ACROSS** | two legs ALONG |
|---|---|---|---|
| adopted critical load, softest plane | **1.46 pN** | **9.77 pN** | 2.91 pN |
| governing plane | free | **loaded** | free |
| draw-in supplied / demanded at 3 nm | 3.75 | **2.90** | 1.39 |
| assembled tangent at 3 nm | 25.20 | **26.09** | 33.02 |
| buckling margin (CanDo / Fields) | 0.50 / 0.38 | **2.79 / 2.10** | 0.54 / 0.40 |
| verdict | FAIL `P6` | **PASS** | FAIL `P6` |

> **The cross row costs 23 % of the draw-in supply and 3.5 % of the tangent, and buys 6.71× in the critical load.** The along row — the arrangement the two claims' sentence describes — costs 63 % of the supply, adds 31 % to the tangent **and still fails `P6`**, because it spends the whole frame budget on the plane that was not failing.

And the draw-in cost is not even the frame's: `L2a6`, `L2a8` and `L2a12` have **identical** span, tangent, `Φ` and supply-to-demand ratio, because `Σx_i² = 0` at every separation. **The cross row's entire cost is the leg COUNT (two flexibilities in parallel), not the triangulation.**

## What is NOT challenged

- **`C-0028`'s identity stands**, and is re-asserted in `C-0037`'s code: `1/C11` of the assembled head is still the sway stiffness and still the draw-in release.
- **`C-0030`'s supply mechanism stands**, reproduced to ≤ 4e−4 (span 31.82, tangent 25.23, duty 3.313, `P_c` 7.21).
- **`C-0029`'s counting theorem stands** — it is what this challenge is built on — and its ceiling (78.24 / 62.06 / 13.53) is reproduced to ≤ 6e−5.
- **`C-0029`'s verdict on the SINGLE standoff stands**: its weak-axis critical loads are reproduced to ≤ 2.6e−3 and the single standoff fails `P6` at **every** length here too.

What changes is only this: `C-0029`'s *"way 2 of failing — a second element restraining the standoff's free axis **at no cost in sway**"* is not a hypothetical. It is available, its cost in sway is 23 % of a supply that had 3.75× of margin, and it is bought with the arrangement the literature already builds.

## How this challenge would itself fail

1. **A demonstration that the two leg heads cannot be tied rigidly enough.** The cap is in series with the legs' axial couple; at the recommended 8 bp separation the loaded plane is the minimum and the cap's compliance is absorbed entirely, but at the 6 bp steric floor a one-link cap takes the critical load from 11.40 to 7.45 pN.
2. **A demonstration that two 90° junctions cannot close on one sheet duplex 6–8 bp apart.** `C-0029`'s closure search places **one**; two of them share a seat duplex and their scaffold excursions must not collide. That is the recommended design's largest open item.
3. **`k_s` far below `C-0020`'s construction.** At `k_s/32` the margin is 1.29 on CanDo's rigidity and **0.97 on Fields et al.'s** — the same crossing `C-0030` reports, in the same place.
