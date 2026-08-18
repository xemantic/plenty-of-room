# C-0102 — **The three demands on the one phase variable are IRRECONCILABLE at every width, and the one to drop is the inventory.** `C-0090`'s collapse does not close `C-0098`'s question, it sharpens it: at the buildable 38.08 nm the three sets go from **10 / 10 / 2** to **2 / 2 / 2** and stay **disjoint** — richest `{0, 16}`, eight-column and centro-symmetric `{8, 24}`. **Phase 8 is recommended**, at **0.0658484805** of the free stroke against the richest phase's **0.125068659** on the same descent. And the sheet-side price nobody had computed is **larger than the smeared reading says and exact**: a seven-column host splits its columns 4/3, so the **series** `D_⊥` loses `6/7` where the smeared one loses `7/8` — `48/49` apart — and under `C-0087`'s measured incorporation its chance of losing **every** crossover on some interface is **3.58698588×** an eight-column sheet's. The inventory demand buys **1.056–1.119×** on the two published redundancy slopes and the host it demands costs **1.899×**

| | |
|---|---|
| **Task** | [`T-171`](../tasks/T-171.md), raised by [`C-0098`](C-0098-shared-body-placement-and-distribution.md)'s *Still open* item 3 and [`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md)'s *How to settle it* item 3 |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** for the anchoring scheme the array belongs to |
| **Verification type** | **logical** (a complete census over a single quantised integer at three width/convention readings — 96 rows, no solve — plus **nine closed forms**: both `D_⊥` readings, the anisotropy, `C-0054`'s spendable budget and three severance probabilities) **+ in-silico** (`C-0009`'s grillage, `C-0058`'s exact Woodbury bank and `C-0063`'s descent re-run as libraries at the four phases the census singles out, under `C-0022`'s **solved** load; 87 719 descent evaluations and 4 thermal-fluctuation solves) |
| **Verdict** | **PASS on all five predicates, and the answer is that the three demands cannot be reconciled — at either width.** **The cheap bound is the whole comparison and it costs no solve.** The phase is one integer with 32 values and each demand is a census over it: at §3's 40.00 nm the sets are `C-0098`'s **10 / 10 / 2** with the richest disjoint from the other two; at `C-0086`'s buildable **38.08 nm** every one of them collapses to **two** — richest **`{0, 16}`**, eight-column **`{8, 24}`**, centro-symmetric **`{8, 24}`** — and **the disjointness survives**. `C-0090` narrowed the question and did not close it. **The sheet-side price of a seven-column host is computed here for the first time, in fourteen channels of which nine are closed form.** The two `D_⊥` readings **must** disagree, and by an exact rational: a seven-column sheet splits its columns **4/3** between the two parities, so seven of its fourteen interfaces carry **three** crossovers, the **smeared** rigidity loses `49/56 = 7/8 = 0.875` and the **series** one — a harmonic mean, which is how a sheet actually bends across its interfaces — loses `42/49 = 6/7 = 0.857142857`. Under `C-0087`'s **measured** incorporation the probability that *some* interface loses **every** crossover, which takes the series `D_⊥` to **exactly zero**, is **0.0327709113** against **0.00913605807** at the mean 84 % (**3.58698588×**) and **0.796522535** against **0.654574539** at the 48 % `C-0087` measures at the tile edge; `C-0054`'s spendable hinge budget falls 42 → **35**, so its **75 %** is an eight-column number and the seven-column one is **71.4 %**. **The recommendation is phase 8 and the demand to drop is the inventory**, and the arithmetic is one division: 52 → 60 ties buys **1.11878381×** on `C-0093`'s redundancy slope and **1.05539602×** on `C-0098`'s measured one, while the phase it demands costs **1.89934009×** in flatness at **matched descent effort** — and `C-0098`'s own 10 000-realisation dropout grading at 40 nm already measured **1.26510653×** the same way. **Six falsifiers declared, none fired**, and the sixth is the finding: `C-0090`'s *"the row-end crossover can never be an upward site, at any phase"* is false at exactly the two phases that carry the richest inventory, where admitting it is worth **fifteen** stations and **zero** columns — [`CH-0118`](../challenges/CH-0118-the-row-end-crossover-is-a-station-at-two-phases.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, AND THE MOTIF IS NOT DEMONSTRATED.** `C-0055`'s free lever on one upward crossover is unchanged and upstream of every flatness number; the only measured input is `C-0087`'s incorporation map, which is Strauss et al. (2018) read on a plain Rothemund rectangle and transferred to a motif nobody has mapped |
| **Provenance** | `gpd/results/T-171-crossover-phase-selection.json`, produced by `anchoring.CrossoverPhaseSelectionStudyKt`; model in `src/main/kotlin/anchoring/CrossoverPhaseSelection.kt` (**new file** — `BuildableRasterWidth.kt`, `UpwardRootPlacement.kt`, `UnusedJunctionSite.kt`, `ConsumedCrossoverSheet.kt`, `OrigamiGrillage.kt` and `Gen1Tile.kt` were **read, not edited**); **96 census rows, 3 demand ledgers, 14 sheet-price channels, 4 graded phases, 3 tie-count divisions, 1 recommendation, 5 convergence records, 13 upstream reproductions, 5 predicates, 6 falsifiers, 7 findings**; **15 gate-named tests in `src/test/kotlin/anchoring/CrossoverPhaseSelectionTest.kt`**; the study is **39 s** of compute and carries **no wall-clock time in its JSON**, so a re-run diff is a statement about the answer; the result file was **produced three times** — run A differed from B in **one field**, the Woodbury departure's ninth digit, which is `P-18`'s dimensionless-departure class; runs B and C, with departures rounded to two significant digits, are **byte-for-byte identical**; `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 30 s — the whole suite, on its own isolated tree, with NOTHING dropped**; `tools/result-reader-census.py --emit` re-run and its 45 self-checks clean (it reports one sibling study added *after* the emit, which is a note and not a failure); `tools/check-markdown-tables.py` clean over 318 files |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes at the SAXS 2.69 nm** (40.35 nm across the helices, **unchanged**), 0.34 nm rise, 32/3 bp per turn, 16 bp column pitch; along-helix width **40.00 nm** (§3) and **38.08 nm** (`C-0086`'s 112 bp); `C-0055`'s **34** upward roots on `C-0039`'s arm quantised to `C-0085`'s **24 rises = 8.16 nm**; `C-0017`'s **33.3333333 pN/nm** mandate as a **SUM**, 34 equal springs; `C-0022`'s **solved** edge profile at **2 mM, a 10 nm gap and 0.192 V**, carried unchanged to 38.08 nm exactly as `C-0090` carries it and measured by `C-0100` to be worth **0.0712 %**; `C-0001`'s foundation secant; free strokes **4.90731102 nm** (40.00) and **5.15473846 nm** (38.08); dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; the end-of-row crossover **admitted**, which is the programme's carried reading (`C-0095`, `C-0099`) |
| **Consumes** | [`C-0098`](C-0098-shared-body-placement-and-distribution.md)/[`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md) (the conflict, the 60-site census **reproduced**, the two redundancy slopes and the 40 nm graded comparison, **CITED**), [`C-0090`](C-0090-buildable-raster-width.md) (the buildable width, `rasterColumnLayout`/`rasterUpwardSites`/`rasterSiteInventory`/`rasterJunctionPlanes`, its published optimum and placement key **read from its result file as the gate**), [`C-0063`](C-0063-upward-root-placement.md) (`upwardRootLattice`, `centroSymmetricUpwardPhases`, `descendPlacement`, `UpwardRootInfluenceBank`, and its published optimum **read from its result file as the gate**), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, the upward azimuth, the 34, `rasterJunctionSites`), [`C-0054`](C-0054-consumed-crossover-sheet.md) (`uniformCurvatureRigidity`, `uniformMomentRigidity`, the connectivity theorem — **re-run as libraries**), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the measured incorporation map, **CITED**), [`C-0093`](C-0093-shared-body-coupling.md) (the abstract-grid redundancy slope, **CITED**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the 32-phase period, the 56/49 inventory, the per-load-path lever), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage and its thermal fluctuation), [`C-0010`](C-0010-tile-positional-variance.md) (the variance channel), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0100`](C-0100-collar-at-the-buildable-width.md) (that carrying it is worth 0.0712 %), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0086`](C-0086-seamless-scaffold-routing.md), [`C-0085`](C-0085-collinear-stacking-clearance.md), [`C-0017`](C-0017-output-coupling-stiffness.md), `Gen1Tile` |
| **Raises** | [`CH-0118`](../challenges/CH-0118-the-row-end-crossover-is-a-station-at-two-phases.md), against `C-0090`'s Deliverable 3 quantifier |

