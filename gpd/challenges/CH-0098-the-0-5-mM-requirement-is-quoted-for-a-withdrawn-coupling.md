# CH-0098 — **`DECISIONS-FOR-NDI` decision 1 tells NDI the device *"sits on its own pull-in fold at 2 mM"*, and that sentence is about a coupling the programme no longer recommends** — on the element it *does* recommend there is **no fold at 2 mM at any of six layer models**, so 0.5 mM reverts from a **requirement** to a **preference**, and the strongest of the six routes to it is withdrawn

| | |
|---|---|
| **Raised by** | [`C-0084`](../claims/C-0084-recommended-element-pull-in-fold.md) (`T-149`) |
| **Against** | [`C-0032`](../claims/C-0032-softening-coupling-stability.md)'s **escalation** — *"as of iteration 5 it stopped being an improvement and became a requirement for the surviving coupling"* — as carried into [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) decision 1, into `TASKS.md`'s `T-63` row, and into `C-0071`'s conditional ledger. **Not against any number `C-0032` reports**, every one of which stands |
| **Grounds** | `C-0032`'s escalation was measured on `C-0030`'s strain-**softening** coupled-standoff flexure, which [`CH-0081`](CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md) and [`C-0069`](../claims/C-0069-output-element-placement.md) removed from the output role and which [`C-0071`](../claims/C-0071-output-element-recommendation.md) does not recommend. `C-0084` re-runs `C-0018`'s 54-state fold search with the **recommended** element's own law and finds **0 folds of 6** at the 10 nm layer in 2 mM, a bias margin of **1.3877–2.5764** against `C-0032`'s 1.0000–1.0019, and the binding ceiling changing owner from pull-in to `C-0002`'s `φ = 0.2` and the element model's own branch end |
| **Severity** | **a specification brief that overstates its own urgency**, not an error. The recommendation *"adopt 0.5 mM"* survives; what falls is the claim that 2 mM is **unusable** for the Gen-1 device, and with it the framing that a *no* answer costs 1–3 weeks of primitive-model Monte Carlo. **A deliverable that over-claims is exactly as wrong as one that under-claims** (`C-0067`), and this is the over-claiming direction in the one document written for a reader outside the programme |

---

## What is claimed upstream

`DECISIONS-FOR-NDI.md`, decision 1, *"At a glance"* row:

> | 1 | Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer, instead of §3's 2 mM? | `T-63` | **The device sits on its own pull-in fold at 2 mM**; the alternative is 1–3 weeks of Monte Carlo | **Adopt 0.5 mM** |

and in the body:

> *"**At 2 mM the device is placed on its own fold.** … Re-running `C-0018`'s fold analysis on that law over 216 states, the 10 nm / 2 mM bias margin collapses from 1.007–1.032 to **1.0000–1.0019**, and the fold's stroke walks back from 3.41–4.13 nm to 2.80–3.17 nm — **through §3's own 3 nm target at two of six layer models**."*

> *"Six independent routes recommend it: `C-0012` on the force clause, `C-0016` on the bias window, `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin, and `C-0032` on the realised coupling law."*

**Every number in that passage is correct for the coupling it was measured on.**

## What `C-0084` finds

**10 nm layer, `σ` = 0.024 nm⁻², 2 mM MgCl₂, placed at 100 pN over 3 nm, six `C-0003` models.**

| load line | status in the programme | folds | fold's own stroke | bias margin |
|---|---|---|---|---|
| `C-0018`'s affine mandate | an idealisation, never an element | 6 of 6 | 3.4104–4.1248 nm | 1.0071–1.0317 |
| `C-0030`'s coupled flexure (`C-0032`) | **removed from the output role** by `CH-0081`/`C-0069` | 6 of 6 | 2.80–3.17 nm | **1.0000–1.0019** |
| **`C-0071`'s recommended arm** (`C-0084`) | **the recommendation** | **0 of 6** | **none below 7.9097 nm** | **1.3877–2.5764** |

At **0.5 mM** the recommended element's margin is **1.8706–3.4699**, also with no fold — so 0.5 mM is still **better**, by a factor of 1.35 on the bias axis, and `C-0017`'s static floors are still 3.86–15.94 pN/nm against 23.41–27.91.

**Two statements, and the second is the challenge.**

1. **0.5 mM remains the programme's recommendation.** Five of the six routes are untouched, the margin is larger, and the static floors are 3–6× lower. Nothing here argues for 2 mM.
2. **But the sixth route is withdrawn, and it was the one that made the answer urgent.** *"The device sits on its own fold"* is the only clause in decision 1 that says 2 mM is **unusable** rather than **worse**, and it is a statement about `C-0030`'s element. On the recommended one the device sits at a margin of 1.39–2.58 with the pull-in ceiling not binding at any model.

## What this does NOT challenge

- **`C-0032`'s numbers all stand**, including the 1.0000–1.0019 and the 216-state sweep. `C-0084` reproduces `C-0032`'s affine fold strokes to **1.3e−3**, its own quoted precision.
- **`C-0018`'s 1.007–1.032 stands** and is reproduced to **2.9e−4**.
- **The recommendation to adopt 0.5 mM stands.** This is a challenge to a *justification*, not to a conclusion.
- **`T-50` stays on the queue.** `C-0005`'s 123–214 % one-loop correction is larger than every margin in `C-0084` too, and that is a separate exposure which 0.5 mM does not remove either.

## What would settle it

1. **An edit to `DECISIONS-FOR-NDI.md` decision 1**, replacing the *cost of deferring* cell with what is true of the recommended device: *"the margin at 2 mM is 1.39–2.58 against 1.87–3.47 at 0.5 mM, and `C-0017`'s static floors are 3–6× lower"* — a preference, quantified, with the fold clause moved into the history of the decision rather than its head.
2. **`C-0071`'s `T-63` conditional re-read.** Its stated reason — *"what binds is that its own pull-in fold has never been computed"* — is now discharged; the conditional survives only as a preference.
3. **A count of how many of the six routes are read on withdrawn objects.** `C-0032`'s is; `C-0017`'s and `C-0018`'s are read on the affine mandate, which is not an element either. Only `C-0084` is read on the recommended one. **That census has never been taken and it is one pass over six claims.**

## Status

**OPEN.** Filed with `C-0084` (`T-149`). The evidence is `gpd/results/T-149-recommended-element-fold.json`, `devices` rows *"10 nm layer, 2.0 mM MgCl2, placed at 3 nm"* and *"10 nm layer, 0.5 mM MgCl2, placed at 3 nm"*, and `folds` rows at `layerHeight` 10.0.
