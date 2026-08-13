# C-0020 — The in-plane load path into the tile: the concentration factor is 1, and it is bought by aligning the tether with the helices

| | |
|---|---|
| **Task** | [`T-15`](../tasks/T-15-in-plane-shear-lag.md) |
| **Leaves** | `A8.2` (structural rigidity / mode analysis — *"identify the dominant compliance term … and budget stiffness at the joints"*), `A1.2` (the anchoring scheme it prices) |
| **Verification type** | in-silico (an **in-plane** bar-beam-connector grillage written for this task, run beside the orthotropic shear-lag membrane it discretises) **+ logical** (an equilibrium argument that turned out to be too strong, and whose failure is itself a result) |
| **Verdict** | **PASS** on all six items of the acceptance predicate. The in-plane transfer ratio is **exactly 1** for a tether aligned with the helices and **up to 2.33** for one that is not, so `C-0009`'s 2.3–7.6× is replaced not by a number but by a **design rule with a factor of 11.75 riding on it**. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-15-in-plane-shear-lag.json`, produced by `structure.InPlaneLoadPathStudyKt`; models in `src/main/kotlin/structure/OrigamiMembrane.kt` and `ShearLag.kt`; **39 gate-named tests** in `src/test/kotlin/structure/OrigamiMembraneTest.kt` (21) and `ShearLagTest.kt` (18) |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.142 pN·nm`; 40 × 40.35 nm tile, 15 duplexes, 7–8 crossover columns; unit (1 pN) applied tether force, so every force below **is** a ratio |
| **Raises** | [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md) against [`C-0014`](C-0014-lateral-confinement.md) |
| **Consumes** | [`C-0014`](C-0014-lateral-confinement.md) (the scheme, the cable term, the `L_min` form), [`C-0009`](C-0009-discrete-lattice-tile.md) (every structural ingredient, the out-of-plane factor it replaces), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the phase machinery, the "sweep shapes not diagonals" discipline), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the per-path allowables and their literature trace), [`C-0010`](C-0010-tile-positional-variance.md) (**the exact lateral zero, which is what makes this a different problem**) |

---

## Claim, in one line

**A surface-parallel tether collects nothing from the layer — its lateral stiffness is exactly zero by symmetry — so no internal path can be loaded by *sharing*; a tether aligned with the helices therefore puts exactly its own tension into the one duplex it attaches to and `η = 1.0000`, while a tether at an angle applies a *moment* that the crossovers react as an axial couple and `η` reaches 2.33, so the in-plane factor is not a property of the sheet but of the direction the tether runs in, worth 11.75× in the effective allowable at no cost in material, count or stroke.**

---

## The two definitions, kept apart, because mixing them is half of `CH-0021`

| symbol | definition | value, aligned |
|---|---|---|
| **`η` — transfer ratio** | peak force in **one** load path ÷ the **applied tether force** | **1.0000** |
| `C` — concentration factor | the same peak ÷ the **equal share** over the paths available | 15.00 (duplex), 0.67 (crossover) |

`C-0009` reports the second (2.3–7.6). `C-0014` applied it as the first. Out of plane they differ by the 9.3
paths on the `ℓ`-contour, i.e. by a factor of ~20. Everything below is `η` unless it says otherwise, because
`η` is what `C-0014`'s `L_min` contains.

---

## The cheap bound, and the half of it that was wrong

Two things were available before any matrix was assembled.

**The shear-lag lengths, derived for this lattice.** Duplex `n` is a bar of stretch modulus `S` coupled to its
neighbours only through crossovers of in-plane shear stiffness `k_s` recurring every `p` on one interface, so
`S u_n'' + (k_s/p)(u_{n+1} − 2u_n + u_{n−1}) = 0` and a mode of across-helix wavenumber `q` decays as
`Λ(q) = √(S/(2(k_s/p)(1 − cos qd)))`:

| `k_s` [pN/nm] | `Λ = √(Sp/k_s)` | `Λ_nn` | **`Λ₁` (sharing)** | aspect ratio | `Λ₁/40 nm` |
|---|---|---|---|---|---|
| 2.02 | 76.9 | 54.4 | 368.0 | 28.6 | 9.20 |
| 32.4 | 19.2 | 13.6 | 92.0 | 7.15 | 2.30 |
| **64.7 (nominal)** | **13.60** | **9.62** | **65.05** | **5.06** | **1.63** |
| 517.6 | 4.81 | 3.40 | 23.0 | 1.79 | 0.58 |
| 8282 | 1.20 | 0.85 | 5.75 | 0.45 | 0.14 |

&nbsp;&nbsp;&nbsp;&nbsp;**`Λ₁ = 65 nm` against a 40 nm footprint. The Gen-1 tile is too small to share an in-plane point load at all** —
which is why an aligned `η` is 1 and not 1/15.

**The equilibrium bound, `η ≤ 1` — and it is false.** The Plan argued that because the layer supplies exactly
zero lateral stiffness (`C-0010`) a tether collects nothing, so every internal path carries a *fraction* of its
tension. That is right for the **cut total** and wrong for the **per-duplex peak**. See the falsifier section:
the declared falsifier fired, and it fired for a mechanism worth reporting.

---

## 1. The two principal directions

Per pN of applied tether force, nominal crossover stiffness, nominal phase:

| | `η` duplex axial | `η` crossover | `η` duplex in-plane shear | binding path | **`A_eff`** |
|---|---|---|---|---|---|
| **along the helices** | **1.0000** | **0.1826** | 0.0328 | the attachment's own duplex | **48.00 pN** |
| across the helices | 0.4851 | **0.7949** | 0.7276 | a crossover | **12.58 pN** |

