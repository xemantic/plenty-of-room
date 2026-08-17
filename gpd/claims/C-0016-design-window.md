# C-0016 — The Gen-1 design window: non-empty at 7 and 10 nm in the axes §4 names, and decided by an axis it does not

| | |
|---|---|
| **Task** | [`T-2`](../tasks/T-2-design-window.md) |
| **Leaf** | `A2.1`, and it satisfies or explicitly declines the acceptance strings of `A2.2`, `A1.1`, `A1.2`, `A8.2` and `A7.4` |
| **Verification type** | **logical** (constraint intersection over a common grid) **+ in-silico** (the grid and the thresholds are consumed from the emitting studies' own result files and re-intersected, not re-derived) |
| **Verdict** | **PASS on `P1` — a non-empty region is exhibited at two heights and a proof of emptiness with a named crossing at the third. NOT CLOSED on `P2`, in either direction, and the single missing number is named.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** This matters more here than anywhere else in the programme: **a design window is exactly the artifact a reader mistakes for a recommendation.** |
| **Provenance** | `gpd/results/T-2-design-window.json`, produced by `window.DesignWindowStudyKt`; 183 grid points, 3 height windows, 15 bias clauses, 45 stability clauses, an 18-entry provenance ledger; 38 gate-named `window` tests, **646 in the suite**; the result file re-run twice and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, aqueous MgCl₂ at 0.5 / 1 / 2 / 5 / 10 mM, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile; linear PEG; layer heights 5 / 7 / 10 nm |
| **Consumes** | [`C-0011`](C-0011-scf-density-profile.md) (the layer and its grid), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md) (the coupled thresholds and the stability clause), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the layout sweep and the flatness count), [`C-0014`](C-0014-lateral-confinement.md) (the cable relation), [`C-0005`](C-0005-mean-field-screening-validity.md) (the partitioning model), [`C-0004`](C-0004-poroelastic-drainage.md) (the drainage model), [`C-0002`](C-0002-peg-material-parameters.md)/[`C-0003`](C-0003-crossover-valid-layer-response.md) (the material), and the challenges `CH-0001` … `CH-0014` in their final form |
| **Raises** | [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md) against `C-0012` |
| **Re-run by** | [`C-0027`](C-0027-window-resynthesis.md) (`T-25`) against all of iteration 4, and [`C-0051`](C-0051-second-window-resynthesis.md) (`T-118`) against iterations 5–7 |

