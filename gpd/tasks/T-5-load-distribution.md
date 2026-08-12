# T-5 — Load distribution across the DNA-origami tile

| | |
|---|---|
| **Leaf** | `A1.2` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 5; question §4(f); parameters §3; geometry §1 |
| **Verification type** | in-silico (analytic plate mechanics + a Rayleigh-Ritz plate solve written for this task) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) |
| **Paired with** | [`T-5b`](T-5b-tile-flatness.md) — same structural model, one iteration, shared Plan |

---

## Formulate

### The question, as a numeric target

Given the §3 parameters — 100 pN over a 40 × 40 nm tile (test tiles to ~70 × 100 nm) —
produce the force in pN carried by **one load path** of the origami,
for **distributed** attachment and for **concentrated** attachment separately,
and report it against the §4(f) bands: reversible isomerisation 10–35 pN,
irreversible disassembly 35–60 pN.

### Acceptance predicate

Leaf `A1.2`, as specialised by §6 task 5:

> Peak per-load-path force reported against the 35–60 pN disassembly band,
> distributed and concentrated attachment treated separately.

Discharged when all five hold:

1. the load paths of the tile are **enumerated** — what a "load path" is for this structure
   is stated, not assumed, and each class is counted from the tile geometry;
2. the peak per-path force is emitted for the distributed case and for the concentrated case,
   with the mechanism that sets each;
3. every reported force carries its `StructuralBand` against the cited 10–35 / 35–60 pN bands;
4. the **minimum number of parallel load paths** needed to stay strictly below 35 pN, and
   strictly below 10 pN, is reported;
5. the answer is stated as a function of the foundation stiffness `k_f`, which is in flux
   under `T-1c`, rather than at a single value of it.

### Units, locked

SI, scaled, per `P-2`: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly), **flexural rigidity pN·nm** (a plate rigidity is an energy),
**foundation stiffness per unit area pN/nm³**, hinge stiffness pN·nm/rad.
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer with Mg²⁺**.

### Geometry and sign conventions, fixed before deriving

Restated here rather than inherited, per §5 of the problem definition.

- `z` is normal to the electrode, positive **away** from it, origin at the electrode surface —
  the `T-1` convention, unchanged.
- The tile mid-surface sits at height `h`; the polymer layer occupies `0 < z < h`.
- Within the tile, `x` runs **along** the DNA helices, `y` **across** them, and the origin is
  at the centre of the footprint, so `x ∈ [−L_x/2, L_x/2]`, `y ∈ [−L_y/2, L_y/2]`.
- The plate deflection `w(x, y)` is measured **downward**, positive when the layer is
  compressed. This is the actuation direction, and it keeps the applied electrostatic
  pressure positive. It is the opposite sense to `z`, deliberately: mixing them is how sign
  errors enter, so the two are given different letters and never added.
- The applied pressure `q(x, y) > 0` acts downward. The foundation reaction is `k_f w` upward.
- A load path carries **tension** as positive.

### What a load path is, for this structure

Named in advance, because the acceptance predicate is about "one load path" and the phrase
has no meaning until the structure is decomposed:

| class | what it is | how it is counted |
|---|---|---|
| `LP-brush` | one grafted PEG chain under the tile | `σ · A` |
| `LP-crossover` | one antiparallel crossover on a cut **parallel** to the helices | `L_x / p` per layer, `p` the crossover spacing |
| `LP-duplex` | one duplex crossing a cut **perpendicular** to the helices | `L_y / d`, `d` the interhelical distance |
| `LP-anchor` | one discrete tether tying the tile to the substrate | a design variable, swept |
| `LP-lever` | one attachment delivering force out of the tile to the lever | a design variable, swept |

`LP-brush` cannot disassemble the origami and is reported only to close the force balance.
`LP-crossover` is the one the §4(f) bands were measured on and is the binding internal path.

### What is deliberately excluded

No electrostatics is *solved* here — the electrostatic load enters only as (i) a total of
100 pN and (ii) a bounded spatial non-uniformity. That is `T-3`'s work and this task must not
pre-empt it. No poroelasticity, no ion partitioning. The polymer layer enters only through its
stiffness per unit area, taken from `C-0001` **and swept**, because `CH-0001` reclassified
`C-0001`'s numbers as lower bounds and `T-1c` is re-deriving them concurrently.

---

## Plan

