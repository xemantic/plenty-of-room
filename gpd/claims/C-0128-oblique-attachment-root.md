# C-0128 — **The cost of an oblique attachment root is ONE anisotropy, and the corpus has three answers for it, none of which spends `C-0118`'s flatness.** `κ(ψ) = cos²ψ + sin²ψ·A` with `A = k_radial/k_tangential`, so at the honeycomb's own **60°** the cost is `0.25 + 0.75 A` — **exactly 1.000 for a flexible tie** (an isotropy *symmetry*, not a small number), **6.017× for a crossover-hinged rigid body**, and **NOT REPRESENTABLE** as a ratio under this corpus's own reading of a covalent link as a *constraint*. The **absolute** oblique stiffness survives that boundary — **10.753 pN/nm** against **11.220 pN/nm**, 1.043× apart — and re-graded with the alternation the lattice imposes, **4 of 4 `10 × 6` cells stay flat at the 90th percentile**, the paired cost of the alternation being **0.04–0.56 %**

> **Annotated, iteration 34 — see the iteration-33 annotation in §1 below ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **The angle is 30°, not 60°, and THERE IS NO PERPENDICULAR ROOT ANYWHERE ON A HONEYCOMB FACE** —
> so this claim's `k_z(60°)`, its `±60°` pair, and every comparison here against a *perpendicular* root describe a half-row termination the published designs do not have.
> `κ(30°) = 0.75 + 0.25 A` against `κ(60°) = 0.25 + 0.75 A`, so the rigid-body oblique cost **falls from 6.017× to 2.67233333×**:
> **the correction is favourable and this claim's verdict — that the azimuth does not spend `C-0118`'s flatness — is strengthened.**

| | |
|---|---|
| **Task** | [`T-206`](../tasks/T-206-oblique-root.md) — what does an oblique attachment root cost against a perpendicular one? |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (a closed-form decomposition on the corpus's own joint constants) **+ in-silico** (the re-grading of `C-0118`'s cells, paired on one dropout stream) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Verdict** | **PASS on all four predicates. Neither falsifier fired.** `P1`: the cost is delivered as a number **with the root model it was read on**, and the one branch that has no number is declared `NOT REPRESENTABLE` with what it would take (`T-9`). `P2`: `ψ = 0` reproduces the perpendicular root exactly and `ψ = 90°` is the pure in-plane case, both asserted as tests. `P3`: `C-0118`'s flat cells survive. `P4`: the cheap bound was stated before any constant was read and it settled the shape of the whole answer. |
| **Provenance** | [`gpd/results/T-206-oblique-root.json`](../results/T-206-oblique-root.json), produced by `tile.ObliqueRootStudyKt`; model [`tile/ObliqueRoot.kt`](../../src/main/kotlin/tile/ObliqueRoot.kt), tests [`tile/ObliqueRootTest.kt`](../../src/test/kotlin/tile/ObliqueRootTest.kt) (23, written first and watched to fail). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb at 10.5 bp/turn, `d` = 2.536 nm, 112 bp span; `C-0009`'s `k_θ` and `C-0020`'s **derived** in-plane link; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0017`'s mandate; `C-0087`'s measured dropout, 4 000 realisations, **seed 197197 — `C-0118`'s own**, so the equal-spring rows are a bit-exact reproduction and the alternating rows share their stream. |
| **Consumes** | [`C-0122`](C-0122-honeycomb-station-lattice.md) (the census that raised this), [`C-0119`](C-0119-honeycomb-raster-width.md) (the primary honeycomb rules), [`C-0118`](C-0118-coupled-four-layer.md) (the cells re-graded), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0009`](C-0009-discrete-lattice-tile.md) / [`C-0020`](C-0020-in-plane-shear-lag.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md) |
| **Constrains** | **`C-0118`'s flatness is not spent by the azimuth.** One challenge is raised — [`CH-0151`](../challenges/CH-0151-an-oblique-helix-has-two-free-azimuths-not-one.md), against `C-0122`'s station **count**, which the same geometry raises by 1.47–1.50×. `CH-0152` was reserved and is **not used**: no second contradiction was found. |

---

## 1. The cheap bound, stated before any constant was read

