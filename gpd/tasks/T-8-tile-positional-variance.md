# T-8 — Tile positional variance at 300 K

| | |
|---|---|
| **Leaf** | `A1.2` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`), with `A1.1` as its bound table |
| **Problem definition** | §6 task 8; parameters §3; bandwidth §3; geometry §1; process §5, §7 |
| **Verification type** | in-silico (analytic multi-mode equipartition on a Rayleigh-Ritz plate functional) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0010`](../claims/C-0010-tile-positional-variance.md) |
| **Consumes** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the structure), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (the stiffness bracket), [`C-0004`](../claims/C-0004-poroelastic-drainage.md) (the bandwidth), [`C-0002`](../claims/C-0002-peg-material-parameters.md) (the material) |
| **Raises** | [`CH-0009`](../challenges/CH-0009-worst-point-is-not-the-centre.md) against `C-0006` |

---

## Formulate

### The question, as a numeric target

Produce the RMS thermal fluctuation of the Gen-1 tile's position at 300 K,
**for every degree of freedom the tile has against the polymer layer** — not for one —
and compare it against the 3.0 nm threshold of §6 task 8.

`C-0001` answered this with a single number, `√(k_BT/k) = 0.28 nm` at the 10 nm design point.
That number is the **piston mode of a rigid plate**,
and [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) has already established that the tile is not a rigid plate.
So the deliverable is a **budget**, not a number,
and the first thing it has to settle is *which* quantity the predicate is read against —
because "the tile's positional RMS" has at least four inequivalent readings
and they differ by a factor of seven.

### Acceptance predicate

§6 task 8, verbatim:

> σ_RMS ≤ 3.0 nm for the nominal Gen-1 tile.

Leaf `A1.2`'s own acceptance string is **stricter**, and is quoted here verbatim
rather than paraphrased, because part of the deliverable is saying which half of it is discharged:

> **Simulate as-designed lever positional variance at 300 K.**
> Tool/method: *Coarse-grained/MD (oxDNA/Martini) ensemble.*
> Acceptance: *Simulated sigma_RMS <= 3.0 nm for nominal Gen-1 lever; 95% CI reported.*

And leaf `A1.1` supplies the bound table this has to be consistent with:

> Bound table: sigma=3 nm -> k>=~0.46 pN/nm; sigma=0.1 nm (prize) -> k>=~414 pN/nm;
> sigma=0.03 nm -> k>=~4.6 N/m (kBT=4.14 pN.nm @300K).

Discharged when all seven hold:

1. the fluctuation is reported **mode by mode** — piston, the two rigid tilts,
   the internal shape (dishing) modes, and the lateral translation —
   with the dominant one named;
2. it is reported across the **`C-0003` stiffness bracket**, not at one stiffness,
   and **at a stated compression**, never "at the resting height"
   (`C-0001` surprise `S-1`, which `C-0003` upholds);
3. the lateral and tilt stiffnesses are **derived from the layer and the anchoring model**,
   not asserted — or, where they cannot be, declared undetermined with the requirement stated;
4. the variance is reported against a **stated measurement bandwidth**,
   with the fraction below 1 kHz given, because §3 asks for ≥ 1 kHz operation;
5. the verdict against 3.0 nm is given **with its margin**, across the whole bracket;
6. the parts of leaf `A1.2`'s acceptance string that are **not** discharged are named as such,
   and not substituted for;
7. all five gates pass, with gate 3 checking something *independent* of the construction.

### Units, locked

SI throughout, in the programme's scaled form.
Lengths and RMS amplitudes in **nm**; forces in **pN**; energies in **pN·nm** and eV;
stiffness in **pN/nm** (= mN/m); foundation stiffness in **pN/nm³**;
flexural rigidity in **pN·nm**; drag in **pN·s/nm**; diffusivity in **nm²/s**;
frequency in **Hz**; time in **s**; temperature in **K**.
`k_BT = 4.142 pN·nm` at 300 K, aqueous buffer, 2/5/10 mM MgCl₂.

### Geometry and sign conventions, fixed before deriving

Inherited unchanged from [`T-5`](T-5-load-distribution.md) and [`T-5b`](T-5b-tile-flatness.md):
`x` along the helices, `y` across them, origin at the centre of the 40 × 40 nm footprint,
`w` positive **downward**, compressing the polymer layer.
Every amplitude reported here is a root-mean-square and therefore unsigned.

