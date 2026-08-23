# C-0193 — **THE ZERO-LOOP TURN IS REACHABLE, DRAWABLE AND LATTICE-LEGAL, AND NOBODY HAS FOLDED IT ON THE HONEYCOMB — AND THE FIGURE THAT SAYS SO ALSO SAYS WHERE THE BUILT COVALENT LINK ACTUALLY SITS: `14 bp = 4.76 nm` OUTBOARD OF THE DUPLEX END, ON BOTH HELICES.** Read off the built `10 × 6` block's own strand diagram, the scaffold occupies bases **14 → 140** of every helix — **126**, the paper's own allotment — and the staples occupy **28 → 126** — **98**, the paper's own paired count. So the scaffold **does** turn at the rim with no topological loopout, and the two **DUPLEX** ends it joins are **28** unpaired nucleotides apart, 14 on each side. `C-0175`'s tie is a covalent element between two duplex ends at `s = ±L/2`; the built one is not that object. **The loops' own stated purpose is aggregation, not closure** — *"Unpaired scaffold bases **often** are introduced at the ends of helices **to minimize undesired multimerization**"*, in the honeycomb paper and again in its square-lattice sibling — and the field's current tutorial calls a `4+` base poly-T loop **on the staples** *"tried-and-tested"*, which costs **zero scaffold**. What the 28 nt actually **buys** is freedom from caDNAno's `±5 bp` residue condition, because **an unpaired base has no azimuth**: that is how a lattice carrying both turn senses is folded at **one** uniform row length, and it is exactly why `C-0151`'s zero-loop raster has to be **two-length**. **The two designs are separated by the SCAFFOLD**: on the drawable `102 / 109` raster the built allowance needs `8 010 nt`, so M13mp18 is short by **761**, p7560 by **450**, and p8064 has **54** spare — inverted, M13 affords **15 nt** per turn, p7560 **20**, and p8064 **exactly the built 28**. **And a tether is not free either**: at M13's 15 nt the turn carries `2.03800431–3.03288672 pN` and stores `1.00857129–1.48373364 k_BT`, so 59 of them store `59.5057061–87.5402845 k_BT` — **`7.15756436–10.5296662×`** `C-0190`'s whole rigid-duplex ceiling for route A. **Verdict: `C-0175` §9, `C-0180` §4 and `C-0190` are correct about a DRAWABLE design that has not been demonstrated**, `0` of `186` unique records over `30` queries in `9` families reports the motif on this lattice, and `7 of 7` built cross-sections do the other thing

| | |
|---|---|
| **Task** | [`T-296`](../tasks/T-296-zero-loop-raster-turn.md), raised by [`C-0190`](C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`) §10 and §11 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **literature** (the built precedent's own accounting, its own strand **diagram**, and the loops' own stated purpose, all **read directly** — three of the five sources were already in `gpd/data/` and were not fetched at all — plus a recorded existence sweep) **+ logical** (a covalent reach bound on the **measured** backbone, exact integer scaffold arithmetic, and an exact freely-jointed-chain law, all closed forms) **+ in-silico** (parsing this repository's own committed `.sc` designs and the field's own reference generator) |
| **Verdict** | **PASS on all seven predicates. Of the seven declared falsifiers `F1`, `F2`, `F3` and `F5` did not fire, and the three declared OPEN — `F4`, `F6`, `F7` — did not fire either.** Raises [`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md) against [`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md)'s and `C-0175` §9's unqualified framing, and opens `T-299` for the re-grade it implies |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED** except the constants and the published designs. The drawability verdict is a statement about a **design file** and a **lattice rule**; the folding verdict is a statement about the **literature**. No folding experiment is reported here, and this repository cannot run one |
| **Provenance** | [`gpd/results/T-296-zero-loop-raster-turn.json`](../results/T-296-zero-loop-raster-turn.json), written by [`tools/T-296-emit-result.py`](../../tools/T-296-emit-result.py) (**new**, no Kotlin main source touched), **byte-identical across two independent runs**. Its freely-jointed-chain mirror carries **24 named assertions and one raise check** and is asserted against `T-230`'s **own committed records at the file's own emission precision**: `round9(mine)` **is** the committed literal at **252 of 252** fields. Sources and their manifest at [`gpd/data/T-296-sources/`](../data/T-296-sources/MANIFEST.md) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; rise **0.34 nm/bp**; honeycomb `d` = **2.536 nm** (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS); phosphate radius **0.908637858 nm** and intrastrand P···P step **0.66448058 ± 0.036162985 nm** C2′-endo (`T-71`, **measured**, 13 084 linkages) — all three **parsed out of the Kotlin sources that declare them**, not transcribed; ssDNA Kuhn **2.10–2.84 nm** (zero-force) with the **inextensible** contour **0.65–0.70 nm/nt** that travels with it; cross-section **`10 × 6`**, raster **102 / 109** (`C-0151`, drawable) |
| **Consumes** | [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md)/`T-230` (the reach bound and the FJC law, **re-derived and reproduced**), [`C-0151`](C-0151-closing-raster-selection.md)/`T-245` (the drawable raster's `6 330 / 919 / 15`, **read from its result file**), [`C-0175`](C-0175-drawable-raster-rim.md)/`T-254`, [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md)/`T-279`, [`C-0190`](C-0190-the-departure-is-common-mode-and-what-replaces-it.md)/`T-291` (the conditionality table, **read from their result files**), `Gen1Tile`, `MeasuredBackbone` |
| **Raises** | [`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md) |

