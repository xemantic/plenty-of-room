# C-0062 — A torsion-feasible trio DOES exist on `C-0048`'s cap crossbar, at every one of the twenty-one admissible `(crossbar, row)` configurations, and `C-0059`'s *"not found"* was a property of its budget and not of the geometry — its own marginal statistics say those 24 lattices were worth **0.28** trios, so the null result was the expected one; deepened to 49 857 lattices and 149 789 junction solves the crossbar carries **609** closing trios, the truss branch stays open, and the row pitch that the base wants and the row pitch that the cap wants turn out to be the same number for the first time

| | |
|---|---|
| **Task** | [`T-127`](../tasks/T-127.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (an existence sweep over `C-0052`'s three junctions on one lone crossbar, with `C-0057`'s inverse-kinematic closure re-run as a library at every reach-feasible azimuth of every lattice) **+ logical** (two cheap bounds, one of which is an exact pruning of the solver and the other a marginal-statistics prediction with a declared falsifier) |
| **Verdict** | **PASS on the acceptance question, and the answer reverses the branch's outlook.** **A torsion-feasible trio exists.** It exists at **every one of the 21 admissible `(crossbar length, row pitch)` configurations**, at **4–13** closing lattices per 750–904 reach-feasible ones, and at **151 of 11 834** and **167 of 11 874** in the two depth runs. **`C-0059`'s negative reproduces exactly and carried no information**: restricted to its own budget this task also finds nothing, and the *marginal closure census* — the cheap bound, run before the sweep — says why. The three junctions close on **21.1 %, 20.0 % and 27.8 %** of lattices individually, so under conditional independence `C-0059`'s **24** solved lattices were worth **0.28** trios and its 0-of-24 was the expected outcome; one full 1 800-lattice grid is worth **21.1**, and the sweep returns **6** there — the conjunction is **1.5× harder** than independence, not impossible. **The budget is 1 039× `C-0059`'s in lattices solved and 557× in junction solves** (49 857 against 48; 149 789 against 269; 299 578 link closures against 538), and it is affordable only because `C-0057`'s reach bound is a **per-assignment** proof of exclusion: 12 % of the 32 assignments survive it, a **17.2×** speedup measured against `bestLinkClosure`, with **0 disagreements** in the gate test. **39 of the 44 published trios still close on `C-0057`'s own 180-step verdict grid.** The best chord alignment found is **9.0°** at a leg's cap and **21.0°** taken over all three junctions, against the **6.0°** `C-0059` assumed from a stage that had failed — worse, and by less than the design cares about. **And the design table, recomputed at ONE row pitch throughout and at the alignment a closing trio actually delivers, does not move**: the best representable design is the **10 bp row at both ends**, base 6.0° and cap 27.0° on a 17 bp crossbar, at a 4.08 nm leg, carrying **2.45 / 1.84** against `C-0059`'s own best 2.45 / 1.84 — **a 4.5× worse cap floor for a third-decimal change in the margin**, which is `C-0059`'s insensitivity finding tested rather than assumed. `C-0042`'s **7 bp** and `C-0037`'s **8 bp** rows remain **not representable at all** (base misalignments 69.0° and 57.0°, past the half right angle at which `C-0037`'s two-axis base exchanges its axes) ([`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md)). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED about the junction.** A torsion check is a **necessary** condition and never a sufficient one, exactly as `C-0029`, `C-0052`, `C-0057` and `C-0059` all say — so a *"closes"* verdict is an **upper bound on buildability**, and this claim's positive result is therefore weaker in kind than `C-0059`'s negative was. What it removes is a *"not found"*, not a doubt about assembly. |
| **Provenance** | `gpd/results/T-127-crossbar-trio-existence.json`, produced by `anchoring.CrossbarTrioExistenceStudyKt`; **4 cheap-bound quantities, 3 marginal-census records, 21 configuration records, 44 trio records, 107 design records, 5 sensitivities, 9 convergence records, 8 upstream reproductions, 5 budget records**; **19 gate-named tests in `CrossbarTrioExistenceTest`**; `tools/verify.sh` **BUILD SUCCESSFUL on the whole suite in 11 m 14 s with no `--drop-file` needed**, twice. The sweep is deterministic by construction — fixed grids, strict comparisons, no tolerance in any control flow, and every parallel unit a pure function recombined in lattice-index order, so the outcome does not depend on the thread count — and rounded at the **serialisation boundary**; a byte-for-byte re-emission was **not** performed and is not claimed |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 0.34 nm rise, 10.67 bp/turn; the cap a **lone** crossbar duplex along `x̂`; phosphate radius **1.00 nm**, minor groove **120°** — `C-0029`'s geometry, with 0.90 nm, 154° and 180° carried as sensitivities; the phosphodiester window the inherited **`[0.60, 0.70]` nm**, unchanged, so that the feasible set **is** `C-0057`'s and `C-0059`'s; search grid **60** torsion steps / **4** refinements (`C-0059`'s own), verdict grid **180** / **6** (`C-0057`'s own); `EI = 230 pN·nm²` (CanDo) with every critical load also on Fields et al.'s implied **172.906**; angles in **degrees** throughout |
| **Consumes** | [`C-0059`](C-0059-torsion-feasible-routing.md) (`TorsionFeasibleTrioSearch`, `junctionLinks`, `reachVerdict`, `feasibleTrussDesign`, `legBudgetDegrees` — **re-run as a library**, and its trio numbers **re-derived** and its pair floors **consumed from its result file**), [`C-0057`](C-0057-backbone-torsion-closure.md) (`linkReach`, `closePhosphodiester`, `PlacedResidue`, `DuplexSite`, `NucleotideTemplate`, `PhosphodiesterGeometry`, `BDnaTorsionOccupancy`), [`C-0052`](C-0052-crossbar-junction-trio.md) (`CrossbarGeometry`, `TrioJunctionSpec`, `TrioPlacement`, `CrossbarTrioClosure`, `boundedSeatContactLength`, `loneSeatFaceHeight`, `chordPairMisalignment`), [`C-0048`](C-0048-truss-cap.md) (`capBendingStiffness`, `capTorsionalStiffness`, `capDesign`), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`foldedChordMisalignment`, `couplePhaseProjection`, the 1.60 nm contact floor, the 6 bp steric floor), [`C-0037`](C-0037-triangulated-standoff.md) (`TwoLinkBase`, `TrussLayout`), [`C-0029`](C-0029-perpendicular-junction-routing.md) (`DuplexBackbone`, `linkWindowResidual`, the counting theorem), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md) against `C-0059`'s design table — its cap floor is read off a stage that returned no closure, and its row pitch is 7 bp at the cap and 9 bp at the base |

---

## The claim, in one line

**`C-0059` asked whether three 90° junctions can close together on one crossbar, solved 24 lattices at two azimuths each, found none and said honestly that it was a *"not found within the budget"* — and the missing half of that sentence is that the budget was worth a quarter of a trio: measure how often each junction closes on its own, multiply, and 24 lattices predict 0.28 hits, which is exactly what was observed; deepen the same search to 49 857 lattices with every reach-feasible azimuth solved and the trio appears at every crossbar length and every row pitch in the admissible band, 609 times, so what decides the truss branch is not chemistry but how much of a six-dimensional continuum a search was allowed to look at.**

---

## The two cheap bounds, which ran first — and both of them did work

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the per-assignment reach pruning.** `C-0057`'s `O3′···C5′` interval is a proof of exclusion for **one** assignment of donor end, strand polarity and sugar pucker; `bestLinkReach` throws that granularity away by reporting the best of 32. A closure whose residuals are all inside the 3 σ ceiling has its `O3′···C5′` inside the interval, so an assignment that fails the bound **cannot close and need not be solved** | **12 %** of assignments survive; measured speedup **17.2×** (46.8 s → 2.7 s for 60 junction solves), **0 disagreements** against `bestLinkClosure(…).closes` on 20 links | **the bound that made the budget affordable.** It is the cheap-bound-before-the-expensive-search rule applied to the *solver*, and it is exact rather than heuristic — which is why it is a gate-2 test and not a tuning parameter |
| **2** | **the marginal closure census.** Three junctions share a lattice and nothing else, so each junction's placement problem is conditionally independent of the others given the lattice; measure `q_j` = the fraction of lattices at which junction `j` closes **somewhere**, and `N·Πq_j` is the yield a budget of `N` lattices should return | `q` = **21.1 %, 20.0 %, 27.8 %**; `N·Πq` = **0.28** trios at `C-0059`'s 24 lattices and **21.1** at one 1 800-lattice grid | **it is the answer to the question the task was set.** `C-0059`'s 0-of-24 is what a budget worth 0.28 trios returns. It is also a genuine **bound in the negative direction**: any `q_j = 0` would have killed the trio outright with no joint search at all |

> **The falsifier declared in the Plan section did not fire, and its non-firing is the finding.** Falsifier 3 was *"the deepened sweep finding 0 closing trios where the marginal product predicts many — that would mean the junctions are not independent given the lattice, and that correlation would be the finding."* The sweep returns **6** closing lattices at the design point against the independence prediction of **21.1 per 1 800**, i.e. the conjunction is **1.5× harder** than independence — a mild negative correlation, entirely accounted for by the six-distinct-targets and termini-clearance conditions the marginal census does not contain, and nowhere near enough to produce a null result. Falsifier 2 (the pruning being unsound) did not fire either: 0 disagreements.

---

## The free limiting case — `C-0059`'s budget, reproduced

| | `C-0059` published | re-run from its own class | this task's search, capped back to that budget |
|---|---|---|---|
| lattices enumerated, 13 bp | 1 800 | **1 800** | **1 800** |
| reach-feasible for all three junctions | **750** | **750** | **750** |
| lattices solved | 24 | **24** | **24** |
| junction solves | **134** | **134** | 50 |
| **closing trios** | **0** | **0** | **0** |
| best reach-feasible alignment | 6.0° | **6.0°** (departure `1.7e−14`) | — |

**Departure 0 on every count.** The 50 against 134 is not a discrepancy: this task solves the junction with the *fewest* candidates first and abandons a lattice the moment one junction closes nowhere, which is cheaper and returns the same verdict. **The negative reproduces; what changes is what it is worth.**

---

## The deepened sweep — and the trio exists everywhere in the band

Every lattice of a `90 phase × 4 axial × 5 lateral` grid, at every `(crossbar, row)` configuration `C-0042`'s 6–12 bp row band admits, with **every** covalent, reach-feasible azimuth solved at every junction rather than the best-aligned two.

| row [bp] | 6 | 6 | 6 | **7** | 7 | 7 | 8 | 8 | 8 | **9** | 9 | **9** | 10 | 10 | 10 | 11 | 11 | 11 | 12 | 12 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| crossbar [bp] | **12** | 13 | 14 | **13** | 14 | 15 | 14 | 15 | 16 | 15 | 16 | **17** | 16 | 17 | 18 | 17 | 18 | 19 | 18 | 19 | 20 |
| reach-feasible lattices | 767 | 824 | 847 | 750 | 833 | 882 | 768 | 816 | 851 | 781 | 865 | 886 | 769 | 793 | 839 | 821 | 873 | 904 | 785 | 852 | 882 |
| **closing trios** | **13** | 10 | 9 | **6** | **13** | **13** | 6 | 4 | 7 | 6 | 8 | **12** | 10 | 11 | 10 | 10 | 7 | 10 | 11 | 9 | 11 |
| best worst chord [°] | **21** | 48 | 48 | 57 | **21** | **21** | 24 | 24 | 78 | 24 | 66 | **27** | 30 | 27 | 27 | 27 | 36 | 24 | 51 | 54 | 57 |
| best **leg** chord [°] | **21** | 36 | 36 | 57 | **21** | **21** | 24 | 24 | 78 | 24 | 66 | **27** | 30 | 27 | 27 | 27 | 36 | 24 | 27 | 27 | 36 |
| best flexure chord [°] | 9 | 51 | 54 | 6 | 6 | 6 | 3 | 3 | 9 | 24 | 27 | **0** | 12 | 18 | 3 | 24 | 0 | 18 | 54 | 54 | 63 |

Five things fall out.

1. **The trio closes at every configuration in the band.** Not one of the 21 comes up empty, and the closing rate is remarkably flat: **0.5–1.7 %** of reach-feasible lattices, with no systematic dependence on the crossbar's length or on the row.
2. **`C-0052`'s own 13 bp / 7 bp design point is the *worst* of the three crossbar lengths at its own row** — 6 closing lattices at a 57.0° best chord, against 13 at 21.0° for 14 and 15 bp. **One base pair of overhang is worth 2.2× in closures and 36° of alignment**, and it costs the design nothing: `C-0048`'s cap terms are `12EI/w` and `4C/w`, which carry the row and not the crossbar's length.
3. **The alignment a closing trio delivers is 21.0° at best over the band and 9.0° in the depth run — not the 6.0° `C-0059` composed its design table at.** That number was the best *reach-feasible* alignment of a stage that returned nothing; the best alignment a stage that *closes* delivers is 1.5–3.5× worse ([`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md)).
4. **The flexure's own chord is the easy one.** It reaches **0.0°** at two configurations and ≤ 9° at eleven of the twenty-one, because the flexure junction is — exactly, by a rigid rotation about the crossbar's own axis — a leg junction at a phase shifted by 90° and a mirrored azimuth, so it samples the same feasible set from a different corner and is not competing with the legs for a chord direction.
5. **The combination stage almost never binds.** Across the band, 0–8 lattices per configuration had all three junctions closing but no *collected* combination with six distinct targets; and the number is **identical at 2, 4 and 8 collected closers per junction**, so the one cap that could manufacture a negative does not.

