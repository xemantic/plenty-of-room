# C-0055 — The square lattice offers a helix **four** crossover azimuths at 8 bp and a single-layer sheet occupies **two**, so the unused sites are real, are **out of the sheet plane**, and are in **better** register than the ones the sheet uses: `C-0054`'s exclusivity holds at the site and its **pigeonhole does not bind** — the hinge budget goes from **42 of 56, sheet severed** to **52–60 with every interface intact** — but the placement only goes **25 → 34**, because an upward line belongs to one duplex and its roots are therefore **twice as sparse**

| | |
|---|---|
| **Task** | [`T-119`](../tasks/T-119-unused-junction-site.md), raised by [`C-0054`](C-0054-consumed-crossover-sheet.md)'s *Challenges* item 1, which names it *"the most valuable single falsifier of this claim"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Verification type** | **literature** (the square lattice's crossover rule read directly from the paper that states it, 62 recorded queries in fifteen families for the motif) **+ logical** (helical-phase arithmetic on cited constants — no mesh, no fitted parameter) **+ in-silico** (`C-0015`'s `CrossoverLayout`, `C-0053`'s exact interval scheduler and `C-0039`'s elastica **re-run as libraries**) |
| **Verdict** | **PASS, and the answer is (a): the site exists, it is published geometry, and `C-0054`'s pigeonhole falls — while its conclusion about §3's 45 survives for a different reason.** Ke et al. (*JACS* **131**:15903, 2009, **read directly**) state the square lattice's rule verbatim: *"each double helix has up to four nearest neighbors … Every 8 bp, that staple strand is positioned to cross over to one of its four neighbors … adjacent helices share crossovers every 32 bp."* A **single-layer** sheet has two neighbours, so it occupies **two of the four azimuths** and leaves the other two empty — and `8 bp × 33.75°/bp = 270.0°` **exactly**, so the empty pair points **out of the sheet plane**, which is precisely where a flexure arm wants to go. **The unused site is also in BETTER register than the used one**: at B-DNA's preferred 10.5 bp/turn the departure is linear in the offset, so the 8 bp out-of-plane site is off by **4.286°** against **8.571°** for the sheet's own next in-plane crossover — a ratio of exactly **2**. **The censused inventory is 161–176 sites of which the sheet builds 49–56: it occupies 27.8–33.1 %, under a third at every one of the 32 phases**, and the in-plane half of the census **reproduces `C-0015`'s 56/49 exactly at every phase** from a completely independent construction. **So `C-0054`'s two premises separate: exclusivity AT A SITE is upheld and its INVENTORY premise is false.** The hinge ceiling is not `56 − 14 = 42` with a severed tile but **52 upward hinges at `C-0054`'s own phase and 60 at the best one, with every one of the 56 interface crossovers retained and the sheet in ONE piece at every count** — [`CH-0068`](../challenges/CH-0068-the-hinge-inventory-is-not-the-sheets-own.md). **`C-0053`'s count moves too, and much less: 25 → 34.** The host is no longer cut up, so what binds is a **new** constraint — an upward line belongs to **one** duplex where an interior row sees **two** interfaces, so its roots sit at **10.88 nm** against an arm demanding **11.82 nm**, and three arms per row become two. **§3's 45 still does not place** — for the root pitch, not for the host's survival. **And the motif is only half-published**: the site and a crossover on it are the elementary step of square-lattice multilayer origami, but a **free lever** held to a single-layer sheet by one crossover at that azimuth was **not found** in 62 recorded queries, and Ke et al. themselves report the 8 bp staple break it forces as a **yield** cost. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The **geometry** is published and read directly; the **motif** is not demonstrated, and `C-0028`'s and `C-0029`'s findings are unchanged. |
| **Provenance** | `gpd/results/T-119-unused-junction-site.json`, produced by `anchoring.UnusedJunctionSiteStudyKt`; model in `src/main/kotlin/anchoring/UnusedJunctionSite.kt`; **4 cheap bounds, 4 azimuth records, 32 phase censuses, 15 row records, 17 count records, 5 budget readings, 34 explicit placements, 5 sensitivities, 3 convergence records, 13 upstream reproductions, 6 literature provenance records, 7 predicates**; **25 tests, 22 of them gate-named, in `src/test/kotlin/anchoring/UnusedJunctionSiteTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with three concurrent agents' mid-TDD files dropped by `--drop-file` (`anchoring/BackboneTorsionTest.kt`, `coupling/NonUniformCouplingStudy.kt`, `coupling/NonUniformCouplingTest.kt`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, 0.34 nm rise, designed twist **32 bp per 3 turns = 33.75°/bp**, preferred **10.5 bp/turn**; crossover planes at **8 bp**; `C-0039`'s `E5a1` arm placed at `C-0017`'s 33.3333 pN/nm mandate at §3's **acceptable** 3 nm |
| **Consumes** | [`C-0054`](C-0054-consumed-crossover-sheet.md) (the exclusivity argument, the pigeonhole, the 42 — **the claim this one tests**), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`maximumArmsInRow`, `placeHingeArms`, `hostSheetAfterArms`, `HingeSite`, the 11.82 nm demand, the 43 and the 25 — **re-run as libraries**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the 32 bp phase period, the parity rule, the 56/49 inventory — **re-derived from the azimuth and asserted equal**), [`C-0040`](C-0040-hinge-line-census.md) (the definition of a hinge line, `k_θ` as the interhelical dihedral spring, the four-per-interface census), [`C-0039`](C-0039-two-spring-elastica.md) (`elasticaArmForStiffness` — **re-run as a library**), [`C-0034`](C-0034-guided-arm-anchorage.md)/[`C-0029`](C-0029-perpendicular-junction-routing.md) (the `A2` anchorage, and the standing literature survey this one extends), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0068`](../challenges/CH-0068-the-hinge-inventory-is-not-the-sheets-own.md), against `C-0054`'s pigeonhole and the 42-crossover ceiling that follows from it |

