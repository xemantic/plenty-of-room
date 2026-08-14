# C-0070 — The leg's own length budget DOES survive the pinned base, and the shared-length clause costs **nothing**: `C-0065`'s phase-class congruence carries from the station to the leg bases, so *"one leg length for 34 instances"* collapses to *"one leg length for the two legs of one truss"* — and what the pinning actually does is make the base **overspend** `C-0052`'s budget by 1.80× rather than spend it exactly, which changes no verdict because **every one of the 15 leg lengths in the envelope passes all nine predicates**; the buildable design is the **9 bp row at a 12-step, 4.08 nm leg**, carrying **2.443 / 1.836** against the row the array cannot build at 2.446, and **17 of 44** trios still survive every clause at once

| | |
|---|---|
| **Task** | [`T-132`](../tasks/T-132.md), named by [`C-0065`](C-0065-crossbar-array-placement.md) as its own open item 1 and by [`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md) as *"what would settle it"* item 2 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the truss belongs to |
| **Verification type** | **logical** (two closed-form cheap bounds — a phase-class count that decides the size of the problem, and an exact chord-difference floor that no leg length can beat) **+ in-silico** (`C-0059`'s single-junction feasible set and `C-0062`'s pruned closure verdict re-run as a register of **signed** chord azimuths, composed with `C-0052`'s `chordPairMisalignment` and `C-0048`'s `capDesign` pipeline) |
| **Verdict** | **PASS, and the answer to the acceptance question is YES — the budget survives, at one leg length for all 34 instances, and the shared-length clause is free.** Cheap bound 1 decides the shape: `C-0065`'s finding that every station is the same helical phase class of its **own** host duplex carries to the **68 leg bases**, which fall into exactly **2 classes of 34** presenting **1** distinct `(low leg, high leg)` pair at every row pitch — so one length serves 34 instances exactly when it serves **two legs**, and the declared falsifier did not fire. What pinning the base actually costs is a different thing, and it is new: a free rotation always spends `C-0052`'s budget **exactly**, while a pinned one can only **overspend** it, because the sign of the pinned deviation need not oppose the budget's own sense — at the recommended design the two ends spend **81.13°** of a **45.13°** budget, **1.80×**. **And nothing fails**: at the 9 bp row **all 15** leg lengths of `C-0052`'s 12–26 step envelope are representable **and pass all nine predicates**, at margins **1.815–2.443** on CanDo's rigidity and **1.364–1.836** on Fields et al.'s. The single best shared length is **12 steps (4.08 nm)** at **every** surviving row, and at the 9 bp row it carries **2.443 / 1.836** — **better** than `C-0062`'s own 2.410 / 1.812 at that row, because a worse cap chord moves couple into the plane that governs (`C-0052`'s balance finding, in a new place), and **0.13 % below** the 2.446 of the 10 bp row the array **cannot** build. **`C-0065`'s 17 of 44 is unchanged, and so is its recommended 9 bp row at 18.0°** — the pinned composition removes **nothing**, which is the direction `C-0065` predicted but not the reason it gave. One genuinely new constraint appears and does not bind here: **a truss has TWO legs and the register pins them at two DIFFERENT azimuths** (9.0° and 18.0° at the 9 bp row), so their cap chords differ by that same folded angle at **every** leg length, imposing a cap floor of **`|fold(δ_A − δ_B)|/2`** that no length can beat — **4.5°** at the 9 bp row against `C-0062`'s 24.0° chemistry floor, but **28.5°** at the 6 and 10 bp rows against its 9.0° and 27.0° ([`CH-0082`](../challenges/CH-0082-a-truss-has-two-legs-and-the-design-table-gives-it-one-base.md)). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** A torsion closure is a **necessary** condition and never a sufficient one (`C-0029`, `C-0052`, `C-0057`, `C-0059`, `C-0062`, `C-0065`), so every *"closes"* and every *"passes"* here is an **upper bound on buildability** — and this claim's headline is a positive, which is the weak direction. The cap floor is imposed as a floor and not as a joint search, which bounds the design from the **favourable** side. The motif is unchanged and undemonstrated: no free lever has been built on a single-layer sheet at one crossover (`C-0055`). |
| **Provenance** | `gpd/results/T-132-pinned-leg-budget.json`, produced by `anchoring.PinnedLegBudgetStudyKt`; model in `src/main/kotlin/anchoring/PinnedLegBudget.kt`; **5 cheap bounds, 7 phase-class censuses, 7 register records over 89 axial positions and 255 junction solves, 7 row records carrying three compositions each, 105 per-length design records, 44 array re-judgements, 6 sensitivities, 4 convergence records, 23 upstream reproductions, 3 budget records, 5 predicates**; **24 gate-named tests in `src/test/kotlin/anchoring/PinnedLegBudgetTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 12 m 10 s — the whole suite, on its own isolated tree**, with a sibling agent's mid-TDD `anchoring/OutputElementPlacementTest.kt` dropped by `--drop-file` (it does not compile in the working tree and nothing here touches it); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, 10.67 bp/turn, crossover phase **24** — `C-0063`'s own host, its 34 stations read from `gpd/results/T-125-*.json`; the 44 trios and the per-row cap and flexure floors from `gpd/results/T-127-*.json`; `C-0059`'s base floors from `gpd/results/T-124-*.json`; `C-0065`'s register table from `gpd/results/T-130-*.json`; junction geometry `C-0029`'s via `C-0059`'s `SingleJunctionFeasibleSet` — phosphate radius **1.00 nm**, minor groove **120°**, the inherited **[0.60, 0.70] nm** window; closure `C-0062`'s per-assignment pruned verdict at `C-0059`'s own **60-step / 4-refinement** grid; register grid **0.17 nm** (2 steps per base pair) over **±22 bp**, **4** candidates per position, with **every** closer retained and its chord's **sign** kept; leg envelope `C-0052`'s **12–26** base-pair steps at 10.67 bp/turn, 10.5 swept; mechanics `C-0048`'s pipeline at its own **45** load paths, `EI = 230 pN·nm²` (CanDo) with every critical load also on Fields et al.'s implied **172.906 pN·nm²** |
| **Consumes** | [`C-0065`](C-0065-crossbar-array-placement.md) (`BaseRegisterField`, `TrussStation`, `stationPhaseClassCensus`, `distinctSheetTargets`, `EAST_SITE_BASE_PAIRS`, `ROW_AZIMUTH_OFFSET_BASE_PAIRS`; its seven register rows **consumed as data** and reproduced at departure **0**), [`C-0062`](C-0062-crossbar-trio-existence.md) (`junctionClosesOnSomeAssignment`, its 44 trios and its per-row cap and flexure floors **consumed as data**, its design table reproduced), [`C-0059`](C-0059-torsion-feasible-routing.md) (`SingleJunctionFeasibleSet`, `junctionLinks`, `feasibleTrussDesign`, `legBudgetDegrees`, its base floors **consumed as data**), [`C-0052`](C-0052-crossbar-junction-trio.md) (`relativeChordAzimuth`, **`chordPairMisalignment`**, `legAzimuthSplit`, `capDesign`), [`C-0048`](C-0048-truss-cap.md) (`capBendingStiffness`, `capTorsionalStiffness`, `SolvedTrussCap`), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`foldedChordMisalignment`, `chordBaseAxes`, `couplePhaseProjection`), [`C-0037`](C-0037-triangulated-standoff.md) (`TwoLinkBase`, `TrussLayout`, `LegOffset` and the half-right-angle invariant), [`C-0029`](C-0029-perpendicular-junction-routing.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0082`](../challenges/CH-0082-a-truss-has-two-legs-and-the-design-table-gives-it-one-base.md), against `C-0062`'s and `C-0059`'s design tables — they carry **one** base misalignment per truss, and a truss has **two** legs pinned at two different azimuths of one duplex |

---

## The claim, in one line

**`C-0065` left the leg length as its open item and said the composition *"can only tighten the verdict"*; it does not tighten it at all, and the reason is worth more than the tightening would have been — the array clause is free by the same congruence that made the register one question rather than thirty-four, the pinned base **overspends** `C-0052`'s quantised budget by 1.80× instead of spending it exactly, and the design does not care, because every leg length in the envelope passes and the leg's length was never the binding constraint once the base is pinned.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, couples **pN·nm/rad**, angles **degrees** in every reported number and **radians** in code; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive upward**. Origin at the tile centre.
- **A chord is a line**, so every chord azimuth folds modulo `π`; a **misalignment** is the folded distance and lies in `[0°, 90°]`, a **signed deviation** `δ` in `[−90°, +90°]`. The sign is the whole of what this task adds to `C-0065`: a free rotation can move a chord either way and only its magnitude matters; a **pinned** base cannot.
- The **base** chord wants the flexure's own axis (`C-0042`'s and `C-0052`'s `wantedChordAzimuth = π/2`) and the **cap** chord wants **across** it, so the wanted separation is **90°** — `C-0048`'s own demand, unchanged.
- The twist is the square lattice's designed **10.67 bp/turn**, `τ = 33.7394°` per base-pair step; **10.5 bp/turn** is swept.
- **One truss is two legs on ONE host duplex**, at the register centre ∓ `w/2` base pairs, under one crossbar. Both legs are the **same length in base pairs** — legs of different lengths tilt the crossbar, which `C-0052` excludes. 34 trusses are **68 leg bases**.
- **A register offset is signed and measured from the station**, positive toward `+x` — `C-0065`'s convention verbatim.

---

## The five cheap bounds, which ran first — and the first is the method

| | bound | value | against | ratio | what it settled |
|---|---|---|---|---|---|
| **1** | the distinct `(low leg, high leg)` phase-class pairs the 34 trusses present | **1** | 1 | **1.000** | **the shape of the whole answer.** One leg length serves 34 instances exactly when it serves **two legs**; the array clause costs nothing |
| **2** | the phase classes the 68 leg bases occupy | **2** | 2 | 1.000 | two classes of **34** each, at every row pitch — `C-0065`'s bound 3 carried from the station to the leg bases |
| **3** | the cap misalignment no leg length can beat at the 9 bp row, `\|fold(δ_A − δ_B)\|/2` | **4.5°** | 24.0° | **0.188** | the two legs' own chord split is a floor on the **worst** cap, exact and independent of the length — and here it is below `C-0062`'s chemistry cap floor, so the two legs are not what binds |
| **4** | how coarsely the 12–26 step envelope samples the chord circle | **22.44°** | 90° | 0.249 | the widest gap between consecutive relative chord azimuths, so the best achievable worst cap is within **11.22°** of bound 3 |
| **5** | the height spread a **per-instance** leg length would open across the array | **4.76 nm** | 3.0 nm | **1.587** | what the alternative costs if the shared length ever fails — **not staples**, but coplanarity |

> **Bound 1 is not a bound, it is the method, and it is `C-0065`'s bound 3 one storey down.** A leg base is a station displaced by the register's centre offset and then by ∓ half the row pitch **along the same duplex**, so if the stations are one phase class the leg bases are two, one per leg. The declared falsifier — more than two classes, or two classes of unequal population, either of which would mean no single length can serve the array — **did not fire at any of the seven row pitches**.
>
> **Bound 3 is the one nobody had.** `C-0062` and `C-0059` give a truss **one** base misalignment. The register gives it **two**, and their difference is invariant under the leg length because the length rotates both cap chords by the same `m τ`. It is a floor by the triangle inequality on the folded line metric, and it is verified against the search rather than asserted: at the 9 bp row the bound is 4.5°, the search returns **6.95°**, and bound 3 + half of bound 4 is 15.72°.

---

## Deliverable 1 — the register, with the sign kept and both legs separated

`C-0065`'s own 89-position window at 0.17 nm steps, **255 junction solves**, with every closing candidate retained rather than only the best-aligned winner.

| row [bp] | offset | **low leg** | **high leg** | `C-0065` publishes | closers (low + high) | admissible pairs | **two-leg cap floor** |
|---|---|---|---|---|---|---|---|
| 6 | +3.91 nm | **−33.0°** | **+24.0°** | 33.0° | 1 + 1 | 1 | **28.5°** |
| 7 | +3.40 | +57.0 | +69.0 | 69.0 | 1 + 1 | 1 | 6.0 |
| 8 | +3.06 | −18.0 | −66.0 | 66.0 | 2 + 1 | 2 | 24.0 |
| **9** | **+0.17** | **−9.0** | **−18.0** | **18.0** | **3 + 2** | **6** | **4.5** |
| 10 | +0.51 | 0.0 | +57.0 | 57.0 | 2 + 1 | 2 | **28.5** |
| 11 | +1.02 | +6.0 | −33.0 | 33.0 | 1 + 1 | 1 | 19.5 |
| 12 | +0.85 | 0.0 | −33.0 | 33.0 | 2 + 1 | 2 | 16.5 |

**Every offset and every published misalignment reproduces `C-0065` at departure 0** — fourteen numbers, exact — because the candidate ranking is its ranking and the winner is its winner. What is new is the second column of each pair: **`C-0065`'s number is the worse of two legs**, and the pair is what a leg length has to serve.

---

## Deliverable 2 — the three compositions of the same row, side by side

`C-0062`'s free-floor table, `CH-0078`'s pinned **floor** with the rotation still free, and this task's fully **pinned** composition at one shared leg length.

| row [bp] | `C-0059` floor | `C-0062` cap floor | pinned base | **`C-0062`**: steps, CanDo/Fields | **`CH-0078`**: steps, CanDo/Fields | **pinned**: steps, CanDo/Fields | flat? | survives? |
|---|---|---|---|---|---|---|---|---|
| 6 | 33.0° | 9.0° | 33.0° | 15, 2.325 / 1.748 | 15, 2.325 / 1.748 | **12, 2.353 / 1.769** | **no** | no |
| 7 | 69.0 | 21.0 | 69.0 | — not representable | — | — | no | no |
| 8 | 57.0 | 24.0 | **66.0** | — | — | — | no | no |
| **9** | **6.0** | 24.0 | **18.0** | 12, 2.410 / 1.812 | 15, 2.439 / 1.834 | **12, 2.443 / 1.836** | **yes** | **YES** |
| 10 | 6.0 | 27.0 | **57.0** | **12, 2.446 / 1.839** | — not representable | — not representable | yes | no |
| 11 | 33.0 | 24.0 | 33.0 | 15, 2.354 / 1.770 | 15, 2.354 / 1.770 | **12, 2.380 / 1.789** | yes | **YES** |
| 12 | 33.0 | 27.0 | 33.0 | 15, 2.172 / 1.633 | 15, 2.172 / 1.633 | **12, 2.215 / 1.665** | yes | **YES** |

Four things fall out, and the third is the one that decides the task.

1. **`CH-0078` is upheld and nothing softens it.** The 10 bp row `C-0062` recommends is not representable at **any** shared leg length, because 57.0° is past the 45° at which `C-0037`'s `TwoLinkBase` invariant exchanges its axes; the 7 and 8 bp rows are likewise gone. `CH-0078`'s *"what would settle it"* item 2 is discharged, and the verdict it asked for is the one it predicted.
2. **The pinned composition is not worse than the free one — it is marginally better at every representable row.** 2.443 against 2.410 at the 9 bp row, 2.380 against 2.354 at 11, 2.215 against 2.172 at 12. That is `C-0052`'s balance finding in a new place: a worse cap chord moves couple out of the plane that does not govern and into the one that does, and the margin is judged on the minimum of the two.
3. **The best shared leg length is 12 steps — 4.08 nm — at every surviving row**, and it is the same length `C-0062` chose for its own 10 bp design. The row an array can build and the row it cannot want the **same leg**.
4. **The row the array can build carries 2.443 against the 2.446 of the row it cannot** — a **0.13 %** difference on CanDo's rigidity and 0.13 % on Fields et al.'s. Pinning the base moved the design table's recommended row and cost essentially nothing in stability.

---

## Deliverable 3 — what the pinning actually costs: the budget is OVERSPENT, not spent

`C-0052`'s conservation is that rotating a leg about its own axis trades the two misalignments one for one, so `ψ_base + ψ_cap` equals the quantised budget `|m τ − 90°|` **exactly**, for every rotation in the reducing sense. A **pinned** base has no rotation to choose, and its deviation need not be in the reducing sense:

&nbsp;&nbsp;&nbsp;&nbsp;**`ψ_base + ψ_cap ≥ |m τ − 90°|`, always, with equality only when the pinned deviation opposes the budget's own sense.**

Asserted as a gate-3 test at every leg length and 61 pinned deviations, together with the exact-equality condition.

At the recommended design — 9 bp row, 12 steps — the budget is **45.13°** and the two ends spend **18.0° + 63.13° = 81.13°**, an **overspend of 36.0°** and a ratio of **1.80×**. Over the whole envelope at that row the overspend runs **0.0°–36.0°**: `C-0052`'s budget is honoured exactly at **seven** of the fifteen lengths — 14, 15, 16, 20, 21, 25 and 26 steps — and overspent at the other eight.

**And it does not matter.** The whole 15-length envelope is representable and passes:

| steps | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25 | 26 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| leg [nm] | **4.08** | 4.42 | 4.76 | 5.10 | 5.44 | 5.78 | 6.12 | 6.46 | 6.80 | 7.14 | 7.48 | 7.82 | 8.16 | 8.50 | 8.84 |
| budget [°] | 45.1 | 11.4 | 22.4 | 56.1 | 89.8 | 56.4 | 22.7 | 11.1 | 44.8 | 78.5 | 67.7 | 34.0 | 0.25 | 33.5 | 67.2 |
| **cap [°]** | **63.1** | 29.4 | 24.0 | 47.1 | 80.8 | 74.4 | 40.7 | 24.0 | 35.8 | 69.5 | 85.7 | 52.0 | 24.0 | 24.5 | 58.2 |
| overspend [°] | 36.0 | 36.0 | 0.0 | 0.0 | 0.0 | 36.0 | 36.0 | 13.9 | 0.0 | 0.0 | 36.0 | 36.0 | 36.0 | 0.0 | 0.0 |
| **margin CanDo** | **2.443** | 2.280 | 2.225 | 2.399 | 2.068 | 2.165 | 2.277 | 2.127 | 2.159 | 2.150 | 1.861 | 2.038 | 1.870 | **1.815** | 1.854 |
| margin Fields | **1.836** | 1.714 | 1.673 | 1.803 | 1.555 | 1.627 | 1.712 | 1.599 | 1.623 | 1.617 | 1.399 | 1.532 | 1.406 | **1.364** | 1.394 |
| governing plane | free | loaded | loaded | loaded | free | free | loaded | loaded | loaded | free | free | loaded | loaded | loaded | loaded |

**Fifteen of fifteen pass all nine predicates.** The margin varies by only **1.35×** across the whole envelope on either rigidity, and the base misalignment — which is what the frame couple's `cos²ψ` actually reads — is **18.0° at every one of them**, because the register fixes it and the length cannot move it. **The leg's length was the binding variable while the base was free; once the base is pinned, the leg's length is the free one.**

---

## Deliverable 4 — the mechanics, re-quoted at the geometry that moved

`C-0048`'s cap terms carry the **row**, not the crossbar's length, and the row did not change — so they are unchanged, and they are re-derived rather than carried:

| | 9 bp row (`w = 3.06 nm`) | 10 bp row (`w = 3.40 nm`) |
|---|---|---|
| cap **bending**, `12EI/w` | **901.96 pN/nm** | 811.76 (reproduced from `C-0048` at 1.2e−7) |
| cap **torsion**, `4C/w` | **601.31 pN·nm/rad** | 541.18 (reproduced at 4.1e−7) |
| `C-0037`'s **frame couple** at the recommended design | **117.61 pN·nm/rad** | — |
| span | 30.10 nm | — |
| duty at §3's **desired** 10 nm | 7.06 pN | — |
| critical load, CanDo `EI = 230 pN·nm²` | **17.25 pN** | — |
| critical load, Fields et al.'s implied 172.906 | **12.97 pN** | — |
| **margin** | **2.443 / 1.836** | — |

`C-0037`'s frame-couple conservation — `Σx_i² = (w²/2)cos²θ`, `Σy_i² = (w²/2)sin²θ`, **`Σx² + Σy² = w²/2` identically** — is asserted as a gate-3 test at thirteen azimuths of a two-leg row. It contains no base azimuth and no leg length, so nothing in this task can move it; it is re-checked because the claim quotes a frame couple.

**`C-0062`'s `cos²ψ` observation is confirmed and is why so little moves.** The base misalignment goes 6.0° → 18.0° at the 9 bp row, worth `1 − cos²(18°) = 9.5 %` of the base couple; the cap goes 24.0° → 63.1°. The margin moves from 2.410 to 2.443. **The question the pinning decides is representability, not degradation** — exactly as `CH-0078` says — and 45° is the only threshold in the composition that is not a smooth trade.

---

## Deliverable 5 — the 44 trios, re-judged

| clause | trios surviving |
|---|---|
| recorded by `C-0062` | **44** |
| … placing 34 times at one level (`C-0065`) | 44 |
| … closing on `C-0057`'s own verdict grid | 39 |
| … carrying a base `C-0037` can represent at the centre the register offers | 24 |
| … leaving the tile flat at `T-5b`'s 0.10 | **17** |
| … **and carrying a shared leg length that passes all nine predicates** | **17** |

**The shared-length clause removes nothing.** The three surviving rows — **9, 11 and 12 bp** — each carry a passing design at 12 steps, and the rows the clause would have removed had already failed the base or the flatness. `C-0065`'s *"it can only tighten the verdict"* is upheld in direction and refuted in effect: the tightening is zero.

---

## Deliverable 6 — what a per-instance leg length would cost, priced and not needed

The shared length does not fail, so this is a contingency and is reported as one. It is **not** a staple-count problem: the 68 leg bases sit at 68 distinct scaffold positions, so the legs are **already** 68 distinct oligos and per-instance lengths add **no species** to the pool. What it costs is **height**:

- the 12–26 step envelope spans **4.76 nm**, which is **1.59×** §3's acceptable 3 nm stroke and 48 % of the 10 nm gap, so 34 caps at freely chosen lengths are not the **one plane** `C-0053` and `C-0063` place the output elements in;
- within a truss it is worse — two legs of different lengths **tilt the crossbar**, which `C-0052` excludes explicitly;
- and `C-0065`'s single-level packing verdict is written on identical blocks.

So the honest statement is that a per-instance leg length is a **planarity** cost, not an assembly-complexity one, and it is not incurred.

---

## Sensitivities — one moves the verdict

| axis | reading | admissible pairs | best steps | cap | margin CanDo | passing lengths | verdict moves? |
|---|---|---|---|---|---|---|---|
| **reference** | `C-0029`'s geometry, 120° groove, `r_P` = 1.00 nm, seat 0.0, cap 4, 10.67 bp/turn | 6 | **12** | 63.1° | **2.443** | 15 | — |
| candidate cap | **12** per position rather than `C-0059`'s 4 | 6 | 12 | 63.1° | 2.443 | 15 | **no** |
| **lateral seat** | the legs seated **0.5 nm** off the host's axis | **1** | — | — | — | **0** | **YES** |
| the **leg's** own twist | **10.5 bp/turn** — a free-standing leg carries no crossovers | 6 | 12 | 56.6° | 2.449 | 15 | **no** |
| leg envelope | **8–40** steps rather than `C-0052`'s 12–26 | 6 | 12 | 63.1° | 2.443 | **16** | **no** |
| `C-0062`'s cap floor | **removed entirely** — the crossbar closes at any azimuth | 6 | 12 | 63.1° | 2.443 | 15 | **no** |

> **The lateral seat is the one axis that moves the verdict, and it is reported as such.** At a 0.5 nm seat the 9 bp row's register offers **one** admissible pair and its pinned base is past the half right angle, so **no** leg length is representable. `C-0065` swept the same axis and found it moved the *register* (11 closing positions, one centre at −0.85 nm) without moving its *placement* verdict; the pinned design verdict is more fragile than the placement verdict, and that is the honest reading. **The headline is at `lateralSeat = 0`, which is a choice and not a constraint** — `C-0059` sweeps the seat as a free variable and an array pins nothing about it, so a design would choose the seat that registers. Nothing here establishes that a 0.5 nm seat is required or forbidden.
>
> **`C-0062`'s cap floor turns out not to bind at all.** Removing it entirely changes nothing, because the pinned geometry already demands 63.1° at the best length — the *chemistry* floor of 24.0° is below what the *arithmetic* asks for. The conservative independence assumption this composition inherits is therefore not load-bearing at the design point.

---

## The five verification gates

Executed as **24 gate-named tests** in `src/test/kotlin/anchoring/PinnedLegBudgetTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a signed chord deviation is a line coordinate in `[−90°, 90°]` and its **magnitude is `C-0042`'s folded misalignment** at 81 azimuths; adding half a turn to any chord changes nothing, compared as a folded difference because the two ends of the fold interval are the same line; the two-leg cap floor is symmetric in its arguments, lands in `[0°, 45°]` over 1681 pairs, and is exactly zero for equal deviations; unphysical arguments throw at **eight** entry points | **PASS** |
| **2 — limiting cases** | **the two free limiting cases `T-132` declared.** An **unpinned** base (`δ = 0`) reproduces `C-0052`'s `chordPairMisalignment(m)` as the cap misalignment at **every** leg length, with the budget spent exactly and zero overspend; the unpinned composition reproduces **`C-0062`'s own design table** — 12 steps, 2.44608 / 1.83888, budget 45.126523 — through this file's own pipeline; and **one instance reproduces `C-0065`'s register**, offset +0.17 nm and 18.0° at the 9 bp row, with the two legs separated at 9.0° and 18.0°. Plus: the register's winner **is** `BaseRegisterField`'s winner, position for position; a leg with no admissible base has no design; a base past 45° is not representable at any length | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the pinned base misalignment at the 9 bp row is **unchanged** at 2 and 4 axial steps per base pair and at 120 and 240 azimuth steps (departure 0.0 on both); the register is **memoised and deterministic** — a repeat traversal costs zero further solves and returns the same field; a wider candidate cap never loses a closing position; the leg envelope's chord sampling is 22.4367° and a wider envelope samples more finely; **cheap bound 2 is verified against the search** (4.5° ≤ 6.95° ≤ 15.72°); and the result file is **byte-for-byte identical** on two independent `tools/study.sh` runs | **PASS** |
| **5 — literature and upstream** | **23 reproductions, worst departure `3.1e−2`**, which is `C-0052`'s own published rounding of 89.8313 to 89.8: `C-0052`'s budgets at 21, 24 and 16 steps; **all fourteen of `C-0065`'s register numbers at departure 0**; `C-0062`'s 2.44608 / 1.83888 and its 12-step best at departure 0; `C-0059`'s 6.0° floor at the 10 bp row exactly; `C-0048`'s `12EI/w` and `4C/w` at ≤ 4.1e−7 | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **A pinned base can only OVERSPEND the budget**, `ψ_base + ψ_cap ≥ |m τ − 90°|`, asserted at every leg length and 61 pinned deviations — and **equality holds for exactly one of the two signs at every length**, asserted as a count of 15.
2. **The two legs' cap chords differ by a constant independent of the leg length** — asserted at every length on the **signed** cap deviations, because the folded magnitudes do not carry it. This is cheap bound 3, as an identity.
3. **`C-0037`'s frame couple is a rank-one tensor**: `Σx² + Σy² = w²/2` identically over thirteen azimuths of the two-leg row.
4. **A chord is a line**: a half turn of either base leaves the base misalignment, the cap misalignment and the critical load unchanged at every length.
5. **Cheap bound 2 holds at the register's own pinned pair**, not only at synthetic deviations — the 9 bp row's 4.5° floor against a search minimum of 6.95°, inside the half-spacing bracket.

---

## Validity range

- **TRL 1–3, and this claim's headline is a positive**, which is the weak direction. A torsion closure is **necessary** and never sufficient; nothing is measured, no sequence is designed, no assembly is demonstrated, and the motif is undemonstrated exactly as `C-0055` and `C-0029` leave it.
- **`C-0065`'s, `C-0062`'s, `C-0059`'s, `C-0057`'s and `C-0029`'s whole validity ranges are inherited**, including the rigid-residue model, the **marginal** occupancy test and the inherited `[0.60, 0.70] nm` window against `C-0057`'s measured 0.607 / 0.664.
- **The cap floor is imposed as `max(cap_geometric, capFloor)`** — `C-0062`'s own device and its own independence assumption, which presumes the crossbar can be arranged to close at **any** misalignment at or above its floor. That bounds the design from the **favourable** side. It happens not to bind at the design point (the sensitivity removing it changes nothing), which limits the exposure but does not remove it.
- **The base is pinned to the register's closing candidates at ONE centre — the one nearest the station.** A design that accepted a larger translation could reach other centres, at `C-0065`'s flatness and rim cost; that trade is `C-0065`'s and is not re-opened here.
- **The headline is at `lateralSeat = 0`, and that axis moves the verdict.** At 0.5 nm the 9 bp row is not representable at any leg length. The seat is a design choice `C-0059` sweeps freely, so this is a statement about which seat a design must choose, not a failure — but it is the one axis on which this claim is fragile.
- **The mechanics is `C-0048`'s pipeline at its own 45 load paths**, while the array `C-0063` places has **34** stations and **68** leg bases. That inconsistency is inherited from `C-0062` unchanged and is not resolved here; `C-0065` reports the flexure at both counts and finds the span moves 3.4 %.
- **The leg envelope is `C-0052`'s 12–26 steps.** Widening it to 8–40 adds one passing length and does not move the optimum.
- **The register's *count* is not grid-converged and is not meant to be** — it is a measure on a continuum, exactly as `C-0062`'s closing count and `C-0065`'s register are. What the composition needs is the **nearest** centre and its azimuths, and those are unchanged at both axial and both azimuth grids.
- **The per-position candidate cap is `C-0059`'s 4 and a cap is a RANKING**, not monotone under refinement; at 12 candidates nothing moves here.
- **Both legs are the same length in base pairs and the crossbar is level** — `C-0052`'s assumption, and the whole reason *"one leg length"* is the right question.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction**, and every junction constant here rests on it, exactly as in `C-0028`, `C-0029`, `C-0037`, `C-0042`, `C-0048`, `C-0052` and `C-0062`. `C-0048` reports the margin falling to 0.93 / 0.70 at `k_s`/32.
- **SMALL DEFLECTION**, as every claim in this chain flags.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 34 stations and their host phase | phase 24 | **`C-0063`, CONSUMED AS DATA** from `gpd/results/T-125-*.json` |
| the 44 closing trios, their crossbar lengths and row pitches | — | **`C-0062`, CONSUMED AS DATA** from `gpd/results/T-127-*.json` |
| the per-row cap and flexure misalignment floors | 9 / 21 / 24 / 24 / 27 / 24 / 27° and 30 / 6 / 3 / 24 / 18 / 18 / 54° | **`C-0062`, CONSUMED AS DATA** from the same file |
| the base misalignment floors, 6–12 bp | 33 / 69 / 57 / 6 / 6 / 33 / 33° | **`C-0059`, CONSUMED AS DATA** from `gpd/results/T-124-*.json` |
| the register table and the flatness verdict per row | offsets 3.91–0.17 nm | **`C-0065`, CONSUMED AS DATA** from `gpd/results/T-130-*.json`, **and re-derived here** at departure 0 |
| the measured backbone, the phosphodiester window, the phosphate radius, the groove angle | 0.60 / 0.70 nm, 1.00 nm, 120° | **CITED** via `C-0029`/`C-0057`; the window's own primary source **NOT FOUND** |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 10.67 / 10.5 | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Douglas et al. 2009) |
| `EI`, `GJ`, `S`, `k_θ` | 230, 460 pN·nm², 1100 pN, 13.5294 pN·nm/rad | **CITED, CanDo MODEL INPUTS / FITTED** |
| Fields et al.'s implied rigidity | 172.906 pN·nm² | **CITED, MEASURED** (*NAR* **41**:9881, 2013) |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the signed register and all its closers, both cheap bounds and their falsifiers, the overspend identity, the two-leg cap floor, the three compositions of every row, all 105 per-length designs, the 44 re-judgements, the six sensitivities and the four convergence records — is **derived here in code**, with `C-0052`'s, `C-0059`'s, `C-0062`'s and `C-0065`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The joint search over the crossbar lattice AND the base's axial position**, which `C-0065` names as its own open item 2 and which nobody has run. It would supersede the register table rather than contradict it, and it is the only thing that could restore the 10 bp row.
2. **The lateral seat.** It is the one axis on which this claim's verdict moves, it is a free variable upstream, and no task has asked which seat a *registered* array must choose.
3. **The 45-path mechanics against the 34-station array**, inherited from `C-0062` and `C-0048` and still unresolved.
4. **The flexure array**, which `C-0065` finds places **12 of 34** — `C-0041`'s standing negative, untouched here and the thing the truss branch actually hangs on.
5. **The scaffold and the staple breaks.** 34 instances put 136 covalent links into the host's backbone; no routing has been attempted.

## Challenges

**Raises [`CH-0082`](../challenges/CH-0082-a-truss-has-two-legs-and-the-design-table-gives-it-one-base.md)** against `C-0062`'s and `C-0059`'s design tables. **No upstream number fails to reproduce** — 23 reproductions, worst departure `3.1e−2` against a value its own claim publishes to three digits, and fourteen of `C-0065`'s at departure 0.

**[`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md) is UPHELD by this composition**, which is its own *"what would settle it"* item 2: the 10 bp row is not representable at any shared leg length, and the 9 bp row at 18.0° is confirmed and now carries a leg length.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that a leg's base may sit anywhere on the host duplex**, which would dissolve the register and with it the pinning. It rests on `C-0029`'s covalent window and `C-0057`'s torsion closure, neither measured on this junction.
2. **A lateral seat the design is forced to.** At 0.5 nm the 9 bp row has no representable design at any leg length; the headline assumes the seat is free, as `C-0059` treats it.
3. **A joint search including the base**, which is open item 1 and would supersede this table.
4. **A finer register grid finding a different nearest centre.** The nearest centre does not move between 0.17 and 0.085 nm steps or between 120 and 240 azimuths, but a much finer grid has not been run.
