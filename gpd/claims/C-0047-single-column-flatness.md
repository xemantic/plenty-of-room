# C-0047 — Fifteen attachments do not make the Gen-1 tile flat, and below three columns a coupling is a NET DISHING SOURCE: `C-0041`'s realisable 1 × 15 scheme dishes 0.695 of the stroke under `C-0022`'s solved load — 7.0× `T-5b`'s convention, 3.2× `C-0015`'s 3 × 15, and 2.26× worse than having no coupling at all

| | |
|---|---|
| **Task** | [`T-101`](../tasks/T-101-single-column-flatness.md), which is `C-0041`'s *"Still open"* item 1 and which it names *"the largest open item this claim leaves"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage and `C-0006`'s continuum plate, both under `C-0022`'s **solved** electrostatic profile read from its own result file and keyed on concentration, gap **and bias**) **+ logical** (a closed-form Winkler bending length, four arithmetic operations, which settles the structure of the answer and locates the best repair before any matrix is assembled) |
| **Verdict** | **PASS on the predicate, and the answer is NEGATIVE — and it is not `CH-0034`'s saturation, it is a loss.** Under `C-0022`'s solved load the 1 × 15 grid dishes **0.695 of the free-tile stroke**, **7.0×** `T-5b`'s 10 % convention, against **0.218** for `C-0015`'s 45 as 3 × 15 and **0.223** for `C-0009`'s 64 as 8 × 8 at the same load case. `CH-0034` found the criterion **saturates** at 0.149 between 45 and 225 attachments; **below 45 it does not saturate at all**, and 15 sits **4.7×** above that floor. **And the fifteen-path coupling is a NET DISHING SOURCE**: `C-0022`'s free, uncoupled tile dishes **0.308** under the same load, so the 1 × 15 coupling is **2.26× worse than no coupling at all**. The break-even is at **three columns** — the count `C-0041` shows cannot be built. **The cheap bound saw all of it in four operations**: the along-helix Winkler bending length is **12.83 nm**, so one column's 40 nm pitch is **3.12** bending lengths while the 2.69 nm row pitch is **0.47** of the across-helix 5.71 nm — the sheet's 25.6× anisotropy through a fourth root, and `C-0015`'s three columns is exactly the last count whose pitch still falls inside one bending length (1.04). **The orientation is worth 1.08× in dishing and 16× in the load path**, and the angle the packing forces is the good one on both. **`C-0041`'s 8 bp stagger is not quite free** — it costs +2.19 % of the dishing and, more to the point, **breaks `C-0015`'s exact zero at FIRST order**, restoring 0.389 pN under a perfectly uniform load, 1.9× `C-0022`'s entire solved edge effect. **Swept as a design variable rather than a repair the stagger buys 45 % of the dishing back** at ±13.60 nm — the along-helix bending length to 6 % — **but that optimum overhangs the tile: a flexure is centred on its own tie, so the span caps the half-stagger at 9.28 nm (54 bp)**, and the best buildable stagger returns 22 %, to 0.541 of the stroke. Still 5.4× the convention. **`C-0022`'s 32.1 % lever/sensor split is NOT affected**: it is a rim property, unchanged; what moves is the *other* term, from 32 % of the dishing at 3 × 15 to **79 %** at 1 × 15. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the flexure motif this count belongs to is NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of the count itself. |
| **Provenance** | `gpd/results/T-101-single-column-flatness.json`, produced by `coupling.SingleColumnFlatnessStudyKt`; model in `src/main/kotlin/coupling/SingleColumnFlatness.kt`; **8 cheap-bound records, 77 solved flatness states, 24 stagger records, 7 column-sweep records, 21 solved-state records, 5 foundation records, 9 convergence records, 11 upstream reproductions**; **22 gate-named tests in `src/test/kotlin/coupling/SingleColumnFlatnessTest.kt`**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous buffer with **Mg²⁺**; 40.0 × 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; 8 symmetrically centred crossover columns (`T-10`); §3's 100 pN over the footprint; `C-0017`'s 33.3333 pN/nm mandate as `n` equal springs; `C-0001`'s foundation secant swept ×[0.25, 4] |
| **Consumes** | [`C-0041`](C-0041-flexure-array-packing.md) (the 1 × 15 count, the 8 bp stagger, the one feasible orientation), [`C-0026`](C-0026-one-row-per-duplex.md)/[`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md) (the pipeline **re-run as a library**, the saturation table extended downward, four of its numbers reproduced to `1e−9`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the **solved** edge profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`; its 32.1 % split and its 0.321 free-tile dishing), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 3 × 15 grid, the phase machinery, *"shapes, not counts"*), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage, the 8 × 8 answer, the 25.6× anisotropy), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the flatness convention, the rejected rigid-plate assumption), [`C-0017`](C-0017-output-coupling-stiffness.md) (the 33.3333 pN/nm mandate), [`C-0023`](C-0023-two-sided-coupling.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 10 pN unzip allowable) |
| **Raises** | [`CH-0060`](../challenges/CH-0060-the-stagger-is-not-free.md), against `C-0041` |