---

## The claim, in one line

**Three claims want three different values of one integer; the census that shows they cannot all
have it costs no solve at all, and the sheet-side price that decides which of them loses is nine
closed forms and a division.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, plate rigidity **pN·nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward** — away
  from the grafted layer, which lies below the tile; `w` positive **downward**; origin at the tile
  centre.
- **The phase is ONE integer in `[0, 32)`.** Crossover planes at `x = 0.34 φ + 2.72 k` nm; the
  sheet's own **columns** are the planes with `k` **even**; row `r`'s **upward** (`EAST`) sites are
  the planes with `k ≡ 2r + 3 (mod 4)`. The period is **32 and not 16** (`C-0015`), asserted here
  on the census rather than on the column lattice.
- **Dishing** is the peak absolute departure from the area-weighted best-fit **plane** on an
  **81 × 81** grid, over the free-tile stroke. **Flat** means below **0.10** of it — `T-5b`'s
  **convention**.
- **The operating state is named**, for the eleventh time in this programme: `C-0022`'s **solved
  2 mM / 10 nm / 0.192 V** profile.
- **`D_⊥` is quoted in both of `C-0054`'s readings**, never one: the **smeared** (uniform-curvature)
  one is linear in the retained count and is what a continuum plate can express; the **series**
  (uniform-moment) one is a harmonic mean over the fourteen interfaces and is what a sheet bending
  across its helices does.

