# CH-0015 — The 0.02–0.1 V usable bias window is a property of the *unloaded* actuator, and the device §3 specifies is never unloaded

| | |
|---|---|
| **Against** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) — *"the usable bias window is **0.02–0.1 V, not 0–2 V**"*, and the same sentence as it stands in `TASKS.md`'s standing findings |
| **Raised by** | [`C-0016`](../claims/C-0016-design-window.md) (`T-2`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a validity test applied at an operating point the specified device does not occupy, and then quoted as a property of the bias rather than of that point |
| **Direction** | **favourable to `C-0012`'s programme**, which is the direction in which an error survives longest (`CH-0011`'s own words). It does **not** rescue the answer: the same output coupling that moves the operating point is what `T-2` finds closes the window |
| **Status** | raised. **No number in `C-0012` moves, and its central verdict — reachable, but not holdable — is confirmed and sharpened.** What is challenged is the scope of one sentence |

---

## What is challenged

`C-0012` establishes, correctly and with 810 solved operating points behind it:

> And it saturates *at near-contact*. At 2 mM and 1 V the free operating height is **1.11 nm** (5 nm layer),
> **1.01 nm** (7 nm) and **0.98 nm** (10 nm), at volume fractions of **0.33–0.34**.
> Those points are outside three upstream validity ranges at once […]
> **Only 272 of the 810 free operating points in the sweep are inside both boundaries.**

and concludes:

> the *free* operating point leaves three upstream validity ranges at once above ~0.1 V,
> so **the usable bias window is 0.02–0.1 V, not 0–2 V**.

**Every one of those numbers is reproduced here** — the 272 of 810, the near-contact heights, the
`largestModelValidBias` of 0.05–0.10 V at 2 mM — from `C-0012`'s own result file, and none is disputed.

What is challenged is the last clause. *"The usable bias window"* is stated as a property of the **bias**.
It is a property of the **(bias, load)** pair, and the load it was evaluated at is zero.

---

## The methodological ground

The three ranges that close at ~0.1 V are all tests **on the gap and the volume fraction at the operating point**:

| range | test | source |
|---|---|---|
| correlation band | gap `> 1.46 nm` | `C-0005` |
| concentrated crossover | `φ < 0.2` | `C-0002`, per §2's second caveat |
| extrapolation of the local free energy | gap `≳ 2.6 ×` dry thickness | `C-0003` |

All three are violated at 0.98–1.11 nm because **the unloaded tile snaps to near-contact** — which is
`C-0012`'s own central physical finding and is not in question.

But §3 does not specify an unloaded actuator. It specifies one delivering **≥ 100 pN** at **≥ 3 nm** of stroke,
and `C-0012` itself establishes that at 7 and 10 nm that operating point exists **only against an output
coupling supplying its own stiffness**. A tile held by such a coupling is at `h = L₀ − 3 nm` — and it does not
visit 1 nm at all, at any bias in the sweep.

**The two statements are about different points on the same characteristic**, and `C-0012` reports both:
its `largestModelValidBias` is computed on the free root of `W(s) = 0`, while its
`biasForSimultaneousTarget` is computed at a displacement of exactly 3 nm.

## The held operating point, tested against the same three ranges

Computed in `T-2`'s study from `C-0011`'s solved layer, whose polymer volume per unit area is conserved under
compression, so `φ(h) = φ(L₀)·L₀/h` exactly. `φ` is quoted at the **densest** layer the §4(a)–(d) window admits
at that height, which is the worst case:

| `L₀` | held gap | vs `C-0005`'s 1.46 nm | held `φ` | vs `C-0002`'s 0.2 | §6 target reached at |
|---|---|---|---|---|---|
| 5 nm | **2.0 nm** | above, by 1.37× | 0.0308 | below, by 6.5× | 0.122 – 0.368 V (2 mM) |
| 7 nm | **4.0 nm** | above, by 2.74× | 0.0191 | below, by 10.5× | 0.082 – 0.155 V |
| 10 nm | **7.0 nm** | above, by 4.79× | 0.0900 | below, by 2.2× | 0.134 – 0.192 V |

&nbsp;&nbsp;&nbsp;&nbsp;**All three ranges hold at the held operating point at every height, and the §6 target
is reached at 0.06–0.37 V — five to sixteen times below `CH-0007`'s ~1 V point-ion boundary.**

The 5 nm row is the tight one — 2.0 nm is only 1.37× outside the correlation band — and it is flagged rather
than passed over. At 7 and 10 nm the margins are 2.7× and 4.8×.

## Why this is a scope error and not a numerical one

`C-0012` did not compute anything wrong. It ran the validity test on the operating point its own acceptance
clause **(b′)** is about — the *free* stroke — and reported the ceiling that test produces. The error is in
carrying that ceiling forward as *"the usable bias window"* full stop, when the clause the programme actually
needs, **(c′)**, is evaluated somewhere else entirely.

Three things follow, and the third is why this challenge does not help:

1. **A bias ceiling must be quoted with the load it was evaluated at.** This is the same class of statement as
   `C-0001`'s *"stiffness is not a single number at the resting height — quote it at a stated compression"*, and
   as `CH-0011`'s *"quote `k_es`'s sign with the gap it applies to"*. It is the third instance in this programme.
2. **The unloaded ceiling remains real and remains a design constraint** — on start-up, on load removal, and on
   any excursion that unloads the tile. A device biased at 0.15 V that loses its load snaps to near-contact,
   where no model in this programme applies and where `C-0005`'s correlation physics takes over. That is a
   statement about **failure modes**, not about the working point, and it should be recorded as one.
3. **The coupling that moves the operating point is the constraint.** Reading the ceiling at the held point
   requires the output coupling to exist, and `C-0012`'s own numbers say it must supply 5.3–16.0 pN/nm at
   10 nm / 0.10 V and 85.6–276.6 pN/nm at 7 nm / 0.25 V. What a DNA-origami lever can deliver is `T-16` and is
   **not known**. So the challenge converts one unresolved ceiling into another, and `T-2` reports it that way
   rather than as a loosening.

## What should change

- **In `C-0012`:** the sentence should read *"the usable bias window for the **unloaded** actuator is
  0.02–0.1 V"*, with the held-point row alongside. No table, no threshold and no verdict moves.
- **In `TASKS.md`'s standing findings:** the same qualification, since the bare form is what downstream tasks
  are reading.
- **In `T-4`:** its deliverable is *"the maximum usable bias, which upstream validity already caps at ~0.1 V"*.
  That cap is the unloaded one. `T-4` should produce **two** ceilings — unloaded and held — because they differ
  by more than a factor of two and the device operates at the second.

## How this challenge would fail

If the output coupling `C-0012` requires cannot be built, the actuator has no held operating point, the free
root is the only one it has, and `C-0012`'s ceiling is the only ceiling — in which case this challenge is
vacuous rather than wrong. **`T-16` decides it.** That is the same number `T-2` names as the constraint that
closes the design window, and it is not a coincidence: the two questions are the same question.
