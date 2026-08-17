# C-0081 — **The seam breaks `C-0076`'s node congruence wherever it sits, and there is nowhere on a 40 nm tile for it to hide**: removing the junctions at one plane removes exactly **one** pull event from **every** duplex, so between `s−2` and `s+2` every duplex is straight at `±Δ/4` and every interface is pinned at an **extremum** — **6 to 12 of `C-0063`'s 34 stations** lose the node, at every one of the 8 seam positions the tile admits. **`C-0076`'s verdict still stands, on its other argument**: `M = p − d − L` carries no weave coordinate, so the plan margin is **0.898333 nm at every seam, to the last bit**. **What the seam costs is the thing `C-0076` gained** — the departure at an affected station is *exactly* `Δ/2`, so the annihilated **1.2–1.75 nm amplitude bracket is restored at full strength**, and at the cryo-EM amplitude the worst across-row clearance goes **NEGATIVE (−0.0023 nm)**. And the model reproduces Snodin's own sentence — one group of pairs at the **maximum**, the other at the **minimum** — from the shared duplexes' geometry alone, with nothing in the construction forcing it

| | |
|---|---|
| **Task** | [`T-140`](../tasks/T-140.md), raised by [`C-0076`](C-0076-weave-exclusion-width.md)'s *Still open* item 2 and its own failure route 1 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring array the plan model belongs to |
| **Verification type** | **logical** (a seam is one integer on `C-0055`'s 8 bp plane lattice, so the value at a station is exact arithmetic and the parity argument is a proof) **+ in-silico** (`C-0063`'s 34 stations read from `gpd/results/T-125-*.json` and evaluated at every seam position the tile admits) **+ literature** (Snodin et al. 2019 **re-fetched and the whole seam paragraph read directly for this task** — `C-0076` quotes one clause of it and the rest changes what the clause means) |
| **Verdict** | **PASS, and the acceptance's second branch is answered NO.** There is **no** seam position inside the 40 nm tile with no station within one weave period of it: every one of the **8** candidate planes carries **6–12** of the 34 stations inside its straight window and **12–20** within a full period. The tile is **3.68 weave periods** wide and carries 34 stations; there is nowhere for a seam to stand. **The congruence therefore fails, and it fails completely where it acts** — not by a small amount at many stations but by the *whole* half-amplitude at the affected ones, because a seam does not perturb the wave, it **removes an extremum**: with the junctions at plane `s` absent, every duplex loses exactly one pull event (at any even plane every duplex participates in exactly one interface crossover), is straight at `±Δ/4` from `s−2` to `s+2`, and every interface is pinned at `mean ± Δ/2` across the window instead of passing through its mean at the odd planes. **`C-0076`'s VERDICT is untouched**, and this is why it is worth stating rather than assuming: its two arguments are independent and **only one of them is about phase**. The categorical argument — `M = p − d − L` charges an **along**-helix gap between **unbonded** bodies, while the weave is an **across**-helix separation — contains no weave coordinate at all, so the plan margin is `0.898333453 nm` at all 8 seam positions, one distinct value. **What the seam destroys is `C-0076`'s headline**: its finding that the disputed **1.2–1.75 nm** amplitude bracket has coefficient *exactly zero* at the stations. At an affected station the interhelical departure is **exactly `Δ/2`** and the host duplex's own offset **exactly `Δ/4`** — unit slope in the amplitude — so the bracket returns at full strength precisely where it was annihilated: **0.600 / 0.750 / 0.875 nm** at Yoo / Snodin / Bai. **And the sign is not the one the wording suggests.** *"Opens up"* sounds like more room, and on one parity of interfaces it is; the **other parity closes by the same amount**, so the worst across-row clearance at a station falls from **0.872724 nm** to **0.122724** at Snodin's amplitude and to **−0.002276 nm** at Bai's — arms on adjacent rows in contact, a failure mode no plan model in this branch contains. **The one independent check the model has, it passes**: Snodin reports one group of helix pairs *"opens up to the largest extent"* and *"the other group … opens up much less"*, and here the two groups are the two parities of `2b − s (mod 4)` and come out at the **maximum** and the **minimum** exactly — from the shared duplexes' geometry alone, without appeal to the *"extra scaffold crossovers"* he attributes the second half to. Nothing in the construction forces that. **The remedy is free and it is an input, not a result**: the Gen-1 tile has no scaffold routing, and a **seamless** one — Rothemund's own alternative, and the geometry of the 10-helix bundle Snodin compares against, *"with a similar pattern and spacing of junctions but without a seam"* — restores `C-0076` in full at zero cost. Raises no challenge against `C-0076`, which **named this counter-case itself**. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** (`C-0055`, `C-0029`). The weave amplitudes are three literature readings, each with its own read flag below; the seam's *existence and phase rule* are Snodin's own words, read directly. |
| **Provenance** | `gpd/results/T-140-seam-weave-congruence.json`, produced by `anchoring.SeamWeaveStudyKt`; model in `src/main/kotlin/anchoring/SeamWeave.kt` (**new file — `WeaveExclusionWidth.kt` was not edited**); **34 station records, 8 seam records, 3 amplitude records, 5 reproductions, 5 predicates, 5 falsifiers, 9 findings**; **18 gate-named tests** in `src/test/kotlin/anchoring/SeamWeaveTest.kt`; Snodin's seam paragraph re-fetched from `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC6379721/fullTextXML` and quoted verbatim below; the result file emitted **three times** through `tools/study.sh` and **byte-for-byte identical** on the last two; `tools/verify.sh` **BUILD SUCCESSFUL in 13 m 53 s**, the whole suite on its own isolated tree with one sibling's in-progress `T-139` test dropped (`--drop-file src/test/kotlin/electrostatics/DuplexPairSeparationTest.kt`) and nothing else |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer square-lattice sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, **32 bp** crossover interface spacing, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-upward-root-placement.json`. **The weave is measured at `[Na⁺] = 0.5 M` (oxDNA) and in vitrified buffer (cryo-EM), NOT at 2 mM MgCl₂** — carried as a validity note exactly as `C-0076` carries it, and not transferred. |
| **Consumes** | [`C-0076`](C-0076-weave-exclusion-width.md) (`WeaveProfile`, `isWeaveNode`, `weavePlaneIndex`, `planMarginAtWidth`, `DuplexSteric` — **re-run as libraries**, and its congruence reproduced as this task's free limiting case), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, `CROSSOVER_PLANES_PER_PERIOD`, the `(k − 2b) mod 4` azimuth rule), [`C-0063`](C-0063-upward-root-placement.md) (**the placement itself**, read from its result file), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the 2.715609 nm tip gap, reproduced), [`T-71`](C-0057-backbone-torsion-closure.md)'s `MeasuredBackbone` (the measured phosphate radius), `Gen1Tile` |
| **Raises** | nothing against `C-0076`. It **qualifies** `C-0076`'s Deliverable 2 in the direction `C-0076` itself declared open, and the qualification is recorded here rather than as a challenge because a claim that names its own counter-case has not been contradicted by it. |

---

## The claim, in one line

**A scaffold seam does not perturb the weave, it deletes an extremum — and a deleted extremum takes the node with it, so `C-0076`'s amplitude-free congruence is a property of a seamless sheet and of nothing else; the design consequence is that the Gen-1 tile should be routed without a seam, which costs nothing, and the reason the claim it qualifies still stands is that the claim had a second, phase-free argument.**

---

## The passage, read directly and in full

`C-0076` quotes one clause. The whole paragraph is what makes the model, and it was re-fetched for
this task (EuropePMC `fullTextXML`, `PMC6379721`, **200, full text**):

> *"In the middle of the plot (around base-pair index 150), a different pattern is evident. This is
> due to the presence of the **origami's seam (a series of junctions where the scaffold strand is
> exchanged), which runs along the middle of the tile**. In this region, **one group of
> double-helix pairs has a particularly large section without any junctions and so opens up to the
> largest extent here**, as is also very clear from Figure 2; the modulations in the distance in
> the middle of this region reflect the presence of junctions on adjacent pairs of helices. **By
> contrast, the other group of double-helix pairs has a shorter distance between junctions due to
> the extra scaffold crossovers, and opens up much less.**"*

and, on the representation this task needs:

> *"The bending that creates the weave pattern is **mostly localized at the junctions with the
> intervening sections basically straight**."*

and, twice more, that the seam region is anomalous:

> *"we exclude the outermost junctions on the tile, and **the junctions next to the scaffold seam
> as well as the seam itself**."*

> *"a 10-helix bundle tube with a similar pattern and spacing of junctions **but without a
> seam**."*

**Three things `C-0076` could not have known from its one clause.** The seam is *"a series of
junctions"* — a **column**, i.e. one integer on the plane lattice, which is what makes this task
cheap. The pairs *"may be split into two groups"* and **both** are named, the second one *closing*
rather than opening — which is the model's only independent check. And Snodin's own comparison
object is a bundle *without* a seam, which is the remedy.

---

## Deliverable 1 — the cheap bound: a seam is one integer, and the answer is a parity

`C-0055`'s lattice, consumed rather than restated: duplex `b` crosses to `b+1` at planes
`k ≡ 2b (mod 4)` and to `b−1` at `k ≡ 2b+2 (mod 4)`; both are even, and its upward sites are at
`k ≡ 2b+3 (mod 4)`, odd for every `b`.

**At any even plane, every duplex participates in exactly one interface crossover** — `2b ≡ s` or
`2b+2 ≡ s (mod 4)`, and one of the two always holds. So *"the junctions at plane `s` are absent"*
removes **exactly one pull event from every duplex in the sheet**, and between the surviving events
at `s−2` and `s+2` — which are both in the *same* direction, the one it did not lose — the duplex
is straight at `±Δ/4`.

| | over `[s−2, s+2]` |
|---|---|
| duplex `b` with `2b ≡ s (mod 4)` (lost its `+` event) | straight at **`−Δ/4`** |
| duplex `b` with `2b+2 ≡ s (mod 4)` (lost its `−` event) | straight at **`+Δ/4`** |
| interface `b` with `2b ≡ s` — the group whose crossover was deleted | pinned at **`mean + Δ/2`**, the MAXIMUM |
| interface `b` with `2b+2 ≡ s` | pinned at **`mean − Δ/2`**, the MINIMUM |

> **That the two groups come out opened and closed is Snodin's sentence, and nothing in the
> construction puts it there.** He attributes the second half to *"the extra scaffold crossovers"*;
> here it falls out of the shared duplexes alone, because interface `b−1` and interface `b` are
> moved by the *same* duplex `b` in opposite senses. The model needs no extra crossover to
> reproduce the observation, and an extra crossover would only reinforce it.

**The stations at planes `s ± 1` are exactly the ones inside that window**, and they are the only
ones affected — which is the whole calculation, before any code ran.

---

## Deliverable 2 — the exhaustive sweep, and the acceptance's second branch

A seam is a design coordinate of the scaffold routing and **this programme has never fixed one**,
so the deliverable is the sweep. The 40 nm tile at phase 24 admits **8** even planes:

| seam plane | `x` [nm] | stations in the straight window | within one full period | **still on a node** | worst across-row clearance at the measured girth [nm] |
|---|---|---|---|---|---|
| −10 | −19.04 | 8 | 12 | **26** | 0.12272 |
| −8 | −13.60 | **12** | 14 | **22** | 0.12272 |
| −6 | −8.16 | 6 | 20 | **28** | 0.12272 |
| **−4** | **−2.72** | 8 | 14 | **26** | 0.12272 |
| **−2** | **+2.72** | 8 | 14 | **26** | 0.12272 |
| 0 | +8.16 | 6 | 20 | **28** | 0.12272 |
| 2 | +13.60 | **12** | 14 | **22** | 0.12272 |
| 4 | +19.04 | 8 | 12 | **26** | 0.12272 |
| **none** | — | **0** | **0** | **34** | **0.87272** |

**The answer to the acceptance's second branch is NO.** The tile is `40/10.88 = 3.68` weave periods
wide and carries 34 stations; the best a seam can do is take **6** of them off the node and the
worst is **12**.

**The sweep is centro-symmetric**, and so is its cost profile — which is the phase-24
centro-symmetry `C-0063` selected its placement for, appearing in a quantity `C-0063` never looked
at. **Its centre is an exact tie**: the two planes nearest the tile's middle are equidistant to nine
decimals, so *"the seam in the middle"* is a tied **pair**, `−4` and `−2`, and not an argmin.
Decided at six significant digits with the lower plane winning, and reported — `CLAUDE.md`'s *"an
index is not a rounded double"*, caught by the test that asserts the tie rather than by a re-run
diff.

---

## Deliverable 3 — what the seam costs: the amplitude bracket, restored at full strength

`C-0076`'s headline is that the amplitude — the one weave parameter three sources cannot agree on —
has coefficient **exactly zero** at the stations, so the whole 1.2–1.75 nm bracket is annihilated.
At an affected station the departure is **exactly `Δ/2`**:

| source | read flag | `Δ` [nm] | interhelical departure [nm] | host axis offset [nm] | worst across-row clearance at the measured girth [nm] |
|---|---|---|---|---|---|
| Yoo & Aksimentiev 2013, all-atom MD | **CITED, SIMULATED**, carried from `T-134`'s survey, **not** re-fetched | 1.20 | ±0.600 | ±0.300 | +0.2727 |
| Snodin et al. 2019, oxDNA 2D tile | **CITED, SIMULATED**, **re-fetched and READ DIRECTLY** for this task | 1.50 | ±0.750 | ±0.375 | +0.1227 |
| Bai et al. 2012, cryo-EM | **CITED, MEASURED**, carried from `T-137`'s survey | 1.75 | ±0.875 | ±0.4375 | **−0.0023** |

**Unit slope in the amplitude.** So the bracket is restored at full strength exactly where it was
annihilated, and it is now load-bearing: the three readings differ by **46 %** in the departure and
they **straddle a clash**.

> **The sign is not the one the wording suggests.** *"Opens up"* reads as more room, and on the
> deleted-crossover parity it is. The other parity **closes by the same amount**, and the worst
> across-row clearance at a station is a **minimum over both** of a station's bounding interfaces.
> At the measured phosphate-backbone girth (`T-71`, 1.817276 nm) that is 0.87272 nm at a node,
> 0.12272 nm at a seam under Snodin's amplitude, and **negative** under Bai's.

---

## Deliverable 4 — what does not move, and why it is worth saying

| `C-0076` result | under a seam | why |
|---|---|---|
| the plan margin `M = p − d − L` has weave coefficient **exactly zero** — *categorically* | **unchanged**, `0.898333453 nm` at all 8 seam positions, one distinct value | `M` is an **along**-helix identity between **unbonded** bodies; the weave, seam and all, is an **across**-helix separation. A seam is a weave coordinate and `M` has none. |
| the placement threshold `pitch − arm = 2.715609 nm` | **unchanged** | a lattice quantity with no weave in it |
| 34 of 34 stations on a node | **22–28 of 34** | Deliverable 2 |
| the amplitude bracket has coefficient zero | **restored at full strength** | Deliverable 3 |
| an odd-plane element puts its tip at an antinode | **survives, and acquires a second half** | inside the straight window **both** ends of **any** element sit at an extremum, because there is no node there to land on |

> **`CLAUDE.md`'s *"a verdict that survives can survive on a different reason"*, and this is the
> first instance where the surviving reason was named in advance by the claim being qualified.**
> `C-0076` built two independent arguments for one coefficient and only one of them was about
> phase. Had it built only the numerical one, this task would have withdrawn its verdict.

---

## The five verification gates

Executed as **18 gate-named tests** in `src/test/kotlin/anchoring/SeamWeaveTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a distance and an offset are lengths and scale with every length, over 41 positions; an odd seam plane, a repeated seam, two seams closer than one period and a negative duplex or interface index all throw | **PASS** |
| **2 — limiting cases** | **THE FREE LIMITING CASE — with no seam the pull-event weave reproduces `C-0076`'s closed-form `WeaveProfile` to `1e−12`**, at all 15 duplexes and all 14 interfaces, on an 81-point plane grid, under **both** edge-duplex readings; far from a seam it is unchanged at seven planes on every interface; one plane from a seam it is at an extremum where the seamless weave is at its mean | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | a station's plane coordinate is an **integer**, so it needs no grid — identical at `x` snapped to 0.1, 0.01, 0.001 and 0.0001; the profile is Lipschitz with constant `Δ/2` per plane, asserted over 201 samples; **and the centre-of-tile tie is asserted as a tie** (two planes equal to `1e−9`, the third `> 1e−3` away), so the rounding rule is tested rather than assumed | **PASS** |
| **5 — literature and upstream** | `C-0076`'s congruence reproduced at zero seam (34 of 34, worst departure `0.0`); its 0.872724 across-row clearance (`4.3e−6`, its own rounding); its 0.898333 plan margin (`4.5e−7`); `C-0066`'s 2.715609 tip gap (`1.7e−7`); **Snodin's own sentence asserted as a test** — one interface group at the maximum, the other at the minimum, at every interface of a 15-duplex sheet | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The seam's effect is symmetric about its own plane**, at 40 offsets on every interface. The
   construction interpolates between events and does not impose evenness.
