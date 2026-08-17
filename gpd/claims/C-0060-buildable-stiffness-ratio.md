# C-0060 — The 5:1 ratio CAN be built, and the quantum is not the trap it was for `C-0023`'s preload: all seven settings of the five catalogue elements reach both of `C-0058`'s levels at 1–19 % granularity against a flat window a factor of **5.7** wide, and the mandate — an equality on a **sum** — is settable to `1.3e−4` by moving individual paths one base pair. What the flat design cannot do is **place**: the soft level's member is 1.7–2.1× longer than the stiff one, six of seven elements fail to lay 45 stations out, and the qualifier `C-0058`'s verdict needs is about the ARRAY and not about the stiffness

| | |
|---|---|
| **Task** | [`T-122`](../tasks/T-122-buildable-stiffness-ratio.md), which is [`C-0058`](C-0058-non-uniform-coupling.md)'s *Validity range* item — *"nothing here says a per-path stiffness can be BUILT to a prescribed value … the largest open item this claim leaves"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Verification type** | **logical** (an exact enumeration of the buildable settings of seven catalogue elements over an integer design parameter — base pairs, nucleotides, crossovers — with no mesh and no free parameter) **+ in-silico** (`C-0058`'s exact Woodbury surrogate on `C-0009`'s grillage and `C-0006`'s plate, under `C-0022`'s **solved** load read from its own result file and keyed on concentration, gap **and bias**) |
| **Verdict** | **PASS on the predicate, and the answer is YES on the stiffness and NO on the placement.** All seven catalogue settings reach **both** of `C-0058`'s levels — 0.9208 pN/nm at 34 rim stations and 0.1842 at 11 interior ones — and every realised ratio lands in **4.667 – 5.144** against a flat window this task **measures rather than cites** at **3.5 ≤ R ≤ 20**. **The declared falsifier did not fire anywhere**: the coarsest quantum in the catalogue is **19.1 %** of a level's own stiffness (an 11 bp hinge arm) and the finest **1.0 %** (a 99 nt ssDNA limb), against a window **471 %** wide — quantisation is **25× finer than the requirement**, where `C-0023`'s mounting-offset quantum was **8.3× coarser** than its own. **All fourteen built designs are still flat on the solved lattice**, 0.0715–0.0815 of the free-tile stroke against `T-5b`'s 0.10 and the uniform coupling's 0.2182. **The mandate survives, but only because it is a SUM**: rounding the two *levels* independently misses `C-0017`'s 33.3333 pN/nm by **0.40 – 5.44 %**, which is a placement error and not a rounding nuisance; **trimming — moving individual paths by ONE base pair — takes the worst miss to `1.3e−4` in at most 18 moves and leaves 3–4 distinct settings instead of two.** **The tolerance is a threshold, and it is generous**: the built design loses the flatness verdict at **34.6 %** relative scatter on its worst pattern, **2.04×** `C-0026`'s 17 % break-even, and the two populations do not even *overlap* until **66.7 %** — so **flatness binds at half the amplitude the ordering does**. **What fails is the array.** The soft level's member is **1.7–2.1× longer** than the stiff one, so a mixed array is priced at the longer span: `C-0030`'s coupled flexure needs a **52.36 nm** interior span against a 40 nm tile and places **0** of 45; `C-0023`'s `E3` places 15; the hinge arms place **30** and **15**, bounded by plan area rather than by `C-0053`/`C-0055`'s root pitch (43 and 30); only `C-0023`'s `E4` places 45, and it needs a **second ground under the tile** that the polymer layer and the electrode occupy. **`C-0041`'s obstruction is unchanged and the non-uniform design makes it worse, not better.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, no element in the catalogue has been built, and the out-of-plane motif every one of them stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-122-buildable-stiffness-ratio.json`, produced by `coupling.BuildableStiffnessRatioStudyKt`; model in `src/main/kotlin/coupling/BuildableStiffnessRatio.kt`; **14 cheap-bound records, 21 ratio-sweep records, 14 ladder records, 7 built-ratio records, 7 trim records, 16 flatness records, 12 drift records, 24 scatter records, 14 cost records, 7 packing records, 21 convergence records, 18 upstream reproductions**; **28 gate-named tests in `src/test/kotlin/coupling/BuildableStiffnessRatioTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL, 0 failures** on its own isolated tree — the whole suite, on the finished tree — with two concurrent agents' mid-TDD files dropped by `--drop-file` (`src/test/kotlin/anchoring/TorsionFeasibleRoutingTest.kt` and `src/test/kotlin/structure/StackedArmSheetTest.kt`, plus the main source `src/main/kotlin/structure/StackedArmSheetStudy.kt`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical on two independent runs** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous buffer with **Mg²⁺**; 40.0 × 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; 8 symmetrically centred crossover columns (`T-10`); `C-0015`'s 3 × 15 grid; §3's 100 pN over the footprint at the **acceptable** 3 nm; `C-0017`'s **33.3333 pN/nm as a SUM**; `C-0001`'s foundation secant, ×1; free-tile stroke **4.90731 nm**; design point `C-0022` 2 mM, 10 nm, 0.192 V; quanta 0.34 nm per base pair, 0.65 nm per nucleotide, one crossover per hinge |
| **Consumes** | [`C-0058`](C-0058-non-uniform-coupling.md) (the two levels — **re-derived from its own rim × 5 rule, not tabulated** — the surrogate, the distributions, the thermal generalisation, the 0.2182 and 0.0753 reproduced as limiting cases), [`C-0023`](C-0023-two-sided-coupling.md) (`TransverseDuplexFlexure`, `CrossoverHingeFlexure`, `FreelyJointedChain`, `offsetForPreload` — the preload quantum **re-derived as the negative control**), [`C-0030`](C-0030-coupled-standoff-joint.md) (`CoupledJointFlexure`, `standoffTipFlexibility`, `coupledFlexureSpan`, `StandoffBase.crossovers(2)` — its 31.82 nm span reproduced as the `R = 1` limiting case), [`C-0039`](C-0039-two-spring-elastica.md) (`TwoSpringElastica`, `elasticaArmForStiffness` — its 12.7198 nm arm reproduced), [`C-0034`](C-0034-guided-arm-anchorage.md)/[`C-0029`](C-0029-perpendicular-junction-routing.md) (the `A2` anchorage), [`C-0041`](C-0041-flexure-array-packing.md) (`packingLimitedPathCount`), [`C-0053`](C-0053-hinge-arm-array-packing.md)/[`C-0055`](C-0055-unused-junction-site.md) (`packingLimitedElementCount`, `placeHingeArms`), [`C-0026`](C-0026-one-row-per-duplex.md) (`ScatterPattern`, the 0.883 pN per unit amplitude and the 17 % break-even, both **CITED**), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`perPathSecantCeiling`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the **solved** collar), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, as a sum), [`C-0014`](C-0014-lateral-confinement.md) (the thermal force), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 10 pN unzip allowable), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0073`](../challenges/CH-0073-the-along-helix-scatter-rule-reverses-on-a-non-uniform-coupling.md), against `C-0026`'s build rule |

---

## The claim, in one line

**A prescribed per-path stiffness is not like a prescribed preload: a preload is a *length* and DNA quantises it at 0.34 nm against a 0.041 nm requirement, but a stiffness is a *power* of a length, so the same 0.34 nm quantum is 1–19 % of the stiffness and the 5:1 ratio has a factor-of-5.7 window to land in — and the mandate the two levels have to sum to is settable a further 45× more finely, because a builder cuts each path separately. `C-0058`'s design is buildable, at every element in the catalogue. The thing that is not buildable is the 45-station array it stands on, and the non-uniform design makes that worse rather than better, because the soft level is the long one.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous buffer with **Mg²⁺**.
- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre. `w` is positive **downward** (`T-5`, unchanged).
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane**, on the same **81 × 81** grid as `C-0026`, `CH-0034`, `C-0047` and `C-0058`. **Flat** means below **10 %** of the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- **The quantum of a duplex length is the rise per base pair, 0.34 nm**; of a single-stranded contour, **0.65 nm per nucleotide** (inextensible convention, and the convention travels with the Kuhn length); of a hinge count, **one crossover**.
- **A ladder is ENUMERATED, never searched.** Every buildable setting between two integer bounds is evaluated and the nearest to a target reported with the ladder's own step there, so the ladder deliverables have no convergence parameter at all.
- **Granularity** is `|k(m+1) − k(m)|/k(m)` at the setting nearest the target — the mean of the two neighbouring steps. The cheap bound is its power-law limit `|e|·q/p`.
- **Scatter is multiplicative and is NOT renormalised to the mandate.** A build tolerance does not know the mandate; the drift it causes is reported rather than removed.

---

## The two cheap bounds, which ran first

### Bound 1 — the quantum against the window, one division per element per level

A bending element has `k ∝ p^(−3)` and a hinge `k ∝ p^(−2)`, so one quantum is a fractional stiffness step of `|e|·q/p`. The declared falsifier was *"if one step of the design parameter takes the ratio out of `C-0058`'s flat window, the ratio cannot be set and the answer is negative"*.

| element | design parameter | rim setting | interior setting | rim granularity | interior granularity |
|---|---|---|---|---|---|
| `E3f` — `C-0023` flexure, pinned, free | span | 67 bp = 22.78 nm | 115 bp = 39.10 nm | **4.48 %** | 2.61 % |
| `E3c` — the same, clamped | span | 107 bp = 36.38 nm | 183 bp = 62.22 nm | 2.80 % | **1.64 %** |
| `E5n1` — `C-0023` hinge flexure, 1 hinge | arm | 11 bp = 3.74 nm | 23 bp = 7.82 nm | **19.12 %** | 9.31 % |
| `E5n2` — the same, 2 hinges | arm | 15 bp = 5.10 nm | 30 bp = 10.20 nm | 14.58 % | 7.64 % |
| `E4` — `C-0023` antagonistic pair | contour | 20 nt = 13.00 nm | 99 nt = 64.35 nm | 5.01 % | **1.01 %** |
| **`C30` — `C-0030` coupled flexure** | **span** | **87 bp = 29.58 nm** | **154 bp = 52.36 nm** | **3.20 %** | **1.82 %** |
| `C39` — `C-0039` elastica arm | arm | 34 bp = 11.56 nm | 63 bp = 21.42 nm | 7.77 % | 4.27 % |

> **The falsifier did not fire at a single one of the fourteen.** The coarsest quantum is **19.1 %** and the flat window is **471 %** wide — a factor of **25**. `C-0023`'s preload asked for 0.0409 nm against a 0.34 nm quantum and was **8.3× the other way**; that number is re-derived here as the negative control, because a "yes" to a quantisation question is only informative beside the corpus's one "no".
>
> **Why the two answers differ is structural and is the finding**: a preload is a *length*, so the quantum enters it at first power; a stiffness is a *power* of a length, so the same quantum enters divided by the length in units of itself — 87 base pairs, not one.

### Bound 2 — the mandate is a SUM, so its lattice is 45× finer

One path may be re-cut independently of the others, so if a single path's step is `Δk`, the total is settable on a lattice of spacing `Δk` against a total of `K` — a relative granularity of `Δk/K ≈` (per-path granularity)`/n`. **This decides the mandate before any solve**, and it is what makes the *level*-rounding error recoverable.

---

## Deliverable 1 — the flat ratio window, MEASURED

`C-0058` reports its flat window as `5 ≤ R ≤ 20` from a six-point sweep. Re-swept here at 21 ratios on the same surrogate, at the same 6.70 nm collar:

| `R` | 1 | 2 | 3 | **3.5** | **5** | **7** | **10** | **20** | 25 | 100 |
|---|---|---|---|---|---|---|---|---|---|---|
| dishing / stroke | 0.2182 | 0.1415 | 0.1076 | **0.0967** | **0.0753** | **0.0653** | **0.0792** | **0.0970** | 0.1007 | 0.1126 |
| flat? | no | no | no | **yes** | **yes** | **yes** | **yes** | **yes** | no | no |

&nbsp;&nbsp;&nbsp;&nbsp;**The window is `3.5 ≤ R ≤ 20`, a factor of 5.7 wide** — `C-0058`'s `[5, 20]` is contained in it and its lower edge moves down, because the sweep is finer. Every verdict below is quoted against this measured window, never against the cited one.

> **And a small result nobody asked for: the best one-parameter ratio at `C-0058`'s own collar is 7, not 5.** It dishes **0.0653** against 5's 0.0753 — a further **13.4 %**, and 70.1 % below the uniform coupling — and it is inside `C-0058`'s own six-point sweep's *range* but not among its points. It costs nothing: 7 is as buildable as 5 at every element, and it sits further from both edges of the window, so it is the **more scatter-tolerant** choice as well.

---

## Deliverable 2 — the built designs

| element | rim | interior | **realised ratio** | inside `[3.5, 20]`? | total before trim | dishing / stroke | **flat?** |
|---|---|---|---|---|---|---|---|
| `E3f` | 67 bp, 0.9339 | 115 bp, 0.1847 | **5.057** | yes | 33.785 (+1.35 %) | **0.0742** | **YES** |
| `E3c` | 107 bp, 0.9172 | 183 bp, 0.1833 | **5.003** | yes | 33.200 (−0.40 %) | **0.0755** | **YES** |
| `E5n1` | 11 bp, 0.9012 | 23 bp, 0.1918 | **4.698** | yes | 32.750 (−1.75 %) | **0.0795** | **YES** |
| `E5n2` | 15 bp, 0.8669 | 30 bp, 0.1858 | **4.667** | yes | 31.519 (−5.44 %) | **0.0815** | **YES** |
| `E4` | 20 nt, 0.9103 | 99 nt, 0.1839 | **4.950** | yes | 32.974 (−1.08 %) | **0.0763** | **YES** |
| **`C30`** | **87 bp, 0.9078** | **154 bp, 0.1842** | **4.927** | **yes** | 32.892 (−1.32 %) | **0.0767** | **YES** |
| `C39` | 34 bp, 0.9530 | 63 bp, 0.1853 | **5.144** | yes | 34.440 (+3.32 %) | **0.0725** | **YES** |
| *`C-0058`'s nominal* | *0.92081* | *0.18416* | *5.000* | *yes* | *33.333* | *0.0753* | *YES* |
| *uniform (the limiting case)* | *0.74074* | *0.74074* | *1.000* | *no* | *33.333* | *0.2182* | *no* |

- **Every element reaches both levels**, every realised ratio is inside the measured window, and every built design is flat.
- The nearest-rung error runs **`4.0e−4`** (`C30` interior, a 154 bp span) to **`5.9e−2`** (`E5n2` rim, a 15 bp arm), and is at most half the ladder's own step at every one of the fourteen — which is the definition of "the target is bracketed", asserted as a test rather than assumed.

---

## Deliverable 3 — the mandate, and why rounding two levels is not enough

Rounding each *level* to its nearest rung misses `C-0017`'s equality by **0.40 % to 5.44 %**. That is a **placement** error: `C-0017`'s mandate is `100 pN / 3 nm` on the secant, so a 5.44 % shortfall is 5.44 % of the force the actuator delivers at its acceptable stroke.

**Trimming recovers it, and the recovery is bound 2 made executable.** Each path takes its own nearest rung; then single paths move by **one** rung, in path order, while the total's error strictly falls:

| element | total before | total after | error before | **error after** | moves | distinct settings |
|---|---|---|---|---|---|---|
| `E3f` | 33.7847 | 33.3334 | 1.35e−2 | **1.78e−6** | 12 | 4 (67/68, 115/116 bp) |
| `E3c` | 33.1998 | 33.3339 | 4.01e−3 | **1.56e−5** | 6 | 4 |
| `E5n1` | 32.7495 | 33.3377 | 1.75e−2 | **1.30e−4** | 3 | 3 |
| `E5n2` | 31.5193 | 33.3322 | 5.44e−2 | **3.31e−5** | 13 | 3 |
| `E4` | 32.9737 | 33.3298 | 1.08e−2 | **1.07e−4** | 18 | 3 |
| **`C30`** | 32.8923 | **33.3343** | 1.32e−2 | **3.03e−5** | 16 | **4 (86/87, 154/155 bp)** |
| `C39` | 34.4396 | 33.3347 | 3.32e−2 | **4.23e−5** | 18 | 4 |

- **The worst residual is `1.3e−4`, two to three orders below the level-rounding error**, and no path moves more than one base pair from its own nearest.
- The price is that the design is **3–4 distinct staple lengths, not two** — still a rule a builder can write down, and still two *populations*.
- **All seven trimmed designs are flat too** (0.0715–0.0787), so the trim costs nothing in dishing.

---

## Deliverable 4 — the tolerance, delivered as a THRESHOLD because `T-45` has no measurement

`T-45` records that nothing accessible gives the stiffness spread of nominally identical hybridised staple extensions. So the deliverable is what the *design* tolerates, per `C-0026`'s own named patterns, at amplitude `ε` applied multiplicatively and **not** renormalised:

| pattern | nominal, **as built** | **built `C30`, as built** | **built `C30`, mandate held** | vs `C-0026`'s 17 % break-even |
|---|---|---|---|---|
| alternating **rows** (across the helices) | 68.3 % | **69.1 %** | **69.8 %** | 4.06× |
| alternating **columns** (along the helices) | 34.9 % | **34.6 %** | **31.6 %** | **2.04×** — the binding one |
| one whole duplex row off | 73.4 % | **73.8 %** | 74.5 % | 4.34× |
| one attachment off | **never** below 95 % | **never** below 95 % | **never** below 95 % | — |

- **The binding threshold is 34.6 % relative amplitude** (31.6 % with the mandate held). A build tolerance would have to be twice `C-0026`'s break-even before the flat verdict is lost, and **6.9× the 5 % a staple design might plausibly hold**.
- **The two populations do not merely overlap until `(R−1)/(R+1) = 66.7 %`**, derived exactly and asserted as a test. **So flatness binds at 0.52× the amplitude the ordering does** — the ordering criterion the task asked about is *not* the one that matters, and saying so is part of the answer.
- **The two readings separate two effects and both are reported.** *As built* is the honest tolerance — an assembly does not know the mandate, and the along-helix pattern drifts `C-0017`'s total by **21 %** at its own threshold because it is collinear with the rim/interior split. *Mandate held* rescales it away, and the along-helix threshold **falls further, to 31.6 %**: the sensitivity is the distribution's, not the total's.
- **The tolerance is a property of the pattern, not only of its amplitude**, by a factor of **2.21** with the mandate held — which is [`CH-0073`](../challenges/CH-0073-the-along-helix-scatter-rule-reverses-on-a-non-uniform-coupling.md).
- **Small scatter *helps*.** At a 10 % amplitude every pattern lowers the dishing (0.0767 → 0.0571 for the along-helix one); the threshold is where a large amplitude finally destroys the two-population structure, not a slope at the origin. A linearised tolerance budget would have got the sign wrong.

---

## Deliverable 5 — the cost, on every path the programme prices

| | `C30` rim (87 bp) | `C30` interior (154 bp) | allowable |
|---|---|---|---|
| stiffness [pN/nm] | 0.9078 | 0.1842 | 3.333 (`= a/s`, `C-0049`) |
| **force at the 3 nm stroke** [pN] | **2.723** | 0.553 | **10 — margins 3.67× and 18.1×** |
| thermal force `k_i √(k_BT/K)` [pN] | 0.320 | 0.065 | — |
| tangent / secant at 3 nm | **0.756** | **0.782** | strain-**softening** (`CH-0042`) |

- No allowable in the stack is threatened by either level, at any element: the worst per-path force in the whole catalogue is **2.86 pN** (`C39` rim) against 10 pN.
- **`C-0014`'s over-stiffening penalty is linear in the path's share** (`C-0058`, generalised): the rim path carries 0.320 pN where a uniform one carries 0.261.

### The ratio DRIFTS over the stroke, because two spans of a nonlinear element are two different curves

`C-0058`'s springs are linear; `C-0030`'s realised coupling strain-softens and `C-0039`'s strain-stiffens. Realised **secant** ratio:

| stroke [nm] | 0.5 | 1 | 2 | **3 (placement)** | 5 | **10 (desired)** |
|---|---|---|---|---|---|---|
| `C30` | 4.952 | 4.958 | 4.956 | **4.927** | 4.774 | **3.900 (−20.8 %)** |
| `C39` | 4.957 | 4.972 | 5.034 | **5.144** | 5.559 | **12.536 (+143.7 %)** |

> **Both stay inside the measured `[3.5, 20]` window at every stroke swept** — `C30` because it softens the *stiff* limb faster and `C39` because the window's upper edge is far away. But the two run **opposite ways**, and `C39` uses 63 % of the window's width to do it. **A design that fixes a ratio at its placement stroke does not hold it at §3's desired stroke**, and which way it moves is a property of the element, not of the distribution.

---

## Deliverable 6 — what actually fails, and it is not the stiffness

| element | plan kind | longest member | plan-area count | root-pitch count | **places 45?** |
|---|---|---|---|---|---|
| `E3f` | beam | 39.10 nm | **15** | — | **no** |
| `E3c` | beam | 62.22 nm | **0** | — | **no** |
| `E5n1` | rooted arm | 7.82 nm | **30** | 43 | **no** |
| `E5n2` | rooted arm | 10.20 nm | **15** | 30 | **no** |
| `E4` | out of plane | 64.35 nm (contour) | 45 | — | **yes, at a price** |
| **`C30`** | beam | **52.36 nm** | **0** | — | **no** |
| `C39` | rooted arm | 21.42 nm | **0** | 15 | **no** |

- **Six of seven elements cannot lay 45 stations out at all**, and the obstruction is `C-0041`'s and `C-0053`/`C-0055`'s, unchanged.
- **The non-uniform design makes it worse, not better**: the soft level's member is **1.7–2.1× longer** than the stiff one — `k ∝ p^(−3)` means a 5× softer path is a `5^(1/3) = 1.71×` longer one — and a mixed array is priced at the **longer** member. `C-0030`'s uniform 31.82 nm span already places only 15; its interior span here is **52.36 nm**, past the tile edge entirely, and places **0**.
- **`E4` is the only element that places 45**, because a chain normal to the sheet consumes no plan area. Its price is `C-0023`'s own: the down limb is grounded on the **substrate**, and the §1 stack has the polymer layer and the electrode there. **`C-0023` excluded `E4` for exactly this reason and nothing here changes that.**
- For the rooted arms the **plan area** binds before `C-0053`/`C-0055`'s crossover **root pitch** (30 against 43, and 15 against 30) — worth recording, because the arm branch's own claims report the root pitch as the obstruction.

---

## The five verification gates

Executed as **28 gate-named tests** in `src/test/kotlin/coupling/BuildableStiffnessRatioTest.kt`; `tools/verify.sh` **BUILD SUCCESSFUL, 0 failures** on the whole suite.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a bending ladder falls as the **cube** of the span (doubling the units divides the stiffness by exactly 8) and a hinge term **quarters** at double the arm; the power-law granularity is dimensionless and equals `\|e\|q/p` (0.034 at `e = 3`, `p = 30 nm`); the **enumerated** granularity tends to that bound as the quantum shrinks, 6 % out at 0.34 nm and 0.6 % out at 0.034 nm, monotonically; unphysical arguments throw — a zero quantum, an empty or inverted unit range, a zero unit, an empty ladder, a non-positive target, a zero parameter, a ratio below one, a negative stiffness level, a scatter amplitude of 1 (which annihilates a path), and every argument of the mandate trim | **PASS** |
| **2 — limiting cases** | **`R = 1` reproduces `C-0017`'s 45-path design exactly** — 45 paths at 0.740741 pN/nm, and `C-0030`'s own **31.82 nm** span, with the per-path root and the 45-path root agreeing to `1e−9`; an **infinitely fine quantum returns the target** to `< 1e−6` and the trim's total to `< 1e−6`; a ladder of **one** rung reports granularity **exactly 0.0** and `bracketed = false`, never a sentinel infinity; zero scatter returns the nominal distribution **identically**; a two-level design at a unit ratio **is** the uniform one | **PASS** |
| **3 — symmetry and conservation** | the two-level total is exactly `34 k_rim + 11 k_int`; the mask counts are **34 and 11**; a **balanced** scatter pattern preserves the total exactly and an unbalanced one moves it by exactly its own perturbation; the total's relative granularity is the per-path one **divided by the path count**, asserted as `0.04/45`; **a point-reflected two-level design dishes identically on the lattice**, which is centro-symmetric and not mirror-symmetric (`C-0015`); the mandate trim never worsens the total, never moves a path more than one rung from its own nearest, and leaves the two populations still ordered | **PASS** |
| **4 — numerical convergence** | the dishing sampling grid 41/81/161 at the built design; **NESTED** subdivisions `1 ⊂ 2 ⊂ 4`; the elastica's RK4 count 200/400/800 at `C-0039`'s own arm; the scatter threshold **exits on the bracket width** and reports it, takes the **first** crossing rather than assuming monotonicity (asserted on a metric that crosses, falls back and crosses again), and reports "never reached" as a flag with the scan ceiling rather than as `Infinity` | **PASS** |
| **5 — literature and upstream cross-check** | `C-0058`'s **0.921** and **0.184** levels, its 34 and 11 station counts, its **0.2182** uniform and **0.0753** rim dishing; `C-0026`'s free-tile stroke **4.90731 nm**; `C-0017`'s **33.3333 pN/nm**; `C-0030`'s **31.82 nm** span and **25.23 pN/nm** tangent; `C-0023`'s **24.61 nm** span and **4.11 nm** arm (read on the ladder, so to the nearest buildable setting); **`C-0023`'s preload quantum, 0.0409 nm against 0.34 nm — the negative control**; `C-0049`'s **150 pN/nm** per-path secant ceiling and `C-0058`'s **4.5** admissible ratio; `C-0041`'s **15**; `C-0039`'s **12.7198 nm** arm; `C-0014`'s **0.261 pN** thermal force. **Worst relative departure over eighteen reproductions: `7.3e−3`**, which is the ladder-rounded reading of `C-0023`'s 4.11 nm arm | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | **the granularity exceeding the flat ratio window** — the negative answer | **no**, at none of the fourteen | 19.1 % at worst against a 471 % window, a factor of 25 |
| 2 | the surrogate failing to reproduce `C-0058`'s uniform and rim numbers | **no** | 0.2182 and 0.0753, exactly |
| 3 | **a uniform-ratio request failing to reproduce `C-0017`'s 45-path design** | **no** | 0.740741 pN/nm and `C-0030`'s 31.82 nm span |
| 4 | the realised ratio inside the window while the built design's dishing is outside `T-5b`'s 0.10 | **no** | all fourteen built designs flat, 0.0715–0.0815 |

**A result that was not anticipated**: that **rounding the two levels independently is a 5.4 % miss on `C-0017`'s mandate** — the task expected the level rounding to be the whole story, and it is not. The mandate is only recoverable because it is an equality on a *sum*, which is the same structural fact `C-0058` exploited to free the distribution in the first place. **The second cheap bound turned out to be load-bearing rather than decorative.**

**A second one**: that the pattern `C-0026` recommends a builder aim for — scatter alternating **along** the helices, where the crossover force is restored exactly zero — is on this design the pattern the **flatness** verdict tolerates least, by exactly 2.00×. That is `CH-0073`.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured**, and the out-of-plane motif every catalogue element stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`).
- **The two levels are `C-0058`'s** and inherit its whole validity range: `C-0022`'s solved collar with its **unsourced rim charge** (1.85× on the collar depth), `C-0001`'s single foundation secant, one crossover layout, and the fact that the design is flat at **one operating state** (`C-0058`'s own Deliverable 4).
- **`T-45` is still unmeasured.** The scatter numbers here are a **threshold the design tolerates**, never a tolerance any assembly has been shown to hold. `C-0026`'s 0.883 pN per unit amplitude and its 17 % break-even are **CITED** and carried, not re-derived.
- **The ssDNA pair is priced on its GAUSSIAN stiffness at zero tension**, which is the soft end of `C-0023`'s own bracket; the Kuhn length is a **2× method-systematic** bracket (1.34–1.41 nm force-spectroscopy against 2.10–2.84 nm zero-force) and the contour convention travels with it.
- **The elastica ladder is a shooting solve** at 400 RK4 steps, swept 200/400/800 in gate 4; every other ladder is closed form.
- **The packing verdict is `C-0041`'s and `C-0053`/`C-0055`'s, evaluated at the spans this task's two levels demand.** It is a plan-view argument on one body size and, for the beams, one orientation sweep, inherited unchanged. **`C-0041` sweeps a single span; a genuinely MIXED array has not been swept and might do better** — that is named as open, not assumed away.
- **The drift is read on the SECANT**, which is what `C-0017`'s placement condition is written on; the tangent is reported beside it for `CH-0042`'s stability reading and is not the quantity the ratio is defined by.
- **The ladder rounds to the nearest rung in RELATIVE stiffness.** An absolute nearest would be biased toward the stiff level by the ratio itself.
- **`T-5b`'s 10 % is a CONVENTION.** At a 5 % tolerance no built design here survives and neither does `C-0058`'s nominal one.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| rise per base pair | 0.34 nm | **CITED, MEASURED** (Douglas et al. 2009) |
| ssDNA contour per nucleotide | 0.65 nm, inextensible | **CITED, MEASURED** (Sim et al. 2012; Bosco et al. 2014). The convention travels with the number |
| ssDNA Kuhn length | 2.10 nm, zero-force end | **CITED, MEASURED** (Chen et al. 2012); a 2× method-systematic bracket |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT**, not a measurement |
| crossover hinge `k_θ` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al. (2014) SI |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al. (1997) |
| `C-0026` scatter sensitivity / break-even | 0.883 pN per unit amplitude / 17 % | **CITED** from `C-0026`; `T-45` is unmeasured |
| `C-0022`'s solved collar | 2 mM, 10 nm, 0.192 V | **CITED**, read at run time from `gpd/results/T-3b-tile-edge-load-profile.json` |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| `RIGID_PLATE_TOLERANCE` | 0.10 | **CITED CONVENTION** from `T-5b` |
| §3 parameters | 100 pN, 3 nm, 10 nm, 40 × 40 nm | **CITED** |

