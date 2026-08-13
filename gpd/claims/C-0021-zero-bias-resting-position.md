# C-0021 — Where the Gen-1 tile sits at zero bias: nowhere, unless something is added — and the thing that is already there is a trap, not a confinement

| | |
|---|---|
| **Task** | [`T-13`](../tasks/T-13-zero-bias-resting-position.md) |
| **Leaf** | `A1.2` (the 3.0 nm positional bound, read at zero bias), with `A1.1` as its bound table and **`A8.2`** for the coupling that would have to supply the preload |
| **Verification type** | **in-silico** (a one-dimensional zero-bias force balance assembled from six candidate mechanisms, with `C-0003`'s layer and `C-0008`'s field re-run as libraries rather than tabulated, and the positional statistics obtained by **exact Boltzmann quadrature** rather than by equipartition) **+ logical** (a topology argument that fixes every sign before any arithmetic) |
| **Verdict** | **The §3 stack has NO zero-bias resting position — 18 of 18 (model × height) states, and it is undefined rather than large.** Adding what is actually there (van der Waals, the residual field) produces an equilibrium but **not a confinement**: the well is 0.2–5.7 `k_BT` deep and **0 of 54 states confine**. `C-0014`'s eight substrate tethers close it: **`h₀ = 4.62–9.78 nm`, a 30.6–73.4 `k_BT` well, 18 of 18 confining, RMS 0.360–0.501 nm broadband and 0.019–0.041 nm in band, at a cost of 0.07–0.38 nm of stroke.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** No hold-down below has been built and none is proposed as a sequence design. |
| **Provenance** | `gpd/results/T-13-zero-bias-resting-position.json`, produced by `anchoring.ZeroBiasRestingPositionStudyKt`; 21 mechanism records, 24 van der Waals records, 8 zero-bias field records, 6 contact-potential thresholds, **144 solved equilibria**, 126 defining-load records, 8 coupling-preload records, 11 convergence records, 8 upstream reproductions; **31 gate-named tests in `ZeroBiasHoldDownTest`, 83 in `anchoring`, 794 in the suite, 0 failures**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂** (§3's lowest, and `C-0008`'s zero-bias column), `λ_D = 3.93 nm`; 40 × 40 nm tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` layer models |
| **Consumes** | [`C-0010`](C-0010-tile-positional-variance.md) (the zero, the drag, the bandwidth treatment), [`C-0014`](C-0014-lateral-confinement.md) (the tether, the elements, the allowables, the over-stiffening result), [`C-0017`](C-0017-output-coupling-stiffness.md) (`K2`, the mandate, the preload relation), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the layer, as a library), [`C-0011`](C-0011-scf-density-profile.md)/[`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) (that `L₀` is threshold-defined), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md)/[`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md) (the field and the Stern series), [`C-0004`](C-0004-poroelastic-drainage.md) (the drag), [`C-0005`](C-0005-mean-field-screening-validity.md) (the mean-field ceiling) |
| **Raises** | [`CH-0023`](../challenges/CH-0023-placement-preload-sign.md) against `C-0017`, [`CH-0024`](../challenges/CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md) against `C-0012`/`C-0017` |
| **Challenged by** | [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md), on the **currency** of its declared acceptance. See the banner below |

> ⚠️ **The declared acceptance below — `F ≥ k_BT/3 nm = 1.3806 pN`, a FORCE — is challenged by
> [`CH-0027`](../challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md) (2026-08-13),
> raised by [`C-0023`](C-0023-two-sided-coupling.md) (`T-23`), as a property of a ONE-SIDED stack rather than of the problem.**
>
> **No number, table or verdict here moves** — every one of these 144 states is a one-sided stack, and both
> device rows are reproduced by `C-0023` on this claim's own quadrature domain (1.391 against 1.40 `k_BT`,
> 5.371 against 5.37; 18/18 and 0/18 confining). The correction runs the **favourable** way and it removes a part.
>
> A **two-sided** coupling contributes above `L₀` as well as below it, so the potential there is quadratic
> rather than linear and the requirement is a **stiffness**, `k_BT/σ² = 0.4602 pN/nm` — which §3's own mandated
> 33.333 pN/nm exceeds **72.4×, unpreloaded**. `F_req = k_req·σ` identically, i.e. *two-sidedness is worth
> exactly one power of the position bound*, which is this claim's own `holdDownForceScale(σ)/σ` identity read
> as a design statement. `C-0023` finds **three** such elements (a transverse duplex flexure at a 24.61 nm
> span, a crossover-hinge flexure on a 4.11 nm arm, an antagonistic ssDNA pair), and with one fitted in place
> of `K2` the *"device with the tether removed"* row below goes from **1.4–5.4 `k_BT`, 0/18 confining** to
> **959–7582 `k_BT`, 18/18** — so **`C-0014`'s eight substrate tethers are not needed for `T-13`**.
> Read *"a coupling can decide where the tile sits; it cannot be the thing that holds it there"* as
> *"a **one-sided** coupling …"*.

---

## The claim, in one line

**At zero bias the §3 stack does not hold the tile anywhere — the resting position is *undefined*, not merely uncertain, because a non-adsorbing layer exerts no upward force above `L₀` and nothing in the stack pulls down; the two mechanisms that *are* unavoidably present, van der Waals and the residual field, produce an equilibrium whose well is only 0.2–5.7 `k_BT` deep and which the tile therefore escapes; and the element that closes it is the one `C-0014` already found for a different reason — eight ssDNA tethers grounded on the substrate, which turn a 5 `k_BT` trap into a 31–73 `k_BT` confinement for 0.07–0.38 nm of stroke, while the output coupling the programme has committed to supplies *exactly zero* preload because the ssDNA spacer that made it compliant enough for `T-16` cannot carry compression at all.**

---

## The scale everything is judged against, derived rather than assumed

`C-0010` writes the positional requirement as `k ≥ k_BT/σ²`, which is the right currency for a harmonic coordinate. **Above `L₀` the coordinate is not harmonic.** The layer contributes nothing at all there, so a hold-down of magnitude `F` confines the tile through a **linear** potential and the upward excursion is exponentially distributed:

&nbsp;&nbsp;&nbsp;&nbsp;`⟨h − L₀⟩ = k_BT/F` exactly, and `√⟨(h − L₀)²⟩ = √2 k_BT/F`.

| reading | required hold-down | what it is |
|---|---|---|
| **mean excursion (DECLARED ACCEPTANCE)** | **`F ≥ k_BT/3.0 nm = 1.380649 pN`** | leaf `A1.1`'s 3.0 nm, read as a mean |
| RMS excursion | 1.952529 pN | exactly `√2` stricter |

**`holdDownForceScale(σ)/σ = k_BT/σ² = 0.460216 pN/nm` identically** — leaf `A1.1`'s own lateral bound, reproduced from `k_BT` alone to `7.2e−7` and asserted as a gate-5 test. The force requirement and the stiffness requirement are **the same statement one power of the bound apart**, and which one applies is decided by whether the confining potential is linear or quadratic.

---

## The six mechanisms, each with its sign and its magnitude

2 mM MgCl₂, 300 K, 40 × 40 nm, positive = **down**. Each is computed, none asserted.

| | mechanism | 5 nm | 7 nm | 10 nm | × the 1.381 pN scale | verdict |
|---|---|---|---|---|---|---|
| **`M1`** | **entropic tether, grounded on the SUBSTRATE** (`C-0014`'s `S3`, 8 tethers, `b` = 2.10 nm, `L_c` = 51.43 nm = 79 nt) | **4.629 pN** | **6.516** | **9.421** | **3.4 – 6.8×** | **DOWN. The only designable mechanism that clears the bar at every height** |
| **`M2`** | **the committed output coupling `K2`** (`C-0017`, 45 paths to a lever above) | **0.000** | **0.000** | **0.000** | **0** | **EXACTLY ZERO — the topology argument, confirmed rather than assumed** |
| **`M3`** | residual electrostatics at zero **applied** bias, Stern series | 0.404 | 0.296 | 0.078 | 0.06 – 0.29× | DOWN but negligible; **and the bracket around it is 86×** |
| **`M4`** | **van der Waals across the gap** | **3.44 – 28.11** | 0.98 – 9.15 | **0.245 – 3.19** | **0.18 – 20.4×** | **DOWN always, and it cannot be designed away — but it is a *finite* well** |
| **`M5`** | gravity and buoyancy | 2.21 × 10⁻⁸ pN | same | same | **6.3 × 10⁻⁸×** | DOWN, and **7.6 orders of magnitude** below the bar |
| **`M6`** | bridging by the PEG layer | ≤ 610 pN | ≤ 298 | ≤ 159 | ceiling, not a value | the **non-adsorbing premise** is doing all the work — see the threshold below |

`M4`'s range spans the four electrode materials (gold, platinum, rutile titania, alumina) and both readings of §3's *"~10 nm (single-layer honeycomb)"* tile thickness, and both ends of the retardation/screening bracket.

---

## `M2` — the committed coupling does not solve `T-13`, and the reason is the element that closed `T-16`

The topology argument, written into the task file **before** any code ran:

> A taut flexible link pulls its two ends together. A link grounded on the **substrate** pulls the tile *down*; the same link grounded on a lever **above** the tile pulls it *up*. Only a **two-sided** element — one that carries compression as well as tension — can be mounted with a preload of either sign.

`C-0017`'s `K2` path is a 5 nm duplex standoff **in series with a tuned ssDNA spacer carrying 99.6 % of the compliance**. A single strand carries no compression. So `K2` is one-sided, `R(0) = 0` identically, and it supplies no preload — which is exactly what `C-0017` assumed when it took every coupling unpreloaded. Evaluated rather than assumed: `SeriesEntropicCoupling.reaction(0.0)` returns **0.0**.

> **The compliance `T-16` needed is precisely what destroys the two-sidedness `T-13` needs.** Forty-five duplexes in tension are 148× too stiff for the coupling (`C-0017`); the cure was to put an ssDNA spacer in the load path; and that cure removes the only element in the stack that could have been mounted pushing down.

### But the coupling is not *absent* from the zero-bias balance — it dominates it from below

`K2` presents 33 pN/nm the moment the tile leaves `L₀`, and goes slack above it. So it decides **where** the tile sits once something holds it down, and contributes **nothing** to whether anything does:

| | descent `d` [nm] | `k₀` [pN/nm] | well [`k_BT`] | confining |
|---|---|---|---|---|
| all mechanisms, **no coupling fitted** | 0.05 – 2.36 | 8.3 – 189.6 | 23.0 – 108.3 | 18/18 |
| **the device: all mechanisms + `K2`** | **0.07 – 0.38** | **32.5 – 217.9** | **30.6 – 73.4** | **18/18** |
| **the device with the tether removed** | 0.02 – 0.30 | 30.5 – 215.0 | **1.40 – 5.37** | **0/18** |

**A coupling can decide where the tile sits; it cannot be the thing that holds it there.**

### The exact relation between `T-16`'s stiffness and `T-13`'s preload

&nbsp;&nbsp;&nbsp;&nbsp;**`F_down = (k_c − k_c*)·δ*`** — every `pN/nm` above §3's own 33.333 pN/nm mandate is **exactly 3 pN of downward preload**.

| `k_c` [pN/nm] | 33.333 | 34.0 | 35.0 | 36.5 | 39.01 | 70 | 440 |
|---|---|---|---|---|---|---|---|
| downward preload [pN] | **0** | 2.0 | 5.0 | 9.5 | 17.0 | 110 | 1220 |

So `T-16`'s stiffness choice and `T-13`'s hold-down are **one design variable** — a coupling only 4 % above the mandate would supply the whole thermal-scale requirement — and the only thing standing between the programme and that answer is that no two-sided compliant DNA element has been proposed. **Asserted equal to `C-0017`'s own `placementPreload` at five stiffnesses spanning 22×, to the last bit — and its documentation reads that value's sign backwards, which is [`CH-0023`](../challenges/CH-0023-placement-preload-sign.md).**

---

## `M4` — van der Waals is the unavoidable term, it crosses the bar inside §3's own height range, and it is a **trap** rather than a confinement

Built from published constants rather than recalled ones, and every input is flagged:

- **`A_DNA|water|DNA = 4.33 – 5.90 zJ`** — Dryden et al., *Langmuir* **31**:10145 (2015), Lifshitz, cylinder-cylinder, already retarded and `ν = 0`-screened at 5 nm. **The `10⁻²⁰ J` in circulation is not a measurement**: Rau & Parsegian (1992) introduce it as an explicit *overestimate* to prove van der Waals is too weak, and the `2 × 10⁻²⁰ J` of the AFM literature is a **protein** value, substituted knowingly. **No planar `A_DNA` exists at all.**
- **`A_Au|water|Au = 238.6 – 267.9 zJ`**, `A_Pt = 281.7 – 313.2` — Tolias, arXiv:2003.00571. **`A_Al₂O₃ = 36.9 zJ`** (Bell & Dimos 2000; 36.7 independently from Bergström parameters), **`A_TiO₂ = 53.0 – 92.3 zJ`**.
- combined by the **across-water** relation `√(A₁w₁·A₂w₂)`, which Tolias validates to 2 % and proves is always an **overestimate**; the vacuum-form relation is retained only as a **sign diagnostic**.
- slab factor `1 − d³/(d+t)³` = **0.636 at 5 nm, 0.421 at 10 nm** for a 2 nm tile; retardation pressure factor **0.835 at 5 nm, 0.718 at 10 nm**, derived from Tolias's printed energy fit for gold.

| electrode (2 nm tile) | force at 5 nm | at 7 nm | at 10 nm | **well depth at 5 nm** | **at 10 nm** |
|---|---|---|---|---|---|
| gold | **10.4 – 17.2 pN** | 3.0 – 5.2 | 0.74 – 1.42 | **4.82 `k_BT`** | **0.65** |
| platinum | 11.4 – 18.6 | 3.3 – 5.6 | 0.81 – 1.54 | 5.28 | 0.71 |
| rutile titania | 4.3 – 10.1 | 1.2 – 3.1 | 0.31 – 0.84 | 2.02 | 0.27 |
| alumina | **3.4 – 6.4** | 1.0 – 1.9 | **0.25 – 0.53** | 1.60 | 0.21 |

**Three findings, and the third is the one that matters.**

1. **It crosses the bar inside §3's own height range.** At 5 nm van der Waals holds the tile on its own (2.5–20× the thermal scale); at 10 nm it does not (0.18–1.03×). No design decision is involved — this is a `1/h³`.
2. **The sign cannot flip.** Water sits below both bodies, so the vacuum-form sign diagnostic stays positive at every point (minimum 1.13 zJ). The polymer in the gap raises the medium's index and therefore *reduces* the attraction — by **4.7 %** at `φ = 0.09` (Lorentz-Lorenz, cited), far inside every other bracket. **§4(c)'s "the layer changes the field" instinct is right in direction here and negligible in size.**
3. **A `1/h³` force integrates to a BOUNDED potential, so van der Waals does not confine the tile — it traps it.** The well is 0.2–5.7 `k_BT` deep across the whole box, against the 10 `k_BT` this task declares as the confinement threshold. **Zero of 54 van-der-Waals-only states confine**, and any "positional variance" quoted inside such a well is a property of the integration domain, not of the physics. Only a hold-down whose force **rises** as the tile lifts — an entropic tether — confines in the thermodynamic sense.

**The dominant remaining uncertainty is not physics: it is that §1 says *"patterned electrode"* and never says of what.** Metal against oxide is **2.6×**, larger than the DNA bracket (1.17× after the square root), larger than retardation, larger than the polymer.

---

## `M3` — the zero-bias field, and the variable §3 does not specify

`C-0008`'s pipeline re-run at `V = 0`, both readings of the electrode:

| gap | with the 20 µF/cm² compact layer | with an ideal constant-potential electrode |
|---|---|---|
| 3 nm | **+3.944 pN (repulsive)** | −187.7 pN |
| 5 nm | −0.404 | **−34.94** |
| 7 nm | −0.296 | −9.11 |
| 10 nm | −0.078 | −1.571 |

`C-0008`'s own numbers reproduced: **3.94 → 3.9439** and **−0.41 → −0.4037** and **−34.9 → −34.941**, worst departure `1.5e−2` on a figure `C-0008` quotes to two significant digits.

> **The 86× bracket at 5 nm is owned by the STERN LAYER, not by the polymer and not by the buffer.** The grounded electrode acquires 0.0145 e/nm² of induced countercharge, that charge must also charge the compact layer, and the resulting −0.0116 V of diffuse potential is repulsive: the two terms very nearly cancel. `T-6b`'s Stern capacitance, downgraded to *low* after `CH-0007`, is now load-bearing for a **second** question.

### And the real variable is the one nobody has specified

Zero **applied** bias is not zero **charge**. A real electrode sits at its own potential of zero charge against whatever reference the §3 bias is measured from, and §1 and §3 nowhere state it. Rather than guess, the answer is a **threshold**:

| gap | bias delivering the 1.381 pN thermal scale | bias delivering `M1`'s tether-equivalent preload |
|---|---|---|
| 5 nm | **0.89 mV** | 3.8 mV |
| 7 nm | 1.8 mV | 10.1 mV |
| 10 nm | **5.1 mV** | 32.1 mV |

**A contact potential of a few millivolts — below anything a bench would call zero — supplies the entire hold-down.** The zero-bias resting position is therefore set by a quantity the problem definition does not contain, and this is the fourth time in this programme that an electrode convention has decided an answer (`CH-0004`, `CH-0007`, `C-0008`'s charge saturation, and now this).

---

## `M6` — bridging, and the premise that has never been tested

The §3 layer is *non-adsorbing*, and that premise is what makes `C-0010`'s lateral restoring stiffness **exactly** zero. It has never been checked for PEG against a DNA-origami face. Closed as `P-6` closed: a ceiling and a threshold.

| layer | chains under the tile | ceiling at 1 `k_BT` per chain | **`ε` needed for the thermal scale** | **`ε` needed for `C-0014`'s whole tether preload** |
|---|---|---|---|---|
| 5 nm | 147.2 | 610 pN | **0.00226 `k_BT`** | 0.0155 `k_BT` |
| 7 nm | 72.0 | 298 pN | 0.00463 | 0.0316 |
| 10 nm | 38.4 | 159 pN | **0.00868** | **0.0592** |

> **Hundredths of a `k_BT` per chain would supply the entire hold-down.** That is below what any measurement calls zero, so *"PEG does not adsorb to DNA"* cannot be read as *"bridging contributes nothing"*. What is citable: Rau & Parsegian (1992) show, with a membrane preventing direct contact, that *"interhelical distances are unchanged with or without a membrane"* — the cleanest published statement of PEG's non-adsorption to DNA. What is **not** citable: Mg²⁺ bridging between PEG's ether oxygens and DNA's phosphates, which is exactly the mechanism `P-8` has already searched for and failed to find. **The same missing constant that could flip §4(c)'s sign could also supply `T-13`'s answer.**

---

## The net equilibrium, by scenario

144 solved states: 3 heights × 6 `C-0003` models × 8 scenarios, 2 mM.

| scenario | `h₀` [nm] | descent `L₀ − h₀` [nm] | `k₀` [pN/nm] | well [`k_BT`] | **confining** |
|---|---|---|---|---|---|
| **none — the §3 stack as specified** | — | — | — | — | **0/18 — NO EQUILIBRIUM AT ALL** |
| van der Waals only, metal electrode | 4.35 – 9.95 | 0.05 – 0.79 | 2.2 – 182.6 | 0.6 – 5.7 | **0/18** |
| van der Waals only, oxide electrode | 4.67 – 9.98 | 0.02 – 0.42 | 1.3 – 183.9 | 0.2 – 1.7 | **0/18** |
| `C-0014`'s eight substrate tethers only | 4.67 – 9.40 | 0.02 – 2.09 | 8.3 – 187.8 | 83 – 246 | **18/18** |
| all mechanisms, metal electrode | 4.28 – 9.34 | 0.08 – 2.36 | 8.3 – 188.2 | 30.6 – 76.8 | 18/18 |
| all mechanisms, oxide electrode | 4.54 – 9.38 | 0.05 – 2.19 | 8.4 – 189.6 | 71.8 – 95.5 | 18/18 |
| **THE DEVICE: all mechanisms + `K2`** | **4.62 – 9.78** | **0.07 – 0.38** | **32.5 – 217.9** | **30.6 – 73.4** | **18/18** |
| **the device with the tether removed** | 4.70 – 9.98 | 0.02 – 0.30 | 30.5 – 215.0 | **1.40 – 5.37** | **0/18** |

**The two rows that carry the answer are the last two.** The device as the programme has specified it — layer, `K2` coupling, van der Waals, residual field, gravity — has a mechanically stable equilibrium and **no confinement**: a 1.4–5.4 `k_BT` well that the tile leaves. Add `C-0014`'s eight tethers, which the programme already needs for lateral confinement, and the well becomes 31–73 `k_BT`.

### "No equilibrium" means undefined, not large

With `F_down = 0` the net force is **identically zero everywhere above `L₀`** — every height is a neutral equilibrium, the tile has no restoring force in either direction, and the resting position is not a number that is hard to compute but a quantity that does not exist. `C-0010` said this in words; it is now an executable statement asserted as a gate-2 test.

---

## Where the tile sits, and the load that definition rests on

**The zero-bias resting height *is* the force-onset height defined at the total hold-down force.** `L₀` was a convention with a defining load (`C-0011`, `CH-0010`); here the defining load is a *design variable*, and the sensitivity is the deliverable rather than a caveat.

At `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, des Cloizeaux interaction:

| defining load [pN] | 0.1 | 1.0 | **1.381** | 4.6 | **9.4** | 25 | 100 |
|---|---|---|---|---|---|---|---|
| `h₀`, Alexander box [nm] | 9.993 | 9.928 | 9.901 | 9.679 | **9.369** | 8.507 | 6.165 |
| `h₀`, strong stretching [nm] | 9.848 | 9.463 | **9.360** | 8.778 | **8.223** | 7.107 | 4.895 |
| mean excursion `k_BT/F` [nm] | 41.4 | 4.14 | **3.00** | 0.90 | 0.44 | 0.17 | 0.041 |

A **thousand-fold** change in the defining load moves `h₀` by 3.8–5.1 nm; `C-0011`'s hundred-fold moves it 0.6–1.6 nm, which is the same 2.5× sensitivity `CH-0010` reported in `N`, seen in the other coordinate. **The strong-stretching models pay 2–3× more descent for the same preload, because three of six `C-0003` models have exactly zero stiffness at `L₀` and a soft layer is cheap to compress.**

---

## The cost of the hold-down

### In stroke — and the coupling pays most of it

The preload is spent one-for-one along whatever resists descent, and the answer changes by an order of magnitude depending on what that is:

| resisted by | descent [nm] | **delivered stroke** to §3's working point [nm] | shortfall on §3's 3 nm |
|---|---|---|---|
| the layer alone | 0.05 – 2.36 | 0.64 – 2.95 | up to **79 %** |
| **the layer and `K2`'s 33 pN/nm** | **0.07 – 0.38** | **2.62 – 2.93** | **2 – 13 %** |

**The coupling pays for the hold-down out of the one thing it has to spare.** But 2–13 % is not zero, and it is invisible to every claim that measures stroke from `L₀` — which is [`CH-0024`](../challenges/CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md).

### In load-path force

`C-0014`'s design point puts **1.18 pN** on each of eight tethers at 10 nm — 8.5× below the 10 pN unzip allowable even with `C-0009`'s worst-case 7.6× concentration applied (8.9 pN, `C-0014`'s own figure). **`C-0014`'s over-stiffening result binds here too**: the per-anchor force goes as `√(k_BT k)/N`, so a hold-down bought by stiffening the coupling to 39 pN/nm would put 17 pN on 45 paths (0.38 pN each — fine), while one bought with four tethers instead of eight doubles the per-path force for the same preload.

---

## The positional statistics, computed **without** assuming a harmonic well

The zero-bias potential is **harmonic below `h₀` and linear above it**, so equipartition is not a description of it — it is a limiting case it must reproduce. The statistics are obtained by exact Boltzmann quadrature over `Φ(h) = −∫U_net dh`.

At the device operating state (all mechanisms + `K2`, metal electrode), 18 states:

| quantity | value | note |
|---|---|---|
| **broadband RMS** | **0.360 – 0.501 nm** | vs 3.0 nm predicate — **6.0–8.3× margin**, 18/18 pass |
| equipartition RMS `√(k_BT/k₀)` | 0.138 – 0.357 nm | **the harmonic reading understates by up to 2.6×** |
| **in-band RMS (< 1 kHz)** | **0.019 – 0.041 nm** | 73–158× margin |
| variance below 1 kHz | 0.19 – 1.16 % | `C-0010`'s 0.55–3.07 %, at a stiffer state |
| **fraction of time above `L₀`** | **up to 53 %** | where the layer holds the tile with **nothing at all** |
| escape barrier | 30.6 – 73.4 `k_BT` | `e^(−30.6) = 5 × 10⁻¹⁴` |

> **The mean height sits *above* the force-onset height in several states.** The tile spends up to half its time in a region where the only thing acting on it is the hold-down, which is precisely why the requirement is a force and not a stiffness, and precisely why equipartition understates the amplitude here.

---

## The five verification gates

Executed as **31 gate-named tests** in `src/test/kotlin/anchoring/ZeroBiasHoldDownTest.kt`; **83 `anchoring` tests, 794 in the suite, 0 failures**, on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `k_BT/σ` is a force and halving the bound doubles it exactly; **`1 zJ = 1 pN·nm` exactly**, so the whole van der Waals calculation runs with one conversion function and no factors; `A/6πd³` is an energy over a volume and doubling the gap divides by exactly 8; `\|dP/dh\|/P = 3/d` identically for a half-space; the bridging ceiling and its threshold invert each other exactly; a buoyant weight is `Δρ V g` and doubles with the volume | **PASS** |
| **2 — limiting cases** | an infinite slab reduces to the half-space pressure and a slab as thick as the gap keeps exactly 7/8 of it; the vacuum combining relation vanishes when the medium matches either body and **changes sign** when the medium lies between them; a polymer-free medium is inert and a higher-index polymer lowers the attraction; the screening factor is 1 at zero salt and carries `2κd`, not `κd`; the across-water relation is a geometric mean and a 1.36× spread in one factor is `√1.36` in the mean; the retardation factor sits inside its sourced bracket and is clamped rather than extrapolated; **a Boltzmann quadrature over a harmonic well reproduces `√(k_BT/k)`** and over a one-sided linear potential reproduces `k_BT/F`; **a hold-down of zero leaves no equilibrium at all**; a constant hold-down parks the tile exactly where the layer carries it; the resting height is monotone decreasing in the hold-down; the van der Waals well is **finite** and matches its closed form `(AS/12π)[h⁻² − (h+t)⁻²]`; unphysical arguments throw | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the resting-height root exits on the **bracket width** and is **exactly scan-independent** over 64 → 2048 steps (departure `0.00e+00`), landing on the analytic root to `1e−9`; the Boltzmann quadrature converges monotonically in the panel count (`2.2e−5 → 5.4e−6 → 1.3e−6` against 8000 panels); the zero-bias Poisson-Boltzmann force converges in the mesh (`1.1e−2 → 2.5e−3 → 5.1e−4` at 200/400/800 nodes against 1600); **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature cross-check** | `C-0014`'s tether preload **reproduced from its own design rule** (9.42 against 9.37 pN at 10 nm, 4.63 against 4.6 at 5 nm — 0.6 %); leaf `A1.1`'s bound reproduced from `k_BT` alone to `7.2e−7`; **`C-0008`'s entire zero-bias column reproduced by re-running its pipeline** (3.94, −0.41, −34.9 pN to `1.0e−3`, `1.5e−2`, `1.2e−3`); `C-0017`'s mandate reproduced exactly and `K2`'s reaction at zero stroke shown to be exactly zero; the assembled van der Waals pressure reproduced against the sourced bracket (7.2–8.9 kPa at 5 nm, 515–637 Pa at 10 nm); the slab factor reproduced at 0.636 and 0.421; **the zero-frequency term shown to be `(3/4)ζ(3)k_BT`, not the `(3/4)k_BT` truncation, which is 20 % low** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The topology argument as an executable statement.** The *same* chain, at the *same* extension, grounded below gives `+8f(h)` and grounded above gives `0` at zero stroke and `−8f(s)` beyond it. Only the ground point differs, so this asserts a property of the geometry rather than of the element.
2. **The Boltzmann quadrature is invariant to the potential's zero**, over an offset of 500 `pN·nm`, to `1e−12`. An additive constant in a potential is unobservable and no amount of correct arithmetic in one normalisation guarantees it.
3. **The equilibrium stiffness equals the derivative the root is found on**, analytic assembly against central difference, to `1e−9`.
4. **`(k_c − k_c*)δ*` equals `C-0017`'s `placementPreload`** at five stiffnesses spanning 22×, computed by different code paths — **compared absolutely, in pN**, because near the mandate they are a catastrophic cancellation of each other and comparing two quantities that are both meant to be zero *relatively* compares their noise.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a mechanism with the wrong sign in the code | **no** | every sign matches the topology argument written down in advance |
| 2 | `K2` supplying a non-zero preload | **no** | exactly zero, as predicted before the code ran |
| 3 | **no equilibrium with every mechanism switched on** | **no** | but **YES for the stack as specified** — and that is the headline, not a failure |
| 4 | an equilibrium with negative stiffness | **no** | `k₀ > 0` at all 144 solved states; the layer always beats the van der Waals softening |
| 5 | **a hold-down costing more stroke than §3's 3 nm** | **PARTLY, and it is `CH-0024`** | 2–13 % with the coupling fitted, **up to 79 % without one** |
| 6 | the quadrature disagreeing with equipartition in the harmonic limit | **no** | agrees to `1e−6` |
| 7 | **the bridging ceiling landing far above the requirement with no way to bound it** | **YES** | 159–610 pN against a 1.381 pN bar, and the threshold is **0.002–0.009 `k_BT` per chain**. Closed as a ceiling and a threshold, not as a value |

An **eighth** result was not anticipated and is the sharpest thing here: **van der Waals produces an equilibrium that is stable and not confining.** Stability and confinement are different properties, and a `1/h³` force has the first without the second.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** No hold-down has been built and none is proposed as a sequence design.
- **`L₀` is `C-0003`'s force-onset height, at which `P = 0` exactly by construction** — not `C-0011`'s 1 pN threshold. The two conventions differ and the defining-load table above *is* the translation between them.
- **The layer is `C-0003`'s at `C-0001`'s single grafting density per height**, not `C-0011`'s solved SCF profile. `C-0016` reports the solved layer 1.22× outside `C-0003`'s bracket at 5 nm, so every 5 nm number carries that exposure. A solved profile is **stiffer near `L₀`** than the box models, so the direction is toward *less* descent under a given preload — the conservative direction for the stroke cost and the optimistic one for the descent.
- **Mean-field electrostatics, inherited whole.** `C-0005` puts the one-loop correction at 123–214 % across this gap range. The zero-bias force is 0.078–0.404 pN, so **even a 200 % error on it moves no verdict here** — the one place in this programme where that correction is comfortably affordable, and it is affordable because the term is small rather than because the model is good.
- **The van der Waals term is a combining-relation estimate, not a Lifshitz calculation**, and the across-water form is validated only for metal/water/metal while the pair here is low-dielectric/water/metal. Cauchy-Schwarz makes it an **upper bound**. Retardation is sourced for **gold only** and applied across the whole electrode bracket; the DNA constant is already retarded, so the retarded reading retards that half twice and is a **lower** bound. Both ends are reported and the balance uses the lower one.
- **The electrolyte screening of the zero-frequency term is NOT SOURCED.** A search returned an expression with a citation; the citation did not survive checking and it was withdrawn rather than used. The term is carried as a bracket between fully screened and unscreened, worth **10 % (metal) to 25 % (oxide) of the cross constant** — not the 1.5 % a symmetric gold constant would suggest, because the DNA half of the geometric mean is only ~5 zJ. Inside the 2.6× electrode bracket, so it was not chased further.
- **The sheet is ~58 % DNA and 42 % water at the measured 2.69 nm pitch**, and Podgornik & Parsegian show the continuum effective-medium limit is reached only after ~10 layers. A single-layer tile is one layer, so treating it as a homogeneous slab is **not licensed**; the correction runs toward *less* attraction and is not applied.
- **The tile is a rigid plate translating normally.** `C-0006` rejects that under any concentrated load. At zero bias the van der Waals and gravity loads are uniform (where the tile is rigid *exactly*) but the tethers are discrete, and the dishing they cause is `C-0006`'s and is not computed here.
- **No bias, no stroke, no output.** Every number is the `V = 0` state; `T-3`/`T-4` own the rest.
- **The lateral coordinate is untouched.** `C-0014` owns it; nothing here moves its verdict.
- **The drag is `C-0004`'s permeability model on its slowest member**, evaluated at the zero-bias volume fraction — the direction that maximises the in-band variance.
- **The confinement threshold of 10 `k_BT` is a declared convention**, chosen because `e^(−10) = 4.5 × 10⁻⁵` puts the tile outside the well less than one time in twenty thousand. A different threshold moves which states are called confining, and the van der Waals wells (0.2–5.7 `k_BT`) fail any threshold above ~6.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `A_DNA\|water\|DNA` | 4.33 – 5.90 zJ | **CITED, COMPUTED (Lifshitz)**, Dryden et al., *Langmuir* **31**:10145 (2015), read directly. Cylinder-cylinder; **no planar value exists** |
| `A_Au\|water\|Au`, `A_Pt\|water\|Pt`, `A_water(vac)`, the retardation fits | 238.6–267.9 / 281.7–313.2 / 38.9–53.8 zJ | **CITED, COMPUTED**, Tolias arXiv:2003.00571 and arXiv:2202.09159, read directly |
| `A_Al₂O₃\|water\|Al₂O₃`, `A_TiO₂\|water\|TiO₂` | 36.9 zJ; 12.8–22.3 `k_BT` | **CITED**, Bell & Dimos (2000); Prange et al. arXiv:2606.04331. **ITO and HfO₂ do not exist in reachable literature** and are bracketed rather than valued |
| the zero-frequency term | `(3/4)ζ(3)k_BT` = 3.7345 zJ; 0.75 `k_BT` for a low-dielectric body | **CITED**, Tolias; Roth, Neal & Lenhoff, *Biophys. J.* **70**:977 (1996) |
| the PEG medium correction | 4.7 % at `φ = 0.09` | **CITED**, Lorentz-Lorenz at `n_PEG ≈ 1.46`; a Hamaker constant for PEG/PEO **could not be sourced at all** |
| PEG's non-adsorption to DNA | *"interhelical distances are unchanged with or without a membrane"* | **CITED, MEASURED**, Rau & Parsegian, *Biophys. J.* **61**:246 (1992) |
| Stern capacitance | 20 µF/cm² | **CITED** via `C-0008`, order of magnitude |
| the tile's gap-facing charge | Manning-renormalised, half the projected density | **CITED** via `C-0008`, which shows the tile is charge-saturated (7 % over a 3× charge ambiguity) |
| ssDNA Kuhn length, duplex `S` | 2.10 nm; 1100 pN | **CITED, MEASURED**, Chen et al. *PNAS* **109**:799 (2012); Wang et al. *Biophys. J.* **72**:1335 (1997) |
| DNA mass density | 1.7 g/cm³ | **CITED**, standard for B-form duplex |
| the layer models, virials, permeability, drag | — | **CITED** via `C-0002`/`C-0003`/`C-0004`, and **re-run** here rather than tabulated |
| §3/§6 targets | 100 pN, 3 nm, 3.0 nm, 1 kHz, 40 × 40 nm, 5/7/10 nm, 2 mM | **CITED** |

Everything else — every mechanism magnitude and slope, every equilibrium, every stiffness, every Boltzmann moment, every threshold — is **derived here in code**.

## Still open — named, not answered

1. **The electrode material.** It is 2.6× on the one hold-down that cannot be designed away, and §1 does not state it. This is a *specification* gap, not a modelling one.
2. **The electrode's potential of zero charge.** A few millivolts supplies the whole hold-down. A measurement, not a calculation.
3. **Whether PEG bridges a DNA face.** The threshold is 0.002–0.009 `k_BT` per chain, below what any measurement calls zero, and `P-8`'s missing Mg²⁺/PEG coordination constant is the mechanism that would decide it.
4. **A two-sided compliant DNA coupling.** If one exists — an antagonistic spacer pair, a bending hinge rather than a stretched chain — then `T-16`'s stiffness margin and `T-13`'s hold-down become one part instead of two, and the exact relation `F = (k_c − k_c*)δ*` prices it.
5. **The electrolyte screening expression** for the zero-frequency Hamaker term. Worth 10–25 % of the van der Waals force; not chased because the electrode bracket is 2.6×.
6. **The dishing eight discrete tethers cause at zero bias.** `C-0006`'s problem, not solved here.

## Challenges

**Raises [`CH-0023`](../challenges/CH-0023-placement-preload-sign.md)** against `C-0017` — `placementPreload` returns a **downward** preload and its documentation reads the sign as upward. No `C-0017` number moves (the function appears in no study and no result file), and the correction runs the *favourable* way: a stiffer coupling supplies exactly the hold-down `T-13` needs.

**Raises [`CH-0024`](../challenges/CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md)** against `C-0012` and `C-0017` — every stroke in the programme is measured from `L₀`, and the tile is never at `L₀`. With the committed coupling the delivered stroke is **2.62–2.93 nm** against §3's 3 nm; without one it is as little as 0.64 nm.

**None stands against this claim.** The three ways it would fail:

1. **A published `A_DNA` or electrode constant well outside the bracket.** The van der Waals term decides only whether the *unaided* device is a 1.4 or a 5.4 `k_BT` trap — it is a trap either way, and it would have to rise ~4× to confine on its own at 5 nm and ~20× at 10 nm.
2. **A measurement showing PEG adsorbs to a DNA face at more than ~0.01 `k_BT` per chain.** Then bridging supplies the hold-down for free — and `C-0010`'s exact lateral zero, which rests on the same premise, would go with it.
3. **A two-sided compliant coupling.** That would make `M2` non-zero and change the *design* answer without touching any number here.
