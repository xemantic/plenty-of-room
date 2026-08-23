# C-0199 — **THE cadnano.org GALLERY IS STILL LIVE, ITS THREE ARCHIVES STILL DOWNLOAD, AND THEY CARRY 26 caDNAno DESIGN FILES — 15 OF WHICH FORCE A CROSSOVER. THE CATEGORY IS NOT EMPTY AND THE CONJUNCTION STILL FAILS, BECAUSE THE ONLY PER-DESIGN YIELDS ANYBODY HAS PUBLISHED FOR A GALLERY DESIGN BELONG TO THE SEVEN BLOCKS THAT FORCE NOTHING.** `C-0152` §6 read the gallery **page** and did not open the `.zip` files; opened, they resolve through `bit.ly` to live Dropbox archives and unpack into the field's own record. The forcing splits into **three mechanisms and only one of them is `C-0152`'s object**: **425** strand connections joining two helices at *different* base indices, **323** crossings between sites that are not nearest neighbours, and **28** crossings that are aligned, adjacent and simply off-register. **Every one of those 28 is displaced by ONE base pair** (`34.2857143°`) **and not one by the TEN or ELEVEN `C-0152` proves is the cheapest** (`17.1428571°`) — so the field, forcing a crossover 28 times, pays **4×** the energy the lattice offers, every time, which is `C-0152`'s own *no count of base pairs can see the azimuth* observed in the record rather than derived. The yield question is answered and the answer is a partition: the caDNAno paper publishes **per-design** gel and TEM yields (**3.2–25.2 %** and **2.0–53.8 %**) for its seven blocks, **all seven of which carry zero forced crossovers on all three tests**; the Nature paper publishes a **pooled `7 %–44 %`** over a shape set that mixes forced and unforced designs, with no map in the paper or its SI. **0 of 15**, one-sided 95 % upper limit **0.181036273**

| | |
|---|---|
| **Task** | [`T-255`](../tasks/T-255-cadnano-gallery-forced-crossovers.md), raised by [`C-0152`](C-0152-forced-scaffold-crossover-price.md) (`T-246`) §6 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (parsing 26 **foreign** design files with three independent tests built on caDNAno's own published rule, 37 self-tests written first) **+ literature** (the gallery, its archives and the three papers it cites, every number flagged for how it was read; one bar chart digitised at 600 dpi with the paper's own ordinal statements as cross-checks) |
| **Verdict** | **PASS on all six predicates.** Of the seven declared falsifiers **three FIRED**: `F3` (the category is not empty), `F5` (the honeycomb designs' raster turns **are** bound by the `±5 bp` condition — which is what raises the challenge) and `F7` (the conjunction's expected yield is below one). `F1`, `F2` and `F6` did not fire; **`F4` did not fire and its not firing IS the answer**. Raises [`CH-0251`](../challenges/CH-0251-the-deposited-block-has-no-loops.md) against [`C-0193`](C-0193-the-built-turn-is-a-tether.md) §3/§4 |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED IN THIS PROJECT.** Every yield quoted is a published measurement of somebody else's folding experiment, read directly and flagged. The forced-crossover census is a statement about **design files**, not about folded objects |
| **Provenance** | [`gpd/results/T-255-cadnano-gallery-forced-crossovers.json`](../results/T-255-cadnano-gallery-forced-crossovers.json), written by [`tools/T-255-emit-result.py`](../../tools/T-255-emit-result.py) (**new**, no Kotlin source touched), **byte-identical across two independent runs**, `--selftest` **13 checks, 0 failed**. Parser, classifier, digitiser, retrieval driver, full HTTP log and the three archives **unmodified** at [`gpd/data/T-255-sources/`](../data/T-255-sources/MANIFEST.md); `forced_census.py` carries **37 named assertions** |
| **Conditions** | Honeycomb, 21 bp crossover period, 3 azimuth classes at 7 bp, caDNAno scaffold offset `±5 bp`, 10.5 bp/turn so `240/7 = 34.2857143°` per base pair; rise 0.34 nm/bp. **All four lattice constants are PARSED out of [`tile/HoneycombBondClassResidues.kt`](../../src/main/kotlin/tile/HoneycombBondClassResidues.kt)**, not transcribed. The square-lattice comparison period (32 bp, 8 bp step) is `CLAUDE.md`'s own, cited in the source. Counts are dimensionless; a yield is a **percentage of a stated denominator and the denominator travels with it**. No buffer, temperature or environment coordinate enters, so the result file's `regime` is `[]`. Retrieved **2026-08-23**; nothing installed but `poppler-utils`, already present |
| **Consumes** | [`C-0152`](C-0152-forced-scaffold-crossover-price.md)/`T-246` (the question, the azimuth ladder, and the retained gallery captures), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md)/`T-244` (the `±5` closure rule, re-derived per bond), [`C-0193`](C-0193-the-built-turn-is-a-tether.md)/`T-296` (the conditionality this task was told to re-price) |
| **Raises** | [`CH-0251`](../challenges/CH-0251-the-deposited-block-has-no-loops.md) |

