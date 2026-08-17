# `T-148` — the measured per-staple incorporation map, and how it was read

The source of the only measured staple-incorporation field this programme has.
Recorded here so that the transcription is inspectable and its checks are repeatable,
per `CLAUDE.md`'s research practice:
*a delegated literature search is a summary, and a citation attached to it is not evidence that anything was read.*

## The source

**Strauss, Schueder, Haas, Nickels, Jungmann,
*"Quantifying absolute addressability in DNA origami with molecular resolution"*,
Nature Communications **9**:1600 (2018), `doi:10.1038/s41467-018-04031-z`, `PMC5913233`.**

| what | URL | outcome |
|---|---|---|
| main text | `https://www.ebi.ac.uk/europepmc/webservices/rest/PMC5913233/fullTextXML` | **200**, 84 KB, complete — **read directly** |
| main text | `https://www.nature.com/articles/s41467-018-04031-z` | **200**, complete — **read directly** |
| **Supplementary Information** | `https://media.springernature.com/original/springer-static/esm/art%3A10.1038%2Fs41467-018-04031-z/MediaObjects/41467_2018_4031_MOESM1_ESM.pdf` | **200**, 9.5 MB, 23 pp — **read directly** (Suppl. Figs 1, 2, 14, 15; Suppl. Tables 6, 7) |
| PMC article page | `https://pmc.ncbi.nlm.nih.gov/articles/PMC5913233/` | **200 but 21 KB of Google reCAPTCHA**, not the article. **The new PMC host is now gated as well** — `CLAUDE.md`'s note that it serves the real text no longer holds for this article |
| Rothemund 2006 main text | `https://www.dna.caltech.edu/Papers/DNAorigami-nature.pdf` | **200** — read directly |
| Rothemund 2006 SI | `https://www.nature.com/articles/nature04586` → `41586_2006_BFnature04586_MOESM1_ESM.pdf` | **200**, 82 pp — read directly |
| Rothemund 2006 SI | `https://www.dna.caltech.edu/Papers/DNAorigami-supp1.pdf` | **404** |

## What the paper states, verbatim

Abstract:

> *"We find that strand incorporation strongly correlates with the position in the structure,
> **ranging from a minimum of 48 % on the edges to a maximum of 95 % in the center.**"*

Results:

> *"The results indicate a consistently lower efficiency of detection on the outside of the structure
> (with a minimum of **41 %**) compared to inner areas where detection efficiencies reached **88 %**
> (the average detection efficiency for all strands was **77 %**).
> Taking the detection efficiency offset of **7 %** … this translates to
> absolute incorporation efficiencies of **48–95 % with an average of 84 %**."*

On the mechanism:

> *"staples at the edges and corners are missing neighboring helices
> and/or lack stacking interactions to neighboring strands."*

Methods: the scaffold is **p7249**, identical to M13mp18.

## The geometry — **read directly**

Rothemund 2006, Supplementary Note S3, the Fig. S19 caption:

> *"27 turns wide at 10.666 bases / turn -> 288 nt / 24 helices tall — Schematics for the rectangle Fig. 2b."*

So the object is **24 double helices × 288 bp**, 1.5-turn (16 bp) crossover spacing, one central seam.
Strauss's structure is that design re-drawn in caDNAno with its own staple set:
Suppl. Tables 6 and 7 carry helix indices **0–23** and base indices 31–272,
and Suppl. Fig. 2's caDNAno panel shows exactly 24 scaffold lines.
The **stapled** core is therefore **16 staple columns × 16 bp = 256 bp** long,
with ~16 unpaired scaffold bases hanging off each short edge —
the "scaffold loop" the paper probes separately at 90 % detection.

On this project's **measured** interhelical distance (2.69 nm, Fischer et al. 2016)
the stapled core is **87.04 nm along the helices × 64.56 nm across them**;
on Rothemund's own inferred 1 nm gap (3.0 nm per helix) the short axis is 72 nm instead.
**`T-148` fits its boundary-layer conventions on the STAPLED CORE**,
`16 × 16 bp = 256 bp` along by 24 helices across — the domain the 168 measured staples occupy,
and therefore the domain their mean is a mean over.
Both counts are read directly; the only unmeasured number is the interhelical distance,
which `T-148` brackets at **2.69 / 2.73 / 3.00 nm**.

