# CH-0014 — Four numbers in `C-0009` are maxima over a *sample* of the staple layout, reported as properties of the sheet

| | |
|---|---|
| **Raised by** | [`T-14`](../tasks/T-14-crossover-phase-and-registration.md) / [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) |
| **Against** | [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) — four statements, listed below |
| **Type** | methodological, with numeric grounds |
| **Status** | **open** — the challenged numbers stand as *samples*, not as extrema or as class properties |
| **Verdict sought** | `C-0009`'s four statements re-qualified. **No verdict of `C-0009` is overturned**; three of the four numbers move in the unsafe direction and one moves in the safe direction, and the fourth inverts a statement `T-2` is consuming |

---

## The single methodological ground

`C-0009` treated the staple layout as a **nuisance parameter it had sampled**, when it is a **design variable with a space of its own**.

That space is now known exactly. The crossover column lattice has a fixed pitch `p/2 = 16 bp`; what a layout chooses is its **phase**, which is quantised to base pairs and has period `p = 32 bp` — so there are exactly **32** column layouts for any tile. The attachment sits somewhere in the `p × d` one-crossover unit cell, a two-dimensional continuum of which `C-0009` evaluated **four points**. And the attachment *pattern* is a `(columns × rows)` rectangle, of which `C-0009` evaluated the **square diagonal**.

Four of `C-0009`'s reported numbers are extrema or class properties taken over those samples. All four are quoted verbatim below.

---

## Ground 1 — the 19 % attributed to the crossover column count is 0.3–3.4 %, and its sign flips

> **The crossover count is not a convergence parameter and does not converge** — it is a physical property of the design, and moving from 7 to 8 columns moves the peak crossover force by 19 %.

Reproduced exactly: the concentrated-lever peak crossover force is **44.146 pN** at seven columns and **37.139 pN** at eight, a ratio of 1.189.

But the two lattices were compared **with the load held at the same absolute point**, `(0, 0)`. That point is a *different registration* in the two lattices: at the eight-column phase it is 2.72 nm from the nearest column, at the seven-column phase it is **on** one. Registration is the larger of the two levers, so the comparison moves both variables and attributes the sum to one of them.

Controlled — comparing best registration against best, and worst against worst, over the complete 288-point cell at every one of the 32 phases:

| load class | 7 columns | 8 columns | ratio | `C-0009`'s uncontrolled figure |
|---|---|---|---|---|
| concentrated lever, best registration | 34.994 pN | 36.220 pN | **0.966** | — |
| concentrated lever, worst registration | 50.108 pN | 49.942 pN | **1.003** | **1.189** |
| discrete anchor, best registration | 5.143 pN | 5.654 pN | **0.910** | — |
| discrete anchor, worst registration | 7.875 pN | 8.243 pN | **0.955** | — |

**The column-count effect is 0.3–3.4 % for a concentrated attachment and 4.5–9.0 % for a discrete anchor, and in both cases *seven* columns is the better layout** — the opposite of what the uncontrolled comparison implies for the lever. Within a column count, the base-pair phase is worth **under 0.5 %**.

This moves in the **safe** direction for `C-0009`'s own conclusions: the column count is a smaller uncertainty than it recorded. It moves in the unsafe direction for anyone who reads the 19 % as the size of the layout lever, because the real lever is 46–60 % and it is registration.

## Ground 2 — centro-symmetry is a property of `C-0009`'s eight-column lattice, not of a Rothemund sheet

> the symmetry group **corrected**: the lattice is centro-symmetric and **not** mirror-symmetric, both asserted.

The assertion is correct for the lattice `C-0009` asserted it on, and is generalised — in `C-0009`'s prose, and in the `CLAUDE.md` entry it produced — to "a Rothemund sheet".

Under the point inversion a column index maps `c → n−1−c` and an interface index `b → N−2−b`, so the crossover parity `c + b` is preserved **exactly when `n + N` is odd**. With `n = 8` columns and `N = 15` duplexes, `23` is odd and the lattice is centro-symmetric. With `n = 7` it is not, and neither mirror is a symmetry either: **the seven-column tile has the trivial point group.**

Over the 32 base-pair phases of a 40 × 40.35 nm tile, **10 are centro-symmetric and 22 have no symmetry at all** — and the seven-column lattice `C-0009` used in its own convergence sweep is one of the 22. Asserted as a test at both counts (`CrossoverLayoutTest`, gate 3), with a residual of `< 1e−8` at eight columns and `> 1e−3` at seven.

