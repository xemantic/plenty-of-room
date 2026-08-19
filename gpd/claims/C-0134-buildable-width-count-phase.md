# C-0134 — **The buildable width does not move the interaction, and the guard everyone was told to sweep does nothing at all.** `C-0108`'s 32 × 6 count/phase grid, re-run at `C-0086`'s only drawable seamless raster width (112 bp = **38.08 nm**), leaves the two axes **just as inseparable**: the balanced two-way additive fit in `log p90` has a worst residual of **7.81225891 %** of a level against 40.00 nm's **7.72508879 %**, **1.87085751×** its own 34 → 30 count main effect against **1.53234726×**, and the interaction still carries more of the variation (**8.60479966 %**) than the phase main effect does (**5.64944784 %**). On the 2 × 2 the recommendation moves through — whose station sets are **bit-identical** at the two widths — the interaction is **−6.70707057 %** against **−5.74202426 %**, a ratio of **1.16806726** and the same sign. **What does move is the argmin, and it moves onto the phase the programme already builds at**: held at 34 paths the best of the 32 phases is **8** at 38.08 nm where it was **22** at 40.00 nm. And the axis the width was expected to open is **shut by arithmetic**: swept over 0.05 nm, half a rise and one rise, `CrossoverLayout.EDGE_MARGIN` leaves **one** lattice over all 32 phases of the buildable tile — worst station displacement **0.0 nm** — while at 40.00 nm the same sweep leaves **two**, moving the column count at phases 6, 10, 22, 26 and the upward inventory at 2, 14, 18, 30

