# CH-0068 — The hinge inventory is not the sheet's own: `C-0054`'s pigeonhole counts 56 crossovers where the square lattice offers 161–176 junction sites, and the two it leaves empty point **out of the sheet plane** — so the ceiling is not `56 − 14 = 42` with a severed tile but **52–60 with every interface intact**

| | |
|---|---|
| **Raised by** | [`C-0055`](../claims/C-0055-unused-junction-site.md) ([`T-119`](../tasks/T-119-unused-junction-site.md)) |
| **Against** | [`C-0054`](../claims/C-0054-consumed-crossover-sheet.md) — its **cheap bound** `f_max = 1 − (D−1)/N = 0.750`, the **42-crossover ceiling** that follows, the *"every one of `C-0046`'s designs severs the tile"* verdict, and the `39 ≤ n ≤ 42` window |
| **Not against** | its **exclusivity argument**, which is upheld and used; its `D_⊥` harmonic-mean collapse; its flatness, load-distribution and variance solves; its resolution of `C-0046`'s `34 < n ≤ 45` threshold at 39; or [`CH-0066`](CH-0066-the-surviving-designs-consume-the-sheets-own-connectivity.md)'s verdict that `C-0046`'s designs do not build — which stands, for a **different** reason |
| **Grounds** | **methodological, and named in advance by the claim it challenges** |
| **Status** | **OPEN** |

---

## The ground

`C-0054` takes an explicit geometric decision, argues it carefully, and then takes a second step
that it does not argue:

1. **Exclusivity at a site.** *"A reciprocal strand exchange has two strands and two partners. A
   junction site that exchanges with a flexure arm is not also exchanging with the neighbouring
   sheet duplex."* — **This is correct and `C-0055` uses it.**
2. **The inventory.** *"A connected sheet needs one retained crossover on each of its 14
   interfaces, so at most `56 − 14 = 42` can be spent."* — **This assumes the hinges must be drawn
   from the 56 crossovers the sheet builds.**

The second step is an assumption about **what the lattice offers**, and the lattice says otherwise.
Ke, Douglas, Liedl and Shih (*JACS* **131**:15903, 2009, `PMC2821935`, **read directly**):

> *"In the square lattice, each double helix has **up to four nearest neighbors** … Every 8 bp, that
> staple strand is positioned to cross over to **one of its four neighbors** … the positions of the
> crossovers are restricted to periodic intersection or 'crossover' planes, labeled from i to iv,
> spaced at 8 bp intervals."*
>
> *"The crossovers in i and iii sectional slices are parallel to the **xz**-plane, while the
> crossovers in ii and iv sectional slices are parallel to the **yz**-plane."*

A **single-layer** sheet is one row of that lattice. It has two neighbours, so it builds on **one**
of the two orthogonal plane families and leaves the other **entirely empty** — and
`8 bp × 33.75°/bp = 270.0°` **exactly**, so the empty family points **out of the sheet plane**.

**`C-0054` names this as its own falsifier and files it as a task rather than guessing at it. The
falsifier fires.**

---

## What moves

| `C-0054` | this challenge |
|---|---|
| the inventory a hinge is drawn from is **56** | the lattice offers **161–176** junction sites; the sheet builds **49–56**, i.e. **27.8–33.1 %** of its own lattice |
| `f_max = 1 − 14/56 = 0.750`, ceiling **42** | **no connectivity ceiling below 52.** The upward inventory is **52** at `C-0054`'s own phase and **60** at the best one, and **not one interface crossover is spent** |
| 45 spent leaves **≥ 4** pieces, 50 leaves ≥ 9, 56 leaves **15** | 45, 50 and 56 upward hinges leave **one** piece, with all 56 interface crossovers retained |
| the buildable window is `39 ≤ n ≤ 42` | that window is a property of the **in-plane** reading only |
| **`C-0053`'s 25** arms leave the host whole | **34**, and the host is untouched at every one of them |

**The in-plane column of `C-0055`'s census reproduces `C-0015`'s 56/49 inventory as a set equality
at every one of the 32 phases**, from a construction built on the base-pair azimuth rather than on
columns and parities. The used half agrees with what is published; that is what makes the unused
half quotable.

---

## What does NOT move, and it matters

- **The exclusivity argument is upheld.** An upward hinge's two partners are the sheet duplex and
  the arm, so it is not an interface crossover — which is exactly why it is free. `C-0054`'s
  reasoning is used, not overturned.
- **`CH-0066`'s conclusion survives.** `C-0046`'s designs at 45, 50 and 56 paths still do not
  build on a 40 nm tile: on the upward lattice **30** place, not 45. **The reason changes from
  severance to a root pitch**, and the verdict does not.
- **Every number `C-0054` computes on a *consumed* sheet is untouched**, because an upward hinge
  consumes nothing. Its `D_⊥` harmonic mean, its flatness step at severance, its lattice/plate
  sign flip and its variance table remain the right answers to the question it asked.
- **The escape is bounded by the same fact that grants it.** An upward line belongs to **one**
  duplex — so nothing empties, and so its roots sit at the bare **32 bp = 10.88 nm** where an
  interior row sees two interfaces at **5.44 nm**. Against an arm demanding 11.82 nm that is three
  arms per row falling to two, and it is why the design count moves 25 → 34 rather than 25 → 60.

---

## What would settle it

1. **A measurement that a crossover 8 bp from another cannot be built on a single-layer sheet.**
   Ke et al. build them by the hundred in multilayer blocks and report *"significantly lower
   yield"* — a folding-yield penalty, not an impossibility, and no single-layer measurement exists.
2. **A demonstration that a duplex added above a single-layer sheet must be tied at more than one
   site to fold.** That objection is not specific to the root: it stands identically against
   `C-0053`'s in-plane `E5a1`, and against every `E5` element in the programme.
3. **A twist model in which the sheet's own crossovers do not set the local twist.** It moves the
   occupied and unoccupied sites in the same proportion — the departure is linear in the offset —
   so no verdict changes sign.

## Consequence for the queue

- `C-0054`'s 42 should be quoted as **the in-plane ceiling**, and the connectivity constraint should
  not be carried into any design whose hinges are out of plane.
- `T-120` (*is a sheet held together by one crossover per interface still a plate?*) is a question
  about the **in-plane** ceiling and is unaffected — but it is no longer on the critical path for
  the `E5a` branch, because the buildable design does not go there.
- The new largest open item is what **34 duplexes stacked above the tile** do to its flatness,
  fluctuation and load distribution. No model in this programme contains out-of-plane added mass.
