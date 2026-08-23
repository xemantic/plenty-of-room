# `T-296` sources — does any honeycomb origami turn its raster with ZERO unpaired scaffold?

Fetched for [`T-296`](../../tasks/T-296-zero-loop-raster-turn.md) / `C-0193`.
**Two of the three decisive sources were already in `gpd/data/` and were not fetched at all**
(`CLAUDE.md`'s *check `gpd/data/` first*, paying a fourth time).

## Already in the repository — read, not fetched

| what | where | citation | read as |
|---|---|---|---|
| the `126 = 98 + 28` per-helix allotment and its scope over **all seven** cross-sections | `../T-151-sources/PMC2731887-fullTextXML.xml` | Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Research* **37**:5001 (2009) — the caDNAno paper | **read directly** |
| the unpaired loops' own **stated purpose**, and the seam alternative, and *90–97 % paired* | `../T-246-sources/PMC2821935.txt` | Ke, Douglas, Liu, Sharma, Cheng, Liu, Yan & Shih, *J. Am. Chem. Soc.* **131**:15903 (2009) | **read directly** |
| the field's **current** anti-stacking remedy, and that it is on the **staples** | `../T-151-sources/PMC11419732-fullTextXML.xml` | *DNA Origami Design: A How-To Tutorial* (2024) | **read directly** |
| the 28 nt allowance recurring on a second honeycomb object | `../T-246-sources/PMC3957201.txt` | Ke, Bellot, Voigt, Fradkov & Shih, *Chem. Sci.* **3**:2587 (2012) | **read directly** |

## Fetched here

| file | what it is | route | read as |
|---|---|---|---|
| `PMC2688462-douglas2009-articlepage.html`, `.txt` | Douglas, Dietz, Liedl, Högberg, Graf & Shih, *Nature* **459**:414 (2009) — the honeycomb block paper | `https://pmc.ncbi.nlm.nih.gov/articles/PMC2688462/`; EuropePMC's `fullTextXML` returned **zero bytes**, exactly as `CLAUDE.md` records | **read directly** (main text and Methods) |
| `douglas2009-SI.pdf` | its Supplementary Information, 13.4 MB, Adobe Illustrator strand diagrams | `https://www.nature.com/articles/nature08016` → the `MOESM288_ESM.pdf` link in the page. The two URL shapes `CLAUDE.md` records both failed (`Not Found`, `AccessDenied`); scraping the article page for the real `MOESM` number is what worked | **read directly, as an IMAGE** |
| `T-296-douglas-figS4-monolith-04.png` | page 4 of that SI — **Figure S4, the monolith design schematic**, whose cross-section inset is **10 rows of 6 helices** | `pdftoppm -r 110` | **read directly** |
| `T-296-douglas-figS4-left-rim.png`, `-right-rim.png` | the same page cropped at `r = 300` on the two rims, where the scaffold turns | `pdftoppm -r 300 -x … -W …` | **read directly** |
| `query.py`, `query.log`, `europepmc-queries.json` | the existence sweep: **30 queries in 9 named families**, 348 records, **186 unique** | EuropePMC REST, 8 s apart | titles and abstracts only |

**The SI PDF has no usable text layer** — `pdftotext` returns 58 MB of `!"#$` glyphs, which is the
sequence data under a custom Illustrator font encoding. `pdftoppm` plus reading the image is the
only route, exactly as `CLAUDE.md` records for a scanned textbook.

## What the figure says, read off its own axis

Figure S4's base-pair axis is ticked every 7 bp from 0 to 140. On every helix:

- the **scaffold** occupies **14 → 140**, i.e. **126** bases — the paper's own allotment;
- the **staples** occupy **28 → 126**, i.e. **98** base pairs — the paper's own paired count;
- the scaffold **turns at the rim with no topological loopout**, at base 14 and at base 140;
- so the **14 bases nearest each turn carry no staple**, and the two **duplex** ends a turn joins
  are **28** unpaired nucleotides apart.

`98 + 28 = 126`, and `60 × 126 = 7560` exactly — the scaffold the paper names.

## The negative existence result, and how to refute it

**No published honeycomb-lattice origami turns its raster between two DUPLEX ends.**
The 30 query strings are in `query.py` and the 186 unique records in `europepmc-queries.json`;
**7** of those records name the honeycomb lattice and **none** reports the motif.
One paper naming a honeycomb origami whose raster turns carry no unpaired scaffold refutes this,
and that is the whole of what it claims.
