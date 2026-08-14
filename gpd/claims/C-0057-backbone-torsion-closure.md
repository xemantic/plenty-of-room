# C-0057 — The dihedrals do NOT close at any of the three reported optima, and the reason is not that the junction is impossible but that the objective is BLIND: a phosphate-distance search picks the extremes of the measured window, which are exactly the placements a backbone cannot make — of 69 120 placements in `C-0029`'s own space, 3 546 close on distance, 1 855 survive a closed-form reach bound, and 18 of the 100 solved close at torsion level, none of them the one `C-0029` reports

| | |
|---|---|
| **Task** | [`T-71`](../tasks/T-71-backbone-torsion-closure.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (crystallographic backbone geometry placed into this programme's own solved junction placements, and the phosphodiester closed by exact inverse kinematics) **+ logical** (two closed-form reach bounds that run before any solve, one of which is a proof of impossibility) **+ literature** (a measured torsion distribution and a measured restraint library derived here from 876 PDB entries, cross-checked against four primary sources read directly) |
| **Verdict** | **PASS on the acceptance question, and the answer has two halves that must be quoted together.** **First: the dihedrals do not close at any of the three reported optima.** `C-0029`'s single junction closes **0 of 4** links, `C-0042`'s pair **1 of 8**, `C-0052`'s trio **2 of 6** — and the failures are not marginal. Four of the eighteen links are excluded by a **closed-form reach bound**, i.e. they close at **no torsion whatever**: `C-0042`'s two 7 bp legs would need an `O3′–P` bond of **0.2517 and 0.2460 nm** against a covalent 0.1602, and `C-0052`'s `+w/2` leg 0.1821. The rest fail on **population**: `C-0029`'s own binding link needs **ε = −22.9°**, a torsion carried by **0.015 %** of 13 084 measured linkages, and its second link needs **β = 27.4°**, carried by **zero of 15 457** measured residues. **Second, and it is the finding: a torsion-feasible placement EXISTS in `C-0029`'s own search space, and none of the three claims found one, because none of them was looking.** A census of all **69 120** placements on `C-0029`'s grid finds **3 546** that close on phosphate distance, **1 855** that survive the reach bound, and **18 of the 100 solved** that close at torsion level — at gaps around **0.690 nm**, in the *interior* of the measured window, where a distance-minimising objective never goes. The **scaffold excursion** — the topology `C-0029` recommends and `T-71` names — is **12.7× rarer**: 280 covalent placements against 3 546, and **1 of 100** closing. So `C-0029`'s *"a 90° routing exists"* survives; **its routing does not** ([`CH-0070`](../challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md)). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED about the junction.** A torsion check is a **necessary** condition and never a sufficient one — exactly as `C-0029` said of the phosphate distances. It does not establish that any junction assembles, folds, or survives a buffer; it establishes only that a backbone is or is not excluded by its own covalent geometry and by the conformations DNA is *observed* to adopt. |
| **Provenance** | `gpd/results/T-71-backbone-torsion-closure.json`, produced by `anchoring.BackboneTorsionStudyKt`; **9 cheap-bound quantities, 10 baseline closures, 36 link closures, 3 scale verdicts, 2 census records, 10 sensitivities, 11 convergence records, 20 upstream reproductions, 7 literature records**; **25 gate-named tests in `BackboneTorsionTest`**; `tools/verify.sh` **BUILD SUCCESSFUL on the whole suite, with no `--drop-file` needed** (an earlier run in this iteration needed a concurrent agent's half-written `coupling/NonUniformCouplingStudy.kt` dropped). The result file was re-emitted through `tools/study.sh` and reported *"no result file changed"* — **byte for byte**. The measured backbone is `gpd/data/T-71-bdna-backbone-survey.json`, produced by `tools/T-71-bdna-backbone-survey.py`; the Kotlin constants are **generated**, not transcribed, by `tools/T-71-emit-kotlin-constants.py` |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 0.34 nm rise, 10.67 bp/turn; phosphate radius **1.00 nm**, groove **120°** — `C-0029`'s own geometry throughout, with 0.90 nm / 154° / 180° carried as sensitivities. Torsions **degrees**, IUPAC, folded to `(−180, 180]`; lengths **nm**. Covalent restraints measured on **13 084** crystallographic phosphodiester linkages; populated regions on **15 457** residues from **876** X-ray, DNA-only PDB entries at ≤ 2.3 Å |
| **Consumes** | [`C-0029`](C-0029-perpendicular-junction-routing.md) (`DuplexBackbone`, `sheetPhosphate`, `standoffTerminus`, `seatFaceHeight`, `linkWindowResidual`, `bestTwoLinkClosure`, `RoutingTopology` — **re-run as a library**), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`PairedJunctionSearch`, `StandoffPlacement`), [`C-0052`](C-0052-crossbar-junction-trio.md) (`CrossbarTrioSearch`, `TrioPlacement`, `TrioJunctionSpec`), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0070`](../challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md) against `C-0029`'s reported routing as inherited by `C-0042` and `C-0052` |

---

## The claim, in one line

**Every closure result in this programme was minimising the wrong thing: a phosphate pair is joinable only if a real backbone can be built between the two residues, and pushing the pair to the edge of the measured `[0.60, 0.70]` nm window — which is what minimising a window residual does — pushes the backbone out of the conformations DNA has ever been seen in; so all three reported optima fail, four of their links are excluded by a closed-form bound that needs no simulation at all, and yet the junction is not dead, because 18 of the 100 best placements on the same grid do close, at gaps in the middle of the window that the old objective had no reason to visit.**

---

## The two cheap bounds, which ran first and settled four of the eighteen links

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the pinned-phosphate bond.** If the two phosphorus atoms a closure search matched really are the two phosphorus atoms of one step, the donor's `O3′` — rigid on its own sugar — must sit one `O3′–P` bond from the acceptor's `P` | **0.16022 ± 0.00191 nm**, measured on 13 084 linkages | **0 of 18 links** anywhere satisfy it. But this reading's own **baseline** is 7.09–8.50 σ, so it has no discriminating power and **no verdict is taken on it** |
| **2** | **the free-phosphate reach.** Let the bridging phosphorus sit where chemistry puts it. Then the only geometric demand is that the donor's `O3′` and the acceptor's `C5′` be separated by a distance the chain `O3′–P–O5′–C5′` can span — a closed-form interval in the single torsion `α` | **[0.2693, 0.3855] nm**, widened to **[0.2228, 0.4095]** at three measured σ on every bond and angle | **A link outside it closes at NO torsion whatever.** It excludes **4 of the 18** links — both of `C-0042`'s 7 bp inner links and one of `C-0052`'s legs — in microseconds, and it is a **proof**, not a search |

> **This is the Plan's cost justification and it held.** An oxDNA or all-atom minimisation is the natural instrument and it is also the wrong first spend: a coarse-grained model does not represent the dihedrals the question is about, and an all-atom minimiser would report a *local minimum* exactly where a reach bound reports an *impossibility*. Bound 2 costs three atom placements and a distance, which is why it could be swept over 69 120 placements — and that sweep is the result.

---

## The calibration, which is the gate that licenses everything else

`T-71`'s falsifier 1 was that a step *inside* a real duplex might not return canonical torsions, in which case a junction's residual would be measuring the template and not the junction. It did not fire — **but only after the baseline was rebuilt.**

| baseline | reading | `O3′–P` [nm] | worst z | least z | rarest torsion | closes |
|---|---|---|---|---|---|---|
| **MEASURED dinucleotide, C2′-endo** (`8FB4 C/11 DA`) | PINNED | 0.1594 | **1.31** | 0.68 | ζ, 8.0 % | **yes** |
| **MEASURED dinucleotide, C3′-endo** (`5XK1 C/5 DC`) | PINNED | 0.1602 | **0.31** | 0.31 | ζ, 9.5 % | **yes** |
| **MEASURED dinucleotide, C2′-endo** | FREE | 0.1550 | **2.74** | 2.17 | δ, 8.3 % | **yes** |
| **MEASURED dinucleotide, C3′-endo** | FREE | 0.1618 | **0.82** | 0.82 | χ, 10.3 % | **yes** |
| one template reapplied at its own fitted screw | PINNED | 0.1747 | 7.54 | 7.54 | — | **no** |
| this project's **stylised** duplex (10.67 bp/turn, 0.34 nm, `r = 1.00` nm) | PINNED | 0.1738 | 7.09 | 7.09 | — | **no** |
| this project's **stylised** duplex | FREE | 0.1653 | **2.64** | 1.02 | ζ, 8.0 % | **yes**, at α −54.7, β 180.0, γ 55.5, δ 129.6, ε 176.6, ζ −104.3 — **canonical BI** |

Three things fall out and one of them changed the method.

1. **A real dinucleotide closes under both readings and this project's own stylised duplex closes at canonical BI torsions.** The instrument discriminates: baselines run 0.31–2.74 σ, junction links run 0.19–57.8.
2. **Reapplying one template at its own fitted screw does NOT reproduce a real step** — the reconstructed `O3′···P` is out by 0.0145 nm, which is **7.6 bond-length standard deviations**. A local helical axis fitted from coordinates is an approximation, and at a covalent bond's tolerance that approximation is not small. So the free limiting case carries the medoid's **actual successor residue**, in its own local frame about the same axis, at the actual azimuth and rise between the two phosphorus atoms — a real molecule, not a copy.
3. **The PINNED reading is not usable and is reported anyway.** Its baseline is 7.09–8.50 σ on a *reconstructed* duplex, because it over-determines the phosphorus by three degrees of freedom. Every verdict here is taken on **FREE**, which is the reading that cannot be blamed on where a search put a marker.

---

## The verdict at the three scales

`C-0029`'s, `C-0042`'s and `C-0052`'s own searches re-run as libraries, parameter for parameter, and every link they return solved.

| scale | claim | junctions | links | reach-feasible | covalent-acceptable | populated | **closing** | verdict |
|---|---|---|---|---|---|---|---|---|
| single junction | `C-0029` | 2 routings | 4 | 4 | 2 | **0** | **0** | **DOES NOT CLOSE** |
| junction pair, 6 and 7 bp | `C-0042` | 4 | 8 | 4 | 2 | 3 | **1** | **DOES NOT CLOSE** |
| crossbar trio, 13 bp | `C-0052` | 3 | 6 | 5 | 4 | 2 | **2** | **DOES NOT CLOSE** |

### `C-0029`, link by link — and both its routings return the same two links

| routing | link | gap [nm] | `O3′–P` demanded | reach | worst z | α | β | γ | δ | ε | ζ | χ | rarest | closes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `R1` / `R2` | 1 | 0.6000 | **0.1689** | ok | **4.55** | 173.8 | 117.8 | −145.0 | 129.6 | **−22.9** | −0.1 | −111.3 | **ε, 0.015 %** | **no** |
| `R1` / `R2` | 2 | 0.6000 | 0.1645 | ok | **2.76** | 47.0 | **27.4** | 30.7 | 84.4 | −15.0 | 98.3 | −161.2 | **β, 0.000 %** | **no** |

> Link 2's covalent geometry is *acceptable* — 2.76 σ, inside the ceiling. What kills it is that **β = 27.4° has been observed in none of 15 457 crystallographic residues**, and this is precisely the failure mode the literature describes at a four-way junction: *"the junction site itself is formed by a sharp turn in the phosphodiester backbone … captured mainly by a change in the ε, ζ, α + 1, β + 1, and γ + 1 torsions, which adopt unusual values … However, the scarcity of structural data did not allow to classify the junction-site step as a distinct conformation."*

### `C-0042` and `C-0052` — where the reach bound bites

| scale | junction | link | gap [nm] | `O3′–P` demanded [nm] | reach | worst z | closes |
|---|---|---|---|---|---|---|---|
| pair, 6 bp | leg 1 | 1 | 0.6897 | **0.2417** | **fails** | 42.57 | no |
| pair, 6 bp | leg 2 | 2 | 0.6529 | **0.2709** | **fails** | 57.82 | no |
| **pair, 7 bp** | **leg 1** | **1** | **0.6969** | **0.2517** | **fails** | **47.83** | **no** |
| **pair, 7 bp** | **leg 2** | **1** | 0.6958 | **0.2460** | **fails** | 44.82 | no |
| pair, 7 bp | leg 1 | 2 | 0.6501 | 0.1653 | ok | 2.65 | **yes** |
| pair, 7 bp | leg 2 | 2 | 0.6370 | 0.1505 | ok | 5.06 | no |
| trio, 13 bp | leg +w/2 | 1 | 0.6000 | **0.1821** | **fails** | 11.44 | no |
| trio, 13 bp | leg +w/2 | 2 | 0.6065 | 0.1627 | ok | 1.35 | **yes** |
| trio, 13 bp | flexure end | 2 | 0.6212 | 0.1555 | ok | 2.46 | **yes** |
| trio, 13 bp | leg −w/2, flexure end 1 | — | 0.61–0.68 | 0.164–0.166 | ok | 1.36–3.08 | no (ε, β unpopulated) |

> **`C-0042`'s 0.6969 nm binding link — the one its own validity range flagged as *"where the torsion check is least comfortable"* — is the link the reach bound excludes.** It is not uncomfortable; it is impossible, and the bound that says so is closed form and ran in microseconds. `C-0042`'s instinct was right and its number was the right number to worry about.

---

## The census — and it is the finding

`C-0029`'s search minimises a **phosphate-distance** residual and nothing else, so whether its argmin is torsion-feasible is an accident of that objective. The honest question is whether **any** placement in the same space closes, and bound 2 is cheap enough to ask it of every one.

| topology | placements | close on distance | pass the reach bound | solved | **close at torsion level** |
|---|---|---|---|---|---|
| **two independent staples** | **69 120** | **3 546** (5.1 %) | **1 855** (52.3 % of those) | 100 | **18** |
| **scaffold excursion** | **69 120** | **280** (0.4 %) | **137** (48.9 %) | 100 | **1** |

Four things fall out and none was anticipated.

1. **A torsion-feasible placement exists, and `C-0029` did not find one** — not because its search was wrong but because nothing in its objective could see the difference. Its best independent-staple placement is at gap **0.600 nm**, the very edge of the window; the census's best is at **0.690 nm**, in the interior.
2. **The reach bound alone halves the space.** Of the placements that close on distance, **47.7 % close at no torsion whatever** — that is a bound, not a statistic, and it is available before any solve.
3. **The scaffold excursion is 12.7× rarer than independent staples** on the distance criterion (280 against 3 546) and **18× rarer** on the torsion one (1 against 18). `C-0029`'s finding that *"the best independent routing IS the scaffold excursion"* was a property of its argmin; on the torsion criterion the two topologies are not interchangeable at all.
4. **The best torsion-feasible chords are not the ones the design wants.** The independent-staple optimum's chord comes out at **159.0°** and the scaffold excursion's at **−51.0°**, against the **−87.8°** `C-0029` reports and the 90.0° `C-0042` needs on the flexure axis. **Whether a placement can be simultaneously torsion-feasible and correctly aligned is not answered here**, and it is the task this claim opens.

---

## The measured backbone, and why nothing here is a citation

Every constant the check runs on is derived from crystallographic coordinates in this iteration, by `tools/T-71-bdna-backbone-survey.py`, and emitted into Kotlin by `tools/T-71-emit-kotlin-constants.py` so that **no number is transcribed by hand**. RCSB search: X-ray, polymer composition **DNA only**, resolution ≤ 2.3 Å; 900 entries returned, **876** carrying usable deoxyribonucleotides; **15 457** residues, **13 084** phosphodiester linkages, **8 883** helical steps.

| quantity | measured here | published | departure |
|---|---|---|---|
| `P–O3′` bond | **0.16022 ± 0.00191 nm** | 1.607(12) Å | 0.30 % |
| `P–O5′` bond | **0.15955 ± 0.00180 nm** | 1.593(10) Å | 0.15 % |
| `C3′–O3′–P` | **121.30 ± 3.06°** | 119.7(12)° | 1.6° |
| `O3′–P–O5′` | **103.29 ± 2.51°** | 104.0(19)° | 0.7° |
| `P–O5′–C5′` | **120.12 ± 2.84°** | 120.9(16)° | 0.8° |
| BI α, β, γ, δ, ε, ζ, χ | **−59.5, 172.3, 48.7, 131.3, −173.0, −98.0, −108.9** | −61.0, 179.3, 48.4, 132.8, −178.3, −96.8, −109.7 | ≤ 7.1° |
| C3′-endo δ | **85.7°** | 82.1 ± 0.7° | 3.6° |
| intrastrand `P···P`, C3′-endo | **0.6072 ± 0.0044 nm** | 0.6 nm | 1.2 % |
| intrastrand `P···P`, C2′-endo | **0.6645 ± 0.0036 nm** | 0.7 nm | 5.1 % |
| B-form phosphate radius | **0.8901 nm** (medoid), 0.8901 ± 0.0066 (population) | 1.00 nm adopted, 0.90 nm bracket | lands on the **narrow** end |

> **The `[0.60, 0.70]` nm window this whole programme is written on has no primary source, and now it has a measurement.** Bosco et al.'s sentence is verified verbatim — *"a fraction of the deoxyriboses could interconvert from C3-endo (interphosphate distance 0.6 nm) to C2-endo conformation (interphosphate distance 0.7 nm)"* — but its own references are **two textbooks** (Saenger 1984; Bloomfield, Crothers & Tinoco 1999), neither reachable. Measured here on 13 084 linkages the pair is **0.607 and 0.664 nm**: the *ordering* and the *pucker coupling* are confirmed, and the C2′-endo end is **5.1 % shorter** than the number in circulation. **The window this programme searches is wider at the top than the backbone actually is.**

### The populated-region test, and why a conformer class will not do it

The first attempt judged a septet by its distance to the nearest of 12 k-means conformer classes. It does not work: the diffuse classes have 99th-percentile radii above **150°**, so *"inside the nearest class"* admits almost any septet, and the baseline demonstrated it by landing 133° from its own class and being called populated. The adopted test is **marginal and non-parametric**: a ten-degree occupancy histogram per torsion, and a torsion counts as populated if its bin holds at least **0.1 %** of the observed residues. Uniform occupancy would be 2.78 % per bin, so the floor is permissive by **28×** — deliberately, so that no verdict rests on where a threshold was put. It lets the claim make the statement it wants to make: *"β = 27.4° lies in a bin holding zero of 15 457 observed residues."*

---

## The five verification gates

Executed as **25 gate-named tests** in `src/test/kotlin/anchoring/BackboneTorsionTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a torsion is zero at cis and 180 at trans exactly, **odd under reflection** while a bond angle is **even**; an atom placed by bond, angle and torsion reproduces all three to 1e−9; the phosphodiester reach is a length, **even in its torsion**, minimal at `α = 0` and maximal at 180, and its tolerant reading is strictly wider on both sides; unphysical arguments throw at **five** entry points | **PASS** |
| **2 — limiting cases** | **the free limiting case**: a REAL measured dinucleotide returns its own α, β, γ, ε, ζ to within 6° under both readings, at ≤ 1 σ of least strain, in the most populated conformer class there is; a **C3′-endo** dinucleotide closes at its own, different, δ, and the two puckers' δ differ by more than 30°; a rigid residue's δ and χ are its template's identically; the PINNED reading carries exactly **two more** residuals than FREE; a link 50 nm away is excluded by the reach bound with no solve, and a real step passes both bounds | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the closure solve moves **1.6e−6** in the baseline between 180 and 720 grid steps and returns a **bit-identical** torsion set on a repeat call; the survey behind every constant carries > 1 000 residues, > 1 000 linkages and > 30 steps per template, from > 100 entries | **PASS** |
| **5 — literature and upstream** | the measured linkage reproduces Parkinson et al.'s restraint library to ≤ 1.6° and ≤ 0.3 %; the largest conformer class reproduces Svozil et al.'s BI to ≤ 7.1°; the pucker↔`P···P` coupling is measured rather than inherited; the measured phosphate radius lands inside `C-0029`'s own 0.90–1.00 nm bracket; the stylised duplex's intrastrand step is 0.67265 nm; and **`C-0029`'s 0.600, `C-0042`'s 0.6969 and `C-0052`'s 0.679 nm binding links reproduce to 7.9e−5, 3.4e−5 and 5.7e−4** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **Every torsion and every residual is invariant under a rigid motion**, asserted to 1e−7 under an arbitrary rotation about `(0.31, −0.77, 0.55)` composed with a translation. Nothing in the construction imposes it: the frames are rebuilt from the moved coordinates.
2. **A torsion is odd under reflection and a bond angle is even**, asserted on the same four points — the chirality the whole backbone convention rests on.
3. **The local frame is right-handed for BOTH strand polarities**, asserted as `ê_r × ê_t · ê_z = 1` and `ê_z · ẑ = ±1`. This is what licenses one template serving both strands: flipping `ê_z` flips `ê_t` with it, which is a proper rotation and not a reflection, so no torsion changes sign spuriously.
4. **The conformer metric is a metric on the circle** — zero on itself, symmetric, blind to a full turn in either direction — and the classes' fractions sum to one, because a k-means assignment is a partition.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | **the baseline failing** | **yes, and it changed the method** | reapplying one template at its own fitted screw is out by 7.6 bond σ. The baseline was rebuilt on the medoid's **actual successor**, and then a real dinucleotide closes at 0.31–2.74 σ under both readings |
| 2 | the cheap bound neither excluding nor admitting | **no** | it excludes **4 of 18** links outright and **47.7 %** of the census's distance-feasible placements |
| 3 | **the verdict being the pucker's** | **no** | 0 of 4 links close at C2′-endo only and 0 of 4 at C3′-endo only; both puckers are swept per residue in the adopted reading and the verdict is unchanged |
| 4 | **the verdict being the phosphate radius's** | **YES — and it is reported as such** | at `C-0029`'s own 0.90 nm bracket end the single junction's two links **do** close (2.99 σ), and at 1.00 nm they do not. See the validity range: the *placement* moves with the radius, and the torsion feasibility of a placement chosen by a distance objective is not stable under it. This is the same fact the census reports and is why the census, not the argmin, carries the verdict |
| 5 | a closing verdict at all three scales | **no** | none of the three closes, and the caveat is restated anyway |

---

## Sensitivities — what moves a verdict and what does not

| axis | reading | worst z | reach-feasible | populated | closes | verdict moves? |
|---|---|---|---|---|---|---|
| **reference** | `C-0029`'s geometry, both puckers swept, free phosphorus | 4.55 | 4/4 | 0/4 | **no** | — |
| sugar pucker | C2′-endo only — `C-0042`'s and `C-0052`'s | 38.90 | 2/4 | 2/4 | no | **no** |
| sugar pucker | C3′-endo only — `C-0029`'s 0.600 nm links' | 6.33 | 4/4 | 0/4 | no | **no** |
| phosphate radius | 1.00 nm (`C-0029`'s, Hedley et al.) | 4.55 | 2/2 | 0/2 | no | **no** |
| **phosphate radius** | **0.90 nm (`C-0029`'s bracket)** | **2.99** | 2/2 | 2/2 | **yes** | **YES** |
| phosphate radius | 0.8901 nm (measured here) | 21.64 | 1/2 | 0/2 | no | **no** |
| groove convention | 120° (nominal) | 4.55 | 2/2 | 0/2 | no | **no** |
| **groove convention** | **154° (wide)** | **1.59** | 2/2 | 2/2 | **yes** | **YES** |
| groove convention | 180° (the hard chord) | 13.96 | 1/2 | 0/2 | no | **no** |
| reading | PINNED — the closure searches' own criterion, literally | 103.63 | 4/4 | 0/4 | no | **no** |

> **The two axes that move the verdict move it the same way and for the same reason, and it is not a physical sensitivity.** Changing the phosphate radius or the groove angle changes *which placement the distance search returns*, and neighbouring distance-optima differ by **7×** in strain (2.99 against 21.64 across a 0.01 nm change in radius). That is not a statement about DNA; it is a statement about an objective that is blind to the quantity being measured — and it is the same fact the census reports. **The verdict that survives is the census's, not the argmin's**, and it is why this claim's headline is written on the census.

---

## Does `C-0029`'s verdict survive?

**Its counting theorem and its existence result do, in full. Its routing does not, and neither do `C-0042`'s and `C-0052`'s.**

| `C-0029` said | this claim finds |
|---|---|
| *"the closure search tests a NECESSARY condition and never a sufficient one … no backbone torsion angle is checked"* | **discharged.** The torsions are now checked, and the necessary condition was not sufficient |
| *"a routing exists, and it is a scaffold excursion at 0.600 nm on both links"* | **the existence survives; the routing does not.** Those two links need ε = −22.9° (0.015 % of linkages) and β = 27.4° (**0 of 15 457** residues) |
| *"a 'closes' verdict is an UPPER bound on buildability"* | **upheld, and now quantified: the upper bound is loose by a factor of ~2 on the reach bound alone**, and by ~5 more on population |
| the counting theorem — a duplex end has two termini, lever arm ≤ 1.0 nm | **untouched.** It is a count, and nothing here can move it |
| `C-0042`: *"the aligned pair sits at the C2′-endo end of that window … where the torsion check is least comfortable"* | **right, and worse than uncomfortable: the 0.6969 nm link is excluded by a closed-form bound** |
| `C-0052`: it inherits the ceiling at three junctions | **confirmed: 2 of 6 links close**, and one leg is reach-excluded |
| `C-0037`/`C-0048`'s truss and cap, which stand on these junctions | **their mechanics are untouched — this claim tests chemistry, not statics — but the specific placements their legs and cap would use are in the infeasible set.** What they need is a re-search on the torsion criterion, and the census says the search will not come up empty |

---

## Validity range

- **TRL 1–3. Nothing here is measured about the junction**, and a torsion check is a **necessary** condition only. It does not establish that a junction assembles, folds, hybridises correctly, or survives 2 mM Mg²⁺; it establishes only that the backbone is not excluded by its own covalent geometry and by the conformations DNA is *observed* to adopt. **Exactly the caveat `C-0029` attached to the distances, one level down.**
- **Every residue is a RIGID body.** Real sugars pucker continuously, real bond angles bend, and a real junction will relieve some of this strain by deforming the duplexes it joins. So a *"does not close"* verdict here is an **upper bound on the strain**, not a proof of impossibility — except where the **reach bound** excludes a link, which is closed form under three-σ tolerances on every bond and angle and is as near a proof as this method reaches.
- **The two templates are two real nucleotides**, the medoids of their populations (`8FB4 C/11 DA` at C2′-endo, `5XK1 C/5 DC` at C3′-endo). They are representative, not universal: sequence, stacking and crystal environment all move a backbone, and a template is one draw from that distribution. Both puckers are swept per residue everywhere.
- **The C3′-endo template is anchored in an A-form helical frame**, because C3′-endo residues in well-formed B-helices are rare. It is used as a per-residue *option*, never as a design geometry, and the verdict is unchanged when it is excluded.
- **The PINNED reading is reported and not used.** Its baseline is 7.09–8.50 σ on a reconstructed duplex, so it cannot discriminate. Every verdict is on FREE.
- **The census solves only the 100 best-ranked placements of the 1 855 that pass the reach bound**, ranked by reach margin. So *"18 close"* is a **lower** bound on how many placements close, and *"none of the reported optima closes"* is exact because those were solved directly.
- **The census covers `C-0029`'s single-junction space only.** Each leg of `C-0042`'s pair is a single junction on the same seat duplex, so the census bounds the pair from above only in the sense that a pair needs two simultaneously feasible placements at a fixed separation and shared seat — which is strictly harder. `C-0052`'s trio sits on a **lone crossbar**, a different geometry, and is not covered at all.
- **Alignment is not checked against feasibility.** The census reports the chord azimuths of its feasible placements (159.0° and −51.0°) but does **not** search for a placement that is simultaneously torsion-feasible and aligned on the flexure axis, which is what `C-0042` and `C-0048` need. **That is the largest open item.**
- **The populated-region floor is 0.1 % per ten-degree bin**, 28× more permissive than uniform. Raising it can only make more links fail; lowering it to zero would still fail `C-0029`'s β = 27.4°, which is observed **never**.
- **The occupancy test is MARGINAL, not joint.** A septet all of whose torsions are individually populated may still be a combination never observed — the literature names exactly this case at four-way junctions (*"a rare combination g+/g+/g+/t, which has not observed among stable conformers even in the more variable RNA"*). So the test is **permissive** in the direction that matters, and a *"closes"* verdict here is weaker than a *"does not close"* one.
- **The solve returns the best-POPULATED closure under the strain ceiling, not the least-strained one.** Both are reported (`worstCovalentZ` and `minimumStrainZ`) and they differ by up to 2.5 σ. Ranking by strain alone leaves the torsions undetermined wherever the residual has a mirror branch, which is not a physical statement.
- **The measured survey is X-ray, DNA-only, ≤ 2.3 Å.** It is dominated by short oligonucleotide crystals; protein-bound and solution DNA are not represented, and crystal packing bends DNA.
- **The stylised duplex is `C-0029`'s**, phosphate radius 1.00 nm and 10.67 bp/turn. The measurement here puts the radius at **0.8901 nm**, the narrow end of `C-0029`'s own bracket, and the verdict moves across that bracket — see the sensitivity table and `CH-0070`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| phosphodiester restraint targets | 1.607(12) / 1.593(10) Å, 119.7(12) / 104.0(19) / 120.9(16)° | **CITED, READ DIRECTLY** — Parkinson, Vojtechovsky, Clowney, Brünger & Berman (1996), read from Kowiel, Brzezinski & Jaskolski, *NAR* **44**:8479 (2016) Table 3, which reproduces it verbatim; the IUCr original returns HTTP 403. **Used only as a cross-check** — the check runs on this task's own measurement |
| BI/BII backbone torsions | BI α 299.0, β 179.3, γ 48.4, δ 132.8, ε 181.7, ζ 263.2, χ 250.3 (0–360) | **CITED, READ DIRECTLY** — Svozil, Kalina, Omelka & Schneider, *NAR* **36**:3690 (2008) Table 3, 418 dinucleotides of 118 naked B-DNA structures. **Its ± are 95 % confidence intervals on the MEAN, not spreads** — the paper says so, and back-converting gives population SDs an order of magnitude larger. Used only as a cross-check |
| the four-way junction's backbone | *"a sharp turn … captured mainly by a change in the ε, ζ, α + 1, β + 1, and γ + 1 torsions, which adopt unusual values … the scarcity of structural data did not allow to classify the junction-site step as a distinct conformation"* | **CITED, READ DIRECTLY** — Svozil et al. (2008) |
| α/γ population and the unpopulated combinations | γ gauche+ 89.7 %, trans 6.3 %, gauche− 4.0 %; *"t/g–, t/g+ and g–/g– … are located in high energy zones"*; *"The lowest energy barrier is >7 kcal/mol"* | **CITED, READ DIRECTLY** — Várnai, Djuranovic, Lavery & Hartmann, *NAR* **30**:5398 (2002) Table 2 |
| pucker ranges and their δ | C3′-endo `0 ≤ P ≤ 36°`, C2′-endo `144 ≤ P ≤ 190°`; δ 82.1° north, 132.8/143.0° south | **CITED, READ DIRECTLY** — Kowiel, Brzezinski, Gilski & Jaskolski, *NAR* **48**:962 (2020); Svozil et al. (2008) |
| the `[0.60, 0.70]` nm phosphodiester window | 0.6 / 0.7 nm | **CITED, READ DIRECTLY, and its own primary source NOT FOUND** — Bosco, Camunas-Soler & Ritort, *NAR* **42**:2064 (2014); its references are two textbooks, neither reachable. **Measured here instead: 0.607 / 0.664 nm** |
| phosphate radius | 1.00 nm adopted, 0.90 nm bracket | **CITED** via `C-0029` (Hedley et al., *Phys. Rev. X* **14**:031042). **Measured here: 0.8901 nm** |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 10.67 | **CITED** via `C-0009`/`C-0029` |
| `C-0029`/`C-0042`/`C-0052` binding links | 0.600, 0.6969, 0.679 nm | **CITED**, and reproduced here as gate-5 tests to ≤ 5.7e−4 |

Everything else — the reach bounds, the covalent restraint measurement, the torsion distribution and its histograms, the two nucleotide templates and their successors, the inverse-kinematic closure, every torsion, every strain, the census and every count in it — is **derived here in code**, with `C-0029`'s, `C-0042`'s and `C-0052`'s searches **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether a placement can be simultaneously torsion-feasible and correctly ALIGNED.** The census finds 18 feasible placements and reports their chords at 159.0° and −51.0°; `C-0042` needs 90.0° on the flexure axis and `C-0048` needs a stated pair. **This is the largest open item and it is the direct successor to this task.** **ANSWERED by [`C-0059`](C-0059-torsion-feasible-routing.md) (`T-124`), with a split verdict**: yes at one junction (chord **90.0°**, `cos²ψ = 1.0000`, 7 of the 120 best-aligned closing), yes but expensively at two (6°–69° depending on the separation, and `C-0042`'s own 7 bp is the worst), and **not found** at three — 0 closing trios in the 24 best-aligned of 750 reach-feasible crossbar lattices. The census re-derives here at **departure 0**.
2. **Whether `C-0042`'s pair and `C-0052`'s trio have torsion-feasible placements at all**, under their own additional constraints (shared seat, fixed separation, six distinct targets, a lone crossbar). The census does not cover them.
3. **An oxDNA or all-atom relaxation of a census-feasible placement**, which is now the right spend and was not before: there is a candidate to relax.
4. **The joint occupancy test.** The adopted test is marginal; the literature names a junction conformer whose torsions are individually ordinary and jointly unobserved.
5. **`C-0029`'s search objective.** It should minimise a torsion-feasibility measure, or at least tie-break on window *centring*; minimising the window residual drives it to the edges, which is where the backbone is not.

## Challenges

**Raises [`CH-0070`](../challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md)** against `C-0029`'s reported routing as inherited by `C-0042` and `C-0052`. **No number in any of the three fails to reproduce** — 0.600 to 7.9e−5, 0.6969 to 3.4e−5, 0.679 to 5.7e−4.

**None stands against this claim.** The four ways it would fail:

1. **A relaxed atomistic or coarse-grained model showing that a junction relieves the strain by deforming its duplexes.** That would soften every *"does not close"* verdict except the reach-bound exclusions — and those four links would need bond stretching of 0.02–0.11 nm, which no force field permits.
2. **A crystallographic or NMR structure of a sharp backbone turn carrying β near 27° or ε near −23°.** One structure closes it. The survey here is X-ray, DNA-only, ≤ 2.3 Å and 876 entries; a wider survey could populate a bin this one leaves empty.
3. **A demonstration that the phosphate radius is 1.00 nm rather than 0.89.** The verdict at the single junction moves across `C-0029`'s own bracket, and the census is the only reading that survives it.
4. **A torsion-aware re-search that finds an aligned, feasible placement.** That would not overturn anything here — it is the open item this claim names — but it would change what the result *means* for `C-0037`, `C-0042`, `C-0048` and `C-0052`.
