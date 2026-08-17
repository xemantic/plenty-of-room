# C-0086 — **YES, and a seam is a PARITY ON A TREE rather than a fabrication convention.** Crossovers join only *adjacent* duplexes, so a single-layer sheet's row-adjacency graph is the path `P_D` — a tree — and a closed walk on a tree traverses every edge an **even** number of times: a **fully folded circular** scaffold therefore gives every row two segments, i.e. one seam crossing every row. Brute-forced, `P_D` carries **2 Hamiltonian paths and ZERO Hamiltonian cycles** at every `D` from 3 to 12. **So a seam needs BOTH premises, and the Gen-1 tile fails the second anyway**: the sheet takes **1 680** of M13's **7 249** nt. The seamless routing is the plain boustrophedon, and Rothemund built it twice — a **26-helix square** that *"had no vertical reversals in raster direction, **required a linear scaffold**"*, and, in his very first origami experiment, an **8-helix** raster on a **circular** M13 with two thirds unfolded. **Rothemund's own staggered seam is NOT the remedy** — solved exactly it still costs **5 of 34** stations against a straight seam's 6–12. And a seamless raster **quantises the tile's width at 32 bp**: the buildable widths are 16, 48, 80, 112, 144 bp and **40.0 nm is not among them**

| | |
|---|---|
| **Task** | [`T-151`](../tasks/T-151.md), raised by [`C-0081`](C-0081-seam-weave-congruence.md)'s *Still open* item 1 |
| **Leaf** | **`A8.2`** (the plan and weave model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (a graph-parity theorem, with the Hamiltonian counts **brute-forced** rather than asserted) **+ in-silico** (the two routes constructed and validated; the staggered-seam optimum solved exactly by a three-row dynamic program on `C-0076`/`C-0081`'s own plane lattice; `C-0081`'s sweep reproduced from its station planes) **+ literature** (Rothemund 2006 main text and **Supplementary Notes fetched for this task** and read directly, `gpd/data/T-151-sources/`, with a manifest and eight recorded EuropePMC queries) |
| **Verdict** | **PASS, and the acceptance is met in its positive half: a seamless routing exists, it is the plain boustrophedon, and it is conditional on a SPECIFICATION this programme has never made.** **The cheap bound is the whole answer and it is a theorem.** A crossover joins only **adjacent** duplexes, so the row-adjacency graph of a single-layer sheet is the path `P_D`, a **tree**; a closed walk on a tree traverses every edge an **even** number of times, and every edge must be traversed at least once for the scaffold to reach every row — so a **fully folded circular** scaffold gives every row **at least two** segments, i.e. exactly Rothemund's seam. A **linear** scaffold needs only a Hamiltonian **path**, and a **circular** scaffold that is not fully folded closes through its own unpaired remainder, which is an edge the row graph does not have. Brute-forced over `D = 3…12`: **2 Hamiltonian paths, 0 Hamiltonian cycles**, every time; at `D = 2` a cycle exists, which is where the theorem starts. **The theorem predicts Rothemund's own record and he states it in one clause each**: his 26-helix square *"had no vertical reversals in raster direction, **required a linear scaffold**"*, while the rectangles' folding path *"is compatible with a circular scaffold and **leaves a 'seam' (a contour which the path does not cross)**"*. **And the Gen-1 tile is already inside the demonstrated regime.** At the nearest buildable width it takes **1 680** nt of M13's **7 249** — **4.31×** too long — so a remainder is unavoidable on that scaffold, and Rothemund's **first** origami experiment was precisely an 8-helix seamless raster on a **circular** M13 with two thirds unfolded, which *"could easily bridge the corners without deforming the rectangle"* and, he adds, *"long, unfolded single-stranded sections of the scaffold do not adversely affect folding"*. **Three things the programme had not counted.** (i) The **remainder** is a body: 5 569 unpaired nucleotides is a **33.3 nm** ideal coil carrying **1.66×** the sheet's own backbone charge, beside a tile whose edge load `C-0022` solved with nothing there; a purpose-built 1 680 nt scaffold removes it, and a purpose-built *circular* one needs only a **67 nt** return loop. (ii) **Rothemund's own staggered seam is not the remedy**: solved exactly over the three-row coupling `C-0081`'s mechanism implies, the best staggered seam still costs **5 of 34** stations against the best straight seam's **6** and the worst's **12** — worth one station, where seamlessness is worth all 34. (iii) A seamless raster has **only progressive** scaffold crossovers, so Rothemund's *"the distance between successive scaffold crossovers must be an odd number of half turns"* binds the **row length**, which must be an **odd multiple of the 16 bp crossover spacing**: 16, 48, 80, 112, 144 bp — and §3's **40.0 nm = 117.6 bp is not on the list**. The nearest is **112 bp = 38.08 nm**, 4.8 % below. That is [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md). **`C-0081`'s sweep reproduces from its station planes alone**: 8 candidate planes, 6–12 affected stations, 34 of 34 on an odd plane. Raises [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED** — but the two precedents **are built structures**, read directly, and the theorem is a proof rather than a model. The motif the routing carries (`C-0055`'s free lever on one crossover) is unchanged and remains **undemonstrated**. |
| **Provenance** | `gpd/results/T-151-scaffold-routing.json`, produced by `anchoring.ScaffoldRoutingStudyKt`; model in `src/main/kotlin/anchoring/ScaffoldRouting.kt` (**new file — `SeamWeave.kt` and `WeaveExclusionWidth.kt` were not edited**); **11 graph records, 3 topology records, the admissible-width table, 6 scaffold-budget records, 10 seam-cost records, 6 reproductions, 5 predicates, 5 falsifiers, 8 findings**; **25 tests, 20 of them gate-named, in `src/test/kotlin/anchoring/ScaffoldRoutingTest.kt`**; the sources fetched for this task into `gpd/data/T-151-sources/` with a `MANIFEST.md` (per-URL HTTP status and content check) and `queries.md` (**8 recorded EuropePMC REST searches**, 8 s apart, all HTTP 200); `tools/verify.sh` **BUILD SUCCESSFUL in 15 m 08 s** — the whole suite on its own isolated tree, with one concurrent agent's failing test dropped by `--drop-file` (`src/test/kotlin/stability/RecommendedElementFoldTest.kt`, `T-149`) and nothing else; the undropped run reports **exactly one** failure and it is that same sibling test, with no failure in either of this task's classes. The result file was re-run through `tools/study.sh` and diffed **byte-for-byte identical**. `tools/result-reader-census.py` re-emitted and clean; `tools/check-markdown-tables.py` clean over the whole 259-file corpus |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, **32/3 bp per turn**, 1.5-turn (**16 bp**) crossover spacing, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-upward-root-placement.json`; ssDNA at **0.57 nm** per nucleotide (the inextensible-model minimum, the conservative end for a loop that has to *reach*) and a **2.10 nm** zero-force Kuhn length |
| **Consumes** | [`C-0081`](C-0081-seam-weave-congruence.md) (`seamPlanesWithin`, the seam's cost mechanism and its 6–12; **the sweep reproduced, not tabulated**), [`C-0076`](C-0076-weave-exclusion-width.md) (`WeaveProfile`, `weavePlaneIndex`, `isWeaveNode` — **re-run as libraries**), [`C-0063`](C-0063-upward-root-placement.md) (the 34 stations, **read from its result file**), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, `M13_SCAFFOLD_NUCLEOTIDES`, `SQUARE_LATTICE_BASE_PAIRS_PER_TURN`), [`C-0023`](C-0023-two-sided-coupling.md)'s `SsDnaTether` (the Kuhn length and the contour per nucleotide), [`C-0022`](C-0022-tile-edge-load-profile.md) (the edge load the remainder would sit beside, **CITED**), `Gen1Tile` |
| **Raises** | [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md), against `Gen1Tile.EDGE_X` and every plan claim written on a 40.0 nm width |

---

## The claim, in one line

**A scaffold seam is not something Rothemund chose, it is what a circular molecule has to do to close a walk on a tree — so the question *"can the tile be folded without one"* is a question about the scaffold and not about the tile, and the answer is yes twice over, with a built precedent for each.**

---

## The conventions, restated rather than inherited

- Lengths **nm**; the sheet is 15 duplexes at the SAXS **2.69 nm**, rise **0.34 nm**, square lattice, **16 bp** (1.5-turn) crossover spacing; `x` runs **along** the helices, `y` **across** them.
- **A row** is one duplex; **a scaffold segment** is a maximal run of scaffold inside one row.
- **A seam** is Rothemund's own: *"a contour which the path does not cross"* — equivalently, the locus where a row carries two scaffold segments meeting end to end. **Seamless** means every row carries **exactly one**.
- **A plane** is `C-0055`'s 8 bp crossover plane, `C-0076`'s and `C-0081`'s coordinate.
- **The row-adjacency graph** has a node per duplex and an edge per interface. On a single-layer sheet it is the **path** `P_D`; on a tube it would be the cycle `C_D`, and that difference is the whole theorem.

---

## Deliverable 1 — the theorem, computed rather than asserted

A crossover joins only **adjacent** duplexes. So the graph a scaffold path lives on is `P_D`, and `P_D` is a **tree**.

> **A closed walk on a tree traverses every edge an even number of times.**
> Every edge must be traversed at least once for the scaffold to reach every row.
> Therefore under a **fully folded circular** scaffold every row is entered **at least twice**.

| `D` | Hamiltonian paths | Hamiltonian cycles | min segments/row, linear | min segments/row, circular |
|---|---|---|---|---|
| 2 | 2 | **2** | 1 | **1** |
| 3 | 2 | **0** | 1 | **2** |
| 4 | 2 | **0** | 1 | **2** |
| … | 2 | **0** | 1 | **2** |
| 12 | 2 | **0** | 1 | **2** |

The counts are **brute-forced** by exhaustive walk enumeration, not read off a formula, and asserted as a gate-2 test
at every `D` from 3 to 12. If a path graph carried a Hamiltonian cycle the seam would not be forced and nothing here
would hold; **that is falsifier F1 and it did not fire.**

**A seam therefore needs BOTH premises — a circular scaffold AND full utilisation — and dropping either removes it.**

---

## Deliverable 2 — the three specifications, each with a built precedent

| specification | segments/row | seams | scaffold crossovers | seamless? | precedent, **READ DIRECTLY** | what it costs |
|---|---|---|---|---|---|---|
| **LINEAR** | **1** | **0** | **14** | **YES** | the **26-helix square**: *"The square had no vertical reversals in raster direction, **required a linear scaffold**, and used 2.5-turn crossover spacing"* | Rothemund's own measured price for BsrBI digestion: the star folded **63 %** well-formed on untreated circular scaffold and **11 %** on the linearised one, which he attributes to *"strand breakage occurring during BsrBI digestion or subsequent steps to remove the enzyme"*. A synthetic or PCR scaffold of the right length avoids the digestion entirely |
| **CIRCULAR, FULLY FOLDED** | **2** | **1** | 30 | **no** | every Rothemund rectangle: *"the folding path shown in Fig. 1b is compatible with a circular scaffold and **leaves a 'seam' (a contour which the path does not cross)**"* | `C-0081`'s **6–12 of 34** stations off the weave node, and its amplitude bracket restored |
| **CIRCULAR, WITH REMAINDER** | **1** | **0** | **15** | **YES** | Rothemund's **FIRST** origami experiment, the 8-helix third-square: *"a **circular** M13mp18 scaffold DNA was used rather than a linearized one, because the corners of the rectangle were close enough that the unfolded portion of the M13mp18 scaffold DNA could easily bridge the corners without deforming the rectangle"* | the remainder itself — see Deliverable 3 |

> **The theorem's own falsifier is Rothemund's record, and the record agrees in his own words.** The one shape he
> describes as having no vertical reversals is the one he says *required* a linear scaffold; the folding path he
> describes as *compatible with a circular scaffold* is the one that *leaves a seam*. **F3 did not fire.**

---

## Deliverable 3 — the scaffold budget, and the price nobody had counted

| quantity | nucleotides | note |
|---|---|---|
| the sheet at the nearest buildable width, 15 × 112 bp | **1 680** | the tile's whole scaffold demand |
| the sheet at the nominal 40.0 nm, 15 × 118 bp | 1 770 | **not buildable as a seamless raster** — see Deliverable 4 |
| **M13mp18, circular** | **7 249** | **4.31×** what the tile needs, so a remainder is unavoidable on this scaffold |
| M13mp18 after BsrBI digestion | 7 176 | **READ DIRECTLY**: *"While 7,176 nt remained available for folding"* |
| **the unpaired remainder at 112 bp** | **5 569** | a **33.3 nm** ideal coil (2.10 nm Kuhn, 0.57 nm/nt), carrying **1.66×** the sheet's own backbone charge |
| the return loop a purpose-built **circular** scaffold would need | **67** | `14 × 2.69 nm` over 0.57 nm/nt — the whole seamless closure, outside the sheet |

**Rothemund's own verdict on the remainder is favourable and it is measured**: *"No remainder strands were used on
the ∼2/3 of M13mp18 DNA left unfolded. Apparently long, unfolded single-stranded sections of the scaffold do not
adversely affect folding"* — and, usefully here, *"the unfolded scaffold appeared to **prevent stacking** at
adjacent vertical edges."*

**But this device is not an AFM sample.** The remainder is a 33 nm polyanion sitting in a 5–10 nm electrode gap
whose load `C-0022` solved with nothing there, and whose rim charge is `P-14`'s open question. That is a cost this
claim names and does not price. **The clean answer is a purpose-built 1 680 nt scaffold**, which is standard
practice today and which makes the linear route free of the digestion Rothemund paid for.

---

## Deliverable 4 — a seamless raster quantises the tile's WIDTH at 32 bp

Rothemund's fundamental constraint, **read directly**:

> *"for the scaffold to raster progressively from one helix to another and onto a third, the distance between
> successive scaffold crossovers must be an **odd number of half turns**. Conversely, where the raster reverses
> direction vertically and returns to a previously visited helix, the distance between scaffold crossovers must be
> an **even** number of half-turns."*

A boustrophedon has **only** progressive crossovers, and its successive scaffold crossovers are the **two ends of
one row** — so the constraint binds the **row length**. At the 1.5-turn (16 bp) spacing this sheet is built on,
three half-turns are 16 bp, so an odd number of half-turns is an **odd multiple of 16 bp**:

| row length [bp] | width [nm] | admissible? |
|---|---|---|
| 16 | 5.44 | **yes** |
| 48 | 16.32 | **yes** |
| 80 | 27.20 | **yes** |
| **112** | **38.08** | **yes — the nearest to 40.0** |
| **118** | **40.12** (the nominal 40.0 rounds here) | **NO** |
| 144 | 48.96 | **yes** |

**The step is 32 bp = 10.88 nm, not the rise**, and §3's 40.0 nm falls between two admissible widths.
The nearest buildable seamless tile is **38.08 nm**, **4.8 %** narrower — which is [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md).

---

## Deliverable 5 — the fallback, priced: Rothemund's staggered seam is worth one station

If a fully folded circular scaffold were mandated, the seam is forced but its **shape** is not. Rothemund,
**read directly**:

> *"while most seams presented here are vertically aligned (for simplicity and convenience in design) … it is
> possible to create **staggered seams** (as E. Winfree has suggested) so that staple strands naturally cross and
> bridge the seam vertically … A small instance of staggered seams occurs in the smiley face design."*

`C-0081`'s cost mechanism is that the junctions absent at a plane straighten the duplexes and pin the **interfaces**
at an extremum. A station on row `b` sits between interfaces `b−1` and `b`, which are moved by duplexes `b−1`, `b`
and `b+1` — so it survives only if **none of those three rows** splits within one plane of it. That three-row
coupling is solved **exactly** by a dynamic program over consecutive row pairs (64 states, 15 rows):

| seam | stations off the node, of 34 |
|---|---|
| the **worst** straight seam (planes −8, +2) | **12** |
| the **best** straight seam (planes −6, 0) | **6** |
| **the best STAGGERED seam** | **5** |
| **no seam** | **0** |

**The stagger is worth one station.** `C-0081`'s cost is not a property of the seam's straightness; it is a property
of a tile **3.68 weave periods wide carrying 34 stations**, and no contour through it can miss them all.
**F5 fired, and it fired usefully**: the remedy Rothemund offers is not the remedy, and seamlessness is.

**And it is not free either.** Two of the eight recorded EuropePMC searches returned modern corroboration, both
**read directly** from the fetched full texts:

- Dey et al., *"DNA Origami Design: A How-To Tutorial"* (`PMC11419732`, 2021) treats the stagger as a live design
  pattern — *"the horizontal interdigitated design moves the scaffold crossovers of the seam back and forth so that
  they are no longer contiguous, broadening the distribution of fold distances"* — and confirms the seam is
  universal in 2-D circular-scaffold origami: *"The scaffold routing for these two designs places a seam that
  bisects the rectangular footprint along a cardinal axis."*
- *"Design principles for accurate folding of DNA origami"* (`PMC11621765`, 2024) reports that changing a seam's
  stagger changes the **folding yield**: *"in our redesign of the 10 × 10 block, we modified the original highly
  staggered scaffold seam, resulting in a favorable ∆∆G loop … contributing to the substantial increase in folding
  yield observed for this structure."*

Neither is a fourth escape from the theorem; both are evidence that a staggered seam is a **thermodynamic** design
variable as well as a geometric one, so a 15-row stagger chosen to dodge stations would be chosen against the
criterion those papers optimise.

---

## Deliverable 6 — what a seamless routing buys, named claim by named claim

| claim | quantity | under a seam | seamless | moves? |
|---|---|---|---|---|
| **`C-0076`** the node congruence | 34 of 34 on a node | **22–28** | **34** | **YES — restored in full** |
| **`C-0076`** the amplitude bracket | coefficient exactly **zero** | **restored at full strength**, 1.2–1.75 nm | **zero again** | **YES — `C-0076`'s headline comes back** |
| **`C-0081`** the worst across-row clearance | at the measured girth | **0.12272 nm** at Snodin's amplitude, **−0.0023** at Bai's | **0.87272 nm** | **YES — the clash is removed** |
| **`C-0081`** the plan margin | `M = p − d − L` | unchanged at every seam | unchanged | **no** — it carries no weave coordinate |
| **`C-0022`**/`P-14` | the rim charge | — | **a 5 569 nt, 33 nm polyanion** unless the scaffold is purpose-built | **a NEW exposure, named here** |
| **`Gen1Tile.EDGE_X`** | the tile width | 40.0 nm | **not a buildable raster width**; 38.08 nm is | **`CH-0101`** |

---

## The five verification gates

Executed as **25 tests, 20 of them gate-named**, in `src/test/kotlin/anchoring/ScaffoldRoutingTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a nucleotide count is an integer; a loop contour halves when the contour per nucleotide doubles; a radius of gyration goes as `√N`; the half-turn test is invariant under a common rescaling of `(base pairs, bp per turn)`; unphysical arguments throw at **nine** entry points | **PASS** |
| **2 — limiting cases** | **THE THEOREM — 2 Hamiltonian paths and 0 Hamiltonian cycles at every `D` from 3 to 12, brute-forced**; at `D = 2` a cycle exists and a circular scaffold needs one segment per row, which is where the theorem starts; the double raster carries exactly one seam at every `D` from 2 to 20 and closes; the boustrophedon is seamless and connected at every `D` from 1 to 20; a linear scaffold and a circular one with a remainder are seamless, a fully folded circular one is not | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — exactness** | asserted rather than measured, because **every quantity here is an integer or a closed form**: the Hamiltonian counts are exhaustive; the admissible-width list is monotone, every member passes its own predicate, and the 200 bp list is a subset of the 400 bp one | **PASS** |
| **5 — literature and upstream** | **6 reproductions, all exact**: `C-0081`'s **8** candidate seam planes, its **6** best and **12** worst straight-seam costs, `C-0063`'s **34** stations, `C-0076`'s congruence (34 of 34 on an **odd** plane) and the tile's **3.68** weave periods — all recovered from the station planes and `C-0081`'s own `seamPlanesWithin`, none transcribed; and Rothemund's two precedents asserted as tests | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **A route's segment count equals the walk parity the theorem predicts**, for both routes, at 15 rows — two independently written quantities.
2. **The staggered optimum is invariant under reflecting the sheet** (the station rows reversed): same affected count.
3. **Every zero-cost staggered assignment returned really clears every station** — re-checked against the three-row rule directly, not against the dynamic program's own bookkeeping.
4. **A stagger is never worse than the best straight seam**, asserted on an independent station set; and on a set where no stagger can clear anything the routine returns the honest cost rather than a bad assignment.

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | a Hamiltonian **cycle** exists on the 15-row path graph | **NO** | zero at every `D` from 3 to 12; two at `D = 2`, which is where the theorem starts |
| **F2** | no seamless routing exists at this aspect ratio under **any** specification | **NO** | two of three specifications are seamless, and both have a built precedent |
| **F3** | Rothemund's own record contradicts the theorem | **NO** | his seamless square *required* a linear scaffold and his seamed rectangles are circular and fully folded — the theorem's prediction, in his own words |
| **F4** | no admissible seamless width lies near 40 nm | **NO, and it half-fired** | 112 bp = **38.08 nm** is 4.8 % away, which is close — but the **nominal 40.0 nm is not admissible at all**, which the falsifier was not written to catch and which is `CH-0101` |
| **F5** | no staggered seam clears every station, so a mandated seam has no remedy | **YES** | the best staggered seam still costs **5 of 34**, one better than the best straight one. Rothemund's stagger is not the remedy; seamlessness is |

