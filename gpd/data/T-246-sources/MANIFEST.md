# `T-246` source manifest

Literature search for a **published price on a forced / off-rule-position crossover in DNA origami**.
**Result: none exists.** See [`queries.md`](queries.md) for the full log and the verdict.

Every file below was produced by this task. Nothing here is edited by hand: the `*.txt` files are
tag-stripped renderings of the adjacent `*.xml` / `*.html` / `*.pdf` and are provided for grepping.

## Provenance key

- **read directly** — the article's own full text was fetched and the passage read in it.
- **abstract only** — only an abstract or editor's summary could be obtained.
- **not found** — no text obtainable by any route tried.

## Papers

| File(s) | Paper | Provenance | Route |
|---|---|---|---|
| `PMC2821935-articlepage.html`, `PMC2821935.txt` | Ke, Douglas, Liedl, Högberg, Shih, *Multilayer DNA origami packed on a square lattice*, **JACS 131:15903 (2009)** | **read directly** | `pmc.ncbi.nlm.nih.gov/articles/PMC2821935/` — EuropePMC `fullTextXML` returned 0 bytes and `?pdf=render` failed |
| `PMC3957201-articlepage.html`, `PMC3957201.txt` | Ke, Bellot, Voigt, Fradkov, Shih, *Two design strategies for enhancement of multilayer-DNA-origami folding*, **Chem. Sci. (2012)** | **read directly** | `pmc.ncbi.nlm.nih.gov` article page |
| `PMC3336742-articlepage.html`, `PMC3336742.txt` | Ke et al., *Multilayer DNA origami packed on hexagonal and hybrid lattices*, **JACS (2012)** | **read directly** | `pmc.ncbi.nlm.nih.gov` article page |
| `PMC4872871-articlepage.html`, `PMC4872871.txt` | Wei et al., *Design space for complex DNA structures*, **Nat. Commun. (2013)** | **read directly** | `pmc.ncbi.nlm.nih.gov` article page |
| `PMC2737683-articlepage.html`, `PMC2737683.txt` | Dietz, Douglas, Shih, *Folding DNA into Twisted and Curved Nanoscale Shapes*, **Science 325:725 (2009)** | **abstract only** | PMC record is a **stub** (167 characters); `science.org` 403; abstract from `crossref-dietz2009.json` |
| `PMC12907558.pdf`, `PMC12907558.txt` | Majikes et al., *Complex cooperativity in DNA origami revealed via design-dependent defectivity*, **NAR (2026)** | **read directly** | `europepmc.org/articles/PMC12907558?pdf=render` |
| `PMC12696799-fullTextXML.xml`, `PMC12696799.txt` | *Unraveling the Folding Dynamics of DNA Origami Structures* (2025) | **read directly** | EuropePMC `fullTextXML` |
| `PMC11621765-fullTextXML.xml`, `PMC11621765.txt` | *Design principles for accurate folding of DNA origami* (2024) | **read directly** | EuropePMC `fullTextXML` |
| `PMC4268701-fullTextXML.xml`, `PMC4268701.txt` | Pan et al., *Lattice-free prediction of three-dimensional structure of programmed DNA assemblies*, **Nat. Commun. (2014)** | **read directly** | EuropePMC `fullTextXML` |
| `PMC7727707-fullTextXML.xml`, `PMC7727707.txt` | *Insights into the Structure and Energy of DNA Nanoassemblies* (2020) | **read directly** | EuropePMC `fullTextXML` |
| `PMC7708044-fullTextXML.xml`, `PMC7708044.txt` | *Stretching DNA origami: effect of nicks and Holliday junctions on the axial stiffness* (2020) | **read directly** | EuropePMC `fullTextXML` |
| `PMC6499592-fullTextXML.xml`, `PMC6499592.txt` | *The sequence of events during folding of a DNA origami* (2019) | **read directly** | EuropePMC `fullTextXML` |
| `PMC12182888.txt` | *Folding Competition and Dynamic Transformation in DNA Origami: Parallel Versus Antiparallel Crossovers* (2025) | **read directly** | **already in the repository** — tag-stripped here from `gpd/data/T-151-sources/PMC12182888-fullTextXML.xml` |
| — (not copied) | Douglas et al., *Rapid prototyping of 3D DNA-origami shapes with caDNAno*, **NAR 37:5001 (2009)** | **read directly** | **already in the repository**: `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`. Both quotations in the task statement re-verified there. |
| — (not copied) | Rothemund, *Folding DNA to create nanoscale shapes and patterns*, **Nature 440:297 (2006)** | **read directly** | **already in the repository**: `gpd/data/T-151-sources/DNAorigami-nature.txt`. The 63 % → 11 % passage read there. |

