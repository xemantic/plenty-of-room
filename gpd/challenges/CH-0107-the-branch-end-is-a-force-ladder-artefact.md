# CH-0107 — **`C-0084`'s *"the branch ends by FOLDING at 7.9205 nm"* is a property of a doubling force ladder, not of the elastica** — the same arm answers to **8.1611 nm**, 0.2414 nm further, with `max_s|φ|` still below a right angle; and `CH-0099` prices its inflation from that boundary, so the price is right at four states and 2.9× too large at four others

| | |
|---|---|
| **Raised by** | [`C-0092`](../claims/C-0092-large-rotation-arm-branch.md) (`T-157`) |
| **Against** | [`C-0084`](../claims/C-0084-recommended-element-pull-in-fold.md) **Deliverable 2** — *"refusal 7.9197 nm, branch validity 7.9205 nm … **the branch ends by FOLDING, not by turning past a right angle**"* — and the boundary [`CH-0099`](CH-0099-a-ceiling-taxonomy-belongs-to-a-load-line-too.md) prices its **2.57–3.74×** inflation from. **Not against `C-0084`'s verdict**, which this claim strengthens, nor against any of its 108 fold rows |
| **Grounds** | `C-0084` reads its ceiling off `C-0039`'s `TwoSpringElastica.forceForDisplacement`, which **doubles** a tip force from `0.5 δ k_small` until the stroke reaches its target, and off `stateAtForce`, which brackets the shooting parameter by **doubling from a seed** and runs Illinois inside whatever that produces. Both are exact while the far-end moment residual has **one** root. On this arm a second root appears at about **100 pN** of tip force — far below the right angle `C-0039`'s own KDoc warns about — and once it has, a doubling step can bracket the wrong one: the stroke reported at that force collapses, the force ladder never reaches its target, and the routine throws. **A doubling ladder does not report a branch end; it reports having lost the branch.** Continued instead by marching the near-end rotation and taking the **first** sign change in the force above the previous one, the same arm answers to **8.1610821 nm** with `max_s\|φ\| = 1.5707924 rad`, **0.999997 of `π/2` and still below it** |
| **Severity** | **a model boundary quoted 0.2414 nm too shallow, and it moves numbers in two claims — favourably.** `C-0084`'s bounded negative gets *stronger*: *"no fold below 7.9097 nm"* becomes *"no fold at 12 of 12 states below 8.1511 nm"*, 0.9984 of the arm's own contour, with only **0.0033087 nm** — 0.97 % of one base-pair rise — left unresolved. `CH-0099`'s **candidate** stands and its **value** does not |

---

## What is claimed upstream

`C-0084`, Deliverable 2:

> | refusal — the largest stroke at which **both** the reaction and the tangent close | **7.9197 nm** |
> | branch validity — the largest stroke at which the **reaction** closes with `max_s\|φ\| < π/2` | **7.9205 nm** |
>
> **The branch ends by FOLDING, not by turning past a right angle** — the reaction still closes at 7.9205 nm
> with the arm at 0.94 of `π/2` and refuses immediately above, so `π/2` is never reached at all.

and `CH-0099`, which prices an inherited ceiling list against exactly that boundary:

> At **12 of 108** states the branch ends there with no fold. At **8** of them the inherited three-candidate
> list would return a ceiling above the branch end … inflation **2.567× to 3.740×**.

**Both readings are correct about the object they were taken on.** `C-0084` even reproduces its own refusal
here to the last digit it published — `7.9196867` against `7.91968584`, on the same load line through the
same `loadLineStrokeCeiling`.

## What `C-0092` finds

| quantity | `C-0084` | `C-0092` |
|---|---|---|
| the largest stroke the element answers at | **7.9197 nm** | **8.1610821 nm** |
| `max_s\|φ\|` there | 1.4799 rad (0.942 of `π/2`) | **1.5707924 rad (0.999997 of `π/2`)** |
| what ends it | *"the branch folds"* | the **RK4 first integral**, a measured integrator limit |
| the arm's contour, a hard bound on every branch | 8.16439083 nm | 8.16439083 nm |
| the window left unresolved | 0.2447 nm | **0.0033087 nm** |

And the *reason* the ladder loses it is measurable: on a 4000-cell scan of `[0, 4π]` the residual has **one**
root to about 50 pN of tip force, **two** at 100 pN, **seven** at 200, **fifteen** at 1000 and **thirty-nine**
at 5000. **The multiplicity starts three decades of force before the right angle does.**

