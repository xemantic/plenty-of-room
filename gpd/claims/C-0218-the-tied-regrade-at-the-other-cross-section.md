# C-0218 — **THE `15 × 4` BLOCK GRADED COUPLED ON THE TIED LATTICE IS `0 of 64` FLAT AT THE PENALTY LINK AND `0 of 128` AT THE RESOLVED ONE, TIGHTEST `0.160538609` OF THE STROKE — `1.60538609×` `T-5b` WHERE THE TIGHTEST `10 × 6` CELL IN THE CORPUS CLEARS IT BY `0.426 %` — SO THE ORDERING `C-0186` LEFT UN-ANNOTATED IS NOW MEASURED IN ONE STATE.** Only the tie **COUNT** transfers: `15 × 4` carries **`410`** staple bonds (`140` in plane, `270` through the thickness) against `435` (`135 / 300`), and its `59` turn ties split **`45 / 14`** against `50 / 9`, so `F5`'s transfer hypothesis is refuted — and the tie's **worth** does not transfer either, the per-realisation median ratio running `0.930605361`–`0.995818215` against `C-0180`'s `0.902845544`–`0.988116016`, inside it at only `48 of 64`. **AND THE ROW'S OWN ACCEPTANCE CLAUSE ASKS FOR A STATE THE LATTICE REFUSES**: the free stroke goes as `1/m`, so it is `3.5194795 nm` against `5.27921926` — **exactly `2/3`** — and `T-5b`'s `0.10` is therefore `1.5×` tighter in absolute nm at `15 × 4` (`0.35194795` against `0.527921926`). Read in nm the ordering **reverses at `10` of `128`** matched rows (`F10`, declared open, FIRED), all of them rows where **both** blocks are far outside the tolerance. **AND THE FIRST BLOCK WITH AN ODD RASTER-ROW COUNT BREAKS THE STANDING FALSIFIER**: a uniform pressure gives an exactly uniform field — every face beam at `p/k_f` to `1e−10` — and `HoneycombDeflection` reports **`0.0620506254`** of the stroke as dishing, because it removes its rigid plane by three **independent** projections and `⟨piston, tiltY⟩ = ∫y dA` vanishes only when the corrugated gap sequence is palindromic, i.e. only when `m` is **even** (`CH-0282`). Both readings are emitted at every cell; the least-squares fit reads `0.0`