A root's translational stiffness is a symmetric tensor, diagonal in its own two axes — the
**radial** one along its own azimuth (the direction a crossover's covalent link acts in) and the
**tangential** one perpendicular to it in the cross-section (the direction the crossover's dihedral
hinge rotates the attached body in). Loaded along the slab normal at azimuth `ψ`,

&nbsp;&nbsp;&nbsp;&nbsp;`1/k_z(ψ) = cos²ψ/k_radial + sin²ψ/k_tangential`,
&nbsp;&nbsp;&nbsp;&nbsp;**`κ(ψ) ≡ k_z(0)/k_z(ψ) = cos²ψ + sin²ψ·A`, &nbsp;`A ≡ k_radial/k_tangential`.**

Three things follow with no computation at all, and they are what decided where the effort went:

1. **The whole question is ONE anisotropy.** The member, its ground and the material enter only
   through `A`.
2. **`κ ≥ 1` whenever `A ≥ 1`, with equality iff `A = 1`.** An oblique root can never be *stiffer*,
   and it is **free exactly when the root is isotropic**.
3. At the honeycomb's own azimuth, `κ = 0.25 + 0.75 A`: **three quarters of the load path is the
   tangential axis, whatever the radial one is.** That is why the oblique root's *absolute*
   stiffness turns out quotable where its *ratio* to the perpendicular one is not.

**The oblique azimuth is derived, not asserted.**

> **Annotated, iteration 33 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md)).**
> The azimuth **set** is right and which members of it are **free** is a property of the block, not
> of the sublattice. On a full `m × n` block the `±60°` pair points at the other sublattice's helices
> **in its own x-raster row**, which are present; the real face gives every helix **exactly one**
> rooting azimuth at **30°**. `κ(30°) = 0.75 + 0.25 A`, so the rigid-body cost is **2.67233333×** and not
> 6.017× — a **favourable** correction that strengthens this claim's verdict — and *"three quarters
> of the load path is the tangential axis"* becomes three quarters **radial**.

 The honeycomb is two interpenetrating triangular
sublattices whose azimuth sets differ by half the separation, so where one carries `{0°, 120°, 240°}`
the other carries `{60°, 180°, 300°}` — and a top-face helix of the second sublattice has `±60°`
free. `obliqueAzimuthDegrees()` is `HoneycombLattice.azimuthSeparationDegrees()/2` and would be a
different number on any other lattice. It is **not** the square lattice's 33.74°/bp chord quantum,
and `CLAUDE.md`'s `cos²` ≤ 8.4 % entry does not transfer: that is a *couple* projected onto a
**chord the designer chooses**, this is a *translation* along an **azimuth the sublattice pins**.

---

## 2. Three root models, three answers, and the split is not a bracket

| root | `k_radial` | `k_tangential` | `A` | `k_z(0°)` | **`k_z(60°)`** | **`κ`** |
|---|---|---|---|---|---|---|
| **R1** flexible tie, two softened bonds | 64.7058824 | 64.7058824 | **1.0** | 64.7058824 | **64.7058824** | **1.0** |
| **R2** crossover-hinged rigid body | 64.7058824 | 8.4147343 | 7.68959304 | 64.7058824 | **10.7534964** | **6.01719478** |
| **R3** same, link as a **CONSTRAINT** | — | 8.4147343 | — | — | **11.2196457** | **NOT REPRESENTABLE** |

All stiffnesses in pN/nm.

**R1 costs exactly nothing, and that is a symmetry rather than a small number.**
`FlexureEndJoint`'s own invariant — *`k_⊥/k_axial` is exactly 1 for any isotropic element, and for
any covalent tie on a softened bond* — makes `A = 1` identically, so `κ = 1` at **every** azimuth.
A flexible link has no direction of its own; it cannot know which azimuth it left the helix on.

**R2 costs 6.017×**, because its radial axis is the crossover's covalent link and its tangential one
is `C-0009`'s dihedral spring on the frame-indifferent `d/2 = 1.268 nm` lever, `k_θ/(d/2)²`.

**R3 has no ratio at all.** `CLAUDE.md` records that a crossover's vertical link is a *constraint*
and that a covalent tie is a **binary** — *asking how stiff it is, is asking the wrong question*.
Under that reading the perpendicular root's normal stiffness is not a number, `κ = ∞`, and the
honest emission is `null`, not a large double.

**And the model boundary the task asked about does not fire.** `C-0037`'s `TwoLinkBase` refuses a
misalignment past 45°, and this root sits at 60° — but the two angles are different quantities.
That guard is on the misalignment between a two-link **chord's** perpendicular bisector and the axis
a **couple** is demanded about, where past a half right angle the restrained and free axes exchange
and the invariant `restrainedAxis ≥ freeAxis` is violated. This azimuth is a **translation**
direction; `k_z(ψ)` is smooth and monotone over the whole quadrant and reproduces both endpoints
exactly. The guard is checked and does **not** apply; carrying it here would have refused an answer
the models do give.

