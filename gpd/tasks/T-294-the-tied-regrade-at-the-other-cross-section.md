# T-294 — the `15 × 4` block graded coupled on the TIED lattice, so the two cross-sections compare like for like

**Leaf** `A8.2`.
**Verification type** in-silico
(the same three-dimensional beam-and-bond lattice, the same exact Woodbury coupling surrogate and
the same `C-0087`-measured incorporation as a Bernoulli dropout over 4 000 realisations on one
common stream restricted per cell — evaluated at the **other** 60-helix cross-section)
**+ logical**
(a cheap bound that is entirely integer arithmetic and two committed result files, and a derived
lattice census asserted against the bond graph rather than transferred).

---

## 1. Formulate

### The gap, and it is a completeness gap rather than a live risk

[`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) re-graded
[`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md)'s 64 coupled cells on the
**tied** honeycomb lattice — `435` staple bonds **plus the raster's own 59 turn ties** — and it
re-graded `10 × 6` **only**.
[`C-0186`](../claims/C-0186-carrying-the-tied-regrade.md) §1 records the consequence exactly:
of the thirteen passages carrying an uncoupled honeycomb reading, **one was left un-annotated**,
and it is the `15 × 4` comparison cell of `DECISIONS-FOR-NDI.md`'s decision 7 —
*"giving the `10 × 6` side a tied number the `15 × 4` side cannot be given would make the row read
as a measured ordering across two lattice states when only one side moved."*
[`C-0191`](../claims/C-0191-thirteenth-answers-synthesis.md) (`T-276`) has since **stated that
leave in the document**, which is the repair available without a run.

This task closes it with a measurement instead of a caveat.

**Nothing turns on the answer today** and the task file says so before it starts: the ordering
already stands on the **uncoupled** tiles, where
[`C-0154`](../claims/C-0154-honeycomb-grillage.md) reads `15 × 4` outside `T-5b` at every
enhancement it takes (`0.312237799` / `0.227177955` / `0.220064299`) against `10 × 6`'s
`0.127358454` / `0.0449400126` / `0.0477844467`.
What a run buys is a **like-for-like ordering in one state**, and three things a caveat cannot
give: the tie set's own worth at a **different** in-plane / through-thickness split, the ordering
read in the **absolute** convention as well as the normalised one, and a `15 × 4` reading at the
**resolved per-bond link** the document now quotes `10 × 6` in.

### What is open, and what is NOT open

**Open.**
Whether any of the 64 `(f, placement, columns, distribution)` cells of a `15 × 4` block clears
`T-5b`'s `0.10` at the 90th percentile of `C-0087`'s measured staple incorporation, tied or untied,
at the link **penalty** and at `C-0208`'s **resolved** link; what the 59 ties are worth per cell at
a `45 / 14` tie split against the `50 / 9` split `C-0180` measured; and whether the cross-section
ordering survives the change of normalising convention.

**Not open.**
Whether the recommended `102 / 109` raster can be drawn on `15 × 4` at all — it can, and §2's cheap
bound settles it out of a committed result file with no solve, which closes the *"or, failing
that"* branch of this row's acceptance clause before any code is written.
Also not open: the **direction** of the ties on the free tile, which the Loewner statement
`K_tied ⪰ K_untied` fixes for the strain energy under any fixed load. It fixes nothing about peak
dishing, which is a **seminorm** of the field.

### The premise of this row's own acceptance clause that the lattice refuses

The row asks for the cells re-graded at *"same extent, same stations, same mandate, **same
normalising stroke** and the same 4 000-realisation dropout stream."*
Three of the five hold and the fourth does not.
The extent is the same (`116 bp = 39.44 nm`), the mandate is the same (`C-0017`'s
`33.3333 pN/nm` on the SUM), the stream is the same construction at the same seed — but the
**normalising stroke is exactly `2/3` of `10 × 6`'s**, and that is a theorem rather than a
measurement: the interior pressure is `F/(edgeX·edgeY)`, `edgeX` is shared, and
`edgeY = rasterRows · 3d/2`, so the stroke `p/k_f` scales as `1/rasterRows` and
`10/15 = 2/3` exactly. `5.27921926 nm` becomes **`3.5194795 nm`**.