2. **The openings across a cut telescope**: `Σ_b (D_b − a) = offset(14) − offset(0)` at every
   plane, from two independently written functions, `≤ 1e−12`.
3. **Every duplex loses exactly one pull event** — the parity theorem, asserted rather than
   argued: `|offset| = Δ/4` at the seam plane for all 15 duplexes, and `offset(s−1) = offset(s+1)`
   for all of them, which is the straight section.
4. **The departure is exactly half the amplitude**, swept `Δ = 0, 0.5, 1.5, 1.75, 2.0`, `≤ 1e−12`
   — the zero-coefficient statement `C-0076` makes, inverted.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the seam leaves the node congruence intact, so `C-0076` needs no qualification | **YES** | every one of the 8 seam positions takes stations off the node, 6 to 12 of 34 |
| **F2** | no station lies within one weave period of a seam — the acceptance's own second branch | **YES** | every seam has 6–12 stations inside its straight window; the branch is closed as **NO** |
| **F3** | the plan-margin coefficient is non-zero under a seam | **no** | one distinct margin over all 8 positions; the categorical argument carries no weave coordinate |
| **F4** | the across-row clearance stays positive at every amplitude | **YES, at the cryo-EM amplitude** | −0.0023 nm, a clash, at 1.75 nm peak-to-peak; +0.1227 at Snodin's own 1.5 |
| **F5** | the pull-event model does not reproduce `WeaveProfile` at zero seams | **no** | `0.0` at every interface, every plane and both edge readings |