---

## The claim, in one line

**A fifteen-attachment scheme is not flat under the load `T-3b` solved, at any stagger, at any orientation, in any of `C-0022`'s twenty-one operating states and at any foundation stiffness — it dishes 0.695 of the free-tile stroke against `T-5b`'s 10 % convention; and below three attachment columns a distributed coupling stops buying flatness and starts selling it, because an attachment's influence patch on the polymer layer reaches 12.83 nm and it is being asked to cover 40.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous buffer with **Mg²⁺**.
- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre. `w` is positive **downward**, compressing the polymer layer (`T-5`, unchanged).
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane** — piston and both tilts removed — so a rigid translation and a rigid tilt cost nothing.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform* load at the same foundation stiffness: **4.907 nm** at `k_f × 1`. `C-0006`'s, `C-0015`'s and `C-0026`'s normaliser, unchanged, so every number here is directly comparable with theirs.
- **Flat** means peak dishing below **10 %** of that stroke — `T-5b`'s convention via `C-0015`, **a convention and not a physical threshold**.
- A **stagger** of `s` nm displaces even attachment rows by `+s/2` and odd rows by `−s/2` **along `x`**, quantised to the 0.34 nm rise. `C-0041`'s remedy is `s = 8 bp = 2.72 nm`.
- A collar **depth is negative for an enhancement**, which is the sign `C-0022` solved; the total load therefore exceeds 100 pN by `C-0022`'s edge gain, which is `C-0022`'s own convention and `C-0026`'s.

### The upstream gotcha, avoided by construction

`gpd/results/T-3b-*.json` carries **two** solved profiles per `(concentration, gap)` — one per operating bias. Every lookup here is keyed on **`(concentration, gapHeight, appliedBias)`** and errors if the triple is absent; the bias travels into the result file with every record.

---

## The cheap bound, which ran first, and which turned out to predict the repair as well as the failure

A duplex on the polymer layer is a beam of rigidity `EI` on a Winkler foundation of `k_f d` per unit length. Its **bending length** `ℓ = (4EI/k)^(1/4)` is the reach of one support's influence patch.

| direction | rigidity per length | `ℓ` [nm] | pitch [nm] | pitch / `ℓ` | patch covers its tributary |
|---|---|---|---|---|---|
| **across** the helices (crossover hinge, `k_θ d/p`) | 3.35 pN·nm | **5.71** | **2.69** | **0.47** | **yes** |
| along the helices, **1 column** | 230 pN·nm² | 12.83 | 40.00 | **3.12** | no |
| along the helices, 2 columns | — | 12.83 | 20.00 | 1.56 | no |
| along the helices, **3 columns** | — | 12.83 | **13.33** | **1.04** | **no, by 4 %** |
| along the helices, 4 columns | — | 12.83 | 10.00 | 0.78 | yes |
| along the helices, 15 columns | — | 12.83 | 2.67 | 0.21 | yes |

