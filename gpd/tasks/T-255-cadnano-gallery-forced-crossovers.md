# T-255 — Does any cadnano.org gallery design carry a forced crossover, and with what yield?

**Leaf** `A8.2`.
**Raised by** [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) §6 (`T-246`).
**Re-priced by** [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) §4 (`T-296`, filed the same iteration).

---

## 1. Formulate

`C-0152` prices a **forced** honeycomb scaffold crossover — one placed at a residue caDNAno's
own `7k ± 5` condition does not allow — at a ceiling of `0.350894669 k_BT`, sub-thermal,
and records in the same claim that *an elastic energy is not a folding yield*.
`T-246`'s literature sweep (68 queries, 7 families) found **nothing in the literature isolates a
single junction**, and the one source that *defines* the operation — Douglas et al., NAR 2009 —
declines to predict its consequences and points at its own gallery for the empirical record:

> *"for examples of designs that folded successfully, although with varying yields,
> see the gallery section at http://cadnano.org/"*

`T-246` read that page live and at its 2012 Wayback capture and found three citations and three
`.zip` files with **no yields on the page**. **It did not open the `.zip` files.**
This task opens them.

### The question, decomposed

The row asks a **conjunction**, and it must be scored as one:

1. **Obtainable** — can a gallery archive be retrieved at all, in 2026?
2. **Machine-readable** — does it contain design files a parser can read, rather than pictures?
3. **Non-empty category** — does any design in it carry a crossover off caDNAno's own allowed
   residue set?
4. **Attributable yield** — is a folding yield published for **that design**, as opposed to for
   the paper's shape family?

### Numeric target and acceptance predicate

**Acceptance (as written in the queue row):** *either* a gallery design whose file contains a
crossover off the default `7k ± 5` lattice **with a published yield attached**, *or* a recorded
statement that the gallery carries no yields at all.

**Predicate `P1`.** Every retrieval attempt is recorded with its **URL and HTTP status**, and the
retrieval verdict is stated as obtained / not obtained per archive.

**Predicate `P2`.** For every design file obtained, the count of scaffold crossovers, staple
crossovers and raster turns is emitted **separately** — `CLAUDE.md`: *a bare "crossovers" count is
28.6 % ambiguous, filter by strand role*.

**Predicate `P3`.** For every honeycomb design obtained, each crossover is reduced by
`C-0148`'s closure statistic `(level − 7·class) mod 21` and classified **allowed / forced**
against caDNAno's own `±5 bp` condition, using the tool's **own** published rule and not this
repository's derived one.

**Predicate `P4`.** The forced-crossover census is reported **with `C-0193` §4's exemption
applied and stated**: a turn flanked by unpaired scaffold carries no azimuth, so the residue
condition cannot bind it and such a turn is **outside the category**, not a member of it that
happens to pass.

**Predicate `P5`.** Any yield quoted is flagged **read directly / abstract only / not found**, and
is quoted with the **design** it belongs to, or the absence of that attribution is stated.

**Predicate `P6`.** If the category is empty, the **marginal rate** of each conjunct is measured
and multiplied, per `CLAUDE.md`'s rule that *a negative existence result needs its expected yield*.

**Verification type** — **in-silico** (parsing foreign design files with this repository's own
lattice arithmetic) **+ literature** (the gallery, its archives, and the three papers it cites,
every number flagged for how it was read).

**Locked units** — counts are dimensionless integers; a residue is a base-pair index (integer);
an azimuth is degrees, at caDNAno's `240/7 = 34.2857143°` per base pair; a length is nm at the
0.34 nm rise; a yield is a **percentage of a stated denominator, and the denominator travels with
it**. No energy is computed here — `C-0152` owns that axis and this task does not re-derive it.

**Geometry and sign conventions** — honeycomb, 21 bp crossover period, 3 azimuth classes at 7 bp,
scaffold offset `±5 bp` (caDNAno's own integer approximation to a `5.25 bp` half turn,
`CLAUDE.md`). Bond class from the helix pair; `level` is the base-pair index the crossover sits at
in the design file's own coordinate. **A datum shift shifts every crossover alike**, so the
closure statistic is convention-free and no datum needs to be chosen.

---

## 2. Plan — the cheap bound first

### The cheap bound, and it runs before a single byte is fetched

**`C-0193` §4 may have emptied the category before this task was taken.**
An unpaired scaffold base has no azimuth, so caDNAno's `±5 bp` residue condition **cannot bind a
turn flanked by unpaired scaffold**. The seven built honeycomb blocks — which are exactly what two
of the three gallery entries are — allot **126 nt per helix, 98 paired and 28 unpaired** as front
and rear loops. Every raster **turn** in those designs is therefore outside the residue condition
by construction, and a census that counted them as "allowed" or "forced" would be answering a
question the lattice does not ask.

So the cheap bound is one sentence and it **narrows the target before any fetch**:
a forced crossover in a gallery design must be an **interior** crossover — a staple crossover, or
a scaffold crossover that is not a rim turn — and not a raster turn.