**Shared with [`T-5b`](T-5b-tile-flatness.md).** The two tasks are one structural model
evaluated for two outputs — internal forces and deflected shape — so the method, its cost
justification and its falsifiers are stated once, here, and `T-5b` refers to them.

### The cheap bound, run first

Three closed forms, no discretisation, seconds to evaluate:

1. **A uniform load on a uniform Winkler foundation produces no internal force and no dishing
   at all.** `w = q/k_f` is constant, its fourth derivative vanishes, and the free-edge
   conditions `M_n = 0`, `V_n = 0` are satisfied identically. A free plate on a uniform
   foundation under a uniform load *translates*, exactly, whatever its rigidity.
   This is not a limiting case — it is the leading-order answer, and it means the whole task
   reduces to **naming and bounding the departures from uniformity**.
2. **The Winkler length** `ℓ = (D/k_f)^(1/4)`, compared against the tile half-width `L/2` and
   against the anchor spacing. This is the only dimensionless group in the problem and framing
   the answer in `ℓ/L` is what makes it survive `T-1c` changing `k_f`.
3. **Hertz–Westergaard**, `w(0) = P/(8√(D k_f))` for a point load on an infinite plate on a
   Winkler foundation — the concentrated-attachment case. If this already exceeds the stroke,
   concentrated attachment is dead and no numerics are needed to kill it.

### The expensive calculation, and why it is this one and not a bigger one

**Chosen: a Rayleigh-Ritz solve of the orthotropic Kirchhoff plate on a Winkler foundation,
with free edges, discrete point supports and point loads, written for this task. Cost: seconds.**

| method | cost | why not |
|---|---|---|
| closed forms alone | seconds | run first, and they carry most of the answer — but they cannot do *discrete anchors on a finite plate*, which §4(g) names as the actual geometry |
| **Ritz plate solve** | seconds | **chosen** |
| finite-element plate (FEniCS) | hours incl. setup | buys nothing over Ritz for a rectangle: free edges are *natural* in an energy method and awkward in a differenced biharmonic, and the answer is smooth |
| CanDo-style beam FEM of the actual staple layout | days | needs a real design file we do not have; and its crossover model is *rigid*, which is precisely the assumption this task has to test rather than inherit |
| oxDNA coarse-grained MD of a 40 × 40 nm tile | ~10⁵ nucleotides × µs — weeks on 8 cores | the only route to a crossover hinge constant from first principles, and a **queue item with a cost estimate**, not something to start inside this iteration |

The Ritz choice buys three things the alternatives do not:
the rigid-body modes are *exactly* the first three basis functions, so "dishing" needs no
plane-fitting; the basis is orthogonal in the area inner product, so the mean deflection is one
coefficient; and the coefficient covariance under equipartition is `k_BT K⁻¹`, so the **thermal**
dishing falls out of the same matrix as the loaded one, which is what `T-5b` and `T-8` need.

### The plate rigidity is derived, not asserted

A single-layer origami sheet is a *grillage*, not a homogeneous plate, and its two principal
rigidities have different physical origins:

&nbsp;&nbsp;&nbsp;&nbsp;`D_∥ = EI / d` &nbsp;(along the helices — parallel duplex beams, `EI = L_p k_BT`)

&nbsp;&nbsp;&nbsp;&nbsp;`D_⊥ = k_θ d / p` &nbsp;(across the helices — **crossover hinges only**)

with `d` the interhelical distance and `p` the crossover spacing along one adjacent pair.
The second is derived rather than quoted: a moment `M` per unit width is carried by `1/p`
crossovers per unit length, each turning through `M p / k_θ`, and that rotation is spread over
one interhelical distance. **No duplex elasticity appears in `D_⊥` at all** — which is leaf
`A8.2`'s "identify the dominant compliance term (joint compliance, not lever length)", answered
structurally rather than by assertion.

`k_θ`, the crossover hinge constant, is the one number the accessible literature does not supply
directly. It is therefore **swept over an order of magnitude**, bracketed from the angular
fluctuation of a stacked-X junction (`k_θ = k_BT/Δθ²`), and every conclusion is stated as a
function of it.

### What would falsify this approach

Stated in advance, per §5.

1. **`ℓ/L ≫ 1` across the whole sweep.** Then the tile is a rigid plate for trivial reasons,
   the plate solve is an expensive way to write `w = F/(k_f A)`, and the interesting content
   moves entirely to `T-3`'s load model. (Would make the task cheap, not wrong.)
