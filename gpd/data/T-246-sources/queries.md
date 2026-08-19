# `T-246` — literature search log: is there a published price for a FORCED / off-rule crossover?

**Task.** caDNAno (Douglas et al., *Nucleic Acids Res.* **37**:5001, 2009, `PMC2731887`) lets a user *force* a
crossover at a lattice position its default rules forbid,
and warns only that *"departure from the default rules may lead to folding failure if too much deviation from
canonical DNA geometry is implied"*.
This log records every query run in search of a **published measurement** attributable to such a crossover.

**Verdict, stated up front: NO.**
No published measurement — yield, melting temperature, defect rate, structural quality, or simulated energy —
is attributable to a forced / off-rule-position crossover in DNA origami.
The closest published measurements are recorded in §4 and none of them is that quantity.

**Both quotations in the task statement were re-verified** against
`gpd/data/T-151-sources/PMC2731887-fullTextXML.xml` (already in the repository, fetched by `T-151`);
they are present verbatim. Provenance: **read directly**.

---

## 1. Search budget

| Endpoint | Queries | Notes |
|---|---|---|
| EuropePMC REST `search` | **54** | 8 s sleep between queries, 5 retries; raw JSON saved per query |
| arXiv API `query` | **10** | `all:` searches title/abstract/comments only — arXiv does **not** index full text, so it is a weak instrument here and is recorded as such |
| OpenAlex `/works?search=` | **4** | fuzzy relevance over title/abstract, not exact-phrase; used only as a cross-check |
| Direct web fetch | **8** | cadnano.org (live + Wayback), PMC article pages, Crossref |
| **Total queries** | **68** | in **7 declared families** (below) |

Candidate papers examined in full or in part: **14** (§3).

### Query families

- **F1** — forced / off-rule / non-canonical crossover, *direct*
- **F2** — crossover vs folding yield / folding efficiency / folding failure
- **F3** — crossover spacing, density, position
- **F4** — simulation (oxDNA / all-atom / CanDo / lattice-free) energetics and strain of crossovers
- **F5** — twist density, torsional strain, insertions/deletions vs yield
- **F6** — caDNAno-specific design-rule language (*"canonical DNA geometry"*, *"design rules"*, *"manually"*, gallery)
- **F7** — junction conformation, misfolding, thermal stability, defect rate

---

## 2. Every query, verbatim

### 2.1 EuropePMC — `https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=<q>&format=json&pageSize=25&resultType=core`

