# Comparison of two independent runs at the NDI Gen-1 actuator problem

Analysis date: 2026-08-17.
Written by Claude (Fable 5) after reading both repositories and running both test suites.

## The two repositories

Both projects start from the **same problem definition** — the files
`third-party/2026-08-ndi-gen1-problem-definition.md` in the two repos are byte-identical
(NDI Gen-1 DNA-origami actuator: 8 simulation tasks in §6, open questions in §4,
the GPD Formulate → Plan → Execute → Verify loop in §5) — and from the same
Kotlin/JVM TDD project template (Gradle, viktor, openrndr-math, kotlin.test).

| | **p1** — [`xemantic/plenty-of-room`](https://github.com/xemantic/plenty-of-room) | **p2** — [`devrandom/plenty-of-room`](https://github.com/devrandom/plenty-of-room) |
|---|---|---|
| Agent | Claude (multi-agent, iteration loop; per its CLAUDE.md/JOURNAL, ran on a Linux box as user `claude` with passwordless sudo, up to 4+ concurrent agents on one checkout) | Codex (`.codex/config.toml` present; ran sandboxed without sudo, user-local JDK via `GRADLE_USER_HOME`; later got a Blackwell GPU VM `dr-g1` provisioned for MD) |
| Commits | 75 (HEAD 2026-08-17 14:46 UTC, "Iteration 15: four claims, seven challenges, and a propagation that did not close") | 31 (HEAD 2026-08-17 12:52 +0200, "Close problem-def completeness gaps … ROW12 + ROW13 PASS") |
| Working span | 15 recorded iterations | 2 days (2026-08-16 → 08-17) |
| Files (excl. `.git`) | 635 | 124 |
| Kotlin sources | 291 (109 test files) | 45 (~3.7k lines of main source) |
| Knowledge artifacts | `gpd/`: **74 claims, 85 challenges, 72 result JSONs**; `ANSWERS.md` (651-line synthesis), `TASKS.md`, 6,710-line `JOURNAL.md`, 527-line `CLAUDE.md` of accumulated lessons | `results/`: 20 claim/summary MDs + 22 result JSONs; `TASK-QUEUE.md` (73 lines, doubles as the claims log), 760-line `JOURNAL.md` |
| Extra tooling | `tools/verify.sh`, `snapshot.sh`, `study.sh`, literature-survey and PDB-survey Python scripts | `tools/a12md/` — OpenMM CG-Langevin and oxDNA MD drivers (Python) |
| Push status | committed locally | committed locally; push to the upstream `xemantic/plenty-of-room` blocked (the run's SSH key had no write access there) |

## Verification environment (this analysis)

Both suites were re-run on the analysis machine — **not** either project's home
environment: macOS (Darwin 25.5.0), Zulu OpenJDK 26.0.1, each project's own Gradle
wrapper 9.7.0, non-English locale (comma decimal separator — this matters below).

- **p2: `./gradlew test` → BUILD SUCCESSFUL, 94 tests, 0 failures, 0 skipped.**
- **p1: `./gradlew test` → 2014 tests, 2 failures** — both in
  `window/ResynthesisTransferTest`, both the same cause and both a **locale
  artifact of this machine, not a science regression**: a lookup key is formatted
  as `L0 = 10,0 nm` (comma decimal from the host locale) against result files
  that say `10.0`, so `ResynthesisInputs.edgeProfile` finds 0 matching T-3b
  profiles. On its home (C-locale) environment the suite would be green.
  It is a genuine portability bug, but no number is wrong.

## Verdict in one paragraph

p2's "delivered" claim is real at face value: every §6 row closed plus extra
leaves, tests green, machine-readable results, and it beat p1 to the one
deliverable p1 refused (literal oxDNA MD with confidence intervals, including a
full 15,238-nt origami on a GPU). It is the wide-and-shallow delivery: textbook
models applied once each. p1 delivered a much deeper treatment of a partly
overlapping core — and on the three tasks where the two disagree substantively
(1, 2, 5b), p1's analysis either overturns or heavily qualifies p2's PASS; p2's
rigid-plate verdict in particular fails on its own load case. The most valuable
read of the pair is as **independent replication**: where they agree, the result
now stands on two independent derivations.

## Where they agree (independent replication)

- **Task 4 (electrostatic softening / pull-in) — near-exact agreement from
  different methods.** p2: the osmotic divergence removes pull-in at L₀ = 7 nm
  (threshold λ_D* = (4/9)L₀); pull-in returns for L₀ = 10 nm at 2 mM Mg²⁺.
  p1: the unloaded tile has no pull-in at 49/54 states; pull-in binds at
  11/54 — all of them the 10 nm layer in 2 mM.
- **Task 3 (stroke and blocking force) — same verdict, same caught trap.**
  Both find 100 pN at ≤ 2 V reachable at low bias (p2 nonlinear-PB:
  0.009–0.073 V; p1: 0.065–0.699 V), and both independently caught that the
  linearised electrostatic force is an optimistic upper bound — p2 via a
  nonlinear-PB pass (force saturates at 0.02–0.12× the linear estimate at 2 V),
  p1 via voltage saturation above ~0.5 V.
- **The textbook semidilute brush is the wrong model at actuatable densities** —
  both found this by different routes. p2's Task-2 self-challenge: stroke-capable
  grafting densities are dilute, so the 9/4 osmotic law is not the controlling
  physics there. p1 went further: PEG/water at 300 K is a *marginal* solvent,
  Gen-1 chains are 0.02–0.10 of one thermal blob, the des Cloizeaux window is
  exactly empty for every Gen-1 chain, so blob arguments never apply at all.
- **Low salt is the answer — a striking convergence.** p2's coupled
  lever+FET task (its Task 10) finds the simultaneous actuator+sensor window
  only at **0.08–0.5 mM Mg²⁺**; p1's headline specification question to NDI is
  to respecify Gen-1 at **0.5 mM** rather than §3's 2 mM, where every predicate
  clears and the pull-in fold does not exist.
- **Tasks 7 and 8 verdicts:** both conclude poroelastic drainage and thermal
  positional noise are not the binding constraints.
- **Mean-field electrostatics is flagged as fragile by both**, at different
  depths: p2 shows Debye-Hückel *linearisation* fails at the operating tile
  charge (exact 2:1 Grahame potentials, ~76–88 % Manning condensation);
  p1 shows even *nonlinear* mean-field PB is uncontrolled for divalent Mg²⁺
  (one-loop correction 123–214 % of leading, Ξ ∝ q³) yet qualitatively safe at
  the working gaps.

## Where they materially disagree

### Task 5b (does the tile stay flat?) — opposite verdicts; p1 is right

p2: rigid plate **HOLDS** for the nominal tile — 0.176 nm center deflection,
from a Kirchhoff plate that is **simply supported** at its edges under a
**uniform** load. p1 proves that a free tile on a uniform compliant layer under
a uniform load dishes **exactly zero at any flexural rigidity** (w = q/k_f is an
exact rigid translation; p1 wires this in as a solver falsifier), and that the
real dishing comes from the **non-uniform solved electrostatic load** — the rim
*gains* force via a ~1.65 nm sub-Debye edge collar — which dishes the free tile
**32 % of the stroke**, meeting §4(g)'s own criterion for abandoning the
rigid-plate picture (recoverable to ~0.07 of the stroke only by a specifically
*placed* coupling, which is most of p1's later program). p2 never computed the
non-uniform load and used a boundary condition the tile does not have (its edges
are free, resting on the layer — not supported). p2's PASS therefore rests on
the one load case that cannot produce the effect being tested.

### Task 1 (layer stiffness) — ~3× apart, with a validity dispute underneath

p2: k ≈ 160 pN/nm at σ = 0.05, L₀ = 7 nm, 10 % compression, from
Alexander-de Gennes height + des Cloizeaux 9/4 pressure. p1: 47.7–64.1 pN/nm at
the working point over the tile — and, more fundamentally: a stiffness "at the
resting height" is not well posed (three of six layer models give exactly zero
there; quote it at a stated compression), and the 9/4 exponent p2's number is
built on is never licensed for Gen-1 chains (the thermal blob exceeds the chain
for every chain in the parameter range). p2's own caveat row partially concedes
the regime problem but keeps the number.

### Task 2 (design window) — different headline verdicts, partly reconcilable

p2: **EMPTY** for brush-regime actuation (binding constraint named: the
semidilute brush is too stiff, stroke-capable points are dilute; φ and k are
N-independent in the Alexander regime so no chemistry decouples them), then
rescued by electrostatic actuation on a dilute/non-osmotic layer.
p1: **NOT empty** at 7 and 10 nm (24.8× wide in σ at 10 nm), empty at 5 nm only
— because it treats the output coupling (33.333 pN/nm, fixed by §3's own
100 pN / 3 nm) as part of the device — and ultimately: a (σ, L₀) window is the
wrong object, since ten of twelve discovered constraints do not resolve in
grafting density at all; the deliverable becomes a height plus specification
questions. The two verdicts describe different device concepts, but a reader
gets "empty" from one repo and "not empty" from the other. Both agree §3's
*desired* ~10 nm stroke is unreachable on §3's own stack (p1: kinematic ceiling
9.79 nm, dead-load stroke 7.42 nm; a stroke is a compression, s < L₀
identically).

### Task 7 (drainage) — same verdict, margins four orders of magnitude apart

p2: τ ≈ 1.6 ns nominal, ~10⁵× margin over the 1 kHz budget. p1: not binding by
**22×** at the §3 worst case — using the Brinkman-corrected transmissivity
(plain Darcy overstates drainage ~5× when √k ~ h) and the slow end of a 6×
hydrodynamic-screening-length bracket that p2's single κ = ξ²/8 number does not
carry. Not verdict-changing, but if the margin ever mattered p2's version
overstates it enormously.

### Task 8 / leaf A1.2 (positional variance MD) — each side has half of it

p2 actually ran the ensemble MD with 95 % CIs — CG-Langevin (OpenMM/OpenCL on an
RX 5500 XT), reduced oxDNA lever (CPU), and the full as-designed origami
(oxDNA/CUDA on the Blackwell VM): σ_RMS ≈ 0.255 nm nominal, PASS. That is the
leaf's literal acceptance, which p1 refused on the stated ground that oxDNA does
not model the polymer layer that sets the answer (a CI on the wrong subsystem is
a category error). But p2's MD represents that layer as a harmonic spring whose
constant (k_eff = 159.55 pN/nm) comes from its own analytic Task-1/8 result — so
the headline σ is substantially the injected spring measured back; the genuine
new content is the origami body's own compliance (bare stiff lever σ ≈ 1.02 nm;
bare flexible duplex FAILS at ~6.9 nm). Meanwhile p1 found what p2's PASS is
silent about: the layer's **lateral** restoring stiffness is exactly zero by
symmetry (an untethered tile diffuses ~63 nm per 1 kHz period — 21× the
predicate), at zero bias nothing in the §3 stack confines the tile in either
direction (the vdW well is only 0.2–5.7 k_BT), a variance must be quoted with
its bandwidth (only 0.55–3.1 % of it sits below 1 kHz), and the tile's corner is
√7 noisier than its centre. p2's "thermal noise is NOT the binding constraint"
is true of the biased normal coordinate and unqualified about everything else.

