# C-0063 — The upward arm array makes the Gen-1 tile flat, and it does it with **equal springs**: sweeping the row phases of `C-0055`'s own 34 roots takes the dishing from **0.4156** of the stroke to **0.0706** — inside `T-5b`'s 0.10, **4.4× better than no coupling at all**, and better than `C-0058`'s 0.0753 on a grid no placement supplies — so `CH-0074` resolves, and it resolves on the axis `C-0058` said could not be repaired: **a distribution cannot repair a placement, and a placement did not need one**

| | |
|---|---|
| **Task** | [`T-125`](../tasks/T-125-upward-root-placement.md), raised by [`C-0061`](C-0061-stacked-arm-sheet.md)'s *Still open* item 1 — *"the row phases are a free variable nobody has swept"* — and by [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md) |
| **Leaf** | **`A8.2`** (structural rigidity and joint stiffness), with **`A1.2`** for the anchoring scheme the placement belongs to |
| **Verification type** | **logical** (a count and a congruence that fix the shape of the design space and the phases at which it can be symmetric, before any solve) **+ in-silico** (`C-0009`'s grillage and `C-0006`/`C-0047`'s flatness pipeline at **all 32 phases, each on its own host**, under `C-0022`'s **solved** load, driven by an exact Woodbury bank asserted against the assembled solve at **departure 0.0**; `C-0058`'s distribution family, optimiser and least-squares floor re-run as libraries) |
| **Verdict** | **PASS, and the answer is POSITIVE — the first flat Gen-1 tile in this programme that stands on a placement a claim actually supplies.** The best of **1 144 858 evaluated placements** dishes **0.0706** of the free-tile stroke under `C-0022`'s solved load, against **0.3079** for the free tile on the same host, **0.4156** for `C-0055`'s own placement and **0.0753** for `C-0058`'s 3 × 15 flat design. It is **inside `T-5b`'s 0.10**, it beats **no coupling at all by 4.36×** — the bar `C-0047` sets and the one `C-0061` found the array failing by 1.35× — and it needs **no distribution at all**: it is 34 **equal** springs summing to `C-0017`'s unchanged 33.3333 pN/nm. **`CH-0074` RESOLVES.** Its charge was that `C-0058`'s flat distribution lives on stations no placement supplies; the answer is that a placement the upward lattice supplies is flat *without* the distribution, and that adding `C-0058`'s rim rule to it makes it **worse at every ratio** (0.0706 → 0.1410 at ×2, 0.2214 at ×5), while a full 34-parameter optimisation buys a further 13.9 % to **0.0608** at a peak ratio of only **1.30**. **The cheap bounds decided both the shape and the location of the answer.** 34 arms on 15 rows at ≤ 3 per row is `3a + 2(15−a) = 34`, i.e. **exactly four rows of three**; and a row's roots can be symmetric about the tile centre only where `2c ≡ 0 (mod 10.88 nm)`, which holds at **exactly 2 of the 32 phases — 8 and 24** — both of them among `C-0015`'s ten eight-column phases. **The winner is at phase 24 and is centro-symmetric**, its coupling centroid at `0.000 nm` against `C-0055`'s `−8.80`; it was found by an **exhaustive** enumeration of 361 584 centro-symmetric placements, not by the descent, which at that phase stalls at 0.0917. **And the flat set is `C-0015`'s ten**: all ten eight-column phases reach 0.077–0.092 and only one of the twenty-two seven-column phases (15, at 0.0976) gets inside the convention at all — a **fifth** independent construction landing on those ten. **Every placement swept beats no coupling at all** (0.077–0.169 against ~0.310), so `C-0061`'s dishing *source* is a property of `C-0055`'s greedy scheduler and not of the upward lattice. **The cost is affordable and unchanged in kind**: 2.298 pN in the worst path solved (2.941 pN on the mandate secant at 3 nm), 3.4× clear of the 10 pN unzip allowable and inside `C-0049`'s `n·a/s` ceiling of 113.3 pN/nm; 0.346 pN of `C-0014` thermal force per path; 1.246 pN in the worst crossover — **8.3× the 3 × 15 grid's**, exactly as `C-0061` warned, and still 8× clear of the unzip band. Raises [`CH-0076`](../challenges/CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0055`'s *"the geometry is published and the motif is not"* and `C-0029`'s literature finding are unchanged and upstream of every number here. |
| **Provenance** | `gpd/results/T-125-upward-root-placement.json`, produced by `anchoring.UpwardRootPlacementStudyKt`; model in `src/main/kotlin/anchoring/UpwardRootPlacement.kt`; **5 cheap bounds, 32 phase records, 2 exhaustive symmetric enumerations (361 584 placements), 783 274 descent evaluations, 15 best-placement rows, 9 assembled flatness solves, 15 distribution records, 2 load cases, 5 convergence records, 12 upstream reproductions, 5 predicates**; **18 gate-named tests in `src/test/kotlin/anchoring/UpwardRootPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 12 m 49 s — the whole suite, on the finished tree, with NOTHING dropped** (a sibling's mid-TDD `anchoring/CrossbarTrioExistenceTest.kt` had to be dropped by `--drop-file` during development and compiles by the final run); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, each phase carrying **its own** crossover columns (7 or 8) rather than a nominal layout; `C-0039`'s **8.16439 nm** arm at `C-0055`'s self-consistent **34** roots; `C-0017`'s **33.3333 pN/nm** mandate as a **sum**; `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V; `C-0001`'s foundation secant; free-tile stroke **4.90731 nm** |
| **Consumes** | [`C-0055`](C-0055-unused-junction-site.md) (the upward azimuth, the 10.88 nm root pitch, the 34, the arm, and its own placement re-derived from `upwardHingeSites`/`placeUpwardArms` and asserted equal to its result file), [`C-0061`](C-0061-stacked-arm-sheet.md) (the 0.4156, the 0.3558, the centroid, and its **exact zero**, which is what licenses solving on the host with the coupling at the roots), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved profile, keyed on concentration, gap **and bias**), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `rimStiffenedWeights`, `optimiseStiffnessDistribution`, `reachableDishingFloor`, `perPathThermalForces` — **re-run as libraries**), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md) (the grillage, `CrossoverLayout`, the 32-phase period and the 56/49 inventory), [`C-0047`](C-0047-single-column-flatness.md) (the flatness pipeline, the 0.695, and the *"worse than none"* bar), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`perPathStiffnessCeiling`), [`C-0014`](C-0014-lateral-confinement.md) (the thermal force), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`maximumArmsInRow`, against which every row enumeration is asserted) |
| **Raises** | [`CH-0076`](../challenges/CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md), against `C-0061`'s mirrored placement — and **resolves** [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md) |

