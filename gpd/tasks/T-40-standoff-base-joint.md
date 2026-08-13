# T-40 — Is a duplex standing normal to a single-layer sheet buildable, and what holds its base?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Problem definition** | §1 (the stack); §3 (100 pN, 3 nm acceptable, **10 nm desired**, 40 × 40 nm, 2 mM); §4(f); §5, §7 (process) |
| **Verification type** | **in-silico** (`C-0025`'s own partial-restraint machinery applied one level down, with the standoff itself a beam whose base is a rotational spring, and a two-spring sway-column buckling eigenvalue solved rather than assumed) **+ logical** (the observation that the standoff's buckling mode and the compliance the design buys from it are the *same* degree of freedom) **+ literature** (whether the motif is established at all) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0028`](../claims/C-0028-standoff-base-joint.md) |
| **Consumes** | [`C-0025`](../claims/C-0025-flexure-end-joint.md) (the whole joint model, `c(ρ)`, `g(β)`, `S_eff`, `PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `bondedLengthForTension`, the five predicates, the 7–10 nm window — **re-run as a library**), [`C-0023`](../claims/C-0023-two-sided-coupling.md) (the flexure, the 40 pN/nm ceiling, the 45 paths), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ`, `k_s`, `EI`, `S`, the rise, the SAXS interhelical distance), [`C-0014`](../claims/C-0014-lateral-confinement.md) (`eulerBucklingLoad`, `compressedTransverseStiffness`, `k(P) = k₀(1 − P/P_c)`, `FreelyJointedChain`), [`C-0024`](../claims/C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the length-dependent allowable), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the mandate, the envelope), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN) |
| **Raises** | [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md), [`CH-0038`](../challenges/CH-0038-a-standoff-grounded-at-infinity.md), both against `C-0025` |

---

## Formulate

### The gap this task exists to close

`C-0025` closed `T-30` with exactly one passing joint —
a duplex standing **normal** to the sheet —
and named its base as the first of five open questions:

> **Whether a duplex standing normal to a single-layer sheet is buildable with a rotationally stiff base.**
> The base joint is one of `J1`–`J4`, and the standoff's own `k_θ = EI/ℓ` assumes it is a clamp.

That is not a footnote.
`k_θ = EI/ℓ`, `k_a = 3EI/ℓ³` and `k_⊥ = S/ℓ` are the *three* stiffnesses of a cantilever **built in at its base**,
and `C-0025`'s whole design — span 31.64 nm, tangent 37.39 pN/nm, window `ℓ = 7–10 nm` — is written on them.
If the base is a crossover rather than a clamp,
all three change,
the Euler load changes,
and the design has to be re-decided.

**The standoff is the only joint in `C-0025`'s catalogue that passes, so its base is the single assumption the whole coupling rests on.**

### The question, as a numeric target

For each buildable way of grounding a normal standoff on a single-layer Rothemund sheet,
and for each standoff length `ℓ` in `C-0025`'s own 3–10 nm sweep:

1. the **base's** three constants `(k_θ_base, k_z_base, dead band)`, from cited constants with their provenance;
2. the **standoff's** three constants as functions of them —
   its head rotational restraint, its sway (draw-in) stiffness and its transverse support —
   each a *series*, not a cantilever number;
3. its **Euler load**, solved as a two-spring sway-column eigenvalue rather than read off a `K` factor;
4. the **flexure design that results** — span, `c`, secant, tangent, beam tension, `CH-0029` bonded length —
   from `C-0025`'s own root find, unchanged;
5. and a plain verdict on whether `C-0025`'s `ℓ = 7–10 nm` window survives, moves, or closes.

### The geometry and sign conventions, fixed before deriving

Inherited from `T-30` unchanged, with three additions:

- **The standoff's own frame.** `z` runs from the base (`z = 0`, on the sheet) to the head (`z = ℓ`, under the beam end).
  `x` runs along the flexure's axis, toward midspan.
  The standoff **bends in the `x`–`z` plane** and is loaded **axially along `z`**.
- **The base is a joint in the same sense `T-30` uses**: a rotational spring `k_θ_base` about the bending axis,
  an axial spring `k_z_base` along `z`, and a transverse dead band.
- **NEW, and it is the whole of the buckling result — the standoff's *sway* is the flexure's *draw-in*.**
  The head's translation in `x` is the same coordinate under both names.
  So the head cannot be held against sway without also being held against draw-in,
  and holding it against draw-in is exactly `C-0023`'s *ends held axially* reading,
  which `C-0025` spent the whole of `T-30` escaping.
  **The buckling bracket is therefore free-head to guided-head and the held-head reading is not available to this design at all.**

### The acceptance predicate, declared before any code runs

`C-0025`'s five, unchanged, **plus one this task exists to add**:

| | predicate | source |
|---|---|---|
| **P1** | transverse support ≥ 10× the beam's own per-path stiffness (0.7407 pN/nm), **with the base in series**, and no dead band above 0.1 nm | `C-0025` |
| **P2** | the span solved as a root gives `n·R(3 nm)/3 = 33.333 pN/nm` exactly | §3, via `C-0017` |
| **P3** | tangent at that point ≤ **40 pN/nm** | `C-0023` |
| **P4** | beam tension below 10 pN unzip at §3's **desired** 10 nm stroke, judged against `CH-0029`'s ladder | `C-0006`, `CH-0029` |
| **P5** | every length inside `C-0017`'s envelope (spans ≤ 60 nm, standoffs ≤ 10 nm) | `C-0017` |
| **P6** | **`P_c ≥ the standoff's own compression duty at §3's desired stroke**, the duty read from the **element's own reaction** and the critical load from the **conservative free-head** reading | this task |