---

## The claim, in four lines

The gallery the caDNAno paper names as its own record of *"designs that folded successfully,
although with varying yields"* is still live, and its three archives still download.

They hold **26** caDNAno legacy design files. **15** of them force a crossover, by three
mechanisms; **4** force one of the kind `C-0152` prices, **28** crossings in all, and **every one
is displaced by one base pair** where the lattice's cheapest departure is ten or eleven.

Per-design yields exist — for the **seven** blocks that force **nothing**.

So `C-0152`'s negative existence result survives, on a **sharper** ground than the one it was
published on: the designs are there, the forcings are there, and **nobody has ever published a
yield beside one**.

---

## 1. The cheap bound, and it was right about the target and wrong about the category

`T-255`'s task file states it before any byte was fetched: `C-0193` §4 holds that an unpaired
scaffold base has no azimuth, so caDNAno's `±5 bp` residue condition **cannot bind a raster turn
flanked by unpaired scaffold** — and the built honeycomb blocks are recorded as allotting 28 such
bases per helix. If that held of the gallery designs, the category would be empty by construction
and no census would be worth running.

**It narrowed the target correctly and it did not empty the category — and `F5` fired.** Measured
on the deposited files, `10` of the 26 designs carry **zero** unpaired scaffold — including all
seven caDNAno blocks and the Nature monolith — so the exemption does not apply to them at all, and
their raster turns sit on the `±5` lattice exactly, at `0` forced of 118 (or 126). The other
sixteen carry forcings that are not raster turns. **A cheap bound that turns out to be false is
still the right thing to run first**: it cost one census column, it was declared before the fetch,
and it is what makes `CH-0251` a measurement rather than a suspicion.

The second cheap bound also held: conjunct 4 cost **nothing new**, because all three cited papers
were already in `gpd/data/`, and the third — *check `gpd/data/` first* — paid for the **fifth**
recorded time: the archives' resolved URLs were already on disk in `T-246`'s 2012 Wayback capture.

## 2. Retrieval — `F1` and `F2` did not fire

| what | route | status |
|---|---|---|
| `cadnano.org/gallery.html` | live | **200**, identical in substance to `T-246`'s |
| Science 2009 archive | `http://bit.ly/U1CwqS` → Dropbox `Science09.zip` | **200**, 777 635 B, **12** designs |
| NAR 2009 archive | `http://bit.ly/XJvTOI` → Dropbox `NAR09.zip` | **200**, 444 985 B, **7** designs |
| Nature 2009 archive | `http://bit.ly/WJPCI1` → Dropbox `Nature09.zip` | **200**, 3 589 234 B, **7** designs |

Seventeen years after publication, three URL shorteners and a file host that did not exist when
the paper was written all still resolve. **26 machine-readable caDNAno legacy `.json` designs**,
and nothing in any archive is a picture. Every URL tried, including the two that failed, is in
[`fetches.json`](../data/T-255-sources/fetches.json) with its HTTP status.

## 3. Three tests, and they fail differently — so they are reported separately

caDNAno's rule, quoted in this repository's own Kotlin source from the caDNAno paper:

> *"Our default rules allow antiparallel crossovers between **adjacent** staple helices only where
> the strand backbones arrive at **points of closest proximity**, which repeat every 21 base pairs
> … potential staple-crossover positions occur every seven base pairs … Our default rules allow
> antiparallel crossovers between adjacent **scaffold** helices to occur **five base pairs** …
> upstream or downstream of allowed crossover positions for the associated staple helices."*