---

## The claim, in one line

**`C-0058` proved that a distribution cannot repair a placement and concluded that the tile needs the distribution; `C-0061` then found the only real placement failing at 1.35× worse than nothing — and what was wrong with it was neither the distribution nor the lattice but a scheduler that filled every row from the left, because the flattest 34 roots the same lattice offers are equal springs on a centro-symmetric array, and they are flatter than the best distribution on a grid that does not exist.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly);
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` **along** the helices, `y` **across** them, `z` **normal** and positive **upward** — away from
  the grafted layer, which lies below the tile; `w` positive **downward** (`C-0006`, `C-0009`).
- **Dishing** is the peak absolute departure from the area-weighted best-fit **plane** — piston and
  both tilts removed — on the same **81 × 81** grid as `C-0026`, `CH-0034`, `C-0047`, `C-0058` and
  `C-0061`, normalised by the **free-tile stroke 4.90731 nm**. **Flat** means below **0.10** of it,
  `T-5b`'s convention via `C-0015`, **a convention and not a physical threshold**.
- **The phase `φ` is ONE variable and it sets both lattices.** Crossover planes at
  `x = 0.34 φ + 2.72 k` nm; the sheet's own columns are the planes with `k` **even**; row `r`'s
  **upward** (`EAST`) sites are the planes with `k ≡ 2r + 3 (mod 4)`, so an upward lattice has the
  bare **32 bp = 10.88 nm** pitch and adjacent rows are offset by **16 bp = 5.44 nm** (`C-0055`).
  `φ` is quantised to base pairs and its period is **32, not 16** (`C-0015`).
- **A placement is a set of 34 roots**, one per arm, each on its own row's upward lattice, such that
  the arms clear one another and the tile edge under `C-0053`'s footprint convention.
- The coupling is 34 linear springs to ground whose stiffnesses **sum** to `C-0017`'s mandate. The
  headline placement result is at **equal** springs; the distribution is swept separately.
- **Centro-symmetric** means the root set is invariant under `(x, y) → (−x, −y)` — the symmetry a
  Rothemund sheet has and a mirror one does not (`CLAUDE.md`, `C-0009`).

---

## The cheap bounds, which ran before the sweep and decided where to look

| | bound | value | what it settled | falsifier |
|---|---|---|---|---|
| **1** | the count vector, `3a + 2(15 − a) = 34` | **4 rows of three, 11 of two** | the shape of the entire design space in one line — a placement is a choice of *which* four rows and of where each row sits in its own 10.88 nm pitch | a row carrying four arms. **Did not fire**: `8.164 / 10.88 = 0.750` and three roots plus their arms already span 40.8 nm at four |
| **2** | the centro-symmetry congruence `2c ≡ 0 (mod p)` | **2 of 32 phases — 8 and 24** | *where* a symmetric placement can exist at all, and that `C-0055`'s own phase 0 is **not** one of them | the argmin at a phase this excludes. **Did not fire** — the winner is at **24** |
| **3** | `C-0058`'s least-squares floor at `C-0055`'s own stations | **0.0116** of the stroke | that the failure at those stations is **not** a floor: some force vector there is flat, so the 0.4156 is the *equal-spring* reading and not a bound | the floor above 0.10, which would have hardened `CH-0074` immediately |
| **4** | a set-membership test on `C-0061`'s mirrored roots | **16 of 16 off the upward lattice** | that the *"free, one line"* improvement is on the **other face of the sheet** — [`CH-0076`](../challenges/CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md) | — |
| **5** | the uniform per-path force at 34 roots and 3 nm | **2.941 pN** | that the force is not what binds at this count: the admissible non-uniformity ratio is **3.40** | — |

> **Bounds 1 and 2 are the whole search strategy in two lines of arithmetic**, and bound 2 is the
> one that produced the answer: the exhaustive enumeration it licenses beat the descent it was
> meant only to seed.

---

## Deliverable 1 — the sweep, and what the best placement is

**1 144 858 placements evaluated**: 783 274 by deterministic coordinate descent from four named
starts at **every one of the 32 phases, each on its own host**, and **361 584 by exhaustive
enumeration of the centro-symmetric family** at the two phases bound 2 admits.

| placement | host | stations | **dishing / stroke** | flat? | beats no coupling? |
|---|---|---|---|---|---|
| **NONE — free tile** | nominal 8 columns | 0 | **0.3079** | no | — |
| **ROOTS — `C-0055`'s own 34** | nominal 8 columns (`C-0061`'s) | 34 | **0.4156** | no | **no** |
| ROOTS — the same, on **its own** phase-0 host | phase 0, seven columns | 34 | **0.5771** | no | **no** |
| ROOTS-MIRRORED — `C-0061`'s reflection | nominal 8 columns | 34 | 0.3558 | no | no — **and it is not on the upward lattice** (`CH-0076`) |
| **BEST — `T-125`'s swept placement** | **phase 24, eight columns** | **34** | **0.0706** | **YES** | **YES, by 4.36×** |
| NONE — free tile on that same host | phase 24 | 0 | 0.3079 | no | — |
| GRID — `C-0015`'s 3 × 15 | nominal 8 columns | 45 | 0.2182 | no | yes |
| COLUMN — `C-0041`'s 1 × 15 | nominal 8 columns | 15 | 0.6952 | no | no |

**The placement itself**, phase 24, centroid `x = 0.000 nm`, centro-symmetric — rows carrying three
arms in bold:

| row | `y` [nm] | its own upward sites | **roots** |
|---|---|---|---|
| **0** | −18.83 | −16.32, −5.44, 5.44, 16.32 | **−16.32, −5.44, 16.32** |
| 1 | −16.14 | −10.88, 0, 10.88 | 0, 10.88 |
| **2** | −13.45 | −16.32, −5.44, 5.44, 16.32 | **−16.32, 5.44, 16.32** |
| 3 | −10.76 | −10.88, 0, 10.88 | 0, 10.88 |
| 4 | −8.07 | −16.32, −5.44, 5.44, 16.32 | −16.32, 16.32 |
| 5 | −5.38 | −10.88, 0, 10.88 | −10.88, 0 |
| 6 | −2.69 | −16.32, −5.44, 5.44, 16.32 | −16.32, 16.32 |
| 7 | 0.00 | −10.88, 0, 10.88 | −10.88, 10.88 |
| 8 | +2.69 | −16.32, −5.44, 5.44, 16.32 | −16.32, 16.32 |
| 9 | +5.38 | −10.88, 0, 10.88 | 0, 10.88 |
| 10 | +8.07 | −16.32, −5.44, 5.44, 16.32 | −16.32, 16.32 |
| 11 | +10.76 | −10.88, 0, 10.88 | −10.88, 0 |
| **12** | +13.45 | −16.32, −5.44, 5.44, 16.32 | **−16.32, −5.44, 16.32** |
| 13 | +16.14 | −10.88, 0, 10.88 | −10.88, 0 |
| **14** | +18.83 | −16.32, −5.44, 5.44, 16.32 | **−16.32, 5.44, 16.32** |

&nbsp;&nbsp;&nbsp;&nbsp;**Note what centro-symmetry is and is not here.** Row 0 is **not**
symmetric within itself — it carries `−16.32, −5.44, 16.32` — and neither is row 14; the two are
reflections **of each other**. The tempting narrower family, *every row symmetric about the tile
centre*, does not contain this placement at all, and the enumeration is written on the array's
symmetry rather than on the rows' precisely so that it is not missed.

### The phase table — and the flat set is `C-0015`'s ten

| phase class | columns | interface crossovers | best swept dishing | flat? |
|---|---|---|---|---|
| **6, 7, 8, 9, 10** | **8** | **56** | **0.0789, 0.0875, 0.0852, 0.0801, 0.0922** | **all** |
| **22, 23, 24, 25, 26** | **8** | **56** | **0.0773, 0.0769, 0.0706, 0.0823, 0.0881** | **all** |
| the other 22 | 7 | 49 | 0.0976 – 0.1692 | **one** (phase 15, 0.0976) |

**Three readings, and the third is new:**

1. **Every phase beats no coupling at all** once the placement is swept — 0.077 to 0.169 against a
   free tile at 0.308–0.312. `C-0061`'s *"1.35× worse than no coupling"* is a property of
   `C-0055`'s **greedy scheduler**, not of the upward lattice.
2. **The eight-column phases are the flat ones.** `C-0015`'s ten carry 56 interface crossovers and
   an upward inventory of 52–53; the seven-column phases carry 49 and an inventory of 60. **The
   phase that maximises the upward inventory is not the phase that places well** — `C-0055` found
   the first half of this trade and this is the second.
&nbsp;&nbsp;&nbsp;&nbsp;**The two exhaustive enumerations**, for the record: **163 296** centro-symmetric
placements at phase 8 (best **0.0874**, median 0.4169, worst 0.9379) and **198 288** at phase 24
(best **0.0706**, median 0.4309, worst 0.9812). At phase 24 the enumeration beats the descent
(0.0917); at phase 8 the descent wins (0.0852 against 0.0874), because there the best placement is
**not** centro-symmetric. Neither method dominates, and the claim quotes the better of the two at
every phase.

3. **The two centro-symmetric phases are inside those ten**, and the flat winner is one of them.
   That is now the **fifth** independent construction to land on `C-0015`'s ten — after `C-0015`
   itself, `C-0040`, `C-0053` and `C-0055`'s in-plane half.

---

## Deliverable 2 — the distribution, and why the answer is that it is not needed

`C-0058`'s one-parameter rim family and its full optimiser, both on the swept placement, at
`C-0017`'s unchanged total:

| rule | ratio | **dishing / stroke** | flat? | peak path force (secant, 3 nm) | peak solved path force | `C-0014` thermal |
|---|---|---|---|---|---|---|
| **uniform — 34 EQUAL springs** | **1.00** | **0.0706** | **YES** | **2.941 pN** | **2.298 pN** | **0.346 pN** |
| `C-0058`'s rim rule | 2.00 | 0.1410 | no | 3.571 | 2.564 | 0.420 |
| the same | 3.00 | 0.1802 | no | 3.846 | 2.788 | 0.452 |
| **the same — `C-0058`'s own ×5** | **5.00** | **0.2214** | **no** | 4.098 | 3.016 | 0.482 |
| the same | 8, 12, 20 | 0.2545, 0.2751, 0.2928 | no | ≤ 4.425 | ≤ 3.356 | ≤ 0.520 |
| **34-parameter descent under `C-0049`'s ceiling** | **1.30** | **0.0608** | **YES** | 3.819 | 2.608 | 0.449 |
| the least-squares floor over **all** force vectors | — | **0.0031** | — | — | — | — |

**`C-0058`'s rim rule is actively harmful here, and the reason is structural**: its collar rule
exists to move reaction *toward* an edge that a 3 × 15 grid under-supports. The swept placement
already puts a root at `x = ±16.32 nm` on **eight** rows and at `±10.88` on most of the rest, so
stiffening the rim over-reacts an edge that is already carried, and the dishing doubles by ×2.

**What the optimiser wants is almost nothing**: a peak of **1.30×** the uniform share, for 13.9 %.
Against `C-0058`'s grid, where the same optimiser wanted 5× and bought 65 %, that is the whole
finding of this claim in one number.

**The floor is 0.0031** — 22.6× below what was found — so this is emphatically *not* a bound. It is
the same looseness `C-0058` reports on its own grid (20.3×), and for the same reason: the floor
ignores the mandate, and the mandate is what binds.

---

## Deliverable 3 — `CH-0074`, resolved on its own terms

`CH-0074`'s charge, verbatim: *"`C-0058`'s flat tile is flat on a station set that no placement
claim supplies."*

| `CH-0074` said | this claim finds |
|---|---|
| *"the best the flat family reaches on a station set a placement claim actually supplies is **0.1649**"* | **0.0706 with equal springs and 0.0608 with a distribution** — and 0.1649 was itself on the mirrored set, which is not on the upward lattice at all (`CH-0076`), so the true figure it should have quoted is **0.2902** |
| *"a **uniform** coupling on `C-0055`'s own placement is a net dishing SOURCE"* | **upheld, and the cause is located**: it is the scheduler's `−8.80 nm` centroid, not the lattice. Every one of the 32 phases has a placement that beats the free tile |
| *"the per-path stiffness ratio being buildable is necessary and not sufficient: the **stations** have to be buildable too"* | **agreed, and now discharged** — the stations are `C-0055`'s own upward azimuth at phase 24, and the ratio the design needs is **1.30**, which is nearer to *no ratio at all* than to `C-0060`'s 5:1 |
| *"a distribution cannot repair a placement"* | **still true, and it is the sentence that closes the challenge from the other side**: the placement did not need repairing by a distribution, it needed **placing** |

**Status: `CH-0074` is RESOLVED.** `C-0058`'s *result* stands unchanged (its 0.2182 and 0.0753 both
reproduce here); what falls is the reading that the Gen-1 tile's flatness depends on a station set
no placement supplies.

---

## The five verification gates

Executed as **18 gate-named tests** in `src/test/kotlin/anchoring/UpwardRootPlacementTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL in 12 m 49 s** — the whole suite, on its own isolated tree,
with **nothing dropped**.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the upward root pitch is **exactly** 32 bp and the row offset **exactly** 16 bp, measured from the lattice rather than asserted; every root lies inside the footprint and every station carries its own row's `y`; unphysical arguments throw at **eight** entry points, including a row whose directions do not match its roots, roots given out of order, and a placement asked for a station on a sheet too narrow to hold its row | **PASS** |
| **2 — limiting cases** | a row admits **exactly** the arm count `C-0053`'s own exact interval scheduler places, at **every one of the 32 phases and every one of the 15 rows**; an array too long for its row admits **no** direction assignment; **a uniform load on a uniform Winkler foundation dishes exactly zero on the free tile**; the free tile reproduces `C-0022`'s 0.3079 | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | nested subdivisions **1 ⊂ 2 ⊂ 4**: 0.069748 → 0.070615 → 0.070615, a departure of `7.4e−6` between the last two (nested only, per `CLAUDE.md`); the dishing sample grid 41/81/161 gives 0.0688/0.0706/0.0725, **2.7 %**, and the coarsest and finest are both inside the convention; **the Woodbury surrogate against the assembled 855-degree-of-freedom solve: departure `0.0`**; the descent repeated from the same start returns an **identical placement key**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **12 reproductions, worst strict departure `0`**: `C-0055`'s 34 roots re-derived from `upwardHingeSites` and asserted **equal to its own result file to `1e−9` nm**, its 10.88 nm pitch and 8.164 nm arm; `C-0061`'s **0.4156** (`2.6e−5`) and its **−8.80 nm** centroid (`4.0e−16`); `C-0022`'s **0.3079** (`7.7e−6`); `C-0058`'s **0.2182** (`6.1e−5`), **0.0753** (`5.8e−4`) and its 4.5 admissible ratio (exactly); `C-0047`'s **0.695** (`2.9e−4`); `C-0015`'s **56** and **49** (exactly) | **PASS** |

