# ANSWERS — the Gen-1 questions, as posed

This file answers [the problem definition](third-party/2026-08-ndi-gen1-problem-definition.md) in its own terms:
the eight tasks of §6, the open questions of §4, and §7's test of whether the loop worked.

It is a **synthesis, not a source**.
Every number here belongs to a claim in [`gpd/claims/`](gpd/claims/), and the claim carries the provenance,
the validity range and the verdict.
Where a claim has been challenged, the challenge is named — this project does not overwrite results.

**Maturity: TRL 1–3 throughout.**
`PASS` means model-consistent and traceable.
**Nothing in this repository is measured.**
Conditions are 300 K, aqueous buffer with stated Mg²⁺, `k_BT = 4.142 pN·nm`, unless a row says otherwise.

---

## 1. The short version

Three findings dominate, and none of them is a stiffness number.

1. **The tile is not a rigid plate, and the picture has to go.** It is rigid *exactly* under a uniform load —
   at any flexural rigidity — and dishes under every departure from uniformity, including the unavoidable one
   at 300 K. A point-coupled lever and an area-averaging charge sensor **do not measure the same displacement**;
   they differ by 26 % of the stroke. §4(g)'s own test for abandoning the rigid-plate assumption is met.
   (`C-0006`, `C-0009`, `CH-0005`.)
2. **The polymer layer confines the tile in one direction only.** Its lateral restoring stiffness is *exactly*
   zero by symmetry — not small — so an untethered tile diffuses 63 nm in one 1 kHz period, 21× the positional
   predicate. It also exerts no upward force above `L₀`, so at zero bias the tile is unconfined in **both**
   directions. Nothing in the §3 stack holds it. (`C-0010`.)
3. **The output coupling decides the programme, and it is fixed by §3 rather than by the layer.**
   100 pN at ≤ 2 V is reachable with room to spare and drainage clears 1 kHz by 22× — and at 10 nm the
   operating point the device reaches them at is not one it holds by itself.
   But the number a lever must bring is **not** the 5–277 pN/nm the stability table suggested: the force
   delivered to a load over a stroke is `k_c·Δs`, so §3's own 100 pN and 3 nm fix it at **33.333 pN/nm** by
   arithmetic, and read at the bias the device actually operates at the stability floor is **0 at 5 and 7 nm
   and 23.4–27.9 pN/nm at 10 nm**.
   A coupling of **45 attachments — the same grid flatness already needs** — each a duplex standoff in series
   with a **13-nucleotide tuned ssDNA spacer**, supplies it: it *places* the operating point on its secant and
   *stabilises* it on its tangent, at 2.2 pN per load path against a 10 pN allowable.
   **The design window is not empty.**
   The margin at §3's own 2 mM is 1.19–1.42×, inside its own mean-field error, so the verdict is
   *not excluded*, never established — and dropping to 0.5 mM buys six times more.
   (`C-0012`, `C-0016`, `C-0017`, `CH-0016`.)

And the shape of the whole problem, which no single number shows: **static stability wants the thin layer,
whose window is empty by 13.3×; the window, the stroke and the force-versus-height trade all want the thick
one, whose operating point is unstable everywhere.** All three pull the same way and stability pulls against
all three.

Methodologically, the finding worth most is that **this project repeatedly caught itself** — concluding a
direction from the corrections it happened to have (`CH-0002`), quoting a `χ` that was a units error assembled
from an abstract (`CH-0012`), sampling a layout space it believed it had swept (`CH-0014`), and trusting two
models that agreed with each other because they shared a defect (`C-0011`).

---

## 2. The eight tasks of §6

