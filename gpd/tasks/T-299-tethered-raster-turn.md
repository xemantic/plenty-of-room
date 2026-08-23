# T-299 — the raster turn as an entropic TETHER: route B's mechanics

**Leaf** `A8.2`.
**Raised by** [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) §11 and
[`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md) (`T-296`).
**This is the arm the built object occupies.**
[`CH-0251`](../challenges/CH-0251-the-deposited-block-has-no-loops.md) disputed whether route B was
built at all and was **REFUTED on its central point** by
[`C-0200`](../claims/C-0200-the-file-draws-and-the-table-orders.md) (`T-302`, iteration 47).
Route A is already graded (`C-0175`, `C-0180`, `C-0190`) and it is the design nobody has folded;
route B was not graded at all.

---

## 1. Formulate

### The object

`C-0175` §9 adds `H − 1 = 59` **covalent ties** to `C-0154`'s honeycomb grillage on the stated
ground that a raster turn *"is a covalent crossover like any other"* — a tie between two duplex
**ends** at `s = ±L/2`, carrying the same three elements a lattice bond does: a dihedral spring
`k_θ`, a normal link and an axial slip spring.
That is **route A**, and it is the design that carries **zero** unpaired scaffold nucleotides at
the turn.

**Route B** is the built allowance: **28 unpaired nucleotides** per turn, so what stands between
the two rim nodes is a **freely-jointed single-stranded chain**.

**No lattice in this repository carries one**, so the corpus has graded one of two designs.

> **CORRECTED, twice, before execution.**
>
> **(a) The standoff is a CONTOUR, not a rise, and it does not enter the element.** `C-0193`'s
> *"`14 bp = 4.76 nm` outboard"* is `14 × 0.34 nm/bp` — the **duplex** rise applied to a region
> that is **single-stranded**. What ssDNA has is a contour, `0.65–0.70 nm/nt` (the inextensible
> bracket that travels with the zero-force Kuhn length), so a 14 nt half-loop is **9.10–9.80 nm**
> of contour, `1.91–2.06×` the rise reading and a different **kind** of number: a rise is a fixed
> lattice step and a contour is an upper bound on an extension a coil never reaches. Its
> root-mean-square end-to-end distance is `4.37–5.28 nm`.
> **And none of that enters the element**: a freely-jointed chain joins the two **duplex** ends,
> both at the same rim, and where its own covalent link sits along it is a conformation the
> chain's statistics integrate over. The element is fixed by the **nucleotide count** and by the
> **anchor-to-anchor span in the cross-section**, and the outboard distance is a parameter of
> neither.
>
> **(b) There are TWO turn populations, not one.** `C-0200` (`T-302`, this iteration) reads the
> 2009 SI staple **order** against the deposited design: `98` paired bases on all 60 helices over
> the **identical** window `28..125`, and the 28 unpaired split **`12 / 16`**, not `14 / 14`. So a
> raster turn joins two duplex ends **`24` nt apart at thirty turns and `32` at the other thirty**,
> whose mean is exactly `28`. Both populations are carried, on the rim the turn sits at.
>
> **(c) The fork is settled in this arm's favour.** `CH-0251` is **REFUTED on its central point**
> by the same claim: the SI orders `5 880` staple nucleotides in `144` strands — `60 × 98` exactly
> — where the deposited file draws `214` and `7 560`, and the `70` strands the file draws and the
> order omits total exactly `1 680 = 60 × 28` and lie entirely in the helix end regions.
> **A design file is a drawing and a staple table is a purchase.** So route B is the arm the built
> object occupies, and route A — which `C-0175`, `C-0180` and `C-0190` grade — is the one nobody
> has folded.

### The numeric target

The recommended `10 × 6` block, `102 / 109` raster, `116 bp` block extent, re-graded on the
**same** 64 coupled cells — four placements × four column counts × two distributions × two
composite fractions — the **same** stations, the **same** 4 000-realisation dropout stream and
seed `C-0167`, `C-0180` and `C-0187` share, with the 59 rim turns carried as freely-jointed
tethers instead of covalent ties.

Plus the **width** question route B forces: at the built allowance a uniform honeycomb row is
`31.28 nm` on M13mp18 and `36.04 nm` on p8064, against §3's `40 × 40 nm`.

### The falsifiable acceptance predicate

**`P1`** — the tether element is a **pure addition**: `HoneycombGrillage` with an empty tether
list is **bit-identical** to today's object in `assembleLoad`, in its bond census and in its
crossover site set, and equal in a solved field to `1e−10`.

**`P2`** — the tether's stiffness → `0` reproduces the **untied** lattice (`C-0154` / `C-0167`'s
own object) exactly on `assembleLoad` and to `1e−10` in a solved field, and its preload → `0`
with it.

**`P3`** — the tether's stiffness → ∞ drives the tie node's **link residual** to zero, i.e. the
element's stiff limit is a normal link and the residual falls at least as `1/k`.
It does **not** reproduce route A's tie, and that is the finding rather than a defect: a tether
carries **no** dihedral spring and **no** covalent slip spring.

**`P4`** — the **cheap bound** decides whether the expensive grade is needed, and it is reported
either way: the tether's secant `f/x` and tangent `df/dx` at the worst azimuth over the whole
zero-force Kuhn (`2.10–2.84 nm`) and inextensible contour (`0.65–0.70 nm/nt`) bracket, read
against `C-0154`'s `k_θ = 13.5294118 pN·nm/rad` on the arm the rim node offers (`d/2`), against
`Gen1Tile.crossoverInPlaneStiffness()`, against `C-0194`'s span-law link `41.4338953 pN/nm` and
against `RIGID_LINK_STIFFNESS = 1e4 pN/nm`.

**`P5`** — the 64 cells are graded and reported **paired per realisation** on the shared stream,
never as a ratio of two order statistics, and every coupled verdict names the **link stiffness**
it was read at (`C-0194` `F10`: `C-0180`'s two recovered cells are flat at `k_link ≥ 1000` and
not at `100`).

**`P6`** — the width arithmetic is exact integer scaffold accounting and is reported against §3's
nominal, with the free-tile flatness of the route-B row width beside it.

**`P7`** — the standoff is **re-derived** as a contour rather than inherited as a rise, and the
claim states that it is not a parameter of the element.

### Verification type

**in-silico** (a banded finite-element solve on this repository's own honeycomb grillage, and a
Woodbury coupling surrogate that is `C-0058`'s unchanged)
**+ logical** (a closed-form freely-jointed-chain law, an exact geometric decomposition of the
element's stiffness onto the model's own coordinates, and exact integer scaffold arithmetic)
**+ literature** (`C-0193`'s reading of the built precedent's own accounting; the ssDNA Kuhn and
contour brackets `T-230` already carries).

**TRL 1–3. Nothing here is measured.** `PASS` means model-consistent and traceable.

### Locked units

Lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT` (`k_BT = 4.141947 pN·nm` at 300 K),
stiffness **pN/nm**, rotational stiffness **pN·nm/rad**, pressure **pN/nm²** (= 1 MPa), angles
**rad** internally and degrees only at an API boundary.
Medium: aqueous buffer, `2 mM MgCl₂`, 300 K — `C-0022`'s design state, `10 nm` gap, `0.192 V`.

### Geometry and sign conventions

- `s` runs **along** the helices, `y` **across** them in the plane of the face (pitch `3d/2`),
  `z` along the block's **thickness** (pitch `d√3/2`). `W` is positive **downward**, toward the
  electrode — `C-0006`'s convention, `HoneycombGrillage`'s unchanged.
- `d = 2.536 nm` (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS), rise `0.34 nm/bp`, phosphate radius
  `r_P = 0.908637858 nm` (`T-71`, measured).
- A turn joins raster helix `k` to helix `k + 1` at the rim `s = ±L/2`, alternating rims along
  the path; `firstAxialSign = +1` unless stated. That is `honeycombRasterTurnList`'s census
  unchanged — **the same 59 sites route A uses**, so the comparison is controlled.
- Under `C-0200`'s `12 / 16` split the 59 turns are **two populations**, `24 nt` at one rim and
  `32 nt` at the other. Which rim takes which half is a free convention of that reading and is
  exchanged by the axial sign; it is stated, not swept.
- The tether's **span** is the distance between its two anchoring phosphates. It is an azimuth
  bracket, `d − 2r_P = 0.718724283 nm` aligned to `d + 2r_P = 4.35327572 nm` at the worst
  azimuth; the headline is read at the **worst** azimuth, which is the **stiffest** end and
  therefore the adverse one for *"is the tether negligible"*.
- The tether transmits a **force only** and **no moment**: a freely-jointed chain is free to
  rotate about its attachment.
- The connector arm is `d/2 · unitY`, `C-0194`'s theorem and not a fitted parameter: it is the
  only arm annihilating the linearised rigid roll `Φ ≡ α`, `W = α y`. An arm of zero would charge
  energy to a rigid rotation of the whole block, which is `CLAUDE.md`'s frame-indifference trap.

---

## 2. Plan

### The cheap bound runs first, and it may settle the question

`f(x) = (k_BT/b)·L⁻¹(x/L_c)` and `df/dx = (k_BT/b)/(L_c·L′(u))` at `u = L⁻¹(x/L_c)` are closed
forms; `langevin` and `inverseLangevin` are already in `structure/HoneycombTurnLoop.kt` and
`C-0193` reproduces `T-230`'s tensions from them at `2.5e−9`.
Two multiplications per corner and no solve.

**If the tether's secant and tangent are small against every stiffness the element replaces, then
route B's turn is arithmetically indistinguishable from no turn at all, and route B's grade IS
`C-0167`'s — which the corpus already has.**
That is the whole justification for running the expensive grade anyway: the bound predicts a
result, and a prediction that costs one afternoon of compute to *check* against a study whose
machinery already exists is worth checking, because `C-0180`'s two recovered cells clear the
tolerance by **0.426 %** and a 3–11 % element is not obviously below that.

### The element, derived rather than chosen

The relative displacement of the two anchor points, expressed in the model's own coordinates, is
`δ⃗ = (0, δ_ζ, δ_s)`: the model has **no in-plane transverse coordinate**, so `δ_y ≡ 0` by
construction and not by neglect. With `n̂ = (unitY, unitZ, 0)` the chain's own decomposition gives

```
E = ½[(df/dx)·unitZ² + (f/x)·unitY²]·δ_ζ² + ½(f/x)·δ_s²
```

— the **axial** stiffness `df/dx` on the component along the chain and the **transverse** secant
`f/x` on the two perpendicular ones, which is the standard taut-cable geometric stiffness.
So the element is **two scalars on the grillage's two existing gradients**: the link gradient
`(1, armY, −1, armY)` carries the first coefficient and the slip gradient `(1, −armZ, −1, −armZ)`
carries `f/x`.
It carries **no dihedral term at all**, which is the whole mechanical difference between the two
routes.

### The preload is a LOAD, and it is route B's analogue of route A's prestrain

A freely-jointed chain held at any `x > 0` is in **tension**. That tension is self-equilibrated
between the two rim nodes, so it is an internal initial stress: `C-0104`'s rule applies verbatim
— it changes **no** entry of the stiffness matrix, the field is exactly **linear** in it, one
solve fixes the whole axis, and every influence function must be taken on `withoutPrestrain`.
Its projection onto the model's coordinates is `f·unitZ` times the link gradient, so the **nine
in-plane turns contribute exactly zero preload** (their pull is entirely along `y`, a direction
the model has no coordinate for) and the **fifty through-thickness turns** carry it.

### One-sidedness, stated rather than assumed

A tether pulls and does not push, so the element is **not** a spring in the usual sense.
It is a **preloaded** central-force element, and the linearisation about the built state is
legitimate because the built state is **taut**: `x/L_c = 0.037–0.446` over the whole bracket and
`f > 0` everywhere, with `df/dx > 0` and `f/x > 0`, so the element is positive definite and the
solve is linear.
The compressive branch is not reached by any displacement this study produces: it would need the
two rim nodes to close to `x = 0`, which is `2.5–4.4 nm` against solved deflections of order
`1e−2 nm`.
**The state the linearisation is taken at is the undeflected block at the stated span, loop
length and `(b, c)` corner, and every number is quoted with it.**

### What is run

1. **Cheap bound** — the stiffness table over `{15, 20, 28} nt × {aligned, line of centres, worst
   azimuth} × {2.10, 2.84} nm × {0.65, 0.70} nm/nt`, with the four ratios.
2. **Free tile** — untied / tied (route A) / tethered (route B), at `f = 0.30`, `f = 0.26` and
   `f = none`, with and without the preload.
3. **The 64 coupled cells** — the tethered lattice against `C-0167`'s untied one and `C-0180`'s
   tied one, paired per realisation on the shared stream, at 4 000 realisations.
4. **The link stiffness** — every coupled verdict re-read at `k_link ∈ {1e4, 1e3, 1e2}` at the
   deciding cells, because `C-0194` `F10` fired there.
5. **The width** — exact integer scaffold accounting for M13mp18, p7560 and p8064 at the built
   allowance, and the free-tile flatness at each resulting row width.

### Cost

The 64-cell grade is `T-279`'s machinery unchanged: one banded factorisation per lattice
(`4 080 × 243`, ~27 s) and a `n × n` Woodbury slice per cell. `T-279` runs in minutes.
The tether adds **one** lattice per parameter corner and no new solver.
A smoke pass at 150 realisations runs the whole prose and serialisation path in under three
minutes (`CLAUDE.md`: *build the result and write the JSON before formatting any prose*).

### What would falsify this approach

- **`F1`** — the empty-tether lattice is not bit-identical to today's on `assembleLoad`.
  Then the addition is not an addition and every number the corpus carries is at risk.
- **`F2`** — the **uniform-load falsifier** fires: a uniform pressure on the **free** tethered
  lattice dishes more than the solve residual. `HoneycombGrillage.nodeS` carried an unstated
  `rowBasePairs ≡ 0 (mod 7)` precondition that exactly this falsifier found, and a preloaded
  element is a new way to break frame indifference.
  *(It is asserted on the **free** lattice only: a uniform load on a **coupled** tile does not
  dish zero, because the coupling paths run to ground.)*
- **`F3`** — the preload is **not** annihilated by a rigid roll. Then the `d/2` arm is not being
  used consistently and the element charges energy to a rigid-body motion.
- **`F4`** *(declared open)* — the cheap bound says the tether is negligible and the coupled grade
  says otherwise, i.e. some cell's `T-5b` verdict moves between the tethered and the untied
  lattice. That is the result that would make route B a mechanically distinct design rather than
  an absent element.
- **`F5`** *(declared open)* — the tether **preload** moves a free-tile or a coupled verdict.
  Route A's prestrain ceiling is `0.0764244991` of the stroke (`CH-0228`); route B has no azimuth
  and therefore no prestrain, but it has a preload, and nobody has priced it.
- **`F6`** — the stiff limit's link residual does not fall with `1/k`. Then the element is not a
  penalty-consistent constraint in its own limit.
- **`F7`** *(declared open)* — route B's own row width admits a flat free tile where route A's
  does not, or the reverse. The width is a specification question and the flatness is not.
- **`F8`** — the deciding cells do not converge on the quantity the verdict is read on
  (`p90`, not a nominal at some other cell), at nested beam subdivisions `1 / 2`.
- **`F9`** *(added during execution, declared open)* — the preload's predicted rim closure exceeds
  the steric slack `d − 2r_P`, so that the **linear** element is not a representation of the
  structure at that corner. This lattice carries no steric floor between two duplexes, and
  `CLAUDE.md` records that the measured DNA–DNA hydration force moves `0.24 nm` per e-fold at the
  tight end — so wherever this fires, the preload's dishing is an **upper** bound and what is
  quotable is a threshold rather than a value.

### What this task will NOT establish

- It does not settle `CH-0251`. It grades route B on the premise that route B exists.
- It does not re-open the raster, the cross-section, the placement search or the distribution
  rule. Every one of those is `C-0151`'s, `C-0141`'s and `C-0167`'s and is untouched.
- It is not a folding experiment and it is not a measurement of an ssDNA tether in a rim.