### The depth runs

| | lattices enumerated | reach-feasible | **closing** | best worst chord | best **leg** chord |
|---|---|---|---|---|---|
| `C-0052`'s design point, 13 bp / 7 bp | **25 920** (360 × 8 × 9) | 11 834 | **151** | **21.0°** | 21.0° |
| the band's best, 12 bp / 6 bp | **25 920** | 11 874 | **167** | 30.0° | **9.0°** |

**The count of closing lattices is a density on a continuum and it is not grid-converged — deliberately.** Refining the helical phase 45 → 90 → 180 steps takes the closures 2 → 6 → 18, and the azimuth 60 → 120 → 240 takes them 0 → 2 → 9. That is exactly what a *measure* does under refinement, and it has one consequence that matters and one that does not: **the existence verdict is monotone under refinement and therefore safe**, and **every alignment reported here is an upper bound**, because a finer grid can only find a better-aligned member of the same set.

> **And it is why `C-0059`'s search grid could return zero.** At 60 azimuth steps and 45 phase steps this task also finds **0 closing lattices** at the design point. The trio's feasible set is a thin sheet in a six-dimensional continuum, and a coarse enough sampling of a thin set is empty.

---

## The verdict grid

Every published trio is re-solved junction by junction on `C-0057`'s own **180-step / 6-refinement** grid, against the **60 / 4** the search runs on.

