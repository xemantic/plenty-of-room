# C-0179 — **A COUNT OVER A MOVING CORPUS BECOMES A DEBT ONLY IF ITS DENOMINATOR IS THE WHOLE OF THAT CORPUS'S DISCUSSION — AND THE DENOMINATOR `CH-0230` NAMED IS THE ONE THAT DOES NOT WORK.** The advisory `T-233 debt` line now prints **24 of 88 = 0.272727273** with the denominator named in the same output. Over the last **40** revisions of the two deliverables the count rose at **11** revisions; **7** of those are saturated (the documents then carried no pointer and no strike at all, so the ratio is exactly **1** on both sides), and of the remaining **4** the adopted ratio **fell at 3 and rose at 1** — while the reading `CH-0230` itself names, *over all occurrences of the **same** families*, **fell at 0 and rose at 4**. The one rise is earned: that pass added **2** occurrences, **both** unpointed assertions and **neither** a repair. Candidate 3 is **priced and declined** — counting lines compresses **1.7254902×** and the line count rose at **11 of 11**, so it does not change the sign, and **7 of 51** deliverable lines carry more than one class and **6 of 51** more than one discharge, which is exactly what `C-0176`'s data structure exists to keep apart. **23** mutations, **13** narrowing and **10** widening, **0** failing nothing — after **`F2` FIRED on the first run, on 1 of 18**

| | |
|---|---|
| **Task** | [`T-280`](../tasks/T-280-debt-line-as-a-ratio.md) |
| **Leaf** | none — a **process** claim, protecting the census that protects every honeycomb leaf |
| **Verification type** | **logical** — a count and two ratios over the in-scope corpus, taken out of `git` at a recorded ref; a **historical** series over the last 40 revisions of the two deliverables, which reproduces `CH-0230`'s own table at **25 / 10**; and a mutation measurement in **both** directions over **23** mutations and **28** new named self-tests |
| **Verdict** | **PASS on all four `T-280` predicates.** `F1` did **not** fire for the adopted denominator and **FIRED for the one `CH-0230` named**; `F2` **FIRED on the first run** and its repair is §5; `F3` and `F4` did not fire |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Not one physical number moves, not one verdict moves. What moves is what a checker prints beside a number it already printed |
| **Provenance** | `gpd/results/T-280-debt-line-as-a-ratio.json`, emitted by [`tools/T-280-emit-result.py`](../../tools/T-280-emit-result.py) (`--ref`, defaulting to `HEAD`, with the **resolved** sha recorded as `baselineRef` = `3e71284d5fe2bd05bf3b96ccb32cc20d6ba79ddd`), which **derives** its mutation numbers by running the mutation test rather than typing them; the ratio, its two named denominators and **28** new named tests in [`tools/T-234-census.py`](../../tools/T-234-census.py) (`--self-test`); the mutation measurement in [`tools/T-280-mutation-test.py`](../../tools/T-280-mutation-test.py) (new). `python3 tools/T-234-census.py --self-test` exits **0**; `tools/T-280-emit-result.py --self-test` exits **0**; `tools/T-280-mutation-test.py` exits **0** with **0** silent mutations. Document gates run at this state: `check-markdown-tables.py` **0**, `check-corpus-links.py` **0**, `check-corpus-identifiers.py` **0**, `check-challenge-index.py` **0**, `check-entry-points.py` **0**, `check-result-file-hygiene.py --prose/--departures/--saturated` **0**, `check-cold-start-note.py` **0**. `tools/T-234-census.py --check` exits **1** on **18** defects, every one of them **pre-existing at `HEAD`** and none of them reachable from anything this task touched — verified by running `HEAD`'s own copy of the census and its own classification against the same checkout, which reports the **same 18**. **No Gradle suite run**: this task compiles no Kotlin and a full-suite run was already in flight |
| **Conditions** | The corpus at the `baselineRef` the result file records. No physics is computed. Every quantity is a **count** of occurrences or of physical lines, or a dimensionless ratio of two counts, so the emitted floor is **zero** — `RESULT_ABSOLUTE_FLOOR` is a claim in the locked units and does not travel (`P-18`). Units elsewhere unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K |
| **Consumes** | [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (the split without which the adopted denominator carries nothing, and the two-reading rule), [`C-0144`](C-0144-honeycomb-correction-supersession.md) (the census and its five families), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) and [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the discharge this census is about), [`C-0127`](C-0127-format-string-repair.md) and [`C-0150`](C-0150-departure-spelling-set-and-the-wall-clock.md) (mutation testing in both directions), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a silent mutation is the finding, not a gap in the list), [`C-0131`](C-0131-departure-and-saturation-audits.md) (a saturated proportion is the resolution of nothing) |
| **Constrains** | every future reading of the advisory line, and any synthesis pass that spends hours on the strength of it |
| **Answers** | [`CH-0230`](../challenges/CH-0230-the-debt-line-grows-when-the-documents-are-corrected.md) candidate 2, and **prices** its candidate 3 |

