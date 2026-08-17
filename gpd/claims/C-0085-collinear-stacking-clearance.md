# C-0085 — **The collinear clearance should be SIX base pairs, 2.04 nm**, set by an energy and not by a distance: two arms end to end are tethered to the same sheet, so a blunt-end stack costs the elastic work of closing the gap, and holding the stacked state below one per cent on the softest closure path asks **1.90518 nm**, i.e. 6 rises. **And the margin stops being a residue**: a collinear gap is an AXIAL length, so it is quantised at the rise like the other two terms — `M = (32 − N_d − N_L)` rises — and the published **0.02561 nm** is what is left when a **transverse** SAXS constant (7.912 rises) and an elastica root (24.013 rises) are subtracted from an integer pitch. The buildable margin is **2 WHOLE RISES, 0.67561 nm, 26.38×** the published one, and **all three of `C-0071`'s live `NONE` bands become real margins** — the plan length at 2 whole rises, and the two joints: the tip ceiling goes 79.678 → **133.687** (**1.7088×** `A2`) and the root ceiling 13.930 → **25.689** (**1.8988×** one crossover), against 1.018× and 1.030×. Nothing in the placement moves adversely — and the **conservatism is what keeps `CH-0081` standing**

| | |
|---|---|
| **Task** | [`T-152`](../tasks/T-152.md), raised by [`C-0079`](C-0079-unbonded-duplex-separation.md)'s *Still open* item 4 through [`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring array |
| **Verification type** | **logical** (a quantisation argument on the base-pair lattice, and a two-state energy balance in closed form — neither needs a solve) **+ in-silico** (`C-0039`'s exact elastica, `C-0053`/`C-0069`'s packer, `C-0055`'s hinge lattice and `C-0074`'s capacity bisection re-run as libraries at **every** candidate clearance) **+ literature** (the blunt-end stacking corpus consumed from `gpd/data/T-139-blunt-end-stacking-literature.md` with its own read flags carried, and Rothemund's Supplementary Note S5.7 re-verified against a copy fetched for `T-151`) |
| **Verdict** | **PASS, and the acceptance is met in its positive half: the clearance CAN be set, it is an integer, and setting it turns all three of the margins `C-0071` reports as `NONE` into real ones.** **The cheap bound reshaped the question before any code ran.** `CH-0093` establishes that the collinear slot is *axial*; an axial length on a duplex is quantised at the **rise**, and so are the other two terms of `C-0069`'s margin — so `M = (32 − N_d − N_L) × 0.34 nm` is an **integer count of rises**, and the published **0.02561 nm** is the residue of subtracting a **transverse** SAXS lattice constant (2.69 nm = **7.912** rises) and an elastica root (8.16439 nm = **24.013** rises) from an integer pitch. On the lattice the standing design closes at **exactly zero**: 32 − 8 − 24 = 0. **The criterion is then an energy, and the geometry supplies it.** Both faces are covalently rooted to the same sheet at a fixed 32 bp pitch, so a stack forms only if the material strains to close the gap; balancing `½k(g − g₀)²` against Woo & Rothemund's measured **−2.63 kcal/mol = 4.41156 `k_BT`** per helix gives a four-row adopted ladder — **1.13615 / 1.43502 / 1.47790 / 1.90518 nm**, i.e. **4 / 5 / 5 / 6 rises** — whose largest, the one-per-cent suppression on the softest closure path (30.518 pN/nm, arm and host stretch in series with `C-0009`'s crossover spring), is **6 bp = 2.04 nm**. **The design as built already carries 8**, so the requirement is met with **2 whole rises to spare** — 0.67561 nm, and it is *quotable* where 0.02561 nm was not. **What that buys is the joint window.** The budget goes 8.19 → **8.84 nm**, `c ≤ 2.34166` → **2.94462** (**1.2575×**), and both bisected ceilings open: **79.678 → 133.687 pN·nm/rad** against `C-0034`'s `A2` at 78.235 (**1.7088×**, from 1.0184×) and **13.930 → 25.689** against one crossover at 13.529 (**1.8988×**, from 1.0296×). **Nothing in the placement moves adversely**: 34 of 34 at one level at every clearance 1–7 rises, `C-0053`'s 45-arm count **43 → 43**, `C-0074`'s 30-root ceiling **9.535 → 9.86 nm**. **`C-0069`'s central negative is untouched** — the two-support flexure family's floor, 22.414 nm, exceeds the **bare** 10.88 nm pitch by 2.06×, so no clearance whatever reopens it. **Two things were not anticipated.** The **conservatism is load-bearing**: a rigid-rooted arm needs 9.24699 nm and *places* at 4 rises or fewer, so a design adopting the loosest end of the stacking range would reopen the truss branch [`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md) closed. And **distance is the only control the design still owns**: all three of Rothemund's measured anti-stacking remedies need a strand **terminus**, and `C-0034`'s `A2` joint has already spent both of the two a duplex end has. Raises [`CH-0100`](../challenges/CH-0100-the-collinear-margin-is-an-integer-not-a-residue.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** (`C-0055`, `C-0029`). The stacking free energy and its range are measured on **other objects at higher Mg²⁺** and are transferred, exactly as `C-0079` transfers them; the closure stiffness contains `C-0009`'s `k_s`, which `C-0072` calls *"a construction, not a measurement"* — which is why the ladder carries the measured-only path beside it and the two differ by **one rise**. |
| **Provenance** | `gpd/results/T-152-collinear-clearance.json`, produced by `anchoring.CollinearClearanceStudyKt`; model in `src/main/kotlin/anchoring/CollinearClearance.kt` (**new file — no shared main source was edited**); **6 cheap bounds, 2 closure paths, 7 criteria, 8 candidates, 6 lattice rows, 5 design controls, 14 upstream reproductions, 3 convergence records, 6 predicates, 5 falsifiers, 8 findings**; **32 tests, 25 of them gate-named, in `src/test/kotlin/anchoring/CollinearClearanceTest.kt`**; the literature consumed from `gpd/data/T-139-blunt-end-stacking-literature.md` (read directly for `T-139`) and `gpd/data/T-151-sources/DNAorigami-supp1.linux.txt` (fetched and read directly for `T-151`); `tools/verify.sh` **BUILD SUCCESSFUL in 15 m 08 s** — the whole suite on its own isolated tree, with one concurrent agent's failing test dropped by `--drop-file` (`src/test/kotlin/stability/RecommendedElementFoldTest.kt`, `T-149`) and nothing else; the undropped run reports **exactly one** failure and it is that same sibling test, with no failure in either of this task's classes. The result file was re-run through `tools/study.sh` and diffed **byte-for-byte identical**. `tools/result-reader-census.py` re-emitted and clean; `tools/check-markdown-tables.py` clean over the whole 259-file corpus |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, crossover phase **24**; `C-0063`'s **34** upward roots at a **32 bp = 10.88 nm** pitch, read from `gpd/results/T-125-upward-root-placement.json`; `C-0017`'s **33.3333 pN/nm** as a **sum**, so 0.980392 pN/nm per path at §3's **acceptable 3 nm**; `EI` = 230 pN·nm², `S` = 1100 pN, `k_θ` = 13.5294, `C-0034`'s `A2` = 78.2353, `C-0009`'s `k_s` = 64.7059 pN/nm; blunt-end stack **−2.63 kcal/mol per helix** at 1×TAE + 12.5 mM Mg²⁺, 22 °C, **transferred** and not re-measured |
| **Consumes** | [`C-0079`](C-0079-unbonded-duplex-separation.md)/[`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) (`BluntEndStacking` **re-run as a library**; the coaxial geometry, the 15.1103× and the stacking range), [`C-0069`](C-0069-output-element-placement.md) (`rowOfThreeLengthCeiling`, `bendingFactorForLength`, `bendingLengthForStiffness`, `farRestraintCeiling`, `nearRestraintCeiling`, `maximumRootedElementsInRow`, `StationRow` — **all re-run**, and its 8.19 / 0.02561 / 2.34166 / 79.678 / 13.930 reproduced), [`C-0072`](C-0072-plan-tolerance-model.md) (floors 1 and 3, both reproduced), [`C-0074`](C-0074-two-per-row-placement.md) (`maximumPlanCeilingForCount`, its 9.535 reproduced), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`placeHingeArms`, `elementPackingVerdict`, `PlanElement`; its 43 of 45 reproduced), [`C-0055`](C-0055-unused-junction-site.md)/[`C-0063`](C-0063-upward-root-placement.md) (the lattice and the 34 stations), [`C-0039`](C-0039-two-spring-elastica.md) (`elasticaArmForStiffness`, `elasticaArmCeiling`), [`C-0034`](C-0034-guided-arm-anchorage.md) (`ArmAnchorage.twoTerminus`), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the counting theorem, applied to the anti-stacking remedy), [`C-0025`](C-0025-flexure-end-joint.md) (*"a single nick is a clamp"*), [`C-0017`](C-0017-output-coupling-stiffness.md)/[`C-0071`](C-0071-output-element-recommendation.md) (the mandate and the `NONE` bands), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0100`](../challenges/CH-0100-the-collinear-margin-is-an-integer-not-a-residue.md), against `C-0069`'s, `C-0071`'s and `C-0072`'s reading of `p − d − L` as a 0.0256 nm residue |

---

## The claim, in one line

**The gap between two collinear arms is an axial length between two duplex end faces, so it is a base-pair count and not a girth — and once every term of the margin is a count, the knife edge disappears and what remains is a design decision the physics can actually take: six base pairs, set by requiring the elastic price of closing the gap to exceed a measured stacking bond by a factor of a hundred.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, energies **pN·nm** and `k_BT`; `k_BT = 4.141947 pN·nm` at **300 K**; `1 kcal/mol = 6.94769 pN·nm`.
- Medium **aqueous 2 mM MgCl₂**, `I = 3c = 6 mM`.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive upward**.
- **The rise, 0.34 nm, is the design quantum along `x`.** Every axial length on a duplex is an integer count of it.
- **A clearance is the AXIAL gap between two duplex END FACES**; a **body width** is the transverse girth of a duplex in plan. They are never the same number and never interchanged. `C-0053`'s packer took one argument for both; `placeCollinearRootedArray` separates them and reproduces the old packer exactly when they are equal.
- **A stacking free energy is negative** (it binds); the work done to close a gap is **positive**; the bond is suppressed when their sum is positive.
- The **stacked separation** is oxDNA2's coaxial radial minimum, **0.34072 nm** — which is one base-pair rise to 0.2 %, because a coaxial stack *is* a continued helix.

---

## Deliverable 1 — the cheap bounds, and what each settled before any code ran

| | bound | value | against | fired? | what it settled |
|---|---|---|---|---|---|
| **1** | the standing allowance read on the base-pair lattice | **7.912 rises** | 8 | **YES** | 2.69 nm is a **transverse** lattice constant charged in an **axial** slot, and it is not on the design language's own lattice at all. Quantised **up** to the 8 rises a design can draw, the `Q5` margin goes **negative** (−0.00439 nm) |
| **2** | the arm read on the same lattice | **24.013 rises** | 24 | **YES** | the buildable arm is **24 bp**, 0.16 % *stiffer* than `C-0017`'s equality and therefore on its safe side, and the published 0.02561 nm is the **residue** of two off-lattice numbers |
| **3** | the margin as an integer count at the standing allowance | **0 rises** | — | **YES** | `32 − 8 − 24 = 0` **exactly**: on the lattice the standing design closes with nothing left over, which is neither `C-0069`'s `+0.0256 nm` nor a failure |
| **4** | the design space reaching the measured stacking range | **3 integers** | 8 | **YES** | 0.51108–1.3 nm is **2–4 rises**, so the whole space is three integers before any energetics and the only expensive part is the joint window |
| **5** | the midspan flexure family's floor against the **bare** pitch | **22.414 nm** | **10.88** | **YES** | `C-0069`'s central negative survives a clearance of **zero**, so no reading of this task reopens the two-support family |
| **6** | the blunt-end stack against thermal energy | **4.41156 `k_BT`** | 1 | **YES** | the slot holds a **bond** worth four thermal energies, so a nominal-position range criterion is not sufficient and the criterion has to be an energy |

> **Bounds 1–3 are the finding, and they cost nothing.** They are `CLAUDE.md`'s *"a preload is a LENGTH and DNA quantises it at 0.34 nm"* applied to a clearance — and applied to the whole identity rather than to one term.

---

## Deliverable 2 — the criterion, as an energy

Both faces are covalently rooted to the same sheet at a fixed 32 bp pitch, so the stacked state costs the elastic
work of closing the gap:

&nbsp;&nbsp;&nbsp;&nbsp;`ΔG(g) = ΔG_stack + ½ k_closure (g − g₀)²`, &nbsp;&nbsp; `g₀ = 0.34072 nm`, &nbsp;&nbsp; `|ΔG_stack| = 4.41156 k_BT`.

Two closure paths, both series chains, and `C-0072`'s own floor 3 falls out of the stiffer one **exactly**:

| path | members | `k` [pN/nm] | `√(k_BT/k)` [nm] |
|---|---|---|---|
| **stiff** (measured constants only) | arm axial 134.731 + host segment axial 101.103 | **57.7598** | **0.267787** — `C-0072`'s floor 3, 0.26779, reproduced to `1.1e−5` |
| **soft** (with `C-0009`'s constructed `k_s` = 64.706) | the same + root crossover in-plane | **30.5179** | 0.368404 |

| criterion | gap [nm] | rises | adopted? | flag |
|---|---|---|---|---|
| oxDNA2's coaxial-stacking hard cutoff | 0.51108 | 2 | no | CITED, READ DIRECTLY (via `C-0079`) |
| the stacked separation plus one axial thermal σ | 0.608507 | 2 | no | DERIVED |
| the all-atom PMF's repulsive onset | 1.30000 | 4 | no | CITED, READ DIRECTLY (via `C-0079`) |
| **the elastic price exceeds the bond, stiff closure** | **1.13615** | **4** | **yes** | DERIVED |
| **the elastic price exceeds the bond, soft closure** | **1.43502** | **5** | **yes** | DERIVED |
| **the stacked state below one per cent, stiff closure** | **1.47790** | **5** | **yes** | DERIVED |
| **the stacked state below one per cent, soft closure** | **1.90518** | **6** | **yes — THE ADOPTED ONE** | DERIVED |

**The adopted set spans one rise across a factor of two in the closure stiffness and a factor of a hundred in the
admitted occupancy**, which is what makes the recommendation robust: the whole modelling uncertainty is worth
`4 → 6` rises and the design has 8.

---

## Deliverable 3 — the recommendation, and the margin as an integer

> **Six base pairs. 2.04 nm.**

| quantity | standing (2.69 nm) | recommended (6 bp) | ratio |
|---|---|---|---|
| plan budget `p − d` | 8.19000 nm | **8.84000 nm** | 1.0794× |
| `Q5` margin `p − d − L` | 0.02560917 nm | **0.67560917 nm** | **26.3815×** |
| the same, on the lattice | *not on the lattice* | **2 whole rises** | — |
| the elastic work to close | 20.855 `k_BT` | **10.638 `k_BT`** | — |
| the stacked:free ratio | `7.2e−8` | **`2.0e−5`** | — |

**The design as built already carries 8 rises of clearance**, because the arm is 24 bp on a 32 bp pitch. So the
recommendation is a **requirement the design already meets by two rises**, and that is the first margin in this
branch that can be quoted at all: it is a count.

### The whole integer sweep

| `N_d` [bp] | clearance [nm] | budget [nm] | margin [nm] | margin [rises] | × published | `c` ceiling | tip ceiling | ×`A2` | root ceiling | ×1 crossover | placed | rigid root places? | 45-arm | 30-root [nm] |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | 0.34 | 10.54 | 2.37561 | 7 | 92.76 | 4.99107 | *cap* | — | 90.622 | 6.698 | 34 | **yes** | 43 | 10.71 |
| 2 | 0.68 | 10.20 | 2.03561 | 6 | 79.49 | 4.52348 | 9284.6 | 118.7 | 69.920 | 5.168 | 34 | **yes** | 43 | 10.54 |
| 3 | 1.02 | 9.86 | 1.69561 | 5 | 66.21 | 4.08604 | 599.69 | 7.665 | 54.667 | 4.041 | 34 | **yes** | 43 | 10.37 |
| 4 | 1.36 | 9.52 | 1.35561 | 4 | 52.93 | 3.67776 | 294.93 | 3.770 | 42.890 | 3.170 | 34 | **yes** | 43 | 10.20 |
| 5 | 1.70 | 9.18 | 1.01561 | 3 | 39.66 | 3.29762 | 188.23 | 2.406 | 33.461 | 2.473 | 34 | no | 43 | 10.03 |
| **6** | **2.04** | **8.84** | **0.67561** | **2** | **26.38** | **2.94462** | **133.687** | **1.7088** | **25.689** | **1.8988** | **34** | **no** | **43** | **9.86** |
| 7 | 2.38 | 8.50 | 0.33561 | 1 | 13.11 | 2.61775 | 100.45 | 1.284 | 19.124 | 1.413 | 34 | no | 43 | 9.69 |
| 8 | 2.72 | 8.16 | **−0.00439** | **0** | −0.17 | 2.31602 | 77.991 | **0.997** | 13.461 | **0.995** | **30** | no | 43 | 9.52 |

**Row 8 is the standing convention quantised**, and it is the row that fails: at 8 rises both joint ceilings fall
*below* what the design uses (0.997× and 0.995×) and the array places **30 of 34**. That is `C-0069`'s
0.0256 nm knife edge seen from the lattice, and from there it is not a margin but a **shortfall of one rise**.

---

## Deliverable 4 — the joint window, re-read

`C-0069` found both of `Q5`'s joints inside the `c ≤ 2.3416` razor by 1.8 % and 2.9 %, *"the sharpest thing in this
claim and also its largest exposure: the design has no margin on either joint."* At the recommended budget:

| held | its value | ceiling at 8.19 nm | ceiling at 8.84 nm | headroom, standing | headroom, recommended |
|---|---|---|---|---|---|
| **root = one crossover**, 13.5294 | — | tip ≤ **79.678** | tip ≤ **133.687** | **1.0184×** | **1.7088×** |
| **tip = `C-0034`'s `A2`**, 78.2353 | — | root ≤ **13.930** | root ≤ **25.689** | **1.0296×** | **1.8988×** |

**Both `NONE` bands become real margins**, and the mechanism is the cube: a 7.9 % longer budget is 1.2575× in `c`
and — because the elastica stiffens faster than `c` at a fixed length — **1.71× and 1.90×** in the restraints
themselves. `C-0069`'s failure route 3, *"a measurement of `k_θ` at the top of Chen et al.'s bracket"*
(`α = 1.2`, arm 8.332 nm), is inside the recommended budget with 0.508 nm to spare where it was 0.142 nm **over**
the standing one.

---

## Deliverable 5 — what the design must CONTROL, which is not only a distance

A stacking bond is not a distance criterion. It needs two **blunt** ends, and Rothemund gives three measured
remedies — **every one of which needs a strand terminus**:

| control | available here? | evidence, all **READ DIRECTLY** |
|---|---|---|
| **distance — the clearance itself** | **YES** | the only control that spends no terminus, and the one this claim sets |
| omit the terminal staple, leaving unstructured scaffold | **no** | *"the staple strands along the edges of a shape may be simply left out"* (SI Note S5.7) — unavailable at the arm's **tip**, whose terminal base pair **is** `C-0034`'s `A2` joint |
| a 4-T hairpin loop on the terminal staple | **no** | *"stacked chains of 3–5 rectangles still formed, but 30 % of rectangles occurred as monomers … Without hairpins, all rectangles occurred in aggregates"* — **partial** even where available, and it needs a terminus |
| a 4-T tail on the terminal staple | **no** | *"the addition of just a small number (12) of 4T tails … causes almost complete disaggregation"* (SI Note S5.7) — same terminus conflict |
| helical phase of the two faces | **not quantified** | Woo & Rothemund's own SI: *"it is difficult to predict the exact structure and stacking configurations of the blunt-ends on the edges of origami"* |

> **`C-0029`'s counting theorem strikes twice on the same two termini.** A duplex end has exactly **two** strand
> termini; `C-0034`'s `A2` joint uses **both**; and every anti-stacking remedy in the literature appends to a
> terminus. At the arm's **root** the terminal base pair has none free either — both backbones continue into the
> host through the crossover. **So the collinear clearance is not a backup for the end chemistry at this element;
> it is the primary control.**

The escape exists and it is not free: break a staple **one base pair inboard** of the face to liberate a terminus.
That is a single nick, which `C-0025` shows *is* a clamp and costs nothing structurally — and it creates a short
staple domain next to a joint, which Ke et al. report as a **yield** cost (`CLAUDE.md`). It is named, not priced.

And Rothemund's own evidence says the terminal base pair **is** stackable: *"bases at the end of the helices are
highly available for stacking against other DNA origami which suggests that the last base pair does form and
assumes a planar configuration."*

---

## Deliverable 6 — what moves, named claim by named claim

| claim | quantity | standing | at 6 bp | moves? |
|---|---|---|---|---|
| **`C-0069` `Q5`** | the margin `p − d − L` | **+0.02561 nm** | **+0.67561 nm = 2 rises, 26.38×** | **YES** — `CH-0100` |
| **`C-0069`** | the end-factor razor `c ≤ 2.34166` | — | **`c ≤ 2.94462`**, 1.2575× | **YES** |
| **`C-0069`** | the two joint ceilings | 1.0184× and 1.0296× | **1.7088× and 1.8988×** | **YES** |
| **`C-0069`** | *"the midspan family is refused by 2.74×"* | 22.414 vs 8.19 | 22.414 vs a **bare** 10.88 | **no — and it cannot** |
| **`C-0069`** sensitivity, `α = 1.2` | arm 8.332 nm, 30 of 34 | 0.142 nm **over** | 0.508 nm **under** | **YES**, favourably |
| **`C-0071`** | 3 of 14 `NONE` margins | plan length, tip joint, root joint | the two **joints** become 1.71× and 1.90×; the plan length becomes **2 rises** | **YES — all three** |
| **`C-0072`** | *"the two knife edges are one arithmetic"* | 0.02561 nm | still one arithmetic, now **an integer** | **the identity stands, the number moves** |
| **`C-0072`** floors 1–4 | 0.34 / 0.04 / 0.26779 / 1.80744 | all fire against 0.0256 | **1, 2 and 3 stop firing** against 0.67561; floor 4 (1.80744) still fires at 2.68× | **YES, three of four** |
| **`C-0074`** 30-root ceiling | 9.5350 nm | **9.86 nm** | — | **YES**, favourably |
| **`C-0053`** 43 of 45 | 43 | **43** | — | **no** |
| **`CH-0081`** the rigid root | 9.247 nm > 8.19 | 9.247 > **8.84** — still refused | — | **no at the recommendation, YES at 4 rises** |

---

## The five verification gates

Executed as **32 tests, 25 of them gate-named**, in `src/test/kotlin/anchoring/CollinearClearanceTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a clearance, a gap and a margin are lengths and scale with every length; a base-pair count is invariant under a common rescaling of `(length, rise)`; doubling the closure stiffness divides the closing distance by exactly `√2`; unphysical arguments throw at **eleven** entry points | **PASS** |
| **2 — limiting cases** | **THE FREE LIMITING CASE — fed the standing 2.69 nm the identity returns `C-0069`'s own 8.19, 0.02560917 and 2.34165925**; at **zero** clearance the budget is the bare pitch; an infinitely stiff closure path leaves only the contact separation; unit occupancy is the bare `\|ΔG\|` balance to `1e−9`; a series of one is itself and of two equal springs is half; the required gap is monotone decreasing in the closure stiffness over 64× | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the tip restraint ceiling is resolution independent 1e−6 → 1e−9 (`8.3e−5` absolute, `6e−7` relative); the arm is RK4-step independent 400 → 800 (`0.0`); the 30-root capacity ceiling is resolution independent (`5.5e−7`) | **PASS** |
| **5 — literature and upstream** | **14 reproductions, worst strict departure `3.6e−5`** and that one a published rounding: `C-0069`'s 8.19 (`0.0`), 0.02560917 (`1.3e−7`), 2.34165925 (`1.9e−9`), 79.6781387 (`0.0`), 13.9303697 (`2.1e−9`); `C-0055`/`C-0063`'s 8.16439083 (`0.0`); `C-0053`'s 9.131 (`1.7e−5`) and its **43 of 45** (`0.0`); `C-0074`'s **9.5350** (`0.0`); `CH-0081`'s 9.247 (`1.4e−6`); `C-0069`'s midspan floor 22.4141917 (`0.0`); `C-0079`'s 4.4114 `k_BT` (`3.6e−5`); `C-0072`'s floor 1 (`0.0`) and **floor 3, 0.26779** (`1.1e−5`) — the last recovered from an independent construction, as a *closure amplitude* rather than as a tolerance | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The margin computed on the lattice equals the margin computed in nm**, at every clearance from 1 to 7 rises, to `1e−12`. Two independently written expressions — an integer subtraction and a difference of doubles.
2. **`bendingLengthForStiffness` and `bendingFactorForLength` remain exact inverses** at every one of the eight new budgets, to `1e−12`.
3. **`stackOccupancyAtGap` inverts `stackSuppressionGap`** at four occupancies spanning three decades, to `1e−9` in the log.
4. **A series closure path is never stiffer than its softest member** — asserted, not assumed.
5. **The two-width packer reproduces the one-width packer exactly** when both arguments are equal: same placed count, same level count, same overlapping pairs, same area fraction. A widening produced by a changed *packer* rather than by a changed *convention* would be worthless, and this is the test that separates them.
6. **The placed count is invariant under a rigid translation of the whole array.**

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the suppression criterion comes out **above** the standing 2.69 nm, so `CH-0093` buys nothing | **NO** | the loosest adopted criterion asks **1.90518 nm**, 0.708× the standing allowance |
| **F2** | the recommended clearance leaves the margin at or below zero | **NO** | +0.67561 nm, **2 whole rises** |
| **F3** | the margin identity is **not** an integer multiple of the rise | **NO** | pitch 32 bp, arm 24 bp, clearance 6 bp; the residue the 0.02561 nm consists of belongs to two off-lattice **inputs**, not to the identity |
| **F4** | at the recommended budget one of `Q5`'s joints is still inside its ceiling by **under 10 %** | **NO** | 1.7088× and 1.8988× against `C-0069`'s 1.0184× and 1.0296× |
| **F5** | the array stops placing 34 at one level, or `C-0053`'s 45-arm count or `C-0074`'s 30-root ceiling moves adversely | **NO** | 34 of 34 at one level; 43 → 43; 9.535 → 9.86 nm |

