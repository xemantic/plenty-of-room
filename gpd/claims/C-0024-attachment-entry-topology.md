# C-0024 — The attachment's entry topology: the sheet has almost nothing to say about it, and the joint has everything

| | |
|---|---|
| **Task** | [`T-19`](../tasks/T-19-attachment-entry-topology.md) |
| **Leaves** | `A8.2` (structural rigidity / joint stiffness budget), `A1.2` (the anchoring scheme it prices) |
| **Verification type** | in-silico (`C-0020`'s in-plane membrane lattice, loaded through four **entry topologies** instead of one, with the orthotropic shear-lag membrane run beside it) **+ logical** (a cut-equilibrium pigeonhole that bounds every topology before a matrix is assembled) **+ literature** (the joint's own allowable as a function of bonded length, rebuilt from `C-0006`'s own primary source) |
| **Verdict** | **PASS** on all six items of the acceptance predicate. `C-0020`'s `η = 1.0000` is reproduced exactly and its headline survives — but it survives as **arithmetic**: an `m`-duplex bond enters at exactly `1/m`, the peak exceeds that by at most **4.7 %** anywhere in a 3840-design sweep, and the footprint is worth **nothing at 8 bp**. What the entry topology actually decides is the **joint's** allowable, which is length-dependent, concave, and has a **14.3 bp break-even** for splitting. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-19-attachment-entry-topology.json`, produced by `structure.EntryTopologyStudyKt`; models in `src/main/kotlin/structure/OrigamiMembrane.kt` (added: `EntryBond`, `EntryTopology`, `tetherLoads`, `compatibleShares`) and `src/main/kotlin/structure/JointAllowable.kt`; **25 gate-named tests** in `src/test/kotlin/structure/EntryTopologyTest.kt` (16) and `JointAllowableTest.kt` (9) |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.142 pN·nm`; 40 × 40.35 nm tile, 15 duplexes, 8 crossover columns; unit (1 pN) applied tether force, so every force below **is** a ratio; joint allowables at Strunz's own 100 pN/s, swept over his measured 16–4000 pN/s |
| **Raises** | [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) against `C-0006`'s per-path allowable **as consumed** by `C-0009`, `C-0014` and `C-0020` |
| **Consumes** | [`C-0020`](C-0020-in-plane-shear-lag.md) (the lattice, the continuum, the cut-equilibrium invariant, every structural constant, the `L_min` and preload propagation), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the allowables **and their lengths**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the phase machinery and the "sweep shapes, not diagonals" discipline), [`C-0014`](C-0014-lateral-confinement.md) (the tether geometry), [`C-0021`](C-0021-zero-bias-resting-position.md) (that a downward preload is *wanted*, and how much), [`C-0010`](C-0010-tile-positional-variance.md) (the exact lateral zero, which is what makes the load case moment-free) |

---

## Claim, in one line

**The one-point-on-one-duplex model is not what `C-0020`'s `η = 1` rests on — cut equilibrium is: on a `D`-duplex tile no entry topology can put less than `1/D` into its worst duplex, and a bond spanning `m` duplexes enters at exactly `1/m` with the peak above it by at most 4.7 % over every band width, position and column phase; so the sheet's answer to "what does a tether bond to" is pure arithmetic, the crossover path *never* becomes binding because splitting the load relieves the interfaces faster than the duplexes, spreading a bond over a footprint buys **nothing** at 8 bp because the duplex must still carry the whole tension inboard of it — and the entire design content of the entry topology sits in the **joint**, whose shear allowable is 18.8 pN at 8 bp and 47.1 pN at 30 bp against the flat 48 pN in use, is concave, and therefore rewards splitting a bond across two duplexes above a total bonded length of 14.3 bp and punishes it below.**

---

## The cheap bound, run first, and it settles two of the four topologies

