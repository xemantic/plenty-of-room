# `T-191` sources — where does a real crossover-linked DNA bundle sit between the NONE and RIGID limits?

The question: a multilayer bundle's flexural rigidity is bracketed by
`EI = n·EI_duplex` (independent layers) and `EI = n·EI_duplex + S·Σyᵢ²` (full composite action),
which differ by ~40× for a four-layer stack.
What does **measurement** say?

Every load-bearing number below carries a **read status**, per `CLAUDE.md`'s research practice.
`gpd/data/` was checked **before** anything was fetched: it holds **no** persistence-length or
bundle-rigidity source. The only pre-existing trace of the target paper is its **reference entry**
in `../T-182-sources/yoo.txt` (Yoo & Aksimentiev 2013, ref 18), which is where the author list was
corrected — the paper is **Kauert, Kurth, Liedl & Seidel**, not "Kauert, Kurth, Schlichthaerle & Dietz",
and it is **Seidel's** group (TU Dresden), not Dietz's.

## The primary target — NOT OBTAINED

| source | status |
|---|---|
| Kauert, Kurth, Liedl & Seidel, *Nano Lett.* **11**:5558–5563 (2011), `10.1021/nl203503s` | **ABSTRACT ONLY — read directly** from EuropePMC (`epmc-kauert.json`, PMID 22047401). Full text **NOT FOUND**: ACS `pubs.acs.org` returns `403` to the article, the PDF and the SI; Unpaywall reports `oa_status: closed`, `has_repository_copy: false` (`unpaywall-kauert.json`); Semantic Scholar `openAccessPdf.status: CLOSED` (`s2-kauert.json`); `pmc.ncbi.nlm.nih.gov/articles/pmid/22047401/` serves a 21 kB reCAPTCHA page; `api.core.ac.uk` is Cloudflare-gated; the Seidel lab page `home.uni-leipzig.de/mbp/…` does not resolve; no TU Dresden / Qucosa thesis copy was found. |

Its numbers were therefore recovered from **two independent secondary sources that were read
directly and agree with each other**, and are flagged as such below.

## Fetched here — read directly

| file | source | used for | read status |
|---|---|---|---|
| `arxiv-2006.15029.pdf`, `.txt` | Chhabra, Mishra, Cao, Prešern, Skoruppa, Tortora & Doye, *"Computing the elastic mechanical properties of rod-like DNA nanostructures"*, arXiv:2006.15029v1 (2020) | **Table 1** columns `l_b^expt` / `C_expt` carry **Kauert's own measured values with attribution to his ref 29**: 4HB-MT `740 / 390 nm`, 6HB-MT `1880 / 530 nm`. Also its own oxDNA values, the `n`-dependence, and **Eq. 23 / S9** — the RIGID composite limit written as `l_b^tube = n l_b^duplex (1 + 2(R/r)²)` from the parallel-axis theorem | **READ DIRECTLY** |
| `PMC9494703-fullTextXML.xml`, `.txt` | Zhang *et al.*, *"Recent Progress of Magnetically Actuated DNA Micro/Nanorobots"*, *Cyborg Bionic Syst.* (2022) | independent restatement of Kauert: *"the bending stiffness of 4 Helix Bundle (HB) and 6HB was 15-folds and 38-folds stronger than floppy DNA duplexes"* — `15 × 50 = 750` and `38 × 50 = 1900 nm`, cross-checking Chhabra's 740 / 1880 | **READ DIRECTLY**; its torsional sentence is **garbled** (it swaps 4HB and 6HB), so only the bending ratios are used |
| `PMC3326316-cando2012-fullTextXML.xml`, `cando2012.txt` | Kim, Kilchherr, Dietz & Bathe, *Nucleic Acids Res.* **40**:2862 (2012) | CanDo **is** the RIGID limit and says so: *"modeled as bundles of isotropic elastic rods that are **rigidly constrained** to their nearest neighbors at specific crossover positions"*, with `S = 1100 pN`, `EI = 230 pN nm²`, `GJ = 460 pN nm²`. Also the 32-helix honeycomb crossover-density experiment (1 per 21 / 42 / 63 / 84 bp) — **qualitative** (RMSF distributions), no rigidity number | **READ DIRECTLY** |
| `PMC3749440-fullTextXML.xml`, `.txt` | Pfitzner, Wachauf, Kilchherr, Pelz, Shih, Rief & Dietz, *Angew. Chem. Int. Ed.* **52**:7766 (2013) | an independent 6HB measurement: *"persistence lengths of 2 μm for the six-helix bundle and 3.5 μm for the eight-helix bundle"*, honeycomb, 7560 nt scaffold | **READ DIRECTLY** |
| `PMC3267479-articlepage.html`, `PMC3267479.txt` | Wang, Schiffels, Martinez Cuesta, Seeman & Fygenson, *J. Am. Chem. Soc.* **134**:1606 (2012) | the **published statement of the RIGID limit and of its failure**: the *"naïve model … a ring of **rigidly linked** rods"*, `p_tube/p_helix = N[1 + 2(R/r)²]`, predicting `2.7 / 4.4 / 5.25 μm` against measured `1.0 ± 0.1 / 3.6 ± 0.6 / 5.0 ± 0.5 μm` for 6HB / 6HB+2 / 6HB+3 | **READ DIRECTLY** (EuropePMC `fullTextXML` returned **0 bytes**; the PMC *article page* served the complete author manuscript — `CLAUDE.md` records this route) |
| `PMC10395309-fullTextXML.xml`, `.txt` | Li, Wang *et al.*, *"Mechanics of dynamic and deformable DNA nanostructures"*, *Nanoscale* (2023) | the only description found of **Kauert's own model**: *"They studied four different types of boundary conditions between the bundles (fully disconnected in red, fully attached in blue, and two partially attached in green and yellow). They concluded that the most reasonable conditions were the two partial attachments."* | **READ DIRECTLY**; its sentence about CanDo (*"neighboring bases can slide but not separate"*) **contradicts CanDo's own primary text** and is not used |
| `PMC12648286-fullTextXML.xml`, `.txt` | Lee *et al.*, *"SNUPI: A Computational Framework for Rapid Mechanical Analysis of Structured DNA Assemblies"* (2025) | a finite-element prediction with **non-rigid** crossovers: 16-helix square-lattice bundle, bending PL **13 063 nm**, torsional **2 463 nm** | **READ DIRECTLY** — a **model**, not a measurement |
| `PMC9853505`, `PMC7467825`, `PMC8643692`, `PMC11291742`, `PMC12452858` (`.xml`, `.txt`) | context reviews and adjacent papers | scanned for a bundle-rigidity table; **none carries one**. Retained because they are the recorded negative | **READ DIRECTLY (scanned)** |
| `epmc-kauert.json`, `epmc-cando2012.json`, `crossref-kauert-search.json`, `crossref-others.json`, `s2-kauert.json`, `unpaywall-kauert.json` | Crossref / EuropePMC / S2 / Unpaywall records | citation verification and the abstracts of Schiffels *et al.* (2013), Lee *et al.* (2019), Pfitzner *et al.* (2013), Wang *et al.* (2012) | **READ DIRECTLY** |