---

## The claim, in one line

**`C-0054` proved that a crossover cannot be a hinge and an interface at once and then assumed that the hinges must come out of the sheet's own crossovers; the square lattice gives every helix four azimuths and a single-layer sheet fills two, so the sheet is standing on an inventory three times its own, the two unused azimuths point exactly out of its plane, and they are less strained than the ones it uses — the connectivity ceiling is not a ceiling, and what replaces it is a root pitch.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, angles **degrees**, forces **pN**, stiffness **pN/nm**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` **along** the helices, `y` **across** them in the sheet plane, `z` **normal** and positive **upward** — away from the grafted layer, which is below the tile.
- **A junction site** is a base pair at which a strand's backbone faces a neighbour position of the
  lattice. It is a property of the **lattice**; it is *used* when a design builds a crossover on it.
- **The azimuth naming is Ke et al.'s own** — `NORTH` (0 bp), `WEST` (8), `SOUTH` (16), `EAST` (24).
  Here `NORTH`/`SOUTH` are the in-plane neighbours `±y` and `EAST`/`WEST` are `+z`/`−z`.
- **An upward hinge** roots an arm on an `EAST` site: the arm is a duplex **added above** the sheet
  at the interhelical distance, not a length **cut out of** it.

---

## The cheap bounds, which ran first and decided the verdict

