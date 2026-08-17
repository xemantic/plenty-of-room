# CH-0106 — **`DECISIONS-FOR-NDI` tells NDI that six *independent* routes recommend 0.5 mM; two of the six are the other four, read again** — `T-2` carries `T-3`'s blocking bias at 15 of 15 states at departure `0.0`, `T-25` carries `T-16`'s and `T-4`'s extrema at 20 of 20, one route is already withdrawn, and the strongest figure quoted (4.97×) is read at **zero stroke** where the device sits at three nanometres of it

| | |
|---|---|
| **Raised by** | [`C-0091`](../claims/C-0091-buffer-route-census.md) (`T-156`) |
| **Against** | the sentence *"**Six independent routes recommend it**: `C-0012` on the force clause, `C-0016` on the bias window, `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin, and `C-0032` on the realised coupling law"*, as it stands in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) decision 1, in [`ANSWERS.md`](../../ANSWERS.md) question 1 and in `TASKS.md`'s `T-63` row. **Not against any number any of the six claims reports**, every one of which stands |
| **Grounds** | **arithmetic, and mechanically checkable.** Independence was asserted from the *names* of the clauses, not from the numbers behind them. `T-2`'s `biasClauses[].biasForHundredPiconewtonBlocking` is `T-3`'s own number at **15 of 15** `(height, buffer)` states, worst departure **0.0**, and `T-2`'s binding clause at 10 nm is literally *"force"* at every buffer. `T-25`'s `bufferComparison` carries `T-16`'s `stabilityMargin` extrema and `T-4`'s coupled `margin` extrema at **20 of 20** comparisons, worst departure **2.66e−8** — which is `T-25` printing eight significant digits where `T-16` prints nine. `C-0032`'s route was already withdrawn by [`CH-0098`](CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md). **Six named routes are 1 withdrawn + 2 transfers + 3** |
| **Severity** | **a specification brief that overstates the diversification of its own evidence, not an error.** The recommendation *"adopt 0.5 mM"* stands and every surviving route favours it at every layer model. What falls is the count, the implied independence, and the size of the largest number quoted for it. **A deliverable that over-claims is exactly as wrong as one that under-claims** (`C-0067`), and this is the over-claiming direction in the one document written for a reader outside the programme — the second such finding in decision 1 in two iterations |

---

## What is claimed upstream

`DECISIONS-FOR-NDI.md`, decision 1:

> Six independent routes in this programme have converged on **0.5 mM** …
>
> - **Six independent routes recommend it**: `C-0012` on the force clause, `C-0016` on the bias window,
>   `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin,
>   and `C-0032` on the realised coupling law.

and, as the quantified case:

> At 0.5 mM it is reached at 0.141 V even at 10 nm — **a factor of five better than at 2 mM**.

**Every number in that passage is correct for the object it was measured on.** What is wrong is the count and the state.

## What `C-0091` finds

| route | compared quantity | verdict | independence |
|---|---|---|---|
| `C-0012` | the bias 100 pN of **blocking** force needs at 10 nm | survives, same ground | **INDEPENDENT** |
| `C-0016` | *the same field*, re-intersected over a `σ` grid | survives | **TRANSFER of `C-0012`** — 15 of 15, departure `0.0` |
| `C-0017` | `\|k_eff\|` at the held operating point | survives, same ground | **INDEPENDENT** |
| `C-0018` | the coupled bias margin | survives, **DIFFERENT ground** | **INDEPENDENT** |
| `C-0027` | the corrected margins | survives | **TRANSFER of `C-0017` and `C-0018`** — 20 of 20, departure `2.66e−8` |
| `C-0032` | the fold on `C-0030`'s softening flexure | **WITHDRAWN** (`CH-0098`) | — |

**Three, and two of the three are weaker than the sentence implies.**

1. **`C-0018`'s ground is void.** Its own reason is *"dropping to 0.5 mM removes the fold entirely."* On the element `C-0071` recommends there is **no fold at 2 mM at any of six layer models** (`C-0084`), so there is nothing to remove. The route survives as a **preference**: a bias margin of **1.8706** against **1.3877**, a factor of **1.3480**.
2. **`C-0012`'s 4.97× is a zero-stroke reading.** The device operates at `L₀ − 3 nm` delivering 100 pN, and `C-0017`'s `simultaneousTargetBias` is the same clause at that state: **1.4823–1.5703×** over the six layer models, an overstatement of **3.1621–3.3499×**.

**Read at the state the device occupies, the three advantages are 1.35, 1.57 and 1.75 — not 4.97.**

## And the three are not three exposures

All three are downstream of `C-0008`'s single mean-field Poisson-Boltzmann model, carried by **two** mechanisms — the **level** of `|F_es|` at a fixed bias (`C-0012`) and **`1/ℓ` at a force-pinned point** (`C-0017`, `C-0018`). `C-0005`'s one-loop correction is **123–214 %** of the leading term over this gap range: **common mode, and larger than every one of the three advantages.** The word *independent* implies a diversification that is not there, and `T-50` is the only thing in the queue that would supply it.

## A reading of `C-0016` that runs the other way, and is also correct

`C-0016`'s §(f) stability count, read at a **fixed applied bias** rather than at a held operating point, prefers **2 mM**: 1 of 6 models unstable at 0.05 V in 0.5 mM against 0 of 6 in 2 mM, and 86.08–109.99 pN/nm of coupling demanded at 0.25 V against 47.63–71.54.

**This is not a contradiction of `C-0017`.** A held operating point is **force-pinned**, so `k_es = −|F_es|/ℓ` and the longer decay length at low salt wins; a fixed applied bias is not pinned, so the larger force wins instead. But it does mean *"`C-0016` on the bias window"* is a statement about **one of that claim's two clauses**, and the deliverable does not say which.

## What this does NOT challenge

- **The recommendation.** 0.5 mM stands. Every surviving route favours it, at every layer model, at the recommended device.
- **Any number in `C-0012`, `C-0016`, `C-0017`, `C-0018`, `C-0027` or `C-0032`.** All of them reproduce; several of them reproduce *because* they were transferred, which is a genuine guard against transcription and is why the transfers are recorded rather than deleted.
- **`CH-0098`.** This extends it: `CH-0098` withdrew one route and asked for the census of the other five; this is that census.
- **`C-0084`.** Its numbers all stand. Its *"Still open"* item 4, offering itself as a **seventh** route, is the one thing corrected: it is `C-0018`'s route re-read on `Q5`, which is how `C-0091` counts it.

## What would settle it

1. **An edit to `DECISIONS-FOR-NDI.md` decision 1** replacing *"six independent routes"* with the census: three independent routes, one of them on a different ground from the one its claim states, and the strongest of them worth 1.75× at the operating point. **Additively or struck, never rewritten** — the document is Kazik's.
2. **The same edit in `ANSWERS.md` question 1 and `TASKS.md`'s `T-63` row**, which carry the same sentence.
3. **A standing discipline, and it is the generalisable half**: *before counting corroborating routes, diff their result files.* A synthesis that reads claims cannot see a transfer, because each claim is telling the truth about itself; only the JSON shows that two of them are one number.

## Status

**OPEN at filing; the three edits in item 1 and 2 were made with `C-0091` (iteration 18)**, additively and with the superseded text struck rather than deleted. The evidence is `gpd/results/T-156-buffer-route-census.json`, sections `routes`, `blockingBiasTransfers`, `correctedMarginTransfers`, `heldForceClause` and `fixedBiasCounterReading`.
