# C-0056 — At `C-0054`'s connectivity ceiling an anchor's influence patch holds **0.694** crossovers, so the sheet stops being a continuum at **37 spent — five crossovers BEFORE it stops being connected** — and the four thresholds a design might read as *"where the plate fails"* fall in a strict order spanning **1.6× in the crossover count**: 28 (a coupling's own compliance) < 37 (the criterion) < 42 (connectivity) < 45 (a smooth load notices)

| | |
|---|---|
| **Task** | [`T-120`](../tasks/T-120.md), raised jointly by [`C-0054`](C-0054-consumed-crossover-sheet.md)'s *"Still open"* item 5 (*"its `ℓ_⊥/d` is not re-derived here"*) and [`C-0053`](C-0053-hinge-arm-array-packing.md)'s item 1 |
| **Leaf** | **`A8.2`** (structural rigidity / mode analysis), with `A1.2` for the anchoring scheme the counts belong to |
| **Verification type** | **logical** (four closed-form criteria and their exact inversions on measured lattice constants — no mesh, no fitted parameter) **+ in-silico** (`C-0009`'s beam-and-hinge grillage, `C-0006`'s orthotropic plate and a **third** model — the uncoupled beam array — solved side by side under `C-0022`'s **solved** electrostatic profile) |
| **Verdict** | **PASS on the predicate, and the answer is NO — but the interesting part is *when* it becomes no, and it is not where any standing claim would have looked.** The depleted lattice is parametrised by **one** number, the retained crossover count, through `p_eff = p N/N_ret` and `D_⊥ = k_θ d/p_eff`; both reduce to `C-0009`'s own constants **exactly** at `N_ret = N`, so every criterion is that claim's published number times an exact power of `N_ret` and **each therefore inverts**. At the ceiling (14 retained) `ℓ_∥/p_eff = 0.208`, `ℓ_⊥/d = 1.061` and **an anchor's influence patch holds 0.694 crossovers** against `C-0009`'s 3.93 intact. **Inverted rather than evaluated, the patch criterion reaches one at 18.74 retained — 37.26 SPENT — which is INSIDE the region `C-0054` declares buildable.** The three criteria disagree and it matters which a design trusts: `ℓ_⊥/d` never sees the failure (it crosses one at 44.9 spent, past the ceiling) and `ℓ_∥/p_eff` demands **67.2** crossovers, more than the sheet owns, so it fails on the *intact* sheet and cannot distinguish the ceiling from anything — `C-0009`'s own finding, now shown to be **degenerate** rather than merely pessimistic. **A criterion is not a model, so what replaces the plate is settled separately, by putting the uncoupled beam array beside it**: it becomes the nearer model to the lattice at **28 spent** on the point compliance, at **37** on the criterion, at **42** the sheet severs and only at **45** does a smooth load notice. **`C-0009`'s smooth/point split survives the ceiling and is what settles the question**: under `C-0022`'s solved load the smeared plate is still within **14.9 %** at the ceiling, under a concentrated 100 pN lever it is out by **33–79 %**, and the point compliance a coupling feels varies **2.53×** over one crossover cell where the plate says 1.14× — a spread that is not an error in the plate's number but a **quantity the plate does not have**. `C-0054`'s harmonic/smeared discrepancy is **the physics and not an artefact, and it is not what fails first**: at and below the ceiling every interface still holds a crossover, the two conventions agree to `(15/14)² = 1.1480` exactly, and the collapse is a property of the **first empty interface** at 43 spent — six past where the patch criterion has already refused the plate. Raises [`CH-0069`](../challenges/CH-0069-the-plate-fails-before-the-connectivity-does.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the motif whose hinge budget raised the question is NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number. |
| **Provenance** | `gpd/results/T-120-connectivity-ceiling-plate.json`, produced by `structure.ConnectivityCeilingPlateStudyKt`; model in `src/main/kotlin/structure/ConnectivityCeilingPlate.kt`; **8 cheap bounds, 55 criterion records over 5 foundations × 11 levels, 15 inversions, 22 census records, 216 solved three-model states, 24 energy splits, 22 registration records, 4 variance records, 13 convergence records, 20 upstream reproductions, 4 runtime falsifiers**; **22 gate-named tests in `src/test/kotlin/structure/ConnectivityCeilingPlateTest.kt`**; the result file re-run through `tools/study.sh` twice and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, **8 symmetrically centred crossover columns** (56 crossovers); `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V; `C-0001`'s foundation secant, swept ×[0.25, 4]; `C-0017`'s 33.3333 pN/nm mandate as `n` equal springs |
| **Consumes** | [`C-0009`](C-0009-discrete-lattice-tile.md) (**the criteria themselves**, the grillage, `D_⊥ = k_θ d/p`, `ℓ = (D/k_f)^(1/4)`, the 25.6× anisotropy, the smooth/point split — re-run as a library), [`C-0054`](C-0054-consumed-crossover-sheet.md) (the pigeonhole ceiling, `consumedCrossovers`, the `SPREAD` pattern, the Voigt/Reuss pair and their `(15/14)²` identity, the 0.242 dishing), [`C-0053`](C-0053-hinge-arm-array-packing.md) (the 25-arm plan-view bound, cited), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the **mismatched** criterion it used, the flatness convention), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on concentration, gap **and bias**), [`C-0047`](C-0047-single-column-flatness.md)/[`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md) (the flatness pipeline, its 0.218/0.695 and its saturation), [`C-0010`](C-0010-tile-positional-variance.md) (`positionalVarianceBudget`, the `D_⊥` insensitivity), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 3 × 15 grid, the registration lever, *"shapes, not counts"*), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate) |
| **Raises** | [`CH-0069`](../challenges/CH-0069-the-plate-fails-before-the-connectivity-does.md), against `C-0054`'s reading of its own `39 ≤ n ≤ 42` window as a region in which its continuum companion numbers still apply |

