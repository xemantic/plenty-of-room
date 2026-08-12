# JOURNAL

Chronological record of the project:
every interaction with Kazik, every key decision and why it was made, and the progress of the loop.
Newest iteration last.

Conventions: dates are absolute. Claims are referenced by ID. Anything not yet done is named as such.

---

## 2026-08-12 — Iteration 1

### Interaction with Kazik

**Kazik, opening instruction (paraphrased in full, this is the founding brief):**

> The purpose of this project is to fulfil `third-party/2026-08-ndi-gen1-problem-definition.md`.
> It is up to you how to structure this repository to provide the result.
> Running as root on this VPS — install any software without asking permission.
> If an experiment needs more CPU/GPU/MEM than the box has, **stop the loop and ask for assistance.**
> `git@github.com:NanoDynamicsInst/simulation-task-map.git` is cloned at `../simulation-task-map` for inspection.
> On each concluded iteration, commit and push immediately.
> Maintain a journal tracking every interaction, key decisions, and progress.
> Any software created on behalf of the project stays in the project, for re-use and inspection.
> **Use TDD — always write tests before the implementation.**
> Maintain a task queue, and **prioritise process blockers over low-hanging fruit** (override the default ROI orientation).
> The project offers TDD and numeric support in Kotlin; multiple `main` entry points are fine.
> Not bound to any particular technology — use the best tool for each problem.
> Start by creating `SESSION-PROMPT.md`, which can later be started with
> `/loop read @SESSION-PROMPT.md and follow the instructions in it`.

No questions were put back to Kazik this iteration; nothing was blocking.

### Decisions

**D-1. The repository is organised around the GPD loop, not around the code.**
`gpd/tasks/` (Formulate + Plan), `gpd/results/` (machine-readable), `gpd/claims/` (verified, with provenance),
`gpd/challenges/` (contradiction raises a challenge, never an overwrite). The Kotlin sources are an
implementation detail underneath that. Rationale: NDI's §5 and §7 are explicit that the *process* is what is
being evaluated, so the process has to be the visible structure of the repository, not a thing inferred from it.

**D-2. Task IDs are ours (`T-*`, `P-*`) but every one carries the NDI leaf ID it traces to.**
`../simulation-task-map/AGENTS.md` requires citing leaf IDs rather than prose paraphrases. Our own `P-*`
process tasks have no leaf and say so.

**D-3. `kotlinx-serialization` added rather than hand-rolling JSON.**
Every task has to emit machine-readable results, so the emitter is used eight or more times.
Consistent with the CLAUDE.md standing warning against hand-rolled implementations of things libraries do.
Verified to resolve before any code depended on it.

**D-4. A generic `study` Gradle task instead of competing for the single `application` main class.**
`./gradlew study -Pstudy=brush.BrushStiffnessStudyKt`. Each GPD task adds its own entry point.

**D-5. Results are emitted to a fixed filename, with no timestamp.**
A re-run that changes nothing produces no git diff; a re-run that changes something produces a reviewable one.
This is worth more than run-history in the filename, which git already provides.

**D-6. Task 8 (tile positional variance) promoted above its numbering.**
It is nearly free once T-1 exists, and it is a *falsifier*: a layer compliant enough to actuate may be too
compliant to hold position at 300 K. Cheap falsifiers before expensive confirmations.

**D-7. `P-3` (PEG material parameter sheet) and `P-4` (volume-fraction bookkeeping) raised as process
blockers, not as follow-ups.** §2 of the problem definition says outright that where the semidilute crossover
sits *decides which exponent we are entitled to*. T-1 discharges this by carrying all three exponents and
reporting the spread, which is honest but is not the same as answering it.

**D-8. The de Gennes wall mapping was derived rather than looked up.**
§2 names the asymmetric geometry as a source of prefactor confusion. Resolved by the mirror-plane argument:
an impenetrable wall imposes the same boundary condition as the midplane between two non-interpenetrating
brushes, so `D → 2h` and the factor of two cancels from both ratios. The circulating error is keeping the 2
while reinterpreting `D`, which understates the pressure by `2^(9/4) ≈ 4.76`.

