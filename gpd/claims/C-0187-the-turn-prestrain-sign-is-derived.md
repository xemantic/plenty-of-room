# C-0187 — **THE SIGN IS NOT FREE: caDNAno's `±5 bp` IS THE SAME RULE THAT SUPPLIES THE MAGNITUDE, AND ON THE DRAWABLE RASTER `C-0148`'s CLOSURE CONDITION PINS `b₀ = 5` UNIQUELY — SO `2^59` SIGN ASSIGNMENTS COLLAPSE TO ONE GLOBAL PHASE, STRICTLY ALTERNATING, EQUIVALENTLY SET BY THE AXIAL RIM THE TURN SITS AT.** A crossover placed `+5 bp` from its pair's staple position falls **0.25 bp short** of the exact `5.25 bp` half turn and one placed `−5 bp` sits **0.25 bp past** it: equal magnitude, opposite sign, and the residues `[0, 10]` say which. `102 / 109` reads `(L − 7Δ) mod 21 = 11` at **every one of its 58 interior helices**, so every helix flips the sign — 30 turns at the high rim carrying `+8.57142857°` and 29 at the low rim carrying the negation, **invariant at all eight readings** of `firstAxialSign`, `mirrored` and `axialReversed`. That is `C-0175` §8's third swept assignment, *"by the rim the turn sits at"* — one of three guesses, and the derived one, reproduced at **`8.9e−10`**. **So `C-0180`'s two UNIFORM readings are both off-lattice**, and the binary really left is a *global phase*. **Graded on the same 64 cells: `1 of 64` flat at phase `+1`, `1 of 64` at phase `−1`, `0 of 64` at BOTH, `2 of 64` phase-contingent — so deliverable (b) is NOT reachable in this family and that is said plainly.** The exposure is bounded instead: the worst `|movement|` over 128 prestrained cells is **`0.00203756217`** of the stroke and the median `0.000581022203` — a min/max and a median over this study's own 128 `cells[*].movementFromZeroPrestrain` records — so a cell is flat at both phases whenever its zero-prestrain `p90` is below **`0.0979624378`** — and the tightest cell the corpus owns is `0.0995744767`, so **0 of 64 qualify**. **And the one binary the lattice cannot orient is a coordinate whose true eigenstrain is exactly zero** (`CH-0240`). **A UNIFORM assignment does exist — on a raster nobody recommends**: a mutation relaxing the uniqueness check on `b₀` failed **nothing**, because the corpus owns no raster whose every helix carries `C-0136`'s residue `0`; constructed, `112 / 119` is one, and there the sign really is one free binary — so `C-0180`'s sweep is the right sweep for the wrong raster

