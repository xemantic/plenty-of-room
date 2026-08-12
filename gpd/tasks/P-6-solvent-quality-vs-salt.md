# P-6 — `χ(T, salt)` and the Mg²⁺ salting-out coefficient for PEG/water

| | |
|---|---|
| **Task** | `P-6` (process blocker, split out of `P-3` when `P-3` could not close it) |
| **Leaf** | none — premise task under `A2.1`, consumed by `A2.2` (`T-3`) |
| **Verification type** | logical + in-silico, closed against published measurement |
| **Blocks** | `T-3` (stroke and blocking force vs bias, incl. ionic screening), and any later claim that the layer's mechanics and the buffer are independent |
| **Raised by** | `C-0002`, still-open item 1: *"`χ(T, salt)` in 2–10 mM MgCl₂ is not determined … that bound is an argument, not a citation"* |

---

## Formulate

### The question

`C-0002` closed the PEG parameter sheet against a **measured** osmotic equation of state and, in doing so,
never needed `χ` at all — the adopted equation of state is non-virial by construction, so it yields neither
`A₂` nor `χ`. That was a legitimate way to avoid the question. It is not a way to answer it, and §2 of the
problem definition asks the question directly:

> PEG/water also has an unusually mobile `χ`, for reasons that are specific to it rather than generic:
> it shows reentrant (LCST-type) phase behaviour, **kosmotropic salts drive it toward poor-solvent conditions**
> — that is the mechanism behind cloud-point grafting … We would rather these were checked at our operating
> point than inherited from the dilute-solution textbook case.

`T-3` is next in the queue and will sweep the 2 / 5 / 10 mM MgCl₂ buffers of §3. If solvent quality moves with
ionic strength, then the layer stiffness moves with the buffer, and screening and mechanics are **coupled**
rather than independent — which nothing downstream currently assumes. So the question is not "what is `χ`":

> **By how much does PEG/water's solvent quality — and therefore the grafted layer's osmotic modulus and
> stiffness — change between the Gen-1 buffers at 300 K? And is that change even resolvable against the
> uncertainty the same quantity already carries in pure water?**

The second half is not rhetorical. It turned out to decide the task.

### Numeric target and acceptance predicate

**Acceptance (falsifiable), discharged by *either* branch:**

- **(a)** The change in PEG/water solvent quality across the buffer range at 300 K is bounded, expressed as
  `Δχ`, as `Δv/v`, and as the consequent fractional change in the layer's **osmotic modulus** — which is the
  quantity the stiffness is proportional to, since `k/A = K/h` at fixed `N` and `σ`;
  **or**
- **(b)** it is shown that the available methods cannot determine it, **with the reason and the missing
  measurement named**, per §7 of the problem definition.
- **(c)** Every number carries a provenance flag and a source, per `P-3`'s precedent. A number flagged
  `DERIVED` is recomputed in code from more primitive inputs.
- **(d)** The **sign** asserted by §2 — kosmotropic salts drive PEG toward poor solvent — is checked for
  `MgCl₂` specifically rather than inherited from the Hofmeister series as a whole.
- **(e)** `C-0002`'s standing bound (*"10 mM of a divalent chloride shifts `θ` by O(0.1–1 K) out of 375 K,
  ≤ 0.7 % of `τ`"*) is either substantiated or contradicted. A contradiction is filed as a challenge, never
  as an edit.
- **(f)** The **concentration the answer applies to** is stated. `C-0005` shows the layer does not sit in the
  buffer: bulk salt is depleted into it while the tile's counterions flood it.
- **(g)** Anything that cannot be pinned is named as such and left open.

**Falsification of the task itself, stated in advance:** if a published `k_s` for `PEG + MgCl₂` is found and
exceeds ≈ 93 K/M (the threshold computed below), the buffer moves the layer's modulus by more than 1 % across
2–10 mM, the "mechanics are buffer-independent" hand-off to `T-3` fails, and `T-3` must carry a
salt-dependent stiffness. If instead `Δχ` across the buffer range turns out to be larger than the *existing*
spread in `χ` for PEG in pure water, then this task is measuring the wrong thing and the base `χ` must be
pinned first.

### Units, temperature, medium

Locked, per `Physics.kt`: lengths nm, volumes nm³, pressures pN/nm² (= MPa exactly), `k_BT = 4.142 pN·nm` at
**300 K**. Molarity in `mol/L`, number density in `nm⁻³`, cloud-point slope in **K per mol/L**.

**Medium:** aqueous. The Gen-1 buffer is 2 / 5 / 10 mM MgCl₂ — but per `C-0005` the *layer-local* Mg²⁺
concentration spans roughly **1 mM to 66 mM**, and both ends are computed here.

### Conventions fixed before deriving

`P-3` had to disarm the three meanings of `a`. This task has to disarm two more, and both hide a factor of two.

