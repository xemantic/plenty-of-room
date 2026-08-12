# NDI Gen-1 Actuator — Simulation Problem Definition and the Iteration Loop

From: Jeremy Barton

*Draft email body. Nano Dynamics Institute (NDI), August 2026.*

---

## 1. The system

NDI is building electrically addressable DNA-origami nanomechanics.
Generation 1 is deliberately
the simplest stack that could work.

**Bottom to top:** patterned electrode → (optional thin high-k
dielectric) → grafted polymer
layer → DNA origami tile. Everything under aqueous buffer with Mg²⁺.

**Mechanism.** The DNA phosphate backbone carries a large net negative
charge. Positive bias on
the electrode applies a downward force on the tile; the polymer layer
resists; the tile sits where
the two balance. This is a **linear actuator** — voltage sets an
applied *force*, and displacement
is that force divided by the load stiffness. The stiffness of the
polymer layer is not itself being
modulated.

One coupling worth stating because it changes the stability analysis.
Let z be the tile's height
above the electrode. The attractive electrostatic force grows as z
decreases, so its contribution to
the stiffness, k_es = −∂F_es,z/∂z, is **negative** — a displacement
toward the electrode increases
the force driving it. Effective stiffness is

&nbsp;&nbsp;&nbsp;&nbsp;`k_eff(z, V) = k_brush(z) + k_es(z, V)`, with
`k_es < 0` and `|k_es| ≈ F_es/λ_D`
for exponential screening.

This is the DNA-scale analogue of MEMS electrostatic spring-softening
and pull-in, with one
structural difference: the osmotic restoring pressure diverges as the
layer is compressed, whereas
the parallel-plate MEMS spring is linear. Whether that divergence
removes the instability, or
merely bounds it, is an open question we have not answered.

**The restoring force is osmotic, not elastic.** What resists the tile
is the free-energy cost of
raising polymer volume fraction by squeezing water out of the layer.
Chain-stretching elasticity is
the smaller, opposing term under compression.

---

## 2. The model we currently start from

Stated so you know our prior, not to constrain you. If the right
answer is that this framework is
inapplicable, that is a useful result.

**Osmotic pressure of a semidilute solution.** Good solvent, scaling
(des Cloizeaux):
Π ∝ φ^(9/4), from the correlation length ξ ∝ φ^(−3/4). Mean-field
gives Π ∝ φ². At theta
conditions and in the concentrated/melt limit the exponent goes to 3.

**Alexander-de Gennes equilibrium height**, good solvent: `L₀ ≃ N a
(a/s)^(2/3)`, with N monomers
per chain, a the monomer size, s = σ^(−1/2) the grafting spacing.

**Compression.** The de Gennes (1987) scaling form for two opposing
brushes at separation D < 2L₀:

&nbsp;&nbsp;&nbsp;&nbsp;`P(D) = (k_BT/s³)[(2L₀/D)^(9/4) − (D/2L₀)^(3/4)]`

with the first (osmotic) term inheriting the 9/4 directly from Π(φ),
and the second being chain
elasticity.

**Two caveats we want handled explicitly rather than inherited:**

- Our geometry is a brush against a **rigid wall**, not two opposing
brushes. The asymmetric case is
  a documented source of prefactor confusion in the AFM
brush-mechanics literature, and more than one
  functional form is in circulation (the scaling form above, a
logarithmic variant, and the
  Milner–Witten–Cates SCF result, which does not reduce to the same thing).
- Π ∝ φ^(9/4) is the good-solvent **semidilute** result, and it is the
semidilute part we are least
  sure of. Water is a good solvent for PEG at 300 K in dilute low-salt
conditions (χ < 0.5), so the
  solvent-quality premise is fine as stated — but a working brush sits
at polymer volume fractions
  where the semidilute scaling may already be crossing over toward
concentrated behaviour, where the
  exponent moves toward 3 irrespective of solvent quality. Where that
crossover sits for our layer
  decides which exponent we are entitled to.

  PEG/water also has an unusually mobile χ, for reasons that are
