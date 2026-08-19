# T-215 — which part of `T-129`'s result file is a descent manifold, and how wide

**Raised by** [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) (`T-212`), whose `F1` needed it settled.
**Claim** `C-0135`. **Challenges reserved** `CH-0162`, `CH-0163`.
**Result** `gpd/results/T-215-descent-manifold-width.json`.
**Leaf** none — this is a **process** task about the reproducibility of an emitted artifact, not about a physical quantity.
**Verification type** **logical** (a static ensemble assembled from `git`, and a structural classification of the moving fields)
**+ in-silico** (fresh re-runs of `anchoring.RangeRobustPlacementStudyKt` at `HEAD`, and a unit measurement of the optimiser's own degeneracy).

## The question, stated so that it can come back either way

`gpd/results/T-129-range-robust-placement.json` was reported by `C-0131` as **not reproducible from `HEAD`'s own code**:
three independent runs agreed on the whole `ranges[1]` block to the last digit and the committed file disagreed with all three by up to **0.60 %**,
while two runs of *identical* code left **7 `subsets[*].minimaxWorstOverStroke` fields at ≤ `8.6e−4`**.

The deliverable is **either/or**, and the second branch is a perfectly good answer:

- a re-run that **reproduces** the committed file, **or**
- a recorded statement of **which part** of it is a descent manifold and **how wide**.

The question `C-0131` did not ask, and this task must answer **explicitly**:

> Is the 0.60 % the **same phenomenon** as the ≤ `8.6e−4` — one descent landing on a different member of one optimal set,
> read on a different functional — or is it **something else**: an upstream input change, a library change, or a defect?

## Locked units and conventions

Nothing physical is computed. Units unchanged: nm, pN, pN/nm, pressure in pN/nm² = 1 MPa exactly,
`k_BT = 4.141947 pN·nm` at 300 K in aqueous buffer with stated Mg²⁺.
A **dishing** is `T-5b`'s peak-to-mean deflection over the stroke, dimensionless.

Two words are used throughout in `CLAUDE.md`'s sense and they are the whole finding:

- the **VALUE** is the objective a minimax descent reports — `minimaxWorstOverStroke`;
- the **POINT** is the argmin it reports it at — visible in this file only through functions of `max_i k_i`:
  `minimaxPeakRatio`, `peakPathStiffness`, `peakPathForceAtAcceptableStroke`, `peakThermalForce`.

**A width is quoted as a relative spread over an ensemble**, `(max − min)/max|·|`, and every member of the ensemble is named.

## Plan — the cheap bound runs first, and here it is nearly the whole answer

### Cheap bound 1 — `git log`, before any solve

`gpd/results/T-129-range-robust-placement.json` has **three** committed versions: `cf7de13` (iteration 13),
`d1ff95e` (iteration 28), `ce11aaf` (iteration 30). Between them the study source changed **twice** and both changes are provably non-numeric:

- iteration 28: a `+`-binds-tighter-than-`.format()` repair inside one `settles` **prose** field;
- iteration 30: the `digitsByKey = DEPARTURE_DIGITS_BY_KEY` argument at the **serialisation boundary**.

Neither can move a computed quantity. So **the three committed versions are a free three-member ensemble of the same computation**,
and the width can be measured with `git show` and no run at all.

### Cheap bound 2 — the inputs and the libraries

The study reads exactly three files: `T-3b`, `T-125`, `T-108`.
`T-3b` and `T-108` have not been committed since long before iteration 28; `T-125` moved at iteration 30 in **departure fields only**.
The seven library declarations the study imports for the descent (`minimaxStiffnessDistribution`, `MultiStateSurrogate`,
`normalisedStiffnesses`, `perPathStiffnessCeiling`, `rimStiffenedWeights`, `admissibleStiffnessRatio`, `perPathThermalForces`)
live in `coupling/RobustDistribution.kt` and `coupling/NonUniformCoupling.kt`, **neither of which changed between iterations 28 and 30**.
So an input change and a library change are both **excluded before any solve**, and the alternative to the manifold explanation is a defect.

### Cheap bound 3 — the structure of the disagreement

**A closed-form field that moved would falsify the manifold explanation immediately**, so the classification runs before the re-run:
partition every field that varies over the ensemble into (a) a descent VALUE, (b) a function of a descent POINT, (c) anything else.
Category (c) non-empty is the falsifier.

### The measurements

1. **Ensemble width from `git`** — the three committed versions, field by field.
2. **Two fresh runs at `HEAD`**, in one owned snapshot, separate JVMs — does the *currently* committed file reproduce, and what is the run-to-run width now.
3. **The mechanism, as a unit measurement** — `minimaxStiffnessDistribution` on a small `multiStateSurrogate`, started from a point and from
   the same point perturbed at the **last ulp**: measure the VALUE spread and the POINT spread and their ratio.
   This is what turns *"a manifold"* from an explanation into a measured property of the optimiser, and it costs milliseconds.

**Method justified against cost.** The whole classification and the three-member width are `git show` plus one pass of arithmetic —
minutes, against ~15 minutes per fresh run of the study. The fresh runs are still owed, because the question *"does the committed file
reproduce"* is about the file at `HEAD`, which is a **different file** from the one `C-0131` compared against.

## Acceptance predicates

- **P1.** The cheap bound is published **before** any re-run: the git history of the study, its libraries and its three inputs,
  and a statement of what each excludes.
- **P2.** Every field that varies over the ensemble is classified as VALUE / POINT / other, and the **other** bucket is reported with its count.
- **P3.** The width is quoted **twice** — once on the VALUE and once on the POINT — with the ratio between them.
- **P4.** Every **verdict** field of the file (booleans, `bindingStates`, the acceptance predicates) is checked across the ensemble,
  and the claim states whether any published verdict depends on the unstable part.
- **P5.** Two fresh runs at `HEAD` are diffed against each other and against the committed file, and the outcome is reported
  whichever way it comes out.
- **P6.** The optimiser's degeneracy is measured directly, not inferred.

## Falsifiers — declared before the runs

- **`F1`** — **a field outside the two descent blocks moves.** Any varying field that is not a `minimaxWorstOverStroke`,
  a function of a descent argmin, or a deliberate rounding change **falsifies the manifold explanation** and makes this a defect hunt.
- **`F2`** — **the POINT is no wider than the VALUE.** If `spread(peak ratio) ≤ spread(objective)` then what moves is the answer and not
  the place it was found, and `CLAUDE.md`'s manifold entry does not describe this file.
- **`F3`** — **a verdict moves.** Any boolean, any `bindingStates` list, or any acceptance verdict differing across the ensemble
  means the irreproducibility is not cosmetic and `T-129`'s claim must be amended.
- **`F4`** — **the two fresh runs disagree by more than the historical ensemble does.** That would mean the width measured from `git`
  is not a ceiling and the ensemble is not representative.
- **`F5`** — **the ulp-perturbation measurement finds no degeneracy.** If perturbing a start at the last ulp moves neither the value nor the
  point, the optimiser is locally unique and the file's movement has another cause.

## What would falsify the approach itself

If the three committed versions turn out to differ because of a code change that *is* numeric — anything in the diff between
`cf7de13`, `d1ff95e` and `ce11aaf` that reaches an arithmetic path — then the ensemble is not an ensemble of one computation
and every width quoted from it is meaningless. The diffs are read in full for exactly this reason.

---

## What was actually run (appended after execution)

The plan asked for **two** fresh runs; six were drawn, in two owned snapshots, strictly sequentially —
because the first attempt started two against **one** snapshot, which write the same `gpd/results/` path
and overwrite each other, and a copy taken from a snapshot while its run is still in flight returns the
snapshot's own *input* copy (byte-identical to the committed file, and it reads as a perfect reproduction).
Both mistakes are now `CLAUDE.md` entries and the driver that avoids them is [`tools/T-215-ensemble.sh`](../../tools/T-215-ensemble.sh).

A **seventh** run was added that the plan did not contain and that turned out to be the decisive one:
`git archive cf7de13 | tar -x` into a fresh snapshot, run unmodified.
It is the only experiment that can distinguish *"the tree changed"* from *"the descent draws"*, and no
amount of re-running `HEAD` substitutes for it.

`P5`'s *"whichever way it comes out"* came out both ways: five of six fresh runs reproduce the committed
`ranges[1]` and one (`B2`) does not. **`F4` did not fire** — the fresh runs disagree by exactly the width
the three committed versions already showed, and by no more.

`P1`–`P6` are met and are recorded in `gpd/results/T-215-descent-manifold-width.json`;
`F1`, `F2`, `F3` and `F4` did not fire and **`F5` did**.
