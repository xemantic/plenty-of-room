# CH-0055 — "45 load paths on `C-0015`'s 3 × 15 grid" is a premise the geometry does not admit, and a tie-aperture floor quoted as an area is answering the wrong question

| | |
|---|---|
| **Against** | [`C-0023`](../claims/C-0023-two-sided-coupling.md), [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) and [`C-0035`](../claims/C-0035-flexure-mounting-sense.md) — the shared `Conditions` line *"45 load paths on `C-0015`'s 3 × 15 grid"*, and `C-0035`'s Deliverable 3 aperture **floor** |
| **Raised by** | [`C-0041`](../claims/C-0041-flexure-array-packing.md), task [`T-96`](../tasks/T-96-flexure-array-packing.md) |
| **Grounds** | **methodological** — a design point carried as a condition through four claims without a plan view, and a connectivity question priced as an area |
| **Status** | **RAISED.** Every number in all three claims reproduces, and none of their per-element physics is disturbed. What does not survive is the **count**, and with it the design point every one of them is quoted at. |

---

## What the three claims say

`C-0023`, `C-0025`, `C-0028`, `C-0030` and `C-0035` all carry, in their `Conditions` line:

> *"**45 load paths on `C-0015`'s 3 × 15 grid**"*

`C-0030` states it as an explicit assumption and hands the array on:

> *"**One flexure per load path and 45 attachments**, exactly as `C-0023`, `C-0025` and `C-0028` assume. The array is `T-31`'s and the lever `T-33`'s."*

and `C-0035` prices the tie apertures as an area:

> *"So the base body needs **45 duplex-omission holes** whatever the stroke: `45 × 2.69² = 326 nm²`, **20.4 % of the tile footprint** — … the irreducible part of `T-78`'s answer."*

It also records, correctly, that the packing is unsolved — which is what makes this a challenge to the **premise** rather than to the claim's honesty:

> *"**The array packing is not solved.** … so every aperture-area fraction is a **scale** against the tile footprint and not a layout. `T-31` owns the array, and it owns this too."*

---

## Ground 1 — the 45-path array has no plan view, at any level count and on any body

`C-0041` places the array exactly, on the measured lattice constants, with the midspans pinned where the ties put them.

| columns | `n` | span | feasible orientations of 720 | single-level | minimum mutual blocks |
|---|---|---|---|---|---|
| **1** | **15** | **21.44 nm** | **1** | **1** | **0** |
| 2 | 30 | 27.51 nm | **0** | 0 | 15 |
| **3** | **45** | **31.82 nm** | **0** | **0** | **45** |

Two lattice facts, and they meet nowhere:

- **The attachment grid's across-helix pitch IS one duplex.** `C-0015`'s rows sit at exactly 2.69 nm, which is the width a beam occupies, so two beams in the same column and adjacent rows are **tangent at zero tilt** and at any other angle their perpendicular separation is `2.69 cos θ < 2.69` — each covers the other's tie, mutually, at any level count. **Fact A forces `θ = 0`.**
- **The along-helix pitch is under the span.** Two collinear beams need `|Δx| ≥ span + d` — not `≥ span`, because their standoff **feet sit on the beam ends**, so beams laid end to end put two standoffs in the same place. That is **34.51 nm against a 13.33 nm column pitch**. **Fact B fails at `θ = 0`.**

**And stacking is not available**, which is the part an area budget cannot see. A standoff runs from the superstructure up to its own beam plane and a tie runs from that plane down to the tile, so **any two vertical members of the array share a height range whatever levels their beams sit at**. The clash is **level-independent**: no ordering, no level count and no larger body resolves it. `C-0017`'s envelope does admit three beam planes at §3's acceptable stroke — 5.78 / 7.82 / 9.86 nm — and they buy nothing.

> **The array does not fail to pack. It fails to stand up.** The obstruction is the *legs*, not the beams: the beams' own bodies would clear each other in three levels by area.

## Ground 2 — the count is not a free parameter of the four claims, and 45 is unavailable

`C-0023` establishes, correctly and importantly, that **the path count is set by the per-path allowable and not by the stiffness**, and reaches 45 by three independent routes; `CH-0029` adds a fourth. **None of the four asks whether the count can be placed.**

