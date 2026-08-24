# T-310 — A bond's normal link is two mechanisms, and the coupled recovery now turns on the one nobody prices

**Leaf:** `A8.2`
**Raised by:** [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md) (`T-303`) §5 and [`CH-0259`](../challenges/CH-0259-one-scalar-for-two-mechanisms.md).
**Reserved claim:** `C-0208`. **Reserved challenges:** `CH-0264`, `CH-0265`. **Reserved queue rows:** `T-315`, `T-316`.

---

## Formulate

### The standing state

`C-0194` settled that `HoneycombGrillage`'s vertical **link** *is* the crossover's common azimuthal mode:
its residual `R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)` is a function of the **sum** of the two rolls,
and `d/2` is the only frame-indifferent arm.

`C-0205` then priced that link three ways and found a **ceiling of `254.808095 pN/nm`**,
against thresholds of **`834.060958`** and **`607.396049 pN/nm`** at the two cells `C-0180` recovered —
so the whole 64-cell census reads `0 of 64` at every rung the connector can supply,
and `C-0180`'s `2 of 64` is a reading at the numerical penalty `RIGID_LINK_STIFFNESS = 1e4 pN/nm`.

`C-0205` §5 and `CH-0259` then observed that this ceiling is a **shear** ceiling.
`W` is the deflection **normal to the face**.
At an **in-plane** bond `unitZ = 0`, so a relative `W` displacement is a pure transverse shear of the connector
and the ceiling is exact.
At a bond running **through the thickness** `unitZ² = 0.75`,
so three quarters of a relative `W` displacement is a change of the interhelical **separation** —
a different mechanism, resisted by different things, and not priced anywhere in this corpus.
Two of every three bonds of a `10 × 6` honeycomb block are of that kind.

And the same source file already knows how to resolve two mechanisms onto one residual:
`HoneycombTetherElement.normalStiffness` is `tangent·unitZ² + secant·unitY²`,
the central-force decomposition `K = (df/dx) n̂n̂ᵀ + (f/x)(I − n̂n̂ᵀ)` projected onto the link's own gradient.
A **bond**'s link carries the constructor's single `linkStiffness` at every direction.

### The question

1. Can the **radial** constant — the stiffness against a change of interhelical separation — be **bounded** at all?
   `C-0205`'s recorded search found no published number for a crossover's stiffness against a relative
   normal displacement. The honest shape may again be a ceiling and a threshold.
2. What does the census read once `HoneycombGrillage` resolves a bond's link by the bond's own direction?
   `C-0205`'s `0 of 64` is a reading at a **uniform** scalar below the shear ceiling;
   at the resolved link 300 of 435 bonds are stiffer than that ceiling and 135 are exactly at it.
3. Which is the quantity the corpus actually needs — and it is not a uniform `k_link` at all,
   but the **radial** constant, with the shear pinned at its own ceiling.

### Naming, fixed here

`CH-0259` calls the new constant **axial**.
`axial` already means *along the duplex beam* everywhere in `HoneycombGrillage`
(`axialPinBeam`, `axialEnergy`, `axialRelaxed`, `HoneycombTetherElement.axialStiffness`),
so the coordinate along the **line of centres between two duplexes** is called **radial** throughout this task,
after the central-force decomposition it comes from.
`CH-0259`'s *axial* and this task's `radialLinkStiffness` are the same number.

### Numeric targets

| # | target |
|---|---|
| `P1` | the per-bond resolution `k_link(bond) = k_radial·unitZ² + k_shear·unitY²`, reproducing `CH-0259`'s published `475.448622` and `1211.56918 pN/nm` at its own two candidates, with the `135 / 300` bond census and `⟨unitZ²⟩` asserted rather than assumed |
| `P2` | a bracket on the **radial** constant from at least two routes, of which at least one is not a connector construction, with every term's sign stated |
| `P3` | a per-bond link stiffness in `HoneycombGrillage`, whose default is **bit-identical** to the standing object at `assembleLoad` over every degree of freedom, with the crossover site set asserted beside it |
| `P4` | all **64** of `C-0167`/`C-0180`'s coupled cells re-graded at the resolved per-bond link, at each radial candidate, with the flat count emitted per candidate |
| `P5` | the **radial threshold** — the radial constant at which each of the two recovered cells crosses `T-5b`'s `0.10`, with the shear pinned at `C-0205`'s ceiling — bisected, with monotonicity asserted first |
| `P6` | route B carried rather than resolved, with its own state named and the direction of the unmeasured half stated |