---

## The claim, in three lines

Nothing covalent, nothing in the lattice rule and nothing in the design tool refuses a honeycomb
raster turn with zero unpaired nucleotides.

Nobody has folded one, and the seven blocks that exist leave **14** unpaired bases at **every**
helix end — for a reason their own papers state, and it is aggregation, not closure.

So the tie set is a property of a **route**, the route is **drawable and undemonstrated**, and
what it is conditional on is listed to the digit in §7.

---

## 1. The cheap bound, re-derived rather than inherited — and it does not refuse

`n` unpaired nucleotides between two anchoring phosphates make `n + 1` phosphodiester steps.
A turn of **zero** therefore reaches exactly one step — and a turn of zero **is** a scaffold
crossover, whose two backbone phosphates are antipodal on the line of centres at `d − 2r_P`.

| quantity | value | against |
|---|---|---|
| aligned span, `d − 2r_P` | **`0.718724283 nm`** | — |
| the measured C2′-endo step | `0.66448058 ± 0.036162985 nm` | 13 084 crystallographic linkages (`T-71`) |
| **distance from it** | **`+1.49997857 σ`** | inside the 99th percentile, `0.756744753 nm` |
| worst azimuth, `d + 2r_P` | `4.35327572 nm` | — |
| **reach bound there** | **6 nt** | against the built 28 |

Computed here from constants **parsed out of `Gen1Tile.kt` and `MeasuredBackbone.kt`**, and
reproducing `C-0147`'s own emitted figures at a worst departure of `2.5e−9` — which is that
file's own nine-digit emission precision and not a disagreement.
**`F1` did not fire: the question was never geometry.**

## 2. Nor does the tool — and the motif is the field's reference default on the other lattice

| design | grid | scaffold strands | scaffold domains | **loopouts** |
|---|---|---|---|---|
| `gpd/designs/gen1-block-honeycomb-10x6-102-109.sc` | **honeycomb** | 1 | **60** | **0** |
| `gpd/designs/gen1-sheet-square-15x112.sc` | square | 1 | 15 | 0 |
| `gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc` | square | 1 | 31 | 0 |
| `scadnano.origami_rectangle.create(8, 16)`, generated here | square | 1 | 15 | **0** |

`C-0151` shows the `102 / 109` raster closes on caDNAno's `±5 bp` rule with **zero** forced
crossovers, and the committed file above is that design with **no loopout anywhere**.
The last row is the **field's own** generator, not this repository's: on the single-layer square
lattice a raster turn **is** this motif, it is what Rothemund's rectangles are made of, and the
reference implementation emits it with no unpaired scaffold at all.
**`F2` did not fire.**

## 3. What the built block ACTUALLY does — read off its own strand diagram

