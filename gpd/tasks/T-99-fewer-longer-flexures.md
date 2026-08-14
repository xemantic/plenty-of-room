# T-99 — Does a coupling of FEWER, LONGER flexures close where 45 short ones do not?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Raised by** | [`C-0040`](../claims/C-0040-hinge-line-census.md), open item 2 — *"the path count is bounded below at 34 by `CH-0029`'s unzip allowable and above by the tile's crossover inventory; a design at 34 paths on longer hinge lines is the one trade this claim does not sweep"* |
| **Verification type** | **logical** (two exact ledgers on a lattice whose pitch is cited and whose inventory `C-0015` counts — no simulation can move a count) **+ in-silico** (`C-0039`'s two-spring **elastica** re-run as a library at every point of the `(path count, hinge count)` grid) |
| **Maturity target** | TRL 1–3 |

---

## Formulate

### The question, stated numerically

Iteration 6 closed the 45-path crossover-hinge flexure three ways, and **every one of them is a
count or a length**:

| | | |
|---|---|---|
| [`C-0040`](../claims/C-0040-hinge-line-census.md) | the 16-crossover hinge line **does not exist** | a crossover serves one *interface* every 32 bp, so a line of `n` needs `(n − 1) × 10.88 nm`; a 40 nm tile carries **four**, at every one of the 32 phases |
| [`C-0041`](../claims/C-0041-flexure-array-packing.md) | 45 flexures **do not pack** | the Gen-1 tile carries exactly **fifteen** |
| [`C-0039`](../claims/C-0039-two-spring-elastica.md) | the usable stroke inside the compliance ceiling is **3.877 nm** | 0 of 34 placements and 0 of 25 sensitivity points reach 10 nm |

So the natural question — `C-0040`'s own — is whether **trading path count against hinge count**
escapes all three at once: fewer flexures, each owning a longer stretch of the tile's interface
line, each therefore hinged on more crossovers.

**This task sweeps `(path count n, hinge count h, hinge-line length ℓ_h = (h − 1)p)` jointly**, and
either exhibits a point that reaches §3's **desired** 10 nm stroke or states plainly that none does
and reports what the best point delivers.

### The conservation law the trade runs into, stated before any solve

The three variables are **not** independent. A hinge line lies on **one interface**, and the tile's
interfaces carry a finite, counted number of crossovers:

&nbsp;&nbsp;&nbsp;&nbsp;**`n · h ≤ N_inv`** &nbsp; (`C-0015`'s inventory: **56** at the ten
eight-column phases, **49** at the other twenty-two), and

&nbsp;&nbsp;&nbsp;&nbsp;**`n · (h − 1) · p ≤ L_line`** &nbsp; (16 lines × 40 nm = **640 nm** of
collinear interface: 14 interior interfaces plus the 2 free edges), and

&nbsp;&nbsp;&nbsp;&nbsp;**`h ≤ 4`** &nbsp; (`C-0040`'s census: the most a 40 nm line holds).

**"Fewer" and "longer" are therefore the same currency spent twice**, and that is the whole
structure of the question.

### The two acceptance readings, both carried

**The compliance ceiling** (`C-0023`'s 40 pN/nm) is **declared at no stroke**, which is exactly
`T-107`'s question, running concurrently. This task therefore reports **every verdict at both
readings** and says which reading each needs:

| id | reading of the ceiling | test |
|---|---|---|
| **`W`** | the ceiling is a **working-point** requirement | `n·k_tangent(δ_work) ≤ 40 pN/nm` only |
| **`S`** | the ceiling binds over the **whole stroke** | `n·k_tangent(s) ≤ 40 pN/nm` for every `s ≤ δ_target`; equivalently `usableStroke ≥ δ_target` |

**The placement** is likewise two readings, and the second has never been run in this programme:

| id | placement | basis |
|---|---|---|
| **`P3`** | secant **33.3333 pN/nm at 3 nm** | the standing convention of `C-0023`/`C-0034`/`C-0039`/`C-0040`/`C-0041` — §3's 100 pN over its **acceptable** stroke |
| **`P10`** | secant **10.0 pN/nm at 10 nm** | `C-0017`'s own placement arithmetic `k_c = F/Δs` applied to §3's **desired** clause. A **different device**, not a re-evaluation of `P3`'s |

`P10` is carried because `C-0017`'s lesson is that *"placement fixes the value, because the force
delivered to a load over a stroke is `k_c·Δs`"* — and every standing claim has read the desired
stroke on a coupling **placed for the acceptable one**, which charges it 333 pN where §3 asks for
100. `P10` is reported with its **stability** consequence attached (`C-0017`/`C-0032`: stability is
a *lower* bound `k_c > |k_eff|` read on the **tangent**), because a softer placement is not free.

### The acceptance predicate

| # | predicate | threshold |
|---|---|---|
| **P1** | **the cheap product bound**: `h ≤ N_inv/n` intersected with `CH-0029`'s path-count floor at the desired stroke | an integer interval in `h`; if it contains only `h = 1` the trade is refused before any elastica runs |
| **P2** | **the hinge-supplied arm ceiling**: the arm a flexure can have if the *whole* inventory carries it and the far anchorage supplies nothing | a length in nm, against §3's desired 10 nm — `δ = r sin θ < r` is `C-0029`'s geometry and needs no constitutive law |
| **P3** | the **joint sweep** over `(n, h)`, arm placed on `C-0039`'s elastica at every point | arm, cap, tangent at both strokes, usable stroke, per-path forces |
| **P4** | **is the feasible region non-empty at §3's desired stroke** — under `W` and under `S`, under `P3` and under `P10` | a point, or the plain statement that there is none |
| **P5** | if empty: **what the best point reaches**, and **which constraint binds at every boundary** of the region | a stroke in nm and a named constraint per edge |
| **P6** | the region's **width** as a ratio, and what changes across it (`C-0016`'s discipline) | a ratio, and the two end designs |

### Units, and the conventions restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, moments and energies **pN·nm**;
  `k_BT = 4.141947 pN·nm` at **T = 300 K** in aqueous **2 mM MgCl₂**.
- **Plan view.** `x` runs **along** the tile's helices, `y` **across** them, origin at the tile
  centre. `z` is positive **upward**, away from the electrode; §1's bias pulls the tile **down**.
- **A hinge line** is a maximal set of crossovers sharing **one interface** and **one pair of
  bodies**. They are collinear along `x`, they turn through the same angle, and their `k_θ` add in
  **parallel** — the only reading under which `h k_θ` is the right torsional spring (`C-0040`).
- **The per-interface crossover pitch is `p = 32 bp = 10.88 nm`**, not 16 bp: crossovers recur every
  16 bp along a *helix* but alternate between its two neighbours (`C-0015`, `C-0040`).
- **The interface-line supply is `(D + 1) × edge`** — 14 interior interfaces of a 15-duplex sheet
  plus its 2 free edges, 640 nm on a 40 nm tile.
- **The element is `C-0034`'s `E5a`**: a crossover hinge of `h` crossovers grounded on the tile, a
  one-duplex arm of length `r`, and the arm's own duplex end as far anchorage (`A2`,
  `k_far = 78.2353 pN·nm/rad` from `C-0029`'s two-terminus counting theorem), solved as `C-0039`'s
  **inextensible two-spring elastica**, free to draw in (`H = 0`).
- **Positive `δ`** is the stroke the coupling delivers; the arm's tip rises `δ = z(L)` and its ends
  approach by the draw-in `e = L − x(L)`.

---

## Plan

### The cheap bounds, and why they run first

| | bound | cost | what it can settle |
|---|---|---|---|
| **1** | **`n · h ≤ N_inv`** intersected with `CH-0029`'s floor `n ≥ 34` at the desired stroke under `P3` | one division | if it forces `h = 1` the *whole* premise of the task — *"fewer paths buy longer hinge lines"* — is refused, because the allowable forbids fewer paths and the inventory forbids longer lines. **Falsifier: `h ≥ 3` surviving the intersection** |
| **2a** | **the hinge-supplied arm ceiling**, `θ tan θ = K δ²/(N_inv k_θ)`, `r = δ/sin θ` | one bisection | the arm the crossover inventory alone can place, **if the far anchorage carries nothing**. It is a function of the **product** `n·h` and of nothing else, so along the whole trade curve `n·h = N_inv` it is **constant** — which is the degeneracy the task exists to test. **Falsifier: it exceeding 10 nm under `P3`** |
| **2b** | **the combined rigid-arm ceiling**, `θ tan θ = K δ²/(N_inv k_θ + n k_far)` | one bisection per `n` | the same bound with `C-0034`'s `A2` anchorage restored. It depends on `n` **as well as** on the product, because the anchorage is per-flexure — so it is the bound that says which way the trade actually runs. **Falsifier: it being independent of `n`** |
| **3** | **the line ledger** `n(h − 1)p ≤ 640 nm` | one division | the second currency, and the one `C-0040` states in nm rather than in counts |

**Cost justification.** The expensive part is `C-0039`'s elastica, which is a shooting solve inside
a placement bisection inside a usable-stroke scan — roughly `10⁴` RK4 sweeps per grid point. The
grid is `~10 × 6`. Bounds 1 and 2 cost three divisions and one bisection between them and can close
`P3` before any of that runs; they are run first for that reason, and **their falsifiers are
declared above**. The elastica is nevertheless run over the whole grid, because `P5` asks what the
best point *reaches*, which a bound cannot supply.

### What would falsify this approach

1. **Bound 1 admitting `h ≥ 3` at the desired stroke.** Then the trade is real and the task is a
   genuine two-dimensional optimisation rather than a degenerate one.
2. **Bound 2's `P3` ceiling exceeding 10 nm.** Then the inventory does not bind the arm and the
   verdict has to come from the ceiling and the allowable alone.
3. **The sweep failing to reproduce `C-0039`'s adopted design at `(n, h) = (45, 16)`** — arm
   12.7198 nm, tangent 36.44 pN/nm at 3 nm, usable stroke 3.877 nm — or `C-0040`'s census at
   40 nm. Both are free, strong limiting cases and either failing invalidates the pipeline.
4. **The `P10` placement violating `C-0017`/`C-0032`'s stability floor**, in which case `P10` is not
   an available reading at all and must be reported as refused rather than as an escape.
5. **A grid-independent verdict failing to appear** — if the answer moves between a coarse and a
   refined `(n, h)` grid, the region is being resolved by the sampling and not by the physics.

### Method

1. Both ledgers as exact integer/length functions, with the census re-run through `C-0040`'s own
   `hingeLineCensus` rather than tabulated.
2. The hinge-supplied ceiling in closed form at small rotation (`r = √(N k_θ/K)`) and by bisection
   at exact rotation, the two asserted to bracket each other.
3. The joint sweep, `arm = elasticaArmForStiffness(k_θ, h, k_far, EI, n, K, δ_work)`, at each of
   `P3` and `P10`, with the usable stroke scanned from below exactly as `C-0039` does.
4. The feasible region assembled by intersecting the named constraints, with **the binding one
   recorded at every boundary**, and its width reported as a ratio.
5. All five verification gates as executable tests, the limiting cases being `C-0039`'s `(45, 16)`
   design, `C-0040`'s census and fan law, and `C-0041`'s packing-limited count.