`A_eff = min over path classes of (allowable ÷ η)`, with the duplex axial force judged against the **48 pN**
single-duplex shear allowable (a nick in a loaded duplex is a staple domain in shear geometry), the crossover
against the **10 pN** unzip allowable (`C-0009`'s convention, the conservative one), and the duplex in-plane
shear against the **65 pN** nicked ceiling. `A_eff` is what `L_min` consumes.

**Along the helices the load runs down a duplex and is shed to its neighbours slowly; across them, every part
of it must cross an interface immediately.** The two directions load different members and are judged against
allowables a factor of 4.8 apart, which is most of the 3.8× in `A_eff`.

## 2. The complete placement sweep, and the direction rule

`C-0015` established that searching a diagonal of a discrete anisotropic space is the wrong slice. The design
space of an edge-to-edge tether pair is **which duplex each end attaches to**, and there are only fifteen of
them, so a complete sweep is possible: **15 × 15 duplex pairs × 32 base-pair column phases = 7200 designs.**
Sweeping a continuous *angle* instead samples this space unevenly — several nominal angles snap to the same
pair of duplexes and therefore to the same physical design, and the intermediate ones are unreachable.

| across-helix offset | angle | placements | worst `η` axial | worst `η` crossover | **worst `A_eff`** | `L_min` at 10 nm |
|---|---|---|---|---|---|---|
| **0 (aligned)** | **0.00°** | 480 | **1.0000** | 0.1862 | **48.00 pN** | **33.5 nm** |
| 1 | 3.85° | 896 | 1.0745 | 0.4604 | 21.72 | 50.1 nm |
| 2 | 7.66° | 832 | 1.2307 | 0.6734 | 14.85 | 60.7 nm |
| 4 | 15.06° | 704 | 1.5295 | 1.1043 | 9.06 | 77.8 nm |
| 7 | 25.21° | 512 | 1.8860 | 1.6326 | 6.13 | 94.6 nm |
| 10 | 33.92° | 320 | 2.1484 | 2.0510 | 4.88 | 106.1 nm |
| **14 (rim to rim)** | **43.27°** | 64 | 2.2139 | **2.4475** | **4.09 pN** | **115.9 nm** |

&nbsp;&nbsp;&nbsp;&nbsp;**One duplex of misalignment — 3.85°, the finest step the lattice allows — costs a factor of 2.2 in the effective allowable. Fourteen costs 11.75.**

**Aligned, `η` on the binding path is exactly 1.0000 at every one of the 480 placements**, at every column
phase and every duplex, and — see §4 — at every crossover stiffness across four decades. It is not an average
or a best case; it is a saturated equilibrium bound.

## 3. The declared falsifier fired, and the mechanism it exposed

`T-15` declared *"the lattice returning `η > 1` anywhere"* as its primary falsifier and wired it in as a
runtime `check`. **It fired.** The largest values anywhere in the sweep are `η_axial = 2.3290` and
`η_crossover = 2.4475`.

**The equilibrium argument was too strong, and here is what it missed.** A tether that does not pull along a
duplex applies a **moment** to the duplex it lands on. The crossovers react that moment as an axial **couple**,
because they act on the **interface line** and not on the duplex axis. Equilibrium bounds the *sum* of the
duplex axial forces on a cut — checked to `1e−4` as a runtime assertion, for both the aligned and the worst
oblique case — and says nothing about how that sum is split, or about a member carrying more than the total in
one direction and its neighbour carrying the balance in the other. The transverse component is additionally
levered by the short free overhang between the last crossover column and the tile edge, held by supports one
crossover spacing behind it: the classical two-support over-reaction of a cantilever tip load.

Two things make it a **bracket** rather than an unbounded exposure, and both are asserted:

- **it is mesh-converged**: the peak crossover force at the worst placement moves 1.81906 → 1.81880 → 1.81827 →
  1.81720 pN over nested subdivisions 1 ⊂ 2 ⊂ 4 ⊂ 8, i.e. **0.1 %**;
- **it saturates in the one undetermined input**:

| `k_s` [pN/nm] | 2.02 | 8.09 | 32.4 | **64.7** | 129 | 518 | 2071 | 8282 |
|---|---|---|---|---|---|---|---|---|
| `η` aligned | **1.0000** | **1.0000** | **1.0000** | **1.0000** | **1.0000** | **1.0000** | **1.0000** | **1.0000** |
| `η` oblique, axial | 1.2422 | 1.7589 | 2.2044 | **2.3290** | 2.4002 | 2.4576 | 2.4725 | **2.4762** |
| `η` across, crossover | 0.5890 | 0.7217 | 0.7994 | **0.8255** | 0.8441 | 0.8625 | 0.8681 | 0.8695 |

&nbsp;&nbsp;&nbsp;&nbsp;**The aligned answer is exactly 1 over four decades of the parameter nobody has measured, and the oblique overshoot is bounded by 2.48 over the same range.**

## 4. What the crossover's in-plane stiffness does, and does not, decide

`k_s` is the one input with no measurement behind it — nothing in the accessible literature gives a crossover's
in-plane stiffness in any form, and `k_θ`, the only crossover elastic constant ever fitted, describes relative
**rotation**. It is **derived** here by applying Chen et al.'s own softened-bond construction to the one duplex
constant that describes displacement rather than rotation (`k_s = 2αS/(100a) = 64.7 pN/nm`) and **swept over
four decades**, and the answer is reported as a function of it.

| `k_s` × | along: `η` axial | along: `η` crossover | along `A_eff` | across `A_eff` |
|---|---|---|---|---|
| 0.031 | 1.0000 | 0.0255 | 48.00 | 18.22 |
| **1.0** | **1.0000** | **0.1826** | **48.00** | **12.58** |
| 8 | 1.0000 | 0.2297 | 43.54 | 11.70 |
| 128 | 1.0000 | 0.2384 | **41.94** | 11.54 |

**Aligned, `A_eff ≥ 41.9 pN` everywhere in the sweep**, so `L_min` at the 10 nm stroke is at worst 35.9 nm
against the 33.5 nm nominal — a **7 % exposure to a completely undetermined parameter**. That is the cheapest
result in this claim and it is why the deliverable can be quoted as a number at all.

## 5. Layout is worth nothing here, and that is the structural difference

`C-0015` found the staple layout worth **×1.43–1.60** on the peak per-load-path force **out of plane**.
In plane, over the same complete 32-phase sweep:

| | crossover path | **duplex-axial path (the binding one)** |
|---|---|---|
| along the helices, 480 placements | ×2.721 (0.0684 → 0.1862) | **×1.0000** |
| across the helices, 1024 placements | ×1.120 (0.7949 → 0.8904) | ×1.4258 |

&nbsp;&nbsp;&nbsp;&nbsp;**Layout moves the crossover path by up to 2.7× and the binding path by exactly nothing.**

The reason is the whole difference between the two problems in one sentence: **out of plane the load has to
travel through the lattice to reach a crossover, and where the crossovers are decides how much each takes;
in plane the attachment itself is the most loaded member, and no arrangement of crossovers can relieve it.**
`C-0015`'s design lever does not exist here — and neither does the corresponding uncertainty.

## 6. The continuum beside it, and two things it cannot do

Per `CLAUDE.md`'s standing rule, the orthotropic **shear-lag membrane** — `(S/d)u_xx + (k_s d/p)u_yy + f = 0`,
free edges, solved in closed form as a cosine series whose `n = 0` mode **is** the equal share — is run beside
the lattice on the same footprint with the same constants.

| `k_s` | station | lattice [pN] | continuum [pN] | difference | excess |
|---|---|---|---|---|---|
| 2.0 | `x = 0`, loaded duplex | 0.92794 | 0.93901 | **−0.011** | 0.988 |
| 2.0 | `x = 5`, loaded duplex | 0.93690 | 0.94362 | −0.007 | 0.993 |
| **64.7** | `x = 0`, loaded duplex | **0.35762** | **0.32490** | **+0.033** | **1.101** |
| 64.7 | `x = 5`, loaded duplex | 0.42736 | 0.34682 | +0.081 | 1.232 |
| 2071 | `x = 5`, loaded duplex | 0.19218 | 0.07882 | +0.113 | **2.438** |

**Where the continuum's own premise holds — a transfer length well above the crossover spacing — the two agree
to better than 1.4 % of the applied force**, which is what licenses attributing the disagreement at the Gen-1
stiffness to discreteness. Stations where both are below 1 % of the applied force are compared **absolutely**
and carry no ratio at all, per `CLAUDE.md`'s rule about comparing two quantities that are both meant to be zero.

**The continuum cannot produce this claim's deliverable, and it fails in two distinct ways.**

1. **It converges only logarithmically at the load point** — a point load on a two-dimensional elliptic problem
   is a log singularity — so it has no peak per-path force to give, exactly as `C-0006`'s plate had none at a
   discrete anchor. Asserted as a test. It converges *to the applied force*, which is the equilibrium bound
   reached independently by the continuum.
2. **It is not frame-indifferent.** Classical shear lag drops `∂v/∂x` from the shear strain, so a rigid in-plane
   rotation of the whole sheet costs it energy. The lattice keeps the term, through the **connector arm** — and
   the arm is **not a free parameter**: frame indifference fixes it at exactly `d/2`, because only then do the
   two duplexes' material points coincide on the interface line. Asserted by sweeping the arm and showing the
   rigid-rotation energy is `2e−14 pN·nm` at `d/2` and finite at 0, 0.5, 1.0 and 2.0 nm.

The dropped term is not small:

| `k_s` | loaded duplex's share, arm = `d/2` | arm = 0 (classical shear lag) | **ratio** | rigid-rotation energy, arm = 0 |
|---|---|---|---|---|
| 2.0 | 0.9356 | 0.9279 | 1.008 | 4.05e−4 |
| **64.7** | **0.5924** | **0.3576** | **1.656** | 1.31e−2 |
| 2071 | 0.4821 | 0.1197 | **4.028** | 4.20e−1 |

&nbsp;&nbsp;&nbsp;&nbsp;**Classical shear lag understates how much load the attached duplex keeps, by up to 4×,
and it does so through a term whose omission also breaks frame indifference.**

## 7. The distributed-drive class is milder, and it is the thermal one

A tile driven laterally by a **distributed** force — which is what thermal excitation and any lateral field
gradient supply — and reacted at four corner anchors is `C-0009`'s "load reacted at a point" class:

| anchor stiffness | drive | peak anchor force | `η` axial | `η` crossover | `A_eff` |
|---|---|---|---|---|---|
| 5 pN/nm | along | 0.2550 | 0.2461 | 0.1805 | 55.4 pN |
| 55 pN/nm | along | 0.2573 | 0.2471 | 0.1924 | 52.0 pN |
| 550 pN/nm | along | 0.2582 | 0.2478 | 0.1935 | 51.7 pN |
| 55 pN/nm | across | 0.3575 | 0.4688 | 0.5008 | 20.0 pN |

**A distributed drive never loads a single path above a quarter of the total**, and the anchor stiffness is
worth under 1 % across two decades. The tether-tension case above is the binding one, and it is the one
`C-0014` needs.

---

## What this does to `C-0014` — the propagation, in full

`C-0014`'s rule is `T ≤ A/n` ⇒ `L_min = δ√(S n/(2A))`. The exact (non-linearised) counterpart is used here,
`L = δ/√((1 + A_eff/S)² − 1)`, and the two agree to ~1 % at these strokes.

| stroke | basis | `n` | `A_eff` [pN] | **`L_min`** | vs `C-0014` | tether tension | **4-tether normal preload** |
|---|---|---|---|---|---|---|---|
| 3 nm | `C-0009`'s 7.6× as applied | 7.60 | 6.32 | 27.96 nm | — | 6.3 pN | 2.7 pN |
| **3 nm** | **aligned with the helices** | **1.00** | **48.0** | **10.05 nm** | **2.79× shorter** | 48.0 pN | **54.9 pN** |
| 3 nm | across the helices | 3.82 | 12.6 | 19.78 nm | 1.42× shorter | 12.6 pN | 7.5 pN |
| 3 nm | worst of 7200 placements | 11.75 | 4.09 | 34.77 nm | **0.81× — worse** | 4.1 pN | 1.4 pN |
| 3 nm | aligned, joint in unzip geometry | 1.00 | 10.0 | 22.20 nm | 1.26× shorter | 10.0 pN | 5.4 pN |
| 10 nm | `C-0009`'s 7.6× as applied | 7.60 | 6.32 | 93.18 nm | — | 6.3 pN | 2.7 pN |
| **10 nm** | **aligned with the helices** | **1.00** | **48.0** | **33.49 nm** | **2.79× shorter** | 48.0 pN | **54.9 pN** |
| 10 nm | across the helices | 3.82 | 12.6 | 65.93 nm | 1.42× shorter | 12.6 pN | 7.5 pN |
| 10 nm | worst of 7200 placements | 11.75 | 4.09 | **115.92 nm** | **0.80× — worse** | 4.1 pN | 1.4 pN |
| 10 nm | aligned, joint in unzip geometry | 1.00 | 10.0 | 73.99 nm | 1.26× shorter | 10.0 pN | 5.4 pN |

### Does *"lateral confinement and the desired stroke are incompatible at a fixed footprint"* survive?

**No, not in the currency it was written in — and yes, in a currency `C-0014`'s formula does not contain.**

- **The footprint statement falls.** Aligned, §3's *desired* 10 nm stroke needs a **33.5 nm** tether, i.e. an
  assembly of ~107 nm around a 40 nm tile — which is what `C-0014` already accepted for the *acceptable* 3 nm
  stroke (~96 nm). The 10 nm stroke costs what the 3 nm stroke cost. The ~227 nm assembly is withdrawn.
- **A new constraint takes its place, and it is exact.** At the minimum length the tether tension *is* the
  allowable, so the geometry fixes `δ/L = √((1+A/S)² − 1)` and the **normal preload**
  `F_z = n_t A (δ/L)/√(1+(δ/L)²) ≈ n_t A √(2A/S)` is **independent of the stroke**:

&nbsp;&nbsp;&nbsp;&nbsp;**54.9 pN for four tethers presented in shear geometry — 55 % of the §3 100 pN target force — at 3 nm and at 10 nm alike.**

  In unzip geometry the same rule gives **5.4 pN** at a 2.2× longer tether. **The design trade is explicit:
  shear geometry buys the short tether and pays 55 % of the target force in preload; unzip geometry pays a
  2.2× longer tether and 5 % of it.** This is `T-13`'s problem (what holds the tile down) arriving from a new
  direction, and it did not exist while the factor was 7.6.
- **And the whole gain is conditional on a design rule.** At the worst of the 7200 placements `L_min` is
  **115.9 nm**, *worse* than the 93.3 nm the conservative stand-in produced. **The physics is not kinder; the
  alignment is free.**

This raises [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md).

---

## The five verification gates

Executed as tests: `src/test/kotlin/structure/OrigamiMembraneTest.kt` (21) and `ShearLagTest.kt` (18),
**39 tests, all green**, each named for the gate it discharges. Full-suite run `tools/verify.sh` on the working
tree: **BUILD SUCCESSFUL**, no packages dropped.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `Λ = √(Sp/k_s)` is a length and quartering `k_s` doubles it *exactly*; `Λ/Λ_nn = √2`; the aspect ratio is `Λ` measured in interhelical distances; a uniform axial strain costs exactly `½(S/d)ε²A` and slides no crossover; the lattice has exactly three in-plane degrees of freedom per node; **the in-plane and out-of-plane lattices are the same sheet** — identical crossover count (56), identical `(lowerBeam, column, x, y)` for every one, identical node stations | **PASS** |
| **2 — limiting cases** | see below | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | nested mesh 1 ⊂ 2 ⊂ 4 (⊂ 8 for the oblique case): peak crossover force to **0.1 %** at the worst oblique placement and `6e−6` aligned; the regularising bed swept over four decades moves the answer by `<1e−5` between `1e−6` and `1e−4` and carries **`<1e−9`** of the applied force at every point; the continuum mode series converges to `1e−6` away from the load and **is shown not to converge** at it | **PASS** |
| **5 — literature cross-check and controls** | the lattice **reproduces the shear-lag neighbour-exchange length it is compared against**, 78.7 nm measured against 77.4 nm predicted (1.8 %) from the decay of a two-duplex strip; the lattice and the continuum membrane agree to `<0.02` pN of the applied force where the continuum's own premise holds; every structural constant is `C-0006`/`C-0009`'s, unchanged, so any difference is the plane the load is applied in | **PASS** |

### Gate 2 — the limiting cases that license the model

| statement | outcome |
|---|---|
| `u = γy` costs `½(k_s d/p)γ²` per unit area | **exactly `56/55.147`** — the *identical* discretisation excess `C-0009` found for `D_⊥`, because it is the identical crossover count over the identical continuum areal density. Asserted to `1e−9`, and against the counted ratio independently |
| `v = εy` costs `½(k_n d/p)ε²` per unit area | same `56/55.147`, to `1e−9` |
| `v = ½κx²` costs `½(EI/d)κ²` per unit area | exact to `1e−9` — `D_∥` reproduced in the membrane problem |
| a rigid in-plane **translation** stores nothing structural | `<1e−12` |
| a rigid in-plane **rotation** stores nothing structural | `<1e−12`, **and only at `connectorArm = d/2`** — swept over 0, 0.5, 1.0, `d/2`, 2.0 nm and finite at every other value. **Frame indifference fixes the arm; it is not fitted** |
| doubling `S` doubles the axial energy and nothing else | exact |
| a rigid crossover on a **long** strip forces an equal share | `1/3` each to `1e−3` on a 200 nm, 3-duplex strip — and it needs *both* premises, a rigid crossover **and** a strip long enough for the load to have become uniform, which the 40 nm tile is not |
| the continuum's rigid-crossover limit is exactly the equal share | `1/15` per duplex to `1e−6`, at every duplex |
| at the zone boundary the lattice decay length is exactly `π/2` of the continuum's | exact identity, not a tolerance |
| the Gen-1 sharing length exceeds the tile | asserted, as the cheap bound that decides the regime |

### Gate 3 — what is checked

1. **The transfer ratio never exceeds one for an axis-aligned pull** — asserted at four crossover stiffnesses
   and two duplexes, on all three path classes. This is the surviving half of the falsified bound.
2. **`η = 1.0000` exactly for every aligned placement** — a runtime `check` over all 480 of them, to `1e−5`.
3. **In-plane force balance closes on the applied load** to `1e−8`, anchors plus bed against the applied force.
4. **The crossovers on one interface carry exactly the in-plane force crossing it** — both components, computed
   independently from cut equilibrium including the bed's own reaction, to `1e−6`, at all 14 interfaces, under
   an asymmetric two-component load.
5. **The duplex axial forces on a cut sum to the net applied force crossing it** — the equilibrium statement
   that *does* hold, asserted at `1e−4` for the aligned and the worst oblique case. This is what distinguishes
   the falsified bound from a broken assembly.
6. **A chord on the mid duplex produces a symmetric force distribution** about it, to `1e−6`.

### The falsifiers, and whether they fired

| falsifier | fired? | outcome |
|---|---|---|
| 1. the lattice returning `η > 1` anywhere | **YES — and it is the most informative result in the task** | 2.3290 axial and 2.4475 crossover, at oblique placements. The equilibrium argument bounds the **cut total**, not the per-duplex peak: an oblique tether applies a moment which the crossovers react as an axial couple, levered by the free overhang at the tile edge. Mesh-converged to 0.1 % and saturating at 2.48 over four decades of `k_s`, so it is a bracket. The runtime check was **replaced by the correct invariant** (the cut sum) plus the saturation ceiling, not deleted |
| 2. `η` depending strongly on `k_s` | **no, and emphatically so** | aligned `η` is **exactly 1.0000** at every one of the eight stiffnesses spanning four decades; `A_eff` moves from 48.0 to 41.9, a 7 % exposure |
| 3. lattice and continuum disagreeing where discreteness is small | **no** | ≤ 1.4 % of the applied force at `k_s = 2`, where `Λ_nn/p = 5.0` |
| 4. layout moving `η` by more than the direction does | **no, and it is the cleanest contrast in the claim** | layout moves the binding path by **×1.0000** and direction by **×11.75** |
| 5. the regularisation carrying a non-negligible part of the load | **no** | `<1e−9` of the applied force at every point of every sweep, asserted as a runtime `check` |
| 6. the mesh not converging at the attachment node | **no** | 0.1 % at the worst oblique placement over nested 1 ⊂ 2 ⊂ 4 ⊂ 8 |
| 7. the in-plane and out-of-plane models disagreeing on a shared quantity | **no** | identical crossover count, identical crossover positions and parities, identical node stations |

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Not measured.** No in-plane force in a loaded origami sheet
  has ever been measured, and no crossover in-plane stiffness exists in the literature in any form.
- **`k_s` is DERIVED, not measured**, from Chen et al.'s softened-bond construction with the stretch modulus in
  place of the bending rigidity. Swept over four decades; the aligned answer is invariant and the oblique one
  is bracketed. **`T-9` should produce it**, at the same cost as `k_θ` and by the same simulation.
- **Linear elasticity, small displacements.** The largest in-plane rotation anywhere in the sweep is 0.027 rad,
  so the linearisation is safe. The tether's own geometric stiffening — `C-0014`'s cable term — is a
  finite-displacement effect and is *consumed* here, not re-derived.
- **No out-of-plane coupling.** Exact for a flat sheet at linear order. **The tile is not flat under load**
  (`C-0006` rejects the rigid-plate assumption, and `C-0009` and `C-0015` quantify the dishing), so a dished
  tile couples the membrane and bending problems at second order in the slope. Not modelled, and the direction
  of that coupling is not established here.
- **No in-plane foundation**, on `C-0010`'s exact symmetry zero. The regularising bed is shown to carry below
  `1e−9` of the applied force. Any in-plane adsorption of the tile onto the layer would break that symmetry
  and would *reduce* every force here.
- **The crossover has no rotational restraint in plane.** A crossover that resisted the relative in-plane
  rotation of the two duplexes it joins would stiffen the sheet and spread the load further, which lowers every
  force reported here. Omitting it is the conservative direction, and it is stated rather than computed.
- **An attachment is one point on one duplex.** A tether bonded across two duplexes, or onto a crossover, would
  change the entry topology entirely — and since the binding path *is* the attachment, that is the one
  modelling choice this claim's headline rests on.
- **The 48 pN allowable is applied to a duplex axial force**, on the argument that every origami helix is
  nicked and an axial load at a nick is a hybridised staple domain in shear geometry. If a particular
  attachment presents its joint in **unzip** geometry the allowable is 10–15 pN and `L_min` rises 2.2×; that
  row is carried in the table. `C-0006`'s *"unzip geometry is 4–6× weaker than shear, and that is the single
  largest design lever in this task, and it costs nothing"* applies here unchanged.
- **Rupture allowables are quasi-static extrapolations** of loading-rate-dependent measurements (`C-0006`).
  §4(f)'s 35–60 pN band is **not** used, being a whole-cross-section number.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.** The four-layer reading of §3 is not attempted.
- **The tile is 3.7 crossover repeats wide along the helices** (`C-0015`), so "phase" and "position in tile"
  are not cleanly separable in `x` at this size. It does not affect the headline, because the headline is
  layout-independent — but it does affect the ×2.721 crossover-path span quoted in §5.

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

- `S = 1100 pN` — **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997). **The single most
  load-bearing number here**: it sets `Λ`, and `L_min ∝ √S`.
