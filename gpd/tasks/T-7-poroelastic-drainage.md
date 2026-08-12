# T-7 — Poroelastic drainage time of the grafted polymer layer

| | |
|---|---|
| **Leaf** | none — `T-7` has no leaf in `../simulation-task-map/knowledge/program_tasks_feynman_path.csv`; it is a "new" row of §6 |
| **Problem definition** | §6 task 7; the question is §4(d); parameters §3; bandwidth requirement §3 ("≥ 1 kHz") |
| **Verification type** | in-silico (analytic poroelastic / Brinkman model, evaluated numerically) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0004`](../claims/C-0004-poroelastic-drainage.md); raises [`CH-0003`](../challenges/CH-0003-blob-stack-height.md) |

---

## Formulate

### The question, as a numeric target

Water has to leave the layer as the tile descends and re-enter as it rises.
Produce the resulting mechanical relaxation time `τ` in seconds, and the corner frequency `f_c = 1/(2πτ)` in Hz,
as a function of layer thickness, polymer volume fraction and tile footprint,
with the drainage path identified rather than assumed,
and with the competing dissipation channels bounded so that the *slowest* one is the one quoted.

§4(d) is explicit that "not binding" is only half an answer:

> Our own back-of-envelope does not suggest this is the binding constraint at these dimensions,
> but we want it done properly rather than waved away, **and we want to know what would make it binding**.

So the deliverable is a bound **and** a boundary.

### Acceptance predicate

> Bounded, with the conditions under which it would constrain ≥ 1 kHz operation stated.

Discharged when all five hold:

1. `τ(h, φ, footprint)` is emitted across the §3 thicknesses (5 / 7 / 10 nm) and both §3 footprints
   (40 × 40 nm, and the 70 × 100 nm test tile), swept in thickness and volume fraction beyond that range;
2. the **drainage length** is derived, not assumed — the lateral and vertical paths are computed on
   a common definition and compared;
3. **all three dissipation channels** — poroelastic drainage, tile Stokes drag, polymer Zimm relaxation —
   are bounded, and the one that sets the bandwidth is named;
4. the result is **parameterised by the layer stiffness** rather than hard-coding `C-0001`'s number,
   and the sensitivity to a factor of two either way is reported (`T-1c` is re-deriving it concurrently);
5. the 1 kHz boundary is stated **quantitatively in every variable**: thickness, volume fraction,
   tile size, stiffness, permeability and frequency, each with a reachability verdict.

### Units, locked

SI, scaled, per `P-2`. Lengths nm, forces pN, pressures pN/nm² (`= 1 MPa` exactly), stiffness pN/nm.
New units this task needs, and their conversions, enforced in code (`poroelastic/Water.kt`):

| quantity | locked unit | conversion |
|---|---|---|
| permeability `k` | nm² | `1 nm² = 1e-18 m²` |
| transmissivity `T` | nm³ | depth-integrated permeability |
| viscosity `η` | pN·s/nm² | `1 Pa·s = 1e-6 pN·s/nm²` |
| drag `γ` | pN·s/nm | |
| diffusivity `D_p` | nm²/s | `1 nm²/s = 1e-14 cm²/s` |
| mass `m` | pN·s²/nm | `1 pN·s²/nm = 1e-3 kg` |
| mass density | g/cm³ | `1 g/cm³ = 1e-21 pN·s²/nm⁴` |

`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer, 2–10 mM MgCl₂**.
Water viscosity **`η = 8.541e-4 Pa·s` at 300 K**, evaluated in code from the Vogel correlation
`η = 2.414e-5 · 10^(247.8/(T−140))` Pa·s and cross-checked against the IAPWS 20 °C reference value.
The 20 °C handbook value would have been 17 % wrong at 300 K, and `τ ∝ η` exactly, so this is not pedantry.

### Geometry and sign conventions, fixed before deriving