**What was not anticipated:** that *"opens up"* would be the **favourable** half of the effect, and
that the binding number would come from the parity that **closes**. The task was formulated to ask
whether a station stops being a node; the answer that matters is that half of the affected stations
lose across-row clearance, and at the cryo-EM amplitude they lose all of it.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged
  and upstream of everything here.
- **The seam model is a MODEL of a measured curve, not a solve.** With no junction between `s−2`
  and `s+2` it makes the duplex perfectly straight, i.e. it predicts a **plateau**, where Snodin's
  Figure 3 shows residual modulation inside the opened region *"reflect[ing] the presence of
  junctions on adjacent pairs of helices"*. The plateau is the extremum, so **this is the
  conservative reading**: a real profile relaxes toward the mean, never past it. No residual
  against his Figure 3 has been computed, because its data are not published as numbers.
- **The *"extra scaffold crossovers"* are not modelled.** Snodin attributes the second group's
  behaviour to them; on the 8 bp plane lattice an interface's crossovers are confined to one
  parity of even planes, so a 16 bp spacing is not expressible there and the real seam is a local
  motif this lattice does not carry. The model reproduces the *observation* without them, and they
  would push the same way.
- **The seam's position is an INPUT this programme has never supplied.** The Gen-1 tile has no
  scaffold routing. Everything above is a sweep over that input, and the honest headline is the
  range, not any one row.
