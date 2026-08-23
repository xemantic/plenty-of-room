# C-0201 — **ROUTE B'S RASTER TURN IS A LOAD, NOT A SPRING, AND IT IS A DISHING *SOURCE* WHERE ROUTE A'S TIE IS A *SINK*.** The tether's own stiffness is `0.22377084–0.919840405 pN/nm` — at most **`0.109313066`** of `k_θ` on the rim node's `d/2` arm and **`9.19840405E−5`** of the link penalty the lattice uses — so with its preload removed it moves the free tile `0.0501417315 → 0.0496660245`, a **`0.95 %`** move *toward* flatness. **What moves the tile is the chain's own tension**, `0.160569993–3.03288672 pN`, a self-equilibrated internal load in `C-0104`'s exact sense: no entry of the stiffness matrix moves, the field is **linear** in it, and its projection is `f·unitZ` on the link gradient, so the **nine in-plane turns contribute exactly zero** and the **fifty through-thickness ones** carry it. With the preload the free tile reads **`0.11296458`**, past `T-5b`'s `0.10`. Graded on `C-0167`'s own 64 coupled cells, the same stations, the same distributions and the same 4 000-realisation stream, **all four tethered states read `0 of 64` flat at the 90th percentile against `C-0180`'s tied `2 of 64`, the tether is a dishing source at `64 of 64` cells at the built allowance and `248 of 256` over all four tethered states** (median per-realisation ratio `1.0046118–1.76745293` at the built allowance), and **the verdict does not move over four decades of `k_link`**. **The FREE tile straddles the tolerance on an AZIMUTH convention nobody has measured** — `0.0569815008` at the aligned azimuth and the softest chain against `0.166312182` at the worst and the stiffest, `24 of 36` bracket corners flat. `CH-0251` is **REFUTED** by `C-0200` this same iteration, so **this is the arm the built object occupies** and `C-0175` §9's `1.12×`, `C-0180` §4's `2 of 64` and `C-0190`'s `17.1428571°` are all about the design nobody has folded. And the width route B forces is the larger finding: at the built allowance a uniform honeycomb row is **`92 bp = 31.28 nm`** on M13mp18 and **`106 bp = 36.04 nm`** on p8064 against §3's `40 nm` — flat, and too small