- `z` normal to the electrode, positive away from it, origin at the electrode surface.
- The layer occupies `0 < z < h`; the tile is a **rigid, impermeable, non-adsorbing plate** at `z = h`,
  and the electrode below it is impermeable too.
- Descent is `−ż > 0`. The displaced water therefore has **nowhere to go but sideways**, out through the
  perimeter of the footprint, where the pore pressure is taken as zero — beyond the tile edge the layer's
  upper surface is open to bulk buffer within about one layer thickness.
- Both drainage lengths are defined so that `τ = ℓ²/D_p` with the **same** `D_p`. That is the only
  definition under which "which path dominates" is a meaningful question:
  lateral `ℓ = √G`, vertical `ℓ = 2h/π` (first consolidation mode, one open face).
- The layer stiffness `k` is an **input**, per `CH-0001`; every time reported is exactly `∝ 1/k`.

### What is deliberately excluded

No electrostatics; no ion partitioning; no electro-osmotic coupling (the buffer is ionic and the electrode
is biased, so a streaming potential opposes the squeeze flow — that needs `T-6` and is named as open, not modelled);
no tile compliance (`T-5b`); no non-linear poroelasticity (`k` and `M` are held at the stated `φ`).

---

## Plan

### The cheap bound first

The problem definition asks for the cheap bound before the expensive calculation, and here the cheap bound
*is* the answer, for a reason worth stating: the whole question is one dimensionless group.

&nbsp;&nbsp;&nbsp;&nbsp;`τ = γ / k_layer`, &nbsp; `γ = η G A / T`, &nbsp; `f_c = 1/(2πτ)`

with `G` the footprint mean of the solution of `∇²u = −1` (the Saint-Venant torsion function),
and `T` the depth-integrated transmissivity. Everything else is what goes into `T`.

| Method | Cost | Why not |
|---|---|---|
| **Analytic Brinkman squeeze-out + Poisson footprint factor** | seconds | **chosen** |
| Finite-element Biot poroelasticity of the tile-on-layer geometry | hours–days | Buys a transient pressure field. The answer is already three orders of magnitude clear of the requirement, and the FE model's permeability input is the same forty-fold-uncertain number. It would compute a precise consequence of an imprecise premise. |
| Coarse-grained MD with explicit solvent | weeks | The only route that could *measure* the screening length of this layer, which is the actual open question — but it cannot be interpreted before the analytic bound says how much precision is worth buying, and it is not worth buying for a 100× margin. |

The expensive routes are declined on the same grounds `P-3` declined simulating PEG's excluded volume:
they would be *less* trustworthy than what already exists, not merely more expensive.

### The three physical inputs, and how each was obtained

**1. Permeability.** Sourced rather than guessed, and sourcing it produced a *disagreement* rather than
a number. Three constructions of the same layer:

| model | provenance | `√k` at `φ = 0.0289` |
|---|---|---|
| free-draining Kuhn rods, slender-body friction | **DERIVED** here | 0.99 nm |
| Jackson–James fibrous-media correlation | **CITED, primary source NOT obtained** | 0.86 nm |
| `k = ξ²`, `ξ = v₀^(1/3) φ^(−3/4)` | **CITED and measured on PEG** (Offeddu et al. 2018) | 5.60 nm |

They differ by a factor of 40 in `k` and 6.4 in length. That is not a defect of the sources: it is what
`C-0002` and `CH-0001` predict should happen at `φ/φ# ≈ 1.1`, where the correlation blob is two thirds of
the whole coil, so "monomer scale" and "blob scale" are not separated. **The bound is therefore quoted from
the slowest model**, so the verdict does not depend on which is right — only the size of the margin does.

The Jackson–James constant `0.931` is used **only as a cross-check** and is flagged in code as unverified:
the primary source (*Can. J. Chem. Eng.* **64**:364) is paywalled and was not obtained this iteration, and
`CLAUDE.md`'s research-practice rule forbids acting on a secondary quotation. It agrees with the derived
free-draining model to a factor of 1.3, which is all it is asked to do.

