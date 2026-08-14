# CH-0059 — §3's desired stroke has never been PLACED; it has only been evaluated on the acceptable stroke's coupling — and when it is placed on its own arithmetic the element passes and the ACTUATOR refuses it, at 2.34–2.79×

| | |
|---|---|
| **Raised by** | [`C-0046`](../claims/C-0046-fewer-longer-flexures.md) ([`T-99`](../tasks/T-99-fewer-longer-flexures.md)) |
| **Against** | the programme-wide **reading convention**, carried by [`C-0023`](../claims/C-0023-two-sided-coupling.md), [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md), [`C-0034`](../claims/C-0034-guided-arm-anchorage.md), [`C-0039`](../claims/C-0039-two-spring-elastica.md), [`C-0040`](../claims/C-0040-hinge-line-census.md) and [`C-0041`](../claims/C-0041-flexure-array-packing.md), and relevant to the concurrent [`T-107`](../../TASKS.md) and [`T-108`](../../TASKS.md) |
| **Grounds** | **methodological** — a design evaluated at a state it was not placed for, which is the sixth instance of the discipline `CLAUDE.md` records as *"quote a stiffness with its compression"*, here applied to the **acceptance clause** |
| **Status** | **OPEN.** No number in any challenged claim fails to reproduce — 14 reproductions at ≤ `5.9e−4`. What is challenged is what the numbers are *of* |

---

## The challenge

`C-0017` establishes that **placement is arithmetic**: the force a coupling delivers to a load over
a stroke is `k_c·Δs`, so §3's 100 pN over its **acceptable** 3 nm gives `k_c = 33.3333 pN/nm` *"by
arithmetic"*, with *"no physics in it"*.

Every claim that has since asked whether an element reaches §3's **desired** 10 nm stroke has done
so by taking that same 33.3333 pN/nm coupling — placed for a 3 nm stroke — and evaluating it at
10 nm. On that reading the element must deliver

&nbsp;&nbsp;&nbsp;&nbsp;`33.3333 × 10 = **333 pN**`, &nbsp; 3.33× what §3 asks for,

and `C-0039` reports that the exact element actually needs **699 pN**, calling it *"seven times
§3's own 100 pN"*. That sentence is the challenge in one line: **§3 never asked for 699 pN, and it
never asked for 333 either.** The desired clause's own coupling is

&nbsp;&nbsp;&nbsp;&nbsp;`k_c = 100 pN / 10 nm = **10 pN/nm**`,

which is a **different device**, not a re-evaluation of the same one. `C-0017`'s own arithmetic
says so, and no claim has run it.

## What happens when it IS run

`C-0046` places `C-0034`'s `E5a` on `C-0039`'s elastica at 10 pN/nm and a 10 nm working stroke.
The element passes everything the flexure branch has ever been judged on:

| `n` | `h` | arm [nm] | tangent at 10 nm | usable stroke | per path at 10 nm | inside `C-0023`'s 40 pN/nm |
|---|---|---|---|---|---|---|
| 10 | 4 | 12.73 | 33.4 | **10.000** | 10.00 | yes |
| **14** | **4** | **13.78** | **26.3** | **10.000** | **7.14** | **yes** |
| 28 | 2 | 15.64 | 20.2 | **10.000** | 3.57 | yes |
| 56 | 1 | 18.10 | 16.7 | **10.000** | 1.79 | yes |

The arm reaches past the stroke, the tangent holds the compliance ceiling with room to spare at
both readings, and the per-path force is inside the 10 pN unzip allowable from 10 paths upward.
**12 of 29 points clear every clause the branch owns.**

## And every one of them is refused by the actuator

`C-0017`'s second clause is **stability**, `k_c > |k_eff|`, read on the tangent — and at the 10 nm
layer in 2 mM MgCl₂ it reports `|k_eff| = **23.41–27.91 pN/nm**`. So

&nbsp;&nbsp;&nbsp;&nbsp;**10 pN/nm is 2.34–2.79× BELOW the floor**,

and `C-0032` has already shown that the *existing* 33.3333 pN/nm coupling clears it with a bias
margin of only **1.0000–1.0019**. Composing the two clauses of `C-0017` gives a bound that names no
element at all:

&nbsp;&nbsp;&nbsp;&nbsp;**`δ ≤ F/|k_eff|` &nbsp;⟹&nbsp; 3.58–4.27 nm at §3's 100 pN.**

> **§3's desired 10 nm stroke and §3's 100 pN cannot both be delivered by any STABLE coupling at
> the 10 nm layer in 2 mM, whatever the coupling is made of.** One power of the position bound
> away, this is the same shape as `C-0023`'s *"a confinement requirement is a force or a stiffness
> depending on the topology of what confines"*: **a stroke requirement and a stability requirement
> are the same inequality read on opposite sides of a placement.**

## Why this matters even though no verdict changes

Every desired-stroke verdict in the corpus stands. What changes is **where the failure lives**, and
therefore what would have to be fixed:

| claim | said the desired stroke fails because | this challenge says |
|---|---|---|
| `C-0029` | the arm cap `(c n EI/k)^(1/3)` is below 10 nm | true **on the acceptable stroke's placement**; on the desired clause's own placement the cap is 1.49× larger and the arm clears |
| `C-0034` | one link at the far end collapses the cap | unchanged; two links clear it at **either** placement |
| `C-0039` | the tangent at 10 nm is 264.2 pN/nm, 6.6× the ceiling, and delivering the stroke takes 699 pN | **both quantities are properties of a coupling placed for 3 nm.** At 10 pN/nm the tangent is 16.7–33.4 and the force is exactly 100 pN |
| `C-0040`, `C-0041` | the path count is bounded below at 34 by the unzip allowable at the desired stroke | **34 is `33.333 × 10/10`, a property of the placement.** On the desired clause's own placement the floor is **10** |
| `C-0046` | the crossover inventory caps the arm | unchanged at the standing placement; at the desired clause's placement the arms clear and **stability** binds instead |

**The 34-path floor is the concrete casualty.** It is quoted in three claims as though it were a
property of the material and the allowable; it is a property of a *placement convention*, and the
same allowable gives 10 under the other one.

## What would settle it

1. **A ruling from NDI on §3's own reading**: is the desired clause *"100 pN at a 10 nm stroke"* —
   in which case `k_c = 10 pN/nm` and stability refuses it at the 10 nm layer — or *"the stroke a
   33.3333 pN/nm coupling can travel"*, in which case the delivered force at full stroke is 333 pN
   and §3's force target is exceeded by design. **These are different devices and the specification
   does not distinguish them.** This is the third specification question this branch has raised,
   after `T-95` and `T-102`.
2. **`T-107`'s answer**, which decides only how badly the *first* reading misses; `C-0046` shows it
   does not move the verdict at either reading.
3. **`C-0017`'s stability floor at layer heights other than 10 nm**, where it is reported as
   **zero** and where the 10 pN/nm placement would not be refused — against `C-0016`'s standing
   verdict that the desired stroke is unreachable at every height and grafting density.

## What is NOT challenged

- Every number in every claim named above. All 14 reproductions in `C-0046` land at ≤ `5.9e−4`,
  and that against values quoted rounded to three digits.
- `C-0017`'s placement arithmetic or its stability clause — this challenge is built **out of** both.
- The verdict that §3's desired stroke is out of reach. Three routes said so before this one; this
  challenge relocates the reason, from the element to the actuator, and supplies a bound that
  survives any element.