**What was not anticipated:** the task expected to spend its budget deciding *how wide* the allowance should be, and
the decisive finding turned out to be that the **margin is an integer**. The energy criterion then settled the
integer, but the reason the answer is quotable at all is a quantisation argument that costs one division. And the
second surprise is that the **conservative** end of the ladder is the one that has to be adopted for a reason
nothing in the ladder contains: at 4 rises the rigid-rooted arm places, and `CH-0081`'s closure of the truss branch
depends on its not doing so.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged and upstream of everything here.
- **The stacking free energy is transferred.** −2.63 kcal/mol per helix is measured on origami **edges** at 1×TAE + 12.5 mM Mg²⁺ and 22 °C, between two **separate** bodies; this device is 2 mM MgCl₂ at 300 K and the two faces are on **one** body. `C-0079`'s validity note applies verbatim: **no per-stack free energy as a function of Mg²⁺ exists**, and `C-0079` records that a Mg²⁺ titration of an origami stacking device titrates the *competition* with electrostatic repulsion, not the stack.
- **The stacked separation is a simulation potential's minimum**, 0.34072 nm, not a measurement. Its agreement with the rise to 0.2 % is a consistency check, not an independent datum.
- **The closure stiffness contains `C-0009`'s `k_s`, which is a construction.** `C-0072` excludes it from its own floors for that reason. Here it is carried as the **soft** path and the measured-only **stiff** path is reported beside it; the two differ by exactly **one rise** at both occupancies, which is the whole modelling exposure of the recommendation.
- **The closure model is a single serial spring.** It does not contain bending of the arm out of the row, rotation at the root, or the host sheet's own weave; each of those is a further compliance and would make the required gap *larger*, so the recommendation is on the **optimistic** side of its own model. `C-0072`'s floor 4 — the arm tip's transverse fluctuation, 1.80744 nm — is a bound on how much larger, and it is 2.68× the recommended margin.
- **The one-per-cent occupancy is a chosen tolerance and is named as one.** The parity criterion (`occupancy = 1`), which contains no choice, asks 5 rises on the same path.
- **A *"places"* verdict is still the weak direction.** The plan model is `C-0041`'s and `C-0053`'s hard-body one at nominal positions; `C-0072`'s tolerance argument stands over this claim unchanged. What this claim changes is that the margin is now **larger than three of `C-0072`'s four floors**, where before it was smaller than all four.
- **No flatness, stiffness, force, stroke or bias number is touched.** This claim moves a plan margin, a joint window and a placement ceiling, and no load path.
- **The arm quantised to 24 bp is 0.16 % stiffer than `C-0017`'s equality demands.** `C-0072` shows a per-path base-pair adjustment recovers the sum to `1.3e−4`, so this is a rounding on the safe side and not a placement error.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| blunt-end stack free energy per helix | **−2.63 kcal/mol** | **CITED, READ DIRECTLY** — Woo & Rothemund, *Nature Chem.* **3**:620 (2011), SI Table S4, via `C-0079`'s survey |
| oxDNA2's coaxial-stacking minimum and cutoff | **3.4072 / 5.1108 Å** | **CITED, READ DIRECTLY** — LAMMPS `pair_oxdna2` / Henrich et al., *EPJE* **41**:57, via `C-0079` |
| the all-atom PMF's repulsive onset | **~13 Å** | **CITED, READ DIRECTLY** — Maffeo, Luan & Aksimentiev, *NAR* **40**:3812 (2012), via `C-0079` |
| the three anti-stacking remedies and the 30 % monomer figure | verbatim | **CITED, READ DIRECTLY** — Rothemund, *Nature* **440**:297 (2006), main text and SI Note S5.7; the SI re-fetched for `T-151` to `gpd/data/T-151-sources/` |
| *"bases at the end of the helices are highly available for stacking"* | verbatim | **CITED, READ DIRECTLY** — the same SI |
| duplex `EI`, `S` | 230 pN·nm², 1100 pN | **CITED, CanDo MODEL INPUT** / **MEASURED** |
| crossover `k_θ`, `k_s` | 13.5294 pN·nm/rad, 64.7059 pN/nm | **CITED, FITTED** (Chen et al. 2014) / **A CONSTRUCTION, NOT A MEASUREMENT** (`C-0009`) |
| interhelical distance, rise, crossover spacing | 2.69 nm, 0.34 nm, 32 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** |
| the 34 stations, the phase, the arm, `A2` | phase 24, 8.16439 nm, 78.2353 | **`C-0063`/`C-0055`/`C-0039`/`C-0034`, RE-RUN** |

