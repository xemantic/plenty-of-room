# C-0066 — The arm slab and the tie-down path **can** share one face, and there is exactly one registration at which they do: the ties must land **on the arms**. Every regular grid fails — 26 of 45 ties, 24 of 30 and 10 of 15 land on an arm at the best arm senses, and **no rigid translation of a two- or three-column grid clears every row** — yet the free tie capacity is **108** against the 45 demanded, so what refuses them is not the room but the **registration**; and the sweep runs the favourable way, because a rotating arm's plan projection is a **cosine**

| | |
|---|---|
| **Task** | [`T-126`](../tasks/T-126-arm-slab-clearance.md), raised by [`C-0061`](C-0061-stacked-arm-sheet.md)'s *Still open* item 4 and named again as [`C-0063`](C-0063-upward-root-placement.md)'s item 4 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring scheme |
| **Verification type** | **logical** (exact plane and section geometry on measured lattice constants, with the arm senses enumerated exhaustively — no mesh, no fitted parameter) **+ in-silico** (`C-0009`'s grillage under `C-0022`'s **solved** load, for the one quantity the geometry cannot supply: what the escape costs in flatness) |
| **Verdict** | **PASS, and the answer is a plan and a section rather than the impossibility the task offered as its alternative — but it is a *conditional* yes, and the condition is a registration.** **The section decides the plan.** `C-0035`'s tie has to reach the **tile**, so its clear column runs from the tile's own top face at `z = +1.0 nm` to the standoff base plane, and the arm slab — **1.69 to 3.69 nm** at rest, **1.69 to 6.69 nm** swept over §3's acceptable 3 nm — lies **strictly inside it**. A plan overlap is therefore **level-independent**: it cannot be relieved by stacking, by re-ordering or by a larger body. That is `C-0041`'s Fact A in a new place, and it is what makes an area bound the wrong instrument here — the area is **0.664** of the footprint and settles nothing. **On the plan, every regular tie grid fails.** At `C-0063`'s phase-24 placement, `C-0015`'s own **3 × 15** grid puts **30 of its 45 ties inside an arm** at the arm senses `C-0063` published and **26** at the best of the **2916** sense assignments the lattice allows — because **8 of the 15 rows have no choice at all**, their senses forced by the tile edge. Two columns give 24 of 30, one column 10 of 15. **And no rigid translation rescues them**: sweeping the whole grid through one column pitch at 400 001 offsets, the two- and three-column grids have **zero** clearing windows and the one-column grid has **four**, the nearest **6.785 nm off the tile centre-line** and the widest **0.99 nm** wide. **What refuses them is not the room.** The array leaves **108** places a tie could stand against the **45** demanded, and every one of the fifteen rows carries at least **five** — so the obstruction is **registration**, and the escape is to displace each tie into the room its own row leaves. That escape exists (45 stations, worst displacement **4.332 nm**, mean **2.081 nm**) and it is **nearly free**: the displaced set dishes **0.2219** of the stroke against the grid's **0.2182**, **+1.7 %**. **Neither is flat** (`T-5b` asks 0.10), and the design that *is* flat needs no tie grid at all — `C-0063`'s 34 roots dish **0.0706** with the coupling entering at the hinges. **The one registration the slab supplies for free is the arms' own tips**: 34 ties landing on the arm ends clear every neighbouring arm by **2.7156 nm** against the **1.345 nm** demanded, and that margin is the **root pitch minus the arm** — a lattice quantity with no fitted parameter in it, which clears a 2.69 nm duplex by **0.0256 nm** and does **not** clear the 2.73 nm square-lattice one. **The sweep runs the favourable way**: an arm rotates about its root, so its plan projection is `√(L² − s²)` and *shortens*; the swept envelope is the rest footprint **identically**, and a static plan view is conservative at every stroke. **§3's desired 10 nm does not change the clearance — it removes the element**, because 10 nm exceeds the 8.164 nm arm and a lever is a rotation, which is `C-0050`'s kinematic ceiling arriving from a plan view. Raises [`CH-0079`](../challenges/CH-0079-a-tie-grid-is-a-registration-and-an-armed-tile-has-none.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0029`'s two-covalent-link finding and `C-0055`'s *"the geometry is published and the motif is not"* are unchanged and upstream of every number. |
| **Provenance** | `gpd/results/T-126-arm-slab-clearance.json`, produced by `anchoring.ArmSlabClearanceStudyKt`; model in `src/main/kotlin/anchoring/ArmSlabClearance.kt`; **5 cheap bounds, 5 section records, 15 row records, 3 grid records, 3 station sets, 5 assembled flatness solves, 4 convergence sweeps, 14 upstream reproductions, 5 predicates**; **25 tests, 18 of them gate-named, in `src/test/kotlin/anchoring/ArmSlabClearanceTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 11 m 5 s, 0 failures** — the whole suite on its own isolated tree, with one concurrent agent's mid-TDD file dropped by `--drop-file` (`src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt`, `T-130`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**; **`C-0063`'s phase-24 placement** of `C-0055`'s **34** arms at `C-0039`'s **8.16439 nm**, read from its own result file; `C-0035`'s `Su` mounting at a standoff of **8.0 nm**; §3's **acceptable 3 nm** stroke (the desired 10 nm evaluated); `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V; `C-0017`'s **33.3333 pN/nm** mandate; free-tile stroke **4.90731 nm** |
| **Consumes** | [`C-0063`](C-0063-upward-root-placement.md) (**the placement itself**, read from `gpd/results/T-125-upward-root-placement.json`, and its 0.0706 reproduced), [`C-0061`](C-0061-stacked-arm-sheet.md) (the 1.69–3.69 nm slab and the 0.4626 plan fraction, both re-derived), [`C-0055`](C-0055-unused-junction-site.md) (`upwardHingeSites`, the `EAST` azimuth and its 53-site inventory at phase 24, the 10.88 nm root pitch, the 8.164 nm arm — **re-run as libraries**), [`C-0035`](C-0035-flexure-mounting-sense.md) (`midspanClearance`, `midspanPenetration`, `tieApertureArea`, `OrigamiDuplex` — the mounting whose tie-down path this is, **re-run as a library**), [`C-0041`](C-0041-flexure-array-packing.md) (`PLAN_TANGENCY_TOLERANCE`, the level-independence argument, the *"an area bound invites stacking"* discipline), [`C-0053`](C-0053-hinge-arm-array-packing.md) (the rooted-not-centred footprint convention and `armDirections`' feasibility rule), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the two-link 90° junction, which is why a tie's `x` is *not* on a crossover lattice), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md), [`C-0058`](C-0058-non-uniform-coupling.md)/[`C-0047`](C-0047-single-column-flatness.md) (the 0.2182 and the flatness pipeline), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0050`](C-0050-desired-stroke-reach.md) (the desired-stroke ceiling, corroborated here from a new direction) |
| **Raises** | [`CH-0079`](../challenges/CH-0079-a-tie-grid-is-a-registration-and-an-armed-tile-has-none.md), against `C-0035`'s nominal-design row *"what the tile carries: only the 45 tie attachments of `C-0015`'s 3 × 15 grid"* |

---

## The claim, in one line

**`C-0035` chose a mounting on a tile that carried nothing out of plane and `C-0055` then put 34 levers on exactly the face its ties cross; the two do not compete for *room* — there are 108 places a tie could stand and 45 are wanted — they compete for a *registration*, because the arms sit on a 10.88 nm crossover lattice and every coupling grid in this programme is drawn on a regular one, and the only regular grid that survives the collision is the one whose columns are the arms themselves.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, areas **nm²**, forces **pN**, stiffness **pN/nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan.** `x` runs **along** the helices, `y` **across** them, origin at the tile centre.
  **Section.** `z` is normal and positive **upward**, `z = 0` on the sheet's own **mid-plane**;
  §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`**, so two at exactly `d` are tangent
  and admissible (`C-0041`, `C-0053`); **in section it is a cylinder of radius 1.0 nm**, the B-DNA
  phosphate radius, which **is** the steric surface (`CLAUDE.md`). The 2.0 nm steric and 2.73 nm
  square-lattice readings are carried as sensitivities.
