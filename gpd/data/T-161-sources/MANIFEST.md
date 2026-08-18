# T-161 source manifest

*Can a crossover be drawn at the LAST base pair of a duplex?* — the sources fetched for
[`T-161`](../../tasks/T-161.md), claim [`C-0095`](../../claims/C-0095-row-end-crossover.md).

Everything here was fetched by the retained driver `tools/T-161-fetch-sources.py`
(`python3 tools/T-161-fetch-sources.py`, re-runnable; `T161_SKIP_SEARCH=1` fetches only the
non-EuropePMC URLs).
EuropePMC REST searches were spaced 8 s apart, per `CLAUDE.md`.
`curl`-equivalent requests carried a desktop-Chrome `User-Agent`.

**The primary source of this task is already in the repository.**
Rothemund 2006, its Supplementary Notes and the caDNAno paper were fetched and manifested by
`T-151` and live in [`../T-151-sources/`](../T-151-sources/MANIFEST.md);
`T-161` re-read them rather than re-fetching them, and every passage it quotes is listed below
with the file and line it was read at.

## Read directly, from `gpd/data/T-151-sources/` (fetched by `T-151`, re-read here)

| Source | File | Locator | Read status | What it settles |
|---|---|---|---|---|
| Rothemund, *Nature* **440**:297 (2006), main text | `DNAorigami-nature.txt` | p. 298 col. 1 (line 68-71 of the `pdftotext -layout` extraction) | **read directly** | the odd-half-turn rule is written on *"the distance between successive scaffold crossovers"* — in a boustrophedon, the two ends of one row |
| Rothemund 2006, Supplementary Note S2 | `DNAorigami-supp1.linux.txt` | lines 154-158 | **read directly** | *"even where a seam or edge lines up with the underlying crossover lattice"* — the 38.08 nm case, contemplated and not forbidden; and the strain it carries |
| Rothemund 2006, Supplementary Note S2 | `DNAorigami-supp1.linux.txt` | lines 161-165 | **read directly** | *"the last base pair does form and assumes a planar configuration"*; the relief mechanism is unknown; the remedy is one or two unpaired scaffold bases |
| Rothemund 2006, Supplementary Fig. S19 | `DNAorigami-supp1.linux.txt` | lines 1126-1127 | **read directly** | the rectangle of Fig. 2b is *"27 turns wide at 10.666 bases / turn -> 288 nt"*, *"24 helices tall"* — 288 bp is **18 column pitches exactly** |
| Rothemund 2006, main text | `DNAorigami-nature.txt` | line 164 | **read directly** | that rectangle's yield: *"The yield of well-formed rectangles was high (90%, S = 40)"* (the extraction renders `=` as a `1/4` ligature) |
| Rothemund 2006, Supplementary Note S5.7 | `DNAorigami-supp1.linux.txt` | lines 2428-2429 | **read directly** | *"rectangles which have many parallel blunt-ends along their left and right edges"* — the vertical edge presents duplex termini |
| Douglas et al., *NAR* **37**:5001 (2009), caDNAno | `PMC2731887-fullTextXML.xml` | Results and Discussion, ¶1 | **read directly** | the default crossover rule is **azimuthal** (points of closest proximity), and *"caDNAno permits the user to force crossovers between any two staple bases or between any two scaffold bases"* |

## Fetched for T-161

