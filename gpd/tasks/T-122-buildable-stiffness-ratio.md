# T-122 — Can a 5:1 per-path coupling stiffness ratio be BUILT?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Raised by** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md), whose *Validity range* names it **the largest open item the claim leaves** — *"nothing here says a per-path stiffness can be BUILT to a prescribed value"* — and whose headline (the first flat Gen-1 tile in this programme) turns on it |
| **Verification type** | **logical** (an exact enumeration of the buildable settings of five catalogue elements over an integer design parameter — base pairs, nucleotides, hinges — which needs no mesh and has no free parameter) **+ in-silico** (`C-0058`'s Woodbury surrogate on `C-0009`'s grillage and `C-0006`'s plate, re-run under `C-0022`'s solved load, to price the quantised design and to bisect the scatter threshold) |
| **Units** | lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**, energy **pN·nm**; `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with **Mg²⁺** |
| **Maturity target** | **TRL 1–3.** Model-consistent and traceable. Nothing here is measured; no element in the catalogue has been built, and the out-of-plane motif every one of them stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |

---

## Formulate

### The question

`C-0058` produced the first flat Gen-1 tile in this programme —
dishing **0.0753** of the free-tile stroke under a one-parameter rule and **0.0544** under a full optimisation,
both inside `T-5b`'s 0.10, where `CH-0034`'s attachment-**count** axis saturates at 0.149 and never arrives.
The rule is simple enough to build to:
**the 34 stations within 6.7 nm of a tile edge carry 0.921 pN/nm and the 11 interior ones carry 0.184** —
a **5:1** ratio at the same mandated total of 33.3333 pN/nm.

But every distribution in `C-0058` assumes an *independently specifiable linear spring per station*,
which no claim in this corpus has ever had to supply.
`C-0030`'s flexure is a **span**; `C-0023`'s hinge is an **arm**; `C-0039`'s elastica is an **arm** and a **hinge count**;
`C-0023`'s antagonistic pair is a **contour**.
Every one of those is quantised by the DNA lattice —
and **this project has met exactly that trap before**:
`C-0023` found that a two-sided preload is a *mounting offset*, i.e. a length,
that DNA quantises it at **0.34 nm**,
and that the requirement asked for **0.041 nm** while the smallest buildable offset delivered **9.3× too much**.
A design that cannot set the quantity it needs is not a design.

**So: is 5:1 inside the range the quantised parameters reach, at both stiffness levels simultaneously, with everything else still satisfied?**

### The numeric target and the acceptance predicate

**Acceptance.** For each element of the catalogue — `C-0023`'s `E3` transverse duplex flexure, `E5` crossover-hinge flexure and `E4` antagonistic ssDNA pair; `C-0030`'s coupled-standoff flexure; `C-0039`'s exact-elastica arm — and for **both** of `C-0058`'s two stiffness levels simultaneously:

1. the **nearest buildable setting** of the element's own integer design parameter, the stiffness it realises, and the relative departure from the target;
2. the **stiffness granularity** at each target — the fractional step between adjacent settings — which is the quantity `C-0023`'s preload trap is a statement about;
3. the **realised ratio** `k_rim/k_interior` against 5, and the **realised total** against `C-0017`'s 33.3333 pN/nm mandate;
4. the **flatness verdict re-run on the quantised design** through `C-0058`'s own surrogate, against `T-5b`'s 0.10;
5. the **scatter threshold**: the relative stiffness scatter, per `C-0026`'s named patterns, at which the flatness verdict is lost — `T-45`'s unmeasured assembly tolerance answered as a *threshold* rather than guessed as a value — reported beside the ratio's own **population-overlap** bound `(R−1)/(R+1)`;
6. the **cost**, on the constraints that have closed other designs: the per-path unzip allowable and `C-0049`'s `n·a/s`; `C-0014`'s thermal force `√(k_BT k)/N`, which `C-0058` shows is **linear** in a path's share; stability on the **tangent** at the realised spans (`C-0032`/`CH-0042`, the realised coupling strain-*softens*, so two spans do not stay in a 5:1 ratio over the stroke); and the **packing** (`C-0041`: three columns are needed and the tile carries one).

**Falsifiable form.**

> **`BUILDABLE`** ⟺ there is an element of the catalogue and an integer setting of its design parameter at each of the two levels such that
> (a) the realised ratio lies inside `C-0058`'s own **flat window in the ratio**, `5 ≤ R ≤ 20` at the 6.70 nm collar;
> (b) the realised total meets `C-0017`'s mandate to within the total's own granularity;
> (c) the quantised design's peak dishing is still `< 0.10 ×` the free-tile stroke; and
> (d) no per-path allowable, stability floor or compliance ceiling is newly violated by either level.

If no element clears (a)–(d), the deliverable is the **threshold form** `CLAUDE.md`'s research practice asks for:
the granularity achievable and the largest ratio it supports, against the 5:1 the flat design wants.

### Geometry, sign and quantisation conventions, restated rather than inherited

- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre; `w` is positive **downward** (`T-5`, unchanged).
- The tile is **40.0 nm along `x`** and 15 duplexes at the SAXS-measured `d = 2.69 nm` across `y`, i.e. 40.35 nm.
- The **attachment grid** is `C-0026`'s `columns × 15`; the design point is `C-0015`'s **3 × 15**.
- The **load** is `C-0022`'s solved collar profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`
  and keyed on **`(concentration, gapHeight, appliedBias)`** — `CLAUDE.md`'s upstream gotcha, avoided by construction.