### Gate 3 — six things that are not restatements of the construction

1. **The Woodbury bank is asserted against the assembled lattice at departure `0.0`** — not
   approximately: superposition is exact for a linear system, and this is the falsifier that would
   have invalidated the entire sweep.
2. **Maxwell-Betti**: the influence matrix's asymmetry is below `1e−9` relative, measured between
   two different quadratures rather than imposed.
3. **Equilibrium**: the support forces plus the foundation carry the whole applied load to `1e−8`.
4. **The phase period is 32 and not 16**, asserted as an inequality of position **sets** at
   `φ = 3` against `φ = 19`, with `φ = 35` the identity — `C-0015`'s trap, re-checked on the
   *upward* lattice where it has never been checked before.
5. **Exactly two of the 32 phases can supply a centro-symmetric placement**, and `C-0055`'s own is
   not one of them — the congruence, asserted rather than asserted-of.
6. **`C-0061`'s mirrored roots are `WEST` sites**, asserted against the `junctionSites` census of
   the *downward* azimuth rather than by counting what is missing from the upward one.

Two further checks live under gate 4 and in the study's convergence records: **a surrogate built
over every candidate root and then sliced to a placement equals one built over that placement
alone** (`1e−12`), which is what makes 1.1 million placements affordable; and **the best placement
re-solved as a `StackedArmGrillage` carrying all 34 arms** gives departure `0.0` — `C-0061`'s exact
zero, which is what licenses running the sweep without the arms at all.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the surrogate disagreeing with an assembled solve | **no** | departure `0.0` |
| **F2** | a uniform load dishing anything on the free tile | **no** | `< 1e−9` nm, and the uniform-load sweep selects a **different** argmin, which is why a flatness result is quoted with its load case |
| **F3** | `C-0061`'s 0.4156 failing to reproduce | **no** | `2.6e−5` |
| **F4** | the best placement failing to beat the free tile | **no** | 0.0706 against 0.3079, and **all 32 phases** beat it |
| **F5** | the argmin at a phase the congruence excludes | **no** | phase 24, which the congruence names |

