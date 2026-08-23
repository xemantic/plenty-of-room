# `T-255` sources — the cadnano.org gallery, opened

Retrieved **2026-08-23**.
The subject of this directory is the **web**, so the date is part of the record
([`CLAUDE.md`](../../../CLAUDE.md): a result file whose subject is the corpus or the web must name
the state it measured).

`T-246` read the gallery **page** live and at its 2012 Wayback capture and recorded three
citations, three `.zip` links and no yields.
It did not open the `.zip` files.
This directory opens them.

## What the gallery links, and what came back

The live page hides its three archives behind `bit.ly` shorteners;
the 2012 Wayback capture `T-246` retained (`gpd/data/T-246-sources/wayback-2012-gallery.html`)
states the resolved paths outright.
**All three shorteners resolve, in 2026, to live Dropbox files, and all three downloaded.**

| archive | bytes | sha256 (first 16) | designs |
|---|---|---|---|
| `Science09.zip` | 777635 | `4ec08bbd82bb0aac` | **12** |
| `NAR09.zip` | 444985 | `e7459abf5224ecb9` | **7** |
| `Nature09.zip` | 3589234 | `c722544c1da96782` | **7** |

**26 caDNAno legacy `.json` design files**, and nothing in any archive is a picture.
`Nature09.zip` also carries an `.svg` rendering beside each design.

## Every URL tried, with its HTTP status

| URL | status | bytes | note |
|---|---|---|---|
| `http://bit.ly/U1CwqS` | **200** | 195531 | the gallery's Science 2009 link; resolves to Dropbox |
| `http://bit.ly/XJvTOI` | **200** | 195049 | the gallery's NAR 2009 link; resolves to Dropbox |
| `http://bit.ly/WJPCI1` | **200** | 194932 | the gallery's Nature 2009 link; resolves to Dropbox |
| `https://cadnano.org/gallery.html` | **200** | 12231 | the gallery page, refetched; identical in substance to `T-246`'s |
| `https://www.dropbox.com/scl/fi/9lhrzf8r2svfkyten4izb/Science09.zip?rlkey=n257w8pbudlwn9964ygw...` | **200** | 777635 | the archive itself, `&dl=1` |
| `https://www.dropbox.com/scl/fi/j0s0zxzctf8nzc77l28xj/NAR09.zip?rlkey=dwezs6wgre4lzvcinh0u4ij1...` | **200** | 444985 | the archive itself, `&dl=1` |
| `https://www.dropbox.com/scl/fi/swn5n0yij823rmtrs242p/Nature09.zip?rlkey=wf4rgxmyu6r5i3t1x8so0...` | **200** | 3589234 | the archive itself, `&dl=1` |
| `https://cdn.ncbi.nlm.nih.gov/pmc/blobs/1fbe/2731887/3132da3bc609/gkp436f2.jpg` | **200** | 315359 | caDNAno paper Figure 2, for the per-design yield chart |
| `https://europepmc.org/api/get/articleApi?query=TITLE%3A%22Folding%20DNA%20into%20twisted%20an...` | **200** | 1089 | Dietz 2009 lookup; the search endpoint below is what answered |
| `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2737683/fullTextXML` | **404** | 0 | **404** — EuropePMC has no full text for this PMCID |
| `https://europepmc.org/articles/PMC2737683?pdf=render` | **500** | 0 | **500** — the documented PDF route fails for this PMCID |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC2737683/` | **200** | 151204 | Dietz 2009, **read directly**; `T-246` got a reCAPTCHA page for the same PMCID, so the gate is intermittent — retry before recording a source as unreachable |
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC2731887/pdf/gkp436.pdf` | **200** | 4274175 | caDNAno paper PDF, through PMC's SHA-256 proof-of-work cookie (`tools`-free, `pmc_pow.py`) |

## Files

| file | what | how read |
|---|---|---|
| `Science09.zip`, `NAR09.zip`, `Nature09.zip` | the three gallery archives, **unmodified** | parsed |
| `cadnano_legacy.py` | a reader for the caDNAno **legacy** `.json` format; reads the archives without unpacking them, and **parses** the honeycomb lattice constants out of `src/main/kotlin/tile/HoneycombBondClassResidues.kt` | — |
| `forced_census.py` | the three forced-crossover tests (adjacency, register, alignment), **37 self-tests** | — |
| `run_census.py` | runs the three tests over all 26 designs into `census-raw.json` | — |
| `census-raw.json` | the full per-design census, including every forced instance | — |
| `digitise_fig2de.py` | digitises Figure 2d/2e of the caDNAno paper from the publisher PDF at 600 dpi, with the paper's own two ordinal statements as cross-checks | — |
| `cadnano-NAR-fig2de-digitised.json` | the seven per-design yields it recovers | — |
| `cadnano-NAR-gkp436.pdf`, `cadnano-NAR-gkp436.txt` | Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Res.* **37**:5001 (caDNAno) | **read directly** |
| `cadnano-NAR-fig2.jpg` | the same figure at PMC's 620 px, too coarse to digitise — kept as the record of why the PDF was fetched | **read directly** |
| `PMC2737683-dietz2009-articlepage.html`, `.txt` | Dietz, Douglas & Shih, *Science* **325**:725 | **read directly** |
| `cadnano-org-gallery-2026.html` | the gallery page as it stands in 2026 | **read directly** |
| `fetch.py`, `pmc_pow.py`, `batch*.json`, `fetches.json` | the retrieval driver, PMC's proof-of-work solver, and the full log | — |

## Sources already in `gpd/data/`, and NOT refetched

[`CLAUDE.md`](../../../CLAUDE.md): *check `gpd/data/` BEFORE fetching anything*. It paid a fifth time.

| already on disk | what it supplied here |
|---|---|
| `gpd/data/T-246-sources/wayback-2012-gallery.html` | the three archives' resolved URLs |
| `gpd/data/T-246-sources/cadnano-org-gallery.html` | the `bit.ly` shorteners and the citation list |
| `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml` | the caDNAno paper's text, including its scaffold-pairing list and its own gallery sentence |
| `gpd/data/T-296-sources/PMC2688462-douglas2009.txt` | the Nature paper's pooled `7% to 44%` yield range |
| `gpd/data/T-296-sources/douglas2009-SI.pdf` | checked for a per-shape yield map; it carries staple sequence tables and none |