`P6` is declared as a **predicate** and not, as in `C-0025`, reported beside them —
because `T-40` exists precisely to ask it.

### The cheap bounds that must run first, and there are three

**Three divisions, and between them they decide the shape of the answer before any eigenvalue is solved.**

1. **`ρ_b = k_θ_base ℓ/EI` at `C-0009`'s crossover constant.**
   13.53 pN·nm/rad on a 3–10 nm standoff against `EI = 230 pN·nm²` gives **`ρ_b = 0.18–0.59`**.
   Since the head restraint is `(EI/ℓ)·ρ_b/(ρ_b+1)`, a single crossover delivers **13–37 %** of the clamp `C-0025` assumed.
   **The base is not nearly a clamp**, and one division says so.
2. **The Euler load in the pinned limit.**
   A column with a *pin* at its base and a *free* head has `P_c = 0` **exactly** — it is a mechanism, not a strut.
   So `P_c` is not a 4× bracket around `C-0025`'s 8.87/35.5 pN;
   it runs to zero, and the only question is how fast.
3. **The support series.**
   `k_⊥ = 1/(ℓ/S + 1/k_z_base)`. At `ℓ = 8 nm`, `S/ℓ = 137.5` against a crossover's 64.71,
   so the base takes **68 %** of the compliance and `C-0025`'s 137.5 pN/nm is 3.1× optimistic before anything is solved.

Only because the first two say *"the base is neither a clamp nor negligible, and the pinned limit is a mechanism"* is the eigenvalue sweep worth running.

### The two-spring sway column, derived here and asserted as a limit test

A column of rigidity `EI` and length `ℓ` under axial load `P`, base rotational spring `ρ_b`, head rotational spring `ρ_h`,
head **free to translate**. With `u = ℓ√(P/EI)`, the buckling determinant is

&nbsp;&nbsp;&nbsp;&nbsp;**`D(u) = sin u·(u² − ρ_b ρ_h) − cos u·(ρ_b + ρ_h)·u = 0`**, &nbsp;&nbsp; first root in `(0, π)`,
&nbsp;&nbsp; **`P_c = u² EI/ℓ²`**.

Its four corners are the four textbook `K` factors, and each is a gate-2 test:

| `ρ_b` | `ρ_h` | `u` | `K` | condition |
|---|---|---|---|---|
| ∞ | 0 | `π/2` | 2 | clamped base, free head — `C-0025`'s "pinned head", 8.87 pN at 8 nm |
| ∞ | ∞ | `π` | 1 | clamped base, guided head — `C-0025`'s "guided", 35.5 pN at 8 nm |
| 0 | 0 | **0** | ∞ | **pinned base, free head — a mechanism, `P_c = 0` exactly** |
| 0 | ∞ | `π/2` | 2 | pinned base, guided head |

