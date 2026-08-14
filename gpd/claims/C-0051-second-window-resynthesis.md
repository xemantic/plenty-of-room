# C-0051 — Three iterations, forty artifacts, and **not one window edge moves** — because only one of them is a function of grafting density at all; what did move is off the `(σ, L₀)` plane entirely, and the composed correction nobody had composed makes `C-0033`'s margin rise vanish: the fold tangent at `C-0018`'s own fold is **−8.40 to −11.06 pN/nm** once the coupling the programme actually has is carried beside the collar

| | |
|---|---|
| **Task** | [`T-118`](../tasks/T-118-window-resynthesis-two.md) |
| **Leaf** | `A2.1`, re-checking the acceptance strings of `A2.2`, `A1.1`, `A8.2` and `A7.4` |
| **Verification type** | **logical** (constraint intersection over a common grid, re-run) **+ in-silico** (every upstream number read from the emitting study's own result file **at run time**, keyed on every dimension its sweep varied; `C-0030`'s element **re-run as a library** rather than tabulated) |
| **Verdict** | **`C-0016`'s `P1` STANDS, unmoved: 0 of 6 window edges move by even one grid step and no edge changes owner. `C-0027`'s `P2` STANDS for the AFFINE MANDATE and FAILS for the realised coupling — read `C-0032`'s 1.0000–1.0019. One axis LEAVES the acceptance stack, one replaces it, two NEW `σ`-resolved axes appear and neither binds. The deliverable is no longer a window.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, and almost nothing here is re-derived: every number is a transfer, and every transfer is checked against the file it came from. |
| **Provenance** | `gpd/results/T-118-window-resynthesis-two.json`, produced by `window.SecondResynthesisStudyKt`; **6 windows, 6 edge reproductions, 8 candidate-axis records, 549 stroke-ceiling records, 1 licence check, 6 composed folds, 12 classified axes, 2 buffer rows, 23 upstream reproductions, 5 falsifiers, a 9-entry ledger**; **23 gate-named tests in `window/SecondResynthesisTest`**, **1453 in the suite, 0 failures** (`BUILD SUCCESSFUL`, on `tools/snapshot.sh`'s own snapshot with three concurrent agents' mid-TDD test files and one mid-TDD study main dropped by name, and one sibling **main** source — `structure/OrigamiGrillage.kt`, which `window` depends on transitively — **restored to `HEAD`** rather than deleted, because `--drop <pkg>` is the wrong granularity for it); the result file re-run through the study runner and reported *"no result file changed"*, and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous MgCl₂ at **0.5 and 2 mM, neither adopted**; 40 × 40 nm tile; linear PEG; layer heights 5 / 7 / 10 nm on `T-1d`'s 61-point logarithmic `σ` grid, ratio **1.10913** |
| **Consumes** | [`C-0011`](C-0011-scf-density-profile.md) (the layer and its grid), [`C-0027`](C-0027-window-resynthesis.md) (the windows it re-runs), [`C-0031`](C-0031-bracketed-root-repair.md), [`C-0032`](C-0032-softening-coupling-stability.md), [`C-0033`](C-0033-collar-on-the-equilibrium-path.md), [`C-0036`](C-0036-concentrated-crossover.md), [`C-0030`](C-0030-coupled-standoff-joint.md) (**re-run as a library**), [`C-0041`](C-0041-flexure-array-packing.md), [`C-0049`](C-0049-compliance-ceiling-stroke.md), [`C-0050`](C-0050-desired-stroke-reach.md), and the challenges `CH-0043` … `CH-0062` in their final form |
| **Raises** | [`CH-0063`](../challenges/CH-0063-the-collar-was-carried-onto-a-load-line-the-device-does-not-have.md) against `C-0033`, and [`CH-0064`](../challenges/CH-0064-the-validity-ceiling-is-read-on-a-layer-four-times-denser-than-the-window.md) against `C-0050` |

---

## THE HEIGHT CONVENTION — read this before any number below

