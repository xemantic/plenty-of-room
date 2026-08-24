# T-303 — What link stiffness the coupled recovery needs, and what the crossover connector can supply

**Leaf:** `A8.2`
**Raised by:** [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) (`T-297`) §6, where falsifier `F10` was declared OPEN and **FIRED**.
**Reserved claim:** `C-0205`. **Reserved challenges:** `CH-0258`, `CH-0259`. **Reserved queue rows:** `T-309`, `T-310`.

---

## Formulate

### The standing state

`C-0194` established that the crossover's **common** azimuthal mode is `HoneycombGrillage`'s vertical **link**,
not a spring the lattice lacks,
and that `d/2` is the only frame-indifferent arm.
What is wrong is the link's **magnitude**:
`CH-0242`'s own premise gives a bond tension `T = 2 k_θ / r_P` and a span-derived link stiffness `k_R = T/g`,
against `RIGID_LINK_STIFFNESS = 1e4 pN/nm`.

`C-0194` §5 measured that the **free** tile does not care —
`0 of 6` verdicts move over six decades, worst relative spread `0.0380542`.
`C-0194` §6 measured that the **coupled** cells do:
`C-0180`'s two recovered cells are flat at `k_link ≥ 1000` and **not** flat at `100` or at `k_R`.

So the corpus's only coupled recovery — `C-0180`'s `2 of 64` —
rests on a link stiffness **nothing in this repository measures**,
and it is quoted with no link stiffness attached at all.

### The question, in two halves

1. **What can the crossover connector's transverse stiffness actually be?**
   `k_R` is the **tension** term alone, and tension is not the only mechanism.
2. **Where is the threshold, and what is the census either side of it?**
   `C-0180`'s `2 of 64` must be quoted with the link stiffness it is read at.

### Numeric targets

