# C-0015 — The staple layout is a 1.4–1.6× lever on the peak per-load-path force, and one attachment row per duplex removes the load path entirely

| | |
|---|---|
| **Task** | [`T-14`](../tasks/T-14-crossover-phase-and-registration.md) |
| **Leaf** | `A8.2` (structural rigidity / mode analysis — *"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | in-silico (`C-0009`'s beam-and-hinge grillage, re-parameterised by the staple layout's own free variables and swept completely over both) |
| **Verdict** | **PASS** on all six items of the acceptance predicate. The lever is real, it is **registration** rather than crossover count, and it is **not** big enough on its own to clear the unzip allowable at the soft end of the foundation sweep — it misses by 0.5 %. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-14-crossover-phase-and-registration.json`, produced by `structure.CrossoverRegistrationStudyKt`; model in `src/main/kotlin/structure/CrossoverLayout.kt` and the additions to `OrigamiGrillage.kt`; 21 gate-named tests in `src/test/kotlin/structure/CrossoverLayoutTest.kt` |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.142 pN·nm`; 40 × 40.35 nm tile, 15 duplexes; 100 pN total force (§3) |
| **Raises** | [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md) against [`C-0009`](C-0009-discrete-lattice-tile.md) |
| **Consumes** | [`C-0009`](C-0009-discrete-lattice-tile.md) (the lattice, every physical ingredient, the per-path allowables), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate it is compared against), [`C-0001`](C-0001-layer-stiffness.md) (three foundation stiffnesses, swept), [`C-0004`](C-0004-poroelastic-drainage.md) (the foundation is drained) |

---

## The design space, stated exactly

`C-0009` varied a **count** of crossover columns. A count is not what a staple layout chooses.

The column lattice has a fixed pitch — crossovers recur every 16 bp along a helix but **alternate between its two neighbours**, so one interface is linked every `p = 32 bp = 10.88 nm` and the columns, counting both parities, sit at `p/2 = 5.44 nm`. What a layout chooses is the **phase**, and the count follows: a 40 nm tile spans `40/5.44 = 7.35` column pitches, so some phases fit eight columns and some seven.

| | |
|---|---|
| distinct phases | **32** — a crossover can only sit at a base pair, so the phase is quantised and the sweep here is **complete**, not sampled |
| **period of the phase** | **`p = 32 bp`, not `p/2 = 16 bp`** — a half-period shift leaves every column *position* inside the footprint unchanged and hands every interface **the other parity's** columns, which is a different sheet. Asserted as an identity, not a tolerance |
| phases giving 8 columns / 56 crossovers | **10 of 32** |
| phases giving 7 columns / 49 crossovers | **22 of 32**, with `D_⊥` down by exactly `49/56 = 0.875` |
| registration cell | the crossovers form a **centred** rectangular lattice, primitive vectors `(p/2, d)` and `(p/2, −d)`; its primitive cell is `p × d = 29.27 nm²` and holds exactly **one** crossover. Swept at 32 base pairs × 9 stations = **288 points**, against `C-0009`'s four |

### The symmetry group is a property of the phase, not of the sheet

Under the point inversion a column maps `c → n−1−c` and an interface `b → N−2−b`, so the crossover parity `c + b` survives **exactly when `n + N` is odd**.

&nbsp;&nbsp;&nbsp;&nbsp;**10 of the 32 phases are centro-symmetric. The other 22 have no symmetry at all — neither mirror nor inversion.**

`C-0009`'s "the lattice is centro-symmetric and not mirror-symmetric" is true of its eight-column lattice and false of the seven-column lattice it used in its own convergence sweep. This raises [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md).

---

## 1. The lever, and what governs it

Complete sweep, design point (`C-0001` secant `k_f`, Chen et al. `α = 1`):

| load class | best layout | worst layout | **ratio** |
|---|---|---|---|
| discrete anchor, `k_a = k_f A` | **4.542 pN** | **7.148 pN** | **1.57** |
| discrete anchor, `k_a = 10 k_f A` | **5.143 pN** | **8.243 pN** | **1.60** |
| concentrated attachment, 100 pN at one point | **34.99 pN** | **50.13 pN** | **1.43** |

### The governing variable is the distance from the attachment to the nearest crossover

Not "on a duplex axis" or "between duplexes" — those were the buckets a four-point sample suggested. Over the whole cell the peak crossover force is a **monotone decreasing** function of one distance:

