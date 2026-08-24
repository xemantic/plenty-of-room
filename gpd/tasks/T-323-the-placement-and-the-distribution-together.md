# T-323 — The placement and the distribution searched TOGETHER, and the INTERACTION nobody has measured

**Leaf:** `A8.2`
**Raised by:** [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) (`T-316`) §14 and §15,
whose first *Still open* item is *"the placement and the distribution searched TOGETHER"*,
and by [`C-0063`](../claims/C-0063-upward-root-placement.md)'s own *Still open* item 3 —
*"joint optimisation over placement **and** distribution has not been run; each was optimised with the other fixed."*
**Reserved claim:** `C-0216`. **Reserved challenges:** `CH-0278`, `CH-0279`. **Reserved queue rows:** `T-328`, `T-329`.

---

## Formulate

### The standing state

This corpus has **two** claims about these two design variables and **no** measurement of their interaction.

`C-0063` searched the **placement** with the distribution fixed, on the square lattice:
1 144 858 placements took the dishing from `0.4156` to `0.0706` of the stroke — **5.9×** — with **equal springs**,
and adding `C-0058`'s rim rule to the winner made it *worse at every ratio*.
Its sentence, which `CLAUDE.md` carries, is
*"which stations a coupling enters at is worth more than how its stiffness is distributed."*

`C-0212` searched the **distribution** with the placement fixed, on this honeycomb lattice at `C-0208`'s resolved
per-bond link: `22 of 32` cells flat at the 90th percentile against `0 of 32` on either transferred rule,
tightest `0.0647024141`, out-of-sample gain `1.10434917`–`1.70065256×`.

Neither has moved the other variable.
And `CLAUDE.md` already records that the ordering question is **half-answered in the adverse direction** —
*"selecting a placement on the EQUAL-SPRING objective is selecting on the wrong quantity once a distribution is
free"* (`C-0072`: the *worse* equal-spring placement was the *better* minimax one).
That is a **warning inherited without a number**; nothing on this lattice measures it.

What this task inherits from that sentence is the *hypothesis*, not a verdict:
it says a placement ranked on a transferred rule may not be the placement a searched distribution wants,
which is precisely the statement a **rank correlation** and a **regret** measure, and neither exists here.

### The question

1. Does a **joint** search over the station set and the per-path stiffness vector reach a 90th-percentile
   dishing that **neither** search reaches alone — and by how much, out of sample?
2. What is the **interaction** of the two factors, in **both** orderings?
   `CLAUDE.md` demands of any two-factor move *"a TOTAL and an INTERACTION, never an X term and a Y term"*,
   records that the split is order-dependent and that on the one prior occasion the **interaction carried more
   of the variation than a main effect did** ([`C-0108`](../claims/C-0108-count-phase-interaction.md): interaction 9.79 % against a phase main effect
   of 7.84 %). A joint search reporting only *"joint beats each alone"* has not answered this row.
3. Does `C-0063`'s **ordering** — placement worth more than distribution — hold on this lattice?
4. Is the joint optimum **admissible**, on every threshold the moving quantities feed, and does it beat the
   **uncoupled** tile — which `CH-0272` records `0 of 32` searched cells do at the 90th percentile?

### What is fixed and what moves

**Fixed**, and inherited unchanged from `C-0167`/`C-0180`/`C-0205`/`C-0208`/`C-0212`:
the `10 × 6` cross-section, the `116 bp` block extent, the drawable `102 / 109` raster,
the 435 staple bonds and 59 raster turn ties, `C-0022`'s solved collar,
`C-0017`'s mandate at the acceptable clause, `C-0087`'s measured depth incorporation,
the resolved per-bond link at `C-0208`'s radial bracket **floor**,
the `21 bp` station ladder at the phase `C-0148`'s `±5 bp` rule **determines** (16) and the forced `14 bp`
inter-row offset (`C-0141`),
grading seed `197197` and 4 000 realisations, the `81 × 81` dishing grid, `T-5b`'s `0.10`.

**Moves**: two things, and for the first time together.

