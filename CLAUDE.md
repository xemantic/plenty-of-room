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

### Brush mechanics

- The layer stiffness is **not a well-posed single number at the resting height**: the de Gennes scaling form has finite stiffness at first contact, the Milner-Witten-Cates SCF form has exactly zero (its pressure vanishes quadratically at `L0`, because the brush's outer edge is diffuse). Always quote a stiffness at a stated compression.
- `k/A = 3 k_BT sigma^(3/2) / L0` is **not** a 3/2 law in the grafting density — `L0` carries `sigma^(1/3)` too, so the equilibrium stiffness goes as `sigma^(7/6)`.
- Mapping the de Gennes two-brush pressure onto a brush against a rigid wall is `D -> 2h`, and the factor of two then **cancels out of both ratios**. Keeping it while reinterpreting `D` as the wall distance understates the pressure by `2^(9/4)` — this is the prefactor confusion the NDI problem definition warns about.

## Environment

- The agent does **not** run as root, but as the `claude` user with passwordless sudo.
  Missing tooling is therefore installable — `sudo apt-get install -y poppler-utils` for the `pdftotext` the research practice above relies on, for instance —
  so do not report a tool as unavailable without trying to install it first.

## Known gotchas

- viktor rejects empty arrays already on construction — `DoubleArray(0).asF64Array()` throws `IllegalArgumentException: empty arrays not supported` — so an empty `F64Array` cannot exist, and guarding a function against one is dead code.
- `L0 = N a^(5/3) sigma^(1/3)` evaluated in floating point does not land on a round number even when the inputs are chosen so it should — `0.125.pow(1.0/3.0)` is `0.5000000000000001`. Do not assert an exact equilibrium height, and do not embed one in a `require` message that a test then matches literally; interpolate the computed value instead.
- After upgrading the Gradle wrapper, `test` may fail with `NoSuchFileException: build/test-results/test/binary/in-progress-results-generic.bin`, because the results of the previous Gradle version are stale — delete `build/test-results` (or run `clean`) and retry.

## Research practice

- **Do not take a numeric result from a search-engine summary or from memory.** Download the paper and read the passage. A summary of Hansen et al. (2003) reported the des Cloizeaux onset as `φ# ≈ 0.04 / 0.025`; the paper says `0.15` and `0.07–0.09` — the summary had picked up the *overlap* concentrations instead. Acting on the summary would have inverted the conclusion of a whole iteration. `pdftotext -layout` on the arXiv PDF is the cheap, reliable route.
- Prefer **published measurement on the actual material** over simulating it. For `P-3` the "serious" method (MD/SCF for PEG's excluded volume) would have been *less* trustworthy than existing osmometry over the same concentration range, not merely more expensive. Say so explicitly in the Plan section — that is the cost justification NDI asks for.

## Anti-patterns to avoid

- Do not add content to this file that is already discoverable by reading the source or build scripts — that inflates context without adding signal, reducing AI agent task success rates (see [arxiv 2602.11988](https://arxiv.org/abs/2602.11988)).
