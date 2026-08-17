# DECISIONS-FOR-NDI

Four decisions this programme cannot make for itself.

Each is a **specification** question rather than a modelling one:
no amount of calculation closes it,
because the thing that is missing is a statement of intent about the device,
not a number about the physics. 
They are collected here so they can be reviewed together and answered in one pass,
rather than one at a time across iterations.

They are live in [`TASKS.md`](TASKS.md) as `T-63`, `T-115`, `P-13` and `T-112`,
and this file is the reviewable form of the same four.

## How to read this

For each decision: what is being asked, why the programme cannot decide it,
what has been established either way, the options with their consequences,
what the programme would recommend and on what grounds,
and **what it costs to leave it open** — which is the part that is easy to miss.

**A one-sentence answer is enough for all four.**
None needs a document; three of them need a single word.

**Nothing here is measured.** Every number is TRL 1–3:
model-consistent and traceable to a claim, never empirically demonstrated.

## At a glance

| # | Decision | Owner task | Cost of deferring | Programme's recommendation |
|---|---|---|---|---| 
| 1 | Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer, instead of §3's 2 mM? | `T-63` | The device sits **on** its own pull-in fold at 2 mM; the alternative is 1–3 weeks of Monte Carlo | **Adopt 0.5 mM** |
| 2 | May the polymer layer be taller than 10 nm — 17–26 nm? | `T-115` | §3's *desired* ~10 nm stroke stays unreachable on §3's own stack | **A yes/no is enough**; a *no* closes a branch cheaply |
| 3 | What is the electrode made of, and where is its potential of zero charge? | `P-13` | 2.6× on the one hold-down no design can remove | **Name a material**; the programme will not choose one |
| 4 | Which device does §3's *desired* clause ask for? | `T-112` | Every desired-stroke number in the corpus has to be quoted twice | **One device, placed at the acceptable clause** |

---

## 1. Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer? (`T-63`)

**The question.** §3 specifies 2 mM MgCl₂. Leaf `A2.2` names a low-screening operating point.
Six independent routes in this programme have converged on **0.5 mM**,
and as of iteration 5 it stopped being an improvement and became a requirement for the surviving coupling.

**Why the programme cannot decide it.** Buffer composition is a specification of the experiment,
not an output of the model.

**What is established.**
  
- **Six independent routes recommend it**: `C-0012` on the force clause, `C-0016` on the bias window,
  `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin,
  and `C-0032` on the realised coupling law.
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
