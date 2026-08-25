# `T-326` — the cheap bound, retained

[`closed-form-check.py`](closed-form-check.py) is the prototype every number in
[`gpd/tasks/T-326-the-fit-and-the-sample-in-one-reconstruction.md`](../../tasks/T-326-the-fit-and-the-sample-in-one-reconstruction.md) §2 came from,
retained so the derivation stays checkable without a JVM.
[`closed-form-check.out`](closed-form-check.out) is its output at the commit this task was formulated at.

It runs in under a second and needs only `numpy`:

```
python3 gpd/data/T-326-cheap-bound/closed-form-check.py
```

It checks four things, all with **no solve** and all before any Kotlin was written:

| | what | reading |
|---|---|---|
| 1 | the three closed forms of §2 against a direct, **exactly piecewise** integration of both reconstructions, over `m = 3…16`, both face columns, three random `(W, Φ)` fields each | worst scaled departure **below `1e-12`** (the run prints its own value; it is pure ulp noise and moves between seeds, which is why the shipped Kotlin emits a declared threshold and a boolean rather than the number — `C-0221` §7) |
| 2 | the limiting cases — a pure piston and a pure `y` must give exactly zero under both modes | `0.0` to `1.8E-12` |
| 3 | the class's own **6-point Gauss** rule against the exact integral, on the piston gap | **`0.819694`** at every one of 20 readings, i.e. the shipped `areaInnerProduct` under-reports the gap by a constant `1.2200×` |
| 4 | the piston collinearity `(C − A)/(B − A)` of the three conventions | **exactly `6.000000`** at every even `m` at `faceColumn = 0`; field-dependent elsewhere, which is why the streams are seeded per configuration — see below |

[`margin-census.py`](margin-census.py) is the prototype §2c came from, with its predicate stated in its own docstring; [`margin-census.out`](margin-census.out) is its output at the same commit.
Over the eighteen committed result files carrying a `HoneycombDeflection` dishing it finds **1 146** verdict-bearing readings inside `[0.09, 0.11]`, the tightest being **`0.100001020`** at `T-294/cells/92/nominalCorrectedOverStroke`, **`1.02E-5`** relative from `T-5b` — `417×` tighter in stroke units than the `C-0180` margin `CH-0284` prices against — with **2 / 96 / 99 / 126 / 484** inside the successive channel sizes.

**A note on why the integration is piecewise.**
A first draft integrated with a uniform trapezoid grid and reported a worst departure of `1.0E-1`, which reads as a broken derivation and is a broken **quadrature**: the nearest-beam reconstruction is *discontinuous* at every cell boundary, so a smooth rule is first order there.
Integrating each linear piece in closed form takes the same check below `1e-12` — and the same fact, applied to the shipped class, is reading 3.

**A note on the random streams.**
Every field is drawn from a stream seeded by its own `(m, faceColumn, draw)`, not from one shared generator, so this file's output does not depend on how many draws precede it and [`closed-form-check.out`](closed-form-check.out) is reproducible after any edit that does not change a number.
That matters because the `faceColumn = 1` collinearity ratios are **field-dependent by design** — the end beams break the pairing there — so with a shared stream they moved whenever a section was added above them, which reads as a result and is not one.