**D-9. The MWC form was derived rather than cited, because the cited version could not be trusted.**
§2 names MWC as a third circulating form that "does not reduce to the same thing". Rather than reproduce a
half-remembered closed form, it was rebuilt from the parabolic self-consistent potential plus the mean-field
contact-value theorem `P = ½ w k_BT n(h)²`. This turned out to be the right call — see below.

### Progress

- `P-1` (loop skeleton) — **done**. `SESSION-PROMPT.md`, `JOURNAL.md`, `TASKS.md`, `gpd/`.
- `P-2` (locked units + result emitter) — **done**. `Physics.kt`, and the `StudyResult` envelope that logs
  every parameter alongside units, conventions and validity.
- `T-1` (stiffness of the polymer layer, leaf `A2.1`) — **done, verified, filed as `C-0001`**.
- 75 tests, all green, written before their implementations. All five verification gates are executed
  as tests and named for the gate they discharge.

### What was surprising

**S-1. The SCF brush has *zero* stiffness at first contact.**
The scaling form's pressure vanishes linearly at `L₀`, the SCF form's vanishes quadratically, because the
SCF brush has a diffuse outer edge. Consequence: **"the stiffness of the polymer layer" is not a well-posed
single number at the resting height** — it is only well-posed at a stated compression. This was not
anticipated and it changes how every downstream task has to quote a stiffness. It is also a concrete
instance of exactly what §2 warned about, arrived at independently.

**S-2. The de Gennes and MWC heights agree at `w = π²a³/4`, independent of `N` and `σ`.**
That the calibration is a pure number was not expected, and it is what makes the two forms comparable:
with it, any residual difference in the compression curves is functional form rather than prefactor.

**S-3. The equilibrium stiffness scales as `σ^(7/6)`, not `σ^(3/2)`.**
`k/A = 3k_BT σ^(3/2)/L₀` looks like a 3/2 law until one notices `L₀` carries `σ^(1/3)` too.
Easy to get wrong, so it is pinned by a test.

**S-4. The mechanical window is empty at 5 nm and 7 nm.**
The headline result, and sharper than expected: at the §3 target force, the brush-regime window with ≥ 3 nm
stroke is empty at two of the three specified layer heights, and narrow at the third. The ~10 nm desired
stroke is unreachable anywhere in the brush regime. All four models agree in direction; they disagree by
about 1.5× in magnitude. §4(b) asked whether there is a reason to go outside 5–10 nm — the answer, from the
mechanics alone, is yes, and the direction is **thicker**.

**S-5. The semidilute premise survives contact with our own layer.**
Working volume fraction across the surviving window is φ ≈ 0.03–0.044, roughly five times below the
conventional semidilute→concentrated crossover. So the `9/4` exponent is one we *are* entitled to here —
which is a checked premise rather than an inherited one, and it is the first of §2's caveats to actually close.

### Next

`T-2` — the feasible design window. T-1 hands it a sharp starting point rather than a blank sweep:
a single narrow band at `L₀ = 10 nm`, already bounded by two constraints, before §4(c) and §4(d) touch it.
`P-3` runs before or alongside, because it is now the binding premise under both.

---

## 2026-08-12 — Iteration 2

Task `P-3` — the PEG material parameter sheet. Taken because `TASKS.md` had it at the top of the queue
as the binding premise under `C-0001`, and because process blockers outrank cheap wins.

### Interaction with Kazik

One instruction, at the start of the session: *"you are no longer running as root, but as a user with
sudoers and no password, so that you can install any software this way."* No change to the work followed
from it beyond installing `poppler-utils` to read a downloaded preprint. No questions were put back;
nothing was blocking.

### Decisions

**D-10. The expensive method was rejected on the merits, not on cost.**
`P-3` needs PEG's excluded volume and osmotic law. The obvious "serious" route is MD or SCF. It was
rejected because osmometry on the actual polymer in the actual solvent over the actual concentration
range **already exists**, and a simulation would be *less* trustworthy, not merely more expensive.
§5 asks that method choice be justified against cost; here the cheap method is also the better one,
and saying so is the justification.