> **Every layer height here is a FORCE-ONSET height: `L₀` is where the layer carries 1.0 pN over the
> 40 × 40 nm tile** (`C-0011`). The first-moment thickness `2⟨z⟩` of the same layer is 1.71–2.16× smaller,
> and a bench reading this window in the wrong convention would order 8–9 kDa PEG where it needs 1.1–3.3 kDa.

---

## The claim, in one line

**Twenty claims and twenty challenges ran since `C-0027`, and the window is unchanged to the last digit —
not because it survived them, but because **exactly one of the twenty carries a quantity that is a function
of `σ` at all**, and `C-0036` says itself that it reaches the design only through `C-0018`'s bias ceiling.
`C-0050` did produce two genuinely new `σ`-resolved constraints — the kinematic and validity stroke ceilings —
and evaluated on the layer the window is actually drawn on neither binds, by 1.71–3.11×. What moved is
entirely off the plane: an axis **left** the acceptance stack (`C-0049` withdraws `C-0023`'s 40 pN/nm tangent
ceiling, which is `1.2 ×` a placement mandate and carries that stroke inside it), a path **count** replaced it,
and the pull-in margin was moved in opposite directions by two claims of the same iteration that never met.
Composed here for the first time — exactly, because at `C-0018`'s own fold the baseline tangent vanishes by
construction — the collar's `+2.61` to `+4.99 pN/nm` is overwhelmed by the realised coupling's `−9.21` to
`−10.29`, giving **−8.40 to −11.06 pN/nm at 6 of 6 models**. The collar recovers 27–49 % of what the element
costs and nothing more.**

---

## `P1` — the window, re-run: **zero edges move**

Re-intersected on the **current** result files, i.e. after `C-0031`'s solver repair re-emitted nine of them.

| `L₀` | `C-0016` baseline | `T-118` re-run | `C-0027` published | `T-118` re-run | **grid steps** |
|---|---|---|---|---|---|
| **5 nm** | EMPTY | **EMPTY** | EMPTY | **EMPTY** | — |
| **7 nm** | `[0.029552, 0.049602]` | **identical** | `[0.029552, 0.049602]`, 1.678× | **`[0.029552, 0.049602]`, 1.678×** | **0** |
| **10 nm** | `[0.011634, 0.260150]` | **identical** | `[0.011634, 0.288540]`, 24.80× | **`[0.011634, 0.288540]`, 24.80×** | **0** |

**Worst edge departure over the six windows: `0.0` — not "small", zero.**
The intersection is an **index** comparison on the grid, so it is immune to `CH-0043`'s rounding concern by
construction; that is why the re-run is a stronger check than the byte diff `C-0031` performed.

**No edge changes owner.** Coil overlap `Σ = πR₀²σ ≥ 1` still owns every lower edge and §3's 3 nm stroke,
read as `3.0 + d` delivered, still owns every upper one — **through nine claims and thirty challenges.**

> **`C-0031`'s finding is confirmed by re-intersection rather than by a diff of the file it produced**, which
> is the distinction `CH-0043` was raised to make.

---

## `P2` — the axes, and why the window could not have moved

An axis is `σ`-resolved iff its constraint quantity varies across the grid at fixed height. Where such a
quantity exists the variation ratio is **computed**.

