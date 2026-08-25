# C-0216 — **THE PLACEMENT AND THE DISTRIBUTION SEARCHED TOGETHER REACH `0.0677344328` WHERE NEITHER SEARCH ALONE REACHES IT — AND `C-0063`'s ORDERING **REVERSES** ON THIS LATTICE, AT `0.009241` AGAINST `0.3046` IN LOG UNITS.** The corpus carried a claim about these two variables' ordering (*"which stations a coupling enters at is worth more than how its stiffness is distributed"*) and no measurement of their **interaction**. Measured, the 2 × 2 is `−0.452630313` log units in **total**, a factor of **`1.57244277`**, and the interaction is **`−0.1388`, `−12.96 %`, NEGATIVE — the two freedoms are SYNERGISTIC**, so each is worth *more* when the other is free and the declared expectation (substitutive) is **wrong**. The split is order-dependent exactly as `CLAUDE.md` says: placement first is `−0.009241` then `−0.4434`, distribution first is `−0.3046` then `−0.1481`, the totals agree identically and the gap **is** the interaction, at a path disagreement below `1e−12`. **The same 2 × 2 is also taken IN SAMPLE**, where no corner carries a selection — `−7.845 %` — so the selection is separable from the interaction by measurement rather than by caveat. **The cheap bound decided the whole method and needed no solve**: the determined ladder carries **55** stations, `5, 6, 5, 6, 5, 6, 5, 6, 5, 6` by row, so at five columns the five-station rows are **forced** and the family is `6⁵ = 7 776` — **exhaustible**, which removes `C-0102`'s *a descent compared against an exhaustive enumeration is not a comparison* at the deciding cell — and it admits **NO centro-symmetric member** at any of its five row pairs, so `C-0063`'s own search strategy has no analogue here. **`CLAUDE.md`'s inherited warning is now measured and it is sharper than it was stated**: the **equal-spring** ranking of placements is *anti*-correlated with what a searched distribution wants (Spearman **`−0.09354`** and **`−0.4451`**) while the **rim-graded** ranking is mildly informative (`+0.4924`, `+0.626`). **`F1`, `F4`, `F5`, `F6`, `F12`, `F15`, `F16`, `F20` and `F23` fired**; `F2`, `F3`, `F14`, `F17`, `F18`, `F19` and `F21` were declared open and did not. **`F23` was declared CLOSED and fired**: two runs move **26 of 1 252** leaves, all of them an argmin or a rendering of one, **0 verdicts and 0 unclassified** — and the cause is `C-0135`'s cure reaching **2 of this study's 14** selection sites, which is published rather than repaired away (§14)

