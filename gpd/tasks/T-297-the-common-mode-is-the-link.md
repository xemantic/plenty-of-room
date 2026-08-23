# T-297 — the crossover's common mode is the vertical LINK, and the lattice sits at the stiff end of it

**Leaf** `A8.2`.
**Raised by** [`CH-0242`](../challenges/CH-0242-the-tie-carries-no-common-mode-stiffness.md),
through [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`).
**Against** [`C-0154`](../claims/C-0154-honeycomb-grillage.md)'s element set.

## Formulate

`CH-0242` expands `turnPhosphateSpan` about the line of centres, finds a quadratic form of rank
**two** in the two backbone azimuths, observes that `HoneycombGrillage`'s bond and tie carry
`½ k_θ(Φ_u − Φ_l)²` — the **relative** eigenmode — and concludes that the **common** one, which it
prices at `3.52810239×` the relative, is missing at all 435 bonds and all 59 ties.

The question this task answers is not *"what is `k_common`"*. It is **which coordinate of the model
the common mode already lives on, and what the model's own value for it is** — because a stiffness
that is absent and a stiffness that is over-carried are different defects with opposite signs, and
`C-0190` §6's whole twist magnitude is quoted as a threshold on the strength of the first reading.

### Numeric target

1. The exact ratio `1 + 2 r_P/(d − 2 r_P)` at `d = 2.536 nm` and `T-71`'s measured
   `r_P = 0.9086378584708424 nm`, re-derived rather than inherited.
2. The model's **own** common-mode azimuthal stiffness, in `pN·nm/rad`, read off the assembled
   stiffness matrix rather than argued from the source.
3. The link stiffness the same span law implies, `k_R`, against `RIGID_LINK_STIFFNESS = 1e4 pN/nm`.
4. The free tile's peak dishing over the stroke on the tied `10 × 6` block, swept over
   `linkStiffness` between those two ends.
5. The departure applied as a **link** eigenstrain, and the two coupled cells `C-0190` §6 quotes
   its threshold on, re-read at that load.

### Falsifiable acceptance predicate

`P1` — **the link residual is a function of `Φ_u + Φ_l`**, asserted on the assembled matrix:
a field with `Φ_a = Φ_b = θ`, every other coordinate zero, stores **zero** hinge energy and
**non-zero** link energy, and the link energy equals `½ k_link (d·unitY·θ)²` in closed form.

`P2` — **`d/2` is the only arm that annihilates the linearised rigid roll.** The field
`Φ ≡ α`, `W = −α y`, `Θ = U = 0` stores exactly zero hinge, link and slip energy; and the same
field against an arm `a ≠ d/2` stores a non-zero link energy, so the arm is forced.

`P3` — **the model's common-mode azimuthal stiffness exceeds `CH-0242`'s physical one.** If it
does not, the challenge stands as written and this task's thesis is wrong.

`P4` — **the free tile's dishing over the whole `linkStiffness` sweep is quoted with a departure**,
and `C-0175` §9's `0.0446459684` is reproduced at the standing `1e4` to `1e-9`.

`P5` — **the link eigenstrain's projection on the relative-roll coordinate is exactly zero** at
every tie — the mirror of `C-0190`'s `F2`, which found the relative prestrain's projection on the
common mode to be exactly zero.

`P6` — **`linkStiffness → ∞` converges** for the eigenstrain field: the constrained limit exists,
so the answer is penalty-independent at the stiff end and the two ends of the sweep are a genuine
bracket rather than two models.

### Verification type

**logical** — the element decomposition, the frame-indifference argument and the exact identity
`d²/(2 g r_P) − d/(2 r_P) = d/g` are algebra on the corpus's own `turnPhosphateSpan` and on the
committed source of `HoneycombGrillage.assemble`; **plus in-silico** — the assembled matrix probed
directly, the free tile swept, and the eigenstrain graded through `C-0058`'s exact Woodbury
surrogate on `C-0167`'s stations and `C-0087`'s dropout stream.

### Locked units

Lengths nm; forces pN; energies pN·nm and `k_BT = 4.141947 pN·nm` at 300 K; rotational stiffness
pN·nm/rad; translational stiffness pN/nm; angles **degrees at every API, radians only where a
lattice is loaded**; dishing dimensionless, as a fraction of the closed-form free stroke.

### Geometry and sign conventions

`s` along the helices, `y` across them in the plane of the face, `z` through the block's thickness;
`W` positive **downward**, toward the electrode (`C-0006`). A roll `Φ` is about the beam's own axis,
positive so that a point at `+y` offset moves by `+Φ·y` in `W`. Honeycomb `d = 2.536 nm` (SAXS),
row pitch `3d/2`, layer pitch `d√3/2`, rise `0.34 nm/bp`. `θ_u`, `θ_l` are the two backbone azimuths
off the line of centres in the **same** rotational sense, which is `ForcedCrossoverPrice`'s
`(θ, 180° + θ)` construction; `g = d − 2 r_P` is the span at zero departure.
`Δφ = Φ_u − Φ_l`, `Σ = Φ_u + Φ_l`, `ΔW = W_u − W_l`, `R = ΔW + (d/2)·unitY·Σ`.

## Plan

### The cheap bound runs first, and it needs no solve at all

Three readings, all closed form:

1. `HoneycombGrillage.assemble` builds the link as
   `linkGradient = [1, armY, −1, armY]` over `(W_a, Φ_a, W_b, Φ_b)` with `armY = (d/2)·unitY`.
   Its residual is therefore `ΔW + armY·(Φ_a + Φ_b)` — **a function of the sum**. `CH-0242` §3's
   *"and nothing else on the azimuthal coordinates"* is checkable by reading four lines.
2. At the challenge's own configuration (both duplexes rolled by `θ`, axes at nominal) the link
   stores `½ k_link (d·unitY·θ)²`, i.e. a common-mode stiffness of `k_link d²/4` in plane. One
   multiplication says whether the model is above or below `3.52810239 k_θ`.
3. Matching the same bond tension that fixes `k_θ` — the challenge's own premise, that both
   eigenmodes are one span mechanism — gives `T = 2k_θ/r_P` and `k_R = T/g`. One division says how
   far the penalty is from it.

If (2) lands **above** the physical value, the challenge's direction is reversed and the expensive
half of the task changes from *"add a spring"* to *"measure what the penalty costs"*, which is a
sweep of an existing constructor argument and needs no new element at all.

### Then the sweep, which is the error the approximation carries

`linkStiffness` is already a `HoneycombGrillage` constructor parameter, so measuring the error
costs six solves of a lattice that exists. Re-take `C-0175` §9's free tile on the tied `10 × 6`
block at `k_R`, `1e2`, `1e3`, `1e4`, `1e5`, `1e6`, at all three couplings.

### Then the eigenstrain, which is the only new element

The departure is an **offset in the link's own residual**, `R₀ = d·unitY·(roll)`, and an offset is a
**load** — no entry of the stiffness matrix moves, the field is exactly linear in it, and
`C-0104`'s influence-bank trap is avoided structurally. One new public method on
`HoneycombGrillage`, in the shape of `beamTwistResponse` beside it, with no existing member
touched, so every existing consumer is bit-identical by construction and that is proved by
re-running them.

### Justification against cost

The expensive alternative is a new azimuthal spring element, which changes every entry of the
matrix, invalidates every influence bank in the corpus and forces a re-grade of `C-0167`'s 64
cells, `C-0175`'s ceiling and `C-0180`'s recovery. The cheap bound decides whether any of that is
owed, and it is three multiplications. The sweep then bounds the residue **between two ends of one
parameter** rather than between two models, which is what `CLAUDE.md` demands of a bracket.

### What would falsify this approach

- **`P1` fails** — the link's gradient does not carry `Φ_a + Φ_b`. Then `CH-0242` §3 is right, the
  common mode really is absent, and the element must be added.
- **`P3` fails** — the model's common-mode stiffness is *below* `CH-0242`'s. Then the challenge
  stands in direction as well as in kind.
- **`P2` fails** — some arm other than `d/2` also annihilates the rigid roll. Then the link's arm
  is a fitted parameter rather than a theorem and the whole identification is unsound.
- **`P6` fails** — the eigenstrain field does not converge as the penalty stiffens. Then the
  departure cannot be carried on the link at all and `C-0190`'s per-beam twist is the only
  formulation available, threshold and all.
- The sweep shows the free tile **moving across `T-5b`'s 0.10** between the two ends. Then the
  corpus's honeycomb flatness verdicts are penalty-dependent, which is a defect of a different and
  larger kind, and this task's deliverable becomes that statement rather than a bound.
