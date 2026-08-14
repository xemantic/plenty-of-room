# C-0058 — A NON-UNIFORM coupling makes the Gen-1 tile flat, and nothing else in this programme ever has: at `C-0015`'s 45 paths, redistributing `C-0017`'s **same** mandated total takes the dishing from 0.218 of the stroke to **0.075** with a one-parameter rule and **0.054** with a full optimisation — inside `T-5b`'s 10 % convention that `CH-0034`'s count axis saturates 1.5× above — while `C-0041`'s buildable 1 × 15 buys only 13 % and stays a net dishing source

| | |
|---|---|
| **Task** | [`T-113`](../tasks/T-113-non-uniform-coupling.md), which is `C-0047`'s *"Still open"* item 2 and which it names **the last unexplored axis, and the only one that could attack `CH-0034`'s floor** |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage and `C-0006`'s continuum plate, under `C-0022`'s **solved** electrostatic profile read from its own result file and keyed on concentration, gap **and bias**, both driven by an **exact Woodbury surrogate** asserted against the assembled solve to `1.5e−12`) **+ logical** (a least-squares bound in the space of attachment *forces*, which bounds every stiffness distribution whatever, and the per-path force ceiling as two lines of arithmetic) |
| **Verdict** | **PASS on the predicate, and the answer is POSITIVE at three columns and NEGATIVE at one.** At **3 × 15** the same 33.3333 pN/nm, redistributed, dishes **0.0753** of the free-tile stroke under a one-parameter rule (*the 34 stations within 6.7 nm of an edge carry 5× the other 11*) and **0.0544** under a 45-parameter optimisation, against the uniform coupling's **0.2182** — improvements of **65.5 %** and **75.1 %**, and **both are inside `T-5b`'s 0.10**. `CH-0034`'s count axis saturates at **0.149** and never reaches it, so **225 uniform attachments cannot do what 45 unequal ones can**, and the 0.149 is a property of the equal-spring family and not of the rim — this is [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md). **The cost is affordable and is a force**: the flat rim design peaks at **2.762 pN per path** at §3's 3 nm stroke, 3.62× clear of the 10 pN unzip allowable, with 0.784 pN in the worst crossover (12.8× clear) and 1.13 pN of duplex shear against a 48–65 pN band; the per-path **thermal** force rises 24 % (`C-0014`, and it is **linear** in the path's share, not its square root). **At `C-0041`'s buildable 1 × 15 the axis fails**: the best admissible distribution buys **13.0 %** (0.6952 → 0.6048), stays **6.0×** the convention and **1.96× worse than no coupling at all** — fifteen springs on the single line `x = 0` can reshape only the across-helix profile, and `C-0047` showed the dishing there is the along-helix bow. **A distribution cannot repair a placement.** **And flatness bought this way is owed at ONE state**: the same rim design is flat at three of `C-0022`'s five solved states and dishes 0.187 at the 2 nm gap, where the *uniform* coupling dishes 0.071; a **minimax over all five** reaches a worst case of only **0.1587**, so no distribution found is flat everywhere. Two smaller results: **the per-path ceiling costs nothing at 45 paths** — the capped and uncapped optima coincide to the last digit — and **non-uniformity moves `C-0047`'s break-even from three columns to two** (2 × 15 goes 0.3504 → 0.2512, past the free tile's 0.3079). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, and nothing here says a per-path stiffness can be **built** to a prescribed value — see the validity range, where that is the largest open item this claim leaves. |
| **Provenance** | `gpd/results/T-113-non-uniform-coupling.json`, produced by `coupling.NonUniformCouplingStudyKt`; model in `src/main/kotlin/coupling/NonUniformCoupling.kt`; **3 cheap-bound records, 150 rim-sweep records, 25 solved distributions, 225 per-path records, 13 optimiser records, 54 transfer records, 12 convergence records, 12 upstream reproductions**; **23 gate-named tests in `src/test/kotlin/coupling/NonUniformCouplingTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 8 m 23 s** (the whole suite, on the finished tree) on its own isolated tree, with one concurrent agent's mid-TDD test dropped by `--drop-file` (`src/test/kotlin/anchoring/BackboneTorsionTest.kt`, `T-71`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical on two independent runs** — after the search's **path** diagnostics were removed from it, the evaluation count, the sweep count and the winning start all being ulp-sensitive while every objective is identical to nine significant digits |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous buffer with **Mg²⁺**; 40.0 × 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; 8 symmetrically centred crossover columns (`T-10`); §3's 100 pN over the footprint; `C-0017`'s **33.3333 pN/nm as a SUM**, distributed; `C-0001`'s foundation secant, ×1; free-tile stroke **4.90731 nm**; design point `C-0022` 2 mM, 10 nm, 0.192 V |
| **Consumes** | [`C-0047`](C-0047-single-column-flatness.md) (the question, the 1 × 15 and 3 × 15 uniform numbers, the free tile — all three reproduced here to `1e−3` as the limiting case), [`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md)/[`C-0026`](C-0026-one-row-per-duplex.md) (the saturation table and the pipeline), [`C-0022`](C-0022-tile-edge-load-profile.md) (the **solved** collar, read from `gpd/results/T-3b-tile-edge-load-profile.json`), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 3 × 15 grid, *"shapes, not counts"*), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage and its rank-one anchor update, generalised here to rank `n`), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the flatness convention, the 10 pN unzip allowable via [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md)), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, **as a sum**), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`perPathSecantCeiling`, re-derived and asserted equal), [`C-0014`](C-0014-lateral-confinement.md) (`√(k_BT k)/n`, generalised to unequal paths and reduced back exactly), [`C-0041`](C-0041-flexure-array-packing.md) (the 15-path count) |
| **Raises** | [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md), against `CH-0034` |