## Search responses (raw)

| File(s) | What |
|---|---|
| `search-*.json` (54 files) | One raw EuropePMC REST response per query. Filename is the query slug. |
| `europepmc-batch{1..7}.json` | Per-batch index: query string, endpoint, hit count, response filename, and the top-25 hits with PMCID/DOI/year. |
| `batch{1..7}.json` / `.log` / `.err` | The query lists as submitted, the console digest, and the progress/retry stream. |
| `arxiv-*.xml` (10 files) | Raw arXiv Atom responses. |
| `arxiv-queries.json`, `arxiv-queries2.json`, `arxiv-queries-log.json`, `arxiv-queries2-log.json` | arXiv query lists and index. |
| `openalex-*.json` (4 files, ~800 kB each) | Raw OpenAlex responses. Fuzzy relevance search, used only as a cross-check; retained because a null result must be auditable. |
| `openalex-queries.json`, `openalex-queries-log.json` | OpenAlex query list and index. |

## Web fetches

| File | What | Provenance |
|---|---|---|
| `cadnano-org-index.html` | cadnano.org front page, fetched live. HTTP 200. | **read directly** |
| `cadnano-org-gallery.html` | **cadnano.org/gallery, fetched live.** The page the caDNAno paper points at for *"designs that folded successfully, although with varying yields"*. Contains three citations and three `.zip` links and **no yields**. | **read directly** |
| `cadnano-org-legacy.html` | cadnano.org legacy downloads page. Software only. | **read directly** |
| `wayback-avail-gallery.json` | Wayback availability query for a 2009-era gallery snapshot. | **read directly** |
| `wayback-2012-gallery.html` | **Archived cadnano.org/gallery, 2012-02-21** — the nearest surviving state to publication. Identical in substance to today's: three papers, three zips, no yields. | **read directly** |
| `wayback-cdx-2009-2012.txt` | Wayback CDX index request — returned HTTP 504, retained as the record of a failed route. | **read directly** |
| `crossref-dietz2009.json` | Crossref record for Dietz, Douglas & Shih, *Science* **325**:725 (2009), `10.1126/science.1174251`. **Abstract only** — the editor's summary. The article itself is paywalled (`science.org` → 403) and its PMC record `PMC2737683` is a 167-character stub. | **abstract only** |

## Tools retained

| File | What |
|---|---|
| `query.py` | EuropePMC REST runner (8 s sleep, 5 retries, saves raw JSON per query). Retained so the search is repeatable. |
| `arxiv.py` | arXiv API runner. |
| `openalex.py` | OpenAlex `/works?search=` runner. |
| `fetch.py` | Full-text fetcher: tries EuropePMC `fullTextXML`, falls back to `europepmc.org/articles/<id>?pdf=render`. |
| `fetches.json` | Per-PMCID fetch log: every URL tried, HTTP status, byte count, which route succeeded. |

## Not obtained

| Source | Why | Consequence |
|---|---|---|
| Dietz, Douglas & Shih, *Science* **325**:725 (2009), full text | `science.org` returns **403**; the PMC record `PMC2737683` is a 167-character stub; no repository or author copy found | **None.** Its one relevant claim — that a 60-helix bundle folded at higher yield underwound to 11 bp/turn than at 10.5 — is quoted verbatim in Ke et al. 2012 (`PMC3957201`), which **was** read directly, and it is recorded in [`queries.md`](queries.md) §4.1 as a second-hand statement. |

---

Total: **141** files, **12.4 MB**.
