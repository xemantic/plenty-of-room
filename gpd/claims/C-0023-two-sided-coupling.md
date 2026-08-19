# C-0023 — DNA offers three two-sided compliant couplings, and two-sidedness changes the hold-down requirement from a force into a stiffness

| | |
|---|---|
| **Task** | [`T-23`](../tasks/T-23-two-sided-coupling.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.1`/`A1.2` for the positional bound and `A2.2` for the operating point |
| **Verification type** | **in-silico** (signed force-extension laws for six candidate elements, composed into a load line and re-solved against `C-0021`'s own zero-bias balance, with `C-0003`'s layer and `C-0008`'s field re-run as libraries) **+ logical** (a sidedness argument that fixes the *currency* of the requirement before any element is evaluated) |
| **Verdict** | **PASS, and the answer is not the one the task was sent for.** Three two-sided compliant elements exist — a transverse duplex flexure (span **24.61 nm = 72 bp**), a crossover-hinge flexure (arm **4.11 nm = 12 bp**) and an antagonistic ssDNA pair — but the *preload* they were wanted for turns out to be unnecessary: **a two-sided coupling makes the potential above `L₀` quadratic, so the requirement is `k_BT/σ² = 0.4602 pN/nm` and §3's own mandate over-supplies it by 72.4× with no preload at all.** The well goes from `C-0021`'s **1.4–5.4 `k_BT`, 0/18 confining** to **959–7582 `k_BT`, 18/18**, and `C-0014`'s eight substrate tethers come out of the design. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** No element below has been built and none is proposed as a sequence design; the spans and arms are quoted in base pairs to make the design statement concrete, not to specify a staple. |
| **Provenance** | `gpd/results/T-23-two-sided-coupling.json`, produced by `anchoring.TwoSidedCouplingStudyKt`; 3 requirement records, 6 element records, 21 design records, **90 solved zero-bias states**, 5 mounting-offset records, 6 pull-in records, 12 convergence records, 15 upstream reproductions; **26 gate-named tests in `TwoSidedCouplingTest`, 109 in `anchoring`, 883 in the suite, 0 failures** on `tools/verify.sh`; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `λ_D = 3.93 nm`; 40 × 40 nm tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` layer models; 45 load paths on `C-0015`'s 3 × 15 grid |
| **Consumes** | [`C-0021`](C-0021-zero-bias-resting-position.md) (the balance, the van der Waals assembly, the quadrature, the preload relation — **re-run, not tabulated**), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, `K2`, the stability floor, the secant/tangent split), [`C-0014`](C-0014-lateral-confinement.md) (the elements, the cable term, `√(k_BT k)/N`), [`C-0015`](C-0015-crossover-phase-and-registration.md) (45 as 3 × 15), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (the fitted crossover hinge constant), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the allowables), [`C-0018`](C-0018-maximum-usable-bias.md) (the pull-in margin), [`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the layer and the field) |
| **Raises** | [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md) against `C-0021` |
| **Challenged by** | [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md), on the **joint** that was proposed to close `E3`'s axial-restraint bracket. See the banner below; and [`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md), from [`C-0040`](C-0040-hinge-line-census.md) (`T-81`), on `E5`'s **free `hingeCount`** — the lattice supplies **1–2** per flexure at 45 paths, where `E5` needs 3 to hold this claim's own 40 pN/nm ceiling at §3's acceptable stroke; [`CH-0055`](../challenges/CH-0055-the-forty-five-path-array-is-not-a-placement.md) (from [`C-0041`](C-0041-flexure-array-packing.md), `T-96`) — against the `Conditions` premise *"45 load paths on `C-0015`'s 3 × 15 grid"*, which has **no plan view at any level count or body size**; the tile carries **15**. **No number here fails to reproduce** |

> ⚠️ **The sentence below — *"a two-nucleotide single-stranded hinge at each end absorbs it"* — is challenged by
> [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md) (2026-08-13), raised by
> [`C-0025`](C-0025-flexure-end-joint.md) (`T-30`), on the ground that a flexible link has no direction: it
> supplies its axial slack isotropically, so it releases the beam along its axis and drops it across.**
>
> **No number, table or verdict here moves.** All four spans (24.61, 39.07, 49.41, 54.91 nm), both tangents
> (33.333, 91.13 pN/nm), the 8.08 and 86.7 pN tensions and the 0.88 nm draw-in demand are reproduced in
> `C-0025` to ≤ 5.9e−4, **as the two limits of one partial-restraint joint model**. What moves is that `E3a`'s
> *ends free to draw in* column is an **idealisation** rather than a buildable joint: no covalent origami motif
> reaches it, and the joint that does — a **duplex standing normal to the sheet**, `S/ℓ` across and `3EI/ℓ³`
> along — lands **inside** this claim's own bracket at a 31.64 nm span and a **37.39 pN/nm** tangent.
> `C-0025` also confirms this claim's *Still open* item 1 as the binding one it says it is, and sharpens
> falsifier 5: the restrained beam's 86.7 pN at the desired stroke is past the **68.1 pN** loading-rate-free
> saturation of the shear allowable, so **no bonded length of any size carries it** (`CH-0029`).