| distance to the nearest crossover [nm] | 0.0 | 0.5 | 1.0 | 1.5 | 2.0 | 2.5 | 3.0 | 3.5 |
|---|---|---|---|---|---|---|---|---|
| **anchor**: peak crossover force [pN] | **8.195** | 8.157 | 8.002 | 7.635 | 7.231 | 6.676 | 6.209 | **5.985** |
| anchor: peak duplex shear [pN] | 3.515 | 3.711 | 4.228 | 4.735 | 5.339 | 5.971 | 6.288 | 6.124 |
| **attachment**: peak crossover force [pN] | **49.94** | 48.73 | 46.53 | 44.62 | 42.39 | 40.15 | 37.88 | **38.17** |
| attachment: peak duplex shear [pN] | 24.58 | 25.50 | 28.40 | 31.95 | 36.12 | 41.24 | 43.90 | 44.57 |

&nbsp;&nbsp;&nbsp;&nbsp;**Put a tether on a crossover and the crossover takes the load; put it in the deep hole of the crossover lattice, 3.2 nm away, and the duplex takes it instead.**

The two optima are at opposite corners of the same 29 nm² cell, and the trade is decided by the allowables rather than by the mechanics: the crossover is judged against **10–15 pN unzip** and the duplex against the **65 pN nicked ceiling**, so the crossover is 4–6× the tighter constraint and the deep hole wins.

### Decomposition: registration is the lever, phase is a rounding error

| | anchor, `k_a = 10 k_f A` | concentrated attachment |
|---|---|---|
| **registration** at fixed phase | **×1.46 – 1.53** | **×1.42 – 1.43** |
| **phase** at fixed registration | ×1.05 – 1.10 | ×1.004 – 1.035 |
| joint | ×1.60 | ×1.43 |
| separable product | ×1.54 | ×1.43 |
| joint / separable | **104 %** | **100 %** |

**The two levers compose — they do not trade.** And of the phase's own ×1.05–1.10, essentially all of it is the **column count**: seven columns against eight is 4.5–9.0 % for an anchor and 0.3–3.4 % for a concentrated attachment, while the base-pair phase *within* a count is worth **under 0.5 %**.

**Seven columns is the better layout for both load classes at the best registration**, which is the opposite sign to what `C-0009`'s uncontrolled 19 % implies. See `CH-0014`, ground 1.

---

## 2. Is the lever big enough? No — it misses by 0.5 %

Against the per-path allowables, over the complete `k_f ×[0.25, 4]` sweep, Chen et al.'s `α ∈ [0.6, 1.2]`, four `(k_f, α)` corners, and `CH-0005`'s out-of-range isotropic probe:

| state | best layout [pN] | worst layout [pN] | verdict on the best |
|---|---|---|---|
| `k_f` × 0.25, `α` = 1.2 | **10.049** | **14.647** | **INSIDE the 10–15 pN unzip band** |
| `k_f` × 0.25, `α` = 1.0 | 9.760 | 14.216 | below every allowable |
| `k_f` × 0.25, `α` = 0.6 | 8.839 | 13.017 | below every allowable |
| `k_f` × 0.5, `α` = 1.0 | 7.273 | 11.013 | below every allowable |
| isotropic probe, `α` = 25.56 | 7.628 | 11.631 | below every allowable |
| **design point** | **5.143** | **8.243** | below every allowable |
| `k_f` × 4, `α` = 1.2 | 2.304 | 4.361 | below every allowable |

&nbsp;&nbsp;&nbsp;&nbsp;**At the design point every layout is clear of the unzip band. Only at the soft end of the foundation sweep is the band entered at all, and there the best layout brings 14.65 pN down to 10.05 pN — to 0.5 % above the lower edge, not clear of it.**

So the answer to *"can registration alone keep the worst case clear of unzip?"* is **no, by half a per cent**, and the correct reading is that **layout converts a mid-band exceedance into an edge case and no more**. It removes 4.60 pN of a 4.65 pN excursion.

For a **concentrated** output coupling the same lever crosses a different allowable:

| | best layout anywhere | worst layout anywhere |
|---|---|---|
| peak crossover force, one attachment | **37.59 pN** — below the 48 pN duplex-shear allowable | **50.15 pN** — **above** it |

**Layout alone decides whether a single-attachment coupling sits above or below the quasi-static single-duplex shear allowable.** `C-0009` reported 37.14 pN for this case and read the shear exceedance off the *duplex's* transverse shear; at the worst registration the **crossover** force reaches it too.

### Is any of this a property of the layer?