**D-11. The literature was read, not recalled.**
The two load-bearing papers were downloaded and read in full rather than cited from memory. This
immediately paid: a summary of the first one reported the des Cloizeaux onset as `φ# ≈ 0.04` and `0.025`,
and the actual text says **0.15** and **0.07–0.09** — the summary had picked up the *overlap*
concentrations instead. Had the number been taken second-hand, this iteration would have concluded the
exact opposite of what it concluded.

**D-12. The three quantities the literature writes as `a` are now three named properties, and none of
them is called `a`.** `effectiveMonomerLength` (0.35 nm, a contour length), `volumetricMonomerSize`
(0.392 nm), `kuhnLength` (1.1 nm). Their cubes differ by a factor of 39. This is enforced by the type,
not by a comment, because `C-0001` had already made the substitution error once without noticing.

**D-13. The equation of state is carried whole rather than reduced to an exponent.**
`ScalingEquationOfState` implements both limbs and exposes `localExponent(φ)`. The alternative — picking
the exponent that best matches our operating point — would have thrown away exactly the information
that the operating point is *inside* the crossover, where no exponent is correct.

**D-14. `CH-0001` was filed rather than editing `C-0001`.**
The result contradicts a standing claim, so §5 says challenge, not overwrite. `C-0001` keeps its numbers
and its PASS verdict; what changed is its validity range, and the challenge says exactly which bullet
fails and why. The claim file carries a banner and struck-through text pointing at the challenge.

### Progress

- `P-3` — **done, verified, filed as `C-0002`**, raising `CH-0001`.
- `P-4` (volume-fraction bookkeeping) — **done as a consequence**: the crossover is located, measured,
  for our chain length. It is not where it was assumed to be.
- `P-5` (brush-regime criterion) — **resolved in substance**: `Σ = 5` ⇔ `φ = 1.085 φ#` exactly, for any
  PEG layer of any thickness. The convention is a real material statement; it is just not a sufficient one.
- 119 tests, all green, written before their implementations (75 → 119).
- New entry point `material.PegMaterialStudyKt`.

### What was surprising

**S-6. The layer is not in the semidilute regime, and the criterion that said it was is known — in print,
for this exact material — to be insufficient.** Hansen et al. measure the des Cloizeaux onset for PEG in
water at `φ# ≈ 0.15` (PEG-2000) and `0.07–0.09` (PEG-5000), against overlap concentrations of 0.05 and
0.02, and write that coil overlap "does not provide a sufficient criterion". Our layer sits at
`φ/φ# = 1.08–1.23`. The premise `C-0001` recorded as the *first of §2's caveats to actually close* is
the one that turned out to be open.

**S-7. At fixed reduced grafting density, `φ/φ#` is independent of layer height and chain length.**
Not noticed until the four design points came back at 1.09, 1.11, 1.13, 1.23 — which looked like a bug.
It is an identity: `Σ = π L₀^(6/5) σ^(3/5)` with the monomer length cancelling, `φ ∝ σ^(2/3)`,
`φ# ∝ σ^(4/15)`. So `Σ = 5` always means `φ = 1.085 φ#` for PEG in water. A convention we had flagged as
arbitrary turned out to be a precise material statement — of something other than what it was being used to say.

**S-8. Every correction found this iteration makes the layer softer, and they compound.**
Exponent 9/4 → 1.67; prefactor 1 → 0.751; excluded volume 4.3× down. `C-0001`'s window is therefore a
*lower* bound on its own width, and its "empty at 5 and 7 nm" headline may not survive. That was not the
expected direction — a premise check that finds the premise violated usually shrinks the answer.

**S-9. The 30 pN chain-tension premise cannot be violated by grafting density at all.**
The intrinsic brush tension is `3k_BT n_K a^(5/3) σ^(1/3)/b²` — independent of chain length, cube root in
grafting density. Melt-like `σ = 1 nm⁻²` gives 5.55 pN. §2 suggested this was "within a factor of two"
of binding; it is a factor of seven away and structurally cannot get closer.

**S-10. A compliant PEG brush and a semidilute PEG brush may be mutually exclusive.**
Reaching `φ = 5φ#` needs `σ = 0.99 nm⁻²` at 10 nm and `3.96 nm⁻²` at 5 nm — the latter puts chains closer
together than one Kuhn diameter, so it is not realisable at all. The densities that would make the brush
theory valid are the ones §4(a) rules out for being far too stiff. This is a `T-2` input.

