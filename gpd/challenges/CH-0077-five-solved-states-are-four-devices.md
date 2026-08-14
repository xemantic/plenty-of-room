# CH-0077 — `C-0022`'s five headline states are **four devices**, not one operating range, so *"flat at every operating state"* is not a requirement any Gen-1 device expresses: over the range each device actually traverses a robust distribution exists at 0.0372–0.0619 of the stroke, and the five-state minimax that fails is a portfolio duty nothing upstream asks for

| | |
|---|---|
| **Against** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md)'s **Deliverable 4** — *"A distribution is tuned to a **load**, and the load is an operating state"*, and its conclusion *"a flatness count now needs a load case **and an operating state**"* as applied to those five — and against [`CH-0071`](CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md)'s **overturning condition 2**, *"A requirement that one distribution be flat at every operating state. No distribution found here is."* |
| **Raised by** | [`C-0064`](../claims/C-0064-robust-distribution.md) (`T-123`) |
| **Date** | 2026-08-14 |
| **Grounds** | **a premise, not a number.** Every number in `C-0058`'s Deliverable 4 reproduces here — 0.2182, 0.0753, 0.1867, 0.0710, 0.2551 to `≤ 5.8e−4` — and its five-state minimax is *improved on* rather than contradicted. What is challenged is that those five states are the set the requirement is owed over |
| **Direction** | **favourable to `C-0058`.** Its published two-level rim rule turns out to be flat over the **whole traversed range** of both 10 nm devices (0.0753 and 0.0683), which is more than its own Deliverable 4 claims for it |
| **Status** | raised. **No count, no table, no verdict and no remedy of `C-0058` moves.** What moves is the word *"every"* |

---

## What is challenged

`C-0058` reports its flat design at `C-0022`'s five solved states, finds it flat at three of them and
*worse than uniform* at the 2 nm gap, runs a minimax over all five that reaches 0.1587, and concludes:

> *"**A minimax over all five** … reaches a worst case of **0.1587**, so **no distribution found is flat
> at every solved state**."*

and `CH-0071` makes the same set an overturning condition:

> *"**A requirement that one distribution be flat at every operating state.** No distribution found here
> is."*

Both sentences are **true of those five states**. The challenge is that the five states are not an
operating range, and that treating them as one imports a duty no Gen-1 device has.

---

## Ground 1 — the five states are four devices, and `C-0022` labels them so

A device is a `(buffer, layer height, bias)`; under bias it descends from `gap = L₀` to `gap = L₀ − s`.
`C-0022`'s five headline states are:

| state | what it is |
|---|---|
| 2 mM, 10 nm, 0.192 V | the **rest** state of the 10 nm device at 2 mM |
| 0.5 mM, 10 nm, 0.134 V | the **rest** state of the 10 nm device at **0.5 mM** — a different buffer |
| 10 mM, 10 nm, 0.192 V | the **rest** state of the 10 nm device at **10 mM** — a third buffer |
| 2 mM, 5 nm, 0.368 V | the **rest** state of the **5 nm** device |
| 2 mM, 2 nm, 0.368 V | that same 5 nm device **held at §3's 3 nm stroke** — `C-0022`'s own row label is *"held at 3 nm stroke"* |

So the set is *three buffers × one layer* plus *one device's two ends*. **The 2 nm state is not a state
of the 10 nm device at all**, and the 10 mM and 0.5 mM states are not states of the 2 mM device. Asking
one distribution to be flat at all five asks one tile to be flat across three buffers and two layer
heights — a **portfolio** duty. `C-0018` places **one** device with 1–3 % of pull-in margin and `C-0032`
recommends **one** buffer; neither asks for a tile that works in all three.

## Ground 2 — over the range each device traverses, a robust distribution exists

`C-0022` solved **both ends** of the 2 mM 10 nm device's own stroke at its own bias — gaps 10 nm and
7 nm at 0.192 V. Running the same minimax over the states a device traverses:

| device | **minimax / stroke** | flat under `T-5b`'s 10 %? | uniform | `C-0058`'s rim × 5 |
|---|---|---|---|---|
| 2 mM, `L₀` = 10 nm, 0.192 V (`C-0018`'s placed device) | **0.0372** | **YES** | 0.2182 | **0.0753 — also flat** |
| 0.5 mM, `L₀` = 10 nm, 0.134 V (`C-0032`'s recommendation) | **0.0436** | **YES** | 0.2086 | **0.0683 — also flat** |
| 2 mM, `L₀` = 5 nm, 0.368 V | **0.0619** | **YES** | 0.0796 | 0.1867 |
| 10 mM, `L₀` = 10 nm, 0.192 V | **0.0500** | **YES** | 0.2551 | 0.1179 |

Both endpoints are **active** at each optimum, so these are equalised minima and not one state carrying
the other; and adding two interpolated intermediate gaps to the first range moves its worst case by
`2.4e−15`, so two solved endpoints *are* the range.

**`C-0058`'s own design is stronger than its Deliverable 4 says.** Its rim × 5 rule is flat not only at
the design point but over the entire stroke of both 10 nm devices — which is the statement a designer
needs and which the five-state table does not contain.

## Ground 3 — the five-state failure is real, and it is a **sign**, which localises it precisely

The challenge does not soften `C-0058`'s negative; it sharpens it. A genuine minimax — log-sum-exp
smoothing with continuation, analytic gradients through the Woodbury solve, conjugate gradients on the
log-weights, 42 starts, `C-0058`'s own optimiser as a polish — reaches **0.1247**, 21.4 % better than
0.1587 and still 1.25× the tolerance, with **two states active**. Of all 31 non-empty subsets:

- every one of the **14** that puts the 2 nm state together with a 10 nm state is **not flat**
  (0.1085–0.1252);
- every one of the other **17 is flat** (0.0090–0.0797), including everything-but-the-2-nm-state at
  0.0797 and the 2 nm state paired with its **own device's** rest state at 0.0619.

The reason is in `C-0022`'s own table and no downstream claim had used it: **the 2 nm state is the only
one of its 21 solved states whose finite tile carries *less* total force than a 1-D pressure over the
footprint** — −3.91 % against +4.9 % to +19.2 % everywhere else. Its edge effect is a **loss** where
every other state's is a **gain**, so its free-tile dishing field is anti-parallel to theirs: cosine
−0.943 to −1.000, and exactly **−1.000** against the 10 mM state, where every pair among the other four
runs +0.949 to +0.997. **A distribution that flattens an edge enhancement deepens an edge deficit.**

That is a *sign*, so the five-state requirement is not merely hard, it is **structurally
unsatisfiable within any accuracy the search could buy** — and it is unsatisfiable for a reason that
belongs to a *device change*, not to an operating excursion.

---

## What this does *not* challenge

- **`C-0058`'s Deliverable 4 table**, every entry of which reproduces here.
- **The principle `C-0058` draws from it** — *"a flatness verdict needs the state it is read at"* —
  which this challenge **keeps and sharpens**: a flatness verdict needs the **range** it is read over,
  and a range is a device's, not a parameter sweep's. This is the seventh instance of the project's
  standing discipline, not a repeal of it.
- **`CH-0071`'s Grounds 1–3**, its remedy, or its verdict against `CH-0034`. Only its second
  *overturning condition* is mis-specified, and it is mis-specified in the direction that makes
  `CH-0071` look weaker than it is.
- **`C-0058`'s "not found" label**, which was exactly the right one: the improved minimax confirms that
  the search was not the limit, and the per-state least-squares floor (0.0032, 31× below the tolerance)
  still forbids nothing, so the five-state negative remains a *"not found at a large budget"* and not a
  theorem.

## The remedy proposed

Annotate `C-0058`'s Deliverable 4 and `CH-0071`'s overturning condition 2 in place, per
`gpd/README.md`, to read:

> **A requirement that one distribution be flat across `C-0022`'s five headline states** — which are
> the rest states of three buffers plus one 5 nm device's two ends, i.e. a **portfolio** duty rather
> than an operating range. No distribution is: the best worst case is **0.1247** and the obstruction is
> the sign of the 2 nm state's edge effect. **Over the range a single device traverses, a robust
> distribution exists** (0.0372–0.0619 of the stroke), and `C-0058`'s own rim × 5 rule is one of them
> for both 10 nm devices.

## What would overturn this challenge

1. **A §3 or NDI requirement that a Gen-1 tile operate in more than one buffer without re-tuning.**
   Then the five states *are* the duty and `C-0058`'s and `CH-0071`'s wording is right as it stands.
   `C-0064` names this as a **specification gap** rather than a modelling one.
2. **A `C-0022` rim charge that flips the 2 nm state's total-force gain positive.** `C-0022` names its
   rim charge as unsourced and worth 1.85× on the collar, and the discriminant here is a −3.91 %
   difference of two larger numbers — the most exposed number in `C-0064`.
3. **A demonstration that the device traverses states `C-0022` did not solve** — for instance that the
   bias is not held constant over the stroke, so the traversed set is not the two solved endpoints.
   `C-0064` brackets exactly this where `C-0022` left it open, at the two devices whose compressed end
   is unsolved at their own bias.
