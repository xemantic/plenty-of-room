# `T-254` — does a raster TURN sit on the flatness axis at all?

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) §7 and §11, and [`C-0154`](../claims/C-0154-honeycomb-grillage.md) §10 |
| **Verification type** | **logical** (an exact census of the raster's turns against the honeycomb bond lattice, and a lever-arm argument that costs no solve) **+ in-silico** (`C-0154`'s three-dimensional beam-and-bond lattice, a linear prestrain influence bank and a triangle-inequality ceiling) |
| **Units** | lengths **nm**, rise **0.34 nm/bp**, angles **degrees** in prose and **radians** internally, hinge stiffness **pN·nm/rad**, energies **pN·nm** and `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂ |

---

## Formulate

Every raster crossover of an x-raster sits at a row **turn**, which is the block's axial **rim**
rather than its gap-facing face.
[`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) proved that the coefficient of
the *raggedness* on §3's flatness is **exactly zero**, on an axis argument;
[`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) §7 records the parallel for a
**prestrain** as *"an observation and not a result"*; and
[`C-0154`](../claims/C-0154-honeycomb-grillage.md) §10 carries it forward as an open question,
having answered only the **placement-free** version — a ceiling over every choice of ten of its own
**staple**-lattice bonds.

**The two statements are not the same kind of statement, and that is the whole task.** The
raggedness is a perturbation of the block's *geometry* whose direction is in the tile plane; a
prestrain is a **load** (`C-0104`: *an initial stress is a load, not a stiffness*), and a load is
not confined to the coordinate it is applied on — it produces a field. Being on the rim bounds
nothing by itself: `C-0104`'s own subject is a **row-end** crossover prestrain, and it moves a
flatness verdict across `T-5b`.

**Numeric target.** A **coefficient** — peak dishing per unit free stroke per radian of prestrain —
for a prestrain at a raster turn of the recommended block, together with the verdict on whether
that coefficient is zero, and the actual departure a turn carries.

**And the departure has to be re-derived, because the raster does.** `C-0152` prices **ten forced**
crossovers on the `112 / 108` raster at `17.1428571°`. [`C-0151`](../claims/C-0151-closing-raster-selection.md)
shows `112 / 108` does **not** close on caDNAno's published `±5 bp` rule and `102 / 109` does, with
**zero** forced crossovers — so on the recommended design the *forcing* load does not exist. What
does exist at every turn of every honeycomb origami ever folded is `C-0152` §5's own calibration:
under the exact `10.5 bp/turn` geometry an **allowed** scaffold crossover already sits
`8.57142857°` off the line of centres, because caDNAno's `±5 bp` is an integer approximation to a
`5.25 bp` half turn. That is a prestrain on **all** the turns, on **either** raster, and no claim
in this corpus has ever applied it as a load.

**Acceptance predicates.**

- `P1` — the **cheap bound runs first and is decisive about the sign of the answer**: a census of
  which lattice interfaces carry a raster turn, where each sits axially, and what in-plane lever
  arm its covalent link has. If some geometric feature makes the coefficient exactly zero, the
  task ends there and says so with the proof; if none does, the census says *why* — and the
  argument must be an argument about **this** lattice, not a restatement of `C-0147`'s.
- `P2` — the turn census is **derived from the raster path**, not asserted: the path is
  reconstructed in the block's own `(rasterRow, column)` coordinates and asserted equal to
  `honeycombXRasterPath`; every consecutive pair is asserted to be a honeycomb bond; the counts of
  in-plane and through-thickness turns are emitted and asserted to sum to `H − 1`.
- `P3` — the **departure** is re-derived at both rasters from `C-0152`'s own model rather than
  transcribed, and the recommended raster's **zero** forced crossovers is emitted beside
  `112 / 108`'s ten.
- `P4` — the coefficient is emitted **per turn** — every turn's unit response — so that the answer
  is a distribution and not a single number, together with the two summaries a verdict needs: the
  **largest** unit response (which bounds any single turn) and the **triangle-inequality ceiling**
  over the whole turn set (which bounds every sign assignment and every subset).
- `P5` — the load actually carried is evaluated: all turns at `8.57142857°`, and the ten forced
  ones of `112 / 108` at their own departure, with the **sign assignment declared and swept**,
  because no source in this repository fixes it (`CLAUDE.md`: *sweep any sign a symmetry argument
  leaves free*).
- `P6` — every influence function is taken on the lattice **without** prestrain
  (`C-0104`/`CLAUDE.md`: a prestrain contaminates every influence computed from the prestrained
  structure, and it fails silently at the angles that matter), and the linearity of the field in
  the prestrain is asserted rather than assumed.
- `P7` — where the model cannot represent something, that is **stated**: the grillage carries one
  row length, so the two-length raster enters as its 116 bp block extent.

**Geometry and sign conventions, fixed before deriving.**

- The block is `m` raster rows (**in-plane**, pitch `3d/2 = 3.804 nm`) by `n` helices per row
  (**thickness**, pitch `d√3/2 = 2.19624042 nm`), `d = 2.536 nm`; beam index `= rasterRow·n +
  column`; the **gap-facing** face is column `0`, and it is the only face the Winkler foundation
  acts on. `10 × 6` is the recommended cross-section; `15 × 4` is carried as the control.
- `s` runs along the helices, `s = 0` at the block's centre, so the two axial rims are
  `s = ±L/2` with `L = rowBasePairs × 0.34 nm`. `y` is in plane, `z` is the tile normal, `w` is
  the deflection along `z`, `φ` the **roll** about `s`.
- A **raster turn** is a consecutive pair of the x-raster path: the scaffold runs `+s` along one
  helix, turns at that helix's end, and runs `−s` along the next. A turn with **zero** unpaired
  nucleotides *is* a scaffold crossover — a covalent tie between two duplexes at their ends — so
  it sits at `s = ±L/2`, alternating ends along the path.
- A **prestrain** `θ₀` at a tie is the relative roll it is built at: the hinge stores
  `½k_θ(Δφ − θ₀)²`, so it enters the assembled system as a **couple pair** `±k_θθ₀` on the two
  beams' `φ` and changes **no** entry of the stiffness matrix.
- The **departure** an allowed scaffold crossover carries is `8.57142857°` and a forced one of the
  minimal rung `17.1428571°`, both from `C-0152`, both read as a relative roll through `C-0104`'s
  mapping, which is the mapping this corpus already uses for the same quantity.
- Dishing is `T-5b`'s: the peak of the field with its mean and its two rigid tilts removed, over
  the free stroke `p/k_f`. Threshold **0.10**.

---

## Plan

**The cheap half first, and it is a lever arm.** The prestrain's work conjugate is a *roll*, and a
roll reaches the deflection field only through the covalent **link** that ties the two duplexes'
surfaces together — the link constrains `w_a + a_y φ_a − w_b + a_y φ_b`, with `a_y = (d/2)·û_y` the
in-plane component of the bond's own half-vector. So the coefficient of a prestrain on `w` is
**exactly zero if and only if `û_y = 0`** for that bond: a tie stacked purely through the
thickness would roll its two duplexes against each other and lift neither.

That is a one-line test of the honeycomb's own azimuths, and it needs no solve. The three bonds of
a honeycomb site are at `90/210/330°` (sublattice A) or `270/30/150°` (B) in the cross-section, so
`|û_y|` is **1** for the in-plane bond and **1/2** for each of the two through-thickness ones.
**No honeycomb bond has a zero in-plane arm**, and if that is what the census returns then the
answer to *"does a raster turn sit on the flatness axis at all"* is **yes, at every turn**, before
a matrix is assembled — and `C-0147`'s zero is shown not to transfer, for a reason of the same
kind as the one that established it.

**The census is the other cheap half and it is worth running for itself.** A raster turn joins two
consecutive helices of the path, and on an x-raster the path steps through the **thickness** within
a row and **in plane** at a row transition — so a `10 × 6` block's 59 turns are **50** through-
thickness and **9** in-plane, exactly the 50 interlayer interfaces `C-0154` counts and 9 of its 27
in-plane ones. That is a fact about which of the block's own interfaces are scaffold-loaded, and it
also names the modelling gap: `C-0154`'s lattice carries the **staple** crossovers, whose planes are
the 7 bp ladder, and a turn sits at `s = ±L/2`, past the last of them. **A turn tie is therefore an
element the corpus's honeycomb lattice does not have**, and putting it in is a change with a sign
that has to be measured, not assumed.

**Then the solve, and it is affordable because a prestrain is a load.** The field is **linear** in
the prestrain (`C-0104`), so one factorisation and one back-substitution per turn gives the whole
influence bank; the sum of the largest `k` unit responses is a rigorous **ceiling** over every
choice of `k` turns and every sign; and peak dishing is a convex seminorm, so the triangle
inequality is exact rather than heuristic. `C-0154`'s banded, node-major ordering makes one solve
of the `10 × 6` block ~4 000 unknowns at half-bandwidth 243, so 59 unit responses plus a handful of
assembled fields is seconds, not minutes. **The expensive alternative — a nonlinear or per-helix
model — would buy nothing, because the quantity is exactly linear and the placement question is
settled by a bound rather than by a search.**

**The one source change, and why it is additive.** `HoneycombGrillage` builds its bonds from the
staple lattice alone. A raster turn tie is added as an **explicit, optional list** of
`(lowerBeam, upperBeam, node, prestrain)`, defaulting to empty, so every lattice `C-0154` and
`C-0167` measured is **bit-identical** by construction — which is asserted as a test, not claimed.
The turn tie carries the same three elements a bond does (hinge, normal link, axial slip), because
a scaffold crossover is a covalent tie and not a spring on one coordinate.

**What result would falsify this approach.**

| # | falsifier | if it fires |
|---|---|---|
| `F1` | some honeycomb bond has `û_y = 0`, so the roll couple has no in-plane arm | the coefficient is exactly zero on that class of tie and the cheap bound settles the task the other way |
| `F2` | the turn census fails to reproduce `honeycombXRasterPath`, or the in-plane and through-thickness counts fail to sum to `H − 1`, or a consecutive path pair is not a honeycomb bond | the census is wrong and every coefficient here is attributed to the wrong elements |
| `F3` | adding the turn ties moves the **uniform-load** dishing off zero | the solver or the tributary is wrong; a free body on a uniform Winkler foundation translates rigidly whatever its internal ties, and this is the sharpest falsifier the project has |
| `F4` | the field is **not** linear in the prestrain, or an influence taken on the prestrained lattice differs from one taken on `withoutPrestrain` | the whole bank is void and `C-0104`'s trap has been walked into |
| `F5` | the turn-set ceiling at the departure the recommended raster carries **exceeds** `T-5b`'s 0.10 — *declared open* | the raster turns are a flatness term the corpus has never carried, and `C-0154`'s and `C-0167`'s free-tile readings are owed a re-derivation |
| `F6` | adding 59 covalent ties moves the free-tile dishing by more than the emitted convergence departure | `C-0154`'s honeycomb block is missing a structural element, which is a challenge against its free-tile numbers rather than a term in this one |
| `F7` | the recommended raster is found to carry forced crossovers after all, or `112 / 108` to carry none | `C-0151`'s and `C-0152`'s closure censuses disagree with this one and the discrepancy owns the task |
| `F8` | the largest single-turn coefficient is **below** the emitted solve residual | the answer is a zero this method cannot distinguish from noise, and it must be quoted as a threshold rather than as a value |