| | |
|---|---|
| **Task** | [`T-284`](../tasks/T-284-turn-prestrain-sign.md) — what sets the sign of a raster turn's `8.57142857°` departure |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (a congruence on caDNAno's own `±5 bp` rule, exhaustible over the 59 turns and over all eight readings of the free conventions, with **no solver at all**) **+ in-silico** (the derived assignment graded through the same three-dimensional beam-and-bond lattice, the same exact Woodbury coupling surrogate and the same `C-0087`-measured incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream restricted per cell** that `C-0167` and `C-0180` used) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The departure's magnitude is `C-0152`'s rigid-duplex reading of caDNAno's own rule, not a measurement; `k_θ` at a scaffold turn is asserted equal to `k_θ` at a staple crossover because it is the same covalent object; `k_θ` itself is `Gen1Tile`'s **square-lattice-fitted** constant; the tie's axial station is `s = ±L/2` exactly, where a scaffold crossover sits 5 bp from a staple position. |
| **Verdict** | **PASS on all eleven predicates. `F1`–`F6`, `F9`, `F10` and `F11` did not fire; `F7` and `F8` were declared open and BOTH FIRED, and their firing is the second half of the finding.** `F11` was added **after** the mutation test, because a mutation that failed nothing said the corpus owns no raster that can tell two checks apart (§8). Deliverable **(a) is delivered in full**: the sign is derived, executable and tested, and 58 of the 59 binaries are fixed. Deliverable **(b) is NOT reachable on the graded family** and is reported as such rather than answered anyway. |
| **Provenance** | [`gpd/results/T-284-turn-prestrain-sign.json`](../results/T-284-turn-prestrain-sign.json) (`tile.RasterTurnPrestrainSignStudyKt`, **new**); model [`tile/RasterTurnPrestrainSign.kt`](../../src/main/kotlin/tile/RasterTurnPrestrainSign.kt) (**new file**, one function, one data class, one class). **NO SHARED SOURCE ON ANY NUMBER'S PATH IS EDITED** — `tile/HoneycombBondClassResidues.kt`, `tile/HoneycombRasterTurnTies.kt`, `tile/HoneycombGrillage.kt`, `tile/HoneycombTiedRegrade.kt`, `tile/ForcedCrossoverPrice.kt`, `structure/HoneycombRasterTurnSense.kt` and `coupling/NonUniformCoupling.kt` were **read, not edited**, so nothing `C-0154`, `C-0167`, `C-0175` or `C-0180` published can move and no consumer re-run is owed. The one shared source touched is `structure/ResultInputs.kt`, which gains a `T_284` handle because the tree's invariant is *every result path spelled in a main source has a handle* — and that edit is **provably inert**: `ResultInputs.all` is read only in `structure/ResultInputsTest.kt`. **Fourteen gate-named tests written first and watched fail** — [`tile/RasterTurnPrestrainSignTest.kt`](../../src/test/kotlin/tile/RasterTurnPrestrainSignTest.kt), which did not compile against a model that did not yet exist — of which **one failed on its first real run and found a real defect** (§7) — and **mutation-tested afterwards**, five mutations over the fourteen: swapping the departure's sign fails **1**, dropping the datum's handedness **1**, guessing `b₀` instead of requiring one **1**, reading caDNAno's `±5` as the exact half turn **3**, and inverting which rim a turn sits at **3**; the restored source passes **14 of 14**. **The third of those failed NOTHING on its first run, and that was the finding** (§8). Result file **byte-identical across two independent JVM runs** (and again across a second pair after `F11` was added). A full `./gradlew test` on the final sources gives **3 329 tests in 192 classes, 0 failures, 0 errors, 0 skipped**, with **no task excluded** — `testQueueVocabularyMutations` included. `check-result-file-hygiene.py` (`--prose`, `--departures`, `--saturated`), `check-kotlin-format-strings.py`, `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-queue-vocabulary.py`, `trace-answers.py` (both deliverables, **0 ABSENT**), `result-reader-census.py --check`, `T-278-emitter-rounding-census.py --check` and `P-31-harness-census.py --check` are all clean; `gpd/results/P-22-result-reader-census.json` is re-emitted, **additively** — three counts move and no edge is removed. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair per class every **21 bp**, scaffold crossovers at the staple position **± 5 bp**. Cross-section `10 × 6` (60 helices), block extent **116 bp = 39.44 nm** at `C-0151`'s `102 / 109` raster, `edgeY` = 38.04 nm. `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm, link penalty `1e4` pN/nm; **435 staple bonds and 59 raster turn ties** (`firstAxialSign = +1`, ties at `s = ±L/2`, 30 at the high rim and 29 at the low, 50 through-thickness and 9 in plane). `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Composite fractions **0.30** and **0.26** (`C-0116`, entering as `hingeStiffnessEnhancement` 21.1851817 and 18.4938242) plus the lattice's own **1.0**. Departure magnitude **8.57142857°** (`C-0152` §5), applied at **both** global phases. |
| **Consumes** | [`C-0152`](C-0152-forced-scaffold-crossover-price.md) (`T-246`) §5 — the `5.25 bp` half turn and the allowed departure; [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) — the closure condition and `HoneycombRasterResidues`; [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md) — the per-helix residue `{0, 10, 11}`; [`C-0151`](C-0151-closing-raster-selection.md) — the `102 / 109` raster, the 116 bp extent and the determined ladder phase; [`C-0175`](C-0175-drawable-raster-rim.md) (`T-254`) — the tie set, its census and its three swept free-tile readings, **reproduced**; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — the 64 coupled cells, the surrogate port, the stations, the distributions, the dropout stream and the zero-prestrain readings, **reproduced**; [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0154`](C-0154-honeycomb-grillage.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0103`](C-0103-path-count-at-fixed-geometry.md), [`C-0104`](C-0104-row-end-prestrain.md) |
| **Constrains** | **`T-284` is DONE as to (a) and reports (b) as unreachable.** `C-0180` §4's *"a sign no source in this repository fixes"* and `C-0175` §8's *"the sign of each turn's departure is fixed by no source in this repository, so it is swept"* are **superseded, not disputed**: 58 of the 59 binaries are fixed and the survivor is one global phase. `CH-0228` §5's second falsifier — *"a source that fixes the **sign** of each turn's departure such that the 59 cancel"* — is **half-met**: the source is caDNAno's own rule, the signs are fixed, and they do **not** cancel. **One challenge is raised**: [`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md), that a level displacement is **common-mode** and therefore has coefficient exactly zero on the relative-roll coordinate `CH-0228`, `C-0175` §8 and `C-0180` §4 load it through. **One task is opened**: `T-291`, to price the per-beam torsional eigenstrain that replaces it. |

