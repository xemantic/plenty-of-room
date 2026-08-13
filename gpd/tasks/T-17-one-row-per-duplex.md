# T-17 — "One attachment row per duplex" as an output-coupling scheme: what the exact zero costs, and what breaks it

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` (the anchoring scheme it prices) and `A1.1` (the lateral and yaw bounds the same anchors also carry) |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage, loaded through `C-0022`'s *solved* electrostatic edge profile instead of a uniform pressure, with the continuum plate run beside it) **+ logical** (a rigid-tile cut-equilibrium identity that gives the restored force in closed form before any matrix is assembled) |
| **Raised by** | [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (*"the zero-crossover-force result for one row per duplex is exact only for a uniform load and a uniform foundation … any load non-uniformity, thermal excitation or attachment-stiffness scatter restores a finite crossover force in proportion to the non-uniformity"*) and by [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) open question 4 (*"the exact-zero per-path crossover force is fragile … `T-17` costs that; `T-3b` would supply the non-uniformity"*) |
| **Consumes** | [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (the lattice as re-parameterised by the layout, the 3 × 15 grid, the exact zero, the phase machinery), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) (the grillage, the concentration factor, the 56 crossovers), [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) (the **solved** lateral load profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the plate, the assumed taper, the allowables), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the 33.333 pN/nm mandate, `K2`, the lateral/yaw by-products), [`C-0023`](../claims/C-0023-two-sided-coupling.md) (the two-sided element and the path-count-from-the-allowable rule), [`C-0024`](../claims/C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the **length-dependent** joint allowable), [`C-0014`](../claims/C-0014-lateral-confinement.md) (the lateral and yaw bounds), [`C-0010`](../claims/C-0010-tile-positional-variance.md) (the thermal excitation) |

---

## Formulate

### The scheme, stated exactly

`C-0015` found that a rectangular attachment grid of **fifteen rows on a fifteen-duplex tile** — one
attachment row per duplex — makes the peak per-load-path **crossover** force *exactly* zero under a
uniform load, at every column count from 1 to 15, to the `1e−9 pN` reporting floor. Every beam then
carries the identical load at the identical stations, so no interface transmits anything. It is the
same symmetry that makes a uniform load on a uniform Winkler foundation produce no dishing at all.

**The first thing to settle is whether this is a *different* scheme from the one the programme has
committed to.** `C-0015`'s flatness answer is **45 attachments as 3 × 15** and `C-0017`'s `K2` and
`C-0023`'s `E3`/`E5` are all built on it. `3 × 15` is *three columns × fifteen rows* — i.e. it **is**
one attachment row per duplex, with three stations along each row. The task's own framing ("15 rows
versus 45 attachments is a different grid") is therefore a hypothesis to be **tested in code**, not a
premise: the test is whether the fifteen rows of `attachmentGrid(3, 15, 40, 40.35)` land on the fifteen
duplex axes `beamY`, exactly.

### The geometry and sign conventions, restated rather than inherited

- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
- `w` is positive **downward**, compressing the polymer layer — `T-5`'s convention, unchanged.
- The footprint is `40.0 × 40.35 nm`: 15 duplexes at the SAXS-measured `d = 2.69 nm`.
- A **crossover force** is the transverse force one crossover transmits between the two duplexes it
  joins, signed as in `C-0009`; the reported quantity is `max |verticalForce|` over all crossovers.
- The **load** is a downward pressure field `q(x, y)` with interior value `q₀ = 100 pN / 1600 nm²`,
  which is `C-0022`'s own convention for the dishing it computes on `C-0006`'s plate.
- The **coupling** is `n` discrete springs to ground of total stiffness `k_c = 33.333 pN/nm`
  (`C-0017`'s mandate), one per attachment, placed on the grid.
- The **foundation** is `C-0001`'s secant `k_f`, swept ×`[0.25, 4]` per `CH-0001`.

### The question, in one line

> **Is the exact zero worth anything once the load is the one `T-3b` actually solved, and does it
> survive the two other duties the same attachments have to discharge?**

### The acceptance predicate

Declared before the code, falsifiable both ways.

> **`P1` (the grid).** State, from the code and not from prose, whether `C-0015`'s 3 × 15 grid **is**
> one attachment row per duplex. **PASS** if the fifteen rows are shown to coincide with `beamY` to
> machine precision, or if they are shown not to and the two schemes are then costed separately.
>
> **`P2` (the fragility, in the currency the programme measured).** Report the peak per-load-path
> crossover force restored by (i) `C-0022`'s **solved** edge profile at every one of its operating
> states, (ii) `C-0006`'s **assumed** taper, and (iii) attachment-stiffness scatter, each as an
> absolute pN and as a fraction of the same scheme's per-path **static** share `100/n`. **PASS** if
> every source is quantified and the binding one named. Falsified as an *approach* if the restored
> force is not linear in the non-uniformity, because the whole "restores it in proportion" statement
> then has no meaning.
>
> **`P3` (the cheap bound).** A closed-form rigid-tile cut-equilibrium identity for the restored
> interface force is derived and evaluated **before** any lattice solve, and the lattice is graded
> against it. **PASS** if the identity is exact in the rigid limit (asserted as a test) and the
> lattice's departure from it is reported as a number rather than assumed small.
>
> **`P4` (the allowables, corrected).** Every per-path force is judged against `CH-0029`'s
> **length-dependent** shear allowable (18.8 pN at 8 bp, 34.8 at 16, 47.1 at 30) and against the
> 10–15 pN unzip band — never against the flat 48 pN. **PASS** if the count and the allowable at which
> the scheme crosses are both reported.
>
> **`P5` (one scheme or three).** Report whether the same grid discharges output coupling, `T-12`'s
> lateral confinement and `T-13`'s hold-down, by reproducing `C-0017`'s lateral and yaw by-products
> on **this** grid and reporting how they move as the column count changes. **PASS** either way; the
> finding is which duty sets the column count and which sets the row count.
>
> **`P6` (the other path).** `C-0015` shows the crossover and the duplex optima are at opposite
> corners of the unit cell. Report the peak **duplex** shear the scheme puts into the sheet, so that
> a scheme which zeroes one path and loads the other is not reported as free.

**Locked units.** nm, pN, pN/nm (= 1 mN/m exactly), pN·nm² , pN/nm² (= 1 MPa exactly), pN·nm/rad,
`k_BT = 4.142 pN·nm` at `T = 300 K` in aqueous buffer with Mg²⁺.

**Maturity.** TRL 1–3. `PASS` means model-consistent and traceable. **Nothing here is measured.**

---

## Plan

### The cheap bound, which runs first and is the whole of `P3`

Take the tile **rigid** and the coupling one equal spring per attachment on a one-row-per-duplex grid.
Then every duplex's foundation reaction and every duplex's coupling reaction are equal, and the
equilibrium of everything above the cut between duplex `j` and `j+1` gives the transverse force
crossing that interface in closed form:

&nbsp;&nbsp;&nbsp;&nbsp;**`V_j = Σ_{i>j} (Q_i − Q̄)`**, with `Q_i` the load on duplex `i`'s tributary strip and `Q̄` the mean.

Three things fall straight out of it and cost one quadrature each:

1. **A load that varies only in `x` restores nothing at all**, because `Q_i` is then the same for every
   duplex. So only the *across-helix* content of the edge profile can break the zero — the two rim
   duplexes and their neighbours — and the collar along the `x` rims is common mode.
2. `V_j` is **exactly linear** in the collar depth, so the whole `P2` sweep is one number times a
   depth, which is what "restores it in proportion" means quantitatively.
3. The peak per crossover is at least `V_j` divided by the crossovers on that interface, which is
   `3` or `4` on a 40 nm tile — so the concentration factor the lattice adds is bounded below by 1
   and measured rather than assumed.

**Justification against cost.** The expensive alternative — re-solving the 2-D Poisson-Boltzmann field
around the tile for each grid — buys nothing: `C-0022` has already solved it, its result file carries
the `(depth, width)` reduction and `C-0006` demonstrated the structural response **exactly linear** in
the depth. Re-solving the field would move the third significant figure of a quantity whose own
mean-field error is 123–214 % (`C-0005`). The lattice solve is run because the *concentration factor*
— peak over cut average — is exactly what a continuum cannot produce (`C-0009`), and that is the only
thing the cheap bound cannot give.

### Method

- **The lattice is `C-0009`/`C-0015`'s `OrigamiGrillage`, unmodified.** No third lattice is built. The
  `structure` and `anchoring` packages are read and not edited.
- **The load** is `C-0022`'s solved `(depth, width)` collar plus its rim residual, read from
  `gpd/results/T-3b-tile-edge-load-profile.json` at run time rather than transcribed, superposed as
  raised cosines exactly as `C-0022`'s own `dishingCase` does. The one thing that must be written here
  is a collar field admitting a **negative** depth, because `structure`'s `edgeTaperedPressure`
  requires `depth ∈ [0, 1]` and the solved edge effect is an *enhancement*. It is asserted equal to
  `edgeTaperedPressure` wherever both are defined.
- **The design space is swept over shapes, not counts** (`C-0015`'s discipline): every
  `(columns × rows)` grid on the 15 × 15 rectangle, plus the equal-count contrasts
  `3 × 15` / `5 × 9` / `9 × 5` / `15 × 3`, which have the same 45 attachments and the same 2.22 pN
  static share and differ only in registration.
- **The continuum plate runs beside the lattice** on the same load and the same supports, and the
  excess is quoted — `CLAUDE.md`'s standing rule.
- **The duplex forces are integrated over the tributary strip**, never sampled on the axis.
- **Nested mesh refinements only**, `1 ⊂ 2 ⊂ 4`.
- **Thermal excitation** is taken as the exact equipartition variance of the crossover-force
  functional, `Var(F) = k_link² · k_BT · cᵀK⁻¹c`, evaluated by one forward substitution per crossover.

### What would falsify this approach

1. **The 3 × 15 grid turning out not to be one row per duplex.** Then `P1` splits the task in two and
   the comparison the coordinator asked for is a real one rather than an identity.
2. **The restored force not being linear in the collar depth.** The lattice is linear, so this would
   mean the load model or the support model is not — a bug, not a finding.
3. **The rigid-tile identity failing in the rigid limit.** Asserted as a test at a foundation stiffness
   swept over decades; if it fails, the cut-equilibrium reading of the lattice is wrong and every
   interface number here is wrong with it.
4. **The restored force exceeding the per-path static share.** Then the exact zero is not a small
   correction to the scheme but the scheme's binding constraint, and the design moves.
5. **The thermal crossover force failing to converge in the link penalty.** `C-0009` chose the link
   stiffness as a penalty whose value *the answer must not depend on*, and gate 4 of `C-0009` shows the
   **static** force has stopped moving by `1e4 pN/nm`. If the *thermal* force has not, the two are not
   the same kind of quantity and that is a result rather than a defect — but it must then be reported
   as one and the quantity re-posed.
6. **The lateral or yaw by-product falling below `C-0014`'s bounds at some column count.** Then the
   three duties do not want one scheme after all and the column count is contested.

---

## Verify — what was actually executed

The five gates, as **22 gate-named tests** in `src/test/kotlin/coupling/UniformityBudgetTest.kt`.
Run: `./gradlew test -PbuildDirectory=build-t17` (22/22 green) and `tools/verify.sh`
(**931 tests in the suite, 0 failures**). The result file was produced through `tools/study.sh`
and diffed **byte-for-byte identical** on two independent pairs of runs.

| gate | what was checked, and the number it returned | outcome |
|---|---|---|
| **1 — dimensional** | the tributary strip loads sum to the footprint integral of the pressure to `1e−12`; a uniform pressure puts `q·L_x·d` on every strip; **the reconstructed crossover-force functional dotted into a solved field reproduces the lattice's own `verticalForce`** — at a zero load case (**absolutely**, `< 1e−9 pN`, because both are meant to be zero and a relative test would compare their noise) and at a non-zero one (`< 1e−9` relative); a zero-width, non-finite or zero-footprint collar throws | **PASS** |
| **2 — limiting cases** | a zero-depth collar is the uniform field exactly; **the collar field equals `structure.edgeTaperedPressure` at all 1681 sample points wherever both are defined**; a depth above one reverses the load at the rim, as `C-0022` reports and `edgeTaperedPressure` cannot represent; a uniform load restores exactly zero on **every** one-row grid at 1, 2, 3, 5, 8 and 15 columns; a load varying only along the helices restores exactly zero *while the duplexes bend*; a grid whose rows are not one per duplex restores a finite force under the same uniform load; **the 3 × 15 grid's rows are the duplex axes to `1e−12`** | **PASS** |
| **3 — symmetry and conservation** | the crossover forces on one interface sum to the lattice's own `shearAcrossInterface` at all 14 interfaces (`< 1 %` of the peak); **the restored force is exactly linear in the collar depth** (`5.000×` at 5× the depth, to `1e−6`) and reverses with its sign; **the rigid-tile identity is recovered as the SHEET stiffens** — monotone over `×1 → ×10⁶` in `EI`, `GJ`, `k_θ`, landing within 1 % | **PASS** |
| **4 — numerical convergence** | **nested** subdivisions `1 ⊂ 2 ⊂ 4` (450/855/1665 dof): `2.1e−3` then `4.0e−5`; the link penalty `10³/10⁴/10⁵` on the **static** force: `1.9e−3` then `1.7e−4`; the strip quadrature refined `12/24/48` panels, tightening monotonically to `< 1e−6`; **and the THERMAL force asserted NOT to converge** — `√10` per decade to 5 %, the executable form of the fifth falsifier; the 32 base-pair column phases swept at the design point, worth **3.9 %** | **PASS** |
| **5 — literature and upstream cross-check** | `C-0015`'s exact zero reproduced under its **own** point-load case at `7.8e−11 pN`; `C-0017`'s yaw stiffness 8206 against 8205 (`1.5e−4`) and its mean squared radius 253.59 against 253.55; `CH-0029`'s ladder reproduced at 8 bp (18.796 against 18.80) and 30 bp (47.107 against 47.11) through `structure.ShearJointAllowable`; `C-0022`'s total force gain reproduced at **+14.61 % against its +14.71 %** (`6.5e−3`) through a different integration of the same fitted collar; `C-0006`'s free-tile stroke reproduced at 4.907 nm against 4.95 | **PASS** |

### Which declared falsifiers fired

**Falsifier 5 fired exactly as written** — the thermal crossover force does not converge in the link penalty,
it grows as `√k_link` with `peak/√(k_BT k_link) = 1.0000`. The Plan said in advance that this "must then be
reported as one and the quantity re-posed", and it is: [`CH-0033`](../challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md).

**Falsifier 3 fired once and was a mistake in the test, not in the model**: the rigid limit was first taken by
stiffening the *foundation*, which makes the tile **conform** to the load rather than rigidify. Taken on the
sheet it passes monotonically. Recorded because it is the kind of error a green test would have hidden.

**A sixth, undeclared falsifier fired**: under `C-0022`'s solved load **no attachment count is flat** — the
dishing saturates at 0.149 of the stroke between 45 and 225 attachments. That is
[`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md).

Falsifiers 1, 2, 4 and 6 did not fire.

## The outcome

Filed as [`C-0026`](../claims/C-0026-one-row-per-duplex.md). **PASS on all six clauses.** The branch is **not**
killed: the 3 × 15 grid remains the design, and what is retired is the *status* of the exact zero — from an
exact structural property to a **20.2×** design margin against the worst equal-count shape, which a few per
cent of attachment scatter spends and which still leaves the crossover path 12–67× clear of every allowable.
