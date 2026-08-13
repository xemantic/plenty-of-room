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