The seven honeycomb blocks are one sentence's worth of accounting, and the sentence covers **all
seven**:

> *"The shapes were folded either from a **7560**-base scaffold into **60** parallel helices or
> from an **8064**-base scaffold into **64** parallel helices to create … combinations of
> **15 × 4, 10 × 6, 8 × 8, 6 × 10, 4 × 16, 3 × 20, 2 × 30**. **Each helix was allotted 126 bases
> of scaffold. Of those 126 bases, 98 were paired with complementary staples, and the remaining
> 28 bases were divided into front and rear unpaired loop fragments at the ends of each helix.**"*
> — the caDNAno paper, **read directly**

`60 × 126 = 7 560` and `64 × 126 = 8 064`, **both exact**: the allotment **is** the scaffold
divided by the helix count. **`F3` did not fire.**

**And the strand diagram says where the covalent link sits.** *Nature* **459**:414's
Supplementary **Figure S4** is the **monolith design schematic**, whose cross-section inset is
**ten rows of six helices** — the `10 × 6` block, which the caDNAno paper itself names as
*"`10 × 6` (analyzed independently in ref. 14)"*. Its base-pair axis is ticked every 7 bp from 0
to 140, and rendered at 300 dpi on both rims it reads:

| what | where | in nucleotides |
|---|---|---|
| the **scaffold** occupies | **14 → 140** | **126** — the allotment, exactly |
| the **staples** occupy | **28 → 126** | **98** — the paired count, exactly |
| the scaffold **turns** at | base **14** and base **140** | with **no topological loopout** |
| so each helix end carries | **14** unstapled bases | `4.76 nm` |
| and two **DUPLEX** ends are | **28** unpaired nucleotides apart | `18.2–19.6 nm` of ssDNA contour |

`98 + 28 = 126`. **The scaffold does turn without a loopout, and it is still not a covalent tie
between two duplex ends** — the link sits **14 bp = 4.76 nm outboard** of the duplex end on each
of the two helices it joins, and what stands between the two rim nodes is **ssDNA**.
`C-0147`'s 28 nt tether is the right object; the figure says where it sits.

## 4. And the 28 nt is a PURCHASE, not a necessity — and what it buys is the residue condition

**An unpaired base has no azimuth.** So caDNAno's `±5 bp` residue condition — which `C-0136`,
`C-0148` and `C-0151` spend three claims closing — **cannot bind a turn flanked by 14 unpaired
nucleotides at all.** That is exactly how a lattice carrying **both** turn senses (`C-0140`) is
folded with all sixty helices at **one** length, and it is exactly why `C-0151`'s zero-loop raster
has to be **two-length**.

**The built design buys freedom from the residue condition and pays 28 nt of scaffold for it.**
Route A pays no scaffold and closes the condition instead. Neither is free and the currencies are
different, which is the whole reason this is a fork and not a tolerance.

## 5. The loops' own stated purpose, and it is NOT closure

> *"Sometimes staple crossovers are removed at the edges of the shapes to allow adjustment of
> staple lengths to preferred values. **Unpaired scaffold bases *often* are introduced at the ends
> of helices to minimize undesired multimerization**, or else to accommodate later addition of
> connecting staple strands that mediate desired multimerization."*
> — Douglas et al., *Nature* **459**:414 (2009), Methods, **read directly**

The same sentence, nearly verbatim, is in the square-lattice sibling, which adds the alternative:

> *"**Alternatively, if a seam composed of scaffold crossovers is implemented on the inside of the
> structure, then a circular scaffold path can be accommodated without the need for the long
> unpaired loop.**"* … *"Target structures were designed so that **90–97 %** of the scaffold strand
> should be paired with staple strands."*
> — Ke et al., *J. Am. Chem. Soc.* **131**:15903 (2009), **read directly**

Three things follow and each is falsifiable.
**(a)** The purpose named is **aggregation**, not turn closure — so a design that discharges
aggregation another way owes nothing here. **`F5` did not fire.**
**(b)** The word is **"often"**. It is a practice, not a rule.
**(c)** The honeycomb blocks are `98/126` = **77.8 %** paired; the **same laboratory in the same
year** built multilayer square-lattice cuboids at **90–97 %** paired. **28 nt is the slack end of
the built multilayer family, not its centre.**