---

## 1. The cheap bound is the whole of deliverable (a), and it needs no solver

`C-0152` §5 fixes the magnitude and, in the same sentence, says where the sign lives:

> caDNAno's `±5` is an integer approximation to **5.25**, so an ALLOWED scaffold crossover already
> carries `8.57142857°` — 0.25 bp off the exact half turn, **on either side**.

*On either side.* A crossover at `+5 bp` from its pair's staple position falls **0.25 bp short** of
the exact downstream half turn; one at `−5 bp` sits **0.25 bp past** the exact upstream one. Equal
magnitude, opposite sign — and which side a turn takes is not a convention, it is `C-0148`'s
closure condition, already modelled in this tree as `HoneycombRasterResidues.reducedResidues`.

| | `102 / 109` (`C-0151`, drawable) | `112 / 108` (`C-0140`, undrawable) | `112 / 119` (constructed, §8) |
|---|---|---|---|
| raster crossovers | 59 | 59 | 59 |
| distinct reduced residues `(level − 7·class) mod 21` | **`[0, 10]`** | `[0, 10, 11]` | `[0]` |
| `b₀` candidates | **`[5]` — unique** | **none** | `[5, 16]` — **two** |
| turns at `−5 bp`, i.e. `+8.57142857°` | **30** | — | — |
| turns at `+5 bp`, i.e. `−8.57142857°` | **29** | — | — |
| the assignment | **determined, and strictly alternating** | **not determined at all** | **uniform, and its sign genuinely free** |

Two distinct residues pin `b₀` **uniquely**, because they are `10` apart and `+10` and
`−10 ≡ 11` are *different* residues modulo 21 — so `{r, r + 10}` admits `b₀ = r + 5` and nothing
else. **The `2^59` sign assignments collapse to the one global phase**, for the price of one
modular reduction.

**And the undrawable raster has no answer at all.** `112 / 108` carries three residues and no
`b₀`, so its ten forced crossovers are not the only thing it costs: on that raster *no* turn's sign
is determined. `C-0151` selected `102 / 109` on closure, and this is a second consequence of the
same rule.

## 2. The alternation is a theorem about the row lengths, and it has a second derivation

`C-0136`'s per-helix residue says it one level up. A helix's length is the difference of its two
crossover levels, so `(L − 7Δ_eff) mod 21 ∈ {0, 10, 11}`: **`0` carries the sign THROUGH the helix
and `10` or `11` FLIP it.**

`102 / 109` reads **`[11]` at every one of its 58 interior helices**, so every helix flips and the
assignment is strictly alternating. The two constructions — `C-0148`'s on the crossovers and
`C-0136`'s on the lengths — are asserted to agree helix for helix, and they do.

*(Read on the undrawable raster the same quantity is `[0, 10]`, i.e. it would have been **partly**
uniform — which is a fact about that raster and not an answer, because it does not close.)*