### Next

`T-1c` — re-derive the layer response with a crossover-valid free energy. `CH-0001` shows this cannot be
repaired by swapping an exponent: the Alexander-de Gennes *height* relation is itself a consequence of
semidilute blob structure, and `T-1` inverts it to get the chain length, so `N` rests on the same premise.
`T-2` must not run on `C-0001`'s window until then, except as a stated lower bound.

---

## 2026-08-12 — Iteration 3

The first iteration run **in parallel**: four GPD loops against one working tree, coordinated from a single
context window, each owning a disjoint Kotlin package and a disjoint block of claim and challenge IDs.
`TASKS.md`, `JOURNAL.md`, `CLAUDE.md`, `README.md`, `build.gradle.kts` and every `git` write stayed with the
coordinator, so the loop keeps one coherent history rather than four interleaved ones.
This section is written per task as each closes, newest last.

### Interaction with Kazik

One instruction, at the start of the session: run the loop from the main context window, spawn subagents
for queued items, extend and maintain the queue, and run independent tasks in parallel if they fit the box.
No questions were put back; nothing was blocking. Nothing has yet needed more compute than this box provides.

### `P-7` — build isolation, taken as a process blocker mid-iteration

**D-15. A harness failure that looks like a test failure is a process blocker, and outranks the science.**
Four agents sharing one checkout could not get an authoritative `./gradlew test`: Gradle's results writer
races between concurrent runs and fails with `EOFException` or
`NoSuchFileException: build/test-results/test/binary/in-progress-results-generic.bin`, which reads as a
broken test rather than a broken harness. `build.gradle.kts` now takes `-PbuildDirectory=<dir>` and each
concurrent run gets its own build directory. The alternative — the workaround one agent found for itself,
copying the tree into a scratch directory to get a clean run — would have left every *other* agent
mis-diagnosing its own red build.

**D-16. The toolchain gap was filled before it was needed, not after.**
The box had no `g++`, `make` or `cmake` at all, and no numpy/scipy. Leaf `A1.2` names an oxDNA/Martini
ensemble for `T-8`, which would have required a from-source build; discovering that at the point of need
would have stalled a science task behind an install.

### `T-7` — poroelastic drainage (leaf: none; the question is §4(d))

**Done, verified, filed as `C-0004`**, raising `CH-0003`. §4(d) is discharged as a non-constraint:
91 kHz corner frequency at the nominal design point, 22.6 kHz at the §3 worst case (70 × 100 nm tile on a
10 nm layer), and **5.6 kHz under a composite worst case** that stacks the largest tile, the thickest layer,
the least permeable of three models and a stiffness four times below `C-0001`'s — against a 1 kHz requirement.

NDI asked for this one to be *"done properly rather than waved away"*, with the conditions that would make
it binding. Those are now numbers: it binds at a 437 nm tile edge (10.9× Gen-1, 4.4× the longest §3 test
tile), or at φ ≤ 0.0022 (a mushroom carpet that fails §4(a) first), or at 116× less permeability. The
honest form of the answer is that **the design would have to leave the poroelastic model's own domain of
validity before poroelasticity could become the binding constraint.**

#### Decisions

**D-17. The bound is quoted from the least permeable of three models rather than from a chosen one.**
Three published constructions of the same layer disagree by 40× in permeability. Choosing one would have
made the verdict rest on the choice; quoting the slow end makes only the *margin* rest on it.

**D-18. Brinkman transmissivity everywhere, never plain Darcy.**
`T = kh[1 − (2√k/h)tanh(h/2√k)]` contains the free-film Reynolds squeeze-film limit the layer degrades to
when `√k ~ h` — which is exactly what the measurement-anchored permeability gives. Plain Darcy would have
overstated drainage by 5× there. Poroelastic drainage and lubrication squeeze film are the same expression
at two ends; they are not additive channels.

**D-19. A paywalled primary source demotes the number rather than licensing the secondary quotation.**
Jackson & James (1986) could not be obtained, so its permeability correlation is flagged unverified *in the
code*, not merely in prose, and used only as a cross-check against an independently derived free-draining
bound. The two agree to 1.3×, and nothing in the claim changes if the cited constant is wrong.