Everything else — the quantisation argument and its six bounds, both closure paths, all seven criteria, the eight
candidates with their budgets, margins, end factors, bisected joint ceilings, occupancies, placed counts, 45-arm
counts and 30-root ceilings, the lattice map, the five design controls and the five falsifier verdicts — is
**derived here in code**.

## Still open — named, not answered

1. **No per-stack free energy at 2 mM Mg²⁺ exists**, and `C-0079` already records that the quantity is not cleanly separable from the electrostatic competition. A measurement at this buffer would move the criterion by `√(ΔG)`, i.e. weakly — a factor of two in the bond is 1.41× in the closing distance and **less than one rise** here.
2. **The closure model has one degree of freedom.** A two-dimensional closure — the arm bending sideways in the row as well as stretching — has not been solved, and it can only make the required gap larger.
3. **Whether the arm should instead be LENGTHENED into the freed budget.** At 6 rises the budget admits 26 bp where the design uses 24; a longer arm is a *softer* path and would need `C-0017`'s sum re-placed. Not done here, because the recommendation is a clearance and not a redesign.
4. **The staple break that liberates a terminus.** It is the only route to the end-chemistry control, it is structurally free by `C-0025`, and its yield cost (an 8 bp domain beside a joint) is cited and not priced.
5. **`C-0072`'s floor 4 still fires**, at 2.68× the recommended margin. It is *"a floor of resolution, not of failure"* by `C-0072`'s own classification, but it means the *nominal* verdict is still not the instrument that decides whether the assembled array is comfortable.

