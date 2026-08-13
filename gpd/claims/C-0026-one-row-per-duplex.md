# C-0026 — "One attachment row per duplex" is not a different scheme from the 3 × 15 grid, and its exact zero is a 20× margin rather than a knife-edge

| | |
|---|---|
| **Task** | [`T-17`](../tasks/T-17-one-row-per-duplex.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` (the anchoring scheme it prices) and `A1.1` (the lateral and yaw bounds the same anchors carry) |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage loaded through `C-0022`'s **solved** electrostatic edge profile, read from its own result file, with `C-0006`'s continuum plate run beside it) **+ logical** (a rigid-tile cut-equilibrium identity that gives the restored interface force in closed form before any matrix is assembled) |
| **Verdict** | **PASS on all six clauses of the predicate, and the coordinator's own framing is refuted in code: `3 × 15` *is* one attachment row per duplex, so there was never a choice between two schemes.** The branch is **not** killed; what is killed is the *status* of the exact zero. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** No crossover force in a loaded origami sheet has ever been measured. |
| **Provenance** | `gpd/results/T-17-one-row-per-duplex.json`, produced by `coupling.OneRowPerDuplexStudyKt`; model in `src/main/kotlin/coupling/UniformityBudget.kt`; **22 gate-named tests** in `src/test/kotlin/coupling/UniformityBudgetTest.kt` (61 in `coupling`, **931 in the suite, 0 failures** on `tools/verify.sh`). 15 grid shapes × 6 load profiles × 1–5 foundation stiffnesses = **133 solved lattice-and-plate states**, plus a **23-profile sweep** on the design grid, 20 scatter states, 10 thermal states, 15 duty records, 6 allowable crossings, 8 convergence records and 7 upstream reproductions. The result file re-run through `tools/study.sh` and diffed **byte-for-byte identical**. |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.141947 pN·nm`; 40.0 × 40.35 nm tile, 15 duplexes at the SAXS-measured `d = 2.69 nm`, 8 crossover columns, 56 crossovers; §3's 100 pN over the footprint; `C-0017`'s 33.333 pN/nm coupling as `n` equal springs; `C-0001`'s secant `k_f` swept ×[0.25, 4] |
| **Raises** | [`CH-0033`](../challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md) against `C-0015` and `C-0017`; [`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md) against `C-0006`, `C-0009` and `C-0015` |
| **Consumes** | [`C-0015`](C-0015-crossover-phase-and-registration.md) (the lattice as re-parameterised by the layout, the 3 × 15 grid, the exact zero, the phase machinery, the "shapes not counts" discipline), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage, the concentration factor, the 56 crossovers), [`C-0022`](C-0022-tile-edge-load-profile.md) (the **solved** lateral load profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the assumed taper, the allowables, the flatness convention), [`C-0017`](C-0017-output-coupling-stiffness.md) (the 33.333 pN/nm mandate, `K2`, the lateral and yaw by-products), [`C-0023`](C-0023-two-sided-coupling.md) (two-sidedness and the path-count-from-the-allowable rule), [`C-0024`](C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the **length-dependent** joint allowable), [`C-0014`](C-0014-lateral-confinement.md) (the lateral and yaw bounds), [`C-0010`](C-0010-tile-positional-variance.md) (the thermal excitation) |

---

## The claim, in one line

**`C-0015`'s 45-as-3 × 15 flatness grid *is* one attachment row per duplex — its fifteen rows land on the fifteen duplex axes to `3.6e−15 nm` — so the two schemes the task was sent to compare are one object; and once the load is the one `T-3b` actually solved, the exact zero becomes 0.150 pN at the design point and at most 0.332 pN over all 21 of `C-0022`'s states, which is 6.8 % of the same scheme's 2.222 pN per-path static share and 67× below the 10 pN unzip allowable. The zero is not a knife-edge but a **20.2× margin** against the worst equal-count shape under the same load, it costs nothing in count, share or flatness to keep, and no non-uniformity this programme can name — including a coupling with every second path at one per cent of nominal — brings the crossover path within 12× of an allowable. The binding per-path constraint is, and remains, the static share `100/n`.**

---

## The conventions, restated rather than inherited

- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
- `w` is positive **downward**, compressing the polymer layer (`T-5`, unchanged).
- A **crossover force** is the transverse force one crossover transmits between the two duplexes it joins, signed as in `C-0009`; the reported quantity is `max |verticalForce|`.
- The **load** is a downward pressure of interior value `100 pN / 1600 nm²` modified by `C-0022`'s solved collar — `C-0022`'s own convention, so the **total** load exceeds 100 pN by its edge gain (+14.6 % at the design point, reproduced here to 0.65 %).
- A collar **depth is negative for an enhancement**, which is the sign `C-0022` solved.
- The **coupling** is `n` discrete springs to ground of total stiffness `33.333 pN/nm` (`C-0017`'s mandate), one per attachment.
- **Flat** means peak dishing below 10 % of the **free-tile** stroke (`T-5b`'s convention, via `C-0015`); the free stroke here is 4.907 nm at `k_f` × 1, against `C-0006`'s 4.95 nm.

---

## `P1` — the two schemes are one, and the premise of the comparison is refuted in code

`attachmentGrid(columns, 15, 40.0, 40.35)` places its rows at `40.35(j + ½)/15 − 20.175 = (j − 7)·2.69`, which
is `beamY[j]` **identically**. Asserted, not argued:

| shape | attachments | rows | one row per duplex? | max departure from a duplex axis |
|---|---|---|---|---|
| 1 × 15 … 15 × 15 | 15 … 225 | 15 | **true** | **3.6e−15 nm** |
| **3 × 15** | **45** | **15** | **true** | **3.6e−15 nm** |
| 5 × 9, 9 × 5, 15 × 3 | 45 | 9, 5, 3 | false | — |
| 8 × 8, 7 × 7, 3 × 11, 3 × 14, 3 × 7 | 64, 49, 33, 42, 21 | — | false | — |

&nbsp;&nbsp;&nbsp;&nbsp;**"Fifteen rows" and "forty-five attachments as 3 × 15" are the same object. There is no scheme conflict to cost, and `C-0017`'s `K2`, `C-0023`'s `E3`/`E5` and `C-0015`'s flatness answer are all already the one-row scheme.**

What the comparison has to be run against instead is the set of grids at the **same count** that are *not*
commensurate — and that is what §3 below does.

---

## `P3` — the cheap bound, which needs no matrix and settles the structure of the answer

Cut between duplex `j` and `j + 1`. On a **rigid** tile every duplex's foundation reaction and every duplex's
coupling reaction are equal (the first because the deflection is uniform, the second because one row per
duplex puts the same springs at the same stations on every row), so

&nbsp;&nbsp;&nbsp;&nbsp;**`V_j = Σ_{i>j} (Q_i − Q̄)`**, with `Q_i` the load on duplex `i`'s tributary strip.

Three consequences, each free, and each executed as a test:

1. **A load varying only ALONG the helices restores exactly nothing** — `Q_i` is then the same for every duplex. Only the **across-helix** content of the edge collar can break the symmetry, and the collar along the `x` rims is common mode.
2. `V_j` is **exactly linear** in the collar depth. The lattice confirms it to `1e−6`: `5.000×` at five times the depth, and the same magnitude with the sign reversed for an enhancement.
3. `V_j` vanishes identically for a uniform load — `C-0015`'s exact zero, recovered without a matrix.

The identity is asserted in the rigid limit by stiffening the **sheet** (not the foundation — a stiff Winkler
foundation makes the tile *conform*, which is the opposite of rigid): over `×1 → ×10⁶` in `EI`, `GJ` and `k_θ`
the departure falls monotonically to **under 1 %** of the identity.

> **The identity is a conservative CEILING, not an estimate: at `C-0022`'s design point it gives 2.124 pN on the worst interface and the solved lattice carries 0.239 pN — the tile's own compliance sheds 89 % of it**, because a rim duplex under extra load sinks further into its own foundation and its own attachments instead of handing the excess inboard.

---

## `P2` — what the solved edge profile actually restores

Design grid 3 × 15, `k_f` × 1, `C-0022`'s design point (2 mM, 10 nm gap, 0.192 V; depth `−0.303`, width 8.94 nm, plus its rim residual):

| quantity | value |
|---|---|
| peak per-load-path crossover force, **uniform** load | **0.000000 pN** (`< 1e−9`, the reporting floor) |
| peak per-load-path crossover force, **solved** load | **0.1504 pN** |
| … as a fraction of the 2.222 pN per-path static share | **6.8 %** |
| … against the 10 pN unzip allowable | **67× below** |
| worst over **all 21** of `C-0022`'s solved states (0.5 mM, 10 nm, 0.192 V) | **0.3315 pN**, 30× below unzip |
| `C-0006`'s **assumed** taper (+0.50 over 4 nm), for audit | 0.2093 pN |
| peak interface force | 0.2389 pN, against the rigid identity's 2.124 |
| concentration onto the 4 crossovers of that interface | **2.52×** (2.52–3.49× over the headline states) |

The concentration factor is `C-0009`'s quantity measured at a **distributed** coupling instead of at a rigid
point anchor, and it sits **at or below the bottom** of its 2.3–7.6× band. That is what makes `CH-0033`
possible: `C-0017` applied that band to the 2.222 pN *share*, which never crosses a crossover at all.

### The restored interface force is a property of the load, not of the grid

Over the seven one-row shapes from 1 × 15 to 15 × 15 — a **fifteen-fold** range in attachment count:

| shape | 1 × 15 | 2 × 15 | **3 × 15** | 4 × 15 | 5 × 15 | 8 × 15 | 15 × 15 |
|---|---|---|---|---|---|---|---|
| peak **interface** force [pN] | 0.2401 | 0.2401 | **0.2389** | 0.2386 | 0.2384 | 0.2384 | 0.2384 |
| peak **crossover** force [pN] | 0.2093 | 0.1645 | **0.1504** | 0.1535 | 0.1543 | 0.1557 | 0.1564 |

&nbsp;&nbsp;&nbsp;&nbsp;**0.72 % over the whole range. Adding attachment columns cannot relieve the crossovers, because the cut equilibrium does not contain the column count** — what the columns change is only how the same force is shared out, and even that moves the peak by 1.4×.

### The foundation sweep

| `k_f` × | 0.25 | 0.50 | **1.00** | 2.00 | 4.00 |
|---|---|---|---|---|---|
| peak crossover force [pN] | 0.1659 | 0.1606 | **0.1504** | 0.1322 | 0.1049 |
| peak duplex shear [pN] | 1.096 | 0.971 | **0.793** | 0.580 | 0.379 |

The whole `CH-0001` sweep is worth **1.58×**, and the softest corner — the direction `T-1c`'s corrections run —
is the worst. Even there the margin against unzip is **60×**.

---

## §3 — shape, not count: the comparison the task was actually asking for

Four grids at **45 attachments** and an identical 2.222 pN static share, under the same solved load:

| shape | one row per duplex? | peak crossover [pN], uniform | peak crossover [pN], solved | peak dishing / stroke, uniform | flat? |
|---|---|---|---|---|---|
| **3 × 15** | **yes** | **0.0000** | **0.1504** | **0.049** | **yes** |
| 5 × 9 | no | 0.9152 | 1.1669 | 0.046 | yes |
| 9 × 5 | no | 1.4246 | 1.8648 | 0.128 | no |
| 15 × 3 | no | 2.4202 | 3.0321 | 0.334 | no |
| *(8 × 8, 64 attachments — `C-0009`'s answer)* | no | 1.1824 | 1.4940 | 0.052 | yes |

&nbsp;&nbsp;&nbsp;&nbsp;**20.2× between the best and worst 45-attachment shape, at identical count, identical share and — for 5 × 9 — identical flatness (0.046 against 0.049 of the stroke). The commensurability is a FREE 7.8× against the only equal-count shape that matches it on flatness (1.167 against 0.150 pN under the solved load, and 0.915 against 0.000 pN under a uniform one), and a free 20.2× against the worst.**

That is the real content of `C-0015`'s exact zero once a realistic load is applied: not an exact zero, but a
design rule that costs nothing to obey and is worth an order of magnitude on one load path.

---

## §4 — attachment scatter: the non-uniformity a builder controls, and the one that is *correlated* the wrong way

Deterministic patterns under a **uniform** load, which isolates the scatter from the load shape. 3 × 15 grid:

| pattern | pN per unit relative amplitude | at ε = 0.10 | at ε = 0.99 |
|---|---|---|---|
| **alternating rows** (`±ε` duplex by duplex) | **0.883** | **0.0878 pN** | **0.860 pN** |
| **alternating columns** (`±ε` station by station) | **0.000** | **`3e−11 pN`** | **`3e−11 pN`** |
| one attachment off by `ε` | 0.473 → 0.365 | 0.0460 | 0.362 |
| one whole duplex row off by `ε` | 0.406 → 0.294 | 0.0392 | 0.291 |

Two findings:

1. **Which way a tolerance is correlated matters more than how big it is.** A scatter alternating *along* the helices restores exactly nothing at any amplitude, because it does not break the across-helix symmetry; the same amplitude alternating *across* them is the worst pattern in the set. A build rule follows: **make the attachment paths on one duplex differ from each other rather than from their neighbours' — and if they must differ, let the error be along the helix.**
2. **Scatter overtakes the solved edge effect at ε = 17 %**, and the two together are still 4 % + 7 % of the static share.

> **And even at ε = 0.99 — every second path at one per cent of its nominal stiffness, a coupling assembled about as badly as one can be while still existing — the peak crossover force is 0.860 pN, 12× below the 10 pN unzip band. The worst grid in the whole sweep, 15 × 3 under a merely uniform load, reaches 2.42 pN, 4.1× below it. For a coupling distributed over 45 paths the crossover path never becomes binding under any non-uniformity this programme can name** — the same conclusion `C-0024` reached in plane, reached here out of plane.

---

## §5 — the thermal channel is not a load path at all, and the penalty proves it

This is `CH-0033`'s ground 1 and the most surprising result of the task.

| `k_link` [pN/nm] | 10² | 10³ | 10⁴ | 10⁵ | 10⁶ |
|---|---|---|---|---|---|
| peak thermal crossover force RMS [pN] | 20.32 | 64.35 | 203.51 | 643.58 | 2035.18 |
| **÷ `√(k_BT k_link)`** | 0.9986 | 0.9999 | **1.0000** | **1.0000** | **1.0000** |
| ratio per decade | — | 3.166 | 3.163 | 3.162 | **3.162 = √10** |

The **static** force in the same link converges in the same penalty — `1.9e−3` then `1.7e−4` over `10³ → 10⁵`.
The thermal force does not, and cannot: a spring in thermal equilibrium stores `½k_BT`, so its force variance
is `k·k_BT`. **The rigid-constraint limit of a static constraint force exists; the rigid-constraint limit of a
fluctuating one does not.**

So the well-posed statement is a **joint** property, not a scheme property:

| reading of the crossover's vertical stiffness `k_v` | `k_v` [pN/nm] | `√(k_BT k_v)` [pN] |
|---|---|---|
| the hinge's own equivalent, `k_θ/d²` | 1.87 | **2.78** |
| one duplex rise in axial tension, `S/a` | 3235 | **115.8** |
| ~~`C-0009`'s penalty, read as if physical~~ | ~~10⁴~~ | ~~203.5 — meaningless~~ |

It is **identical on a 3 × 15 grid and on an 8 × 8 one to four decimal places** (203.5148 against 203.5141 pN),
so it cannot discriminate between attachment schemes; it is present under a **perfectly uniform** load, so it
is not "restored" by anything; and it is **already inside every per-path allowable in use**, because a rupture
force is measured at 300 K on the same kind of bond. **Adding a broadband thermal RMS to a static share
double-counts the motion the measurement contains.**

`T-9` is what would turn 2.78–115.8 pN into a number. It moves nothing static in this claim.

---

## `P5` — one scheme discharges all three duties, and the two axes of the grid are set by different ones

| duty | requirement | what the 3 × 15 coupling supplies | margin |
|---|---|---|---|
| output coupling (placement) | 33.333 pN/nm (`C-0017`, §3 arithmetic) | 33.333 pN/nm | 1.00 by construction |
| lateral confinement (`T-12`) | 0.4602 pN/nm per coordinate (`C-0014`) | 32.36 pN/nm | **70.3×** |
| yaw | 368.173 pN·nm/rad (`C-0014`) | 8206 pN·nm/rad | **19.0×** |
| hold-down (`T-13`), two-sided | 0.4602 pN/nm (`C-0023`) | 33.333 pN/nm | **72.4×** |

&nbsp;&nbsp;&nbsp;&nbsp;**One scheme, three duties, no extra part and no extra stiffness — `C-0017`'s and `C-0023`'s conclusions confirmed on the grid this task solves rather than cited.**

And the two axes of the grid are set by **different** constraints, which is new:

| axis | set by | evidence |
|---|---|---|
| **rows = 15** | the **load path** | every other row count restores 0.69–2.42 pN under a merely uniform load; 15 restores zero |
| **columns = 3** | **flatness** | 3 × 15 is the smallest one-row grid under 10 % of the stroke (4.9 %), against 13.5 % for 2 × 15 |
| *not* the columns | yaw | a **single** column of 15 already clears the yaw bound 10.1× |
| *not* the columns | the per-path allowable | 15 paths carry 6.67 pN each, already inside the 10 pN unzip band |

---

## `P4` — the allowables, corrected per `CH-0029`, and the count at which each crosses

Against `CH-0029`'s **length-dependent** shear allowable, reproduced here through `structure.ShearJointAllowable`
(18.80 pN at 8 bp, 34.81 at 16, 47.11 at 30, at Strunz's own 100 pN/s), and against the length-independent
10–15 pN unzip band:

| allowable | force [pN] | minimum paths, **static share alone** | minimum paths, **share + worst restored (0.332 pN)** | margin at 45 paths |
|---|---|---|---|---|
| single-duplex unzip, lower edge | 10.00 | **11** | **11** | **3.90×** |
| single-duplex unzip, upper edge | 15.00 | 7 | 7 | 5.85× |
| staple-domain shear at 8 bp | 18.80 | 6 | 6 | 7.34× |
| **staple-domain shear at 16 bp** | **34.81** | **3** | **3** | **13.58×** |
| staple-domain shear at 30 bp | 47.11 | 3 | 3 | 18.38× |
| nicked-duplex ceiling | 65.00 | 2 | 2 | 25.37× |

> **The restored force costs ZERO extra paths at every allowable, because 0.332 pN does not fall with the path count while `100/n` does — and by the time the share is small enough to matter the restored force is already negligible beside it. The binding per-path constraint is the static share, exactly as `C-0023` found at 8 paths.**
>
> `CH-0029`'s correction costs **27 %** of the margin at a realistic 16 bp joint (13.58× against the 18.72× a flat 48 pN would report) and **changes no verdict**.

---

## `P6` — the scheme does not zero the sheet; it moves the load into the duplex path

| path | peak under the solved load, 3 × 15 | allowable | margin |
|---|---|---|---|
| **crossover** | **0.1504 pN** | 10–15 pN unzip | **67×** |
| **duplex transverse shear** | **0.793 pN** | 65 pN nicked ceiling | **82×** |

The duplex path carries **5.3×** what the crossover path does — `C-0015`'s *"the two optima are at opposite
corners of the same 29 nm² cell"* seen at the scheme level. The trade is still favourable, and by design:
the crossover is judged against 10–15 pN and the duplex against 65 pN, so the 5.3× ratio buys a 1.2× *increase*
in the binding margin rather than a loss. Under the worst grid (1 × 15) the duplex shear reaches 1.83 pN, still
36× clear.

---

## The five verification gates

Executed as **22 gate-named tests** in `src/test/kotlin/coupling/UniformityBudgetTest.kt`, all green.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the tributary strip loads sum to the footprint integral of the pressure to `1e−12`; a uniform pressure puts `q·L_x·d` on every strip; **the reconstructed crossover-force functional dotted into a solved field reproduces the lattice's own `verticalForce`** — at a zero load case (absolutely, `< 1e−9 pN`, because both are meant to be zero and a relative test would compare their noise) and at a non-zero one (`< 1e−9` relative); a zero-width or non-finite collar throws | **PASS** |
| **2 — limiting cases** | a zero-depth collar is the uniform field exactly; **the collar field equals `structure.edgeTaperedPressure` at all 1681 sample points wherever both are defined**; a depth above one reverses the load at the rim, as `C-0022` reports and `edgeTaperedPressure` cannot represent; a uniform load restores exactly zero on **every** one-row grid at 1, 2, 3, 5, 8 and 15 columns; a load varying only along the helices restores exactly zero *while the duplexes bend*; a grid whose rows are not one per duplex restores a finite force under the same uniform load; **the 3 × 15 grid's rows are the duplex axes to `1e−12`** | **PASS** |
| **3 — symmetry and conservation** | the crossover forces on one interface sum to the lattice's own `shearAcrossInterface` at all 14 interfaces (`< 1 %` of the peak); **the restored force is exactly linear in the collar depth** (`5.000×` at 5× the depth, `1e−6`) and reverses with its sign; **the rigid-tile identity is recovered as the SHEET stiffens** — monotone over `×1 → ×10⁶`, landing within 1 % | **PASS** |
| **4 — numerical convergence** | **nested** subdivisions `1 ⊂ 2 ⊂ 4` (450/855/1665 dof): `2.1e−3` then `4.0e−5`; the link penalty `10³/10⁴/10⁵` on the **static** force: `1.9e−3` then `1.7e−4`; the strip quadrature refined `12/24/48` panels, tightening monotonically to `< 1e−6`; **and the THERMAL force asserted NOT to converge** — `√10` per decade to 5 %, which is the executable form of `CH-0033`'s ground 1; the 32 base-pair column phases swept, worth **3.9 %** | **PASS** |
| **5 — literature and upstream cross-check** | `C-0015`'s exact zero reproduced under its **own** point-load case at `7.8e−11 pN`; `C-0017`'s yaw stiffness 8206 against 8205 (`1.5e−4`) and its mean squared radius 253.59 against 253.55; `CH-0029`'s ladder reproduced at 8 bp (18.796 against 18.80) and 30 bp (47.107 against 47.11); `C-0022`'s total force gain reproduced at **+14.61 % against its +14.71 %** (`6.5e−3`), through a completely different integration of the same fitted collar; `C-0006`'s free-tile stroke reproduced at 4.907 nm against 4.95 | **PASS** |

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured.**
- **The crossover's VERTICAL/AXIAL compliance is a rigid penalty constraint**, inherited unchanged from `C-0009` and `C-0015`. The **static** force converges in it; the **thermal** force provably does not, and is reported as a bracket `2.78–115.8 pN` set by `T-9`'s missing number rather than as a value.
- **The load profile is `C-0022`'s and inherits its whole validity range**: mean field (`C-0005`'s one-loop correction is 123–214 % across this gap range, larger than every effect in this claim), point ions, a two-dimensional solve with the corner **bracketed rather than solved**, an **unsourced rim charge** worth 1.85× on the depth, and a gap filled with free buffer.
- **The interior pressure is `100 pN / 1600 nm²` and the collar sits on top of it**, so the total load exceeds 100 pN by `C-0022`'s edge gain. That is `C-0022`'s convention. A design normalised to a fixed total would move every force here by one common factor and no ratio at all.
- **Linear Winkler foundation** at `C-0001`'s secant, swept ×[0.25, 4]; `C-0001`'s stiffnesses are lower bounds per `CH-0001` and the soft corner is the worst one here.
- **The coupling is `n` IDENTICAL LINEAR springs.** `C-0023`'s flexure and hinge are exactly linear (secant = tangent), so this is their model; `C-0017`'s ssDNA-spacer path is strain-stiffening with `tangent/secant = 1.17`, which would raise every restored force by that factor and no verdict by any.
- **The scatter patterns are DETERMINISTIC design tolerances, not a random ensemble.** No distribution over assembly error is claimed, and none is available. The sensitivity is reported **per unit amplitude** so that a specified tolerance can be substituted without re-running.
- **A per-path allowable is a rupture force measured at 300 K at a stated loading rate**, so the thermal channel must not be added to a static share.
- **One layout** — `T-10`'s eight symmetrically centred columns — with the 32 base-pair phases swept at the design point only (3.9 %). `C-0015`'s ranking (seven columns beats eight) is **not** re-derived here.
- **No electrostatics is solved and no lateral coordinate is carried.** The dishing is out-of-plane only.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0022`'s solved `(depth, width, rim)` triples | 21 states | **CITED**, and **read from `gpd/results/T-3b-tile-edge-load-profile.json` at run time**, not transcribed |
| `C-0006`'s assumed taper | 50 % over 4 nm | **CITED**, superseded in sign and width by `CH-0025`; carried so the replacement is auditable |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| `C-0017`'s lateral and yaw by-products | 32.36 pN/nm; 8205 pN·nm/rad | **CITED**, and reproduced here to `1.5e−4` |
| `C-0014`'s lateral and yaw bounds | 0.460216 pN/nm; 368.173 pN·nm/rad | **CITED** |
| `C-0023`'s two-sided hold-down bound | 0.460216 pN/nm (`k_BT/σ²`) | **CITED** |
| the shear allowable's three constants | Strunz et al. *PNAS* **96**:11277 (1999) | **CITED, MEASURED**, via `structure.ShearJointAllowable` and `CH-0029` |
| unzip 10–15 pN | Essevaz-Roulet et al. (1997) | **CITED, MEASURED**, length-**independent** |
| 65 pN nicked ceiling | van Mameren et al. (2009) | **CITED, MEASURED** |
| `d = 2.69 nm` | Fischer et al. (2016), SAXS | **CITED, MEASURED** |
| `p = 32 bp` per interface, `0.34 nm` rise | Rothemund (2006); Douglas et al. (2009) | **CITED** |
| `k_θ = 2αB/(100a)` | Chen et al. (2014) SI | **CITED, FITTED**; `α = 1` here, its `[0.6, 1.2]` bracket not re-swept |
| `EI = 230`, `GJ = 460 pN·nm²` | CanDo (Kim et al. 2012) | **CITED, MODEL INPUTS, not measurements** |
| `k_f` | `C-0001`'s secant | **DERIVED**, itself under `CH-0001`, swept ×[0.25, 4] |
| `RIGID_PLATE_TOLERANCE = 0.10` | `T-5b` | **CITED CONVENTION**, not a physical threshold |
| §3's 100 pN, 3 nm, 40 × 40 nm, 10 nm | — | **CITED** |

Everything else — the collar field, the tributary strip loads, the rigid-tile identity, every restored force,
every concentration factor, the scatter sensitivities, the thermal variance and its `√k` scaling, the duty
margins, the allowable crossings, the flatness saturation and the convergence records — is **derived here in
code**, with `C-0009`/`C-0015`'s lattice and `C-0006`'s plate **re-run rather than tabulated**.

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the 3 × 15 grid turning out **not** to be one row per duplex | **no** | it is, to `3.6e−15 nm`, and the task splits into a comparison against equal-count *incommensurate* grids instead |
| 2 | the restored force not being **linear** in the collar depth | **no** | `5.000×` at 5× the depth, to `1e−6`, and it reverses sign with the depth |
| 3 | the rigid-tile identity failing in the rigid limit | **no** | monotone to under 1 % over six decades of sheet rigidity — **once the limit was taken on the SHEET rather than on the foundation**, which is where the first attempt failed |
| 4 | the restored force exceeding the per-path static share | **no** | 6.8 % of it at the design point, 15 % at the worst of 21 states, and 39 % at a 99 % assembly scatter |
| 5 | **the thermal crossover force failing to converge in the link penalty** | **YES, exactly as written, and it is the result** | `√10` per decade, `peak/√(k_BT k_link) = 1.0000`. `T-17`'s Plan said in advance that this "must then be reported as one and the quantity re-posed", and it is: `CH-0033` |
| 6 | a lateral or yaw by-product falling below `C-0014`'s bounds at some column count | **no** | 60.1× and 10.1–20.1× at every shape swept, including a **single** column |

A **seventh, undeclared** falsifier fired and is `CH-0034`: under `C-0022`'s solved load **no attachment count
is flat**, and the dishing saturates at 0.149 of the stroke between 45 and 225 attachments.

## Still open — named, not answered

1. **`T-9`, the crossover's vertical stiffness.** It moves nothing static here and it is the *entire* content of the thermal channel — a 42× bracket, 2.78 to 115.8 pN.
2. **Whether a real 45-path coupling's paths are equal to better than a few per cent.** The sensitivity is 0.883 pN per unit relative amplitude and the break-even against the edge effect is 17 %; nothing in the accessible literature gives an assembly tolerance for a hybridised staple extension.
3. **The corner of the tile**, which `C-0022` brackets rather than solves. The collar here is its minimum-margin construction, which counts a corner once.
4. **Whether the lever's own frame grounds the 45 springs independently.** A compliant common frame couples them, and the whole scatter analysis is written on independent grounds.
5. **The 32 base-pair phase is swept only at the design point** (3.9 %), and `C-0015`'s seven-versus-eight-column ranking is not re-derived under the solved load.

## Challenges

**Raises [`CH-0033`](../challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md)** against `C-0015`'s
fragility clause and `C-0017`'s second failure route. **No verdict, number or table of either moves**, and the
direction is favourable throughout: `C-0017`'s `P4` stands as written with a stronger warrant than it had.

**Raises [`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md)** against
`C-0006`'s, `C-0009`'s and `C-0015`'s attachment counts **as design numbers**. No count moves; what moves is
what a count buys.

**None stands against this claim.** The two ways it would fail:

1. **A measurement of the crossover's vertical compliance far below the rigid limit.** A soft crossover adds a load path the whole map does not have, would flatten the concentration factor and would put the thermal channel on a definite number. Every static force here is an upper bound in that direction, so the verdict would not reverse — but the *thermal* statement would become a real design constraint rather than a category note.
2. **A load non-uniformity with far more across-helix content than the electrostatics has.** Everything here is 0.15–0.33 pN because `C-0022`'s collar is 8.9 nm wide against a 40 nm tile; a load structured on the duplex pitch itself would restore far more, and nothing in the programme produces one. A patterned electrode would.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds
rather than overwriting it.
