# `T-182` sources — what prestrain does a row-end crossover carry?

Every load-bearing number below carries a **read status**, per `CLAUDE.md`'s research practice.
`gpd/data/` was checked **before** anything was fetched: Rothemund's Supplementary Notes and
**Snodin et al. (2019)** were already in the repository, fetched by `T-151`, and the second of
those turned out to carry the closest published measurement of the quantity this task is about.

## Already in the repository — checked first, and it paid

| file | source | used for | read status |
|---|---|---|---|
| `../T-151-sources/DNAorigami-supp1.linux.txt` | Rothemund, *Nature* **440**:297 (2006), Supplementary Notes S1 §4 and S2 | the glide symmetry, the *"on average, flat"* balancing, the edge failure of it, the by-hand edge domain-length tuning (*"The 6-base distance creates the least strain"*), the *"one or two scaffold bases"* remedy, *"How the strain is actually relieved is unknown"* | **READ DIRECTLY** |
| `../T-151-sources/PMC6379721-fullTextXML.xml` | Snodin, Schreck, Romano, Louis & Doye, *Nucleic Acids Res.* **47**:1585 (2019) | the corrugation definition and measurement; the pitch-mismatch mechanism; the 31/32 bp twist correction; **the exclusion of the outermost junctions** | **READ DIRECTLY** |
| `../T-161-sources/PMC2821935-Ke2009-fullTextXML.xml` | Ke et al., *JACS* **131**:15903 (2009) | the square lattice's 33.75°/bp and its four azimuths (via `C-0055`/`C-0015`) | **READ DIRECTLY** (by `C-0095`) |

## Fetched here

| file | source | used for | read status |
|---|---|---|---|
| `PMC6379721-snodin2019.pdf`, `snodin.txt`, `T-182-snodin-p7-07.png` | Snodin et al. (2019), **Figure 5** | the corrugation angle's magnitude: the thick black **average** peaks at ≈ +8° and ≈ −9° either side of a junction; individual junctions reach ≈ ±16° | **READ DIRECTLY — figure rendered at 170 dpi and digitised against its own printed axis**, which is `CLAUDE.md`'s stated route for a number that exists only in a figure |
| `PMC9127610-fullTextXML.xml` (the PDF was fetched, read and then deleted as redundant with the XML) | Ni, Fan, Zhou, Guo, Lee, Seeman, Kim & Yao, *iScience* **25**:104373 (2022) | cryo-EM of a 2D origami: *"2D origami uses ∼10.7 base pairs for every turn … while natural B-DNA has around 10.5 … The 0.2 base pair per turn difference leads to a dramatic change in geometry … owing to the large number of base pairs"*; *"the cross-tile edges, where there is maximum flexibility"* | **READ DIRECTLY** |
| `PMC3864285-yoo-aksimentiev2013.pdf`, `yoo.txt` | Yoo & Aksimentiev, *PNAS* **110**:20099 (2013) | all-atom MD of origami: the junction dihedral *"has a mean of −4°"* against a free Holliday junction's *"right-handedness and ∼60° dihedral angle"*; the square lattice *"developed a visible overall twist"*, `ω₃` mean **−1.3°** per array cell | **READ DIRECTLY**; the `ω₃` figure is **NOT load-bearing** here — the passage states the axis as *"array cell index"* and does not state the cell length for the SQ design |
| — | Bai, Martin, Scheres & Dietz, *PNAS* **109**:20012 (2012) | the cryo-EM object Snodin et al. compare against | **fetched and then DELETED, not read** — it is not load-bearing here, and this project does not keep a source it has not read. The fetch is recorded in `fetches.json` and `fetch.py` will retrieve it again |
| `crossref-baker2018.json` | Baker et al., *ACS Nano* **12**:5791 (2018), *"Dimensions and Global Twist of Single-Layer DNA Origami Measured by Small-Angle X-ray Scattering"* | the one direct **experimental** measurement of a single-layer origami's global twist | **NOT OBTAINED** — ACS returns `403` to `curl`; Crossref carries the title and no abstract. Named as the source that would settle the accumulation term |

## What is NOT found

**No source quantifies the residual dihedral angle at a row-end / edge crossover of a
single-layer origami.** Rothemund states in print that *"How the strain is actually relieved is
unknown"*; Snodin et al. define and measure exactly the right coordinate and write that they
*"exclude the outermost junctions on the tile, and the junctions next to the scaffold seam as well
as the seam itself"*. That is the whole of `T-182`'s negative result, and it is what makes the
oxDNA run warranted rather than redundant.

## Query log

- `query.py` — 20 EuropePMC queries, **156 unique records**, results in `europepmc-queries.json`,
  console log in `query.log`. The modalities are the ones `C-0104` did **not** search: oxDNA and
  coarse-grained, all-atom MD, cryo-EM, the global-twist measurement line, the corrugation and
  tube-formation literature, and the Holliday-junction geometry literature.
- `targeted.py` — 10 further title-level queries for the papers the keyword sweep did not surface,
  results in `targeted-queries.json`.
- `fetch.py` — every fetch attempt with its URL, HTTP status and byte count, in `fetches.json`.
  Two `403`s (ACS) and two `404`s (EuropePMC `fullTextXML` on articles whose PDFs fetch fine —
  `CLAUDE.md` records this) are recorded rather than silently dropped.

Both drivers are retained and re-runnable.
