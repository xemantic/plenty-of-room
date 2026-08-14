# C-0061 — 34 duplexes stacked above the tile do **exactly nothing** to its rigidity, and the reason is the same sentence that limited them to 34: a body held at **one** crossover adds a **zero** Schur complement, and `C-0055`'s arm is 8.164 nm against a 10.88 nm root pitch so it can never be held at two — the array is worth `0` in stiffness, `1.21×` in quality factor, `−9.1 %` in drainage corner, and its whole effect on the device is that the coupling now enters at **its** stations, where a uniform coupling dishes **1.35× worse than no coupling at all**

| | |
|---|---|
| **Task** | [`T-121`](../tasks/T-121.md), raised by [`C-0055`](C-0055-unused-junction-site.md)'s *"Still open"* item 1 — *"the largest open item on the branch"* |
| **Leaf** | **`A8.2`** (structural rigidity and joint stiffness), with **`A1.2`** for the anchoring scheme and the `σ_RMS ≤ 3.0 nm` predicate |
| **Verification type** | **logical** (a static-condensation identity and three length inequalities — no mesh, no fitted parameter) **+ in-silico** (`C-0009`'s grillage **augmented** with the arms as extra degrees of freedom in one stiffness matrix, `C-0006`/`C-0047`'s flatness and `C-0010`'s variance re-run under `C-0022`'s **solved** load, `C-0058`'s rim family re-run on the buildable stations, and `C-0004`'s drag budget re-read from its own result file) |
| **Verdict** | **PASS, and the answer to all three of `C-0055`'s named channels is settled in the order the cheap bound put them.** **RIGIDITY: exactly zero.** A body attached to a structure at **one** point and otherwise free has a **zero Schur complement** there — its rigid-body motions span everything one crossover can impose (deflection through the vertical link, roll through `k_θ`), so the minimised arm energy is identically zero. 34 arms covering **46.3 %** of the tile's plan change `C-0009`'s peak dishing, peak crossover force and peak duplex shear by `5e−9` relative, and that residual is the arm regularisation and vanishes **linearly** with it over four decades. **And the single tie is not a modelling choice**: an upward site belongs to one duplex, so its lattice pitch is the bare **32 bp = 10.88 nm**, while `C-0039`'s elastica gives **8.164 nm** at `C-0055`'s self-consistent 34 paths and **9.131 nm even at §3's 45** — *no arm in the design range can reach a second root*. **The escape, its price and now its structural harmlessness are one sentence.** The bracket has three steps and none of them is large: one tie adds nothing; **two ties add a TORSION BAR and nothing else** — 5.832 pN·nm/rad, worth **0.112 %** of the dishing, because two points determine a line and a rigid arm meets both; the arm's own `EI` is engaged only at **three** ties, 21.76 nm of arm, and the 19.3× axially coupled second layer is outside both the motif and the kinematics. **MASS: 46.3 % more duplex, `√1.463 = 1.21×` on a quality factor `C-0004` puts at `5.3e−4`** — still overdamped by three orders, and the arms' inertial force at 1 kHz and §3's 3 nm stroke is `1e−10 pN` against §3's 100 pN. **DRAG: the only channel that moves anything, and it is an upper bound.** The arms stand on the `+z` face; the polymer layer and the electrode are on `−z`, so the squeeze film is untouched **by construction** and what they add is bulk dissipation in parallel with the tile's own Stokes term: **9.1 %** of `C-0004`'s total drag (8.7 % at the narrow duplex radius), taking the nominal corner from **91.2 kHz to 82.9 kHz** and the **worst** 40 × 40 nm §4(d) margin from **22.81× to 20.73×**. **§4(d) stays discharged and `C-0010`'s `σ_RMS ≤ 3.0 nm` stays PASS** — equipartition does not see a drag or a mass, so the broadband variance is *identically* unchanged and only the **in-band** amplitude rises, by **4.9 %**. **What the array really changes is WHERE THE COUPLING ENTERS, and that is the one place a verdict moves**: on `C-0055`'s own 34 roots a uniform coupling dishes **0.4156** of the stroke against **0.2182** on `C-0015`'s 3 × 15 and **0.3079** for the free tile — **1.35× worse than no coupling at all** — and `C-0058`'s flat rim rule reaches only **0.1649** there against its published **0.0753**. [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings and `C-0055`'s own *"the geometry is published and the motif is not"* are unchanged and upstream of every number. |
| **Provenance** | `gpd/results/T-121-stacked-arm-sheet.json`, produced by `structure.StackedArmSheetStudyKt`; model in `src/main/kotlin/structure/StackedArmSheet.kt`; **7 cheap bounds, 8 condensation records over 4 regularisations × 2 tie counts, 18 solved augmented states, 10 flatness records, 21 distribution records, 6 variance records, 4 drainage records, 3 convergence sweeps, 14 upstream reproductions, 5 predicates, 4 runtime falsifiers**; **24 gate-named tests in `src/test/kotlin/structure/StackedArmSheetTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with two concurrent agents' mid-TDD files dropped by `--drop-file` (`coupling/BuildableStiffnessRatioStudy.kt` — a **main** source — `coupling/BuildableStiffnessRatioTest.kt` and `anchoring/TorsionFeasibleRoutingTest.kt`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `η = 8.5406e−4 Pa·s` at 300 K; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, 8 symmetrically centred crossover columns (56 crossovers); `C-0055`'s **own** 34-arm upward placement, read from its result file, at `C-0039`'s **8.164 nm** arm; `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V; `C-0017`'s **33.3333 pN/nm** mandate; `C-0001`'s foundation secant for the loads and its working-point tangent for the variance; `C-0004`'s 40 × 40 nm drag budget |
| **Consumes** | [`C-0055`](C-0055-unused-junction-site.md) (**the placement itself**, read from `gpd/results/T-119-unused-junction-site.json`; the 34, the 8.164 nm arm, the 10.88 nm upward root pitch, the 0.46 plan fraction), [`C-0009`](C-0009-discrete-lattice-tile.md)/`OrigamiGrillage` (**the grillage, augmented rather than replaced** — every response read through its own `GrillageDeflection`), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`C-0047`](C-0047-single-column-flatness.md) (the flatness pipeline and its 0.218/0.695), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved profile, keyed on concentration, gap **and bias**), [`C-0010`](C-0010-tile-positional-variance.md) (the modal budget, the bandwidth split), [`C-0004`](C-0004-poroelastic-drainage.md) (the drag budget and the §4(d) margin, read from its own result file), [`C-0058`](C-0058-non-uniform-coupling.md) (`rimStiffenedWeights`, `admissibleStiffnessRatio`, the 6.70 nm collar, the 0.0753 — **re-run as libraries**), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate), [`C-0035`](C-0035-flexure-mounting-sense.md) (the mounting the arms interact with) |
| **Raises** | [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md), against `C-0058`'s station set |