| test | what it reads | needs |
|---|---|---|
| **A — adjacency** | *"between **adjacent** helices"*. A crossover between two sites that are not nearest neighbours on the design's own lattice | pure geometry: no residue, no datum, no twist |
| **B — register** | a crossover **site** occupies two consecutive bases, so a crossing at base `i` is allowed iff `i mod 21 ∈ {r, r+1}` for the **bond's own** site residue `r`; the scaffold window is that window shifted both ways by five | one modular subtraction **per bond**; a datum shift moves every crossing alike, so the verdict is convention-free |
| **C — alignment** | *"points of closest proximity"*. An antiparallel crossover joins two helices at the **same** base index; a connection between different indices is a manual one, and its axial offset is a number the file states | pure integer comparison |

**Test B's blind spot is declared rather than discovered**: a *lone* crossing displaced by exactly
`+1 bp` lands inside the window, so **B is a lower bound**. A *doubled* crossover displaced by 1 bp
is caught, because its two crossings are `r+1` and `r+2`.

**Testing the crossing residue rather than the site level is what removes the last convention.**
caDNAno renders one crossover as **two** strand crossings at `(o, o+1)` — `CLAUDE.md` records the
same doubling in the field's own generator — and a staple nicked at one half, or a raster turn at a
helix end, leaves exactly one crossing behind. Which half a lone crossing is, is a convention;
whether its residue is in the two-wide window is not.

**`F6` did not fire, on a SUBSTITUTED calibration and the substitution is the stronger one.**
`T-255`'s plan named this repository's own committed `102 / 109` honeycomb design as the
calibration; that design is a **scadnano** file and this is a **caDNAno legacy** reader, so the
two do not meet and no conversion exists here. What the classifier was calibrated against instead
is the **tool author's own** `10 × 6` block, on which it returns **77 of 77** staple bonds fitting
one window, three site residues **`[6, 13, 20]`** exactly seven apart, and **zero** forced
crossovers of any kind. `CLAUDE.md`: *the field's own generator is the right calibration for a
generalised rule* — a design deposited by the people who wrote the rule is a stronger test of the
rule than a design this repository drew to obey it.

## 4. The census — `F3` FIRED

**15 of 26 designs carry a forced crossover.** Over the whole gallery: **17 837** staple crossings
and **4 069** scaffold crossings, of which

| mechanism | crossings | designs |
|---|---|---|
| **C** — a connection between two **different** base indices | **425** | **13** |
| **A** — a crossover between **non-adjacent** sites | **323** | **14** |
| **B** — aligned, adjacent, and **off-register** | **28** | **4** |
| — unscorable (a scaffold bond with no staple crossover to score against) | 59 | 8 |

The two big mechanisms are not `C-0152`'s object at all. A connection with an axial offset of
`|2|` to `|323|` base pairs is not a lattice crossover; nor is one between sites 4, 8 or 16 columns
apart. Both are the caDNAno *forcing tool* doing what the paper says it does — *"deviations from
the basic honeycomb architecture … to create very complicated designs"* — and the icosahedron
alone accounts for **324 + 234** of them.

**Mechanism B is `C-0152`'s object, and it is small and uniform.**

| design | forced | departure | what the paper calls it |
|---|---|---|---|
| `slottedcross` | 23 scaffold | `−1 bp` ×19, `+1 bp` ×4 | *"the two domains are connected by a pair of Holliday-junction crossovers derived from the scaffold strand"* |
| `stackedcross` | 3 scaffold | `+1 bp` ×3 | *"four sub-modules that each are connected to the C-shaped domain by a Holliday-junction crossover derived from the scaffold strand"* |
| `doublegear` | 1 scaffold | `+1 bp` | — |
| `railedbridge` | 1 staple | `+5 bp` — the bond's own **scaffold** position | — |

**Every register-forced crossover in the entire gallery is displaced by one base pair**, except one
staple crossover placed at exactly the half-turn a scaffold crossover would take. Four of the 28
are **doubled**, so their site departure is exactly `+1 bp` with no ambiguity at all; the other 24
are lone crossings, whose site departure is therefore `1` or `2 bp`.

## 5. And that is a measurement against `C-0152`, on the axis `C-0152` derived

`C-0152` proves that the residue→azimuth map folds, so the **smallest** departure the 21-residue
lattice offers is `17.1428571°` — half a base-pair step — reached at a displacement of **ten or
eleven** base pairs, and *the cheapest forcing is the one displaced furthest in base pairs*.

| | displacement | azimuth | `C-0152`'s own ceiling, per crossover |
|---|---|---|---|
| `C-0152`'s cheapest rung | 10 or 11 bp | `17.1428571°` | `0.350894669 k_BT` |
| **what the gallery actually does, 28 times of 28** | **1 bp** | **`34.2857143°`** | **`1.05268401 k_BT`** |

