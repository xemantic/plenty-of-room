# T-302 — settle `CH-0251` by counting one column: does the 2009 SI order `5 880` staple nucleotides for the `10 × 6` block, or `7 560`?

**Leaf** `A8.2`.
**Raised by** [`CH-0251`](../challenges/CH-0251-the-deposited-block-has-no-loops.md) / [`C-0199`](../claims/C-0199-the-gallery-opened.md) (`T-255`), which names this experiment and deliberately does not run it.

---

## 1. Formulate

Two primary artifacts of the same authors disagree about the same object.

The caDNAno paper's Methods sentence, read directly and quoted by [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) §3:

> *"Each helix was allotted **126** bases of scaffold. Of those 126 bases, **98** were paired with
> complementary staples, and the remaining **28** bases were divided into front and rear unpaired
> loop fragments at the ends of each helix."*

The seven **deposited design files** for those same seven cross-sections, parsed by `T-255`, say `126` allotted and **`0`** unpaired: on `NAR09/ii_10x6` (bit-identical in every `vstrand` to `Nature09/01-monolith`) the staples occupy every one of the 126 scaffold bases of every one of the 60 helices.

**One column separates them.** A design leaving 28 unpaired scaffold bases per helix orders
`60 × 98 = 5 880` staple nucleotides for the `10 × 6` block; the deposited file draws `60 × 126 = 7 560`.

### The numeric target

The **sum of the `Length` column** of the `10 × 6` block's staple sequence table in the 2009 supplementary information, and its **row count**.

### Acceptance predicate — falsifiable, and binary before the run

Let `S` be that sum and `R` that row count, read directly from the supplementary table, and let `F` be the deposited file's own staple census.

1. **The table must be identifiable as this design's.** Every row's `Start` and `End` — caDNAno `helix[base]` coordinates — must name a strand that exists in the deposited file, at the same length. Fewer than **100 %** of rows matching means the table is about a different object and **neither** reading is licensed; the task then reports that and stops.
2. Given (1), the verdict is decided by `S`:
   - `S = 5 880` and `R < F.strands` ⟹ **the Methods sentence describes the folded object and the design file is a drawing.** `CH-0251` is **REFUTED** on its central point; `C-0193` §3/§4 stand.
   - `S = 7 560` and `R = F.strands` ⟹ **the design file is the order.** `CH-0251` is **UPHELD**; `C-0193` §3/§4 are withdrawn and `C-0175` §9, `C-0180` §4 and `C-0190` stand unconditionally.
   - any other `S` ⟹ neither, reported as such.
3. The residue `F.nt − S` must be **located**, not merely computed: the strands the table omits must be identified in the file and their positions stated.
4. Under the ordered staple set, the **per-helix paired base count** must be emitted. The sentence predicts `98` on each of the 60 helices; anything else is a departure from the sentence and is reported at that granularity.
5. Every URL tried is recorded with its HTTP status, and the date. A source not obtained is recorded as **not found**, with what was tried.

### Verification type

**Literature** (a supplementary table, read directly) **+ in-silico** (an exact integer match of that table against the deposited design files, which this repository already parses).

### Locked units

Counts are dimensionless. A staple length is in **nucleotides**; a scaffold allotment in **nucleotides**; a duplex window in **base pairs**. Where a contour is quoted it is `0.65–0.70 nm/nt` for **ssDNA** (`C-0147`'s inextensible bracket) and `0.34 nm/bp` for **duplex** rise — never the duplex rise on a single-stranded segment, which is the unit error `CLAUDE.md` records against `28 nt = 9.52 nm`.

### Geometry and sign conventions

caDNAno legacy coordinates: `helix[base]`, base indices increasing along the drawn axis, `-1` for an absent neighbour. A **strand** is a maximal path through the `stap` linked list; its `Start` is the 5′ terminus (`prev == -1`) and its `End` the 3′ one. A **crossing** is an adjacency whose two ends sit on different helices. A **duplex window** on a helix is the closed interval of base indices covered by the **ordered** staple set.

---

## 2. Plan

### The cheap bound, and it runs first

Before any PDF is rendered: **census the deposited file.** Its staple strand count, its total staple nucleotides, its length histogram and the per-helix span of its staple coverage are four integers that cost milliseconds and that fix what a table can possibly agree with. If the file's own totals do not admit either candidate the question is malformed and no reading of a table can repair it.

The second cheap bound is `CLAUDE.md`'s standing one: **check `gpd/data/` before fetching anything.** `T-296` already retained `douglas2009-SI.pdf` (13.4 MB) and `T-255` the three gallery archives. Nothing about this question requires a new fetch of the decisive artifact.

### The route, and why it is not the one the queue row predicted

The queue row and `CH-0251` both expect a **rendering** problem: `T-296` recorded that the SI's text layer is *"garbled by font subsetting past the front matter"* and `CLAUDE.md` prescribes `pdftoppm` plus reading the image. That is true of the **figure** pages, whose embedded TrueType subsets carry no `/ToUnicode` and no `post` table. It is **not** true of the table pages, and the check that settles it costs one command: `pdftotext -bbox -f 12 -l 26`. Run it before committing to a 26-page raster read.

Where the text layer is clean the table is read by **geometry**, not by regular expression: cluster the extracted words by `xMin` against the header row's own column positions (`Start`, `End`, the title, `Length`, `Color`), group by `yMin`, and take the sequence from the line below. Nothing about that depends on a font.

### Three independent cross-checks, all cheap, and the corpus insists on them

1. **`Length` against the sequence itself.** Every row states a length and carries a string; `len(sequence) == Length` at every row or the extraction is wrong.
2. **The row count and the endpoint pairs against the design file.** Predicate (1) above. This is exact integer matching and it certifies the identification.
3. **The `Color` column against the design file's own stored `stap_colors`.** A caDNAno file stores a colour per strand; if the table's hex matches the file's stored integer at every row, the table was generated from that exact file and not from a variant.

### Why this and not something else

The alternative reading — render 26 pages at 300 dpi and transcribe ~2 000 rows by eye — is what `CH-0251` implicitly costs the task at, and it is both slower and *less* checkable: a transcription cannot be matched to the file endpoint by endpoint. The geometric read is exact, and its own correctness is falsifiable by cross-check 1 at every row.

The second source, the caDNAno paper's own Supplementary Note 3 (*"detailed schematics and staple lists"*, for all seven cross-sections), is worth one round of fetching because it would generalise the answer from one block to seven. It is **not** worth a long hunt: the `10 × 6` is the block `CH-0251` is about and the block this repository models.

### What result would falsify this approach

- **If fewer than 100 % of the table's rows resolve to a strand of the deposited file** at the same length, the table is not about that file and the whole method is void — the read would then have to be redone against whatever design the table *is* about, and the challenge would remain open.
- **If the sum is neither `5 880` nor `7 560`**, the one-column discriminator the challenge names does not exist and the question has to be reformulated.
- **If the text layer of the table pages is garbled after all**, the geometric read returns nothing and the task falls back to the raster route at a cost the plan has to restate before spending it.
- **If a colour disagreement appears at any row**, the table and the file are two generations of one design and the residue cannot be attributed to an editorial omission.

### What this task will NOT settle

- **Whether the object folded as ordered.** A staple order is a purchase, not a micrograph. What it settles is what was *bought*.
- **The other six cross-sections**, unless the caDNAno paper's own supplementary note is obtained. A rule inferred from one block's colour or geometry and applied to six untabulated ones is a proxy, and a proxy must be validated on every design where a table exists before it may be carried to one where none does.
