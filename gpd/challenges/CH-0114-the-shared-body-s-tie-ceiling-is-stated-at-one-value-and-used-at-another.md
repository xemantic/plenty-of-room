# CH-0114 — **`C-0093` states its tie ceiling at 3.33333333 pN/nm and reports its headline at 1000.** Its Bound 2 makes `C-0049`'s `a/s` *"the escape"* — the number the whole topology argument turns on — and every graded cell it publishes then runs at 3× to 300× past it. Both readings are defensible and they are not the same reading: `a/s` is derived on an ARRAY path, whose extension **is** the stroke, and a shared body's tie extension is not

| | |
|---|---|
| **Against** | [`C-0093`](../claims/C-0093-shared-body-coupling.md)'s **Bound 2** (*"the mandate arithmetic, which is the escape in one division"*) and the `sharedBodyPerStation` column of `mandatePlacementArithmetic` in `src/main/kotlin/coupling/SharedBodyCoupling.kt` |
| **Raised by** | [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md) (`T-165`) |
| **Grounds** | **an inherited bound applied outside the topology it was derived in**, and a claim whose stated cap and reported design disagree by 300× without the disagreement being named |
| **Status** | **OPEN** |

---

## What the standing claim says

`C-0093`, Bound 2, in full:

> | **34** | **0.980392157** | **3.33333333** | **3.4** |
>
> **The ties are then capped by `C-0049`'s per-path force, not by the mandate** — and the ratio
> *grows* with the path count instead of shrinking

and its claim line:

> the tie is capped by `C-0049`'s per-path **force**, 3.33333333 pN/nm, **3.4×** more local support

Every graded cell in the same claim, including its headline **0.24028028**, is read at ties of
**1000 pN/nm**, and its own tie ladder runs 3.33 / 10 / 100 / 1000. The headline is 300× the cap
the claim states.

## Why both readings exist, and why they are different

`C-0049`'s ceiling is `a/s` — an allowable **force** divided by a **stroke** — and it becomes a
stiffness only under the assumption that the path extends by the whole stroke. On an array that is
exactly right: a path runs from the tile to ground and the tile moves `s`, so the path stretches
`s` and carries `k·s`.

**Under a shared body it is not right, and the reason is the same division `C-0093` itself
identifies.** The body heaves with the tile — that is what puts 99.90 % of the coupling's
compliance in the *ground* — so a tie's **relative** extension is not the stroke but the
station's departure from the body's plane, which is a dishing-scale quantity. The force allowable
is therefore a constraint on the **solved** tie force and not on the tie stiffness, and the two
differ by whatever fraction of the stroke the tie actually takes.

`C-0093` knows this: it emits `peakTieForceUnderDropout` beside every cell and reports 9.01 pN at
the winner, inside `C-0006`'s 10 pN. What it does not do is reconcile that with the number its own
cheap bound calls *"the escape"*.

## What it is worth

`T-165` runs the two readings as two families of the same descent, on the same 53 real upward
stations at phase 24, under `C-0087`'s measured dropout at 10 000 realisations:

| tie cap | reading | **90th percentile** | zero defects | peak tie force under dropout |
|---|---|---|---|---|
| **3.33333333 pN/nm** | `C-0049`'s `a/s` read as a stiffness | **0.522220166** | 0.0635501261 | **5.7708004 pN**, inside 10 |
| **1000 pN/nm** | the solved force, checked against the allowable | **0.385192562** | **0.0173449294** | **10.5108848 pN**, past 10, inside 48 |

**The conservative reading costs 1.36× of flatness at the 90th percentile and 3.67× at zero
defects, and it buys 4.7 pN of headroom nothing asks for.** It is not conservatism, it is a
different topology's constraint applied by inheritance. The stiff reading is admissible on the
48 pN **shear** allowable and 3 % past the 10 pN **unzip** one, so what the honest bound decides
is a **bond geometry**, not a stiffness — and that is a design statement `C-0093`'s table cannot
make because its cap is not a solved quantity.

## Why this is a challenge and not a note

Because Bound 2 is not decoration: it is the claim's *explanation* of why a shared body is flatter
than an array, and the explanation is quoted in `ANSWERS.md` §1 (*"frees each tie from 0.98 to
**3.33 pN/nm**"*). A reader who takes 3.33 as the design tie gets a materially worse coupling than
the one `C-0093` measured, and a reader who takes 1000 has no stated warrant for it.

## What is NOT claimed

- **No published number is wrong.** `C-0093`'s 0.24028028 is read at 1000 pN/nm and its claim
  block says so; its 3.33333333 is `C-0049`'s `a/s` and is correctly computed.
- **`C-0049` is not challenged.** Its bound is a bound on a **force** and it says so; what is
  challenged is reading it as a bound on a **stiffness** under a topology in which the path's
  extension is not the stroke.
- **The verdict does not move.** Neither reading reaches `T-5b`'s 0.10.

## How to settle it

1. `C-0093` re-labels its Bound 2 column as *"the tie stiffness at which an ARRAY path reaches the
   allowable"* and states, where its headline is quoted, that the binding constraint under a
   shared body is the **solved** tie force. Then nothing numeric moves.
2. The per-path allowable is re-derived for a tie whose extension is the station's departure from
   the body's plane, which would give a genuine shared-body stiffness cap rather than an inherited
   one. Nothing in this corpus does that, and it is the honest version of Bound 2.
3. `ANSWERS.md` §1's *"frees each tie from 0.98 to 3.33 pN/nm"* becomes *"frees each tie from a
   0.98 pN/nm budget to a 10 pN force allowable, which the solved design meets at 1000 pN/nm"*.