| | bound | value | against | ratio | what it settled |
|---|---|---|---|---|---|
| **1** | the azimuth advance over one crossover plane, `8 × 33.75°` | **270.000°** | 270° | **1.000** | **exactly a quarter turn** — the unoccupied azimuths point out of the sheet plane. **Falsifier 1 did not fire** |
| **2** | the register departure of the **unused** site at the preferred 10.5 bp/turn | **4.286°** | **8.571°** (the sheet's own next in-plane crossover) | **0.500** | the unused site is **less** strained than the used one, because the departure is linear in the offset. **Falsifier 2 did not fire** |
| **3** | the upward inventory against the interfaces it would have to spare | **60** | 14 | **4.29** | the escape is larger than the constraint it removes. **Falsifier 4 did not fire** |
| **4** | the **upward root pitch** against an interior row's in-plane one | **10.88 nm** | 5.44 nm | **2.00** | **the new binding constraint**, and it is the price of the same fact that removes the connectivity cost |

> Bounds 1 and 2 are the whole literature answer in four arithmetic operations, and bound 4 is the
> answer to *"and what does that buy?"* before any placement runs. **Bound 4 is the surprise: the
> escape and its price are the same sentence** — an upward line belongs to **one** duplex, which is
> why it consumes no interface and why its roots are twice as sparse.

---

## Deliverable 1 — the four azimuths, and which two the sheet leaves empty

| azimuth | offset | design azimuth | in the sheet plane? | occupied? | departure from the **nearest occupied** site at 10.5 bp/turn |
|---|---|---|---|---|---|
| **`NORTH`** | 0 bp | **0.0°** | yes, `+y` | **OCCUPIED** | 0.000° |
| **`WEST`** | 8 bp | **270.0°** | **no, `−z`** | **empty** | **4.286°** |
| **`SOUTH`** | 16 bp | **180.0°** | yes, `−y` | **OCCUPIED** | 0.000° |
| **`EAST`** | 24 bp | **90.0°** | **no, `+z`** | **empty** | **4.286°** |

The three published statements this rests on, **read directly**:

> *"In the square lattice, each double helix has up to four nearest neighbors and is designed to
> link to each with antiparallel strand crossovers. … Every 8 bp, the staple strand of a given
> double helix completes a rotation of 8 bp/(10.67 bp/turn) = 0.75 turns. Thus every 8 bp, that
> staple strand is positioned to cross over to one of its four neighbors … Thus adjacent helices
> share crossovers every 32 bp, and the positions of the crossovers are restricted to periodic
> intersection or 'crossover' planes, labeled from i to iv, spaced at 8 bp intervals."*
>
> *"The crossovers in i and iii sectional slices are parallel to the xz-plane, while the crossovers
> in ii and iv sectional slices are parallel to the yz-plane."*
>
> *"…initial geometrical parameters of 2.0 nm diameter, 0.34 nm per bp rise, and 33.75° per bp
> average twist (or 32 bp per 3 turns) … compared to the preferred 34.3° per bp or 10.5 bp per
> turn."*
>
> — Ke, Douglas, Liedl and Shih, *JACS* **131**:15903 (2009), `PMC2821935`

The second quotation is the load-bearing one: **the four planes fall into two orthogonal families**,
and a single-layer sheet is one row of the lattice, so it can build on **one** family only. The
per-helix 16 bp against the per-interface 32 bp that `C-0015` and `C-0040` both quote is exactly the
statement that the sheet visits its two azimuths alternately — and it is silent about the other two.

**caDNAno's honeycomb rule is the same arithmetic at three neighbours** and is reproduced here as a
control: *"potential staple-crossover positions occur every seven base pairs, or two-thirds of a
turn"*, repeating *"every 21 base pairs"* at 10.5 bp/turn (Douglas et al., *NAR* **37**:5001, 2009,
`PMC2731887`, read directly). Three neighbours at 120°, four at 90°: the family is one rule.

---

## Deliverable 2 — the census, complete over all 32 phases

| phase | planes | **interface (built)** | outward-facing | **upward `+z`** | downward `−z` | **total** | **used** | `C-0054`'s ceiling | **upward ceiling** |
|---|---|---|---|---|---|---|---|---|---|
| 0–2, 14–18, 30, 31 | 15 | 49 | 7 | **60** | 60 | **176** | **27.8 %** | 35 | **60** |
| 3–5, 11–13, 19–21, 27–29 | 14 | 49 | 7 | 52 or 53 | 53 or 52 | 161 | 30.4 % | 35 | 52–53 |
| **6–10, 22–26** | 15 | **56** | 8 | **52 or 53** | 53 or 52 | **169** | **33.1 %** | **42** | **52–53** |

Four things, and three of them are not restatements:

1. **The in-plane column of this census reproduces `C-0015`'s inventory exactly — 56 at the ten
   eight-column phases and 49 at the other twenty-two — from a construction that knows nothing
   about columns or parities**, only about a base-pair azimuth. It is asserted as an equality of
   *position sets*, at every one of the 32 phases, not as an equality of counts. **That is what
   makes the unused column worth quoting.**
2. **The sheet occupies under a third of its own lattice at every phase**, and the bound is not an
   accident: a helix has four azimuths per 32 bp, the two in-plane ones are **shared** with a
   neighbour and so count once each, and the two out-of-plane ones are its own — three site
   equivalents per duplex per period, of which the sheet builds one. **1/3 is the asymptote and
   33.1 % is how close a 40 nm tile gets to it.**
3. **The phase that maximises the upward inventory is never the phase that maximises the
   interface inventory.** The upward lattice is the in-plane one shifted by 8 bp, so the ten
   eight-column phases `6–10, 22–26` carry 52–53 upward sites while `0–2, 14–18, 30, 31` carry
   **60** and only 49 interface crossovers. **This is a new trade in a design variable three
   claims have already swept** — and where `C-0040` and `C-0053` each landed on `C-0015`'s ten
   phases for reasons of their own, this construction lands on the **complementary** ten, which is
   the first time the phase has had two optima that disagree.
4. The **outward-facing** in-plane azimuths of the two edge duplexes — 7 or 8 sites — are unused
   too, and are **not** counted as usable: they point off the sheet at nothing.

