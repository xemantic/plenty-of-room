# CH-0025 — The electrostatic edge taper is an edge *enhancement*, and its width is not the Debye length

| | |
|---|---|
| **Raised** | 2026-08-13, by [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) (`T-3b`) |
| **Against** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (`T-5`/`T-5b`) and [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) (`T-10`), specifically their `electrostatic-edge-taper` load case |
| **Grounds** | methodological — an assumed load model, flagged as assumed by its own authors, replaced by a solve |
| **Scope** | the **sign** and the **width** of the taper. **No dishing verdict in either claim moves**, and no arithmetic in either is disputed. |

---

## What is challenged

`C-0006` introduces the edge taper as a *bounded perturbation* and says so explicitly:

> *"a finite charged tile at a gap comparable to the Debye length loses field lines off its rim, so the downward pressure is lower there"*

and, in its validity range:

> *"No electrostatics is solved. The load enters as a 100 pN total and a bounded edge taper. `T-3` owns the load model, and the linearity demonstrated above means `T-3`'s answer can be substituted without re-running anything."*

`C-0009` inherits the same field, at the same 50 % depth over the same 4 nm rim.

**`T-3b` has now solved it, and both halves of the assumption are wrong.**

| | `C-0006`/`C-0009` assumed | `T-3b` solved (2 mM, 10 nm, operating bias) |
|---|---|---|
| **sign** | a **taper** — the rim loses load | an **enhancement** — the rim gains it |
| **depth** | +0.50 | **−0.303** (−0.06 to −0.52 across the 0.5 and 2 mM box) |
| **width** | 4.00 nm, *"one Debye length"* | **8.94 nm** equivalent, decay length 2.66 nm |
| total force over the footprint | unchanged by assumption | **+14.7 %** |

## Why the sign is wrong, and it is not a subtlety

The premise is that a finite charged plate loses field lines off its rim. It does — and a finite capacitor's **fringing field nevertheless increases** its capacitance, its stored energy and the force between its plates. The nonlinear 2-D solve reproduces exactly that: the load is *enhanced* over the band 0.8–5 nm inside the rim, peaking at **1.88×** the interior value about a nanometre in, and reverses sign only inside the last half nanometre where the field concentration at the sharp edge lifts the tile.

Stated in the way that needs no profile at all: **the finite tile behaves electrostatically as one 1.65 nm larger on every side** — a sub-Debye collar, worth +14.7 % of the total force on a 40 nm tile and +25.8 % on a 20 nm one.

The width is wrong for a different and simpler reason. `C-0006` took the rim width to be the Debye length. The lateral decay rate inside a slit is not `κ` but the transverse eigenvalue `q₀`, and `q₀² ≥ κ² + (π/2h)²` — the geometric term is comparable to `κ²` at every §3 working gap. So the correct scale is **0.62–0.84 of `λ_D` at 2 mM**, and it **narrows as the gap closes**, which is the opposite of the intuition the Debye-length choice encodes. The solved profile's own decay length, 0.71–2.73 nm over the box, respects that ceiling everywhere.

## Why this is a challenge and not an overwrite

**Because the two errors very nearly cancel in the only number `C-0006` and `C-0009` actually publish.**

| | peak dishing | / stroke |
|---|---|---|
| `C-0006`'s assumed taper (+0.50 over 4 nm) | 1.326 nm | 0.268 |
| **`T-3b`'s solved edge effect (−0.303 over 8.94 nm, plus its rim term)** | **1.590 nm** | **0.321** |
| the solved depth at `C-0006`'s assumed width | 0.803 nm | 0.162 |

A 1.20× move, on a load case `C-0006` already reported as **REJECTED** by a factor of 2.7. So:

- **`C-0006`'s `T-5b` verdict table does not change** — the edge-taper row was REJECTED and stays REJECTED, now by 3.2× rather than 2.7×;
- **`C-0006`'s "upheld below ~19 % depth" flip point does not change**, because it is a statement about depth and the response is still exactly linear in it;
- **`C-0009`'s lattice-versus-plate ratios do not change** — the smooth-taper ratio of 0.944–0.994 is applied to the new load unchanged, giving 30.3–31.9 % at the design point;
- **the 55/64/45-attachment flatness counts do not change**, since they are set by the *anchored* and *concentrated* load cases, not by this one.

But the agreement is a **coincidence of two errors and not a vindication**: at `C-0006`'s width the solved depth gives 16.2 %, so either correction applied alone moves the answer by 1.7×. A downstream reader who takes the 4 nm rim width and pairs it with a solved depth — the natural half-substitution — lands nowhere near either.

## What would falsify this challenge

Stated in advance, per §5.

1. **A rim charge model that inverts the depth.** The rim charge is unsourced and it is a **1.85× bracket** on the depth (`−0.2906` uncharged against `−0.1575` at the face density). It does not reach a sign change over that bracket, but nothing here proves a third reading could not.
2. **A 3-D corner solve** showing the corner contributes with the opposite sign to the straight edge. The two mappings this task uses bracket the corner at 1.8 percentage points of total force at 40 nm; a corner effect larger than that bracket would be a new result.
3. **Explicit-ion simulation.** `C-0005`'s one-loop correction is 123–214 % of the leading term here and its direction for oppositely charged walls is unpublished. It is bigger than the entire effect being challenged, and it is the honest limit on how much weight this challenge can bear.

## What the challenged claims should do

Nothing urgent. Substitute `(depth, width) = (−0.303, 8.94 nm)` plus the rim line term wherever `(0.50, 4.00 nm)` appears, note that the sign is a *sign* and not a magnitude — the tile bows the other way, edges down rather than centre down — and carry the 1.85× rim-charge bracket. `C-0006`'s own demonstrated linearity in depth means no solver has to be re-run to do it.