Everything else — every ladder, granularity, realised ratio, trim, flatness solve, drift, scatter threshold, cost and packing count — is **derived here in code**, with `C-0058`'s, `C-0030`'s, `C-0039`'s, `C-0041`'s, `C-0053`'s and `C-0026`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **`T-45` itself**: what relative scatter a staple-designed attachment array can be *guaranteed* to. This claim delivers the threshold the design tolerates — 34.6 % — and nothing accessible gives the spread an assembly achieves.
2. **Whether an array of MIXED spans packs differently from an array of equal ones.** `C-0041` sweeps a single span; here the interior members are 1.7–2.1× longer than the rim ones and the array is priced at the longer. A mixed layout — long members on the rim rows, short in the interior, or two different *elements* at the two levels — has never been run, and it is the one route by which the placement could be recovered.
3. **Whether the two levels can be realised by two DIFFERENT elements.** Nothing forbids it, and the soft level's length is what breaks the packing: a hinge arm at the interior and a flexure at the rim is a design nobody has priced.
4. **The ratio's drift at §3's desired stroke.** Both nonlinear elements stay inside the window here, but they run opposite ways and `C39` uses 63 % of its width.
5. **`C-0058`'s own open item 2** — whether a distribution flat at *every* operating state exists — which no quantisation argument touches.

