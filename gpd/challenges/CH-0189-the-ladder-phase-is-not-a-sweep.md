# CH-0189 — **The honeycomb station ladder's PHASE is not a free 21-valued convention: the same caDNAno rule that fixes the inter-row offset over-determines it.** `C-0141` §5 and `C-0146` §2 sweep 21 phases and read a best cell off the sweep; where the raster closes, the phase is **determined** — 16 at the one closing length pair, carrying **55 of 60** stations — and where it does not close, **no** phase is determined and the sweep is a sweep over designs that need forced crossovers. `CH-0184`'s saturating cell, *phase 11 at the 14 bp offset*, is neither

| | |
|---|---|
| **Against** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) §5, *"Every phase of every case is enumerated — 21 phases × 3 offsets × 2 row lengths × 2 cross-sections"*, read as an enumeration of **available** designs; and [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) §2 / [`CH-0184`](CH-0184-the-inter-row-offset-stops-being-free.md) §2, which report *"the two-length raster, best of 42 pairs: **60**, at **phase 11 / 14 bp**"* as an attainable optimum |
| **Raised by** | [`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) / [`T-244`](../tasks/T-244-face-bond-class-residues.md), result [`gpd/results/T-244-face-bond-class-residues.json`](../results/T-244-face-bond-class-residues.json), sections `determined` and `census` |
| **Grounds** | **logical.** The `±5 bp` scaffold-crossover rule read from the primary source, applied to `C-0140`'s own level walk. Exact integer arithmetic; `CH-0184`'s 42-cell table is reproduced first, at departure `0.0` |
| **Status** | **raised.** `C-0141`'s enumeration is correct as an enumeration and its **structural** results — the forced stagger, the parity rule, the 30° azimuth, the centro-symmetry criterion — are untouched, being properties of the offset and of the row parity rather than of the phase |

---

## 1. Why a phase looked free

`C-0141` treats the ladder phase as the design's registration against the crossover lattice and
sweeps it. That reads as a free variable because sliding a design along its own base-pair axis is
free — and it is, but it slides the **windows with the lattice**, so it does not move the phase at
all. The phase is the row window's own end **relative to** the crossover residues, and both are
fixed by the routing.

## 2. What fixes it

Every raster crossover is a **scaffold** crossover, so caDNAno's default rule puts it `±5 bp` from
its pair's staple position `b₀ + 7c (mod 21)`. One `b₀` must serve the whole design. Where one
does, every staple position in the design is fixed relative to the level walk — and a **station** is
a staple crossover position on a free azimuth, so the ladder is fixed with them.

At the one length pair of `C-0140`'s five that closes (`CH-0188`):

| | `10 × 6` | `15 × 4` |
|---|---|---|
| `b₀` | **5** | **5** |
| ladder phase, from the block's low plane | **16** | **16** |
| inter-row offset | **14** | **14** |
| stations per row | `5, 6, 5, 6, …` | `5, 6, 5, 6, …` |
| stations on the face | **55** of 60 | **82** of 90 |
| best over the 21-phase sweep at that offset | **55** | **82** |

**The determined phase happens to be optimal**, which is a result and not a construction — the
sweep's maximum sits at phases 14–18 and the rule lands at 16.

## 3. The cell that is withdrawn

`CH-0184`'s headline is that *exactly one of 42 `(phase, offset)` pairs keeps all sixty stations —
phase 11 at the 14 bp offset*, and that *"a **six**-column placement stands at the one saturating
pair"*. Both halves of the census are reproduced here at departure `0.0`. The cell is nonetheless
**not a design**:

- it lives at **112 / 108**, which does not close (`CH-0188`), so no `b₀` serves it at all;
- and at that pair the two *near-miss* readings of `b₀` — the ones violating 10 and 25 of the 59
  raster crossovers respectively — put the phase at **2** (50 stations) and **13** (55). Neither
  is 11.

**So no six-column placement stands at any pair this repository has examined**, and the station
count a honeycomb face offers is **55 of 60** rather than 60.

## 4. What survives, and it is most of it

- **The offset is unaffected.** It is a difference of two bond classes and contains no `b₀`, which
  is why `C-0148` can settle it at **14** on a raster that does not close.
- **`C-0141` §5's structural results survive**: the forced stagger, *"no honeycomb face has its
  station rows in register"*, the row-parity rule that makes `15 × 4` admit no centro-symmetric
  lattice at any phase, and the 30° azimuth are all properties of the offset and the parity.
- **What is withdrawn is the reading of a swept phase as an available design choice.** A phase
  sweep remains the right instrument for *"which phases could a design occupy if the routing were
  re-chosen"*; it is not a menu.
- **No graded cell moves.** `C-0142`'s and `C-0146`'s flatness numbers are read at column counts
  that every phase admits, and `C-0146`'s own `F4` — *no graded placement is refused* — is
  unaffected.

## 5. What would settle it further

A sweep of **closing** length pairs for the station census: the three closing residue classes on
the `10 × 6` path are `(7, 14)`, `(17, 3)` and `(18, 4)` modulo 21, and only the last has been
examined. If some closing pair's determined phase saturates the census, the six-column placement
returns — at a raster nobody has graded.