---

## The claim, in one line

A ratio is a debt only if its denominator is **the whole of what the corpus says about the premise** —
including the sentences that say it **correctly** —
and `CH-0230`'s own statement of candidate 2 puts those sentences **outside** the denominator, which is why that reading rises at every pass it was meant to fall at.

---

## 1. The two cheap bounds, and the first one decided the design

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — measure every candidate denominator over history **before writing one** | one loop over `git show`, 40 revisions, seconds | over the 4 non-saturated passes at which the count rose, the **same-families** denominator fell at **0** and rose at **4**; the **all-occurrences** denominator fell at **3** and rose at **1** | the challenge's own denominator is refuted and the wider one adopted. Had this been inherited rather than verified, the task would have shipped a number that rises exactly where the count does |
| **2** — price candidate 3 with one `Counter` | seconds | **7 of 51** deliverable lines carry more than one **class** and **6 of 51** more than one **discharge**; the line count rose at **11 of 11** passes at which the occurrence count rose | candidate 3 declined, and **not on cost**: it does not change the sign, and it would collapse the very distinction `T-260` built |

The row that raised this task recorded that candidate 2 *"needs no new predicate, only the number published as a fraction"*.
That is **true of the predicate and false of the arithmetic**: no new predicate was needed, and the fraction the challenge specified is the wrong fraction.

---

## 2. Why the wider denominator works, and why it works only on top of `T-260`/`T-262`

`CH-0230`'s mechanism is exact and it is not repaired here:
**a correcting sentence has to name the withdrawn premise in order to withdraw it**,
so a document pass adds occurrences to a token census of that premise.

Read as a fraction, that mechanism has an arithmetic consequence the challenge did not draw.
A correcting sentence lands in the **numerator** and in the **denominator** at once,
and a ratio below one that gains equally top and bottom goes **up**.
So candidate 2 over *"all occurrences of the same families"* cannot work, and measured it does not.

What rescues it is `C-0176`'s split.
A correcting sentence written **properly** does not read as the withdrawn premise at all:
it reads as the **restored** reading, or as the half belonging to **another** census's discharge.
Those are families this census does **not** gate — denominator, never numerator.
So the wider denominator counts *the corpus discussing the premise* and the numerator counts *the corpus still asserting it*,
and the ratio falls exactly when a pass adds correct sentences.

**Candidate 2 is therefore not independent of `T-260`.** Before the split there was no family for a correct restatement to land in, and no denominator that could grow faster than its numerator.

---

## 3. The measurement

Over the last **40** revisions of `ANSWERS.md` and `DECISIONS-FOR-NDI.md`.
The numerator is the advisory line's own predicate **minus the class judgement**, which is what makes it computable from a historical text alone —
the same substitution `CH-0230` and `C-0176` §4 make, so the series is comparable with theirs.
The reproduction is exact: the old predicate ends at **25** and the new one at **10**, which is `CH-0230`'s table.

So the series' own last row reads **26 of 88 = 0.295454545** where the tool's live line reads **24 of 88 = 0.272727273**:
same denominator, and a numerator **two** larger, because the historical numerator cannot apply the class and two of the twenty-six are classified `RECORD` or `CORRECT`.
The denominator is identical, which is the point — the class judgement moves the numerator and nothing else.