| Query string (verbatim) | Endpoint | Hits | Raw response |
|---|---|---|---|
| `"forced crossover" AND "DNA origami"` | EuropePMC REST search | 0 | `search-forced_crossover_AND_DNA_origami.json` |
| `"forced crossovers" AND DNA` | EuropePMC REST search | 3 | `search-forced_crossovers_AND_DNA.json` |
| `"force crossovers" AND caDNAno` | EuropePMC REST search | 1 | `search-force_crossovers_AND_caDNAno.json` |
| `"DNA origami" AND crossover AND "folding yield"` | EuropePMC REST search | 28 | `search-DNA_origami_AND_crossover_AND_folding_yield.json` |
| `caDNAno AND "design rules" AND yield` | EuropePMC REST search | 32 | `search-caDNAno_AND_design_rules_AND_yield.json` |
| `"DNA origami" AND "crossover spacing"` | EuropePMC REST search | 18 | `search-DNA_origami_AND_crossover_spacing.json` |
| `"DNA origami" AND "crossover density"` | EuropePMC REST search | 23 | `search-DNA_origami_AND_crossover_density.json` |
| `"DNA origami" AND "non-canonical" AND crossover` | EuropePMC REST search | 28 | `search-DNA_origami_AND_non_canonical_AND_crossover.json` |
| `oxDNA AND crossover AND "DNA origami" AND energy` | EuropePMC REST search | 40 | `search-oxDNA_AND_crossover_AND_DNA_origami_AND_energy.json` |
| `"DNA origami" AND "folding failure"` | EuropePMC REST search | 2 | `search-DNA_origami_AND_folding_failure.json` |
| `"DNA origami" AND strained AND crossover` | EuropePMC REST search | 37 | `search-DNA_origami_AND_strained_AND_crossover.json` |
| `"DNA origami" AND "melting temperature" AND crossover` | EuropePMC REST search | 85 | `search-DNA_origami_AND_melting_temperature_AND_crossover.json` |
| `"DNA origami" AND "off-register"` | EuropePMC REST search | 0 | `search-DNA_origami_AND_off_register.json` |
| `"DNA origami" AND "twist correction" AND yield` | EuropePMC REST search | 5 | `search-DNA_origami_AND_twist_correction_AND_yield.json` |
| `"staple crossover" AND yield AND DNA` | EuropePMC REST search | 17 | `search-staple_crossover_AND_yield_AND_DNA.json` |
| `"DNA origami" AND "design rules" AND "crossover" AND stability` | EuropePMC REST search | 41 | `search-DNA_origami_AND_design_rules_AND_crossover_AND_stability.json` |
| `"DNA origami" AND "base pairs per turn" AND yield` | EuropePMC REST search | 14 | `search-DNA_origami_AND_base_pairs_per_turn_AND_yield.json` |
| `"DNA origami" AND insertions AND deletions AND twist AND yield` | EuropePMC REST search | 28 | `search-DNA_origami_AND_insertions_AND_deletions_AND_twist_AND_yield.json` |
| `TITLE:"Multilayer DNA origami packed on hexagonal and hybrid lattices"` | EuropePMC REST search | 1 | `search-TITLE_Multilayer_DNA_origami_packed_on_hexagonal_and_hybrid_lattices.json` |
| `"Holliday junction" AND "DNA origami" AND "free energy" AND simulation` | EuropePMC REST search | 20 | `search-Holliday_junction_AND_DNA_origami_AND_free_energy_AND_simulation.json` |
| `"DNA origami" AND "strain energy" AND crossover` | EuropePMC REST search | 11 | `search-DNA_origami_AND_strain_energy_AND_crossover.json` |
| `"DNA nanostructure" AND "geometric frustration"` | EuropePMC REST search | 1 | `search-DNA_nanostructure_AND_geometric_frustration.json` |
| `"DNA origami" AND "crossover position" AND yield` | EuropePMC REST search | 8 | `search-DNA_origami_AND_crossover_position_AND_yield.json` |
| `"DNA origami" AND "unconventional crossover" OR "irregular crossover"` | EuropePMC REST search | 5540 | `search-DNA_origami_AND_unconventional_crossover_OR_irregular_crossover.json` |
| `"DNA origami" AND "irregular crossover"` | EuropePMC REST search | 0 | `search-DNA_origami_AND_irregular_crossover.json` |
| `"DNA origami" AND "unconventional crossover"` | EuropePMC REST search | 0 | `search-DNA_origami_AND_unconventional_crossover.json` |
| `"DNA origami" AND caDNAno AND "manually" AND crossover` | EuropePMC REST search | 43 | `search-DNA_origami_AND_caDNAno_AND_manually_AND_crossover.json` |
| `scadnano OR cadnano AND "crossover" AND "violat"` | EuropePMC REST search | 0 | `search-scadnano_OR_cadnano_AND_crossover_AND_violat.json` |
| `"DNA origami" AND "helical phase" AND crossover` | EuropePMC REST search | 0 | `search-DNA_origami_AND_helical_phase_AND_crossover.json` |
| `"DNA origami" AND "defect rate" AND staple` | EuropePMC REST search | 4 | `search-DNA_origami_AND_defect_rate_AND_staple.json` |
| `"DNA origami" AND "crossover" AND "kinetic trap" AND yield` | EuropePMC REST search | 2 | `search-DNA_origami_AND_crossover_AND_kinetic_trap_AND_yield.json` |
| `"DNA origami" AND "junction" AND "strain" AND "folding yield"` | EuropePMC REST search | 11 | `search-DNA_origami_AND_junction_AND_strain_AND_folding_yield.json` |
| `"DNA origami" AND "torsional strain" AND yield` | EuropePMC REST search | 12 | `search-DNA_origami_AND_torsional_strain_AND_yield.json` |
| `"DNA origami" AND "design rule" AND violation` | EuropePMC REST search | 0 | `search-DNA_origami_AND_design_rule_AND_violation.json` |
| `"DNA origami" AND "crossover" AND "thermal stability" AND melting` | EuropePMC REST search | 52 | `search-DNA_origami_AND_crossover_AND_thermal_stability_AND_melting.json` |
| `"DNA origami" AND "21 base pairs" AND crossover` | EuropePMC REST search | 10 | `search-DNA_origami_AND_21_base_pairs_AND_crossover.json` |
| `"DNA origami" AND "seven base pairs" AND crossover` | EuropePMC REST search | 4 | `search-DNA_origami_AND_seven_base_pairs_AND_crossover.json` |
| `"DNA origami" AND "overwound" AND yield` | EuropePMC REST search | 8 | `search-DNA_origami_AND_overwound_AND_yield.json` |
| `"DNA nanostructure" AND "junction" AND distortion AND "coarse-grained"` | EuropePMC REST search | 4 | `search-DNA_nanostructure_AND_junction_AND_distortion_AND_coarse_grained.json` |
| `"DNA origami" AND "crossover" AND "local strain"` | EuropePMC REST search | 2 | `search-DNA_origami_AND_crossover_AND_local_strain.json` |
| `"DNA origami" AND "canonical DNA geometry"` | EuropePMC REST search | 1 | `search-DNA_origami_AND_canonical_DNA_geometry.json` |
| `"DNA origami" AND "lattice-free" AND crossover` | EuropePMC REST search | 11 | `search-DNA_origami_AND_lattice_free_AND_crossover.json` |
| `"DNA origami" AND "arbitrary crossover"` | EuropePMC REST search | 0 | `search-DNA_origami_AND_arbitrary_crossover.json` |
| `"DNA origami" AND "crossover" AND "structural consequences"` | EuropePMC REST search | 6 | `search-DNA_origami_AND_crossover_AND_structural_consequences.json` |
| `"DNA origami" AND "non-lattice" AND crossover` | EuropePMC REST search | 1 | `search-DNA_origami_AND_non_lattice_AND_crossover.json` |
| `caDNAno AND gallery AND yield` | EuropePMC REST search | 10 | `search-caDNAno_AND_gallery_AND_yield.json` |
| `"DNA origami" AND "crossover" AND "off-lattice"` | EuropePMC REST search | 3 | `search-DNA_origami_AND_crossover_AND_off_lattice.json` |
| `"DNA origami" AND crossover AND "folding efficiency"` | EuropePMC REST search | 18 | `search-DNA_origami_AND_crossover_AND_folding_efficiency.json` |
| `"DNA origami" AND "single crossover" AND "double crossover" AND stability` | EuropePMC REST search | 8 | `search-DNA_origami_AND_single_crossover_AND_double_crossover_AND_stability.json` |
| `"DNA origami" AND "crossover" AND "free energy penalty"` | EuropePMC REST search | 2 | `search-DNA_origami_AND_crossover_AND_free_energy_penalty.json` |
| `"four-way junction" AND "DNA origami" AND "stacking" AND "angle" AND measurement` | EuropePMC REST search | 13 | `search-four_way_junction_AND_DNA_origami_AND_stacking_AND_angle_AND_measurement.json` |
| `"DNA origami" AND "misfolded" AND crossover` | EuropePMC REST search | 37 | `search-DNA_origami_AND_misfolded_AND_crossover.json` |
| `"DNA origami" AND "crossover" AND "twist density" AND folding` | EuropePMC REST search | 12 | `search-DNA_origami_AND_crossover_AND_twist_density_AND_folding.json` |
| `"DNA origami" AND "design defect" OR "design flaw" AND yield` | EuropePMC REST search | 1 | `search-DNA_origami_AND_design_defect_OR_design_flaw_AND_yield.json` |