| # | Task | Leaf | Verdict | Claim |
|---|---|---|---|---|
| 1 | Stiffness of the polymer layer | `A2.1` | **PASS**, then **superseded twice** | `C-0001` → `C-0003` → `C-0011` |
| 2 | Feasible design window | `A2.1` | **PASS** — non-empty at 7 and 10 nm, empty at 5 nm; P2 closed by `C-0017` | `C-0016`, `C-0017` |
| 3 | Stroke and blocking force vs bias | `A2.2` | **PASS** — reachable, but the operating point is not holdable | `C-0008`, `C-0012` |
| 4 | Electrostatic softening and pull-in | new | **partly answered by `C-0012`/`CH-0011`**; re-formulated, still open | `C-0012` |
| 5 | Load distribution across the origami | `A1.2` | **PASS** | `C-0006`, `C-0009` |
| 5b | Deflected shape of the tile | `A8.2` | **PASS**, verdict *rigid plate rejected* | `C-0006`, `C-0009` |
| 6 | Validity boundary of mean-field screening | `A7.4` | **PASS** | `C-0005`, `C-0008` |
| 7 | Poroelastic drainage time | new | **PASS** — not binding, boundary named | `C-0004` |
| 8 | Tile positional variance | `A1.2` | **PASS** at the operating point, **partial** against the leaf | `C-0010` |

### Task 1 — stiffness

`L₀ = N a^(5/3) σ^(1/3)` is **replaced**, not merely re-parameterised.
It is a *two-body* result; the des Cloizeaux interaction gives `σ^(5/13)`, and the blob construction gives
`σ^(1/3)` again only because it minimises against blob elasticity rather than Gaussian.
So "which height law" reduces to "which elasticity", which is a checkable material question — and the check
says PEG in water is a **marginal** solvent whose Gen-1 chains carry 0.02–0.10 of one thermal blob.
The chains are not swollen, there are no blobs, and every blob-based statement made about this layer across
three iterations was about a structure it does not have.

Stiffness is **not a single number at the resting height** and never was: the strong-stretching pressure
vanishes quadratically at `L₀`, so three of six models give exactly zero there.
Quote it at a stated compression. At the working point, 47.7–64.1 pN/nm over the 40 × 40 nm tile.

### Task 2 — the design window

**Answered, and the answer changes shape halfway through.**
In the axes §4(a)–(d) names the window is **not empty**: `σ ∈ [0.0116, 0.2601] nm⁻²` at 10 nm — 22.4× wide —
and `[0.0296, 0.0496]` at 7 nm.
**5 nm is empty**, and the proof names two constraints: the layer must be at least `σ = 0.0751 nm⁻²` for its
coils to overlap at all and at most `σ = 0.00563 nm⁻²` to deliver 3 nm of stroke, **missing each other by
13.3×**.
At both surviving heights the lower edge is coil overlap and the upper edge is the 3 nm stroke — §4(a)'s own
tension, quantified. **§4(c) and §4(d) bind nothing anywhere**, at any of 183 grid points.

**The window must be read in the FORCE-ONSET convention** (`L₀` is where the layer carries 1 pN over the tile).
It says: order **PEG of 1.6–3.3 kDa at 10 nm**, or 1.1–1.2 kDa at 7 nm, at a grafting spacing of 2–9 nm.
In the first-moment convention the same layer is ~8–9 kDa.
**That factor of four is the single most likely way this window gets misread at a bench**, and `T-1e` has not
yet separated the definitional part from the physical.

Then the shape changes.
**Three of the five axes this programme discovered are not functions of grafting density at all** — flatness
(45 attachments as 3 × 15, against 56 crossovers), the usable bias window, and the output-coupling stiffness.
They cannot narrow a window; they can only close a height.
So a `(σ, L₀)` window is the wrong object for the Gen-1 decision, and the two axes that *do* resolve in `σ`
both survive: the peak per-load-path force is 3.9–8.9 pN against a 10 pN unzip allowable everywhere inside the
window — **the exceedance `C-0015` found is unreachable, because the solved layer is never as soft as the soft
end of its sweep** — and lateral confinement is a footprint cost (a 97–152 nm assembly around a 40 nm tile)
with **no threshold in §3 to test it against**, which is stated rather than invented.