### Acceptance predicate

The task passes when `P1`–`P6` are discharged and the claim states,
**with the radial constant attached**,
whether `C-0205`'s `0 of 64` stands or reverses,
and whether the radial constant can be bounded from a source or only bracketed by construction.

### Units and conventions — locked before deriving

- nm, pN, pN/nm, pN·nm, pN·nm/rad, pN/nm² (= 1 MPa). `k_BT = 4.141947 pN·nm` at `T = 300 K`, aqueous 2 mM MgCl₂.
- `W` positive **downward**, toward the electrode (`C-0006`).
- `s` along the helices, `y` across them in the face plane (row pitch `3d/2`), `z` through the thickness (layer pitch `d√3/2`).
- A bond's unit line of centres is `(unitY, unitZ)` with `unitY² + unitZ² = 1`, as `HoneycombGrillage` already builds it.
- **Radial** = along `(unitY, unitZ)`. **Transverse** = perpendicular to it, in the same plane. The link's own gradient direction is `z`, so `k_link = k_radial·unitZ² + k_transverse·unitY²`.
- Honeycomb `d = 2.536 nm` (SAXS, Fischer et al.); `r_P = 0.908637858 nm` (`T-71`, measured on 13 084 linkages); `g = d − 2 r_P = 0.718724283 nm`; rise `0.34 nm/bp`.
- `k_θ = 13.5294118 pN·nm/rad`, `k_s = 64.7058824 pN/nm` at `α = 1`; `S = 1100 pN`.
- Cross-section `10 × 6`, block extent `116 bp = 39.44 nm`, raster `102 / 109` (`C-0151`, drawable), **435** staple bonds and **59** raster turn ties at `s = ±L/2`, zero prestrain.
- `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only; §3's 100 pN over the face; `C-0017`'s mandate at the **acceptable** clause; `C-0058`'s rim-graded 5:1 at a 6.7 nm band and its equal-spring twin; `C-0087`'s measured depth incorporation; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10.
- Positive is repulsive for a pair force; positive is stiffening for every stiffness.

---

## Plan

### The cheap bound runs first, and it is arithmetic

`CH-0259` already publishes the resolution at its own two candidates.
What this task adds before any solve is a **third term on the radial axis that is measured rather than constructed**,
and it costs one derivative of a function already in the tree.

Two crossover-bonded duplexes also interact **directly**, and that interaction is central.
`C-0205` §1b carried the **transverse** half of the central-force tensor, `V′(d)/d`, for its **sign** —
negative wherever the pair repels — and quoted it *per unit of repulsive force per unit length*,
i.e. it never evaluated it.
The **radial** half is `V″(d)`, which for a repulsive decaying law is **positive**, and this repository
already carries the measured law: `MengMagnesium`'s osmotic-stress equation of state
(Meng, Timsina, Bull, Andresen & Qiu, *Biophys. J.* **118**:3019, 2020, `Π_R = 201.8e3 pN/nm²`, `λ = 0.24 nm`),
with `f_∥(d) = Π(d)·d/√3` exact for a hexagonal array.
So `k_pair,radial = −(d f_∥/d d)·L_contact` over the `21 bp` of interface one honeycomb crossover owns,
in closed form, with `d = 2.536 nm` **above** the fit's own `2.45 nm` data floor.

That is the difference between a bracket built entirely out of constructions and one with a measured term in it,
and it also supplies a **force cross-check nobody has made**:
the pair's outward force per crossover against `C-0194`'s implied inward bond tension `T = 2k_θ/r_P`.
Two utterly independent constructions of what holds a honeycomb crossover at its built separation.

### The implementation is one parameter and three call sites

`linkStiffness` has been a `HoneycombGrillage` constructor argument all along.
Adding `radialLinkStiffness: Double? = null` beside it, with

&nbsp;&nbsp;&nbsp;&nbsp;`linkStiffnessAt(unitY, unitZ) = if (radial == null) linkStiffness else radial·unitZ² + linkStiffness·unitY²`,

touches the bond link, the tie link, `linkEnergy` and `turnLinkOffsetLoad`, and **nothing else**.
The `null` default returns the scalar **by identity rather than by arithmetic**,
so the assembled matrix is bit-identical rather than nearly so —
`k·g_i·g_j` with the same `k`. `linkEnergy` is branched for the same reason:
`0.5·k·Σx²` and `0.5·Σ k x²` are not the same floating-point number, and `C-0194` §2 quotes `6.7528608` from it.
The tether element is **not** touched: it already resolves.

Cost: the census is 64 cells × 4 000 realisations per radial candidate over `C-0058`'s exact Woodbury surrogate,
which is what `T-303` already pays per rung; the radial bisection is 16 surrogate builds per deciding cell.
No new element, no new matrix, and every influence bank in the corpus stands.

### What would falsify the approach

If the resolved per-bond values do not reproduce `CH-0259`'s own `475.448622` and `1211.56918`,
the resolution is not the one the challenge is about and the whole task is answering a different question.
If the `null` default is not bit-identical, the change is not a refinement of the standing object
and no comparison against `C-0167`, `C-0180`, `C-0205` or `C-0207` is admissible.

### What this task does NOT do

It does not re-open the placement search, the distribution rule, the cross-section, the raster or the load case.
It does not re-grade route B: `C-0201`'s own committed sweep and `C-0207`'s `756 of 756` are **read**, and
what the per-bond link would do to them is named as an open question rather than answered.
It does not withdraw `C-0205`'s ceiling, which is exact on the coordinate it is written on.

---

## Falsifiers, declared before the run

| # | fires if | expected |
|---|---|---|
| `F1` | the default (`radialLinkStiffness = null`) lattice is **not** bit-identical to the standing object at `assembleLoad` over every degree of freedom, or its crossover site set differs | must not fire |
| `F2` | a uniform pressure on the free per-bond lattice does not dish exactly zero — `CLAUDE.md`'s standing falsifier, re-taken because a per-bond link moves every entry of the matrix | must not fire |
| `F3` | the resolved per-bond link does not reproduce `CH-0259`'s `475.448622` and `1211.56918 pN/nm`, or `C-0205`'s ceiling `254.808095`, at the emission precision | must not fire |
| `F4` | the bond census is not `135` in plane and `300` through the thickness, or `⟨unitZ²⟩` is not `0.0` and `0.75` | must not fire |
| `F5` | **OPEN** — the census at the resolved per-bond link recovers cells `C-0205`'s `0 of 64` refuses | either answer is the result |
| `F6` | **OPEN** — the bisected **radial threshold** falls **inside** the radial bracket, so the corpus's own candidates do not decide the question | either answer is the result |
| `F7` | the measured pair radial term is not positive, or the equation of state is evaluated below its own `2.45 nm` data floor | must not fire |
| `F8` | **OPEN** — the pair's outward force per crossover and `C-0194`'s implied inward tension disagree by more than **one order of magnitude** | either answer is informative |
| `F9` | **OPEN** — the verdict at the deciding cell moves between beam subdivisions `1` and `2`. `T-303`'s own `F8` fired on the *value* by 21 % and not on the verdict | either answer is the result |
| `F10` | any reproduction of `C-0205`, `C-0194` or `C-0180` at the default fails to close at the emission precision | must not fire |
