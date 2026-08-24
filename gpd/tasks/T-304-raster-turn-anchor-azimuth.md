# T-304 — the tether's SPAN is not a bracket: the anchor azimuth, derived on the lattice

**Leaf** `A8.2`.
**Raised by** [`C-0201`](../claims/C-0201-the-tether-is-a-load-not-a-spring.md) §9 and §10 (`T-299`),
which names it *"the highest-value follow-up this claim names"*.

---

## 1. Formulate

### The object

`C-0201` grades route B — the raster turn as a freely-jointed ssDNA chain between two duplex
ends — and every one of its numbers is quoted at a **span**, the distance between the chain's
two anchoring phosphates.
That span is carried as an **azimuth bracket**, `d − 2r_P = 0.718724283 nm` to
`d + 2r_P = 4.35327572 nm`, because [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md)
and [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) were bounding **reach**, where a
bracket is the right instrument: a turn either closes at some azimuth or it does not.

For a mechanical **element** it is the wrong instrument, and the cost is exact:
over `C-0201`'s 36 corners the free tile runs `0.0569815008` to `0.166312182` of the stroke —
**flat at one end and not at the other**, `24 of 36` corners flat — and the span is the axis that
straddles `T-5b`.

**The span is not unknown. It is a lattice arithmetic.**
A chain leaves helix `a` at the phosphate of that helix's **last paired base** and enters helix `b`
at the phosphate of **its** last paired base, and a phosphate's azimuth is fixed by its base-pair
index and the lattice's own phase — exactly the quantity
[`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) reduces to a residue and
[`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) turns into a sign.

### The numeric target

The azimuth `θ_k` of both anchoring phosphates of each of the **59** raster turns of the
recommended `10 × 6` block on `C-0151`'s drawable **`102 / 109`** raster, the span each implies,
and `C-0201`'s 36-corner free-tile bracket **collapsed onto it**.

### The falsifiable acceptance predicates

