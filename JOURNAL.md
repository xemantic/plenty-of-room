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
