# CH-0084 — `T-45`'s tolerance has been measured all along, and the flat design is on the wrong side of it: a Rothemund rectangle's staple incorporation is **48 % at the edges and 84 % on average** over all 168 staples, which as a per-path dropout is **43.6 %** relative stiffness scatter against `C-0060`'s **34.6 %** flatness threshold and `C-0026`'s **17 %** break-even — and the same dropout is a **16 %** shortfall on `C-0017`'s mandate, **2.9×** the worst rounding error `C-0060` calls a placement error. **The position dependence runs the wrong way for `C-0058`**, which puts 34 of its 45 stations on the rim

| | |
|---|---|
| **Raised by** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md) (`T-134`) |
| **Against** | [`C-0060`](../claims/C-0060-buildable-stiffness-ratio.md)'s tolerance verdict — *"the built design loses the flatness verdict at **34.6 %** relative scatter … **6.9× the 5 % a staple design might plausibly hold**"* — and, through it, [`C-0058`](../claims/C-0058-non-uniform-coupling.md)'s flat two-level distribution and [`C-0017`](../claims/C-0017-output-coupling-stiffness.md)'s mandate as an achievable **sum** |
| **Grounds** | **a measurement exists and neither claim looked for it.** `C-0060` records `T-45` as *"still unmeasured"* and reasons about *"the 5 % a staple design might plausibly hold"*, which is an assumption with no source. Strauss et al., *Nat. Commun.* **9**:1600 (2018) map incorporation efficiency at **single-staple resolution across all 168 staples of a Rothemund rectangle** by DNA-PAINT, corroborated by next-generation sequencing, and report **48–95 %, mean 84 %**. A missing staple does not perturb a load path's stiffness — it **removes** it, so the population is two-valued and its relative standard deviation is `√(f/(1−f))` with `f = 0.16`, i.e. **43.6 %** |
| **Severity** | **`C-0060`'s tolerance clause and `C-0058`'s flatness verdict, not `C-0060`'s stiffness result.** Everything `C-0060` establishes about *buildability* — the 25×-finer quantum, the seven catalogue settings reaching both levels, the 4.667–5.144 realised ratios, the one-base-pair trim — is untouched and reproduces. What falls is the reading that the tolerance is *generous*. And the direction is unambiguous: `C-0060`'s own threshold is the number the measurement is graded against, and the measurement is **1.26×** past it |

---

## What is claimed upstream

`C-0060` (`T-122`, iteration 10) delivers `T-45` as a **threshold** and says so plainly:

> *"**The tolerance is a threshold, and it is generous**: the built design loses the flatness verdict at **34.6 %** relative scatter on its worst pattern, **2.04×** `C-0026`'s 17 % break-even, and the two populations do not even overlap until **66.7 %** — so **flatness binds at half the amplitude the ordering does**."*

and, in its own reading of that number:

> *"A build tolerance would have to be twice `C-0026`'s break-even before the flat verdict is lost, and **6.9× the 5 % a staple design might plausibly hold**."*

and, in *Still open*:

> *"**`T-45` itself**: what relative scatter a staple-designed attachment array can be *guaranteed* to. This claim delivers the threshold the design tolerates — 34.6 % — and **nothing accessible gives the spread an assembly achieves**."*

`C-0026` supplies the other threshold, 17 %, at 0.883 pN per unit relative amplitude, *"precisely so a measured or specified tolerance can be substituted without re-running."*

**The `TASKS.md` row for `T-45` has carried *"nothing accessible gives the stiffness spread of nominally identical hybridised staple extensions"* since iteration 3.**

## What the measurement says

**Strauss, Schueder, Haas, Nickels, Jungmann, *Nat. Commun.* **9**:1600 (2018)** — read directly from EuropePMC's full text (`PMC5913233`), not from a search summary. Abstract, verbatim:

> *"We find that strand incorporation strongly correlates with the position in the structure, **ranging from a minimum of 48 % on the edges to a maximum of 95 % in the center.**"*

Results, verbatim:

> *"this translates to **absolute incorporation efficiencies of 48–95 % with an average of 84 %**, in good agreement with qualitative results of relative staple abundance from next-generation sequencing."*

Corroborated by the founding paper itself — **Rothemund, *Nature* **440**:297 (2006), read directly**: *"**94 % of '1' pixels (of 1,080 observed) were visualized.**"*

## The composition

A staple either forms or it does not. A load path whose staple is absent has stiffness **zero**, not a perturbed stiffness — so the population is Bernoulli, and its relative standard deviation is exact:

&nbsp;&nbsp;&nbsp;&nbsp;`σ_rel = √(f/(1 − f))`, &nbsp;&nbsp; `f = 1 − (incorporation efficiency)`

