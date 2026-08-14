# CH-0079 — A tie grid is a **registration**, not a count, and an armed tile has none: 26 of `C-0035`'s 45 tie attachments do not exist once the tile carries `C-0055`'s arms, and no rigid translation of that grid restores them

| | |
|---|---|
| **Against** | [`C-0035`](../claims/C-0035-flexure-mounting-sense.md) — its *nominal design* row **"what the tile carries: only the 45 tie attachments of `C-0015`'s 3 × 15 grid — the scheme `C-0026` already validated"**, and the premise stated beside it that *"the tile now carries no out-of-plane element at all"* |
| **From** | [`C-0066`](../claims/C-0066-arm-slab-tie-clearance.md) ([`T-126`](../tasks/T-126-arm-slab-clearance.md)) |
| **Grounds** | **conditions**, not arithmetic — the row is true of the tile `C-0035` had and false of the tile the programme now proposes |
| **Severity** | **moderate.** No number in `C-0035` fails to reproduce: its 325.62 nm² aperture floor, its 5.31 nm midspan clearance and its 4.69 nm penetration at the desired stroke are all recomputed through its own library and agree exactly. What falls is the *availability of the stations*, and with it the reading that the tie array's plan view is a solved question |
| **Status** | **OPEN** |

---

## The charge

`C-0035` settled the flexure mounting and wrote down the design that results. Its last two rows say
what each body must carry, and the tile's row reads:

> *"**what the tile carries** — only the 45 tie attachments of `C-0015`'s 3 × 15 grid — the scheme
> `C-0026` already validated. No standoff, no base couple, no 90° junction anywhere on the tile"*

and its opening verdict records, correctly at the time, that *"the tile now carries no out-of-plane
element at all"*.

**`C-0055` then put 34 out-of-plane levers on that tile**, on the `+z` face, rooted on the unused
`EAST` crossover azimuth; `C-0061` priced them at zero in rigidity and named the clearance as open;
`C-0063` moved them to the phase-24 placement that makes the tile flat. On that tile, `C-0066`
finds:

| `C-0015`'s grid | ties | on an arm, at `C-0063`'s own arm senses | at the **best** of every feasible sense | rows fully clear |
|---|---|---|---|---|
| 3 × 15 | 45 | **30** | **26** | **1 of 15** |
| 2 × 15 | 30 | **24** | **24** | **0 of 15** |
| 1 × 15 | 15 | **10** | **10** | 5 of 15 |

and the section makes each of those **level-independent**: the tie must reach the tile, so its clear
column runs from the tile's top face to the standoff base plane and strictly contains the
1.69–6.69 nm arm slab. **A clash that no level can relieve is not relieved by a larger body
either.**

**And no rigid translation restores the grid.** Swept through a full column pitch at 400 001
offsets, the two- and three-column grids have **zero** clearing windows; the one-column grid has
four, the nearest **6.785 nm** off the tile centre-line.

---

## What is *not* charged

1. **No number in `C-0035` is wrong.** Every one reproduces, and the mounting determination `Su`
   — the product of two binaries, the unique survivor — is untouched. The tie still comes down
   through the superstructure onto the tile.
2. **This is not a "the tile is too small" objection.** The arms leave **108** places a tie could
   stand against the 45 demanded, and the poorest of the fifteen rows holds **five**. `T-102` is
   not what this wants.
3. **The escape is priced and it is cheap.** Displacing each tie to the feasible `x` nearest its
   own column — worst displacement 4.332 nm — costs **+1.7 %** of dishing (0.2182 → 0.2219).

---

## Why it is a challenge and not a footnote

Because *"45 tie attachments on `C-0015`'s 3 × 15 grid"* is the sentence four downstream claims
read as a **buildable station set**. `C-0026` validated one attachment row per duplex; `C-0047`,
`C-0058`, `C-0063` and `C-0064` all quote a dishing **on that grid**; `C-0035` names it as what the
tile carries. On an armed tile it is not a station set at all — it is a registration that 26 of its
45 members cannot occupy — and the claim that owns the design has to say which tile it means.

This is the third time the *"45 load paths"* premise has been challenged from a plan view
(`CH-0055` for the flexure bodies, `CH-0065` for the hinge arms), and the first time the objection
is to the **stations** rather than to the elements standing on them.

## What would settle it

1. **A specification or a design decision that the programme builds `E5a1` arms and not `E5`
   standoff flexures.** Then there is no separate tie grid: the coupling enters at the arm hinges,
   `C-0063`'s 0.0706 is the flatness, and this challenge lapses with `C-0035`'s tie row.
2. **`C-0035` re-stating its tile row conditionally** — *"45 tie attachments on `C-0015`'s grid, on
   a tile that carries no arms; on an armed tile, 45 displaced stations at up to 4.33 nm, dishing
   0.2219"* — which is what `C-0066` supplies.
3. **A demonstration that a coupling grid need not be regular at all.** Then the objection softens
   to the displaced set and its 1.7 %, and what remains is that neither reaches `T-5b`'s 0.10.