**2. Longitudinal modulus.** `M = φ dΠ/dφ` from the **measured** equation of state of `C-0002`, not from
a textbook exponent. For a *grafted* layer the van't Hoff limb is dropped — `C-0002` is explicit that a
grafted layer has no chain translational entropy — leaving `M = (9/4) α (k_BT/v₀) φ^(9/4)`, which is
0.026–0.195 pN/nm² across the design points. That is the *smallest* of the three available moduli
(`M_grafted < M_bulk_EOS < M_from_C-0001_stiffness`), again the conservative direction.

**3. Drainage length.** Not assumed. Computed both ways on a common definition and compared — see Verify.

### Why Brinkman rather than Darcy

Because Darcy is not valid for one of the three permeability models, and the task said to say so plainly
rather than quote a number. The depth-integrated transmissivity of a layer between two no-slip walls,

&nbsp;&nbsp;&nbsp;&nbsp;`T = k h [ 1 − (2√k/h) tanh(h / 2√k) ]`

reduces to `k h` when `√k ≪ h` (Darcy) and to `h³/12` when `√k ≫ h` (a free water film, i.e. the Reynolds
lubrication squeeze film). The Gen-1 layer has `√k/h = 0.09–0.13` on the segment-scale models and
**`0.56–0.58` on the blob-scale one**. So the answer must degrade gracefully across that range, and the
Brinkman form is what does it. Using plain Darcy with the blob-scale permeability would overstate the
drainage rate by 5×.

### What would falsify this approach — stated in advance

1. **The screening length coming out comparable to or larger than the layer thickness.**
   Then the Darcy/Brinkman continuum is outside its own domain and no number may be quoted from it.
2. **A competing dissipation channel coming out slower than drainage.** Then `T-7` is answering the wrong
   question and the bandwidth belongs to that channel.
3. **The tile turning out to be under-damped** (`Q > ½`). Then the first-order `τ = γ/k` picture is wrong
   and the response rings rather than relaxes.
4. **The drainage time landing within an order of magnitude of 1/(2π·1 kHz) = 159 µs.** Then the analytic
   bound is not decisive and the finite-element route has to be bought after all.

**Outcome.** (1) **fired, on the blob-scale model** — `√k = 0.56 h`, and it is reported as a failure of the
premise rather than papered over; the bound is quoted from the models where it did not fire, and the
Brinkman form covers the case where it did.
(2) did not fire — drainage is 11–30× slower than the polymer's own Zimm relaxation
and 32× the tile's Stokes drag.
(3) did not fire — `Q ≈ 7e-4`. (4) did not fire by the widest margin of the four: the slowest
time anywhere in the §3 parameter table is **7.1 µs** at `C-0001`'s stiffness (22.6× clear of 1 kHz), and
**28 µs** even at a quarter of it (5.6× clear).

---

## Execute

Code: `src/main/kotlin/poroelastic/` — `Water.kt`, `LayerPermeability.kt`, `DrainageGeometry.kt`,
`DrainageResponse.kt`, `PoroelasticDrainageStudy.kt`.
Tests, written first: `src/test/kotlin/poroelastic/` — 49 tests, all green.

```shell
./gradlew test
./gradlew study -Pstudy=poroelastic.PoroelasticDrainageStudyKt
```

Result: [`../results/T-7-poroelastic-drainage.json`](../results/T-7-poroelastic-drainage.json) —
96 design points (4 layer states × 2 footprints × 3 permeability models × 4 stiffness multipliers),
364 sweep points (2 footprints × 2 models × 7 thicknesses × 13 volume fractions),
14 points of the 1 kHz contour, and 6 binding conditions.
Every run parameter is in the file. Deterministic: no timestamp.

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- `η [pN·s/nm²] · G [nm²] · A [nm²] / T [nm³]` reduces to `pN·s/nm`, and that over a stiffness in `pN/nm`
  reduces to seconds. Asserted against an independently written expression, not against the implementation.
