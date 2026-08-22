# CH-0227 — **The honeycomb grillage carries the STAPLE crossovers and not the raster's own 59 TURN ties, and putting them in stiffens the recommended block by `1.12×`.**

| | |
|---|---|
| **Against** | [`C-0154`](../claims/C-0154-honeycomb-grillage.md) (`T-253`) — [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt)'s bond list, and every free-tile dishing built on it; and through it [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) (`T-263`), whose uncoupled references and 64 re-graded cells are read on the same object |
| **Raised by** | [`T-254`](../tasks/T-254-raster-turn-prestrain.md) / [`C-0175`](../claims/C-0175-drawable-raster-rim.md) |
| **Grounds** | **logical** (a census of what the scaffold covalently ties against what the lattice assembles) **+ in-silico** (the same lattice with the ties added, on the same load, the same grid and the same stroke) |
| **Status** | **RAISED; §6 DISCHARGED by [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`).** The omission is real, its sign is favourable, and its size is `1.12×` at the recommended cross-section and `1.017×` at the control. ~~No verdict of `C-0154` or `C-0167` reverses~~ — **that was a FREE-TILE statement and it is false at the coupled level: `C-0167`'s *"`0` of `64`"* is **`2` of `64`** on the tied lattice, which is [`CH-0234`](CH-0234-no-verdict-reverses-was-a-free-tile-statement.md). No verdict of `C-0154` reverses, and every FREE-tile reading below reproduces at `≤ 1.5e−9`.** What moves is every uncoupled reference, and they move toward flat |

---

## 1. What the lattice contains

`HoneycombGrillage.bonds` is built from `block.sites × honeycombAzimuthsOf(site)`, placed at the
planes of the site's own bond class — the **7 bp ladder**, one pair per interface every 21 bp.
That is the **staple** crossover lattice, and on the recommended `10 × 6` block at the drawable
raster's 116 bp extent it is **435** bonds.

## 2. What the scaffold also ties

A honeycomb x-raster runs `+s` along one helix, **turns**, and runs `−s` along the next. A turn
carrying zero unpaired nucleotides *is* a scaffold crossover — `C-0147`'s own `n = 0` row, whose
span `d − 2r_P = 0.718724283 nm` sits at `+1.49997857 σ` of `T-71`'s measured phosphodiester step
and closes. It is a covalent tie between two duplexes, at their **ends**, i.e. at `s = ±L/2`.

There are `H − 1` of them. On a 60-helix block that is **59**, and none of them is in the bond
list, because a turn sits *past* the last plane of the 7 bp ladder.

This is `C-0099`'s square-lattice `56 = 42 + 14` — *"the two end columns **are** the scaffold's own
turns, and the **staple** count is 42 either way"* — read on the honeycomb, where the split is
**435 + 59**.

## 3. What they are worth

The same lattice, the same `C-0022` collar at 2 mM / 10 nm / 0.192 V, the same 81 × 81 grid, the
same free stroke, the ties added with the hinge, link and slip a bond carries:

| cross-section | coupling | free dishing, no ties | free dishing, 59 ties | ratio |
|---|---|---|---|---|
| **`10 × 6`** | `f = 0.30` | **0.0501417316** | **0.0446459684** | **0.890395426** |
| `10 × 6` | `f = 0.26` | 0.0522223659 | 0.0467367262 | 0.894956124 |
| `10 × 6` | none (the lattice's own lower bound) | 0.132443428 | 0.12738041 | 0.961772226 |
| `15 × 4` | `f = 0.30` | 0.22389874 | 0.220086801 | 0.982974723 |
| `15 × 4` | `f = 0.26` | 0.23097815 | 0.227094793 | 0.983187342 |
| `15 × 4` | none | 0.316408058 | 0.31116115 | 0.983417274 |

The two untied columns reproduce `C-0167`'s own uncoupled references — **0.0501417315** and
**0.0522223659** — at a departure of `1e-9`, so this is the same object measured twice and not two
objects.

## 4. Why the two cross-sections differ by 6.5×

The turns are **50** through-thickness ties and **9** in-plane ones on `10 × 6`, and **45** and
**14** on `15 × 4`. A through-thickness tie at the rim ties two *layers* together at the one place
the block has no crossover column, and `C-0154`'s own finding is that a honeycomb block's
across-helix load path **necessarily traverses the thickness** — so the ties land exactly where
that path is weakest. On `15 × 4`, which has 15 face helices and only four layers, the same 59 ties
are spread over a stiffer in-plane direction and buy 1.7 %.

## 5. What it does and does not change

- ~~**No verdict reverses.**~~ **No FREE-TILE verdict reverses** (`CH-0234`): the recommended
  `10 × 6` block is flat with and without the ties and `15 × 4` fails with and without — but
  **two of `C-0167`'s 64 COUPLED cells do reverse**, `0.106041029 → 0.0995744767` and
  `0.101931622 → 0.0998791032`, because a favourable move is exactly what reverses a cell sitting
  1.93 % over the tolerance (`C-0180`, `T-279`).
- **Every uncoupled reference in `C-0154` and `C-0167` is 1.12× too soft** at the recommended
  cross-section. `C-0167`'s headline — *"the uncoupled four-layer honeycomb tile is flat at
  0.0501417315 and 0.0522223659"* — is a **conservative** reading, which is the safe direction, and
  its *"`0` of `64`"* coupled cells were graded on the same soft object.
- **The coupled re-grade is owed on the tied lattice**, and this challenge does not run it: a
  coupling changes the load path, and `CLAUDE.md` records that a factor measured on a free tile
  does not transfer to a coupled one (`C-0154`'s own `f` reads 0.2468 on the rigidity and 0.9405 on
  the dishing). **RUN (`C-0180`, `T-279`): the coupled median per-realisation ratio is
  `0.902845544–0.988116016` against this free tile's `0.890395426`, so the `1.12×` here
  OVER-states the coupled benefit at every one of the 64 cells.**
- **The ties are a stiffness AND a load.** They are the only elements of the block that carry a
  built-in prestrain (`C-0152` §5), which is [`CH-0228`](CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md).

## 6. What would settle it

Re-run `T-263`'s 64 coupled cells on the tied lattice. It is one argument to
`HoneycombGrillage`'s constructor and the surrogate is model-agnostic, so the cost is a re-run and
not a model.

**DONE (`C-0180`, `T-279`), and the cost estimate held**: two functions, no shared source edited,
and the untied half reproduces all 128 of `C-0167`'s committed values at `4.2e−9`.

## 7. What this challenge does NOT claim

- That the tie is at exactly `s = ±L/2`. A scaffold crossover sits **5 bp** from a staple position,
  so its true axial station is within **1.7 nm** of the rim node; `T-254` emits the influence per
  turn so that sensitivity is readable.
- That `k_θ` at a scaffold turn equals `k_θ` at a staple crossover. No honeycomb measurement of
  either exists in this repository, and the tie is assembled with the same three elements a bond
  has because it is the same covalent object — not because anything measured it.
