# T-199 — Is 10 × 6 a better tile than 15 × 4?

**Leaf:** `A8.2`
**Raised by:** [`C-0119`](../claims/C-0119-honeycomb-raster-width.md)
**Verification type:** in-silico (beam-and-hinge grillage) + logical (a second-moment bound)
**Units:** rigidity in pN·nm, length in nm, dishing dimensionless as a fraction of the free-tile stroke

**Conventions**, restated: Douglas et al.'s nomenclature `m × n` is `m` x-raster rows of `n` helices; in this
programme's grillage `m` is the beam count and `n` the layer count. The span along the helices is fixed at
`C-0086`'s buildable **112 bp** for every cross-section, so `m` sets the other side. Load is `C-0022`'s solved
collar at 2 mM / 10 nm / 0.192 V; **no coupling anywhere**; flat means below `T-5b`'s 0.10.

---

## Formulate

`C-0119` found that the tile this programme recommends is **design (i), 15 × 4**, of the caDNAno paper — and
that the paper's own conclusion is that **10 × 6** yields the greatest fraction of defect-free objects. Both
are **60** helices, so the choice costs no scaffold.

**This is the first design axis this programme has been handed by a measurement rather than deriving.**

### Acceptance predicate

1. Both cross-sections are solved on identical machinery, load and span, and their free-tile dishing compared.
2. `C-0116`'s composite-fraction threshold is re-read **per cross-section**, because it is a function of the
   parallel-axis factor and that moves with the layer count.
3. `C-0116`'s and `C-0109`'s own numbers reproduce on 15 × 4, licensing the comparison.
4. Whatever the flatness says, the **consequences of changing the cross-section** are stated — a different
   `m` at fixed span is a different tile, not merely a stiffer one.

**Falsifiers.** `F1` — 10 × 6 is not flatter, in which case the paper's yield recommendation and this
programme's flatness criterion disagree and the choice is a trade. `F2` — `C-0116` does not reproduce, so no
comparison is licensed. `F3` — some cross-section is **not** flat at the measured coupling, which would bound
the aspect ratio from the flatness side.

---

## Plan

**The cheap bound runs first and needs no plate.** `Σy²` for `n` layers at spacing `d` is `n(n²−1)d²/12`, so
the parallel-axis **excess** scales as `(n² − 1)` — a pure integer function of the layer count with no
material constant in it. Six layers therefore carry `35/15 = 2.333×` the excess of four **at the same 60
helices**, and the prediction is that 10 × 6 dishes less unless the narrower span undoes it. Asserted as a
test before anything is solved.

**Method.** Solve four of the paper's 60-helix cross-sections (15 × 4, 10 × 6, 6 × 10, 3 × 20) on the same
grillage, load and span; locate each one's flatness threshold with the same scan-then-bisect `C-0116` used;
report the footprint beside every row.

**Justification against cost.** Minutes: `tile/FourLayerTile.kt` and `tile/CompositeFractionThreshold.kt` are
both parameterised on the layer count already, and the load is read from a committed result file. Against
that, the tile is the object every structural claim in this programme describes, and it was chosen without
anyone knowing its own source recommends a different one.

**What would falsify the approach.** That the cross-sections are not comparable at fixed span — that a
narrower tile changes `C-0022`'s solved collar enough to invalidate reading it unchanged. That is a real
limitation and it is recorded rather than resolved: the collar was solved on a 40 nm square.