| | |
|---|---|
| **Task** | [`T-323`](../tasks/T-323-the-placement-and-the-distribution-together.md) — raised by [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md) (`T-316`) §15 as the first of its open questions, and by [`C-0063`](C-0063-upward-root-placement.md)'s own *Still open* item 3 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (the same honeycomb grillage, the same `C-0208` per-bond link, the same mandate, the same 4 000-realisation grading stream — the placement and the distribution both move) **+ logical** (the placement family's size is a product and its centro-symmetry a set intersection, both before any solve; the 2 × 2's two orderings share their endpoints, so their totals agree identically and their splits differ by exactly the interaction) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** One lattice, one raster, one load case, one dropout model, at a radial link constant `C-0208` records as unsourceable |
| **Provenance** | [`gpd/results/T-323-the-placement-and-the-distribution-together.json`](../results/T-323-the-placement-and-the-distribution-together.json) — the **second** of two emissions, both retained ([`gpd/data/T-323-reproducibility/`](../data/T-323-reproducibility/README.md)), because `F23` **fired** and the measurement is external to either run (§14), written by [`tile/JointPlacementDistributionStudy.kt`](../../src/main/kotlin/tile/JointPlacementDistributionStudy.kt) (**new**) on [`tile/JointPlacementDistribution.kt`](../../src/main/kotlin/tile/JointPlacementDistribution.kt) (**new**). **29 named tests** written first and watched fail — [`tile/JointPlacementDistributionTest.kt`](../../src/test/kotlin/tile/JointPlacementDistributionTest.kt), which did not compile against a model that did not exist — and a **25-mutation** harness at [`tools/T-323-mutation-test.py`](../../tools/T-323-mutation-test.py), **0 survivors over a subtracted baseline of 0** (`CH-0237`), declared in `tools/P-31-harness-census.py` and wired as `testJointPlacementDistributionMutations` in the same commit. **No existing Kotlin main source was modified** except one hand-added line of `structure/ResultInputs.kt`. `tile/HoneycombGrillage.kt`, `tile/SearchedDistribution.kt` and `coupling/RobustDistribution.kt` were **not touched**, and `coupling/CountPhaseInteraction.kt`'s `countPhaseSplit` is **reused unchanged** |
| **Consumes** | [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md)/`T-316` (the search composition, the two transferred rules, the training stream, and its published cells, reproduced), [`C-0208`](C-0208-a-bond-link-is-two-mechanisms.md)/`T-310` (the resolved per-bond link and its published cells, reproduced), [`C-0063`](C-0063-upward-root-placement.md) (the placement axis, the influence bank sliced to a placement, and the ordering claim this task measures), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the grillage port and the four placements), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the 21 bp station ladder and its forced 14 bp inter-row offset), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (which DETERMINES the ladder phase), [`C-0108`](C-0108-count-phase-interaction.md) (`twoWayLogInteraction` and `countPhaseSplit`, reused rather than copied), [`C-0135`](C-0135-descent-manifold-width.md) (`searchDecision` and the smoothed minimax), [`C-0089`](C-0089-dropout-robust-placement.md) (the percentile objective, the oracle floor, the single-path removal, `spearmanRankCorrelation`), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the measured depth incorporation), [`C-0060`](C-0060-buildable-stiffness-ratio.md) (its **FLAT** ratio window, named as `CH-0273` requires), [`C-0023`](C-0023-two-sided-coupling.md) (the 10 pN unzip allowable), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, as a SUM), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load, which is what makes the bank a compliance), [`C-0022`](C-0022-tile-edge-load-profile.md)/`T-3b` |
| **Verdict** | **PASS on all eight predicates.** Of the twenty-three declared falsifiers **`F1`, `F4`, `F5`, `F6`, `F12`, `F15`, `F16`, `F20` and `F23` FIRED** — **seven** declared **OPEN**, so *"either answer is the result"*, and **two declared CLOSED**: `F12`, whose declaration is one word short (§9), and `F23`, which is a real result about this study and is published rather than repaired away (§14). `F16` carries a correction this claim states rather than glosses. **`C-0212`'s and `C-0208`'s published readings are NOT withdrawn**: all 13 reproduce at `3.8E-9`. Raises [`CH-0278`](../challenges/CH-0278-a-restored-source-is-not-a-restored-class.md) and [`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `C-0022`'s design state — 10 nm gap, `0.192 V`, its solved collar; cross-section **`10 × 6`**, block extent **`116 bp`** (`edgeX` 39.44 nm, `edgeY` 38.04 nm), the drawable **`102 / 109`** raster, 435 staple bonds and 59 raster turn ties present; `d` = 2.536 nm (SAXS); `k_θ` = 13.5294118 pN·nm/rad; transverse link pinned at `C-0205`'s ceiling **`254.808095 pN/nm`** and the radial rung at `C-0208`'s bracket floor **`754.005141`**, giving a through-thickness link of **`629.20588`**; composite fractions **0.30** (primary) and **0.26** (`P8`, which **ran**); the station ladder at `C-0148`'s determined phase **16** and `C-0141`'s forced **14 bp** inter-row offset; `C-0017`'s mandate **`33.3333333 pN/nm` on the SUM**; **grading** seed `197197` / **4 000** realisations, **training** seed `316316` / **120**, **screening** seed `323323` / **40**; dishing on an **81 × 81** grid and the searches' own on **41**; `subdivisions = 1`; rim band 6.7 nm; `T-5b` = 0.10 |
| **Raises** | [`CH-0278`](../challenges/CH-0278-a-restored-source-is-not-a-restored-class.md), [`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md) |

---

## The claim, in six lines

`C-0063` searched the **placement** with the distribution fixed and `C-0212` the **distribution**
with the placement fixed. Neither moved the other, so this corpus had a claim about their
**ordering** and no measurement of their **interaction**.

Searched together at `C-0208`'s resolved per-bond link, the four corners of the 2 × 2 read
`0.106508519` / `0.105528854` / `0.078544978` / **`0.0677344328`** of the free-tile stroke, all
out of sample on the `197197` stream neither search ever sees. The joint corner beats **both**
singles, the total is `−0.452630313` log units, and the **interaction is `−0.1388`** — negative,
so the two freedoms are **synergistic**.

**And the ordering the corpus carried is reversed here.** The placement main effect is
`0.009241` and the distribution main effect `0.3046` in log units — the quotient of this study's
own `0.304553543` and `0.00924055963` is **`32.9583`**, the other way round from `C-0063`'s
square-lattice reading.

## 1. The two cheap bounds ran first and neither needed a solve

**Bound 1 — the family is a PRODUCT, and one cell of it is EXHAUSTIBLE.**

| columns | paths | row option counts | family size | enumerated |
|---|---|---|---|---|
| 1 | 10 | `5, 6, 5, 6, 5, 6, 5, 6, 5, 6` | **24 300 000** | no — descent |
| 2 | 20 | `10, 15, 10, 15, …` | **75 937 500 000** | no — descent |
| 3 | 30 | `10, 20, 10, 20, …` | **320 000 000 000** | no — descent |
| **5** | **50** | **`1, 6, 1, 6, 1, 6, 1, 6, 1, 6`** | **7 776** | **YES, exhaustively** |

The determined ladder carries **55** stations on the ten rooting helices, alternating **5** on the
even rows (the `102 bp` sense) and **6** on the odd ones (the `109 bp` sense) — the `7 bp` row
stagger showing up as a **station**. At five columns the five-station rows are **forced**
(`C(5,5) = 1`) and the family is `6⁵`. That is the cell `C-0208`'s and `C-0212`'s tightest
readings both live at, and it needs **no descent at all**.

