# `T-302` sources — the 2009 supplementary staple tables, read

Read **2026-08-23**.
The subject of the retrieval log below is the **web**, so the date is part of the record
([`CLAUDE.md`](../../../CLAUDE.md)); the decisive artifacts are **immutable and were already on disk**,
and their `sha256` digests travel in the result file instead of a date.

## The two decisive sources were NOT fetched

[`CLAUDE.md`](../../../CLAUDE.md): *check `gpd/data/` BEFORE fetching anything.*
It paid a **sixth** recorded time, and this time it supplied the whole answer.

| already on disk | retained by | what it supplied here |
|---|---|---|
| `../T-296-sources/douglas2009-SI.pdf` | `T-296` | **the `monolith staple sequences` table**, and eleven more |
| `../T-255-sources/Nature09.zip`, `NAR09.zip` | `T-255` | the deposited caDNAno designs the tables were matched against |
| `../T-151-sources/PMC2731887-fullTextXML.xml` | `T-151` | the caDNAno paper's own pointer to *"Supplementary Notes 2 and 3"* |

## The rendering trap, and why it did not apply

`T-296` recorded that `douglas2009-SI.pdf` *"has no usable text layer — `pdftotext` returns 58 MB of `!"#$` glyphs"*,
and both [`CH-0251`](../../challenges/CH-0251-the-deposited-block-has-no-loops.md) and the `T-302` queue row
therefore budget this task as a **26-page raster transcription**.

That is true of **pages 1–11**, the strand-diagram figures, whose embedded TrueType subsets carry
**no `/ToUnicode`** and **no `post` table** — so the code points a reader gets are the subset's own.
It is **false of pages 12–23**, which carry the twelve staple sequence tables and are typeset normally.

**The check is one command** — `pdftotext -bbox -f 12 -l 26` — and it turned the task from a transcription into a geometric read.
A blanket statement about a PDF's text layer is a statement about the pages somebody looked at.

## Files

| file | what | how read |
|---|---|---|
| `si_tables.py` | the table reader and the design-file matcher: columns located from each table's **own header row**, rows grouped by `yMin`, sequences taken from the line below. **28 self-tests**, all on synthetic fixtures | — |
| `fetches-1.json` | every URL tried in the search for the caDNAno paper's **own** staple lists, with its HTTP status | — |
| `PMC2731887-articlepage.html` | the caDNAno paper on PMC, refetched for its supplementary links; its **only** pointer is the dead OUP legacy URL | **read directly** |
| `NAR-supplementaryFiles.zip` | EuropePMC's `supplementaryFiles` archive for `PMC2731887` — it answers, and it holds the two **figures** and nothing else | **read directly** |

## Every URL tried, with its HTTP status

All in the search for **Supplementary Note 3** of the caDNAno paper, which would carry staple lists for all seven cross-sections.

| URL | status | bytes | note |
|---|---|---|---|
| `https://pmc.ncbi.nlm.nih.gov/articles/PMC2731887/` | **200** | 141375 | the caDNAno paper; its only supplementary pointer is the dead OUP legacy URL |
| `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2731887/supplementaryFiles` | **200** | 511599 | a real endpoint that answers — and the archive holds `gkp436f1` and `gkp436f2` only |
| `https://archive.org/wayback/available?url=nar.oxfordjournals.org/cgi/content/full/gkp436/DC1` | **200** | 87 | `archived_snapshots: {}` |
| `https://academic.oup.com/nar/article/37/15/5001/2409718` | **403** | 0 | the publisher refuses |
| `https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkp436` | **403** | 0 | the publisher refuses |
| `https://academic.oup.com/nar/article/37/15/5001/2409718#supplementary-data` | **403** | 0 | the publisher refuses |
| `http://nar.oxfordjournals.org/cgi/content/full/gkp436/DC1` | **200** | 1455 | a redirect stub, no content |
| `https://web.archive.org/web/2010/http://nar.oxfordjournals.org/cgi/content/full/gkp436/DC1` | **200** | 150665 | resolves to the Archive's only capture, which is a **301** |
| `https://web.archive.org/cdx/search/cdx?url=nar.oxfordjournals.org/cgi/content/full/gkp436/DC1&output=json` | **200** | 657 | three captures, **all `301`** |
| `https://web.archive.org/web/20160518225945/http://nar.oxfordjournals.org/content/37/15/5001` | **200** | 88425 | the archived article page, which **links** the supplementary page |
| `https://web.archive.org/web/20160518225945/http://nar.oxfordjournals.org/content/37/15/5001/suppl/DC1` | **404** | 0 | and the Archive never captured its content |
| `https://web.archive.org/web/2013/http://nar.oxfordjournals.org/content/37/15/5001/suppl/DC1` | **404** | 0 | nor at any other timestamp tried |
| `https://web.archive.org/cdx/search/cdx?url=nar.oxfordjournals.org/content/37/15/5001&matchType=prefix&limit=60&output=json` | **200** | 6454 | the prefix listing that supplied the two rows above |
| `http://web.archive.org/cdx/search/cdx?url=nar.oxfordjournals.org*&filter=urlkey:.*gkp436.*&limit=40&output=json` | **504** | 0 | the wildcard query times out; the narrow one above is what answered |

**Supplementary Note 3 of the caDNAno paper is recorded as NOT FOUND**, by these fourteen attempts.
One working URL refutes that, and it would generalise this task's answer from **one** cross-section to **seven**.