specific to it rather than generic:
  it shows reentrant (LCST-type) phase behaviour, kosmotropic salts
drive it toward poor-solvent
  conditions — that is the mechanism behind cloud-point grafting — and
there is at least one report
  that chain tension itself degrades solvent quality above ~30 pN per
chain, which is within a factor
  of two of the tension in a densely grafted brush. We would rather
these were checked at our
  operating point than inherited from the dilute-solution textbook case.

---

## 3. Current parameters

| Quantity | Working value |
|---|---|
| Tile footprint | 40 × 40 nm (test tiles up to ~70 × 100 nm) |
| Tile thickness | ~10 nm (single-layer honeycomb); effort point may
sit ~20–25 nm above the electrode |
| Polymer layer | PEG, PEO, or a PS→PEG reinitiation block copolymer;
5 / 7 / 10 nm heights |
| Grafting density | open — this is the main free variable |
| Buffer | 2 / 5 / 10 mM MgCl₂ |
| Debye length | ~4 nm at 2 mM Mg²⁺ |
| Target stroke | ≥3 nm acceptable, ~10 nm desired |
| Target force | ≥100 pN |
| Bandwidth | ≥1 kHz |
| Temperature, medium | 300 K, aqueous; k_BT = 4.142 pN·nm |

We have deliberately not filled in a stiffness for the polymer layer.
Deriving it from the
parameters above is part of the problem.

---

## 4. What we want to know

Six open questions. We are not asserting how they relate to each other.

**(a) Grafting density and regime.** We want brush-regime compliance,
not mushroom-regime compliance.
Dense antifouling-grade PEG is far too stiff to actuate; sparse
grafting falls out of the brush
regime. Is there a window, and where is it?

**(b) Layer height.** How do stroke, stiffness and screening trade
against thickness across the
5–10 nm range, and is there a reason to go outside it?

**(c) Porosity and ion partitioning.** Mobile ions inside the polymer
layer screen the field exactly
where we need it. How much hydrated-ion inclusion do we get as a
function of the layer's structure?

**(d) Poroelasticity.** Water has to move in and out of the layer as
the tile displaces. We want the
drainage time and its scaling with thickness and volume fraction
bounded. Our own back-of-envelope
does not suggest this is the binding constraint at these dimensions,
but we want it done properly
rather than waved away, and we want to know what would make it binding.

**(e) Screening.** Debye screening sets a *decay length*, not a cutoff
— force falls off
exponentially and remains finite well beyond λ_D. The questions are
how much force survives at the
working gap, whether mean-field Poisson-Boltzmann is adequate for
divalent Mg²⁺ in this confined
geometry with Manning condensation on the origami, and where explicit
ions become necessary.

**(f) Structural survival.** Does the field and force that produce
useful stroke also pull the
origami apart? Literature anchors: reversible isomerisation in the
10–35 pN band, irreversible
disassembly 35–60 pN for one jointed structure; spermidine-folded
origami survive high-field pulses
where conventional-buffer origami do not. Our force target sits above
those thresholds, so the
answer depends on how load distributes across the structure rather
than on the total.

**(g) Does the tile stay flat?** We have been treating the origami
tile as a rigid plate that
translates. It is a finite-stiffness structure under a distributed
load, supported by a compliant
layer and anchored at discrete points, so it may instead deform —
dishing into the polymer rather
than displacing as a body. We want the deflected shape under the
actuation load compared against
the rigid-plate assumption, and the consequences for force transfer to
the lever and for what an
adjacent charge sensor would see. If the two are comparable in
magnitude, the rigid-plate picture
has to go.

---

## 5. The loop

This is the process we run, and the part we most want to see an AI
scientist execute. We use **GPD**
— Formulate → Plan → Execute → Verify. Public repo:
`github.com/NanoDynamicsInst/simulation-task-map`.

**Formulate.** Restate the question as a numeric target with a
falsifiable acceptance predicate.
Lock units (SI; forces in pN; energies in k_BT and eV). State
temperature and medium explicitly.
Fix geometry and sign conventions before deriving. Output is a task
with an ID, a verification type
(in-silico / instrumental / logical), and the acceptance string.