---

## Deliverable 1 — the census, which is the whole comparison and costs no solve

`crossoverPhaseCensus` runs `C-0055`'s own construction at every one of the 32 phases, at three
width/convention readings. **96 rows, no solve, no search.**

| reading | richest upward inventory | most columns | centro-symmetric | all three |
|---|---|---|---|---|
| **40.00 nm, interior** (`C-0015`/`C-0055`/`C-0098`) | **60**: 0, 1, 2, 14, 15, 16, 17, 18, 30, 31 | **8**: 6, 7, 8, 9, 10, 22, 23, 24, 25, 26 | 8, 24 | **∅** |
| **38.08 nm, row-end admitted** (the carried reading) | **60**: **0, 16** | **8**: **8, 24** | **8, 24** | **∅** |
| 38.08 nm, row-end refused (`C-0090`'s bracket) | 53: 17…31 (15 phases) | 7: 30 phases | 8, 24 | **∅** |

- **The 40.00 nm row is `C-0098`'s, reproduced exactly** — 52/53/60, the ten richest, the ten
  eight-column and the two centro-symmetric phases, all at departure **0**.
- **`C-0090`'s collapse sharpens the conflict rather than closing it.** Ten against ten becomes
  **two against two**, and the two sets are still disjoint. Item 3 of `C-0098`'s *Still open* list
  is therefore a real question at the width the programme has adopted, not an artefact of the
  nominal one.
- **The third row is why the convention matters.** With the row end refused there is no
  eight-column phase at all and the richest set moves to fifteen seven-column phases at 53 — a
  *different* conflict. Both readings are censused; the programme carries the admitted one.

### The congruence behind the collapse, and the one `C-0090` missed

`38.08 = 112 bp = 7 × 16 bp` exactly, so a **plane** lands on the row end when
`φ ≡ −56 ≡ 0 (mod 8)` — phases **0, 8, 16, 24** — and that plane is a **column** only when the same
congruence holds **modulo 16** — phases **8, 24**. The two conditions are not the same condition,
and the difference is worth an entire demand set:

| phase | row-end **columns** admitted | row-end **upward stations** admitted | upward inventory, refused → admitted |
|---|---|---|---|
| **0** | **0** | **+15** | **45 → 60** |
| 8 | +2 | 0 | 52 → 52 |
| **16** | **0** | **+15** | **45 → 60** |
| 24 | +2 | 0 | 53 → 53 |

**The entire richest set at the buildable width exists only under the end-of-row convention**, and
`C-0090`'s prose says that convention cannot produce a station. That is
[`CH-0118`](../challenges/CH-0118-the-row-end-crossover-is-a-station-at-two-phases.md). Fifteen and
not sixteen because the two end planes serve complementary row parities.

---

## Deliverable 2 — the sheet-side price of a seven-column host

Nobody had computed this. Fourteen channels, **nine of them closed form**, at the buildable width:

| channel | owner | 7 columns | 8 columns | 7/8 | closed form |
|---|---|---|---|---|---|
| interface crossovers | `C-0015` | 49 | 56 | 0.875 | **yes** |
| minimum crossovers on any one interface | `C-0054` | **3** | 4 | 0.75 | **yes** |
| `D_⊥`, **smeared** (uniform curvature) [pN·nm] | `C-0054`/`C-0009` | 3.12204441 | 3.56805075 | **0.875** | **yes** |
| `D_⊥`, **series** (uniform moment) [pN·nm] | `C-0054` | 3.51083711 | 4.09597663 | **0.857142857** | **yes** |
| bending anisotropy `D_∥/D_⊥`, series | `C-0009` | 24.3536957 | 20.8745964 | 1.16666667 | **yes** |
| spendable crossovers under the connectivity theorem | `C-0054` | **35** | 42 | 0.833333333 | **yes** |
| `P`(some interface loses every crossover), `p` = 0.48 | `C-0087`/`C-0054` | **0.796522535** | 0.654574539 | 1.21685536 | **yes** |
| `P`(same), `p` = 0.84 — `C-0087`'s **mean** | `C-0087`/`C-0054` | **0.0327709113** | **0.00913605807** | **3.58698588** | **yes** |
| `P`(same), `p` = 0.95 | `C-0087`/`C-0054` | 0.000918382857 | 8.74964454e−05 | **10.4962305** | **yes** |
| free-tile peak crossover force under the solved load [pN] | `C-0016`/`C-0009` | 0.267659924 | 0.254959195 | 1.04981475 | no |
| free-tile peak duplex shear under the same load [pN] | `C-0016`/`C-0009` | 0.182415337 | 0.176884264 | 1.03126945 | no |
| thermal **dishing** RMS of the free host [nm] | `C-0010` | 1.28744031 | 1.16875253 | 1.10155082 | no |
| thermal **centre** RMS of the free host [nm] | `C-0010` | 1.09246446 | 1.0690893 | 1.02186455 | no |
| best 34-root dishing, **matched** descent effort | `C-0063`/`C-0090` | 0.125068659 | **0.0658484805** | **1.89934009** | no |

**Three readings, and two of them are new.**

1. **The two `D_⊥` readings MUST disagree on a seven-column sheet, and the disagreement is an exact
   rational.** Seven columns split **4/3** between the two parities, so seven of the fourteen
   interfaces carry three crossovers and seven carry four. The smeared rigidity is linear in the
   count, `49/56 = 7/8`; the series one is `L_y k_θ/(L_x Σ 1/n_i)`, and
   `Σ 1/n_i = 14/4 → 7/4 + 7/3`, so it loses `42/49 = 6/7`. **The two are `48/49` apart** and the
   smeared one **understates** the loss, because a sheet bends across its interfaces in series and
   the thin ones dominate. The anisotropy `C-0015`'s *"shapes, not counts"* rule is written on
   rises **16.7 %** on the series reading, 20.8745964 → 24.3536957 — and `C-0009`'s continuum
   **25.56** is a third convention again, quoted here so the three are not confused.
2. **The thin interfaces are where folding fails, and that channel is 3.58698588×.** `C-0054`'s theorem
   is that a connected sheet needs one retained crossover on each of its `D − 1` interfaces, and
   an empty interface takes the **series** `D_⊥` to **exactly zero**. So the quantity to compute is
   not a rigidity but `P = 1 − Π_i (1 − (1−p)^{n_i})`, and on the three-crossover interfaces the
   exponent is 3 rather than 4. At `C-0087`'s **mean** 84 % the seven-column sheet is **3.58698588×**
   more likely to have no across-helix rigidity at all; at the 48 % `C-0087` measures at the tile
   **edge**, where the outermost interfaces are, both readings are near certainties (0.796522535 against
   0.654574539) and the *ratio* collapses — which is the honest way round: the severance channel
   discriminates where folding is good and stops discriminating where it is bad.
3. **`C-0015`'s *"seven columns is the better layout"* is a statement about a point load, and the
   sign is a property of the load path.** Under `C-0022`'s **distributed solved** load with **no
   coupling** — the clean host comparison, with no placement in it — the seven-column host is
   **4.981 % worse** in peak crossover force and **3.127 % worse** in peak duplex shear. Under the
   34-root coupling the comparison reverses at some phases (phase 0 reads 1.4599317 pN against
   phase 8's 1.53739509), but that reading **confounds the host with the placement** and is
   reported as such rather than quoted.

**And the thermal channel moves the least.** `C-0010`'s dishing RMS is **10.155 %** worse on the
seven-column host and the *centre* RMS — the quietest point on the tile, quoted with its point —
only **2.186 %**. A phase is not a variance decision.

---

## Deliverable 3 — the four candidate phases, measured

The descent is `C-0063`'s, run from **`C-0090`'s own four starts** with the same sweeps at every
phase, because a comparison between an exhaustive enumeration and a descent is not a comparison.

| phase | role | cols | upward sites | free tile | **best descent** | flat? | evaluations |
|---|---|---|---|---|---|---|---|
| 0 | richest upward inventory | 7 | **60** | 0.299397543 | **0.137716682** | **no** | 18 694 |
| **8** | **eight-column, centro-symmetric** | **8** | 52 | 0.299034765 | **0.0658484805** | **YES** | 18 760 |
| 16 | richest upward inventory | 7 | **60** | 0.299397543 | **0.125068659** | **no** | 33 852 |
| 24 | eight-column, centro-symmetric | 8 | 53 | 0.299034733 | 0.0777862581 | YES | 16 413 |

**Both richest phases are outside `T-5b`'s convention and both eight-column phases are inside it**,
at matched search effort, and the eight-column phases have the better number even though the
richest ones were given **1.8× more** descent evaluations at phase 16. The exhaustive
centro-symmetric family — which exists **only** at 8 and 24 and which `C-0090` shows beats the
descent there — takes phase 8 further, to **0.0621469105**, reproduced here from its own key.

---

## Deliverable 4 — the division that decides which demand to drop

| statement | slope | ties | **the inventory buys** | **the phase costs** | worth it? |
|---|---|---|---|---|---|
| `C-0093`'s redundancy slope, abstract grids | −0.784357442 | 52 → 60 | **1.11878381×** | 1.89934009× | **NO** |
| `C-0098`'s slope, measured on the **real** lattice | −0.376769756 | 52 → 60 | **1.05539602×** | 1.89934009× | **NO** |
| `C-0098`'s own **measured** 40 nm grading, 10 000 dropout realisations | — | 53 → 60 | 0.790447269× (i.e. it **loses**) | **1.26510653×** | **NO** |

> **The inventory demand is worth at most 1.12× on the most generous slope in the corpus and the
> host it demands costs 1.90×. It loses by a factor of 1.8** — and `C-0098` had already measured it
> losing by 1.27× on the other axis, under dropout, at the other width.

The two axes are different objectives (a zero-defect placement optimum and a 90th-percentile
dishing under measured dropout) and they are **not** added; each is quoted against its own baseline
and both point the same way, which is the whole of why this is decidable.

---

## Deliverable 5 — the recommendation

> **Phase 8, at `C-0086`'s buildable 38.08 nm, with the row-end crossover admitted.**
> Eight columns, 56 interface crossovers, centro-symmetric, 52 upward stations, and
> **0.0658484805** of the free stroke on the matched descent — **0.0621469105** on `C-0090`'s
> exhaustive centro-symmetric enumeration, inside `T-5b`'s 0.10.
>
> **The demand dropped is the richest upward inventory** — 52 stations against 60, a **13.3 %**
> smaller tie inventory.

**Why that one and not another.** The inventory demand is

- the only one of the three whose owner has **already measured it losing** (`C-0098`: 0.487309625
  against 0.385192562, with seven more ties);
- the only one that is **not also a structural property of the host** — the other two buy an
  eight-column sheet, which is 12.5 % of the smeared and 14.3 % of the series `D_⊥`, 7 of
  `C-0054`'s 42 spendable crossovers, and a 3.58698588× severance probability;
- the only one whose loss is **bounded by a published slope**, so the cost of dropping it is a
  number (1.06–1.12×) and not a judgement.

Dropping `C-0015`'s demand costs the sheet; dropping `C-0063`'s costs the **exhaustive
centro-symmetric family**, which is the only search in this programme that has ever beaten a
descent on this lattice. Neither is priced at 1.12×.

---

## The five verification gates

Executed as **15 gate-named tests** in `src/test/kotlin/anchoring/CrossoverPhaseSelectionTest.kt`,
plus two in-study `check`s that abort the run.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a census is one row per phase and every count is a count; the per-interface counts are a **partition** of the interface total at every phase; every upward station of every phase lies inside its own footprint at both widths; unphysical arguments throw at **six** entry points, including a severance probability outside `[0, 1]`, a negative crossover count and a malformed placement key | **PASS** |
| **2 — limiting cases** | the 40.00 nm census reproduces `C-0098`'s published table **exactly** — 52/53/60, the ten richest, `C-0015`'s ten eight-column phases with 56 and 49 interface crossovers, `C-0063`'s `{8, 24}`; the demand ledger's disjointness; a seven-column sheet splits **7 × 4 + 7 × 3** and an eight-column one **14 × 4**; `uniformMomentRigidity` is exactly `(D/(D−1))²` times `uniformCurvatureRigidity` on a uniform lattice; an **empty** interface annihilates the series rigidity and not the smeared one; a severance probability is 0 at `p = 1`, 1 at `p = 0`, exactly `(1−p)³` on one three-crossover interface, and monotone in the redundancy | **PASS** |
| **3 — symmetry and conservation** | the census is invariant under `φ → φ + 32` in **every** field and **not** under `φ → φ + 16`; the census's centro-symmetric set equals `C-0063`'s own `centroSymmetricUpwardPhases` at both widths, computed two independent ways; a placement round-trips through its own key; **a uniform load on a uniform Winkler foundation dishes exactly zero on all four free hosts**, worst residual `2.3e−7` of the free stroke (the conditioning of the short element the row-end inset creates — and **exactly `0.0`** at phases 0 and 16, where the end plane is not a column and no short element exists) | **PASS** |
| **4 — numerical convergence** | nested subdivisions 1 ⊂ 2 ⊂ 4 on the recommended placement: 0.0671041125 / 0.0658484796 / 0.0660332772, departure **0.0028**; the dishing grid 41/81/161: 0.0658484796 / 0.0658484796 / 0.0666462862, **0.012**; the Woodbury surrogate against the assembled 855-DOF solve, **1.5e−8**; the descent repeated from the same start returns an **identical placement key** at departure **0.0**; departures emitted at **two significant digits** and no step counter anywhere | **PASS** |
| **5 — literature and upstream** | **13 reproductions, 11 of them strict, worst strict departure `5.5e−10`**: `C-0063`'s **0.0706145537** (`2.9e−10`) and `C-0090`'s **0.0621469105** (`5.5e−10`), each recomputed **from the claim's own published placement key**; `C-0098`'s **60** and its **ten** phases; `C-0066`/`C-0093`'s **53** at phase 24; `C-0015`'s **56**, **49** and its **ten** eight-column phases; `C-0063`'s **two** centro-symmetric phases; `C-0090`'s **two** eight-column phases at 38.08 nm; `C-0055`'s **34**; `C-0054`'s `(D/(D−1))² = 1.14795918` | **PASS** |

### The declared falsifiers, and what actually happened

Declared in [`T-171`](../tasks/T-171.md) **before** execution.

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | at the buildable width a phase is simultaneously richest, eight-column and centro-symmetric — no conflict to price | **NO** | richest `[0, 16]`, eight-column and centro-symmetric `[8, 24]`; **0** phases satisfy all three at any of the three readings |
| **`F2`** | the two `D_⊥` readings of a seven-column sheet agree | **NO** | **0.875** against **0.857142857** — `7/8` against `6/7`, exactly `48/49` apart |
| **`F3`** | at matched search effort the richest phase is flatter | **NO** | phase 8 reads **0.065848481** against phase 16's **0.125068659**, same four starts, same descent |
| **`F4`** | the pipeline fails to reproduce `C-0063`'s or `C-0090`'s published optimum | **NO** | **2.9e−10** and **5.5e−10**, each from the claim's own placement key |
| **`F5`** | a uniform load on a uniform foundation dishes non-zero on a free host | **NO** | worst **2.3e−7** of the free stroke over the four hosts |
| **`F6`** | `C-0090`'s *"the row-end crossover can never be an upward site, at any phase"* holds | **NO — and that is `CH-0118`** | row-end upward stations by phase: `{0 = 15, 8 = 0, 16 = 15, 24 = 0}`; at 0 and 16 it adds fifteen stations and **zero** columns |

**Three results that were not anticipated.**

1. **That `C-0090`'s width collapse does not close the question.** The task was formulated with the
   possibility that it had; it takes the conflict from ten-against-ten to **two-against-two** and
   leaves it disjoint.
2. **That the whole richest set at the buildable width is manufactured by the end-of-row
   convention** — and that `C-0090`'s own prose denies the mechanism while its own descent table
   publishes a phase-0 placement standing on those very stations (roots at `±18.99 nm`).
3. **That the series/smeared `D_⊥` gap is exactly `48/49`.** The two readings coincide to
   `(D/(D−1))²` on a *uniform* lattice — `C-0054`'s own identity — and the seven-column sheet is
   the first *non*-uniform lattice in this programme that arises from a design choice rather than
   from depletion.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** Every flatness number is downstream of `C-0055`'s
  free lever on one upward crossover, which 62 recorded queries did not find in the literature.
- **The flatness comparison is a DESCENT at three of the four phases, and an exhaustive enumeration
  exists at only two.** The four-start descent is matched across phases, so the *comparison* is
  sound; the richest phases' numbers are **upper bounds** and an exhaustive treatment there could
  improve them. It would have to find **1.90×**, where the whole 32-phase descent spread at this
  width is 0.0658–0.1377, i.e. 2.09×.
- **One load state.** `C-0022`'s solved 2 mM / 10 nm / 0.192 V. `C-0068` measures a 12 % range cost
  at zero defects and `C-0058` a flat design dishing 0.187 at another state; **the phase
  recommendation is not re-read over `C-0022`'s other states**, and `C-0064`'s device partition
  means some of those states belong to other devices.
- **The collar is CARRIED to 38.08 nm, not re-solved.** `C-0100` measures that at **0.0712 %** of
  the flatness, three decades below the 1.90× here — but `T-174` owns the re-solved-collar re-run
  and this claim does not anticipate it.
- **The severance probability is an INDEPENDENT Bernoulli model on a MEASURED marginal.** `C-0087`'s
  map is position-dependent and this bound is evaluated at three of its values rather than
  convolved with its own spatial structure; staple incorporation is also not independent between
  neighbouring staples, and nothing here measures that. It is a **cheap bound**, and its ratio is
  the quantity to read, not its level.
- **`C-0087`'s map is a plain Rothemund rectangle's**, transferred to a crossover; an inter-layer or
  out-of-plane crossover's incorporation has never been measured (`C-0093`'s, `C-0098`'s open item).
- **`k_θ` is a fitted model input** (Chen et al., `α = 1`), and both `D_⊥` readings are linear in
  it, so the **ratios** here are `k_θ`-free and the **levels** are not.
- **The thermal fluctuation is equipartition on the harmonic model**, exact for that model, and it
  is quoted for the **free** host — a coupled tile's variance is a different quantity and is
  `C-0010`'s.
- **The end-of-row convention is carried, not re-argued.** `C-0095` settled the permission and
  `C-0099` the mechanics; the refused reading is censused and its own conflict is stated.
- **Static, single-layer, linear**, exactly as `C-0009`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0087`'s staple incorporation | 0.48 / 0.84 / 0.95 | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`** |
| `C-0093`'s redundancy slope | −0.784357442 | **CITED** |
| `C-0098`'s real-lattice redundancy slope | −0.376769756 | **CITED** |
| `C-0098`'s graded 40 nm comparison | 0.487309625 against 0.385192562 | **CITED** |
| `C-0063`'s and `C-0090`'s published optima | 0.0706145537, 0.0621469105 | **CITED, READ FROM THEIR RESULT FILES and REPRODUCED** to `2.9e−10` / `5.5e−10` |
| `C-0022`'s solved collar | 2 mM / 10 nm / 0.192 V, read at run time | **CITED** |
| `C-0100`'s width sensitivity of the collar | 0.0712 % | **CITED** |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** (Kim et al. 2012) |
| crossover hinge `k_θ` | 13.5294118 pN·nm/rad | **CITED, FITTED**, Chen et al. (2014) SI, via `C-0009` |
| interhelical distance, rise, bp/turn, crossover spacing | 2.69 nm, 0.34 nm, 32/3, 16 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| the buildable raster row | 112 bp = 38.08 nm | **`C-0086`/`C-0090`, CITED** |
| `C-0039`'s arm and `C-0085`'s quantisation | 8.16439083 → 8.16 nm | **CITED** |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |

Everything else — the 96-row census, the three demand ledgers and every intersection, the two
row-end congruences, the fourteen sheet-price channels, the four graded phases, the three tie-count
divisions, the recommendation, the convergence axes and the thirteen reproductions — is **derived
here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0063`** | **Nothing.** Its 0.0706145537 reproduces at `2.9e−10` from its own placement key, its centro-symmetry congruence is re-derived at both widths, and its two phases are half of the recommendation. What is **added** is that its demand is now priced against the other two and survives them. |
| **`C-0090`** | **Its verdict and every number stand; one quantifier in its prose does not.** Its 0.0621469105 reproduces at `5.5e−10`, its two eight-column phases at 38.08 nm are reproduced exactly, and its recommendation of **phase 8** is confirmed by an independent route. **`CH-0118`**: *"the row-end crossover can never be an upward site, at any phase"* is true at phases 8 and 24 and false at 0 and 16, where admitting it is worth **15** stations and **0** columns. Its own code computes this correctly; the sentence is the thing challenged. |
| **`C-0098`** | **Its *Still open* item 3 is DISCHARGED and its census is scoped.** The three demands are irreconcilable at both widths and the one to drop is the inventory — the demand `C-0098` itself raised. Its census is a **40.00 nm** census: at the buildable width the ten richest phases become **two** and the ten eight-column ones become **two**, and its disjointness finding survives the collapse. Its 60 and its ten phases both reproduce exactly. |
| **`CH-0113`** | **Its *How to settle it* item 3 is DISCHARGED** — *"whoever next chooses a crossover phase prices all three demands at once"*. Its item 1 (`C-0093` restating its row) and item 2 (`ANSWERS.md`) are unchanged and belong to their owners. |
| **`C-0093`** | **Nothing.** Its slope is consumed as an input and quoted, not refitted. Its 53-site ceiling remains `CH-0113`'s business. |
| **`C-0054`** | **Its 75 % spendable-crossover budget is an EIGHT-column number.** On a seven-column host the theorem gives 35 of 49, i.e. **71.4 %**, and the identity `series = (D/(D−1))² × smeared` holds only on a uniform lattice — a seven-column sheet is the first *designed* non-uniform lattice it has been read on. Nothing in `C-0054` is wrong; a qualifier is added. |
| **`C-0015`** | **Its *"seven columns is the better layout for both load classes"* is a point-load statement.** Under `C-0022`'s distributed solved load with no coupling the sign reverses: **+4.981 %** in peak crossover force. Its own two load classes are unchanged and not re-run here; what is added is a third, and the reading that the sign belongs to the **load path**. |
| **`C-0071`** | **Its recommended element is untouched and its stated phase is not.** Every one of its 14 graded margins is a function of the pitch, the interhelical distance and the arm length — `pitch − d − L = 0.0256 nm` contains no phase — so **no margin moves**. What moves is its **Conditions** line, which reads *"crossover phase **24**"*: `C-0090` had already moved the winner to phase 8 at the buildable width and this claim confirms it on an independent route. Its quoted design-state dishing **0.0706** is the 40.00 nm number; at the recommended width and phase it is **0.0621469105**. |
| **`ANSWERS.md`** | One edit is owed and the synthesis task owns it: the crossover phase the programme recommends is **8 at 38.08 nm**, and the three demands on it are irreconcilable with the inventory dropped at a cost of 1.06–1.12×. **A moved verdict is a challenge, not an overwrite.** |

## Still open — named, not answered

1. **An exhaustive enumeration at phases 0 and 16.** Neither admits a centro-symmetric family, so
   `C-0063`'s exhaustive route does not exist there and the descent's numbers are upper bounds. It
   would have to find 1.90×.
2. **The recommendation is read at ONE of `C-0022`'s states.** `C-0068`'s range reading and
   `C-0064`'s device partition are not applied to the phase; a phase flat at one state need not be
   flat over a device's stroke, and `C-0068` has already shown a placement can reverse between
   layer heights.
3. **The re-solved collar at 38.08 nm** (`C-0100`, `T-174`). Worth 0.0712 % on the flatness, so it
   cannot move this verdict — but the argmax placement at phase 8 may move, and that is `T-174`'s.
4. **The severance bound is an independent-Bernoulli model.** Correlated dropout between
   neighbouring staples, and `C-0087`'s own spatial structure convolved rather than sampled, would
   both change the level; the ratio is more robust than the level and neither is measured.
5. **Whether a seven-column host is ever worth choosing for a reason other than inventory.** Its
   only favourable channel found here is the *coupled* peak crossover force, and that reading
   confounds the host with the placement. A matched-placement comparison across column counts is a
   different study.
6. **The tie-count demand's own owner.** `C-0093`'s and `C-0098`'s shared body wants ties, not arms;
   this claim prices its phase demand and leaves its 3.76× exceedance exactly where `C-0098` left
   it.

## Challenges

**Raises [`CH-0118`](../challenges/CH-0118-the-row-end-crossover-is-a-station-at-two-phases.md)**
against `C-0090`'s Deliverable 3 quantifier.

**Discharges [`C-0098`](C-0098-shared-body-placement-and-distribution.md)'s *Still open* item 3 and
[`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md)'s
*How to settle it* item 3.**

**None stands against this claim.** The four ways it would fail:

1. **An exhaustive placement search at phase 0 or 16 finding 1.90×.** The descent spread over all
   32 phases at this width is 2.09×, so this would have to be nearly the whole of it.
2. **A measurement of an inter-layer or out-of-plane crossover's incorporation materially above a
   staple's**, which would move the severance channel — though not the two `D_⊥` readings, which
   contain no incorporation.
3. **NDI ruling the row-end crossover inadmissible.** Then there is no eight-column phase at
   38.08 nm at all, the richest set moves to fifteen seven-column phases at 53 stations, and the
   whole comparison must be re-run on `C-0090`'s BRACKET reading — where the flat design is already
   outside `T-5b`.
4. **A width other than 38.08 nm.** `C-0086`'s next admissible rung is 144 bp = 48.96 nm, 22 %
   larger than §3's nominal, and its congruences are different: the census here is complete over
   the phase and not over the width.