- **An arm** is `C-0055`'s: a duplex lying parallel to its host one interhelical distance above it,
  held by **one** crossover at an `EAST` (`+z`) site, **rooted at its hinge and not centred on
  it** — so `+x` and `−x` are different designs.
- **A tie-down** is `C-0035`'s: a duplex standing **normal** to the sheet, descending from a
  flexure midspan through the output superstructure onto the tile. `C-0023` makes it two-sided and
  therefore a duplex; `C-0029` makes its landing a **two-link joint at a duplex end**, quantised at
  the 0.34 nm rise along `x` and **on no crossover lattice** — which is why the tie's `x` is a
  design variable where the arm's root is not.
- **The arm's rotation axis is taken ACROSS its own length at the root.** This is `C-0055`'s
  deliberately unadjudicated open item 2, and it is stated as a convention: under `C-0040`'s
  hinge-line reading the axis runs **along** `x`, the arm spins about its own axis, and the element
  delivers no stroke at all — so there would be no clearance to ask about.

---

## The cheap bounds, which ran first and decided where the expensive part went

| | bound | value | against | ratio | fired? | what it settled |
|---|---|---|---|---|---|---|
| **1** | the plan area of 34 arms plus 45 ties | **1072.3 nm²** | 1614 nm² | **0.664** | **no** | **an area budget settles nothing**, exactly as `C-0041` warned. Run in order to be refuted |
| **2** | the arm slab's band inside the tie's own clear column | 5.00 nm | 8.38 nm | 0.597 | **YES** | **a plan overlap is LEVEL-INDEPENDENT** — the bound that makes the whole question a plan one |
| **3** | the **swept** plan envelope against the rest footprint | **0.000 nm** departure | 8.164 nm | **0** | **no** | the sweep is **favourable**: the projection is a cosine, so the swept envelope *is* the rest footprint and a static plan view is conservative |
| **4** | the root pitch minus the arm, against a tie's width | **2.71561 nm** | 2.69 nm | **1.0095** | **no** | the only gap the lattice offers clears a duplex by **0.0256 nm** — and does **not** clear the 2.73 nm square-lattice value |
| **5** | 34 arms plus 45 ties against the `EAST` inventory at phase 24 | 79 sites | **53** | 1.491 | **YES, conditionally** | **a ceiling with its threshold**: *if* a tie had to root on the upward crossover azimuth it could not be placed at all. It does not — `C-0029`'s junction is at a duplex **end** — so the bound is **recorded and not used** |

