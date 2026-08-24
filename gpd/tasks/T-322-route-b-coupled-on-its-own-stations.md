# T-322 — Route B's own buildable widths, graded COUPLED, on stations derived at each row length

**Leaf:** `A8.2`
**Raised by:** [`C-0207`](../claims/C-0207-the-uniform-raster-is-flat-with-its-tethers.md) §7,
which first named the residue,
and [`C-0211`](../claims/C-0211-the-uniform-raster-at-the-resolved-link.md) §10 and §11,
which named it again after re-grading the same three widths **uncoupled** at the resolved link.
**Reserved claim:** `C-0215`. **Reserved challenges:** `CH-0276`, `CH-0277`. **Reserved queue rows:** `T-326`, `T-327`.

---

## Formulate

### The standing state

The tile this programme now recommends is **route B's**:
a uniform honeycomb raster whose turns are flexible tethers of unpaired scaffold,
which is the only turn topology anybody has folded
([`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md), [`C-0200`](../claims/C-0200-the-file-draws-and-the-table-orders.md)).
Its buildable paired row lengths are fixed by the scaffold —
`scaffoldNucleotides / 60 − 28` — and are **`92 / 98 / 106 bp`** for M13mp18, p7560 and p8064.

`C-0211` graded that tile **free** at `C-0208`'s resolved per-bond link and found it flat:
`756 of 756` cells inside `T-5b`'s `0.10`, `0.048606444`–`0.0960647281` of the stroke,
with the recommended lattice constant `b₀ = 5 / 16 / 9` holding at every rung.

**Every coupled number in this corpus is read on a different tile.**
`C-0167`'s 64 cells, `C-0180`'s, `C-0205`'s, `C-0208`'s and `C-0212`'s 32
are all read at the `116 bp` **block extent** of the drawable `102 / 109` two-length raster —
route A's geometry, with 59 covalent **ties** rather than 59 tethers,
435 staple bonds rather than route B's `358 / 385 / 410`
([`CH-0270`](../challenges/CH-0270-the-uniform-raster-does-not-carry-435-bonds.md)),
and a station ladder that carries **six** stations per rooting helix where route B's rows carry **five**.

So the corpus holds a flat **free** tile and an unflat **coupled** one, and they are not the same object.

### The question, and it is not `C-0208`'s question

`C-0208` asked *can a coupling be made flat*.
On route B the free tile is **already** flat, and `C-0017`'s mandate is not optional:
the device needs `33.3333333 pN/nm` of output coupling whether or not the flatness wants it.
`CLAUDE.md` records that *an attachment coupling can be a NET DISHING SOURCE*, and
[`CH-0272`](../challenges/CH-0272-a-flat-count-is-not-an-admissible-design.md) records that
`0 of 32` searched coupled cells beat the **uncoupled** tile at the 90th percentile.

The question is therefore inverted, and it is:

1. Does attaching the **mandated** coupling to route B's own tile, at stations **derived at its own row length**, destroy the flatness `C-0211` measured — at the 90th percentile of [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md)'s measured staple dropout, which is the statistic every census here is graded on?
2. If some cell survives, is it **admissible** — inside `C-0023`'s per-path unzip allowable read over §3's *acceptable* 3 nm stroke — and does it beat the **uncoupled** tile? Reported as a **conjunction**, which `CH-0272` records the last study's verdict block did not do.
3. How much of `C-0208`'s and `C-0212`'s coupled census is a property of the **tile** they were read on rather than of the coupling? The paired comparison at the same `(placement, columns, fraction)` is what turns *"the stations belong to a different tile"* from an objection into a number.

### What is fixed, and what must be DERIVED rather than inherited

**Fixed**, and inherited unchanged:
the `10 × 6` cross-section; the composite fraction `f = 0.30` at the headline
([`C-0116`](../claims/C-0116-composite-fraction-threshold.md)'s calibrated reading);
[`C-0022`](../claims/C-0022-tile-edge-load-profile.md)'s solved collar at the 10 nm / 2 mM / `0.192 V` design state;
[`C-0017`](../claims/C-0017-output-coupling-stiffness.md)'s mandate as an **equality on the sum** at §3's *acceptable* clause;
the transverse link pinned at `C-0205`'s ceiling `254.80809548301096 pN/nm` and the headline radial rung at
`C-0208`'s bracket **floor** `754.005141 pN/nm` (through-thickness `629.20588`), which is `T-316`'s own headline rung so the two censuses are paired;
grading seed `197197` at **4 000** realisations and training seed `316316` at **120**, disjoint in seed, both `T-316`'s **as run**;
the `81 × 81` dishing grid, `subdivisions = 1`, and `T-5b = 0.10`.

**Derived at each row length, and asserted rather than assumed** — this is the half of the task that is not a re-run:

| what | why it cannot be inherited |
|---|---|
| `edgeX = L × 0.34 nm` = `31.28 / 33.32 / 36.04` | the block extent is `39.44` and a coupling's pitch is measured against the tile it stands on |
| the interior pressure `TARGET_FORCE / (edgeX · edgeY)`, and the collar built on it | `edgeY = 38.04 nm` is unchanged and `edgeX` is not, so the pressure is **higher** on every route-B width |
| the **bond census** `358 / 385 / 410`, split `108\|250`, `135\|250`, `135\|275` | `CH-0270`: the crossover planes are every 7 bp, so a shorter row carries fewer of them **and the in-plane share moves** |
| the **station ladder**, `honeycombStationLattice(10, L, phase, stagger)` | `C-0141`'s ladder is `phase + 21k ≤ L`; at `L = 116` the face carries **6** stations per helix and at `92 / 98` it carries **5** |
| the **ladder phase** | `C-0141` records that the `±5 bp` rule fixes the phase only where the raster **closes**, and route B's uniform rows close at **no** phase — so the phase is a free design variable here and `T-316`'s inherited `16` is a *route-A* number |
| the **dropout probabilities**, `measuredDepthIncorporation(edgeX, edgeY)` | `CLAUDE.md`: *a boundary-layer measurement does not transfer between two tile sizes; only the profile transfers, and it transfers in nm* |
| the **tethers**, their spans and their preload | `UniformRasterTethers` at that width's own `b₀`, which `C-0211` recommends at `5 / 16 / 9` |
| the **free stroke** and the uncoupled reference | one per width and per rung, and the uncoupled reading must **reproduce** `C-0211`'s committed cell |

**Moves**: the per-path stiffness vector, subject to the mandate on the **sum**, exactly as in `T-316`.
That, and the tile it stands on, are the only design variables this task opens.

### What `HoneycombGrillage` does at a row length that is not a multiple of 7 — read, not assumed

`CLAUDE.md` records a lattice-class precondition of exactly this shape:
`nodeS` was once built from `(0..rowBasePairs step 7)` alone, so it reached the end of the tile only when
`rowBasePairs ≡ 0 (mod 7)`, and a uniform pressure on the `116 bp` block duly dished `0.15` of the stroke.

**Read at `HEAD`, the repair is present and its KDoc states it.**
`nodeS` lays the ladder stations, then adds a subdivided **trailing overhang** whenever
`lengthS/2 − stations.last() > 1e−9`. At the three route-B widths:

| row | crossover planes | trailing remainder | overhang nodes at `subdivisions = 1` |
|---|---|---|---|
| `92 bp` | 14 | `0.33999999999999986 nm` = one rise | 1 |
| `98 bp` | 15 | `0.0` (`98 = 14 × 7`) | **0** — the ladder alone spans the row |
| `106 bp` | 16 | `0.3399999999999963 nm` = one rise | 1 |
| `116 bp` (route A's) | 17 | `1.36 nm` = four rises | 1 |

So two of the three widths carry a one-rise free overhang and one carries none,
and `T-315` has already exercised all three: its `F1` read a worst uniform-load dishing of `8.2e−11` of the stroke.
The standing falsifier is re-declared here on the **coupled** study's own lattices rather than inherited (`F1`).

### Numeric targets

| # | target |
|---|---|
| `P1` | the **cheap bounds**, before any search and with no solve at all: (a) the station census per width and per ladder phase, which says which column counts route B's tile can carry; (b) the ratio transfer from `C-0212`'s own 32 cells applied to `C-0211`'s committed uncoupled readings, as a **prediction** with its own falsifier; (c) the oracle `p90` dishing floor at every cell, a pointwise lower bound over *every* distribution, with the statement of what it can and cannot decide |
| `P2` | the **transferred** reference census — `C-0058`'s equal springs and its rim-graded 5:1 — graded at every cell of the sweep: the census `C-0208` would have read had it been taken on route B's own tile |
| `P3` | a distribution **searched** at every cell, by `T-316`'s composition of `C-0135`'s smoothed minimax with `C-0089`'s true-percentile descent, graded **out of sample** on the grading ensemble, with the in-sample/out-of-sample gap emitted per cell |
| `P4` | the **uncoupled** route-B tile carried as the reference at every cell, `beatsUncoupledAtP90` emitted, and the uncoupled reading asserted against `C-0211`'s committed `freeTileWithPreload` |
| `P5` | **every threshold the moving quantity feeds**, per cell and as a **conjunction** (`CH-0272`): flatness against `T-5b`; the peak single-path stiffness against `C-0023`'s 10 pN unzip allowable over §3's acceptable 3 nm stroke, `3.33333333 pN/nm`, which is the one **physical** per-path threshold; and the `max/min` ratio against `C-0060`'s `[3.5, 20]`, emitted and named a **flatness** window and not a buildability one (`CH-0273`) |
| `P6` | **fragility and rung-independence**: the worst single-path removal at every flat cell, and the deciding cell re-graded at all of `C-0208`'s radial rungs and at the `1e4` penalty, so the answer is a property of the question and not of one rung |
| `P7` | the **paired** comparison against `C-0212` at the same `(placement, columns, fraction)` — route B's own tile against the `116 bp` block, read per cell — so *"the stations belong to a different tile"* becomes a measured number |

### Acceptance predicate

The task passes when `P1`–`P7` are discharged and the claim states plainly,
for route B's own buildable widths and at stations derived at each of them:

- how many cells are flat at the 90th percentile on a **transferred** rule and on a **searched** one;
- whether any flat cell is also inside the **physical** per-path allowable, stated as a conjunction;
- whether any coupled cell beats the **uncoupled** route-B tile, which `C-0211` has already shown is flat;
- and how much of the difference from `C-0212` is the **tile** rather than the coupling.

**A negative is as much a result as a positive.**
If no coupled cell on route B's own tile is both flat and admissible,
that is the statement that the mandated output coupling — not the raster, not the link and not the tether —
is what stands between this programme and a flat Gen-1 tile,
and it is read for the first time on the geometry the programme recommends.

### Units and conventions — locked before deriving

- nm, pN, pN/nm, pN·nm, pN·nm/rad, pN/nm² (= 1 MPa). `k_BT = 4.141947 pN·nm` at `T = 300 K`, aqueous 2 mM MgCl₂.
- `W` positive **downward**, toward the electrode (`C-0006`); a coupling's support force is upward and enters as its negative.
- `s` along the helices, `y` across them in the face plane (row pitch `3d/2`), `z` through the thickness (layer pitch `d√3/2`).
- Honeycomb `d = 2.536 nm` (SAXS); rise `0.34 nm/bp`; phosphate radius `0.908637858 nm` (`T-71`, measured); `k_θ = 13.5294118 pN·nm/rad` at `α = 1`.
- The link is resolved per bond, `k_link = k_radial·unitZ² + k_transverse·unitY²` (`C-0208`), with `k_transverse = 254.80809548301096` throughout and `k_radial` at `C-0208`'s five rungs, the **floor** `754.005141` being the headline.
- A **paired row length** is `scaffoldNucleotides / 60 − 28`, `C-0193`'s built allowance: `92 / 98 / 106 bp`.
- The **tether** is `C-0201`'s element unchanged — a linearisation about the built, taut state, one-sided, on `C-0194`'s frame-indifferent `d/2` arm — carried **per turn** at its own span, with the preload **on** for the free response and **off** for every influence (`C-0104`).
- The **lattice constant** `b₀` is fixed per width at `C-0211`'s recommendation, **read out of its committed `best` records** rather than transcribed. The **chain corner** is fixed at the **worst of the twelve** at that width's own `b₀` and the headline rung, identified the same way — so every cell is graded at the reading the recommendation is made on.
- The **station ladder phase** is derived per width as the **smallest** phase in `[0, 21)` maximising the minimum station count per rooting helix, ties to the earlier phase (`CLAUDE.md`: *decide coarser than the noise, earlier candidate wins ties*). The forced inter-row stagger is `14 bp` at the headline — `T-316`'s, so the two censuses are paired — and `7 bp` is carried at the deciding cell as a sensitivity, `C-0141` carrying both.
- `C-0017`'s mandate is `MANDATED_TOTAL_STIFFNESS = 100/3 pN/nm` on the **sum**; every distribution sums to it exactly, so the search is a redistribution of a fixed budget.
- **Dishing** is `|w − affine fit|` peaked over the `81 × 81` face grid and divided by the free-tile stroke of the **same** lattice at the **same** rung, exactly as `C-0167`, `C-0208` and `C-0212` read it.
- The **free stroke** is `solve(uniformPressure(interiorPressure)).meanDeflection`. `T-315` takes it on the **untied** lattice and `T-316` on the **tied** one; a uniform load translates a free tile rigidly, so the two must agree, and that is asserted (`F8`) rather than assumed — it is what licenses the reproduction of `C-0211`.
- A stiffness **ratio** is `max/min` over the per-path vector; a **peak** is `max`.

---

## Plan

### The cheap bounds run first, and one of them is pure lattice arithmetic

**Bound 1 — the station census. No solve, no ensemble, and it decides whether the cell grid exists.**
`honeycombStationsOnHelix(L, p) = (L − p)/21 + 1`, and a `columns`-column placement needs
`columns ≤ min_row(stations)`, which `honeycombSnappedGrid` **refuses** rather than silently returns.
Computed over all 21 phases at both admissible staggers:

| row | at `T-316`'s inherited phase `16`, stagger `14` | derived phase (stagger `14`) | derived phase (stagger `7`) | best `min_row(stations)` |
|---|---|---|---|---|
| `92 bp` | **4** | `7` | `0` | **5** |
| `98 bp` | **4** | `0` | `0` | **5** |
| `106 bp` | 5 | `0` | `0` | **5** |
| `116 bp` (route A's) | 5 | `7` | `0` | **6** |

Two readings fall straight out, and both are declared as falsifiers rather than assumed.
**At the inherited phase a 5-column placement is refused at `92` and `98 bp`** — the very column count that
carries `T-316`'s tightest and best cells — which is `F12`, and it is the concrete form of
*the station set must be derived, not inherited*.
And **route B's tile carries at most five station columns where the block extent carries six**, at every phase:
the shorter row is not merely a smaller tile, it is a tile with one fewer place to stand.
Phase `7` at stagger `14` serves all three widths at the full five, so one common phase exists;
whether the derivation rule returns it is asserted, not assumed.

**Bound 2 — the ratio transfer, which is a PREDICTION and not a theorem.**
`C-0212`'s own 32 cells carry `searchedP90 / uncoupledDishing` at the headline fraction over
**`1.4438156 – 2.7106587×`**, and its best-transferred reading over `2.2359 – 3.5094×`.
Applied to `C-0211`'s committed uncoupled readings at the recommended phase and the resolved-floor rung
(`0.0521565503 / 0.0576976711 / 0.0523876952` at `92 / 98 / 106 bp`):

| row | predicted on a **transferred** rule | predicted on a **searched** rule |
|---|---|---|
| `92 bp` | `0.1166 – 0.1830` | `0.0753 – 0.1414` |
| `98 bp` | `0.1290 – 0.2025` | `0.0833 – 0.1564` |
| `106 bp` | `0.1171 – 0.1839` | `0.0756 – 0.1420` |

So the bound **predicts a transferred census of `0 of 48`** — `C-0208`'s direction, on route B's own tile —
and it **straddles `T-5b` on the searched one**, tightest at the 5-column determined-lattice cells.
**It cannot decide, and saying so is the point**: it is a ratio transferred between two lattices
whose bond census, station ladder, tile width, interior pressure, dropout field and turn topology all differ,
which is precisely the class of transfer this task exists to test.
It is stated so that the run either lands inside it — in which case the transfer is measured to work —
or does not, in which case the transfer is measured to fail and `C-0212`'s numbers are the more clearly
a property of their own tile.

**Bound 3 — the oracle `p90` floor.**
`InfluenceSurrogate.reachableDishingFloorAt` optimises over attachment **force** vectors, which relaxes
optimising over **stiffness** vectors, so its `p90` is a pointwise lower bound on the `p90` of every
distribution whatever. `CLAUDE.md` states the limitation in advance and `C-0212` reproduced it:
**the floor can EXCLUDE and can never ADMIT**, and it excluded `0 of 32` there.
It is run because a cell it excludes needs no search, and because a slack floor measures how much room the search had.

**And the bank is free.** An `InfluenceSurrogate` is a property of the **structure**, and a distribution
enters the Woodbury system as a **diagonal**. One bank per `(width, placement, columns)` — `10 + 20 + 30 + 50 = 110`
point-load lattice solves per `(width, placement)` — serves every distribution ever tried at that cell.

### The sweep, and how it is scaled deliberately

**3 widths × 4 placements × 4 column counts × 1 composite fraction = 48 cells**, against `T-316`'s 32.

The four placements are `C-0167`'s, re-derived at each width:
the abstract `columns × 10` grid at that width's own `edgeX`; that grid on the **rooting helices**
(whose `y` alternate `±d/4` about the uniform `3d/2` ladder and are read off `lattice.faceBeams`);
the **determined** station lattice, `honeycombSnappedGrid` at that width's own row length and derived phase;
and that lattice on the rooting helices.
Column counts `{1, 2, 3, 5}` are `T-316`'s, so every cell is paired with one of its 32.

**The scaling is stated rather than silent.** `T-316`'s full run was 55 minutes at 32 cells,
4 000 grading and 120 training realisations. The percentile descent dominates and is a property of the
**surrogate** rather than of the lattice, so it does not get cheaper on a shorter row:
48 cells is `1.5×` and lands near **80–90 minutes**, plus the rung ladder, the convergence axes and the
fragility pass — call it **1.5–2 hours**, measured on a smoke pass before the full run as `CLAUDE.md` requires.
What is dropped to buy it, deliberately and named here:

- the second composite fraction `f = 0.26` is carried **at the deciding cell only**, not across the sweep;
- the `7 bp` stagger is carried **at the deciding cell only**;
- the 21 lattice phases and 12 chain corners of `T-315` are **not** re-swept: `b₀` and the chain corner are fixed per width at `C-0211`'s own recommendation and its own worst corner;
- `P7`'s `116 bp` comparands are **read out of `C-0212`'s committed result file**, not re-run — which is also what makes the comparison paired rather than re-derived.

If the smoke pass extrapolates past ~2.5 hours the column set is cut to `{1, 3, 5}` and that is recorded
in the claim as a departure, in the form `T-316` had to use for its training count.

### The search, unchanged, and why nothing new is invented

`searchedStiffnessDistribution` is used exactly as `T-316` uses it: `C-0135`'s smoothed minimax on the
zero-defect peak (a genuine maximum of smooth functions, where the cure applies), whose answer becomes one of
the starts of `C-0089`'s multi-start coordinate descent on the **true** training percentile
(an order statistic, where neither the smoothing nor its adjoint transfers), with every acceptance and
tie-break taken through `searchDecision` at six significant digits and the earlier candidate winning ties.
Seeding the percentile descent from its own comparands is what makes *"never worse in sample than the best
transferred rule"* a property of the composition rather than a hope about it (`F10`).

**No shared source is edited.** The one line of plumbing this task needs —
a route-B tethered lattice at a resolved rung — already exists as `UniformRasterTethers.latticeAtRung`
(`T-315`), and `honeycombTiedSurrogate`, `honeycombMultiStateSurrogate`, `honeycombSnappedGrid`,
`measuredDepthIncorporation`, `dropoutEnsemble` and `searchedStiffnessDistribution` are all lattice-generic
and take the route-B grillage unmodified. `tile/HoneycombGrillage.kt`, `tile/SearchedDistribution.kt` and
`coupling/RobustDistribution.kt` are **not** touched.
The new sources are `tile/RouteBCoupled.kt` and `tile/RouteBCoupledStudy.kt`, with
`tile/RouteBCoupledTest.kt` written first and watched fail,
one hand-added line of `structure/ResultInputs.kt`
(hand-added, never by running `tools/T-272-emit-result-inputs.py`, which reads the git index and would
delete the handles of files not yet staged), and a mutation harness at `tools/T-322-mutation-test.py`
declared in `tools/P-31-harness-census.py` and wired in the same commit, with the unmutated copy run first
and its failures subtracted ([`CH-0237`](../challenges/CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md)).

### What would falsify the approach

If the free stroke read on the tethered lattice and on the untied one disagree, the dishing here is not
`C-0211`'s dishing and no reproduction of it means anything (`F8`).
If the uncoupled reading does not reproduce `C-0211`'s committed `freeTileWithPreload` at the cells used,
the two studies are not on the same tile and nothing below is admissible (`F6`).
If the surrogate at full presence does not reproduce the **assembled** lattice solve with its own Woodbury
support forces applied as point loads, the whole sweep is on the wrong object (`F3`).
If the one-state `MultiStateSurrogate` and the `InfluenceSurrogate` disagree about the peak dishing of one
distribution, the smoothed search and the graded percentile are not searching the same object (`F5`).

### What this task does NOT do

It does not re-open the raster, the cross-section, the chain model, the span census, the link resolution or
the radial bracket. It does not re-sweep `b₀` or the chain corners (`T-315` did). It does not withdraw
`C-0208`'s `0 of 64` or `C-0212`'s `22 of 32`, both of which are exact readings on the `116 bp` block.
It does not price the **placement** search — which stations a coupling enters at is `C-0063`'s axis and
`C-0212`'s own open question — nor the two-layer body, nor anything about route A.

---

## Falsifiers, declared before the run

| # | fires if | expected |
|---|---|---|
| `F1` | a uniform pressure on the **free** route-B tethered lattice at the resolved link, preload off, dishes more than `1e−9` of the stroke at any of `92 / 98 / 106 bp` — taken per width, because two of the three carry a one-rise `nodeS` overhang and one carries none | must not fire — `CLAUDE.md`'s standing falsifier, on the **free** lattice; the coupled one does not dish zero and asserting it there asserts something false |
| `F2` | a route-B lattice at a null radial constant and the default link is not bit-identical to `UniformRasterTethers.lattice`'s, on `assembleLoad` over every degree of freedom and on the crossover **site set**, at all three widths | must not fire — this task edits no shared source |
| `F3` | the surrogate at full presence does not reproduce the **assembled** route-B solve with its own Woodbury support forces applied as point loads, at `< 1e−9` relative | must not fire |
| `F4` | two independent runs of the study do not produce a byte-identical result file | must not fire |
| `F5` | the one-state `MultiStateSurrogate` and the `InfluenceSurrogate` disagree about the peak dishing of one distribution by more than `1e−10` relative | must not fire |
| `F6` | the **uncoupled** route-B reading here does not reproduce `C-0211`'s committed `freeTileWithPreload` at every `(width, b₀, chain corner, rung)` this study grades at, to `1e−8` relative | must not fire |
| `F7` | the bond census is not `358 / 385 / 410` at `92 / 98 / 106 bp`, or its in-plane / through-thickness split is not `108\|250`, `135\|250`, `135\|275`, or `⟨unitZ²⟩` is not exactly `0` in plane and `0.75` through the thickness | must not fire — `CH-0270`, asserted at the row length the studies **build**, not at the one the design is named by |
| `F8` | the free stroke of the tethered lattice and of the untied one differ by more than `1e−9` relative at any width or rung | must not fire — a uniform load translates a free tile rigidly, so it is element-independent; this licenses `F6` |
| `F9` | a searched `p90` falls **below** the oracle `p90` floor at any cell | must not fire — it is a theorem |
| `F10` | the searched distribution's **in-sample** training objective is worse than the best of its own starts at any cell | must not fire — it is a property of the composition |
| `F11` | **OPEN — the headline.** A distribution searched at the resolved per-bond link puts at least one **route-B** coupled cell inside `T-5b`'s `0.10` at the 90th percentile of the grading ensemble | either answer is the result; the cheap bound predicts `0.0753–0.1564` and straddles the threshold |
| `F12` | **OPEN.** `T-316`'s inherited ladder phase `16` at the `14 bp` stagger cannot carry a 5-column placement at some route-B width — a station set that does not exist on the tile the census is read on | either answer is the result; the lattice arithmetic predicts it **FIRES** at `92` and `98 bp`, where `min_row(stations) = 4` |
| `F13` | **OPEN.** No cell of the sweep is both flat at the 90th percentile **and** inside `C-0023`'s per-path allowable `3.33333333 pN/nm` — the **conjunction** `CH-0272` records the last verdict block did not report | either answer is the result |
| `F14` | **OPEN.** No coupled cell beats the **uncoupled** route-B tile at the 90th percentile, which `C-0211` has already shown is flat at every one of its 756 cells | either answer is the result; `C-0212` read `0 of 32` on the `116 bp` block |
| `F15` | **OPEN.** A cell that clears at the 90th percentile loses the verdict when its worst **single** path is removed — `CLAUDE.md`'s *an optimised placement is a cancellation, and a cancellation has no tolerance to a missing term* | either answer is the result |
| `F16` | **OPEN.** The verdict at the deciding cell moves across `C-0208`'s radial rungs, or between the resolved link and the `1e4` penalty | either answer is the result; `C-0211` found the free tile moves `1.02460822×` over the same ladder |
| `F17` | **OPEN.** The searched distribution's **out-of-sample** `p90` is worse than the best transferred distribution's at any cell — an over-fit, which a percentile search on a 120-realisation training stream can produce | either answer is the result |
| `F18` | **OPEN.** The flat verdict at a route-B width differs from `C-0212`'s paired verdict at the same `(placement, columns, fraction)` on the `116 bp` block — i.e. the tile substitution moves the census | either answer is the result, and it is the whole point of the task |
| `F19` | **OPEN.** The three widths rank differently on the **coupled** `p90` than on `C-0211`'s **uncoupled** reading, where the ordering is `92 < 106 < 98` at the resolved floor (`0.0521565503 < 0.0523876952 < 0.0576976711`) — a free-tile answer failing to predict the coupled one | either answer is the result |
| `F20` | **OPEN.** The searched `p90` at some cell falls outside the cheap bound's own predicted band (`0.0753–0.1414` at `92 bp`, `0.0833–0.1564` at `98`, `0.0756–0.1420` at `106`) — the ratio transfer between two lattices failing as a prediction | either answer is the result; a miss measures how much of `C-0212` is a property of its own tile |

> **This table is committed one commit BEFORE the result**, on its own, with no study source and no run —
> which is `C-0092`'s discipline in **form** and not only in spirit.
> `T-316` could not do that: its `F14` row landed in the same commit as its own result, and it had to say so
> in three places. Nothing is added to this table after the study source is written; if the smoke pass reveals
> a threshold the moving quantity feeds that is not here, the row is added in a **further** commit before the
> full run, and the fact is recorded rather than glossed.