**39 of the 44 close on both.** The five that do not are not a defect and they are not dropped: the refinement is a **local zoom**, so neither grid is exhaustive and neither dominates the other — a closure found at one can be missed at the other, in either direction. The existence result is quoted on the 39 that survive both, which is what makes it a result rather than an artefact of one grid.

---

## The mechanics, at the alignment a CLOSING trio delivers and at ONE row pitch

`C-0048`'s pipeline and `C-0037`'s nine predicates re-run at the misalignments the found trios deliver, with `C-0052`'s leg-is-one-body budget `chordPairMisalignment(m)` imposed on the sum of the base and cap misalignments, over the whole 12–26 step leg envelope, on **both** rigidities.

**The row pitch is one number.** It is the legs' separation, and a leg has one of those: the pair floor at the base and the trio's leg chord at the cap must be read at the *same* pitch. `C-0059`'s table reads them at 9 and 7 — [`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md).

| row [bp] | crossbar | base floor (`C-0059`'s pair, consumed) | **cap floor (this task's closing trio)** | flexure | representable? | margin CanDo | margin Fields |
|---|---|---|---|---|---|---|---|
| 6 | 12 bp | 33.0° | **9.0°** | 30.0° | yes | 1.74–2.33 | 1.31–1.75 |
| **7** | 13 bp | **69.0°** | 21.0° | 6.0° | **NO — past the half right angle** | — | — |
| **8** | 14 bp | **57.0°** | 24.0° | 3.0° | **NO** | — | — |
| **9** | 15 bp | **6.0°** | 24.0° | 24.0° | **yes** | 1.84–2.41 | 1.38–1.81 |
| **10** | **17 bp** | **6.0°** | **27.0°** | 18.0° | **yes** | **1.85–2.45** | **1.39–1.84** |
| 11 | 19 bp | 33.0° | 24.0° | 18.0° | yes | 1.76–2.35 | 1.32–1.77 |
| 12 | 18 bp | 33.0° | 27.0° | 54.0° | yes | 1.63–2.17 | 1.23–1.63 |