---

## The claim, in one line

**`C-0017`'s mandate is an equality on a SUM and every claim in this corpus had read it as an equality on each path; freeing the distribution — no extra stiffness, no extra attachments, no new motif — is worth 65–75 % of the dishing at 45 paths and takes the Gen-1 tile inside `T-5b`'s tolerance for the first time, while at 15 paths it is worth 13 % and changes nothing, because a distribution can reweight a placement and cannot move it.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous buffer with **Mg²⁺**.
- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre. `w` is positive **downward**, compressing the polymer layer (`T-5`, unchanged).
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane** — piston and both tilts removed — sampled on the same **81 × 81** grid as `C-0026`, `CH-0034` and `C-0047`.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform* load at the same foundation stiffness: **4.90731 nm** at `k_f × 1`. Unchanged normaliser, so every number here is directly comparable with `C-0006`, `C-0015`, `C-0026`, `CH-0034` and `C-0047`.
- **Flat** means peak dishing below **10 %** of that stroke — `T-5b`'s convention via `C-0015`, **a convention and not a physical threshold**.
- The **coupling** is `n` linear springs to ground whose stiffnesses **sum** to `C-0017`'s 33.3333 pN/nm. The sum is the mandate; the **distribution is this task's design variable**, and nothing upstream constrains it.
- A **rim-stiffened** distribution of ratio `R` over a collar of width `c` gives weight `R` to every attachment whose distance to the nearest tile edge is `≤ c` and weight 1 to the rest, then normalises to the mandate. `R = 1` is the uniform coupling identically.
- A collar **depth is negative for an enhancement**, which is the sign `C-0022` solved.

### The upstream gotcha, avoided by construction

`gpd/results/T-3b-*.json` carries **two** solved profiles per `(concentration, gap)` — one per operating bias. Every lookup here is keyed on **`(concentration, gapHeight, appliedBias)`** and errors if the triple is absent; the bias travels into the result file with every record.

---

## The two cheap bounds, which ran first

### Bound 1 — the reachable floor, in the space of attachment FORCES

The models are linear, so the deflection under the solved load with springs at the `n` stations is `w = w_free − Σ_j F_j g_j`, with `g_j` the influence function of a unit force at station `j` and `F_j` whatever force that spring happens to carry. **Dishing is affine in `F`**, and *every* stiffness distribution produces *some* `F` — so minimising over the whole of `ℝⁿ`, ignoring the mandate, positivity and the relation between a force and a stiffness, bounds them all. The peak of a sampled field is never below its own root mean square, so

