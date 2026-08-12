# CLAUDE.md

This file captures only what cannot be inferred from the codebase itself.

## Rules for editing this file

Both developers and AI agents are expected to add entries as they encounter surprises.

- **Add an entry** when you encounter something unexpected: a build quirk, a non-obvious constraint, a dependency gotcha, or any behavior that would surprise the next agent or developer.
- **Add an entry** when a developer flags an anti-pattern produced by AI — describe the anti-pattern and the preferred alternative.
- **Do not** add codebase overviews, directory listings, or anything discoverable by reading the source.
- Keep entries concise: one line per lesson, grouped under a heading if a theme emerges.

## Conventions

### Markdown authoring

Markdown files use [semantic line breaks](https://sembr.org/):
break a line after a sentence,
and optionally at clause boundaries within a long sentence,
so that diffs stay meaningful and reviewable.

There is no column width limit —
never reflow or hard-wrap a paragraph to fit some character count.
Modern editors soft-wrap Markdown visually,
see the [README](README.md#markdown-soft-wrapping-in-the-ide) for how to enable it.

### Vector arithmetics

Use [viktor](https://github.com/JetBrains-Research/viktor) for vector arithmetics —
its `F64Array` is SIMD-accelerated and avoids the hand-rolled `DoubleArray` loops an AI agent tends to produce.
For small fixed-size geometry — `Vector2`/`Vector3`/`Vector4`, matrices, quaternions —
[openrndr-math](https://github.com/openrndr/openrndr/tree/master/openrndr-math) can be used instead.

viktor ships JNI natives and is JVM-only,
which is one of the reasons this project is a plain Kotlin/JVM application rather than a multiplatform one.

### PEG material parameters

- **`a` is three different quantities in the brush literature and they are not interchangeable.** The Alexander-de Gennes *effective monomer length* (0.35 nm, a **contour** length), the *volumetric* monomer size `v₀^(1/3)` (0.392 nm), and the *Kuhn length* `b` (1.1 nm). Their cubes differ by a factor of 39. Use `PegWater`'s three named properties; never write `a` for a volume.
- A **volume fraction** in this project always means the physical one, `N σ v₀ / h`. Sources that write `φ = n a³` are quoting a *reduced density*, 1.408× smaller for PEG — and a fitted prefactor always travels with the `φ` convention it was fitted under.
- **The binding crossover for a grafted PEG layer is the dilute→semidilute one, approached from below**, at `φ# = (αN)^(−4/5) ≈ 0.026` — not the semidilute→concentrated one at `φ ≈ 0.2–0.3`. Checking distance from the upper boundary says nothing about the lower one; `C-0001` made exactly this mistake.
- **Coil overlap (`Σ ≥ 5`, or `φ > φ* = N^(−4/5)`) is not a sufficient criterion for semidilute behaviour**, and this is documented in print for PEG specifically. For PEG in water `Σ = 5` is exactly equivalent to `φ = 1.085 φ#`, independent of layer height — i.e. the middle of the crossover, not the semidilute regime.

- **`χ` lives on a lattice, and the lattice site is not always the monomer.** The measured PEG/water `χ` sits on a **water-molecule** site, so `v = v₀·(v₀/v_water)·(1 − 2χ)` — **2.010×** the familiar `v₀(1 − 2χ)`. Invisible by inspection; caught only by cross-check against `B₂`, where the right convention agrees to 16 % and the naive one misses by 42 %.
- **`χ ≈ 0.45` for PEG/water has no primary source** — the 0.44 in circulation is *polystyrene in toluene*, quoted for contrast in the very paper that measures PEG at **0.372** (300 K). Use `χ(T) = 1.156 − 235.3/T`, and read `dχ/dT` at the **cloud point**, not at 300 K: they differ by 51 %.
- **PEG in water is only *weakly* good, so order-of-magnitude reasoning about `χ` does not transfer to `v`.** `1 − 2χ = 0.257`, so `Δχ = 1e-3` — unmeasurable by any method — is already 0.74 % of the excluded volume, and one kelvin of cloud-point depression costs 1.35 %.
- **"The theta temperature of PEG in water" is a 16 K band, not 375 K** — 358.7 K (Flory-Huggins), 369 ± 3 K (cloud points), 373.2 K (virial). So `τ` at 300 K is 0.164–0.200, and `C-0002`'s 0.200 is the optimistic end.
- **A bulk `χ` is not a brush `χ`.** A densely grafted PEO layer is reported at `χ ≈ 0.60` — above θ, negative excluded volume — against 0.372 in bulk. Every osmotic number in this project comes from a bulk-solution property; that gap is 200× larger than any salt effect at Gen-1 concentrations.

### Brush mechanics

- The layer stiffness is **not a well-posed single number at the resting height**: the de Gennes scaling form has finite stiffness at first contact, the Milner-Witten-Cates SCF form has exactly zero (its pressure vanishes quadratically at `L0`, because the brush's outer edge is diffuse). Always quote a stiffness at a stated compression.
- `k/A = 3 k_BT sigma^(3/2) / L0` is **not** a 3/2 law in the grafting density — `L0` carries `sigma^(1/3)` too, so the equilibrium stiffness goes as `sigma^(7/6)`.
- Mapping the de Gennes two-brush pressure onto a brush against a rigid wall is `D -> 2h`, and the factor of two then **cancels out of both ratios**. Keeping it while reinterpreting `D` as the wall distance understates the pressure by `2^(9/4)` — this is the prefactor confusion the NDI problem definition warns about.
- **Ideal mobile salt exerts exactly no osmotic pressure on a grafted layer.** Ions excluded from the polymer contribute `f = k_BT n_s phi`, strictly linear in `phi`, and `Pi = phi f' - f` annihilates a linear term; equivalently the layer's polymer volume per unit area is conserved under compression, so the energy is height-independent. At 10 mM MgCl₂ that cancels a term **3.5× the layer's own osmotic pressure** — not small, exactly zero. Everything a buffer does to a *neutral* layer's mechanics is therefore a `χ`, never an ion count.
- **The blob-stack height is `L0/s = (Sigma/pi)^(5/6)`, identically** — 1.47 blobs at the conventional `Sigma = 5` onset, for every polymer, chain length and thickness; a ten-blob stack needs `Sigma ~ 50`. So `Sigma >= 5` no more delivers a stack of blobs than it delivers semidilute thermodynamics, and the two failures (`CH-0003`, `CH-0001`) are inverse powers of the same `Sigma`.

### Electrostatics conventions

- **"The Debye length" is three different numbers in this project, and all three are correct in their own place** — 3.93 nm in the bulk buffer at 2 mM MgCl₂; 0.84–1.18 nm in the tile-electrode gap, which is counterion-dominated 3–33× so its ion content is set by the tile's charge and not by the buffer; and 4.5–5.5 nm inside the PEG layer, which excludes 23–48 % of the salt. Substituting one for another is `CH-0004`.
- **MgCl₂ is a 2:1 electrolyte: `I = ½ Σ c_i z_i² = 3c`, not `c`.** A monovalent intuition understates the screening threefold and the Debye length by `√3`.
- **The Manning parameter has two conventions in circulation** — Manning's own `ξ_M = l_B/b` is valency-free with condensation at `q ξ_M > 1`, while Naji et al. fold the valency in as `ξ = q l_B τ`. That is why the same DNA is quoted as both 4.1 and 8.2. State which you mean. The lateral counterion spacing has the same problem: `a_⊥ = sqrt(q/σ_s)` against the Wigner-Seitz `sqrt(q/(π σ_s))`, a factor of `√π` apart, and only the latter makes `Γ = sqrt(Ξ/2)` exact.
- **`Ξ ∝ q³` and `μ_GC ∝ 1/q`.** Divalent Mg²⁺ at a DNA surface is a *different problem* from monovalent Na⁺ at the same surface, not a rescaling of it — `Ξ` goes from 3.0 to 24.0, and the mean-field error at a 7 nm gap from 36 % to 163 %.
- **Read `Ξ` from the duplex cylinder charge density, not the projected one.** The projected density gives a PB contact density 89× past close packing; the high projected values describe the far field, which is where PB works anyway.

### DNA-origami structural parameters

- **The crossover spacing is 32 bp, not 16 bp, for a single-layer Rothemund sheet.** Crossovers recur every 1.5 turns (16 bp) along a helix but *alternate between its two neighbours*, so a given adjacent pair is linked every 32 bp; honeycomb is 21 bp per interface. Using the per-helix number where the per-interface one belongs doubles the across-helix flexural rigidity.
- `10.67 bp/turn` is the **square** lattice and `10.5 bp/turn` the **honeycomb**, not the other way round.
- **Interhelical distance is measured, not designed** — 2.69 nm single-layer, 2.73 square, 2.54 honeycomb (SAXS, Fischer et al. 2016). The ~3.0 nm that circulates is Rothemund's *inferred* 1 nm gap; CanDo's 2.25 nm is an assumed helix diameter. Do not substitute one for another.
- **CanDo treats crossovers as rigid, and says so.** Fine for a multilayer bundle, where that degree of freedom is geometrically frustrated; wrong for a single-layer sheet, where it is the *only* across-helix compliance. Its `EI = 230 pN·nm²` is a model input, not a measurement — it implies `L_p = 55.5 nm` against ~40–47 nm measured.
- **A DNA rupture force without a loading rate is not a material constant** — the same origami class moves from ~42 pN at 5.5 pN/s to ~75 pN at 1.8e5 pN/s. The problem definition's 35–60 pN band is a *whole-cross-section* number, not a per-load-path allowable; per path use duplex shear (~48–65 pN) or unzip (10–15 pN), with 65 pN a hard ceiling because every origami helix is nicked.

### Poroelastic transport

- **The layer's hydrodynamic screening length is not known to better than a factor of 6.** Segment-scale models give `sqrt(k) ~ 0.9 nm`, the measurement-anchored `k = xi^2` gives 5.6 nm, and at `phi/phi# ~ 1` the blob is two thirds of the coil — so these describe the same object. Bracket it and quote from the slow end; never a single number.
- **Squeeze-out under a tile is a footprint problem, not a thickness problem.** `tau = eta G / (k M f)`: the thickness cancels except through the Brinkman wall correction, and `tau ~ L^2` in the tile edge. Lateral and vertical drainage cross over at `L = 3.4 h`, so at 40 x 40 nm on a 10 nm layer neither path is obviously dominant.
- **Use the Brinkman transmissivity `T = k h [1 - (2 sqrt(k)/h) tanh(h/2 sqrt(k))]`, never plain Darcy `k h`.** It contains the free-film Reynolds squeeze-film limit a layer with `sqrt(k) ~ h` degrades to; plain Darcy overstates drainage by 5x there. Poroelastic drainage and lubrication squeeze film are the same expression at two ends — they are **not** additive channels.

## Environment

- The agent does **not** run as root, but as the `claude` user with passwordless sudo.
  Missing tooling is therefore installable — `sudo apt-get install -y poppler-utils` for the `pdftotext` the research practice above relies on, for instance —
  so do not report a tool as unavailable without trying to install it first.
- The box ships **no compiler toolchain at all** — `g++`, `make`, `cmake`, numpy and scipy were all absent and had to be installed. Anything that builds from source (oxDNA, for leaf `A1.2`) needs them first.
- **When more than one agent works this checkout, pass `-PbuildDirectory=<dir>` to every Gradle command.** Concurrent runs otherwise race on `build/test-results` and fail with `EOFException` or `NoSuchFileException` on the in-progress results binary — a harness failure that reads exactly like a broken test. `build-*/` is git-ignored. Under heavy load a run can still die with `Test process encountered an unexpected problem`; that is contention, so retry rather than "fixing" a test.

## Known gotchas

- viktor rejects empty arrays already on construction — `DoubleArray(0).asF64Array()` throws `IllegalArgumentException: empty arrays not supported` — so an empty `F64Array` cannot exist, and guarding a function against one is dead code.
- `L0 = N a^(5/3) sigma^(1/3)` evaluated in floating point does not land on a round number even when the inputs are chosen so it should — `0.125.pow(1.0/3.0)` is `0.5000000000000001`. Do not assert an exact equilibrium height, and do not embed one in a `require` message that a test then matches literally; interpolate the computed value instead.
- **A uniform load on a uniform Winkler foundation produces no dishing at all** — a free plate translates exactly, whatever its flexural rigidity, because `w = q/k_f` has zero fourth derivative and satisfies the free-edge conditions identically. If a plate-on-foundation solve reports dishing under a uniform load, the solver is wrong, not the physics. It makes an excellent falsifier; wire it in as a test.
- **The plate-on-foundation ripple transfer function `1/(1+(2 pi l/lambda)^4)` does not apply at a free edge** — it attenuated a rim perturbation 50× more than the finite-plate solve, because a free edge has no material beyond it to bend against. Interior non-uniformity only.
- **`Double` results are not reproducible across runs of the same JVM.** The JIT compiles a hot reduction part-way through a run, changing summation order and moving the last one or two ulp, so `gpd/results/*.json` differs on a re-run that changed nothing. Round the whole tree at the *serialisation boundary*, not at each construction site, so nothing can be emitted unrounded by omission.
- Comparing two quantities that are both meant to be zero **relatively** compares their noise — compare absolutely. (An odd Gauss-Legendre rule has a node at the origin, which is where this bites.)
- `1 - tanh(x)/x` loses every significant digit to cancellation below `x ~ 1e-2`, which is exactly the free-film limit of the Brinkman transmissivity — use the series `x^2/3 - 2x^4/15 + 17x^6/315` there.
- After upgrading the Gradle wrapper, `test` may fail with `NoSuchFileException: build/test-results/test/binary/in-progress-results-generic.bin`, because the results of the previous Gradle version are stale — delete `build/test-results` (or run `clean`) and retry.

## Research practice

- **Do not take a numeric result from a search-engine summary or from memory.** Download the paper and read the passage. A summary of Hansen et al. (2003) reported the des Cloizeaux onset as `φ# ≈ 0.04 / 0.025`; the paper says `0.15` and `0.07–0.09` — the summary had picked up the *overlap* concentrations instead. Acting on the summary would have inverted the conclusion of a whole iteration. `pdftotext -layout` on the arXiv PDF is the cheap, reliable route.
- **arXiv identifiers recalled from memory are unreliable — search for them, never guess.** All four recalled for the strong-coupling electrostatics literature resolved to unrelated articles; one was a paper on electricity-price risk management. Query `https://export.arxiv.org/api/query?...` with `curl -sL` (the `http` endpoint 301-redirects and returns an empty body without `-L`), then read the PDF with `pdftotext -layout`.
- **Publisher sites are mostly closed; Crossref and EuropePMC are not.** ACS and Elsevier answer `403`, Springer redirects to an identity provider. `https://api.crossref.org/works/<doi>` returns a **verbatim abstract** for many older papers, and EuropePMC's REST search does the same for anything PubMed-indexed. An abstract read verbatim from Crossref is a citation and may be quoted as one; a search-engine summary of the same paper is not, and must be labelled differently.
- **When a coefficient cannot be sourced, deliver a ceiling and a threshold instead of a value.** The largest effect any member of the family reaches, plus the value the unknown would need for the answer to change, together decide the question without the missing number — and either half is falsified by a single published measurement. `P-6` closed this way after finding that the quantity it was sent for does not exist and is not even well posed.
- **A paywalled primary source is a reason to demote the number, not to quote the secondary.** Flag it unverified *in the code*, use it only as a cross-check against an independently derived bound, and make sure nothing changes if it is wrong. Where a publisher blocks the fetch with a reCAPTCHA, EuropePMC's REST full text often serves the same article: `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC<id>/fullTextXML`.
- Prefer **published measurement on the actual material** over simulating it. For `P-3` the "serious" method (MD/SCF for PEG's excluded volume) would have been *less* trustworthy than existing osmometry over the same concentration range, not merely more expensive. Say so explicitly in the Plan section — that is the cost justification NDI asks for.

## Anti-patterns to avoid

- Do not add content to this file that is already discoverable by reading the source or build scripts — that inflates context without adding signal, reducing AI agent task success rates (see [arxiv 2602.11988](https://arxiv.org/abs/2602.11988)).
