# C-0137 — The beyond-mean-field correction reaches `C-0017`'s margin only as a decay rate, the threshold is a 9.7 % shortening, and every channel that can be evaluated is empty, favourable, or worth 1.4 %

| | |
|---|---|
| **Task** | [`T-50`](../tasks/T-50-beyond-mean-field-gap.md) |
| **Leaf** | **`A7.4`**, consumed by `A2.2` and `A8.2` |
| **Verification type** | **logical** (a level/gradient decomposition, and a threshold read off `C-0017`'s own force balance by one division per state) **+ in-silico** (`C-0008`'s 1-D nonlinear Poisson-Boltzmann pipeline re-run over two correction channels and a level ladder) **+ literature** (the decay-length theorem, the primitive model's own measured decay length, and the one published Monte Carlo of two oppositely charged walls, which is counterion-only) |
| **Verdict** | **PASS on `P1`–`P6`. The exposure is bounded and it was not bounded before.** `C-0017`'s 10 nm / 2 mM verdict stays **NOT EXCLUDED, never established** — this claim does not establish it — but the reason it is not established is no longer *"the correction is unbounded"*. **Two declared falsifiers fired and both are recorded**; one of them is a challenge against a standing claim. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The intermediate-coupling regime `Ξ = 17–24` still has no systematic theory (`C-0005`), and no primitive-model Monte Carlo of *this* gap exists in the literature or was run here. What changes is what that ignorance is ignorance **about**. |
| **Provenance** | `gpd/results/T-50-beyond-mean-field-gap.json`, produced by `electrostatics.BeyondMeanFieldGapStudyKt`; 54 threshold records, 6 level thresholds, 30 level-leak records, 8 net member effects, 135 multiplier points, 45 channel gradients, 12 bulk-decay records, 7 literature criteria, 3 convergence records, 12 reproductions; **26 gate-named tests** in `electrostatics.BeyondMeanFieldGapTest`; the result file re-run end to end and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at 0.5 / 1 / 2 / 10 mM (2:1, `I = 3c`); 40 × 40 nm Manning-renormalised tile; PEG layer 5 / 7 / 10 nm; all six `C-0003` layer models; the held operating point at which `W(3 nm) = 100 pN` |
| **Consumes** | [`C-0017`](C-0017-output-coupling-stiffness.md) (the 54 requirement records, **read from its result file and not re-solved**), [`C-0005`](C-0005-mean-field-screening-validity.md) (the coupling parameters, the one-loop ratio, the point-ion boundary, the saturated charge), [`CH-0035`](../challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md)/[`C-0033`](C-0033-collar-on-the-equilibrium-path.md) (the level/gradient split), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the field pipeline, re-run as a library), [`gpd/data/T-50-beyond-mean-field-literature.md`](../data/T-50-beyond-mean-field-literature.md) (38 recorded queries, read flags per number) |
| **Raises** | [`CH-0166`](../challenges/CH-0166-a-level-correction-is-not-exactly-absorbed-into-the-bias.md) against `CH-0035`/`C-0033`, [`CH-0167`](../challenges/CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md) against the corpus-wide use of `C-0005`'s deviation |
| **Answers** | [`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) item 3, which asked for this task; and `C-0005`'s own still-open item 4 |

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; **the electrostatic gap IS the layer height, exactly** (`C-0012`).
- The **stroke** `s = L₀ − h` is positive **downward**; **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`).
- **The beyond-mean-field multiplier is `μ(h, V) ≡ |F_true(h, V)| / |F_PB(h, V)|`**, dimensionless, `μ > 1` an enhancement. Same *shape* as `C-0033`'s collar multiplier and a **different physical correction**; the two compose multiplicatively and their gradients add.
- **`g ≡ d ln μ/dh` in `nm⁻¹`, at FIXED APPLIED BIAS.** `g > 0` means the true force decays **more slowly** than the mean-field one, which makes `|k_es|` smaller and the actuator **more** stable.
- `k_es = |F_es| d ln|F_es|/dh`, `ℓ = −1/(d ln|F_es|/dh)`, so **`k_es = −|F_es|/ℓ` identically**.
- The **stability floor** is `max(0, −k_eff)` and the **margin** is `33.3333 pN/nm / floor` (`C-0017`'s mandate).
- `Ξ = q² l_B/μ_GC` is a property of **one surface**, read from the duplex **cylinder** charge density (`C-0005`).
- Lengths nm, forces pN, stiffness pN/nm, bias V, buffer mM `MgCl₂`, `k_BT = 4.142 pN·nm` at 300 K.

---

## The claim, in one line

**At the operating point `C-0017` is read at, the electrostatic force is pinned by a mechanical balance, so `C-0005`'s 123–214 % — which is a statement about the LEVEL of a correction — cannot be the error bar on a STIFFNESS margin. What reaches the margin is a level *re-solved through the bias* and a decay-rate gradient, and both have thresholds: the true force would have to be `1.48–2.22×` SMALLER than the mean-field one, or decay `9.73 %` faster at the 7 nm held gap in 2 mM. Every member of the family that can be evaluated is far below both — a factor of SIXTEEN swept in effective wall charge moves the net margin from 1.1942 to 1.1785–1.2114, i.e. by at most 1.44 %; finite ion size contributes exactly zero to the far-field gradient by one line of algebra on the published steric Poisson-Boltzmann equation and 1.4 % of the threshold when solved; and the bulk decay channel — the only one a surface correction cannot enter, by Kjellander's theorem — is EMPTY, because this device sits at `dκ_D = 0.109–0.487`, inside the window where four independent methods agree the primitive model's decay length IS the Debye length, with the Kirkwood crossover at 64 mM. Taking `C-0005`'s own broken expansion literally, in the only reading with a defensible sign for two oppositely charged walls, lands the margin at 1.044 and not below one.**

---

## `P1` — the level channel, and the premise nobody had checked

### Force-pinning, measured rather than assumed

`C-0017` reads its stability floor at the bias where `W(3 nm) = 100 pN`, so the balance fixes `|F_es| = 100 pN + P(g)A`. Checked over its own 54 records: the pinned force is **identical across 0.5, 1 and 2 mM at every `(model, height)` pair**, to a worst relative spread of **`0.0`** over the 18 groups. **Falsifier `F1` did not fire; all 54 states are force-pinned.**

That is what makes `C-0005`'s number the wrong error bar. `k_es = −|F_es|/ℓ` with `|F_es|` fixed, so a multiplier on the level does not enter `k_es` as a multiplier at all.

### But it is not *exactly* zero, and that is `CH-0166`

`CH-0035` states the absorption as **exact**: *"It is not small; it is exactly zero at the operating point."* The identity it is read from contains a premise that is not in it — that `ℓ` does not depend on the bias — and `ℓ` depends on the bias strongly, because the gap is counterion-dominated and the counterion content is set by the bias.

Solved at the binding state (10 nm layer, 2 mM, held at 7 nm), with the bias re-solved so the *pinned* force is delivered:

| level `μ₀` | required bias | `ℓ` | floor | margin |
|---|---|---|---|---|
| `1/3.14 = 0.318` | **unreachable at any bias to 8 V** | — | — | — |
| `1/2.23 = 0.448` | 0.7407 V | 2.3227 nm | 39.587 | **0.8420 — FAILS** |
| **1.000** | 0.1568 V | **2.8621 nm** | **27.909** | **1.1943** |
| `2.23` | 0.0784 V | 3.2264 nm | 22.232 | 1.4993 |
| `3.14` | 0.0592 V | 3.2922 nm | 21.340 | 1.5620 |

`ℓ` runs **2.2593 → 3.3158 nm** over the ladder, and the residual reaches **12.585 pN/nm**, `2.88×` the gradient threshold. The slope is **`−3.61` to `−8.70 pN/nm` per e-fold of level**, six models, and it is **one-signed**: the floor is monotone decreasing in `μ₀`, so **every enhancement of the force raises the margin**. **Falsifier `F2` fired**, and it is `CH-0166`.

The null multiplier leaks `3.83e−3 pN/nm` — which is this study's reproduction of `C-0017`'s floor from its own force balance, and the scale at which everything above should be read.

---

## `P2` — the two thresholds

### The gradient threshold, exact and free

`floor(g) = |F| (1/ℓ − g) − k_brush`, so `g* = (floor(0) − mandate)/|F|` — one division per state, no solve.

| state (2 mM, 10 nm layer, held at 7 nm) | `\|F\|` | `ℓ` | floor | margin | **`g*`** | **`ℓ` required** |
|---|---|---|---|---|---|---|
| **alexander-box(two-body)** | **143.922** | **2.8619** | **27.913** | **1.1942** | **−0.03766 nm⁻¹** | **2.5834 nm (−9.73 %)** |
| alexander-box(des-Cloizeaux) | 165.073 | 2.7682 | 24.904 | 1.3385 | −0.05106 nm⁻¹ | 2.4253 nm (−12.38 %) |
| strong-stretching(two-body) | 115.958 | 2.9932 | 27.039 | 1.2328 | −0.05428 nm⁻¹ | 2.5749 nm (−13.98 %) |
| alexander-box(virial) | 163.738 | 2.7739 | 23.414 | 1.4236 | −0.06058 nm⁻¹ | 2.3748 nm (−14.39 %) |
| strong-stretching(des-Cloizeaux) | 127.013 | 2.9407 | 23.953 | 1.3916 | −0.07386 nm⁻¹ | 2.4159 nm (−17.84 %) |
| strong-stretching(virial) | 124.448 | 2.9528 | 23.804 | 1.4003 | −0.07657 nm⁻¹ | 2.4083 nm (−18.44 %) |

Over the **18** states that have a floor at all, `g*` runs **−0.0377 to −0.1800 nm⁻¹**. The **36** states at 5 and 7 nm have `k_eff > 0` and no floor, and their thresholds are **−0.1983 to −1.0530 nm⁻¹** — an order of magnitude further away. **The binding state is the shallowest of the eighteen and every number below is quoted against it.**

### The level threshold

By bisection on `ln μ₀`, with the bias re-solved at every step:

> **The margin reaches 1.0 at `μ = 0.4506 – 0.6753` over the six models — the true force would have to be `1.48×` to `2.22×` SMALLER than the mean-field one.**

**Both thresholds are on the SUPPRESSION side.** That is not an accident of arithmetic: the floor is monotone decreasing in the level and `−|F|g` is linear in the gradient, so an enhancement of the force and a slower decay both help.

---

## `P3` — the ceiling, channel by channel, and NET

A correction can reach the gradient through exactly two channels — the **boundary condition** (how any correlation-renormalised wall charge, image charge, charge regulation or specific adsorption acts) and the **constitutive relation** (how finite ion size and ion-ion correlation act). Both were solved.

### The net movement, which is what a design feels

Level and gradient carried **together** onto the binding state:

| member | level `μ(7 nm)` | `g` [nm⁻¹] | floor | **margin** |
|---|---|---|---|---|
| effective wall charge × 0.25 | 0.7194 | +0.03242 | 27.695 | **1.2036** |
| effective wall charge × 0.5 | 0.9028 | +0.00607 | 28.285 | **1.1785** |
| **mean field (× 1.0)** | **1.0000** | **0.00000** | **27.909** | **1.1943** |
| effective wall charge × 2.0 | 1.0508 | −0.00209 | 27.651 | **1.2055** |
| effective wall charge × 4.0 | 1.0770 | −0.00300 | 27.517 | **1.2114** |
| finite ion size, hydrated `Mg²⁺` (0.428 nm) | 1.0145 | +0.00043 | 27.682 | **1.2041** |
| finite ion size, hydrated `Cl⁻` (0.332 nm) | 1.0067 | +0.00020 | 27.804 | **1.1989** |

> **A factor of SIXTEEN swept in effective wall charge moves the margin by at most 1.44 %.** The two channels largely cancel — at a quarter of the wall charge the level term is `+4.448 pN/nm` and the gradient term `−4.666 pN/nm` — and six of the seven measured members **improve** the margin.

`C-0005`'s own saturation finding is why the level term is so small: at a fixed applied bias, halving the tile's effective charge costs 9.7 % of the force and quartering it 28.1 %, against the 32.5–55 % the level threshold demands.

### The bulk channel is EMPTY, and the arithmetic that says otherwise is a trap

Härtel & Kjellander (dressed-ion theory; **READ DIRECTLY**, `gpd/data/T-50-beyond-mean-field-literature.md` row 1):

> *"Surface forces between two macroscopic bodies decay for large separations with the same decay length as in the bulk phase in contact with the surfaces, but the amplitude and, for oscillatory forces, the phase depend on the properties of the bodies."*

So **no surface convention this project carries can enter `g` at all** at large `κh` — not the Manning fraction, not saturation, not the rim charge, not the Stern layer, not image charges, not finite ion size at the wall. And the bulk decay length of this electrolyte **is** the Debye length: Cats, Evans, Härtel & van Roij measure it by MSA integral-equation theory, two classical DFTs and molecular dynamics and find `ξ_Z ≈ κ_D⁻¹` for `dκ_D < 0.5` (**READ DIRECTLY**). This device sits at `dκ_D = 0.2180` at 2 mM and **0.109–0.487 over 0.5–10 mM** at the hydrated `Mg²⁺` diameter (0.089–0.488 over the 0.70–0.856 nm diameter bracket); the Kirkwood crossover to oscillatory decay would need **63.6 mM**.

**The trap, recorded because it is exactly the size of the answer.** The Waisman-Lebowitz/Blum MSA screening parameter has the closed form `2Γ = [√(1 + 2dκ_D) − 1]/d`, which gives a decay length **5.2 / 7.2 / 9.9 / 20.3 % longer** than Debye at 0.5 / 1 / 2 / 10 mM — `0.0293 nm⁻¹` at 2 mM, **0.778 of the threshold**, favourable. The paper that supplies the closed form says in as many words that `1/2Γ` *"is merely an intermediate parameter of the theory and should not be regarded as a physical screening length"*. **It is the number that would have been quoted and would have been wrong.**

### Finite ion size contributes exactly zero to the far field, by algebra

The Borukhov-Andelman-Orland steric Poisson-Boltzmann equation has `sinh²(zeψ/2kT) = O(ψ²)` in its denominator, so the **linearised** operator is `∇²ψ = κ²ψ` with the **unmodified** `κ`, at every packing fraction. Bikerman's up-to-56 % enhancement of `|F_es|` (`CLAUDE.md`) is entirely an **amplitude**. The solved near-field residue at the operating bias is `+0.00043 nm⁻¹`, **1.4 % of the threshold**, favourable — and it rises to `−0.0185 nm⁻¹` (0.49 of the threshold, adverse) only at **2 V**, ten times the operating bias and ten times past `C-0005`'s own point-ion boundary.

### The rigorous favourable bound

`g < 1/ℓ_PB = 0.3494 nm⁻¹`, **9.28×** the threshold, needing no model at all: at `g = 1/ℓ` the true force is gap-independent, which is two oppositely charged plates with **no mobile ions between them**. Nothing screens less than nothing.

### The pessimistic corner, and it does not cross

`C-0005`'s own `Ξ|P⁽¹⁾|/P_PB` is reproduced here from the same closed forms to **0.14–0.47 %** at all five of its gaps. Transferred into this geometry — which it does not describe — in the only reading with a defensible sign for two **oppositely charged** walls, `μ = 1 + ratio = 2.634` at the 7 nm gap:

| | level | `g` | floor | margin |
|---|---|---|---|---|
| mean field | 1.0000 | 0 | 27.913 | 1.1942 |
| **`μ = 1 + Ξ\|P⁽¹⁾\|/P_PB`** | **2.6340** | **−0.07095** (1.88× threshold, adverse) | **31.935** | **1.0438** |

The level buys `−6.189 pN/nm` where the gradient costs `+10.211`, and the sum is `+4.022`. **It does not cross one.** This is the largest correction anything in this corpus can be made to produce, and it is built from an expansion that has broken down.

---

## `P4` — does the `Mg²⁺`-does-not-condense bound transfer? **No.** And what replaces it is stronger

`CLAUDE.md` records the empirical fact that `Mg²⁺` does not condense duplex DNA at any concentration as *"the ONLY bound this project has on strong-coupling correlation attraction"*. It **does not transfer to the actuated gap**, and the reason is structural rather than quantitative:

> **That bound is about the SIGN of the force between LIKE charges** — it says a correlation attraction never exceeds the mean-field repulsion it is subtracted from. **Between oppositely charged walls there is no sign to reverse**: the mean-field force is already an attraction, and a correlation term adds to it rather than cancelling it. The bound has nothing to bound.

What replaces it is better than a null result, and it is published. Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik give the weak-coupling validity criterion as `Ξ < D̃/ln D̃` for a **repulsive** mean-field pressure and, for the **attractive** branch — which is only possible for oppositely charged surfaces — as a bound whose *"right hand side here is exponentially large"*, concluding verbatim (**READ DIRECTLY**):

> *"…for charged surfaces of opposite sign, the weak-coupling analysis performs far better at finite coupling parameters and smaller inter-surface separations than for the surfaces of equal sign."*

and backing it with Monte Carlo at `Ξ = 0.32, 8.6, 86`:

> *"In the case of ζ = −0.5 the difference between the strong and weak coupling results for the rescaled interaction pressure is marginal and the simulation data and the analytical results nearly coincide for all rescaled separations."*

**`C-0005`'s `Ξ = 17–24` alarm is calibrated on the like-charged problem, and this device is not that problem.** This closes `C-0005`'s own still-open item 4, *"no published `Ξ` criterion exists for oppositely charged walls"* — one exists, and it says the actuated configuration is the easy case.

**One thing it does NOT settle, and it must be said.** Evaluated here, the *repulsive* criterion gives a bound of **14.43** against `Ξ = 24.00` at the **bare duplex** wall (**FAILS**) and **2.80** against `Ξ = 1.455` at the **charge-saturated gap-facing** wall (**PASSES**). The two readings are `16.5×` apart in `Ξ` and land on opposite sides of one inequality. `CLAUDE.md` says to read `Ξ` from the duplex cylinder — *for the local coupling* — while the criterion is written for a **planar wall bounding the gap**. Which wall it is owed at is queued, not answered.

---

## `P5` — the verdict

> **`C-0017`'s 10 nm / 2 mM verdict stays NOT EXCLUDED and this claim does not establish it.** What it does is name the two numbers a primitive-model Monte Carlo would have to return to change the answer — **a force `1.48×` smaller than mean field, or a decay length `9.73 %` shorter, at a 7 nm gap in 2 mM `MgCl₂`** — and show that every channel that can be evaluated is empty, favourable, or worth `1.44 %` of the margin. **`CH-0019`'s "last unbounded exposure" is bounded.**
>
> **The one-line reason: `C-0005`'s 123–214 % is a LEVEL, and a stability margin is not a level.**

The recommendation `C-0017` already makes — operate at leaf `A2.2`'s 0.5 mM, where the margin is 2.09–8.65× — is **untouched and now over-determined**: at 0.5 mM the gradient threshold is `−0.1477 nm⁻¹`, `3.9×` further away than at 2 mM, and the bulk channel there is `0.0063 nm⁻¹`. But **`T-63`'s answer stands**: 2 mM is the nominal and 0.5 mM is a costed option, and this claim removes one of the reasons the option looked necessary.

---

## `P6` — the level is free in the STIFFNESS and it is not free in the BIAS

A level correction is absorbed into the bias, and the bias has ceilings. Of the 30 level-corrected states:

- **7 are UNREACHABLE at any bias up to 8 V.** The electrode charge is Stern-limited, so `|F_es|` **saturates** — at **365.1 pN** at the 7 nm held gap, against **352.7 pN** already at §3's own 2 V. A large *suppression* of the force is refused by the bias budget outright, not merely made expensive.
- Of the 23 reached, **5 leave `C-0005`'s 0.197 V point-ion boundary**, 1 leaves `C-0017`'s 1 V trusted ceiling and 1 leaves §3's 2 V.
- The required bias moves **0.367× to 50.06×** over the ladder — the top of that range being the near-saturation case.

**So the suppression direction is refused twice over: no mechanism in the family produces it, and the bias budget could not deliver the pinned force if one did.** **Falsifier `F6` fired**, and this is what it found.

---

## Verification gates

1. **Dimensional** — `g` in `nm⁻¹`, `|F| g` in `pN/nm`, `Ξ` and `dκ_D` dimensionless, association volume in `nm³`. Asserted.
2. **Limiting cases** — `g = 0` leaves the floor exactly unmoved; the floor is exactly linear in `g` while positive; `decayLengthUnderGradient` is singular exactly at `g = 1/ℓ`; the MSA parameter reduces to `κ` at zero ion diameter (and does so **stably** — the textbook `(√(1+2κσ)−1)/σ` loses every digit to cancellation there and is rationalised in the implementation); Bjerrum association vanishes exactly when the contact distance reaches the critical separation; the unassociated ionic strength is exactly `3c`.
3. **Symmetry and conservation** — the pinned force is buffer-independent at every `(model, height)` (relative spread `0.0`); the mass-action law is asserted by substitution at `1e−8`; the MSA closed form is asserted against its own defining relation `2Γ(1+Γσ) = κ`.
4. **Numerical convergence** — the finite-size **gradient** checked separately from the multiplier it differentiates, on the same solves: `μ_B` departs `2.9e−8` over 1000/2000/4000 mesh nodes and `d ln μ_B/dh` **`4.0e−7`**, 14× worse, which is `CLAUDE.md`'s *"a gradient converges more slowly than the quantity it differences"* holding again; the difference-step spread over `0.25 / 0.5 / 1.0 nm` is `1.024×`. The `Ei` series is converged to `1e−14` in its own term count. **`F4` did not fire.**
5. **Literature cross-check** — `C-0005`'s five one-loop deviations reproduced to **0.14–0.47 %**; its 2 mM Debye length to `3.1e−5`; its `Ξ = 24.0` to `1.0e−4`; `C-0017`'s binding floor to `3.5e−6`; and **four of `T-3a`'s own size-modified force ratios reproduced to `8e−6 – 2.2e−5`** at the gaps and biases that file reports. Twelve reproductions, all recorded. The literature file behind `P3`/`P4` carries **38 query strings** and a read flag per number.

---

## Declared falsifiers, and whether they fired

| | statement | fired | what it found |
|---|---|---|---|
| **`F1`** | the binding states are not force-pinned | **no** | the pinned force is identical across all three buffers at every `(model, height)`, spread `0.0` |
| **`F2`** | the level channel's residual leak is not zero | **YES** | `−3.61` to `−8.70 pN/nm` per e-fold, up to `12.585 pN/nm` over `C-0005`'s band. **`CH-0166`** |
| **`F3`** | a measured member's `\|g\|` exceeds the threshold | **no** | boundary `0.883×`, finite size `0.014×`, bulk `0.778×` at its own buffer; net margin `1.1785–1.2114` |
| **`F4`** | the finite-size gradient is not converged | **no** | `4.0e−7` in the mesh, `1.024×` over a 4× difference step |
| **`F5`** | the operating gap is not in the far field | **YES**, as predicted | `ℓ/λ_D = 0.7288`. The decay-length theorem is a **structure** argument here; the ceiling rests on the solved channels |
| **`F6`** | a level correction pushes the bias past a ceiling | **YES** | 7 of 30 unreachable at 8 V; 5 of 23 past the point-ion boundary |

---

## Validity range

- **TRL 1–3, nothing measured.** The threshold is exact **given** `C-0017`'s mean-field force balance; if that balance moves, the threshold moves with it.
- **The level/gradient split is a property of a FORCE-PINNED operating point.** It does not hold at a fixed-bias one, where the level reaches the answer in full — `C-0018`'s **free** load line is exactly that case (`CH-0035`'s own item 2), and nothing here changes it.
- **`F5` fired and is not repaired.** `ℓ/λ_D = 0.7288` at the binding state, so the decay-length theorem's `h → ∞` premise is not satisfied at the operating gap. It is used as a **structure** argument — that the gradient is a bulk quantity — and the quantitative ceiling comes from the solved channels, which are solved at the actual gap.
- **The boundary sweep varies ONE wall.** The electrode's effective charge is set by the Stern series at the applied bias and is not swept independently.
- **Bikerman is a STERIC correction, not a correlation one.** It is a member of the family and it is not a proxy for the correlation term.
- **The Bjerrum association arithmetic** is a distance-cut-off mass-action model with no activity coefficients. It bounds a direction and an order of magnitude, not a measured association constant — and it is subsumed by the limiting-law window in any case.
- **`Ξ = 17–24` at the duplex surface remains without a systematic theory** (`C-0005`). Nothing here computes the correlation correction; what is computed is what it would have to do to matter.
- **Which wall the Kanduč criterion is owed at is NOT settled** (§`P4`), and the two readings straddle it.
- **The decay-length theorem is quoted from its author's own 2024 published restatement, not from the 1992/1994 originals.** Kjellander & Mitchell's primary dressed-ion papers and Kjellander's 2019 *Soft Matter* review were **NOT FOUND** — AIP and RSC refuse both `curl` and WebFetch — so `P3` rests on Härtel & Kjellander (arXiv:2412.01653, published *Faraday Discuss.* **253**), read directly, corroborated independently by Cats et al.'s Eq. (49) and by their operational check that the DFT decay lengths *"were independent of the surface potential"*.
- **No published value of `q^eff(κ)/q` for aqueous `MgCl₂` at 0.5–10 mM was found**, and by the dressed-ion criterion `(κ/κ_DH)² = q^eff(κ)/q` that single scalar decides the **sign** of any deviation exactly and uniquely. The limiting-law window is what stands in for it here.
- **Kanduč et al. is counterion-only**, *"neglecting completely the effects of salt"*, so it has no Debye length and says nothing about `g` directly. It answers only whether mean field is more reliable for oppositely charged walls. It is.