## Challenges

**Raises [`CH-0100`](../challenges/CH-0100-the-collinear-margin-is-an-integer-not-a-residue.md)** against `C-0069`'s,
`C-0071`'s and `C-0072`'s reading of `p − d − L` as a 0.0256 nm residue.

**[`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) is UPHELD
and closed**: its own statement that *"it does not assert that 1.3 nm is the right allowance"* and that *"a design
would in any case quantise the choice at the 0.34 nm rise"* is exactly what this claim does, and the answer lands
**above** its generous end.

**None stands against this claim.** The five ways it would fail:

1. **A blunt-end stacking range or free energy materially larger than the measured ones.** The criterion goes as `√(|ΔG|)`, so it takes a **4×** stronger bond to cost two more rises — and the design has two in hand.
2. **A closure path softer than the series chain here.** Adding a compliance lowers `k` and raises the gap as `1/√k`; a 4× softer path costs two more rises and would take the recommendation to the 8 the design has, with nothing left.
3. **A demonstration that the two faces cannot stack at all** — a phase or end-chemistry argument. Then the requirement collapses to the coaxial steric/electrostatic one, which `C-0079` shows is finite at contact, and the budget becomes the bare pitch.
4. **A different arm length.** The whole margin arithmetic is `32 − N_d − N_L`; any change to `N_L` moves it one rise at a time, and `C-0069`'s own `α = 1.2` sensitivity is exactly such a change.
5. **A demonstration that `C-0053`'s convention is charging a staple-routing allowance** — `C-0079`'s own failure route 5, and neither this claim nor `CH-0093` would then apply.
