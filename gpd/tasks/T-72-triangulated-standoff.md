# T-72 — Re-price the triangulated standoff as a **stability** remedy, not a rigidity one

**Covers `T-66`** (*"a triangulated standoff — the only rigid out-of-plane mounting the literature actually shows"*),
which `T-72` re-scopes rather than replaces:
the two are one question and are formulated, planned, executed and filed together here.

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a multi-leg frame assembled at the head and solved into `C-0030`'s coupled beam) **+ logical** (a frame couple is a rank-one tensor on the leg offsets, so the restraint it supplies and the release it costs live on *orthogonal* axes and their sum is conserved) **+ literature** (the triangulated motif the programme has been citing, re-fetched and re-read) |
| **Units** | nm, pN, pN·nm, pN·nm/rad, pN/nm; `k_BT = 4.141947 pN·nm` at 300 K; aqueous 2 mM MgCl₂ |
| **Status** | **DONE** — claim [`C-0036`](../claims/C-0036-triangulated-standoff.md) |

---

## Formulate

### What is being asked, and why the question changed

`T-66` was raised by [`C-0028`](../claims/C-0028-standoff-base-joint.md) as a **rigidity** question:
the literature's only rigid out-of-plane mounting is triangulated
(*"were held rigidly at this angle with a **set** of double-helical spacers"*),
so would a truss give the standoff the base couple a single duplex cannot?