Three further conventions this task has to fix, because they are exactly where the ambiguity lives:

- **`pistonRms` is the fluctuation of the tile's area-averaged height**, and it is the *only* mode with a non-zero area average:
  the two tilts and every dishing mode integrate to zero over the footprint by construction.
  So the tile's *mean position* fluctuates by the piston amount and by nothing else.
- **`areaRms = √(piston² + tilt² + dishing²)`** is the RMS over the ensemble **and** the footprint —
  the fluctuation of a typical material point.
- **`centreRms`, `edgeMidpointRms`, `cornerRms`** are the fluctuations of a *named material point*,
  which is what a point-coupled lever samples.
  They are **not** equal to each other: even for a perfectly rigid tile the corner exceeds the centre by exactly `√7`,
  because both tilts are at full lever there.

### The acceptance quantity, declared in advance

The predicate says "σ_RMS" without saying of what.
Four readings are available and they differ by 7× at the working point:

| reading | at the nominal working point | what it is |
|---|---|---|
| piston | 0.26 nm | the tile's **mean height** — what an ideal area-averaging sensor reads |
| area RMS | 0.89 nm | a **typical point** of the tile, ensemble- and area-averaged |
| centre point | 0.70 nm | the *stiffest* point of the footprint |
| corner point | 1.92 nm | the **worst** point — where a badly placed lever would attach |

**The declared acceptance quantity is the area RMS**, with the **worst point reported alongside**.
Choosing silently between these would have been the failure mode of this task,
so the choice is made in Formulate and the other three are reported anyway.

### What is deliberately excluded

- **Electrostatics.** Under bias §1's electrostatic spring constant is *negative*,
  so `k_eff < k_brush` and every amplitude here is a **lower bound** under bias.
  `T-4` owns that, and it is the one correction that runs the wrong way.
- **The lever.** Leaf `A1.2` says "lever positional variance"; this task produces the **tile's**.
  `C-0006` shows the two differ by the local dishing wherever the coupling is not effectively continuous,
  and that the coupling cannot be made continuous with fewer than 64 attachment points
  against the 56 crossovers the tile contains (`C-0006` as corrected by `C-0009`).
- **Anharmonicity.** Every case is the *tangent* stiffness at a stated compression.
  A fluctuation comparable to the compression itself would leave this linearisation,
  and the cases where it does are reported as **undefined** rather than as large numbers.

---

## Plan

### The method choice, and the explicit decision about leaf `A1.2`'s named method

Leaf `A1.2` names a **coarse-grained/MD (oxDNA/Martini) ensemble** and demands a **95 % CI**.
This task does **not** run one. The reasoning, costed per §5:

| | analytic multi-mode equipartition (chosen) | coarse-grained ensemble (leaf `A1.2`'s named method) |
|---|---|---|
| what it gives | the covariance of the generalised coordinates, `k_BT K⁻¹`, exactly | a sampled trajectory, hence a genuine sampling CI |
| cost | seconds; ~80 % of the machinery already exists in `src/main/kotlin/structure/` from `T-5b` | days of wall-clock on 8 cores, plus the `T-9` hinge constant it would need first |
| the layer | present, as the Winkler foundation whose stiffness `C-0003` derives from measured osmometry | **absent** — oxDNA models the origami, not the grafted PEG it rests on |
| the uncertainty it reports | the **model bracket**: six `C-0003` free energies, four compressions, the `C-0006` `k_θ` sweep | a sampling interval around *one* model |

The third row is decisive and is the reason this is not merely a cost argument.
**oxDNA would answer a different question.**
The tile's positional variance is set by the *layer* it rests on, and oxDNA has no representation of that layer;
running it as specified would produce a confidence interval on the fluctuation of a free origami sheet in buffer,
which is not the quantity §6 task 8 asks for.
To answer the right question it would have to carry the layer as an external potential —
and that external potential would be exactly the `C-0003` stiffness this task uses directly,
so the sampling would add a statistical error bar to a number the analytic route already has exactly.

The fourth row is the honest statement about the CI.
**A 95 % confidence interval on an exact analytic result is a category error.**
There is no sampling here, so there is no sampling interval;
the real uncertainty is the model bracket, and that is what is reported.
Quoting the model bracket *as* a CI would be worse than reporting no CI at all,
because it would imply a statistical meaning it does not have.

Per §7 — *"where a question can't be answered with the available methods, that is stated plainly instead of being answered anyway"* —
the simulated-ensemble-with-CI half of `A1.2` is recorded as **not discharged**,
with the costed proposal for what would discharge it in the Result section.

### The cheap bound runs first

Leaf `A1.1` **is** the cheap bound, and it is NDI's own:
`k ≥ k_BT/σ²`, so `σ = 3 nm` needs `k ≥ 0.4602 pN/nm` over the whole tile.
`C-0003`'s softest defined stiffness anywhere in the bracket is 3.31 pN/nm — 7.2× above it —
so the *piston* mode passes before anything is computed,
and the only way the predicate can fail is through the modes `C-0001` did not count.
That is what makes the expensive calculation worth its (small) cost:
it is not being run to get the piston number, which is a one-line closed form.

### The stiffness bracket is re-derived, not copied

Per §7, inherited numbers get re-derived.
`C-0003`'s six (profile × interaction) layer readings are **rebuilt in code** from the same measured
PEG/water virials, at the same 10 nm / `σ = 0.024 nm⁻²` design point,
and the resulting tangent stiffnesses are asserted against `C-0003`'s published table as a gate-5 test.

### The bandwidth comes from `C-0004`, not from an assumption

`C-0004` establishes that the tile is overdamped by six orders (`Q = 7 × 10⁻⁴`),
which is what licenses treating each mode as first-order with a single Lorentzian.
The drag is `C-0004`'s own squeeze-out coefficient `η G A / T` plus the tile's broadside Stokes drag,
evaluated at each case's **compressed** height and volume fraction,
on the least permeable of `C-0004`'s three permeability models —
so the corner frequency is the *lowest* the three models allow
and the in-band variance fraction is an upper bound.

The piston mode's corner is used for the whole budget.
That is conservative and provably so: for a mode of wavelength `L/n`
the drainage path shortens as `L/n` (so `γ_n ∝ n⁻²`) while the modal stiffness only *rises* (`k_1 + Dq⁴A`),
so `τ_n ≤ τ_1` and every other mode has a **higher** corner than the piston.
Since the in-band fraction is monotone decreasing in the corner frequency —
asserted as a gate-2 test rather than argued — using the piston corner bounds the in-band share from above.

### The lateral mode is not given a number it has not earned

A brush resists lateral tile motion very differently from how it resists normal compression,
and the honest answer is that it does not resist it at all.
The free energy of a laterally homogeneous grafted layer under a laterally homogeneous non-adsorbing tile
is **invariant under lateral translation of the tile**,
so the mean lateral restoring force vanishes identically — by symmetry, not by smallness.
The same argument kills the yaw mode.

Equipartition therefore does not apply to it, and the task does not pretend otherwise.
What is computed instead is (a) the **diffusive** excursion over a stated observation time,
from a Brinkman shear drag derived from `C-0004`'s own permeability,
and (b) the **requirement** any anchoring scheme would have to meet, from leaf `A1.1`.
No lateral stiffness is invented.

### What would falsify this approach

1. **A dishing amplitude comparable to the layer height.**
   Then the tile has contacted the electrode somewhere and the linear Winkler foundation is gone.
2. **A stiffness so low that the piston RMS exceeds the layer height.**
   Then the harmonic linearisation has left its own domain and the answer is "undefined", not a large number.
   *Declared in advance because `C-0003` gives exactly zero stiffness at first contact for three of its six models.*
3. **The point fluctuation failing to converge in the Ritz basis.**
   A Ritz restriction can only stiffen, so the compliance must rise monotonically with the basis degree;
   if it did not, the solve would be wrong rather than merely truncated.
4. **The static-compliance route and the modal-covariance route disagreeing.**
   They are the same number by fluctuation-dissipation; disagreement would mean the load assembly,
   the quadrature or the factorisation is wrong.
5. **The in-band fraction coming out of order unity.**
   Then the bandwidth split would be doing no work and the broadband number would be the whole answer.
6. **The `k_θ` sweep moving the answer materially.**
   Then `T-8` would inherit `T-9`'s open premise and would have to wait for it.

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t8
./gradlew study -Pstudy=structure.TilePositionalVarianceStudyKt -PbuildDirectory=build-t8
```

Code: `src/main/kotlin/structure/TilePositionalVariance.kt` (the budget, the bandwidth split,
the lateral machinery) and `src/main/kotlin/structure/TilePositionalVarianceStudy.kt` (the study).
Both are new files; nothing in the shared `structure` package was modified,
because `T-10` is working the same package concurrently.

Result: [`../results/T-8-tile-positional-variance.json`](../results/T-8-tile-positional-variance.json),
deterministic — verified by re-running and diffing, no change.
21 defined cases (4 compressions × 6 `C-0003` models, less 3 that are undefined),
plus the `k_θ` sweep, the anchored variant, the lateral block, the `A1.1` bound table
and the basis-degree convergence record.

Tests: `src/test/kotlin/structure/TilePositionalVarianceTest.kt`, 26 tests, each named for the gate it discharges.

---

## Verify

### The five gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | equipartition round-trips a stiffness through an amplitude; `D γ = k_BT` exactly; the Brinkman shear drag is `η · area / length` and halving the screening length doubles it; a variance fraction is dimensionless and bounded by 1 | **PASS** |
| **2 — limiting cases** | a rigid plate gives **exactly** 1, `√2`, `√3` and `√7` pistons for centre / tilt / area / corner; a 4× stiffer foundation halves every mode; the dishing-to-piston ratio is monotone increasing in `k_f`; the Brinkman drag reduces to the free-film Couette value `ηA/h`; the in-band fraction runs 0 → ½ → 1 through the corner and is monotone **decreasing** in the corner frequency; the unconfined lateral excursion grows exactly as `√t` | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the corner-point RMS is monotone non-decreasing in the basis degree and converged to **1.9169 nm at degrees 12/16/20** (< 0.01 %); the area RMS 0.866/0.894/0.904/0.908 at 8/12/16/20, 0.4 % between the last two | **PASS** |
| **5 — literature cross-check** | leaf `A1.1`'s bound table reproduced from `k_BT` alone (0.46 / 414 / 4600 pN/nm); `C-0006`'s thermal amplitudes reproduced (0.748 / 1.272 / 1.365 nm); `C-0003`'s six tangent stiffnesses reproduced to < 0.1 % by rebuilt free energies | **PASS** |

### Gate 3 — what is checked, given that equipartition *is* the construction

The problem definition names equipartition as gate 3,
and this task's whole method is equipartition — so asserting `σ² = k_BT/k` here would be a tautology.
Four genuinely independent things are checked instead:

1. **Fluctuation-dissipation through a static solve.**
   The piston variance is `k_BT (K⁻¹)₀₀`, which the budget takes from the Cholesky inverse diagonal.
   The *same* number is `k_BT` times the tile's static compliance under a unit uniform load —
   a different code path entirely, which assembles a load vector by Gauss quadrature over a pressure field
   and back-substitutes. They agree to `1e−9` at three foundation stiffnesses.
   Nothing about the construction guarantees that; a wrong quadrature or a wrong factorisation would break it.
2. **The same at a point, reciprocally.**
   The point variance equals `k_BT ×` the deflection at `x` under a **unit point load at `x`**.
   Checked against the solver's own centre-point fluctuation.
3. **The Lorentzian sum rule.**
   `∫₀^∞ 4k_BTγ/(k² + (2πfγ)²) df = k_BT/k`, integrated **numerically** to `1e−6`,
   so the closed-form `arctan` split is checked against the spectrum rather than assumed to describe it.
   This ties the bandwidth answer to the variance answer;
   without it the two halves of the deliverable would be unconnected.
4. **The Ornstein-Uhlenbeck bridge.**
   A confined coordinate reduces to free diffusion for `t ≪ γ/k` and to equipartition for `t ≫ γ/k`,
   so the lateral free-diffusion bound is the `k → 0` limit of the same theory rather than a rival to it.
   (This one caught a real error: the *variance* of an OU process relaxes at `2/τ`, not `1/τ`,
   and dropping the factor gives `√(Dt)` instead of `√(2Dt)` — a `√2` no dimensional check would catch.)

### The declared falsifiers

| falsifier | fired? | outcome |
|---|---|---|
| 1. dishing comparable to the layer height | no | worst dishing 1.51 nm against a 10 nm layer |
| 2. stiffness below `k_BT/L₀²` | **yes, for three of six models at first contact** | the strong-stretching models have **exactly zero** stiffness at `L₀`, so the *unbiased* variance is undefined, not large. Reported as `undefinedCases` in the JSON with the reason, and the predicate is answered at a stated compression instead. This is the sharpest form of `C-0001`'s surprise `S-1` |
| 3. point fluctuation not converging | no | converged to < 0.01 % by degree 12 |
| 4. the two fluctuation-dissipation routes disagreeing | no | agree to `1e−9` |
| 5. in-band fraction of order unity | no | **0.55–3.07 %** across the whole bracket — the bandwidth split does most of the work |
| 6. the `k_θ` sweep moving the answer | **no, and this is a result** | over Chen et al.'s full admissible `α ∈ [0.6, 1.2]`, `D_⊥` moves 2× and the area RMS moves **2.5 %** (0.886 → 0.918 nm). `T-8` does **not** inherit `T-9`'s open premise |

### The predicate, item by item

1. reported mode by mode, with the dominant one named — **yes** (the shape modes, by 2.8–3.0× at the working point);
2. across the `C-0003` bracket at four stated compressions — **yes**;
3. lateral and tilt derived rather than asserted — **yes**: the tilt from the same Winkler foundation as the piston,
   the lateral **derived to be exactly zero** and reported as unbounded-by-the-layer with a stated requirement;
4. reported against a stated bandwidth with the < 1 kHz fraction — **yes**;
5. verdict with margin across the bracket — **yes**;
6. the undischarged parts of `A1.2` named — **yes**, in `leafAcceptanceNotDischarged` in the JSON and in `C-0010`;
7. five gates with an independent gate 3 — **yes**.

## Result

Filed as [`C-0010`](../claims/C-0010-tile-positional-variance.md).
**PASS** on §6 task 8's predicate at the operating point, on the declared acceptance quantity,
across the whole `C-0003` bracket — and **FAIL at the tile's corners** in the lightly compressed states,
which is why [`CH-0009`](../challenges/CH-0009-worst-point-is-not-the-centre.md) is raised against `C-0006`.

### What would discharge the rest of leaf `A1.2`, costed

Not started, and not to be started without the coordinator's go-ahead.

| step | cost | prerequisite | what it buys |
|---|---|---|---|
| `T-9` — crossover hinge constant `k_θ` from oxDNA | days on 8 cores | none (tooling installed) | replaces the fitted `1/100`. **`T-8` shows this buys `T-8` almost nothing** (2.5 % on the area RMS); it is `T-5`/`T-5b`/`T-10` that need it |
| oxDNA ensemble of the bare tile in buffer, µs scale | days | `T-9`'s force field | a genuine 95 % CI — **on the wrong quantity**, because the layer is absent |
| the same **with the layer as an external potential** derived from `C-0003` | days, plus the coupling work | `T-9`, `C-0003` | a genuine 95 % CI on the right quantity, and the only route that discharges `A1.2` as written. It would test the one thing the analytic route assumes: that the tile's modes are harmonic and that the Kirchhoff plate reduction survives at `ℓ_⊥/p < 1` |
| `T-10` — discrete lattice check | **already done, concurrently** | none | the cheap partial substitute for the row above. [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) landed during this iteration and measures the plate-to-lattice **thermal** ratio at **1.113–1.199**, so the dishing amplitudes here are underestimates by 11–20 % — a cited correction rather than an argument, and one that moves no verdict |

The honest ordering was that **`T-10` should run before any ensemble is contemplated**,
because it tests the same premise (the continuum plate reduction, which is marginal by its own criterion here)
for hours rather than days. It has now run, it upholds the reduction to within 20 % for the thermal case,
and **the ensemble therefore buys only the CI** — which is exactly the thing the analytic route cannot supply
and the thing whose absence is recorded above rather than papered over.

## Feedback into Formulate

- **`T-4` inherits a tightened predicate.** Every amplitude here is a *lower* bound under bias,
  because `k_es < 0`. The margin at the working point is 3.4× on the area RMS and 1.5× at the corner,
  so `T-4` must report `k_eff`, not just the pull-in voltage: a bias that halves `k_eff`
  puts the corner over the predicate before it puts the tile on the electrode.
- **`T-2`'s design window gains a second topological constraint**, and it is the same one `C-0006` found:
  the output coupling must be distributed, *and* the tile must be anchored laterally at `k ≥ 0.46 pN/nm`,
  which §3 supplies nothing for.
- **A new question, not previously in the queue:** the unbiased tile is unconfined in the normal direction too,
  because a non-adsorbing layer exerts no upward force above `L₀`.
  Whatever holds the tile down at zero bias is not in the §3 stack, and it is not this task's to invent.