## The map — **read directly from Supplementary Fig. 14**, transcribed and validated

Suppl. Fig. 14 is Fig. 4c with the numbers printed.
**Detection efficiency in per cent**, 16 columns (A–P, along the helices, one 16 bp staple domain each)
× 12 rows (1–12, across the helices, one **pair** of helices each);
`--` marks the 24 unprobed positions,
which carry the biotinylated surface-attachment strands and their neighbours
(columns C, G, K, O at rows 2–4 and 9–11 — all interior).

| row | A | B | C | D | E | F | G | H | I | J | K | L | M | N | O | P |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **12** | 62.9 | 66.1 | 66.7 | 62.9 | 68.3 | 68.3 | 61.3 | 66.7 | 73.9 | 75.3 | 75.3 | 71.5 | 76.3 | 73.1 | 69.4 | 80.1 |
| **11** | 65.1 | 76.3 | -- | 81.2 | 79.0 | 79.0 | -- | 81.5 | 81.2 | 80.6 | -- | 84.4 | 80.6 | 74.2 | -- | 80.1 |
| **10** | 76.3 | 70.4 | -- | 73.7 | 77.4 | 72.0 | -- | 76.1 | 75.9 | 84.9 | -- | 80.1 | 75.3 | 68.8 | -- | 71.0 |
| **9** | 82.3 | 77.4 | -- | 80.6 | 80.1 | 75.3 | -- | 77.5 | 79.6 | 76.3 | -- | 84.9 | 83.3 | 71.0 | -- | 76.3 |
| **8** | 84.4 | 78.0 | 74.2 | 87.1 | 85.5 | 82.3 | 84.4 | 86.0 | 79.8 | 78.5 | 78.5 | 80.6 | 88.2 | 75.8 | 75.3 | 71.5 |
| **7** | 83.9 | 82.8 | 71.0 | 82.3 | 84.9 | 84.4 | 83.9 | 85.0 | 82.3 | 87.1 | 79.0 | 87.6 | 77.4 | 77.4 | 77.4 | 74.7 |
| **6** | 52.7 | 79.6 | 80.6 | 84.9 | 82.8 | 82.8 | 83.9 | 86.6 | 80.7 | 81.2 | 80.6 | 80.6 | 85.5 | 85.5 | 76.9 | 79.0 |
| **5** | 78.0 | 76.3 | 72.0 | 81.7 | 81.2 | 76.3 | 79.0 | 78.5 | 85.2 | 81.2 | 83.3 | 83.9 | 87.6 | 76.3 | 74.2 | 77.4 |
| **4** | 68.8 | 69.9 | -- | 80.1 | 81.7 | 76.3 | -- | 79.3 | 80.7 | 82.8 | -- | 78.5 | 78.5 | 79.0 | -- | 75.3 |
| **3** | 65.1 | 72.0 | -- | 75.3 | 78.5 | 70.4 | -- | 78.0 | 70.4 | 76.3 | -- | 81.7 | 78.5 | 78.5 | -- | 70.4 |
| **2** | 71.0 | 76.3 | -- | 73.1 | 80.6 | 75.3 | -- | 75.3 | 75.3 | 72.0 | -- | 73.1 | 81.2 | 72.6 | -- | 75.8 |
| **1** | 53.2 | 70.4 | 59.7 | 80.1 | 76.9 | 67.2 | 66.7 | 74.7 | 66.9 | 76.9 | 63.4 | 72.0 | 71.5 | 64.5 | 64.5 | 40.9 |

The array is carried in code as `coupling.StrausIncorporationMap.DETECTION_PER_CENT`,
and **incorporation is detection plus the paper's own 7 percentage-point offset**.

### Three checks, each an executable test

1. **The paper's own panel arithmetic.** Suppl. Fig. 14 prints detection (panel a) and incorporation (panel b);
   panel b = panel a + 7 on all 168 cells.
2. **The paper's own printed summary.** This array gives min **40.9**, max **88.2**, mean **76.50**,
   against the printed 41 / 88 / 77 — and, plus the offset, 47.9 / 95.2 / 83.5 against the printed 48 / 95 / 84.
3. **The denominator.** Methods give `n = 186` imaged structures, so every cell must be `k/186`.
   The mean distance to the nearest whole count is **0.067** at `n = 186`,
   against **0.233** at 185, **0.239** at 187 and **0.101** at 372 —
   a 3.5× discrimination, which identifies the denominator and certifies that no digit was mis-read.

