# T-79 — A large-rotation two-spring elastica for `E5`'s arm

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Raised by** | [`C-0034`](../claims/C-0034-guided-arm-anchorage.md) open item 1, and [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md) *"what would settle it"* item 1 |
| **Verification type** | **in-silico** (a planar inextensible elastica with a rotational spring at each end, solved by shooting) **+ logical** (a chord bound that needs no elastica at all) |
| **Maturity** | **TRL 1–3.** Nothing here is measured. |

---

## Formulate

### The state of the question

`E5a16`/`E5g16` — a crossover-hinge flexure — is the only element in this programme that
reaches §3's **desired** 10 nm stroke with no 90° junction anywhere in it. It is placed on two
compositions, each exact in one respect and wrong in the other, and they bracket its arm at
**11.03–12.50 nm**:

| reading | rotation | end condition | arm |
|---|---|---|---|
| `C-0029`'s **series** form `1/k = r²/(nk_θ) + r³/(cEI)`, on `C-0034`'s realised `c` | **exact** (`δ = r sin θ`) | **wrong** — exact only at `ρ_f = 0` (`CH-0044`) | **11.028 nm** |
| `C-0034`'s **two-spring BVP** `c(ρ_n,ρ_f) = 12(ρ_nρ_f+ρ_n+ρ_f)/(ρ_nρ_f+4ρ_n+4ρ_f+12)` | **wrong** — small deflection | **exact** | **12.496 nm** |

`C-0034` reports that *"two errors run opposite ways and very nearly cancel"* and names the
missing composition as this task. `C-0034`'s own third failure mode is *"a large-rotation solve
landing outside the 11.03–12.50 nm bracket, which would mean neither composition brackets the
truth and the placement has to be re-solved."*

### Geometry and sign conventions — fixed before deriving

The arm is **one duplex lying in the sheet plane**, of contour length `L` and bending rigidity
`EI`, joining two rigid bodies (the tile and the substrate) that translate relative to each other
and **do not rotate**.

- Arc length `s ∈ [0, L]`, measured from the **near** (hinge) end. The arm is inextensible: `L` is
  a contour length, not a span.
- `φ(s)` is the tangent angle, measured **counter-clockwise from the undeformed axis `+x`
  toward `+z`**, where `+x` runs along the arm in the sheet plane and `+z` is the stroke
  direction (normal to the sheet). `x(s) = ∫₀ˢ cos φ`, `z(s) = ∫₀ˢ sin φ`.
- **Near end**, `s = 0`: the crossover hinge, a rotational spring `k_n = n k_θ` grounded on the
  **tile**, resisting the near-end rotation `φ(0)`.
- **Far end**, `s = L`: the anchorage, a rotational spring `k_f` grounded on the **other body**,
  resisting `φ(L)`. `C-0034`'s counting theorem fixes its catalogue.
- Loads: a transverse force `F` (along `+z`) and an axial force `H` (along `+x`, **positive in
  tension**) applied by the far body at the tip, plus an optional external tip moment `M₀`
  (used only for the reciprocity gate).
- **Stroke** `δ ≡ z(L)`, positive. **Draw-in** `e ≡ L − x(L)`, the in-plane approach the two
  attachment points demand of each other.

Moment about the cut at `s` of everything to its right, positive counter-clockwise:

&nbsp;&nbsp;&nbsp;&nbsp;`EI φ′(s) = M₀ − k_f φ(L) + F(x(L) − x(s)) − H(z(L) − z(s))`

Differentiating once gives the field equation and the boundary conditions follow by inspection:

&nbsp;&nbsp;&nbsp;&nbsp;**`EI φ″ = −F cos φ + H sin φ`**, &nbsp;
**`EI φ′(0) = k_n φ(0)`**, &nbsp; **`EI φ′(L) = M₀ − k_f φ(L)`**.

At vanishing load this is the boundary-value problem `C-0034` condenses, so its
`twoSpringArmFactor` is the **free** limiting-case gate for the whole solver: `c(∞,∞) = 12`,
`c(∞,0) = c(0,∞) = 3`, `c(0,0) = 0`.

### Units, locked

nm, pN, pN·nm, pN·nm/rad, pN/nm; `k_BT = 4.141947 pN·nm` at **300 K**; aqueous **2 mM MgCl₂**;
40 × 40 nm tile; **45** load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN over the **acceptable**
3 nm stroke = the **33.3333 pN/nm** mandate, and the **desired** 10 nm stroke.

### Acceptance predicates, declared before the run