**What was not anticipated:** the task was formulated as a routing search and the answer turned out to be a
one-line graph theorem — *a closed walk on a tree traverses every edge twice* — that explains **why** Rothemund's
rectangles have seams and his square does not, and that he confirms in two clauses written fifteen years before
this programme asked. The second surprise is that the constraint which does bind is not topological at all: it is
the **odd half-turn** rule, and it says the tile's nominal width cannot be rastered.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged and upstream.
- **The theorem is about SEGMENTS, not about staples.** It says a fully folded circular scaffold must enter every row twice; it does not say the resulting seam must be *straight*, *unbridged*, or at any particular place. Deliverable 5 prices the staggered case; the bridged/unbridged distinction is Rothemund's and is not modelled here (his Fig. 3e rectangle used an **unbridged** seam, *"held together only by stacking interactions"*).
- **The seamless route is validated as a ROUTE, not as a fold.** Connectivity, segment count and the odd-half-turn rule are checked; base-pair-level staple design, the crossover *phase* the scaffold turns land on, and the twist correction Rothemund's program applies are not. A routing that passes here can still fail a caDNAno-level design.
- **The odd-half-turn constraint is derived for a BOUSTROPHEDON only.** A double raster's segments have different separations and its admissible widths are not derived here.
- **`C-0081`'s cost mechanism is inherited whole**, including its plateau model (*"the conservative reading"*), and the staggered-seam cost is computed **inside that model**. A staggered seam is a *different* weave problem — each duplex loses its event at its own plane — and the three-row rule used here is the natural reading of `C-0081`'s parity theorem, not a re-solve of the weave. A re-solve could only make the staggered case worse or equal, since it removes the assumption that the damage is confined to one plane's neighbourhood.
- **The remainder's radius of gyration is an IDEAL-CHAIN estimate** at the zero-force Kuhn length, with no excluded volume, no electrostatic swelling and no interaction with the tile or the electrode. It is an order-of-magnitude statement about a body, not a solved conformation.
- **The 63 % → 11 % yield figures are Rothemund's, for the STAR, and he attributes the drop to the digestion rather than to the linear topology.** They are the only measured price of a linear scaffold read here, and they do not transfer to a synthetic scaffold.
- **No flatness, stiffness, force, stroke or bias number is touched.** This claim moves a registration and a specification.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the seam's definition, the raster constraint, the square's linear scaffold, the third-square's circular one, the staggered-seam alternative, the 4-T remedies | verbatim | **CITED, READ DIRECTLY, fetched for this task** — Rothemund, *Nature* **440**:297 (2006) and its Supplementary Notes, `gpd/data/T-151-sources/` (`MANIFEST.md`, HTTP 200 on each) |
| M13mp18 circular / linearised length | 7 249 / **7 176** nt | **CITED** (`C-0055`) / **CITED, READ DIRECTLY** |
| the star's yield, circular against linearised | 63 % (S = 43) / 11 % (S = 70) | **CITED, READ DIRECTLY, MEASURED** |
| the seam as a universal 2-D pattern, and interdigitation as a live design variable | verbatim | **CITED, READ DIRECTLY** — Dey et al., *"DNA Origami Design: A How-To Tutorial"*, `PMC11419732` (2021), fetched for this task |
| a staggered seam changes the folding **yield** | verbatim | **CITED, READ DIRECTLY** — *"Design principles for accurate folding of DNA origami"*, `PMC11621765` (2024), fetched for this task |
| ssDNA Kuhn length and contour per nucleotide | 2.10 nm, 0.57 nm/nt | **CITED, MEASURED** (Chen et al., *PNAS* **109**:799) via `C-0023`'s `SsDnaTether` |
| interhelical distance, rise, bp per turn, crossover spacing | 2.69 nm, 0.34 nm, 32/3, 16 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| the 34 stations, phase 24, the 8 seam planes and the 6–12 cost | — | **`C-0063`/`C-0081`, CONSUMED AS DATA and REPRODUCED** |

