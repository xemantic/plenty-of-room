# T-291 — The allowed departure is common-mode: settling `CH-0240`, and pricing the per-beam twist that replaces it

**Leaf** `A8.2`.
**Verification type** **logical** (two lines of algebra on the challenged claims' own azimuth
convention, plus a reading of the model file every one of them consumes — settled with **no solver
at all**)
**+ in-silico** (the replacement eigenstrain assembled on the same three-dimensional beam-and-bond
lattice and graded through the same exact Woodbury coupling surrogate, the same `C-0087`-measured
incorporation as a Bernoulli dropout over 4 000 realisations on one common stream restricted per
cell, and the same `T-5b` convention that `C-0180` and `C-0187` used — so the answer is comparable
cell for cell).

---

## 1. Formulate

### The question in one sentence

[`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md) disputes a **coordinate**
without disputing a single number.
Every allowed honeycomb scaffold crossover sits `8.57142857°` off the line of centres
([`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) §5);
[`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md),
[`C-0175`](../claims/C-0175-drawable-raster-rim.md) §8 and
[`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) §4 all load that departure through the
model's **relative roll** `Φ_upper − Φ_lower`.
The challenge says a level displacement rotates **both** backbones the same way, so the relative
azimuth is level-independent and the departure has coefficient **exactly zero** on that coordinate.

Two questions follow, and only the second needs a computer:

1. **Does `CH-0240` stand?** — on the lattice and on the model's own source, not by assertion.
2. **What replaces it, and what is it worth?** — the alternation
   [`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) derives puts opposite roll
   demands at a helix's two ends, i.e. a demanded **twist** over each interior helix's own row.
   That is a **per-beam torsional eigenstrain**, and its sign relative to the applied term is the
   open half.

### Numeric target

- **The channel**: whether the tie prestrain load's projection onto the demanded kinematics is
  exactly zero, tie by tie, at all 59 raster turns of the recommended `10 × 6` / `102 · 109`
  block. Target: **`0.0`**, absolutely — a projection of two vectors both of which the model
  builds, so it is an identity and not a tolerance.
- **The replacement**: the per-beam twist demand in degrees, and its census over the 60 beams.
  Predicted `2 × 8.57142857° = 17.1428571°` on every **interior** beam and `0` on the two raster
  termini.
- **The price**: the rigid-duplex ceiling `½ (GJ/L) θ₀²` per beam, in `k_BT`, read against
  `C-0079`'s measured **7.99969697 `k_BT`** per crossover column of the host sheet; and the peak
  dishing the eigenstrain adds, as a fraction of the free stroke, at `T-5b`'s **0.10**.
- **The grade**: the flat census over `C-0167`'s 64 coupled cells at **both** signs of the
  eigenstrain, against `C-0180`'s zero-prestrain reading of the same cells and `C-0187`'s derived
  relative-roll reading of the same cells.

### Locked units, geometry and sign conventions — fixed before deriving

- SI: lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT` (`k_BT` = 4.142 pN·nm at 300 K),
  stiffnesses pN/nm and pN·nm/rad, angles **degrees at every API and radians only inside a solve**.
- `s` runs **along** the helices, `y` **across** them in the plane of the face (pitch `3d/2`),
  `z` along the block's **thickness** (pitch `d√3/2`); `W` is positive **downward**, toward the
  electrode (`C-0006`). Origin at the face centre.
- `Φ` is the roll about a beam's **own** axis. Its sense is right-handed about `+s` in the
  `(s, y, W)` frame, which is not an assertion but the reading of `HoneycombGrillage`'s own link
  gradient `[1, armY, −1, armY]`: a bond point at in-plane offset `Δy` from the axis moves in `W`
  by `+Δy·Φ`. **This is restated as a test rather than left in prose.**
- **Backbone azimuth** increases counter-clockwise viewed from `+z`, `240/7°` per base pair,
  which is `HoneycombRasterTurnSense`'s and `ForcedCrossoverPrice`'s shared convention.
- **A turn's departure `+δ`** means both backbones sit `+δ` from the line of centres, in the same
  rotational sense. The **demanded relief roll** is therefore `−δ` on **both** duplexes of the
  turn — an absolute roll of each duplex about its own axis, independent of which neighbour that
  turn bonds to.
- **The per-beam twist eigenstrain** is `θ₀(b) = φ_d(s = +L/2) − φ_d(s = −L/2)`, positive when the
  demanded roll increases with `s`. A beam with a demand at only one end carries `θ₀ = 0`, because
  a single-ended roll demand is a rigid roll of that duplex and costs nothing.
- **The global phase** `p ∈ {+1, −1}` is the map from the derived azimuthal sense onto the model's
  `Φ` sense. It is graded at both values and no direction is claimed.

### Falsifiable predicates

| # | fires if |
|---|---|
| `F1` | the two backbones of a honeycomb scaffold crossover are **not** at antipodal azimuths — i.e. the relative azimuth is not exactly `180°` at every level displacement, which would make part of the departure differential and `CH-0240` §2 wrong |
| `F2` | the tie prestrain load has a **nonzero** projection on the common-mode roll direction at any of the 59 ties, which would make `CH-0240` §3 wrong |
| `F3` | a pure **relative** roll reaches the same phosphate span as the common-mode relief does — which would make the two channels interchangeable and the challenge an overstatement |
| `F4` | the derived per-beam twist demand is not `17.1428571°` in magnitude, or not the **same sign on every interior beam**, or not invariant over all eight readings of `firstAxialSign`, `mirrored` and `axialReversed` |
| `F5` | the twist eigenstrain's response is **not exactly linear** in the eigenstrain — i.e. it is not a load, and one solve does not fix the axis |
| `F6` | a **uniform** twist eigenstrain over every beam does not relax into the twisted ribbon `W = y θ₀ s / L` as the foundation and the link vanish. It is the limiting case, and it is also what fixes the sign of the response against the sign of `θ₀` |
| `F7` | the standing falsifier: a **uniform pressure** on the tied, zero-eigenstrain lattice dishes anything but exactly zero. Its eigenstrain sibling is asserted the other way — `CLAUDE.md` records that a uniform eigenstrain does **not** inherit it, and here the relaxed state is a **saddle** rather than a rigid mode, which is asserted rather than assumed |
| `F8` | the twist term's peak dishing exceeds the triangle-inequality ceiling built on its own 60 unit responses |
| `F9` | **declared OPEN** — the coupled flat census over the 64 cells is the same at both signs of the eigenstrain |
| `F10` | **declared OPEN** — the twist term and the relative-roll term the corpus applies move a cell's `p90` by the same amount, i.e. the channel substitution is immaterial |
| `F11` | a convergence step — beam subdivisions 1 → 2, dishing sample grid 81 → 161 — moves a verdict at any deciding cell |

---

## 2. Plan

### The cheap bound runs first, and it settles deliverable 1 with no solve at all

`CH-0240` §2 is one line of algebra on a convention the challenged claims already use, and §3 is a
reading of a function in the tree. Both are executable in microseconds:

- **§2** — `forcedCrossoverSpan` *is* `turnPhosphateSpan(d, r_P, θ, 180° + θ)`, so the relative
  azimuth is `180°` at every `θ` by construction (`F1`). The discriminating half is `F3`: sweep the
  relative roll and show that **no** relative roll reaches the minimum span `d − 2r_P`, which the
  common-mode relief reaches exactly.
- **§3** — assemble the load vector with and without the derived prestrain and project the
  difference onto the common-mode direction at each tie (`F2`).

If either fires, `CH-0240` does not stand and nothing else in this task is owed.

### The replacement's price is a bound before it is a solve

The demand is `θ₀ = 2 × 8.57142857° = 17.1428571°` over a beam of length `L`, resisted by the
duplex's own `GJ = 460 pN·nm²`. The **fully restrained** energy `½ (GJ/L) θ₀²` is a rigorous upper
bound on the relaxed one, because the relaxed state minimises — so it prices the channel before any
lattice is assembled, exactly as `C-0152` §5 priced the roll channel with `½ k_θ θ²`. Read at the
two physical row lengths (102 and 109 bp) and at the model's beam (116 bp), and quoted against
`C-0079`'s host-sheet column energy.

### The model term, and why one method is added rather than one parameter

`HoneycombGrillage` already carries the element the eigenstrain lives on: a per-element torsion
spring `GJ/L_e` on `(Φ_{e+1} − Φ_e)`. What it lacks is a way to **load** it. For a uniform twist
rate the element eigenstrains are `θ₀ L_e / L`, so `(GJ/L_e)·θ₀ L_e/L = GJ θ₀ / L` is the same at
every element and the interior contributions **telescope away**: the load is one couple pair on the
beam's two **end** `Φ` nodes, `∓GJ θ₀ / L`, at any node spacing and any `subdivisions`.

So the change to the shared class is **one new public method** in the shape of the two that are
already there (`unitPrestrainResponse`, `unitTurnResponse`) and **no edit to any existing member**
— no new constructor parameter, no changed default, no touched line of the assembly. That is the
cheapest change that can be made and still be exercised, and the inertness argument is a diff
rather than a sentence. Its consumers are re-run anyway, because `CLAUDE.md` says a proof is not a
substitute for a run.

### The grade

`C-0187`'s `T284Column` structure is reproduced rather than re-invented: one stiffness matrix per
composite fraction, the influence bank taken on `withoutPrestrain` (`C-0104`'s rule), the free
field taken on the lattice as built, and the eigenstrain entering as a **load** so that both signs
share one factorisation. 64 cells × {no eigenstrain, `+θ₀`, `−θ₀`}, plus the relative-roll readings
`C-0187` published, reproduced as a cross-check.

### What would falsify this approach

- **`F1`, `F2` or `F3` firing** — then the challenge does not stand and the whole replacement is
  unmotivated.
- **`F5` firing** — then the eigenstrain is not a load, the Woodbury bank is not a compliance, and
  every number here is taken on a contaminated influence function (`C-0104`'s trap, verbatim).
- **`F6` firing** — then the couple pair is in the wrong place or has the wrong sign, and the
  response's own sign is unmoored.
- **`F8` firing** — then the ceiling is not a ceiling and the triangle inequality has been applied
  to something that is not a seminorm.
- A **verdict** moving under `F11` — then the answer is a discretisation and not a design.

### What this task deliberately does not do

It does not re-open the placement search, the distribution rule, the raster, the cross-section or
the departure's magnitude. It does not supply the tie's missing **common-mode** stiffness — that is
a change to the element set and it is filed as a challenge rather than taken here.