**Bound 2 — the family admits NO centro-symmetric member, at any of its five row pairs.**
Row `r` maps to row `9 − r` under `(s, y) → (−s, −y)`, and those rows carry **opposite** window
parities, so a station at `s` on an even row needs `−s` on an odd row's ladder. The intersection
is empty at **every** row pair. `C-0063`'s entire search strategy was a centro-symmetry
congruence — two of 32 square-lattice phases admit one, and its winner was at one of them — and
**the honeycomb's forced row stagger destroys that symmetry outright.** Which is exactly why
bound 1 matters.

**Bound 3 — the placement axis's own width, and it is the first surprise.** The exhaustive census
over all 7 776 placements at equal springs runs `0.0890058` to `0.129966804` on the screening
stream: a spread of **`1.46020601`**, against the distribution axis `C-0212` measured at
`1.10434917`–`1.70065256` on this same lattice. The two axes are **comparable in width**, where
`C-0063` measured the placement axis at **5.9×** and the distribution axis at **13.9 %**.

**And the bank is what made it affordable.** One `HoneycombStationBank` of **55** unit-point-load
solves per `(fraction, rung)` serves **every** placement and **every** distribution at that cell,
because a placement is a **slice of the bank's index set**. The slice is asserted against a
surrogate built on that placement alone at a departure of **`3.8E-16`**, not argued.

## 2. The answer, and the 2 × 2 in both orderings

| | `D₀` = the best transferred rule | `D₁` = searched |
|---|---|---|
| **`P₀`** = `C-0167`'s determined lattice on the rooting helices | `0.106508519` | `0.078544978` |
| **`P₁`** = searched over the determined family | `0.105528854` | **`0.0677344328`** |

| | out of sample | in sample |
|---|---|---|
| total | **`−0.452630313`** | `−0.545415273` |
| placement first, then distribution | `−0.009241` then `−0.4434` | `−0.0272398815` then `−0.518175392` |
| distribution first, then placement | `−0.3046` then `−0.1481` | `−0.436478834` then `−0.108936439` |
| **interaction** | **`−0.1388` = `−12.96 %`** | **`−0.0817` = `−7.845 %`** |
| path disagreement | below `1e−12` | below `1e−12` |
| joint beats both singles | **yes** | **yes** |

**The interaction is NEGATIVE, so the two freedoms are SYNERGISTIC** — each is worth *more* when
the other is free. `T-323`'s Plan declared the expectation as **substitutive**, before the run,
precisely so that it could be wrong; `F3` was declared on the positive sign and **did not fire**.
The declared expectation is retained rather than struck (`C-0071`), and it was wrong.

**Both orderings are emitted because the split is order-dependent and the corners are not.** The
totals agree identically — the two paths share their endpoints — and their difference **is** the
interaction. This is `coupling/CountPhaseInteraction.kt`'s `countPhaseSplit` **reused unchanged**
under `count ↔ placement`, `phase ↔ distribution`; the mapping is emitted as a field and the
arithmetic is not written twice.

**And the same 2 × 2 in sample is what separates the interaction from the SELECTION.** Out of
sample every corner but `(P₀, D₀)` carries a selection and the placement freedom carries a much
larger one — it is chosen from a screened set drawn out of 7 776. In sample none of them does.
The interaction is `−12.96 %` out of sample and `−7.845 %` in sample, so **the selection accounts
for about two fifths of it and the other three fifths are the freedoms themselves.** **At `f = 0.26` the two readings EXCHANGE PLACES** — `−8.0 %` out of sample against `−13.0 %` in
sample, where at `f = 0.30` it is `−12.96 %` out against `−7.845 %` in. So the two arms agree that
the interaction is synergistic and about its size, and they **disagree about whether the selection
adds to it or subtracts from it**. Both are published and neither is picked.

## 3. `C-0063`'s ordering, measured on this lattice, is REVERSED

| | `C-0063`, square lattice | here, honeycomb |
|---|---|---|
| the placement axis | `0.4156 → 0.0706`, **5.9×** | main effect **`0.009241`** log units |
| the distribution axis | **13.9 %** on its winner | main effect **`0.3046`** log units |
| which is larger | **placement** | **DISTRIBUTION** — `0.304553543 / 0.00924055963` = **`32.9583`** |

`F5` was declared open on exactly this and **fired**. The sentence `CLAUDE.md` carries —
*"which stations a coupling enters at is worth more than how its stiffness is distributed"* — is
`C-0063`'s and is correct **on `C-0063`'s lattice**; on the honeycomb face at the resolved link it
is the other way round, and the reason is visible in bound 3: the honeycomb's placement family is
**1.46×** wide where the square lattice's was 5.9×, because the determined ladder gives each row
only five or six stations and forces half of them.

**The `5 × 3` grid says the same thing with a share rather than a ratio.** Over `C-0167`'s four
fixed placements plus the searched one, against equal / rim-graded / searched:

| | share of the variation |
|---|---|
| the **distribution** main effect | **`0.9596`** |
| the **interaction** | **`0.02575`** |
| the **placement** main effect | **`0.0146`** |

**`F4` fired**: the interaction carries a **larger** share than the smaller main effect does —
`C-0108`'s own finding (`9.79 %` against a phase main effect of `7.84 %`) reproduced on a new pair
of factors and on a new lattice, and here the margin is `1.76×`.

## 4. The inherited sentence, measured — and it is sharper than it was stated

`CLAUDE.md` carries *"selecting a placement on the EQUAL-SPRING objective is selecting on the
wrong quantity once a distribution is free"* as a warning from `C-0072`, with no number on this
lattice. Measured over the searched set, against the **training percentile the distribution search
actually minimises**:

| screen | Spearman ρ at `f = 0.30` | at `f = 0.26` | regret |
|---|---|---|---|
| **equal springs** | **`−0.09354`** | **`−0.4451`** | `1.2208`, `1.19325` |
| **rim-graded 5:1** | `+0.4924` | `+0.626` | `1.04061`, `1.0` |
| the oracle floor (distribution-free) | `−0.115384615` | `+0.236263736` | `1.20415739`, `1.10954222` |

**`F6` fired.** The warning is confirmed and it is *specific*: the equal-spring ranking is
**anti-correlated** with what a searched distribution wants, at both fractions, and costs
`1.11×`–`1.19×` in regret. The **rim-graded** ranking is the informative one — which no upstream
source predicts, and which is worth stating because the corpus grades on **both** rules and treats
them as interchangeable comparands.

**The oracle floor is not the screen it looks like it should be.** It is a pointwise lower bound
over *every* distribution and therefore ranks placements by their **potential**, which is the
right idea and does not work: `−0.115` and `+0.236`, with the worst regret of the three.

## 5. Every threshold the moving quantities feed, and their CONJUNCTION

`CH-0272` records that no verdict block in this corpus has ever stated one. Over the **17** graded
corners:

| | count |
|---|---|
| flat at the 90th percentile | **7** |
| flat **and** inside `C-0023`'s `3.33333333 pN/nm` per-path allowable | **1** |
| beating the **uncoupled** tile at the 90th percentile | **0** |
| beating it at **zero defects** | the joint corner does, at `0.0274177603` against `0.0448134881` |

**The one flat-and-admissible corner is `(P₀, D₁)` — the corner with FEWER freedoms.** The joint
corner is flatter (`0.0677344328` against `0.078544978`) and its peak is `3.3594977` against the
allowable's `3.33333333`: `3.3594977 / 3.33333333 − 1` = **0.78 %**. `F16` fired.