**A result that was not anticipated:** the **exhaustive** enumeration beat the **descent** at the
same phase — 0.0706 against 0.0917 — so the cheap bound did not merely locate the answer, it
supplied it. The descent is a descent, and at the 22 phases with no symmetric family its number is
an **upper bound** on what that phase can do.

**A second one:** `C-0058`'s rim rule **reverses sign** on this placement. Every ratio above 1
makes it worse, monotonically, and by ×5 the dishing has trebled. A distribution rule is a property
of a station set, not of a tile.

---

## Does `C-0061`'s verdict survive?

**All of it that is about the sheet; not the one line that is about the placement.**

| `C-0061` said | this claim finds |
|---|---|
| the arms add **exactly zero** static stiffness at one tie | **reproduced at the new placement**: departure `0.0` between the armed and unarmed solves |
| *"on `C-0055`'s own 34 roots a uniform coupling dishes 0.4156 … 1.35× worse than no coupling at all"* | **reproduced to `2.6e−5`**, and it is a property of that placement only |
| *"reflecting the odd rows is free, lands on the same column lattice"* | **it lands on the same COLUMN lattice and on the other AZIMUTH**: 16 of its 16 reflected roots are `WEST` sites, pointing into the grafted layer — **`CH-0076`** |
| *"the grillage here is run at the nominal 8-column layout … the seven-column phase is not swept"* | **swept, and it is worth 38.9 %**: the same 34 roots on their own phase-0 host dish **0.5771**, not 0.4156 |
| *"the peak crossover force is 8.3× larger at the arm roots than on the inset grid"* | **upheld at the new placement**: 1.246 pN against 0.150, still 8× clear of the 10–15 pN unzip band |
| *"the row phases are a free variable nobody has swept"* | **swept — 1 144 858 placements — and it is worth 5.9× in dishing** |