## 3. Read on the block rather than on the path it is simpler still — and it survives every convention

An x-raster's turns alternate between the block's two axial rims, so the alternation has a
coordinate-free statement:

> **A turn at the block's HIGH axial rim sits 5 bp BELOW its pair's staple position and carries
> `+8.57142857°`; a turn at the low rim sits 5 bp above it and carries the negation.**

| `firstAxialSign` | `mirrored` | `axialReversed` | `b₀` | high-rim displacement | high-rim departure | alternating |
|---|---|---|---|---|---|---|
| `+1` | no | no | 5 | `−5 bp` | **`+8.57142857°`** | yes |
| `+1` | no | yes | 16 | `+5 bp` | **`+8.57142857°`** | yes |
| `+1` | yes | no | 19 | `−5 bp` | **`+8.57142857°`** | yes |
| `+1` | yes | yes | 2 | `+5 bp` | **`+8.57142857°`** | yes |
| `−1` | no | no | 16 | `−5 bp` | **`+8.57142857°`** | yes |
| `−1` | no | yes | 5 | `+5 bp` | **`+8.57142857°`** | yes |
| `−1` | yes | no | 9 | `−5 bp` | **`+8.57142857°`** | yes |
| `−1` | yes | yes | 12 | `+5 bp` | **`+8.57142857°`** | yes |

`b₀` moves with the datum, the *displacement's label* moves with an axial reversal — and the
**departure** does not, at any of the eight, because the datum's handedness travels with the level.
That last clause is not decoration: it is what the first real run of the test suite found (§7).

## 4. The reproductions bind the derivation to the corpus's own sweep

`C-0175` §8 swept three sign assignments on the free tile *because the sign was unknown*. One of
the three is the derived one, and this study reproduces it:

| | published (`C-0175` §8) | here, phase `+1` | relative departure |
|---|---|---|---|
| free tile, `f = 0.30`, *"by the rim the turn sits at"* | **0.0457993778** | **0.0457993778** | **`8.9e−10`** |
| free tile, `f = 0.26` | 0.0480631403 | 0.0480631403 | `1.5e−9` |
| free tile, no enhancement | 0.135080506 | 0.135080506 | `2.0e−9` |
| `C-0180`'s recovered cell A, zero prestrain | 0.0995744767 | 0.0995744767 | `2.9e−10` |
| `C-0180`'s recovered cell B, zero prestrain | 0.0998791032 | 0.0998791032 | `3.5e−10` |

**Five reproductions, worst `2.0e−9`.** So the tie machinery here *is* the tie machinery there, and
`C-0175`'s *"by the rim"* row was right by accident — it is the third of three guesses and the only
one the lattice makes.

The free tile at the other phase is a number nobody had: **0.0462879851** at `f = 0.30`, against a
uniform assignment's 0.0460995878 and the derived phase `+1`'s 0.0457993778. Every one of the nine
free-tile readings is inside `T-5b` at the two coupled enhancements and outside it at the lattice's
own `1.0` lower bound, at **every** phase — so no free-tile verdict depends on the phase, which is
exactly why the free tile could not have settled anything.

## 5. The grade — and deliverable (b) is NOT reachable, which is said plainly

Graded on `C-0167`'s 64 cells, the same four placements, the same two distributions, the same two
composite fractions, the same 4 000-realisation common stream:

| | |
|---|---|
| cells flat at `T-5b`'s 0.10 at the 90th percentile, **phase `+1`** | **1 of 64** |
| the same, **phase `−1`** | **1 of 64** |
| cells flat at **BOTH** phases | **0 of 64** |
| cells whose verdict **depends on** the phase | **2 of 64** |
| `C-0180`'s uniform reading, at either of its two signs | 1 of 64, a different one at each |

The two cells are `C-0180`'s own two:

| cell | zero prestrain | **derived, phase `+1`** | **derived, phase `−1`** |
|---|---|---|---|
| abstract grid, `3 × 10` = 30 paths, rim-graded, `f = 0.30` | **0.0995744767** — flat | **0.0992065942** — flat | 0.100191449 — **not flat** |
| abstract grid on the rooting helices, `5 × 10` = 50 paths, rim-graded, `f = 0.30` | **0.0998791032** — flat | 0.100244866 — **not flat** | **0.0997581964** — flat |