---

> ℹ️ **Interaction with [`C-0055`](C-0055-unused-junction-site.md) (`T-119`), filed in the same iteration, which challenges the premise that any crossover is spent at all.**
>
> `C-0055` reads Ke et al.'s square-lattice rule directly and finds the sheet occupies **two of four**
> crossover azimuths, so a hinge can be rooted **out of the sheet plane** without taking an interface
> crossover: its ceiling is *"52–60 with every interface intact"*, i.e. `N_ret = 56` and **no depletion
> at all**. **That does not move one number in this claim, because every criterion here is a function
> of the RETAINED count and of nothing else.** `C-0055` changes how many crossovers a design *spends*;
> it does not change what a criterion reads at a given `N_ret`.
>
> **What it does change is which regime the Gen-1 design sits in.** If `CH-0068` is upheld, the design
> point is `N_ret = 56`, the patch count is 3.93, the continuum is exactly as valid as `C-0009` found
> it, and this claim's thresholds are a *contingency* rather than a description — as is
> [`CH-0069`](../challenges/CH-0069-the-plate-fails-before-the-connectivity-does.md), which is written
> against `C-0054`'s window and falls with it. If `CH-0068` is refused, the thresholds bind.
> **Four results here are indifferent to that outcome**: the criteria's exact inversions, the
> degeneracy of `ℓ_∥/p`, the `ℓ_⊥/d` foundation/depletion identity, and the smooth-versus-point change
> of model class — none of them contains a design.

---

## The claim, in one line

**`C-0054` found the count at which the sheet stops being one body; this claim finds the count at which it stops being one *material*, and it is five crossovers lower — so the design window `C-0054` reports is a window in which the tile is connected and the plate that priced it is not valid.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rigidities **pN·nm**, foundation stiffness
  **pN/nm³**, pressure **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at **300 K** in aqueous
  **2 mM MgCl₂**.
- **Plan view.** `x` **along** the helices, `y` **across** them, origin at the tile centre; `w`
  positive **downward**, compressing the polymer layer.
- **An interface** is an adjacent duplex pair; a 15-duplex sheet has **14**, each carrying the
  columns of one parity, so the per-interface pitch is **32 bp = 10.88 nm** and the nominal
  eight-column layout carries **56** crossovers (`C-0015`, `C-0040`).
- **`N_ret`** is the retained crossover count, and it is the **only** parameter of every criterion.
- **`p_eff = p · N/N_ret`** — the depleted per-interface pitch, on the areal-density reading.
- **`D_⊥ = k_θ d/p_eff`** — the depleted across-helix rigidity, i.e. `C-0009`'s `k_θ d/p` scaled by
  `N_ret/N`. `D_∥ = EI/d` is **untouched**: a crossover is not a load path along the helices.
- **`ℓ = (D/k_f)^(1/4)`** — the Winkler bending length, `C-0009`'s own.
- **The connectivity ceiling** is `N_ret = D − 1 = 14`, i.e. 42 spent — `C-0054`'s pigeonhole.