---

## The claim, in one line

**`C-0055` worried that 34 duplexes above the tile were rigidity nobody had modelled; they are not rigidity at all, because a lever held at one point is a mechanism and a mechanism stores no energy — and the same 10.88 nm root pitch that cut the escape from 60 arms to 34 is what makes a second tie impossible, so the harmlessness is not an approximation but a consequence.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rigidities **pN·nm²**, foundation stiffness
  **pN/nm³**, pressure **pN/nm²** (= 1 MPa exactly), drag **pN·s/nm**, mass **pN·s²/nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` **along** the helices, `y` **across** them, `z` **normal** and positive **upward** — away from
  the grafted layer, which is below the tile (`C-0055`);
  `w` positive **downward**, compressing the layer (`C-0006`, `C-0009`).
- An **arm** is a duplex lying **parallel** to its host, one interhelical distance above it, held by
  **one** antiparallel crossover.
  It is *not* a duplex standing normal to the sheet — the crossover motif requires parallel
  helices, and that is `C-0055`'s reading of Ke et al. as well as `C-0029`'s standing finding.
- An **upward root** is a site of the `+z` column lattice at pitch `p = 32 bp = 10.88 nm`,
  because an upward site belongs to **one** duplex and is shared with nobody.
- A **tie** is one crossover between an arm and its host.

---

## The cheap bounds, which ran first and decided the verdict

| | bound | value | what it settled |
|---|---|---|---|
| **1** | the added stiffness a body attached at **one** point contributes at its root | **0.000 pN/nm** (`2.0e−10` at `ε = 1e−10`, and `∝ ε`) | **exactly zero, by condensation** — the rigidity channel closed before any lattice ran. **Falsifier 1 did not fire** |
| **2** | the arm length against the upward root pitch | **0.750** (8.164 / 10.88) | **the single tie is forced by the lattice**, at 34 paths and at §3's 45 (0.839). **Falsifier 4 did not fire** |
| **3** | the arm length at which the arm's **own bending** is engaged at all | **21.76 nm** | three ties, **2.67×** `C-0055`'s arm — so even the two-tie counterfactual adds no bending |
| **4** | the plan footprint fraction, **identically** the contour fraction | **0.4626** | reproduces `C-0055`'s 0.46; the two are one number, because an arm's footprint *is* its host's strip |
| **5** | the quality factor with the arm mass added | **1.209×** `C-0004`'s `5.3e−4` | still overdamped by three orders; the mass channel closed |
| **6** | the arms' drag as a fraction of `C-0004`'s total | **9.1 %** | **the one channel that moves anything**, and it is an upper bound. **Falsifier 5 did not fire** |
| **7** | what a genuinely tied second layer would carry, `2EI + Sδ²/2` | **19.3×** one duplex's `EI` | the far end of the bracket, reported and **not adopted** |

> **The bounds decided the order of work and were right.** Rigidity **exactly zero**, inertia
> `1e−12` of the actuation load, drag 9.1 % of a budget with 22.8× margin. Only the third
> moves a number, and it moves the one quantity in this programme that is quoted **with a
> bandwidth**.

---

## Deliverable 1 — why it is exactly zero, and what it is a zero OF

**The identity.** The arm's energy is `½uᵀKu` with `K` positive *semi*-definite: in the grillage's
out-of-plane kinematics its null space is three-dimensional — a translation `w = 1`, a pitch
`w = x`, and a roll `φ = 1` — those being its rigid-body motions. One crossover constrains exactly
two host degrees of freedom at the root, the deflection through the vertical link and the roll
through `k_θ`, and **both are spanned by that null space**. So for any motion the host imposes
there is a zero-energy arm motion that follows it, the minimised arm energy is identically zero, and
`K_hh − K_ha K_aa⁻¹ K_ah` vanishes term by term.

**It is the same class of statement as** *"a uniform load on a uniform Winkler foundation dishes
exactly zero"*, and it is checked the same way — as a **runtime falsifier** the study refuses to
emit a result file without.

**The three-step bracket, and none of the steps is large:**

| ties | added deflection stiffness [pN/nm] | added roll stiffness [pN·nm/rad] | peak dishing under `C-0022`'s load | against the bare sheet |
|---|---|---|---|---|
| **1 — `C-0055`'s motif** | **0** | **0** | **1.070841 nm** | **`5e−9`** |
| **2 — the counterfactual** | **0** | **5.832** | 1.072041 nm | **+0.112 %** |
| 3 or more | the arm's own `EI` enters | — | not evaluated | needs **21.76 nm** of arm |
| a tied, **axially coupled** second layer | — | — | not in these kinematics | **19.3×** `EI` |

**Two ties add a torsion bar and nothing else, and that is exact.** 5.832 pN·nm/rad is the two
crossover hinges in series with the arm's own `GJ/L`: `(2/13.529 + 10.88/460)⁻¹`. There is **no**
added bending stiffness at two ties, at any arm rigidity, because **two points determine a line and
a rigid arm meets both**. The arm's `EI` is engaged only at three ties.

**And the lattice forbids two.** `C-0039`'s elastica gives the arm as a function of path count, and
the upward root pitch is fixed at 32 bp:

| path count | arm [nm] | arm / pitch | second root reachable? |
|---|---|---|---|
| 20 | 6.641 | 0.610 | **no** |
| **34 — `C-0055`'s self-consistent count** | **8.164** | **0.750** | **no** |
| 42 | 8.882 | 0.816 | **no** |
| **45 — §3's own path count** | **9.131** | **0.839** | **no** |

> **The escape and its price and its harmlessness are the same fact, three times.** An upward site
> costs the sheet nothing *because* it belongs to one duplex; its roots are twice as sparse
> *because* it belongs to one duplex; and the arm it roots can never be tied twice *because* the
> pitch that makes them sparse is longer than the arm. `C-0055` found the first two and named the
> third as the open question.

---

## Deliverable 2 — `C-0009`'s grillage, re-run carrying the array

Augmented assembly: `C-0009`'s 855 host degrees of freedom plus 6 per arm, in **one** stiffness
matrix (1059 in total), the applied load recovered exactly as `f = K_host q_host` and the arms
carrying **none** of it — the electrostatic load acts on the tile's *underside* and the arms stand
on the other face. Under `C-0022`'s solved profile, `C-0015`'s 3 × 15 coupling:

| configuration | peak dishing [nm] | peak crossover force [pN] | peak duplex shear [pN] | arm strain energy [pN·nm] |
|---|---|---|---|---|
| **BARE — `C-0009`'s sheet** | **1.0708408** | **0.1503503** | **0.7925145** | — |
| **ARMED — 34 arms, one crossover each** | **1.0708408** | 0.1503503 | 0.7925145 | **0** |
| TIED — the same 34 at two crossovers each | 1.0720409 | 0.1522338 | 0.7920787 | `5.8e−5` |

&nbsp;&nbsp;&nbsp;&nbsp;**Nothing moves.** The armed row is the bare row to eight significant
figures, and what separates them is the arm regularisation, which vanishes linearly.

---

## Deliverable 3 — `C-0006`/`C-0047`'s flatness, and the one verdict that moves

`C-0022`'s solved profile; `C-0017`'s 33.3333 pN/nm held fixed in every row; dishing over the free
tile's own stroke; `T-5b`'s convention is **0.10**.

| placement | stations | **dishing / stroke** | flat? | peak path force [pN] | peak crossover [pN] |
|---|---|---|---|---|---|
| **NONE — free tile** | 0 | **0.3079** | no | — | 0.244 |
| **ROOTS — `C-0055`'s own 34, as placed** | **34** | **0.4156** | no | 2.700 | 1.255 |
| ROOTS-MIRRORED — the same 34, odd rows reflected | 34 | **0.3558** | no | 2.771 | 1.198 |
| **GRID — `C-0015`'s 3 × 15** | 45 | **0.2182** | no | 1.943 | 0.150 |
| COLUMN — `C-0041`'s 1 × 15 | 15 | **0.6952** | no | 4.008 | 0.209 |

**Three things, and two of them are new:**

1. **A uniform coupling on the buildable stations is a net dishing SOURCE** — 1.35× the free
   tile. That is the pathology `C-0058` reports at `C-0041`'s 1 × 15 (1.96×) appearing at the
   *largest* placement this programme has.
2. **The peak crossover force is 8.3× larger at the arm roots than on the 3 × 15 grid** (1.255
   against 0.150 pN). It is still 8× clear of the 10–15 pN unzip band, but the direction is the
   one `C-0009` warns about: a coupling that enters on the crossover column lattice loads that
   lattice, where a coupling on an inset grid does not.
3. **`C-0055`'s placement is not centro-symmetric, and nothing upstream noticed.** Its scheduler
   fills every row greedily from the low-`x` end and points every arm the same way, so the
   coupling centroid sits at **`x = −8.80 nm`** on a tile that runs −20 to +20. Reflecting the
   odd rows is free, lands on the same column lattice, is inside `C-0055`'s own per-row
   independence, and is worth **0.4156 → 0.3558**.

### `C-0058`'s rim family, on the stations the array supplies — [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md)

| station set | uniform | ×2 | ×3 | **×5** (`C-0058`'s rule) | ×8 | ×12 | ×20 |
|---|---|---|---|---|---|---|---|
| **`C-0015`'s 3 × 15, 45** | 0.2182 | 0.1415 | 0.1076 | **0.0753 — flat** | **0.0709** | 0.0849 | 0.0970 |
| **`C-0055`'s 34 roots** | 0.4156 | 0.3015 | **0.2902** | 0.3419 | 0.3781 | 0.4012 | 0.4216 |
| the same, mirrored | 0.3558 | 0.2296 | **0.1649** | 0.2250 | 0.2674 | 0.2947 | 0.3190 |

&nbsp;&nbsp;&nbsp;&nbsp;**The best the flat family reaches on a station set a placement claim
actually supplies is 0.1649 — 2.2× `C-0058`'s headline, 1.65× `T-5b`'s tolerance, and above
`CH-0034`'s 0.149 saturation floor for equal springs on a grid.** Every design in the table is
inside the 10 pN unzip allowable (2.94–4.62 pN per path), so this is not a force limit; it is the
placement. `C-0058`'s own sentence — *"a distribution cannot repair a placement"* — applies to its
positive result as well as to its negative one.

---

## Deliverable 4 — `C-0010`'s variance, broadband and in band

Foundation at `C-0001`'s working-point tangent, `C-0009`'s lattice rather than `C-0006`'s plate:

| placement | piston | tilt | dishing | **area RMS** | centre | corner [kHz] | **in-band RMS** | margin vs 3.0 nm |
|---|---|---|---|---|---|---|---|---|
| free tile, **no arms** | 0.2775 | 0.3924 | 0.9664 | **1.0793** | 0.8215 | **91.2** | **0.0902** | 2.78× / 33.3× |
| free tile, **34 arms** | 0.2775 | 0.3924 | 0.9664 | **1.0793** | 0.8215 | **82.9** | **0.0946** | 2.78× / 31.7× |
| 34 arm roots, no arms | 0.2302 | 0.3240 | 0.8777 | 0.9635 | 0.7476 | 91.2 | 0.0805 | 3.11× / 37.3× |
| **34 arm roots, 34 arms** | **0.2302** | **0.3240** | **0.8777** | **0.9635** | **0.7476** | **82.9** | **0.0844** | **3.11× / 35.5×** |
| `C-0015`'s 3 × 15, 34 arms | 0.2187 | 0.3134 | 0.8862 | 0.9651 | 0.7179 | 82.9 | 0.0846 | 3.11× / 35.5× |

&nbsp;&nbsp;&nbsp;&nbsp;**The broadband variance is IDENTICALLY unchanged, and that is a theorem
rather than a small number: equipartition is `k_BT K⁻¹` and the arms change no entry of `K`.**
A drag and a mass cannot enter a thermodynamic average at all. **What the arms move is the
BANDWIDTH**, through the drainage corner, and the in-band amplitude therefore rises by
**4.9 %** — `√((2/π)arctan(1/82.9) / (2/π)arctan(1/91.2))`.

> **`C-0010`'s `σ_RMS ≤ 3.0 nm` predicate (leaf `A1.2`) does not move.** It was the most exposed
> standing verdict and it is untouched: 2.8–3.1× margin broadband, 32–37× in band.

**And `C-0010`'s own `D_⊥` insensitivity is confirmed from a fourth direction.** `C-0056` found
it *strengthens* at the connectivity ceiling (3.1 % → 0.99 %) while the amplitude rises 1.71×;
here the perturbation is **added out-of-plane rigidity** rather than removed in-plane rigidity, and
the sensitivity is **exactly zero** — because the added rigidity is exactly zero. The insensitivity
survives a perturbation of the opposite sign, and it survives it trivially.

---

## Deliverable 5 — the drainage, which is the genuinely new part

**The arms are on the DRY side.** `C-0004`'s squeeze-out is a **footprint** problem —
`τ = ηG/(kMf)`, the thickness cancelling except through the Brinkman wall correction — and the
footprint is the tile's *underside*, which the array does not touch. `C-0055`'s escape costs the
host no duplex length, so the underside is the same 40 × 40 nm it was. **The Brinkman
transmissivity, the drainage factor `G`, the poroelastic diffusivity and the lateral/vertical
crossover are all unchanged, identically.**

What the arms add is bulk dissipation on the upper face, in parallel with the tile's own Stokes
drag. Slender-body transverse drag, Tirado & García de la Torre, at `p = L/2a`:

| `C-0004` design point | arm radius | squeeze drag | Stokes | **arm drag** | **arm share** | corner [Hz] | **§4(d) margin** |
|---|---|---|---|---|---|---|---|
| **nominal, 10 nm layer, `k_f ×1.0`** | 1.00 nm | 1.2587e−5 | 3.084e−7 | **1.293e−6** | **9.1 %** | 91232 → **82920** | 91.23× → **82.92×** |
| the same | 0.89 nm | — | — | 1.229e−6 | 8.7 % | 91232 → **83258** | 91.23× → **83.26×** |
| **worst 40 × 40 nm, `k_f ×0.25`** | 1.00 nm | 1.2587e−5 | 3.084e−7 | 1.293e−6 | 9.1 % | 22808 → **20730** | **22.81× → 20.73×** |
| the same | 0.89 nm | — | — | 1.229e−6 | 8.7 % | 22808 → **20821** | 22.81× → 20.82× |

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0004`'s §4(d) discharge stands, with 20.7× rather than 22.8×.**