> **Bound 2 is what warranted the expensive part and pointed it at the plan.** Had it not fired,
> the question would have been `C-0041`'s level assignment and this task would have been
> formulated on the wrong quantity.

---

## Deliverable 1 — the section, which reduces the question to plan

| body | stroke | `z` band [nm] | thickness | inside the tie's column? |
|---|---|---|---|---|
| the host sheet | — | **−1.00 … +1.00** | 2.00 | no, it *is* the floor |
| **`C-0055`'s arm slab** | **0** | **1.69 … 3.69** | 2.00 | **yes** |
| the same | 1 | 1.69 … 4.69 | 3.00 | **yes** |
| **the same, at §3's acceptable stroke** | **3** | **1.69 … 6.69** | 5.00 | **yes** |
| **`C-0035`'s tie clear column** | 3 | **1.00 … 9.38** | 8.38 | — |

**The slab's floor does not move under stroke and its ceiling rises with it**, because the root
end is pinned to the tile and the tip is held by the driven body. Whatever the standoff base plane
is — and `C-0035` leaves it a design choice — it is above the arms, so the tie's column contains
the slab and `tieMayPassOverSlab` is **false at every stroke**.

> **This is the whole reason the answer is not an area.** A clash that cannot be relieved by a
> level cannot be relieved by a bigger body either, so the 0.664 area fraction and the *"stack it"*
> reflex it invites are both settled before the layout runs.

---

## Deliverable 2 — the plan, at `C-0063`'s phase-24 placement

Each row's arms as `C-0063` placed them, the room they leave, and what `C-0015`'s three columns
(`x = −13.333, 0, +13.333 nm`) then find. **Bold rows have no choice of arm sense at all.**

