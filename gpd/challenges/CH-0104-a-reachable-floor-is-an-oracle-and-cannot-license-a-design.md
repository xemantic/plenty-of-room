# CH-0104 — **A reachable dishing floor is an ORACLE bound, and three claims read it as a licence.** *"The negative belongs to the equal springs, not to the station set — the least-squares floor over every phase-24 upward root is 0.00071"* is true of a force vector chosen **after** the load is known, and a coupling is specified before it is folded. Under `C-0087`'s measured dropout the same floor is **0.00111473343** on a 90-path grid where the best **fixed** distribution reaches **0.284537599** — a gap of **255×** — so the floor licenses nothing about a buildable design and the whole failure of `T-155` lives in that gap

| | |
|---|---|
| **Raised by** | [`C-0089`](../claims/C-0089-dropout-robust-placement.md) (`T-155`) |
| **Against** | [`C-0074`](../claims/C-0074-two-per-row-placement.md)'s *"the negative belongs to the equal springs, not to the station set: the least-squares floor over every phase-24 upward root is **0.00071**, 140× below the convention"*, and by inheritance the same construction in [`C-0058`](../claims/C-0058-non-uniform-coupling.md) (`InfluenceSurrogate.reachableDishingFloor`, *"the honest statement of how much room the search may have left"*) and in [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md)'s falsifier `F4` |
| **Grounds** | **the floor optimises over force vectors, and a design chooses a STIFFNESS vector once.** `reachableDishingFloor` is an ordinary linear least squares over `ℝⁿ` at a station set: it asks *"what is the flattest field any set of support forces at these stations could leave?"*. A stiffness distribution does not choose forces — the forces are the solution of a coupled system, and under fabrication the **support set itself** is random. `C-0089` measures the gap between the two on the same station sets and the same 10 000 realisations: the p90 oracle floor is **0.00111473343** at 90 paths against a best achievable fixed distribution of **0.284537599**, and **0.00917** on `C-0063`'s own 34 roots against a best of **0.449584219** |
| **Severity** | **a class error in how a bound is read, which is inert at zero defects and decisive under fabrication.** `C-0058`'s and `C-0087`'s uses are correct as stated — both call it a *bound* and neither claims a design. `C-0074` goes one step further and reads it as evidence about where a negative *belongs*, and that step does not survive a random support set. **No published number is wrong**; what is wrong is the inference from a small floor to *"a distribution exists"* |

---

## What is claimed upstream

`C-0058` (`T-113`) introduces the construction and states it carefully:

> *"**The bound.** The smallest root-mean-square dishing any set of forces at these stations can
> leave — and therefore a rigorous lower bound on the peak dishing of every stiffness distribution
> whatever … This is the cheap bound that says in advance whether the expensive search can possibly
> reach `T-5b`'s tolerance."*

That is exact, and as a **bound** it is used correctly there and in `C-0087`, whose falsifier `F4`
reads *"the reachable floor over the full station sets already exceeds 0.10, making the Monte Carlo
unnecessary — did not fire, 0.00548"*.

`C-0074` (`T-136`) takes the further step:

> *"**But the negative belongs to the equal springs, not to the station set**: the least-squares
> floor over every phase-24 upward root is **0.00071**, and a **distribution** at `C-0017`'s
> unchanged total puts six of eight priced placements inside 0.10."*

At zero defects that sentence is supported — by the *second* clause, which is a search over real
distributions, not by the first. The trouble is that the first clause has since travelled: it is
the sentence a reader takes away, and it is the form of argument a fabrication-aware reader will
reach for next.

## What `C-0089` measures

Same station sets, same `C-0022` load, `C-0087`'s measured incorporation map, 10 000 seeded
realisations, and the floor evaluated **over each realisation's surviving stations**
(`InfluenceSurrogate.reachableDishingFloorAt`, added by `T-155`):

| station set | paths | oracle p90 floor | **best FIXED distribution, p90** | ratio |
|---|---|---|---|---|
| 1 × 15 grid | 15 | 0.01988 | 0.67415521 | **33.9×** |
| 3 × 15 grid | 45 | 0.00486 | 0.363481976 | **74.8×** |
| **6 × 15 grid** | **90** | **0.00111** | **0.284537599** | **255×** |
| `C-0063`'s roots | 34 | 0.00917 | 0.449584219 | **49.0×** |
| `C-0074`'s roots | 30 | 0.01434 | 0.547929633 | **38.2×** |

The oracle is allowed a **different distribution for every tile it builds**. A builder is allowed
one, specified before folding. **That is the entire content of `T-155`'s negative**, and the floor
is what makes it visible: nothing here is refused by geometry, because the geometry admits a flat
answer at every count. What is refused is a *fixed* answer.

Note also that the gap **widens with the path count** — 33.9× at 15 paths, 255× at 90 — which is
the opposite of the intuition that a denser station set makes the floor more informative about a
design. A denser set gives the oracle more freedom faster than it gives a fixed distribution.

## Why this is a challenge and not a note

Because the inference is about to be needed. `C-0089` closes the flatness branch under
fabrication, and the natural next move — the one an agent or a reviewer will make — is to observe
that the reachable floor is tiny and conclude that a cleverer distribution must exist. **It does
not**: the floor is the *oracle's* value and the gap to a fixed distribution is two orders of
magnitude. Writing that down before it is used is cheaper than un-writing it after.

`CLAUDE.md` carries the neighbouring discipline already — *"a saturation measured inside one
family is not a floor of the object — before calling a limit irreducible, name the family it was
swept in"* — and this is its mirror: **before calling a floor reachable, name what is allowed to
vary to reach it.**

## What is NOT claimed

- **`C-0058`'s and `C-0087`'s uses are not challenged.** Both call it a bound and use it as one.
- **`C-0074`'s zero-defect conclusion is not withdrawn.** Its *"six of eight priced placements
  inside 0.10"* is a search over real 30-parameter distributions and stands on its own legs; the
  floor sentence is a redundant support for it, not its ground.
- **The floor is not useless.** It is exactly the right instrument for the question it answers,
  which is *"can the expensive search possibly succeed?"* — it can **exclude** and can never
  **admit**, and `C-0089` reports it in that role (falsifier `F5`, did not fire).

## What would settle it

1. **A one-line qualification on the construction itself** — in `NonUniformCoupling.kt`'s KDoc and
   in any claim that quotes a floor: *the floor is attained by a force vector chosen with knowledge
   of the load and of the surviving support set; it bounds a design from below and never
   characterises one.*
2. **The gap quoted beside the floor** wherever a floor is quoted. `C-0089` emits it for ten
   station sets and it runs 33.9–255×.
3. **A `C-0074`-style re-read at zero defects.** Even with no dropout at all the floor sits two
   orders of magnitude below what a searched distribution reaches — its own **0.00071** against
   the **0.0648 – 0.1726** its own 30-parameter search returns — so the inference was never as
   strong as the sentence reads, and the dropout only widens a gap that was already there.

## Suggested disposition

**Amend the sentence, not the number.** `C-0074`'s claim line should read *"the negative belongs to
the equal springs and not to the station set — a 30-parameter distribution puts six of eight
priced placements inside 0.10"*, with the floor quoted as a bound rather than as the reason. And
add the oracle/fixed gap to the KDoc of `reachableDishingFloor` so the next reader cannot make the
step this challenge is about.