It cannot. The Gen-1 tile carries **exactly fifteen** — solved self-consistently, because `L ∝ n^(1/3)` moves the span at every candidate count. So the design point every one of the four claims is quoted at does not exist, and the one that does is `1 × 15`:

| | `C-0023`/`C-0030`/`C-0035`'s point | `C-0041`'s |
|---|---|---|
| paths | 45 | **15** |
| grid | 3 × 15 | **1 × 15**, ties staggered 8 bp |
| span | 31.82 nm = 94 bp | **21.44 nm = 63 bp** |
| assembled tangent at 3 nm | 25.23 pN/nm | **25.49 pN/nm** |
| per path at 3 nm | 2.22 pN | **6.67 pN** (against a 10 pN allowable) |
| per path at 10 nm | 6.63 pN | **18.23 pN** — past it |
| buckling margin at 3 nm | 6.49 / 5.12 | **2.16 / 1.71** |

**What survives is almost everything.** The assembled tangent moves by 1 % across the whole range 10–60 paths, because the span is *placed*: the count moves the length, not the stiffness. What does **not** survive is §3's **desired** stroke, which at 15 paths is 1.82× past the unzip allowable and which the same allowable bounds below at **29** paths — so on the specified tile the window between the two bounds is **empty**.

## Ground 3 — a tie-aperture floor quoted as an AREA is answering the wrong question

`C-0035`'s **326 nm², 20.4 %** is arithmetically right and is not the question a sheet asks. The holes lie on the attachment grid, whose across-helix pitch is **exactly one duplex**, so a column of ties does not punch 15 holes — **it removes a line of material**.

| layout | helices | duplexes | segments | **components** | |
|---|---|---|---|---|---|
| 1 × 15 | along `x` | 15 | 30 | **2** | SEVERED |
| **3 × 15** | **along `x`** | **15** | **60** | **18** | **SEVERED** |
| 3 × 15 | across `x` | 15 | 12 | **4** | SEVERED |

**Every duplex is cut into four pieces and the superstructure falls into 18 disconnected components** — at every one of `C-0015`'s 32 crossover phases, so it is not a phase artefact. With the helices running across `x` it is worse in kind: three whole duplexes are obliterated and the body falls into four strips.

**A 20.4 % area loss sounds like a stiffness correction. A severed body is not a stiffness correction.**

---

## What this challenge does NOT claim

- **It does not claim any number in `C-0023`, `C-0030` or `C-0035` is wrong.** All of them reproduce here — `C-0030`'s span to `2.9e−5`, its tangent to `1.1e−4`, its critical load to `3.9e−4`, its clearance to `1.7e−16`; `C-0035`'s slot to `9.6e−5` and all three of its areas to better than `1.2e−3`.
- **It does not disturb the per-element physics.** The coupled joint, the supplied draw-in, the mounting identity and the aperture shape are all re-run as a library and are untouched.
- **It does not reopen `C-0023`'s rule that the count is set by the allowable.** It adds a *second* bound, from above, that the rule did not have — and the two now cross.
- **It does not claim the flexure branch is dead.** At §3's **acceptable** clause it is alive, on 15 paths, with 1.50× of allowable margin and 2.16× of buckling margin.

## The remedy, stated as a threshold

| what must give | by how much |
|---|---|
| **the path count**, for §3's **acceptable** stroke | **45 → 15**, at no cost against any standing allowable |
| **the tile footprint**, for §3's **desired** stroke | **≥ 2330 nm², 1.44× the Gen-1 tile, 1.20× in edge** — and `C-0022` says the rim is *cheaper* there, +6.3 % against +14.7 % |
| **the tie column**, for the superstructure's connectivity | **8 bp = one duplex pitch** of stagger, free of every upstream claim because `C-0026` fixes the attachment **rows** and not the positions along them |

## How this challenge would be answered

1. **A tie that is not vertical**, which unpins the midspans from the attachment grid — but also breaks `C-0035`'s `dδ/ds = ±1` identity, which assumes the tie transmits the two bodies' separation change.
2. **A flexure tied somewhere other than its midspan**, which would require re-solving the symmetric beam all four claims share.
3. **An attachment grid on a coarser row pitch**, which relieves Fact A at the price of `C-0026`'s exact zero.
4. **A demonstration that two duplexes may sit closer than 2.69 nm in plan.** The verdict is 5× from that choice.