Everything else — the tree-parity theorem and its brute-forced Hamiltonian counts, the three topology verdicts, both
constructed routes and their validation, the admissible-width list, the whole scaffold budget, the remainder's coil
size and charge ratio, the return-loop length, the staggered-seam dynamic program and its optimum, and the five
falsifier verdicts — is **derived here in code**.

## Still open — named, not answered

1. **Which scaffold the Gen-1 device uses is a SPECIFICATION QUESTION and this programme has never asked it.** Linear or circular, M13 or purpose-built, and at what length. The whole verdict is conditional on it, and the answer is a purchase order rather than a calculation. **Carried to NDI.**
2. **The remainder as a body in the gap.** If M13 is used, a 5 569 nt, 33 nm polyanion sits beside the tile. Its effect on `C-0022`'s edge collar and on `P-14`'s rim charge is unevaluated, and it is the one place a seamless routing could cost something the seamed one does not.
3. **The tile at 38.08 nm.** `CH-0101` names the width; what it does to `C-0063`'s 34 stations, `C-0015`'s phase census and every plan margin in the branch is unevaluated. The area falls **4.8 %**, so §3's 100 pN over a smaller footprint is a higher pressure.
4. **The seamed tile's own admissible widths.** Derived here only for the boustrophedon.
5. **Whether a staggered seam is even buildable at 15 different planes.** Rothemund's own instance is *"a small 2-helix seam"* in the smiley face; a 15-row stagger is not demonstrated, and it is refused here on cost before it is refused on precedent.