**Both depletion rules reduce to `C-0009`'s own constants exactly at `N_ret = N`**, which is what
makes every number below that claim's published one times an exact power of the retained count:

&nbsp;&nbsp;&nbsp;&nbsp;`ℓ_⊥/d ∝ N_ret^(1/4)`, &nbsp; `ℓ_∥/p_eff ∝ N_ret`, &nbsp;
**`patch ∝ N_ret^(5/4)`**.

---

## The cheap bound, which ran first and answered the predicate

&nbsp;&nbsp;&nbsp;&nbsp;**crossovers in an anchor's influence patch at the ceiling = `π ℓ_∥ ℓ_⊥/(d p_eff)` = 0.694**

Four operations on `C-0009`'s own 3.93, no mesh, no matrix.

> **The declared falsifier did not fire.** Had the patch count stayed at or above one at the
> ceiling, the continuum would have survived the connectivity limit and the task would have
> closed in a paragraph.

| | value | what it settles |
|---|---|---|
| patch, intact | **3.929** | `C-0009`'s 3.9 — *"four elements is not a continuum"* |
| **patch, at the ceiling** | **0.694** | **under one**, and it is the answer to the predicate |
| **the count at which patch = 1** | **18.74 retained, 37.26 SPENT** | **five crossovers below `C-0054`'s ceiling of 42** |
| `ℓ_⊥/d` at the ceiling | **1.061** | still above one — this criterion **never sees it** |
| `ℓ_∥/p_eff` at the ceiling | **0.208** | against 0.834 intact: the criterion that already failed |
| the count `ℓ_∥/p_eff` demands | **67.2** | **more than the sheet owns**; unreachable at any consumption |
| the foundation at which the two ceilings coincide | **0.482 × `C-0001`** | softer than that, connectivity fails first |

---

## Deliverable 1 — `C-0009`'s criteria, re-derived down to the ceiling

Nominal foundation. **The `spent = 0` row is `C-0009`'s published table and is asserted against it
to the digits it printed, not to a tolerance of convenience.**

| spent | kept | `p_eff` [nm] | `D_⊥` [pN·nm] | `ℓ_⊥` [nm] | **`ℓ_⊥/d`** | **`ℓ_∥/p_eff`** | **in patch** |
|---|---|---|---|---|---|---|---|
| **0** | 56 | **10.880** | **3.3450** | **4.035** | **1.500** | **0.834** | **3.929** |
| 14 | 42 | 14.507 | 2.5088 | 3.754 | 1.396 | 0.625 | 2.742 |
| 28 | 28 | 21.760 | 1.6725 | 3.393 | 1.261 | 0.417 | 1.652 |
| **37** | **19** | **32.067** | **1.1349** | **3.079** | **1.145** | **0.283** | **1.017 — the crossing** |
| 40 | 16 | 38.080 | 0.9557 | 2.950 | 1.097 | 0.238 | 0.821 |
| **42** | **14** | **43.520** | **0.8363** | **2.853** | **1.061** | **0.208** | **0.694 — the ceiling** |
| 45 | 11 | 55.389 | 0.6571 | 2.686 | 0.999 | 0.164 | 0.514 |
| 49 | 7 | 87.040 | 0.4181 | 2.399 | 0.892 | 0.104 | 0.292 |

**The three criteria cross one at three different places, and only one of them lands inside the
design region.** Over `C-0009`'s whole foundation sweep:

| criterion | `k_f` ×0.25 | ×0.5 | **×1.0** | ×2.0 | ×4.0 |
|---|---|---|---|---|---|
| **patch = 1, spent** | 45.2 | 41.8 | **37.3** | 31.3 | 23.4 |
| `ℓ_⊥/d` = 1, spent | 53.2 | 50.5 | **44.9** | 33.9 | 11.7 |
| `ℓ_∥/p_eff` = 1, spent | 8.5 | **−0.5** | **−11.2** | −23.9 | −39.0 |

*(a negative "spent" is a criterion that fails on the intact sheet — `C-0009`'s own finding, and
at every foundation but the softest)*

---

## Deliverable 2 — the identity that says why the three disagree

&nbsp;&nbsp;&nbsp;&nbsp;**`ℓ_⊥/d` depends only on `D_⊥/k_f`, so consuming to `N_ret/N = 1/4` is EXACTLY a
fourfold stiffer foundation for it — and it is not, for the other two.**