- **The ranking is not.** The best layout has **7 crossover columns and the worst 8 in all thirteen states** of `k_f` and `k_θ` — the coarse ranking is pure lattice geometry, exactly as `C-0009` argued the attachment *count* is, and it therefore survives `T-1c` re-deriving `k_f` and `T-9` measuring `k_θ`.
- **The lever's *size* is not either, within 30 %**: the joint ratio is 1.46–2.03 across every state, and it **grows** as the foundation stiffens.
- **The forces are.** They scale roughly as `k_f^(−1/2)` (2.22 → 9.76 pN for the best layout over `k_f` ×4 → ×0.25) and rise with `k_θ` (4.575 → 5.319 pN over `α` 0.6 → 1.2, and 7.628 pN at the isotropic probe). Every force here is therefore quoted per state and none is quoted as *the* number.
- The base-pair phase *within* a column count is the only thing whose argmin moves with the layer, and it is worth under 0.5 %, so it is not a design decision.

---

## 3. The non-monotone flatness curve, explained

`C-0009` reported the lattice's flatness-versus-attachment-count curve as non-monotone — worse at 121 than 100, worse at 196 than 169, and barely better at 49 than 36 — and could say only that "where the attachments land relative to the crossovers matters".

### The control passes first

**The continuum plate's curve is monotone at every one of the fourteen square grids.** The plate has no duplexes and no unit cell, so every non-monotonicity in the lattice curve is a lattice property and nothing about the attachment grid on its own. Wired in as a test.

### The excess is a function of the attachment *rows* alone

Over the whole `(columns × rows)` rectangle, 225 grids on both models, the lattice/plate excess at 15 columns:

| rows | 3 | 4 | 5 | 6 | **7** | 8 | 9 | 10 | **11** | 12 | 13 | **14** | **15** |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| row offset spread | 0 | 0.50 | 0 | 0 | **0.86** | 0.75 | 0.67 | 0 | **0.91** | 0.50 | 0.92 | **0.86** | **0** |
| lattice / plate | 1.05 | 1.17 | 1.18 | 1.45 | **1.98** | 1.32 | 1.22 | 1.28 | **1.74** | 1.16 | 1.33 | **1.79** | **0.161** |

The three worst row counts are **7, 11 and 14** — and those are exactly the three that produce `C-0009`'s three anomalies:

| transition | plate improves by | lattice/plate rises by | net on the lattice |
|---|---|---|---|
| 36 → **49** (rows 6 → 7) | ×0.72 | ×1.35 | **×0.97 — barely improves** |
| 100 → **121** (rows 10 → 11) | ×0.82 | ×1.34 | **×1.11 — gets worse** |
| 169 → **196** (rows 13 → 14) | ×0.86 | ×1.35 | **×1.16 — gets worse** |

&nbsp;&nbsp;&nbsp;&nbsp;**The non-monotonicity is entirely the jump in the lattice/plate excess at rows 7, 11 and 14, and the excess is set by how the rows register against the duplex axes.**

The offset arithmetic is pure: with `L_y = N d`, row `j` of an `s`-row grid sits at `N(j + ½)/s − ½` duplexes from the first axis, so the offsets are a function of `s` and `N` only — **no elasticity, no foundation stiffness, no solve**. That is why this explanation does not move when `T-1c` re-derives `k_f` or `T-9` measures `k_θ`.

**The metric is not a complete ordering, and this is stated rather than hidden.** The offset spread puts 7, 11 and 14 at the top, which orders all three of `C-0009`'s anomalies, but it misorders rows 6 (spread 0, excess 1.45) against rows 13 (spread 0.92, excess 1.33). No single scalar of the offsets was found that orders all thirteen row counts.

### The rule, and the mechanism, exactly

| rows | peak crossover force under the grid |
|---|---|
| 3, 4, 5, … 14 | 1.06 – 12.17 pN |
| **15 — one row per duplex** | **exactly 0** |

With one attachment row per duplex every beam carries the identical load at the identical stations, so no interface transmits anything: **the crossovers stop being load paths at all**. It is the same symmetry argument that makes a uniform load on a uniform Winkler foundation produce no dishing, and it is exact to the `1e−9 pN` reporting floor at every column count from 1 to 15.

That is also the only region of the whole 225-point scan where the **lattice is flatter than the continuum plate** — by 1.02× at one column and **6.2×** at fifteen.

