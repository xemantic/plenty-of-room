# T-15 — The in-plane (membrane) load path into the tile, by shear lag

| | |
|---|---|
| **Leaves** | `A8.2` (structural rigidity / mode analysis — *"identify the dominant compliance term … and budget stiffness at the joints"*), `A1.2` (the anchoring scheme it prices) |
| **Problem definition** | §6 tasks 5 and 5b; questions §4(f) and §4(g); parameters §3 |
| **Verification type** | in-silico (an **in-plane** beam-and-bar-and-connector grillage written for this task, run beside the orthotropic shear-lag membrane it discretises) **+ logical** (an equilibrium bound that decides the sign of the answer before any lattice is assembled) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0020`](../claims/C-0020-in-plane-shear-lag.md) |
| **Raised by** | [`C-0014`](../claims/C-0014-lateral-confinement.md) (`T-12`), which had to apply an **out-of-plane** concentration factor to an **in-plane** load and said so in its own validity range |

---

## Formulate

### Why this task exists

`C-0014` closes its per-anchor force section with a paragraph that names its own weakest step:

> **On `C-0009`'s concentration factor.** It is applied at its worst value (7.6×) throughout, and that is
> **conservative and known to be so**: `C-0009` measures it for an *out-of-plane* anchor reaction, where the
> load is confined to an `ℓ`-sized bending patch. A lateral tether loads the tile as a **membrane**, carried
> by duplexes in tension at `S = 1100 pN` each, and it spreads further. The correct treatment is a shear-lag
> problem on a membrane-loaded lattice and **nobody has done it**; it is queued as `T-15`.

The stake is not a refinement. `C-0014`'s one `T-2` window constraint is a **footprint**:

&nbsp;&nbsp;&nbsp;&nbsp;`L_min = δ √(S n /(2A))`, with `n` the concentration factor and `A` the per-path allowable,

so `L_min ∝ √n` and the factor enters the deliverable as a square root. At `n = 7.6` §3's *desired* 10 nm
stroke needs a **93 nm** tether — a frame standing off by more than twice the tile's own width — and
`C-0014` concludes that *"lateral confinement and the desired stroke are incompatible at a fixed device
footprint"*. At `n = 1` the same stroke needs **34 nm**, which is what the *acceptable* 3 nm stroke already
costs today, and the incompatibility is a different statement or no statement at all.

### The physics that makes this a different problem, stated before anything is computed

A surface-parallel tether pulls **in the plane of the sheet**. The load therefore enters through

- **duplex axial stiffness** — a bar of stretch modulus `S = 1100 pN`, and
- **crossover in-plane shear** — the relative sliding of two adjacent helices at a crossover,

and **not** through plate bending, which is the only channel `C-0009` and `C-0015` modelled.
Three consequences fix the shape of the answer in advance.

1. **There is no distributed lateral load to collect.** `C-0009`'s factor exceeds one because an out-of-plane
   anchor is a *reaction*: it gathers the foundation load from an area of order `ℓ²` around itself, so the
   anchor force `8q ℓ_∥ℓ_⊥` is **not** the applied force and the peak crossover force is measured against an
   equal share of it. Laterally the layer supplies **exactly zero** restoring stiffness (`C-0010`, by
   translation invariance), so a lateral tether collects nothing: **its own tension is the entire load**, and
   every internal path carries a *fraction* of it. That is an equilibrium statement, it costs nothing, and it
   bounds the deliverable at 1 before any lattice runs.
2. **The transfer length is a shear-lag length, not a bending length.** For duplexes of axial stiffness `S`
   coupled at crossovers of in-plane shear stiffness `k_s` spaced `p` apart along one interface, the
   load-transfer length is `Λ = √(S p / k_s)`, and the across-helix load-sharing length of an `N`-duplex strip
   is `Λ₁ = √(S /(2 (k_s/p)(1 − cos(π/N))))`. Both are properties of the *lattice*, derived here for the
   single-layer Rothemund sheet rather than lifted from a fibre-composite formula.
3. **The answer is direction-dependent, and the two directions use different load paths and different
   allowables.** Along the helices the load runs down a duplex and is shed to its neighbours slowly; across
   them every part of it must cross an interface immediately. `C-0015` has already shown that searching the
   square diagonal of a 25.6×-anisotropic space is the wrong slice, so both principal directions **and** the
   intermediate angles are swept, and the worst is reported.

### The quantity to produce, defined so it cannot be misread

`C-0009` reports its factor as **peak per-load-path force ÷ equal-share force**. `C-0014` applied that number
as **peak per-load-path force ÷ applied force**. Those are different quantities — out of plane they differ by
the ~9.3 paths on the `ℓ`-contour — and only the second is the `n` that `L_min` contains.
Both are therefore produced here, named apart and never mixed:

| symbol | definition | what it is for |
|---|---|---|
| **`η`** — *transfer ratio* | peak force in **one** load path ÷ the **applied tether force** | **the deliverable**: `C-0014`'s `n`, the multiplier `L_min` needs |
| `C` — *concentration factor* | peak force in one load path ÷ the **equal share** over the load paths available | comparable with `C-0009`'s 2.3–7.6, and with the continuum's `n = 0` mode |

`η` is reported per **path class** — duplex axial force at a nick, crossover in-plane force, duplex in-plane
shear — because the three are judged against different allowables, and the binding one is
`A_eff = min over paths of (A_path / η_path)`, which is what `L_min` must actually use.

### The question, as a numeric target

Produce, on an in-plane lattice built from `C-0009`'s and `C-0015`'s own ingredients:

1. the **in-plane transfer ratio `η`** and concentration factor `C`, per path class, for a tether pull
   **along** the helices and **across** them, and over the intermediate angles;
2. the **worst case over anchor placement** — the crossover column phase over all 32 base pairs and the
   attachment's registration within the crossover unit cell, `C-0015` having found registration worth
   ×1.43–1.60 out of plane;
3. the **shear-lag transfer length** `Λ` and the across-strip sharing length `Λ₁` in the lattice's own form,
   with the sensitivity to the one undetermined input (`k_s`) reported over four decades;
4. the **continuum orthotropic shear-lag membrane run beside the lattice**, with the excess quoted, per
   `CLAUDE.md`'s standing rule that a lattice effect is claimed only against its continuum;
5. the resulting per-load-path forces judged against `C-0006`'s allowables — 10–15 pN unzip, ~48–65 pN
   single-duplex shear, 65 pN hard ceiling — and **not** against §4(f)'s 35–60 pN whole-cross-section band;
6. the propagation into `C-0014`: `L_min` at a 3 nm and a 10 nm stroke, with an explicit verdict on whether
   *"lateral confinement and the desired stroke are incompatible at a fixed footprint"* survives.

### Acceptance predicate

Discharged when all six hold.

1. `η` and `C` are produced for **both** principal directions and for the intermediate angles, per path class,
   with the definitions above kept apart and the equal-share denominator stated for each.
2. The worst case over **anchor placement** is a sweep, not a sample: all 32 base-pair column phases, and the
   attachment registration over one full crossover repeat along the helices and over every duplex across them.
3. `Λ` and `Λ₁` are **derived for this lattice**, asserted as identities against the lattice's own numerical
   decay rather than quoted, and the whole answer is reported as a function of `k_s` over ≥ 4 decades
   including the rigid-constraint limit — because `k_s` is not determined by anything accessible.
4. The **continuum shear-lag membrane** is solved on the same footprint with the same constants and the excess
   `lattice / continuum` is quoted at a stated station, with the continuum's own failure at a point load
   (a logarithmic divergence) reported rather than hidden.
5. Every per-load-path force is judged against 10–15 / 48 / 65 pN, and `A_eff = min(A_path/η_path)` is
   reported as the number `L_min` consumes.
6. `C-0014`'s `L_min` table is **recomputed**, the footprint consequence stated at both strokes, and the
   incompatibility finding either upheld or challenged in `gpd/challenges/` — not silently corrected.

### Units, locked

SI, scaled, per `P-2`: lengths nm, forces pN, energies pN·nm, stiffness pN/nm (`= 1 mN/m` exactly),
axial (stretch) modulus pN, flexural rigidity pN·nm², distributed shear coupling pN/nm².
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer with Mg²⁺**.
Transfer ratios and concentration factors are dimensionless and are reported **per pN of applied tether
force**, the study applying exactly 1 pN so that a force and a ratio are the same number.

### Geometry and sign conventions, fixed before deriving

Restated rather than inherited, per §5 of the problem definition.

- `x` runs **along** the DNA helices, `y` **across** them, `z` normal to the electrode. The origin of `(x, y)`
  is the centre of the footprint. `T-10`'s and `T-14`'s conventions, unchanged.
- The in-plane displacements are `u` along `x` and `v` along `y`, **both positive in the positive axis
  direction**. There is no `w` in this model at all: the out-of-plane problem is `C-0009`'s and the two do not
  couple at linear order for a flat sheet.
- A duplex axial force `N` is **positive in tension**. A crossover's in-plane force is reported as the vector
  `(F_s, F_n)` it transmits from the lower-`y` duplex to the upper one, `F_s` along `x` (shear) and `F_n`
  along `y` (normal), and the **magnitude** is what is judged against an allowable.
- The crossover connector attaches at the interface line, `d/2` from each duplex axis, so the sliding it
  resists is `(u_{b+1} + (d/2)θ_{b+1}) − (u_b − (d/2)θ_b)` with `θ = dv/dx` the in-plane rotation. That is the
  exact in-plane analogue of `C-0009`'s vertical link extension `w + (d/2)φ`, and it is what makes the two
  models the same sheet seen in two planes.
- **A tether attaches to one duplex**, at a base-pair station along it. The registration variable along the
  helices is therefore continuous over one crossover repeat `p`; across the helices it is the **duplex
  index**, not a continuous coordinate, because there is no material between two duplex axes to attach to.
- **Footprint** 40 × 40.35 nm, 15 duplexes — `T-10`'s and `T-14`'s, unchanged, so nothing here is a footprint
  effect.

### What is deliberately excluded

- **No out-of-plane coupling.** A flat sheet's membrane and bending problems decouple at linear order. The
  tether's *normal* component is `C-0014`'s cable term and is already priced there; what is missing and what
  this task supplies is the in-plane half.
- **No oxDNA.** The crossover's in-plane shear stiffness `k_s` is swept, not derived — the same posture
  `C-0009` and `C-0015` take toward `k_θ`. `T-9` is what could settle it, and this task adds it to `T-9`'s
  deliverables.
- **No electrostatics, no polymer layer.** The layer's lateral stiffness is **exactly zero** by symmetry
  (`C-0010`), so there is no in-plane foundation to model; that is a result being consumed, not an omission.
- **No new elasticity.** Every duplex constant, the interhelical distance, the crossover spacing, the column
  phase machinery and the per-path allowables are `C-0006`/`C-0009`/`C-0015`'s, so any difference reported
  here is the **plane the load is applied in** and nothing else.

---

## Plan

### The cheap bound, run first — and it decides the sign of the answer

Two things are available for the price of an equilibrium argument and a square root, before any matrix is
assembled, and they are asserted as tests rather than written in prose.

> ⚠️ **Post-hoc, and left standing rather than edited: bound (a) below is FALSIFIED, and that is the most
> informative outcome of this task.** It is right about the **cut total** and wrong about the **per-duplex
> peak**: an *oblique* tether applies a moment which the crossovers react as an axial couple, so `η` reaches
> **2.33**. Only the axis-aligned case obeys it, and there it is *saturated*. See Verify, falsifier 1, and
> [`C-0020`](../claims/C-0020-in-plane-shear-lag.md) §3.

**(a) The equilibrium bound: `η ≤ 1`, identically.**
The lateral load path has no distributed source. The layer contributes exactly zero lateral stiffness
(`C-0010`), so the only in-plane forces on the tile are the tether forces themselves. Cut any single load path
and the force it carries is a part of the applied force resolved onto it; with a single self-equilibrated
tether pair every internal path force is a fraction of `T`. **So the in-plane transfer ratio cannot exceed 1,
and `C-0009`'s 2.3–7.6 cannot be the in-plane number at all** — it is the ratio of a peak to an *equal share*
of a reaction the tile collected, and laterally the tile collects nothing.
This is the whole of the qualitative answer, and it costs one paragraph.

**(b) The shear-lag lengths, from the lattice's own constants.**
Bar `n` obeys `S u_n'' + (k_s/p)(u_{n+1} − 2u_n + u_{n−1}) = 0`, so a mode `u_n ∝ cos(q d n)` decays along the
helices as `exp(−x/Λ(q))` with

&nbsp;&nbsp;&nbsp;&nbsp;`Λ(q) = √( S / (2 (k_s/p)(1 − cos(q d))) )`, &nbsp;&nbsp;
`Λ_nn = √(S p/(2k_s))` (neighbour exchange), &nbsp;&nbsp; `Λ = √(S p/k_s)` (the composite convention),

and the longest non-uniform mode of a free strip of `N` duplexes, `q d = π/N`, gives the **load-sharing
length** `Λ₁`. At the nominal `k_s` this is ~65 nm against a 40 nm tile, which says in advance that the tile
is **too small to share an in-plane point load at all** — the duplex the tether lands on keeps nearly all of
it, and the crossovers carry the difference. Everything after this is quantifying "nearly".

### The expensive calculation, and why this method and not another

**Chosen: extend the existing `structure` package with an in-plane grillage, `OrigamiMembrane`, built on the
same `OrigamiSheet` and the same `CrossoverLayout` as `C-0009`'s out-of-plane `OrigamiGrillage`.**

The existing grillage is out-of-plane **only** — its three nodal degrees of freedom are `w`, `dw/dx` and the
roll `dw/dy`, and it has no membrane coordinate at all. Extending *it* with two more degrees of freedom per
node would double the matrix and couple two problems that are exactly decoupled for a flat sheet, so the
in-plane problem is built as a **sibling class with the same topology, the same node layout and the same
three-degree-of-freedom count** (`u`, `v`, `dv/dx`), sharing `OrigamiSheet`, `CrossoverLayout`, `Cholesky`,
`Gen1Tile`, `LoadPaths` and `ResultRounding` unchanged. That is the cost: one new element library, ~350 lines,
against a second lattice that would have duplicated the geometry, the phase machinery and the layout sweep.

Per `CLAUDE.md`, the stiffness matrix is **assembled straight into one array** and the individual
contributions are exposed as **energies**, never as retained matrices — five dense `n × n` matrices at 855
degrees of freedom is 110 MB and is what turned a comfortable lattice solve into an out-of-memory failure once
already.

| method | cost | why not |
|---|---|---|
| the equilibrium bound and the shear-lag lengths | milliseconds | **run first**; they fix the sign and the regime, but they cannot produce a peak force |
| **in-plane grillage + the orthotropic shear-lag membrane beside it** | ~1 minute for the whole study | **chosen** |
| re-using `OrigamiGrillage` with membrane degrees of freedom added | ~1 day | doubles a matrix to couple two problems that are exactly decoupled; the out-of-plane results would all have to be re-verified for a change that cannot move them |
| a full orthotropic plane-stress Ritz solve | hours | the *shear-lag* reduction is the physically right continuum here, and it is the one whose `n = 0` mode **is** the equal share, which is what makes the concentration factor readable off the series |
| oxDNA | days | `T-9`'s territory, and it is what would settle `k_s` — which is swept here instead |

Prefer published measurement on the actual material, per the research practice — and there is none: **no
in-plane force in a loaded origami sheet has ever been measured**, and the crossover's in-plane shear
stiffness is not in the literature in any form. The honest posture is a sweep with the answer's dependence on
it reported, which is what `C-0009` and `C-0015` do with `k_θ`.

### What would falsify this approach

Stated in advance, per §5. The outcome of each is in Verify.

1. **The lattice returning `η > 1` anywhere.** Then either the equilibrium bound is wrong or the assembly is,
   and every number in the study is an artefact. This is the primary falsifier and it is wired in as a runtime
   `check`, not only as a test.
2. **`η` depending strongly on `k_s`** over the four decades it is swept. Then the deliverable is a bracket,
   not a number, and `C-0014`'s tether lengths cannot be tightened until `T-9` measures it.
3. **The lattice and the continuum shear-lag membrane disagreeing away from the load point.** The two must
   agree where the discreteness has nothing to say; if they do not, the lattice is not a discretisation of the
   membrane it claims to be, exactly as `C-0009` had to show for the plate.
4. **Registration or column phase moving `η` by more than the direction does.** Then the answer is a layout
   result and not a direction result, and the design rule this task produces would be the wrong one.
5. **The regularisation carrying a non-negligible part of the load.** The chord case is self-equilibrated and
   is solved with a vanishing in-plane spring bed to remove the three rigid-body modes; if the bed takes more
   than a part in `10⁶` the answer is a property of the regulariser.
6. **The mesh not converging at the attachment node**, where the axial force is discontinuous by construction.
   Nested refinements only (1 ⊂ 2 ⊂ 4), per the monotonicity caveat `C-0009` recorded.
7. **The in-plane and out-of-plane models disagreeing on a quantity they share.** Both must reproduce the same
   crossover count, the same column phases and the same footprint from the same layout; if they do not, the
   two are not the same sheet and no comparison between them means anything.

---

## Execute

Code: `src/main/kotlin/structure/` — `OrigamiMembrane.kt` (the in-plane lattice and its member forces),
`ShearLag.kt` (the transfer lengths and the continuum orthotropic membrane, closed form),
`InPlaneLoadPathStudy.kt` (the study). `OrigamiGrillage.kt`, `CrossoverLayout.kt`, `OrigamiSheet.kt`,
`Gen1Tile.kt`, `LoadPaths.kt`, `Cholesky.kt` and `ResultRounding.kt` are **unchanged** — nothing already
published moves.
Tests, written first: `src/test/kotlin/structure/ShearLagTest.kt` and
`src/test/kotlin/structure/OrigamiMembraneTest.kt`, each test named for the gate it discharges.

```shell
./gradlew test -PbuildDirectory=build-t15
tools/study.sh structure.InPlaneLoadPathStudyKt
```

Result: [`../results/T-15-in-plane-shear-lag.json`](../results/T-15-in-plane-shear-lag.json), deterministic in
filename **and** content — re-run and confirmed by `tools/study.sh` reporting *"no result file changed"*, with
`ResultRounding.kt` applied at the serialisation boundary and every extremum selected on the **rounded** value
with the index as tie-break, per the argmin trap `CLAUDE.md` records.

---

## Verify

Full gate table, falsifier outcomes and per-item predicate discharge in
[`C-0020`](../claims/C-0020-in-plane-shear-lag.md). In brief:

- **39 gate-named tests** green (`OrigamiMembraneTest` 21, `ShearLagTest` 18); full suite green under
  `tools/verify.sh` on the working tree with no packages dropped.
- **Falsifier 1 fired.** `η > 1` does occur — 2.3290 on the duplex-axial path and 2.4475 on the crossover path,
  at oblique placements. The equilibrium argument bounds the **sum** of the duplex axial forces on a cut, which
  is asserted to `1e−4`, and not the per-duplex peak. The runtime `check` was replaced by that correct
  invariant plus a saturation ceiling, not deleted, and the mechanism — a moment reacted by the crossovers as
  an axial couple, levered by the free overhang at the tile edge — is reported as a result.
- **Falsifiers 2–7 did not fire.** The aligned answer is **exactly 1.0000** over four decades of `k_s`; lattice
  and continuum agree to 1.4 % of the applied force where the continuum's premise holds; layout moves the
  binding path by ×1.0000 against direction's ×11.75; the bed carries `<1e−9`; the mesh converges to 0.1 % over
  nested 1 ⊂ 2 ⊂ 4 ⊂ 8; the in-plane and out-of-plane lattices are the same sheet, crossover for crossover.
- **Determinism**: `tools/study.sh structure.InPlaneLoadPathStudyKt` reports *"no result file changed"* on a
  re-run, and two independent runs on an isolated copy are byte-identical.

### The predicate, item by item

1. `η` and `C` produced for both principal directions, per path class, definitions kept apart with the
   equal-share denominator stated — **yes**;
2. the worst case over anchor placement is a **complete** sweep, all 32 base-pair column phases × all 15 × 15
   edge-to-edge duplex pairs = **7200 designs**, plus 1024 across-helix stations — **yes**, and it replaced the
   continuous angle scan, which samples the lattice unevenly;
3. `Λ`, `Λ_nn` and `Λ₁` derived for this lattice and **asserted against the lattice's own measured decay**
   (78.7 nm against 77.4 nm predicted), with the whole answer reported over four decades of `k_s` including the
   rigid limit — **yes**;
4. the continuum shear-lag membrane solved on the same footprint, the excess quoted at stated stations, and its
   two failures (no convergence at the load point, no frame indifference) reported rather than hidden —
   **yes**;
5. every force judged against 10–15 / 48 / 65 pN and never against §4(f)'s band, with
   `A_eff = min(A_path/η_path)` reported as the number `L_min` consumes — **yes**;
6. `C-0014`'s `L_min` table recomputed at both strokes, the footprint consequence stated, and the
   incompatibility finding **challenged** rather than silently corrected — **yes**,
   [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md).

## Result

Filed as [`C-0020`](../claims/C-0020-in-plane-shear-lag.md), which raises
[`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md) against `C-0014`.

## Feedback into Formulate

- **`T-9` gains a fourth deliverable**: the crossover's **in-plane shear** stiffness `k_s`, which is the one
  undetermined input here and which no accessible source gives in any form.
- **`T-12`'s footprint constraint is a different constraint now**, and `T-13` inherits the new one: the
  minimum-length tether's *normal* preload is `n A √(2A/S)` — **independent of the stroke** — and that is the
  currency the tether-length reduction is paid in.
- **`T-17` and `T-16` should be told that the in-plane and out-of-plane concentration factors are not the same
  quantity**, and that an out-of-plane number applied to an in-plane load is conservative by a factor that is
  itself worth reporting.
