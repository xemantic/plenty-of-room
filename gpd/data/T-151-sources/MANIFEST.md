# T-151 source manifest

All fetches used `curl -sL` with a desktop-Chrome User-Agent.
EuropePMC requests were spaced ~8 s apart.
PDFs were converted with `pdftotext -layout`; both the PDF and the `.txt` are kept.

| Intended source | URL attempted | HTTP | Saved path | Bytes | Content check |
|---|---|---|---|---|---|
| Rothemund 2006, Nature 440:297 — main text (PDF) | `https://www.dna.caltech.edu/Papers/DNAorigami-nature.pdf` | 200 | `DNAorigami-nature.pdf` | 589244 | real PDF, 6 pages; pdftotext -layout gave DNAorigami-nature.txt (42020 B), head shows the Nature volume/DOI line |
| Rothemund 2006 — Supplementary Notes 1-11 (SI part 1) | `https://www.dna.caltech.edu/Papers/DNAorigami-supp1.pdf` | 404 | `(not saved; 404 body was a 196-byte Apache 'Not Found' page, discarded)` | - | checked with head -c 300: HTML 404 page. Correct filename found on the Caltech publications page (see next row). |
| Rothemund 2006 — Supplementary Notes 1-11 (SI part 1), correct filename | `https://www.dna.caltech.edu/Papers/DNAorigami-supp1.linux.pdf` | 200 | `DNAorigami-supp1.linux.pdf` | 6304544 | real PDF, 82 pages; text at DNAorigami-supp1.linux.txt (357690 B), head -c 150 shows the SI title/author block |
| Rothemund 2006 — Supplementary Note 12 (SI part 2) | `https://www.dna.caltech.edu/Papers/DNAorigami-supp2.pdf` | 200 | `DNAorigami-supp2.pdf` | 192844 | real PDF, 9 pages; text at DNAorigami-supp2.txt (514941 B), head -c 150 shows the SI Note 12 title/author block |
| Caltech DNA group Papers/ directory listing | `https://www.dna.caltech.edu/Papers/` | 200 | `Papers-index.html` | 520 | not a listing: a 520-byte HTML meta-refresh stub redirecting to the publications page (read in full). |
| Caltech DNA group publications page (used to recover the SI filenames) | `http://dna.caltech.edu/DNAresearch_publications.html` | 200 | `pubs.html` | 253864 | real HTML publication list; grepped its href list for origami/supp entries. |
| Rothemund, 'Design of DNA origami', ICCAD 2005 (found on the same publications page) | `https://www.dna.caltech.edu/Papers/rothemund-origami-iccad05.pdf` | 200 | `rothemund-origami-iccad05.pdf` | 661756 | real PDF, 8 pages; text at rothemund-origami-iccad05.txt (54101 B). Fetched opportunistically, not on the task list. |
| Douglas et al. 2009, NAR 37:5001, caDNAno — PMC2731887 full text | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2731887/fullTextXML` | 200 | `PMC2731887-fullTextXML.xml` | 46666 | real JATS XML (head -c 400 shows <article> + NAR journal-meta), not empty, not an error page. |
| Douglas et al. 2009 — PMC article page (new host), as a backup copy | `https://pmc.ncbi.nlm.nih.gov/articles/PMC2731887/` | 200 | `PMC2731887-articlepage.html` | 141375 | real article HTML (head -c 300 shows a normal PMC page head), not a reCAPTCHA stub. |
| Snodin, Schreck, Romano, Louis, Doye 2019, NAR 47:1585 — PMC6379721 full text | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC6379721/fullTextXML` | 200 | `PMC6379721-fullTextXML.xml` | 155949 | real JATS XML (head -c 400 shows <article> + NAR journal-meta). |
| EuropePMC search hit — 'Design principles for accurate folding of DNA origami' (2024) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11621765/fullTextXML` | 200 | `PMC11621765-fullTextXML.xml` | 87390 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |
| EuropePMC search hit — 'DNA Origami Design: A How-To Tutorial' (2021) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11419732/fullTextXML` | 200 | `PMC11419732-fullTextXML.xml` | 183728 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |
| EuropePMC search hit — 'Reverse engineering DNA origami nanostructure designs from raw scaffold and staple sequence lists' (2023) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC10371787/fullTextXML` | 200 | `PMC10371787-fullTextXML.xml` | 165407 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |
| EuropePMC search hit — 'Mechanism of DNA origami folding elucidated by mesoscopic simulations' (2024) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11001925/fullTextXML` | 200 | `PMC11001925-fullTextXML.xml` | 178063 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |
| EuropePMC search hit — 'Folding Competition and Dynamic Transformation in DNA Origami: Parallel Versus Antiparallel Crossovers' (2025) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC12182888/fullTextXML` | 200 | `PMC12182888-fullTextXML.xml` | 78457 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |
| EuropePMC search hit — 'Computer-Aided Production of Scaffolded DNA Nanostructures from Flat Sheet Meshes' (2016) | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC6680348/fullTextXML` | 200 | `PMC6680348-fullTextXML.xml` | 59295 | real JATS XML (head -c 60 shows a JATS DOCTYPE or <article>), non-empty. |

## Search logs

| File | Bytes | Note |
|---|---|---|
| `queries.md` | 7095 | Eight EuropePMC REST search queries, verbatim, with HTTP status, hitCount and the top five hits each. |
| `search-_DNA_origami__AND__raster_fill_.json` | 30555 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_DNA_origami__AND__scaffold_seam_.json` | 36473 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_DNA_origami__AND__seam__AND__rectangle_.json` | 54399 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_DNA_origami__AND__seam__AND__staple_.json` | 174167 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_DNA_origami__AND__seamless_.json` | 169893 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_scaffold_routing__AND__DNA_origami_.json` | 180976 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-_seamless__AND__scaffold__AND__DNA_nanostructure_.json` | 181815 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |
| `search-origami_AND_raster_AND__scaffold_routing_.json` | 50120 | Raw EuropePMC `search` JSON (`resultType=core`, `pageSize=25`), HTTP 200. |

## Failures

- `https://www.dna.caltech.edu/Papers/DNAorigami-supp1.pdf` — HTTP 404. Recovered as `DNAorigami-supp1.linux.pdf` (HTTP 200) via the Caltech publications page.
- `https://www.dna.caltech.edu/Papers/` — HTTP 200 but not a directory listing (meta-refresh stub).
- No other fetch failed.