**The zeros are the result.** Note in particular:

| Query | Hits |
|---|---|
| `"forced crossover" AND "DNA origami"` | **0** |
| `"DNA origami" AND "off-register"` | **0** |
| `"DNA origami" AND "irregular crossover"` | **0** |
| `"DNA origami" AND "unconventional crossover"` | **0** |
| `"DNA origami" AND "arbitrary crossover"` | **0** |
| `"DNA origami" AND "design rule" AND violation` | **0** |
| `"DNA origami" AND "helical phase" AND crossover` | **0** |
| `scadnano OR cadnano AND "crossover" AND "violat"` | **0** |
| `"DNA origami" AND "canonical DNA geometry"` | **1** — and the one hit is `PMC2731887`, caDNAno itself |
| `"forced crossovers" AND DNA` | **3** — one relevant, and it is `PMC2731887` |
| `"force crossovers" AND caDNAno` | **1** — `PMC2731887` |
| `"DNA origami" AND "folding failure"` | **2** — one is `PMC2731887` |

**One query of mine was malformed and is recorded as such:**
`"DNA origami" AND "unconventional crossover" OR "irregular crossover"` returned **5540** hits because
EuropePMC bound the `OR` above the `AND`. It was re-run correctly as two separate queries
(`"DNA origami" AND "irregular crossover"` → 0; `"DNA origami" AND "unconventional crossover"` → 0).
The malformed response is retained for audit.