> ⚠️ **Re-run again against `C-0031`–`C-0050` by [`C-0051`](C-0051-second-window-resynthesis.md)
> (2026-08-14). `P1` stands with `C-0027`'s edges unmoved — 0 of 6, 0 grid steps, no owner change.**
>
> The reason is this claim's own `P2` lesson turned on itself: **of the twenty claims of iterations 5–7
> exactly one is `σ`-resolved**, and it says itself it cannot reach a window edge. Two NEW `σ`-resolved
> constraints appear (`C-0050`'s kinematic and validity stroke ceilings) and neither binds, by 1.71–3.11×.
> **§3's *desired* stroke is now settled kinematically**, `s = L₀ − h < L₀ ≤ 10 nm` (`C-0050`), and the
> deliverable is a **height plus five specification questions** rather than a window.

> ⚠️ **Re-run against iteration 4 by [`C-0027`](C-0027-window-resynthesis.md) (2026-08-13). `P1` stands;
> exactly one edge moves, and it moves outward.**
>
> The 10 nm upper edge goes `0.26015 → 0.28854 nm⁻²` (**one grid step**, window 22.36× → **24.80×**), driven by
> `C-0019`'s fluctuation widening against `CH-0024`'s delivered-stroke tightening. **The 7 nm window is
> unchanged to the last digit** — those two corrections cancel there to exactly the grid. **No edge changes
> owner** and 5 nm is still empty. Axis **(i)**, the lateral-confinement footprint, **leaves the window
> entirely** (`CH-0021` + `CH-0027`), and four new axes were added, none of which resolves in `σ`.

---

## THE HEIGHT CONVENTION — read this before any number below

> **Every layer height here is a FORCE-ONSET height: `L₀` is the height at which the layer carries 1.0 pN
> over the 40 × 40 nm tile** (`C-0011`).

The **first-moment** thickness `2⟨z⟩` of the same layer is **1.71–2.16× smaller** across the surviving windows.
The polymer to order differs by about **four times** between the two conventions, and `T-1e` — which would
separate the definitional part of that gap from the physical part exactly — **has not run**.
`CH-0010` requires this to be stated in Formulate rather than appended, and `T-2` does.

**A bench reading this window in the wrong convention would order 8–9 kDa PEG where it needs 1.1–3.3 kDa.**

> ⚠️ **`T-1e` HAS NOW RUN** — [`C-0077`](C-0077-first-moment-chain-length.md), raising
> [`CH-0091`](../challenges/CH-0091-a-first-moment-ten-nanometre-layer-is-not-a-ten-nanometre-layer.md)
> against this banner. **No edge, owner or verdict of this claim moves.** Three corrections to the
> banner itself:
> 1. **The two conventions are two DEVICES, not two readings of one.** A layer specified at
>    `2⟨z⟩ = 10 nm` puts its tile at **13.20–18.05 nm** — inside §3's stated 5–10 nm band at
>    **0 of 61** grid points. §3 specifies a distance between two bodies, so **this window is in the
>    right convention.**
> 2. **The molar mass is 7.71 kDa at the design point and 4.17–8.73 kDa across the window**, not
>    "8–9 kDa" — that figure came from `C-0011`'s scaling estimate, which `CH-0090` shows is 12–19 % high.
> 3. **The factor is 2.59–2.84×, not "about four"** — four was the top of one range over the bottom
>    of the other. The *"1.71–2.16× smaller"* thickness ratio is correct (measured 1.710–2.065 here).
>
> The banner's **point** — that this is the likeliest way the window gets misread at a bench — is
> upheld intact, and both rows should travel in a bench order: a bench cannot measure a force onset
> and cannot buy a thickness. The gap `CH-0010` opened splits as **2.819× convention × 1.636–1.648×
> physics**, i.e. 62–68 % definitional.

---

## The claim, in one line

**In the axes §4(a)–(d) names the window is not empty — `σ ∈ [0.0116, 0.2601] nm⁻²` at 10 nm and
`[0.0296, 0.0496]` at 7 nm, both bounded below by coil overlap and above by the 3 nm stroke, with 5 nm empty
by a 13.3× crossing of those same two constraints and §4(c) and §4(d) binding nothing anywhere. Adding the
axes this programme discovered the window does not close, but it stops being a statement about grafting
density: three of the five discovered axes are not functions of `σ` at all, and the one that decides the
programme is the output coupling — at 7 and 10 nm the §6 operating point is statically unstable under every
layer model at §3's own buffer, so it exists only against a lever supplying 5–277 pN/nm of its own stiffness,
and no claim in this programme supplies what a DNA-origami lever can deliver.**

---

## `P1` — §4(a)–(d) as posed

Conservative over `C-0011`'s three measurement-anchored interaction laws: shortest stroke, lowest overlap.

| `L₀` | `σ` window [nm⁻²] | width | lower edge | upper edge |
|---|---|---|---|---|
| **5 nm** | **EMPTY** | — | overlap needs `σ ≥ 0.07506` | compliance needs `σ ≤ 0.005635` |
| **7 nm** | **`[0.02955, 0.04960]`** | **1.678×** | **coil overlap `Σ ≥ 1`** | **3 nm stroke at 100 pN** |
| **10 nm** | **`[0.01163, 0.26015]`** | **22.36×** | **coil overlap `Σ ≥ 1`** | **3 nm stroke at 100 pN** |

**Every edge has exactly one owner** — declared falsifier 1 did not fire — and the two owners are the same two
constraints at both heights, running in opposite directions in `σ`. That is §4(a)'s own tension, quantified:
*"dense antifouling-grade PEG is far too stiff to actuate; sparse grafting falls out of the brush regime."*

### The proof of emptiness at 5 nm

> **The layer must be at least `σ = 0.0751 nm⁻²` for its coils to overlap at all, and at most
> `σ = 0.00563 nm⁻²` to deliver 3 nm of stroke against 100 pN. Those two demands miss each other by
> `13.32×` in grafting density.**

No chemistry, no buffer and no bias closes that: the lower bound is the validity condition of the
one-dimensional mean field itself, and the upper bound is §3's own stroke target.
This is the branch of §6 task 2's predicate that names the binding constraint, and it names two.

### `P-5` closes as re-opened: `L₀/R₀ ≥ 1` is not weak, it is exactly vacuous

`P-5`'s adopted criterion admits **all 183 grid points at all three heights** — including layers at
`Σ = 0.063`, which are carpets of isolated mushrooms with a grafting spacing of 22 nm. Asserted as a test over
the whole grid rather than argued. Against a force-onset height it measures how far into the coil's tail the
threshold sits and cannot fall below one. **Coil overlap is the only one of the two that bounds anything**,
and it owns the lower edge at every height.

---

## What a bench would order — bench units, force-onset convention

| | **7 nm** | **10 nm** |
|---|---|---|
| **grafting density `σ`** | **0.0296 – 0.0496 nm⁻²** | **0.0116 – 0.2601 nm⁻²** |
| **grafting spacing `s`** | **4.49 – 5.82 nm** | **1.96 – 9.27 nm** |
| **chain length `N`** | **25.3 – 28.0 monomers** | **36.6 – 74.6 monomers** |
| **PEG molar mass** | **1.11 – 1.23 kDa** | **1.61 – 3.29 kDa** |
| first-moment thickness `2⟨z⟩` | 3.45 – 3.60 nm | 4.64 – 5.84 nm |
| dead-load stroke at 100 pN | 3.06 – 3.40 nm | 3.05 – 6.00 nm |
| secant stiffness | 29.3 – 32.4 pN/nm | 16.6 – 30.6 pN/nm |
| tangent stiffness at `0.8 L₀` | 12.1 – 16.3 pN/nm | 4.07 – 28.4 pN/nm |
| mean `φ` | 0.0071 – 0.0109 | 0.0052 – 0.0630 |
| coil overlap `Σ` | 1.01 – 1.53 | 1.05 – 11.7 |
| piston `σ_RMS` at the working point | 0.187 – 0.191 nm | 0.194 – 0.231 nm |
| attachments for flatness | **45 as 3 × 15**, against 56 crossovers | **45 as 3 × 15** |
| minimum in-plane tether | 28.6 – 31.7 nm | 28.4 – 56.0 nm |
| assembly edge on a 40 nm tile | **97 – 103 nm** | **97 – 152 nm** |

**The 10 nm window is 22× wide in `σ` but its two ends are different devices** — a 9.3 nm spacing carrying a
6.0 nm stroke at 16.6 pN/nm, against a 2.0 nm spacing carrying 3.05 nm at 30.6 pN/nm — so a bench should pick
a target inside it, not treat it as a tolerance. The 7 nm window is 1.7× wide and is a tolerance.

**And this is 1.1–3.3 kDa PEG, not the 8–17 kDa three earlier iterations of this programme would have ordered.**
Most of that difference is the height convention (`CH-0010`); the rest is the conformational pressure that both
trial-function profile models omit. `T-1e` separates them exactly.

---

## §4(c) and §4(d), answered at every point of the window rather than at five labelled ones

| | **7 nm window** | **10 nm window** | requirement |
|---|---|---|---|
| salt partition coefficient `K` | 0.905 – 0.937 | **0.559 – 0.954** | `≤ 1` — exclusion |
| layer-local `λ_D` at 2 mM | 4.06 – 4.13 nm | **4.02 – 5.25 nm** | vs 3.93 nm bulk |
| drainage corner, slowest model | 644 – 742 kHz | **134 – 907 kHz** | `≥ 1 kHz` |

&nbsp;&nbsp;&nbsp;&nbsp;**Neither constraint owns an edge at any height. Both are admissible at all 183 grid points.**

§4(c) is **one-sided by construction and its sign is the opposite of the question's** (`C-0005`): the layer
*excludes* salt, so the local screening length is 1.02–1.34× **longer** inside it — the layer protects the
field rather than shorting it. The bound counts exclusion only; `P-8` (Mg²⁺ coordination by PEG's ether
oxygens) is the one mechanism that could flip it and it does not exist in accessible literature.

§4(d) clears 1 kHz by **134×** at its worst point inside the window, on the least permeable of `C-0004`'s
three models, at the solved layer's own `φ` and secant stiffness. `C-0004`'s discharge is confirmed on a grid
183 times denser than the four points it evaluated.

---

## `P2` — the five axes §4 does not name, and which of them resolve in `σ`

**This is the part of the answer §4 could not ask for**, and its structure is the finding:

| axis | source | resolves in `σ`? | does it narrow the window? |
|---|---|---|---|
| **(h)** peak per-load-path force | `C-0015` | **yes**, through the foundation stiffness | **no** — clear everywhere inside the window |
| **(i)** lateral confinement footprint | `C-0014` | **yes**, through the stroke | **no threshold exists in §3** — reported as a cost |
| **(g)** flatness attachment count | `C-0015` | **no** — topological | no: 45 ≤ 56 crossovers |
| **(e)** usable bias window | `C-0012` | **no** — height-level | **it closes heights, not `σ` intervals** |
| **(f)** output-coupling stiffness | `C-0012` | **no** — height-level | **it is what closes the programme** |

> **Three of the five discovered axes are not functions of grafting density at all. They cannot narrow a
> window; they can only close a height entirely.** A design window in `(σ, L₀)` is therefore the wrong shape
> for the answer, and §6 task 2's own axes are not the ones the decision turns on.

### (h) The unzip exceedance `C-0015` found is unreachable inside the window

The solved layer's secant stiffness puts `C-0015`'s foundation multiplier at **0.823–1.605** across both
windows — entirely inside its swept `×[0.25, 4]`, asserted in a test so nothing extrapolates silently.

| | best registration | worst registration | 10 pN unzip |
|---|---|---|---|
| 7 nm window | 3.90 – 4.14 pN | 6.58 – 6.90 pN | clear |
| 10 nm window | 4.04 – 5.67 pN | 6.77 – 8.90 pN | clear |

`C-0015` entered the 10–15 pN band only at `k_f × 0.25`, and **the solved layer is never that soft**. So the
0.5 % miss `C-0015` reported does not occur anywhere a Gen-1 layer can sit — at *either* registration.
**That is a loosening of a standing constraint, and it is a coupling between two claims that neither could
see alone**: `C-0015` swept a foundation range without knowing which part of it the layer occupies, and
`C-0011` produced a layer without knowing what the lattice does with it.

### (i) Lateral confinement is a footprint cost with no threshold to test against

`L_min = δ√(S/2A)` reproduces `C-0014`'s table exactly (10.2 / 28.0 / 61.3 / 93.3 / 204 nm) as a gate-5 test.
Across the window it runs **28.4–56.0 nm**, i.e. a **97–152 nm assembly around a 40 nm tile**, and it is
*linear in the stroke*, so the widest part of the 10 nm window is also the most expensive in footprint.

**§3 states no footprint budget, so this axis cannot close the window and `T-2` does not pretend it can.**
It is reported as a cost curve. Stated plainly per §7: *the question "is this footprint affordable?" cannot be
answered with the information in the problem definition.*

### (e) The usable bias window, under both readings — and they disagree

**Reading A — `C-0012`'s ceiling as written**, i.e. a bias is usable only up to where the **free** operating
point leaves upstream validity:

| `L₀` | models of 6 reaching both §3 targets, by buffer (0.5 / 1 / 2 / 5 / 10 mM) | binding clause | worst shortfall |
|---|---|---|---|
| 5 nm | 0 / 0 / 0 / 0 / 0 | **stroke** | 1.04 – 1.49× short |
| 7 nm | **3** / **3** / 0 / **1** / 0 | force at 2 and 10 mM | 1.15× at 2 mM |
| 10 nm | 0 / 0 / 0 / 0 / 0 | **force** | **1.40× at 0.5 mM, 2.90× at 2 mM** |

&nbsp;&nbsp;&nbsp;&nbsp;**Under Reading A the only place both §3 targets are simultaneously reachable is
7 nm at ≤ 1 mM MgCl₂ — below §3's own stated buffer range — and there under three of six layer models.**

That is leaf `A2.2`'s low-screening condition vindicated a second time and by a different route from `C-0012`'s.
And the failure at 10 nm is **exactly `σ`-free**: the bias for 100 pN of blocking force is identical to twelve
digits across all six layer models at every `(height, buffer)`, because `F_es` depends only on the tile, the
electrode, the buffer and the gap — and the gap *is* the layer height. **Where the force clause fails it fails
across the entire `σ` window at that height, and no grafting density rescues it.** This was `T-2`'s cheap
bound, run first and *checked* against `T-3`'s records rather than assumed.

**Reading B — the held operating point.** At the §6 target the tile is held at `L₀ − 3 nm`:

| `L₀` | held gap | vs 1.46 nm band | held `φ` (densest admissible layer) | vs `φ = 0.2` | §6 target at |
|---|---|---|---|---|---|
| 5 nm | 2.0 nm | above by 1.37× | 0.0308 | below by 6.5× | 0.122 – 0.368 V |
| 7 nm | 4.0 nm | above by 2.74× | 0.0191 | below by 10.5× | 0.082 – 0.155 V |
| 10 nm | 7.0 nm | above by 4.79× | 0.0900 | below by 2.2× | 0.134 – 0.192 V |

**The held operating point is inside every upstream validity range at every height**, and the target is
reached five to sixteen times below `CH-0007`'s ~1 V boundary. `C-0012`'s 0.02–0.1 V is a property of the
**unloaded** actuator. This is [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md), and it is
**favourable to the programme, which is the direction in which an error survives longest**.

### (f) What Reading B costs — and it is the whole answer

Models statically unstable at the simultaneous §6 target, out of six:

| `L₀` | 0.5 mM | 1 mM | **2 mM** | 5 mM | 10 mM |
|---|---|---|---|---|---|
| 5 nm | 1 | 1 | **1** | 1 | 2 |
| 7 nm | 3 | 4 | **6** | 6 | 6 |
| 10 nm | 6 | 6 | **6** | 6 | target not reached |

Required output-coupling stiffness at 2 mM: **0 at 5 nm below 0.25 V; 11.2 pN/nm at 7 nm / 0.10 V rising to
85.6–276.6 at 0.25 V; 5.3–16.0 pN/nm at 10 nm / 0.10 V rising to 47.6–71.5 at 0.25 V.**
`C-0012`'s table is reproduced to the digit, and its `(c′)` verdict — *PASS at 5 nm, FAIL at 7 and 10 nm* — is
reproduced as a per-buffer count.

> **Static stability wants the thin layer, whose `σ` window is empty by 13.3×. The window and the stroke want
> the thick one, whose operating point is unstable under every layer model at §3's own buffer.**

**That is the shape of the whole Gen-1 answer**, and it is `C-0012`'s height inversion closed against
`C-0011`'s window: the two halves of §6 task 3 run in opposite directions with height, and §4(a)'s own window
runs with the stroke, so all three pull the same way and static stability pulls against all of them.

---

## The verdict on §6 task 2

| branch | verdict |
|---|---|
| **non-empty region satisfying §4(a)–(d)** | **YES at 10 nm and 7 nm**, exhibited above with both edges attributed |
| **proof of emptiness naming the binding constraint** | **YES at 5 nm** — coil overlap against the 3 nm stroke, crossing by 13.32× |
| **the same, with the axes this programme discovered** | **NOT CLOSED IN EITHER DIRECTION.** Under Reading A it is empty everywhere except 7 nm at ≤ 1 mM; under Reading B it survives at 7 and 10 nm **conditional on an output-coupling stiffness nobody has computed** |
| **the constraint that decides it** | **the output coupling.** `T-16`. It is cheap, it is unstarted, and it is the single number that decides whether Gen-1 has a design window at all |
| **§3's *desired* 10 nm stroke** | **unreachable at every height and every grafting density** — `C-0001`'s one surviving headline, now confirmed against a third layer model and a fourth constraint set |

**The honest form of the answer NDI asked for**: *non-empty in the axes §4(a)–(d) names, and undecided — not
empty — once the axes this programme discovered are added, because the axis that decides it is one no task has
yet evaluated.* `T-2` names it rather than closing the window on a guess.

---

## The five verification gates

Executed as 38 tests in `src/test/kotlin/window/`, each named for the gate it discharges.
Full detail in [`T-2`](../tasks/T-2-design-window.md#verify).

- **Gate 1** — `s = σ^(−1/2)` at all 183 points; `Σ = πR₀²σ` reproduced from the file's own `R₀`; `φ = Nσv₀/h`
  reproduced at all 183 points; a window width is a **ratio**; `T-14`'s Winkler modulus × 1600 nm² recovers
  `C-0001`'s published 20.201 pN/nm.
- **Gate 2** — the dead-load stroke is strictly monotone **decreasing** in `σ` and the secant stiffness strictly
  **increasing**, at all three heights, which is what licenses the one-sided transfer of `T-3`'s single-`σ`
  stroke clause onto the lower part of the window; a taller layer strokes further; salt exclusion → 1 as
  `φ → 0`; `φ(h) → φ(L₀)` at zero compression; **a hole in an admissible set throws**.
- **Gate 3** — **the blocking-force bias is exactly model-independent** at all fifteen `(height, buffer)` pairs;
  the intersection is commutative and associative, so the window does not depend on the order the constraints
  were applied in; the interpolated per-path force reproduces every sampled foundation state to `1e−9`; **no
  design point extrapolates `C-0015`'s swept range.**
- **Gate 4** — the `σ` grid is logarithmic with a constant ratio 1.10913 to `1e−6`, so every window edge is
  located to one grid ratio and no better; halving the grid moves an edge by at most one coarse step; **the
  result file is byte-identical on two independent re-runs.**
- **Gate 5** — `C-0011`'s windows reproduced by **two independent code paths**; `C-0005`'s partition
  coefficients to `1e−9`; `C-0004`'s 91 kHz to 2 %; `C-0014`'s tether lengths from the cable relation;
  `C-0015`'s 45-as-3×15 and its exactly-zero crossover force read from `T-14`'s file; `C-0012`'s coupling table
  to the digit; and **the transfer licence**, checked rather than assumed.

### The declared falsifiers, and what actually happened

| # | fired? | outcome |
|---|---|---|
| 1 — an edge no constraint owns | **no** | every edge of both windows has exactly one owner |
| 2 — a non-contiguous admissible set | **no** | all five `P1` constraints are a single contiguous run at all three heights |
| 3 — the solved layer outside `C-0003`'s bracket | **YES, at 5 nm** | see below |
| 4 — a discovered axis not monotone in `σ` | **no** | but three of five are not functions of `σ` at all, which is reported as a finding |
| 5 — the window surviving everything with margin | **no** | 12 of 15 `(height, buffer)` pairs fail Reading A outright |

### Falsifier 3 fired, and this is the exposure it names

| shared design point | solved layer | `C-0003`'s bracket | licensed? |
|---|---|---|---|
| 10 nm, `σ = 0.024` | 5.308 nm | 3.828 – 6.013 nm | **yes** |
| 7 nm, `σ = 0.045` | 3.129 nm | 1.537 – 3.197 nm | **yes** |
| **5 nm, `σ = 0.092`** | **1.869 nm** | **0.473 – 1.530 nm** | **NO — 1.22× above the top** |

**At 5 nm the solved layer is not inside `C-0003`'s response bracket**, so `T-3`'s coupled verdicts there were
computed on a layer `C-0011` does not reproduce, and every 5 nm number taken from `C-0012` is reported here as
an exposure rather than as a result. Two things bound the damage and both are stated rather than assumed:
the 5 nm verdict is decided by `P1` **before** any `T-3` number is used, and the direction is favourable —
the solved layer strokes *more*, so `T-3`'s "5 nm cannot reach 3 nm of stroke" is conservative, and 1.87 nm is
still 1.6× short.

**`CH-0010` upheld `C-0003`'s response numbers at the 10 nm design point. It did not check 5 nm, and 5 nm is
where they fail.** That is a `T-2` finding about a challenge, not about a claim.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** `PASS` means model-consistent and traceable. A design window is the
  artifact most likely to be read as a recommendation, and it is not one.
- **THE HEIGHT CONVENTION IS FORCE-ONSET**, at a defining load of 1 pN over the tile — and
  [`C-0077`](C-0077-first-moment-chain-length.md) (`T-1e`) establishes that this is the **right**
  convention for a window §3 specifies: a first-moment 10 nm layer puts its tile at 13.20–18.05 nm,
  outside §3's 5–10 nm band at 0 of 61 grid points. The conversion to the first-moment convention is
  **2.585–2.843×** in `N` over this window (`CH-0091`).
- **Every window edge is a grid point** on `T-1d`'s 61-point logarithmic sweep, located to **1.109×** and no
  better. The narrowest window is five grid steps wide and the one crossing is 13.3×, so no verdict sits on
  the resolution — but no edge should be quoted to more digits than that.
- **The layer is `C-0011`'s solved SCF profile and inherits every one of its limits**: mean field at
  `φ ≈ 0.01` with the fluctuation corrections **not bounded** (`T-1f`, the largest unbounded exposure under
  this entire window); an interaction free energy that is **not measured below `φ#`**, which is the whole
  working range; monodisperse chains; laterally uniform grafting; a rigid tile (`CH-0005` rejects that, and
  `C-0006`'s dishing ratios are cited rather than recomputed).
- **`T-3`'s coupled verdicts were computed on `C-0003`'s six models at one grafting density per height**, not
  on the solved layer and not across `σ`. The transfer is checked at each shared design point; at 5 nm it is
  **not licensed**. The stroke clause transfers **one-sidedly** to lower `σ` by a monotonicity asserted in a
  test; the stability clause transfers with the **opposite** sign and its magnitude off `T-3`'s own `σ` is
  **not computed here**.
- **Reading A's boundary is bracketed, not located.** `T-3`'s bias grid has no sample between 0.10 V and
  0.25 V (`C-0012`'s own open question 5), so verdicts at heights whose largest valid bias is 0.10 V could
  move if the true crossing is higher.
- **Mean-field electrostatics, inherited whole.** `C-0005` puts the one-loop correction at **123–214 % of the
  leading term across the entire 5–10 nm range**. This is the largest single uncertainty in `P2` and it is not
  reducible by a better Poisson-Boltzmann solve.
- **The in-plane load path is `C-0009`'s out-of-plane concentration factor used as a conservative stand-in**
  (`C-0014`, `T-15`). Every minimum tether length here could shrink by up to 2.8×.
- **The layer is neutral linear PEG.** §3 also permits PEO and a PS→PEG block copolymer, for which **no
  osmotic equation of state was consumed anywhere in this programme**. The "chemistry" axis of §6 task 2's
  title is therefore answered for one chemistry only, and that is stated rather than papered over.
- **No lateral load profile, no tile edge, no fringing** (`T-3b`).

## Numbers that are CITED rather than DERIVED

Generated from the study's own ledger rather than written by hand, per §7.

| quantity | value | unit | source | provenance |
|---|---|---|---|---|
| resting load defining `L₀` | 1.0 | pN | `C-0011` | **CITED** |
| monomer volume `v₀` | 0.06035 | nm³ | `C-0002` via `C-0011` | **CITED** |
| acceptable / desired stroke | 3.0 / 10.0 | nm | §3 | **CITED** |
| target force | 100 | pN | §3 | **CITED** |
| bandwidth target | 1000 | Hz | §3 | **CITED** |
| lateral counterion spacing | 1.46 | nm | `C-0005` | **CITED** |
| concentrated crossover | 0.2 | — | `C-0002` | **CITED** |
| duplex unzip / shear allowable | 10 / 48 | pN | `C-0006` via `C-0015` | **CITED, MEASURED**, loading-rate dependent |
| load concentration factor | 7.6 | — | `C-0009` via `C-0014` | **CITED**, out-of-plane applied in-plane |
| duplex stretch modulus `S` | 1100 | pN | Wang et al. 1997 via `C-0014` | **CITED, MEASURED** |
| bulk Debye length at 2 mM | 3.9268 | nm | `C-0005` | **CITED** |
| flatness attachments / crossovers | 45 / 56 | — | `C-0015` / `C-0009` | **CITED** |
| reference foundation stiffness | 20.201 | pN/nm | `C-0001` secant | **DERIVED here** from `T-14`'s own Winkler modulus, and checked against `C-0001`'s published figure |
| tether allowable | 6.3158 | pN | shear ÷ concentration factor | **DERIVED here** |
| grafting-density grid ratio | 1.10913 | — | `T-1d`'s own sweep | **DERIVED here** |

Everything else — every window edge, every binding attribution, every crossing ratio, the partition
coefficients and Debye lengths at the window's own `φ`, the drainage corners at the window's own stiffness,
the per-load-path forces at the window's own foundation multiplier, the tether lengths, the held-point volume
fractions, and the Reading A/B verdicts — is **derived here from the three consumed result files**.

## Challenges

**Raises [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md)** against `C-0012`'s
*"the usable bias window is 0.02–0.1 V"*. No number in `C-0012` moves; its scope does.

**None stands against this claim.** The two ways it would fail:

1. **`T-16` finding that no DNA-origami lever reaches 5–277 pN/nm at the tile.** `P2` would then close as
   **empty at 7 and 10 nm**, leaving only 5 nm — which `P1` has already emptied by 13.3×, so the Gen-1 stack
   as specified would have **no design window at all**. That is the outcome NDI asked to know about now
   rather than after a year at the bench, and this claim is written so that one number decides it.
2. **`T-1f` bounding the mean-field fluctuation corrections at `φ ≈ 0.01` and finding them large.** Every
   number in `P1` rests on `C-0011`'s solved layer, which does not bound them.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds
rather than overwriting it.