| | |
|---|---|
| **Task** | [`T-294`](../tasks/T-294-the-tied-regrade-at-the-other-cross-section.md) — the completeness row [`C-0186`](C-0186-carrying-the-tied-regrade.md) §1 opened and [`C-0191`](C-0191-thirteenth-answers-synthesis.md) (`T-276`) stated in the document |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (the same three-dimensional beam-and-bond lattice, the same exact Woodbury coupling surrogate and the same `C-0087`-measured incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream restricted per cell**, at the OTHER 60-helix cross-section) **+ logical** (a cheap bound that is integer arithmetic and two committed result files, and every `15 × 4` census **derived from the block and asserted against the bond graph** rather than transferred) |
| **Verdict** | **PASS on all nine predicates.** `F1` FIRED and **its firing is the finding** (`CH-0282`); `F4` did not fire; `F5`'s declaration is internally inconsistent and **both readings are emitted** (§8); `F6`, `F7`, `F8`, `F9`, `F11`, `F12`, `F13` were declared **OPEN** and none of the seven fired; `F10` was declared open and **FIRED**; `F2` and `F3` did not fire, and are measured **outside** the run so they emit `fired: null` per `CH-0281`; **`F14` was declared CLOSED and FIRED** — two independent emissions differ at 3 leaves of 10 649, all three a `reproductionDeparture` in a record type the departure gate cannot see, `0` verdicts and `0` unclassified (§9). **Two challenges are raised**: [`CH-0282`](../challenges/CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md) and [`CH-0283`](../challenges/CH-0283-the-uncoupled-tile-is-not-always-the-floor.md). `C-0186` §1's deliberate leave is **DISCHARGED** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** `k_θ` is `Gen1Tile`'s **square-lattice-fitted** constant and `k_s` a construction; a scaffold turn is assembled with a staple crossover's three elements because it is the same covalent object; the tie sits at `s = ±L/2` exactly where a scaffold crossover sits `5 bp` from a staple position; the **radial** link constant is unsourceable (`C-0208` §6) and is carried as two rungs |
| **Provenance** | [`gpd/results/T-294-the-tied-regrade-at-the-other-cross-section.json`](../results/T-294-the-tied-regrade-at-the-other-cross-section.json), written by [`tile/CrossSectionTiedRegradeStudy.kt`](../../src/main/kotlin/tile/CrossSectionTiedRegradeStudy.kt) (**new**) on [`tile/CrossSectionTiedRegrade.kt`](../../src/main/kotlin/tile/CrossSectionTiedRegrade.kt) (**new**). **NO SHARED SOURCE ON ANY NUMBER'S PATH IS EDITED** — `tile/HoneycombGrillage.kt`, `tile/HoneycombRasterTurnTies.kt`, `tile/CrossoverLinkResolution.kt`, `tile/HoneycombTiedRegrade.kt`, `tile/HoneycombFaceLattice.kt`, `tile/HoneycombTwoLengthRaster.kt`, `tile/FourLayerTile.kt`, `coupling/NonUniformCoupling.kt` and `coupling/DropoutRobustPlacement.kt` were **read, not edited**, so nothing `C-0154`, `C-0167`, `C-0180`, `C-0208` or `C-0212` published can move and no consumer re-run is owed. The one shared source touched is `structure/ResultInputs.kt`, which gains a `T_294` handle **by hand** (never through the generator, `CLAUDE.md`) and is **provably inert**: `ResultInputs.all` is read at eight sites, every one of them in `structure/ResultInputsTest.kt`. **30 gate-named tests written first and watched fail** — [`tile/CrossSectionTiedRegradeTest.kt`](../../src/test/kotlin/tile/CrossSectionTiedRegradeTest.kt) — of which ****two failed on their first real run and one of them is `CH-0282`** (§7); the other was the author's own assertion that the correction is bit-inert at `10 × 6`, which it is not, and a measurement replaced it** — and **mutation-tested afterwards** by [`tools/T-294-mutation-test.py`](../../tools/T-294-mutation-test.py), **20 mutations, **0 survivors**** over a subtracted baseline (`CH-0237`), registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts`. Result file **NOT byte-identical across two independent runs — `F14` FIRED**, at **3 leaves of 10 649**, all three a `reproductionDeparture` in the `upstream` block; **0 verdicts, 0 unclassified**, and no token any document quotes moves (§9). Both emissions are retained in [`gpd/data/T-294-reproducibility/`](../data/T-294-reproducibility/README.md) with a by-kind differ, and **no source was changed afterwards** (`C-0092`). A full `./gradlew test` on the final sources gives **3 615 tests in 204 classes, 0 failures, 0 errors**, run in a snapshot carrying this iteration's in-flight sibling sources; `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-result-path-references.py`, `check-kotlin-format-strings.py`, `check-cold-start-note.py`, `check-queue-vocabulary.py`, `P-31-harness-census.py --check`, `cli_guard.py --check`, `result-reader-census.py --check` and `check-result-file-hygiene.py` (base, `--prose`, `--departures`, `--saturated`) are all clean. |
| **Conditions** | T = 300 K, aqueous **2 mM MgCl₂**, `k_BT = 4.142 pN·nm`. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair per class every **21 bp**. Cross-section **`15 × 4`** (60 helices), block extent **116 bp = 39.44 nm** at `C-0151`'s `102 / 109` raster — **drawable at both cross-sections**, ladder phase **16**, inter-row offset **14 bp** — `edgeY` = **57.06 nm** (the plate convention `m · 3d/2`). `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm; **410 staple bonds** and **59 raster turn ties** (`firstAxialSign = +1`, `s = ±L/2`, 30 high and 29 low), 4 320 unknowns, half-bandwidth 243. Link: the **penalty** `1e4 pN/nm` (`C-0180`'s object) and `C-0208`'s resolved per-bond link at transverse `254.808095` with radial `254.808095` and `754.005141`. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the **gap-facing face only**; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Composite fractions **0.30** and **0.26** (`C-0116`), entering as `hingeStiffnessEnhancement` **9.65079217** and **8.49735322** — **four** layers, not six — plus the lattice's own **1.0**. Tie prestrain **0**: `C-0180` §4's coordinate is withdrawn (`CH-0240` upheld by `C-0190`) and is deliberately not mirrored |
| **Consumes** | [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — the `10 × 6` tied cells, the median-ratio band and the three free tiles, **read from its result file and reproduced**; [`C-0208`](C-0208-a-bond-link-is-two-mechanisms.md) (`T-310`) — the resolved link, its five rungs and its tightest cell, **reproduced**; [`C-0151`](C-0151-closing-raster-selection.md) (`T-245`) — the closure sweep, whose three residue pairs are **the same at both cross-sections**; [`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md) (`T-235`) — the committed `15 × 4` geometry at the 116 bp extent, **reproduced**; [`C-0154`](C-0154-honeycomb-grillage.md) (`T-253`) — the grillage, the bond census and the three `15 × 4` free tiles, **reproduced and corrected**; [`C-0175`](C-0175-drawable-raster-rim.md), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0103`](C-0103-path-count-at-fixed-geometry.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0109`](C-0109-four-layer-tile.md), [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md) |
| **Constrains** | **`C-0186` §1's deliberate leave is DISCHARGED**: the `15 × 4` comparison cell of `DECISIONS-FOR-NDI.md`'s decision 7 can now be annotated in one state. **Two challenges are raised**, `CH-0282` against the dishing decomposition and `C-0154`'s three `15 × 4` readings, and `CH-0283` against the scope of `C-0109`'s *"every coupled cell is worse than the uncoupled tile"*. **Nothing `C-0180`, `C-0208` or `C-0212` published is disputed** — every one of them is `10 × 6`, where the decomposition is exact |

---

## 1. The cheap bound, and it closed the branch the acceptance clause offered

`T-294`'s acceptance clause offers a fallback — *"or, failing that, the statement of why a
`15 × 4` raster's tie set cannot be built the way `10 × 6`'s was"*. It is closed out of a
committed result file with **no solve and no code**.

`C-0151`'s closure sweep (`T-245`) is exhaustive over the 441 residue pairs and it was run at
**both** 60-helix cross-sections. Its `closingResidueClasses` block returns the same three pairs
at each — `(7, 14)`, `(17, 3)`, `(18, 4)` — and `102 mod 21 = 18`, `109 mod 21 = 4`. So the
recommended raster is the third of them and it closes at `15 × 4` too:

| | `10 × 6` | `15 × 4` |
|---|---|---|
| closes / forced crossovers | yes / **0** | yes / **0** |
| block extent | 116 bp = 39.44 nm | **116 bp = 39.44 nm** |
| class-zero residue `b₀` | 5 | **5** |
| ladder phase / inter-row offset | 16 / 14 bp | **16 / 14 bp** |
| stations on the face | 55 of 60 | **82 of 90** |
| sparsest row | 5 | **5** |
| scaffold / M13 spare | 6 330 / 919 nt | **6 337 / 912 nt** |

**The whole comparison geometry transfers**, which is why all four of `C-0167`'s placements and
all four column counts exist at `15 × 4` (`F12`, and it did not fire).

The bound also decided the **method** in three places, and each is a refusal with a reason:

- **`C-0180` §4's prestrain deliverable is not mirrored.** Its coordinate is **withdrawn** —
  `CH-0240` upheld by `C-0190` — so mirroring it would reproduce a retired coordinate at half the
  run's cost.
- **`C-0190`'s replacement twist is not mirrored either**, because it reads `0 of 64` at `10 × 6`
  and cannot separate two blocks that are both zero, and its magnitude is published as a
  threshold rather than a value.
- **Two of `C-0208`'s five radial rungs are graded, not five**, because its own census column
  moves its tightest `p90` by **`0.00383`** over the whole ladder. That figure is measured at
  the *other* cross-section, so it is re-measured here: `F11` reports **`0.00437`**.

What it could not decide is the answer. `C-0154`'s uncoupled `15 × 4` readings are
`0.220064299`–`0.312237799` before any coupling and `C-0109`'s regularity reproduces at 64 of 64,
16 of 16 and 0 of 32 — but a free-tile ratio is a **ceiling** the coupled cells never reach
(`C-0180` §3) and `C-0109`'s statement is an empirical regularity rather than a theorem. What the
bound settled is the **size** of the margin: a factor of two, not the `0.198 %` and `0.426 %` the
`10 × 6` verdicts turn on. That is what licenses the two-rung reduction.

## 2. What does not transfer — derived from the block, asserted against the bond graph

| | `10 × 6` | `15 × 4` | how |
|---|---|---|---|
| staple bonds | **435** = 135 in plane + 300 through | **410** = **140** + **270** | the interface census, read off the assembled lattice |
| raster turn ties | **59** = 9 in plane + 50 through | **59** = **14** + **45** | `m(n−1)` and `m−1` |
| ties at the high / low rim | 30 / 29 | 30 / 29 | a property of the COUNT, and it does transfer |
| `hingeStiffnessEnhancement`, `f = 0.30 / 0.26` | 21.1851817 / 18.4938242 | **9.65079217** / **8.49735322** | `1 + f·S·Σy²/(nB)` at **four** layers |
| `edgeY` / free stroke | 38.04 / **5.27921926** nm | **57.06** / **3.5194795** nm | `m·3d/2`, and `p = F/(edgeX·edgeY)` |
| `T-5b`'s 0.10 in absolute nm | 0.527921926 | **0.35194795** | `0.10 ×` that tile's own stroke |

Every one of these is asserted as a named test at **both** cross-sections in one run, and every
raster turn is checked against the lattice's own **bond list** rather than against the raster
path — `C-0175`'s first-run defect, re-taken at the other cross-section.

**The stroke ratio is a theorem, not a measurement.** `edgeX` is shared, `edgeY = m·3d/2`, so the
free stroke goes as `1/m` and the ratio is `10/15` **exactly**, asserted to `1e−12`. That is the
premise of this row's own acceptance clause — *"same extent, same stations, same mandate, same
normalising stroke"* — and the lattice refuses the fourth of the four. Read as a statement about
the **convention** the clause is right and is what this study adopts; read as a statement about
the **number** it is false, and the consequence is §5.

## 3. The re-grade

| | `10 × 6` (`C-0180`, `C-0208`) | **`15 × 4`, this study** |
|---|---|---|
| tied cells clearing `T-5b` at the 90th percentile, penalty link | **2 of 64** | **0 of 64** |
| tied cells clearing it at the resolved link | 0 of 64 | **0 of 128** |
| untied cells clearing it, penalty link | 0 of 64 | **0 of 64** |
| the tightest coupled cell | 0.0995744767 (clears by 0.426 %) | **0.160538609** = **0.565012343 nm** (**1.60538609×** the tolerance) |
| the **uncoupled** tied block, `f = 0.30 / 0.26` | 0.0446459684 / 0.0467367262 — flat | **0.157228044 / 0.160659712** — **not flat** |
| the uncoupled tied block, no enhancement | 0.12738041 — not flat | **0.241288532** — not flat |

*(the `15 × 4` column is the CORRECTED dishing convention, §7; the standing convention is emitted
beside every one of them and is larger by `0.869539965`–`1.02643898`.)*

**The ordering is decided by the uncoupled tile and not by the coupling**, and it was decided
before this study: `15 × 4`'s free tile is outside `T-5b` at every enhancement in **both**
conventions. What the run adds is that it stays outside coupled, at every placement, every column
count, every distribution, both composite fractions, both tie states and three link states — and
by **how much**.

## 4. The tie's own worth, paired per realisation — and it does not transfer either

| | `10 × 6` (`C-0180`) | **`15 × 4`** |
|---|---|---|
| tie split | 50 through / 9 in plane | **45 / 14** |
| median of the per-realisation ratio | 0.902845544 – 0.988116016 | **0.930605361 – 0.995818215** |
| cells inside `C-0180`'s band | 64 of 64 | **48 of 64** |
| cells at which the ties are a dishing SOURCE (median ratio > 1) | 0 of 64 | **0 of 64** |
| flatness verdicts the ties move | **2 of 64** | **0 of 64** |

`F7` was declared open against `C-0180`'s band and it did not fire: **48 of 64** of the cells
fall inside it. The ties are worth systematically **less** at `15 × 4` — nearer one — which is
what a `45 / 14` split against a `50 / 9` one buys, on a face that carries fifteen beams instead
of ten. `C-0180`'s own lesson stands and is sharpened: the tie's worth is not a multiplier of the
free tile's ratio, and it is not a constant of the tie set either.

## 5. The ordering, unpaired, and in two conventions — `F10` FIRED

The two blocks carry **different path counts at the same column count** (15/30/45/75 against
10/20/30/50), so no realisation of one corresponds to a realisation of the other: the pairing is
by **column count**, which is `C-0142`'s own pairing, and the comparison is **unpaired** in the
field name. What is quoted is an ordering and a margin, never a paired ratio.

| reading | rows putting `15 × 4` worse |
|---|---|
| dishing over each tile's own stroke — the convention `T-5b` is written in | **128 of 128** |
| absolute peak dishing in nm — the convention §3's gap is written in | **118 of 128** |

**The ordering is not convention-independent**, and the reason is one line of arithmetic: the
stroke ratio is `2/3`, so a `15 × 4` cell reads *better in nm* wherever its fractional excess over
the matched `10 × 6` cell is under `1.5×`. `F10` was declared open, expected not to fire, and
fired.

**And it does not touch the decision it exists to serve.** Of the `128` matched rows,
**2** have either side flat, and the two readings disagree at **0**
of those: the reversal is confined to rows where **both** cross-sections are far outside the
tolerance. So decision 7's comparison cell can be annotated on the fractional reading — with the
absolute column beside it, because a reader who takes one alone will get the ordering wrong
somewhere.

## 6. `C-0109`'s regularity, at the first tile that is not flat — `CH-0283`

| | uncoupled reading | inside `T-5b` | coupled cells worse than it |
|---|---|---|---|
| `C-0109`, four-layer standing | 0.0577199433 | yes | all |
| `C-0142`, `10 × 6` smeared | 0.0240648102 | yes | 16 of 16 |
| `C-0180`, `10 × 6` tied | 0.0446459684 | yes | 64 of 64 |
| `C-0212`, `10 × 6` searched | 0.0448134881 | yes | 32 of 32 |
| **this study, `15 × 4` tied** | **0.157228044** | **no — 1.57× over** | **255 of 256** |

**The regularity survives, and it is at the edge of what this ensemble resolves.** The single
exception — *untied, `f = 0.26`, determined station lattice, `5 × 15 = 75` paths, rim-graded 5:1* —
reads `0.163948801` against its own uncoupled tile's `0.164599297`, which is **0.395 %** on a 90th
percentile of 4 000 draws. **It is not offered as a refutation**: the study's convergence
departure at the deciding cell is `2.2e−4`, but a percentile's *sampling* scatter at that sample
size is wider than four parts in a thousand. And it is not flat.

What **is** reported is the **scope clause**. `C-0109`'s own sentence reads *"every coupled cell
**of the same tile** reads worse under dropout"*, every quotation of it drops those three words,
and every tile it has been reproduced on has an uncoupled reading **inside** `T-5b` — where a
coupling can only spend flatness. This is the first tile outside it, and there the regularity is
marginal rather than clean. `CH-0283` asks for the clause, not for a reversal.

## 7. `F1` fired, and its firing is the finding — `CH-0282`

A uniform pressure on the tied `15 × 4` lattice gives a field that is **exactly uniform**: every
face beam reads `p/k_f` to `1e−10` relative, asserted as a named test. `HoneycombDeflection`
reports **0.0620506254** of that stroke as dishing.

The solve is not in question. `HoneycombDeflection` removes its rigid plane by **three
independent projections**, which is the least-squares fit iff the three modes are mutually
orthogonal. `⟨piston, tiltS⟩ = ∫s dA = 0` and `⟨tiltS, tiltY⟩ = 0` because the axial range is
symmetric; `⟨piston, tiltY⟩ = ∫y dA` over the face's tributaries vanishes iff the rooting-helix
positions are symmetric about their own datum — and a honeycomb face is **corrugated**, its gap
sequence `d, 2d, d, 2d, …`, which is palindromic **iff `m` is even**.

| | `m = 10` | `m = 15` |
|---|---|---|
| worst off-diagonal of the Gram, relative | **0.0** | **0.0358744468** |
| a uniform pressure's dishing, **standing** decomposition | not emitted; the named test bounds it at `< 1e−9` | **0.0620506254** |
| the same, **least-squares** | `< 1e−9` (named test) | **0.0** |

The parity itself is asserted at four raster-row counts rather than two — `m = 10` and `m = 14`
orthogonal, `m = 11` and `m = 15` not — but as the **orthogonality flag**, in a named test, and
only the two cross-sections this study grades have their Gram and their uniform-pressure reading
emitted. *(An earlier draft of this section carried dishing values at `m = 11` and `m = 14` taken
from a throwaway probe that no longer exists; they are struck rather than quoted, because a number
in a claim that is findable in no artifact is `CH-0199`'s class and this one could not even be
reconstructed.)*

**Every block this corpus has graded has `m = 10`.** The least-squares fit — a `3 × 3` Gram solve
in the face's own inner product, built **once per lattice** — reads **0.0**, and both
readings are emitted at every cell rather than one replacing the other (`C-0092`).

`C-0154`'s three `15 × 4` free tiles, which **both deliverables quote**, are the numbers this
reaches. Re-taken at `C-0154`'s own `112 bp` row and its own enhancements:

| enhancement | published | reproduced here | **corrected** | flat either way |
|---|---|---|---|---|
| 1.0 | 0.312237799 | 0.312237799 | **0.242196276** | no / no |
| 9.65079217 | 0.227177955 | 0.227177955 | **0.157167743** | no / no |
| 12.7228458 | 0.220064299 | 0.220064299 | **0.150056485** | no / no |

**No verdict moves** — at either cross-section, at any enhancement — which is why `CH-0282` is a
challenge against three numbers and not against `C-0154`'s conclusion. The corrected readings are
**smaller**, so the ordering this row exists to settle is if anything **strengthened**.

At `10 × 6` the two conventions agree to under one part in a thousand, and that residue is a
**second and far smaller** inconsistency: the class **fits** with `faceFunctional`'s owning-beam
reconstruction and **samples** with `evaluate`'s nearest-beam one, and on a corrugated face a
`3d/2` strip reaches past the midpoint of a `d` gap. It is recorded in `CH-0282` §5 and not
challenged.

## 8. `F5`'s declaration was internally inconsistent, and both readings are emitted

`F5` was declared as *"the tie set TRANSFERS — the `15 × 4` block carries `435` bonds and a
`50 / 9` tie split"*, marked **declared to FIRE**, with the expectation *"predicted `410` bonds
and `45 / 14`"*. Those two halves cannot both be met: a falsifier fires when its **statement** is
found true, and the statement is the transfer hypothesis, which the prediction says is false.

Following `CLAUDE.md`'s own rule for a mis-stated pre-registration — *retain the constant by name,
emit both verdicts, strike nothing* — the study emits both: **on the statement as written `F5`
does not fire**, and **the transfer hypothesis it names is REFUTED**, at `410` bonds and a
`45 / 14` split. The pre-registration is a diff at `8b65e94`, one commit before any study source
existed, so the inconsistency is on the record rather than repaired away.

## 9. Convergence, and the five gates

No verdict moves anywhere in this study, so the axes are taken on the cell nearest to
moving — the tightest tied cell, `0.160538609` of the stroke, `1.61×` the tolerance.

| axis | coarse → fine | departure | verdict survives |
|---|---|---|---|
| beam subdivisions | 1 → 2 | **`2.2e−4`** | yes |
| the dishing sample grid | 41 → 81 | **`9.7e−5`** | yes |
| the dishing sample grid | 81 → 161 | **`0.0`** | yes |

### `F14` FIRED, and the cause is this corpus's own most-repeated defect on a record name I coined

Two independent emissions of the same sources in the same snapshot differ at **3 leaves of
10 649**, classified by kind by [`gpd/data/T-294-reproducibility/diff.py`](../data/T-294-reproducibility/diff.py):

| kind | count | fields |
|---|---|---|
| **NUMBER** | **3** | `upstream/1/reproductionDeparture`, `upstream/3/…`, `upstream/5/…` |
| PROSE | 0 | |
| PROSE RENDERING OF A NUMBER | 0 | |
| **VERDICT** | **0** | |
| **UNCLASSIFIED** | **0** | |

`8.28434053e−10 → 8.28432953e−10`, `1.05274933e−9 → 1.05274955e−9`,
`1.20386157e−10 → 1.20385721e−10`. **Every one is a `reproductionDeparture`** — a difference of two
nearly equal solves, emitted at nine significant digits — which is exactly the field `CLAUDE.md`
records a JIT recompilation moves, and exactly the repair `C-0093`, `C-0101`, `C-0127`, `C-0129`
and `C-0131` each made in turn before `C-0138` moved the rule into the serialisation layer.

**It escaped that rule because `DEPARTURE_DIGITS_BY_KEY` is keyed on `reproductions` and
`convergence` records and this study coined a third: `upstream`.** The hygiene gate duly reports
*departure precision: clean*, correctly, on a predicate that cannot see the record. `CH-0193`'s
*grep a new study's records for any field that is a DIFFERENCE OR A RATIO of two nearly equal
quantities, and key it, before the first diff* — met by a study whose author had read it, on a
record name he invented four hours earlier.

**It is published rather than repaired.** Rounding the field to two significant digits now would
make the artifact reproducible and leave the committed emitter unable to reproduce the committed
file; and `C-0092`'s rule is that a repair must leave the defect measurable. Both emissions are
retained. **No token any document quotes moves** — the three that did are not cited anywhere, and
the `upstream` block's own maximum, `1.79e−9`, is carried by the one index that did **not** move,
so the verdict line is stable across both runs. The repair is a queue item, not this claim's.

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | both censuses asserted at **both** cross-sections in one run — 410 / 140 / 270 and 435 / 135 / 300, 59 ties at 45 / 14 and 50 / 9, 30 / 29 at the two rims — every tie asserted against the **bond graph**, and both range guards exercised at **both** ends (`C-0204` §8) |
| **2 — limiting cases** | an **empty** tie list bit-identical to the plain grillage in the bond site set, `assembleLoad` and the point-load dual; `radialLinkStiffness = null` bit-identical to the penalty lattice **entry for entry**; a single-layer block's enhancement exactly `1.0` at every fraction; a one-row block with no in-plane turn and a one-column block with none through the thickness; a `3 × 3` solve exact on a diagonal, refusing a singular matrix and **needing its pivoting** on a non-singular one |
| **3 — symmetry, conservation and the standing falsifier** | the uniform-pressure falsifier taken on the **field** (exactly uniform, `1e−10`) and on **both** dishing conventions — it FIRED on the standing one, which is `CH-0282`; the Loewner statement where it is a statement, the deflection **at** a unit point load; the stroke asserted equal to `p/k_f` and to `2/3` of `10 × 6`'s; the surrogate asserted equal to the **assembled** solve under the support forces it reports; `C-0104`'s trap taken on public quantities alone, at a **prestrained** `15 × 4` lattice |
| **4 — numerical convergence** | beam subdivisions `1 → 2` and the dishing sample grid `41 / 81 / 161`, on the **`p90` of the tightest `15 × 4` cell**; the result file **NOT byte-identical** — `F14` fired at 3 leaves of 10 649, `0` verdicts, `0` unclassified, cause named in §9; every same-quantity identity emitted as a **threshold and a boolean** |
| **5 — literature and upstream** | **10 reproductions, worst 1.2e−9** — `C-0180`'s three tied free tiles and its two recovered cells, `C-0208`'s tightest cell at its measured radial rung, `C-0146`'s committed `15 × 4` geometry at the 116 bp extent, `T-253`'s realised `15 × 4` enhancement, and `C-0154`'s six free tiles at its own 112 bp row |

## 10. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **The deliverable is an ordering with a validity range, not a recommendation.** It exists so
  that one comparison passage can be annotated in one state; nothing here re-opens the
  cross-section choice, the placement search, the distribution rule or the raster.
- **The cross-section comparison is UNPAIRED**, and the two conventions disagree at
  **10** of **128** rows.
- **Both blocks are graded on TRANSFERRED distributions.** `C-0212` shows `C-0208`'s `0 of 64`
  reverses to `22 of 32` once the distribution is **searched**, so this is an ordering of two
  blocks **under two fixed rules**. The cheap half of the searched question is stated and not
  run: `C-0212` measures the search worth `1.45251772×` at its own tightest cell and reads
  `0 of 32` searched cells beating the uncoupled tile — and `15 × 4`'s uncoupled tile is itself
  outside `T-5b`, so the transfer does not close a gap of **1.60538609×**. `T-330` carries it.
- `k_θ` is square-lattice-fitted; the tie is at `s = ±L/2` exactly; the lattice carries **no**
  across-helix parallel-axis term (so `D_⊥` is a lower bound) and Kirchhoff is not safe at these
  thicknesses (so `D_∥` is an upper bound).
- The dropout statistics are measured on a **single-layer Rothemund rectangle**; only the profile
  transfers, in nm, and the ensemble perturbs the **coupling** and never the block's own
  crossovers or its ties.
- The lattice carries **one** row length, the 116 bp extent both cross-sections share; the 102 bp
  interface window is not modelled.
- **The corrected dishing is a fit in `areaInnerProduct`'s inner product**, which is the one
  `peakDishing` samples in. It is self-consistent; it is not identical to a fit in
  `faceFunctional`'s owning-beam form, and at `10 × 6` the two differ by under one part in a
  thousand.

## 11. Open questions

- **Whether `HoneycombGrillage`'s own dishing decomposition should be repaired.** It is a shared
  source this study did not edit; every reading it has produced at an even raster-row count is
  unaffected, and `C-0154`'s three `15 × 4` readings are not. `T-330`.
- **Whether a distribution SEARCHED at `15 × 4`, as `C-0212` searched at `10 × 6`, recovers any
  cell.** The cheap bound says no and does not settle it.
- **What the scope clause of `C-0109`'s regularity costs elsewhere.** `CH-0283` and `T-331`.
- What the tie's true axial station is worth at a `45 / 14` split.
- **The departure gate's record qualifier.** `DEPARTURE_DIGITS_BY_KEY` is keyed on
  `reproductions` and `convergence`; this study coined `upstream`, and `F14` fired on exactly the
  three fields that qualifier cannot see. The repair is one entry, and it is deliberately **not**
  made here — see §9.
- Whether any other odd-`m` honeycomb block appears anywhere in this corpus's committed results.
