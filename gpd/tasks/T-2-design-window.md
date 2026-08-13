# T-2 — The feasible design window in (grafting density, height, chemistry)

| | |
|---|---|
| **Task** | `T-2`, §6 task 2 of [the problem definition](../../third-party/2026-08-ndi-gen1-problem-definition.md) |
| **Leaf** | `A2.1`, and it must satisfy the acceptance strings of `A2.2`, `A1.1`, `A1.2`, `A8.2` and `A7.4` as well |
| **Verification type** | **logical** (constraint intersection over a common grid) **+ in-silico** (the grid and the thresholds are consumed from the emitting studies' own result files and re-intersected, not re-derived) |
| **Status** | **DONE** — claim [`C-0016`](../claims/C-0016-design-window.md), challenge [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md) |

---

## Formulate

### The question, as posed

> **Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint.**

Until `C-0011` landed, **neither** branch was available:
`C-0003` had said the window's existence at 10 nm was decided by the *profile model*, not by the interaction law,
and neither profile model's premise was met.
`C-0011` solved the profile and both branches became available at once.

### The tightened, falsifiable predicate

The predicate above is not falsifiable as written, because it does not say what "satisfying §4(a)–(d)" means numerically,
and because §4 names four axes while this programme has since found five more.
It is tightened here into **two predicates that are answered separately and reported separately**, because
this task's central finding is that they give different answers.

> **P1 — the predicate as posed.**
> A non-empty set of `(L₀, σ)` at which, simultaneously:
> **(a)** the layer is in the brush regime — coil overlap `Σ = πR₀²σ ≥ 1` **and** `L₀/R₀ ≥ 1` —
> and compliant enough to deliver §3's *acceptable* stroke, `δ ≥ 3.0 nm` under a 100 pN dead load over the 40 × 40 nm tile;
> **(b)** `L₀ ∈ {5, 7, 10} nm`, §3's stated heights, each answered separately;
> **(c)** the layer's salt partitioning does not defeat the drive — `K_salt` and the layer-local Debye length reported at every surviving point;
> **(d)** the poroelastic drainage corner exceeds §3's 1 kHz bandwidth.
> **FALSIFIED** for a given `L₀` by exhibiting the two constraints whose admissible `σ` intervals do not intersect, and their crossing ratio.

> **P2 — the predicate with the axes this programme discovered.**
> The P1 region, further required to satisfy, simultaneously:
> **(e)** §3's 100 pN force target reachable at a bias inside the **usable** window — and the usable window is
> **answered under two readings, not one**, because `C-0012`'s declared `0.02–0.1 V` is a property of the
> **free** operating point and the device §3 specifies works against a 100 pN load. **Reading A** takes
> `C-0012`'s ceiling as written; **Reading B** applies the validity tests at the **held** operating point,
> `h = L₀ − 3 nm`, whose only remaining ceiling is `CH-0007`'s ~1 V point-ion boundary.
> Which reading is right is itself a `T-2` deliverable and it is filed as a challenge, not decided quietly;
> **(f)** the loaded operating point statically stable, `k_eff > 0`, or an output coupling supplying `|k_eff|` — and the stroke that survives that coupling still `≥ 3.0 nm`;
> **(g)** flatness reachable with at most the 56 attachment points the tile's crossovers provide (`C-0015`);
> **(h)** peak per-load-path force below the 10–15 pN single-duplex unzip allowable (`C-0009`, `C-0015`);
> **(i)** lateral confinement available at a frame standoff the device can carry (`C-0014`).
> **FALSIFIED** by the same exhibition, on the enlarged constraint set.

**The verdict is required to name, for every edge of every surviving interval and for every height that empties, the single constraint that binds it.**
An answer that reports a window without naming what closes each edge does not discharge this predicate.

### Locked units

SI throughout, in the programme's locked forms.
Lengths **nm**; grafting density **nm⁻²** and grafting spacing `s = σ^(−1/2)` **nm**;
chain length `N` in **monomers** and the polymer to order in **kDa**;
forces **pN**; stiffness **pN/nm**; pressure **pN/nm² = 1 MPa exactly**;
energies in **k_BT** and **eV**; bias in **V**; buffer in **mM MgCl₂**; frequency in **Hz**.
`T = 300 K`, aqueous buffer with stated Mg²⁺, `k_BT = 4.142 pN·nm`.
**A window width is a ratio of its edges, never a difference** — a grafting-density window spans decades,
and "22.4× wide" is what a process engineer can act on.

### Geometry and sign conventions, restated

Inherited unchanged from the claims consumed, and restated here because a window read at the bench is
exactly the artifact in which a convention gets lost:

- `z` normal to the electrode, positive away from it, origin at the electrode surface.
- Chains grafted at `z = 0`, one end fixed and the other free; the tile is a **rigid non-adsorbing wall** at height `h`.
- **The electrostatic gap is the layer height, exactly and by construction** (`C-0012`): the tile's bottom face rests on the layer's outer surface.
- Compression means `h < L₀`; disjoining pressure **positive** when the layer pushes the tile away.
- `k = −dF/dh`, positive for a restoring layer. `k_es = −∂F_es,z/∂z` is **negative** above the force maximum and positive below it (`CH-0011`).
- `φ` **always** means the physical polymer volume fraction, `N σ v₀ / h` on average.
- A **stroke** is a root of a force balance, never a force divided by a stiffness (`C-0012`).
- Every constraint is evaluated **conservatively over `C-0011`'s three interaction laws**: the shortest stroke, the lowest overlap, the softest layer, the densest layer.

### THE HEIGHT CONVENTION — stated in Formulate, not appended

`C-0011`/`CH-0010` require this and it is the single most likely way for this window to be misread.

> **Every layer height in this window is a FORCE-ONSET height:
> `L₀` is the height at which the layer carries 1.0 pN over the 40 × 40 nm tile.**

An SCF layer has no resting height at all unless one is defined — its pressure reaches zero only asymptotically —
so `L₀` is a *convention* with a load attached, and the load travels with it.
The **first-moment** thickness `2⟨z⟩` of the same layer is reported alongside every point, and the run finds it
**1.71–2.16× smaller** across the surviving windows (1.94–2.03× at 7 nm, 1.71–2.16× at 10 nm) — a *range*, not the
single figure the pre-run draft of this file asserted.
A hundred-fold change in the defining load moves `N` by 2.5× (`C-0011`).

**The polymer to order differs by about four times between the two conventions.**
The window below is in the force-onset convention. The first-moment inversion is `T-1e` and **has not run**;
what is reported for it here is `C-0011`'s own `N^(0.5–0.55)` scaling, flagged as an extrapolation.

---

## Plan

### Method, and why this one

This is a **synthesis and intersection** task. Every physical quantity it needs already exists,
verified, in a claim that carries its own validity range. The method is therefore:

1. **Consume the emitting studies' own machine-readable results** — `gpd/results/T-1d-scf-density-profile.json`
   for the layer grid (183 design points × 5 models), `gpd/results/T-3-stroke-and-blocking-force.json`
   for the coupled thresholds (90 threshold records, 810 operating points), and
   `gpd/results/T-14-crossover-phase-and-registration.json` for the layout sweep — rather than re-typing their tables.
   A number copied from a claim's prose is a transcription risk; a number read from the file the claim was written from is not.
2. **Consume the claims' own code as libraries** where a relation has to be evaluated at a point the claim did not
   tabulate — `electrostatics.LayerPartitioning` for §4(c) at the window's own `φ`, `poroelastic.drainageResponse`
   for §4(d) at the window's own `φ` and stiffness. **Nothing in another package is edited.**
3. **Carry every remaining scalar as a tagged ledger entry** with its claim ID and a `CITED`/`DERIVED` flag,
   so that the list §7 asks for is generated rather than written by hand.
4. **Intersect on a common grid** — the 61-point logarithmic `σ` grid `T-1d` already ran, at each of §3's three heights —
   and report the surviving interval, the binding constraint at each edge, and for an empty height the two
   constraints that cross and their crossing ratio.

### Justification against cost

The expensive alternatives were costed and declined, and the reason is not merely price:

- **Re-solving the SCF profile on a finer `σ` grid** would locate the window edges to better than the grid's
  1.109× ratio. The run confirms it would not move any verdict: the narrowest surviving window is
  **1.678× wide, i.e. five grid steps**, and the one height that empties does so by a **13.3× crossing** —
  so a one-step error at any edge changes no verdict anywhere, and the 33-minute solve buys resolution in
  the one place the answer is insensitive.
- **A coupled solve of the electrostatics against the *SCF* layer**, rather than against `C-0003`'s six models,
  is the one calculation that would genuinely tighten this answer — and it is `T-3`'s to own, not `T-2`'s.
  What licenses the transfer is stated and checked rather than assumed: `CH-0010` upholds `C-0003`'s **response**
  numbers (stroke, secant stiffness) while rejecting its structural ones, and gate 5 below checks the SCF layer
  lands inside `C-0003`'s bracket at every shared design point. **Where it does not, that is reported as an
  exposure, not absorbed** — and it does not, at 5 nm.
- **Any new physics at all** would be the wrong purchase. `T-2`'s deliverable is a decision about a programme,
  and the decision is already determined by results that exist. The cheap route is not merely cheaper here;
  a new model would introduce a seventh layer description into an answer whose whole difficulty is holding
  six of them at once.

### What would falsify this approach

Stated in advance, per §5:

1. **A window edge that no single constraint owns.** The method reports a binding constraint per edge. If two
   constraints coincide at an edge to within the grid spacing, the attribution is not resolved and must be
   reported as a tie rather than a name.
2. **A non-contiguous admissible set.** The method reports an interval. If a constraint's admissible `σ` set has
   a hole in it, an interval is the wrong object and reporting one would hide the hole. **Asserted in code; it throws.**
3. **The SCF layer landing outside `C-0003`'s response bracket** at a shared design point, which would remove the
   licence to transfer `C-0012`'s coupled verdicts onto `C-0011`'s window and leave `P2` unanswerable.
4. **A discovered axis that is not monotone in `σ`**, which would make "the binding constraint at an edge" ill-posed.
5. **The window surviving every constraint with margin**, which would mean the constraint set is too weak to be
   the answer to a feasibility question and something has been left out.

### The cheap bound, run first

Before any intersection: **the bias for 100 pN of blocking force is independent of grafting density**, because
`F_es` is a property of the tile, the electrode, the buffer and the gap, and the gap *is* the layer height.
This is a claim to be *checked* against `T-3`'s threshold records, not one established here —
and it is checked, as a gate-3 test, over all fifteen `(height, buffer)` pairs and all six layer models.

---

> **Note on this file.** An earlier attempt at this iteration was interrupted, and wrote Execute, Verify and
> File sections — including five gate outcomes marked `PASS` and a list of which declared falsifiers had
> fired — **before any code existed**. Those sections were removed by the coordinator. Nothing in this
> project may record a verification outcome that was not produced by a run; the sections below were
> written only after the study existed and had been executed. What survives above is the Formulate and Plan
> work, which legitimately precedes execution — and three sentences in it that asserted an *outcome* have
> been re-derived and corrected against the run: the first-moment ratio (asserted 1.9–2.0×, measured
> **1.71–2.16×**), the grid-resolution justification (asserted "a factor of 1.4 or more", now stated as the
> measured 5-grid-step window and 13.3× crossing), and predicate `P2(e)`, which asserted a single usable
> bias window and is now answered under two readings because the run showed they disagree.

---

## Execute

**Study:** `./gradlew study -Pstudy=window.DesignWindowStudyKt`
**Emits:** [`gpd/results/T-2-design-window.json`](../results/T-2-design-window.json) —
183 grid points × 27 reported quantities, 3 height windows, 15 bias clauses, 45 stability clauses,
an 18-entry provenance ledger, and the five declared falsifiers with their actual outcomes.
**Code:** `src/main/kotlin/window/` — `ConstraintInterval.kt` (the interval algebra),
`UpstreamResults.kt` (the readers), `WindowPhysics.kt` (the four relations evaluated at untabulated points),
`WindowResultRounding.kt`, `DesignWindowStudy.kt`.
**Tests:** `src/test/kotlin/window/` — 38 gate-named tests, 646 in the suite, all green.
**Determinism:** the result file was re-run twice and diffed **byte-for-byte identical** both times.

### P1 — §4(a)–(d) as posed

Force-onset convention, `L₀` defined at 1 pN over the tile, conservative over all three interaction laws.

| `L₀` | `σ` window [nm⁻²] | width | lower edge bound by | upper edge bound by |
|---|---|---|---|---|
| **5 nm** | **EMPTY** | — | coil overlap needs `σ ≥ 0.0751` | compliance needs `σ ≤ 0.00563` |
| **7 nm** | **`[0.02955, 0.04960]`** | **1.678×** | coil overlap `Σ ≥ 1` | 3 nm stroke at 100 pN |
| **10 nm** | **`[0.01163, 0.26015]`** | **22.36×** | coil overlap `Σ ≥ 1` | 3 nm stroke at 100 pN |

**5 nm is empty and the crossing ratio is 13.32×** — the layer must be at least 0.0751 nm⁻² to be a brush at
all and at most 0.00563 nm⁻² to deliver 3 nm of stroke, and those two demands miss each other by a factor of
thirteen in grafting density. No chemistry, no buffer and no bias closes that.

`C-0011`'s published windows are reproduced exactly, by two independent code paths (this intersection, and
`T-1d`'s own `strokeWindows` records), as gate-5 tests.

### What a bench would order — bench units, force-onset convention

| | **7 nm** | **10 nm** |
|---|---|---|
| grafting density `σ` | **0.0296 – 0.0496 nm⁻²** | **0.0116 – 0.2601 nm⁻²** |
| grafting spacing `s` | **4.49 – 5.82 nm** | **1.96 – 9.27 nm** |
| chain length `N` | **25.3 – 28.0 monomers** | **36.6 – 74.6 monomers** |
| **PEG molar mass** | **1.11 – 1.23 kDa** | **1.61 – 3.29 kDa** |
| first-moment thickness `2⟨z⟩` | 3.45 – 3.60 nm | 4.64 – 5.84 nm |
| convention ratio `L₀/2⟨z⟩` | 1.94 – 2.03 | 1.71 – 2.16 |
| dead-load stroke at 100 pN | 3.06 – 3.40 nm | 3.05 – 6.00 nm |
| secant stiffness | 29.3 – 32.4 pN/nm | 16.6 – 30.6 pN/nm |
| mean `φ` | 0.0071 – 0.0109 | 0.0052 – 0.0630 |
| attachments for flatness | 45 as 3 × 15, against 56 crossovers | 45 as 3 × 15 |
| minimum in-plane tether | 28.6 – 31.7 nm | 28.4 – 56.0 nm |
| assembly edge on a 40 nm tile | 97 – 103 nm | 97 – 152 nm |

**This is 1.1–3.3 kDa PEG, not the 8–17 kDa three earlier iterations of this programme would have ordered.**
The difference is the height convention plus the conformational pressure `CH-0010` identified; `T-1e` separates
the two exactly and has not run.

### §4(c) and §4(d), answered at every point of the window rather than at five labelled ones

| | **7 nm window** | **10 nm window** |
|---|---|---|
| salt partition coefficient `K` | 0.905 – 0.937 | 0.559 – 0.954 |
| layer-local `λ_D` at 2 mM | 4.06 – 4.13 nm | 4.02 – 5.25 nm |
| drainage corner (slowest model) | 644 – 742 kHz | 134 – 907 kHz |

**Neither owns an edge anywhere, at any height.** Both are admissible at all 183 grid points.
§4(c) is one-sided by construction — the layer *excludes* salt, so it **protects** the field and the local
screening length is 1.02–1.34× **longer** than bulk — and §4(d) clears 1 kHz by 134× at its worst point in the window.

### P2(h) — the per-load-path force, the one discovered axis that does resolve in `σ`

The solved layer's own secant stiffness sets `C-0015`'s foundation multiplier, which runs **0.823–1.605**
across the two windows — entirely inside `C-0015`'s swept `×[0.25, 4]`, so nothing is extrapolated.

| | best registration | worst registration | 10 pN unzip allowable |
|---|---|---|---|
| 7 nm window | 3.90 – 4.14 pN | 6.58 – 6.90 pN | clear |
| 10 nm window | 4.04 – 5.67 pN | 6.77 – 8.90 pN | clear |

**The unzip exceedance `C-0015` found does not occur anywhere inside the window**, at *either* registration.
`C-0015` entered the band only at `k_f × 0.25`, and the solved layer is never that soft: the softest point in
the whole window is `×0.823`. That is a genuine loosening of a standing constraint and it is a *coupling*
between two claims neither could see alone.

### P2(e) — the usable bias window, under both readings

**Reading A — `C-0012`'s declared window as written.** A bias is usable only up to the point at which the
**free** operating point leaves upstream validity. Pass = some layer model reaches both §3 targets there.

| `L₀` | models passing, by buffer (0.5 / 1 / 2 / 5 / 10 mM) | binding clause | shortfall |
|---|---|---|---|
| 5 nm | 0 / 0 / 0 / 0 / 0 of 6 | **stroke** | 1.04–1.31× short |
| 7 nm | **3** / **3** / 0 / **1** / 0 of 6 | force at 2 and 10 mM | 1.15× short at 2 mM |
| 10 nm | 0 / 0 / 0 / 0 / 0 of 6 | **force** | **1.40× short**, at every buffer |

**Under Reading A the only place both §3 targets are simultaneously reachable is 7 nm at ≤ 1 mM MgCl₂ —
below §3's own stated buffer range, and under three of six layer models.**
That is leaf `A2.2`'s low-screening instruction vindicated a second time and by a different route.

**Reading B — the held operating point.** At the §6 target the tile is held at `L₀ − 3 nm`. The run checks
that point against the same three upstream ranges:

| `L₀` | held gap | held `φ` (densest layer in the window) | gap above `C-0005`'s 1.46 nm | `φ` below `C-0002`'s 0.2 |
|---|---|---|---|---|
| 5 nm | 2.0 nm | 0.0308 | yes, by 1.37× | yes |
| 7 nm | 4.0 nm | 0.0191 | yes, by 2.74× | yes |
| 10 nm | 7.0 nm | 0.0900 | yes, by 4.79× | yes |

**The held operating point is inside every upstream validity range at every height**, and the §6 target is then
reached at 0.063–0.192 V at 7 and 10 nm — five to sixteen times below `CH-0007`'s ~1 V boundary.
The 0.02–0.1 V ceiling is a property of the **unloaded** excursion. This is [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md).

### P2(f) — static stability at the §6 target, which is what Reading B costs

| `L₀` | models statically unstable at the simultaneous target, by buffer (0.5 / 1 / 2 / 5 / 10 mM) |
|---|---|
| 5 nm | 1 / 1 / 1 / 1 / 2 of 6 |
| 7 nm | 3 / 4 / **6** / **6** / **6** of 6 |
| 10 nm | **6 / 6 / 6 / 6** of 6 (10 mM: target not reached at any bias) |

Required output-coupling stiffness at 2 mM: **0 at 5 nm below 0.25 V; 11.2 pN/nm at 7 nm / 0.10 V rising to
85.6–276.6 at 0.25 V; 5.3–16.0 pN/nm at 10 nm / 0.10 V rising to 47.6–71.5 at 0.25 V.**
`C-0012`'s table is reproduced to the digit.

> **Stability wants the thin layer, whose `σ` window is empty. The window and the stroke want the thick one,
> whose operating point is unstable under every layer model at every buffer.** That is `C-0012`'s height
> inversion, now closed against `C-0011`'s window, and it is the shape of the whole answer.

---

## Verify

The five gates, executed as 38 tests in `src/test/kotlin/window/`, each named for the gate it discharges.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `s = σ^(−1/2)` at all 183 points; `Σ = πR₀²σ` is an area times an inverse area, reproduced from the file's own `R₀`; `φ = Nσv₀/h` reproduced at all 183 points; a window width is a **ratio**; `T-14`'s Winkler modulus × 1600 nm² recovers `C-0001`'s published 20.201 pN/nm | **PASS** |
| **2 — limiting cases** | the dead-load stroke is strictly **monotone decreasing** in `σ` and the secant stiffness strictly **increasing**, at all three heights — which is what licenses the one-sided transfer of `T-3`'s single-`σ` stroke clause; a taller layer strokes further at fixed `σ`; salt exclusion → 1 as `φ → 0`; a stiffer foundation lowers the per-path force; `φ(h) → φ(L₀)` at zero compression; an always-true constraint gives the whole grid and an always-false one gives nothing; a hole **throws** | **PASS** |
| **3 — symmetry and invariance** | **the bias for 100 pN of blocking force is identical to twelve digits across all six layer models** at every `(height, buffer)` — the cheap bound, checked not assumed; the intersection is commutative and associative, so the window does not depend on the order the constraints were applied; the interpolated per-path force reproduces every sampled foundation state to `1e−9`; no design point extrapolates `C-0015`'s swept range | **PASS** |
| **4 — numerical convergence** | the `σ` grid is logarithmic with a constant ratio 1.10913 to `1e−6`, so a window edge is located to one grid ratio and no better; halving the grid moves an edge by at most one coarse step; **the result file is byte-identical on two independent re-runs** | **PASS** |
| **5 — cross-check against every claim consumed** | `C-0011`'s windows reproduced from `T-1d`'s design points **and** from its own independently-emitted `strokeWindows`; `C-0005`'s partition coefficients reproduced to `1e−9` at its own labelled `φ`; `C-0004`'s 91 kHz reproduced at its own design point to 2 %; `C-0014`'s minimum tether lengths (10.2 / 28.0 / 61.3 / 93.3 / 204 nm) reproduced from the cable relation; `C-0015`'s 45 attachments as 3 × 15, 0.804 per crossover and exactly zero crossover force read from `T-14`'s file rather than transcribed; `C-0012`'s coupling-stiffness table reproduced to the digit; **the transfer licence** checked at all three shared design points | **PASS, with one exposure — see falsifier 3** |

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a window edge no single constraint owns | **no** | every edge of both surviving windows has exactly one owner; no tie at any height |
| 2 | a non-contiguous admissible set | **no** | all five `P1` constraints have a single contiguous admissible run at all three heights; the `check` never fired |
| 3 | the SCF layer outside `C-0003`'s response bracket | **YES, AT 5 nm** | 10 nm: 5.308 nm against 3.828–6.013 — inside, licensed. 7 nm: 3.129 against 1.537–3.197 — inside, licensed. **5 nm: 1.869 against 0.473–1.530 — outside by 1.22×, NOT licensed.** Every `T-3` verdict at 5 nm is therefore reported as an exposure. It does not move the 5 nm verdict, which is decided by `P1` before any `T-3` number is used, and the direction is favourable (the solved layer strokes *more*), which is the direction in which an error survives longest |
| 4 | a discovered axis not monotone in `σ` | **no** | the per-path force falls monotonically with foundation stiffness, which rises monotonically with `σ`. **But three of the five discovered axes are not functions of `σ` at all** — that is a different finding and is reported as one |
| 5 | the window surviving everything with margin | **no** | 5 nm is empty on §4(a) alone, and under Reading A **12 of 15** `(height, buffer)` pairs fail the §3 clauses outright |

**A gate that fails and is reported is worth more than one that passes.** Falsifier 3 is the one that fired.

---

## File

**Claim:** [`C-0016`](../claims/C-0016-design-window.md).
**Challenge raised:** [`CH-0015`](../challenges/CH-0015-usable-bias-window-is-unloaded.md), against `C-0012`'s
statement that the usable bias window is 0.02–0.1 V.

### The answer to §6 task 2, in one paragraph

**Non-empty in the axes §4(a)–(d) names — `σ ∈ [0.0116, 0.2601] nm⁻²` at 10 nm and `[0.0296, 0.0496]` at 7 nm,
both bounded below by coil overlap and above by the 3 nm stroke, with 5 nm empty by a 13.3× crossing of those
same two constraints. §4(c) and §4(d) bind nothing anywhere. Adding the axes this programme discovered, the
window does not close but it stops being a statement about grafting density: flatness, the usable bias window
and the output-coupling stiffness are not functions of `σ`, and the constraint that decides the programme is
the output coupling — at 7 and 10 nm the §6 operating point is statically unstable under every layer model at
§3's own buffer, so it exists only against a lever supplying 5–277 pN/nm of its own stiffness, and no claim in
this programme supplies what a DNA-origami lever can deliver. `T-2` therefore closes `P1` and states plainly
that it cannot close `P2` in either direction, naming the single missing number.**

### What this hands to the queue

- **`T-16` is promoted to the highest-value open item in the programme.** It is the one number that decides
  whether Gen-1 has a design window at all, and it is cheap.
- **`T-1e`** decides what polymer to buy: 1.1–3.3 kDa in this convention against ~8–9 kDa in the other.
- **`P-5` closes as re-opened:** `L₀/R₀ ≥ 1` admits **all 183 grid points**, including layers at `Σ = 0.063`.
  It is not a weak criterion; it is exactly vacuous against a force-onset height. `Σ ≥ 1` is the only one that bounds.
- **`C-0015`'s unzip exceedance is unreachable inside the window** and that loosening should be recorded.