| trap | statement |
|---|---|
| **`χ` lives on a lattice, and the lattice site is not always the monomer** | The measured PEG/water `χ` is defined on a **water-molecule** site. The monomer excluded volume is `v = v₀·(v₀/v_site)·(1 − 2χ)`, and for PEG `v₀/v_water = 2.010`. Writing `v = v₀(1 − 2χ)` with a water-lattice `χ` halves the excluded volume. |
| **`B₂` is per chain, `v` is per monomer pair** | `B₂ = N²v/2` in the mean-field (nearly-Gaussian) limit. |
| **sign of `k_s`** | Positive `k_s` = salting **out** = cloud point falls = poorer solvent. Negative = salting in. Both are carried, because for `MgCl₂` the sign is not established. |
| **sign of a fractional shift** | Negative `Δv/v` means a **poorer** solvent. |
| **where `dχ/dT` is read** | At the **cloud point**, not at 300 K. For PEG/water they differ by 51 %, and using the wrong one is a silent 51 % error. |

---

## Plan

### Method, and why this one

**Cheap bound first, and there are two of them, in order.**

1. **Channel inventory before any number.** PEG is electrically neutral, so the buffer has exactly two ways to
   reach the layer's mechanics: through the *mobile ions* (an osmotic/depletion term), and through *solvent
   quality* (a `χ`). Channel 1 is pure arithmetic and is done first, because if it does not vanish the whole
   task changes shape — 10 mM MgCl₂ carries **3.5×** the osmotic pressure the polymer layer itself musters,
   which is not a term one may assume away. It is shown here to vanish **exactly**, by a Legendre-transform
   argument, not to be small.
2. **Then the literature, for channel 2.** The chain is
   `k_s → ΔT_cp → Δχ → Δv → Δα → ΔΠ → ΔK → Δk`, and every link is closed form. There is no simulation here
   and there should not be: an MD or SCF calculation of PEG's excluded volume in MgCl₂ would cost days, would
   have to reproduce a Hofmeister effect that MD force fields are notoriously bad at, and would be *less*
   trustworthy than the scattering and cloud-point measurements that already exist. The expensive method is
   not merely unnecessary — for this quantity it is worse. That is the cost justification §5 asks for.
3. **If the coefficient cannot be sourced, bound it instead.** Two constructions, both falsifiable:
   the **ceiling** — the largest cloud-point slope any salt reaches for PEO, applied to a chloride that the
   same source says is much weaker — and the **threshold** — the slope `MgCl₂` would have to have for the
   answer to matter. The pair converts a missing measurement into a decision.
4. **Read the papers, per CLAUDE.md.** `pdftotext -layout` on downloaded PDFs; Crossref and EuropePMC REST for
   abstracts where publishers answer with a 403. Every number in the claim is either from a passage read on
   disk or from an abstract retrieved verbatim from Crossref/EuropePMC, and which of the two is stated.

### What would falsify this approach

Stated in advance:

- **Channel 1 does not vanish.** If the mobile-ion term contributed a pressure, the coupling would be
  first-order in salt concentration, linear, and large — and the whole `χ` question would be a second-order
  detail. (It vanishes. The test asserts the cancellation to `1e-9` of the ion pressure scale.)
- **A published `k_s` for MgCl₂ above ≈ 93 K/M.** Then the buffer moves the modulus by > 1 % and `T-3` must
  carry it.
- **`Δχ` across the buffer range comparable to the spread in `χ` for pure water.** Then the base value has to
  be pinned first and this task is premature. (The ratio came out at **239×** the other way, which is the
  headline.)
- **A `χ` for a *grafted* PEG layer far from the bulk value.** Then a bulk `χ`, however well measured, is the
  wrong input, and the task's whole currency is wrong. **This one fired**, and it is the largest single
  finding of the iteration — see `C-0007` and the proposed `P-9`.

### Cost

Minutes of compute. The expense is in the reading, which is where it belongs for a premise task —
and a substantial part of the reading returned **nothing**, which is itself part of the deliverable.

---

## Execute

`src/main/kotlin/material/SolventQuality.kt`, additive to the `material` package (`PegWater.kt` and
`OsmoticEquationOfState.kt` are untouched — `T-1c` was editing them concurrently).
Entry point `material.SolventQualitySaltStudyKt`, emitting `gpd/results/P-6-solvent-quality-vs-salt.json`.
Tests in `src/test/kotlin/material/SolventQualityTest.kt`, written first (40 tests).

Results are carried in [`C-0007`](../claims/C-0007-solvent-quality-vs-salt.md). The short version:

| quantity | value |
|---|---|
| `χ` of PEG/water at 300 K, **measured** | **0.372** (0.378 on the paper's second fit) |
| `dχ/dT` at 300 K | `+2.61e-3` K⁻¹ |
| `dχ/dT` at the 369 K cloud point | `+1.73e-3` K⁻¹ |
| monomer excluded volume `v` | **0.0311 nm³** (0.0269 nm³ from the independent `B₂` route, 16 % apart) |
| `Δv/v` per kelvin of cloud-point depression | **−1.35 %** |
| ceiling on `k_s` for any PEO salt | **69 K/M** |
| `Δv/v` across 2 → 10 mM at that ceiling | **−0.74 %** |
| `ΔK/K` across 2 → 10 mM at that ceiling | **−0.40 %** |
| `k_s` needed to move `v` by 1 % across 2 → 10 mM | **92.8 K/M** — above the ceiling |
| `ΔK/K` across the layer-local 1 → 66 mM span | **−3.3 %** |
| osmotic pressure from ideal excluded salt | **exactly 0** |

---

## Verify — the five gates

### 1. Dimensional consistency — **PASS**

`1 mol/L = N_A/10²⁴ nm⁻³ = 0.602214 nm⁻³`, pinned. The blob relation
`α = C·v₀·v_K^(3/4)·b^(3/2)·v_K,vol^(−9/4)` is `nm^(3 + 9/4 + 3/2 − 27/4) = nm⁰`, verified by scaling every
length by 1.7 and asserting `α` is unchanged to `1e-12` — a dimensional check run as an experiment rather
than counted on paper. Free-energy density and pressure share the unit `pN/nm²`, as they must.

### 2. Limiting cases — **PASS**

`χ → ½` and `v → 0` at the theta temperature, to `1e-14`. `v < 0` above it — PEG/water is LCST, so the poor
solvent is on the *hot* side, and getting that backwards would invert every conclusion about salt.
`B₂ = 0` at its own theta temperature and negative above. A zero-width buffer step gives exactly zero shift.
The two transfer exponents bracket the answer: 3/4 (blob) < 1 (mean field), and the bound is taken on the
larger.

### 3. Symmetry and conservation — **PASS**, and this gate carries the main result of channel 1

Ideal mobile ions excluded from the polymer's own volume contribute a free-energy density `k_BT n_s φ`,
**strictly linear in `φ`**, and a linear term is annihilated by `Π = φ f′ − f`. Asserted numerically to
`1e-9` of the ion pressure scale. Its physical restatement is a conservation law: the layer's polymer volume
per unit area is conserved under compression and the excluded-salt energy is proportional to exactly that, so
the total is **independent of layer height** — asserted at 4, 7 and 10 nm to `1e-12`. The salt therefore
exerts **no force on the tile at any height at any concentration**, at ideal order.
Sign symmetry is also checked: salting-in and salting-out of equal magnitude give exactly opposite shifts.

### 4. Numerical convergence — **PASS**

The Legendre transform is a central difference; halving the step cuts the error by more than 3× (second
order, as expected). The transform reproduces the closed-form equation of state at four volume fractions to
`1e-6`. The blob relation round-trips to `1e-12`, and its logarithmic derivative is verified to be exactly
3/4 at four different values of the unknown prefactor — the property the whole transfer function relies on.

### 5. Literature cross-check — **PASS**, on four independent legs

1. **The theta temperature is recovered from the `χ` fit that did not fit it.** `χ = a + b/T` crosses ½ at
   **358.69 K**; the same paper's independent quadratic fit reports **358.85 ± 1.1 K**. Two analyses of one
   dataset agreeing to 0.2 K.
2. **Two routes to the excluded volume agree to 16 %.** `v` from `χ(300 K)` = 0.0311 nm³; `v` from the
   measured `B₂(T)` = 0.0269 nm³. They rest on theta temperatures 14 K apart, so 16 % is good — and the
   *wrong* lattice convention would have missed the agreement by 42 %, which is how the convention was caught.
3. **The measured crossover index `α = 0.49` is reproduced to an order-unity blob prefactor.** The blob
   relation at `C = 1` gives 1.22 from the `χ` route and 1.09 from the `B₂` route, i.e. `C = 0.40` and `0.45`
   — two independent inputs giving the same prefactor to 12 %.
4. **The premise of the invoked scaling law is checked against the actual material.** The `9/4` exponent
   belongs to the *good-solvent* semidilute window, whose upper edge is the thermal-blob volume fraction
   `φ** = v/v₀`. Measured, that is **0.516**, and the layer sits at 0.029–0.071, i.e. 6–14 % of it.
   The premise holds with a factor of 7 to spare — and it would have held with only a factor of 3 had the
   inherited `χ = 0.45` been used, so checking it mattered.

---

## What could not be verified, and is therefore not claimed

- **No `k_s` for PEG + MgCl₂.** The one study that measured Group II chlorides (Boucher & Hines 1976) is
  paywalled; only its abstract was obtained, verbatim, from Crossref.
- **The 69 K/M ceiling is a construction, not a citation.** It is built from the verbatim abstract statement
  that `θ` across that survey lies between 300 and 360 K against a salt-free 369 ± 3 K, with molar
  concentrations. It is conservative — if the extremes occur above 1 M, the true ceiling is lower — and it is
  falsified by any published PEO salt slope above 69 K/M.
- **The grafted-`χ` number is quoted from a verbatim abstract**, not from a passage in the body, and is used
  only as a flag, never as an input.
