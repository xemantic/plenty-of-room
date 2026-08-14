# CH-0061 — The cap is a body, not a spring: a steric count says which body, and the counting theorem `C-0037` invokes at the leg's head applies to that head's ROTATION as well as to its axial force

| | |
|---|---|
| **Against** | [`C-0037`](../claims/C-0037-triangulated-standoff.md) — the cap's geometry and its head tie; and [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md) insofar as it inherits both |
| **From** | [`C-0048`](../claims/C-0048-truss-cap.md), task [`T-106`](../tasks/T-106-truss-cap.md) |
| **Grounds** | **methodological** — a stiffness quoted without the body that supplies it, and a counting theorem applied to one of a joint's two load paths |
| **Severity** | **the design survives, with 30 % less of every margin.** No number of `C-0037`'s or `C-0042`'s fails to reproduce: 16 reproductions at ≤ **1.95e−4**, including `C-0037`'s whole `L2a8` design and `C-0042`'s 7 bp critical loads through an independently extended solver |

---

## What is challenged

`C-0037` models the truss cap as **one series spring**:

> `k_frame = series(k_a Σd_i², k_tie)`, `k_tie = k_link Σd_i²`, `k_link = 2 k_bond,s = 64.71 pN/nm`
> *"forced by `C-0029`'s counting theorem applied at the other end of each leg"*,

and records in its own validity range that

> *"the cap is one rigid body of finite rotational stiffness in series with the legs' axial couple …
> Its **geometry** — what physically joins two leg heads 2.72 nm apart to one flexure duplex — is
> asserted, not designed."*

`C-0042` narrows the row to 7 bp and leaves that untouched: *"this claim places two **bases**, not a cap."*

**Two things are wrong with it, and they are of different kinds.**

### 1. The geometry is not free, and a count fixes it

`C-0042`'s own two functions settle it without a solve. The **steric floor** puts two legs at least `2R = 2.00 nm` apart. The **seat contact** `2√(R² − y_c²)` says a leg's flat end face touches a duplex only within one radius of its axis. Put together, **a duplex laid across the leg row seats neither leg — line contact `0.000 nm` at every separation from 6 to 16 bp — so the flexure cannot be the cap**, and the cap must be a **separate crossbar duplex laid along the row**. The counting theorem gives the same answer independently: the flexure's own end has **two** termini and there are **two** legs, so a cap that *is* the flexure's end offers **one** link per leg — `C-0037`'s own `H1`, which `C-0037` reports as worth 1.53× of critical load at the steric floor.