## Challenges

**Raises [`CH-0073`](../challenges/CH-0073-the-along-helix-scatter-rule-reverses-on-a-non-uniform-coupling.md)** against `C-0026`'s build rule — *"let the scatter alternate along the helices, not across them, where it restores exactly zero"*. **No number in `C-0026` moves**; what moves is that the rule was derived on an **equal-spring** coupling, where the along-helix direction is a symmetry, and on `C-0058`'s three-column non-uniform design that same direction **is the ratio's own axis**.

**None stands against this claim.** The four ways it would fail:

1. **A tolerance measurement showing an assembly cannot hold 35 % relative scatter.** Then the flat design fails on the tolerance rather than on the quantum, and the threshold here is what the measurement would be graded against. This is `T-45` and it is named.
2. **A demonstration that the two levels cannot be realised by two settings of ONE element** — for example, that a 154 bp and an 87 bp flexure cannot share a superstructure. Nothing here checks the *routing* of two different spans on one body, only their plan area.
3. **A mixed-array packing solve reaching 45.** That would *strengthen* the claim's positive half and remove its negative one; it is named as open item 2.
4. **A finer flat-window sweep whose lower edge rises above the realised ratios.** The window is measured at 21 ratios and the realised ratios sit at 4.67–5.14, a factor of 1.33 above the measured lower edge of 3.5; a sweep at 0.1 resolution could move that edge but not past 5.
