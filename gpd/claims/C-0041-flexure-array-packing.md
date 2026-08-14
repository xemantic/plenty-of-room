# C-0041 — The array does not pack, and the obstruction is not area but topology: the attachment grid's across-helix pitch IS one duplex and its along-helix pitch is under the span, so 45 flexures are unrealisable at any level count and on any body — the Gen-1 tile carries exactly FIFTEEN

| | |
|---|---|
| **Task** | [`T-96`](../tasks/T-96-flexure-array-packing.md), which is [`T-31`](../../TASKS.md) with a plan-view constraint attached |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Verification type** | **logical** (exact plane geometry on measured lattice constants, with no free parameter and no mesh) **+ in-silico** (`C-0030`'s span placement re-run at every candidate path count, the layouts solved as a blocking digraph, and the superstructure's connectivity solved by union-find on `C-0015`'s crossover lattice) |
| **Verdict** | **PASS, and `T-96` is SETTLED — the array does NOT pack, at EITHER of §3's strokes, and it does not fail on area.** The cheap bound is 2.39× the footprint in beam and **2.59×** with the standoff feet, which is exactly the size that invites *"stack it in three levels"* — and `C-0017`'s envelope has room for three, at `ℓ = 5.78 / 7.82 / 9.86 nm`. **Stacking buys nothing.** A standoff runs from the superstructure up to its own beam plane and a tie runs from that plane down to the tile, so **any two vertical members of the array share a height range whatever levels their beams sit at**: the clash is **level-independent**, and so is the verdict. The obstruction is a pair of lattice facts that meet nowhere: the attachment grid's **across-helix pitch is EXACTLY one duplex**, so beams in adjacent rows are tangent at zero tilt and mutually bury each other's ties at any other — 14 mutually blocking pairs at every angle tested, from 0.001 rad up; and its **along-helix pitch, 13.33 nm, is under the span plus a duplex, 34.51 nm**, so the three beams of a row bury each other's standoff feet. **The 3 × 15 array is unrealisable at 0 of 720 orientations, at any level count, and on a body of any size; so is 2 × 15.** What the Gen-1 tile carries is **exactly fifteen** — one flexure per duplex, one column, span **21.44 nm = 63 bp**, `C-0026`'s one-attachment-row-per-duplex scheme with `m = 1`, feasible at **exactly one of 720 orientations** and that one exactly parallel to the helices. **The two clauses of §3 then differ in KIND.** At the **acceptable 3 nm** stroke the binding variable is the **path count**, the threshold is **45 → 15**, and it costs nothing against any standing allowable: 6.67 pN per path against the 10 pN unzip allowable, 2.16× of buckling margin (1.71× on the measured rigidity), assembled tangent 25.49 pN/nm against `C-0023`'s 40 pN/nm ceiling. At the **desired 10 nm** stroke the count is bounded **below at 29** by the same allowable and **above at 15** by the packing, so **the window is empty on the specified tile** and the binding variable is the **footprint**: **2330 nm², 1.44× the Gen-1 tile, 1.20× in edge**. And one more thing `C-0035` could not see from an area: the tie apertures are **not 45 holes**, they are **m collinear slots**, and they **sever** the superstructure — the 3 × 15 grid cuts every one of the 15 duplexes into four pieces and leaves **18 disconnected components**, at every one of the 32 crossover phases. The remedy costs **8 bp — one duplex pitch — of stagger**, and it is free of every upstream claim. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED EITHER** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number: no duplex has been built standing normal to a single-layer sheet, every published out-of-plane base is a **pin**, and a duplex END has at most **two** covalent links. |
| **Provenance** | `gpd/results/T-96-flexure-array-packing.json`, produced by `anchoring.FlexureArrayPackingStudyKt`; **10 cheap-bound records, 3 orientation sweeps of 720 samples each, 18 layout records, 10 design records, 2 level records, 6 severance records, 12 body records, 17 convergence records, 12 upstream reproductions**; **35 gate-named tests in `FlexureArrayPackingTest`, 0 failures**, and the whole suite green on `tools/verify.sh`; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm tile (15 duplexes at the SAXS-measured 2.69 nm), footprint **1614 nm²**; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; `C-0017`'s 33.3333 pN/nm mandate and 10 nm envelope; `EI = 230 pN·nm²` with every buckling margin also on Fields et al.'s implied **172.906**; `C-0028`'s `B2` base at 261.2 pN·nm/rad; `ℓ = 8 nm` |
| **Consumes** | [`C-0035`](C-0035-flexure-mounting-sense.md) (the `Su` mounting, `restrainedBeamShape`, `apertureLength`, `midspanClearance`, `tieApertureArea`, `OrigamiDuplex` — **re-run as a library**, its 18.37 nm slot, its 2223 nm² and 326 nm² areas and its 1.39× fraction all reproduced), [`C-0030`](C-0030-coupled-standoff-joint.md) (`CoupledJointFlexure`, `coupledFlexureSpan`, `FlexureOrientation` — the whole placement pipeline re-run at ten path counts; its 31.82 nm span, 25.23 pN/nm tangent and 5.31 nm clearance reproduced), [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase.crossovers(2, favourable)`, `standoffBucklingLoad` — its 7.21 pN reproduced), [`C-0026`](C-0026-one-row-per-duplex.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md) (`attachmentGrid`, one row per duplex, the 32 bp interface spacing and the phase as a design variable), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the two-sided tie as a **duplex**, the path count set by the allowable), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the 10 nm envelope, the superstructure), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 10 pN unzip allowable), [`C-0022`](C-0022-tile-edge-load-profile.md) (a larger tile costs **less** at the rim), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`EI`, `S`, the rise, the SAXS interhelical distance) |
| **Raises** | [`CH-0055`](../challenges/CH-0055-the-forty-five-path-array-is-not-a-placement.md), against `C-0023`, `C-0030` and `C-0035` |
| **Challenged** | **[`CH-0060`](../challenges/CH-0060-the-stagger-is-not-free.md)** — the 8 bp stagger is not free. See the banner below. |

---

> ⚠️ **Scope note from [`CH-0060`](../challenges/CH-0060-the-stagger-is-not-free.md) (2026-08-14), raised by [`C-0047`](C-0047-single-column-flatness.md) (`T-101`), which is this claim's own open item 1.**
>
> **No count, span, orientation, packing verdict, area, severance result or allowable below changes.** What changes is
> Deliverable 4's *"free of every upstream claim"* and *"moves nothing else"*: the 8 bp stagger **breaks `C-0015`'s
> exact zero at FIRST order**, restoring **0.389 pN** of crossover force under a *uniform* load — 1.9× `C-0022`'s
> entire solved edge effect, and 26× inside the 10 pN unzip allowable — and costs **+2.19 %** of the peak dishing.
> It is admissible, necessary and cheap; it is not free.
>
> This claim's open item 1 is now answered by `C-0047`: the 1 × 15 scheme dishes **0.695 of the stroke** under
> `C-0022`'s solved load, **7.0×** `T-5b`'s convention and **2.26× worse than no coupling at all**. The stagger,
> swept past 8 bp as a *design variable*, buys **45 %** of that back at ±13.60 nm — but **this claim's own span
> caps it**: a flexure is a 21.44 nm beam centred on its own tie, so the half-stagger cannot exceed
> `edgeX/2 − span/2 = 9.28 nm` (**18.56 nm peak to peak, 54 bp**) without the beam overhanging the body. The best
> buildable stagger returns **22 %**, to 0.541 of the stroke, and still does not reach the tolerance.

---

## The claim, in one line

**Forty-five flexures of `C-0030`'s span cannot be placed, at any number of levels and on a body of any size, because the attachment grid that pins their midspans has an across-helix pitch of exactly one duplex and an along-helix pitch smaller than the span — and stacking cannot separate them, because their standoffs and ties are vertical members that share a height range whatever levels their beams occupy; the Gen-1 tile carries exactly fifteen, which delivers §3's acceptable clause and not its desired one, and buying the desired one costs a 1.44× larger tile rather than anything the flexure branch owns.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan view.** `x` runs **along** the tile's helices, `y` **across** them, origin at the tile centre. `z` is positive **upward**, away from the electrode; §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — the SAXS-measured single-layer interhelical distance — so two parallel duplexes at exactly `d` are **tangent and admissible**. That is the lattice condition, and it is deliberately the *loosest* defensible one: the steric diameter is 2.0 nm and a lattice packs at 2.69.
- **A vertical member — a standoff or a tie — is a disc of radius `d/2` in plan.**
- **A flexure occupies one rectangle `span × d` centred on its midspan, and owns three vertical members**: two standoff feet at its ends, one tie at its midspan (`C-0035`'s `Su`).
- **The midspans are pinned.** The tie is vertical, so a flexure's midspan sits over its own tile attachment: `C-0015`'s `m × 15` grid, `x_i = 40(i + ½)/m − 20`, `y_j = (j − 7)·2.69`.
- **Blocking**: `Y`'s beam covers one of `X`'s vertical members ⟹ `level(Y) > level(X)` **strictly**. A directed cycle — of which a mutual pair is the shortest case — is unrealisable at every level count.
- **Clash**: two vertical members closer than `d`. **Level-independent**, therefore fatal outright.

---

## The cheap bound, which ran first and pointed the expensive part at the topology

| `n` | span [nm] | bp | beam area [nm²] | with feet | / footprint | with feet |
|---|---|---|---|---|---|---|
| 10 | 18.52 | 54 | 498 | 571 | 0.309 | 0.354 |
| **15** | **21.44** | **63** | **865** | **974** | **0.536** | **0.603** |
| 20 | 23.77 | 70 | 1279 | 1424 | 0.792 | 0.882 |
| 25 | 25.76 | 76 | 1733 | 1913 | 1.073 | 1.186 |
| **29** | **27.18** | **80** | **2120** | **2330** | **1.313** | **1.443** |
| 34 | 28.77 | 85 | 2632 | 2878 | 1.631 | 1.783 |
| **45** | **31.82** | **94** | **3852** | **4178** | **2.387** | **2.588** |
| 60 | 35.28 | 104 | 5694 | 6128 | 3.528 | 3.797 |

> **Falsifier 1 (the area coming out below the footprint) did not fire, and the bound did not close the task either** — 2.59× is exactly the size that invites *"stack it in three levels"*, and `C-0017`'s envelope has room for three. **The cheap bound's whole contribution is to say where the expensive part has to go: not at the area, at the topology.** It is the same shape as `C-0035`'s own, whose cheap bound settled a sign and sent the expensive part to buildability.

---

## Deliverable 1 — the two lattice facts, and why they meet nowhere

**Fact A — the across-helix pitch IS one duplex.** `C-0015`'s rows sit at `2.69 nm`, which is exactly the width the beams occupy. Two beams in the same column and adjacent rows are therefore **tangent at zero tilt** — admissible, because that is what a lattice is — and at any other angle their perpendicular separation is `2.69 cos θ < 2.69`, so each covers the other's tie. **Mutual, at any tilt, at any level count.**

**Fact B — the along-helix pitch is under the span.** Two collinear beams need `|Δx| ≥ span + d`, not `≥ span`: their standoff feet sit on the beam **ends**, so beams laid end to end put two standoffs in the same place. At 45 paths that is **34.51 nm against a 13.33 nm column pitch**.

Fact A forces `θ = 0`. Fact B fails there. There is no third option.

| columns | `n` | span | feasible orientations of 720 | single-level | minimum mutual blocks | minimum clashes |
|---|---|---|---|---|---|---|
| **1** | **15** | **21.44** | **1** | **1** | **0** | **0** |
| 2 | 30 | 27.51 | **0** | 0 | 15 | 0 |
| **3** | **45** | **31.82** | **0** | **0** | **45** | **0** |

> **Falsifier 2 (some orientation packing 45 in one level) did not fire. Falsifier 3 (a level assignment existing) did not fire.** The sweep is **sample-count independent** over 180 → 2880.
>
> **And the one layout that works is feasible on a set of measure zero.** The single-column array is realisable at **1 of 720** orientations, and that one is exactly `θ = 0` — beams parallel to the attachment rows. That is a lattice statement and not a tolerance: the sheet's own helix direction supplies the angle **exactly**, which is the one circumstance under which a measure-zero window is a design rather than a defect.

### The layout table, at 0°, 5°, 11.7°, 23°, 45° and 90°

| columns | 0° | 5° | 11.7° | 23° | 45° | 90° |
|---|---|---|---|---|---|---|
| **1 × 15** | **PACKS, 1 level** | 14 mutual | 14 mutual | 14 mutual | 14 mutual | 38 clashes |
| 2 × 15 | 15 mutual | 57 mutual | 55 mutual | 51 mutual | 15 clashes | 56 clashes |
| **3 × 15** | **30 clashes** | 129 mutual | 28 clashes | 50 clashes | 22 clashes | 78 clashes |

---

## Deliverable 2 — stacking is not an escape, and the reason is not an area

`C-0017`'s envelope does admit three beam planes at §3's acceptable stroke — `ℓ = 5.78 / 7.82 / 9.86 nm`, i.e. **17 / 23 / 29 bp**, quantised to the rise, each clearing `C-0030`'s `ℓ ≥ stroke + 2.69` and separated by 2.04 nm against a 2.0 nm steric diameter. Three planes against a 2.59× area demand is exactly enough, and it is **irrelevant**:

> **A standoff runs from the superstructure up to its own beam plane and a tie runs from that plane down to the tile. Any two vertical members of the array therefore share a height range whatever levels their beams sit at.** A clash between them is **level-independent**, and no ordering, no level count and no larger body resolves it.

And at §3's **desired** 10 nm stroke the ladder is **empty**: no standoff inside `C-0017`'s 10 nm envelope clears its own midspan, which is `C-0030`'s `ℓ ≥ 12.69 nm` in another form.

---

## Deliverable 3 — what the Gen-1 tile actually carries, and both of §3's clauses

The packing-limited count is solved self-consistently — the span is re-placed at every candidate count, because `L ∝ n^(1/3)` — and it is **15**.

| `n` | span | assembled tangent | per path @3 nm | per path @10 nm | margin @3 (CanDo / Fields) | margin @10 | packs | verdict |
|---|---|---|---|---|---|---|---|---|
| 10 | 18.52 | 25.80 | **10.00** | 27.08 | 1.44 / 1.14 | 0.53 | yes | acceptable only, **at the allowable exactly** |
| **15** | **21.44** | **25.49** | **6.67** | **18.23** | **2.16 / 1.71** | 0.79 | **yes** | **PASS at §3's ACCEPTABLE stroke** |
| 20 | 23.77 | 25.34 | 5.00 | 13.86 | 2.88 / 2.28 | 1.04 | no | does not pack, and past the allowable at 10 nm |
| 28 | 26.83 | 25.24 | 3.57 | 10.15 | 4.04 / 3.19 | 1.42 | no | does not pack, and past the allowable at 10 nm |
| **29** | **27.18** | **25.23** | **3.45** | **9.83** | 4.18 / 3.30 | 1.47 | **no** | **clears the allowable at both strokes, does not pack** |
| 34 | 28.77 | 25.22 | 2.94 | 8.51 | 4.90 / 3.87 | 1.69 | no | clears both, does not pack |
| **45** | **31.82** | **25.23** | **2.22** | **6.63** | 6.49 / 5.12 | 2.18 | **no** | **`C-0030`'s design — does not pack** |

- **The compliance ceiling is never the constraint.** The assembled tangent sits at **25.2–25.8 pN/nm** at every count, 36 % below `C-0023`'s 40 pN/nm — because the span is *placed*, so the count moves the span and not the assembled stiffness. **`C-0003`'s discipline in a new place: a perturbation at specified stiffness is absorbed by the length.**
- **`CH-0029`'s floor is a bracket, not a number.** Read on `C-0017`'s **mandate secant** it is `33.333 × 10 / 10 = 34` paths exactly, which is the number in circulation. Read on the **element's own** delivered force it is **29**, because `C-0030`'s coupling **strain-softens** and delivers less at 10 nm than its 3 nm secant implies. Both are quoted; both are above 15.

### The two clauses, which differ in kind

| | §3's **acceptable** 3 nm | §3's **desired** 10 nm |
|---|---|---|
| binding variable | the **path count** | the **footprint** |
| threshold | **45 → 15** | tile area **≥ 2330 nm², 1.44×** (1.20× in edge) |
| what it costs | nothing against any standing allowable | a change to §3 |
| where the count sits | 15 packs, and 10 is the allowable's own floor | bounded **below at 29**, **above at 15** — **empty** |

---

## Deliverable 4 — the tie apertures are not 45 holes, they are `m` slots, and they SEVER the sheet

`C-0035` prices the tie apertures as **45 × 2.69² = 326 nm², 20.4 % of the footprint**, *"the irreducible part of `T-78`'s answer"*. **An area is not the question a sheet asks.** The holes lie on the attachment grid, whose across-helix pitch is **exactly one duplex**, so a column of ties removes a whole **line** of material.

| layout | helices | duplexes | holes | segments | crossovers | **components** | |
|---|---|---|---|---|---|---|---|
| 1 × 15 | along `x` | 15 | 15 | 30 | 42 | **2** | SEVERED |
| 1 × 15 | across `x` | 15 | 15 | 14 | 42 | **2** | SEVERED |
| 2 × 15 | along `x` | 15 | 30 | 45 | 35 | **17** | SEVERED |
| **3 × 15** | **along `x`** | **15** | **45** | **60** | **42** | **18** | **SEVERED** |
| 3 × 15 | across `x` | 15 | 45 | 12 | 28 | **4** | SEVERED |

> **Every duplex of the superstructure is cut into four pieces by the 3 × 15 grid, and the body falls into 18 disconnected components** — at every one of `C-0015`'s **32 crossover phases**, so it is not a phase artefact. With the helices running **across** `x` it is worse in kind rather than in count: three whole duplexes are obliterated and the sheet falls into four strips.
>
> **Falsifier 5 (the regular grid leaving the sheet connected) did not fire.**

### The remedy, and its price

**`C-0026` fixes the attachment ROWS — one per duplex — and says nothing about where along a row an attachment sits.** So staggering the tie column is free of every upstream claim. The smallest stagger that restores a single connected component is

&nbsp;&nbsp;&nbsp;&nbsp;**2.72 nm = 8 bp — one duplex pitch, quantised up to the rise** — alternating `±1.36 nm` row to row,

which leaves the beams comfortably inside the 40 nm edge (the 15-path array uses 21.44 of it) and moves nothing else: rows are disjoint strips, so a stagger cannot make two beams overlap.

---

## The nominal design that results

| | |
|---|---|
| **path count** | **15, not 45** — one flexure per duplex, `C-0026`'s scheme at `m = 1` |
| **grid** | **1 × 15**, ties **staggered ±1.36 nm (8 bp)** row to row so the superstructure survives |
| **span** | **21.44 nm = 63 bp** (was 31.82 nm = 94 bp at 45 paths) |
| **orientation** | **beams parallel to the tile's helices, exactly** — 1 of 720 orientations, and the lattice supplies it |
| **levels** | **one.** Stacking is unavailable in principle, not merely unnecessary |
| **mounting** | `C-0035`'s `Su`, unchanged: bases on the superstructure, standoffs away from the tile, midspans tied back down |
| **standoff** | 8.0 nm = 24 bp on `C-0028`'s `B2` base, unchanged |
| **assembled tangent** | **25.49 pN/nm**, 36 % below `C-0023`'s 40 pN/nm ceiling |
| **per-path force at §3's acceptable stroke** | **6.67 pN** against the 10 pN unzip allowable — 1.50× of margin |
| **buckling margin at that stroke** | **2.16×** on CanDo's rigidity, **1.71×** on Fields et al.'s measured one |
| **plan area used** | 974 nm² of 1614 — **60 %**, one column 21.44 nm wide in a 40 nm edge |
| **what it does NOT deliver** | §3's **desired** 10 nm stroke: 18.23 pN per path, **1.82× past** the unzip allowable |
| **what the desired stroke needs** | a tile of **≥ 2330 nm², 1.44× the Gen-1 footprint, 1.20× in edge** — e.g. **29.9 × 78.0 nm** at 1 × 29, or the same area at any aspect |

---

## The minimum body area is the beams' own, and it carries no aspect ratio

&nbsp;&nbsp;&nbsp;&nbsp;**`A_min = n(L(n) + d)·d`**, exactly.

Growing the tile **across** the helices adds rows; growing it **along** them adds columns; the product is the same either way. At 34 paths it is **2878 nm² at 1 × 34 and at 2 × 17 alike** — 31.5 × 91.5 nm against 62.9 × 45.7 nm, the same area to the last digit. At three and four columns the row count rounds up to 12 and 9, so the array carries 36 paths and the area follows the *count* exactly, 3047 nm². So *"which way to grow the tile"* is not a packing question at all, and the axis is free for whatever else decides it — the only thing the aspect changes is how many paths a **whole** number of rows and columns delivers.

**And `C-0022` says growing it is cheaper than it looks**: a larger tile costs **less** at the rim, +6.3 % instead of +14.7 %, because the fringing collar is a fixed 1.65 nm and scales as `1/L`. The 1.20× edge growth §3's desired stroke needs is therefore **favourable** at the edge. It is nevertheless a change to §3, and belongs to NDI rather than to this loop.

---

## The five verification gates

Executed as **35 gate-named tests** in `src/test/kotlin/anchoring/FlexureArrayPackingTest.kt`, 0 failures, with the whole suite green on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the array's plan area is a length squared, equals the **sum of the individual rectangles** and **doubles exactly** with the span; the whole packing verdict is **dimensionless** — scaling every length by 10 (edges, span, pitch) leaves the overlap count, the mutual-block count and the feasibility identical, at five orientations; unphysical arguments throw, including a negative span, a zero width, an empty array, a zero column count and a level envelope below the contact distance | **PASS** |
| **2 — limiting cases** | **a single flexure packs at one level in all 36 orientations** — the limiting case that reproduces `C-0030`; two collinear beams overlap below the span, **clash between the span and the span plus one duplex**, and clear above it, at three angles; two parallel beams tangent at **exactly** 2.69 nm neither overlap nor block; the generalised slot **reproduces `C-0035`'s `apertureLength` to `1e−12`** at 5 standoff lengths × 2 strokes; a stroke inside the clearance needs no slot and a zero clearance needs the **whole span**; the level ladder is quantised to the rise, respects the clearance and the envelope, and is **empty at the desired stroke** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the orientation sweep is **sample-count independent** over 180 → 2880 (`0.0`); the slot is **scan-step independent** over 64 → 4096 (`0.0`); the re-placed span is scan-step independent over 64 → 2048 at four path counts (`2.2e−16`); the severance component count is **identical at 4 crossover phases** and the *verdict* identical at all **32** | **PASS** |
| **5 — literature and upstream cross-check** | **`C-0030` reproduced** — span 31.82 nm (`2.9e−5`), assembled tangent 25.23 pN/nm (`1.1e−4`), free-head critical load 7.21 pN (`3.9e−4`), clearance 5.31 nm (`1.7e−16`); **`C-0035` reproduced** — slot 18.37 nm (`9.6e−5`), 2223 nm² (`2.1e−4`), 326 nm² (`1.2e−3`), 1.39× (`2.4e−4`); **`CH-0029`'s 34-path floor reproduced exactly** on the mandate secant (`0.0`); the placed span follows `n^(1/3)` to 2.9 %; **`C-0026`'s one-row-per-duplex asserted at every column count 1–15**; the SAXS 2.69 nm, the steric 2.0 nm, the 32 bp interface spacing and the 0.34 nm rise | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The verdict is invariant under a rigid rotation of the whole array** — every midspan and every beam turned together by 0.17, 0.9, `π/2` and 2.0 rad, at four internal orientations. The overlap count, the block count, the mutual count **and the level count** are unchanged. Nothing in a separating-axis test forces that; it is coordinate arithmetic throughout.
2. **Covering a tie always implies overlapping the beam that owns it** — a theorem, because a tie sits at the centre of its own beam and the beam's half-width **is** the tie's radius. Asserted over 24 orientations × all ordered pairs of the 3 × 15 array. **And the standoff case is deliberately excluded from it**, because a foot sits on its beam's *end* where half the disc lies outboard — which is exactly why the clash relation had to be added and is the finding of Deliverable 1's Fact B.
3. **Blocking between two collinear identical beams is mutual whichever is called lower** — asserted both ways at the design pitch.
4. **The severance union-find conserves material**: `1 ≤ components ≤ segments` and `segments ≥ duplexes` at three grid shapes.
5. **The closed-form column limit `⌊edgeX/(span + d)⌋` equals the solved layout's own answer**, which knows nothing of it — and the same-row overlap threshold angle equals the closed-form `asin(d/pitch)` to either side.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the beam area coming out **below** the footprint | **no** | 2.39× in beam, 2.59× with the feet — and it did **not** close the task either |
| 2 | some orientation packing 45 in **one** level | **no** | 0 of 720, at three sample counts |
| 3 | a level assignment existing at 2 or 3 levels inside the envelope | **no** | and the reason is not the envelope: the clash is **level-independent** |
| 4 | the packing-limited count reaching **34** | **no** | it is **15**, and the two clauses of §3 therefore differ in kind |
| 5 | the regular tie grid leaving the superstructure **connected** | **no** | 18 components at every one of 32 phases |
| 6 | the re-placed spans failing to reproduce `C-0030` at 45 paths | **no** | `2.9e−5` on the span, `1.1e−4` on the tangent |

**A result that was not anticipated:** the obstruction that decides the answer — the **standoff feet** — is not the one the task was formulated around. `C-0035`'s framing is about the beams and their apertures; the beams' own bodies clear each other at 45 paths in three levels by area. What does not clear is the **legs**: an upper beam's standoffs must reach the superstructure through every plane below, and its neighbours' bodies are exactly where they must land. **The array does not fail to pack, it fails to stand up.**

**A second one:** the `1 × 15` layout is feasible at **exactly one** of 720 orientations. A measure-zero design window is normally a defect; here it is not, because the angle is `0` and the sheet's own helix direction supplies it exactly. **A lattice can hold a tolerance of zero.**

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number.
- **The plan model is a hard-body one.** Beams, standoffs and ties are rigid rectangles and discs at their nominal positions; no thermal excursion, no assembly tolerance and no out-of-plane bow is represented. Both are conservative in the same direction — a real array is *less* likely to pack, not more.
- **The exclusion width is the SAXS interhelical distance, 2.69 nm, not the 2.0 nm steric diameter.** That is the **loosest** defensible choice and it is deliberate: at 2.0 nm the collinear clearance falls from `L + 2.69` to `L + 2.0` and the column pitch condition is unchanged, because 13.33 nm is under both. **The verdict is 5× away from the choice.**
- **The beams of one level are not lattice-bonded to each other.** Each is a free duplex held only at its two standoff heads, which is what makes the orientation a continuous variable at all. `T-31`'s original question — whether crossovers *between* neighbouring flexures stiffen the array — is **not** answered here and is not affected: this claim shows the 45-beam array has no plan view, so there is nothing to couple.
- **The tilt analysis assumes all beams share one orientation.** Mixed orientations within a level are not swept. They cannot rescue the 3 × 15 array — the mutual tie-blocking between the centre column and both neighbours holds for **any** orientation of the upper beam, because the *lower* beam's coverage is what does it — but they are not ruled out for other grid shapes, and a per-beam orientation is in any case at odds with `C-0028`'s base couple, whose **9.65× orientation benefit** requires the crossovers to lie across the beam axis on the superstructure's own lattice.
- **The level ladder uses `C-0017`'s 10 nm envelope and `C-0030`'s clearance.** Both are inherited; neither decides the verdict, which is level-independent.
- **The severance model is `C-0015`'s lattice with rigid connectivity** — a crossover either exists or does not, and a segment is connected or not. It says nothing about the **stiffness** of the perforated sheet, which is `T-68`'s question and is now larger.
- **The stagger remedy is checked for connectivity and for fit, not for flatness.** Moving the attachments along `x` changes `C-0015`'s grid *shape*, and `CH-0034` has already shown the flatness criterion saturates under the solved load between 45 and 225 attachments. **A 15-attachment scheme is below the range `CH-0034` examined and its dishing is not established here** — that is `T-5b`'s to re-run, and it is the largest open item this claim leaves.
- **Every span is `C-0030`'s coupled, favourable-mounting placement at §3's acceptable stroke, on the chord draw-in model**, so `T-43`'s 1.13–1.20× inconsistency travels unchanged, and the 10 nm columns are linear-theory extrapolations exactly as `C-0030`'s and `C-0035`'s are.
- **The yaw and lateral by-products of dropping to 15 paths are not computed here.** `C-0017` reports 70× of spare in the lateral condition and `C-0014`'s yaw margin scales as `Σ r²`, which a single column reduces; it is named as an open item rather than asserted.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| B-DNA steric diameter | 2.0 nm | **CITED**; the phosphate backbone *is* the surface |
| rise per base pair | 0.34 nm | **CITED** |
| crossover interface spacing | 32 bp | **CITED** via `C-0015` |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement**; every margin also on Fields et al.'s implied **172.906** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| `C-0028`'s `B2` base | 261.2 pN·nm/rad | **CITED**, and its critical load reproduced here |
| `C-0030`'s and `C-0035`'s design numbers | — | **CITED**, and reproduced as gate-5 tests |
| §3 parameters | 100 pN, 3 nm, 10 nm, 40 × 40 nm | **CITED** |
| `C-0022`'s +6.3 % / +14.7 % edge gain | — | **CITED**, not re-derived here |

Everything else — the plan geometry, the blocking and clash relations, the level assignment, every orientation sweep, every layout verdict, the packing-limited count, the self-consistent unzip floor, the minimum body area, the severance and the stagger — is **derived here in code**, with `C-0030`'s and `C-0035`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. ~~**The flatness of a 15-attachment scheme under the solved load.**~~ **ANSWERED by [`C-0047`](C-0047-single-column-flatness.md) (`T-101`): 0.695 of the stroke, 7.0× `T-5b`'s convention, 3.2× `C-0015`'s 3 × 15 and 2.26× worse than no coupling at all — the criterion does not saturate below 45, it reverses below three columns.**
2. **The stiffness of a perforated superstructure.** Connectivity is restored by an 8 bp stagger; the sheet still loses `n` duplex-omission holes, and `T-68` asks what a compliant host does to `C-0028`'s base couple.
3. **The yaw and lateral by-products of a single attachment column.** `C-0014`/`C-0017`.
4. **Whether §3's tile may grow.** The desired stroke needs 1.44× the footprint, and `C-0022` says the rim is cheaper there. **A specification question, and the second one this branch has raised** — `T-95` is the first.
5. **`T-31`'s original question — array coupling — is not answered**, only shown to be moot at 45 paths. At 15 the beams are one duplex apart and free; whether they should be crossed over to each other is a live design choice.
6. **`C-0029`'s two-covalent-link ceiling on the standoff's base couple is untouched**, and remains the binding constraint on the whole standoff branch.

## Challenges

**Raises [`CH-0055`](../challenges/CH-0055-the-forty-five-path-array-is-not-a-placement.md)** against `C-0023`, `C-0030` and `C-0035`: the 45-path array on `C-0015`'s 3 × 15 grid has no plan view at any level count or body size, so *"45 load paths on `C-0015`'s 3 × 15 grid"* is a premise those claims carry and the geometry does not admit; and `C-0035`'s 326 nm² tie-aperture floor is an **area** where the question is **connectivity**.

**No number in `C-0030` or `C-0035` fails to reproduce**, and none of their per-element physics is disturbed — every one of them is re-run here at ten path counts and the assembled tangent moves by 1 % across the whole range. What is challenged is the **design point**.

**None stands against this claim.** The five ways it would fail:

1. **A tie that is not vertical.** An oblique tie would unpin the midspans from the attachment grid and the whole obstruction would dissolve. It also breaks `C-0035`'s `dδ/ds = ±1` identity, which assumes the tie transmits the bodies' separation change; an oblique rigid tie must rotate, and the tile cannot slide laterally to let it.
2. **A flexure tied somewhere other than its midspan.** The symmetric beam `C-0023`, `C-0025`, `C-0028` and `C-0030` all solve would have to be re-solved asymmetric, and its placement, its `c`, its draw-in and its buckling would all move.
3. **An attachment grid that is not one row per duplex.** `C-0026` shows 15 rows *is* that scheme; a grid on a coarser row pitch would relieve Fact A at the price of the exact zero `C-0026` establishes.
4. **A standoff that does not reach the superstructure** — a beam supported from above, or from a second superstructure level. That is a body nothing in §1, §3 or `C-0017` describes.
5. **A demonstration that two duplexes may sit closer than 2.69 nm in plan.** The verdict is 5× from that choice, so it would take a factor of five, not a correction.