- `EI = 230 pN·nm²` — **CITED**, CanDo (Kim et al., *NAR* **40**:2862, 2012); a *model input* in that paper.
  Used as the **in-plane** bending rigidity too, because a duplex has a circular section.
- `d = 2.69 nm` — **CITED, MEASURED** (SAXS), Fischer et al., *Nano Lett.* **16**:4282 (2016).
- `p = 32 bp` per interface — **CITED**, Rothemund, *Nature* **440**:297 (2006). The per-helix 16 bp would
  **halve** `Λ` and double the in-plane shear coupling — the same trap `CLAUDE.md` records for `D_⊥`.
- `0.34 nm` rise per base pair — **CITED**, Douglas et al., *Nature* **459**:414 (2009). Quantises the phase.
- **`k_s = 2αS/(100a) = 64.7 pN/nm` — DERIVED, from a CITED and FITTED construction** (Chen et al., *JACS*
  **136**:6995, 2014, SI). **Not measured, and not measurable from anything in the literature.** Swept.
- The connector arm `d/2` — **DERIVED**, and forced: frame indifference admits no other value.
- Per-path allowables 48 / 10–15 / 65 pN — **CITED, MEASURED** via `C-0006`'s trace (Strunz et al. 1999;
  Essevaz-Roulet et al. 1997; van Mameren et al. 2009), all loading-rate dependent.
- The layer's **exactly zero** lateral stiffness — **DERIVED**, `C-0010`, and it is the premise that makes this
  a different problem from `C-0009`'s.
- `C-0014`'s 28.0 / 93.3 nm tether lengths, its `L_min` form and its 7.6× stand-in — **CITED**, and held as
  constants in the study so the correction is *computed* against them rather than asserted.
- The 40 nm footprint, 100 pN target, 3 and 10 nm strokes — §3.

Everything else is derived from these in code.

## Challenges

**Raises [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md)** against `C-0014`'s
per-anchor force table, its `L_min` design rule and the `T-2` footprint constraint it produced.
None stands against this claim.

The way this claim would fail is through **the entry topology at the attachment**. Its headline — `η = 1`
aligned — says the attachment's own duplex carries the whole tether tension, which is true precisely because a
tether is modelled as loading one point of one duplex. A tether bonded across two duplexes would halve it; a
tether whose joint is presented in unzip geometry raises `L_min` by 2.2×; and a crossover with in-plane
rotational restraint, which nothing here models, would lower every oblique figure. A further result
contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than
overwriting it.