2. **`ℓ/L ≪ 1` *and* the load genuinely uniform.** Then the tile is a membrane that follows the
   foundation pointwise, Kirchhoff plate theory is the wrong reduction, and the right model is
   a locally-compliant sheet with no bending at all.
3. **The dishing depending on the tile's own rigidity at leading order.** The cheap bound says
   it must not, for a uniform load; if the numerics say otherwise, the numerics are wrong and
   the assembled stiffness matrix is not what it is claimed to be. This is the strongest
   internal falsifier available and it is wired in as a test.
4. **The transverse shear on a cut not vanishing at the free edges and at the symmetry plane.**
   Same character: an exact statement the numerics must reproduce or be discarded.
5. **Kirchhoff being inapplicable.** A single-layer sheet is 2 nm thick and 40 nm wide, so
   transverse shear deformation is negligible and Kirchhoff is safe. For a 10 nm-thick
   multi-layer tile the thickness-to-span ratio reaches 1/4 and it is **not** safe; that case
   is reported as a bound with the limitation named, not as an answer.

Outcome of each is recorded in Verify.

---

## Execute

Code: `src/main/kotlin/structure/` — `Legendre.kt`, `Cholesky.kt`, `OrigamiSheet.kt`,
`PlateOnFoundation.kt`, `LoadPaths.kt`, `TileLoadDistributionStudy.kt`, `TileFlatnessStudy.kt`.
Tests, written first: `src/test/kotlin/structure/`.

```shell
./gradlew test
./gradlew study -Pstudy=structure.TileLoadDistributionStudyKt
```

Result: [`../results/T-5-load-distribution.json`](../results/T-5-load-distribution.json),
deterministic, every run parameter in the file.
6 sheet variants × 5 foundation stiffnesses × (1 distributed + 15 anchored + 6 concentrated) load cases.

## Verify

All five gates are carried in
[`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md#the-five-verification-gates),
executed as tests in `src/test/kotlin/structure/`, named for the gate they discharge.
The outcome of the falsifiers stated in the Plan:

| falsifier | fired? | outcome |
|---|---|---|
| 1. `ℓ/L ≫ 1` everywhere | no | the opposite: `ℓ/L = 0.14–0.64` across the sweep |
| 2. `ℓ/L ≪ 1` and the load uniform | **partially** | `ℓ_⊥/p = 0.26–0.52 < 1`, so the continuum plate reduction across the helices **is** marginal; recorded as a validity limitation, and it makes every flatness conclusion conservative rather than optimistic |
| 3. dishing depending on the tile's rigidity under a uniform load | no | reproduced as zero at rigidities spanning 10⁹, to `< 1e−9 nm` |
| 4. shear not vanishing at the free edges and the symmetry plane | no | vanishes to `< 1e−9 pN`, wired in as a test |
| 5. Kirchhoff inapplicable | **for the 10 nm four-layer variant only** | reported as a bound with the limitation named, not as an answer |

### The one place the cheap bound was wrong

The analytic ripple transfer function predicted an edge perturbation would be attenuated by 50×.
The finite-plate solve gives an effective transmission of 0.53. A **free edge** has no material
beyond it to bend against, so an edge perturbation costs far less curvature than an interior
ripple of the same wavelength. The interior transfer function stands and was verified; it simply
does not apply at a boundary. This is the only result in the iteration that the closed forms
could not have produced, and it is the cost justification for the solve.

## Result

Filed as [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md),
which also raises [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) against `C-0001`.

## Feedback into Formulate

- **`T-3` must not treat "the tile displacement" as a single number** unless the electrostatic
  force is delivered uniformly. If it is delivered anywhere concentrated, `C-0006` applies and
  `C-0001` does not.
- **`T-3` can hand its load non-uniformity straight in**: the dishing is exactly linear in the
  edge-taper depth, demonstrated rather than asserted, so no re-run is needed.
- **The §4(f) bands were being used as a per-load-path allowable and are not one.** They are a
  whole-cross-section number at 5.5 pN/s. The per-path allowables are the single-duplex shear
  and unzip figures, and the unzip one is 4–6× weaker — the largest free design lever found.
- **A new queue item:** a crossover hinge constant from oxDNA. `k_θ` is the single largest open
  premise under `C-0006` and no accessible measurement exists.