**It is an upper bound, and the lower bound is exactly zero**, for two reasons that run the same
way: an arm one duplex diameter above a translating plate sits in fluid that is already moving with
it, and 34 arms at a 2.69 nm row pitch screen one another. The pair is quoted rather than a single
number, per this programme's discipline on brackets.

---

## Deliverable 6 — the interaction with `C-0035`, stated rather than left implicit

`C-0035` settled the flexure mounting as a **product of two binaries** and found exactly one
buildable survivor: *"the standoff bases stand on the OUTPUT SUPERSTRUCTURE, the standoffs point
AWAY from the tile, the flexure is OUTBOARD of its own ground, and each midspan is tied back DOWN
through that ground to the tile"* — and it rejected the mounting that puts the flexure **under the
tile, inside the actuation gap**, where the 45-flexure array alone occupies 37–85 % of the polymer
layer's volume.

**`C-0055`'s upward arms put an out-of-plane element back on the tile, and they put it on the side
`C-0035` left free.** Three consequences, none of which is a contradiction and none of which was
stated before:

1. **They do not re-open `C-0035`'s rejection.** That rejection is about the `−z` half-space: the
   layer, the electrode and the gap. An upward arm is at `z = +2.69 nm`, occupies **zero** of the
   polymer layer's volume, and contributes nothing to any electrostatic, osmotic or squeeze-film
   quantity in this programme. Every number in `C-0035`'s occupancy table is untouched.
