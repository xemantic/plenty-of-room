# CH-0164 — the *"ten eight-column phases collapse to two"* is a collapse from a **translation** to a **parity**, and it does not rest on `38.08 = 7 × 5.44`

| | |
|---|---|
| **Against** | [`C-0133`](../claims/C-0133-twist-corrected-raster-row.md) *Still open* item 1 — *"`C-0090`'s collapse of `C-0015`'s ten eight-column phases to two is an identity in the **uniform** 16 bp pitch, and a twist-corrected row has no uniform pitch. … **That is the largest single gap here**"* — and the framing it inherits from [`C-0090`](../claims/C-0090-buildable-raster-width.md) Deliverable 2, *"the row-end scaffold crossover … is a lattice point only at the phases `b ≡ 8 (mod 16)`, i.e. **8 and 24**"* |
| **Raised by** | [`C-0136`](../claims/C-0136-mixed-domain-phase-and-honeycomb-twist.md) (`T-216`) |
| **Kind** | **methodological** — a gap correctly identified and misattributed. The cause named (*a uniform pitch*) is not the cause, and the consequence drawn from it (*the phase results may not transfer*) is the opposite of what follows |
| **Status** | **OPEN, and it makes `C-0133`'s own position stronger.** No number of `C-0090` or `C-0133` moves; `C-0133`'s two published 110 bp enumerations are reproduced here at relative departure `3.1e−10` and `8.5e−10`. What changes is *what a phase is* on a seamless row, and therefore what does and does not transfer to a mixed-domain one |

---

## The ground

`C-0015`'s phase is a rigid **translation** of the 8 bp plane lattice relative to the tile, quantised
at the rise, of period 32 bp. A *seamless* raster row's two ends **are** the tile edges along the
helix axis and both carry a scaffold crossover (`C-0086`, `C-0095`), so both end columns are pinned.
Translating the pattern by `t` requires `0` and `N` to remain columns, i.e. `−t` and `N − t` to have
*been* columns; `0` is the smallest column and `N` the largest, so `t = 0`.

**The admissible translation group of a seamless row is `{0}` — at 110 bp and at `C-0086`'s uniform
112 bp alike.** Enumerated over every base-pair translation of the period rather than argued: 223
translations checked at 112 bp, 219 at 110, one admitted at each.

So what survives at 38.08 nm is not two lattices. Reconstructed from `rasterColumnLayout`, phases 8
and 24 give

- **identical column positions**, to `1e−12` nm at all eight columns, and
- **inverted parities**, at all eight.

`C-0015` already names that object — *"a shift by one column pitch leaves every column position
unchanged and hands every interface the other parity's columns — a physically different sheet"* —
and one column pitch is two 8 bp planes, so the shift also exchanges every duplex's `EAST` and `WEST`
azimuths. **The two "phases" are one column lattice, two interface parities, and two opposite faces
for the arms to point out of.** Their station counts differ (52 against 53) and `C-0090` measures the
cost without naming it: **0.0621469105** of the stroke at phase 8 against **0.070693794** at phase 24.

## What is wrong with the stated cause

`38.08 = 7 × 5.44` is what allows a **uniform** 16 bp pitch to be seamless at all — it is the
condition that an integer number of equal domains spans the row. It is **not** what fixes the phase;
seamlessness does that, and it does it identically on a non-uniform pitch, where the column set is
pinned at both ends **by construction**.

So the inference *"a twist-corrected row has no uniform pitch, therefore its phase lattice is
unknown"* has the sign reversed. The mixed-domain row does not lose a phase variable it never had;
it inherits the **parity binary** verbatim, and the binary costs almost exactly what it costs on the
uniform row — measured here, **1.169×** in the best 34-root dishing at 110 bp against **1.137×** at
`C-0090`'s 112 bp.

## What the mixed row gains instead, and no claim has swept it

An **arrangement** axis. A uniform row has exactly **one** arrangement of its domains; the 110 bp
twist-corrected row has **21** — every one of which carries **eight columns**, because the column
count is the identity `domains + 1` where the uniform lattice's count is a function of the phase —
**3** with a centro-symmetric column set, and **12** distinct up to reflection. With the parity that
is **42** column lattices against the 112 bp row's **2**.

And the condition `C-0063`'s exhaustive family actually needs is a centro-symmetric **station**
lattice, which is **not** a centro-symmetric column set: **6** arrangements have one under some
out-of-plane offset convention, giving **7** enumerable (arrangement, convention) lattices, and
**three** of those need no mirroring at all — so they carry no `30.0°` station and their whole
azimuth departure is `4.2857°`. `C-0133`'s mirrored convention, and the 8 of 52 stations it puts at
30°, is a cost of its **arrangement** rather than of the twist correction.

`C-0133` selects among the three on the **peak register angle** and states so. Re-read on the
flatness over all **seven** enumerable lattices, its choice is confirmed under the graded field it
was selected for (**0.0580196384**, the best of seven) and **displaced at zero prestrain**
(0.0552787638 on a different arrangement, where `C-0133`'s is fourth). The best lattice carrying **no**
mirrored offset — and therefore **no 30° station**, `4.2857°` everywhere — dishes **0.0629599351**,
**1.085×** the recommended one and well inside `T-5b`. And the two arrangements that admit a full
**24-rise** arm, so that `CH-0159` would not apply, are exactly the two whose 34-root family
collapses `163 296 → 11 664` and whose best placement is **outside** `T-5b`.

## What it does NOT touch

`C-0090`'s numbers, its two admissible phases, its arm ceiling and its verdict are all untouched, and
so are `C-0133`'s. What it removes is the standing procedure *"sweep the 32 phases"* as something a
new lattice inherits: on any seamless row there is nothing to sweep but a **binary**, and the axis a
mixed-domain row actually opens is one the procedure does not mention.
