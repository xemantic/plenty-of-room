# CH-0195 — **Both crossover-column counts this corpus has graded a honeycomb block at belong to a raster that cannot be drawn, and at the one that can the count is TEN.** `C-0146`'s eleven- and twelve-column readings and `C-0148` §4b's selection between them are all read at `112 / 108`, which needs 10 forced scaffold crossovers; the drawable pair's interface window is **102 bp**, giving **10** columns at *every* `EDGE_MARGIN` convention. So `C-0146`'s *"three cells of eight are decided by a numerical guard"* is a statement about an undrawable geometry — at the drawable one the guard is **inert**, and the count is decided by the raster

| | |
|---|---|
| **Against** | [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) §3 (*"a **numerical guard** … that column alone is the difference between **6 flat cells of 8** and **3**"*), its Deliverable-4 table and its *Still open* item 4; and [`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) §4b's selection of *"`C-0146`'s **116 bp / 11-column** column"* |
| **Raised by** | [`C-0151`](../claims/C-0151-closing-raster-selection.md) / [`T-245`](../tasks/T-245-closing-raster-selection.md), result [`gpd/results/T-245-closing-raster-selection.json`](../results/T-245-closing-raster-selection.json), sections `columnCounts`, `cells` and `paired` |
| **Grounds** | **logical + in-silico.** The interface window is `rowSpan − stagger` exactly, and a 7 bp stagger is what closure costs (`CH-0194`); the eight cells are then re-graded on the same machinery, same seed, same stream, with `C-0146`'s own readings reproduced at `≤ 4.1e−9` in the same process |
| **Kind** | **a state, not an error.** `CLAUDE.md`'s *"quote it with the state it is read at"*, where the state is a **raster** — every number `C-0146` and `C-0148` publish is correct for `112 / 108` and neither claim could have known that pair does not close |
| **Status** | **raised.** `C-0146`'s grading machinery, its paired reading, its station census, its family table and its finding that *the `+3.57 %` is a stagger and not a row length* are all upheld and consumed unmodified; **32 of its cells are reproduced here at `≤ 4.1e−9`**. `C-0148`'s row-span reading of the column count is upheld and is precisely what this challenge applies |

---

## 1. The three readings, and which raster each belongs to

`C-0148` established that a crossover column serves an **interface**, so its window is the
intersection of two adjacent row spans. For a two-length raster every interface joins an even row
to an odd one, so all nine carry the identical window, and it is `rowSpan − stagger` exactly:

| pair | closes | row span | stagger | interface window | **row-derived columns** | box columns |
|---|---|---|---|---|---|---|
| `112 / 108` — `C-0140`'s recommendation | **no**, 10 forced | 112 bp | 4 bp | 108 bp | **11** | 12 |
| **`102 / 109`** — the drawable pair | **yes**, 0 forced | 109 bp | 7 bp | **102 bp** | **10** | 12 |

`C-0146` graded at **11** (the row reading) and **12** (the bounding box), and `C-0148` selected
the 11-column column. **Both belong to `112 / 108`.**

## 2. The guard is inert at the drawable pair

`C-0146` §3's finding is that `CrossoverLayout.EDGE_MARGIN` decides three cells of eight, because a
116 bp box clears eleven honeycomb pitches by **0.07 nm**. That is a property of the **box**, and
`C-0148` §4 already showed the box is not the crossovers' window. At the drawable pair the
interface window is 102 bp = 34.68 nm and the count is **10 at all three margins** — 0.05, 0.17 and
0.34 nm — with slack 2.45 / 2.21 / 1.87 nm past the last pitch. `guardIsInertOnTheInterface` is
`true`.

**So the guard decides nothing at the geometry a design would be built at**, and the 0.07 nm knife
edge `CH-0185` raised is an artefact of reading a column count off a dimension no crossover
occupies, at a raster nobody can draw.

## 3. What the tenth column costs, measured

`C-0140`'s pair and the drawable pair have the **same block extent**, 116 bp = 39.44 nm, so at the
width §3 is owed they are the **same tile** — graded at the same column count all eight cells agree
below `1e−10`, which is what a solved field of this lattice can be asserted to. The **only**
difference is the column count.

`10 × 6`, abstract grid, `f = 0.30`, `p90` of the measured staple dropout on **one** common stream
(**bold** is flat against `T-5b`'s 0.10):

| columns | paths | distribution | **`102 / 109`, 10 col** | `112 / 108`, 11 col | `112 / 108`, 12 col |
|---|---|---|---|---|---|
| 1 | 10 | equal springs | **0.0773373597** | **0.0708759349** | **0.0662801686** |
| 1 | 10 | rim-graded 5:1 | 0.11075597 | 0.104654401 | **0.0998334915** |
| 3 | 30 | rim-graded 5:1 | 0.109744899 | 0.100357905 | **0.0938556471** |
| 5 | 50 | rim-graded 5:1 | **0.0921821694** | **0.0855380627** | **0.0805842317** |
| | | **flat of 8** | **2** | **3** | **6** |

Read per realisation, the tenth column costs a **uniform 5.7–9.6 %** of the dishing across all
eight cells — a design difference, not a sampling one, because the two readings share one stream.

**`C-0142`'s tightest cell moves for the third time and now for a structural reason.** The
3-column rim-graded cell is `0.0938556471` at twelve, `0.100357905` at eleven and **0.109744899**
at ten. `C-0146` reports it *"decided by a 0.07 nm slack against a numerical guard"*; at the
drawable raster it is decided by the raster, and it is lost.

## 4. What survives

- **`C-0146`'s recommended coupled design survives.** One column, ten paths, equal springs:
  `0.0773373597` at `f = 0.30` and `0.0821458169` at `f = 0.26`, both flat.
- **The width reading is untouched.** Both pairs give the same 116 bp box; only the row span moves,
  112 → 109 bp.
- **`C-0146`'s *Still open* item 4** — *"whether the twelfth crossover column exists on a staggered
  112 bp row lattice"* — is answered in a way it did not anticipate: on the drawable raster the
  question is not 11 against 12 but **10**, and 12 was never a row's count.
- **`C-0148`'s reasoning is upheld exactly**; only the pair it was applied to moves. Its §4b table
  remains the correct reading of `112 / 108`.

## 5. What would settle it further

A **price** for a forced scaffold crossover. [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md)
(`T-246`, filed in the same iteration) supplies the **elastic** half — `0.350894669 k_BT` per
forced crossover, sub-thermal, `0.438634952` of one crossover column of the host sheet for the
whole forcing — and records that no published **yield** price exists, over 68 queries in 7
families. So the trade is now stated in both currencies and undecided in one: **one crossover
column and one flat cell of eight against ten sub-thermal elastic departures whose kinetic cost
nobody can quote.** Until that is priced, ten columns is the geometry a downstream flatness number
is owed at, because it is the one that needs no unpriced permission.