---

## The claim, in one line

**DNA's compliance comes in exactly two kinds — entropic, which only pulls, and bending, which is signed — and this programme reached for the first: `C-0017`'s `K2` put 99.6 % of its compliance in an ssDNA spacer, which is why `C-0021` found the committed coupling supplying *exactly zero* hold-down. Load the same duplex transverse to its own axis, or through a crossover hinge, and it is two-sided at any stiffness the designer wants; and once the coupling can push, the hold-down requirement is no longer a *force* (`k_BT/σ = 1.3806 pN`, which only a coupling above the mandate can supply) but a *stiffness* (`k_BT/σ² = 0.4602 pN/nm`, which §3's own mandate exceeds 72.4-fold), so the design needs no preload, no tethers, and no stiffness it did not already have.**

---

## The cheap bound, which ran before any element and is the headline

`C-0021` derives the hold-down requirement as a **force** and is explicit about why: above `L₀` a non-adsorbing
layer contributes nothing, so a constant hold-down `F` confines the tile through a **linear** potential and the
upward excursion is exponentially distributed, `⟨h − L₀⟩ = k_BT/F`.

**That derivation is a property of a one-sided stack, not of the problem.** A two-sided coupling of stiffness
`k` makes the same potential **quadratic** there, the excursion Gaussian, and the requirement:

| reading | requirement | what §3's own mandate supplies | margin |
|---|---|---|---|
| **one-sided** (`C-0021`) | **`F ≥ k_BT/σ = 1.380649 pN`** | **exactly 0** (`K2`'s `R(0) = 0`) | — |
| **two-sided** (this claim) | **`k ≥ k_BT/σ² = 0.460216 pN/nm`** | **33.3333 pN/nm** | **72.43×** |
| the same as an RMS | `σ_RMS ≤ 3.0 nm` | **0.3525 nm** | 8.51× |

&nbsp;&nbsp;&nbsp;&nbsp;**`F_req = k_req·σ` identically — two-sidedness is worth exactly one power of the position bound.**

Asserted as a gate-1 test over four bounds spanning 15×, because it is an identity and not a coincidence. It
is the same structural fact `C-0021` itself records one line apart (*"`holdDownForceScale(σ)/σ = k_BT/σ²`
identically … the force requirement and the stiffness requirement are the same statement one power of the
bound apart"*) — what is new here is that **which one applies is decided by the coupling's topology, and the
topology is a design variable.** That is [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md).

---

## The catalogue: six elements, sidedness **evaluated** at negative argument

45 paths, §3's 3 nm working point, 2 mM. `R(−0.5 nm)` is the operational test of two-sidedness — never the
geometry, which is what makes `E1`'s pass and `E2`'s failure results rather than assumptions.

| | element | design parameter | `R(−0.5)` [pN] | secant | tangent | `t/s` | per-path peak | verdict |
|---|---|---|---|---|---|---|---|---|
| **`E1`** | axial duplex standoff, 5 nm (`C-0017`'s `K1`) | `S/L` = 220 pN/nm | **−4950** | 9900 | 9900 | 1.000 | 2.22 pN | **two-sided and 297× too stiff** |
| **`E2`** | ssDNA spacer + standoff (`C-0017`'s `K2`) | 8.61 nm = 13.2 nt | **0.000** | 33.341 | 39.022 | 1.170 | 2.22 pN | **compliant and ONE-SIDED, exactly** |
| **`E3a`** | **transverse duplex flexure, ends free to draw in** | **span 24.61 nm = 72 bp** | **−16.67** | **33.333** | **33.333** | 1.000 | **2.22 pN** | **PASS on all four** |
| `E3b` | the same, ends held axially | span 49.41 nm = 145 bp | −2.47 | 33.333 | **91.13** | **2.734** | 8.08 pN | placed, but **2.7× past the compliance ceiling** |
| **`E5`** | **crossover-hinge flexure** | **arm 4.11 nm = 12 bp** | **−16.67** | **33.333** | **33.333** | 1.000 | **3.40 pN** | **PASS on all four, and the most compact** |
| **`E4`** | **antagonistic ssDNA pair** | 45 × 8.57 nm (13 nt) up, **1 × 44.24 nm (68 nt) down** | **−1.45** | 33.333 | 39.654 | 1.190 | 2.24 pN | **PASS, at the cost of a second ground** |

`E2`'s secant (33.341) and tangent (39.022) reproduce `C-0017`'s own 33.333 and 39.010 to 2.4e−4 and 3.1e−4,
by re-running its code — so the one element the programme committed to is graded here against itself.

### The structural statement the catalogue makes, and it is the general answer

> **An element loaded along its own axis must choose.** A hybridised duplex is `S/L` — two-sided and 297× too
> stiff. A single strand is compliant and carries no compression at all. **There is nothing in between on that
> axis, because axial compliance in DNA is entropic and entropy only pulls.**
>
> **An element loaded transverse to its axis, or through a hinge, does not have to choose**, because its
> compliance is *bending* and a bending moment is signed. `c EI/L³` and `k_θ/r²` are as small as the designer
> makes `L` and `r`, and both are exactly linear, so secant = tangent and placement and stability are
> discharged by one number.

**The programme did not miss a part; it searched one axis.** Every coupling element in `C-0014` and `C-0017` —
strut, tether, spacer, standoff — is loaded along its own axis, and on that axis the trade is real.

---

## `E3` — the transverse flexure, and the one design choice that decides it

The flexure is a duplex spanning between two posts of the lever with the tile tied at its **midspan**. Its
compliance is `L³/(c EI)`; its law is **odd**, asserted rather than assumed. Two brackets are carried, each a
design choice rather than a measurement, and together they are worth 2.2× in span and 2.7× in tangent stiffness:

| | pinned ends | clamped ends |
|---|---|---|
| bending factor `c` | 48 | 192 — **exactly 4×**, asserted as a limiting case |
| span at 45 paths, **ends free to draw in** | **24.61 nm = 72 bp** | 39.07 nm = 115 bp |
| span at 45 paths, **ends held axially** | 49.41 nm = 145 bp | 54.91 nm = 162 bp |

**Axial restraint is the binding choice.** If the ends are held, the beam cannot deflect without stretching and
`C-0014`'s cable term reappears in the normal direction, built here from `C-0014`'s own functions:

- the tangent at the working point becomes **91.13 pN/nm** against a 33.333 secant — `t/s = 2.73`, which is
  free stability margin by `C-0017`'s theorem but **2.3× past this task's declared 40 pN/nm compliance ceiling**;
- the beam's own axial tension is 8.08 pN at §3's acceptable 3 nm stroke and **86.7 pN at §3's desired 10 nm**,
  **past the 65 pN nicked-duplex ceiling** — the same `L_min ∝ δ` structure `C-0014` found for in-plane tethers,
  in the other direction;
- and the span no longer fits the tile (49–55 nm against a 40 nm edge), though it fits `C-0017`'s 60 nm lever.

If instead the ends can draw in, the element is exactly linear and the demand it places on its end joints is
**0.88 nm = 2.6 base pairs of in-plane draw-in** (`2.4 δ²/L`, the *same* shape factor for both end conditions —
not obvious, and asserted). **A two-nucleotide single-stranded hinge at each end absorbs it**, and it is loaded
along the beam's axis, not in the normal load path.

---

## `E5` — the crossover hinge, and why it does not have to wait for `T-9`

A single antiparallel crossover as a torsional spring on a short arm:

&nbsp;&nbsp;&nbsp;&nbsp;`1/k = r²/(n k_θ) + r³/(c EI)` → **`r = 4.11 nm = 12 bp` at 45 paths**, with `k_θ = 13.53 pN·nm/rad`.

- **92.5 % of the path compliance is the hinge** and the rest is the arm's bending — leaf `A8.2`'s explicit ask,
  answered for this element.
- the hinge moment resolved over the interhelical distance is **3.40 pN** per crossover at the working point,
  against `C-0006`'s 10 pN unzip allowable — 2.9× of margin, and 1.35× less again if it is resolved over the
  duplex diameter instead.
- `k_θ` is `C-0009`'s **cited, fitted** constant with its own experimental bracket `α ∈ [0.6, 1.2]`, i.e. a
  factor of exactly 2. Because **`r ∝ √k_θ`**, that whole bracket is `3.25 → 4.45 nm` — **1.37× in a length the
  designer chooses anyway**, and every verdict is identical across it.

> **This is the one place in the programme where `T-9`'s missing number is an advantage rather than an
> exposure**: it is used here as a **spring**, which is what it was fitted as, and the design absorbs its
> uncertainty in a length rather than propagating it into a force.

---

## `E4` — the antagonistic pair works, and it is `C-0014`'s tethers under another name

The pair's structure is worth stating because it is what makes the topology usable:

> **the preload is the DIFFERENCE of the two limb tensions and the stiffness is their SUM**, so the hold-down
> and the coupling stiffness are independent design variables even though each limb supplies both.

Sized exactly — 45 up-spacers of **8.57 nm (13 nt)** opposed by **one** down-tether of **44.24 nm (68 nt)**
across the 10 nm layer — it delivers `R(0) = −1.380649 pN`, the thermal scale to the last digit, and a secant
of 33.333 pN/nm. Two findings:

1. **An entropic down-limb's stiffness is its tension over the span.** A Gaussian chain has `k = f/x`, so a
   hold-down of `F` across a layer of height `h` contributes exactly `F/h` of coupling stiffness — 0.138 pN/nm
   at the thermal scale, **0.4 % of the mandate**. A hold-down bought this way is *free* in stiffness.
2. **A small preload needs a long chain, and fewer of them.** `L_c = 3 n k_BT h/(b F)`, so the thermal-scale
   hold-down is **one 68 nt tether**, and eight of them would need 264 nt each. `C-0014`'s eight 79 nt tethers
   deliver 9.42 pN because their contour was set by the *lateral* requirement, not by this one.

But the down limb is grounded on the **substrate**, so `E4` is `C-0014`'s scheme restructured, not a new part:
it still costs a second ground and a through-layer path. **A genuinely self-contained antagonistic pair would
need the coupling's own frame to reach *under* the tile, and the §1 stack has the polymer layer and the
electrode there.** `E3` and `E5` need neither.

---

## The zero-bias balance, re-solved on `C-0021`'s own machinery

90 solved states: 3 heights × 6 `C-0003` models × 5 scenarios, 2 mM, gold electrode, 2 nm tile,
`C-0021`'s van der Waals assembly, residual field, gravity and quadrature domain rule, all unchanged.

| scenario | descent [nm] | delivered stroke [nm] | `k₀` [pN/nm] | RMS [nm] | in band [nm] | well [`k_BT`] | **confining** | ≤ 3.0 nm |
|---|---|---|---|---|---|---|---|---|
| `C-0021`'s device (`K2` + 8 tethers) | 0.072 – 0.382 | 2.619 – 2.928 | 32.5 – 217.9 | 0.360 – 0.501 | 0.019 – 0.041 | 30.6 – 73.4 | **18/18** | 18/18 |
| **`C-0021`'s device with the tether removed** | 0.018 – 0.296 | 2.704 – 2.982 | 30.5 – 214.9 | **2.558 – 12.984** | 0.166 – 1.069 | **1.4 – 5.4** | **0/18** | **3/18** |
| **`T-23`: two-sided flexure, NO tether, NO preload** | **0.017 – 0.281** | **2.719 – 2.983** | 33.0 – 217.4 | **0.217 – 0.352** | **0.012 – 0.035** | **959 – 7582** | **18/18** | **18/18** |
| the same with one base pair of mounting offset | 0.106 – 0.476 | 2.524 – 2.894 | 38.3 – 227.1 | 0.169 – 0.328 | 0.009 – 0.025 | 251 – 713 | 18/18 | 18/18 |
| the two-sided coupling **alone** (no vdW, no field) | **0.000** | **3.000** | 33.3 – 125.6 | 0.254 – 0.350 | 0.015 – 0.044 | 6568 – 8294 | 18/18 | 18/18 |

**The first two rows are `C-0021`'s own, reproduced to 6.4e−3 and 0 on their own quadrature domain** — 30.6–73.4
and 1.40–5.37 `k_BT`, 18/18 and 0/18 — which is what makes the third row a comparison rather than an assertion.

Three things follow.

1. **The same stiffness, made two-sided, is the whole difference.** No extra part, no extra stiffness, no
   preload: 33.333 pN/nm one-sided is a 1.4–5.4 `k_BT` trap and 33.333 pN/nm two-sided is a well bounded only
   by the integration domain. `C-0021`'s *"a coupling can decide where the tile sits; it cannot be the thing
   that holds it there"* is true of a one-sided coupling and false of this one.
2. **`C-0014`'s eight substrate tethers come out of the design** for `T-13`'s purpose. `C-0017` already showed
   the coupling delivers lateral confinement with 70× to spare; `T-13` was the last thing the tethers were
   needed for.
3. **The tetherless one-sided device does not merely fail to confine — it fails leaf `A1.1` outright**, at 15
   of 18 states (RMS 2.56–12.98 nm against 3.0 nm). `C-0021` reported the well depth for that row and not the
   RMS; the RMS is worse than the well depth suggests.

### `CH-0024`, partly discharged

The delivered stroke rises from **2.619–2.928 nm** (`C-0021`'s device) to **2.719–2.983 nm**, i.e. the shortfall
against §3's 3 nm falls from **2.4–12.7 %** to **0.6–9.4 %**, and to **exactly zero** if van der Waals were
absent. **The residue is van der Waals, which cannot be designed away** — `CH-0024` is not closed by a
two-sided coupling, it is reduced to its irreducible term.

---

## The preload branch, priced in the unit a DNA design actually has

For a two-sided coupling the preload is a **mounting offset** `q`, i.e. a *length*: the element is built
unstressed `q` below `L₀`, so `k = F_t/(δ_t − q)` and **`F_down = F_t q/(δ_t − q)`** — algebraically identical
to `C-0021`'s `F_down = (k_c − k_c*)δ*`, asserted equal to it **absolutely, in pN**, at six offsets.

| offset | `k_c` [pN/nm] | preload [pN] | × the thermal scale | span [nm] | descent [nm] | delivered stroke | stability margin | bias margin (`C-0018` axis) |
|---|---|---|---|---|---|---|---|---|
| **0** | **33.333** | **0** | **0** | **24.61** | 0.025 | **2.975** | **1.194** | 1.0070 |
| 0.12 bp (0.0409 nm) | 33.794 | 1.381 | 1.00 | 24.50 | 0.066 | 2.935 | 1.211 | 1.0075 |
| **1 bp (0.34 nm)** | 37.594 | **12.78** | **9.26** | 23.64 | 0.360 | 2.640 | 1.347 | 1.0118 |
| 2 bp | 43.103 | 29.31 | 21.2 | 22.59 | 0.688 | 2.312 | 1.544 | 1.0172 |
| 5 bp | 76.923 | 130.8 | 94.7 | 18.62 | 1.662 | 1.338 | 2.756 | 1.0406 |

> **The preload is quantised by the base pair, and the quantum is 9.3× the requirement.** The thermal-scale
> hold-down asks for a mounting offset of **0.0409 nm — an eighth of a base-pair rise**, below anything an
> assembly can set; the smallest offset a design *can* build delivers 12.78 pN and costs 0.36 nm of stroke.
> **A design cannot set the preload it would need, which is the sharpest argument for not needing one.**

(`C-0021`'s *"a coupling only 4 % above the mandate would supply the entire thermal-scale hold-down"* is
confirmed and sharpened: **+1.38 % suffices**, and 4 % over-supplies it 2.9-fold.)

---

## The second thing a stiffer coupling was supposed to buy — and it does not

`C-0017`'s stability floor at 10 nm / 2 mM is reproduced here **independently, from the field**, at
**23.413 – 27.912 pN/nm** against its published 23.41 – 27.91 (departures 1.5e−4 and 8.5e−5), at operating
biases of **0.128 – 0.180 V** against its published 0.128 – 0.180. So the *stiffness* axis is exact.

On the **bias** axis it buys almost nothing, and why is itself a result:

| axis | elasticity `d ln\|k_eff\|/d ln V` | margin at the mandate | margin at 1 bp of offset | gain |
|---|---|---|---|---|
| **held gap** (derived here) | **1.88 – 2.79** | 1.099 | 1.172 | +6.6 % |
| **`C-0018`'s fold** (moving equilibrium, implied by its own pair) | **11.2 – 25.5** | **1.007 – 1.032** | **1.012 – 1.043** | **+0.5 – 1.1 %** |

> **Most of the steepness of `C-0018`'s fold is the equilibrium MOVING, not the field stiffening** — the held
> gap's own `|k_eff|` rises only as `V^1.9–2.8`, four to thirteen times gentler. Either way the conclusion is
> the same and it is `C-0017`'s and `C-0018`'s: **the buffer is the lever (6×), not the coupling (1.005–1.011×).**

The two rows are **different quantities** and are recorded as such in the result file (`definitional: true`),
never as a failed reproduction.

---

## The path count is set by the allowable, not by the stiffness

21 design points — element × path count × bracket. **10 pass.**

| paths | flexure span (free) | hinge arm | per-path static | verdict |
|---|---|---|---|---|
| **8** | 13.8 – 22.0 nm | 1.39 – 1.93 nm | **12.5 pN** | **every element FAILS unzip** — on the static share alone |
| 15 | 17.1 – 27.1 nm | 1.89 – 2.62 nm | 6.67 pN | all pass |
| **45** | **24.6 – 39.1 nm** | **3.25 – 4.45 nm** | **2.22 pN** | all pass, with 4.5× and 2.9× of margin |

**More paths make each flexure *longer*, not shorter** (`L ∝ n^(1/3)`), so the count that `C-0015` fixed for
flatness is also the count that makes the element buildable and safe. That is a third independent route to 45.

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/TwoSidedCouplingTest.kt`; **109 `anchoring`
tests, 883 in the suite, 0 failures**, on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | **`k_BT/σ = (k_BT/σ²)·σ` at four bounds spanning 15×** — the currency identity; a flexure stiffness is `EI` over a cubed length and doubling the span divides it by exactly 8; a hinge stiffness is a torsional constant over a squared arm and quarters at double radius; a mounting offset is a length and its preload a force, with zero offset returning §3's mandate exactly; unphysical arguments throw | **PASS** |
| **2 — limiting cases** | **sidedness decided at negative argument for all five elements** — flexure, hinge and axial standoff push, the ssDNA path returns exactly 0 in *both* reaction and tangent; a symmetric flexure's law is **odd** and its tangent **even**, under both end conditions and both restraint readings; the two end conditions differ by **exactly 4**; the membrane term is **zero at zero deflection** and cubic thereafter (8.00× per doubling); an axially free flexure has secant = tangent and a restrained one is strictly convex; the end draw-in vanishes as `δ²` and is **2.4 δ²/L for both end conditions**; an antagonistic pair of identical limbs carries **zero** preload while its stiffness is exactly the **sum**; a zero mounting offset gives `R(0) = 0` and still resists upward motion; the preload diverges as the offset approaches the stroke | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the design span reproduces its own target secant to **1e−8** under both end conditions and both restraints; it is **exactly scan-independent** over 32 → 4096 steps (departure `0.00e+00`) and exits on the **bracket width**; the hinge arm likewise; the Boltzmann quadrature converges monotonically in the panel count (`2.9e−5 → 1.4e−6 → 7.8e−7` against 8000); the field converges in the mesh (`7.8e−4 → 1.9e−4 → 3.7e−5` at 200/400/800 against 1600); **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature cross-check** | `Gen1Tile`'s fitted crossover hinge constant reproduced from `2αB/(100a)` and its `α` bracket shown to be a factor of exactly 2; `C-0017`'s `K1` reproduced (220 pN/nm, 9900, **297×**); `C-0017`'s `K2` secant and tangent reproduced (33.341 and 39.022 against 33.333 and 39.010); `C-0021`'s `M2` exact zero re-evaluated; `C-0021`'s thermal scale and `C-0014`'s `A1.1` bound reproduced from `k_BT` alone; `C-0008`'s zero-bias column reproduced (−0.404 against −0.41, −0.0778 against −0.078); `C-0014`'s eight-tether preload reproduced (9.42 against 9.37); **`C-0021`'s two device rows reproduced on their own quadrature domain** (1.391 against 1.40; 5.371 against 5.37); **`C-0017`'s entire stability floor at 10 nm / 2 mM reproduced from the field** (23.413/27.912 against 23.41/27.91); the base-pair preload quantum, 12.78 pN = 9.26× the requirement | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The mounting offset and `C-0021`'s stiffness relation agree to the last bit.** `F_t q/(δ_t − q)` computed
   from a *geometry* against `(k_c − k_c*)δ*` computed from a *stiffness*, at six offsets, **compared absolutely
   in pN** — because near the mandate they are a catastrophic cancellation of each other (`CLAUDE.md`).
2. **The analytic tangent against a central difference of the law**, at three deflections × two end conditions
   × two restraints, to 1e−6. The membrane tangent `(2S/a)(1 − a³/r³)` is derived, not differenced.
3. **The same quadrature reproduces both closed forms it must.** Over a two-sided harmonic potential it returns
   `√(k_BT/k)` to 1e−6; over a one-sided linear one it returns `k_BT/F` to 1e−3. The requirement's two
   currencies are therefore checked against the *same* integrator.
4. **The escape barrier scales as the domain squared for a two-sided element and linearly for a one-sided one**,
   to 1e−3 — a statement about the *shape* of the potential that no amount of correct arithmetic at one domain
   size would give, and it is the executable form of "a confinement, not a trap".

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | **no element with a signed reaction below 40 pN/nm** | **no** | three, and the axial standoff is two-sided as well |
| 2 | a required span or arm outside the buildable envelope | **partly** | the *axially restrained* flexure needs 49–55 nm, past the 40 nm tile though inside `C-0017`'s 60 nm lever. The free flexure and the hinge are 24.6 nm and 4.11 nm |
| 3 | a per-path force above an allowable at the required stiffness | **YES, at 8 paths** | at 8 paths the **static share alone** is 12.5 pN, past the 10 pN unzip allowable, for every element. The count is set by the allowable, not by the stiffness |
| 4 | **the two-sided element failing to change the confinement verdict** | **no** | 0/18 → 18/18, well 1.4–5.4 → 959–7582 `k_BT` |
| 5 | **the membrane term dominating at every span** | **YES for the restrained reading** | tangent/secant 2.73, and 86.7 pN of axial tension at §3's desired 10 nm stroke, past the 65 pN ceiling. It is what makes axial freedom the binding design choice |
| 6 | **the preload needed being far below the base-pair quantum** | **YES, and it is a result** | 0.0409 nm asked against a 0.34 nm quantum — 8.3× — which is the argument for zero preload |
| 7 | **the pull-in margin gain being negligible** | **YES, and it is a result** | +0.5–1.1 % on `C-0018`'s axis. The buffer remains the lever |

A **result that was not anticipated**: the held gap's own bias elasticity is 1.9–2.8 while `C-0018`'s fold
implies 11–25, so **most of the steepness of that fold is the equilibrium moving**, not the field stiffening.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** No element has been built and none is proposed as a sequence design;
  spans and arms are quoted in base pairs to make the design statement concrete, not to specify a staple.
- **Euler-Bernoulli, with the end condition carried as a bracket of exactly 4.** An origami-to-superstructure
  joint is not obviously either, and **which it is is a design choice rather than a measurement** — which is
  why no finite-element model was run to collapse it.
- **The membrane term is the two-term large-deflection model.** At 3 nm on a 25–55 nm span the deflection
  ratio is 5–12 %, inside the range that model covers; at §3's **desired** 10 nm stroke it is 18–40 % and the
  two-term form *understates* the stiffening, so the 10 nm column is a **lower** bound on the tension and the
  verdict there (past the 65 pN ceiling) is conservative.
- **`E5`'s hinge constant is `C-0009`'s cited model input** — CanDo's `1/100` nick softening carried through
  Chen et al.'s fit, with an experimental `α ∈ [0.6, 1.2]`. `T-9` would settle it; because `r ∝ √k_θ` the whole
  bracket is 1.37× in a design length and **no verdict moves across it**.
- **The hinge bond force resolves a moment over the interhelical distance**, which is a construction. The duplex
  diameter would give 1.35× more, still inside the unzip allowable at 45 paths.
- **The lever is assumed vertically AND laterally grounded**, exactly as `C-0017` assumes — and a two-sided
  coupling makes that assumption load-bearing in a way a one-sided one did not: the lever now has to react a
  downward push as well as an upward pull.
- **The zero-bias balance is `C-0021`'s, re-run unchanged**, and inherits every one of its limits: the van der
  Waals combining relation, the **unspecified electrode material (2.6×)**, the unsourced electrolyte screening
  of the zero-frequency term, and the single-layer effective-medium caveat.
- **The layer is `C-0003`'s at `C-0001`'s single grafting density per height**, not `C-0011`'s solved SCF
  profile; `C-0016` reports the solved layer 1.22× outside that bracket at 5 nm.
- **Mean-field electrostatics, inherited whole.** `C-0005`'s one-loop correction is 123–214 % across this gap
  range — larger than every margin in the pull-in section, which is therefore reported as a **sensitivity** and
  not as a ceiling. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **The pull-in reading is taken at the HELD gap**, not on `C-0018`'s moving equilibrium path. The two are
  different quantities and are labelled as such; the stiffness floor they share reproduces to 1.5e−4.
- **The flexure is one beam per load path.** A real superstructure would carry many on a common sheet, where
  crossovers between neighbours would stiffen the array. Independent leaf springs are the **compliant** reading,
  and the compliance **ceiling** is the binding side — so this assumption is not conservative and is flagged.
- **No dishing.** `C-0006` rejects the rigid-plate assumption under any concentrated load; the 45 attachments
  are `C-0015`'s flatness grid, cited and not recomputed against the layer this task loads.
- **No biased states, no lateral coordinate.** `T-3`/`T-4` and `T-12` own them.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI`, `GJ` | 230, 460 pN·nm² | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012), **not measurements**. CanDo's implied `L_p` is 55.5 nm against ~40–47 measured, so this is the **stiff** end and the spans here are correspondingly long |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997), in Mg²⁺ |
| crossover hinge constant `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009`/`Gen1Tile`. The `1/100` is CanDo's nick softening |
| ssDNA Kuhn length | **2.10 nm, zero-force end** | **CITED, MEASURED**, Chen et al., *PNAS* **109**:799 (2012). The **method-systematic** bracket is 1.34–1.41 nm from 10–40 pN force spectroscopy (Bosco et al., *NAR* **42**:2064, 2014) against 2.10–2.84 nm at zero force; these elements carry ~1–2 pN, an order below the lowest force the spectroscopy fits cover, so the zero-force end is the applicable one — and it is the soft one, hence conservative |
| ssDNA contour per nucleotide | 0.65 nm, inextensible | **CITED, MEASURED** (Sim et al. 2012; Bosco et al. 2014). The convention travels with the number |
| rise per base pair, interhelical distance | 0.34 nm; 2.69 nm | **CITED** (Douglas et al. 2009; Fischer et al. 2016, SAXS, **MEASURED**) |
| per-path allowables | 10 / 48 / 65 pN | **CITED** via `C-0006`. **Not** §4(f)'s 35–60 pN whole-cross-section band |
| `C-0017`'s stability floor, `C-0018`'s bias margin | 23.41–27.91 pN/nm; 1.007–1.032 | **CITED**, and used to grade this task's own derived reading of both |
| §3/§6 targets | 100 pN, 3 nm, 10 nm, 3.0 nm, 1 kHz, 40 × 40 nm, 5/7/10 nm, 2 mM | **CITED** |

Everything else — every element law, secant, tangent, span, arm, draw-in, per-path force, equilibrium,
Boltzmann moment, mounting offset, operating bias and elasticity — is **derived here in code**, with `C-0021`'s,
`C-0017`'s, `C-0014`'s, `C-0008`'s and `C-0003`'s pipelines **re-run rather than tabulated**; the worst
relative departure over the fifteen reproductions is **1.5e−2** (`C-0008`'s two-significant-digit −0.41 pN).

## Still open — named, not answered

1. **How an origami joint is built decides the flexure's end condition *and* its axial restraint**, and those
   two together are 2.2× in span and 2.7× in tangent stiffness. It is a sequence-design question, and it is the
   first thing this task would hand to a designer.
2. **Whether a flexure array on a common superstructure stays as compliant as independent leaf springs.**
   Crossovers between neighbours would stiffen it, and the compliance **ceiling** is the binding side.
3. **The dishing a two-sided coupling causes.** It reacts in *both* directions, so during a thermal excursion
   the tile is loaded upward at some attachments and downward at others — a case `C-0006`'s uniform-load
   exact-rigidity result does not cover.
4. **Whether the lever's own joints are two-sided.** A coupling that can push is only as two-sided as the path
   behind it, and `C-0017` budgets the lever as a section requirement rather than as a jointed structure.
5. **`T-9`**, still — not for `E5`'s verdict, which is invariant across its bracket, but for the arm length a
   builder would cut.

## Challenges

**Raises [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md)**
against `C-0021` — its hold-down requirement, `F ≥ k_BT/3 nm`, and the sentence *"a coupling can decide where
the tile sits; it cannot be the thing that holds it there"*, are properties of a **one-sided** stack and not of
the problem. **No number in `C-0021` moves** — every one of its 144 states is a one-sided stack and all are
reproduced here — and the correction runs the **favourable** way.

**None stands against this claim.** The three ways it would fail:

1. **A joint model showing that an origami beam end cannot draw in.** Then only the restrained flexure exists,
   it fails the compliance ceiling by 2.3× and breaks the 65 pN ceiling at the desired stroke, and the answer
   falls back to the hinge (`E5`), which is unaffected because it accommodates its rotation in a hinge rather
   than in a span.
2. **A measurement of `k_θ` far outside `α ∈ [0.6, 1.2]`.** `r ∝ √k_θ`, so it would take a factor of ~10 to
   move the arm outside the buildable range, and `E3` is unaffected.
3. **A superstructure that cannot react a downward push.** Then no coupling is two-sided whatever the element
   is, and `C-0021`'s force requirement returns — with `C-0014`'s tethers, or `E4`'s single 68 nt limb, as the
   answer.