**The structure `C-0180` found at a uniform sign survives at the derived one: exactly one of the
two is flat at each phase, and it is a different one.** The other **62 of 64** carry the same
verdict at both phases — *not flat* — which is phase-independent and is the wrong verdict. So no
`(placement, distribution, column count, composite fraction)` cell of the graded family is **flat**
at both phases, and **deliverable (b) is not reachable on it**. That is reported rather than worked
around.

What *is* delivered instead of (b) is a **rule that needs no phase**, with its census:

> A coupled cell is flat at both phases whenever its **zero-prestrain** `p90` is below
> `0.10 − max|movement|`.

Over all **128** prestrained cells the worst `|movement from the zero-prestrain cell|` is
**`0.00203756217`** of the stroke and the median **`0.000581022203`**
*(a max and a median over this study's own 128 `cells[*].movementFromZeroPrestrain` records;
the threshold and the shortfall below are one subtraction each from emitted numbers
and are not themselves emitted)*,
so the sufficient condition is a zero-prestrain `p90` below **`0.0979624378`** —
and the tightest cell the corpus owns is `0.0995744767`.
**0 of 64 qualify, and the shortfall is `0.0995744767 − 0.0979624378 = 0.0016120389`,
1.612 % of the tolerance.**
That is the number a distribution search would have to beat, and it is `T-291`'s second half.

## 6. Convergence, taken at the cells the verdict rests on

| cell | axis | departure | verdict survives |
|---|---|---|---|
| abstract grid, 30 paths, rim-graded, `f = 0.30`, phase `+1` | subdivisions 1 → 2 | **`9.6e−5`** | yes — 0.0992065942 → 0.0993023023, margin `7.9e−4`, a factor of **8.3** |
| the same, phase `+1` | sample grid 81 → 161 | `2.9e−5` | yes |
| abstract grid, 30 paths, rim-graded, `f = 0.30`, phase `−1` | subdivisions 1 → 2 | **`2.1e−4`** | yes — 0.100191449 → 0.100403783, i.e. the refinement moves it **further** from flat |
| the same, phase `−1` | sample grid 81 → 161 | `3.6e−5` | yes |
| rooting helices, 50 paths, rim-graded, `f = 0.30`, phase `+1` | subdivisions 1 → 2 | `2.0e−5` | yes |
| the same, phase `+1` | sample grid 81 → 161 | **`0.0`** | yes |
| rooting helices, 50 paths, rim-graded, `f = 0.30`, phase `−1` | subdivisions 1 → 2 | `6.8e−6` | yes — margin `2.4e−4`, a factor of **36** |
| the same, phase `−1` | sample grid 81 → 161 | **`0.0`** | yes |

**0 of 8 steps move a verdict.** The one row worth naming is the third: its departure `2.1e−4` is
larger than the `1.9e−4` by which that cell exceeds the tolerance, and the verdict survives only
because the refinement moves it in the **adverse** direction. A *"not flat"* that gets less flat
under refinement is robust; the same arithmetic the other way round would not have been, and it is
quoted here rather than left to the ratio.