| revision | pass | count | adopted ratio | verdict | `CH-0230`'s ratio | verdict |
|---|---|---|---|---|---|---|
| `89fd099` … `b410c32` | the first seven passes | 0 → 41 | 1 → 1 | **SATURATED** | 1 → 1 | **SATURATED** |
| `413659f` | iteration 36 | 17 → 20 | 0.326923077 → 0.3125 | **FELL** | 0.333333333 → 0.357142857 | ROSE |
| `49b1a01` | iteration 38 | 20 → 21 | 0.3125 → 0.287671233 | **FELL** | 0.357142857 → 0.368421053 | ROSE |
| `d077d55` | the eleventh synthesis | 21 → 23 | 0.287671233 → 0.306666667 | ROSE | 0.368421053 → 0.389830508 | ROSE |
| `cfbe0cc` | the twelfth synthesis | 23 → 26 | 0.306666667 → 0.295454545 | **FELL** | 0.389830508 → 0.412698413 | ROSE |

**The saturated head is a reading, not a gap.**
For the seven earliest passes the ratio is **exactly 1** under both denominators,
because the deliverables then carried no pointer and no strike at all —
every occurrence of every gated family was open.
A proportion pinned at its own boundary is the resolution of nothing (`C-0131`/`CH-0153`, met on a ratio of two censuses rather than on a Monte Carlo),
so those passes are reported as `SATURATED` and counted neither as a fall nor as a rise.

**The one rise is earned, and that is the point.**
At `d077d55` the pass added **2** occurrences and **both** of them went into the numerator:
two unpointed assertions of a withdrawn premise and no repair at all.
A metric that can rise **and** fall is what separates a debt from a counter;
a metric that only rises is a counter whatever it is called.

---

## 4. Candidate 3, priced

`CH-0230` offers counting **physical lines** rather than occurrences as the dearer alternative,
on the ground that *"a correcting sentence that names a premise three times is one edit"*.
It is a fair ground and it is not the number that decides it.

| what | corpus | the two deliverables |
|---|---|---|
| occurrences | 391 | 88 |
| distinct lines | 281 | 51 |
| compression | 1.39145907× | 1.7254902× |
| lines carrying more than one occurrence | 68 | 18 |
| lines carrying more than one **class** | **13** | **7** |
| lines carrying more than one **discharge** | **13** | **6** |

Two findings, and the first is decisive on its own.

**It does not change the sign.** The line count rose at **11 of the 11** passes at which the occurrence count rose.
Compressing 1.73 occurrences into one line does not make a monotone counter fall;
it makes a smaller monotone counter.

**And it would undo `T-260`.**
`C-0176`'s architecture is that a **family carries its own pointer set** and a **class is a reading held per occurrence**,
*"because the class is a READING and must be inspectable and falsifiable one occurrence at a time"*.
Seven of fifty-one deliverable lines carry occurrences of more than one class and six of more than one discharge —
one line of `ANSWERS.md` carries a `DISCHARGED` and a `MOVED` occurrence together, three carry a repaired premise beside the half a different claim discharged.
A line-keyed census has **one** key for those, so it cannot represent a partial discharge at all.

**Verdict: candidate 3 is declined, and not on cost.**

---

## 5. `F2` fired, and the mutation it fired on is `C-0176`'s own lesson one level down

The first mutation table had **18** rows and **one** of them failed nothing:
*"the report drops the count and prints the ratio alone."*

The rule was asserted.
The **test** was the defect: it asked whether the report contained a digit and the word *occurrence*,
and the word *occurrence* appears in the **denominator's own name**, which the mutation leaves untouched.
So a report that had dropped the count entirely still satisfied the test that existed to protect the count.

`C-0176` found nine such rows and attributed eight of them to the mutation table.
This one is the other kind: the table was right and the assertion was weak,
and only a mutation could tell the difference.
Rewritten to assert the report's **headline line itself**, and with five further mutations added to reach the tests no row had touched:

| | |
|---|---|
| mutations | **23** (13 `NARROW`, 10 `WIDEN`) |
| mutations failing **no** named test | **0** |
| named tests added by this task | **28** |
| of those, reached by at least one mutation | **26** |