**D-20. The result is parameterised by the layer stiffness, not by `C-0001`'s number**, because `T-1c` is
re-deriving it concurrently. Every time is exactly `∝ 1/k_layer`, so the claim survives its own input moving.

**D-21. FE Biot and explicit-solvent MD were declined on the `P-3` precedent** — they would compute a
precise consequence of an imprecise premise, and the margin is 22×.

#### What was surprising

**S-11. The layer thickness cancels out of the drainage time.** Squeeze-out under a tile is a *footprint*
problem: `τ = ηG/(kMf)`, and `h` survives only inside the Brinkman wall correction. The sign inverts, too —
the **thin** layer is the slow one, because a thin channel screens its own flow against its walls. A
vertically drained layer would have been four times faster at 5 nm than at 10 nm.

**S-12. A denser layer drains faster.** `k` falls as `φ^(−1)` but the modulus rises as `φ^(9/4)`, and the
modulus wins. "Poroelasticity gets worse as you compress" is backwards for this system, which is why the
binding direction is dilution.

**S-13. Lateral and vertical drainage are nearly tied at the Gen-1 tile** — 7.50 nm against 6.37 nm, with a
closed-form crossover at `L = 3.396 h` and the tile sitting 18 % past it. "The water goes sideways" was 18 %
from being wrong. Consequence: a hydraulically open origami tile would buy ≤ 1.4×, so tile permeability is
not a design lever.

**S-14. The `Σ = 5` brush convention has an exact geometric twin of `CH-0001`'s thermodynamic finding.**
`L₀/s = (Σ/π)^(5/6)` identically, so `Σ = 5` buys **1.473 blobs** — for every polymer, chain length and
thickness — and a ten-blob stack needs `Σ ≈ 50`. Found by asking a hydrodynamics question, not a mechanics
one. Filed as `CH-0003`, and it lands directly on `T-1c`: strong-stretching theory is outside its own
premise here too, at `L₀/R_F = 1.17–1.25`.

**S-15. The declared falsifier fired, and it fired where it did not change the answer.** The Darcy premise
fails on the measurement-anchored permeability (`√k/h = 0.56–0.58`; the layer is under two screening lengths
thick). The 1 kHz contour lies *entirely* inside the region where the premise has already failed — so the
boundary is reported as *where this model would say it binds*, not as a prediction that it does.

**S-16. Sourcing a prefactor produced a disagreement rather than a number**, and the disagreement is
structural: at `φ/φ# ≈ 1.1` the correlation blob is two thirds of the coil, so "segment scale" and "blob
scale" are not separated scales for this layer. The honest deliverable was a bracket, not a value.

### `T-5` + `T-5b` — load distribution and tile flatness (leaves `A1.2`, `A8.2`)

Taken as one iteration because they share one structural model.
**Done, verified, filed as `C-0006`**, raising `CH-0005` against `C-0001`.
**The rigid-plate assumption is rejected.**

The reason it is rejected is sharper than the numbers. A uniform load on a uniform Winkler foundation
produces **no dishing at all, exactly, at any flexural rigidity** — `w = q/k_f` has zero fourth derivative
and satisfies the free-edge conditions identically. So `C-0001` got the right answer for the wrong reason:
not because the tile is stiff, but because a uniform load needs no stiffness. Every departure from
uniformity dishes it: 27 % of the stroke for an electrostatic edge taper, 50 % for four discrete anchors,
369 % for a single lever attachment — and 26 % for the unavoidable one, thermal excitation at 300 K.

#### Decisions

**D-22. Rayleigh-Ritz on a tensor Legendre basis, not finite differences or FEM.**
Free edges are *natural* in an energy method and awkward in a differenced biharmonic, and the basis buys
three things used directly: the rigid-body modes are exactly the first three basis functions, so dishing
needs no plane-fitting; the basis is area-orthogonal, so the mean deflection is one coefficient; and the
coefficient covariance under equipartition is `k_BT K⁻¹`, so thermal dishing falls out of the same matrix
as the loaded solve, exactly, with no sampling.