### Task 5 (structural survival) — p2's bracket is coarser

p2 divides the total load by the anchor count (per-path = F/n) and compares
against the 35–60 pN band directly. p1 shows the band is a whole-cross-section
disassembly force at a stated loading rate, not a per-path allowable (per path:
duplex shear ~48–65 pN or unzip 10–15 pN, and the allowable is a function of
bonded length and loading rate); and that a rigid anchor is carried by its two
nearest crossovers and essentially nothing else, so an equal-share figure
understates the peak by 2.3–7.6×. Verdict-compatible (both find distributed
attachment safe), but p1's numbers are the defensible ones.

## What only one side has

**Unique to p1 — the buildability program.** The entire output-coupling branch:
an element catalogue, the recommended 34-instance hinge-rooted arm on the
unoccupied out-of-plane crossover azimuth (flat at 0.0706 of the stroke with
equal springs), and then the tolerance model that **defeats its own
recommendation** — the surviving margin is one lattice quantity,
p − d − L = 0.0256 nm, below one base-pair rise (13.28×), so the recommendation
stands as "best element the catalogue contains and NOT a buildable design."
Plus DNA-lattice structural analysis (crossover phase census, hinge counting
theorems, plan-view packing), primary-literature measurements (Fischer 2016
single-layer lattice width 9.1 %, Bai 2012 interhelical 18.5→36 Å sawtooth,
Strauss 2018 staple incorporation 48–95 %, a 13,084-linkage PDB backbone
survey), and **six specification questions back to NDI with thresholds**
(buffer, electrode material, superstructure perforation, tile area, which
device the desired clause names, a 17–26 nm layer as the only route to the
desired stroke). p2 stops at continuum physics; it has nothing at the
DNA-design level.

