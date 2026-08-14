# T-97 — Can TWO 90° junctions close on ONE sheet duplex 6–8 bp apart?

| | |
|---|---|
| **Leaf** | `A8.2` (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Raised by** | [`C-0037`](../claims/C-0037-triangulated-standoff.md), *"Still open"* item 1 and *"Challenges"* way-to-fail 1 — **the largest open item under its recommended design** |
| **Extends** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s closure search, which places exactly **one** junction |
| **Verification type** | **in-silico** (a backbone-geometry closure search over a *pair* of standoffs on one seat duplex) **+ logical** (a steric floor and an azimuthal quantum, both closed form, both cheap) **+ literature** (whether two duplexes have ever been stood on one sheet duplex, and at what spacing) |

---

## Formulate

### The question, and why it is not `C-0029`'s

`C-0037` closes the standoff branch by putting **two** legs in a row **across** the flexure's axis,
which on `C-0029`'s geometry means **along one sheet duplex**, at a pitch it quotes as 6, 8 or 12 bp —
reporting that the three are *bit-identical in the loaded plane* and concluding that
*"the draw-in cost of a cross row is the leg COUNT, not the leg SPACING"*.

That statement is about the **frame**, and it is derived on a mechanics model that never asks whether the two junctions exist.
`C-0029`'s closure search places **one** scaffold-excursion junction, and finds it closes with both links at 0.600 nm
inside the measured `[0.60, 0.70]` nm phosphodiester step and **zero unpaired nucleotides**.
Nothing has ever searched for a **second** one on the same seat duplex.

Three things could stop it and none of them is in any upstream claim:

1. **Sterics.** Two B-form duplexes standing on one seat duplex cannot be closer than one duplex diameter.
2. **The azimuth.** `C-0029` establishes that a base chord's direction is quantised at `360°/10.67 = 33.74°` per base pair,
   and that the *couple* projects as `cos²` onto the axis the flexure needs.
   A seat duplex is a **helix**: the sheet phosphates the second junction has to reach are rotated by `n × 33.74°` relative to the first's.
   But the standoff must stand **normal** to the sheet, so the second junction is **not** a screw image of the first —
   it is a different geometric problem, and whether it can be solved *with its chord still on the flexure axis* is exactly what is unknown.
3. **Collision and occupancy.** The two scaffold excursions must not collide, their targets must be distinct phosphates,
   and neither may consume a base pair the seat duplex's own crossovers need.

### Numeric target and acceptance predicate

**Acceptance (from `TASKS.md`)**: *a closure search with a second standoff seated on the same duplex, or a statement that the pair does not fit.*

Made falsifiable:

| id | predicate |
|---|---|
| **`Q1`** | **Steric.** The two standoff axes are at least `2R = 2.00 nm` apart in plan, and no terminal phosphate of one junction comes within the van der Waals separation `0.35 nm` of a terminal phosphate of the other. |
| **`Q2`** | **Covalent.** *Both* links of *both* junctions sit inside the **measured** `[0.60, 0.70]` nm intrastrand phosphodiester step, with **zero** unpaired nucleotides — `C-0029`'s own `P7`, applied twice. |
| **`Q3`** | **Distinct.** The four sheet targets are four distinct phosphates. |
| **`Q4`** | **Coplanar seat.** Both legs sit at the **same** lateral offset `y_c` from the seat duplex's axis, so the truss's loaded-plane second moment `Σ(Δy_i)²` is **exactly zero** — `C-0037`'s `L2a8` is only `L2a8` if the row is straight. |
| **`Q5`** | **Azimuth.** The two base chords are reported with their misalignment from the flexure's axis, and the couple each supplies to the **loaded** plane is `2k_bond,θ + 2k_bond,s a² cos²ψ`. **`C-0037` asserts the azimuth cost is ≤ 8.4 %; this task measures it for the pair.** |
| **`Q6`** | **Stability.** The mixed-base truss's critical load is re-solved (the legs no longer share one base constant) and its margin against `C-0037`'s own duty(10) = **3.499 pN** is reported on **both** rigidities — CanDo's `EI = 230 pN·nm²` and Fields et al.'s implied **172.9**. |
| **`Q7`** | **Occupancy.** The number of the seat duplex's **32** crossover phases (`C-0015`) that leave every target base pair free is reported, and is greater than zero. |

**PASS** = a pair exists at some separation in **6–8 bp** satisfying `Q1`–`Q4` and `Q7`,
with `Q5` and `Q6` reported. **FAIL** = no separation in 6–8 bp admits a pair,
in which case the task reports the separations that *do* and what `C-0037`'s design would have to become.

### Units, geometry and sign conventions (restated, per the invariants)

- Lengths **nm**, forces **pN**, moments **pN·nm**, rotational stiffness **pN·nm/rad**, translational **pN/nm**.
  `k_BT = 4.141947 pN·nm` at **T = 300 K**, aqueous **2 mM MgCl₂**. Nothing here is temperature dependent.
- **The sheet is the `x–y` plane and `z` its normal.** The **seat duplex** runs along **`x̂`** with its axis at `y = 0, z = 0`;
  its neighbours are at `y = ±2.69 nm` (SAXS, Fischer et al. 2016).
- **The flexure's own axis is `ŷ`** — across the sheet helices. This is `C-0037`'s `x` and the mapping is stated there:
  its *"across the flexure axis"* row is *"along one sheet helix, quantised at the 0.34 nm rise"*.
  So the **loaded plane is `y–z`**, the legs are offset along **`x̂`**, and `C-0037`'s `Σx_i² = 0` is **our `Σ(Δy_i)² = 0`**.
- **A base chord aligned with `ŷ` puts the base's strong couple in the loaded plane.**
  Misalignment `ψ` is the angle between the chord and `ŷ`, folded into `[0°, 90°]`; the loaded-plane couple carries `cos²ψ` and the free-plane one `sin²ψ`.
  Their **sum is conserved** — `4k_bond,θ + 2k_bond,s a²`, independent of `ψ` — which is `C-0037`'s rank-one identity one level down and is asserted as a gate-3 test.
- **A standoff stands along `+z`**, axis at `(x_c, y_c)`, terminal base pair in the plane `z = ` the seat height,
  its two termini at the phosphate radius `r_P = 1.00 nm` and azimuths `ψ₀`, `ψ₀ + Δ` with `Δ` the backbone separation (120° nominal, 154° wide, 180° hard).
- **Positive `ψ` is anticlockwise seen from `+z`.** A chord is a **line**, not a vector: every azimuth is folded modulo `π`.

---

## Plan

### The cheap bounds, which run first

| | bound | cost | what it can settle on its own |
|---|---|---|---|
| **1** | **The steric floor.** Two duplexes of radius `R` on one seat duplex need `2R = 2.00 nm` of axial separation, i.e. `⌈2R/0.34⌉ = ` **6 bp**. | one division | It puts `C-0037`'s **6 bp** row exactly *on* the floor, so any failure at 6 bp closes the lower half of the band before any search runs. |
| **2** | **The screw-image azimuth.** If the second junction were the first translated `n` base pairs along the seat duplex, it would also be **rotated by `n × 33.74°` about the seat duplex's axis** — and a standoff rotated about a horizontal axis is no longer normal to the sheet. The rotation folded to `[0°, 90°]` is 22.4° at 6 bp, 56.2° at 7 and **89.9° at 8**. | one modulo | If the second junction were forced onto the screw image, **`C-0037`'s recommended 8 bp would be the single worst separation available** and the whole azimuth budget would be spent. The bound therefore says *where to look*, and its failure to bind is itself the result. |
| **3** | **The occupancy count.** Crossovers recur every 16 bp along one duplex (`C-0015`: 32 bp per *interface*, alternating between the two neighbours), and the pair spans ≤ 10 bp. | one count | If no crossover phase left the targets free the pair would be dead on arithmetic. |

**Only if bound 2 fails to bind is the expensive search worth running** — and it can fail to bind for one reason:
the standoff's azimuth about its **own** axis is a free continuous parameter, and so is its axial position,
so the second junction may reach a *different* target pair at a *different* phase and still put its chord on `ŷ`.
That is a two-parameter search against two 0.1 nm windows, and it cannot be decided by arithmetic.

### The expensive calculation, and why this one and not another

A **deterministic grid search over the pair**, on `C-0029`'s own backbone geometry and its own admissibility test:

1. For each lateral seat `y_c` and each axial position `x_c` on a grid over one **32 bp helical repeat**,
   sweep the standoff's own azimuth and record the **best-aligned covalent placement** — the one whose chord is closest to `ŷ`
   among all placements whose two links both sit inside the measured step and which clash with nothing.
   This is a *field*, `cos²ψ(y_c, x_c)`, not a single optimum, and that is the point: `C-0029` reported one argmin.
2. For each candidate separation `n` in base pairs, maximise `min(cos²ψ_A, cos²ψ_B)` over `x_c` at fixed `y_c`,
   subject to `Q1`, `Q3` and `Q4`.
3. Feed the two resulting base couples into a **mixed-base** truss, because `C-0037`'s `TriangulatedStandoff`
   gives every leg the *same* base and cannot represent two chords at different azimuths.

**Justification against cost.** The alternative is an atomistic or coarse-grained (oxDNA) minimisation of the pair,
which is what `T-71` is for and which — as `C-0029` states — **can only make the answer worse**:
this search tests a *necessary* condition (a phosphate pair inside the measured step, no van der Waals overlap)
and never a sufficient one, so a *"closes"* verdict is an **upper bound on buildability**
and a *"does not close"* verdict would be a proof of impossibility.
Spending the toolchain build on a pair before the cheap geometry says the pair exists would be the wrong order,
and the geometry costs seconds.

**The mixed-base truss needs a new solver and gets a small one.** `C-0028`'s sway determinant
`sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u` is written for **one** column, and `C-0037` multiplies it by the leg count —
legitimate only when every leg has the same base. Two chords at different azimuths give two different `ρ_b`,
so the assembly is solved as a **beam-column finite element** (Hermite cubics with the consistent geometric stiffness,
base rotational springs, one shared head node carrying `(u, φ)` and the frame couple),
with the critical load taken as the smallest total load at which the assembled matrix loses positive definiteness.
It is validated by reproducing `C-0037`'s `trussBucklingLoad` **exactly** in the equal-base case, and its element count is swept.

### What would falsify this approach

| # | falsifier |
|---|---|
| **1** | **The pair not fitting at any separation.** Then `C-0037`'s recommended design does not exist and the standoff branch closes again — the outcome the task was raised to test for. |
| **2** | **The pair fitting only at separations `C-0037`'s frame cannot use** — e.g. only above 12 bp, where the leg row is wider than the flexure it caps. |
| **3** | **Bound 2 binding**, i.e. the second junction being forced onto the screw image. Then the azimuth cost is 22–90 % rather than `C-0037`'s ≤ 8.4 %, and 8 bp is the worst choice in the band. |
| **4** | **The alignment being free but the seat degenerate** — an optimum that stands the standoff on the *edge* of the seat duplex, where the flat-face line contact `2√(R² − y_c²)` has collapsed to a point. That is an artefact of `seatFaceHeight`, not a design, and it must be excluded by a stated bound on `y_c` rather than by inspection. |
| **5** | **The mixed-base solver failing to reproduce `C-0037`'s equal-base critical loads.** Then nothing downstream of it may be quoted. |
| **6** | **No crossover phase leaving the targets free.** |

### Pre-registered prediction

The steric floor is **6 bp**, so `C-0037`'s 6 bp row sits exactly on it and its 8 bp row has 0.72 nm of clearance.
Bound 2 is expected **not** to bind — the standoff's own azimuth is free, so the second junction should reach a different
target pair and recover most of the alignment — but the *residual* misalignment is expected to be several degrees
rather than `C-0029`'s 2.2°, and to differ between separations, which would make the leg spacing a **design variable**
where `C-0037` reports it as free.
