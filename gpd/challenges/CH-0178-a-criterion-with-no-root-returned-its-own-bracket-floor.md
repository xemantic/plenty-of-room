# CH-0178 — A criterion with no root returned its own bracket floor, and `T-6` emitted one as a length

| | |
|---|---|
| **Challenges** | `ChargedSurface.loopExpansionValidityGap` and `ChargedSurface.meanFieldValidityGap`, and the field `gpd/results/T-6-mean-field-screening-validity.json` → `surfaces[2].loopExpansionValidityGap = 0.9240787673730241` — a number that is not a separation |
| **Raised by** | [`C-0143`](../claims/C-0143-planar-coupling-wall.md), task [`T-221`](../tasks/T-221-planar-coupling-wall.md) |
| **Raised** | 2026-08-19, iteration 34 |
| **Status** | **REPAIRED in the same iteration.** Both functions now return `Double?`; `T-6` re-emitted, **exactly one field moved**, `0.9240787673730241` → `null`. No claim in the corpus quotes the withdrawn number. |

---

## The defect

`loopExpansionValidityGap(Ξ, μ)` finds the separation at which Naji Eq. (20) / Kanduč Eq. (64), `D̃/ln D̃ > Ξ`, becomes an equality. It did so by geometric bisection on `[e·1.000001, 1e12]`.

**`D̃/ln D̃` has a global minimum of `e` at `D̃ = e`.** So for `Ξ ≤ e = 2.71828` the criterion is satisfied at **every** separation and there is no root at all. The bisection had no bracket, never straddled, and returned its own **low endpoint** — `e μ` — dressed as a length in nm.

`T-6` emitted one: the hydrated-hard-core wall with `Na⁺`, `Ξ = 2.1006`, `μ = 0.33995 nm`, reported `loopExpansionValidityGap = 0.9240787673730241 nm`. That is `e × 0.33995 = 0.92416` to four figures — **the bracket, not the physics**.

The same shape was latent in `meanFieldValidityGap`, whose `[4μ, 1e6μ]` bracket is not checked to straddle either; no `T-6` surface reaches it, but a weakly coupled wall would.

## How it was found

Not by a falsifier. `T-221` ran `loopExpansionValidityGap` over six candidate walls and emitted, beside each root, the criterion's own log residual at that root. Five came back `0.0`; the saturated gap face came back **`0.87`**. A root at which the residual is not zero is not a root.

That is `CLAUDE.md`'s *"a root-finder handed a target the function never reaches should return `null`, and the `null` is a VERDICT"* — the verdict here being **"this wall is weakly coupled at every separation"**, which is exactly the statement `C-0137` floated and which the function was silently unable to express.

## Why it survived

The reading it fired on is a `q = 1` cross-check row that no claim quotes — `C-0005`'s bands are all `q = 2`, where `Ξ = 16.8–171.7` and every root is real. **A defect that is invisible in the quoted answer is invisible to every check written on the quoted answer**, for the fourth recorded time in this repository.

It also survived a gate that looks like it should have caught it: `ChargedSurfaceTest`'s *"gate 4 should agree with the closed-form Netz loop-expansion criterion to within a fifth"* asserts `|full − closedForm|/closedForm < 0.2` — but only on `dna`, the `q = 2` duplex, where both roots exist.

## The repair

Both functions return `Double?`:

- `loopExpansionValidityGap` returns `null` immediately when `Ξ ≤ e`, which is a **closed-form** rejection needing no evaluation;
- `meanFieldValidityGap` checks that its bracket straddles and returns `null` otherwise;
- `MeanFieldValidityStudy`'s band prose renders the null as *"NONE — the criterion holds at every separation"* rather than as a number.

Four new tests in `PlanarCouplingWallTest` pin it: that the saturated wall has no root; that the withdrawn number **is** `e μ` to `5e−4`; that a wall just above `e` still has a real root whose log residual is `< 1e−9`; and that the full one-loop boundary returns `null` where its deviation never reaches one.

## What it costs

**One field of one result file.** `gpd/results/T-6-mean-field-screening-validity.json` re-emitted: `surfaces[2].loopExpansionValidityGap`, `0.9240787673730241` → `null`, and nothing else — diffed field by field. `tools/result-reader-census.py` finds **no code reader** of `T-6`, and a corpus grep finds no document quoting `0.9241`.

`C-0005`'s own headline — *"band_C_controlled: gap > 12.91 nm (Naji Eq. 20 closed form: 13.52 nm)"* — is the `q = 2` duplex row and is **unmoved**.

## What it does not cost

Nothing in `C-0143` rests on it: that claim's own use of the function is at walls where the root exists, and its threshold is a closed form (`Ξ*(D) = (q²l_B/D)e^{D/(q²l_B)}`) that never bisects at all.
