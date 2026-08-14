# CH-0045 — The mounting sense is not a free binary, and "which body carries the standoffs" is half the variable

| | |
|---|---|
| **Against** | [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) — its Deliverable 4, its *"Still open"* item 1, and its sensitivity row `mounting: favourable → adverse` |
| **Raised by** | [`C-0035`](../claims/C-0035-flexure-mounting-sense.md), task [`T-75`](../tasks/T-75-flexure-mounting-sense.md) |
| **Grounds** | **methodological** — a design variable named without the kinematic chain that determines it, and an emptiness quoted over a swept interval rather than as a threshold |
| **Status** | **RAISED.** `C-0030`'s numbers all reproduce; its *framing* of the variable does not survive. |

---

## What `C-0030` says

> *"`Φδ` is odd and `e(δ)` is even, so the coupled law is no longer odd and the sign of everything above is decided by **WHICH BODY CARRIES THE STANDOFFS**."*

and

> *"**Both mountings are reported and NEITHER is asserted to be the one §3 builds.** §3 does not say which body carries the standoffs; nothing upstream does either. This is a **specification gap**, not a modelling one."*

and, of the adverse mounting:

> *"**`P3` fails at every one of the eight lengths** … There is no `ℓ` at which the adverse mounting passes."*

## Why each of the three is wrong

### 1. The sign is not free — it is a kinematic identity

The flexure's midspan is tied to one body and its ends stand on standoffs rooted in the other. So the midspan's deflection **relative to its own ends** is exactly the change in the two bodies' separation, and differentiating that chain along the stroke gives, with no free parameter,

&nbsp;&nbsp;&nbsp;&nbsp;**`dδ/ds = (v_base − v_driven)/n`**, &nbsp; `v_TILE = −1` (§1: the bias pulls the tile down), &nbsp; `v_SUPERSTRUCTURE = 0`, &nbsp; `n = ±1`.

It is exactly `±1`, and it contains **no length** — asserted over 4 mountings × 4 standoff lengths × 3 tie lengths. A builder does not get to choose it once the topology is chosen; a builder chooses the topology.

### 2. "Which body carries the standoffs" is half the variable, and half of it carries no information

The sign is the **product** of two binaries: the base body **and** the direction the standoffs point out of that body's plane.

| mounting | base body | standoff normal | `dδ/ds` | sense |
|---|---|---|---|---|
| `Tu` | tile | up | −1 | adverse |
| `Td` | tile | **down** | +1 | favourable |
| `Sd` | superstructure | down | −1 | adverse |
| `Su` | superstructure | **up** | +1 | favourable |

**Both bodies appear on both sides of the split, and so do both normals.** `favourableCountWithTileBase = 1` of 2 and `favourableCountWithUpwardNormal = 1` of 2 — so naming the body alone predicts the sign no better than a coin. Flipping either variable alone flips the sign at every mounting, which is asserted separately as a test.

The physically meaningful statement of the same thing is **topological**: favourable ⟺ the driven body lies on the **far side of the standoff base plane from the beam**, i.e. the midspan's tie **crosses that plane**, i.e. the flexure is **outboard** of its own ground rather than **inboard** between the two bodies. That is checked here by a second, independent construction — comparing three `z` coordinates of the built stack — and it agrees with the differentiated chain at every realisable geometry.

### 3. The adverse mounting is short of a LENGTH, not of a mechanism

`C-0030` swept `ℓ = 3–10 nm`. The adverse assembled tangent falls **monotonically** with the standoff length, and it meets `C-0023`'s 40 pN/nm ceiling at

&nbsp;&nbsp;&nbsp;&nbsp;**`ℓ = 13.16 nm`**, against **3.48 nm** favourable — **3.78× longer**.

So *"there is no `ℓ` at which the adverse mounting passes"* is a statement about the swept interval. The correct form — the one that is falsifiable by a single longer standoff — is **the length the window would need, quoted against the envelope**: 13.16 nm against `C-0017`'s 10 nm (`C-0030`'s own `P5`). It is still a fail, and it is a fail for a *stated* reason. This is the same discipline `C-0030` itself applies to `CH-0037`.

## What survives

**Everything numeric.** `C-0035` re-runs `C-0030`'s pipeline as a library and reproduces:

| `C-0030` | published | reproduced | departure |
|---|---|---|---|
| favourable span at `ℓ = 8 nm` | 31.82 nm | 31.820924 | `2.9e−5` |
| favourable assembled tangent at 3 nm | 25.23 pN/nm | 25.227268 | `1.1e−4` |
| adverse span at `ℓ = 8 nm` | 40.14 nm | 40.137176 | `7.0e−5` |
| adverse assembled tangent at 3 nm | 44.82 pN/nm | 44.817310 | `6.0e−5` |
| clearance at `ℓ = 8 nm` | 5.31 nm | 5.310000 | `1.7e−16` |
| length covering the desired stroke | 12.69 nm | 12.70 (grid) | `7.9e−4` |

**And the physics `C-0030` discovered is unchanged and is what makes the answer decidable at all:** because the law is signed and not odd, the topology matters — and because the topology is a kinematic chain rather than a preference, it can be determined.

## What changes downstream

- **`T-75` closes as a determination, not as a specification gap.** The programme's specification-gap count goes back to three (§3's electrode material, its loading rate, and — new — whether the superstructure may be perforated), not four.
- **`C-0030`'s sensitivity row `mounting: favourable → adverse` stops being a sensitivity.** It is a comparison between a buildable configuration and three unbuildable ones.
- **The recommended design gains a sentence it did not have:** the standoffs stand on the **output superstructure**, not on the tile, so the tile carries only `C-0015`'s 45 tie attachments and no out-of-plane element at all.

## How this challenge would itself fail

1. **A specification placing the output superstructure below the tile**, which exchanges `v_TILE` and `v_SUPERSTRUCTURE` and makes `Td` the survivor instead of `Su`. The sign would still be determined; only the answer would move.
2. **A coupling whose midspan tie goes slack over part of the stroke** — `C-0023`'s one-sided element returning. Then `|dδ/ds| < 1` there and the placement condition moves, but **no sign changes**.
3. **A lever whose own attachment descends faster than the tile drives it** (`f > 1` in `C-0035`'s scaling), which no passive load does. `T-33`.