---

## Deliverable 3 — what `C-0054`'s budget becomes

| reading | ceiling | interfaces intact? | components | clears §3's 45? |
|---|---|---|---|---|
| **`C-0054`** — hinges drawn from the sheet's own crossovers | **42** | **no** — each left with exactly one | 1 at 42, **4 at 45**, 15 at 56 | **no** |
| **`T-119`** — hinges on the upward azimuth, at `C-0054`'s own phase | **52** | **yes, all 56** | **1 at every count** | **yes** |
| **`T-119`** — the same, at the phase that maximises the upward inventory | **60** | **yes, all 49** | **1** | **yes** |
| both unoccupied azimuths, if a downward arm were admissible | 120 | yes | 1 | yes | 
| **the placement, which is what actually binds** | **34** | **yes** | **1** | **no** |

&nbsp;&nbsp;&nbsp;&nbsp;**The pigeonhole does not bind. Nothing in it is wrong — the arithmetic `56 − 14 = 42` is
exact and the exclusivity argument behind it is upheld — but it counts the wrong inventory, and on the
right one the constraint it expresses is discharged entirely: no interface crossover is spent, no
interface empties, and the sheet is one body at every hinge count the lattice can supply.**

The downward row is reported and **not adopted**: `−z` points into the grafted layer, and an arm
swinging there is a different device. Halving the out-of-plane inventory is the price of that, and
it is taken.

---

## Deliverable 4 — what `C-0053`'s count becomes, and the new constraint

`C-0039`'s elastica re-run as a library at seventeen path counts, `C-0053`'s exact per-row interval
schedule re-run on the **upward** root lattice, best of all 32 phases:

| paths | arm [nm] | demand `arm + d` [nm] | **upward placed** | in-plane placed | **duplexes bonded, upward** | duplexes bonded, in plane |
|---|---|---|---|---|---|---|
| 20 | 6.641 | 9.331 | **53** | 52 | **15** | 15 |
| **25** | 7.236 | 9.926 | **45** | 43 | **15** | **15** — `C-0053`'s design point |
| 30 | 7.770 | 10.460 | 45 | 43 | **15** | 14 |
| **34** | **8.164** | **10.854** | **45** | 43 | **15** | **10** — **the self-consistent count** |
| **35** | **8.259** | **10.949** | **30** | 43 | 15 | 8 |
| 42 | 8.882 | 11.572 | 30 | 43 | 15 | **0** |
| **45** | **9.131** | **11.821** | **30** | **43** | **15** | **0** |

&nbsp;&nbsp;&nbsp;&nbsp;**The self-consistent upward count is 34, against `C-0053`'s 25 — 1.36× — and the
host is untouched at every one of them.**

**The cliff at 34/35 is exactly the root pitch.** An upward line's sites sit at **10.88 nm**; three
arms fit in a row only while `arm + d ≤ 10.88`, which is `arm ≤ 8.19 nm`, which is `n ≤ 34`. One
base pair of arm past that and every row drops to two, and the whole array drops 45 → 30. **The
transition is a lattice fact with no free parameter in it**, and it is the reason the escape is worth
1.36× rather than 1.8×.

**Why the upward roots are sparser is the same sentence as why they are free.** An in-plane crossover
belongs to **two** rows — which is why spending it empties an interface, and why an interior row sees
its **two** bounding interfaces at a 16 bp = 5.44 nm combined pitch. An upward site belongs to
**one** duplex: nothing is emptied, and nothing is shared, so the pitch is the bare 32 bp. **The
per-row problems are therefore independent, and the construction meets the independent per-row bound
identically at every one of the 32 phases** — where `C-0053`'s in-plane schedule has to remove the
crossovers a previous row has consumed and run in both row orders to reach its bound at all. That
identity is asserted as a gate-3 test rather than observed.

---

## Deliverable 5 — the sensitivities, and the one that reaches §3's 45

| axis | reading | upward sites | **self-consistent count** |
|---|---|---|---|
| exclusion width | the 2.0 nm steric diameter rather than 2.69 nm SAXS | 60 | **41** |
| exclusion width | **the SAXS 2.69 nm, as adopted** | **60** | **34** |
| exclusion width | the 2.73 nm square-lattice SAXS value | 60 | 33 |
| duplex count | a **16**-duplex host — `C-0053`'s own named escape | 64 | **34** |
| **tile edge** | a **49.25 nm** edge — `C-0053`'s other named escape | **75** | **45** |

Two findings here, neither anticipated:

1. **More duplexes buy nothing.** `C-0053` names 16 duplexes as a way to reach 45 in plane; on the
   upward lattice it moves the count by **zero**, because the constraint is a pitch **along** `x`
   and a row is a row however many there are. The escape and the constraint are on different axes.
2. **A 1.23× longer tile reaches §3's 45 exactly** — 45 arms, all fifteen duplexes bonded, all 56
   interface crossovers retained. **That is the first configuration in this programme at which
   §3's own path count places on a host that survives it**, and it is a **specification** question
   (§3 fixes 40 × 40 nm), not a physics one — the fourth time this branch has raised one.

---

## Deliverable 6 — the price, which is published and is not geometric

Ke et al. report, of exactly the pattern an upward hinge creates:

> *"In our default design strategy, some staple breaks must be implemented between crossovers 8 bp
> apart. For the two-layer and three-layer structures, very few such breaks need to be
> incorporated. However, for the six-layer design, many such breaks must be used. **We observed
> significantly lower yield for these structures. Introducing these breaks may be destabilizing for
> the structure.** … For the 8 × 8 design, we avoided the implementation of such staple breaks by
> omitting many crossovers in the core of the block. For this design, we observed a high yield of
> well-folded structures."*

A single-layer sheet's staple crosses over every 16 bp; an upward crossover sits **8 bp** from its
neighbours on the same helix, so building one splits a 16 nt domain into **two of 8**. At the
self-consistent 34 arms that is **68** such domains. **This is the cost of the escape and it is a
folding-yield cost, not a geometric one** — which is the honest form of the answer, and it is the
`CLAUDE.md` pattern of *"the closest published precedent is often a failure named in one clause"*
landing in the body of the paper rather than in a supplement.

**The scaffold is not a constraint**: the sheet is 1770 bp and 34 arms of 8.164 nm add 816, for
**2586 of M13's 7249 nt — 35.7 %**.

---

## The five verification gates