| row | roots [nm] | senses | arm footprints [nm] | free [nm] | ties it could hold | grid clashes, as placed | at the best senses |
|---|---|---|---|---|---|---|---|
| **0** | −16.32, −5.44, 16.32 | + + − | [−16.32, −8.16] [−5.44, 2.72] [8.16, 16.32] | 15.51 | 5 | **3** | **3** |
| 1 | 0, 10.88 | + + | [0, 8.16] [10.88, 19.04] | 23.67 | 8 | 2 | 1 |
| **2** | −16.32, 5.44, 16.32 | + − − | [−16.32, −8.16] [−2.72, 5.44] [8.16, 16.32] | 15.51 | 5 | **3** | **3** |
| 3 | 0, 10.88 | + + | [0, 8.16] [10.88, 19.04] | 23.67 | 8 | 2 | 1 |
| **4** | −16.32, 16.32 | + − | [−16.32, −8.16] [8.16, 16.32] | 23.67 | 8 | 2 | **2** |
| 5 | −10.88, 0 | + + | [−10.88, −2.72] [0, 8.16] | 23.67 | 8 | 1 | 1 |
| **6** | −16.32, 16.32 | + − | [−16.32, −8.16] [8.16, 16.32] | 23.67 | 8 | 2 | **2** |
| 7 | −10.88, 10.88 | + + | [−10.88, −2.72] [10.88, 19.04] | 23.67 | 8 | 1 | **0** |
| **8** | −16.32, 16.32 | + − | [−16.32, −8.16] [8.16, 16.32] | 23.67 | 8 | 2 | **2** |
| 9 | 0, 10.88 | + + | [0, 8.16] [10.88, 19.04] | 23.67 | 8 | 2 | 1 |
| **10** | −16.32, 16.32 | + − | [−16.32, −8.16] [8.16, 16.32] | 23.67 | 8 | 2 | **2** |
| 11 | −10.88, 0 | + + | [−10.88, −2.72] [0, 8.16] | 23.67 | 8 | 1 | 1 |
| **12** | −16.32, −5.44, 16.32 | + + − | [−16.32, −8.16] [−5.44, 2.72] [8.16, 16.32] | 15.51 | 5 | **3** | **3** |
| 13 | −10.88, 0 | + + | [−10.88, −2.72] [0, 8.16] | 23.67 | 8 | 1 | 1 |
| **14** | −16.32, 5.44, 16.32 | + − − | [−16.32, −8.16] [−2.72, 5.44] [8.16, 16.32] | 15.51 | 5 | **3** | **3** |
| | | | | **322.41** | **108** | **30** | **26** |

**Three readings, and two of them are new:**

1. **`C-0063`'s free variable is not free on eight of the fifteen rows.** Its item 4 records that
   the arm directions are chosen greedily and do not enter the flatness. They do enter *this*, and
   they are **forced** wherever a row carries a root at `±16.32` — its arm cannot point off the
   tile — which is every three-arm row and every row of the `±16.32` pair. Freeing the seven rows
   that do have a choice is worth **30 → 26 of 45**, and no more. The whole array admits
   **2916** sense assignments (`3⁶ × 4`, the rows being independent), and every one of them was
   enumerated.
2. **The middle column is the worst place a tie could be put.** Six of the fifteen rows carry a
   root at exactly `x = 0`, which is `C-0015`'s own middle column, so those six clash **at every
   sense and at every arm length**.
3. **The three-arm rows are the tight ones.** They leave 15.51 nm of a 40 nm row and hold five
   ties; the two-arm rows leave 23.67 nm and hold eight.

### The regular grids, and the rigid translations of them

| columns | ties | column `x` [nm] | clashes as placed | at the best senses | rows fully clear | clearing windows | measure [nm] | nearest [nm] | widest [nm] |
|---|---|---|---|---|---|---|---|---|---|
| **1** | 15 | 0 | **10** | **10** | 5 / 15 | **4** | 2.031 | **6.785** | **0.990** |
| **2** | 30 | ±10 | **24** | **24** | 0 / 15 | **0** | 0 | — | — |
| **3** | 45 | 0, ±13.333 | **30** | **26** | 1 / 15 | **0** | 0 | — | — |

> **No rigid translation of a two- or three-column grid clears every row**, over 400 001 offsets
> spanning a full column pitch. The one-column grid clears in **four** windows — and the two
> narrow ones are **0.0256 nm** wide, which is bound 4 itself: they are the gap between an arm's
> tip and the next arm's root, with a duplex exactly in it.
>
> **The nearest clearing offset is 6.785 nm — 2.5 duplex pitches, 17 % of the tile edge.** And
> the grid it clears is `C-0047`'s 1 × 15, which **on its own centred axis** already dishes 0.695
> of the stroke — 2.26× worse than no coupling at all. Its off-centre dishing is not computed
> here and would have to be worse; a one-column coupling is not a design this branch wants
> anyway.