Every substitution **replaces** a rule; none widens one to `original|mutant`, which is a no-op.
The mutation harness refuses a substitution whose anchor does not occur exactly once **and** refuses a mutation that leaves the source unchanged,
so an accidental no-op is a build failure rather than a silent pass.
The two unreached tests are aggregate assertions — *"it is in both denominators"*, *"the numerator never exceeds either denominator"* — reached by no plausible mutation of the shipped logic; that is reported and not gated.

**One further defect surfaced and was repaired the same way.**
The coverage table's own test-name extractor was a regular expression over the first string literal of each assertion,
so a name written as adjacent literals — which is how a long name is written under this project's line-break rule — was read as its first fragment,
and two tests a mutation had plainly killed were reported `UNREACHED`.
Python's parser concatenates adjacent literals into one constant, so `ast` recovers the whole name for nothing.
The measurement was wrong in the direction that flatters the tool.

---

## 6. Which reading of `C-0176`'s two, and why the question has an exact answer here

`C-0176` publishes every family split **twice** — with and without its own worked examples —
because writing that claim added **13** occurrences to the census it is about.

For **this** line the two readings **coincide exactly, and by construction**:
the advisory line counts only `ANSWERS.md` and `DECISIONS-FOR-NDI.md`,
and a claim's worked examples live under `gpd/claims/`.
That is asserted as a named test rather than argued, so it fails the day a claim file is added to the deliverable set —
and a mutation that adds one is in the table.

---

## 7. The four declared falsifiers

| | falsifier | outcome |
|---|---|---|
| **F1** | the ratio does not fall at any pass at which the count rose, so candidate 2 delivers nothing the count does not | **DID NOT FIRE for the adopted denominator** — 3 of the 4 non-saturated passes fell — and **FIRED for the denominator `CH-0230` named**: 0 fell, 4 rose. The challenge's own statement of its own remedy is refuted, and that is the claim's first half |
| **F2** | a mutation of any new rule fails no named test | **FIRED on the first run, on 1 of 18 rows** — and the row was right, the test was weak. Repaired, plus five mutations added and the coverage table's own name extractor fixed; **0 of 23** now |
| **F3** | `C-0176`'s two readings differ for this line, so the ratio must say which it is | **DID NOT FIRE.** They coincide exactly and by construction; asserted as a named test |
| **F4** | candidate 3 changes the sign of the growth where candidate 2 does not, so this task built the wrong remedy | **DID NOT FIRE.** The line count rose at 11 of the 11 passes at which the occurrence count rose |

---

## 8. Validity range

- **This is a claim about a checker's output, not about the honeycomb.** No physical number moves and no claim's verdict moves.
- **The sign of the COUNT is unchanged and `CH-0230`'s mechanism is not repaired.** It is priced. The count still grows on every synthesis pass; what the ratio adds is that the line can now **fall**, and does.
- **The ratio is not monotone in either direction, and that is the property being bought.** It rose at one of the four non-saturated passes, and that pass added two unpointed assertions and no repair.
- **Every reading here is dated by its ref** (`CH-0182`, now for the eighth consecutive iteration). The result file records the resolved sha and takes every historical reading out of `git show`, never out of the working tree; only candidate 3's per-class price is taken from the tree, because the class is only available through the classification the tree carries, and it says so in the field.
- **`gpd/results/T-260-*.json` and `T-262-*.json` are NOT re-emitted.** Their `mutationCoverage` record carries a count of the census's named self-tests, and this task adds 28 to that tool — so those two fields are larger now than they were then. Each file is a measurement at a recorded `baselineRef` and reproduces there; no predicate, verdict or reading in either moves. The `T-234` mutation test's own scoped measurement is untouched, because it scopes on names beginning `T-260 ` or `T-262 `.
- **The census's `--check` is still not wired into `tools/verify.sh`**, for `C-0176` §8's reason, and this task adds no gate. It exits **1** on **18** defects at this state, every one of them a claim or a queue row that entered the corpus after the classification was last regenerated — `CH-0182` again, and the regeneration is queued as `T-282` rather than done here, because regenerating mid-iteration would sweep in whatever is in flight.
- **The saturated head is seven of the eleven passes.** Any future reading that quotes *"the ratio fell at 3 of 4"* must carry that denominator with it, or it is quoting 3 of 11.