That is why the ceiling's **1.061** is the *same number* as `C-0009`'s own `k_f × 4` corner, to the
last digit. The other two criteria separate the same pair of states by exactly **2√2**, because
`p_eff` moves with consumption and does not move with the foundation. Asserted as a gate-3 test.

&nbsp;&nbsp;&nbsp;&nbsp;**An across-helix criterion cannot tell a depleted sheet from a stiffer polymer
layer.** A design that reports `ℓ_⊥/d ≥ 1` has said nothing about how many crossovers it has left.

---

## Deliverable 3 — the integer census, which is where a density stops being a count

Over `C-0015`'s 45 attachment stations, counting **actual** retained crossovers inside each
anchor's elliptical patch `(ℓ_∥, ℓ_⊥)`:

| spent | kept | density | min | mean | max | **anchors with NO crossover** | mean distance to the nearest [nm] |
|---|---|---|---|---|---|---|---|
| 0 | 56 | 3.929 | 1 | 3.11 | 4 | **0** | 2.04 |
| 14 | 42 | 2.742 | 0 | 2.40 | 4 | 1 | 2.93 |
| 28 | 28 | 1.652 | 0 | 1.42 | 3 | 13 | 5.10 |
| 37 | 19 | 1.017 | 0 | 0.93 | 3 | 22 | 7.09 |
| **42** | **14** | **0.694** | **0** | **0.62** | **2** | **28 of 45** | **8.37** |