- **The placement** — which stations of the face's own ladder the coupling enters at, at a fixed path count.
  The family is `C-0141`'s **determined station lattice on the rooting helices**: one rooting helix per
  x-raster row, `columns` distinct ladder stations chosen on each. `C-0167`'s
  *"determined station lattice on the rooting helices"* is a **member** of it, which is what makes the baseline
  a corner of the search rather than a comparand outside it.
- **The per-path stiffness vector**, subject to `C-0017`'s mandate on the **sum**, exactly as `T-316`.

The three *unrealisable* members of `C-0167`'s four placements — the abstract grid, the abstract grid on the
rooting helices, and the determined lattice off the helices — are **graded and not searched**.
`CLAUDE.md`: *"realising a placement on a lattice can invalidate the distribution rule that was optimised on the
abstract one"*, and a search over positions the lattice does not supply is a search over a design nobody can draw.

### Numeric targets

| # | target |
|---|---|
| `P1` | the **cheap bound**, before any solve: the determined ladder's per-row station counts and total, the exact family size `Π_r C(\|L_r\|, c)` at every column count, which cell is **exhaustible**, and whether the family admits a **centro-symmetric** member at all — all asserted against the lattice object rather than transcribed |
| `P2` | `(P₁, D₀)` — the placement searched with the distribution held at the **best transferred rule**, taken **EXHAUSTIVELY** at 50 paths, with the placement axis's whole distribution (best / median / worst) so the reader can see whether placement is a design or a lottery |
| `P3` | `(P₁, D₁)` — the **joint** corner: the distribution searched at the screened placements, graded **out of sample** on the `197197` stream neither search ever sees |
| `P4` | the **2 × 2 in both orderings** and the **INTERACTION**, with the path disagreement asserted at the floating-point floor; plus the `5 × 3` two-way log fit over `C-0167`'s four fixed placements **and** the searched one against equal / rim-graded / searched, reporting `interactionShare` and `worstResidualPerCent` |
| `P5` | **`C-0063`'s ordering, measured on this lattice** — the placement main effect against the distribution main effect — **and the inherited sentence measured**: the Spearman rank correlation of the transferred-rule placement ranking against the searched ranking over the searched set, and the **regret** of having selected the placement on the transferred objective |
| `P6` | every threshold the joint optimum feeds, **and their conjunction**, which `CH-0272` records no verdict block here has ever stated: `T-5b`'s `0.10`; `C-0023`'s `3.33333333 pN/nm` per-path allowable; `C-0060`'s **FLAT** ratio window `[3.5, 20]` — named as `CH-0273` requires and read on the **two-level projection**, the object `C-0060` measured it on; the **uncoupled** tile at the 90th percentile **and** at zero defects; the worst **single-path removal**; and the in-sample / out-of-sample gap |
| `P7` | the **descent's slack, measured where the truth is known** — the per-row placement descent run at the exhaustible 50-path cell against its own exhaustive optimum, which is what licenses using that descent at 10, 20 and 30 paths where the family cannot be enumerated |
| `P8` | the same 2 × 2 at `C-0116`'s second composite fraction `f = 0.26` — run if the smoke pass's measured solve rate admits it inside the declared budget, and emitted as an explicit `null` **with the measured reason and the dropped-work census** if it does not |

### Acceptance predicate

The task passes when `P1`–`P7` are discharged, `P8` is discharged **or** explicitly declined with its measured
reason logged in the result file, and the claim states plainly:

- whether a joint search reaches a 90th-percentile dishing neither search reaches alone, and by how much
  out of sample, **paired per realisation** and not as a ratio of two order statistics;
- what the interaction is, in both orderings, and whether it is resolvable against the study's own
  convergence departure on the same quantity;
- whether `C-0063`'s ordering holds here;
- whether the joint optimum is admissible on **every** threshold and against the uncoupled tile.