---

## Validity range

- **TRL 1–3. Nothing is measured, and the motif is not demonstrated.** A free lever held to a
  single-layer sheet by one crossover is this programme's own construct (`C-0055`, 62 recorded
  queries), and Ke et al.'s **8 bp staple break** yield cost is unpriced and applies to all 34.
- **ONE load state.** `C-0022`'s 2 mM, 10 nm, 0.192 V solve. `C-0058` shows a flat design at one
  state dishing 0.187 at the 2 nm gap, and `T-123` owns that question; **the swept placement is not
  evaluated at `C-0022`'s other four states**, and nothing here says it survives them.
- **The descent is a descent.** At the 22 phases without a symmetric family the reported best is an
  upper bound; the exhaustive enumeration covers only the centro-symmetric family, and only at
  `2 ≤ n_r ≤ 3` per row. The descent was given the freedom to drop a row to one arm and never took
  it, which is evidence and not proof.
- **The least-squares floor is 0.0031 and the best found is 0.0706**, so 22.6× of headroom is
  unexplained. It is a loose bound (it ignores the mandate), but no claim is made that 0.0706 is
  optimal.
- **The flatness is a dishing convention at one grid resolution.** At 161 samples the winner reads
  0.0725 rather than 0.0706; both are inside 0.10, but a placement quoted at 0.09 would not be safe
  against that 2.7 %.