- **The weave is measured on other objects at other ionic strengths** — Bai's is a multilayer
  square-lattice brick in vitrified buffer, Snodin's an oxDNA 2D tile at `[Na⁺] = 0.5 M`, Yoo's
  all-atom MD. **None is a single-layer sheet at 2 mM MgCl₂.**
- **Only ONE seam at a time is swept.** A real Rothemund rectangle has one; the model refuses two
  seams closer than one weave period, because a duplex that loses two consecutive events is
  straight over a span no measurement covers.
- **No flatness, stiffness or force number is touched.** This claim moves a plan clearance and a
  registration, and no load path.
- **The across-row clash at Bai's amplitude is a statement about the MEASURED phosphate-backbone
  girth, 1.817276 nm**, which `C-0076` establishes is a **floor** on the exclusion width and not an
  estimate of it (`T-139` is deciding the value). At any larger girth the clash is worse, so the
  finding is robust to `T-139` in the only direction `T-139` can move it.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the seam's existence, its being *"a series of junctions where the scaffold strand is exchanged"*, its running *"along the middle of the tile"*, and the two-group behaviour | — | **CITED, READ DIRECTLY for `T-140`** (Snodin et al., *NAR* **47**:1585, `PMC6379721`, EuropePMC `fullTextXML`) — the load-bearing input, quoted verbatim above |