[`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) changed the question by a **counting theorem**.
A B-form duplex has two backbones,
so a duplex **end** presents exactly two strand termini,
so a base joint has at most **two** covalent links,
whose separation is the terminal chord `2 r_P sin(Δ/2) ≤ 2.0 nm`.
Two links lie on a **chord**, and a couple on a chord has an **axis**:
it restrains rotation about the chord's perpendicular bisector — up to `2k_bond,θ + 2k_bond,s r_P²` = **78.24 pN·nm/rad** —
and about the chord itself leaves only `2k_bond,θ` = **13.53 pN·nm/rad**, which *is* `C-0028`'s `B1`.
**A column buckles about its softest axis**, so `P6` fails at every length
and the standoff branch closes at §3's *desired* 10 nm stroke.
The one-sided blunt-end contact is **3.7× short** of carrying that axis.

So the truss is no longer wanted for rigidity in the loaded plane;
it is wanted to **restore the axis the two-link base leaves free**.
That is `T-72`.

### And why the trade looks forced

[`C-0028`](../claims/C-0028-standoff-base-joint.md) establishes that
**the standoff's sway IS the flexure's draw-in** — one coordinate, two names —
and [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) establishes that the draw-in is not a compliance
but a kinematic **SUPPLY**, `Φδ` per end,
**first order in the stroke where the demand is second order** and **3.09×** larger than it at the design point.
A head that cannot sway cannot supply it.
`C-0030` prices the adverse case at 42–61 pN/nm with **no window at any length**.

**The pre-registered prediction of this task, written before any code ran:**

> the conflict is real for a *single* coordinate and **not** real for a truss,
> because the axis a two-link base leaves free is **orthogonal** to the loaded plane.
> A frame couple `k_a Σ d_i²` is a rank-one tensor on the leg offsets,
> so laying the legs **across** the flexure axis restrains the free axis and adds **nothing** to the loaded plane,
> and laying them **along** it does the exact opposite.
> If that is right, the answer is a **partially** triangulated head,
> and the azimuth is worth a whole window at no cost in material —
> `C-0028`'s own *"orientation is worth 9.65× and it decides the design"*, one level up.
>
> **The falsifier of the prediction is a leg azimuth at which both requirements hold nowhere,
> or one at which the loaded-plane frame term is not zero when the legs are collinear across.**

### Geometry and sign conventions, fixed before deriving

- The sheet lies in the `x–y` plane; `z` is the sheet normal; all legs run along `+z` with length `ℓ`.
- **`x` is the flexure's own axis.** The flexure spans `L` in `x`, deflects `δ` in `z`, and its ends draw in along `x`.
- **The loaded plane is `x–z`**: the standoff head's coordinates there are `(u_x, φ_y)`,
  the pair `C-0030`'s `CoupledJointFlexure` consumes, and `u_x` is `C-0028`'s sway = the draw-in.
- **The free plane is `y–z`**: coordinates `(u_y, φ_x)`. Nothing loads it in the nominal design;
  it is the plane the column buckles in, and it is the whole of `C-0029`'s verdict.
- A leg's **base chord** is laid along `x`, so the base's strong axis (`78.24 pN·nm/rad`, hard, convention-free)
  restrains `φ_y` and its weak axis (`13.53`) restrains `φ_x`.
  This is `C-0029`'s favourable orientation, and its azimuthal quantum costs at most 8.4 % (`cos²`).
- Leg offsets from the head centroid are `(x_i, y_i)`, `Σx_i = Σy_i = 0`.
- `δ > 0` is `C-0030`'s **favourable** sense — the midspan moving *away* from the plane the leg bases stand on,
  which tilts the heads *inward*. `T` is positive in **tension**.
- **The head cap is a rigid body of finite rotational stiffness `k_tie`**, in series with the frame couple.
  It is not assumed infinite: that assumption is what this task has to price, and it is swept.

### Acceptance predicate

`T-72`: *whether a second standoff restores the free axis at an affordable cost in sway.*
`T-66`: *whether two or three duplexes in a truss can carry the flexure's end shear without buckling, and what draw-in release survives.*

Discharged against the seven predicates `C-0028`/`C-0030` already carry, evaluated on the truss:

| | predicate |
|---|---|
| `P1` | the head supports the flexure — transverse stiffness ≥ 10× the beam's own per path, dead band ≤ 0.1 nm |
| `P2` | placement: 45 elements present **33.3333 pN/nm** secant at §3's acceptable 3 nm, by construction |
| `P3` | tangent ≤ `C-0023`'s **40 pN/nm** compliance ceiling |
| `P4` | beam axial tension ≤ the **10 pN** unzip allowable at the desired stroke |
| `P5` | inside `C-0017`'s buildable envelope (`ℓ ≤ 10 nm`, span ≤ 60 nm) |
| **`P6`** | **the truss's critical load, taken on its SOFTEST plane, ≥ the flexure's own end shear at §3's DESIRED 10 nm stroke** — the predicate `C-0029` closed the branch on |
| `P7` | the flexure itself does not buckle under the compression the joint imposes |

and two the truss adds, because a truss has parts a single standoff does not:

| | predicate |
|---|---|
| **`P8`** | **the draw-in that survives**: `Φδ ≥ e(δ)` at the placement stroke, i.e. the joint still supplies more than the geometry demands, so `C-0030`'s favourable mounting still inverts the membrane term |
| **`P9`** | **no leg is overloaded by the head moment**: the peak per-leg compression, `P/n + M x_i/Σx_i²`, is what `P6` is judged on, not the mean |

**PASS means model-consistent and traceable at TRL 1–3. Nothing here is measured, and the motif is not demonstrated.**

---

## Plan

### The cheap bound, which runs first

Two divisions, before any matrix, any root find and any span solve.

1. **The frame couple against the bond couple.** A leg's axial stiffness is `k_a = series(S/ℓ, k_z,base)`
   — 44.0 pN/nm at `ℓ = 8 nm` — and two legs at the SAXS interhelical distance carry
   `k_a Σd_i² = 44.0 × 2 × (1.345)²` = **159 pN·nm/rad**, against the **13.53** the free axis has.
   If that ratio were below ~2 the truss could not restore the axis and the task would close in a paragraph.
2. **The conservation identity.** For a two-leg row of separation `w` at azimuth `θ` to the flexure axis,
   `Σx_i² = (w²/2)cos²θ` and `Σy_i² = (w²/2)sin²θ`, so **`Σx_i² + Σy_i² = w²/2` identically**:
   the truss has a fixed budget of frame couple and the azimuth spends it.
   If the two were not complementary the "partial triangulation" answer would not exist.

**What would falsify this approach.** Three declared falsifiers:

1. **the cheap bound failing** — the frame couple not exceeding the bond couple by enough to move `P6`;
2. **`Σx_i²` not vanishing for a cross-row**, i.e. the loaded plane inheriting frame stiffness anyway,
   which would make the conflict real and close `T-66` as `C-0028` feared;
3. **`P3` failing for the cross-row** — the doubled sway stiffness alone taking the tangent past 40 pN/nm,
   in which case the cost of the second leg is unaffordable regardless of azimuth.

### The method, and its cost justification

**Assemble at the head, then re-run `C-0030`'s pipeline unchanged.**

Each leg is `C-0030`'s own `standoffTipFlexibility(EI, ℓ, k_θb)`, inverted to a 2 × 2 stiffness `K_leg`.
`n` legs share a rigid cap, so they act in parallel;
their **axial** stiffnesses at their offsets add a frame couple to the head's rotation and to nothing else,
because a rigid-body rotation about the centroid stretches leg `i` by `∓d_i φ` and translates none of them:

&nbsp;&nbsp;&nbsp;&nbsp;**`K_truss = n·K_leg + [[0, 0], [0, k_frame]]`**, &nbsp;
`k_frame = series(k_a Σd_i², k_tie)`, &nbsp; `k_a = series(S/ℓ, k_z,base)`.

Inverting `K_truss` gives a `StandoffTipFlexibility`,
which is exactly what `CoupledJointFlexure`, `coupledFlexureSpan`, `coupledBucklingStroke`
and `peakFlexureCompression` already consume —
so **every number in this task is `C-0030`'s pipeline with one object substituted**,
and the `n = 1`, `d = 0` case must reproduce `C-0030` **identically**.
That is the whole cost justification: the expensive parts are already written, tested and filed,
and the new object is a 2 × 2 inverse.

**Buckling** is `C-0028`'s own two-spring sway determinant, one level up:
the cap enforces a common head rotation, so per column the external head spring is `k_frame/n` and

&nbsp;&nbsp;&nbsp;&nbsp;**`P_c,plane = n·u²EI/ℓ²`**, &nbsp; `u` the first root of `sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u`,
&nbsp; `ρ_b = k_θb ℓ/EI`, &nbsp; `ρ_h = (k_frame/n)ℓ/EI`,

solved in **both** planes with the base's strong constant in one and its weak constant in the other, and

&nbsp;&nbsp;&nbsp;&nbsp;**`P_c,truss = min(P_c,loaded, P_c,free)`** — *a column buckles about its softest axis.*

The duty is the element's own end shear at the desired stroke (`CH-0037`), never the mandate secant,
and the deliverable is also quoted as **the stroke at which the truss buckles**, which needs no margin convention.
Every critical load is reported on CanDo's `EI = 230 pN·nm²` **and** on Fields et al.'s implied 172.9,
which is 25 % lower and is the measured end.

**The lattice.** Leg offsets are not continuous. A standoff seats on a sheet duplex,
so an offset is `(i × 0.34 nm along a helix, j × 2.69 nm across helices)`
with a steric floor of one duplex diameter (2.0 nm) between leg axes.
Per `CLAUDE.md` — *"sampling a continuous angle on a discrete lattice is not a sweep"* —
the sweep is over **realisable placements**, with the continuous azimuth carried only to exhibit the conservation identity.

**Why not oxDNA.** The counting theorem is not a model and cannot be overturned by one;
the frame couple is an equilibrium statement about `n` axial springs at known offsets;
and the two constants the answer rests on (`k_s`, `k_θ`) are already the programme's least-determined numbers (`T-9`).
A coarse-grained solve would inherit both and add nothing the 2 × 2 does not already say.
What it *could* add — whether two 90° junctions 2 nm apart on one sheet duplex close at all — is `T-71`'s question, not this one.

### Sensitivities declared in advance

`k_tie` (rigid → two crossovers → one crossover), `k_s` over `C-0020`'s four decades,
`α` over Chen et al.'s `[0.6, 1.2]`, `EI` on both rigidities, the draw-in model (chord → shape),
the base reading (hard 180° ceiling → nominal 120° groove), and the mounting sense.