- `√k` is asserted to be a length for all three permeability models.
- The mass unit is derived from `F = m a` and checked both ways: a 40 × 40 × 10 nm tile at 1.7 g/cm³ is
  `2.72e-17 pN·s²/nm`, which is `2.72e-20 kg` computed independently in SI.

### Gate 2 — limiting cases

- **Darcy limit.** `√k ≪ h` recovers `T = k h` to 0.2 %.
- **Free-film limit.** `√k ≫ h` recovers `T = h³/12` exactly, and the resulting drag reproduces the
  classical Reynolds squeeze-film coefficient for a square plate, `γ = 0.42174 η L⁴/h³`, to 1e-6.
  This is the check that matters most: it means the same expression covers "poroelastic drainage" and
  "lubrication squeeze film", which are usually presented as separate channels and are **not additive**.
- **Infinite strip.** The footprint factor tends to `W²/12` to 0.07 % at aspect ratio 1000.
- **Over- versus under-damped**, which §5 names explicitly: `Q = √(mk)/γ ≈ 7e-4` and the inertial time is
  `2.7 ps` against a drainage time of `1.4 µs` — overdamped by six orders, so the first-order picture holds.
- Every permeability model diverges in the dilute limit and falls monotonically with `φ`.

### Gate 3 — symmetry and conservation

- **The two routes to the same time are proved identical, not observed to agree.** With the stiffness the
  layer's own modulus implies, `k_layer = M A / h`, the drag route `τ = γ/k_layer` and the diffusion route
  `τ = G/D_p` with `D_p = kM/η` are the *same number exactly* in the Darcy limit (to 1e-12). That identity
  is what licenses quoting either, and it is the hand-off between `T-7` and `T-1c`.
- The footprint factor is homogeneous of degree two in length (doubling both edges quadruples it, to 1e-9)
  and symmetric under exchanging the edges (to 1e-12).
- The squeeze-drag relaxation time scales as **exactly** `L²`, not `L⁴`, because the stiffness of the layer
  under the tile carries the other `L²` — which is what makes the 1 kHz boundary a *tile size* statement.
- The correlation-length model's exponent `d ln k/d ln φ = −3/2` and the free-draining model's `−1` are
  exact for every `φ` and every prefactor.

### Gate 4 — numerical convergence

- The double Fourier series for the footprint factor converges: 201 harmonics are within `9e-8` relative of
  801, and the error falls by more than 100× from 25 to 201 harmonics.
- The log-derivative of each permeability model converges quadratically to its closed form under step
  refinement.
- Both bisections (tile edge at 1 kHz, volume fraction at 1 kHz) are on strictly monotone functions —
  `f_c` is strictly decreasing in the edge, and `k M ∝ φ^(5/4)` is strictly increasing — so the root is
  unique and 200 halvings drive the bracket to machine precision. The tile-edge solver is checked by
  round-trip: evaluating the response at the returned edge gives 1000.000 Hz to 1e-6.
- `1 − tanh(x)/x` is evaluated by series below `x = 1e-2`, because the closed form loses every significant
  digit to cancellation there — which is exactly the free-film limit the model has to reach.

### Gate 5 — literature cross-check, with the premises checked against the material

- **Water viscosity.** The Vogel correlation reproduces the IAPWS reference value at 20 °C
  (1.0016 mPa·s) to 0.02 %, without being fitted to it here.
- **Footprint factor.** The unit square gives 0.0351443, the classical mean of the Saint-Venant torsion
  function.
- **Permeability, against measurement rather than theory.** Gao & Cho (arXiv:2209.14382, Table 1) measure
  21 hydrogels across four polymer families: `k = 2.8–23.1 nm²` at moduli `K = 7.0–85.7 kPa`. Our blob-scale
  model overlaps that band at the working volume fractions (8.2–31.3 nm²); both segment-scale models
  land an order of magnitude *below* it. The measurement therefore sits between our two brackets, and the
  bound we quote is the conservative one.
