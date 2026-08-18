# T-204 — Does `C-0022`'s collar transfer to the `10 × 6` aspect ratio?

**Leaf:** `A7.4`, with `A8.2`
**Raised by:** [`C-0120`](../claims/C-0120-cross-section-comparison.md) and [`C-0122`](../claims/C-0122-honeycomb-station-lattice.md), which both name it as owed
**Verification type:** logical (a perimeter-over-area bound) + in-silico (the sensitivity)
**Units:** nm; dishing dimensionless as a fraction of the free-tile stroke

---

## Formulate

`C-0022` solved the edge collar on a **40 × 40.35 nm** tile, and **every four-layer number in this programme
is read under it unchanged** — including `C-0120`'s cross-section comparison and `C-0118`'s flat coupled
cells, on tiles of 38.08 × 38.04 and 38.08 × 25.36 nm.

### Acceptance predicate

1. The transfer is **bounded before any field is solved**, from the collar's own locality.
2. The flat cells are re-graded over a range of collar scales that **contains** that bound.
3. Whether the bound moves a verdict is stated as a **margin**, not as a yes/no.
4. What the bound does **not** cover is named.

**Falsifiers.** `F1` — some `10 × 6` cell stops being flat at or below its own geometric factor, so the
transfer *does* move the verdict and a re-solve is owed. `F2` — the dishing is not monotone in the collar
scale, in which case a single-factor bound is the wrong instrument entirely.

---

## Plan

**The cheap bound is the whole method, and it needs no solve.** The collar is a **local** rim effect —
`CLAUDE.md` records a sub-Debye 1.65 nm band whose total contribution scales as `1/L` — so its depth and width
are set by **screening** and its share of the load by the tile's **perimeter over area**. That is arithmetic.

**Then measure rather than argue.** Rather than asserting that dishing tracks the collar share, scale both
collar terms over `1.0 … 3.0` — a range that *contains* each cross-section's own geometric factor — and read
where, if anywhere, a flat cell stops being flat. The margin is then the ratio of that scale to the factor the
tile actually needs.

**Justification against cost.** A genuine 2-D Poisson–Boltzmann re-solve at the new aspect ratio is the
thorough answer and costs a study; this is minutes on machinery that already exists, and if the margin is
large the re-solve is not needed. If the margin is small, the re-solve is owed and this says so.

**What would falsify the approach.** That the collar's **shape** — not only its share — moves with the aspect
ratio. It does: a rectangular tile's short and long sides see different fringing. This study scales the share
and holds the shape, and that limitation is recorded rather than hidden.