| | |
|---|---|
| **Task** | [`T-299`](../tasks/T-299-tethered-raster-turn.md), raised by [`C-0193`](C-0193-the-built-turn-is-a-tether.md) §11 and [`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md) (`T-296`) |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (the same beam-and-bond lattice, the same exact Woodbury coupling surrogate and the same measured-incorporation dropout ensemble, with 59 freely-jointed tethers in place of 59 covalent ties) **+ logical** (an exact bit-identity between the empty-tether lattice and the object `C-0167` measured, a closed-form freely-jointed-chain law, an exact geometric decomposition of the element onto the model's own coordinates, and exact integer scaffold arithmetic) **+ literature** (`C-0193`'s and `C-0200`'s reading of the built precedent, and `T-230`'s ssDNA Kuhn and contour brackets) |
| **Verdict** | **PASS on all seven predicates. Of the nine declared falsifiers `F5` FIRED — declared open, and its firing IS the finding.** `F1`, `F2`, `F3` and `F6` did not fire; `F4`, `F7`, `F8` and `F9`, all declared open, did not fire either. Raises [`CH-0254`](../challenges/CH-0254-the-tether-was-priced-as-a-stiffness.md) against `C-0193` §11's and `CH-0247`'s statement of what is open |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The element is a **linearisation about the built, taut state**, at a stated span, loop length and `(b, c)` corner, and every number is quoted with all four |
| **Provenance** | [`gpd/results/T-299-tethered-raster-turn-regrade.json`](../results/T-299-tethered-raster-turn-regrade.json), written by [`tile/HoneycombTetheredRegradeStudy.kt`](../../src/main/kotlin/tile/HoneycombTetheredRegradeStudy.kt) (**new**). The element is a **pure addition** to `HoneycombGrillage`: one optional constructor argument defaulting to the empty list, and the lattice with it empty is **bit-identical** in `assembleLoad` over all 4 320 degrees of freedom. `tile/HoneycombRasterTurnTethers.kt` and the grillage addition are covered by **24 named tests** written first and watched fail, and by a **14-mutation** harness retained at [`gpd/data/T-299-mutation/mutate.py`](../data/T-299-mutation/mutate.py) — **0 survivors**, after a first run whose 3 survivors were three real gaps (§8). The result file is **byte-identical across two independent runs** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `C-0022`'s design state — 10 nm gap, `0.192 V`, its solved collar read from `T-3b`; cross-section **`10 × 6`**, block extent **116 bp**, raster **102 / 109** (`C-0151`, drawable); `d` = **2.536 nm** (SAXS), rise `0.34 nm/bp`, phosphate radius **`0.908637858 nm`** (`T-71`, measured); ssDNA Kuhn **2.10–2.84 nm** (zero force) with the **inextensible** contour **0.65–0.70 nm/nt** that travels with it; **`k_link = 1e4 pN/nm`** at every headline cell, and swept to `C-0194`'s span-law `41.4338953` |
| **Consumes** | [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md)/`T-263` (the 64 cells, **reproduced at `1e−8` on all 128 committed values**), [`C-0175`](C-0175-drawable-raster-rim.md)/`T-254` and [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md)/`T-279` (route A, **reproduced**), [`C-0193`](C-0193-the-built-turn-is-a-tether.md)/`T-296` (the tensions, **reproduced**), [`C-0200`](C-0200-the-file-draws-and-the-table-orders.md)/`T-302` (the `12 / 16` split), [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md)/`T-230` (the FJC law and both spans, **reproduced**), [`C-0194`](C-0194-the-common-mode-is-the-link.md)/`T-297` (the `d/2` arm and the span-law link), [`C-0022`](C-0022-tile-edge-load-profile.md)/`T-3b` |
| **Raises** | [`CH-0254`](../challenges/CH-0254-the-tether-was-priced-as-a-stiffness.md) |

---

## The claim, in four lines

The corpus's raster-turn tie is **route A**, a covalent crossover with zero unpaired scaffold.
`C-0200` has just shown that the 2009 staple **order** buys **route B**, whose turn is an ssDNA
chain — so the graded design and the built one are different objects.

Route B's turn has essentially **no stiffness** and a real **preload**, and the preload is what
moves the tile: route A's tie is a dishing **sink** and route B's tether is a dishing **source**.

**The two routes differ in the SIGN of what the turn does, not only in its size.**

---

## 1. The cheap bound ran first, it answered the question it was asked, and the question was wrong

`T-299`'s Plan states the cheap bound in as many words: the tether's secant and tangent against
`k_θ` on the arm the rim node offers. It is two multiplications and no solve, and here it is over
the whole zero-force Kuhn bracket, the whole inextensible contour bracket, all three azimuths and
all three loop lengths a real scaffold affords:

| quantity | over the whole bracket |
|---|---|
| tension `f` | **`0.160569993–3.03288672 pN`** |
| secant `f/x` | `0.223409723–0.696690704 pN/nm` |
| tangent `df/dx` | **`0.22377084–0.919840405 pN/nm`** |

against everything the element sits beside:

| comparand | value | the tether is at most |
|---|---|---|
| `k_θ` on the rim node's own `d/2` arm | `8.4147343 pN/nm` | **`0.109313066`** |
| `Gen1Tile.crossoverInPlaneStiffness()` | `64.7058824 pN/nm` | `0.0142157153` |
| `C-0194`'s span-law link `T/g` | `41.4338953 pN/nm` | `0.0222001914` |
| `RIGID_LINK_STIFFNESS`, which the lattice uses | `1e4 pN/nm` | **`9.19840405E−5`** |

**So the cheap bound predicts *"arithmetically no element at all"*, and it is right about the
stiffness and wrong about the answer.** Measured, with the tether's stiffness present and its
preload removed, the free tile moves `0.0501417315 → 0.0496660245` — `0.95 %`, and *toward*
flatness — and over all 36 corners of the bracket the stiffness-only free tile spans
`0.0491249255–0.0498313632`, never leaving the untied lattice's own neighbourhood.

That is [`CH-0254`](../challenges/CH-0254-the-tether-was-priced-as-a-stiffness.md).

## 2. The element, derived rather than chosen

A chain held at end-to-end distance `x` carries `f(x) = (k_BT/b)·L⁻¹(x/L_c)` along its own line
and always **pulls**. Linearised about that taut state a central-force element is
`K = (df/dx)·n̂n̂ᵀ + (f/x)·(I − n̂n̂ᵀ)` — the tangent along the chain and the secant transverse to
it, which is the geometric stiffness of a taut cable.

`HoneycombGrillage` has coordinates for the relative **normal** displacement (its link residual,
arm `d/2·unitY`) and the relative **axial** one (its slip residual, arm `d/2·unitZ`), and **no
in-plane transverse coordinate**, so `δ_y ≡ 0` by construction rather than by neglect. With
`n̂ = (unitY, unitZ, 0)` the decomposition collapses to two scalars on the grillage's two existing
gradients:

```
E = ½[(df/dx)·unitZ² + (f/x)·unitY²]·δ_ζ² + ½(f/x)·δ_s²
```

It carries **no dihedral term at all** — a freely-jointed chain transmits a force and no moment —
and that, not its softness, is the qualitative difference between the two routes.

The connector arm is `d/2·unitY`, `C-0194`'s theorem rather than a fitted parameter: it is the
only arm annihilating the linearised rigid roll `Φ ≡ α`, `W = α y`. An arm of zero would charge
energy to a rigid rotation of the block, which is `CLAUDE.md`'s frame-indifference trap. **`F3` did
not fire**: the whole preload vector does `0.0 pN·nm` of work on a 1 mrad rigid roll.

## 3. The preload is a LOAD, and it is route B's analogue of route A's prestrain

`f > 0` at every `x > 0`, so the built state is **taut** and the chain applies a self-equilibrated
pull between its two rim nodes. `C-0104`'s rule applies verbatim — it changes **no** entry of the
stiffness matrix (asserted band-entry by band-entry), the field is **exactly linear** in it
(`1e−9` over a threefold scaling), and every influence function is taken on `withoutPrestrain`.

`δ|Δ| = n̂·δ⃗`, and with `δ_y ≡ 0` that is `−unitZ` times the link residual, so the load is
`+f·unitZ` times the link gradient. **The nine in-plane turns therefore contribute exactly zero
preload** — their pull is entirely along `y`, a direction this lattice has no coordinate for —
and the **fifty** through-thickness ones carry it. That is a statement about the model and not
about the chain, and it is stated as one.

| | free tile at `f = 0.30`, over the stroke |
|---|---|
| untied (`C-0167`'s own) | **`0.0501417315`** |
| tied — route A's 59 covalent ties (`C-0175`) | **`0.0446459684`** — a **sink** |
| tethered, stiffness only, no preload | `0.0496660245` |
| tethered, 28 nt, aligned azimuth, softest chain | `0.0569815008` |
| tethered, `C-0200`'s ordered `24 / 32` split, worst azimuth, stiffest chain | `0.106960378` |
| tethered, 28 nt, worst azimuth, stiffest chain | **`0.11296458`** — a **source**, past `T-5b` |
| tethered, 15 nt (M13's affordance), worst azimuth, stiffest chain | `0.166312182` |

**`F5` was declared open and FIRED**, and its firing is the finding: the preload moves the free
tile by `0.00715013757–0.117187257` of the stroke over the 36 corners, against route A's own
`0.0764244991` triangle-inequality prestrain ceiling (`CH-0228`).

## 4. The 64 coupled cells, paired per realisation on the shared stream

Six turn states over `C-0167`'s four placements × four column counts × two distributions × two
composite fractions, on **one** dropout stream restricted per cell — 384 graded cells.

| turn state | flat at `p90` | tightest cell |
|---|---|---|
| untied — `C-0167`'s own | **`0 of 64`**, reproduced | `0.101931622` |
| tied — route A, `C-0180`'s own | **`2 of 64`**, reproduced | `0.0995744767` |
| tethered, 28 nt (the built allowance, p8064) | **`0 of 64`** | `0.125832006` |
| tethered, `C-0200`'s ordered `24 / 32` split | **`0 of 64`** | `0.120407872` |
| tethered, 28 nt, aligned azimuth and softest chain | **`0 of 64`** | `0.102016157` |
| tethered, 15 nt (M13's affordance) | **`0 of 64`** | `0.169005218` |

Paired per realisation against the untied lattice, the built tether's median ratio runs
**`1.0046118–1.76745293`** and it is a dishing **source at `64 of 64`** cells. **`F4` was declared
open and did not fire** against the untied lattice — `0 of 64` verdicts move, because both read
`0 of 64` — and **`2 of 64` move against the TIED one**: route B loses `C-0180`'s entire coupled
recovery.

Over **all four** tethered states the tether is a dishing source at `248 of 256` cells and the
median ratio spans `0.996617145–2.48458701`: the **eight** exceptions are all at the aligned
azimuth, where the tension is `0.160569993 pN` and the element is approaching its stiffness-only
limit — which §1 measures as very slightly *favourable*. The sign of what the turn does is set by
the **preload**, and it goes through zero exactly where the preload does.

**And every coupled cell is worse than the uncoupled tile**, `64 of 64`, which is `C-0109`
reproduced on a third lattice.

## 5. Which link stiffness it was read at — measured, not caveated

`C-0194`'s `F10` fired on exactly this axis: `C-0180`'s two recovered cells are flat at
`k_link ≥ 1000` and not at `100`. Swept at the tightest cell of each tethered state:

| `k_link` | ground | built 28 nt | ordered `24/32` | aligned/softest | 15 nt |
|---|---|---|---|---|---|
| `1e4` | `OrigamiGrillage`'s own penalty | `0.125832006` | `0.120407872` | `0.102016157` | `0.169005218` |
| `1e3` | one decade down | `0.12643693` | `0.120927986` | `0.102099186` | `0.169753109` |
| `1e2` | two decades down | `0.132431202` | `0.12655624` | `0.104075876` | `0.176454822` |
| `41.4338953` | `C-0194`'s span law `T/g` | `0.141095376` | `0.134804705` | `0.107339529` | `0.186515281` |

**No verdict moves anywhere on that ladder** — every cell is not flat at every link stiffness, over
`1.12×` in the worst column. So the link stiffness, which decides `C-0180`'s route-A recovery,
decides nothing here, and this claim needs no caveat about it.

## 6. The standoff, re-derived — and it is not a parameter of the element

`C-0193`'s *"`14 bp = 4.76 nm` outboard"* is `14 × 0.34 nm/bp`: the **duplex** rise applied to a
region that is **single-stranded**. What ssDNA has is a **contour**, and the contour that travels
with a zero-force Kuhn length is `0.65–0.70 nm/nt`.

| per-helix half-loop | contour | root-mean-square end to end | the rise reading | ratio |
|---|---|---|---|---|
| 12 nt (`C-0200`'s short half) | `7.8 nm` | `4.04722127–4.88426044 nm` | `4.08 nm` | `1.91176471×` |
| 14 nt (the `14 / 14` reading) | `9.1–9.8 nm` | `4.3714986–5.27560423 nm` | `4.76 nm` | `1.91–2.06×` |
| 16 nt (`C-0200`'s long half) | `10.4–11.2 nm` | `4.67332858–5.63985815 nm` | `5.44 nm` | `1.91–2.06×` |

It is out by `1.91176471–2.05882353×` **and it is the wrong kind of number**: a rise is a fixed
lattice step and a contour is an upper bound on an extension a coil never reaches.

**And none of it enters the element.** A freely-jointed chain joins the two **duplex** ends, both
at the same rim; where its own covalent link sits along it is a conformation the chain's statistics
integrate over. What fixes the element is the **nucleotide count** and the **anchor-to-anchor span
in the cross-section**, and the outboard distance is a parameter of neither.

`C-0200`'s `12 / 16` split is carried: the 59 turns are **two populations**, `24 nt` at one rim and
`32 nt` at the other, whose mean is `C-0193`'s `28` exactly. Which rim takes which half is a free
convention of that reading, exchanged by the axial sign, and it is stated rather than swept.

## 7. The width route B forces, and it is the larger finding

At the built allowance the scaffold buys `perHelix − 28` paired bases, and the row is that many
base pairs of duplex:

| scaffold | nt | paired per helix | uniform row | against §3's 40 nm | uncoupled dishing at `f = 0.30` |
|---|---|---|---|---|---|
| **M13mp18** | 7 249 | **92** | **`31.28 nm`** | **`−21.8 %`** | `0.0425678289`, **flat** |
| p7560 | 7 560 | 98 | `33.32 nm` | `−16.7 %` | `0.0422200543`, flat |
| **p8064** | 8 064 | **106** | **`36.04 nm`** | **`−9.9 %`** | `0.0451172785`, flat |

**`F7` was declared open and did not fire**: all three route-B rows are flat uncoupled. The tile is
not too *floppy*, it is too *small* — and `DECISIONS-FOR-NDI.md`'s width decision is asked about
route A's tile, which is a different object.

---

## 8. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a chain tension in pN from `k_BT/b`; a stiffness in pN/nm from `k_BT/(b·L_c·L′)`; the element resolved onto the lattice's own link and slip gradients, both dimensionless; a contour in nm/nt against a rise in nm/bp, kept apart by name; a scaffold budget in nucleotides against a scaffold in nucleotides | **PASS** |
| **2 — limiting cases** | `L′(0) = 1/3` exactly and `langevinDerivative` against a central difference of `langevin` at seven arguments, plus the large-argument asymptote **above** the guard and its continuity at one argument on each side; the tension against `turnLoopTension` at 12 corners; the tangent against a central difference of the tension in the span; an **empty** tether list bit-identical in `assembleLoad` and equal to `1e−10` under a **point load**; a **vanishing** stiffness and preload likewise; a stiffening tether driving its own link residual down by more than five per decade over two decades; equal rim chains reproducing the single-chain overload exactly | **PASS**, 24 named tests |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the FREE tethered lattice dishes `0.0`** and its mean deflection is `p/k_f` to `1e−9` — `CLAUDE.md`'s sharpest falsifier, re-run on a **preloaded** element, which is a new way to break frame indifference (**`F2` did not fire**); a rigid roll stores `< 1e−18 pN·nm` of tether energy and the preload does `0.0` work on it **PER ELEMENT as well as in the sum** (**`F3`**), which is not a refinement — the honeycomb's two through-thickness azimuths carry opposite `unitY`, so a per-element sign defect cancels exactly in the sum, and one duly survived the first mutation run; a lattice of **only** the nine in-plane tethers applies an identically **zero** preload; the preload changes **no** entry of the stiffness matrix, probed band-entry by band-entry; the field is exactly linear in the tension to `1e−9`; global force balance to `1e−9`; `withoutPrestrain` zeroes the tension and nothing else; the preload **shortens** every chain it can reach, and the 50 / 9 through-thickness / in-plane census | **PASS** |
| **4 — numerical convergence** | re-taken on the **deciding quantity at the deciding cell** — the `p90` of the tightest cell of each tethered state — over nested beam subdivisions 1 / 2 and the dishing sample grid 41 / 81 / 161, **with an untied control run** so that a departure can be attributed to the tether rather than to this study's code. Worst deciding departure **`0.0012`**, and `0 of 16` steps move a verdict (**`F8`**) | **PASS** |
| **5 — literature and upstream** | **twelve reproductions**, worst departure `1.5e−9`: `C-0167`'s two untied free tiles and **all 128 of its committed cells' `p90` and nominal at `1e−8`**; `C-0175`'s two tied free tiles; `C-0193`'s three published tensions; `C-0147`'s two spans; `C-0193`'s two uniform row widths. Every closed form is the corpus's own function, **called** rather than re-implemented | **PASS** |

### The nine declared falsifiers

| # | falsifier | fired |
|---|---|---|
| `F1` | an empty tether list is not bit-identical to `C-0167`'s object on `assembleLoad` | **no** — identical over all 4 320 DOF |
| `F2` | a uniform pressure on the free tethered lattice dishes more than `1e−9` | **no** — `0.0` |
| `F3` | the preload is not annihilated by a rigid roll, **per element** and in the sum | **no** — `0.0 pN·nm` at both readings, over all 59 |
| `F4` *(open)* | the tether moves a flatness verdict against the untied lattice | **no** — `0 of 64` |
| `F5` *(open)* | the preload moves a free-tile or a coupled verdict | **FIRED** — and its firing is the finding |
| `F6` | the stiff limit's link residual does not fall with `1/k` | **no** — a named test |
| `F7` *(open)* | route B's own row width refuses a flat free tile | **no** — `3 of 3` flat |
| `F8` *(open)* | a deciding cell loses its verdict under its own convergence axes | **no** — `0 of 16` |
| `F9` *(open)* | the preload's rim closure exceeds the steric slack | **no** — worst `0.549926604 nm` against `0.718724283 nm` |

### The mutation test, and its three survivors were three real gaps

`C-0161`'s standard applied to a Kotlin element rather than to a Python predicate: **14 mutations,
every one of which must fail a NAMED test**, with the **unmutated copy run first** and its failures
subtracted (`CH-0237`), and `find src -name '<file>.kt'` asserted to return **exactly one** path for
both subjects (`C-0190`'s stray-copy trap). The harness is retained at
[`gpd/data/T-299-mutation/mutate.py`](../data/T-299-mutation/mutate.py).

**First run: 11 killed, `3 SURVIVED`** — and none of the three was a missing test of a rule already
stated. Each was a **fixture that could not discriminate**:

| mutation | why it survived | the repair |
|---|---|---|
| the Langevin derivative's large-argument branch scaled by two | `u > 350` is **unreachable at every state this study occupies** — the largest `x/L_c` in the whole bracket is `0.446`, i.e. `u ≈ 1.5` — so the guard exists only to make the function total | assert the asymptote **at** an argument above the guard, and its continuity **at one argument** against the same closed form on each side. Comparing `L′(349.9)` with `L′(350.1)` is not a continuity check: `1/u²` itself moves `1.1e−3` between them, which is the whole width of the check and none of its content |
| the preload's `unitZ` projection dropped, so the nine in-plane turns are loaded | the existing test read the **element record** (`tension × unitZ`) and never the **load vector** the mutation lives in | a lattice of **only** the nine in-plane tethers must have an identically zero preload vector |
| the preload's second roll arm's sign flipped, breaking frame indifference | **the honeycomb's two through-thickness azimuths carry opposite `unitY`, so a per-element sign defect cancels EXACTLY in the whole-lattice sum** | assert the rigid-roll work **per element**, on a lattice carrying one tether — and `F3` of this study is now taken that way too, over all 59 |

**After the three repairs: 14 mutations, `0` survivors.** The third is the one worth carrying: a
conservation test taken on a **sum** cannot see a per-element defect the lattice's own symmetry
annihilates, and only a mutation can tell you which of the two you have written.

---

## 9. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated. No folding
  experiment is reported and this repository cannot run one.
- **The element is a LINEARISATION about the built, taut state**, at a stated span, loop length and
  `(b, c)` corner. It is **one-sided** — a chain pulls and does not push — and the compressive
  branch is not reached: it would need the two rim nodes to close by the whole span, `2.5–4.4 nm`,
  against solved deflections four orders smaller.
- **The lattice carries no steric floor between two duplexes.** `F9` did not fire at any of the 36
  corners — the worst closure is `0.549926604 nm` against `0.718724283 nm` of slack before
  backbone contact on `T-71`'s **measured** phosphate radius — but the margin is `1.31×`, and
  `CLAUDE.md` records that the measured DNA–DNA hydration force moves `0.24 nm` per e-fold at the
  tight end. A stiffer chain than any in this bracket would need the floor.
- **The AZIMUTH is a bracket here and it is a DETERMINED quantity of a specified design.** The
  chain's two anchors are the phosphates of the last paired base on each helix, whose azimuths are
  fixed by the base-pair index and the lattice phase; `C-0147` and `C-0193` bracket the span
  because they were bounding **reach**, where a bracket is the right instrument. For the
  **element** it is not, and the bracket straddles `T-5b` at the free tile. **Determining it is
  the highest-value follow-up this claim names.**
- **The tether's anchor is taken at the beam AXIS with the frame-indifferent `d/2` arm**, which is
  `C-0194`'s theorem for a covalent link rather than a measurement of where a chain attaches. A
  phosphate-radius arm would add a roll coupling of order `(f/x)·r_P²`, under three per cent of
  `k_θ`; that is priced and not measured.
- **The 14 unpaired bases at each helix end also remove duplex from the beam.** The lattice models
  full-length beams at the block extent; the ordered object's duplex window is 98 bp. The width
  rows price that as a **row length** and the coupled cells do not carry it.
- `k_θ` is `Gen1Tile`'s **square-lattice-fitted** constant and `k_s` a construction; no honeycomb
  measurement of either exists in this repository.
- The lattice carries **no across-helix parallel-axis term**, so its `D_⊥` is the independent one
  and a lower bound; the composite fraction enters as a smeared multiplier on `k_θ`.
- The dropout statistics are measured on a single-layer Rothemund rectangle and only the
  **profile** transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers or its turns.
- **Nothing here re-opens the raster, the cross-section, the placement search or the distribution
  rule.** The stations are `C-0151`'s and the distributions are `C-0058`'s two.

## 10. Still open — named, not answered

- **The azimuth.** It is determinable from the design's own lattice phase and it straddles the
  tolerance; §9 above states why a bracket is the wrong instrument for it here.
- **Which rim takes `C-0200`'s 24 nt half.** A free convention of that reading, not swept.
- **Whether the recommended `10 × 6` block needs an attachment coupling at all.** Every coupled
  cell graded here is worse than the uncoupled tile, at `64 of 64`.
- **Whether route B's tile is gradable at §3's footprint at all**, and what that does to
  `DECISIONS-FOR-NDI.md`'s width decision, which is asked about route A's tile.
- **What the 98 bp duplex window does to the coupled cells**, as against the 116 bp block extent
  the comparison is controlled at.

The first is `T-304`, filed **HIGH**: it is the single largest unknown here and it is a lattice
arithmetic rather than a measurement. The mutation harness's home is `T-305`.