## Challenges

**Raises [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md)** against
`Gen1Tile.EDGE_X` and every plan claim written on a 40.0 nm width.

**[`C-0081`](C-0081-seam-weave-congruence.md)'s *Still open* item 1 is CLOSED in the affirmative**, and its
recommendation — *"the Gen-1 tile should be routed without a seam, which costs nothing"* — is upheld with one
correction: it costs a **scaffold specification**, and on M13 it costs a remainder.

**None stands against this claim.** The five ways it would fail:

1. **A published seamless raster on a single-layer sheet folded from a fully used circular scaffold.** That would mean the row graph is not a tree, and the only way it is not is if scaffold crossovers can join **non**-adjacent duplexes.
2. **A demonstration that a linear scaffold cannot fold a 15-duplex sheet** for a reason outside the routing — nuclease sensitivity, end fraying, or the digestion yield being intrinsic rather than procedural.
3. **A measurement that a long unpaired remainder DOES adversely affect folding**, against Rothemund's own statement that it does not.
4. **A weave model in which a staggered seam's damage does not couple across three rows.** Then the stagger might clear every station and the fallback becomes a remedy.
5. **A scaffold-crossover rule that is not the odd-half-turn one** — a different lattice, or a design language that permits an unpaired base or two at a turn to absorb the phase. Rothemund himself offers that escape for edge strain (*"one or two scaffold bases could be left unpaired"*), and it would remove `CH-0101` at the price of a defect he introduced it to fix.
