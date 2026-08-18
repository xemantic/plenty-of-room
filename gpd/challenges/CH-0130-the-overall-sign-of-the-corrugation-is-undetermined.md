# CH-0130 — the **overall sign** of `C-0107`'s corrugated field is not fixed by anything in this repository, so the graded field is **two** states and only one was read

| | |
|---|---|
| **Against** | [`C-0107`](../claims/C-0107-row-end-prestrain-value.md), Deliverable 3 (*"the sign … is a lattice congruence"*) and Deliverable 4's single graded row |
| **Raised by** | [`C-0112`](../claims/C-0112-interior-crossover-prestrain.md) (`T-190`) |
| **Kind** | **completeness** — a swept axis left at one of its two values |
| **Status** | **OPEN, and it is DISCHARGED on the number**: `T-190` reads the missing state at **0.0910197** of the free stroke, flat, so the verdict survives at both signs. What does not survive is the claim that the sign was *determined* |

---

## The ground

`C-0107` Deliverable 3 composes two alternations and concludes:

> *"**The two `(−1)^b` cancel.** `θ₀(b) = (−1)^b · (−1)^b u_max = +u_max`, every interface, the
> **same sign** — which is `C-0104`'s **uniform** distribution."*

The composition is right and it settles a **relative** question: whether the 14 row-end
crossovers share a sign. It does **not** settle the **absolute** one. The glide symmetry says the
corrugation alternates with the interface index; it does not say which parity folds up. Relabelling
the interfaces by one (`b → b + 1`) — which is what calling the *other* edge duplex "row 0" does —
multiplies the whole field by `−1`, and `C-0107` carries no statement, and cites no source, that
fixes which labelling the built object has.

`C-0104` treats the sign as a swept axis and reads **both** at every rung of its ladder, finding
`−17.14°` **17 % worse** than `+17.14°` because `C-0022`'s solved collar breaks the tile's up/down
symmetry. `C-0107` inherits that asymmetry and sweeps the sign for its **row-end-only** states
(0.1022820 against 0.1193334, a 17 % spread) — and then reads its **graded** field at one sign only.

## What the missing state is

`gpd/results/T-190-interior-crossover-prestrain.json`, `states`:

| state | dishing / stroke | free tile | flat at 0.10? |
|---|---|---|---|
| the graded field, overall sign `+` | **0.0922622** | 0.2647317 | **yes** |
| the graded field, overall sign `−` | **0.0910197** | 0.3435120 | **yes** |

The two differ by only **1.4 %** in the coupled dishing while their **free-tile** responses differ
by **1.30×** and lie on opposite sides of the 0.2990348 zero-prestrain value — so the coupling is
what makes the sign nearly irrelevant, and that is a result rather than a coincidence. Over
`C-0107`'s own 12-cell boundary-layer bracket, **40 of 40** sign-and-cell combinations keep the
graded field flat, against **14 of 40** for the row-end-only idealisation.

## Why it is worth raising even though it discharges

1. **It was an unstated assumption carrying a headline number.** `C-0107`'s 0.0922622 is quoted in
   its own one-line claim, and until `T-190` nothing said the other sign existed.
2. **The margin is not the same on both.** 0.0910197 leaves 9.0 % of the convention unused and
   0.0922622 leaves 7.7 %; a tolerance written on one is 1.2 percentage points optimistic for the
   other.
3. **The sign is a real design unknown, not a bookkeeping one**, and it is the same unknown
   `C-0104` names in its *Still open* item 2 — *"the sign, and whether the 14 share one"*. `C-0107`
   answered the second half. The first half is still open and now has a measured cost: **0.0012**
   of the stroke coupled, **0.079** free.

## What would settle it

1. **Quote both graded states**, as `C-0104` quotes both signs of every rung.
2. **A design statement.** Which interface parity carries which crossover type is fixed once a
   caDNAno-level staple layout exists; it is a routing fact, not a measurement, and `C-0086` is the
   claim that would own it.
3. **An oxDNA row end** (`C-0107`'s own recommendation) returns the signed angle directly.

## What this challenge does **not** say

It does not overturn `C-0107`'s verdict — both signs are flat — and it does not touch the
boundary-layer derivation, the 17.15–24.98° bracket, or the literature finding. It says one axis of
a two-valued design unknown was read once, and reports the other value.