Read as a statement about the **convention** — that both sides normalise by their own `p/k_f`, so
the comparison is controlled — the clause is right and is what this task adopts.
Read as a statement about the **number** it is false, and it has a consequence a synthesis must
carry: **`T-5b`'s `0.10` is a fraction of a stroke, so in absolute nm it demands `1.5×` flatter on
`15 × 4`** — `0.35194795 nm` against `0.527921926 nm`.
Both readings are therefore emitted, and a declared falsifier asks whether the ordering depends on
which one is taken.

### Locked units and conventions

Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa), angles **rad**
internally and degrees only in prose. `k_BT = 4.142 pN·nm` at **T = 300 K**, aqueous **2 mM
MgCl₂**. Dishing is reported **dimensionless, as a fraction of that tile's own free stroke** —
the convention `T-5b`'s `0.10` is written in — **and in absolute nm beside it**.

`s` runs **along** the helices, `y` **across** them in the plane of the face, `z` through the
block's thickness; the origin is the face centre; `W` is positive **downward**, toward the
electrode (`C-0006`). A point load carries `force` positive downward, so a coupling's upward
support force enters as its negative.

Geometry is **consumed, never re-derived**, and is `C-0180`'s own except for the cross-section:
`d = 2.536 nm` (SAXS), in-plane row pitch `3d/2 = 3.804 nm`, layer pitch `d√3/2 = 2.19624042 nm`,
rise `0.34 nm/bp`, crossover planes every **7 bp** with one pair per class every **21 bp**,
`k_θ = 13.5294118 pN·nm/rad`, `k_s = 64.7058824 pN/nm`.
`C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing
face only; `C-0017`'s mandate at §3's **acceptable** clause; seed `197197`, **4 000** realisations
on one common stream restricted per cell, `81 × 81` dishing grid, `T-5b`'s **0.10**;
ties at `firstAxialSign = +1`, `s = ±L/2`, **zero prestrain**.

Two **link** states, both `C-0208`'s own entry point `honeycombTiedLatticeAtResolvedLink`:

| state | transverse | radial | what it is |
|---|---|---|---|
| **penalty** | `1e4 pN/nm` | unset | `C-0180`'s and `C-0167`'s object, bit-identical at `radialLinkStiffness = null` |
| **resolved, control** | `254.808095` | `254.808095` | `C-0208`'s softest rung — every bond at `C-0205`'s shear ceiling |
| **resolved, measured** | `254.808095` | `754.005141` | `C-0208`'s middle rung, the connector candidate **plus** the measured pair term |

### The numeric targets

| | target |
|---|---|
| **`P1`** | the `15 × 4` lattice at `116 bp` is asserted to carry **`410`** staple bonds — `140` in plane and `270` through the thickness — and **`59`** raster turn ties split **`45`** through-thickness / **`14`** in plane, every tie asserted against the **bond graph** and not against the raster path, with `10 × 6`'s `435` (`135` / `300`) and `50` / `9` asserted in the same run |
| **`P2`** | the `15 × 4` free stroke reproduces the closed form `p/k_f = 3.5194795 nm` at every tile, relative departure `≤ 1e−9`, and the ratio to `10 × 6`'s `5.27921926 nm` is emitted and asserted equal to `2/3` |
| **`P3`** | all **64** `(f, placement, columns, distribution)` cells are graded on the `15 × 4` lattice in **both** tie states at the penalty link, and the count clearing `T-5b`'s `0.10` at the 90th percentile is reported beside `C-0180`'s **2 of 64** on the corresponding `10 × 6` cells |
| **`P4`** | the tie's worth is reported as the **median of the per-realisation ratio** on the shared stream, cell by cell — never as a scalar multiplier and never as a ratio of two order statistics — and its range is compared with `C-0180`'s `0.902845544`–`0.988116016` |
| **`P5`** | all **64** cells are graded tied at **both** resolved rungs, the rung sensitivity is measured at `15 × 4` rather than transferred, and the count is reported beside `C-0208`'s **0 of 64** |
| **`P6`** | the cross-section ordering is emitted **twice** — dishing over each tile's own stroke, and absolute peak dishing in nm — and stated as **unpaired**, because the two cross-sections carry different path counts and therefore different dropout streams |
| **`P7`** | the `10 × 6` control reproduces `C-0180`'s three tied free-tile readings, `C-0180`'s two recovered cells and `C-0208`'s tightest cell at a relative departure `≤ 1e−8`, out of their committed result files; and the `15 × 4` free stroke reproduces `C-0146`'s own committed `3.5194795` at the same block extent |
| **`P8`** | the uncoupled `15 × 4` tile is carried as the reference at **every** cell, and the count of coupled cells beating it is emitted |
| **`P9`** | two convergence axes — beam subdivisions `1 → 2` and the dishing sample grid `41 / 81 / 161` — are taken on the **`p90` of the tightest `15 × 4` cell**, and every same-quantity identity is emitted as a **threshold and a boolean**, never as a value |

### Acceptance predicates (falsifiable)

- **`A1`** every `15 × 4` census in this study is **derived and asserted**, and no count, split,
  station ladder, enhancement or stroke is transferred from `10 × 6`. *Falsified by any number in
  the result file whose provenance is a `10 × 6` reading.*
- **`A2`** the four `C-0167` placements and the four column counts are all realisable on
  `15 × 4`'s own station ladder, or the study names which are not and grades the rest.
  *Falsified by a cell silently dropped.*
- **`A3`** the standing falsifier — a uniform pressure on a uniform Winkler foundation dishes
  **exactly zero** — holds on the tied `15 × 4` coupled lattice, with the tributary strips one row
  pitch **centred on each beam's own axis**.
- **`A4`** the tied-versus-untied comparison at `15 × 4` is **paired**, one stream, read per
  realisation; the cross-section comparison is **unpaired** and says so.
- **`A5`** the deliverable is an **ordering with a validity range**, not a recommendation: nothing
  here re-opens the placement search, the distribution rule, the raster or the cross-section
  choice.

### Falsifiers, declared before the run

| | statement | expected |
|---|---|---|
| **`F1`** | a uniform pressure on the tied `15 × 4` coupled lattice dishes exactly zero (`< 1e−9`), and its mean deflection is `p/k_f` to `1e−9` | not to fire |
| **`F2`** | an **empty** tie list at `15 × 4` is bit-identical to the plain lattice — bond site set and load vector bit-identical, point-load dual and solved field at `1e−10` — and `radialLinkStiffness = null` is bit-identical to `honeycombTiedLatticeAtLinkStiffness` | not to fire |
| **`F3`** | the influence surrogate reproduces the **assembled** `15 × 4` solve at every graded distribution, `≤ 1e−9` | not to fire |
| **`F4`** | the `10 × 6` control reproduces `C-0180`'s and `C-0208`'s committed values at `≤ 1e−8` | not to fire |
| **`F5`** | **the tie set TRANSFERS** — the `15 × 4` block carries `435` bonds and a `50 / 9` tie split — **declared to FIRE**, and this row's own Notes cell says it should: the count `H − 1 = 59` transfers and nothing else does | **expected to fire**; predicted `410` bonds and `45 / 14` |
| **`F6`** | **the tied `15 × 4` lattice recovers at least one coupled cell at the penalty link — declared OPEN** | expected not to fire; the cheap bound puts every candidate `2.0×` or more over the tolerance |
| **`F7`** | **the tie's per-cell worth at `15 × 4` lies inside `C-0180`'s own median-ratio band `0.902845544`–`0.988116016` — declared OPEN** | unknown; the tie split is `45 / 14` against `50 / 9`, and a ratio is not a lattice invariant |
| **`F8`** | **the ties are ADVERSE at some `15 × 4` cell** — some median per-realisation ratio exceeds `1` — **declared OPEN** | unknown; `C-0180` reads `0 of 64` in the median and `27 of 64` in the ratio's own 90th percentile |
| **`F9`** | **the ties move an UNCOUPLED `15 × 4` reading the adverse way at some enhancement — declared OPEN.** This row's Notes cell asserts *"the ties move every uncoupled reading the favourable way, so the tied re-grade can only widen a gap that is already decided"*, and that is a monotonicity assertion about a seminorm | expected not to fire; if it fires, this row's own stated ground is withdrawn and a challenge is owed against it |
| **`F10`** | **the cross-section ordering REVERSES under the absolute (nm) reading — declared OPEN** | expected not to fire; the stroke ratio is `2/3` — worth `1.5×` in the absolute reading — where the uncoupled gap between the two cross-sections is a factor of several, but the two are read at different enhancements so no single ratio is quoted here and the run emits both columns |
| **`F11`** | **the resolved-link rung axis moves a `15 × 4` `p90` by more than `5 %` — declared OPEN.** `C-0208`'s own five rungs move its tightest `p90` by `3.83e−4`, i.e. `0.38 %`, which is what licenses grading **two** rungs here rather than five | expected not to fire; if it fires, the two-rung reduction is void and the other three rungs are owed |
| **`F12`** | a `15 × 4` placement **refuses** its station snap — a five-column placement colliding two of a five-station row, or a row shorter than its column count — **declared OPEN** | expected not to fire; `T-245` emits `sparsestRowStations = 5` at this raster |
| **`F13`** | a cell whose `T-5b` verdict moves keeps it under its own convergence axes — **declared OPEN**, and vacuous if no verdict moves | unknown |
| **`F14`** | **two independent runs produce a byte-identical result file** — declared CLOSED, and discharged by an actual second run diffed **outside** the study | not to fire |

## 2. Plan

### The cheap bound, and it runs before any solve — three questions, all answered

**(a) Can the recommended raster be drawn on `15 × 4` at all?**
This is the *"or, failing that"* branch of the acceptance clause, and it is closed out of
`gpd/results/T-245-closing-raster-selection.json` with **no solve and no code**.
`C-0151`'s selection swept the closure on **both** 60-helix cross-sections, and its
`closingResidueClasses` block carries three residue pairs — `(7, 14)`, `(17, 3)`, `(18, 4)` —
**identical at `10 × 6` and `15 × 4`**, every one at `L₁ − L₂ ≡ 14 (mod 21)`.
`102 mod 21 = 18` and `109 mod 21 = 4`, so the recommended pair is the third of them and it closes
at both cross-sections.
Its `closingFamily` row for `15 × 4` at `102 / 109` gives, exactly:

| | `10 × 6` | `15 × 4` |
|---|---|---|
| closes / forced crossovers | yes / **0** | yes / **0** |
| block extent | **116 bp = 39.44 nm** | **116 bp = 39.44 nm** |
| row span / interface window | 109 / 102 bp | 109 / 102 bp |
| class-zero residue `b₀` | **5** | **5** |
| ladder phase / inter-row offset | **16 / 14 bp** | **16 / 14 bp** |
| stations on the face | 55 of 60 | **82 of 90** |
| sparsest row | **5** | **5** |
| scaffold / M13 spare | 6 330 / 919 nt | **6 337 / 912 nt** |

**So the whole comparison geometry transfers, and the branch that would have made this row a
statement instead of a measurement is closed for the price of reading a committed file.**
The sparsest row carries five stations at both cross-sections, which is why the same four column
counts `1 / 2 / 3 / 5` are available — asserted as `F12` rather than assumed.

**(b) What does NOT transfer, derived and not measured?**
Four things, all integer or closed-form arithmetic:

| | `10 × 6` | `15 × 4` | why |
|---|---|---|---|
| staple bonds | `435` = `27 × 5 + 50 × 6` | **`410`** = `28 × 5 + 45 × 6` | the interface census, `C-0154` |
| raster turn ties | `59` = **`50`** through + **`9`** in plane | `59` = **`45`** through + **`14`** in plane | `m(n−1)` and `m−1`, `C-0175` |
| `hingeStiffnessEnhancement` at `f = 0.30 / 0.26` | `21.1851817` / `18.4938242` | **`9.65079217`** / **`8.49735322`** | `1 + f·S·Σy²/(nB)` at `n = 4` layers |
| `edgeY` / free stroke | `38.04 nm` / `5.27921926 nm` | **`57.06 nm`** / **`3.5194795 nm`** | `m · 3d/2`, and `p = F/(edgeX·edgeY)` |

The enhancement is the one that decides the size of the gap: a `15 × 4` block stacks **four**
layers, so its parallel-axis factor is `29.8359739` where `10 × 6`'s is `68.2839391`, and the
calibrated enhancement is **`2.19` to `2.18×` smaller** at the same composite fraction.
That is not a coupling result — it is `Σy²` over four offsets instead of six — and it is why the
uncoupled ordering was never close.

**(c) How much can a coupled `15 × 4` cell possibly move?**
The most favourable transfer any hypothesis offers is `C-0180`'s own free-tile tie ratio
`0.890395426` applied to the untied `p90`, which needs a `p90` below `0.112309652`.
`C-0154`'s uncoupled `15 × 4` readings are `0.220064299`–`0.312237799` **before any coupling**, and
`C-0109`'s *every coupled cell is worse than the uncoupled tile* reproduces at
**64 of 64** on `C-0180`'s tied `10 × 6` lattice, **16 of 16** on `C-0142`'s and
**0 of 32** on `C-0212`'s searched cells.

Those three `C-0154` readings are taken at a **`112 bp`** row and this study builds at **`116 bp`**,
which is not a rounding: at `10 × 6` the same quantity reads `0.127358454` at `112` and
`0.132443428` at `116`, **`1.04×`**, because the block's four-base-pair overhang past its last
crossover plane carries beam, foundation and load and no bond.
So the `15 × 4` uncoupled figures are **re-derived at `116 bp`** here and the `C-0154` values enter
only as the cheap bound's input — `CLAUDE.md`'s *a lattice census must be asserted at the row length
the studies build*, applied to a dishing reading rather than to a bond count.
And the row's *"eight `15 × 4` coupled cells"* are already on disk, on the **smeared** four-layer
model, at this very block extent: `gpd/results/T-235-coupled-cells-at-the-two-length-raster.json`
carries `C-0146`'s eight at `crossSection = 15 x 4`, `widthReading = block extent 116 bp`,
`f = 0.30`, with `edgeY = 57.06`, `interiorPressure = 0.0444356284` and
`freeStroke = 3.5194795` — **an independent committed emission of the stroke this task derives**,
which turns `P2` from a derivation into a reproduction.
Their `p90` runs **`0.141713508`** (5 columns, rim-graded) to **`0.327149593`**, and **none is
flat**. `C-0167`'s smeared→grillage correction at `10 × 6` was `1.87602525×` at its recommended cell, so
the same eight on the grillage are expected around `0.27` and the ties are worth at most `1.12×`
against that.

So the bound says the answer is almost certainly `0 of 64` **and it cannot say so**: it is a
transfer of a free-tile ratio, which `C-0180` §3 measures to be a **ceiling** the coupled cells
never reach; `C-0167`'s `1.87602525×` is itself *"not a multiplier"* by its own §1; and `C-0109`'s
statement is an empirical regularity rather than a theorem.
What the bound does decide is the **size of the margin the run is looking for** — a factor of two
or more, not the `0.198 %` and `0.426 %` the `10 × 6` verdicts have turned on — and that is what
licenses §2's two-rung reduction on the link axis.

**What the cheap bound also decides is the METHOD, in three places.**

1. **The prestrain deliverable is NOT mirrored.** `C-0180` §4 grades the ties at
   `±8.57142857°`, and its coordinate is **withdrawn**:
   [`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md) is **UPHELD** by
   [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`) —
   a level displacement rotates both backbones the same way, so the departure's coefficient on a
   relative roll is **exactly zero**. Mirroring it would reproduce a coordinate the corpus has
   retired, at half the run's cost. It is dropped and the reason is stated.
2. **The twist eigenstrain that replaced it is NOT mirrored either**, and for a different reason:
   `C-0190` reads `0 of 64` flat at either sign on `10 × 6`, so it cannot separate two
   cross-sections that are both `0`; and its magnitude *"rests on a spring the model does not
   have"* and is published as a threshold rather than a value.
3. **The resolved link is graded at TWO of `C-0208`'s five rungs, not five.** `C-0208`'s own
   census column moves its tightest `p90` by `3.83e−4` over the whole rung ladder
   (`0.100581834` → `0.100198485` → `0.100210806`), i.e. **`0.38 %`**, against a `15 × 4` margin
   the bound puts at `100 %` or more. The two graded are the ladder's **softest** (the control,
   every bond at the shear ceiling) and its **measured** middle rung. Because that `0.38 %` is
   measured on the *other* cross-section — exactly the transfer this task exists to distrust —
   the rung sensitivity is **measured again at `15 × 4`** and `F11` fires if it exceeds `5 %`.

### The method, justified against cost

**This is a re-run at a different constructor argument, and that claim is verified rather than
inherited.**

Every object the study needs is already parameterised by the block:
`HoneycombBlock(rasterRows, helicesPerRow)`, `HoneycombGrillage(block, rowBasePairs, …)`,
`honeycombScaffoldTurnTies(block, nodesPerBeam, …)`,
`honeycombTiedLatticeAtResolvedLink(block, rowBasePairs, …)`,
`honeycombTiedSurrogate(lattice, grid, pressure)`,
`twoLengthRaster(rasterRows, helicesPerRow, 102, 109)`,
`twoLengthSnappedGrid(raster, columns, edgeY, 16, 14)`,
`attachmentGrid(columns, rasterRows, edgeX, edgeY)`,
`measuredDepthIncorporation(edgeX, edgeY)`.
**No shared source is edited.** A new model file and a new study main are written and
`structure/ResultInputs.kt` gains one handle, which is provably inert (`ResultInputs.all` is read
at eight sites, all of them in `structure/ResultInputsTest.kt`).

The lattice is `4 320` unknowns at half-bandwidth `243` at both cross-sections — `60` helices, four
coordinates, seventeen nodes — so a factorisation is shared per lattice object and each influence
function is one back-substitution. Eleven influence banks are built: four at the `15 × 4` penalty
link (two tie states × two `f`), four at the two resolved rungs, one fine (`subdivisions = 2`) for
the convergence axis, and two partial `10 × 6` control banks for the reproductions. The `15 × 4`
path counts are `15 / 30 / 45 / 75` against `10 × 6`'s `10 / 20 / 30 / 50`, so the Monte Carlo is
`3.37×` `C-0180`'s in the Woodbury term and `1.5×` in the dishing evaluation.

**The `10 × 6` side is QUOTED, not re-run.** `C-0180`'s 64 tied cells and `C-0208`'s five census
rows are committed; re-grading them would double the run to reproduce numbers that are already on
disk. What this study reproduces instead is the **machinery**: three tied free tiles, the two
recovered cells and `C-0208`'s tightest cell, at `≤ 1e−8`. That is what licenses reading the rest
of both files, and it is `F4`.

The alternative — a full lattice solve per dropout realisation — is four orders dearer for an
answer superposition gives exactly.

### What would falsify the approach

- **`F2` firing.** If an empty tie list at `15 × 4` is not bit-identical to the plain lattice, the
  tie extension is not additive at this cross-section, the pairing is between two models rather
  than two states of one object, and every ratio here is a comparison of noise.
- **`F1` firing.** A corrugated honeycomb face broke the standing uniform-load falsifier once
  (`CH-0214`, an unstated row-length precondition) and 59 rim ties are a second chance to break it.
  `15 × 4` has **fifteen** face beams and a `14 / 45` tie split, so the tributary construction is
  exercised differently from `10 × 6`'s.
- **`F4` firing.** If the control does not reproduce `C-0180` and `C-0208`, the cross-section
  comparison is between this study's machinery and somebody else's, not between two blocks.
- **`F12` firing.** If a placement refuses its snap, the acceptance clause's *"same stations"* is
  false and the deliverable becomes a partial census that says which cells exist.

### What this cannot establish

- **TRL 1–3.** Model-consistent and traceable, not empirically demonstrated. `k_θ` at a scaffold
  turn is asserted equal to `k_θ` at a staple crossover because it is the same covalent object,
  and `k_θ` itself is `Gen1Tile`'s **square-lattice-fitted** constant (`CH-0227` §7, inherited
  verbatim). The tie sits at `s = ±L/2` exactly, where a scaffold crossover sits `5 bp` from a
  staple position.
- **The cross-section comparison is UNPAIRED.** The two blocks carry different path counts, so no
  realisation of one corresponds to a realisation of the other; sharing a seed would not make the
  defect patterns the same. The deliverable is an **ordering** and a **margin**, never a paired
  ratio.
- **It is a comparison on TRANSFERRED distributions.**
  [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) (`T-316`) shows that
  `C-0208`'s `0 of 64` **reverses to `22 of 32`** the moment the distribution is *searched* rather
  than transferred, and every cell graded here is on `C-0058`'s equal-spring and rim-graded `5:1`
  rules. So an ordering taken here is an ordering **of two blocks under two fixed rules**, and a
  searched `15 × 4` census is a separate question. The cheap half of it is stated rather than run:
  `C-0212` measures the search worth **`1.45251772×`** at its own tightest cell and reads
  **`0 of 32`** searched cells beating the uncoupled tile — and `15 × 4`'s uncoupled tile is itself
  outside `T-5b` — so the transfer does not close the gap. That is a row (`T-330`), not a caveat
  that decides anything here.
- The lattice carries **no** across-helix parallel-axis term, so its `D_⊥` is the independent lower
  bound and the enhancement enters as a smeared multiplier (`C-0167` §8); Kirchhoff is not safe at
  these thicknesses, so every `D_∥` is an upper bound.
- The dropout statistics are measured on a **single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers or its ties. A missing scaffold turn is not in this model at all.
- Nothing here re-opens the placement search, the distribution rule, the raster or the
  cross-section choice, and nothing here is a recommendation.
