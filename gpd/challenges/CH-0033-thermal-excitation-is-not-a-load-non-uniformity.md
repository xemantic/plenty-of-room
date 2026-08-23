# CH-0033 — Thermal excitation is not a load non-uniformity, and the concentration factor does not multiply the static share

| | |
|---|---|
| **Against** | [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md)'s validity clause *"any load non-uniformity, thermal excitation or attachment-stiffness scatter restores a finite crossover force **in proportion to the non-uniformity**"*, and [`C-0017`](../claims/C-0017-output-coupling-stiffness.md)'s second named failure route, *"`T-17` finding that a real, non-uniform load restores a per-path crossover force large enough to matter, which would put `K2` back inside `C-0009`'s 2.3–7.6× concentration and take its 2.22 pN to 5.1–16.9 pN"* |
| **Raised by** | [`C-0026`](../claims/C-0026-one-row-per-duplex.md) (`T-17`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — **three quantities of different kinds put in one list**, and a **concentration factor applied to a force that never crosses the interface it concentrates on** |
| **Direction** | **favourable throughout.** The load non-uniformity is real and restores 0.15–0.33 pN; the scatter is real and restores 0.88 pN per unit relative amplitude; the thermal term is not a member of the family at all; and `C-0017`'s route to 5.1–16.9 pN cannot occur |
| **Status** | ~~raised.~~ **UPHELD**, and recorded as such by both targets: [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) (`T-16`) carries a banner withdrawing its **failure route 2** on this challenge's ground, and [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) (`T-14`) carries one correcting its validity clause. **No verdict, number or table of `C-0015` or `C-0017` moves.** What moves is the *status* of one sentence in each |

---

## What is challenged

`C-0015`'s exact-zero result is correct and is reproduced here to the `1e−9 pN` floor.
Its validity clause then names three things that break it, in one breath:

> **The zero-crossover-force result for one row per duplex is exact only for a uniform load and a uniform
> foundation**, exactly as `C-0006`'s zero dishing under a uniform load is. Any load non-uniformity, thermal
> excitation or attachment-stiffness scatter restores a finite crossover force in proportion to the
> non-uniformity.

`C-0017` consumes that clause and writes its own failure mode on it.
Two things in the pair do not survive being computed.

---

## Ground 1 — the thermal term is a property of the JOINT, and in the model as built it does not converge at all

`C-0009` introduces the crossover's vertical link as a **penalty** and says so explicitly:
*"the link is a constraint, and `linkStiffness` is a penalty whose value the answer must not depend on"*.
Its gate 4 demonstrates that for the **static** transmitted force, and `C-0026` reproduces that: over
`k_link` = 10³ → 10⁵ pN/nm the static peak moves `1.9e−3` then `1.7e−4`.

The **thermal** force in the same link does the opposite, and provably:

| `k_link` [pN/nm] | 10² | 10³ | 10⁴ | 10⁵ | 10⁶ |
|---|---|---|---|---|---|
| peak crossover force RMS [pN] | 20.32 | 64.35 | 203.51 | 643.58 | 2035.18 |
| ÷ `√(k_BT k_link)` | 0.9986 | 0.9999 | **1.0000** | **1.0000** | **1.0000** |
| per decade | — | 3.166 | 3.163 | 3.162 | **3.162 = √10** |

A spring in thermal equilibrium stores `½k_BT`, so its extension variance is `k_BT/k` and its **force**
variance is `k·k_BT`. **The rigid-constraint limit of a *static* constraint force exists; the rigid-constraint
limit of a *fluctuating* one does not.** So "thermal excitation" is not a non-uniformity that restores
something the symmetry removed — it is `√(k_BT k_v)` with `k_v` the crossover's own vertical stiffness, the
number `T-9` has not produced, and it is:

- **present under a perfectly uniform load** — it is not restored by anything;
- **identical on a 3 × 15 grid and on an 8 × 8 one to four decimal places** (203.5148 against 203.5141 pN at
  `k_link = 10⁴`), so it cannot discriminate between attachment schemes;
- **already inside every per-path allowable in use**, because a rupture force is measured at 300 K on the same
  kind of bond. Adding a broadband thermal RMS to a static share double-counts the motion the measurement
  contains.

This is the same structural fact `CLAUDE.md` already records for anchors — *"over-stiffening an anchor is not
free: the per-anchor thermal force is `√(k_BT k)/N`"* — met here on an internal constraint, where it has the
extra consequence that the model's own regularisation sets the answer.

## Ground 2 — `C-0009`'s concentration factor multiplies the force that CROSSES an interface, not the force that ENTERS at an attachment

`C-0017` writes that a restored crossover force *"would put `K2` back inside `C-0009`'s 2.3–7.6× concentration
and take its 2.22 pN to 5.1–16.9 pN"*.

**The 2.22 pN is `100 pN / 45`, the tension in one attachment.** It never crosses a crossover. What crosses a
crossover is the *imbalance* between neighbouring duplexes, which is exactly what the one-row-per-duplex
symmetry annihilates and what a non-uniformity restores. The concentration factor is defined on that
imbalance — it is the ratio of the peak crossover force to the interface force divided by the crossovers on
that interface — and `C-0026` measures it directly at **2.52–3.49×** on the one-row grids, i.e. at or below the
bottom of `C-0009`'s 2.3–7.6× band, which was measured at a *rigid point anchor*.

Applied where it belongs, it gives

&nbsp;&nbsp;&nbsp;&nbsp;`0.239 pN` of interface force ÷ 4 crossovers × 2.52 = **0.150 pN**, not 5.1–16.9 pN.

**A concentration factor and a share are quantities on two different cuts, and multiplying one by the other is
a category error.** `C-0017`'s failure route 2 is therefore retired, not merely unfired.

## Ground 3 — "in proportion" is right, and the constant of proportionality is what the clause is missing

The two genuine members of the list are linear, and `C-0026` asserts the linearity rather than assuming it
(the restored force is exactly `5.000×` at five times the collar depth, and reverses sign with it). But
linearity without a coefficient is not a fragility statement:

| source | coefficient | value at the design point | ÷ the 2.222 pN static share |
|---|---|---|---|
| `C-0022`'s **solved** edge profile | exactly linear in the collar depth | **0.150 pN** (0.331 worst of 21 states) | 6.8 % |
| attachment scatter, **duplex by duplex** | **0.883 pN per unit relative amplitude** | 0.088 pN at 10 % | 4.0 % |
| attachment scatter, **station by station** | **exactly zero** | `3e−11 pN` at any amplitude | 0 |
| thermal | not a member — see ground 1 | — | — |

The two are equal at a scatter of **17 %**, and even at a relative scatter of **0.99** — every second path at
one per cent of its nominal stiffness — the restored force is **0.86 pN**, still 12× below the 10 pN unzip
band. **The exact zero is not a knife-edge. It is a 20.2× margin** against the worst equal-count shape under
the same solved load (15 × 3, 3.03 pN against 0.150 pN), and the whole non-uniformity budget spends a fraction
of it.

---

## What this does *not* challenge

- **`C-0015`'s exact zero itself**, which is reproduced here at `7.8e−11 pN` under its own point-load case.
- **`C-0015`'s 45-as-3 × 15 flatness answer**, which is confirmed as the smallest flat one-row grid under the
  load its criterion is written on — see [`CH-0034`](CH-0034-flatness-count-saturates-under-the-solved-load.md)
  for the separate question of *which* load that is.
- **`C-0017`'s `K2`, its stability floor, its 33.333 pN/nm mandate or any of its six scheme verdicts.** `K2`'s
  per-path peak stays at its per-path static, 2.222 pN, and `C-0017`'s `P4` therefore stands **as written**,
  with a stronger warrant than it had.
- **`C-0009`'s 2.3–7.6× band**, which is a correct statement about a *rigid point anchor* and is measured at a
  different quantity from the one `C-0017` applied it to.

## The remedy proposed

1. Split `C-0015`'s clause in two: *"any load non-uniformity or attachment-stiffness scatter restores a finite
   crossover force in proportion to it, with the coefficients of `C-0026`"* — and separately, *"the crossover
   also carries a thermal force `√(k_BT k_v)` set by its own vertical stiffness, which is present under a
   uniform load, is common to every scheme, and is not defined until `T-9` runs."*
2. Withdraw `C-0017`'s failure route 2 as stated and replace it with the quantity it should have named: the
   **restored interface force**, 0.24 pN, concentrated 2.5–3.5× onto 4 crossovers.
3. Never multiply a concentration factor by a share. State the cut first.

`C-0015` and `C-0017` are annotated in place with a banner pointing here rather than edited, per
`gpd/README.md`.
