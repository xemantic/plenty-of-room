# C-0014 — Lateral confinement of the Gen-1 tile: the binding constraint is anisotropy, and it is topological

| | |
|---|---|
| **Task** | [`T-12`](../tasks/T-12-lateral-confinement.md) |
| **Leaves** | `A1.2` (the 3.0 nm bound), with `A1.1` as its bound table, and **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | in-silico (closed-form element mechanics assembled into a four-coordinate anchor stiffness) **+ logical** (a convexity theorem that decides the topology before any number is computed) |
| **Verdict** | **PASS.** Two schemes meet `k_lat ≥ 0.4602 pN/nm` *and* the yaw budget *and* the declared 10 % stroke budget simultaneously. The obvious scheme — a strut standing under the tile — **fails by 40–160×**, and it fails for a reason that generalises. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** No scheme here has been built and none is proposed as a sequence design. |
| **Provenance** | `gpd/results/T-12-lateral-confinement.json`, produced by `anchoring.LateralConfinementStudyKt`; 52 gate-named `anchoring` tests green |
| **Conditions** | T = 300 K, aqueous buffer 2/5/10 mM MgCl₂, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile; 5/7/10 nm layer; §3 target force 100 pN, stroke ≥ 3 nm |
| **Consumes** | [`C-0010`](C-0010-tile-positional-variance.md) (the requirement and the zero), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the elements and the allowables), [`C-0009`](C-0009-discrete-lattice-tile.md) (the concentration factor, the 64 attachments), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the layer), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the field), [`C-0004`](C-0004-poroelastic-drainage.md) (the drag, for the no-scheme baseline) |
| **Raises** | [`CH-0013`](../challenges/CH-0013-entropic-tether-is-not-zero.md) against `C-0010` |
| **Challenged by** | [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md), on the **7.6× concentration factor** this claim itself flagged as a stand-in. See the banner below |

> ⚠️ **The `7.6×` this claim applies throughout is corrected by [`CH-0021`](../challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md) (2026-08-13), from the in-plane shear-lag solve [`C-0020`](C-0020-in-plane-shear-lag.md) (`T-15`) — the task this claim's own validity range queued.**
>
> **No verdict here moves**, and the anisotropy theorem, the cable term, the `r²` yaw cancellation, the
> over-stiffening result and the `S6`/`S7` ceilings are all untouched. What moves is `n` in
> **`L_min = δ√(Sn/(2A))`**, and it moves **in both directions**:
> `C-0009`'s factor is a peak over an **equal share** of a reaction the tile collected from its foundation,
> applied here as a peak over the **applied** force — two quantities differing by the ~9.3 paths on an
> `ℓ`-contour — and laterally the tile collects nothing at all, because the layer's lateral stiffness is
> exactly zero.
> Solved: **`n = 1.00` for a tether aligned with the helices** (`L_min` = 10.0 nm at 3 nm and **33.5 nm at
> 10 nm**, 2.79× shorter, so the ~227 nm assembly is withdrawn), **`n = 3.82` across them**, and
> **`n = 11.75` at the worst of 7200 placements** — where `L_min` is **115.9 nm, worse than the 93.3 nm this
> stand-in produced**. The stand-in was *not* conservative for a misaligned tether.
> And the footprint constraint below **changes currency rather than disappearing**: at the minimum tether
> length the normal preload is `n_t A √(2A/S)`, **independent of the stroke**, i.e. **54.9 pN — 55 % of the §3
> target force** — which this claim's `L_min` formula does not contain.
> Do not consume the `× C-0009's 7.6` column, or the `L_min` table, without reading `CH-0021`.

---

## Claim, in one line

**A scheme exists, and the thing that decides it is not the material, the anchor count or the stiffness — it is the anchor's *orientation*: every load path that crosses the polymer layer must accommodate the stroke axially, which costs at least as much normal stiffness as it buys laterally (a convexity theorem) and costs 160× more than that for a rigid duplex strut; the same duplex laid *in* the surface has the ratio the other way up, by four orders of magnitude.**

---

## The requirement, in three readings, with the one used declared

Re-derived from `k_BT` alone, not accepted from leaf `A1.1`'s table.