**Unique to p2 — operational reach and breadth.** The MD deliverable end to end
(including getting GPU hardware provisioned mid-run and documenting access),
plus four leaves beyond §6: closed-loop (precision, rate) Pareto vs sensor
noise (A1.4), the coupled gated-lever + FET operating window vs ionic strength
(A7.4 — the 0.08–0.5 mM result), §4(c) ion partitioning (Ogston partition
K ≈ 0.99 for the dilute brush — the field fully permeates), and the §2
solvent-quality caveats checked at the operating point (χ(300 K) ≈ 0.43,
kosmotropes negligible at mM, per-chain tension 2–5× below the 30 pN onset,
with the dilute-σ crossing flagged). Note the solvent-quality check lands close
to p1's independently measured χ = 0.372 and "weakly good/marginal" conclusion.

## §7 — the loop the client said they are actually evaluating

The problem definition states it is judging process, not answers. There p1 is in
a different class: 85 challenges filed against its own claims, corrections
recorded in place rather than overwritten (ANSWERS.md carries dated CORRECTED
blocks), under-claiming hunted as rigorously as over-claiming (a mechanised
drift checker over the synthesis), premises of every invoked scaling law checked
against the material, inherited numbers re-derived from primary sources or
measured from public data banks, and negative results carried with their query
logs. p2 honors the shape — TRL 1–3 / "model-consistent, not empirical"
language throughout, two self-raised challenges, one recorded self-correction
(the σ–L₀ coupling error in its first Task-2 pass), a clean note on the
problem-definition's A1.2 leaf-ID discrepancy, and an explicit completeness
audit that found and closed its own §4(c)/§2 gaps — but its claims mostly rest
on textbook formulas applied once, and the two places its verdicts are weakest
(Task 5b's boundary condition, Task 1's exponent) are exactly where "premises
checked against the material, not assumed from the textbook" would have caught
it.

## Bottom line

- p2 delivered breadth: every row closed, 94 tests green, real MD on real GPUs,
  fast. Trustworthy where it overlaps agreement with p1; over-confident where it
  is alone (Tasks 1, 2-headline, 5b, and the Task-7/8 margins).
- p1 delivered depth: fewer leaves beyond §6, but its results carry provenance,
  validity ranges, challenges, and a buildability verdict — and where the two
  conflict, p1's treatment wins on the physics each time it was checked here.
- The pair together is worth more than either alone: pull-in at 10 nm / 2 mM,
  low-bias reachability with a saturating force, the failure of the textbook
  brush model at actuatable densities, and the push toward ~0.5 mM buffer are
  now independently replicated results.