Both figures are `C-0152`'s, and the second is its `10.5268401 k_BT` block ceiling divided by the
ten crossovers it is written on. Their ratio is **`3.00000001`** — **not** the `4` a reader would
get by squaring the azimuth, because `C-0152`'s ceiling reaches the angle through its span and
roll mapping and not directly. **Quote the owner's two numbers, never the ratio you expect between
them.**

**The field, forcing a crossover, has never once taken the cheap rung.** That is not a criticism
of the designers and it is not a refutation of `C-0152`; it is `C-0152`'s own sentence —
*no count of base pairs can see it* — observed in the only record of the operation that exists.
A designer nudging a crossover reaches for the neighbouring base pair, which is the expensive one.

**`C-0152`'s ceiling is untouched and its threshold is the one the field lands on.** `C-0152`
delivers a ceiling at the cheap rung and a **threshold** at the next one, and says *"the departure
this raster needs sits a factor of two below the first rung that would exceed"* the host sheet's
own calibration. The gallery says the field's designers, 28 times of 28, put their forcing **on
that threshold rung** rather than below it. Nothing here moves either number; what it does is say
which rung a real design lands on, and it is the dearer one.

## 6. The yield — `F4` did NOT fire, and its not firing is a partition

The gallery **page** carries no yields, as `T-246` recorded. The papers it cites do, and they
divide cleanly.

**(a) Per-design yields exist, for the seven caDNAno blocks — and all seven force nothing.**
Figure 2d/2e of the caDNAno paper, digitised from the publisher PDF at 600 dpi:

| design | scaffold (paper) | scaffold (file) | gel yield % | TEM yield after purification % | forces a crossover? |
|---|---|---|---|---|---|
| i `16 × 4` | 8064 | **8064** | **25.2** | 11.2 | **no** |
| ii `10 × 6` | 7560 | **7560** | 12.9 | **53.8** | **no** |
| iii `8 × 8` | 8064 | **8064** | 5.0 | 15.3 | **no** |
| iv `6 × 10` | 7560 | **7560** | 3.2 | 9.1 | **no** |
| v `4 × 16` | 8064 | **8064** | 6.2 | 2.9 | **no** |
| vi `3 × 20` | 7560 | **7560** | 3.2 | 2.0 | **no** |
| vii `2 × 30` | 7560 | **7560** | 8.3 | 16.3 | **no** |

The middle two columns are the paper's own scaffold-pairing sentence — *"i: p8064, ii: p7560,
iii: p8064, iv: p7560, v: p8064, vi: p7560, vii: p7560"* — against the scaffold each **file**
occupies, and they agree at **7 of 7**. That is what identifies the archive's designs as the
paper's designs, and it was not assumed.

**The digitisation is cross-checked against the paper's own two ordinal statements** and passes
both: *"the four-helix-per-`x`-raster design produced the leading band with the greatest intensity,
indicating the best yield"* → (i) is the tallest bar of panel d; *"the six-helix-per-`x`-raster
(`10 × 6`) shape appeared the most robust of the seven designs"* → (ii) is the tallest bar of
panel e. The axis scale is recovered from the **spacings** of the rules rather than from a label,
so a rule occluded by a tall bar costs nothing, and every detected spacing is asserted to be a
whole multiple of the 15-unit step.

**(b) The designs that DO force a crossover have a pooled range and no map.**

> *"The fraction of scaffold strands that were incorporated into monomeric species after folding
> varied from **7 % to 44 %** for these targets as estimated by ethidium-bromide fluorescence
> intensity."* — Douglas et al., *Nature* **459**:414, **read directly**

*"These targets"* is the shape set of its Figure 2 — monolith, square nut, railed bridge, genie
bottle, stacked cross, slotted cross — which contains designs that force and designs that do not.
**`CLAUDE.md`: a range quoted in a paper is not a field, and the map is usually in the SI.** It is
not: the 13 MB Supplementary Information carries staple sequence tables and no per-shape yield.

**(c) Dietz 2009 gives ordinals, and its controlled series holds the forcing FIXED.**
For the seven protractor designs it reports *"Folding of five of the seven 3 by 6 bundle versions
resulted in products that migrate as sharp bands … while the 150° and 180° versions migrate as more
'fuzzy' bands"*. All seven protractor files carry the **identical** forced-crossover census — 4
misaligned, 4 non-adjacent, 0 off-register — and differ only in insertions and deletions. So the
one per-design ordinal yield series in the gallery varies the **twist correction** at constant
forcing, and says nothing about forcing at all.

