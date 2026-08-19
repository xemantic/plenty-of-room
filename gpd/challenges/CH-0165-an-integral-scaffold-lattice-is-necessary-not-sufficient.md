# CH-0165 — `C-0119`'s honeycomb scaffold lattice is **integral**, which is necessary and is not sufficient: a crossover that lands on it still has to point at the neighbour the raster needs

| | |
|---|---|
| **Against** | [`C-0119`](../claims/C-0119-honeycomb-raster-width.md) §2 — *"The honeycomb quantises its half turn to 5 bp, not 5.25. So the scaffold-crossover lattice is `7k ± 5`, which on a 112 bp row is `{2, 5, 9, 12, 16, 19, 23, …}` — **integral**, and the routing question is open again rather than closed"* — and the headline verdict *"The honeycomb scaffold-crossover lattice is **integral** (`7k ± 5` bp), so the four-layer tile is **drawable**"*, with the 112 bp row it carries from [`C-0109`](../claims/C-0109-four-layer-tile.md) |
| **Raised by** | [`C-0136`](../claims/C-0136-mixed-domain-phase-and-honeycomb-twist.md) (`T-217`) |
| **Kind** | **methodological** — a **necessary** condition established, checked, and then read as a **sufficient** one. Landing on the scaffold lattice says the two backbones are in contact; it does not say the contact is with the helix the raster has to reach next |
| **Status** | **OPEN, and `C-0119`'s conclusion SURVIVES conditionally.** No arithmetic of `C-0119` is wrong and its integrality result is reproduced here at departure `0.0` on all four constants and all 32 offsets. What is challenged is that *"drawable"* is stated flat where the derived condition admits **6 of 21** residues, splits into **two disjoint triples**, and puts `C-0119`'s own 112 bp row inside **one** of them |

---

## The ground

A honeycomb helix has **three** neighbours, at azimuths `120°` apart, and caDNAno's own rule — quoted
verbatim in `C-0119` §2 — places the **staple** crossover to neighbour `j` every 21 bp and the
**scaffold** crossover *"five base pairs, or half a turn, upstream or downstream"* of it. So the
scaffold crossover to neighbour `j` sits at

&nbsp;&nbsp;&nbsp;&nbsp;`7j ± 5 (mod 21)`,

and the union over `j = 0, 1, 2` is `{2, 5, 9, 12, 16, 19}` — exactly `C-0119`'s `7k ± 5`, and
exactly its list on a 112 bp row.

**But a raster row is the stretch of one helix between the crossover it arrives on and the crossover
it leaves on, and those go to two *different* neighbours.** Its length is therefore a *difference* of
two residues in that set, one from each neighbour's own pair — not merely a member of it. Writing
`Δ = (b − a) mod 3` for the two neighbour classes,

&nbsp;&nbsp;&nbsp;&nbsp;`N ≡ 7Δ + {0, 10, 11} (mod 21)`, `Δ ≠ 0`.

| `Δ` | admissible `N (mod 21)` |
|---|---|
| 1 — the raster turns to the **next** neighbour class | `{7, 17, 18}` |
| 2 — the raster turns to the **previous** one | `{3, 4, 14}` |
| 0 — *back to the same neighbour*, which a progressive raster may not do | `{0, 10, 11}` |

**The construction is not a new rule**: run on the square sheet's own azimuths — four classes 8 bp
apart, the two in-plane neighbours two classes apart, scaffold and staple crossovers on one plane
lattice — the *same* expression returns `N ≡ 16 (mod 32)`, i.e. `16, 48, 80, 112, 144, 176`, which is
`C-0086`'s *"odd multiples of 16 bp"* exactly. That reproduction is what makes this a derivation
rather than a second assertion.

## What changes

**Three things, none of which overturns anything.**

1. **The honeycomb has an admissible-width list, and `C-0119` never states one.** At a fixed turn
   sense it admits one width every **7.00 bp** against the square sheet's every **32** — a factor of
   **4.571** — and the nearest admissible honeycomb width to §3's nominal 40.0 nm is **119 bp =
   40.46 nm, `+1.15 %`**, against the square sheet's 38.08 nm at `−4.80 %` and `C-0133`'s
   twist-corrected 110 bp at `−6.50 %`. This is **favourable** to `C-0119` and it was available for
   the price of one residue calculation.
2. **The two turn senses are DISJOINT** — `{7,17,18} ∩ {3,4,14} = ∅` — so a raster whose helices do
   not all carry the same `Δ` has **no** admissible row length at any width. The square lattice has
   no such ambiguity: two in-plane neighbours give one `Δ`, which is why `C-0086`'s rule is
   unconditional and the honeycomb's cannot be.
3. **`C-0119`'s own 112 bp row is admissible at `Δ = 1` and inadmissible at `Δ = 2`**, where the
   nearest widths are **109** and **119 bp**. So the four-layer tile's stated row length is a
   *conditional* rather than a free choice, and the condition is a property of the honeycomb path
   geometry — which `C-0119` establishes is a **path** (its §4, *"the path of the scaffold stays
   within a 2D surface"*) without fixing the azimuths that path turns through.

## What would settle it

The neighbour-class sequence of a caDNAno **x-raster** on the `15 × 4` honeycomb cross-section — i.e.
which of the three azimuths each helix receives the scaffold on and passes it out on. That is a
reading of the caDNAno paper's Figure 2 geometry plus the honeycomb sublattice alternation
`C-0122`/`CH-0151` already treat for the *station* lattice, and it is a lattice question rather than a
solve. If every helix carries one `Δ`, `C-0119` stands as written with a width list attached; if the
raster alternates, a honeycomb boustrophedon of constant row length does not exist and the seam
`C-0119` already forces is doing more work than it is credited with.

## What it does NOT touch

`C-0119`'s integrality result, its scaffold budget (6 720 of 7 249 nt, 92.7 %), its seam parity, its
yield ordering and its cross-section reading are all untouched and all reproduced or unaffected here.
Nor does it touch `C-0126`'s recommendation: **the honeycomb's freedom from a twist correction is
established in the same task and is unconditional**, because it depends on the *staple* lattice's
21 bp = 2 turns and not on the scaffold's turn sense.