---

## 3. The ratio is a property of a model boundary; the ABSOLUTE is not

`Gen1Tile.crossoverInPlaneStiffness` is **derived** from Chen et al.'s softened-bond construction
and has never been fitted — which is why `Gen1Tile` ships a four-decade sweep for it. Over that
sweep:

| | perpendicular `k_z(0°)` | **oblique `k_z(60°)`** |
|---|---|---|
| whole declared sweep (`×0.03125` → `×128`) | **4096×** | **2.38716×** |
| the derived value upward (`×1` → constraint) | unbounded | **1.04335×** |

**The oblique root is the reading that can be quoted without settling `T-9`.** Three quarters of its
load path is the *tangential* axis, which is the one constant in this pair that **is** fitted.
`CLAUDE.md`'s *"ask what the correction multiplies before concluding it is unbounded"*, in a new
place — and the same arithmetic is why the *ratio* is not quotable at all.

**`F2` did not fire** (1.04335× against its 1.25× threshold), and the sweep's low end is worth
recording rather than hiding: below a link stiffness of **8.415 pN/nm** the anisotropy inverts
(`A < 1`) and an oblique root becomes **stiffer** than a perpendicular one, `κ = 0.43` at the
sweep's floor. The cheap bound predicts the crossing exactly, at `A = 1`, and `Gen1Tile`'s own
declared sweep straddles it — so *"oblique is worse"* is a statement about the derived value, not
about the family.

---

## 4. Two roots on one head add as TENSORS, and the scalar reading is 2.09× wrong

An oblique helix carries **two** free azimuths, at `±60°`, and rooting both on one rigid head gives

&nbsp;&nbsp;&nbsp;&nbsp;`k_z,pair = 2(cos²ψ·k_radial + sin²ψ·k_tangential)` = **44.9750426 pN/nm**,

because the two tensors sum and the mirror symmetry cancels the off-diagonal exactly. Adding the two
roots' own *normal* stiffnesses instead gives **21.5069928 pN/nm**, **2.0911823×** less — that
reading lets each head move laterally on its own, and the shared rigid head forbids exactly the
motion it was counting.

**The pair recovers the perpendicular root to within 1.43870642×**, for two stations instead of one.
It is priced here and not packed: whether the motif is buildable, and what its 7 bp domain costs in
folding yield, is left open.

---

## 5. What it costs a coupling PATH, and why the answer falls with the path count

A coupling path is the root **in series** with whatever supplies the compliance `C-0017`'s mandate
demands, and the mandate is soft by construction: at 10 paths the demand is **3.33333333 pN/nm**
against R2's oblique root of **10.7534964 pN/nm**. Sizing the series partner on the *perpendicular*
root and then re-reading it on the oblique one:

| paths | demand, pN/nm | partner, pN/nm | **oblique delivers** | paired root delivers | partner re-size |
|---|---|---|---|---|---|
| **10** | 3.33333333 | 3.514377 | **0.79462102** | 0.977899446 | **1.37456931×** |
| 20 | 1.66666667 | 1.71073095 | 0.885558579 | 0.98882625 | 1.15293372× |
| 30 | 1.11111111 | 1.13052415 | 0.920679884 | 0.992522984 | 1.09608153× |
| 50 | 0.666666667 | 0.673606859 | 0.950848447 | 0.995500333 | 1.0551088× |
| 75 | 0.444444444 | 0.447518308 | 0.966686495 | 0.996995716 | 1.03594725× |

**The cost FALLS as the coupling gets denser** — and `C-0118`'s best cell is the *sparsest*, which is
the adverse direction. Even there it is 0.795 of a share, and

**the remedy is a spacer CONTOUR LENGTH, not a lattice.** The oblique path's series partner has to be
**1.37456931×** stiffer, which is quantised at a nucleotide and not at a lattice site. A design that
uses one staple length everywhere pays a **10.27 % (the result file carries this in **prose only**, not as a field — quoted at the precision it states)** shortfall on `C-0017`'s mandated **sum**
instead — a **specification** failure, repairable by a second staple length, and not a flatness one.

---

## 6. `C-0118`'s flat cells survive, and the paired reading is 5× smaller than the unpaired one

Re-graded with the alternation the lattice imposes — half the top-face helices oblique at R2's
fraction, renormalised to the mandate so the *shape* is isolated from the *total* — under `C-0087`'s
measured dropout at the 90th percentile:

| paths | equal springs (reproduces `C-0118`) | **alternating** | flat at p90 | **paired median ratio** | realisations worse |
|---|---|---|---|---|---|
| 10 | 0.0278431488 | **0.0286224717** | **yes** | **1.00564884** | 2375 / 4000 |
| 20 | 0.0541089284 | **0.0542526922** | **yes** | 1.00345382 | 2117 / 4000 |
| 30 | 0.0461988976 | **0.0463821661** | **yes** | 1.00121389 | 2067 / 4000 |
| 50 | 0.0408747025 | **0.0407968539** | **yes** | 1.00036049 | 2040 / 4000 |

**4 of 4 flat**, against `T-5b`'s 0.10 — a **1.84×** margin at the worst cell and **3.49×** at the best. All four equal-spring rows
reproduce `C-0118` at departure **exactly 0.0**, because this study takes `C-0118`'s own seed
deliberately.

**The two readings of the same comparison differ by 5×, and the paired one is right.** The ratio of
the two cells' 90th percentiles is **1.02798976** at worst; the **median of the per-realisation
ratio**, on the stream both cells share, is **1.00564884**. A ratio of two order statistics is not
the order statistic of the ratio, and `CLAUDE.md`'s common-random-numbers discipline is what makes
the smaller number the honest one — it also matters that the realisation-count convergence departure
is **0.017**, i.e. of the same size as the unpaired ratio and 3× the paired one.

---

## 7. The five gates

1. **Dimensional** — `k_radial`, `k_tangential` and `k_z` are all pN/nm and `κ` is their ratio; the
   tangential axis is a pN·nm/rad spring divided by an nm² lever, asserted against
   `k_θ/(d/2)²` rather than constructed.
2. **Limiting cases, both named** — `ψ = 0°` returns the **radial axis exactly** (the perpendicular
   root) and `ψ = 90°` returns the **tangential axis exactly** (the pure in-plane case, which is
   `C-0009`'s across-helix compliance); both are tests, and the second holds for a root whose radial
   axis is a constraint.
3. **Symmetry** — `±ψ` give identical stiffness (the two oblique azimuths are mirror images), an
   **isotropic** root gives `κ = 1` at every azimuth sampled, and `k_z(ψ)` assembled from the full
   stiffness **tensor** agrees with the closed form, which is what would fail if the two axes were
   not its eigenvectors.
4. **Numerical convergence** — the decomposition is closed form and has no discretisation. The only
   sampled quantity is the dropout ensemble: **0.017** over 1000 → 2000 → 4000 realisations. The
   dishing sampling axis is **exactly 0.0** over 41 → 81 → 161 points, because at this cell the peak
   lands on a node all three grids share.
5. **Literature cross-check** — the azimuth count, the 7 bp step and the 21 bp period are Douglas et
   al. via `C-0119`, asserted as `SAME_PAIR_PERIOD_BP / ANY_AZIMUTH_STEP_BP == AZIMUTHS`; the two
   joint constants are Chen et al.'s softened bond via `C-0009`/`C-0020`, asserted against their
   literal `2αB/(100a)` and `2αS/(100a)` rather than read from a symbol.

---

## 8. Validity range, and what this does NOT establish

- **The tangential axis is fitted and the radial one is not.** `k_θ` is Chen et al.'s; the in-plane
  link is `C-0020`'s construction from the same softened bond and nobody has measured it. That is
  why it is swept over four decades here rather than quoted, and why `T-9` is named as the thing
  that would settle the ratio.
- **The decomposition assumes the root's two axes are the eigenvectors of its stiffness tensor.**
  Asserted as a test (`normalStiffnessFromTensor`) rather than assumed; it is what would fail if the
  two covalent links' own geometry coupled the axes.
- **The lateral offset of an oblique root's exit point is NOT modelled.** Its strand leaves the
  backbone about 60° around the helix, displacing its station by under a nanometre — below half the
  2.536 nm row pitch, and the grillage puts every attachment on a beam that sits on a helix axis, so
  a sub-pitch lateral offset is not representable in it at all. It is reported. The two oblique
  azimuths of one helix have **opposite** offsets, so alternating their sense cancels it by
  construction, at no lattice cost.
- **Only `10 × 6` is re-graded**, because it is the cross-section `C-0118` finds flat. `15 × 4` is
  flat at none of these counts under equal springs either way, which is `C-0118`'s own reading.
- **The re-grading moves the DISTRIBUTION only** — it does not move stations, does not re-route, and
  does not check that a root can be built on an azimuth without costing a torsion feasibility.
- The dropout statistics are measured on a **single-layer Rothemund rectangle**; only the profile
  transfers, in nm. `C-0109`'s assumption, inherited and named.
