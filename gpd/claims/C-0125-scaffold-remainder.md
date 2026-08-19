# C-0125 — **THE FOUR-LAYER TILE HAS SPENT THE EXCESS, AND THE QUESTION REDUCES TO A BOUND.** Which remainder is real is a **literature** answer already in `gpd/data/`: the caDNAno paper folds every 60-helix block from **p7560**, and design **(ii) is the 10 × 6** cross-section `C-0120` recommends — so the remainder is **840 nt**, not `C-0109`'s 529 and not `C-0119`'s 1 344. The **whole** of it, Manning-renormalised, smeared onto the tile's own gap-facing plane — the closest place any of it can be — moves `σ_eff` by **0.0350791486**, against **0.537733246** on `C-0086`'s single-layer sheet: a **15.3×** reduction, **28.2×** like-for-like on M13mp18. The single-layer number would have owed a field solve; the four-layer number is inside `C-0008`'s own **7.2 %** charge-reading ambiguity. And the coil cannot be in the gap at all — the weakest slit penalty anywhere in the 4 × 2 ssDNA bracket is **6.64635939 `k_BT`**, so at most **0.15045831** of the remainder threads it. Over **all 21** of `C-0022`'s states the unconditional bound moves the load **0.134892067** and the held bias **0.156750765**; the penetration-limited estimate moves them **0.00489872699** and **0.00634332688**. The collar's **width** is `1/q₀` with `q₀² ≥ κ² + (π/2h)²` and carries **no surface charge at all**, so the 2-D edge solve is not re-run and that is an argument rather than an omission