*(staggered retention; on `C-0054`'s own `SPREAD` round robin it is **30 of 45** and 14.08 nm)*

**Below one crossover per patch the density and the census stop being the same statement**, because
0.694 is 0 or 1 depending on where the anchor lands. At the ceiling **28 of 45 attachment stations
have no surviving crossover inside their own influence patch at all**: their load reaches the
foundation and never reaches the sheet's across-helix path.

> **A retention artefact found and bracketed rather than inherited.** `C-0054`'s `SPREAD` pattern
> is round robin over the *interfaces* — optimal for connectivity, which is what makes its
> pigeonhole tight — and its column tie-break takes the **lowest** available one, so at the ceiling
> all fourteen survivors sit in the two lowest columns and the right two thirds of the tile has no
> across-helix path. `staggeredRetention` is the other extreme, and the two bracket a real design:
> same count, same density, **1.68× in the mean distance to the nearest surviving crossover**.
> **Which crossovers survive is a design variable, and no upstream claim owns it.**

---

## Deliverable 4 — what replaces the plate, and the order of the four thresholds

A criterion says a reduction has stopped being valid; it does not say what to use instead. Three
models under `C-0022`'s solved load, on `C-0015`'s 3 × 15 grid:

- **PLATE** — `C-0006`'s orthotropic Kirchhoff plate with `D_⊥` smeared to `k_θ d/p_eff`;
- **LATTICE** — `C-0009`'s grillage with the spent crossovers removed (the reference);
- **BEAMS** — the same duplexes on the same foundation with **no across-helix path at all**.

| spent | pieces | lattice [nm] | plate [nm] | beams [nm] | plate off by | nearer |
|---|---|---|---|---|---|---|
| 0 | 1 | 1.0708 | 1.1649 | 2.2866 | 8.8 % | PLATE |
| 28 | 1 | 1.1192 | 1.2567 | 2.2866 | 12.3 % | PLATE |
| 37 | 1 | 1.1586 | 1.3140 | 2.2866 | 13.4 % | PLATE |
| **42** | **1** | **1.1860** | **1.3633** | **2.2866** | **14.9 %** | **PLATE** |
| **45** | **4** | **2.2830** | 1.4057 | 2.2866 | 48.5 % | **BEAM_ARRAY** |
| 56 | 15 | 2.2866 | 10.2234 | 2.2866 | 347 % | BEAM_ARRAY |

&nbsp;&nbsp;&nbsp;&nbsp;**On a smooth load the plate survives the ceiling.** It is out by 14.9 % at
42 spent and is still the nearer of the two models; it is replaced only at **severance**.

&nbsp;&nbsp;&nbsp;&nbsp;**On a point-coupled quantity it does not.** The point compliance a coupling
element actually feels — max over one crossover cell — is 1.046 nm/pN on the lattice at the
ceiling against **0.330** on the plate and 1.428 on the beam array: the beam array is the nearer
model from **28 spent** onward, and under a concentrated 100 pN lever the plate is out by
**33 % (staggered) to 79 % (round robin)** where the smooth load costs it 15 %.

**So the four thresholds fall in a strict order, asserted as a runtime check rather than read off a
table:**

&nbsp;&nbsp;&nbsp;&nbsp;**28** *(a coupling's own compliance)* **< 37.3** *(the criterion)*
**< 42** *(connectivity)* **< 45** *(a smooth load notices)*

**1.61× in the crossover count between the first and the last.** A design that reads any one of
them as *"where the plate fails"* is wrong by up to that factor, and `C-0054` reads the third.

---

## Deliverable 5 — the standing verdicts, checked at the ceiling

### `C-0006`'s load distribution

**The across-helix path stops carrying the load long before it stops existing.** The strain-energy
split under `C-0022`'s solved load with the 3 × 15 coupling (energies, not retained matrices —
`CLAUDE.md`):

| spent | beams [pN·nm] | **hinges** | foundation | **hinge share of the total** |
|---|---|---|---|---|
| 0 | 2.3115 | **0.1146** | 48.961 | **9.04e−4** |
| 28 | 2.3193 | 0.0872 | 49.017 | 6.87e−4 |
| **42** | 2.3233 | **0.0691** | 49.060 | **5.44e−4** |
| 56 | 2.3032 | **0.0000** | 49.913 | **0** |

The hinges carry **under a thousandth** of the energy on the *intact* sheet, so removing three
quarters of them changes 9.0e−4 to 5.4e−4 and nothing in `C-0006`'s force balance moves. **That is
consistent with `C-0054`'s finding that the peak crossover force falls monotonically**, and it is
the reason: the load path was never through the hinges under a distributed load. **`C-0006`'s
verdicts stand at the ceiling; what does not stand is using the plate to price a point coupling.**

### `C-0010`'s positional variance

`C-0010`'s *"a 2× change in `D_⊥` moves the answer by 2.5 %"* is re-tested on the same lever
(a factor of two in `D_⊥`, via `k_θ`) at every consumption level, on both models:

| spent | kept | lattice dishing RMS [nm] | at 2× `D_⊥` | **lattice sensitivity** | **plate sensitivity** |
|---|---|---|---|---|---|
| 0 | 56 | 0.9664 | 0.9367 | **3.07 %** | 3.71 % |
| 14 | 42 | 1.2391 | 1.2176 | 1.74 % | 3.65 % |
| 28 | 28 | 1.4385 | 1.4207 | 1.23 % | 3.57 % |
| **42** | **14** | **1.6515** | **1.6352** | **0.99 %** | **3.38 %** |

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0010`'s insensitivity SURVIVES and gets stronger, and it is still the
wrong channel.** The shape modes stay foundation-dominated all the way to the ceiling — a factor of
two in `D_⊥` is worth 3.1 % intact and **0.99 %** at the ceiling — while the *amplitude* rises
1.71×, exactly as `C-0054` reports. **The insensitivity is to the rigidity; the amplitude moves
with the connectivity, and those are different parameters.** Anyone quoting `C-0010`'s 2.5 % as
tolerance to crossover consumption is reading a bound on the wrong body, which is `C-0054`'s
warning confirmed on its own lever.

### `C-0047`'s flatness, under `C-0022`'s solved load

| spent | 1 × 15 | **3 × 15** | free tile | flat at `T-5b`'s 10 %? |
|---|---|---|---|---|
| 0 | 0.695 | **0.218** | 0.308 | no |
| 42 | 0.813 | **0.242** | 0.362 | no |
| 45 | 1.029 | 0.465 | 0.564 | no |

`C-0047`'s three-column break-even and its 0.218/0.695 are reproduced exactly, and **nothing reaches
`T-5b`'s tolerance at any consumption level** — `CH-0034`'s saturation, not a new failure. What
moves is the *ratio* the plate would have predicted: the plate under-reads the ceiling's dishing by
14.9 % and over-reads a severed sheet's by up to 347 %.

---

## The five verification gates

Executed as **22 gate-named tests** in `src/test/kotlin/structure/ConnectivityCeilingPlateTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with three concurrent agents'
mid-TDD files dropped by `--drop-file` (`coupling/NonUniformCouplingTest.kt`,
`coupling/NonUniformCouplingStudy.kt` — a **main** source — and `anchoring/BackboneTorsionTest.kt`).

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | every criterion is a pure ratio and the patch count an area over an area: scaling all lengths by `λ` and all rigidities by `λ⁴` leaves all four invariant to `1e−12` while `ℓ` scales by `λ`; the elliptical census is a count and doubles with the patch; unphysical arguments throw at **eight** entry points, including a zero retained count, a retained count above the inventory on the **integer** entry point, and a zero lattice reference in the model selection | **PASS** |
| **2 — limiting cases** | **zero consumption reproduces `C-0009`'s published table** — 0.83, 1.50, 0.37, 3.9 at nominal, 1.18/2.12/7.9 at ×0.25 and 0.59/1.06/2.0 at ×4; **a uniform load dishes a free tile exactly zero** in all three models at 0, 14, 28, 42 and 56 spent (also a runtime falsifier in the study); the beam array **is** the lattice with every crossover spent, and has fifteen components; every criterion is strictly increasing in the retained count; the staggered retention keeps the sheet in one piece and uses ≥ 6 columns where the round robin uses ≤ 2 | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | nested mesh 1 ⊂ 2 ⊂ 4 (`1.7e−3` → `5.2e−5`); the crossover link penalty over `1e3 … 1e6` (`2.9e−5`); the plate's Ritz basis degree 8/10/12 (`1.4e−3`); the census sample grid 15/45/135 stations (`1.4e−1` → 0, and the coarse row is a *placement* statement, recorded); the patch inversion lands at 18.7413 / 37.2587; the result file re-run through `tools/study.sh` **twice** and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **20 reproductions, worst strict departure `1.8e−2` against a value `C-0009` prints to two significant digits** — its `D_∥`, `D_⊥`, 25.56 anisotropy, `ℓ_∥`, `ℓ_⊥` and all four criteria at three foundations; `C-0006`'s **mismatched** 0.37; `C-0054`'s 42, its `(15/14)²` and its 0.242; `C-0047`'s 0.218 and 0.695; `C-0022`'s 0.308. Lattice constants cited to Rothemund (2006), Fischer et al. (2016), Douglas et al. (2009) and Chen et al. (2014) as in `C-0009` | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **Each criterion inverts exactly and the inversion round-trips.** Evaluating a criterion at its
   own inverted count returns the target to `1e−12` — on a *continuous* entry point, so the
   round-trip is not a table lookup. The along-helix inversion returns **more than the inventory**,
   and that is asserted rather than clipped.
2. **The degeneracy identity.** `ℓ_⊥/d` at `(N/4, k_f)` equals `ℓ_⊥/d` at `(N, 4k_f)` to `1e−12`,
   and the other two criteria separate the same pair by exactly `2√2`. Nothing in the construction
   forces the first or the second.
3. **Global force balance** at every consumption level in all three models, on a field the
   consistent load vector and the footprint quadrature integrate **identically** (uniform plus a
   point load), to `1e−9`. Under `C-0022`'s collar profile the two quadratures differ by ~0.07 % on
   the 1 nm rim term, so the balance there is asserted at `1e−3` and the limitation is named.
4. **A plate cannot represent the lattice's inhomogeneity**, and it is asserted rather than
   observed: the point compliance spread over one cell is larger on the depleted lattice than on
   the smeared plate **and** larger than on the intact lattice — so the spread is a statement about
   depletion and not about the tile's free edges.
5. **The ordering of the four thresholds is a runtime check**, not a reading of a table: the study
   refuses to emit a result file in which 28 < 37.3 < 42 < 45 does not hold.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the patch count at the ceiling staying at or above 1 | **no** | **0.694**, and the task did not close in a paragraph |
| 2 | `N_ret = N` failing to reproduce `C-0009` | **no** | 20 reproductions, worst strict `1.8e−2` |
| 3 | a uniform load dishing anything but zero | **no** | `< 1e−9` at every level, in all three models |
| 4 | the beam array being nearer than the plate on **every** quantity at the ceiling | **partly, and it is the finding** | it is nearer on the **point compliance** from 28 spent and further on the **smooth dishing** until 45 |
| 5 | the plate being the nearer model at every level swept | **no** | it loses the point compliance at 28 and the smooth dishing at 45 |

**A result that was not anticipated:** the task was formulated as *"is it still a plate?"* and the
answer is that **"a plate" is not one question**. The same body at the same consumption is a
perfectly good plate for a distributed load (15 % at the ceiling) and not a plate at all for a
point-coupled one (33–79 %, and a 2.5× compliance spread the plate cannot express). `C-0009`'s
smooth/point split was reported as a *sign* of an error; at the ceiling it is a **change of model
class**, and it is the reason the four thresholds do not coincide.

**A second one:** the criterion `C-0009` singled out as the one that fails, `ℓ_∥/p`, turns out to be
**degenerate** — it demands 67.2 crossovers on a sheet that has 56, so it reads "invalid" at every
consumption level including zero and therefore carries no information about depletion at all. The
criterion that *does* discriminate is the one `CLAUDE.md` recommends for needing no convention.

---

## Does `C-0054`'s verdict survive?

**Its pigeonhole, its lattice numbers, its rigidity conventions and its connectivity verdict survive
untouched and are re-run rather than restated. What does not survive is the implicit licence that
its continuum companion numbers still describe the sheet inside its own window.**

| `C-0054` said | this claim finds |
|---|---|
| a connected sheet can spend at most 42 of 56 | **untouched, and reproduced exactly** |
| the branch survives at `39 ≤ n ≤ 42` | **the sheet in that window is not a continuum**: the patch criterion crosses one at 37.3 spent, so the whole window sits past it — `CH-0069` |
| the smeared plate *"still reports 0.667 pN·nm and an anisotropy of 128 instead of unbounded"* at 45 spent | **correct, and the collapse is at the FIRST EMPTY interface (43 spent), not at the ceiling.** At and below 42 the two conventions agree to `(15/14)²` exactly and both are finite — the series reading is the physics, and it is not what fails first |
| *"a 2× change in `D_⊥` moves `C-0010` by 2.5 %" is correct and is not the relevant channel* | **confirmed on its own lever**: the sensitivity *falls* to 0.99 % at the ceiling while the amplitude rises 1.71× |
| the flatness at the ceiling is 0.242 of the stroke | **reproduced to `1.3e−3`** |
| *"where the empty interfaces fall is arbitrary within the spreading pattern"* | **and so is where the survivors' COLUMNS fall**, which its tie-break fixes at the two lowest: worth 1.68× in the distance from an anchor to the nearest surviving crossover, at identical count |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.**
- **The depletion rules are the AREAL-DENSITY reading and they are a modelling choice**, stated
  rather than assumed: `p_eff = p N/N_ret` and `D_⊥ = k_θ d/p_eff`. They are the only pair that
  reduces to `C-0009`'s constants exactly at `N_ret = N`, and the *criteria* are properties of them.
  A design whose survivors are clustered has a *local* `p_eff` much larger than the mean, so the
  criteria as computed are **optimistic** wherever the retention is non-uniform — which the census
  quantifies and the two retention patterns bracket.
- **The criteria are evaluated on the SMEARED `D_⊥`**, deliberately: they are tests of whether a
  smeared description is admissible, so evaluating them on a series rigidity would beg the question.
  `C-0054`'s Reuss reading is carried beside them and its `(15/14)²` identity reproduced.
- **The lattice is the nominal eight-column layout, 56 crossovers.** `C-0015` shows 22 of the 32
  base-pair phases carry **49**, whose ceiling is 35; the criteria scale with `N_ret` and the
  inversions with `N`, so the thresholds move, and they are **not swept here**.
- **Two retention patterns are swept and they bracket the placement**; a real `E5a` array's
  survivors would fall where its plan view puts them, which is `C-0053`'s question and not this one.
- **The model-selection verdict is taken on peak dishing and on point compliance.** Other
  quantities — the peak per-load-path force, the in-band variance — are not model-selected here.
  `C-0054` computes the first on the lattice and it is not in dispute.
- **The concentrated-lever crossing is PLACEMENT-dependent** (37 spent on the round robin, 45 on the
  staggered retention) because it depends on whether a survivor sits under the load. The point
  compliance, which is a maximum over the cell, crosses at **28 on both** and is the quantity quoted.
- **`k_θ` is `C-0009`'s CITED, FITTED constant** (Chen et al., `α ∈ [0.6, 1.2]`). It enters `D_⊥`
  linearly and every criterion through a **fourth root**, so Chen's whole ±20 % band is ±4.7 % in
  `ℓ_⊥` and ±4.7 % in the patch count — worth about **1.5 crossovers** in the 37.3 threshold, and
  no verdict moves.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**, not a measurement.
- **The foundation is `C-0001`'s secant**, swept ×[0.25, 4] for the criteria and held at nominal for
  the solves; the variance table uses `C-0010`'s working-point stiffness.
- **The vertical crossover link is a constraint (a penalty), as in `C-0009`.** A crossover with real
  axial compliance would add a load path and soften the intact sheet toward the depleted one.
- **Static, single-layer, linear**, exactly as `C-0009`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| crossover spacing per interface | **32 bp** | **CITED**, Rothemund, *Nature* **440**:297 (2006), via `C-0015`/`C-0040` |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al., *Nature* **459**:414 (2009) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| duplex `EI`, `GJ` | 230, 460 pN·nm² | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) |
| the solved edge profile | depth −0.3029 over 8.939 nm, rim −0.5939 over 1.0 nm | **`C-0022`**, read from its own result file |
| `C-0009`'s published criteria | 0.83, 1.50, 0.37, 3.9, 1.18, 2.12, 7.9, 0.59, 1.06, 2.0, 9.07, 4.03, 85.50, 3.345, 25.56 | **CITED**, and every one reproduced here as a gate-5 test |
| `C-0054`'s, `C-0047`'s and `C-0022`'s published numbers | 42, 1.1480, 0.242, 0.218, 0.695, 0.308 | **CITED**, and every one reproduced here |
| `C-0010`'s `D_⊥` insensitivity | 2.5 % | **CITED as PROSE, not as a number** — it is quoted from a three-row table on a 40 × 40 tile against this programme's 40 × 40.35, and the same lever on that table's own end points is 3.5 %. Reproduced here as 3.7 % on the plate and flagged **non-strict** in the result file |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — both depletion rules, all four criteria at every consumption level and foundation,
the three closed-form inversions, the degeneracy identity, the elliptical census on two retentions,
the three-model comparison on three placements and three load cases, the energy split, the point
compliance spread and the four ordered thresholds — is **derived here in code**, with `C-0009`'s,
`C-0022`'s, `C-0047`'s and `C-0054`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The crossover phase.** Every number here is at the nominal eight-column layout. The 22
   seven-column phases carry 49 crossovers and a ceiling of 35, and the inversions move with `N`.