**That is not the search failing, it is the search being given one objective and no constraint**,
and `optimiseStiffnessDistribution` has carried a `ceiling` parameter since `C-0058` —
[`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md).

**`F17` did not fire and `CH-0272` is reproduced with BOTH variables free**, which is the
strongest form of it this corpus has: **on this lattice, under `C-0087`'s measured dropout,
flatness is not what the coupling buys — and that survives freeing the placement as well as the
distribution.** At zero defects the joint corner beats the uncoupled tile by `1.63×`, so the cost
is the **dropout** and not the coupling. It remains **not** an argument for removing the coupling:
`C-0017`'s mandate is a placement and stability requirement.

## 6. Fragility, the two-level projection, and the paired readings

**`F18` did not fire.** The joint corner's worst **single**-path removal is `0.0431473228` against
`T-5b`'s `0.10` — it does not merely survive, it survives with `2.3×` of margin, and it amplifies
`1.57369976×` against its own zero-defect reading. `CLAUDE.md` prices an optimised cancellation as
having *no tolerance to a missing term*; a cancellation optimised in **two** variables at once
does not behave that way here, which is the same direction `C-0212` measured on one.

**Quantised onto `C-0060`'s own two levels** — the object its window was measured on (`CH-0273`):

| corner | `R₂` | two-level `p90` | flat | inside `[3.5, 20]` |
|---|---|---|---|---|
| `(P₀, D₁)` | `4.99455139` | `0.0993653869` | **yes** | yes |
| **`(P₁, D₁)` — joint** | `5.64640155` | `0.096638263` | **yes** | yes |

Both projections stay flat, and the joint one is the flatter. `C-0060`'s window is quoted here as
what its owner calls it — its **FLAT** ratio window, measured on `C-0058`'s square-lattice
45-station design — and the word *buildable* is not used of it.

**Every difference is reported PAIRED as well as between two summaries** (`CLAUDE.md`: *a ratio of
two order statistics is not the order statistic of the ratio*):

| | ratio of the two `p90`s | median of the per-realisation ratio | realisations the other design wins |
|---|---|---|---|
| joint against `(P₀, D₀)` | `1.57244277` | **`1.73073531`** | **627 of 4 000** |
| joint against `(P₁, D₀)` | `1.55797944` | **`1.78373006`** | 673 of 4 000 |
| joint against `(P₀, D₁)` | `1.15960192` | `1.14510565` | **1 358 of 4 000** |

The paired reading is **larger** than the unpaired one against both transferred corners, so the
unpaired ratio **understates** the joint search's advantage there — and against `(P₀, D₁)` the
fixed placement still wins at **34 %** of realisations, which no summary of either kind carries.

## 7. The other column counts, and the descent that was calibrated where the truth is known

| paths | `(P₀, D₀)` | `(P₀, D₁)` | **joint** | joint flat? |
|---|---|---|---|---|
| 10 | `0.144098481` | `0.103930459` | `0.111678905` | no |
| 20 | `0.156294197` | `0.104079638` | **`0.0761163727`** | **yes** |
| 30 | `0.119235074` | `0.0945642352` | **`0.066722838`** | **yes** |
| 50 | `0.106508519` | `0.078544978` | **`0.0677344328`** | **yes** |

**At 10 paths the joint search is WORSE than the distribution search alone out of sample**
(`0.1117` against `0.1039`) — the one cell where freeing the placement loses, and it is the cell
with the largest family relative to its path count. **And 30 paths is as flat as 50**
(`0.066722838` against `0.0677344328`), which no count sweep on a transferred rule has ever shown.

**`F21` did not fire, and it did not fire exactly.** The per-row placement descent — the
instrument used at 10, 20 and 30 paths — finds the **exhaustive** optimum at the 50-path cell at a
departure of **`0.0`**: same placement, same objective, `1.0×`. That is `C-0102`'s
*a descent compared against an exhaustive enumeration is not a comparison* answered by measuring
the instrument at the one cell where both exist.

## 8. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a per-path stiffness in pN/nm summing to a mandate in pN/nm; a family size as a dimensionless product of counts; a split term as a natural logarithm of a ratio of two dishings, therefore dimensionless, and an interaction as a difference of two such terms; a dishing as a fraction of the free stroke of the **same** lattice | **PASS** |
| **2 — limiting cases** | a saturated family has exactly one member; a column count no row can supply is refused, and refused **by its own message** rather than by an understudy's; a key of the wrong shape, an unsorted key and a repeated station are refused; a zero-sweep descent, an empty start set and a foreign start are refused; a constant objective moves the descent nowhere | **PASS**, 29 named tests |
| **3 — symmetry and the standing falsifiers** | a uniform pressure on the free tethered lattice at the resolved link dishes **`3.3E-13`** of the stroke (`F7`); the default lattice bit-identical to the standing object on `assembleLoad` over **4 320** degrees of freedom and on the **435**-bond site set (`F8`); the bank **slice** against a surrogate built on that placement alone, **`3.8E-16`** (`F9`); the surrogate at full presence against the **assembled** solve with its own Woodbury support forces, **`3.9E-14`**, taken on the **searched** distribution (`F10`); centro-symmetry measured in **both** directions on the same predicate; a prestrain moves the free field and **not** the bank's compliance, which is `C-0104`'s trap asserted rather than assumed | **PASS** |
| **4 — numerical convergence** | **nine** axes at the deciding cell: the descent against the exhaustive optimum (**`0.0`**), the search grid 41 against 81 (`0.015`), the verdict grid 81 against 41 and 161 (**`0.0`** and **`0.0`**), the training realisations 120 against 240 (`0.015`) and against 60 (`0.26`), the descent's sweeps 2 against 3 (`0.0078`), beam subdivisions 1 against 2 (`0.0073`), and the screening realisations 40 against 80 (`0.1`). **`0 of 9` move the flatness verdict**; the screening axis moves the top-6 **set** (`F15`), which is a statement about the screen and not about the answer | **PASS** |
| **5 — literature and upstream** | **13 reproductions, worst departure `3.8E-9`** — `C-0208`'s published `p90` at all four of `C-0167`'s placements on **both** transferred rules, `C-0212`'s published **searched** `p90` at all four, and `C-0212`'s uncoupled tile | **PASS** |

### The twenty-three declared falsifiers

| # | declared | fired | reading |
|---|---|---|---|
| `F1` | **OPEN** | **FIRED** | joint `0.0677344328` against `0.105528854` placement-only and `0.078544978` distribution-only |
| `F2` | **OPEN** | no | interaction `12.96 %` against a **worst** convergence departure of `26.1 %` — and see §9, because that worst is a deliberate **coarsening** |
| `F3` | **OPEN** | no | the interaction is **negative**; the declared expectation was substitutive and is **wrong** |
| `F4` | **OPEN** | **FIRED** | interaction share `0.02575` against a placement main effect of `0.0146` |
| `F5` | **OPEN** | **FIRED** | `C-0063`'s ordering **reverses** — `0.009241` against `0.3046` |
| `F6` | **OPEN** | **FIRED** | the equal-spring screen is **anti-correlated**, `−0.09354` and `−0.4451` |
| `F7` | closed | no | `3.3E-13` |
| `F8` | closed | no | 4 320 degrees of freedom, 435 bond sites, identical |
| `F9` | closed | no | `3.8E-16` |
| `F10` | closed | no | `3.9E-14` |
| `F11` | closed | no | worst of 13 is `3.8E-9` |
| `F12` | closed | **FIRED** | **and the declaration is one word short** — see §9 |
| `F13` | closed | no | path disagreement below `1e−12` at both arms |
| `F14` | **OPEN** | no | the four finalists' training and grading rankings agree at rank 1 |
| `F15` | **OPEN** | **FIRED** | the top-6 set moves between 40 and 80 screening realisations |
| `F16` | **OPEN** | **FIRED** | peak `3.3594977` against `3.33333333`, i.e. `3.3594977 / 3.33333333 − 1` = **0.78 %** ([`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md)) |
| `F17` | **OPEN** | no | `0.0677344328` against an uncoupled `0.0448134881`; at zero defects it **does** beat it |
| `F18` | **OPEN** | no | worst single-path removal `0.0431473228` against `0.10` |
| `F19` | **OPEN** | no | out-of-sample penalty `0.00477` against a gain over the fixed placement of `0.01081` |
| `F20` | **OPEN** | **FIRED** | at `f = 0.30` the joint winner ranks **6 of 7 776** in the equal-spring screen, which is the last one admitted; at `f = 0.26` it ranks 1 in the rim screen |
| `F21` | **OPEN** | no | the descent finds the exhaustive optimum **exactly**, `1.0×` |
| `F22` | closed | no | 55 stations, `5, 6, …`, `7 776`, no centro-symmetric member — asserted against the lattice object |
| `F23` | closed | **FIRED** | **two independent runs are NOT byte-identical** — 26 of 1 252 leaves moved, **0 verdicts, 0 booleans, 0 added, 0 removed, 0 unclassified**. See §14 |