**What decides the programme is the output coupling, and `T-16` has now evaluated it.**
At 10 nm the §6 operating point is statically unstable at §3's own 2 mM buffer, so it exists only against a
lever supplying its own stiffness — but that stiffness is **33.333 pN/nm, fixed by §3's 100 pN and 3 nm
alone**, and it clears the stability floor at every height, buffer and layer model in the box.
`C-0016`'s `P2` therefore closes **non-empty at 7 and 10 nm**.
What a DNA lever cannot easily be is *compliant* enough: forty-five duplexes in tension are 4950 pN/nm,
148× too stiff, and the element that closes the task is a 10–19 nt ssDNA spacer carrying **99.6 %** of each
path's compliance.
The margin at 2 mM is 1.19–1.42× against a 123–214 % mean-field error, so this is **not excluded rather than
established**, and `T-1f` — bounding that error — is now the binding uncertainty in the programme.

Two further findings travel with it.
**The bias ceiling has to be quoted with the load it was evaluated at**: `C-0012`'s 0.02–0.1 V is a property of
the *unloaded* actuator, which snaps to near-contact, while the tile held at the §6 target sits at a 2–7 nm gap
and `φ ≤ 0.09`, inside every upstream validity range (`CH-0015`).
And **static stability wants the thin layer, whose window is empty, while the window, the stroke and the
force-versus-height trade all want the thick one** — all three pull the same way and stability pulls against
all three.
That inversion, not any single number, is the Gen-1 design problem.

**§3's *desired* ~10 nm stroke remains unreachable** at every height and every grafting density — `C-0001`'s
one surviving headline, now confirmed against a third layer model and a fourth constraint set. (`C-0016`.)

### Task 3 — stroke and blocking force

**Reachable, and the operating point it is reachable at is not one the device can be held at.**
100 pN of blocking force needs 0.065–0.699 V and 100 pN *at* a 3 nm stroke needs 0.082–0.368 V, all inside the
~1 V point-ion boundary with 5–12× of margin.
The actuator is **voltage-saturated above ~0.5 V** — a factor of 8 in bias buys 1.9× in force — so §3's 2 V
ceiling is nearly irrelevant to what the device can do.

But **`k_eff < 0` at the loaded operating point at 7 and 10 nm**, and the *free* operating point leaves three
upstream validity ranges at once above ~0.1 V.
Two consequences that a single "bias needed" figure hides: **the blocking force understates the peak output
force by up to 20×**, because `dW/dh = k_eff` exactly and the characteristic *rises* with stroke wherever the
field softens the layer; and **the two halves of this task run in opposite directions with layer height** —
blocking force 10× harder from 5 to 10 nm, stroke 10× easier. (`C-0012`.)

### Task 4 — electrostatic softening and pull-in

**Partly answered, and NDI's own second branch is nearly right for the wrong reason.**
`k_eff` does reach zero, at the predicate's own operating point — crossing between 0.05 and 0.10 V at 10 nm and
between 0.10 and 0.25 V at 7 nm, **an order of magnitude below** a resting-height estimate.
And the collapse **is** arrested — but by **`k_es` reversing sign at 0.55–1.58 nm**, not by the osmotic
divergence §6 proposes: past the force peak the electrostatics *stiffens* the layer.
What remains open is the maximum usable bias, which upstream validity already caps far below any pull-in
estimate. (`C-0012`, `CH-0011`.)

### Task 6 — mean-field screening

The answer is **yes and no, and the two halves have different reasons.**
Mean field is **uncontrolled** across the whole 5–10 nm working range (the one-loop correction is 123–214 % of
leading for Mg²⁺) yet **qualitatively safe** there, because correlation attraction needs a gap under 1.46 nm
and the layer never allows it. Controlled PB begins only above 12.9 nm.
`Ξ ∝ q³`: the divalence does this, not the surface charge — Na⁺ at the same surface gives 3.0 against 24.

