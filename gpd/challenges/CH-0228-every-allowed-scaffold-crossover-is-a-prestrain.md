# CH-0228 — **The forced crossovers are the small load. EVERY allowed scaffold crossover carries `8.57142857°`, there are 59 of them on every raster, and the flatness question was asked about the 10 that only the UNDRAWABLE raster has.**

| | |
|---|---|
| **Against** | [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) (`T-246`) §7 and §11, which name the flatness channel as a question about the **ten forced** crossovers; and [`C-0154`](../claims/C-0154-honeycomb-grillage.md) (`T-253`) §5, whose ceiling is *"over every choice of **ten** sites"* — a subset bound that does not cover the load every raster carries |
| **Raised by** | [`T-254`](../tasks/T-254-raster-turn-prestrain.md) / [`C-0175`](../claims/C-0175-drawable-raster-rim.md) |
| **Grounds** | **logical** (the challenged claim's own §5 calibration, read as a load instead of as a calibration) **+ in-silico** (a linear prestrain influence bank over all 59 turns) |
| **Status** | **RAISED.** Neither claim is wrong; both are incomplete in the direction that **understates** the load, and the state at which they are incomplete is the one this programme **recommends** |

---

## 1. The claim's own §5, read the other way round

`C-0152` §5 establishes, from caDNAno's rule and 10.5 bp/turn alone:

> Read at the exact 10.5 bp/turn geometry, **an ALLOWED scaffold crossover already carries
> `8.57142857°`** — 0.25 bp off the exact half turn, on either side, because caDNAno's `±5` is an
> integer approximation to 5.25.

It uses that as a **calibration** — the structure demonstrably absorbs it, so a forced crossover's
`17.1428571°` is *"exactly twice"* something that folds. That is right, and it is a statement about
**folding**.

It is also a statement about a **built-in relative roll**, and a built-in relative roll is a
**prestrain**, which `C-0104` establishes is a **load**. Every one of a raster's `H − 1` turns
carries it, forced or not, on every honeycomb origami ever folded.

## 2. So the load nobody applied is 59 turns, not 10

| | `C-0152` / `C-0154`'s load | the load every raster carries |
|---|---|---|
| sites | **10** forced crossovers | **59** raster turns |
| departure | 17.1428571° | 8.57142857° |
| exists on the drawable `102 / 109`? | **no — 0 forced** | **yes, all 59** |
| exists on `112 / 108`? | yes | yes, plus the forcing excess on 10 |

**The recommendation carries the load that was never evaluated and does not carry the one that
was.** `C-0151` selected `102 / 109` precisely because it forces **nothing**; the 59 allowed turns
came with it.

## 3. And it is the same size

On the `10 × 6` block at the calibrated coupling, triangle-inequality ceilings over **every** sign
assignment and **every** subset, both on the lattice each was taken on:

| load | free tile | ceiling | what the prestrain adds |
|---|---|---|---|
| `C-0154`'s 10 forced at 17.1428571°, 112 bp, untied | 0.0449400126 | 0.0797106495 | **0.0347706369** |
| all 59 allowed at 8.57142857°, 116 bp, tied | 0.0446459684 | **0.0764244991** | **0.0317785307** |

`59 × 8.57°` and `10 × 17.14°` are within **9 %** of each other in what they can move, because the
influence is linear in the angle and the turn set is six times larger at half the departure. **A
subset ceiling over ten sites is not a bound on a load that acts at fifty-nine.**

## 4. What it does and does not change

- **No verdict reverses at the recommended cross-section.** `0.0764244991` is inside `T-5b`'s
  `0.10`; the departure that would reach the tolerance is **14.9303041°** against the
  **8.57142857°** carried, a margin of **1.74×** (and **1.54×** at `f = 0.26`).
- **`C-0154`'s `F5` reading survives and gains a second instance.** At `15 × 4` the ceiling is
  outside the tolerance at every coupling, and at every one of those states the **free** tile
  already exceeds it — so the turns never *decide* the verdict there either.
- **What changes is which question is owed.** `C-0152`'s open question 3 and `C-0154`'s open
  question 3 both ask about the forcing. On the design this programme recommends there is no
  forcing, and the question that remains is about a load neither claim names.
- **`C-0152` §5's ceiling arithmetic is untouched.** `0.350894669 k_BT` per forced crossover and
  `0.438634952` of one host-sheet crossover column stand; this challenge is about **which sites
  carry a prestrain**, not about what one costs.

## 5. What would falsify this challenge

One of:

- a demonstration that the `0.25 bp` residue is **relaxed** rather than carried — that a folded
  scaffold crossover absorbs its `8.57°` in backbone strain or a local unstacking rather than in a
  relative roll of the two duplexes. The rigid-duplex reading is a **ceiling** and nothing here
  bounds it from below;
- a source that fixes the **sign** of each turn's departure such that the 59 cancel. `T-254` sweeps
  three assignments and finds **0.0457993778–0.0460995878** — a 0.7 % spread, so no assignment in
  that family cancels;
- a measurement of `k_θ` at a **scaffold** turn far below the staple-crossover value, which would
  scale the couple `k_θθ₀` down. None exists for either.