and the two one-spring reductions `u tan u = ρ_b` and `u cot u = −ρ_b` fall out of it.

The **head restraint is not a free parameter either**: the beam presents its own end rotational stiffness to the standoff head,
`2EI_beam/L` in the symmetric mode and `6EI_beam/L` in the antisymmetric one,
so `ρ_h = 2ℓ/L` and `6ℓ/L` are *realised* readings between the free and guided brackets, and all four are reported.

### The base motifs, declared in advance

| id | motif | `k_θ_base` | expected |
|---|---|---|---|
| **`B0`** | ideal clamp | ∞ | `C-0025`'s assumption, carried to reproduce it |
| **`B1`** | one antiparallel crossover to one sheet duplex | 13.53 | far from a clamp; buckling suspect |
| **`B2u`** | two crossovers to adjacent duplexes, **couple axis parallel to the beam** | `2k_θ` = 27.06 | the couple contributes **nothing** |
| **`B2`** | two crossovers to adjacent duplexes, **couple axis perpendicular to the beam** | `2k_θ + 2k_s(d/2)²` = 261.2 | the couple is the whole of it |
| **`B3`** | three crossovers, favourable orientation | `3k_θ + 2k_s d²` = 977.0 | geometrically demanding — a 2 nm duplex over a 5.38 nm footprint |
| **`B4`** | nicked / scaffold continuation | — | **structurally unavailable**: a nick preserves the helix axis, so it cannot turn 90° |
| **`B5-n`** | `n`-nt poly-T flexible junction | 3.35 at 2 nt | `CH-0031` one level down: it has a dead band and cannot support |

### The prediction, written down before the code runs

> **The base joint moves the design in two OPPOSITE directions, and the mechanism that closed `C-0025`'s window from below is the one a compliant base relieves.**
> A softer base makes the standoff softer in *both* bending directions, so it releases more draw-in, the membrane term collapses, and the tangent falls — `P3` gets *easier* and short standoffs become admissible.
> The same softness collapses the Euler load faster than it collapses the duty, so `P6` gets *harder*.
> **The window will therefore not narrow or widen: it will be re-cut by a different pair of constraints, and the binding one will be buckling.**

Recorded here so that finding it is a confirmation and not a discovery after the fact.

### What "an answer to `T-40`" has to deliver, in full

1. the base modelled as a joint, with all three standoff constants as series and `C-0025`'s numbers as the `ρ_b → ∞` limit;
2. the two-spring sway-column eigenvalue derived, its four corners asserted exactly, and the pinned-base mechanism among them;
3. `C-0025`'s 8.87 and 35.5 pN reproduced, **and** recomputed with a compliant base;
4. every one of `C-0025`'s five acceptance clauses re-checked over `ℓ = 7–10 nm` for every base motif, plus `P6`;
5. the replacement window, or a statement that it closes;
6. the buckling checked at §3's **acceptable** 3 nm (which is also `C-0019`/`C-0023`'s **held** gap, `L₀ − 3 nm`), at §3's **desired** 10 nm, and as a continuous **stroke at which the standoff buckles**;
7. the literature answer to whether the motif is established, with a `read directly` / `abstract only` / `not found` flag on every number;
8. all five gates, with gate 3 checking something that is not a restatement of the construction.

### What is deliberately excluded

- **The fully coupled joint compliance matrix.** `C-0025`'s open question 1. Its *magnitude* is bounded here (the off-diagonal correlation and the other-DOF-fixed reading), its sign is argued, and it is queued as its own task rather than solved inside this one.
- **`T-9`.** `k_θ` and `k_s` are swept over the brackets their own claims carry.
- **Any sequence design.** Base pairs and nucleotides make the statement concrete; they do not specify a staple.
- **The flexure array** (`T-31`) and the lever (`T-33`).

---

## Plan

### The cheap bounds first, and why they justify the method