- **Every phase's host is that phase's own**, which is the correction this claim makes to `C-0061`
  — but the **arm roots are then not always host nodes** (they are at phases 8 and 24, and the
  `StackedArmGrillage` check is run there). Elsewhere the coupling enters through the element's
  own Hermite interpolation, exactly as every `PointSupport` in this programme does.
- **The count and the arm are fixed** at `C-0055`'s self-consistent 34 and `C-0039`'s 8.16439 nm.
  A different count re-solves the arm and re-opens the placement.
- **Static, single-layer, linear**, exactly as `C-0009`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the upward azimuth, its 10.88 nm pitch, the 16 bp row offset, the 34 roots, the 8.164 nm arm | — | **`C-0055`**, re-derived here from `upwardHingeSites` and asserted equal to `gpd/results/T-119-unused-junction-site.json` |
| the solved edge profile | depth −0.30293 over 8.939 nm, rim −0.59388 over 1.0 nm | **`C-0022`**, read from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on concentration, gap **and bias** |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| crossover spacing per interface, rise per base pair | 32 bp, 0.34 nm | **CITED**, Rothemund (2006) / Ke et al. (2009) |
| the 10 pN unzip allowable and the 48–65 pN shear band | — | **CITED**, via `C-0006`/`CH-0029` |
| `C-0061`'s, `C-0058`'s, `C-0047`'s and `C-0022`'s published flatness numbers | 0.4156, 0.3558, 0.2182, 0.0753, 0.695, 0.3079 | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the upward root lattice as a per-row position set, the count arithmetic, the
centro-symmetry congruence and its two phases, the row-option enumeration and its agreement with
`C-0053`'s scheduler, the influence bank and every placement evaluated through it, the 32-phase
table, the exhaustive symmetric enumerations, the best placement, the distribution family on it,
the floors and the force and thermal budgets — is **derived here in code**, with `C-0009`'s,
`C-0022`'s, `C-0047`'s, `C-0055`'s, `C-0058`'s and `C-0061`'s pipelines **re-run rather than
tabulated**.

