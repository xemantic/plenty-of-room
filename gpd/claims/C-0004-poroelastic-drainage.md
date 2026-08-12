# C-0004 — Poroelastic drainage does not limit the Gen-1 actuator, and what would make it

| | |
|---|---|
| **Task** | [`T-7`](../tasks/T-7-poroelastic-drainage.md) |
| **Leaf** | none — `T-7` is a "new" row of §6; the question is §4(d) |
| **Verification type** | in-silico (analytic Brinkman/poroelastic model), closed against published measurement |
| **Verdict** | **PASS** — the acceptance predicate is discharged in both halves: bounded, **and** the binding boundary is quantified in every variable |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-7-poroelastic-drainage.json`, produced by `poroelastic.PoroelasticDrainageStudyKt`, 49 `poroelastic` tests green |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous buffer 2–10 mM MgCl₂; **`η = 8.541e-4 Pa·s`**, evaluated at 300 K, not at 20 °C |
| **Consumes** | [`C-0001`](C-0001-layer-stiffness.md) (stiffness, as a *parameter*), [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `d_K`, `α`, `φ`), [`CH-0001`](../challenges/CH-0001-semidilute-premise.md) (validity) |
| **Raises** | [`CH-0003`](../challenges/CH-0003-blob-stack-height.md) against `C-0001` |

---

## Claim

**Poroelastic drainage is not the binding constraint on Gen-1 bandwidth, anywhere in the §3 parameter table:
91× clear at the nominal design point (40 × 40 nm tile, 10 nm layer) and 22.6× clear at the worst point in
the table (70 × 100 nm test tile, 10 nm layer) — both at `C-0001`'s stiffness and on the least permeable
of the three models.**

The tile-on-layer relaxation time is

&nbsp;&nbsp;&nbsp;&nbsp;**`τ = γ / k_layer`**, &nbsp;&nbsp; **`γ = η G A / T`**, &nbsp;&nbsp;
**`T = k h [1 − (2√k/h) tanh(h/2√k)]`**, &nbsp;&nbsp; `f_c = 1/(2πτ)`

with `G` the footprint mean of the Saint-Venant torsion function (`0.0351443 L²` for a square),
`A` the tile area, `T` the Brinkman transmissivity and `k_layer` the layer stiffness.

### The number, at the surviving `C-0001` design points, 40 × 40 nm tile

`τ` in µs and `f_c` in kHz, at `C-0001`'s stiffness. The **bound** is the slowest column.

| `L₀` | `φ` | `k_layer` | `τ` blob-scale | `τ` free-draining | `τ` fibre-array | **`f_c` bound** | **× 1 kHz** |
|---|---|---|---|---|---|---|---|
| 5 nm | 0.0708 | 111.0 pN/nm | 0.090 µs | 0.469 µs | 0.855 µs | **186 kHz** | **186×** |
| 7 nm | 0.0439 | 27.1 pN/nm | 0.140 µs | 0.832 µs | 1.23 µs | **130 kHz** | **130×** |
| 10 nm | 0.0289 | 7.39 pN/nm | 0.206 µs | 1.37 µs | 1.75 µs | **91 kHz** | **91×** |
| 10 nm | 0.0335 | 10.3 pN/nm | 0.155 µs | 1.12 µs | 1.50 µs | **106 kHz** | **106×** |

### The same, on the largest §3 test tile (70 × 100 nm) — the worst case in the table

| `L₀` | `τ` bound | **`f_c` bound** | **× 1 kHz** |
|---|---|---|---|
| 5 nm | 3.52 µs | 45 kHz | 45× |
| 7 nm | 5.03 µs | 32 kHz | 32× |
| 10 nm | **7.05 µs** | **22.6 kHz** | **22.6×** |

**Sensitivity to the stiffness `T-1c` is re-deriving: `τ ∝ 1/k_layer` exactly.**
A factor of two softer halves the corner frequency and nothing else moves.
At `CH-0001`'s own combined correction (×0.61) the worst case is 13.7 kHz; at a quarter of `C-0001`'s
stiffness it is **5.6 kHz — still 5.6× clear**.
Full sweep — 96 design points and 364 (thickness × volume fraction) cells — in the JSON.

---

## The three findings that matter more than the number

### 1. Drainage is a footprint problem, not a thickness problem

The layer thickness **cancels** out of the drainage time. Writing `k_layer = M A / h`,

&nbsp;&nbsp;&nbsp;&nbsp;`τ = η G / (k M f)`, &nbsp; `f = 1 − (2√k/h) tanh(h/2√k)`

so `h` survives only inside the Brinkman wall correction `f`. At fixed volume fraction, `τ` changes by
29 % across 5–10 nm — and in the *opposite* direction to intuition: the **thin** layer is the slow one,
because a thin channel screens its own flow against its walls. A vertically drained layer would have been
four times faster at 5 nm than at 10 nm. What `τ` does scale with is the **square of the tile edge**
(exactly `L²`, because the drag's `L⁴` is divided by the stiffness's `L²`).

### 2. A denser layer drains *faster*

`k ∝ φ^(−1)` (segment-scale) or `φ^(−3/2)` (blob-scale), but `M ∝ φ^(9/4)`, so `k M ∝ φ^(5/4)` or `φ^(3/4)` —
increasing. Squeezing the layer makes it less permeable *and* stiffer, and the stiffness wins.
Across the sweep, `f_c` rises from 4.5 kHz at `φ = 0.005` to 1.5 MHz at `φ = 0.30` for a 10 nm layer under
the Gen-1 tile. **The binding direction is dilution, not densification** — which is the opposite of what
"poroelastic drainage gets worse as you compress" suggests, and it is why the boundary below is a
*lower* bound on `φ`.

### 3. The lateral and vertical drainage paths are nearly equal at the Gen-1 tile

Defined so both give `τ = ℓ²/D_p`:

| tile | lateral `√G` | vertical `2h/π` at `h = 10 nm` | ratio of times |
|---|---|---|---|
| 40 × 40 nm | 7.50 nm | 6.37 nm | **1.39×** |
| 70 × 100 nm | 15.24 nm | 6.37 nm | 5.73× |

The crossover is at **`L = 3.396 h`** for a square tile — a closed form, independent of every material
parameter — and the Gen-1 tile at `L/h = 4.0` sits only 18 % past it. So the lateral path was *not*
obviously dominant and had to be computed. Two consequences: (a) the lateral path is the operative one,
since both bounding surfaces are impermeable; (b) if the origami tile turns out to be hydraulically open,
opening the vertical path buys at most **1.4×** at the Gen-1 tile and 5.7× at the test tile — **tile
permeability is not a design lever**.

---

## Which dissipation channel sets the bandwidth

All three bounded, at the 10 nm / 40 × 40 nm design point with the conservative permeability:

| channel | quantity | value | share |
|---|---|---|---|
| **squeeze-out through the layer** | `γ = η G A / T` | **9.84e-6 pN·s/nm** | **97.0 %** |
| tile broadside Stokes drag | `γ = 16 η R` | 3.08e-7 pN·s/nm | 3.0 % |
| polymer Zimm relaxation | `τ_Z = η R_F³/k_BT` | 0.122 µs | 11× faster than `τ` |
| tile inertia | `m/γ = 2.7 ps`, `Q = 7.0e-4` | — | overdamped by six orders |

**Squeeze-out sets the bandwidth**, and it is 32× the tile's own Stokes drag. The chains relax 11–30×
faster than the water drains, which is the precondition for treating the layer as poroelastic rather than
viscoelastic — checked, not assumed. `Q ≪ ½` discharges the over/under-damped limiting case and licenses
the first-order `τ = γ/k` picture used throughout.

A caution that follows from the permeability spread: at the **blob-scale** permeability the squeeze-out
drag falls to only 4× the Stokes drag, and the transmissivity is 76 % of a free water film. In that reading
the bandwidth is set by **lubrication of a nearly-free water film under the tile, mildly retarded by
polymer** — not by poroelastic drainage in any meaningful sense. Both readings give microseconds.

---

## The boundary: what would make poroelasticity binding

Every entry is "hold everything else at the `L₀` = 10 nm design point and move this one variable until
`f_c` = 1 kHz". Computed by bisection on strictly monotone functions, round-trip verified to 1e-6.

Computed with the **derived** free-draining permeability, because the fibre-array model that gives the
slightly slower bound above is flagged unverified; the corresponding fibre-array frequencies are
91 kHz and 22.6 kHz, i.e. the boundary in every other row moves by a factor of 1.27 and no verdict changes.

| variable | binds at | × the Gen-1 value | reachable? |
|---|---|---|---|
| **frequency** (40 × 40 nm tile) | **116 kHz** | 116× the requirement | operation above this is drainage-limited |
| **frequency** (70 × 100 nm tile) | **28.8 kHz** | 29× the requirement | ditto, and this is the §3 worst case |
| **square tile edge** | **437 nm** | 10.9× | **No** — 4.4× the longest §3 test-tile edge |
| **volume fraction** | **φ ≤ 0.00216** | 0.075× | **No** — deep in the mushroom regime §4(a) rules out; a layer that dilute has no restoring force to actuate against |
| **layer stiffness** | ×0.0086 | 116× softer | **No** — `CH-0001`'s own corrections come to ×0.61 |
| **permeability** | ×0.0086 | 116× less permeable | **No** — the three models span 40× and the bound is already the slowest; every *measured* hydrogel permeability is higher, not lower |

### The 1 kHz contour, and the reason it is not a prediction

| tile | `h` = 3 nm | 5 nm | 7 nm | 10 nm | 15 nm | 20 nm | 30 nm |
|---|---|---|---|---|---|---|---|
| 40 × 40 nm | φ ≤ 0.0048 | 0.0033 | 0.0026 | 0.0022 | 0.0019 | 0.0019 | 0.0020 |
| 70 × 100 nm | φ ≤ 0.0096 | 0.0066 | 0.0054 | 0.0045 | 0.0038 | 0.0035 | 0.0033 |

**At every §3 thickness the contour lies in a region where the Darcy premise has already failed**
(`√k/h` = 0.25–0.80 there, against 0.09–0.13 at the design point). So the boundary is reported as
*where this model would say it binds*, not as a prediction that it does. The honest statement is:
**the design would have to leave the poroelastic model's domain of validity before poroelasticity could
become the binding constraint**, and by then the layer is a mushroom carpet that fails §4(a) first.

**Composite worst case.** The largest §3 test tile, the thickest layer, the least permeable model and a
stiffness four times below `C-0001`'s, all at once: **5.6 kHz, still 5.6× the requirement.**

---

## Validity range

Respected downstream, and enforced in code where it can be:

- **Darcy/Brinkman continuum.** Valid where `√k ≪ h`. On the segment-scale permeability models
  `√k/h = 0.09–0.13` and it holds. **On the blob-scale model `√k/h = 0.56–0.58` and it does not** —
  the layer is 1.7–1.8 screening lengths thick, so there is no separation of scales and the continuum
  reading is qualitative. The Brinkman transmissivity is used precisely because it contains the free-film
  limit that case degrades to; plain Darcy would have overstated the drainage rate by 5×.
  **This is the falsifier `T-7` declared in advance, and it fired.** The bound is quoted from the models
  where it did not.
- **Permeability is uncertain by a factor of 40 for this layer**, and the uncertainty is structural, not
  sloppy: the layer has no separation between its monomer scale and its blob scale (`CH-0001` for the
  thermodynamic version, `CH-0003` for the geometric one). Every number here is quoted from the slow end.
- **Rigid, impermeable tile.** If the tile dishes (`T-5b`) the footprint does not sample one `h`; if it is
  hydraulically open the vertical path opens (worth ≤ 1.4×).
- **Linear poroelasticity.** `k` and `M` are held at the stated `φ`. Under a 3 nm stroke on a 10 nm layer
  `φ` rises 40 % and both move — making the layer **faster**, so `τ` remains an upper bound, but the
  transient is not a single exponential.
- **No electrostatics, no ion partitioning, no electro-osmosis.** The layer is porous, the buffer ionic
  and the electrode biased, so a streaming potential opposes the squeeze flow. Not bounded here.
- **Pure water.** The viscosity is that of pure water at 300 K; the equation of state behind `M` was
  fitted in pure water (`C-0002`). The Gen-1 buffer is 2–10 mM MgCl₂ — a < 0.1 % viscosity increment,
  stated rather than modelled.
- **Linear PEG.** A PS→PEG block copolymer (§3) is not the material any of this was built on.

## Numbers that are cited rather than derived

Flagged, per §7 of the problem definition:

- **`η = 2.414e-5 · 10^(247.8/(T−140))` Pa·s** — **CITED** (Vogel-type engineering correlation).
  Cross-checked against the IAPWS reference value at 20 °C, which it reproduces to 0.02 % without being
  fitted to it here. `τ ∝ η` exactly.
- **`k = ξ²` with prefactor unity** — **CITED**, Offeddu, Axpe, Harley & Oyen, *AIP Adv.* **8**:105006
  (2018), who measure it on **PEG hydrogels** by poroelastic indentation and state that "the square root
  of the intrinsic permeability approximates the size of the fluid path, in this case corresponding with ξ"
  (`k ∝ ξ²`, `r² = 0.92`; `k ∝ φ^(−3/2)`, `r² = 0.96`). The exponent is reproduced exactly rather than
  fitted. Read from the article full text, not from a search summary.
- **`k = r²(3/20φ)(−ln φ − 0.931)`** — **CITED, AND NOT VERIFIED AGAINST THE PRIMARY SOURCE.**
  Jackson & James, *Can. J. Chem. Eng.* **64**:364 (1986) is paywalled and was not obtained. The constants
  come from secondary literature, which `CLAUDE.md` says is not good enough to act on. It is therefore
  used **only** as a cross-check on the derived free-draining model, which it agrees with to a factor of
  1.3, and nothing in this claim changes if it is wrong.
- **Hydrogel permeability and modulus data** used for the gate-5 cross-check — **CITED**, Gao & Cho,
  arXiv:2209.14382, Table 1: 21 gels, `k = 2.8–23.1 nm²`, `K = 7.0–85.7 kPa`.
- **Tile mass density 1.7 g/cm³** for hydrated DNA origami — **CITED**, and load-bearing for nothing:
  it enters only the inertial check, which is clear by six orders of magnitude.

Everything else — the Brinkman transmissivity, the Saint-Venant footprint factor, the free-draining
segment friction, the grafted longitudinal modulus, the drag coefficients and every time — is derived
from the §3 parameters and from `C-0001` / `C-0002`.

## Cross-checks passed

1. **Gate 1** — `η G A / T` reduces to `pN·s/nm`; the mass unit is checked both ways against SI.
2. **Gate 2** — Darcy limit to 0.2 %, free-film limit to 1e-6 **against the classical Reynolds squeeze-film
   coefficient `0.42174 η L⁴/h³`**, infinite-strip limit to 0.07 %, `Q = 7e-4` overdamped.
3. **Gate 3** — the drag route and the poroelastic-diffusion route are proved to be the *same number*
   (1e-12) in the Darcy limit; the footprint factor is exactly homogeneous of degree two and symmetric;
   `τ ∝ L²` exactly in the drainage channel.
4. **Gate 4** — the Fourier series converges to 9e-8 at 201 harmonics; the permeability log-derivatives
   converge quadratically; both bisections round-trip to 1e-6.
5. **Gate 5** — our poroelastic diffusivity (`2.96e7 nm²/s`) is a factor of **2.7–10.5 slower** than any
   of 21 measured hydrogels, i.e. conservative and within an order; the `k = ξ²` prefactor is validated on
   PEG specifically; the correlation length agrees with the Flory radius at overlap to 12 % and with the
   Alexander blob (grafting spacing) to 13 %.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **The hydrodynamic screening length of *this* layer is not known to better than a factor of 6.4.**
   No source was found measuring it for a grafted PEG layer at `σ ≈ 0.024 nm⁻²`. Settling it needs either
   that measurement or explicit-solvent simulation, and at a 22× margin neither is worth buying.
2. **Electro-osmotic coupling is not bounded.** Raised as `T-7b`; it needs `T-6`'s screening model.
3. **Jackson & James (1986) primary source not obtained.** Named above.
4. **Whether the origami tile is hydraulically permeable is not established** — and it does not matter,
   which is itself the result.

## Challenges

[`CH-0003`](../challenges/CH-0003-blob-stack-height.md) is raised **by** this claim against `C-0001`,
on the geometric ground that the Alexander-de Gennes layer is `(Σ/π)^(5/6)` blobs tall — 1.47 at the
`Σ = 5` convention, 1.48–1.73 at every surviving design point. None stands against this claim.