> **The rows were never the problem and the columns always were.** The 2.2× between the two bending lengths is the sheet's **25.6×** rigidity anisotropy seen through a fourth root, and it is `C-0015`'s *"shapes, not counts"* in closed form — **three columns is the last count whose pitch still falls inside one bending length**, which is why `C-0015`'s answer is three and not eight.
>
> **Falsifier 1 (the lattice putting 1 × 15 within 2× of 3 × 15) did not fire** — the measured ratio is 3.2×.

---

## Deliverable 1 — the flatness table, at `C-0022`'s design point (2 mM, 10 nm gap, 0.192 V), `k_f × 1`

| scheme | attachments | dishing [nm] | **dishing / stroke** | × the 10 % tolerance | flat? | lattice / plate | peak crossover [pN] | peak duplex shear [pN] |
|---|---|---|---|---|---|---|---|---|
| **free — no coupling at all** | **0** | 1.511 | **0.308** | 3.1 | no | 0.957 | 0.244 | 0.173 |
| **1 × 15 (`C-0041`'s count)** | **15** | **3.412** | **0.695** | **7.0** | **no** | **1.011** | **0.209** | **1.834** |
| **1 × 15 staggered 8 bp** | **15** | **3.486** | **0.710** | **7.1** | **no** | **1.024** | **0.592** | **1.721** |
| 15 × 1 (along one helix) | 15 | 3.698 | 0.754 | 7.5 | no | 1.006 | **3.283** | 1.624 |
| 2 × 15 | 30 | 1.719 | 0.350 | 3.5 | no | 0.974 | 0.165 | 0.987 |
| **3 × 15 (`C-0015`'s answer)** | **45** | **1.071** | **0.218** | **2.2** | **no** | **0.919** | **0.150** | **0.793** |
| 4 × 15 | 60 | 0.895 | 0.182 | 1.8 | no | 0.900 | 0.154 | 0.480 |
| 5 × 15 | 75 | 0.825 | 0.168 | 1.7 | no | 0.892 | 0.154 | 0.420 |
| 8 × 15 | 120 | 0.759 | 0.155 | 1.5 | no | 0.884 | 0.156 | 0.243 |
| **15 × 15 (`CH-0034`'s floor)** | **225** | **0.731** | **0.149** | **1.5** | **no** | **0.880** | **0.156** | **0.147** |
| **8 × 8 (`C-0009`'s answer)** | **64** | **1.094** | **0.223** | **2.2** | **no** | **1.038** | **1.494** | **0.616** |

**Under a uniform load** — the case in which the objective is identically zero at infinite count, kept as the falsifier:

| scheme | free | 1 × 15 | 1 × 15 stag. | 15 × 1 | 2 × 15 | **3 × 15** | 8 × 8 | 15 × 15 |
|---|---|---|---|---|---|---|---|---|
| dishing / stroke | **0.0000** | 0.426 | 0.440 | 0.643 | 0.135 | **0.049** | 0.052 | 0.0015 |
| flat? | yes | no | no | no | no | **yes** | yes | yes |

> **The free falsifier this project already uses did not fire**: a free tile on a uniform foundation under a uniform load dishes **exactly zero**, lattice and plate alike, and it is a test.

---

## Deliverable 2 — the finding `CH-0034`'s table could not see, because it starts at 45

| columns | attachments | dishing / stroke | free tile | ratio | net dishing **source**? |
|---|---|---|---|---|---|
| **1** | **15** | **0.695** | 0.308 | **2.26** | **YES** |
| **2** | **30** | **0.350** | 0.308 | **1.14** | **YES** |
| **3** | **45** | 0.218 | 0.308 | 0.71 | no |
| 4 | 60 | 0.182 | 0.308 | 0.59 | no |
| 5 | 75 | 0.168 | 0.308 | 0.55 | no |
| 8 | 120 | 0.155 | 0.308 | 0.50 | no |
| 15 | 225 | 0.149 | 0.308 | 0.48 | no |

&nbsp;&nbsp;&nbsp;&nbsp;**Below three columns a distributed coupling adds more sag between its own attachments than it removes from the rim. "Attachments buy flatness" is true only above the break-even, and `C-0041`'s realisable scheme is below it.**

`CH-0034`'s remedy sentence — *"45 attachments as 3 × 15 is the count at which further attachments stop buying flatness"* — **stands and gains a lower companion**: three columns is also the count at which attachments *start* buying it. The whole useful range of the axis is one grid step wide, and the packing forbids it.

---

## Deliverable 3 — the orientation, and where the 25.6× actually appears

| | dishing / stroke | peak crossover [pN] |
|---|---|---|
| **1 × 15, across the helices** (one per duplex — `C-0041`'s column) | **0.695** | **0.209** |
| **15 × 1, along one helix** | 0.754 | **3.283** |
| ratio | **1.08×** | **15.7×** |

**The dishing barely notices the orientation and the load path notices by sixteen.** At fifteen attachments neither orientation flattens a 40 nm tile — both are dominated by the same along-helix bow — but fifteen attachments on one duplex is the exact **opposite** of `C-0026`'s one-row-per-duplex scheme, and the other fourteen duplexes must then be carried across the hinges.

> `C-0041` finds the single-column flexure array feasible at **exactly 1 of 720** orientations, and that one is the sheet's own helix direction. **That is also the orientation that lays the attachments across the helices.** The packing constraint and the load-path constraint want the *same* angle, so the measure-zero window is a design and not a defect — a second, independent reason for the same statement `C-0041` made from plan geometry alone.

---

## Deliverable 4 — the stagger, which is not quite free and is more useful than it was introduced to be

| stagger [bp] | [nm] | half [nm] | dishing / stroke | Δ vs collinear | peak crossover [pN] | unzip margin | order in `s` | flexure fits the body? |
|---|---|---|---|---|---|---|---|---|
| 0 | 0.00 | 0.00 | 0.695 | — | **0.209** | 47.8× | — | yes |
| 2 | 0.68 | 0.34 | 0.699 | +0.6 % | 0.308 | 32.4× | — | yes |
| 4 | 1.36 | 0.68 | 0.703 | +1.2 % | 0.406 | 24.7× | 0.40 | yes |
| **8 (`C-0041`'s remedy)** | **2.72** | **1.36** | **0.710** | **+2.2 %** | **0.592** | **16.9×** | **0.55** | **yes** |
| 16 | 5.44 | 2.72 | 0.717 | +3.2 % | 1.051 | 9.5× | 0.83 | yes |
| 32 | 10.88 | 5.44 | 0.668 | −3.9 % | 1.932 | 5.2× | 0.88 | yes |
| 48 | 16.32 | 8.16 | 0.595 | −14.5 % | 2.515 | 4.0× | — | yes |
| **54 — the BUILDABLE optimum** | **18.36** | **9.18** | **0.541** | **−22.2 %** | **2.581** | **3.9×** | — | **yes, just** |
| 64 | 21.76 | 10.88 | 0.405 | −41.8 % | 2.464 | 4.1× | 0.35 | **no** |
| *80 — the unconstrained optimum* | *27.20* | *13.60* | *0.380* | *−45.4 %* | *2.259* | *4.4×* | — | **no** |
| 96 | 32.64 | 16.32 | 0.565 | −18.7 % | 2.263 | 4.4× | — | **no** |
| 112 | 38.08 | 19.04 | 0.683 | −1.7 % | 1.909 | 5.2× | — | **no** |

Four separate things are in that table.

1. **`C-0041`'s 8 bp remedy costs +2.19 % of the dishing** — above `T-101`'s declared one-per-cent falsifier, which therefore **fired**, and immaterial to every verdict because the quantity it perturbs is already 7× the tolerance.
2. **It breaks `C-0015`'s exact zero, at FIRST order.** Under a perfectly **uniform** load the collinear column restores `0 pN` and the 8 bp staggered one restores **0.389 pN** — **1.9×** `C-0022`'s entire solved edge effect on the same grid. The reaction *is* second order (the tile's bow is even about `x = 0`, so `w'(0) = 0`), but **a crossover measures the relative deflection of two adjacent duplexes**, and two duplexes propped at `+s/2` and `−s/2` have mirror-image *shapes* whose difference is `O(s)` everywhere but the centre. **Alternating a support STATION across the helices is the same symmetry break as alternating its STIFFNESS** — `C-0026`'s worst scatter pattern, reached a second way and from a geometry rather than a tolerance. It stays **17–26×** below the 10 pN unzip allowable, so no verdict moves. This is `CH-0060`.
3. **Swept as a design variable the stagger buys 45 % of the dishing back**, and the cheap bound predicts *where*: the unconstrained optimum half-stagger is **13.60 nm**, which is the along-helix bending length (12.83 nm) to **6 %** and `C-0015`'s three-column pitch (13.33 nm) to **2 %**. A large alternating stagger makes **adjacent duplexes prop each other through the crossovers** — a single column doing the best imitation of a multi-column grid that alternation allows.
4. **And that optimum is NOT BUILDABLE, because the constraint is the span.** A staggered *attachment* only has to stay on the tile; a staggered **flexure** has to stay on the **body**, and a flexure is a beam of `C-0041`'s 21.44 nm span **centred on its own midspan** — which is exactly where the tie, and therefore the attachment, sits. The half-stagger is therefore capped at `edgeX/2 − span/2 = 9.28 nm`, i.e. **18.56 nm peak to peak, 54 base pairs**, and the 80 bp optimum **overhangs the tile edge by 4.32 nm**. Inside the cap the best is **54 bp: 0.541 of the stroke, a 22 % gain rather than 45 %**, at 2.58 pN on the crossover path (3.9× clear of unzip). It is a real gain, it is **half** the unconstrained one, and it is **still not enough** — 0.541 is 5.4× the convention and 2.5× worse than simply having the three columns `C-0041` shows cannot be built. **The same span that forbids three columns also caps the repair for having only one.**

---

## Deliverable 5 — the lattice beside the plate, with the excess quoted and its sign reported

| scheme | lattice / plate | excess |
|---|---|---|
| free | 0.957 | **−4.3 %** |
| **1 × 15** | **1.011** | **+1.1 %** |
| 1 × 15 staggered 8 bp | 1.024 | +2.4 % |
| **3 × 15** | **0.919** | **−8.1 %** |
| 8 × 8 | 1.038 | +3.8 % |
| 15 × 15 | 0.880 | **−12.0 %** |

`CLAUDE.md` records that **a discretisation is not automatically a relaxation** — softer under a point load entering the sheet, stiffer under a point reaction and a smooth load. This load case is a smooth pressure reacted through point supports, which is the **stiff** corner, and the lattice is duly the stiffer model at every column count above two. The excess never exceeds a tenth in magnitude and **is not of one sign**, so no verdict in this claim rests on the choice of model.

---

## Deliverable 6 — is `C-0022`'s 32.1 % irreducible split affected? **No, and that is the point**

`C-0022`'s split is a property of the tile's **rim** — an 8.9 nm collar no interior attachment can reach — and it is written on the **free** tile. This task's own plate reproduces it to **0.2 %** (0.32188 against 0.32126); the lattice gives 0.308. **Nothing about the attachment count moves it.**

What the count moves is the **other** term, the sag between attachments:

| scheme | total dishing / stroke | rim floor (`CH-0034`) | **coupling's own sag** | sag as a share of the dishing |
|---|---|---|---|---|
| 3 × 15 | 0.218 | 0.149 | 0.069 | **32 %** |
| **1 × 15** | **0.695** | 0.149 | **0.546** | **79 %** |

&nbsp;&nbsp;&nbsp;&nbsp;**The lever/sensor split stands exactly as `C-0022` states it. A 15-path design does not sit at it — it sits well above it, and four fifths of what it sits above is the coupling's own doing.**

---

## Deliverable 7 — every operating state, and every foundation stiffness

**All 21 of `C-0022`'s solved states**, 1 × 15 staggered 8 bp: dishing runs **0.404 to 0.769** of the stroke. The *best* state (2 mM, 2 nm gap, 0.368 V) is still **4.0×** the convention. **There is no salt concentration, no gap, no bias and no operating state at which fifteen attachments make the Gen-1 tile flat.** The worst restored crossover force over the 21 states is **0.878 pN**, 11× clear of the unzip allowable.

**The foundation sweep**, `C-0001`'s secant ×[0.25, 4]:

| `k_f` × | 0.25 | 0.50 | **1.00** | 2.00 | 4.00 |
|---|---|---|---|---|---|
| free-tile stroke [nm] | 19.629 | 9.815 | **4.907** | 2.454 | 1.227 |
| dishing [nm] | 7.766 | 5.583 | **3.486** | 1.891 | 0.915 |
| **dishing / stroke** | **0.396** | **0.569** | **0.710** | **0.771** | **0.745** |
| flat? | no | no | no | no | no |

The softest corner — the direction `T-1c`'s corrections run — is the *most* favourable here and is still **4.0×** the tolerance. The whole `CH-0001` sweep is worth 1.9× and closes nothing.

---

## The five verification gates

Executed as **22 gate-named tests** in `src/test/kotlin/coupling/SingleColumnFlatnessTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the Winkler bending length is a length and scales as the **fourth root** of the rigidity (16× rigidity is exactly 2× length, to `1e−12`); the dishing is **exactly linear** in the applied pressure (3× the load is 3.0000× the dishing, to `1e−10`), so `dishing/stroke` is invariant under load scaling; unphysical arguments throw — a negative stagger, a stagger that puts an attachment outside the tile, a negative base-pair count | **PASS** |
| **2 — limiting cases** | **a uniform load on a free tile dishes exactly zero, lattice AND plate** (`< 1e−9 nm`) — the free falsifier; a zero stagger reproduces `attachmentGrid` **point by point** to `1e−15`; a staggered grid is still one attachment row per duplex, with the displacement living entirely along `x`; the 8 bp stagger is 2.72 nm and one base pair is the rise; **a one- and a two-column coupling dish MORE than no coupling and a three-column one dishes less**; the span caps the stagger at 18.56 nm, `C-0041`'s 8 bp and the 54 bp buildable optimum fit and the 64 bp one does not | **PASS** |
| **3 — symmetry and conservation** | the support forces plus the foundation carry the whole applied load to `1e−6` **on a smoothly varying field**, the collar's `C⁰` kink being worth 0.07 % and reported separately rather than hidden inside a conservation gate; **a stagger and its mirror image dish identically** (`< 1e−9 nm`); the **collinear** single column keeps `C-0015`'s exact zero under a uniform load (`< 1e−9 pN`, compared **absolutely** because both sides are meant to be zero); the **staggered** one does not, and is **first order** in the stagger; the wrong orientation costs **> 10×** on the crossover path and still clears the unzip allowable | **PASS** |
| **4 — numerical convergence** | **NESTED** subdivisions `1 ⊂ 2 ⊂ 4` (never 1/2/3/4, which moves a point support off a node): `5.0e−4` then `1.4e−4`, tightening monotonically; the plate basis degree 8/10/12: `1.1e−3` then `1.8e−4`; the peak-dishing sampling grid 41/81/161, which moves the answer by **exactly zero** because the peak sits on a **corner** every grid contains — `CLAUDE.md`'s *"a corner is √7 noisier than the centre"* showing up as the argmax | **PASS** |
| **5 — literature and upstream cross-check** | `C-0026`/`CH-0034` reproduced at **1 × 15 (`3.5e−10`)**, **3 × 15 (`6.8e−10`)** and **8 × 8 (`4.9e−10`)** under the solved design point, and at 3 × 15 under a uniform load (`7.8e−3`); `CH-0034`'s **0.149 saturation floor** reproduced at 15 × 15 (`1.7e−4`); `C-0026`'s free-tile stroke 4.90731 nm (`7.7e−10`); `C-0022`'s **free-tile 0.3213** reproduced through this task's own plate (`2.0e−3`); `C-0026`'s 1 × 15 peak crossover force 0.2093 pN (`1.1e−4`); the **25.6× sheet anisotropy** from the sheet's own rigidities (`1.5e−3`); `C-0041`'s 8 bp = 2.72 nm exactly; `C-0015`'s 13.333 nm column pitch; and **the best half-stagger asserted equal to the along-helix bending length within 10 %** | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a uniform load producing non-zero dishing on the **free** tile | **no** | exactly zero, lattice and plate, and it is a test |
| 2 | the 1 × 15 dishing coming out at or below `CH-0034`'s 0.149 floor | **no** | 0.695, **4.7× above** it — the criterion does not saturate below 45 |
| 3 | the lattice putting 1 × 15 within 2× of 3 × 15, falsifying the bending-length argument | **no** | 3.2×, and the bound also predicted the optimum stagger to 6 % |
| 4 | **the 8 bp stagger moving the dishing by more than a per cent** | **YES** | **+2.19 %**, and the more interesting cost is on the load path, not the dishing. `T-101`'s Plan said in advance that this *"needs a challenge"*, and it has one: `CH-0060` |
| 5 | the staggered grid restoring a crossover force comparable with the per-path static share | **no** | 0.389 pN against a 6.67 pN share — 5.8 % of it, and 26× below unzip |

**A prediction of this task's own that failed, in code, at the first run:** gate 3 was written asserting the staggered column's restored force is **second order** in the stagger, on the argument that the tile's bow is even about `x = 0` so the reaction changes at `O(s²)`. The measured exponent is **0.9**. The reasoning conflated the *reaction* with the *shape*: the reaction is indeed second order, but a crossover measures the **relative deflection of two adjacent duplexes**, and mirror-image shapes differ at first order. The test now asserts first order and says why.

**A result that was not anticipated at all:** that a coupling can be a **net dishing source**. Every upstream claim treats the attachment count as an axis along which flatness is bought, and `CH-0034` established that the buying *stops* at 45. Nobody had asked what happens on the other side of the range, and the answer is that it **reverses** below three columns.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured**, and the flexure motif the count belongs to is not demonstrated (`C-0028`, `C-0029`).
- **The load profile is `C-0022`'s and inherits its whole validity range**: mean field (`C-0005`'s one-loop correction is 123–214 % across this gap range, larger than every effect in this claim), point ions, a two-dimensional solve with the **corner bracketed rather than solved**, an **unsourced rim charge** worth 1.85× on the depth, and a gap filled with free buffer.
- **Linear Winkler foundation** at `C-0001`'s secant, swept ×[0.25, 4]; `C-0001`'s stiffnesses are lower bounds per `CH-0001`, and the soft corner is the *favourable* one here.
- **The coupling is `n` IDENTICAL LINEAR springs** at `C-0017`'s mandate. `C-0030`'s flexure **strain-softens** (`CH-0042`), so a real 15-path coupling is not exactly this one; the dishing is monotone in the coupling stiffness, and the direction was swept through the foundation multiplier instead.
- **The crossover's vertical link is `C-0009`'s rigid PENALTY**, inherited unchanged; static forces converge in it and thermal ones provably do not (`CH-0033`). No thermal channel is computed here.
- **One crossover layout** — `T-10`'s eight symmetrically centred columns. `C-0015`'s **32 base-pair phase is not swept**; `C-0026` measured it at 3.9 % on the crossover force, and it is not expected to move a dishing, but that is an expectation and not a measurement.
- **The stagger is a rigid translation of alternate rows along `x`.** No assembly tolerance, no thermal excursion and no out-of-plane bow is represented.
- **`T-5b`'s 10 % is a CONVENTION.** Every verdict here is quoted with it named; the 1 × 15 answer is 7× above it and would survive a tolerance five times looser.
- **No electrostatics is solved and no lateral coordinate is carried.** The dishing is out-of-plane only.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| rise per base pair | 0.34 nm | **CITED** |
| crossover interface spacing | 32 bp | **CITED** via `C-0015` |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| crossover hinge stiffness `k_θ = 2αB/(100a)` | `α = 1` | **CITED, FITTED**, Chen et al. (2014) SI; its `[0.6, 1.2]` bracket not re-swept |
| `C-0022`'s solved collars | 21 states | **CITED**, and **read at run time** from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on `(concentration, gap, bias)` |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| `RIGID_PLATE_TOLERANCE` | 0.10 | **CITED CONVENTION** from `T-5b`, not a physical threshold |
| `C-0041`'s 1 × 15 count, 8 bp stagger, one feasible orientation | — | **CITED**, and the stagger reproduced here exactly |
| §3 parameters | 100 pN, 3 nm, 40 × 40 nm | **CITED** |

Everything else — the two Winkler bending lengths, every dishing, the column sweep and its break-even, the whole stagger sweep and its optimum, the orientation contrast, the lattice-over-plate excesses, the sag/rim decomposition, the 21-state and foundation sweeps and every convergence record — is **derived here in code**, with `C-0026`'s pipeline **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether a tile that grows 1.44× (`T-102`) needs proportionally more columns.** The bending length is a **material** property and does not grow with the tile, so a larger tile needs *more* columns for the same flatness — which the packing forbids exactly as it does here. Named, not computed.
2. **Whether a NON-UNIFORM coupling stiffness could buy back the edge dishing.** Every spring here is equal by `C-0017`'s mandate and nothing upstream requires it; stiffer paths at the rim, where the load is, are an unexplored axis and the only one left that does not need more attachments.
3. **The perforated superstructure (`T-68`)**, which sets the effective coupling stiffness the tile actually sees — the one input this task takes from `C-0017` rather than deriving.
4. **`C-0015`'s 32 base-pair crossover phase**, swept by `C-0026` on the crossover force and not here on the dishing.
5. **`T-9`'s crossover vertical stiffness**, unchanged and untouched.

## Challenges

**Raises [`CH-0060`](../challenges/CH-0060-the-stagger-is-not-free.md)** against `C-0041`'s clause that the 8 bp stagger *"is free of every upstream claim"* and *"moves nothing else"*. **No count, no orientation, no packing verdict and no allowable of `C-0041` moves**; what moves is that sentence, on the narrow and quantified ground that the stagger breaks `C-0015`'s exact zero at first order and costs 2.19 % of the dishing.

**None stands against this claim.** The four ways it would fail:

1. **A coupling stiffness materially below `C-0017`'s mandate.** The dishing at fifteen paths is the coupling's own sag, so a softer coupling dishes less — in the limit reaching the free tile's 0.308, which is still 3.1× the convention. The verdict survives the whole limit.
2. **A load with far less rim content than `C-0022`'s.** Under a merely uniform load 1 × 15 dishes 0.426, still 4.3× the convention, so even destroying the edge effect entirely does not reach it.
3. **A demonstration that `T-5b`'s 10 % is far too tight.** It is a convention and this claim says so; but 0.695 survives a five-fold loosening, and the *free* tile's 0.308 survives a threefold one.
4. **An attachment scheme that is not a grid.** Everything here places attachments on rows and columns; a genuinely irregular placement is unexplored, and the stagger sweep is the one hint that the space is not exhausted — it found 45 % on an axis nobody had swept.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