### 2.2 arXiv — `https://export.arxiv.org/api/query?search_query=<q>&max_results=25`

| Query string (verbatim) | Endpoint | Total results | Raw response |
|---|---|---|---|
| `all:"forced crossover"` | arXiv API | 0 | `arxiv-all_forced_crossover.xml` |
| `all:"DNA origami" AND all:"crossover" AND all:"folding yield"` | arXiv API | 0 | `arxiv-all_DNA_origami_AND_all_crossover_AND_all_folding_yield.xml` |
| `all:"DNA origami" AND all:"crossover spacing"` | arXiv API | 0 | `arxiv-all_DNA_origami_AND_all_crossover_spacing.xml` |
| `all:oxDNA AND all:crossover AND all:origami` | arXiv API | 2 | `arxiv-all_oxDNA_AND_all_crossover_AND_all_origami.xml` |
| `all:"DNA origami" AND all:"design rules" AND all:crossover` | arXiv API | 0 | `arxiv-all_DNA_origami_AND_all_design_rules_AND_all_crossover.xml` |
| `all:cadnano AND all:crossover` | arXiv API | 0 | `arxiv-all_cadnano_AND_all_crossover.xml` |
| `all:"DNA origami"` | arXiv API | 131 | `arxiv-all_DNA_origami.xml` |
| `all:"DNA origami" AND all:crossover` | arXiv API | 2 | `arxiv-all_DNA_origami_AND_all_crossover.xml` |
| `all:"DNA origami" AND all:yield AND all:crossover` | arXiv API | 0 | `arxiv-all_DNA_origami_AND_all_yield_AND_all_crossover.xml` |
| `all:"DNA nanostructure" AND all:"crossover" AND all:"strain"` | arXiv API | 0 | `arxiv-all_DNA_nanostructure_AND_all_crossover_AND_all_strain.xml` |

**arXiv is a weak instrument for this question and the log says so.** `all:"DNA origami"` alone returns
**131** — arXiv indexes title, abstract and comments, not full text — so `all:"DNA origami" AND all:crossover`
returning **2** is a statement about abstracts, not about papers. It is recorded for completeness and is
not offered as an exclusion.

### 2.3 OpenAlex — `https://api.openalex.org/works?search=<q>&per-page=25`

| Query string (verbatim) | Endpoint | Count | Raw response |
|---|---|---|---|
| `forced crossover DNA origami` | OpenAlex `/works?search=` | 115 | `openalex-forced_crossover_DNA_origami.json` |
| `DNA origami crossover folding yield design rules` | OpenAlex `/works?search=` | 364 | `openalex-DNA_origami_crossover_folding_yield_design_rules.json` |
| `DNA origami crossover spacing stability` | OpenAlex `/works?search=` | 295 | `openalex-DNA_origami_crossover_spacing_stability.json` |
| `off-lattice crossover DNA origami strain` | OpenAlex `/works?search=` | 134 | `openalex-off_lattice_crossover_DNA_origami_strain.json` |

OpenAlex `search` is a **fuzzy relevance** ranking over title and abstract, not an exact-phrase full-text
search, so its counts bound nothing. It is recorded because its top-ranked hit for
`forced crossover DNA origami` is **the caDNAno paper itself** — consistent with the EuropePMC zeros.

### 2.4 Direct web fetches

