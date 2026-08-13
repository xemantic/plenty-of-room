# T-19 — The attachment's entry topology: what a tether actually bonds to

| | |
|---|---|
| **Leaves** | `A8.2` (structural rigidity / joint stiffness budget), `A1.2` (the anchoring scheme it prices) |
| **Problem definition** | §6 tasks 5 and 5b; questions §4(f) and §4(g); parameters §3 |
| **Verification type** | in-silico (`C-0020`'s in-plane membrane lattice, loaded through four *entry topologies* instead of one, with the orthotropic shear-lag membrane run beside it) **+ logical** (a cut-equilibrium pigeonhole that bounds every topology before a matrix is assembled) **+ literature** (the joint's own allowable as a function of bonded length, from the primary source `C-0006` already traces) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Raised by** | [`C-0020`](../claims/C-0020-in-plane-shear-lag.md) (`T-15`), in its own validity range |

---

## Formulate

### Why this task exists

`C-0020`'s headline is that the in-plane transfer ratio is **exactly 1.0000** for a tether aligned with
the helices, and its validity range names the one assumption that produces it:

> **An attachment is one point on one duplex.** A tether bonded across two duplexes, or onto a crossover,
> would change the entry topology entirely — and since the binding path *is* the attachment, that is the one
> modelling choice this claim's headline rests on.

The point is sharper than a caveat. `η = 1` holds **because** the model gives the tether exactly one duplex
to enter through, which makes the attachment the most loaded member *by construction*. That is a
**sequence-design choice** — how many duplexes the staple extension hybridises to, over how many bases, and
in which geometry — and not a property of the sheet. Everything `C-0020` propagates into `C-0014` rides on
it: `A_eff = 48 pN`, `L_min(10 nm) = 33.5 nm`, the withdrawal of the ~227 nm assembly, and the
stroke-independent 54.9 pN normal preload.

### The quantity to produce, and the two allowables it must be judged against

The deliverable is `C-0020`'s own pair, unchanged in definition, per **entry topology**:

| symbol | definition |
|---|---|
| **`η` — transfer ratio** | peak force in **one** load path ÷ the **applied tether force** |
| `C` — concentration factor | the same peak ÷ the **equal share** over the paths available |

and the number the propagation consumes, `A_eff = min over path classes of (allowable ÷ η)`. `T-19` adds one
path class that `C-0020` did not carry, and it is the one the entry topology decides most directly:

- **the duplex axial force** at the attachment, judged against the nick in the loaded duplex — a hybridised
  staple domain in **shear** geometry (`C-0020`'s reading, 48 pN);
- **the crossover in-plane force**, judged against 10 pN unzip (`C-0009`'s conservative convention) and
  against 48 pN shear;
- **the duplex in-plane shear**, judged against the 65 pN nicked ceiling;
- **the tether's own joint** — *new here* — judged against `A_joint(n)`, the shear rupture force of a
  hybridised domain of `n` base pairs, which is what a *footprint* actually changes.

§4(f)'s 35–60 pN band is **not** used: it is a whole-cross-section number (`C-0006`).

### The topologies, as load introductions into `C-0020`'s lattice

Every one is the *same* tile, the *same* constants, the *same* self-equilibrated edge-to-edge chord, and
1 pN applied, so every force below is a ratio.

| | topology | what it is physically |
|---|---|---|
| **`E1`** | one point on one duplex | `C-0020`'s model. The control, reproduced rather than cited |
| **`E2`** | a band of `m` adjacent duplexes at one station | a staple extension hybridising to `m` duplexes. `m = 2` is `C-0020`'s "would halve it" |
| **`E3`** | onto a crossover | the two duplexes are already tied together there; necessarily an *interior* station, so it carries a one-point control at the **same** station |
| **`E4`** | `k` consecutive bases along one duplex | the realistic case: a hybridised extension has a footprint of 8–20 bp |

and each of them presented in **shear** or in **unzip** geometry, which `C-0020` records as a factor of 2.2
in the allowable and which the topology — not the sheet — decides.

### The split is not free, and both limits are solved

An `m`-duplex bond does **not** come with a 50/50 split written on it. Two limits bracket it and both are
computed:

- **prescribed equal split** — every bond carries `T/m`. This is the compliant staple, or `m` independent
  tethers, and it is what "halves the load" means when said informally;
- **compatible split** — a *rigid* staple, so all bonded points move together along the pull. With the
  mirror-symmetric chord this is exactly `m` springs in parallel between two rigid ends: `C a = λ 1`,
  `Σ a = 1`, with `C` the tile's own compliance matrix between the bonded stations, which is symmetric by
  Maxwell-Betti. The stiffest path takes more than its share, so **the compatible split is never better than
  the equal one**, and the gap is the answer to "is the halving exact".

Only the *aligned* case admits a compatible split without inventing a support: an unequal split at one end
of an oblique chord carries a **couple**, which nothing in the model reacts (`C-0010`'s exact lateral zero
means the tile would spin) and which the regularising bed would absorb at a stiffness eight orders below any
structural one. The oblique cases are therefore solved with the prescribed split, whose resultants are
collinear through the two **centroids** by construction, and that restriction is stated rather than hidden.

### The question, as a numeric target

1. `η` per path class for `E1`–`E4`, complete over the discrete design space of each, and the resulting
   `A_eff`;
2. whether `C-0020`'s "halving" is exact, whether it depends on **which** pair, and **what it costs in the
   crossover path** — i.e. at which `m` the crossover becomes the binding path;
3. the peak as a function of the **footprint** `k` in base pairs, and where it saturates;
4. the **oblique** case (`C-0020`'s worst placement, `η = 2.33`) under a two-duplex bond: relieved or worsened;
5. whether **layout** — all 32 base-pair column phases — is still worth **exactly nothing** on the binding
   path, as `C-0020` found (×1.0000, against `C-0015`'s ×1.43–1.60 out of plane);
6. the propagation, exactly as `C-0020` did it: `L_min = δ√(Sn/2A)` (and its exact counterpart
   `L = δ/√((1+A/S)² − 1)`) at §3's 3 nm and 10 nm strokes, the assembly footprint around a 40 nm tile, and
   the **stroke-independent normal preload** `n_t A √(2A/S)` — with an explicit statement of which way each
   topology moves the net design, given that `C-0021` has since shown a downward preload is *wanted*.

### Acceptance predicate

Discharged when all six hold.

1. All four entry topologies are solved on the same lattice with the same constants, and `E1` **reproduces
   `C-0020`'s `η = 1.0000` and `A_eff = 48.00 pN`** to `1e−4` — otherwise nothing below is a comparison.
2. The design space of each topology is swept **completely** and discretely: every band width `m = 1..15` at
   every band position, every crossover interface, every footprint `k = 1..20 bp`, each over all
   **32 base-pair column phases** — not a diagonal, not a continuous parameter that snaps to lattice sites.
3. The **cut-equilibrium bound** `η ≥ 1/D` is stated before the sweep, asserted as a runtime check over every
   design in it, and the distance of each topology from its own `1/m` limit is reported.
4. Both split limits are computed for the aligned band, the compatible one derived rather than assumed, and
   the bracket between them quoted as the answer to "is the halving exact".
5. The **continuum shear-lag membrane** is run beside the lattice for the topologies it can represent, with
   the excess quoted at stated stations, per `CLAUDE.md`'s standing rule.
6. `C-0014`'s / `C-0020`'s `L_min`, footprint and preload table is recomputed for every topology in both
   joint geometries, and the direction each topology moves the **net** design is stated, not just its `A_eff`.

### Units, locked

SI, scaled, per `P-2`: lengths nm, forces pN, energies pN·nm, stiffness pN/nm, stretch modulus pN, flexural
rigidity pN·nm². `k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer with Mg²⁺**. Transfer ratios
are dimensionless and reported per pN of applied tether force, the study applying exactly 1 pN.

### Geometry and sign conventions, fixed before deriving

`T-15`'s, restated and unchanged:

- `x` along the helices, `y` across them, origin at the centre of the footprint; footprint 40 × 40.35 nm,
  15 duplexes, `d = 2.69 nm`, 32 bp per interface, `S = 1100 pN`, `EI = 230 pN·nm²`.
- `u` along `x`, `v` along `y`, no `w`; a duplex axial force is **positive in tension**; a crossover force is
  the vector the connector exerts on the lower-`y` duplex and its **magnitude** is judged.
- **New here:** an *entry topology* is a set of **bonds**, each `(duplex, x, share)` with the shares summing
  to one, and a tether pair is two such sets whose **centroids** define the pull direction — which keeps the
  resultants collinear and the load case moment-free, exactly as `C-0020`'s single-point chord was.
- A bond station is placed at an exact base-pair multiple of the 0.34 nm rise and is made a **node**, so the
  axial force it introduces is resolved rather than averaged across an element. The footprint load is applied
  as `k` discrete bond forces at those stations, never as a traction sampled at one point.

### What is deliberately excluded

- **The staple's own elasticity.** It enters only through the *split*, and both limits are solved; a number
  for it would need a model of a two-domain staple's linker, which nothing in the literature supplies.
- **Out-of-plane coupling**, on the same flat-sheet argument `C-0020` uses, with the same caveat.
- **Any change to `k_s`**, `S`, `EI`, `d`, `p`, the phase machinery, or the allowables. Everything is
  `C-0006`/`C-0009`/`C-0015`/`C-0020`'s, so any difference reported here is the **entry topology** and
  nothing else.

---

## Plan

### The cheap bound, run first — and it settles two of the four topologies

Three things are available for the price of an equilibrium argument, before any matrix is assembled, and each
is asserted as a test rather than written in prose. The justification against cost is that they decide the
*sign* of every answer and reduce the lattice's job to quantifying "how close".

**(a) The cut-equilibrium pigeonhole.** `C-0020` verified (gate 3, item 5) that the duplex axial forces on a
cut sum to the net applied force crossing it. On a tile of `D = 15` duplexes the pigeonhole then gives

&nbsp;&nbsp;&nbsp;&nbsp;**`η ≥ 1/D = 0.0667` for any entry topology whatsoever**,

so the absolute ceiling on what entry-topology design can buy on the duplex path is `15 × 48 = 720 pN`, and
only a bond to **every** duplex reaches it — which is an edge clamp, not a tether. The design ladder is
bounded at both ends before anything runs.

**(b) The short-bond limit.** If the bond spans `m` duplexes at one station and the station is short compared
with the neighbour-exchange length `Λ_nn = 9.62 nm` (`C-0020`), then at the entry element only the bonded
duplexes have received anything, so **`η → 1/m`, attained exactly by an equal split**, and the compatible
split can only exceed it. So `C-0020`'s "halving" is an *upper bound on the benefit*, not an estimate.

**(c) The footprint is answered by (b) with `m = 1`.** Spreading a bond over `k` bases of **one** duplex
leaves `m = 1`: the duplex must still carry the whole tension somewhere inboard of the footprint, so the peak
cannot fall below `1 − O(ℓ_f/Λ_nn)` and **saturates immediately**. Whatever the footprint buys, it is not
bought on the sheet — it is bought on the **joint**, whose allowable `A_joint(n)` is what a longer domain
changes. That redirects the third topology from the lattice to the literature at zero cost.

**(d) The concavity of the joint allowable, from the primary source.** Strunz et al. (*PNAS* **96**:11277,
1999) — the source `C-0006` already traces the 48 pN to — measures 10, 20 and 30 bp duplexes pulled at
opposite 5′ ends, i.e. in **shear**, and reports that both the barrier separation `x` and `ln ν` scale
linearly with the number of base pairs. Their own single-barrier form then gives

&nbsp;&nbsp;&nbsp;&nbsp;`F(n) = k_BT B/x₁ + (k_BT/(x₁ n)) ln(u c x₁ n/(k_BT e^a))`,

which is **increasing and concave, saturating** — exactly `C-0006`'s recorded "shear rupture saturates with
domain length". Concavity alone settles the joint side of the split question with no numbers at all:
`m A(n/m) ≥ A(n)`, so **splitting a bond of fixed total bonded length across `m` duplexes never loses on the
joint, and strictly wins wherever the saturation bites**. The constants are fitted here from the paper's own
quoted values and the ratio is reported over the measured loading-rate range, so a wrong fit moves a
quantity, not a sign.

### The expensive calculation, and why this method and not another

**Chosen: extend `OrigamiMembrane` with an entry-topology load introduction and a compatible-split solver,
and re-run `C-0020`'s own sweeps through it.**

| method | cost | why not |
|---|---|---|
| the bounds (a)–(d) | milliseconds | **run first**; they settle `E4` and the sign of `E2`, but cannot give a crossover-path force or the oblique relief |
| **entry topologies on `C-0020`'s lattice + the continuum beside it** | ~2 minutes for the whole study | **chosen**; the lattice, its verification and its continuum control already exist and are unchanged, so the only new physics is the load introduction |
| a lattice with the staple modelled as an explicit two-domain element | ~2 days | needs a linker force-extension law nothing supplies; the two split limits bracket it exactly, and the bracket is the honest deliverable |
| oxDNA on the joint itself | days | would produce a rupture force at a simulation loading rate, i.e. a number less trustworthy than Strunz's measurement on the same object — the `P-3` posture, applied again |

The compatible split needs `m` extra solves per design and each solve is a back-substitution on a
factorisation the lattice already caches per phase, so the complete sweep is affordable and no design space
has to be sampled. The stiffness matrix is assembled straight into one array and contributions are exposed as
energies, per `CLAUDE.md`'s out-of-memory lesson; the footprint cases add nodes, so they are run at
`subdivisions = 1` with a nested `1 ⊂ 2 ⊂ 4` refinement quoted beside them rather than at a mesh that would
put a dense `n × n` matrix past the heap.

### What would falsify this approach

Stated in advance, per §5. The outcome of each is in Verify.

1. **`E1` failing to reproduce `C-0020`'s `η = 1.0000` and `A_eff = 48.00 pN`.** Then the load introduction is
   not a generalisation of `C-0020`'s and no comparison in this task means anything. Wired in as a runtime
   `check`, not only as a test.
2. **Any design returning `η < 1/D`.** Then cut equilibrium is broken and the assembly is wrong.
3. **The `m`-band peak falling below `1/m`.** Then the short-bond limit is wrong, or the split is not what it
   is declared to be.
4. **The compatible and prescribed splits differing by more than ~10 %.** Then the staple's own stiffness is a
   first-order input, the deliverable is a bracket rather than a number, and a two-duplex bond cannot be
   costed without an element nothing in the literature supplies.
5. **The footprint moving the peak by more than the shed over its own length.** Then the `m = 1` bound is
   wrong and the footprint is a sheet effect after all.
6. **Layout moving the binding path by more than the topology does.** Then `C-0020`'s "layout is worth exactly
   nothing" was an artefact of the one-point model and the design rule this task produces is the wrong one.
7. **The regularising bed carrying a non-negligible part of the load**, or **the mesh not converging** over
   nested `1 ⊂ 2 ⊂ 4` with the base-pair stations present.

---

## Execute

Code: `src/main/kotlin/structure/OrigamiMembrane.kt` — `EntryBond`, `EntryTopology` and its builders, the
tether-pair load introduction and the compatible-split solver, all **added**, nothing existing changed;
`src/main/kotlin/structure/JointAllowable.kt` — Strunz's single-barrier model as a function of bonded length
and loading rate; `src/main/kotlin/structure/EntryTopologyStudy.kt` — the study.
`ShearLag.kt`, `OrigamiGrillage.kt`, `CrossoverLayout.kt`, `OrigamiSheet.kt`, `Gen1Tile.kt`, `Cholesky.kt` and
`ResultRounding.kt` are **unchanged**.
Tests, written first: `src/test/kotlin/structure/EntryTopologyTest.kt` (16) and
`src/test/kotlin/structure/JointAllowableTest.kt` (9), each named for the gate it discharges.

```shell
./gradlew test -PbuildDirectory=build-t19
tools/study.sh structure.EntryTopologyStudyKt
```

Result: [`../results/T-19-attachment-entry-topology.json`](../results/T-19-attachment-entry-topology.json),
deterministic in filename **and** content, with `ResultRounding` applied at the serialisation boundary and
every extremum selected on the **rounded** value with the index as tie-break.

## Verify

Full gate table and per-item predicate discharge in [`C-0024`](../claims/C-0024-attachment-entry-topology.md).

## Result

Filed as [`C-0024`](../claims/C-0024-attachment-entry-topology.md).
