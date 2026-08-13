# T-67 — Does a 90° scaffold or staple routing between a sheet duplex and a normal standoff exist at all?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Problem definition** | §1 (the stack); §3 (100 pN, 3 nm **acceptable**, 10 nm **desired**, 40 × 40 nm, 2 mM); §4(f); §5, §7 (process) |
| **Verification type** | **logical + in-silico** (a counting theorem about strand termini, then B-form backbone geometry solved for the junction's realisable links, then `C-0028`'s own design pipeline re-run on the base that results) **+ literature** (whether any published routing turns a duplex 90° out of a sheet, with a `read directly` / `abstract only` / `not found` flag on every statement) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured, and the geometry is not demonstrated either.** |
| **Status** | Executed, verified, filed as claim [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) |
| **Consumes** | [`C-0028`](../claims/C-0028-standoff-base-joint.md) (`StandoffBase`, `basedNormalStandoff`, the sway-column eigenvalue, `baseRotationalStiffnessThreshold`, the six predicates, the `B2` design — **re-run as a library**), [`C-0025`](../claims/C-0025-flexure-end-joint.md) (`PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `c(ρ)`, `g(β)`, `S_eff`), [`C-0023`](../claims/C-0023-two-sided-coupling.md) (`E5`, `CrossoverHingeFlexure`, `hingeArmForStiffness`, the 40 pN/nm ceiling, the 45 paths), [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (the 32 bp phase period and its quantisation to base pairs), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`EI`, `S`, the rise, `d = 2.69 nm`, `k_θ`, `k_s`, the allowables), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the mandate and the envelope), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN) |
| **Raises** | [`CH-0039`](../challenges/CH-0039-the-base-couple-needs-a-lever-arm-the-standoff-does-not-have.md) against `C-0028`, [`CH-0040`](../challenges/CH-0040-e5-is-a-small-rotation-law-at-47-degrees.md) against `C-0023` |

---

## Formulate

### The gap this task exists to close

`C-0028` closed `T-40` with a design —
a normal duplex standoff of 7–9 nm on **two antiparallel crossovers to the two adjacent sheet duplexes, laid across the flexure axis** —
and named as its own open question 2, at the first rank:

> **Whether a 90° scaffold or staple routing between a sheet duplex and a normal standoff exists at all.**
> The literature has no instance and a nicked continuation cannot supply it.
> **This is upstream of every number in this claim.**

It is upstream in the strict sense.
`C-0028`'s recommended base, `B2`, is *defined* as `2k_θ + 2k_s(d/2)²` = 261.2 pN·nm/rad,
where `d/2 = 1.345 nm` is **half the sheet's interhelical distance**.
That number is a statement about *where the two crossovers are*,
and nothing in `T-40` checked that a standoff can put a covalent link there.

**If it cannot, `C-0028`'s window is written on a lever arm that does not exist,
`C-0025`'s only passing joint is unbuildable, and `T-23` falls back to `E5`.**

### The question, as a numeric target

1. **How many independent covalent links can ground a duplex standing normal to a single-layer sheet, and how far apart can they be?**
   Not "is there a motif" but "how many strand termini exist at the base, and what is the largest lever arm they span".
2. **What base rotational stiffness does that permit**, at `C-0009`'s own softened-bond constants, about each of the two bending axes — against `C-0028`'s own threshold ladder `k_θ_base(ℓ)`.
3. **What the junction's helical phase costs**: the azimuth of the base chord is set by which base pair of the standoff carries the junction, quantised at `360°/(bp per turn)`; report the worst-case misalignment and what it costs the couple.
4. **Whether a normal (90°) exit is reachable at all**, or only discrete oblique ones — and if the polar angle is not set by the routing, what does set it.
5. **A concrete routing in base pairs** for each of the four candidates named in `T-67`: a staple that terminates in the sheet and continues as one standoff strand; a scaffold excursion; a separate short duplex on sticky ends; and the literature's own pin-plus-triangulation.
6. **The `E5` fallback evaluated against the same acceptance**, so the programme is left with a design either way — or a plain statement that it is not.

### The geometry and sign conventions, fixed before deriving

- **The sheet.** Duplex axes run along `x`, spaced `d = 2.69 nm` along `y` (SAXS, `C-0009`), all axes at `z = 0`. The sheet occupies `z ≤ 0`-ish; `z > 0` is where the standoff goes.
- **The standoff.** A B-form duplex whose axis is the `+z` direction, centre at `(x_c, y_c)`, **bottom face** — the plane of its terminal base pair — at `z = z_e`.
- **Backbone model.** Phosphorus atoms on two coaxial helices of radius `r_P`, rise `a = 0.34 nm/bp`, twist `Ω = 360°/(bp per turn)`, the two strands separated in azimuth by the **minor-groove backbone angle** `Δ`. A duplex is sterically a cylinder of radius `R = 1.0 nm`.
- **A link** is one phosphodiester step between a standoff strand terminus and a sheet strand terminus. Its reach is not a distance but the **measured window** `[0.60, 0.70] nm` — C3′-endo to C2′-endo — because a pair too *close* cannot be bonded either; beyond 0.70 nm the link needs `⌈(gap − 0.70)/0.65⌉` unpaired nucleotides and stops being covalent-rigid.
- **The base's two axes.** `k_θ_base∥` restrains rotation about the line **along** the base chord; `k_θ_base⊥` about the line perpendicular to it. Only the second carries the couple.
- Sign conventions otherwise inherited from `T-40` and `T-30` unchanged.

### The acceptance predicate, declared before any code runs

`C-0028`'s six, unchanged, applied to the base that this task finds to be **realisable** rather than to one assumed:

| | predicate | source |
|---|---|---|
| **P1** | transverse support ≥ 10× the beam's own per-path stiffness (0.7407 pN/nm), with the base in series, no dead band above 0.1 nm | `C-0025` |
| **P2** | the span solved as a root gives `n·R(3 nm)/3 = 33.3333 pN/nm` exactly | §3, via `C-0017` |
| **P3** | tangent at that point ≤ **40 pN/nm** | `C-0023` |
| **P4** | per-path force below the 10 pN unzip allowable at §3's **desired** 10 nm stroke | `C-0006`, `CH-0029` |
| **P5** | inside `C-0017`'s envelope (spans ≤ 60 nm, standoffs ≤ 10 nm) | `C-0017` |
| **P6** | `P_c ≥ the element's own compression duty at the desired stroke`, free-head reading | `C-0028` |

and **one this task adds, because it is the question**:

| | predicate | source |
|---|---|---|
| **P7** | **every load-bearing link in the base is a covalent phosphodiester step, i.e. requires zero unpaired nucleotides**, and the base's restrained axis can be aligned with the flexure's bending plane | this task |

`P7` is what separates a *clamp* from a *pin*, and a pin is `C-0028`'s `B5`, which fails `P1` at every length.

### The three cheap bounds that must run first — geometry before literature

They cost three multiplications between them and they decide the shape of the answer.

1. **The two-terminus count.**
   A B-form duplex has **two** backbones, so a duplex **end** presents exactly **two** strand termini,
   at the two backbone positions of its terminal base pair.
   Every covalent link grounding the standoff must start at one of them.
   **A base joint therefore has at most two links, and their separation is the terminal chord `2 r_P sin(Δ/2) ≤ 2 r_P = 2.0 nm`.**
   The lever arm is half of that: **≤ 1.0 nm hard, and 0.866 nm at the nominal `Δ = 120°`**, against `C-0028`'s **1.345 nm**.
2. **The couple ceiling, against `C-0028`'s own threshold.**
   `k_θ_base ≤ 2·αB/(100a) + 2·αS/(100a)·(chord/2)²` = **78.24 pN·nm/rad** at the hard 1.0 nm lever
   and **62.06** at `Δ = 120°`.
   `C-0028`'s `B2` is **261.2**, and its `P6` threshold is **68.8 pN·nm/rad at its own 8 nm design length**.
   **So `B2` is over the ceiling by 3.34×, and the threshold is met only up to 8.25 nm at the hard ceiling and 7.50 nm at the nominal chord.**
3. **The azimuthal quantum.**
   `360°/10.67 = 33.74°` per base pair on the square lattice, `34.29°` on the honeycomb.
   The base chord's azimuth can therefore be placed within **±16.87°** of any target,
   and a couple projects as `cos²`, so **the phase costs at most 8.5 % of the couple.**
   **The phase is cheap; the ceiling is what binds.**

Only because bound 1 gives a *ceiling* rather than a value, and bound 2 puts that ceiling within a factor of 1.14 of the requirement it has to meet, is the backbone-geometry solve worth running at all.

### The prediction, written down before the code runs

> **A routing exists and the standoff can be built — but every routing produces a HINGE and not a clamp, because the count of strand termini at a duplex end is two and their separation is the duplex's own diameter.**
> `C-0028`'s `B2` is not a motif that has not been demonstrated; it is a motif that asks for a lever arm 1.49× larger than the standoff's own backbone radius.
> The base that *is* realisable lands within a factor of ~1.2 of `C-0028`'s `P6` threshold, so the window will be pushed to **short** standoffs, where `C-0028` has already shown a soft base relieves `P3`.
> **And the restrained axis is the chord's perpendicular bisector, so the couple exists about one axis only** — `C-0028`'s `B2u`/`B2` anisotropy, but now forced rather than chosen.

Recorded here so that finding it is a confirmation and not a discovery after the fact.

### What is deliberately excluded

- **Any torsion-angle or atomistic check of the junction.** The model tests a **necessary** condition (a phosphate pair inside phosphodiester reach with no steric overlap), never a sufficient one. A *"closes"* verdict is therefore an **upper** bound on buildability and a *"does not close"* verdict is a proof of impossibility. Stated in the claim's validity range, not hidden.
- **Sequence design.** Base pairs and nucleotides make the statement concrete; they do not specify a staple.
- **`T-9`.** `k_θ` and `k_s` are swept over their own brackets.
- **`T-65`** (the coupled 2 × 2 tip compliance) and **`T-66`** (the triangulated standoff), which this task hands work to rather than doing.

---

## Plan

### Why this method and not another

| | strand-terminus counting + B-form backbone geometry (chosen) | an atomistic model of the junction | oxDNA of a 90° junction |
|---|---|---|---|
| what it gives | how many links exist, how far apart, and therefore the base couple's **ceiling** — which is what `C-0028` needs | the same, plus whether a specific dihedral set closes | the junction's own moment-rotation law |
| cost | seconds | days, and it needs a force field this project has not validated | days, and it is `T-9`'s cost a fifth time |
| what decides | **the counting theorem, which no simulation can overturn** — a duplex end has two termini whatever the force field says | — | — |

**The decisive row is the first.** The binding constraint is a *count* and a *radius*, both of which are properties of B-form DNA and not of a model of it. An atomistic or coarse-grained study could only tell us whether a junction that the counting theorem already caps is *also* geometrically frustrated — i.e. it can make the answer worse, never better. Spending days to lower an upper bound that already fails is the wrong order.

### What would falsify this approach — stated in advance

1. **A routing with more than two rigid links per standoff base.** The counting theorem is the whole method; a counterexample removes it. (An internal nick near the base adds termini, but they sit ≥ 1 bp above the base face and cannot reach the sheet — checked in code.)
2. **The ceiling not binding**, i.e. `C-0028`'s threshold met at every window length even at the smallest chord. Then `B2`'s over-specification is harmless and this task closes in a paragraph.
3. **The reach failing**, i.e. no phase at which two links close with zero unpaired nucleotides. Then the answer is the harder one — *no* routing — and `C-0025`'s only passing joint is unbuildable outright.
4. **`C-0028`'s pipeline not reproducing** on the realisable base at `ρ_b → ∞` and at its own `B1`/`B2`.
5. **`E5` passing the acceptance unchanged**, in which case the fallback is free and the standoff branch can simply be dropped.
6. **The literature showing a rigid perpendicular duplex-to-sheet junction with a characterised base.** Best possible outcome; it would replace the ceiling with a measurement.

### The literature search, and how it is made falsifiable

A negative existence claim needs a stated corpus. The strategy is registered here **before** the search:

- **Services**: arXiv API (`export.arxiv.org/api/query`, with `curl -sL`), EuropePMC REST search and full text, Crossref works API for verbatim abstracts, publisher PDFs where open.
- **Query families**: (i) four-arm / Holliday junction open conformation and inter-arm angles; (ii) the gridiron four-arm motif; (iii) T-junction / T-motif; (iv) three-arm junctions; (v) wireframe and polyhedral origami vertex routing and its poly-T spacer counts; (vi) out-of-plane elements on a 2D origami sheet — pillar, post, standoff, strut, leg, hairpin label; (vii) explicit statements that the double-crossover motif requires parallel helices; (viii) B-DNA phosphate radius and intrastrand P–P distance; (ix) junction bending or torsional stiffness measurements.
- **Every statement carries `read directly` / `abstract only` / `not found`, the URL fetched and a verbatim quote.** A delegated search is treated as a summary and spot-checked against the primary source by hand.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0028` | `StandoffBase`, `basedNormalStandoff`, `standoffBucklingLoad`, `baseRestraintParameter`, `bucklingStroke`, `baseRotationalStiffnessThreshold` | **re-run as a library**; its `B0`/`B1`/`B2` numbers and its threshold ladder reproduced as gate-5 tests |
| `C-0025` | `PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `midspanFactor`, `effectiveStretchModulus` | **re-run as a library** |
| `C-0023` | `CrossoverHingeFlexure`, `hingeArmForStiffness` — `E5` | **re-run as a library**, then re-solved under exact rotation |
| `C-0009`/`Gen1Tile` | `EI`, `S`, `a`, `d`, `k_θ`, `k_s`, the allowables | **cited**, and swept over `α ∈ [0.6, 1.2]` and `k_s`'s four decades |
| `C-0015` | the 32 bp phase period and its quantisation to base pairs | **cited**, and the azimuthal quantum derived beside it |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t67 --tests '*PerpendicularJunctionTest*'
tools/study.sh anchoring.PerpendicularJunctionStudyKt
tools/verify.sh
```

Code, all in `src/main/kotlin/anchoring/` — nothing outside it created or modified:

| file | what is in it |
|---|---|
| `PerpendicularJunction.kt` | the B-form backbone geometry, the seat solve, the terminus-count theorem, the two-link closure search, the realisable base and its two axes, the azimuthal quantum, the one-sided contact, and `E5` under exact rotation |
| `PerpendicularJunctionStudy.kt` | the study entry point, emitting the result JSON |

Result: [`../results/T-67-perpendicular-junction-routing.json`](../results/T-67-perpendicular-junction-routing.json).

Tests: `src/test/kotlin/anchoring/PerpendicularJunctionTest.kt`, each named for the gate it discharges.

---

## Verify

See [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md#the-five-verification-gates)
for the executed gate table and the falsifier outcomes.