2. **They compete for the space `C-0035`'s survivor uses.** Its standoff bases stand on the output
   superstructure and its ties come *down* through that ground to the tile; the arm array occupies
   a slab **1.69 to 3.69 nm** above the sheet surface over **46.3 %** of the plan. Whether the
   tie-down path and the arm slab can coexist is a **clearance** question with a stated geometry
   and no solve here.
3. **`C-0035` records that the tile now carries no out-of-plane element at all.** That statement
   is no longer true of a design that adopts `C-0055`'s escape, and the difference is exactly the
   34 arms priced here — which, structurally, is nothing.

---

## The five verification gates

Executed as **24 gate-named tests** in `src/test/kotlin/structure/StackedArmSheetTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with two concurrent agents'
mid-TDD files dropped by `--drop-file` (`coupling/BuildableStiffnessRatioStudy.kt` — a **main**
source — `coupling/BuildableStiffnessRatioTest.kt` and `anchoring/TorsionFeasibleRoutingTest.kt`).

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the plan fraction is an area over an area and is invariant under a common length rescaling to `1e−12`; the composite rigidity scales as `λ⁴`; the slender-body drag is linear in the viscosity and in the length at fixed aspect ratio; unphysical arguments throw at **thirteen** entry points, including a body no longer than its own diameter, a tie count of three, a zero regularisation, an arm row outside the sheet, and **an arm root that does not land on a host node — refused rather than snapped** | **PASS** |
| **2 — limiting cases** | **zero attached arms reproduces `C-0009`'s grillage exactly** (departure `< 1e−9` nm on a 4.9 nm deflection, and the mean deflection to `1e−9` relative); **a uniform load on a uniform Winkler foundation dishes exactly zero with and without the arms**, and the armed residual vanishes **linearly** in the regularisation; the one-tie condensation is zero and the two-tie one is not; the arm is shorter than the root pitch at 34 paths **and at §3's 45**, and reaches it exactly at the pitch | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the arm regularisation over `1e−5 … 1e−9` — the host departure is `4.84e−5 → 4.84e−7 → 4.86e−9`, **linear to `1.0e−4` over four decades**, which is the numerical signature of an exact zero rather than a small one; nested mesh `1 ⊂ 2 ⊂ 4` (`5.2e−5`); the crossover link penalty over `1e3 … 1e5` (`1.7e−5`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **14 reproductions, worst strict departure `0`**: `C-0055`'s 34, its 10.88 nm pitch (exactly), its 8.164 nm arm (`4.8e−5`) and its 0.46 plan fraction (`5.8e−3`); `C-0047`'s **0.218** and **0.695** (`9.8e−4`, `2.9e−4`); `C-0022`'s **0.308** (`3.2e−4`); `C-0004`'s **91231.5 Hz** (`4.2e−7`) and its 0.70 % in-band fraction; `C-0058`'s **0.0753** (`5.8e−4`) and its 4.5 admissible ratio (exactly); `C-0009`'s `EI`, `k_θ` and the SAXS 2.69 nm | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The degree-of-freedom layout this model reconstructs from `OrigamiGrillage`'s public API is
   asserted against that lattice's own `basisAt`** — the shape function at a node must be exactly
   one at the node's own `w`. Nothing forces the reconstruction to be right; the test is what makes
   it so.
2. **The arms hold no strain energy and their root crossovers transmit nothing** — asserted
   separately from the host-solution comparison, so the zero is checked on the arms' side as well
   as on the sheet's.
3. **The exact zero is a property of the attachment and not of the arm's elasticity**: a ten-fold
   stiffer arm at half and at twice the length adds the same nothing.
4. **The thermal point fluctuation of the host computed on the AUGMENTED matrix equals the bare
   lattice's own `centreRms`** — an independent route (a `bᵀK⁻¹b` on a 1059-dimensional matrix
   against a trace-based modal budget on an 855-dimensional one).
5. **Two quantities both meant to vanish are compared ABSOLUTELY**, not relatively — the armed and
   bare dishings under a uniform load are both `~3e−11` nm, and a relative comparison of them
   compares their round-off (`CLAUDE.md`).

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **1** | the condensed stiffness at one tie failing to vanish, or failing to vanish linearly | **no** | `2.0e−10` at `ε = 1e−10`, exactly `∝ ε` over four decades |
| **2** | zero arms failing to reproduce `C-0009`'s grillage | **no** | identical to the factorisation's own round-off |
| **3** | a uniform load dishing anything but zero | **no** | `< 1e−9` nm bare, `O(ε)` armed |
| **4** | the arm reaching a second upward root anywhere in the design range | **no** | 0.750 of the pitch at 34 paths and 0.839 at §3's 45 |
| **5** | the arms' drag exceeding the squeeze drag | **no** | **9.1 %** of it, and that is the upper bound |
| **6** | the flatness at the arm roots equalling the flatness on the 3 × 15 grid | **YES, and it is the finding** | 0.4156 against 0.2182, and **worse than no coupling at all** — `CH-0074` |

**A result that was not anticipated:** the two-tie counterfactual adds a **torsion bar and nothing
else**. The task was formulated expecting one tie to give zero and two to give something like a
composite beam; two ties give zero bending stiffness at *any* arm rigidity, because two points
determine a line. The bracket therefore has three steps, not two, and the interesting one — three
ties, 21.76 nm of arm — is 2.67× outside `C-0055`'s design range rather than 1.33× outside it.

**A second one:** `C-0055`'s placement is **not centro-symmetric**, and reflecting alternate rows —
one line, free, on the same lattice — is worth more flatness than `C-0058`'s whole rim rule buys on
the unreflected set.

---

## Does `C-0055`'s verdict survive?

**Entirely, and this claim closes the item it named as its largest open one.**

| `C-0055` said | this claim finds |
|---|---|
| *"34 duplexes stacked above the tile are mass and rigidity added out of plane, which no model in this programme contains"* | **the rigidity is exactly zero and the mass is worth 1.21× on a quality factor already `5e−4`.** What it did not name — **drag** — is the only channel that moves a number |
| *"`C-0054`'s tables do not apply, and nothing replaces them"* | **nothing needs to**: the host's tables are `C-0009`'s own, unchanged to eight figures |
| the 10.88 nm upward root pitch, *"the price of the same fact that removes the connectivity cost"* | **and the third consequence of that same fact**: the arm cannot be tied twice, so the exact zero is structural rather than an idealisation |
| the 8.164 nm arm at 34 paths, the 0.46 plan fraction | **reproduced** (`4.8e−5`, `5.8e−3`) |
| *"the arm's actual rotation axis is deliberately not adjudicated"* | **still not adjudicated, and it does not matter for this claim** — the condensation is zero for **any** single-point attachment, whatever the arm's orientation |
| its 34 explicit placements | **consumed as the placement**, and found to be **asymmetric** — a design variable it did not sweep |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and
  `C-0029`'s literature findings are upstream of every number, and `C-0055`'s own separation of
  *published geometry* from *undemonstrated motif* is unchanged.
- **The exact zero is a STATIC statement.** A free body attached at one point has a zero *static*
  Schur complement; its **dynamic** impedance at frequency `ω` is not zero, and that is precisely
  the drag and inertia channels, which are priced separately here. The zero is exact in the
  quasi-static regime the whole programme works in, and `C-0004`'s `Q = 5e−4` is what licenses it.
- **The kinematics are `C-0009`'s out-of-plane ones**, so the arm exchanges a transverse force and
  a roll moment with its host and nothing else. An **axially** coupled second layer — the 19.3×
  composite — is outside this model, and it is also outside the motif: it needs many ties and the
  crossover's in-plane constant `k_s`, which `Gen1Tile` flags as a **construction, not a
  measurement**. It is reported as a ceiling and not adopted.
- **The regularisation is a spring to GROUND**, so at finite `ε` the arms are vanishingly weak
  anchors. Every result is quoted at `ε = 1e−9` with the linear convergence recorded; the exact
  constraint is the `ε → 0` limit.
- **The arm roots land on the host's node stations exactly at the nominal 8-column layout**, which
  is a convenience of that layout and is asserted rather than assumed — a root that does not land
  on a node is **refused**. `C-0055`'s own best phase carries **49** interface crossovers (seven
  columns), not 56; the grillage here is run at the nominal 8-column layout so that zero arms
  reproduces `C-0009` exactly, and the seven-column phase is **not** swept.
- **The drag is an isolated-cylinder upper bound.** Wall entrainment and mutual screening both
  reduce it and neither is modelled; the honest bracket is `[0, 9.1 %]`.
- **The mass uses 650 Da/bp** and ignores hydration and counterions. It enters only the quality
  factor, through a square root, and a 10 % error in it is invisible.
- **`C-0004`'s drag budget is read at its own 40 × 40 nm design points**, so the whole `τ ∝ L²`
  footprint scaling it establishes travels unchanged; nothing here re-derives it.
- **The flatness and variance are at ONE state** — `C-0022`'s 2 mM, 10 nm, 0.192 V solve.
  `C-0058` shows a rim design flat at three of five solved states and dishing 0.187 at the 2 nm
  gap; the arm-root placements are **not** swept over `C-0022`'s other states.
- **The distribution search is `C-0058`'s one-parameter family plus a seven-point ratio sweep.**
  No 34-parameter optimisation is run; `C-0058`'s own was worth a further 27.8 % on its grid, which
  applied to 0.1649 would reach 0.119 — still outside `T-5b`'s 0.10, but the search is not done.
- **Static, single-layer, linear**, exactly as `C-0009`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 34-arm placement, the 8.164 nm arm, the 10.88 nm upward root pitch | — | **`C-0055`**, read from `gpd/results/T-119-unused-junction-site.json` and reproduced here |
| the solved edge profile | depth −0.3029 over 8.939 nm, rim −0.5939 over 1.0 nm | **`C-0022`**, read from `gpd/results/T-3b-tile-edge-load-profile.json` |
| the drag budget and the §4(d) margins | 1.2587e−5, 3.084e−7 pN·s/nm; 91231.5 Hz; 91.23×, 22.81× | **`C-0004`**, read from `gpd/results/T-7-poroelastic-drainage.json` |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) / Wang et al. (1997) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| crossover spacing per interface | 32 bp | **CITED**, Rothemund, *Nature* **440**:297 (2006), via `C-0015`/`C-0040` |
| duplex steric radius | 1.0 nm (0.89 nm swept) | **CITED**, the B-DNA phosphate radius, via `C-0029`/`CLAUDE.md` |
| slender-body transverse drag `4πηL/(ln p + ν_⊥)` | `ν_⊥ = 0.839 + 0.185/p + 0.233/p²` | **CITED**, Tirado & García de la Torre, *J. Chem. Phys.* **71**:2581 (1979) and **73**:1986 (1980) — **PRIMARY SOURCE NOT RE-READ HERE**, and used only as an upper bound whose lower bound is zero |
| duplex mass per base pair | 650 Da | **CITED**, the standard double-stranded value |
| solvent viscosity at 300 K | 8.5406e−4 Pa·s | **`C-0004`** |
| `C-0047`'s, `C-0022`'s and `C-0058`'s published flatness numbers | 0.218, 0.695, 0.308, 0.0753, 4.5 | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM, 1 kHz | **CITED** |

Everything else — the condensation identity and its three-step bracket, the augmented assembly and
every response on it, the length inequalities and the 21.76 nm three-tie threshold, the composite
ceiling, the mass and drag budgets, the arm-root flatness table, the mirrored placement, the rim
family on both station sets, the variance budget in band, and the `C-0004` margins with the arms —
is **derived here in code**, with `C-0009`'s, `C-0022`'s, `C-0047`'s, `C-0055`'s and `C-0058`'s
pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **A placement search over the upward lattice.** The mirrored set was found in one line and beat
   `C-0055`'s own by 14 %; the row phases are a free variable nobody has swept, and `C-0055`'s own
   gate-3 finding (the per-row problems are independent) is what makes the sweep cheap.
2. **A full 34-parameter distribution optimisation on the arm roots**, and the same on the mirrored
   set. `CH-0074` states what the one-parameter family reaches; it does not state the optimum.
3. **The seven-column phase.** `C-0055`'s best upward phase carries 49 interface crossovers, and
   every grillage number here is at the nominal eight-column 56.
4. **The clearance between the arm slab and `C-0035`'s tie-down path.** Stated with a geometry
   (1.69–3.69 nm above the sheet, 46.3 % of the plan) and not solved.
5. **The arms' drag with wall entrainment and mutual screening.** The bracket is `[0, 9.1 %]` and
   the upper end is what is quoted; a Brinkman-style calculation of an array of cylinders above a
   translating plate would narrow it, and no verdict depends on it.
6. **The dynamic impedance of a free lever.** Exactly zero statically; at 1 kHz it is the drag and
   inertia priced here, but the arm's own first bending mode is not computed, and a resonance
   inside the operating band would be a different statement. `C-0004`'s `Q` makes that unlikely
   and does not make it checked.

## Challenges

**Raises [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md)**
against `C-0058`'s station set. **No number in `C-0055`, `C-0009`, `C-0047`, `C-0022`, `C-0058`,
`C-0010` or `C-0004` fails to reproduce** — 14 reproductions, worst strict departure zero.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that an upward arm must be tied at more than one site to fold at all.** That
   is the multilayer motif; it would make the arm rigid rather than free and is the same objection
   that stands against every `E5` element. It would also put the two-tie row of the bracket in
   play, which is 0.112 % of the dishing.
2. **A measurement of a crossover's in-plane (axial-shear) constant.** `Gen1Tile` flags it as a
   construction. A stiff one would open the axially coupled second-layer channel — but only at two
   or more ties, which the pitch forbids.
3. **A drag calculation showing the arms add more than an isolated cylinder does.** Both physical
   effects available (wall entrainment, mutual screening) run the other way; a mechanism that
   *increased* the drag above the isolated bound would be new.
4. **A placement of the array that is not on the upward column lattice.** The exact zero would
   survive it — it depends on nothing but the tie count — but every flatness number here would move.
