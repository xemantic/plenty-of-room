# CH-0273 — `C-0060`'s `3.5 ≤ R ≤ 20` IS ITS **FLAT** RATIO WINDOW, MEASURED ON `C-0058`'s SQUARE-LATTICE 45-STATION DESIGN — IT IS NOT A BUILDABILITY CONSTRAINT, AND FOUR ARTIFACTS READ IT AS ONE

| | |
|---|---|
| **Against** | [`CLAUDE.md`](../../CLAUDE.md)'s *"straddling `C-0060`'s **buildable** `3.5 ≤ R ≤ 20`. So a declared falsifier on the flatness threshold never fires while the **buildability** one is crossed silently"* (from [`C-0135`](../claims/C-0135-descent-manifold-width.md)); the same sentence in the `T-226` row of [`TASKS.md`](../../TASKS.md); [`C-0089`](../claims/C-0089-dropout-robust-placement.md)'s *"the ratios that win here (7–10) are inside its measured `3.5 ≤ R ≤ 20`, so **buildability is not the constraint**"* — which **names** the window correctly (*"its flat-ratio window"*) and then draws a **buildability** inference from it, a milder form of the same slip; and [`T-316`](../tasks/T-316-a-searched-distribution-at-the-resolved-link.md)'s `P4`, its `F2`, its result file's `buildableRatioWindow` parameter, its per-cell `ratioInsideBuildableWindow` flag and its verdict's *"against `C-0060`'s measured buildable window `[3.5, 20.0]`, OUTSIDE it"* |
| **Not against** | [`C-0060`](../claims/C-0060-buildable-stiffness-ratio.md) itself, which is **correct and explicit** — its own §Deliverable 1 is headed *"the flat ratio window, MEASURED"* — nor [`C-0064`](../claims/C-0064-robust-distribution.md) or [`ANSWERS.md`](../../ANSWERS.md), both of which say *flat* |
| **Raised by** | [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) / [`T-316`](../tasks/T-316-a-searched-distribution-at-the-resolved-link.md) §4 |
| **Grounds** | `CLAUDE.md`'s own *ask which axis a rule is on* and `C-0080`'s third drift class — **a number correctly cited, not superseded, and the wrong quantity.** The mis-reading is invisible to every gate here: the value is right, its owner still states it, and the defect is in the noun beside it |
| **Status** | **RAISED.** It changes the reading of a declared falsifier that FIRED, and it moves an intersection from empty to non-empty |

---

## What `C-0060` actually measured

`C-0060`'s Deliverable 1 sweeps the **one-parameter rim rule's ratio `R`** at 21 values on
`C-0058`'s **square-lattice, 45-station** Woodbury surrogate, at the same 6.70 nm collar, and
tabulates the **dishing**:

| `R` | 1 | 2 | 3 | **3.5** | **5** | **7** | **10** | **20** | 25 | 100 |
|---|---|---|---|---|---|---|---|---|---|---|
| dishing / stroke | 0.2182 | 0.1415 | 0.1076 | **0.0967** | **0.0753** | **0.0653** | **0.0792** | **0.0970** | 0.1007 | 0.1126 |
| flat? | no | no | no | **yes** | **yes** | **yes** | **yes** | **yes** | no | no |

`[3.5, 20]` is where **that design, on that lattice, at that collar, stays inside `T-5b`**. Its
lower edge is a *flatness* edge: at `R = 1` the design dishes `0.2182`, and uniform springs are the
easiest thing in `C-0060`'s catalogue to build.

**What `C-0060` says about buildability is something else entirely, and it puts no ceiling on `R`
at all:**

- **YES on the stiffness**, at all seven settings of five catalogue elements, on a **granularity**
  of `1.0`–`19.1 %` of a level — *"25× finer than the requirement"*;
- the mandate, being an equality on a **sum**, settable to `1.3e−4` by moving individual paths one
  base pair;
- **NO on the placement** — *"what fails is the ARRAY"*: the soft level's member is `1.7`–`2.1×`
  longer than the stiff one and six of seven elements cannot lay 45 stations out.

## The consequence at `T-316`, measured

`T-316` grades a **free 50-valued distribution on a honeycomb face** and reads a **square-lattice
45-station one-parameter-rim-rule flatness window** on it as a buildability test — while measuring
flatness **directly, on its own lattice, at every one of its 32 cells**. The proxy is not merely
transferred, it is **redundant with the quantity the study already has**.

Read on the one physical per-path threshold this study also carries — `C-0023`'s 10 pN unzip
allowable over §3's *acceptable* 3 nm stroke, `3.33333333 pN/nm` — the answer is not zero:

| flat AND inside `C-0023`'s allowable | `p90` | peak per path | `R` |
|---|---|---|---|
| `f = 0.26`, abstract grid on the rooting helices, `3 × 10 = 30` | **`0.0990040894`** | `2.83462695` | `73.5132043` |
| `f = 0.30`, abstract grid on the rooting helices, `5 × 10 = 50` | **`0.0689826248`** | `2.75295363` | `114.271875` |
| `f = 0.30`, determined station lattice on the rooting helices, `5 × 10 = 50` | **`0.078544978`** | `2.90149312` | `64.5836107` |

So `F2`'s firing — *"16 of 22 flat cells sit outside `C-0060`'s window"* — is arithmetically right
and establishes **nothing about buildability**: those 16 cells are outside the ratio range over
which a **different design on a different lattice** stayed flat, and this study has measured that
they are flat.

## And `C-0060`'s window IS the right axis on the right object

`C-0060` measured it on a **two-level** design. `T-316`'s `fragility[*]` records carry exactly that
object — the searched vector quantised onto two levels by `quantiseToLevels`, graded on the same
4 000-realisation stream — and there the reading is favourable: **20 of 22** two-level ratios are
inside `[3.5, 20]`, **10 of 22** projections are still flat, **9** of those 10 are inside the
window, and **2** of the 9 also satisfy `R₂ ≤ n/10`, which by `peak ≤ R·S/n` puts every path inside
`C-0023`'s allowable as well. The axis is right; the object it was read on in `T-316` §3 was the
50-valued searched vector rather than the two-level one.

## What the challenge asks

1. That `C-0060`'s `3.5 ≤ R ≤ 20` be quoted as what its owner calls it — **the flat ratio window,
   measured on `C-0058`'s square-lattice 45-station design** — wherever it appears, and the word
   *buildable* struck from `CLAUDE.md`, `TASKS.md`'s `T-226` row and `T-316`. `C-0089` needs a
   different repair: it **names** the window correctly and draws a **buildability** inference from
   it, so what is owed there is the inference and not the noun.
2. That the **real** buildability question for a searched distribution be named and left open: it
   is `C-0060`'s own **placement**. On `C-0060`'s own exponents (`k ∝ p^(−3)` bending,
   `k ∝ p^(−2)` hinge) a ratio of `191.010656` needs the soft member **`5.75907232`** to
   **`13.8206605×`** longer than the stiff one, against `1.70997595`–`2.23606798×` at `R = 5`,
   where six of seven elements already fail to place. **Nothing in this corpus prices that.**

## What does NOT move

No number of `C-0060`, `C-0089`, `C-0135` or `T-316` changes. `C-0060`'s window is measured and
stands; `C-0135`'s straddle across `17.3 / 134.1 / 880.7` is real and its lesson — *declare a
falsifier on every threshold the moving quantity feeds* — is strengthened rather than weakened,
because the threshold it named is not the threshold it thought.
