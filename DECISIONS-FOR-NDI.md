# DECISIONS-FOR-NDI

Six decisions this programme cannot make for itself.

Each is a **specification** question rather than a modelling one:
no amount of calculation closes it,
because the thing that is missing is a statement of intent about the device,
not a number about the physics. 
They are collected here so they can be reviewed together and answered in one pass,
rather than one at a time across iterations.

They are live in [`TASKS.md`](TASKS.md) as `T-63`, `T-115`, `P-13`, `T-112`, `T-154` and `T-166`,
and this file is the reviewable form of the same six.

## How to read this

For each decision: what is being asked, why the programme cannot decide it,
what has been established either way, the options with their consequences,
what the programme would recommend and on what grounds,
and **what it costs to leave it open** — which is the part that is easy to miss.

**A one-sentence answer is enough for all six.**
None needs a document; three of them need a single word.

**Nothing here is measured.** Every number is TRL 1–3:
model-consistent and traceable to a claim, never empirically demonstrated.

## At a glance

| # | Decision | Owner task | Cost of deferring | Programme's recommendation |
|---|---|---|---|---| 
| 1 | Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer, instead of §3's 2 mM? | `T-63` | ~~The device sits **on** its own pull-in fold at 2 mM~~ — **withdrawn, `C-0084`**; the recommended element does not fold at 2 mM at all, on **any** elastica branch (`C-0092`). The cost of deferring is now a **margin**, 1.39× against 1.87× at worst, not a failure | **Adopt 0.5 mM — but as a PREFERENCE, not a requirement, and on THREE routes rather than six (`C-0091`)** |
| 2 | May the polymer layer be taller than 10 nm — 17–26 nm? | `T-115` | §3's *desired* ~10 nm stroke stays unreachable on §3's own stack | **A yes/no is enough**; a *no* closes a branch cheaply |
| 3 | What is the electrode made of, and where is its potential of zero charge? | `P-13` | 2.6× on the one hold-down no design can remove | **Name a material**; the programme will not choose one |
| 4 | Which device does §3's *desired* clause ask for? | `T-112` | Every desired-stroke number in the corpus has to be quoted twice | **One device, placed at the acceptable clause** |
| 5 | What is the scaffold — linear or circular, M13 or synthetic, and how long? | `T-154` | A seam costs 6–12 of 34 arm stations, and §3's 40.0 nm is not a buildable seamless width | **A purpose-length scaffold**; the choice is a fabrication decision, not a modelling one |
| 6 | Does §3 admit a **two-layer** tile, or is single-layer a requirement? | `T-166` | The only coupling topology that narrows the flatness negative is multilayer, and it is the one motif here with a published precedent | **A yes/no is enough**; a *no* closes the last open recovery route |

---

## 1. Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer? (`T-63`)