**A negative is as much a result as a positive**, and it is the stronger one.
`CH-0272` establishes that `0 of 32` cells with a **searched distribution** beat the uncoupled tile at the 90th
percentile. If a joint optimum — both design variables free — still loses to no coupling at all, then on this
lattice, under `C-0087`'s measured dropout, **flatness is not what the coupling buys, and that survives the
freeing of both design variables**, which removes the last *"but you never optimised it"* objection the corpus
can raise against its own census. It would **not** be an argument for removing the coupling: `C-0017`'s mandate
is a **placement and stability** requirement, and the stroke and the lateral confinement want ties for reasons
that are not flatness. If instead the joint optimum **does** beat the uncoupled tile, `CH-0272`'s reading is
scoped to a fixed placement and the coupling buys flatness once the placement is free — a reversal, and equally
a result.

### Units and conventions — locked before deriving

- nm, pN, pN/nm, pN·nm, pN·nm/rad, pN/nm² (= 1 MPa). `k_BT = 4.141947 pN·nm` at `T = 300 K`, aqueous 2 mM MgCl₂.
- `W` positive **downward**, toward the electrode (`C-0006`); a coupling's support force is upward and enters as
  its negative.
- `s` along the helices, `y` across them in the face plane (row pitch `3d/2`), `z` through the thickness
  (layer pitch `d√3/2`). Honeycomb `d = 2.536 nm`; rise `0.34 nm/bp`; `k_θ = 13.5294118 pN·nm/rad` at `α = 1`.
- The link is resolved per bond, `k_link = k_radial·unitZ² + k_transverse·unitY²` (`C-0208`), with
  `k_transverse` pinned at `C-0205`'s ceiling `254.80809548301096 pN/nm` and `k_radial` at `C-0208`'s bracket
  **floor** `754.005141 pN/nm`, giving a through-thickness link of `629.20588`.
- `C-0017`'s mandate is an **equality on the sum**, `MANDATED_TOTAL_STIFFNESS = 100/3 pN/nm`.
- **Dishing** is `|w − affine fit|` peaked over the `81 × 81` face grid and divided by the free-tile stroke,
  exactly as `C-0167`, `C-0208` and `C-0212` read it. The search's own sample grid is `41`.
- **A placement** is, for each of the 10 x-raster rows, a set of `columns` **distinct** stations of that row's
  own `21 bp` ladder, at the row's rooting-helix `y`. Its **key** — the tie-break — is the tuple of station
  indices in row-major order, ascending within a row; the enumeration order is lexicographic on that key and
  the **earlier** candidate wins every tie.
