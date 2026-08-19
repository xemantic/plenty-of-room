# Is anything in this codebase not available in current DNA-nanotech tooling?

Second pass, grounded in the literature rather than in recollection.
Every claim below is marked **SURVIVES**, **CORRECTED** or **WITHDRAWN** against what the sources actually say,
and each source is flagged for how it was read —
*(read directly)* for full text or a verbatim quotation obtained,
*(abstract only)*, or *(search summary)* where a paywall blocked the primary.

## The landscape, as the 2024–2026 literature describes it

The field partitions the same way I claimed, and the newest review states the partition explicitly.

**Design tools** — caDNAno, scadnano, Athena, Adenita, MagicDNA, ENSnano, DNAforge, Tiamat.
Of these the review says caDNAno *"uses a 2D mesh to aid in the design and placement of staple strands"*,
and the newer generation *"(semi)automate the design"* and export to oxDNA.
DNAforge's own paper describes output as *"the full 3D nucleotide model, stapling arrangement where applicable, and the primary sequences"* —
a design artifact, not a verdict.

**Simulation tools** — oxDNA, CanDo, SNUPI, mrDNA.
These predict shape, flexibility and dynamics of a design that already exists.

The gap I claimed — *"does this device meet a force-and-stroke specification, and which design rule is binding?"* —
is not addressed by either family, and I found nothing in the 2024–2026 literature that closes it.
**SURVIVES**, with the caveat that the two families have converged somewhat:
MagicDNA is explicitly a computer-aided *engineering* loop, not only a drawing surface,
and 2024–2026 has added generative and optimisation-driven design
(a diffusion model for de novo origami, generative wireframe exploration, thermodynamic strand-routing optimisation giving 6–30× yield gains).
Those optimise **foldability**, not **device performance**.

## 1. Coupling origami mechanics to a solved solvent and a soft foundation — CORRECTED, narrowed, still stands

My original sentence *"CanDo has no foundation and no electrolyte; oxDNA has no equation of state for a grafted PEG layer"* was two-thirds right and misleading on the middle term.

- **oxDNA does model electrolyte.** oxDNA2 introduced *"a salt-dependent interaction term"* via Debye–Hückel.
  But the parameterisation is *"restricted to salt concentrations of 0.1 M of monovalent salt or greater"*,
  and magnesium *"interacts with DNA and RNA in a non-uniform, site-specific manner and therefore is not included in the oxDNA model."*
  This project's whole operating range is 0.5–10 mM **MgCl₂** — below the floor and in the ion oxDNA excludes.
  So the correct statement is not *"oxDNA has no electrolyte"* but *"oxDNA's electrolyte is parameterised outside this device's regime, in the wrong valency."*
- **mrDNA can apply an external electric field** — it studies DNA *"under a variety of environmental conditions, such as applied electric field."*
  So an applied field is not novel. What is absent there is a **solved** electrode boundary:
  a diffuse layer, a Stern series converting a diffuse-layer drop into an applied bias, and a force balance whose root is the operating point.
- **Charged surfaces are named as an explicit oxDNA exclusion**: the review lists
  *"complex interactions with charged surfaces, lipids, protein or small molecule binding"*
  among what *"cannot be fully represented by this model."*
  Origami-on-surface work (mica, supported lipid bilayers) is experimental; I found no coarse-grained model coupling an origami plate to a surface double layer.
- **Grafted polymer layers**: the closest published work is *Hairygami* (ACS Nano 2024), which shows single-stranded overhangs curve a 2D tile by an entropic mechanism, simulated by modelling every overhang explicitly in oxDNA.
  That is the same physics this project puts under the tile, approached from the opposite end:
  explicit ssDNA at MD cost, versus a continuum PEG brush equation of state (SCF) coupled to a plate.

Net: the composition — `brush/SelfConsistentField.kt` + `electrostatics/PoissonBoltzmannEdge.kt` + `structure/OrigamiGrillage.kt` + `actuator/PullInStability.kt`, closing one force balance and locating a pull-in fold — I still find nowhere.
**The novelty is the closed loop and the buffer regime, not the individual ingredients.**

## 2. Crossovers as design elements — WITHDRAWN as stated, replaced

My claim that a crossover-as-elastic-spring is not in released tooling is **wrong**.
SNUPI models *"DNA origami crossovers use a heuristic dihedral angle potential with a spring constant to keep adjacent helices roughly parallel,"*
with single-stranded DNA as an entropic spring — the same construct as `k_θ` here.
CanDo's rigid-crossover assumption is confirmed as stated
(*"The interhelical crossovers are modeled as rigid constraints"*),
and so is its `EI = 230 pN·nm²`, so `CLAUDE.md`'s note about CanDo is accurate.
Applied external loads are also not novel: FE force-extension curves on origami under applied axial force are published (ABAQUS, NAR 2020).