| reading | requirement | what it is |
|---|---|---|
| **per-coordinate (DECLARED ACCEPTANCE)** | **`k_lat ≥ 0.460216 pN/nm`** | leaf `A1.1`'s own bound table, and the reading `C-0010` handed down |
| radial | 0.920433 pN/nm | `√(σ_x² + σ_y²) ≤ 3.0 nm` — exactly 2× |
| worst-point combined | 1.380649 pN/nm | the corner's in-plane RMS, translation *and* yaw — exactly 3× |

**A design sitting exactly on leaf `A1.1`'s bound in both translations and in yaw puts `√3 × 3.0 = 5.196 nm` on the tile's corner.**
That is [`CH-0009`](../challenges/CH-0009-worst-point-is-not-the-centre.md)'s finding restated in the plane, and it is why the reading is declared in Formulate rather than chosen afterwards.

## The yaw budget, in the same currency

The symmetry argument that kills the layer's lateral stiffness kills its yaw stiffness by the same step (`C-0010`).
Yaw is budgeted here as **the in-plane displacement of the tile's worst material point — the corner, at `r_c = L/√2 = 28.2843 nm` — held to the same 3.0 nm**:

&nbsp;&nbsp;&nbsp;&nbsp;`ψ_RMS ≤ 3.0/28.2843 = 0.106066 rad` &nbsp;→&nbsp; **`k_yaw ≥ k_BT r_c²/σ² = 368.173 pN·nm/rad`.**

The footprint-RMS-radius reading (`r = √(L²/6) = 16.3299 nm` → 122.724 pN·nm/rad) is **3.0× weaker** and is reported, not used.

### An exact placement rule, and it is the useful part

For anchors of in-plane stiffness `k_i` at radius `r_a`, `k_yaw = Σ k_i r_a²` and `k_lat = Σ k_i`, while the requirements are `k_BT/σ²` and `k_BT r_b²/σ²`. The two margins therefore stand in the ratio