- The **coupling** is `n` springs to ground whose stiffnesses **sum** to `C-0017`'s 33.3333 pN/nm.
- **The quantum of a duplex length is the rise per base pair, 0.34 nm** (Douglas et al. 2009, cited).
  The quantum of a single-stranded contour is **0.65 nm per nucleotide** (Sim et al. 2012; Bosco et al. 2014, cited, and the convention travels with the Kuhn length).
  The quantum of a hinge count is **one crossover**; `C-0040`/`CH-0054` supply **1–2** per flexure at 45 paths.
- **A ladder is enumerated, not searched.** Every buildable setting between the stated integer bounds is evaluated and the nearest is reported, so there is no optimiser and no convergence parameter in deliverables 1–3.
- **Granularity** is the fractional stiffness step between adjacent integer settings, `|k(m+1) − k(m)|/k(m)`, evaluated at the setting nearest the target. For a pure power law `k ∝ p^e` it tends to `|e|·q/p`, which is the cheap bound.
- **Scatter** is a multiplicative relative amplitude `ε` applied to the nominal per-path stiffnesses by `C-0026`'s own `ScatterPattern`, **without renormalising the total** — a build tolerance does not know the mandate — with the resulting drift in the total reported.

---

## Plan

### The cheap bound, which runs first

**One division.** Every element in the catalogue whose compliance is bending has `k ∝ p^{−3}` or `k ∝ p^{−2}`,
so its fractional granularity at one base pair is `|e|·(0.34 nm)/p`.
At the spans and arms the two levels demand — of order 30 and 49 nm for `C-0030`'s flexure, 3.7 and 8.0 nm for `C-0023`'s hinge —
that is a few per cent, against a **flat window in the ratio that is a factor of four wide** (`5 ≤ R ≤ 20`).

**Declared falsifier.**
*If the granularity at either level exceeds the width of `C-0058`'s flat ratio window — i.e. if one step of the design parameter takes the ratio out of `[5, 20]` — then the ratio cannot be set, the answer is negative, and the deliverable is the threshold form.*
This is the exact shape of the test `C-0023`'s preload failed (0.041 nm asked against a 0.34 nm quantum),
and running it first costs one division per element.

**A second cheap bound, also one line.** The mandate is an equality on a **sum over 34 + 11 paths**,
and one path may be moved by one step independently of the others,
so the *total's* relative granularity is the per-path one **divided by the path count** —
which decides deliverable 3(b) before any solve.

### The expensive part, and why it is justified

Deliverables 4 and 5 need a dishing field, and that is `C-0058`'s pipeline.
It is **re-run as a library, not tabulated**: `latticeInfluenceSurrogate`/`plateInfluenceSurrogate` price a distribution in microseconds after one factorisation,
which is what makes a scatter *threshold* — a bisection over ε, per pattern — affordable at all.
No new physics is introduced; the surrogate is asserted against `C-0058`'s published 0.2182 and 0.0753 as the limiting case.

Deliverable 6's ratio drift needs `C-0030`'s coupled flexure evaluated at two spans over the stroke,
which is closed form, and `C-0039`'s elastica at two arms, which is a shooting solve and is therefore called **twice**, not swept.

### What would falsify this approach

1. **The granularity bound firing** — then no solve is needed and the answer is negative in the threshold form.
2. **The surrogate failing to reproduce `C-0058`'s uniform and rim numbers**, which would mean the pipeline is not the one the claim was written on.
3. **A uniform-ratio request failing to reproduce `C-0017`'s 45-path design** — `R = 1` must return 45 paths at 0.740741 pN/nm and `C-0030`'s own 31.82 nm span, exactly. This is the free limiting case and it is a test.
4. **The realised ratio being inside the window while the quantised design's dishing is outside `T-5b`'s 0.10** — which would mean the ratio is the wrong summary of the design and the per-path values are not interchangeable with it.

### The five gates

| gate | what will be checked |
|---|---|
| **1 — dimensional** | a bending stiffness is `EI` over a cubed length and doubling the span divides it by exactly 8; a hinge stiffness quarters at double arm; the granularity is dimensionless and equals `|e|q/p` in the small-quantum limit; a base-pair count is an integer and a length is its multiple of 0.34 nm; unphysical arguments throw |
| **2 — limiting cases** | **`R = 1` reproduces `C-0017`'s 45-path design exactly** (0.740741 pN/nm per path, `C-0030`'s 31.82 nm span); an infinitely fine quantum returns the target exactly; zero scatter returns `C-0058`'s own dishing; a ladder of one setting has no granularity and says so |
| **3 — symmetry and conservation** | the quantised total conserves the mandate to within the total's own granularity; `C-0026`'s **alternating-columns** scatter (along the helices) restores **exactly zero** crossover force where alternating rows does not — the symmetry statement, re-asserted on an *unequal* nominal; a point-reflected quantised design dishes identically on the centro-symmetric lattice |
| **4 — numerical convergence** | the dishing sampling grid 41/81/161 at the quantised design; the scatter-threshold bisection exits on the **bracket width**, and its own bracket is reported; the elastica arm's shooting tolerance |
| **5 — literature and upstream cross-check** | `C-0058`'s 0.2182 uniform and 0.0753 rim reproduced; `C-0030`'s 31.82 nm span, 25.23 pN/nm tangent and 5.31 nm clearance; `C-0023`'s 24.61 nm span, 4.11 nm arm and its **0.0409 nm against 0.34 nm** preload quantum, which is the trap this task is testing for; `C-0049`'s `perPathSecantCeiling`; `C-0014`'s `√(k_BT k)/n`; `C-0026`'s 0.883 pN per unit relative amplitude and its 17 % break-even; `C-0041`'s 15-path packing limit |
