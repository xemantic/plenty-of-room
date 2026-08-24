# `T-315` — route B's uniform raster re-graded at the **resolved per-bond** link

**Leaf** `A8.2`.
**Raised by** [`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md) (`T-310`) §5 and
[`CH-0265`](../challenges/CH-0265-756-of-756-is-a-reading-at-the-penalty.md), against
[`C-0207`](../claims/C-0207-the-uniform-raster-is-flat-with-its-tethers.md)'s headline `756 of 756`.

---

## Formulate

### The question

`C-0207` graded route B's three uniform paired rows — `92 / 98 / 106 bp`, what M13mp18, p7560 and
p8064 afford at the built `28 nt` allowance — with the 59 per-turn tethers their own geometry
implies, and found the free tile flat at **`756 of 756`** cells (21 lattice phases × 3 widths × 12
chain corners), `0.0483790868–0.0946863482` of the stroke against `T-5b`'s `0.10`.

That is a reading at `k_link = 1e4 pN/nm` — `HoneycombGrillage.RIGID_LINK_STIFFNESS`, a numerical
**penalty**, `39.2452209×` (`10000 / 254.808095`) above every rung a crossover connector can
supply. `C-0208` has since resolved a bond's normal link by the bond's own direction,
`k_link = k_radial·unitZ² + k_transverse·unitY²`, so the same lattice's staple bonds sit at
**`254.808095 pN/nm`** in plane (`unitZ = 0`, `C-0205`'s shear ceiling, exact there) and
**`629.20588`–`1365.32644 pN/nm`** through the thickness (`unitZ² = 3/4`).

**Does `756 of 756` survive the resolved link?**

### Numeric target and falsifiable acceptance predicates

| | |
|---|---|
| `P1` | route B's `756` uniform-raster cells re-graded at the resolved per-bond link at **every** declared rung, and the flat count against `T-5b`'s `0.10` stated **with the link stiffness attached** |
| `P2` | a **penalty control** rung (`k_link = 1e4`, radial unset) that reproduces `C-0207`'s committed 756 cells to `≤ 1e-8` of the stroke — so the re-grade is against the same object |
| `P3` | the default lattice is **bit-identical**: `assembleLoad` over **every** degree of freedom, and the crossover **site set** beside it, at all three row lengths |
| `P4` | a cheap bound stated **before** the grading — a crossing threshold on `C-0207`'s own committed cells and a predicted count — and the prediction judged against the measurement |
| `P5` | `CH-0265` adjudicated in its own `**Status**` row |

### Units, locked

nm, pN, pN/nm, pN·nm; `k_BT = 4.141947 pN·nm` at `T = 300 K`; pressure in pN/nm² (= 1 MPa);
dishing dimensionless, as a fraction of the free stroke of the **same** lattice.

### Geometry and sign conventions, fixed before deriving

- `s` runs **along** the helices, `y` **across** the raster rows in the face plane, `W` **normal**
  to the face — the coordinate the Winkler foundation resists and the link residual is written on.
  `Φ` is the roll about a beam's own axis.
- A bond's line of centres is the unit vector `(unitY, unitZ)` between the two beam axes. An
  **in-plane** bond has `unitZ = 0`; one running **through the thickness** has `unitZ² = 3/4`.
- The normal link is `k_radial·unitZ² + k_transverse·unitY²` (`C-0208`), so the **transverse**
  constant is the one an in-plane bond reads and the **radial** one is a resistance to a change of
  the interhelical **separation**. `radialLinkStiffness = null` means the standing single-scalar
  object, by **identity** and not by arithmetic.
- A tether's tension is positive when the chain pulls its two ends **together**; it is a **load**
  in `C-0104`'s exact sense and changes no entry of the stiffness matrix.
- Peak dishing is the largest departure from the best-fit affine field over the face, divided by
  the free stroke — the mean deflection of the **same** lattice under a uniform interior pressure.
  `T-5b`'s tolerance is `0.10`.
- The lattice phase is `b₀`, the class-zero residue, in `[0, 21)`. `firstAxialSign = +1`.

### Verification type

**logical** (the link resolution is closed form and the bond census is exact integer geometry)
**+ in-silico** (the same honeycomb grillage, the same `T-299` tether element, the same `T-307`
per-turn spans — only the link moves) **+ literature** (`C-0205`'s shear ceiling and `C-0208`'s
radial bracket, both re-derived here through the corpus's own functions rather than transcribed).

---

## Plan

### The cheap bound runs first, and it needs **no solve at all**

`CH-0265`'s adverse direction is read off `C-0201`'s **coupled `p90` under measured staple
dropout** — `0 of 16`, rising as the link softens. This study grades the **free tile**, which is a
different quantity, and the corpus already carries a free-tile link sweep on this very
cross-section: `C-0194`/`T-297`'s `sweep` block, six decades of `linkStiffness` at `10 × 6`.

So the cheap bound is one division per committed row:

1. read the largest free-tile **amplification** `T-297`'s sweep records between `1e4 pN/nm` and its
   softest rung `41.4338953 pN/nm` — a rung **6.15× below** the softest bond the resolved lattice
   contains, so the ratio is a deliberate over-estimate;
2. divide `T-5b`'s `0.10` by it to get a **crossing threshold** on `C-0207`'s committed cells;
3. count how many of the 756 exceed it.

That prediction is emitted **before** the grading section runs. It is a direction and a magnitude
measured on a **neighbouring** object (the `116 bp` block, at `enhancement = 18.4938242` against
this study's `21.1851817`, with route-A ties or none rather than tethers), so it is a **prediction
and not a theorem** — which is exactly why the study grades and does not estimate.

### The method, and its cost

A re-grade on an existing lattice: nothing new is modelled. Four rungs of the link, each the whole
`3 × 21 × 12` grid, loaded and unloaded:

| rung | transverse | radial | in plane | through the thickness |
|---|---|---|---|---|
| `penalty` (control) | `1e4` | unset | `1e4` | `1e4` |
| `resolved ceiling` | `254.808095` | `1735.49922` | `254.808095` | `1365.32644` |
| `resolved floor` | `254.808095` | `754.005141` | `254.808095` | `629.20588` |
| `uniform shear ceiling` | `254.808095` | `254.808095` | `254.808095` | `254.808095` |

The fourth rung is `C-0208`'s own first radial rung and is the **softest defensible lattice**: it
puts every bond at the shear ceiling, which is what `C-0205` bisected its uniform thresholds on.
It is carried because a bound taken on a uniform link only bounds a per-bond one if the dishing is
monotone in the link — `CH-0264`'s trap, and it is **measured** here rather than assumed.

Cost, measured before committing to it: a `92 bp` solve of this lattice runs in `~0.1 s`
(`T-307`'s own smoke pass, 145 solves in 15 s of compute), so `4 × 756 × 2` graded solves plus
`4 × 3 × 59` unit-tension bank columns is `~6 800` solves, `~15` minutes. The alternative — a
placement search — is not what the question asks: `C-0207` §7 already records that the **coupled**
reading at these widths needs one, and this task inherits that as carried, not answered.

### What would falsify this approach

- The penalty control failing to reproduce `C-0207` (`F2`) would say the re-grade is on a
  different object and nothing below it can be read.
- The uniform-load falsifier firing at the resolved link (`F1`) would say the lattice is wrong
  before any verdict is taken.
- Non-monotonicity of the free-tile dishing in the link (`F6`) would remove the bounding argument
  the cheap bound rests on, and the answer would then stand only at the rungs actually graded.

---

## The declared falsifiers

Declared **before** the study was run, in the commit before the result.

| # | falsifier | open? |
|---|---|---|
| `F1` | a uniform pressure on the free tethered lattice **at the resolved link**, preload off, dishes more than `1e-9` of the stroke at any of `92 / 98 / 106 bp` | |
| `F2` | the penalty control rung fails to reproduce `C-0207`'s committed `freeTileWithPreload` at all 756 cells to `1e-8` | |
| `F3` | a lattice built through this task's own entry point at `radialLinkStiffness = null` and the default link is **not** bit-identical to `UniformRasterTethers.lattice`'s, on `assembleLoad` over every degree of freedom or on the crossover site set, at some row length | |
| `F4` | the bond census is not two-valued in the resolved link, or an in-plane bond's link departs from the transverse constant by more than `1e-9` relative, or a through-thickness bond's from `¾ k_radial + ¼ k_transverse` by more than `1e-9` relative | |
| `F5` | **the flat count at the resolved floor is not `756 of 756`** — the deliverable's own question | **open** |
| `F6` | the free-tile dishing at the deciding cell is **not monotone** in the link stiffness over the four rungs | **open** |
| `F7` | the `C-0104` triangle-inequality ceiling, rebuilt at each rung's own link, is exceeded by a measured dishing at some cell | |
| `F8` | a cell flat at the penalty is **not** flat at the resolved link although its penalty reading lies below the cheap bound's crossing threshold — the cheap bound's own falsifier | |
| `F9` | the phase recommendation `b₀ = 5 / 16 / 9` (`C-0207` §3) changes at the resolved link | **open** |
| `F10` | the tether element list is not identical across the rungs — i.e. the link resolution has reached the chain, which it must not | |

## What this task does not do

- It does not grade the **coupled** cells. `C-0207` §7 stands: at `92 / 98 / 106 bp` the station
  ladder, the plan ceiling and the centro-symmetric family all move, so that is a **placement
  search** and not a re-grade.
- It does not re-open the span census, the raster, the cross-section or the chain model.
- It carries, rather than resolves, that the whole route-B branch is what the only folded instance
  of this cross-section does (`C-0193`, `C-0200`), and that route A is drawable and undemonstrated.
- The **radial** constant remains unsourceable (`C-0208` §6); it is carried as a bracket and the
  answer is stated at both ends of it.

---

## Post-run note on `F4` — the declared criterion was wrong about one rung, and both readings are published

`F4` as declared above asks for a **two-valued** bond census *"in the resolved link"*, and the
study read that as *two-valued at every rung that is not the penalty*. The fourth rung sets the
radial constant **equal** to the transverse one, so its two readings are **one number by
construction** and its census is correctly one-valued — at 3 of 12 `(width, rung)` rows, every one
of them that rung.

The declared criterion is **kept as written**, and the result file emits **both** verdicts:
`F4 as declared` (**FIRED**) and `F4 corrected` — *one-valued where a rung's two readings agree at
the census's own `1e-9` quantisation and two-valued where they do not* — which does not fire.
The geometry half of the same falsifier is clean at every row: `⟨unitZ²⟩` is exactly `0` in plane
and exactly `0.75` through the thickness, and every link departure is exactly `0.0`.

`CLAUDE.md`: *a pre-registered criterion can still be arithmetically wrong, so publish both
readings rather than picking one.*