| reading | efficiency | dropout `f` | implied `σ_rel` | vs `C-0026`'s 17 % | vs `C-0060`'s 34.6 % |
|---|---|---|---|---|---|
| **Strauss mean, all 168 staples** | **0.84** | **0.16** | **43.6 %** | **2.57×** | **1.26×** |
| Rothemund's own pixel yield | 0.94 | 0.06 | 25.3 % | 1.49× | 0.73× |
| **Strauss, EDGE sites** | **0.48** | **0.52** | **104.1 %** | **6.12×** | **3.01×** |
| Strauss, centre sites | 0.95 | 0.05 | 22.9 % | 1.35× | 0.66× |
| **the efficiency `C-0060`'s threshold implies** | **0.893** | **0.107** | 34.6 % | 2.04× | **1.00×** |

**Three statements, and the third is the one that matters most.**

1. **The measured mean is past `C-0060`'s own threshold by 1.26×.** `C-0060`'s verdict *"the tolerance is generous … 6.9× the 5 % a staple design might plausibly hold"* rests on a 5 % assumption with no source; the measurement is **8.7×** that assumption. The threshold `C-0060` published is exactly the right instrument — it is the number the measurement is graded against, which is what `C-0060` said it was for — and the grade is a fail.
2. **The same dropout is a placement error on `C-0017`'s mandate, and a large one.** `C-0060` establishes that the mandate is an **equality on a sum**, and treats a **0.40–5.44 %** miss from rounding the two levels as *"a placement error, not a rounding nuisance"*, spending a one-base-pair trim to take it to `1.3e−4`. A 16 % mean dropout takes the realised total from 33.3333 to **28.00 pN/nm** — a **16 %** miss, **2.9×** the worst rounding error, and a trim cannot recover it because the missing paths are not there to trim.
3. **The position dependence runs the wrong way for `C-0058`.** Strauss finds incorporation **worst at the edges**. `C-0058`'s flat design puts **34 of its 45 stations on the rim** and gives them the **stiff** level — so the sites carrying `34/45` of the coupling and 5× the per-path stiffness are the sites the measurement says are least likely to form. That is a **correlated** dropout aligned with the design's own rim/interior axis, which is precisely the axis [`CH-0073`](CH-0073-the-along-helix-scatter-rule-reverses-on-a-non-uniform-coupling.md) showed the flat design tolerates **least** — 31.6 % with the mandate held, against 69.8 % across the helices.

## What this does NOT touch

- **`C-0060`'s stiffness answer is untouched and reproduces.** Both levels are reachable by all seven catalogue settings; the coarsest quantum is 19.1 % of a level against a 471 %-wide window; the realised ratios are 4.667–5.144; the one-base-pair trim takes the mandate miss to `1.3e−4`. None of that is a tolerance statement.
- **`C-0060`'s 34.6 % and `C-0026`'s 17 % are not wrong** — they are the thresholds, and this challenge is them being used for the purpose they were computed for.
- **The array finding stands**: a mixed-span array still places 0 of 45 at `C-0030`'s interior span.
- **`C-0060`'s "small scatter helps"** (0.0767 → 0.0571 at 10 %) is untouched, and is why a linearised budget would get the sign wrong at small amplitude. It says nothing at 43.6 %.

## The honest qualifications, stated before they are asked for

1. **This is a translation, not an equivalence.** A Bernoulli dropout and `C-0060`'s alternating scatter pattern have the same relative standard deviation and **different spatial structure**, and `C-0060` measures the pattern at **2.21×**. So the 1.26× is indicative on the flatness channel; what is *not* pattern-dependent is statement 2 — the 16 % mandate shortfall — which follows from the mean alone.
2. **A staple is not a load path.** Strauss measures *staple* incorporation on a plain rectangle; a Gen-1 coupling path is a designed element (`C-0055`'s arm on one crossover, `C-0030`'s flexure) whose incorporation nobody has measured. The claim here is that a per-site incorporation of 84 % is the **right order** and the **only measured one**, not that it is the coupling's own number.
3. **The measurement is of a plain Rothemund rectangle at one folding protocol.** `C-0060`'s design is not that object. Incorporation is improvable — Strauss's own point is that it is *"position-dependent"* and therefore addressable — and this challenge is a statement about the state of the art, not a physical limit.
4. **It cuts the other way at the centre.** Centre-site incorporation (95 %, `σ_rel` = 22.9 %) is **inside** `C-0060`'s threshold. A design that used only interior stations would pass — and `C-0058`'s does the opposite.

## What would settle it

- **A per-site incorporation measurement on a coupling-bearing tile**, by the method Strauss already demonstrates (DNA-PAINT with single-staple resolution, or next-generation sequencing of staple abundance). This is the measurement `T-45` has been waiting three iterations for, and it is *routine*.
- **Re-running `C-0060`'s flatness sweep under a Bernoulli dropout rather than an alternating amplitude**, with the dropout probability position-dependent as Strauss measures it. That is the pattern-correct version of statement 1 and it needs no new physics — only `C-0060`'s own pipeline with a different perturbation.
- **A design that does not put its stiff level on the rim.** `C-0058`'s 5:1 rim/interior split is what aligns the coupling with the measured failure gradient; whether a flat distribution exists that does not is `C-0058`'s own search under a new objective.
