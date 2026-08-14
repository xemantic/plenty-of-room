# CH-0043 — `C-0019`'s result file is reproducible to nine significant digits and determinate to six, and the certificate was written on the wrong one

| | |
|---|---|
| **Against** | [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md) — its **provenance line**, not any of its numbers |
| **Raised by** | [`C-0031`](../claims/C-0031-bracketed-root-repair.md) / [`P-15`](../tasks/P-15-bracketed-root-repair.md) |
| **Grounds** | **methodological.** A byte-for-byte diff certifies that nothing perturbed the iteration, which is a property of the *path*. It was reported as a property of the *answer*, and the two differ here by three significant digits |
| **Status** | **UPHELD, and no number of `C-0019` fails.** Every verdict, every window edge and every quoted figure stands. What fails is the sentence about what the diff established |

---

## What the claim says

`C-0019`'s provenance line:

> *"…the result file re-run on an independent snapshot and diffed **byte-for-byte identical**"*

That is true, it was checked, and it is the discipline this project adopted after `S-166` found a
`Double` result tree that differed on a re-run which changed nothing. The whole tree is rounded at
the serialisation boundary — to **nine significant digits** — precisely so that a re-run diff means
something.

## The challenge

**Nine significant digits is three digits below what the study's own height solve determines.**

`SelfConsistentFieldLayer.heightAtPressure` brackets on a logarithm with
`HEIGHT_TOLERANCE = 1e-6` — a *relative* tolerance, declared in the source, entirely correct for
what it does, and inherited by everything downstream of a solved height. So the emitted numbers are
determinate to about `1e-6` and are printed to `1e-9`.

The consequence is that the byte-for-byte certificate was passing **because nothing had perturbed
the iteration**, not because the answer was pinned to the precision being printed. Repairing
`bracketedRoot` (`P-15`) perturbs the iteration — a correct Illinois takes a different and shorter
path to the same root — and the file promptly moves:

| | |
|---|---|
| numeric fields changed | **589** |
| relative change, min / median / max | `8.4e−9` / **`9.0e−7`** / `4.2e−3` |
| string (findings prose) fields changed | **1** |
| verdicts changed | **0** |
| structural changes | **0** |

**The median movement is `9.0e−7`. `HEIGHT_TOLERANCE` is `1e−6`.** The file moved by exactly its own
declared tolerance, which is the signature of a quantity relocating inside its noise floor rather
than a quantity changing.

The largest movements are the smallest numbers, as they must be: the four biggest are
`convergence[*]/departureFromFinest`, a grid-convergence diagnostic whose own value is `7.7e−6` to
`8.8e−4`, moving by `8.4e−4` to `4.2e−3` relative. The largest movement in anything `C-0019`
**quotes** is `8.5e−5` relative — `0.0085 %` — in a window edge quoted to three significant figures.

The one prose change is the same effect surfacing in a rounded percentage:

> *"the coil-overlap edge therefore moves only 0.92 % and **0.34 %**… while the STROKE edge moves
> 12.32 % and **1.42 %**"* → **0.33 %** and **1.43 %**

## What this challenge does and does not do

**Does not touch a single verdict.** `Gi = 1.302` at the design point, `Gi ∈ [0.30, 1.71]` across the
window, `k_brush` −9.4 % at 10 nm and −5.1 % at 7 nm, the windows widening 13.4 % and 1.8 %, the
measured `d ln k/d ln K = 0.0647` against `C-0003`'s `0.3077` — all stand, at every digit `C-0019`
quotes them to. The `0.0647481 → 0.0647484` movement is in the seventh figure of a number quoted to
three.

**Does replace the certificate.** "Byte-for-byte identical on an independent snapshot" should be
read as *the run is deterministic*, which is worth having and is what `S-166` asked for. It is
**not** evidence that the digits printed are digits determined, and `C-0019` — and by inheritance
`C-0011`, `C-0016` and `C-0027`, which round the same way — should not be read as asserting the
eighth and ninth figures of anything that passes through a solved height.

## The general lesson, and it is a new one for this project

> **Round a result file to the precision the answer is determined to, not to a fixed digit count.**
> Rounding below the solve's own tolerance makes the file *reproducible without being determinate*:
> the diff then measures whether the code path changed, so any solver improvement is indistinguishable
> from a physics change and has to be re-adjudicated by hand — which is this challenge.

It is the sixth appearance in this repository of one shape — a quantity that is not well posed
without the state it is read at — and the first where the missing qualifier is a **number of digits**
rather than a compression, a bandwidth, a loading rate, a gap, a load case or a load line.

## Recommendation

1. **`C-0019`'s numbers stand as published.** No re-derivation is required and none is offered.
2. **The `T-1f` result file is replaced by the repaired-solver output**, because the repaired solver
   is the correct one. The movement is recorded here rather than hidden by keeping the old file.
3. **Queued as `P-18`**: carry the serialisation rounding down to the determined precision, or carry
   `HEIGHT_TOLERANCE` up to the printed one. The second is the honest direction and costs compute;
   the first is free and costs nothing but the appearance of precision. Neither is done here, because
   both change every result file in the repository and that is an iteration of its own.