Executed as **22 gate-named tests** (of 25) in `src/test/kotlin/anchoring/UnusedJunctionSiteTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with three concurrent agents'
mid-TDD files dropped.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the twist is `32/3` bp/turn and `33.75°/bp` to `1e−15`; the azimuth is a pure angle **linear** in the base-pair count and inverse in the twist; the register departure is **linear in the offset and exactly zero at the design twist**; every site count is a count; unphysical arguments throw at **eight** entry points | **PASS** |
| **2 — limiting cases** | the four azimuths land at exactly `0, 270, 180, 90°`; **exactly two of four are out of plane** and the pair is a quarter turn from the occupied pair; **caDNAno's honeycomb rule falls out of the same arithmetic** (7 bp = 240°, 21 bp = 720° at 10.5 bp/turn); a two-duplex sheet can spare **no** in-plane crossover and its upward inventory is untouched by that; an arm longer than the tile places **zero**, and every placed arm lies inside the footprint | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the **32 bp phase sweep is complete** — refining it ten-fold produces no upward count the base-pair phases do not already contain; the placement is **deterministic** on repeat calls; the closest a crossover plane comes to the footprint truncation is **0.23 nm, 4.6× the edge margin**, so no count is a margin artefact, and the self-consistent count is unchanged under a `1e−6 nm` nudge of the edge; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **13 reproductions, worst departure `4.2e−4`** (Ke et al.'s own `34.3°/bp`, quoted to three digits): their 33.75°/bp, 0.75 turns per 8 bp and 3.0 turns per 32 bp **exactly**; caDNAno's 240° per 7 bp exactly; `C-0015`'s **56 and 49**; `C-0040`'s **four per interface**; `C-0054`'s **42**; `C-0039`'s **9.131 nm** arm (`1.7e−5`); `C-0053`'s **11.821 nm** demand and its **43** in-plane placement, exactly | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **`C-0015`'s inventory recovered from the azimuth, as a set equality.** The in-plane sites this
   construction derives are asserted **equal as `(interface, position)` sets** to `hingeSites`'
   own output at every one of the 32 phases. Two independent constructions, one of them already
   published, and nothing forces them to agree.
2. **The four azimuth classes partition the sites**, and the total is exactly `duplexes × planes`,
   at every phase — a conservation law the census never imposes.
3. **A 32 bp phase shift is the identity class by class, and an 8 bp shift is a cyclic rotation of
   the classes at unchanged positions** — `NORTH(φ+8) = WEST(φ)` and `EAST(φ+8) = NORTH(φ)`,
   asserted as position-set equalities. This is `C-0015`'s *"the period is `p`, not `p/2`"*
   recovered from a third construction, and it is what makes the sweep complete.
4. **No upward site is shared between two duplexes**, asserted by injectivity of the placed roots,
   and **the greedy construction therefore meets the independent per-row bound identically** —
   which the in-plane placement cannot do.
5. **The sheet is one component and retains its whole inventory at every upward hinge count**,
   asserted across the range rather than argued in prose.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **1** | an out-of-plane azimuth not at `±90°` | **no** | **270.000° exactly**, by the lattice's own construction |
| **2** | the out-of-plane register departure exceeding the in-plane one | **no** | **half** of it, exactly, because the departure is linear in the offset |
| **3** | the azimuth model failing to reproduce `C-0015`'s 56/49 | **no** | set equality at all 32 phases |
| **4** | the out-of-plane inventory smaller than the interfaces it replaces | **no** | 52–60 against 14 |
| **5** | the placement failing to beat `C-0053`'s 25 | **no** | **34**, and the host survives it |
| **6** | a published statement that the unoccupied azimuth is forbidden or has failed | **no, and the nearest thing to it is a yield cost** | Ke et al.'s 8 bp staple break, quoted in full above |

**A result that was not anticipated:** the escape and its price are the **same fact**. An upward site
costs the sheet nothing *because* it belongs to one duplex, and its roots are twice as sparse *because*
it belongs to one duplex. `C-0054`'s constraint is discharged and a new one of comparable strength
takes its place from the same sentence — which is why the design number moves 25 → 34 and not 25 → 60.

**A second one:** the phase that maximises the upward inventory is disjoint from the phase that
maximises the interface inventory, so **the crossover phase has acquired a trade it did not have**
in `C-0015`, `C-0040` or `C-0053`.

---

## Does `C-0054`'s verdict survive?

**Its geometry, its lattice, its flatness and load solves and its `D_⊥` collapse survive untouched.
Its ceiling does not, and it named this as the way that would happen.**

| `C-0054` said | this claim finds |
|---|---|
| *"hinge use and interface use are EXCLUSIVE at the site"* | **upheld, and used.** A reciprocal exchange has two partners; an upward hinge's partners are the sheet duplex and the arm, so it is *not* an interface crossover. That is what makes it free |
| *"a connected sheet needs one retained crossover on each of its 14 interfaces, so at most 56 − 14 = 42 can be spent"* | **the arithmetic is exact and the premise under it is false.** Hinges need not come from the 56 — **`CH-0068`** |
| *"all three of `C-0046`'s surviving designs sever the tile"* | **only if the hinges are in-plane.** On the upward azimuth 45, 50 and 56 hinges leave the tile in **one** piece with all 56 interface crossovers |
| *"the branch survives at `39 ≤ n ≤ 42`"* | **that window is a property of the in-plane reading.** The upward reading has no connectivity ceiling at all below 52 |
| its `D_⊥`, flatness, load-distribution and variance tables | **untouched**, and every one of them is computed on *consumed* interfaces, which an upward hinge does not consume |
| *"a junction site the single-layer sheet does not use … is the most valuable single falsifier of this claim"* | **correct, and it fires** |
| **`C-0053`'s 25** | **34**, and for a different reason — the host survives, so the root pitch binds |
| *"45 does not place"* | **still true**, on a 40 nm tile, and **false at a 49.25 nm one** |

---

## Validity range

- **TRL 1–3. The GEOMETRY is published and read directly; the MOTIF is not demonstrated.** No free
  lever held to a single-layer sheet by one crossover at an unoccupied azimuth was found. `C-0028`'s
  and `C-0029`'s findings stand and are upstream of every number.
- **What IS published is the crossover, not the lever.** A crossover from a sheet duplex to a duplex
  added at an out-of-plane azimuth is the elementary step of **square-lattice multilayer origami**
  (Ke et al. 2009) — but there the added duplex is tied at many sites and is rigid. **The
  undemonstrated part is that it is free at one crossover**, and that part is *identical* to the
  undemonstrated part of `C-0053`'s in-plane `E5a1`. **This claim does not make the element more
  speculative than it already was; it makes it no less.**
- **The register argument assumes the sheet's own crossovers set the local twist**, so that an
  unoccupied site 8 bp away is strained by 8 bp of relaxation and not by the accumulated phase from
  the tile edge. That is the design assumption of the square lattice itself and is the reading under
  which Ke et al.'s global-twist analysis is written; **the alternative reading (accumulated phase)
  is not swept and would make the sheet's own 16 bp sites worse in the same proportion.**
- **The 8 bp staple break is a YIELD cost of unknown size here.** Ke et al. report *"significantly
  lower yield"* for a six-layer block needing many of them; nothing measures the cost for a
  single-layer sheet with 34 of them, and this claim does **not** convert it into a number.
- **`k_θ` is unchanged and is not re-derived.** The upward site is the in-plane site rotated 90°
  about the helix axis, so it is the same joint between the same kind of pair of bodies. **What
  `C-0040` says about the axis of `n k_θ` therefore transfers verbatim, including its warning**:
  the hinge line runs along `x` in both cases.
- **The kinematics of the arm are outside this claim.** Whether `E5a1`'s rotation is about the
  interhelical line or about an axis perpendicular to the arm is a tension between `C-0040`'s axis
  statement and `C-0053`'s plan orientation that predates this task; it is **unchanged** by moving
  the root, because the two roots differ by a rigid rotation about the arm's own axis.
- **The plan model is `C-0053`'s hard-body one**, at nominal positions, with the same 2.69 nm
  exclusion convention (2.0 and 2.73 nm swept). An upward arm's plan footprint is taken as the strip
  above its host duplex; **no clearance between an arm and the sheet below it is modelled**, because
  they are one interhelical distance apart by construction.
- **The downward azimuth is counted and not used.** It doubles the inventory and points into the
  grafted layer.
- **The arm is `C-0039`'s placement at §3's ACCEPTABLE stroke.** The desired-stroke verdicts of
  `C-0050` and `C-0046` are untouched.
- **Nothing here re-opens `C-0046`'s designs.** They fail `C-0053`'s placement at 45 on a 40 nm tile
  on the upward lattice too — 30 place — and that is now the reason, rather than severance.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| four nearest neighbours; crossover planes at **8 bp**; same pair every **32 bp**; planes i/iii ⊥ ii/iv | — | **CITED, READ DIRECTLY**, Ke et al., *JACS* **131**:15903 (2009), `PMC2821935` |
| square-lattice designed twist | **33.75°/bp = 32 bp per 3 turns** | **CITED, READ DIRECTLY**, Ke et al. (2009) |
| preferred B-DNA twist | **34.3°/bp = 10.5 bp/turn** | **CITED, READ DIRECTLY**, Ke et al. (2009) |
| the 8 bp staple-break yield cost | *"significantly lower yield … may be destabilizing"* | **CITED, READ DIRECTLY**, Ke et al. (2009) |
| honeycomb: 7 bp = two-thirds turn, 21 bp per pair at 10.5 bp/turn | — | **CITED, READ DIRECTLY**, Douglas et al., *NAR* **37**:5001 (2009), `PMC2731887` |
| origami hinges are made of **ssDNA connections**, not crossovers | *"a nanoscale hinge realized using ssDNA connections"* | **CITED, READ DIRECTLY**, Zhou et al. review, *PMC10395309* (2023) |
| rise per base pair | 0.34 nm | **CITED**, Ke et al. (2009) and Douglas et al. (2009) |
| interhelical distance | 2.69 nm single-layer, 2.73 square | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| M13mp18 scaffold | 7249 nt | **CITED**, Rothemund (2006) |
| crossover hinge `k_θ`, duplex `EI` | 13.5294 pN·nm/rad, 230 pN·nm² | **CITED, FITTED** / **CanDo MODEL INPUT**, via `C-0009`; **neither enters any count here** |
| `C-0015`'s 56/49, `C-0040`'s 4, `C-0054`'s 42, `C-0039`'s 9.131, `C-0053`'s 11.821 and 43 | — | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the azimuth model and its four classes, the register departures, the complete
32-phase census and its three inventories, the used fraction and its 1/3 asymptote, the phase trade,
the upward root lattice, every placement and the self-consistent 34, the five sensitivities and the
scaffold ledger — is **derived here in code**, with `C-0015`'s, `C-0039`'s, `C-0053`'s and
`C-0054`'s pipelines **re-run rather than tabulated**.

## The literature search, recorded so that the negative is falsifiable

**62 EuropePMC queries in fifteen named families**, ~8 s apart, retried on 503. Every query string, its
hit count and its top six hits are recorded verbatim in **`gpd/results/T-119-literature-queries.json`**,
and the survey itself is retained as **`tools/T-119-europepmc-survey.py`** so that the negative is
falsifiable by re-running the search rather than by recollection. Every number above is flagged
**READ DIRECTLY**; the **motif** is flagged **NOT FOUND**.

| family | queries | what it returned |
|---|---|---|
| **F1** square-lattice crossover register | 5 | **the answer**: Ke et al. 2009, full text via `pmc.ncbi.nlm.nih.gov`, read directly |
| **F2** out-of-plane duplex on a single-layer sheet | 6 | nothing; two families returned **0 hits** |
| **F3** crossover as a hinge or pivot | 6 | reviews only; *"DNA origami" AND "hinge" AND "single crossover"* returns **0** |
| **F4** unused or omitted crossover positions | 5 | *"omitting crossovers"* returns **1** — Ke et al., again, and it is about **removing** them |
| **F5** mixed single- and multi-layer origami | 3 | no local second layer on a single-layer sheet |
| **F6** cantilever / lever arm on an origami plate | 3 | levers exist and are **ssDNA-hinged** |
| **F7** attachment by a single covalent link | 3 | nothing beyond `C-0029`'s Rothemund SI precedent |
| **F8** twist from lattice underwinding | 3 | Dietz et al. 2009, consistent with Ke et al.'s account |
| **F9** staple break length and yield | 3 | *"staple breaks" AND destabilizing* returns **exactly one paper**: Ke et al. 2009 |
| **F10** unoccupied azimuth, by five phrasings | 6 | **0 hits** for *"free azimuth"*, *"crossover azimuth"*, *"backbone azimuth"* |
| **F11** interlayer crossover to an added helix | 5 | **0 hits** for four of the five phrasings |
| **F12** a crossover used as a pivot | 5 | **0 hits** for *"hinge joint" AND "crossover"* |
| **F13** free lever on an origami plate | 4 | **0 hits** for *"cantilevered helix"* and *"protruding helix" AND crossover* |
| **F14** the Rothemund protruding-marker precedent | 2 | `C-0029`'s finding, unchanged |
| **F15** three named leads followed | 3 | the 2023 mechanics review, read directly: hinges are **ssDNA** |

> **The one review that could have settled it says the opposite of what the motif would need**:
> *"an example of common kinematic chains and a nanoscale hinge realized using ssDNA connections"*
> (`PMC10395309`, read directly). **Every published origami hinge is a single-stranded connection.
> A crossover-rooted flexure hinge — in plane or out of it — is this programme's own construct.**

## Still open — named, not answered

1. **What 34 upward arms do to the sheet's flatness, load distribution and fluctuation.** They
   consume no crossover, so `C-0054`'s tables do not apply; but 34 duplexes stacked above the tile
   are **mass and rigidity added out of plane**, which no model in this programme contains. **This
   is now the largest open item on the branch.**
2. **The `E5a1` element's actual rotation axis**, which `C-0040` and `C-0053` do not agree on and
   which this claim deliberately does not adjudicate.
3. **The 8 bp staple break, as a number.** It is a folding-yield cost with a published sign and no
   published size at this crossover density.
4. **The scaffold routing.** 35.7 % of M13 is a ledger, not a route: whether a raster exists that
   visits 34 arms above the sheet is a caDNAno question this claim does not answer.
5. **The 49.25 nm tile**, at which §3's 45 places with the host intact. It is a **specification**
   question for NDI, and the fourth this branch has raised.

## Challenges

**Raises [`CH-0068`](../challenges/CH-0068-the-hinge-inventory-is-not-the-sheets-own.md)** against
`C-0054`'s pigeonhole ceiling. **No number in `C-0054`, `C-0053`, `C-0040`, `C-0039` or `C-0015`
fails to reproduce** — 13 reproductions, worst departure `4.2e−4` against a value its own source
quotes to three digits.

**None stands against this claim.** The four ways it would fail:

1. **A measurement that a crossover 8 bp from another cannot be built on a single-layer sheet.**
   Ke et al. build them by the hundred in multilayer blocks and report a **yield** penalty, not an
   impossibility; a single-layer measurement does not exist.
2. **A demonstration that the arm above the sheet must be tied at more than one site to fold at
   all.** That is the multilayer motif and it would make the arm rigid rather than free — the same
   objection that stands against every `E5` element and is not specific to the root.
3. **A twist model in which the sheet's crossovers do not set the local twist**, under which the
   register departures would accumulate from the tile edge instead. It moves the used and unused
   sites in the same proportion, so no verdict here changes sign.
4. **A plan-view obstruction between an upward arm and the sheet's own staples.** Not modelled;
   the arm is one interhelical distance above the sheet by construction, and any such obstruction
   would reduce 34, not restore 42.