> ### CORRECTED, iteration 17 — this decision's severity is withdrawn, its recommendation stands
>
> **`C-0084` ([`CH-0098`](gpd/challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md)) removes the word *requirement* from this decision.**
> Everything below about *"the device sits on its own fold at 2 mM"* is `C-0032`'s finding about
> `C-0030`'s strain-**softening** coupled-standoff flexure — **an element `CH-0081` has since removed
> from the output role.** The element the programme actually recommends (`C-0071`'s `Q5`) is
> strain-**stiffening**, and at 2 mM it has **no pull-in fold at all**, at **0 of 6** layer models
> where the affine mandate folds at 6 of 6. On the bias axis its margin is **1.3877–7.3137** at 2 mM
> against **1.8706–10.9072** at 0.5 mM (the *tops* widened by `C-0092` in iteration 18, which found
> `C-0084`'s branch end to be a force-ladder artefact; both **minima** are unchanged); on the stroke axis the fold moves from 3.4104–4.1248 nm to
> **past 7.9097 nm**, and the binding ceiling changes owner from pull-in to `C-0002`'s `φ = 0.2`.
>
> **So 0.5 mM remains the recommendation and stops being a requirement.** It is bought for margin
> — a 1.4–2.6× reserve becomes 1.9–3.5×, and the stability floor falls from 23.41–27.91 to
> 3.86–15.94 pN/nm — rather than to keep the device off a fold it is not on. The 1–3 week Monte
> Carlo alternative (`T-50`) is correspondingly **less** forced than the text below says.
>
> This is `CLAUDE.md`'s own standing warning arriving in the deliverable: *a correction can be
> quoted against a stack that has already left the design.* ~~**A census of how many of the six
> routes below are read on withdrawn objects has never been taken** and is queued as `T-156`.~~

> ### CORRECTED AGAIN, iteration 18 — the count. The recommendation still stands
>
> **`C-0091` ([`CH-0106`](gpd/challenges/CH-0106-six-routes-are-three.md)) took the census, and
> the six independent routes are THREE.** One (`C-0032`) is withdrawn, as above. **Two are the
> other four, read again**: `T-2`'s bias clause carries `T-3`'s own blocking bias at **15 of 15**
> states at a departure of **0.0**, and `T-25`'s buffer comparison carries `T-16`'s and `T-4`'s
> extrema at **20 of 20** comparisons at a departure of **2.66e−08** — which is one file printing
> eight significant digits where the other prints nine. So `C-0016` is `C-0012` on a `σ` grid and
> `C-0027` is `C-0017` and `C-0018` with two corrections applied.
>
> **Two of the three survivors are also weaker than the text below says.**
> `C-0018`'s stated reason — *"0.5 mM removes the fold"* — is **void** on the recommended element,
> which has no fold at 2 mM to remove; it survives as a **1.35×** preference on the bias margin.
> And the *"factor of five"* below is read at **zero stroke**: at the operating point the device
> actually occupies (`L₀ − 3 nm`, 100 pN delivered) the same clause is **1.48–1.57×**.
>
> **Read at the state the device occupies, the three routes are worth 1.35×, 1.57× and 1.75×** —
> and they are **not three independent exposures**: all three are downstream of `C-0008`'s single
> mean-field solve, whose one-loop error (`C-0005`, **123–214 %**) is common mode over all of them
> and larger than each. `T-50` is the only thing in the queue that would change that.
>
> **Nothing here argues for 2 mM.** Every surviving route favours 0.5 mM, at every layer model.
> What is corrected is the count, the word *independent*, and the size of the largest number.

**The question.** §3 specifies 2 mM MgCl₂. Leaf `A2.2` names a low-screening operating point.
~~Six independent routes in this programme have converged on **0.5 mM**,
and as of iteration 5 it stopped being an improvement and became a requirement for the surviving coupling.~~
**Three independent routes converge on 0.5 mM** (`C-0091`), and it is a **preference** rather than a
requirement (`C-0084`, `CH-0098`).

**Why the programme cannot decide it.** Buffer composition is a specification of the experiment,
not an output of the model.

**What is established.**
  
- ~~**Six independent routes recommend it**: `C-0012` on the force clause, `C-0016` on the bias window,
  `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin,
  and `C-0032` on the realised coupling law.~~
  **THREE independent routes recommend it** (`C-0091`, `CH-0106`): `C-0012` on the force clause
  (**1.48–1.57×** at the operating point, 4.97× at zero stroke), `C-0017` on the static stability
  floor (**1.75×**, and the floor itself is element-independent), and `C-0018` on the coupled bias
  margin (**1.35×**, re-read on the recommended element, and on a *different* ground from the one
  `C-0018` states). `C-0016` is `C-0012`'s own number on a `σ` grid, `C-0027` is `C-0017` and
  `C-0018` corrected, and `C-0032` is withdrawn.
- **At 2 mM the device is placed on its own fold.** `C-0017`'s stability margin was banked on the
  coupling being strain-*stiffening*; `C-0030` established that the realised coupled-standoff flexure
  strain-*softens*. Re-running `C-0018`'s fold analysis on that law over 216 states, the 10 nm / 2 mM
  bias margin collapses from 1.007–1.032 to **1.0000–1.0019**, and the fold's stroke walks back from
  3.41–4.13 nm to 2.80–3.17 nm — **through §3's own 3 nm target at two of six layer models**.
- **At 0.5 mM the fold does not exist at all** (0 of 6 states, against 6 of 6 at 2 mM), and the
  corrected stability margin is **2.16–9.87×** against 1.23–1.53×. It is the only margin in this
  repository that clears `C-0005`'s own 123–214 % mean-field error.
- **It costs nothing.** `C-0007` puts the layer's buffer sensitivity at **≤ 0.4 %** of the modulus,
  so the window edges, the chain length, the stroke and the chemistry are all unchanged.
- **The obvious escape is priced and fails.** The adverse mounting is 42.4–61.0 pN/nm, past
  `C-0023`'s 40 pN/nm compliance ceiling at **0 of 8** lengths. A shorter standoff lands 2.2 % short
  at ℓ = 5 nm and is excluded by the 10 pN unzip allowable at ℓ = 4 nm.

**The alternative if the answer is "hold 2 mM".** `T-50` — a beyond-mean-field treatment of the
actuated gap, costed at **1–3 weeks of primitive-model Monte Carlo** in a regime `C-0005` reports has
**no systematic theory at all** (`Ξ = 17–24`). It is the last unbounded exposure on the Gen-1 critical
path, and `CH-0019` established that nothing else in the queue can reach it.

**What deferring costs.** The programme continues to carry two readings of every stability and bias
margin, and `T-50` stays on the queue as a multi-week item that a one-word answer would delete.

---

## 2. May the polymer layer be taller than 10 nm — 17–26 nm? (`T-115`)

**The question.** §3 specifies a 5–10 nm polymer layer. Is that a hard bound, or a nominal?

**Why the programme cannot decide it.** It is a stack geometry decision.

**What is established.**

- **A stroke is a compression of the layer**, so `s = L₀ − h < L₀` identically (`C-0050`). With §3's
  `L₀ ≤ 10 nm`, the *desired* ~10 nm stroke asks the layer for `h = 0`.
- The layer height at which a **100 pN dead-load stroke reaches 10 nm** is **16.63–26.12 nm** over
  `C-0003`'s six models at `σ = 0.024 nm⁻²`.
- **§3's own tile row already allows the effort point to sit ~20–25 nm above the electrode**, so the
  geometry is not absurd on its face.
- **But nothing in this programme has evaluated a layer that tall.** A 17–26 nm layer moves
  `C-0002`'s crossover, `C-0005`'s screening validity, `C-0007`'s drainage and `C-0012`'s bias.

**Why a *no* is as useful as a *yes*.** A *no* closes the branch immediately and confirms what is
already this programme's most-repeated finding — that §3's desired stroke is unreachable on §3's own
stack, and the acceptable clause (3 nm) is what Gen-1 targets. A *yes* opens real work: it is a
substantial re-run across four claims, not a parameter change.

**What deferring costs.** This is the only route in the programme that can buy the desired stroke, so
while it is open the desired-stroke branch cannot be either pursued or closed.

---

## 3. What is the electrode made of, and where is its potential of zero charge? (`P-13`)

**The question.** §1 says *"patterned electrode"* and never says of what.

**Why the programme cannot decide it.** A better calculation of the wrong material is not an
improvement. The answer is bracketed and handed back rather than chosen.

**What is established.**
  
- **Metal against oxide is 2.6×** on the van der Waals hold-down — the one term no design can remove.
  That is larger than the DNA Hamaker constant (1.17× after the square root), larger than 
  retardation, and larger than the polymer in the gap.
- **Zero *applied* bias is not zero *charge*.** A contact potential of **0.9–5.1 mV** — below anything
  a bench would call zero — supplies the **entire** thermal-scale hold-down by itself. No §3 parameter
  fixes it.

**What an answer needs to be.** A material (gold, ITO, SiO₂, …) and, if known, its potential of zero
charge in this buffer. If the material is genuinely not yet chosen, saying so is also an answer — the
programme will then carry the bracket explicitly rather than implicitly. 

**What deferring costs.** `C-0021` already states the exposure as a bracket with a threshold rather
than a guess, so nothing is blocked. The number is simply softer than it needs to be.

---

## 4. Which device does §3's *desired* clause ask for? (`T-112`)

**The question.** §3 gives an acceptable clause (100 pN at 3 nm) and a desired one (~10 nm stroke).
Are these two operating points of **one** device, or two devices?

**Why the programme cannot decide it.** It is a reading of the requirement.

**What is established** (`CH-0059`).

- **Every claim in this programme reads the desired stroke on a coupling placed for the acceptable
  one**, `k_c = 33.333 pN/nm` — which must then deliver **333 pN** at 10 nm, and `C-0039` finds the
  real element needs **699 pN**.
- **`C-0017`'s own arithmetic says the desired clause's coupling is `100/10 = 10 pN/nm`** — a
  *different* device. `C-0046` shows the flexure **can** build it (arms 11.4–18.1 nm, 12 of 29 points
  clearing every element clause), and `C-0017`'s **stability floor refuses it at 2.34–2.79×**.
- **Composing the two clauses caps the stroke at 3.58–4.27 nm at 100 pN**, whatever the coupling is
  made of — a bound that names no coupling element at all.
  
**What deferring costs.** Every desired-stroke number in the corpus has to be quoted with the clause
that placed it, and a per-path allowable read at one clause is 3.33× the other. `C-0049` had to
withdraw a 40 pN/nm ceiling for exactly this reason.

---

## 5. What is the scaffold — linear or circular, M13 or synthetic, and how long? (`T-154`)

**The question.** §1 and §3 specify a 40 × 40 nm single-layer origami tile and never say what scaffold folds it.
Until iteration 17 that looked like an implementation detail. It is not: it decides whether the tile has a
**seam**, and a seam costs arm stations.

**Why the programme cannot decide it.** It is a choice of starting material and a fabrication route.

**What is established** (`C-0086`, `C-0081`).

- **A seam is a parity on a tree, not a fabrication convention.** Crossovers join only *adjacent* duplexes, so a
  single-layer sheet's row-adjacency graph is a **path** — a tree — and a closed walk on a tree traverses every
  edge an even number of times. A **fully folded circular** scaffold therefore gives every row two segments,
  i.e. exactly one seam. Brute-forced: the path graph carries **2 Hamiltonian paths and ZERO Hamiltonian cycles**
  at every width from 3 to 12 duplexes.
- **So a seam needs two premises — circular *and* fully folded — and the Gen-1 tile fails the second anyway**:
  the sheet takes **1 680** of M13's **7 249** nt.
- **A seam is not free.** `C-0081` finds it deletes a weave extremum, putting **6–12 of `C-0063`'s 34** arm
  stations off the node, and the parity that *closes* takes the worst across-row clearance to **0.122724 nm** —
  and to **−0.002276 nm** at the measured phosphate girth, which is a clash.
- **Three specifications, each with a built precedent read directly from Rothemund (2006).**

All three rows are `C-0086`'s Deliverable 2, and every figure in them is quoted from it:

| specification | seams | cost |
|---|---|---|
| **linear** (`C-0086`) | **0** | linearisation by BsrBI digestion dropped Rothemund's own yield from **63 %** well-formed to **11 %**, which he attributes to strand breakage during digestion. A **synthetic or PCR** scaffold of the right length avoids the digestion entirely |
| **circular, fully folded** (`C-0086`, `C-0081`) | **1** | the seam, i.e. 6–12 of 34 stations and `C-0081`'s restored amplitude bracket |
| **circular, with remainder** (`C-0086`) | **0** | the remainder itself: **5 569 nt**, a **33.3 nm** unpaired coil carrying **1.66×** the sheet's own charge in the actuated gap. A purpose-built 1 680 nt circular scaffold removes it and needs only a **67 nt** return loop |

- **And a seamless raster quantises the tile WIDTH at 32 bp.** Admissible row lengths are odd multiples of
  16 bp — **16, 48, 80, 112, 144** — and **§3's 40.0 nm = 117.6 bp is not among them**. The nearest is
  **112 bp = 38.08 nm**, a 4.8 % narrower tile, which every plan margin in the corpus would have to be re-read
  at (`T-153`).

**What the programme would recommend.** A **purpose-length scaffold** — synthetic, PCR, or a circular 1 680 nt
construct with a short return loop — because it is the only option that is seamless *and* avoids both the
digestion yield penalty and the unpaired remainder. But this is a wet-lab cost judgement about scaffold
sourcing, which the programme cannot price.

**What deferring costs.** `C-0081` and `C-0086` must both be carried in two readings, and `T-153` — a re-read of
every plan margin at 38.08 nm — cannot be scoped, because whether it is needed at all depends on this answer.

---

## 6. Does §3 admit a two-layer tile, or is single-layer a requirement? (`T-166`)

**The question.** §1 and §3 name a single-layer origami tile. Is that a requirement, or the simplest thing
that came to mind when the problem was written?

**Why the programme cannot decide it.** It is a statement about what the device is allowed to be.

**What is established** (`C-0087`, `C-0089`, `C-0093`).

- **The flat Gen-1 tile is a zero-defect result.** Under the only per-staple incorporation statistics anybody
  has measured, **no array coupling is flat**: the best 90th-percentile dishing over 22 designs is **0.2845**
  against `T-5b`'s 0.10, and what refuses it is a **count** — a dropout *is* an increase in the attachment
  pitch, so the density demanded is 195 paths against the 34 the plan admits.
- **The one structural escape is a coupling that is not an array** (`C-0093`): tying the tile to a **stiff
  shared body** moves the mandate into the body's ground, frees each tie by 3.4×, and is **2.05× flatter**
  than the array at zero defects on identical stations. Under the dropout it reaches **0.24028028** — the
  best this programme has achieved — and still misses, needing **252 ties against 53**.
- **But that body is multilayer origami.** A body tied at many out-of-plane sites is a square-lattice second
  layer, which is *the one motif in this programme with a published precedent* — against the single-tie
  out-of-plane arm the recommendation currently rests on, which has none in 62 recorded queries.
- **A buildable body is not the rigid one**: a four-layer honeycomb brick reads **0.100166871**, *worse* than
  the array. Body rigidity is first order, so a *yes* opens real work rather than a free win.

**What the programme would recommend.** Nothing yet — `T-165` has not been run, and it is the only unspent
axis (a distribution *and* placement search on the shared-body topology, needing 2.40× to reach the
convention). What the programme can say is that **a *no* closes the last open recovery route for a flat
tile**, and that is worth knowing before more is spent on it.

**What deferring costs.** `T-165` cannot be scoped, and the flatness answer stays *"flat as designed, not
shown flat as built"* with no route left to test.

---

## Two questions that were on this list and have been DISCHARGED

Recorded because a list that only ever grows is not a record.
`C-0071` found both had stopped applying and nobody had noticed —
they were raised by the flexure-and-tie branch that `CH-0081` removed from the output role.

| was | question | why it no longer applies |
|---|---|---|
| `T-95` | May the output superstructure be perforated under each flexure midspan? | The recommended element (`C-0071`'s `Q5`) has no tie grid, so nothing crosses the standoff base plane |
| `T-102` | May §3's tile grow by 1.44× in area? | Raised by the flexure array's minimum body; that array is no longer the output stage | 

---

## Two asks that are resources rather than decisions

**Access.** Two sources would convert a bound into a number and neither is reachable by an automated
fetch (ACS, Elsevier, Springer and IOP all refuse; Crossref and EuropePMC serve only abstracts):

- **Boucher & Hines, *J. Polym. Sci. Polym. Phys. Ed.* 14:2241 (1976)** — the only study measuring
  Group II chlorides against PEO, and so the only source for the θ-versus-[MgCl₂] curve *including
  the minima* that make a linear salting-out coefficient ill-posed.
- **Any PEG/PEO salt study below 50 mM.** `C-0007` found none; every cloud-point and aqueous-two-phase
  paper works at 0.1–3 M. This may simply not exist rather than be paywalled.
  
Neither is blocking: `C-0007` bounds the effect they would pin at **≤ 0.4 %** of the layer modulus and
states it as a falsifiable threshold rather than a guess.

**Compute.** `T-50` (above) and `T-51` (field-theoretic simulation of the grafted layer, costed at
weeks) both exceed what one session can run on the current box. `T-51` is **not** warranted — `C-0019`
bounds the layer response at ≤ 9.4 % without it. `T-50` is warranted only if decision 1 comes back as
"hold 2 mM".

---
Two things worth flagging about the document itself:

It answers a question you didn't ask. The four decisions were previously scattered across TASKS.md rows written for agents, not for a reviewer — each one buried in a paragraph of provenance. This form
separates what is being asked from what the programme found, which is what makes it reviewable in one pass rather than four.

The discharged section is deliberate. C-0071 found two questions had been carried to NDI for iterations after they stopped applying, and C-0067 found the same failure in ANSWERS.md's "cannot answer" list —
an entry that stood seven iterations after being answered. A questions-for-NDI file is exactly the kind of document that only ever grows, so it needs a place to record removals or it will drift the same
way.
