# CH-0081 — A truss standoff is a **root**, and a rigid root demands a **LONGER** arm, not a shorter one: at 34 paths a clamped-root, free-tip element is **9.247 nm** against an **8.19 nm** plan budget and places **24 of 34**, so the truss array that places 34 times as a standoff cannot carry an output element at all — the two-support flexure it was built to cap is refused by **2.74×** and the end-loaded arm it could cap is refused by **12.9 %**

| | |
|---|---|
| **Raised by** | [`C-0069`](../claims/C-0069-output-element-placement.md) (`T-133`) |
| **Against** | [`C-0048`](../claims/C-0048-truss-cap.md)'s, [`C-0062`](../claims/C-0062-crossbar-trio-existence.md)'s and [`C-0065`](../claims/C-0065-crossbar-array-placement.md)'s truss branch — specifically the premise that a triangulated standoff array is a **useful** foundation for the Gen-1 output coupling, which `C-0065` states as *"the truss standoff repeats 34 times; the element it caps does not"* and leaves as its open item 3 |
| **Grounds** | **the truss's whole purpose is to be stiff, and stiffness at the root is what makes the element too long to place.** A bending element's plan length is `(c EI/k)^(1/3)` and `c` is a monotone increasing function of **both** end restraints (`C-0034`'s `c(ρ_n, ρ_f)`). A truss cap is a *rigid* root by construction. So the truss does not merely fail to help the element it caps — it is the thing that pushes it outside the budget |
| **Severity** | **the truss branch's output stage, not the truss itself.** `C-0065`'s placement result is untouched and every one of its numbers reproduces; `C-0062`'s trios still close and still place 34 times; what falls is the reading that a standoff array is the foundation the Gen-1 coupling wants. The element that places needs a **compliant** root — one antiparallel crossover at 13.53 pN·nm/rad, against a ceiling of **13.93** |

---

## What is claimed upstream

`C-0048` designs a truss cap as a **stiff** standoff head. `C-0062` searches 49 857 crossbar lattices and finds 609 closing trios. `C-0065` composes them with `C-0063`'s placement and finds that **all 44 of 44 recorded trios place 34 times, 0 overlaps, one level**, and reports:

> *"the flexure is the other story and is reported apart: with `C-0030`'s span the same array covers **1.84×** the footprint, needs **7 levels**, and places **12 of 34** — `C-0041`'s obstruction at 34 paths, independent of every trio."*

and, as its open item 3:

> *"whether a **different** output element — one that does not lie in plan — escapes it is the question the truss branch now hangs on."*

**Every one of those numbers reproduces in `C-0069`**: the 27.4119472 nm span at departure `0.0` and the **12 of 34** at departure `0.0`, through `C-0065`'s own `placeTrussArray`.

## What the composition finds

The plan budget for a **rooted** element on any 34-root placement of the upward lattice is `pitch − d = 8.19 nm`, exactly — `3a + 2(15 − a) = 34` forces four rows of three (`C-0063`'s own bound 1), and three roots at a 10.88 nm pitch cap a rooted element at the bare pitch minus one duplex. Against that budget:

| what the truss could cap | its family | length at 34 paths | against 8.19 nm | placed |
|---|---|---|---|---|
| `C-0030`'s coupled flexure, across the rows (`C-0065`'s own reading) | supported twice, loaded at midspan | **27.412 nm** | **3.35×** | **12 of 34** |
| the same, along the rows | the same | 27.412 | 3.35× | 23 of 34 |
| `C-0023`'s `E3a` at its **softest** end condition | the same | **22.414 nm** | **2.74×** | 23 of 34 |
| **an end-loaded arm on a RIGID root with a free tip** | supported once, loaded at the far end | **9.247 nm** | **1.129×** | **24 of 34** |
| the same arm on **one antiparallel crossover** | the same | **8.164 nm** | **0.997×** | **34 of 34** |

**Two statements, and the second is the challenge.**

1. **The two-support family is refused at every span, every end joint and every placement.** Its `c` is bounded below by 48 (`C-0025`), so its shortest possible member is `(48 EI/k₁)^(1/3) = 22.414 nm`, **2.74×** the budget. That is strictly stronger than `C-0065`'s 12 of 34, which is a count on one placement; this is a bound on the family.
2. **The one-support family is admitted — but only with a COMPLIANT root.** Bisecting `C-0039`'s exact elastica at `C-0034`'s `A2` tip gives a **root ceiling of 13.930 pN·nm/rad**. One antiparallel crossover is **13.529** — 2.9 % inside. A truss cap is far above it: even the *idealised* rigid root (`c = 3` exactly in the small-rotation limit, 3.37 solved) asks for **9.247 nm**, **12.9 % past** the budget.

> **The truss's own virtue is the defect.** `C-0048` and `C-0062` spend the whole trio search on making a base that does not rotate; `C-0069` shows that a base which does not rotate forces `c` up, and `c^(1/3)` forces the plan length up with it. The correction runs the **unfavourable** way for the branch and the **favourable** way for the design, because the design already uses the compliant root.

## What this does NOT challenge

- **`C-0065`'s placement result stands entirely.** 44 of 44 trios place 34 times, 0 overlaps, one level; the register finding, the 9 bp row, the 18.0° base and the 0.0780 dishing are untouched and none of them is re-derived here.
- **`C-0062`'s closure search stands.** 609 closing trios, 196 band closures, 44 recorded trios — reproduced by `C-0065` and not disturbed.
- **`C-0048`'s cap mechanics stand.** Nothing here says a truss cap is badly designed; it says it is the **wrong end condition** for the one element family the plan admits.
- **The truss may still be the right part for something else** — a rigid standoff is exactly what a *support* wants, and `CH-0067` and `CH-0082` are about its own internals. This challenge is about what it **caps**.

## What would settle it

1. **A rooted element family whose `c` falls as its root stiffens.** None exists: `c(ρ_n, ρ_f)` is monotone increasing in both arguments, from 0 at the free-free mechanism to 12 at the guided pair (`C-0034`, asserted at all four textbook corners by `C-0039` to `1.7e−14`).
2. **A footprint convention in which consecutive collinear elements need no clearance.** The budget would rise from 8.19 nm to the bare 10.88 nm pitch, and the rigid-root arm at 9.247 nm would then place. **This is the single largest lever on the challenge** and the convention is `C-0053`'s, inherited by `C-0055`, `C-0063`, `C-0065` and `C-0066` alike — so moving it would move all of them, not just this.
3. **A larger tile or a different path count.** At 15 paths the rigid-root arm is shorter; at 45 it is longer. But 34 is `C-0055`'s self-consistent count and it is what fixes the stations.
4. **A truss cap that is deliberately made compliant about one axis** — which is `C-0028`'s 9.65× orientation finding read backwards, and which nobody has designed. It would have to land inside 13.93 pN·nm/rad, i.e. **below one antiparallel crossover's own 13.53 plus 3 %**, which is a very small target for a triangulated body.

## Status

**OPEN.** Filed with `C-0069` (`T-133`), against the truss branch's output stage. The numbers are in `gpd/results/T-133-output-element-placement.json` (`candidates` rows `Q1`–`Q9`, `window` row *"near restraint at `C-0034`'s A2 tip"*) and every upstream figure it rests on is reproduced there.