| | |
|---|---|
| **Task** | [`T-188`](../tasks/T-188.md), raised by [`C-0108`](C-0108-count-phase-interaction.md) (`T-178`) *Still open* item 4 |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (a width × phase × convention lattice census, an inset-invariance **proof** and a station-identity proof — three cheap bounds, all closed form and all before any solve, discharged as 11 gate-named tests) **+ in-silico** (`C-0058`/`C-0063`/`C-0087`/`C-0089`/`C-0103`/`C-0108`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, one bank per phase per convention, `C-0087`'s seeded Bernoulli dropout unchanged, 10 000 realisations per cell) |
| **Verdict** | **PASS on all six predicates. `F1` FIRED; `F2`, `F3`, `F4` and `F5` did not.** **400 graded cells** — the search-free canonical nested chain at 22/25/28/30/34/45 paths at **all 32** crossover phases, on **both** end-of-row conventions, plus a 16-cell graded guard sweep. **The cheap bound settles the axis the task was expected to turn on, and it settles it the other way round.** At a width that is an exact whole number of column pitches the row-end column sits **on** the edge, so **any** positive inset deletes it and the next column in is a whole 16 bp = 5.44 nm further: the guard's **value** is therefore *exactly* inert at 38.08 nm (**1** distinct lattice over 32 phases, worst station displacement `0.0` nm, worst column-count change `0`) and **not** inert at 40.00 nm (**2** lattices; the column count moves at phases 6, 10, 22, 26 and the upward inventory at 2, 14, 18, 30). What went live at the buildable width is the guard's **existence** — the binary `C-0095` and `C-0099` already closed — and once the row end is admitted the inset stops being a truncation and becomes a **position**, worth **0.4442352 %** of a level over 16 graded cells. **The interaction is the same object at both widths.** Worst additive residual **0.075221185** log units (**7.81225891 %** of a level) against `C-0108`'s **0.0744123217** (7.72508879 %), refitted here from its own result file at run time; the variation splits **5.64944784 %** phase / **85.7457525 %** count / **8.60479966 %** interaction against 7.83610303 / 82.3717151 / 9.79218186, so `C-0108`'s signature — **the interaction is larger than the phase main effect** — holds at the buildable width and by a wider margin. Both decompositions close to **`0.0`**. On the 2 × 2 the total is **+3.90945627 %** against **+6.48887749 %** and the interaction **−6.70707057 %** against **−5.74202426 %**. **The count term barely notices the width**: adverse at **25 of 32** phases against 27, favourable at **25 – 31** — a contiguous band, `C-0108`'s own 25 – 29 extended by two — spanning **−3.96485487 to +12.075078 %** against −4.59519576 to +12.2058991 %, and at `C-0102`'s recommended phase **8** it is **+4.55812345 %** against **+4.47532136 %**. **`F1` fired in the programme's favour**: at 34 paths the argmin moves from phase 22 to phase **8**, and at 30 paths and over the whole grid it is phase 8 too — so at the buildable width the dropout-robustness optimum and `C-0090`/`C-0102`'s recommended phase are the same phase, which at 40.00 nm they were not. **The end-of-row CONVENTION moves the split three times further than the WIDTH does**: refusing the row-end column takes the 2 × 2 interaction to **−20.2843017 %**, **2.88226311×** its own count term, and there the **count** term changes sign between the two orderings (+8.18307433 % taken first, **−13.7611068 %** taken second). **Nothing is flat**: 192 of 192 admitted cells and 192 of 192 refused cells exceed `T-5b`'s 0.10 at **100 %** exceedance (one-sided Clopper-Pearson `p > 0.999700472`) and every one is worse than no coupling at all. Raises [`CH-0160`](../challenges/CH-0160-a-stratified-robustness-argument-is-quantified-over-a-widths-own-strata.md) and [`CH-0161`](../challenges/CH-0161-the-guard-that-went-live-is-the-existence-not-the-value.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The dropout **input** is `C-0087`'s, which is Strauss et al. (2018) read directly — *staple* incorporation on a plain Rothemund rectangle at one folding protocol — and the out-of-plane motif every placement stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-188-buildable-width-count-phase.json`, produced by `coupling.BuildableWidthCountPhaseStudyKt`; model in `src/main/kotlin/coupling/BuildableWidthLattice.kt` (**new file** — it **adds no method to any shared source** and composes `C-0090`'s `rasterColumnLayout`/`rasterUpwardSites`/`endOfRowColumnPhases`/`BUILDABLE_RASTER_WIDTH`, `C-0108`'s `canonicalRootChain`/`twoWayLogInteraction`/`countPhaseSplit`, `C-0103`'s `nestedRootChain`/`restrictEnsemble`/`rootStationIndices`/`rootStations`/`rowsAreCentroSymmetric`, `C-0089`'s `DropoutRobustPlacement`, `C-0087`'s `StapleDropout`, `C-0058`'s `NonUniformCoupling` and `C-0055`/`C-0063`'s influence bank as libraries); **5 cheap bounds, 3 guard-sweep records, a 64-row census, 400 graded dropout cells at 10 000 seeded realisations each, 64 count-term rows, 6 two-way additive fits, 3 path splits, 3 argmins, 3 convergence axes, 104 reproductions, 6 predicates, 5 falsifiers, 9 findings**; **11 gate-named tests in `src/test/kotlin/coupling/BuildableWidthLatticeTest.kt`**; the result file was **produced twice on the same snapshot** and diffed **byte for byte identical** — `0` differing lines over 545 kB |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes at the SAXS-measured 2.69 nm** — 40.35 nm across the helices, **unchanged at both widths**, because the scaffold does not raster along it (`C-0090`); along-helix width **38.08 nm** (112 bp, `C-0086`'s buildable seamless row) against **40.00 nm** (§3's nominal, `C-0108`'s); **all 32 crossover phases, each carrying its own host**; **both** end-of-row conventions, ADMITTED primary per `C-0095`/`C-0099`; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V**, its collar terms **carried unchanged** to the narrower tile as `C-0090` carries them; `C-0017`'s **33.3333333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**, shared **EQUALLY** at every cell; free-tile stroke **5.15473846 nm** at the buildable width against 4.90731102 nm at the nominal; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; `C-0087`'s **`MEASURED_DEPTH`** field evaluated on **this width's own tile**; **one** Bernoulli stream **per phase** at seed **20260817**, **10 000** realisations, restricted per subset; guard swept at **0.05 nm / 0.17 nm / 0.34 nm**; decisions at 6 significant digits, emission at 9, difference-of-nearly-equal fields at **2** |
| **Consumes** | [`C-0108`](C-0108-count-phase-interaction.md) (`canonicalRootChain`, `twoWayLogInteraction`, `countPhaseSplit`; its whole 32 × 6 canonical grid **read from `gpd/results/T-178-*.json` at run time** and refitted, its 0.0744123213, 0.0485610042, 0.0979218189, −5.74202435 % and +6.48887743 % reproduced), [`C-0090`](C-0090-buildable-raster-width.md) (`rasterColumnLayout`, `rasterUpwardSites`, `rasterJunctionPlanes`, `endOfRowColumnPhases`, `BUILDABLE_RASTER_WIDTH`; its 32-phase free-tile descent, its census and its **5.15473846 nm** free stroke **read from `gpd/results/T-153-*.json`** and reproduced), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the 112 bp row), [`C-0102`](C-0102-crossover-phase-selection.md) (the end-of-row congruence and the strata, **recomputed at this width**), [`C-0095`](C-0095-row-end-crossover.md)/[`C-0099`](C-0099-row-end-crossover-stiffness.md) (the end-of-row convention, closed in favour of ADMITTED), [`C-0103`](C-0103-path-count-at-fixed-geometry.md), [`C-0089`](C-0089-dropout-robust-placement.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0063`](C-0063-upward-root-placement.md), [`C-0055`](C-0055-unused-junction-site.md), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (`T-5b`'s 0.10) |
| **Raises** | [`CH-0160`](../challenges/CH-0160-a-stratified-robustness-argument-is-quantified-over-a-widths-own-strata.md) against `C-0108`'s per-stratum deliverable, and [`CH-0161`](../challenges/CH-0161-the-guard-that-went-live-is-the-existence-not-the-value.md) against `CLAUDE.md`'s standing `EDGE_MARGIN` entry |

---

## The claim, in one line

**A width that cannot be built and a width that can give the same interaction to within 1.1 %, and the numerical guard the corpus told the next agent to sweep is provably worth nothing at the width it was told to sweep it at — what is worth something there is whether the guard exists, which is a binary somebody else already closed.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa** exactly;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices and carries the width;
  `y` runs **across** them and does **not** —
  `C-0090`'s axis result, restated: the across-helix span is a **count of duplexes**,
  15 × 2.69 = 40.35 nm at both widths, because a boustrophedon's successive scaffold crossovers
  are the two ends of **one row** and that is an along-helix length.
- `z` is normal and positive **upward**, away from the grafted layer; `w` positive **downward**;
  the origin is the tile centre.
- **A row** is one duplex; **a plane** is `C-0055`'s 8 bp crossover plane;
  **a column** is the sheet's own 16 bp column lattice, of which the planes are every other member;
  **a station** is an upward (`EAST`) plane site at which an arm may be rooted.
- **Row-end REFUSED** is `CrossoverLayout.phased`'s strictly-interior truncation by
  `EDGE_MARGIN`; **row-end ADMITTED** keeps a plane or column lying *on* the row end, inset by
  that margin. `C-0095` (the permission) and `C-0099` (the mechanics) closed this in favour of
  **admitted**, which is this claim's primary reading; **refused** is carried as the bracket.
- **Dishing** is over that width's **own** free-tile stroke, so it is dimensionless and
  comparable across widths — `C-0090`'s cancellation: the level of the load moves it not at all.
- The **statistic** is the 90th percentile, nearest rank, under `C-0087`'s measured dropout.
- **`T-5b`'s 0.10 is a CONVENTION**, not a physical threshold.

---

## Deliverable 1 — the cheap bound, and it inverts the brief

`CLAUDE.md` carries a standing instruction to **sweep `CrossoverLayout.EDGE_MARGIN`**
at the buildable width rather than assume it is inert there.
Swept, it is inert there — **exactly** — and it is *not* inert at the nominal width.

| sweep, 0.05 nm / half a rise / one rise | distinct lattices over 32 phases | worst column-count change | worst station displacement | phases whose column count moves | phases whose stations move |
|---|---|---|---|---|---|
| **38.08 nm, row-end REFUSED** (`C-0108`'s truncation) | **1** | **0** | **0.0 nm** | none | none |
| **38.08 nm, row-end ADMITTED** (the carried convention) | 3 | **0** | 0.29 nm | none | 0, 16 |
| **40.00 nm, row-end REFUSED** (`C-0108`'s own width) | **2** | **1** | 10.88 nm | **6, 10, 22, 26** | **2, 14, 18, 30** |

**The reason is a lattice one and needs no solve.**
At a width that is an exact whole number of column pitches
the row-end column sits **on** the edge,
so *any* positive inset deletes it —
and the next column inboard is a whole 16 bp = **5.44 nm** further,
which no admissible guard reaches.
At 40.00 nm the closest approach a base-pair phase makes is **0.28 nm**,
which **one rise crosses**;
there the guard's own KDoc sentence — *"it never decides a column count that the physics does not
already decide"* — is a statement about the value **0.05 nm** and not about the guard.

The 10.88 nm in the last row is the row's station list **re-indexing** when its outermost station
leaves, not a station moving; the column-count and inventory columns are the physical statement.

**So at the buildable width the guard is not a tolerance at all — it is a BINARY**,
and `C-0100`'s *"the only two physical states of a constraint are present and absent"* applies
verbatim. Admitting the row end turns the inset from a **truncation** into a **position**
(the kept column sits at `±(L/2 − inset)`), which is a different quantity: graded at 16 cells
its whole spread is **0.4442352 %** of a level, beside `C-0090`'s 0.32 % on its own
searched placement.

Two further cheap bounds run before the sampler, and both are asserted as tests:

- **`endOfRowColumnPhases(112) = [8, 24]`** — `C-0102`'s congruence, re-derived.
  Admitting the row end, exactly **2 of 32** phases carry the richest column count (**8**) and
  they are 8 and 24; **2** carry the richest inventory (**60** stations) and they are **0 and 16**.
  Refusing it, those same two phases carry **6** columns and **no phase carries more than 7**.
  `C-0015`'s ten eight-column phases at 40.00 nm have become **two, or none**.
- **The 2 × 2's own corners carry identical stations at the two widths.**
  At phases 8 and 24 the buildable **admitted** station lattice is bit-identical to the 40.00 nm
  one, position by position — and the **columns** are not. That is what makes the headline
  comparison **matched on stations and unmatched on hosts**: every difference between the two
  widths' 2 × 2 is host geometry and load, and none of it is which stations were available.

---

## Deliverable 2 — the interaction is the same object at both widths

The balanced two-way additive fit in `log p90`, over 32 phases × 6 counts,
each phase on its own host. `C-0108`'s grid is **read from its own result file at run time and
refitted by the same function**, so its published numbers are a reproduction and not a citation.

| grid | worst residual, log units | as % of a level | ÷ its own 34 → 30 count main effect | phase share | count share | **interaction share** | decomposition closes to |
|---|---|---|---|---|---|---|---|
| **38.08 nm, ADMITTED** | **0.075221185** | **7.81225891 %** | **1.87085751** | 5.64944784 % | 85.7457525 % | **8.60479966 %** | `0.0` |
| 40.00 nm (`C-0108`'s, refitted) | 0.0744123217 | 7.72508879 % | 1.53234726 | 7.83610303 % | 82.3717151 % | 9.79218186 % | `0.0` |
| 38.08 nm, REFUSED (bracket) | 0.18041191 | **19.771061 %** | 6.5203418 | — | — | 11.318476 % | `0.0` |

**`C-0108`'s signature survives and widens.**
Its finding was that the interaction carries **more** of the variation than the phase main effect;
at the buildable width the gap goes from 9.79218186 against 7.83610303 to
**8.60479966 against 5.64944784**, i.e. from 1.24963× to **1.52312×**.
The worst residual is **1.87085751×** the fit's own count main effect, against 1.53234726×.

**The convention is worth three times what the width is worth.**
Refusing the row-end column — which at this width deletes two of eight columns at exactly the two
phases the design wants — takes the worst residual to **0.18041191** log units,
**6.5203418×** its own count main effect.

---

## Deliverable 3 — the 2 × 2 the recommendation moves through

`C-0063`'s 34 roots at phase **24** → `C-0074`'s 30 at phase **8**,
on the canonical search-free chain at each corner.
The station sets at both corners are identical at the two widths (Deliverable 1),
so this is a comparison of hosts and loads.

| | 38.08 nm ADMITTED | 40.00 nm (`C-0108`) | 38.08 nm REFUSED |
|---|---|---|---|
| `p90` at 34 paths, phase 24 | 0.682310008 | 0.722404177 | 0.763336831 |
| `p90` at 30 paths, phase 24 | 0.764699474 | 0.800711112 | 0.825801252 |
| `p90` at 34 paths, phase 8 | 0.678077031 | 0.736327095 | 1.00842843 |
| `p90` at 30 paths, phase 8 | 0.708984619 | 0.769280099 | 0.869657514 |
| **count first, then phase** | +12.075078 %, then −7.28584972 % | +10.8397677 %, then −3.92538739 % | +8.18307433 %, then +5.31075263 % |
| **phase first, then count** | −0.620389084 %, then +4.55812345 % | +1.92730309 %, then +4.47532139 % | **+32.1079221 %, then −13.7611068 %** |
| **total** (path disagreement `0.0`) | **+3.90945627 %** | **+6.48887749 %** | +13.9284098 % |
| **interaction** | **−6.70707057 %** | **−5.74202426 %** | **−20.2843017 %** |
| interaction ÷ count term | 0.609005218 | 0.574595423 | **2.88226311** |

**The width moves the interaction by a factor of 1.16806726 and does not change its sign**,
which is falsifier `F2` and it did not fire.
Under the **refused** convention the same 2 × 2 is a different animal:
the **count** term changes sign between the orderings (+8.18307433 % first, −13.7611068 % second)
and the interaction is 2.88226311× the term it splits.
`C-0108`'s finding is therefore *stronger* under the convention this claim brackets than under the
one it recommends.

---

## Deliverable 4 — the count term at every phase, and the argmin

| | 38.08 nm ADMITTED | 40.00 nm (`C-0108`) | 38.08 nm REFUSED |
|---|---|---|---|
| 34 → 30 adverse at | **25 of 32** | 27 of 32 | 23 of 32 |
| favourable at phases | **25 – 31** (7) | 25 – 29 (5) | 0, 8, 25 – 31 (9) |
| range | **−3.96485487 to +12.075078 %** | −4.59519576 to +12.2058991 % | −13.7611068 to +10.9200981 % |
| at `C-0102`'s phase 8 | **+4.55812345 %** | +4.47532136 % | −13.7611068 % |
| at `C-0063`'s phase 24 | +12.075078 % | +10.8397678 % | +8.18307433 % |
| monotone decreasing in count | 0 of 32 | 0 of 32 | 0 of 32 |

**The reversal band is in the same place and two phases wider.**
`C-0108` found the count term favourable at exactly 25 – 29 and could not explain it (`T-187`);
at the buildable width it is favourable at 25 – 31, contiguous, containing that set.
Whatever mechanism `T-187` is looking for, it is not an artefact of the unbuildable width.

**And the argmin moves — onto the phase the programme already builds at.**

| grid | argmin over the whole 32 × 6 | best phase at **34** paths | best phase at **30** paths |
|---|---|---|---|
| **38.08 nm ADMITTED** | phase **8**, 45 paths, 0.64199059 | **8** (0.678077031) | **8** (0.708984619) |
| 40.00 nm (`C-0108`) | phase 6, 45 paths, 0.682355843 | **22** (0.716638347) | 8 (0.769280099) |
| 38.08 nm REFUSED | phase 9, 45 paths, 0.675632858 | 9 (0.702222164) | 9 (0.728347764) |

That is falsifier **`F1`**, and it **FIRED** — in the programme's favour.
At the nominal width the dropout-robustness optimum at 34 paths (phase 22) and
`C-0090`/`C-0102`'s recommended phase (8) were **different phases**;
at the buildable width they are **the same phase**, at 34 paths, at 30 paths and over the grid.
`C-0090`'s *"the width SELECTS the design the programme already recommends"*
was a statement about a zero-defect searched placement; it now also holds
on a search-free family under the measured dropout.

---

## Deliverable 5 — nothing is flat, at either width or either convention

| | admitted | refused |
|---|---|---|
| cells exceeding `T-5b`'s 0.10 at `p90` | **192 of 192** | **192 of 192** |
| cells worse than no coupling at `p90` | **192 of 192** | **192 of 192** |
| exceedance | **1.0** at every cell | **1.0** at every cell |
| one-sided Clopper-Pearson limit (`T-213`) | `p > 0.999700472` | `p > 0.999700472` |
| `p90` range over the grid | 0.64199059 – 1.00182028 | — |
| uncoupled tile's own dishing over the 32 hosts | 0.299034733 – 0.305393677 | 0.299397543 – 0.307355642 |

The buildable tile is **flatter uncoupled** than the nominal one
(0.299034733 – 0.305393677 against `C-0108`'s 0.307902368 – 0.312235717),
and it is still true that neither the width, nor the count, nor the phase,
nor the end-of-row convention decides an acceptance verdict on this branch.
The only route by which this programme keeps a flat tile remains `C-0087`'s item 2 —
whether the coupling element's own incorporation is the staple's.

---

## Verification — the five gates

| gate | how | verdict |
|---|---|---|
| **1 — dimensional consistency** | every graded quantity is a dishing over that width's own free-tile stroke and therefore dimensionless; every term of a split is a log ratio; the guard sweep is in nm and its outputs are counts and nm | **PASS** |
| **2 — limiting cases** | with the row end refused, `upwardLatticeSignature` reproduces `CrossoverLayout.atBasePairPhase` and `upwardRootLattice` **to the last bit** at both widths and all 32 phases; at 40.00 nm no plane lands on the row end so the two conventions **coincide** at every phase; admitting the row end can only **add** stations, never move or remove one, and adds exactly 15 at exactly phases 0 and 16 | **PASS** |
| **3 — symmetry and conservation** | the two orderings of every 2 × 2 agree on their total at a path disagreement of **`0.0`**; every two-way decomposition closes to **`0.0`**, which is the orthogonality of a balanced design; a **uniform** load on the uniform foundation dishes `2.32e−07` of the stroke, worst over **64** hosts | **PASS** |
| **4 — numerical convergence** | realisations 1250/2500/5000/10 000 on the count **factor**: departure **9.9e−04**; dishing grid 41/81/161 on a 200-realisation **mean**: **2.2e−05**; nested beam subdivisions 1 ⊂ 2 ⊂ 4: **2.2e−04**; the guard sweep is exact rather than converged | **PASS** |
| **5 — literature cross-check** | nothing new is taken from the literature. The dropout field is `C-0087`'s reading of Strauss et al. (2018); the 112 bp row is `C-0086`'s reading of Rothemund's odd-half-turn rule; the interhelical 2.69 nm is Fischer et al.'s SAXS | **PASS (inherited)** |

**104 reproductions, worst strict departure `8.7e-08`** — `C-0090`'s free stroke, its 32-phase
free-tile dishings, its column and inventory census at every phase, `C-0108`'s fit and its 2 × 2,
and `C-0102`'s congruence. The result file was produced **twice** on the same snapshot and diffed **byte for byte identical**, `0` differing lines.

---

## What this does to the standing claims

| claim | effect |
|---|---|
| **`C-0108`** | **Its verdict transfers to the buildable width and one of its arguments does not.** The interaction, the inseparability, the *"interaction larger than the phase main effect"*, the reversal band and the count term at phase 8 all survive within 1.2× or better. What does not transfer is its **per-stratum** argument, because three of `C-0102`'s four strata **do not exist** at 38.08 nm — [`CH-0160`](../challenges/CH-0160-a-stratified-robustness-argument-is-quantified-over-a-widths-own-strata.md). Its *Still open* item 4 is **DISCHARGED**. |
| **`C-0090`** | **Nothing moves and its headline is strengthened on a second objective.** Its 5.15473846 nm free stroke, its 32 free-tile dishings and its whole census reproduce. Its *"the width selects the design the programme already recommends"* was read on a zero-defect searched placement; under the **measured dropout on a search-free family** the argmin at 34 paths also moves onto phase 8. |
| **`C-0102`** | **Its congruence reproduces and its stratification must be recomputed, not carried.** `endOfRowColumnPhases(112) = [8, 24]` is re-derived. But at 38.08 nm the richest-inventory set is **{0, 16}** and the richest-column set **{8, 24}**, disjoint and of size two — where at 40.00 nm each had ten members. A stratum is a property of a width. |
| **`C-0095` / `C-0099`** | **Nothing moves, and the value of what they closed is measured on a new axis.** The end-of-row convention is worth **−20.2843017 % against −6.70707057 %** in the 2 × 2 interaction and **6.5203418 against 1.87085751** in the grid's residual ratio — three times what the width is worth. Closing that binary was worth more than this whole task. |
| **`C-0103`** | **Nothing moves.** Its count effect is measured at a third state and its size at phase 8 is within 0.09 percentage points of `C-0108`'s. |
| **`C-0087` / `C-0089`** | **Nothing moves.** The dropout model, the ensemble, the percentile and the single-removal instrument are used unchanged, and `T-213`'s one-sided bound travels with the saturated exceedance. |
| **`C-0086`** | **Nothing moves.** Its 112 bp row is the width everything here is read at. |
| **`CLAUDE.md`'s `EDGE_MARGIN` entry** | **CHALLENGED** — [`CH-0161`](../challenges/CH-0161-the-guard-that-went-live-is-the-existence-not-the-value.md). Its two halves are each true of a different quantity, and the sweep it recommends cannot see the effect it is about. |

---

## Still open — named, not answered

1. **Whether the coupling element's own incorporation is the staple's.** `C-0087`'s item 2,
   unchanged, and still the only route by which this programme keeps a flat tile.
2. **Why the count term reverses at 25 – 31 and nowhere else.** `T-187` owns it. This claim
   narrows the question rather than answering it: the band is **not** an artefact of the
   unbuildable width, and it **grows by two phases** at 38.08 nm, so whatever sets its edges is a
   function of the width as well as of the phase.
3. **Whether a SEARCHED placement at the buildable width reorders the phases the way the
   search-free chain does.** `C-0090`'s own descent is at one count and one arm; `CH-0119` is the
   standing warning that a searched family measures the search.
4. **Whether the distribution freed at every count changes the argmin at this width.** `T-179`
   owns the axis and it has never been run at 38.08 nm.
5. **What fraction of built tiles a flatness verdict is owed over** — `C-0087`'s item 4,
   unchanged, and the parameter the whole branch is most sensitive to.

## Challenges

**Raises [`CH-0160`](../challenges/CH-0160-a-stratified-robustness-argument-is-quantified-over-a-widths-own-strata.md)**
against `C-0108`'s per-stratum deliverable
and **[`CH-0161`](../challenges/CH-0161-the-guard-that-went-live-is-the-existence-not-the-value.md)**
against `CLAUDE.md`'s standing `EDGE_MARGIN` entry.

**None stands against this claim.** The four ways it would fail:

1. **A per-site incorporation measurement on a coupling-bearing tile.** The whole grid transfers a
   plain rectangle's map, and the transfer is now made at **two** tile sizes, which is a second
   place `CLAUDE.md`'s *"a boundary-layer measurement does not transfer between two tile sizes"*
   bites.
2. **A second anchor rule over all 32 phases at this width.** The canonical chain is a third rule
   beside `C-0103`'s two, worth up to 6 % below 30 paths on its own evidence; a rule three times
   as influential could move the reversal band.
3. **A ruling that the row-end column cannot be drawn.** Then the primary reading here is the
   REFUSED one, where the interaction is −20.2843017 % and the argmin is phase 9 — and `C-0095`
   would have to be withdrawn first.
4. **A re-solve of `C-0022`'s collar at 38.08 nm.** Its terms are carried unchanged, which is
   `C-0090`'s inherited approximation and is a statement about the **level and shape** of the
   edge load on a 4.8 % narrower tile.
