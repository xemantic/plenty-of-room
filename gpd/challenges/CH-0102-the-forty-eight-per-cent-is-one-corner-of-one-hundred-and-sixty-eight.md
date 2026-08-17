# CH-0102 — `CH-0084`'s **48 %** is the single worst cell of 168, and it is a **corner**: read off the per-staple map instead of the abstract, the perimeter *mean* incorporation is **77.5 %** and one corner is **87.1 %**, *above* the interior mean. The `104 %` edge scatter describes one staple. **And the correction does not save anything** — the perimeter mean is still **1.56×** `C-0060`'s threshold, the *interior* mean is **1.16×** it, and **only 30 of the 168 measured positions** are incorporated well enough for their own scatter to sit inside it

| | |
|---|---|
| **Raised by** | [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md) (`T-148`) |
| **Against** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s `T-45` deliverable and the [`CH-0084`](CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md) it raises — specifically the composition table's row *"**Strauss, EDGE sites** \| **0.48** \| **0.52** \| **104.1 %** \| **6.12×** \| **3.01×**"*, and the `TASKS.md` and `CLAUDE.md` phrasing *"48 % at the edges to 95 % in the centre"* read as an edge value |
| **Grounds** | **the per-staple map was never read; only the abstract was.** Strauss et al.'s Supplementary Fig. 14 prints all 168 values. Their minimum, 47.9 % incorporation, is **one cell** — `P1`, the bottom-right **corner**. The perimeter's 52 cells average **77.5 %**, the interior's 116 average **86.2 %**, six perimeter cells are *above* the interior mean, and the four corners run 60.2, 69.9, 47.9 and **87.1 %**. An "edge value" of 48 % is 29.6 percentage points below what the edges measure |
| **Severity** | **an arithmetic correction to one published row, running in the FAVOURABLE direction, that changes no verdict.** `CH-0084`'s mean row (0.84 → 43.6 %) is untouched and is reproduced here at 0.835 → **44.5 %** from the map itself. What falls is only the *edge* row. And the correction is inert: at the corrected perimeter mean the scatter is **53.9 %**, still **1.56×** `C-0060`'s 34.6 % threshold — and the corrected reading is the one `C-0087` grades the flatness on |

---

## What is claimed upstream

`C-0072` (`T-134`, iteration 14) answered `T-45` from the abstract of Strauss et al. (2018) and
raised `CH-0084` on it. `CH-0084`'s composition table reads:

| reading | efficiency | dropout `f` | implied `σ_rel` | vs `C-0026`'s 17 % | vs `C-0060`'s 34.6 % |
|---|---|---|---|---|---|
| **Strauss mean, all 168 staples** | **0.84** | **0.16** | **43.6 %** | **2.57×** | **1.26×** |
| **Strauss, EDGE sites** | **0.48** | **0.52** | **104.1 %** | **6.12×** | **3.01×** |
| Strauss, centre sites | 0.95 | 0.05 | 22.9 % | 1.35× | 0.66× |

and its statement 3 rests on the edge row:

> *"**The position dependence runs the wrong way for `C-0058`.** Strauss finds incorporation
> **worst at the edges**. `C-0058`'s flat design puts **34 of its 45 stations on the rim** and gives
> them the **stiff** level."*

`CH-0084`'s own *"What would settle it"* names the missing piece:
*"Re-running `C-0060`'s flatness sweep under a Bernoulli dropout … with the dropout probability
**position-dependent as Strauss measures it**."* That is `T-148`, and doing it required reading the
map.

## What the map says

**Supplementary Fig. 14** of the same paper prints the detection efficiency of every one of the 168
probed staples; incorporation is that plus the paper's own 7 percentage-point offset. The
transcription, its three validation checks and the URLs are in
[`gpd/data/T-148-strauss-incorporation-map.md`](../data/T-148-strauss-incorporation-map.md);
the array is `coupling.StrausIncorporationMap`, and seven gate-named tests assert its statistics
against the paper's own printed 41 / 88 / 77 and against the `k/186` quantisation its `n = 186`
imaged structures impose.

| | detection | **incorporation** | dropout `f` | **`σ_rel = √(f/(1−f))`** | vs `C-0060`'s 34.6 % |
|---|---|---|---|---|---|
| all 168 probed staples | 76.50 % | **83.50 %** | 0.1650 | **44.5 %** | **1.29×** |
| the **52 perimeter** cells | 70.51 % | **77.51 %** | 0.2249 | **53.9 %** | **1.56×** |
| the **116 interior** cells | 79.18 % | **86.18 %** | 0.1382 | **40.0 %** | **1.16×** |
| the single worst cell, **`P1`, a corner** | 40.9 % | **47.9 %** | 0.5210 | **104.3 %** | **3.01×** |
| the single best cell, `M8` | 88.2 % | **95.2 %** | 0.0480 | **22.5 %** | 0.65× |