And the remedy has moved. The field's current tutorial:

> *"the exposed cylinder ends … are prone to reversible, low-energy stacking … it may lead to
> uncontrolled agglomeration. **A tried-and-tested of way to prevent stacking is to place a 4+ base
> poly-T loop on the staples when they jump between helices at the cylinder ends.**"*
> — *DNA Origami Design: A How-To Tutorial* (2024), **read directly**

**On the staples. Zero scaffold.** It is Rothemund's own measured 4-T remedy, which `CLAUDE.md`
already records, restated as current practice fifteen years later.

## 6. The existence sweep — and the answer is a NEGATIVE, recorded so it can be refuted

**30 queries in 9 named families**, retained in
[`gpd/data/T-296-sources/query.py`](../data/T-296-sources/query.py); 348 records, **186 unique**,
in `europepmc-queries.json`.

| statement | lattice | status |
|---|---|---|
| the covalent link a zero-loop turn needs is **reachable** | honeycomb | **derived**, on measured constants alone |
| a scaffold crossover at a **helix end**, i.e. a raster turn with no unpaired scaffold | square, single layer | **demonstrated**, and it is the field's reference default |
| a scaffold crossover between two adjacent **honeycomb** helices | honeycomb | **demonstrated**, at interior positions — the `±5 bp` rule is the method's own primitive |
| a rim turn with **no topological loopout** | honeycomb | **demonstrated** — and it does **not** make a covalent tie (§3) |
| **the conjunction: a honeycomb raster turn between two DUPLEX ends** | honeycomb | **NOT FOUND** |

**7** of the 186 unique records name the honeycomb lattice and **none** reports the motif; the one
design whose strand diagram was read puts 14 unpaired bases at every helix end.
**`F4` was declared open and did not fire.**
**One paper naming a honeycomb origami whose raster turns carry no unpaired scaffold refutes this,
and that is the whole of what it claims.**

## 7. The conditionality, quantified — deliverable 2, and it is read out of the committed files

| claim | on route A (zero loop) | on route B (the built tether) | what SURVIVES | what does NOT |
|---|---|---|---|---|
| **`C-0175` §9** — the 59 ties | the block dishes **`0.0446459684`** with them against **`0.0501417316`** without, ratio **`0.890395426`** | there are no ties: the free tile **is** `0.0501417316` | the untied column, which is `C-0167`'s own and was never in doubt | the **`1.12×`**, the `435 + 59` split, and every number taken on it |
| **`C-0175` §8 / `CH-0228`** — the allowed `8.57142857°` prestrain | a triangle-inequality ceiling of **`0.0764244991`** of the stroke over all 59 turns | no covalent tie carries an azimuth, so **the load does not exist** | nothing of the load | the ceiling, the 59-site census, the three swept sign assignments |
| **`C-0180` §4** — the coupled recovery | **2 of 64** flat at the 90th percentile | **0 of 64** — `C-0167`'s own | `C-0167`'s `0 of 64`, which the untied lattice already said | the two recovered cells, `0.0995744767` and `0.0998791032`, and the `0.902845544–0.988116016` median band |
| **`C-0190`** — the per-beam twist | `0 of 64` flat; free tile **`0.296735462`** at `f = 0.30` | a tether demands **no azimuth at either end**, so there is no roll to be common-mode and no twist to replace it | the geometry — `CH-0240`'s antipodality and the `u* = 1.37990892` stationary point | the `17.1428571°` demand, the `8.31368089 k_BT` ceiling, the `0 of 64` grading |
| **`C-0151` / `C-0148`** — the drawable raster | `102 / 109` is selected **because** it closes on `±5 bp` | no residue condition binds a turn through unpaired scaffold: **every** row length is admissible | the closure arithmetic, as a statement about route A | its status as a **selection** — on route B there is nothing to select against |

