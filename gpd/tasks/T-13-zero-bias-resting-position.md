# T-13 — Where the tile sits at zero bias, and what holds it there

| | |
|---|---|
| **Leaf** | `A1.2` (the 3.0 nm positional bound, read at zero bias rather than at the operating point), with `A1.1` as its bound table and **`A8.2`** for the coupling that would have to supply the preload |
| **Problem definition** | §1 (the stack, and the fact that it names nothing that holds the tile down); §3 (geometry, heights, buffer, stroke, force); §4(a), §4(f); §5, §7 (process) |
| **Verification type** | in-silico (a one-dimensional force balance at `V = 0` assembled from six candidate mechanisms, each computed rather than asserted) **+ logical** (a topology argument that decides which mechanisms *can* have the right sign before any of them is evaluated) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) |
| **Consumes** | [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (the layer, as a library), [`C-0011`](../claims/C-0011-scf-density-profile.md)/[`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) (that `L₀` is a convention with a defining load), [`C-0010`](../claims/C-0010-tile-positional-variance.md) (the zero, the drag, the bandwidth treatment), [`C-0014`](../claims/C-0014-lateral-confinement.md) (the tether, the elements, the allowables, the over-stiffening result), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the committed coupling `K2`, and the preload relation it declined to evaluate), [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md)/[`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md) (the zero-bias field and the Stern series), [`C-0005`](../claims/C-0005-mean-field-screening-validity.md) (the mean-field error carried) |
| **Raises** | [`CH-0023`](../challenges/CH-0023-placement-preload-sign.md) against `C-0017`, [`CH-0024`](../challenges/CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md) against `C-0012`/`C-0017` |

---

## Formulate

### The gap this task exists to close

Three claims have now said, independently, that nothing in the §3 stack owns the unbiased state.

- [`C-0010`](../claims/C-0010-tile-positional-variance.md): *"a **non-adsorbing** layer exerts no *upward* force above `L₀` at all, so an unbiased free tile is unconfined in **both** directions. Whatever holds the tile down at zero bias is not in the §3 stack."*
- [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md): three of six layer models have **exactly zero** stiffness at `L₀`, because the strong-stretching disjoining pressure vanishes quadratically there.
- [`C-0017`](../claims/C-0017-output-coupling-stiffness.md), in its own *Still open* list: *"The preloaded branch is not evaluated. A coupling stiffer than the placement value needs a **downward** preload the layer must carry at zero bias … `T-13`."*

And one claim has partly answered it as a by-product: [`C-0014`](../claims/C-0014-lateral-confinement.md) finds that its passing `S3` scheme —
eight ssDNA tethers through the layer — pulls the tile **down** with **4.6–9.4 pN**, *"which is exactly the missing preload — and it is the same element that confines it laterally. `T-13` should evaluate that before inventing anything else."*

**This task closes the gap: it names the zero-bias resting position, names and quantifies every mechanism that could produce one, and says whether the result is a stable equilibrium or an admission that the device has none.**

### The question, as a numeric target

Two numbers and a verdict:

1. **`h₀`**, the tile's zero-bias resting height in nm above the electrode, **with the load its definition rests on stated**;
2. **`k₀ = −dU_net/dh` at `h₀`** in pN/nm, and the positional statistics that follow from it — broadband and **in the ≥ 1 kHz band**, per `C-0010`;
3. the verdict: **is there a stable zero-bias equilibrium at all**, and which mechanism supplies it.

### Why `h₀` cannot be quoted without a load, and what that does to the target

`CH-0010` established that a solved layer reaches `P = 0` only asymptotically, so `L₀` is a **convention with a defining load** (`C-0011`: 1 pN over the 40 × 40 nm tile, with 0.1 and 10 pN carried, and a hundred-fold change moving `N` by 2.5×).

This task inherits that and sharpens it into its own deliverable, because here the defining load is not a convention at all:

> **The zero-bias resting height is exactly the force-onset height defined at the total downward hold-down force.**
> `h₀` solves `P(h₀)·A = F_down(h₀)`. So *naming the hold-down mechanism is the same act as fixing the height convention*, and the sensitivity of `h₀` to the defining load is a **design sensitivity**, not a bookkeeping one.

`h₀(F_down)` is therefore reported as a curve over `F_down ∈ [0.1, 100] pN` at every state, and never as a single number without its load.

### The thermal scale every mechanism is judged against, derived rather than assumed

Above `L₀` the layer contributes nothing, so a hold-down of magnitude `F` confines the tile there through a **linear** potential, not a quadratic one. For `Φ(h) = F·(h − L₀)`, `h > L₀`, the upward excursion is exponentially distributed with

&nbsp;&nbsp;&nbsp;&nbsp;`⟨h − L₀⟩ = k_BT/F` and `RMS(h − L₀) = √2 k_BT/F`.

Holding the mean upward excursion inside leaf `A1.1`'s own 3.0 nm therefore requires

&nbsp;&nbsp;&nbsp;&nbsp;**`F_down ≥ k_BT/3.0 nm = 1.3807 pN`** — the *force* analogue of `C-0010`'s `k ≥ k_BT/σ² = 0.4602 pN/nm`, and the scale every candidate below is measured against.

The RMS reading is `√2` stricter (1.9526 pN) and is reported alongside. **The declared acceptance is the mean reading**, stated here rather than chosen afterwards, and the distinction matters because the zero-bias distribution is **not Gaussian**: harmonic below `h₀`, exponential above it. Equipartition is therefore *not* used to produce the variance; it is used to check it in the limit where it applies.

### Units, locked

Lengths in **nm**, forces in **pN**, stiffness in **pN/nm** (= mN/m), pressure in **pN/nm²** (= 1 MPa exactly),
energies in **pN·nm** and `k_BT`, **Hamaker constants in `pN·nm`, which is exactly the zeptojoule** (`1 zJ = 10⁻²¹ J = 1 pN·nm`),
frequencies in Hz, drag in `pN·s/nm`, densities in `g/cm³`, potentials in V, charge density in `e/nm²`.
`k_BT = 4.142 pN·nm` at 300 K, aqueous buffer, **2 mM MgCl₂** (§3's lowest, and `C-0008`'s zero-bias column).

### Geometry and sign conventions, fixed before deriving

Inherited unchanged from `T-12` and `T-16`:

- `z` normal to the electrode, positive **away** from it; the electrode surface is `z = 0`; the tile's underside sits at `h`.
- **the electrostatic gap is the layer height, exactly** (`C-0012`'s convention).
- **`U_net(h) = P(h)·A − F_down(h)` is the net force on the tile, positive UPWARD.** A *hold-down* is any mechanism contributing to `F_down > 0`.
- a stable equilibrium is a root of `U_net` with `dU_net/dh < 0`; the stiffness there is `k₀ = −dU_net/dh`, so a mechanism whose force **grows as the gap closes** contributes **negative** stiffness — the same structure as `k_es < 0`, and it is checked with the same care.
- **the stroke a hold-down costs is `L₀ − h₀`**, taken off the top of the actuator's travel, because the working point is fixed by the load and not by where the tile started.

### The six candidate mechanisms, declared in advance

Every one is evaluated and reported, including the ones that are certainly negligible — §7 rewards saying which terms were checked.

| id | mechanism | sign | why it is in the list |
|---|---|---|---|
| `M1` | **entropic through-layer tether** (`C-0014`'s `S3`), grounded on the substrate | **down** | `C-0014` says to evaluate this first |
| `M2` | **the committed output coupling** `K2` (`C-0017`), 45 paths to a lever above | see below | the programme has already bought it; does it solve `T-13` for free? |
| `M3` | **residual electrostatics at zero applied bias** — induced countercharge on a grounded electrode, in series with the compact layer, in the asymmetric 2:1 buffer | down or up | §1's mechanism does not switch off at `V = 0`, and `C-0008` reports a **sign-changing near-cancellation** there |
| `M4` | **van der Waals across the gap** | **down** | it is always attractive here, it cannot be designed away, and nothing in this programme has yet evaluated it |
| `M5` | **gravity and buoyancy** | down | certainly negligible; stated as a bound because §7 rewards saying it was checked |
| `M6` | **depletion or bridging by the PEG layer itself** | down | the *non-adsorbing* premise is what makes `C-0010`'s lateral zero exact; it is load-bearing and it has never been tested |

### The topology argument, which runs before any number

A mechanism can only hold the tile **down** if its load path is grounded **below** the tile or is a body force.
That is not a modelling choice, it is the geometry:

> **A taut flexible link pulls its two ends together.** A link grounded on the substrate (`M1`) therefore pulls the tile *down*;
> a link grounded on a lever *above* the tile (`M2`) pulls it *up*. Only a two-sided element — one that carries compression as
> well as tension — can be mounted with a preload of either sign.

`C-0017`'s `K2` path is a 5 nm duplex standoff **in series with** a tuned ssDNA spacer carrying **99.6 % of the compliance**.
A single-stranded chain carries no compression at all. So `K2` is a one-sided element and `R(0) = 0` identically —
which is exactly what `C-0017` assumed when it took every coupling unpreloaded.

**The prediction this makes, before any code runs: `M2` contributes exactly zero, and the reason is the same element that closed `T-16`.**
It is written down here so that finding it is a confirmation rather than a discovery after the fact.

### What "an answer to `T-13`" has to deliver, in full

Discharged when all eight hold:

1. `h₀` at every §3 layer height, over all six `C-0003` models, **with the defining load stated and its sensitivity reported over three decades**;
2. every one of `M1`–`M6` given a **sign** and a **magnitude in pN** at 300 K in 2 mM MgCl₂, at 5 / 7 / 10 nm, each with its provenance flagged `derived`/`cited`;
3. each judged against the derived thermal scale `k_BT/3 nm = 1.3807 pN` and against the buoyant scale of `M5`;
4. the **net** verdict: is there an equilibrium, is it stable, what is `k₀`;
5. the positional statistics about `h₀` computed **without assuming a harmonic well** (the potential is asymmetric), broadband **and** in the ≥ 1 kHz band, per `C-0010`'s bandwidth treatment;
6. the **cost** of whatever supplies the hold-down: stroke lost (`L₀ − h₀`), per-path force against `C-0006`'s allowables with `C-0014`'s over-stiffening result applied, and normal stiffness added;
7. whether the non-adsorbing premise is safe for PEG against a DNA-origami face — as a **ceiling and a threshold** if it cannot be settled, per `CLAUDE.md`'s research practice and `P-6`'s precedent;
8. all five gates, with gate 3 checking something independent of the construction.

### What is deliberately excluded

- **Any bias at all.** `T-3`/`T-4` own the biased states. `M3` is the zero-*applied*-bias state, which is not the zero-*charge* state, and the difference is treated as a threshold rather than resolved.
- **A 2-D field or the tile edge** — `T-3b`.
- **The lateral coordinate** — `T-12` owns it, and `C-0014`'s answer is unaffected by anything here.
- **Redesigning the coupling.** `C-0017` chose `K2`; this task prices it and says what a preloaded variant would have to be, without proposing one as a sequence design.

---

## Plan

### The cheap bounds run first, and there are three of them

All three cost nothing and two of them decide parts of the answer before any solver starts:

1. **The topology argument above** — decides `M2`'s sign, hence whether the committed coupling can solve `T-13` at all.
2. **The thermal force scale** `k_BT/3 nm = 1.3807 pN`, derived from the exponential distribution above `L₀` — the bar every mechanism is measured against, and it is one division.
3. **Gravity** (`M5`), which is `Δρ V g` and one multiplication: if it lands nine orders below the bar, it never has to be thought about again, and that is worth one line of code to establish rather than one sentence of assertion.

### Then the mechanism-by-mechanism evaluation, and why each method

| mechanism | method chosen | what was rejected, and why |
|---|---|---|
| `M1` tether | `C-0014`'s own `FreelyJointedChain`, evaluated at the extension the geometry imposes | nothing — the element and its parameters are already sourced and tested |
| `M2` coupling | `C-0017`'s own `SeriesEntropicCoupling`, `R(0)` evaluated rather than assumed, plus the exact preload relation `F = (k_c − k_c*)·δ*` | nothing |
| `M3` field | the **existing** `T-3a` pipeline at `V = 0`: nonlinear 2:1 Poisson-Boltzmann in the gap, Stern series on the electrode, `C-0008`'s own tile charge | a fresh field model — `C-0008` already solved this geometry, and re-deriving it would add nothing but a second chance to get the conventions wrong. The *new* work is the **PZC threshold**, which nobody has asked for before |
| `M4` van der Waals | Hamaker/Lifshitz **combining relation** on published constants, with the finite-thickness slab correction, retardation and electrolyte screening of the zero-frequency term all carried as brackets | a full Lifshitz computation from optical data. It needs dielectric spectra for DNA and for an electrode material **§3 does not specify** — so the honest object is a bracket over plausible electrodes, not a number for one |
| `M5` gravity | `Δρ V g` | — |
| `M6` bridging | a **ceiling and a threshold**, per `P-6`'s precedent | a simulation of PEG against a DNA face. The quantity that decides it (the PEG/Mg²⁺/DNA association constant) is the same one `P-8` has already searched for and not found |

### The equilibrium, the stability and the statistics

- `h₀` is a **root** of `U_net`, found by scanning **downward from `L₀`** and bisecting inside the first bracket, on the **bracket width** — never a force divided by a stiffness (`C-0012`), and never a monotone bisection over the whole interval, because `F_down(h)` and `P(h)` both vary and the difference is not guaranteed monotone (`CLAUDE.md`).
- `k₀` is `−dU_net/dh` at `h₀`, assembled from the layer's analytic stiffness and each mechanism's own analytic derivative, and checked against a central difference.
- **the positional statistics are computed by exact Boltzmann quadrature** over `Φ(h) = −∫U_net dh`, not by equipartition, because the potential is linear above `L₀` and steeply nonlinear below `h₀`. Equipartition is then asserted as a **limiting case**: in the stiff limit the quadrature must reproduce `σ² = k_BT/k₀`.
- the bandwidth treatment is `C-0010`'s, unchanged: a Lorentzian per mode, `(2/π)arctan(f/f_c)` below `f`, with the drag from `C-0004`'s permeability and squeeze-out at the **zero-bias** height and volume fraction.

### Why not something more expensive

| | closed-form mechanism budget + the existing field and layer solvers (chosen) | a coarse-grained ensemble of the anchored tile at zero bias | a Lifshitz calculation from optical data |
|---|---|---|---|
| what it gives | every mechanism's sign, magnitude and derivative; the equilibrium as a root; the statistics by quadrature | a sampled `h₀` with a genuine CI | `A(d)` including retardation and screening, for one named material pair |
| cost | seconds | days on 8 cores, **and it has no polymer layer** — `C-0010`'s argument, unchanged | hours to days, **and §3 does not name the electrode material** |
| what it would add | — | noise around closed forms, for a state whose defining question is which *terms* exist | precision on one member of a bracket whose width is set by an unspecified material |

The decisive row is the third: **the largest uncertainty in `M4` is not the physics, it is that §1 says "patterned electrode" and never says of what.** A better calculation of the wrong material is not an improvement, and the right response is a bracket over plausible electrodes plus the threshold at which the answer changes.

### What would falsify this approach — stated in advance

1. **A mechanism with the wrong sign in the code** — e.g. van der Waals coming out repulsive, or the substrate tether pushing the tile up. Both are decided by the topology argument before any arithmetic, so a disagreement is a coding error, not a discovery.
2. **The committed coupling `K2` supplying a non-zero preload.** That would falsify the topology argument, which is the spine of the answer.
3. **No equilibrium at all with every mechanism switched on.** Then the device has no zero-bias resting position, which is a design finding of the first rank and belongs in `ANSWERS.md` as one.
4. **An equilibrium whose stiffness is negative** — i.e. the hold-down beating the layer, so the tile collapses onto the electrode at zero bias. Van der Waals grows as `1/h³` and the layer's pressure grows faster, so this should not happen; if it does, the *unbiased* state is the pull-in state and §1's whole picture inverts.
5. **A hold-down that costs more stroke than §3's 3 nm.** Then holding the tile down and stroking it are incompatible, and `T-2`'s window loses a height.
6. **The exact Boltzmann quadrature disagreeing with equipartition in the harmonic limit.** A numerics error, not a physics one.
7. **The bridging ceiling landing far above the requirement with no way to bound it.** Then the non-adsorbing premise is doing work nobody has checked, and `C-0010`'s exact lateral zero inherits the same exposure.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0003` | the six layer models and `chainLengthForHeight` | **re-run as a library**, not tabulated |
| `C-0011`/`CH-0010` | that `L₀` is threshold-defined, and the 0.1/1/10 pN sensitivity | **cited**, and reproduced here in the task's own currency (`h₀` against `F_down`) |
| `C-0010` | the exact lateral/upward zero, the drag pipeline, the bandwidth treatment, the 3.0 nm bound | the zero is **the premise**; the drag and bandwidth are **re-run** on this task's own state |
| `C-0014` | the FJC, the tether design rule, the allowables, `√(k_BT k)/N` | **re-run as a library**; the 4.6–9.4 pN preload is reproduced as a gate-5 test |
| `C-0017` | `K2`'s geometry and the `placementPreload` relation | **re-run**; `R(0)` is evaluated, not assumed |
| `C-0008` | the tile charge, the Stern capacitance, the zero-bias sign change | the pipeline is **re-run at `V = 0`**; the 3.94/−0.41 pN pair is reproduced as a gate-5 test |
| `C-0005` | the 123–214 % one-loop correction | **cited**, and carried as the ceiling on what any electrostatic number here means |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t13
tools/study.sh anchoring.ZeroBiasRestingPositionStudyKt
tools/verify.sh
```

Code, all in `src/main/kotlin/anchoring/` — nothing outside it created or modified, because `actuator/`,
`brush/` and `structure/` are being worked concurrently:

| file | what is in it |
|---|---|
| `ZeroBiasHoldDown.kt` | the six mechanisms as functions with their derivatives: the thermal force scale, the van der Waals slab pressure with its combining relation and its corrections, the buoyant weight, the bridging ceiling and threshold, the hold-down budget, the equilibrium root and its stiffness, and the exact Boltzmann position statistics |
| `ZeroBiasRestingPositionStudy.kt` | the study entry point, emitting the result JSON |

Result: [`../results/T-13-zero-bias-resting-position.json`](../results/T-13-zero-bias-resting-position.json).

Tests: `src/test/kotlin/anchoring/ZeroBiasHoldDownTest.kt`, each named for the gate it discharges.

---

## Verify

See [`C-0021`](../claims/C-0021-zero-bias-resting-position.md#the-five-verification-gates) for the executed gate table and the falsifier outcomes.