---

## Deliverable 3 — the room is there, and it is the registration that is missing

**The array leaves 108 places a tie could stand, against the 45 §3's path count demands**, and the
poorest row holds five. So the failure above is **not** a *"the tile is too small"* result and
`T-102` is not what it wants.

**The escape is to displace each tie into the room its own row leaves** — the feasible `x` nearest
its nominal column, assigned in column order:

| scheme | stations | worst displacement | mean displacement | **dishing / stroke** | flat at 0.10? | peak path force | peak crossover |
|---|---|---|---|---|---|---|---|
| **NONE — free tile** | 0 | — | — | **0.3079** | no | — | 0.244 pN |
| **`C-0015`'s 3 × 15, as `C-0035`'s ledger draws it** | 45 | 0 | 0 | **0.2182** | no | 1.943 pN | 0.150 pN |
| **the same 45, SNAPPED into the arms' room** | **45** | **4.332 nm** | **2.081 nm** | **0.2219** | no | 2.047 pN | 0.791 pN |
| **`C-0063`'s 34 arm ROOTS** | 34 | — | — | **0.0706** | **YES** | 2.298 pN | 1.246 pN |

**The displacement is nearly free and it does not help.** Moving 45 ties by up to 4.33 nm costs
**+1.7 %** of dishing (0.2182 → 0.2219) — a smaller perturbation than this claim expected, and the
reason is `C-0026`'s: the coupling's across-helix registration is what matters and the displacement
is entirely **along** the helices. But **neither set is flat**, and the set that is flat is the one
that needs no tie grid at all: `C-0063`'s coupling enters at the arm **hinges**, which are the arms.

> **A tie grid displaced 4.33 nm is still a coupling on stations, and it is 3.1× less flat than
> the arm roots.** The escape therefore exists and is not worth taking.

---

## Deliverable 4 — the one registration the slab supplies for free

If the tie-down lands on the **arm's own far end** rather than on the tile — the arm's `A2`
two-link joint (`C-0034`) being a `+z` attachment like any other — the clearance is discharged by
the lattice:

| | value |
|---|---|
| ties | **34**, one per arm |
| clashes with a **neighbouring** arm | **0 of 34** |
| worst clearance, tip to the nearest other arm | **2.71561 nm** |
| demanded (half a duplex) | **1.345 nm** |
| margin | **2.02×** |
| what the number **is** | the **root pitch minus the arm**, `10.88 − 8.16439` — a lattice quantity with no fitted parameter |

**And it is a knife edge in a different currency.** The same 2.71561 nm gap, asked to hold a tie
**entirely** between two consecutive same-sense arms, clears a **2.69 nm** duplex by **0.0256 nm**
and fails outright at the **2.73 nm** square-lattice interhelical distance. The tip registration
survives that because half of a tip link's own disc lies over its own arm by construction; a
free-standing tie between two arms does not.

---

## Deliverable 5 — the stroke, in plan and in section

| stroke | arm plan reach | swept plan envelope | slab ceiling | verdict |
|---|---|---|---|---|
| 0 | **8.16439 nm** | 8.16439 nm | 3.69 nm | — |
| 1 | 8.10287 | **8.16439** | 4.69 | the envelope is the rest footprint |
| **3 — §3's acceptable** | **7.59316** | **8.16439** | **6.69** | **the plan clearance is worst at rest** |
| **10 — §3's desired** | — | — | — | **the arm cannot reach it at all**: `sin θ = 1.2248` |

**The sweep is favourable, exactly.** `√(L² − s²)` is monotone decreasing, so the union over the
whole stroke is the `s = 0` footprint and every plan number above is the worst case. That is
computed as a union at 8, 64 and 4096 samples and agrees to the last digit.

**§3's desired stroke does not change the answer; it removes the element.** 10 nm exceeds the
8.164 nm arm, and a lever is a rotation — which is `C-0050`'s kinematic ceiling reached from a
plan view rather than from a force balance, and an independent corroboration of it.

---

## The five verification gates