&nbsp;&nbsp;&nbsp;&nbsp;`min_k peak dishing ≥ min_F peak dishing ≥ min_F rms dishing`

and the right-hand side is one `n × n` Cholesky on precomputed grid fields.

| grid | reachable rms floor / stroke | peak of that field / stroke | uniform / stroke | did the bound fire? |
|---|---|---|---|---|
| 1 × 15 | 0.0169 | 0.0554 | 0.6952 | **no** |
| 2 × 15 | 0.0136 | 0.0280 | 0.3504 | **no** |
| **3 × 15** | **0.0027** | 0.0070 | 0.2182 | **no** |

> The declared falsifier was *"if the floor exceeds 0.10, no distribution can make the tile flat and the optimisation is unnecessary"*. It **did not fire**, and a distribution reaching the tolerance was then found. The best found sits **20.3×** above the 3 × 15 floor — which measures how **loose** the bound is (it ignores the mandate, and the mandate is what binds), not how much room the search left.
>
> **Maxwell-Betti reciprocity of the influence matrix holds to `1.2e−15`** — measured between two different quadratures rather than imposed, which is what makes it informative.

### Bound 2 — the price of non-uniformity, in one line

A path carrying `k_i` delivers `k_i·s` at stroke `s`, so `C-0006`'s per-path allowable caps it at `a/s`, and against the uniform share `K/n` that is a **ratio ceiling**

&nbsp;&nbsp;&nbsp;&nbsp;**`R_max = n·a / (s·K)`** — `C-0049`'s `n·a/s` divided by the mandate.

| paths | `R_max` at §3's **acceptable** 3 nm | `R_max` at §3's **desired** 10 nm |
|---|---|---|
| 15 (`C-0041`) | **1.50** | **0.45** — below one: *not even the uniform coupling is admissible* |
| 45 (`C-0015`) | **4.50** | 1.35 |

`C-0049` reached the second column from the other side and this is the same statement: at 15 paths and the desired stroke the coupling is past the allowable before any distribution is chosen.

---

## Deliverable 1 — the flatness table at `C-0022`'s design point (2 mM, 10 nm, 0.192 V), `k_f × 1`

### 3 × 15 — `C-0015`'s 45 paths