### Task 7 — poroelasticity

**Not binding, by 22× at the §3 worst case and 5.6× under a composite worst case.**
Drainage is a *footprint* problem, not a thickness problem — the thickness cancels and `τ ∝ L²` in the tile
edge — and a **denser** layer drains *faster*, so the binding direction is dilution.
The design would have to leave the poroelastic model's own domain of validity before poroelasticity could bind.

### Task 8 — positional variance

**PASS at the operating point**: 0.87–0.96 nm broadband, 0.069–0.110 nm in band below 1 kHz, against 3.0 nm.
Two qualifications travel with it. The tile's **worst point** (a corner — the centre is the fixed point of both
tilts and therefore the *quietest* place on it) exceeds 3.0 nm in every state softer than the working point.
And the **lateral coordinate is not part of the PASS at all.**

Leaf `A1.2` asks for a *simulated* σ_RMS with a **95 % CI**, and that half is **not discharged**.
The reason is not cost: oxDNA models the origami and **not the polymer layer that sets the answer**, so run as
specified it returns a confidence interval on a different quantity.
A CI on an exact analytic result is a category error, and the model bracket is not one.

---

## 3. The open questions of §4

| | Question | Answer |
|---|---|---|
| (a) | Grafting density and regime | **Four brush criteria have failed here**, each a convention asked to do a measurement's work: `Σ ≥ 5` failed thermodynamically *and* geometrically; `L₀/R₀ ≥ 1`, adopted to replace it, turned out **exactly vacuous** — it admits all 183 points of the sweep. **Coil overlap `Σ = πR₀²σ ≥ 1` is the only criterion that bounds anything**, and it owns the lower edge of every surviving window. Window: see Task 2. |
| (b) | Layer height | **Empty at 5 nm only** — the earlier "empty at 7 nm too" was withdrawn when a solved density profile replaced two trial functions. There is a genuine **trade**, not an ordering: the window, the stroke and the force all want the thick layer, and static stability wants the thin one. |
| (c) | Porosity and ion partitioning | **The sign in the question is backwards.** The layer *excludes* 23–48 % of the salt, so it **lengthens** the local Debye length by 1.14–1.39× and **protects** the field rather than screening it away. It also *amplifies* `F_es` by 1.15–1.60×. The dielectric-decrement mechanism named in §4(c) is a 3.9 % effect — the layer is 97 % water. **The bound is one-sided** (exclusion only); cation coordination by PEG's ether oxygens could flip it, and no binding constant exists in accessible literature. |
| (d) | Poroelasticity | **Not binding**, with the boundary named. See Task 7. |
| (e) | Screening | See Task 6. The force's own decay length is **1.8–2.8 nm** at the working gap — not the 4 nm bulk Debye length, and it is bias-dependent. Leaf `A2.2`'s low-screening operating point is **vindicated twice**: at 10 mM the 100 pN target is unreachable at 7 and 10 nm, at 0.5 mM it is reached even at 10 nm. |
| (f) | Structural survival | The **35–60 pN band is not a per-load-path allowable** — it is a whole-cross-section disassembly force at a stated loading rate, and a DNA rupture force without a loading rate is not a material constant. Per path use duplex shear (~48–65 pN) or unzip (10–15 pN), 65 pN a hard ceiling. **Three load paths clear 35 pN, eleven clear 10 pN — and 45 (as 3 × 15, not 64 as 8 × 8) are needed for flatness, against 56 crossovers.** A rigid anchor is carried by its **two nearest crossovers and essentially nothing else**, so an equal-share figure understates the peak by 2.3–7.6× — but inside the actual design window the peak is 3.9–8.9 pN against a 10 pN unzip allowable, so **the exceedance is unreachable there**. |
| (g) | Does the tile stay flat? | **No.** Rigid *exactly* under a uniform load at any rigidity, and dishing 26–369 % of the stroke under every departure. The lever and the sensor see displacements differing by 26 % of the stroke. §4(g)'s own criterion for abandoning the rigid-plate picture is met. |

