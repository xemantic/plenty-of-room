# T-30 — The origami joint at a flexure's end: does it draw in, and does it clamp?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Problem definition** | §1 (the stack); §3 (100 pN, 3 nm acceptable, **10 nm desired**, 40 × 40 nm, 5/7/10 nm, 2 mM); §4(f) (the disassembly band, read per `C-0006` as *not* a per-path allowable); §5, §7 (process) |
| **Verification type** | **in-silico** (a partial-restraint Euler-Bernoulli flexure whose two end brackets are the two limits of one two-parameter joint, solved for the span that places §3's mandate) **+ logical** (an anisotropy argument that decides which joints can exist before any of them is evaluated) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0025`](../claims/C-0025-flexure-end-joint.md) |
| **Consumes** | [`C-0023`](../claims/C-0023-two-sided-coupling.md) (the flexure, the membrane term, the draw-in demand, the two brackets, the 40 pN/nm compliance ceiling, the 45 paths), [`C-0024`](../claims/C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the shear allowable **as a function of bonded length**), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ = 2αB/(100a)`, `k_s = 2αS/(100a)`, `EI`, `S`, the rise, the crossover pitch), [`C-0014`](../claims/C-0014-lateral-confinement.md) (`FreelyJointedChain`, the Kuhn bracket, `cableTension`, `eulerBucklingLoad`, the convexity theorem), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (the mandate, the buildable envelope), [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) (that the coupling has to be two-sided at all), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the allowables) |
| **Raises** | [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md) against `C-0023`'s two-nucleotide hinge remedy |

---

## Formulate

### The gap this task exists to close

`C-0023` closed `T-23` with a design and **two brackets it deliberately did not collapse**, and named them
as the first thing it would hand to a designer:

> **How an origami joint is built decides the flexure's end condition *and* its axial restraint**, and those
> two together are 2.2× in span and 2.7× in tangent stiffness.

| | pinned ends | clamped ends |
|---|---|---|
| bending factor `c` | 48 | 192 — exactly 4× |
| span at 45 paths, **ends free to draw in** | **24.61 nm = 72 bp** | 39.07 nm = 115 bp |
| span at 45 paths, **ends held axially** | 49.41 nm = 145 bp | 54.91 nm = 162 bp |

and the restrained reading fails `C-0023`'s own declared 40 pN/nm compliance ceiling (tangent **91.13 pN/nm**,
2.3× past) and breaks the 65 pN nicked ceiling at §3's **desired** 10 nm stroke (**86.7 pN** of beam tension).
`C-0023` quantified the demand precisely — **0.88 nm = 2.6 base pairs of in-plane draw-in**, `2.4 δ²/L` — and
proposed a remedy in one sentence: *"a two-nucleotide single-stranded hinge at each end absorbs it, and it is
loaded along the beam's axis, not in the normal load path."*

**That sentence is a joint design, and it has never been checked.** This task checks it.

### The question, as a numeric target

A **joint model**, not a fitted constant. For each candidate way of building the end of a transverse duplex
flexure inside a Rothemund single-layer sheet, two numbers and one verdict:

1. its **rotational restraint** `k_θ` in `pN·nm/rad`, which fixes the end-condition factor `c` — and `c` is a
   **continuum**, not a binary, because 48 and 192 are the two limits of one elastic end spring;
2. its **axial restraint** `k_a` in `pN/nm`, which fixes how much of the `2.4 δ²/L` draw-in demand the joint
   can supply and therefore how much of the membrane term survives;
3. its **transverse** stiffness, because the same joint has to react the beam's end shear in **both**
   directions — a flexure whose ends are not supported is not a flexure;
4. the **flexure design that results**: span in nm and in base pairs, `c`, secant and tangent at §3's working
   point, and the per-load-path forces at §3's 3 nm **and** 10 nm strokes against `C-0006`'s 10 pN unzip and
   the 65 pN nicked ceiling, **with `CH-0029`'s correction applied** — the shear allowable is a function of
   the bonded length (18.8 pN at 8 bp, 34.8 at 16, 47.1 at 30), never the flat 48 pN.

### The acceptance predicate, declared before any code runs

A joint **passes** when all five hold:

| | predicate | source of the number |
|---|---|---|
| **P1** | **it supports the beam**: transverse stiffness at the end ≥ 10× the beam's own per-path stiffness, in **both** directions and with no dead band above 0.1 nm | a support that moves is not a support; 0.1 nm is well below `C-0023`'s 3 nm stroke |
| **P2** | **placed**: the span solved as a root gives `n·R(3 nm)/3 = 33.333 pN/nm` exactly | §3, via `C-0017` |
| **P3** | **compliant**: the tangent at that point ≤ **40 pN/nm**, `C-0023`'s own declared ceiling | `C-0023` |
| **P4** | **safe**: the beam's axial tension and the joint's own reactions below 10 pN unzip at §3's **desired** 10 nm stroke, and below 65 pN under every reading; any hybridised joint judged against `A(n_bp)` and not against 48 | `C-0006`, `CH-0029` |
| **P5** | **buildable**: every length inside `C-0017`'s envelope (lever spans ≤ 60 nm, standoffs ≤ 10 nm) and quoted in base pairs or nucleotides | `C-0017`, via `C-0023`'s `P5` |

### The cheap bounds that must run first, and there are three

**They are three divisions, and between them they decide the shape of the answer before any root is found.**

1. **`ρ = k_θ L/EI` at the crossover constant.** `C-0009`'s fitted `k_θ = 2αB/(100a) = 13.53 pN·nm/rad` on a
   ~25–50 nm span against `EI = 230 pN·nm²` gives `ρ ≈ 1.4–2.9`. Since `c(ρ) = 192(ρ+2)/(ρ+8)` — derived
   below — that is `c ≈ 70–87`, i.e. **neither 48 nor 192**. The 4× end-condition bracket is not 4× for any
   real joint; it collapses to a band of about 1.25×.
2. **`2S/(k_a L)` at the crossover's in-plane constant.** `k_s = 2αS/(100a) = 64.7 pN/nm` on the same spans
   gives 0.85–1.36, i.e. the joint's axial compliance is **comparable to the beam's own**: neither of
   `C-0023`'s two readings applies, and the answer is in the middle.
3. **The anisotropy bound, which is logical and costs nothing.** A joint has to be **stiff transversely** (it
   supports the beam) and **soft axially** (it releases the draw-in). For any *flexible link* those are the
   same number — a chain's transverse secant and its axial secant are both `f/x` — so **an isotropic element
   cannot do both**, and `C-0023`'s ssDNA hinge is isotropic. This is `C-0014`'s convexity theorem
   (`k_lat/k_norm ≤ 1` for a flexible link) in a new place, and it predicts which joints can exist.

### The partial-restraint model, derived here and asserted as a limit test

A beam of span `L`, rigidity `EI`, loaded at midspan by `P`, with an equal rotational spring `k_θ` at each end.
Superposing the simply supported central-load solution with a pair of end moments `M` and imposing
`θ_end = M/k_θ`:

&nbsp;&nbsp;&nbsp;&nbsp;`M = P L ρ/(8(ρ + 2))`, &nbsp;&nbsp; `δ = (P L³/EI)[1/48 − ρ/(64(ρ + 2))]`,
&nbsp;&nbsp; **`c(ρ) = 192(ρ + 2)/(ρ + 8)`**, &nbsp;&nbsp; `ρ ≡ k_θ L/EI`.

`c(0) = 48` and `c(∞) = 192` **exactly**, so `C-0023`'s bracket is the two limits of this one model, and that
is a gate-2 test rather than a claim.

**Axial restraint enters the same way.** The draw-in demand is taken up by the beam's own stretch in series
with the two end springs, so

&nbsp;&nbsp;&nbsp;&nbsp;**`S_eff = S/(1 + 2S/(k_a L))`**,

which is `S` at `k_a → ∞` and `0` at `k_a → 0` — again `C-0023`'s two readings as limits, again a gate-2 test.
Every other formula (`cableTension`, `cableNormalForce`, the analytic tangent) is `C-0023`'s unchanged, with
`S_eff` substituted for `S`.

**And the draw-in shape factor is not 2.4 in between.** Integrating `Δ = ∫(1/2)w′²dx` over the *partially
restrained* shape gives, with `β ≡ 3ρ/(ρ + 2) ∈ [0, 3]`,

&nbsp;&nbsp;&nbsp;&nbsp;`g(β) = (2.4 − 1.25β + β²/6)/(1 − β/4)²`,

which is **2.4 at both ends and has an interior minimum of exactly 9/4 at β = 2.4, i.e. ρ = 8, c = 120**.
`C-0023`'s *"2.4 for both, which is not obvious"* is right at the endpoints and up to 6.25 % high between them.

### Units, locked

Lengths in **nm**, forces in **pN**, stiffness in **pN/nm** (= mN/m), moments in **pN·nm**, rotational
stiffness in **pN·nm/rad**, bending rigidity in **pN·nm²**, energies in **pN·nm** and `k_BT`.
`k_BT = 4.141947 pN·nm` at 300 K, aqueous **2 mM MgCl₂**. Base pairs at the 0.34 nm rise, nucleotides at
0.65 nm of ssDNA contour (**inextensible** convention — the convention travels with the number).

### Geometry and sign conventions, fixed before deriving

Inherited unchanged from `T-23`, with three additions this task needs:

- `z` normal to the electrode, positive **away** from it; the stroke `s = L₀ − h` is positive **downward**;
  the coupling reaction `R` is positive **upward**; the element displacement `δ` is signed and positive
  downward (`C-0023`).
- **NEW — the flexure's local frame.** `x` runs along the beam's own axis, `w` is its transverse deflection
  (which is the tile's normal coordinate), and the beam is symmetric about midspan. The **draw-in** is the
  total inward motion of the two ends, `Δ = ∫(1/2)w′²dx`, positive when the ends approach each other.
- **NEW — a joint is a pair of springs and a dead band**, `(k_θ, k_a, w_dead)`: a rotational spring about the
  bending axis, an axial spring along `x`, and the transverse free play before the joint reacts at all. A
  covalent joint has `w_dead = 0`; a joint containing slack single strand has `w_dead = n · 0.65 nm`.
- **NEW — the joint's transverse stiffness is a third number and is not assumed equal to either.** For an
  *isotropic* element it equals `k_a` identically; for a *bending* element it does not, and the ratio is the
  design content.

### The candidate joints, declared in advance

Every one is evaluated and reported, including the two that are expected to fail — §7 rewards saying which
were checked.

| id | joint | what it is | expected verdict |
|---|---|---|---|
| **`J1`** | **direct antiparallel crossover** | the sheet's own motif: the flexure duplex tied to the post by one crossover | supports; too stiff axially |
| **`J2`** | **nicked continuation** | the flexure duplex *is* the post's duplex, continuing through a nick on one backbone | effectively clamped **and** effectively held — the worst corner |
| **`J2b`** | **doubly nicked continuation** | both backbones nicked at the same base pair | identical to `J1` — a double nick *is* a crossover |
| **`J3`** | **`n`-nucleotide single-stranded hinge** — `C-0023`'s own remedy | both backbones single-stranded for `n` nt at each end | axially free, and **cannot support the beam**: `w_dead = 0.65 n` |
| **`J4`** | **rigid multi-crossover clamp** | `n_x` crossovers at the 32 bp pitch | clamped and held; the couple term dominates `k_θ` |
| **`J5`** | **normal standoff** — the design this task proposes | the beam's end sits on a short duplex standing **normal** to the sheet | the only **anisotropic** joint: `S/ℓ` transversely, `3EI/ℓ³` axially |

### The prediction, written down before the code runs

> **`J1`–`J4` are all covalent, therefore isotropic, therefore they cannot separate transverse support from
> axial release; and `J3`, the one element that is axially soft, is soft in *every* direction and so trades
> a membrane term for a transverse dead band of the same size. The design has to be anisotropic, and the only
> anisotropy DNA offers is the same one `C-0023` used to escape the axial trade-off: bending.**

It is recorded here so that finding it is a confirmation rather than a discovery after the fact.

### What "an answer to `T-30`" has to deliver, in full

Discharged when all seven hold:

1. the partial-restraint model derived, with `c(0) = 48` and `c(∞) = 192` asserted as **exact** limits and
   `C-0023`'s two spans (24.61 and 49.41 nm) and its two tangents (33.333 and 91.13 pN/nm) reproduced there;
2. the draw-in shape factor `g(β)` derived, its two endpoints shown to be 2.4 and its interior minimum
   located exactly;
3. all six joints given `(k_θ, k_a, k_transverse, w_dead)` from **cited** constants with their provenance, and
   every derived-not-measured constant swept over the bracket its own claim carries;
4. for every joint, the span solved as a **root** at 45, 15 and 8 paths, with the secant, the tangent, the
   `t/s` ratio, the draw-in demand in nm and base pairs, and the per-path forces at 3 **and** 10 nm;
5. `CH-0029`'s ladder applied — the bonded length each joint's own load would need, not a flat 48 pN;
6. a plain verdict on whether `C-0023`'s free-to-draw-in reading survives, and if not, whether the two-sided
   coupling still closes `T-13`;
7. all five gates, with gate 3 checking something that is not a restatement of the construction.

### What is deliberately excluded

- **Any finite-element or shell model of the joint.** The joint's two springs are what a designer *chooses*
  by which motif to build; resolving them by simulation would answer a question a staple answers.
- **`T-9`.** `k_θ` and `k_s` are `C-0009`'s cited fit and `C-0020`'s derived construction, and both are swept
  over the brackets their own claims carry (`α ∈ [0.6, 1.2]`; `k_s` over four decades).
- **Re-solving the zero-bias balance.** `C-0023` owns it; this task changes the element's *geometry*, not its
  sidedness, so the confinement verdict is inherited and re-checked only through the stiffness it delivers.
- **The lever's own joints.** `T-33` owns them, and `C-0023` flags the assumption.
- **The flexure array.** `T-31` owns whether neighbouring flexures on a common superstructure stay
  independent; this task treats one beam, exactly as `C-0023` does.

---

## Plan

### The cheap bounds first, and why they justify the method

The three bounds above are **three divisions and one paragraph**, and they already say that the answer is
neither of `C-0023`'s two readings and that the ssDNA remedy has a cost `C-0023` did not price. Only because
they say that is a two-parameter root find worth running at all: if `ρ` had come out at 0.01 or 100 the
bracket would have collapsed to one of its ends and the task would have closed on a division.

### Then the model, and why this one

| | partial-restraint Euler-Bernoulli with a two-spring joint (chosen) | a finite-element beam with a modelled joint | a coarse-grained (oxDNA) simulation of the joint |
|---|---|---|---|
| what it gives | `c` and `S_eff` as **continuous functions of the joint**, with `C-0023`'s two brackets as exact limits; the span, tangent and per-path forces of every candidate | the same two numbers, computed rather than composed | the joint's own force-extension and moment-rotation laws with no fitted constants |
| cost | seconds | hours | days, and it is `T-9`'s cost a third time |
| what it would add | — | nothing the composition does not already give, because the joint constants are the input either way | the one thing that would settle `k_s`, which is `T-9`'s job and is already queued |

**The decisive row is the third.** Every candidate joint's stiffness traces to Chen et al.'s softened-bond
construction — `k_θ` fitted, `k_s` derived from it — so a finite-element model of the joint would be a finite
element model *of that construction*, not of DNA. The honest thing is to compose the construction, carry its
own bracket, and report the answer as a function of it.

### What would falsify this approach — stated in advance

1. **The partial-restraint model failing to reproduce `C-0023`'s two spans and two tangents at its limits.**
   Then the model is not a generalisation of the filed one and nothing here is comparable to it.
2. **Every candidate joint landing at one end of the bracket.** Then the bracket was never a design variable
   and `C-0023` should have collapsed it — a result, and one that would close this task in a paragraph.
3. **The answer being insensitive to the joint**, i.e. the span and tangent moving by less than the
   uncertainty in `k_s`. Then the joint is not the binding open choice `TASKS.md` calls it.
4. **No joint passing `P1` and `P3` together.** Then the transverse flexure `E3` is not buildable at all and
   the answer to `T-23` falls back to `E5`, the crossover hinge — which `C-0023` itself names as the fallback.
5. **The draw-in shape factor turning out to be 2.4 everywhere**, which would make the interior of the
   bracket uninteresting and the whole continuum a two-point choice after all.
6. **`CH-0029`'s ladder making no difference** — i.e. every joint's own load being so far below every
   allowable that the bonded length never binds. Expected to fire *for the design* and not for `J1`/`J2`,
   which is itself the discriminator.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0023` | the flexure law, the membrane term, `endDrawIn`, the two brackets, the 40 pN/nm ceiling, the 45 paths, the design root-find discipline | **re-run as a library**; its two spans and two tangents reproduced as gate-5 tests |
| `C-0009`/`Gen1Tile` | `k_θ = 2αB/(100a)`, `k_s = 2αS/(100a)`, `EI`, `S`, the rise, the 32 bp pitch, the allowables | **cited**, and swept over `α ∈ [0.6, 1.2]` and `k_s`'s four decades |
| `C-0024`/`CH-0029` | `ShearJointAllowable` — the length-dependent shear allowable | **re-run as a library**; its 18.8/34.8/47.1 pN reproduced as a gate-5 test |
| `C-0014` | `FreelyJointedChain`, the Kuhn bracket, `cableTension`, `cableNormalForce`, `eulerBucklingLoad`, the convexity theorem | **re-run as a library** |
| `C-0017` | the mandate `33.333 pN/nm`, the buildable envelope | **cited** |
| `C-0006` | 10 / 65 pN | **cited** |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t30 --tests '*FlexureEndJointTest*'
tools/study.sh anchoring.FlexureEndJointStudyKt
tools/verify.sh
```

**26 gate-named tests, 135 in `anchoring`, 931 in the suite, 0 failures**; the result file re-run through
`tools/study.sh` and diffed byte-for-byte identical.

Code, all in `src/main/kotlin/anchoring/` — nothing outside it created or modified:

| file | what is in it |
|---|---|
| `FlexureEndJoint.kt` | the joint as a pair of springs and a dead band, the six candidates with their provenance, `c(ρ)`, `g(β)`, `S_eff`, the partially restrained flexure, and the span root find |
| `FlexureEndJointStudy.kt` | the study entry point, emitting the result JSON |

Result: [`../results/T-30-flexure-end-joint.json`](../results/T-30-flexure-end-joint.json).

Tests: `src/test/kotlin/anchoring/FlexureEndJointTest.kt`, each named for the gate it discharges.

---

## Verify

See [`C-0025`](../claims/C-0025-flexure-end-joint.md#the-five-verification-gates) for the executed gate table
and the falsifier outcomes.
