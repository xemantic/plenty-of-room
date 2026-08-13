# T-23 — A two-sided compliant DNA coupling, or a demonstration that DNA offers none

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.1`/`A1.2` for the positional bound the hold-down is written against, and `A2.2` for the operating point |
| **Problem definition** | §1 (the stack, and the fact that it names nothing holding the tile down); §3 (100 pN, 3 nm, 40 × 40 nm, 5/7/10 nm, 2 mM); §4(f) (the disassembly band, read per `C-0006` as *not* a per-path allowable); §5, §7 (process) |
| **Verification type** | in-silico (signed force-extension laws for five candidate elements, composed into a load line and re-solved against `C-0021`'s own zero-bias balance) **+ logical** (a sidedness argument that fixes the *currency* of the requirement before any element is evaluated) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0023`](../claims/C-0023-two-sided-coupling.md) |
| **Consumes** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) (the hold-down budget, the van der Waals term, the Boltzmann quadrature, the preload relation), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the mandate, `K2`, the stability floor, the secant/tangent split), [`C-0014`](../claims/C-0014-lateral-confinement.md) (the elements, the convexity theorem, `√(k_BT k)/N`), [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (the 3 × 15 grid), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)/`Gen1Tile` (the crossover hinge constant), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the allowables), [`C-0018`](../claims/C-0018-maximum-usable-bias.md) (the pull-in margin), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md)/[`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the layer and the field, as libraries) |
| **Raises** | [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md) against `C-0021` |

---

## Formulate

### The gap this task exists to close

`C-0021` closed `T-13` with an exact relation and an admission.

> **`F_down = (k_c − 33.333)·3 nm`** — every `pN/nm` by which the output coupling exceeds §3's own mandate is exactly 3 pN of downward preload,
> *"and the only thing standing between the programme and that answer is that no two-sided compliant DNA element has been proposed."*

`C-0017`'s committed coupling `K2` puts **99.6 % of its compliance in an ssDNA spacer**, and a single strand
carries no compression, so `K2`'s reaction at zero stroke is **exactly zero** and it supplies no hold-down of
either sign. What blocks the design is therefore **topology, not stiffness**.

**This task asks whether DNA offers a compliant element that carries load in both directions, and prices the
answer against the four requirements a coupling now has to meet at once: placement, stability, hold-down and
lateral confinement.**

### The question, as a numeric target

An element, or a set of elements, each delivered with:

1. its **signed** force-extension law `R(δ)` over `δ ∈ [−1, +3] nm`, `R(δ) < 0` for `δ < 0` being the whole
   definition of two-sidedness;
2. its **secant** at §3's 3 nm working point (which must equal the mandate, `33.333 pN/nm`, over `n` paths)
   and its **tangent** there (which must clear `C-0017`'s stability floor and stay **≤ 40 pN/nm**);
3. its **per-load-path peak force** against `C-0006`'s allowables — 10 pN unzip, 48 pN duplex shear, 65 pN
   nicked ceiling — with `C-0014`'s over-stiffening result `√(k_BT k)/N` applied;
4. its **DNA sequence-level parameters**: contour length in nucleotides, span or arm in nm and in base pairs,
   Kuhn length quoted on the **method-systematic bracket** (1.34–1.41 nm from 10–40 pN force spectroscopy
   against 2.10–2.84 nm from zero-force scattering — a ~1 pN element needs the zero-force end);
5. the **zero-bias verdict** it produces when substituted into `C-0021`'s own balance: resting height, well
   depth, confining or not, positional RMS broadband and in band, and delivered stroke.

### The acceptance predicate, declared before any code runs

An element **passes** when all five hold, at all three §3 heights and over all six `C-0003` layer models:

| | predicate | source of the number |
|---|---|---|
| **P1** | **two-sided**: `R(−δ) < 0` for every `δ > 0`, evaluated rather than asserted | the topology argument |
| **P2** | **placed and compliant**: `n·R(3 nm) = 100 pN` exactly, and tangent at that point **≤ 40 pN/nm** while exceeding `C-0017`'s stability floor | §3, and this task's declared ceiling |
| **P3** | **safe**: per-path peak force below 10 pN unzip, and below 65 pN under every reading | `C-0006` |
| **P4** | **confining with no tether and no preload**: `C-0021`'s well ≥ 10 `k_BT` and broadband RMS ≤ 3.0 nm | `C-0021`, leaf `A1.1` |
| **P5** | **buildable**: every geometric parameter inside the envelope `C-0017` already budgeted (lever spans ≤ 60 nm, standoffs ≤ 10 nm) and quoted in base pairs or nucleotides | `C-0017` |

### The cheap bound that must run first, because it changes the *currency* of the answer

`C-0021` derives the hold-down requirement as a **force**, `F ≥ k_BT/3 nm = 1.3806 pN`, and the derivation is
explicit about why: above `L₀` the layer contributes nothing, so a constant hold-down confines the tile through
a **linear** potential and the upward excursion is exponentially distributed, `⟨h − L₀⟩ = k_BT/F`.

**That derivation is a property of a one-sided stack, not of the problem.** A two-sided coupling of stiffness
`k` above `L₀` gives a **quadratic** potential there, so the excursion is Gaussian and the requirement is

&nbsp;&nbsp;&nbsp;&nbsp;`k ≥ k_BT/σ² = 0.460216 pN/nm` — leaf `A1.1`'s own bound, and **exactly `1/σ` of the force requirement.**

So before any element is evaluated:

> **Two-sidedness is worth exactly one power of the position bound.** `F_req = k_req·σ` identically, and for
> σ = 3.0 nm the same coupling that is *4 % short* of the force requirement is *72× past* the stiffness one.

If that holds, then a two-sided element **at the mandate**, unpreloaded, discharges `T-13` — and the preload
relation `F_down = (k_c − k_c*)δ*`, which `C-0021` reports as the price of the hold-down, becomes a *choice*
rather than a requirement. This is the cheap bound and it is one division; it runs before any geometry.

### Units, locked

Lengths in **nm**, forces in **pN**, stiffness in **pN/nm** (= mN/m), moments and energies in **pN·nm** and
`k_BT`, pressure in **pN/nm²** (= 1 MPa exactly), bending rigidity in **pN·nm²**, torsional stiffness in
**pN·nm/rad**, frequencies in Hz, potentials in V. `k_BT = 4.141947 pN·nm` at 300 K, aqueous **2 mM MgCl₂**.

### Geometry and sign conventions, fixed before deriving

Inherited unchanged from `T-12`, `T-16` and `T-13`, with one addition this task needs:

- `z` normal to the electrode, positive **away** from it; the electrode surface is `z = 0`.
- the **stroke** `s = L₀ − h` is positive **downward**; the **electrostatic gap is the layer height, exactly**.
- the **coupling reaction `R` is positive upward**, i.e. resisting descent (`C-0017`).
- **NEW — the element displacement `δ` is signed and is the tile's displacement from the element's own
  unstressed configuration**, positive downward. A **one-sided** element has `R(δ) = 0` for all `δ ≤ 0`;
  a **two-sided** element has `R(−δ) = −R(δ)` for a symmetric element and, in general, `R(δ) < 0` for `δ < 0`.
  **Two-sidedness is a property of the element's law at negative argument and of nothing else**, so it is
  tested by evaluating there, never by inspecting the geometry.
- a **hold-down** is any mechanism contributing to `F_down > 0` (`C-0021`), and `R(0) = −F_down`.

### The five candidate elements, declared in advance

Every one is evaluated and reported, including the two that are known to fail — §7 rewards saying which were
checked.

| id | element | expected sidedness | why it is in the list |
|---|---|---|---|
| **`E1`** | **axial duplex standoff** — a hybridised duplex loaded along its axis | **two-sided** (it buckles, but only above its Euler load) | the null hypothesis: DNA's stiffest element *is* two-sided. `C-0017`'s `K1` |
| **`E2`** | **ssDNA spacer** — the element that closed `T-16` | **one-sided** | `C-0017`'s `K2`, and `C-0021`'s exact zero. The control |
| **`E3`** | **transverse duplex flexure** — a duplex spanning between two lever posts, tied to the tile at midspan | **two-sided** | bending is signed; the compliance is `EI/L³` and the span is a free design variable |
| **`E4`** | **antagonistic ssDNA pair** — an up-spacer and a down-tether pre-tensioned against each other | **two-sided as a pair** | neither part is; the pair's stiffnesses **add** while its preload is their **difference** |
| **`E5`** | **crossover-hinge flexure** — an antiparallel crossover as a torsional spring on a short arm | **two-sided** | the sheet's own motif, and the only crossover elastic constant anyone has ever fitted |

### The sidedness argument, which runs before any number

> **An element loaded along its own axis must choose.** A duplex is 220 pN/nm at 5 nm — two-sided and 297×
> too stiff (`C-0017`'s `K1`). A single strand is compliant and carries no compression. There is nothing in
> between **on that axis**, because axial compliance in DNA is entropic and entropy only pulls.
>
> **An element loaded transverse to its axis, or through a hinge, does not have to choose**, because its
> compliance is *bending*, and a bending moment is signed. `EI/L³` and `k_θ/r²` are as small as the designer
> makes `L` and `r`.

**The prediction this makes, written down before the code runs: `E3` and `E5` are two-sided at any stiffness,
and the design problem for them is not sidedness but geometry — the span or arm that lands on 33.333 pN/nm
over 45 paths.** It is recorded here so that finding it is a confirmation rather than a discovery after the fact.

### What "an answer to `T-23`" has to deliver, in full

Discharged when all seven hold:

1. the currency bound above, derived and asserted as a test, with the exact identity `F_req = k_req σ`;
2. all five elements given a **signed** law, a secant, a tangent, a per-path force and a sidedness verdict;
3. for every element that passes `P1`–`P3`, the **design parameter** solved as a root (span, arm, contour) at
   45, 15 and 8 paths, with both end conditions and both readings of axial restraint carried as brackets;
4. the **zero-bias balance re-solved** with the surviving element in place of `K2`, tether-free and
   preload-free, against `C-0021`'s own scenarios, and `C-0021`'s device row reproduced as a cross-check;
5. the **preload branch priced**: what a mounting offset buys and costs, in stroke, in per-path force and in
   `C-0018`'s pull-in margin, with the offset quoted **in base pairs** because that is the quantum a design has;
6. the **null option costed honestly** — stay one-sided, keep `C-0014`'s eight tethers;
7. all five gates, with gate 3 checking something that is not a restatement of the construction.

### What is deliberately excluded

- **Any redesign of the lever.** `C-0017` budgets it as a section requirement; this task budgets the element
  between the tile and it, and assumes the lever's far end **laterally and vertically grounded**, exactly as
  `C-0017` does.
- **A finite-element model of the flexure.** Euler-Bernoulli with a stated end condition and an explicit
  membrane term is the model; a shell model of a duplex is outside this programme.
- **The biased states.** `T-3`/`T-4` own them. The pull-in margin is read on `C-0018`'s own axis and is
  reported as a **sensitivity**, not as a new ceiling.
- **`T-9`.** `E5`'s hinge constant is `C-0009`'s cited fit with its own `α ∈ [0.6, 1.2]` bracket, and the
  design parameter is reported *as a function of it* — because `r ∝ √k_θ`, a 2× uncertainty is 1.41× in a
  length the designer chooses anyway.

---

## Plan

### The cheap bounds first, and there are three

1. **The currency bound** — one division, and it decides whether the answer is "a stiffer coupling" or
   "any two-sided coupling". It runs before any element.
2. **The sidedness argument** — decides `E1`, `E2` before arithmetic, and predicts `E3`, `E5`.
3. **The span and arm estimates in closed form** — `L = (c EI n/k_c*)^(1/3)` and `r = √(k_θ n/k_c*)`, two
   evaluations, which say immediately whether the geometry is inside `C-0017`'s envelope or hopeless. Only if
   they are inside is a root find with the series chain and the membrane term worth running.

### Then the element-by-element evaluation, and why each method

| element | method chosen | what was rejected, and why |
|---|---|---|
| `E1` | `C-0014`'s `rodAxialStiffness` and `eulerBucklingLoad`, unchanged | nothing — the element is already sourced and tested |
| `E2` | `C-0014`'s `FreelyJointedChain` through `C-0017`'s `SeriesEntropicCoupling`, `R(−δ)` evaluated | nothing |
| `E3` | Euler-Bernoulli midspan stiffness `c EI/L³` with **both** end conditions (48 and 192, a factor of exactly 4 apart, as `C-0014` carries its own pair), **plus** the membrane (cable) term built from `C-0014`'s own `cableTension`, carried as a bracket over axial restraint | a large-deflection shell or finite-element beam. The membrane term *is* the large-deflection correction and it is closed-form; the deflection is 3 nm on a 25–55 nm span, i.e. 5–12 %, where the two-term expansion is the standard model |
| `E4` | two `FreelyJointedChain`s in opposition, one grounded on the substrate at the layer height and one on the lever at the stroke — `C-0021`'s own two topologies, summed | nothing |
| `E5` | `Gen1Tile.crossoverHingeStiffness(α)` on a bending arm, in series | inventing a hinge. The constant is cited and fitted, and its `α` bracket is carried |

### The zero-bias re-solve

`C-0021`'s balance is re-run **as a library, not tabulated**: the same layer models, the same van der Waals
assembly (gold and alumina, 2 nm tile, the retarded fully-screened low end), the same residual field, the same
`zeroBiasRestingHeight` root and the same `boltzmannPositionStatistics` quadrature. Only the coupling changes.
`C-0021`'s *"device with the tether removed"* row (1.40–5.37 `k_BT`, 0/18 confining) is reproduced as a gate-5
test, because it is the row this task is trying to beat and reproducing it is what makes the comparison mean
anything.

### Why not something more expensive

| | closed-form signed element laws + `C-0021`'s balance re-run (chosen) | a finite-element beam model of each flexure | a coarse-grained (oxDNA) simulation of the flexure |
|---|---|---|---|
| what it gives | every element's sidedness, secant, tangent, per-path force and geometry; the confinement verdict | the end-condition factor `c` as one number instead of a 4× bracket | a force-extension law with no fitted constants |
| cost | seconds | hours | days, and it is `T-9`'s cost for a second time |
| what it would add | — | precision on a bracket the **designer chooses** by how the ends are built, not a quantity to be measured | the one thing that would settle `E5`'s `k_θ`, which is `T-9`'s job and is already queued |

The decisive row is the second: **the end condition of an origami joint is a design choice, not a measurement**,
so collapsing its bracket by simulation would be answering a question the designer answers with a staple.

### What would falsify this approach — stated in advance

1. **No element with a signed reaction below 40 pN/nm.** Then the honest close is *"DNA offers none"*, with
   the missing element named — an acceptable close under `SESSION-PROMPT.md`.
2. **A required span or arm outside the buildable envelope** — a flexure needing 200 nm, or an arm shorter
   than a base pair. Geometry, not physics, would close the branch.
3. **A per-path force above an allowable at the required stiffness**, which is how `C-0017`'s `K5`/`K6` died.
4. **The two-sided element failing to change the confinement verdict** — a finite well in the quadrature would
   falsify the currency argument, which is the spine of this task.
5. **The membrane term dominating at every span**, which would make the flexure a cable rather than a spring
   and put it back under `C-0014`'s `L_min ∝ δ` footprint constraint.
6. **The preload needed being far below the base-pair quantum**, so that a design cannot *set* it. This is
   expected to fire, and if it does it is a result, not a failure: it argues for zero preload.
7. **The pull-in margin gain being negligible**, i.e. the coupling being the wrong lever for `C-0018`'s
   1.007–1.032. Also expected to fire, and also a result.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0021` | the thermal force scale, the van der Waals assembly, the quadrature, the preload relation, the *device with the tether removed* row | **re-run as a library**; the row is reproduced as a gate-5 test |
| `C-0017` | the mandate, `K2`, the stability floor at 10 nm / 2 mM, the secant/tangent theorem | mandate and `K2` **re-run**; the floor **cited** and reproduced from the field |
| `C-0014` | `FreelyJointedChain`, `cableTension`, `eulerBucklingLoad`, `√(k_BT k)/N`, the Kuhn bracket | **re-run as a library** |
| `C-0015` | 45 attachments as 3 × 15 | **cited** as the path count |
| `C-0009`/`Gen1Tile` | `k_θ = 2αB/(100a)` with `α ∈ [0.6, 1.2]` | **cited, fitted**, and swept |
| `C-0006` | 10 / 48 / 65 pN | **cited** |
| `C-0018` | the 1.007–1.032 bias margin and the 1.19–1.42 stiffness margin | **cited**, and used to grade this task's own derived elasticity `d ln|k_eff|/d ln V` |
| `C-0003`, `C-0008` | the layer and the field | **re-run as libraries** |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t23
tools/study.sh anchoring.TwoSidedCouplingStudyKt
tools/verify.sh
```

Code, all in `src/main/kotlin/anchoring/` — nothing outside it created or modified, because `brush/`,
`electrostatics/` and `structure/` are being worked concurrently:

| file | what is in it |
|---|---|
| `TwoSidedCoupling.kt` | the signed element laws — the transverse flexure with its membrane term and its end draw-in, the hinge flexure, the antagonistic pair — the design root finds, the sidedness probe, the mounting-offset quantum, and the two readings of the positional requirement |
| `TwoSidedCouplingStudy.kt` | the study entry point, emitting the result JSON |

Result: [`../results/T-23-two-sided-coupling.json`](../results/T-23-two-sided-coupling.json).

Tests: `src/test/kotlin/anchoring/TwoSidedCouplingTest.kt`, each named for the gate it discharges.

---

## Verify

See [`C-0023`](../claims/C-0023-two-sided-coupling.md#the-five-verification-gates) for the executed gate table
and the falsifier outcomes.