| bound | value | what it costs |
|---|---|---|
| **the pigeonhole on the cut** — the duplex axial forces on a cut sum to the applied force (`C-0020` gate 3), so some duplex carries at least `1/D` | **`η ≥ 0.0667`** | one paragraph |
| the same, as an allowable — the **ceiling** on what entry-topology design can ever buy on the duplex path | **`A_eff ≤ 720 pN`**, reached only by a bond to **every** duplex, which is an edge clamp and not a tether | free |
| **the short-bond limit** — no crossover sits on the rim, so the entry element of each bonded duplex carries exactly its own share | **`η → 1/m`, exactly**, and `C-0020`'s *"a two-duplex bond would halve it"* is an **upper bound on the benefit**, not an estimate | free |
| **the footprint is the `m = 1` case** | the duplex still carries the whole tension inboard of the footprint, so **the footprint is not a sheet variable at all** and the question moves to the joint | free — and it redirects a whole topology from the lattice to the literature |
| **concavity of the joint allowable** | `m A(n/m) ≥ A(n)` wherever `A` is concave, so splitting never loses on the joint **above** its break-even | free, from `C-0006`'s own recorded *"shear rupture saturates with domain length"* |

Everything the lattice then had to do was quantify *how close* each topology gets to its own bound, and the answer is **within 4.7 %, everywhere**.

---

## 1. The four topologies, at the nominal layout, per pN of applied tension

| | largest share | `η` duplex axial | `η` crossover | peak ÷ share | `A_eff` (`C-0020` convention) |
|---|---|---|---|---|---|
| **`E1` one point on one duplex** (`C-0020`, reproduced) | 1.0000 | **1.0000** | 0.1826 | **1.0000** | **48.00 pN** |
| **`E2` two adjacent duplexes**, equal split | 0.5000 | **0.5000** | **0.0969** | 1.0000 | **96.00 pN** |
| `E2` three adjacent duplexes | 0.3333 | 0.3375 | 0.0575 | 1.0126 | 142.21 |
| `E2` four adjacent duplexes | 0.2500 | 0.2538 | 0.0412 | 1.0154 | 189.10 |
| `E2` two duplexes, **rigid** staple | 0.5000 | 0.5000 | 0.0969 | 1.0000 | 96.00 |
| **`E3` onto a crossover** (interior station, all 14 interfaces) | 0.5000 | 0.5000–0.5190 | 0.0697–0.0952 | ≤ 1.038 | **92.5–96.0 pN** |
| `E3` **control**: one point on one duplex at the *same* station | 1.0000 | 0.9011–0.9264 | 0.1215–0.1822 | — | 51.8–53.3 |
| **`E4` 8–20 bp footprint on one duplex**, worst column phase | 1.0000 | **1.0000 / 0.9143** | 0.18 | — | **48.00 / 52.5 pN** |

&nbsp;&nbsp;&nbsp;&nbsp;**`E1` reproduces `C-0020` to the last digit — asserted as a runtime `check`, because without it nothing here is a comparison.**

## 2. The complete band ladder — 3840 designs, and the answer is arithmetic

Every band width `m = 1..15`, at every one of its `16 − m` positions, at all **32 base-pair column phases**:

| `m` | designs | `1/m` | worst `η` | worst ÷ `1/m` | `A_eff` worst..best | worst `η` crossover | **layout span** | position span |
|---|---|---|---|---|---|---|---|---|
| **1** | 480 | 1.0000 | **1.0000** | **1.0000** | 48.00 .. 48.00 | 0.1862 | **×1.0000** | ×1.0000 |
| **2** | 448 | 0.5000 | 0.5202 | 1.0404 | **92.27 .. 96.00** | 0.0978 | ×1.0041 | ×1.0404 |
| 3 | 416 | 0.3333 | 0.3490 | 1.0469 | 137.55 .. 142.21 | 0.0578 | ×1.0172 | ×1.0339 |
| 4 | 384 | 0.2500 | 0.2595 | 1.0381 | 184.95 .. 189.10 | 0.0412 | ×1.0094 | ×1.0164 |
| 8 | 256 | 0.1250 | 0.1302 | 1.0417 | 368.64 .. 375.54 | 0.0201 | ×1.0086 | ×1.0068 |
| 14 | 64 | 0.0714 | 0.0744 | 1.0416 | 645.14 .. 650.52 | 0.0087 | ×1.0083 | ×1.0000 |
| **15** | 32 | 0.0667 | **0.0667** | **1.0000** | 720.00 .. 720.00 | **0.0000** | ×1.0000 | ×1.0000 |