## What the map says that the three summary numbers do not

| statistic | value |
|---|---|
| probed cells | **168** of 192 |
| mean detection / incorporation | 76.50 % / **83.50 %** |
| **perimeter** (52 cells) mean detection / incorporation | 70.51 % / **77.51 %** |
| **interior** (116 cells) mean detection / incorporation | 79.18 % / **86.18 %** |
| perimeter s.d. / interior s.d. | 8.16 / 4.50 = **1.81×** |
| the single lowest cell | **P1**, the bottom-right **corner**, 40.9 % / **47.9 %** |
| the four corners | 53.2, 62.9, 40.9, **80.1** |
| perimeter cells above the interior mean | **6** of 52 |
| binomial standard error per cell at `n = 186` | 2.4–3.7, mean **3.05** percentage points |

**`CH-0084`'s *"48 % at the edges"* is one corner of 168, not an edge value.**
The perimeter *mean* is 77.5 % incorporation, 29.6 percentage points above it,
and one corner (P12, 80.1 %) is above the *interior* mean.
Any field that puts a whole rim at 0.48 is harsher than the measurement,
which is why `T-148` carries the `MEASURED_DEPTH` convention —
the map's own depth means, with no fit — beside the ones fitted to the three summary numbers.

**The boundary layer is anisotropic**, and both directions are wider than one lattice cell:

| depth class from the nearer **along-helix** edge | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|---|
| depth [nm] | 2.72 | 8.16 | 13.60 | 19.04 | 24.48 | 29.92 | 35.36 | 40.80 |
| mean detection [%] | 71.51 | 74.07 | 73.38 | 79.45 | 79.83 | 76.09 | 78.46 | 78.21 |

| depth class from the nearer **across-helix** edge | 0 | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|---|
| depth [nm] | 2.69 | 8.07 | 13.45 | 18.83 | 24.21 | 29.59 |
| mean detection [%] | 68.36 | 76.87 | 74.88 | 78.15 | 80.07 | 80.78 |

The across-helix profile is a single large step (+8.5 points) at the outermost pair of helices
and then a slow rise; the along-helix one is a shallower, wider depression over two to three columns.
Neither is monotone, because the per-cell binomial noise is ~3 points.

**A caution the paper's own design supplies**: the short edges of this rectangle are **not** blunt-ended duplexes —
16 unpaired scaffold bases hang off each helix end —
so the column-A/column-P depression is not the *"missing neighbouring helix"* mechanism the paper's sentence names.
That mechanism applies to rows 1 and 12 only, and the data do separate the two edge types
(rows 66.8 / 69.9 % against columns 70.3 / 72.7 %).

## Corroboration and context

- **Rothemund 2006, read directly**: *"94 % of '1' pixels (of 1,080 observed) were visualized."*
- **Strauss Suppl. Fig. 15, read directly** — a CanDo model of the same rectangle,
  RMS thermal fluctuation 1.3–3.4 nm, *"little thermal fluctuations in the center …
  Fluctuations with the highest magnitude can be found at the corners and edges"* —
  qualitatively the same anisotropic, one-to-three-cell boundary layer.
- **Strand census, read directly**: Suppl. Table 6 lists **176** core staples and Suppl. Table 7 **8** biotinylated ones,
  184 in all; 168 of the 192 grid positions are probed.
  A tile-level budget written on "168 staples" is missing 16 strands, all interior.

## One correction to `CLAUDE.md`, checked rather than accepted

A first reading of this material reported that `CLAUDE.md`'s `σ_rel = √(f/(1−f))` is inverted.
**It is not.** For a two-valued population — stiffness `k` with probability `1 − f`, zero with probability `f` —
the mean is `k(1 − f)`, the variance `k²f(1 − f)`, and the relative standard deviation `√(f/(1 − f))`,
which at `f = 0.16` is **0.4364** and at `f = 0.52` is **1.0408** — the two numbers `CH-0084` publishes.
The inverted form `√((1 − f)/f)` gives 2.29 at `f = 0.16` and is not what is quoted anywhere.
Asserted as a gate-1 test in `src/test/kotlin/coupling/StapleDropoutTest.kt`.
