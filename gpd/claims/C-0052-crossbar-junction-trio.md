# C-0052 — THREE 90° junctions DO close on one lone crossbar duplex, at `C-0048`'s own 13 bp and with zero unpaired nucleotides; but a LEG IS ONE BODY WITH TWO JUNCTIONS, so its base chord and its cap chord differ by `m × 33.74°` and cannot be chosen independently — and at `C-0048`'s own recommended leg length that difference is 78.5°, which puts the recommended azimuth pair out of reach at the length it is recommended at

| | |
|---|---|
| **Task** | [`T-117`](../tasks/T-117-crossbar-junction-trio.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a deterministic closure search over **three** junctions on one **lone, finite** seat duplex, on `C-0029`'s own backbone geometry and its own admissibility test, plus `C-0048`'s whole design pipeline re-run as a library at stated azimuths) **+ logical** (four cheap bounds, all closed form: a truncated seat contact, an exact convex-body clearance, a **chord-twist quantisation**, and a duplex free energy) **+ literature** (two thermodynamic parameter sets fetched and read for this task, and 15 recorded negatives) |
| **Verdict** | **PASS on the acceptance question, and the answer to the question nobody asked is the finding.** Three 90° junctions — two leg heads from below and the flexure's own end from the side — **close on one lone crossbar duplex at `C-0048`'s own 13 bp**, with all six links inside the measured `[0.60, 0.70]` nm phosphodiester step, **zero unpaired nucleotides**, six distinct targets, no van der Waals contact anywhere in the assembly, and every junction's **truncated** seat contact at 1.83–1.96 nm against `C-0042`'s 1.60 nm floor. It closes at **every** crossbar length from 13 to 19 bp and at **every** row separation from 6 to 12 bp, with the chord alignment the design wants available for **0.00°** on the converged grid. **And it still closes when the legs' azimuths are not searched at all but carried up from `C-0042`'s solved base — at 14 of the 15 integer leg lengths, with the residual misalignment exactly the arithmetic budget below. So `C-0048`'s cap exists, and the risk it opened is retired at the level a phosphate-distance model can retire it.** Three things were not anticipated. **The rim clearance is an arithmetic identity, and it is 0.02 nm**: `2R = 2.00 nm` is 5.88 rises, so `ceil` buys 6 and the minimum crossbar overhangs each leg's footprint by exactly **0.02 nm at every row width** — the recommended cap is one base pair from not covering its own row. **The flexure's entry between the legs is tighter than any packed origami**: solved as two convex bodies, a leg's surface and the flexure's come within **0.249 nm** at the 7 bp row, against the **0.54–0.69 nm** gaps a honeycomb and a single-layer sheet actually keep, and it is the **row width** that buys it back (0.184 nm at 6 bp, 0.714 at 12). **And the azimuth pair is not two variables but one variable and a length.** A leg is one rigid duplex with a junction at *each* end, so its two terminal chords differ by `m × 33.74°`, folded modulo `π`; `C-0048`'s recommended 7.00 nm leg rounds to **21 steps**, whose budget is **78.53°** — it cannot present its base chord along the flexure axis *and* its cap chord across it, and the corner it is forced toward is the one `C-0048` itself reports at 6.20 pN. **The design survives everywhere** — all nine predicates PASS at every integer leg length in the 12–26 step envelope, at margins **1.81–2.43** on CanDo's rigidity and **1.36–1.83** on Fields et al.'s — and, because rotating the leg trades the two misalignments one for one, the pair the constraint forces the design onto at 21 steps is **better** than the recommended one (2.20/1.66 against 1.95/1.46), because `C-0048` picked its cap azimuth to maximise the plane that does not govern ([`CH-0067`](../challenges/CH-0067-a-leg-is-one-body-and-its-two-chords-are-not-independent.md)). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The closure test is `C-0029`'s, applied three times: a **necessary** condition and never a sufficient one, so a *"closes"* verdict is an **upper bound on buildability**. The chord-twist quantisation is **arithmetic** and inherits none of that caveat. |
| **Provenance** | `gpd/results/T-117-crossbar-junction-trio.json`, produced by `anchoring.CrossbarJunctionTrioStudyKt`; **8 cheap-bound quantities, 21 geometry records, 15 twist records, 74 closure records, 17 design records, 23 stability records, 6 sensitivity records, 12 convergence records, 17 upstream reproductions, 5 literature records**; **31 gate-named tests in `CrossbarJunctionTrioTest`**; `tools/verify.sh` **BUILD SUCCESSFUL** on the whole suite, on its own isolated tree, with **no** `--drop-file` needed. The study is deterministic by construction — fixed grids, strict comparisons, no tolerance in any control flow — and rounded at the **serialisation boundary**; a byte-for-byte re-emission was **not** performed in this iteration and is not claimed |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 0.34 nm rise, phosphate radius **1.00 nm**, phosphodiester step **0.60–0.70 nm, measured**; closure on `C-0029`'s nominal **120°** groove with the wide 154° carried; mechanics on the **hard, convention-free 180° chord**, as `C-0037`, `C-0042` and `C-0048` all adopt; `EI = 230 pN·nm²` (CanDo model input) with every critical load also on Fields et al.'s implied **172.9**; the leg's own twist carried at **10.67 and 10.5 bp/turn** |
| **Consumes** | [`C-0048`](C-0048-truss-cap.md) (`SolvedTrussCap`, `TrussCapGeometry`, `capBendingStiffness`, `capTorsionalStiffness`, `cappedHeadFlexibility`, `cappedTrussBucklingLoad` — **re-run as a library**), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`seatContactLength`, `pairStericFloorBasePairs`, `chordBaseAxes`, `foldedChordMisalignment`), [`C-0029`](C-0029-perpendicular-junction-routing.md) (`DuplexBackbone`, `seatFaceHeight`, `linkWindowResidual`, `unpairedNucleotidesForGap`, `maximumBaseRotationalStiffness`, `couplePhaseProjection`, the counting theorem), [`C-0037`](C-0037-triangulated-standoff.md) (`TwoLinkBase`, `TrussLayout`), [`C-0030`](C-0030-coupled-standoff-joint.md) (`CoupledJointFlexure`, `coupledFlexureSpan`, `FlexureOrientation`, `DrawInModel`, `FIELDS_BENDING_RIGIDITY`, `bracedColumnBucklingLoad`, `peakFlexureCompression`), [`C-0035`](C-0035-flexure-mounting-sense.md), [`C-0023`](C-0023-two-sided-coupling.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0067`](../challenges/CH-0067-a-leg-is-one-body-and-its-two-chords-are-not-independent.md) against `C-0048`'s recommended azimuth pair and leg length, and against `C-0037`/`C-0042` insofar as they treat the leg length as a continuum |

---

## The claim, in one line

**`C-0048` derived a cap and did not route it, and the routing turns out to be the easy half — three junctions and six covalent links fit on a lone 13 bp crossbar, with nothing to spare at the rim and nothing to spare in the links — while the half nobody had looked at is that the two ends of a truss leg are the two ends of ONE body, so the design cannot choose their chord azimuths separately, and the leg length everyone has been treating as a continuum is the variable that sets the difference.**

---

## The four cheap bounds, which ran first — and the two that decided it

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the leg's rim clearance** on the minimum crossbar, and its **truncated** seat contact | **0.02 nm**, at *every* row width; contact **2.000 nm** | the exclusion `C-0042` introduced against a *lateral* rim seat does **not** bind axially — but only because `ceil` bought a whole base pair. It is an identity, not a margin: `2R` is 5.88 rises and the ceiling gives 6, so the slack is `0.34 × 6 − 2.00 = 0.04` nm shared between two ends |
| **2** | **the leg-to-flexure solid clearance**, exactly, by alternating projection between two convex bodies | **0.249 nm** at the 7 bp row (0.184 at 6, 0.714 at 12) | **positive, so the flexure's end does enter between the legs — and it is tighter than any packed origami**, against the 0.69 nm surface gap of a 2.69 nm single-layer sheet and the 0.54 nm of a 2.54 nm honeycomb. A **capsule** approximation — the distance between the two axis segments, `√(1.19² + 1² + 1²) = 1.848 nm`, less two radii — reports **−0.15 nm**, i.e. a clash, where the solved bodies clear by **+0.25**: it is wrong by more than the answer, because it rounds each flat end face into a hemisphere exactly where the two faces pass each other |
| **3** | **the chord-twist quantisation** of `C-0048`'s own 21-step leg | **78.53°**, against a wanted 90° separation between the base and cap chords | **the bound written to bind, and it binds.** The best available in the 12–26 step envelope is **0.25°** at 24 steps, and **11.39°** at 13 — so the constraint does not close the design, it *chooses its length* |
| **4** | the crossbar's **duplex free energy** at 13 bp | **−18.7 to −21.7 kcal/mol** = **31–36 `k_BT`** at 2 mM MgCl₂ on measured magnesium nearest-neighbour parameters | the crossbar is a duplex. The **sequence** spread (−10.1 to −28.5 kcal/mol) is 2.8× and dominates the length; and overhang is mechanically free, because `12EI/w` and `4C/w` both carry the **row**, not the crossbar |

> **Bound 1 is the one that looked like a formality and is not.** `C-0042` introduced `seatContactLength` to stop *its* search parking a standoff on the **lateral** rim of a sheet duplex; on a 4.42 nm crossbar the **axial** rim sits 1.02 nm from each leg's axis, which is 0.02 nm outside the leg's own footprint. The exclusion has to be written in the other direction — and having written it, the finding is that the recommended crossbar passes it by two hundredths of a nanometre, at every row width, for an arithmetic reason.
>
> **Bound 2 is the one nobody would have run.** `C-0048` establishes that the flexure cannot be the cap because a duplex across the row seats neither leg. It does not ask whether the flexure's *end* fits between them, and the honest test needs the bodies' **flat end faces**: both are a cylinder intersected with a half-space, hence convex, so alternating projection converges to the exact closest pair, and the answer at the 7 bp row is a 0.249 nm gap between two surfaces that a real origami never brings closer than 0.54.

---

## The closure, which is the acceptance question

`C-0029`'s admissibility test applied **three** times — a phosphate pair inside the measured `[0.60, 0.70]` nm step, no van der Waals overlap — with the six targets required distinct across the whole assembly, the two legs on the crossbar's one straight axis, and every junction's truncated seat contact above `C-0042`'s 1.60 nm floor.

### `FREE` — every junction's own azimuth searched, which is the acceptance question as posed

| crossbar [bp] | 13 | 14 | 15 | 16 | 17 | 18 | 19 |
|---|---|---|---|---|---|---|---|
| row [bp] | 7 | 7 | 7 | 7 | 7 | 7 | 7 |
| **closes** | **yes** | **yes** | **yes** | **yes** | **yes** | **yes** | **yes** |
| binding link [nm] | 0.679 | 0.665 | 0.665 | 0.661 | 0.661 | 0.678 | 0.678 |
| unpaired nucleotides | **0** | **0** | **0** | **0** | **0** | **0** | **0** |
| six distinct targets | yes | yes | yes | yes | yes | yes | yes |
| worst chord misalignment | 0.76° | **0.00°** | **0.00°** | **0.00°** | **0.00°** | 0.53° | 0.53° |
| closest two termini [nm] | 0.655 | 0.648 | 0.648 | 0.648 | 0.648 | 0.653 | 0.653 |
| minimum seat contact [nm] | 1.833 | 1.833 | 1.833 | 1.833 | 1.833 | 1.833 | 1.833 |

and across the row widths `C-0042` admits, each on the crossbar its own `ceil` demands plus two:

| row [bp] | 6 | **7** | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|
| crossbar [bp] | 14 | 15 | 16 | 17 | 18 | 19 | 20 |
| **closes** | **yes** | **yes** | **yes** | **yes** | **yes** | **yes** | **yes** |
| binding link [nm] | 0.700 | 0.665 | 0.700 | 0.700 | 0.695 | 0.645 | 0.691 |
| worst misalignment | 5.53° | **0.00°** | 3.62° | 1.07° | **0.00°** | 1.07° | **0.00°** |
| closest two termini [nm] | 0.355 | 0.648 | 0.938 | 1.327 | 0.373 | 1.952 | 2.020 |

Five things fall out and none was assumed.

1. **It closes everywhere in the searched band**, with **zero** unpaired nucleotides at every point and six distinct crossbar phosphates. `C-0048`'s cap is routable.
2. **The alignment is free, exactly as `C-0042` found one storey down** — 0.00° at most lengths and never worse than 5.53°, which is worth `1 − cos²(5.53°) = 0.93 %` of a junction's couple.
3. **The binding link sits at 0.645–0.700 nm**, i.e. at the **C2′-endo** end of the measured step, exactly as `C-0042`'s aligned pair does. `T-71` inherits that pucker, now three junctions' worth.
4. **The narrow rows are the tight ones and it is the TERMINI, not the bodies, that measure it**: at 6 bp two termini of different junctions come within **0.355 nm**, a hair above the 0.35 nm van der Waals floor, where 12 bp leaves 2.02 nm. The steric floor `C-0042` derived from the *bodies* is not the binding one once a third junction is present.
5. **The rim never binds.** Every closing trio keeps its seat contact at 1.833–1.960 nm against `C-0042`'s 1.60 nm floor, at the minimum crossbar as much as at 19 bp.

### `LOCKED` — the legs' azimuths carried up from `C-0042`'s solved base

The strictly harder question: the two legs' own azimuths are **not** searched but taken from `C-0042`'s solved base placement (both chords exactly on the flexure axis) carried up the leg through `m` steps of twist, leaving only the crossbar's helical phase, its axial phase, the shared lateral seat, each leg's half-turn and the flexure's own azimuth free.

**It closes at 14 of the 15 integer leg lengths** — every one except **13 steps**, which fails all four half-turn combinations at this crossbar length and row. And the residual chord misalignment is **exactly the quantisation budget** at every length (45.13° at 12 steps, 78.53° at 21, 0.25° at 24), which is the internal consistency check the whole finding rests on: with the legs' azimuths locked there is nothing left for the search to align, and it recovers the arithmetic.

> **The `LOCKED` sweep is a restricted question and its one failure is not a refusal.** It fixes the leg's own rotation at the value that puts the base chord *exactly* on the flexure axis, the crossbar at 15 bp and the row at 7 bp — and the design does not want the first of those, because the optimum splits the budget between the two ends. A failure at one point of a restricted space is reported as one.

---

## The quantisation, which is the finding

A duplex **end** has exactly two strand termini (`C-0029`), and their chord's direction is the terminal base pair's azimuth plus `Δ/2 + π/2` — *a function of the body's own azimuth alone* (`C-0042`'s gate-3 identity). **A truss leg has two ends and both carry junctions**, and its two terminal base pairs are `m` steps apart, so

&nbsp;&nbsp;&nbsp;&nbsp;**`chord_cap − chord_base = m × 33.74°`, folded modulo `π`.**

`C-0048`'s recommended design wants that difference to be **90°** — the base chord along the flexure axis, the cap chord across it.

| leg, steps `m` | 12 | **13** | 14 | 15 | 16 | 17 | 18 | 19 | 20 | **21** | 22 | 23 | **24** | 25 | 26 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| leg [nm] | 4.08 | **4.42** | 4.76 | 5.10 | 5.44 | 5.78 | 6.12 | 6.46 | 6.80 | **7.14** | 7.48 | 7.82 | **8.16** | 8.50 | 8.84 |
| relative chord [°] | 44.9 | 78.6 | 112.4 | 146.1 | 179.8 | 33.6 | 67.3 | 101.1 | 134.8 | **168.5** | 22.3 | 56.0 | **89.8** | 123.5 | 157.2 |
| **budget** [°] | 45.1 | **11.4** | 22.4 | 56.1 | **89.8** | 56.4 | 22.7 | 11.1 | 44.8 | **78.5** | 67.7 | 34.0 | **0.25** | 33.5 | 67.2 |
| `cos²` | 0.50 | 0.96 | 0.86 | 0.31 | **0.00** | 0.31 | 0.85 | 0.96 | 0.50 | **0.04** | 0.14 | 0.69 | **1.00** | 0.70 | 0.15 |
| budget at 10.5 bp/turn [°] | 38.6 | **4.3** | 30.0 | 64.3 | 81.4 | 47.1 | 12.9 | 21.4 | 55.7 | **90.0** | 55.7 | 21.4 | 12.9 | 47.1 | 81.4 |

Four things fall out and none was assumed.

1. **`C-0048`'s own leg is the worst choice in the envelope, on every twist reading.** 21 steps gives 78.5° on the square lattice's 10.67 bp/turn, **90.0°** on the natural 10.5 and 83.1° on 10.4. A free-standing leg carries no crossovers, so which of those is its twist is a live question — and it does not matter, because 21 steps is on the wrong side of all three.
2. **13 steps is the length that survives every reading** — 11.4° / 4.3° / 0.0° — and 24 steps is the best on the square lattice alone (0.25°) but only 12.9° on the natural one. **And 13 steps is also the one length at which the `LOCKED` closure fails**, at the single crossbar length and row that sweep fixes. The two statements are about different things — one is arithmetic on the leg, the other a phosphate search on the crossbar — and a design would resolve it by moving the crossbar length or the row, neither of which the `LOCKED` sweep varies. It is recorded because it is exactly the kind of coincidence a reader should be able to see.
3. **The quantum is on the RELATIVE azimuth and nothing else.** `CH-0056` is upheld, not overturned: a free duplex's *absolute* azimuth is continuous, `C-0042`'s 0.00° base alignment stands, and this claim's own closure search sweeps the azimuth continuously and uses it. What is quantised is a **difference between two ends of one body** — a quantity a lone standoff does not have.
4. **The same statement applies to the flexure**, whose two ends sit on two crossbars and both want a **vertical** chord, i.e. a difference of **0** — and there the length is an *output* of the placement condition rather than a free choice. At `C-0048`'s solved 28.25 nm span the count is **76 or 77 steps** depending on whether the end faces are taken at each crossbar's axis or its surface, worth **44.2°** and **77.9°** respectively: *the ±1 ambiguity is a whole quantum*, which is the point rather than a caveat. The lattice does contain an answer and it is the same one — **multiples of 16 steps** land within 0.9° of parallel, so **80 steps** delivers **0.84°** at a **29.20 nm** span, 3.4 % longer than the placement condition solved for. **Reported and not adopted**: re-placing a flexure array is `C-0046`'s and `T-116`'s currency, not this claim's.

### The conservation that makes it a design choice rather than a failure

Rotating the leg about its own axis moves **both** chords together, so it takes misalignment off one end and puts exactly as much onto the other:

&nbsp;&nbsp;&nbsp;&nbsp;**`ψ_base + ψ_cap = |m × 33.74° − 90°|`, exactly, for every rotation in the reducing sense** — asserted as a gate-3 test at four rotations.

**Only the difference is quantised.** The design chooses *where* to spend the budget, not whether to. That is `C-0042`'s rank-one identity one storey up: there, a chord's two *axes* shared one conserved couple; here, a body's two *ends* share one conserved misalignment.

---

## The design, at the azimuths the leg's length permits

`C-0048`'s pipeline re-run unchanged at every integer leg length, with the leg's own rotation swept to maximise the critical load. Seven base pairs of row, 45 paths, secant placed at 33.3333 pN/nm at 3 nm by construction, favourable mounting (`C-0035`), duty at §3's **desired** 10 nm.

| id | steps | leg [nm] | `h` [nm] | base [°] | cap [°] | budget [°] | span [nm] | tangent | supply | duty | `P_c` load | `P_c` free | margin CanDo | margin Fields | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`C-0048`** | — | 7.00 | 8.00 | 0.0 | 0.0 | **unavailable** | 28.25 | 30.93 | 1.81 | 4.60 | 8.95 | 9.24 | **1.95** | **1.46** | PASS |
| `C-0048` along | — | 7.00 | 8.00 | 0.0 | 90.0 | unavailable | 30.30 | 29.40 | 2.20 | 4.18 | 9.67 | 6.20 | 1.48 | 1.12 | PASS |
| `Q13` | 13 | 4.42 | 5.42 | 15.5 | 26.9 | 11.4 | 29.33 | 33.05 | 1.36 | 6.67 | 15.24 | 16.40 | **2.28** | **1.72** | PASS |
| `Q16` | 16 | 5.44 | 6.44 | 23.2 | 66.6 | 89.8 | 30.17 | 30.34 | 1.77 | 5.06 | 12.29 | 12.48 | **2.43** | **1.83** | PASS |
| `Q19` | 19 | 6.46 | 7.46 | 9.8 | 20.9 | 11.1 | 28.97 | 30.58 | 1.82 | 4.71 | 10.14 | 10.43 | 2.15 | 1.62 | PASS |
| **`Q21`** | **21** | **7.14** | **8.14** | **19.0** | **59.5** | **78.5** | **30.12** | **29.33** | **2.27** | **4.08** | **8.99** | **9.09** | **2.20** | **1.66** | **PASS** |
| `Q24` | 24 | 8.16 | 9.16 | 6.3 | 6.6 | **0.25** | 28.37 | 30.49 | 2.12 | 4.12 | 7.46 | 7.94 | 1.81 | 1.36 | PASS |
| `Q26` | 26 | 8.84 | 9.84 | 14.1 | 53.2 | 67.2 | 30.09 | 29.19 | 2.79 | 3.63 | 6.87 | 6.94 | 1.89 | 1.42 | PASS |

> **Nothing fails, anywhere.** All fifteen quantised leg lengths pass all nine predicates, at margins 1.81–2.43 (CanDo) and 1.36–1.83 (Fields et al.), against `C-0048`'s recommended 1.95 / 1.46.
>
> **And the constrained design beats the unconstrained one it is not allowed to have.** At `C-0048`'s own 21 steps the best split of the 78.53° budget is base **19.0°** / cap **59.5°**, worth **2.20 / 1.66** against the recommended pair's **1.95 / 1.46**. The reason is visible in the last two columns: at every quantised optimum the loaded and free critical loads have come **together** — 8.99 against 9.09 at `Q21`, 11.788 against 11.788 at `Q17`. **The optimum is the balance of the two planes, and `C-0048`'s recommendation is a corner of it**: that claim chose the cap azimuth *"because that is where `P6` binds"*, i.e. to maximise the **free** plane, and at its own design point the **loaded** plane governs (8.947 against 9.236). Moving couple out of the free plane and into the loaded one raises the minimum, which is the quantity `P6` is judged on.

---

## Is the crossbar a structural member?

`C-0048` records that *"a 13 bp crossbar is a short duplex and its own thermodynamic stability is not modelled"*. It is modelled here, on **measured magnesium** nearest-neighbour parameters rather than on the 1 M NaCl set, because the buffer is 2 mM MgCl₂.

| 13 bp crossbar | sequence-averaged | weakest (all TA/AT) | strongest (all GC/CG) |
|---|---|---|---|
| **2 mM MgCl₂, natural log** (adopted, pessimistic) | **−18.72** kcal/mol = **31.4 `k_BT`** | −10.13 = **17.0 `k_BT`** | −28.47 = 47.7 `k_BT` |
| 2 mM MgCl₂, decimal log | −21.71 = 36.4 `k_BT` | −13.80 = 23.2 `k_BT` | −31.00 = 52.0 `k_BT` |
| 1 M NaCl cross-check (SantaLucia) | −16.01 = 26.9 `k_BT` | −5.93 = 9.9 `k_BT` | −25.85 = 43.4 `k_BT` |
| **base pairs needed to match the averaged 13-mer** | 13 | **24** | 9 |

**The crossbar is a duplex.** Even the worst sequence a designer could pick — an all-`TA/AT` 13-mer — carries **17 `k_BT`** of duplex free energy at 2 mM MgCl₂ on the pessimistic reading of the salt correction, and the sequence-averaged one carries **31**. Three things make that a usable answer rather than a number:

1. **Sequence, not length, is the lever.** The spread over sequence is **2.8×** where eight base pairs of length are worth 1.7×; and the length that would restore an average 13-mer's stability at the worst sequence is **24 bp**.
2. **Lengthening is nearly free, mechanically.** `C-0048`'s cap terms are `12EI/w` and `4C/w`: both carry the **row pitch**, not the crossbar's length, and the overhangs are free ends that carry nothing. So the crossbar may be lengthened to whatever the thermodynamics wants at a cost **only in plan area** — which is `T-96`'s and `T-116`'s currency, not this claim's.
3. **The number is a lower bound on the tethered case.** These are formation free energies at a 1 M standard state, so their initiation term stands in for a bimolecular association that a covalently routed crossbar — `C-0029`'s scaffold excursion, in and out of the same strand — does not have to pay.

> **And the parameter set matters more than the arithmetic.** On SantaLucia's 1 M NaCl set the same 13-mer reads 26.9 `k_BT` and its worst sequence **9.9** — which is the wrong side of "is it a duplex at 300 K". The measured **magnesium** set is 17 % more stabilising per step and its salt correction is **measured per motif**; the paper's own headline is that a homogeneous correction is *"definitely incompatible"* with the data.

---

## The five verification gates

Executed as **31 gate-named tests** in `src/test/kotlin/anchoring/CrossbarJunctionTrioTest.kt`; the **whole suite** green through `tools/verify.sh` on its own isolated tree, 7 m 40 s, no `--drop-file`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a truncated seat contact is a length, scales with the standoff radius, and never exceeds either the untruncated contact or the seat's own length; the chord twist is an angle **per base-pair step**, so halving the base pairs per turn doubles it and zero steps give zero; a duplex free energy is a step energy times **steps** plus one initiation, so doubling the step doubles the propagation term and leaves the initiation alone, and a one-base-pair duplex is initiation only; unphysical arguments throw at **nine** entry points | **PASS** |
| **2 — limiting cases** | a **lone** seat's face height equals `C-0042`'s `seatFaceHeight` with the neighbours pushed to `1e6` nm, at five offsets, and is the seat radius exactly inside the rim; the truncated contact collapses to zero one radius outside the rim, keeps exactly half **on** it, and is even in the lateral offset; two coaxial solids separated along their common axis are exactly apart, touching faces give exactly zero, and two parallel cylinders give the axis distance less two radii; the chord twist folds like a **line** into `[0, π)` over 40 steps, returns to zero after a helical repeat, and never exceeds a right angle; **the trio search reduces to `C-0042`'s pair** — two legs, no flexure, a long crossbar and `C-0042`'s own wanted chord — reproducing its perfect alignment, its covalent verdict and a binding gap inside the measured window; a crossbar too short for its row does not exist; the flexure clears the legs at every admissible row and a wider row clears more; the salt correction is exactly zero at 1 M on **both** log conventions and strictly destabilising below it | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the search returns the **identical** trio — phase, axial offset, residual and misalignment, to the last bit — on a repeat call; the verdict does not move when both continuous grids are refined, and a finer grid never returns a worse alignment; the solid separation agrees between 200 and 4000 iterations to **1e−6** and is **symmetric in its two arguments** | **PASS** |
| **5 — literature and upstream** | `C-0029`'s window, phosphate radius, rise and azimuthal quantum are asserted to be what the search actually uses; `C-0048`'s crossbar geometry is reproduced **from the row**, including the 4.38 nm demand against the 4.42 nm duplex; `C-0042`'s conserved chord budget **91.76** at five azimuths; `C-0048`'s whole `Sy7` design row and its `Sx7` corner reproduced through an independently written assembly; SantaLucia's ten unified steps, their average and both initiation terms, with the ten transcribed steps asserted to average to the paper's own tabulated average; Huguet's magnesium steps and salt factors; and `1 kcal/mol = 1.678 k_BT` **derived** from the two constants rather than quoted | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The chord azimuth is a function of the junction's own azimuth alone**, `ψ₀ + Δ/2 + π/2` — asserted over the *solved* placements of all three junctions, in two different chord planes. `C-0042`'s identity on a body it was not derived for, and it is what makes the quantisation a statement about a **difference**.
2. **The relative chord azimuth is additive in the base-pair steps** — `m + n` steps is `m` steps then `n` steps, modulo `π`, asserted at three pairs. The twist between two ends is a **group action**, which is why it cannot be relieved by anything but the length.
3. **The base and cap misalignments are conserved under the leg's own rotation**, exactly, in the sense that reduces the cap's — asserted at four rotations — and strictly increased in the other sense. `C-0042`'s rank-one identity one storey up.
4. **The trio does not care in which order its junctions are listed** (exact as physics), and **a chord is a line**: every junction's misalignment is invariant under a half turn, at four azimuths.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | **the trio not closing at any crossbar length** — the branch would close on chemistry | **no** | it closes at 13–19 bp, at 6–12 bp of row, on both groove conventions and both twist readings |
| 2 | **the trio closing only at the free azimuths** | **no** | it closes at 14 of 15 leg lengths with the legs' azimuths locked to `C-0042`'s base, and the residual is exactly the arithmetic budget |
| 3 | **bound 3 not binding** | **NO — it binds, and it is the finding** | 78.53° at `C-0048`'s own recommended leg, on all three twist readings |
| 4 | **the reductions failing** | **no** | the trio search reproduces `C-0042`'s pair, and `C-0048`'s whole `Sy7` row and `Sx7` corner come back at ≤ 3.4e−9 |
| 5 | **the crossbar not being a duplex at any length that fits the plan** | **no** | 17–48 `k_BT` at 13 bp over the sequence range, at 2 mM MgCl₂ on measured magnesium parameters |

**The pre-registered prediction was right in three of its four parts and wrong in the one that would have cost plan area.** It predicted that the trio closes (it does), that bound 3 binds hardest at `C-0048`'s own leg length (it binds at 78.5° there, and the only worse length in the envelope, 16 steps at 89.8°, is not one anybody had recommended), and that the crossbar's stability is bought by **overhang**, which is mechanically free (it is). It predicted that **the axial rim would bind, so the crossbar would have to be longer than `C-0048`'s minimum**. It does not: the rim clears by 0.02 nm and the trio closes at the minimum. What the rim delivers instead is the identity — that 0.02 nm is the same at every row width, and that it is the whole of `ceil`'s gift.

---

## Sensitivities — what moves a verdict and what does not

| axis | range | closes? | worst misalignment | verdict moves? |
|---|---|---|---|---|
| **groove convention** | nominal 120° → wide 154° | **yes** | 0.11° | **no** |
| **lateral seat** | the crossbar's own axis only (`y_c = 0`) | **yes** | 13.22° | **no** |
| **lateral seat** | out to ±0.8 nm | **yes** | **0.00°** | **no** |
| **crossbar length** | `C-0048`'s own minimum, 13 bp | **yes** | 0.76° | **no** |
| **crossbar length** | 19 bp | **yes** | 0.53° | **no** |
| **twist reading** | square 10.67 → natural 10.5 bp/turn | **yes** | **0.00°** | **no** for the closure, **YES** for the leg length — 21 steps goes from 78.5° to **90.0°** |
| **leg length** (the quantisation) | 12 → 26 steps | — | budget 0.25°–89.8° | **no**: every length passes all nine predicates, at margins 1.81–2.43 / 1.36–1.83 |
| **cap azimuth** at a fixed leg | `C-0048`'s corner → the balanced optimum | — | — | **no, and it is a finding**: 1.95/1.46 → 2.20/1.66 |
| **`k_s`** (`C-0020`'s four decades) | unchanged from `C-0048` | — | — | `C-0048` reports 0.93/0.70 at `k_s`/32, on both rigidities. **Untouched here** |

> **The closure verdict does not move on any axis swept.** What moves is the *alignment* the search achieves, over a range that is worth at most `1 − cos²(13.2°) = 5.2 %` of a junction's couple, and it is the *single lateral seat* reading — the one place the search has no freedom to spend — that reaches it — and the leg-length quantisation, which is not a sensitivity but a design variable.

---

## Do `C-0048`'s and `C-0042`'s verdicts survive?

**Both do. One recommendation in `C-0048` does not, and it is a recommendation that improves when corrected.**

| upstream said | this claim finds |
|---|---|
| `C-0048`: *"whether three 90° junctions close on one 13 bp crossbar is open exactly as the pair was before `C-0042`"* | **they close**, at 13 bp and at every length to 19, with zero unpaired nucleotides and six distinct targets |
| `C-0048`: the cap is a crossbar of **13 bp = 4.38 nm** | **two different quantities.** 4.38 nm is the *demand* `w + 2R`; 13 bp is **4.42 nm** of duplex, and the difference is the whole rim clearance — 0.02 nm per end |
| `C-0048`: each base chord **along** the flexure axis, each cap chord **across** it | **not available at its own 7.00 nm leg.** The two chords differ by `m × 33.74°` and 21 steps gives 78.53° — [`CH-0067`](../challenges/CH-0067-a-leg-is-one-body-and-its-two-chords-are-not-independent.md) |
| `C-0048`: the cap azimuth is chosen *"because that is where `P6` binds"* | **chosen on the plane that does not govern.** The optimum balances loaded against free, and the constrained optimum at 21 steps is 2.20 / 1.66 against the recommended 1.95 / 1.46 |
| `C-0048`: the recommended design's `Sy7` row | **reproduced to ≤ 3.4e−9** through an independently written assembly — frame couple, span, tangent, supply, duty, both critical loads and both margins |
| `C-0048`: *"duty … 4.599 pN at 10 nm"* | its own result file says **4.59624**; the prose rounds it the wrong way. Recorded, not challenged |
| `C-0048`: the cap's height is one radius, the flexure butting its **side** | **upheld, and the side is where the clearance is spent**: the flexure's end passes a leg's surface at 0.249 nm |
| `C-0042`: the base chord comes out at **0.00°**, and the azimuth costs nothing | **upheld for one junction, and it is exactly the reason the constraint is on the difference.** `CH-0056` is completed, not contradicted |
| `C-0042`: `seatContactLength` excludes a rim seat | **upheld and extended**: on a finite seat the exclusion is needed in the axial direction too, and the minimum crossbar passes it by 0.02 nm |
| `C-0042`: seven base pairs of row | **untouched as a row pitch** — and it is the *tightest* row for the flexure's own entry, at 0.249 nm against 0.714 at 12 bp |

---

## The literature

| question | answer | flag |
|---|---|---|
| **What is a short Watson-Crick duplex worth, in magnesium, at this project's own salt?** | Ten measured nearest-neighbour energies from **−1.38 (TA/AT) to −2.74 (GC/CG)** kcal/mol at 298 K and a 1 M reference, with **per-motif** salt corrections `m` from 0.032 to 0.092 — and the paper's own headline is that a *homogeneous* correction is *"definitely incompatible"* with the data and that the unified set's assumption *"that the salt correction for the monovalent ions is exactly twice the correction for the divalent ones"* is disproved | **read directly** (Huguet, Ribezzi-Crivellari, Bizarro & Ritort, *Nucleic Acids Res.* **45**:12921, 2017; `PMC5728412`, article fetched and Table 1 read) |
| **And the form of that correction?** | *"a simple linear logarithmic dependency with salt concentration"* about *"the energy of motif at the reference condition = 1 M"*, and the direction is fixed by the paper's own Figure 1D, *"the mean unzipping force … increases with the concentration of Mg²⁺, demonstrating that divalent cations stabilize the duplex"*. **The equation itself is rendered as an IMAGE and its logarithm's base was not read**, so both conventions are carried and the pessimistic one is adopted | **read directly** for the text, **not found** for the equation |
| **The 1 M NaCl cross-check** | SantaLucia's unified parameters: ten steps from **−0.58 to −2.24** kcal/mol at 37 °C, average −1.42, initiation **+0.98** (terminal G·C) and **+1.03** (terminal A·T) | **read directly** (SantaLucia, *PNAS* **95**:1460, 1998; `PMC19045`, Tables 1 and 2 read verbatim) |
| **Is there a published crossbar hosting three perpendicular duplex junctions?** | **NOT FOUND**, over 15 queries here, on top of `C-0048`'s 10, `C-0042`'s 11, `C-0037`'s ~72 and `C-0029`'s ~110 | **not found** |
| **Is there a published rule relating a duplex's LENGTH to the relative azimuth of its two ends' junctions?** | **NOT FOUND.** The helical phase of an origami duplex is a standard design consideration for **crossover** placement — that is `C-0015`'s 32 bp lattice — but nothing was found stating it for the two **ends** of a free duplex carrying two 90° junctions. The positive statement here needs no source: it is arithmetic on `C-0029`'s counting theorem | **not found** |
| **How close do packed origami duplexes actually come, surface to surface?** | **0.69 nm** on a single-layer sheet and **0.54 nm** on a honeycomb, from the SAXS interhelical distances less the 2.00 nm diameter | **read directly**, via `C-0009` (Fischer et al., 2016) |

**Query strings recorded, so the negatives are falsifiable by one paper** (EuropePMC REST search, ~9 s apart):
`ABSTRACT:"DNA origami" AND ABSTRACT:"crossbar" AND ABSTRACT:"junction"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"three junctions"` (0);
`ABSTRACT:"DNA" AND ABSTRACT:"blunt end" AND ABSTRACT:"perpendicular" AND ABSTRACT:"duplex"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"short duplex" AND ABSTRACT:"stability" AND ABSTRACT:"melting"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"staple" AND ABSTRACT:"binding domain" AND ABSTRACT:"length"` (0);
`ABSTRACT:"DNA nanostructure" AND ABSTRACT:"13 base pair"` (0);
`ABSTRACT:"origami" AND ABSTRACT:"helical phase" AND ABSTRACT:"azimuth"` (0);
`ABSTRACT:"DNA" AND ABSTRACT:"duplex" AND ABSTRACT:"two junctions" AND ABSTRACT:"twist"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"helical twist" AND ABSTRACT:"design rule"` (0);
`ABSTRACT:"DNA" AND ABSTRACT:"duplex length" AND ABSTRACT:"rotational alignment"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"protruding duplex" AND ABSTRACT:"orientation"` (0);
`ABSTRACT:"DNA nanotechnology" AND ABSTRACT:"blunt-end" AND ABSTRACT:"stacking" AND ABSTRACT:"perpendicular"` (0);
`AUTH:"SantaLucia J" AND PUB_YEAR:1998 AND TITLE:"nearest-neighbor"` (**4** — the unified paper, read);
`TITLE:"unified view" AND TITLE:"nearest-neighbor"` (1 — the same paper);
`TITLE:"nearest-neighbor" AND ABSTRACT:"magnesium" AND ABSTRACT:"melting"` (**2** — Huguet et al., read).

> **One query failed with a `503` and is not counted**, which is `CLAUDE.md`'s own recorded gotcha about EuropePMC under rapid sequential querying: the unretried failure is an HTML error page that parses as a JSON error and reads exactly like a zero-hit result.

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** Two duplexes standing normal to a single-layer sheet under a shared crossbar carrying a flexure is not in the literature at any of its three storeys.
- **The closure test is `C-0029`'s and inherits its caveat three times over**: a phosphate pair inside the measured `[0.60, 0.70]` nm step with no van der Waals overlap. **No backbone torsion angle is checked and no sequence is designed.** A *"closes"* verdict is an **upper bound on buildability**; only a *"does not close"* verdict would be a proof of impossibility. `T-71`.
- **The chord-twist quantisation inherits none of that.** It needs only `C-0029`'s count — a duplex end has two termini — and the fact that a helix twists. It is arithmetic, and no force field can overturn it.
- **It is carried on TWO twist readings and the recommended lengths are the ones that survive both.** A free-standing leg carries no crossovers, so the square lattice's 10.67 bp/turn is a *convention borrowed from the sheet*; 10.5 is the other end. Nothing here decides which, and 21 steps fails on both.
- **The `LOCKED` search is a RESTRICTED question and is reported as one.** It fixes the leg's own rotation at the value that puts the base chord *exactly* on the flexure axis — which the design does not want, because the optimum splits the budget between the two ends — and it fixes the crossbar at 15 bp, the row at 7 bp and the twist at 10.67 bp/turn. Its one failure, at 13 steps, is a failure at one point of that restricted space and not a refusal. The `FREE` search, which is the acceptance question, is convention-swept and unaffected.
- **The leg's rotation sweep is over a base misalignment of `[0, π/4]`**, because past a half right angle a two-link base's two axes exchange and `C-0037`'s `TwoLinkBase` invariant cannot represent it. A declared modelling boundary, and the returned optima are all interior to it.
- **The bodies are hard cylinders with flat end faces.** A real duplex has grooves and a real end face has a rim, so the 0.249 nm leg-to-flexure clearance is a **model** number — and it is the one number here that a better body model could turn negative.
- **The crossbar is an ideal duplex of the same rise, radius and twist as every other duplex here.** Its own **end fraying** is not modelled, and a real 13-mer frays from both ends — which is the same 0.02 nm rim the geometry has no margin at.
- **The free energies are formation free energies at a 1 M standard state**, so their initiation term stands in for a bimolecular association the tethered crossbar does not have to pay. A covalently routed crossbar is therefore **more** stable than the quoted number, and the number is used as a bound and a threshold, never as a probability.
- **The base of the logarithm in the magnesium salt correction was not read** and both conventions are carried, a factor of 2.30 in the correction. The verdict is taken on the pessimistic one.
- **The search is a grid and its optimum is A solution, not THE solution.** Every grid was refined and the verdict did not move, but a grid cannot prove a non-existence finer than itself — which matters only if a future task needs a *negative* here, and this one does not.
- **The two legs are taken to be the same length in base pairs**; legs of different lengths would tilt the crossbar and are not modelled.
- **The flexure butts the crossbar's SIDE at the crossbar's own axis height**, which is `C-0048`'s `e = R`. A flexure sitting **on** the crossbar would double it.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction**, and every junction constant here rests on it, exactly as in `C-0028`, `C-0029`, `C-0037`, `C-0042` and `C-0048`.
- **SMALL DEFLECTION**, exactly as every claim in this chain flags.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| phosphate radius | 1.00 nm | **CITED, READ DIRECTLY** (Hedley et al., *Phys. Rev. X* **14**:031042, 2024) via `C-0029` |
| intrastrand phosphodiester step | **0.60–0.70 nm** | **CITED, MEASURED** (Bosco et al., *NAR* **42**:2064, 2014) via `C-0029`. A **window** |
| duplex steric radius | 1.00 nm | **CITED**, the standard 2 nm diameter |
| rise per base pair | 0.34 nm | **CITED** (Douglas et al., 2009) |
| base pairs per turn | 10.67 square, 10.5 | **CITED**; both carried, and the verdict is taken on both |
| interhelical distance | 2.69 nm single-layer, 2.54 honeycomb | **CITED, MEASURED** by SAXS (Fischer et al., 2016) via `C-0009` |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012) |
| Fields et al.'s implied rigidity | 172.9 pN·nm² | **CITED, MEASURED** (*NAR* **41**:9881, 2013) |
| `k_bond,θ` | 6.765 pN·nm/rad | **CITED+FITTED** (Chen et al., *JACS* **136**:6995, 2014) via `C-0009` |
| `k_bond,s` | 32.35 pN/nm | **DERIVED** (`C-0020`), **NOT measured** |
| unified nearest-neighbour `ΔG°₃₇` | ten steps, −0.58 to −2.24 kcal/mol | **CITED, READ DIRECTLY** (SantaLucia, *PNAS* **95**:1460, 1998, Table 1, `PMC19045`) |
| magnesium nearest-neighbour `ΔG` and salt factors | ten steps, −1.38 to −2.74 kcal/mol; `m` 0.032–0.092 | **CITED, READ DIRECTLY** (Huguet et al., *NAR* **45**:12921, 2017, Table 1, `PMC5728412`) |
| the **base** of the logarithm in that salt correction | — | **NOT READ** — the equation is an image; both conventions carried and the pessimistic one adopted |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the truncated seat contact, the lone seat's face height, the convex-body clearance and its alternating projection, the chord-twist quantisation and its conservation, the three-junction closure search and every placement it returns, the quantised design table and every span, tangent, supply ratio, duty, critical load, margin and verdict — is **derived here in code**, with `C-0029`'s, `C-0042`'s and `C-0048`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The backbone torsion of the trio.** `T-71`, now with **three** junctions on one seat and six links, and at the C2′-endo end of the window where `C-0042` records the check is least comfortable.
2. **The flexure's own two ends.** The same quantisation applies to them and their length is an **output** of the placement condition, not a free choice; at `C-0048`'s solved span the nearest base-pair count leaves 44.2° of budget, and half a base pair of span buys any azimuth at a ~2 % placement error. Reported here, not resolved.
3. **The leg-to-flexure clearance under a better body model.** 0.249 nm at the recommended 7 bp row is tighter than any packed origami, and the row width is what buys it back.
4. **Whether the plan view admits 180 legs, 90 crossbars and 45 flexures** with the overhangs a stable crossbar wants. `T-96` / `T-116`.
5. **`k_s`.** `T-9`, unchanged and still verdict-critical at `k_s`/32.

## Challenges

**Raises [`CH-0067`](../challenges/CH-0067-a-leg-is-one-body-and-its-two-chords-are-not-independent.md)** against `C-0048`'s recommended azimuth pair and its treatment of the leg length as a continuum. **No number of `C-0048`'s or `C-0042`'s fails to reproduce** — 17 reproductions at ≤ 3.4e−9, including the whole `Sy7` design row and the `Sx7` corner.

**None stands against this claim.** The four ways it would fail:

1. **A backbone-torsion check showing the closed routing does not close.** `T-71`, now with three junctions on one seat, and `C-0042` already records that the aligned pair sits at the C2′-endo end of the window where that check is least comfortable. It can only make the answer worse.
2. **A steric model finer than a cylinder of radius 1.0 nm with a flat end face.** The leg-to-flexure clearance is 0.249 nm at the recommended row; grooves and a real rim could take it either way, and it is the one number here that a better body model could turn negative.
3. **A leg that is not one rigid duplex**, which would release the chord-twist constraint. `C-0029` rules out the nicked continuation; anything else is a new joint in the leg's own axis and a new compliance nobody has priced.
4. **A measurement of `k_s` more than ~30× below `C-0020`'s construction**, which `C-0048` already reports takes the margin to 0.93 / 0.70 — unchanged here, and untouched by anything in this claim.