Three things fall straight out.

- **The ladder is `1/m` to within 4.7 %, and the excess saturates at ×1.0416.** It is not zero because the connector arm couples each duplex's in-plane rotation into the interface sliding, so a duplex inside the band picks up a little from its bonded neighbours — the same term `C-0020` found worth ×1.66 in the loaded duplex's share, seen here at a hundredth of the size.
- **The crossover path never becomes binding.** Its closest approach anywhere in the 3840 designs is **×1.07** the duplex-limited tension, and it recedes as the band widens: splitting the load across duplexes relieves the *interfaces* faster than it relieves the *duplexes*, because both bonded duplexes move together and the interface between them stops sliding. **A two-duplex bond costs nothing in the crossover path — it pays 1.88× there.**
- **Both ends of the ladder are exact.** `m = 1` and `m = D` have no interior interface to pick anything up from, so they sit on `1/m` identically.

## 3. Is the halving exact? Yes to 4 %, and the staple's own stiffness cannot change that

The split of an `m`-duplex bond is not written on it. Two limits bracket it and both are solved:

| | what it is | two-duplex split | peak |
|---|---|---|---|
| **prescribed** | a compliant staple, or `m` independent tethers | 0.500 / 0.500 | 0.5000–0.5202 |
| **compatible** | a **rigid** staple, so all bonds move together — exactly `m` springs in parallel between two rigid ends, `C a = λ1`, `Σa = 1`, with `C` the tile's own compliance matrix, symmetric by Maxwell-Betti | **0.5569 / 0.4431** at worst (duplexes 0–1, phase 6 bp) | ×**1.0705** |

&nbsp;&nbsp;&nbsp;&nbsp;**The two limits bracket the answer to 11.4 % in the share and 7.1 % in the peak, so a two-duplex bond can be costed without the one thing the literature does not supply — a two-domain staple's own force-extension law.**

The rigid split is never *better* than the equal one (the stiffest path takes more than its share, by convexity), and that is asserted over all 448 designs.

**Which pair barely matters**: over every adjacent pair and every column phase the equal-split peak spans 0.5000–0.5202, i.e. **4.0 %**. The worst pairs are at the rim, where one bonded duplex has an unloaded neighbour on one side only.

## 4. Bonding onto a crossover adds nothing the two-duplex bond does not already give

A crossover is a place where two duplexes are already tied together, so a tether bonded to it is a two-duplex
bond that is **forced to sit at an interior station** — the columns lie strictly inside the footprint, so the
chord shortens from 40 nm to 32.6 nm at the nominal phase.

| | `η` axial | `η` crossover | `A_eff` |
|---|---|---|---|
| bonded onto the crossover, 14 interfaces | 0.5000–0.5190 | 0.0697–0.0952 | **92.5–96.0 pN** |
| **control**: one point on one duplex, *same two stations* | 0.9011–0.9264 | 0.1215–0.1822 | 51.8–53.3 pN |

&nbsp;&nbsp;&nbsp;&nbsp;**The crossover bond is numerically the two-duplex bond (`A_eff` 92.5–96.0 against 92.3–96.0 at the rim). The ×1.74–1.81 it shows over its control is the two-duplex halving less the 10 % the control itself gains from entering past the first crossover column.** The crossover contributes nothing mechanically; it is a *place*, not a mechanism.

## 5. The footprint is not a sheet variable, and the layout sweep is what proves it

A bond spread over `k` consecutive base pairs of **one** duplex leaves `m = 1`.

| | 1 bp | 8 bp | 16 bp | 20 bp |
|---|---|---|---|---|
| `η` at the nominal phase | 1.0000 | 0.9029 | 0.9090 | 0.8622 |
| **worst over all 32 column phases** | 1.0000 | **1.0000** | — | **0.9143** |
| the *same single-point* attachment, read at the inboard end of the same footprint | 1.0000 | 0.9006 | 0.9006 | 0.7195 |
| the joint's shear allowable at 100 pN/s | — | **18.80** | **34.81** | **39.53 pN** |

