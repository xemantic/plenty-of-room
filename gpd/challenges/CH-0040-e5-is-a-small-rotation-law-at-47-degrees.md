# CH-0040 — `E5` is a small-rotation law evaluated at 47°, and its arm is capped below §3's desired stroke by the placement condition itself

| | |
|---|---|
| **Raised by** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) (`T-67`) |
| **Against** | [`C-0023`](../claims/C-0023-two-sided-coupling.md) (`T-23`), and by inheritance `C-0025`'s and `C-0028`'s *"`E5` is untouched, and still the fallback"* |
| **Date** | 2026-08-13 |
| **Grounds** | **geometric** — a tip cannot rise past its arm, and the arm is capped by the mandate |
| **Severity** | **high**: `E5` is the branch the whole standoff line falls back to |

---

## The statement challenged

`C-0023` files `E5`, the crossover-hinge flexure, as

> **arm 4.11 nm = 12 bp**, `R(−0.5) = −16.67`, secant 33.333, tangent 33.333, `t/s` = 1.000,
> **PASS on all four, and the most compact**,

on the law `1/k = r²/(n k_θ) + r³/(c EI)`, which is `δ = r θ` — the **small-rotation** reading.
`C-0025` and `C-0028` both inherit it verbatim as the fallback if the standoff fails.

## The ground, and it is in two parts

### 1. The working point is at 47°, not at small angle

The tip of an arm of length `r` on a torsional hinge rises `δ = r sin θ`, not `r θ`,
and the restoring moment acts through a lever `r cos θ`, so `F = k θ/(r cos θ)`.
At `C-0023`'s own 3 nm working point on its own 4.11 nm arm,

&nbsp;&nbsp;&nbsp;&nbsp;`θ = asin(3/4.11) = ` **46.9°**.

Re-solving the placement condition under the exact law puts the arm at **4.62 nm** and gives a
**tangent of 51.65 pN/nm against a 33.333 secant** — `t/s` = **1.549**, not 1.000.
**`E5` fails `C-0023`'s own 40 pN/nm compliance ceiling by 1.29×**, on the element `C-0023` reports as
*"exactly linear, so secant = tangent and placement and stability are discharged by one number"*.

### 2. The arm is capped below the desired stroke, at any hinge count

The hinge and the arm's own bending are in **series** at the tip, so the assembled stiffness can
never exceed `n c EI/r³`, and the placement condition therefore caps the arm at

&nbsp;&nbsp;&nbsp;&nbsp;**`r ≤ (c·n·EI/k_target)^(1/3)`** = `(3 × 45 × 230/33.3333)^(1/3)` = **9.77 nm**.

§3's **desired** stroke is 10 nm, and **a tip cannot rise past its arm.**
So `E5` cannot deliver the desired stroke at *any* hinge constant and *any* hinge count —
not because the hinge is too soft, but because an arm long enough to reach is too soft to place.
Solved arms at 1, 2, 4, 8, 64 and 1024 crossovers are 4.62, 5.74, 6.98, 8.06 nm and below,
all under the cap, and the cap is under the stroke.

**Neither half of this needs a constitutive law.** `δ ≤ r` is geometry and the cap is the
placement condition's own arithmetic.

## What it costs, and the remedy is in the same expression

`c` is the arm's end-condition factor: 3 for a cantilever, **12 for a guided arm**. That single
change lifts the cap to **15.50 nm** and the branch reopens:

| | `C-0023`'s `E5` | `C-0029`'s `E5g16` |
|---|---|---|
| arm | 4.11 nm = 12 bp, cantilever | **12.24 nm = 36 bp, guided** |
| crossovers | 1 | **16** |
| secant at 3 nm | 33.333 | **33.333** (placed) |
| tangent at 3 nm | 33.333 (**understated**) | **33.68** |
| tangent at 10 nm | — (unreachable) | **38.68**, inside the 40 pN/nm ceiling |
| rotation at 10 nm | — (unreachable) | **23.2°** |
| force per crossover at 10 nm | — | **2.04 pN** against 10 pN unzip |
| reaches §3's desired stroke | **no** | **yes** |

## What does NOT move

`C-0023`'s filed numbers are reproduced: the arm 4.111 against 4.11 (3.0e−4), the hinge
compliance share 0.9254 against 0.925 (4.3e−4), the hinge bond force 3.396 pN against 3.40
(1.1e−3). The exact law reduces to `C-0023`'s **identically** as the rotation vanishes, asserted
as a gate-2 test. `E5`'s **two-sidedness**, its `r ∝ √k_θ` insensitivity to `T-9`, and
`C-0023`'s whole `CH-0027` currency argument are untouched — this challenge is about the
*amplitude* at which the law is evaluated, not about its sign.

`C-0023`'s own 3 nm working point verdict also survives on the *placement* clause: at 3 nm the
element does deliver 33.333 pN/nm as a secant. What fails is the **tangent** there and the
**stroke** at 10 nm.

## How this challenge would itself fail

1. **A crossover hinge whose moment is sublinear in the angle**, so that the exact law's stiffening is smaller than modelled. Chen et al.'s constant is a small-angle fit and its extrapolation to 47° is not established — but the *geometric* cap `δ ≤ r ≤ (c n EI/k)^(1/3)` survives any constitutive law.
2. **A larger `c`.** That is the remedy, not a refutation, and it is taken above.
3. **Reading the acceptance at §3's ACCEPTABLE 3 nm only.** There `E5` as filed still fails the tangent by 1.29× but delivers the stroke.
