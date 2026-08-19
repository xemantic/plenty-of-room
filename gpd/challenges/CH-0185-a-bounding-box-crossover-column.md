# CH-0185 — **`CrossoverLayout.EDGE_MARGIN`'s own KDoc still certifies it inert, and it decides three flat cells of eight at the honeycomb's buildable width.** A 116 bp = 39.44 nm block extent clears eleven honeycomb crossover pitches by **0.07 nm** — one fifth of a base-pair rise — so the guard admits a **twelfth** column at 0.05 nm and refuses it at half a rise, and the coupled flat count is **6 of 8 against 3**. The deeper objection is that **every x-raster row of that block is 112 bp**: the twelfth column is a property of the block's bounding **box**, and no row can carry it

| | |
|---|---|
| **Against** | `CrossoverLayout.EDGE_MARGIN`'s KDoc in [`structure/CrossoverLayout.kt`](../../src/main/kotlin/structure/CrossoverLayout.kt): *"The margin is far below the 0.28 nm closest approach any base-pair phase makes on a 40 nm tile, so it never decides a column count that the physics does not already decide."* And against the practice, standing across `C-0109`, `C-0116`, `C-0118`, `C-0120`, `C-0141` and `C-0142`, of deriving a four-layer tile's crossover-column count from a bounding-box `edgeX` |
| **Raised by** | [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) / [`T-235`](../tasks/T-235-coupled-cells-at-the-two-length-raster.md), result [`gpd/results/T-235-coupled-cells-at-the-two-length-raster.json`](../results/T-235-coupled-cells-at-the-two-length-raster.json), sections `columnCounts`, `references` and `cells` |
| **Grounds** | **logical** (one division, and the integer row spans `C-0140`'s level walk produces) **+ in-silico** (the same cells graded at both column counts on one common dropout stream) |
| **Status** | **raised.** This is the **second** geometry at which the inertness sentence fails — [`C-0134`](../claims/C-0134-buildable-width-count-phase.md) found it false at 38.08 nm on the square lattice, where it deleted two of eight columns at exactly the two phases the design wanted |

---

## 1. The arithmetic

The honeycomb's per-interface crossover spacing is 21 bp, so the four-layer studies lay columns at
`pitch = 21 × 0.34 / 2 = 3.57 nm` and take `floor((edgeX − 2 · EDGE_MARGIN)/pitch) + 1`.

| width | `edgeX` | guard | columns | slack past the eleventh pitch |
|---|---|---|---|---|
| 112 bp (`C-0142`) | 38.08 nm | 0.05 / 0.17 / 0.34 nm | **11 / 11 / 11** | 2.28 / 2.04 / 1.70 nm |
| **116 bp** (`C-0140`'s buildable extent) | 39.44 nm | **0.05 nm** | **12** | **0.07 nm** |
| 116 bp | 39.44 nm | 0.17 nm | **11** | 3.40 nm |
| 116 bp | 39.44 nm | 0.34 nm | **11** | 3.06 nm |

**0.07 nm is 0.206 of a base-pair rise.** `CLAUDE.md` records that *"a margin below 0.34 nm cannot
be corrected, only removed"* — DNA quantises every length at the rise, so there is no shorter
increment to trade with. The guard is not resolving a physical clearance here; it is standing on
one.

## 2. What it decides

`10 × 6`, four-layer honeycomb, at the measured composite fraction `f = 0.30`:

| | uncoupled dishing | coupled cells flat of 8 |
|---|---|---|
| 112 bp, 11 columns | 0.0240648102 | **4** |
| 116 bp, **11** columns | 0.0252615047 | **3** |
| 116 bp, **12** columns | 0.0231299291 | **6** |

The width alone is adverse (`+4.97 %` on the uncoupled tile) and the twelfth column is favourable
(`−8.44 %`), so the two run opposite ways and **the guard picks which wins**. Three cells of eight
are flat only at twelve columns: 1 column rim-graded, 3 columns equal springs, and 3 columns
rim-graded — the last being `C-0142`'s own tightest cell, `0.100357905` at eleven columns and
`0.0938556471` at twelve, straddling `T-5b`'s 0.10.

## 3. The deeper objection: a bounding box is not a row

`C-0140`'s two-length raster assigns 112 bp to sense-1 helices and 108 bp to sense-2 ones. Walked
over the real `10 × 6` path, **every x-raster row spans exactly 112 bp** — the row unions are
`[−112, 0]` and `[−116, −4]` on a global base-pair axis — and the block extent is 116 bp **only
because consecutive rows are staggered axially by 4 bp**.

**And it is a property of the family, not of the pair.** Over all five candidate pairs `C-0140`
tabulates, at both 60-helix cross-sections, every raster row spans the **larger** of the two
lengths exactly and the block extent exceeds it by exactly the stagger — 112 / 108 → rows 112,
block 116; 101 / 109 → rows 109, block 117; 102 / 109 → 109 and 116; 112 / 109 → 112 and 115;
122 / 119 → 122 and 125. So `CH-0187`'s re-selection of the pair cannot dissolve this objection.

A crossover column serves an **interface between two rows**, and both of those rows are 112 bp
long. So the column inventory a *row lattice* carries is the 112 bp one, eleven columns; the
twelfth exists only in a model whose `lengthX` is the bounding box. `OrigamiGrillage` has one
`lengthX` and cannot represent the stagger at all, so it cannot distinguish the two.

**This is not an argument that eleven is right and twelve is wrong.** It is an argument that the
choice is currently being made by a numerical guard whose own documentation says it makes no
choices.

## 4. What would settle it

- A crossover layout derived from the **row** spans rather than from `edgeX` — which needs the
  grillage to carry a per-beam axial window, and it does not.
- Failing that, `EDGE_MARGIN` should be quoted as a **swept** parameter wherever a four-layer
  flatness verdict is read at a width whose slack is below one base-pair rise, and the KDoc's
  inertness sentence should be replaced by the condition under which it holds:
  `(edgeX − 2m) mod pitch` bounded away from zero by more than the guard's own range.

## 5. Scope

Nothing at 112 bp moves: the guard is inert there at all three conventions (slack 1.70–2.28 nm),
and `C-0142`'s sixteen cells reproduce at `≤ 3.9e−9`. `C-0134`'s square-lattice finding is
untouched and is corroborated. What is challenged is one sentence of a `const val`'s KDoc, and the
practice of reading a bounding-box `edgeX` into a column count on a staggered row lattice.