| URL | HTTP | What it was for | Saved as |
|---|---|---|---|
| `https://cadnano.org/` | 200 | cadnano.org site, current | `cadnano-org-index.html` |
| `https://cadnano.org/gallery` | 200 | **the gallery the caDNAno paper points at** | `cadnano-org-gallery.html` |
| `https://cadnano.org/legacy.html` | 200 | legacy downloads page | `cadnano-org-legacy.html` |
| `http://archive.org/wayback/available?url=cadnano.org/gallery&timestamp=20091001` | 200 | locate a 2009-era gallery | `wayback-avail-gallery.json` |
| `http://web.archive.org/web/20120221003655/http://cadnano.org:80/gallery` | 200 | **archived 2012 gallery** | `wayback-2012-gallery.html` |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC2821935/` | 200 | Ke et al. 2009 *JACS* full text | `PMC2821935-articlepage.html` |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC3957201/` | 200 | Ke et al. 2012 *Chem. Sci.* full text | `PMC3957201-articlepage.html` |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC3336742/` | 200 | Ke et al. 2012 *JACS* hexagonal/hybrid | `PMC3336742-articlepage.html` |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC4872871/` | 200 | Wei et al. *Nat. Commun.* design space | `PMC4872871-articlepage.html` |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC2737683/` | 200 | Dietz 2009 *Science* — **stub, 167 characters of text** | `PMC2737683-articlepage.html` |
| `https://www.science.org/doi/10.1126/science.1174251` | **403** | Dietz 2009 *Science* — paywalled | — |
| `https://api.crossref.org/works/10.1126/science.1174251` | 200 | Dietz 2009 abstract (editor's summary only) | `crossref-dietz2009.json` |

**The cadnano.org gallery does not quantify anything.** The paper's closing sentence points readers to it
*"for examples of designs that folded successfully, although with varying yields"*. The gallery — both
**today** and in the **2012 Wayback capture**, i.e. the nearest surviving state to publication — consists of
exactly three entries, each a *citation plus a `.zip` of source files*: Dietz/Douglas/Shih *Science* 2009,
Douglas et al. *NAR* 2009 (caDNAno itself), and Douglas et al. *Nature* 2009. **There are no yields on the
page at all**, and no forced-crossover designs are identified as such. The "varying yields" live in those
three papers, and none of the three attributes a yield to a forced crossover.
`http://cadnano.org/` also 404s no more: the `/docs` page fetched by `T-161` two iterations ago carries no
forced-crossover material either.

---

## 3. Candidate papers examined, with provenance

| # | Title | Year | PMCID / DOI | Provenance | Verdict on the question |
|---|---|---|---|---|---|
| 1 | Rapid prototyping of 3D DNA-origami shapes with caDNAno | 2009 | `PMC2731887` / `10.1093/nar/gkp436` | **read directly** (from `gpd/data/T-151-sources/`) | **The source of the question.** States the capability and the warning; states explicitly that *"Additional software development will be required … to predict the structural consequences of these changes"* and *"More work is also needed to see what design rules lead to stable structures"*. Quantifies **nothing**. |
| 2 | Two design strategies for enhancement of multilayer–DNA-origami folding: underwinding for specific intercalator rescue and staple-break positioning | 2012 | `PMC3957201` / *Chem. Sci.* | **read directly** | **Closest thing found.** A systematic sweep of crossover *spacing* (§4.1). Not a forced crossover: every crossover stays on its own design's lattice; the whole lattice is re-pitched. |
| 3 | Multilayer DNA origami packed on a square lattice (Ke, Douglas, Liedl, Högberg, Shih) | 2009 | `PMC2821935` / *JACS* **131**:15903 | **read directly** | Yield statement is about **staple breaks** and **crossover density**, not about a forced crossover (§4.2). |
| 4 | Multilayer DNA origami packed on hexagonal and hybrid lattices | 2012 | `PMC3336742` / *JACS* | **read directly** | Uses lattices caDNAno cannot draw — *"all crossovers need to be manually implemented in caDNAno"* — i.e. **forced crossovers in the software sense**, and they fold. But no yield number and no per-crossover cost (§4.3). |
| 5 | Complex cooperativity in DNA origami revealed via design-dependent defectivity | 2026 | `PMC12907558` / `10.1093/nar/gkag052` | **read directly** | Broadest design→defectivity survey to date (§4.4). None of its ~34 design variants is an off-rule crossover. |
| 6 | Folding Competition and Dynamic Transformation in DNA Origami: Parallel Versus Antiparallel Crossovers | 2025 | `PMC12182888` | **read directly** (from `gpd/data/T-151-sources/`) | Measures relative yield of a **non-default crossover *type*** (parallel), not a non-default *position* (§4.5). |
| 7 | Unraveling the Folding Dynamics of DNA Origami Structures | 2025 | `PMC12696799` | **read directly** | Crossovers-per-staple as an **entropic penalty** vs folding temperature and yield (§4.6). A count, not a position. |
| 8 | Lattice-free prediction of three-dimensional structure of programmed DNA assemblies | 2014 | `PMC4268701` / *Nat. Commun.* | **read directly** | Predicts the **shape** consequence of off-register junction arm lengths (20/21/22 bp). No energy, no yield (§4.7). |
| 9 | Design space for complex DNA structures (Wei et al.) | 2013 | `PMC4872871` / *Nat. Commun.* | **read directly** | *"We systematically changed crossover patterns"* — but this is **single-stranded-tile**, not scaffolded origami, and the characterisation is curvature/twist. The word *yield* does not occur in the article text. |
| 10 | Design principles for accurate folding of DNA origami | 2024 | `PMC11621765` | **read directly** | Staple-**breakpoint** optimisation over a caDNAno JSON. Its only crossover constraint is a penalty for breaking *both halves of a double crossover*. Nothing about forced positions. |
| 11 | Insights into the Structure and Energy of DNA Nanoassemblies | 2020 | `PMC7727707` | **read directly** | Review of four-way-junction conformational energetics (iso I / iso II, parallel vs antiparallel). Background, not a measurement of an off-lattice crossover. |
| 12 | Folding DNA into Twisted and Curved Nanoscale Shapes (Dietz, Douglas & Shih) | 2009 | `10.1126/science.1174251` | **abstract only** (Crossref editor's summary; `science.org` returns 403; the PMC record `PMC2737683` is a 167-character stub) | The origin of "targeted insertions/deletions". Its 60hb yield comparison is available here only **second-hand**, quoted in item 2, which I did read directly. |
| 13 | Stretching DNA origami: effect of nicks and Holliday junctions on the axial stiffness | 2020 | `PMC7708044` | **read directly** (fetched, skimmed) | Mechanics of *canonical* junctions. No off-register case. |
| 14 | The sequence of events during folding of a DNA origami | 2019 | `PMC6499592` | **read directly** (fetched, skimmed) | Folding-order measurement on a standard design. No crossover-position variable. |

Not obtained: **Dietz, Douglas & Shih, *Science* **325**:725 (2009)** — the only item in this search whose full
text could not be read. Flag: **abstract only**. Nothing in the verdict rests on it.

---

## 4. The closest published measurements, separately and clearly labelled

None of the following is a price for a forced crossover. Each is labelled with what it *is* about.

### 4.1 Crossover SPACING swept systematically — Ke, Bellot, Voigt, Fradkov & Shih, *Chem. Sci.* 2012 (`PMC3957201`) — **read directly**

**This is the only systematic crossover-spacing-vs-yield sweep found.** A 24-helix-bundle honeycomb
multilayer origami was built at reciprocal twist densities of **10.5 to 13.5 bp/turn in 0.5 bp/turn steps**,
by inserting base pairs *between* crossovers ("targeted insertions") — i.e. the crossover interval along each
interface is moved off its canonical 21 bp.

Verbatim, without intercalator:

> *"Whereas the previously reported 60hb folded better at 11.0 bp/turn, here underwinding 24hb to 11.0 bp/turn
> appeared to decrease the efficiency of folding, as indicated by the diffuseness and lower intensity of the
> product band"*
>
> *"However, underwinding 24hb to 12.0 bp/turn abolished productive folding completely. Instead, a ladder of
> multimers of misfolded products was formed"*

Verbatim, with ethidium rescue at 16 µM:

> *"the 11.5 bp/turn and 12.0 bp/turn 24hb both appear to fold to higher yield (estimated 24 % for
> 24hb_11.5bp/turn and 30 % for 24hb_12bp/turn) than the 10.5 bp/turn 24hb (estimated 14 %)"*

And the paper's own statement that the sign is **not transferable**:

> *"the design underwound to 11 bp/turn did not fold as well as the design at 10.5 bp/turn. The different
> behavior between these 60hb and 24hb designs suggests that the net energetic benefit for introducing
> underwinding into DNA nanostructures is architecture dependent."*

**Why this is not the number the task wants.** (i) It moves **every** crossover in the structure, not one;
(ii) it changes the base-pair **count** as well as the crossover position (the 13.5 bp/turn design has 29 %
more base pairs), so the crossover geometry is confounded with structure size and staple length;
(iii) the reported effect is dominated by an electrostatic-bowing / intercalator argument, not by junction
strain; (iv) the sign reverses between two architectures of the same laboratory.

### 4.2 Ke et al. 2009 *JACS* **131**:15903 (`PMC2821935`) — **read directly** — *this is NOT about a forced crossover*

The task asked me to confirm what this one is really about. It is about **staple breaks** and **crossover
density**, on crossovers that are all at default lattice positions. Verbatim:

> *"In our default design strategy, some staple breaks must be implemented between crossovers 8 bp apart. For
> the two-layer and three-layer structures, very few such breaks need to be incorporated. However, for the
> six-layer design, many such breaks must be used. We observed significantly lower yield for these structures.
> Introducing these breaks may be destabilizing for the structure. **Alternatively, simply having a large
> number of layers with our default crossover pattern may be destabilizing, irrespective of the position of the
> breaks.** For the 8 × 8 design, we avoided the implementation of such staple breaks by omitting many
> crossovers in the core of the block. For this design, we observed a high yield of well-folded structures.
> These results suggest that omitting crossovers produces more relaxed structures that are easier to realize or
> else that the omission of staple breaks positioned between crossovers 8 bp apart could improve folding quality
> as well. **Future systematic studies will be required to determine the relative importance of these staple
> breaks toward affecting folding efficiency.**"*

Three things follow, and they matter for how this repository currently cites this passage:

1. The crossovers here are **canonical**. What is forced is the *staple break* (a nick) that a canonical
   crossover pair 8 bp apart obliges — a consequence of crossover **density**, not of crossover position.
2. **There is no number.** *"Significantly lower yield"* and *"a high yield"* are the only quantifications.
3. **The paper itself declares the attribution unresolved** — the two bolded sentences say the cause may be
   the layer count rather than the breaks, and that systematic study is still required. A citation of this
   passage as *"a published yield cost"* of a crossover 8 bp from another is stronger than the source.

### 4.3 Ke, Voigt, Shih et al., *JACS* 2012, hexagonal and hybrid lattices (`PMC3336742`) — **read directly** — *the closest thing to a forced-crossover DEMONSTRATION*

This is the one paper found in which crossovers are placed where caDNAno's rules do not put them, and it says
so in as many words:

> *"Currently, all crossovers need to be manually implemented in caDNAno for hexagonal-lattice or hybrid
> origami."*

Crossovers occur at **13 bp (7/6 turns)** in the "Long" version and **9 bp (5/6 turns)** in the "Short"
version; the hybrid designs re-pitch to **10.8 bp/turn with one crossover every 54 bp** (56HB) and
**10.4 bp/turn, one every 52 bp** (52HB). The results: *"both hybrid-lattice origami successfully assembled
with high yields"* — **no percentage is given, anywhere in the paper**. The only quantitative
crossover-density result is structural, not a yield:

> *"an effective diameter of 2.60 nm (SD ± 0.17 nm) … significantly larger than the 2.4 nm effective DNA
> diameter reported earlier for honeycomb-lattice origami. This trend is roughly consistent with the lower
> crossover density employed (every 9 bp in hexagonal lattice versus every 8 bp in square lattice versus every
> 7 bp in honeycomb lattice)."*

and the L/S comparison is qualitative: *"the L-version shapes appear more disordered, while the S-version
objects appear more compact and well-defined. Presumably the higher density of crossovers in the S-version
hexagonal-lattices results in a stronger constraint on DNA double helix positional fluctuation."*

**So: forced crossovers demonstrably fold, and nobody has priced them.**

### 4.4 Majikes et al., *Nucleic Acids Res.* 2026 (`PMC12907558`) — **read directly** — the broadest design→yield survey

*"one of the broadest explorations to date of design variations within a fixed geometry"*: **six staple
motifs, twelve scaffold rotations, eight seam-symmetry variants, two multi-seam designs, four
helix-direction designs, three inter-helix-angle designs**, all measured as AFM defectivity. It is the right
*shape* of experiment and the wrong *variable*: **not one variant is an off-rule crossover position**.
Its nearest statement is a null one — *"We observed significant changes for modified staple motifs, but
without a clear interpretation for these changes"* — and the motif change it describes
(8-16-8 → 16-16) *"will convert the double crossovers to single crossovers, but not change the number or
position of topological links"*.

### 4.5 Parallel vs antiparallel crossovers, 2025 (`PMC12182888`) — **read directly** — a non-default crossover TYPE, measured

36 competitive folding experiments over 12 designs (6 antiparallel AX, 6 parallel PX), AFM-counted, giving a
merged ranking `A3 > A1 ≈ A2 > P3 > P1 ≈ P2 > A5 ≈ A6 > P5 ≈ P6 > A4 > P4` and, e.g., an average relative
formation yield for design P1 against all six AX designs of **35.5 %**. Conclusion: *"a preferential
formation of AX over PX conformation"*. This prices a crossover **topology** outside caDNAno's default
assumption; it does not price a crossover at a forced **position**, and the yields are *relative* (competitive)
rather than absolute.

### 4.6 Crossovers per staple as an entropic penalty, 2025 (`PMC12696799`) — **read directly**

*"limiting the number of crossovers per staple should be prioritized over extending staple binding domains, as
the entropic penalty dominates the favorable binding"*, and *"fewer scaffold crossovers result in more
cooperative folding"*, with a table of folding/unfolding temperatures against total and unbridged scaffold
crossover counts. A crossover **count** result, on canonical crossovers.

### 4.7 Off-register junction arm lengths predicted structurally, 2014 (`PMC4268701`) — **read directly**

*"slightly varying the lengths of the two duplex arms from 20 to 22 bps. Because each of the four-way
junctions … are interconnected by B-form DNA duplexes that resist bending, twisting and stretching, over- and
under-winding and stretching induced by these mismatches in tile dimensions are expected to result in ribbon
structures that deviate from the flat, ground-state structure observed for the 21 × 21 bp case."*
This is the right *physics* — a junction lattice put off its own register — but the output is a predicted
**shape**, validated against AFM. No strain energy per junction is reported, and no yield.

### 4.8 Rothemund 2006, 63 % → 11 % — **read directly** (from `gpd/data/T-151-sources/DNAorigami-nature.txt`) — *this is NOT about a crossover at all*

The task asked me to confirm this one. Verbatim:

> *"Many of the structures observed were star fragments (Fig. 2c, lower AFM image), and only 11 % (S = 70) were
> well-formed. The low yield of stars (and squares, see above) **may be due to strand breakage occurring during
> BsrBI digestion or subsequent steps to remove the enzyme**; when untreated circular scaffold was folded into
> stars, 63 % (S = 43) were well-formed."*

(Transcription note: `pdftotext` renders this paper's `=` as `¼` and its `<` as `,`. Both are restored above;
the raw strings in `gpd/data/T-151-sources/DNAorigami-nature.txt` read `S ¼ 70`, `S ¼ 43`, `,1%, S ¼ 199`.)

So the 63 % → 11 % is the cost of **enzymatically linearising the scaffold**, and Rothemund attributes it, with
a hedge, to **strand breakage during the digestion** — a chemistry cost, not a routing cost and certainly not a
crossover cost. Citing it as the price of a routing change over-reads the source.

A genuinely nearby number is on the same page and is about a **joint topology**, not a crossover: of the
triangle built from three separate raster-fill domains where *"only single covalent bonds along the scaffold
hold the domains together"*, Rothemund reports *"the desired equiangular triangles (upper AFM image) were
rarely observed (< 1 %, S = 199)"*.

---

## 5. What would have to exist for the answer to be yes

A published experiment in which one design is folded with `n` crossovers at default lattice positions and an
otherwise identical design is folded with one or more of them moved off-rule, with a yield, melting temperature
or defect rate for each. **No such experiment was found in 68 queries across 7 families.**
Every crossover-related design variable that *has* been swept (spacing, density, count, type, staple-break
position, twist density) moves the whole lattice or the whole staple set, so nothing in the literature isolates
a single forced junction — and both of the field's own review-level statements on the point
(caDNAno's *"more work is also needed"*, 2009; Ke et al.'s *"future systematic studies will be required"*, 2009)
say the same thing and have not been answered in the seventeen years since.

One paper would falsify this: any measurement, of any kind, on a design differing from a control only in the
lattice position of a crossover.