## 9. Two declarations were one word short, and both are retained rather than repaired

**`F12` — a composition's guarantee is an IN-SAMPLE guarantee.** It was declared *"must not fire —
a property of the composition: `C-0167`'s member is inside the enumerated family and among the
descent's starts."* That is true **in sample**, on the stream the search sees, and the `5 × 3` grid
is graded **out** of sample. It fired: the searched row reads `0.115838204` at equal springs where
the abstract grid reads `0.114289438`. The in-sample statement — freeing the placement over the
tier-2 set cannot lose against `C-0167`'s own member, which is in that set — **holds**, at a
transferred gain of `1.02761251×` and a searched gain of `1.11509757×`. Two of the five grid rows
are placements the searched family does not contain at all, so no guarantee of any kind covers
them. `CLAUDE.md`'s *a pre-registered criterion can still be arithmetically wrong, so publish both
readings rather than picking one*, met on the author's own declaration.

**`F2` — a threshold taken over the WORST of a convergence sweep includes a deliberate
coarsening.** `F2` compares the interaction against *"the study's own worst convergence departure
on the searched `p90`"*, and the worst is `0.26` — the **60**-realisation arm, which is the
training ensemble deliberately **halved**. The **refinement** direction, which is what measures
convergence, is `120 → 240` at **`0.015`**, and the interaction of `12.96 %` exceeds that by
**`8.6×`**. So the interaction **is** resolvable against the axis that measures convergence and is
**not** resolvable against an arm that measures degradation, and the declared criterion reads the
second. Both are published; the declaration is not struck.

## 10. The mutation test, and the run that produced it is itself a finding

`C-0161`'s standard on a Kotlin subject: **25 mutations, every one of which must fail a NAMED
test**, unmutated copy first and its failures subtracted (`CH-0237`), `find src -name` asserted to
return exactly one path (`C-0190`), every anchor asserted to occur exactly once (`C-0185`), and
the `-x` flags **derived** from `build.gradle.kts`'s own `dependsOn` block (`C-0194`).

**First run: 25 mutations, 9 SURVIVED** — and every survivor was a gap in the **fixtures**, in
three kinds. A predicate with three clauses whose families satisfied or violated all three at once
(the centro-symmetry census, four rows). **`C-0207`'s duplicated-guard shape, twice**: widening the
family's own column check lets `ascendingSubsets` throw instead and widening the descent's family
check lets `placementAt` throw instead, the **same exception type**, so only the **message**
separates a guard from its understudy. And a rule held open only in the direction a raw comparison
also refuses: the decision-precision test perturbed the objective *upward*, which a raw comparison
rejects too, so dropping `searchDecision` was invisible — the discriminating case is a candidate
better by **less** than six significant digits carrying a **larger** key.

**After the repair: 25 mutations, `0` survivors over a subtracted baseline of `0`.** 23 named
tests before, **29** after.

**And the run in between found a defect in the harness, not in the code** —
[`CH-0278`](../challenges/CH-0278-a-restored-source-is-not-a-restored-class.md). Handed the
snapshot it had already mutated, the baseline bound to the previous invocation's **last mutant's
compiled class**: a harness restores the **source** and cannot restore a **class**, and Kotlin's
incremental compiler sees the restored file and the pre-mutation build state as one state. Runs 2
and 3 differ in **one line of the harness and nothing else** and read `2 survivors` against `0`.

