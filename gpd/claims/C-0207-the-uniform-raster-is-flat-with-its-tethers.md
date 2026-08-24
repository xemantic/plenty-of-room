# C-0207 — **`C-0201`'s ALARM DOES NOT SURVIVE ITS OWN RELOCATION: ROUTE B'S UNIFORM RASTER IS FLAT WITH ITS TETHERS, AT EVERY LATTICE PHASE AND EVERY CORNER — `756 of 756`.** Graded with the 59 chains its own geometry implies, at the **per-turn** span distribution its own lattice gives, the three uniform paired rows the built `28 nt` allowance affords — `92 / 98 / 106 bp` — read **`0.0483790868–0.0946863482`** of the stroke against `T-5b`'s `0.10`, where `C-0201` §7 graded the same three widths **untied** at `0.0425678289 / 0.0422200543 / 0.0451172785` and this study reproduces all three at a worst departure of **`8.6e−10`**. **The span reaching `C-0201`'s worst corner is a LEVEL and the dishing is a FIELD**, and the transfer between them was never made: the worst turn spans `3.93454333–4.35327572 nm` exactly as `C-0204` §6 measures, and it costs at most `0.0502869583` of the stroke because the 59 contributions are **signed** and only `50` of them have a coordinate at all. **The lattice phase is a real design variable worth `1.82364566×`** — best `5 / 16 / 9`, worst `10 / 12 / 14` — and **`F6` FIRED**: the phase `C-0204` names best on `turnsInsideTheAlignedHalf` is phase **`7`** at all three widths and is the dishing optimum at **none** of them, costing `1.053–1.354×`. The cheap bound ran first, said the mean chain tension is `3.14109619–4.49761906×` the determined raster's and predicted a **straddle**, and was wrong in the favourable direction; the `C-0104` triangle-inequality ceiling built from 59 unit-tension solves is honoured at `756 of 756` cells and is loose by `1.949–5.438×`. **And the `nodeS` precondition did not bite**: `92 mod 7 = 1` and `106 mod 7 = 1`, and the uniform-load falsifier reads exactly `0.0` at all three