**So: 0 of 15.** One-sided 95 % Clopper-Pearson upper limit **`0.181036273`** — the exact limit at
zero of fifteen, `1 − 0.05^(1/15)`, and not a symmetric standard error, which at a saturated
proportion is a function of the estimate alone.

## 7. The conjunction, scored — `F7` fired and is reported

| step | observed | marginal rate |
|---|---|---|
| an archive is obtainable in 2026 | 3 of 3 | `1.0` |
| an archive contains machine-readable design files | 26 designs in 3 of 3 | `1.0` |
| a design carries a forced crossover | **15 of 26** | `0.576923077` |
| **a per-design yield is published for a design carrying one** | **0 of 15** | `0.0` |

Product **`0.0`**. `F7` was declared to fire if the expected yield of the conjunction fell below
one, and it did — but the reason is the *last* conjunct, not the search's budget: the first three
succeeded outright. **A null over a conjunction is informative exactly when the earlier conjuncts
are measured rather than assumed**, and here they are 1.0, 1.0 and 0.577.

And the failure is **structural rather than accidental**, which is the finding: the one paper that
publishes per-design yields is the one whose whole purpose was to compare cross-sections **at a
fixed rule**, so its designs cannot carry a forcing by construction; the papers whose designs force
are the ones demonstrating shapes, and they pool.

## 8. What this does and does not settle

- **It does not falsify `C-0152`.** Its ceiling stands, its ladder stands, and its negative
  existence result now stands on a stronger ground: the operation is used, 28 times, in the field's
  own record, and **not once with a yield attached**.
- **It does not price a forced crossover.** Nothing here is an energy. A design carrying one and a
  design carrying none differ in a hundred other things.
- **It removes the last route by which the gallery could have falsified `C-0152`.** `C-0152` §6
  named the archives as the one unexamined source; they are examined.
- **It leaves one route open and names it**: a controlled series in which forcing is the only
  variable, which `T-246` established nobody has run and this census confirms nobody has deposited.

## 9. The five verification gates

1. **Dimensional consistency** — every quantity is a count, a base-pair index, a residue modulo 21,
   or a percentage with its denominator named. The one derived angle uses `240/7°` per base pair,
   parsed from the source that declares it.
2. **Limiting cases** — a fixture built exactly on the rule reports zero forced crossovers; the same
   fixture with one crossover moved two base pairs reports it forced; a bond with no staple
   crossover is `unscorable` and never silently `allowed`; a design whose grid positions are four
   columns apart is forced by adjacency alone at zero register cost.
3. **Symmetry and conservation** — the verdict is **invariant under a datum shift**, asserted on a
   fixture translated by `+3 bp`; and the scaffold path of the `10 × 6` block closes as one linear
   strand of **7 560** bases into **119** domains against **118** crossings, `119 = 118 + 1`.
4. **Numerical convergence** — none applies: every step is exact integer arithmetic. The one
   continuous measurement, the bar chart, carries its own convergence statement instead: the axis
   scale is derived from rule **spacings** and every spacing is asserted a whole 15-unit multiple.
5. **Literature cross-check** — the seven blocks' scaffold occupancy against the paper's own
   scaffold-pairing sentence (**7 of 7**); the digitised chart against the paper's own two ordinal
   statements (**2 of 2**); and `NAR09/ii_10x6` against `Nature09/monolith`, deposited by two
   different papers, **bit-identical in every `vstrand`**
   (`ac46b69a04813ee0cc15eddd5fc33265f56a8072bfda3063924b3c5034317053`).

## 10. Validity range

- **This is a census of DESIGN FILES.** Whether a folded object matched its file is not settled
  here and cannot be. `CH-0251` is exactly that gap, on one specific block.
- **Test B is a lower bound** on the forced count, by the declared blind spot in §3.
- **The three archives are what the gallery links today.** A design that folded and was never
  deposited is invisible to this, and so is any archive the gallery once linked and no longer does;
  the 2012 capture lists the same three.
- **The per-design yields are digitised from a bar chart**, so they carry the resolution of that
  chart. They are quoted to one decimal because the digitiser's own pixel scale is
  `0.140187 %` per pixel; the *ordering* is what the cross-checks certify, and the ordering is
  the paper's own.
- **No forcing is priced.** `C-0152` owns that axis and this claim does not re-derive it.
