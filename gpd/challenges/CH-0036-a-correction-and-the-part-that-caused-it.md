# CH-0036 — `CH-0024`'s shortfall is a property of a part that left the design in the same iteration, and removing it is worth four grid steps of window

| | |
|---|---|
| **Against** | [`CH-0024`](CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md) — its **quoted bracket** and its status line, not its methodological point |
| **Raised by** | [`C-0027`](../claims/C-0027-window-resynthesis.md) (`T-25`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a correction evaluated against a stack that a claim filed on the same day removed, and reported as a property of the device |
| **Direction** | **favourable, and larger than the correction it corrects** |
| **Status** | raised. **`CH-0024`'s coordinate argument is upheld in full and is right.** What moves is the number attached to it, from 2–13 % to **0.6–0.9 %** at the design point, and the reason the number moved is worth more than the number |

---

## What is challenged

`CH-0024` establishes — correctly, and this challenge does not dispute a word of it — that every stroke in
this programme is measured from `L₀`, a height the tile never occupies, so the delivered travel is `δ* − d`.
It then quotes:

| what the preload is spent against | `d` [nm] | delivered stroke [nm] | shortfall |
|---|---|---|---|
| the layer **and** `C-0017`'s `K2` at 33 pN/nm | **0.07 – 0.38** | **2.62 – 2.93** | **2 – 13 %** |

and states in its own footnote that the stack is *"`C-0014`'s eight substrate tethers + van der Waals against
a gold electrode + the residual zero-bias field + gravity."*

**Those eight substrate tethers are not in the design.** [`C-0023`](../claims/C-0023-two-sided-coupling.md)
and [`CH-0027`](CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md), filed the same day,
remove them: a two-sided coupling turns the hold-down requirement from a **force** into a **stiffness**, which
§3's own mandate exceeds 72.4× unpreloaded, and `C-0023` states the consequence explicitly — *"`C-0014`'s eight
substrate tethers leave the design."*

---

## Ground 1 — the tetherless number is in `T-13`'s own file, and it is an order of magnitude smaller

`gpd/results/T-13-zero-bias-resting-position.json` carries both scenarios. Read at the same 18 states:

| scenario | hold-down at `L₀` [pN] | `d` at 10 nm [nm] | `d` at 7 nm | `d` at 5 nm |
|---|---|---|---|---|
| `C-0021`'s device, **with** the eight tethers (`CH-0024`'s row) | 10.24 / 9.78 / 15.39 | **0.224 – 0.320** | 0.125 – 0.303 | 0.072 – 0.382 |
| **the committed device, tetherless** | **0.815** / 3.263 / 10.760 | **0.0184 – 0.0267** | 0.0426 – 0.109 | 0.051 – 0.296 |

The delivered stroke at the 10 nm design point is therefore **2.973 – 2.982 nm, a shortfall of 0.6–0.9 %**,
not 2–13 %. `C-0023`'s own confinement table says the same thing by a different route (descent 0.017–0.281 nm
across all three heights) and this challenge simply reads it at the height the design window is written at.

**Why the tethers dominate:** they contribute **9.4 pN** of the 10.24 pN hold-down at 10 nm — 92 % of it —
because a surface-parallel tether stretched to the layer height carries tension whether or not it is taut
(`CH-0013`). Remove them and van der Waals against a metal electrode plus the residual field is 0.815 pN,
against a coupling of 33.333 pN/nm.

---

## Ground 2 — it is not a rounding: the tethers are worth four grid steps of design window

`C-0027` runs the §4(a)–(d) intersection with the delivered-stroke threshold `3.0 + d` under both readings:

| | 10 nm window | width | 7 nm window | width |
|---|---|---|---|---|
| **committed device, tetherless** | `[0.011634, 0.288540]` | **24.80×** | `[0.029552, 0.049602]` | **1.678×** |
| had the substrate tethers stayed | `[0.011634, 0.190667]` | 16.39× | `[0.029552, 0.036354]` | 1.230× |
| **difference** | **4 grid steps** | **1.51×** | **3 grid steps** | **1.36×** |

> **The 7 nm window under `CH-0024`'s own tethered stack is 1.23× wide — one grid step from empty.**

`C-0023` was formulated and filed as a `T-13` result: it answers where the tile sits at zero bias. It is also,
and was reported nowhere, **the largest single defence of the 7 nm design window in this programme** — larger
than `C-0019`'s fluctuation widening, larger than `C-0022`'s edge correction, and larger than anything `T-2`
found. A claim's consequences are not confined to the task it was written for, and this one crossed three
tasks in a direction nobody was looking.

---

## What this does *not* challenge

- **The coordinate argument**, which is `CH-0024`'s actual content and is right: `s = 0` is `h = L₀`, the tile
  is not at `L₀` at zero bias, and a stroke quoted from `L₀` is not the travel a load receives. `C-0027`
  adopts it as a **σ-resolved window axis** — the first new one iteration 4 produced — and re-reads §4(a)'s
  compliance clause as `stroke ≥ 3.0 + d` rather than `≥ 3.0`.
- **The mechanism of the spread.** Three of six layer models have exactly zero stiffness at `L₀`, so a soft
  layer is cheap to compress and the coupling protects its own stroke figure. That is why the tetherless `d`
  at 5 nm (0.051–0.296 nm) is still an order of magnitude larger than at 10 nm (0.018–0.027).
- **The remedy.** *"Quote strokes as `δ* − d` with `d` named"* is exactly what `C-0027` does, at all three
  heights and under all three coupling topologies.
- **Anything in `C-0012`, `C-0016` or `C-0017`.** `CH-0024`'s status line — *"no verdict moves"* — is correct
  and remains correct; it is now correct with 10× more room.

## What would falsify this challenge

1. **`T-33` finding that the lever cannot react a downward push**, which is `C-0023`'s own named failure
   route. The coupling would revert to one-sided, `C-0021`'s force requirement would return, and `C-0014`'s
   eight substrate tethers would come back with them — and so would `CH-0024`'s 2–13 %.
2. **A hold-down term nobody has evaluated.** `C-0021` names one: PEG bridging the origami face, where
   hundredths of a `k_BT` per chain would supply the whole requirement (`T-24`). It would raise `d` for
   *every* topology, tethered or not.
3. **`T-40`**, the standoff's base joint — the one unexamined assumption under `C-0025`'s flexure, and
   therefore under the two-sidedness this whole argument rests on.

## What the challenged document should do

Add one row and one sentence:

> With `C-0023`'s two-sided coupling the eight substrate tethers leave the design and the same table reads
> **`d` = 0.018 – 0.027 nm at 10 nm, delivered 2.973 – 2.982 nm, a shortfall of 0.6 – 0.9 %.**
> The 2–13 % figure is a property of `C-0014`'s tethers, not of the stroke coordinate.

`CH-0024` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
