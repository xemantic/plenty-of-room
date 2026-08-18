# C-0095 — **YES, and it is not a permission — it is the DEFINITION of a raster turn.** Rothemund's odd-half-turn rule binds *"the distance between successive scaffold crossovers"*, and in a boustrophedon those are the **two ends of one row**: the 112 bp `C-0086` quantised **is** the crossover-to-crossover distance, so a crossover at the last base pair is what makes the row 112 bp long. Three headings, decided separately. **The geometry does not forbid it**: a duplex end carries **2** strand termini and the azimuthal quantum lets one base pair reach **one** neighbour, so it offers **1** crossover — and a boustrophedon demands **exactly 1** at every row end it uses, leaving **two** free, one per edge, which are the scaffold's own termini. **The software does not forbid it**: caDNAno's default rule is azimuthal and it *"permits the user to force crossovers between any two staple bases or between any two scaffold bases"*; cadnano 2.1 **automates** the raster turn; scadnano's API states that *"an xover is necessarily at an enpoint of a strand"*. **And it is published**: Rothemund's 24-helix rectangle is **288 bp = 18 column pitches EXACTLY**, so **both** vertical edges lie on the crossover lattice, and it folded **90 % well-formed**. His Supplementary Note S2 contemplates the case in as many words — *"even where a seam or edge lines up with the underlying crossover lattice"* — and reports that *"the last base pair does form and assumes a planar configuration"*. **The programme therefore carries `C-0090`'s ADMITTED reading, `0.0621469105`**, against the refused `0.168371808` at the same phase (**2.70×**) and `0.156510532` at the best refused phase (**2.52×**): the 38.08 nm tile is **inside** `T-5b`'s 0.10 and **flatter** than §3's nominal 40.0 nm tile at 0.0706145537. `CrossoverLayout.EDGE_MARGIN` is a numerical guard and must not be read as a physical assertion

