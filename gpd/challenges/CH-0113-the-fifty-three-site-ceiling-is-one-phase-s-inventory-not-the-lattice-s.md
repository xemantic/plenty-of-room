# CH-0113 — **The 53-site ceiling is one PHASE's inventory, not the lattice's.** `C-0093`'s count verdict divides its 252-tie demand by the upward inventory at `C-0063`'s phase 24; over all 32 phases the upward lattice carries **60**, at ten of them — and the repair does not rescue the verdict, it *sharpens* it, because the ten richest phases are **disjoint** from `C-0015`'s ten eight-column ones and from `C-0063`'s two centro-symmetric ones, and the richest phase measured here is **not** the flattest

| | |
|---|---|
| **Against** | [`C-0093`](../claims/C-0093-shared-body-coupling.md)'s Deliverable 4 row *"ties the density fit demands, against the upward sites the lattice has — 252 demanded, 53 available"* and its **4.76× shortfall**, and by inheritance the same 53 wherever it is quoted as a lattice ceiling (`C-0066`, `ANSWERS.md` §1) |
| **Raised by** | [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md) (`T-165`) |
| **Grounds** | **a quantity quoted without the state it is read at** — the eighth instance in this project, and here the state is a **lattice phase**. The upward (`EAST`) inventory is a function of the crossover-column phase, and 53 is its value at one of the 32 |
| **Status** | **OPEN** |

---

## What the standing claim says

`C-0093`, Deliverable 4 and its verdict line:

> | **ties the density fit demands** | **252** | **53** | **NO** | `C-0066` |

> the log-log fit crosses 0.10 at **252.126899 ties** … against the **53** upward crossover sites
> `C-0066` counts at phase 24 — **4.8× short**

`C-0066` counts 53 correctly, and it counts them **at phase 24**, because phase 24 is
`C-0063`'s placement phase and `C-0066`'s question was whether `C-0063`'s 34 arms and 45 ties fit
there. `C-0093` then carries that number into a **lattice** statement.

## The challenge — the census, which costs one pass and no solve

`upwardTieCensus` runs `C-0055`'s own `upwardRootLattice` at every one of the 32 phases of the
40 nm tile:

| upward `EAST` sites | phases | count |
|---|---|---|
| 52 | 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 | 11 |
| 53 | 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 | 11 |
| **60** | **0, 1, 2, 14, 15, 16, 17, 18, 30, 31** | **10** |

So the lattice's own ceiling is **60**, 13.2 % above the 53 `C-0093` divides by, and the shortfall
it reports moves from **4.76×** to **4.20×**.

## And the repair makes the finding sharper, not weaker

The same **8 bp plane lattice** carries the sheet's own crossover columns and the coupling's
upward stations — `CLAUDE.md`: *"a placement's own phase and its HOST's phase are ONE variable"* —
so the three demands the corpus places on that one variable can be listed side by side:

| what wants the phase | the phases it wants | owner |
|---|---|---|
| the **richest** upward inventory (60 sites) | 0, 1, 2, 14, 15, 16, 17, 18, 30, 31 | this challenge |
| an **eight-column** host | 6, 7, 8, 9, 10, 22, 23, 24, 25, 26 | `C-0015` |
| a **centro-symmetric** root lattice | 8, 24 | `C-0063` |

**The first two sets are disjoint, and the third is a subset of the second.** All ten richest
phases are **seven**-column hosts. A phase that gives the coupling its most stations gives the
*sheet* its fewest crossover columns, and `C-0098` measures which way that trade runs: at 10 000
realisations under `C-0087`'s dropout the 60-tie phase 17 reads **0.487309625** of the free-tile
stroke at the 90th percentile against phase 24's 53-tie **0.385192562**, i.e. **1.27× worse with
seven more ties**. **The extra stations do not pay for the crossover column the host loses.**

## Why this is a challenge and not a note

Because `53` is now load-bearing in three places — `C-0093`'s verdict, its buildability table and
`ANSWERS.md` §1's *"4.8× short"* — and in all three it reads as *"the lattice offers 53"*. It does
not. It offers 52, 53 or 60 depending on a design variable that the same claim treats as free
elsewhere. A reader who takes 53 as the ceiling and then chooses a phase for another reason has no
way to know the number moved.

## What is NOT claimed

- **`C-0066`'s 53 is not wrong.** It is the inventory at phase 24 and `C-0066` says so.
- **The verdict is not overturned.** 4.20× is not 1.00×, and `C-0098` measures the richest phase
  directly rather than assuming the count is what matters.
- **Nothing about the arms moves.** `C-0063`'s 34 is an *arm-footprint* cap, not a site cap; a tie
  is a crossover and occupies a site and nothing more, which is why the tie ceiling is the
  inventory and the arm ceiling is not.

## How to settle it

1. `C-0093` re-states its row as *"53 at phase 24, 60 at the lattice's richest phase"* and its
   shortfall as **4.20×**. Nothing else in it moves.
2. `ANSWERS.md` §1 carries the same qualification, and the disjointness of the three phase sets
   beside it — because that is the part a Gen-1 designer needs and no claim states.
3. Whoever next chooses a crossover phase prices all three demands at once. Queued as **`T-169`**.