## 7. The five verification gates, and what the failing test found

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | the departure asserted to be `C-0152`'s allowed magnitude at every one of the 59 turns; every derived displacement asserted to be exactly `±` caDNAno's own `SCAFFOLD_OFFSET_BP`; the tie census asserted at **30** high-rim and **29** low-rim, **9** in plane and 50 through the thickness, which is `C-0175`'s own split |
| **2 — limiting cases** | a raster that does **not** close asserted to **refuse** rather than guess — `112 / 108` throws on `classZeroResidue` and on `signs`; a zero departure asserted to return the pure-stiffness tie list **itself**; a `phase` outside `{+1, −1}` refused |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the tied, zero-prestrain lattice dishes exactly `0.0`** with 59 rim ties present — and it is **not** asserted on a prestrained lattice, because a uniform eigenstrain relaxes into a cylinder (`CLAUDE.md`) and the derived assignment is not uniform anyway; the free field asserted **exactly linear** in the assignment vector, `field(a) + field(−a) = 2·field(0)` to `1e−9`; the partition by axial rim asserted invariant over all **eight** readings of the free conventions; every turn asserted to be a pair the **bond graph** actually bonds, indexed the same way in the residue walk and in the tie list |
| **4 — numerical convergence** | beam subdivisions 1 → 2 and the dishing sample grid 81 → 161, taken on the **`p90` of each deciding cell at each phase**; the result file **byte-identical across two independent JVM runs**; the shared influence bank asserted equal to a surrogate built the long way (`F10`) |
| **5 — literature and upstream** | the `±5 bp` rule quoted from the primary source (Douglas et al., *NAR* **37**:5001, `PMC2731887`, in `gpd/data/T-151-sources/`, **read directly**) and consumed through `C-0148`'s model rather than re-implemented; **five reproductions, worst `2.0e−9`** |

### The failing test, and it found a real defect

`F3`'s second test — *"reversing the axial datum alone inverts it, which an improper flip must"* —
failed on its first real run. `scaffoldDisplacementDepartureDegrees` is written at
`AZIMUTH_PER_BASE_PAIR = +240/7°` **per base pair of increasing `z`**, and the first draft applied
it to the displacement without asking whose `z`. Reversing the axial datum reverses the handedness
a residue is read with, so **both** the displacement and the constant flip and the departure is
invariant; leaving the constant behind reported the departure as inverted under a datum change,
which is `CLAUDE.md`'s own *a residue map is a handedness, so it must be reversed whenever `z` is*
met from the other side. The repair is one `datumSign`, and without the test the emitted assignment
would have carried a datum's sign as if it were the lattice's.

### The ten declared falsifiers

| # | falsifier | fired | outcome |
|---|---|---|---|
| `F1` | the 59 crossovers take exactly two reduced residues, ten apart, so `b₀` is unique | **no** | `[0, 10]`, `b₀ = 5` |
| `F2` | the derived displacement is **not** constant | **no** | strictly alternating, 30 / 29, per-helix residue `[11]` at every interior helix |
| `F3` | the partition by axial rim is the same at every free convention | **no** | eight readings, `+8.57142857°` at the high rim at all eight |
| `F4` | the derived assignment reproduces `C-0175` §8's free-tile reading | **no** | three enhancements, worst `2.0e−9` |
| `F5` | the free field is exactly linear in the assignment vector | **no** | below the `1e−9` identity tolerance |
| `F6` | a uniform pressure on the tied, zero-prestrain lattice dishes exactly zero | **no** | `0.0` with 59 rim ties |
| `F7` | **declared OPEN** — `C-0180`'s two recovered cells are flat at **both** phases | **FIRED** | 0 of 2; cell A `0.0992065942` / `0.100191449`, cell B `0.100244866` / `0.0997581964` |
| `F8` | **declared OPEN** — the flat census over all 64 cells is the same at both phases | **FIRED** | **2 of 64** carry a phase-dependent verdict |
| `F9` | the undrawable `112 / 108` raster determines no assignment at all | **no** | three residues, no `b₀` |
| `F10` | the shared influence bank equals a surrogate built the long way | **no** | below `1e−9`; the prestrain is a **load**, so one bank serves both phases |
| `F11` | a raster carrying **one** reduced residue leaves **two** `b₀` candidates, and the class refuses rather than guessing | **no** | `112 / 119`: residues `[0]`, candidates `[5, 16]`, per-helix `[0]` — the state **no raster this repository owns is in**, and it was constructed rather than found (§8) |

`F7` and `F8` are the ones that decide whether this task delivers (b) as well as (a). They fired.

## 8. The mutation that failed nothing, and the state it made us construct

Five mutations were applied to the model after the tests were green. Four killed named tests at
once. The fifth — relaxing `classZeroResidue`'s `check(candidates.size == 1)` to
`check(candidates.isNotEmpty())`, i.e. **guessing `b₀` rather than requiring one** — failed
**nothing**, and that is a measurement of the **corpus** rather than of the test list:

> `102 / 109` has exactly **one** candidate and `112 / 108` has **none**, so both refuse either
> way. The two rasters this repository owns cannot distinguish the two checks.

The state that can is a raster carrying **one** distinct reduced residue, which `b₀ = r + 5` and
`b₀ = r − 5` both admit. By §2 that is a raster whose every helix carries `C-0136`'s residue **0** —
the residue that carries the sign **through** — and it is one modular condition away:
`112 ≡ 7` and `119 ≡ 14 (mod 21)`, which are `7Δ` at the two effective senses the `10 × 6` block
puts on its helices. `112 / 119` **closes**, carries residues `[0]`, admits `b₀ ∈ {5, 16}`, and the
class refuses. `F11` is that test, and it kills the mutation.

The finding is not only about the tests. **A uniform assignment does exist — on a raster this
programme does not recommend**, and there the sign really is *one free binary*. So `C-0180`'s
uniform sweep is the right sweep for the wrong raster, and the reason nobody noticed is that the
corpus owns no raster of that kind. `CLAUDE.md`'s own *a mutation that fails nothing is the
finding, not a gap in the test list* — reached, as it records, by **constructing** the state rather
than by asserting the existing designs harder.

## 9. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- The derivation is about caDNAno's **default** rule. A design that **forces** a crossover puts it
  where the rule does not allow, and no residue then says which side it is on; the recommended
  raster forces none, and the undrawable one determines nothing.
- The magnitude is `C-0152`'s **rigid-duplex** reading and is a **ceiling**: nothing here bounds
  from below how much of the 0.25 bp is taken up in backbone strain or a local unstacking rather
  than in a roll.
- **The map from a derived departure to the model's RELATIVE roll is not derivable at all**,
  because the departure is common-mode. That is `CH-0240`, and it is why both phases are graded and
  the exposure is bounded rather than a phase being chosen.
- `k_θ` at a scaffold turn is asserted equal to `k_θ` at a staple crossover, and `k_θ` itself is
  `Gen1Tile`'s square-lattice-fitted constant (`C-0175`, `C-0180`, inherited verbatim).
- The tie sits at `s = ±L/2` **exactly**; a scaffold crossover sits 5 bp from a staple position, so
  its true axial station is within **1.7 nm** of the rim node, and nothing here prices that.
- The lattice carries **no** across-helix parallel-axis term, so its `D_⊥` is the independent one
  and a lower bound; Kirchhoff is not safe at these thicknesses, so every `D_∥` is an upper bound.
- The dropout statistics are measured on a **single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers or its ties. A missing scaffold turn is not in this model at all.
- **Nothing here re-opens the placement search, the distribution rule, the raster or the
  cross-section.** The stations are `C-0151`'s and the distributions `C-0058`'s two.

## 10. Open questions

- **What the common-mode departure is actually worth**, which needs a per-beam torsional
  eigenstrain this tree does not carry. The alternation puts opposite roll demands at a helix's two
  ends, i.e. **`17.1428571°` of demanded twist over its own row** — which is, to the digit, the
  departure `C-0152` prices a **forced** crossover at, reached from the other side. That is `T-291`.
- **Whether a distribution *searched* on the tied lattice reaches `0.0979624378`**, which is what a
  phase-independent flat cell needs. Every distribution graded here is a rule written on a smeared
  model's geometry, and both candidate cells are rim-graded.
- **What the tie's true axial station is worth**, 5 bp from the rim node, against margins of
  `2.4e−4` to `7.9e−4` of the stroke.
- ~~**Whether any other closing two-length pair puts a per-helix residue of `0` somewhere** — a
  raster whose sign assignment is partly *uniform*.~~ **ANSWERED here, in §8**: `112 / 119` is
  *entirely* at residue 0 and its assignment is uniform. What is still open is the **census** — how
  many of the 441 residue pairs close *and* are uniform, and whether any of them is otherwise
  buildable. It is one modular pass and it is `T-291`'s cheapest half.