**`P1`** — the azimuth is **derived**, not chosen: a closed form in the crossover residue `ρ` and
the lattice constant `b₀` that reproduces
[`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md)'s
`scaffoldDisplacementDepartureDegrees` at **both** allowed residues, at **every** `b₀`, exactly;
and the span is `forcedCrossoverSpan` — `C-0147`'s own `turnPhosphateSpan` consumed unmodified.

**`P2`** — every one of the 59 turns is reported with its reduced residue, its derived `±5 bp`
displacement, the rim it sits at, its exit and entry azimuths and its span; and the report states
whether the span is single-valued over the 59 rather than assuming it.

**`P3`** — the answer is **invariant** over all eight readings of the three free conventions
`(firstAxialSign, mirrored, axialReversed)`, asserted rather than assumed.
`CLAUDE.md` records that a base-pair displacement becomes an azimuth only through the datum's own
handedness, that a residue map is a handedness and must be reversed whenever `z` is, and that
`T-284`'s `F3` test found exactly this on its first real run.

**`P4`** — `C-0201`'s 36-corner bracket is collapsed onto the determined azimuth and **every**
surviving corner is reported with its tension, its two stiffnesses and the **free tile** at
`f = 0.30` and `f = 0.26`, with and without the preload, against `C-0201`'s own two endpoints —
which are **reproduced** rather than transcribed.

**`P5`** — the alternative in which the unpaired loop is carved **out of** the paired row rather
than added **outboard** of it is priced as a different **design**, with its own derived azimuths,
and is never presented as an uncertainty in this one.

**`P6`** — route B's own **uniform** rasters (`C-0201` §7's `92 / 98 / 106 bp` at the three
scaffolds) do **not** close, so their `b₀` is a free design variable; they are reported over all
**21** phases and the span **distribution** over the 59 turns is given, not a single value.

**`P7`** — every number published here is quoted with the raster, the anchor reading and the
`(loop, b, c)` corner it is read at.

### Verification type

**logical** (exact integer residue arithmetic on the lattice this repository already carries, a
closed form for the azimuth, and `C-0147`'s span geometry consumed unmodified)
**+ in-silico** (the same honeycomb grillage, the same tether element and the same free-tile
evaluation `C-0201` used, re-run at the determined span)
**+ literature** (`C-0152`'s allowed-crossover span, `C-0200`'s duplex window and `12 / 16` split,
`T-71`'s measured phosphate radius, and `T-230`'s ssDNA Kuhn and contour brackets).

**TRL 1–3. Nothing here is measured.** `PASS` means model-consistent and traceable.

### Locked units

Lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT` (`k_BT = 4.141947 pN·nm` at 300 K),
stiffness **pN/nm**, angles in **degrees** at every API boundary of this task and radians only
where a trigonometric function is called, base-pair levels **integers** on one global `z`.
Medium: aqueous buffer, `2 mM MgCl₂`, 300 K — `C-0022`'s design state, `10 nm` gap, `0.192 V`.

### Geometry and sign conventions

- `d = 2.536 nm` (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS), rise `0.34 nm/bp`, phosphate radius
  `r_P = 0.908637858 nm` (`T-71`, **measured**).
- **Azimuth `θ` is measured at helix `a` from the line of centres pointing at helix `b`**, in the
  plane of the cross-section, positive in the sense of increasing `z` at `+240/7°` per base pair.
  `θ = 0` is closest approach.
- **The two anchors of one turn are at the SAME base-pair level.** Both helices of a honeycomb
  bond are parallel, same-handed and at one design twist, so `∂(ψ_a − ψ_b)/∂z = 0` identically
  (`CLAUDE.md`, `ForcedCrossoverPrice`'s own header) — a level displacement rotates **both**
  backbones the same way. Hence `ψ_b = θ + 180°` and the span is
  `√(d² − 4 d r_P cos θ + 4 r_P²)`, whose two endpoints are `C-0147`'s own bracket.
- **The exact facing residue is `b₀ + 21/4`, not `b₀ + 5`.** caDNAno's scaffold rule is
  *"five base pairs, **or half a turn**"* and the half turn at 10.5 bp/turn is `5.25` bp
  (`C-0152` §5, `CH-0197`). Since `10.5 bp` is exactly `360°`, `b₀ + 5.25` and `b₀ − 5.25` are the
  **same azimuth**, so the reference is single-valued:
  `θ(ρ, b₀) = fold((ρ − b₀ − 21/4) · 240/7°)`, folded to `(−180°, +180°]`.
- **The anchor reading.** The recommended raster's `102 / 109` are **paired** row lengths and the
  unpaired loop sits **outboard** of the duplex, which is what `C-0200` reads off the built block
  (duplex `28..125` on all 60 helices, scaffold beyond it) and what `C-0201` §7's width arithmetic
  assumes (*"the scaffold buys `perHelix − 28` paired bases, and the row is that many base pairs
  of duplex"*). So the last paired base sits **at** the raster level and
  `anchorOffsetBasePairs = 0` is the reading of the graded object. The alternative reading is
  swept as `P5` and is a different design, not a tolerance.
- The turn census is `honeycombRasterTurnList`'s, unchanged — the same 59 sites route A uses, so
  the comparison with `C-0175`, `C-0180` and `C-0201` is controlled.
- `W` positive **downward**, toward the electrode (`C-0006`), `HoneycombGrillage`'s unchanged.

---

## 2. Plan

### The cheap bound runs first, and here it is the whole answer

Nothing in the primary deliverable needs a solver. The residues are
`HoneycombRasterResidues.reducedResidues`, already in the tree and already asserted against
`C-0136`'s per-helix construction; `b₀` is `HoneycombRasterTurnSigns.classZeroResidue`, which
**refuses** rather than guesses on a raster that does not close; the azimuth is one subtraction and
one multiplication; and the span is `forcedCrossoverSpan`, which `T-246` already tests.
**59 turns, eight datum readings, microseconds.**

The prediction the derivation makes before it is run: on the `102 / 109` raster `C-0187` records
`b₀ = 5` with residues `[0, 10]`, so every turn is an **allowed** scaffold crossover and
`|θ| = 8.57142857°` at all 59 — which is `C-0152`'s own allowed-crossover departure, whose span
is **`0.787091706 nm`**, a number already committed in `gpd/results/T-246-…json`. That is
`9.5 %` above the aligned end of `C-0201`'s bracket and `5.5×` below the worst.
If it holds, the bracket collapses onto its **soft** end.

The second prediction, and it is the one the rim alternation makes non-obvious: the derived
departure alternates with the rim (`+8.57°` at the high rim, `−8.57°` at the low), and the span
depends on `cos θ`, which is **even** — so the two populations collapse to **one** span. That is
`F1`, declared open.

### The collapse costs one re-evaluation and no re-solve

`C-0201` establishes that the field is **exactly linear** in the preload and that the preload is
`C-0104`'s internal initial stress — no entry of the stiffness matrix moves. So pricing a
*determined* span is a re-evaluation of an element whose machinery exists:
`freelyJointedTetherState(span, n, b, c, kT)` and `honeycombTetheredLattice`, both `T-299`'s,
called at one span instead of three. The 36 corners fall to **12** (three loop lengths × two Kuhn
× two contour), plus `C-0200`'s ordered `24 / 32` split as a two-population state.
One free-tile solve per corner per composite fraction.

### The coupled cells, and why they are a check rather than a search

`C-0201` reads `0 of 64` flat at the 90th percentile at **every** tethered state and `0 of 64`
untied, with the tightest cells `0.101931622` untied and `0.102016157` at the softest tether.
The determined span sits between the softest and the built corner, so the coupled verdict is
**predicted** not to move. Grading `C-0167`'s own 64 cells at the determined adverse corner, on
the same 4 000-realisation stream restricted per cell, is one sixth of `T-299`'s own cost and it
is what turns a prediction into a measurement (`F6`).
A smoke pass at 150 realisations runs the whole prose and serialisation path first
(`CLAUDE.md`: *build the result and write the JSON before formatting any prose*).

### What would falsify this approach

- **`F1`** *(declared open)* — the 59 turns do **not** all take the same span on the `102 / 109`
  raster. The derivation predicts one, because the departure's rim alternation and the anchor
  offset's rim alternation both enter through `cos θ`. If it fires the deliverable is a
  distribution and not a value, and that is the finding.
- **`F2`** — the derived span is not `C-0152`'s own allowed-crossover span `0.787091706 nm`,
  read out of `gpd/results/T-246-forced-scaffold-crossover-price.json`, to `1e−9`.
  Then two independent constructions of one geometry disagree and the derivation is wrong.
- **`F3`** — the closed form `θ(ρ, b₀)` disagrees with `C-0187`'s
  `scaffoldDisplacementDepartureDegrees` at either allowed residue at any of the 21 `b₀`.
- **`F4`** — the span, or any turn's `|θ|`, is **not** invariant over the eight readings of
  `(firstAxialSign, mirrored, axialReversed)`. `CLAUDE.md` records that the azimuth constant and
  the axial datum must travel together and that `T-284`'s `F3` found the omission on its first
  real run; this is that test, on a new quantity.
- **`F5`** *(declared open)* — the determined span leaves the **free tile** past `T-5b`'s `0.10`
  at some surviving corner, i.e. the collapse does **not** settle the verdict `C-0201` left
  straddling. Its firing would be the finding and would keep the azimuth on the queue.
- **`F6`** *(declared open)* — the determined span moves a **coupled** verdict against
  `C-0167`'s untied `0 of 64`.
- **`F7`** *(declared open)* — no `b₀` exists at which a uniform route-B raster puts every turn's
  span inside the aligned half of the bracket, i.e. route B's own tile cannot be drawn with soft
  tethers everywhere.
- **`F8`** — the reach bound refuses at some determined corner: `span ≥ contour`, so the chain
  cannot reach at all. It should not fire — `0.787 nm` against a contour of at least `9.75 nm` —
  and running the `n = 0` case of a reach bound before the case you wanted is `CLAUDE.md`'s own
  rule.
- **`F9`** — the preload's predicted rim closure at the determined span exceeds the steric slack
  `d − 2r_P`. `C-0201`'s `F9` re-taken where the span is determined rather than bracketed.

### What this task will NOT establish

- It does not re-open the raster, the cross-section, the placement search or the distribution
  rule. Every one of those is `C-0151`'s, `C-0141`'s and `C-0167`'s and is untouched.
- It does not settle which rim takes `C-0200`'s `24 nt` half; that is a free convention of that
  reading and it is stated, not swept.
- It does not read the deposited `10 × 6` file's own `b₀`, so it says nothing about the **built**
  block's anchor azimuths — only about the recommended raster and about route B's own uniform
  ones, where `b₀` is a design variable rather than a datum to be recovered.
- It is not a folding experiment and it is not a measurement of an ssDNA tether in a rim.