| axis | source | level | can it narrow? |
|---|---|---|---|
| **(a)** coil overlap | `C-0011` | `σ`-resolved | **yes** — owns every lower edge, unchanged |
| **(a)** compliance stroke | `C-0011`/§3 | `σ`-resolved | **yes** — owns every upper edge; `C-0050` gives it a *mechanism* without moving it |
| **(n)** **kinematic stroke ceiling** `L₀ − Nσv₀` | `C-0050` | **`σ`-resolved, NEW** | **yes — and it does not.** 2.31–3.11× clear |
| **(o)** **concentrated crossover at rest** `φ(L₀) ≤ φ_c` | `C-0002`/`C-0036`/`C-0050` | **`σ`-resolved, NEW** | **yes — and it does not.** 1.71–2.87× clear |
| **(f)** output-coupling stiffness | `C-0017`/`C-0032`/`C-0049` | height- and buffer-level | no — **but its verdict MOVED** |
| **(e)** usable bias / pull-in | `C-0018`/`C-0032`/`C-0033`/`C-0036` | height- and buffer-level | no — **and it moved on three channels** |
| **(p)** declared compliance ceiling, 40 pN/nm | `C-0023` → `C-0049` | **WITHDRAWN** | **the axis LEAVES the acceptance stack** |
| **(q)** per-path allowable as a stiffness, `n·a/s` | `C-0006`/`CH-0029`/`C-0049` | topological — a **COUNT**, tightening as `1/s` | no — and it is what replaced (p) |
| **(r)** coupling plan view | `C-0041`/`C-0047`/`C-0046` | topological / plan geometry | no |
| **(s)** hinge inventory | `C-0040`/`CH-0054`/`CH-0062` | topological — a **LATTICE COUNT** | no |
| **(t)** reach of §3's desired clause | `C-0050` | **a SPECIFICATION question — a layer HEIGHT** | no — it closes a **clause** |
| **(u)** numerics provenance | `C-0031`/`CH-0043` | methodological | no |

> **Ten of the twelve axes cannot narrow a `(σ, L₀)` window, and the two that can are the two that always
> did.** `C-0016`'s central lesson is now confirmed a third time and from the other side: *a constraint that
> cannot narrow is invisible to an intersection* — **so a window that does not move is not evidence that it
> survived anything.**

### The two new `σ`-resolved constraints, evaluated where they belong

`C-0050` derives both on `C-0003`'s six trial-function models at one grafting density per height. The window
is drawn on `C-0011`'s **solved SCF** layer, and `φ = Nσv₀/h` identically — `T-1d` emits it — so both ceilings
are available at all 61 grid points for one multiplication:

| `L₀` | `φ` across the window | kinematic margin over §3's 3 nm | crossover margin, `φ_c` = 0.2 / 0.141 / 0.49 |
|---|---|---|---|
| **7 nm** | 0.0064 – 0.0109 | **2.31×** | 2.21× / 2.15× / 2.28× |
| **10 nm** | 0.0052 – 0.0686 | **3.11×** | 2.19× / **1.71×** / 2.87× |

**Neither binds at any of the 61 points of either surviving window, under any of the three readings of the
crossover `C-0036` leaves standing.** Declared falsifier 2 did not fire.

### The axis that left, and the one that replaced it

`C-0049` reads `C-0023`'s 40 pN/nm as `1.2 × (100 pN / 3 nm)` — **exactly** — so it is a declared linearity
tolerance on the *placement discharge* and inherits the placement's stroke. The acceptance stack `C-0017` and
`C-0018` actually define contains **no upper bound on a coupling tangent at all**: one equality on a secant,
one floor on a tangent. What binds beyond the working point is the per-path unzip allowable, and that is a
bound on a **force**, so as a stiffness it is `n·a/s` and tightens as `1/s`:

| | 3 nm | 10 nm |
|---|---|---|
| 45 paths (`C-0015`) | 150 pN/nm | 45 pN/nm |
| **15 paths (`C-0041`'s buildable count)** | **50 pN/nm** | **15 pN/nm** |

At `C-0041`'s 15 the mandate clears at §3's acceptable stroke by 1.50× and is refused at its desired one by
2.22×. **A count, not a grafting density** — invisible to the intersection, and it is now the only ceiling in
the stack.

> `C-0027` recorded one axis leaving the window. This is the second, and unlike the first it was **replaced**
> rather than discharged — which an intersection also cannot see.

---

## `P3` — three corrections, one margin, and nobody had composed them

`C-0033` (`T-60`) and `C-0032` (`T-76`) both moved `C-0018`'s 10 nm / 2 mM pull-in margin, in **opposite**
directions, in the same iteration, and **neither carries the other**. `CLAUDE.md` records exactly this trap
from iteration 4; this is its second instance, and here the two are *not* the same size.

At `C-0018`'s own fold the baseline coupled tangent **vanishes by construction** — `k_c + k_brush + k_es = 0`
is what located it — so every perturbation enters as an **increment** and the composition is exact:

| model | fold stroke | `k_c(s_fold)` | **collar** (`C-0033`) | **fluctuation** (`C-0019`) | **softening** (`C-0032`) | **total** |
|---|---|---|---|---|---|---|
| alexander-box(two-body) | 3.410 nm | 24.13 | **+2.605** | −2.464 | **−9.207** | **−8.717** |
| alexander-box(virial) | 4.078 | 23.09 | **+4.993** | −5.807 | −10.248 | **−11.062** |
| alexander-box(des-Cloizeaux) | 3.657 | 23.64 | +3.942 | −4.354 | −9.697 | **−10.108** |
| strong-stretching(two-body) | 3.578 | 23.78 | +2.605 | −1.449 | −9.554 | **−8.398** |
| strong-stretching(virial) | 4.125 | 23.05 | +3.826 | −3.179 | −10.288 | **−9.641** |
| strong-stretching(des-Cloizeaux) | 3.952 | 23.21 | +3.552 | −2.903 | −10.120 | **−9.472** |

> **Negative at 6 of 6 models, and it does not straddle zero** — declared falsifier 3 did not fire.
> A positive tangent at the old fold means the path still ascends there and the fold moves **deeper**
> (`C-0033`'s finding); a negative one means it moves **shallower**, which is what `C-0032` measured directly.
> **`C-0033`'s collar recovers 27–49 % of what the realised element costs, and no more.**

**So `C-0033`'s 1.021–1.028 is the margin of the AFFINE MANDATE, and the affine mandate is not the device the
programme has.** `C-0030`'s coupled-standoff flexure is the only mounting that survives `C-0023`'s ceiling
(`C-0032`: the adverse mounting is 1.06–1.53× past it at **0 of 8** standoff lengths) and it strain-softens.
That is [`CH-0063`](../challenges/CH-0063-the-collar-was-carried-onto-a-load-line-the-device-does-not-have.md).

> **The standing 10 nm / 2 mM statement is `C-0032`'s `1.0000 – 1.0019`: the device sits on its fold.**
> This gives the **direction** exactly and the relocated fold not at all — which is stated as a limit.

### `C-0049`'s re-reading, carried

On the stability-**floor** side `C-0049` runs the other way: read over the strokes the device traverses,
`[0, s*]`, `C-0030`'s element is **25.227 pN/nm** and clears **4 of `C-0017`'s 6** model floors at 2 mM,
against **0 of 6** for `CH-0042`'s interior 22.875. `C-0032`'s `Q2` becomes model-dependent rather than
universal — and **`Q3`, the fold, is untouched**, which is why the bias margin above is the binding statement.

---

## `P4` — `C-0050`'s licence at the window's own upper edge, and it departs by 4.15×

`CLAUDE.md`: *an upstream bracket upheld at one design point is not upheld at all of them.*
`C-0016`'s falsifier 3 fired at 5 nm and was carried forward by `C-0027`. Nobody checked the **10 nm upper
edge** — which is exactly where `C-0050` reads its bound 3.

| at `σ = 0.28854 nm⁻²`, `L₀ = 10 nm` | resting `φ` | validity ceiling exists? |
|---|---|---|
| **`C-0011`'s solved SCF layer** — what the window is drawn on | **0.0623 – 0.0686** | **yes**, 6.57–6.89 nm |
| `C-0003`'s six trial-function models — what `C-0050` reads | **0.1505 – 0.2845** | **NO at 2 of 6** |
| | **up to 4.15×** | a qualitative disagreement |

> **`C-0050`'s verdict is untouched and its reason is strengthened**: its bound 2 is *kinematic*,
> `L₀ − Nσv₀`, needs no crossover at all, and is short of §3's desired stroke by 1.02× on its own.
> But its bound-3 sweep over `C-0027`'s window is read on a layer up to **4.15× denser** than that window's,
> and its validity clause says the transfer inherits `C-0003`'s range *"in full"*. That is
> [`CH-0064`](../challenges/CH-0064-the-validity-ceiling-is-read-on-a-layer-four-times-denser-than-the-window.md),
> and it is the **sixth** instance of this project's own discipline: a stiffness with a compression, a variance
> with a bandwidth, a rupture force with a loading rate, `k_es` with a gap, a flatness count with a load case —
> and now **a volume-fraction ceiling with a layer model.**

At 7 nm the check cannot be made: `T-108` never sampled that window's upper edge. It is reported as
**unchecked**, not asserted on a substitute.

---

## `P5` — what the programme's answer to §6 task 2 now is

| branch | verdict |
|---|---|
| **a non-empty region satisfying §4(a)–(d)** | **YES, unchanged.** 10 nm: `σ ∈ [0.0116, 0.2885] nm⁻²`, 24.8× wide. 7 nm: `[0.0296, 0.0496]`, 1.68× wide |
| **the binding constraints** | **lower: coil overlap `Σ ≥ 1`. Upper: §3's 3 nm stroke as `3.0 + d`.** Unchanged owners at both heights |
| **a proof of emptiness** | **YES at 5 nm**, `C-0016`'s 13.3× crossing, unmoved |
| **the axes §4 does not name** | **the window is no longer where they live.** Ten of twelve cannot narrow it; three were *added* in iterations 5–7 as counts and plan layouts, one **left**, and one is a specification question |
| **§3's acceptable clause — 3 nm at 100 pN** | **DELIVERED**, and delivered at `C-0041`'s buildable 15 paths with 1.50× on the per-path allowable |
| **§3's *desired* clause — ~10 nm** | **UNREACHABLE ON §3's OWN STACK, kinematically.** `s = L₀ − h < L₀ ≤ 10 nm`. Ceilings **9.790 / 8.959 / 7.424 nm**, none containing a coupling |
| **what the deliverable now is** | **a HEIGHT plus five specification questions** — `T-63` (buffer), `T-95` (superstructure), `T-102` (tile area), `T-112` (which device the desired clause names), **`T-115` (a 17–26 nm layer)** — and **only `T-115` can buy the desired stroke** |

> **The window is still correct, still non-empty and still owned by the same two constraints. It is simply no
> longer where the programme's remaining uncertainty lives.** Of the four things that moved in iterations 5–7,
> three cannot be drawn on a `(σ, L₀)` plane at all, and the one that decides §6 is a **kinematic identity
> about a coordinate.**

---

## `P6` — the buffer, reported at both and adopted at neither

| 10 nm | pull-in folds | bias margin | stability margin, corrected |
|---|---|---|---|
| **0.5 mM** | **0 of 6** | — (no fold) | **2.163 – 9.867×** |
| **2 mM** (§3's own) | **6 of 6** | **1.0071 – 1.0317** (affine); **1.0000 – 1.0019** realised (`C-0032`) | **1.231 – 1.528×** |

> **`T-118` is the seventh independent route to 0.5 mM**, and it arrives from a new direction: the composed
> fold tangent above is negative **only where a fold exists**, and at 0.5 mM none does at the 10 nm layer under
> any of the six models — so the whole of `P3`'s degradation is vacuous there.
> **§3 names 2 mM and does not name 0.5. Neither is adopted here. It is `T-63`, a specification question.**

---

## The five verification gates

Executed as **23 gate-named tests** in `src/test/kotlin/window/SecondResynthesisTest.kt`.

- **Gate 1** — a stroke ceiling is a length and is homogeneous of degree one in the layer at fixed `φ`;
  `perPathSecantCeiling(a, n, s)·s = n·a` identically at 4 strokes × 4 counts; the declared ceiling is
  `1.2 ×` the mandate at three `(force, stroke)` pairs; unphysical arguments throw at seven entry points.
- **Gate 2** — a vanishing layer occupies its whole height and a melt none of it; the validity ceiling **tends
  to the kinematic one** as `φ_c → 1` and **vanishes** at `φ = φ_c`; the three-channel increment is **exactly
  zero** when every channel is off; the identity correction set still reproduces `C-0016`'s four edges.
- **Gate 3** — the three channels are additive and each carries its own sign; the intersection is
  **order-independent** under the full correction set; the polymer volume per unit area `φh` is **conserved**
  under compression at four strokes, which is what makes both ceilings statements about one number.
- **Gate 4** — the grid is logarithmic with ratio 1.10913 at all 60 intervals; a movement below one grid ratio
  is **0 steps** and is reported as sub-grid; the `σ`-resolved candidates are evaluated at **all 61** points ×
  3 crossover readings × 3 heights; **the result file is byte-for-byte identical on two independent runs.**
- **Gate 5** — **23 reproductions, worst departure `4.0e−4`**: `C-0027`'s eight non-empty window edges to
  **exactly 0**, `C-0030`'s span 31.821 and tangent 25.227 from its **own library**, `C-0017`'s mandate,
  `C-0023`/`C-0049`'s ceiling at both §3 clauses, `C-0049`'s per-path ceilings at 45 and 15 paths, `C-0050`'s
  9.78969 and 8.95887 from `T-108`'s own file, `C-0041`'s 15 from **its own design table**, `C-0033`'s
  2.604/4.994 from `T-60`'s own file, `C-0018`'s 1.00708/1.03170; and **a key that does not identify a unique
  upstream record throws**, at two accessors.

### The declared falsifiers, and what happened

| # | fired? | outcome |
|---|---|---|
| 1 — a window edge moving on the repaired solver | **no** | 0 of 6 windows, 0 grid steps, no owner change |
| 2 — a `σ`-resolved constraint from iterations 5–7 binding | **no** | neither of the two new ones binds, by 1.71–3.11× |
| 3 — the composed fold tangent straddling zero | **no** | −8.40 to −11.06 pN/nm, negative at 6 of 6 |
| 4 — an upstream number failing to reproduce | **no** | worst departure `4.0e−4`, over 23 |
| 5 — `C-0050`'s ceiling verdicts transferring unchanged | **the check was NOT idle** | they do not transfer: 4.15× in `φ`, and a disagreement about whether the ceiling exists |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and almost nothing is re-derived.** Every number is a transfer, and
  every transfer is checked against the file it came from.
- **THE HEIGHT CONVENTION IS FORCE-ONSET**, at a defining load of 1 pN over the tile. `T-1e` has not run.
- **Every window edge is a grid point** located to **1.10913×** and no better. The re-intersection is an
  **index** comparison and is exact; the reproductions are not, and their tolerances match `CH-0043`'s finding
  that this tree rounds to nine digits where a solved height is determined to about six.
- **The three-channel fold increment is EXACT at `C-0018`'s own fold** — the baseline tangent vanishes there
  by construction — but it gives the **direction** the fold moves and **not the new margin.** Relocating the
  fold needs `C-0018`'s path search re-run with `C-0030`'s nonlinear law **and** `C-0033`'s solved collar
  together, which no study has done. That is this claim's largest open item.
- **The softening channel is read at 45 paths**, `C-0015`'s count, which `C-0041` shows does not pack. At
  `C-0041`'s buildable 15 the assembled tangent moves ~1 % (`C-0041`: 25.23 → 25.49), so the **sign** is
  unaffected; 45 is carried because that is the state `C-0032` and `C-0033` were both evaluated at.
- **`C-0032`'s SMALL-DEFLECTION exposure travels unchanged**: its tangent minimum sits at the edge of small
  deflection and a large-deflection solve would move it in an unknown direction.
- **Mean-field electrostatics, inherited whole.** `C-0005`: 123–214 % of the leading term across the whole
  5–10 nm range. **Every margin here is NOT EXCLUDED, never established.**
- **`C-0036` replaces `C-0002`'s `φ = 0.2` with a one-parameter family and `CH-0049` disputes the 0.2
  independently.** All three readings are carried and the window's verdict is the same under every one — which
  is computed, not assumed.
- **The crossover licence is checked only where `T-108` sampled the window's own upper edge.** It did not
  sample the **7 nm** edge, which is reported as **unchecked** rather than asserted on a substitute.
- **`C-0016`'s and `C-0027`'s own validity ranges travel unchanged**, including the 1.22× exposure of the
  solved layer against `C-0003`'s bracket at 5 nm.
- **The layer is neutral linear PEG.** §3 also permits PEO and a PS→PEG block copolymer, for which no osmotic
  equation of state was ever consumed in this programme.

## Numbers that are CITED rather than DERIVED

| quantity | value | unit | source | provenance |
|---|---|---|---|---|
| mandated coupling stiffness | 33.3333 | pN/nm | `C-0017` from §3 alone | **CITED** |
| acceptable / desired stroke | 3.0 / 10.0 | nm | §3 | **CITED** — the desired one quoted, never adopted |
| per-path unzip allowable | 10 | pN | `C-0006`/`CH-0029` | **CITED, MEASURED**, loading-rate dependent |
| concentrated crossover as every upstream claim used it | 0.2 | — | `C-0002` | **CITED** — `CH-0049` disputes it, `C-0036` replaces it |
| standoff length of the realised element | 8 | nm | `C-0030` | **CITED** |
| `C-0019` `k_brush` multiplier at 10 nm | 0.90584 | — | `C-0019` via `T-60`'s own field | **CITED** |
| declared ceiling factor | 1.2 | — | `C-0049` | **DERIVED here** as `40/(100/3)` |
| buildable path count | 15 | — | `C-0041` | **DERIVED here** from `T-96`'s own design table |
| grid ratio | 1.10913 | — | `T-1d`'s own sweep | **DERIVED here** |

Everything else — every window edge, every edge movement, both stroke ceilings at all 549 `(height, σ,
reading)` states, every licence departure, all three channels at all six folds, the axis classification and
its computed `σ` spans, and the buffer comparison — is **derived here from the eight consumed result files**,
with `C-0030`'s pipeline **re-run rather than tabulated**.

## Challenges

**Raises [`CH-0063`](../challenges/CH-0063-the-collar-was-carried-onto-a-load-line-the-device-does-not-have.md)**
against `C-0033`'s `P4`, and
**[`CH-0064`](../challenges/CH-0064-the-validity-ceiling-is-read-on-a-layer-four-times-denser-than-the-window.md)**
against `C-0050`'s bound 3. Neither moves a number in the claim it is raised against; both move its scope.

**None stands against this claim.** The three ways it would fail:

1. **A re-solve of `C-0018`'s fold under both `C-0030`'s law and `C-0033`'s collar putting the relocated fold
   deeper than 3 nm at every model.** The composed tangent here is negative by 8.4–11.1 pN/nm against a collar
   worth 2.6–5.0, so it would take a sign reversal, not a refinement.
2. **A coupling element that packs, places, clears the per-path allowable and strain-STIFFENS.** `C-0035`
   closes the mounting sense as a determination and `C-0032` finds the adverse mounting past `C-0023`'s
   ceiling at 0 of 8 lengths, so no such element is currently known.
3. **NDI specifying a layer taller than 10 nm**, in which case `C-0050`'s bound 1 moves, §3's desired clause
   re-opens, and every window edge in this claim has to be recomputed at a height nothing upstream has
   evaluated. **That is `T-115`, and it is the single most consequential open question in the programme.**