| # | target |
|---|---|
| `P1` | a bracket on the physical link stiffness from **at least two independent routes**, each stated with its premise, and an explicit **ceiling** |
| `P2` | the threshold `k*` at which each of the two recovered cells crosses `T-5b`'s `0.10`, **bisected**, with monotonicity in `k_link` asserted rather than assumed |
| `P3` | all **64** of `C-0167`/`C-0180`'s coupled cells re-graded at every rung of a link ladder that spans the bracket and the threshold, with the flat count emitted per rung |
| `P4` | the same question answered on **route B** (`C-0201`'s tethered turn, the design the 2009 staple order buys), stated as its own reading and not inherited |
| `P5` | convergence taken **at the deciding cell** on the **deciding quantity** (the bisected threshold), and every reproduction closed |

### Acceptance predicate

The task passes when `P1`–`P5` are discharged and the claim states,
with the link stiffness attached,
whether `C-0180`'s `2 of 64` survives at any link stiffness the connector can supply.

### Units and conventions — locked before deriving

- nm, pN, pN/nm, pN·nm, pN·nm/rad. `k_BT = 4.141947 pN·nm` at `T = 300 K`, aqueous 2 mM MgCl₂.
- `W` positive **downward**, toward the electrode (`C-0006`).
- `s` along the helices, `y` across them in the face plane (pitch `3d/2`), `z` through the thickness (pitch `d√3/2`).
- Honeycomb `d = 2.536 nm` (SAXS, Fischer et al.); `r_P = 0.908637858 nm` (`T-71`, measured on 13 084 linkages);
  `g = d − 2 r_P = 0.718724283 nm`; rise `0.34 nm/bp`.
- Cross-section `10 × 6`, block extent `116 bp = 39.44 nm`, raster `102 / 109` (`C-0151`, drawable), 435 staple bonds and 59 raster turn ties at `s = ±L/2`.
- `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only;
  §3's 100 pN over the face; `C-0017`'s mandate at the **acceptable** clause; `C-0058`'s rim-graded 5:1 at a 6.7 nm band;
  `C-0087`'s measured depth incorporation; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10.
- A **link stiffness** is a stiffness on the residual `R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)`, in pN/nm — `C-0194`'s coordinate, unchanged.

---

## Plan

### The cheap bound runs first, and it is the whole of `P1` — no solver at all

Three closed-form routes to the same coordinate, each one multiplication:

1. **Tension.** `C-0194`'s own `k_R = T/g` with `T = 2 k_θ/r_P`.
   Its premise (`CH-0242`) is that *both* eigenmodes of the span form are one mechanism,
   so it attributes the **whole** of `k_θ` to the span — the largest tension the attribution admits,
   and therefore an **upper** bound on the tension term and a **lower** bound on the total.
2. **Chen et al.'s softened bond, read on the displacement axis.**
   `CLAUDE.md` already records the comparison and nobody has made it:
   `Gen1Tile.crossoverInPlaneStiffness = 2αS/(100a) = 64.7058824 pN/nm` is
   *"the same two phosphate bonds on the orthogonal axis"*.
   A relative **normal** displacement and a relative **axial** slip of two crossover-bonded duplexes
   are the same kind of coordinate — a displacement transverse to the connector —
   and `CLAUDE.md`'s own *an isotropic element cannot be stiff across and soft along*
   makes them equal for a softened covalent bond.
   This route is **independent of `k_θ`**: it substitutes the stretch modulus `S`, not the bending rigidity `B`.
3. **The connector's own bending.** A beam of span `g` whose two ends are displaced transversely
   while each is held by a rotational spring `k_r` against its own duplex has
   `k_B = c(ρ)·EI/g³` with `c(ρ) = 12ρ/(6+ρ)`, `ρ = k_r g/EI` — derived here, exactly `0` at a pinned end
   and exactly `12` at a clamped one, so `c ∈ [0, 12]` is a bracket that needs no `k_r`.
   `EI = L_p k_BT` over the corpus's own ssDNA bracket, `b = 1.34–2.84 nm`, `L_p = b/2`.

A fourth term is computed and reported for its **sign**, not its size:
the DNA–DNA pair interaction is **central**, so its contribution to a coordinate perpendicular to
the line of centres is `V′(d)/d` per unit length — **negative** for a repulsive pair,
which `CLAUDE.md` records this one is at every separation.
It **lowers** the ceiling.

The ceiling is then `max over the routes of (tension or softened bond) + 12 EI/g³`,
and the whole of `P1` is arithmetic.

### Why the expensive half is cheap too

`linkStiffness` is already a `HoneycombGrillage` constructor argument (`C-0194` §1, row 3),
so no element, no matrix and no influence bank changes.
`HoneycombGrillage.factorisation` is `by lazy`, so one lattice costs one factorisation and then
one back-substitution per station.
A 64-cell census at one rung is `2` lattices (the two composite fractions) and `440` point-load solves,
which is what `T-279` already pays twice.

The bisection is on `log₁₀ k_link`, because the readings in `C-0194` §6 fall as `p90_∞ + A/k`
over three decades, so a log axis is where the crossing is well conditioned.
It is guarded by `F4`: **a verdict that is not monotone in a swept variable has no threshold**
(`CLAUDE.md`, on `C-0070`'s lateral seat), so monotonicity is asserted over the whole ladder
**before** any bisection is believed.

### Route B is a citation and a direction, not a re-run

`C-0201`'s committed result file already carries its `linkStiffness` block:
the tightest tethered cell reads `0.125832006 / 0.12643693 / 0.132431202 / 0.141095376`
at `1e4 / 1e3 / 1e2 / 41.4338953` — **monotone the same way and flat at none of them**.
So on the built object there is **no threshold to bisect**, and re-running it would buy a fourth digit
on a verdict that does not move. It is read out of the artifact and cited, and `F9` says so.

### What would falsify this approach

- If `p90` is **not** monotone in `k_link` at the deciding cells, the bisection is a fiction and the
  deliverable becomes a ladder with no threshold (`F4`).
- If the two independent routes to the connector stiffness disagree by more than an order of magnitude,
  the bracket is not a bracket and the honest answer is a threshold alone (`F6`).
- If the census at `k_link = 1e4` does not reproduce `C-0180`'s `2 of 64`, this is a different object (`F10`).

---

## Falsifiers, declared before the run

| # | statement | fires if |
|---|---|---|
| `F1` | a **uniform** pressure on the free tied lattice dishes exactly zero at **both ends** of the link ladder — `CLAUDE.md`'s standing falsifier, re-taken because a link stiffness moves every entry of the matrix | the peak dishing over the stroke exceeds `1e-9` at either end |
| `F2` | this study's lattice builder at the default link stiffness is the object `C-0180` measured — the free tied and untied tiles reproduce `C-0175` §9 / `C-0180` §2 | any reproduction departs by more than `1e-8` |
| `F3` | the two recovered cells reproduce `C-0194` §6's six-rung zero-eigenstrain table | any of the twelve values departs by more than `1e-7` |
| `F4` | **DECLARED OPEN.** `p90` is monotone **decreasing** in `k_link` at every graded cell over the whole ladder | any cell's `p90` rises with `k_link` by more than `1e-9` |
| `F5` | **DECLARED OPEN — this is the deliverable's question.** The ceiling on the physically supportable link stiffness reaches the threshold, i.e. the recovery survives | the ceiling is at or above the smaller of the two bisected thresholds |
| `F6` | the two `k_θ`-independent routes to the link stiffness (span-law tension and Chen et al.'s softened bond on the displacement axis) agree within one order of magnitude | their ratio is outside `[0.1, 10]` |
| `F7` | the bending continuum `c(ρ) = 12ρ/(6+ρ)` has the two textbook limits exactly, and `k_B` scales as `L_p/g³` under a common rescaling of every length | either limit is off by more than `1e-12`, or the scaling departs by more than `1e-12` relative |
| `F8` | the bisected threshold is converged at the deciding cell — beam subdivision `1 → 2` and dishing grid `81 → 161` | either moves the threshold by more than `5 %` |
| `F9` | **DECLARED OPEN.** Route B has a threshold at all, i.e. some link stiffness in `C-0201`'s swept range makes its tightest tethered cell flat | any of `C-0201`'s four `linkStiffness` readings at that cell is below `0.10` |
| `F10` | the census at `k_link = 1e4` reproduces `C-0180`'s `2 of 64` | the flat count at that rung is not 2 |