The large-rotation branches, incidentally, are the wrong place to look for a deeper stroke: a curled shape's
`∫sin φ` cancels against itself, so **every** one of them reaches a *smaller* `δ` than the small-rotation
root at the same force. They retreat from the stroke, they do not extend it.

## What it costs, and where

`CH-0099` priced its inflation at eight states as 2.567–3.740×. With the domain corrected the *"element model
branch end"* candidate **binds at 0 of 12** states of the recommended device, and the margins move by:

| state | `C-0084`'s margin | with the corrected domain | movement | what binds now |
|---|---|---|---|---|
| 10 nm, 0.5 mM, strong-stretching(des-Cloizeaux) | 3.2141 | **10.7287** | **3.3380×** | point-ion 1.0 V |
| 10 nm, 0.5 mM, strong-stretching(virial) | 3.4699 | 10.9072 | 3.1433× | point-ion 1.0 V |
| 10 nm, 2.0 mM, strong-stretching(des-Cloizeaux) | 2.3679 | 7.1784 | 3.0316× | point-ion 1.0 V |
| 10 nm, 2.0 mM, strong-stretching(virial) | 2.5764 | 7.3137 | 2.8387× | point-ion 1.0 V |
| 10 nm, 2.0 mM, strong-stretching(two-body) | 2.2834 | 2.6534 | **1.1620×** | `φ = 0.2` |
| 10 nm, 0.5 mM, strong-stretching(two-body) | 3.0885 | 3.5824 | 1.1599× | `φ = 0.2` |
| 10 nm, 2.0 mM, alexander-box(des-Cloizeaux) | 2.1591 | 2.1609 | **1.0008×** | `φ = 0.2` |
| 10 nm, 0.5 mM, alexander-box(des-Cloizeaux) | 3.0181 | 3.0204 | 1.0007× | `φ = 0.2` |

**So the taxonomy gap's price is set by whichever candidate is SECOND, and `CH-0099` priced it against the
last.** At four states `C-0002`'s `φ = 0.2` steps in and the movement is a rounding error; at four the
point-ion boundary binds and it is 2.8–3.3×. The generalisable half is that sentence, not the numbers.

## What this does NOT challenge

- **`C-0084`'s verdict.** *No fold at 2 mM at 0 of 6 models* stands and is **strengthened** — the negative now
  runs to 0.9984 of the arm's contour instead of to 0.969 of it.
- **`C-0084`'s worst margin.** At 10 nm / 2 mM the band becomes **1.3877–7.3137** against its published
  1.3877–2.5764: **the minimum, which is what governs, does not move at all.**
- **`CH-0099`'s candidate.** A coupling element has a domain, `C-0018`'s three-candidate list has no name for
  it, and three of the five mechanisms `C-0069` admits have one. All of that stands. What moves is where this
  element's domain ends.
- **`C-0039`'s solver on the problems it was written for.** `T-79`'s primary reading is a 3 nm stroke on an
  11–13 nm arm, where the residual has one root and every routine in it is exact. The failure needs a stroke
  that is 97 % of the contour.
- **Any number of `C-0069`, `C-0071` or `C-0034`.** The contour, the root and tip stiffnesses and the
  placement are all re-derived here and all reproduce.

## What would settle it

1. **An amendment to `C-0084` Deliverable 2** replacing *"the branch ends by folding"* with *"the doubling
   force ladder loses the branch"*, and the 7.9205 nm with the contour as the model's own boundary.
2. **A repricing of `CH-0099`'s table** at the corrected domain — done here for the recommended device's
   twelve states, **not** done for the other 96.
3. **The generalisable repair, and it is one line**: `TwoSpringElastica.forceForDisplacement` should walk the
   force **down** from a bracket the caller supplies, or continue in the near-end rotation, rather than
   doubling blind. `CLAUDE.md` already records the neighbouring trap — *a search floor written as a multiple
   of the working stroke silently excludes designs whose working stroke IS the target* — and this is its
   ceiling-side twin.

## Status

**OPEN.** Filed with `C-0092` (`T-157`). The evidence is
`gpd/results/T-157-large-rotation-arm-branch.json`: `contourBound`, the `enumeration` rows, the `branch`
table's last row, and the `folds` rows' `biasMarginInC0084` / `marginMovement` pair.