**`F7` was declared open and did not fire at the free-tile level**: `0.0501417316` untied against
`0.0446459684` tied, **both inside `T-5b`'s 0.10**. At the **coupled** level the verdict does move,
and that is `C-0180`'s own `2 of 64` against `0 of 64` read backwards — which is the point rather
than a surprise.

## 8. And the two designs are separated by the SCAFFOLD, on the raster this programme recommends

`C-0147`'s budget is written on a **uniform 112 bp** row; the design now recommended is `C-0151`'s
two-length `102 / 109`, whose paired total is **`6 330 nt`** — a raster that post-dates the claim
that would have priced it.

| scaffold | needs at the built 28 nt | spare | **largest loop it affords per turn** |
|---|---|---|---|
| **M13mp18**, 7 249 | **8 010** | **`−761`** | **15 nt** |
| p7560, 7 560 — the **60**-helix designs, `CH-0173`'s correction, `60 × 126` exactly | 8 010 | `−450` | 20 nt |
| **p8064**, 8 064 | 8 010 | **`+54`** | **28 nt — exactly the built allowance** |

**At the built allowance THIS PROGRAMME's recommended raster is a p8064 design and not an M13 one** — a statement about a Gen-1 tile nobody has folded, and not about which scaffold any 2009 block was folded from, which `CH-0173` settled at `60 × 126 = 7 560`.
`F6` was declared open and **did not fire**.
Inverted for a **uniform** row, route B's tile is narrow before any mechanics is run: `92 bp` =
`31.28 nm` on M13 (`−21.8 %` of §3's nominal), `98 bp` = `33.32 nm` on p7560, `106 bp` =
`36.04 nm` on p8064 — and only at the 6 nt **reach bound**, where the turn is fully extended, does
a uniform row reach `114 bp` = `38.76 nm` on M13.

**And a tether is not free either.** At the worst azimuth, over the whole zero-force Kuhn and
inextensible-contour bracket:

| loop per turn | tension [pN] | stored [`k_BT`] | over 59 turns [`k_BT`] | against `C-0190`'s route-A ceiling |
|---|---|---|---|---|
| **15** — M13's affordance | **`2.03800431–3.03288672`** | **`1.00857129–1.48373364`** | **`59.5057061–87.5402845`** | **`7.15756436–10.5296662×`** |
| 20 — p7560's | `1.44741624–2.13070196` | `0.737054595–1.07900034` | `43.4862211–63.6610198` | `5.23068201–7.65738072×` |
| 28 — the built | `1.00195245–1.46667915` | `0.518481856–0.7570064` | `30.5904295–44.6633776` | `3.67952895–5.37227471×` |

The comparand is `C-0190`'s **`8.31368089 k_BT`** rigid-duplex ceiling for route A's whole block;
the two are different channels and this is a **magnitude**, not an identity. Read in the host
sheet's own currency the 15 nt turn set is **`7.43849502–10.9429501`** crossover columns of the
`7.99969697 k_BT` `C-0079` measures — and the host sheet folds one column at a time.
**On the scaffold this programme uses, route B's turn set is the more expensive of the two in
stored energy.**

---

## 9. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **dimensional** | every span in nm against a step in nm; a scaffold budget in nucleotides against a scaffold in nucleotides; tension in pN and energy in `k_BT` from `k_BT/b` and `k_BT L_c/b` | consistent |
| **limiting cases** | `n = 0` **is** a scaffold crossover and must fall inside the measured step (it does, `+1.50 σ`); `langevin(0) = 0`; both guarded branches meet at their switches; `minimumUnpairedNucleotides(step, step) = 0` and `(step + ε, step) = 1` | 24 named assertions, 0 failures |
| **symmetry / conservation** | `98 + 28 = 126` from the figure **and** from the sentence; `60 × 126 = 7 560` and `64 × 126 = 8 064` exact; the four scaffold budgets and the four affordances are one division each and invert one another | closes |
| **numerical convergence** | the inverse Langevin is a **bisection**, where the bracket width **is** the error, at 200 halvings; there is no other iterative step, and the result file is **byte-identical across two independent runs** | settled |
| **literature cross-check** | five primary sources, **all read directly**, three of them already in `gpd/data/` and not fetched; the FJC mirror asserted against `T-230`'s **committed** records at that file's own emission precision — `round9(mine)` **is** the committed literal at **252 of 252** fields | agrees |

### The twelve reproductions

Every figure this claim inherits is reproduced from an independent implementation rather than
quoted: the aligned span, its `σ`, the worst-azimuth span, the 6 nt reach bound, the drawable
raster's `6 330` and `919`, `C-0151`'s own **15 nt**, `C-0140`'s **92 bp**, the built turn's
tension and stored energy at both ends of the bracket, and the paper's `98 + 28 = 126` and
`60 × 126 = 7 560`. **Worst relative departure `2.5e−9`, which is the emission precision of the
file being reproduced.**

### The seven declared falsifiers

| id | fires if | fired |
|---|---|---|
| **F1** | the `n = 0` span falls outside the measured step's 99th percentile | **no** |
| **F2** | the committed honeycomb `.sc` carries a loopout | **no** |
| **F3** | the `126 = 98 + 28` allotment is design-specific | **no** |
| **F4** *(open)* | a published honeycomb origami with zero-loop raster turns is found | **no** |
| **F5** | the primary source names turn **closure** as the loops' purpose | **no** |
| **F6** *(open)* | at the built allowance the drawable raster fits M13mp18 | **no** |
| **F7** *(open)* | removing the ties reverses a **free-tile** flatness verdict | **no** |

---

## 10. What this does NOT establish

- **TRL 1–3.** The drawability verdict is about a **design file** and a **lattice rule**; the
  folding verdict is about the **literature**. Neither is a folding experiment.
- **A negative existence result is only as strong as its query set.** The 30 strings are retained
  and one paper refutes it.
- **The reach bound reads the crossover span as `d − 2r_P`**, both backbones antipodal on the line
  of centres. That is `C-0147`'s convention and it is a **bracket end**: the measured interhelical
  distance is a **Bragg lattice constant**, and `CLAUDE.md` records that the *local* separation at
  a crossover is smaller. The other reading makes the span shorter and the turn easier, so the
  verdict has a **known sign**.
- **The scaffold budget assumes the loop is spent uniformly**, `L/2` at each helix end, which is
  how the built blocks spend theirs. A design paying only at the turns that need it affords more.
- **Figure S4's numbers are read off a rendered image**, because the SI PDF has no usable text
  layer. They are checked three ways — against the paper's own `126 = 98 + 28`, against
  `60 × 126 = 7 560`, and against the 7 bp tick spacing the honeycomb lattice imposes — and all
  three close.
- **Nothing here re-opens the raster, the cross-section, the placement search, the distribution
  rule, or any number of `C-0175`, `C-0180` or `C-0190`.** Every one of those is correct about
  route A. What is established is **which design they are about**.
- **This claim does not grade route B.** It prices its scaffold and its turn energy and says the
  mechanics has not been done. That is `T-299`.

## 11. Still open — named, not answered

- **What a 28 nt tether does to the block mechanically.** It is not a tie and it is not nothing:
  it is a one-sided entropic element between two duplex ends, `4.76 nm` outboard on each helix,
  and **no lattice in this repository carries one**. That is `T-299`, and until it is run the
  corpus has graded **one** of two designs.
- **Whether route B's tile is gradable at §3's footprint at all.** At the built allowance a
  uniform honeycomb row is `31.28 nm` on M13mp18 and `36.04 nm` on p8064, so route B and the
  40 nm nominal are in tension before any mechanics is run — and `DECISIONS-FOR-NDI.md`'s width
  decision is asked about route A's tile.
- **Whether a staple-side poly-T is available at every rim helix end of the recommended block.**
  `CLAUDE.md` records an anti-stacking remedy and a duplex-end **joint** competing for the same
  two strand termini; the recommended coupling attaches on the **face**, so the rim termini look
  free, but nothing here counts them.
- **Whether p8064 changes the recommendation.** It affords exactly the built 28 nt on the drawable
  raster **and** a wider uniform row on the tether one, and no claim in this corpus has priced a
  scaffold change against the design window.