- **Ensembles.** Grading: seed `197197`, 4 000 realisations (`C-0208`'s own, so its published cells reproduce).
  Training: seed `316316`, 120 realisations (`T-316`'s own, so its published cells reproduce).
  **Screening: seed `323323`, 40 realisations** — new, and disjoint in seed from both, because the exhaustive
  placement pass selects a *tightest of 7 776* and must not do so on the stream either later stage is read on.
  `DropoutRandom` draws one deviate per station per realisation in grid order, so at a fixed path count every
  placement consumes the **same uniform stream** and every placement comparison is **paired** by construction.
- **Every difference between two designs is reported per realisation as well as between two summaries** —
  `CLAUDE.md`'s *a ratio of two ORDER STATISTICS is not the order statistic of the ratio*, which `C-0212`'s own
  headline had to correct for.
- A stiffness **ratio** is `max/min` over the per-path vector. `C-0060`'s `[3.5, 20]` is its **FLAT** ratio
  window, measured on `C-0058`'s square-lattice 45-station design; the word *buildable* is not used of it
  (`CH-0273`), and it is read on the **two-level projection**, which is the object `C-0060` measured.
- **Every search decision and every tie-break** goes through `searchDecision` at six significant digits
  (`C-0135`, `C-0177`). No emitted field counts a step, an evaluation or a second.

---

## Plan

### The cheap bound runs first, it needs no solve, and it decides the whole method

The placement family is a **product of row option sets**, so its size is one product and its symmetry is one
set intersection. Both are arithmetic on `TwoLengthRaster.stationLattice(16, 14)` and neither touches a solver.

**Bound 1 — the family is a product, and one cell of it is EXHAUSTIBLE.**
The determined ladder at the phase `C-0148` fixes carries **55** stations on the ten rooting helices,
alternating **5** on the even rows (the `102 bp` sense, window `[−102, 0]`) and **6** on the odd ones
(the `109 bp` sense, window `[−102, +7]`) — the `7 bp` stagger showing up as a station.
The family size is `Π_r C(|L_r|, c)`:

| columns | paths | family size |
|---|---|---|
| 1 | 10 | 24 300 000 |
| 2 | 20 | 75 937 500 000 |
| 3 | 30 | 320 000 000 000 |
| **5** | **50** | **7 776** |

At **five** columns the five-station rows are **forced** (`C(5,5) = 1`) and each six-station row has
`C(6,5) = 6` choices, so the whole family is `6⁵ = 7 776` and can be **enumerated exhaustively**.
That is decisive: `C-0102` records that *a descent compared against an exhaustive enumeration is not a
comparison*, `CH-0119` that *a placement-searched family measures the search rather than the axis*, and
`C-0063` found its own answer by an **exhaustive enumeration beating its own descent**. Here the deciding
cell — 50 paths, which is where `C-0208`'s tightest cell and `C-0212`'s tightest searched cell both live —
needs no descent at all. The other three column counts do, and `P7` calibrates that descent **at the cell
where the truth is known**.

**Bound 2 — the family admits NO centro-symmetric member, at any of its 42 lattice readings.**
`C-0063`'s entire search strategy was a centro-symmetry congruence: two of 32 square-lattice phases admit a
symmetric placement, the winner was at one of them, and the exhaustive symmetric enumeration supplied the
answer. Here row `r` maps to row `9 − r` under `(s, y) → (−s, −y)`, and those two rows carry **opposite**
window parities, so a station at `s` on an even row needs `−s` on an odd row's ladder.
Over all ten row pairs at the determined `(offset 14, phase 16)` that intersection is **empty at every pair** —
not small, empty — and swept over all `21` phases at both `7 bp` and `14 bp` inter-row offsets only **2 of the
42** readings admit even a **single** centro-symmetric station pair, neither of them the determined one.
So the honeycomb's forced row stagger **destroys the symmetry the square lattice's answer was built on**,
and the shortcut is unavailable — which is exactly why bound 1 matters. Both bounds are asserted against the
lattice object as tests, never transcribed.

**Bound 3 — the spread the corpus has already measured on each axis, separately.**
`C-0063`: the placement axis is worth **5.9×** on the square lattice and the distribution axis **13.9 %** on
its winner. `C-0212`: the distribution axis is worth **`1.10434917`–`1.70065256×`** on this one.
So the standing expectation is placement ≫ distribution, and `F5` is declared on the reverse.
One division on two committed files, and it says which factor the sweep must resolve best.

**And the bank is free — this is why a joint search is an outer loop and not a new problem.**
An influence bank is a property of the **structure**; a distribution enters the Woodbury system as a
**diagonal**, and a *placement* is a **slice of the bank's index set**. So **one** bank of 55 unit-point-load
solves per `(fraction, rung)` serves **every** placement and **every** distribution ever tried at that cell.
`C-0063` built exactly this (`UpwardRootInfluenceBank.surrogateFor(indices)`) on the square lattice and asserted
the slice against a bank built on the placement alone at `1e−12`; the honeycomb twin is the same object over
`influenceSurrogate(...)`, which `HoneycombTiedRegrade` already factors out. **The joint search is therefore an
outer loop over placements around a search that is already written, already tested and already reproducible**
(`tile/SearchedDistribution.kt`, `T-316`), and the per-placement cost is one `n × n` Cholesky per realisation
rather than a 4 320-degree-of-freedom lattice factorisation. If a cost estimate for this task does not exploit
that, the plan is wrong.

### The search, in three tiers, and every cap declared

**Tier 1a — the census.** All **7 776** placements at 50 paths, at **each** transferred rule, on the
**screening** ensemble. This is `(P₁, D₀)` **exactly** — an optimum over the family, not a descent — and it
delivers the placement axis's whole distribution.

**Tier 1b — the screens' own quality, on a sample rather than a census.**
A third, **distribution-free** screen — `InfluenceSurrogate.reachableDishingFloorAt`, the oracle floor, which
is a pointwise lower bound over *every* distribution whatever — is run on a **deterministic sample** of 400
placements (every 19th of the enumeration order, so it is a function of the family and of no seed). A ranking
quality is a **correlation** and a correlation needs a sample, not a census; running the floor over all 7 776
would cost as much as tier 1a and buy nothing the sample does not.

**Tier 2 — the coarse joint search.** The top **8** placements of each screen (union ≤ 17 after dedup) get a
**one-sweep** percentile descent on **60** training realisations. This is a ranking pass, not an answer, and it
is declared as such.

**Tier 3 — the finalists.** The best **3** of tier 2 **plus `C-0167`'s own member** get the full
`T-316` composition — `C-0135`'s smoothed minimax on the zero-defect peak, then a two-sweep multi-start
percentile descent on the true training percentile at **120** realisations, seeded from both transferred rules
*and* from the smoothed answer — and are then graded on the **4 000**-realisation `197197` stream.
Including `C-0167`'s member unconditionally is what makes `(P₀, D₁)` a reproduction of `T-316` rather than a
re-derivation, and it makes `F12` a property of the composition.

**Nothing above is a silent cap.** The result file emits the family size, how many members were evaluated, the
screening seed and realisation count, `K` per screen, the union size, **the rank of the joint winner inside each
screen** (so a binding screen is visible rather than inferred), and an explicit `dropped` census naming every
combination not run and why.

**The other column counts** — 10, 20 and 30 paths — cannot be enumerated (`P1`), so they take a deterministic
**per-row coordinate descent**: hold every row but one, enumerate that row's `C(|L_r|, c)` options
exhaustively, accept on the `searchDecision`-rounded objective with the placement key breaking ties, sweep until
no row moves; multi-start from `C-0167`'s member, a centred placement, an edge-loaded placement and two
deterministic spreads. `P7` runs the identical descent at **50** paths, where the exhaustive optimum is known,
and reports its slack — so the descent at the other counts is quoted with a measured error bar rather than as
an answer.

### The interaction, and why the corner must be one number

The 2 × 2's factors are **freedoms**, not values, and that is forced rather than chosen: a distribution is a
vector **indexed by station**, so it does not transfer between placements at all. What transfers is a
*procedure* — *"a rule the corpus already grades on"* against *"searched"* — and each corner is then
*the best objective this study finds under that combination of freedoms*:

| | `D₀` = the best transferred rule | `D₁` = searched |
|---|---|---|
| **`P₀`** = `C-0167`'s determined lattice on the rooting helices | `0.106508519` — the corpus's own number | `0.078544978` — `C-0212`'s |
| **`P₁`** = searched over the determined family | **new**, and **exhaustive** at 50 paths | **new** — the joint corner |

The two **orderings** are then two ways of *attributing* one total, and they share their endpoints, so the total
is identical and the two splits **differ by exactly the interaction**:

- placement first: `ln(P₁D₀/P₀D₀)`, then `ln(P₁D₁/P₁D₀)`;
- distribution first: `ln(P₀D₁/P₀D₀)`, then `ln(P₁D₁/P₀D₁)`;
- `interaction = ln(P₁D₁/P₀D₁) − ln(P₁D₀/P₀D₀)`, identically the difference of the two distribution terms.

This is `coupling/CountPhaseInteraction.kt`'s `countPhaseSplit`, **reused unchanged** — its arithmetic is
generic in the two factors and only its field names are `count`/`phase`, so the mapping
`count ↔ placement`, `phase ↔ distribution` is emitted as an explicit table in the result file and the
arithmetic is **not written twice** (`CLAUDE.md`: *a duplicated rule is invisible to a mutation test of either
copy*). Its `pathDisagreement` is an arithmetic check and is emitted as a **threshold and a boolean**, never as
a value — `CLAUDE.md`'s *a quantity that is nothing but ulp noise must be emitted as a threshold*.

A **sign convention, stated before the run**: a **negative** interaction means the two freedoms are
*synergistic* — each is worth more when the other is free; a **positive** one means they are *substitutive* —
a searched distribution is partly doing what a searched placement would have done. The standing expectation is
**substitutive**, because both freedoms repair the same defect (too little support where the solved collar puts
the load), and if so the sum of the two separately measured gains **overstates** what the joint search buys.
Either sign is the result; the expectation is written down so that it can be wrong.

Beside the 2 × 2, the `5 × 3` grid — `C-0167`'s four fixed placements plus the searched one, against equal /
rim-graded / searched — goes through `twoWayLogInteraction`, whose residual **is** the interaction and is
exactly zero on a separable grid. Twelve of its fifteen cells are `T-316`'s own, so the extra cost is three
numbers. It is reported as **descriptive**: the searched row is an optimum over a family containing row 4, so
its levels are not exchangeable with the others, and the load-bearing statement stays on the clean 2 × 2.

### Out of sample, which a joint search needs more than a single search does

A joint search selects a *tightest of 7 776* and then searches 50 stiffnesses on it, so it has strictly more
freedom to fit its own training stream than `T-316` had. Three separate readings, and none of them is inferred:

1. **Three disjoint seeds.** The exhaustive census selects on `323323`, the distribution search on
   `T-316`'s own `316316`, and every quoted verdict is read on `197197`, which neither search ever sees.
2. **The selection gap over the finalists.** Every tier-3 finalist carries **both** its training and its grading
   objective, so a **rank inversion** between the two rankings is visible rather than argued. `F14` is declared
   on rank 1 inverting.
3. **The screen's own convergence axis.** The tier-1a census is re-run at **80** screening realisations and the
   two rankings' top-8 sets compared, so *"the ranking is a property of the placements and not of 40 draws"* is
   a measurement.

### Cost, measured against `T-316`'s own runtime rather than guessed

`T-316`'s full run was **55 minutes** over 32 cells. Its inner loop is
`starts × (1 + sweeps × paths × 12) × realisations` realisation-solves per cell — 12 evaluations per coordinate
(4 scan points off zero, 2 golden-section brackets, 6 refinements) — which over its own 32 cells is
`≈ 7.6 × 10⁶` realisation-solves. That calibrates the machine at **≈ 2 300 realisation-solves per second**
averaged over path counts, i.e. **≈ 900 per second at `n = 50` and 41 samples**.
Every figure below is that constant times an exact count, and it is re-measured by a smoke pass before the
full run, as `CLAUDE.md` requires.

| stage | realisation-solves | at ≈ 900 / s |
|---|---|---|
| tier 1a — 7 776 placements × 2 rules × 40 | 622 080 | ≈ 12 min |
| tier 1b — 400 sampled placements, oracle floor × 40 | 16 000 | < 1 min |
| tier 1 convergence — the census re-ranked at 80 | 622 080 | ≈ 12 min |
| tier 2 — 17 placements × 3 starts × 601 × 60 | ≈ 1.84 × 10⁶ | ≈ 34 min |
| tier 3 — 4 placements × 3 starts × 1 201 × 120 | ≈ 1.73 × 10⁶ | ≈ 32 min |
| **the 50-path cell at `f = 0.30`** | | **≈ 90 min** |
| the descents and searches at 10, 20, 30 paths | ≈ 1.2 × 10⁶, cheaper per solve | ≈ 15 min |
| convergence axes, fragility, two-level projection, reproductions, gradings | | ≈ 40 min |
| **`P1`–`P7`** | | **≈ 2 h 25 m** |
| `P8` — the same 50-path cell at `f = 0.26` | | ≈ 90 min |
| **total with `P8`** | | **≈ 4 h** |

**`P8` is the declared elastic.** The smoke pass measures the rate; if the projected total exceeds **5 hours**,
`P8` does not run, its result-file entry is `null`, and the `dropped` census carries the measured rate and the
projection that refused it. That is a **deliberate, logged** reduction and not a silent cap.

### What would falsify the approach

If the **sliced** surrogate — the 55-station bank restricted to a placement's indices — does not reproduce a
surrogate built on that placement alone to the last few ulp, then the 7 776 evaluations are not evaluations of
the placements they are labelled with and nothing below is admissible (`F9`).
If the surrogate at full presence does not reproduce the **assembled** honeycomb solve with its own Woodbury
support forces applied as point loads, the whole sweep is on the wrong object (`F10`).
If a uniform pressure on the free lattice does not dish exactly zero at the resolved link, the solver is wrong
and not the physics (`F7`).
If the two transferred rules at `C-0167`'s own member do not reproduce `C-0208`'s and `C-0212`'s published
readings at the emission precision, this study is not standing where they stood (`F11`).

### What this task does NOT do

It does not re-open the cross-section, the raster, the ladder **phase** (`C-0148` determines it, and
`C-0141`'s sweep of it is over-determined by the same rule), the inter-row offset, the load case, the link
resolution, the radial bracket beyond its floor rung, the path counts beyond `{10, 20, 30, 50}`, the topology
(`C-0017`'s mandate spent once in a shared rigid-body mode is a change of **topology**, not of placement), or
route B. It does not withdraw `C-0208`'s `0 of 64` or `C-0212`'s `22 of 32`, both of which are exact on the
distributions and placements they were read on. It does not touch
`tile/HoneycombGrillage.kt`, `tile/SearchedDistribution.kt` or `coupling/RobustDistribution.kt`.

### What is written, and where

New: `src/main/kotlin/tile/JointPlacementDistribution.kt` (the honeycomb station-influence bank with its
`surrogateFor(indices)` slice, the placement family enumerator, the per-row placement descent, and the
factor-split adapter over `countPhaseSplit`) and `src/main/kotlin/tile/JointPlacementDistributionStudy.kt`;
tests first in `src/test/kotlin/tile/JointPlacementDistributionTest.kt`; a mutation harness at
`tools/T-323-mutation-test.py`, **declared** in `tools/P-31-harness-census.py` and **wired** in
`build.gradle.kts` in the same commit (`C-0185`, `C-0206`), with a subtracted unmutated baseline (`CH-0237`),
`find src -name` asserted to return exactly one path (`C-0190`), every anchor asserted to occur exactly once,
and its printed row shape declared (`T-306`). Result: `gpd/results/T-323-the-placement-and-the-distribution-together.json`,
with an `emission` block (`lattice: honeycomb`; the 2 mM / 10 nm / 0.192 V regime) and a **hand-added**
`ResultInputs` handle — never generated, because `tools/T-272-emit-result-inputs.py` reads the git index and
deletes handles for files not yet staged.

**Precision, declared before the first diff.** `pathDisagreement` is emitted as a threshold and a boolean;
`interaction`, every `worstResidual`, every `residual` and every field whose name contains *departure* is
emitted at **two** significant digits through `digitsByKey`, because each is a difference of two nearly equal
quantities; `regret` is emitted as a **ratio** rather than a difference for the same reason; and every number
inside a prose string goes through `roundedForProse`, with the dimensionless floor removed for ratios
(`C-0150`, `T-249`, `T-250`).

---

## Falsifiers, declared before the run

| # | fires if | expected |
|---|---|---|
| `F1` | **OPEN — the headline.** A **joint** search reaches an out-of-sample 90th-percentile dishing strictly better than **both** `(P₁, D₀)` and `(P₀, D₁)` at the deciding cell | either answer is the result |
| `F2` | **OPEN.** The **interaction** is resolvable — `\|interactionPerCent\|` exceeds the study's own worst convergence departure on the searched `p90`, which is the only honest threshold for it | either answer is the result |
| `F3` | **OPEN.** The interaction is **positive** — the two freedoms are *substitutive*, so the two separately measured gains **overstate** what a joint search buys. The sign is declared in the Plan and either outcome is the result | either answer is the result |
| `F4` | **OPEN.** The interaction carries a **larger share of the variation** than the smaller main effect does, over the `5 × 3` grid — [`C-0108`](../claims/C-0108-count-phase-interaction.md)'s finding, on a new pair of factors | either answer is the result |
| `F5` | **OPEN — `C-0063`'s ordering.** The **distribution** main effect is larger than the **placement** main effect on this lattice, which reverses *"which stations a coupling enters at is worth more than how its stiffness is distributed"* | either answer is the result |
| `F6` | **OPEN — the inherited sentence, measured.** The Spearman rank correlation of the transferred-rule placement ranking against the searched ranking over the tier-2 set is **below 0.5**, or the regret of selecting on the transferred objective exceeds `1.05×` — `CLAUDE.md`'s *selecting a placement on the EQUAL-SPRING objective is selecting on the wrong quantity once a distribution is free*, which this corpus asserts and has never measured here | either answer is the result |
| `F7` | a uniform pressure on the free honeycomb lattice at the resolved link does not dish exactly zero, at `< 1e−9` of the free stroke | must not fire — `CLAUDE.md`'s standing falsifier |
| `F8` | the default (`radialLinkStiffness = null`) lattice is not bit-identical to the standing object at `assembleLoad` over every degree of freedom, or its 435-bond crossover site set differs | must not fire — this task edits no shared source |
| `F9` | the **sliced** surrogate — the 55-station bank restricted to a placement's indices — differs from a surrogate built on that placement alone by more than `1e−10` relative | must not fire — it is the identity the whole method rests on (`C-0063`) |
| `F10` | the surrogate at full presence does not reproduce the **assembled** solve with its own Woodbury support forces applied as point loads, at `< 1e−9` relative | must not fire |
| `F11` | the two transferred rules at `C-0167`'s own member do not reproduce `C-0208`'s and `C-0212`'s published `p90` at the emission precision | must not fire |
| `F12` | the **searched placement** is worse than the best **fixed** placement in the same column, at any column of the `5 × 3` grid | must not fire — `C-0167`'s member is inside the enumerated family and among the descent's starts, so it is a property of the composition |
| `F13` | the 2 × 2's `pathDisagreement` exceeds `1e−12` of a log unit | must not fire — it is an arithmetic error, never a result |
| `F14` | **OPEN.** The tier-3 finalists' **training** ranking and their **grading** ranking disagree at rank 1 — the selection over 7 776 showing as noise rather than as a design | either answer is the result |
| `F15` | **OPEN.** The tier-1 census's top-8 set at **40** screening realisations differs from its top-8 at **80** — the exhaustive ranking being a property of the draws rather than of the placements | either answer is the result |
| `F16` | **OPEN.** The joint optimum's per-path **peak** exceeds `C-0023`'s `3.33333333 pN/nm`, read over §3's acceptable 3 nm stroke — the threshold a mandate on a **SUM** does not constrain | either answer is the result |
| `F17` | **OPEN.** The joint optimum's out-of-sample `p90` **beats the uncoupled tile** (`0.0448134881` at `f = 0.30`), at the 90th percentile and at zero defects — `CH-0272`'s axis, on a design with both variables free | either answer is the result |
| `F18` | **OPEN.** The joint optimum loses `T-5b` to its **worst single missing path** — `CLAUDE.md`'s *an optimised placement is a cancellation, and a cancellation has no tolerance to a missing term*, now read on a cancellation optimised in **two** variables at once | either answer is the result |
| `F19` | **OPEN.** The joint optimum's out-of-sample `p90` is **worse** than its in-sample training objective by more than the whole gain it reports over `(P₀, D₁)` — an over-fit large enough to consume the answer | either answer is the result |
| `F20` | **OPEN.** The **screen is binding** — the joint winner is the last-ranked placement admitted to tier 2, so the answer is a property of `K` rather than of the family | either answer is the result |
| `F21` | **OPEN.** The per-row placement **descent** does not find the exhaustive optimum at the 50-path cell — which measures the slack of the instrument used at 10, 20 and 30 paths, where no exhaustive answer exists | either answer is the result |
| `F22` | the placement family census does not reproduce: 55 stations, per-row ladder sizes `[5, 6, 5, 6, 5, 6, 5, 6, 5, 6]`, family sizes `24 300 000 / 75 937 500 000 / 320 000 000 000 / 7 776`, and **no** centro-symmetric member at the determined `(phase 16, offset 14)` — asserted against the lattice object, not transcribed | must not fire — `P1`, and it runs before any solve |
| `F23` | two independent runs of the study do not produce a byte-identical result file | must not fire |