## Still open — named, not answered

1. **The other four of `C-0022`'s solved states.** This is the same exposure `C-0058` carries and
   `T-123` is chartered for; a placement flat at one state is not a flat device.
2. **The 22 asymmetric phases are searched, not enumerated.** Phase 23 reaches 0.0769 by descent
   alone — within 9 % of the exhaustive winner — so an exhaustive treatment there might well beat
   0.0706.
3. **The 22.6× gap to the least-squares floor.** Joint optimisation over placement *and*
   distribution has not been run; each was optimised with the other fixed.
4. **The arm directions are chosen greedily**, `+x` first. They do not enter the flatness — the
   coupling enters at the root — but they set which way 34 levers point, and `C-0035`'s clearance
   question is a plan-view one.
5. **`C-0061`'s open items are untouched**: the seven-column phase's *variance* and *drag*, the
   clearance between the arm slab and `C-0035`'s tie-down path, and the 8 bp staple-break yield.
6. **Whether the placement survives `C-0060`'s buildability**. It needs a ratio of 1.30 at most,
   which is 3.6× *easier* than the 5:1 `C-0060` showed is buildable — but `C-0060`'s array
   objection (`k ∝ span⁻³`) is about the flexures, and it is unchanged.

## Challenges

**Raises [`CH-0076`](../challenges/CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md)**
against `C-0061`'s mirrored placement and its nominal-host reading.
**Resolves [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md).**
**No number in `C-0055`, `C-0061`, `C-0058`, `C-0047`, `C-0022` or `C-0015` fails to reproduce** —
12 reproductions, worst strict departure zero.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that the crossover phase is not a free design variable** — that a staple layout
   cannot choose `φ = 24`. `C-0015` established the opposite, and the phase is quantised to base
   pairs precisely because a staple chooses it.
2. **A load state at which the swept placement is not flat.** Expected, and named as open item 1;
   it would move this claim from *"the tile can be made flat"* to *"the tile can be made flat at
   the design point"*, which is exactly `C-0058`'s own exposure.
3. **A finer dishing grid moving 0.0706 past 0.10.** It moves +2.7 % from 81 to 161 samples; it
   would need 40 %.
4. **The `E5a` motif failing.** Everything here is downstream of `C-0055`'s undemonstrated arm, and
   nothing in this claim makes it more or less demonstrated.
