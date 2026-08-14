# C-0042 — Two 90° junctions DO close on one sheet duplex, at every separation from the 6 bp steric floor up, and the azimuth costs nothing at all — because the standoff's rotation about its own axis is CONTINUOUS, not quantised at 33.74°/bp; and the separation is not free either: SEVEN base pairs is the smallest that puts the loaded plane in charge

| | |
|---|---|
| **Task** | [`T-97`](../tasks/T-97-paired-perpendicular-junction.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a deterministic closure search over a *pair* of standoffs on one seat duplex, on `C-0029`'s own backbone geometry and its own admissibility test, plus a **mixed-base beam-column finite element** for the truss whose legs no longer share one base) **+ logical** (a steric floor and two azimuthal bounds, all closed form and all cheap) **+ literature** (11 further primary-source queries with their strings recorded, on top of `C-0037`'s ~72 and `C-0029`'s ~110) |
| **Verdict** | **PASS, and the risk `C-0037` named as its single largest one does not exist.** A second standoff seats on the same sheet duplex at **every** separation from the **6 bp = 2.04 nm steric floor** to 12 bp, with **both links of both junctions inside the measured `[0.60, 0.70]` nm phosphodiester step and ZERO unpaired nucleotides**, four distinct targets, both legs on one shared lateral seat (`Σ(Δy)² = 0` exactly), and 24–28 of the seat duplex's 32 crossover phases left free. **And the azimuth costs exactly nothing: both base chords come out at 90.0°, i.e. EXACTLY on the flexure axis, at every separation, on both groove conventions, under the strict "both junctions grounded on ONE sheet duplex" reading, and with both legs seated on the duplex's own axis.** The one non-zero cost anywhere in the sweep is the *scaffold-excursion* topology at 6 and 9 bp — 3.13° and 5.87°, i.e. **0.30 % and 1.04 %** of the couple, against `C-0037`'s own ≤ 8.4 % allowance. **`C-0037`'s "best-phase" caveat is therefore discharged rather than merely noted, and its recommended design exists.** Two things were not anticipated. **The cheap bound that was written to bind did not**: if the second junction were the first's **screw image** its chord would be rotated by `n × 33.74°`, which at `C-0037`'s recommended **8 bp is 89.9°** — the entire couple on the wrong plane — and 8 bp would be the single worst separation in the band. It does not bind, because a standoff must stand **normal** to the sheet and a screw rotation about a horizontal axis does not preserve that. **And the separation is not free after all**: `C-0037` reports `L2a6`/`L2a8`/`L2a12` as bit-identical *in the loaded plane* — reproduced here exactly — but they are not identical in the **free** plane, which is the plane its truss exists to restrain. At 6 bp the free plane still governs at **8.84 pN** (margin 2.52); at **7 bp** it has crossed and the **loaded** plane governs at **9.77 pN** (margin **2.79** on CanDo, **2.10** on Fields et al.), which is `C-0037`'s `L2a8` number. **So seven base pairs buys the whole of the recommended design at 0.68 nm less row width, and `C-0037`'s "between 6 and 8 bp" is resolved to 7.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the motif is still not demonstrated** — `C-0028`'s, `C-0029`'s and `C-0037`'s literature finding is unchanged and this claim's own search adds to it. The closure test is `C-0029`'s: a **necessary** condition and never a sufficient one, so a *"closes"* verdict is an **upper bound on buildability**. |
| **Provenance** | `gpd/results/T-97-paired-perpendicular-junction.json`, produced by `anchoring.PairedPerpendicularJunctionStudyKt`; **14 cheap-bound quantities, 11 screw-image records, 28 pair records, 6 length records, 11 sensitivity records, 12 convergence records, 14 upstream reproductions, 5 literature records**; **32 gate-named tests in `PairedPerpendicularJunctionTest`, 0 failures, and the whole suite green**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with a concurrent agent's half-written `anchoring/TwoSpringElasticaTest.kt` removed by `--drop-file` |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 10.67 bp/turn, 0.34 nm rise; phosphate radius **1.00 nm**; phosphodiester step **0.60–0.70 nm, measured**; base couple on the **hard, convention-free 180° chord**, as `C-0037` adopts; `EI = 230 pN·nm²` (CanDo model input) with every margin also on Fields et al.'s implied **172.9** |
| **Consumes** | [`C-0029`](C-0029-perpendicular-junction-routing.md) (`DuplexBackbone`, `seatFaceHeight`, `linkWindowResidual`, `unpairedNucleotidesForGap`, `RoutingTopology`, `bondHingeStiffness`, `bondSlideStiffness`, `couplePhaseProjection`, the counting theorem), [`C-0037`](C-0037-triangulated-standoff.md) (`TrussLayout`, `TwoLinkBase`, `trussFrameCouple`, `trussTipFlexibility`, `trussBucklingLoad`, `TriangulatedStandoff` — **re-run as a library**), [`C-0030`](C-0030-coupled-standoff-joint.md) (`StandoffTipFlexibility`, `standoffTipFlexibility`, `CoupledJointFlexure`, `coupledFlexureSpan`, `FlexureOrientation`, `DrawInModel`, `FIELDS_BENDING_RIGIDITY`), [`C-0028`](C-0028-standoff-base-joint.md) (`standoffBucklingLoad`, `baseRestraintParameter`, `seriesStiffness`), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 32 bp interface period, 16 bp along one duplex), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0056`](../challenges/CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md) against `C-0029` and `C-0037` |

---

## The claim, in one line

**`C-0037` closed the standoff branch on a truss that needs two 90° junctions on one seat duplex and could only say that `C-0029` had placed one; this task places two, and finds the pair is not tight but *loose* — it fits at every separation from the steric floor up, with both chords exactly on the flexure axis and zero unpaired nucleotides, because the standoff's rotation about its own axis is a CONTINUOUS free parameter and not the base-pair lattice `C-0029` named — so the design's largest open risk is discharged; what the search does bind is something neither claim looked for, that the two separations `C-0037` calls identical are identical only in the plane it was not worried about, and that SEVEN base pairs, not eight, is the smallest row that hands the governing plane back to the loaded one.**

---

## The three cheap bounds, which ran first — and the one that was supposed to bind, and did not

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **The steric floor.** Two duplexes of radius `R` on one seat duplex need `2R = 2.00 nm`, i.e. `⌈2R/0.34⌉` | **6 bp = 2.04 nm** | `C-0037`'s `L2a6` row sits **exactly on** the floor and its `L2a8` row has 0.72 nm of clearance. Nothing below 6 bp is searchable and `bestPair(5)` throws |
| **2** | **The screw image.** If the second junction were the first translated `n` bp along the seat duplex *and rotated with it*, its chord would be rotated by `n × 33.74°` | 6 bp **22.4°**, 7 bp **56.2°**, **8 bp 89.9°** — `cos²` = **2.2e−6** | **It says `C-0037`'s recommended 8 bp is the single worst separation in the band, and it is wrong.** A standoff must stand *normal* to the sheet and a screw rotation about a horizontal axis does not preserve that, so the second standoff is not the first's image: **its own azimuth, its axial position, its seat and its choice of target pair are all free.** The bound's failure to bind is the whole answer |
| **3** | **The occupancy count.** Crossovers recur every **16 bp** along one duplex (`C-0015`'s 32 bp per *interface*, alternating), and the pair spans ≤ 10 bp | **24–28** of 32 phases free | The pair is never dead on arithmetic |

> **The refinement that explains bound 2's failure**, and it is reported as an *explanation* and never used to decide anything. The second standoff has two moves the screw image does not: a **swap to the seat duplex's other backbone**, worth `±Δ = ±120°`, and a **half turn about its own axis**, which is free because **a chord is a line**. Under those, the *sheet phase residual* is
>
> | `n` bp | 6 | **7** | 8 | 9 | 10 | 11 |
> |---|---|---|---|---|---|---|
> | screw image | 22.4° | 56.2° | **89.9°** | 56.3° | 22.6° | 11.1° |
> | **residual** | 22.4° | **3.8°** | 29.9° | 3.7° | 22.6° | 11.1° |
>
> **and the two bounds order the band oppositely.** At 7 bp the search's two standoffs duly come out as *literal translates at one azimuth* (300.0° both), one grounded on **each backbone** of the same duplex — which is what a 3.8° residual buys. At 6 and 11 bp they come out a **half turn apart** instead, and the alignment is still exact. So even the refinement is not a bound: the axial position, the seat and the target pair absorb whatever is left.

---

## The answer, in base pairs

`C-0029`'s admissibility test applied twice — a phosphate pair inside the measured `[0.60, 0.70]` nm step, no van der Waals overlap — with both legs on one shared lateral seat and both junctions grounded on the **seat duplex only**.

| `n` bp | nm | screw image | **worst misalignment** | `cos²ψ` | worst gap [nm] | nt | free phases | `P_c` loaded | `P_c` free | **`P_c`** | plane | **margin** | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **6** | 2.04 | 22.4° | **0.00°** | **1.0000** | 0.690 | **0** | 24 | 9.77 | **8.84** | **8.84** | free | **2.52** | **PASS** |
| **7** | **2.38** | 56.2° | **0.00°** | **1.0000** | 0.697 | **0** | 24 | **9.77** | 10.30 | **9.77** | **loaded** | **2.79** | **PASS** |
| 8 | 2.72 | **89.9°** | **0.00°** | **1.0000** | 0.685 | **0** | 24 | 9.77 | 11.70 | 9.77 | loaded | 2.79 | PASS |
| 9 | 3.06 | 56.3° | 0.00° | 1.0000 | 0.689 | 0 | 24 | 9.77 | 13.00 | 9.77 | loaded | 2.79 | PASS |
| 10 | 3.40 | 22.6° | 0.00° | 1.0000 | 0.697 | 0 | 24 | 9.77 | 14.17 | 9.77 | loaded | 2.79 | PASS |
| 11 | 3.74 | 11.1° | 0.00° | 1.0000 | 0.685 | 0 | 24 | 9.77 | 15.22 | 9.77 | loaded | 2.79 | PASS |
| 12 | 4.08 | 44.9° | 0.00° | 1.0000 | 0.698 | 0 | 24 | 9.77 | 16.14 | 9.77 | loaded | 2.79 | PASS |

Four things fall out and none was assumed.

1. **The azimuth is free.** Not *cheap* — free. Every row's worse chord is at 90.0° from the seat duplex's axis, i.e. exactly on the flexure's. The same holds at the wide 154° groove, with links allowed onto the neighbour duplexes, and with both legs on the seat duplex's own axis (`y_c = 0`). **The only non-zero costs in the whole study are the scaffold-excursion topology at 6 bp (3.13°, 0.30 % of the couple) and at 9 bp (5.87°, 1.04 %).**
2. **The separation is a design variable, and it is not the one `C-0037` thought.** `L2a6` and `L2a8` are bit-identical in the **loaded** plane — reproduced here to three figures, 9.77 pN at both — and differ by **1.32×** in the **free** one, 8.84 against 11.70. `P6` is judged on the **minimum**, so 6 bp buys a 2.52 margin and 7 bp a 2.79. **Seven base pairs is the smallest separation whose free plane has crossed above its loaded one**, and beyond it further spending buys nothing at all.
3. **The junctions land on the two different backbones of one duplex.** The 7 bp optimum grounds one junction on strand 0 (bp 15 and bp 13) and the other on strand 1 (bp 22 and bp 20) — which is the strand swap of bound 2's refinement, appearing in the search rather than being imposed on it.
4. **The optimum is not a scaffold excursion.** `C-0029`'s single junction had its free `R1` optimum land on *consecutive* phosphates, i.e. on `R2`'s constraint. The aligned pair's targets are **two apart** on one strand (15 and 13; 22 and 20), so the aligned routing is two independent staples. The scaffold excursion is still available at every separation and costs at most 1.04 % of the couple.

### The design that results, over `C-0017`'s envelope

Both legs 7 bp = 2.38 nm apart on one seat duplex, one on each backbone; 45 paths; secant placed at 33.3333 pN/nm at 3 nm by construction; favourable mounting; duty at §3's **desired** 10 nm on the element's own end shear.

| `ℓ` [nm] | 5 | 6 | **7** | **8** | 9 | 10 |
|---|---|---|---|---|---|---|
| span [nm] | 33.26 | 33.26 | 33.34 | **33.43** | 33.51 | 33.56 |
| tangent [pN/nm] | 28.89 | 26.85 | 26.16 | **26.09** | 26.27 | 26.57 |
| supply/demand at 3 nm | 1.69 | 2.08 | 2.49 | **2.90** | 3.32 | 3.75 |
| duty(10) [pN] | 6.57 | 4.91 | 4.01 | **3.50** | 3.21 | 3.04 |
| `P_c` [pN] | 18.42 | 14.76 | 11.95 | **9.77** | 8.15 | 6.90 |
| plane | free | free | loaded | **loaded** | loaded | loaded |
| **margin, CanDo** | 2.80 | 3.01 | 2.98 | **2.79** | 2.54 | 2.27 |
| **margin, Fields** | 2.11 | 2.26 | 2.24 | **2.10** | 1.91 | 1.70 |
| verdict | PASS | PASS | PASS | **PASS** | PASS | PASS |

> **Every span, tangent, coupling factor, supply ratio and duty is `C-0037`'s `L2a8` row to the last digit** — because `Σ(Δy_i)² = 0` makes the loaded plane blind to the separation, which is `C-0037`'s own finding 2 and is upheld. What moves is `P_c`, and only through the **free** plane, and only below 7 bp.

---

## The mixed-base truss, which is the one piece of new mechanics

`C-0028`'s sway determinant `sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u` is written for **one** column, and `C-0037` multiplies its root by the leg count — legitimate only because every leg there has the same base. **Two chords at different azimuths give two different `ρ_b` and the assembly is then a genuine two-degree-of-freedom eigenproblem**, so the truss is re-solved as a **beam-column finite element**: Hermite cubics with the consistent geometric stiffness, each leg pinned in translation at its base and held by its own rotational spring, one shared head node carrying `(u, φ)` and the frame couple, every leg carrying `P/n`. The critical load is the smallest total `P` at which the assembled matrix stops being positive definite, found by bisection on the first non-positive pivot of an `LDLᵀ` factorisation — **Sylvester's criterion, which is exact and not a tolerance**.

It was needed even though the answer turned out to be `ψ = 0` everywhere: **without it there is no way to price a misalignment that the search might have found**, and the scaffold-excursion rows (3.13°, 5.87°) are priced on it.

It reproduces, through a completely different route:

- `C-0028`'s own `standoffBucklingLoad` at four lengths, to **2e−4**;
- `C-0037`'s `trussBucklingLoad` over 3 leg counts × 3 frame couples, to **2e−4**;
- `C-0037`'s `L2a8` **loaded 9.7715** and **free 11.7021** pN to **4e−10**;
- `C-0037`'s `trussTipFlexibility` **entry by entry** to 1e−12 over 3 leg counts × 3 frame couples;
- and `C-0028`'s *"a pinned base under a sway column is a MECHANISM, not a weaker strut"* — **exactly zero** at zero base stiffness and zero frame couple.

---

## The five verification gates

Executed as **32 gate-named tests** in `src/test/kotlin/anchoring/PairedPerpendicularJunctionTest.kt`; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree, and the result file re-emitted through `tools/study.sh` reported *"no result file changed"*.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the steric floor is a diameter over a rise, so halving the rise doubles it and it never falls below `2R`; the seat's line contact is a length and scales with the standoff radius; a chord couple is a slide stiffness times a **squared** lever arm, so halving the arm quarters it; a mixed-base critical load is a rigidity over a **squared** length — halving the length at *fixed* dimensionless restraint quadruples it exactly; unphysical arguments throw at **nine** entry points | **PASS** |
| **2 — limiting cases** | the screw image vanishes at 0 bp and returns to 0.34° at 32 bp, and never leaves `[0, π/2]`; the seat's line contact collapses **to a point** at the rim and is even in the offset; the loaded couple fraction is 1 on the axis, 0 across it and even; **one leg with no frame reproduces `C-0028`'s sway column**, and equal bases reproduce **`C-0037`'s truss buckling load** and **its assembled tip flexibility entry by entry**; a mixed truss lies **strictly between** its all-weak and all-strong readings; pinned bases with no frame couple give **exactly zero**; the sheet-phase residual **never exceeds** the screw image it refines, and at a 180° groove a strand swap *is* a half turn so it buys exactly nothing | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the finite element load falls **monotonically** on nested meshes 8 → 16 → 32 → 64 and moves **6.1e−10** between the last two; the pair's alignment is **bit-identical** at azimuth grids 120 / 180 / 360 / 720 and at axial grids 2 / 4 / 8 / 16 steps per base pair (departure `0.00e+00` at every level); the search returns the **identical configuration** — `centreX`, azimuth and objective — on a repeat call | **PASS** |
| **5 — literature and upstream** | `C-0029`'s terminal chord (2.000), lever arm (1.000), ceiling (**78.24**), free axis (**13.53**), azimuthal quantum (**33.74°/bp**) and its own `cos²(16.87°) = 0.9158`; `C-0037`'s `L2a8` second moment (3.699), loaded (**9.77**) and free (**11.70**) critical loads; `C-0015`'s 32 bp = 3 square-lattice turns; the SAXS **2.69**; Fields et al.'s **172.906**; and the measured step `[0.60, 0.70]`, the phosphate radius **1.00** and the rise **0.34** asserted to be what the search actually uses. Worst departure over 14 reproductions: **3.1e−4**, which is `C-0015`'s own published rounding of 32.01 to 32 | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The chord's two axes are a rank-one tensor and their sum is invariant.** `loaded + free = 4k_bond,θ + 2k_bond,s a² = 91.76 pN·nm/rad` at **every** misalignment — `C-0037`'s `Σx_i² + Σy_i² = w²/2` one level *down*, on the **base** rather than on the frame. Asserted at seven angles, with each half asserted equal to its own `cos²`/`sin²`.
2. **The chord azimuth is a function of the standoff's own azimuth alone** — `ψ₀ + Δ/2 + π/2` — and not of where it sits nor of which phosphates it links to. Asserted over the solved placements at five axial positions × three seats. **Nothing in the search imposes it, and it is what makes the alignment a one-parameter matter.**
3. **A chord is a line**: its misalignment is invariant under a half turn, asserted at five angles, and never exceeds `π/2`.
4. **A mixed truss does not care in which order its legs are listed** (exact as physics, limited only by the `LDL` elimination order — agrees to 1e−9), and **a junction pair is invariant under exchanging its two legs**.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the pair not fitting at any separation | **no** | it fits at all seven searched, and the closure has **zero** unpaired nucleotides everywhere |
| 2 | the pair fitting only at separations the frame cannot use | **no** | 6 bp, the steric floor itself, already fits |
| 3 | **bound 2 binding** — the second junction forced onto the screw image | **no, and its failure is the answer** | 0.00° against a bound that says 89.9° at 8 bp |
| 4 | **the alignment being free but the seat degenerate** | **no** | the seats swept keep the flat-face line contact above 1.60 nm; the seat duplex's own axis alone (contact 2.00 nm) gives the *same* 0.00° and the same 9.77 pN, and so does a sweep out to ±0.9 nm |
| 5 | the mixed solver failing to reproduce `C-0037` | **no** | 4e−10 on both planes of `L2a8` |
| 6 | no crossover phase leaving the targets free | **no** | 24 of 32 at every separation |

**A result that was not anticipated**, and it is the one the pre-registered prediction got wrong: the prediction said *"the residual misalignment is expected to be several degrees rather than `C-0029`'s 2.2°"*. It is **zero**, and the reason is that the chord azimuth is not on a lattice at all — [`CH-0056`](../challenges/CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md).

---

## Sensitivities — what moves a verdict and what does not

| axis | range | worst misalignment | `P_c` [pN] | margin | verdict moves? |
|---|---|---|---|---|---|
| **lateral seat** | the seat duplex's own axis only (`y_c = 0`) | **0.00°** | 9.77 | 2.79 | **no** |
| **lateral seat** | out to ±0.9 nm (line contact down to 0.87 nm) | **0.00°** | 9.77 | 2.79 | **no** |
| **groove convention** | nominal 120° → wide 154° | **0.00°** | 9.77 | 2.79 | **no** |
| **routing topology** | independent staples → scaffold excursion | 0.00° at 7 bp (3.13° at 6, 5.87° at 9) | 9.77 | 2.79 | **no** |
| **link targets** | seat duplex only → seat plus both neighbours | **0.00°** | 9.77 | 2.79 | **no** |
| **window centring** | `\|gap − 0.65 nm\|` as a tie-break, and **priced at 5.7° of alignment per 0.05 nm** | 0.00°, worst gap **0.6969 nm** either way | 9.77 | 2.79 | **no**, and see the validity range |
| **`k_s`** (`C-0020`'s four decades, unmeasured) | ×1/32 → ×8 | 0.00° | 3.28 → 10.30 | **0.94** → 2.94 | **YES, at ×1/32** — the same crossing `C-0028`, `C-0030` and `C-0037` all report |
| **separation** | 6 → 7 bp | 0.00° | **8.84 → 9.77** | **2.52 → 2.79** | **no, and it is the design finding** |

> **`k_s` remains verdict-critical and this task does not remove that**, exactly as in `C-0028`, `C-0030` and `C-0037`. `T-9` is still the task that settles it.
>
> **The window centring is the one place where the alignment is not free.** The aligned pair's *binding* link sits at **0.6969 nm** — the **C2′-endo** end of the measured `[0.60, 0.70]` nm step — against `C-0029`'s single junction at **0.600 nm**, the C3′-endo end. Pricing the centring at 5.7° of alignment per 0.05 nm does not move it, so this is a property of the aligned solutions and not of the objective. **The couple is free; the sugar pucker is not.**

---

## Does `C-0037`'s verdict survive?

**In full, and one of its caveats can be struck.**

| `C-0037` said | this claim finds |
|---|---|
| **the largest open item is whether two 90° junctions can close 6–8 bp apart** | **they can, at every separation from the 6 bp floor to 12 bp**, with zero unpaired nucleotides, four distinct targets and 24 free crossover phases |
| *"a demonstration that two 90° junctions cannot close … is the single largest risk"* | **the risk is retired**, at the level a phosphate-distance model can retire it. `T-71`'s torsion check remains, and it now has two junctions to check |
| the base chord is **assumed** laid along the flexure axis, and `C-0029`'s worst phase would cost 8.4 % | **the assumption is right and the 8.4 % is not a debt at all** — the search *achieves* 0.00° for both legs — but the reason it is not a debt is that the quantum was applied to the wrong body: [`CH-0056`](../challenges/CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md) |
| `L2a6`, `L2a8`, `L2a12` are **bit-identical**, so the cost is the leg COUNT not the leg SPACING | **upheld exactly in the LOADED plane** — 9.77 pN, span 33.43, tangent 26.09, `Φ` 0.258, supply 2.90 at all three — and **not in the free one**, 8.84 / 11.70 / 16.14 |
| the loaded plane becomes the minimum *"between 6 and 8 bp"* | **resolved: at 7 bp.** `P_c` free 10.30 > loaded 9.77 |
| the recommended row is **8 bp = 2.72 nm** | **7 bp = 2.38 nm delivers the identical design 0.68 nm narrower**, which is `T-96`'s currency |
| `P9` — an off-square row overloads its outermost leg | **untouched**: `Σ(Δy_i)² = 0` is enforced as `Q4` and holds exactly, both legs sharing one seat |
| the cap's **geometry** is asserted, not designed | **still true and still open.** This claim places the two *bases*; it does not design the *cap* |

---

## The literature

`C-0037` fetched, re-read and hand-counted the load-bearing sources for this motif in the iteration before this one, and this task **re-uses those readings rather than re-summarising them** — each is flagged with where it was read.

| question | answer | flag |
|---|---|---|
| **Is there a published rule for how far apart two duplexes protruding from one origami face must sit?** | **NOT FOUND.** 11 further EuropePMC queries in this task, on top of `C-0037`'s ~72 and `C-0029`'s ~110. Query strings recorded below | **not found** |
| **How many spacers does the literature's only rigid out-of-plane mounting have, and does the paper say how they are arranged?** | **Exactly two, each 39 bp, one covalent link per end — and the paper does NOT say how they are arranged**: the word *"spacer"* occurs twice in the whole article. **So there is no published precedent to agree or disagree with this task's separation** | **read directly**, by `C-0037`, from Pumm et al., *Nature* **607**:492 and its SI strand table |
| **The measured step the closure test is written on** | *"C3-endo (interphosphate distance 0.6 nm) to C2-endo conformation (interphosphate distance 0.7 nm)"* — a **window** | **read directly**, by `C-0029` (Bosco, Camunas-Soler & Ritort, *NAR* **42**:2064) |
| **The phosphate radius the counting theorem rests on** | *"Phosphates (red circles) sit at a radius of `a_DNA ≈ 10 Å`"* | **read directly**, by `C-0029` (Hedley et al., *Phys. Rev. X* **14**:031042) |
| **Has a torsion-level check of any 90° scaffold excursion been published?** | **NOT FOUND**, and it is this project's own `T-71` | **not found** |

**Query strings recorded, so the negative is falsifiable by one paper** (EuropePMC REST search, ~9 s apart):
`ABSTRACT:"DNA origami" AND ABSTRACT:"protruding" AND ABSTRACT:"spacing"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"two duplexes" AND ABSTRACT:"perpendicular"` (0);
`ABSTRACT:"DNA nanostructure" AND ABSTRACT:"standoff"` (0);
`TITLE:"DNA origami" AND ABSTRACT:"pillar" AND ABSTRACT:"spacing"` (0);
`ABSTRACT:"origami" AND ABSTRACT:"scaffold excursion"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"out-of-plane" AND ABSTRACT:"helix"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"two spacers"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"vertical" AND ABSTRACT:"duplex"` (0);
`ABSTRACT:"origami" AND ABSTRACT:"protrusion" AND ABSTRACT:"rigid"` (1 — a soft-robotics skin review, unrelated);
`ABSTRACT:"DNA" AND ABSTRACT:"90 degree" AND ABSTRACT:"junction" AND ABSTRACT:"helix"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"tripod"` (5 — all 3-D nanorod scaffolds, none an out-of-plane duplex on a single-layer sheet).

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** Two duplexes standing normal to a single-layer sheet under a shared cap is not in the literature; the closest thing in print is inclined 50° on a multilayer platform.
- **The closure test is `C-0029`'s and inherits its caveat exactly, twice over**: a phosphate pair inside the measured `[0.60, 0.70]` nm step with no van der Waals overlap. **No backbone torsion angle is checked and no sequence is designed.** A *"closes"* verdict is an **upper bound on buildability**; only a *"does not close"* verdict would be a proof of impossibility. `T-71`.
- **And the aligned pair sits at the C2′-endo end of that window** — binding link 0.6969 nm against `C-0029`'s 0.600 — which is where the torsion check is *least* comfortable. Whoever runs `T-71` must run it at the pucker the alignment demands, not at `C-0029`'s.
- **The seat is a BOUND, not an output.** An unbounded search parks its optimum on the **rim** of the seat duplex, where the flat end face's line contact `2√(R² − y_c²)` has collapsed to a point and the standoff is balanced on an edge. `seatFaceHeight` alone does not exclude that; `seatContactLength` is introduced here to do so, and the adopted sweep keeps the contact above **1.60 nm**. The verdict is unchanged at `y_c = 0` (contact 2.00 nm) and out to ±0.9 nm.
- **The sheet's neighbouring duplexes are given the SAME helical phase as the seat duplex**, exactly as `C-0029` assumes. Letting the phase be chosen could only make closure easier.
- **A link to a *neighbour* duplex changes the ground, and that is not modelled.** The adopted design grounds every link on the seat duplex; the "seat + neighbours" variant is reported and gives the same numbers, but its base would sit on the sheet's across-helix compliance rather than on one duplex, and that compliance is `C-0020`'s and is not carried into `k_s` here.
- **`Q4` is enforced, not discovered**: both legs share one lateral seat, so `Σ(Δy_i)² = 0` exactly and `C-0037`'s `P9` stays vacuous. A design that let the two seats differ would spend frame couple on the loaded plane, and `C-0037` shows that fails at **12 of 12** azimuths.
- **The mixed-base truss shares the axial load equally**, `P/n`, which is exact for a symmetric two-leg row under a centroidal shear and is `C-0037`'s own assumption. The frame couple is taken to be unaffected by the axial preload, and cap yaw is not modelled.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction**, and the base couple, the cap and the frame all rest on it. Swept four decades; the verdict moves at `k_s/32`, as in `C-0028`, `C-0030` and `C-0037`.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**; every margin is also given on Fields et al.'s implied **172.9**, which is the measured end and 25 % lower.
- **SMALL DEFLECTION**, exactly as `C-0025`, `C-0028`, `C-0030` and `C-0037` flag.
- **The crossover occupancy is a count, not a routing.** It says how many of the 32 phases leave the four target base pairs free; it does not design the staple set, and it treats a crossover column as occupying one base pair index across the sheet.
- **The search is a grid, and its optimum is `a` solution and not `the` solution.** Every grid was tripled and the answer did not move by one bit, but a grid cannot prove a *non-existence* finer than itself — which matters only if a future task needs a *negative* here, and this one does not.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| phosphate radius | 1.00 nm | **CITED, READ DIRECTLY** (Hedley et al., *Phys. Rev. X* **14**:031042, 2024), via `C-0029` |
| intrastrand phosphodiester step | **0.60–0.70 nm** | **CITED, MEASURED** (Bosco et al., *NAR* **42**:2064, 2014), via `C-0029`. A **window** |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al. (2009) |
| base pairs per turn | 10.67, square lattice | **CITED** |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012) |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED** (Wang et al., *Biophys. J.* **72**:1335, 1997) |
| `k_bond,θ` | 6.765 pN·nm/rad | **CITED+FITTED** (Chen et al., *JACS* **136**:6995, 2014) via `C-0009` |
| `k_bond,s` | 32.35 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, and a verdict moves |
| Fields et al.'s implied rigidity | 172.9 pN·nm² | **CITED, MEASURED** (Fields, Meyer & Cohen, *NAR* **41**:9881, 2013) |
| crossover recurrence along one duplex | 16 bp | **CITED** via `C-0015` |
| Pumm et al.'s spacer count and length | 2 per plate, 39 bp | **CITED, READ DIRECTLY** by `C-0037`; used only as a cross-check |
| `C-0029`'s ceiling and `C-0037`'s `L2a8` design | — | **CITED**, and reproduced here as gate-5 tests |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the steric floor, the seat's line contact, the screw image and the sheet-phase residual, the chord's two axes and their conserved sum, the pair closure search and every placement it returns, the mixed-base finite element and every critical load, span, tangent, supply ratio, duty, margin and verdict — is **derived here in code**, with `C-0029`'s, `C-0030`'s and `C-0037`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The backbone torsion of the pair.** `T-71`, now with two junctions and at the C2′-endo end of the window.
2. **The cap as a solved body rather than a series spring.** `C-0037`'s open item 2, untouched here: this claim places two *bases*, not a cap.
3. **`k_s`.** `T-9`, and it still moves a verdict at ×1/32.
4. **Whether the plan view admits 180 standoffs and 45 flexures on a 40 × 40 nm footprint.** `T-96`. This claim narrows the row from 2.72 to 2.38 nm, which is 0.68 nm × 90 rows of currency there.
5. **Whether this branch should be preferred to `E5a16` at all.** `T-98`. **This claim retires the standoff branch's largest chemistry risk and therefore changes the count of open premises `T-98` has to weigh — it does not argue the branch should be taken.**

## Challenges

**Raises [`CH-0056`](../challenges/CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md)** against `C-0029`'s azimuthal quantum as inherited by `C-0037`. **No number in either claim fails to reproduce** — 14 reproductions at ≤ 3.1e−4, and `C-0037`'s `L2a8` critical loads at 4e−10 through an independent solver.

**None stands against this claim.** The four ways it would fail:

1. **A torsion or coarse-grained check showing the closed routing does not close.** `T-71`. It can only make the answer worse, and it is the honest ceiling on everything here.
2. **A demonstration that the two leg heads cannot be tied.** Unchanged from `C-0037`: this claim places the bases and not the cap.
3. **A measurement of `k_s` more than ~30× below `C-0020`'s construction.**
4. **A steric model finer than a cylinder of radius 1.0 nm.** The floor is `2R`; a real duplex has grooves and a real end face has a rim, and either could raise or lower it by a base pair.