&nbsp;&nbsp;&nbsp;&nbsp;**`(yaw margin)/(translation margin) = (r_a/r_b)², exactly — independent of the stiffness, of the anchor count, and of the radius itself.**

| arrangement | ratio | consequence |
|---|---|---|
| 4 anchors **at the corners**, budget at the corner | **1.000** | yaw and translation are *identically the same condition* |
| 4 anchors at the **edge midpoints** | 0.500 | **yaw becomes binding**, by exactly 2× |
| 4 anchors at half radius | 0.250 | yaw binding by 4× |
| 4 anchors on a frame at 40 nm | 2.000 | translation binding |
| **1 anchor at the centre** | **0** | pins translation completely and leaves the tile **free to rotate** |

Asserted as a gate-3 test over a range of radii, because the claim is that the radius cancels *exactly*.

---

## The theorem that decides the answer, and it costs nothing to run

> **The anisotropy theorem.** A flexible link from the substrate to the tile spans the gap `h` with a force-extension law `f(x)`, `f(0) = 0`, convex (every polymer and every duplex strain-stiffens). Its **transverse** stiffness is the tension over the span, `f(h)/h`; its **axial** stiffness is the tangent, `f′(h)`. Convexity with `f(0) = 0` gives `f(h) = ∫₀ʰ f′ ≤ h f′(h)`, so
>
> &nbsp;&nbsp;&nbsp;&nbsp;**`k_lat/k_norm = secant/tangent ≤ 1`, with equality only for a linear spring.**

Checked, not asserted, against the freely-jointed chain it is applied to:

| tension [pN] | 1e-4 | 0.01 | 0.1 | 1.0 | 5.0 | 20.0 |
|---|---|---|---|---|---|---|
| extension [nm] | 0.001 | 0.068 | 0.676 | 6.65 | 24.73 | 36.06 |
| **secant/tangent** | **1.0000** | **1.0000** | 0.9997 | 0.9668 | 0.5338 | **0.1094** |

**Two consequences, and the second is the answer to the task:**

1. **No through-layer load path can buy lateral stiffness more cheaply than one-for-one in normal stiffness** — but that floor is *affordable*: 0.4602 pN/nm against `C-0003`'s 16.6–26.1 pN/nm layer secant is 1.8–2.8 % of the stroke. The theorem does not kill the through-layer topology; it prices it.
2. **A rigid rod is not covered by the theorem and does far worse than it.** Its lateral stiffness is *bending*, `cEI/L³`, against an axial `S/L`, so the ratio is `cEI/(S L²)` — **0.0063** for a clamped-pinned duplex at 10 nm and 0.0251 clamped-guided, i.e. **40–160× below the floor a flexible tether reaches**. The escape is topological: a load path that lies **in** the surface does not have to accommodate the stroke axially, and the theorem does not bind it.

---

## Scheme by scheme

Every scheme is evaluated at all three §3 layer heights, on both duplex bending rigidities
(CanDo's `EI = 230 pN·nm²` and the Mg²⁺-measured `L_p = 40 nm`, `EI = 165.7`), and under **both** plausible end conditions —
clamped-pinned and clamped-guided differ by exactly 4 in transverse stiffness and by exactly 4 in buckling load,
and an origami-to-substrate joint is not obviously either.

### `S1` — vertical duplex struts. **FAIL, three times over**

At the 10 nm layer, four struts, CanDo `EI`:

| | clamped-pinned | clamped-guided |
|---|---|---|
| `k_lat` [pN/nm] | 2.76 | 11.04 |
| `k_norm` [pN/nm] | **440** | **440** |
| anisotropy | **0.0063** | 0.0251 |
| **stroke lost** | **96.4 %** | **96.4 %** |
| Euler load per strut | **5.67 pN** | 22.70 pN |
| load per strut at 100 pN | 25 pN | 25 pN |

**A single 10 nm strut does clear the lateral bound at 0.69 pN/nm — `C-0010`'s own number, reproduced from `EI` and `L` as a gate-5 test — and it is still unusable**, because:

1. its axial stiffness is `S/L = 110 pN/nm`, **twice the whole layer's tangent stiffness under the tile**, so it takes 87 % of the stroke on its own;
2. it carries the actuation load in **compression** and buckles at 5.67–22.7 pN against the 25–100 pN it would have to carry;
3. **a column at its Euler load has exactly zero lateral stiffness** — `k(P) = k₀(1 − P/P_c)` — so the actuation load destroys the very stiffness the strut was there to provide. The scheme is destabilised by its own duty.

Stiffening the strut does not rescue it. A four-helix bundle (`EI = 8877 pN·nm²` by the parallel-axis theorem, 39× a single duplex) does not buckle and reaches `k_lat = 426 pN/nm`, but its axial stiffness is `4S/L = 440 pN/nm` per post and it takes **99.1 %** of the stroke.

### `S3` — entropic (ssDNA) tethers through the layer. **PASS, at the theorem's floor**

The equality case: a chain at low extension is a *linear* spring, so `k_lat = k_norm` exactly and the through-layer cost is the theorem's minimum.

Eight tethers, 10 nm layer, Kuhn length 2.10 nm (Chen et al. 2012, zero-force, the applicable end):

| quantity | value |
|---|---|
| **longest admissible contour** | **103.4 nm = 159 nt** (below which the bound is met) |
| evaluated design point (half that) | 51.7 nm = 80 nt |
| `k_lat` | **0.937 pN/nm**, 2.03× the bound |
| `k_yaw` | 2.03× its bound (the exact corner cancellation) |
| `k_norm` | 0.981 pN/nm — **anisotropy 0.955**, the floor |
| tension per tether | 1.17 pN; **total preload 9.37 pN, 9.4 % of the §3 target force** |
| **stroke lost** | **5.6 %** |
| peak path force with `C-0009`'s 7.6× | **8.90 pN — below even the 10 pN unzip allowable** |

Over the full ssDNA parameter bracket (`b = 1.34–2.84 nm`) and 4 or 8 tethers, the stroke cost runs **1.4–8.5 %** and every case passes.
**The design rule is a contour-length ceiling, not a "short, stiff" instruction:** `L_c b ≤ 3 N k_BT/k_req`, i.e. 30–159 nt depending on `b` and `N` — an ordinary staple extension.

### `S4` — surface-parallel duplex tethers to a coplanar fixed frame. **PASS, with the largest margins and a layout cost**

Four 40 nm tethers attached at the tile's corners and running **tangentially** to a substrate-fixed coplanar frame:

| quantity | clamped-pinned | clamped-guided |
|---|---|---|
| `k_lat` | **55.02 pN/nm — 119.6× the bound** | 55.09 |
| `k_yaw` | **88 000 pN·nm/rad — 239× the bound** | 88 000 |
| `k_norm` | 0.0431 | 0.1725 |
| **anisotropy** | **1276** | 319 |
| stroke lost | **0.26 %** | 1.03 % |
| corner in-plane RMS | **0.434 nm** | 0.434 |
| cable tension at a 3 nm stroke | 3.09 pN → 23.5 pN concentrated | same |

**Why the frame escapes the theorem:** the frame does not stroke, so *its* attachments to the substrate may be arbitrarily stiff. The anisotropy penalty is paid only by load paths that must move with the tile, and the in-plane tether is not one of them — it accommodates the stroke in **bending**, which is `1/L²` softer than stretching.

**Two things this scheme gets wrong if built naively:**

- **Radial tethers fail the yaw budget.** With four *radial* 40 nm tethers the tangential direction sees only the tethers' bending, `k_yaw` = 34.5–138 pN·nm/rad against the 368 required — a **FAIL by 2.7–10.7×**. Rotating the same four tethers by 90° into the **tangential** direction multiplies `k_yaw` by `k_a/k_t` = 640–2550 and it passes by 239×. **Same parts, same count, same cost; a factor of 2550 in yaw.**
- **A short in-plane tether is not better.** At 10 nm the tether's own bending stiffness (`12EI/L³`) costs 14–40 % of the stroke, and its cable tension at a 3 nm stroke is **48.4 pN — at the quasi-static duplex shear allowable**. The in-plane scheme wants tethers *long*, which the vertical one cannot afford.

### `S5` — the same tether on a single-duplex post. **FAIL**, and it is leaf `A8.2`'s answer

Putting the far end of the in-plane tether on a single duplex standing in the layer collapses the scheme:
the post's bending, 0.69 pN/nm, is in series with the tether's 55 pN/nm, and the series is **1.54 pN/nm** — a factor of 36 lost to the weakest element.

### `S6`/`S7` — the anchorless routes, closed as ceilings with thresholds

`C-0010`'s zero is a statement about a laterally **homogeneous** layer under a laterally **homogeneous** tile. Break either and it goes away.
Neither can be *solved* here, so both are closed the way `P-6` closed — the largest value the mechanism can reach, and the value it would have to reach for the answer to change.

**`S6`, a grafting-density pad.** Sliding the tile off a pad of its own size exposes `x·W` of it to a denser layer, so `F ≤ U/W` and `k ≤ U/(W ℓ)` with `U` the compression energy the actuator stored and `ℓ ≈ h` the layer's lateral healing length:

| layer | ceiling [pN/nm] | × the bound | verdict |
|---|---|---|---|
| 5 nm | 0.118 – 0.383 | 0.26 – 0.83 | **EXCLUDED** — even 100 % of the stored energy is short |
| 7 nm | 0.277 – 0.573 | 0.60 – 1.25 | **NOT ESTABLISHED** — the ceiling straddles the bound |
| **10 nm** | **0.479 – 0.751** | **1.04 – 1.63** | **NOT EXCLUDED**, and by less than a factor of two |

Threshold, stated dimensionlessly: **the tile must store at least `W ℓ k_BT/σ² = 184 pN·nm = 44.4 k_BT` of compression energy in the layer.**
And one property no design can remove: `U` is the energy the *actuator* stored, so **the pad's lateral stiffness is proportional to the load the tile already carries and is exactly zero at zero bias** — which is `T-13`'s problem, not a solution to it.

**`S7`, a patterned electrode.** §1 already says the electrode is patterned. With `C-0008`'s force decay length (2.83 nm at a 10 nm gap) the whole tile-electrode interaction energy is ~283 pN·nm, and the lateral stiffness a modulation of it can exert is `U_mod q²·|sinc(qW/2)|·e^{−(√(κ²+q²)−κ)z}`:

| | 5 nm | 7 nm | 10 nm |
|---|---|---|---|
| optimum period | 62.4 nm | 62.6 nm | **62.8 nm** |
| `k_lat` at **100 %** modulation | 0.810 | 1.007 | **1.234 pN/nm** (2.68× the bound) |
| **modulation depth required** | 56.8 % | 45.7 % | **37.3 %** |
| `C-0006` ripple transfer at that period | 0.974 | 0.974 | 0.974 |
| **dishing it costs** | 55.3 % | 44.5 % | **36.3 % of the stroke** |

**REACHABLE BUT SELF-DEFEATING.** The tile follows a 63 nm ripple almost perfectly (`C-0006`'s own interior transfer function, 0.974), so the modulation depth that would confine the tile laterally dishes it by a third of its own stroke — and `C-0006` rejects the rigid-plate assumption above ~19 % depth. **Lateral confinement bought from the field is paid for in flatness, and the exchange rate is 1:1.**

---

## What the anchors cost the actuator, and where the real cost is

| scheme | added `k_norm` | vs `C-0003`'s 16.6–26.1 pN/nm secant | stroke lost |
|---|---|---|---|
| 4 vertical duplex struts, 10 nm | 440 pN/nm | 17–26× the layer | **96.4 %** |
| 4 four-helix-bundle struts | 1760 | 67–106× | **99.1 %** |
| 8 ssDNA tethers | 0.98 | 3.8–5.9 % | **5.6 %** |
| **4 in-plane tethers, 40 nm, tangential** | **0.043 – 0.173** | **0.16 – 1.0 %** | **0.26 – 1.03 %** |

**The linearised normal stiffness is not the binding cost of the in-plane scheme. The cable term is.**
An in-plane tether cannot let the tile descend without stretching: the chord between its fixed ends grows to `√(L²+δ²)`, so

&nbsp;&nbsp;&nbsp;&nbsp;`T = S(√(L²+δ²) − L)/L ≈ S δ²/(2L²)`, &nbsp;&nbsp; `F_z ≈ S δ³/(2L³)`.

| stroke | tether | tension | `F_z` (4 tethers) | verdict |
|---|---|---|---|---|
| 3 nm | 10 nm | **48.4 pN** | 55.7 pN | **FAIL** — at the duplex shear allowable |
| 3 nm | 20 nm | 12.3 pN | 7.3 pN | MARGINAL — 93.6 pN once concentrated |
| **3 nm** | **40 nm** | **3.09 pN** | **0.92 pN** | **PASS** on both |
| 10 nm | 20 nm | **129.8 pN** | 232 pN | **FAIL** — past the 65 pN nicked-duplex ceiling |
| 10 nm | 40 nm | 33.9 pN | 32.8 pN | MARGINAL |
| 10 nm | 80 nm | 8.6 pN | 4.3 pN | MARGINAL |

Which gives a design rule linear in the stroke, `T ≤ A/n` ⇒ **`L_min = δ √(S n/(2A))`**:

| stroke | allowable | minimum tether | `k_lat` there | margin |
|---|---|---|---|---|
| 3 nm | shear, direct on the tether | **10.2 nm** | 433 pN/nm | 941× |
| **3 nm** | shear + `C-0009`'s 7.6× | **28.0 nm** | 157 | 342× |
| 3 nm | unzip + 7.6× | 61.3 nm | 71.7 | 156× |
| **10 nm** | shear + 7.6× | **93.3 nm** | 47.2 | 103× |
| 10 nm | unzip + 7.6× | **204 nm** | 21.5 | 47× |

**This is the `T-2` window constraint this task produces.**
§3's *acceptable* 3 nm stroke needs a ~28 nm tether and a frame standing off by ~28 nm — a 40 nm tile becomes a ~100 nm assembly.
§3's *desired* 10 nm stroke needs a **93 nm** tether, i.e. a frame standing off by more than twice the tile's own width, and **204 nm if any joint is presented in unzip geometry.**
Lateral confinement and the desired stroke are not incompatible; they are **incompatible at a fixed device footprint**, and the exchange rate is `L_min ∝ stroke`.

## The per-anchor force — and a result about over-stiffening

The thermal force in one of `N` anchors sharing a total stiffness `k` is

&nbsp;&nbsp;&nbsp;&nbsp;**`F_RMS = k σ/N = √(k_BT k)/N`** — it grows as the **square root of the stiffness**.

| scheme | `k_lat` | per-anchor force | × `C-0009`'s 7.6 | vs 10 pN unzip | vs 48 pN shear |
|---|---|---|---|---|---|
| minimum design, 4 anchors at the bound | 0.460 | 0.345 pN | 2.62 pN | **3.8× margin** | 18× |
| 8 ssDNA tethers (preload, not thermal) | 0.937 | 1.17 pN | 8.90 pN | 1.1× | 5.4× |
| 4 in-plane tethers, 40 nm | 55.1 | 3.78 pN | **28.7 pN** | **FAIL by 2.9×** | 1.7× |
| the same, cable tension at 3 nm stroke | — | 3.09 pN | 23.5 pN | FAIL by 2.3× | 2.0× |

**Over-stiffening is not free.** The 120×-margin in-plane scheme puts 29 pN on a load path where the minimum design puts 2.6 pN, and that is past the unzip allowable.
**Every joint in a lateral scheme must therefore be presented in shear geometry** — `C-0006`'s "single largest design lever in this task, and it costs nothing" — and a scheme that needs unzip-geometry joints must be de-stiffened toward the bound rather than made as stiff as possible.

**On `C-0009`'s concentration factor.** It is applied at its worst value (7.6×) throughout, and that is **conservative and known to be so**: `C-0009` measures it for an *out-of-plane* anchor reaction, where the load is confined to an `ℓ`-sized bending patch. A lateral tether loads the tile as a **membrane**, carried by duplexes in tension at `S = 1100 pN` each, and it spreads further. The correct treatment is a shear-lag problem on a membrane-loaded lattice and **nobody has done it**; it is queued as `T-15`.

---

## The dominant compliance term — leaf `A8.2`'s explicit ask

| variant | tether axial | post | ssDNA spacer | per anchor | 4 anchors | margin |
|---|---|---|---|---|---|---|
| **as anyone would first draw it** (single-duplex post, 10-nt spacer) | 55.0 pN/nm, **0.7 %** of the compliance | 0.690, **56.5 %** | 0.910, **42.8 %** | 0.390 | 1.559 | 3.4× |
| **as the analysis recommends** (four-helix-bundle post, no ssDNA in the path) | 55.0, 66 % | 106.6, 34 % | — | 36.3 | 145.1 | **315×** |

**The dominant compliance term of a lateral anchor is the *anchor post's bending* and any ssDNA in the load path — never the tether.**
That is the same answer `C-0006` reached for the tile itself (*"joint compliance, and only joint compliance"*), arrived at independently and for a different structure.
The build rule that follows: **put nothing soft in the lateral load path**, and if a spacer is unavoidable it may be at most **79 nucleotides** before four anchors drop below the bound.

---

## Can the lateral anchors be the same attachments as the flatness ones?

**No, and the distinction is a `T-2` window constraint in its own right.**

`C-0009` establishes that flatness needs **64 attachment points against the tile's 56 crossovers** — an *area-distributed, normal-direction* output coupling.
The lateral scheme's anchors sit on the **perimeter** and load the tile **in its own plane**.
They are different load paths in different directions, and they are **additional**: a Gen-1 tile needs ≥ 64 distributed normal attachments *and* 4–8 perimeter lateral anchors, on a structure that has 56 crossovers.

---

## The five verification gates

Executed as tests in `src/test/kotlin/anchoring/`, each named for the gate it discharges:
`AnchorElementTest` (17), `AnchorSchemeTest` (8), `LateralConfinementBudgetTest` (16), `LayerCorrugationTest` (11). **52 tests, all green.**

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `EI/L³` doubles the length and divides by exactly 8; `S/L`; `π²EI/(KL)²` quarters at double length; `3k_BT/(L_c b)` is an energy over an area; a yaw stiffness is a lateral stiffness times a squared radius; a corrugation stiffness is an energy over a squared length, and its threshold inverts its own ceiling | **PASS** |
| **2 — limiting cases** | the two end conditions differ by **exactly 4** in both transverse stiffness and buckling load; a bundle reduces to `n EI₁` when its helices are coincident; a compressed strut loses **exactly all** its transverse stiffness at its Euler load, and past it raises rather than extrapolates; the chain is Gaussian at low force and asymptotes to but never reaches its contour; the cable term vanishes as the **cube** of the offset; a link along the normal puts its axial stiffness in `z` and its transverse in the plane and a link in the plane swaps them **exactly**; a single central anchor has **zero** yaw stiffness whatever its lateral stiffness; a corrugation whose period is a multiple of the tile width does **nothing**; leaf `A1.1`'s bound table reproduced from `k_BT` alone (0.46 / 414 / 4600 pN/nm) | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the chain inversion round-trips `f → x → f` to `1e−9` and exits on the **bracket width**, not a residual; the tangent stiffness is smooth and monotone through the small-argument series join; the tether design solve reproduces its own requirement to `1e−7` at every count and span, and approaches its Gaussian closed form `L_c = 3Nk_BT/(kb)` to `1e−3` for a slack chain; the electrode optimum is a genuine interior maximum inside the domain the expression is valid on | **PASS** |
| **5 — literature cross-check** | `C-0010`'s strut bracket **reproduced from `EI` and `L`** (0.69 and 0.08625 pN/nm); `EI = L_p k_BT` and CanDo's implied `L_p = 55.5 nm`; the ssDNA Kuhn bracket read from **primary sources for this task** and shown to be method-systematic; `C-0006`'s ripple transfer exactly ½ at `λ = 2πℓ`; `C-0006`'s dishing exactly linear in the modulation depth; `C-0009`'s concentration factor applied rather than the equal share | **PASS** |

### Gate 3 — what is checked, given that equipartition *is* the requirement

Asserting `σ² = k_BT/k` here would be a tautology, exactly as it was in `T-8`. Four independent things are checked instead:

1. **The trace invariant.** `k_xx + k_yy + k_zz = k_a + 2k_t` for a link at *any* orientation — a property of the projector `k_a n̂n̂ᵀ + k_t(I − n̂n̂ᵀ)` that no amount of correct arithmetic in one orientation would guarantee. Asserted over 15 (polar, azimuth) pairs.
2. **The exact `r²` cancellation.** The yaw and translation margins stand in the ratio `(r_a/r_b)²` identically, asserted over radii spanning 20× — an algebraic claim about *placement*, independent of any stiffness.
3. **The cable statics identity.** `F_z = T·δ/√(L²+δ²)`, with the tension and the normal force computed by separate code paths.
4. **The anisotropy theorem itself**, checked against the chain it is applied to at three contour lengths and six tensions, including its equality case (`1.0000` at vanishing force) and its monotone departure from it.

### The declared falsifiers

| falsifier | fired? | outcome |
|---|---|---|
| 1. a through-layer link stiffer across than along | no | the ratio never exceeds 1; the equality case is reached exactly at zero force |
| 2. a per-anchor force reaching an allowable | **YES, and it is a result** | the 120×-margin in-plane scheme puts **28.7 pN** on one path against a **10 pN unzip** allowable. Strength binds the *over-stiffened* design, not the minimum one — hence the "do not over-stiffen" rule above |
| 3. the entropic tether coming out at zero | **no, and this is `CH-0013`** | it is **0.115–0.266 pN/nm per tether** across the whole evaluated set, i.e. 25–58 % of the entire requirement from a single tether, and four to eight of them clear the bound |
| 4. every scheme over the 10 % stroke budget | no | two topologies come in at 0.26–8.5 % |
| 5. the FJC tangent disagreeing with its Gaussian limit | no | agree to `1e−6` |
| 6. the `r²` cancellation failing | no | exact at every radius tested |
| 7. a corrugation ceiling far above the requirement | **partly** | the electrode ceiling is 2.68× the bound, so the branch is **not** excluded — but the depth it needs costs 36 % of the stroke in dishing, which is what closes it. Handed to `T-3b` rather than concluded |

---

## Validity range

- **Linear response.** Every stiffness is a tangent at a stated configuration. The cable term is reported as a *secant over a stated stroke* precisely because it is **zero in the linearisation** and dominant outside it.
- **The layer is not modelled as a medium the anchors sit in.** A through-layer tether displaces polymer and is squeezed by the layer's osmotic pressure. Both are omitted. Both would **stiffen** a chain (excluded volume in a semidilute layer is screened, not absent) rather than soften it, so the ssDNA design rule here is the conservative direction — but this is stated, not computed.
- **The frame is assumed rigid.** It is stroke-free, which is exactly why the anisotropy theorem does not bind it, but a real frame has finite compliance in series. `S5` shows what a soft anchor point does: a factor of 36.
- **The in-plane load path into the tile is not solved.** `C-0009`'s 2.3–7.6× concentration factor is an *out-of-plane* result applied here as a conservative bound. The correct treatment is a shear-lag problem on a membrane-loaded lattice (`T-15`).
- **The crossover's axial compliance is a rigid constraint in `C-0009` with nothing cited behind it** (`T-9` is queued to produce it). Any scheme that loads a crossover **axially** inherits that gap. The schemes that pass here load the tile in its own plane and out of the crossover-bending direction, which is the direction that gap does not govern — but a vertical strut *would* load it axially, and that is a second reason not to build one.
- **No electrostatics is solved.** `S7` is a ceiling built on `C-0008`'s force and decay length, not a 2-D Poisson-Boltzmann result.
- **The grafting-pad healing length is taken as the layer height**, on the argument that a grafted layer cannot follow a density step more sharply than a chain can lean. The ceiling scales as `1/ℓ` exactly, so a shorter healing length raises it in proportion.
- **Rupture allowables are quasi-static extrapolations** of loading-rate-dependent measurements (`C-0006`). A static bias is not a 5.5 pN/s ramp.
- **Nothing here is measured about this tile or any anchor.** `PASS` means model-consistent and traceable.

## Numbers that are cited rather than derived

- `EI = 230 pN·nm²`, `GJ = 460 pN·nm²` — **CITED**, CanDo *model inputs* (Kim et al., *NAR* **40**:2862, 2012).
- `S = 1100 pN` and `L_p = 40 nm` in Mg²⁺ — **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997).
- Interhelical distance 2.69 nm — **CITED, MEASURED** (SAXS), Fischer et al., *Nano Lett.* **16**:4282 (2016).
- **ssDNA Kuhn length `1.34–1.41 nm`** — **CITED, MEASURED**, Bosco, Camunas-Soler & Ritort, *Nucleic Acids Res.* **42**:2064 (2014), Table 4, optical tweezers **in MgCl₂ at 2/4/10 mM**, fitted over **10–40 pN**.
- **ssDNA persistence length `1.05–1.42 nm` at ionic strength 6–30 mM** — **CITED, MEASURED**, Chen et al., *PNAS* **109**:799 (2012), SAXS + smFRET on dT₄₀ **at zero force**. `b = 2l_p = 2.10–2.84 nm`. **The tethers here carry ~1 pN, an order of magnitude below the lowest force the spectroscopy fits cover, so this is the applicable end — and it is also the soft one.** Chen et al. further report that a *surface-tethered* chain measures ~50 % stiffer in `l_p` than the same chain free in solution, so this is a lower bound on what a real tether would show.
- ssDNA contour **0.65 nm/nt**, inextensible convention — **CITED, MEASURED**, Sim et al., *Phys. Rev. E* **86**:021901 (2012) and Bosco et al. (2014). **The convention travels with the number**: Bosco et al. state that an *extensible* fit needs the crystallographic 0.57 nm instead, and mixing the two double-counts the extension. At ~1 pN against a 630–710 pN stretch modulus the inextensible model costs 0.15 % of extension, which is what licenses leaving extensibility out.
- Per-path allowables 48 / 10 / 65 pN — **CITED** via `C-0006`'s literature trace (Strunz 1999; Essevaz-Roulet 1997; van Mameren 2009). **Not** §4(f)'s 35–60 pN, which is a whole-cross-section number.
- The 2.3–7.6× concentration factor, the 64-attachment flatness count, the 56 crossovers — **CITED**, `C-0009`.
- The layer stroke bracket, and hence its secant stiffness — **CITED**, `C-0003`.
- The force decay length 1.82 / 2.28 / 2.83 nm — **CITED**, `C-0008`.
- The lateral diffusivity `1.969 × 10⁶ nm²/s` and the 62.8 nm excursion — **CITED**, `C-0010`.
- The ripple transfer function and the dishing-per-depth coefficient — **CITED**, `C-0006`.
- The 3.0 nm bound, the 100 pN, the 40 × 40 nm footprint, the 5/7/10 nm heights, the 3 and 10 nm strokes — §3 and §6.

Everything else is derived from these in code.

## Challenges

**Raises [`CH-0013`](../challenges/CH-0013-entropic-tether-is-not-zero.md) against [`C-0010`](C-0010-tile-positional-variance.md)** — its reachability bracket's ssDNA line, *"a flexible single-stranded tether gives essentially nothing at zero tension"*, is quantitatively wrong, and wrong in the **favourable** direction. `C-0010`'s verdict does not move: the layer still supplies exactly zero and an anchoring scheme is still required.

None stands against this claim. The most likely such result is `T-15`'s: if the in-plane load spreads by shear lag rather than by `C-0009`'s out-of-plane concentration, every "minimum tether length" here is an overestimate by up to `√7.6 = 2.8×`, and the 10 nm stroke would become reachable at a 34 nm tether rather than a 93 nm one. That would **loosen** this claim, not contradict its structure.
