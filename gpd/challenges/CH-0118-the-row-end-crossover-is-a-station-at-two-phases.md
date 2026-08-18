# CH-0118 — **The row-end crossover IS an upward station, at two of the four phases that have one.** `C-0090` reads its own end-of-row convention as *"adds in-plane inventory and no stations"* and generalises it to *"the row-end crossover can never be an upward site, **at any phase**"*. The clause it rests on — *"an end plane has an even index"* — is true at phases 8 and 24 and **false at 0 and 16**, where the end plane's index is odd, it is not a column at all, and admitting it adds **fifteen** upward stations and **zero** columns, taking the inventory **45 → 60**

| | |
|---|---|
| **Against** | [`C-0090`](../claims/C-0090-buildable-raster-width.md)'s Deliverable 3, the sentences *"an end plane has an **even** index, and the upward azimuth needs `k ≡ 2b + 3 (mod 4)`, which is odd. **The row-end crossover can never be an upward site**, at any phase, so admitting it adds in-plane inventory and no stations"* |
| **Raised by** | [`C-0102`](../claims/C-0102-crossover-phase-selection.md) (`T-171`) |
| **Grounds** | **a statement proved at the state it was read at and quoted as a theorem** — the ninth instance in this project, and here the state is again a *lattice coordinate*. The parity of the end plane's index is a function of the phase, and `C-0090` examined the two phases at which it is even |
| **Status** | **OPEN** |

---

## What the standing claim says

`C-0090`, Deliverable 3, in full:

> That is what makes the whole rest of this claim a two-variable comparison: the **host** (which
> columns the grillage carries) and the **load field** (where the collar sits). It is also why the
> end-of-row planes matter only for the *columns*: an end plane has an **even** index, and the
> upward azimuth needs `k ≡ 2b + 3 (mod 4)`, which is odd. **The row-end crossover can never be an
> upward site**, at any phase, so admitting it adds in-plane inventory and no stations.

## The congruence, which is one line and settles it

A crossover **plane** sits on the row end when `phase ≡ −rowBasePairs/2 (mod 8)`, because the planes
are 8 bp apart. Its **index** `k` is even — i.e. it is one of the sheet's own **columns** — only
when the same congruence holds **modulo 16**, because the columns are 16 bp apart.

At `C-0086`'s buildable 112 bp row those two conditions are

| | congruence | phases |
|---|---|---|
| a **plane** lands on the row end | `phase ≡ −56 ≡ 0 (mod 8)` | **0, 8, 16, 24** |
| that plane is a **column** | `phase ≡ −56 ≡ 8 (mod 16)` | **8, 24** |
| that plane is **not** a column — so it carries the out-of-plane azimuths | | **0, 16** |

`C-0090` computes at phases 8 and 24, because they are the two its `endOfRowColumnPhases` names, and
its sentence is exactly right there. It is not right at 0 and 16.

## What it is worth, measured on `C-0055`'s own construction

`rowEndUpwardStations` is a difference of two `rasterSiteInventory` calls — **`C-0090`'s own
function**, at the two settings of its own flag:

| phase at 38.08 nm | row-end upward stations | row-end columns | upward inventory, refused → admitted |
|---|---|---|---|
| **0** | **+15** | **0** | **45 → 60** |
| 8 | 0 | +2 | 52 → 52 |
| **16** | **+15** | **0** | **45 → 60** |
| 24 | 0 | +2 | 53 → 53 |

Fifteen, and not sixteen, because the two end planes serve complementary row parities: `k = +7`
is `EAST` on the eight even rows and `k = −7` on the seven odd ones.

## Why this is a challenge and not a note

Because the sentence is **load-bearing for the question `C-0098` left open**. `C-0098`'s *Still
open* item 3 asks which phase a whole design should take, and its answer turns on the richest
upward inventory. At the buildable width **the entire richest set is `{0, 16}` and it exists only
under the admitted convention** — the same convention `C-0090` adopts, on `C-0095`'s and `C-0099`'s
evidence, and whose consequence for stations its own prose denies. A reader who takes
*"adds in-plane inventory and no stations"* at face value concludes that the coupling's inventory
cannot depend on the end-of-row convention, and would therefore never census `{0, 16}` at all.

It is also load-bearing the *other* way. `C-0090`'s recommended placement at phase 8 is unaffected —
but `C-0090`'s own 32-phase descent table publishes a placement at phase 0 whose key contains roots
at `±18.99 nm`, which is the **inset row-end station**. The claim's code was already standing on
stations its prose says do not exist.

## What is NOT claimed

- **No number in `C-0090` moves.** `rasterSiteInventory`, `rasterUpwardSites` and `rasterColumnLayout`
  compute the inventory correctly at every phase; `C-0102` reproduces `C-0090`'s **0.0621469105**
  from its own published placement key at a departure of `5.5e−10`. What is challenged is a
  quantifier in the prose.
- **The recommendation does not change.** `C-0102` recommends phase 8, which is `C-0090`'s own —
  and it recommends it *because* the richest phases were censused and priced, not because they were
  excluded by a sentence.
- **Nothing about the row-end convention itself is reopened.** `C-0095` settled the permission and
  `C-0099` the mechanics; both stand.

## How to settle it

1. `C-0090` restates the sentence as *"at phases 8 and 24 the end plane is a **column**, so
   admitting it adds in-plane inventory and no stations; at 0 and 16 the end plane is **odd** and
   admitting it adds fifteen stations and no columns"*, with the two congruences beside it.
2. Any future census of the upward inventory at a commensurate width runs at both settings of
   `admitRowEnd` and emits the difference, which is one subtraction (`rowEndUpwardStations`).