**Plan.** Choose a method and justify the choice against cost. Say in
advance what result would
falsify the approach. Where a cheaper method can bound the answer, run
it first and use the
expensive one to calibrate.

**Execute.** Run it. Log every parameter. Emit machine-readable results.

**Verify.** Five gates, applied to every result at every level of theory:

1. **Dimensional consistency** — energy/length reduces to force;
energy/op reduces to joules.
2. **Limiting cases** — free vs clamped; dilute vs concentrated; over-
vs under-damped.
3. **Symmetry and conservation** — equipartition (σ² = k_BT/k),
fluctuation-dissipation, charge and
   energy conservation.
4. **Numerical convergence** — mesh, timestep, sampling adequacy,
statistical power.
5. **Literature cross-check** — with the premises of any invoked
scaling law checked against the
   actual material rather than assumed.

**File and iterate.** A verified result is recorded as a claim with
its provenance and acceptance
verdict. A result that contradicts a standing claim raises a formal
challenge with methodological
grounds, rather than silently overwriting it. "PASS" means
model-consistent and traceable — not
empirically demonstrated. We are at TRL 1–3 and the loop needs to keep
saying so. The loop closes by
feeding verified results back into Formulate as tightened predicates,
or by killing the branch.

---

## 6. Tasks

Acceptance predicates only. Method is yours to choose and justify —
that choice is part of what we're
evaluating. IDs map to `knowledge/program_tasks_feynman_path.csv` in
the public repo.

| # | Task | Acceptance | ID |
|---|---|---|---|
| 1 | Stiffness of the polymer layer under the tile, derived from §3
parameters | Number with stated model, parameters, and validity range;
sensitivity to grafting density reported | A2.1 |
| 2 | Feasible design window in (grafting density, height, chemistry)
| Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of
emptiness naming the binding constraint | A2.1 |
| 3 | Stroke and blocking force vs bias, including ionic screening |
Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration that it
is unreachable | A2.2 |
| 4 | Electrostatic softening and pull-in: does k_eff = k_brush + k_es
reach zero anywhere in the working range? | Either a maximum usable
bias with margin to the operating point, or a demonstration that the
osmotic divergence removes the instability | new |
| 5 | Load distribution across the origami under the modelled
electrostatic load | Peak per-load-path force reported against the
35–60 pN disassembly band, distributed and concentrated attachment
treated separately | A1.2 |
| 5b | Deflected shape of the tile under the actuation load |
Deformation amplitude reported against the stroke; rigid-plate
assumption upheld or rejected, with consequences for force transfer
and sensing | A8.2 |
| 6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ in this
geometry | Quantified deviation from mean-field, with the boundary
stated | A7.4 |
| 7 | Poroelastic drainage time vs thickness and volume fraction |
Bounded, with the conditions under which it would constrain ≥1 kHz
operation stated | new |
| 8 | Tile positional variance at 300 K | σ_RMS ≤ 3.0 nm for the
nominal Gen-1 tile | A1.2 |

Tasks 1 and 2 are the ones to start with. Task 1 is cheap and
everything downstream depends on it.
Task 2 is where the programme actually turns: if the window is empty,
we want to know now rather
than after a year at the bench.

---

## 7. What we'd count as the loop working

Process, not answers:

- Numbers that were inherited get re-derived rather than cited.
- Premises behind invoked scaling laws are checked against the
material, not assumed from the textbook.
- Method choice is justified against cost, and the cheap bound is run
before the expensive calculation.
- Results carry their validity range, and the range is respected downstream.
- Disagreement with a prior result is raised as a challenge with
grounds, not an overwrite.
- The distinction between model-consistent and measured is maintained
without being prompted.
- Where a question can't be answered with the available methods, that
is stated plainly instead of
  being answered anyway.

---

### Reference material

- `github.com/NanoDynamicsInst/simulation-task-map` — GPD-formatted
task map, V&V matrix, acceptance
  predicates, TRL and dependency tracking (Apache-2.0)