---

## 4. §7 — what NDI would count as the loop working

| §7 criterion | Where to check it |
|---|---|
| Inherited numbers get re-derived | `a = 0.35 nm` closed two ways (`C-0002`); `λ_D ≈ 4 nm` re-derived to 1.8 % (`C-0005`); the de Gennes wall mapping derived, not looked up (`C-0001`); the MWC form rebuilt rather than cited. |
| Premises checked against the material | The semidilute premise **failed** (`CH-0001`); the Darcy premise **failed** where it did not change the verdict (`C-0004`); strong stretching is outside its own premise (`CH-0003`); `χ ≈ 0.45` turned out to have **no primary source at all** (`C-0007`). |
| Method justified against cost, cheap bound first | SCF numerics deferred twice on the stated ground that it would be *"calibrating to a guess"*, and bought only once the interaction was anchored in measurement (`T-1d`). MD declined for the Hofmeister effect because it would be *worse* than the existing measurement, not merely dearer (`C-0007`). Explicit-ion MC costed at 1–3 weeks and not run (`C-0005`). |
| Validity ranges travel and are respected | `C-0008` is handed to `T-3` with an explicit may/may-not list; `C-0004` is parameterised by a stiffness that was being re-derived concurrently. |
| Disagreement raised as a challenge, not an overwrite | Nine challenges in [`gpd/challenges/`](gpd/challenges/). `CH-0007` challenges **our own** queue's reading, not a subordinate's. |
| Model-consistent vs measured maintained | Every claim header carries it; no `PASS` in this repository asserts measurement. |
| A feared effect chased down and *dissolved* rather than carried | The "grafted `χ` ≈ 0.60", once thought 239× the salt effect, turned out to be `1.2 × ½` assembled from an abstract, against a model whose own theta is 0.696, for the wrong geometry and the wrong observable (`C-0013`, `CH-0012`). |
| Unanswerable questions stated plainly | The Mg²⁺/PEG binding constant does not exist in accessible literature (`P-8`); the crossover hinge constant is a fitted model input (`T-9`); leaf `A1.2`'s CI is **not discharged** rather than approximated; the intermediate-coupling regime has no systematic theory and the sources say so themselves. |

---

## 5. What we cannot answer, and why

- **The 95 % CI of leaf `A1.2`.** Requires an ensemble; the named tool models the wrong subsystem.
- **The Mg²⁺/PEG coordination constant.** Two independent searches; the mechanism is documented in water, the
  number is not, and the quantitative NMR work is in methanol. It needs a paywalled pull or an experiment.
- **The crossover hinge constant `k_θ`.** No accessible measurement of a single-layer origami sheet's bending
  rigidity exists in any direction. Costed as `T-9` — days of oxDNA on 8 cores.
- **The direction of the correlation correction for *oppositely* charged walls.** Every published coupling
  criterion is a like-charge result. This is the largest uncertainty on every electrostatic force here.
- **A compression measurement of a PEG brush *inside* the Gen-1 grafting window.** None exists. `P-9` bounds
  the bulk-versus-brush `χ` difference at `|Δχ| ≤ 0.053` from *denser* layers, so the bound comes from above
  and assumes monotonicity in grafting density.
- **Two paywalled papers** would close the only genuinely missing measurements. This is an access limit, not a
  compute limit, and it is the first thing this programme has needed that the machine cannot supply.

---

## 6. Reading order

For the process, [`JOURNAL.md`](JOURNAL.md) — decisions and surprises in order, including the ones that
reversed earlier conclusions.
For the live state, [`TASKS.md`](TASKS.md).
For any number in this file, the claim it belongs to in [`gpd/claims/`](gpd/claims/), and the challenge
standing against it in [`gpd/challenges/`](gpd/challenges/).
