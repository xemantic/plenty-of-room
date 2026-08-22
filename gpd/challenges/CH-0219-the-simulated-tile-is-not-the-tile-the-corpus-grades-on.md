# CH-0219 — **`C-0157`'s oxDNA tile and the tile every placement result in this corpus is graded on share NOT ONE staple crossover column.** The design file's *"phase 8"* is the **row-start** datum and the placement corpus's *"phase 8"* is the **tile-centre** datum; on a 112 bp row those differ by exactly 8 bp, because 112 is **seven** column pitches and seven is odd. The simulated tile is tile-centre phase **16** — 7 columns, **49** staple crossovers, **63** inter-duplex ties — and the graded tile is tile-centre phase **8** — 8 columns, **42** staple crossovers and **14** raster turns, **56** ties. Their staple columns are at `8, 24, 40, 56, 72, 88, 104 bp` and `0.147, 16, 32, 48, 64, 80, 96, 111.853 bp`: they **interleave** and are **disjoint**

| | |
|---|---|
| **Against** | [`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md)'s stated ground — its `Consumes` row cites [`C-0063`](../claims/C-0063-upward-root-placement.md) for *"crossover phase 8"*, and `C-0063`'s phase 8 is the tile-centre datum whose congruence `2c ≡ 0 (mod p)` picks out phases 8 and 24 — and [`C-0160`](../claims/C-0160-scadnano-writer.md)'s headline, which reports the emitted sheet as *"15 duplexes, 112 bp, **phase 8**, seven columns, 49 crossovers"*, four facts in one list of which the first, third, fourth and fifth are in the file's datum and the second is the corpus's |
| **Raised by** | [`C-0170`](../claims/C-0170-simulated-tile-census.md) (`T-275`) |
| **Grounds** | **logical** — an integer census over the committed `gpd/designs/gen1-sheet-square-15x112.sc` against `anchoring.rasterColumnLayout`, the function every placement study in this corpus calls. No solve decides it; three solves price it |
| **Status** | **OPEN — filed, not repaired.** Nothing here says `C-0157`'s `k_θ` bracket is wrong: `k_θ = k_BT/Var(roll at a site)` is a per-crossover equipartition and reads no count. What it says is that the object the variance was measured **on** is not the object the constant is **used in**, and that no claim in the corpus states which |

---

## The observation

`ScadnanoDesign.crossoverPhase()` returns *"the first column, which is the phase the column lattice
was laid out at"* — counted from the scaffold's own offset 0. `CrossoverLayout.phased` lays its
lattice down as `x = phase·rise + k·(p/2)` with `x` measured from the **centre** of the footprint.
Both are called *the phase*, both are quoted as integers of base pairs, and on `C-0086`'s buildable
row they are **8 bp apart**:

```
112 bp = 7 × 16 bp,  and 7 is ODD
⇒ the tile centre sits half a column pitch off any row-start lattice point
⇒ centre-datum phase ≡ row-start-datum phase + (112/2) mod 16 = + 8   (mod 16)
```

One division. It predicts the whole of what follows, and it is the reason the collision is on the
integer **8** specifically rather than on any other.

## What the two lattices are

| | the design file, `gen1-sheet-square-15x112.sc` | the graded lattice, `rasterColumnLayout(8, …, 38.08, admit)` |
|---|---|---|
| tile-centre phase | **16** | **8** |
| row-start phase | 8 | 0 |
| columns | 7 | 8 |
| staple crossover columns, bp | `8, 24, 40, 56, 72, 88, 104` | `16, 32, 48, 64, 80, 96` |
| row-end columns, bp | none | `0.147, 111.853` (inset by `EDGE_MARGIN`) |
| **staple** crossovers | **49** | **42** |
| raster turns | 14, at offsets 0 and 111 | 14, and they **are** the two end columns (`C-0090`, `C-0095`) |
| inter-duplex ties | **63** | **56** |
| per-interface split | 4/3 | 4/4 |

**The staple column sets are disjoint.** Not nearly disjoint — they interleave at 8 bp, which is
`CLAUDE.md`'s own *"a shift by one column pitch … hands every interface the other parity's columns —
a physically different sheet"*, at **half** that shift.

## Why it is a challenge and not a note

Three things in the corpus read as though the two objects were one:

1. **`C-0157`'s ground.** Its `Consumes` row attributes *"crossover phase 8"* to `C-0063`.
   `C-0063`'s phase 8 is one of the two phases at which a column lands on the row end
   (`C-0090`: *"38.08 nm is `7 × 5.44` exactly, so a column lands on the row end at `b ≡ 8 (mod 16)`"*),
   and the simulated design has **no** column on either row end. The measurement's stated lattice is
   not the lattice it was made on.
2. **`C-0160`'s headline** carries *"phase 8, seven columns, 49 crossovers"* as one list. Seven and
   49 are right; *phase 8* in the datum the rest of the corpus quotes is **eight** columns and 56.
3. **`C-0161`'s note** — *"the two phase conventions differ by `112/2 = 56 = 8 (mod 16)` and the
   lattices do not differ at all"* — is **true of the comparison it makes** (the file against
   corpus phase 16) and is the sentence a reader takes as settling the question. It says nothing
   about phase **8**, which is where the placement corpus lives.

## What it is worth

- **On `k_θ`: nothing directly, and a direction.** The estimator is per-site. What the census
  changes is the *surroundings*: the simulated tile carries **63** ties against the graded tile's
  **56**, `1.125×` — **more** restraint, not less — so the roll variance was measured on the
  stiffer of the two objects and the upper end of the bracket is the biased-high end. That
  direction is already inside `C-0157`'s own bracket, whose two readings are *"the hinge alone"*
  against *"the hinge over its neighbours"*.
- **On the continuum, the sign is the other way round.** `D_⊥ = k_θ d/p` is a **linear density**
  `1/p`, which over fourteen 112 bp interfaces is exactly **49** crossovers — the *simulated*
  tile's count. The graded lattice carries `1.14285714×` the density its own smeared rigidity
  assumes.
- **In the corpus's own currency**, the same load case on the same footprint gives a peak dishing
  of **1.273683 nm** on the simulated lattice against **1.24539212 nm** on the graded one:
  **2.3 %**. The mismatch is real and it is small — which is worth saying, because the honest
  reading is *"the two objects differ and the difference is affordable"*, not *"they are the same"*.

## What would settle it

- A **datum-carrying** phase. `ScadnanoDesign.crossoverPhase()` and `CrossoverLayout` should not
  both be able to hand a bare integer to a claim; a phase quoted anywhere in this corpus should
  name its datum, and `C-0161`'s own `parityPairs` record already shows why (0 and 16 have
  identical positions and exchanged parities, so even the datum is not enough — the **file** is).
- A one-line assertion in `tools/oxdna/gen1_tile_design.py` against `rasterColumnLayout` rather
  than against a transcribed literal — which is `CH-0220`.
- A decision, recorded, about **which** lattice a re-run should simulate. `C-0169` §1 prices that
  re-run at about one day and 649 MB; it should not be bought before this is answered.