**Second cheap bound: the conjunction's own arithmetic.**
`T-246` already established that the **page** carries no yields, so conjunct 4 cannot be
discharged from the gallery and must come from the three cited papers. Two of the three
(Dietz 2009 *Science*; Douglas 2009 *Nature*) are gel-yield papers and are **already in
`gpd/data/`**; the third is the caDNAno paper itself, also already fetched. **So conjunct 4 costs
nothing new** and the whole expense of this task is conjuncts 1–3.

**Third cheap bound: check `gpd/data/` before fetching.** `CLAUDE.md` records that this has paid
four times. `T-246` retained `cadnano-org-gallery.html`, `cadnano-org-index.html`,
`cadnano-org-legacy.html`, `wayback-2012-gallery.html` and the Wayback CDX listing; `T-296`
retained the Douglas 2009 paper, its SI and the monolith strand diagram. **The gallery's three
archive URLs are already on disk**, in the 2012 capture, in resolved form
(`209.20.81.220/data/2009{Science,NAR,Nature}.zip`) — the live page hides them behind `bit.ly`
shorteners. Nothing needs to be searched for.

### What would make the whole search pointless — stated before it is run

- **The archives are gone at every route.** Then the answer is a retrieval record, not a census,
  and `C-0152`'s negative existence result stands **unfalsified but also untested** from the
  field's own record. This is a finding and it must be reported as one, with statuses.
- **The archives contain pictures, not designs.** Same outcome.
- **The designs are all default-lattice.** Then the category is empty, and the result is a
  negative that is only worth publishing **with its marginal rate** — `CLAUDE.md`: a null over a
  conjunction whose expected yield is below one carries no information at all.
- **The category is empty because `C-0193` §4 exempts every turn.** Then the answer is
  *the question does not apply to this lattice as posed*, which is a **complete** answer and the
  most valuable of the four, because it re-scopes `C-0152` §6's own open item.

### Method, and its justification against cost

1. **Retrieve.** Try, in order and recording every status: the live `bit.ly` targets; the live
   cadnano.org paths; the 2012 Wayback captures of the three `.zip` URLs; the Wayback CDX index
   for any other capture of the same paths. Nothing here needs a browser.
2. **Unpack and inventory.** List every member of every archive with its size and type. A caDNAno
   legacy design is a `.json` document with a documented shape (`vstrands`, each carrying `scaf`
   and `stap` arrays of `[prevHelix, prevBase, nextHelix, nextBase]` quadruples, plus `row`, `col`,
   `num`, `skip`, `loop`). **That format is parseable in a hundred lines of Python and needs no
   Kotlin at all** — which is the cost argument: writing a `.json` importer into the Kotlin
   scadnano reader would cost a compile cycle per iteration under four-agent contention, where a
   Python parser with its own self-tests costs seconds. The **arithmetic** consumed
   (`(level − 7·class) mod 21`, `±5`, 21 bp, 3 classes, 7 bp) is read out of the committed
   Kotlin source rather than transcribed.
3. **Classify.** Per design: helix count, lattice (honeycomb / square, from the file's own
   `row`/`col` and the presence of a `helix` grid statement), scaffold crossings, staple
   crossings, raster turns, unpaired scaffold at each helix end (from `loop`/`skip` and from the
   scaffold's occupied base range), and — for honeycomb — the residue census.
4. **Score the conjunction** and report the marginal rate of each conjunct whether or not the
   category turns out to be non-empty.

### TDD

The parser's arithmetic is asserted **before** it is pointed at a foreign file:
against this repository's own committed honeycomb design (`C-0151`'s `102 / 109` block, which is
known to close with **zero** forced crossovers), against `C-0148`'s committed residue table, and
against hand-built minimal `vstrands` fixtures whose expected census is written down first.
A parser that reports a forced crossover on the design this repository has proved closes is
**wrong**, and that is the first test.

---

## 3. Falsifiers, declared before the run

| | statement | fires if |
|---|---|---|
| **`F1`** | the gallery archives are **obtainable** in 2026 | no route returns an archive |
| **`F2`** | the archives contain **machine-readable design files** | they contain only images, PDFs or binaries |
| **`F3`** | **at least one gallery design carries a crossover off caDNAno's own allowed residue set** | it does — this is written the favourable way round, so its **not** firing is `C-0152` §6's answer |
| **`F4`** | a **yield is attributable to a specific gallery design file** | it is |
| **`F5`** | the honeycomb gallery designs' raster turns **are** bound by the `±5 bp` residue condition | they are — which would contradict `C-0193` §4 and raise a challenge |
| **`F6`** | the parser reproduces this repository's own committed honeycomb design's census | it does not — the whole run is then void |
| **`F7`** | the marginal rate of the conjunction is **above one**, so a null would carry information | it is below one — the null must then be reported as uninformative about the world and informative only about the gallery |

---

## 4. Conditions

Honeycomb 21 bp crossover period, 3 azimuth classes at 7 bp, caDNAno scaffold offset `±5 bp`,
10.5 bp/turn, `240/7 = 34.2857143°` per base pair, rise 0.34 nm/bp. No buffer, no temperature and
no environment coordinate enters — this is a lattice-and-record question, so the result file's
`regime` is `[]`. Date of every retrieval is recorded, because the subject is the web.