| | |
|---|---|
| **Task** | [`T-161`](../tasks/T-161.md), raised by [`C-0090`](C-0090-buildable-raster-width.md)'s *Still open* item 2 |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **literature** (Rothemund 2006 main text and Supplementary Notes, the caDNAno paper, the scadnano API reference and the cadnano2 documentation, **all read directly**, with **twelve** recorded EuropePMC searches and a per-source read status in `gpd/data/T-161-sources/MANIFEST.md`) **+ logical** (a covalent count and a parity congruence, both closed form, the congruence asserted over `1 … 400` bp) **+ in-silico** (`C-0090`'s two readings **recomputed from its own result file**, and the upward station lattice re-checked under both conventions) |
| **Verdict** | **PASS, and the acceptance is met in full: the question is answered under all three headings, every load-bearing source carries a read status, and the placement verdict is stated beside it.** **The cheap bound was the whole answer and it cost a `grep`** — the primary source was already in the repository, fetched by `T-151`. **The question dissolves on the axis it is asked on.** A crossover is an **azimuthal** condition (the tangent point between backbones, `33.74°` per base pair) and *"the last base pair"* is an **axial** coordinate; they are independent, and the terminal base pair is in fact *less* constrained than an interior one, because nothing lies outboard of it. **The counting closes exactly.** `crossoverBudgetOfDuplexEnd` = **1** against `maximumTurnsPerRowEnd` = **1**, at both raster senses, with **two** free row ends — nothing to spare and nothing missing. **And `C-0086`'s odd-half-turn rule turns out to be the row-end column PARITY condition, read twice.** A boustrophedon's turns at one edge join interfaces of one parity and at the other the complementary one; a column serves the interfaces whose index parity matches its own; the two row-end columns are complementary **iff** the row is an odd number of column pitches — **iff** it is an odd number of half turns. Asserted over every row length from 1 to 400 bp: **identical, every time**. So at phase 8 or 24 the eight columns carry `C-0015`'s **56** interface crossovers as **14 scaffold raster turns + 42 staple crossovers**, and the 14 are one per interface — exactly `C-0086`'s independent count for the LINEAR topology. **The model then reproduces a structure it was not told about**: Rothemund's rectangle is an *even* pitch count, which puts the **same** parity on both edges, which is precisely what a seamed **double** raster demands and a boustrophedon cannot use. Raises [`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable.** The *permission* is not a model at all — it is a design rule and a folded structure, both read directly. What is **not** measured is the row-end crossover's **stiffness**, which this claim admits at an interior crossover's value and which Rothemund states in print is unrelieved and unresolved (`CH-0111`) |
| **Provenance** | `gpd/results/T-161-row-end-crossover.json`, produced by `anchoring.RowEndCrossoverStudyKt`; model in `src/main/kotlin/anchoring/RowEndCrossover.kt` (**new file** — `BuildableRasterWidth.kt`, `ScaffoldRouting.kt`, `CrossoverLayout.kt` and `Gen1Tile.kt` were **read, not edited**); **10 source records, 6 geometry records, 16 congruence records, 2 turn records, 2 inventory records, 4 readings, 1 verdict, 5 predicates, 6 falsifiers, 8 findings**; **19 gate-named tests in `src/test/kotlin/anchoring/RowEndCrossoverTest.kt`**; sources, driver and query log in `gpd/data/T-161-sources/` (`MANIFEST.md`, `queries.md`, `fetches.md`, raw search JSON) with the retained fetcher `tools/T-161-fetch-sources.py`; the result file **re-run through `tools/study.sh` and diffed byte-for-byte IDENTICAL** across two independent JVM runs (and again after a prose-only edit); `tools/verify.sh` **BUILD SUCCESSFUL in 20 m 31 s** on its own isolated tree with **one** concurrent agent's file dropped by `--drop-file` (`src/test/kotlin/stability/LargeRotationArmBranchTest.kt`, which `T-159`'s in-progress repair of `TwoSpringElastica.forceForDisplacement` is currently failing) and nothing else — the undropped run reports **exactly one** failure and it is that same sibling test, out of **2 309**, with **no failure in either of this task's classes** and `anchoring` importing no `stability` source; `tools/result-reader-census.py --emit` re-run and `--check` clean (85 studies, 60 direct + 27 transitive read edges, `T-153` now read by `RowEndCrossoverStudy.kt`); `tools/check-markdown-tables.py` clean over 290 files |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, 0.34 nm rise, 32/3 bp per turn, **16 bp** column pitch, **32 bp** per-interface spacing; along-helix width **38.08 nm** (112 bp, `C-0086`) against §3's nominal **40.0 nm** (118 bp); `C-0090`'s buildable 24-rise **8.16 nm** arm; `C-0022`'s solved collar at 2 mM, a 10 nm gap and 0.192 V, **as `C-0090` carried it** |
| **Consumes** | [`C-0090`](C-0090-buildable-raster-width.md) (`endOfRowColumnPhases`, `rasterColumnLayout`, `rasterUpwardSites` — **re-run as libraries**; its two readings **read from its result file**), [`C-0086`](C-0086-seamless-scaffold-routing.md) (`isOddHalfTurnSeparation`, `admissibleRasterRowLengths`, the 112 bp row and the LINEAR topology's 14 scaffold crossovers — **re-derived**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the parity rule, the 56/49 inventory), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice and the upward azimuth), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the two-termini counting theorem at a duplex end), [`C-0063`](C-0063-upward-root-placement.md) (the phases 8 and 24), [`T-71`](../tasks/T-71.md) (the measured phosphate radius), `Gen1Tile` |
| **Raises** | [`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md), against this claim's own consequence in `C-0090` and against `C-0009`'s uniform `k_θ` |

---

## The claim, in one line

**The question was posed as a permission and it is a tautology: the row length `C-0086` quantised is measured *between* the two crossovers the question asks about, so refusing them refuses the raster — and the one thing genuinely unknown is not whether the crossover exists but how stiff it is.**

---

## The conventions, restated rather than inherited

- Lengths **nm**; `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward**.
- **A row** is one duplex; **a plane** is `C-0055`'s 8 bp crossover plane; **a column** is the sheet's own 16 bp column lattice, of which the planes are every other member.
- **An interface** is the boundary between duplexes `b` and `b+1`, indexed `b = 0 … D−2`; a column of parity `p` serves the interfaces with `b mod 2 = p` (`CrossoverLayout`'s own rule).
- **The row end** is `x = ±edgeX/2`, the last base pair of the duplex.
  A crossover **at** the row end is one whose base-pair position is that terminal base pair.
- **A raster turn** is the scaffold crossover at which a boustrophedon passes from one row to the next.
- **Admitted** / **refused** are `C-0090`'s two end-of-row conventions, unchanged.

---

## Deliverable 1 — the cheap bound, which is a count and ran before any search

`CLAUDE.md`, from `C-0029`: *"A duplex END has exactly two strand termini … It is a count, and no
force field can add a third."*
A crossover consumes **one** strand continuation, so the naive budget is two.
But the azimuth is quantised at `33.74°` per base pair, and **one base pair points at one
neighbour** — so a terminal base pair offers exactly **one** crossover, and to one specific
neighbour. That is `crossoverBudgetOfDuplexEnd = 1`, and it does not rise if the lattice is given
eight azimuths instead of four.

**What a boustrophedon demands there is exactly one.**

| quantity | value | how |
|---|---|---|
| strand termini at a duplex end | **2** | `C-0029`, a count |
| crossovers one terminal base pair can carry | **1** | the azimuthal quantum, derived here |
| raster turns a boustrophedon puts on any one row end | **1** | `maximumTurnsPerRowEnd`, both senses, `D = 2 … 24` |
| row ends left free | **2** | the scaffold's own termini — one per edge on an **odd** row count, both on one edge on an even one |
| **demand exceeds budget?** | **NO** | `F3` did not fire |

**And the axis the question is asked on is the wrong one.** A crossover is an *azimuthal*
condition — the tangent point where a single phosphate bridges the interhelical gap, at the
measured `0.908638 nm` phosphate radius (`T-71`, 13 084 linkages) — and *"the last base pair"* is
an *axial* coordinate. They are independent. The terminal base pair is, if anything, **less**
constrained than an interior one: nothing lies outboard of it to clash with.

---

## Deliverable 2 — the literature answer, under three headings decided separately

### (a) Does the geometry forbid it? **NO.**

Deliverable 1, plus Rothemund's own observation, **read directly** from Supplementary Note S2:

> *"bases at the end of the helices are highly available for stacking against other DNA origami
> **which suggests that the last base pair does form and assumes a planar configuration**"*

### (b) Does the software forbid it? **NO — and one design language REQUIRES it.**

| source | statement, **read directly** |
|---|---|
| caDNAno (Douglas et al., *NAR* **37**:5001) | the default rule is *"only where the strand backbones arrive at points of closest proximity"* — **azimuthal**, not axial — and *"caDNAno **permits the user to force crossovers between any two staple bases or between any two scaffold bases**"* |
| cadnano2 documentation, v2.1 | *"**Automatic scaffold rasterization.** Adjacent strands that are added via a click-and-drag operation in the lattice view will be automatically resized and **connected via crossovers**"* — the raster turn is a one-click feature |
| scadnano Python API, `Domain.has_crossover_at` (translated from cadnano2's `strand.py`) | *"**An xover is necessarily at an enpoint of a strand**"* (`enpoint` is the source's typo, quoted verbatim) |

### (c) Has anybody published one? **YES — every Rothemund rectangle, and the arithmetic is his.**

Supplementary Fig. S19, the rectangle of Fig. 2b, **read directly**:

> *"27 turns wide at 10.666 bases / turn -> 288 nt"*, *"24 helices tall"*

`288 = 18 × 16`. **An exact whole number of column pitches** — so both vertical edges of that
structure lie **on** the crossover lattice, and its raster turns are crossovers at the last base
pair. Main text, **read directly**: *"The yield of well-formed rectangles was high (90 %, S = 40)"*.

**Rothemund contemplates the configuration explicitly**, Supplementary Note S2:

> *"However, at seams and edges this is not necessarily true, **even where a seam or edge lines up
> with the underlying crossover lattice**. At seams or edges, because DNA has a major and minor
> groove, a crossover involving staple strands is in tension with an adjacent crossover involving
> the scaffold strand. **Such a configuration of crossovers in tension has never before been used
> in DNA nanostructures.**"*
>
> *"How the strain is actually relieved is unknown, the final base pairs of each helix may be
> distorted. **Strain at seams or edges does not appear to cause any gross defects in the
> origami** … If, in the future, strain associated defects should be detected at edges, then one
> or two scaffold bases could be left unpaired and allowed to form a hairpin that should relax
> the crossover."*

**This is an answer and a named residual risk, and they are different things.**
The permission is settled; the *mechanics* of the edge crossover is not, and that is `CH-0111`.

### The negative half, recorded so it is falsifiable

**Twelve** EuropePMC searches, verbatim with `hitCount` and top hits in
`gpd/data/T-161-sources/queries.md`. `"DNA origami" AND "boustrophedon"` returns **0**;
`"DNA origami" AND "crossover" AND "helix terminus"` returns **0**;
`"DNA origami" AND "crossover" AND "last base"` returns **1**, on another subject.
**No source was found that forbids a crossover at a terminal base pair.** One paper falsifies
this and the queries are recorded so that it can.

---

## Deliverable 3 — `C-0086`'s rule and the row-end column parity are the SAME congruence

This is the result the task did not expect, and it is what upgrades *"the row-end column may be
the scaffold turn"* from a plausible identification to a theorem.

- A boustrophedon's turns at one edge join interfaces `0, 2, 4 …` and at the other `1, 3, 5 …` —
  **complementary parities**, by the alternation of the raster.
- A column serves the interfaces whose index parity matches its own (`CrossoverLayout`).
- The two row-end columns are `rowBasePairs / pitch` columns apart, so they are complementary
  **iff that count is odd**.
- `C-0086`'s rule says the row must be an **odd** number of half turns, which at 1.5-turn spacing
  is an **odd multiple of 16 bp** — an odd number of pitches.

> **`isOddHalfTurnSeparation(n) == rowEndColumnsAreComplementary(n)` for every `n` from 1 to
> 400 bp.** Asserted as a gate; the two are computed by unrelated arithmetic.

**So the design language's own scaffold rule *is* the sheet's crossover-lattice parity condition,
and a seamless raster width is exactly a width whose two row-end columns can be the turns.**

| row length | pitches | admissible seamless? | end columns complementary? | a boustrophedon's turns land on them? |
|---|---|---|---|---|
| 112 bp = **38.08 nm** (`C-0086`) | **7** | **yes** | **yes** | **yes**, at phases 8 and 24 |
| 118 bp = 40.12 nm (§3's nominal) | — | **no** | **no** (not a whole pitch count) | **no row-end column exists** |
| 128 bp | 8 | no | **no** | **no** — same parity at both edges |
| **288 bp** (Rothemund's rectangle) | **18** | no | **no** | **no** — and it is a seamed **double** raster, which demands exactly that |

**The last row is the check the model could have failed and did not.** A double raster's two outer
edges serve the **same** interface parity (asserted for every even row count from 2 to 24), and an
even pitch count supplies exactly that. The model reproduces the topology of a structure it was
never told about.

---

## Deliverable 4 — what the two row-end columns then contain

At 38.08 nm, phases 8 and 24, with the row end admitted:

| | phase 8 | phase 24 |
|---|---|---|
| columns | **8** | **8** |
| interface crossovers (`C-0015`'s 56) | **56** | **56** |
| of which **scaffold** raster turns (the two end columns) | **14** | **14** |
| of which **staple** crossovers (the interior six) | **42** | **42** |
| parity, negative-`x` end / positive-`x` end | **0 / 1** | **1 / 0** |
| upward stations, row end **admitted** | **52** | **53** |
| upward stations, row end **refused** | **52** | **53** |

**The 14 is `D − 1`, which is `C-0086`'s independent count of the scaffold crossovers a LINEAR
topology makes** — recovered here from the lattice instead of from the route.
Every column and site count in this table also appears in `C-0090`'s own `phaseCensus`
(`interfaceSites` 56 admitted / 42 refused, `upwardSites` 52 and 53) and agrees; that is a
consistency check across two result files, not a gate, because both are computed by the same
library functions.
**And the row-end column adds no station**, which is `C-0090`'s Deliverable 3 re-checked rather
than cited: an end plane has an even index and the upward azimuth needs an odd one. `F6` did not
fire.

---

## Deliverable 5 — the placement verdict, recomputed from `C-0090`'s own result file

Read out of `gpd/results/T-153-buildable-raster-width.json`, keyed on **all four** dimensions its
sweep varied (width, arm length, convention, phase), never transcribed.

| convention | phase | columns | best dishing / stroke | inside `T-5b`'s 0.10? |
|---|---|---|---|---|
| **ADMITTED — carried** | **8** | **8** | **0.0621469105** | **yes** |
| ADMITTED | 24 | 8 | 0.070693794 | yes |
| refused | 8 | 6 | **0.168371808** | **no** |
| refused | 24 | 6 | **0.156510532** | **no** |
| *reference* — §3's nominal 40.00 nm, `C-0063`'s optimum | 24 | 8 | 0.0706145537 | yes |

> **The programme carries `0.0621469105`.**
> Against the refused reading at the **same phase** that is **2.70925468×**; against the best
> refused reading **anywhere** it is **2.51839602×**. *Both are stated, because they are
> different comparisons and `C-0090`'s headline pair is the first one.*

**So the 38.08 nm tile is inside `T-5b`'s convention and is `12.0 %` FLATTER than §3's nominal
40.0 nm tile.** `CH-0101`'s *"unfavourable but small"* is wrong twice over: the width is
favourable, and the thing that decided it was a numerical guard.

**`CrossoverLayout.EDGE_MARGIN` is not a physical assertion.** Its own KDoc says why it exists —
*"a column exactly on the edge would seed a zero-length beam element"* — and says it is *"far
below the 0.28 nm closest approach any base-pair phase makes on a 40 nm tile, so it never decides
a column count that the physics does not already decide."* That sentence is **true at 40.0 nm and
false at 38.08 nm**, where it decides two of eight columns. It is a guard and it must be inset,
not removed; `C-0090` swept the inset over `0.05 / 0.17 / 0.34` nm and the answer moved 0.32 %.

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/anchoring/RowEndCrossoverTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a turn census is a **partition** of the interfaces and carries no length; unphysical arguments throw at **six** entry points | **PASS** |
| **2 — limiting cases** | the two-termini count and the one-crossover budget, the latter invariant under doubling the azimuth count; the demand is 1 at every row end for `D = 2 … 24`; reversing the raster sense swaps the two edges **and nothing else**; the parity partition at every `D`; the complementarity congruence at 112 / 16 / 48 / 144 / 128 / 288 / 118 bp; **`isOddHalfTurnSeparation == rowEndColumnsAreComplementary` over 1 … 400 bp** | **PASS** |
| **3 — symmetry and reconstruction** | the lattice's **own** parity list (read off `rasterColumnLayout`) against the closed-form congruence — two independently written quantities; exactly one raster sense matches, at both phases; an **even** pitch count matches **neither** sense; the two end columns carry one crossover per interface (14 = `D − 1`); admitting the row end adds **no** upward station, worst departure **`0.0`** | **PASS** |
| **4 — convergence / invariance** | the parity verdict invariant under the numerical inset over `0.01 … 0.34` nm; the congruence holds at **every** admissible seamless width to 400 bp, not just 112 | **PASS** |
| **5 — literature and upstream** | Rothemund's rectangle at 288 bp = 18 pitches and its half-row at 9 (odd, his own progressive condition); the double raster's two outer edges serving one parity at every even `D`; `C-0090`'s **0.0621469105** and **0.168371808** recomputed from its result file to `< 1e-9`; the column counts 8 and 6; the same-phase and best-phase ratios distinguished | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | a primary design-rule source **forbids** a crossover at the terminal base pair | **NO** | none found in twelve recorded searches, in Rothemund 2006 and its SI, in the caDNAno paper, or in scadnano — which states the **opposite** |
| **F2** | the two row-end columns of a 112 bp row carry the **same** parity | **NO** | complementary at both phases; and the congruence is `C-0086`'s own rule over 1 … 400 bp |
| **F3** | a boustrophedon demands **more than one** crossover at some row end | **NO** | demand **1**, budget **1**, at both raster senses |
| **F4** | the recomputed readings differ from `C-0090`'s published pair | **NO** | 0.0621469105 and 0.168371808, to `< 1e-9` |
| **F5** | Rothemund's own built structures have **no** row end on the crossover column lattice | **YES** | **fired favourably.** The 24-helix rectangle is **288 bp = 18 pitches**, both edges on the lattice, **90 % well-formed**. The falsifier was written to catch *"unprecedented"* and found *"built, imaged and counted"* |
| **F6** | admitting the row-end column changes the **upward station lattice** | **NO** | 52 and 53 stations under **both** conventions, at phases 8 and 24 |

**What was not anticipated.** The task was formulated as a permission question with a literature
sweep behind it, and the sweep was almost unnecessary: the primary source was already in the
repository, and the answer is contained in the definition of the quantity `C-0086` had already
quantised. The genuine surprise is Deliverable 3 — that Rothemund's scaffold rule and the sheet's
crossover-lattice parity are the **same congruence** — which nobody had noticed in the two claims
that used them side by side, and which turns the identification of the row-end column with the
raster turn from an argument into a theorem.

---

## Validity range

- **TRL 1–3.** The *permission* is a design rule and a folded structure, not a model. Everything else here is a count or a congruence.
- **The row-end crossover is admitted with an INTERIOR crossover's stiffness.** `C-0009`'s grillage gives every crossover the same dihedral spring `k_θ` and the same vertical link, and 14 of the 56 at this width are scaffold raster turns tying two duplex **ends**. Rothemund states in print that the strain there is unrelieved and that *"how the strain is actually relieved is unknown"*. **That is [`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md), and this claim raises it against its own consequence.**
- **`C-0090`'s two readings BRACKET the soft-crossover case rather than being two conventions.** Refusing the column removes the dihedral spring *and* the vertical link *and* the node; admitting it gives all three at full value. A row-end crossover of intermediate stiffness therefore lies between **0.0621469105** and **0.168371808**, and both ends of that bracket are already computed. Whether the dishing is monotone in `k_θ` is **not** shown here.
- **Nothing is re-solved at 38.08 nm.** `C-0022`'s collar is `C-0090`'s, carried; no stiffness, stroke, bias or layer number is touched. This claim moves a **convention**.
- **Ke et al. 2009 is `not found`** — EuropePMC's full text 404s and the PMC article page serves a browser-challenge stub. Nothing here rests on it beyond the 8 bp plane lattice `C-0055` already carries.
- **Rothemund's remedy is a length and it is priced only as arithmetic.** *"One or two scaffold bases could be left unpaired"*: unpaired bases are **not base pairs**, so the 112 bp duplex row and the whole column lattice are untouched and the cost is `15 × 1…2 = 15–30` nt of scaffold on 1 680, i.e. **0.9–1.8 %**, absorbed without comment by `C-0086`'s 5 569 nt M13 remainder. **Whether a design language counts the unpaired base inside or outside the row length is not settled here**, and neither reading changes the duplex.
- **The parity argument assumes the boustrophedon `C-0086` recommends.** A seamed tile has scaffold crossovers at its seam as well, and its admissible widths are `C-0086`'s open item 4.
- **`maximumTurnsPerRowEnd` is a count of raster turns, not of all crossovers at a row end.** A staple crossover at a terminal base pair is permitted by everything read here and is simply not needed: the two end columns are fully populated by the scaffold.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| Rothemund's rectangle: 288 nt per row, 24 helices, 90 % well-formed (S = 40) | 288 / 24 / 90 % | **CITED, READ DIRECTLY, MEASURED** — Rothemund 2006 Suppl. Fig. S19 and main text, `gpd/data/T-151-sources/` |
| the odd-half-turn rule; the edge-strain passage; the unpaired-base remedy; *"the last base pair does form"* | verbatim | **CITED, READ DIRECTLY** — Rothemund 2006 main text and Suppl. Note S2 |
| caDNAno's default rule and its force-crossover clause | verbatim | **CITED, READ DIRECTLY** — Douglas et al., *NAR* **37**:5001, `PMC2731887` |
| scadnano's *"an xover is necessarily at an enpoint of a strand"* | verbatim | **CITED, READ DIRECTLY** — scadnano Python API reference, fetched for this task |
| cadnano 2.1's automatic scaffold rasterization | verbatim | **CITED, READ DIRECTLY** — cadnano.org, fetched for this task |
| the phosphate radius | 0.908638 nm | **`T-71`, MEASURED HERE** on 13 084 crystallographic linkages; **cited, not re-measured** |
| the azimuthal quantum, interhelical distance, rise, bp per turn, crossover spacing | 33.74°/bp, 2.69 nm, 0.34 nm, 32/3, 16 bp | **CITED** (Ke et al. 2009, Rothemund 2006) / **CITED, MEASURED** (SAXS, Fischer et al. 2016) |
| `C-0090`'s two readings and the nominal-width optimum | 0.0621469105 / 0.168371808 / 0.156510532 / 0.0706145537 | **`C-0090`, READ FROM ITS RESULT FILE and keyed on all four sweep dimensions** |
| the 112 bp row and the LINEAR topology's 14 scaffold crossovers | — | **`C-0086`, CONSUMED AND RE-DERIVED** (`isOddHalfTurnSeparation` re-run; the 14 recovered from the lattice) |

Everything else — the covalent budget of a duplex end, the raster-turn census and its free ends,
the parity congruence and its identity with `C-0086`'s rule over 1 … 400 bp, the double-raster
cross-check, the 14/42 scaffold/staple split, the station-lattice re-check, and the two verdict
ratios — is **derived here in code**.

## Still open — named, not answered

1. **How stiff is a row-end crossover?** `CH-0111`. It ties two duplex **ends**, and Rothemund says its strain is unrelieved and unresolved. The bracket is already computed (0.0621 to 0.1684); what is missing is where inside it the answer sits. **This is a modelling task (an oxDNA or all-atom edge crossover, or a `k_θ` sweep on the two end columns), not a specification question.**
2. **No sixth NDI question is added, and that is a deliberate outcome.** `T-161` was queued as *"carry it to NDI beside `C-0086`'s scaffold question if the literature is silent"*. The literature is **not** silent — it is explicit, in the primary source, about a folded structure. What is left is mechanics, and mechanics belongs in this repository.
3. **The seamed tile's row-end columns.** Derived here for the boustrophedon only; a seam adds scaffold crossovers at a third contour, and `C-0086`'s open item 4 already carries the width question.
4. **Whether the dishing is monotone in the row-end crossover's stiffness.** Assumed nowhere and used nowhere, but it is what would let the bracket be read as an interval.
5. **`C-0022`'s collar at 38.08 nm is still carried, not re-solved** — `C-0090`'s own open item 1, unchanged by this claim.

## Challenges

**Raises [`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md)**, against the stiffness this claim's verdict gives the 14 row-end crossovers.

**[`C-0090`](C-0090-buildable-raster-width.md)'s *Still open* item 2 is CLOSED**, in the affirmative,
and its own reading of the question is corrected in one respect: it describes the two conventions
as a choice — *"the argument for refusing it is that no crossover has ever been drawn at the last
base pair of a duplex"* — and **one has, in the paper this programme's whole lattice comes from.**

**None stands against this claim.** The four ways it would fail:

1. **A primary source that forbids a terminal-base-pair crossover.** Twelve recorded searches and four primary documents found none; one paper reverses this and the queries are recorded so that it can.
2. **A reading of Supplementary Fig. S19 in which the rectangle's 288 nt is not the crossover-to-crossover row length** — for instance if the figure's *"288 nt"* counted the scaffold including an unpaired turn. Then the 18-pitch coincidence is an artefact and the *built* precedent weakens to *"contemplated in print"*, which is Supplementary Note S2 and is still not a prohibition.
3. **A row-end crossover so soft that the flatness lands outside `T-5b`.** That is `CH-0111`, and its bracket is stated rather than hidden.
4. **An interhelical distance of 2.73 nm or a bp-per-turn other than 32/3.** Neither touches this claim: the congruence is integer arithmetic in base pairs and carries no length at all.
