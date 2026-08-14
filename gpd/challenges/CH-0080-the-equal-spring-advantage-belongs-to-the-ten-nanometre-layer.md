# CH-0080 — `C-0063`'s *"it needs no distribution at all"* is a statement about a **10 nm layer's load field**, not about the placement: over the range of every 10 nm device the 34 equal springs are flat (0.0789 / 0.0853 / 0.0896), and against a **5 nm** layer the same 34 roots dish **0.2000** and are **worse than no coupling at all at both of that device's states**

| | |
|---|---|
| **Against** | [`C-0063`](../claims/C-0063-upward-root-placement.md)'s **Deliverable 2** — *"the distribution … is not needed"*, and the verdict sentence *"it needs **no distribution at all**: it is 34 **equal** springs summing to `C-0017`'s unchanged 33.3333 pN/nm"* — as that sentence is carried forward by [`C-0065`](../claims/C-0065-crossbar-array-placement.md) and [`C-0066`](../claims/C-0066-arm-slab-tie-clearance.md) |
| **Raised by** | [`C-0068`](../claims/C-0068-range-robust-placement.md) (`T-129`) |
| **Date** | 2026-08-14 |
| **Grounds** | **conditions, not arithmetic.** Every number in `C-0063` reproduces here — its **0.0706145537** to the last emitted digit on its own host at its own state, its rim-rule reversal, its 3.40 admissible ratio, `C-0061`'s 0.4156 and `C-0022`'s 0.3079 — and its placement is *strengthened*: it is the argmin of the **range** objective as well as of the single-state one, with **0 of 198 288** exhaustively enumerated centro-symmetric placements beating it. What is challenged is the **scope** of the equal-spring sufficiency |
| **Direction** | **narrowing, and mostly favourable.** `C-0063`'s headline survives over the whole stroke of the device the programme actually places (`C-0018`/`C-0032`), which is more than its own claim asserts; what it does not survive is a **change of layer height** |
| **Status** | raised. **No count, no phase, no placement and no force budget of `C-0063` moves.** What moves is the word *"needs"* — from a property of the placement to a property of the `(placement, layer)` pair |

---

## What is challenged

`C-0063` concludes, of its swept 34-root placement:

> *"**What the optimiser wants is almost nothing**: a peak of **1.30×** the uniform share, for 13.9 %.
> … it needs **no distribution at all**."*

and its verdict line says the tile is flat *"with **equal springs**"* — the sentence
`C-0065` and `C-0066` both carry as the standing description of the Gen-1 coupling.

That is **true at the state it is read at, and over the whole traversed range of every 10 nm device**.
It is **false against a 5 nm layer**, and `T-129` measures by how much.

---

## Ground 1 — over a 10 nm device's own range, the sentence holds, and better than `C-0063` claims

`C-0022` solved both ends of the stroke `C-0018`'s placed device traverses — gaps 10 → 7 nm at its own
0.192 V — and both ends of the two alternative buffers, bracketed at 7 nm by the neighbouring solved
biases.