2. **Where a real `E5a` array's survivors fall.** The two retention patterns bracket it and differ
   by 1.68× in the census; `C-0053`'s plan view is what would settle it.
3. **A homogenisation theory for a sparse lattice.** This claim says the smeared plate is out by
   15 % (smooth) to 79 % (point) at the ceiling and that the uncoupled beam array is nearer on the
   point compliance. **Neither is a model of the ceiling**; the honest model is the lattice itself,
   which is what every number here is computed on.
4. **Whether the peak per-load-path force needs the same treatment.** `C-0054` computes it on the
   lattice and finds it falls to zero; nothing here checks whether a *continuum* estimate of it
   survives the ceiling, and `C-0009`'s own concentration factor is a lattice quantity anyway.
5. **The in-band variance at the ceiling.** `C-0010`'s bandwidth argument is not re-run; the
   amplitude moves 1.71× and the corner frequency, which is `C-0004`'s, has not been re-derived on a
   sheet whose rigidity has fallen fourfold.

## Challenges

**Raises [`CH-0069`](../challenges/CH-0069-the-plate-fails-before-the-connectivity-does.md)** against
the implicit licence in `C-0054`'s `39 ≤ n ≤ 42` window. **No number in any consumed claim fails to
reproduce.**

**None stands against this claim.** The four ways it would fail:

1. **A different depletion rule for `p_eff`.** If the survivors' *local* pitch rather than the mean
   is what a criterion should carry, every threshold moves — and it moves toward **worse**, because
   any non-uniform retention has a larger local pitch somewhere. The direction is settled; the
   number is not.
2. **A measurement of the crossover's own vertical compliance.** Modelled as a constraint, as in
   `C-0009`; a soft link softens the intact sheet toward the depleted one and narrows every gap here.
3. **A criterion that discriminates better than the patch count.** The claim is not that the patch
   count is the right criterion, only that it is the only one of `C-0009`'s three that crosses inside
   the design region and the only one carrying no direction convention.
4. **A design that never applies a point load to the sheet.** The 15 %-at-the-ceiling smooth result
   would then be the whole answer and the plate would survive to severance. `C-0017`'s coupling is a
   set of discrete attachments, so this programme does not have that design.
