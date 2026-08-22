# ANSWERS — the Gen-1 questions, as posed

This file answers [the problem definition](third-party/2026-08-ndi-gen1-problem-definition.md) in its own terms:
the eight tasks of §6, the open questions of §4, and §7's test of whether the loop worked.

It is a **synthesis, not a source**.
Every number here belongs to a claim in [`gpd/claims/`](gpd/claims/), and the claim carries the provenance,
the validity range and the verdict.
Where a claim has been challenged, the challenge is named — this project does not overwrite results.

**Maturity: TRL 1–3 throughout.**
`PASS` means model-consistent and traceable.
**Nothing in this repository is measured.**
Conditions are 300 K, aqueous buffer with stated Mg²⁺, `k_BT = 4.142 pN·nm`, unless a row says otherwise.

---

## 1. The short version

Three findings dominate, and none of them is a stiffness number.

**And the programme now has a stated output-element recommendation** (`C-0071`, iteration 14):
`C-0069`'s `Q5` — a **hinge-rooted arm of 8.16439 nm = 24.0 bp**, rooted on **one** antiparallel crossover at
the unused out-of-plane azimuth and tipped on a duplex end, **34 instances at one level**, 2.941 pN per
path, flat at **0.0706** of the free-tile stroke at the design state and 0.0789–0.0896 across its device's
whole traversed range **with 34 equal springs and no tie grid at all** — at §3's **acceptable** clause,
the desired one being unreachable on §3's own stack (`C-0050`).
The decision needed no new calculation: 11 catalogued elements → 3 place → 2 survive every clause, and the
three tie-break axes were unanimous.
**Its price is stated with it**, and is the honest part: **20 premises of which 3 are UNDEMONSTRATED**
(the free lever on one crossover, not found in 62 recorded queries; the normal-standing duplex; and that an
**in-plane-fitted** `k_θ` transfers to the **out-of-plane** azimuth, which no measurement covers);
**no margin at all on 3 of 14 graded quantities** (1.00314×, 1.01844×, 1.02964×) — and those three turn out to be
**one arithmetic**, `pitch − d − L = 0.0256 nm`; **4 specification questions still binding**; and
**9 failure routes, 5 of which remove the element**, two of those already inside a published bracket.

**And the tolerance model, run one iteration later, does not clear it** (`C-0072`).
The three unmargined quantities and `C-0066`'s tie clearance are **one lattice quantity**,
`M = p − d − L` — the two claims differ only in how they group the subtraction and agree to `1e−12` nm,
which neither had noticed. **Four floors exceed that 0.0256 nm and none of them needs a fabrication
measurement**: the base-pair **rise** (13.28×, so *the margin is below the finest length any DNA design can
specify*, and no correction recovers it), the disagreement between the two measured SAXS interhelical
distances of the same material (1.56×), the **thermal axial** breathing of the two segments the margin
differences (10.46×), and the arm tip's own bending **at a perfectly rigid root** (70.6×, so no joint
stiffening escapes it).
**The literature was expected to return a negative and returned four measurements**, three of them in
supplementary material their own main texts never discuss. Fischer et al. (2016) fit the **single-layer
sheet's** lattice-constant width at **9.1 %**, which is **9.76× the margin**; Bai et al. (2012) measure the
interhelical distance as a deterministic 18.5 → 36 Å **sawtooth**, so 2.69 nm is a *Bragg lattice constant*
rather than a spacing; Strauss et al. (2018) map staple incorporation at **48–95 %**.
So **`T-45`, open since iteration 3, is answered from published measurement — and the answer is a failure.**
The escape is a **reduced path count** (30 paths dissolve the four forced rows of three and buy 53× of
margin) and it **loses the flatness**, 0.0706 → 0.2603: *margin and flatness are bought from the same four
arms.*

**SUPERSEDED IN BOTH HALVES, iteration 15** — and the correction runs the favourable way on the margin and
the unfavourable way on the springs.
The **53×** was read at `C-0072`'s own reduction rule rather than at the lattice, and *a plan ceiling is a
property of a placement, not of a count*: the largest element a 30-root placement can keep is **9.5350 nm**,
so the margin is **1.76451 nm — 68.9×** the knife edge and **1.31×** `C-0072`'s own figure (`C-0074`,
`CH-0086`).
The **0.2603** is that same rule's output and an *upper* bound.
Searched rather than ruled, **with equal springs there is no flat 30-root placement at all** — the
exhaustive centro-symmetric family reaches **0.166653** at phase 24 and **0.172575** at phase 8, and a
descent over the non-symmetric family at **every one of the 32 phases** reaches **0.1670**, all outside
`T-5b`'s 0.10 — while a **distribution** at `C-0017`'s unchanged total recovers it: **0.06822** over the
whole range the placed 2 mM device traverses, at a peak stiffness ratio of only **2.057** and **6.857 pN**
per path.
So the escape does not cost the flatness; **it costs the equal springs** — the one thing that made
`C-0063`'s 34-root placement remarkable — and it moves the crossover phase from **24** to **8**, where the
same construction at phase 24 reaches only 0.11239. (`C-0074`; and `C-0075`, which finds the ceiling is a
**step function** of the path count with its step at exactly 31 — 8.19 nm above 30, **9.535** from 30 to 24,
**14.975** at 22, **30.88** at 15.)

**And the 0.0256 nm itself is a margin against a number nobody has measured in the role it is used in**
(`CH-0089`, `C-0076`).
The `d` in `p − d − L` is the **girth of one free duplex**; the 2.69 nm it is evaluated at is the SAXS
packing distance of a **crossover-bonded pair**, which is a different quantity.
At this repository's own measured phosphate contact — **1.817276 nm**, from `T-71`'s 13 084 crystallographic
linkages — the same clearance is **+0.898333 nm, 2.64 base-pair rises and 35× the published 0.0256**, and
**three of `C-0072`'s four floors stop firing** (the base-pair rise falls from 13.28× to **0.38×**; only the
arm-tip bending survives, at **2.01×**, which `C-0072` itself calls a floor of *resolution* rather than of
failure).
Two of the four measurements also read differently now: `C-0072`'s **weave bracket is withdrawn** — it
substitutes an *across*-helix separation of two *bonded* duplexes into an *along*-helix clearance between
two *unbonded* ones, and on the axis where the weave does live all **34** of `C-0063`'s stations sit on a
**node** at every one of the 32 phases (worst departure `4.4e−16 nm`), so the disputed **1.2–1.75 nm**
amplitude bracket has coefficient **exactly zero** (`CH-0088`) — while Fischer's **9.1 %** and Strauss's
**48–95 %** stand as written.
**That exactly-zero is conditional on the tile having no SEAM, and the condition is met** (`C-0081`,
`C-0086`).
A seam does not perturb the weave, it **deletes an extremum** — removing the junctions at one plane removes
exactly one pull event from **every** duplex — so it puts **6–12 of the same 34 stations** off the node at
every one of the 8 seam positions a 40 nm tile admits, and restores the annihilated **1.2–1.75 nm** bracket at
full strength: worst across-row clearance **−0.0023 nm**, a clash. What removes the seam is that **a seam is a
parity on a tree rather than a fabrication convention** — a fully folded *circular* scaffold gives every row
two segments — and the Gen-1 sheet takes only **1 680** of M13's **7 249** nt, so it is not fully folded and
the routing is the plain boustrophedon. `C-0076`'s verdict is unmoved either way, on its other argument:
`M = p − d − L` carries no weave coordinate, so the plan margin is **0.898333 nm at all 8 seam positions**, one
distinct value. *A verdict that survives can survive on a different reason*, and this is the first instance
here where the surviving reason was named in advance.
**`T-139` HAS LANDED, and the answer is that the quantity does not exist as a separation** (`C-0079`,
`CH-0094`, iteration 16). Two unbonded duplexes are repulsive at **every** separation on four independent
methods, each read directly, so they hold no equilibrium spacing at all and the plan model's `d` is a
**threshold on an energy** rather than a distance: the map runs **11.45 nm at 0.5 `k_BT` to ≤ 2.1 nm at
8 `k_BT`** and **straddles** `C-0076`'s step function at **2.715609 nm** — 34 of 34 instances placing below it
and 22 above. So every plan claim in this branch states a width and none states the energy budget it was read
at, which is the eighth instance of this file's own *quote it with the state it is read at*.
**And the collinear term of that same margin is set by an energy too, and is QUANTISED** (`CH-0093`,
`CH-0100`, `C-0085`). Two collinear arms are **coaxial**, not crossed and not parallel, so what the clearance
has to prevent is a blunt-end **stacking bond** — an established origami motif, **−4.4114 `k_BT`** per helix —
and not a clash; holding the stacked state below one per cent on the softest closure path asks **1.90518 nm**,
i.e. **6 base-pair rises, 2.04 nm**. And a gap between two duplex **end faces on a common axis** is an *axial*
length, so the whole margin is an **integer count of rises**, `M = (32 − N_d − N_L)`: the published
**0.02561 nm** is what is left over when a *transverse* SAXS lattice constant (**7.912** rises) and an elastica
root (**24.013** rises) are subtracted from an integer pitch. The buildable margin is **2 whole rises,
0.67561 nm — 26.38×** the published one, and **all three of `C-0071`'s live `NONE` bands become real
margins**: the plan length at two whole rises, the tip ceiling **79.678 → 133.687** (**1.7088×** its demand)
and the root ceiling **13.930 → 25.689** (**1.8988×** one crossover), against 1.018× and 1.030× as published.

**`C-0071`'s recommendation therefore stands as the best element the catalogue contains and NOT as a
buildable design**, which is what TRL 1–3 means here and is the honest reading.
**The ground of that verdict has moved TWICE, and the second move runs the favourable way.** As written it
rested on a 0.0256 nm margin exceeded by four floors; iteration 15 replaced that with the statement that
**the exclusion width in this role is unmeasured**, which no tolerance model can repair; and iteration 16
settled it by **removing the question** — there is no separation to measure, only an energy budget to state
(`C-0079`) — while iterations 16–18 turned the collinear term into an integer count of rises with **2 whole
rises, 26.38×**, of margin (`C-0085`). So the knife edge is gone and the recommendation does not rest on it.
**What it does rest on is unchanged**: 3 undemonstrated premises of 20, the free lever on one crossover being
the largest of them, and that remains the larger exposure. A recommendation whose *stated* ground has been
replaced three times in four iterations, with the verdict never moving, is worth reading as such.