| device | equal springs over its range | flat under `T-5b`'s 0.10? |
|---|---|---|
| 2 mM, `L₀` = 10 nm, 0.192 V (`C-0018`'s placed device) | **0.0789** | **YES** |
| 0.5 mM, `L₀` = 10 nm, 0.134 V (`C-0032`'s recommendation) | **0.0853** | **YES** |
| 10 mM, `L₀` = 10 nm, 0.192 V | **0.0896** | **YES** |

So `C-0063`'s exposure — *"ONE load state … nothing here says it survives them"* — is discharged in the
favourable direction for every device built on a 10 nm layer. The cost is the **margin**: 1.42× at the
single state becomes **1.12×** at the tightest range.

## Ground 2 — against a 5 nm layer the same 34 roots are a dishing SOURCE

| state | free tile | 34 equal springs | coupled/free |
|---|---|---|---|
| 2 mM, **5 nm**, 0.368 V (that device's **rest** state) | **0.0638** | **0.1104** | **1.73× WORSE** |
| 2 mM, **2 nm**, 0.368 V (the same device **held** at §3's 3 nm) | **0.1648** | **0.2000** | **1.21× WORSE** |

Both readings fail `T-5b`'s 0.10, and both fail `C-0047`'s *"beats no coupling at all"* bar — which is
the very test `C-0063` used to convict `C-0055`'s greedy placement (0.4156 against 0.3079) and to
acquit its own. **On a 5 nm layer, `C-0063`'s placement is in the class it convicted.**

The mechanism is not mysterious and it is the same one `C-0063` names: **a placement is tuned to a load
field.** A 5 nm layer's solved collar is a much weaker perturbation — the free tile dishes **4.8×** less
at its rest state than a 10 nm layer's does — so there is far less for the coupling to correct, and what
remains is the coupling's own sag at 34 discrete points.

And the two states of that one device are **anti-parallel to each other**: the cosine between their
free-tile dishing fields is **−0.9427**, where every 10 nm device's own pair runs **+0.9969 to +0.9998**.
`C-0064` located this sign for the 2 nm state against the *10 nm* states; on these stations it appears
**inside a single device's range**.

## Ground 3 — there are two remedies, both cheap, and the second is the interesting one

**A distribution.** The 34-parameter minimax over the 5 nm device's own range reaches **0.0565**, inside
the convention, at a peak ratio of **2.32** — comfortably below the 5:1 `C-0060` prices as buildable, and
below the 3.40 admissible ratio `C-0063` itself computes at 34 paths.

**Or a different placement, at a different phase.** Re-enumerating the centro-symmetric family
exhaustively under the 5 nm device's own range objective:

| phase | enumerated | best with **equal springs** | flat? | that winner at the 10 nm design state |
|---|---|---|---|---|
| 24 — `C-0063`'s | 198 288 | **0.1169** | **NO — nothing at this phase clears** | 0.2986 |
| **8** | 163 296 | **0.0895** | **YES** | **0.2416** |

So the array **can** serve a 5 nm layer with equal springs — at phase **8**, not at `C-0063`'s phase 24 —
and the placement that does it is poor for the 10 nm device. **The equal-spring property is a property of
the `(placement, phase, layer)` triple, and `C-0063`'s is the 10 nm member of it.** This is a scope
correction, not a failure.

---

## What this does *not* challenge

- **`C-0063`'s placement**, which `C-0068` re-derives as the **range argmin** of its own family.
- **Any number in `C-0063`** — twelve of its readings are reproduced, the tightest exactly.
- **`C-0063`'s resolution of `CH-0074`**, which is a statement about the design point and about
  station sets, and which `C-0068` upholds for every 10 nm device.
- **`C-0063`'s finding that `C-0058`'s rim rule reverses sign on this station set**, which `C-0068`
  reproduces over a whole range rather than at a state.

## The remedy proposed

Annotate `C-0063`'s Deliverable 2 and its verdict line in place, per `gpd/README.md`, to read:

> **It needs no distribution at all *against a 10 nm layer***: 34 equal springs are flat at the design
> state (0.0706) and over the whole traversed range of all three buffers `C-0022` solved
> (0.0789 / 0.0853 / 0.0896). Against §3's **5 nm** layer the same placement dishes **0.2000** over that
> device's range and is worse than no coupling at all at both of its states; a 34-parameter distribution
> at a peak ratio of 2.32 restores it to **0.0565**, and a *different* placement — phase **8**, equal
> springs — reaches **0.0895** where nothing at phase 24 clears at all.

## What would overturn this challenge

1. **A `C-0022` 5 nm load profile materially different from the solved one.** Its rim charge is unsourced
   and worth 1.85× on the collar, and the 5 nm states are a small difference of larger numbers.
2. **A demonstration that no Gen-1 device is built on a 5 nm layer.** §3 names 5 / 7 / 10 nm and
   `C-0018`/`C-0032` place the design device at 10 nm, but nothing says the 5 nm one is excluded — and
   `C-0068` names that as a specification gap rather than a modelling one.
3. **A single placement flat over both layers.** `C-0068` finds one for each layer separately and finds
   their argmins mutually poor; the 22 **asymmetric** phases are enumerated by neither claim, and a joint
   placement-and-distribution search has never been run.
