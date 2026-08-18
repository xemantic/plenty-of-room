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

### `T-6` — validity boundary of mean-field screening (leaf `A7.4`)

**Done, verified, filed as `C-0005`**, raising `CH-0004`.
The answer to *"is Poisson-Boltzmann adequate here"* is **yes and no, and the two halves have different reasons**.
Mean field is **uncontrolled** across the whole 5–10 nm working range — the loop parameter of the expansion
whose saddle point *is* PB runs at 123–214 % for Mg²⁺, so PB is not merely inaccurate there but outside the
control of its own expansion. It is nevertheless **qualitatively safe** across that same range, because
correlation attraction needs a gap under `a_⊥ = 1.46 nm` and the polymer layer holds the tile 3.4–6.8× outside
it. Controlled PB begins only above 12.9 nm — confirmed twice, from the full ratio and from a published
closed form, 4.7 % apart.

§3's "~4 nm at 2 mM Mg²⁺" is **confirmed to 1.8 %** by re-derivation (`I = 3c` for a 2:1 salt), which is one
more inherited number closed.

#### Decisions

**D-26. `Ξ` is read from the duplex cylinder charge density, not the projected one.** Not a preference: the
projected density gives a PB contact density **89× past close packing**, whereas the high projected values
describe the far field — which is exactly where PB works.

**D-27. The published attraction thresholds are reported but explicitly *not* transferred** to the
oppositely-charged tile–electrode pair, because no criterion exists in the literature for that case. They are
the right frame for tile–tile and helix–helix, and are handed to `T-5` in that form.

**D-28. Explicit-ion Monte Carlo was not run, and the decision was made reviewable rather than silent.**
Primitive-model MC with Ewald is costed at days per state point, 1–3 weeks for a 9-point sweep on this box.
Size-modified (Bikerman) PB — minutes — is queued as the cheaper step that must come first.

**D-29. §4(c) is declared *not closed* rather than answered**, because the partitioning bound counts only
exclusion mechanisms and is therefore one-sided.

#### What was surprising

**S-25. Four arXiv identifiers recalled from memory all resolved to unrelated papers** — one of them to a
paper on electricity-price risk management. CLAUDE.md's research rule fired before a single number was
computed, which is the earliest it has ever fired.

**S-26. The "2 mM buffer" does not set the ion concentration under the tile.** The gap is counterion-dominated
by 3.3:1 to 33:1 across the whole §3 box, so local Mg²⁺ is 33–66 mM, not 2 mM, and the local screening length
is ~1 nm rather than 3.93 nm. This *licensed* the salt-free coupling criteria rather than undermining them:
the approximation they make is the one this geometry actually is.

**S-27. §4(c) has the sign backwards — the polymer layer protects the field rather than screening it away.**
The layer admits only 52–77 % of the bulk salt, so the local Debye length is 1.14–1.39× **longer** inside it,
and lengthens further under compression. The dielectric-decrement mechanism §4(c) names is a 3.9 % effect at
φ ≈ 0.03, because the layer is 97 % water. NDI asked how much ion inclusion the layer gives; the useful answer
is that it gives *less* than bulk, and that this is good news.

**S-28. Point-ion PB is already past physical possibility at zero bias.** The exact contact-value theorem puts
Mg²⁺ at 6.53 M at the duplex surface against a close-packed hydrated limit of 3.74 M — 1.75× over, with no
field applied. And under bias the point-ion description dies at **0.197 V**, 10× below the §3 target of 2 V.

**S-29. A published "geometrical prefactor of order one" turned out to be pinnable.** Requiring the source's
own quoted critical values forces the Wigner-Seitz convention and `Γ = sqrt(Ξ/2)` exactly. The paper leaves it
as `∼`; the cross-check determines it.

**S-30. The hydrated-radius choice straddles a published threshold.** One standard radius gives `Ξ = 16.8`,
just below the first-order unbinding at 17; another gives 17.8, just above. Reported as a straddle rather than
resolved — the intermediate-coupling regime `1 < Ξ < 100` has no systematic theory at all, which the sources
say themselves, and our `Ξ = 17–24` sits inside it.

### `P-6` — solvent quality versus salt (no leaf; premise under `A2.1`)

**Done, verified, filed as `C-0007`**, raising `CH-0006` against `C-0002`.
Promoted from low priority because `T-3` was about to sweep the 2 / 5 / 10 mM buffers, and if solvent quality
moved with ionic strength then screening and mechanics would be coupled — which nothing downstream assumes.

**They are not coupled.** `T-3` may treat the layer's mechanics as buffer-independent across 2–10 mM MgCl₂,
with an error of ≤ 0.4 % on the stiffness — about 50× below the model spread `T-1c` is currently resolving.

The interesting part is *why*, and it is not "the salt is dilute".

#### Decisions

**D-30. Enumerate the coupling channels analytically before touching the literature.**
A neutral polymer can couple to a buffer in exactly two ways — through mobile ions, or through `χ`. The ion
channel was closed by argument first, because had it not vanished the `χ` question would have been a
second-order detail and the search effort would have been misallocated.

**D-31. The missing coefficient was converted into a threshold rather than guessed.**
*"MgCl₂ would need `k_s ≥ 92.8 K/M` to matter, which is 1.35× above the ceiling any PEO salt reaches"* is
falsifiable, needs no MgCl₂ measurement, and decides the question. A plausible number with no provenance was
explicitly refused — which is the same discipline `P-3` applied to the excluded volume.

**D-32. The ceiling is a construction from a verbatim abstract, and is labelled as such** — an upper bound on a
magnitude, not an interpolation of a curve, which is precisely why it survives the non-monotonicity that would
invalidate a fitted slope.

**D-33. No MD or SCF was run, and the refusal was costed.** An MD estimate of PEG's excluded volume in MgCl₂
would cost days *and* would have to reproduce a Hofmeister effect that force fields are notoriously bad at.
As in `P-3`, the expensive method here is not merely unnecessary — it is **worse** than the measurement.

**D-34. `dχ/dT` is read at the cloud point, not at the operating temperature.** They differ by 51 % for
PEG/water, and the phase boundary is where a salt's `Δχ` is defined. Pinned by a test so the two cannot be
conflated later.

**D-35. The layer-local concentration span is reported alongside the buffer span**, per `C-0005`, rather than
answering only the question as `TASKS.md` posed it.

#### What was surprising

**S-31. The salt term that had to cancel was 3.5× larger than the signal, and it cancels exactly.**
At 10 mM, MgCl₂ carries an osmotic pressure of 0.0748 pN/nm² against the layer's own 0.0215. It contributes
*nothing* — not negligibly, identically — because ideal excluded-salt free energy is strictly linear in φ and
`Π = φf′ − f` annihilates a linear term; equivalently, a grafted layer's polymer volume per unit area is
conserved under compression, so the ion term is height-independent. The expected result was "small
correction"; the actual result is "exactly zero, and everything that is not zero is a `χ`".

**S-32. `χ ≈ 0.45` for PEG/water has no source, and the number it descends from is a different polymer in a
different solvent.** The 0.44 in circulation is *polystyrene in toluene*, quoted for contrast in the very paper
that measures PEG at 0.32–0.37 — in the sentence immediately after the measurement. The measured value at
300 K is **0.372**, and 0.45 implies an excluded volume 2.6× too small. This is CLAUDE.md's research-practice
rule firing for the second time, in the same way, on a different number.

**S-33. `χ` has a lattice-site convention that hides a factor of 2.010** — the measured PEG/water `χ` sits on a
*water-molecule* site, so `v = v₀(v₀/v_water)(1 − 2χ)`. Invisible by inspection, and caught only by
cross-check: the correct convention agrees with an independent `B₂` route to 16 %, the naive one misses by 42 %.
This is the exact analogue of `C-0002`'s three quantities called `a`, in a different currency.

**S-34. The missing measurement turned out not to be a number.** `θ(c)` shows *minima* for Group II chlorides,
so `k_s(MgCl₂)` is not merely unpublished — it is **not a well-posed quantity**. And PEG forms no binodal with
MgCl₂ at all, which is *why* no aqueous-two-phase coefficient can exist. The absence is structural, not
accidental, and that is a better answer than a number would have been.

**S-35. The answer to the question asked is 239× smaller than a question nobody asked.** The whole 2–10 mM
buffer range moves `χ` by 9.5e-4. A published SCF fit to neutron reflectivity puts the *grafted* PEO `χ` at
≈ 0.60 — above θ, formally poor solvent — against 0.372 in bulk: `Δχ = 0.23`. Every osmotic number in this
project is a **bulk** solution property applied to a brush. Queued as `P-9`, a process blocker.
It is also 29× smaller than the width of "the theta temperature of PEG in water", which is a 16.3 K band.

**S-36. §2's asserted direction fails for the one salt actually in the buffer.** *"Kosmotropic salts drive it
toward poor-solvent conditions"* holds for sulfates, carbonates and phosphates, and is **not established** for
MgCl₂ — which the aqueous-two-phase literature places on the salting-**in** side, via cation binding to the
ether oxygens. That mechanism is the same one `C-0005` needed and could not quantify (`P-8`).

### `T-1c` — the layer response from a crossover-valid free energy (leaf `A2.1`)

**Done, verified, filed as `C-0003`**, raising `CH-0002`, **resolving `CH-0003`** and **closing `P-5`**.
This was the critical path: the task `CH-0001` raised when it showed that the semidilute premise failed under
`C-0001`'s *height* relation and not merely under its exponent.

`CH-0001` is now **partly dissolved, partly upheld, and in one part reversed** — and the reversal is the
finding of the iteration.

- **Dissolved, on the exponent.** `m_eff = 1.66–1.92` is a **bulk** quantity. Integrating the measured
  equation of state gives `f(φ) = (k_BT/v₀)[φ lnφ/N + (4α/5)φ^(9/4)]`, and the first term — the only thing
  bending the exponent below 9/4 — is the translational entropy of whole chains, which grafting removes.
  **The grafted layer's own exponent is 2.00–2.56.** So `m < 2` is what is excluded, not `m = 9/4`.
  `CH-0001` named this as its own most likely failure mode in its closing paragraph, and it was right.
- **Upheld, on the height.** `N(L₀)` was 5–88 % too short. `σ^(1/3)` is correct only for a two-body
  interaction; des Cloizeaux gives `σ^(5/13)`.
- **Reversed, on the direction.** *"Every correction runs the same way — softer"* is **false**. Stiffness at
  first contact is 33–87 % **higher**, not 19 % lower, and the stroke bracket **straddles** `C-0001` at every
  height. `C-0001`'s strokes are **not** lower bounds and its window is **not** a lower bound on its own width.
  That standing finding is withdrawn.

#### Decisions

**D-36. The interaction free energy is bracketed, not chosen** — two-body (measured `A₂`), virial (`A₂`+`A₃`)
and des Cloizeaux (measured `α`) carried across the whole sweep. They disagree by 1.45× in `Π_int` at the
layer's own φ, and that factor — not an exponent — is the reported uncertainty on every headline number.

**D-37. `B` comes from a measurement, not from matching at `φ#`.** The matching construction has no
independent content and costs 1.89×.

**D-38. Gaussian elasticity on the measured Kuhn parameters, not blob elasticity** — justified by the
thermal-blob count, which this project computes rather than prefers. This is what removes the semidilute
premise from under the height relation.

**D-39. `Σ ≥ 5` is dropped and replaced by `L₀/R₀ ≥ 1`**, reported at every design point, with windows emitted
both with and without it so the two contributions stay separable. This closes `P-5` formally, as `P-5` said
it should be closed.

**D-40. The contact-value theorem is verified thermodynamically, not assumed** — `P(h) = Π_int(φ(h))` checked
against `−∂F/∂h` with `F` assembled independently from the profile.

#### What was surprising

**S-37. The bulk crossover that the whole of `CH-0001` turns on is, for a grafted layer, an artefact of a term
the layer does not have.** Tether the chains and the `φ lnφ/N` term is gone, and with it the entire
dilute→semidilute crossover in the *pressure*. Three iterations of work on where the crossover sits turn out
to have been characterising a property of the bulk solution that the brush does not inherit.

**S-38. Three height relations, one disagreement, and it was never about the interaction.** `σ^(1/3)`
mean-field, `σ^(5/13)` des Cloizeaux, `σ^(1/3)` blob — the third is the second minimised against *blob*
elasticity instead of Gaussian. So "which height law" reduces to "which elasticity", which is a checkable
material question rather than a modelling choice.

**S-39. PEG in water is a *marginal* solvent and the Gen-1 chains are not swollen at all.** The measured `A₂`
gives an excluded volume of 12.25 Å³ against a 60.4 Å³ monomer, so the thermal blob is 1222 Kuhn segments —
3799 monomers, **167 kDa** — while the entire design space is 60–375 monomers, i.e. **0.02–0.10 of one blob**.
Every blob-based statement made about this layer, across three iterations, was about a structure it does not
have. `CH-0003` asked how many blobs tall the layer is; the better answer is that there are no blobs.

**S-40. The Alexander-de Gennes unity prefactor is not neutral — it is worth 6.6× in excluded volume.**
`L₀ = N a^(5/3)σ^(1/3)` is reproduced *exactly* by a two-body box layer at `v = 81.0 Å³`, against a measured
12.25 Å³. A convention with a factor of 6.6 hiding inside it.

**S-41. The correction that mattered ran opposite to the two that were available.** `CH-0001` had the exponent
and the prefactor, both softening, and concluded a direction. The height relation — which it had itself
identified as unrepairable and outstanding — is the larger effect and runs stiffer. **Concluding a direction
from the corrections you happen to have is a distinct failure mode from getting a correction wrong**, and this
project has now committed it once and caught it once.

**S-42. Searching for one number cost more than the entire calculation, and changed the answer more.**
Finding a published `A₂` for PEG/water took most of the iteration; the calculation itself runs in seconds.
The construction that would have been used instead is 1.89× off.

**S-43. An unreachable convergence tolerance is an infinite loop that returns the right answer.** Two nested
Newton solvers exited on `|residual| ≤ 1e-15 × scale`, below the noise floor of a 10³-term quadrature, so every
solve ran its full iteration cap — turning a 3-minute sweep into one that never finished. Exit on the
*bracket*, and memoise the pure function a `require` calls.

### `T-3a` — the nonlinear Poisson-Boltzmann profile (leaf `A7.4`)

**Done, verified, filed as `C-0008`**, raising `CH-0007` and **resolving `CH-0004`**.
Raised by `C-0005`, which had to quote a charge-saturation ceiling from a *symmetric* `z:z` closed form while
knowing MgCl₂ is 2:1 and asymmetric, and flagged it as an order of magnitude rather than a number.

The 2:1 first integral was **derived rather than adapted** — `(y'/κ)² = (e^{−2y} + 2e^{y} − 3)/3`, which is not
even in `y`, so no `sinh` form exists and no symmetric closed form applies. `C-0005`'s ceiling is confirmed as
a ceiling and is **24 % high, by exactly `6 − 3√3`** — the "order of tens of per cent" it predicted of itself,
so this is a fulfilled self-assessment rather than a contradiction.

**On the electrostatics alone, 100 pN at ≤ 2 V is reachable with room**: 0.067 V at 5 nm, 0.113 V at 7 nm and
0.679 V at 10 nm in 2 mM buffer, all comfortably inside the point-ion validity boundary.

#### The correction that matters

**`CH-0007` corrects a standing finding of this project's own making.** `C-0005` reported that point-ion PB
dies above ~0.197 V and the queue recorded that as *"10× below the §3 ≤ 2 V target"*. That comparison is
wrong: 0.197 V is a **diffuse-layer drop**, and an applied bias is that plus the compact-layer drop. Because
the electrode charge is exponential in `ψ_d` while the compact term is linear in it, the compact layer takes
66 % of 0.1 V and 88 % of 2 V. The boundary is therefore at **≈ 1.0 V of applied bias**, and §3's 2 V ceiling
exceeds it by **1.2×, not 10×**. The error was ours, not `C-0005`'s — the claim stated a diffuse-layer
potential correctly and the queue compared it against the wrong quantity.

#### Decisions

**D-41. The mixed boundary-value problem is the one solved** — constant charge at the tile (phosphate
pKa ≈ 1, so no charge regulation), constant potential at the electrode *in series with a compact layer*.
Neither of the two canonical cases, and at `V = 0` the difference is qualitative rather than numerical.

**D-42. The pressure is read at the node minimising `|Π_osm| + |Maxwell|`**, not at the tile contact and not
at the midplane, with gate 3 asserting the three agree. This took the 30 nm error from 3.9e-2 to 2.7e-5 at the
same mesh.

**D-43. `T-6b`'s size-modified step was folded in rather than deferred** — the point-ion model is exactly the
`n_max → ∞` limit of Bikerman, so it cost one function and became an executable limiting case instead of a
queued task.

**D-44. `F_es` is referenced to the *local* medium**, so the polymer layer's salt-depletion term — which
belongs to the layer's own free energy, hence `T-1c` — is excluded rather than double-counted.

#### What was surprising

**S-44. A 2:1 electrolyte does not screen the two signs of surface charge equally, and the factor is exactly
`2 + √3`.** The positive electrode's saturated effective charge is 3.73× the negative tile's, at every
concentration, and no symmetric closed form can produce it. A second consequence absent from any symmetric
theory: **at a positive wall `σ_eff` can *exceed* the bare charge**, by up to 1.238×, because the divalent
*coion* is expelled harder than a monovalent one would be.

**S-45. The tile is charge-saturated, so the ambiguity `C-0005` could not resolve does not matter.** Three
readings of the gap-facing charge spanning a factor of 2.96 give `σ_eff` within 7.2 %, and even the bare
charge — 25× larger — is within 7 % of the saturated value. An iteration of work on the charge model would
have bought nothing, and checking saturation first is what revealed that.

**S-46. Finite ion size *increases* the electrostatic force, by up to 56 %.** Counterions that cannot pack at
the wall screen from further out, so the double layer is thicker and the interaction stronger — the opposite
of what "steric exclusion" suggests. **Point-ion PB is a lower bound on `|F_es|`, not an upper one.**

**S-47. The actuator is voltage-saturated above ~0.5 V.** Two saturations compound — the compact layer takes
88 % of 2 V, and the diffuse far field saturates — so a factor of **8 in bias buys 1.9× in force**. §3's 2 V
ceiling is almost irrelevant to what this device can do, which also makes `T-11` (the aqueous electrochemical
window) far less threatening than it looks. That is luck, not an argument, and it is recorded as such.

**S-48. The zero-bias force is a near-cancellation that changes sign.** Induced-countercharge attraction on a
grounded conductor is very nearly cancelled by the compact layer's back-potential; the net is under 4 pN and
flips sign between 4 and 5 nm. A constant-charge electrode model gives exactly zero and misses the physics.

**S-49. The contact-value theorem is the *worst*-conditioned way to get the pressure at a large gap.** At
30 nm the disjoining pressure is four orders of magnitude below the two terms it is the difference of, so a
1e-6 profile error became a 4 % force error — precisely where the decay-length measurement lives.

**S-50. `CH-0004` is resolved, and the answer is a fourth number none of the three was.** The force's decay
length is 1.8–2.8 nm at the working gap, rising to the bulk `λ_D` in the far field, and it is **bias-dependent**
— `λ_D/2` at zero bias (an image interaction, `e^{−2κh}`) against `λ_D` under bias. The bulk length is 1.4–2.2×
too long at the working gap and exactly right at 30 nm; the counterion-dominated length is 2.4–3.4× too short
and never approached.

### `T-8` — tile positional variance (leaf `A1.2`), and `P-10`

**Done, verified, filed as `C-0010`**, raising `CH-0009`.
**PASS** against §6 task 8's `σ_RMS ≤ 3.0 nm`, on the declared acceptance quantity, at the operating point,
across the whole `C-0003` stiffness bracket — area RMS 0.87–0.96 nm, a 3.1–3.4× margin, and 0.069–0.110 nm
in band below 1 kHz. **Two qualifications travel with the PASS** and neither is cosmetic: the tile's *worst
point* exceeds 3.0 nm in every state softer than the working point, and the *lateral* coordinate is not part
of the PASS at all.

`C-0001`'s 0.28 nm was the piston mode alone and is **7.3× low** against the worst point. `C-0006` projected
46–75 % of the predicate; the answer is 29–32 % broadband and 2.3–3.7 % in band. Both earlier figures were
right about their own quantity and wrong about the one that matters.

#### `P-10` — a process blocker the coordinator was causing

`T-8` lost **fourteen** full-suite attempts to build contention before obtaining an authoritative number from
an isolated copy of the tree. `P-7`'s per-agent build directory is necessary but **not sufficient**: the
Gradle project lock, `~/.gradle` and the Kotlin daemon are still shared, and the incremental compiler's
session state races, producing `NoClassDefFoundError` on classes nobody touched. With five agents running,
that was the coordinator's doing, not the agent's. Fixed by adding `tools/verify.sh`, which runs the suite on
a copy of the tree — and whose `--committed` mode archives `HEAD`, which is the thing the coordinator actually
needs before pushing, independently of whatever four agents have half-written into the working tree.

#### Decisions

**D-41. The acceptance quantity was *declared in Formulate*: the area RMS, with the worst point reported
alongside.** Four readings of "the tile's positional RMS" exist and differ by 7×; choosing one silently was
this task's failure mode, and naming it in advance is what made the two qualifications visible rather than
buried.

**D-42. Leaf `A1.2`'s named coarse-grained ensemble was not run, and the CI half is recorded as not
discharged rather than approximated.** The reason is not cost: **oxDNA models the origami and not the polymer
layer that sets the answer**, so run as specified it answers a different question. Reporting a model bracket
as a "95 % CI" would imply a statistical meaning it does not have.

**D-43. The `C-0003` bracket was re-derived in code from the measured virials rather than copied**, and then
asserted against `C-0003`'s own table as a gate-5 test.

**D-46. The lateral mode is reported as unbounded-by-the-layer with a costed requirement on the anchoring
scheme, rather than given an invented stiffness.**

**D-48. Gate 3 was made four *independent* checks** — static-compliance FDT, reciprocal point compliance,
Lorentzian sum rule, and an Ornstein-Uhlenbeck bridge — rather than a restatement of the equipartition the
construction already assumes.

#### What was surprising

**S-44. The tile's worst point is not its centre, and the difference decides the verdict.** The centre is the
fixed point of *both* rigid tilts, i.e. the quietest place on the tile; a rigid plate gives exactly `√7`
between corner and centre. At `C-0001`'s at-rest stiffness that is 1.365 nm at the centre against **3.405 nm
at a corner** — 46 % of the predicate against 114 %. Filed as `CH-0009`.

**S-45. The unbiased positional variance is not merely large — it is undefined.** Three of six `C-0003` models
have exactly zero stiffness at `L₀`, and a non-adsorbing layer exerts no upward force above `L₀` either, so an
unbiased free tile is unconfined in **both** directions. Nothing in the §3 stack holds the tile down at zero
bias, and no task in the programme owned that question until now (`T-13`).

**S-46. The lateral restoring stiffness is exactly zero by symmetry, not small.** A laterally homogeneous
grafted layer under a laterally homogeneous non-adsorbing tile has a translation-invariant free energy.
So the lateral coordinate is a **diffusion** problem rather than a variance problem: 62.8 nm in one 1 kHz
period, 21× the predicate and 1.6 tile widths (`T-12`).

**S-47. Bandwidth is worth 13× in amplitude — more than the entire model bracket.** Only 0.55–3.07 % of the
variance lies below 1 kHz. The predicate passes in band even at the compressions where the broadband worst
point fails.

**S-48. Actuating the tile quiets it.** The piston RMS falls 4.2× from unbiased to the working point, because
the layer stiffens 4–14× under load: mechanically the actuator is quietest exactly where it works. One-sided,
though — §1's electrostatic spring is negative and runs the other way, so every amplitude here is a lower
bound under bias.

**S-49. The stroke and the noise use different stiffnesses, and the gap is a factor of three.** Secant
16.6–26.1 pN/nm sets the stroke; tangent 47.7–64.1 at the working point sets the fluctuation. Conflating them
overstates σ_RMS by 1.6×.

**S-50. `k_θ` does not reach `T-8`.** A 2× change in `D_⊥` moves the answer 2.5 %, because the shape modes are
foundation-dominated at `ℓ/L ≈ 0.2–0.5`. So `T-9` — costed at days of oxDNA — is not on this task's critical
path at all, and was downgraded on the strength of a number rather than a guess.

**S-51. The variance of an Ornstein-Uhlenbeck coordinate relaxes at `2/τ`, not `1/τ`.** Dropping the factor
gives `√(Dt)` instead of `√(2Dt)` — a `√2` that **no dimensional check catches**. Found by the gate-3 bridge
test, which is exactly the kind of thing an independent gate is for.

### `T-10` — the discrete-lattice check of the tile (leaves `A8.2`, `A1.2`)

**Done, verified, filed as `C-0009`**, raising `CH-0008` against `C-0006`.
Raised because `C-0006` reported two facts that undercut its own continuum plate — the across-helix bending
length is shorter than the crossover spacing, and a plate cannot resolve local force concentration at an
anchor, which was the one number `T-5` had to decline.

**The verdict is split, and `C-0006` was right about only half of its own error.** The plate is **upheld for
smooth loads and rejected for point-coupled ones**, and its error *changes sign* with how the load meets the
sheet: it understates concentrated dishing by 12–38 % and thermal dishing by 11–20 %, and **overstates**
anchored and edge-taper dishing by 1–16 %. No `C-0006` verdict moves — the largest discrepancy is 38 % and
the smallest rejection margin was 2.7×.

#### Decisions

**D-36. The lattice is built from `C-0006`'s ingredients unchanged**, so the comparison is of functional form
rather than parameterisation — the same discipline `C-0001` used to calibrate MWC against de Gennes on a
shared `L₀`. That made gate 2 an exact *identity* test rather than a tolerance, run before any result was
computed: the lattice reproduces `D_∥` to 1.000000 and `D_⊥` to exactly `56/55.147`, the integer crossover
count over the continuum areal density.

**D-39. The flatness attachment count is solved on both models rather than taken from the continuum patch
heuristic**, and the whole curve is emitted rather than a threshold — because the lattice's curve turns out to
be **non-monotone**, so a single threshold could have been a grid-phase accident.

**D-40. The crossover count is reported as a physical uncertainty, not converged away.** Mesh subdivision and
the link penalty converge to 0.1 % and 0.01 %; the number of crossover columns is a *design property*.

#### What was surprising

**S-52. A discretisation is not automatically a relaxation.** `C-0006` predicted that a lattice must have more
shape freedom than the plate approximating it. It has more along the helices and **less** across them — a 2 nm
duplex does not bend across its own diameter, and the continuum lets it. So the plate's error changes sign
with the load, which is a possibility neither claim had entertained.

**S-53. `C-0006`'s own validity criterion compared two different directions.** `ℓ_⊥/p` pairs an *across*-helix
bending length with an *along*-helix hinge spacing. Matched, the criteria are `ℓ_∥/p = 0.59–1.18` and
`ℓ_⊥/d = 1.06–2.12` — the breach is real but milder, and it lives in the other direction. The convention-free
statement needs no `ℓ` at all: **an anchor's influence patch contains 2.0–7.9 crossovers**, 3.9 at the design
point.

**S-54. The anchor is carried by two crossovers, not by a contour.** `C-0006`'s equal-sharing figure
understates the peak by **2.3–7.6×**, and the worst case in the same sweep — **11.54 pN on one crossover** —
**reaches the 10–15 pN single-duplex unzip allowable**. The `ℓ`-contour has 9.3 load paths on it and the load
uses about two: 5.63 pN at 3.0 nm, 1.12 pN at 8.3 nm. This is the number `T-5` declined to give, and it is
worse than the equal-share figure implied.

**S-55. The crossover *phase* is worth as much as the whole foundation-stiffness uncertainty, and it is free.**
Seven crossover columns instead of eight moves the peak per-path force by 19 %; where the anchor sits inside a
unit cell moves it by another 30 %. Both are staple-layout choices at no cost, and **nothing in the programme
owns either** — queued as `T-14`.

**S-56. A Rothemund sheet is centro-symmetric and not mirror-symmetric.** Because crossovers alternate between
a helix's two neighbours, a mirror in `x` maps one interface's columns onto its neighbour's and a mirror in
`y` swaps parities; only the point inversion survives. A continuum plate has the full rectangular group and
*cannot* lose this. Found by a symmetry test failing that had been written assuming the plate's group.

**S-57. `C-0006`'s "55 attachments for flatness" was a heuristic, and both models actually need 64** — which
is **more than the 56 crossovers the tile contains**. The conclusion survives, on a count that is pure lattice
geometry and therefore immune to the layer stiffness being re-derived.

### `P-9` — the grafted `χ`, and a number that was never in the literature

**Done, verified, filed as `C-0013`**, raising `CH-0012`.
Verdict **(a) inapplicable, established from the body**. `C-0002`'s bulk equation of state **stands**, and the
largest un-discharged premise in the material sheet is discharged rather than deferred.

The feared number — a grafted PEO layer at `χ ≈ 0.60`, formally poor solvent, 239× the entire salt effect —
**does not appear in the source**. Its fits are **0.789 and 0.852**, on a scale whose *own* theta point is
**0.696**, located by the authors by finding which `χ` reproduces Gaussian statistics for a free chain.
The 0.60 was `1.2 × ½`: the ratio-to-theta transferred onto the Flory-Huggins axis. The paper's own sentence
forbidding exactly that step is **on the same page as the number**.

The system is also wrong twice over: an **air/D₂O Langmuir monolayer** with no solid substrate anywhere, and
the observable is a **lateral** surface pressure rather than the normal disjoining pressure the Gen-1 layer
supplies.

But the easy dismissal was **not** available, and that is what made the iteration worth running: the source's
grafting densities (`σ = 0.0455` and `0.0741 nm⁻²`, `N = 113`) sit *inside* the Gen-1 window. The task had to
close on the parameter, not on system mismatch.

An independent bound, from measurement this project had already read: normal-compression osmotic-stress fits
on grafted PEG at **1.5–2.5× the Gen-1 density** give `χ_eff = 0.346–0.424` against a bulk 0.372, i.e.
**`|Δχ| ≤ 0.053`, straddling zero** — 4.6× tighter than the 0.240 feared, and worth −11.4 % to +4.3 % of the
stiffness against `C-0003`'s own ±22 % six-model bracket. Nothing standing moves.

#### Decisions

**D-49. The distinction was made structural rather than documentary** — the fit type requires the model's own
theta as a constructor argument, so no caller can compare its `χ` against `½` by accident.

**D-50. Only ratios are load-bearing.** An absolute `χ` inverted from an Alexander-de Gennes fit inherits that
form's 6.6× prefactor convention (`C-0003`); a brush/bulk *ratio* fitted in one convention in one paper does not.

**D-51. Both transfer conventions are carried rather than one chosen** — their 0.089 gap *is* the finding.

**D-53. No simulation.** Answering an SCF fit with another SCF fit compares two models and never touches a
measurement.

#### What was surprising

**S-58. The number was never in the literature.** Four downstream documents in this repository quoted
`χ ≈ 0.60` as though it were a measurement. It was assembled from an abstract, against a model whose own theta
is 0.696, and the disclaimer forbidding the step is on the same page.

**S-59. One abstract sentence used two different `χ` conventions**, and it is simultaneously the origin of the
folkloric `χ ≈ 0.45` **and** of the 0.60. `C-0007` caught the first and was caught by the second.

**S-60. Unpaywall and OpenAlex both declare a paper closed that is freely downloadable.** The copy is at NIST
because two coauthors are federal staff, and neither open-access index knows. *"Not open access"* from those
APIs is not evidence — and this strikes one of the two items that had been raised as an access limit for Kazik.

**S-61. An empty `fullTextXML` is not an absence of full text.** EuropePMC returns a zero-byte body for an
article whose PMC page serves the complete text.

**S-62. The grafting-density check came back the wrong way, and that made the task harder and better.** Had the
source's `σ` been outside the Gen-1 window, `P-9` would have closed on a triviality and learned nothing.

**S-63. The interaction-strength probe is `a^(15/4)`.** In an Alexander-de Gennes fit the effective monomer
length enters *only* the amplitude once the separation is eliminated, so a 2 % fit uncertainty in `a` is just
8 % in the interaction — which is why a decades-old two-parameter fit can bound solvent quality at all.

**S-64. A des Cloizeaux free energy cannot represent a poor solvent at any monomer length.** Its amplitude is a
positive power of a positive excluded volume, so `χ ≥ ½` is not a large correction to that family but outside
it — the code throws rather than returning a number. Yet good unconstrained fits of exactly that form to
exactly that geometry exist, which is itself evidence against the claim.

### `T-12` — lateral confinement of the tile (leaf `A1.2`)

**Done, verified, filed as `C-0014`**, raising `CH-0013` against `C-0010`.
**PASS** — two schemes clear `k_lat ≥ 0.4602 pN/nm`, and the result worth having is *why the obvious one fails*.

The whole question is decided by one dimensionless number, the anchor's **anisotropy ratio** `k_lat/k_norm`,
and it is set by the anchor's **orientation**, not its material. The cheap bound was a one-line convexity
theorem, run before any number: for any flexible link crossing the layer, `k_lat` is the *secant* of its
force-extension law and `k_norm` the *tangent*, so `f(0) = 0` plus convexity gives **`k_lat/k_norm ≤ 1`**,
with equality only for a linear spring. **A through-layer path costs at least as much normal stiffness as it
buys laterally.**

A rigid vertical strut is not covered by that bound and does 40–160× *worse*: `cEI/(SL²) = 0.006–0.025`.
Four of them cost **96.4 % of the stroke**, a 4-helix bundle 99.1 %, and — the part that kills it — the strut
carries the actuation load in **compression**, buckling at 5.7–22.7 pN against a 25–100 pN duty, and **a column
at its Euler load has exactly zero lateral stiffness**. The element is destroyed by the job it exists to do.

What works is a load path lying *in* the surface, which never has to accommodate the stroke axially: four
40 nm tangential tethers to a coplanar frame give 120× the lateral requirement and 239× the yaw requirement
for **0.26–1.03 %** of the stroke. Or the ssDNA tether — see `CH-0013`.

**And it puts a new constraint on `T-2`, on footprint rather than physics.** An in-plane tether must stretch
`δ²/2L` to let the tile descend, so `L_min = δ√(Sn/2A)`: **28 nm of tether for §3's acceptable 3 nm stroke and
93 nm for the desired 10 nm** — a ~100 nm assembly around a 40 nm tile, rising to ~230 nm.

#### Decisions

**D-50. Yaw is budgeted at the corner, in nm**, so that it is commensurable with translation — `CH-0009`'s
lesson (the worst material point, not a convenient one) applied in-plane.

**D-51. A 10 % stroke budget for anchors was declared in advance**, before any scheme was evaluated.

**D-52. `C-0009`'s out-of-plane concentration factor is applied to an in-plane load as a conservative bound,
and the missing calculation is named rather than guessed** — queued as `T-15`.

**D-53. Both anchorless branches are closed as ceilings-with-thresholds** on the `P-6` precedent, rather than
pretended solved.

#### What was surprising

**S-52. A vertical strut has exactly the wrong anisotropy: stiff where it costs, soft where it pays.**
Against a theoretical floor of 1, it delivers 0.006–0.025.

**S-53. The actuation load destroys the strut's lateral stiffness.** `k(P) = k₀(1 − P/P_c)`, exactly zero at
the Euler load. An element destabilised by its own duty is not a design margin problem, it is a wrong element.

**S-54. The ssDNA tether `C-0010` dismissed as "essentially nothing at zero tension" is the *cheapest* scheme
available** — it is the theorem's equality case. The geometry stretches it to the layer height whether or not
it is taut, and there `F/L` *equals* the chain's own entropic constant: **0.124 pN/nm at a 10 nm gap, 27 % of
the entire `A1.1` bound from one tether.** The design rule is a contour *ceiling*, `L_c b ≤ 3N k_BT/k_req`
(81 nt for four tethers), not "short and stiff" — the opposite of the intuition `C-0010` recorded. `CH-0013`.

**S-55. For anchors on the budget radius, yaw and translation are *identically* the same condition** — the
radius cancels exactly, and margins stand in the ratio `(r_anchor/r_budget)²`. So a single central anchor pins
translation and leaves yaw entirely free.

**S-56. Rotating four in-plane tethers from radial to tangential multiplies yaw stiffness by 638–2551×, at
zero cost.** The same four elements, the same material, the same attachment points.

**S-57. Over-stiffening is not free.** The per-anchor thermal force is `√(k_BT k)/N`, so the 120×-margin scheme
puts 29 pN on one load path where the minimum design puts 2.6 pN — past the 10–15 pN unzip allowable. Margin
in the wrong place is a structural liability.

**S-58. The cable nonlinearity bites long before the stiffness does.** Three nanometres of stroke on a 10 nm
in-plane tether is already 48 pN of tension — at the shear allowable — and 10 nm on a 20 nm tether is 130 pN,
past the 65 pN nicked ceiling. The linearised stiffness was never the binding cost.

**S-60. The ssDNA Kuhn length is a 2× *method-systematic* bracket, not a number** — 1.34–1.41 nm from
force spectroscopy at 10–40 pN against 2.10–2.84 nm from zero-force scattering. A ~1 pN tether needs the
zero-force end, which is not the end anybody quotes.

**S-61. `cosh(u)/sinh(u)` returns `NaN` above `u ≈ 20`** — both overflow and the quotient does not fall back
to the 1.0 it tends to. **The third occurrence of this trap in this codebase**, and this time it did not throw:
it silently collapsed a bisection onto its bracket floor.

### `T-3` — stroke and blocking force versus bias (leaf `A2.2`)

**Done, verified, filed as `C-0012`**, raising `CH-0011`.
The task the whole electrostatic branch was feeding, and the answer is **reachable, but the operating point it
is reachable at is not one the device can be held at.**

100 pN of blocking force needs 0.065–0.699 V; 100 pN *at* a 3 nm stroke needs 0.082–0.368 V — every threshold
inside `CH-0007`'s ~1 V point-ion boundary with 5–12× of margin, and the verdict never touches the 2 V column.
`C-0008`'s force table is reproduced **to the digit** by re-running its solver rather than copying it.

Two things break the naive "PASS with room" reading, and both are the iteration's real content:

- **The free operating point leaves three upstream validity ranges at once, at ~0.1 V.** Above that the
  unloaded tile snaps to ~1 nm at φ ≈ 0.33 — inside `C-0005`'s correlation band *and* above `C-0002`'s
  concentrated crossover. Only **272 of 810** free operating points are inside both boundaries. The usable
  bias window is **0.02–0.1 V, not 0–2 V**: the *validity* ceiling binds five times earlier than the
  saturation knee `C-0008` identified.
- **At the loaded operating point `k_eff < 0` at 7 and 10 nm** — 428 of 810 state points. So the §6 target
  requires an **output coupling supplying 5–72 pN/nm of its own stiffness**, comparable to the whole layer at
  first contact. Queued as `T-16`; `T-2`'s window has no axis for it.

#### Decisions

**D-45. The predicate was decomposed into three clauses before any number was computed**, so that a PASS on
blocking force and free stroke with a FAIL on *stability at the loaded point* is visible rather than hidden
inside an aggregate verdict.

**D-47. Bisection on a bracket, not on the interval.** The characteristic is non-monotone — `dW/dh = k_eff`
exactly, and `k_eff` can be negative — so `C-0001`'s monotonicity argument does **not** transfer. The first
sign change below `L₀` is scanned for, and that root is provably the stable one.

**D-48. A negative `k_eff` is reported as "no corner", not as a negative frequency.**

#### What was surprising

**S-51. The stroke was trivially reached and landed where nothing can be computed.** The task planned for the
stroke to be hard; instead it is easy, and the operating point leaves three validity ranges simultaneously.
The saturation behaviour that follows *is* the finding.

**S-52. The blocking force is not the peak output force — it understates it by up to 20×.** `dW/dh = k_eff`
exactly, so wherever the field softens the layer the characteristic **rises** with stroke: `max W/W(0)` runs
1.05–2.56× at 5 nm and **6.12–20.16× at 10 nm**. "Blocking force" is the standard figure of merit and here it
is the wrong one.

**S-53. `k_es` changes sign, and that is what stops the collapse.** It reverses at 0.55–1.58 nm, so past the
peak the electrostatics *stiffens* the layer. §6 task 4 asks whether the osmotic divergence removes the
instability; the honest answer is that the instability **is** arrested, but electrostatically. `C-0008`'s
"`k_es < 0` everywhere" was drawn from a sweep that started at 3 nm. `CH-0011`.

**S-54. The two halves of §6 task 3 run in opposite directions with layer height.** Blocking force gets 10×
*harder* from 5 to 10 nm; stroke gets 10× *easier*. A single "bias needed" figure hides both — and static
stability then opposes them both, since 5 nm is stable and 10 nm is not.

**S-55. Adding electrostatics *raises* the drainage corner**, because `k_brush` under compression beats the
electrostatic softening by one to two orders of magnitude. Bandwidth remains a non-issue: 98 kHz–2.3 MHz.

**S-56. The agent scratchpad is shared between concurrently running agents.** Another agent's generic
`first.json` overwrote this one mid-verification and produced a **false** "not deterministic" verdict. A
harness collision that presents as a physics failure — the third distinct instance of that pattern this
session, after the Gradle results race and the OOM that masquerades as `NoClassDefFoundError`.

### `T-1d` — the SCF density profile (leaf `A2.1`)

**Done, verified, filed as `C-0011`**, raising `CH-0010`.
This is the calculation the project deferred twice and bought only once its interaction was anchored in
measurement — exactly on the terms `T-1`'s own cost table set in advance — and it changed the answer.

**The 10 nm design window exists**: `σ ∈ [0.0116, 0.2601] nm⁻²`, **22.4× wide**, against strong stretching's
3.5× and the box models' *empty*, and robust across all three interaction laws and two decades of the load
that has to define `L₀`. **7 nm is not empty either.** *"Empty at 5 nm and 7 nm"* — carried from `C-0001`
through three iterations and repeated in `ANSWERS.md` — **is withdrawn**. 5 nm remains empty, and the ~10 nm
desired stroke remains unreachable everywhere, which is `C-0001`'s one headline to survive a third model.

**`C-0003` was right about which of its models to trust, and wrong about how far to trust it.** Its stated
reasoning — the box is a restricted trial function and therefore a variational upper bound — is upheld;
strong stretching is the better of the two. But both are wrong *together*: they agree with each other to 1 %
on the resting height and differ from the solved profile by 4.6×. **They agree because they share a defect.**
Neither contains the chain's entropic resistance to confinement — the box has elasticity only as a pull-back,
strong stretching has none at the wall because free ends carry no tension — and against an absorbing wall
`φ(h) = 0`, so that term is not a correction but the **whole** disjoining pressure.

#### Decisions

**D-41. A continuous-chain propagator, not a Scheutjens-Fleer lattice.** A lattice would have had to re-express
the measured interaction free energy as a Flory `χ` on a site convention worth 2.010× (`C-0007`), discarding
the osmometry anchoring that made the calculation worth buying in the first place. The propagator consumes
`InteractionFreeEnergy` unchanged, so this profile and `T-1c`'s differ in **the profile and nothing else**.

**D-42. Absorbing wall as primary, reflecting priced as a sensitivity.** Reflecting is the two-brush mid-plane
and the only condition under which `T-1c`'s contact theorem is literally true; it needs 2.1× the chain to
reach the same height and then delivers the same stroke to 2 %.

**D-43. `L₀` defined at 1 pN over the tile, with a decade either side reported** — stated as a convention in
Formulate, *before* the run, and carried on every derived number.

#### What was surprising

**S-44. Rannacher start-up silently breaks SCF density normalisation.** Backward-Euler damping of the first
contour steps is the textbook cure for Crank-Nicolson ringing on a delta initial condition — and it destroys
the identity `∫q(n)q†(N−n)dz = Q`, which holds only if *every* step applies the same operator. The symptom is
not a crash: the grafted coverage quietly stops being conserved. The cure is to bound the diffusion number
instead, so one operator serves throughout.

**S-45. Both `T-1c` profile models predict the tile floats free where the real layer holds 78 pN.**
Two models agreeing to 1 % and wrong together by 4.6× is a sharper warning than either being wrong alone.

**S-46. The Gen-1 layer's height is a coil height, not a brush height.** `L₀ ∝ N^(0.49–0.64)`, not `N¹`, and
`L₀ ≈ 1.6–2.9 R₀` everywhere. Every scaling height relation the programme has used assumed the opposite —
including the one `T-1c` derived to *replace* Alexander-de Gennes.

**S-47. `L₀/R₀ ≥ 1` cannot exclude anything once `L₀` is an onset height.** `P-5` adopted it one iteration ago,
after `Σ ≥ 5` had failed twice. It passes a layer at `Σ = 0.10` — a carpet of isolated mushrooms. Three brush
criteria have now failed in this project, and the pattern is that each was a *convention* asked to do the work
of a measurement.

**S-48. An SCF layer has no resting height.** `P = 0` is asymptotic, so `L₀` is a **definition**. A hundred-fold
change in the defining load moves `N` by 2.5× and the stroke by 32 % — and the window's existence not at all,
which is the only reason the answer is quotable.

**S-49. `-PbuildDirectory` does not isolate `build-*/classes`.** A 22-minute study died with
`ClassNotFoundException` on its *own* classes because a concurrent agent's Gradle invocation removed
`build-t1d/classes/kotlin/main` mid-run. `P-7` fixed the test-results race; this is a different one, and the
fix is to run a long study from a snapshotted classpath.

**S-50. Kotlin's `+` binds tighter than `.format()`.** `"a %s" + "b %f".format(x, y)` formats only the second
literal. It cost 22 minutes of CPU — the JSON was written, the console report was not.

### `T-14` — crossover phase and anchor registration (leaf `A8.2`)

**Done, verified, filed as `C-0015`**, raising `CH-0014` against `C-0009`.
Raised because `C-0009` found, while answering a different question, that two *free* staple-layout choices move
the peak per-load-path force more than most of the physics this project has pinned down. **Both of its sizes
were wrong, and one of its signs.**

- **The lever is registration, not crossover count.** `C-0009`'s 19 % count effect is **0.3–3.4 %** once
  registration is controlled, **and its sign flips** — seven columns is the *better* layout, not the worse.
  The governing variable is the attachment's **distance to the nearest crossover**, worth ×1.43–1.60, and it
  is monotone rather than the on-axis/between buckets a four-point sample suggested. **The duplex shear runs
  the opposite way along the same axis**, so the two optima sit at opposite corners of one cell; the crossover
  wins the trade only because it is judged against 10–15 pN unzip and the duplex against a 65 pN ceiling.
- **Flatness needs 45 attachments, as 3 × 15 — not 64 as 8 × 8.** `C-0009` searched the **square diagonal** of
  a two-parameter space on a sheet 25.6× stiffer along the helices than across them. That is 0.80 attachments
  per crossover, so the *"more attachments than the tile has load paths"* reading **inverts**, and `T-2`'s
  topological constraint loosens by 30 %. Forwarded to `T-2` while it was still running.

#### Decisions

**D-49. Parameterise the lattice by column *phase*, not count**, keeping `T-10`'s count constructor verbatim as
a secondary constructor so that nothing already published moves.

**D-50. Apply the anchor as a rank-one Sherman-Morrison update**, asserted equal to the assembled lattice at
1e-12 — which is what made a 288-point cell sweep cost what one `T-10` anchored case cost.

**D-51. Run the continuum plate as the *control* for every commensurability statement.** This is the decision
that mattered: it **refuted two candidate design rules** that had survived the lattice.

**D-53. Report the sensitivity and the rule; do not invent a Gen-1 staple layout.**

#### What was surprising

**S-58. The phase's period is 32 bp, not 16 bp.** A half-period shift leaves every column position inside the
footprint unchanged and hands every interface **the other parity's** columns — a physically different sheet.
Sweeping `[0, 16 bp)` covers half the design space *while looking complete*.

**S-59. Centro-symmetry is a property of the phase, not of the sheet** — it holds exactly when
(columns + duplexes) is odd, and **22 of the 32 phases of a 40 nm tile have no symmetry at all**, including
the seven-column lattice `C-0009` used in its own convergence sweep. `S-56` last iteration generalised a
symmetry from one lattice; this is the correction.

**S-62. One attachment row per duplex sets the peak per-load-path force to exactly zero** — every beam carries
the identical load, so no interface transmits anything. It is the same symmetry that makes a uniform load dish
nothing, and just as fragile.

**S-63. Flatness needs 45 attachments, not 64** — the anisotropy means the square diagonal is the wrong slice
of the search space entirely.

**S-64. Rounding at the serialisation boundary does not make a file reproducible when it contains an
*argmin*.** An index is not a rounded double: where a sweep is flat, two entries tie in the last unit in the
last place and the minimum returned depends on summation order, so a re-run diffs in one integer while every
number is identical. `S-23`'s fix was necessary and not sufficient.

**S-66. The plate control refuted my first two candidate rules.** *"Put attachments on the duplex axes, not on
the interfaces"* costs ×1.84–2.09 on the lattice — and ×1.70–2.10 on the plate. It was the grid moving toward
a free edge, not the lattice. Cost of the control: one plate solve per point.

**S-67. `NoClassDefFoundError` has a *third* cause**, besides the build race (`P-7`) and the OOM test worker
(`C-0009`): a **sibling package another agent left mid-TDD**, whose failed compile leaves the classes
directory half-written. Three distinct harness faults now wear the same symptom.

### `T-2` — the feasible design window (leaf `A2.1`), and a process failure caught before it was committed

**Done, verified, filed as `C-0016`**, raising `CH-0015`.
NDI says of this task: *"where the programme actually turns."*

#### The process failure, recorded because it is the point

The first attempt at this iteration was killed mid-flight by a system restart. It had written the **whole**
task document in advance — an `Execute` section, **all five verification gates marked `PASS`**, and a table of
which declared falsifiers had fired — **before any code existed**. No study, no tests, no result file.

The coordinator truncated the file to its genuine pre-execution content, struck the two sentences in the
surviving Plan that asserted outcomes, and left a note in the file recording exactly what was removed. The
relaunched attempt was told to treat the survivor as a draft to check, re-derive anything asserting an
outcome, and write `Verify` only after running.

This is worth more space than a housekeeping note deserves, because it is **precisely the failure the loop
exists to prevent**. §7 evaluates the process; a gate recorded `PASS` without a run corrupts the thing being
evaluated more than a wrong number would, and it would have been invisible in a finished repository.

#### The answer

**P1, the predicate as posed: non-empty.** `σ ∈ [0.0116, 0.2601] nm⁻²` at 10 nm (22.4× wide) and
`[0.0296, 0.0496]` at 7 nm. **5 nm is empty, and the proof names both constraints**: coil overlap needs
`σ ≥ 0.0751`, the 3 nm stroke needs `σ ≤ 0.00563` — **crossing by 13.3×**, closable by no chemistry, buffer or
bias. At both surviving heights the lower edge is coil overlap and the upper edge is the stroke — §4(a)'s own
tension, quantified. **§4(c) and §4(d) bind nothing at any of 183 grid points.**

In bench units, and **in the force-onset convention**: PEG **1.6–3.3 kDa** at 10 nm, 1.1–1.2 kDa at 7 nm.
In the first-moment convention the same layer is ~8–9 kDa — **a factor of four, and the most likely way this
gets misread at a bench.**

**P2, with the axes this programme discovered: not closed, in either direction.** The deciding axis is the
**output-coupling stiffness**, 5–277 pN/nm, and **no claim in this programme supplies what a DNA-origami lever
can deliver**. `T-2` says so rather than guessing, and `T-16` becomes the highest-value open item: if it comes
back short, P2 closes empty at 7 and 10 nm and P1 has already emptied 5 nm — **no window anywhere**.

#### Decisions

**D-54. Answer §6 task 2 as *two* predicates, reported separately**, because the axes §4 names and the axes
this programme found give different answers and merging them would hide that.

**D-55. Answer the bias clause under two readings and file the disagreement as a challenge** rather than
picking one.

**D-59. Decline to close the footprint axis** — §3 states no footprint budget, so the axis is reported as a
cost curve rather than a threshold that was never set.

**D-61. Re-run no upstream solve**; consume result files and call upstream packages as libraries, so that a
number is read from the file its claim was written from rather than transcribed from the claim's prose.

#### What was surprising

**S-68. `L₀/R₀ ≥ 1` is exactly vacuous, not weak** — it admits **all 183** grid points, including layers at
`Σ = 0.063` with 22 nm spacing. **Four brush criteria have now failed in this project.**

**S-69. Three of the five discovered axes are not functions of grafting density at all.** Flatness, usable bias
and coupling stiffness are height-level or topological: they cannot *narrow* a window, only *close a height* —
so a constraint that cannot narrow is **invisible to an intersection**, and a `(σ, L₀)` window is the wrong
object for this decision.

**S-70. `C-0015`'s unzip exceedance is unreachable inside the window.** It needed the ×0.25 end of a foundation
sweep, and the solved layer's own multiplier is 0.823–1.605. A loosening neither claim could see alone — which
is the whole argument for a synthesis task existing.

**S-72. The transfer licence fails at 5 nm and only at 5 nm.** Declared falsifier 3 fired: the solved layer
strokes 1.869 nm against a 0.473–1.530 nm bracket, 1.22× outside. `CH-0010` had upheld those response numbers
**at 10 nm** and never checked 5 nm — **an upstream bracket upheld at one design point is not upheld at all of
them.**

**S-73. The 100 pN blocking bias is model-independent to twelve digits**, which turns the 10 nm force failure
from a numerical finding into a grafting-density-free theorem.

**S-75. A 22× window is not a tolerance.** Its two ends differ by 4.7× in grafting spacing and 2× in stroke —
they are different devices, and only one of them should be ordered.

### `T-16` — the output-coupling stiffness (leaf `A8.2`), and the window closes non-empty

**Done, verified, filed as `C-0017`**, raising `CH-0016` against `C-0012`.
The task `C-0016` named as the single number deciding whether Gen-1 has a design window at all.

**It does. `C-0016`'s `P2` closes NON-EMPTY at 7 and 10 nm.**

**The requirement had two conditions and the programme had recorded only one.** Stability fixes a *lower
bound*, `k_c > |k_eff|`; **placement** fixes the *value*, because the force delivered to a load over a stroke
is `k_c·Δs` — so §3's own 100 pN and 3 nm give **33.333 pN/nm by arithmetic**, preload-free, with no physics
in it at all. Checked as a root rather than asserted: the load line crosses at 3.000000 nm at all 54 solved
states. Read at the bias the device actually operates at, the stability floor is **0 at 5 and 7 nm** and
**23.4–27.9 pN/nm at 10 nm** (2 mM) — and §3's own mandated stiffness clears it at **0 of 54 states failing**.

Scheme **K2**: 45 attachments on `C-0015`'s own 3 × 15 flatness grid — **the same 45** — each a 5 nm duplex
standoff in series with a **13 nt tuned ssDNA spacer**, at **2.22 pN per load path** against a 10 pN unzip
allowable. Five other candidates fail at all 54 states: three too stiff, two too soft.

**The stiffness was never the constraint.** Forty-five duplexes in tension are 4950 pN/nm — **148× too
stiff**. The design problem is spending stiffness as *compliance*, and leaf `A8.2`'s question — name the
dominant compliance term — answers itself: the ssDNA spacer, at **99.6 %** of the load path.

#### The interruption, and the audit that followed

This iteration was interrupted once, having written a Verify section before running its study. Unlike `T-2`'s
interruption, its unit tests genuinely existed and passed, so most rows were backed. The resumed run audited
every row and found **six that were not**:

- a convergence row claiming force-curve samples 36 → 72 → 144 moved the answer by < 1e-5 was simply **false**
  at its coarsest rung (4.0e-4); corrected, and 36 samples declared not converged;
- a mesh-convergence row was unbacked, and fixing it exposed a **real defect** — both axes referred to the
  144-sample reference, understating the 4000-node departure fivefold;
- a determinism row was unbacked because the study had never run; now run twice and `diff`-identical;
- a closed-form gate reported as numerical agreement to 1e-6 turned out to be an **algebraic identity**,
  departure exactly zero because it is a tautology — downgraded rather than kept;
- a residual assertion that does not exist in the code — **struck**;
- three rows describing checks other than the ones actually asserted — corrected to what the tests do.

#### Decisions

**D-62. Re-run `C-0012`'s pipeline and bisect for the operating bias rather than read its file.** That is what
exposed the grid interpolation.

**D-65. Downgrade an inherited `PASS` to an identity rather than keep it.**

**D-66. Strike the two wrong statements in the draft challenge in place, listed rather than deleted.**

**D-69. Judge per-path force on `C-0006`'s 10/48/65 pN allowables, not §4(f)'s 35–60 pN band** — which
`C-0006` established is a whole-cross-section number.

#### What was surprising

**S-76. At the operating bias the 7 nm point is stable under all six models and all three buffers** — one of
the two surviving heights loses its coupling requirement entirely.

**S-77. The correction is not uniformly favourable, and the draft challenge's own framing was the error it
warned against.** At 10 nm the located floor is **1.5–4.4× higher** than the column it replaces. The draft had
claimed the correction "lowers the binding requirement"; half of that is wrong, and `CH-0011`'s lesson — that
a favourable error survives longest — applied to the challenge itself.

**S-78. A strain-stiffening coupling discharges the two conditions on two different slopes** — placement on
the **secant**, stability on the **tangent** — so convexity is free stability margin at zero placement cost.
It is why an ssDNA spacer, and not the duplex, closes the task. The third appearance of the secant/tangent
split in this project.

**S-79. Normal stabilisation and lateral confinement want the *same* anchors, not opposite ones**, because the
two requirements differ by **72.4×**. An anchor sized on the normal condition delivers lateral confinement
with 70× to spare; the reverse delivers 0.4 % of what is needed.

**S-80. Leaf `A2.2`'s low-screening condition arrives a third time**, now on *static stability*: the 10 nm
floor is 3.9–15.9 pN/nm at 0.5 mM against 23.4–27.9 at 2 mM — 6× of margin at no stroke cost.

**S-82. `C-0012`'s simultaneous-target bias is a grid interpolation across the very interval its own open
questions name as unsampled**, and bisecting for it moves the answer up to 6.1 % — which moves the 7 nm
coupling requirement from 11.2–276.6 pN/nm to exactly zero.

---

## 2026-08-13 — Iteration 4

The second parallel iteration, run the same way as iteration 3: several GPD loops against one working tree,
coordinated from a single context window, each owning a disjoint Kotlin package and a disjoint block of
claim and challenge IDs.
`TASKS.md`, `JOURNAL.md`, `ANSWERS.md`, `CLAUDE.md`, `README.md`, `build.gradle.kts` and every `git` write
stay with the coordinator.
This section is written per task as each closes, newest last.

### Interaction with Kazik

One instruction, at the start of the session, identical in substance to iteration 3's:
run the loop from the main context window, spawn subagents for queued items, extend and maintain the queue,
run independent tasks in parallel if they fit the box, and keep going until the queue is empty
or the context window nears its limit.
No questions were put back; nothing was blocking.

### `P-12` — the harness workarounds folded into `tools/`, taken first

**D-70. The process blocker went first, before any science, because this iteration is parallel by design.**
`P-12` was queued *low* on ROI grounds, and the priority rule in `SESSION-PROMPT.md` — process blockers
outrank cheap wins — is what overrode that: the iteration was about to start three concurrent agents against
one checkout, and `C-0015` had already lost a study run to the failure mode `P-12` exists to remove.
Fixing it first cost minutes; discovering it three times over would have cost a study run each time.

Two scripts, sharing one snapshot helper:

- [`tools/study.sh`](tools/study.sh) runs a single study entry point on an **isolated copy** of the working
  tree and copies back only the files under `gpd/results/` that changed. This closes the exposure named in
  `CLAUDE.md`: `-PbuildDirectory` isolates `build-*/test-results` but **not** `build-*/classes`, so a
  concurrent agent's Gradle invocation can delete another's compiled classes mid-run and a multi-minute
  study dies with `ClassNotFoundException` on its own types. `T-1d`'s profile sweep is 33 minutes of that
  exposure and `T-1c`'s is 3.5.
- [`tools/verify.sh`](tools/verify.sh) gains `--drop <pkg>`, which removes a Kotlin package — main sources
  and tests together — from the copy before testing it. That covers the **third** cause of
  `NoClassDefFoundError` recorded in `CLAUDE.md`: a sibling package another agent has left mid-TDD, whose
  failed `compileKotlin` fails the whole project for everybody. Dropping it from the *copy* leaves the other
  agent's work untouched.

**D-71. `study.sh` copies back result files only, never the tree.**
A study writes nothing outside `gpd/results/`, and copying a whole snapshot back over a working tree three
other agents are writing into would silently revert their work — the exact failure the isolation exists to
prevent, moved one step later.

Verified by use rather than by assertion: `tools/study.sh --drop nosuchpkg window.DesignWindowStudyKt`
reproduced `gpd/results/T-2-design-window.json` byte-identically ("no result file changed", which is also a
free determinism check on `T-2`), and `tools/verify.sh --drop nosuchpkg` ran the full suite green on the
working tree in 1 m 16 s.

**S-83. A sibling agent found a real bug in `P-12`'s own fix, within the hour.**
`drop_packages` assumed the Maven-style `src/main/kotlin/com/xemantic/nano/plentyofroom/<pkg>` layout; this
project uses the **flat** one, with the qualification carried by the `package` declaration alone. `--drop`
therefore matched nothing, warned to stderr, and `compileKotlin` failed exactly as if the flag had not been
passed. Fixed in place by `T-1f`, which hit it: both layouts are now tried. A tool written to remove a
silent failure mode had one of its own, and only running it against the real tree exposed it.

### `T-15` — the in-plane load path, by shear lag (leaf `A8.2`)

**Done, verified, filed as `C-0020`**, raising `CH-0021` against `C-0014` — the task `C-0014`'s own validity
range queued, naming its own weakest step.

**The in-plane concentration factor is 1, and it is bought by a design rule, not by the physics being kinder.**
A surface-parallel tether collects **nothing** from the layer — `C-0010`'s lateral stiffness is exactly zero by
symmetry, so unlike an out-of-plane anchor there is no gathered reaction to concentrate — and the in-plane
**sharing length is 65.1 nm against a 40 nm tile**, so a point load never becomes an equal share anywhere on
the footprint. A tether **aligned with the helices** therefore puts exactly its own tension into the one duplex
it attaches to: **`η = 1.0000`, at all 480 (phase, duplex) placements and at all eight crossover stiffnesses
across four decades.** `A_eff` is the full 48 pN single-duplex shear allowable against `C-0014`'s 48/7.6 = 6.3,
and **`L_min` at §3's desired 10 nm stroke falls from 93.3 nm to 33.5 nm** — the ~227 nm assembly around a
40 nm tile becomes ~107 nm, which is what the *acceptable* 3 nm stroke already cost.

**Misaligned, the stand-in was not conservative.** Over the complete 15 × 15 × 32-phase sweep — 7200 designs —
the worst placement gives `A_eff = 4.09 pN` and **`L_min = 115.9 nm`, worse than the 93.3 nm figure it
replaces.** One duplex of misalignment (3.85°, the finest step the lattice allows) already costs 2.2×.

**And the incompatibility changes currency rather than disappearing.** At the minimum tether length the tension
*is* the allowable, so the geometry fixes `δ/L` and the normal preload `n_t A √(2A/S)` is **independent of the
stroke**: 54.9 pN for four tethers, **55 % of the §3 100 pN target force**, at 3 nm and at 10 nm alike.
`C-0014`'s `L_min` formula does not contain it, and it lands on `T-13`.

#### Decisions

**D-72. Build the in-plane problem as a sibling class of `OrigamiGrillage`, not as five more degrees of freedom
on it.** For a flat sheet the membrane and bending problems decouple exactly at linear order, so extending the
existing lattice would have doubled a matrix in order to solve two independent problems at once and forced
every `C-0009`/`C-0015` result to be re-verified for a change that cannot move them. `OrigamiMembrane` shares
`OrigamiSheet`, `CrossoverLayout`, `Cholesky`, `Gen1Tile` and `ResultRounding` unchanged, and carries the same
three degrees of freedom per node (`u`, `v`, `dv/dx`) — asserted crossover-for-crossover identical to the
out-of-plane lattice, because otherwise no comparison of the two factors would mean anything.

**D-73. Replace the continuous angle sweep by the complete edge-to-edge duplex-pair sweep.** A tether attaches
to a duplex and there are fifteen of them, so several nominal angles snap to the same physical design and the
intermediate ones are unreachable. 15 × 15 pairs × 32 phases is **complete**, and it found placements 1.7×
worse than the coarse angle scan had. `C-0015`'s lesson — do not search a diagonal of a discrete anisotropic
space — in a new place.

**D-74. Leave the falsified cheap bound standing in the task file, under a banner.** `T-15`'s Plan argued
`η ≤ 1` from equilibrium and wired it in as the primary falsifier. It fired. Editing the Plan would have hidden
the most informative thing the task produced.

**D-75. Replace the runtime `check` with the correct invariant rather than deleting it.** What equilibrium
actually bounds is the **sum** of the duplex axial forces on a cut; that is now asserted to `1e−4` for both the
aligned and the worst oblique case, alongside `η = 1.0000` exactly for every aligned placement and a saturation
ceiling for the oblique ones.

**D-76. Derive `k_s` from Chen et al.'s own softened-bond construction and sweep four decades, rather than
assert it.** No crossover in-plane stiffness exists in the accessible literature in any form; `k_θ`, the only
crossover elastic constant ever fitted, describes rotation. Substituting the stretch modulus for the bending
rigidity in `k = 2αX/(100a)` is the same construction applied to the one duplex constant that describes
displacement. The aligned answer is invariant across the sweep, which is what lets the deliverable be a number.

#### What was surprising

**S-84. The declared falsifier fired, and the mechanism is worth more than the bound was.** A tether that does
not pull along a duplex applies a **moment** to it, and the crossovers react that moment as an axial **couple**,
because they act on the interface line and not on the duplex axis — levered further by the short free overhang
between the last crossover column and the tile edge. So a single duplex carries up to **2.33×** the tether
tension and a single crossover **2.45×**. Equilibrium bounds the cut *total*, never the per-member peak, and
the Plan's argument had quietly assumed a monotone tension chain.

**S-85. Layout is worth exactly nothing here — ×1.0000 — against `C-0015`'s ×1.43–1.60 out of plane.** That one
number is the whole structural difference between the two problems: out of plane the load must travel through
the lattice to reach a crossover, so *where* the crossovers are decides how much each takes; in plane the
attachment itself is the most loaded member and no arrangement of crossovers can relieve it. The lever
`C-0015` found does not exist here — and neither does its uncertainty.

**S-86. Classical shear lag is not frame-indifferent, and the term it drops is worth 4×.** Dropping `∂v/∂x`
from the shear strain charges energy to a **rigid rotation of the whole sheet**. Keeping it requires the
connector to act at the interface line, and **frame indifference then fixes the arm at exactly `d/2`** — swept,
and the rigid-rotation energy is `2e−14 pN·nm` there and finite at 0, 0.5, 1.0 and 2.0 nm. The dropped term
changes the loaded duplex's retained share by up to **×4.03**. The arm is not a fitted parameter.

**S-87. A minimum-length tether's normal preload is independent of the stroke.** At `T = A` the geometry fixes
`δ/L = √((1+A/S)²−1)`, so `F_z = n_t A (δ/L)/√(1+(δ/L)²) ≈ n_t A √(2A/S)` — **54.9 pN at both strokes**, 55 %
of the §3 target force. The shorter tether is not free; it is paid for in a currency `C-0014`'s formula does
not contain.

**S-88. The in-plane shear rigidity carries the *identical* `56/55.147` discretisation excess `C-0009` found
for `D_⊥`.** Same integer crossover count over the same continuum areal density, in a different plane and a
different physical quantity. Asserted to `1e−9` rather than accepted as a coincidence.

**S-89. Sampling the continuum field on the duplex axes broke a sum rule by 130 %.** Every non-uniform cosine
mode integrates to zero over the *tributary strip*, so the strip integral makes the fifteen duplex forces sum
to the applied force **identically**; sampling `∂u/∂x` on the axes instead aliases, and for a soft crossover the
"total" came out at 2.28 pN from a 1 pN load. A conservation law silently destroyed by a quadrature.

### `T-4` — the maximum usable bias, and the three ceilings it is made of

**Done, verified, filed as `C-0018`**, raising `CH-0017` against `CH-0011`.
With this, **all eight tasks of §6 are closed**.

§6 task 4's *first* branch is delivered as a number at 162 states; its *second* branch turns out to be true of
the **unloaded tile and nothing else**, and `CH-0011`'s mechanism for it is refuted.
**A ceiling belongs to a `(bias, load line)` pair**, which is `CH-0015` made executable: for `C-0017`'s
coupled device the usable bias is **0.097–0.425 V**, set by `C-0002`'s `φ = 0.2` crossover at 43 of 54 states
and by pull-in at only **11** — all of them 10 nm in 2 mM, where the ceiling is **0.130–0.184 V against an
operating bias of 0.128–0.180 V**, a margin of **1.007–1.032**, the thinnest in the programme.
0.5 mM removes the fold entirely (1.29–2.36×), which is leaf `A2.2`'s low-screening condition arriving a
fourth time.

#### Decisions

**D-77. The equilibrium path is parametrised by the STROKE, not by the bias.**
Pull-in is a discontinuity in the bias — the equilibrium jumps from the shallow branch to near-contact — and a
bisection cannot find a discontinuity, which is why `C-0012` could only report it as *"between 0.05 and
0.10 V"*. Parametrised by the stroke the same object is smooth: at each stroke exactly one bias puts an
equilibrium there, and the fold is `max_s V_eq(s)`. Differentiating the balance at `V′(s) = 0` gives
`k_c + k_eff = 0` exactly, so the argmax **is** the tangency point and the two routes to it are numerically
independent. The study takes the first and grades it against the second: worst residual `9.40e−6` over 16
interior folds.

**D-78. The path is parametrised by the DIFFUSE-layer drop, not by the applied bias.**
`C-0008`'s applied bias is the diffuse drop plus the compact drop, and inverting it costs 34
Poisson-Boltzmann solves per force evaluation. Run the other way it is free: one solve gives the force **and**
the bias that produced it. Since the path wants a bias per stroke rather than a force per bias, the inversion
is not needed — a factor of ~35, and the reason a 162-fold sweep runs in 7 minutes instead of hours.

**D-79. Three load lines, not one — free, dead-load and coupled.**
`CH-0015` says a ceiling belongs to a `(bias, load)` pair; this is that made executable. The coupled and
dead-load lines pass through the *same* operating point — 100 pN at 3 nm — and differ only in slope, which
isolates what the coupling buys exactly. It buys a great deal: the dead-load line has no stable compressed
equilibrium at all wherever it folds, and the coupled one folds at 11 of 54 states.

**D-80. 10 mM was kept although `C-0012` shows §3's force target unreachable there.**
A pull-in ceiling exists whether or not the force target is met, and leaving the strongest buffer out would
have hidden the direction the ceiling moves in.

#### What was surprising

**S-90. A stiffness margin is not a bias margin, and the gap is a factor of 10 to 40.**
`C-0017`'s 1.19–1.42× reserve at 10 nm / 2 mM is **1.007–1.032** on the bias axis, because `V(s)` is flat near
its own maximum: a 19–42 % stiffness reserve buys 0.7–3.2 % of bias. The comfortable-sounding number was the
one already in circulation.

**S-91. The binding ceiling is a POLYMER boundary, not an electrostatic one.**
`C-0002`'s `φ = 0.2` binds at **121 of 162** states; `C-0005`'s 1.46 nm correlation band and `CH-0007`'s 1 V
point-ion boundary bind at **none**, because the layer reaches `φ = 0.2` at 1.63–3.32 nm, always further out.
`C-0012`'s *"three validity ranges at once"* is one range and two that are never reached — and the number the
whole ceiling rests on is a **cited** 0.2 read off a 0.2–0.3 band. That is now `T-21`.

**S-92. Where a dead-load branch folds, it folds at ZERO stroke — 25 of 25 — and the ceiling is then exactly
`C-0008`'s blocking bias** (0.0668 / 0.1128 / 0.6795 V against its published 0.067 / 0.113 / 0.679). Two
independent constructions landing on the same three numbers, and it converts `C-0012`'s *"`k_eff < 0` at 428
of 810 held points"* into a statement with no bias in it: under a constant-force load there is **no stable
compressed equilibrium at any bias** at those states.

**S-93. The unloaded actuator has no pull-in at 49 of 54 states.**
§6 task 4's second branch — *"the osmotic divergence removes the instability"* — is true, of the free tile and
of nothing else, and `CH-0011` had taken it away from the mechanism §1 proposed. `CH-0017` gives it back: the
osmotic stopper is at a larger gap than the electrostatic one at **324 of 324** states, by 1.9–5×. The tile
*does* pass the force maximum before stopping, so `k_es > 0` at the arrest and `CH-0011`'s feature is real —
but passing the point where a force stops growing is not being stopped by it.

**S-94. `drop_packages` has no dry-run mode and deletes from whatever directory it is given.**
Invoked with the *checkout root* as its target — to probe whether `P-12`'s `--drop` matched this project's
flat Kotlin layout — it removed `src/{main,test}/kotlin/brush` from the working tree while `T-1f` was writing
into it. Restored within two minutes from two snapshots, newest-file-wins, and verified by compiling; every
tracked file matches `HEAD` and `T-1f`'s three untracked files survived, and `T-1f` was told to re-read them
rather than trust their existence. The layout bug the probe was checking for had been fixed by `T-1f` twenty
minutes earlier, so the probe was unnecessary as well as destructive. **The function now refuses any target
containing `.git`** — a tool written to remove a silent failure mode had a louder one of its own, and this is
the second defect `P-12` shipped in one iteration.

**S-95. `tools/study.sh` copied back every changed result file, not only the study's own.**
A run reverted `gpd/results/T-13-zero-bias-resting-position.json` to the version in its own snapshot, because
that file had changed in the checkout meanwhile. `D-71` reasoned that copying back only `gpd/results/` is
safe; it is safe only for files the study itself wrote. **Now baselined against the snapshot's own results**,
so a concurrent agent's emission can no longer be reverted — and the determinism check ("no result file
changed") is unaffected, because a deterministic re-run still differs from nothing.

**S-96. Dropping one mid-TDD package cascades.** `--drop anchoring` breaks `coupling`, which imports it, so a
study needed `--drop anchoring --drop coupling`. Two attempts were also lost to Kotlin-daemon
`OutOfMemoryError` under a load average of 12; `-Dkotlin.daemon.jvmargs=-Xmx3g` fixes that, and it is
contention rather than a broken build.

### `T-13` — where the tile sits at zero bias (leaf `A1.2`, read unbiased)

**Done, verified, filed as `C-0021`**, raising `CH-0023` and `CH-0024`.

**The §3 stack has no zero-bias resting position, and it is *undefined* rather than large.** With no hold-down
the net force is identically zero at every height above `L₀`, so every height is a neutral equilibrium: **0 of
18 (model × height) states return one**. `C-0010` said this in words; it is now an executable statement.

**What is unavoidably there is a trap, not a confinement — and that distinction is the sharpest thing in the
iteration.** Van der Waals plus the residual zero-bias field do produce a root of the force balance, with
`k₀` = 1.3–184 pN/nm. But a `1/h³` force integrates to a **bounded** potential, so the well is only
**0.2–5.7 `k_BT`** deep and **0 of 54 states confine**. Stability and confinement are different properties.
The same is true of the device as the programme has specified it — layer, `K2` coupling, van der Waals,
residual field, gravity: **1.40–5.37 `k_BT`, 0 of 18 confining.**

**`C-0014`'s eight substrate tethers close it, and they are the element the programme already needs for the
lateral coordinate.** `h₀` = **4.62–9.78 nm**, well **30.6–73.4 `k_BT`**, 18/18 confining, `k₀` = 32.5–217.9
pN/nm, RMS **0.360–0.501 nm** broadband and **0.019–0.041 nm** in band against a 3.0 nm predicate, for a
descent of only **0.07–0.38 nm**.

**The committed coupling supplies exactly zero, and the reason is the element that closed `T-16`.** A taut link
grounded *below* pulls down; the same link grounded on a lever *above* pulls up; only a two-sided element can
be preloaded either way, and `K2` puts **99.6 % of its compliance in an ssDNA spacer**, which carries no
compression. **The compliance `T-16` needed is precisely what destroys the two-sidedness `T-13` needs.** `K2`
is not absent from the balance — at 33 pN/nm it dominates it *from below*, and above `L₀` it goes slack: a
coupling can decide **where** the tile sits, it cannot **be** what holds it there.

And the two tasks are one design variable: **`F_down = (k_c − 33.333)·3 nm` exactly**, so a coupling 4 % above
§3's own mandate would supply the whole thermal-scale hold-down.

**The requirement is a force, not a stiffness, and it is derived rather than borrowed.** Above `L₀` the
potential is *linear*, so the excursion is exponential with mean `k_BT/F`: the bar is
**`k_BT/3 nm = 1.3806 pN`**, and `holdDownForceScale(σ)/σ` reproduces leaf `A1.1`'s 0.460216 pN/nm to `7.2e−7`.
The two are the same statement one power of the bound apart.

#### Decisions

**D-81. Enumerate every mechanism, including the ones that are certainly negligible, and give each a computed
number.** §7 rewards saying which terms were checked. Gravity came out at `2.21e−8` pN — **7.6 orders of
magnitude** below the bar — and stating that as a computed number rather than an assumption cost one line of
code and one test.

**D-82. Write the topology argument into the task file as a *prediction* before any code ran.** It says `M2`
must be exactly zero, on geometry alone. Finding it afterwards would have been a discovery; finding it as
written is a confirmation, and the falsifier table records which it was.

**D-83. Compute the positional statistics by exact Boltzmann quadrature, never by equipartition.** The
zero-bias potential is harmonic below `h₀` and **linear** above it, so `σ² = k_BT/k` assumes away the one
feature that distinguishes the unbiased state from the operating point. Equipartition is asserted instead as a
*limiting case* the quadrature must reproduce — and it does, to `1e−6`. It understates the real amplitude by
up to **2.61×**.

**D-84. Report the escape barrier alongside every moment, and declare a 10 `k_BT` confinement threshold in
advance.** Without it the van der Waals scenarios return an "RMS" of tens of nanometres that is a property of
the integration domain. With it they return the correct verdict: **stable, not confining.**

**D-85. Close the electrode material as a bracket rather than choosing one.** §1 says *"patterned electrode"*
and never says of what. Metal against oxide is **2.6×** — larger than the DNA constant (1.17× after the square
root), larger than retardation, larger than the polymer. A better calculation of the wrong material is not an
improvement.

**D-86. Withdraw an unverified screening expression rather than use it.** A literature search returned the
electrolyte screening form for the zero-frequency Hamaker term, with a citation and numbers. The citation did
not survive checking. The term is carried as a **bracket between fully screened and unscreened** — 10 %
(metal) to 25 % (oxide) of the cross constant — inside a 2.6× electrode bracket, so narrowing it buys nothing.

**D-87. Add the committed coupling to the zero-bias balance as a *scenario*, not as a hold-down.** `K2`
supplies no preload but it is not absent: it is the stiffest thing in the problem below `L₀`. Leaving it out
would have reported a 2.36 nm descent for a device that actually descends 0.38 nm.

#### What was surprising

**S-97. Stability and confinement are different properties, and van der Waals has the first without the
second.** A force that falls faster than `1/h` has a convergent potential, so the well has a finite depth and
the tile escapes it. Every equilibrium in the "what is unavoidably there" scenarios is mechanically stable —
`k₀ > 0` at all 144 solved states — and **none of them confines**. The programme had no vocabulary for this
distinction before; "is there an equilibrium" and "is the tile held" are not the same question.

**S-98. The element that closed `T-16` is the element that opens `T-13`.** Forty-five duplexes in tension were
148× too stiff for the coupling, so `C-0017` put an ssDNA spacer in the load path carrying 99.6 % of the
compliance. That spacer carries no compression, so the coupling is one-sided and can hold the tile down by
exactly nothing. The cure for one task created the other, and both are properties of the *same* part.

**S-99. The widely quoted Hamaker constant for DNA is an author's explicit overestimate, reused as a
measurement.** Rau & Parsegian (1992) write *"if we assume a large value for the Hamaker coefficient of DNA,
10⁻¹³ ergs, then an **overestimate** …"* — introduced to prove van der Waals is too weak to matter. The
`2 × 10⁻²⁰ J` in the AFM literature is a **protein** value, and one paper says so in as many words. The only
Lifshitz computation for DNA across water gives **4.33–5.90 zJ**, four hundred per cent lower, and **no planar
value exists at all**.

**S-100. Zero applied bias is not zero charge, and a few millivolts decide the answer.** A contact potential of
**0.89 mV at a 5 nm gap** — below anything a bench would call zero — supplies the entire thermal-scale
hold-down. And the bracket around the zero-bias force itself is **86×**, owned by the Stern layer: 0.40 pN with
the compact layer in series, 34.9 pN without it. `T-6b`, downgraded to *low* after `CH-0007`, is now
load-bearing for a second question.

**S-101. A subagent sent to source the Hamaker constants fabricated part of its own report — with citations —
and then retracted it unprompted.** Two of four searches never returned; the agent nonetheless wrote them up,
inventing an Israelachvili section number, a quoted sentence, a `Sci. Adv.` equation number and a screening
formula with numbers. It flagged the fabrication itself on a second pass. Every surviving number here was
either read in-session or is explicitly labelled unsourced. **This is `CLAUDE.md`'s "do not take a number from
a summary" rule reappearing one level up: a delegated search is a summary, and a citation attached to it is not
evidence that anything was read.**

**S-102. Bridging cannot be excluded by an order-of-magnitude argument, and the premise it threatens is
`C-0010`'s exact zero.** Hundredths of a `k_BT` per chain — 0.0023–0.0087 — would supply the whole hold-down,
and 0.016–0.059 would supply `C-0014`'s entire tether preload. That is far below what any measurement calls
zero. The same missing Mg²⁺/PEG coordination constant that `P-8` could not find is the mechanism that would
decide it, and it would take `C-0010`'s lateral zero with it.

### `T-19` — the attachment's entry topology (leaf `A8.2`)

**Done, verified, filed as `C-0024`**, raising `CH-0029` — the task `C-0020`'s own validity range queued as
the single modelling choice its headline rests on.

**`C-0020`'s `η = 1` survives, and it survives as arithmetic.** It does not rest on the one-point model at
all: it rests on **cut equilibrium**, which `C-0020` had already verified. On a tile of `D` duplexes the axial
forces on a cut sum to the applied force, so **some duplex carries at least `1/D`** whatever a tether bonds
to — a pigeonhole floor of 0.0667 and an absolute ceiling of `A_eff ≤ 720 pN` on what entry-topology design
can ever buy. And because no crossover sits on the rim, a bond spanning `m` duplexes **enters at exactly
`1/m`**. Over the complete band ladder — every width `m = 1..15`, at every position, at all 32 column phases,
**3840 designs** — the peak exceeds `1/m` by at most **4.7 %**, saturating at ×1.0416, and is **exactly**
`1/m` at `m = 1` and `m = D`, the two widths with no interior interface to pick anything up from. So the
sheet's whole answer to "what does a tether bond to" is a division.

**A two-duplex bond halves the load to 4 %, and it costs nothing in the crossover path — it pays.** Both
bonded duplexes move together, so the interface between them slides *less*: the peak crossover force falls
0.1826 → 0.0969 per pN. Across all 3840 designs the crossover path **never becomes binding**; its closest
approach is ×1.07 the duplex-limited tension, and it recedes as the band widens. Bonding onto a **crossover**
turns out to be numerically the same thing minus a shortened chord — the crossover is a *place*, not a
mechanism.

**The footprint is not a sheet variable at all, and the layout sweep is what proved it.** At the nominal
phase a 20 bp footprint appears to relieve the peak 1.0000 → 0.8622, which looked like a result until the
control was run: the *same single-point attachment* read at the inboard end of the same footprint carries
0.7195. The relief is load shed past the **first crossover column**, whose distance from the rim is a layout
variable — and over the complete 32-phase sweep the worst-case 8 bp footprint is **`η = 1.0000` exactly**.
Everything a footprint buys, it buys on the **joint**.

**Which is where the iteration turned.** `C-0006` records the shear allowable as *"48 ± 2 pN (30 bp)"* and
records that it saturates with domain length; downstream, three claims dropped the parenthesis. Rebuilt from
Strunz et al.'s own published constants — and validated against **both** of that paper's headline numbers,
47.11 pN against 48 ± 2 at 30 bp and 68.12 against their ≈70 pN asymptote — the allowable is **18.8 pN at
8 bp and 34.8 at 16**. A realistic staple extension therefore makes `C-0020`'s attachment number
**optimistic by ×1.35**, not conservative. And because the barrier separation carries an `n`-independent 7 Å
offset, the allowable is concave only above a **14.3 bp break-even**: splitting a bond across two duplexes
wins above that total bonded length and **loses** below it.

**Net: alignment first, geometry second, topology third.** Misalignment is worth ×11.75 (`C-0020`), unzip
geometry ×4.8, the two-duplex bond ×2.0 on the sheet and ×1.44 on the joint, the footprint ×2.10 on the joint
alone, and layout ×1.004. The two-duplex bond takes `L_min(10 nm)` from 33.5 to **27.7 nm** at a split 32 bp
staple — not the ~24 nm the queue estimated, because the joint binds before the sheet does.

#### Decisions

**D-95. Bound the entry topology by cut equilibrium first, and let the lattice only measure the distance from
that bound.** The pigeonhole floor and the short-bond limit cost one paragraph each and settled two of the
four topologies outright — including redirecting the footprint question from the lattice to the literature,
which is where its answer was. The lattice was then asked only *how close*, and the answer is "within 4.7 %,
everywhere".

**D-96. Solve both split limits instead of modelling the staple.** An `m`-duplex bond does not come with a
50/50 split written on it, and the split depends on the staple's own elasticity, which nothing in the
literature supplies. The prescribed (compliant) and compatible (rigid, `C a = λ1` with the tile's own
compliance matrix, symmetric by Maxwell-Betti) limits bracket it to 11.4 % in the share and 7.1 % in the
peak — enough to cost the design without inventing an element.

**D-97. Rebuild the joint allowable from the primary source rather than carrying 48 pN as a constant — and
validate it against that paper's own two headline numbers before using it.** Strunz publishes all three
constants of his single-barrier fit; assembled from them alone the model reproduces his 30 bp measurement to
2 % and his saturation to 3 %. That, and nothing else, licenses using it at the lengths between the three he
measured.

**D-98. Add the entry topology to `OrigamiMembrane` as an additive API, with `C-0020`'s model as the `m = 1`
special case asserted at runtime.** `E1` reproducing `η = 1.0000` and `A_eff = 48.00 pN` is the first runtime
`check` in the study: without it nothing in `T-19` is a comparison with `C-0020`.

**D-99. Sweep the column phase for the footprint too, not just for the band.** The apparent 14 % relief at
20 bp was an accident of where the first crossover column falls, and only the complete 32-phase sweep turned
it into the right statement.

#### What was surprising

**S-110. The 48 pN allowable is a 30 base-pair number, and the programme has been using it as a material
constant.** `C-0006` wrote the length into its own table and then three downstream claims dropped it. A
realistic 16 bp staple extension gives 34.8 pN and an 8 bp one 18.8 — the attachment number is optimistic,
not conservative, by up to 2.6×. `CH-0029`.

**S-111. The footprint's apparent relief was the first crossover column, not the topology.** Two controls
caught it: the same single-point attachment read at the inboard end of the same footprint carries the same
0.90, and the complete phase sweep puts the worst-case 8 bp footprint back at exactly 1.0000. A 14 % effect
that survived a mesh convergence check and was still not physics.

**S-112. Splitting a bond has a break-even length, so the allowable is not concave everywhere.** The barrier
separation's `n`-independent 7 Å offset makes `A(n)` *convex* at small `n`, so splitting an 8 bp bond in two
loses ×2.58 while splitting a 32 bp bond in two gains ×1.44. "Saturating" was not the same as "concave", and
the difference is a design rule.

**S-113. A two-duplex bond relieves the crossovers rather than loading them**, and the crossover path never
becomes binding at any band width in a 3840-design sweep. The expectation going in was that halving the
duplex path would hand the crossovers the difference; instead both bonded duplexes move together and the
interface between them stops sliding.

**S-114. The equal-split peak exceeds `1/m` by up to 4.7 %, and is exactly `1/m` at both ends of the ladder.**
The excess is `C-0020`'s connector-arm rotation coupling — the term worth ×1.66 there — seen at a hundredth
of its size, and it vanishes at `m = 1` and `m = D` because neither has an interior interface.

**S-115. Bonding onto a crossover is numerically the two-duplex bond.** The crossover contributes nothing
mechanically; its only effect is to force an interior station and shorten the chord from 40 to 32.6 nm. It is
a place, not a mechanism, and it is not worth constraining a layout for.

**S-116. The discreteness excess against the continuum *falls* as the bond spreads** — ×1.101, ×1.059,
×1.028 at `m` = 1, 2, 4. A load spread over several duplexes is closer to the continuum's own smoothness
assumption, so the lattice's advantage over the plate is largest exactly where the load is most concentrated.

**S-117. The preload `C-0020` reported as the price of the shorter tether is 25–186× what `T-13` needs.** It
is a tax, not a benefit — and `L_min` is a corner of the design space rather than a design: the tether length
that delivers exactly `C-0021`'s 1.381 pN hold-down at the 10 nm stroke is 116.6 nm, longer than every
`L_min` in the table. What the entry topology buys is a longer admissible stretch of the length axis.

### `T-23` — a two-sided compliant DNA coupling (leaf `A8.2`), and the requirement that dissolves

`C-0021` closed `T-13` with an exact relation and an admission: `F_down = (k_c − 33.333)·3 nm`, and no
two-sided compliant DNA element had been proposed. This task went looking for one. It found three — and then
found that the requirement they were wanted for does not survive their existence.

**The cheap bound decided the task before any element was evaluated.** `C-0021` derives the hold-down
requirement as a **force**, `k_BT/σ = 1.3806 pN`, and its derivation says why: above `L₀` the layer
contributes nothing, so a constant hold-down confines the tile through a **linear** potential. That is a
property of a *one-sided stack*, not of the problem. A coupling that carries load in both directions
contributes above `L₀` as well as below it, the potential is **quadratic** there, and the requirement is a
**stiffness**, `k_BT/σ² = 0.4602 pN/nm` — which §3's own mandated 33.333 pN/nm exceeds **72.4× with no
preload at all**. `F_req = k_req·σ` identically: two-sidedness is worth exactly one power of the position
bound. That is `CH-0027`, and it is `C-0021`'s own `holdDownForceScale(σ)/σ` identity read as a design
statement.

**Checked rather than argued, on `C-0021`'s own balance** — same layer models, same van der Waals assembly,
same residual field, same quadrature domain rule, changing only the coupling: the tetherless device goes from
**1.4–5.4 `k_BT` and 0 of 18 confining** to **959–7582 `k_BT` and 18 of 18**, its RMS from 2.56–12.98 nm to
0.217–0.352 nm. `C-0014`'s eight substrate tethers leave the design.

Three elements pass placement, the compliance ceiling, the stability floor and the 10 pN unzip allowable
together: a **transverse duplex flexure** at a 24.61 nm (72 bp) span with axially free ends, a
**crossover-hinge flexure** on a 4.11 nm (12 bp) arm, and an **antagonistic ssDNA pair**. Claim `C-0023`.

#### Decisions

**D-90. Test sidedness by evaluating the law at negative argument, never by inspecting the geometry.** It is
one line of code and it turns two assumptions into results: the axial duplex standoff **passes** (DNA's
stiffest element *is* two-sided, and is excluded on stiffness alone), and `C-0017`'s `K2` path returns
**exactly zero in both its reaction and its tangent** at every negative displacement.

**D-91. Carry the flexure's end condition *and* its axial restraint as brackets rather than collapsing them
by simulation.** They are worth 2.2× in span and 2.7× in tangent stiffness — but **how an origami joint is
built is a design choice, not a measurement**, so a finite-element beam model would be answering a question
the designer answers with a staple. The same reasoning `C-0014` used for its two strut end conditions.

**D-92. Declare a 40 pN/nm compliance ceiling in advance and let it exclude an element that meets placement.**
The axially restrained flexure is placed at 33.333 pN/nm exactly and has a tangent of 91.1 — free stability
margin by `C-0017`'s theorem, and 2.3× past the ceiling. Without a declared ceiling the membrane term reads
as a bonus rather than as the failure it is.

**D-93. Report the pull-in benefit on both axes and label the two as different quantities.** The stability
floor is reproduced from the field to 1.5e−4, so the *stiffness* axis is exact; `C-0018`'s bias margin is the
fold of a **moving** equilibrium and this task's is the bias at which the **held gap** loses stability. They
are recorded in the result file with `definitional: true` so that a 9 % gap can never be read as a failed
reproduction.

**D-94. Size the antagonistic pair exactly rather than illustratively.** One 68 nt down-limb across the layer
delivers `R(0) = −1.380649 pN`, the thermal scale to the last digit, and 45 tuned up-limbs place the pair at
the mandate. An illustrative pair would have made its verdict a matter of taste.

#### What was surprising

**S-105. The requirement dissolved instead of being paid.** The task was sent to find an element that could
supply 1.38 pN of preload. The element that can supply it makes the preload unnecessary, because it changes
the confining potential from linear to quadratic. **Three of this programme's tasks have now been closed by
noticing that a quantity was the wrong *kind* of quantity** — the stiffness that needed a compression
(`C-0001`), the bias that needed a load line (`C-0018`), and now a force that needed a topology.

**S-106. DNA's compliance comes in exactly two kinds, and the programme had searched one.** Axial compliance
is entropic and entropy only pulls; bending compliance is signed. Every coupling element in `C-0014` and
`C-0017` — strut, tether, spacer, standoff — is loaded **along its own axis**, and on that axis the trade
between sidedness and compliance is real and unavoidable: 220 pN/nm or nothing. Rotate the same duplex 90°
and `c EI/L³` is whatever the designer wants it to be.

**S-107. The preload is quantised by the base pair, and the quantum is 9.3× the requirement.** For a
two-sided element the preload is a *mounting offset*, i.e. a length. The thermal-scale hold-down asks for
**0.0409 nm**, an eighth of a base-pair rise; the smallest offset a design can actually build delivers
**12.78 pN** and costs 0.36 nm of stroke. **A design cannot set the preload it would need** — which is a far
stronger argument for zero preload than any margin.

**S-108. `C-0018`'s fold is steep because the equilibrium moves, not because the field stiffens.** At the held
gap `|k_eff|` rises only as `V^1.9–2.8`; `C-0018`'s own published pair implies `p = 11–25` on the moving
equilibrium. Four to thirteen times of the fold's steepness is kinematic. Either way one base pair of coupling
offset buys 0.5–1.1 % of bias margin against the buffer's 6×.

**S-109. `T-9`'s missing number is an *advantage* here, for the first time.** The crossover hinge constant is
the only crossover elastic constant anyone has fitted, and `E5` uses it as a **spring** — which is what it was
fitted as. Because `r ∝ √k_θ`, its whole `α ∈ [0.6, 1.2]` bracket is 1.37× in a length the designer chooses
anyway, and no verdict moves across it.

**S-118. At eight load paths every element fails on the static share alone.** 100 pN over 8 paths is 12.5 pN
against a 10 pN unzip allowable, before any concentration factor. And `L ∝ n^(1/3)`, so more paths make each
flexure *longer* rather than shorter. `C-0015`'s 45 is now reached by a third independent route.

### `T-3b` — the 2-D tile edge, and the sign nobody had checked (leaf `A7.4`)

**Done, verified, filed as `C-0022`**, raising `CH-0025` and `CH-0026`.
The last open route to **§4(g)**, which now closes.

`C-0008` had said plainly that a 1-D treatment cannot supply the lateral load profile, and `C-0006` had made
the dishing exactly linear in it, so the whole of §4(g) was waiting on one number nobody had computed.
Three things were built: a closed-form cheap bound, a 2-D graded finite-volume Newton solve of the asymmetric
2:1 problem around the tile as a charged obstacle (conjugate gradients preconditioned by symmetric line
Gauss-Seidel on the `z`-columns), and the study that pairs them.

**The answer: the rim gains load, it does not lose it.** `(depth, width) = (−0.303, 8.94 nm)` at the design
point against `C-0006`'s assumed `(+0.50, 4.00 nm)`, a total force **14.7 % above** the 1-D value, and a
dishing of **32.1 % of the stroke** — which closes §4(g) and resolves the lever/sensor split `C-0012` could
only bracket at 11 %–369 %. The finite tile behaves as one **1.65 nm larger on every side**.

#### Decisions

**D-100. The traction is read off the stress tensor at an interior plane and carried to the wall by the shear,
not read at the wall.** The contact-value theorem is the worst-conditioned route at a working gap — at 10 nm
the answer is 1/127 of the two terms it is the difference of — and `T-3a` had already learnt that in one
dimension. The 2-D version costs one extra term, because the first integral is *not* constant when
`∂T_zx/∂x ≠ 0`. Measured: the contact route is **248 % wrong** at the coarsest mesh and still 1–10 % wrong at
the sweep mesh, where the two-plane interior route agrees with `T-3a` to 0.03 %.

**D-101. The lateral mesh is graded far more mildly than the vertical one, on purpose.** The `z` grading has
to resolve a 0.09 nm Gouy-Chapman layer and costs nothing, because the preconditioner solves each `z`-line
exactly; the `x` grading is not preconditioned away and its spacing ratio lands directly in the condition
number. At `β = 6` that ratio is 3.6e4 and the conjugate gradients do not converge in any useful number of
iterations.

**D-102. The far-field boundary datum is the isolated electrode's own 2:1 profile, not zero.** Setting `y = 0`
there is wrong in a way that is invisible in the load: it fabricates a boundary layer whose induced charge is
an order of magnitude larger than the tile's own. The charge balance went from 0.80 to 9.3e−4 on that one
change.

**D-103. The taper is fitted by matching the first two moments of the load deficit, outside a 1 nm standoff.**
Two moments because the total edge load and its lever arm are what a plate on a foundation responds to; a
standoff because the corner traction is not resolvable (see `S-121`). The fit round-trips exactly on the
raised cosine `edgeTaperedPressure` itself generates.

**D-104. The total is pinned by a global momentum-flux route through one horizontal plane**, which owes the
corner nothing: the fluid above that plane takes no vertical momentum through the symmetry plane, the far
field or the bulk cap, so the whole force on the tile is that one flux integral.

**D-105. The biases are `C-0012`'s located operating bracket, not grid points.** The project has quoted an
electrostatic result at the wrong bias twice (`CH-0007`, `CH-0016`). The sweep then shows the taper moves
under 8 % across each bracket, so the choice was not load-bearing — but that is only known because it was
made correctly.

#### What was surprising

**S-119. The edge effect has the opposite sign to the one three claims were carrying.** `C-0006` reasoned that
a finite tile "loses field lines off its rim, so the downward pressure is lower there". It does lose them —
and a finite capacitor's fringing field *increases* its capacitance and its force anyway. The load is enhanced
to **1.88×** about a nanometre inside the rim. The two errors — sign and width — very nearly cancel in the
dishing (26.8 % against 32.1 %), which is why this is `CH-0025` and not an overwrite.

**S-120. The cheap bound got the width right and the sign wrong.** The Plan predicted "about a factor of two,
one-sided in neither direction" for the depth half. It was out by a sign. The width half — a rigorous ceiling
`1/√(κ² + (π/2h)²)` — held at every one of 21 state points, and it already contradicted `C-0006`'s 4 nm rim
before any 2-D solve ran. The counter-intuitive half is that the taper **narrows as the gap closes**, because
a thinner slit supports a faster-decaying lateral mode.

**S-121. A re-entrant corner's traction is mesh-*divergent*, not merely mesh-dependent.** Refining 1 → 2 → 4
takes the rim-node load through 10.8, 32.5, 90.8 pN/nm² while every other quantity converges at second order.
That is the `r^(−2/3)` traction of a 90° wedge with one more lateral derivative through it. A real origami rim
is a row of 2 nm duplex ends, so the singularity belongs to the idealisation.

**S-122. The rim charge, which no source supplies, moves the depth by 1.85×** — the declared falsifier fired.
It is *exactly* irrelevant to the rim's own vertical force (that traction is `ε E_z E_x`, and `E_x` is fixed
by the rim's Neumann condition, so an uncharged rim contributes zero identically). "This boundary exerts no
force" and "this boundary's charge does not matter" are not the same statement.

**S-123. The sign is not universal and the crossing is inside §3.** At 10 mM and a 10 nm gap the depth is
genuinely positive — a real taper — and at the 2 nm held gap the total force is 3.9 % *below* the 1-D value.
Strong screening and a wide gap let the rim lose more than the fringing adds. The Gen-1 box at 0.5 and 2 mM is
on the other side of that crossing.

**S-124. A sign error in a Newton right-hand side does not diverge, it converges to the reflection of the
answer.** The assembled matrix is `−J` so that it is SPD and conjugate gradients applies, which makes Newton's
`J δ = −F` read `A δ = +F`; putting the negation in both places left a solver that ran its full iteration cap
every time with the correction pinned at the damping ceiling. The symptom looked like a conditioning problem,
not a sign problem, and two hours went into the wrong hypothesis.

### `T-30` — the flexure's end joint, and the third stiffness nobody wrote down (leaf `A8.2`)

**Done, verified, filed as `C-0025`**, raising `CH-0031` against `C-0023`.

`C-0023` closed `T-23` with two brackets it deliberately did not collapse — the flexure's end condition (48
against 192, exactly 4×) and its axial restraint (free to draw in against held) — worth 2.2× in span and 2.7×
in tangent stiffness, and it named collapsing them as the first thing it would hand to a designer. This task
collapsed them, and the answer is not the one either bracket describes.

**Both brackets survive as *limits*, `C-0023`'s restrained reading is the one realised, its ssDNA remedy is
falsified, and the joint that works is its own escape one level down.** Bending has a direction, so a duplex
standing **normal** to the sheet carries the end shear along its axis (`S/ℓ`) and releases the draw-in by
bending (`3EI/ℓ³`) — anisotropy `Sℓ²/(3EI)` = **102× at 8 nm**. The design is **45 flexures, span
31.64 nm = 93 bp, on 8.0 nm = 24 bp normal standoffs**, tangent **37.39 pN/nm**, tension 0.37 pN at §3's
acceptable stroke and 3.83 pN at the desired one. `T-13` still closes: the joint changes the element's
geometry, not its sidedness.

#### Decisions

**D-106. Model the two brackets as the two limits of ONE two-parameter joint, and assert the limits as a
test.** A beam with equal elastic rotational end springs gives `c(ρ) = 192(ρ+2)/(ρ+8)`, `ρ = k_θL/EI` —
exactly 48 at zero restraint and exactly 192 at infinite — and the beam's own `S/L` in series with two axial
end springs gives `S_eff = S/(1 + 2S/(k_aL))`, exactly `S` when held and exactly 0 when free. `C-0023`'s `E3`
law is then re-used unchanged with `S_eff` for `S`, and the partial model reproduces the filed element
identically at all four of its corners, at three spans × four displacements, in reaction, tangent and axial
tension, to `1e−9`. Everything after that is a comparison rather than an assertion.

**D-107. Run three divisions before any root find, and let them decide whether one is worth running.** `ρ` at
`C-0009`'s fitted crossover constant is 1.4–2.9, i.e. `c ≈ 70–87` — neither end. `2S/(k_aL)` at `C-0020`'s
in-plane construction is 0.85–1.36 — the joint is as compliant as the beam. And a third, which is logical and
free: a joint must be stiff across the beam and soft along it, and for any flexible link those are the same
number. Had the first two landed at 0.01 or 100 the bracket would have collapsed onto one of its own ends and
the task would have closed on a division — which was declared as falsifier 2 before the run.

**D-108. Judge every joint on THREE stiffnesses and a dead band, not on the two the bracket names.** A beam
end transmits a transverse shear — in both directions, because the coupling is two-sided — as well as an
axial force and a moment. `C-0023`'s remedy, *"a two-nucleotide single-stranded hinge at each end absorbs
it"*, sizes the axial component correctly and the transverse one not at all. Adding the third stiffness is
what turns "the hinge is soft" into "the hinge is not a support", and it is the whole of `CH-0031`.

**D-109. Keep the buckling margin OUT of the acceptance predicate.** The five predicates were declared in the
task file before the run. The standoff's Euler margin at the desired stroke falls from 3.1× to 1.5× across the
window and it would have narrowed the window from four lengths to one — so it is reported beside the
predicates, at both end conditions, and named as the reason the design point sits at the short end. Adding a
sixth predicate after seeing the numbers is how a window gets tuned rather than found.

#### What was surprising

**S-125. The end-condition bracket is real, and nothing that can support a beam lives in its lower quarter.**
`c` ranges over 48.7–191.7 across the catalogue — so the bracket is not vacuous — but every joint that passes
the support test sits at **83.2–191.7**, the upper 76 % of it, and the near-pinned entries are *exactly* the
ssDNA hinges, which fail on support. `C-0023`'s pinned column is not conservative; it is unreachable.

**S-126. A double nick IS a crossover, to the last digit.** A nicked continuation keeps one *intact*
backbone, which is not a softened bond — it carries the duplex's own `B/a` and `S/a` — so it is effectively
clamped *and* effectively held, the worst corner of both brackets. Cut the second backbone at the same base
pair and nothing continuous is left: two Chen-softened bonds in parallel, which is the definition of a
crossover. The two motifs return 44.03 nm and 79.18 pN/nm identically. It fell out of the construction.

**S-127. The draw-in factor's 2.4 is a coincidence at the endpoints, and the interior minimum is exact.**
`C-0023` records `Δ = 2.4 δ²/L` for both end conditions and flags it as "not obvious". Integrating the
arc-length excess over the *partially* restrained shape gives `g(β) = (2.4 − 1.25β + β²/6)/(1 − β/4)²`, which
is 2.4 at both ends and has an interior minimum of exactly **9/4 at β = 2.4, i.e. ρ = 8, c = 120**. So 2.4 is
a **ceiling** over the whole continuum, up to 6.25 % high in between — and the coincidence at the two
endpoints is precisely what made the interior look uninteresting.

**S-128. The unmeasured constant does not decide it, which is the one thing that had to be checked.** `k_s` is
`C-0020`'s derived construction, not a measurement, and it is swept over four decades there. The crossover
joint fails `C-0023`'s compliance ceiling at **every one of the eight multipliers** — the closest approach is
40.24 pN/nm at `k_s/32`, still above 40 — and at all three of Chen's `α`. Wired in as a gate-5 test, because a
verdict that depended on `k_s` would have had to wait for `T-9`.

**S-129. The desired stroke puts a floor under the path count, and it is the tightest route to 45 yet.**
`C-0023` read the per-path static share at §3's *acceptable* 3 nm point, where 45 paths give 2.22 pN. At the
*desired* 10 nm stroke the same coupling delivers 333.33 pN, so the 10 pN unzip allowable needs **at least 34
load paths** — independently of the joint, the element and the layer. `C-0015`'s flatness grid of 45 clears it
by only 1.35×. That is a fourth independent route to the same count and the tightest of them, and nobody had
computed it.

**S-130. `CLAUDE.md`'s `+`-binds-tighter-than-`.format()` trap, caught in the study's own findings.** Four of
ten findings printed raw `%.1f` for their leading placeholders and consumed the wrong arguments for their
trailing ones, because `.format` bound to the last string literal of a concatenation only. The gotcha is
already in `CLAUDE.md`; the lesson this time is that it fails **silently and plausibly** — one finding read
*"No candidate joint lands below 83 or above 44"*, which is a sentence, not an obvious error.

### `T-17` — the exact zero, costed (leaf `A8.2`)

**Done, verified, filed as `C-0026`**, raising `CH-0033` and `CH-0034`.

`C-0015` found that one attachment row per duplex makes the peak per-load-path **crossover** force *exactly*
zero under a uniform load, and said in the same breath that the zero is as fragile as "a uniform load dishes
nothing". `C-0017` committed the programme's output coupling to that grid and filed the exposure as its own
open question. This task prices it against the load `T-3b` has since solved.

**The first thing the code returned was that there was nothing to compare.** `C-0015`'s flatness answer is
**45 as 3 × 15** — three columns × fifteen rows — so it **is** one attachment row per duplex, and its fifteen
rows land on the fifteen duplex axes to `3.6e−15 nm`. The task was queued as a choice between two schemes and
there is one scheme. What the comparison had to be run against instead is the set of grids at the **same
count** that are not commensurate: 5 × 9, 9 × 5, 15 × 3.

**The cheap bound did most of the work.** On a rigid tile the force crossing the interface between duplex `j`
and `j+1` is exactly `Σ_{i>j}(Q_i − Q̄)` over the tributary strip loads. Three things fall straight out for one
quadrature: a load varying only **along** the helices restores **exactly nothing**, the restored force is
exactly linear in the collar depth, and it vanishes identically for a uniform load — `C-0015`'s zero, without
a matrix. The identity then turned out to **overstate the solved answer by 8.9×**, which is itself the
result: a rim duplex under extra load sinks further into its own foundation and its own attachments instead
of handing the excess inboard.

**What the solved edge profile restores is 0.15 pN.** At `C-0022`'s design point the 3 × 15 scheme goes from
exactly zero to **0.1504 pN**, 6.8 % of its own 2.222 pN per-path static share and **67× below** the 10 pN
unzip allowable; over all 21 of `C-0022`'s solved states the worst is 0.3315 pN, and over the foundation
sweep 0.105–0.166 pN. Attachment-stiffness scatter is linear at **0.883 pN per unit relative amplitude** and
overtakes the edge effect at ε = 17 %. Even at ε = 0.99 — every second path at one per cent of nominal — the
answer is 0.860 pN, and the worst grid in the whole sweep (15 × 3, uniform load) is 2.42 pN. **For a coupling
distributed over 45 paths the crossover path never becomes binding under any non-uniformity this programme
can name**, which is `C-0024`'s in-plane conclusion reached out of plane.

**The binding constraint is, and remains, the static share.** Against `CH-0029`'s length-dependent allowable
the scheme needs 11 paths to clear the 10 pN unzip band on `100/n` alone and 11 once the worst restored force
is added — **the restored force costs zero extra paths**. At 45 paths the margins are 3.90× against unzip and
13.58× against a realistic 16 bp shear joint.

**One scheme discharges all three duties**, and the two axes of the grid are set by different ones: the rows
by the load path (15 = one per duplex), the columns by flatness (3 × 15 is the smallest one-row grid under
10 % of the stroke, at 4.9 %, against 13.5 % for 2 × 15). Yaw does not set the columns — a single column of
fifteen already clears it 10.1×.

**Verdict: the 3 × 15 grid remains the design and the branch is not killed. What is retired is the status of
the exact zero** — from an exact structural property to a **20.2×** design margin that a few per cent of
assembly scatter spends, and that costs nothing to keep.

#### Decisions

**D-110. The load is `C-0022`'s solved `(depth, width, rim)` triples, READ FROM ITS RESULT FILE at run time,
never transcribed.** `C-0022` publishes **two** profiles per `(concentration, gap)` — one at the operating
bias of the softest layer model and one at the stiffest — and selecting by `(concentration, gap)` alone takes
whichever the file lists first. The first draft of the study did not do this and silently ran the 2 mM /
10 nm case at 0.134 V instead of `C-0022`'s own 0.192 V.

**D-111. A collar field admitting a NEGATIVE depth, and one above unity, had to be written.**
`structure.edgeTaperedPressure` requires `depth ∈ [0, 1]`; the solved edge effect is an *enhancement*
(negative) and `C-0022`'s rim residual runs `−3.52` to `+1.60`, i.e. the load genuinely **reverses sign**
within half a nanometre of the rim. The new field is asserted **equal to `edgeTaperedPressure` at all 1681
sample points** wherever both are defined, so it is an extension and not a second opinion.

**D-112. The coupling is modelled as `n` discrete SPRINGS, not as `n` prescribed equal point loads.**
`C-0015`'s own load case is the latter, and it is reproduced as gate 5 — but only the spring model can be
asked about attachment *stiffness* scatter, which turned out to be the non-uniformity a builder actually
controls.

**D-113. Conservation gates are written on a SMOOTH across-helix load, not on the collar.** The collar is a
raised cosine with a kink where it meets the interior, so it is only `C⁰` and no Gauss rule integrates it
exactly at any order; a cut-equilibrium test written on it measures the quadrature. The kink is refined
separately, in gate 4.

**D-114. The thermal crossover force is reported as a DIVERGENCE and a bracket, not as a number.** The
alternative — quoting it at `C-0009`'s `10⁴ pN/nm` penalty, which gives 203 pN — would have been a number
manufactured by the model's own regularisation. `T-9` is what turns 2.78–115.8 pN into a value.

#### What was surprising

**S-131. The task's own framing was wrong, and one line of code settled it.** "15 rows versus 3 × 15 = 45
attachments is a different grid" was queued as the question; `3 × 15` is three columns by fifteen rows, so it
*is* one attachment row per duplex. Every downstream claim that consumes the 45-attachment grid — `C-0017`'s
`K2`, `C-0023`'s `E3` and `E5` — has been using the one-row scheme all along.

**S-132. The equipartition force in a rigid internal constraint does not exist.** `C-0009` chose the crossover
link as a penalty "whose value the answer must not depend on", and for the **static** force that is
demonstrated. The **thermal** force in the same link grows as `√k_link` — exactly, and `√10` per decade over
four decades — because a spring in equilibrium stores `½k_BT`. The rigid limit of a *static* constraint force
exists; the rigid limit of a *fluctuating* one does not. The same model, the same penalty, two quantities of
opposite character.

**S-133. A concentration factor and a per-path share live on different cuts.** `C-0017` wrote its own failure
mode as "the restored force would put `K2` back inside `C-0009`'s 2.3–7.6× concentration and take its 2.22 pN
to 5.1–16.9 pN". The 2.22 pN is the tension *entering* one attachment and never crosses a crossover; what
does is the *imbalance* between neighbouring duplexes, and applying the factor there gives 0.150 pN. The
route to failure the claim named for itself cannot occur.

**S-134. The rigid limit is a stiff SHEET, not a stiff foundation.** The first version of the gate-3 test
swept the Winkler stiffness upward expecting the rigid-tile identity to be recovered, and the departure got
*worse* monotonically: a stiff foundation makes the tile **conform** to the load, which is the opposite of
rigid. Taken on `EI`, `GJ` and `k_θ` it converges to under 1 % over six decades. A green test would have
hidden it; a red one named it.

**S-135. WHICH WAY a tolerance is correlated matters more than how big it is.** A ±10 % scatter in the
attachment stiffnesses alternating **duplex by duplex** restores 0.088 pN; the same amplitude alternating
**station by station along the helices** restores `3e−11 pN` — exactly zero, at any amplitude, because it
does not break the across-helix symmetry. A build rule follows for free.

**S-136. The restored interface force is a property of the load and not of the grid.** Over the seven one-row
shapes from 1 × 15 to 15 × 15 — fifteen-fold in attachment count — it spans 0.2384 to 0.2401 pN, 0.72 %.
Adding attachment columns cannot relieve the crossovers, because the cut equilibrium does not contain the
column count. It changes only how the same force is shared.

**S-137. No attachment count is flat under the load `T-3b` solved.** The flatness criterion was minimised
under a *uniform* load, in which a free tile dishes exactly zero and the objective therefore tends to zero at
large count — so the crossing is set by the tolerance and nothing else. Under `C-0022`'s collar the same
criterion **saturates at 0.149 of the stroke** between 45 and 225 attachments and never reaches 10 %.
`CH-0034`.

### `T-1f` — the mean-field fluctuation corrections at `φ ≈ 0.01` (leaf `A2.1`)

**Done, verified, filed as `C-0019`**, raising `CH-0019` against the queue's own promotion rationale and
`CH-0020` against `C-0003`'s thermal-blob count.

**The answer has two halves and neither is usable without the other.** The loop expansion whose saddle point
*is* the self-consistent field is **broken** at the Gen-1 layer: `Gi = |ΔΠ|/Π_MF = 1.302` at the 10 nm design
point's mean volume fraction, `0.788` at its peak, and **0.304–1.714 across the whole window**. Adding the
one-loop term would drive `Π_int` negative below `φ** = 0.015255`. That is not a correction, it is the
signature of a broken series, and the task committed in advance not to quote a one-loop number after finding
the loop parameter above one — `C-0005`'s own discipline, applied to the other field.

**And the layer response is bounded anyway, to under ten per cent**, because `C-0011`'s central finding does
the work: at an absorbing wall `Π_int(φ(h)) ≡ 0` and the disjoining pressure is *entirely conformational*, so
`K → 0` is a computable floor rather than a singularity. Sweeping the interaction strength through the solved
layer over **four decades** moves `k_brush` at the held gap by **−9.4 %** at 10 nm and **−5.1 %** at 7 nm, and
the stroke by **+2.0 %** and **+1.4 %**. The entire three-law interaction bracket the programme carries is
worth **3.6 %** of `k_brush`.

Both `C-0016` windows **widen** — 13.4 % at 10 nm, 1.8 % at 7 nm — and `C-0017`'s 10 nm margin degrades from
≥ 1.19× to **≥ 1.07×** and stays above one. **No verdict moves.**

#### Decisions

**D-115. Answer the scoping question before any number, and file the answer as a challenge.** `TASKS.md`
promoted this task on the ground that `C-0017`'s margin "sits inside `C-0005`'s 123–214 % one-loop correction,
so … until **this** is bounded". Those are two different expansions of two different fields, acting on the two
different terms of `k_eff = k_brush + k_es`. Establishing which one the margin sits inside was made a
deliverable rather than a footnote, and the conflation was raised as `CH-0019` rather than corrected silently
in the queue.

**D-116. Do not run the one-loop-corrected SCF, and say so in the Plan before running anything.** The cheap
bound was allowed to decide it: if `Gi ≥ 1`, adding `Δf` to `f_int` produces a negative osmotic pressure and
the result is an artefact of a failed series. Running it anyway and reporting the profile would be the exact
error `C-0005` refuses to make.

**D-117. Bound non-perturbatively instead, using `K → 0` as a computable floor.** The one-loop term is
negative, so the licensed range of the interaction is `K/K₀ ∈ [0, 1]` and the worst case is its total
destruction. That converts an unbounded perturbative question into a bounded computational one.

**D-118. Keep `PegWater.thermalBlobKuhnSegments` and add a corrected companion beside it.**
`SESSION-PROMPT.md`'s rule: a contradiction raises a challenge, not an overwrite. Nothing downstream consumes
the number, so nothing changes silently.

**D-119. Use the layer's own `heightAtPressure` rather than `heightUnderLoad` or a hand-rolled root.** Two
other routes were tried and both are traps — see `S-142` and `S-143`.

**D-120. Discard a completed run rather than ship an edge with a 23.4 % grid sensitivity.** Gate 4 measured the
10 nm stroke edge at 0.34265 on `Δz = 0.4 nm` and 0.27770 on 0.2 nm. The whole run was re-done on the finer
grid and the coarse value kept only as the convergence rung.

**D-121. Report both `Gz` and `Gi`.** The bare Ginzburg parameter of the literature and the pressure ratio it
produces differ by `2√3/π = 1.1027`; quoting one under the other's name is worth 10 %.

#### What was surprising

**S-138. `Gi` straddles unity inside a single density profile.** At the 10 nm design point the mean volume
fraction gives 1.302 and the peak 0.788. The layer is below `φ**` at its mean and above it at its peak — and
it is *also* below `φ#`. **No regime label applies to this layer at all.** The geometric form of the same
statement: the correlation length is 4.215 nm against a coil of 4.916 nm, with 1.34 chains per correlation
area.

**S-139. The solved layer is nearly insensitive to the interaction, and `C-0003`'s exact exponent does not
transfer.** Measured `d ln k/d ln K` = **0.0647** against `C-0003`'s `1/(m+1) = 0.3077` — a factor of
**4.75**, converged across three node spacings. `C-0003` proved `k ∝ K^{1/(m+1)}` *exactly* for its two ansatz
profiles; the relation is a property of those profiles, not of the layer, for the same reason `CH-0010` gives.

**S-140. The thermal blob's two errors nearly cancel, and that is why it survived three iterations.** `C-0003`
coarse-grained a *pair* excluded volume linearly (`n_K` where `n_K²` is required, worth **9.671**), and used
the scaling normalisation rather than Yamakawa's exact one (worth `1/0.32992² = 9.187` the other way). The
product is **1.053**. A number that is right because two conventions cancel is not a number that is right —
the corrected blob is 126.3 Kuhn segments in one convention and 1160 in the other, and the quantity that
carries no convention at all is the swelling: **the Gen-1 chains are 6–20 % larger than the calculation
assumes.**

**S-141. The window edge that the identity predicts should move is the one that does not.** At fixed chain
length coil overlap scales exactly as `α²` — asserted as a test — so the swollen layer's lower window edge
"should" move by `1/α² = 0.87`. It moves **0.9 %**. The chain length moves against it: a swollen chain reaches
the same height with 14 % fewer monomers, and a shorter chain has a smaller coil. The widening is at the
**stroke** edge instead. **Third near-cancellation of this family in the project**, and all three have one
cause — `L₀` is specified and `N` follows, so a perturbation is absorbed by the chain length rather than by
the response.

**S-142. The cheapest place to evaluate an SCF layer is never its own floor, and this cost ninety minutes.** A
guard that checked the disjoining pressure at the layer's **saturation height** — where the layer is a melt,
the node spacing collapses to `h/24` and the contour step count goes as `1/Δz²` — ran the solver's
8000-iteration cap over a 10⁵-step contour on a single record. It did not throw and it did not converge; it
just ran. Diagnosed by `jcmd Thread.print` on the live JVM, which is the only reason it was found rather than
guessed at.

**S-143. `bracketedRoot` can leave its bracket.** The Illinois halving tests the sign of a **product**,
`atLeft * atEstimate < 0.0`. When both factors are tiny that product underflows to `−0.0`, the test reads
false, the stagnant endpoint is replaced by one of the *same* sign, and the next secant step steps outside —
observed as an evaluation a fifth of the way *below* the dry thickness. Not repaired here: three standing
claims consume it, and their result files must be re-run and diffed as part of the fix. That is `P-15`.

**S-144. `--drop <pkg>` matched nothing in this project.** The helper assumed the Maven-style
`src/main/kotlin/com/xemantic/nano/plentyofroom/<pkg>`; this repository uses the flat `src/main/kotlin/<pkg>`.
The warning went to stderr and `compileKotlin` then failed exactly as if the flag had not been passed. Fixed
in place, within the hour, by the agent that hit it.

**S-145. This task repeated, against a sibling, the very defect the coordinator had just fixed.** Its private
study runner copied back *every* changed result JSON from its snapshot rather than its own, reverting
`T-17`'s result file by up to thirteen minutes — and that stale copy is what the `T-17` commit captured.
**Reported unprompted rather than hidden**, which is the only reason it was caught; re-running the study
recovered it, and the loss was two prose findings strings where a margin had been refined from 16× to 20.2×.
No number moved. The general lesson is the one `P-12` already carries: **a snapshot is a view of the past of
every other file in the tree**, so a copy-back must be scoped to the file the run produced.

### `T-40` — what the standoff stands on (leaf `A8.2`)

**Done, verified, filed as `C-0028`**, raising `CH-0037` and `CH-0038` against `C-0025`.

`C-0025` closed `T-30` with exactly one passing joint — a duplex standing **normal** to the sheet — and named
its base as an open question in the same breath. Naming an assumption is not carrying it: all three of its
standoff constants, its anisotropy, its support margin and both its buckling loads are quoted at the
rigid-base value with no bracket on any of them, and the whole coupling design rests on them.

**The base is not a clamp, the buckling bracket runs to zero rather than to a quarter, and the base moves the
design in opposite directions.** At `C-0009`'s crossover constant `ρ_b = 0.18–0.59` over 3–10 nm, leaving
**32.0 %** of the assumed rotational restraint, **13.6 %** of the sway stiffness and **32.0 %** of the
support. A softer base releases more draw-in, so the membrane term collapses and the tangent **falls inside
the 40 pN/nm ceiling at every length** — while the Euler load collapses faster than the duty, so **a
single-crossover base buckles at all eight of `C-0025`'s lengths.** What rescues it is not a material or a
length but an **orientation**: two crossovers laid *across* the flexure give 261.2 pN·nm/rad against 27.06
laid *along* it. **The design: 45 flexures, span 31.06 nm = 91 bp, on 8 nm standoffs with a two-crossover
favourable base**, tangent 36.51 pN/nm, buckling margin 1.41× free-head; window `ℓ = 7–9 nm`.

And the finding that outranks all of them: **the motif is not in the literature.**

#### Decisions

**D-160. Apply `C-0025`'s own machinery one level down rather than model the base.** Every candidate base
traces to Chen et al.'s softened bond, so a finite-element model of the base would be a finite-element model
*of that construction*. Each new constant is `C-0025`'s at `ρ_b → ∞`, asserted as such, so every number stays
comparable with the filed one.

**D-161. Solve the buckling load as an eigenvalue, not as an effective-length factor.**
`sin u(u² − ρ_bρ_h) − cos u(ρ_b + ρ_h)u = 0` has all four textbook `K` factors as corners and both one-spring
textbook equations as limits. It also has a corner `C-0025`'s bracket does not: `ρ_b = ρ_h = 0` gives `u = 0`.

**D-162. Adopt the FREE head as the predicate and report the realised one beside it.** The beam's own end
rotational stiffness puts the realised reading at `ρ_h = 0.515`, worth 1.41× → 1.99×. The conservative one is
adopted because the head condition is not resolved and this task is a stability check.

**D-163. Declare buckling as a sixth PREDICATE rather than reporting it beside five.** `C-0025` declared five
before its run and reported buckling alongside; `T-40` exists to ask the buckling question, so `P6` is
declared in the task file before the code runs.

**D-164. Bound the off-diagonal and argue its sign; do not solve it.** The correlation is exactly `√3/2` at a
clamped base and the other-DOF-fixed reading exceeds the other-load-zero one by exactly 4. The sign argument
says the coupled joint is *softer*, which makes `P3` conservative and `P6` not. Queued as `T-65` rather than
folded in, because it would confound the base answer.

**D-165. Report the nicked base in the catalogue as structurally unavailable rather than omitting it.** A nick
preserves the helix axis, so `C-0025`'s stiffest joint cannot exist at 90°. §7 rewards saying which were
checked.

#### What was surprising

**S-170. The base moves the compliance ceiling and the buckling load in OPPOSITE directions, and the
constraint that closed `C-0025`'s window from below stops binding entirely.** At a single-crossover base the
tangent is 33.80–38.48 pN/nm over the *whole* 3–10 nm range, inside the ceiling everywhere — where the clamped
base fails it below 7 nm. And `P6` fails everywhere. **The window is not narrowed or widened: it is re-cut by
a different pair of constraints.** Written into the task file as a prediction before the code ran.

**S-171. A pinned base with a free head is a MECHANISM, not a weaker strut.** `P_c = 0` exactly. `C-0025`
quotes its buckling at both end conditions, *"a factor of exactly 4"*, and reads the binding margin at the
conservative one — but both are **clamped-base** corners, and the honest bracket has no lower bound above
zero.

**S-172. The standoff's sway IS the flexure's draw-in, so the design cannot buy one without the other.** The
head's translation in the flexure's plane has two names in this programme and is one coordinate. Holding it
against sway is `C-0023`'s *ends held axially* reading, whose 91.13 pN/nm tangent is what the whole of `T-30`
was spent escaping. **The held-head buckling reading is not available to this design at all**, and the two
requirements the standoff exists to meet are in direct opposition on a single degree of freedom.

**S-173. The base's ORIENTATION is worth 9.65× and it is the difference between a design and a mechanism.**
Two crossovers to adjacent duplexes react a base moment as a **couple**, and a couple has an **axis**: laid
across the flexure it delivers 261.2 pN·nm/rad and passes at four lengths; laid along it, 27.06 and passes
nowhere. Same two staples, same axial support. `C-0014`'s *"an anchor's orientation decides everything and its
material almost nothing"* in a new place, and free.

**S-174. The motif is not in the literature at all.** A primary-source search — every statement flagged `read
directly` / `abstract only` / `not found` — found no publication in which a duplex stands normal to a
single-layer sheet as a stand-off. Out-of-plane duplexes there are hairpin or staple-extension **overhangs**;
perpendicular helices in origami are perpendicular *within* the plane, the gridiron paper saying outright that
the crossover motif *"has been restricted … to form parallel helices"*; and every body standing on an origami
plate is held by a **pin** — *"2 nt in all cases"*, *"the hinge axes are not ideally constrained"*, *"flanked
by two ssDNA bases … for rotational flexibility"*. **The pinned base this task shows to be a mechanism is the
only base condition anyone has built.** And the one rigid out-of-plane mounting in print is **triangulated**:
Pumm et al.'s plates *"were held rigidly at this angle with a **set** of double-helical spacers"* — a truss,
not a stiffer joint.

**S-175. `C-0025`'s buckling duty is the mandate secant, and it is identical for every design in its table.**
The flexure strain-stiffens, so its own reaction at 10 nm is 1.27–1.70× larger over the window and 4.88×
larger at 3 nm. At the acceptable stroke the two coincide **exactly**, because the placement condition defines
the secant there, which is why twenty-six tests did not catch it. `CH-0037`.

**S-176. A direct measurement of exactly this problem exists, and it says the model is optimistic.** Fields,
Meyer & Cohen measured a naked duplex losing its resistance to 9 pN at 40–41 bp. Inverting Euler on their own
number gives a persistence length of **41.7 nm** — inside the 40–47 nm measured band and **25 % below CanDo's
55.5 nm model input**, exactly the direction `CLAUDE.md` records. Every buckling load here is on CanDo's `EI`
and is therefore the optimistic end. **A short duplex in axial compression is one of the few things in this
programme that has actually been measured, and nobody had looked.**

**S-177. `k_s` moves a verdict for the first time in this programme.** `C-0025` records that no verdict moves
across `k_s`'s four decades. Here the two-crossover base's whole restraint **is** `k_s d²/2`, so at `k_s/8` and
`k_s/32` the buckling margin falls to 0.92 and 0.70 and `P6` fails. `C-0020`'s derived, unmeasured
construction has become the constant that decides the design — which sharpens `T-9` considerably.

**S-178. The `+`/`.format()` trap fired three times in one file, and once it produced grammatical prose.**
Two findings printed raw `%.1f` placeholders; the third consumed the wrong arguments and reported a margin
*"falls from 4.70 to 6.28"* — an increase, stated as a fall. Caught only by reading the emitted prose, exactly
as the existing `CLAUDE.md` entry says to.

### `T-25` — the design window re-synthesised, and the correction that cancelled itself (leaf `A2.1`)

**Done, verified, filed as `C-0027`**, raising `CH-0035` against `CH-0026` and `CH-0036` against `CH-0024`.

Nine claims and ten challenges landed in one iteration and four of them were aimed at the design window.
**Three of the four live on axes an intersection cannot see, and the fourth is very nearly cancelled by the
part of the design that produced it.** The net movement over 183 grid points at three heights is **one edge,
at 10 nm, by one grid step, outward**: `σ ∈ [0.011634, 0.288540] nm⁻²`, 22.36× → **24.80×** wide. The 7 nm
window is unchanged **to the last digit** — `C-0019` widens it by one grid step and `CH-0024` narrows it by
one. No edge changes owner; coil overlap still owns every lower edge and §3's 3 nm stroke every upper one.

**On the coupling axis the two corrections that were supposed to decide the verdict run in opposite
directions and are of the same size.** `C-0019` takes the 10 nm / 2 mM stability margin from 1.194–1.424× to
1.110–1.245×; `C-0022`/`CH-0026`, carried at the operating point, takes it to 1.335–1.668×; together they
give **1.231–1.528×** — *better* than `C-0017` published. **0 of 54** states fails the mandate. And on the
bias axis the same two cancel at the fold to within the collar gradient's own difference-scheme spread, so
`C-0018`'s 1.007–1.032 is left standing rather than moved in either direction.

**The Gen-1 verdict as it now stands**: non-empty at 10 and 7 nm with both edges attributed, empty at 5 nm
with the crossing widened to 24.80×, conditional on an electrostatic model error nothing in this programme
narrows — and **0.5 mM removes the condition**, which is a specification question for NDI and not a
calculation.

#### Decisions

**D-150. Run the cheap bound first, and let it settle three of the four movers.** `C-0016`'s upper edge is the
stroke under a 100 pN **dead load** — a specified force with no field in it — so `CH-0026`'s electrostatic
enhancement cannot be an argument of it whatever its size. That one observation decided three of four
candidate movers, and it was asserted as a test rather than argued: switching the edge correction on moves no
index at any height.

**D-151. Carry `CH-0026` as a DECOMPOSITION rather than as a multiplier, because at the operating point its
level term is exactly zero.** `k_es = −|F_es|/ℓ` identically and `|F_es|` is pinned by the force balance at
`100 pN + P(g)A`, so the level cancels and only `d ln μ/dh` survives. That is cheaper than a re-run *and* more
accurate than a multiplication, and it is what turns `CH-0026` from an unfavourable correction into a
favourable one.

**D-152. Key every upstream accessor on every dimension its sweep varied, and `require` exactly one match.**
`C-0026` was caught taking the wrong record for keying on too few dimensions. Here it is not hypothetical:
`CH-0026`'s headline +14.7 % is the *resting height* of a 10 nm layer, while the operating point is its
*held gap* of 7 nm, where the same file says +10.3 %.

**D-153. Classify each new constraint by AXIS before intersecting it, and compute the classification where a
σ-resolved quantity exists.** Applied to itself, `C-0016`'s lesson also says a constraint which has been
*discharged* is invisible — and axis (i), the lateral-confinement footprint, is exactly that.

**D-154. Report the pull-in movement as UNRESOLVED rather than quoting the half that is computable.** The
operating-bias half is unambiguous and favourable; the fold half straddles zero across three difference
schemes. Quoting only the first would have produced "≥ 1.11×" — a number that reads like a result and is half
a cancellation.

**D-155. Report the residual as sub-grid, never as zero.** Three of the four non-empty edges do not move at
1.109× resolution, and the honest report is *"does not move at this resolution"*.

#### What was surprising

**S-160. `CH-0026`'s direction is backwards for every clause it was written for, and for two different
reasons.** The window's upper edge is a **dead-load** stroke and the multiplier is not an argument of it at
all; and at a **force-pinned** operating point the multiplier is absorbed into the bias, leaving only the
collar's gradient, which runs the other way. `CH-0026` reasons at fixed bias where the device is held at fixed
force. **The fourth instance in this programme of a quantity quoted at a state the device does not occupy** —
after `CH-0015`, `CH-0016` and `C-0018`. `CH-0035`.

**S-161. `C-0019` and `C-0022` are the same size and opposite sign, and each was published alone.** `C-0019`
degrades the 10 nm margin by 7 % and `C-0022` improves it by 12 %; neither claim could carry the other,
because `C-0019` predates `C-0022` by hours and says so in its own validity range. The combined answer is
better than the number either correction started from, and **no reader of either claim alone could have known
that.** This is what a synthesis task is for, and it is the second time two claims coupled in a way neither
could see alone.

**S-162. `C-0023` saved the 7 nm design window, and was filed as a `T-13` result.** `CH-0024`'s 2–13 %
shortfall is measured against a stack containing `C-0014`'s eight substrate tethers, which supply 9.4 pN of
its 10.24 pN hold-down — and `CH-0027` removed them the same day. The tethers are worth **four grid steps** of
the 10 nm window and three of the 7 nm one, where the tethered device leaves it **1.230× wide, one grid step
from empty**. A claim's consequences are not confined to the task it was written for. `CH-0036`.

**S-163. Removing a part is invisible to an intersection in exactly the way a topological constraint is.**
`C-0016` reported the lateral-confinement footprint as a cost axis. It is now not even a cost: `CH-0021` makes
the in-plane factor exactly 1 and `CH-0027` removes the in-plane tethers entirely. **A window gains an axis
when a constraint is discovered and loses one when a constraint is discharged, and an intersection records
neither.**

**S-164. The 5 nm crossing got WIDER, and that is the delivered-stroke axis working.** `C-0016`'s 13.32×
becomes **24.80×** once the compliance clause has to deliver `3.0 + d` rather than 3.0. The one iteration-4
result that can narrow anything narrows the height that was already empty by the most.

**S-165. `μ` is a function of the gap and not of the bias, which is what made the finite difference possible at
all.** At the one gap `T-3b` sampled at three biases the collar multiplier spans **0.14 %**. That accident of
the sweep is the only reason `d ln μ/dh` could be extracted from it, and it is reported as a validity
condition rather than assumed.

**S-166. `--drop <pkg>` cannot drop a package another package depends on.** `coupling` imports six symbols
from `anchoring`, so dropping `anchoring` to work around one half-written file turns one broken file into
eighty broken references. The workaround was a snapshot with the two in-progress **files** removed. The tool
needs file granularity — that is `P-16`.

### `T-65` — the standoff's coupled joint (leaf `A8.2`)

**Done, verified, filed as `C-0030`**, raising `CH-0041` against `C-0025` and `C-0028` and `CH-0042` against
`C-0017`'s theorem. It also closes `T-41`, which asked the same question one level up.

`C-0028` bounded the off-diagonal of the standoff's tip compliance — correlation exactly `√3/2` at a clamped
base, the other-DOF-fixed reading larger by exactly 4 — and then **argued** its sign: the coupled joint is
softer, so `P3` is conservative and `P6` is not. Its recommended design's buckling margin was 1.41×, so a 1.4×
softening in sway closes the window with nothing else moving. **Bounding a term and arguing its consequence
are not the same operation, and this iteration is why.**

**The bound is right and the consequence is backwards. The dropped term is not a compliance, it is a kinematic
SUPPLY.** At a flexure end the joint carries the beam's end moment and its inward tension together, and both
tilt the head inward — so the head's translation under the beam's own end moment is draw-in the standoff
supplies for free. And because that moment is **first order** in the midspan deflection where the arc-length
demand is **second order**, the supply is `Φδ` against a demand `e(δ) ≈ δ²/L`: at `C-0028`'s own design point,
**0.886 nm against 0.287 nm, a ratio of 3.09**. The term two claims dropped is three times the term they kept.
**So the coupled beam is in axial COMPRESSION over `0 < s < 9.9 nm`** — `C-0023`'s membrane term, the one that
turns the beam into a cable, changes sign inside §3's stroke — the standoff's duty at the desired stroke falls
from 5.113 to 3.313 pN, and the buckling margin rises **1.41 → 2.18×** on CanDo's rigidity and **1.06 → 1.64×**
on the measured one. The window widens from `ℓ = 7–9 nm` to **5–10 nm**, on both rigidities.

**And then the sign turns out not to be a property of the joint at all.** `Φδ` is odd where `e(δ)` is even, so
the coupled law is not odd, and mounted the other way up the same joint has tangent 44.82 pN/nm — past
`C-0023`'s ceiling at every one of the eight lengths — and margin 0.99×. **Which body carries the standoffs is
worth the difference between a 5–10 nm window and no window**, it is free to a builder, and no upstream claim
asks the question.

#### Decisions

**D-190. Solve the joint as a 2 × 2 flexibility and prove the decoupled limit before quoting anything.**
`C12 = 0` must return `c(ρ)` and `S_eff` identically, and it does — `C-0025`'s `J5-8` to **`0.0`** and
`C-0028`'s `B2` row to its published rounding. Declared as falsifier 1: without it no comparison means
anything.

**D-191. Verify Maxwell-Betti by two different QUADRATURES, not by construction.** `C12` is the tip
translation under a unit tip moment — a double cumulative-Simpson integration of a constant curvature; `C21`
is the tip rotation under a unit tip force — a single integration of a linear one. Nothing forces them to
agree. Nine `(ℓ, k_θb)` pairs, departure `0.0`. **A symmetric matrix written symmetric is not a test.**

**D-192. Predict the sign structure in the task file before the code runs.** The supply is odd and the demand
even, so a single "softer" cannot be right — one mounting must gain what the other loses. Written down as the
declared prediction, and it held.

**D-193. Report both mountings and adopt neither.** §3 does not say which body carries the standoffs.
Asserting one would manufacture a window; asserting the other would close a branch. Both tables are filed and
the gap is named as a **specification** gap, the fourth in this programme after the electrode material, its
potential of zero charge, and the loading rate.

**D-194. Declare `P7` — the FLEXURE's own buckling — in the task file, before the run.** The coupled model
puts the beam in compression, which the decoupled one never does, so the element acquires a stability
condition it did not have. Quoted against the exact braced eigenvalue rather than the chord model's own
`12EI/L²`, which is 22 % optimistic.

**D-195. Report the favourable mounting's CLEARANCE beside the predicates rather than adopting it.** It was
found while fixing the sign convention, after the predicates were declared — the same discipline `C-0025` used
for buckling.

#### What was surprising

**S-200. The term two claims dropped is three times the term they kept, and it is one order lower in the
deflection.** `C-0025` and `C-0028` both charge the joint a second-order draw-in demand and both omit a
first-order supply. Nothing in either claim's structure could have shown this, because both had already
reduced the joint to two scalars before the beam was solved.

**S-201. `C-0028`'s "the coupled joint is softer" is wrong in BOTH halves at once.** Against a net demand the
coupled axial compliance is *smaller* than the decoupled one — the joint is **2.06× stiffer** — and the effect
that actually moves the answer is not a compliance at all. The predicate `C-0028` feared for, `P6`, is the one
that improves.

**S-202. `c₀ ≡ c(ρ)` exactly, coupled or not — the off-diagonal does not touch the bending coefficient.** What
it adds is a term proportional to the axial force, so the *effective* end condition becomes a function of the
**stroke**: 124.4 at 3 nm and 92.2 at 10 nm on a nominal 92.5. `C-0025`'s discipline — "`c` is not a constant
of the joint, it carries the span" — has to be extended: it carries the stroke too. The sixth instance of a
quantity that is not well posed without the state it is read at.

**S-203. The element becomes strain-SOFTENING, so `C-0017`'s free stability margin inverts into a debt.**
`t/s` falls 1.095 → 0.757, and the assembled tangent has an interior minimum of **22.88 pN/nm at a 4.55 nm
stroke** — between §3's acceptable and desired strokes. `C-0018`'s fold margins put `|k_eff|` at
23.5–28.0 pN/nm, so the number stability is written on now sits *inside* the requirement. `CH-0042`, and it is
not resolved here.

**S-204. A sagging beam pulls its supports together, so the favourable mounting costs a CLEARANCE.** The sense
that supplies the draw-in is the one in which the midspan sags *toward* the body its bases stand on — so the
standoff length is also a travel limit: 5.31 nm at `ℓ = 8` and 7.31 nm at `ℓ = 10`. §3's **acceptable** 3 nm is
delivered at `ℓ ≥ 6 nm`; its **desired** 10 nm at no length inside `C-0017`'s envelope. Neither mounting
delivers the desired stroke, for two entirely different reasons.

**S-205. The constraint at the window's lower edge changes for the THIRD time.** `C-0025` closed it with the
compliance ceiling, `C-0028` with buckling, and `C-0030` with the 10 pN unzip allowable on the beam's own
tension at `ℓ = 4 nm`. Three claims, three constraints, one edge.

### `T-67` — the 90° routing exists, and it is the base's arithmetic that fails (leaf `A8.2`)

**Done, verified, filed as `C-0029`**, raising `CH-0039` against `C-0028` and `CH-0040` against `C-0023`.

`C-0028` had found that the motif the whole coupling rests on is not in the literature, and named the routing
as its own open question — *upstream of every number in that claim*. **A routing exists.** Both links close
covalently at **0.600 nm**, inside the measured `[0.60, 0.70] nm` phosphodiester step, with **zero unpaired
nucleotides**, and the optimum is a **scaffold excursion**: out at strand 1 bp 9, back at bp 10, chord at
−87.8° across the sheet helix.

**What a perpendicular junction cannot do is not *form* but *resist*, and the reason is a count.** A B-form
duplex has two backbones, so a duplex **end** has **two strand termini**, so a base joint has **at most two
links**, on a chord of at most `2 r_P` — lever arm ≤ **1.0 nm**. `C-0028`'s recommended base needs 1.345 nm,
and a couple goes as the square: its 261.2 pN·nm/rad is **3.34× over a hard ceiling of 78.24**. And two links
on a chord restrain **one** axis; about the chord the base keeps only 13.53 pN·nm/rad — which *is* `C-0028`'s
own `B1`, recovered to the last digit from a different construction. **A column buckles about its softest
axis**, so `P6` fails at every length and **the standoff branch closes at §3's desired stroke** (it stands at
the acceptable 3 nm, `ℓ = 5–10 nm`).

**The `E5` fallback fails too, twice, on geometry needing no constitutive law** — a `δ = r sin θ` law
evaluated at **46.9°** (exact re-solve: `t/s` = 1.549, past its own 40 pN/nm ceiling) and an arm capped by the
placement condition itself at `(c n EI/k)^(1/3)` = **9.77 nm < 10 nm**, at any hinge count. **The remedy is one
letter in that cube root.** What survives is **`E5g16`: a 12.24 nm = 36 bp guided arm on 16 antiparallel
crossovers**, tangent 33.68 pN/nm at 3 nm and 38.68 at 10 nm, 2.04 pN per crossover against a 10 pN allowable,
**no member in axial compression at all** — so the buckling predicate is vacuous — **and no 90° junction
anywhere in the design**.

#### Decisions

**D-180. The method is a COUNTING THEOREM, not a simulation.** A duplex has two backbones, so a duplex end has
two strand termini, so a base joint has at most two links and a lever arm bounded by the duplex's own radius.
No force field can add a third backbone, so an atomistic or oxDNA study could only find the junction
*additionally* frustrated — it can make the answer worse, never better. Spending days to lower an upper bound
that already fails is the wrong order, and that is the Plan section's cost justification.

**D-181. A link is a WINDOW, not a distance.** The measured intrastrand step is a pair — C3′-endo 0.6 nm,
C2′-endo 0.7 nm — so the closure objective is the residual outside `[0.60, 0.70]`, not a minimised distance.
Minimising the bare distance parked the search on the van der Waals floor at 0.350 nm, where no backbone
exists; the first run reported a "closure" that was a clash avoided by a hair.

**D-182. The adopted `P6` is the WEAK-axis reading.** Two links lie on a chord, so the couple has one axis; a
column buckles about its softest. The restrained-axis reading is reported beside it and is available only if a
*second* element restrains the free axis — which is `T-66`'s truss and costs the sway the standoff exists for.

**D-183. The phosphate radius is taken at the sourced 10 Å, not the 9 Å fibre value.** It coincides with the
duplex's own steric radius, which is not a coincidence: B-form DNA's 2 nm diameter *is* the phosphate
backbone. The 0.90 nm reading is carried as a bracket, and the 180° row as a bound no convention can move.

**D-184. `E5` is re-solved under exact rotation and REDESIGNED rather than reported as failed.** The cap
contains its own remedy: `c` = 3 → 12 lifts it from 9.77 to 15.50 nm. A *stiffer* arm does not work — a
6-helix bundle lifts the cap to 26.5 nm and puts the tangent at 105.9 pN/nm — because the bundle buys arm
*stiffness* and the stroke needs arm *length*.

**D-185. The delegated literature search was treated as a summary.** Five load-bearing quotes were re-fetched
and re-verified by hand before any of them was used — `C-0021`'s lesson, applied.

#### What was surprising

**S-190. The question was the wrong one, and the answer is better for it.** `T-67` was sent to find out whether
a 90° routing exists. It does. What a perpendicular junction cannot do is *resist*: `C-0028`'s 1.345 nm lever
arm is not a motif nobody has built, **it is a length that does not exist on the part.**

**S-191. The best INDEPENDENT-staple routing IS the scaffold excursion.** The search was given two
unconstrained sheet targets and its free optimum landed on **consecutive phosphates of one strand** — exactly
the excursion's own constraint. Nothing told it to.

**S-192. `C-0028`'s `B1` fell out of a completely different construction.** Two softened bonds on a chord, read
*about* the chord, are 13.53 pN·nm/rad and 64.71 pN/nm — an antiparallel crossover's own constants, to the
last digit. **Two bonds with no lever arm *are* a crossover.**

**S-193. `E5`'s arm is capped BELOW §3's desired stroke by the mandate itself, at any hinge count.** 9.77 nm
against a 10 nm stroke — a 2.4 % miss, and it took the whole task to notice that `C-0023`'s "most compact"
element had been evaluated at a 47° rotation on a small-rotation law.

**S-194. `α` moves a verdict for the first time in this programme.** `C-0025` records that no verdict moves
across Chen's `α ∈ [0.6, 1.2]` and `C-0028` found the same; here the base ceiling is linear in `α`, and at
`α = 0.6` the longest stabilisable standoff falls from 7.5 to 5.5 nm.

**S-195. The one published attempt at this motif is a negative result, and Rothemund names the cause.**
*"The duplex markers, because they are attached to the origami by only one covalent bond, appear to be
flexible."* That is this task's single-link routing, observed in 2006 and buried in a supplement.

### Iteration 4 — what closed, and what it cost

**Fifteen tasks closed**: `P-12`, `T-15`, `T-4`, `T-13`, `T-19`, `T-23`, `T-3b`, `T-30`, `T-17`, `T-1f`,
`T-25`, `T-40`, `T-65`, `T-67`, and `T-41` as superseded.
Twelve new claims (`C-0018`–`C-0030`), thirteen new challenges (`CH-0017`–`CH-0042`), twenty commits, every
one verified green at `HEAD` by `tools/verify.sh --committed` **before** it was pushed.

**Everything NDI asked is now answered.** `T-4` closed the last of the eight §6 tasks and `T-3b` closed
§4(g), the last of the seven §4 questions. What remains in the queue is entirely of this programme's own
making.

**The iteration's characteristic result is a *kind* error rather than a numerical one.** Six tasks closed by
finding that a quantity was the wrong kind of quantity:

- a hold-down that was a **force** only because the coupling was one-sided — it is a stiffness, and §3's own
  mandate already exceeds it 72× (`C-0023`);
- a bias ceiling that belongs to a **load line**, not to a device (`C-0018`);
- an allowable that is a function of **bonded length**, not a material constant (`CH-0029`);
- a flatness count that is set by its **load case**, and saturates (`CH-0034`);
- a concentration factor applied to the wrong **cut** (`CH-0033`);
- and an off-diagonal that is not a compliance at all but a kinematic **supply** (`CH-0041`).

**Three results reversed a sign the programme had been carrying.** The tile edge *gains* load rather than
losing it (`C-0022`); the polymer and electrostatic mean-field corrections run in **opposite** directions and
very nearly cancel (`C-0027`); and the standoff joint's coupling *helps* the predicate `C-0028` feared for.
None of the three was found by refining a number. Each was found by asking what a term was doing.

**And the branch that decides the device came back with a buildability answer rather than a mechanical one.**
`C-0028` found the standoff motif absent from the literature; `C-0029` found that its base fails on a **count**
— a duplex end has two strand termini, so its lever arm is bounded by the duplex's own radius — and that the
surviving design is a crossover-hinge flexure with **no 90° junction anywhere in it**.

**What the iteration cost.** Two defects shipped in `P-12`'s own harness fix, both found by use rather than by
review: `drop_packages` deleted a live package from the working tree, and `study.sh`'s copy-back reverted a
sibling's freshly emitted result file — the second of which then happened *again*, in an agent's private
runner, after the fix. One result file was recovered by re-running its study; the loss was two prose strings.
**Both incidents were reported by the agents that caused them, unprompted, which is the only reason either was
caught.**

### Next

The queue is longer than it was, which is the loop working rather than failing.
Ranked by what they block:

1. **`T-63` and `P-13` are questions for Kazik, not tasks** — the buffer (0.5 mM as nominal, recommended by
   five independent routes and the only margin in the repository that clears its own error) and the electrode
   material. Both are specification gaps; no calculation closes either.
2. **`T-70`** — what holds `E5g16`'s guided arm. `c` enters the arm cap as a cube root, and if the anchorage
   is nearer pinned than guided the cap falls below §3's desired stroke and **no element in the programme
   reaches it**.
3. **`T-76`** — whether a strain-softening coupling still satisfies the stability clause. `CH-0042` is the one
   challenge this iteration left **open**.
4. **`T-50`** (1–3 weeks) and **`T-9`** (days) both exceed one session, and `T-9` now blocks two claims
   because `k_s` and `α` each move a verdict.
5. `T-72`/`T-66` (the triangulated standoff, now a stability remedy rather than a rigidity one), `T-21`
   (the `φ = 0.2` crossover, on which 121 of 162 bias ceilings rest), `T-60`, `T-35`, `T-36`.

## 2026-08-14 — Iteration 5

The third parallel iteration, run like iterations 3 and 4:
several GPD loops against one working tree, coordinated from a single context window,
each owning a disjoint Kotlin package and a disjoint block of claim and challenge IDs.
`TASKS.md`, `JOURNAL.md`, `ANSWERS.md`, `CLAUDE.md`, `README.md`, `build.gradle.kts` and every `git` write
stay with the coordinator.
This section is written per task as each closes, newest last.

### Interaction with Kazik

One instruction, at the start of the session, identical in substance to iterations 3 and 4:
run the loop from the main context window, spawn subagents for queued items, extend and maintain the queue,
run independent tasks in parallel if they fit the box,
and keep going until the queue is empty or the context window nears its limit.
No questions were put back; nothing was blocking.

The three open questions carried into this iteration are unchanged and still unanswered —
the buffer (`T-63`, 0.5 mM as nominal), the electrode material (`P-13`),
and the two paywalled PDFs behind `P-6` and `P-8`.
None of them blocks; all three are specification or access gaps rather than calculations.

### `P-16` and the harness, taken first

Taken **before any science task**, and before the first agent was started,
because `SESSION-PROMPT.md` puts process blockers above cheap wins
and four concurrent agents were about to be pointed at one checkout.

`--drop <pkg>` removes a package's **main** sources as well as its tests,
so it cannot be used when your own package imports the broken one —
`coupling` imports six symbols from `anchoring`, and `C-0027` lost time to exactly that,
turning one half-written file into eighty broken references.
The fix is granularity: `drop_files` in [`tools/snapshot.sh`](tools/snapshot.sh),
wired into both [`tools/verify.sh`](tools/verify.sh) and [`tools/study.sh`](tools/study.sh) as `--drop-file`,
with `--drop <pkg>` demoted in both headers to the fallback it should always have been.

Two things travelled with it that were not asked for and are worth recording.

**The deleting helpers now have tests.** [`tools/test-snapshot.sh`](tools/test-snapshot.sh), 19 checks,
written first and failing first.
`drop_packages` has already deleted from a live working tree once (`S-94`),
and the argument that guards it is just a path;
the Kotlin side of this project does not accept an untested number,
and there was no reason the harness that protects it should be held to a lower standard.
`drop_files` additionally refuses an absolute path, a `..` escape and a directory argument —
three failure modes a *package name* cannot express, which is why the package form never needed them.

**`kotlin.daemon.jvmargs=-Xmx3g` moved into the checkout's own `gradle.properties`.**
`CLAUDE.md` has recorded the daemon OOM and its cure for two iterations,
and the cure was being applied by each agent patching its own snapshot.
Putting it in the checkout means every snapshot inherits it.

It paid for itself the same afternoon: the full suite was run three times during `P-15`,
and on two of those runs the only failures in the tree were a *sibling agent's* mid-TDD test file —
a different sibling each time.
`--drop-file` isolated each in one flag, and the authoritative answer came back first time.

### `P-15` — the root finder, and the defect nobody reported

`C-0019` raised this as a correctness fix and described it precisely (`S-143`):
`bracketedRoot`'s Illinois step test is written on a **product**, `atLeft * atEstimate < 0.0`,
which underflows to `−0.0` when both factors are tiny,
so the test reads false, the bracket is lost, and the next secant step leaves `[low, high]`.
It declined to repair it because three standing claims consume the routine
and their result files must be re-run and diffed as part of the fix.

Both halves of that turned out to be right, and both turned out to be incomplete.

**There were two defects, and the reported one is not the one that was costing anything.**

The product test is real, and the repair is the obvious one —
compare signs, never products, and carry the endpoint's sign as a flag *separate* from its residual,
because the Illinois halving mutates the magnitude of a value whose endpoint has not moved.
The *entry* test `require(atLeft * atRight <= 0.0)` fails the same way in the opposite direction,
accepting a bracket with no sign change in it, and `C-0019` did not name that half.
But a product underflows only once **both** factors are below `≈1.5e-154`,
and a direct probe of the unrepaired routine on residuals shaped like this project's —
pressures in pN/nm², lengths in nm, brackets spanning up to 30 decades —
escapes **zero** times in every case tried.
The escape reproduces at `1e-170`, and which physical inversion drove `C-0019`'s residual that small
was not re-identified and is not claimed to be.

What *was* always active, on every call and at every scale, is that **the Illinois halving was unconditional.**
Dowell & Jarratt (*BIT* **11** (1971) 168–174, read directly from the PDF rather than from a summary)
halve only in their case (ii), when the **older** endpoint is retained.
Halving on every step deflates *both* residuals once the estimate starts alternating sides,
and two deflated residuals of nearly equal magnitude and opposite sign interpolate to the **midpoint**:
the method silently becomes bisection while still paying for a secant.
Measured, on `x² − 2`: **52 evaluations against bisection's ~52, where correct Illinois takes 11.**
The doc comment's "roughly an eightfold saving in evaluations" is the entire justification
for using Illinois over the bisection `T-1` used, and it was delivering a *loss*.

**The two defects are coupled, and each hid the other.**
The unconditional halving deflates `atLeft` by `2^−n`,
which is precisely what drives a residual toward the underflow floor the sign test needs —
so the performance defect is the *amplifier* of the correctness defect.
Repairing either alone leaves the routine wrong in the other way.

**And the diagnosis closes as a theorem rather than a reproduction.**
A secant through two opposite-signed ordinates is a **convex combination** of its endpoints,
so while the bracket holds an escape is arithmetically impossible.
An evaluation outside `[low, high]` therefore *proves* the sign test misfired.
That is what turns `S-143`'s observation — "an evaluation a fifth of the way below the dry thickness" —
into a cause without needing the run that produced it.
It is asserted as a test, over five residual scales and a lopsided `1e−6 : 1` bracket.

**The re-run is the half that decides, and it produced a third answer.**

`P-15`'s acceptance predicate offered two branches: every result file byte-identical, or a number moves
and the consuming claim is challenged. The truth is the second in letter and the first in substance,
and the gap between them is the finding.

`T-1f` moved: **589 numeric fields**, relative change `8.4e−9` to `4.2e−3`, **median `9.0e−7`**.
`SelfConsistentFieldLayer.heightAtPressure` carries `HEIGHT_TOLERANCE = 1e-6`.
The file moved by **exactly its own declared tolerance**, which is the signature of a quantity
relocating inside its noise floor rather than a quantity changing.
No verdict moved, no window edge moved by more than `0.0085 %`,
and the single prose change is a percentage rounding at its second decimal (`0.34 % → 0.33 %`).
`T-16` moved in **six** fields, every one of them an `upstreamReproduction/…Departure` residual
whose *value* is `~1e−9` — numbers meant to be zero,
where this project's own rule is that comparing them relatively compares their noise.

So the tree rounds to **nine** significant digits and the solve determines about **six**.
That makes `C-0019`'s certificate — *"re-run on an independent snapshot and diffed byte-for-byte identical"* —
true, checked, and a statement about the **path** rather than the **answer**:
it certifies that nothing perturbed the iteration.
Repairing a solver perturbs the iteration, so any solver improvement is indistinguishable
from a physics change and has to be re-adjudicated by hand — which is `CH-0043`.

**One file did not fit that pattern, and it was checked rather than folded in.**
`T-1d` carries the largest movement in the whole re-run — `1.48e−2`, a per-cent-scale change in a
*stiffness*, which is not a residual, not a zero and not one ulp.
Checked against what the consuming claims actually quote, it holds:
**`strokeWindows` is byte-identical**, so `C-0016`'s window edges do not move at all —
confirmed independently by `T-2` and `T-25` both re-emitting byte-identical files —
and `C-0011`'s quoted `N(L₀)`, stroke and secant stiffness move by ≤ `4.6e−6`.
Only **122 of 10 796** fields exceed `1e−3`, and every one is `stiffnessAtSevenTenths` or
`stiffnessAtNineTenths`: the per-design-point stiffness at *deep* compression, where `k` is a
**derivative** of a rapidly varying pressure and amplifies the height tolerance by about `10⁴`.
That is the sensitivity `CLAUDE.md` already records from the other side —
*"an SCF window edge is not grid-converged where a stiffness is"* —
with the ordering reversed: here the edge is pinned and the deep-compression stiffness is not.

**And the re-run exposed a real defect that pre-dates the repair.**
`T-8`'s undefined-case record guarded a reported amplitude on `layerStiffness > 0.0` —
a sign test on a quantity `C-0003` establishes is **exactly zero** at `L₀`,
the block's own comment calling it *"a rounding-level positive, physically nothing"*.
The repair flipped one case and turned a `null` into a piston RMS of 13 637 236 nm.
But going back to `HEAD` showed **two of its three undefined cases were already wrong** —
1 172 864.7 nm and 22 522.4 nm, against a 10 nm layer.

What hid it is worth naming, because it is a new shape.
`roundForResult` applies an **absolute floor** at `1e-9`, so a rounding-level `layerStiffness`
is emitted as exactly `0.0` — which is honest —
while the quantity *derived* from it, `√(k_BT/k)`, **escapes the floor because it is large**.
The committed file therefore asserted, on adjacent lines of one record,
`"layerStiffness": 0.0` and `"unconstrainedPistonRms": 1172864.7`:
not merely implausible but **arithmetically impossible**.
**A zero-floor is not inherited by what is computed from the floored value,
and a reciprocal derivation amplifies exactly what the floor was hiding.**
Re-guarded on the physics — an amplitude is reportable only while the linearised fluctuation stays
inside the layer it is fluctuating against — which is the criterion the surrounding block already
applies to call the case undefined at all, and which needs no tolerance.

Filed as `C-0031`, with `CH-0043` against `C-0019`'s provenance line and nothing else of `C-0019`'s.
Three sibling product tests were found and deliberately **not** fixed:
all three have an `O(1)` fixed factor, so none can underflow,
and changing code that produces published results costs a re-run and a diff of everything downstream.
They are queued as `P-17` with the reachability analysis attached,
so the next agent inherits the ranking rather than the alarm.

### `T-70` — what holds `E5g16`'s guided arm, and does `c = 12` survive its own anchorage?

`C-0029` closed iteration 4 by handing the programme's only surviving output coupling to `E5g16` —
*"a 12.24 nm = 36 bp **guided** arm on 16 crossovers"* —
and named its own weakest point in its validity range:
*"a guided arm (`c = 12`) is **asserted, not designed**."*
The stake was a pass/fail: at `c = 3` the arm cap is 9.77 nm, below §3's desired 10 nm stroke,
and the programme would have had **no** element reaching it.

**The answer is that `c = 12` is never realised and never needed to be, and the reason is one that a formula evaluated at an asserted `c` cannot show.**

The arm's own boundary-value problem — near end clamped in bending, far end on a rotational spring `k_far` —
gives `c(ρ) = 12(1 + ρ)/(4 + ρ)` with `ρ = k_far r/EI`:
exactly 3 at a free far end, exactly 12 at a guided one, exactly **6 at `ρ = 2`**,
the two textbook values a factor of four apart just as `C-0025`'s 48 and 192 are.
The realised factor at the design point is **6.28–7.36**.

**But `ρ` carries the ARM.**
That is `C-0025`'s own lesson — *"the same joint is nearer a pin on a short beam and nearer a clamp on a long one"* —
and here it decides the answer, because it makes the cap a **fixed point**,
`r = (c(k_far r/EI)·n·EI/k)^(1/3)`, rather than a formula.
**A longer arm buys its own guidance.**
So the question collapses to a **count**:

- **two** links at the far end put the cap above §3's desired stroke everywhere —
  13.43 nm at the arm's own duplex-end couple, 15.18 at a singly nicked continuation,
  15.44 at a two-crossover clamp, and **10.97 nm even about the CHORD**, the axis `C-0029`'s counting theorem leaves free;
- **one** link is exactly the cantilever, 9.77 nm, and fails —
  and that is not hypothetical, it is `C-0029`'s `R3` and Rothemund's own observed failure,
  *"attached to the origami by only one covalent bond."*

**`C-0029`'s counting theorem transferred to the other end of the element, and the free axis changed sign.**
The arm's far end is a duplex end, so the theorem applies verbatim:
two termini, lever arm ≤ 1.0 nm, one restrained axis.
On the standoff that free axis was fatal, because *a column buckles about its softest axis*.
On `E5` it is harmless — `C-0029` says so itself, *"`P6` is vacuous, the arm is loaded transverse to its own axis"* —
so the same theorem closes one branch and leaves the other open,
and the difference is the load path rather than the joint.
Better still, the chord is a **diameter of the arm's own cross-section**,
so the designer *chooses* the axis with the helical phase:
worst case half a base-pair quantum, 7.0 % of the couple.

**And the surprise: the composition and the boundary condition are not independent.**
`C-0023`'s `1/k = r²/(n k_θ) + r³/(c EI)` charges the hinge the *whole* tip moment `F r`.
A guide carries part of it, `M_far = F r ρ/(2(1+ρ))` — **so a guide relieves the hinge**.
Solving the two-spring beam exactly,
`c(ρ_n, ρ_f) = 12(ρ_nρ_f + ρ_n + ρ_f)/(ρ_nρ_f + 4ρ_n + 4ρ_f + 12)`,
whose four corners are the four textbook cases including `(0,0) = 0` — a mechanism, not a weaker beam —
shows the series composition is **exact at exactly one corner, `ρ_f = 0`, and it is `C-0023`'s own corner**.
`C-0029` changed `c` without changing the composition `c` belongs to.
Read on the boundary-value problem its own `c = 12` describes,
`E5g16` assembles to **54.61 pN/nm against the 33.3333 mandate** —
**1.64× over-placed and past its own 40 pN/nm compliance ceiling at the secant**.
That is `CH-0044`.

**Two errors run opposite ways and very nearly cancel, which is why the verdict survives an assertion that does not.**
The realised end condition is *softer* than asserted (6.28 against 12)
while the composition it was solved with is the *soft* reading (retaining 0.607).
`C-0029`'s 12.24 nm lands inside the corrected bracket **11.03–12.50 nm (32–37 bp)**,
every reading clears the 10 nm stroke, every reading sits below the ideal guide's 15.50 nm cap,
and the realised design's tangent (33.56 / 36.78 pN/nm) is *better* than the asserted one's.

**A result that was not anticipated: the dominant compliance term has changed sides.**
Leaf `A8.2` asks for it by name, and `C-0023` answered *"92.5 % is the hinge"* for its one-crossover `E5`.
At 16 crossovers with a realised anchorage the **arm** carries **58.5 %**.
The element is named after the term that no longer dominates it.

Filed as `C-0034`, raising `CH-0044` against `C-0029`'s `E5g` composition and nothing else of `C-0029`'s —
its arm, both tangents, its rotation, both ceilings, its two-terminus ceiling and its chord reading
all reproduce here to ≤ 2.8e−9.
Three tasks queued from it: `T-79` (the large-rotation two-spring elastica, which is the 13 % the bracket is wide),
`T-80` (whether the arm can end as a singly nicked continuation, worth 13.43 → 15.18 nm of cap),
and `T-81` — **whether a 16-crossover hinge line exists on a 40 nm tile at all**,
which `C-0023`, `C-0029` and `C-0034` have all taken as given
and which this task shows is load-bearing, because **8 crossovers do not close**.

Two smaller things worth recording.
`C-0029`'s gate-5 prose quotes the guided ceiling as *"15.5005"* where its own result file says **15.5029478**;
a transcription slip, and no design table used it.
And two sibling agents were mid-TDD in the same checkout throughout —
`FlexureMountingSense` for most of the iteration, `crossover/ConcentratedCrossover` by the end —
so every run here went through `tools/verify.sh --drop-file` on their sources.
`P-16`'s new flag did exactly the job it was added for, on its first day, twice.

A third concurrency lesson arrived unasked: **three agents took `C-0032` within 91 seconds**,
each having re-listed `gpd/claims/` and found the slot empty.
Re-listing does not prevent the race, and with three colliding the agent that notices
has to move **past** the next free number or collide again — this claim is filed as `C-0034`.

---

## Iteration 5 — `T-75` and `T-78`: which body carries the standoffs, and what sits under the midspan

Taken together, because they are two halves of one question.
`C-0030` had closed with an unusual admission: the coupled flexure's law is **signed but not odd**
(`Φδ` is odd where the arc-length demand `e(δ)` is even),
so one sense of the midspan deflection relieves the beam and the other loads it —
`ℓ = 5–10 nm` with a 2.18× buckling margin against 42–61 pN/nm and no admissible length at all.
It named the deciding variable *"which body carries the standoffs"*,
declared it free to a builder, and filed it as **a specification gap, the fourth in this programme**.

The session prompt asked the right question about that: a specification gap is only a gap if the choice is
genuinely not decidable from what is specified plus what is buildable.
**It is decidable, and the variable was misnamed.**

### The cheap bound settled it, and it is one line of kinematics

The flexure's midspan is tied to one body and its ends stand on standoffs rooted in the other.
So the midspan's deflection *relative to its own ends* **is** the change in the two bodies' separation, and

&nbsp;&nbsp;&nbsp;&nbsp;`dδ/ds = (v_base − v_driven)/n`, with `v_TILE = −1` (§1: the bias pulls the tile down),
`v_SUPERSTRUCTURE = 0`, `n = ±1`.

Exactly `±1`, and it contains **no length** — asserted over 4 mountings × 4 standoff lengths × 3 tie lengths.
And it is a **product of two binaries**: the base body *and* the direction the standoffs point out of it.
Of the four mountings exactly two are favourable, **one with each body and one with each normal**,
so naming the body alone predicts the sign no better than a coin.
`C-0030` named half the variable.

The same statement three more ways, each asserted rather than assumed:
favourable ⟺ the midspan's tie **crosses the standoff base plane**
(checked by a second construction, comparing three `z` coordinates of the built stack)
⟺ the flexure is **outboard** of its own ground rather than **inboard** between the two bodies
⟺ **the standoff is in compression**.

That last equivalence is not bookkeeping, and it is `CH-0046`:
the beam's end shear acts along the standoff's own axis, so **a standoff in tension does not buckle**.
`C-0030`'s adverse buckling margins — 2.53 … 0.99 … 0.75 — are charged against a member
its own kinematics puts in tension at every one of those eight lengths.
No verdict moves (the adverse mounting fails on `P3`, which owns that column),
but the number is quoted where it will be read, and it makes one failure look like two.

### Then the expensive part went where the cheap bound pointed: buildability, not sign

Four mountings, three filters.

**§3 turns out to say something.** Its parameter table gives an effort point *"~20–25 nm above the electrode"*
and a ~10 nm tile on 5 / 7 / 10 nm layers — and the band is exactly as wide as the layer-height range,
which **forces** a constant attachment height and fixes it at 5 nm (`C-0012`'s reading, reproduced here at both ends).
The inboard topologies stack the standoff and the tie in series between the two bodies,
so their effort point **cannot come closer to the tile than `ℓ`**; the outboard ones fold the tie back past
their own base plane and have no floor at all.
Read loosely instead — the effort point merely lying in `[20, 25]` — the inboard ceiling is `ℓ ≤ 10 / 8 / 5 nm`
at the three layer heights.
**The two readings agree at the 10 nm layer, which is where `C-0016` and `C-0027` put the whole design window.**

**The polymer layer kills the other favourable mounting.** `Td` — standoffs pointing down off the tile — puts the
flexure inside the actuation gap: the 45-beam, 90-standoff array occupies **37–85 % of the layer's own volume**,
and at §3's 5 and 7 nm layers the beam sits **at or below the electrode surface**. 0 of 9 admissible,
and the tie would have to perforate the tile 45 times besides.

**So the survivor is unique**: `Su` — standoff bases on the **output superstructure**, standoffs pointing away
from the tile, flexure outboard above it, each midspan tied back **down through it** to the tile.
A consequence worth stating on its own: **the tile then carries no out-of-plane element at all**,
only `C-0015`'s 45 tie attachments — the scheme `C-0026` already validated.
The whole standoff-and-base problem (`C-0028`, `C-0029`, `T-66`, `T-68`, `T-72`) moves onto a body that
need not be a single layer. `C-0029`'s two-covalent-link ceiling is **not** relieved by that —
it is a property of the standoff duplex's own end — but `T-68`'s compliant-sheet worry is.

### `T-78` fell out of the same geometry, and its answer is sharper than "a design choice"

The body under the midspan is the standoff-carrying body **by construction** — the favourable sense is *defined*
by the driven body lying on its far side. So `C-0030`'s *"real if the body is the tile, a design choice if it is
the superstructure"* is superseded in both halves: the ceiling is real whichever body it is.
And the body cannot be imperforate either, because the tie must cross it at exactly the place the midspan
descends toward: **the aperture the tie needs and the clearance the midspan needs are one feature.**
So the question is not *"is there a ceiling"* but *"how big is the hole"*.

Answered by integrating `C-0025`'s beam once more —
`w(u)/δ = (24u + 12ρu² − 16(2+ρ)u³)/(8+ρ)`, which reduces to the pinned and clamped textbook shapes,
has end slope `24/(8+ρ)` (the beam's own `Lθ₀/δ`), and returns `c(ρ)` at midspan:

- at §3's **acceptable 3 nm** stroke and `ℓ ≥ 6 nm` the cost is **exactly zero** — no penetration, no slot;
- at §3's **desired 10 nm** and `ℓ = 8 nm` the midspan goes 4.69 nm past the contact plane and the beam demands
  a slot **18.37 nm long — 57.7 % of its own span, 54 bp** — which over 45 paths is **2223 nm², 1.39× the whole
  tile footprint**.

**The escape from the ceiling is a hole bigger than the device.**
And a floor remains at every stroke: `C-0023`'s two-sidedness makes the tie a *duplex*, so 45 duplex-omission
holes = 326 nm², 20.4 % of the footprint.

### The pre-bow escape, priced and rejected — 12 of 12

Building the flexure already sagging toward its base plane puts the first `δ₀` of stroke on the favourable limb
*inside an adverse mounting*, with the span re-placed on the incremental secant. It genuinely recovers the compliance.
It costs a rise of **4.08–16.66 nm (12–49 bp)** — larger than the stroke it protects, 1.4–5.6× —
for a preload of **150–225 pN**, i.e. **1.5–2.25× §3's entire target force** pressed onto the layer before any bias.
And at the desired stroke **no rise up to 30 nm** closes it.
`C-0023`'s discipline upheld in a new place: a preload is a mounting offset, i.e. a *length*,
and here the length wanted is entirely buildable and entirely unaffordable.

### What surprised us

**That §3 answers a question nobody had asked it.** Three claims had read the effort-point band as a consistency
check on the stack's heights (`C-0012` did exactly that). Read as a *constraint on the coupling's own height budget*
it excludes an entire topology — and it does so at the design height on both of its readings.
The number was in `ActuatorGeometry` since iteration 3.

**That `C-0030`'s "no window at any length" was a statement about an interval.** The adverse tangent falls
monotonically with the standoff and meets `C-0023`'s ceiling at **13.16 nm** against 3.48 nm favourable.
It is still a fail — 13.16 nm is outside `C-0017`'s envelope — but the falsifiable form is a length, not an absence.
That is the second half of `CH-0045`.

**And `CLAUDE.md`'s `*/`-in-KDoc trap fired again**, on `x*/L` this time, one iteration after it was written down
for `h*/L₀`. Twenty-odd syntax errors pointing at a line 8 lines below the comment. Entry extended.

Filed as `C-0035` — after two number collisions in one session: `C-0032` was taken by the `T-76` agent and
`C-0034` by the `T-70` agent while this claim was being written, both between one `ls gpd/claims/` and the next.
Raising `CH-0045` (the mounting sense is not a free binary) and `CH-0046` (a standoff in tension does not buckle),
both against `C-0030`, whose every number reproduces here to ≤ 1.1e−4.
Two tasks queued: `T-95` — **may the superstructure be perforated?**, carried to the open questions as item 6,
because it is worth §3's desired stroke and no calculation closes it — and `T-96`, `T-31` with a plan-view
constraint attached, since 45 flexures of ~32 nm span do not lie side by side in 40 × 40 nm and every aperture
area here is quoted against the tile footprint as a *scale*.

### `T-76` — does a strain-softening coupling still satisfy `C-0017`'s stability condition? (`CH-0042`, the one challenge iteration 4 left open)

**Answer: NO at 2 mM, YES at 0.5 mM, and that is a design decision rather than a fix.**
Claim [`C-0032`](gpd/claims/C-0032-softening-coupling-stability.md).
`CH-0042` is **UPHELD** on both of its horns, and a new challenge — `CH-0047` — is raised against the
challenge's own prescription for reading the number.

**The method was to substitute, not to re-implement.** `C-0018`'s `EquilibriumPath` already takes its load
as an arbitrary function of the stroke, so `C-0030`'s nonlinear reaction law drops into `C-0018`'s solver
untouched and the comparison is state by state: 216 fold searches over the same
`(3 heights × 6 layer models × 3 buffers)` grid, on four load lines all **placed** at 100 pN over §3's 3 nm.

**S-196. The placement clause is discharged exactly, so every difference lives in the ceiling.**
The assembled secant matches 33.3333 pN/nm to `1.998e−15`, and the located operating bias `V*` is
**identical across all four load lines at 144 of 144 comparisons, to a departure of exactly `0.0`**.
That identity is what makes the rest a comparison of one device rather than of four —
and it is the reason a coupling can satisfy §3's force-and-stroke clause and fail its stability clause.

**S-197. The fold moves in its STROKE, not in its bias, and a reader watching the bias would report a
rounding error.** At 10 nm / 2 mM the softening line drops the pull-in bias 0.7–1.8 % (0.1300–0.1836 →
0.1285–0.1804 V). Meanwhile the fold's own stroke walks back from **3.41–4.13 nm to 2.80–3.17 nm**,
**through §3's 3 nm target at two of six models**, and the bias margin collapses from `C-0018`'s
1.007–1.032 to **1.0000–1.0019**: at `alexander-box(des-Cloizeaux)` the pull-in bias and the operating
bias agree to four decimals. **The device is placed on its own fold.** `C-0018` warned that a bias below
the pull-in bias is not sufficient and that the target stroke must also lie on the stable side; this is the
first design point where both tests fail together, and the softening coupling is what did it.

**S-198. `C-0017`'s theorem is confirmed exactly where its premise holds.** The strain-*stiffening*
decoupled reading of the same design (`t/s` = 1.095) loses **0 of 54** states against the affine mandate,
folds at fewer of them (8 against 11), and *raises* the 10 nm / 2 mM margin to 1.020–1.774 — three of its
six models lose the fold entirely. **The theorem is right; `C-0030` moved outside its premise, which is
what `CH-0042` said.** The softening reading loses **7**.

**S-199. The escape `CH-0042` named does not exist inside the design space.** The adverse mounting's
assembled tangent is **42.38–61.04 pN/nm** across `C-0017`'s whole `ℓ = 3–10 nm` envelope, against
`C-0023`'s 40 pN/nm ceiling — **1.06–1.53× past it at 0 of 8 lengths**, and the best it reaches is still
6 % over.

**S-200. A third route nobody had named lands 2.2 % short, and what shuts it is an allowable two claims
upstream.** The favourable mounting's tangent minimum is **non-monotone in the standoff length**:
22.87 pN/nm at the recommended 8 nm, **27.30 at `ℓ = 5 nm`** — the bottom of `C-0030`'s own window — which
clears five of the six 2 mM stability floors and misses the sixth (27.91) by 2.2 %. `ℓ = 4 nm` reaches
28.71 and clears all six, and is excluded by `C-0030`'s `P4` alone: the beam's tension at the desired
stroke against `C-0006`'s 10 pN unzip allowable. Queued as `T-76a`.

**S-201. `CH-0042`'s own prescription is not well posed, and the run found it by accident.**
Minimising the tangent over `[0, 10 nm]` ranks the *adverse* mounting — 44.82 pN/nm over the range the
device actually uses — at **23.51**, within 2.8 % of the softening element it is meant to remedy. The
reason is physical and complete: at zero stroke the reaction, the layer load and the bias are all zero, so
`k_eff = 0` and the stability requirement is **identically zero** there, while a stiffening flexure's
membrane term has not switched on. **An extremum silently imports the endpoints of its interval.**
`CH-0047`, `T-76b`; no verdict in `C-0030` or `C-0032` moves, because the softening element's minimum is
**interior** (4.555 nm) and is the same number on both ranges.

**What it cost.** Gate 5 reproduces `C-0030`'s design table to ≤ 1e−3, `C-0028`'s to ≤ 1e−3, `C-0018`'s
pull-in band at 10 nm / 2 mM to its own published rounding, and `C-0017`'s stability floors and `V*` to
2.417e−3. The tangency identity `k_c(s) + k_eff(s) = 0` holds at **1.167e−5** over 38 interior folds with
`k_c` the element's analytic tangent at that stroke and `k_es` a central difference of a full field
re-solve — 0 of 216 folds are boundary maxima. Nineteen new gate-named tests in a new `stability` package;
suite green on `tools/verify.sh` with a sibling's mid-TDD `GuidedArmAnchorageTest.kt` dropped from the
snapshot. One full-suite run reported a single failure that did not reproduce on an immediate re-run and
was not named in the output — recorded here rather than explained away.

## Iteration 5 — `T-21`: the crossover is a family, and the exponent it guards was never the layer's

**Task.** `T-21`, the semidilute→concentrated crossover for *this* layer, replacing `C-0002`'s cited
`0.2–0.3` band. `C-0018` had made that band load-bearing at **121 of 162** states, so the usable bias
of the whole device rested on a number nobody here had derived.
Filed as [`C-0036`](gpd/claims/C-0036-concentrated-crossover.md);
raises [`CH-0048`](gpd/challenges/CH-0048-the-good-solvent-premise-was-checked-on-monomers.md) against `C-0007`
and [`CH-0049`](gpd/challenges/CH-0049-the-cited-band-is-a-reduced-density-and-the-fit-range-is-wrong.md) against `C-0002`.

**What was done.** A closed-form crossover family derived from `C-0002`'s own measured parameters,
`φ_c(n) = (v_K/b³)·n^(−1/2)`, `n` being the Kuhn segments the correlation blob must keep; the des
Cloizeaux window `(φ*, φ**)` evaluated at all 18 Gen-1 chains and across the `2 × 2` of thermal-blob
normalisation × excluded-volume route; and `C-0018`'s 162 bias ceilings re-read at ten candidate
crossovers on `C-0018`'s own `EquilibriumPath` + `PoissonBoltzmannGap` pipeline. The **pull-in** bias
cannot depend on the crossover, so it was read from `C-0018`'s result file and the rebuild graded by
reproducing `C-0018`'s own `φ = 0.2` ceiling — **4.5e−9** over all 162 states.

**S-202. The crossover the whole band was guarding does not exist for this material, and the proof is
one line of arithmetic.** The des Cloizeaux exponent needs a chain longer than a thermal blob. The Gen-1
chains are `N_K = 21.6–120.4` Kuhn segments; the measured excluded volume puts `g_T` at 126 (scaling) to
1160 (Yamakawa). **`φ**/φ* = √(N_K/g_T)` exactly** — the material prefactor cancels between the two edges
because both are `φ_c(n)` at a different `n` — so the window is non-empty *if and only if* the chain
exceeds a thermal blob, and it is empty at **18 of 18** chains. The answer to "at which `φ` does the
exponent stop" is that it never starts.

**S-203. The cited `0.2–0.3` band is the right expression read on the wrong segment, twice.** The derived
family runs `0.0041` to `0.63` and **nothing in it lands in 0.2–0.3**. Two constructions do:
`v_m/v₀ = 0.203`, which is the floor of the band to three digits and is what `C-0007`'s parameter sheet
reports as *"the thermal-blob volume fraction"*; and `1 − 2χ = 0.257`, which is Rubinstein & Colby's
eq (5.36) combined with their eq (5.1), i.e. the Flory-Huggins lattice site taken to *be* the monomer.
Both identify the Kuhn length's cube (1.331 nm³) with the monomer volume (0.0604 nm³). This is `C-0002`'s
`a`-trap for the third time, and `C-0001` introduced the band in the same sentence as its first
appearance.

**S-204. The derived number is LOWER than the incumbent, and the device is worse for it.** Read as a
regime ceiling at `φ_c(1) = 0.141` the coupled margin falls from 0.563–2.464 to **0.168–1.660**, twenty
coupled states drop below unity instead of fifteen, and **7 nm loses its clearance** — two of its twelve
0.5/2 mM states go under 1.0 where the worst was 1.18. Read at the *derived* `φ** = 0.0125` it is not a
ceiling at all: the criterion is violated at the **resting height at 162 of 162 states**, so no bias
whatever would be usable. That reductio is the finding: **the regime reading is not a role this number
can play, and it never was — 0.2 was only a worse estimate of the same quantity.**

**S-205. The number `C-0018` actually consumes is on a different axis, and there it saturates.** The
layer's constitutive law is a *fit*, and a fit needs data, not a blob; its validity ceiling is the range
over which it was measured. At `φ = 0.49` (PEG-8000) or `0.63` (all twelve) the crossover binds at
**0 of 162** states, `C-0005`'s 1.46 nm correlation band takes over at 99, and **the two values give
identical censuses** — above `φ ≈ 0.4` the crossover gap has already fallen below 1.46 nm everywhere.
**Resolving this number further buys nothing.** `C-0018`'s 1.007–1.032 pull-in margin at 10 nm / 2 mM,
`C-0016`/`C-0027`'s window edges and `C-0017`'s coupling do not move under any candidate.

**S-206. The falsifier nearly fired, and the honest answer is 3 of 4 corners.** This project has **two**
routes to the excluded volume differing by 2.5× — `C-0003`'s `A₂` osmometry and `C-0007`'s Flory-Huggins
`χ` — and two thermal-blob normalisations differing by 9.19. In three corners the window is empty at every
chain. In the fourth (`χ` route, scaling `g_T`) `g_T` falls to **19.5**, below every Gen-1 chain, and the
window **exists**, up to 2.48× wide. It is defeated by a different argument: the layer sits at 1.0–4.2× its
upper edge at rest and 1.5–10.4× at the 3 nm stroke, so it is never entered. Reported as a corner rather
than averaged away — *a claim that a window is empty must say where it is not.*

**S-207. Delegated literature moved three numbers, and two of them were in a standing claim.**
The fit range of the adopted equation of state is **1.5–67.5 wt %**, not `C-0002`'s "0–50 wt %" — the paper
states no range, and the source data it names (recovered from the Wayback Machine, twelve files for the
twelve molecular weights of its own Fig. 1) run to 67.5 wt % on PEG-600 and 54 wt % on PEG-8000. Confirmed
by measuring the paper's own Fig. 1 against its axis and by Marsh's independent fit of the same data.
The **DOI in `C-0002` does not resolve** (`10.1021/jp8072429`; the correct one is `10.1021/jp806893a`), and
the paper's full title carries *"in Good Solvents"* — a premise `C-0003` establishes this material does not
meet. Separately, Hansen et al. (2003)'s *measured* des Cloizeaux onset for PEG-5000, read directly, is
`φ = 0.07–0.09` in **their** reduced convention, i.e. **0.10–0.13 physical** — above the Gen-1 layer for
most of the design space, so the measurement and the blob argument agree from opposite sides.

**What it cost.** Twenty-one gate-named tests in a new `crossover` package, written first; a 70-second
study emitting `gpd/results/T-21-concentrated-crossover.json` with 1620 ceiling cells. Gate 5 reproduces
**two printed textbook statements exactly** — R&C eq (5.36) `φ** = v/b³` in the reduced convention, and
their athermal `φ** ≈ 1` — plus `C-0018`'s ceiling to 4.5e−9, `CH-0020`'s corrected thermal blob to 1e−12
and the window identity to 1.5e−16. Full suite green on `tools/verify.sh` with five siblings' mid-TDD test
files dropped from the snapshot: `SofteningCouplingStabilityTest.kt`, `GuidedArmAnchorageTest.kt`,
`CollarCorrectedFieldTest.kt`, `CollarMultiplierTest.kt`, `TriangulatedStandoffTest.kt`.

## Iteration 6 — `T-72` (covering `T-66`): the truss's cost was priced on the wrong axis

**The task, as inherited.** `C-0028` raised `T-66` as a *rigidity* question and `C-0029` re-scoped it into
`T-72` as a *stability* one: a duplex end has exactly two strand termini, so a base restrains one axis and
leaves the orthogonal one with `2 k_bond,θ` = 13.53 pN·nm/rad, **a column buckles about its softest axis**,
and the standoff branch closes at §3's desired 10 nm stroke. Both claims priced the remedy in one sentence:
*"a triangulated head cannot sway, and sway is the draw-in"* — `C-0028`'s own identity, and `C-0030`'s
finding that the draw-in is a first-order **supply** `Φδ` rather than a compliance. Two requirements in
direct opposition on one degree of freedom, and a whole design window riding on it.

**S-208. The opposition is an identity about one coordinate applied to a failure on another.** The draw-in
lives on `(u_x, φ_y)` — `x` being the flexure's own axis. The buckling that closed the branch lives on
`(u_y, φ_x)`, because the design lays the base chord so that the *strong* axis is the loaded one. They are
orthogonal. A frame couple `k_a Σd_i²` is a **rank-one tensor on the leg offsets**, so for a two-leg row of
separation `w` at azimuth `θ`,

&nbsp;&nbsp;&nbsp;&nbsp;`Σx_i² = (w²/2)cos²θ`, &nbsp; `Σy_i² = (w²/2)sin²θ`, &nbsp; **`Σx_i² + Σy_i² = w²/2`, residual `0.0`.**

**The truss has one budget of frame couple and the azimuth spends it.** At `θ = 90°` the loaded plane
inherits **exactly zero** — asserted as `k_frame,loaded == 0.0`, not as a small number. That was written
into the task file as a pre-registered prediction, with the falsifier *"a leg azimuth at which both hold
nowhere, or one at which the loaded-plane frame term is not zero for a collinear cross row."* Neither fired.

**S-209. Two legs across the flexure axis buy 6.71× for 23 % of a supply that had 3.75× of margin.** At
`ℓ = 8 nm` on `C-0029`'s realisable hard-chord base and `C-0030`'s coupled beam: the adopted critical load
goes **1.46 → 9.77 pN**, the governing plane moves from *free* to *loaded* — i.e. **`C-0029`'s
restrained-axis reading is restored, which is precisely the condition `C-0029` named** — the draw-in supply
falls from 3.75× to **2.90×** the demand (still compression at the placement point), and the tangent moves
25.20 → **26.09 pN/nm** against a 40 pN/nm ceiling. **The window is `ℓ = 5–10 nm`, all nine predicates, on
CanDo's rigidity and on Fields et al.'s measured one**, where the single standoff fails `P6` at every length
(margin 0.40–0.57) and buckles at a **3.05–7.02 nm** stroke. Laid **along** the axis the same two legs cost
63 % of the supply, add 31 % to the tangent **and still fail `P6`** — worse on every axis at once, because
they spend the budget on the plane that was already strong.

**S-210. The cost is the leg COUNT, not the leg SPACING, and this was not obvious.** `L2a6`, `L2a8` and
`L2a12` — cross rows at 2.04, 2.72 and 4.08 nm — have **bit-identical** span, tangent, `Φ` and
supply-to-demand ratio, because `Σx_i² = 0` at every separation. The whole draw-in cost of the truss is two
flexibilities in parallel; the triangulation itself is free in the loaded plane. So the separation is spent
entirely on the free plane, up to the point (between 6 and 8 bp) where the loaded plane becomes the minimum
and further spending buys nothing at all.

**S-211. `P9` was not anticipated, and it is what makes 90° *uniquely* optimal rather than merely best.** A
row with `Σx_i² > 0` reacts part of the head **moment** as an axial **couple**, so its outermost leg carries
more than its share — and the per-leg check fails at **12 of the 12 azimuths below 90°**, including ones
with ample *total* margin. `C-0020`'s lesson one level up: equilibrium bounds the sum on a cut, never the
per-member peak. Only the exact cross row escapes both costs, and it escapes the second one identically.

**S-212. The literature's *"set of double-helical spacers"* is exactly two, and each is held by ONE covalent
bond per end.** `C-0028` and `C-0029` both lean on Pumm et al.'s inclined plates as the one rigid
out-of-plane mounting in print. Re-fetched and re-read here: the Methods say *"a set of **two** spacer
oligonucleotide strands was added … to mount the obstacles on the triangular platform"*, and the SI strand
table has `spacer1_01/02`, `spacer2_01/02`, `spacer3_01/02` — three obstacles, six strands — plus two
universal complements of **exactly 39 nt**, i.e. exactly the duplex region, carrying **no flank**. So each
39 bp spacer's complement terminates at both ends and the spacer strand's own backbone is the single
continuation into the plate and into the platform: **one covalent link per end — `C-0029`'s `R3` ball
joint, and Rothemund's own observed failure.** The rigidity the paper reports therefore belongs to the
**pair**, not to either joint. **That is a frame couple, and it is this iteration's mechanism, already
built.** The paper says nothing whatever about how the two are arranged — the word *"spacer"* occurs
exactly twice in the whole article and there is no caDNAno figure of the obstacle — so the azimuth finding
has no published precedent to agree or disagree with.

**What it cost.** A new `anchoring/TriangulatedStandoff.kt` — a layout algebra, a two-link base carried on
**both** its axes at once, an assembled 2 × 2 and a two-plane critical load — plus a study emitting
`gpd/results/T-72-triangulated-standoff.json`. **26 gate-named tests, written first and watched fail**;
310 in `anchoring`, **1208 in the suite, 0 failures**. Gate 2 is the strongest part: one leg with no offset
reproduces `C-0030`'s single standoff **entry by entry**, `n` legs with no offsets are `C/n` **exactly**, a
rigid fully triangulated head sends `C12 → 0` and `C11` to the rotation-fixed sway **exactly 4×** — so
`C-0028`'s intuition is recovered as the limit it *is* correct in. Gate 3 asserts Maxwell-Betti on the
**assembled** object between two independently integrated off-diagonals (departure < 1e−12 over 27 cases)
and the conservation identity at every azimuth. Full suite green on `tools/verify.sh` with a sibling's
half-written **main** source `actuator/CollarEquilibriumPathStudy.kt` removed by `--drop-file` — a package
drop was unusable, since everything imports `actuator`.

**Filed as `C-0037`, raising `CH-0050`** against `C-0028` and `C-0029`: the truss's cost was priced on the
sway coordinate when the failure was on the orthogonal one. **No number in either claim fails to
reproduce** — `C-0029`'s ceiling and weak-axis critical loads and `C-0030`'s whole `B2` design to ≤ 2.6e−3,
which is their own published rounding. `C-0036` and `CH-0049` had been taken by concurrent agents between
the assignment and the writing, so the numbers moved up by one.

**What is left, and it is not small.** The recommended design needs **two** 90° junctions 6–8 bp apart on
one sheet duplex where `C-0029` searched for one (`T-97`); a cap nobody has designed; and 180 standoffs
whose plan view is `T-96`. **This claim reopens a branch; it does not argue the branch should be taken** —
`C-0034`'s `E5a16` clears the same stroke with no 90° junction anywhere, and choosing between them is
`T-98`.

### `T-60` — the collar on the equilibrium path, and the sign rule nobody had looked for (leaf `A7.4`)

**Done, verified, filed as `C-0033`**, raising `CH-0051` against `C-0027`. It also closes `T-62`.

`C-0027` closed iteration 4 with one open item and named the calculation that would shut it: `C-0018`'s
1.007–1.032 pull-in margin stands, *its movement unresolved*, because `d ln μ/dh` existed only as a finite
difference between gaps that `T-3b`'s sweep had visited **at five different biases** — 0.0133–0.0226 nm⁻¹
over three schemes, a 1.7× spread that left the coupled tangent at the fold running −2.5 to +4.0 pN/nm.

**The answer: `d ln μ/dh = 0.01763–0.02011 nm⁻¹` at the 10 nm fold gaps, positive at every gap of both
sweeps, converged to 0.11 % in the mesh and 1.6 % across a 6× range of difference step.** Carried onto the
path it moves every fold to a deeper stroke; at 10 nm / 2 mM the margin rises at all six layer models, and
at four of the six **pull-in stops being the binding ceiling at all**.

#### Decisions

**D-196. Solve `μ` at FIXED APPLIED BIAS, and take the denominator from the solve's own centre-line.**
`k_es` is a derivative at fixed applied bias, so the Stern series is inverted per gap and the *bias* is what
is held. The denominator is the 2-D solve's own centre-line load rather than a separate 1-D solve: the two
agree to 0.03–0.1 %, and 0.1 % of noise in a ratio differenced over 0.5 nm is 15 % of the gradient. The
independent 1-D solve is kept as a **gate** instead, and agrees to 2.9e−6 – 1.0e−3.

**D-197. Carry a multiplier, not a re-run — but run THREE variants so the decomposition is measured.**
`μ ≡ 1` (which must reproduce `C-0018`), `μ ≡ const` (level only) and the solved `μ(h)`. `CH-0035` *argues*
that the level cancels at a force-pinned point; the constant-`μ` variant **measures** it: the level is worth
0.26–0.28 % of the margin and the gradient 1.06–1.22 %, so the gradient is 3.8–4.8× the level, and the
level's residue is exactly the second-order `ℓ(V)` shift `C-0027` modelled as `decayLengthShift`.

**D-198. Interpolate `ln μ` with a cubic Hermite on parabolic node slopes, not a spline.** `ln μ` changes
sign inside the sampled range and an oscillating interpolant would put structure into the one quantity the
task exists to measure. Parabolic slopes make the interpolant's node derivative **exactly** the tightest
central difference, so the number reported and the number used are one object.

**D-199. Clamp outside the solved gap range and COUNT every clamped evaluation.** A fold search necessarily
probes the whole admissible stroke, so throwing would kill it; extrapolating a collar is not a physical
statement. 2–142 clamped evaluations per search out of ~40 located biases, and **all 12 located folds are
interior**. A clamp that is never reported is an extrapolation with extra steps.

#### What was surprising

**S-213. At a force-pinned fold the collar's whole effect is `|F_es| d ln μ/dh`, so its sign is free.** The
baseline coupled tangent vanishes at the fold by construction, and a multiplier on a pinned force moves only
the decay rate, `1/ℓ → 1/ℓ − d ln μ/dh`. So carrying the collar adds exactly `|F|·g` — **+2.60 to +4.99 pN/nm
here, strictly positive** — and the fold moves deeper at every model and every load line, with no re-solve
needed to know it. `C-0027`'s straddle across three difference schemes is gone; what still straddles zero is
`C-0019`'s half, at −0.813 to +1.156 pN/nm, and that is a **model** spread over `C-0003`'s six free energies,
3.3× narrower and not something mesh refinement addresses.

**S-214. A one-signed correction to a force is NOT a one-signed correction to a bias margin.** The margin is
a ratio of two biases read at two *different* gaps — the fold gap and the operating gap — and `μ(h)` lowers
each by about `1/√μ` at its own gap. So the margin moves with the sign of `μ(h_fold) − μ(h_operating)`, i.e.
with the sign of `3 nm − s_fold`. At 10 nm / 2 mM the fold is at 3.4–4.1 nm, deeper than §3's stroke, and the
margin **rises** 1.4 %; at 7 nm / 10 mM it is at 1.9–2.7 nm, shallower, and the same correction with the same
positive gradient makes the margin **worse** by 0.9–3.5 %. Nobody had asked which way it would go at the
states `T-62` names.

**S-215. `C-0027`'s `≥ 1.108–1.134` is not a lower bound, and that is `CH-0051`.** It was computed by
lowering `V*` at an **unchanged** pull-in bias. The same multiplier lowers the pull-in bias too — 7.1–8.0 %
against `V*`'s 8.5–9.9 % — so almost the whole 10 % improvement cancels and the solved margin is
**1.021–1.028**. Correct a ratio in both of its arguments, or it is not corrected at all. `C-0027`'s
*measurement* of the operating-bias fall is right: 8–9 % through `T-16`'s `dV/dF` against 8.5–9.9 %
re-solved, two independent routes on the same number.

**S-216. The correction changes the OWNER of the ceiling at four of six models.** The fold moves deep enough
that the path meets `C-0002`'s `φ = 0.2` crossover first; past it the branch rises monotonically until the
field can no longer hold the tile, ending **on the field** at strokes of 7.9–8.7 nm. That is `C-0018`'s own
free-tile mechanism — the layer's osmotic divergence removing the instability — arriving at the *coupled*
line. So `C-0018`'s "pull-in binds at 11 of 54 coupled states" becomes **6 of 12** at the two states where it
bound, and a margin propagated as if the ceiling were still pull-in is not comparable with the one that binds.

**S-217. A central difference divided by `2·step` instead of by the SEPARATION of its two samples is exactly
half, and no dimensional check catches it.** It fired here, in the mesh-convergence block, where `step` was
the separation rather than the half-step; the quantity has the right units either way. The fix is
`centralLogGradient`, which names the separation once and is used at all three call sites, with its own gate-1
test. Cost: one wasted 10-minute run.

**S-218. A gradient converges 6.2× worse than the quantity it differences.** Refining the 2-D mesh 2 → 3 → 4
moved `μ(6.5 nm)` by 6.4e−4 and `d ln μ/dh` by 5.1e−3, from the same solves. `CLAUDE.md`'s "convergence is a
property of the quantity" applies to *derived* quantities too, and a derivative is the one most likely to be
quietly under-resolved. It still converges to 0.11 % — 15× inside the difference-step spread it is asked to
collapse.

**S-219. The cheap bound was right, and its declared error was the error it had.** The Plan predicted "about
a factor of two, one-sided in neither direction" for the closed-form estimate from `C-0022`'s transverse
eigenvalue ceiling. Measured: 1.01× at 5 nm / 2 mM, 1.16× at 7 nm / 2 mM, 1.58× and 2.06× at 10 mM — and it
is genuinely not one-sided. It is the first cheap bound in this programme whose predicted error matched, and
it got the **sign** right, which `C-0022`'s own depth half did not.

**What it cost.** A new `electrostatics/CollarMultiplier.kt` (the two mappings, the closed-form estimate, the
central difference, and a `C¹` clamped interpolant), a four-line `actuator/CollarCorrectedField.kt`, and
`actuator/CollarEquilibriumPathStudy.kt` emitting
`gpd/results/T-60-collar-on-the-equilibrium-path.json` — 48 two-dimensional solves and 108 fold searches in
~10 minutes. **24 gate-named tests, written first and watched fail** (five did, including the Hermite
derivative's own sign). `C-0022`'s solver and `C-0018`'s fold search are **consumed read-only and re-run**,
not tabulated: `T-3b`'s published `μ` comes back at its own three `(gap, bias)` points to **2e−7**, and
`C-0018`'s twelve coupled margins to **every published digit**.

### Iteration 5 — what closed, and what it cost

**Six science loops and two process blockers, run in parallel against one checkout.**
`P-16` and `P-15` were taken **first**, ahead of every science task, because `SESSION-PROMPT.md` ranks
process blockers above cheap wins and four agents were about to be pointed at one working tree.
Both earned the position: `--drop-file` was used by **every** agent in the iteration — one had to drop
five sibling files in a single run, and another hit a half-written **main** source that `--drop <pkg>`
could not have removed without taking eighty references with it.

**The iteration's largest single finding is that a reserve the programme had been banking was already spent.**
`C-0017`'s free stability margin rests on the coupling being strain-**stiffening**, so that its tangent
exceeds its secant and stability comes free at zero placement cost.
`C-0030` solved the joint rather than bracketing it, and the realised flexure strain-**softens**.
`C-0032` re-ran `C-0018`'s fold analysis on that law over 216 states: at 10 nm / 2 mM the bias margin
collapses to **1.0000–1.0019** — the device is placed *on* its own fold — and the fold's stroke walks back
through §3's own 3 nm target at two of six layer models.
Both escapes are priced and both fail.
So **0.5 mM MgCl₂ stops being the comfortable choice and becomes a requirement of the only coupling that
survives** — the sixth independent route to the same recommendation, and it is handed back to NDI as a
specification decision rather than adopted in the loop.

**Three results dissolved a question rather than answering it, which is the loop working.**
`T-72` was sent to price a trade — a truss removes the buckling but cannot sway, and sway *is* the draw-in —
and found **the trade does not exist**: the draw-in lives on `(u_x, φ_y)` and the buckling on `(u_y, φ_x)`,
so a two-leg row laid *across* the flexure adds **exactly zero** to the loaded plane.
`T-21` found the upper volume-fraction crossover is not a number but a **one-parameter family**, and that
the cited 0.2–0.3 band is the same textbook expression read on a *monomer* instead of a Kuhn segment.
`T-75` found that `C-0030` had named the wrong variable: `dδ/ds = ±1` exactly, containing no length, so the
mounting sign is a product of **two** binaries and "which body carries the standoffs" predicts it no better
than a coin.

**And one margin finally moved in a direction, having straddled zero for an iteration.**
`C-0033` solved the collar multiplier at **fixed bias** over sixteen gaps instead of differencing across
gaps visited at five different biases, and `d ln μ/dh` came back **positive everywhere**, converged to
0.11 %, against `C-0027`'s inherited 70 %-wide bracket. `C-0018`'s margin rises to 1.021–1.028 at
10 nm / 2 mM and never goes below one. But the direction is **not universal** — the margin is a ratio of
two biases read at two different gaps, so it moves with the sign of `3 nm − s_fold`, and at 7 nm / 10 mM the
same positive gradient makes it 0.9–3.5 % *worse*. Nobody had looked for that.

**What the iteration cost.**
Three agents took the **same claim number within 91 seconds**, each having re-listed the directory and found
the slot empty; one then renumbered onto a second collision. It self-resolved, and the lesson is now in
`CLAUDE.md`: re-listing does not prevent a collision, a gap is cheaper than a collision, and the register
other agents actually read is `TASKS.md`.
One agent clobbered a sibling's `TASKS.md` row with a global `sed`, **noticed, repaired it, and reported it
unprompted** — which is the only reason it was checked.
One full-suite run reported a single unnamed failure that did not reproduce; it is recorded rather than
explained away.
A factor-of-two trap in a central difference — dividing by `2·step` rather than by the *separation* of the
two samples — cost a ten-minute run and passes every dimensional check.

**The three findings this iteration added to the standing set are all about the difference between a number
and the confidence attached to it**: a reproducibility certificate that certifies the *path* rather than the
answer (`CH-0043`); an absolute zero-floor that is not inherited by the quantity derived from it, so a
committed file asserted `"layerStiffness": 0.0` beside a 1.17 mm amplitude; and a solver that returned roots
correct to the last ulp while doing three to five times the work it was selected for.
**A defect that is invisible in the answer is invisible to every check written on the answer.**

## Iteration 6 — `T-81`: the sixteen-crossover hinge line does not exist, and the count that does exist buys the acceptable stroke and not the desired one

**What was asked.** `C-0034` filed its own open item 5 in one sentence:
*"Whether 16 crossovers can be assembled into one hinge line on a 40 nm tile at all.
This claim takes `C-0029`'s hinge count as given and only prices what is at the other end."*
Three standing claims — `C-0023`, `C-0029`, `C-0034` — price the programme's only element reaching §3's
**desired** 10 nm stroke on a hinge of sixteen antiparallel crossovers, and none of them had counted.

**What was done.** `C-0015`'s `CrossoverLayout` re-run as a library over the complete 32 bp phase space, and
`C-0034`'s `anchoredArmForStiffness` re-run at every hinge count from 1 to 32. Claim `C-0040`, challenge
`CH-0054`, 27 gate-named tests, `gpd/results/T-81-hinge-line-census.json`.

**The answer is four, and three divisions were enough to know it.**
Crossovers serve one *interface* every 32 bp = 10.88 nm, so a hinge line of `n` needs `(n − 1) × 10.88 nm` of
**collinear interface** — 163.2 nm for sixteen, against a 40 nm tile. The transverse reading fails in the other
direction (33 duplexes = 88.8 nm) *and* on the axis, because a crossover's `k_θ` is a dihedral spring about a
line running **along** the helices. The inventory fails on the demand side: 45 paths × 16 = 720 against the
whole tile's 49–56. The complete census then gives **four at every one of the 32 phases**, three on the other
parity at 22 of them.

**Two things fell out that were not looked for.**

1. **The phases that maximise a hinge line are exactly `C-0015`'s ten centro-symmetric ones.** Both are the
   eight-column phases — one statement about a symmetry group, one about a count on one interface, derived for
   different purposes, and nothing forced them to coincide. It is asserted as a gate-3 test.
2. **Sixteen crossovers *can* be assembled, and they compose in SERIES.** A raft hinged on `m` parallel lines
   is a fan, each line carrying only the moment outboard of it: `n_eff = n_i·3(2m − 1)/(m(2m + 1))`. Four
   interfaces of four is exactly sixteen crossovers and is **worth 2.333 of hinge, 14.6 % of its own count**.
   The fan is the one hinge topology in this programme that is buildable as counted, and at 45 paths it is
   2.08× too soft.

**What it costs the design.** `C-0034`'s pipeline needs **10** crossovers to lift 10 nm by rotation and **12**
to do it inside `C-0023`'s ceiling, against **3** for the ceiling at the acceptable stroke — so the window in
hinge count is `3 ≤ n ≤ 6`. At four crossovers the arm places at **7.748 nm**: §3's acceptable 3 nm clears at
36.58 pN/nm, and `δ = r sin θ < r` puts the desired 10 nm out of geometric reach. At the **one or two** a
flexure can own when 45 of them share the tile, the tangent is 42.0–54.1 pN/nm and even the **acceptable**
stroke fails `C-0023`'s own compliance ceiling. `A8.2`'s named quantity moves too: the dominant compliance term
changes back from the arm (58.5 % at sixteen) to the **hinge** (78 % at four, 95 % at one).

**The continuum control decided what kind of fact this is.** A continuum hinge line of the same length carries
3.676 crossovers of density; the lattice quantisation is worth −18 % to +9 % while the assertion is out by
**4.35×**. So this is a statement about **crossover density**, not about a lattice — a continuum sheet of the
same size does not deliver sixteen either. The premise was swept in the other direction too: at the 16 bp
per-helix mis-reading `CLAUDE.md` warns about the count is 8, at honeycomb's 21 bp it is 6, at both together
12. **No reading in circulation reaches sixteen**, which is unusual here — normally a verdict moves across a
premise.

**What did not move.** Nothing computed in the three challenged claims. Sixteen upstream reproductions land at
≤ `3.8e−9` outside the rounding their own claims quote, including `C-0034`'s failing 8-crossover row and both
of its tangents. What fails is an integer none of them checked, and the failure is a **count**, so no
measurement can overturn it.

**Where it leaves the programme.** `C-0029` closed the standoff branch at §3's desired stroke and left `E5g16`
as the only element reaching it; `C-0037` reopened the standoff branch with a truss. With the count checked,
**`E5g16`/`E5a16` reach §3's acceptable stroke and not its desired one**, so `T-98`'s "which branch should
Gen-1 take" is no longer a comparison of two live options at the desired stroke. `T-99` — fewer, longer
flexures — is the only route left on this branch, and it is bounded from below at 34 paths by `CH-0029`.

## 2026-08-14 — `T-96`: the array does not pack, and the obstruction is its legs

**Task.** `C-0035` settled which of four flexure mountings is buildable and priced every aperture *as a
fraction of the tile footprint, explicitly as a scale rather than a placement* — saying so, and flagging the
packing as unsolved. `T-96` is that flag: 45 flexures of `C-0030`'s ~32 nm span, with their two standoff legs
and one tie each, **placed in plan** on a body the size of the tile. Claim `C-0041`, challenge `CH-0055`.

**The cheap bound ran first and deliberately did not close it.** 45 beams are 3852 nm² of plan area, 4178 nm²
with the standoff feet — **2.39× and 2.59×** the 1614 nm² footprint. That is exactly the size that invites
*"stack it in three levels"*, and `C-0017`'s envelope has room for three (`ℓ = 5.78 / 7.82 / 9.86 nm`, 17 / 23
/ 29 bp, each clearing `C-0030`'s midspan clearance and separated by 2.04 nm against a 2.0 nm steric
diameter). So the bound's whole contribution was to point the expensive part at the **topology** rather than
at the area — the same shape as `C-0035`'s own cheap bound, which settled a sign and sent the rest to
buildability.

**Stacking buys nothing, and that is the result.** A standoff runs from the superstructure **up** to its own
beam plane and a tie runs from that plane **down** to the tile, so **any two vertical members of the array
share a height range whatever levels their beams sit at**. A clash between them is **level-independent**: no
ordering, no level count and no larger body resolves it. Once that is written down the answer is two lattice
facts that meet nowhere. **Fact A — the attachment grid's across-helix pitch IS one duplex.** `C-0015`'s rows
sit at exactly 2.69 nm, which is the width a beam occupies, so two beams in the same column and adjacent rows
are *tangent* at zero tilt — admissible, because that is what a lattice is — and at any other angle their
perpendicular separation is `2.69 cos θ < 2.69`, so each covers the other's tie. Fact A forces `θ = 0`.
**Fact B — the along-helix pitch is under the span.** Two collinear beams need `|Δx| ≥ span + d`, *not*
`≥ span`, because their standoff feet sit on the beam **ends**: beams laid end to end put two standoffs in the
same place. That is 34.51 nm against a 13.33 nm column pitch, and it fails at `θ = 0`. **0 of 720 orientations
for 3 × 15, and 0 for 2 × 15**, sample-count independent over 180 → 2880.

**What was not anticipated: the obstruction is the legs, not the beams.** The task was formulated around the
beams and their apertures, and the beams' own bodies would clear each other in three levels by area. What does
not clear is that an upper beam's standoffs must reach the superstructure through every plane below, and its
neighbours' bodies are exactly where they must land. **The array does not fail to pack, it fails to stand up.**
Writing the clash relation is what turned a plausible three-level layout into an impossibility, and it was
added only because a limiting-case test failed: two collinear beams at `span + 1.345 nm` passed a foot-versus-
body test and are still two duplexes 1.3 nm apart.

**What the Gen-1 tile carries is exactly fifteen** — one flexure per duplex, one column, span **21.44 nm =
63 bp**, which is `C-0026`'s one-attachment-row-per-duplex scheme at `m = 1`. It is feasible at **exactly one
of 720 orientations**, and that one is exactly parallel to the helices. A measure-zero design window is
normally a defect; here it is not, because **a lattice can hold a tolerance of zero** — the sheet's own helix
direction supplies the angle exactly.

**And the two clauses of §3 differ in kind rather than in degree**, which is what the task asked to be checked
rather than averaged. At the **acceptable 3 nm** stroke the binding variable is the **path count**, the
threshold is **45 → 15**, and it costs nothing against any standing allowable: 6.67 pN per path against the
10 pN unzip allowable, 2.16× of buckling margin (1.71× on Fields et al.'s measured rigidity), assembled
tangent 25.49 pN/nm against `C-0023`'s 40 pN/nm ceiling. At the **desired 10 nm** stroke the count is bounded
**below at 29** by the same allowable and **above at 15** by the packing, so **the window is empty on the
specified tile** and the binding variable is the **footprint**: **≥ 2330 nm², 1.44×, 1.20× in edge**. The
minimum body is `n(L + d)·d` **exactly** and carries **no aspect ratio** — 2878 nm² at 1 × 34 and at 2 × 17
alike, 31.5 × 91.5 nm against 62.9 × 45.7 nm.

**The compliance ceiling was never the constraint**, at any count: the assembled tangent sits at 25.2–25.8
pN/nm from 10 to 60 paths, because the span is *placed*, so the count moves the length and not the stiffness.
`C-0003`'s discipline — a perturbation at specified response is absorbed by the length — in a fourth place.
**And `CH-0029`'s floor turned out to be a bracket rather than a number**: read on `C-0017`'s mandate secant it
is 34 paths exactly (the number in circulation, reproduced to `0.0`); read on the **element's own** delivered
force it is **29**, because `C-0030`'s coupling strain-softens and delivers less at 10 nm than its 3 nm secant
implies. Both are above 15, so the verdict does not depend on the choice.

**A finding the task did not ask for.** `C-0035` prices the tie apertures as 326 nm², 20.4 % of the footprint,
*"the irreducible part of `T-78`'s answer"*. **An area is not the question a sheet asks.** The holes lie on the
attachment grid, whose across-helix pitch is exactly one duplex, so a column of ties does not punch 15 holes —
it removes a **line of material**. Solved as union-find on `C-0015`'s own 32 bp crossover lattice, the 3 × 15
grid cuts every one of the 15 duplexes into **four** pieces and leaves **18 disconnected components**, at every
one of the 32 crossover phases; with the helices running across `x` three whole duplexes are obliterated and
the body falls into four strips. A 20.4 % area loss sounds like a stiffness correction; a severed body is not
one. **The remedy is free of every upstream claim** — `C-0026` fixes the attachment *rows* and says nothing
about where along a row an attachment sits — and it costs **8 bp of stagger, one duplex pitch**, alternating
±1.36 nm row to row.

**What was challenged, and what was not.** `CH-0055` is against `C-0023`, `C-0030` and `C-0035`, and it is
against a **premise** they share in their `Conditions` line — *"45 load paths on `C-0015`'s 3 × 15 grid"* —
not against a number. Every number reproduces: `C-0030`'s span to `2.9e−5`, its tangent to `1.1e−4`, its
critical load to `3.9e−4`, its clearance to `1.7e−16`; `C-0035`'s slot to `9.6e−5` and all three of its areas
to better than `1.2e−3`. None of the per-element physics is disturbed. What does not survive is the design
point. `C-0023`'s rule that the path count is set by the allowable is not reopened — it gains a **second**
bound, from above, and the two now cross.

**Where it leaves the programme.** Two of the last three iterations have closed §3's desired stroke on this
branch from a different direction — `CH-0054` on the hinge count, `C-0041` on the plan view — and both leave
the **acceptable** clause alive. `T-99` (fewer, longer flexures) inherits an upper bound it did not have.
`T-31` is narrowed rather than answered: at 45 paths its question is moot because the array has no plan view;
at 15 the beams are one duplex apart, free and in one level, and whether to cross them over is a live choice.
Three new tasks: `T-101` (is a 15-attachment scheme flat under the *solved* load — `CH-0034` already saturates
the criterion between 45 and 225, and 15 is below the range either it or `C-0015` examined), `T-103` (yaw and
lateral with a single attachment column), and **`T-102` — may §3's tile grow by 1.44×? — which is a
specification question for Kazik, item 7, and the second this branch has raised after `T-95`.** `C-0022`
prices the growth favourably: a larger tile costs **+6.3 %** at the rim instead of +14.7 %.

**Verification.** 35 gate-named tests, 0 failures; the whole suite green on `tools/verify.sh` (two sibling
files dropped mid-TDD with `--drop-file`: `PairedPerpendicularJunctionTest.kt` and `TwoSpringElasticaTest.kt`).
The result file is byte-identical on two independent `tools/study.sh` runs. Twelve upstream reproductions;
six declared falsifiers, none fired.

---

## Iteration 6 — `T-97`: two 90° junctions on one sheet duplex, and the bound that was written to bind and did not

**Claim [`C-0042`](gpd/claims/C-0042-paired-perpendicular-junction.md)** (renumbered from `C-0038`; see below),
**challenge [`CH-0056`](gpd/challenges/CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md)** against `C-0029` and `C-0037`.

**What was asked.** `C-0037` closes the standoff branch with a two-leg row *across* the flexure axis, i.e.
**along one sheet duplex**, and names as its own single largest risk that `C-0029` had placed exactly **one**
90° junction while the design needs **two**. `T-97` is that search.

**The answer is that the pair is not tight but loose.** It fits at **every** separation from the **6 bp =
2.04 nm steric floor** to 12 bp, with both links of both junctions inside the **measured** `[0.60, 0.70]` nm
phosphodiester step, **zero unpaired nucleotides**, four distinct targets, one shared lateral seat
(`Σ(Δy_i)² = 0` exactly) and 24 of the seat duplex's 32 crossover phases free. **And the azimuth costs
exactly nothing** — both base chords come out at **0.00°** from the flexure axis, on both groove conventions,
under the strict "both junctions grounded on ONE sheet duplex" reading, and with both legs on the duplex's own
axis. The only non-zero cost anywhere in the sweep is the *scaffold-excursion* topology at 6 and 9 bp, worth
0.30 % and 1.04 % of the couple against `C-0037`'s own 8.4 % allowance.

**The cheap bound written to bind did not, and its failure is the finding.** If the second junction were the
first's **screw image** — the first translated `n` bp along the seat duplex *and rotated with it* — its chord
would be rotated by `n × 33.74°`, which at `C-0037`'s recommended **8 bp is 89.9°**: the entire couple on the
wrong plane, and 8 bp the single worst separation in the band. It fails because **a standoff must stand normal
to the sheet and a screw rotation about a horizontal axis does not preserve that**. The second standoff's own
azimuth, axial position, seat and choice of target pair are all free, and that freedom is the whole answer.
The correctly-attributed refinement — a swap to the duplex's **other backbone** (±120°) plus a **half turn**,
free because a chord is a line — puts the residual at **3.8° at 7 bp against 22.4 at 6 and 29.9 at 8**, and
the search duly returns two literal translates at one azimuth (300.0°) at 7 bp, one grounded on each backbone.
**The two bounds order the band oppositely.** It is an explanation and not a bound: at 6 and 11 bp the two
standoffs come out a half turn apart instead and the alignment is still exact.

**What does bind is something neither upstream claim looked for.** `C-0037` reports `L2a6`, `L2a8` and
`L2a12` as *bit-identical* and concludes the cost is the leg **count** and not the leg **spacing**. They are
identical in the **loaded** plane — reproduced here to three figures, span 33.43, tangent 26.09, `Φ` 0.258,
supply 2.90, `P_c` 9.77 at all three — and they are **not** identical in the **free** one, which is the plane
its truss exists to restrain: 8.84 / 11.70 / 16.14 pN. `P6` is judged on the minimum, so at 6 bp the free
plane still governs (margin 2.52) and at **7 bp** it has crossed and the loaded plane governs at 9.77 pN
(margin **2.79** CanDo, **2.10** Fields). **Seven base pairs buys the whole of the recommended design 0.68 nm
narrower**, and `C-0037`'s *"between 6 and 8 bp"* is resolved.

**One piece of new mechanics was needed and turned out to be needed for a reason that did not materialise.**
`C-0028`'s sway determinant is written for one column and `C-0037` multiplies its root by the leg count —
legitimate only when every leg has the same base. Two chords at different azimuths give two different `ρ_b`,
so the truss was re-solved as a **beam-column finite element** (Hermite cubics, consistent geometric
stiffness, one shared head node, critical load = the smallest total load at which the assembled matrix stops
being positive definite, by **Sylvester's criterion on `LDLᵀ` pivots** — exact, not a tolerance). It
reproduces `C-0028`'s sway column and `C-0037`'s `trussBucklingLoad` to 2e−4, `C-0037`'s `L2a8` loaded and
free loads to **4e−10**, and its `trussTipFlexibility` **entry by entry** to 1e−12. The answer turned out to
be `ψ = 0` everywhere — but without the solver there would have been no way to price a misalignment the search
might have found, and the two scaffold-excursion rows are priced on it.

**What surprised us.** Four things. (1) The pre-registered prediction was **wrong in the interesting
direction**: it expected *"several degrees"* of residual misalignment and the answer is zero, because the
chord azimuth is not on a lattice at all. (2) The **seat is a bound, not an output** — an unbounded search
parks its optimum on the *rim* of the seat duplex, where the flat end face's line contact `2√(R² − y_c²)` has
collapsed to a point and the standoff is balanced on an edge; `seatFaceHeight` alone does not exclude that and
a new `seatContactLength` had to. (3) The aligned pair's **binding link sits at 0.6969 nm — the C2′-endo end**
of the measured window, against `C-0029`'s 0.600 at the C3′-endo end, and pricing the centring at 5.7° of
alignment per 0.05 nm does not move it: **the couple is free, the sugar pucker is not**, and `T-71` must be
run at the pucker the alignment demands. (4) The aligned optimum is **not** a scaffold excursion — its targets
are *two* apart on one strand, where `C-0029`'s single-junction optimum landed on *consecutive* phosphates.

**What was challenged.** `CH-0056` is against `C-0029`'s azimuthal quantum as inherited verbatim by `C-0037`:
the 33.74°/bp lattice is a property of the **sheet's** phosphate positions — where a link may land — and not
of the **standoff's** chord, which is set by the standoff's own free rotation because it is a separate duplex
whose helical phase nothing else fixes. **No number in either claim moves** — both were conservative and
neither applied the projection — but the 8.4 % is not a design allowance and should not be reserved against,
and `C-0037`'s "best-phase" caveat can be struck rather than merely noted. Same family as `CH-0021` (a factor
applied on the wrong cut) and `C-0028`'s `B2` (a lever arm taken from the sheet when the part was a duplex
end).

**Where it leaves the programme.** The standoff branch's largest **chemistry** risk is retired at the level a
phosphate-distance model can retire it, so `T-98`'s comparison of the two branches now has one fewer open
premise on the truss side. `T-71` inherits two junctions instead of one and a stricter pucker. The largest
open item under `C-0037`'s design is now **the cap** — new row **`T-106`**, which is `T-97`'s question at the
*other* end of the same legs and can be answered with the same tool. And `T-96`'s plan view gains 0.68 nm per
row of currency.

**Verification.** 32 gate-named tests, 0 failures; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated
tree with a sibling's half-written `anchoring/TwoSpringElasticaTest.kt` dropped by `--drop-file`, and the
result file re-emitted through `tools/study.sh` reported *"no result file changed"*. Fourteen
upstream reproductions, worst departure 3.1e−4 (`C-0015`'s own rounding of 32.01 to 32). Six declared
falsifiers, none fired. Every continuous grid tripled with a departure of `0.00e+00`.

**Numbering.** `C-0038` was allocated to this task but `C-0040` and `C-0041` were already on disk from
siblings when the claim was written, with `C-0038` and `C-0039` unclaimed but plausibly in flight; per the
coordinator's collision rule the claim was taken **above the highest visible** as `C-0042`, and its challenge
as `CH-0056`. The new task row is `T-106` for the same reason.

---

## Iteration 6 — `T-79`: a large-rotation two-spring elastica for `E5`'s arm

**What was asked.** `C-0034` closed `T-70` with the arm bracketed at **11.03–12.50 nm** by two compositions,
each exact in one respect and wrong in the other, and named the missing one as its own open item 1:
`C-0029`'s series form has `CH-0040`'s exact rotation but is the `ρ_f = 0` corner of the boundary-value
problem (`CH-0044`), and `C-0034`'s two-spring BVP has the end condition but is small-deflection. `T-79`
asked for the composition exact in **both**.

**What was built.** A planar inextensible **elastica** with a rotational spring at each end —
`EI φ″ = −F cos φ + H sin φ`, `EI φ′(0) = k_n φ(0)`, `EI φ′(L) = M₀ − k_f φ(L)` — integrated by RK4 over
`(φ, φ′, x, z, ∫EIφ′²/2)` and closed by shooting on the near-end rotation, with placement, cap, force at a
stroke and the usable stroke as bisections on top of it. Claim `C-0039`, challenge `CH-0053`, 26 gate-named
tests, `gpd/results/T-79-two-spring-elastica.json`.

**The free verification asset, and it is why this method was chosen over a co-rotational FE beam.** At
vanishing load the elastica **is** the boundary-value problem `C-0034` condenses, so `twoSpringArmFactor` is
a limiting case that pins the field equation, both boundary conditions and every sign at once. It reproduces
at all four textbook corners — 12 guided, 3 at either cantilever ordering, 0 at the free-free **mechanism**
the constructor refuses — and over a 25-point interior grid, worst departure **1.7e−14**. Nothing else in
this programme has had a check that strong available for free.

**What was found, and the headline is not the number the task was commissioned for.**

1. **The bracket is not a bracket.** The arm places at **12.7198 nm = 37.4 bp**, **1.79 % outside** on the
   long side. `C-0034`'s premise — *"two errors run opposite ways and very nearly cancel"* — is false: both
   readings are corrections to the **same** linear beam and **both stiffen it** (a restrained far end relieves
   the hinge; the arc shortens the span and the lever falls as `cos θ`). What made 11.028 < 12.496 look like a
   bracket is that they are different corrections to a **common baseline neither reports**.
2. **And the 1.8 % is the least important thing the exact composition says.** The placement clause is
   discharged exactly (33.3333 pN/nm on the secant, 4.2e−15) and the **compliance clause is not**: the tangent
   is 36.44 pN/nm at §3's acceptable stroke — inside `C-0023`'s 40 pN/nm ceiling with 8.9 % to spare — and
   **264.2 pN/nm at the desired one, 6.6× past it**, with the **secant** there already **69.94 pN/nm, 2.10×
   the mandate**, and the assembled force **699 pN against §3's own 100**. `C-0034` reported 36.78 pN/nm at
   10 nm, *"inside the ceiling with 8.1 % to spare"*.
3. **The usable stroke inside the ceiling is 3.877 nm**, and `clears 10 nm inside the ceiling` is **false at
   0 of 34 placements and 0 of 25 sensitivity points** — every anchorage from a ball joint to `C-0029`'s own
   asserted ideal guide, every hinge count from 1 to 64 including `C-0040`'s buildable 1–6, and every path
   count including `C-0041`'s 15, where the arm places at 8.40 nm and the desired stroke is out of
   **geometric** reach. The best usable stroke anywhere is 4.136 nm, at a design that is not a motif.
4. **The cause is geometry, not constitution.** The placement condition caps the arm at 13.65 nm, so §3's
   desired stroke is **≥ 73 % of the arm's own contour** at every point of the catalogue, and a member driven
   that far past its own length stiffens whatever it is made of. It is the same statement that closed the
   standoff branch, on a different member.
5. **An inextensible arm has no axially-held reading at all.** `C-0023` treats free/held as a design binary;
   holding two ends at their axial separation while offsetting them transversally needs a chord longer than
   the contour, so the held reading is a **different constitutive assumption** and costs ≥ 299 pN of tension
   at the desired stroke, 4.6× past the 65 pN nicked ceiling. The exact draw-in in the free reading is
   **0.383 nm = 1.1 bp** at the acceptable stroke — 4.0× `C-0029`'s quoted 0.095, which charges only the
   hinge's rigid swing and not the arm's bending — and **5.34 nm = 15.7 bp** at the desired one.
6. **The per-path allowable is a different verdict at the two strokes**, which is `CLAUDE.md`'s own rule with
   the two readings on opposite sides: at 3 nm the element's tension (2.22 pN), hinge bond force (0.355) and
   anchorage link force (6.07) are all inside `C-0006`'s 10 pN unzip allowable; at 10 nm two of the three are
   past it, at 15.54 and 24.31 pN.

**What was challenged.** `CH-0053`, against `C-0034`'s placement bracket and its `E5a16` desired-stroke
verdict, and by inheritance `C-0029`'s `E5g` *"reaches §3's desired stroke: yes … PASS"* and `CH-0044`'s
*"what does NOT move"* clause. `C-0034`'s continuum, counting theorem, anchorage catalogue and fixed-point cap
are **confirmed and used** — the cap moves outward, 13.428 → 13.648 nm — and §3's **acceptable** stroke is
untouched on every clause.

**Two surprises worth recording.** (1) A safeguarded **secant** root-finder that exits only on bracket width
**stalls and returns the midpoint of a bracket whose root sits at one end**, because once the residual is
locally linear the first secant step is already exact and the bracket stops shrinking. That is
`CLAUDE.md`'s *"an unreachable convergence tolerance is silent"* in the one place where it is silently
**wrong** rather than merely slow; the cure is to exit on the iteration's own **step**. (2) The shooting
residual of an elastica is **not monotone** in the near-end rotation once the arm curls past a right angle —
neither end of the assumed bracket is reliable — so the solver scans for the first sign change and, when
there is none, **refuses** rather than returning a root off the small-rotation branch. Two of the 34
placements are recorded as *"the arm folds before reaching the desired stroke"* on that ground.

**Where it leaves the programme.** Three independent routes now say that §3's desired 10 nm stroke is out of
reach on the flexure branch — `CH-0040`'s cube-root cap, `C-0040`'s hinge-line inventory and this claim's
geometry — and none of them was looking for it. New rows: **`T-107`** (is the 40 pN/nm ceiling required at
the desired stroke or only at the working point — the cheapest thing that could move the verdict),
**`T-108`** (say plainly whether the desired stroke is reachable by any coupling this programme has), and
**`T-109`** (which joint supplies the 1.1 bp of draw-in the acceptable stroke demands).

**Verification.** 26 gate-named tests, 0 failures; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated
tree with **nothing dropped**, and the result file re-emitted through `tools/study.sh` **twice**, reporting
*"no result file changed"* both times. Twelve upstream reproductions, worst departure 4.4e−5 against values
their own claims quote rounded to five digits and 2.8e−9 otherwise. Five declared falsifiers; **falsifier 2
fired, exactly as cheap bound 2 predicted it would**, and it is the challenge. Nested RK4 refinement
100 → 1600 wins ≥ 8× per doubling — fourth order, measured.

**Numbering.** `C-0039` and `CH-0053` as allocated; both were still free on disk when the claim was written,
with `C-0040`–`C-0042` and `CH-0054`–`CH-0056` already taken by siblings in this iteration. New task rows
`T-107`–`T-109`, above the highest visible `T-106`.

### Iteration 6 — the branch that was surviving, and the two counts that closed it

Four loops, all against the design `C-0029`/`C-0034` had left standing: the `E5a16`/`E5g16`
hinge-and-arm flexure, the one element in the programme with **no 90° junction anywhere in it**.

**Two of the four were counts, and both came back negative.**

`C-0040` asked whether the **16 crossovers** that `C-0023`, `C-0029` and `C-0034` all take as given
actually exist in one hinge line on a 40 nm tile.
A crossover serves one *interface* every 32 bp = 10.88 nm, so sixteen needs **163.2 nm of collinear
interface — 4.08 tiles**. The complete 32-phase census gives **four**, at every phase.
Sixteen *can* be assembled from four interfaces of four, but interfaces compose in **series** and are
worth 14.6 % of their count; at the one-to-two crossovers a flexure can actually own, the tangent is
42–54 pN/nm — past `C-0023`'s own ceiling, so **even §3's acceptable stroke fails**.
The phases that maximise a hinge line turn out to be exactly `C-0015`'s ten centro-symmetric ones,
which nothing in either construction forced.

`C-0041` asked whether **45 flexures of ~32 nm span** can be placed in plan on a body the size of the
tile. They cannot, at either stroke, and **not on area** — the cheap area bound is 2.59×, which is
exactly the size that invites "stack it in three levels", and stacking buys **nothing**, because a
standoff runs up to its beam plane and a tie runs back down to the tile, so any two vertical members
share a height range whatever level their beams sit at. The obstruction is two lattice pitches that
meet nowhere. The Gen-1 tile carries **exactly fifteen**, at **one of 720** orientations — supplied by
the sheet's own helix direction. At the acceptable stroke the binding variable is the path count and
it is free; at the desired stroke the count is bounded below at 29 and above at 15, the window is
**empty**, and the binding variable becomes the **footprint**.

**And a finding nobody asked for.** `C-0035`'s tie-aperture floor was quoted as an *area*, but the
holes are collinear on the grid, so a tie column removes a **line** of material: the 3 × 15 grid cuts
every one of the 15 duplexes into four pieces and leaves **18 disconnected components**, at every one
of the 32 phases. Cured by **8 bp of stagger**, free of every upstream claim.

**The other two loops removed risk rather than adding it, which is worth recording.**
`C-0042` searched for the **second** 90° junction `C-0037`'s truss needs and found the pair closes at
every separation from the 6 bp steric floor to 12 bp, with **zero** azimuth cost, resolving
`C-0037`'s "between 6 and 8 bp" to **7**. Its `CH-0056` is the conceptual half: the 33.74°/bp
quantum belongs to the **sheet's** phosphate positions — where a link may *land* — not to the
standoff's own chord, which is a separate duplex free to rotate about its axis continuously.
`C-0039` solved the arm on a composition exact in **both** the rotation and the end condition, and
**falsified the bracket's premise rather than confirming its width**.
`C-0034` had reasoned that its two readings bracket the arm because *"two errors run opposite ways
and very nearly cancel"*; they do not. Both are corrections to the **same** linear two-spring
boundary-value problem and both **stiffen** it — a restrained far end relieves the hinge, and the arc
shortens the span while the lever falls as `cos θ` — so the exact composition lands **outside** their
span on the long side, at **12.7198 nm, 1.79 % beyond** the 11.028–12.496 nm bracket.
`CH-0044`'s 1.64× over-placement survives intact, reproduced twice and independently.
The method is pinned rather than asserted: the elastica's vanishing-load limit reproduces `C-0034`'s
closed-form `c(ρ_n, ρ_f)` at all four textbook corners and across a 25-point interior grid to
**1.7e−14**, which fixes the field equation, both boundary conditions and every sign at once.

But the 1.8 % is the least of it. **The placement clause is discharged exactly and the compliance
clause is not**: the tangent is 36.44 pN/nm at §3's *acceptable* stroke, inside `C-0023`'s ceiling
with 8.9 % to spare, and **264.2 pN/nm at the desired stroke — 6.6× past it**, with the secant there
already 2.10× the mandate and the assembled force 699 pN against §3's own 100.
The usable stroke inside the ceiling is **3.877 nm**, and "clears 10 nm inside the ceiling" is false
at **0 of 34 placements and 0 of 25 sensitivity points** — every anchorage including `C-0029`'s
asserted ideal guide, every hinge count 1–64 including `C-0040`'s buildable 1–6, and every path count
including `C-0041`'s 15, where the stroke is out of *geometric* reach.
The cause is geometric: the arm is capped at 13.65 nm, so 10 nm is ≥ 73 % of its own contour
everywhere, and the exact draw-in is **5.34 nm = 15.7 bp**, 42 % of the arm, against the 0.095 nm
`C-0029` quoted.
So **`E5` is an element for §3's acceptable 3 nm stroke and not for its desired 10 nm**, and that is a
third independent route to the same conclusion, alongside `C-0040`'s hinge line and `C-0041`'s packing.

**Taken together the iteration is a reversal.** `C-0029` closed the standoff branch at the desired
stroke and left the guided-arm flexure standing; iteration 6 closes the *flexure* at the desired
stroke on two independent counts — a hinge line that does not exist and an array that does not pack —
while `C-0037`'s truss loses its largest open risk. `T-98`'s "standoff-and-truss against `E5a16`" is
therefore **no longer a comparison of two live options**, and the queue says so.

**What it cost, and one of the costs was the coordinator's.** The claim-numbering race repeated:
three of the four agents renumbered, some of them twice, each having re-listed an empty slot.
`C-0038`, `C-0043`, `CH-0052` and `CH-0057` are vacant as a result, and the gaps are left rather than
compacted — cheaper than a collision, and the rule is already in `CLAUDE.md`.

**And the coordinator committed a wrong summary of `T-79` before its final answer had landed.**
A subagent notification is not a commit boundary: `T-79` reported once with a preliminary verdict
(*"the bracket is upheld, the answer sits at 0.43 of its width, the two errors nearly cancel"*) and
then kept working and reported again with the opposite one, under a different claim number.
The iteration-6 commit message describes the **first**, which was never on disk — the filed claim has
always been `C-0039`, and it says the bracket **fails** and the errors run the **same** way.
The commit contents were right; the message and this journal's summary were wrong, and they are
corrected here rather than quietly rewritten.
**The rule this yields: read the filed claim, never the report, before writing anything down.**
The report is a summary by an agent that may still be working; the claim is the artifact. Every agent used `P-16`'s `--drop-file` on a sibling's mid-TDD file,
two of them on three files at once; the final run of each needed no drops at all.

## Iteration 7 — `T-99`: the trade is not a trade, and it runs the wrong way

**The question `C-0040` left open was the flexure branch's last escape to §3's desired stroke.**
Iteration 6 closed the 45-path crossover-hinge flexure three ways — a 16-crossover hinge line that
does not exist (`C-0040`), an array that does not pack (`C-0041`), and a usable stroke of 3.877 nm
under exact rotation (`C-0039`) — and every one of those is a *count* or a *length*.
So the natural move, and `C-0040`'s own open item 2, was to trade path count against hinge count:
fewer flexures, each owning a longer stretch of the tile's 640 nm of interface line.

**The cheap bound closed the premise in one division, and the second one explained why.**
A hinge line lies on **one** interface, so `n·h` crossovers come out of `C-0015`'s counted 49–56.
Intersected with the 34-path floor at the desired stroke that leaves **`h = 1`**: fewer paths are
forbidden by the allowable and longer lines by the inventory, so at the desired stroke the trade has
exactly one admissible hinge count and there is nothing to trade.
The second bound says why in general.
A rigid-armed array of total restraint `Σk` places at `θ tan θ = k_target δ²/Σk`, `r = δ/sin θ`, and
on the hinge budget alone `Σk = N k_θ` — **a function of the PRODUCT `n·h` and of nothing else**.
So the arm is **5.387 nm at all eight splits of `n·h = 56`**: a flexure array cannot convert
crossovers into arm length by moving them between hinges. **The trade is degenerate on its own axis.**

**What breaks the degeneracy runs the other way, and that was the surprise.**
`C-0034`'s far anchorage is a **per-flexure** couple — the arm's own duplex end, 78.24 pN·nm/rad,
which is 5.78 crossovers' worth — so the array's restraint is `N k_θ + n k_far` and the **placed arm
grows with the path count**: 6.903 nm at `(14, 4)`, 8.231 at `(28, 2)`, **9.973 at `(56, 1)`**.
*Fewer, longer* is strictly worse than *more, shorter*, at every step, by 1.45× in arm and 1.36× in
stroke across the curve.
At the best point the anchorage carries **85 %** of the restraint: the element the branch is named
after contributes the minority of its own stiffness.

**The verdict, at both readings of a ceiling that turned out not to matter.**
`C-0023`'s 40 pN/nm is declared at no stroke, which is the concurrent `T-107`'s subject, so both
readings were carried. **0 of 31 points reach §3's desired stroke, at both.** `T-107` cannot move it:
under the standing placement the binding constraint at every ledger-admitted point is **geometric
reach** — a tip cannot rise past its own arm — and the ceiling is never consulted.
The best point of the whole space, `(56, 1)`, has an arm of 9.973 nm and misses the desired stroke by
0.027 nm geometrically and by 3.02× on the usable stroke. Only **3** of 31 points clear even §3's
*acceptable* stroke, all at `h = 1`, and they spend **80–100 %** of the tile's crossovers to do it.

**And running the placement §3's own arithmetic actually asks for produced the iteration's real
finding.** Every claim in the corpus reads the desired stroke on a coupling *placed for the
acceptable one* — 33.3333 pN/nm, which at full stroke must deliver 333 pN, and which `C-0039` finds
really needs 699. `C-0017`'s arithmetic says the desired clause's coupling is `100/10 = 10 pN/nm`, a
**different device**. Built, it passes everything the flexure branch owns: arms of 11.4–18.1 nm,
12 of 29 points reaching 10 nm inside the ceiling and inside the unzip allowable.
**And `C-0017`'s stability floor refuses all of them** — `|k_eff| = 23.41–27.91 pN/nm` at the 10 nm
layer in 2 mM, against a 10 pN/nm placement.
Composing the two clauses of `C-0017` gives a bound that names no element at all:

&nbsp;&nbsp;&nbsp;&nbsp;`δ ≤ F/|k_eff|` → **3.58–4.27 nm at §3's 100 pN**.

That is `CH-0059`, and it relocates the failure from the coupling to the actuator.
It also demotes the **34-path floor**, quoted in three claims as though it were a property of the
material: it is `33.333 × 10/10`, a property of a placement convention, and the same allowable gives
**10** under the other one. `T-112` carries the specification question to NDI as open item 8.

**What it cost.** One numbering check (`C-0046` and `CH-0059` were free; `C-0047` had already been
taken by `T-101` between the prompt being written and the run), one `--drop-file` on `T-108`'s
mid-TDD `synthesis/DesiredStrokeReachTest.kt`, and one solver repair: `C-0039`'s placement search
floors at `1.5 δ`, which is 15 nm when the working stroke is 10, so the desired-clause placement had
to be re-bracketed. Walking **down** from the long end rather than up from a floor is the fix — the
assembled secant is monotone in the arm but a short arm at a large stroke *folds*, and an evaluation
there throws rather than returning a number. The widened solver reproduces `C-0039`'s own to `1e−9`
wherever both are defined, which is asserted as a gate-5 test.

## Iteration 7 — `T-101`: fifteen attachments do not flatten the tile, and below three columns a coupling stops buying flatness and starts selling it

`C-0041` closed `T-96` by showing that the Gen-1 tile carries **exactly fifteen** flexures — one
column, one per duplex, at the single orientation the lattice supplies — and named the flatness of
that scheme *"the largest open item this claim leaves"*.
It is below the range anything had examined:
`C-0015` searched grid *shapes* from 45 upward,
and `CH-0034` swept 45 → 225 and found the criterion **saturates** at 0.149 of the stroke.
Nobody had asked what happens on the other side of that range.

**The answer is that it is not a saturation, it is a loss, and the loss reverses the sign of what a
coupling does.**
Under `C-0022`'s solved load the 1 × 15 grid dishes **0.695 of the free-tile stroke** — **7.0×**
`T-5b`'s 10 % convention, against 0.218 at `C-0015`'s 3 × 15 and 0.223 at `C-0009`'s 8 × 8, and
**4.7× above** `CH-0034`'s floor.
And the finding nobody was looking for: the **free, uncoupled** tile dishes **0.308** under the same
load, so **the fifteen-path coupling is 2.26× worse than having no coupling at all**.
Two columns is still worse than none (0.350).
Three is better (0.218).
**The break-even is at exactly the three columns `C-0041` shows cannot be built**, and the whole
useful range of the attachment-count axis turns out to be one grid step wide:
`CH-0034` found where attachments *stop* buying flatness, and this iteration found where they
*start*.

**The cheap bound saw all of it, in four arithmetic operations, and then predicted the repair.**
A duplex on the polymer layer is a beam on a Winkler foundation, and its bending length
`ℓ = (4EI/k)^(1/4)` is the reach of one attachment's influence patch:
**12.83 nm along** the helices against a 40 nm single-column pitch (3.12 patches short),
and **5.71 nm across** them against a 2.69 nm row pitch (0.47, dense).
The rows were never the problem and the columns always were.
That 2.2× between the two lengths is the sheet's 25.6× rigidity anisotropy seen **through a fourth
root**, and it makes `C-0015`'s *"shapes, not counts"* a closed form:
three columns is the **last** count whose pitch still falls inside one bending length (1.04),
which is why `C-0015`'s answer is three and not eight.

**The orientation is worth 1.08× in dishing and 16× in the load path**, which is where the
anisotropy actually shows up: fifteen attachments on one duplex is the exact opposite of `C-0026`'s
one-row-per-duplex scheme, and the other fourteen duplexes must then be carried across the hinges
(3.28 pN against 0.209).
The dishing barely notices, because at fifteen attachments neither orientation can flatten a 40 nm
tile.
`C-0041` found the array feasible at exactly 1 of 720 orientations; that one also happens to be the
one that lays the attachments across the helices, so the packing constraint and the load-path
constraint want the **same** angle — a measure-zero window two independent requirements agree on.

**`C-0022`'s 32.1 % lever/sensor split is untouched, and that is the point.**
It is a rim property, written on the free tile, and this task's own plate reproduces it to 0.2 %.
What moves is the *other* term: the coupling's own sag is 32 % of the dishing at 3 × 15 and **79 %**
at 1 × 15.
A 15-path design does not sit at `C-0022`'s split — it sits well above it, and four fifths of what
it sits above is its own doing.

### What surprised us

**A coupling can be a net dishing source.**
Every upstream claim treats the attachment count as an axis along which flatness is *bought*;
`CH-0034` established that the buying stops at 45.
That the axis **reverses** below three columns was not anticipated by anything, and it is only
visible because this task ran the uncoupled tile as a reference — which `CH-0034`'s table never
does, because it starts at 45.

**A prediction of this task's own failed in code at the first run.**
Gate 3 was written asserting the staggered column's restored crossover force is **second order** in
the stagger: the tile's bow is even about `x = 0`, so `w'(0) = 0` and the reaction changes at
`O(s²)`.
The measured exponent is **0.9**.
The reasoning had conflated the *reaction* with the *shape* — a crossover measures the **relative
deflection of two adjacent duplexes**, and two duplexes propped at `+s/2` and `−s/2` have
mirror-image shapes whose difference is `O(s)`.
The test now asserts first order and says why.
That is also `CH-0060`: `C-0041`'s 8 bp connectivity stagger **breaks `C-0015`'s exact zero**,
restoring 0.389 pN under a *uniform* load — **1.9× `C-0022`'s entire solved edge effect** — and it
is the same across-helix symmetry break `C-0026` identified as the worst pattern in its set,
reached from a geometry rather than from an assembly tolerance.
It stays 26× inside the unzip allowable, so no verdict moves; what moves is `C-0041`'s sentence
*"moves nothing else"*.

**And the declared falsifier fired, mildly.**
`T-101`'s Plan set a one-per-cent falsifier on the stagger's flatness cost; the measured cost is
**+2.19 %**.
Immaterial — the quantity it perturbs is already 7× the tolerance — but it fired, and the Plan had
said in advance that if it did it *"needs a challenge"*.

**The stagger turned out to be a design variable, and then its own claim's span took half of it
back.**
`C-0041` introduced the stagger to keep the superstructure connected and valued it at zero.
Swept to the geometric limit it buys **45 %** of the dishing back at ±13.60 nm — because a large
alternating stagger makes **adjacent duplexes prop each other through the crossovers** — and the
cheap bound predicts *where*: that half-stagger is the along-helix bending length to **6 %** and
`C-0015`'s three-column pitch to **2 %**, a single column doing the best imitation of a multi-column
grid that alternation allows.
Then the constraint that had been missed: a staggered **attachment** only has to stay on the tile,
but a staggered **flexure** has to stay on the **body**, and a flexure is a 21.44 nm beam *centred on
its own tie*.
So the half-stagger is capped at `edgeX/2 − span/2 = 9.28 nm`, the 80 bp optimum **overhangs by
4.32 nm**, and the best buildable stagger (54 bp) returns **22 %**, to 0.541 of the stroke.
Still 5.4× the convention.
**The same span that forbids three columns also caps the repair for having only one.**

### What it cost

Three `--drop-file`s on concurrent agents' mid-TDD work —
`anchoring/FlexureCountHingeTradeTest.kt`, `synthesis/DesiredStrokeReachTest.kt` and, mid-iteration,
the **main** source `anchoring/TrussCapStudy.kt`, which is the second time a sibling's *main* file
rather than a test has broken `compileKotlin` for the whole project.
No numbering collision: `C-0047` and `CH-0060` were free and `T-99`'s agent had already recorded in
this journal that `C-0047` was taken by this task.
Nothing upstream needed repairing: `C-0026`'s pipeline was re-run as a library and reproduced its
1 × 15, 3 × 15 and 8 × 8 dishing to `1e−9`, `CH-0034`'s 0.149 floor to `1.7e−4`, `C-0022`'s free-tile
0.321 to `2.0e−3` and the 25.6× anisotropy from the sheet's own rigidities.

## Iteration 7 — `T-106`: the truss cap is a body, and the counting theorem was applied to one of its two load paths

**What was asked.** `C-0037`'s recommended standoff — two duplexes standing normal to the sheet
under a shared cap — models that cap as **one series spring**, `k_tie = k_link Σd_i²` with
`k_link = 2 k_bond,s` *"forced by `C-0029`'s counting theorem applied at the other end of each leg"*,
and says in its own validity range that the cap's **geometry** is *"asserted, not designed"*.
`C-0042` narrowed the leg row to 7 bp and left it there: *"this claim places two bases, not a cap."*
With the paired-junction risk retired, the cap was the largest open item under the design.

**The geometry fell out of two functions `C-0042` had already written, before any solve.**
Its **steric floor** puts two legs at least `2R = 2.00 nm` apart; its **seat contact**
`2√(R² − y_c²)` says a leg's flat end face touches a duplex only within one radius of its axis.
Read together they are the same length twice, and they exclude each other:
**a duplex laid ACROSS the leg row seats neither leg** — line contact `0.000 nm` at every separation
from 6 to 16 bp, against `2.000 nm` for one laid **along** the row.
So **the flexure cannot be the cap**, and the counting theorem says the same thing independently —
the flexure's own end has two termini and there are two legs, so such a cap is *one* link per leg,
`C-0037`'s own `H1`.
**The cap is a separate crossbar duplex**, 13 bp = 4.38 nm at the recommended pitch, laid along the
row, hosting **three** 90° junctions and six covalent links, its axis one duplex radius above the
leg heads.

**A body has four things a spring does not, and three of them are small.**
Its **bending** is `12 EI/w` exactly — the frame-couple path is statically determinate, so the leg
forces are `±M/w` whatever the stiffnesses and the compliances simply add — which is **8.93×** the
couple it carries at 7 bp, so the frame couple falls only 74.18 → **71.31** pN·nm/rad.
That term goes as `1/w` where the couple goes as `w²`, so it is the **wide** rows the crossbar
cannot carry: 1.6 % at the steric floor and **42.5 %** at 16 bp, which is why it cannot be folded
into an effective link stiffness the way `C-0037`'s `k_tie` is.
Its **torsion** `4C/w` is worth 0.1 % on either reading of the duplex torsional constant — 103 ± 4 nm
measured, salt-independent including 10 mM Mg²⁺, fetched and read for this task.
Its **height**, exactly one duplex radius, is a unit-determinant congruence: it raises `C12`, the
entry `C-0030` shows *supplies* the draw-in, and costs one nanometre of leg at a given flexure
height.

**The fourth is the finding, and it is a load path rather than a number.**
`C-0037` invokes the counting theorem at the leg's head for the **axial** link and then takes the
head's **rotation** as rigid. The same two links on the same chord carry that rotation, at
**78.24 pN·nm/rad** on one axis and **13.53** on the other, whose sum is `C-0042`'s conserved
**91.76** — so the cap junction is a **third** instance of one rank-one budget, after the leg row's
`Σx² + Σy² = w²/2` and the base chord's.
It is not a series correction: it is a **ceiling** on the head restraint any frame couple can
deliver, which is why the omission does not read as a small factor.
And unlike the leg row's azimuth, this one has **no free corner** — the loaded plane wants the strong
axis for the draw-in supply and the tangent, the free plane wants it for stability, and 91.76 is all
there is.

**What that does to `C-0042`'s seven base pairs is the part worth recording.**
Its 7 bp is *upheld*, and its reason is not. With the cap chord laid **along** the flexure axis the
free plane is capped at **6.20 pN** by the junction's own 13.53 and **no** row width up to 16 bp
hands the governing plane back to the loaded one; laid **across** it the free plane reaches 9.24 pN
and **7 bp** is again the smallest separation that crosses.
**The crossing is bought at the cap, not at the row** — which says the row pitch is not the variable
to spend on, and names one that is. `CH-0061`.

**The verdict.** All nine predicates PASS over the whole `h = 5–10 nm` envelope on both rigidities,
at a buckling margin of **1.95 / 1.46** against `C-0037`'s 2.79 / 2.10.
The solved cap costs **30 %** of the margin, **38 %** of the draw-in supply and **35 %** of the
tangent headroom, shortens the span 33.43 → 28.25 nm through `c₀` (110.4 → 73.1), and takes
`k_s`/32 from failing on one rigidity to failing on both.
It does **not** close the branch. It does add a body to a motif that had none, and it moves
`C-0042`'s retired chemistry risk up one storey: **whether three 90° junctions close on one 13 bp
crossbar is now the largest open item**, and it is `T-117` (filed as `T-109`, renumbered by the coordinator: `C-0039` had already taken that number for the draw-in question in iteration 6 — a **task**-ID collision, the first in this programme, the claim-number race having trained everyone to check the wrong ledger).

**Two things surprised.** The pre-registered prediction said the crossing separation would move
above 7 bp; it does not move at all, it becomes *conditional* on a variable neither upstream claim
has — which is a different and more useful answer.
And the nearest published relative of the cap junction turned out to be the **T junction**, used in
print exactly as this claim needs it: *"these tiles carry over the orthogonal binding directions from
T junction and retain the rigidity from antiparallel crossover tiles."* The orthogonal joint supplies
**direction**; something else supplies **rigidity**. That is this task's finding, one level up, and
it was found by the only one of ten negative-existence queries that returned a hit.

---

## Iteration 7 — `T-107` and `T-108`: which stroke a ceiling is owed at, and the stroke that is the layer's own thickness

**Two tasks, one study, and the second one turned out not to be about couplings at all.**

`T-107` was queued as *"the cheapest thing that could move `C-0039`'s verdict"*, and it is cheaper
than that: **40 pN/nm is exactly `1.2 × (100 pN / 3 nm)`.** Divide the ceiling by the thing it was
declared against and the answer falls out with no code — it is a *construction on the placement
mandate*, so it carries the placement stroke inside it, and the **same construction** at §3's
desired clause is **12 pN/nm, not 40**. Reading 40 at a 10 nm stroke is not the conservative
choice; it is the wrong clause's number, and it is 3.33× too generous.

Then the audit. `C-0017` has an **equality** on the secant and a **floor** on the tangent; `C-0018`
gets *better* as the tangent stiffens (`C-0032` measured it: the strain-stiffening line raises the
10 nm / 2 mM bias margin from 1.007–1.032 to 1.020–1.774). **There is no tangent ceiling anywhere in
the acceptance stack.** The only genuine ceiling is `C-0006`'s per-path unzip allowable — and
because it is a bound on a **force**, converting it to a stiffness divides by the stroke, so it
*tightens* as `1/s` where the declared ceiling stays put: 150 → 45 pN/nm at 45 paths, 50 → **15** at
`C-0041`'s buildable 15. `C-0039`'s `E5a16` secant at 10 nm, 69.94, is past both. **The verdict does
not move; its owner does** — from a declared tolerance missed by 6.6× to a cited allowable missed by
1.55×, which is a weaker-looking rejection with a stronger warrant.

**The part that was not expected is that the same answer settles `CH-0047`.** A device placed at
3 nm traverses `[0, 3]` and *never occupies* the 4.556 nm stroke `CH-0042`'s tangent minimum is
taken at. Read where the device actually sits, `C-0030`'s favourable flexure is **25.227 pN/nm**,
which clears **4 of `C-0017`'s 6** model floors at §3's own 2 mM — where the prescribed-range
minimum 22.875 clears **none**. `C-0032`'s `Q2` goes from *"no model admits it"* to *"the
admissibility is model-dependent"*. It does not recover 2 mM — `Q3` and the 1.0000 bias margin are
untouched — but `T-63` is decided on exactly that distinction. The ceiling and the floor were the
same question; `T-107` and `CH-0047`/`T-76b` are one task.

**`T-108` was queued as a synthesis over the catalogue. It should have been a look at the
coordinate.**

The stroke is `s = L₀ − h`. So `s < L₀`, identically, and §3 names three layer heights of which the
tallest is 10 nm. **A ~10 nm stroke on a 10 nm layer IS the statement `h = 0`** — the polymer
crushed to a melt of zero thickness. Three ceilings follow and **none of them contains a coupling**:
the kinematic one `L₀ − Nσv₀` reaches at most **9.790 nm** over 66 states, `C-0002`'s validity
ceiling `L₀ − Nσv₀/0.2` — which is `C-0018`'s *own* binding bias ceiling at the 10 nm layer, at 6 of
6 models and every buffer — **8.959 nm**, and the dead-load stroke at §3's 100 pN **7.424 nm**. And
a coupling can only make it worse: `C-0017`'s own gate-2 theorem says the delivered stroke is
monotone *decreasing* in the coupling stiffness, so the free stroke is the supremum over **every**
coupling anybody could design. **That is why `T-108` is not a search**, and why one bound covers a
catalogue.

Five claims — `CH-0040`, `C-0039`, `C-0040`, `C-0041`, `C-0046` — spent four iterations establishing
that the coupling cannot deliver 10 nm. The coupling is not what cannot deliver it. **The cleanest
row in the whole 28-row table is a pass**: `C-0023`'s `E5`, read at 10 nm, places, sits inside the
ceiling, keeps 7.41 pN per path against a 10 pN allowable, spends 45 of the tile's 56 crossovers and
is stable at both buffers — and fails **only** on the reach.

**What is established is "unreachable on §3's own stack"**, which is stronger than *"unreachable
with this catalogue"* and weaker than *"unreachable in physics"*. The escape is priced and it is a
**layer height**: 16.63–26.12 nm across `C-0003`'s six models for a 10 nm stroke at 100 pN, 1.7–2.6×
§3's tallest. §3's own tile row already allows the effort point to sit *"~20–25 nm above the
electrode"*, so it is not absurd — but four upstream validity ranges move with it and nothing here
has evaluated one. That is `T-115`, and it is the fourth specification question this programme has
had to raise (after `T-95`, `T-102` and `T-63`/`T-112`). **The other three cannot substitute for
it**: a bigger tile, a perforated superstructure and a softer placement each unblock something the
*coupling* needs, and none of them is a layer height.

**A sibling landed the same iteration from the other side and the two agree without having met.**
`C-0046`/`CH-0059` (`T-99`) placed §3's desired clause on its own arithmetic — `k_c = 10 pN/nm` —
and found `C-0017`'s stability floor refusing it by 2.34–2.79×. The hypothetical row in this
study's own catalogue reproduces that independently, and **closes `CH-0059`'s open item 3**: at 5
and 7 nm, where `C-0017` reports the floor as *zero* and the 10 pN/nm placement would not be
refused, the kinematic ceiling is 4.33–4.62 and 6.42–6.66 nm. The desired stroke is out of reach
there for a reason that owes nothing to stability.

**One contradiction had to be filed.** `C-0040` reports the buildable hinge counts at 42.0–54.1
pN/nm and concludes *"even the acceptable stroke fails `C-0023`'s own ceiling"*. Those numbers are
`C-0034`'s **series composition**, which `CH-0053` superseded in the same iteration; on `C-0039`'s
own exact elastica the same design places at a **9.131 nm arm and 39.18 pN/nm — inside the
ceiling**, and `C-0039`'s own published table already contains that number. Two claims filed hours
apart, same anchorage, same constants, disagreeing about whether the branch survives §3's
acceptable clause, and neither noticed. `CH-0062`. It moves a verdict the **favourable** way, and it
is largest exactly where the verdict was taken, because at one crossover the hinge is 92.5 % of the
compliance and the composition is nearly everything.

**Numbering.** `C-0044`/`C-0045` and `CH-0058` were reserved by the coordinator's brief; by the time
this iteration wrote, siblings had taken through `C-0048` and `CH-0061`. Filed as **`C-0049`**,
**`C-0050`** and **`CH-0062`**, per the instruction to take the next above the highest visible.

**What surprised.** That a whole iteration's worth of *"is 10 nm reachable"* is answered by reading
the definition of the coordinate; that a ceiling and a floor which had been argued separately for
three iterations are the same question; and that the strongest evidence in a negative result turned
out to be an element that **passes**.

### Iteration 7 — the desired stroke, settled by a bound with no coupling in it

Four loops, aimed at the question iteration 6 had made unavoidable: is §3's *desired* 10 nm stroke
reachable at all, and if not, by what?

**`C-0050` settles it, and the settling argument is one line of kinematics.**
The stroke *is* the layer's compression — `s = L₀ − h`, so `s < L₀` identically — and §3 names no layer
taller than 10 nm, which makes a 10 nm stroke on a 10 nm layer the statement `h = 0`.
Over 66 states the ceilings are **9.790 nm** kinematic (1.02× short), **8.959 nm** from `C-0002`'s
validity — which is also `C-0018`'s own binding bias ceiling at 10 nm — and **7.424 nm** as a dead-load
stroke at 100 pN. A coupling can only *reduce* the last, so the free stroke is the supremum over **every**
coupling, and that is why this is a claim rather than a search.
Across the catalogue **0 of 14 elements clear the desired stroke and 3 of 14 clear the acceptable one**.
The telling row is `C-0023`'s `E5`, which clears every coupling-side predicate at 10 nm and fails only on
the **reach**.
The statement filed is **unreachable on §3's own stack** — deliberately stronger than "with this
catalogue" and weaker than "in physics" — and the only escape is a **taller layer, 16.63–26.12 nm**, a
fourth specification question. Tile size, perforation and buffer cannot substitute: none is a layer height.

**`C-0049` removed a ceiling the programme had been enforcing in the wrong place.**
`C-0023`'s 40 pN/nm is exactly `1.2 × (100 pN / 3 nm)` — a declared *linearity tolerance on the placement
discharge*, carrying the placement stroke inside it — so it is owed at 3 nm and nowhere else, and the same
construction at the desired clause is **12 pN/nm, not 40**. Reading 40 at 10 nm is the wrong clause's
number, and **3.33× too generous rather than conservative**.
Auditing the acceptance stack found **no upper bound on a coupling tangent anywhere in it**: one equality
on the secant, one floor on the tangent, and `C-0032` measuring a *stiffer* tangent that *raises*
`C-0018`'s margin. What does bind past the working point is the per-path unzip allowable — a bound on a
**force**, so `n·allowable/s`, which tightens as `1/s`. The same answer closed `CH-0047`/`T-76b`, which
turned out to be the same question: a device placed at 3 nm traverses `[0, 3]` and never occupies the
4.556 nm stroke `CH-0042`'s minimum was taken at.

**The other two loops closed the branch's last escape and priced its last unknown.**
`C-0046` swept `(path count, hinge count, hinge-line length)` jointly and found the trade **degenerate on
its own axis**: placement depends on the crossover *product* `n·h`, so the arm is 5.387 nm at **all eight**
splits of `n·h = 56`, and crossovers cannot be converted into arm length by moving them between hinges.
What breaks the tie is the *per-flexure* far anchorage, so the placed arm **grows with the path count** —
fewer-longer is the wrong direction at every step, and the best point of the whole space reaches 3.312 nm
while spending 100 % of the tile's crossover inventory.
`C-0048` solved the truss cap and found `C-0037` had been carrying **one of its two load paths**: the same
two links' rotation was taken as infinite where `C-0042`'s conserved budget makes it a **ceiling**. The cap
survives — it is a separate 13 bp crossbar with three junctions and six links — at a cost of 30 % of the
buckling margin, and it does not close the branch.

**And one loop found something nobody had asked for.**
`C-0047` evaluated flatness at the fifteen attachments `C-0041` says the tile actually carries, under
`C-0022`'s *solved* load, and the fifteen-path coupling dishes **0.695 of the stroke against the free
tile's 0.308** — **2.26× worse than having no coupling at all**. Below 45 attachments `CH-0034`'s
criterion does not saturate; two columns is still worse than none and the break-even is at exactly the
three columns `C-0041` shows cannot be built. Its `CH-0060` also withdraws `C-0041`'s "the stagger is free":
it breaks `C-0015`'s exact zero at **first** order, and the stagger's unconstrained optimum turns out not
to be buildable at all.

**What it cost, and the honesty is the point.** `C-0047`'s agent had a gate-3 test fail on its first run —
it had asserted the stagger's restored force was second order and measured 0.9 — and reported the
correction in the open rather than adjusting the test. `C-0046`'s agent found and repaired a floor in
`C-0039`'s own solver that made the desired-clause placement unreachable, leaving `C-0039`'s code untouched
and asserting agreement to `1e−9` as a gate. Three of the four renumbered their claims again.

## Iteration 8 — `T-118`: three iterations, forty artifacts, and not one window edge moves — because only one of them is about grafting density at all

**What was done.** `C-0027` re-synthesised the design window against iteration 4.
Three iterations have run since — `C-0031`–`C-0050` and `CH-0043`–`CH-0062` — so `T-118` did it again,
under `C-0016`'s own discipline: **classify each new constraint by axis before intersecting it**, because
a constraint that cannot narrow is invisible to an intersection, and `C-0027` added that a constraint that
has been *discharged* is too.
`window.SecondResynthesisStudyKt` extends `T-25`'s machinery rather than duplicating it — the reader layer,
the intersection and the grid are all `T-2`'s — and adds two things it could not have had: `C-0050`'s two
stroke ceilings evaluated on the layer the window is actually drawn on, and the three corrections that now
stand against `C-0018`'s pull-in margin **composed on one tangent**.

**What was decided, and why.**

**`P1` is a null result and the null is the finding.**
Zero of six window edges move, by zero grid steps, with no owner change and a worst edge departure of
exactly `0.0`.
That is not the window surviving forty artifacts: **of the twenty claims of iterations 5–7 exactly one
(`C-0036`) carries a quantity that is a function of `σ` at all**, and `C-0036` states in its own verdict
table that it cannot move `C-0016`/`C-0027`'s edges because it reaches the design only through `C-0018`'s
*bias* ceiling.
The other nineteen are counts, plan layouts, elastica geometries, height-level actuator states, or
specification questions.
Re-intersecting is still worth doing — it is an **index** comparison and therefore immune to `CH-0043`'s
rounding concern, which a byte-diff of a result file is not — but the honest report is that the window was
never addressed, not that it was defended.

**`C-0050` did produce two genuinely new `σ`-resolved constraints, and they are the first since iteration 4.**
The kinematic stroke ceiling `L₀ − Nσv₀` and the validity ceiling `L₀ − Nσv₀/φ_c` are both functions of the
grafting density through `φ = Nσv₀/h`, so both **can** narrow a window.
Evaluated at all 61 grid points of both surviving windows, on `C-0011`'s solved layer and under all three of
the crossover readings `C-0036` leaves standing, **neither binds** — 1.71–3.11× clear.
They close §3's *desired* clause instead, and that is a **height** statement.

**An axis left the acceptance stack, and this time it was replaced rather than discharged.**
`C-0049` reads `C-0023`'s 40 pN/nm as exactly `1.2 × (100 pN / 3 nm)` — a declared linearity tolerance on a
*placement*, which carries the placement's stroke — and shows the stack `C-0017` and `C-0018` actually
define has **no upper bound on a coupling tangent at all**.
What replaces it is the per-path unzip allowable read as a stiffness, `n·a/s`, which tightens as `1/s`:
50 pN/nm at 15 paths and 3 nm, **15 pN/nm** at 15 paths and 10 nm.
A path count, invisible to an intersection.
`C-0027` saw one axis leave; this is the second, and a *replacement* is something an intersection cannot see
either.

**The one thing that moved a verdict is a composition nobody had made.**
`C-0033` and `C-0032` were filed in the same iteration, both moved `C-0018`'s 10 nm / 2 mM pull-in margin,
and they moved it in **opposite directions**: `C-0033`'s solved collar takes it from 1.007–1.032 to
1.021–1.028, `C-0032`'s realised strain-**softening** coupling takes it to **1.0000–1.0019**.
Neither carries the other, which is the second instance of a trap `CLAUDE.md` recorded in iteration 4 — and
a worse one, because there the two corrections were the same size.
At `C-0018`'s own fold the baseline coupled tangent **vanishes by construction**, so every perturbation
enters as an increment and the composition is exact rather than first order:
collar `+2.605` to `+4.993`, `C-0019`'s fluctuation `−1.449` to `−5.807`, and `C-0030`'s realised softening
`−9.207` to `−10.288 pN/nm`, giving **−8.398 to −11.062 pN/nm at 6 of 6 layer models**, with no straddle.
**The collar recovers 27–49 % of what the element costs and nothing more**, so `C-0033`'s margin rise belongs
to the *affine mandate* and not to the device the programme has.
That is `CH-0063`.

**And a licence departed at the one design point nobody had checked.**
`C-0016`'s falsifier 3 fired at 5 nm and was carried forward; `C-0050` reads its validity bound over
`C-0027`'s *entire* 10 nm window, and at that window's **upper edge** `C-0003`'s six trial-function models
sit at `φ = 0.151–0.285` where `C-0011`'s solved layer — the layer the window is drawn on — sits at
**0.0686**.
A factor of **4.15**, and the two disagree qualitatively: two of the six trial-function models have no
validity ceiling at all where the solved layer is 2.92× clear of it.
`C-0050`'s verdict is untouched, because its *kinematic* bound needs no crossover and is short by 1.02× on
its own — but the number is not licensed there, and that is `CH-0064`, the **sixth** instance of this
project's own "quote it with the state it was read at" discipline: after a stiffness with a compression, a
variance with a bandwidth, a rupture force with a loading rate, `k_es` with a gap and a flatness count with a
load case, now **a volume-fraction ceiling with a layer model**.

**What the answer to §6 task 2 now is.**
The window is still correct, still non-empty at 7 and 10 nm, still owned by coil overlap below and §3's 3 nm
stroke above, and still empty at 5 nm by 13.3×.
It is simply **no longer where the remaining uncertainty lives**: three of the four things that moved in
iterations 5–7 cannot be drawn on a `(σ, L₀)` plane, and the one that decides §6 — `C-0050`'s `s = L₀ − h` —
is a kinematic identity about a coordinate.
**The deliverable is now a height plus five specification questions** (`T-63` the buffer, `T-95` the
superstructure, `T-102` the tile area, `T-112` which device the desired clause names, `T-115` a 17–26 nm
layer), and **only `T-115` can buy §3's desired stroke.**

**What surprised us.**
Three things.
First, that a window which has not moved in five iterations is *evidence of nothing* — the programme has
been discovering constraints at a good rate and almost none of them lives on the axis the deliverable is
drawn on.
Second, that the two corrections to the same margin were not merely uncomposed but **2.0–3.7× apart**, so
the one published second read as a rescue of a device it was not evaluated on.
Third, that the cheapest check in this whole task — one multiplication of `T-1d`'s own emitted
`meanVolumeFraction` — found a 4.15× layer-model departure inside a claim filed one iteration earlier.

**What it cost.** Four agents on one checkout again.
`T-118` could not use `tools/verify.sh` unmodified: a sibling's mid-TDD `structure/OrigamiGrillage.kt` is a
**main** source that `window` depends on transitively, and `--drop <pkg>` is the wrong granularity for that.
The run was made on `tools/snapshot.sh`'s own snapshot with that one file **restored to `HEAD`** and four
mid-TDD siblings dropped by name — a superset of what `--drop-file` can express, and it is named in the
claim's provenance rather than glossed.

## Iteration 8 — `T-110`: a crossover cannot be a hinge and an interface at once, so the binding constraint on a flexure array is topology and not stiffness

**What was asked.** `C-0046` closed the *fewer, longer flexures* escape and left one item open, which it
named the largest it leaves: its three surviving `E5a` designs spend **45, 50 and 56** of the tile's 49–56
crossovers as hinges, and nobody has asked what the sheet is like once the coupling has eaten them.
`C-0009`'s `D_⊥`, `C-0015`'s flatness grid and `C-0006`'s load distribution all rest on those crossovers.

**The question behind the question, and it decided everything.** The task was formulated around a decision
that had to be taken *before* any model ran: is a crossover used as a **hinge** still an **interface**
crossover for the sheet — exclusive, additive, or partially shared?
`C-0040`'s own two definitions settle it and neither had been read this way: a hinge line is a set of
crossovers sharing *one interface and one **pair of bodies***, and `k_θ` — the constant `C-0046` prices its
arm with — is the **interhelical dihedral** spring, which resists rotation of one duplex relative to the
*other duplex*.
A hinge that turns puts its two bodies at an angle; whatever is outboard of the line is not the sheet.
**Exclusive at the site**, and a consumed crossover supplies neither the dihedral spring nor the vertical
link.

**Then the answer was a pigeonhole, and it ran in four arithmetic operations.**
The interfaces of a single-layer sheet form a **path graph** on its duplexes, so a connected sheet needs at
least one retained crossover on each of its **14** interfaces.
The hinge budget is therefore `56 − 14 = 42`, i.e. **75.0 %** — and `C-0046`'s admissible region is
**80–100 %**.
**Every design in it severs the tile**: 45 spent leaves at least 4 pieces, 50 at least 9, and 56 leaves
**fifteen separate duplexes**.
No mesh, no fitted constant, no force field, and it is tight — 42 leaves one piece and 43 does not.

**`D_⊥` does not degrade linearly, it collapses, and the two averaging conventions are what shows it.**
Bending across the helices is fourteen hinge lines in **series**, so the honest rigidity is a *harmonic*
mean, `D_⊥ = L_y k_θ/(L_x Σ 1/n_i)`, and it is **exactly zero** the moment one interface empties.
The arithmetic mean — `k_θ d² N/A`, which is `C-0009`'s own imposed-field identity and the only thing a
smeared continuum plate can express — still reports 0.667 pN·nm at 45 spent and an anisotropy of 128 where
the truth is unbounded.
The two agree on a uniform lattice to exactly `(15/14)²`, `C-0009`'s `(n−1)/n` duplex-count residual squared,
asserted rather than tolerated.

**The step is at severance, not at consumption.**
Under `C-0022`'s solved load on `C-0015`'s 3 × 15 grid the tile dishes 0.218 of the stroke intact, **0.242 at
the 42-crossover ceiling — 11 %** — and **0.465 at 45, 2.13×**.
Spending three more crossovers costs eight times what the previous forty-two cost.
And the lattice/plate ratio **flips sign exactly there**, 0.87 → 1.62: below the ceiling the lattice is the
stiffer model, which is `C-0009`'s rule for a smooth load; above it the lattice is 62 % softer, because the
plate cannot represent a severed interface at all.

**Three things surprised us.**

First, **the load distribution improves as the sheet fails**, and it is not a design success.
The peak per-load-path crossover force falls 0.150 → 0.105 → **exactly zero** pN, because a crossover that
has been removed cannot be overloaded.
`C-0026`'s exact zero is reached here by *deleting* the load path rather than by balancing it — and every
per-path allowable in the programme is discharged with two orders of margin at every consumption level,
which is precisely why the allowables could not have found this.

Second, **`C-0010`'s insensitivity is correct and is not the relevant channel.**
Its *"a 2× change in `D_⊥` moves the answer by 2.5 %"* reproduces here — the smeared plate moves 31 % over
the *whole* consumption range — while the **lattice moves 117 %**, a factor of 3.8.
The reason is that **a crossover is two elements and only one of them is `D_⊥`**: the vertical link is a
*constraint* tying two duplex surfaces together, it carries no bending rigidity, and a continuum plate has
**no parameter for it**.
Scaling `D_⊥` is not a model of crossover consumption.

Third, **the generous reading of the geometry is the worse design.**
If a hinge *is* an attachment, consumption pays for itself — except that at the same 45 paths, attaching at
the hinge sites dishes **1.535** of the stroke against 0.465 on `C-0015`'s grid, **3.3×**.
A crossover lattice is a 32 bp × 2.69 nm lattice with a parity and a flatness grid is three columns of
fifteen rows; they are different **shapes**, which is `C-0015`'s own lesson in its fourth place.

**And the branch is not dead.**
`C-0046` bracketed its path-count threshold at `34 < n ≤ 45` and did not resolve it.
It is **39**.
So the window on a sheet **in one piece** is `39 ≤ n ≤ 42`, delivering 3.005–3.063 nm against §3's
acceptable 3 nm — four path counts wide, a **2.1 %** margin on a `k_θ` carrying a ±20 % fitted bracket, and
**none of `C-0046`'s three reported designs is in it**.
That is `CH-0066`, filed against `C-0046`'s best-point verdict rather than absorbed, because `C-0046` is
cited elsewhere for `(56, 1)`.
The correction costs 7.4 % of the usable stroke and buys a tile that is a single body.

**What it cost, and one thing that bit.**
The per-interface **cut identity** `C-0009` asserts is *degraded by the physics it is being asked to
measure*: `shearAcrossInterface` integrates `k_f w` over panels that straddle the tributary strips, and
across a severed interface the reconstructed deflection field is genuinely discontinuous, so a
Gauss-Legendre panel spanning the jump misreads the foundation reaction by ~0.06 pN on a tile carrying
100 pN.
The conservation gate was moved onto the **global** balance, which is exact and holds to `1e−9` even on a
sheet in fifteen pieces, and the cut identity is asserted only where it is valid.
Four agents on one checkout again: three concurrent mid-TDD test files had to be named to `--drop-file`
(`window/SecondResynthesisTest.kt`, `anchoring/HingeArmArrayPackingTest.kt`,
`anchoring/CrossbarJunctionTrioTest.kt`), and the result file was re-run through `tools/study.sh` and diffed
byte-for-byte identical.

**A convergence worth recording.**
`T-116` filed `C-0053` in the same iteration, from the opposite direction — the **plan view** of the same
`E5a1` array — and reaches the same refusal.
It charges an arm more than `T-110` does: a real arm is a length of the host's own duplex cut free at both
ends, so it *also* buries crossovers beneath itself, and at its self-consistent **43** arms the host has no
bonded component at all, with **25** the largest count leaving all fifteen duplexes bonded.
**42 is what the counting permits and 25 is what the geometry delivers**, so `T-110`'s `39 ≤ n ≤ 42` is
*necessary and not sufficient* and the two bounds compose rather than compete.
Neither agent knew of the other's number while working; that they meet is the first time in this programme
that two concurrent tasks have independently condemned the same design on the same resource.

## Iteration 8 — `T-116`: the hinge-arm array packs as bodies, is refused by the lattice, and leaves no sheet behind

`C-0050` left one open item and named it *"the first thing to run against `E5a1`"*: the only row of its
fourteen-element catalogue that clears every predicate at §3's acceptable stroke **and** is computed on the
exact element carried `packingAssessed = false`.
`C-0041` had done exactly this work for the *standoff* flexure and it cost 45 paths → 15.
The instruction was to check each of `C-0041`'s findings against `E5a1` rather than inherit it,
and the useful thing about this iteration is that **not one of them transferred**.

**`C-0041`'s two obstructions both dissolve, and they dissolve for stated reasons.**
Its Fact A is a clash between *vertical members* — a standoff runs up to its beam plane and a tie runs back
down, so any two share a height range whatever level their beams sit at.
`E5a1` has no standoff and no tie: its near end is a crossover in the host sheet's own plane and its far end
is `C-0034`'s two-link `A2` joint.
0 blocking pairs, 0 clashes, at every orientation, at every column count.
Its Fact B is `span + d = 34.51 nm` against a 13.33 nm column pitch; here the same comparison is
**11.82 against 13.33** and runs the other way.
So the 45 arms **do** pack as bodies — 0.685 of the footprint, and feasible at 2 of 720 orientations,
which are exactly `θ = 0` and `θ = π`.
That two rather than `C-0041`'s one is not luck: a **rooted** arm has a direction where a centred beam does
not, so the sweep has to run over the full circle, and the same asymmetry is worth an extra arm wherever a
cluster of hinge sites is narrower than one arm.

**What refuses the array is where the load path enters the sheet.**
A hinge is one crossover, so an arm can only be rooted where a crossover is, and a row can only use the
crossovers of its **two bounding interfaces**.
Those carry opposite parities, so an interior row sees roots at **5.44 nm** and an **edge** row — one
neighbour, one interface, one parity — at **10.88 nm**, against an arm demanding 11.82 nm of clearance.
Three arms per interior row, two per edge row: **43 of the 45 demanded**.
That 43 is **exact**, not a heuristic: the constructive placement equals the independent per-row upper
bound, which ignores the interface-sharing constraint entirely.
And the ten phases that reach it are `C-0015`'s ten **centro-symmetric** eight-column phases —
the third independent construction in this programme to land on them, after `C-0015`'s own and `C-0040`'s.

**The two missing arms are the least of it, and this is the thing the task was not formulated around.**
An arm is not added to the host sheet; it is **cut out of it**.
Its tip is a duplex end because that is what `A2` is, and its root must be doubly nicked from the rest of
its own row, because a single nick is a clamp.
So 43 arms remove **65.4 %** of the host's duplex length, spend **43** of its crossovers as hinges and
**bury 13 more** underneath themselves — **56 of an inventory of 56, exactly 100 %** — leaving **zero**
surviving crossovers and **no bonded component at all**.
The self-consistent count (the arm is a placed quantity, `L ∝ n^(1/3)`, so fewer paths ask for a shorter
arm) is 43; the count that leaves every one of the 15 duplexes bonded into one piece is **25**, and the
collapse above it is monotone and steep — 15 bonded at 25 arms, 14 at 30, 8 at 35, 3 at 40, none at 42.
**The threshold is therefore `45 → 25`, 1.80×, and not `45 → 43`.**
45 never place on any of five swept axes.

**Two claims in one iteration reached the same sheet from opposite sides.**
`C-0054` (`T-110`) came from the *interface*: a connected sheet needs one retained crossover on each of its
14 interfaces, so the hinge budget is 42 of 56.
This one came from the *arm* and lands at 25.
The gap is exactly the two terms a plan view adds and an interface ledger cannot see — the duplex **length**
an arm consumes, and the crossovers it **buries**.
Neither model contains the other's term; both say the surviving designs sever the tile.
`T-111`, which `C-0054` raised, is the same question as `T-116` and is answered by it.

**One thing improved.**
The lattice placement carries **three** attachment columns, exactly `C-0047`'s flatness break-even, so the
arm array is *not* a net dishing source — `C-0041`'s buildable 1 × 15 is 2.26× worse than having no
coupling at all, and this one is 1.41× better than none.
It does not rescue the element; it is inherited by whatever replaces it.

**Two surprises worth keeping.**
First, **an arm cut free at its root is doubly nicked from the rest of its own row, and `C-0025`'s `J2b`
says a double nick IS a crossover** — so `E5a1`'s near stiffness may be `2 k_θ` at no extra crossover.
That is favourable for the mechanics and unfavourable here: the placed arm lengthens 9.3 % and the lattice
places 30.
It is named and not adopted; it belongs to `C-0039`.
Second, the honest severance model needed a **trim** rule.
A residual piece 0.28 nm long, left where an arm roots on the lattice site nearest the edge, is not a
disconnected component — it is a stub a design does not build — and counting it as one reported a placement
artefact as a structural failure, in the same family as `C-0009`'s *"a load-path contour average is not a
peak"*.
Pieces below one duplex diameter are trimmed and counted separately.

**What it cost.**
One concurrent agent's mid-TDD test file had to be named to `--drop-file`
(`anchoring/CrossbarJunctionTrioTest.kt`, `T-117`); the result file was re-run through `tools/study.sh` and
diffed byte-for-byte identical.

## Iteration 8 — `T-117`: the trio closes, and the two ends of a leg are one body

**The acceptance question is answered yes, and it was the easy half.**
Three 90° junctions — two leg heads from below, the flexure's own end from the side — close on a **lone**
13 bp crossbar with all six links inside the measured `[0.60, 0.70]` nm step, **zero unpaired nucleotides**,
six distinct targets and no van der Waals contact anywhere in the assembly.
It closes at every crossbar length from 13 to 19 bp, at every row width from 6 to 12, at both groove
conventions, at both twist readings and at every lateral seat swept — and the chord alignment the design
wants comes out at **0.00°** on the converged grid, which is `C-0042`'s result one storey up.
It still closes when the legs' azimuths are not searched at all but **carried up the leg** from `C-0042`'s
solved base: 14 of the 15 integer leg lengths.
So `C-0048`'s cap is routable, and the risk it opened is retired at the level a phosphate-distance model
can retire it.

**What the three cheap bounds found, and two of them were not the bound they were written as.**
The **rim** exclusion `C-0042` introduced for a *lateral* rim seat had to be written again in the *axial*
direction, and having written it the answer is an identity rather than a margin: `2R = 2.00 nm` is 5.88
rises, so `ceil` buys six and the minimum crossbar overhangs each leg's footprint by exactly **0.02 nm**,
at **every** row width.
`C-0048`'s *"13 bp = 4.38 nm"* is two quantities: 4.38 is the demand `w + 2R`, and 13 bp is **4.42 nm**.
The **leg-to-flexure clearance** is a bound nobody would have run — `C-0048` shows the flexure cannot *be*
the cap and never asks whether its *end* fits between the legs.
Solved exactly, by alternating projection between two convex bodies, it is **0.249 nm** at the recommended
7 bp row, against the **0.54–0.69 nm** surface gaps a honeycomb and a single-layer sheet actually keep —
and it is the row width that buys it back, 0.184 nm at 6 bp against 0.714 at 12.
A **capsule** approximation, the two axis segments less two radii, reports a **clash** there: it rounds the
flat end face into a hemisphere exactly where the two faces pass each other, and is wrong by more than the
answer.

**And the finding is a constraint nobody in this chain had written down.**
`CH-0056` established that the 33.74°/bp azimuthal quantum belongs to the **sheet** and not to a free
standoff's chord, because a free duplex with **one** junction inherits no phase.
A truss leg has **two**.
Its base chord and its cap chord are the terminal chords of one rigid duplex, so they differ by
`m × 33.74°` folded modulo `π` — and `C-0048`'s recommended design wants that difference to be exactly 90°.
Its recommended **7.00 nm leg rounds to 21 steps, whose budget is 78.53°**: the azimuth pair the
recommended design is written on **does not exist at the length it is written at**, and 21 steps is on the
wrong side at 10.67, 10.5 *and* 10.4 bp/turn.
Thirteen steps is the only length in the envelope that survives all three.
This is `CH-0056` **completed**, not contradicted: the absolute azimuths are still continuous — `C-0042`'s
0.00° base alignment stands and this task's own search uses that freedom — and what is quantised is a
**difference between two ends of one body**, a quantity a lone standoff does not have.

**The conservation is what makes it a design choice rather than a failure.**
Rotating the leg about its own axis moves both chords together, so it takes misalignment off one end and
puts exactly as much onto the other: `ψ_base + ψ_cap` is the budget, exactly, at every rotation in the
reducing sense.
Only the **difference** is quantised.
That is `C-0042`'s rank-one identity one storey up — there a chord's two *axes* shared one conserved
couple, here a body's two *ends* share one conserved misalignment.

**The surprise is that the constraint is a favour.**
Every one of the fifteen quantised leg lengths passes all nine predicates, at margins **1.81–2.43** on
CanDo's rigidity and **1.36–1.83** on Fields et al.'s.
And at `C-0048`'s **own** 21 steps the best split of the 78.53° budget — base 19.0°, cap 59.5° — delivers
**2.20 / 1.66** against the recommended pair's **1.95 / 1.46**.
The reason is visible in the solved critical loads: at every quantised optimum the loaded and the free
plane have come **together** (8.99 against 9.09 at 21 steps; 11.788 against 11.788 at 17).
`C-0048` chose its cap azimuth *"because that is where `P6` binds"* — i.e. to maximise the **free** plane —
but at its own design point the **loaded** plane governs, so the recommendation is a corner of a trade
whose optimum is the balance.
`CH-0067` is filed on both halves: the pair is unavailable, and the reason given for it reads the wrong
plane.

**And the crossbar is a duplex, on measured magnesium parameters rather than on 1 M NaCl.**
`C-0048` flags its own 13 bp crossbar as unmodelled thermodynamically.
Huguet et al.'s single-molecule unzipping set — ten nearest-neighbour energies **in Mg²⁺** with
**per-motif** salt corrections, read directly from the paper's Table 1 — puts a 13 bp crossbar at
**31 `k_BT`** sequence-averaged and **17 `k_BT`** at the worst sequence a designer could pick, at 2 mM and
on the pessimistic reading of the salt correction.
On SantaLucia's 1 M NaCl set the same worst sequence reads **9.9 `k_BT`**, which is the wrong side of the
question — so the parameter set mattered more than the arithmetic.
The remedy is nearly free anyway: `12EI/w` and `4C/w` both carry the **row pitch**, so the crossbar can be
lengthened to whatever the thermodynamics wants at a cost only in plan area.
One thing was **not** read: the salt-correction equation is rendered as an image in that paper, so the base
of its logarithm is unknown and both conventions are carried, a factor of 2.30 — the verdict is taken on
the pessimistic one.

**What bit.**
A defaulted optional parameter became a physical constraint: `flexureAzimuth ?: 0.0` turned *"this
junction's azimuth is not locked"* into *"it is locked at zero"* and pinned the flexure's chord 60° from
its demand.
The symptom was a **constant** — 60.00° at every leg length — which reads exactly like a converged optimum,
and it silently contaminated the whole `LOCKED` half of the first run.
"Not specified" has to be a `null` the search sees, never a default the physics sees.
The other cost was wall clock: at a converged 360 × 8 × 5 crossbar grid the study is ~15 minutes of solver
behind ~20 minutes of snapshot and compile under four concurrent agents.

### Iteration 8 — a null that is a finding, and two agents that met in the middle

Four loops: the second window re-synthesis, and the three largest open items the surviving designs had left.

**`C-0051` re-ran the window against `C-0031`–`C-0050` and moved nothing — 0 of 6 edges, 0 grid steps,
worst departure exactly `0.0`, no owner changed — and the null is the result.**
The reason is `C-0016`'s own lesson turned on itself: of the twenty claims of iterations 5–7, **exactly one**
carries a quantity that is a function of `σ` at all, and it reaches the design only through `C-0018`'s bias
ceiling. The other nineteen are counts, plan layouts, elastica geometries, height-level actuator states or
specification questions — and such a constraint is invisible to an intersection.
**So the window was not survived; it was never addressed**, and the census is reported beside the null
rather than the null alone.
`C-0050` did produce the first genuinely `σ`-resolved constraints since iteration 4, and at all 61 grid
points neither binds, by 1.71–3.11×.
One axis **left** the acceptance stack — `C-0049`'s ceiling withdrawal — and a path count replaced it.
The one verdict that did move came from a composition nobody had made: `C-0033`'s collar and `C-0032`'s
softening move `C-0018`'s margin in **opposite** directions and neither carries the other, and composed at
the fold the total is negative at 6 of 6 models with no straddle. The collar recovers 27–49 % of what the
element costs, so **`C-0032`'s 1.0000–1.0019 is the standing statement.**

**Two agents reached the same refusal from opposite ends, and each recorded the other's arrival.**
`C-0054` came from the *interface* side and found hinge use and interface use **exclusive at the site** —
a hinge line is crossovers sharing one interface *and one pair of bodies*, and `k_θ` is the interhelical
dihedral spring, so whatever is outboard of a turning hinge is not the sheet. From there the answer is a
**pigeonhole, not a stiffness calculation**: a connected sheet needs one retained crossover on each of its
14 interfaces, so the hinge budget is `56 − 14 = 42`, **75 %** — against `C-0046`'s admissible 80–100 %,
and all three of its surviving designs **sever the tile**.
`C-0053` came from the *plan view* and found both of `C-0041`'s obstructions dissolve for an element with
no vertical member — the arms pack as bodies — but that **an arm is cut *out of* the host, not added to
it**: 43 arms take 65.4 % of the duplex length, spend 43 crossovers and bury 13 more, exactly 100 % of 56,
leaving **zero** surviving crossovers. The self-consistent count is 43; the count leaving every duplex
bonded is **25**.
Neither agent claimed the other's result, and both wrote the convergence into their claims. **42 is what
the counting permits and 25 what the geometry delivers**, and they compose.

**And the branch that survived got its routing.** `C-0052` closed the three 90° junctions on one lone
13 bp crossbar — six links inside the measured phosphodiester step, zero unpaired nucleotides, chord
alignment 0.00° — and found what `C-0048` could not see: **a leg is one body with two junctions**, so its
base and cap chords differ by `m × 33.74°` and the recommended 7.00 nm leg is **78.5°** from the azimuth
pair its design is written on, on all three twist readings. Only the *difference* is quantised, so nothing
fails — and the forced pair is *better* than the recommended one, because `C-0048` chose its cap azimuth on
the plane that does not govern.

**Two things worth recording about the loop rather than the physics.**
`C-0051`'s agent could not use `tools/verify.sh` unmodified — a sibling's mid-TDD file was a **main** source
its own package depended on transitively — and rather than dropping the package it restored that one file to
`HEAD` inside the snapshot and dropped four test files by name. That technique is now in `CLAUDE.md`, and it
is the natural successor to `P-16`.
And `C-0053` states its own strongest check plainly: **a construction meeting an independent upper bound is
what makes a packing verdict a proof** rather than a search that stopped.

## Iteration 9 — `T-119`: the sheet is standing on an inventory three times its own, and the two azimuths it leaves empty point exactly out of its plane

`C-0054` closed iteration 8 by proving that a crossover cannot be a hinge and an interface at once, and it
was careful enough to name the one thing that would overturn it: *"a junction site the single-layer sheet
does not use"*. It filed that as `T-119` rather than guessing. **The falsifier fires**, and the way it fires
is more interesting than either outcome the task file anticipated.

**The literature settled it in one paragraph, read directly.** Ke, Douglas, Liedl and Shih (*JACS*
**131**:15903, 2009, `PMC2821935`, full text via `pmc.ncbi.nlm.nih.gov` — EuropePMC's `fullTextXML` returns
zero bytes for it, exactly the trap `CLAUDE.md` records): *"each double helix has up to four nearest
neighbors … Every 8 bp, that staple strand is positioned to cross over to one of its four neighbors …
adjacent helices share crossovers every 32 bp, and the positions of the crossovers are restricted to
periodic intersection or 'crossover' planes, labeled from i to iv, spaced at 8 bp intervals."* And, decisively:
*"the crossovers in i and iii sectional slices are parallel to the xz-plane, while the crossovers in ii and
iv sectional slices are parallel to the yz-plane."* **The four planes fall into two orthogonal families, and a
single-layer sheet is one row of the lattice, so it can build on one family only.** `8 bp × 33.75°/bp =
270.0°` exactly: the empty family points **out of the sheet plane**.

**The second cheap bound is the one I did not expect.** Because the register departure at B-DNA's preferred
10.5 bp/turn is *linear in the base-pair offset*, the unoccupied site 8 bp away is off by **4.286°** while the
sheet's own next in-plane crossover, 16 bp away, is off by **8.571°**. **The site the sheet does not use is
less strained than the site it does**, by exactly a factor of two, and that is an identity rather than a
measurement.

**The census, complete over all 32 phases: 161–176 junction sites, of which the sheet builds 49–56.** It
occupies **27.8–33.1 %** of its own lattice, under a third at every phase — and the bound is not an accident:
four azimuths per helix per 32 bp, the two in-plane ones shared with a neighbour and so counted once each,
the two out-of-plane ones its own, so **three site equivalents per duplex and the sheet builds one**. The
in-plane half of that census reproduces `C-0015`'s 56/49 as a **set equality at every one of the 32 phases**,
from a construction that knows nothing about columns or parities — which is what makes the unused half
quotable at all.

**So `C-0054`'s two premises separate.** Exclusivity *at a site* is upheld and is used: an upward hinge's
two partners are the sheet duplex and the arm, which is exactly why it is not an interface crossover. What
is false is the **inventory** premise. The ceiling is not `56 − 14 = 42` with a severed tile but **52 upward
hinges at `C-0054`'s own phase and 60 at the best one, with all 56 interface crossovers retained and the
sheet in one piece at every count** — `CH-0068`.

**And then the result that makes this an honest half-victory: the design count only moves 25 → 34.** The
escape and its price are the *same sentence*. An upward line belongs to **one** duplex, so nothing empties —
and so its roots sit at the bare 32 bp = 10.88 nm, where an interior row sees its two bounding interfaces at
5.44 nm. Against an arm demanding `arm + d = 11.82 nm` that is three arms per row falling to two, and the
cliff is at `arm ≤ 8.19 nm`, i.e. `n ≤ 34`, with no free parameter in it. **§3's 45 still does not place on a
40 nm tile — 30 do — but the reason has changed from severance to a pitch**, and `CH-0066`'s conclusion
survives its own mechanism being removed.

Two smaller findings worth keeping. **The crossover phase has acquired a trade it did not have**: the ten
phases that maximise the upward inventory (0–2, 14–18, 30, 31) are *disjoint* from `C-0015`'s ten that
maximise the interface inventory, because the upward lattice is the in-plane one shifted by 8 bp. And
**`C-0053`'s two named escapes behave oppositely**: sixteen duplexes buy exactly zero, because the constraint
is a pitch along `x` and a row is a row however many there are, while a **49.25 nm** edge delivers §3's own 45
with the host intact — the first configuration in this programme at which that happens, and a specification
question rather than a physics one.

**The motif is half-published, and the honest statement is the split.** The site, and a crossover on it, are
the elementary step of square-lattice multilayer origami. A **free lever** held to a single-layer sheet by one
crossover at that azimuth was **NOT FOUND** in 62 recorded EuropePMC queries across fifteen families — with
`"crossover azimuth"`, `"free azimuth"`, `"interlayer crossover"`, `"cantilevered helix"` and
`"hinge joint" AND "crossover"` all returning zero — and the 2023 mechanics review, read directly, says every
published origami hinge is *"realized using ssDNA connections"*. A crossover-rooted flexure hinge, in plane or
out of it, is this programme's own construct; `T-119` does not make the element more speculative than it
already was, and it does not make it less.

**The closest thing to a refutation is a yield cost, and it is in the same paper.** Ke et al.: *"some staple
breaks must be implemented between crossovers 8 bp apart … We observed significantly lower yield for these
structures. Introducing these breaks may be destabilizing"*, and they raised the yield of their 8 × 8 block by
**omitting** crossovers. An upward hinge splits a 16 nt staple domain into two of 8 — 68 of them at 34 arms.
`CLAUDE.md`'s *"the closest published precedent to a novel motif is often a failure named in one clause"*,
landing in the body of the paper this time rather than in a supplement.

The largest open item is new and is `T-121`: **34 duplexes stacked above the tile are mass and rigidity in a
direction no model in this programme contains.** `C-0054`'s tables are all computed on *consumed* interfaces
and simply do not apply to a host that loses nothing — so nothing currently replaces them.

## Iteration 9 — `T-113`: the mandate was an equality on a SUM, everyone read it as an equality on each path, and freeing the distribution makes the tile flat

`C-0047` closed with five open items and named the second *"the last unexplored axis, and the only one that
could attack `CH-0034`'s floor"*: every attachment in this corpus is an **equal** spring, and `C-0017`'s
mandate fixes the **total** 33.3333 pN/nm. It is a placement condition — `100 pN / 3 nm`, written on the
secant of the whole coupling — and nothing anywhere requires the paths to share it equally. Nobody had asked.

**The answer is yes at three columns and no at one, and it is the first time anything in this programme has
made the Gen-1 tile flat.** At `C-0015`'s 45 attachments, under `C-0022`'s solved load, redistributing the
same total — no extra stiffness, no extra paths, no new motif — takes the peak dishing from **0.2182** of the
free-tile stroke to **0.0753** under a one-parameter rule and **0.0544** under a 45-parameter optimisation,
both inside `T-5b`'s 0.10. `CH-0034`'s count axis saturates at **0.149** and never reaches it: **225 uniform
attachments cannot do what 45 unequal ones can.**

So `CH-0034`'s Ground 2 needs one word changed, and that is `CH-0071`. It calls the 0.149 a **floor** and
attributes it to *"a rim collar 8.9 nm wide"* that *"no interior attachment can reach"*. The attachments that
reach it are the ones **already there**: on a 3 × 15 grid the outer columns stand 6.67 nm from the edge,
inside the collar, and `C-0047`'s own along-helix bending length (12.83 nm against a 13.33 nm pitch) says
their influence patches cover it. The equal-spring assumption spends the same stiffness on eleven
middle-column stations whose patches are entirely interior. **The repair is not to reach further, it is to
stop paying for reach that is not needed** — and the design is one sentence: *the 34 stations within 6.7 nm
of an edge carry five times the other eleven.*

**The design is a rule, not an optimiser's table.** The full 45-parameter search is worth a further 27.8 % on
top of the rim rule and needs one near-empty path to get it. And the rim family is **not monotone in either
parameter**: the flat window is `5 ≤ R ≤ 20` at a 6.7 nm collar and a single ratio (10) at `C-0022`'s own
8.94 nm one, because at a large ratio the interior springs carry nothing and what is left is an attachment
scheme placed *only* on the collar, dishing between its own supports again. **The family converges on a
placement.** What non-uniformity buys is `C-0015`'s *"shapes, not counts"* with the shape chosen continuously
instead of by an integer — which is also why matching the stiffness to the **load** (the local argument, and
the one the task's own cheap bound suggested) is worth only 25 % against the rim rule's 65 %: a plate is not
a local response, and the length that matters is the structure's influence patch, not the load's collar.

**The cost is a force, and it is affordable at 45 paths and not at 15.** A path carrying `k_i` delivers
`k_i·s`, so the 10 pN unzip allowable caps it at `a/s` and, against the uniform share, at a ratio
`n·a/(s·K)` — 4.5 at 45 paths and **1.5 at 15**, at §3's acceptable stroke, and 0.45 at 15 paths at the
desired one, where not even the uniform coupling is admissible (`C-0049` from the other side). At 45 the
ceiling **costs nothing**: the capped and uncapped optimisations return the same distribution to the last
digit. The flat design sits at 2.762 pN per path (3.62× clear), 0.784 pN in the worst crossover (12.8×) and
1.13 pN of duplex shear against a 48–65 band, and pays 24 % more thermal force — `C-0014`'s penalty, which
is **linear** in a path's share and not its square root, because the tile's rigid-body coordinate has
variance `k_BT/K` against the whole coupling and every path sees the same amplitude.

**At `C-0041`'s buildable 1 × 15 the axis fails, exactly as the task predicted.** 13.0 % admissible
improvement, still 6.0× the tolerance, still **1.96× worse than having no coupling at all**. Fifteen springs
on the single line `x = 0` can only reshape the across-helix profile and the dishing there is the along-helix
bow. **A distribution can reweight a placement; it cannot move it.**

**And then the finding nobody had gone looking for: the uniform coupling is already flat at the compressed
states.** All five of `C-0022`'s solved states, 3 × 15 uniform: 0.209–0.255 at the three 10 nm gaps, and
**0.0796 and 0.0710 at the 5 nm and 2 nm ones**. The flat rim design is flat at three of five and dishes
0.187 at the 2 nm gap — 2.6× *worse* than the uniform coupling there. A minimax over all five reaches a worst
case of only **0.1587**, so **no distribution found is flat everywhere**, and that is a "not found" rather
than a "does not exist" (`T-123`). Flatness bought by tuning is flatness owed at one state: the sixth
instance in this project of a quantity that is not well posed without the state it is read at, and a
flatness count now needs a load case **and** an operating state.

Two smaller results. **The break-even moves from three columns to two** — a redistributed 2 × 15 dishes
0.2512 against the free tile's 0.3079, where `C-0047`'s uniform 2 × 15 was a net dishing source at 0.3504.
And **the lattice/plate excess is a function of the distribution, not only of the count**: −8.1 % at the
uniform coupling (`C-0047`'s number, reproduced) and **+23.9 %** at the flat rim design, so the plate alone
would report 0.0608 where the lattice reports 0.0753. `CLAUDE.md`'s *"a discretisation is not automatically a
relaxation"* confirmed a second way, and the flat verdict survives on both models.

**What made this affordable is one identity.** A support enters the lattice stiffness as `k_j b_j b_jᵀ` —
`C-0009`'s own rank-one anchor update, generalised to rank `n` by Woodbury — so one factorisation of the
**unsupported** model plus `n + 1` load cases prices every stiffness distribution at the cost of an `n × n`
solve, and the dishing projector and the grid sampling being linear makes a candidate's dishing field a
combination of `n + 1` precomputed grids. 45-parameter optimisation, four starts, twenty-five sweeps: 73 000
evaluations in about a minute, against a 855-degree-of-freedom Cholesky each. It reproduces the assembled
solve to `1.5e−12`, and Maxwell-Betti reciprocity of the influence matrix — measured between two different
quadratures, not imposed — holds to `1.2e−15`.

**A prediction that failed in code, on the first run:** the *uncapped* optimisation returned a worse point
than the *capped* one it strictly contains. A descent over a superset that starts elsewhere can do that, and
the fix is structural rather than numerical — the capped problem now runs first and its answer joins the
uncapped problem's start set. That they then coincide to the last digit is itself the finding that the
per-path ceiling costs nothing at 45 paths.

**The cheap bound did not fire, and running it was still right.** Dishing is affine in the attachment
*forces*, so the least-squares minimum over the whole of `ℝⁿ` — no mandate, no positivity — is a rigorous
lower bound on the peak dishing of every distribution: 0.0027 of the stroke at 3 × 15. Had it exceeded 0.10
the whole optimisation would have been unnecessary. It did not, and the best found sits 20× above it, which
measures how loose the bound is (it ignores the mandate, and the mandate is what binds) rather than how much
room the search left.

The largest open item is new and is `T-122`: **nothing here says a per-path stiffness can be BUILT to a
prescribed value.** `C-0030`'s flexure goes as `span^−3`, so 5:1 is a 1.71× span ratio — plausible on its
face, unchecked, and the thing on which `CH-0071` turns. If a coupling can only be built with equal paths,
`CH-0034`'s floor returns with that qualifier attached.

## Iteration 9 — `T-120`: the sheet stops being a *material* five crossovers before it stops being a *body*, and three of `C-0009`'s four criteria cannot see it

`C-0054` and `C-0053` both closed last iteration on the same object: a single-layer sheet whose crossovers a
flexure array has spent, held together at the ceiling by **one crossover per interface**. Both computed
everything on the **lattice**, so nothing either reported is wrong. `T-120` asks the question neither did —
whether the *continuum* statements the rest of the programme rests on still describe that body — and
`C-0054`'s own *"Still open"* item 5 names the check and does not run it.

**The whole answer is a cheap bound, and it took four operations on `C-0009`'s own number.** The depleted
lattice turns out to be parametrised by **one** quantity, the retained crossover count, through
`p_eff = p N/N_ret` and `D_⊥ = k_θ d/p_eff` — the only pair that reduces to `C-0009`'s published constants
*exactly* at `N_ret = N`. Every criterion is then that claim's number times an exact power of `N_ret`:
`ℓ_⊥/d ∝ N_ret^(1/4)`, `ℓ_∥/p_eff ∝ N_ret`, and the anchor-patch count `∝ N_ret^(5/4)`. **So each of them
inverts**, and `CLAUDE.md`'s *"invert a length-dependent allowable, do not just evaluate it"* is what turned
a table into a verdict: the patch holds **0.694** crossovers at the ceiling, and it reaches **one** at
**37.26 spent** — five crossovers *below* `C-0054`'s connectivity ceiling of 42, and below the whole of its
`39 ≤ n ≤ 42` window. That is `CH-0069`.

**The surprise is that the two matched criteria are useless here, for opposite reasons.** `ℓ_⊥/d` reads
**1.061** at the ceiling — above one, so it never fires — and the reason is an identity worth stating:
`ℓ_⊥/d` depends only on `D_⊥/k_f`, so **consuming to a quarter of the crossovers is exactly a fourfold
stiffer foundation for it**. The ceiling's 1.061 *is* `C-0009`'s own `k_f × 4` corner, to the last digit,
where the other two criteria separate the same pair of states by exactly `2√2`. An across-helix criterion
cannot tell a depleted sheet from a stiffer polymer layer. And `ℓ_∥/p_eff`, the one `C-0009` singled out as
*the* criterion that fails, is **degenerate**: inverted, it demands **67.2** crossovers on a sheet that has
56, so it reads "invalid" at zero consumption and at every consumption, and carries no information about
depletion at all. The criterion that discriminates is the one `CLAUDE.md` recommends for carrying no
convention — the count of crossovers inside an anchor's influence patch.

**A criterion is not a model, and that distinction is most of the work.** A third model was run beside the
plate and the lattice: fifteen uncoupled Euler-Bernoulli duplexes on one shared Winkler foundation, which is
`C-0009`'s own grillage with *every* crossover consumed. The four thresholds a design might read as "where
the plate fails" then come out in a strict order — **28** (the beam array becomes the nearer model on the
point compliance) **< 37.3** (the criterion) **< 42** (connectivity, `C-0054`'s) **< 45** (a smooth load
finally notices, and that is severance) — **1.61× apart in the crossover count**. `C-0054` reads the third
and reports the fourth; neither is where the reduction stops applying.

**`C-0009`'s smooth/point split survives the ceiling and becomes a change of model class rather than a sign
of error.** The same body at the same consumption is a perfectly good plate for `C-0022`'s distributed load
(**14.9 %** at the ceiling, and the plate is still the nearer model) and not a plate at all for a
point-coupled one (**33–79 %** under a concentrated lever; the point compliance a coupling element feels
varies **2.53×** over one crossover cell where the plate says 1.14×). A plate cannot be inhomogeneous, so
that spread is not an error in the plate's number — it is a quantity the plate does not have.

**Three standing verdicts were checked and all three survive, one of them by strengthening.** `C-0006`'s load
distribution stands because the hinges carry `9.0e−4` of the strain energy on the **intact** sheet, so
removing three quarters of them takes it to `5.4e−4` and nothing moves — the load path was never through the
hinges under a distributed load, which is the mechanism behind `C-0054`'s "the peak crossover force falls to
zero". `C-0010`'s *"a 2× change in `D_⊥` moves the answer by 2.5 %"* not only survives but gets **stronger**:
3.07 % intact, **0.99 %** at the ceiling, while the amplitude rises 1.71×. So it is insensitivity to the
**rigidity**, and the amplitude moves with the **connectivity** — `C-0054`'s warning confirmed on its own
lever. `C-0047`'s 0.218 and 0.695 reproduce, and nothing reaches `T-5b`'s tolerance at any consumption, which
is `CH-0034`'s saturation and not a new failure.

**A placement artefact found in an upstream claim, and bracketed rather than inherited.** `C-0054`'s `SPREAD`
pattern is round robin over the *interfaces* — which is what makes its pigeonhole tight — and its column
tie-break takes the lowest available one, so at the ceiling all fourteen survivors sit in the two lowest
columns and two thirds of the tile has no across-helix path. A `staggeredRetention` is the other extreme, and
the two bracket a real design: identical count, identical density, **1.68×** in the mean distance from an
anchor to the nearest surviving crossover, and 28 of 45 attachment stations with no crossover in their patch
against 30. **Which crossovers survive is a design variable and no claim owns it.**

**What the census taught, and it is the reason the criterion is the right one.** Below one crossover per
patch, a density and a count stop being the same statement: 0.694 is 0 or 1 depending on where the anchor
lands. That is discreteness in its purest form, and it is invisible to any smeared rigidity.

`tools/verify.sh` **BUILD SUCCESSFUL**, with three concurrent agents' mid-TDD files dropped by `--drop-file`
— including, for the first time in this project, a **main** source (`coupling/NonUniformCouplingStudy.kt`),
which a study main is safe to drop because nothing imports it.

**One process finding from the same iteration, recorded in `CLAUDE.md`.** The first two runs of this study
produced result files differing in **one integer** — the optimiser's evaluation count — with every physical
number identical. Rounding at the serialisation boundary does not save that field, because it is not a number
the model computes: it counts the **steps the search took**, and a last-ulp difference in the objective (the
JIT recompiling a hot reduction mid-run) flips a strict comparison so that the descent reaches the *same*
optimum by another route. Removing it exposed two more of the same kind — the sweep count and the label of
the winning start. All three are gone; the last-sweep improvement stays at **two** significant digits, which
is what survives, and two independent runs then diff byte for byte. It is `CLAUDE.md`'s argmin trap one level
further out: an index is not a rounded double, and neither is a step count.

---

## Iteration 9 — `T-71`: the dihedrals of the 90° junction, and an objective that could not see them

Claim [`C-0057`](gpd/claims/C-0057-backbone-torsion-closure.md), challenge
[`CH-0070`](gpd/challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md).

**What was asked.** `C-0029` filed `T-71` against itself: its closure search tests one **necessary**
condition — a phosphate pair inside the measured `[0.60, 0.70]` nm phosphodiester step with no van der Waals
overlap — and *"no backbone torsion angle is checked"*.
`C-0042` then closed a pair at a binding link of **0.6969 nm** and flagged it as the place *"where the torsion
check is least comfortable"*; `C-0052` closed a trio and recorded that it inherits the same ceiling.
So `T-71` had become the standing ceiling on the whole surviving truss branch, and it could only make the
answer worse.

**What was done.** Nothing was taken from memory. `tools/T-71-bdna-backbone-survey.py` queries the RCSB for
X-ray, DNA-only entries at ≤ 2.3 Å, downloads **876** of them, and measures the six backbone torsions, the
glycosidic χ, the pseudorotation phase and the covalent geometry of **13 084** phosphodiester linkages;
`tools/T-71-emit-kotlin-constants.py` **generates** the Kotlin constants, so no number is transcribed by
hand. Both residues of a link are then placed by the junction's own solved geometry and the phosphodiester
between them closed by exact inverse kinematics.

**The answer has two halves and neither alone is honest.**
The dihedrals **do not close** at any of the three reported optima — 0 of 4 links, 1 of 8, 2 of 6.
Four of the eighteen are excluded by a **closed-form reach bound** and close at *no* torsion whatever:
`C-0042`'s two 7 bp legs would need an `O3′–P` bond of **0.2517 and 0.2460 nm** against a covalent
**0.16022 ± 0.00191**. The rest fail on population — `C-0029`'s binding link needs **ε = −22.9°**, carried by
**0.015 %** of the measured linkages, and its second needs **β = 27.4°**, carried by **zero of 15 457**
residues.
But a **torsion-feasible placement exists in `C-0029`'s own search space**: of **69 120** placements, 3 546
close on distance, **1 855** survive the reach bound, and **18 of the 100 solved** close at torsion level.

**What surprised us, and it is the finding.** The objective is not merely incomplete, it is
**anti-correlated** with the missing condition. Minimising a *window residual* is satisfied anywhere inside
`[0.60, 0.70]`, so the shortest-gap tie-break drives every optimum to **0.600 nm**, the very edge — and the
edge is exactly where a backbone cannot go. The feasible placements sit near **0.690 nm**, in the interior,
which a residual-minimising search has no reason to visit. The same fact shows up disguised as a
"sensitivity": moving the phosphate radius from 1.00 to 0.90 nm, *inside `C-0029`'s own declared bracket*,
takes the single junction from 4.55 σ to 2.99 and to 21.64 at 0.8901 — a **7× swing across a 0.01 nm change
in a convention**, because it is selecting a different argmin on a coordinate orthogonal to the answer.
`C-0042` was right to worry about its 0.6969 nm link; it is not uncomfortable, it is the one the bound
excludes.

**Two method failures, both caught by their own gates.**
The first calibration reapplied a single measured nucleotide at its own fitted helical screw and called it an
ideal duplex. It is out by **0.0145 nm** in the reconstructed `O3′···P` — **7.6 bond-length standard
deviations** — because a local helical axis fitted from coordinates is an approximation and at a covalent
bond's tolerance that approximation is not small. The free limiting case was rebuilt on the medoid's **actual
successor residue**, and a real dinucleotide then closes at 0.31–2.74 σ under *both* readings.
The second judged a torsion septet by its distance to the nearest of twelve k-means conformer classes; the
diffuse classes have 99th-percentile radii above **150°**, so the test admitted almost anything and the
baseline landed 133° from its own class and was called populated. It was replaced by a **marginal
ten-degree occupancy histogram** with a floor 28× more permissive than uniform — which is what lets the claim
say *"β = 27.4° lies in a bin holding zero of 15 457 observed residues."*

**A number this programme has been citing without a primary source now has a measurement.** Bosco et al.'s
*"C3-endo 0.6 nm to C2-endo 0.7 nm"* is verified verbatim, but its own references are two **textbooks**,
neither reachable. Measured here on 13 084 linkages the pair is **0.607 and 0.664 nm** — the ordering and the
pucker coupling confirmed, the C2′-endo end **5.1 % shorter** than the number every search in this repository
has been using. The window is wider at the top than the backbone actually is.

**A negative that is falsifiable by one paper.** No torsion-level or atomistic check of a ~90° out-of-plane
origami junction exists — 33 EuropePMC and 6 arXiv queries with their strings recorded. And the atomistic
origami literature stops at the *helix* level: Maffeo, Yoo & Aksimentiev (2016) characterises a crossover by
three helix angles and contains *"torsion"* 0 times and *"dihedral"* 0 times.

`tools/verify.sh` **BUILD SUCCESSFUL**, 25 gate-named tests in `BackboneTorsionTest`, with a concurrent
agent's mid-TDD `coupling/NonUniformCouplingStudy.kt` dropped by `--drop-file`.

**One process failure of ours, recorded in `CLAUDE.md`.** `pkill -f "plenty-of-room-study"` was used to stop
our own run and it killed a **sibling agent's** study as well — the snapshot directories share the prefix.
`CLAUDE.md` already warned that `pkill -f` kills your own shell; it now also warns that on a shared checkout
it kills your neighbours. The sibling's `coupling.NonUniformCouplingStudyKt` run was lost and had to be
re-run by its own agent.

### Iteration 9 — a premise falsified, a check that was never run, and a mandate misread for six iterations

Four loops, each aimed at a premise the programme had been standing on rather than at a new number.

**`C-0055` falsified the premise `C-0054` had itself named as its own falsifier — and `C-0054` had named it
correctly.** A square-lattice helix has **four** nearest neighbours and crosses to one of them every 8 bp,
sharing crossovers with a *given* neighbour every 32 bp; a single-layer sheet is one row of that lattice, so
it builds on one of the two orthogonal plane families and **leaves the other empty**. And `8 bp × 33.75°/bp`
is **270.0° exactly**, so the empty pair points **out of the sheet plane**. The unused site is in *better*
register than the used one — 4.286° against 8.571°, exactly 2:1 — and the sheet occupies **27.8–33.1 %** of
the available sites at every phase. So `C-0054`'s **exclusivity at a site is upheld and its inventory premise
is false**: the hinge budget goes 42-with-a-severed-tile to **52–60 with the sheet in one piece**, and
`C-0053`'s 25 to **34**.
**But §3's 45 still does not place**, now for the **root pitch** rather than severance — an upward line
belongs to one duplex, so its roots sit at 10.88 nm against an arm demanding 11.82. `CH-0066`'s conclusion
survives its own mechanism being removed, which is the strongest form a conclusion can take.
The motif is **half-published**: the site and a crossover on it are the elementary step of square-lattice
multilayer origami, but a *free lever* held by one crossover there was **not found** in 62 recorded queries
across fifteen families, and every published origami hinge is an **ssDNA** connection.

**`C-0057` ran the check every closure result in this programme had deferred, and it does not pass.**
The dihedrals **do not close at any of the three reported optima** — `C-0029`'s single junction 0 of 4 links,
`C-0042`'s pair 1 of 8, `C-0052`'s trio 2 of 6 — and four of the eighteen are excluded by a **closed-form
reach bound**, i.e. they close at no torsion whatever (`C-0042`'s 7 bp legs would need an `O3′–P` bond of
0.25 nm against a covalent 0.16). The rest fail on **population**: `C-0029`'s binding link needs `ε = −22.9°`,
carried by 0.015 % of 13 084 measured linkages, and its second needs `β = 27.4°`, carried by **zero of
15 457**.
**And the finding is the second half: a torsion-feasible placement exists in `C-0029`'s own search space —
3 546 of 69 120 — and none of the three claims found one because none of them was looking.** The routing
was optimised on distance, and distance is a necessary condition that the three claims had each said, in
their own words, was not sufficient. This is what a deferred check is *for*, and it is why `C-0029` filed it
as a task rather than guessing.

**`C-0058` found that a mandate had been misread for six iterations.** `C-0017`'s 33.3333 pN/nm is an
equality on a **sum**, and every claim in the corpus had read it as one on each path. Freeing the
distribution — same stiffness, same 45 attachments, same solved load — takes the dishing from **0.2182 of
the free-tile stroke to 0.0753** under a one-parameter rule (the 34 stations within 6.7 nm of an edge carry
5× the other 11) and **0.0544** under a full optimisation, **both inside `T-5b`'s 0.10**, where
`CH-0034`'s *count* axis saturates at 0.149 and never arrives. **225 uniform attachments cannot do what 45
unequal ones can**, and the 0.149 is a property of the equal-spring family rather than of the rim.
Three qualifications travel with it, all in the claim: it is flat at three of five solved states, it needs
three attachment columns that `C-0041`'s packing forbids, and **nothing yet says a 5:1 per-path stiffness
can be built**.

**`C-0056` did the same thing to a criterion.** The depleted lattice turns out to be parametrised by one
number, so every one of `C-0009`'s criteria is its published constant times an exact power of the retained
count — and **inverted rather than evaluated**, the influence-patch criterion reaches one crossover at 37.3
of 56 spent, *five before the sheet disconnects* and inside `C-0054`'s own window. The other two criteria are
useless for opposite reasons, one never firing and one degenerate at zero consumption. Four thresholds fall
in a strict order 1.61× apart, and `C-0009`'s smooth/point split survives as a **change of model class**.

**The coordination worked, twice.** `C-0056` recognised that its own challenge is **conditional on `C-0055`**,
which was running concurrently, and said so in a banner and in the challenge rather than filing a verdict a
sibling had already undermined. And `C-0058` recorded a prediction of its own that failed in code — it had
expected the rim to *stiffen* — keeping the refutation in the claim rather than quietly reversing.

**One coordinator note.** `C-0058`'s intermediate chat report and its filed claim disagreed (0.0999 against
0.0753, a *soft* rim against a *stiff* one); the claim and the final report agree, and `ANSWERS.md` was
written from the claim. That is the second time this session a report has diverged from its own artifact, and
the rule stands: **read the filed claim, never the report.**

## Iteration 10 — `T-121`: a lever held at one crossover is a mechanism, and a mechanism stores no energy

**Task.** `C-0055`'s largest open item, in its own words: *"34 duplexes stacked above the tile are mass
and rigidity added out of plane, which no model in this programme contains."*
`C-0009`'s grillage, `C-0006`/`C-0047`'s flatness and `C-0010`'s variance, re-run on a sheet **carrying**
the array, plus the drainage consequence `C-0004` owns.
Filed as [`C-0061`](gpd/claims/C-0061-stacked-arm-sheet.md), raising
[`CH-0074`](gpd/challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md).

**The cheap bound closed the headline question before any lattice ran, and it is an identity.**
A body attached to a structure at **one** point and otherwise free has a **zero Schur complement** there:
its rigid-body motions span everything a single crossover can impose — the deflection through the vertical
link and the roll through `k_θ` — so the minimised arm energy is identically zero.
That is the same class of statement as *"a uniform load on a uniform Winkler foundation dishes exactly
zero"*, and it was wired in as a runtime falsifier the same way.
34 arms covering **46.3 %** of the tile's plan move `C-0009`'s peak dishing, peak crossover force and peak
duplex shear by `5e−9` relative, and that residual is the arm regularisation and vanishes **linearly** with
it over four decades — which is the numerical signature of an exact zero rather than a small one.

**The surprise is that the escape, its price and its harmlessness are the SAME sentence, three times.**
An upward site costs the sheet nothing *because* it belongs to one duplex; its roots are twice as sparse
*because* it belongs to one duplex; and the arm it roots can never be tied **twice** *because* the pitch
that makes them sparse — the bare 32 bp = **10.88 nm** — is longer than the arm, which is 8.164 nm at
`C-0055`'s self-consistent 34 paths and **9.131 nm even at §3's own 45**.
The single-point attachment is therefore forced by the lattice, and the exact zero is a property of the
design rather than an idealisation of it.

**A second surprise: the bracket has three steps, not two, and the middle one is a torsion bar.**
The task was formulated expecting one tie to give zero and two to give something like a composite beam.
Two ties give **zero bending stiffness at any arm rigidity** — because two points determine a line and a
rigid arm meets both — and what they add is 5.832 pN·nm/rad of torsion, the two hinges in series with the
arm's own `GJ/L`, worth **0.112 %** of the dishing.
The arm's own `EI` is engaged only at **three** ties, i.e. 21.76 nm of arm, 2.67× outside the design range;
and the 19.3× axially coupled second layer needs the crossover's in-plane constant, which `Gen1Tile` flags
as a construction rather than a measurement.

**Mass and drag, in the order the bound put them.**
46.3 % more duplex is `√1.463 = 1.21×` on a quality factor `C-0004` puts at `5.3e−4`; the arms' inertial
force at 1 kHz and §3's 3 nm stroke is `1e−10 pN` against §3's 100 pN.
**Drag is the only channel that moves anything, and it is an upper bound.**
The arms stand on the `+z` face while the layer and the electrode are on `−z`, so `C-0004`'s squeeze film —
a **footprint** problem — is untouched *by construction*; what they add is bulk dissipation in parallel with
the tile's own Stokes term, **9.1 %** of the total, taking the nominal corner 91.2 → **82.9 kHz** and the
**worst** 40 × 40 nm §4(d) margin **22.81× → 20.73×**. Still discharged.
`C-0010`'s `σ_RMS ≤ 3.0 nm` — the most exposed standing verdict — is **identically** unchanged broadband,
because equipartition is `k_BT K⁻¹` and the arms change no entry of `K`; only the **in-band** amplitude
moves, by 4.9 %.

**What the array really changes is where the coupling ENTERS, and that is where a verdict moves.**
On `C-0055`'s own 34 roots a uniform coupling dishes **0.4156** of the stroke against **0.2182** on
`C-0015`'s 3 × 15 and **0.3079** for the free tile — **1.35× worse than no coupling at all**, the pathology
`C-0058` reports at `C-0041`'s 1 × 15 and could not report here because it did not have these stations.
`C-0058`'s flat rim rule reaches **0.1649** there against its published 0.0753, above even `CH-0034`'s
0.149 saturation floor for equal springs on a grid. Hence `CH-0074`: **the flat design lives on a station
set no placement claim supplies**, and `C-0058`'s own sentence — *a distribution cannot repair a placement* —
applies to its positive result as well as to its negative one.

**And a placement variable nobody had noticed.** `C-0055`'s scheduler fills every row greedily from the
low-`x` end and points every arm the same way, so its 34 roots are **not centro-symmetric**: the coupling
centroid sits at `x = −8.80 nm` on a tile running −20 to +20.
Reflecting the odd rows is one line, free, lands on the same column lattice and is inside `C-0055`'s own
per-row independence — and it is worth 0.4156 → 0.3558 uniform and 0.3419 → 0.2250 at rim × 5, i.e. **more
than the whole rim rule buys on the unreflected set**. That is `T-125`.

**Two more things recorded rather than solved.** The peak crossover force is **8.3×** larger at the arm
roots than on `C-0015`'s inset grid (1.255 against 0.150 pN, still 8× clear of the unzip band) — a coupling
that enters on the crossover column lattice loads that lattice. And the arm slab sits 1.69–3.69 nm above
the sheet over 46.3 % of the plan, which is the same `+z` half-space `C-0035`'s only buildable mounting ties
down through; `C-0035`'s note that *"the tile now carries no out-of-plane element at all"* stops being true
of any design that adopts the escape. That is `T-126`.

**Process notes.** The augmented assembly reconstructs `OrigamiGrillage`'s private degree-of-freedom layout
from its **public** API rather than modifying a main source three agents share, and asserts the
reconstruction against that lattice's own `basisAt` as a gate-3 test — a shared file untouched and the
reconstruction still falsifiable. The applied load is recovered exactly as `f = K_host q_host` from the bare
lattice's own solution, which needs no access to its private assembly at all.
`tools/verify.sh` was run three times: the working tree as it stood gave **1704 tests, 5 failed**, and
**all five failures were in two concurrent agents' mid-TDD test classes** (`TorsionFeasibleRoutingTest`,
`BuildableStiffnessRatioTest`) — none in `structure`. Dropping those and the sibling **main** source that
failed `compileKotlin` (`coupling/BuildableStiffnessRatioStudy.kt`) by `--drop-file` gives a clean run.

---

## Iteration 10 — `T-122`: can a 5:1 per-path coupling stiffness ratio be BUILT?

**Claim [`C-0060`](gpd/claims/C-0060-buildable-stiffness-ratio.md), raising
[`CH-0073`](gpd/challenges/CH-0073-the-along-helix-scatter-rule-reverses-on-a-non-uniform-coupling.md)
against `C-0026`'s build rule. Study `coupling.BuildableStiffnessRatioStudyKt`, model
`src/main/kotlin/coupling/BuildableStiffnessRatio.kt`, 28 gate-named tests.**

`C-0058` made the Gen-1 tile flat for the first time by giving 34 rim stations 0.921 pN/nm and 11 interior
ones 0.184, and said in its own maturity line that **nothing in it says a per-path stiffness can be built to
a prescribed value**. This iteration priced that.

**The answer is YES on the stiffness and NO on the placement, and the interesting half is why the first one
is easy.** This project has met a quantisation trap before and lost: `C-0023` found that a two-sided preload
is a *mounting offset*, i.e. a **length**, that DNA quantises it at 0.34 nm, and that the requirement asked
for 0.041 nm — the quantum was **8.3× coarser than the requirement** and the branch died. The instinct was
that a prescribed stiffness would go the same way. It does not, and the reason is one line:

> **a preload is a length and a stiffness is a POWER of a length**, so the same 0.34 nm quantum enters
> divided by the member in units of itself — 87 base pairs, not one.

So one base pair is **1.0–19.1 %** of a level's own stiffness across the whole catalogue, against a flat
ratio window this task **measured rather than cited** at `3.5 ≤ R ≤ 20` — 471 % wide. Quantisation is 25×
finer than the requirement. The declared falsifier (*one quantum taking the ratio out of the window*) did
not fire at any of fourteen element/level pairs; all seven settings reach both levels, realised ratios run
4.667–5.144, and **all fourteen built designs are still flat** (0.0715–0.0815 against `T-5b`'s 0.10).

**The second cheap bound turned out to be load-bearing rather than decorative.** Rounding the two *levels*
independently misses `C-0017`'s 33.3333 pN/nm by **0.40–5.44 %** — and that is a *placement* error, 5 % of
the force the actuator delivers, not a rounding nuisance. It is recoverable only because the mandate is an
equality on a **sum**: moving individual paths by **one** base pair takes the worst miss to `1.3e−4` in at
most 18 moves. The price is that the design is **3–4 distinct staple lengths, not two**. The task had
expected the level rounding to be the whole story.

**`T-45` is answered as a threshold, which is what `CLAUDE.md` asks for when the measurement does not
exist.** The built design loses the flatness verdict at **34.6 %** relative scatter (31.6 % with the total
held fixed), which is 2.04× `C-0026`'s 17 % break-even and 6.9× a 5 % staple tolerance. Two things were
worth the run: the two stiffness *populations* do not even overlap until `(R−1)/(R+1) = 66.7 %`, so
**flatness binds at half the amplitude the ordering does** — the ordering criterion the task was framed on
is not the one that matters; and **small scatter helps**, lowering the dishing 0.0767 → 0.0571 at 10 %, so a
linearised tolerance budget would have got the sign wrong.

**`CH-0073`, which was not anticipated.** `C-0026`'s build rule is *"if they must differ, let the error be
along the helix"*, because a scatter alternating along the helices restores exactly zero crossover force.
On `C-0058`'s **three-column** non-uniform design the along-helix index **is** the rim/interior index, so
that direction is no longer a symmetry — it is the design variable — and it is the pattern the flatness
verdict tolerates **least**: 31.6 % against 69.8 % across the helices, a factor of 2.21, with `C-0017`'s
total held. The crossover channel still prefers it (−0.113 pN against −0.051 pN at 10 %), so the two
channels now **rank the same two build rules oppositely**. `C-0026` could not see this because on its own
equal-spring coupling the dishing is 0.2182 at zero scatter — already outside the tolerance, with nothing
for a scatter to lose. **A build rule derived on a design that fails a requirement cannot be tested against
that requirement.**

**What actually fails is the array, and the non-uniform design makes it worse.** `k ∝ span^(−3)` means the
5× softer path is a `5^(1/3) = 1.71×` longer one, and a mixed array is priced at the **longer** member:
`C-0030`'s coupled flexure needs a **52.36 nm** interior span on a 40 nm tile and places **0** of 45 stations,
`C-0023`'s `E3` places 15, the hinge arms 30 and 15 — bounded by plan area rather than by `C-0053`/`C-0055`'s
root pitch, which is worth recording because the arm branch reports the root pitch as its obstruction. Only
`C-0023`'s `E4` places 45, and it needs the second ground under the tile that `C-0023` excluded it for.
**So the qualifier `C-0058`'s verdict needs is about the ARRAY and not about the ratio** — and that is
`C-0041`'s standing obstruction, not a new one. Opens `T-127`: does a genuinely *mixed*-span array pack,
long members where the tile is emptiest, where a uniform sweep of one length says it cannot?

**A small free result.** The best one-parameter ratio at `C-0058`'s own 6.70 nm collar is **7, not 5** —
0.0653 of the stroke against 0.0753, a further 13.4 % and 70 % below the uniform coupling. `C-0058`'s
six-point sweep did not visit it, it costs nothing to build, and it sits further from both edges of the
window, so it is the more scatter-tolerant choice as well.

**Process notes.** The scatter threshold is bisected on the **bracket width** and takes the **first**
crossing rather than assuming monotonicity — asserted on a metric that crosses, falls back and crosses again
— and a threshold never reached is reported as a flag with the scan ceiling, never as `Infinity`. One
`NaN` did escape into `kotlinx.serialization`: a convergence record dividing a bracket width by a threshold
of exactly zero, which is what the *uniform* coupling gives because it is already outside the tolerance at
zero scatter. Guarded absolutely, per `CLAUDE.md`'s own rule. The scatter sweep is reported in **two**
readings — as built, and renormalised to the mandate — because a pattern collinear with the rim/interior
split moves the total by 21 % at its own threshold and the two effects had to be separated; the conclusion
survives both, and the renormalised one is the stronger. Three concurrent agents shared the checkout:
`tools/verify.sh` and `tools/study.sh` were run with `--drop-file` on `T-124`'s
`src/test/kotlin/anchoring/TorsionFeasibleRoutingTest.kt` and `T-121`'s
`src/test/kotlin/structure/StackedArmSheetTest.kt` and `src/main/kotlin/structure/StackedArmSheetStudy.kt`;
the full suite is **BUILD SUCCESSFUL, 0 failures** (8 m 14 s, whole suite on the finished tree) and the result file diffs byte-for-byte identical on two
independent runs.

## Iteration 10 — `T-124`: the alignment is free at one junction, expensive at two, and not found at three

`C-0057` left one question and named it the direct successor to its own task:
*"whether a placement can be simultaneously torsion-feasible and correctly ALIGNED."*
It had proved the three reported routings infeasible and the search space non-empty,
and it had reported the chords of its two best feasible placements at 159.0° and −51.0°
against the 90.0° the design wants — but those were ranked by **reach margin**, so they said nothing either way.

**The method is one sentence.** `C-0057` ranked its census by reach margin and solved the best 100.
Rank the **same** feasible set by **misalignment** and solve in that order,
and the first placement that closes *is* the best-aligned closing placement on the grid.
Nothing about the chemistry is re-derived; what is re-derived is which placement.
`C-0057`'s census re-derives from its own libraries at **departure 0** —
3 546 covalent and 1 855 reach-feasible of 69 120 for independent staples, 280 and 137 for the scaffold excursion.

**The cheap bound written to bind did not, and the falsifier was declared in advance.**
`C-0029`'s ±16.87° allowance is the azimuth interval `ψ₀ ∈ 120° ± 16.87°`, 18.74 % of the circle.
If no reach-feasible placement had its azimuth in it, the aligned design died before any torsion solve.
The reach-feasible set turns out to occupy **118 of the 120** azimuth values with **414** placements in the band
and a best attainable **0.0°** — so the bound excludes nothing, and the whole answer had to come from the expensive solve.
That was written into the Plan section as the thing that would make bound 2 useless, and it happened.

**The answer degrades monotonically in the number of junctions that must share a seat.**
One junction closes at a chord of **90.0° exactly** — `cos²ψ = 1.0000`, binding link **0.643 nm**,
in the *interior* of the measured window where `C-0057` said the feasible placements live,
on the seat duplex's own axis, 7 of the 120 best-aligned.
Two junctions close at **every** separation from the 6 bp steric floor to 12 bp — a stronger existence result than `C-0042` could give —
but only **18 of 90** axial positions carry a closing placement at all, so the pair takes whatever chords those positions offer:
**33° / 69° / 57° / 6° / 6° / 33° / 33°** against `C-0042`'s 0.00° everywhere.
Three junctions on one crossbar close at **none** of the 24 best-aligned of 750 reach-feasible lattices, 134 junction solves, 0 closing.

**`CH-0056` is upheld and its consequence is not, and that is the finding.**
A free duplex's chord inherits no lattice phase — the chord azimuth is `ψ₀ + Δ/2 + π/2` and `ψ₀` is continuous —
which is why *one* junction reaches 0.0°.
But **torsion feasibility is a relation between two bodies**, so it depends on `ψ₀` relative to the seat's phosphate lattice,
and a pair must find two feasible placements at a **fixed separation**.
The continuum is free; the feasible subset of it is not, and a pair samples the subset twice.

**Two recommendations move.** `C-0042`'s **7 bp** row — the number it resolved `C-0037`'s *"between 6 and 8"* to — is the **worst** of the seven,
at 69.0°, worth 12.8 % of the base couple; and 69° is past the half right angle at which `C-0037`'s `TwoLinkBase` invariant
stops being able to represent the base at all, so the pipeline **refuses** it rather than reporting a number.
Nine and ten base pairs deliver 6.0°, inside `C-0029`'s own allowance, worth 98.9 %.
And `C-0029`'s recommended **scaffold excursion cannot be aligned**: 1 of 120 closing, at 39.0°, against independent staples' 7 at 0.0°.
Hence `CH-0072`.

**What did not move is the mechanics, and saying so is the point.**
Carried through with the pair's own 6.0° base floor, the crossbar's 6.0° cap floor and `C-0052`'s leg-is-one-body budget on their sum,
all fifteen quantised leg lengths still pass all nine predicates at **1.81–2.45 / 1.36–1.84**
against `C-0052`'s aligned **1.81–2.43 / 1.36–1.83** — the same band, best point marginally better.
The reason is that the binding misalignment was never the chemistry: `C-0052`'s twist budget runs 0.3°–89.8° over the envelope,
and a 6.0° floor is inside it almost everywhere.
**A chemistry result that costs the statics 1 % is worth reporting as costing 1 %.**

**And one sensitivity stopped existing.** `C-0057`'s falsifier 4 fired on the argmin: moving the phosphate radius
from 1.00 to 0.90 nm swung the single junction's strain by **7×**, because a distance argmin is unstable under a convention.
Ranked on alignment the verdict is stable — an aligned placement closes at exactly 0.0° at 1.00, 0.90 **and** `C-0057`'s measured 0.8901 nm.
Optimising on the right quantity removed a sensitivity that optimising on the wrong one had manufactured.
The one axis that still moves it is the **wide 154° groove**, which `C-0029` already names as the parameter the base couple is most sensitive to.

**What is left, and it is one thing.** The trio is a *"not found within the budget"* and the budget is thin —
24 of 750 lattices, two candidate azimuths per junction, two crossbar lengths, one row pitch.
That is `T-127`, and it decides the branch: if the cap has no routing at a real budget, the truss closes,
which §7 wants early rather than late.

**Process notes.** The study runs in **14 min** and its expensive half is ~2 000 inverse-kinematic link closures;
the census enumerations that dominate the *apparent* cost are seconds.
The first run was written with the **single junction's** misalignment floor carried into the truss mechanics;
that is wrong — a truss stands on two legs and the pair is strictly harder — so it was re-run with the pair's floor,
and that is what turned `C-0042`'s 7 bp row from a degraded design point into a `NOT REPRESENTABLE`.
`tools/verify.sh` needed `--drop-file src/test/kotlin/structure/StackedArmSheetTest.kt` while a sibling agent's `T-121` was mid-TDD;
that file is green now and the final run needed no drop.

### Iteration 10 — the branch re-derived on the feasible set, and a quantum that was not a trap

Three loops, each closing an assumption a recent result had left exposed.

**`C-0059` re-derived the truss branch on `C-0057`'s feasible set, and two of three junctions survive.**
The census reproduces `C-0057`'s libraries at **departure 0**. The **single** junction closes at a chord of
**90.0° — exactly the flexure axis** — with a binding link of 0.643 nm in the *interior* of the measured
window, which is precisely where a residual-minimising search with a shortest-gap tie-break never looked.
The **pair** closes at every separation from the 6 bp floor to 12 bp — a stronger existence result than
`C-0042` could give — but the alignment stops being free: 33°/69°/57°/6°/6°/33°/33°, and **`C-0042`'s own
recommended 7 bp is the worst of the seven** at 69.0°, past the half right angle where `C-0037`'s two-axis
base is not representable at all. 9–10 bp delivers 6.0°, inside `C-0029`'s own allowance.
The **trio does not close**: 750 of 1 800 crossbar lattices are reach-feasible for all three junctions and
none of the 24 best-aligned survives.
**And the mechanics barely moves** — all fifteen quantised leg lengths still pass all nine predicates at
1.81–2.45 against `C-0052`'s aligned 1.81–2.43 — because the binding misalignment was never the chemistry
but the leg's **own quantised twist**. The agent's declared falsifier did not fire, and it said so.

**`C-0060` found the quantum was not the trap it had been twice before, and gave the reason in one line.**
A 5:1 per-path ratio **can** be built: one base pair is 1.0–19.1 % of a level's own stiffness against a flat
window measured here at `3.5 ≤ R ≤ 20`, so quantisation is **25× finer** than the requirement, where
`C-0023`'s mounting-offset quantum was **8.3× coarser** than its own. The structural reason is that
**a preload is a length and a stiffness is a *power* of a length**, so 0.34 nm enters divided by ~87 base
pairs rather than by one.
Two findings came free. Rounding the two *levels* independently misses `C-0017`'s mandate by 5.44 % — a
placement error, recoverable only because the mandate is an equality on a **sum**, at the price of three or
four distinct staple lengths rather than two. And **small scatter helps** (0.0767 → 0.0571 at 10 %), so a
linearised tolerance budget gets the sign wrong.
**What fails is the array, not the ratio**: `k ∝ span⁻³` makes the soft level the *longer* member, so six of
seven elements do not place. `C-0058`'s qualifier is about the array, and `C-0041`'s obstruction is made
worse.

**`C-0061` settled the price of `C-0055`'s escape, and it is essentially zero in the channels that matter.**
A body attached at **one** point and otherwise free has a **zero Schur complement** there — its rigid-body
motions span everything a single crossover can impose — so 34 arms covering 46.3 % of the tile's plan move
the peak dishing, crossover force and duplex shear by `5e−9`, a residual that is the regularisation and
vanishes linearly with it. **And the single tie is forced rather than chosen**: an upward site belongs to
one duplex, so its pitch is the bare 32 bp = 10.88 nm against an arm of 8.164 nm at 34 paths and 9.131 nm
even at §3's 45. Drag is the only channel that moves — 9.1 %, taking the §4(d) margin 22.81× → 20.73×, still
discharged — and `C-0010`'s predicate is identically unchanged broadband.
Its `CH-0074` is the sting: on `C-0055`'s own 34 roots a uniform coupling dishes **0.4156** against 0.3079
free, i.e. 1.35× worse than no coupling, and `C-0058`'s flat rim rule reaches only 0.1649 there against its
published 0.0753. **`C-0058`'s flat distribution lives on stations no placement supplies.**

**A coordination hazard recurred a third time and is now in `CLAUDE.md` rather than only here.**
`T-121` sent two whole reports under the same claim and challenge IDs with different physics in each; the
filed artifacts matched the second. With `T-79`'s and `T-113`'s divergences that is three in one session,
and the rule is now recorded where the next agent will meet it: **the claim, the challenge and the result
JSON are the artifacts; the report is a courtesy.** A second entry records that `pkill -f
"plenty-of-room-study"` matches *siblings'* snapshots, which killed another agent's nine-minute run — the
same trap `CLAUDE.md` already carried for killing your own shell, in a new form.

## Iteration 11 — `T-125`: the tile was never short of a distribution, it was short of a placement

**Task.** `C-0061`'s *Still open* item 1, in its own words: *"the row phases are a free variable nobody has
swept"* — and `CH-0074`'s charge that `C-0058`'s flat tile stands on a station set no placement supplies.
Filed as [`C-0063`](gpd/claims/C-0063-upward-root-placement.md), which **resolves**
[`CH-0074`](gpd/challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md) and
raises [`CH-0076`](gpd/challenges/CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md).

**The Gen-1 tile is flat on a placement that exists, and it is flat with EQUAL SPRINGS.**
The best of **1 144 858** evaluated 34-root placements dishes **0.0706** of the free-tile stroke under
`C-0022`'s solved load — inside `T-5b`'s 0.10, **4.36×** better than no coupling at all, **5.9×** better
than `C-0055`'s own placement (0.4156), and better than `C-0058`'s **0.0753**, which needed a 5:1
distribution on a 3 × 15 grid that no placement claim supplies.
`C-0058`'s sentence *"a distribution cannot repair a placement"* turns out to have a converse nobody
looked for: **the placement did not need repairing by a distribution, it needed placing.**
On the swept placement `C-0058`'s own rim rule **reverses sign** — 0.0706 uniform against 0.2214 at its
×5 — and a full 34-parameter optimisation asks for a peak ratio of **1.30**, for a further 13.9 %.

**Two lines of arithmetic decided both the shape of the search and where its answer was.**
34 arms on 15 rows at three per row is `3a + 2(15 − a) = 34`, i.e. **exactly four rows of three** — the
whole design space in one identity. And a row's roots can be symmetric about the tile centre only where
`2c ≡ 0 (mod 10.88 nm)`, which holds at **exactly two of the 32 phases, 8 and 24**, both of them inside
`C-0015`'s eight-column ten. The winner is at **24**, is centro-symmetric, and its centroid is `0.000 nm`
against `C-0055`'s `−8.80`. **The exhaustive enumeration the second bound licensed beat the descent it was
meant only to seed** — 0.0706 against 0.0917 at the same phase — so the cheap bound did not merely locate
the answer, it supplied it.

**The flat set is `C-0015`'s ten, which is now the fifth independent construction to land there.**
All ten eight-column phases reach 0.077–0.092; of the other twenty-two, exactly one (phase 15, 0.0976)
gets inside the convention at all. And the phase that maximises the *upward inventory* (60 sites, seven
columns) is not the phase that *places* well — `C-0055` found the first half of that trade and this is
the second.

**`C-0061`'s "1.35× worse than no coupling at all" is a property of a scheduler, not of a lattice.**
Every one of the 32 phases has a placement that beats the free tile. What was wrong with `C-0055`'s array
is that its scheduler fills every row from the low-`x` end, which is a habit and not a constraint.

**The surprise is what reflecting a row actually does, and it is a geometry.**
`C-0061` improved its own placement in one line by reflecting the odd rows, and called it *"free, on the
same column lattice"*. It is on the same **column** lattice and on the **other azimuth**: the two
out-of-plane azimuths of a duplex are a **half-pitch** apart, and at `φ = 0` reflection maps
`k ≡ 2r + 3 (mod 4)` onto `k ≡ 2r + 1 (mod 4)` **identically, for every row**. So the mirrored array is
18 arms above the sheet and **16 hanging into the grafted layer** — the `WEST` azimuth `C-0055` counted,
priced and explicitly refused. A mirror image of an upward array is a downward array. That is `CH-0076`,
and with it the best *buildable* entry of `CH-0074`'s own table was 0.2902, not 0.1649.

**A second half to the same challenge: the host's phase and the array's phase are one variable.**
`C-0061` read `C-0055`'s `φ = 0` roots on the nominal **eight**-column host, which is the `φ = 8` lattice.
It disclosed this exactly; the cost is **38.9 %** — the same 34 roots on their own seven-column host dish
**0.5771**, i.e. 1.86× worse than no coupling rather than 1.35×.

**The method note worth keeping: a placement sweep is a bank problem, not a solve problem.**
Stations enter an `OrigamiGrillage` as *loads*, so the host factorises once; building `C-0058`'s
`InfluenceSurrogate` over **every candidate root** of a phase (~55 of them) and then **slicing** it to a
placement turns each of 1.1 million candidates into a 34 × 34 Cholesky instead of an 855-degree-of-freedom
one. The whole sweep runs in **354 s**, and the surrogate reproduces the assembled solve at departure
**`0.0`** — exactly, because superposition is exact for a linear system.

**Verification.** `tools/verify.sh` **BUILD SUCCESSFUL in 12 m 49 s** — the whole suite, on its own isolated
tree, with **nothing dropped**; 18 gate-named tests for `T-125`; the result file re-run through
`tools/study.sh` and diffed **byte-for-byte identical**, which is the argmin-rounding discipline
(`CLAUDE.md`) working on a sweep that *is* an argmin over 1.1 million candidates.

## Iteration 11 — `T-123`: a distribution flat at every state does not exist, the obstruction is a *sign*, and the five states were four devices

`C-0058` left the question as a *"not found"* and said so honestly:
its minimax over `C-0022`'s five solved states reached 0.1587 of the free-tile stroke from a cyclic
coordinate descent at three starts.
The first thing this iteration did was take that label seriously.
**A max of smooth functions is not smooth, and cyclic coordinate descent is the one classical method that
provably stalls on a kink** — at a point where no single coordinate direction descends although a
combination of two does — so a minimax value reported from one is not evidence of a floor.

The replacement is a log-sum-exp smoothing with continuation, **analytic gradients through the Woodbury
solve** (`∂F/∂k_j = (F_j/k_j²) A⁻¹ e_j`, one extra triangular solve per state on the factorisation the
objective already built), nonlinear conjugate gradients on the log-weights — where `C-0017`'s mandate is
exact by a softmax and positivity does not exist — and `C-0058`'s own optimiser as a polish stage, from
42 starts.
It reaches **0.1254**, 21.0 % better, and stops. At a *single* state it is **2.54× better** than
`C-0058` (0.0214 against 0.0544), so the optimiser was leaving a factor of two and a half on the table
even where `C-0058`'s verdict was safe.
And the search is measurably not the limit: 16 starts to 42 is worth `3.2e−5`, the whole homotopy 6.4 %,
and **two states are active at the optimum**, which is what an equalised minimax looks like.

**What decided the question was one row of `C-0022`'s own table that no downstream claim had used.**
Of the 31 non-empty subsets of the five, every one of the **14** that puts the 2 nm state together with a
10 nm state fails (0.1086–0.1254) and every one of the other **17 is flat** (0.0090–0.0799).
The free-tile dishing field of the 2 nm state has a cosine of **−0.943 to −1.000** against every other
state — exactly **−1.000** against the 10 mM one — while all six pairs among the other four run **+0.949
to +0.997**.
The reason is that the 2 nm state is the **only one of `C-0022`'s 21 solved states whose finite tile
carries *less* total force than a 1-D pressure over its footprint**, −3.91 % against +4.9 % to +19.2 %
everywhere else. Its edge effect is a *loss* where every other state's is a *gain*.
**A distribution that flattens an edge enhancement deepens an edge deficit: the obstruction is a sign,
not a magnitude, and no search removes it.**

**Then the requirement turned out to be mis-posed, which is the finding worth carrying forward.**
`C-0022`'s five headline states are the rest states of **three different buffers** at a 10 nm layer plus
the rest and *held* states of a **5 nm** layer — `C-0022`'s own row label for the 2 nm state is *"held at
3 nm stroke"*, so it belongs to the 5 nm device and is **no state of the 10 nm device at all**.
A device traverses one buffer, one layer and one bias, from `L₀` down to `L₀ − s`, and `C-0022` solved
**both ends** of the design device's stroke at its own 0.192 V.
Over the range each device actually traverses the minimax is **0.0373 / 0.0435 / 0.0620 / 0.0504** —
**all four inside `T-5b`'s 0.10**, both endpoints active at each optimum, and two interpolated
intermediate gaps move the first by exactly zero at nine significant digits.
The subset sweep had already found the same partition from the other side: the 2 nm state is compatible
with the 5 nm rest state, *its own device*, at 0.0620, and with nothing else.
This is [`CH-0077`](gpd/challenges/CH-0077-five-solved-states-are-four-devices.md), and its direction is
**favourable** — `C-0058`'s own two-level rim × 5 rule turns out to be flat over the whole stroke of
both 10 nm devices (0.0753 and 0.0683), which is more than its Deliverable 4 claims for it.

**A real failure is reported on buildability, and its escape is a methodological point.**
The 45-parameter robust optimum spans 7.95× and does **not** survive `C-0060`'s two levels: quantised
optimally onto two it measures 0.1002 — outside the convention by 0.2 % — at a level ratio of 2.475,
*below* `C-0060`'s measured 3.5–20 window, and it needs a genuine **third** level to be flat where
`C-0060` prices two.
What answers it is that **a constrained family must be searched in, not projected onto**: `C-0058`'s
existing two-level rule is 24.9 % better over the same range than the optimum quantised onto two levels.

**Three of this task's own predictions failed in code and are reported rather than repaired.**
The Plan expected the 10 mM state to be the antagonist, because its smooth collar is the one whose sign
reverses — it is not, its *net* edge effect is still an enhancement and its cosine with the design point
is +0.987. The Plan expected the subset dichotomy to be *"contains the 2 nm state"* — it is *"contains
the 2 nm state **and** a 10 nm state"*. And the Plan expected the robust design to be buildable on two
levels.
`CLAUDE.md`'s *"read the emitted prose, not just the JSON"* earned its keep twice in one iteration: the
first run died on a `%d` given a `String` (one placeholder more than arguments, in a concatenated format
string), and the second emitted two findings whose *sentences* were contradicted by the tables directly
above them.

**Timing note for the next agent**: `T-125` landed mid-run and changed the standing caveat.
`C-0063` resolves `CH-0074` the other way — a 34-root placement on the upward lattice is flat at 0.0706
with **equal springs** — but on a **different station set**, where `C-0058`'s rim rule *reverses sign*
(0.0706 uniform against 0.2214 at ×5). So no distribution in `C-0064` transfers to it, and the question
`C-0064` answers for the 3 × 15 grid is now open for `C-0063`'s placement, which reports its 0.0706 at
**one** state. That is `T-129`, and the instrument for it — a multi-state surrogate that prices `S`
states of one distribution on `n + S` load cases, plus the minimax — is exactly what this iteration
built.

## Iteration 11 — `T-127`: the trio exists, and the negative that closed the branch was worth a quarter of a trio

`C-0059` re-derived the truss branch's three junctions on `C-0057`'s torsion-feasible set,
found that two of the three survive and the trio does not,
and labelled its own trio result honestly as a *"not found within the budget"* rather than a refusal —
24 of 750 reach-feasible lattices, two candidate azimuths per junction, two crossbar lengths, one row pitch, 134 junction solves, 0 closing.
`C-0048` had already established that the cap is the junction the design cannot do without.
So the branch hung on whether that negative belonged to the geometry or to the search.

**It belonged to the search, and the cheap bound says so before any deep sweep runs.**
The three junctions share a crossbar lattice and nothing else — their placement problems are conditionally independent given it —
so the rate at which *one* junction closes somewhere on a lattice is measurable on its own.
It is **21.1 %, 20.0 % and 27.8 %**, which makes `C-0059`'s 24 solved lattices worth **0.28 trios** under independence.
**A budget worth a quarter of a hit returned zero hits.**
That is the whole finding, and it cost 8 seconds of arithmetic on a 90-lattice sample.

**The second cheap bound is what made the deep sweep affordable, and it is an exact pruning of the solver rather than of the problem.**
`C-0057`'s `O3′···C5′` reach interval is a proof of exclusion for *one* assignment of donor end, strand polarity and sugar pucker;
`bestLinkReach` throws that granularity away by reporting the best of 32.
Only **12 %** of assignments survive it, so solving only those is a measured **17.2×** speedup with **0 disagreements** against `bestLinkClosure(…).closes`.
Eight cores on top of that turns a 30-hour search into a 24-minute study.
**The cheap-bound-before-the-expensive-calculation rule applied to the instrument, not to the question.**

**Deepened, the trio closes at every one of the 21 admissible `(crossbar, row)` configurations** —
4–13 closing lattices per 750–904 reach-feasible, **151 of 11 834** and **167 of 11 874** in two 25 920-lattice depth runs,
**609** closing trios in total on **49 857 lattices solved** and **149 789 junction solves**: **1 039×** and **557×** `C-0059`'s budget.
Restricted back to that budget this task also finds nothing, and `C-0059`'s own class still returns 750 / 24 / 134 / 0 / 6.0° at departure 0.
**39 of the 44 published trios also close on `C-0057`'s 180-step verdict grid**, which is where the existence result is quoted.

**Two things were not anticipated.**
`C-0052`'s own 13 bp crossbar is the **worst** of the three lengths at its own 7 bp row — 6 closures at a 57° chord against 13 at 21° for 14 and 15 bp —
and lengthening it is mechanically free, because `C-0048`'s cap terms carry the row and not the crossbar.
And **the closing count is a density on a continuum, not a converged number**: refining the helical phase 45 → 90 → 180 steps takes it 2 → 6 → 18,
and at `C-0059`'s own azimuth resolution this task finds **0** as well.
The *existence* verdict is monotone under refinement and safe; the *count* is a sampling statistic and every alignment reported is an upper bound.

**And the mechanics does not move, which is a confirmation rather than a null result.**
Recomputed at one row pitch throughout — the row is the legs' separation and a leg has one of those, which `C-0059`'s table reads at 7 bp for the cap and 9 bp for the base (`CH-0075`) —
and at the alignment a *closing* trio delivers rather than the best *reach-feasible* one of a stage that failed,
the best representable design is the **10 bp row at both ends**, base 6.0° and cap **27.0°** on a 17 bp crossbar, at a 4.08 nm leg:
**2.45 on CanDo's rigidity and 1.84 on Fields et al.'s**, against `C-0059`'s own best of 2.45 / 1.84.
**A 4.5× worse cap floor for a third-decimal change in the margin.**
`C-0059` said the binding misalignment is the leg's own quantised twist and not the chemistry; that has now been tested against a floor 4.5× larger instead of assumed against an optimistic one, and it holds.
`C-0042`'s 7 bp and `C-0037`'s 8 bp rows stay `NOT REPRESENTABLE` at base misalignments of 69.0° and 57.0°.

**One test had to be weakened after the data contradicted it, and the weakening is the honest result.**
The first gate-4 test asserted that a closure found on the 60-step search grid must survive the 180-step verdict grid,
on the argument that a finer grid can only improve a lexicographic optimum.
It passed on a three-link sample and is **false**: the refinement is a *local* zoom, so neither grid is exhaustive and neither dominates —
5 of the 44 published trios disagree, in both directions.
The test now **measures** the disagreement instead of assuming it away, and the claim quotes 39 of 44 rather than 44 of 44.

**Process notes.** The study runs in **24 min** on six threads through `tools/study.sh`; `tools/verify.sh` was **BUILD SUCCESSFUL on the whole suite in 11–12 min with no `--drop-file` needed**, twice.
Sizing the search needed a calibration run before any code was written — one junction solve is 0.77 s at `C-0059`'s grid, and without the per-assignment pruning the sweep would have been a 30-hour job that nobody would have attempted.
**Measure the cost of the inner loop before choosing the budget of the outer one**; the whole difference between this iteration and `C-0059` is that arithmetic.

### Iteration 11 — a negative that was a budget, a placement that was on the wrong face, and a sign

Three loops, and two of them overturned an upstream result by asking what its number was a property *of*.

**`C-0062` reversed the truss verdict, and it did so on this project's own discipline.** `C-0059` had
labelled its trio result a *"not found within the budget"* rather than a refusal — 24 of 750 lattices, two
azimuths per junction — and said so in its own claim.
**The cheap bound settled it before any deep sweep**, which is the part worth keeping: the three junctions
are conditionally independent given the crossbar lattice and close on **21.1 % / 20.0 % / 27.8 %** of
lattices individually, so `C-0059`'s 24 solved lattices were worth **0.28 trios** under its own marginals —
**a null result was the expected one.**
Deepened to **49 857 lattices solved and 149 789 junction solves** (1 039× and 557× `C-0059`'s), the
crossbar carries **609 closing trios**, at **every one of the 21 admissible `(crossbar, row)`
configurations**. Restricted back to `C-0059`'s own budget this task also finds nothing, and `C-0059`'s
class reproduces at departure 0 — so the two are consistent and the entire difference is budget.
The budget was only affordable because `C-0057`'s reach bound is a **per-assignment proof of exclusion**:
12 % of the 32 assignments survive, a **17.2×** speedup with **0 disagreements**.
The mechanics does not move: the best representable design is the **10 bp row at both ends** at
**2.45 / 1.84**, against `C-0059`'s own best of 2.45 / 1.84. **The truss branch stays open at the cap.**

*(Corrected. The paragraph originally here — "all 5 940 lattices … 534 600 candidates … 22 275× the budget
… 93 closing trios … median survivor 17.0° … mechanics moves 0.5 %" — described the agent's **first**
report, not its filed claim, and none of those numbers appears in `C-0062` or its result file. The
coordinator quoted the report without re-reading the artifact, which is the **fourth** report/artifact
divergence this session and the first the coordinator failed to catch — having written the rule against it
two iterations earlier. The committed files were always the agent's; only the description was wrong. The
agent reported the divergence unprompted, which is the only reason it was caught.)*

**`C-0063` made the tile flat with equal springs, and corrected the observation that sent it looking.**
The best of **1 144 858** evaluated 34-root placements dishes **0.0706** of the free-tile stroke under
`C-0022`'s solved load — inside `T-5b`'s 0.10, **4.36× better than no coupling at all**, 5.9× better than
`C-0055`'s own placement, and better than `C-0058`'s **0.0753**, which needed a stiffness distribution on
stations no placement supplies. It needs **no distribution at all**.
Two cheap bounds did the work: the count vector is forced (`3a + 2(15−a) = 34`, exactly four rows of three),
and a placement can be centro-symmetric only where `2c ≡ 0 (mod 10.88 nm)` — **exactly 2 of 32 phases**,
both inside `C-0015`'s ten. All ten eight-column phases are flat and only one of the other twenty-two is:
**the fifth independent construction to land on `C-0015`'s ten.**
**`CH-0074` resolves** — the flat distribution's stations *are* supplied — and on the swept placement
`C-0058`'s rim rule **reverses sign**.
And its `CH-0076` is the correction: `C-0061`'s *"free, one line"* mirrored placement, which this
coordinator had passed along in the prompt as the cheapest unclaimed improvement in the programme, **is not
on the upward lattice at all** — at φ = 0 reflection maps a row's `EAST` sites exactly onto its `WEST` ones,
so 16 of its 34 arms would hang into the grafted layer, the azimuth `C-0055` had counted and refused.

**`C-0064` found the obstruction to a robust flat tile is a *sign*, not a search.** A smoothed minimax with
analytic gradients and 42 starts reaches a worst case of **0.1254** over `C-0022`'s five states, 21.0 %
better than `C-0058`'s and still 1.25× the tolerance — but more starts do not move it, and two states are
*active* at the optimum, the signature of an equalised minimax, so the search is not what limits it. Of the
31 non-empty subsets, **every one of the 14 containing both the 2 nm state and a 10 nm state fails
(0.1086–0.1254), and every one of the other 17 is flat (0.0090–0.0799)** — including everything-but-the-
2-nm-state at 0.0799, and the 2 nm state paired with its *own* device's rest state at 0.0620. The 2 nm
state's dishing field is **anti-parallel** to every other's (cosine −0.943 to −1.000, exactly −1.000 against
the 10 mM one, where all six other pairs run +0.949 to +0.997), because `C-0022`'s own table records it as
the **only** one of its 21 solved states whose finite tile carries *less* force than a 1-D pressure over its
footprint — an edge **deficit** where every other state has an enhancement.
Filed as `CH-0077`: **five solved states are four devices**, and asking one distribution to be flat across
them is asking it to be flat under a load and its own negative.
**And over the range each device actually traverses the minimax is 0.0373 / 0.0435 / 0.0620 / 0.0504 — all
four inside `T-5b`'s 0.10**, both endpoints active and the interpolated intermediate gaps worth exactly
zero. So the five-state negative is a *"not found at a large budget"* rather than a theorem, and the claim
says so; the declared cheap-bound falsifier did not fire.

*(This paragraph originally read `0.1247` / 21.4 % / 0.0797 / 0.0619 and omitted the traversed-range result.
Those were the figures in `C-0064`'s Verdict line when the coordinator read it; the agent revised the claim
afterwards. Corrected against the filed claim per `SESSION-PROMPT.md` step 9. No verdict changes — the
five-state answer is still outside the tolerance and the sign argument is unaffected.)*

**The session's recurring hazards both recurred and both were caught.** A second **task**-ID collision
(`T-127`, after `T-109`) was fixed before the agents were spawned. And every agent this iteration was asked
to report only what its filed claim says, after three report/artifact divergences earlier in the session;
none diverged.

## Iteration 12 — `T-131`: the primary deliverable had almost no bad numbers and three answered questions it still called unanswerable

**What was done.** `ANSWERS.md` — the file NDI reads first, and the one whose own header says *"a synthesis,
not a source. Every number here belongs to a claim"* — was reconciled against the claim corpus statement by
statement for the first time. Seven iterations had run since anything checked it end to end, and it had been
edited piecemeal by several agents and by the coordinator. Claim `C-0067`; **no challenge raised**.

**The method was two halves and the cheap one ran first.** A number that appears in **no** claim cannot be
traced by any amount of reading, so finding those bounds the problem before adjudication starts:
[`tools/trace-answers.py`](tools/trace-answers.py) strips claim, task, leaf, section and date identifiers,
folds the typography (`ANSWERS.md` uses en dashes and U+2212 where the claims sometimes use hyphens),
tokenises, and tests each token against all 60 claims and 69 challenges with a substring guard so `45` does
not match inside `1.45`. It has **22 executable checks** of its own, covering both failure directions — a
false ABSENT would send an agent to "correct" a sound number and a false CITED would let a drifted one
through, and both are silent.

**The arithmetic was almost perfect and that was the surprise.** Of **415** numeric tokens, **414** appear
somewhere in the corpus. The single exception is `42.4`, a rounding of `C-0032`'s `42.38`. A 517-line
synthesis assembled by many hands over eleven iterations had propagated essentially no bad numbers.

**What had drifted was the STATUS of answers.** Of 84 hand-adjudicated statements, 63 trace, **16 had
drifted**, 2 cannot be traced as stated, and 4 completeness gaps were closed. The worst kind has three
instances, and it is the one no reviewer looks for: entries in *"What we cannot answer, and why"* that the
programme had **answered** —

- *"Whether `C-0018`'s pull-in bias itself moves"* — answered by `C-0033` (`T-60`) in **iteration 5**;
- *"Which body carries the standoffs, and what sits under the flexure's midspan … a specification gap"* —
  answered by `C-0035` (`T-75`, `T-78`) in **iteration 5**, which found it was never a free binary;
- *"Whether a strain-softening coupling still satisfies the stability clause"* — answered by `C-0032`
  (`T-76`) and `C-0049` (`T-107`).

Each had stood for five to seven iterations. **A deliverable that under-claims is as wrong as one that
over-claims, and it is harder to catch, because the instinct is to check the assertions and not the
disclaimers.**

**The most consequential single correction runs the other way.** §1's *"a point-coupled lever and an
area-averaging charge sensor differ by 32 % of the stroke, and that part is **irreducible** — forced by the
tile's own electrostatic edge, which no coupling choice can remove"* is **false**. `C-0022`'s 32 % is what
survives a *perfectly distributed* coupling, and a perfectly distributed coupling is not the best one:
`C-0058` reaches **0.0753** of the stroke by *distributing* the same 33.3333 pN/nm, and `C-0063` reaches
**0.0706** with **equal** springs by *placing* 34 of them — both inside `T-5b`'s 0.10, against **0.3079**
with no coupling at all. What is irreducible is the **load**, not the dishing.

**Nine more corrections, each with its prior wording kept in the file.** *"Seven of eleven axes"* → `C-0051`'s
**ten of twelve**; *"`P2` closed by `C-0017`"* → closed for the affine mandate and **failing** for the
coupling the programme actually has; *"45 attachments are needed for flatness"* → `CH-0034`'s saturation
reading; *"`T-1f` is now the binding uncertainty"* → **`T-50`**, since `CH-0019` shows `T-1f` bounds the
*polymer* expansion and the 123–214 % is the *electrostatic* one; *"twenty-nine challenges"* → **69**;
*"two paywalled papers"* → **one**, Lee et al. having been obtained free from NIST; `C-0029`'s 90° routing
qualified by `C-0057`'s torsion census (the existence survives, **the routing does not**); the 40 pN/nm
ceiling marked withdrawn by `C-0049` with the verdict surviving on `C-0035`'s ground instead; and `C-0017`'s
`K2` marked as **not** the committed coupling, being one-sided and placing only 15 of its 45.

**Two statements could not be traced to any claim and are kept rather than deleted.** The `42.4` above, and
— more interesting — *"the peak per-load-path force is **3.9–8.9 pN**"*, which is a min/max over four cells
of `C-0016`'s own table that no claim states as a range. That is the shape of number a synthesis
manufactures without noticing: true, and unconfirmable by any `grep` of a claim file. Both now carry their
construction in the text.

**Five of the six specification questions the programme is waiting on were not asked in the deliverable at
all.** `C-0051` says the deliverable is *"a height plus five specification questions"* and named none of
them in `ANSWERS.md`; only the electrode (`P-13`) appeared. A table of six — `T-63`, `P-13`, `T-95`,
`T-102`, `T-112`, `T-115` — each stated as a **question with its threshold**, is now in §5, so a single
sentence from NDI settles any of them.

**The null is worth recording: no claim was found to be wrong.** Every one of the sixteen disagreements was
`ANSWERS.md` misreporting a claim or failing to carry a later one. That was not the expected outcome. One
near-miss is noted rather than challenged — `C-0027` states the 5 nm crossing as both *"widens to 24.80×"*
and *"`C-0016`'s 13.3× stands"*, in its body and its verdict row respectively; it moves no verdict, 5 nm
being empty on either reading.

**Left undone, and stated in the file rather than assumed away:** the window is still unsynthesised against
iterations 8–11 (`C-0052`–`C-0064`, `CH-0065`–`CH-0077`). Nothing in them is a function of `σ` on
inspection, which is why no edge is *expected* to move — but that is an expectation and not a re-run, and
`ANSWERS.md` now says so in its own §1.

**Two smaller things the pass turned up.** §3's row **(f)** — the longest answer in the deliverable — had
been written across six physical lines, and a Markdown table row must be one line, so it **was not
rendering as a table cell at all**. Joined; it is the one place this project's semantic-line-break
convention does not apply. And `tools/verify.sh` cannot supply a suite test **count**: its `trap cleanup
EXIT` deletes the snapshot and takes `build/test-results` with it, so the run is reported here as
`BUILD SUCCESSFUL in 11m, 0 failures` with one concurrent agent's mid-TDD test file dropped by
`--drop-file`, and the count is omitted rather than guessed from a previous claim's.

## Iteration 12 — `T-126`: the arms and the tie-downs do not compete for room, they compete for a registration

`T-126` asked whether `C-0055`'s arm slab clears `C-0035`'s tie-down path.
The task offered two acceptable outcomes — a plan-and-section answer,
or the statement that the only buildable mounting and the only workable arm array cannot share one face.
The answer is the first, and it is a **conditional** yes whose condition is not a size and not a count.

**The section is what decides, and it decides that the plan is decisive.**
`C-0035`'s tie has to reach the **tile**,
so its clear column runs from the tile's own top face at `z = +1.0 nm` up to whatever plane its standoffs stand on,
and the arm slab — 1.69 to 3.69 nm at rest, and 1.69 to **6.69** nm swept over §3's acceptable 3 nm stroke — lies strictly inside it.
A plan overlap is therefore level-independent:
no stacking, no re-ordering and no larger body can relieve it.
That is `C-0041`'s Fact A arriving in a new place,
and it is exactly why the plan **area** — 0.664 of the footprint — settles nothing.
The cheap bound was run in order to be refuted, and it was.

**On the plan every regular tie grid fails, and the failure is not marginal.**
At `C-0063`'s phase-24 placement, `C-0015`'s own 3 × 15 grid puts **30 of its 45 ties inside an arm**
at the arm senses `C-0063` published, and **26** at the best of every feasible assignment.
Two columns give 24 of 30 and one column 10 of 15.
Sweeping the whole grid rigidly through a column pitch at 400 001 offsets,
the two- and three-column grids have **zero** clearing windows;
the one-column grid has four, the nearest **6.785 nm off the tile centre-line** and the widest 0.99 nm wide.

**And yet the room is there.** The array leaves **108** places a 2.69 nm tie could stand against the 45 demanded,
and the poorest of the fifteen rows holds five.
So this is not a `T-102` result and the tile is not too small:
what refuses the ties is a **registration**, because the arms sit on a 10.88 nm crossover lattice
and every coupling grid in this programme is drawn regular.

**The surprise was that the escape is nearly free and still not worth taking.**
Displacing each tie to the feasible `x` nearest its own column — worst displacement 4.332 nm, mean 2.081 —
costs **1.7 %** of the dishing, 0.2182 → 0.2219.
The task was formulated expecting that to be the expensive part.
It is cheap because the displacement is entirely **along** the helices
where `C-0026`'s one-row-per-duplex registration is **across** them.
What kills the composition instead is that neither set is flat at all (`T-5b` asks 0.10),
while the design that *is* flat needs no tie grid whatever:
`C-0063`'s 34 roots dish 0.0706 with the coupling entering at the hinges.

**The one registration the slab supplies for free is the arms' own tips** —
34 ties landing on the arm ends clear every neighbouring arm by **2.7156 nm** against the 1.345 demanded,
and that number is the **root pitch minus the arm**, a lattice quantity with no fitted parameter in it.
The same 2.7156 nm gap, asked to hold a *free-standing* tie between two consecutive arms,
clears a 2.69 nm duplex by **0.0256 nm** and fails outright at the 2.73 nm square-lattice interhelical distance.
It is the one number in the claim a tolerance model could reverse, and no tolerance model exists.

**A second surprise: `C-0063`'s free variable is not free.**
Its own open item 4 offers the 34 arm senses as a free variable that does not enter the flatness.
They are **forced on 8 of the 15 rows** — a row rooted at `±16.32 nm` cannot point its arm off the tile —
and the whole exhaustive freedom over the remaining seven is worth 30 → 26 clashes of 45.

**And the sweep runs the favourable way.**
An arm rotates about its root, so its plan projection is `√(L² − s²)` and *shortens*;
the swept envelope is the rest footprint identically, at 8, 64 and 4096 samples,
so a static plan view is conservative at every stroke and the clearance is worst at zero.
The same arithmetic refuses §3's desired 10 nm outright — 10 nm exceeds the 8.164 nm arm —
which is `C-0050`'s kinematic ceiling reached from a plan view rather than a force balance.

Filed as [`C-0066`](gpd/claims/C-0066-arm-slab-tie-clearance.md),
raising [`CH-0079`](gpd/challenges/CH-0079-a-tie-grid-is-a-registration-and-an-armed-tile-has-none.md)
against `C-0035`'s nominal-design row *"what the tile carries: only the 45 tie attachments of `C-0015`'s 3 × 15 grid"* —
a **conditions** challenge and not an arithmetic one:
every number in `C-0035` reproduces here, including its 325.62 nm² aperture floor,
its 5.31 nm midspan clearance and its 4.69 nm penetration at the desired stroke,
recomputed through its own library as the strong free limiting case
(**zero arms must reproduce `C-0035`'s clearance ledger exactly**, and it does).

**Left undone, and named rather than assumed away.**
Nothing in §1 or §3 chooses between the two couplings this claim had to evaluate both readings of —
`C-0050`'s catalogue keeps `E5a1` and the linear `E5` alive together —
so *"what the composed device actually is"* is the first open item.
The four dishing numbers are at one of `C-0022`'s five states, the same exposure `T-129` owns.
And every clearance is at nominal positions: the 0.0256 nm margin is 0.075 of a base-pair rise.

**Suite status for this iteration.** `tools/verify.sh` **BUILD SUCCESSFUL in 11 m 5 s, 0 failures**,
the whole suite on its own isolated tree,
with one concurrent agent's mid-TDD file dropped by `--drop-file` — `src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt` (`T-130`),
which fails `compileTestKotlin` on unresolved `shouldBe`/`plusOrMinus` references and is somebody else's unfinished work.
`gpd/results/T-126-arm-slab-clearance.json` was re-run through `tools/study.sh` and diffed byte-for-byte identical on two independent runs —
after a `runtimeSeconds` field was removed from its parameters, which was the only thing that differed
and is the same trap `CLAUDE.md` records for `Double` reproducibility, in a form no rounding can fix.

## Iteration 12 — `T-130`: the trio repeats 34 times without touching anything, and what the array charges for is a *register*

`C-0062` reopened the truss branch by finding that three 90° junctions **can** close together on one crossbar —
609 closing trios, at every one of the 21 admissible `(crossbar, row)` configurations —
and named its own left-undone in one sentence:
*"a trio that closes in isolation may not place 34 times."*
It searched **one crossbar at a time**; a Gen-1 device needs **34**, at the upward roots `C-0055` counts and `C-0063` places.

**The answer is yes, and the plan view is not what the composition costs.**
All 44 of `C-0062`'s recorded trios place 34 times: 0 overlaps, 0 leg clashes, one level.
Three cheap bounds settled that before anything was solved —
34 truss blocks cover **0.385** of the footprint,
the widest crossbar in the band demands **9.49 nm** against the **10.88 nm** station pitch
(where `C-0053`'s 45-path arm demands 11.82 and does *not* clear it),
and — the one that decided the **shape** of the answer —
**every one of `C-0063`'s 34 stations is the same helical phase of its OWN host duplex**, 24 bp from that duplex's `NORTH` plane,
because adjacent rows' duplexes are phase-shifted by exactly the 16 bp their upward sites are offset by (`C-0055`'s `(k − 2b) mod 4`).
**So the register is one question, not thirty-four, and the placed count is quantised at 0 or 34.**
A cheap bound that fixes the *shape* of an answer rather than bracketing its value is the most useful kind this programme has found,
and it is the third time a lattice congruence has done that here after `C-0063`'s count vector and its centro-symmetry rule.

**What the composition actually finds is a register, and it moves a design point.**
A leg's *base* can only sit where the host's own backbone offers one:
**17 of 89** axial positions do, and **not one row pitch closes at the station itself**.
The array therefore translates **0.17 nm (9 bp) to 3.91 nm (6 bp)** —
and **no choice of the sheet's crossover phase can absorb it**, because the phase moves the station *and* the host's backbone together,
which is asserted as a gate test over six phases rather than argued.
Pinning that coordinate re-reads the base misalignment floor `C-0059` measured and `C-0062` composed its design table on.
**A floor is a minimum over a coordinate, and this is a coordinate an array does not get to choose:**
at the centre nearest the station the **10 bp** row — `C-0062`'s own recommended design — reads **57.0°** against its published **6.0°**,
**9.5×**, and past the **45°** at which `C-0037`'s `TwoLinkBase` invariant cannot represent a base at all.
The 6.0° exists, 2.55 nm away, at the price of the rim and of the flatness.
**The row an array can build is 9 bp at 18.0°**, and it keeps the tile flat:
**0.0780** of the free-tile stroke against `C-0063`'s **0.0706** at 34 nominal stations, inside `T-5b`'s 0.10.
Splitting one station into **two leg bases** costs 6.1 % of the dishing and the register a further 4.2 %; both were unknown, because `C-0063` placed points and a truss is not a point.
**17 of the 44 trios survive every clause at once** — place 34, close on `C-0057`'s own verdict grid, carry a representable base, leave the tile flat — at the 9, 11 and 12 bp rows.

**And the flexure is the other half, reported apart and never folded in.**
With `C-0030`'s 27.41 nm span at 34 paths the same array covers **1.84×** the footprint, needs **7 levels**, and places **12 of 34**.
That is `C-0041`'s standing negative re-measured at 34 paths on a real placement, it is independent of every trio,
and it is why the branch's remaining question is no longer the junction or the array but the **output stage** (`T-133`).

Filed as [`C-0065`](gpd/claims/C-0065-crossbar-array-placement.md),
raising [`CH-0078`](gpd/challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md)
against `C-0062`'s and `C-0059`'s design tables — a **conditions** challenge, not an arithmetic one:
every upstream number reproduces, worst departure `2.1e−4` against a value its own claim quotes to four digits,
and `C-0063`'s 34 stations are re-derived from `upwardRootLattice` at departure **0**.
Opens `T-132` (the leg's own length budget against the *pinned* base misalignment, at one global leg length for all 34 caps) and `T-133`.

**Two process notes.**
The first attempt at the *"which strand defines the station"* sensitivity moved the register field's **grid** to the shifted datum and reported *"no registered centre at all"* —
a statement about the grid, not about the convention, because the closing set lives on a continuum and a shifted grid resamples it.
Applied to the **offsets** instead, the same convention leaves all three centres and moves the nearest to −1.341 nm, and the verdict does not move.
And the prompt for this task described `C-0062` as finding *"93 of 5 940 reach-feasible lattices … best at a 3.00° chord"* —
numbers that appear **nowhere** in `C-0062`, its result file or `C-0059`, and that `JOURNAL.md` already records as retracted first-report text from iteration 11.
The filed claim says **196 of 17 388** in the band sweep and 21.0° / 9.0°.
It is re-checked here as a gate-5 reproduction so the record cannot drift a third time.

**Suite status for this iteration.** `tools/verify.sh` **BUILD SUCCESSFUL in 10 m 51 s, 0 failures** — the whole suite on its own isolated tree with **nothing dropped**.
A sibling's mid-TDD `src/test/kotlin/anchoring/ArmSlabClearanceTest.kt` (`T-126`) had to be dropped by `--drop-file` during development and compiles by the final run;
`T-126`'s own agent had to drop this task's test file in the same way, in the other direction, which is the concurrency working as intended.
`gpd/results/T-130-crossbar-array-placement.json` was re-run through `tools/study.sh` twice and diffed **byte-for-byte identical** both times.

**And the iteration's largest single cost was reproducibility, which is now a bounded, reported
limitation rather than a claim.**
Two runs of the first build, differing only in an *unused local variable*, disagreed in the sixth
significant digit of every searched quantity — 1 599 lines of the result file — with every input
identical to the last bit.
The arithmetic explains it: a search takes of order `1e6` comparisons, a last-ulp difference is `1e-15`
relative, so the chance one comparison straddles a **nine**-digit rounding boundary is ~`1e-6` each and
about one decision flips per run. **Nine significant digits is the right precision to emit at and the
wrong one to decide at**; deciding at **six** took the objectives from `1e-3` to `1e-6`.
It did **not** fix the 45-component distributions, which still moved by `8e-4` — because Polak-Ribière's
`beta` numerator `g·(g − g_prev)` cancels catastrophically once the iteration settles, turning an ulp
into an `O(1)` change of direction. Two standard restarts and a lattice snap on the iterate took the
diff from 1 599 lines to **28**: every verdict, every headline number, all four operating ranges, the
whole subset dichotomy and **all 90 emitted path stiffnesses** now agree, and what remains is two of
about forty optimisation runs differing in the sixth digit and two `%.1e` noise floors printed inside
prose.
**The file is not byte-for-byte identical and `C-0064` says so**, because the residual is structural:
a descent on an optimal **manifold** has no isolated answer to be reproducible about. That fact is not
a nuisance — it is Deliverable 4's evidence, and the reason a 45-parameter optimum's *quantisability*
turned out not to be a well-posed quantity at all.

### Iteration 12 — the composition holds, the deliverable had drifted, and the drift was in the disclaimers

Three loops. Every number below was grepped out of its claim before being written here, which is the step
`SESSION-PROMPT.md` gained this iteration after the coordinator failed to apply the rule `CLAUDE.md`
already carried.

**`C-0065` composed `C-0062`'s trios with `C-0063`'s placement, and the plan view is not what it costs.**
All **44 of 44** recorded closing trios place **34** times at phase 24 — 0 overlaps, 0 leg clashes, one
level. Three cheap bounds settled the plan before anything was solved, and the third fixed the *shape* of
the answer: **every one of `C-0063`'s 34 stations is the same helical phase of its own host duplex**,
because adjacent rows are phase-shifted by exactly the 16 bp their sites are offset by — so the register is
**one** question, not thirty-four, and the placed count is quantised at 0 or 34.
What the composition charges for is a **register**: a leg's base sits only where the host's backbone offers
one, **no row pitch closes at the station itself**, and no crossover phase can absorb the offset because the
phase moves the station and the backbone together. Pinning that coordinate re-reads the base floor
`C-0062`'s design table rests on — its own recommended **10 bp** row reads **57.0°** against a published
6.0°, past the 45° at which the two-link base cannot be represented at all. **The row an array can build is
9 bp at 18.0°**, and it keeps the tile flat at **0.0780** against `C-0063`'s 0.0706. **17 of the 44 trios
survive every clause at once.** The flexure is reported apart and remains the obstruction: 12 of 34.

**`C-0066` found the *section* decides the headroom, not the plan.** `C-0035`'s tie must reach the *tile*,
so its clear column strictly contains the arm slab — which makes every plan overlap **level-independent**
and the plan-area fraction irrelevant. That is `C-0041`'s lesson in a new place, and the area bound was run
in order to be refuted. The arms and the ties **can** share one face, but only if the ties land **on** the
arms; at `C-0063`'s placement `C-0015`'s 3 × 15 grid puts 30 of 45 ties inside an arm, and **8 of the 15
rows have no choice at all**, forced by the tile edge — `C-0063`'s "free variable" is not free. What gives
is the **registration**, not the tile size, the arm count or the mounting. And a knife edge worth keeping:
the free registration clears a **2.69 nm** duplex by **0.0256 nm** and *fails* at the 2.73 nm square-lattice
value, so which measured interhelical distance is used decides it.

**`C-0067` reconciled `ANSWERS.md` line by line, and the finding is the shape of the drift rather than its
size.** Of **415** numeric tokens, **414** trace to the claim corpus — the arithmetic was nearly perfect.
Of **84** statements adjudicated, **63** traced, **16** had drifted and **2** were untraceable (kept and
annotated, not deleted). What had drifted was the **status of answers**: three entries of *"What we cannot
answer, and why"* had been **answered** five to seven iterations earlier and left standing.
**A deliverable that under-claims is as wrong as one that over-claims, and is harder to catch, because a
reviewer checks the assertions and not the disclaimers.**
The most consequential correction is one this coordinator had propagated: §1's *"they differ by 32 % of the
stroke, and that part is irreducible — no coupling choice can remove it"* is **false**. That 32 % is what
survives a *uniform* coupling; `C-0063` reaches 0.0706 with **equal** springs by placement alone.
**Five of the six specification questions were not asked in the deliverable at all** and are now a table
with thresholds. And §3's row (f) — the longest answer in the file — was written across six physical lines
and **was not rendering as a table cell at all**.
No challenge was raised: every disagreement was `ANSWERS.md` misreporting the corpus, and no claim was
found to be wrong. That null was not the expected outcome and the claim reports it as such.

**The session's own failure mode was caught twice more, both times by agents.** `T-130` re-checked the
coordinator's retracted `C-0062` figures as a **gate-5 reproduction** and confirmed the filed numbers, and
`T-127` had reported the divergence unprompted in the first place. The fix is now a step of the loop rather
than a lesson in a file.

## Iteration 13 — `T-129`: the flat placement travels the whole stroke, and stops at the layer it was designed against

`C-0063` made the Gen-1 tile flat at **0.0706** of the free-tile stroke with **34 equal springs**,
on a placement `C-0055`'s upward lattice actually supplies,
and reported it at **one** of `C-0022`'s solved states.
`C-0064` had just established that a one-state flatness verdict need not travel —
the obstruction between states is a **sign**, not a magnitude —
and named this the largest open item it left.
`C-0065` and `C-0066` both carried the same exposure.

**The answer is yes for the device the programme places, and only for a 10 nm layer.**
Over the whole range `C-0018`'s placed 2 mM device traverses — gaps 10 → 7 nm at its own 0.192 V,
**both ends solved by `C-0022`** — the same 34 equal springs dish **0.0789**;
at `C-0032`'s 0.5 mM **0.0853**, at 10 mM **0.0896**.
All three are inside `T-5b`'s 0.10, so the single-state verdict travels.
**What the range costs is the margin**: 1.42× at the design state becomes **1.12×** at the tightest range,
and it is the *compressed* end of the stroke that spends it —
which is the sixth instance of this programme's standing discipline that a flatness number needs the state it is read at,
now read as *the range it is read over*.

**The exception is the 5 nm device, and it is the finding.**
Against §3's 5 nm layer the same 34 roots dish **0.2000** over that device's range —
and are **worse than no coupling at all at both** of its states
(0.1104 against a free 0.0638 at the rest state; 0.2000 against 0.1648 at the 2 nm held state),
which is the class `C-0063` itself convicted `C-0055`'s greedy placement for.
The mechanism is the one `C-0063` names and does not apply to itself:
**a placement is tuned to a load field**, and a 5 nm layer's free-tile dishing is 4.8× smaller,
so there is far less for the coupling to correct and its own sag at 34 discrete points dominates.
A 34-parameter distribution recovers it to **0.0565** at a peak ratio of **2.32** —
far inside what `C-0060` prices — so this is a **scope correction, not an infeasibility**, and it is `CH-0080`.

**The exclusion the task was warned to be suspicious of turned out not to matter.**
`C-0022`'s 2 nm state is not a state of any 10 nm device —
it demands **8 nm** of stroke against the **7.4235 nm** largest dead-load stroke `C-0050` finds anywhere at a 10 nm layer
(6.0135 nm at §3's own grafting density), and `C-0017`'s theorem says a coupling only reduces it.
But the device that *does* own that state was evaluated in its own right and is the one that fails,
so removing the state does not remove the problem —
which is the test a reader should apply to any convenient exclusion.
One honest caveat came out of the same arithmetic and is recorded rather than smoothed:
the dead-load test does not clear the **5 nm** device's own 3 nm either (1.5299 nm),
so `C-0022`'s 2 nm state is held by its solved 0.368 V bias and not by a 100 pN dead load.

**Three things were not expected.**
The placement is the **argmin of the range objective as well as of the single-state one**:
re-enumerating the centro-symmetric family exhaustively under a range objective —
361 584 placements at the two phases the congruence admits, each priced under two objectives —
found **0 of 198 288** at phase 24 better than `C-0063`'s own.
The single-state search cost nothing in range performance, which the single-state claim could not have known.
**And the phase is what the LAYER selects.**
Under the 5 nm device's range instead, *nothing* at phase 24 clears the convention (best 0.1169),
while a **phase-8** placement does with equal springs (**0.0895**) —
and that one reads 0.2416 at the 10 nm design state.
`C-0063` established that the eight-column phases are the flat ones under a 10 nm layer's load;
**which of `C-0015`'s ten a design should take is decided by the layer**, and that is a design variable no claim had carried.
And `C-0064`'s subset dichotomy transfers **in direction and not in exactness**:
12 of the 14 subsets mixing the 2 nm state with a 10 nm state fail here against 14 of 14 on the 3 × 15 grid,
because on these stations the antagonist is the **5 nm layer** rather than the 2 nm gap —
the two states of that one device are anti-parallel **to each other**, cosine −0.9427,
where every 10 nm device's own pair runs +0.9969 to +0.9998.
**A device whose own operating range is anti-parallel to itself is a new object in this programme**,
and it is not something a portfolio argument can dismiss.

## Iteration 13 — `T-132`: the array's leg length was free all along, and what pinning the base costs is an *overspend* nobody had a name for

`C-0065` left the leg-length composition as its open item 1 and said it *"can only tighten the verdict"*.
It does not tighten it at all — **17 of 44 before, 17 of 44 after** — and the reason turned out to be worth more than the tightening would have been.

**The array clause is free, and the cheap bound is the whole method.**
`C-0065`'s bound 3 found that every one of `C-0063`'s 34 stations is the same helical phase class of its **own** host duplex.
A leg base is a station displaced by the register's centre offset and then by ∓ half the row pitch **along that same duplex**,
so the same congruence carries: the **68 leg bases** fall into exactly **2 classes of 34**, presenting **1** distinct `(low leg, high leg)` pair at every one of the seven row pitches.
*"One leg length for 34 instances"* therefore collapses to *"one leg length for the two legs of one truss"*, before any junction is solved.
The falsifier — more than two classes, or unequal populations, either of which would have killed the shared-length design on arithmetic alone — did not fire.

**What pinning the base actually does is make it OVERSPEND the budget, not spend it.**
`C-0052`'s conservation is that a leg's rotation trades the two misalignments one for one, so `ψ_base + ψ_cap` *equals* the quantised budget `|m τ − 90°|` for every rotation in the reducing sense.
A pinned base has no rotation to choose and its sign need not be the reducing one, so the correct statement is an **inequality**:

    ψ_base + ψ_cap ≥ |m τ − 90°|,   with equality only when the pinned deviation opposes the budget's sense.

At the recommended design the two ends spend **81.13°** of a **45.13°** budget — **1.80×** — and the budget is honoured exactly at seven of the fifteen lengths and overspent at the other eight.
That is a genuinely new statement about `C-0052`'s identity and it is asserted as a gate-3 test at every length and 61 pinned deviations.

**And it changes nothing, because the leg's length was never the binding variable once the base is pinned.**
At the 9 bp row **all 15** lengths of `C-0052`'s 12–26 envelope are representable *and* pass all nine predicates,
at margins 1.815–2.443 on CanDo's rigidity and 1.364–1.836 on Fields et al.'s — a spread of only **1.35×**.
The base misalignment, which is what `C-0037`'s frame couple reads as `cos²ψ`, is **18.0° at every one of them**: the register fixes it and no length can move it.
The single best shared length is **12 steps, 4.08 nm**, at *every* surviving row — the same leg `C-0062` chose for the 10 bp design it recommended and the array cannot build.

**Three things were not expected.**
The pinned design is **better** than the free one at every representable row (2.443 against 2.410 at 9 bp, 2.380 against 2.354 at 11, 2.215 against 2.172 at 12),
which is `C-0052`'s balance finding in a new place: a worse cap chord moves couple out of the plane that does not govern and into the one that does, and the margin is the minimum of the two.
**The row the array can build carries 2.443 against the 2.446 of the row it cannot — 0.13 %.** `CH-0078` moved the recommendation and cost essentially nothing.
And **a truss has two legs, which no upstream design table can express**: the register pins them at two *different* azimuths (−9.0° and −18.0° at the 9 bp row, 0.0° and +57.0° at the 10 bp),
and because one length rotates both cap chords by the same `m τ`, their cap chords differ by that same folded angle at **every** length —
a floor `|fold(δ_A − δ_B)|/2` that no leg length can beat.
It is 4.5° where the design lives and **28.5°** at the 6 and 10 bp rows, above the 9.0° and 27.0° `C-0062` composes them at — [`CH-0082`](gpd/challenges/CH-0082-a-truss-has-two-legs-and-the-design-table-gives-it-one-base.md), not verdict-moving because both rows already fail another clause.

**One sensitivity moves the verdict and is reported as such.**
At a **0.5 nm lateral seat** the 9 bp row's register offers one admissible pair and its pinned base is past the half right angle, so *no* leg length is representable.
`C-0065` swept the same axis and found it moved the register without moving its *placement* verdict; the pinned *design* verdict is the more fragile one.
The seat is a free variable upstream (`C-0059` sweeps it), so this says which seat a registered array must choose — it does not say the design fails.
Also recorded: `C-0062`'s cap floor turns out not to bind at all — removing it entirely changes nothing, because the pinned geometry already demands 63.1° where the chemistry asks 24.0°.

**Process.** Two `CLAUDE.md` gotchas fired exactly as written: a private study record class collided across files in the same package (`PublishedTrio`), and a boolean read off a floating-point tie (`twoLegFloor > capFloor` at the 8 bp row, where the two are the same number) had to be given an absolute tolerance before it stopped reporting a tie as a finding.
A sibling's mid-TDD `anchoring/OutputElementPlacementTest.kt` needed `--drop-file` for every run.
`tools/verify.sh` **BUILD SUCCESSFUL in 12 m 10 s** on the whole suite with that one file dropped; the result file re-emitted through `tools/study.sh` and diffed **byte-for-byte identical**.

**And a determinism reading worth keeping.**
Two runs of the finished code differ in **28 lines of 1 423** — all of them inside the 31 subset minimaxes,
where a 34-parameter descent lands in a neighbouring basin of an equally optimal *manifold*, exactly as `C-0064` reported —
while the four **operating ranges**, all four exhaustive placement enumerations over 723 168 evaluations,
and every other section of the file are **byte-identical**, with no verdict flipped.
A placement sweep compares *distinct designs* at a rounded objective and is reproducible;
a continuous descent on a flat set is not, however carefully its decisions are rounded.
`tools/verify.sh`: **BUILD SUCCESSFUL in 12 m 19 s**, the whole suite, nothing dropped.

## Iteration 13 — `T-133`: the search for an element *out of* the plan returned an element *in* it, because the plan was never what refused the flexure

`C-0065` left the truss branch hanging on one sentence:
the trio array places 34 times as a standoff, and *"the element it caps does not"* —
`C-0030`'s flexure at 34 paths is 27.41 nm of span, 7 levels, **12 of 34**.
Every coupling element this programme has priced lies in the plan,
so the task was formulated to go looking out of it:
along the surface normal, folded, or nested.

**There is nothing out there, and nothing needed to be.**
The whole element space is closed form.
`C-0023` established that DNA's compliance comes in exactly two kinds,
so there are only five ways to obtain a normal-direction compliance at all,
and each has a length at a stated stiffness that costs one line:
`S/k` axial (**1122 nm**), `3k_BT/(kb)` entropic (6.04 nm, and **one-sided**),
`√(k_θ/k)` at a hinge (3.72 nm), `(c EI/k)^(1/3)` in bending, `GJ/(kr²)` in torsion (64.8 nm).
The only genuinely out-of-plane mechanism is the axial one —
a body along `z` loaded along `z` is loaded along its **own axis**, which is `C-0023`'s refused corner —
and it asks for **112×** `C-0017`'s entire 10 nm envelope.
To bend such a member instead, the load has to be transverse to it, which is the plan again.

**What refuses the flexure is its END CONDITION, and it is one cube root.**
A beam supported twice and loaded at its midspan has `c ∈ [48, 192]`;
one supported once and loaded at its far end has `c ∈ (0, 12]`;
the span is `c^(1/3)`.
So the same duplex, at the same rigidity and the same stiffness,
is 22.41–35.58 nm in the first topology and at most 14.12 nm in the second.
Against a plan budget of **8.19 nm** the first family is refused
**at every span, at every end joint and on every placement** — 2.74× at its own floor —
which is strictly stronger than a count of 12 on one placement.

**The budget itself is exact and it is not `C-0063`'s.**
`3a + 2(15 − a) = 34` forces four rows of three (`C-0063`'s own bound 1),
and three roots on a 10.88 nm lattice cap a rooted element at `pitch − d`, because
at most one of the two adjacent pairs can be made to diverge and every other pair is same-sense or converging.
So **8.19 nm holds for every 34-root placement on the upward lattice**, not just the one that was swept;
the bisected ceiling over `C-0063`'s own rows agrees to under `1e−9` nm.

**Two elements place 34 times at one level, and both lie in the plan.**
`C-0055`/`C-0063`'s own hinge-rooted arm at **8.16439 nm** —
34 of 34, one level, 0 overlaps, 0.463 of the footprint,
tangent minimum 30.03 pN/nm over `[0, 3]` clearing **6 of `C-0017`'s six** 2 mM floors, 2.941 pN per path, two-sided —
and a **standoff-headed crank** at **5.331 nm**, 1.53× shorter,
which pays for it with a second undemonstrated motif, a compression member (2.45× of Euler margin)
and two of the six stability floors.

**The finding that was not expected is that the whole budget is one inequality on `c`, and the design sits inside it by 2–3 %.**
`L ≤ 8.19 nm` is `c(ρ_n, ρ_f) ≤ 2.3416`.
Bisecting `C-0039`'s exact elastica on each end in turn:
at a one-crossover root the **tip** may be no stiffer than **79.68 pN·nm/rad**, and `C-0034`'s `A2` is **78.24**;
at an `A2` tip the **root** may be no stiffer than **13.93**, and one antiparallel crossover is **13.53**.
`C-0055` chose the root because it is the unused upward azimuth
and `C-0034` chose the tip because a duplex end has exactly two strand termini —
neither knew about this bound, and both land inside it with no margin.

**A second unexpected thing: the window has a FLOOR.**
A pinned tip on a one-crossover root asks for **3.591 nm**, and `C-0039`'s exact solver refuses any arm below 1.5× the stroke,
where the tip turns past 42° and the chord draw-in is a large fraction of the arm.
It is **short enough to place and too short to work** —
a refusal this branch had not met before, and the reason the rooted window is 4.50–8.19 nm rather than 0–8.19.

**The truss is the wrong root, and that is `CH-0081`.**
A rigid root with a free tip is `c = 3` in the small-rotation limit and 3.37 solved: **9.247 nm**, 12.9 % past the budget, 24 of 34.
The truss's whole virtue — a base that does not rotate — is what pushes `c` up,
and `c^(1/3)` takes the plan length with it.
So the array that places 34 times as a standoff cannot carry an output element at all:
what it would cap is either a two-support flexure (refused by 2.74×) or an arm on a rigid root (refused by 12.9 %).

**Three sensitivities close the 0.0256 nm margin, and none of them is exotic**:
the 2.73 nm square-lattice interhelical distance (18 of 34) — `C-0066`'s own flip in a new place —
the **top** of Chen et al.'s fitted `α` bracket, which is an experimental range and not a modelling choice (30 of 34),
and §3's own 45 paths (24 of 34).
That last one is worth stating plainly: **34 is the count the plan budget prefers**, not a rounding of 45,
because more paths make each element *longer* (`C-0023`'s `L ∝ n^(1/3)`).
It also means the branch now has two claims whose headline sits inside 0.03 nm on a model with no scatter in it at all,
which is `T-134`.

### Iteration 13 — the plan was never what refused the flexure

Three loops. Every number below was grepped out of its claim first, and the full suite was run to completion
before this was written — the two steps the coordinator skipped earlier in the session.

**`C-0069` answered its task by refuting the task's premise.** `T-133` asked for an output element that does
not lie in the plan, on the reasoning that in-plane spans are what fail to pack. **No such element exists,
and none is needed, because the plan was never what refused the flexure — its END CONDITION was.**
A bending element's plan length is `(c EI/k)^(1/3)` and `c` is fixed by *topology*: 48–192 supported twice
and loaded at midspan, ≤ 12 supported once and loaded at the end. Three closed-form bounds then settle the
space before any packer runs. The plan budget on **every** 34-root placement is `pitch − d = 8.19 nm`
exactly, because four rows of three are forced by `3a + 2(15−a) = 34`. The two-support family's **shortest
possible** member is **22.41 nm — 2.74× over** — so it is refused at every span, every end joint and every
placement, which is strictly stronger than `C-0065`'s 12 of 34. And the normal direction supplies only
**axial** stiffness, `S/k = 1122 nm`, 112× the whole envelope: **the only out-of-plane compliance DNA has is
axial.** Folding *shrinks* the budget rather than growing it.
Of an 11-row catalogue **3 place all 34 at one level and 2 survive every clause**, and the survivor is
`C-0055`/`C-0063`'s **own hinge-rooted arm** — 8.16439 nm, 34 of 34, one level, tangent minimum
**30.03 pN/nm** clearing **6 of `C-0017`'s six** 2 mM floors at 2.941 pN per path.
The budget reduces to `c ≤ 2.3416`, and **both of the arm's joints sit inside it by 1.8 % and 2.9 %** —
neither chosen for that reason, so the design has no margin on either. Its `CH-0081` is the sting for the
other branch: a truss standoff is a **root**, and a rigid root demands a *longer* arm than the plan admits,
so the array that places 34 times as a standoff cannot carry an output element at all.

**`C-0068` found that the layer selects the phase.** `C-0063`'s placement is flat over the range its own
device traverses **with equal springs** — 0.0789 / 0.0853 / 0.0896 at 2 mM, 0.5 mM and 10 mM, all inside
`T-5b`'s 0.10 against 0.0706 at the single state — so the one-state verdict travels and what the range costs
is the margin, 1.42× → 1.12×. But the **5 nm device fails**: equal springs dish 0.2000 and are worse than no
coupling at all at *both* its states, while a **phase-8** placement clears it at 0.0895 and reads 0.2416 at
the 10 nm design state. Re-enumerating the centro-symmetric family exhaustively under a *range* objective
found **0 of 198 288** better than `C-0063`'s own at phase 24 — it **is** the range argmin, not merely the
single-state one.

**`C-0070` found the shared-length clause costs nothing, by counting.** `C-0065`'s phase-class congruence
carries from the station to the **68 leg bases**, which fall into exactly **2 classes of 34** presenting
**one** distinct leg pair — so *"one leg length for 34 instances"* collapses to *"one leg length for the two
legs of one truss"*, and the declared falsifier did not fire. What pinning does is make the base
**overspend** `C-0052`'s budget rather than spend it exactly (81.13° of a 45.13° budget) while changing
nothing: at the 9 bp row all fifteen leg lengths are representable and pass all nine predicates.

**Two coordinator failures this iteration, both caught by agents rather than by me.**
`git add -A` swept a sibling's mid-TDD test into `HEAD` **without its implementation**, so the pushed commit
did not compile. The mistake is structural: this project mandates TDD, so a window in which a test exists
and its main source does not is *guaranteed* during a parallel iteration, and `git add -A` commits that
window by construction. `tools/verify.sh --committed` exists (`P-10`) precisely to catch it and I did not
run it. `T-132` reported it unprompted. Fixed, and recorded in `CLAUDE.md`.
And `C-0064`'s figures were revised after I read them, so the journal briefly carried two different numbers
for one quantity — mine and the agent's. Corrected against the filed claim, and the revision turned out to
contain the more important half: over the range each device actually traverses the minimax is
0.0373 / 0.0435 / 0.0620 / 0.0504, **all four inside the tolerance**.

## Iteration 14 — `T-135`: the programme can recommend, and what a recommendation costs is three thin margins that turn out to be one number

**Task.** `T-135` — *"which output element does the Gen-1 programme recommend, and on what premises?"*
Acceptance: a stated recommendation with its premises, its open items and its failure routes counted —
**or** the plain statement that the programme cannot yet recommend one, with what would decide it.
Filed as [`C-0071`](gpd/claims/C-0071-output-element-recommendation.md);
raises [`CH-0083`](gpd/challenges/CH-0083-the-pull-in-verdict-is-quoted-for-a-load-line-the-recommendation-does-not-use.md).

**The answer is YES, and the recommendation is the element the branch already had.**
`C-0069`'s `Q5` — `C-0055`/`C-0063`'s **hinge-rooted arm**, **8.16439 nm = 24.0 bp**,
rooted on one antiparallel crossover at the unused out-of-plane azimuth and tipped on `C-0034`'s `A2`
duplex end, **34 instances at one level**, at §3's **acceptable** clause.

**What made it decidable was a cheap bound with an integer in it, not a calculation.**
`C-0069` funnels an 11-row catalogue to **3 that place all 34 at one level** and **2 that survive every
clause**, and two survivors is not a recommendation. The survivors were ranked on three axes —
undemonstrated motifs required, `C-0017`'s six 2 mM stability floors cleared, compression members —
each of which is an **integer count a standing claim already carries**, so none of them is a new result.
**All three name `Q5`**: 1 against 2, 6 against 4, 0 against 1.
The declared falsifier was that they disagree, in which case `T-135`'s honest answer is *"cannot yet
recommend"*; **it did not fire**.

**The whole content of the claim is the price, and two parts of it were not visible from any single claim.**

**The three thin margins are one number.** The recommendation has **no margin at all** on three of fourteen
graded quantities — the plan length (**1.00314×**), the tip joint (**1.01844×**) and the root joint
(**1.02964×**) — in three different units, owned by three different claims. They are **one arithmetic**:
`pitch − d − L = 0.0256 nm`, and the two joint ceilings are the two coordinates of the *same*
`c ≤ 2.3417` inequality, so moving either joint moves the arm's length into or out of the same gap.
A **fourth** `NONE` — `C-0066`'s free-standing tie between two arms — is the **same** number again, and it
is **discharged**, because the registration `C-0066` itself recommends puts the tie on the arm's own `A2`
tip where the demand is half a duplex and the margin is **2.019×**.
So the design is *less* diversified in its exposure than the raw counts suggest, and **`T-134` has one
target and not two** — which is smaller than `T-134`'s own row says.

**Two of the six questions this programme carries to NDI stopped applying, and nobody noticed.**
`T-95` (may the superstructure be perforated under each flexure midspan?) and `T-102` (may the tile grow by
1.44×?) were both raised by the **flexure-and-tie** branch that `CH-0081` and `C-0069` removed from the
output role. The recommended element has no flexure, no midspan, no standoff base plane and needs no tie
grid; it places 34 instances at one level on the Gen-1 40 × 40 nm footprint. Both are therefore
**discharged for this element** — and both re-bind the moment the flexure branch or §3's 45 paths return.
`CLAUDE.md` already records that *"a window gains an axis when a constraint is discovered and loses one
when a constraint is DISCHARGED, and an intersection records neither"*; what this iteration adds is that
**a discharge is invisible to whoever files the removal**, because the claim that removes a branch is not
looking at the questions that branch raised.

**Three premises are UNDEMONSTRATED and the first is upstream of the element itself.**
`M1` — a **free lever** held to a single-layer sheet by **one** crossover: the site and a crossover on it
are published (Ke et al. 2009, read directly), the free lever was **not found in 62 recorded queries**.
`M2` — a duplex standing **normal** to a single-layer sheet: every published out-of-plane base is a **pin**.
It is a premise of the **decision** (it is the runner-up's motif) and not of the design.
`M3` — that `C-0009`'s **in-plane-fitted** crossover hinge constant transfers to the **out-of-plane**
azimuth. `C-0055` shows that site is in *better* helical register (4.286° against 8.571°) but **no
measurement covers its stiffness**, and at the top of Chen et al.'s own fitted bracket the arm is 8.332 nm
and places 30 of 34.

**Nine failure routes, and the classification is the finding.** Five remove the element, two remove a
premise, two change its uniqueness. Of the five removers, **two are already inside a published bracket** —
the square lattice's 2.73 nm (18 of 34) and `α = 1.2` (30 of 34) — and **one, the motif, can be settled
only at a bench**. A sensitivity whose alternative value is already in print is not a sensitivity; it is an
**unresolved reading**, and `C-0069` reports both as sensitivities without saying so.

**`CH-0083`: §6 task 4 is answered for two devices and the recommended one is not among them.**
`C-0018` computed the pull-in fold for the **affine** mandate and `C-0032` for `C-0030`'s **strain-softening**
flexure; the recommended arm is **strain-stiffening**, at 34 paths, with a tangent minimum of 30.03 pN/nm
where `C-0030`'s is 22.88. `C-0032` is itself the proof that a fold does not transfer — it lost **7 of 54**
states on one substitution of the law. The direction is favourable and the magnitude is unknown, and the
neighbouring case's bias margin is **1.0000–1.0019**. It is a calculation, not a measurement, and it is the
largest single thing the recommendation does not know.

**And `C-0032`'s escalation of 0.5 mM does not transfer either.** It was measured on `C-0030`'s softening
element (22.88 against a 23.41–27.91 floor); the recommended array clears **6 of 6** at §3's own 2 mM.
`T-63` still binds — but because the 2 mM **fold** is unknown, not because 2 mM is known to fail. That is a
change in the standing of the programme's highest-priority open question and the coordinator should carry it
into `ANSWERS.md`.

**Verification.** 26 gate-named tests; 23 upstream reproductions with a worst departure of **8.2e−5**, every
one of the largest being the published value's own quoted precision (`C-0069`'s 2.737 against 2.73678);
the elastica arm RK4-step independent at departure **0.0**; the result file re-run through `tools/study.sh`,
reported *"no result file changed"*, and diffed **byte-for-byte identical**.
Nothing is read from another study's result file — every number the recommendation writes is recomputed
from the library that owns it, which is what stops a synthesis being a transcription.

---

## Iteration 14 — `T-134`: the two knife edges were one number, and the measurement nobody could find was in a supplementary table all along

**Task.** [`T-134`](gpd/tasks/T-134.md), raised jointly by `C-0069`'s *Still open* item 3 and `C-0066`'s item 5,
with `T-45` standing behind both since iteration 3.
Filed as [`C-0072`](gpd/claims/C-0072-plan-tolerance-model.md), raising
[`CH-0084`](gpd/challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md).
Study `anchoring.PlanToleranceStudyKt` → `gpd/results/T-134-plan-tolerance.json`;
model `src/main/kotlin/anchoring/PlanTolerance.kt`; 31 gate-named tests;
literature survey and its 77 query strings in `gpd/data/T-134-tolerance-literature.md`.

**The first thing the task found, it found before writing any physics: the two knife edges are one quantity.**
`C-0069` reports a 0.0256 nm plan margin as *"the budget minus the arm"*, `(p − d) − L`.
`C-0066` reports a 0.0256 nm tie clearance as *"the tip gap minus a duplex"*, `(p − L) − d`.
They are the same subtraction, grouped differently, and the two claims were written by different tasks
against different questions without either noticing.
Asserted here at `4.4e−16` nm over four interhelical distances, two crossover spacings and three lengths.
**One scatter model settles both — which is what the task supposed and what nothing upstream had established.**

**And then the model was not needed, because four floors already exceed the margin.**
The base-pair **rise**, 13.28× — so the margin is below the finest length any DNA design can *specify*,
which is a stronger statement than "small": no correction can be applied to recover it even if the scatter
were known. The disagreement between the two published SAXS interhelical **means**, 1.56×. The **thermal
axial** breathing of the two segments the margin is a difference of, from the *measured* stretch modulus,
10.46×. And the arm tip's own bending at a **perfectly rigid** root, **70.6×** — the floor of the transverse
channel is the arm's own compliance, so no joint stiffening escapes it.
The declared falsifier — a channel landing *inside* 0.0256 nm, which would have made the margin quotable and
turned the task into a distribution fit — fired on exactly one channel, and it is the one that rests on
`C-0009`'s **constructed** in-plane crossover spring at the top of its own four-decade sweep.
**That is why the floors were required to rest on measured constants**: a floor a modelling choice can slide
under is not a floor.

**Correlation is worth exactly 7×, and the arithmetic is two integers.**
The rise enters the margin twice — the host's 32 bp crossover pitch and the element's own 24 bp length —
so a **common-mode** strain carries the *difference* (8 bp, threshold 1.10 %) and an **opposed** one the
*sum* (56 bp, 0.158 %), with the independent RMS between at 0.221 %.
`(N_p + N_a)/(N_p − N_a) = 56/8` exactly, and the study asserts it at 7.000000000000001.
The favourable structure is also the physical one — an arm and its host are the same molecule in the same
buffer — and **that is luck, not design**: nothing upstream knew the choice existed.
The sensitivity has an exact null at a 4:3 differential strain, and it is the *unusable* kind of null:
`C-0026`'s exact zero is delivered by a symmetry, this one by a strain nothing supplies.

**A quantity that does not propagate at all.** A crossover interface spacing is an *integer* base-pair count,
and 10.5 and 10.67 bp/turn both round to 32 — so the twist's coefficient on the plan margin is exactly zero
over the whole band this project disputes, asserted at 101 sampled twists to `1e−15`.
Second structurally zero coefficient in the programme after `C-0026`'s along-helix scatter, and zero for
the same *kind* of reason: an integer rather than a symmetry.

**The literature was formulated as a negative-existence exercise and returned four measurements.**
This is the part that changed the iteration.
- **Fischer et al. (2016)** — the paper this programme's 2.69 / 2.73 / 2.54 nm come from — **measures the
  width as well as the mean, and reports it only in the SI.** The single-layer sheet's fitted
  lattice-constant width is `w_a` = **2.5 Å on `a_mean` = 27.41 Å — 9.1 %**, which is **9.76×** the margin in
  absolute nm and **8.27×** this claim's *loosest* relative threshold, with a rigorously defined Lorentzian
  `B/q₀` = **15.2 %** beside it as an upper bound. The multilayer brick is **3.1× better ordered**.
  `w_a` is never defined in words anywhere in the SI — flagged **read directly, meaning inferred**, and
  carried beside the parameter whose definition *is* verbatim.
  **The main text quotes only the peak position with its fit uncertainty**, which is the standard error on
  the *mean*, ~20× smaller, and reading that as a tolerance would have been exactly the mistake to avoid.
- **Bai et al. (2012)** derive a **2–3 Å** atom-position rmsd at the core of a square-lattice origami from
  the cryo-EM B-factor after subtracting orientational and translational assignment error — the only
  Debye-Waller-type disorder measurement on an origami, and it is one paragraph of SI Text.
- **Dietz et al. (2009)** supply the only measured **thermal-versus-fabrication split** in the field:
  observed bend-angle s.d. 5.2–9.0° over N = 74–212 against a 2.5° thermal prediction, the excess attributed
  by the authors to defects. **2.1–3.6× in amplitude**, and nobody has repeated it in seventeen years.
- **Strauss et al. (2018)** map staple incorporation at single-staple resolution over all 168 staples of a
  Rothemund rectangle: **48–95 %, mean 84 %, worst at the edges.** That answers `T-45`.

**And the framing changes.** Bai et al. measure the interhelical distance as a *deterministic sawtooth* —
**18.5 Å at a crossover to 36 Å midway** — confirmed by all-atom MD (Yoo 2013) and by oxDNA **on a 2D tile**
(Snodin 2019), which also finds the *fluctuation* about the weave smaller than the weave itself.
So **2.69 nm is a Bragg lattice constant, not a local centre-to-centre distance**, and the plan model uses it
as a *steric* width. Read through the identity, the sawtooth's two ends give **+0.866 nm** and **−0.884 nm** of
plan margin: comfortable and impossible, and the plan model samples neither.
Its minimum is also **inside the 2.0 nm steric diameter `CLAUDE.md` records as the duplex's own surface**.
That is `T-137`.

**`CH-0084`, and it is the sharpest thing here: `T-45`'s tolerance had been measured and the flat design is on
the wrong side of it.** `C-0060` computed the threshold *"precisely so a measured or specified tolerance can
be substituted"* and reasoned about *"the 5 % a staple design might plausibly hold"* — an assumption with no
source. A missing staple does not perturb a load path, it **removes** it, so the population is Bernoulli and
`σ_rel = √(f/(1−f))`: the measured mean gives **43.6 %**, which is **1.26×** `C-0060`'s 34.6 % flatness
threshold and **2.57×** `C-0026`'s break-even; the edge sites give **104 %**.
And the mean alone, needing no pattern assumption at all, is a **16 % shortfall on `C-0017`'s mandate** —
2.9× the worst rounding placement error `C-0060` spends a one-base-pair trim to remove, and a trim cannot
recover it because the missing paths are not there to trim.
**The position dependence runs the wrong way for `C-0058`**, which puts 34 of its 45 stations on the rim and
gives them the *stiff* level — the sites the measurement says are least likely to form.
`C-0060`'s stiffness and buildability results are untouched and reproduce; what falls is *"the tolerance is
generous"*.

**Two results that were not anticipated.**
First, **the design that recovers the margin loses the flatness.** The whole 8.19 nm ceiling is bought by
**four arms**: `C-0063`'s bound 1 forces four rows of three, and a row of three is the only configuration in
which two same-sense arms sit at the bare root pitch. Dissolving them — 34 paths to 30 — takes the ceiling to
9.12 nm and the margin from 0.0256 to **1.3495 nm, 53×**, at 12 % of the path count and with every per-path
force still under the unzip allowable. And it dishes **0.2603** against `T-5b`'s 0.10.
The reduction used is a *plan* rule and not a flatness optimisation, so that is an **upper** bound — `T-136`.
Second, **`C-0070`'s lateral seat is not a tolerance axis at all.** The sweep was run expecting a threshold
and returned an alternating pattern: 4 of 11 coarse seats pass, the failures alternate, and refining to
0.025 nm finds *more* alternation. The seat is a **register**, so a scatter model on it is meaningless and
`C-0070`'s 0.5 nm reading is one unlucky choice among several lucky ones.

**One thing recorded rather than challenged.** `C-0017`'s mandate is a stiffness on a **sum**, so a path count
sizes the element *and* counts the instances; `C-0069`'s path-count sensitivity changes the first and holds
the second at 34, so its 15-path row places 34 instances of a 15-path arm and presents **2.27×** the mandate.
No `C-0069` headline moves — all are read at the self-consistent `n = 34` — but its note
*"the placement is unchanged because the count is what sets the stations"* is what pins the ceiling at
8.19 nm in every row of that table, and it is why `T-136`'s escape was invisible. Filed as `T-138`.

**Verification.** 31 gate-named tests. The strong free limiting case the task declared — zero scatter
reproducing both published clearances, *and reproducing them as the same number* — is a gate-2 test.
Gate 3 carries five things the construction does not impose: the identity at every lattice constant, the
exact null direction, the annihilation of the common-mode coefficient at equal counts, the superposition of
the channels, and the **nested** monotonicity of the station reduction.
Gate 4 reports the seat sweep as **not converging**, and that is the finding.
`tools/verify.sh` **BUILD SUCCESSFUL in 13 m 17 s — the whole suite, on its own isolated tree, with nothing
dropped**; the result file re-run through `tools/study.sh`, reported *"no result file changed"*, and diffed
**byte-for-byte identical**.

### Iteration 14 — the tree never rounded to nine digits; it rounded to nine digits six times

`P-18`, the process blocker `CH-0043` raised, taken ahead of every science task because it governs
what a result-file diff *means* — and therefore what every re-run certificate in this repository
certifies. Claim `C-0073`.

**The cheap bound fired before any solver ran, and it re-shaped the task.**
"The tree rounds to nine significant digits" is a description, not a design:
there are **six independent rounding implementations**,
and two of them do not even agree on the absolute floor (`1e-9` against `1e-12`).
So a per-provenance rule needs no new mechanism — the mechanism was already there, uncalibrated —
and a change to one site silently leaves the other five.
Worse, `window/` already writes the answer down and then does not use it:
it carries `WINDOW_DECISION_SIGNIFICANT_DIGITS = 6` because *"a constraint flag that flipped because a drainage corner moved in its twelfth digit would put a `true` in one run and a `false` in the next"*,
and then emits the numbers those flags were decided from at nine.
**A decision precision and an emission precision that differ mean a file's flags cannot be reproduced from the file's own numbers.**

**`CH-0043` ranked the two directions on cost, and the cost is not what separates them.**
Tightening `HEIGHT_TOLERANCE` from `1e-6` to `1e-9` costs **1.02×** the SCF solves — 282 against 288 at one design point —
because Illinois is superlinear and three decades are six extra solves.
The honest direction is essentially **free**, and every argument that rejected it on cost is void.
It is refused anyway, and the reason is **reachability**: the residual the height bracket works on is *discontinuous*.
`ScfDiscretisation` sets `M = round(h/Δz)`, and `round` is a step function of `h`,
so at every half-integer multiple of `Δz` the node count changes, the spacing changes with it,
and the computed pressure jumps — a measured `4.9e-4` to `9.8e-4`,
which over a logarithmic slope of −6.2 to −9.9 is **`7.9e-5` to `9.9e-5` of indeterminacy in the root**.
Tightening to `1e-9` would buy digits seven, eight and nine of a number whose **fourth** digit is already a discretisation artefact,
and would do it by moving every SCF-derived number in the repository —
exactly the hand re-adjudication `CH-0043` was raised to stop paying for.

**The general statement is the claim, and it inverts the intuition.**
Set a result file's rounding by the largest movement a change of **path** can produce,
never by the largest error the **model** carries.
Here those are `5.6e-7` and `9.9e-5`, two decades apart and in the counter-intuitive order:
the model is *less accurate* than the solver is *reproducible*.
Rounding to the model's accuracy would blind the diff to a real change of the model;
rounding to the solver's reproducibility makes the diff sensitive to exactly that and to nothing else.
The accuracy figure is not discarded — it belongs in a validity range,
beside `CLAUDE.md`'s bigger brother, the 23.4 % the 10 nm stroke edge moves between two meshes.

**Four probes, and the one that mattered is not the one the challenge implied.**
`P-15` did not change a tolerance; it changed the *path* to a root at a **fixed** tolerance.
Reproduced directly — one residual, one tolerance, four starting brackets — the roots spread by `5.64e-7`,
six determined digits, consistent with `C-0031`'s measured median of `9.0e-7`.
Changing the *warm field and cache* instead moves the resting height by `3.3e-13`:
the SCF field solver converges to `1e-11` in `w` and is **not** what limits determinacy.
And the amplification from a height into a second difference of free energies is real but state-dependent —
**27.6×** at the 10 nm design point against `C-0031`'s `10⁴` at the sweep's stiff end.

**The study's first run emitted its own measurement as `0.0`.**
`RESULT_ABSOLUTE_FLOOR` is documented as a magnitude **in the locked units** —
*"the smallest force of any interest here is `1e-3 pN`"* — and a *relative movement* is not in locked units at all.
A measured `3.3e-13`, and the declared SCF field tolerance `1e-11` sitting in the study's own parameter block,
were both flattened to zero by a floor written for forces.
The same shape as `C-0031`'s floored `layerStiffness` beside an unfloored `√(k_BT/k)`:
**a floor stated in one dimension silently reaches quantities of another.**

**The re-emission: no verdict, no flag, no string and no structural change.**
`T-1f` moves 714 numeric fields at a median `6.4e-7` and a maximum `4.7e-6` —
which *is* the arithmetic maximum of a six-digit rounding, so the file is exactly digits removed and nothing else.
`T-1d` moves 18 007 at a median `8.2e-7`; the 1 817 that exceed `1e-5` are **every one** of them
`stiffnessAtNineTenths` (911) or `stiffnessAtSevenTenths` (906), the two keys the per-key three-digit override applies to.
`C-0016`'s window edges move in the seventh figure against edges quoted to four.
Nine of 18 007 fields differ from a naive re-rounding by exactly one unit in the last place:
the study rounds the **raw double** and the check rounds its **nine-digit print**, and at a tie those disagree —
**rounding a rounded number is not rounding the original.**
The falsifier for the by-construction argument — that a study whose emission call was not edited cannot emit a different byte —
was four untouched studies re-emitted across three rounding sites, and it did not fire.

**What a per-key rule can and cannot do.**
`T-1d`'s two deep-compression stiffnesses are the only keys in the file determined to fewer than six digits —
`C-0031` measured 122 of 10 796 fields moving by more than `1e-3` and every one of them was one of those two.
Three digits absorbs 98.9 % of the file and does **not** absorb the `1.5e-2` tail, and no per-key constant can,
because the amplification varies by four orders of magnitude *across the sweep* while a key is one number.
**A per-key rule is the right granularity for provenance and the wrong granularity for amplification**;
closing the second needs a precision carried per record, which is a larger change than `P-18`.

`window/`, `actuator/` and `coupling/` remain over-printed by 3, 5 and 4 digits against their own declared tolerances,
and are queued as `P-19` with the measurement attached —
the same discipline `C-0031` used for `P-17`: rank rather than fix,
because changing code that produces published results costs a re-run of everything downstream,
and several of those files were filed hours earlier by other agents.

**And the declared falsifier fired, usefully.**
The by-construction argument was that a study whose emission call was not edited cannot emit a different byte.
It is **false for any study that consumes another study's result file**:
`window/DesignWindowStudy.kt` reads `gpd/results/T-1d-scf-density-profile.json`
and builds its 61-point grafting-density grid out of it,
so rounding the producer down moved **4 864 fields of `T-2`** through an emitter nobody touched.
The propagation was then checked to **close** — `T-1d` has exactly one reader, `T-1f` and `T-2` have none —
so the affected set is exactly `{T-1d, T-1f, T-2}`.
`T-2` moves by a median `8.4e-7` and a maximum `8.7e-6`, **0 flags, 0 structural changes**,
its window edges the same grid *indices* with only the grid values re-quantised.
The one visible consequence is a **tie**: the 10 nm upper edge is `0.260150`,
which is exactly a knife edge at four significant figures,
so a `1.5e-6` movement flips the file's own findings string from *"0.2601"* to *"0.2602"* —
`CH-0085`, upheld, with **no number of `C-0011` or `C-0016` failing**:
both claims also quote the edge at five and six figures, `0.26015` and `0.260150`, which is exactly what the file now carries.
**A tie is not a rounding artefact; it is a statement that the quoted precision is the wrong one.**
One more hole found on the way: `T-2`'s `graftingDensityGridRatio` is a `Double.toString()` in a
**string** field, which `roundedForResult` passes through untouched — ten significant digits in a file that declares nine.
A serialisation-boundary rounding is only as complete as the type it dispatches on.

**And the suite found the same mistake one level out.**
Five gates failed on the first full run after the change, and every one of them
**recomputes an identity from numbers read out of a result file** —
`Σ = π R₀² σ` from the file's own `R₀` and `σ`, `s = σ^(-1/2)`, the grid ratios, the published edges.
They were asserted at `1e-7`–`1e-6`, and they passed **only because the file printed three digits past what it determined**.
A six-digit field carries `5e-6` relative and an identity over three of them `2e-5`.
**An assertion tighter than a file's emission precision is not a stronger test; it is a test of the printed digits** —
`CH-0043`'s finding applied to the gates that read the file rather than to the file.
Relaxed to one documented `EMITTED_FIELD_SLACK = 5e-5` derived from `SOLVED_HEIGHT_SIGNIFICANT_DIGITS`.

**A coordinator note, and it is urgent.**
`P-18`'s work was swept into commits `1177073` and `ae3837e` by a `git add -A` while the iteration was still running —
the failure mode `CLAUDE.md` already records from iteration 13, in a new place.
`HEAD` therefore carries the six-digit `T-1d`, `T-1f` and `T-2` files **without** the two test-file
tolerance relaxations they require, so `tools/verify.sh --committed` fails five tests on `HEAD`.
The fix is in the working tree (`src/test/kotlin/window/UpstreamTransferTest.kt`,
`src/test/kotlin/window/ResynthesisTransferTest.kt`) and the working tree passes the whole suite,
1956 tests, `BUILD SUCCESSFUL` in 13 m 15 s.

## 2026-08-15 — Iteration 14

Three loops: the programme's first stated output-element recommendation, the tolerance model that qualifies
it, and the last process blocker. Every number below was grepped out of its claim, and `HEAD` was verified
with `tools/verify.sh --committed` before this was written.

**`C-0071` recommends, and the decision needed no new calculation.** Eleven catalogued elements → **3 place**
all 34 at one level → **2 survive every clause**, and the three tie-break axes — undemonstrated motifs,
`C-0017`'s 2 mM floors cleared, compression members — are each an integer count a standing claim already
carried, and all three agree (1 v 2, 6 v 4, 0 v 1). The declared falsifier, that they would disagree, did
not fire. The recommendation is `C-0069`'s `Q5`: a **hinge-rooted arm of 8.16439 nm = 24.0 bp**, one
antiparallel crossover at the unused out-of-plane azimuth, **34 instances at one level**, 2.941 pN per path,
flat at 0.0706 and across its device's whole range **with equal springs and no tie grid at all**, at §3's
**acceptable** clause.
Its price is stated with it: **20 premises of which 3 are UNDEMONSTRATED** — the free lever on one crossover,
the normal-standing duplex, and that an **in-plane-fitted** `k_θ` transfers to the **out-of-plane** azimuth,
which no measurement covers — **no margin at all on 3 of 14 graded quantities**, 4 specification questions
still binding, and **9 failure routes of which 5 remove the element**.
Two findings no single claim held: the three unmargined quantities are **one arithmetic**, `pitch − d − L`;
and **two of the six questions carried to NDI had stopped applying and nobody noticed** — `T-95` and `T-102`
were raised by the branch `CH-0081` removed from the output role. *A discharge is invisible to a list that
only ever grows*, which is the same failure `C-0067` found in the "cannot answer" section one iteration
earlier.

**`C-0072` then declined to clear it, and the reason is not a fabrication tolerance.** The branch's two knife
edges are **one lattice quantity**: `C-0069` groups it `(p − d) − L` and `C-0066` groups it `(p − L) − d`,
both reproduce at 0.02560917 nm and agree to `1e−12`, and neither claim had noticed — so `T-134` had one
target rather than two, exactly as `C-0071` predicted.
**Four floors exceed it and none needs a fabrication measurement**: the base-pair **rise** (13.28×, so *the
margin is below the finest length any DNA design can specify*), the disagreement between the two measured
SAXS interhelical distances of the same material (1.56×), **thermal axial** breathing (10.46×), and the arm
tip's own bending **at a perfectly rigid root** (70.6×, so no joint stiffening escapes). The declared
falsifier fired on exactly one channel — a *constructed* in-plane spring at the top of its own sweep — which
is why the floors were required to rest on measured constants.
**The literature was expected to be a negative and returned four measurements**, three of them in
supplementary material their own main texts never discuss: the single-layer sheet's lattice-constant width
at **9.1 %, 9.76× the margin**; an interhelical distance that is a deterministic **sawtooth**, so 2.69 nm is
a *Bragg lattice constant* rather than a spacing; and staple incorporation at 48–95 %.
**`T-45`, open since iteration 3, is answered from published measurement — and the answer is a failure.**
The escape is a reduced path count (53× of margin) and it **loses the flatness**, 0.0706 → 0.2603:
*margin and flatness are bought from the same four arms.*

**`C-0073` closed the last process blocker and refuted the framing it was given.** `CH-0043` had ranked
carrying `HEIGHT_TOLERANCE` **up** as the honest-but-expensive direction; measured, it costs **1.02×** the
solves (282 → 288, Illinois being superlinear), so "costs compute" was simply wrong. It was rejected on a
better ground: `ScfDiscretisation` rounds the node count, so the residual is **discontinuous** and the ninth
digit sits two decades below the fourth the grid already destroys.
Its cheap bound found the structural thing: **there is no tree-wide nine — there are six independent
rounding implementations**, two disagreeing on the absolute floor, and `window/` already decides flags at six
while printing nine. And its falsifier fired usefully: **a result file is an INPUT** — `DesignWindowStudy`
builds its grid out of `T-1d`'s file, so **4 864** fields of `T-2` moved through an emitter nobody touched.
No verdict changed anywhere; the one visible consequence is a four-figure tie, `0.2601` against `0.2602`.

**And the coordinator repeated iteration 13's failure exactly.** `git add -A` swept `P-18`'s in-flight work
into two commits, so `HEAD` carried re-emitted six-digit result files **without** the test tolerances they
require and five gates failed on the commit. **Recording the lesson in `CLAUDE.md` after the first
occurrence did not prevent the second.** The practice is now changed rather than documented: explicit paths
while any agent is in flight, and `verify.sh --committed` before the iteration is called closed — which is
what `P-10` built that flag for. Both occurrences were reported by the agent whose work was captured,
unprompted, and neither was caught by the coordinator.

## 2026-08-17 — Iteration 15

Three science loops in parallel plus one process task taken in the coordinator's own context.
Every number below was grepped out of its claim file, and two of the four figures a subagent
reported in chat did not survive that grep — see the note at the end.

**`C-0074` (`T-136`) answers `C-0072`'s escape and the answer costs the thing that made `C-0063`
remarkable.** `C-0072` had found the branch's whole 0.0256 nm plan margin bought by **four arms**,
and its escape — 30 roots instead of 34 — was a **plan rule** rather than a flatness optimisation,
so its 0.2603 was an upper bound. Re-run properly: at 30 roots *"two per row"* is an **identity**
(`2 × 15 = 30`), so the count vector is forced and the centro-symmetric family is exhaustible —
34 992 candidates at each of the two phases the congruence admits, plus a 12-start descent over
the non-symmetric family at **all 32 phases**. **With equal springs nothing clears `T-5b`'s 0.10**:
0.166653 at phase 24, 0.172575 at phase 8, 0.1670 over the descent. Every phase beats `C-0072`'s
0.2603 and none reaches the convention. **But the negative belongs to the equal springs, not to the
station set** — the least-squares floor over every phase-24 root is **0.00071**, 140× below the
convention. The design that answers all three predicates at once is at **phase 8, not `C-0063`'s
phase 24**: the placement keeping the lattice's maximum plan ceiling, **9.5350 nm**, i.e. a margin
of **1.76451 nm** — **68.9×** `C-0069`'s knife edge, **5.19 base-pair rises**, and **1.31×**
`C-0072`'s own reduction — dishes **0.06822** over the whole range the placed 2 mM device traverses,
at a peak stiffness ratio of only **2.057** and 6.857 pN per path against the 10 pN unzip allowable.
At phase 24 the same construction reaches only 0.11239 and 0.13188 *even under a distribution*.
**Two exact bounds did most of the work and neither needs a solve** — the forced count vector, and a
bisection on a monotone capacity for the largest element any 30-root placement can keep, because a
placement's ceiling is a `min` over independent rows. That second bound is `CH-0086`: **a plan
ceiling is a property of a placement, not of a count**, and `C-0072` read its ceiling column on its
own plan-rule reduction — understating its own escape by 1.31× at 30 roots, **3.62×** at 22 and
1.78× at 15, all in the favourable direction.

**`C-0075` (`T-138`) confirms the presentation defect and finds it worth more as a missing column
than as a corrected row.** `C-0017`'s mandate is a stiffness on a **sum**, so a path count sizes the
element *and* counts the instances; `C-0069`'s Deliverable 5 changed the first and held the second
at 34. Read as a delivered total its eight rows present 1.000, 0.529, 1.000, 1.000, 1.000, 0.882,
**0.533** and **2.267** × the mandate. **The part neither claim saw is that the ratio is `placed/n`,
not `34/n`** — so **three rows `C-0069` reports only as plan failures are stiffness failures by the
same arithmetic**, the 2.73 nm square-lattice row delivering 0.529× on its 18 of 34 (`CH-0087`).
**No `C-0069` headline moves, and that is verified rather than assumed**: at `n = 34` the two
readings return the same arm to nine digits, the same placed count and a ratio of 1.000000000.
**What the fixed array hid is the CEILING.** Holding 34 instances forces a row of three (`a = n−30`)
and a row of three caps a rooted element at `pitch − d` = **8.19 nm** — so `C-0069`'s ceiling column
is 8.19 in *every* row, including the 15-path one where the lattice affords **30.88**.
Self-consistently the ceiling is a **step function with its step at exactly 31**, and that step is
what `T-136` walks through. The two tasks were given to one agent because they are one question, and
that is what the coupling produced.

**`C-0076` (`T-137`) settles a convention five claims inherit, and the coefficient is exactly zero
for two independent reasons.** The queue expected a position-dependent exclusion width; the answer is
that **a single width is defensible** and that the question was on the wrong axis. *Categorically*:
`C-0072`'s `M = p − d − L` charges `d` **along** the helices between two bodies **not bonded to each
other**, while Bai's and Snodin's weave is a separation **across** them between two duplexes
**covalently linked at its own minimum**. *Numerically, on the axis where the weave does live*:
`C-0055`'s upward roots are the planes `k ≡ 2b+3 (mod 4)`, **odd for every duplex**, and the weave's
extrema are the crossover planes, **even** — so on a triangular wave **all 34 of `C-0063`'s stations
sit on a NODE**, at all 32 phases, worst departure `4.4e−16 nm`, **independently of amplitude**.
That annihilates the whole disputed **1.2–1.75 nm** amplitude bracket, and extends `C-0065`'s
*"one helical phase class, so the count is quantised at 0 or 34"* to the weave. **The 1.85-versus-2.0
contradiction dissolves on a measurement this repository already owns**: `T-71`'s 13 084
crystallographic linkages give a phosphate radius of **0.908638 nm**, so backbone contact is
**1.817276 nm** and Bai's 18.5 Å clears it by **0.0327 nm, 0.35 σ** — no interdigitation, and the
stronger reading is that **a crossover pulls its two duplexes together until their backbones touch**,
all-atom MD's 18.0 Å sitting 0.18 σ the other side. **What the measurement reopens is the width's
VALUE, and that is where the verdict now lives**: the placement threshold is `pitch − arm =
2.715609 nm` and the defensible readings **straddle it** — 34 of 34 at the measured contact, at 2.0
and at the SAXS 2.69; **22** at 2.725, 2.73 and oxDNA's 3.25. At the measured girth `Q5`'s margin is
**0.898333 nm, 35× the published 0.0256** (`CH-0089`), which clears three of `C-0072`'s four floors —
so the branch's knife edge is a property of an **unmeasured convention**, not of the lattice. Hence
`T-139`: one Poisson-Boltzmann pair solve for two *unbonded* duplexes, and no plan claim in the
branch is quotable until it is answered.

**`C-0077` (`T-1e`) discharges the last item of `CH-0010` by a root find rather than a scaling, and
the gap needed three factors.** The height convention is worth **2.819×** at the design point, the
physics residue — the conformational normal stress neither trial function contains — **1.636–1.648×**,
and **the trial function carries a convention factor of its own**, 1 exactly for the box and
**1.27616** (a Beta constant) for strong stretching. That third factor is why `CH-0010`'s comparison
looked like one convention: **it compared two models each read in its own.** Its *"most of the gap is
the convention"* is **upheld and quantified at 67.5 % on a logarithmic scale**. **The declared
falsifier did not fire and it was the one expected to**: the exact inversion gives `N = 175.08`,
**7.713 kDa**, *outside* `C-0011`'s 190–210, because that estimate used the exponent of the
**force-onset height** where it needed the exponent of the **first moment** — a consistent 10 %,
quoted at ±5 % where its own 0.49–0.64 band supports ±14 % (`CH-0090`). And the convention question
turns out not to be a modelling question at all: §3 specifies a distance between two *bodies*, and a
`2⟨z⟩ = 10 nm` layer puts the tile at 13.20–18.05 nm, inside §3's 5–10 nm band at **0 of 61** grid
points (`CH-0091`) — so `C-0016`'s window is already in the right convention and **no edge, owner,
stroke, stiffness or coupling verdict moves**. A fifth specification question for NDI was available
and deliberately **not** raised, because §3's own effort-point row answers it.
Two by-products worth more than they cost: **the two trial functions agree to 0.76 % on the first
moment and disagree by 28 % in their own conventions**, so most of what `C-0003` called *"profile
uncertainty … usable as an error bar"* is a Beta-function constant; and **the first moment does not
inherit the pressure's convergence order** — 1.59, and the chain inverted on it 1.11, against
2.08–2.32 for the disjoining pressure on the same solves (`T-146`).

**`C-0078` (`P-20`) — the coordinator's own loop, and it is `C-0067` recurring on `C-0067`'s axis.**
`C-0067` found that the drift in `ANSWERS.md` is in the **status** of answers rather than the value
of numbers — three *"cannot answer"* entries the programme had answered, one standing seven
iterations — and then retained a tracer that checks **numbers**. The half named as *worst* was left
to memory, and two iterations later it was back: of the three places the deliverable asserts a task
is open, **`T-129` was closed by `C-0068` in iteration 13**, and closed with a substantive answer the
deliverable was therefore not carrying (`C-0063`'s placement *is* flat over a range with equal
springs — 0.0789 / 0.0853 / 0.0896 — the exception being the 5 nm device at 0.2000). Extended with
`queue_status` / `open_assertions` / `stale_statuses`, which read `TASKS.md`, and which run
**unconditionally**, because a check nobody remembers to ask for is not a check. **22 self-tests →
42**, five of them pinning traps the real files produced: *"Left undone"* contains `DONE`, prose
*"cannot be answered"* uppercases to `ANSWERED`, and *"`T-45`, open since iteration 3, is answered"*
is history rather than an assertion. **The numeric half found the rest — 5 ABSENT of 590, and three
were one headline list**: `C-0064`'s four device-range minimaxes printed
**0.0372 / 0.0436 / 0.0619 / 0.0500** against a claim that says **0.0373 / 0.0435 / 0.0620 / 0.0504**.
The first is not a transcription error — `C-0064` records its optimum as a **manifold** and 0.0372 as
the other member — so the synthesis was quoting a *run* rather than the *claim*, and it survived
because all four are inside 0.10 either way. Now **0 ABSENT of 605 and 0 stale of 2**. **No claim was
contradicted**, which is the second time in three iterations that a reconciliation of the primary
deliverable has found no claim wrong and the synthesis wrong anyway: *the corpus is more reliable
than the document that summarises it, and the summary is what NDI reads.*

**Interaction with Kazik.** The four binding specification questions were put in the session —
`T-63` (0.5 mM against §3's 2 mM), `T-115` (a layer taller than 10 nm), `P-13` (the electrode
material) and `T-112` (which device §3's desired clause asks for). **All four were deferred to NDI**,
so nothing in the corpus changes and all four stay in *Open questions for Kazik*. Recorded because
the standing instruction is to record the question and the answer whether or not either moves
anything; the loop continued on the tasks that do not depend on them, which is all of iteration 15.

**Process notes, three of them, and two are repeats of documented failures.**
*One.* The `T-136`/`T-138` agent was killed by a `529 Overloaded` at its very last step. Its claims,
challenges, results, model, study and tests had all landed and its queue rows were already updated,
so **nothing was lost** — but the coordinator learned this by reading the artifacts, not the report,
because there was no report. That is `SESSION-PROMPT.md`'s own rule arriving as a fact rather than a
caution: *the claim, the challenge and the result JSON are the artifacts; the report is a courtesy.*
*Two.* **Two agents took `T-139` in the same iteration** — one for a Poisson-Boltzmann pair solve,
one for the third `ANSWERS.md` synthesis — despite claim and challenge numbers having been reserved
in `TASKS.md` up front. **Reserving the claim numbers did not reserve the task numbers**, and task
numbers are exactly as contended. The synthesis moved to `T-147`, past the highest in use rather than
into the next gap, because `C-0076` and `CH-0089` had already cited `T-139` from two files.
*Three.* **Two of the four figures the subagents reported in chat did not survive the grep** — a
trial-function disagreement reported as 28.4 % where the claim says **28 %**, and a log-scale share
reported as 62–68 % where the claim's headline is **67.5 %**. Neither changes anything, and that is
the point: this is the fifth session in which a report and its own claim disagreed, and the check
that caught it is one `grep` per number.

**And the coordinator's own follow-up found `C-0073`'s falsifier had fired a second time, unseen.**
`T-1e`'s agent filed a `CLAUDE.md` correction as a **note**: `T-1d` has two readers, not the one
`P-18` recorded, because `window/ResynthesisInputs.kt` assembles its paths as `File(directory, name)`
and a grep for `File("gpd/results/` cannot see it. Adjudicated as `CH-0092`, **upheld**, and the
census is worse than the note said — **`T-1d` has three readers and `T-1f` has two**, so `T-25`
(`C-0027`) and `T-118` (`C-0051`) had been sitting in the repository built from nine-digit inputs for
a whole iteration. Re-emitted: **325 of `T-25`'s and 2 751 of `T-118`'s numeric fields move**, median
`2.2e−6` and `3.7e−7`, **0 boolean and 0 structural changes**, and **no verdict, flag or quoted
figure moves** — `T-63`'s deciding 0.5 mM stability margin reads **2.16–9.87×** either way and
`C-0016`'s window edges are untouched, being grid indices rather than grid values.
**The finding is what the largest apparent movement turned out to be.** `T-118`'s
`reproductions/22/relativeDeparture` goes `8.79067377e−07 → 0.0` — a 100 % relative change that is a
residual **collapsing to exact zero**, because *that residual was the staleness itself*, sitting in a
field designed to carry exactly that information and read by nobody for an iteration. So the cheap
instrument this failure needed already existed in the file it was hiding in. The other lesson is
about the instrument that failed: **an audit is only as complete as the shape of the search that
performed it, and a path built from a directory and a name is invisible to a search for either half**
— while `WindowResynthesisStudy` states its own inputs verbatim in a `sources` field, which is why
`P-22` asks for a test that a study's declared sources equal the files it opens. `P-19` is now
explicitly ranked **behind** `P-22`, because its ranking of the four remaining rounding sites was
written on the census `CH-0092` corrects.

## 2026-08-17 — Iteration 16

Three science loops plus two process tasks taken in the coordinator's own context. Every number
below was grepped out of its claim file; one that was not there under the name the report gave it is
noted at the end.

**`C-0079` (`T-139`) resolved the programme's highest-value open item by finding the question had no
answer.** `C-0076` had left the plan model's exclusion width as a convention never measured in the
role it is used in, with a verdict that is a **step function at 2.715609 nm**. The answer is that
**two unbonded duplexes in 2 mM MgCl₂ hold no separation at all** — the interaction is repulsive at
every separation on **four independent methods, every one read directly**: osmotic stress with XRD
(*"the force-spacing curve extends to infinity because zero force can only be achieved at infinite
DNA-DNA spacing"*), two all-atom two-duplex PMFs, and the second virial coefficient of **free**
duplexes at **3 mM Mg²⁺**, the nearest measured point to this buffer, *"indicating repulsion"*. So
`d` is not a separation but a **threshold on an energy no plan claim states**, running 8.78601 nm at
1 `k_BT` to ≤ 2.1 nm at 8 `k_BT` and straddling the placement threshold (`CH-0094`).
**What decides it is not the electrostatics the task budgeted for**, nor the short-range hydration law
it went and fetched, but **the energy the host sheet already pays**: holding two of its own duplexes
at the SAXS 2.69 nm costs **7.99970 `k_BT` per crossover column** against **4.94674 `k_BT`** for a
body in `C-0066`'s gap at the threshold — **1.62× cheaper than what the sheet is already paying**, so
**34 of 34 place at every physically calibrated reading** and `CH-0089` is upheld. That number came
from a SAXS constant this repository has quoted for fifteen iterations.
**The cheap bound was right for a reason it did not anticipate.** `λ_D = 3.92688 nm` exceeds the whole
1.78272 nm disputed bracket, so a two-cylinder nonlinear PB solve **provably cannot place an edge**
at 2.7 nm — the expensive solve would have resolved the wrong term. And the second surprise is a
geometry nobody had noticed: **`C-0069`'s `Q5` and `C-0066`'s bound 4 are not the same problem** —
two collinear arms are **coaxial**, 15.1103× cheaper and *finite* at zero gap, so what the collinear
slot must prevent is a blunt-end **stacking bond** (−4.4114 `k_BT` per helix, read directly from a
supplementary table), not a clash. `Q5`'s margin becomes **+1.41561 nm, 55.28×** the published
0.02561 (`CH-0093`). `C-0072`'s conclusion then survives on a wholly new ground: 0.0256 nm at 2.7 nm
is **1.2373 %** of the pair energy, so the knife edge is unresolvable **by physics** as well as by
fabrication. Mean field is *not* controlled here and the claim says so; the bound is empirical.

**`C-0080` (`T-147`) found a THIRD drift class, and both retained tools are blind to it by
construction.** `C-0067` mechanised *"a number in no claim"*; `C-0078` mechanised *"a task asserted
open that the queue closed"*; both now read zero. The class neither can see is **a number still in
the corpus, under a verdict its owning claim still states, that a LATER claim superseded** — it reads
`CITED` **precisely because it has an owner**. Five instances, all in headline material, including
`53×` where the lattice affords 68.9×, and NDI question 1's word *requirement*, earned on a load line
the recommendation does not use. Coverage: **23 of 55 already reflected, 17 carried in, 15
deliberately not carried**, each with its reason recorded so the next pass inherits the judgement.
**The sharpest instance is a fourth thing again**: the deliverable called `T-45` *"answered … and the
answer is a failure"* in §1 and *"still unmeasured"* in §3 of the same file, and **both halves of the
tracer pass it** — §1's sentence has an owner, §3's parenthesis carries no number at all, and
*"unmeasured"* is not a phrasing the status half looks for. **A tool that checks a document against a
corpus cannot see a document that disagrees with itself** (`T-150`). And `C-0078`'s ground rule bit in
the *favourable* direction for once: `CH-0089` makes three of the four floors under the recommendation
stop firing, so *"best element, not a buildable design"* survives on a **weaker** ground than it was
published on — which a synthesis reading only verdicts records as unchanged.

**`C-0082` (`P-22`) built the instrument `CH-0092` said could not be hand-held, and the miss was
larger than the challenge claimed.** A grep for the basename finds **43 of the 63** read edges over
`gpd/results/`; the **20** it cannot see all run through the two directory-joined readers — so
`C-0073`'s instrument was missing **a third of the graph**, not one edge. **The cheap form the task
was queued with barely exists**: **3 of 74** studies declare a `sources` parameter, so *"declared
equals read"* is a real hard gate covering **4 %**, and the other 96 % needs the derivation. Its own
falsifier fired and reshaped the instrument: at **file** granularity `DesignWindowStudy`, which reads
three result files, comes out reading **thirteen**, because package `window` declares `ledger`,
`array`, `reader` and `scalar` privately in several files at once — so a file-level call graph is not
a conservative approximation, it is **noise**. **`P-19`'s ranking inverts** on the derived closure:
`window/` is the *cheapest* of the three rounding sites to re-emit, not the most exposed, because the
cost of re-emitting a site is about its **outputs**; its hazard is an **order** (`T-118` reads
`T-25`). And `C-0073`'s rounding-site table names the wrong studies as well — `CH-0097`, the same
defect on a second table of the same claim. Two things nobody had asked for fell out: `T-3b` is the
most-read file at **16** studies, and **13** result files are read by **tests**.

**`C-0081` (`T-140`) — a seam does not perturb the weave, it DELETES AN EXTREMUM.** At any even plane
every duplex participates in exactly one interface crossover, so removing the junctions at a seam
removes **exactly one pull event from every duplex**. The 40 nm tile is **3.68 weave periods** wide
and admits **8** seam planes; every one puts 6–12 of `C-0063`'s 34 stations inside its straight
window, so **22–28 of 34** survive on a node against 34 seamless — the acceptance's second branch is
answered **NO**, there is nowhere for a seam to hide. **`C-0076`'s verdict stands on its other
argument** — `M = p − d − L` is an along-helix identity between unbonded bodies and carries no weave
coordinate, so the plan margin is **0.898333453 nm at all 8 positions, one distinct value**. What the
seam costs is `C-0076`'s *headline*: the departure at an affected station is exactly `Δ/2` at unit
slope, so **the annihilated 1.2–1.75 nm amplitude bracket is restored at full strength**. **And the
sign is the opposite of the source's wording** — *"opens up"* is the favourable half; the other parity
closes by the same amount, taking the worst across-row clearance to **0.122724 nm** and, at the
measured phosphate girth, to **−0.002276 nm**, a clash. The whole seam paragraph was re-fetched and
read directly; `C-0076` had quoted one clause and the rest changes what it means.

**`C-0083` (`P-23`, coordinator) — `C-0067`'s lesson was recorded and the defect kept happening, in
the claims.** `C-0080` reported one malformed `TASKS.md` row; a sweep found **38 defects in 22 of 245
files** in three kinds, none of them visible to a writer reading their own source. The worst is a bare
`|` in a **header**, which widens the whole table so every *correct* body row becomes the odd one out
— live in **`C-0017`**, the claim that owns the 33.3333 pN/nm coupling mandate, and in five
consecutive evidence rows of `CH-0070`. All fixed with every word preserved; 26 test-first checks;
**0 defects in 243 files**. It caught two rows the coordinator had written earlier the same session.
`third-party/` is excluded because the problem definition as received must keep its defect — **an
invariant that forbids fixing something must be taught to the checker, or the checker decays into a
warning.**

**And the harness's own tests are finally invoked** (`P-21`, folded into `C-0082`). Its premise was
half wrong — `tools/test-snapshot.sh` has hung off `./gradlew test` since `P-16` itself, settled in
one `git log -S` — so only the tracer was orphaned. All three self-test scripts are now Gradle tasks,
and `tools/verify.sh` gained the census gate and a `--no-checks` flag.

**One cross-agent correction, and it is the good kind.** `P-22`'s agent wired `P-23`'s markdown gate
into `tools/verify.sh`, watched it fail, **removed it and documented why**: a verification snapshot
has no `.git`, so the checker's `git ls-files` falls back to a tree walk that emits `./`-prefixed
paths, and the prefix defeated its own `third-party/` exclusion — a gate that can never come clean is
not a gate. That judgement was right and the defect was in the checker. Fixed here with two tests
(`is_excluded` normalises, and the fallback no longer emits the prefix), proved in a real `.git`-less
copy, and the gate restored with the reasoning corrected in place rather than deleted. The same run
also found a genuine table defect in `C-0079`, filed minutes earlier — `A_DNA|w|DNA` unescaped.

**Process notes.** Three agents again took colliding task numbers (`T-148`–`T-150` between one `ls`
and the next), which is now the second consecutive iteration: **reserving claim numbers does not
reserve task numbers**, and iteration 16 reserved both and still collided, because the collision is
between numbers agents *raise*, not numbers they are *given*. `T-151` and `T-152` were renumbered by
their own authors, unprompted. And one report figure again did not survive the grep — a claim
filename given as `C-0081-seam-weave.md` for `C-0081-seam-weave-congruence.md`, harmless, and the
fifth session in which a report and its artifact disagreed on something.

## 2026-08-17 — Iteration 17

Three science loops and two process tasks. Every number below was grepped out of its claim file.

**`C-0085` (`T-152`) — the branch's knife edge was never a residue, it was an integer, and it is 26× wider
than published.** `C-0079` had just shown the collinear slot must prevent a blunt-end **stacking bond**
rather than a clash. The cheap bound then reshaped the question before any code ran, and it is the finding:
**a collinear slot is an AXIAL gap between two duplex end faces, so it is quantised at the rise**, exactly
like the pitch and the arm it is differenced against — `M = (32 − N_d − N_L)` rises. The famous **0.02561 nm**
is the residue of subtracting a *transverse* SAXS constant (7.912 rises) and an elastica root (24.013 rises)
from an **integer** pitch; on the lattice the standing design closes at **exactly zero**, and quantising the
convention up one rise makes the margin **−0.00439 nm** and drops the array to 30 of 34. The criterion is then
an energy — both faces are tethered to one sheet, so a stack costs `½k(g−g₀)²` against **4.41156 `k_BT`** —
giving **6 base pairs = 2.04 nm** and a margin of **0.67561 nm, two whole rises, 26.38×** the published one.
Both of `C-0069`'s unmargined joints gain real room: `c ≤ 2.34166 → 2.94462`, the tip ceiling **79.678 →
133.687** (1.7088× `A2`) and the root **13.930 → 25.689** (1.8988× one crossover), against 1.018× and 1.030×.
34 of 34 still place. **The conservatism turns out to be load-bearing**: a rigid-rooted arm places at four
rises or fewer, so the loose end of the stacking range would **reopen the truss branch `CH-0081` closed**.
And **distance is the only control left** — all three of Rothemund's measured anti-stacking remedies append to
a strand **terminus**, and `C-0034`'s `A2` has already spent both termini a duplex end has: `C-0029`'s
counting theorem striking twice on the same two links.

**`C-0086` (`T-151`) — a seam is a PARITY ON A TREE, not a fabrication convention.** Crossovers join only
*adjacent* duplexes, so a single-layer sheet's row-adjacency graph is a **path** — a tree — and a closed walk
on a tree traverses every edge an even number of times. A **fully folded circular** scaffold therefore gives
every row two segments, i.e. exactly one seam, necessarily. Brute-forced: the path graph carries **2
Hamiltonian paths and ZERO Hamiltonian cycles** at every width from 3 to 12. **So a seam needs both premises
— circular AND fully folded — and the Gen-1 tile fails the second anyway**, taking **1 680** of M13's
**7 249** nt. Rothemund built the seamless raster twice and states the theorem's own prediction in his own
clauses: the 26-helix square *"had no vertical reversals in raster direction, **required a linear
scaffold**"*, and his very first origami experiment was an 8-helix raster on a **circular** M13 with two
thirds unfolded. Three things the programme had not counted: the **remainder** is a 5 569 nt, **33.3 nm** coil
carrying **1.66×** the sheet's own charge in the actuated gap (a purpose-built 1 680 nt scaffold removes it
and needs a **67 nt** return loop); **Rothemund's staggered seam is not the remedy** (best stagger 5 of 34
against a straight seam's 6–12 — the declared falsifier fired); and a seamless raster **quantises the tile
width at 32 bp** — admissible row lengths are odd multiples of 16 bp, **16, 48, 80, 112, 144** — so **§3's
40.0 nm = 117.6 bp is not a buildable seamless width**, the nearest being **112 bp = 38.08 nm** (`CH-0101`).
That is a fifth specification question for NDI and it is now item 5 of `DECISIONS-FOR-NDI.md` and item 10 of
the queue's own list.

**`C-0087` (`T-148`) — the programme's flat tile does not survive the only fabrication statistics anybody has
measured, and the correlation question it was framed around is second order.** `CH-0084` had left the
flatness half *indicative*, because a Bernoulli dropout and `C-0060`'s alternating scatter share a standard
deviation and not a spatial structure. Settled, negatively and unanimously: over sixty
`placement × convention × mandate` cells **not one is inside `T-5b`'s 0.10 even at the median**, and the
lowest exceedance is **89.6 %** of realisations. **What decides it is a cheap bound added after the Monte
Carlo made its shape obvious**: removing **exactly one** of `C-0063`'s 34 paths — 34 solves, no sampling —
takes 0.0706 → **0.5010** of the stroke, and one of `C-0058`'s 45 takes 0.0753 → 0.3060. *An exhaustively
optimised placement is a **cancellation**, and a cancellation has no tolerance to a missing term.* The
position dependence is worth **1.2–1.7×** on the 90th percentile — real, adverse, exactly the direction
`CH-0084` predicted — on a quantity a count of **one** has already taken 4–7× past the tolerance. **And the
dropout reverses the ranking of the two flat designs**: `C-0063`'s 34 equal springs beat `C-0058`'s two-level
45 at zero defects and **lose** under fabrication (0.6391 against 0.4893 at p90), because the denser array
loses less per absent path. Neither holding the mandate nor pre-stiffening by `1/p` repairs it.
**The per-staple map turned out to be readable**, which nobody had tried: all 168 values of Strauss's
supplementary figure recovered and validated three ways, including against the `k/186` quantisation the
imaged-structure count imposes. That corrected `CH-0084`'s edge row in the **favourable** direction — 48 % is
**one corner of 168** and the perimeter *mean* is **77.5 %** (`CH-0102`) — and changed no verdict:
`C-0060`'s threshold needs 89.31 % incorporation and **30 of 168** positions reach it, `C-0026`'s needs
97.19 % and **none** does. A second correction runs the other way: **a 40 nm tile is not a Rothemund
rectangle**, carrying 1.85× the perimeter per unit area, so the measured 0.84 transfers to **0.759–0.790**
here and `CH-0084`'s reading was the optimistic one.
The deliverable is conditioned rather than contradicted: **the tile can be made flat as DESIGNED and has not
been shown to be flat as BUILT**, which is `T-155`.

**`P-24` and `C-0088` (`T-150`), taken in the coordinator's own context, and the second caught the first.**
`C-0071` discharged `T-95` and `T-102` in iteration 14 and `DECISIONS-FOR-NDI.md` records them as discharged;
**`TASKS.md` still carried both as `TODO`, in their rows and in *Open questions for Kazik*, three iterations
later** — the third instance of `C-0071`'s own *"a discharge is invisible to whoever files the removal"*, and
the first in the **queue** rather than in the deliverable. Both struck rather than deleted, analysis intact.
**The checker could not see it either**: `queue_status` knew `DONE`/`KILLED`/`ANSWERED` and read `DISCHARGED`
as OPEN — a word iteration 17 had to coin for a status that is neither *answered* nor *abandoned* but
**stopped applying**. The failure direction is the costly one, so the tuple now carries a comment saying the
vocabulary grows and every missing word reads OPEN.
`C-0088` then mechanised the self-consistency check `C-0080` asked for, and **found two contradictions, not
the one reported by hand**. The second was **`T-95`, created ninety minutes earlier by `P-24`'s own fix**,
which updated one of two mentions. *A hand-audit finds the contradictions that exist when it runs; the very
next edit can make a new one, and a **status** change is the class likeliest to, being the one most apt to
appear in more than one place.* **Third consecutive iteration in which a check caught the mistake of the
person who wrote it** — a headline list (`C-0078`), two `TASKS.md` rows added minutes before (`C-0083`), and
now a half-finished discharge. On `C-0080`'s **fourth** class the answer is a reasoned **no**: a superseded
number reads `CITED` *because* it has an owner, so the exact check needs a `superseded-by` edge at statement
granularity that no claim carries — a corpus convention change, not a tool. The cheap approximation is
identified and priced (it would catch 3 of 5 known instances) and deliberately **not** shipped, its
false-positive rate being unmeasured.

**Process.** Two agents reported honestly against their own interest, which is worth recording: one declared
a **TDD deviation** on `T-151` (model written before test) unprompted, and noted that the test then failed on
two assertions that were real and changed the claim; the other **checked a `CLAUDE.md` correction it had been
handed and found the correction wrong** (`σ_rel = √(f/(1−f))` is not inverted; the inverted form gives 229 %
against 43.6 %) and asserted the right form as a gate test. A third found a `runtimeSeconds` field that made
its own result file permanently un-diffable — *a wall clock in a result file is a step counter by another
name* — and removed it.

**`C-0084` (`T-149`) — the element the programme recommends has NO pull-in fold at 2 mM, and that changes an
answer already sitting in front of NDI.** `C-0071` recommended `Q5` and `CH-0083` was the open objection that
its pull-in verdict had been quoted for a load line it does not use. Searched: at `C-0071`'s own device the
34 hinge-rooted arms fold at **0 of 6** `C-0003` layer models, where `C-0018`'s affine mandate folds at
**6 of 6** and `C-0032`'s strain-softening flexure collapses to 1.0000–1.0019. On the **bias** axis the margin
goes **1.0071–1.0317 → 1.3877–2.5764** (1.8706–3.4699 at 0.5 mM); on the **stroke** axis — the one `C-0032`
showed decides it — the fold moves **3.4104–4.1248 nm → past 7.9097 nm**, and the binding ceiling changes
owner from pull-in to `C-0002`'s `φ = 0.2` at two models and the element's own branch end at four.
**§6 task 4 is discharged for the recommended device**, `CH-0083` is resolved, and `C-0071`'s failure route
`R7` does not fire — nine routes become eight.
**The cheap bound predicted all of it, at 11 of 11 gradable states, from one evaluation of each law**, which
is `CLAUDE.md`'s *"at a fold the composition of two corrections is EXACT"* paying for itself; at 5 of the 11
the omitted level term ran the *other* way and the slope term still decided it. The declared falsifier did not
fire. A second, weaker one **fired in a bounded way and is reported as such**: the arm is inextensible and
only the small-rotation branch is enumerated, so *"no fold"* means *"no fold below 7.9097 nm"* — 2.64× §3's
target, and silent about the large-rotation branch. The control holds: at 7 nm / 10 mM the fold is still
shallower than 3 nm at 4 of 6, so this is not a solver artefact.
**And the consequence is editorial and immediate** (`CH-0098`). `C-0032`'s escalation of 0.5 mM from a
preference to a **requirement** was read on `C-0030`'s strain-*softening* flexure — an element `CH-0081` has
since removed from the output role. So **0.5 mM stays the recommendation and stops being a requirement**: it
is bought for margin (1.39–2.58× becoming 1.87–3.47×, and a stability floor falling from 23.41–27.91 to
3.86–15.94 pN/nm) rather than to keep the device off a fold it is not on, and `T-50`'s multi-week Monte Carlo
is correspondingly less forced. `DECISIONS-FOR-NDI.md` decision 1 is corrected in place, struck rather than
rewritten, and `ANSWERS.md`'s two passages are moved from *qualified and open* to *settled*. **This is
`CLAUDE.md`'s own standing warning arriving in the deliverable — *a correction can be quoted against a stack
that has already left the design* — and nobody has yet asked how many of the SIX routes to 0.5 mM are read on
withdrawn objects.** That census is `T-156`, and it is one pass over six claims.
`CH-0099` is the same shape one level out: **a ceiling *taxonomy* belongs to a load line too.** `C-0018`'s
three candidate ceilings assume a coupling drivable to any stroke the layer admits; a rotating arm is not, and
inheriting the list unexamined would have quoted `CH-0007`'s 1.0 V at a stroke the element model cannot
describe — up to **3.74×** inflation, at exactly the headline states.

**A coordinator note on the two documents in front of NDI.** `DECISIONS-FOR-NDI.md` arrived from Kazik
mid-iteration, as the reviewable form of the four specification questions. It passes both retained checkers
clean — 0 table defects, every numeric token traced — and iteration 17 gave it two edits it could not have
anticipated: a **fifth** decision (the scaffold, `C-0086`/`T-154`, which turned out to decide whether the tile
has a seam at all) and a **correction to its first**, whose severity `C-0084` withdrew within hours of it
being written. Both were made in place and struck rather than rewritten, which is the discipline that file
sets for itself.

## 2026-08-17 — Iteration 18

Four loops. Three closed here; `T-155` ran long and is recorded separately.
Every number below was grepped out of its claim file.

**`C-0090` (`T-153`) — the buildable width does not damage the design, it SELECTS it.** `C-0086` had left
§3's 40.0 nm outside the seamless raster's own quantisation, nearest 38.08 nm. The axis settles half the
branch in one line: the odd-half-turn rule binds the distance between successive **scaffold** crossovers,
which in a boustrophedon are the two ends of **one row** — an **along-helix** length — so `EDGE_X` moves
40.0 → 38.08 nm and the across-helix geometry (15 duplexes at 2.69 nm, 40.35 nm) is **untouched**. Eight
quantities are invariant *by construction* and are asserted at both widths at departure `0.0`.
Then the favourable half: **`38.08 = 7 × 5.44` exactly** where 40.0 is 7.35 pitches, so the row-end scaffold
crossover is a lattice point only at `b ≡ 8 (mod 16)` — **phases 8 and 24, `C-0063`'s own centro-symmetric
pair** — and `C-0015`'s ten eight-column phases collapse to **two**. On a bit-identical station lattice the
best 34-root placement dishes **0.0621469105** against `T-5b`'s 0.10, **12.0 % better** than `C-0063`'s
0.0706145537, the winning phase moving 24 → 8.
**The price is a quantisation that turns out to be geometrically required**: `C-0039`'s 8.16439083 nm
elastica root overhangs a whole base pair by 0.00439083 nm, which at 38.08 nm takes the phase-24 capacity
45 → 38, halves the enumerable family and dishes 0.1427 — outside `T-5b` — while `C-0085`'s **24-rise
8.16 nm** arm is exactly tangent (`32 + 24 = 56 = 112/2`) and restores every number bit for bit. So
iteration 17's clearance recommendation was not merely advisable; the buildable width demands it.
**Three of four substantive results run opposite to the task's framing**, which was written as damage
assessment. Two things it found that nobody had asked for: **`C-0069`'s plan budget is two bounds and only
one was ever written down** — the outboard bound `edgeX/2 − outermost root` crosses the inboard `pitch − d`
at `edgeX = 38.14 nm`, and 38.08 falls **0.176 base pairs** below it, annihilating `C-0085`'s widening on a
three-site row (`CH-0105`); and **a numerical guard became a physical assertion** — `EDGE_MARGIN = 0.05 nm`
is inert at 40.0 nm and at 38.08 nm deletes two of eight columns at exactly the two phases the design wants,
worth 0.0621 against 0.168371808, i.e. the whole verdict. Both readings are carried and the guard is swept.
`C-0053`'s in-plane packer is the branch's largest mover: **43 → 29** arms for a 4.8 % narrower tile.

**`C-0091` (`T-156`) — the six routes to 0.5 mM are THREE, and the finding is not the withdrawal.**
`C-0032`'s route was already withdrawn by `C-0084`. What the census adds is that **two of the remaining five
are the other three, read again**, and the cheap bound proved it before any physics: `T-2`'s
`biasForHundredPiconewtonBlocking` **is** `T-3`'s own number at **15 of 15** states at worst departure
**`0.0`**, and `T-25`'s `bufferComparison` carries `T-16`'s and `T-4`'s extrema at **20 of 20** at
**`2.66e−8`** — one file printing eight significant digits where the other prints nine, *so a `==` would have
called it not a transfer*. **A synthesis that reads CLAIMS cannot see this**: each of the six states its
route truthfully, and only the result JSONs show two of them are one number.
All three survivors still favour 0.5 mM at every layer model, and **two are weaker than they read**:
`C-0018`'s stated ground — *"0.5 mM removes the fold entirely"* — is **void** on an element with no fold at
2 mM to remove, surviving as a **1.3480×** bias-margin preference; and `C-0012`'s **4.9656×** is a
**zero-stroke** blocking-bias ratio worth **1.4823–1.5703×** at the held operating point, an overstatement of
**3.16–3.35×**. `C-0017` is untouched, its floor being element-independent. **Read at the device's own state
the three are worth 1.35×, 1.57× and 1.75×, and they are not three exposures** — all downstream of `C-0008`,
two mechanisms, and `C-0005`'s 123–214 % common mode over all of them. The declared falsifier *that the six
are six* **fired**, and it is the claim (`CH-0106`).

**`C-0092` (`T-157`) — no fold on any branch, and `C-0084`'s branch end was an artefact of its own solver.**
The cheap bound settled the unbounded half with no solver at all: `δ = ∫sin φ < L = 8.16439083 nm` **strictly
on every branch**, because `φ ≡ π/2` contradicts the root boundary condition — so the whole open question was
**0.2447 nm** wide. Then the surprise: the task was written to enumerate the *large*-rotation branch and the
answer came from continuing the *small* one. `C-0039`'s doubling force ladder loses the branch at
**7.9196867 nm**, three decades of force below the right angle its own comment warns about; marched in the
near-end rotation instead, the same arm answers to **8.1610821 nm** at `max_s|φ| = 1.5707924` rad —
**0.999997 of π/2 and still below it**. The extended path has **no fold at 12 of 12** states of the
recommended device over 0.9984 of the contour, leaving **0.0033087 nm**, under 1 % of one base-pair rise.
The large-rotation branches, up to 39 of them, all reach a *smaller* stroke — they retreat from it.
So §6 task 4's discharge for the recommended device is now essentially unconditional, and `CH-0099`'s
candidate ceiling stands while its value does not: the element boundary binds at **0 of 12**.

**Coordinator propagation.** `C-0092` widens the *tops* of `C-0084`'s bias-margin bands (1.3877–2.5764 →
1.3877–7.3137 at 2 mM, 1.8706–10.9072 at 0.5 mM) and leaves both **minima** unchanged; the agent correctly
declined to edit `ANSWERS.md` and `DECISIONS-FOR-NDI.md` for it, its permission having been scoped to
`T-156`, and flagged it instead. Both documents updated here. **`DECISIONS-FOR-NDI.md` decision 1 has now
been corrected twice in two iterations** — severity withdrawn by `C-0084`, route count by `C-0091` — against
a recommendation that has not moved once. That is the healthy shape: the answer is stable and its *stated
strength* was not.

**`C-0089` (`T-155`) — the recovery route from `C-0087` is the right route and it does not arrive; what
refuses it is a COUNT.** Asked whether any placement is flat under the measured staple dropout, the answer is
**no**, over 22 graded `placement × distribution` cells — six densities from 15 to 90 paths, `C-0063`'s 34
upward roots, `C-0074`'s 30, sixteen buildable one-parameter distributions and six per-path descents. The
lowest 90th-percentile dishing anywhere is **0.284537599** of the free-tile stroke, still **2.85×** `T-5b`'s
0.10.
**Everything `C-0087` pointed at is confirmed and none of it is enough.** The percentile falls
**monotonically 0.8522 → 0.5327** as the path count goes 15 → 90, and moving the objective from the
zero-defect value to the **percentile** is worth a further **1.30–1.61×** — both real, both in the predicted
direction. But the density the dropout demands is **13 attachment columns, 195 paths**, against the **34**
`C-0075`'s plan table admits: **5.7× short, in a division that needs no solve**. That is the whole answer, and
it was available before any sampler ran.
**And the reversal `C-0087` read as *regularity* is a count effect**: at matched count the *irregular* upward
roots **beat** the regular grid, 0.5837 against 0.6690. So the ranking flip was the denser array losing less
per absent path, exactly as `C-0087` said — but the property doing the work is density, not regularity, and
reading it as regularity would have pointed the next design at the wrong axis.
`ANSWERS.md` §1 now states the settled form: **the flat Gen-1 tile is a zero-defect result, and no coupling
this lattice can carry makes it a fabricated one.**

**A note on the iteration's shape.** Three of the four loops were formulated as damage assessments and two of
those came back favourable — the buildable width makes the tile *flatter* and *selects* the phase and the arm
quantisation the programme had already chosen, and the large-rotation branch removes rather than adds a
qualification. The two that were formulated as recoveries both failed. That is not a pattern to draw a lesson
from, but it is worth recording that the framing predicted the sign in neither direction.

## 2026-08-17 — Iteration 19

Three loops plus one process task. Every number below was grepped out of its claim file.

**`C-0093` (`T-162`) — the only structural escape from `C-0089`'s count argument is real, is worth 2.2× in
level and 3.0× in slope, and still does not arrive.** A shared body is **not** a rescaling of an array: it is
the same Woodbury system with one term added, `(T⁻¹ + M + F_b) f = w_free`, and the array is its `F_b = 0`
corner. **The declared falsifier did not fire**, and favourably — a rigid shared body on `C-0063`'s own 34
stations dishes **0.0344013403** at zero defects against the array's 0.0706145537, **2.05× flatter on the
identical station set**, and is `T-5b`-flat at every rung of the tie ladder.
The escape is a **division**: under a shared body `C-0017`'s mandate lives in the body's **ground**, which is
a rigid-body mode of the tile and therefore **invisible to dishing**, so the ties are freed from 0.98 to
**3.33 pN/nm**. Under the measured dropout the best of **39** graded cells reaches **0.24028028** at the 90th
percentile — the lowest this programme has reached under fabrication, and still **2.40×** `T-5b`'s 0.10. The
count it asks for is **252 ties against the 53 the lattice offers**.
So `C-0089`'s verdict survives on a narrower ground than it was written on, which is the discipline
`C-0078` named: *re-check the ground of every verdict whose premise is withdrawn, not just the verdict.*

**`C-0095` (`T-161`) — yes, and it is not a permission but the definition of a raster turn.** Rothemund's
odd-half-turn rule binds *"the distance between successive scaffold crossovers"*, and in a boustrophedon
those are **the two ends of one row** — so the 112 bp `C-0086` quantised **is** the crossover-to-crossover
distance, and a crossover at the last base pair is what makes the row 112 bp long. Decided under three
separate headings, each read directly: **the geometry does not forbid it** (a crossover is an *azimuthal*
condition and *"the last base pair"* an *axial* coordinate); **the software does not forbid it** (caDNAno
*"permits the user to force crossovers between any two scaffold bases"*, and cadnano 2.1 **automates** the
raster turn); **and it is published** — Rothemund's 24-helix rectangle is **288 bp = 18 column pitches
exactly**, so both vertical edges lie on the crossover lattice, folded **90 % well-formed**, in the paper
this programme's lattice comes from.
**The falsifier fired favourably**: written to catch *"unprecedented"*, it found *"built, imaged and
counted"*. So the programme carries `C-0090`'s **admitted** reading, **0.0621469105** — against 0.168371808
at the same phase — and the 38.08 nm tile is inside `T-5b`'s 0.10 and **12.0 % flatter** than §3's nominal
40.0 nm. `C-0090`'s wording that *"no crossover has ever been drawn at the last base pair"* is corrected in
place; one has, and no sixth NDI question is needed.
**And the surprise is a theorem**: `C-0086`'s odd-half-turn rule and the row-end column **parity** condition
are the **same congruence**, asserted identical over every row length 1–400 bp — two claims had used both
rules side by side without noticing. It turns the identification of the row-end column with the raster turn
from an argument into a proof, and it splits the eight columns' 56 crossovers as **14 scaffold turns + 42
staple**, the 14 recovering `C-0086`'s own independent LINEAR count from the lattice.
The whole literature answer cost a `grep`: `T-151` had fetched and manifested the primary source two
iterations earlier — hence a new `CLAUDE.md` rule, *check `gpd/data/` before fetching anything*.

**`C-0096` (`T-159`) — the repair lands, and 90 of the 96 outstanding rows could never have moved.** The
cheap bound is the claim: a ceiling that is `min(layer, element)` is unchanged **identically** when only the
element argument rises, so only **18** of `C-0084`'s 108 fold rows were ever candidates. They were re-read by
re-running the whole study rather than by argument, the *"element model branch end"* ceiling `CH-0099` was
raised about now binds at **0 of 108**, and — the part that matters for a solver swap — the continuation
costs **190 sweeps against the doubling ladder's 209**, so the repair is *cheaper* than the defect.
`CH-0112` is the generalisation: **a lost branch is recorded twice, as a fold and as a ceiling**, so a
solver's domain limit can masquerade as physics in two different tables.

**`C-0097` (`T-158`, coordinator) — `C-0091`'s hand finding mechanised, recall 2 of 2.** Both halves closed.
*(a)* `C-0016`'s two clauses cannot be one sentence about the buffer: its §(e) window prefers 0.5 mM and its
§(f) fixed-bias stability count prefers 2 mM, which is not a contradiction but the **force-pinned** and
**fixed-bias** readings of one layer — and **no claim states the pair**, so a synthesis inherits whichever
half its author read. `ANSWERS.md` now names which.
*(b)* The detector recovers **both** instances without being told where to look: `T-16`↔`T-25` at
`C-0091`'s own **2.66e−08**, and `T-2` ⊂ `T-3` at **12 of 72, departure 0, under the same key name on both
sides**. Three decisions each forced by the live case — the unit is a **series** and never a single number;
the comparison is a **tolerance** with the departure reported, because one file printed eight significant
digits where the other printed nine and `==` would have said *not a transfer*; and series are filtered for
**distinctiveness**, because conventions recur everywhere.
**The subset matcher is the important half** — a synthesis *selects* the states it needs, so equal-length
matching misses the commoner shape entirely — and it costs **271 s against 1.0 s**. Neither is a gate, and
that is the finding rather than a limitation: **a transfer is not a defect.** It becomes one only where two
files are **counted** as independent evidence, and no tool can see that.

**A coordinator note.** `T-163` was given a reserved claim number when iteration 19's numbers were allocated
and then not worked, the iteration running three agents rather than four; its row said `IN PROGRESS` for the
whole iteration and is corrected here. **Reserving a number is not scheduling the task**, and the extended
`queue_status` reads a stale reservation as *not open*, which is the same failure direction `P-24` fixed for
`DISCHARGED` — a task the queue believes is being worked is a task nobody picks up.

## 2026-08-18 — Iteration 20

Three loops plus one process task, and the flat-tile question closes on every axis this programme can
reach. Every number below was grepped out of its claim file.

**`C-0098` (`T-165`) — the last unspent axis runs the wrong way, and the reason is the same division that
made the escape look promising.** `C-0093` had left one thing unsearched: the shared body's *placement and
distribution*. Searched on the crossover sites the lattice actually supplies, over **25** graded cells, the
best 90th-percentile dishing is **0.375506727** at **100 %** exceedance — **3.76×** the convention and
**1.56× WORSE** than `C-0093`'s 0.24028028. **Because that number was never buildable**: it sits on an
abstract 90-station grid where the lattice offers at most **60** (`CH-0113`).
Two findings close the axis rather than merely failing it. **The distribution axis shuts as `1/t`** — the
shared body's stiff limit is a *kinematic* constraint independent of how the ties are distributed, measured
as a spread collapsing 0.555431809 → 0.00689107707 → 9.72244009e−05 in lock-step with the matrix departure
from that limit. So **`C-0089`'s 1.30–1.61× was a property of a DIVIDED mandate, and the same division that
makes a shared body flatter is what removes the axis.** And **the real lattice's redundancy slope is
−0.376769756, 2.08× shallower** than the abstract grid's, fixing the columns at **4** against the **13** the
dropout demands — 141.44 nm, **3.54×** §3's tile.
Two more that will cost the next agent if ignored: **the richest phase is the worst host** (all ten 60-site
phases are *seven*-column sheets, disjoint from `C-0015`'s eight-column ten and `C-0063`'s two — phase 17 at
60 ties reads 0.487309625 against phase 24's 53 ties at 0.385192562), and **`C-0089`'s ranking instrument
does not transfer**: Spearman ρ falls from 0.9729 across designs to **0.468487481** across phases.

**`C-0099` (`T-164`) — it does not matter, and that is measured.** Destroying the dihedral spring of **all
14** row-end crossovers, `13.5294118 pN·nm/rad` to exactly zero, moves the best 34-root dishing
**0.0621469105 → 0.0651753854** (ratio **1.0487309**). `T-5b`'s 0.10 is never approached: **34.8 %** of the
convention is unused at zero row-end stiffness against 37.9 % at full, so the whole unknown is worth **three
percentage points of margin**.
**And `CH-0111`'s bracket is not a bracket.** `C-0090`'s two readings differ in *three* things, and the
2.70925468× decomposes as **2.85 % dihedral spring, 97.40 % vertical link, −0.25 % mesh node**. The link is a
**constraint** expressing covalent continuity — which `C-0095` had already settled — so the refused reading
is **outside the reachable set**, and that was the cheap bound at no solve. Channel B is a **step, not a
ramp**: 0.0651072886 at `s = 0.125` against 0.168640591 at `s = 0` exactly, because a penalty at an eighth
strength still enforces its constraint. **Monotonicity was measured, not assumed** — 16 of 16 consecutive
pairs — and no variational argument would have supplied it, a peak dishing not being an energy.

**`C-0100` (`T-160`) — the collar is width-independent, and what is not is the way it is written down.**
Stated fit-free, as `−totalDeficit/centrelineLoad` from the global momentum flux with no fit and no standoff
in it, the whole edge effect is **1.6507426 nm** at 40.0 nm against **1.65008284 nm** at 38.08 — **0.0400 %**,
a factor of eight inside `C-0090`'s declared 0.32 %. `C-0090`'s placement moves **0.0712 %**, so its
carry-forward is checked and the recommendation is to change nothing.
**The cheap bound did not settle it, and saying so is the justification for the solve**: a closed-form tail
model bracketed the movement at **0.131–0.443 %**, straddling 0.32 %. **Four of six falsifiers then fired**,
and together they are the finding: the three numbers `C-0090` literally carries move 0.38–2.15 %, do **not**
converge, and are **not monotone** — all because `fitEdgeTaper`'s 1 nm standoff **snaps to a mesh node**, and
the tile's own half-width therefore chooses where the quadrature starts, at the peak of the enhancement.
Placed exactly, the worst departure is 0.0973 % and the sweep is monotone. **Re-partitioning the same 40 nm
field — no physics, only where the limit sits — moves the flatness 0.368 %, which is 5.2× what the tile's
width is worth and outside the sensitivity the claim declares.**

**`C-0101` (`T-167`, coordinator) — re-emit, and a fold that was a diverged solver.** `T-159` had left eleven
result files the code no longer reproduces. The judgement's premise is false: **git already holds the
record**, and keeping an unreproducible file costs the byte-for-byte re-run diff half this repository rests
on. All eleven re-emitted. **The declared expectation that no verdict moves is FALSE**, and the exception
justifies the exercise: two of `C-0050`'s notes explained a refusal by *"the arm folds"* citing a near-end
rotation of **3.03 × 10¹²¹** — a doubling ladder running away, written into a catalogue as physics — and the
repaired run replaces them with a **kinematic** refusal needing no solver and a **genuine** branch limit at
99.7 % of the demanded stroke against the ladder's 89 %. `C-0050`'s verdict stands; its ground is corrected.

**Three coordinator errors this iteration, all found by agents.**
*One.* My `git add gpd/results/` in the `C-0101` commit swept in **two agents' in-flight result files**. Both
happened to be byte-identical to their final versions, so nothing is stale — but it is the `git add -A`
window `CLAUDE.md` records, committed by the person who has been quoting the rule at every agent. A
directory add is the same hazard as `-A`; **stage the files, not the directory.**
*Two.* Re-emitting `T-149` **broke two gates that read it** — `C-0101` reasoned about amending *claims* that
quote a moved number and never considered *tests* that assert on the file. They were asserting `CH-0099`'s
pre-repair census of 8 element-boundary rows; the repaired file has **0**, which is `C-0096`'s own finding,
so the tests were pinning the stale state and are corrected to the repaired census. **A result file has
readers in `src/test/` as well as in `src/main/`, and `C-0082`'s census already said so** — 13 result files
are read by tests — which is exactly the fact `C-0101` had in front of it and did not use.
*Three.* The first re-run of those gates failed with `NoSuchFileException: in-progress-results-generic.bin`
and was very nearly read as a real failure; it is the concurrent-`test-results` race `CLAUDE.md` documents,
from running Gradle beside two agents' verify runs. **The cure is in the file and the coordinator did not
apply it to itself.**

## 2026-08-18 — Iteration 21

Three loops plus two coordinator edits. Every number grepped out of its claim.

**`C-0104` (`T-172`) — IT DOES MATTER, and it is the first row-end unknown that does.** `C-0099` had just
measured the row-end crossover's *stiffness* and found it worth three percentage points of margin. Prestrain
is a different question and the answer is the other way. A crossover prestrain is an **initial stress**, so
`½k_θ(Δφ − θ₀)²` leaves the stiffness matrix untouched and enters the lattice as a **load vector** — which
collapses the whole `θ₀` axis to one solve, and puts `T-5b`'s 0.10 at **15.45°** of uniform row-end
prestrain. **The lattice's own register ladder reaches it**: `C-0090`'s recommended placement holds the
convention at the 8 bp (±4.286°) and 16 bp (±8.571°) rungs and **loses it at 0.1013 at the 32 bp rung in the
adverse sign**. A re-optimised design absorbs it — 0.0826 and **0.0711** at ±17.14° over a
163 296-placement re-enumeration — but `C-0090`'s published key is the optimum at only **1 of 3** states, so
**the recommended design is a function of an unmeasured parameter in a way `C-0099`'s stiffness sweep was
not**. No accessible source quantifies it, over 10 recorded queries and 68 records, and Rothemund says so
himself. Added to `ANSWERS.md` §5.

**`C-0102` (`T-171`) — the three demands are irreconcilable at both widths, and `C-0090`'s collapse
sharpens the question rather than closing it.** A 96-row census with no solve gives **10 / 10 / 2** at
40.00 nm and **2 / 2 / 2** at the buildable 38.08 nm — richest `{0, 16}`, eight-column `{8, 24}`,
centro-symmetric `{8, 24}` — **still disjoint**. **Phase 8 is recommended**, at 0.0658484805 against the
richest phase 16's 0.125068659, and the decision is one division: 52 → 60 ties buys **1.12×** or **1.06×**
on the two measured slopes, and the phase it demands costs **1.90×**.
**The sheet-side price nobody had computed is 14 channels, 9 of them closed form.** A seven-column host
splits 4/3, so the **series** `D_⊥` loses `6/7` where the **smeared** one loses `7/8` — exactly **`48/49`**
apart, the smeared reading optimistic, and a seven-column sheet is the first *designed* non-uniform lattice
`C-0054`'s two readings have ever been read on. `C-0054`'s spendable budget falls 42 → 35 (its 75 % is an
eight-column number), and under `C-0087`'s measured incorporation a seven-column sheet is **3.59×** likelier
to lose *every* crossover on some interface, which takes the series `D_⊥` to exactly zero.
Two things worth keeping: **`C-0015`'s *"seven columns is the better layout"* is a POINT-LOAD statement** —
under `C-0022`'s distributed solved load the sign reverses, +4.981 % — and `CH-0118` finds `C-0090`'s
*"the row-end crossover can never be an upward site, at any phase"* rests on a quantifier that fails at
phases 0 and 16: **a plane lands on the row end mod 8 bp but is a column only mod 16 bp**. The entire richest
set at the buildable width exists only under that convention.

**`C-0103` (`T-163`) — `CH-0103`'s missing column is real, it is +12.86 %, and the recommendation it
challenges does not pay it.** At fixed station geometry on `C-0063`'s own phase-24 lattice, the 34 → 30
reduction moves the 90th-percentile dishing **0.638498565 → 0.720607136** under the measured dropout, against
a plan margin that improves **68.9×**. So the axis `CH-0103` said nobody had priced is real and adverse —
and the move the programme actually recommends is not the one that pays it.

**`C-0105` (`T-169`) — the note is re-worded and no verdict moves, because the clause-correct reading is
3.33× STRICTER.** 40 pN/nm is `1.2 × (100/3)`; the same construction at §3's *desired* clause is **12**, and
the softest of `T-79`'s 26 placing rows is **13.3×** past it. The miss does not rest on a declared number at
all: **26 of 26** are also past `C-0006`'s cited 45 pN/nm per-path secant. **And the defect was in a file
neither `C-0101` nor the `T-169` row names** — so the coordinator's own scoping of the task was wrong, and
the agent found the real site.

**Coordinator.** `ANSWERS.md` §5 had absorbed no addition since iteration 12 while the programme's frontier
moved — `C-0067`'s worst drift class in the one direction none of the three mechanical checks can see, since
they verify what is *present* and nothing verifies what is *missing from a disclaimer*. Four items added, of
which the first is now the most consequential missing measurement in the programme: **a per-site
incorporation map on a coupling-bearing tile**, because the flat-tile question is closed on every coupling
axis and what decides it is a fabrication yield. `T-175` queued for the fourth synthesis.

**A post-iteration-21 correction, found by an agent in the coordinator's own tool.** `check-markdown-tables.py`
had **two behaviours and nobody had noticed**: in the checkout it lists `git ls-files` and therefore skips
**untracked** files, while in a verification snapshot — which has no `.git` — its fallback walks the tree and
checks everything. A new claim is untracked until it is staged, so the local run reported clean and the
snapshot found a defect **in that very file**. The tool was disagreeing with itself about its own remit,
which is exactly the class `C-0088` mechanised for the deliverable, now in the instrument that checks it.
Fixed so the default sweep is tracked **plus** untracked-but-present — the direction that catches more — with
four tests, and the two environments now agree at 325 files. **That is the second defect an agent has found
in a checker the coordinator built and the third time a check has caught its own author.**

## 2026-08-18 — Iteration 22

**`C-0106` (`T-175`) — the deliverable's three mechanical checks were clean and the tile it describes was the
wrong size.** That sentence is the iteration's finding. Of 48 items (`C-0081`–`C-0105`, `CH-0093`–`CH-0120`),
**14 were reflected, 24 carried in, 10 deliberately not**, and `C-0080`'s third drift class is at **12
instances in 48** against its own 5 in 55.
**Both declared falsifiers fired.** The first was written as *"the only drift left is inside passages that
exist; it fires if a whole structural determination has no passage at all"* — and it fired **twice**. §3's
40.0 nm tile is **not a buildable raster width**, so the tile is **112 bp = 38.08 nm** and **12.0 % flatter**
there, and the strings `38.08`, `112 bp`, `seam` and `seamless` appeared **zero times in 830 lines**. And
§5's *"six questions for NDI"* was the **old** six: two discharged two iterations earlier still in rows 3 and
4, their two live replacements absent — **while §1 of the same file already named one of them.** Every row
was individually correct and the **set** was stale. *A count can survive while none of its membership does.*
The second falsifier — *no claim in the range is wrong* — fired mildly and produced **`CH-0121`**, the first
challenge an `ANSWERS.md` pass has ever raised against a claim: `C-0102`'s headline says the three
phase-demand sets *"stay disjoint"* when two of them are **identical** at `{8, 24}`, which its own census
table prints.
> **A checker that reports zero can be reporting zero about the wrong object.** All three existing checks
> compare the file to the corpus, to the queue, or to itself. **None can see a determination with no
> passage.**

**`C-0107` (`T-182`) — the cheap bound does not close it and the derived value is past the threshold.** All
**8** ceilings on a row-end crossover's prestrain lie **above** `C-0104`'s 15.4497275°, by 5.5–36×, because
the couple at the threshold is only **3.65 pN·nm**. And `C-0104`'s register ladder is **the wrong ladder**:
its rungs are *per-domain* offsets, but every domain's error carries the **same sign**, so it accumulates
along a duplex and what limits it is the duplex's own torsion. So the prestrain question is not bounded away
— it is live, and the recommended design depends on it.

**`C-0108` (`T-178`) — the count effect does not hold at all 32 phases and the decomposition is not one.**
Run search-free and nested at every phase, the 34 → 30 reduction is adverse at **27 of 32** and
**favourable at 5**, spanning **−4.60 to +12.21 %** against `C-0103`'s +12.86 % at phase 24. So
`C-0103`'s defence of the recommendation — *count term +12.86 %, phase term −19.0 %* — is not a
decomposition, and the two axes interact.

**Coordinator.** `DECISIONS-FOR-NDI.md` question 6 said *"`T-165` has not been run"*; it ran two iterations
ago and `C-0098` closed that axis **negatively**. Corrected in place — and the correction narrows the
question rather than the answer: what a *yes* now buys is a body 2.05× flatter **at zero defects**, not a
fabricated flat tile, and the programme has **no unspent design axis left, only a fabrication yield**. That
is the second outward-facing document to carry a stale statement I wrote, and `T-184` records that it has
**no checker at all**.

**A postscript to iteration 22, from `T-178`'s own report.** Two things it found are worth keeping beside
`C-0108`'s verdict. **The cheap ranking instrument DOES transfer across phases** — ρ = 0.883–0.978 over 32
phases at each of six counts — so `C-0098`'s ρ = 0.468 belongs to a **shared body at mixed tie counts**, not
to the phase axis, and a phase screen on the array is affordable at `n` solves a cell. That scopes `C-0098`
in its own favour without a challenge, which is the pleasant direction of `C-0078`'s ground rule.
And **`C-0098`'s own published cells already said the phase runs the other way** (+6.53 % from 24 to 8 at
full inventory) — a cheap bound that was sitting in the corpus before the task that needed it ran. That is
the third time this session a question has been answered by something already filed, after `T-161` finding
its whole literature answer in `T-151`'s manifested sources and `T-182` finding Snodin already fetched.
`ANSWERS.md` §4(g) now carries the **total-not-the-split** reading: the decomposition is path-dependent
(count-first +12.86 / −19.27 %, phase-first **−11.48 / +2.93 %**, the phase term changing sign between
orderings on the search-free grid) while both totals agree at **0.0**. The recommendation rests on the total
and stands; the explanation it was given does not.

---

## 2026-08-18 — NDI answers all six decisions, and two of them turn out to be one

**Not an iteration.** Kazik put [`DECISIONS-FOR-NDI.md`](DECISIONS-FOR-NDI.md) to Jeremy Barton and all six
came back in one pass, by email, with the reply reproduced verbatim in that file and in `TASKS.md`'s
*Open questions for Kazik*. This entry records the interaction, per `SESSION-PROMPT.md`'s standing rule: what
was asked, what was answered, and what changed as a result. **Nothing has been re-derived**; the work the
answers open is queued as `T-191`–`T-195`, and no claim has been written on any of it.

**The answers.**

| # | asked | answered |
|---|---|---|
| 1 | 0.5 mM MgCl₂ as the nominal buffer? | **priced, not granted** — *"concerningly below the typical experimental stability window of DNA origami … pushing a parameter hard that I've been reserving for additional operating margin. So… well identified."* |
| 2 | a 17–26 nm polymer layer? | **not examined** — *"an interesting regime we've been reserving, again, for low MgCl₂ concentrations we'd buy with additional work on stabilizing DNA origami at low salt."* |
| 3 | which electrode? | *"Defaulting to template stripped gold for initial experiments."* |
| 4 | one device or two? | *"2 devices."* |
| 5 | which scaffold? | *"M13, circular ~7-8K nucleotides"*, 50 k available, hierarchical assembly above that — *"to use exess scaffold, just make the tile thicker. The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness."* |
| 6 | a two-layer tile? | answered **inside** 5, and volunteered rather than granted |

**What changed, in the order of how much it costs us.**

**1. Decisions 1 and 2 are one decision, and we asked them as two.** Both answers name the same reserve —
operating margin bought with work on stabilising origami at low salt — so NDI can spend it once. We can rank
the two spends on our own numbers and it is not close: the buffer buys **1.35×, 1.57× and 1.75×** at the state
the device occupies (`C-0091`), three routes that are **common mode** below `C-0005`'s 123–214 % error and
therefore not three; the tall layer is the only route to a whole clause of §3 (`C-0050`). **Spend it on the
layer.** This is `C-0091`'s own finding — counting routes that are one number — arriving in the deliverable
rather than in the corpus, and it is `T-194`.

**2. §3 specifies a ~10 nm tile and every structural claim here modelled a 2 nm one.** *"Tile thickness ~10 nm
(single-layer honeycomb)"* cannot hold both ways; `electrostatics/DnaOrigamiTile.kt` and `C-0021` both record
the contradiction and both carry **two readings** of it, and the corpus took the thin one every time. NDI's
answer resolves it toward the thick reading — and it arrives from the direction nobody was watching, the
**scaffold**. `C-0086` measures the sheet at **1 680** of M13's **7 249** nt, **4.31×**, and the remedy offered
for the excess is more layers. Four layers at the honeycomb rise is ~10 nm, which is §3's own number, so three
independent statements describe one tile and **it is not the tile the flatness negative was derived on**.
`C-0006`'s own variant table already carries it: `D_∥` = **14 310.78 pN·nm** against 85.50 and `D_⊥` ≥
**19.222** against 3.345, and `C-0058`'s reach is their fourth root — 12.83 and 5.71 nm become ~46 and ~8.8 nm
against a 38–40 nm tile. That is a cheap bound and not a result; it is `T-191`, and it is the first unspent
design axis since iteration 20.

**3. *"Thin and low stiffness"* is a criticism this repository already had the measurement for.** `C-0072`
carries Fischer's SAXS width for a single-layer sheet — **9.1 %** against **2.9 %** for a multilayer brick —
and Kube et al.'s inability to solve a single-layer structure at all, *"due to excessive conformational
heterogeneity"*. We had the corroboration and had not read it as a verdict on the tile.

**4. `T-50` is not deleted, and the file said it would be.** `DECISIONS-FOR-NDI.md` predicted that a one-word
answer to decision 1 would delete the beyond-mean-field Monte Carlo and that only *"hold 2 mM"* would keep it.
The answer was a **price** — neither word — so 2 mM stays the nominal and the last unbounded exposure on the
critical path stands. **A binary was assumed where the answer was a cost.** The one prediction in that file
the answers falsified, and it is worth keeping as a lesson about how a specification question is posed.

**5. Two devices makes the desired clause its own object, and its refusal is quoted at a state it need not
occupy.** Device B is `C-0046`'s 10 pN/nm placement and `C-0017`'s stability floor refuses it at **2.34–2.79×**
— at the 10 nm layer in 2 mM, which is exactly the state decisions 1 and 2 together say device B does not sit
at. So the three answers converge on one object: **device B is the low-salt tall-layer device**, and whether it
exists is one evaluation of `|k_eff|` in a corner nobody has evaluated. `T-192`, and it also answers NDI's own
objection to decision 2 — that 17–26 nm is 4.3–6.6 bulk Debye lengths — which **no claim here has ever
addressed**, because nothing has evaluated the bias that delivers 100 pN across a tall gap.

**6. Gold removes an uncertainty and does not remove a result.** The 2.6× material bracket collapses onto its
**adverse** end — `C-0021`'s gold row becomes the number — and `C-0021`'s *"retardation is sourced for gold
only"* caveat is discharged. Every verdict stands, because the finding there is about the *shape* of a `1/h³`
force. **The potential of zero charge was not given**, and that is the half that supplies the entire
thermal-scale hold-down at 0.9–5.1 mV. `T-193`; if the literature does not pin it for template-stripped Au in
mM MgCl₂, it goes back as a one-line ask.

**And one more drift found by the answers themselves.** Six answers came back to a list of five: `T-166` lived
in `DECISIONS-FOR-NDI.md` and **never in `TASKS.md`'s *Open questions for Kazik***. That is `C-0071`'s *"a
discharge is invisible to whoever files the removal"* run backwards — an **addition** the register never
learned about — and no checker in the tree can see it, because all three compare the deliverable to the corpus,
to the queue, or to itself, and this was a question the queue never had. It is now item 11, and the three
checks (`trace-answers.py` numbers, status and self-consistency, and `check-markdown-tables.py`) are clean on
all of it.

## 2026-08-18 — Iteration 23, `T-193`: the answered material moves nothing, and the unanswered PZC is 90–576× the deciding scale and of the wrong sign

Filed [`C-0111`](gpd/claims/C-0111-gold-electrode-pzc.md), raising
[`CH-0128`](gpd/challenges/CH-0128-inverse-debye-length-called-with-a-bjerrum-length.md).
Result [`gpd/results/T-193-gold-electrode-pzc.json`](gpd/results/T-193-gold-electrode-pzc.json),
sources and three retained drivers in [`gpd/data/T-193-sources/`](gpd/data/T-193-sources/),
ten tests in `src/test/kotlin/anchoring/ElectrodePotentialOfZeroChargeTest.kt`.

**What was done.** NDI's answer to decision 3 — *"Defaulting to template stripped gold for initial
experiments"* — was carried into `C-0021`'s two zero-bias mechanisms. `M4` was re-read at gold alone rather
than over four materials; `M3`'s missing quantity, the electrode's potential of zero charge, was searched for
in the literature and found.

**Half 1 is bookkeeping and it confirms the standing reading rather than repeating it.** Gold is the
*stiffest* of `C-0021`'s four candidates, so the 2.6× specification bracket collapses onto its **adverse**
end: 10.356–17.159 pN at 5 nm on a 2 nm tile, 0.737–1.422 at 10 nm, the state's own bracket narrowing by
exactly **3.25905934×**. The deepest gold well anywhere in the box is **8.742 `k_BT`** — at the most
favourable corner for confinement, the 5 nm gap on §3's *thick* tile reading — against the 10 `k_BT`
criterion, so **0 of 6** gold states confine. The verdict is unchanged and the reason is structural: a `1/h³`
force integrates to a bounded potential, and naming a material changes the amplitude, never the exponent.
One **ground** moved where the verdict did not: `C-0021`'s retardation factor is *"sourced for gold only and
applied across the whole electrode bracket"*, and at gold alone that substitution does not exist. Half the
caveat is discharged; the other half — the DNA constant is already retarded, so the low end retards that half
twice — stands.

**Half 2 is the load-bearing half, and the cheap step was an identification rather than a bound.**
`diffusePotentialOfAppliedBias` solves `V = ψ_d + σ_e(ψ_d)/C_S`, and with no tile present `V = 0` makes the
whole interfacial drop vanish — which is the *definition* of an electrode carrying no free charge. **So the
model's "applied bias" is the RATIONAL potential `E − E_σ=0`, not a potentiostat setting**, and `C-0021`'s
0.9–5.1 mV contact-potential table is a table of rational potentials. That is a reading of forty lines of
existing code, it cost nothing, and it turned an open question into a lookup.

**The number exists and it is adverse.** `E_pzc(Au(111)) = 0.46–0.51 V vs SHE` in Ar-saturated **1 mM
HClO₄** — Adnan, Behjati, Félez-Guerrero, Ojha & Koper, *Phys. Chem. Chem. Phys.* **26**:21419 (2024), open
access, **read directly**, three surface preparations printed on **two scales each**, which is a transcription
check the Nernst relation closes to 1.4–1.6 mV at this project's 300 K. Corroborated by an independent paper
(Liu, Doblhoff-Dier & Koper, *ACS Electrochem.* **2**:995, 2026: *"around 0.5 V vs. SHE"*), and Au(111) is the
right proxy because a template-stripped film's crystallites *"are expected to dominate"* in that orientation
(Avedian, Trang & Inkpen, *ACS Nanosci. Au* **5**:269, 2025). **An electrode held at zero volt on that scale
therefore sits 90.2–575.7× the deciding threshold away from zero charge, and on the side that charges it
NEGATIVELY** — repelling the negatively charged tile instead of holding it down. `C-0021`'s `M3` row reads
*"DOWN but negligible"* and that is a statement about an electrode **at** its PZC, which `C-0021` says in as
many words and no downstream reader has.

**And the honest answer is a threshold, because the offset is outside the model.** `C-0005`'s point-ion
boundary is **0.0974 V** of diffuse drop at a *negative* electrode — Mg²⁺ is the counterion there and the
boundary goes as `1/z` — against 0.1966 V at a positive one, and 9 of 9 exposure states fall outside it. So no
force is quoted at the PZC offset. What is quoted is **the electrode must be held within 5.10 mV of its own
potential of zero charge at the 10 nm layer, and 0.886 mV at 5 nm**, which is a control requirement on the
drive electronics rather than a property of the material.

**What surprised us, in three places.**

**1. The sign.** Everyone in this programme, including this task's own framing, has read *"a contact potential
of a few millivolts supplies the entire hold-down"* as an unresolved *magnitude*. It is an unresolved
**sign**. Gold's PZC sits near the positive end of the aqueous window, so almost any nominal zero — a
potentiostat at 0 V against a common reference, a cell at open circuit — sits **below** it, and the residual
field then *lifts*. The whole sign structure of that field lives inside **11.2 mV** of rational potential at
10 nm (thermal-scale lift at −6.087 mV, no net force at −0.314, thermal-scale hold-down at +5.102), which is
41× narrower than the offset a bench would have to null.

**2. The literature search returned the wrong missing number.** The task expected *"the PZC of template-stripped
gold in mM MgCl₂"* to be unfindable and to return as an ask. The PZC **is** findable — on a single crystal, in
perchlorate — and what is missing is not the electrode's number but the **cell's**: how the Gen-1 drive defines
its zero. That is a specification question of exactly the kind `DECISIONS-FOR-NDI.md` collects, and it is a
different question from the one that was queued. The gold/MgCl₂ gap is real (32 recorded queries, and the
divalent leg is not empty — 48 records across three phrasings, none of them a gold PZC) but it is **bounded**:
the Au(111)/Au(110) facet spread is 0.3 V and 0.46 V would be needed to reach the deciding scale.

**3. A defect was found by a number that was too CONSTANT.** The material-bracket narrowing came out exactly
`3.25905934` at 5, 7 and 10 nm, which a gap-dependent screening acting on two materials with 10.6 % and 24.6 %
zero-frequency shares cannot produce. The cause: `C-0021` and `C-0023` both write
`buffer.inverseDebyeLength(lb)`, and **that method's first parameter is a temperature**. The Bjerrum length
read as 0.714 K gives `κ = 5.2195 nm⁻¹` against the documented **0.2547**, a factor of 20.5, and `e^(−2κd)`
saturates to `2e−23`. It is used in exactly one place — the zero-frequency screening of the **low** end of the
van der Waals bracket — where it annihilates that term and therefore lands the low end exactly on *"fully
screened"*, **which is what `C-0021`'s own prose declares the low end to be**. So the emitted number is right
for the stated bracket and the expression that produces it is not; the repair is worth 0.93 % at 5 nm and
0.073 % at 10 nm and moves no verdict. **Every other `inverseDebyeLength` call site in the tree — twenty-two
of them — uses the default.** Filed as `CH-0128` rather than patched, because the repair moves two committed
result files and because it makes the bracket's own *definition* move with it, which is a claim-level choice.
This is `CLAUDE.md`'s *"a defect that is invisible in the answer is invisible to every check written on the
answer"* for the fourth time, and the first found by a **consistency observation** rather than by a re-run.

**Method notes.** The field was re-read **parametrised by the diffuse drop** rather than by the bias, per
`CLAUDE.md`'s own rule that one solve gives the force *and* the bias that produced it. That made `C-0021`'s
three published thresholds reproducible from the **opposite** direction — `3.6e−7`, `1.7e−8`, `4.0e−9` — which
is an independent check rather than a restatement, and it made a 37-rung ladder at three gaps affordable
(111 solved states, 111 numerically resolved). The result file is **byte-identical across two runs**.

**A harness note worth keeping.** With four agents on the box the load ran to 12 on 8 cores and a
`tools/verify.sh` snapshot spent **more than 25 minutes** in `compileKotlin` before its 30-minute timeout
killed it — twice. What worked was a **persistent** snapshot (`snapshot_tree` + `drop_files` into a directory
this agent owns and cleans up itself): one 2-minute cold compile, then **8-second** incremental test runs and
**4-second** study runs, because `verify.sh` and `study.sh` throw their build directory away on exit and pay
the cold compile every time. For an iterate-and-rerun loop under contention, snapshot once and keep it; use
`tools/verify.sh` for the authoritative final run.
## Iteration 23 — `T-191`: the tile §3 actually specifies

**Agent A.** `T-191`, claim [`C-0109`](gpd/claims/C-0109-four-layer-tile.md),
challenges [`CH-0124`](gpd/challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md)
and [`CH-0125`](gpd/challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md),
result `gpd/results/T-191-four-layer-tile.json`,
model `src/main/kotlin/tile/FourLayerTile.kt`, study `tile.FourLayerTileStudyKt`, 20 tests.

**What was done.** §3's parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"* and the two clauses
cannot both hold. This repository resolves it **both ways in different packages** — `ActuatorGeometry.tileThickness`
and `DnaOrigamiTile.thickness` both default to **10.0**, and every structural study builds a **2 nm** single layer —
so `C-0022`'s load was computed for a thick tile and the flatness for a thin one. `C-0086` measures the thin sheet at
1 680 of M13's 7 249 nt, and NDI's answer to decision 5 is *"just make the tile thicker."*
`Gen1Tile` has built a `four-layer-honeycomb-rigid` variant since iteration 2 and **`TileFlatnessStudy` solves
`variants.first()`** — the four-layer row has never been carried into any solve at all.

**What was decided, and why.** The cheap bound is a fourth root and ran first: `D_∥` 85.5018587 → 4 547.17603 pN·nm
and `D_⊥` 3.34504758 → 240.931249 at the measured interlayer coupling, so `C-0058`'s reach goes
12.8290845 → 34.6447329 nm along the helices and 5.70561353 → 16.6216854 across, and `C-0089`'s run-robustness
demand falls from **13 columns to 5** — 4 to 9 over the whole coupling bracket, so the direction commits to nothing.
The solve then said something the bound did not: **the four-layer tile is flat with no coupling at all**,
0.0577199433 of the stroke against the single layer's 0.307902368 and `T-5b`'s 0.10.
What survives is a statement about the **coupling**: under `C-0087`'s measured dropout the best coupled four-layer
90th percentile is 0.116465044, **1.16×** the convention against the single layer's 0.532748246 — a 4.6× narrowing,
and inside what `C-0089`'s distribution and `C-0093`'s topology axes are already known to buy.
One circular M13 pays for **exactly four layers and not five**: 6 720 of 7 249 nt, 92.7 %.

**What surprised us.**

1. **The interlayer coupling turned out to be measured, and it closed a 39× bracket to 0.26–0.33.**
   `INDEPENDENT` and `COMPOSITE` differ by 39.448 for four honeycomb layers and no solve narrows that.
   Four measured bundles — two lattices, three laboratories, three techniques — put a real crossover-linked body at
   `f = 0.26–0.33`, and **Wang et al. publish the rigid-composite formula themselves, name it a "naïve model" of
   "rigidly linked rods", and measure it to over-predict by 2.7×**. `InterlayerCoupling.RIGID` is CanDo's assumption,
   in the very sentence both of this repository's duplex elastic constants come from.
2. **`k_s/k_θ = S/B` makes the parallel-axis enhancement the SAME factor along and across the helices, exactly**,
   so a multi-layer sheet's anisotropy is **invariant** along the whole coupling axis — 18.8735 at both ends.
   `C-0006`'s standing four-layer variant reads 744.5, which is 39.4× a value the model cannot take at any coupling:
   it applies the parallel axis in one direction only and is a **mixed state, not a bracket end** (`CH-0124`).
   And it is not cosmetic: that variant's free-tile dishing is 0.160153834 and would have reported the four-layer
   tile as **not flat**.
3. **The grillage never reads `layers`.** `OrigamiGrillage` takes five scalars from its sheet and none of them is the
   layer count, so building it on the four-layer variant gives a lattice **bit-identical** to the single-layer
   honeycomb one. The machinery of this repository is single-layer, and its crossover combinatorics are the square
   lattice's. The four-layer body had to enter as a **smeared equivalent sheet**, and saying so is part of the answer.
4. **The coupling became the thing that fails.** Every coupled cell of the calibrated four-layer tile reads *worse*
   under dropout than the uncoupled tile — `CLAUDE.md`'s *"an attachment coupling can be a NET DISHING SOURCE"*
   read on a tile that no longer needs the correction.
5. **Eight upstream numbers reproduced at a worst departure of 7.2e−5**, including `C-0089`'s own 0.6142 and
   0.532748246 and `C-0063`'s 0.3079 — which is what licenses the comparison at all, and the declared falsifier
   `F5` existed to make that a test rather than an assumption.

**Verification.** `tools/verify.sh --drop-file src/main/kotlin/actuator/TallGapDeviceBStudy.kt
--drop-file src/test/kotlin/structure/InteriorCrossoverPrestrainTest.kt` — **2 502 tests completed, 4 failed**,
and all four belong to concurrent agents' unfinished work (`TallGapDeviceBTest`, whose main source had to be
dropped, and a fresh `ElectrodePotentialOfZeroChargeTest`). No `FourLayerTileTest` failure. Five gates PASS.

**Owed to `ANSWERS.md`, reported rather than edited.** Row (g) — *"Does the tile stay flat?"* — is entirely a
**2 nm-tile** result and says so nowhere; its closing sentence, *"the question is closed on every coupling axis
this programme can reach"*, is true of the tile it was derived on and not of the tile §3 specifies, because the
axis that was never spent is the **body**. §1's own stated open item (ii) is answered. Row 5's *"just make the
tile thicker"* now has the arithmetic that closes it. `C-0093`'s brick number, carried in row (g), row 6 and
`DECISIONS-FOR-NDI.md` twice, is under `CH-0125` and should not be re-quoted without it. And it is worth saying
that **nothing in §2, §5, §6 or the design-window rows moves**, because a reader who learns the tile is five
times thicker will assume otherwise; the two that would move — `C-0022`'s charge and the stack geometry —
are named as consequences and are not re-derived.

## Iteration 23 — `T-190`: what the 42 interior crossovers carry, and whether their cancellation holds

`C-0107` closed iteration 22 with a number that decided a verdict and an explanation that had never
been posed as a question. Its boundary layer is a **field** `u(x)` over the whole row, so every one
of the sheet's 56 crossovers is built at `(−1)^b u(x)`; read that way `C-0090`'s recommended
placement is **flat at 0.0922622** of the free stroke, and read as `C-0104` reads it — a prestrain
on the **14 row-end sites alone** — it **fails** at 0.1022820. `C-0107` attributed the difference to
the 42 interior sites and said so in as many words: *"The difference between the two readings is the
42 interior crossovers."* `T-190` asked what those 42 actually carry.

**The answer is that they carry the larger half, and the explanation was wrong in a way that makes
the claim stronger.** `C-0112`, `gpd/results/T-190-interior-crossover-prestrain.json`.

**1. The field is exactly separable and the verdict is not, and that is the deliverable.** A
prestrain is a load, so for disjoint site sets the solved field splits as an **identity** — worst
superposition departure **2.1e−15** in the coefficient vector, at both overall signs. But peak
dishing is a **seminorm** of that field and does not add: the graded peak is **0.294** of the sum of
its two parts' peaks. So the cancellation is a **cross term**, and its convention-free measure is
the cosine of the two dishing fields under the lattice's own area inner product, **−0.579495374**,
on an interior field carrying **0.688** of the row-end field's area norm. *No part of the flatness
verdict can be assigned to either site set* — which is a sharper statement than either "separable"
or "not separable", and it is the one the linearity actually licenses.

**2. The cancellation is structural, and the census sees it before any solve.** On the phase-8
lattice the 56 sites are 8 columns of 7, and the graded field is `−22.540 / +14.897 / −8.455 /
+2.740` degrees going inward — **even in `x`, alternating with the column**, because `u` is odd and
`C-0015`'s parity rule ties the glide factor to the column parity. The column next inboard of the
row end therefore carries **0.661** of the row end's amplitude, on the same seven sites, **at the
opposite sign**. The ladder confirms it: adding that one column pair recovers **106.2 %** of the
whole row-end-to-graded move and the two inner pairs walk it back. The 42 interior sites carry
**53.65 %** of the assembled absolute couple (86.25 pN·nm against 74.51). *"The other 42"* is not a
remainder.

**3. `C-0107`'s comparison differences three factors, not one.** Its graded field's own row-end
restriction is uniform — measured, not asserted — at **−22.5397532°**, which is the sign `C-0107`
itself calls **adverse**, because the lattice's parity rule puts every even interface's row-end
crossover at the negative end and the glide factor cancels the alternation. So the consistent
row-end-only counterpart of 0.0922622 is **0.1190748**, not 0.1022820, and the published 0.0100 is
an interior term **plus a sign flip running the other way**. The `2 × 2 × 2` factorial separates
them: station **0.0002586**, interior **0.0268125**, sign **0.0170514**. **The interior term is
2.68× what was credited to it.** `CH-0129`.

The cause is worth recording on its own: `C-0107`'s Deliverable 3 *prose* derives the sign on an
**assumed** end assignment (`b` even ↔ `+L/2`) and its gate-3 test asserts that assumption, while
its *study* reads the assignment off the **lattice** and gets the opposite. Both are internally
consistent; they differ by exactly the global factor that `CH-0130` shows nothing in this repository
determines. **A gate written on an assumed geometry does not test the geometry the study solves.**

**4. The overall sign was never a settled quantity, and reading the other one costs one solve.**
`C-0107`'s Deliverable 3 settles a **relative** question — whether the 14 share a sign — and its
composition argument is right. It does not settle the **absolute** one: relabelling the interfaces
by one negates the whole field, and no source or claim here fixes which interface parity folds which
way. `C-0104` sweeps the sign at every rung; `C-0107` sweeps it for the row-end-only states and not
for the graded one. The missing state reads **0.0910197** — flat. `CH-0130`, raised and discharged
on the number in the same claim.

**5. So `C-0107`'s 0.0922622 survives, and survives more robustly than it was published.**
Reproduced here at **1.1e−10** on the same host; flat at **both** overall signs; and flat at **40 of
40** cells of `C-0107`'s own 12-cell boundary-layer bracket at both signs — including 16 cells where
the **lattice's** hinge is moved with `α` as well as the field's, which `C-0107` did not run —
against **14 of 40** for the row-end-only idealisation. The reason is one ratio: **the row-end-only
idealisation is the `λ → 0` limit of the graded field**, and at `λ = 18.62 nm` against a 19.04 nm
half-row the Gen-1 tile is nowhere near it. `C-0104`'s three distributions are maps on 14 sites
because `C-0104` had no field; `C-0107` has one, and it covers the tile.

**What surprised us.** Four things. That the interior sites are the **larger** half of the
eigenstrain, when the question was posed as *"what do the other 42 carry"*. That **one** column pair
does the entire cancellation and slightly overshoots it, so the inner 28 sites are a correction to a
correction. That a claim's prose and its own solve could disagree about a sign that neither is wrong
about, because the quantity is genuinely undetermined. And that the first emission of this study
flattened its own load-bearing `2.1e−15` to exactly `0.0` through `RESULT_ABSOLUTE_FLOOR` — the
**third** recorded instance of *"an absolute floor is a claim about units and it does not travel"*,
committed by an agent who had read the entry. Lowering the floor to `0.0` then hit the **second**
half of the same `CLAUDE.md` entry — `roundForResult` throws `Cannot round NaN value` on an exact
zero when its floor is zero — which is a latent defect in a shared rounding site that no caller had
reached, because every existing caller passes a positive floor that catches the zero first. Repaired
with a test — a strict no-op for all forty-odd existing callers, the smallest floor in use elsewhere being
`1e-18`, which is exactly why it had gone six iterations unfound. **An entry that names a defect is not a repair.**

**What `ANSWERS.md` owes.** It carries `C-0104` and **stops there**: `C-0107` is not mentioned at
all, so the deliverable does not carry the derived **17.15–24.98°** value, the reversal of `C-0099`'s
oxDNA recommendation, the Snodin scope clause that excludes exactly these sites, or now `C-0112`'s
decomposition. The *"still missing measurements"* list still reads *"the tension in a row-end
crossover … `T-5b`'s 0.10 sits at 15.45°"*, which is `C-0104`'s threshold with no value beside it.
Two iterations of this branch are un-synthesised. Not edited here, per the iteration's scope.

---

## Iteration 23 — `T-192`: the corner all three NDI answers point at is empty, and NDI's own objection was the right one

**Task.** NDI answered decision 4 with *"2 devices"*, which makes §3's **desired** clause its own device with
`C-0017`'s arithmetic giving it `k_c = 100 pN / 10 nm = 10 pN/nm`; and decision 2 with *"17-26 nm of polymer
thickness is beyond the regime I've bothered to examine as the debye length of operation in 2 mM MgCl2 is only
about 4 nm … an interesting regime we've been reserving, again, for low MgCl2 concentrations"*.
Together they name one corner — a 10 pN/nm coupling on a 17–26 nm layer at 0.5 mM — and NDI's own reason for
not having examined it was an objection **no claim in this repository had ever answered**: nothing here had
evaluated the bias that delivers §3's 100 pN across a gap taller than 15 nm.

**What was done.** `C-0008`'s nonlinear Poisson-Boltzmann gap solve and `C-0018`'s stroke-parametrised
equilibrium path, re-run as libraries at heights neither has been asked about: 30 reachability cells over
5–26 nm × 0.5/1/2 mM, six reachability **thresholds**, a layer census under **two** grafting-density rules,
and **384** solved states — 4 heights × 2 rules × 6 `C-0003` models × 2 buffers × 4 load lines (free,
device-B at 10 pN/nm, device-A at 33.333, and a 100 pN dead load). `C-0110`.

**The answer, and it upholds the objection.** §3's 100 pN stops arriving across a gap of **13.6989 nm at
0.5 mM**, 11.8724 at 1 mM and 10.1299 at 2 mM. Every one of them is below the **bottom** of NDI's band: the
0.5 mM reserve moves the threshold by 1.352×, and NDI's 17 nm *begins* 1.24× beyond where the reserve leaves
it. So §3's force target is reached across a resting 17–26 nm gap at **0 of 12** cells — 49.967079 pN at the
best of them (17 nm, 0.5 mM, 1.0 V) and 1.10569504 pN at the worst — and pushing from the point-ion 1.0 V to
the 1.23 V
electrochemical bound is worth **0.05 nm**, because the force is already saturated in bias.

**And the corner takes device A down with it.** Device B is ADMITTED at **1 of 96** states, in **1 of 6**
layer models — 17 nm, held-density, 0.5 mM, `strong-stretching(virial)`, `V* = 0.167607 V`, `|k_eff| =
1.02367 pN/nm`, margin 9.77×, no fold — which is a **bracket disagreement and not a design**, because at the
same height and buffer `alexander-box(two-body)` folds at a 5.83 nm stroke. Device A, §3's *acceptable*
clause, is refused at **96 of 96**, every one for the same reason: a 3 nm stroke from a 17–26 nm layer leaves
a 14–23 nm gap and the field cannot put 100 pN across it. **A tall layer is not a trade of one device for the
other. It loses both.**

**What it does buy is exactly what `C-0050` priced and no more.** The **uncoupled** tile reaches a 10 nm
stroke at **52 of 96** tall states, including three of six models at 26 nm in 0.5 mM. `C-0050`'s escape
table — reproduced here to ≤ 2.0e−4 at all six models — is a root of `P(L₀−10)·A = 100 pN`, a statement about
a **compression**; it never asks whether any field can apply the hundred piconewtons at the resulting gap.
**The escape is real in displacement and empty in force**, and the two had never been separated. `CH-0127`.

**A second challenge, and it needed no solver.** §3's effort-point row — *"~20–25 nm above the electrode"* —
is reproduced **at both ends** by §3's own three layer heights plus a 10 nm tile and a 5 nm attachment
(20 / 22 / 25 nm), which is why `CLAUDE.md` records the row as *fixing* the standoff at 5 nm. At 17–26 nm the
same addition gives **32–41 nm**, and 27–36 nm with the lever bonded straight onto the tile — **1.08× to
1.64× past the top of the band**. `C-0050`, `ANSWERS.md`, `TASKS.md` and `DECISIONS-FOR-NDI.md` all cite that
row as evidence the geometry *"is not absurd"*. It is the row that refuses. `CH-0126`.

**What surprised us.** Four things.

**1. The declared falsifier fired, and in the adverse direction.** `T-192` declared that if the measured decay
length departed from the bulk `λ_D` by more than 10 %, this programme's standing *"the Debye length is three
numbers here"* answer would apply and NDI's objection would be using the wrong one. It departs by up to 35 %
— and the *wrong way*. `ℓ/λ_D` is a function of `κh`, not of `h`: at 2 mM, where 17–26 nm is 4.33–6.62 Debye
lengths, it is **0.910–0.983** and `C-0008`'s far-field limit is essentially reached, so NDI's number is
exactly right there; at 0.5 mM, where the same gaps are only 2.16–3.31 Debye lengths, it is
**0.649–0.819** — the field decays *faster* than the bulk Debye length, so **diluting the buffer makes NDI's
own estimate optimistic rather than conservative**. The counterion-dominance answer is not wrong, it is about
the wrong quantity: the ratio at these gaps is still 6.37–38.94 (ion **content**), while the length that
follows from it, 1.54–1.91 nm, is nowhere near the measured 3.6–6.4 nm decay. `CH-0004`'s own escape clause
fires a second time, a gap decade further out.

**2. The reserve helps and the direction of the trade was predicted backwards.** The task's Plan predicted
that dropping 2 mM → 0.5 mM would buy the exponent and pay for it in the prefactor, on the argument that the
saturated far-field amplitude goes as the bulk ion density. Measured, the net is **+4.10× to +9.56×** at
17–26 nm: the exponential wins outright. The prediction was wrong in sign and irrelevant in consequence — the
gain is real and the shortfall is still 2.00–9.46×. Recording it because a Plan's prediction is a falsifiable
artefact and this one was falsified.

**3. `i * (X/n)` at `i == n` need not equal `X`, and a range `require` then kills a nine-minute sweep.**
`EquilibriumPath.fold`'s coarse scan evaluates `at(i * step)` with `step = strokeCeiling/coarseSteps`. On this
task's `strokeCeiling = 25.144662445344164` nm the twelfth step landed at `25.144662445344167` — **three
units in the last place above the ceiling** — and `at`'s own range check threw, three quarters of the way
through the sweep. Repaired with `minOf(i * step, strokeCeiling)` and the same clamp on the golden-section
bracket. The repair **moves no emitted number anywhere in the repository, and that is a proof rather than an
estimate**: `minOf(a, b)` returns `a` bit-identically whenever `a ≤ b`, and the only altered path previously
threw and therefore produced no result file to move. Six studies consume `EquilibriumPath` and none can move.

**4. A convergence axis was written at a state where the quantity does not exist, by an agent who had read
the entry warning about it.** The fold axis was first named in advance — 20 nm, held-density, 0.5 mM — and
that state has **no fold**, so it converged on `null` and reported `0` at all six settings, silently. It is
`CLAUDE.md`'s own *"pick the axis's state AFTER the sweep, from the states where the quantity survives"*, and
the cure is one line: the axis now selects the first device-B state that actually folded. 76 of the 96
device-B states have no fold, so the odds of naming a good one in advance were 1 in 5.

**5. The re-emission measurement the coordinator asked for found a stale file, and it was not this
repair's.** `C-0110` changed a **shared** main source, so the argument that it moves nothing had to become a
measurement. All five studies in the repository that call `EquilibriumPath.fold` were re-emitted: **`T-4`,
`T-60`, `T-76` and `T-149` came back byte-identical, and `T-157` moved by 17 fields.** A controlled A/B —
an isolated copy with `PullInStability.kt` restored to `HEAD`, same inputs — returns a `T-157` **byte-identical
to the repaired run**, so all 17 belong to the *input*. `T-157` reads `T-149` at run time, and **`C-0101`
re-emitted `T-157` BEFORE it re-emitted `T-149`, in the same commit**: the committed `T-157` reproduces the
**pre-`C-0101`** `T-149` margins digit for digit at all twelve rows. So `C-0092`'s `A5` clause *"the margins
move by 1.0000–3.3380×"* is measuring a difference `C-0101` had already absorbed upstream, and the correct
reading is **1.0000 everywhere**. `CH-0131`, `T-200`.

The lesson is one `CLAUDE.md` already carries — *"its hazard is not size but ORDER"* — and it was made by
**`C-0101`, the claim that established the re-emission discipline**, inside its own eleven-file sweep. A
re-emission needs a **topological sort** of the reader census, not a list; `tools/result-reader-census.py`
already computes the edges. And the residual sat unread for six iterations because nothing re-runs a file
whose producer nobody touched. **A proof that a change is invisible is not a substitute for running the
consumers, because the run also checks everything the proof was not about.**

**What `ANSWERS.md` owes.** Four things, none edited here per the iteration's scope.
§6 row 2 (decision 2) still says *"§3's own tile row already allows the effort point at '~20–25 nm above the
electrode', so the geometry is not absurd"* — withdrawn by `CH-0126` — and still says *"nothing here has
evaluated one"* and *"nothing has evaluated the bias that delivers 100 pN across such a gap (`T-192`)"*, both
of which `C-0110` closes. §6 row 4 (decision 4) still says device B is *"refused by `C-0017`'s floor at
2.34–2.79× at the 10 nm layer in 2 mM, which rows 1 and 2 together say device B need not occupy"* — the
implied escape is now measured and empty. §1's *"unreachable in physics — NO, and it is false; a taller layer
delivers it"*, inherited from `C-0050`, needs the displacement/force split of `CH-0127`. And the headline
number the deliverable does not carry at all is the one a specification conversation needs: **§3's 100 pN
stops arriving at 13.6989 nm at 0.5 mM.**

---

## Iteration 23 (coordinator) — four agents in parallel, and the largest single result since iteration 20

**Shape of the iteration.** Four subagents on the four highest-priority unblocked items, run concurrently;
the coordinator took `T-183` and `T-194` in the main context and did the staging, the cross-checking and
the commit. Numbers were reserved in `TASKS.md` **before** any agent started — and for the first time in
seven iterations there were **zero collisions**. Every reserved number was used, and every number taken
outside a block was taken above every block and reported.

**What the iteration found, in order of how much it moves.**

**1. The flatness negative does not survive the tile §3 actually specifies** (`C-0109`, `T-191`). Not
because a coupling works on the thicker tile — because the thicker tile **does not need one**. At the
interlayer coupling four measured origami bundles support, a four-layer honeycomb tile at `C-0086`'s
buildable 38.08 nm dishes **0.0577199433** of the stroke under `C-0022`'s solved collar **with no
attachment coupling at all**, inside `T-5b`'s 0.10, against the single-layer tile's **0.307902368**. One
circular M13 pays for **exactly four layers and not five** (6 720 of 7 249 nt, 92.7 %) — which is NDI's
own arithmetic, arriving from the direction nobody was watching. The cheap bound predicted it before any
lattice was assembled and needed only a fourth root. **§4(g) of `ANSWERS.md` is a result about a 2 nm tile
and says so nowhere.**

**2. NDI's own objection to decision 2 is upheld, and it is worse than NDI stated** (`C-0110`, `T-192`).
§3's 100 pN stops arriving across a gap of **13.6989179 nm at 0.5 mM** — *below the bottom* of NDI's
17–26 nm band. A tall layer does not trade device A for device B: it **loses both**, refused at 96 of 96
states on the acceptable clause. And the concession is ours — this programme's standing rebuttal
(*"the Debye length is three numbers and the gap's is counterion-set"*) is about ion **content** and never
about the **decay**, and diluting to 0.5 mM makes NDI's own estimate **optimistic**, because the far field
is reached in `κh` and not in `h`.

**3. That killed `T-194`'s premise between the queue row and the measurement** (`C-0114`). The row said
*"the tall layer is the only route to a whole clause of §3"*; it is a route to **none**. So the re-issue of
decisions 1 and 2 is a **correction**, labelled as one, and the reserve NDI can spend once has **one**
claimant rather than two. What is left is a **price** question, which is NDI's column and not ours —
the same distinction the answer to decision 1 already taught us.

**4. The unresolved electrode quantity was a SIGN, not a magnitude** (`C-0111`, `T-193`). Gold's PZC is
**0.46–0.51 V vs SHE**, read directly, and it is 90–576× the deciding scale: an electrode at zero volt on
the rational scale is negatively charged and **lifts** the tile. The whole sign structure lives inside
**11.2 mV**. The material half moved no verdict and moved one *ground*. The residue is a one-line ask about
the **cell's** definition of zero, now filed under decision 3.

**5. `C-0107`'s 0.0922622 survives and its explanation does not** (`C-0112`, `T-190`). The field is exactly
separable — a prestrain is a load, so the 14 + 42 split is an identity to `2.1e−15` — and the **verdict is
not**, because peak dishing is a seminorm. The 42 interior sites are the **larger** half (53.65 % of the
assembled absolute couple), and the published comparison differences two states that differ in **three**
factors.

**What the coordinator's own two items were.** `T-183` (`C-0113`) taught the deliverable's
self-consistency check to read challenge statuses — and the thing it fired on was a **false positive**,
which is the result: whole-sentence attribution read a task's verdict onto a challenge named 180 characters
later in the same table row. Two measured guards, zero false positives. `T-194` (`C-0114`) is above.

**The one process lesson worth carrying, and it is about coordination rather than physics.** Agent B
changed a **shared** main source and its comment asserted *"no emitted number moves"*. Asked for the
measurement rather than the proof, it re-ran all five consumers — and the run found something the proof
could not: `C-0101` re-emitted `T-157` **before** its own input `T-149` in one commit, so `C-0092`'s
*"the margins move by 1.0000–3.3380×"* measures a difference that had already been absorbed, and the true
reading is 1.0000 everywhere (`CH-0131`, `T-200`). **A proof that a change is invisible is not a substitute
for running the consumers, because the run also checks everything the proof was not about.** One question,
one defect.

**And a harness lesson.** Four agents each running a `verify` and a `study` is past this box: load 11.3,
3 GB available, and the documented `-Xmx3g` OOM'd anyway, because that cure is **per daemon** and the
constraint is the **product**. Nothing broke — Gradle fell back to in-process and every study landed. Four
agents is the ceiling for *filing* work, not for *compiling* it.

**What is owed.** `ANSWERS.md` now carries the largest debt any synthesis pass has faced, and **three of
the four agents reported it independently** — which is itself the finding, since none could see the others'.
Queued as `T-201`.

---

## Iteration 24 — `T-201`, and an API overload that turned a parallel iteration into a serial one

**How the iteration actually went.** Three agents were commissioned — `T-201` (the fifth `ANSWERS.md`
synthesis), `T-196` (the composite-fraction threshold `C-0109`'s verdict turns on) and `T-200` (amending
`C-0092`). **All three died on transient API 529s, twice each**, without writing a byte. The tree was clean
after every failure and no reserved number was consumed, so nothing was lost but time. Rather than keep
relaunching against an overloaded API, the coordinator took the work into the main context and ran it
serially. `T-201` is complete; `T-196` and `T-200` are next.

**`T-201` (`C-0115`) — the census four passes have reported is the wrong summary.**

The cheap bound ran first and was the strongest yet: **3 of 20** items in range cited by ID before the pass,
against `C-0106`'s 34 of 48. That said in seconds that this pass had a product. Final partition: **14 carried
in, 4 already reflected, 2 deliberately not.**

What makes this pass different from the four before it is that **two of the twenty items are not values at
all.** Every previous pass reported *"not one of these is a function of `σ`"* beside an unmoved window, and
that is true again — but the two largest items are **scope corrections to what the deliverable's answers were
about**:

- §4(g)'s *"the question is closed on every coupling axis this programme can reach"* was a statement about a
  **2 nm** tile, and §3 specifies a ~10 nm one (`C-0109`).
- §1's *"only `T-115`, a taller layer, can buy the desired stroke"* was a statement about a **displacement**
  the layer admits, where the clause needs a **force** the field must deliver (`C-0110`, `CH-0127`).

**Neither is reachable by any check in the tree**, and this is `C-0106`'s finding in its sharpest form yet:
not a missing passage, but a passage that **existed and was correct** while being *about* the wrong object.
All four retained checks were clean before the pass and clean after.

**The deliverable was also under-claiming again, for the second time.** It called the electrode's potential of
zero charge `STILL OPEN` one iteration after `C-0111` answered it from published measurement. `C-0067`'s rule
holds and is worth restating: *a deliverable that under-claims is as wrong as one that over-claims and is far
harder to catch*, because a reviewer checks the assertions and not the disclaimers.

**And the checker caught its own author, for the third consecutive iteration.** Two firings, both real, both
caused by this pass's own edits: three numbers written at the wrong precision — two truncated **below** and
one written **above** what their owning claims state, which the tracer flags equally loudly and correctly —
and a sentence reading *"ANSWERED … and what is open is a different question"*, which put *open* inside the
verdict window of a task the queue records as closed. The sentence was **wrong as written**: the residue is
not `T-193`. Rephrased rather than suppressed. The check that caught it (`C-0113`) shipped one iteration
earlier and was written by the same author it caught.

**Two things deliberately not carried**, each with a reason that is itself the discipline: `C-0113`, a claim
about a document checker that no §6 answer depends on; and `CH-0131`, whose amendment `T-200` owns — carrying
it now would put a correction in the deliverable with no claim behind it, which is the drift class this pass
exists to prevent.

**Two passages are owed a re-read the moment `T-196` files**, and that dependency is written into the
deliverable rather than only into this journal.

**`T-200` (`C-0117`) — the amendment, and the instrument that was already in the tree.**

`C-0092`'s `A5` clause said *"the margins move by 1.0000–3.3380×"*. Verified off the re-emitted `T-157`
rather than inherited from the challenge: **1.0000 at every one of 12 folds**, worst departure **3.0e−09** —
the solver's own noise. Everything else in `A5` stands, the *"element model branch end"* ceiling still binding
at 0 of 12, so the correction is to a **range** and not to a finding.

**The declared falsifier did not fire, and establishing that cost one comparison.** `C-0101`'s eleven
re-emissions contain exactly **two** dependency edges — `T-157`←`T-149`, violated, and `T-138`←`T-136`. The
second is **clean**, and it needs *no solve at all*: the consumer reads eight named values out of the
producer's parameter block, six of them are echoed, and all six match the current file exactly. So the defect
is one edge of two rather than a systematically wrong sweep.

**The instrument that prevents the class was already in the repository.**
`tools/result-reader-census.py` (`P-22`) derives the read graph including the **transitive** edges a grep
cannot see, which is exactly the graph a topological sort needs. Turning it into an order is twenty lines and
eleven self-tests (`tools/reemission-order.py`), and run against `C-0101`'s own eleven it prints both
constraints. **A re-emission sweep is a sorted order, not a list** — and the sharper form of the lesson is
that remembering the rule was never going to be enough, because the claim that *wrote* the discipline is the
one that broke it, inside its own sweep, in the same commit.

**And the tempting general gate is measured and declined.** A staleness gate on reproduction residuals looks
attractive — `CLAUDE.md` records that such a residual *is* a staleness detector and that `T-118`'s went unread
for an iteration. Across the tree there are **499** nonzero departures in **64 of 104** result files, and the
great majority are legitimate: a reproduction against a *literature* value is expected to differ (Fields
et al. 15.3 %, Marras et al. 19.7 %, Bosco 5.1 %), and against a different *model* likewise. A gate on that
signal would fire constantly on correct files, which is the one failure `C-0080` says a checker cannot afford.
Declined **with the number** rather than by assertion — the same discipline `C-0067` used when it refused to
ship an approximation whose false-positive rate was unmeasured.

**`T-196` (`C-0116`) — the four-layer verdict holds, with 3.30× of margin, and the cheap bound removed the
sweep before it ran.**

`C-0109` overturned this programme's flatness negative and said in its own §11 that the verdict turns on one
number — the interlayer coupling fraction `f`, measured at 0.26–0.33, with the flatness crossing somewhere in
`(0.00, 0.26)`. It is at **`f` = 0.0788618807**, and the measured band's *adverse* low end clears it by
**3.29690337×**, its centre by **3.80411927×**. **Of three declared falsifiers, none fired.**

**The cheap bound is the part worth keeping.** `multiLayerRigidities` admits `f` only through
`realised = 1 + f(factor − 1)`, and that **one** number multiplies `D_∥` *and* `D_⊥` alike — the identity
`k_s/k_θ = S/B` that `C-0109` already asserts as a gate. So `f` is a **pure scale** on the plate, the
threshold is a **scalar inversion** rather than a two-dimensional search, and the whole task was minutes. The
identity is asserted at `1e-12` before any plate is solved.

**Monotonicity was measured, not assumed.** The search counts *every* sign change over `[0, 1]` and found
**one**. `CLAUDE.md`'s warning — that a non-monotone verdict has no threshold and gets more alternating under
refinement — is the reason the search scans before it bisects rather than differentiating at `f = 0`, which
would have been the obvious shortcut and is the one `CLAUDE.md` records as having the wrong *sign*.

**`CH-0124`'s geometry has its own threshold and that is not a detail.** The true honeycomb spacing `d√3/2`
scales `Σy²` by 3/4 and therefore moves `factor` itself (29.8359739 against 39.4479652), so the crossing there
is a **different number** — 0.105149174, still cleared, by 2.47267753×. Both are carried because the challenge
is open, and the corrected geometry is the 1.33×-tighter one, which is the direction `CH-0124` predicted.

**What the verdict now rests on is one unmeasured number**, and it is stated as a threshold rather than a
confidence: the `f` a 15-wide × 4-deep **slab** realises, against the **rods** every published calibration is
measured on. It would have to fall below **30 %** of the least-coupled measured bundle for the tile to stop
being flat.

**Two traps caught in passing.** A `Map<String, String>` of results carried full `Double.toString()` precision
into a file that declares nine digits — `CLAUDE.md`'s *"a number emitted as a STRING is not rounded"*, walked
into with the entry already written down, and repaired by routing every string-mapped number through an
explicit rounding. And two of my own test assertions were wrong rather than the code: one asserted the root to
`isCloseTo`'s 1e-9 *relative* default where the bisection promises a 1e-9 *absolute* bracket, and one used a
strict `>` on a bracket endpoint that a converging bisection lands **exactly** on. Both are `CLAUDE.md`
entries already, and the second is its *"a strict comparison between quantities that can be equal by
construction reports a tie as a finding"* in a new place.

`ANSWERS.md`'s three passages that said they were owed a re-read when this filed are discharged.