| weave peak-to-peak, 2D tile | 1.5 nm | **CITED, SIMULATED**, re-fetched and read directly |
| weave minimum / maximum | 1.85 / 3.60 nm | **CITED, MEASURED** (Bai et al., *PNAS* **109**:20012), carried from `T-137`'s survey |
| all-atom weave window | 1.80–3.00 nm | **CITED via `gpd/data/T-134-tolerance-literature.md`, NOT re-fetched** |
| interhelical distance, single-layer sheet | 2.69 nm | **CITED, MEASURED** (SAXS, Fischer et al. 2016) |
| B-form phosphate radius | 0.908638 nm | **MEASURED, THIS REPOSITORY** (`T-71`, 13 084 crystallographic linkages) |
| rise, 8 bp plane lattice, 32 bp interface spacing, phase 24 | 0.34 nm | **`C-0015`/`C-0055`, CONSUMED** |
| the 34 stations, the 10.88 nm pitch, the 8.16439 nm arm | — | **`C-0063`/`C-0055`/`C-0039`, CONSUMED AS DATA and re-run** |

Everything else — the parity theorem, the pull-event weave and its equivalence to `C-0076`'s closed
form, all 34 station records, all 8 seam records, the three amplitude rows, the centro-symmetry of
the sweep and its tied centre, and the five reproductions — is **derived here in code**.