**D-23. `D_⊥` derived from crossover hinge compliance and then swept over an order of magnitude**, because
`k_θ` is the single largest open premise and no accessible measurement of it exists. The nominal value
deliberately excludes the series duplex-twist term so that it is an explicit *upper* bound.

**D-24. Every conclusion is stated as a function of `k_f`**, carrying `C-0001`'s three distinct stiffnesses
(at rest, secant, at the working point) separately rather than choosing one — because `C-0001`'s own gate 2
had already established that "the layer stiffness" is not well posed at a single compression.

**D-25. The expensive calculation was declined and costed instead.** An oxDNA run is the only route to
`k_θ` from first principles; it is queued as `T-9` with an estimate (2–5 k nucleotides, µs-scale umbrella
sampling, days on 8 cores) rather than started inside the iteration.

#### What was surprising

**S-17. A uniform load produces no dishing at all — exactly, at any rigidity.** This is not a limiting case,
it is the leading-order answer, and it collapses the whole task to *naming and bounding the departures from
uniformity*. It also means the rigid-tile assumption is harmless exactly where `C-0001` uses it and wrong
everywhere else.

**S-18. The cheap analytic bound was wrong by 50×, at a free edge, and the solver caught it.** The interior
ripple transfer function `1/(1+(2πℓ/λ)⁴)` predicts an edge perturbation is attenuated to 0.010; the
finite-plate solve gives 0.53. The transfer function is correct and was verified — it simply does not apply
at a boundary, because a free edge has no material beyond it to bend against. This is the only result in the
iteration the closed forms could not have produced, and it is the entire cost justification for the solver.
The project's standing rule is to run the cheap bound first; this is the case where the cheap bound was
*also* wrong, and the discipline of running both is what exposed it.

**S-19. The tile's thermal *shape* fluctuation exceeds its rigid-body *position* fluctuation** — 1.27 nm
against 0.75 nm, a ratio that *grows* as the foundation stiffens, because dishing modes gain `D q⁴` and the
piston mode gains nothing. Leaf `A8.2` asks for "no floppy modes in the workspace"; the floppy modes are the
shape modes.

**S-20. A rigid anchor cannot collect more than `8 q ℓ_∥ ℓ_⊥` = 18.3 pN, however large the tile.**
The compliance that makes the tile dish is the same compliance that protects its anchors. Strength and
flatness pull in opposite directions, and flatness turns out 5–18× the stricter requirement: three load
paths suffice for strength, eleven for a 10 pN margin, but **fifty-five** for flatness — against 43.7
independent patches. **No discrete attachment scheme is flat.**

**S-21. The 35–60 pN band the problem definition hands us is not a per-load-path number.** Traced to its
source, it is a *whole-cross-section* disassembly force for a 6–8-helix tube at 5.5 pN/s, and the authors'
own thesis is that it scales with parallel Holliday-junction density. Using it per path overstates capacity
by roughly the parallel-junction count. More generally: a DNA rupture force quoted without a loading rate is
not a material constant — the same origami class moves from ~42 pN at 5.5 pN/s to ~75 pN at 1.8e5 pN/s.

**S-22. Three separate crossover-spacing figures are all correct, for different things** — 16 bp per helix,
32 bp per interface (crossovers alternate between neighbours), 21 bp per interface for honeycomb. Quoting
the per-helix number where the per-interface one belongs would have doubled `D_⊥`. This is the same class of
error `C-0002` caught with the three quantities called `a`.

**S-23. `Double` results are not reproducible across runs of the same JVM.** Two study re-runs produced
different JSON: the JIT compiles a hot reduction part-way through a run, changing summation order and moving
the last one or two units in the last place. `gpd/README.md`'s determinism rule — *"a re-run that changes
nothing produces no diff"* — is not satisfied by a bare `Double`, and is now enforced by rounding the whole
JSON tree at the serialisation boundary.

**S-24. The continuum plate reduction is marginal by its own criterion.** `ℓ_⊥ < p`: the across-helix
bending length is shorter than the crossover spacing, so at the relevant wavelength the "plate" is really
~15 quasi-independent duplex beams. Recorded as a validity breach and queued as `T-10`, not papered over.