**AND §3'S OWN 40.0 nm TILE IS NOT A BUILDABLE RASTER WIDTH** (`CH-0101`, `C-0086`, iteration 18).
A seamless boustrophedon has only **progressive** scaffold crossovers, so Rothemund's own constraint — the
distance between successive scaffold crossovers must be an **odd number of half turns** — binds the **row
length**, which must therefore be an odd multiple of the 16 bp crossover spacing: **16, 48, 80, 112 or
144 bp**, and nothing between. **40.0 nm is 117.6 bp.** The nearest admissible width is **112 bp = 38.08 nm**,
**4.8 %** narrower, and the step is **32 bp = 10.88 nm** rather than the base-pair rise, so no tolerance
argument reaches it — this is the second quantity in the programme, after `C-0072`'s margin, that is below the
resolution of the design language rather than merely tight.
**The tile survives it, and the correction SELECTS the design the programme already had** (`C-0090`,
`T-153`). 38.08 nm is *exactly seven column pitches* where 40.0 nm is 7.35, so the row-end scaffold crossover
— the one that turns the raster — is a lattice point only at the phases `b ≡ 8 (mod 16)`, i.e. **8 and 24**,
which are exactly `C-0063`'s two centro-symmetric phases; `C-0015`'s **ten** eight-column phases collapse onto
the same two. At them the upward station lattice is **bit-identical** to the 40 nm one (departure `0.0`), and
the best 34-root placement dishes **0.0621469105** of the stroke against `T-5b`'s 0.10 — **12.0 % FLATTER**
than `C-0063`'s 0.0706145537 at the nominal width. The price is that the **arm must be quantised**: the binding
plan ceiling switches from the inboard `pitch − d` = 8.19 nm to the **outboard** `edgeX/2 − pitch` = 8.16 nm,
the two crossing at `edgeX = 2(2p − d) = 38.14 nm` with 38.08 falling **0.176 base pairs** below — so
`C-0039`'s elastica arm overhangs by **0.00439 nm** and `C-0085`'s **24-rise, 8.16 nm** arm is exactly tangent,
restoring the capacity 38 → 45. (`CH-0105`: the plan budget was always a **minimum of two bounds** and only
ever reported one of them; only the outboard bound carries the tile width, and only the inboard one carries the
interhelical distance, so `C-0085`'s widening of the inboard bound buys nothing on the rows that bind.)
**Fourteen of the tile's crossovers are then ROW-END crossovers, whose mechanics Rothemund says are unknown**
(`CH-0111`). That they **exist** is settled: the raster turn *is* a row-end crossover — it is what makes the
row 112 bp long — a duplex end offers exactly the **one** crossover a boustrophedon demands, caDNAno automates
it, and Rothemund's own 24-helix rectangle is **288 bp = 18 column pitches exactly**, both edges on the
crossover lattice, folded **90 % well-formed** (`C-0095`). `CH-0111`'s stiffness bracket — 0.0621469105 to
**0.168371808**, one end inside `T-5b`'s 0.10 and one outside — is then **97 % not a stiffness**: decomposed
it is **2.85 % the dihedral spring, 97.40 % the vertical link and −0.25 % the mesh node**, and the link is a
**constraint** expressing covalent continuity, which `C-0095` has settled (`CH-0115`). Sweeping the only
elastic element of the three over its **whole** range — an interior crossover's `k_θ` to exactly zero — moves
the answer to **0.0651753854**, a factor of **1.0487309**, and the threshold is empty: there is no crossing
(`C-0099`). **What a row end does carry that moves a verdict is a PRESTRAIN** — item 1 below.
**Nothing electrostatic rides on the width**: re-solving `C-0022`'s 2-D tile edge at 38.08 nm moves the whole
edge effect by **0.0400 %** (`C-0100`). What moves up to **2.15 %** is the *split* between the collar's two
load terms, and that split is a property of the lateral **mesh** rather than of the field, their **sum** — the
global momentum flux — moving a fifty-fourth as much (`CH-0116`).
**The phase is now over-subscribed, and one of the three demands has to be dropped** (`C-0102`, `T-171`).
At 38.08 nm the richest upward inventory is `{0, 16}` while the eight-column and centro-symmetric demands have
both collapsed onto `{8, 24}`; the richest set is **disjoint** from them, and no phase serves all three.
(`CH-0121`: `C-0102`'s headline calls all three sets disjoint, and two of them are *identical* — its own
census table says so, and the verdict does not depend on it.) **Phase 8 is recommended**, at
**0.0658484805** of the free stroke against the richest phase's **0.125068659** on the same descent: the
inventory demand buys **1.056–1.119×** on the two published redundancy slopes and the seven-column host it
requires costs **1.899×**, because a seven-column sheet splits its columns **4/3** so the *series* `D_⊥` loses
`6/7` where the smeared reading loses `7/8` — exactly `48/49` apart — and under `C-0087`'s measured
incorporation its chance of losing **every** crossover on some interface is **3.58698588×** an eight-column
sheet's. `CH-0118` then adds fifteen upward stations and no columns at phases 0 and 16, which is what makes
the richest set richest and does not make it flatter.

1. **The tile is not a rigid plate, and the picture has to go.** It is rigid *exactly* under a uniform load —
   at any flexural rigidity — and dishes under every departure from uniformity, including the unavoidable one
   at 300 K. A point-coupled lever and an area-averaging charge sensor **do not measure the same displacement**;
   the *free* tile dishes **32 %** of the stroke under the solved electrostatic load, and a *uniform* coupling
   does not remove it. §4(g)'s own test for abandoning the rigid-plate assumption is met, and met by a load
   nobody chose.
   **CORRECTED, iteration 12** — this passage read *"that part is **irreducible** — it is forced by the tile's
   own electrostatic edge, which no coupling choice can remove"*, and that is no longer true. `C-0022`'s 32 %
   is what survives a **perfectly distributed** coupling, and a perfectly distributed coupling is not the best
   one: at the *same* 33.3333 pN/nm total, **distributing** it across `C-0015`'s 45 stations reaches **0.0753**
   of the stroke (`C-0058`) and **placing** 34 **equal** springs on `C-0055`'s upward lattice reaches
   **0.0706** (`C-0063`) — both inside `T-5b`'s 0.10, against **0.3079** for the same tile carrying no
   coupling at all. What is irreducible is the **load**: the rim gains force, and no coupling changes that.
   **But both of those numbers are read at ZERO fabrication defects, and that is a state no build reaches**
   (`C-0087`, iteration 17). Under the only per-staple incorporation statistics anybody has measured —
   Strauss et al.'s map of all 168 staples of a Rothemund rectangle — **every flat Gen-1 design stops being
   flat**: not one of sixty `placement × convention × mandate` cells is inside `T-5b`'s 0.10 even at the
   median. What does it is **not** the position dependence but the sheer sparsity of an optimised array —
   **one** missing path takes `C-0063`'s 0.0706 to **0.5010**, because an exhaustively optimised placement is
   a *cancellation* and a cancellation has no tolerance to a missing term — a statement that holds along the
   **distribution** axis and, `CH-0109` charges, fails **across topologies**, the amplification column being
   what hides it.
   **The dropout also reverses the ranking**: the 34 equal springs beat the 45 two-level paths at zero defects and lose under fabrication,
   the denser array losing less per absent path. So the honest statement is that the tile can be made flat
   **as designed** and has not been shown to be flat **as built**. **`T-155` then closed that gap and the
   answer is NO** (`C-0089`, iteration 18): over 22 graded `placement × distribution` cells the lowest
   90th-percentile dishing anywhere is **0.2845** of the stroke, still **2.85×** `T-5b`'s convention. The
   recovery route is the right one — the percentile falls monotonically **0.8522 → 0.5327** as the path count
   goes 15 → 90, and optimising the *percentile* rather than the zero-defect value is worth a further
   1.30–1.61× — but **what refuses it is a COUNT**: the density the dropout demands is **13 attachment
   columns, 195 paths**, against the **34** the plan admits, **5.7× short in a division that needs no solve**.
   And the regularity `C-0087` read into the reversal is itself a count effect — **at matched count the
   irregular upward roots beat the regular grid**, 0.5837 against 0.6690. So the flat Gen-1 tile is a
   zero-defect result, and no coupling this lattice can carry makes it a fabricated one.
   **But the negative belongs to FIXED distributions, and that is the sharpest thing in it** (`CH-0104`):
   the *oracle* floor — the best stiffnesses chosen after the absences are known — reaches **0.00111–0.01988**
   on the same realisations, **255×** better. Nothing here is refused by geometry. What refuses it is that a
   coupling is **specified before it is folded**, and the oracle/fixed gap *widens* with the path count, so a
   reachable floor can exclude a design and can never license one.
   **A second topology was then tried and narrows the negative without removing it** (`C-0093`, `T-162`):
   tying the tile to one **stiff shared body** rather than to an array of independent paths is not a
   rescaling but the same system with one term added, and it moves `C-0017`'s mandate into the body's
   **ground** — a rigid-body mode of the tile, invisible to dishing — which frees each tie from 0.98 to
   **3.33 pN/nm**. At zero defects it is **2.05× flatter** than the array on the identical stations
   (**0.0344013403** against 0.0706145537), and under the measured dropout it reaches **0.24028028** at the
   90th percentile, the lowest this programme has attained and **1.18×** better than any array. It still
   fails: the redundancy slope is **2.96×** steeper but crosses `T-5b`'s 0.10 only at **252 ties** against
   the **53** the lattice offers — **4.8× short** where the array was 5.7×. And a *buildable* body loses
   where the rigid limit wins (a four-layer honeycomb brick reads 0.100166871, worse than the array), so
   body rigidity is first order rather than an idealisation. **The cleanest obstruction is a specification
   gap rather than physics**: a body tied at many out-of-plane sites *is* multilayer origami, and §3 names a
   single-layer tile — which is `T-166`, item 6 of [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md).
   **ANSWERED, 2026-08-18: yes, and the permission given is larger than the one asked for** — NDI's remedy for
   M13's 4.31× scaffold excess is *"just make the tile thicker"*, i.e. the shared body **fused** to the tile
   rather than tied to it. **So this whole row is a result about a 2 nm tile, and §3 asks for a ~10 nm one**
   (`T-191`).
   **And the last unspent axis was then spent, and it runs the wrong way** (`C-0098`, `T-165`). Searching
   the shared body's *placement and distribution* on the crossover sites the lattice actually supplies —
   25 graded cells — the best 90th-percentile dishing is **0.375506727**, at 100 % exceedance: **3.76×**
   the convention and **1.56× WORSE** than `C-0093`'s 0.24028028, **because that figure was never
   buildable** — it sits on an abstract 90-station grid where the lattice offers at most 60 (`CH-0113`).
   Two things close the axis rather than merely failing it. **The distribution axis shuts as `1/t`**: the
   shared body's stiff limit is a *kinematic* constraint independent of how the ties are distributed, so
   the 1.30–1.61× `C-0089` bought was a property of a **divided** mandate — *the same division that makes a
   shared body flatter is what removes the axis*. And **the real lattice's redundancy slope is 2.08×
   shallower** than the abstract grid's, fixing the columns at **4** against the **13** the dropout demands
   — 141.44 nm of tile, **3.54×** §3's. (**That slope is challenged and the challenge is open**: `CH-0119`
   points out that it is fitted through six placement-**searched** optima, and on the same lattice, at the
   same phase, under the same ensemble, a *nested* chain fits **−0.740086889** where a searched one fits
   **+0.0610348337** — the same data, opposite signs, differing only in whether the placement was allowed to
   move (`C-0103`). `C-0098`'s **3.76× shortfall does not depend on the slope**; what moves is the number and
   the mechanism attributed to it, and `T-180` settles it.)
   **The count axis itself survives the transfer to the real lattice and is steeper there, not shallower**
   (`C-0103`, `T-163`, resolving `CH-0103`): at *fixed* station geometry the 34 → 30 reduction the plan margin
   asks for costs **+12.86 %** of the 90th percentile (**0.638498565 → 0.720607136**) against a plan margin
   that improves **68.9×** — 528 : 1 in relative moves — **but the move the programme actually recommends
   carries a phase change with it and is 8.68 % BETTER** (0.639129638 → 0.583664426), the count term being
   +12.86 % and the **phase** term **−19.0 %**. So the recommendation stands and its bookkeeping did not:
   **no claim in the corpus contained both terms of that trade until `C-0103`.**
   **And the count/phase trade must be read on the TOTAL, not on its split**
   (`C-0108`, `CH-0123`): the two-way decomposition defending the 34 → 30 reduction is **path-dependent** —
   count-first gives +12.86 % / −19.27 %, phase-first gives **−11.48 % / +2.93 %**, and on the search-free
   grid the phase term **changes sign** between orderings — while both totals agree at **0.0**. The
   recommendation rests on the total, so it stands; the split it was explained by does not. The count effect
   is also not uniform across the lattice: adverse at **27 of 32** phases and **favourable at 5**, and at the
   recommended phase 8 it costs only **+2.93 to +4.48 %**, 2.9–4.4× cheaper than the figure the trade was
   priced at. So the flat-tile question is closed on every **coupling** axis this
   programme can reach, and what remains is a fabrication yield or a specification change, not a design.
   **AND THE SPECIFICATION CHANGE ARRIVED, FROM THE SCAFFOLD** (`C-0109`, `T-191`, iteration 23). Every
   sentence above is derived on a **2 nm** tile; §3's own parameter row says *"Tile thickness ~10 nm
   (single-layer honeycomb)"*, `C-0086` measures the sheet at **1 680** of M13's **7 249** nt, and NDI's
   remedy for the excess is *"just make the tile thicker"* — three independent statements describing a tile
   this programme had never modelled. On it the negative does not survive, and **not because a coupling works:
   because the tile does not need one.** A four-layer honeycomb tile at the buildable 38.08 nm dishes
   ~~**0.0577199433**~~ **0.0978155002** (`C-0141`) of the stroke under the same solved collar **with no attachment coupling at all**, inside
   `T-5b`'s 0.10, against the single layer's **0.307902368** — and one circular M13 pays for **exactly four
   layers and not five** (**6 720** of 7 249 nt, 92.7 %). What survives, narrowed **4.6×**, is a statement
   about the *coupling*: the best coupled four-layer 90th percentile under the measured dropout is
   **0.116465044**, 1.16× the convention against the single layer's 0.532748246, and every coupled cell is
   worse than the uncoupled tile. **The unspent axis was the BODY, not the coupling** — which is where
   `C-0098` had genuinely closed it. Conditional on an interlayer coupling fraction measured at **0.26–0.33**
   and `C-0116` puts the flatness crossing at ~~**`f` = 0.0788618807**, cleared by **3.29690337×** at that band's adverse end~~
   **`f` = 0.276970522, INSIDE that band, where `15 × 4` dishes 0.101759944 and FAILS `T-5b`** (`C-0141`).
   **AND THE LINE THEN RAN THREE STEPS FURTHER, ALL FAVOURABLE** (iteration 25). The tile is **design (i)
   of the caDNAno paper** — folded, gel-analysed, one of three of seven cross-sections to give sharp monomer
   bands — and its own source recommends **`10 × 6`** instead (`C-0119`). That cross-section is ~~**6.6×
   flatter**~~ **4.06× flatter — 0.0978155002 against 0.0240648102, a ratio this file constructs and no claim
   states** — and **has NO threshold at all**, flat even at `f = 0` where the layers do not couple, so it does
   not depend on the calibration the `15 × 4` verdict rests on — at ~~**two-thirds of the footprint**, a
   specification trade rather than a free improvement~~ **0.929467162 of §3's footprint against `15 × 4`'s
   1.40084263×, i.e. at no footprint cost at all** (`C-0120`, corrected by `C-0141`). And **a COUPLED tile is flat under the
   measured folding statistics for the first time in this programme**: ~~9 of 16 graded cells at the mandated
   total, **all eight** on `10 × 6`, best **0.0278431488** at the sparsest coupling tested — **the
   cross-section worth 3.17109774× and the distribution carrying no consistent sign**~~ (`C-0118`)
   — **RE-GRADED at the corrected cross-section (`C-0142`): FOUR of 16, all four on `10 × 6`, `15 × 4`
   0 of 8 at both ends of the band, best 0.0680677948, the cross-section worth 2.13543134×.**
   And `C-0093`'s buildable
   four-layer body reads 0.100166871 against a 0.0344013403 rigid limit, so **body rigidity is first order**.

   **RESTATED, iteration 35 — the four-layer numbers above are corrected in place, and this is what moved** ([`C-0141`](gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md), [`C-0142`](gpd/claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`CH-0174`](gpd/challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md), [`CH-0176`](gpd/challenges/CH-0176-the-first-flat-coupled-tile-is-flat-at-half-its-cells.md); was *"UNDER REVISION, iteration 33"*). **The cross-section every four-layer claim in this corpus was written on is not a honeycomb**: a honeycomb spends `3√3/4·d²` = **8.35449857 nm²** of plan per helix and the standing model spends `d²` = 6.431296, **1.29903811×**, because the in-plane row pitch is `3d/2` and the layer pitch `d√3/2` and only their **product** is the cell — so every four-layer `edgeY` was exactly **1.5×** too small. Corrected, the **footprint ordering REVERSES**: `15 × 4` is 38.08 × **56.524** nm, **1.40084263** of §3's 40.35, and `10 × 6` is 38.08 × **37.504** nm, **0.929467162** of it. The free tile dishes **0.0978155002** on `15 × 4` and **0.0240648102** on `10 × 6`; `C-0116`'s interlayer threshold moves to **0.276970522**, **inside** the measured 0.26–0.33 band, where `15 × 4` dishes **0.101759944** and **fails** `T-5b` while `10 × 6` dishes **0.0255589305** and does not. The coupled evidence halves with it (`C-0142`): **four** of sixteen cells flat at the 90th percentile, **all four on `10 × 6`**, `15 × 4` **0 of 8 at both ends of the band**, best **0.0680677948**, the cross-section worth **2.13543134×**. **A coupled four-layer tile is still flat under the measured folding statistics, and now on stations a derived lattice supplies** — three of the sixteen placements are flat after snapping onto `C-0141`'s honeycomb ladder (worst snap **3.332 nm** inside a 3.57 nm ceiling), all three **equal springs**. Two further corrections travel with it: the top face carries **exactly one** rooting azimuth per helix at **30°** with **no perpendicular root anywhere**, so `C-0122`'s **90** and **60** stations are restored and `C-0128`'s oblique cost falls 6.01719478× → **2.67233333×** ([`CH-0175`](gpd/challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md)); and there is **no uniform honeycomb row length at all** — an x-raster carries **both** turn senses, so 38.08 nm is a withdrawn width and design (i)'s remedy is **112 / 108 bp = 39.44 nm** axial extent, `−1.40 %` of §3's nominal ([`C-0140`](gpd/claims/C-0140-honeycomb-raster-turn-sense.md), [`CH-0172`](gpd/challenges/CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md)).
   **RESTATED AGAIN, iteration 36 — three things the corpus filed in iteration 35 and this paragraph did not carry, and all three run the FAVOURABLE way** ([`C-0146`](gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md), [`C-0147`](gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md)). **(1) `38.08 nm` is not a withdrawn width, it is one of two readings of the same block.** Every x-raster row of the two-length block spans **112 bp = 38.08 nm exactly** — at all ten rows and at every one of `C-0140`'s five candidate pairs — and the 116 bp = **39.44 nm** extent exceeds it by exactly a **4 bp = 1.36 nm inter-row STAGGER** that no plate model here can represent. **Which of the two §3 names is a specification question** and is now decision **8** ([`T-242`](TASKS.md)); ~~it is worth **six flat coupled cells of eight against three** through a crossover column admitted by a **0.07 nm** slack against `CrossoverLayout.EDGE_MARGIN`~~ **that price is WITHDRAWN — see the iteration-38 restatement below** ([`CH-0185`](gpd/challenges/CH-0185-a-bounding-box-crossover-column.md), answered by `C-0148`; [`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md)), and the recommended cell — one column, ten paths, equal springs — is flat at **every** reading (**0.0662801686** / **0.0708759349**, and **0.0708859619** / **0.0754995025** at the measured band's adverse end). **(2) The 4 bp ragged face costs §3's flatness EXACTLY ZERO**, because a four-layer block's gap-facing surface is the outermost layer's **sidewalls** while a row length moves where a helix **ends** — a coordinate in the tile plane, at right angles to the gap. The ragged faces are the tile's **RIM**, so the coefficient on the normal-direction flatness field is exactly zero and `T-5b`'s convention cannot read it; the residual channel, a 2-row rim modulation at **7.608 nm** against across-helix bending lengths of **17.2310927** and **23.2114857 nm**, is bounded at **5.54399427e−05** and **1.68371917e−05** of the stroke against **0.0274976866** of headroom — a margin of **496×**. What it does cost is **plan budget**: 1.36 nm against a `C-0141` outboard ceiling that saturates at **2.380 nm**, i.e. **0.571** of it at 90 demanded paths. **(3) The 28 nt turn allowance is a CHOICE**, **4.66666667×** its own reach bound of **6** — and M13 affords exactly **8**, so a uniform 112 bp row fits by two nucleotides and only strained (**6.54349121–12.112167 pN**, at or past the 10 pN unzip allowable). The yield half is **declared unpriceable** and the **threshold 8 nt** is quoted instead. **And one comparison above is cross-convention** ([`CH-0191`](gpd/challenges/CH-0191-a-width-advantage-across-two-conventions.md)): `C-0140`'s `−1.40 %` *"beats the square lattice's `−4.80 %`"* reads a **bounding box** against a **row length** — a uniform square-lattice raster has no stagger, so read on the row length the two are **both 38.08 nm and the advantage is exactly zero**, and read on the bounding box the honeycomb wins by the **1.36 nm stagger itself**. See §3 row (g) and the NDI table.
   **RESTATED AGAIN, iteration 38 — the recommended raster is `102 / 109 bp`, not 112 / 108, and the WIDTH FINDING DOES NOT MOVE** ([`C-0151`](gpd/claims/C-0151-closing-raster-selection.md), [`C-0148`](gpd/claims/C-0148-face-bond-class-residues-and-row-span-columns.md), [`C-0152`](gpd/claims/C-0152-forced-scaffold-crossover-price.md); [`CH-0194`](gpd/challenges/CH-0194-the-filter-and-closure-are-disjoint.md), [`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md)). **`112 / 108` does not close on caDNAno's own `±5 bp` scaffold-crossover rule**: it would need **10** of the block's **59** raster crossovers **forced**. Closure depends on the two row lengths only through their residues **modulo 21**, so the family is **441** cases and needs no solve — exactly **three** residue pairs close, they are the same on both 60-helix cross-sections, and **all three differ by `14 (mod 21)`**, so **the minimum stagger a DRAWABLE two-length honeycomb raster can carry is 7 bp = 2.38 nm** where `C-0140`'s selection filter admitted **at most 4**: the filter and the closing family are **exactly disjoint** and could not have returned a buildable design at any lengths (`CH-0194`). Selected inside the closing family, **`102 / 109`** ties at the **same 116 bp = 39.44 nm** extent and `−1.40 %` — **so nothing in the width finding moves** — and is strictly best among the three pairs that tie there: **10** crossover columns against 9 and 6, **55** stations against 50 and 40, a 7 bp stagger against 14 and 28. **The whole cost of closure is ONE crossover column**, because the two pairs give the **same tile** and differ only in the **interface window**, 102 bp against 108: **2** flat cells of eight against 3, a uniform **1.0567397–1.09611647×** of the dishing, and **the recommended cell survives at both ends of the measured band** — **0.0773373597** at `f = 0.30` and **0.0821458169** at `f = 0.26`, against `T-5b`'s 0.10 and uncoupled references of **0.0281953496** and **0.0299114053**. The ladder phase is **determined at 16** with the **14 bp** inter-row offset, carrying **55 of 60** stations (`0.0868025325` when the recommended cell is snapped onto it, a **12 %** cost, and still flat), and closure **buys** 270 nt of scaffold — **6 330 nt** of M13's 7 249 on `10 × 6`, **919** spare — which reads as **15** unpaired nucleotides per helix against 10 at 112 / 108 and 8 at a uniform 112 bp row, so the strained uniform-row loop route of the previous paragraph's item (3) **gains** headroom. The blunt-end stacking clearance goes from **0.18** of a base-pair rise — below the design language's own quantum, so by this project's rule **not a quotable margin at all** — to **3.18**. **And closure is a reason to PREFER `102 / 109`, not a proof that `112 / 108` is unbuildable** (`C-0152`): caDNAno's rule is that software's **default**, the forcing tool exists in the same paper, and the **elastic** price of forcing is `0.350894669 k_BT` per crossover — **sub-thermal**, `2.84985806×` below one `k_BT` — with all ten costing **`0.438634952`** of one crossover column of a host sheet that folds. What is unpriced is the **kinetic** half, and `C-0152` records a negative existence result for it over **68** queries in **7** families. **Both graded column counts in the corpus, 11 and 12, were read at a raster that cannot be drawn** (`CH-0195`); at the drawable one the count is **10** at all three `EDGE_MARGIN` conventions — 0.05, 0.17 and 0.34 nm, slack **2.45 / 2.21 / 1.87 nm** — so the numerical guard decides nothing, and **decision 8's threshold goes with it**.

   **RESTATED AGAIN, iteration 39 — the recommended raster is now a FILE, its closure is CHECKED ON THE FILE, and the function that would have checked it had been passing a foreign design for the WRONG REASON** ([`C-0160`](gpd/claims/C-0160-scadnano-writer.md), [`C-0164`](gpd/claims/C-0164-lattice-aware-buildability.md), [`C-0161`](gpd/claims/C-0161-mechanics-on-an-imported-design.md)). The `10 × 6` block is emitted as `gpd/designs/gen1-block-honeycomb-10x6-102-109.sc`, and the **reference** scadnano implementation (0.21.1) loads it — and this corpus's own 15 × 112 bp sheet — with **zero warnings**, counting **49** staple crossings and **14** scaffold crossings on the sheet with its own parser: a third implementation agreeing with this corpus's census. `read → write → read` reproduces every lattice fact at **integer equality with no tolerance**, so the writer is the reader's *inverse* rather than a plausible second implementation, and the block's 55 stations, phase 16, 14 bp offset, 7 bp stagger, 102 bp interface window and **116 bp = 39.44 nm** extent are re-derived **on the emitted design**. **`C-0148`'s `±5 bp` closure is now derived from the file and the block reads `ADMISSIBLE` at ZERO forced crossovers** (`C-0164`): its **59** raster crossovers reduce by their own bond class to exactly **two** residues, `{4, 14}` — ten apart, which *is* the closure condition — and one `b₀` admits them; the withdrawn `112 / 108` fails at exactly the **10** forced crossovers `C-0148` found, *while passing the per-element rule*. The square and honeycomb width rules turn out to be **one statement**, `N ≡ step·Δ + {0, ±2·offset} (mod period)` — `C-0086`'s odd multiples of 16 bp at `offset = 0` and `C-0136`'s `7Δ + {0, 10, 11}` at `offset = 5`. **What moved is the GROUND, not the verdict**: `C-0160`'s declared falsifier `F2` **fired**, `checkBuildability()` having been **lattice-blind** — applying the square sheet's odd-multiple-of-16-bp rule to a honeycomb design — and grading the one design in this tree that nobody here drew found it passing `scadnano.origami_rectangle` on a **144 bp row width that is neither that design's 128 bp span nor any of its 48 / 80 / 128 bp scaffold runs**. **Two honesty clauses travel with this**: `ADMISSIBLE` means *"every rule this repository has for this design's lattice applies, and passes"* and **not** that anything is foldable; and the emitted block **carries no staple set**, so it is a lattice artifact rather than a design somebody could order (`C-0164`, `C-0160` §6).
   **One structural unknown does remain, and it was found by looking for a negative** (`C-0104`, `T-172`).
   A crossover **prestrain** is an *initial stress*, so it leaves the stiffness matrix untouched and enters
   the lattice as a **load vector** — which makes the whole axis one solve, and puts `T-5b`'s 0.10 at
   **15.45°** of uniform row-end prestrain. **The lattice's own register ladder reaches it**: the
   recommended placement holds the convention at the 8 bp (±4.286°) and 16 bp (±8.571°) rungs and loses it
   at **0.1013** at the 32 bp rung in the adverse sign. A re-optimised design absorbs it — 0.0711 at
   ±17.14° — but the *published* placement is the optimum at only 1 of 3 states, **so the recommended
   design is a function of an unmeasured parameter in a way `C-0099`'s row-end *stiffness* sweep was not**. No
   accessible source quantifies the prestrain, and Rothemund says as much himself.
   (`C-0006`, `C-0009`, `C-0022`, `CH-0005`, `CH-0025`; corrected by `C-0058`, `CH-0071`, `C-0063`;
   **conditioned by `C-0087`, `CH-0084`, `CH-0102`**;
   closed on topology and phase by `C-0093`, `C-0098`, `C-0102`, `C-0103`;
   challenged on the slope by `CH-0119` and on the oracle floor by `CH-0104`.)
2. **The polymer layer confines the tile in one direction only.** Its lateral restoring stiffness is *exactly*
   zero by symmetry — not small — so an untethered tile diffuses 63 nm in one 1 kHz period, 21× the positional
   predicate. It also exerts no upward force above `L₀`, so at zero bias the tile is unconfined in **both**
   directions. Nothing in the §3 stack holds it — **and `T-13` now says what does, and what does not.**
   What is unavoidably present, van der Waals across the gap, gives a **stable but not confining**
   equilibrium: its `1/h³` force has a bounded potential, so the well is only **0.2–5.7 `k_BT`** deep and the
   tile escapes it. **Stability and confinement are different properties.** A **one-sided** coupling — the
   ssDNA spacer that made `C-0017`'s lever compliant enough — supplies **exactly zero** downward preload,
   because a single strand carries no compression, and closing the hold-down then costs eight substrate
   tethers for 0.07–0.38 nm of stroke.
   **The requirement dissolves once the coupling is two-sided.** The potential above `L₀` turns from linear
   to quadratic, so the requirement stops being a *force* (`k_BT/σ` = 1.38 pN) and becomes a *stiffness*
   (`k_BT/σ²` = 0.4602 pN/nm) — one power of the position bound apart — and §3's own mandated 33.333 pN/nm
   exceeds it **72×** unpreloaded. The tetherless device goes from 1.4–5.4 `k_BT` and 0 of 18 confining to
   **959–7582 `k_BT` and 18 of 18**, and `C-0014`'s eight substrate tethers **leave the design**.
   (`C-0010`, `C-0021`, `C-0023`, `CH-0027`.)
3. **The output coupling decides the programme, and it is fixed by §3 rather than by the layer.**
   100 pN at ≤ 2 V is reachable with room to spare and drainage clears 1 kHz by 22× — and at 10 nm the
   operating point the device reaches them at is not one it holds by itself.
   But the number a lever must bring is **not** the 5–277 pN/nm the stability table suggested: the force
   delivered to a load over a stroke is `k_c·Δs`, so §3's own 100 pN and 3 nm fix it at **33.333 pN/nm** by
   arithmetic, and read at the bias the device actually operates at the stability floor is **0 at 5 and 7 nm
   and 23.4–27.9 pN/nm at 10 nm**.
   A coupling of **45 attachments — the same grid flatness already needs, and that grid is one attachment row per
duplex** — each a duplex standoff in series
   with a **13-nucleotide tuned ssDNA spacer**, supplies it: it *places* the operating point on its secant and
   *stabilises* it on its tangent, at 2.2 pN per load path against a 10 pN allowable.
   That element is `C-0017`'s `K2` and it is **not the committed design** — it is one-sided, which is exactly
   what `C-0023` had to replace (a single strand carries no compression), and `C-0041`'s plan view admits
   only **15** of its 45 members. The committed element is the two-sided coupled-standoff flexure below.
   **The design window is not empty.**
   The margin at §3's own 2 mM is 1.19–1.42×, inside its own mean-field error, so the verdict is
   *not excluded*, never established — and dropping to 0.5 mM buys six times more.
   Carrying **both** iteration-4 corrections it is **1.23–1.53×** — that figure being the **affine
   mandate's**, not the coupling the programme actually has — the polymer fluctuation bound *degrades*
   it to 1.11–1.25× and the finite-tile enhancement *restores* it to 1.34–1.67×, and the two are of the same
   size and opposite sign, so `C-0019`'s ≥ 1.07× was one half of a two-sided correction (`C-0027`).
   The verdict is unchanged in kind — *not excluded*, never established, because the **electrostatic**
   ~~one-loop error is 123–214 % and nothing in this programme narrows it (`CH-0019`).~~
   **RESTATED, iteration 33 (`CH-0167`, `C-0137`, `C-0139`): that percentage is an error bar on the LEVEL of
   the force, and this is a margin read at a force-pinned point, where the level is absorbed into the bias.**
   The same-kind thresholds are `C-0137`'s: the true force would have to be **1.48–2.22× smaller**, or the
   decay length **9.73 % shorter**, and nothing evaluable reaches either.
   The verdict is unchanged — *not excluded*, never established — and it now rests on the absence of a
   systematic theory at `Ξ = 17–24` rather than on a two-hundred-per-cent number.
   **And that reserve has since been spent.** Every margin above was banked on the coupling being
   strain-**stiffening**, which makes its tangent exceed its secant and hands stability a free reserve at zero
   placement cost. The coupling the programme actually arrived at, once its joint was solved rather than
   bracketed, strain-**softens**: `t/s` falls 1.095 → 0.757, and the assembled tangent has an *interior*
   minimum of 22.88 pN/nm at a 4.55 nm stroke, inside the operating range. Re-running the pull-in analysis on
   that law over 216 states, the 10 nm / 2 mM bias margin collapses to **1.0000–1.0019** — the device is
   placed *on* its own fold — and the fold's stroke walks back from 3.41–4.13 nm to **2.80–3.17 nm, through
   §3's own 3 nm target at two of six layer models**. Both escapes are priced and both fail: the adverse
   mounting is **42.38–61.04 pN/nm** (**CORRECTED, iteration 12** — read *"42.4–61.0"*, which is `C-0032`'s
   figure rounded and was the one number in this file that appeared in no claim at all), past the 40 pN/nm
   compliance ceiling at **0 of 8** lengths, and a shorter standoff lands 2.2 % short. At **0.5 mM every
   predicate clears** (1.44–5.93× on stiffness, 1.038–2.327× on bias).
   Two qualifications on that paragraph, both from later claims and neither moving the verdict.
   The **40 pN/nm ceiling has since been withdrawn** — `C-0049` shows it is exactly `1.2 ×` a *placement*
   mandate and carries the placement stroke inside it — so the adverse mounting no longer fails on *that*
   ground; it fails on `C-0035`'s, which is that both adverse mountings **cannot place §3's own effort point**
   at all. And the **22.88 pN/nm minimum is read at a 4.55 nm stroke the placed device never occupies**: read
   over `[0, s*]`, the range the convention `C-0049` settles asks for, the same flexure is **25.227 pN/nm**
   and clears **4 of `C-0017`'s 6** model floors at §3's own 2 mM, where the prescribed-range minimum clears
   none. `C-0032`'s **1.0000–1.0019** is nevertheless the standing statement — `C-0051` composed `C-0033`'s
   collar beside it at `C-0018`'s own fold and got **−8.40 to −11.06 pN/nm**, the collar recovering only
   **27–49 %** of what the element costs (`CH-0063`).
   **So 0.5 mM is no longer the comfortable choice with 2 mM still defensible — it is a requirement of the
   only coupling that survives.** That is a *specification* decision for NDI, and it is handed back as one
   rather than adopted here.
   (`C-0012`, `C-0016`, `C-0017`, `C-0030`, `C-0032`, `CH-0016`, `CH-0042`.)

4. **The design window survived nine claims and ten challenges by moving one edge, outward.**
   Of the four iteration-4 results aimed at it, three live on axes an intersection cannot see and the fourth
   is nearly cancelled by the part of the design that produced it.
   **Ten of twelve discovered axes do not resolve in grafting density**, and axes now *leave* the stack as
   well as arriving: the lateral-confinement footprint left when the in-plane tethers left the design, and
   `C-0049` withdrew `C-0023`'s 40 pN/nm tangent ceiling — it is exactly `1.2 ×` a **placement** mandate and
   carries that stroke inside it — replaced by a path count, `n·a/s`.
   **A second re-synthesis against three further iterations moves nothing at all**: `C-0031`–`C-0050` and
   `CH-0043`–`CH-0062` leave **0 of 6 window edges** moved, 0 grid steps, worst departure exactly `0.0`, no
   owner changed. That null is the finding, and its cause is `C-0016`'s own lesson turned on itself — of
   those twenty claims **exactly one** carries a quantity that is a function of `σ` at all. The window was
   not *survived*; it was never addressed. `C-0050` did produce the first genuinely `σ`-resolved constraints
   since iteration 4 (a kinematic ceiling and a validity ceiling), and at all 61 grid points **neither
   binds**, by 1.71–3.11×.
   **So a `(σ, L₀)` window is the wrong object for the Gen-1 decision, and the deliverable is now a height
   plus five specification questions** — and **the count survived two iterations in which two of its five
   members changed**, which is the shape drift takes in a list of open questions. As written at iteration 7 the
   five were `T-63` (the buffer), `T-95` (the superstructure), `T-102` (the tile area), `T-112` (which device
   §3's desired clause names) and `T-115` (a 17–26 nm layer). `T-95` and `T-102` were **discharged** by
   `C-0071`, and `T-154` (the scaffold, which decides whether the tile has a seam and therefore what width it
   can be built at) and `T-166` (whether §3 admits a **two-layer** tile) arrived in their place. Of the five
   live ones only `T-115`, a taller layer, can buy the desired stroke, and only `T-166` can reopen the flat
   tile. They are stated as questions in §5 below.
**ANSWERED, 2026-08-18 — all six, in one pass**, and the count drifted once more in the recording: `T-166` was
in [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md) and **never in `TASKS.md`'s own list of questions for Kazik**,
which is `C-0071`'s *"a discharge is invisible to the queue"* run backwards — an **addition** the register never
learned of, found only because six answers came back to a list of five.
   **AND THE TWO ANSWERS THIS PARAGRAPH RESTS ON HAVE SINCE BEEN MEASURED, BOTH NEGATIVE.**
   ~~Of the five live ones only `T-115`, a taller layer, can buy the desired stroke~~ — **withdrawn**
   (`C-0110`, `T-192`): §3's 100 pN stops arriving across a gap of **13.6989179 nm** at 0.5 mM, *below the
   bottom* of the 17–26 nm band, so a tall layer is refused at **96 of 96** states on the acceptable clause
   and admitted at **1 of 96** on the desired one. It buys the *stroke* and not the *force* — 52 of 96
   uncoupled states reach 10 nm — which is `CH-0127`'s displacement/force split. **A taller layer does not
   trade one device for another; it loses both.**
   ~~and only `T-166` can reopen the flat tile~~ — **superseded** (`C-0109`, `T-191`): it reopened, and by a
   route nobody asked about. The permission NDI gave is larger than the one requested — the shared body
   **fused** to the tile rather than tied to it — and on the resulting four-layer tile the coupling is not
   needed at all.
   **And decisions 1 and 2 turn out to be ONE decision drawing on one reserve**, which this programme asked
   as two: both of NDI's answers name *"additional operating margin"* bought with *"additional work on
   stabilizing DNA origami at low salt"*. Since `C-0110` withdraws the layer, that reserve now has **one**
   claimant — the buffer, worth **1.75104168×** at the state the device occupies on **three** surviving routes
   that are **common mode** below `C-0005`'s 123–214 % one-loop error, i.e. an error larger than any of them.
   (**RESTATED, `CH-0167`/`C-0137`, iteration 33**: the common-mode exposure is real and that percentage is the
   wrong measure of it — it is a LEVEL, and these three routes are read at a force-pinned point.
   The common-mode quantity is the *gradient*, whose threshold is a **9.73 %** shortening of the decay length.)
   What remains is a **price** question, which this programme has no column for (`C-0114`, and
   `DECISIONS-FOR-NDI.md` §1+2).
   **Coverage, stated so it is not mistaken for a verdict:** `C-0051` re-synthesises against `C-0031`–`C-0050`
   and `CH-0043`–`CH-0062`.
   **UPDATED, iteration 16 (`C-0080`).** `C-0052`–`C-0078` and `CH-0065`–`CH-0092` — 27 claims and 28
   challenges — have now been read against this deliverable, and the census `C-0051` taught us to report
   beside a null is this: **not one of the 55 carries a quantity that is a function of `σ`.** They are
   placements, plan views, lattice congruences, torsion closures, a tolerance model, a stated recommendation
   and two repository-numerics claims. So the window edges are unchanged and this time that is a reading
   rather than an expectation — **but it is still not a re-run**, and a re-intersection would cost a study to
   return the same null for the same reason it did in iteration 12.
   What the range does move is everything below the window: the output element, its price, and the flatness
   answer.
   (`C-0027`, `C-0051`, `C-0080`.)
   **UPDATED, iteration 29 (`C-0130`).** `C-0122`–`C-0128` and `CH-0147`–`CH-0151` have now been read against
   this deliverable, at a cheap bound of **0 of 11 cited** — the strongest product signal any pass has had.
   Four are carried (`C-0122`, `C-0125`, `C-0128` and `CH-0147`/`CH-0148`/`CH-0151`) and three are
   deliberately not (`C-0124`, `C-0126`, `C-0127`: a decision-file audit, a queue reclassification and a
   source repair, none of which any §6 answer depends on). **And row (g) was REWRITTEN rather than appended
   to**, which `C-0121` asked for: it had reached **23 414 characters in one table cell** with eleven revision
   markers and a verdict that had reversed twice, and it is now **under 2 500**, with the full derivation history
   preserved verbatim below the table — because the reversals are the most instructive thing here and deleting
   them would leave the answer looking inevitable.
   **UPDATED, iteration 26 (`C-0121`).** `C-0115`–`C-0120` have now been read against this deliverable, and
   for the first time in six passes the range is **not** dominated by scope corrections or by numbers with no
   `σ` in them: four of the six move headline answers in §1, §2 row 5b, §3 row (g) and §5, and they move them
   in the **favourable** direction. `C-0117` is deliberately **not** carried — it amends `C-0092`'s `A5`
   margin-movement range, which this deliverable never quoted. The window is again un-re-run and again for the
   same reason.
   **UPDATED, iteration 22 (`C-0106`) and iteration 24 (`C-0115`).** `C-0081`–`C-0114` and
   `CH-0093`–`CH-0131` have now been read against this deliverable. The census stays what it was — **not one
   of them is a function of `σ`** — but the fifth pass is the first in which the census is the *wrong* summary,
   because two of the items are not values at all but **scope corrections to what this file's answers were
   about**: §4(g)'s flatness closure was a statement about a **2 nm** tile and §3 specifies a ~10 nm one
   (`C-0109`), and the *"only route to the desired stroke"* was a statement about a **displacement** rather
   than a force (`C-0110`). Neither is visible to a numeric trace, a status check or a self-consistency check,
   for the reason `C-0106` established: **a determination with no passage is invisible to every check in the
   tree**, and all four were clean before this pass began. The window edges are again unchanged and again
   un-re-run.

And the shape of the whole problem, which no single number shows: **static stability wants the thin layer,
whose window is empty by 13.3×; the window, the stroke and the force-versus-height trade all want the thick
one, whose operating point is unstable everywhere.** All three pull the same way and stability pulls against
all three.

**Whether the tile is held at zero bias is a question about the coupling's *topology*, not about any force in
the stack.**
DNA's compliance is either entropic — which only pulls — or bending, which is signed, and the programme had
committed to the first.
A flexure or a crossover hinge at exactly the stiffness §3 already mandates turns a 1.4–5.4 `k_BT` trap into a
959–7582 `k_BT` confinement, with no preload and no extra part.
The element is settled; the **joint** was not, and it is `C-0025`: no covalent origami joint reaches the
free-to-draw-in reading, and the single-stranded hinge that would cannot *support* the beam, because a
flexible link has no direction and pays its axial slack back as transverse dead band.
The flexure is buildable on **normal duplex standoffs** — span **31.82 nm = 94 bp on 8 nm standoffs with a
two-crossover, favourable-orientation base**, tangent **25.23 pN/nm**, window **`ℓ = 5–10 nm`** — and `T-13`
still closes, because the joint changes the element's geometry and not its sidedness.
The **joint** is now solved rather than bracketed: the two standoff springs are the diagonal of one 2 × 2 read
with the other load zero, and the off-diagonal they dropped is not a compliance but a **draw-in the standoff
supplies** — 3.09× the demand it was set against, so the flexure runs in **compression** rather than tension
and the buckling margin rises **1.41 → 2.18×** (1.64× on the measured rigidity).
**But that supply is odd where the demand is even, so the coupling's law is no longer odd and its sign is a
mounting choice** — worth the difference between a window and none, and free to a builder.
**CORRECTED, iteration 12** — this read *"its sign is decided by which body carries the standoffs … and §3
does not say which"*, and `C-0035` (`T-75`) has settled both halves. The sign is a **product of two
binaries**, base body **and** standoff normal, so *"which body"* decides nothing on its own: of the four
mountings exactly two are favourable, **one with each body**. And it is a **determination**, not a
specification gap — of the two favourable mountings one puts the flexure inside the actuation gap, where the
array alone occupies **37–85 %** of the layer's own volume and sits at or below the electrode at §3's 5 and
7 nm layers, so **the survivor is unique**: standoff bases on the **output superstructure**, standoffs
pointing **away** from the tile, flexure **outboard**, each midspan tied back **down** through its own ground
to the tile. What `T-75` handed on is a *different* question, and it is a real one — whether that
superstructure may be **perforated** — which was open question 6 and is **`T-95`, DISCHARGED**:
`C-0071` found it had stopped applying, because the recommended element has no tie grid and nothing
crosses the standoff base plane.
The base is now specified rather than assumed: a **single** crossover buckles at every length, and the base's
**orientation** is worth 9.65×, because two crossovers react a moment as a couple and a couple has an axis.
(`C-0023`, `C-0025`, `C-0028`, `CH-0027`, `CH-0031`, `CH-0037`, `CH-0038`.)

**And the branch it depends on has since closed at §3's *desired* stroke.**
`T-67` found that the 90° routing **does** exist — both links close covalently with zero unpaired
nucleotides, and the optimum is a scaffold excursion — but that a duplex **end** has only **two strand
termini**, so its base is a hinge with a lever arm bounded by the duplex's own radius, 1.0 nm, against the
1.345 nm the design needs. A couple goes as the square, so the base is **3.34× short of a hard ceiling**, and
the two links lie on a chord, leaving one axis free — **which is the axis the column buckles about**.
**QUALIFIED, iteration 12.** `C-0057` (`T-71`) has since checked the *dihedrals* rather than the phosphate
distances, and they **do not close at any of the three reported optima** — `C-0029`'s single junction 0 of 4
links, `C-0042`'s pair 1 of 8, `C-0052`'s trio 2 of 6, four of the eighteen excluded by a closed-form reach
bound at **no torsion whatever**. *"A 90° routing exists"* survives and **the reported routing does not**
(`CH-0070`): a census of all **69 120** placements on `C-0029`'s own grid finds **3 546** closing on distance
and **18 of the 100 solved** closing at torsion level, at gaps around **0.690 nm** in the *interior* of the
measured window — where a distance-minimising objective never looks. `C-0059` then re-derived the junctions
on the feasible set and `C-0062` found the truss cap's trio does close, at every one of 21 configurations,
so **the truss branch is open rather than closed** and the routing question is a live one.
**What survived at the time was a crossover-hinge flexure instead: `E5g16`, a 12.24 nm = 36 bp guided arm on
16 antiparallel crossovers**, 2.04 pN per crossover against a 10 pN allowable, **with no member in axial
compression and no 90° junction anywhere in the design**. (`C-0029`, `CH-0039`, `CH-0040`.)

**Its `16` has since been withdrawn, and with it the `g16`/`a16` designs.**
A crossover serves one *interface* every 32 bp = 10.88 nm, so a hinge line of `n` needs `(n−1)×10.88 nm` of
collinear interface — **sixteen needs 163.2 nm, i.e. 4.08 tiles** — and the complete 32-phase census of a
40 nm tile gives **four**, at every phase. Sixteen can be assembled from four interfaces of four, but
interfaces compose in **series** and are worth 14.6 % of their count. The surviving hinge design is
**`E5a1`, one crossover per flexure** (`C-0040`).
Its arm was then solved as the elastica it is rather than composed: `C-0034`'s bracket premise — *"two errors
run opposite ways and very nearly cancel"* — is **false**, both readings correct the same linear
boundary-value problem and both stiffen it, so the exact composition lands **outside** their span
(`C-0039`, `CH-0053`).
And the tangent figures above were read against a ceiling owed at the **placement** stroke: `C-0023`'s
40 pN/nm is exactly `1.2 × (100 pN / 3 nm)`, a declared linearity tolerance carrying the placement stroke
inside it, so the same construction at §3's desired clause is **12 pN/nm, not 40** (`C-0049`). There is no
upper bound on a coupling tangent anywhere in the acceptance stack; what binds beyond the working point is the
per-path unzip allowable, `n·allowable/s`, which tightens as `1/s`.

**The literature finding that started it stands.**
No publication was found in which a duplex stands normal to a single-layer origami sheet as a stand-off:
out-of-plane duplexes there are hairpin or staple-extension *overhangs*, perpendicular helices in origami are
perpendicular *within* the plane, and **every body standing on an origami plate that has actually been built
is held by a pin** — which this programme has just shown to be a *mechanism*, `P_c = 0` exactly.
The only rigid out-of-plane mounting in print is **triangulated**, not clamped.
**This is the largest single buildability risk in the coupling branch**, it is stated rather than assumed
away, and it is `T-66` and `T-67`. (`C-0028`.)

**But the sheet's own crossovers are not the inventory a hinge must come out of.**
A square-lattice helix has **four** crossover azimuths at 8 bp intervals and a single-layer sheet occupies
**two** — `8 bp × 33.75°/bp = 270°` exactly, so the unoccupied pair points **out of the sheet plane**, and at
B-DNA's preferred twist it is off-register by **half** what the sheet's own next in-plane crossover is
(4.286° against 8.571°, the departure being linear in the offset). A 40 nm tile offers **161–176** junction
sites and builds **49–56**: it occupies **under a third of its own lattice at every phase**. So a hinge
rooted upward consumes **no** interface crossover, the sheet stays in one piece at every count, and the
hinge ceiling is **52–60** rather than the 42 a pigeonhole on the built crossovers gives. What it does *not*
buy is §3's 45 on a 40 nm tile: an upward line belongs to one duplex, so its roots sit at 10.88 nm against an
arm demanding 11.82, and the buildable count is **34** — against 25 in plane, and 45 only on a 49.25 nm tile.
**The site is published geometry and the free lever on it is not** — 62 recorded queries find no
crossover-rooted flexure hinge in print, and every published origami hinge is an ssDNA connection.
(`C-0055`, `CH-0068`; the geometry read directly from Ke et al., *JACS* **131**:15903.)

**Two of this programme's structural results are the same theorem at two scales**: an anchor's orientation
decides everything and its material almost nothing (`C-0014`), and a **joint** cannot be stiff in one
direction and soft in another unless it bends (`C-0025`).
Both are convexity, and both are escaped by the same thing — bending is signed, and has a direction.

**Orientation decides the anchoring problem twice over.**
`C-0014` found that an anchor's orientation *relative to the layer* decides everything and its material almost
nothing; `C-0020` finds the same for its orientation *within the plane of the sheet, relative to the helices* —
worth 11.75× in the effective allowable, at no cost in material, count or stroke.

Methodologically, the finding worth most is that **this project repeatedly caught itself** — concluding a
direction from the corrections it happened to have (`CH-0002`), quoting a `χ` that was a units error assembled
from an abstract (`CH-0012`), sampling a layout space it believed it had swept (`CH-0014`), trusting two
models that agreed with each other because they shared a defect (`C-0011`), quoting a loop expansion of one
field as the uncertainty on another (`CH-0019`), and carrying a number that was right only because two
convention errors cancelled (`CH-0020`).

---

## 2. The eight tasks of §6

| # | Task | Leaf | Verdict | Claim |
|---|---|---|---|---|
| 1 | Stiffness of the polymer layer | `A2.1` | **PASS**, then **superseded twice** | `C-0001` → `C-0003` → `C-0011` |
| 2 | Feasible design window | `A2.1` | **PASS** — non-empty at 7 and 10 nm, empty at 5 nm; `P2` closed by `C-0017` **for the affine mandate, and it FAILS for the coupling the programme actually has** (`C-0032`'s 1.0000–1.0019 at 2 mM; every predicate clears at 0.5 mM) | `C-0016`, `C-0017`, `C-0032`, `C-0051` |
| 3 | Stroke and blocking force vs bias | `A2.2` | **PASS** — reachable, but the operating point is not holdable | `C-0008`, `C-0012` |
| 4 | Electrostatic softening and pull-in | new | **PASS**, and now **DISCHARGED FOR THE RECOMMENDED DEVICE** — `C-0018` searched the **affine** mandate's load line and `C-0032` a strain-*softening* flexure's, and `CH-0083` charged that neither is `C-0071`'s `Q5`. `Q5`'s fold has since been searched: **no fold at 2 mM at 0 of 6 layer models** (`C-0084`), and the branch continued past `C-0084`'s own force-ladder artefact covers **0.9984** of the arm's contour (`C-0092`, `CH-0107`). **`CH-0083` is RESOLVED** | `C-0018`, `CH-0017`, ~~`CH-0083`~~ **RESOLVED**, `C-0084`, `C-0092` |
| 5 | Load distribution across the origami | `A1.2` | **PASS** | `C-0006`, `C-0009` |
| 5b | Deflected shape of the tile | `A8.2` | **PASS**, verdict *rigid plate rejected*. The successor question, *can it be made flat*, was **closed on every coupling axis this programme can reach** (count, distribution, placement, topology, phase) — **flat as designed**, 0.0621469105 of the stroke at the buildable width, and **not shown flat as built**, the best 90th percentile under the measured staple dropout being **0.2845** for an array and **0.375506727** for the shared body on the real lattice, against `T-5b`'s 0.10. **REOPENED AND ANSWERED FROM THE OTHER SIDE, iteration 23**: that closure is a statement about a **2 nm** tile, and §3 specifies a ~10 nm one. A **four-layer** honeycomb tile dishes ~~**0.0577199433**~~ of the stroke **with no coupling at all**, and the unspent axis was the **body**, not the coupling (`C-0109`). What survives is a 1.16× statement about the coupling under dropout, and the crossing that verdict is conditional on is now located: ~~**`f` = 0.0788618807** (`C-0116`), so the measured band clears it **monotonically** — **3.29690337×** at its adverse low end~~. See row (g) of §3 ~~**AND ON THE CROSS-SECTION ITS OWN SOURCE RECOMMENDS, A COUPLED TILE IS FLAT UNDER THE MEASURED FOLDING** (`C-0118`, `C-0119`, `C-0120`): the tile is design (i) of the caDNAno paper, that paper recommends `10 × 6` instead, `10 × 6` is **6.6× flatter with no threshold at all** at two-thirds the footprint, and **9 of 16 coupled cells are flat at the 90th percentile** — all eight on `10 × 6`, best **0.0278431488**. **The cross-section is worth 3.17109774× and the distribution has no consistent sign**~~, so the design order runs tile first, coupling second. **RESTATED, iteration 35 — the cross-section those four-layer numbers were solved on is not a honeycomb** (`C-0141`, `C-0142`, `CH-0174`, `CH-0176`): corrected, the free tile dishes **0.0978155002** on `15 × 4` and **0.0240648102** on `10 × 6`, the interlayer threshold moves to **0.276970522** — **inside** the measured 0.26–0.33 band, where `15 × 4` dishes **0.101759944** and **fails** `T-5b` — and the coupled evidence is **four** of sixteen cells rather than nine, **all four on `10 × 6`**, best **0.0680677948**, the cross-section worth **2.13543134×**. The footprint ordering reverses with it: `10 × 6` is **0.929467162** of §3's 40.35 nm and `15 × 4` is **1.40084263×** it. | `C-0006`, `C-0009`; `C-0087`, `C-0089`, `C-0093`, `C-0098`, `C-0090`, `C-0104`, `C-0107`, `C-0109`, `C-0112`, `C-0116`, `C-0118`, `C-0119`, `C-0120`, `C-0141`, `C-0142` |
| 6 | Validity boundary of mean-field screening | `A7.4` | **PASS** | `C-0005`, `C-0008` |
| 7 | Poroelastic drainage time | new | **PASS** — not binding, boundary named | `C-0004` |
| 8 | Tile positional variance | `A1.2` | **PASS** at the operating point, **partial** against the leaf | `C-0010` |

### Task 1 — stiffness

`L₀ = N a^(5/3) σ^(1/3)` is **replaced**, not merely re-parameterised.
It is a *two-body* result; the des Cloizeaux interaction gives `σ^(5/13)`, and the blob construction gives
`σ^(1/3)` again only because it minimises against blob elasticity rather than Gaussian.
So "which height law" reduces to "which elasticity", which is a checkable material question — and the check
says PEG in water is a **marginal** solvent whose Gen-1 chains carry 0.02–0.10 of one thermal blob.
The chains are not swollen, there are no blobs, and every blob-based statement made about this layer across
three iterations was about a structure it does not have.

Stiffness is **not a single number at the resting height** and never was: the strong-stretching pressure
vanishes quadratically at `L₀`, so three of six models give exactly zero there.
Quote it at a stated compression. At the working point, 47.7–64.1 pN/nm over the 40 × 40 nm tile —
a number owned by `C-0010` (its tangent at 100 pN), not by any of the three claims in the row above.

### Task 2 — the design window

**Answered, and the answer changes shape halfway through.**
In the axes §4(a)–(d) names the window is **not empty**: `σ ∈ [0.0116, 0.2885] nm⁻²` at 10 nm — 24.8× wide —
and `[0.0296, 0.0496]` at 7 nm.
**5 nm is empty**, and the proof names two constraints: the layer must be at least `σ = 0.0751 nm⁻²` for its
coils to overlap at all and at most `σ = 0.00563 nm⁻²` to deliver 3 nm of stroke, **missing each other by
13.3×**.
At both surviving heights the lower edge is coil overlap and the upper edge is the 3 nm stroke — §4(a)'s own
tension, quantified. **§4(c) and §4(d) bind nothing anywhere**, at any of 183 grid points.

**The window must be read in the FORCE-ONSET convention** (`L₀` is where the layer carries 1 pN over the tile).
It says: order **PEG of 1.6–3.3 kDa at 10 nm**, or 1.1–1.2 kDa at 7 nm, at a grafting spacing of 2–9 nm.
In the first-moment convention the same **layer is 4.2–8.7 kDa** — but that is a *different device*, whose
tile sits 16.1–18.0 nm above the electrode rather than at 10 nm, outside §3's own 5–10 nm band at every point
of the grid (`CH-0091`; and the banner this replaces, *"8–9 kDa … about four times"*, was itself built on an
exponent measured on a **different quantity** — `CH-0090` withdraws `C-0011`'s `N ≈ 190–210` and replaces it
with **175.08**).
**A convention mismatch is the single most likely way this window gets misread at a bench**, and `T-1e` has now
separated the two parts exactly (`C-0077`): the height convention is worth **2.82×** in the chain and the
physics — the conformational stress both trial-function profiles omit — **1.64×**, so 62–68 % of the
chain-length gap `CH-0010` opened is definitional. A bench cannot measure a force onset and cannot buy a
thickness, so **both numbers have to travel**.

Then the shape changes.
**Ten of the twelve axes this programme discovered are not functions of grafting density at all** — flatness
(45 attachments as 3 × 15, against 56 crossovers), the usable bias window, and the output-coupling stiffness.
They cannot narrow a window; they can only close a height.
(**CORRECTED, iteration 12** — this read *"Seven of the eleven"*, which is `C-0027`'s iteration-4 census.
`C-0051` re-ran it after three more iterations and the count is **ten of twelve**: three axes were added as
counts and plan layouts, one **left** the stack entirely, and one is a specification question.)
So a `(σ, L₀)` window is the wrong object for the Gen-1 decision, and the two axes that *do* resolve in `σ`
both survive: the peak per-load-path force is 3.9–8.9 pN — the min and max of `C-0016`'s own two-window,
two-registration table (7 nm 3.90–6.90, 10 nm 4.04–8.90 pN), a range no claim states as such —
against a 10 pN unzip allowable everywhere inside the
window — **the exceedance `C-0015` found is unreachable, because the solved layer is never as soft as the soft
end of its sweep** — and **lateral confinement is no longer a footprint cost**.
With the in-plane load path solved rather than stood in for, a tether **aligned with the helices** carries a
concentration factor of exactly **1**, so it needs 33.5 nm at §3's *desired* 10 nm stroke — a ~107 nm assembly,
which is what the *acceptable* 3 nm stroke already cost.
`C-0024` sharpens both halves: the 33.5 nm rests on a 48 pN allowable that presumes a 30 bp joint, so a
realistic 16 bp staple extension makes it **39.4 nm**, while the **same staple split across two duplexes** —
costing nothing in material, layout or stroke, and *relieving* the crossovers — makes it **27.7 nm**.
And the 54.9 pN preload is **25–186× what `T-13` actually needs**, so it is a tax rather than a price:
`L_min` is a corner of the design space, and the length delivering exactly `C-0021`'s hold-down at the 10 nm
stroke is 116.6 nm.
**The entry topology's value is that it widens the admissible length axis, not that the shortest tether is
the one to build.**
The 93–227 nm figures rested on `C-0009`'s out-of-plane factor applied to an in-plane load (`CH-0021`).
**The cost moves into the normal direction instead**: at the minimum tether length the tension *is* the
allowable, so the preload is `n_t A √(2A/S)` = **54.9 pN for four tethers, independent of the stroke** and 55 %
of §3's own 100 pN target — a `T-13` problem that did not exist before.
And the whole gain is conditional on alignment: misaligned, `L_min` is 115.9 nm, *worse* than the figure it
replaces. (`C-0020`.)

**What decides the programme is the output coupling, and `T-16` has now evaluated it.**
At 10 nm the §6 operating point is statically unstable at §3's own 2 mM buffer, so it exists only against a
lever supplying its own stiffness — but that stiffness is **33.333 pN/nm, fixed by §3's 100 pN and 3 nm
alone**, and it clears the stability floor at every height, buffer and layer model in the box.
`C-0016`'s `P2` therefore closes **non-empty at 7 and 10 nm**.
What a DNA lever cannot easily be is *compliant* enough: forty-five duplexes in tension are 4950 pN/nm,
148× too stiff, and the element that closes the task is a 10–19 nt ssDNA spacer carrying **99.6 %** of each
path's compliance.
The margin at 2 mM is 1.19–1.42×, and this is **not excluded rather than established**.
~~against a 123–214 % mean-field error, and `T-50` — bounding that error — is the binding uncertainty in the
programme.~~
**CORRECTED, iteration 32 (`C-0137`, `T-50` DONE; `CH-0167`): the 123–214 % is a statement about the LEVEL
of the correction, and a stability margin is not a level.**
All 54 of `C-0017`'s states are force-pinned — the pinned force is identical across all three buffers at every
`(model, height)`, relative spread `0.0` — so a level correction is absorbed into the bias.
The margin's own thresholds are that the true force be **1.48–2.22× smaller** than the mean-field one, or
decay **9.73 % faster** at the 7 nm held gap (`d ln μ/dh = −0.0377 nm⁻¹`), and **both are on the suppression
side**.
Every channel that can be evaluated is empty, favourable or worth **1.44 %**: a factor of **sixteen** swept in
effective wall charge moves the net margin 1.1942 → 1.1785–1.2114; finite ion size contributes **exactly zero**
to the far-field gradient, by one line of algebra on the published steric Poisson-Boltzmann equation; and the
**bulk** channel — the only one a surface correction can enter at all, by Kjellander's dressed-ion theorem —
is **empty**, this device sitting at `dκ_D = 0.109–0.487` inside the window where four independent methods
agree the decay length **is** the Debye length, with the Kirkwood crossover at 63.6 mM.
Taking `C-0005`'s own broken expansion literally, in the only sign that is defensible for two *oppositely*
charged walls, lands the margin at **1.0438** and not below one.
And `C-0005`'s own open item 4 is closed in the favourable direction: Kanduč et al. give the oppositely charged
branch an *exponentially* larger weak-coupling validity bound and Monte Carlo at `Ξ` up to **86** where
Poisson-Boltzmann and strong coupling *"nearly coincide"* — **the `Ξ = 17–24` alarm is calibrated on the
LIKE-charged problem, and this device is not that problem.**
The primitive-model Monte Carlo `C-0005` prices at 1–3 weeks was **not run** and is not superseded; what is
gone is the claim that only it could bound this.
(**CORRECTED, iteration 12** — this originally named `T-1f`. `T-1f` is done (`C-0019`) and `CH-0019` is the
reason it does not help: it bounds the **polymer** one-loop correction, which acts on the other term of
`k_eff`, at ≤ 9.4 %. The 123–214 % is the **electrostatic** expansion.)

Two further findings travel with it.
**The bias ceiling has to be quoted with the load it was evaluated at**: `C-0012`'s 0.02–0.1 V is a property of
the *unloaded* actuator, which snaps to near-contact, while the tile held at the §6 target sits at a 2–7 nm gap
and `φ ≤ 0.09`, inside every upstream validity range (`CH-0015`) —
and solved on its own load line rather than read off a grid, the *unloaded* ceiling is itself **0.085–0.595 V**
(`C-0018`).
And **static stability wants the thin layer, whose window is empty, while the window, the stroke and the
force-versus-height trade all want the thick one** — all three pull the same way and stability pulls against
all three.
That inversion, not any single number, is the Gen-1 design problem.

**§3's *desired* ~10 nm stroke remains unreachable** at every height and every grafting density — `C-0001`'s
one surviving headline, now confirmed against a third layer model and a fourth constraint set. (`C-0016`.)

**And it is now settled, with a mechanism, by a bound that contains no coupling at all.**
The stroke *is* the layer's compression — `s = L₀ − h`, so `s < L₀` identically — and §3 names no layer
taller than 10 nm, which makes a 10 nm stroke on a 10 nm layer the statement `h = 0`.
Over 66 states the best any device can do is a **kinematic ceiling of 9.790 nm** (1.02× short), a
`C-0002` **validity ceiling of 8.959 nm** (1.12× — and it is `C-0018`'s own binding bias ceiling at
10 nm), and a **dead-load stroke at 100 pN of 7.424 nm** (1.35×).
A coupling can only *reduce* the last of these, so the free stroke is the supremum over **every**
coupling — which is why this is a claim rather than a search.
Across the catalogue **0 of 14 elements clear the desired stroke and 3 of 14 clear the acceptable one**,
and the telling row is `C-0023`'s `E5`, which clears every coupling-side predicate at 10 nm and fails
only on the **reach**.
The established statement is therefore **unreachable on §3's own stack** — stronger than "with this
catalogue", weaker than "in physics" — and the only escape is a **taller layer, 16.63–26.12 nm**, which
is a specification question (`T-115`). Tile size, superstructure perforation and buffer cannot
substitute, because none of them is a layer height.
**ANSWERED, 2026-08-18: the taller layer is available at 0.5 mM and nowhere else** — NDI holds both it and the
low buffer behind one reserve — ~~**and NDI's own objection to it has never been answered here**: at 3.93 nm of
bulk screening a 17–26 nm layer is 4.3–6.6 Debye lengths, and no claim has evaluated the bias that delivers
100 pN across one (`T-192`, `T-194`).~~
**`T-192` HAS NOW RUN AND THE OBJECTION IS UPHELD, AND THE ESCAPE IS CLOSED** (`C-0110`, iteration 23).
§3's 100 pN stops arriving across a gap of **13.6989179 nm at 0.5 mM** (11.8724439 at 1 mM, 10.1299463 at
2 mM) — *below the bottom* of the 17–26 nm band, which begins **1.241×** and ends **1.898×** beyond where
the 0.5 mM reserve leaves it. So 100 pN reaches a resting 17–26 nm gap at **0 of 12** (gap, buffer) cells,
§3's *acceptable* clause is refused at **96 of 96** tall states, and device B — the 10 pN/nm placement
`T-112`'s answer creates — is admitted at **1 of 96**, in **1 of 6** layer models, which is a bracket
disagreement and not a design. **A tall layer does not trade device A for device B: it loses both.**
**What it does buy is exactly the kinematics and nothing else**: the *uncoupled* tile reaches a 10 nm stroke
at **52 of 96** tall states, so `C-0050`'s escape is real in **displacement** and empty in **force**
(`CH-0127`), a split no claim here had made. And `CH-0126` falls out of the same solve: §3's own
*"effort point ~20–25 nm above the electrode"* row cannot be met by a tall layer at all — the effort point
lands at **32–41 nm** — so that row is a **ceiling** on the layer height rather than permission for a taller
one.
**And the concession is ours.** This programme's standing rebuttal was that *"the Debye length is three
numbers here and the gap's is counterion-set"*. That is a statement about ion **content** — still true,
6.37–38.94× — and never about the **decay**, which measures 3.6–6.4 nm against a counterion length of
1.54–1.91. Worse, diluting to 0.5 mM makes **NDI's own estimate optimistic** rather than conservative:
`ℓ/λ_D` is 0.910–0.983 at 2 mM (NDI's ~4 nm is exactly right) and **0.649–0.819 at 0.5 mM**, because the far
field is reached in `κh` and not in `h`. (`C-0050`; `C-0046`, `C-0039`, `C-0040`, `C-0041`
agree from four independent directions.)

**And the mean field the window is computed in is itself broken there.**
The polymer Ginzburg parameter is 0.30–1.71 across the window and 1.30 at the design point, so the one-loop
correction exceeds the term it corrects and the expansion supplies no bound at all.
The window survives anyway, because the disjoining pressure is *conformational*: destroying the interaction
entirely costs 9.4 % of the layer stiffness, and both windows in fact **widen**, by 13.4 % at 10 nm and 1.8 %
at 7 nm. (`C-0019`.)

### Task 3 — stroke and blocking force

**Reachable, and the operating point it is reachable at is not one the device can be held at.**
100 pN of blocking force needs 0.065–0.699 V and 100 pN *at* a 3 nm stroke needs 0.082–0.368 V, all inside the
~1 V point-ion boundary with 5–12× of margin.
The actuator is **voltage-saturated above ~0.5 V** — a factor of 8 in bias buys 1.9× in force — so §3's 2 V
ceiling is nearly irrelevant to what the device can do.

But **`k_eff < 0` at the loaded operating point at 7 and 10 nm**, and the *free* operating point leaves an
upstream validity range above ~0.1 V — **one** range, `C-0002`'s `φ = 0.2`, and not the three reported here
until `C-0018` checked them: `C-0005`'s 1.46 nm correlation band and `CH-0007`'s 1 V point-ion boundary are
never reached at all.
Two consequences that a single "bias needed" figure hides: **the blocking force understates the peak output
force by up to 20×**, because `dW/dh = k_eff` exactly and the characteristic *rises* with stroke wherever the
field softens the layer; and **the two halves of this task run in opposite directions with layer height** —
blocking force 10× harder from 5 to 10 nm, stroke 10× easier. (`C-0012`.)

### Task 4 — electrostatic softening and pull-in

**Both branches answered, each for a different load line — because a ceiling belongs to a `(bias, load line)`
pair and not to a device.**

For the **coupled** device the usable bias is **0.097–0.425 V**, and the ceiling is `C-0002`'s `φ = 0.2`
crossover at 43 of 54 states.
**Pull-in binds at only 11 of 54**, all of them 10 nm in 2 mM, where it is **0.130–0.184 V against an operating
bias of 0.128–0.180 V** — a margin of **1.007–1.032**, the thinnest anywhere in the programme.
`C-0017`'s comfortable-sounding 1.19–1.42× is a *stiffness* margin, and a stiffness margin is not a bias
margin: `V(s)` is flat at a fold, so 19–42 % of stiffness buys 0.7–3.2 % of bias.
**0.5 mM removes the fold entirely** (1.29–2.36×), which is leaf `A2.2`'s low-screening condition arriving a
fourth time.

The **unloaded** tile has **no pull-in at 49 of 54 states** — so §6's own second branch, *"the osmotic
divergence removes the instability"*, is true, of the free tile and of nothing else — and its ceiling is
**0.085–0.595 V, not the 0.02–0.1 V** carried until now.
A **dead load** has no stable compressed equilibrium at any bias wherever it folds, its ceiling degenerating
to `C-0008`'s blocking bias, reproduced to 2.3e−3 by an independent construction.

**QUALIFIED, iteration 16 — and the qualification is a coverage gap rather than an error** (`CH-0083`, raised open in iteration 16 and **RESOLVED in iteration 17**, below).
Every number above stands.
What does not is the sentence *"§6 task 4 is answered for the Gen-1 device"*: `C-0018` searched the **affine**
mandate's load line and `C-0032` the **strain-softening** flexure's, and the element the programme now
recommends is a **third** law — strain-*stiffening* — whose pull-in fold has **never been searched**.
The two halves point opposite ways and neither is decisive on its own.
Favourably, `Q5`'s tangent over the traversed `[0, 3 nm]` is **30.03 pN/nm** against `C-0030`'s 22.88, so it
clears the **static** stability floors at 2 mM at **6 of 6** models where `C-0030`'s element clears none of
the 23.41–27.91 pN/nm band — which means `C-0032`'s escalation of 0.5 mM from a preference to a
**requirement does not transfer to the recommended element**.
*A held-gap stability margin is not a fold margin* (this file's own §2 lesson), so `Q5`'s fold had to be
searched rather than inferred — and **it was, in iteration 17**: `C-0084` finds **no fold at 2 mM at 0 of 6
layer models**, discharging §6 task 4 for the recommended device and resolving `CH-0083`. `T-63` ~~stays on the
binding list~~ **was ANSWERED on 2026-08-18 as a PRICE rather than a permission** — 0.5 mM is reachable at the
cost of origami stabilisation work and a reserve NDI holds for operating margin, so **2 mM stays the nominal**
and this margin is owed there. It was already quoted for **margin** — **1.3877–7.3137×** at 2 mM against **1.8706–10.9072×** at 0.5 mM,
the tops widened by `C-0092` and the minima unchanged — rather than for exclusion. And `C-0092` closes the
one-sidedness: `C-0084`'s 7.9097 nm branch end was a **force-ladder artefact**, the arm answers to
**8.1610821 nm** of a 8.16439083 nm contour, and `δ = ∫sin φ < L` **strictly on every branch**, so the
no-fold verdict now covers **0.9984** of the contour with 0.0033087 nm — under 1 % of one base-pair rise —
unexamined.

**And the arrest is osmotic after all.** `CH-0011`'s feature is real and is now four executable tests —
`|F_es|` is non-monotone, its peak lies below 3 nm, `k_es` changes sign there — but two counterfactuals at 324
states put the osmotic stopper at the larger gap **everywhere**, by 1.9–5×.
Passing the point where a force stops growing is not being stopped by it.
(`C-0018`, `CH-0017`, correcting `C-0012` and `CH-0011`.)

**And every stroke in this programme is measured from `L₀`, which is a height the tile never occupies.**
On the device the programme actually committed to — two-sided, tetherless — the delivered stroke is
**2.973–2.982 nm** at the 10 nm design point, a **0.6–0.9 %** shortfall.
The 2–13 % that `CH-0024` reported is a property of `C-0014`'s eight substrate tethers, which supply 92 % of
that stack's hold-down and which `CH-0027` removed the same day (`CH-0036`) — and those tethers are worth
**four grid steps** of the 10 nm window and three of the 7 nm one, where the tethered reading leaves it
1.230× wide, one grid step from empty.
No verdict moves; the *statement* does. (`C-0021`, `C-0023`, `C-0027`, `CH-0024`, `CH-0036`.)

**Every electrostatic force in this programme is a one-dimensional pressure multiplied by 1600 nm², and that
understates the force on the finite tile by 5–19 %** (`C-0022`, `CH-0026`) — 14.7 % at the design point,
25.8 % on a 20 nm tile, and equivalent to a sub-Debye 1.65 nm collar on every side.
It is favourable to every force clause and — at the **force-pinned** operating point the device is actually
held at — favourable to the stability clauses too: the level term is absorbed entirely into the bias and only
the collar's *gradient* survives, which lengthens the decay and takes the 10 nm / 2 mM margin from
1.19–1.42× to 1.34–1.67×.
The unfavourable direction `CH-0026` asserted holds only at fixed **bias**, i.e. for the free tile (`CH-0035`).
**And the collar's own convention exposure is now measured and points the other way from the one in print**
(`C-0132`, `T-218`, iteration 32): a rim charge is not a free convention but a **partition of a conserved
charge** — `σ_face = ρt/2` is Gauss's law on a slab, so `σ_rim = ρt/4` is *exactly* half the face density at
every aspect ratio with `t ≤ 2a`, and any rim charge must be taken from the collar's own face. The conserving
one-parameter family runs **1.222623 to 1.77269012 nm** of collar, **1.44990738×**, and it **straddles**
`C-0022`'s **1.65495953 nm** — against a published **1.65495953 → 2.91297923 nm**, **1.76015133×**, that is
one-sided **upward**. **`C-0022`'s falsifier 5 is not a member of the family at all** — it applies **1.5000**
of the tile's charge in 3-D and **1.2500** in 2-D, i.e. it solved a bigger tile — so that bracket is
**WITHDRAWN rather than narrowed**, and **fourteen** downstream validity ranges carry an exposure that is both
too large and pointed the wrong way.
~~Either way it is an order of magnitude inside the standing 123–214 % mean-field uncertainty.~~
**RESTATED, iteration 33 (`CH-0167`, `C-0137`): that standing uncertainty is an error bar on a LEVEL, and
this is a margin.** The collar's own decomposition here is `C-0137`'s — level absorbed into the bias, gradient
surviving — and the same-kind threshold on the gradient is a **9.73 %** shortening of the decay length.

### Task 6 — mean-field screening

The answer is **yes and no, and the two halves have different reasons.**
Mean field is **uncontrolled** across the whole 5–10 nm working range (the one-loop correction is 123–214 % of
leading for Mg²⁺) yet **qualitatively safe** there, because correlation attraction needs a gap under 1.46 nm
and the layer never allows it. Controlled PB begins only above 12.9 nm.
`Ξ ∝ q³`: the divalence does this, not the surface charge — Na⁺ at the same surface gives 3.0 against 24.
**AND `T-50` — the last unbounded exposure on the Gen-1 critical path — IS NOW CLOSED, FAVOURABLY**
(`C-0137`, iteration 32; [`CH-0167`](gpd/challenges/CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md)).
**The 123–214 % is an error bar on a LEVEL, and every quantity it was quoted against is a margin read at a
force-pinned point**: all **54** of `C-0017`'s states are force-pinned — the pinned force is identical across
all three buffers at every `(model, height)`, relative spread **`0.0`** — so a correction to the *level* of the
force is absorbed entirely into the bias and contributes **exactly zero** to the stiffness. The same-kind
thresholds are that the true force be **1.48–2.22× smaller** or the decay length **9.73 % shorter**, and
**every channel that can be evaluated is empty, favourable, or worth 1.44 %**: sixteenfold in effective wall
charge moves the net margin 1.1942 → 1.1785–1.2114, finite ion size contributes **exactly zero** to the
far-field gradient by one line of algebra, and the **bulk** channel is empty because this device sits at
`dκ_D = 0.109–0.487`, inside the window where four independent methods agree the decay length **is** the Debye
length. **The multi-week Monte Carlo was not run and is not superseded**; what is gone is the claim that only
it could bound this. `C-0143` then settles which input the coupling criterion is owed — see §5.

### Task 7 — poroelasticity

**Not binding, by 22× at the §3 worst case and 5.6× under a composite worst case.**
Drainage is a *footprint* problem, not a thickness problem — the thickness cancels and `τ ∝ L²` in the tile
edge — and a **denser** layer drains *faster*, so the binding direction is dilution.
The design would have to leave the poroelastic model's own domain of validity before poroelasticity could bind.
One later charge against it, and it does not bind either: `C-0055`'s 34 upward arms add **9.1 %** of the
total drag, taking the nominal corner 91.2 → **82.9 kHz** and the worst §4(d) margin **22.81× → 20.73×**
(`C-0061`). The arms are on the tile's dry `+z` face, so `C-0004`'s squeeze film is untouched by
construction.

### Task 8 — positional variance

**PASS at the operating point**: 0.87–0.96 nm broadband, 0.069–0.110 nm in band below 1 kHz, against 3.0 nm.
Two qualifications travel with it. The tile's **worst point** (a corner — the centre is the fixed point of both
tilts and therefore the *quietest* place on it) exceeds 3.0 nm in every state softer than the working point.
And the **lateral coordinate is not part of the PASS at all.**

Leaf `A1.2` asks for a *simulated* σ_RMS with a **95 % CI**, and that half is **not discharged**.
The reason is not cost: oxDNA models the origami and **not the polymer layer that sets the answer**, so run as
specified it returns a confidence interval on a different quantity.
A CI on an exact analytic result is a category error, and the model bracket is not one.

---

**And the unbiased state now has an answer of its own.**
`C-0021` computes it by exact Boltzmann quadrature rather than equipartition — the zero-bias potential is
harmonic below the rest height and **linear** above it — giving **0.360–0.501 nm broadband and
0.019–0.041 nm in band** against the same 3.0 nm predicate, with the tile spending up to **53 % of its time
above `L₀`**, where the layer holds it with nothing at all.
The harmonic reading understates that amplitude by up to **2.6×**.
And the requirement there is a **force** or a **stiffness** depending on the **topology of the coupling**,
the two being exactly one power of the bound apart (`F_req = k_req·σ`).
With the *one-sided* coupling the programme had committed to it is a force, `k_BT/3 nm = 1.381 pN`, and
nothing in the §3 stack supplies it: the tetherless device is a 1.4–5.4 `k_BT` trap and its RMS is
**2.56–12.98 nm**, failing leaf `A1.1` at 15 of 18 states.
With a **two-sided** coupling it is a stiffness, `k_BT/σ² = 0.4602 pN/nm`, which §3's own mandated
33.333 pN/nm exceeds **72.4× unpreloaded** — and the same device becomes **959–7582 `k_BT`, 18/18 confining,
0.217–0.352 nm broadband and 0.012–0.035 nm in band**.
Three DNA elements deliver it, and the eight substrate tethers `C-0021` needed leave the design.
(`C-0021`, `C-0023`, `CH-0027`.)

## 3. The open questions of §4

| | Question | Answer |
|---|---|---|
| (a) | Grafting density and regime | **Four brush criteria have failed here**, each a convention asked to do a measurement's work: `Σ ≥ 5` failed thermodynamically *and* geometrically; `L₀/R₀ ≥ 1`, adopted to replace it, turned out **exactly vacuous** — it admits all 183 points of the sweep. **Coil overlap `Σ = πR₀²σ ≥ 1` is the only criterion that bounds anything**, and it owns the lower edge of every surviving window. Window: see Task 2. |
| (b) | Layer height | **Empty at 5 nm only** — the earlier "empty at 7 nm too" was withdrawn when a solved density profile replaced two trial functions. There is a genuine **trade**, not an ordering: the window, the stroke and the force all want the thick layer, and static stability wants the thin one. |
| (c) | Porosity and ion partitioning | **The sign in the question is backwards.** The layer *excludes* 23–48 % of the salt, so it **lengthens** the local Debye length by 1.14–1.39× and **protects** the field rather than screening it away. It also *amplifies* `F_es` by 1.15–1.60×. The dielectric-decrement mechanism named in §4(c) is a 3.9 % effect — the layer is 97 % water. **The bound is one-sided** (exclusion only); cation coordination by PEG's ether oxygens could flip it, and no binding constant exists in accessible literature. |
| (d) | Poroelasticity | **Not binding**, with the boundary named. See Task 7. |
| (e) | Screening | See Task 6. The force's own decay length is **1.8–2.8 nm** at the working gap — not the 4 nm bulk Debye length, and it is bias-dependent. Leaf `A2.2`'s low-screening operating point is **vindicated twice**: at 10 mM the 100 pN target is unreachable at 7 and 10 nm, at 0.5 mM it is reached even at 10 nm. |
| (f) | Structural survival | The **35–60 pN band is not a per-load-path allowable** — it is a whole-cross-section disassembly force at a stated loading rate, and a DNA rupture force without a loading rate is not a material constant. Per path use duplex shear (~48–65 pN) or unzip (10–15 pN), 65 pN a hard ceiling. **Three load paths clear 35 pN, eleven clear 10 pN — and 45 (as 3 × 15, not 64 as 8 × 8) are the flatness count, against 56 crossovers** (read *"are needed for flatness"* until iteration 12; `CH-0034` is the correction — 45 is where further attachments **stop buying** flatness, not where the tile becomes flat, and row (g) below carries what does). A rigid anchor is carried by its **two nearest crossovers and essentially nothing else**, so an equal-share figure understates the peak by 2.3–7.6× **out of plane** — but inside the actual design window the peak is 3.9–8.9 pN against a 10 pN unzip allowable, so **the exceedance is unreachable there**. **The exact-zero load path of a one-row-per-duplex grid survives a realistic load with 20× to spare** (`C-0026`): under the edge profile `T-3b` actually solved it restores 0.150 pN (0.332 worst case) against a 2.222 pN static share, and what breaks it is attachment-stiffness scatter at 0.883 pN per unit relative amplitude — even a coupling with every second path at 1 % of nominal stays 12× clear of unzip. So the crossover path never becomes binding for a distributed coupling, out of plane as well as in. **In plane the factor is different in kind**: a lateral tether collects nothing from the layer, so the peak is a *fraction* of its own tension — `η = 1.0000` aligned with the helices, up to 2.33 misaligned, and the staple layout is worth **exactly nothing** on the binding path (`C-0020`). ~~**And the desired stroke puts a floor under the path count**: at 10 nm the mandated coupling delivers 333 pN, so the 10 pN unzip allowable alone needs **≥ 34 load paths**, a fourth and tightest route to 45 (`C-0025`).~~ **WITHDRAWN by `CH-0059`/`C-0049`.** The 34 is not a material property but a property of the **placement convention**: it is `33.333 × 10/10`, i.e. §3's *desired* stroke read on a coupling placed for its *acceptable* one, and the same allowable gives **10** under the other reading. The per-path ceiling it derives from is `n·allowable/s`, a bound on a **force**, so it *tightens* as `1/s` — 150 → 45 pN/nm at 45 paths, and 50 → **15** at the 15 paths `C-0041` shows the tile actually carries. **And the per-path allowable is itself a function of the bonded length** (`C-0024`, `CH-0029`): 48 pN is Strunz's **30 bp** number, while a realistic 16 bp staple extension gives 34.8 pN and an 8 bp one 18.8. **What a tether bonds to is a sequence-design choice, and it is worth more than the sheet is** — the sheet's answer is pure arithmetic (a bond spanning `m` duplexes enters at exactly `1/m`, floor `1/D`, ceiling 720 pN), while the joint moves ×2.5 over the realistic 8–20 bp range, and **splitting a bond across two duplexes wins above a 14.3 bp total bonded length and loses below it**. **And the 3 × 15 grid is a REGISTRATION, not a count** (`CH-0079`, open, iteration 11): once `C-0055`'s upward arms stand on the tile, **26 of those 45 tie stations do not exist**, and **no rigid translation** of a two- or three-column grid clears every row — swept through a full column pitch at **400 001** offsets, both have **zero** clearing windows. The escape is nearly free and it is *along* the helices, at **+1.7 %** of dishing (0.2182 → 0.2219), because *"one attachment row per duplex"* is an across-helix statement that says nothing about position along a row. Four downstream claims read 3 × 15 as a buildable station set; the premise is latent behind the flexure-and-tie branch `CH-0081`/`C-0069` removed from the output role, and it re-binds exactly when that branch does. |
| (g) | Does the tile stay flat? | **YES, ON THE `10 × 6` FOUR-LAYER TILE — and after iteration 33–34 the cross-section is part of the answer rather than a preference.** The short answer, current as of iteration 35. **(1) The tile is four honeycomb layers, not one**: §3's own parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"* and every structural claim before iteration 23 modelled a **2 nm** sheet; one circular M13 pays for exactly four layers (`C-0109`). **(2) The cross-section every four-layer number was solved on was NOT a honeycomb, and correcting it REVERSES the footprint ordering** (`C-0141`, `CH-0174`): the in-plane row pitch is `3d/2` and the layer pitch `d√3/2`, and only their product is the cell, so every `edgeY` was exactly **1.5×** too small — `15 × 4` is **1.40084263** of §3's 40.35 nm and `10 × 6` is **0.929467162** of it. **(3) Uncoupled both are flat, and only one STAYS flat**: `15 × 4` dishes **0.0978155002** and `10 × 6` **0.0240648102** under `C-0022`'s solved collar, but `C-0116`'s interlayer-coupling threshold moves to **0.276970522** — **inside** the measured 0.26–0.33 band — so at the band's adverse end `15 × 4` dishes **0.101759944** and **fails** `T-5b` while `10 × 6` dishes **0.0255589305** and does not. **(4) A COUPLED tile is still flat under the measured folding statistics, on four of sixteen graded cells and all four on `10 × 6`** (`C-0142`, `CH-0176`): best **0.0680677948** at the sparsest coupling tested, `15 × 4` **0 of 8 at both ends of the band**, the cross-section worth **2.13543134×**. **(5) The stations exist and the placement survives them**: the honeycomb top face supplies **90** stations on `15 × 4` and **60** on `10 × 6`, at **exactly one** rooting azimuth per helix of **30°** and with **no perpendicular root anywhere** — `C-0122`'s census restored and `CH-0151`'s upward correction withdrawn (`C-0141`, `CH-0175`) — and an oblique root costs **nothing** for a flexible tie, as a symmetry, and **2.67233333×** for a crossover-hinged body (`C-0128`, corrected from 6.01719478×). Snapped onto that 21 bp ladder all sixteen placements are realisable, worst snap **3.332 nm** inside a 3.57 nm ceiling, and **three are flat — all three equal springs** ([`CH-0177`](gpd/challenges/CH-0177-the-path-count-axis-is-not-monotone.md): the rim grading loses at every count once the snap breaks the rim/interior partition it was written against), the first coupled flatness result here standing on stations a derived lattice supplies. **What is still owed**: a per-site staple-incorporation measurement on a coupling-bearing tile, which is a bench measurement and not a solve; ~~the **row length**, because a honeycomb x-raster carries **both** turn senses and has no uniform width at all (`C-0140`, `CH-0172`) — design (i)'s remedy is **112 / 108 bp**, an axial extent of **39.44 nm**, `−1.40 %` of §3's nominal, and `10 × 6` carries the same alternation with its own two lengths not yet derived~~ **RESTATED, iteration 36** (`C-0146`, `C-0147`): **the row length is derived and it is not what is owed — a CONVENTION is.** Every x-raster row of the block spans the LARGER of its two lengths exactly, at all ten rows and at every one of `C-0140`'s five candidate pairs, and the 116 bp = **39.44 nm** extent exceeds it by exactly the inter-row **STAGGER**; ~~`10 × 6` carries **the same 112 / 108 pair**, with the identical 4 / 8 bp faces and the identical extent~~ **`10 × 6` carries the same pair as `15 × 4`, and that pair MOVED in iteration 37, to `102 / 109` — see the `CH-0187` sentence below**. What is owed is **which of the two §3 names**, which is a specification question (decision **8**, `T-242`) ~~worth **six flat cells of eight against three** through a crossover column admitted by a **0.07 nm** slack (`CH-0185`)~~ **and, since iteration 38, worth NO flatness cell at all: both graded column counts belonged to a raster that does not close (`CH-0195`), and the drawable one reads the same 10 columns at every guard convention** — and the recommended cell is flat at every reading. **And the 4 bp ragged face that follows costs this row EXACTLY NOTHING** (`C-0147`): the ragged faces are the tile's **rim**, not its gap-facing surface, so the coefficient on the flatness field is exactly zero and the residual rim channel is bounded at **5.54399427e−05** and **1.68371917e−05** of the stroke against **0.0274976866** of headroom, a margin of **496×**. What it costs is **plan budget** — 1.36 nm against an outboard ceiling saturating at **2.380 nm**, **0.571** of it at 90 paths; **at the drawable raster the relief is 7 bp = 2.38 nm and the SATURATED ceiling is not reachable — the determined phase supplies 55 of 60 stations, and at 55 paths the ceiling is 3.06 nm, a margin of exactly 2 base-pair rises** (`C-0151`, read at its own census by [`CH-0202`](gpd/challenges/CH-0202-the-plan-ceiling-escape-is-quoted-at-fifty-paths.md); the claim quotes the 50-path row, 4.604 nm), and `T-258` owns the re-run of the ragged-face cost at 7 bp. ~~**And the pair itself is CONDITIONAL** (`CH-0187`): among all pairs that fit M13 the width optimum is **101 / 109 bp at `−0.55 %`**, better on three axes of four, and 112 / 108 wins only the plan budget and only at a saturated path count.~~ **`CH-0187` is ANSWERED, iteration 37, and the pair is `102 / 109`** (`C-0151`, `C-0148`, `CH-0194`): `112 / 108` needs **10** of the block's **59** raster crossovers **forced** on caDNAno's `±5 bp` scaffold rule, `101 / 109` does not close either, and the minimum stagger any DRAWABLE two-length raster can carry is **7 bp = 2.38 nm** where the filter that produced both admitted at most 4 — so the filter and the closing family are **exactly disjoint**. `102 / 109` ties at the **same 116 bp = 39.44 nm** extent, so **this row's width number does not move**; its row span is `109 bp`, its interface window **102 bp**, its columns **10** and its stations **55 of 60** at the determined phase 16. **The whole cost of closure is one crossover column** — 2 flat cells of eight against 3, a uniform **1.0567397–1.09611647×** of the dishing — and **the recommended cell survives at both ends of the measured band, 0.0773373597 at `f` = 0.30 and 0.0821458169 at `f` = 0.26**. **Closure is a reason to PREFER `102 / 109` and NOT a proof that `112 / 108` is unbuildable**: the rule is caDNAno's default, and `C-0152` prices the elastic half of forcing at **0.350894669 `k_BT`** per crossover, **sub-thermal**, with all ten at **0.438634952** of one crossover column of a sheet that folds. The **kinetic** half is unpriced and `C-0152` records a negative existence result over **68** queries in **7** families. Also owed: the **scaffold**, which the caDNAno paper contradicts itself about at exactly design (i) (`CH-0180`) — and there the **turn allowance is a choice**, **28 nt** against a **6 nt** reach bound, with M13 affording exactly **8** so a uniform 112 bp row fits by two nucleotides and only strained at **6.54349121–12.112167 pN** (`C-0147`). **The full derivation history — the negative this row carried for eight iterations, the four axes on which it was closed, and the two reversals since — is preserved verbatim below the table.** **AND THE WHOLE PHASE AXIS OF THIS ROW IS OVER ONE PARITY FAMILY, WHICH ONLY A DESIGN THIS REPOSITORY DID NOT DRAW COULD SHOW** ([`C-0161`](gpd/claims/C-0161-mechanics-on-an-imported-design.md) §4(b), iteration 39). `CrossoverLayout.centred` and `CrossoverLayout.phased` — the two generators every phase sweep above runs through — **alternate the column parity by construction**, and a **SEAM doubles a column pitch**, so two consecutive columns then serve the *same* interface parity. Graded on `scadnano.origami_rectangle`, the reference implementation's own Rothemund rectangle, the parity sequence is **`[0, 1, 0, 1, 1, 0]`** — **which no phase sweep in this corpus can generate**. **So every phase-swept placement, count and flatness result above is a statement about the ALTERNATING family, and a seamed sheet is outside it.** The restriction is on the **swept family**, not on the data structure: `CrossoverLayout` carries the parities explicitly and represents the seamed sequence **without complaint**, and the rectangle is representable *exactly* — **90 lattice sites against 90 drawn, 0 absent**. Nothing here re-runs those studies, and this row does **not** claim a number moves; what it claims is that the scope was never stated, because no claim could see it without a foreign design. **AND THE ONE PLACE IT COULD BITE THIS ROW IS THE RECOMMENDED BLOCK ITSELF** ([`CH-0212`](gpd/challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md), open, raised by this pass): `C-0119` §4 derives a **forced seam** on the four-layer honeycomb tile as a tree parity and calls the 60-helix case *"a theorem, not an enumeration"*, while `C-0160`'s committed `.sc` for the same block carries **one scaffold strand with 60 domains on 60 helices** — one per helix, which is `C-0161`'s **own** seam discriminator returning *no seam* — and `HoneycombCoupledStudy` and `HoneycombPlacementStudy` **both** grade this row's cells through `CrossoverLayout.centred`. **The favourable resolution is the likelier one**: a seam needs a path graph **and** a fully folded circular scaffold, and this block leaves **919** of M13's 7 249 nt, so it closes through its own remainder. **No number moves on either resolution** — what moves is which of two standing statements about the recommended tile a reader may rely on, and it is settled by two free readings rather than a solve. **A SECOND AMBIGUITY ARRIVES WITH THE SAME DESIGN, AND IT IS A FACTOR OF TWO IN A CROSSOVER CENSUS** ([`CH-0209`](gpd/challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md), open): the field's own generator draws **every** Rothemund crossover as **two** strand crossings at **adjacent** offsets (`o` and `o+1`) where this corpus draws **one**, so the same imported file is **90 lattice sites or 45**, and reading it as junctions softens the tile by **`1.087×`** in peak dishing over stroke (**0.258057772** as drawn against **0.28058418** as junctions, 16 duplexes, `C-0001`'s secant foundation, `T-10`'s own edge taper). `T-267` therefore **refuses to default the reading** and names it in the record, which is a mitigation and not a resolution. **None of this touches the numbers above**, which are this corpus's own designs drawn with one crossing per junction — what it touches is any verdict handed to somebody else's design, and `C-0157`'s oxDNA `k_θ` bracket, which relaxed **one** of the two motifs. |

---


---

### Row (g)'s derivation history, preserved verbatim (`T-211`, iteration 29)

**SECOND PRESERVATION, iteration 35.** Row (g) was rewritten a second time, by `T-233`/`C-0145`, because
[`C-0141`](gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md) and
[`C-0142`](gpd/claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) moved **every four-layer number
in it** — the footprint ordering between the two candidate cross-sections **reverses**, the interlayer-coupling
threshold moves *inside* the band it was said to clear, and the coupled evidence halves.
**Every number below is superseded; read the row above.**
The iteration-28 cell, with the iteration-33 revision banner it carried, is kept here unedited:

> **UNDER REVISION, iteration 33 (`C-0141`, `CH-0174`, queued as `T-233`): the four-layer cross-section every number in this row is written on is NOT a honeycomb — a honeycomb spends `3√3/4·d²` of plan per helix and the model spends `d²`, so every four-layer `edgeY` is 1.5× too small, the footprint ordering between `15 × 4` and `10 × 6` REVERSES, and `C-0116`'s interlayer threshold moves to 0.276970522 — INSIDE the measured 0.26–0.33 band, where `15 × 4` fails `T-5b` and `10 × 6` does not. Read the rest of this row as conditional on the cross-section, and see `T-233`.** **YES, ON THE TILE §3 SPECIFIES — and the question was reopened and answered twice in four iterations.** The short answer, current as of iteration 28. **(1) The tile is four honeycomb layers, not one**: §3's own parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"* and every structural claim before iteration 23 modelled a **2 nm** sheet; one circular M13 pays for exactly four layers (`C-0109`). **(2) On that tile the free tile is flat with NO coupling at all** — **0.0577199433** of the stroke under `C-0022`'s solved collar, inside `T-5b`'s 0.10, against the single layer's **0.307902368** — and the interlayer coupling it depends on clears its threshold **monotonically by 3.29690337×** (`C-0116`). **(3) A COUPLED tile is flat under the measured folding statistics**: 9 of 16 cells at the 90th percentile, **all eight** on the `10 × 6` cross-section, best **0.0278431488** at the *sparsest* coupling tested — and **the cross-section is worth 3.17109774× while the distribution has no consistent sign**, so the design order runs tile first, coupling second (`C-0118`). **(4) The stations exist and the placement survives them**: the honeycomb top face supplies 90 stations on `15 × 4` and 60 on `10 × 6` — corrected *upward* to 132 and 90 by `CH-0151`, an oblique helix having two free azimuths and not one — and snapping every station to the lattice's real 21 bp ladder keeps every flat cell flat (`C-0122`); an oblique root costs **nothing** for a flexible tie, as a symmetry, and 6.01719478× for a crossover-hinged body (`C-0128`). **(5) And the source's own folding measurements recommend a better cross-section still**: `10 × 6` is **6.6× flatter** than `15 × 4` and has **no coupling-fraction threshold at all**, flat even at zero interlayer coupling, so it *removes* the last unmeasured dependency rather than clearing it — at **two-thirds of the footprint**, a specification trade which is decision 7 with NDI (`C-0119`, `C-0120`). **What is still owed**: a per-site staple-incorporation measurement on a coupling-bearing tile, which is a bench measurement and not a solve; and the interlayer coupling of a 15-wide × 4-deep **slab** against the **rods** every published calibration uses — though `C-0120`'s cross-section removes that dependency entirely. **The full derivation history — the negative this row carried for eight iterations, and the four axes on which it was closed — is preserved verbatim below the table.**

---

`C-0121` recorded that row (g) *"is now the passage most in need of a rewrite rather than another append"*.
It had reached **23 414 characters in a single table cell** with eleven accumulated revision markers, and a
reader wanting the current answer had to reconstruct it from a chronological accretion in which the standing
verdict reversed **twice**. The row above now states the answer; **the cell as it stood is kept here
unedited**, because the reversals are the most instructive thing in this deliverable and deleting them would
leave the answer looking inevitable.
**And every four-layer number in the block below is superseded too, iteration 35 (`C-0141`, `C-0142`,
`C-0140`)** — the cross-section, the footprint ordering, the interlayer threshold, the coupled cell count, the
station azimuth and the 38.08 nm width have all moved. It is retained as a record of what was believed, not as
an answer.

> **No, and the irreducible part is now a number.** Rigid *exactly* under a uniform load at any rigidity, and dishing 26–369 % of the stroke under every departure. The part that **cannot be designed away** is the electrostatic edge effect, now solved in 2-D (`C-0022`): the rim *gains* load rather than losing it, and the dishing it forces is **32 % of the stroke**, and **45 attachments is the count at which further attachments stop buying flatness, not the count at which the tile becomes flat** — under the solved load the dishing saturates at 0.149 of the stroke between 45 and 225 attachments (`CH-0034`) (21–44 % over the foundation sweep, 30–32 % on the discrete lattice). So the lever and the area-averaging sensor differ by **32 % of the stroke whatever the coupling does** — `C-0012`'s 11 %–369 % band was a statement about the number of attachments, which is a design choice; this is not. §4(g)'s own criterion for abandoning the rigid-plate picture is met, and met by a load nobody chose. **REVISED, iteration 9 (`C-0058`): it CAN be made flat, and the axis is the coupling's *distribution* rather than its size.** `C-0017`'s mandate is an equality on a **sum**, and every claim above shares it equally between the paths. Freeing that — same 33.3333 pN/nm, same 45 attachments, same solved load — gives **0.0753** of the stroke under a one-parameter rule (*the 34 stations within 6.7 nm of an edge carry 5× the other 11*) and **0.0544** under a full optimisation, both inside `T-5b`'s 10 %, at 2.762 pN per path against a 10 pN allowable. **So 0.149 is where the equal-spring family saturates, not a floor** (`CH-0071`). Three qualifications travel with it: it is owed at **one operating state** (the same design dishes 0.187 at the 2 nm gap, where the uniform coupling gives 0.071, and no distribution found is flat at all five); it needs **three attachment columns**, which `C-0041`'s packing forbids (at the buildable 1 × 15 the axis buys 13 % and the coupling is still 1.96× worse than none); and the 5:1 per-path ratio **can** be built but the array **cannot** be placed. **`C-0060` (`T-122`, iteration 10)**: all seven settings of the five catalogue elements reach both levels — one base pair is 1.0–19.1 % of a level's own stiffness against a flat ratio window measured at `3.5 ≤ R ≤ 20`, i.e. 25× finer than the requirement, where `C-0023`'s *preload* quantum was 8.3× coarser than its own, because a preload is a length and a stiffness is a **power** of a length — and all fourteen built designs are flat (0.0715–0.0815). The mandate survives only because it is a **sum**: rounding the two levels independently misses `C-0017`'s equality by up to 5.44 %, recovered to `1.3e−4` by moving individual paths one base pair, at the price of 3–4 distinct staple lengths. The tolerance is a **threshold**, 34.6 % relative scatter, 2.04× `C-0026`'s break-even — read *"(`T-45` is answered from published measurement — `C-0072`/`CH-0084` — and the answer is 43.6 % relative scatter, past this threshold; **`C-0087` then settles the flatness half negatively**: under the measured per-staple map every flat design here fails `T-5b` in 89.6–100 % of realisations, and **one** absent path already takes `C-0063`'s 0.0706 to 0.5010, so the tolerance question is second order behind the count)"* until iteration 16, and **`CH-0084` is the correction**: `T-45` has been answered from published measurement (Strauss et al. 2018, DNA-PAINT at single-staple resolution over all 168 staples of a Rothemund rectangle, **48–95 %, mean 84 %**) and the answer sits on the **wrong side** of the threshold. A missing staple **removes** a load path rather than perturbing it, so the population is Bernoulli and `σ_rel = √(f/(1−f))` — **43.6 %** at the mean, **1.26×** the 34.6 % and **2.57×** `C-0026`'s break-even, and **104.1 %** at the edge sites. **Two of the three statements that follow are of different strengths and the difference matters.** The **16 % mandate shortfall** is unqualified and follows from the mean alone: incorporation at 84 % takes `C-0017`'s realised total from 33.3333 to **28.00 pN/nm**, 2.9× the worst rounding error the two-level design was priced against. The **flatness fail is indicative, not established** — a Bernoulli dropout and `C-0060`'s alternating scatter have the same relative standard deviation and *different spatial structure*, and centre sites (95 %, 22.9 %) are inside the threshold — so it is owed a re-run of `C-0060`'s own pipeline under a position-dependent dropout. What is not indicative is the *direction*: incorporation is **worst at the edges**, `C-0058` puts **34 of its 45 stations on the rim** and gives them the stiff level, so the dropout is correlated on exactly the axis `CH-0073` shows a non-uniform coupling tolerates least (**31.6 %** across the columns against 69.8 % across the helices). **What fails is the placement**: `k ∝ span^(−3)` makes the soft level 1.71× the longer member, so `C-0030`'s interior span is 52.36 nm on a 40 nm tile and six of seven elements place 0–30 of the 45 stations — `C-0041`'s obstruction unchanged and made worse (`T-127`). One free improvement: the best one-parameter ratio at the same collar is **7, not 5** (0.0653 against 0.0753). **REVISED AGAIN, iteration 11, and this is where the row now stands (`C-0063`, `T-125`, resolving `CH-0074` — which had charged that the flat distribution lives on stations no placement supplies, and was resolved from the other side than either party expected): the tile is made flat by the PLACEMENT, with EQUAL springs, and the distribution is not needed at all.** Sweeping the row phases of `C-0055`'s own 34 upward roots — **1 144 858** placements evaluated, the winner found by an *exhaustive* enumeration of the **361 584** centro-symmetric ones — reaches **0.0706** of the stroke under the same solved load, against **0.3079** for no coupling at all (**4.36×** better), **0.4156** for `C-0055`'s own greedy placement, and `C-0058`'s **0.0753** on a 3 × 15 grid `C-0041`'s packing does not supply. Two cheap bounds did it: `3a + 2(15 − a) = 34` forces **exactly four rows of three**, and `2c ≡ 0 (mod 10.88 nm)` admits a symmetric placement at **exactly 2 of the 32 phases, 8 and 24** — the winner is at 24. **On those stations `C-0058`'s rim rule REVERSES SIGN** (0.0706 uniform against 0.1410 at ×2 and 0.2214 at ×5) and the 34-parameter optimum wants a peak ratio of only **1.30**. Cost 2.298 pN in the worst path (3.4× clear of unzip), 1.246 pN in the worst crossover — **8.3×** the 3 × 15 grid's, still 8× clear. **And a flatness verdict must be quoted with the operating state it is read at** (`C-0064`, `CH-0077`): no distribution is flat at all five of `C-0022`'s solved states — a real minimax reaches **0.1254**, still 1.25× outside (~~0.1247~~ **CORRECTED, iteration 33, `CH-0171`**: 0.1247 is `C-0058`'s minimax at the 2 mM / 10 nm design point, not `C-0064`'s worst of five, and it is the one number of `T-113` that did not reproduce run to run — 0.1247 / 0.1385 / 0.1403 over three emissions, against `C-0064`'s 0.1254 converged to `1e−4`) — because the 2 nm state's dishing field is **anti-parallel** to every other's (cosine −0.943 to −1.000), it being the only one of 21 whose finite tile carries *less* force than a 1-D pressure. **But the five states are four devices**, and over the range each device actually traverses the minimax is **0.0373 (2 mM) / 0.0435 (0.5 mM) / 0.0620 (the 5 nm device) / 0.0504 (10 mM) — all four inside `T-5b`'s 0.10.** **AND `C-0063`'s placement IS flat over a range, with equal springs** (`C-0068`, `T-129`, iteration 13): **0.0789** over the whole range `C-0018`'s placed 2 mM device traverses, **0.0853** at 0.5 mM and **0.0896** at 10 mM, all three inside `T-5b`'s 0.10 against 0.0706 at the single state — so what the range costs is the *margin*, 1.42× becoming 1.12×, spent at the compressed end. **The exception is the 5 nm device**, whose range owns `C-0022`'s 2 nm state: equal springs dish **0.2000** there and are worse than no coupling at all at both of its states, which a distribution recovers (0.0291 / 0.0365 / 0.0565 / 0.0382) at peak ratios of only 1.72–2.32 — a scope correction, not an infeasibility (`CH-0080`). **And the crossover phase is selected by the LAYER**: re-enumerating the centro-symmetric family exhaustively under a range objective finds 0 of 198 288 placements at phase 24 better than `C-0063`'s own, while under the 5 nm device's range nothing at phase 24 clears at all and a **phase-8** placement does. **AND THE PATH COUNT SELECTS THE PHASE AS WELL, AND IT COSTS THE EQUAL SPRINGS** (`C-0074`, `T-136`, iteration 15). Reducing `C-0063`'s array from 34 arms to 30 — the move `C-0072` recommends to recover the plan margin — makes the two-per-row constraint an **identity** (`2 × 15 = 30`), so the whole symmetric family is enumerable: **34 992** candidates at each of the two phases the congruence admits, reaching **0.166653** at phase 24 and **0.172575** at phase 8, and a 12-start descent over the **non-symmetric** family at **every one of the 32 phases** reaching **0.1670**. All are outside `T-5b`'s 0.10, and **all improve on `C-0072`'s plan-rule 0.2603**, which is an upper bound rather than a search. **The negative belongs to the equal springs and not to the station set**: the least-squares floor over every phase-24 upward root is **0.00071**, 140× below the convention, and a **distribution** at `C-0017`'s unchanged total puts six of eight priced placements inside 0.10. The design that answers flatness, margin and the per-path ceiling at once is at **phase 8**, not `C-0063`'s 24 — **0.06822** over the whole traversed range at a peak stiffness ratio of only **2.057** and **6.857 pN** per path, carrying the largest plan margin the lattice affords, **1.76451 nm**. At phase 24 the maximum-ceiling placements reach only **0.11239** and **0.13188** even under a distribution. So the flatness answer is now a function of *three* things a designer chooses — the placement, the phase and the path count — and only the first was ever thought to be one. **AND UNDER THE ONLY MEASURED FOLDING STATISTICS IT IS NOT FLAT AT ALL** (`C-0087`, `T-148`, iteration 17; `C-0089`, `T-155`, iteration 18). Strauss et al. measure staple incorporation at single-staple resolution — **48–95 %, mean 84 %** — and a missing staple **removes** a load path rather than perturbing it. **A flat design is a cancellation and a cancellation has no tolerance to a missing term**: removing exactly ONE of `C-0063`'s 34 paths takes 0.0706 to **0.5010**, and every standing design fails `T-5b` in **89.6–100 %** of realisations. **The recovery route was then searched and closed.** `C-0089` grades **22** `placement x distribution` cells and the best 90th-percentile dishing anywhere is **0.2845** of the stroke — 2.85x the convention, at 100 % exceedance. The direction is right and the slope is not: *denser* is monotone (0.8522 -> 0.5327 over 15 -> 90 paths) but six times the paths buys **1.60x**, and optimising the **percentile** instead of the zero-defect value is worth a further 1.30–1.61x at the price of 2.9–4.1x of zero-defect flatness. **What refuses it is a count, in a division that needs no solve**: a dropout IS an increase in the attachment pitch, so the surviving pitch stays inside one Winkler bending length (12.83 nm) only at **13 columns — 195 paths** — against the **34** the plan ceiling admits, **5.7x** short. **The negative belongs to FIXED distributions**: an oracle that knew which staples were missing could be flat by **255x**, so nothing here is refused by geometry — only by the fact that a coupling is specified before it is folded. **AND THE REMAINING AXES WERE THEN SPENT — TOPOLOGY AND PHASE — AND THE ROW IS CLOSED** (`C-0093`, `T-162`, iteration 19; `C-0098`, `T-165`, iteration 20; `C-0102`, `T-171`, iteration 21). A coupling that is **not an array** narrows the negative without removing it: a **stiff shared body** moves `C-0017`'s mandate into the body's **ground**, freeing each tie from 0.98 to **3.33 pN/nm**, and is **2.05× flatter** at zero defects on identical stations (**0.0344013403** against 0.0706145537) and **0.24028028** at the 90th percentile under the dropout — the lowest reached here, still **2.40×** the convention and **252 ties against 53** (`C-0093`; `CH-0113` corrects the 53 to a **phase's** inventory rather than the lattice's, and the repair *sharpens* the verdict because the ten richest phases are disjoint from the eight-column and centro-symmetric ones). Searching that body's **placement and distribution** on the sites the lattice actually supplies then runs the wrong way: 25 graded cells, best **0.375506727** at 100 % exceedance, **1.56× WORSE** than 0.24028028 — *because that figure was never buildable*, sitting on an abstract 90-station grid. And the **distribution axis shuts as `1/t`**: a stiff body's kinematic limit is independent of how the ties are distributed, so `C-0089`'s 1.30–1.61× was a property of a **divided** mandate and the same division that makes this topology flatter is what removes the axis (`C-0098`). The **phase** is spent too and is over-subscribed — three demands on one integer, no phase serving all three, **phase 8** recommended at **0.0658484805** of the free stroke against the richest phase's 0.125068659 (`C-0102`). **And at the buildable 38.08 nm width the same placement is 12.0 % FLATTER, 0.0621469105** (`C-0090`), which is where the row's zero-defect number now stands; its 14 row-end crossovers move it only to **0.0651753854** at zero dihedral stiffness (`C-0099`), while a **prestrain** on them reaches `T-5b`'s 0.10 at **15.45°** and the lattice's own register ladder gets there (`C-0104`). So the honest answer to *(g)* is now: **the tile can be made flat on paper — 0.0621469105 of the stroke as designed — and cannot be made flat at the state of the art in DNA-origami folding, and the question is closed on every coupling axis this programme can reach: count, distribution, placement, topology and phase.** What decides it is a **fabrication yield**, not a design: a per-site incorporation measurement on a coupling-bearing tile, which is a bench measurement and not a solve. **AND THE CLOSURE IS A STATEMENT ABOUT A 2 nm TILE, WHICH IS NOT THE TILE §3 SPECIFIES** (`C-0109`, `T-191`, iteration 23). Everything above is derived on a **single-layer, 2 nm** sheet. §3's own parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"* — a contradiction `electrostatics/DnaOrigamiTile.kt` and `C-0021` both carry **two readings** of, and every structural claim here took the thin one; `C-0086` measures the sheet at **1 680** of M13's **7 249** nt; and NDI's answer to decision 5 resolves it toward the thick reading from the direction nobody was watching — *"to use exess scaffold, just make the tile thicker."* **On that tile the negative does not survive, and not because a coupling works: because the tile does not need one.** At the interlayer coupling four measured origami bundles support, a **four-layer honeycomb** tile at the buildable 38.08 nm dishes **0.0577199433** of the stroke under the same solved collar **with no attachment coupling at all** — inside `T-5b`'s 0.10, against the single layer's **0.307902368**. The cheap bound predicted it and needs only a fourth root: `D_∥` goes 85.502 → **4 547.17603** pN·nm and `D_⊥` 3.34504758 → **240.931249**, so `C-0058`'s reach goes 12.8290845 → **34.6447329** nm along the helices and 5.70561353 → **16.6216854** across, against a 38 nm tile; and `C-0089`'s run-robustness demand falls from **13 columns / 195 paths to 5 / 75**. **One circular M13 pays for exactly four layers and not five** — 15 × 112 bp × 4 = **6 720** of 7 249 nt (92.7 %), where five layers are 1 151 nt over — so the 4.31× scaffold excess is spent almost exactly by the thickness §3 states. **What survives, narrowed 4.6×, is a statement about the COUPLING and not about the tile**: under `C-0087`'s measured dropout the best *coupled* four-layer 90th percentile is **0.116465044**, 1.16× the convention against the single layer's 0.532748246, and **every coupled cell is worse than the uncoupled tile** — `CLAUDE.md`'s own *"an attachment coupling can be a NET DISHING SOURCE"* read on a tile that no longer needs the correction. That 1.16× is inside what `C-0089`'s distribution axis (1.30–1.61×) and `C-0093`'s topology axis (2.22×) are already known to buy, neither of which is searched there. **Three things qualify it and are stated rather than assumed**: the verdict is conditional on the interlayer coupling fraction `f`, measured at **0.26–0.33** on four bundles across two lattices and three labs, and **`T-196` HAS NOW LOCATED THE CROSSING AND THE VERDICT SURVIVES WITH 3.30× OF MARGIN** (`C-0116`): it sits at **`f` = 0.0788618807**, reached **monotonically** — one sign change over all of `[0, 1]`, counted rather than assumed — so the measured band's *adverse* low end clears it by **3.29690337×** and its centre by **3.80411927×**, and `CH-0124`'s true honeycomb spacing has its own threshold **0.105149174**, cleared by **2.47267753×**. **What the verdict now rests on is one unmeasured number**: the `f` a 15-wide × 4-deep **slab** realises against the measured **rods**, which would have to fall below **30 %** of the least-coupled measured bundle for the tile to stop being flat; the calibration is measured on **rods** and a 15-wide × 4-deep slab has a different crossover topology, so 0.30 is plausibly an *upper* bound there; and `C-0093` already found a *buildable* four-layer body reads 0.100166871 where its rigid limit reads 0.0344013403, so **body rigidity is first order rather than an idealisation**. `C-0109` raises `CH-0124` (`C-0006`'s four-layer variant is a **mixed state**, not a bound, and its layer spacing is not a honeycomb's — worth 0.160153834 against 0.0577199433, i.e. it would have called the tile *not* flat) and `CH-0125` (`C-0093`'s brick is mis-specified in three ways whose net is **not signed**), both open. Also unre-derived on the thicker tile: `C-0022`'s charge, `C-0004`'s drainage, the honeycomb raster width (`T-198`) and the attachment lattice of a four-layer top face — every plan ceiling, phase result and placement in this corpus is single-layer square-lattice. **AND THE ROW-END PRESTRAIN NOW HAS A VALUE, WHICH THE FILE CARRIED NOWHERE** (`C-0107`, `T-182`, iteration 22; `C-0112`, `T-190`, iteration 23). `C-0104`'s 15.45° was a threshold with no measured quantity beside it; the derived register value is **17.15–24.98°**, past it, in the adverse sign pattern, and with **no accessible measurement** — the one published study of the coordinate (Snodin et al., *NAR* **47**:1585) excludes in a scope clause exactly the sites in question, which is far stronger evidence than a null search and reverses `C-0099`'s recommendation against an oxDNA edge-crossover run (`T-189`, formerly `T-183`, has now taken the cheaper alternative: **the row CAN be twist-corrected, at 110 bp = 37.40 nm, and it takes the row-end register to −3.56…−2.19° — but it RELOCATES the strain into the interior (peak 12.37–12.84°) and the flatness at a frozen placement moves only 1.07×**, `C-0133`). `CH-0122` (open) corrects that threshold's own derivation: 15.4497275° is a **secant** of the peak and the triangle inequality it is named as gives **11.5188°**, 1.34× tighter — the secant bound survives by *convexity*, an argument the claim does not make. **And the graded field is flat where the idealisation is not**: read over all 56 crossovers rather than the 14 row ends, the complete corrugated field reads **0.0922622269** — flat, and flat at **both** overall signs (0.0910197) and at **40 of 40** cells of `C-0107`'s bracket, where the row-end-only map is flat at 14 of 40. The 42 interior sites are the **larger** half (53.65 % of the assembled absolute couple), the split is an exact identity (`2.1e−15`) while the *verdict* is not — peak dishing is a **seminorm**, so the graded peak is 0.294 of the sum of its parts' peaks and the cancellation is a **cross term**, cosine **−0.579495374**. `C-0112` raises `CH-0129` (the published comparison differences two states differing in **three** factors, so the true interior term is 2.68× what was credited) and `CH-0130` (the overall sign is undetermined; raised *and* discharged on the number, the unread state being flat). Two qualifications on the closure itself: `CH-0119` (open) charges that `C-0098`'s redundancy slope is fitted through placement-**searched** optima and therefore does not measure the count axis — on the same lattice a *nested* chain fits −0.740086889 where a searched one fits +0.0610348337, the same data with opposite signs (`C-0103`) — and `CH-0104` (open) that the *oracle* floor, **0.00111–0.01988** on the same realisations, licenses nothing, because a coupling is specified before it is folded. **AND THE TILE IS SOMEBODY'S PUBLISHED DESIGN, WHOSE OWN SOURCE RECOMMENDS A DIFFERENT ONE, AND ON THAT ONE A COUPLED TILE IS FLAT UNDER THE MEASURED FOLDING** (`C-0119`, `C-0120`, `C-0118`, iteration 25). The four-layer tile is **design (i) of the caDNAno paper** — 15 × 4 in Douglas et al.'s nomenclature, 15 x-raster rows of 4 helices, 60 duplexes — **folded from p8064 and one of only three of seven cross-sections to produce sharp leading monomer bands**; the honeycomb's scaffold-crossover lattice is `7k ± 5` bp and **integral** (`C-0086`'s odd-half-turn rule fails there as a **domain error**, not a prohibition), and a **seam is still forced** because *"the path of the scaffold stays within a 2D surface"*, so the graph the scaffold may use is a path even though the honeycomb's is three-regular. **That paper recommends `10 × 6`** — the same 60 helices — and on this programme's own criterion it is **6.6× flatter (0.00874363524 against 0.0577199433) and has NO composite-fraction threshold at all**: its dishing never reaches 0.10 anywhere in `f ∈ [0, 1]`, **including `f = 0` where the layers are fully independent**, so its flatness does not depend on the interlayer-coupling calibration that the `15 × 4` verdict rests on. **The stronger cross-section is stronger by REMOVING a dependency, not by widening a margin** — at **two-thirds of the footprint** (38.08 × 25.36 nm against §3's ~40 × 40), over which §3's 100 pN is specified and on which `C-0022`'s collar was **not** solved, so it is a specification trade rather than a free improvement (`C-0120`). **AND THE COUPLED TILE IS FLAT** (`C-0118`): of **16** graded cells at `C-0017`'s mandated total under `C-0087`'s measured dropout, **9 are flat at the 90th percentile** — **all eight** on `10 × 6`, best **0.0278431488** at the *sparsest* coupling tested (one column, ten paths, equal springs), and **one of eight** on `15 × 4` at **0.0882933461**. **The cross-section is worth 3.17109774× and the distribution has no consistent sign.** This is the first coupled tile in the programme to clear `T-5b` under the only folding statistics anybody has measured — and it re-frames rather than contradicts *"every coupled cell is worse than the uncoupled tile"*, which is true and is **not a design verdict**, because §3 requires 100 pN to reach a load so the mandate is an **equality on the SUM** and the uncoupled tile is a **reference, never a candidate**. **What it does not establish is a census**: the attachment grid is the abstract one, every plan ceiling in this corpus is single-layer **square**-lattice, and the honeycomb's three azimuths at 7 bp are a different inventory nobody has counted — so a path count there is a **request**, not a demonstration that the stations exist.

## 4. §7 — what NDI would count as the loop working

| §7 criterion | Where to check it |
|---|---|
| Inherited numbers get re-derived | `a = 0.35 nm` closed two ways (`C-0002`); `λ_D ≈ 4 nm` re-derived to 1.8 % (`C-0005`); the de Gennes wall mapping derived, not looked up (`C-0001`); the MWC form rebuilt rather than cited. |
| Premises checked against the material | The semidilute premise **failed** (`CH-0001`); the Darcy premise **failed** where it did not change the verdict (`C-0004`); strong stretching is outside its own premise (`CH-0003`); `χ ≈ 0.45` turned out to have **no primary source at all** (`C-0007`). |
| Method justified against cost, cheap bound first | SCF numerics deferred twice on the stated ground that it would be *"calibrating to a guess"*, and bought only once the interaction was anchored in measurement (`T-1d`). MD declined for the Hofmeister effect because it would be *worse* than the existing measurement, not merely dearer (`C-0007`). Explicit-ion MC costed at 1–3 weeks and not run (`C-0005`). `T-15` built an in-plane *sibling* lattice rather than adding degrees of freedom to the out-of-plane one, on the ground that the two decouple exactly for a flat sheet and merging them would have forced re-verification of four published claims for a change that cannot move them. `T-19` bounded all four entry topologies by a cut-equilibrium pigeonhole before assembling a matrix, which settled two of them outright and redirected the footprint question from the lattice to the literature — where its answer was. |
| Validity ranges travel and are respected | `C-0008` is handed to `T-3` with an explicit may/may-not list; `C-0004` is parameterised by a stiffness that was being re-derived concurrently. |
| Disagreement raised as a challenge, not an overwrite | ~~**One hundred and nine**~~ ~~**One hundred and fifty-four**~~ **One hundred and eighty-four** challenges in [`gpd/challenges/`](gpd/challenges/), against ~~**one hundred**~~ ~~**one hundred and forty**~~ **one hundred and sixty-one** claims (read at this pass's own commit, iteration 39 — a sibling filed one *while this pass was running*, which is why the row is quoted with its state) — **more challenges than claims, and that ratio is the point** (**CORRECTED three times**: this row read *"Twenty-nine"* at iteration 4, *"Sixty-nine … against sixty"* at iteration 12 and *"Eighty-four … against seventy-four"* at iteration 16 and *"One hundred and fifty-four … against one hundred and forty"* at iteration 37 — the ratio is the invariant, not either count, and **this row has now been stale four times out of five passes**, which is the argument for deriving it rather than writing it: the counts are `ls gpd/challenges/CH-*.md \| wc -l` and `ls gpd/claims/C-*.md \| wc -l`, and no retained checker reads either). `CH-0007` challenges **our own** queue's reading, not a subordinate's, and `CH-0021` corrects a factor `C-0014` had itself flagged as a stand-in — finding it wrong in **both** directions. |
| Model-consistent vs measured maintained | Every claim header carries it; no `PASS` in this repository asserts measurement. |
| A feared effect chased down and *dissolved* rather than carried | The "grafted `χ` ≈ 0.60", once thought 239× the salt effect, turned out to be `1.2 × ½` assembled from an abstract, against a model whose own theta is 0.696, for the wrong geometry and the wrong observable (`C-0013`, `CH-0012`). |
| Unanswerable questions stated plainly | The Mg²⁺/PEG binding constant does not exist in accessible literature (`P-8`); ~~the crossover hinge constant is a fitted model input (`T-9`)~~ — **measured in iteration 35** (`C-0157`): oxDNA brackets `k_θ` at `5.62052112 – 25.9227606 pN·nm/rad` and the fitted `13.5294118` is inside it, so the input keeps its value and changes its ground — **and iteration 39 narrows what that ground covers** ([`CH-0209`](gpd/challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md), open): the run relaxed a sheet carrying **one** strand crossing per junction, which is **one of two** motifs the field draws, and the pair at `o` and `o+1` that `scadnano.origami_rectangle` emits is measured **nowhere in this repository**; the crossover's **vertical compliance** and **in-plane shear** stay unmeasured; leaf `A1.2`'s CI is **not discharged** rather than approximated; the intermediate-coupling regime has no systematic theory and the sources say so themselves. |

---

## 5. What we cannot answer, and why

- **The 95 % CI of leaf `A1.2`.** Requires an ensemble; the named tool models the wrong subsystem.
- **The Mg²⁺/PEG coordination constant.** Two independent searches; the mechanism is documented in water, the
  number is not, and the quantitative NMR work is in methanol. It needs a paywalled pull or an experiment.
- ~~**The crossover hinge constant `k_θ`.** No accessible measurement of a single-layer origami sheet's bending
  rigidity exists in any direction. Costed as `T-9` — days of oxDNA on 8 cores.~~
  **RUN, iteration 35** (`C-0157`, `gpd/results/T-9-crossover-hinge-constant.json`). It cost a day on 8 cores.
  The interduplex roll of this programme's own 112 bp raster brackets `k_θ` at
  **`5.62052112 – 25.9227606 pN·nm/rad`**, and the corpus's fitted **`13.5294118`** sits inside — so `D_⊥`
  and the `25.5607302×` anisotropy survive on a measurement. **The bracket is `4.61×` wide and its width is
  an assumption, not a sample size**, so more compute does not narrow it. What the same run could **not**
  answer is the item below it: the three plate rigidities are under-sampled by 4–5× in the direction that
  would have flattered this corpus, at 12–55 h per replica to settle. Still unanswered: the crossover's
  **vertical compliance** and **in-plane shear `k_s`**, which is why `T-9` stays open.
  **AND, iteration 39, WHICH MOTIF WAS MEASURED IS NOW A QUESTION**
  ([`CH-0209`](gpd/challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md), open, raised by
  [`C-0161`](gpd/claims/C-0161-mechanics-on-an-imported-design.md)). The relaxed sheet carries **one** strand
  crossing per junction; the field's own reference generator draws **two**, at adjacent offsets, on every
  interface of its canonical Rothemund rectangle — **90 strand crossings over 45 junction positions**, counted
  by the reference implementation's own parser. The two cases `C-0157` reasoned about (one crossing, or two at
  the **same** offset, which does not relax) **do not cover** the one the field actually draws. Nothing here says
  the bracket is wrong: it is a property of the object simulated, and that object is stated. What is missing is a
  relaxation of the other motif — the same 15 × 112 bp sheet with each crossover drawn as a pair — whose three
  possible outcomes are **all** useful, and which is the same shape of run `T-9` already has a driver for.
- ~~**The direction of the correlation correction for *oppositely* charged walls.** Every published coupling
  criterion is a like-charge result. This is the largest uncertainty on every electrostatic force here.~~
  **NARROWED TWICE AND NO LONGER THE LARGEST, iterations 32–34** (`C-0137`, `T-50`; `C-0143`, `T-221`).
  The premise is wrong as stated: Kanduč et al. publish an oppositely-charged branch, and their Monte Carlo at
  `Ξ` up to **86** finds Poisson-Boltzmann and strong coupling *"nearly coincide"* there — **`C-0005`'s
  `Ξ = 17–24` alarm is calibrated on the LIKE-charged problem and this device is not that problem.**
  What `C-0143` then settles is which **input** that inequality is owed: the criterion is **scale-covariant**,
  so the disputed `16.5×` is **100 % the bare/renormalised axis and 0 % the cylinder/plane axis it was framed
  as**, and it is owed at the **bare** charge. On the branch this device is on it is worth **half a per cent**
  of an asymmetry range. **One clause does not survive**: `C-0137` declined to evaluate the attractive branch
  because its right-hand side is *"exponentially large"*, and
  [`CH-0179`](gpd/challenges/CH-0179-exponentially-large-is-not-a-property-of-the-branch.md) (open) shows that
  is a statement **at fixed asymmetry** — the bound diverges at both ends of the admissible range and has an
  infimum in between, so the conservatism it was offered as is **6–15 %**, and it **reverses** at one of the
  eighteen readings. **No verdict moves; the ground under one clause does.**
  What is genuinely still open is the **primitive-model Monte Carlo `C-0005` prices at 1–3 weeks**, which was
  **not run and is not superseded** — the intermediate regime still has no systematic theory.
- **Anything about a SEAMED sheet — and that is a scope statement about a large body of standing work, not a
  new question.** Iteration 39 graded `scadnano.origami_rectangle`, the reference implementation's own Rothemund
  rectangle, through this repository's own mechanics ([`C-0161`](gpd/claims/C-0161-mechanics-on-an-imported-design.md) §4(b)).
  `CrossoverLayout.centred` and `CrossoverLayout.phased` **alternate the column parity by construction**; a seam
  **doubles a column pitch**, so two consecutive columns serve the same interface parity, and the rectangle's
  sequence is **`[0, 1, 0, 1, 1, 0]`**. **No phase sweep in this corpus can generate it.** So every phase-swept
  placement, count and flatness result here — §3 row (g)'s whole phase axis, `C-0015`'s 32 phases, `C-0063`'s
  exhaustive centro-symmetric family, `C-0090`'s two buildable phases, `C-0102`'s over-subscription — is over the
  **alternating** family. It is a restriction on the swept family and **not** on the data structure: the layout
  class carries the parities explicitly and represents the seamed sheet without complaint, and the rectangle is
  representable exactly (90 lattice sites against 90 drawn, 0 absent). What cannot be answered from anything
  standing here is **what a seam does to a placement**, and it took a design this repository did not draw to see
  that the question had never been asked. Nothing re-runs those studies and `C-0161` states the restriction
  rather than acting on it.
- **The tension in a row-end crossover.** `C-0104` shows this is the one row-end unknown that can move a
  verdict — `T-5b`'s 0.10 sits at **15.45°** of uniform prestrain and the lattice's own register ladder
  reaches it — and that no accessible source quantifies it, over 10 recorded queries and 68 records.
  Rothemund states the edge strain is unrelieved and that *"how the strain is actually relieved is
  unknown"*, which is the nearest thing to a source there is.
  **UPDATED, iterations 22–23 — the quantity now has a DERIVED value and the threshold has moved twice.**
  `C-0107` derives **17.15–24.98°** from the lattice's own register, i.e. **past** `C-0104`'s threshold, in
  the adverse sign, and confirms there is no accessible measurement: the one published study of this
  coordinate (Snodin *et al.*, *NAR* **47**:1585, oxDNA on a 2D tile) **excludes in a scope clause exactly
  these sites** — *"exclude[s] the outermost junctions on the tile, and the junctions next to the scaffold
  seam as well as the seam itself"* — which is far stronger evidence of absence than a null search, and it
  **reverses `C-0099`'s recommendation against an oxDNA edge-crossover run**. `CH-0122` (open) then corrects
  the threshold's own derivation: 15.4497275° is a **secant** of the peak, and the triangle inequality it is
  named as gives **11.5188°**, 1.34× tighter — the secant survives by *convexity*, an argument `C-0104` does
  not make. **And `C-0112` shows the verdict is not the row ends' to move**: read over all 56 crossovers
  rather than the 14 row ends, the complete graded field is **flat** (0.0922622269, and 0.0910197 at the other
  overall sign, at **40 of 40** bracket cells against 14 of 40 for the row-end-only idealisation), the 42
  interior sites carry the **larger** half of the eigenstrain (53.65 % of the assembled absolute couple), and
  the cancellation is a **cross term** (cosine −0.579495374) that cannot be attributed to either site set.
  So what is still missing is narrower than it was: not *"the tension in a row-end crossover"* but **whether
  the overall sign of the corrugation is what the lattice's parity rule says** (`CH-0130`, raised and
  discharged on the number — both signs are flat) and **an independent value for the register the boundary
  layer relieves**, which `T-189`'s twist correction would remove instead of measuring —
  **taken at iteration 31** (`C-0133`): the correction exists at 110 bp = 37.40 nm and takes the row-end
  register from +16.68…+24.79° to **−3.56…−2.19°**, but it **relocates** the strain into the interior
  (peak **12.37–12.84°**) rather than removing it, so at a frozen placement the flatness moves only
  **1.07×** and at a re-optimised one **1.41×** with the argmin moving. What it costs is one base pair
  of arm and, unless the out-of-plane offset is mirrored, the station lattice's centro-symmetry.
  **AND THE WHOLE ITEM IS A SQUARE-LATTICE ITEM, which matters because the recommended tile is not one**
  (`C-0136`, `T-217`, iteration 31): the honeycomb's design twist is `720/21 = 34.2857 °/bp`, which **is**
  B-DNA's `360/10.5` — the same number — so its `Δω` is **exactly zero** and there is **no twist to correct**.
  Every register number in `C-0104`, `C-0107` and `C-0133` is therefore a **single-layer square-lattice**
  number, and on the four-layer honeycomb tile §3 specifies this axis is **empty** rather than unmeasured.
  `C-0136` also removes the variable the square-lattice half was being optimised over: a **seamless** raster
  row has **no translational phase variable at all** — the admissible rigid translation group is `{0}` at
  112 bp and at `C-0133`'s 110 bp alike — so `C-0090`'s *"ten eight-column phases collapse to two"* is a
  collapse from a translation to a **parity**. What a mixed-domain row gains instead is an **arrangement**
  axis of **21** members, every one carrying eight columns as an identity.
- ~~**A CENSUS OF THE HONEYCOMB'S ATTACHMENT LATTICE.** New at iteration 25, and it is now the largest gap in
  the four-layer line.~~ **DISCHARGED, iteration 26–28** (`C-0122`, ~~corrected upward by `CH-0151`~~, priced by
  `C-0128`): the top face supplies ~~**132** stations on `15 × 4` and **90** on `10 × 6`~~, against 10 to 75
  demanded; snapping to the lattice's real 21 bp ladder keeps every flat cell flat; and an oblique root —
  ~~which half the top-face helices carry~~ — costs **nothing** for a flexible tie, as a symmetry, and
  ~~**6.01719478×**~~ for a crossover-hinged body. The original entry is kept below because it is the reasoning
  the discharge answers.
  **RESTATED, iterations 33–34 ([`C-0141`](gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md),
  [`CH-0175`](gpd/challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md)) — the
  discharge stands and every reason given for it is withdrawn.** `C-0122`'s **90** and **60** reproduce at
  departure `0.0` and `CH-0151`'s upward correction is withdrawn: its `±60°` pair belongs to a helix whose two
  up-oblique neighbours are absent, which on a full `m × n` block they are not. The face carries **exactly one**
  rooting azimuth per helix, at **30°**, sign alternating, and **no perpendicular root exists anywhere on it**,
  so `C-0128`'s rigid-body oblique cost falls to **2.67233333×**. `C-0141` also supplies the two things this
  entry said did not exist — the **plan ceiling** (an exact bisection at ten counts, reproducing `C-0072`'s
  **9.535 nm** on the square lattice at departure `0.0`) and the **centro-symmetric placement family**, which
  `10 × 6` admits at the full 60 stations and `15 × 4` admits at **none** of 21 phases, at either offset and
  either row length.
  **RESTATED, iteration 38** ([`C-0148`](gpd/claims/C-0148-face-bond-class-residues-and-row-span-columns.md),
  [`C-0151`](gpd/claims/C-0151-closing-raster-selection.md),
  [`CH-0189`](gpd/challenges/CH-0189-the-ladder-phase-is-not-a-sweep.md)): **the ladder phase is not a sweep
  and the offset is not a choice.** caDNAno's own `±5 bp` scaffold rule over-determines both — the inter-row
  offset is **14** at every one of **32** proper readings, and at the drawable `102 / 109` raster the phase is
  **determined at 16**, which is also the optimum of the 21-phase sweep. There it carries `5, 6, 5, 6, …`, i.e.
  **55 of 60** stations rather than the full 60, so a **six**-column placement does not stand: the sparsest row
  caps a placement at **five** columns and **50** paths. The saturating pair `CH-0184` found lives at
  `112 / 108`, which does not close, and **exactly one** closing pair inside M13 saturates the census —
  `123 / 109`, at 60 of 60 and an axial extent of **46.58 nm**, `+16.45 %` on §3's nominal. **A six-column
  honeycomb placement exists, is drawable, and costs a tile sixteen per cent too wide.** And the path counts are no longer requests: `C-0142` snaps all sixteen coupled placements
  onto that ladder, worst snap **3.332 nm** inside a 3.57 nm ceiling, and **three stay flat**.
- **A CENSUS OF THE HONEYCOMB'S ATTACHMENT LATTICE** (the entry as written at iteration 25). Every plan ceiling, station lattice, crossover phase and placement in this corpus is
  **single-layer square-lattice**; the honeycomb has **three** crossover azimuths at 7 bp rather than the
  square lattice's four at 8 bp (`C-0119`, read directly from Douglas et al.), and **nobody has counted what
  that offers**. So every path count in `C-0118`'s flat coupled cells — including the ten-path winner — is a
  **request** rather than a demonstration that the stations exist. It is a lattice derivation, not a
  measurement, and it is cheap; it is listed here because a reader would otherwise take the coupled flatness
  result as buildable, which it is not yet shown to be.
- **WHAT A SHORT SCAFFOLD TURN LOOP DOES TO FOLDING YIELD**, added iteration 36
  ([`C-0147`](gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md), `T-230`) — and this one is
  **declared unpriceable rather than merely unmeasured**. The built caDNAno blocks spend **28 unpaired
  nucleotides** per raster turn against a **6 nt** covalent **reach** bound on `T-71`'s measured backbone, so
  28 is **4.66666667×** its own bound and is a **choice**. Whether a shorter loop folds is a *yield* question,
  and **no published measurement is on that axis**: the three nearest are Ke et al.'s **8 bp staple domain**,
  Rothemund's scaffold **linearisation** (63 % → 11 %) and Strauss et al.'s per-**staple** incorporation
  (48–95 %), and the only measured point on the loop-length axis is the built blocks themselves. **The
  threshold is quoted instead, and it is 8 nt** — `60 × (112 + L) ≤ 7 249` — because at or below 8 a uniform
  112 bp row fits M13 and above it it does not. At 8 nt the turn carries **6.54349121–12.112167 pN**, at or
  past the 10 pN unzip allowable, so the route fits **strained**; **p8064 affords 22 nt and removes the
  question**, which is half of why decision 7b matters. This is a **fabrication** column and
  `CLAUDE.md` records that this programme does not have one: it can rank what a route **buys** and cannot
  price what it **costs**.
- **WHAT A FORCED SCAFFOLD CROSSOVER COSTS IN FOLDING YIELD**, added iteration 38
  ([`C-0152`](gpd/claims/C-0152-forced-scaffold-crossover-price.md), `T-246`) — the same shape as the entry
  above, and the **elastic** half of it is now closed. A crossover placed where caDNAno's default `±5 bp` rule
  does not allow it is an **azimuth**, not a count of base pairs: because one turn is **10.5** bp the smallest
  departure the 21-residue lattice offers is **`17.1428571°`**, reached at a displacement of **ten or eleven**
  base pairs rather than one. Its ceiling is **`0.350894669 k_BT`** — **sub-thermal**, `2.84985806×` below one
  `k_BT` — and the whole of a `112 / 108` raster's ten forced crossovers costs **`0.438634952`** of **one**
  crossover column of the host sheet's own demonstrated currency. The lattice supplies its own calibration for
  nothing: at the exact 10.5 bp/turn geometry an **allowed** scaffold crossover already carries
  **`8.57142857°`**, which every honeycomb origami ever folded absorbs, and a forced one adds exactly
  **twice** that. **So a forced crossover cannot be argued out of a fold on elastic grounds** — and whether it
  folds is a **kinetic** question, on which **no published price exists**: **68** queries in **7** declared
  families, and the source that *defines* the operation says in its own Discussion that its structural
  consequences are **not predicted** and that *"more work is also needed to see what design rules lead to
  stable structures"*. Seventeen years old and unanswered. This is why the recommended raster is `102 / 109` —
  which needs no forcing at all — and why closure is stated as a **preference** rather than as a prohibition.
- **A per-site staple-incorporation measurement on a COUPLING-BEARING tile.** This is now the single most
  consequential missing measurement in the programme, and it was not on this list before iteration 21.
  `C-0087`/`C-0089`/`C-0093`/`C-0098` between them close the flat-tile question on **every design axis this
  programme can reach** — count, distribution, placement, topology and phase — leaving *"the tile is flat as
  designed and has not been shown flat as built"*. What decides it is therefore a **fabrication yield**, and
  the only per-staple map anybody has published (Strauss et al. 2018, 48–95 %, mean 84 %) is of a **bare
  Rothemund rectangle**, which `CH-0102` shows is not this tile: a 40 nm tile carries **1.85×** the perimeter
  per unit area, so the measured 0.84 transfers to 0.759–0.790 here. The measurement is **routine by
  Strauss's own method** and needs a bench, not a solver.
- **The stiffness spread of nominally identical hybridised staple extensions.** Distinct from the incorporation
  map above and still not found in nine queries across three databases (`T-45`, `C-0072`). The nearest
  published work measures an inter-subunit ssDNA handle by cryo-EM multi-body refinement, which is the method
  that would supply it one level down.
- **A DNA–DNA force curve at 2 mM MgCl₂.** `C-0079` establishes that two unbonded duplexes are repulsive at
  every separation on four independent methods, so the plan model's exclusion width is a **threshold on an
  energy** rather than a separation — but the lowest *parametrised* force curve in the literature is at
  **20 mM**, an order of magnitude above the Gen-1 buffer, and the second-virial measurement nearest to it is
  at 3 mM and gives a sign rather than a curve.
- **A compression measurement of a PEG brush *inside* the Gen-1 grafting window.** None exists. `P-9` bounds
  the bulk-versus-brush `χ` difference at `|Δχ| ≤ 0.053` from *denser* layers, so the bound comes from above
  and assumes monotonicity in grafting density.
- **One paywalled paper** would close the last genuinely missing measurement of `P-6` — Boucher & Hines,
  *J. Polym. Sci. Polym. Phys. Ed.* **14**:2241 (1976), the only study that measured Group II chlorides
  against PEO. This is an access limit, not a compute limit, and it is the first thing this programme has
  needed that the machine cannot supply.
  (**CORRECTED, iteration 12** — this read *"Two paywalled papers"*. The second, Lee et al.,
  *J. Phys. Chem. B* **116**:7367 (2012), **was obtained free** from NIST's public repository, because two of
  its coauthors are federal staff; Unpaywall and OpenAlex both reported it `closed` and both were wrong.
  `P-8`'s Mg²⁺/PEG constant is the bullet above, and it may not exist at all rather than be paywalled.)

---

- **A fluctuation-corrected density profile for the layer.**
  The expansion whose saddle point is the SCF is broken at `φ ≈ 0.01`, so the correction cannot be *computed*
  — only bracketed by re-running the mean field over the range the broken series licenses. The method that
  would settle it is a field-theoretic (complex-Langevin) simulation, costed at weeks; the nearest published
  substitute is paywalled with no repository copy. **The bracket is ≤ 9.4 % on stiffness, so nothing turns on
  it.** (`C-0019`.)
- ~~**Whether `C-0018`'s pull-in bias itself moves.**~~ **ANSWERED — `C-0033` (`T-60`), iteration 5, and this
  bullet should have left the list then.**
  `T-3b`'s own solver was run on the equilibrium path, so `d ln μ/dh` is a derivative rather than a
  three-scheme band: **0.01763–0.02011 nm⁻¹** at the 10 nm folds, converged to 0.11 % in the mesh. The
  collar-only tangent at `C-0018`'s own fold is **+2.60 to +4.99 pN/nm, strictly positive**, and at 10 nm /
  2 mM the margin **rises** to 1.021–1.028 where pull-in still binds — but the direction is not universal,
  and at 7 nm / 10 mM it **falls** 0.9–3.5 %, because the margin is a ratio of two biases at two gaps and
  moves with the sign of `3 nm − s_fold`. `C-0051` then composed it with the coupling the programme actually
  has: **−8.40 to −11.06 pN/nm** at 6 of 6 models, the collar recovering **27–49 %** of what `C-0032`'s
  element costs, so `C-0032`'s **1.0000–1.0019** stands (`CH-0063`). (`C-0033`, `C-0051`, `CH-0051`,
  `CH-0063`.)
- **The thermal force in a crossover.**
  It is `√(k_BT k_v)` with `k_v` the crossover's vertical stiffness, which nothing measures (`T-9`); the
  programme's lattice models it as a rigid penalty, in which the *static* force converges and the
  *fluctuating* one provably does not. Bracketed at 2.78–115.8 pN and reported as a bracket. (`C-0026`.)
- **Whether a duplex can be routed at 90° out of a single-layer sheet at all.**
  `T-40` consulted the literature `C-0025` had not, and found **no published instance** — and the two obvious
  motifs are excluded, because a nicked continuation preserves the helix axis and the antiparallel crossover
  requires parallel helices. The base condition every published out-of-plane element actually uses is a
  **pin**, which is a mechanism here. A buildability question, not a modelling one. (`C-0028`, `T-66`, `T-67`.)
  **Narrowed since, and still open**: `C-0029` found a covalent routing, `C-0057` found that its **dihedrals
  do not close** while feasible placements exist elsewhere in the same search space, and `C-0055` found the
  *site* — the unoccupied out-of-plane crossover azimuth — to be **published geometry** while a free lever
  held on it is **not published**, in 62 recorded queries. The literature answer has not changed.
  **Narrowed again in iterations 10–11, and in the unfavourable direction, by a lattice fact rather than by
  chemistry**: a base misalignment floor is a **minimum over an axial coordinate that an array pins**, and 34
  instances pin it once for all of them (they are one helical phase class). `C-0062`'s recommended 10 bp row
  is reachable only at **57.0°** against its published 6.0° floor — past the **45°** at which `C-0037`'s
  two-link base cannot be represented at all — so that design table stands for a **lone** truss and is
  withdrawn for an **array**; the buildable row is **9 bp at 18.0°** (`CH-0078`, `C-0065`, `C-0070`).
  Two branch recommendations moved with it: the row pitch from **7 bp to 9–10 bp**, and the topology from
  `C-0029`'s scaffold excursion to **two independent staples** (`CH-0072`).
- ~~**Which body carries the standoffs, and what sits under the flexure's midspan.**~~ **ANSWERED —
  `C-0035` (`T-75`, `T-78`), iteration 5, and this bullet should have left the list then.**
  It was never a free binary: the sign is a product of **two** binaries and exactly one of the four mountings
  is buildable (bases on the output superstructure, standoffs pointing away from the tile, flexure outboard,
  midspan tied back down through its own ground). What sits under the midspan is that same ground **by
  construction**. The specification gap it hands on is a *different* one — whether the superstructure may be
  **perforated** — `T-95`, which is **DISCHARGED** rather than open: `C-0071` found it stopped applying
  once `CH-0081` removed the flexure-and-tie branch from the output role, so the recommended element has no
  tie grid and nothing crosses the standoff base plane. (`C-0035`, discharged by `C-0071`.)
- ~~**Whether a strain-softening coupling still satisfies the stability clause.**~~ **ANSWERED — `C-0032`
  (`T-76`) and `C-0049` (`T-107`), iterations 5 and 7.**
  `CH-0042` is **UPHELD and RESOLVED**: **NO at 2 mM, YES at 0.5 mM**, and that is a design decision rather
  than a calculation (open question 3). `CH-0047` is answered too — the range a stability tangent is
  minimised over is `[0, s*]`, so the 22.88 pN/nm minimum at a 4.55 nm stroke is read at a state the placed
  device never occupies; at the placement stroke the same flexure is **25.227 pN/nm** and clears **4 of 6**
  of `C-0017`'s 2 mM floors. (`C-0032`, `C-0049`, `CH-0042`, `CH-0047`.)
- **Whether a flexure array on a shared superstructure stays as compliant as independent leaf springs.**
  `C-0023` models 45 of them as independent, which is the *compliant* reading — and the compliance ceiling is
  the binding side, so the assumption is not conservative. (`T-31`.)
  **RE-SCOPED, and the re-scoping is most of the answer**: at 45 the array **has no plan view** (`C-0041`),
  so the question is moot at the count it was asked about, and the recommended element has no flexure at all.
  It returns at **15 paths**, where the beams are one duplex apart and in one level, and there whether they
  should be crossed over to each other is a live design choice rather than a modelling gap.
  **And the bound `C-0069` placed on that whole family is a bound at 34 PATHS, not on the family**
  (`CH-0108`, open): *"refused at every span, every end joint and every placement"* inherits the path count
  it was searched at, and the path count is a design variable — which is why `C-0075`'s step function in the
  count exists at all.
- ~~**What separation two UNBONDED duplexes hold in this buffer.**~~ **ANSWERED — `C-0079` (`T-139`),
  iteration 16, and the answer is that the quantity does not exist as a separation.**
  `C-0076` found that the `d` of the plan model is the girth of a free body while the 2.69 nm it is evaluated
  at is the packing distance of a crossover-**bonded** pair, so the number had never been measured in the
  role the plan model uses it in; the verdict is a step function at **2.715609 nm** and the recommended
  array's placed count is **34 below it and 22 above**. `T-139` then found that two unbonded duplexes are
  repulsive at **every** separation, on four independent methods each read directly, so they hold **no**
  equilibrium spacing and the plan model's `d` is a **threshold on an energy** rather than a distance
  (`CH-0094`): the map runs **11.45 nm at 0.5 `k_BT` to ≤ 2.1 nm at 8 `k_BT`** and **straddles** the
  2.715609 nm threshold, so every plan claim in this branch states a width and none states the energy budget
  it was read at. What survives on this list is the **force curve**, three bullets above: the lowest
  *parametrised* one in the literature is at 20 mM. (`C-0076`, `C-0079`, `CH-0094`.)
- ~~**What the electrode is made of.**~~ **ANSWERED — 2026-08-18, NDI: template-stripped gold,
  *"for initial experiments"*.** Metal against oxide was **2.6×** on the van der Waals hold-down, the one term
  no design can remove, and the answer collapses that bracket onto its **adverse** end: `C-0021`'s gold row —
  10.4–17.2 pN at 5 nm, 0.74–1.42 at 10 nm — stops being the top of a four-material range and becomes the
  number, and the *"retardation is sourced for gold only"* caveat is discharged. **No verdict moves**, because
  the finding is about the *shape* of a `1/h³` force: it integrates to a bounded potential, so 0 of 54 states
  confine either way. *Template-stripped* is a second statement and it is favourable — an atomically flat
  Au(111)-textured surface is the one case where a smooth-wall solve and a planar slab are the right
  idealisations. (`C-0021`, `P-13`.)
- ~~**Where the electrode's potential of zero charge sits.** **STILL OPEN, and it is the half that matters.**~~
  **ANSWERED from published measurement, iteration 23 (`C-0111`, `T-193`).** The residue that answer leaves
  is a different question, and it is stated below rather than inherited. `E_pzc(Au(111)) = 0.46–0.51 V vs SHE` in 1 mM HClO₄ (Adnan *et al.*, *PCCP* **26**:21419, 2024,
  read directly; template-stripped gold is (111)-dominated). That is **90.2–575.7×** the scale that decides
  the zero-bias hold-down, and **of the wrong sign**: the model's *"applied bias"* is the **rational**
  potential `E − E_pzc`, so an electrode sitting at zero volt on that scale is negatively charged and
  **lifts** the negatively charged tile rather than holding it down. The whole sign structure lives inside
  **11.2 mV** at the 10 nm layer — lift −6.087, force-free −0.314, hold-down **+5.10177544** mV — 41× narrower
  than the offset a bench must null, and the requirement is that the electrode be held within **5.10** mV of
  its own PZC at 10 nm and **0.886** mV at 5 nm. No force is quoted at the lifting sign, because it falls
  outside `C-0005`'s point-ion boundary at 9 of 9 states; the answer is the **threshold**.
  **What is now open is the cell's definition of zero** — 0 V against a named reference electrode, the cell
  at open circuit, or two identical electrodes shorted are hundreds of millivolts apart and differ in the
  **sign** of the force at rest. That is a one-line ask, filed under decision 3 of
  [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md). **The material half moves no verdict and moves one ground**:
  gold is the *stiffest* of the four candidates, so NDI's answer collapses the 2.6× bracket onto its
  **adverse** end (verified, not inherited — 0 of 6 gold states confine, deepest gold well **8.742** `k_BT`
  against a 10 `k_BT` criterion), and `C-0021`'s *"retardation is sourced for gold only"* caveat is
  discharged. `CH-0128` (open) reports that `C-0021` and `C-0023` both call
  `buffer.inverseDebyeLength(lb)` whose **first parameter is a temperature** — 0.714 nm read as 0.714 K — so
  the zero-frequency term is annihilated and the bracket's low end lands exactly on *"fully screened"*, which
  is what the prose declares it to be: the emitted number is right for the stated bracket and the expression
  is wrong, worth 0.93 % at 5 nm. (`C-0021`, `C-0111`.)

### The questions for NDI — specification gaps, not modelling ones

**Six were sent and answered on 2026-08-18; a SEVENTH was raised in iteration 26 and is the first this programme has raised against its own recommendation.** It is item 7 of [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md) and item 12 of [`TASKS.md`](TASKS.md)'s register, recorded in both at the same time — because the last time this set gained a member, the register never learned about it. **AND AN EIGHTH, iteration 36**, which is the first this programme has **no recommendation for at all**: decision 7's answer makes the tile a two-length honeycomb raster, and a two-length raster has **two true widths** — the **112 bp = 38.08 nm** span every one of its rows has and the **116 bp = 39.44 nm** box the folded block occupies. (**RESTATED, iteration 36**: `C-0140` withdrew 112 bp as a *uniform tile width* and `C-0146` restores it as a *row span*, which is a different functional of the same block — the two are not in conflict.) It is item 8 of [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md), item 13 of [`TASKS.md`](TASKS.md)'s register, and `T-242`.

These are the items no calculation closes, because they are choices about the device rather than facts about
it. They are stated as **questions with thresholds**: each names what the answer is worth, so a single
sentence from NDI settles it. The live versions are in [`TASKS.md`](TASKS.md)'s *Open questions for Kazik*
and in [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md), whose numbering this table now follows.
**Two of the six changed identity between iterations 14 and 19 while the count stayed at six**, which is
exactly the way a list like this one drifts without looking as though it has.
`T-95` and `T-102` were **discharged** by `C-0071` and are recorded below the table; the **scaffold**
(`T-154`) and the **two-layer tile** (`T-166`) arrived in their place.
Only the second of those two is a route to anything the programme wants;
the first is a fabrication choice that decides a tile **width**.


> **ALL SIX ARE ANSWERED — 2026-08-18, Jeremy Barton (NDI), by email through Kazik**, in one pass, as this
> file's own framing asked for (*"a single sentence from NDI settles it"* — four of the six took one).
> The answers are in the last column, verbatim in [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md), and recorded
> with what they changed in [`JOURNAL.md`](JOURNAL.md). **Nothing below has been re-derived yet**; the work
> they queue is `T-191`–`T-195`.
>
> **Three things the answers changed that are larger than the answers.**
> **(i) Rows 1 and 2 are ONE decision** — both name the same reserve, so NDI can spend it once, and this
> programme's own numbers rank the layer above the buffer (a clause of §3 against **1.35–1.75×** that is
> common mode below `C-0005`'s 123–214 % — **RESTATED, `CH-0167`/`C-0137`**: a LEVEL, not the error bar on a
> margin; the same-kind threshold is a decay length **9.73 %** shorter).
> **(ii) §3 specifies a ~10 nm tile and every structural claim here modelled a 2 nm one** — the contradiction
> in *"Tile thickness ~10 nm (single-layer honeycomb)"* that `C-0021` and `DnaOrigamiTile.kt` both carry two
> readings of, resolved by NDI toward the thick one.
> **(iii) M13 pays for exactly that tile**: the sheet takes **1 680** of **7 249** nt (`C-0086`), NDI's remedy
> for the excess is more layers, and `C-0006`'s four-layer row is **167×** in `D_∥` and **5.75×** in `D_⊥`.
> **So row (g)'s negative — *flat as designed, unproven as built* — is a result about a tile nobody is asking
> for**, and the axis reopens on the **body**, not on the coupling, where `C-0098` genuinely closed it.
> **MEASURED, iteration 23 (`C-0109`, `T-191`), and the prediction held**: on §3's own ~10 nm tile — four
> honeycomb layers, which one circular M13 pays for exactly (**6 720** of 7 249 nt, 92.7 %) — the free tile
> dishes ~~**0.0577199433**~~ of the stroke under the same solved collar **with no coupling at all**, inside
> `T-5b`'s 0.10 and against the single layer's **0.307902368**. The residual negative is **4.6× narrower** and
> is a statement about the coupling (**0.116465044** at the 90th percentile under the measured dropout, 1.16×
> the convention), not about the tile — and every coupled cell is *worse* than the uncoupled tile, which is
> `CLAUDE.md`'s own *"an attachment coupling can be a NET DISHING SOURCE"* on a body that no longer needs the
> correction.

**RESTATED, iteration 35 (`C-0141`, `C-0142`): every four-layer number in the block above was solved on a
cross-section that is not a honeycomb.** Corrected, the free tile dishes **0.0978155002** on `15 × 4` and
**0.0240648102** on `10 × 6`, the interlayer-coupling threshold moves **inside** the measured 0.26–0.33 band on
`15 × 4` (**0.276970522**, dishing **0.101759944** and failing `T-5b` at the band's adverse end), and the
residual coupling statement is `C-0142`'s: **four** of sixteen graded cells flat at the 90th percentile, all
four on `10 × 6`, best **0.0680677948**. The block is kept as the record of what iteration 23 established.

| | Question | The threshold it turns on | Where | **NDI's answer, 2026-08-18** |
|---|---|---|---|---|
| 1 | **Should Gen-1 be specified at 0.5 mM MgCl₂ rather than §3's 2 mM?** | ~~Six independent routes recommend it, and `C-0032` makes it a **requirement** rather than a preference:~~ (both halves corrected below — **three** routes, and a preference.) The historical reading: at 2 mM the realised strain-softening coupling sits *on* its own fold (bias margin **1.0000–1.0019**) and the fold's stroke walks back through §3's own 3 nm target at two of six layer models; at 0.5 mM every predicate clears (**1.44–5.93×** on stiffness, **1.038–2.327×** on bias) and the fold does not exist. It costs **nothing** — the layer is buffer-independent to ≤ 0.4 % (`C-0007`). **CORRECTED, 2026-08-18: that is a statement about the PHYSICS, and the price NDI names is a fabrication one — origami stability at low salt — on an axis this programme has no column for.** **SETTLED, iteration 17 (`C-0084`, `CH-0098`; `CH-0083` RESOLVED): the word *requirement* was earned on a load line the programme no longer recommends, and it does not transfer.** `C-0032`'s element strain-*softens*; `C-0071`'s recommended `Q5` strain-*stiffens*, with a tangent of **30.03 pN/nm** over the traversed `[0, 3 nm]` that clears the 2 mM **static** stability floors at **6 of 6** models where `C-0030`'s clears none of the 23.41–27.91 pN/nm band. **`Q5`'s pull-in fold has now been searched** (`C-0084`, `T-149`): at 2 mM it does **not fold at all**, at **0 of 6** layer models where the affine mandate folds at 6 of 6 — the bias margin is **1.3877–2.5764** and the fold's stroke moves from 3.4104–4.1248 nm to **past 7.9097 nm**, the binding ceiling changing owner from pull-in to `C-0002`'s `φ = 0.2`. **§6 task 4 is discharged for the recommended device.** The bound is one-sided and says so: the arm is inextensible and only the small-rotation branch is enumerated, so *"no fold"* means *"no fold below 7.9097 nm"*, 2.64× §3's target. **The recommendation to specify 0.5 mM is unchanged** (at 0.5 mM the fold does not exist at all and the bias margin is 1.038–2.327×); what is withdrawn is the claim that 2 mM is *excluded* for the recommended device. **THE COUNT IS CORRECTED, iteration 18 (`C-0091`, `CH-0106`; `CH-0098` RESOLVED): ~~six independent routes~~ → THREE.** `C-0032`'s is withdrawn as above, and **two of the remaining five are the other three, read again** — `T-2`'s `biasClauses[].biasForHundredPiconewtonBlocking` is `T-3`'s own number at **15 of 15** `(height, buffer)` states at a departure of **0.0**, and `T-25`'s `bufferComparison` carries `T-16`'s `stabilityMargin` extrema and `T-4`'s coupled `margin` extrema at **20 of 20** comparisons at **2.66e−8**, which is one file printing eight significant digits where the other prints nine. So `C-0016` is `C-0012` on a `σ` grid and `C-0027` is `C-0017` and `C-0018` corrected. **Of the three survivors, one holds on a different ground and one is quoted at a state the device never occupies**: `C-0018`'s *"0.5 mM removes the fold entirely"* is **void** on an element that has no fold at 2 mM to remove, surviving as a **1.3480×** bias-margin preference (1.8706 against 1.3877); and `C-0012`'s **4.9656×** is a **zero-stroke** blocking-bias ratio — at the held operating point (`L₀ − 3 nm`, 100 pN delivered) the same clause is **1.4823–1.5703×**, an overstatement of **3.16–3.35×**. `C-0017`'s route is untouched, its floor being element-independent (3.8557–15.9409 pN/nm at 0.5 mM against 23.4145–27.9132 at 2 mM, `Q5` clearing 6 of 6 at both), though its **margins** fall 9.9 % on being read at `Q5`'s tangent rather than the mandated secant: 2.0911–8.6452 → **1.8838–7.7882** and 1.1942–1.4236 → **1.0758–1.2825**. **Read at the state the device occupies the three routes are worth 1.35×, 1.57× and 1.75×, and they are not three independent exposures** — all three are downstream of `C-0008`'s single mean-field solve, whose one-loop correction (`C-0005`, 123–214 %) is common mode and larger than each. **The recommendation is unchanged; the count, the word *independent* and the largest number are not.** **RESTATED, iteration 33 (`CH-0167`/`C-0137`): that percentage is an error bar on a LEVEL and these are margins read at a force-pinned point.** The common-mode exposure survives and its same-kind measure is the *gradient* — a decay length **9.73 %** shorter — which nothing evaluable reaches. | `T-63` **AND THE TWO CLAUSES OF `C-0016` ARE TWO QUESTIONS, which no claim had stated** (`C-0097`, `T-158`): its §(e) bias window prefers **0.5 mM** while its §(f) stability count, read at a **fixed applied bias**, prefers **2 mM** (86.08–109.99 pN/nm of coupling demanded at 0.25 V against 47.63–71.54). That is not a contradiction — at a **held** operating point the force balance pins `\|F_es\|`, so the buffer is absorbed into the bias, and at a **fixed bias** nothing is pinned — but a synthesis inherits whichever half its author was reading, so **every buffer statement in this file is the HELD-point reading unless it says otherwise**. | **PRICED, NOT GRANTED.** *"Concerningly below the typical experimental stability window of DNA origami … pushing a parameter hard that I've been reserving for additional operating margin. So… well identified."* **2 mM stays the nominal**; 0.5 mM is a costed option, and **`T-50` is not deleted** — the answer was a price rather than either of the two words this file expected. It is also **the same reserve as row 2** (`T-194`) |
| 2 | **May the polymer layer be taller than §3's 10 nm — 17 to 26 nm?** | The stroke *is* the layer's compression, `s = L₀ − h`, so a 10 nm stroke on a 10 nm layer is the statement `h = 0`, refused **1.02×** kinematically, **1.12×** on `C-0002`'s validity range and **1.35×** under §3's own 100 pN, all **before any coupling exists**. The layer height at which the 100 pN dead-load stroke reaches 10 nm is **16.63–26.12 nm**. §3's own tile row already allows the effort point at *"~20–25 nm above the electrode"*, so the geometry is not absurd — but such a layer is a **different device** and nothing here has evaluated one. ~~**Of the six, this is the only one that can buy §3's desired stroke.**~~ **Withdrawn by measurement, iteration 23 — it buys neither clause.** | `T-115` | **NOT EXAMINED, and behind the same purchase as row 1.** *"An interesting regime we've been reserving, again, for low MgCl₂ concentrations we'd buy with additional work on stabilizing DNA origami at low salt."* ~~The objection given — 17–26 nm is **4.3–6.6** bulk Debye lengths — is one **no claim here has answered**: nothing has evaluated the bias that delivers 100 pN across such a gap (`T-192`)~~ **ANSWERED AND UPHELD, iteration 23** (`C-0110`): §3's 100 pN stops arriving at **13.6989179 nm** at 0.5 mM, *below the bottom* of the band — refused at **96 of 96** states on the acceptable clause and **1 of 96** on the desired one, so a tall layer **loses both devices**. It buys the stroke (52 of 96 uncoupled) and not the force (`CH-0127`), and §3's effort-point row cannot be met at all (32–41 nm, `CH-0126`). **The concession is ours**: counterion dominance is about ion *content*, never *decay*, and diluting makes NDI's own estimate **optimistic**. **So this row is no longer the one that can buy the desired stroke, and no ruling is needed on it** |
| 3 | **What is the electrode made of, and where is its potential of zero charge?** | Metal against oxide is **2.6×** on the van der Waals hold-down, the one term no design can remove — larger than the DNA Hamaker constant, than retardation, and than the polymer in the gap. And zero *applied* bias is not zero *charge*: a contact potential of **0.9–5.1 mV** supplies the entire thermal-scale hold-down by itself. | `P-13` | **TEMPLATE-STRIPPED GOLD**, *"for initial experiments"*. The 2.6× bracket collapses onto its **adverse** end and **no verdict moves**, the finding being about the *shape* of a `1/h³` potential. **The PZC was not given** and is the half that carries the whole zero-bias hold-down (`T-193`) |
| 4 | **Which device does §3's *desired* clause ask for — a 33.3 pN/nm coupling travelling 10 nm, or a 10 pN/nm one?** | Every claim in the corpus reads the desired stroke on the coupling placed for the *acceptable* one, which at full stroke must deliver **333 pN**. The desired clause's own coupling is `100 pN / 10 nm = 10 pN/nm`, a **different device** — which `C-0046` can build (arms 11.4–18.1 nm, **12 of 29** points clearing every element clause) and `C-0017`'s stability floor **refuses at 2.34–2.79×**. Composing the two clauses caps the stroke at **3.58–4.27 nm at §3's 100 pN**, whatever the coupling is made of. | `T-112` | **TWO DEVICES.** So `δ ≤ F/\|k_eff\|` = 3.58–4.27 nm bounds **device A**, and device B is the 10 pN/nm placement — refused by `C-0017`'s floor at **2.34–2.79×** *at the 10 nm layer in 2 mM*, ~~which rows 1 and 2 together say device B need not occupy (`T-192`)~~ **and the corner rows 1 and 2 pointed at has now been evaluated and does not contain it** (`C-0110`, iteration 23): over 384 solved states at 17–26 nm in 0.5 and 2 mM, device B is admitted at **1 of 96**, in **1 of 6** layer models — a bracket disagreement rather than a design — and device A at **0 of 96**. **So the escape rows 1 and 2 offered device B is closed, and device B has no state in this programme where it is admitted on more than one layer model** |
| 5 | **What is the scaffold — linear or circular, M13 or synthetic, and how long?** | **A seam is a PARITY ON A TREE rather than a fabrication convention** (`C-0086`): crossovers join only *adjacent* duplexes, so a single-layer sheet's row-adjacency graph is a **path** — a tree — and a closed walk on a tree traverses every edge an **even** number of times, so a **fully folded circular** scaffold gives every row two segments, i.e. exactly one seam. Brute-forced, the path graph carries **2 Hamiltonian paths and ZERO Hamiltonian cycles** at every width from 3 to 12 duplexes. **A seam is not free**: it does not perturb the weave, it **deletes an extremum**, putting **6–12 of `C-0063`'s 34** arm stations off the node at every one of the 8 seam positions a 40 nm tile admits and restoring `CH-0088`'s annihilated **1.2–1.75 nm** amplitude bracket at full strength — worst across-row clearance **−0.0023 nm**, a clash (`C-0081`). **The Gen-1 sheet takes only 1 680 of M13's 7 249 nt**, so it is not fully folded and the seamless boustrophedon is available; each of the three specifications carries a Rothemund precedent read directly — **linear** (0 seams, but BsrBI linearisation dropped his own yield **63 % → 11 %**), **circular fully folded** (1 seam), **circular with remainder** (0 seams, but a **5 569 nt, 33.3 nm** unpaired coil carrying **1.66×** the sheet's own charge in the actuated gap). **And a seamless raster quantises the tile WIDTH at 32 bp** — 16, 48, 80, 112 or 144 bp, and §3's **40.0 nm = 117.6 bp is not among them** (`CH-0101`); see §1. **ANSWERED AND CLOSED, iterations 25–27.** The route: the honeycomb's scaffold-crossover lattice is `7k ± 5` bp and **integral** — `C-0086`'s odd-half-turn rule fails there as a **domain error**, not a prohibition — ~~and a **seam is still forced**, because Figure 2b of the caDNAno paper states *"the path of the scaffold stays within a 2D surface"*, so the graph the scaffold may use is a path even though the honeycomb's is three-regular (`C-0119`).~~ **WITHDRAWN, iteration 40** ([`C-0168`](gpd/claims/C-0168-recommended-block-seam.md), [`CH-0212`](gpd/challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md)): **a seam is AVAILABLE and is not forced**, because the tree parity needs a **fully folded** circular scaffold as well as a path and the recommended block leaves **919 nt** spare — its two raster termini sit **35.504 nm = 14 d** apart, both at offset 7 and on the same face, and the circle closes at **1.03–2.69 `k_BT`** across the whole ssDNA Kuhn bracket against the **8.0 `k_BT`** a crossover column costs. The committed artifact — **60 domains on 60 helices**, a Hamiltonian path — is right as drawn, both studies that grade the block are inside `C-0161`'s alternating family and **no number moves**, and the counterfactual is measured anyway at **1.15701888×**, both flat. Every derivation of `C-0119` §4 is upheld and reproduces at departure `0.0`; and the path premise is false too and does not matter — the block's own adjacency carries **77 edges on 60 helices** and its two termini are **degree one**, so no honeycomb block of this family admits a Hamiltonian cycle at all. The remainder: **840 nt**, because the `10 × 6` cross-section is folded from **p7560** and not M13mp18's 7 249 (`CH-0147`) — and **the four-layer tile had already paid for it before the question was asked**: the gap-facing wall sits at **0.966331968** of the 2:1 saturated amplitude, so smearing the *entire* remainder onto it moves `σ_eff` by **0.0350791486** against **0.537733246** on the single-layer sheet, **15.3291419×** less and inside `C-0008`'s own charge-reading ambiguity (`C-0125`). At most **0.15045831** of the chain can thread the gap, bounded over *every* placement so it needs no attachment point §3 never fixes. `CH-0148`, open: `C-0086` compared a single strand with a duplex on **bare** charge, and ssDNA retains 0.399–0.490 under Mg²⁺ against a duplex's 0.119, so its published *"1.66×"* is **5.56–6.82×** in effective charge — an exposure it named and did not price. | `T-154` | **CIRCULAR M13, ~7–8 k nt**, 50 k available above that — so **circular-with-remainder is the default** and the purpose-length recommendation is declined (`T-195`). And the remedy for the excess is *"just make the tile thicker. The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness"* (`T-191`) |
| 6 | **Does §3 admit a TWO-LAYER tile, or is single-layer a requirement?** | The flat Gen-1 tile is a **zero-defect** result (row (g) below), and the one structural escape is a coupling that is **not an array**: tying the tile to a **stiff shared body** moves `C-0017`'s mandate into the body's **ground** — a rigid-body mode of the tile, invisible to dishing — freeing each tie from 0.98 to **3.33 pN/nm**, and it is **2.05× flatter** than the array at zero defects on identical stations (**0.0344013403** against 0.0706145537), reaching **0.24028028** at the 90th percentile under the measured dropout — the lowest this programme has attained, and still **2.40×** `T-5b`'s 0.10, needing **252 ties against 53** (`C-0093`). `CH-0114`, unresolved, notes that `C-0093` states its per-tie ceiling at **3.33333333 pN/nm** and runs every graded cell **3× to 300×** past it, because `C-0049`'s `a/s` is derived on an array path whose extension **is** the stroke and a shared body's tie extension is not — two defensible readings, and not the same one. **But a body tied at many out-of-plane sites IS square-lattice multilayer origami** — the one motif in this neighbourhood with a published precedent, against the single-tie out-of-plane arm the recommendation rests on, which has none in 62 recorded queries. **The last unspent axis has since been spent and it runs the wrong way** (`C-0098`): searched for placement *and* distribution on the crossover sites the lattice actually supplies, the best of 25 graded cells is **0.375506727**, **1.56× WORSE**, because 0.24028028 lives on an abstract 90-station grid where the lattice offers at most 60 (`CH-0113`). And a *buildable* body loses where the rigid limit wins — a four-layer honeycomb brick reads **0.100166871**, worse than the array — so body rigidity is first order rather than an idealisation. **A *no* would have closed the last recovery route for a flat tile.** | `T-166` | **YES, BY IMPLICATION** — answered inside row 5 and **volunteered rather than granted**. The question asked whether a second **body** may be tied to the tile; what was answered is that the **tile** may be thicker, which is the stronger permission: the shared body **fused** to the tile rather than tied to it. **`T-191` HAS RUN AND IT SETTLES THE ROW** (`C-0109`, iteration 23): on a four-layer, ~10 nm tile at the buildable 38.08 nm the flatness verdict is ~~**0.0577199433 of the stroke with NO coupling at all**~~ **0.0978155002 on `15 × 4` and 0.0240648102 on `10 × 6` — RESTATED, iteration 35 (`C-0141`)** — inside `T-5b`'s 0.10 against the single layer's 0.307902368 — so the recovery route this row was protecting is not a coupling topology but the **body**, and one circular M13 pays for exactly four layers (**6 720** of 7 249 nt). A *no* would have closed it; the *yes* removes the need for the coupling rather than improving it. **AND THE ANSWER HAS OUTGROWN THE QUESTION** (iteration 25): the thicker tile is a **published, folded, gel-verified cross-section** (`C-0119`), its own source recommends a **different** one that is ~~6.6×~~ flatter with **no** coupling-fraction threshold at all (`C-0120`), and on that one **a COUPLED tile is flat under the measured folding statistics** — ~~9 of 16 cells, all eight on `10 × 6`~~ (`C-0118`). ~~What remains is not a ruling but a **census**: the honeycomb's attachment lattice has never been counted~~ **RESTATED, iterations 33–35** (`C-0141`, `C-0142`, `CH-0174`, `CH-0176`): the cross-section those numbers were solved on is not a honeycomb, so the free tile dishes **0.0978155002** on `15 × 4` and **0.0240648102** on `10 × 6`, and the coupled evidence is **four** of sixteen cells rather than nine — **all four on `10 × 6`**, `15 × 4` **0 of 8 at both ends of the measured band**, best **0.0680677948**. **And the census has been taken**: the top face supplies **90** stations on `15 × 4` and **60** on `10 × 6` at one **30°** azimuth per helix, with a plan ceiling and a centro-symmetric placement family, so what remains is neither a ruling nor a census but ~~the **row length**~~ **a width CONVENTION** and the **scaffold** (`C-0140`, `CH-0180`). **RESTATED, iteration 36** (`C-0146`, `C-0147`): the row length **is** derived — every x-raster row spans **112 bp = 38.08 nm** and the 116 bp = **39.44 nm** extent is a **1.36 nm inter-row stagger** — so what is owed is which of the two §3 names (decision **8**, `T-242`), and the 4 bp ragged face that follows costs this row's flatness **exactly zero**, being on the tile's **rim** |
| **7** | ~~**Is a 38 × 25 nm tile acceptable, in exchange for removing the last unmeasured dependency in the flatness verdict?**~~ **SUPERSEDED AS POSED, iteration 35** — **RE-POSED: *confirm `10 × 6`, and name the scaffold*** | ~~The tile that follows from NDI's own answers is `15 × 4` — 38.08 × 38.04 nm, essentially §3's square, and **design (i) of the caDNAno paper**, folded and gel-verified. That paper's own folding measurements recommend **`10 × 6`**: the same 60 helices at **38.08 × 25.36 nm**, which is **6.6× flatter**, flat at **8 of 8** coupled cells rather than 1 of 8, and — the part that matters — has **no coupling-fraction threshold at all**, so it *removes* the interlayer-coupling calibration from the verdict rather than clearing it by 3.3×. It costs **a third of the footprint**, over which §3's 100 pN is specified.~~ `C-0123` shows the collar transfer is not the obstruction. ~~**Both criteria point the same way and the cost is a specification, so this programme can rank what the two tiles BUY and not what the smaller one COSTS.**~~ **THE TRADE DOES NOT EXIST AND THE QUESTION WAS POSED THE WRONG WAY ROUND** (`C-0144` §4, on `C-0141` and `C-0142`): corrected to the honeycomb's own two pitches the tile this question charges a footprint for — `10 × 6` — is **0.929467162 of §3's 40.35 nm**, essentially §3's own square, while the default it offers — `15 × 4` — is **1.40084263×** it **and is no longer flat**, dishing **0.101759944** at the measured band's adverse end and standing at **0 of 8** coupled cells at both ends of it. All three criteria this programme can rank — folding yield, flatness, footprint — now point the same way, so what NDI is owed is a **correction and not a trade**. What is re-posed in its place is a **confirmation** of `10 × 6` and one genuinely new specification question: **which scaffold?** — the caDNAno paper's Methods list says p8064 and its own main-text rule says p7560, agreeing at six of its seven designs and disagreeing at exactly ours (`CH-0180`) | `T-199` | ~~**RAISED, iteration 26 — awaiting**~~ **RAISED iteration 26; superseded as posed and re-posed iteration 35; awaiting** |
| **8** | **NEW — which width is the Gen-1 tile SPECIFIED to: the ~~112 bp = 38.08 nm~~ `109 bp` span every one of its rows has, or the 116 bp = 39.44 nm box the folded block occupies?** | ~~The two readings are **3.57 %** apart and the difference is a **`4 bp = 1.36 nm` inter-row STAGGER**, not a length:~~ **RESTATED, iteration 38 — the difference is the `7 bp = 2.38 nm` stagger of the DRAWABLE `102 / 109` raster (`C-0151`), and it is still a stagger and not a length:** every x-raster row of the block spans the **larger** of its two lengths **exactly**, at all ten rows and at every one of `C-0140`'s five candidate pairs, and the block exceeds it by exactly the stagger (`C-0146` §1). **No plate or grillage model in this repository has a per-row row length**, so the stagger is not representable at all and nothing here can break the tie. ~~It is **not cosmetic**: a 116 bp box clears eleven honeycomb crossover pitches by **0.07 nm** — one fifth of a base-pair rise — so `CrossoverLayout.EDGE_MARGIN` admits a **twelfth** crossover column at its standing 0.05 nm and refuses it at half a rise, and that column alone is **six flat coupled cells of eight against three** at the 90th percentile under the measured dropout ([`CH-0185`](gpd/challenges/CH-0185-a-bounding-box-crossover-column.md)); the row-length reading gives **four**. **No recommendation turns on the answer and a margin does** — the recommended cell (one column, ten paths, equal springs on `10 × 6`) is flat at **0.0662801686** on twelve columns, **0.0708759349** on eleven, and **0.0708859619** / **0.0754995025** at the measured band's adverse end.~~ **WITHDRAWN, iteration 38** ([`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md), [`C-0151`](gpd/claims/C-0151-closing-raster-selection.md) §4): **that threshold was read at a raster that cannot be drawn.** The twelfth column belongs to the bounding box of `112 / 108` and the eleventh to its rows, and `112 / 108` does not close on caDNAno's `±5 bp` rule; at the drawable `102 / 109` the interface window is **102 bp** and the count is **10** at all three `EDGE_MARGIN` conventions — 0.05, 0.17 and 0.34 nm, slack **2.45 / 2.21 / 1.87 nm** — so **the guard is inert and NO flatness cell turns on the width convention at all.** The recommended cell is flat at the drawable raster, **0.0773373597** at `f` = 0.30 and **0.0821458169** at `f` = 0.26. **So this is now a drafting question with no margin on either side of it**, which is a simplification and not a withdrawal: §3 still states one number and the object still has two. **This programme states no preference**, because both readings are true of the same object; if pressed it would ask for **both numbers with a stated stagger**, which is a drafting preference and not a result. **The price line is left explicitly open**: *and if this costs something — in metrology, in bench acceptance, or in a drawing this programme cannot see — what?* | `T-242` | **RAISED, iteration 36 — awaiting** |