| | predicate |
|---|---|
| **`P1`** | the elastica's **vanishing-load** limit reproduces `C-0034`'s `twoSpringArmFactor` at all four textbook corners and over an interior grid, to ≤ `1e−6` |
| **`P2`** | the arm is re-placed on the exact composition, and its position **relative to the 11.03–12.50 nm bracket** is reported — inside upholds `C-0034`, outside falsifies its bracket and raises a challenge |
| **`P3`** | the placement condition is discharged on the **secant** (`C-0017`): `45 × R(3 nm)/3 nm = 33.3333 pN/nm`, residual ≤ `1e−7` |
| **`P4`** | the **tangent** at §3's acceptable and desired strokes is reported against `C-0023`'s 40 pN/nm compliance ceiling, and the stability reading is `min_s k_tangent(s)` over the operating range (`CH-0042`, with `CH-0047`'s caveat on which range) |
| **`P5`** | the per-path allowable is read **separately at each stroke** — the element's own tension, the hinge's bond force and the anchorage's link force against `C-0006`'s 10 pN unzip allowable and `CH-0029`'s inverted ladder |
| **`P6`** | the arm **cap** is re-solved as the fixed point it is, under exact rotation, and its clearance of the 10 nm stroke reported |

---

## Plan

### The cheap bounds, which run first

1. **The chord bound — pure geometry, no elastica.** The arm is inextensible, so its two ends can
   never be further apart than `L`:
   &nbsp;&nbsp;`√(x(L)² + δ²) ≤ L` &nbsp;⟹&nbsp; **`e ≥ L − √(L² − δ²)`**.
   At the bracket's own arm lengths and §3's desired 10 nm stroke this is **≈ 4.9 nm**, 14 base
   pairs — 39 % of the arm. **It says, before any solve, that the desired stroke is a
   large-deflection problem and that no linear reading of it can be trusted.**
2. **The rotation at the placement point.** At 3 nm on a ~12.5 nm arm the end rotations are
   ~5–11°, where `sin θ/θ = 0.994–0.998`. So the large-rotation correction *at the placement
   point* is of order 1 %, and the exact arm should land **just above** the BVP's 12.496 nm —
   i.e. **outside** `C-0034`'s bracket, on the long side, by a small margin. Both bracketing
   errors are corrections to the *same* linear reading and both stiffen; they do not straddle it.
3. **The geometric stiffening scale at the desired stroke.** A lever whose restoring arm is
   `r cos θ` stiffens as `1/cos²θ`; at the ~35° end rotation the desired stroke demands, that is
   ≥ 1.5 — a **lower** bound on the factor by which the exact tangent exceeds the linear one.

### Method, and why this and not something else

A planar inextensible **elastica** with a rotational spring at each end, integrated by classical
RK4 over `(φ, φ′, x, z)` and closed by **shooting** on the near-end rotation, with the far-end
moment condition as the residual. The placement length, the cap, the force at a given stroke and
the axially-restrained variant are all bisections on top of that one integration.

- **Why not a finite-element beam.** A co-rotational FE beam would need its own convergence
  study in element count *and* would still be an approximation to the elastica, which here is
  available exactly. The elastica has a **free** verification asset the FE does not: its
  vanishing-load limit is `C-0034`'s closed-form `c(ρ_n, ρ_f)`, which pins the field equation,
  both boundary conditions and every sign at once.
- **Why not oxDNA.** The question is not whether a duplex is a beam — that is inherited
  (`EI = 230 pN·nm²`, a CanDo **model input**) — but what a beam with two elastic end joints does
  at 80 % of its own length. A coarse-grained run would replace an exactly solvable continuum
  question with a sampling problem and would not touch the constants that matter.
- **Cost.** One integration is ~400 RK4 steps; the deepest nesting is placement → force → shoot,
  ~10⁵ integrations, i.e. seconds. The axially-restrained variant adds one more bisection and is
  therefore run at fixed geometry rather than re-placed.

### What would falsify this approach

| # | falsifier | what it would mean |
|---|---|---|
| **1** | the **vanishing-load limit failing to reproduce `twoSpringArmFactor`** at the four corners | the field equation, a boundary condition or a sign is wrong; nothing downstream is worth reading |
| **2** | the placed arm landing **inside** 11.03–12.50 nm | `C-0034`'s bracket stands, the correction is ≤ 13 %, and this task is a confirmation rather than a challenge |
| **3** | the chord bound at the desired stroke coming out **below one base pair** | the axial question is moot, the deflection is small everywhere, and the task reduces to a percent-level correction |
| **4** | the exact **tangent at the desired stroke landing inside 40 pN/nm** | `C-0034`'s design table survives intact and `E5a16` is unchanged |
| **5** | the elastica **stiffness at vanishing load depending on the step count** at second order or worse | the integrator is not RK4 and the convergence gate is meaningless |

### Numbers inherited rather than derived here

`EI = 230 pN·nm²` (CITED, a CanDo **model input**), `k_θ = 13.53 pN·nm/rad` (CITED, FITTED, Chen
et al. via `C-0009`), the anchorage couple 78.235 pN·nm/rad (`C-0029`'s counting theorem via
`C-0034`), the rise 0.34 nm, the interhelical distance 2.69 nm, `C-0006`'s 10 / 65 pN allowables,
Strunz's four shear constants via `CH-0029`, and §3's 100 pN / 3 nm / 10 nm / 45 paths. All are
re-run through their own code rather than tabulated, and every one is re-derived as a gate-5 test.