The three divisions above cost nothing and already say the answer is not `C-0025`'s.
Had `ρ_b` come out at 20 the base would have been a clamp to within a few per cent and this task would have closed on a division;
had the pinned limit been a finite fraction of the clamped one, a 4× bracket would have sufficed and no eigenvalue would have been needed.
Neither happened: `ρ_b = 0.18–0.59` and the pinned-base free-head column has **exactly zero** critical load.

### Then the model, and why this one

| | `C-0025`'s machinery one level down (chosen) | a finite-element model of the base | an oxDNA simulation of an out-of-plane junction |
|---|---|---|---|
| what it gives | the standoff's three constants and its eigenvalue as **continuous functions of the base**, with `C-0025`'s design as the `ρ_b → ∞` limit | the same, computed rather than composed | the junction's own moment-rotation law with no fitted constant |
| cost | seconds | hours | days, and it is `T-9`'s cost a fourth time |
| what it would add | — | nothing: the base constants are the input either way | the one thing that would settle `k_θ` and `k_s` — which is `T-9`'s job and is queued |

**The decisive row is the third, exactly as in `T-30`.** Every candidate base traces to Chen et al.'s softened-bond construction,
so a finite-element model of the base would be a finite-element model *of that construction*.
Composing it and carrying its own bracket is the honest thing.

### What would falsify this approach — stated in advance

1. **The model failing to reproduce `C-0025` at `ρ_b → ∞`** — the eight standoff-window rows, both buckling loads, the design's span and tangent. Then it is not a generalisation of the filed one.
2. **Every base landing at `ρ_b ≫ 1`**, i.e. the clamp being a good approximation after all. Then `C-0025` was right by luck and this task closes in a paragraph.
3. **No base motif passing all six predicates at any length.** Then the standoff is not buildable, `C-0025`'s only passing joint falls, and the answer to `T-23` reverts to `E5`, the crossover hinge.
4. **The base making no difference** — the design moving by less than the spread `k_s`'s own four decades already produce.
5. **Buckling never binding**, i.e. `P_c` above the duty at every base and every length. Then `C-0014`'s strut lesson does not transfer and `P6` was not worth declaring.
6. **The literature showing the motif is standard and its base measured.** Then the whole modelled bracket is replaced by a number, which would be the best possible outcome.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0025` | `midspanFactor`, `drawInFactor`, `effectiveStretchModulus`, `FlexureEndJoint`, `PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `bondedLengthForTension`, the five predicates, the window | **re-run as a library**; its eight window rows reproduced as gate-5 tests |
| `C-0009`/`Gen1Tile` | `k_θ`, `k_s`, `EI`, `S`, the rise, `d = 2.69 nm` | **cited**, and swept over `α ∈ [0.6, 1.2]` and `k_s`'s four decades |
| `C-0014` | `eulerBucklingLoad`, `compressedTransverseStiffness`, `FreelyJointedChain` | **re-run as a library**; its `K = 1, 2` reproduced as the eigenvalue's corners |
| `C-0024`/`CH-0029` | the length-dependent shear allowable | **re-run as a library** |
| `C-0017`, `C-0006`, `C-0023` | the mandate, the envelope, the allowables, the ceiling | **cited** |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t40 --tests '*StandoffBaseJointTest*'
tools/study.sh anchoring.StandoffBaseJointStudyKt
tools/verify.sh
```

Code, all in `src/main/kotlin/anchoring/` — nothing outside it created or modified:

| file | what is in it |
|---|---|
| `StandoffBaseJoint.kt` | the base as a joint, the seven motifs, the three series reductions, the two-spring sway-column eigenvalue, the buckling-stroke inversion and the base-stiffness threshold |
| `StandoffBaseJointStudy.kt` | the study entry point, emitting the result JSON |

Result: [`../results/T-40-standoff-base-joint.json`](../results/T-40-standoff-base-joint.json).

Tests: `src/test/kotlin/anchoring/StandoffBaseJointTest.kt`, each named for the gate it discharges.

---

## Verify

See [`C-0028`](../claims/C-0028-standoff-base-joint.md#the-five-verification-gates) for the executed gate table
and the falsifier outcomes.
