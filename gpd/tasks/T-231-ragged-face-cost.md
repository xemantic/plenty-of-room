# `T-231` — what does a 4 bp ragged face cost a honeycomb tile that §3 asks to be FLAT?

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) *Still open* item 2 |
| **Verification type** | **logical** (exact integer lattice arithmetic on the rise and on the honeycomb cross-section, plus two closed forms already in the corpus — the plate ripple transfer function and the slit's transverse eigenvalue) **+ literature** (the blunt-end stacking range, consumed from `gpd/data/T-139-blunt-end-stacking-literature.md` with its own read flags) |
| **Units** | lengths **nm**, rise **0.34 nm/bp**, `k_BT = 4.141947 pN·nm` at 300 K |

---

## Formulate

`C-0140`'s recommended two-length raster, **112 / 108 bp**, leaves the tile's two end faces ragged
by **4 bp = 1.36 nm** and **8 bp = 2.72 nm**. §3 specifies a tile *between two bodies*, `C-0022`'s
edge collar is solved on a **flat** rim, and `C-0005`'s gap resolution is **1.46 nm** — the same
size as the relief. `C-0142`'s surviving coupled cells sit at **0.0900–0.0954** against `T-5b`'s
**0.10**, so a few per cent is not obviously free.

**Numeric target.** The relief, read as a load-bearing geometry: **which surface** carries it,
what it costs the plan budget, the flatness verdict and the edge field, and what it buys.

**Acceptance predicates.**

- `P1` — the **axis** is settled before anything is priced: which of the tile's surfaces the
  raggedness lives on, and what the coefficient of the raggedness on §3's gap-facing flatness is.
- `P2` — the raggedness is emitted **per helix** for all 60 helices of design (i), with the front
  and rear spreads reproducing `C-0140`'s 4 and 8 bp at departure `0.0`.
- `P3` — the modulation's own **wavelength** is derived from the cross-section (not assumed), and
  the flatness cost is bounded by a closed form quoted with the correction `CLAUDE.md` records for
  it at a free edge.
- `P4` — a **threshold** is quoted: how much the raggedness would have to be worth for `C-0142`'s
  tightest surviving cell to lose its verdict.
- `P5` — the whole family of admissible two-length pairs is scored on **both** axes — the width
  departure `C-0140` selected on, and whatever this task finds the raggedness is worth — and the
  recommendation is restated or changed.
- `P6` — where no model in this repository can read the geometry, that is **stated**, not
  approximated silently.

**Geometry and sign conventions, fixed before deriving.**

- The tile is a four-layer honeycomb block: helices parallel to `x`, `15` raster rows at the
  in-plane pitch `3d/2 = 3.804 nm`, `4` helices per row at the layer pitch `d√3/2 = 2.196 nm`,
  `d = 2.536 nm` (`C-0141`'s cross-section, which is the corrected one).
- §3's **gap** is along `z`, the tile normal. The gap-facing surface is the outermost layer's
  sidewalls; **flatness** in `T-5b` is a deflection field `w(x, y)` normal to that surface.
- The two **ragged faces** are the planes `x = 0` and `x = L`, i.e. the tile's in-plane **rim**,
  where the helices terminate. A face's **raggedness** is `max − min` of its own helix end levels,
  in base pairs, exactly as `C-0140` emits it.
- A **clearance** below `0.34 nm` is below the resolution of the design language and cannot be
  corrected, only removed (`CLAUDE.md`); every length here is therefore reported in **rises** as
  well as nm.

---

## Plan

**The cheap bound is one question and it runs before any arithmetic: which axis is the relief on?**
A four-layer block's gap-facing surface is a row of parallel duplex **sidewalls**, all at one
column of the cross-section; a row length changes where a helix **ends**, which is a coordinate in
the plane of the tile and not along its normal. If that is right, the coefficient of the
raggedness on §3's flatness is **exactly zero** — a symmetry statement of the same kind as
`CLAUDE.md`'s *"the vertical traction on a vertical wall is `ε E_z E_x`"*, and it closes the
headline threat without a solve. It is also exactly the trap `CLAUDE.md` names: *"before
substituting a measurement into an exclusion width, ask which **axis** it is on"*.

**Then price the three things it does touch, each with a closed form that already exists here.**

1. **Plan budget** — an outboard bound is `edgeX/2 − outermost root` and contains no interhelical
   distance (`C-0069`); a row 4 bp short loses 4 bp of it. Quoted against `C-0141`'s honeycomb plan
   ceilings, which are already emitted.
2. **Flatness** — the relief is a rim-position modulation with a wavelength the cross-section
   fixes. A plate on a Winkler foundation attenuates an interior ripple by
   `1/(1 + (2πℓ/λ)⁴)`; `CLAUDE.md` records that this **over**-attenuates a *rim* perturbation by
   **50×** against a finite-plate solve, so the bound is taken with that penalty applied and the
   softest (single-layer) bending length, both of which run the conservative way.
3. **Edge field** — a rim feature is resolvable only if it is wider than the slit's own lateral
   response length, `q₀² ≥ κ² + (π/2h)²` (`C-0110`), which is a closed form and needs no solve.

**And price what it buys.** A ragged face is not only a cost: all three of Rothemund's measured
anti-stacking remedies work by denying a **terminus** a coaxial partner, and a face whose ends
are staggered past the blunt-end stacking range denies it geometrically. The range is measured and
already consumed here (`C-0079`/`C-0085`), so this is a comparison and not a new number.

**Why no solve.** This repository's flatness machinery reads **one** `edgeX`; a per-row row length
is not a parameter of `OrigamiGrillage` or of `HoneycombCoupledTile`, and extending them is a
change to a shared main source that two concurrent agents are running studies against. The
deliverable `T-231` asks for admits *"or a statement that no model in this repository can read
it"*, and the useful form of that statement is a **bound and a threshold** — which is what the
plan above produces, at the cost of arithmetic.

**What would falsify this approach.**

- `F1` — the ragged face turns out to be a **gap-facing** face. Then the coefficient is not zero,
  the whole cheap bound is void, and the task needs the solve it is declining.
- `F2` — the front/rear spreads fail to reproduce `C-0140`'s 4 and 8 bp.
- `F3` — the modulation wavelength comes out **longer** than the plate's bending length, so the
  ripple argument attenuates nothing and the flatness cost is unbounded by this route.
- `F4` — the bounded flatness cost **exceeds** the threshold that moves `C-0142`'s tightest cell.
  Declared **open**: this is the outcome the task exists to detect.
- `F5` — the relief is smaller than the **0.34 nm** design quantum, so it is not a design variable
  at all and every trade priced here is noise.
- `F6` — the recommendation changes under the second axis, i.e. `C-0140`'s 112 / 108 is not the
  best pair once raggedness is priced. Declared **open**.