## 11. `P1`–`P8`, discharged by name

| # | target | where |
|---|---|---|
| `P1` | the family, its size at every column count, which cell is exhaustible, and whether any centro-symmetric member exists — before any solve | §1 — `24 300 000 / 75 937 500 000 / 320 000 000 000 / 7 776`, and **0** centro-symmetric row pairs |
| `P2` | `(P₁, D₀)` exhaustively at 50 paths, with the placement axis's whole distribution | §1 and §2 — all **7 776** enumerated, `0.0890058`–`0.129966804`, spread `1.46020601` |
| `P3` | `(P₁, D₁)`, the joint corner, graded out of sample | §2 — **`0.0677344328`** on the `197197` stream |
| `P4` | the 2 × 2 in **both** orderings and the interaction; the `5 × 3` two-way fit | §2 and §3 — `−0.1388` out of sample, `−0.0817` in sample, share `0.02575` |
| `P5` | `C-0063`'s ordering measured here, and the inherited sentence measured | §3 and §4 — **reversed**, `0.304553543 / 0.00924055963` = `32.9583`; Spearman `−0.09354` / `−0.4451` |
| `P6` | every threshold, and the conjunction | §5 — 17 corners, **7** flat, **1** flat and admissible, **0** beating the uncoupled tile |
| `P7` | the descent's slack where the truth is known | §7 — departure **`0.0`**, the same placement |
| `P8` | the same 2 × 2 at `f = 0.26` | §2 — **it ran**; interaction `−7.8 %` out of sample and `−13.0 %` in sample |

## 12. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated. No such coupling has
  been drawn, let alone folded.
- **`C-0212`'s `22 of 32` and `C-0208`'s `0 of 64` are not withdrawn.** Both are exact on what they
  graded and all 13 of their readings reproduce here at `3.8E-9`.