## Still open — named, not answered

1. **Should the Gen-1 tile be specified as seamless?** The sweep says a seam costs 6–12 stations
   and a seamless routing costs nothing, so the recommendation is obvious and the *feasibility* is
   not: Rothemund's seamless designs exist, but nothing in this programme has checked that a
   15-duplex 40 nm sheet can be raster-folded from one scaffold without one. **That is a routing
   question, and it is cheap.** Raised as `T-151` — `T-148` through `T-150` were taken by siblings between one `ls` of the queue and the next, which `CLAUDE.md` already records for claim numbers and which applies to task numbers too.
2. **The residual modulation inside the opened region.** The plateau is conservative, but nobody
   has measured how far a real profile relaxes over four planes. One oxDNA run of a seamed sheet
   would settle it, and it would also settle open item 4 of `C-0076` (the weave under load).
3. **Two seams, or a seam plus an edge.** A 40 nm tile's two edges are themselves places where the
   junction pattern stops; the edge reading here is `C-0076`'s (`edgeDuplexesStraight`), which is
   an *across*-helix statement, and the *along*-helix ends of the tile have not been modelled at
   all.
4. **Whether a station should simply be moved.** `C-0063`'s placement was optimised for flatness on
   a seamless lattice. If a seam is unavoidable, the 6–12 affected stations are known integers and
   a re-optimisation could avoid the window entirely — at whatever the flatness costs, which is
   unmeasured.

## Challenges

**Raises none.** `C-0076` declared this counter-case in its own *Still open* list and in its own
failure route 1, so it is qualified rather than contradicted, and the qualification is filed here.

**None stands against this claim.** The four ways it would fail:

1. **A seam that is not a missing junction column** — a motif with its own crossover pattern rather
   than an absence. Snodin's own parenthesis (*"a series of junctions where the scaffold strand is
   exchanged"*) is the check, and it says column.
2. **A measured profile that relaxes to the mean inside the opened region.** Then the departure at
   `s±1` is smaller than `Δ/2` and the clash at Bai's amplitude may not occur. The plateau is the
   bound, not the estimate.
3. **A scaffold routing for this tile with no seam.** Then none of this applies and `C-0076` stands
   unqualified — which is open item 1 and the recommendation.
4. **An exclusion width materially above the measured 1.817 nm floor** (`T-139`). That makes the
   clash *worse*, not better, so it cannot rescue the verdict — but it would move the amplitude at
   which the clash starts, and that number is quoted here at the floor.