| distribution | dishing [nm] | **dishing / stroke** | vs uniform | `k_max` [pN/nm] | `k_max·s` at 3 nm [pN] | **flat?** | lattice / plate |
|---|---|---|---|---|---|---|---|
| **uniform (`C-0047`'s limiting case)** | 1.0708 | **0.2182** | — | 0.741 | 2.222 | no | 0.919 |
| load-matched (the bound's prediction) | 0.7984 | 0.1627 | +25.4 % | 0.878 | 2.633 | no | 0.906 |
| rim × 2 over `C-0022`'s 8.94 nm collar | 0.7927 | 0.1615 | +26.0 % | 0.823 | 2.469 | no | 0.990 |
| rim × 5 over the same collar | 0.5619 | 0.1145 | +47.5 % | 0.882 | 2.646 | no | 1.130 |
| **rim × 10 over the same collar** | 0.4658 | **0.0949** | +56.5 % | 0.903 | 2.710 | **YES** | 1.252 |
| **rim × 5 over a 6.70 nm collar — the best one-parameter design** | **0.3697** | **0.0753** | **+65.5 %** | **0.921** | **2.762** | **YES** | 1.239 |
| **the 45-parameter OPTIMUM** (capped = uncapped) | **0.2671** | **0.0544** | **+75.1 %** | 1.539 | 4.617 | **YES** | 1.124 |
| minimax over all five solved states | 0.6118 | 0.1247 | +42.9 % | 3.115 | 9.346 | no | 1.082 |

### 1 × 15 — `C-0041`'s buildable count, and 2 × 15

| grid | distribution | dishing / stroke | vs uniform | `k_max·s` [pN] | admissible? | flat? | vs the free tile (0.3079) |
|---|---|---|---|---|---|---|---|
| 1 × 15 | uniform | 0.6952 | — | 6.667 | yes | no | **2.26× worse** |
| 1 × 15 | best rim (×20 over 13.0 nm) | 0.6136 | +11.7 % | 9.756 | yes | no | 1.99× worse |
| 1 × 15 | **best admissible optimum** | **0.6048** | **+13.0 %** | 10.000 | yes (exactly at the cap) | no | **1.96× worse** |
| 1 × 15 | optimum, no cap | 0.5514 | +20.7 % | 23.945 | **no** — 2.4× past unzip | no | 1.79× worse |
| 2 × 15 | uniform | 0.3504 | — | 3.333 | yes | no | 1.14× worse |
| 2 × 15 | **optimum** | **0.2512** | **+28.3 %** | 6.097 | yes | no | **0.82× — better** |

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0047`'s break-even moves from three columns to two.** A uniform 2 × 15 coupling is a net dishing source and a redistributed one is not.

---

## Deliverable 2 — the design, which is a rule and not a table of forty-five numbers

The best one-parameter member of the family is: **the 34 attachments within 6.7 nm of a tile edge carry 0.921 pN/nm each and the 11 remaining ones carry 0.184** — i.e. `k_rim/k_interior = 5`, at the mandated total. Geometrically those 11 are exactly *the middle column's eleven middle stations*: on the 3 × 15 grid the outer columns sit 6.67 nm from the edge, so a 6.7 nm collar selects **both outer columns entire, plus the four extreme rows of the middle column**.

| collar | rim paths | best ratio | best dishing / stroke | flat window in the ratio |
|---|---|---|---|---|
| 1.5 nm (two extreme rows only) | 6 | 1.5 | 0.1289 | — never flat |
| 3.0 nm | 6 | 1.5 | 0.1289 | — never flat |
| **6.70 nm (both outer columns + four rows)** | **34** | **5** | **0.0753** | **5 ≤ R ≤ 20** |
| 8.94 nm (`C-0022`'s **solved** collar) | 36 | 10 | 0.0949 | R = 10 only |
| 13.0 nm | 40 | 100 | 0.1602 | — never flat |

Three things follow.

1. **The sweep is not monotone in either parameter** and the flat window is bounded on both sides: at a large ratio the interior springs carry nothing, and what is left is an attachment scheme placed *only* on the collar, which dishes between its own supports again. **The family converges on a placement**, so what non-uniformity buys is `C-0015`'s *"shapes, not counts"* — with the shape chosen **continuously** instead of by an integer.
2. **`C-0022`'s own 8.94 nm collar is not the best selector.** The load's collar and the *structure's* influence patch are different lengths, and it is the second that matters: `C-0047`'s along-helix Winkler bending length is 12.83 nm against a 13.33 nm column pitch, so the outer columns are the last stations whose patches reach the edge. Matching the stiffness to the **load** — the load-matched distribution, which is what a local argument predicts — is worth only 25.4 % against the rim rule's 65.5 %. **A plate is not a local response.**
3. **The full optimisation is worth a further 27.8 %** (0.0753 → 0.0544) and needs 45 numbers, one near-zero path, and a search. The one-parameter rule carries most of the effect and is the thing a design can be written from.

---

## Deliverable 3 — the cost, on every path the programme prices

At the two flat 3 × 15 designs, against `C-0006`/`CH-0029`'s **10 pN** unzip allowable, the 48–65 pN duplex-shear band, and `C-0014`'s thermal force:

| | uniform | **rim × 5 / 6.70 nm** | **optimum** | allowable |
|---|---|---|---|---|
| peak path stiffness [pN/nm] | 0.741 | 0.921 | 1.539 | 3.333 (`= a/s`) |
| **peak path force at the 3 nm stroke** [pN] | 2.222 | **2.762** | **4.617** | **10** — margins **3.62×** and **2.17×** |
| peak attachment force under the solved load [pN] | 1.943 | 2.034 | 2.929 | 10 |
| **peak crossover force** [pN] | 0.150 | **0.784** | **0.823** | 10 — margins 12.8× and 12.2× |
| peak duplex shear [pN] | 0.793 | 1.132 | 1.511 | 48–65 |
| **peak per-path thermal force** [pN] | 0.261 | **0.325** (+24 %) | **0.542** (2.08×) | — |

- **The per-path ceiling costs nothing at 45 paths**: the optimisation run *with* the `a/s` cap and the one run *without* it return the same distribution to the last digit, because the optimum's own peak (1.539 pN/nm) sits well inside the cap (3.333). At **15** paths the cap is binding and worth 7.7 percentage points of dishing (0.6048 capped against 0.5514 uncapped, and the uncapped one is 2.4× past the allowable).
- **`C-0014`'s over-stiffening penalty is LINEAR in the path's share, not its square root.** The tile's rigid-body coordinate has variance `k_BT/K` against the *whole* coupling and every path sees the same amplitude, so `F_i = k_i √(k_BT/K)`, which reduces to `√(k_BT K)/n` at equal paths exactly (gate 5). A path stiffened `R×` therefore carries `R×` the thermal force — the flat design's worst path carries 0.325 pN where a uniform one carries 0.261.
- The crossover path is where the redistribution is most visible — **5.2×** the uniform value — and it is still **12.8×** clear of unzip. No allowable in the stack is threatened by either flat design.

---

## Deliverable 4 — the state dependence, which is the real price

> ⚠️ **Annotated by [`CH-0077`](../challenges/CH-0077-five-solved-states-are-four-devices.md)
> ([`C-0064`](C-0064-robust-distribution.md), `T-123`, 2026-08-14). Every number in this section
> reproduces to `≤ 5.8e−4` and none of it moves; what moves is the word *"every"*.** `C-0022`'s five
> headline states are **four devices** — the rest states of three different buffers at a 10 nm layer,
> plus the rest and *held* states of a **5 nm** layer, of which the 2 nm one is that device at §3's
> 3 nm stroke and no state of the 10 nm device at all. Over the range a **single** device traverses, a
> robust distribution **exists**: 0.0373 (2 mM, 10 nm), 0.0435 (0.5 mM), 0.0620 (the 5 nm device) and
> 0.0504 (10 mM), all inside `T-5b`'s 0.10 — **and this claim's own rim × 5 rule is flat over the whole
> stroke of both 10 nm devices** (0.0753 and 0.0683), which is more than this section claims for it.
> The five-state minimax improves to **0.1254** under a real optimiser and stops there for a reason
> this claim could not have seen: the 2 nm state is the **only one of `C-0022`'s 21** whose finite tile
> carries *less* total force than a 1-D pressure over its footprint (−3.91 %), so its free-tile dishing
> field is **anti-parallel** to every other state's (cosine −0.943 to −1.000) and no force vector
> flattens both.

A distribution is tuned to a **load**, and the load is an operating state. All five of `C-0022`'s solved states, 3 × 15, dishing / stroke:

| state | uniform | rim × 5 / 6.70 | optimum | minimax |
|---|---|---|---|---|
| **2 mM, 10 nm, 0.192 V (design point)** | 0.2182 | **0.0753** | **0.0544** | 0.1247 |
| 0.5 mM, 10 nm, 0.134 V | 0.2086 | **0.0574** | **0.0638** | 0.1286 |
| 10 mM, 10 nm, 0.192 V | 0.2551 | 0.1179 | **0.0966** | 0.1587 |
| 2 mM, 5 nm, 0.368 V | **0.0796** | **0.0944** | 0.1085 | 0.1195 |
| 2 mM, 2 nm, 0.368 V | **0.0710** | 0.1867 | 0.2056 | 0.1587 |
| *uniform load (the falsifier case)* | *0.0486* | *0.1156* | *0.1228* | *0.1307* |
| **worst of the five** | **0.2551** | 0.1867 | 0.2056 | **0.1587** |

- **The uniform coupling is already flat at the two compressed states** and fails only where `C-0022`'s collar is deep, i.e. at the 10 nm gaps. Nobody had reported that: `C-0047`'s 21-state sweep is on 1 × 15.
- **The rim design is flat at three of five** and is *worse than uniform* at the 2 nm gap by 2.6×.
- **A minimax over all five** — the same machinery, one objective up — reaches a worst case of **0.1587**, so **no distribution found is flat at every solved state**. That is a *"not found"*, not a *"does not exist"*: the search is a descent from three starts at 45 paths, and the force-space floor forbids nothing.
- This is the **sixth** instance in this project of a quantity that is not well posed without the state it is read at — after stiffness-with-a-compression, variance-with-a-bandwidth, rupture-force-with-a-loading-rate, `k_es`-with-a-gap and flatness-count-with-a-load-case. **A flatness count now needs a load case *and* an operating state.**

---

## Deliverable 5 — the lattice beside the plate, with the excess quoted and its sign reported

| distribution | lattice / plate | excess |
|---|---|---|
| uniform 3 × 15 | 0.919 | **−8.1 %** (the lattice is the *softer* model) |
| rim × 2 | 0.990 | −1.0 % |
| rim × 5 over 8.94 nm | 1.130 | +13.0 % |
| **rim × 5 over 6.70 nm (flat)** | **1.239** | **+23.9 %** |
| **the optimum (flat)** | **1.124** | **+12.4 %** |

**The excess is a function of the DISTRIBUTION, not only of the count**, and it changes sign inside this study: the lattice is 8 % softer than the plate at the uniform coupling — `C-0047`'s value, reproduced — and 24 % stiffer at the flat rim design. `CLAUDE.md`'s *"a discretisation is not automatically a relaxation"* is confirmed a second way, and the direction matters here: **the plate alone would report 0.0608 where the lattice reports 0.0753**, a 24 % optimism, and the flat verdict survives on **both**. Optimising on the plate and transferring to the lattice gives 0.0668 — 23 % worse than the lattice's own optimum, and still flat.

---

## The five verification gates

Executed as **23 gate-named tests** in `src/test/kotlin/coupling/NonUniformCouplingTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a normalised distribution carries exactly the mandated total (`1e−12`) and preserves the weight ratios; the per-path ceiling is an allowable over a stroke and scales as `1/s` exactly; the admissible ratio is dimensionless and is **1.5** at 15 paths, **4.5** at 45 and **0.45** at 15 paths and the desired stroke; the dishing is **exactly linear in the applied pressure** (`1e−10`); unphysical arguments throw — a negative weight, an empty weight list, a zero total, a zero stroke, a ceiling below the uniform share, a stiffness vector of the wrong length | **PASS** |
| **2 — limiting cases** | **a uniform load on a free tile dishes exactly zero, lattice AND plate** (`< 1e−9 nm`) — the free falsifier; a stiffening ratio of 1 reproduces the uniform distribution identically; the capped projection at a ceiling equal to the uniform share returns the uniform distribution exactly, and at an infinite ceiling returns the plain normalisation; a load-matched distribution under a uniform load is the uniform one to `1e−15`; the optimiser is a **descent** and never returns worse than its start | **PASS** |
| **3 — symmetry and conservation** | the water-filling projection **conserves the mandate exactly** with the cap active (`1e−12`) and leaves nothing above the cap; support forces plus the foundation carry the whole applied load on a smoothly varying field (`1e−6`), with `C-0022`'s `C⁰` collar kink reported separately at under a tenth of a per cent; **a point-reflected distribution dishes identically on the lattice**, which is centro-symmetric and not mirror-symmetric (`C-0015`); **a mirrored distribution dishes identically on the plate**, which has the full rectangular group | **PASS** |
| **4 — numerical convergence** | **NESTED** subdivisions `1 ⊂ 2 ⊂ 4` at the optimum: `8.0e−3` then `8.5e−5`; the sampling grid 41/81/161 — `9.3e−3` at the optimum and **exactly zero at the flat rim design**, whose peak sits on a corner every grid contains; the plate basis degree 8/10/12: `1.1e−2` then `1.2e−3`; and the optimiser's own last-sweep improvement, reported per run at two significant digits (`0` to `6.6e−6`, every run converging rather than exhausting its sweeps) | **PASS** |
| **5 — literature and upstream cross-check** | **the Woodbury surrogate against the assembled solve, `1.5e−12` on the lattice and `1e−8` on the plate**, for a non-uniform distribution — the whole optimisation rests on it; `C-0047`'s **0.695** (1 × 15), **0.218** (3 × 15) and **0.308** (free tile) reproduced as the uniform limiting case to `1e−3`; `C-0026`'s free-tile stroke 4.90731 nm (`2e−7`); `C-0049`'s `perPathSecantCeiling` **called from its own library** and asserted equal to this task's ratio × mandate at 15 and 45 paths (exact); `C-0014`'s `√(k_BT k)/n` reproduced by the unequal-path generalisation at equal paths (exact); `C-0017`'s 33.3333 pN/nm | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a uniform load producing non-zero dishing on the **free** tile | **no** | `1.1e−10` nm, lattice and plate, and it is a test |
| 2 | the Woodbury surrogate disagreeing with the assembled solve | **no** | `1.5e−12` |
| 3 | the optimiser failing to reproduce `C-0047`'s uniform numbers at ratio 1 | **no** | three numbers to `1e−3` |
| 4 | a best-found dishing **below** bound 1 | **no** | 20.3× above it |
| 5 | **the 1 × 15 optimum buying more than a few per cent** — which would falsify `C-0047`'s bending-length reading | **partly** | it buys **13 %** admissible and 21 % inadmissible, which is more than *"a few"* and far less than what would change any verdict. `C-0047` stands: the 1 × 15 dishing is the along-helix bow and no reweighting of a single line of springs reaches it |

**A prediction of this task's own that failed, in code, on the first run:** the uncapped optimisation returned a **worse** point than the capped one it strictly contains — a descent over a superset that started elsewhere. The fix is structural rather than numerical: the capped problem now runs first and its answer is added to the uncapped problem's start set, so the containment is enforced by construction. The two now coincide to the last digit at 45 paths, which is itself the finding that the ceiling costs nothing there.

**A result that was not anticipated at all:** that the uniform coupling is **already flat at the two compressed states**, and that the rim design *breaks* that. The whole task was formulated at the design point, where the collar is deep; the same redistribution is a liability where it is not.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured**, and the flexure motif this count belongs to is not demonstrated (`C-0028`, `C-0029`).
- **Nothing here says a per-path stiffness can be BUILT to a prescribed value.** Every distribution assumes an independent, exactly specifiable linear spring at each station; `C-0030`'s flexure is a **span**, quantised by the lattice it is cut from and strain-softening (`CH-0042`). **The largest open item this claim leaves is whether a 5:1 stiffness ratio is realisable at all** — and the fact that the answer is a two-valued rule rather than 45 distinct values is what makes that question askable.
- **The load profile is `C-0022`'s** and inherits its whole validity range: mean field, point ions, a two-dimensional solve with the **corner bracketed rather than solved**, an **unsourced rim charge** worth 1.85× on the collar depth, and a gap filled with free buffer. The rim design is selected *by* that collar, so it is more exposed to that uncertainty than the uniform coupling is — a rim charge 1.85× different moves the collar the design is matched to.
- **Linear Winkler foundation at `C-0001`'s secant, ×1 only.** `C-0047` swept ×[0.25, 4] on the uniform 1 × 15 and found 1.9× on the *baseline*; the improvement measured here is not swept over it.
- **The optimiser is a DESCENT** reporting the best point it found, never a global optimum. The bound it is quoted against is rigorous; the optimum is not, and the minimax result in particular is a *"not found"*.
- **The crossover's vertical link is `C-0009`'s rigid PENALTY**, inherited unchanged; no thermal channel is computed on it (`CH-0033`).
- **One crossover layout** — `T-10`'s eight symmetrically centred columns; `C-0015`'s **32 bp phase is not swept**.
- **`T-5b`'s 10 % is a CONVENTION.** The flat verdicts here are at 0.0544–0.0949 against 0.10; the rim × 10 design at 0.0949 would **not** survive a 5 % tolerance and the optimum would.
- **No electrostatics is solved and no lateral coordinate is carried.** The dishing is out-of-plane only.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT**, not a measurement |
| crossover hinge stiffness `k_θ` | `α = 1` | **CITED, FITTED**, Chen et al. (2014) SI |
| crossover interface spacing | 32 bp | **CITED** via `C-0015` |
| `C-0022`'s solved collars | 21 states | **CITED**, read at run time from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on `(concentration, gap, bias)` |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| duplex shear allowable / nicked ceiling | 48 / 65 pN | **CITED** via `C-0006` |
| `RIGID_PLATE_TOLERANCE` | 0.10 | **CITED CONVENTION** from `T-5b` |
| §3 parameters | 100 pN, 3 nm, 10 nm, 40 × 40 nm | **CITED** |

Everything else — the Woodbury surrogate and its reciprocity, the force-space bound, the whole rim sweep, every optimisation, the state table, the load-path costs and the lattice/plate excesses — is **derived here in code**, with `C-0026`'s pipeline **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether a 5:1 per-path stiffness ratio can be built**, and at what quantisation. `C-0030`'s flexure stiffness goes as `span^−3`, so a 5× ratio is a `5^(1/3) = 1.71×` span ratio — plausible on its face and not checked here.
   **ANSWERED by [`C-0060`](C-0060-buildable-stiffness-ratio.md) (`T-122`, iteration 10): YES on the stiffness, NO on the placement.**
   All seven settings of the five catalogue elements reach both levels — the coarsest quantum is 19.1 % of a level's own stiffness against a flat ratio window measured at `3.5 ≤ R ≤ 20`, so quantisation is 25× finer than the requirement — and all fourteen built designs are flat (0.0715–0.0815).
   Two qualifications this claim's own text does not carry: **rounding the two levels independently misses `C-0017`'s mandate by up to 5.44 %**, recoverable only by trimming individual paths one base pair (3–4 distinct settings, not two); and the `1.71×` span ratio above is exactly what breaks the **array** — the interior span is 52.36 nm on a 40 nm tile and six of seven elements place 0–30 of the 45 stations, `C-0041`'s obstruction unchanged and made worse.
   `C-0060` also finds the best one-parameter ratio at this claim's own 6.70 nm collar is **7, not 5** — 0.0653 of the stroke against 0.0753, a further 13.4 %, and no harder to build.
2. **Whether a distribution flat at every operating state exists.** The minimax found 0.1587 from three starts; a better search, more paths, or the placement freedom below might reach the tolerance.
3. **The placement itself.** Every station here sits on `C-0026`'s grid. `C-0047`'s stagger sweep found 45 % on an axis nobody had swept, and this claim's own finding — that the family converges on a *placement* — says that is where the remaining room is.
4. **The foundation multiplier**, held at `C-0001`'s secant throughout.
5. **`C-0015`'s 32 base-pair crossover phase**, unswept here as in `C-0047`.

## Challenges

**Raises [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md)** against `CH-0034`'s Ground 2 — that the 0.149 residual is *"a property of the tile's rim, not of the coupling"* and is *"not bought with attachments"*. No count, no table and no remedy of `CH-0034` moves; what moves is the word **floor**.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that per-path stiffnesses cannot be set independently.** Then the design variable does not exist and the whole claim is a statement about an unbuildable coupling. This is named in the validity range as the largest open item, and the two-valued form of the answer is what makes it a fair question rather than a fatal one.
2. **A `C-0022` collar materially different from the solved one.** The rim design is selected by the collar's *existence*, not its depth — the uniform coupling fails at every 10 nm state and the rim design is flat at two of them — but the best collar width would move.
3. **A tolerance materially tighter than `T-5b`'s 10 %.** At 5 % only the optimum survives, and at 2.5 % nothing here does.
4. **A requirement that one distribution be flat at every operating state.** That requirement is *not met* by anything found here, and this claim says so in its own Deliverable 4.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