## Ground 3 — "the worst case anywhere in the sweep" is 27 % low, and the concentrated case crosses an allowable

> **The worst case anywhere in the sweep** … `k_f` × 0.25, one anchor at ten times the layer stiffness: 30.07 pN on the anchor, **`11.54 pN` on a single crossover**.

That is a maximum over `C-0009`'s **sampled** anchor placements — the anchor sat at the tile centre in every case. Swept over the whole unit cell at every phase, the maximum over the same `k_f` range and Chen et al.'s `α` range is **14.65 pN**, i.e. `C-0009`'s figure is **27 % low**, still inside but near the top of the 10–15 pN unzip band rather than at its bottom.

The same correction applies to the concentrated attachment, and there it crosses an allowable:

| quantity | `C-0009` | `T-14`, registration swept |
|---|---|---|
| peak crossover force, one concentrated attachment | 37.14 pN | **50.13 pN** |
| verdict against the 48 pN quasi-static single-duplex shear allowable | 1.29× margin | **above it** |

**A single-attachment output coupling is therefore not merely "above unzip"; at the worst registration the crossover force itself reaches the duplex shear allowable**, which `C-0009` reported only for the duplex's own transverse shear.

## Ground 4 — "flatness needs more attachment points than the tile has crossovers" is a square-grid result, and it inverts

> **Both models need 64.** … **Flatness needs more attachment points than the tile has crossovers.**

`C-0009` searched the **square diagonal** `s × s` of the `(columns × rows)` design space. The sheet is **25.6× stiffer along the helices than across them**, so the square grid is not the shape a designer would choose, and it is a one-parameter slice of a two-parameter space.

Searched over the whole rectangle, at the same 10 %-of-stroke criterion, the same footprint and the same `k_f`:

| model | square grid | best shape | attachments | per crossover |
|---|---|---|---|---|
| **lattice** | 64 | **3 × 15** | **45** | **0.80** |
| continuum plate | 64 | 4 × 10 | 40 | 0.71 |

**45 attachments against 56 crossovers**, at 2.22 pN each — and at fifteen rows the peak per-load-path force is **exactly zero**, because every duplex carries the identical load at the identical stations and no interface transmits anything. The statement "flatness needs more attachment points than the tile has crossovers" therefore **inverts** once the grid shape is allowed to vary, and the constraint it imposes on `T-2`'s design window loosens by **30 %**.

The mechanism is the one `C-0009` could not see from the diagonal: at **fifteen** attachment rows every duplex carries its own attachment, the crossovers stop being load paths at all, and the lattice becomes **1.2–6.2× flatter than the continuum plate** — the only region of the whole 225-point scan where the lattice beats the plate.

---

## What is *not* challenged

- `C-0009`'s split verdict on the continuum plate — upheld for smooth loads, rejected for point-coupled ones — stands untouched. Nothing here re-runs it.
- `C-0009`'s concentration factors (2.3–7.6× against an equal-sharing contour average) stand, and grow.
- The `k_θ` insensitivity of `C-0009`'s *ratios* stands: the layout **ranking** found here is invariant across Chen et al.'s whole admissible range and across `k_f ×[0.25, 4]`.
- `no discrete attachment scheme is flat` stands. It is the *count* that moves, not the conclusion that a finite count is needed.

## How this challenge could itself be wrong

- **The unit cell may not be the right variable at this tile size.** A 40 nm tile is only 3.7 unit cells wide along the helices, and translating an anchor by one full `p = 10.88 nm` moves it 27 % of the footprint. The measured lattice-periodicity residual is 0.1–1.9 % across the helices and **4.7–17.7 % along** them, so the along-helix half of "registration" is partly position-in-tile at this size. Ground 3's 14.65 pN is therefore an extremum over *placements on this tile*, which is what a designer chooses anyway, but it is not purely a cell property.
- **The 45-attachment result inherits every premise of `C-0009`'s lattice**, including the crossover's vertical compliance being a rigid constraint. A compliant crossover would add a load path and could move the count either way.
- **`RIGID_PLATE_TOLERANCE = 0.10`** is `T-5b`'s convention, not a physical threshold; both the 64 and the 45 are counts at that convention and both move together if it changes.