**And two questions that were rows 3 and 4 of this table have been DISCHARGED.**
Recorded rather than deleted, because a list that only ever grows is not a record:
`C-0071` found both had stopped applying and nobody had noticed, and `P-24` found the queue had not
recorded it either.

| | Question | The threshold it turned on, and why it stopped applying | Where |
|---|---|---|---|
| ~~3~~ | **May the output superstructure be perforated under each flexure midspan?** | The only buildable mounting needs the midspan tie to cross the plane its own standoffs stand on, so **45 duplex-omission holes = 326 nm², 20.4 % of the tile footprint**, at every stroke — that is the floor and it is not optional. An **imperforate** superstructure caps the stroke at `ℓ − 2.69 nm`, i.e. **5.31 nm** at the recommended 8 nm standoff: §3's acceptable clause and nothing more. §3's *desired* 10 nm stroke needs each hole widened into an **18.37 × 2.69 nm slot — 2223 nm², 1.39× the whole tile footprint**. **DISCHARGED for the recommended element (`C-0071`), live only for the flexure branch.** This question was raised by the flexure-and-tie branch that `CH-0081`/`C-0069` removed from the output role: the recommended hinge-rooted arm has no flexure, no midspan, no standoff base plane, and places all 34 instances at one level on the Gen-1 footprint with **no tie grid at all**. It re-binds if that branch returns, or if §3's 45 paths are reinstated. | `T-95` |
| ~~4~~ | **May §3's tile grow by 1.44× in area, to 2330 nm²?** | On the specified 40 × 40 nm tile the flexure path count is bounded **below at 29** by the unzip allowable at the desired stroke and **above at 15** by the plan-view packing, so the window is **empty**. The threshold is **≥ 2330 nm², 1.20× in edge**. The price runs the favourable way: `C-0022` finds a larger tile costs **+6.3 %** at the rim instead of +14.7 %, the collar being a fixed 1.65 nm. **DISCHARGED for the recommended element (`C-0071`), live only for the flexure branch.** This question was raised by the flexure-and-tie branch that `CH-0081`/`C-0069` removed from the output role: the recommended hinge-rooted arm has no flexure, no midspan, no standoff base plane, and places all 34 instances at one level on the Gen-1 footprint with **no tie grid at all**. It re-binds if that branch returns, or if §3's 45 paths are reinstated. | `T-102` |

---

## 6. Reading order

For the process, [`JOURNAL.md`](JOURNAL.md) — decisions and surprises in order, including the ones that
reversed earlier conclusions.
For the live state, [`TASKS.md`](TASKS.md).
For any number in this file, the claim it belongs to in [`gpd/claims/`](gpd/claims/), and the challenge
standing against it in [`gpd/challenges/`](gpd/challenges/).
**And, since iteration 39, for the tile itself: [`gpd/designs/`](gpd/designs/)** —
the recommended `10 × 6` block and this corpus's 15 × 112 bp sheet as scadnano `.sc` files, each of which the
reference implementation (0.21.1) loads with zero warnings ([`C-0160`](gpd/claims/C-0160-scadnano-writer.md)).
A `.sc` file is the one artifact here a reader can open in somebody else's software rather than read about;
neither carries a staple set, so it is a lattice artifact and not an order.