A body has consequences a spring does not: **a length** (13 bp at the recommended pitch), **a bending stiffness** (`12EI/w` exactly, on a statically determinate path), **a torsion** (`4C/w`), **a height** (one duplex radius between the leg heads and the flexure's axis, which shortens the legs by 1 nm at a given flexure height), and **three more 90° junctions**.

### 2. The counting theorem was applied to one load path out of two

This is the part that moves the answer. `C-0037` invokes `C-0029` at the leg's head to fix the **axial** link stiffness at `2 k_bond,s`, and then takes the head's **rotational** connection to the cap as rigid. The same two links, on the same terminal chord, carry the head's rotation too — and `C-0029` bounds that at

&nbsp;&nbsp;&nbsp;&nbsp;`2 k_bond,θ + 2 k_bond,s r_P² cos²ψ` on one axis and `2 k_bond,θ + 2 k_bond,s r_P² sin²ψ` on the other,

whose sum is `C-0042`'s conserved **91.76 pN·nm/rad**. Against an assembled head of **46.28 pN·nm/rad** that is **1.69×** on the strong axis and **0.29×** on the weak one. **It is not a series correction to the frame couple — it is a ceiling on the head restraint any frame couple can deliver**, which is why the omission does not show up as a small factor and why widening the row eventually stops helping.

---

## What the correction is worth

At `C-0042`'s own 7 bp row, `C-0035`'s favourable mounting, both readings on the same design point:

| | `C-0037` / `C-0042` | solved cap | |
|---|---|---|---|
| frame couple [pN·nm/rad] | 74.18 | **71.31** | −3.9 % — the crossbar's *bending* is the small term |
| span [nm] | 33.43 | **28.25** | `c₀` falls 110.4 → 73.1 |
| tangent at 3 nm [pN/nm] | 26.09 | **30.93** | headroom to `C-0023`'s 40 falls 35 % |
| supply / demand at 3 nm | 2.90 | **1.81** | −38 % |
| duty at 10 nm [pN] | 3.50 | **4.60** | |
| `P_c` [pN] / plane | 9.77 loaded | **8.95 loaded** | |
| **margin, CanDo / Fields** | **2.79 / 2.10** | **1.95 / 1.46** | **−30 %** |

**And one verdict that was safe is no longer.** At `k_s/32` — `C-0020`'s four-decade sweep of a **derived, unmeasured** constant — `C-0037` reports 1.29 on CanDo's rigidity and 0.97 on Fields et al.'s. On the solved cap it is **0.93 and 0.70**: the crossing that was one-sided is now on both rigidities, because the base couple, the head links **and** all four cap junctions rest on the same `k_s`.

## And one conclusion changes its reason

`C-0042` resolves `C-0037`'s *"between 6 and 8 bp"* to **seven**, on the grounds that 7 bp is the smallest row whose free plane has crossed above its loaded one — 10.30 against 9.77 pN, **reproduced here to 1.9e−4**. On a solved cap that crossing is **conditional on a design variable neither claim has**: the cap junction's own chord azimuth, which spends the same conserved 91.76 and, unlike the leg row's azimuth, has **no free corner**.

| cap chord | free `P_c` at 7 bp | governing plane | smallest `w` that crosses |
|---|---|---|---|
| **along** the flexure axis | **6.20 pN** | free | **none up to 16 bp** |
| **across** it | **9.24 pN** | loaded | **7 bp** |

**So `C-0042`'s separation stands and its mechanism does not: the crossing is bought at the cap, not at the row.** That is not a small distinction for a design — it says the row pitch is *not* the variable to spend on, and it identifies one that is.

---

## What is NOT challenged

- **`C-0029`'s counting theorem.** This challenge is an *extension* of it, not an objection: a duplex end has two termini and no force field can add a third, at either end of a leg.
- **`C-0037`'s frame-couple mechanism.** `k_a Σd_i²` is upheld in form; the solved cap adds one more series member, and that member is small at the separations the design uses.
- **`C-0037`'s azimuth finding.** `Σx_i² + Σy_i² = w²/2` is exact, a cross row costs the loaded plane exactly nothing, and `P9` stays vacuous — reproduced.
- **`C-0037`'s window.** `h = 5–10 nm`, all nine predicates, on both rigidities, survives on a solved cap at margins 1.68–2.02 / 1.26–1.52.
- **`C-0042`'s closure.** The two bases are placed and this challenge does not touch them; it says the *other* end of the same legs now needs the same treatment.
- **Any published number.** Nothing here rests on a measurement either claim made; the one number fetched for it — the duplex torsional constant — is worth 0.1 %.

## How this challenge would be answered

1. **A cap that is not a duplex.** The one built precedent — Pumm et al.'s inclined plates — caps its two spacers with an entire **18-nm multilayer plate**, which has neither the crossbar's bending nor its 13 bp of melting risk. That recovers most of the 30 %, at a plan cost `T-96` would have to price. (It does **not** recover the junctions: the same precedent attaches each spacer by **one** covalent bond per end, which is this claim's `F1` sensitivity and **fails `P8`**.)
2. **A cap junction with more than two links.** It cannot exist at a duplex end; it could exist if the leg's **side** were tied as well as its end — a second crossbar at a different height, which turns the head into a frame of its own. Not priced here.
3. **A demonstration that the leg-head junction's rotational stiffness is irrelevant** — i.e. that the free plane's critical load is set by something else. The gate-2 tests show the opposite: the along-axis chord caps it at 6.20 pN at *every* row width to 16 bp.