Executed as **18 gate-named tests** (of 25) in `src/test/kotlin/anchoring/ArmSlabClearanceTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL in 11 m 5 s, 0 failures** — the whole suite on its own
isolated tree, with one concurrent agent's mid-TDD file dropped by `--drop-file`
(`src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt`, `T-130`).

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the arm slab is a length band and reads 1.69–3.69 nm from the phosphate radius rather than by assertion; **the whole plan verdict is invariant under a common rescaling of every length by 10** — clash count, tie capacity and free-interval count identical, the free length exactly ten-fold; unphysical arguments throw at **eight** entry points, including a backwards band, a backwards interval, a negative stroke, **a stroke longer than the arm**, a base plane below the tile's top face and a row no direction assignment places | **PASS** |
| **2 — limiting cases** | **ZERO ARMS reproduces `C-0035`'s clearance ledger exactly** — no clash at 1, 2 or 3 columns, the whole 40 nm row free, 14 ties per row, and its own 325.62 nm² aperture floor, 5.31 nm clearance and 4.69 nm penetration recomputed through **its own library**; the swept envelope equals the rest footprint at four strokes while the instantaneous footprint is strictly shorter; a gap shorter than a duplex holds no tie and a double one holds two; the interleave is per-row — an arm in row 0 does not touch a tie in row 1 | **PASS** |
| **3 — symmetry, conservation and section** | see below | **PASS** |
| **4 — numerical convergence** | the swept envelope over 8 → 64 → 4096 samples: departure **0.0**; the clearing measure of the one-column grid over 4001 → 40001 → 400001 offsets: 2.000 → 2.030 → 2.031, **`5.9e−4`** — and the last two digits are the two 0.0256 nm windows resolving; nested subdivisions **1 ⊂ 2 ⊂ 4** on the snapped 45: 0.221655 → 0.221863 → 0.221908, **`2.0e−4`** (nested only, per `CLAUDE.md`); the dishing sample grid 41/81/161: departure **0.0**; **the result file re-run through `tools/study.sh` and diffed byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **14 reproductions, worst departure `2.1e−4`**: `C-0055`'s 8.164 nm arm (`4.8e−5`), its 10.88 nm pitch and its **53**-site `EAST` inventory at phase 24 (both exactly); `C-0061`'s **1.69** and **3.69** nm slab (exactly) and its **0.4626** plan fraction (`1.1e−4`); `C-0063`'s **34** and its **0.0706** (`2.1e−4`); `C-0022`'s **0.3079** (`7.7e−6`); `C-0058`'s **0.2182** (`6.1e−5`); `C-0035`'s **325.6 nm²**, **5.31 nm** and **4.69 nm**; `C-0015`'s middle column at exactly 0 | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The section theorem is asserted with its own falsifier.** A body *above* the tie's column
   **can** be passed under — `tieMayPassOverSlab` returns true for a band at 20–22 nm — so the
   `false` at the arm slab is a measurement of where the slab is, not a property of the function.
2. **The free intervals and the arm footprints partition every row exactly**, at all fifteen:
   covered length plus free length equals the 40 nm edge to `1e−9`. A conservation law the
   construction never imposes.
3. **The direction enumeration agrees with `C-0063`'s own `armDirections`** at every row — its
   greedy assignment is a member of the exhaustive feasible set. Two constructions of the same
   feasibility condition, one of them already published.
4. **Every placed arm lies inside the footprint and no two arms of a row overlap**, asserted
   rather than assumed, which is what makes the *free* intervals free.
5. **The snapped stations are re-checked against the arms they were snapped around** — all 45
   verified clear of every arm interval of their own row, and inside the tile — so the escape is
   asserted on its own output rather than on the procedure that produced it.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the section bound not firing — a tie able to pass over an arm | **no** | the slab is inside the column at every stroke; the question is a plan one |
| **F2** | the swept envelope exceeding the rest footprint | **no** | equal to the last digit; the projection is a cosine |
| **F3** | the interleave decided by **room** rather than by registration | **no** | 108 against 45, and the poorest row holds five — so the answer is not `T-102` |
| **F4** | zero arms failing to reproduce `C-0035`'s ledger | **no** | exact at every entry |
| **F5** | some regular grid clearing every row | **no at 2 and 3 columns**; **yes at 1**, and only 6.785 nm off centre | the finding |

**A result that was not anticipated:** **the escape is nearly free and is not worth taking.**
The task was formulated expecting the displacement to be the expensive part; displacing 45 ties by
up to 4.33 nm costs 1.7 % of the dishing, because the displacement is entirely **along** the
helices and `C-0026`'s registration is **across** them. What kills the composition is not the
price of the escape but that the escaped design is still 3.1× less flat than the one that has no
tie grid at all.

**A second one:** `C-0063`'s arm senses are **not a free variable on eight of the fifteen rows**.
Its own item 4 offered them as free; the tile edge forces them wherever a row roots at `±16.32 nm`,
and the whole exhaustive freedom is worth 30 → 26 of 45.

---

## What this does to `C-0035`, `C-0061` and `C-0063`

| said | this claim finds |
|---|---|
| `C-0035`: *"the tile now carries no out-of-plane element at all"* | **no longer true of any design that adopts `C-0055`'s escape**, and that is the whole task. Every one of `C-0035`'s own numbers reproduces |
| `C-0035`: *"what the tile carries: only the 45 tie attachments of `C-0015`'s 3 × 15 grid"* | **26 of those 45 stations do not exist on an armed tile** — [`CH-0079`](../challenges/CH-0079-a-tie-grid-is-a-registration-and-an-armed-tile-has-none.md) |
| `C-0035`: the mounting determination `Su` | **untouched.** Nothing here is about the sense; the tie still comes down through the superstructure, and the arms are on the face it comes down onto |
| `C-0061`: *"the arm array occupies a slab 1.69 to 3.69 nm above the sheet over 46.3 % of the plan … a clearance question with a stated geometry and no solve here"* | **solved**, and the slab and the fraction both reproduce. What it did not name — that the slab **thickens with the stroke**, to 6.69 nm — does not change the verdict, because the tie's column contains it either way |
| `C-0061`: *"they do not re-open `C-0035`'s rejection"* | **upheld**: nothing here touches the `−z` half-space |
| `C-0063`: *"the arm directions are chosen greedily … they do not enter the flatness but they set which way 34 levers point, and `C-0035`'s clearance question is a plan-view one"* | **correct, and the answer is that they are forced on 8 of 15 rows** and worth 30 → 26 on the rest |
| `C-0050`: the desired stroke is kinematically out of reach | **corroborated from a plan view**: 10 nm exceeds the 8.164 nm arm |

---

## Validity range

- **TRL 1–3. Nothing is measured, and the motif is not demonstrated.** A free lever held to a
  single-layer sheet by one crossover is this programme's own construct (`C-0055`, 62 recorded
  queries), and Ke et al.'s 8 bp staple-break yield cost is unpriced and applies to all 34.
- **The plan model is `C-0041`'s and `C-0053`'s hard-body one**, at nominal positions: no thermal
  excursion, no assembly tolerance, no out-of-plane bow. Conservative in the same direction as
  theirs — a real array is *less* likely to interleave, and the 0.0256 nm margin of bound 4 is far
  inside any of those.
- **The tie's `x` is treated as continuous.** `C-0029`'s two-link junction sits at a duplex end,
  so it is quantised at the 0.34 nm rise; 0.34 nm is 0.126 of a duplex width and moves no verdict,
  but the snapped stations quoted here are unrounded to it.
- **The standoff base plane is not specified by §1 or §3**, and this claim does not need it: the
  section theorem holds for **any** plane above the arms, and `C-0035` already establishes that the
  plane must be above the tile. The 8.0 nm standoff is carried only so that `C-0035`'s own ledger
  reproduces.
- **The arm's rotation axis is a CONVENTION**, `C-0055`'s open item 2, stated in the conventions
  and not adjudicated. Under `C-0040`'s reading the element delivers no stroke.
- **`C-0063`'s placement is a single-state flatness result** (`C-0022`'s 2 mM, 10 nm, 0.192 V), and
  `T-129` owns whether it survives a range. The **geometry** here does not depend on the load at
  all; only the four dishing numbers do.
- **The 26 and the 30 are properties of `C-0063`'s placement.** A different placement on the same
  lattice would give different counts — but not a different *kind* of answer, because the
  10.88 nm root pitch and the 8.164 nm arm are what leave the gaps, and both are fixed.
- **Only rigid translations of the grid were swept.** A grid with independently chosen column
  positions is not a `m × 15` grid and is the *snapped* set, which is evaluated separately.
- **The flatness numbers are `C-0009`'s static, single-layer, linear grillage**, exactly as
  `C-0047`, `C-0058`, `C-0061` and `C-0063`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 34-arm placement at phase 24, the 8.16439 nm arm, the 10.88 nm root pitch | — | **`C-0063`**, read from `gpd/results/T-125-upward-root-placement.json` and reproduced here |
| the solved edge profile | depth −0.30293 over 8.939 nm, rim −0.59388 over 1.0 nm | **`C-0022`**, read from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on concentration, gap **and bias** |
| interhelical distance | 2.69 nm single-layer, 2.73 square | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| B-DNA steric diameter / phosphate radius | 2.0 nm / 1.0 nm | **CITED**; the phosphate backbone *is* the surface (`CLAUDE.md`, via `C-0029`) |
| crossover interface spacing, rise per base pair | 32 bp, 0.34 nm | **CITED**, Rothemund (2006) / Ke et al. (2009), via `C-0015`/`C-0055` |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014), via `C-0009` |
| `C-0061`'s, `C-0063`'s, `C-0058`'s, `C-0022`'s and `C-0035`'s published figures | 1.69, 3.69, 0.4626, 0.0706, 0.2182, 0.3079, 325.6, 5.31, 4.69 | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the section bands and the level-independence theorem, the swept envelope, the
exhaustive direction enumeration and the forced-row count, the free intervals and the 108-tie
capacity, the three grid tables, the 400 001-offset translation sweep and its four windows, the
snapped station set and its displacements, the tip-link clearances, and the four assembled dishing
solves — is **derived here in code**, with `C-0009`'s, `C-0022`'s, `C-0035`'s, `C-0053`'s,
`C-0055`'s and `C-0063`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **What the composed device actually is.** This claim answers the geometry of *both* readings —
   ties to the tile beside the arms, and ties onto the arms — because §1 and §3 do not say which
   coupling the programme is building. `C-0050`'s catalogue keeps `E5a1` and `C-0023`'s linear
   `E5` alive together, and **nothing has chosen between them**.
2. **The flatness of the snapped 45 over `C-0022`'s other states.** One state only, the same
   exposure `C-0063` and `C-0058` carry and `T-129` is chartered for.
3. **The `A2` joint at an arm tip carrying a tie-down.** `C-0034` prices it as an anchorage, not
   as the landing of a vertical member under `C-0035`'s kinematics, and the 2.02× clearance margin
   found here says nothing about the joint's own capacity.
4. **The staple routing.** 34 arms and 45 (or 34) ties on one face is a caDNAno question; `C-0055`
   records 35.7 % of M13 as a ledger and not a route, and the ties are not in that ledger.
5. **The scatter.** Every clearance here is at nominal positions; the 0.0256 nm margin of bound 4
   is the one number in this claim that a tolerance could reverse, and no tolerance model exists.

## Challenges

**Raises [`CH-0079`](../challenges/CH-0079-a-tie-grid-is-a-registration-and-an-armed-tile-has-none.md)**
against `C-0035`'s nominal-design row. **No number in `C-0035`, `C-0055`, `C-0061`, `C-0063`,
`C-0058`, `C-0022` or `C-0015` fails to reproduce** — 14 reproductions, worst departure `2.1e−4`
against a value its own claim quotes to three digits.

**None stands against this claim.** The four ways it would fail:

1. **A tie that does not have to reach the tile.** Then the section theorem lapses and stacking is
   available again. `C-0035`'s kinematic identity forbids it: the midspan is tied to the *driven
   body*, and the driven body is the tile.
2. **A demonstration that a coupling grid need not be regular.** Then the whole finding softens to
   the snapped set and its 1.7 % — which is priced here, and which still does not reach `T-5b`'s
   0.10.
3. **A different arm length or root pitch.** Both are fixed by `C-0039` and `C-0055`, but a
   shorter arm would widen every gap; at `arm ≤ 8.19 nm` the placement itself changes (`C-0055`'s
   cliff), so the two cannot be varied independently.
4. **A tolerance model.** The 0.0256 nm margin of bound 4 is 0.075 of a base-pair rise, and any
   real scatter erases it — which would remove the *free-standing* tie between two arms and leave
   the tip registration, whose margin is 1.37 nm.