Three things fall out, and the first is the one that decides how much this whole task moves the design.

1. **The mechanics does not move at all, and that is a *confirmation* of `C-0059` rather than a null result.** The best representable design is the **10 bp row at both ends** — base 6.0°, cap 27.0°, on a 17 bp crossbar — at **12 steps (4.08 nm of leg)**, carrying **2.45** on CanDo's rigidity and **1.84** on Fields et al.'s. `C-0059`'s own best was **2.45 / 1.84**, and its mixed-row composition re-evaluated here gives **2.446 / 1.839**. **The cap floor moved from an assumed 6.0° to a measured 27.0° — 4.5× — and the margin moved in the third decimal**, because `C-0052`'s leg-is-one-body budget runs 0.3°–89.8° over the envelope and swallows a 27° floor almost everywhere. `C-0059` said the binding misalignment is the leg's own quantised twist and not the chemistry; this is that statement tested against a chemistry floor 4.5× larger, and it holds.
2. **Two rows are not design points at all.** At 7 and 8 base pairs the *base* misalignment the torsion-feasible set delivers is 69.0° and 57.0°, past the 45° at which `C-0037`'s `TwoLinkBase` invariant exchanges its restrained and free axes; the pipeline refuses them rather than reporting a number. That is `C-0059`'s finding, carried to the row `C-0042` recommended and to `C-0037`'s original 8 bp, and nothing here softens it.
3. **The two readings agree at 9–10 base pairs.** `C-0059` moved the recommended row there because that is where the *pair* on the sheet reaches 6.0°; this task finds independently that it is also where the whole design carries its best margin once the *crossbar's* own cap floor is imposed at the same pitch. **Two different searches, two different bodies, one row pitch — and 10 bp, not 9, is the best of the two.**