- **The search is UNCAPPED on the per-path allowable**, and one corner of seventeen is flat and
  admissible ([`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md)).
- **The placement family is the DETERMINED ladder on the rooting helices**, at the phase
  `C-0148`'s rule fixes. The three unrealisable members of `C-0167`'s four placements are graded
  and not searched, and a raster closing at another phase is a question about the **raster**.
- **The joint corner is an order statistic over a screened set**, and the screen is **binding** at
  the `f = 0.30` arm (`F20`: rank 6 of a top-6). What the answer would be at a larger `K` is not
  measured, and the direction is known: it can only improve.
- **`WHICH` placement wins is not out of sample the way the COUNT is.** `F14` did not fire, so the
  four finalists' two rankings agree at rank 1; but *tightest of 7 776* carries a selection that
  §2's in-sample arm prices at about two fifths of the interaction.
- **One load case, one cross-section, one raster, one dropout model, one radial rung**
  (`C-0208` records that constant as unsourceable), and **`CH-0242`'s common-mode spring is
  absent** at every bond and every tie.
- **The census is on ROUTE A**, whose raster turns carry zero unpaired nucleotides (`C-0175`'s
  modelling choice); `C-0193` and `C-0200` establish that the only folded block of this
  cross-section does otherwise.
- **Buildability is not established anywhere.** What it costs to PLACE a distribution spanning a
  ratio of `65.2239927` is `C-0060`'s own placement question, and nothing in this corpus prices it.

## 14. `F23` FIRED — two runs are not byte-identical, and the cause is a cure that reached **2 of 14** selection sites

**`F23` was declared CLOSED — *"must not fire"* — and it fired.** It is published, not repaired
away, and this claim is filed against the artifact in the tree, which is **run B**.

**Both runs are retained**, because a measurement nobody can repeat is not one: the first is
[`gpd/data/T-323-reproducibility/run-a.json`](../data/T-323-reproducibility/run-a.json) and the
second is the committed artifact, which is what this claim is filed against.

**The study's own `falsifiers[22].fired` reads `false`, and it has to**: a run cannot assert
byte-identity about itself. The measurement is external, and it is two independent runs through
`tools/study.sh`, each in its own snapshot, diffed outside the study:

| | |
|---|---|
| leaves in the file | **1 252** |
| leaves that moved | **26** |
| **verdicts / booleans moved** | **`0` of 250** |
| leaves added or removed | **`0`** and **`0`** |
| **unclassified** | **`0`** |

`CLAUDE.md` records that *"0 unclassified and 0 verdicts"* is what makes an irreproducibility
cosmetic and that no scalar can say it. Here it is, by kind:

| kind | leaves | what they are |
|---|---|---|
| **argmin — the POINT a search reached** | **1** | `corners/14/placementLabel`: the `(P₁, D₀)` corner at `f = 0.26` selected a **different placement**, one station different in one row of ten |
| **argmin functional — a quantity read AT that point** | **13** | that corner's `p90`, `nominal` and single-path removal; the three `split/2` terms built on it; its five `fragility/5` fields; two `paired/4` fields |
| **argmin functional — a statistic OVER a selected set** | **4** | three `spearmanAgainstSearched` and one `regretOfSelectingOnThisScreen` |
| **argmin functional — a rank or a count** | **2** | `determinedRankFromBest` `83 → 82`, and `realisationsWhereTheNumeratorWins` `600 → 603` |
| **departure — emitted at two significant digits already** | **2** | `split/2/interaction` `−0.081 → −0.083` and its per cent `−7.8 → −8.0` |
| **prose rendering of a moved number** | **4** | `F6`'s note, `F9`'s, `F10`'s, and `findings[5]` |

**Every moved leaf is an argmin, a functional of one, a two-digit departure, or a sentence
carrying one of those. Not one is an objective, a verdict, or a threshold crossing.** The joint
corner, the interaction at `f = 0.30`, the ordering reversal, the family census, the flat count,
the admissible count and every falsifier's fired/not-fired flag are **bit-identical** across the
two runs.

### The cause is three channels and two of them are defects of this study

**(a) `C-0135`'s cure reached `2` of this study's `14` selection sites, and the twelve it
missed are mine.** Counted in the source, the study makes **fourteen** `argmin`-style selections.
**Two** compare a `searchDecision`-rounded objective — `finalistSlots` and `jointSlot` — and both
are the ones that consume `T-316`'s own `percentileObjective`. The other **twelve** compare a
**raw** `Double`: `determinedTransferred`, `topPerScreen`'s sort, `bestPerRuleOnTraining`,
**`bestTransferredCandidate`** (the one that moved), `gradingArgmin`, each screen's `screenArgmin`,
`floorArgmin`, the descent-column `chosen`, `transferredBest`, and the screening axis's
`topAtEighty` / `topAtForty` / `bestAtEighty`. A thirteenth site is a raw `<` **count**,
`determinedRankFromBest`. `t323P90` returns an unrounded order statistic, so a tie between two
placements is broken by the last ulp.

**The pattern is exact and it is the one `CLAUDE.md` names**: the cure travelled with the
machinery that was *inherited* and reached nothing that was *written here* — *a cure is a property
of a CALL SITE, not of a repository; grep for the call sites, not for the fix*, committed by the
author who had just quoted it in this study's own plan.

**(b) Two identity residuals were emitted as VALUES where the rule says a threshold and a
boolean.** `F9`'s bank-slice departure (`9.6E-16` against `3.8E-16`) and `F10`'s assembled-solve
departure (`2.0E-14` against `3.9E-14`) are quantities whose true value is **zero**; `CLAUDE.md`
is explicit that *a number whose every digit is noise is a step counter wearing a physical name*
and must be emitted as the tolerance and a boolean. Both are inside their declared thresholds in
both runs, so nothing they gate moves — but they should never have been printable.

**(c) The residue is the descent manifold, and no rounding supplies an answer for it.** `C-0135`
records that where the active constraints are fewer than the free directions the optimal set is a
**manifold**, that rounding stabilises which branch is taken and **not** the point, and that the
right response is *report the residual rather than asserting byte-identity*. The 90th percentile
of a 60-realisation ensemble is an **order statistic** — it selects a realisation — so an ulp in a
hot reduction can change which one, and the coarse tier-2 objectives moved for that reason.

### What is NOT repaired here, and why

**No source was changed after the run.** Repairing (a) and (b) now would leave the committed
emitter unable to reproduce the committed artifact, which is the invariant `gpd/README.md` rests
on and is worse than the defect. The two repairs are filed as queue rows with their call sites
named, and this claim states plainly that its own `(P₁, D₀)` corner at `f = 0.26` and the three
Spearman readings are **determined to about three significant digits, not nine**.

**And the headline is untouched by all of it.** The `f = 0.30` arm — every number this claim leads
with — is bit-identical between the two runs.

## 13. Still open — named, not answered

- **The capped search** ([`CH-0279`](../challenges/CH-0279-the-search-has-a-ceiling-and-nobody-set-it.md)) — one argument to a function that already takes it, and it is the only route to a corner that is flat, admissible and jointly searched.
- **A larger `K`**, since the screen is binding at one arm and the joint winner ranked 6 of a top-6.
- **The 10-path cell, where freeing the placement LOSES out of sample** — the only such cell, and it is the one with the largest family per path.
- **A smoothed CVaR of a log-sum-exp**, which would let the distribution half use an adjoint gradient on the quantity the verdict is read on; `C-0212` priced it and this task inherits the pricing unchanged.
- **The best design inside `C-0060`'s two-level family, searched rather than projected into.**
- **The shared-body topology**, which is a change of topology and orthogonal to both factors measured here.
- **What a joint search does on ROUTE B**, whose turns carry 28 unpaired nucleotides.
- **`T-328` — route this study's twelve raw selections through `searchDecision`**, named in §14(a): all **twelve** of them, plus the raw `<` in `determinedRankFromBest`. The repair is provably scoped — `2 of 14` sites are already correct and they show the shape — and is **not** made here, because changing the emitter after the run would leave it unable to reproduce the committed artifact.
- **`T-329` — emit `F9`'s and `F10`'s identity residuals as a THRESHOLD and a BOOLEAN**, per §14(b), rather than as values whose every digit is noise.
