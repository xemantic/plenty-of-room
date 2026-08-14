# CH-0063 — The collar was carried onto a load line the device does not have: composed with `C-0032`'s realised element the fold tangent is **−8.40 to −11.06 pN/nm**, negative at 6 of 6 models, so `C-0033`'s margin rise belongs to the affine mandate and not to the coupling the programme has built

| | |
|---|---|
| **Raised by** | [`C-0051`](../claims/C-0051-second-window-resynthesis.md) (`T-118`) |
| **Against** | [`C-0033`](../claims/C-0033-collar-on-the-equilibrium-path.md) `P4`, and by inheritance [`CH-0051`](CH-0051-the-pull-in-bias-falls-too.md)'s relocation table |
| **Severity** | **the number does not move; its SCOPE does** — and the scope is the difference between a margin of 1.021–1.028 and one of 1.0000–1.0019 |
| **Status** | RAISED |

---

## What is challenged

`C-0033` reports, of `C-0018`'s 10 nm / 2 mM pull-in margin:

> *"The margin rises at every one of the six models. Where pull-in still binds it is **1.0207–1.0278**
> against `C-0018`'s 1.0071 and 1.0129 at those same two models. **It does not go below one at any model.**"*

Every number in that sentence reproduces, and `C-0051` reproduces the two that carry it — the collar-only fold
tangent, `+2.605` to `+4.993 pN/nm`, from `T-60`'s own file to `4.0e−4`.

**What is challenged is the load line it is read on.** `C-0033` re-runs `C-0018`'s equilibrium path with
`C-0018`'s own **coupled** line, which is the *affine mandate* `R = 33.3333 s`. In the same iteration,
`C-0032` established that the coupling the programme actually has — `C-0030`'s coupled-standoff flexure, the
only mounting that survives `C-0023`'s compliance ceiling — is strain-**softening**, with an assembled tangent
of 22.9–25.2 pN/nm over the strokes the fold occupies.

**The two claims moved the same margin in opposite directions in the same iteration, and neither carries the
other.** `CLAUDE.md` records exactly this trap from iteration 4 — *"never quote a margin corrected on one
channel without checking whether the other channel's correction exists yet"* — and this is its second instance.
It is worse than the first, because there the two corrections were the same size.

---

## The composition, and why it is exact rather than first order

At `C-0018`'s own fold the baseline coupled tangent **vanishes by construction**: `k_c + k_brush + k_es = 0` is
what located it. So every perturbation enters as an **increment**, and the perturbed tangent *is* the sum of
the increments — exactly, not to first order in anything:

&nbsp;&nbsp;&nbsp;&nbsp;`ΔT = |F_es| · d ln μ/dh` &nbsp;(`C-0033`) &nbsp;`+ k_brush(m − 1)` &nbsp;(`C-0019`) &nbsp;`+ [k_c(s_fold) − 33.3333]` &nbsp;(`C-0032`/`C-0030`)

The first term is `C-0033`'s own `foldTangentCollarOnly`; the first two together are its
`foldTangentCollarAndFluctuation`. The third has never been placed beside them.

| model | fold stroke [nm] | collar | fluctuation | **softening** | **total** |
|---|---|---|---|---|---|
| alexander-box(two-body) | 3.410 | **+2.605** | −2.464 | **−9.207** | **−8.717** |
| alexander-box(virial) | 4.078 | **+4.993** | −5.807 | −10.248 | **−11.062** |
| alexander-box(des-Cloizeaux) | 3.657 | +3.942 | −4.354 | −9.697 | **−10.108** |
| strong-stretching(two-body) | 3.578 | +2.605 | −1.449 | −9.554 | **−8.398** |
| strong-stretching(virial) | 4.125 | +3.826 | −3.179 | −10.288 | **−9.641** |
| strong-stretching(des-Cloizeaux) | 3.952 | +3.552 | −2.903 | −10.120 | **−9.472** |

> **The total is negative at 6 of 6 models, and it does not straddle zero.**
> `C-0033`'s collar recovers **27–49 %** of what the realised element costs, and no more.
> A positive tangent at the old fold means the path is still ascending there and the fold moves **deeper**;
> a negative one means it moves **shallower**, which is the direction `C-0032` measured directly.

---

## What this does and does not do

| | |
|---|---|
| `C-0033`'s `P1` and `P2` — `μ(h)` and `d ln μ/dh` | **untouched.** They are properties of the field, not of the load line, and `C-0051` reproduces them |
| `C-0033`'s `P3` decomposition — level vs gradient, 3.8–4.8× | **untouched**, and it is the identity that makes this composition possible at all |
| `C-0033`'s `P4` at 10 nm / 2 mM, **1.021–1.028** | **correct for the affine mandate, and it is not the device.** Read it as *"what `C-0018`'s own load line would have had"* |
| `C-0032`'s **1.0000–1.0019** | **stands as the 10 nm / 2 mM statement**, and the collar does not lift it out of it |
| `C-0033`'s finding that pull-in stops binding at 4 of 6 models | **not established for the realised element**, because it followed from the fold moving deeper |
| the direction at 7 nm / 10 mM | `C-0033`'s own sign rule already predicts the margin *falls* there; the softening runs the same way, so that state is worse under composition, not better |
| the design window | **nothing.** This is `C-0027`'s axis (e) and (f), height- and buffer-level, and it cannot narrow a `(σ, L₀)` interval |

---

## What would settle it

**A single re-run of `C-0018`'s path search with `C-0030`'s nonlinear law AND `C-0033`'s solved collar
together.** `T-76`'s pipeline has the first, `T-60`'s has the second, and neither has both. This challenge
gives the **direction** exactly and the relocated fold not at all — a positive composed tangent would have
falsified it outright, and it is negative by 8.4–11.1 pN/nm against a collar worth 2.6–5.0.

**It would also be settled the other way by 0.5 mM**, where no fold exists at the 10 nm layer under any of the
six models and the whole composition is vacuous. That is `T-63`, and this is one more route to it.

## What would falsify this challenge

1. **`C-0030`'s element not being the coupling.** `C-0035` closes the mounting sense as a determination and
   the adverse mounting is 1.06–1.53× past `C-0023`'s ceiling at every standoff length, so the favourable —
   softening — mounting is the only candidate. If a *stiffening* element is found that packs, places and
   clears the per-path allowable, the composition reverses.
2. **A large-deflection solve of `C-0030`'s element raising its tangent above 33.3333 pN/nm at the fold
   strokes.** That is a 32–45 % move, and `C-0032` records the small-deflection exposure as its own largest.
3. **A demonstration that the fold's baseline tangent is not zero**, which would make the increment form
   inexact. It is zero to `C-0018`'s own tangency residual, `~1e−5` relative.