## Abstract-only

| source | quantity | status |
|---|---|---|
| Schiffels, Liedl & Fygenson, *ACS Nano* **7**:6700 (2013), `10.1021/nn401362p` | the one source that names interduplex **shear** explicitly: *"In estimating the elastic energy we account for bending and twisting of the individual duplexes as well as **shearing between them**."* Full text closed (ACS, no PMCID) | **ABSTRACT ONLY — read directly** (EuropePMC core record) |
| Lee, Kim, Kim, Lee & Kim, *ACS Nano* **13**:8329 (2019) | *"Individually engineered defects that are short single-stranded DNA (ssDNA) gaps could reduce up to 70% of the bending stiffness of DNA origami constructs"* | **ABSTRACT ONLY — read directly** |

## What is NOT found

1. **No measurement of the bending rigidity of a multilayer origami PLATE / slab** — as opposed to a
   rod-like bundle — was found. 41 EuropePMC queries (below) surfaced none; the closest are the
   32-helix honeycomb *bundle* of Kim *et al.* (2012), which is qualitative, and closed-access
   *"Deformation-Resistant, Double-Layer DNA Self-Assembled Nanoraft"* (no rigidity in its title/abstract record).
2. **No shear modulus and no shear-lag length for a multilayer bundle.** `"DNA nanostructure" AND "shear lag"`
   returns **0 hits**; `"DNA origami" AND "interhelical" AND "shear"` returns 6, none relevant.
   The nearest published quantities are Kauert's own four boundary conditions (via the 2023 review)
   and Schiffels *et al.*'s shear term (abstract only).
3. **No "effective Young's modulus" for an origami bundle** — `"DNA origami" AND "effective Young's modulus"`
   returns **0 hits**. Wang *et al.* (2012) instead *assume* the duplex's own modulus for the bundle.

## Derived here — NOT published anywhere

`composite-fraction.py` converts each measured persistence length to `EI = l_b kT` and places it between
the two limits with CanDo's own `S = 1100 pN` and `EI_duplex = 230 pN nm²`. **This arithmetic is this
repository's, not any source's.** Every measured bundle lands at **f = 0.26–0.33**; the RIGID limit
over-predicts `EI` by **2.7–3.2×**. Re-runnable.

## Query log

- `query.py` / `query.log` / `europepmc-queries.json` — 21 EuropePMC queries in five families
  (the primary measurement, other bundle measurements, interlayer shear / composite action,
  plates and slabs, modelling frameworks).
- `targeted-queries.json`, `targeted-queries-2.json`, `targeted-queries-3.json` — 20 further
  title- and phrase-level queries (SNUPI, engineered defects, multilayer plates, 24HB/32HB,
  overestimation of persistence lengths).
- Fetch attempts that **failed** and are therefore not files here:
  `pubs.acs.org/doi/pdf/10.1021/nl203503s` → `403`;
  `pubs.acs.org/doi/suppl/10.1021/nl203503s/suppl_file/nl203503s_si_001.pdf` → `403`;
  `pmc.ncbi.nlm.nih.gov/articles/pmid/22047401/` → `200` reCAPTCHA;
  `api.core.ac.uk/v3/search/works` → `403` Cloudflare;
  `europepmc.org/articles/PMC8966688?pdf=render` and `.../PMC3267479?pdf=render` → `500`;
  `ebi.ac.uk/…/PMC8966688/fullTextXML` and `.../PMC3267479/fullTextXML` → `404` / 0 bytes;
  `home.uni-leipzig.de/mbp/…` → connection failure;
  `rsc.org/suppdata/nr/c4/c4nr07153k/c4nr07153k1.pdf` → `404`.

## Consumed by

`C-0109` (`T-191`), §3, and `src/main/kotlin/tile/FourLayerTile.kt`'s `MeasuredBundleRigidity`,
whose composite fractions are asserted as executable tests in `src/test/kotlin/tile/FourLayerTileTest.kt`
(gate 5: Kauert's 6HB honeycomb 0.3019, Kauert's 4HB square 0.2885, Pfitzner's 6HB 0.3253).