---

## Sensitivities — what moves the verdict and what does not

| axis | reading | reach-feasible lattices | **closing** | best worst chord | verdict moves? |
|---|---|---|---|---|---|
| **reference** | `C-0029`'s geometry, 120° groove, `r_P` = 1.00 nm, 13 bp / 7 bp | 750 | **6** | 57.0° | — |
| groove convention | **154° (wide)** | 935 | **18** | 38.0° | **no** |
| groove convention | **180° (the hard chord the mechanics is written on)** | 406 | **2** | 57.0° | **no**, and it is the hardest reading — 3× fewer feasible lattices and 3× fewer closures |
| phosphate radius | **0.90 nm** (`C-0029`'s own bracket end) | 820 | **14** | **15.0°** | **no**, and it is the *most* favourable reading |
| seat contact floor | `C-0042`'s 1.60 nm raised to **1.90 nm** | 584 | **6** | 57.0° | **no** — every closing trio already keeps 1.833–2.000 nm of contact |

> **`C-0057`'s falsifier 4 does not fire here either.** Its single-junction verdict swung by 7× in strain across the 0.90–1.00 nm phosphate-radius bracket because a *distance argmin* is unstable under a convention. An **existence sweep over a whole grid** is not: the trio closes at both radii, at all three groove conventions and at both contact floors. The count moves by 3–9×; the verdict does not move at all.

---

## The budget, which is the number this claim lives on

| stage | configurations | lattices enumerated | **lattices solved** | candidate azimuths | **junction solves** | link closures | **closing trios** |
|---|---|---|---|---|---|---|---|
| **`C-0059`'s own trio search** | 2 | 3 600 | **48** | — | **269** | 538 | **0** |
| this task, the `(crossbar, row)` band | 21 | 37 800 | 17 388 | 334 557 | 49 591 | 99 182 | **196** |
| this task, the depth run at the design point | 1 | 25 920 | 11 834 | 234 043 | 35 451 | 70 902 | **151** |
| this task, the depth run at the best configuration | 1 | 25 920 | 11 874 | 234 119 | 36 010 | 72 020 | **167** |
| **this task, total** (including the reproduction, 5 sensitivities and 9 convergence runs) | **37** | **113 040** | **49 857** | **1 012 134** | **149 789** | **299 578** | **609** |

**1 039× the lattices solved and 557× the junction solves**, for a study that runs in about half an hour, because of cheap bound 1.

**What fraction of the reachable space is that?** The honest answer is that the space is a **continuum** and no finite budget covers a fraction of it. What can be stated is the resolution: the helical phase to **4°** (1° in the depth runs), the axial phase to **0.085 nm** (0.043 in the depth runs), the lateral seat to **0.2 nm** (0.1 nm), the standoff azimuths to **3°**, over **21 of the 21** admissible `(crossbar, row)` configurations at the three shortest crossbar lengths each. Within that discretisation the band sweep is **exhaustive**: every lattice, every reach-feasible azimuth, no ranking and no cap. `C-0059`'s was 24 lattices and 2 azimuths of the same grid.

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/anchoring/CrossbarTrioExistenceTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a search budget is a set of counts and adds componentwise, with a link closure exactly two per junction solve; a closing rate is a fraction in `[0, 1]` and the independence yield is `N·Πq`, **exactly zero** if any junction never closes; the admissible crossbar band starts at `C-0048`'s own `ceil`, `row + 6` at every row from 6 to 12; unphysical arguments throw at **six** entry points | **PASS** |
| **2 — limiting cases** | **the pruned verdict reproduces `bestLinkClosure(…).closes` on 20 links, 0 disagreements** — the assertion the whole budget rests on; a junction moved 50 nm away closes at no assignment and is reach-excluded with no solve; **restricting the sweep to `C-0059`'s budget reproduces its trio negative** at 1 800 lattices, 750 reach-feasible and 24 solved; `C-0059`'s own class still returns **750 / 24 / 134 / 0** and its 6.0°; an impossible contact floor returns *"NO lattice"* rather than a *"does not close"* that was never tested | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | a finer azimuth grid never loses a candidate placement nor a reach-feasible lattice; a finer lattice grid never loses a closing lattice; **the search and verdict grids are compared and their disagreement measured rather than assumed**, with at least one closure carried by both | **PASS** |
| **5 — literature and upstream** | `C-0052`'s crossbar geometry reproduced **from the row** (13 bp = 4.42 nm, rim clearance 0.02 nm, minimum 13 bp); `C-0029`'s window, rise, radius and 10.67 bp/turn asserted to be what the sweep actually uses; `C-0052`'s leg budget at 21, 24 and 16 steps. **Worst departure over 8 recorded reproductions: 1.4e−3**, which is `C-0052`'s own published rounding of 78.5286 to 78.53 | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The flexure junction IS a leg junction rotated by a quarter turn about the crossbar's own axis.** The two are built by different branches of `termini` — one in the `x–y` plane and one in `x–z` — yet a rotation carrying `(0,0,−1)` to `(0,−1,0)` maps a leg at azimuth `a` and phase `φ` onto a flexure at azimuth `−a − Δ` and phase `φ − 90°`, **swapping its two termini**, and the same two crossbar phosphates come back at the same two gaps. Asserted over 24 azimuths. It is a rigid-motion invariance nothing in the code imposes, and it is why the marginal census of one junction kind is informative about the other.
2. **The trio verdict does not depend on the order the junctions are listed** — asserted on a reversed list, on the closing count and on the reach-feasible count. The search *does* reorder them internally, cheapest first, so this is a real test of that optimisation and not of physics.
3. **A chord is a line**: a half turn of the standoff leaves the misalignment unchanged, at 30 azimuths, compared absolutely.
4. **The phosphate lattice this task sweeps is `C-0059`'s own crossbar anchor construction**, target for target, to 1e−12 — so the deepened search is deepening the *same* search and not a different one.

---

## Validity range

- **TRL 1–3. Nothing here is measured about the junction**, and a torsion check is a **necessary** condition only. It does not establish that a trio assembles, folds, hybridises correctly or survives 2 mM Mg²⁺. **A positive existence result is weaker in kind than a negative one**, and this claim's headline is a positive: it removes a *"not found"*, it does not add a demonstration.
- **`C-0057`'s whole validity range is inherited**: every residue is a rigid body, both nucleotide templates are single draws from a distribution, the occupancy test is **marginal** rather than joint — so a *"closes"* verdict is weaker than a *"does not close"* one, and this claim is built entirely of *"closes"* verdicts.
- **The `[0.60, 0.70]` nm window is the inherited one and `C-0057` measures 0.607 / 0.664 nm.** The window is **5 % wider at the top than the backbone is**, and it is carried unchanged so that the feasible set is `C-0057`'s and `C-0059`'s. The measured pair can only **shrink** the feasible set, so every closure here would have to be re-checked against it; that is named as an open item and not done.
- **Alignment is not a condition of existence here**, and every alignment reported is an **upper bound** — a finer grid can only improve it.
- **The closing *count* is not grid-converged and is not meant to be.** It is a density on a continuum: refining the phase 45 → 180 takes it 2 → 18. The *existence* verdict is monotone under refinement; the count is a sampling statistic and is quoted with its grid.
- **The lattice grid is finite in the lateral seat**: ±0.4 nm in the band sweep and ±0.4 nm in the depth runs, `C-0059`'s own range. A wider lateral excursion is not searched.
- **The two legs are the same length in base pairs** and the crossbar is an ideal duplex of the same rise, radius and twist as every other duplex here, with no end fraying — `C-0052`'s assumptions, unchanged.
- **The mechanics is `C-0048`'s and `C-0052`'s, unchanged**, including `C-0020`'s derived and unmeasured `k_s`, on which the whole base couple rests. `T-9` still owns it, and `C-0048` reports the margin falling to 0.93 / 0.70 at `k_s`/32.
- **The 5 of 44 trios that fail the verdict grid are reported and not used.** They are also not evidence against the 39 that pass.
- **The parallel sweep is deterministic** by construction and was not re-run to prove it; the tests assert order-independence of the *junction ordering*, not of the thread count.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the measured backbone — bonds, angles, torsion occupancies, both nucleotide templates | 0.16022 nm, 121.30°, … | **DERIVED**, in `C-0057`, from 13 084 crystallographic linkages; consumed here as a library |
| the `[0.60, 0.70]` nm phosphodiester window | 0.60 / 0.70 nm | **CITED** via `C-0029` (Bosco et al.), **and its own primary source NOT FOUND**; `C-0057` measures 0.607 / 0.664. Carried unchanged, deliberately |
| phosphate radius, groove angle | 1.00 nm, 120° | **CITED** via `C-0029` (Hedley et al.); both swept |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 10.67 | **CITED** via `C-0009`/`C-0029` |
| `EI` = 230 pN·nm², Fields et al.'s 172.906 | | **CITED** via `C-0009`/`C-0028` |
| `C-0059`'s pair misalignment floors, 6–12 bp | 33 / 69 / 57 / 6 / 6 / 33 / 33° | **CONSUMED AS DATA** from `gpd/results/T-124-torsion-feasible-routing.json`, per `T-127`'s own acceptance predicate, and the 7 bp value re-checked at departure 0 |
| `C-0052`'s and `C-0048`'s pipeline constants | 78.53°, 4.42 nm, 0.02 nm | **CITED**, and reproduced here as gate-5 tests to ≤ 1.4e−3 |

Everything else — the per-assignment pruning and its equivalence, the marginal census, the independence yield, the whole band sweep, both depth runs, every closing trio, the verdict-grid re-solve, the per-row design tables and every margin — is **derived here in code**, with `C-0029`'s, `C-0037`'s, `C-0042`'s, `C-0048`'s, `C-0052`'s, `C-0057`'s and `C-0059`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The measured window.** `C-0057` measures the phosphodiester step at 0.607 / 0.664 nm against the 0.60 / 0.70 in circulation. Every closure in this programme sits in the inherited window; re-running the sweep on the measured one can only shrink the feasible set, and nobody has done it.
2. **An atomistic or oxDNA relaxation of a specific closing trio.** There are now 609 of them, with phase, axial phase, lateral seat and three azimuths recorded for 44. That is the right next spend and it was not available before.
3. **The flexure's own far end**, whose two ends both want a vertical chord at a span the placement condition sets — `C-0052`'s open item, untouched here.
4. **The groove convention**, still the one axis that changes the *difficulty* most (3× on both the feasible set and the closures between 120° and 180°).
5. **The joint occupancy test.** `C-0057`'s is marginal; the literature names a junction conformer whose torsions are individually ordinary and jointly unobserved. Every *"closes"* verdict here inherits that permissiveness.

## Challenges

**Raises [`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md)** against `C-0059`'s design table. **No upstream number fails to reproduce** — 8 reproductions at ≤ 1.4e−3, six of them exact.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that the per-assignment reach pruning is unsound.** Every number here would then be wrong. It is asserted equal to `bestLinkClosure` link by link, and the argument is that the reach interval is the 3 σ envelope of the same chain the strain ceiling measures — but it is an argument about two pieces of code agreeing, and a wider sample could find a counterexample.
2. **A re-run on `C-0057`'s measured 0.607 / 0.664 nm window** emptying the feasible set. It can only shrink it; whether it shrinks it to nothing is untested.
3. **An atomistic relaxation showing a closing trio is strained beyond what a rigid-residue model can see.** The occupancy test is marginal, so a *"closes"* is the weak direction.
4. **A finer verdict grid disagreeing with the 180-step one.** Five of 44 trios already disagree between 60 and 180 steps; the claim rests on the 39 that do not, and a 540-step grid has not been run.