## Numbers that are cited rather than derived

| number | value | flag |
|---|---|---|
| `C-0005`'s one-loop ratio at 5/7/10/15/20 nm | 2.14 / 1.63 / 1.23 / 0.89 / 0.70 | **CITED and REPRODUCED here to 0.14–0.47 %** |
| `C-0005`'s point-ion boundary at a positive electrode, 2 mM | 0.197 V | **CITED** |
| `C-0005`'s saturated far-field charge, 2 mM | 0.0568 e/nm² | **CITED**, and re-derived here as `κ/(π l_B q)` |
| Nightingale hydrated radii `Mg²⁺` / `Cl⁻` | 0.428 / 0.332 nm | **CITED** through `C-0005` |
| four `T-3a` size-modified force ratios | 1.1233 / 1.1092 / 1.3173 / 1.2589 | **CITED as literals and REPRODUCED to 8e−6 – 2.2e−5** |
| Cats et al.'s limiting-law window and Kirkwood point | `dκ_D < 0.5`, `x_K = 1.229` | **CITED, READ DIRECTLY** |
| Kanduč et al.'s Eq. (64) and its MC couplings | `Ξ < D̃/ln D̃`; `Ξ = 0.32, 8.6, 86` | **CITED, READ DIRECTLY**, evaluated here |
| the MSA closed form and the dressed-ion `K(h)`, `C` | — | **CITED formulas, DERIVED evaluations**, both with limits asserted as gates |

Everything else — the thresholds, the level ladder, the two channels, the net member effects, the bulk arithmetic, `b = q/(2(q+1))`, the `Ei` series — is derived here.

## Challenges

Two are raised **by** this claim: [`CH-0166`](../challenges/CH-0166-a-level-correction-is-not-exactly-absorbed-into-the-bias.md) and [`CH-0167`](../challenges/CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md). None stands against it.