What survives is narrower and, I think, real:

- reporting a **per-crossover force and hinge moment** as a design quantity to be compared against a bond-rupture allowable that carries its own bonded length and loading rate, rather than as an internal diagnostic of a relaxation;
- treating crossover **prestrain as a load**, which makes the field linear in it, so one solve fixes the whole axis and superposition gives influence banks;
- and the combinatorics on top of that — which crossover sites exist at all, at which phase, and how many survive a fabrication dropout.

## 3. Lattice design rules as enumerations and proofs — SURVIVES

Twist correction by insertions and deletions is standard practice and standard in the tools.
The 2024 PNAS work optimises strand routing against a thermodynamic score.
What I do not find anywhere is the **exclusion** direction:
proving that a seamless raster's width is quantised, that a twist-corrected seamless row is impossible with a residual of exactly a quarter base pair at every width,
or enumerating the honeycomb's admissible row lengths as a congruence with two disjoint turn senses.
Design tools let you draw the forbidden cases; simulators tell you afterwards that the result is strained.
This is closer to electronic-design-automation **design-rule checking** than to anything in the DNA CAD literature, and the analogy appears not to have been drawn.

## 4. Backbone-torsion feasibility as a search oracle — CORRECTED

The measurement half of this is not novel and is better done elsewhere:
**DNATCO** has classified dinucleotide backbone conformers since 2016, and DNATCO v5.0 (NAR 2026)
assigns steps to 96 NtC classes by k-nearest neighbours with valence-geometry validation.
So *"judged against a measured distribution"* is state of the art, not a gap — and the project's own PDB survey re-derives what that infrastructure already provides.

Two things are still not in that family, and both are recorded in `CLAUDE.md` as findings:

- DNATCO validates an **experimental structure**; using the same statistics **forward**, as a feasibility oracle inside a design search, is a different use;
- and the project's finding that a clustered conformer class is too diffuse to serve as a *"populated region"* test —
  99th-percentile class radii above 150° — is a direct methodological criticism of exactly the kNN/k-means classification DNATCO uses,
  answered here with a marginal ten-degree occupancy histogram.
  Whether that criticism is fair against DNATCO v5.0's own confal scoring, I have not verified.

The closed-form reach interval that refuses in microseconds before any minimiser runs, I still find nowhere.

## 5. Placement optimisation under measured fabrication statistics — SURVIVES, with a neighbour

Yield-aware design exists and is active: the 2024 PNAS routing optimiser, and a 2026 Nature Communications scaffold-sequence framework scoring *"four classes of off-target interactions."*
Both act at the **sequence and folding-pathway** level, to make defects rarer.
Neither propagates a **measured defect distribution** into a **mechanical** performance requirement — which is what the dropout work here does:
Strauss's 48–95 % per-staple incorporation converted into an attachment-density requirement,
single-path-removal fragility bounds, and an oracle floor over all distributions.
The influence-surrogate bank in `coupling/NonUniformCoupling.kt` is the enabling trick and appears to have no counterpart.

## The provenance tooling — CORRECTED

*"I know of no computational-science suite that ships a false-independence detector"* was too broad about provenance in general.
Provenance and reproducibility tooling is a mature field:
ReproZip, Sciunit, noWorkflow, CAESAR/REPRODUCE-ME, Code Ocean, WholeTale, Binder.
Those capture **how** a result was produced.

The `tools/` here do something adjacent and, as far as I can find, unclaimed:
they check **what a corpus of results asserts about itself** —
`result-transfers.py` finding that two studies claiming independent corroboration are quoting the same number,
`reemission-order.py` topologically ordering re-runs after a repair,
`trace-answers.py` checking a deliverable against the claims it cites.
The nearest relatives are metascience checkers such as statcheck and GRIM in psychology, not workflow-provenance systems.
That is where the comparison belongs, and I have not found either family doing false-independence detection over a computational corpus.

## What it is not

Unchanged, and the literature sharpens it.
No sequence design, no folding-pathway prediction, no structure prediction —
and those are precisely the areas where 2024–2026 has moved fastest
(generative diffusion design, mesoscopic folding simulation, off-target-aware scaffold selection).
The grillage is single-layer square-lattice and does not transfer to honeycomb internals.
The material is one polymer, one salt, one tile.

## Verdict