- **The apparent relief is the load shed past the first crossover column**, not the entry topology: at the nominal phase that column sits 0.96 nm from the rim, and the single-point control read at the same station carries the same 0.90.
- **The column phase is a design variable** (`C-0015`), and at the phases that put the first column further from the rim than the footprint is long the whole bond enters before anything can shed: **the worst-case 8 bp footprint is `η = 1.0000` exactly**, and the worst-case 20 bp footprint 0.9143. A footprint buys at most 8.6 %, and at 8 bp it buys **nothing**.
- **How the joint transfers its load internally does not rescue it either.** Loading only the two ends of the footprint — the shear-lag limit of an overlap joint, which is what a real hybridised domain does — gives 0.9041 at 8 bp and 0.7386 at 20 bp against the uniform 0.9029 and 0.8622, i.e. the same statement with the same phase caveat.

&nbsp;&nbsp;&nbsp;&nbsp;**Everything a footprint buys, it buys on the joint: 8 → 20 bp is ×2.10 in the allowable and ×1.00 on the sheet.**

## 6. The joint's own allowable, rebuilt from `C-0006`'s own primary source

`C-0006` records the shear allowable as *"48 ± 2 pN (**30 bp**)"* and records that *"shear rupture saturates
with domain length (~70 pN asymptote)"*. Downstream the length is dropped. It cannot be: Strunz et al. publish
all three constants of their single-barrier fit, and assembled from those alone the model reproduces **both**
of their headline numbers.

| check | model | paper |
|---|---|---|
| 30 bp at 100 pN/s (50 nm/s × their own 2 pN/nm) | **47.11 pN** | **48 ± 2 pN** |
| saturation, loading-rate free | **68.12 pN** | *"1.2 k_BT/0.7 Å ≈ 70 pN"* |
| the whole measured envelope, 10–30 bp × 16–4000 pN/s | 18.6 → 52.6 pN | *"varied from 20 to 50 pN"* |

| bonded length | 4 | **8** | 12 | **16** | 20 | 24 | **30** | 32 | 40 | ∞ |
|---|---|---|---|---|---|---|---|---|---|---|
| shear allowable at 100 pN/s [pN] | 3.6 | **18.8** | 28.3 | **34.8** | 39.5 | 43.1 | **47.1** | 48.2 | 51.6 | **68.1** |

**Splitting a bond therefore has a break-even length**, because dividing the load by `m` also shortens each
domain by `m`, and the two run against each other:

| loading rate | 2-way break-even | 3-way break-even |
|---|---|---|
| 16 pN/s (Strunz's slowest) | 19.13 bp | 24.68 bp |
| **100 pN/s (the rate the 48 pN was measured at)** | **14.27 bp** | **18.30 bp** |
| 4000 pN/s (his fastest) | 2.96 bp | 3.69 bp |

&nbsp;&nbsp;&nbsp;&nbsp;**Split the bond across two duplexes when the total bonded length exceeds ~14 bp; keep it on one when it does not. A realistic 8–20 bp staple extension straddles that line.**

In **unzip** geometry the same question has a different answer, and it is exact: unzipping opens one base pair
at a time, so its allowable is **length-independent** and splitting multiplies capacity by exactly `m`. The
topology and the geometry are not independent design choices — the geometry decides whether splitting is a
weak win or a linear one.

## 7. The oblique case is relieved, and only in the part that is a share

`C-0020`'s worst oblique overshoot is reproduced exactly — **`η = 2.3290`** over the complete 15 × 15 × 32
sweep, the same number to four decimals.

| duplex offset | angle | `η` one point | `η` two-duplex bond | relief |
|---|---|---|---|---|
| **0 (aligned)** | 0.00° | 1.0000 | **0.5202** | **×1.922** |
| 4 | 15.06° | 1.5295 | 1.1394 | ×1.342 |
| 8 | 28.28° | 1.9838 | 1.6170 | ×1.227 |
| **13** | 41.16° | **2.3290** | **2.0039** | **×1.162** |

&nbsp;&nbsp;&nbsp;&nbsp;**A two-duplex bond is worth ×1.92 aligned and only ×1.16 at the worst oblique placement.** The overshoot is a *moment* reacted by the crossovers as an axial couple, and a band at one station barely changes its arm; what the band divides is the axial *share*, and only that part of the overshoot follows. **The alignment rule is not relaxed by the entry topology — it is made more important, because the topology's benefit is largest exactly where the alignment is right.**

## 8. Layout — `C-0020`'s "exactly nothing" holds, and acquires a second reason

| | crossover path | **duplex-axial path (binding)** |
|---|---|---|
| one point on one duplex, 480 designs | ×2.72 (`C-0020`) | **×1.0000 — exactly** |
| two-duplex bond, prescribed split, 448 designs | ×1.4 | ×1.0041 |
| two-duplex bond, **rigid** split (the largest share) | — | ×1.1137 |

A prescribed split makes the entered share **arithmetic**, so no arrangement of crossovers can touch it. Only
the *rigid* split lets the layout back in, through the compliance matrix, and it is worth ×1.11 — still below
`C-0015`'s ×1.43–1.60 out of plane and well below the topology's own ×2.00.

## 9. The continuum beside it, and a new observation

The orthotropic shear-lag membrane is run beside the lattice on the same footprint with the same constants,
the band taken as a superposition of chords and each duplex force integrated over its **tributary strip**
(never sampled on the axis — that broke the sum rule by 130 % in `T-15`).

| `k_s` | station | `m = 1` | `m = 2` | `m = 4` |
|---|---|---|---|---|
| 2.0 (the continuum's own premise holds) | `x = 0`, loaded duplex | −0.011 | −0.003 | −0.001 |
| **64.7 (nominal)** | `x = 0`, loaded duplex | **+0.033 (×1.101)** | **+0.016 (×1.059)** | **+0.005 (×1.028)** |
| 64.7 | `x = 5`, loaded duplex | ×1.232 | ×1.170 | ×1.151 |

&nbsp;&nbsp;&nbsp;&nbsp;**The discreteness excess falls as the bond spreads** — ×1.10 → ×1.06 → ×1.03 at the load station — because a load spread over several duplexes is closer to the continuum's own smoothness assumption. The continuum still cannot produce the deliverable: it diverges logarithmically at a point load, exactly as in `C-0020` and as `C-0006`'s plate did at a discrete anchor.

---

## What this does to `C-0014` / `C-0020` — the propagation, in full

`L = δ/√((1 + A/S)² − 1)`, the exact counterpart of `L_min = δ√(Sn/2A)`; four tethers; preload
`F_z = n_t A √(2A/S)`, **independent of the stroke**.

| design | bp per bond | `A_eff` | `L_min(3 nm)` | **`L_min(10 nm)`** | assembly | **4-tether preload** | of the §3 target |
|---|---|---|---|---|---|---|---|
| ~~`C-0020` as filed, 48 pN assumed~~ | ~~unstated~~ | ~~48.00~~ | ~~10.05~~ | ~~33.49 nm~~ | ~~107 nm~~ | ~~54.9 pN~~ | ~~55 %~~ |
| one point, realistic 16 bp extension | 16 | 34.81 | 11.83 | **39.44 nm** | 119 nm | 34.2 pN | 34 % |
| one point, 30 bp extension | 30 | 47.11 | 10.14 | 33.81 nm | 108 nm | 53.4 pN | 53 % |
| one point, whole 32 bp staple on one duplex | 32 | 48.00 | 10.05 | 33.49 nm | 107 nm | 54.9 pN | 55 % |
| **two duplexes, the same 32 bp staple split** | **16 each** | **69.62** | **8.30** | **27.67 nm** | **95 nm** | 94.6 pN | 95 % |
| two duplexes, 30 bp each | 30 each | 94.21 | 7.10 | 23.66 nm | 87 nm | 146.7 pN | 147 % |
| four duplexes, the same 32 bp staple split | 8 each | 75.18 | 7.98 | 26.60 nm | 93 nm | 105.8 pN | 106 % |
| four duplexes, 16 bp each | 16 each | 139.24 | 5.78 | 19.27 nm | 79 nm | 256.5 pN | 257 % |
| one point, joint in **unzip** | any | 10.00 | 22.20 | 73.99 nm | 188 nm | 5.4 pN | 5 % |
| two duplexes, both joints in **unzip** | any | 20.00 | 15.66 | 52.20 nm | 144 nm | 15.1 pN | 15 % |
| four duplexes, all joints in **unzip** | any | 40.00 | 11.02 | 36.75 nm | 114 nm | 42.0 pN | 42 % |

### Which way each topology moves the *net* design

`C-0020` found the minimum-length tether's normal preload to be stroke-independent and 55 % of the §3 target,
and treated it as the price of the shorter tether. `C-0021` has since shown that a **downward** preload is
exactly what the stack lacks — but it needs only **1.381 pN** (`k_BT`/3 nm, read as a mean excursion), and
`C-0014`'s eight substrate tethers already supply 4.6–9.4 pN. So:

- **the preload at `L_min` is 4–186× the requirement** across the whole design table (25–186× for every shear
  design), **and therefore a tax, not a benefit.** A topology that raises `A_eff` shortens the admissible
  tether and raises that tax as `A^{3/2}`;
- **but `L_min` is a corner of the design space, not a design.** At any `L ≥ L_min` the tension and the preload
  are what the length makes them, whatever the topology; the length that delivers exactly `C-0021`'s 1.381 pN
  at the 10 nm stroke is **116.6 nm** for four tethers, which is longer than every `L_min` in the table;
- so **the entry topology's value is that it makes more of the length axis admissible**, and the design should
  sit where the preload is wanted rather than where the tether is shortest. Read that way:

| topology | direction on the net design |
|---|---|
| **two-duplex bond, aligned, shear geometry** | **favourable**: ×2 on the sheet, ×1.44 on the joint at a 32 bp budget, ×1.88 relief on the crossovers, no layout exposure, no cost in stroke or footprint. The only price is that the *shortest* admissible tether now carries 95 % of the target force in preload — which is a corner nobody has to occupy |
| bonding onto a crossover | **neutral**: identical to a two-duplex bond, minus a shorter chord. Not worth constraining a layout for |
| a longer footprint on one duplex | **favourable, but only through the joint**: ×2.10 in the allowable from 8 to 20 bp and ×1.00 on the sheet |
| splitting a bond **below** ~14 bp total | **unfavourable**: the joint loses more than the sheet gains — ×2.58 at an 8 bp staple split in two |
| presenting the joint in **unzip** | **catastrophic and free to avoid**: ×4.8 in `A_eff`, `L_min(10 nm)` 74 nm and an assembly of 188 nm. `C-0006`'s largest design lever, unchanged |
| **any misalignment** | **dominant**: ×11.75 in `A_eff` (`C-0020`), against the ×2 the best topology buys. **Alignment first, topology second** |

---

## The five verification gates

Executed as tests: `src/test/kotlin/structure/EntryTopologyTest.kt` (16) and `JointAllowableTest.kt` (9),
**25 tests, all green**, each named for the gate it discharges, plus six runtime `check`s in the study.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | an entry topology's shares are a partition of the tension and are validated; the load introduction applies exactly the tension and **zero net force**; a base-pair footprint lands its bonds on exact multiples of the 0.34 nm rise and every one of them **is a node** of the lattice built with them; the joint model's force rises by exactly `k_BT/x` per e-fold of loading rate | **PASS** |
| **2 — limiting cases** | see below | **PASS** |
| **3 — symmetry and conservation** | the duplex axial forces on a cut sum to the applied force for **every** topology (`1e−4`); **no entry topology goes below the pigeonhole floor `1/D`** (asserted as a runtime `check` over all 3840 ladder designs); the compatible split equalises the extension of every bonded path (`1e−6`); a rigid bond never splits better than an equal one, over all 448 designs | **PASS** |
| **4 — numerical convergence** | nested mesh `1 ⊂ 2 ⊂ 4`: the two-duplex band's peak axial is `0.4999992` at all three and its peak crossover moves `3e−6`; the 8 bp footprint moves `7e−5` with the base-pair stations present; the regularising bed carries `< 2e−13` of the applied force at every point, asserted as a runtime `check`; the entry-element identity tightens by four decades when the bed is softened by four decades, which is what makes the residual **attributable** rather than excused | **PASS** |
| **5 — literature cross-check** | the joint model, built from Strunz's published constants **alone**, reproduces his 48 ± 2 pN at 30 bp (47.11) and his ≈70 pN asymptote (68.12); his 10 bp duplex lands inside the abstract's own 20–50 pN band over his measured rates; the fitted-parameter bracket (`α = 3 ± 1`, `β = 0.5 ± 0.1`) moves the numbers but not the sign of the split gain; **`E1` reproduces `C-0020`'s `η = 1.0000` and `A_eff = 48.00 pN`** and the oblique worst reproduces its **2.3290** to four decimals; lattice and continuum agree to `≤ 0.014 pN` of the applied force where the continuum's premise holds | **PASS** |

### Gate 2 — the limiting cases that license the model

| statement | outcome |
|---|---|
| one point on one duplex gives `η = 1` | **1.0000**, to `1e−5` |
| an `m`-duplex bond with an equal split **enters** at `1/m` | exact to `1e−4`, and to `1e−8` with the bed softened by four decades |
| the peak of an `m`-duplex bond sits **just above** `1/m` and never below | `1.0000 ≤ peak·m ≤ 1.047` over the whole ladder |
| a bond to **every** duplex attains the pigeonhole floor `1/D` | 0.0667 to `1e−4`, **and stores exactly nothing in any crossover** (`< 1e−9`) — every duplex is strained identically |
| a **mirror-symmetric** two-duplex strip splits a rigid bond exactly in half | 0.5 / 0.5 to `1e−9`, a property of the solver rather than of the tile |
| a **centred** three-duplex bond splits symmetrically about its middle duplex | to `1e−9` |
| a two-duplex bond **relieves** the crossovers rather than loading them | 0.1826 → 0.0969 |
| spreading a bond along one duplex cannot relieve it | the far end stays at 1.0000 for every footprint |
| the joint allowable saturates, at a **loading-rate-free** value | `1e−3` at `10⁵` bp, at both ends of the measured rate range |
| splitting a bond has a break-even length and it is not zero | 14.27 bp at the reference rate, bracketed 2.96–19.13 over the measured rates |

### The falsifiers, and whether they fired

| falsifier | fired? | outcome |
|---|---|---|
| 1. `E1` failing to reproduce `C-0020`'s `η = 1.0000` and `A_eff = 48.00` | **no** | exact, and wired in as a runtime `check` |
| 2. any design returning `η < 1/D` | **no** | asserted over all 3840 ladder designs; the closest approach is `m = 15`, which sits *on* the floor |
| 3. the `m`-band peak falling below `1/m` | **no** | it sits above it by 0–4.7 %, and the mechanism (the connector arm's rotation coupling) is `C-0020`'s own term seen at a hundredth of its size |
| 4. the two split limits differing by more than ~10 % | **borderline, and reported as a bracket** | 11.4 % in the share, 7.1 % in the peak. Above the declared 10 % in the share, so the two-duplex bond is quoted as `η = 0.500–0.557` rather than as 0.500 — but not enough to need a staple elasticity model, which was the point of the falsifier |
| 5. the footprint moving the peak by more than the shed over its own length | **no — and the control is what shows it** | the single-point attachment read at the inboard end of the same footprint carries the same 0.90, and over the complete phase sweep the worst-case 8 bp footprint is exactly 1.0000 |
| 6. layout moving the binding path by more than the topology does | **no** | ×1.0041 (prescribed) and ×1.1137 (rigid) against the topology's ×2.00 |
| 7. the bed carrying a non-negligible load, or the mesh not converging | **no** | `< 2e−13`; `3e−6` and `7e−5` over nested `1 ⊂ 2 ⊂ 4` |

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured.** No in-plane force in a loaded
  origami sheet has ever been measured.
- **Everything structural is `C-0020`'s, unchanged** — the same sheet, the same `k_s` (still **derived**, not
  measured, and still the one undetermined input), the same connector arm, the same phase machinery, the same
  footprint. Any difference reported here is the **entry topology** and nothing else. `C-0020`'s whole
  validity range therefore applies unchanged, including its `k_s` sweep, its linearity and its absence of
  out-of-plane coupling.
- **The compatible split is defined for an ALIGNED pull only.** An unequal split at one end of an oblique
  chord carries a **couple** that nothing in this model reacts — `C-0010`'s lateral stiffness is exactly zero —
  and the regularising bed would absorb it at a stiffness eight orders below any structural one. The oblique
  cases are solved with the prescribed split, whose resultants are collinear through the two centroids.
- **The staple's own elasticity is not modelled.** The two split limits bracket it at 11.4 %.
- **The joint allowable is Strunz et al.'s single-barrier model with THEIR published constants**, used inside
  their measured 16–4000 pN/s. The Evans-Ritchie form has **no equilibrium plateau** and must not be
  extrapolated to zero loading rate; every design decision here rests on a **ratio** at a fixed rate, and the
  break-even is quoted across the whole measured range. Below ~4 bp the model returns a negative force, which
  is the form failing and is flagged in the result file rather than clipped.
- **The joint allowable is measured on free oligonucleotides pulled at opposite 5′ ends**, not on a staple
  domain inside a sheet, and **no measurement of the latter exists**. A domain inside a sheet is flanked by
  crossovers and neighbours; whether that raises or lowers its rupture force is not established anywhere.
- **The `1/m` ladder assumes the bond is short compared with the neighbour-exchange length** `Λ_nn = 9.62 nm`.
  A band is one station wide, so this is exact for `E2`/`E3`; for `E4` it is precisely the statement that
  fails, which is why the footprint's relief is reported as shedding.
- **`C-0020`'s "one attachment" is still one attachment.** A scheme of four or eight tethers is four or eight
  of these, and the superposition of their fields is not evaluated here — `C-0014`'s per-anchor treatment is
  consumed unchanged.
- **The preload direction consumes `C-0021`**, whose own validity range (a `C-0003` layer at one grafting
  density per height, and a 1.22× exposure at 5 nm from `C-0016`) travels with every statement above about
  what the preload is *for*.

## Numbers that are cited rather than derived

- `S = 1100 pN`, `EI = 230 pN·nm²`, `d = 2.69 nm`, `p = 32 bp`, `0.34 nm` rise — **CITED**, via `C-0020`,
  unchanged and re-derived nowhere.
- `k_s = 64.7 pN/nm` — **DERIVED** from a cited and fitted construction, `C-0020`'s, and **not measured**.
  Held at its nominal value here because `C-0020` showed the aligned answer invariant over four decades of it.
- **The joint allowable's three constants** — **CITED, MEASURED**: Strunz, Oroszlan, Schäfer & Güntherodt,
  *PNAS* **96**:11277 (1999), Eq. 1 (Evans-Ritchie), Eq. 2 (`ν = 10^(α−βn)`, `α = 3 ± 1`, `β = 0.5 ± 0.1`),
  Eq. 3 (barrier separation, 0.7 Å per bp with a 7 Å offset), and their own 2 pN/nm linker elasticity for the
  loading-rate conversion. Read from the paper's full text, not from a summary.
- 10–15 pN unzip — **CITED, MEASURED**, Essevaz-Roulet et al. (1997), and **length-independent**.
- 65 pN nicked ceiling — **CITED, MEASURED**, van Mameren et al. (2009).
- `1.381 pN` hold-down scale — **CITED**, `C-0021`, `k_BT`/3.0 nm as a mean excursion.
- The 40 nm footprint, 100 pN target, 3 and 10 nm strokes — §3.

## Challenges

**Raises [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md)** against `C-0006`'s
per-path shear allowable **as consumed** by `C-0009`, `C-0014` and `C-0020`. No verdict of any of them moves.

None stands against this claim. The way it would fail is through the **joint allowable's transfer**: a
hybridised staple domain inside a sheet is not a free oligonucleotide on an AFM tip, and if a domain flanked
by crossovers ruptures at a materially different force — or if the length dependence flattens inside a sheet —
then §6 and the whole break-even argument move, while §§1–5 and 8–9 do not. A result contradicting this claim
should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