&nbsp;&nbsp;&nbsp;&nbsp;**Design rule: give every duplex its own attachment row. The per-load-path force then vanishes identically and the flatness problem reduces to along-helix beam bending, which is the sheet's stiff direction (`D_∥/D_⊥ = 25.6`).**

### What flatness actually costs, searched over shapes

| model | square grid (`C-0009`'s search) | best shape | attachments | per crossover |
|---|---|---|---|---|
| **lattice** | 64 | **3 × 15** | **45** | **0.80** |
| continuum plate | 64 | 4 × 10 | 40 | 0.71 |

&nbsp;&nbsp;&nbsp;&nbsp;**45 attachments against 56 crossovers, at 2.22 pN each and zero crossover force — 30 % fewer than the square-grid search found, and it inverts `C-0009`'s "flatness needs more attachment points than the tile has crossovers".** This raises `CH-0014`, ground 4, and it loosens the topological constraint `C-0006`/`C-0009` placed on `T-2`'s design window.

### A rule that did *not* survive its control

The obvious candidate rule — *"put attachments on the duplex axes, not on the crossover interfaces"* — **fails**. Shifting one and the same grid by `d/2` costs ×1.84–2.09 in peak dishing on the lattice, but ×1.70–2.10 on the **plate**, which has no duplexes: the excess is **0.87–1.21**, i.e. nothing. The shift also moves the grid toward a free edge, and that is what the ×2 is. Reported as a refuted rule rather than dropped.

---

## The five verification gates

Executed as tests: `src/test/kotlin/structure/CrossoverLayoutTest.kt`, 21 tests, each named for its gate. Full detail in [`T-14`](../tasks/T-14-crossover-phase-and-registration.md#the-five-gates).

- **Gate 1** — the centred layout reproduces `T-10`'s construction exactly; the nominal `T-10` lattice is the phase at half a column pitch; the base-pair period is exactly the per-interface spacing; a 40 nm tile holds 7 or 8 columns at every phase; **the rank-one anchor update equals a re-assembled anchored lattice to `1e−12`**, and the batched map equals one anchor at a time to the same; the offset spread vanishes on a commensurate row count and the distinct-offset count is `s/gcd(s, N)`.
- **Gate 2** — the phase has period `p` and not `p/2`, with the half-period shift shown to invert every parity; a phased lattice still recovers `D_⊥` as `½ k_θ (κd)²` per crossover exactly; a uniform load dishes no phased lattice; a rigid anchor takes the whole load; rows on the duplex axes beat an incommensurate row count of equal size by ×6.6 with the plate divided out, while the equal-registration control gives ×1.84.
- **Gate 3** — centro-symmetry holds at 8 columns (`< 1e−8`) and fails at 7 (`> 1e−3`); force balance under a rank-one anchor to `1e−8`; **the registration variable is lattice-periodic** — 0.33–1.89 % residual under `(0, 2d)` against a 60 % cell effect.
- **Gate 4** — the phase that puts a column 0.28 nm from the tile edge, and so makes the shortest beam element in the sweep, converges to **0.05 %** between subdivisions 2 and 4 (6.9867 → 6.9834 pN); the loosest phase to `1e−4`.
- **Gate 5** — `C-0009`'s four named anchor placements are reproduced to 0.01 pN (5.11 / 5.56 / 5.76 / 6.66) and its seven- and eight-column lever forces exactly (44.146 / 37.139 pN); **the continuum plate is run as the control** and its flatness curve is monotone where the lattice's is not. Crossover topology from Rothemund 2006 as `C-0009` traced it; per-path allowables from Essevaz-Roulet, Strunz and van Mameren, unchanged.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Not measured.** No crossover force in a loaded origami sheet has ever been measured.
- **NO STAPLE LAYOUT IS REPORTED AS A RESULT.** Whether a Gen-1 tile has seven or eight crossover columns and at what phase is a property of a design nobody in this programme has. What is reported is the **sensitivity** and the **rule**. A "best phase" here is a statement about the lattice, not a claim about the Gen-1 tile.
- **The crossover's vertical/axial compliance is a rigid constraint**, inherited from `C-0009` and still the single assumption under it with nothing cited behind it. A compliant crossover would add a load path, lower the peak and could move the 45-attachment count either way. `T-9` could settle it at the same cost as `k_θ`.
- **`k_θ` is a fitted model input** (Chen et al., in which only `α` was fitted and the `1/100` is CanDo's *nick* softening), not a measurement. Swept over the whole admissible range and at the out-of-range isotropic probe; the **ranking** is invariant across it, the **forces** are not and are quoted per state.
- **Registration is lattice-periodic only up to the finite-tile contamination, which is measured, not assumed.** Across the helices the residual is 0.33–1.89 %; along them, where one full `p = 10.88 nm` is 27 % of the footprint, it is **4.65–17.70 %**. A 40 nm tile is only **3.7 unit cells wide along the helices**, so at this size "registration" and "position in tile" are not cleanly separable in `x`. The extrema reported are extrema over placements *on this tile*, which is what a designer chooses.
- **An attachment is one point on one duplex.** A tether bonded to a crossover, or spanning two duplexes, would spread the load and is not represented — and the map shows the force varies by 1.4× across 3.2 nm, so that difference is not small.
- **Linear Winkler foundation**, drained per `C-0004`; `C-0001`'s stiffnesses are lower bounds per `CH-0001` and are swept ×[0.25, 4]. The corrections in flight under `T-1c` run toward *softer*, which raises every force here.
- **The zero-crossover-force result for one row per duplex is exact only for a uniform load and a uniform foundation**, exactly as `C-0006`'s zero dishing under a uniform load is. Any load non-uniformity, thermal excitation or attachment-stiffness scatter restores a finite crossover force in proportion to the non-uniformity.
- **The result file is byte-identical on re-run, with an intermittent residual of two fields in ~25 000.** Two pairs of independent runs came out identical; one triple differed in two fields, both a penalty-derived crossover force, both by one unit in the ninth significant digit (`1.4e−9` and `8.3e−9` relative): the `10⁴ pN/nm` link penalty multiplies a difference of two nearly equal nodal deflections, so the quantity's own floating-point noise floor is near `1e−11` relative, above what a nine-digit rounding convention assumes. Reported rather than hidden behind a coarser rounding. No conclusion here turns on a ninth significant digit.
- **`RIGID_PLATE_TOLERANCE = 0.10`** is `T-5b`'s convention, not a physical threshold. The 64, the 45 and the 40 all move together if it changes.
- **No electrostatics is solved**; the load model is `T-3`'s to supply, and `C-0009` showed the lattice response linear in the taper depth.
- **Single layer, static, 300 K.**

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

- `p = 32 bp` per interface, 16 bp per helix — **CITED**, Rothemund, *Nature* **440**:297 (2006). **This is what sets the phase period, and quoting the per-helix number would halve the design space.**
- `0.34 nm` rise per base pair — **CITED**, Douglas et al., *Nature* **459**:414 (2009). This is what quantises the phase into exactly 32 values.
- `d = 2.69 nm` — **CITED, MEASURED**, Fischer et al., *Nano Lett.* **16**:4282 (2016), SAXS.
- `EI = 230 pN·nm²`, `GJ = 460 pN·nm²` — **CITED**, CanDo (Kim et al., *NAR* **40**:2862, 2012); model inputs in that paper, not measurements.
- `k_θ = 2αB/(100a)`, `α ∈ [0.6, 1.2]` — **CITED, fitted**, Chen et al., *JACS* **136**:6995 (2014) SI. Swept.
- Per-path allowables: 10–15 pN unzip (Essevaz-Roulet et al., *PNAS* **94**:11935, 1997), 48 ± 2 pN shear (Strunz et al., *PNAS* **96**:11277, 1999), 65 pN nicked ceiling (van Mameren et al., *PNAS* **106**:18231, 2009) — **CITED, MEASURED**, all loading-rate dependent. The §4(f) 35–60 pN band is **not** used as a per-path allowable, per `C-0006`'s trace.
- The 40 nm footprint, 100 pN target, 10 nm layer height — §3.
- `k_f` — **DERIVED** from `C-0001`, itself under challenge (`CH-0001`), swept ×[0.25, 4].
- `C-0009`'s 44.146 / 37.139 pN seven- and eight-column lever forces and its 5.11 / 5.56 / 5.76 / 6.66 pN anchor placements — **CITED** from `gpd/results/T-10-discrete-lattice-tile.json` and reproduced here as gate-5 tests, so the challenge is *computed* against them rather than asserted.

## Challenges

**Raises [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md)** against four statements in `C-0009`. None stands against this claim.

The way this claim would fail is through the **crossover's vertical compliance**, which is a rigid constraint here as it is in `C-0009`: a crossover soft in `z` would add a load path that the registration map does not have, and it would flatten the distance-to-nearest-crossover curve that the whole lever rests on. Nothing accessible measures it, and `T-9` could produce it at the same cost as `k_θ`.
A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