The unique artifact is narrower than I first said, and it survives the check:
a **requirement-driven design-rule and device engine** —
buildability proofs on the lattice, a device-level force balance against a solved brush and double layer in a buffer regime the standard simulators exclude,
and defect-statistics-aware placement optimisation —
sitting between the editors and the simulators,
plus corpus-level self-audit tooling whose relatives are in metascience rather than in nanotechnology.

## Sources

- Haggenmueller et al., *How we simulate DNA origami*, Small Methods 2025 — arXiv:2409.13206 *(read directly)*: https://arxiv.org/html/2409.13206v1
- Snodin et al., *Introducing improved structural properties and salt dependence into a coarse-grained model of DNA* (oxDNA2), J. Chem. Phys. 142:234901 (2015) *(search summary + arXiv abstract)*: https://arxiv.org/abs/1504.00821
- Maffeo & Aksimentiev, *mrDNA: a multi-resolution model…*, NAR 48:5135 (2020) *(abstract only)*: https://academic.oup.com/nar/article/48/9/5135/5814051
- Kim et al., *Quantitative prediction of 3D solution shape and flexibility of nucleic acid nanostructures* (CanDo), NAR 40:2862 (2012) *(search summary; rigid-crossover statement quoted)*: https://academic.oup.com/nar/article/40/7/2862/1193417
- Lee et al., *Rapid Computational Analysis of DNA Origami Assemblies at Near-Atomic Resolution* (SNUPI), ACS Nano 2021 *(search summary; paywalled)*: https://pubs.acs.org/doi/10.1021/acsnano.0c07717
- *SNUPI: A Computational Framework for Rapid Mechanical Analysis of Structured DNA Assemblies*, JACS Au 2025 *(not found — 403)*: https://pubs.acs.org/doi/10.1021/jacsau.5c01110
- Huang et al., *Integrated computer-aided engineering and design for DNA assemblies* (MagicDNA) *(search summary)*: https://github.com/cmhuang2011/MagicDNA
- Elonen et al., *DNAforge: a design tool for nucleic acid wireframe nanostructures*, NAR 52:W13 (2024) *(abstract only)*: https://academic.oup.com/nar/article/52/W1/W13/7673483
- de Llano et al., *Adenita: interactive 3D modelling and visualization of DNA nanostructures*, NAR 48:8269 (2020) *(abstract only)*: https://academic.oup.com/nar/article/48/15/8269/5874358
- Doty et al., *scadnano: a browser-based, scriptable tool…* *(abstract only)*: https://arxiv.org/pdf/2005.11841
- *Design principles for accurate folding of DNA origami*, PNAS 2024 *(abstract only)*: https://www.pnas.org/doi/10.1073/pnas.2406769121
- *Optimising DNA origami assembly by reducing off-target interactions*, Nature Communications 2026 *(abstract only)*: https://www.nature.com/articles/s41467-026-73387-4
- *De novo design of DNA origami with a generative diffusion model*, Nature Communications 2026 *(title only)*: https://www.nature.com/articles/s41467-026-73578-z
- *Generative design-enabled exploration of wireframe DNA origami nanostructures*, NAR 53:gkae1268 (2025) *(title only)*: https://academic.oup.com/nar/article/53/2/gkae1268/7935005
- Sample et al., *Hairygami: Analysis of DNA Nanostructures' Conformational Change Driven by Functionalizable Overhangs*, ACS Nano 18:30004 (2024) *(abstract only)*: https://arxiv.org/abs/2302.09109
- *Stretching DNA origami: effect of nicks and Holliday junctions on the axial stiffness*, NAR 48:12407 (2020) *(search summary)*: https://academic.oup.com/nar/article/48/21/12407/5957171
- Černý et al., *DNATCO: assignment of DNA conformers*, NAR 44:W284 (2016) *(abstract only)*: https://academic.oup.com/nar/article-abstract/44/W1/W284/2499360
- *DNATCO v5.0: integrated web platform for 3D nucleic acid structure analysis*, NAR 54:gkaf1491 (2026) *(abstract only)*: https://academic.oup.com/nar/article/54/1/gkaf1491/8415847
- Trinh et al., *Computer-Aided Design Software and Simulation Tools for DNA Nanoparticles*, Adv. Mater. Technol. 2026 *(not found — 402 paywall)*: https://advanced.onlinelibrary.wiley.com/doi/10.1002/admt.202500438
- *Evaluating Tools for Enhancing Reproducibility in Computational Scientific Experiments*, ACM 2024 *(read directly)*: https://dl.acm.org/doi/fullHtml/10.1145/3641525.3663623
