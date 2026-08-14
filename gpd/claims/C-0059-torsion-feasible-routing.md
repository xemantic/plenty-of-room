# C-0059 — Re-optimised on `C-0057`'s feasible set the truss branch does NOT close: the single junction survives at a chord exactly on the loaded axis, the pair survives but its alignment stops being free and its recommended 7 bp row is the worst of the seven, and the trio — `C-0048`'s cap, the one junction the design cannot do without — does not close at all within the budget; and the mechanics that survives is nearly unchanged, because the binding misalignment was never the chemistry but the leg's own quantised twist

| | |
|---|---|
| **Task** | [`T-124`](../tasks/T-124.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a torsion-aware re-optimisation over `C-0057`'s own feasible set, with `C-0029`'s, `C-0037`'s, `C-0042`'s, `C-0048`'s and `C-0052`'s pipelines re-run as libraries at the alignments it delivers) **+ logical** (two closed-form bounds, one a proof of exclusion and one an interval on the design variable) |
| **Verdict** | **PASS on the acceptance question, and the answer is a split verdict that must be quoted whole. Two of the three junctions survive on the feasible set and the third does not.** `C-0057`'s census reproduces **exactly** — 3 546 covalent and 1 855 reach-feasible of 69 120 for independent staples, 280 and 137 for the scaffold excursion, departure **0**. **The single junction survives, and better than `C-0042` could have hoped:** of the **120 best-aligned** reach-feasible placements, **7 close at torsion level**, the best at a chord of **90.0°** — *exactly* the flexure axis, `cos²ψ = 1.0000` — at a binding link of **0.643 nm** in the **interior** of the measured window, on the seat duplex's own axis. **The pair survives at every separation from the 6 bp steric floor to 12 bp, and its alignment stops being free**: `C-0042`'s 0.00° at every separation becomes **33°/69°/57°/6°/6°/33°/33°**, so its own recommended **7 bp row is the worst of the seven** — 69.0°, **12.8 %** of the base couple, and *past the half right angle at which `C-0037`'s two-axis base cannot be represented at all* — while **9 and 10 bp deliver 6.0°**, inside `C-0029`'s ±16.87° allowance ([`CH-0072`](../challenges/CH-0072-the-alignment-is-not-free-on-the-torsion-feasible-set.md)). **The trio does not close.** On the lone 13 bp crossbar, 750 of 1 800 lattices are reach-feasible for all three junctions and **none of the 24 best-aligned closes** — 134 junction solves, 0 closing — and the same at 15 bp. **And the topology `C-0029` recommends is the wrong one**: the scaffold excursion is feasible but *cannot be aligned*, 1 of 120 closing at a **39.0°** chord against independent staples' 7 at 0.0°. **What does not move is the mechanics.** Carried through with the pair's own 6.0° base floor and the crossbar's 6.0° cap floor, all fifteen quantised leg lengths still pass all nine predicates at margins **1.81–2.45 (CanDo) and 1.36–1.84 (Fields et al.)**, against `C-0052`'s aligned **1.81–2.43 / 1.36–1.83** — because the binding misalignment is the leg's own quantised twist budget and not the chemistry. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED about the junction.** A torsion check is a **necessary** condition and never a sufficient one — as `C-0029` said of the distances and `C-0057` said of the torsions. And **every negative here is bounded by its solve cap**: a *"does not close"* is a *"not found within the budget"*, never a *"does not exist"*. |
| **Provenance** | `gpd/results/T-124-torsion-feasible-routing.json`, produced by `anchoring.TorsionFeasibleRoutingStudyKt`; **5 cheap-bound quantities, 2 census records, 166 azimuth records, 2 aligned-closure records, 7 pair records, 2 trio records, 17 design records, 6 sensitivities, 10 convergence records, 16 upstream reproductions**; **19 gate-named tests in `TorsionFeasibleRoutingTest`**; `tools/verify.sh` **BUILD SUCCESSFUL on the whole suite, with no `--drop-file` needed** (earlier runs in this iteration needed a concurrent agent's mid-TDD `structure/StackedArmSheetTest.kt` dropped). The study is deterministic by construction — fixed grids, strict comparisons, no tolerance in any control flow — and rounded at the **serialisation boundary**; a byte-for-byte re-emission was **not** performed in this iteration and is not claimed |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 0.34 nm rise, 10.67 bp/turn; phosphate radius **1.00 nm**, groove **120°** for the closure — `C-0029`'s own, with 0.90 / 0.8901 nm and 154° / 180° carried as sensitivities; the **mechanics** on the hard, convention-free **180°** chord, as `C-0037`, `C-0042`, `C-0048` and `C-0052` all adopt; `EI = 230 pN·nm²` (CanDo) with every critical load also on Fields et al.'s implied **172.906**; torsions **degrees**, IUPAC, folded to `(−180, 180]`; the closure solved on `C-0057`'s measured backbone — 13 084 crystallographic linkages, 15 457 residues, 876 entries |
| **Consumes** | [`C-0057`](C-0057-backbone-torsion-closure.md) (`PhosphodiesterGeometry`, `bestLinkReach`, `bestLinkClosure`, `NucleotideTemplate`, `BDnaTorsionOccupancy`, `ResidueAnchor` — **re-run as a library**, and its census **re-derived**), [`C-0029`](C-0029-perpendicular-junction-routing.md) (`DuplexBackbone`, `sheetPhosphate`, `standoffTerminus`, `seatFaceHeight`, `linkWindowResidual`, `bestTwoLinkClosure`, `RoutingTopology`, `couplePhaseProjection`, `realisablePerpendicularBase`, the counting theorem), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`chordBaseAxes`, `foldedChordMisalignment`, `StandoffPlacement`), [`C-0052`](C-0052-crossbar-junction-trio.md) (`CrossbarGeometry`, `TrioJunctionSpec`, `TrioPlacement`, `CrossbarTrioClosure`, `loneSeatFaceHeight`, `boundedSeatContactLength`, `capDesign`, `chordPairMisalignment`, `legAzimuthSplit`), [`C-0048`](C-0048-truss-cap.md) (`SolvedTrussCap`, `capBendingStiffness`, `capTorsionalStiffness`), [`C-0037`](C-0037-triangulated-standoff.md) (`TrussLayout`, `TwoLinkBase`, `trussFrameCouple`, `TriangulatedStandoff`), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0072`](../challenges/CH-0072-the-alignment-is-not-free-on-the-torsion-feasible-set.md) against `C-0042`'s *"the azimuth costs exactly nothing"* and its 7 bp row, and against `C-0029`'s recommended scaffold excursion |
| **Challenged by** | [`CH-0075`](../challenges/CH-0075-the-cap-floor-is-read-off-a-stage-that-failed.md), from [`C-0062`](C-0062-crossbar-trio-existence.md) (`T-127`), on the **design table's cap column and its row label** — the 6.0° cap floor is the best *reach-feasible* alignment of a stage that returned no closure, and the table reads the row pitch at **7 bp** for the cap and **9 bp** for the base, which is one length. Recomputed at one pitch throughout and at the alignment a *closing* trio delivers (**27.0°**, 4.5× worse), the best representable design is **2.45 / 1.84** against this claim's own best of **2.45 / 1.84** — so the verdict stands and the **insensitivity finding is strengthened**, having now been tested against a floor 4.5× larger rather than assumed against an optimistic one. **And the trio verdict itself is superseded**: `C-0062` finds a closing trio at every one of the 21 admissible `(crossbar, row)` configurations, and shows that this claim's 24-lattice budget was worth **0.28** trios under the marginal closure rates, so its null result was the expected one. Restricted to this budget, `C-0062` also finds nothing |

---

## The claim, in one line

**`C-0057` proved the three reported routings infeasible and the search space non-empty, and left one question: whether a placement can be simultaneously torsion-feasible and correctly aligned — the answer is yes at one junction, exactly, and it degrades monotonically with the number of junctions that must share a seat, until at three it fails: one junction reaches `cos²ψ = 1.0000`, two reach 0.989 but only at 9–10 base pairs and 0.128 at the 7 the design was written on, and three do not close at all in the 24 best-aligned of 750 reach-feasible crossbar lattices — so the truss branch is not killed by chemistry and it is not delivered by it either, and what the design must now do is find its cap.**

---

## The two cheap bounds, which ran first — and the one written to bind did not

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **`C-0057`'s free-phosphate reach**, `O3′···C5′ ∈ [0.2228, 0.4095]` nm at three measured σ | closed form, three atom placements | it is a **proof** of exclusion and it halves the distance-feasible set (1 855 of 3 546). Inherited and **re-derived**: the counts reproduce to **0** |
| **2** | **the alignment band as an azimuth interval.** `C-0029`'s ±16.87° allowance (`cos² = 0.9158`) is `ψ₀ ∈ 120° ± 16.87° (mod 180°)`, i.e. **18.74 %** of the circle | **414** of 1 855 reach-feasible placements are in it, spread over **118 of the 120** azimuth values, best attainable **0.0°** | **the falsifier declared in the Plan section fired.** The reach-feasible set is azimuthally **dense**, so the bound excludes nothing and the answer has to come from the expensive solve. Declared in advance, and it is why the search ranks by misalignment rather than filtering on it |

> **Bound 2's failure is itself the shape of the result.** Alignment is free at the *reach* level and not at the *torsion* level, which is exactly the gap `C-0057` opened: the reach bound is a property of one link's geometry, and torsion feasibility is a property of a conformation.

---

## The method, in one sentence

`C-0057` ranked its census by **reach margin** and solved the best 100; that ranking has no reason to be aligned, and its two best placements come out at 159.0° and −51.0°. **This task ranks the same feasible set by misalignment and solves in that order**, so the first placement that closes **is** the best-aligned closing placement on the grid. Nothing about the chemistry is re-derived; what is re-derived is *which placement*.

---

## Scale 1 — `C-0029`'s single junction: it survives, exactly aligned

| topology | placements | covalent | reach-feasible | in the ±16.87° band | solved | **closing** | **best chord** | `cos²ψ` | binding link | worst z | rarest torsion |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **two independent staples** | 69 120 | 3 546 | 1 855 | 414 | 120 | **7** | **90.0°** | **1.0000** | **0.6431 nm** | 2.630 | 0.168 % |
| scaffold excursion | 69 120 | 280 | 137 | 59 | 120 | **1** | **−51.0°** (39.0° off) | 0.604 | 0.6910 nm | 2.072 | 0.115 % |

Four things fall out.

1. **A perfectly aligned, torsion-feasible single junction exists**, at standoff azimuth **300.0°**, on the seat duplex's **own axis** (`y_c = 0`), 6.972 nm along it. Its binding link is **0.643 nm** — the *interior* of the measured `[0.60, 0.70]` nm window, which is exactly where `C-0057` said the feasible placements live and where a residual-minimising search never goes.
2. **The chord is at 90.0° to the last digit**, because the azimuth grid is 3° and alignment demands `ψ₀ ≡ 120° (mod 180°)`, which is on it. `CH-0056` is upheld at the level it was written: the chord is continuous and the design may ask for any azimuth.
3. **The scaffold excursion is feasible and cannot be aligned.** Its only closing placement among the 120 best-aligned is `C-0057`'s own census optimum, at 39.0°, worth 0.604 of the couple. `C-0029`'s *"the best independent routing IS the scaffold excursion"* does not survive the feasible set — [`CH-0072`](../challenges/CH-0072-the-alignment-is-not-free-on-the-torsion-feasible-set.md).
4. **The hit rate is 7 of 120, i.e. 5.8 %**, against `C-0057`'s 18 of 100 when ranking by reach margin. **Ranking by alignment costs feasibility** — which is the trade this task exists to price, and it is a factor of three.

---

## Scale 2 — `C-0042`'s pair: it survives, and the alignment stops being free

Both legs on **one** seat duplex, on its own axis, at a fixed separation, with four **distinct** targets. **18 of 90** axial positions carry a torsion-closing placement at all, and the pair takes whatever chords those positions offer.

| separation [bp] | 6 | **7** | 8 | **9** | **10** | 11 | 12 |
|---|---|---|---|---|---|---|---|
| `C-0042`'s worst misalignment | 0.00° | **0.00°** | 0.00° | 0.00° | 0.00° | 0.00° | 0.00° |
| **on the feasible set** | 33.0° | **69.0°** | 57.0° | **6.0°** | **6.0°** | 33.0° | 33.0° |
| chords | 57 / 114 | 147 / 159 | 147 / 114 | **−96 / 90** | −96 / 96 | 57 / −96 | 90 / 57 |
| `cos²ψ` to the loaded plane | 0.703 | **0.128** | 0.297 | **0.989** | **0.989** | 0.703 | 0.703 |
| binding link [nm] | 0.661 | 0.688 | 0.688 | **0.685** | 0.685 | 0.685 | 0.644 |
| worst covalent z | 2.56 | 2.19 | 2.56 | **2.44** | 2.82 | 2.30 | 2.44 |
| **closes** | yes | yes | yes | **yes** | yes | yes | yes |

Three things fall out and two of them move a recommendation.

1. **The pair closes everywhere in the band**, so `C-0042`'s existence result is not merely upheld but *strengthened*: it now holds at torsion level, which `C-0042` could not test and `C-0057` could not deliver.
2. **`C-0042`'s recommended 7 bp is the worst separation of the seven** — 69.0°, 12.8 % of the couple. And 69° is **past the half right angle** at which `C-0037`'s `TwoLinkBase` invariant stops representing the base: beyond 45° the restrained and free axes have exchanged, so that point is not a degraded design but outside the model the whole branch is written in.
3. **9 and 10 base pairs deliver 6.0°**, inside `C-0029`'s own allowance and worth **98.9 %** of the couple. **The row pitch is a live design variable again**, and its optimum has moved by 2–3 base pairs for a reason that is chemistry rather than the free-plane crossing `C-0042` resolved it on.

---

## Scale 3 — `C-0052`'s trio on the lone crossbar: it does NOT close

| crossbar [bp] | lattices swept | **reach-feasible for all three junctions** | best alignment the feasible lattices offer | solved | junction solves | **closing** |
|---|---|---|---|---|---|---|
| **13** | 1 800 | **750** | **6.0°** | 24 | 134 | **0** |
| 15 | 1 800 | **882** | **6.0°** | 24 | 135 | **0** |

**This is the verdict that decides the branch, and it is a bounded negative.** The trio is the *hardest* of the three scales for a reason that is arithmetic: three junctions must share **one** crossbar at **one** helical phase, and their six targets must be distinct. The lattice sweep finds plenty of *reach-feasible* trios — 750 of 1 800, at a best attainable worst-junction alignment of 6.0° — and none of the 24 best-aligned survives the torsion solve, at 134 junction solves of a possible 144.

> **What this is and is not.** It is *"not found in the 24 best-aligned of 750, at two candidate azimuths per junction"*. It is **not** *"no trio closes"*. Deepening the search is the obvious next spend and it is named as the largest open item. But the direction of the evidence is unambiguous and it is the direction the scales already showed: **one junction closes aligned at 5.8 %, two at 20 % of positions with a 6° penalty, three at none of the 24 tried.**

---

## The mechanics at the alignment feasibility gives

`C-0048`'s and `C-0052`'s pipeline re-run at the misalignments the feasible set delivers, with **three** constraints composed conservatively: the **pair's** own base floor (6.0°, at the 9 bp row it is attainable on), the **crossbar's** cap floor (6.0°, its best reach-feasible alignment — an optimistic reading of a stage that failed, used only to show the mechanics does not depend on it), and `C-0052`'s **leg-is-one-body** budget `chordPairMisalignment(m)` on their **sum**, which no rotation can reduce. The leg's own rotation is swept over 24 steps and the split with the largest critical load is taken.

| id | steps | leg [nm] | row [bp] | base [°] | cap [°] | **budget [°]** | `P_c` CanDo | `P_c` Fields | **margin CanDo** | **margin Fields** | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **`C-0052`'s `Q21`, aligned** | 21 | 7.14 | 7 | 26.2 | 75.2 | 78.5 | 8.64 | 6.49 | **2.17** | **1.63** | PASS |
| **`C-0042`'s own 7 bp row** | 21 | 7.14 | **7** | **69.0** | 6.0 | 78.5 | — | — | — | — | **NOT REPRESENTABLE** |
| `F12` | 12 | 4.08 | 9 | 6.0 | 41.4 | 45.1 | 17.69 | 13.30 | **2.44** | **1.83** | PASS |
| `F13` | 13 | 4.42 | 9 | 6.0 | 11.4 | 11.4 | 14.33 | 10.77 | 2.04 | 1.54 | PASS |
| **`F15`** | **15** | **5.10** | **9** | **26.2** | **82.3** | **56.1** | **12.98** | **9.76** | **2.45** | **1.84** | **PASS** |
| `F18` | 18 | 6.12 | 9 | 6.0 | 22.7 | 22.7 | 10.92 | 8.21 | 2.21 | 1.66 | PASS |
| **`F21`** | **21** | **7.14** | **9** | **22.5** | **79.0** | **78.5** | **8.82** | **6.63** | **2.19** | **1.65** | **PASS** |
| `F24` | 24 | 8.16 | 9 | 6.0 | 6.0 | **0.3** | 7.46 | 5.61 | 1.81 | 1.36 | PASS |
| `F26` | 26 | 8.84 | 9 | 22.5 | 89.7 | 67.2 | 6.64 | 4.99 | 1.86 | 1.40 | PASS |

Three things fall out, and the first is the one that decides how much this whole task moves the design.

1. **The mechanics is almost exactly unchanged.** All fifteen quantised leg lengths pass all nine predicates at **1.81–2.45 / 1.36–1.84**, against `C-0052`'s aligned **1.81–2.43 / 1.36–1.83** — the *same band*, and the best point is marginally **better**. **The reason is that the binding misalignment was never the chemistry.** `C-0052`'s leg-is-one-body budget runs 0.3°–89.8° over the envelope, and a 6.0° feasibility floor is inside it almost everywhere: at `F13`, `F18`, `F20` and `F23` the base misalignment *is* 6.0° and the cap takes the whole budget, and at `F24` the budget is 0.3° so the 6.0° floor is the only thing there. **A constraint that binds at only a few points of an envelope already governed by a larger one costs almost nothing, and the honest way to report a chemistry result is to say so.**
2. **`C-0042`'s own 7 bp row is not a design point at all on the feasible set.** Its 69.0° base misalignment is past the half right angle at which `C-0037`'s `TwoLinkBase` invariant — a *restrained* axis and a *free* one — stops being able to represent the base, because beyond 45° the two have exchanged. The pipeline refuses it rather than reporting a number, which is what a `require` is for. **The row must move to 9 bp**, and it costs nothing to move it.
3. **The reference row reproduces `C-0052`'s `Q21` to 1.4 %** — 2.17 / 1.63 against its published 2.20 / 1.66 — the difference being this task's 24 rotation steps against `C-0052`'s 16. Reported as a reproduction with its departure, not as a new number.

> **And the whole table describes a truss with no cap.** The crossbar's three junctions did not close (scale 3), so the cap floor these rows are computed at is the best *reach-feasible* alignment of a stage that failed the torsion test. The mechanics is reported because the acceptance predicate asks for it and because its insensitivity is itself the finding — **not** because the design it describes has been shown to exist.

---

## Sensitivities — what moves a verdict and what does not

| axis | reading | covalent | reach-feasible | in band | best feasible | **closes aligned** | verdict moves? |
|---|---|---|---|---|---|---|---|
| **reference** | `C-0029`'s geometry, 120° groove, `r_P` = 1.00 nm | 3 546 | 1 855 | 414 | 0.0° | **yes, at 0.0°** | — |
| phosphate radius | 0.90 nm (`C-0029`'s bracket) | 3 665 | 1 781 | 385 | 0.0° | **yes, at 0.0°** | **no** |
| phosphate radius | 0.8901 nm (`C-0057`'s measurement) | 3 718 | 1 785 | 384 | 0.0° | **yes, at 0.0°** | **no** |
| **groove convention** | **154° (wide)** | 4 429 | 2 231 | 456 | **1.0°** | **no** | **YES** |
| groove convention | 180° (the hard chord) | 4 554 | 2 500 | 506 | 0.0° | **yes, at 0.0°** | **no** |
| wanted axis | chord **along** the seat duplex rather than across it | 3 546 | 1 855 | 146 | 0.0° | **yes, at 0.0°** | **no** |

> **`C-0057`'s falsifier 4 does not fire here, and that is a result.** Its single-junction verdict moved across the 0.90–1.00 nm phosphate-radius bracket by a factor of **7 in strain**, because the *argmin of a distance objective* is unstable under the convention. **The census's verdict is stable, and so is this one**: at all three radii an aligned placement closes at exactly 0.0°. Optimising on the right quantity removes the sensitivity that optimising on the wrong one manufactured.
>
> **The one axis that moves it is the wide groove**, and it moves it in the *unfavourable* direction: at 154° the feasible set is **larger** (2 231 against 1 855) and no aligned placement closes in the 40 solved. That is a weaker test than the reference's 120 solves and is reported as such; what it says is that the groove convention — which `C-0029` names as the parameter the whole base couple is most sensitive to — remains the parameter to resolve.

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/anchoring/TorsionFeasibleRoutingTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a chord misalignment is an angle in `[0, π/2]` at every azimuth on a 361-point sweep; the alignment band is **half** the sheet's azimuthal quantum and reproduces `C-0029`'s 16.87° and its `cos² = 0.9158`; `alignedStandoffAzimuth` inverts `chordAzimuthOfStandoff` exactly at four wanted axes; unphysical arguments throw at **five** entry points | **PASS** |
| **2 — limiting cases** | **`C-0029`'s reported optimum reproduces `C-0057`'s failure verdict exactly** — both links at 0.600047 nm, both reach-feasible, **0 of 2 closing**, worst `z` **4.55315**, and the two torsions `C-0057` names (`ε = −22.9°`, `β = 27.4°`) recovered to < 0.6°; a link moved 50 nm away is **reach-excluded with no solve**; a zero misalignment reproduces `realisablePerpendicularBase`'s favourable **and** free readings; **the rigid-cap limit reproduces `C-0037`'s truss** — `SolvedTrussCap(asserted = true)`'s frame couple and free-plane critical load equal `TriangulatedStandoff`'s to 1e−9 at the matching head tie, and its rigid height is exactly 0; an alignment band of a full quarter turn admits the **whole** feasible set | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the verdict at `C-0029`'s optimum is unchanged between a 30-step / 3-refinement and a 72-step / 5-refinement torsion grid; a finer azimuth grid never loses a feasible placement nor worsens the best attainable alignment; **the best feasible misalignment is 0.000000° at azimuth grids 60 / 120 / 240 and axial grids 32 / 64 / 128, departure 0.000e+00 at every level**; the worst covalent `z` at the best-aligned feasible placement moves **0.094** between the 60-step search grid and the 180-step verdict grid — 3.7 %, and it does not cross the 3.0 σ ceiling | **PASS** |
| **5 — literature and upstream** | `C-0057`'s census counts reproduce to **0** on all four; `C-0029`'s binding link to 0; `C-0057`'s worst `z` at that optimum to 0; `C-0048`'s recommended frame couple, both critical loads, both margins and its span; `C-0052`'s 78.53° budget at 21 steps; `C-0042`'s conserved 91.76 chord budget; `C-0029`'s allowance and its `cos²`. **Worst departure over 16 reproductions: 2.36e−3**, which is `C-0048`'s own published rounding of 1.4634 to 1.46 | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The base couple budget is conserved under the azimuth**: `loaded + free` is invariant to 1e−9 over 91 misalignments — `C-0042`'s rank-one identity, re-asserted on the object this task moves.
2. **A chord is a line**: its misalignment is invariant under a half turn of the standoff, at 37 azimuths, compared **absolutely** because both quantities are meant to vanish where the chord *is* the flexure axis.
3. **The chord azimuth is a function of the standoff's azimuth and of nothing else** — asserted over solved placements at four axial positions × three lateral seats, modulo a half turn because `JunctionClosure` reports it through `atan2`.
4. **A reach verdict is invariant under a rigid motion** of the whole junction, to 1e−12. Nothing in the construction imposes it: every frame is rebuilt from the moved coordinates.

---

## Validity range

- **TRL 1–3. Nothing here is measured about the junction**, and a torsion check is a **necessary** condition only. It does not establish that a junction assembles, folds, hybridises correctly or survives 2 mM Mg²⁺.
- **Every negative is bounded by a solve cap and is a *"not found within the budget"*.** The single junction solves the **120 best-aligned of 1 855**; the pair solves at most **four candidates per axial position** over 90 positions at one lateral seat; the trio solves the **24 best-aligned of 750** reach-feasible lattices at **two** candidate azimuths per junction. A deeper search can only **improve** every alignment reported here, so the misalignments are **upper bounds** and the trio's failure is the weakest statement in the claim.
- **The pair search is run at ONE lateral seat**, the seat duplex's own axis — which is where `C-0042`'s own optimum sits and where its sensitivity table reports no dependence, but it is a restriction and it is declared.
- **The design table composes three constraints conservatively and independently**: the pair's misalignment floor at the base, the crossbar's at the cap, and `C-0052`'s quantised budget on their **sum**. A joint search over the sheet lattice, the crossbar's continuous phase and the leg's length at once could do better. **It cannot do worse**, so the margins reported are a **lower** bound on what a joint search would find.
- **The cap floor is taken from the trio's best *reach-feasible* alignment (6.0°), because no trio closed.** That is an optimistic reading of a stage that failed, and it is used only to show that the mechanics is insensitive to it — which is the finding. **If no trio closes at all, the cap floor is not 6.0° but undefined, and the design table describes a truss with no cap.**
- **`C-0057`'s whole validity range is inherited**: every residue is a rigid body, both nucleotide templates are single draws from a distribution, the occupancy test is **marginal** rather than joint (so a *"closes"* verdict is weaker than a *"does not close"* one), the survey is X-ray, DNA-only, ≤ 2.3 Å, and the `PINNED` reading is not used.
- **The search grid is `C-0057`'s census grid** (60 torsion steps, 4 refinements), not its verdict grid (180 / 6). The two differ by 0.094 σ at the probe placement, which does not cross the ceiling — but a placement within 0.1 σ of 3.0 could be classified differently by the two, and none of the reported optima is.
- **The mechanics is `C-0048`'s and `C-0052`'s, unchanged**, including `C-0020`'s derived and unmeasured `k_s`, on which the whole base couple rests. `T-9` still owns it, and `C-0048` reports the margin falling to 0.93 / 0.70 at `k_s`/32.
- **Only the pair and the single junction were searched on the sheet; the flexure's own far end was not.** `C-0052` notes the flexure's two ends both want a vertical chord and that the ±1 base-pair ambiguity there is a whole quantum. That is untouched here.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the measured backbone — bonds, angles, torsion occupancies, both nucleotide templates | 0.16022 nm, 121.30°, … | **DERIVED**, in `C-0057`, from 13 084 crystallographic linkages; consumed here as a library and **re-derived** at `C-0029`'s optimum as a gate-2 test |
| the `[0.60, 0.70]` nm phosphodiester window | 0.60 / 0.70 nm | **CITED** via `C-0029` (Bosco et al.), and `C-0057` measures 0.607 / 0.664 instead. Carried unchanged so the feasible set **is** `C-0057`'s |
| phosphate radius, groove angle | 1.00 nm, 120° | **CITED** via `C-0029` (Hedley et al.); both swept |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 10.67 | **CITED** via `C-0009`/`C-0029` |
| `EI` = 230 pN·nm², Fields et al.'s 172.906 | | **CITED** via `C-0009`/`C-0028` |
| `C-0048`'s and `C-0052`'s design pipeline constants | 71.31, 8.95, 1.95 / 1.46, 28.25, 78.53° | **CITED**, and reproduced here as gate-5 tests to ≤ 2.4e−3 |

Everything else — the alignment band as an azimuth interval, the re-derived census, the misalignment ranking, every closure at every scale, the pair field, the crossbar lattice sweep, the leg-budget composition and every margin — is **derived here in code**, with `C-0029`'s, `C-0037`'s, `C-0042`'s, `C-0048`'s, `C-0052`'s and `C-0057`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **A deeper trio search.** 24 of 750 lattices at two azimuths per junction is thin, and the trio is the junction the design cannot do without. **This is the largest open item and it decides the branch.** **ANSWERED by [`C-0062`](C-0062-crossbar-trio-existence.md) (`T-127`): the trio EXISTS**, at every one of the 21 admissible `(crossbar, row)` configurations, 609 closing trios over 49 857 lattices solved and 149 789 junction solves — and this claim's budget was worth **0.28** trios under the junctions' own marginal closure rates, so its null result was the expected one rather than evidence of anything.
2. **A joint search over the sheet lattice, the crossbar phase and the leg length at once**, rather than the conservative composition used here. **PARTLY ANSWERED by `C-0062`**, which sweeps the crossbar length and the row pitch jointly with the lattice and recomputes the design table row by row at one pitch throughout — and finds the margins unchanged to three digits.
3. **An atomistic or oxDNA relaxation of the aligned single-junction placement.** There is now a *specific, aligned* candidate to relax — azimuth 300.0°, `x = 6.972` nm, `y = 0`, links at 0.643 nm — which there was not before.
4. **The groove convention.** It is the one axis that moves the verdict, and `C-0029` already names it as the parameter the base couple is most sensitive to.
5. **The flexure's own far end.** Two junctions on one flexure, both wanting a vertical chord, at a span the placement condition sets.

## Challenges

**Raises [`CH-0072`](../challenges/CH-0072-the-alignment-is-not-free-on-the-torsion-feasible-set.md)** against `C-0042`'s *"the azimuth costs exactly nothing"* and its 7 bp row, and against `C-0029`'s recommended scaffold excursion. **No upstream number fails to reproduce** — 16 reproductions at ≤ 2.4e−3, four of them exact.

**None stands against this claim.** The four ways it would fail:

1. **A deeper trio search finding a closing, aligned trio.** That would restore the branch and is the open item this claim names first. It would not touch anything else here.
2. **An atomistic relaxation showing a junction relieves its strain by deforming its duplexes.** That enlarges the feasible set at every scale and could restore the alignment at 7 bp.
3. **A wider backbone survey** repopulating the torsion bins the failed closures land in.
4. **A demonstration that the pair's single lateral seat is a real restriction.** `C-0042` reports no dependence on the seat over ±0.9 nm on the distance criterion; whether that survives the torsion one is untested here.