| Intended source | URL | HTTP | Saved path | Bytes | Content check | Read status |
|---|---|---|---|---|---|---|
| scadnano Python package API reference (the design language's own semantics) | `https://scadnano-python-package.readthedocs.io/en/latest/` | 200 | `scadnano-python-package-readthedocs.html` | 736167 | real Sphinx HTML, 148 405 characters of text, 59 occurrences of *"crossover"*; `Domain.has_crossover_at` reads *"An xover is necessarily at an enpoint of a strand"* (`enpoint` is the source's typo), attributed to cadnano2's `strand.py hasXoverAt` | **read directly** |
| scadnano documentation index | `https://scadnano.readthedocs.io/en/latest/` | **404** | `scadnano-readthedocs-index.html` | 2160 | a Read the Docs 404 page, discarded as a source; the Python-package site above carries the API | **not found** |
| cadnano2 user documentation | `https://cadnano.org/docs.html` | 200 | `cadnano-org-docs.html` | 13766 | real HTML, 2 402 characters of text; *"Automatic scaffold rasterization: Adjacent strands that are added via a click-and-drag operation in the lattice view will be automatically resized and connected via crossovers"* | **read directly** |
| Ke, Douglas, Liu, Zhang, Lindsay, Yan, *JACS* **131**:15903 (2009), *Multilayer DNA origami packed on a square lattice* — EuropePMC full text | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2821935/fullTextXML` | **404** | `PMC2821935-Ke2009-fullTextXML.xml` | 0 | empty | **not found** |
| the same, PMC article page (new host) | `https://pmc.ncbi.nlm.nih.gov/articles/PMC2821935/` | 200 | `PMC2821935-Ke2009-articlepage.html` | 20355 | **NOT the article**: 2 643 characters, all of it a Google *"Checking your browser"* challenge stub with no article text | **not found** |

**A guessed identifier failed and is recorded rather than deleted.**
The first attempt used `PMC2783486`, recalled rather than searched;
it resolved to *"Barriers to early detection and treatment of head and neck squamous cell
carcinoma in African American men"*.
`CLAUDE.md` already says never to guess an identifier, and this is the second instance in the
repository. The correct PMCID, `PMC2821935`, came from the recorded
`TITLE:"Multilayer DNA origami packed on a square lattice"` search — which then also failed to
serve the text. **Ke et al. 2009 is flagged `not found` and nothing in `C-0095` rests on it.**

## Search log

| File | Bytes | Note |
|---|---|---|
| `queries.md` | 7632 | **Twelve** EuropePMC REST searches, verbatim, with HTTP status, `hitCount` and the top five hits each |
| `search-_DNA_origami_AND_boustrophedon_.json` | 200 | `"DNA origami" AND "boustrophedon"` — **0 hits** |
| `search-_DNA_origami_AND_crossover_AND_helix_terminus_.json` | 219 | `"DNA origami" AND "crossover" AND "helix terminus"` — **0 hits** |
| `search-_DNA_origami_AND_crossover_AND_last_base_.json` | 6359 | 1 hit, *Constructing Large 2D Lattices Out of DNA-Tiles* — not on the question |
| `search-_DNA_origami_AND_crossover_AND_helix_end_.json` | 19375 | 3 hits, none on the question |
| `search-TITLE_Multilayer_DNA_origami_packed_on_a_square_lattice_.json` | 5877 | 1 hit — how `PMC2821935` was recovered |
| `search-_scaffold_crossover_AND_raster_.json` | 28876 | 4 hits, incl. the caDNAno paper |
| `search-_DNA_origami_AND_edge_staple_.json` | 67181 | 9 hits |
| `search-scadnano.json` | 91252 | 14 hits |
| `search-_DNA_origami_AND_terminal_base_pair_.json` | 124542 | 18 hits |
| `search-caDNAno_AND_crossover_AND_design_rules_.json` | 161034 | 23 hits |
| `search-_DNA_origami_AND_edge_AND_strain_AND_crossover_.json` | 179532 | 89 hits |
| `search-_DNA_origami_AND_unpaired_AND_scaffold_AND_edge_.json` | 209676 | 109 hits |
| `fetches.md` | 1100 | the non-EuropePMC fetches with their HTTP status, as the driver emitted them |

## The negative existence result

**No source found — in twelve recorded EuropePMC searches, in Rothemund 2006 and its
Supplementary Notes, in the caDNAno paper, in the scadnano API reference or in the cadnano2
documentation — forbids a crossover at the terminal base pair of a duplex.**
The two searches written to find such a prohibition directly return **zero hits**.
One paper falsifies this; the queries are above so that it can.