> **Annotated, iteration 34 ([`C-0140`](C-0140-honeycomb-raster-turn-sense.md), [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md), [`CH-0180`](../challenges/CH-0180-the-scaffold-pairing-contradicts-its-own-paper.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **The Methods sentence this claim reads `READ DIRECTLY` contradicts its own paper's main text, at exactly one of seven designs — ours.**
> The main text states the rule: *"folded either from a 7560-base scaffold into **60 parallel helices** or from an 8064-base scaffold into **64 parallel helices**"*,
> and design (i) is `15 × 4` = **60** helices. The Methods list agrees with that rule at 6 of the 7 designs and disagrees only at (i).
> If the main text governs, design (i) is **p7560**, the remainder is **0** rather than 1 344 nt, and the row *"p8064 | designs (i), (iii), (v)"* is one design out.
> **The bound this claim delivers is unaffected in form** — it is a per-nucleotide bound and the remainder is an input — but its headline remainder is not established.

| | |
|---|---|
| **Task** | [`T-195`](../tasks/T-195-scaffold-remainder.md), re-queued from `T-154` by NDI's answer to decision 5 (2026-08-18) |
| **Leaf** | **`A7.4`** (the electrostatic load on the tile), with **`A8.2`** (what the tile is folded from) |
| **Verification type** | **literature** (the scaffold per design, **read directly** from a source already in `gpd/data/T-151-sources/`) **+ logical** (two closed-form cheap bounds, neither needing a solve) **+ in-silico** (a 1-D Poisson-Boltzmann re-read at **all 21** of `C-0022`'s operating states, whose nominal loads reproduce at **2.9e−9**) |
| **Verdict** | **PASS on all five predicates; none of the five falsifiers fired.** The deliverable the task offered as the alternative is the one that is owed: **the four-layer tile has spent the excess and the question reduces to a bound**, and the bound is given in the quantity `C-0022` is written on. |
| **Maturity** | **TRL 1–3. Nothing here is measured.** The scaffold lengths and the design-to-scaffold pairing **are** read directly from a published paper; every physical number is model-consistent and traceable. |
| **Provenance** | `gpd/results/T-195-scaffold-remainder.json`, produced by `electrostatics.ScaffoldRemainderStudyKt`; model in `src/main/kotlin/electrostatics/ScaffoldRemainder.kt` (**new file**); **30 tests** in `src/test/kotlin/electrostatics/ScaffoldRemainderTest.kt` (17) and `ScaffoldRemainderResultTest.kt` (13), the second taken on the **emitted** file; 9 budget records, 72 coil records, 192 confinement records, 216 saturation records, 42 bias re-reads, 28 reproductions, 2 convergence records, 5 predicates, 5 falsifiers, 6 findings. Re-run through `tools/study.sh` and diffed **byte-for-byte identical**; `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 33 s** — the whole suite on its own isolated tree, **no `--drop` and no `--drop-file`**. `tools/check-markdown-tables.py` and `tools/check-corpus-links.py` clean over the whole 370-file corpus. New retained tool `tools/check-kotlin-format-strings.py` (17 self-tests) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂** at 0.5 / 2 / 10 mM, a **2:1** electrolyte with `I = 3c`; `ε_r = 78`; Bjerrum length **0.714106611 nm**; gaps 2–10 nm; the tile negative, the electrode positive; ssDNA over the **whole** declared bracket — Kuhn 1.34 / 1.41 / 2.10 / 2.84 nm × contour 0.57 / 0.70 nm per nucleotide |
| **Consumes** | [`C-0022`](C-0022-tile-edge-load-profile.md) (its 21 states and their 1-D loads, **reproduced from its result file, not copied**), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the 2:1 saturation constants and the Stern series), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the single-layer budget and the coil, **both reproduced**), [`C-0109`](C-0109-four-layer-tile.md) (the four-layer budget), [`C-0119`](C-0119-honeycomb-raster-width.md) (the published cross-sections), [`C-0120`](C-0120-cross-section-comparison.md) (the recommended cross-section and its footprint), [`C-0023`](C-0023-two-sided-coupling.md)'s `SsDnaTether` (the Kuhn and contour brackets), [`C-0055`](C-0055-unused-junction-site.md) (`M13_SCAFFOLD_NUCLEOTIDES`) |
| **Raises** | [`CH-0147`](../challenges/CH-0147-the-recommended-tile-is-folded-from-a-different-scaffold.md), [`CH-0148`](../challenges/CH-0148-the-remainder-and-the-sheet-do-not-condense-alike.md) |

---

## The claim, in one line

**The question the queue asked — what a 33 nm, 1.66× polyanion does to an edge load solved with nothing there — was a question about a tile the programme no longer builds, and the tile it does build turns it into two closed-form bounds that need no field solve and no assumption about where the coil is.**

---

## 1. Which remainder is real, and it was four `grep`s

`CLAUDE.md` records that checking `gpd/data/` before fetching has now paid four times.
It paid a fifth: the whole specification half of this task is one passage of
`gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`, fetched by `T-151` **four iterations ago** for a different question.

> *"Each sample was prepared by combining 20 nM scaffold (**p7560 or p8064, derived from M13mp18**) …"*
>
> *"Recombinant phages were prepared by replacement of the BamHI-XbaI segment of M13mp18 by a PCR amplification fragment … The design: scaffold pairings are as follows: **i: p8064, ii: p7560, iii: p8064, iv: p7560, v: p8064, vi: p7560, vii: p7560**."*

`C-0119` established that the cross-section this programme builds is one of those seven designs.
`C-0120` then moved the recommendation from **(i) 15 × 4** to **(ii) 10 × 6** — and design (ii) is folded from **p7560**.

| scaffold | nt | provenance | remainder at 60 × 112 bp | occupancy |
|---|---|---|---|---|
| M13mp18, circular | 7 249 | CITED via `C-0055`; the scaffold `C-0086` and `C-0109` budget against | **529** | 0.927024417 |
| **p7560** | **7 560** | **READ DIRECTLY**; design (ii), i.e. **the recommended tile** | **840** | 0.888888889 |
| p8064 | 8 064 | **READ DIRECTLY**; designs (i), (iii), (v) — the cross-section `C-0120` replaced | 1 344 | 0.833333333 |

**All three are inside NDI's *"M13, circular ~7–8 K nucleotides"***, and all three are M13mp18 derivatives,
so nothing in the specification chooses between them. **The physics verdict below is the same at all three**;
the number is not, and that is an open question for NDI rather than a calculation.

### And a remainder is an artefact of the SPAN, not of M13

`7560 = 60 × 126` and `8064 = 64 × 126`, **exactly** — and the paper's acknowledgements thank a named person
*"for cloning the p7560 and p8064 scaffold vectors"*.
The standard practice in the very source this cross-section comes from is to **build the scaffold to the design**.
The Gen-1 tile carries a remainder because §3's ~40 nm forces a **112 bp** span where those two scaffolds were cut for **126**.
(The `× 126` reading is an inference from the two lengths and the two helix counts, not a sentence in the paper; the cloning is a sentence in the paper.)

---

## 2. The charge ledger — and the two bodies do not condense alike

`C-0086` compared the remainder and the sheet on **bare** charge. They are different molecules:

| body | one charge per | `q ξ_M` | surviving under Mg²⁺ |
|---|---|---|---|
| the sheet (duplex) | **0.17 nm** of axis | 8.40 | **0.119029846** |
| the remainder (ssDNA) | **0.57 nm** of contour | 2.506 | **0.399100072** |
| the remainder (ssDNA) | **0.70 nm** of contour | 2.040 | **0.490122896** |

**3.353–4.118× more of the remainder's charge survives, per nucleotide.** That is [`CH-0148`](../challenges/CH-0148-the-remainder-and-the-sheet-do-not-condense-alike.md).

| tile | scaffold | remainder | **bare** ratio | **Manning** ratio |
|---|---|---|---|---|
| single layer, 15 × 112 | M13mp18 | 5 569 | **1.65744048** (`C-0086`'s 1.66) | **5.55730042–6.8247549** |
| single layer, 15 × 112 | p8064 | 6 384 | 1.9 | 6.37058824–7.82352941 |
| **four layer, 10 × 6** | **p7560** | **840** | **0.0625** | **0.209558824–0.257352941** |
| four layer, 10 × 6 | M13mp18 | 529 | 0.039360119 | 0.131972164–0.162071078 |
| four layer, 10 × 6 | p8064 | 1 344 | 0.1 | 0.335294118–0.411764706 |

**The thickness §3 states is what pays for the remainder.** `C-0109`'s finding — one circular M13 pays for exactly
four layers — has a consequence nobody counted:
the tile's own backbone charge is multiplied by four while the remainder divides by 10.5,
so at the same scaffold the ratio falls **42.1×**, 1.65744048 to 0.039360119.

---

## 3. Cheap bound 1 — saturation, and it settles the question

`CLAUDE.md`: *"a charge-saturated surface makes its own charge ambiguity irrelevant — check for saturation
BEFORE spending an iteration resolving a charge model."* The check is one composition of two closed forms
this repository already carries, and it runs before any solve.

Smear the **entire** remainder onto the tile's gap-facing plane. That is worst in every argument at once:
the whole chain, at its largest surviving fraction, on the closest plane it could occupy.

| wall | its own worst added charge, `e/nm²` | added / bare | **worst `σ_eff` movement** |
|---|---|---|---|
| `C-0086`'s single-layer sheet | 2.03637328 | 15.6470588 | **0.537733246** |
| `C-0022`'s as-solved 40 × 40 tile | 0.411703233 | 1.03270412 | **0.0830525876** |
| `C-0119`'s 15 × 4 four-layer tile | 0.454743826 | 0.823529412 | 0.0528500891 |
| **`C-0120`'s 10 × 6 four-layer tile** | 0.682115739 | 0.823529412 | **0.0350791486** |

- **15.3291419×** reduction, worst-over-scaffolds; **28.2058889×** like-for-like on M13mp18
  (0.53161011 → 0.0188474865).
- The recommended tile's **0.0350791486** is inside `C-0008`'s own **0.072** charge-reading ambiguity,
  a fifth of `C-0034`'s **0.147** fringing, and two orders inside `C-0005`'s **1.23–2.14** mean-field bracket.
- The single-layer sheet's **0.5377** is **not** bounded away and would have owed the field solve.

**A doubling of the bare charge is a 3.5 % movement of the far field**, because the wall is already at
0.966331968 of its 2:1 saturated amplitude at 2 mM. That is the whole of `C-0008`'s finding, used.

---

## 4. Cheap bound 2 — the coil cannot be in the gap, and that needs no placement assumption

§3 fixes **no attachment point** for the scaffold's unpaired arc, so *where* the coil sits is not determined by
anything in the specification. It does not have to be: an ideal chain in a slit of width `d` pays
`π² R_g²/d²` and a swollen one `(R_F/d)^{5/3}`, and the coil is far larger than the gap at **every** corner of
the declared ssDNA bracket.

| the 840 nt remainder | over Kuhn 1.34–2.84 nm × contour 0.57–0.70 nm/nt |
|---|---|
| contour length | 478.8–588.0 nm |
| ideal `R_g` | **10.3–16.7 nm** |
| swollen `R_F` | **42.5–65.3 nm** |
| gap | **2–10 nm** |

Over all 192 combinations of remainder, gap, Kuhn length and contour, the **weaker** of the two penalties is
**6.64635939 `k_BT`** at its weakest — which is the *largest* gap and the *smallest* remainder, i.e. the corner
most favourable to the coil. At most **0.15045831** of the remainder threads the slit, and at the recommended
tile's widest gap that is **50.8 nucleotides** of 840.

Both omitted terms are **positive**: the chain's own electrostatic self-repulsion, and the grafted PEG layer it
would also have to displace. The expulsion is understated, not overstated.

---

## 5. The bias and the edge load, re-read at all 21 of `C-0022`'s states

The gate first: the 1-D pipeline reproduces **every one** of `C-0022`'s 21 published one-dimensional loads at a
worst relative departure of **2.9e−9**, and its tile charge comes out at `C-0022`'s own `−0.398665238 e/nm²`
without being copied. `F2` did not fire.

Two perturbations, both applied to `C-0022`'s **own** charge and footprint — which is the conservative pairing,
because the four-layer tile's gap-facing charge is 2.08× larger and the same addition is a **smaller** relative
perturbation there:

| scenario | added charge, `e/nm²` | worst load movement | worst held-bias movement |
|---|---|---|---|
| **unconditional bound** — the whole p8064 remainder on the gap-facing plane | 0.411703233 | **0.134892067** | **0.156750765** |
| **penetration-limited** — the p7560 remainder's own threading count | ≤ 0.0126683245 | **0.00489872699** | **0.00634332688** |

The unconditional bound's 0.135 sits against the **+0.56** the Bikerman finite-ion-size correction `C-0008`
already carries on the same force, and it is a state the confinement bound weights at `e^{−6.6}` at best.

### Why the 2-D edge solve is NOT re-run, and why that is an argument

`C-0022`'s collar **width** is `1/q₀` with `q₀² ≥ κ² + (π/2h)²` — `transverseDecayRateBound(κ, h)`, whose
signature admits **no surface charge at all**. Within linear theory a uniform change of the tile's charge
therefore cannot move the collar's width; it moves only the **level**, and a level change at a force-pinned
operating point is absorbed by the bias (`CLAUDE.md`'s force-pinned rule — the same rule that makes a force
multiplier contribute exactly zero to `k_es` at a held point). Re-running a 2-D solve to measure a quantity a
closed form says is exactly zero is the expensive way to learn nothing. The collar-width ceilings are emitted
per state so the reader can see they are equal.

---

## 6. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a remainder is an integer and conserves the scaffold; a smeared charge density goes as `1/area`; a confinement free energy as `1/d²`; a penetration count as `d²`; unphysical arguments throw at nine entry points; on the **emitted file**, `added/bare` and `occupancy` reconstruct from their own fields | **PASS** |
| **2 — limiting cases** | Manning condenses nothing at `q ξ_M ≤ 1`; the confinement penalty vanishes as the slit opens; the penetration count caps at the remainder; **doubling a saturated wall's bare charge moves `σ_eff` under 5 %**; no `σ_eff` in 216 records reaches its own ceiling | **PASS** |
| **3 — symmetry and conservation** | the scaffold is conserved across every cross-section; the penetrating subchain, fed back through the **independently written** ideal law, costs exactly 1 `k_BT`; `σ_eff` is monotone in `σ` and bounded by saturation over 40 decades of charge; on the emitted file, **every** `σ_eff` movement is smaller than the bare movement that produced it | **PASS** |
| **4 — numerical convergence** | the worst-case load movement at 2 mM, 10 nm settles: **2.3e−4** at 400 → 800 nodes and **8.3e−5** at 800 → 1600, and the departure falls; the ratio is taken at **matched refinement**, which divides out the model, the ion statistics and `C-0005`'s bracket | **PASS** |
| **5 — literature and upstream** | **28 reproductions.** All 21 of `C-0022`'s 1-D loads at ≤ 2.9e−9; `C-0086`'s 5 569 nt exactly, its 33.3 nm coil at 9.6e−4 and its 1.66× at 1.5e−3 (both quoted to three digits); `C-0109`'s 529 nt and 0.927 occupancy; `C-0119`'s 1 344 nt; `C-0008`'s 0.04562 `e/nm²` at 6.5e−5. The scaffold lengths and the design pairing **read directly** | **PASS** |

### The declared falsifiers

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the worst-case `σ_eff` perturbation exceeds `C-0022`'s standing model spreads | **no** | 0.0350791486 against 0.072; the field solve is not owed |
| **F2** | `C-0086`'s coil and `C-0022`'s 21 loads do not reproduce | **no** | worst load departure 2.9e−9 |
| **F3** | the coil is smaller than the gap somewhere in the ssDNA bracket | **no** | the weakest expulsion is 6.64635939 `k_BT` |
| **F4** | the re-read moves further than `C-0008`'s own Bikerman bracket | **no** | 0.134892067 against 0.56 |
| **F5** | the four-layer tile does not reduce the exposure | **no** | 15.3291419× worst-over-scaffolds, 28.2058889× like-for-like |

---

## 7. A process finding, retained as a tool

`CLAUDE.md` records the `+`-binds-tighter-than-`.format()` trap five times, and its own remedy —
*"count `%` conversions over the whole parenthesised concatenation against the top-level commas"* —
had never been mechanised. It was written as `tools/check-kotlin-format-strings.py` (17 self-tests) **because it
caught a live instance in this task's own study before the run**, and swept over `src/`:

**14 defects in 6 files, from 5 studies, all committed** — and the raw conversions have reached
**8 committed result files**. The sharpest is `coupling/PathCountFixedGeometryStudy.kt:1067`, whose emitted
`T-163` conditions block reads

> `"tile": "40.0 x %.2f nm single-layer square-lattice sheet, %d duplexes at the SAXS-measured 40.35 nm"`

— two raw conversions, **and the surviving number is wrong**: 40.35 nm is the tile's across-helix length printed
where the SAXS-measured **2.69 nm** interhelical distance belongs. Java's `String.format` silently ignores extra
arguments, which is exactly why this class survives: it does not throw.

**This claim does not repair them.** The repair moves 8 result files and the claims that quote them, which is a
task, not a paragraph. `T-195`'s row names it as a successor.

---

## Still open

1. **Which scaffold NDI buys.** 7 249 / 7 560 / 8 064 give 529 / 840 / 1 344 nt, and all three satisfy
   *"M13, circular ~7–8 K"*. The verdict does not move; the number does.
2. **Whether a cloned scaffold at the tile's own length is available.** The caDNAno paper cloned two, so it is
   standard practice in this cross-section's own source — but it is a purchase decision.
3. **Folding yield of a four-layer honeycomb block with a remainder.** Rothemund's favourable measurement is on
   a single-layer rectangle; no measurement on a multilayer block was found and none was searched for here.
4. **Where the coil goes once expelled.** It is not in the gap; whether it sits above the tile and competes with
   the output coupling's plan is a packing question on the **other** face, and it is not asked here.
5. **The 14 format-string defects and the 8 result files carrying raw conversions.**