| | |
|---|---|
| **Task** | [`T-307`](../tasks/T-307-uniform-raster-tether-spans.md), raised by [`C-0204`](C-0204-the-anchor-azimuth-is-determined.md) §6 (`T-304`), whose `F7` was declared open and **FIRED** |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (exact integer residue arithmetic on this repository's own honeycomb crossover lattice, reproduced against `T-304`'s committed `uniformRasters` at all **63** `(width, phase)` rows) **+ in-silico** (the same honeycomb grillage and the same `T-299` tether element, at a **per-turn** span) **+ literature** (`C-0193`'s and `C-0200`'s reading of the built allowance, `T-71`'s measured phosphate radius, `T-230`'s ssDNA Kuhn and contour brackets) |
| **Verdict** | **PASS on all seven predicates. Of the nine declared falsifiers `F6` FIRED — declared open, and its firing is a finding about the CRITERION rather than about the design.** `F1`, `F2`, `F3`, `F4`, `F7` and `F9` did not fire; `F5` and `F8`, both declared open, did not fire either. Raises [`CH-0262`](../challenges/CH-0262-the-alarm-did-not-survive-its-relocation.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The three widths are what three scaffolds afford at the built `28 nt` allowance; **no such raster has been drawn**, let alone folded |
| **Provenance** | [`gpd/results/T-307-uniform-raster-tether-spans.json`](../results/T-307-uniform-raster-tether-spans.json), written by [`tile/UniformRasterTetherStudy.kt`](../../src/main/kotlin/tile/UniformRasterTetherStudy.kt) (**new**) on [`tile/UniformRasterTetherSpans.kt`](../../src/main/kotlin/tile/UniformRasterTetherSpans.kt) (**new**). **27 named tests** written first and watched fail, and a **12-mutation** harness at [`tools/T-307-mutation-test.py`](../../tools/T-307-mutation-test.py) — **0 survivors**, after a first run whose single survivor was a guard duplicated downstream (§8). The result file is **byte-identical across two independent runs**. **No existing Kotlin main source was modified** except one line of `structure/ResultInputs.kt`, the generated handle registry — provably inert, `ResultInputs.all` being read at 8 sites all inside `structure/ResultInputsTest.kt`. Full suite in an isolated snapshot: **3 459 tests, 0 failures, 0 errors** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `C-0022`'s design state — 10 nm gap, `0.192 V`, its solved collar read from `T-3b`; cross-section **`10 × 6`**; **uniform** route-B raster, paired row `scaffoldNucleotides / 60 − 28` = `92 / 98 / 106 bp`; `d` = **2.536 nm** (SAXS), rise `0.34 nm/bp`, phosphate radius **`0.908637858 nm`** (`T-71`, measured); anchor offset **0** (the loop sits outboard); ssDNA Kuhn **2.10–2.84 nm** (zero force) with the **inextensible** contour **0.65–0.70 nm/nt**; three readings of the built `28 nt` allowance (`28 / 28`, and `C-0200`'s ordered `24 / 32` in **both** rim assignments); composite fraction `f = 0.30`; `k_link = 1e4 pN/nm`; `subdivisions = 1` at every headline cell |
| **Consumes** | [`C-0204`](C-0204-the-anchor-azimuth-is-determined.md)/`T-304` (the azimuth closed form and the span census, **reproduced at all 63 `(width, phase)` rows**, and two determined-span tensions), [`C-0201`](C-0201-the-tether-is-a-load-not-a-spring.md)/`T-299` (the element unchanged, and §7's three untied widths, **all reproduced**), [`C-0193`](C-0193-the-built-turn-is-a-tether.md)/`T-296` and [`C-0200`](C-0200-the-file-draws-and-the-table-orders.md)/`T-302` (the built allowance and its `12 / 16` split), [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md)/`T-230` (the span geometry, **reproduced**), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load, which is what makes the bank exact), [`C-0022`](C-0022-tile-edge-load-profile.md)/`T-3b` |
| **Raises** | [`CH-0262`](../challenges/CH-0262-the-alarm-did-not-survive-its-relocation.md) |

---

## The claim, in four lines

`C-0204` determined route B's tether span on the **drawable** raster and found the free tile flat
at `16 of 16` corners — and said, correctly, that the answer is **bought by drawability**.

Route B's own **uniform** rows close at no lattice phase, so their span is a **distribution**
reaching `C-0201`'s worst corner, and three artifacts concluded that `C-0201`'s alarm was
therefore **relocated** there rather than withdrawn.

**Nobody graded it.** Graded, it is flat: `756 of 756` cells, at every phase and every corner.

A span is a **level** and a dishing is a **field**, and the step between them was assumed.

---

## 1. The cheap bound ran first, in two stages, and it was wrong in the favourable direction

**Stage 1 needs no solve at all.** The span distribution is a lattice arithmetic, so the chain
tension follows from one inverse Langevin per turn:

| | |
|---|---|
| `C-0204`'s determined span, `28 nt` | **`0.175872271–0.256181866 pN`** |
| the uniform raster's **mean** tension, over all 21 phases and all 12 corners | **`3.14109619–4.49761906×`** that |
| its **worst** turn | **`6.64777142×`** |

`C-0204` measures the preload's own worth on the free tile at the determined span as
`0.00708426936–0.0195297045` of the stroke. A mean tension three to four and a half times larger,
on top of `C-0201` §7's untied `0.0422200543–0.0451172785`, predicts a reading that **straddles**
`T-5b` — which is why the question needed a solve and not an estimate. **The prediction was
recorded in the result file before the grading section ran, and it is wrong in the favourable
direction**: measured, the preload is worth `0.00361034942–0.0502869583` and the worst cell is
`0.0946863482`.

**Stage 2 is a ceiling, and it is `C-0104`'s linearity read as a bank.** A tether's tension
changes no entry of the stiffness matrix, so one unit-tension solve per turn — `59` per width,
`177` in all — gives a triangle-inequality ceiling at **every** phase and **every** corner with no
further solve. **It is rigorous on the field the bank spans** — the zero-stiffness lattice, whose
stiffness matrix is the untied one's, `dishing` being linear in the coefficients at every point so
that `max|Σ| ≤ Σ max| |` — and the reading it is checked against carries the tether's own
**stiffness**, which is a different matrix. So the comparison is a **measurement** and not a
theorem, and it is stated as one. Honoured at `756 of 756` cells (`F7`), and loose by **`1.949–5.438×`** —
the reciprocal of the measured dishing over its own ceiling, both of which the result file carries
per cell, the worst being the emitted `5.43839636` —
because the 59 contributions are **signed**: `27 of 177` bank columns are **exactly zero**, which
is the nine in-plane turns at each width, whose pull is entirely along a direction this model has
no coordinate for.

## 2. The answer

| | `C-0201` §7, **untied** | this claim, **with the tethers** |
|---|---|---|
| `92 bp` = `31.28 nm` (M13mp18) | `0.0425678289` | best phase **5**, `0.051906358`; worst phase 10, `0.0744541307` |
| `98 bp` = `33.32 nm` (p7560) | `0.0422200543` | best phase **16**, `0.0572710579`; worst phase 12, `0.0734764038` |
| `106 bp` = `36.04 nm` (p8064) | `0.0451172785` | best phase **9**, `0.0519214617`; worst phase 14, `0.0946863482` |
| flat against `T-5b`'s `0.10` | 3 of 3 | **`756 of 756` cells** |

The untied column is **reproduced** here rather than cited — worst departure `8.6e−10` over the
three widths and their three row widths — so the comparison is against the same object.

Each *"best phase"* cell is a **minimax over the twelve chain corners**, which is the criterion a
design is entitled to: the two Kuhn ends, the two contour ends, and the three readings of the
built `28 nt` allowance (`28 / 28`, and `C-0200`'s ordered `24 / 32` at either rim).

## 3. The lattice phase is worth `1.82364566×`, and the criterion `C-0204` supplies is the wrong one

`C-0204` §6 reports the best phase of each uniform width on `turnsInsideTheAlignedHalf` — how
many of the 59 turns fall inside `span < d` — and that optimum is phase **`7`** at all three
widths (`40 / 49 / 40` of 59). It is the dishing optimum at **none** of them:

| row | dishing optimum | worst corner there | alignment optimum | worst corner there | the ratio of the last two |
|---|---|---|---|---|---|
| `92 bp` | phase **5** | `0.051906358` | phase 7 | `0.0619282294` | `1.193×` |
| `98 bp` | phase **16** | `0.0572710579` | phase 7 | `0.060314469` | `1.053×` |
| `106 bp` | phase **9** | `0.0519214617` | phase 7 | `0.070280339` | `1.354×` |

**`F6` was declared open and FIRED.** Both criteria are read off the same 59 spans, and they rank
the same 21 designs differently, because the alignment count is a **census of a level** and the
dishing is a **weighted signed sum of a field**: a turn whose span is long matters only in
proportion to its own influence, `0.0–0.0450809211` of the stroke per pN across the bank, and the
nine in-plane turns have influence exactly zero however badly aligned they are. It is
`CLAUDE.md`'s own *two channels can rank the same build rule oppositely*, on a design variable
rather than on a build rule — and it is why the recommendation below is stated on the objective
the design is built to and not on the one that is cheap to evaluate.

**Recommendation, per width, on the minimax over the twelve chain corners:** `b₀ = 5` at 92 bp,
`b₀ = 16` at 98 bp, `b₀ = 9` at 106 bp. Every phase of every width is flat, so this buys margin
rather than a verdict — `1.28–1.82×` of it — and it costs nothing, because the phase is free.

## 4. The preload carries the movement, and the stiffness is small and no longer nothing

| | over the 756 cells |
|---|---|
| free tile, preload **on** | `0.0483790868–0.0946863482` |
| free tile, preload **off** | `0.0419156819–0.0447687374`, `756 of 756` flat |
| the preload's own worth | **`0.00361034942–0.0502869583`** |
| the same tensions with **both stiffnesses zeroed** — the bank's own lattice | departs from the exact solve by at most **`0.00678987143`** |

The soft end of the preload-off column, `0.0419156819`, is **below** the untied `0.0422200543`:
the tether's stiffness alone moves the tile *toward* flatness, exactly as `C-0201` §1 measured on
the drawable raster. `C-0201`'s *"a LOAD, not a spring"* is **upheld in its ordering** and is
softened in its size: at a span three to four times the determined one the stiffness is worth
`0.135022512` of the largest preload movement and `0.0678987143` of `T-5b`'s whole tolerance,
against the `9.19840405E−5` of the link penalty `C-0201`'s cheap bound quotes. **A stiffness that
is arithmetically absent against a penalty constant is not arithmetically absent against a
tolerance.**

## 5. The `nodeS` precondition did not bite, and the falsifier is what would have said so

`CLAUDE.md` records `HoneycombGrillage.nodeS` carrying an unstated precondition on its own
`rowBasePairs` — `(0..rowBasePairs step 7)`, so a row that is not a multiple of the 7 bp
crossover-plane pitch would leave its outer strip with no element, no foundation and no load, and
*"a uniform pressure dishes 0.15 of the stroke where the exact answer is zero"*. **Two of this
study's three rows are exactly that case**: `92 mod 7 = 1` and `106 mod 7 = 1`, and only `98` is a
multiple.

It was checked **first**, before anything was graded. The repair is already in the tree — `nodeS`
carries a free-overhang branch, added for `C-0151`'s 116 bp block, whose KDoc names the standing
falsifier as the reason — and `F1` reads **exactly `0.0`** at all three row lengths. Two named
tests assert it independently: the uniform-load dishing, and that the first and last node land on
`±L/2` at each of the three.

## 6. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a span in nm from `d` and `r_P` in nm; a tension in pN from `k_BT/b`; a stiffness in pN/nm; a contour in nm/nt never mixed with a rise in nm/bp; a level as an integer base-pair count; dishing dimensionless, as a fraction of the free stroke of the **same** lattice | **PASS** |
| **2 — limiting cases** | a chain too short to reach its own span is **refused** rather than extrapolated, and the whole declared bracket is asserted reachable at all three widths and all 21 phases; a stiffness override of `0.0` makes the element list **inert** in the stiffness matrix; a phase outside `[0, 21)` and a non-positive row length are both refused, **at construction**; a bank column carries a unit tension at one turn and exactly zero at the other 58 | **PASS**, 27 named tests |
| **3 — symmetry and the standing falsifiers** | the uniform-load falsifier at all three row lengths, preload off (`F1`, exactly `0.0`); a stiffness-free preload-free tether list **bit-identical** to the untethered lattice on `assembleLoad` over every degree of freedom, at all three row lengths (`F4`); the preload annihilated by a rigid roll **per element** and not only in the sum, at all 59; the two rim chains exchanged under a reversed traversal sense at all 59 turns | **PASS** |
| **4 — numerical convergence** | re-taken on the **deciding quantity at the deciding cell** — the cell closest to `T-5b`, `106 bp` at phase 14 — over the dishing sample grid `41 / 81 / 161` and nested beam subdivisions `1 / 2`. The sample grid moves the answer by **`0.0`** at both refinements; the subdivision by **`9.4e−6`**, and `0 of 5` readings move the verdict | **PASS** |
| **5 — literature and upstream** | **ten reproductions**, worst departure `4.8e−9`: `C-0204`'s per-phase span census at all **63** `(width, phase)` rows on min, max, mean, distinct count *and* aligned count; two of its determined-span tensions; `C-0201` §7's three untied dishings and three row widths; `C-0147`'s `d + 2r_P`. Every closed form is the corpus's own function, **called** rather than re-implemented | **PASS** |

### The nine declared falsifiers

| # | falsifier | fired |
|---|---|---|
| `F1` | a uniform pressure on the free tethered lattice, preload off, dishes more than `1e−9` of the stroke at any of `92 / 98 / 106 bp` | **no** — exactly `0.0` at all three |
| `F2` | the untied re-grade fails to reproduce `C-0201` §7's three uncoupled dishings to `1e−8` | **no** — worst `8.6e−10` |
| `F3` | the per-turn span census fails to reproduce `T-304`'s committed `uniformRasters` | **no** — `63 of 63` rows, worst `4.8e−9` nm |
| `F4` | a stiffness-free, preload-free tether list is not bit-identical to the untethered lattice on `assembleLoad` at some row length | **no** — bit-identical at all three |
| `F5` *(open)* | no lattice phase of any uniform width leaves the free tile inside `T-5b` at every corner | **no** — **every** phase of **every** width does, `756 of 756` |
| `F6` *(open)* | the phase that minimises the free-tile dishing is not the phase `T-304` names best on `turnsInsideTheAlignedHalf` | **FIRED** — `3 of 3` widths disagree |
| `F7` | the `C-0104` triangle-inequality ceiling is exceeded by a measured dishing at some cell | **no** — `756 of 756` honour it |
| `F8` *(open)* | the flatness verdict at the deciding cell moves under beam subdivision `1 → 2` | **no** — `0.0946863482 → 0.094695778` |
| `F9` | the reach bound refuses at some corner | **no** — every corner is constructed, and reachability is asserted over the whole bracket |

## 7. What the coupled cells would need, and why they are not here

`C-0167`'s 64 cells are graded on placements, station lattices and distributions **derived at the
`116 bp` block extent**. At `92 / 98 / 106 bp` the station ladder, the plan ceiling and the
centro-symmetric family all move, so re-grading them is a **placement search** and not a re-grade,
and `CLAUDE.md`'s own *a two-step design is not a design — the snap is a re-evaluation of every
rule written on a position* is exactly why it cannot be shortcut. `C-0201` and `C-0204` both read
`0 of 64` coupled at every tethered state on the recommended raster. **Named, not answered.**

## 8. The mutation test, and its one survivor was a guard duplicated downstream

`C-0161`'s standard on a Kotlin subject: **twelve mutations, every one of which must fail a NAMED
test**, with the unmutated copy run first and its failures subtracted (`CH-0237`),
`find src -name '<file>.kt'` asserted to return **exactly one** path (`C-0190`), every anchor
asserted to occur exactly once (`C-0185`), and the `-x` flags **derived** from
`build.gradle.kts`'s own `dependsOn` block (`C-0194`).

**First run: 11 killed, `1 SURVIVED`.** Widening `require(classZeroResidue in 0 until 21)` to
`in −100 until 21` failed nothing — because `HoneycombRasterTurnAnchors` carries **the same
guard**, and every test of it went through `.spans`, which builds that census. A guard whose only
observable behaviour is duplicated downstream is a guard no mutation of it can reach. The repair
is the **fixture**: the new test constructs the object and never touches `.spans`, so it tests
*this* class's own contract — refuse at **construction**, where the mistake is, rather than at
first use. **After it: 12 mutations, `0` survivors.**

It is `C-0204` §8's *a guard tested at one end only* one step out: there the fixture could not
reach half of the guard, here it could not reach the guard at all.

---

## 9. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated. No folding
  experiment is reported and this repository cannot run one.
- **This is the FREE tile.** The coupled reading at these widths is §7 and is not answered.
- **The whole branch is conditional on route B.** `C-0193` and `C-0200` establish that the built
  object is route B; `C-0201` establishes that route B's turn is a **load** and a dishing
  **source** where route A's tie is a **sink**. Nothing here grades route A.
- **`C-0204`'s determined span is a property of the DRAWABLE two-length raster**, not of route B
  in general, and this claim is about the **uniform** rasters route B's own scaffold budget
  affords, which close at no lattice phase. The two must not be conflated.
- **The element is `C-0201`'s, unchanged**: a linearisation about the built, taut state,
  one-sided, with the anchor at the beam axis on `C-0194`'s frame-indifferent `d/2` arm rather
  than at the phosphate radius.
- The lattice carries **no steric floor** between two duplexes and **no across-helix parallel-axis
  term**; `k_θ` is `Gen1Tile`'s square-lattice-fitted constant; and `CH-0242`'s common-mode spring
  is absent, so every bond and tie here is missing the stiffer of the two springs.
- **The three widths are the MAXIMUM uniform paired row each scaffold affords at the `28 nt`
  allowance.** A design that spent fewer paired bases on a longer loop is a different point of the
  same budget and is not graded.
- **The tile is still too small.** `C-0201` §7's larger finding stands untouched: `31.28`,
  `33.32` and `36.04 nm` against §3's `40 nm`, `−21.8 %` to `−9.9 %`.
- Nothing here re-opens the raster, the cross-section, the placement search or the distribution
  rule.

## 10. Still open — named, not answered

- **The coupled reading at `92 / 98 / 106 bp`**, which needs a placement search rather than a
  re-grade of `C-0167`'s 116 bp stations (§7).
- **Whether a route-B design should trade paired row length against span.** These three widths
  are the maximum each scaffold affords; a shorter row with a longer loop is a different point of
  the same budget and nobody has swept it.
- **The BUILT block's own lattice phase**, which needs a register read of the deposited `10 × 6`
  file rather than a derivation.
- **What a phosphate-radius attachment arm is worth**, which `C-0201` prices, does not measure,
  and this study inherits unchanged.