**Three things fall out.**

1. **`CH-0084`'s edge row is a single-cell extreme.** 47.9 % is `P1`; the corner at the other end of the
   same short edge, `P12`, is **87.1 %** — above the interior mean — and `A12` is 69.9 % and
   `A1` 60.2 %. Corner-ness is
   not a predictor. Six of the 52 perimeter cells beat the interior mean outright, the perimeter's
   standard deviation is **1.81×** the interior's, and the **second**-lowest cell in the whole map
   (`A6`, 59.7 % incorporated) is a **mid-edge** staple, not a corner. Any field that puts a whole
   rim at 0.48 is far harsher than what was measured.
2. **The mean row survives and is re-derived.** `CH-0084`'s 0.84 → 43.6 % becomes 0.835 → **44.5 %**
   from the map itself — a 2 % move, and in the *adverse* direction. Nothing in `CH-0084`'s
   statement 1 or statement 2 changes.
3. **The correction buys nothing, and this is the part that matters.** `C-0060`'s 34.6 % threshold
   needs an incorporation of **89.31 %**; its mandate-held 31.6 % needs **90.92 %**; `C-0026`'s 17 %
   break-even needs **97.19 %**. Against the measured map:

   - **30 of 168** positions clear 89.31 %,
   - **20 of 168** clear 90.92 %,
   - **0 of 168** clear 97.19 % — the best cell measured anywhere is 95.2 %.

   So there is **no region** of the only origami whose staple incorporation has been mapped whose
   mean is inside `C-0060`'s threshold, and no *single staple* anywhere on it inside `C-0026`'s.
   The corrected edge reading is 1.56× the threshold where the erroneous one was 3.01×; both fail.

## What this does NOT touch

- **`C-0072`'s tolerance model, its four floors, its correlation structure and its `T-45` verdict
  are untouched.** The finding that a measurement exists, that it is the right order, and that it
  is past `C-0060`'s threshold all stand — and are strengthened, because they now rest on 168
  numbers instead of three.
- **`CH-0084`'s mean row and its mandate row are untouched.** The 16 % shortfall on `C-0017`'s
  mandate follows from the mean alone and is reproduced exactly.
- **`C-0060`'s and `C-0026`'s thresholds are not wrong.** They are the instruments, and this is them
  being read against a better number.
- **The direction of `CH-0084`'s statement 3 is untouched**: incorporation *is* worst at the
  perimeter, by 8.7 percentage points of mean, and `C-0058` *does* put 34 of 45 stations there.
  Only the amplitude of the edge deficit moves.

## The honest qualifications

1. **The map is read from a figure.** It is the paper's own Supplementary Fig. 14, transcribed and
   checked three ways — against the paper's printed min/max/mean, against its own `+7` panel
   offset, and against the `k/186` quantisation that identifies `n = 186` at a 3.5× discrimination
   over `n = 185` or `187`. It is not a table of numbers the authors typed.
2. **The per-cell noise is not small.** Each value is a proportion of 186 structures, so its
   binomial standard error is 2.4–3.7 percentage points. A single-cell statement is at the noise
   floor; the 52-against-116 perimeter/interior split (8.7 points) and the 40.9 corner are far
   above it. **The 104 % edge row is exactly the kind of single-cell statement that noise
   dominates**, which is a second reason not to build on it.
3. **It is one structure, one folding protocol, one staple set** — and a Rothemund rectangle rather
   than a Gen-1 tile. `C-0087` carries the transfer as a convention with its own bracket.
4. **The short edges of that rectangle are not blunt-ended duplexes**: 16 unpaired scaffold bases
   hang off each helix end. So the along-helix depression is *not* the *"missing neighbouring
   helix"* mechanism the paper's own sentence names — that applies to the two outermost helix rows
   only, and the data separate the two edge types.

## What would settle it

- **A per-site incorporation map on a coupling-bearing tile**, which is what `CH-0084` already
  asked for and what `T-45` has waited four iterations for. Everything here is still a *transfer*
  from a plain rectangle.
- **The authors' own numeric table**, if one exists outside the figure. The transcription's three
  checks make a digit error unlikely, but they are checks and not the source.
