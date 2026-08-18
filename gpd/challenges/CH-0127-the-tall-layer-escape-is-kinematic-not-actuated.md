# CH-0127 — **`C-0050`'s tall-layer escape is a KINEMATIC root and it was read as an actuated one.** *"Unreachable in physics — NO, and it is false. A taller layer delivers it"* rests on solving `P(L₀−10)·A = 100 pN`, which asks whether the **layer** can be compressed ten nanometres under a hundred piconewtons and never asks whether any **field** can apply them at the resulting gap. Solved on `C-0008`'s own machinery, §3's 100 pN stops arriving across a gap of **13.75 nm** at 0.5 mM, so the escape delivers the **displacement** at 52 of 96 tall states and the **force** at 0 of 96

| | |
|---|---|
| **Raised by** | [`C-0110`](../claims/C-0110-device-b-tall-gap.md) (`T-192`) |
| **Against** | [`C-0050`](../claims/C-0050-desired-stroke-reach.md)'s *"What is established"* table, row 3: *"…unreachable **in physics**" — **NO, and it is false.** A taller layer delivers it"*, and the escape table's framing as *"the escape is a specification change, and it is priced"* |
| **Grounds** | the row is established by a **root of the layer's own load**, `P(L₀ − 10 nm)·A = 100 pN` at `σ = 0.024 nm⁻²` — a statement about a **compression**, not about an **actuator**. `C-0110` reproduces all six of its heights to ≤ 2.0e−4 and then asks the question the root does not contain: the deepest gap across which the field delivers 100 pN is **13.6989 nm at 0.5 mM, 11.8724 at 1 mM and 10.1299 at 2 mM** (1.0 V; 1.23 V buys 0.05 nm more), every one of them **below** the bottom of the escape's own 16.63 nm. On the 96 tall device-B states the coupled equilibrium is admitted at **1**, and on the 96 device-A states — §3's *acceptable* clause — at **0**, all 96 refused because no bias holds the tile at a 3 nm stroke |
| **Severity** | **one row of one table, and it is the row that says the programme's headline negative is escapable.** `C-0050`'s verdict *"§3's desired stroke is unreachable on §3's own stack"* is untouched and is **strengthened**; its catalogue, its 14 rows, its five routes and its `s < L₀` identity are untouched; the escape **table** is reproduced here to four decimal places and is correct as arithmetic. What moves is the word *"delivers"* |

---

## What is claimed upstream

`C-0050`, *"What is established, and what is not"*:

| statement | established? |
|---|---|
| *"§3's ~10 nm desired stroke is unreachable **with this catalogue**"* | **yes**, 0 of 14 rows, and by five independent routes now |
| *"…unreachable **on §3's own stack**"* — its three layer heights, its 100 pN, its buffer | **YES, and this is the claim.** It needs no catalogue at all: `s < L₀ ≤ 10 nm` |
| *"…unreachable **in physics**"* | **NO, and it is false.** A taller layer delivers it |

and then, in the paragraph that supports the third row:

> **The escape is a specification change, and it is priced.** The layer height at which the 100 pN dead-load
> stroke reaches 10 nm, at `σ` = 0.024 nm⁻², solved as a root over `C-0003`'s six models: … **16.63 … 26.12 nm**.

## What the root contains, and what it does not

The root is `P(L₀ − 10 nm)·A = 100 pN` — where the layer's own disjoining pressure over the footprint equals the dead load. It is a statement about the polymer and nothing else. **Three things it does not contain:**

1. **whether any bias can supply 100 pN at the held gap** `L₀ − 10 nm`, which is 6.6–16.1 nm;
2. **whether the equilibrium path can be traversed from `s = 0`**, where the gap is the *full* 16.63–26.12 nm and the dead load is already the whole 100 pN;
3. **whether the resulting equilibrium is stable** — a dead load has `k_c = 0` and clears no positive stability floor at all, which is `C-0017`'s own condition.

`C-0110` measures all three.

| | measured |
|---|---|
| deepest gap delivering 100 pN, 0.5 mM, 1.0 V / 1.23 V | **13.6989 / 13.7498 nm** |
| … 1 mM | 11.8724 / 11.9215 nm |
| … 2 mM | 10.1299 / 10.1772 nm |
| `\|F_es\|` at 17 nm, 0.5 mM, 1.0 V | **49.967 pN** against 100 |
| `\|F_es\|` at 26 nm, 0.5 mM, 1.0 V | **10.574 pN** against 100 |
| tall (gap, buffer) cells reaching 100 pN | **0 of 12** |
| dead-load states with a branch point at zero stroke | **0** — `branchEndStroke` is `null` at every one |
| **device-A** (§3's *acceptable* clause) admitted | **0 of 96**, all refused for the same reason |
| **device-B** admitted | **1 of 96**, in **1 of 6** layer models |
| **free (uncoupled) tile reaching a 10 nm stroke** | **52 of 96** |

## The separation the row conflates

The last two rows are the whole of this challenge. **The escape is real in displacement and empty in force.**
A 17–26 nm layer *does* let the tile travel ten nanometres — at 52 of 96 tall states, including three of six models at 26 nm in 0.5 mM — which is exactly what `C-0050`'s root computes and exactly what it is entitled to say. What it cannot do is deliver §3's 100 pN while travelling, at any of them.

`C-0050`'s row conflates a **stroke** with an **actuator**, and this programme has a standing entry for that class: *"the blocking force and the stroke live at opposite ends of one curve and are never delivered together"*. The escape table is the stroke end read as though it were the whole curve.

## Why this is a challenge and not a note

**Because the row is the programme's only stated route out of its own headline negative**, and it is quoted as such in `ANSWERS.md`, in `TASKS.md`'s `T-115` row and in `DECISIONS-FOR-NDI.md`'s decision 2 — where NDI is being asked to spend a real reserve on it. A *yes* to decision 2 taken on this row buys a 10 nm stroke and no force, and loses §3's acceptable clause on the way: **that is a materially different offer from the one on the table.**

## What would restore it

Any one of these, and each is falsifiable:

- **a larger footprint.** `|F_es|` scales with the tile area at fixed pressure, so 100 pN at 26 nm and 0.5 mM needs **9.46×** the footprint — a 123 nm tile, against §3's 40 nm and `T-102`'s already-open 1.44× request.
- **a bias route the compact layer does not eat.** The force saturates: 1.0 V → 1.23 V is worth 1.01×. A different electrode chemistry with a larger Stern capacitance would move the diffuse share, and `C-0008` already flags that capacitance as cited and load-bearing.
- **a finite-tile collar much larger than `C-0100` measured.** The tile is 1.5–2.4 gap heights across at 17–26 nm and the collar is not carried here; it runs the favourable way. It would have to be worth 2.0× at 17 nm and 9.5× at 26 nm.
- **a first-moment reading of NDI's thickness.** If *"17-26 nm of polymer thickness"* is a first moment, the force-onset heights are 1.71–2.16× **larger** (`C-0077`) — which moves the corner the wrong way, and is listed for completeness rather than as a route.

## What this challenge does NOT say

It does not say `C-0050` is wrong about the layer. Its escape table is reproduced here to ≤ 2.0e−4 at all six models and is correct arithmetic about a correct quantity. It says the quantity is a compression and was published as a delivery.
