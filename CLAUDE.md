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

### Brush mechanics

- The layer stiffness is **not a well-posed single number at the resting height**: the de Gennes scaling form has finite stiffness at first contact, the Milner-Witten-Cates SCF form has exactly zero (its pressure vanishes quadratically at `L0`, because the brush's outer edge is diffuse). Always quote a stiffness at a stated compression.
- `k/A = 3 k_BT sigma^(3/2) / L0` is **not** a 3/2 law in the grafting density — `L0` carries `sigma^(1/3)` too, so the equilibrium stiffness goes as `sigma^(7/6)`.
- Mapping the de Gennes two-brush pressure onto a brush against a rigid wall is `D -> 2h`, and the factor of two then **cancels out of both ratios**. Keeping it while reinterpreting `D` as the wall distance understates the pressure by `2^(9/4)` — this is the prefactor confusion the NDI problem definition warns about.

## Known gotchas

- viktor rejects empty arrays already on construction — `DoubleArray(0).asF64Array()` throws `IllegalArgumentException: empty arrays not supported` — so an empty `F64Array` cannot exist, and guarding a function against one is dead code.
- `L0 = N a^(5/3) sigma^(1/3)` evaluated in floating point does not land on a round number even when the inputs are chosen so it should — `0.125.pow(1.0/3.0)` is `0.5000000000000001`. Do not assert an exact equilibrium height, and do not embed one in a `require` message that a test then matches literally; interpolate the computed value instead.
- After upgrading the Gradle wrapper, `test` may fail with `NoSuchFileException: build/test-results/test/binary/in-progress-results-generic.bin`, because the results of the previous Gradle version are stale — delete `build/test-results` (or run `clean`) and retry.

## Anti-patterns to avoid

- Do not add content to this file that is already discoverable by reading the source or build scripts — that inflates context without adding signal, reducing AI agent task success rates (see [arxiv 2602.11988](https://arxiv.org/abs/2602.11988)).