- **Poroelastic diffusivity, likewise.** The measured `kK` product is 68–266 nm²·kPa, i.e.
  `D_p = 0.80–3.11e8 nm²/s`. Ours, with the segment-scale permeability and the grafted des Cloizeaux
  modulus, is `2.96e7 nm²/s` — a factor of 2.7 to 10.5 **slower** than anything measured.
- **The `k = ξ²` prefactor, on this material.** Offeddu, Axpe, Harley & Oyen (*AIP Adv.* **8**:105006, 2018)
  measure PEG-hydrogel permeability by poroelastic indentation and state that "the square root of the
  intrinsic permeability approximates the size of the fluid path, in this case corresponding with ξ",
  reporting `k ∝ ξ²` at `r² = 0.92` and `k ∝ φ^(−3/2)` at `r² = 0.96`. Our model reproduces that exponent
  exactly rather than fitting it. *(Read from the article full text, per `CLAUDE.md`'s research practice —
  not from a search summary.)*
- **The prefactor is self-consistent at coil overlap.** At `φ* = N^(−4/5)` the correlation length must *be*
  the coil size; ours differs from the Flory radius by exactly `v₀^(1/3)/a = 1.12`.
- **And it agrees with the brush's own blob.** In the Alexander picture the blob is the grafting spacing;
  at the design point `s = 6.455 nm` against `ξ = 5.598 nm` — 13 % apart, from unrelated routes.
- **The premise checked against the actual layer, which is what §7 asks.** `√k/h = 0.09–0.13` on the
  segment-scale models and **0.56–0.58** on the blob-scale one. Falsifier (1) fired. Reported, not hidden;
  see `C-0004`'s validity range and `CH-0003`.

### Not verified, and stated as such

- **Nothing here is measured.** `PASS` means model-consistent and traceable.
- **The permeability of *this* layer is not known to better than a factor of 40**, and no source was found
  that measures the hydrodynamic screening length of a grafted PEG layer at `σ ≈ 0.024 nm⁻²`.
- **Jackson & James (1986) is quoted from secondary literature.** The primary source was not obtained.
- **Electro-osmotic coupling is absent.** The layer is porous, the buffer is ionic and the electrode is
  biased, so a streaming potential opposes the squeeze flow. Bounding it needs `T-6`. Queued as `T-7b`.
- **Tile permeability is not established.** If the origami is hydraulically open, the vertical path opens —
  worth at most 1.4× at 40 × 40 nm and 5.7× at 70 × 100 nm, so it is not a design lever either way.
- **Linear poroelasticity.** `k` and `M` are held at the stated `φ`; under a 3 nm stroke on a 10 nm layer
  `φ` rises 40 % and both move. That makes the layer *faster* under compression, so the reported time is
  still an upper bound, but the transient is not a single exponential.

---

## Result

Filed as [`C-0004`](../claims/C-0004-poroelastic-drainage.md).
Raises [`CH-0003`](../challenges/CH-0003-blob-stack-height.md) against `C-0001`.

## Feedback into Formulate

- **`T-2` gains a constraint that does not bind, and can say so with a number.** §4(d) is discharged as
  non-binding by 22–1800× across the entire §3 parameter table. It cannot shrink the design window.
- **`T-2` also gains a new *upper* bound on tile size**, which nothing before it had: 437 nm on a side.
  It is 11× the Gen-1 tile, so it is slack, but it is a real ceiling and it scales as `L²`.
- **`T-1c` inherits `CH-0003`**: the Alexander-de Gennes layer is `(Σ/π)^(5/6)` blobs tall — 1.47 at the
  `Σ = 5` convention — so the blob-stack premise fails *geometrically* as well as thermodynamically.
- **`T-7b` is raised**: electro-osmotic drag on the squeeze flow, which this task could not bound.
- **`T-8` gains a bandwidth for its noise integral.** Positional variance is set by equipartition and is
  bandwidth-independent, but the *noise spectrum* the sensor sees rolls off at the corner frequency
  computed here, and `T-8` should use it rather than assume one.
