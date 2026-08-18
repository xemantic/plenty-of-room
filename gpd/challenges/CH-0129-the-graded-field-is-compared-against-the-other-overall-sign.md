# CH-0129 — `C-0107`'s *"the 42 interior sites partly cancel the 14 at the ends"* compares its graded field against a **row-end-only idealisation of the opposite overall sign**, and the cancellation it measures, **0.0100**, is one term of a three-factor move whose interior term is **0.0268**

| | |
|---|---|
| **Against** | [`C-0107`](../claims/C-0107-row-end-prestrain-value.md), Deliverable 4 and its *"Two readings, and they disagree about the verdict"* passage |
| **Raised by** | [`C-0112`](../claims/C-0112-interior-crossover-prestrain.md) (`T-190`) |
| **Kind** | **methodological** — two states differing in three factors, differenced as if they differed in one |
| **Status** | **OPEN, and it strengthens the claim it is against.** `C-0107`'s **0.0922622** is reproduced here at **1.1e−10** and its flat verdict stands; what does not stand is the pairing that explains it, and correcting the pairing makes the interior term **2.68×** larger than published |

---

## The ground

`C-0107` Deliverable 4 sets its two readings side by side:

> *"Read as `C-0104` reads it — a prestrain on the **14 row-end sites alone** — the published
> placement **loses `T-5b`'s 0.10 in both signs** at the nominal derived value … Read as the
> **field the boundary layer actually is** … it **keeps** the verdict at **0.0922622**, because the
> 42 interior sites partly cancel the 14 at the ends."*

and states the difference in as many words: *"The difference between the two readings is the 42
interior crossovers."*

**It is not.** The two states `C-0107` differences apart differ in **three** things:

| factor | the row-end-only state | the graded state |
|---|---|---|
| the **overall sign** of the corrugation | `+22.6185°` on every row-end site | `−22.5398°` on every row-end site |
| the **station** the row-end angle is read at | the row end, `x = 19.04 nm` | the row-end **column**, `x = 18.99 nm` |
| the **42 interior sites** | absent | present |

The sign is the one that matters, and it is not a convention `T-190` imposes — it is read out of
`C-0107`'s own graded map. `corrugatedPrestrain` assigns `(−1)^b u(x)`, `u` odd, and the lattice's
parity rule (`(parity(c) + b) % 2 == 0`, `OrigamiGrillage`'s own) puts column 0 at
`x = −18.99 nm` with parity 0, so **every even interface's row-end crossover sits at the negative
end and every odd interface's at the positive one**, and the glide factor cancels the end
alternation to leave

&nbsp;&nbsp;&nbsp;&nbsp;`θ₀(row end) = −u(18.99 nm) = −22.5398°`, **at all 14 interfaces**,

measured rather than asserted (`uniformValueOrNull` returns `null` if they disagree; it does not).
That is the sign `C-0107` itself calls **adverse** and reads as **0.1193334**.

`C-0107`'s Deliverable 3 states the opposite — *"the row-end crossover of interface `b` sits at
`x = (−1)^b L/2` … `θ₀(b) = +u_max`"* — and its gate-3 test asserts it on an **assumed** end
assignment (`endX = if (b % 2 == 0) +L/2 else −L/2`) rather than on the lattice the study then
solves. The prose and the solve disagree by exactly one global sign; the solve is the one that
produced 0.0922622.

## What the corrected decomposition says

`T-190` grades the full `2 × 2 × 2` and every cell is a solve on the same host
(`gpd/results/T-190-interior-crossover-prestrain.json`, `factorial`):

| overall sign | row-end station | interior | dishing / stroke | flat? | reproduces |
|---|---|---|---|---|---|
| `+` | row end | absent | **0.1193334** | no | `C-0107`'s `−θ₀` row |
| `+` | row end | present | 0.0923285 | yes | — |
| `+` | column | absent | **0.1190748** | no | — |
| `+` | column | present | **0.0922622** | yes | **`C-0107`'s graded field** |
| `−` | row end | absent | **0.1022820** | no | **`C-0107`'s `+θ₀` row** |
| `−` | row end | present | 0.0911034 | yes | — |
| `−` | column | absent | 0.1020545 | no | — |
| `−` | column | present | 0.0910197 | yes | — |

At a **fixed** overall sign the three factors separate cleanly:

- the **station** term is **0.0002586** of the stroke (`+` sign) — 0.2 %, and it can never move a
  verdict;
- the **interior** term is **0.0268125** at sign `+` and **0.0110348** at sign `−`;
- the **sign** term alone, at fixed station and no interior, is **0.0170514**.

`C-0107`'s published 0.0100 is `0.1022820 − 0.0922622`, i.e. the interior term of one sign minus
the sign term — the two travelling together and cancelling most of each other. **The interior
sites are worth 2.68× what the claim credits them with.**

## What would settle it

1. **Restate the comparison at a fixed overall sign.** `0.1190748 → 0.0922622` is the sentence
   `C-0107` wanted, and it says the same thing more strongly.
2. **Fix the gate.** `EdgeTwistReliefTest`'s *"the corrugated row-end sign composition is UNIFORM"*
   asserts uniformity on an assumed geometry; asserted on `rowEndCrossoverSites` of the layout the
   study solves, it still passes and it pins the sign as well as the uniformity.
3. **Carry both signs**, which is [`CH-0130`](CH-0130-the-overall-sign-of-the-corrugation-is-undetermined.md).

## What this challenge does **not** say

It does not say 0.0922622 is wrong — `T-190` reproduces it at `1.1e−10` on the same host — and it
moves **no verdict**: the graded field is flat at both overall signs (0.0922622 and 0.0910197) and
at 40 of 40 cells of `C-0107`'s own boundary-layer bracket. It says the **explanation** attached to
the number attributes to the interior sites a cancellation that is partly a sign flip, and that the
true interior term is larger than the one published.
