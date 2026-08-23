# CH-0083 — §6 task 4's pull-in verdict is quoted for **two load lines the programme no longer recommends**: `C-0018` computed it for the **affine** mandate and `C-0032` for `C-0030`'s **strain-softening** flexure, and the element `C-0071` recommends is **neither** — so the one place a fold has never been searched is the design the programme now stands behind

| | |
|---|---|
| **Raised by** | [`C-0071`](../claims/C-0071-output-element-recommendation.md) (`T-135`) |
| **Against** | [`C-0018`](../claims/C-0018-maximum-usable-bias.md)'s and [`C-0032`](../claims/C-0032-softening-coupling-stability.md)'s **coverage** — not against any number either of them reports. Specifically against the reading, carried in `ANSWERS.md`'s §6 table and in `C-0071`'s own §6 row, that §6 task 4 is discharged **for the Gen-1 device** |
| **Grounds** | **`C-0032` is itself the proof that a fold does not transfer between load lines.** It re-ran `C-0018`'s 162 fold searches with one substitution — the affine `R = 33.333 s` replaced by `C-0030`'s nonlinear law — and **lost 7 of 54 states**, moved the pull-in bias by 0.7–1.8 %, and walked the fold's own stroke from 3.41–4.13 nm back to **2.80–3.17 nm, across §3's 3 nm target at two of six models**. A third law is therefore a third answer, and `C-0071`'s recommended element is a third law: it is **strain-stiffening** at the placement stroke, where `C-0030`'s is softening, and its tangent minimum over the traversed range is **30.03 pN/nm** where `C-0030`'s is 22.88 |
| **Severity** | **a gap in coverage, not an error.** Every number in `C-0018` and `C-0032` stands; `C-0032`'s two readings bracket the question but do not close it, because one is the affine idealisation and the other is a *softening* element and the recommended one is *stiffening*. What falls is only the sentence *"§6 task 4 is answered for the Gen-1 device"* — it is answered for two devices, and the recommended one is not among them |
| **Status** | **RESOLVED** by [`C-0084`](../claims/C-0084-recommended-element-pull-in-fold.md) (`T-149`), which searches the third load line — the one [`C-0071`](../claims/C-0071-output-element-recommendation.md) recommends — and finds **no fold at 2 mM at 6 of 6 layer models**, where the affine mandate folds at 6 of 6. §6 task 4 is discharged **for the recommended device**, `C-0071`'s failure route `R7` does not fire, and `C-0032`'s escalation of 0.5 mM from a preference to a **requirement** does not transfer — a materially different answer to `DECISIONS-FOR-NDI` decision 1. **Every number in `C-0018` and `C-0032` still stands**; what this challenge withdrew was a sentence about coverage, and the coverage is now supplied |

---

## What is claimed upstream

`C-0018` finds a maximum usable bias at 162 states and a pull-in fold binding at **11 of 54**, all of them 10 nm in 2 mM, at a bias margin of **1.007–1.032** — *"the thinnest anywhere in the programme"*.

`C-0032` substitutes `C-0030`'s realised law and reports:

> *"the operating bias `V*` is **unchanged to the last bit** (placement), while the pull-in bias falls 0.7–1.8 % and — the number that decides it — **the fold's own stroke walks from 3.41–4.13 nm back to 2.80–3.17 nm, crossing §3's 3 nm target at two of six models.** The bias margin is **1.0000–1.0019**: the device sits *on* its fold."*

and, for the **stiffening** reading of the same design:

> *"the strain-**stiffening** decoupled element (`t/s` = 1.095) loses **0 of 54** states against the affine mandate and *raises* the 10 nm / 2 mM margin from 1.007–1.032 to 1.020–1.774."*

**Nothing here disputes any of that.**

## What the composition finds

`C-0071` recommends `C-0069`'s `Q5` — a **hinge-rooted arm**, 34 paths, root one antiparallel crossover, tip `C-0034`'s `A2`. Its law is neither of the two `C-0032` ran:

| | `C-0018`'s affine mandate | `C-0032`'s `C-0030` flexure | **`C-0071`'s recommended arm** |
|---|---|---|---|
| law | `R = 33.333 s`, exactly linear | strain-**softening**, `t/s < 1` | strain-**stiffening**, `t/s = 1.224` at 3 nm |
| assembled tangent at 3 nm | 33.333 | — | **40.812** — read from `C-0069`'s own result file, `gpd/results/T-133-output-element-placement.json`, `candidates` row `Q5`, field `assembledTangentAtStroke`, and **not** recomputed here |
| tangent minimum over the traversed `[0, 3]` | 33.333 | **22.88** | **30.03** |
| `C-0017`'s six 2 mM floors cleared | 6 of 6 | **0 of 6** | **6 of 6** |
| paths | 45 | 45 | **34** |
| **pull-in fold searched?** | **yes**, 162 states | **yes**, 216 states | **NO — never** |

**Two statements, and the second is the challenge.**

1. **The recommended element clears the *static* stability floors at 2 mM where `C-0030`'s does not** — 30.03 against a 23.41–27.91 band, 6 of 6 — so `C-0032`'s escalation of 0.5 mM from a preference to a **requirement** does not transfer to it, and `T-63`'s standing changes.
2. **But a held-gap stability margin is not a fold margin**, and `CLAUDE.md` records exactly that: *"a stability margin read at a HELD gap is not the same quantity as a fold margin on a MOVING equilibrium — the held gap's `|k_eff|` rises as `V^1.9–2.8` while `C-0018`'s fold implies `V^11–25`."* So clearing six floors says **nothing** about where this element's fold sits, and no upstream claim has looked.

> **The direction is favourable and the magnitude is unknown.** `C-0032` measured a *stiffening* element raising the margin from 1.007–1.032 to 1.020–1.774 — but that element had `t/s = 1.095` and 45 paths, and the recommended one has `t/s = 1.224` and 34. A direction is not a bound, and the quantity being bounded is a **bias margin of 1.0000–1.0019 in the neighbouring case**, which is as thin as anything this programme has quoted.

## What this does NOT challenge

- **`C-0018`'s 162 fold searches stand entirely**, as does its finding that the unloaded tile has no pull-in at 49 of 54 states.
- **`C-0032`'s verdict stands**, including its upholding of `CH-0042` on its first horn and its finding that the escape fails at 0 of 8 standoff lengths.
- **`C-0017`'s theorem stands**, and the recommended element satisfies its premise (strain-stiffening) where `C-0030`'s does not.
- **`C-0071`'s recommendation stands.** This challenge is `C-0071`'s own open item 1, filed formally so that §6 task 4's verdict is not read as covering a device it does not cover.

## What would settle it

1. **Re-run `C-0018`'s `EquilibriumPath` with the recommended element's own law as the load line**, at the 10 nm layer in 2 mM over `C-0003`'s six models — the smallest useful run is 6 fold searches, against `C-0032`'s 216. The element's reaction is a shooting solve on `C-0039`'s elastica, so the cost per evaluation is far above `C-0030`'s analytic law; a tabulated-and-interpolated reaction with a convergence record is the obvious cheap route, and its falsifier is that the interpolation moves the fold's stroke by more than the 0.2 nm `C-0032` found decisive.
2. **A bound rather than a solve.** If `k_c` is monotone in the element's tangent at every stroke — which `C-0032` observed but did not prove — the recommended element's fold is bracketed **between** `C-0018`'s affine margin (1.007–1.032) and `C-0032`'s stiffening reading (1.020–1.774), because its tangent minimum, 30.03, lies between 33.333 and `C-0030`'s stiffening reading. That is a cheap bound and it needs the monotonicity established, not assumed.
3. **0.5 mM answered** (`T-63`). At 0.5 mM `C-0032` finds the fold **does not exist at all** and the bias margin is 1.038–2.327, so a specification answer discharges the question without any solve — which is why `C-0071` keeps `T-63` on its binding list even though the element clears every static floor at 2 mM.

## Status

**OPEN.** Filed with `C-0071` (`T-135`). The composition is in `gpd/results/T-135-output-element-recommendation.json` (`failureRoutes` row `R7`, `conditionals` row `T-63`, `sectionSix` row *"4 — electrostatic softening and pull-in"*, `openItems` row 1).
