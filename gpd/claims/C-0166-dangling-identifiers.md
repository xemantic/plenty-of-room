# C-0166 — a bare `CH-0133` in a sentence is **neither a filename nor a link**, so no gate in this tree could see it: `T-268` cited **two** numbers that were reserved in iteration 24 and never filed, and the corpus carried **thirteen further reservations released by nobody** — the gate that closes the class can come clean only because the legitimate mentions are exactly **two** kinds, and a release note written as a **RANGE** turns out not to release the numbers inside it

| | |
|---|---|
| **Task** | [`T-273`](../tasks/T-273-dangling-identifiers.md), raised by [`C-0162`](C-0162-round-outputs-never-inputs.md) (`T-268`), which found that task's own `P1` citing a challenge that does not exist |
| **Leaf** | none — a **process** claim protecting the corpus's cross-references |
| **Verification type** | **logical** — a census over the committed corpus, with the discriminator's false-positive rate measured against the corpus's own legitimate mentions and every context class held open by a named test in both directions |
| **Verdict** | **PASS on P1–P4.** [`tools/check-corpus-identifiers.py`](../../tools/check-corpus-identifiers.py) reads **0 dangling identifier(s) in 518 file(s); 342 claims and challenges exist**, and it is wired into [`tools/verify.sh`](../../tools/verify.sh) |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No study was re-run, no result file moved, and nothing here is evidence about the device |
| **Provenance** | [`tools/check-corpus-identifiers.py`](../../tools/check-corpus-identifiers.py) (15 self-tests); the repairs in [`gpd/tasks/T-268-emission-layer.md`](../tasks/T-268-emission-layer.md) and in three reservation blocks of [`TASKS.md`](../../TASKS.md) |
| **Conditions** | Nothing physical is computed. Units unchanged and untouched |
| **Consumes** | [`C-0162`](C-0162-round-outputs-never-inputs.md) (which found the defect and could only annotate it), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*; and the filename class one level up), [`C-0122`](C-0122-honeycomb-station-lattice.md) / `T-203` (the link checker this sits beside), [`C-0127`](C-0127-format-string-repair.md) (a drift checker's false positives cost more than its true ones), [`C-0129`](C-0129-result-file-hygiene.md) (print the residue ungated when the gate cannot come clean) |
| **Raises** | no challenge. `CH-0213` was **not reserved and not taken** — this claim contradicts no standing claim; what it found was an omission and a rule the queue states about itself and had not applied |

---

## The claim, in one line

`C-0083` gates a claim's **filename** and `tools/check-corpus-links.py` gates a relative **link**.
A bare `` `CH-0133` `` in prose is neither, and `T-268` cited two such numbers — `CH-0132` and `CH-0133`,
both **reserved by `T-201` in iteration 24 and never filed** — as though they were challenges, accumulating
**nine** occurrences including one in `CLAUDE.md`.
The class is now a gate reading **0**.

---

## 1. What the census found, before anything was repaired

| | |
|---|---|
| claim files | **159** |
| challenge files | **183** |
| challenge numbers in `[1, 211]` with **no file** | **28** |
| claim numbers in `[1, 164]` with **no file** | **5** |
| occurrences the naive gate would fire on | **21**, of which **most are correct sentences** |

A number with no file is *expected* in this corpus: `TASKS.md` reserves numbers before parallel agents start,
precisely so that two agents cannot collide, and an unused reservation is a gap by design.
So the census's first output is not a defect list — it is the reason a naive gate is unusable.

## 2. Why this can be a gate at all

The corpus's legitimate mentions of a non-existent identifier are exactly **two** kinds, and both are
statements **about** the absence rather than citations **of** the thing:

| | example |
|---|---|
| **RELEASED** | *"`CH-0208` was reserved for this claim and is **RELEASED UNUSED**"* |
| **ABSENT** | *"**there is no `CH-0133`** — the corpus's highest challenge is `CH-0209`"* |

The exemption is per **(document, identifier)** rather than per occurrence, and that is not a convenience:
`CLAUDE.md`'s entry about this very defect names `CH-0133` **three times**, and only the first sits beside the
words that declare it absent. Per-occurrence, the gate fires on the sentence that **records** the defect —
which is `T-249`'s failure met from the other side, and the reason its trailing guard had to be repaired twice.

**`JOURNAL.md` is deliberately out of scope.** It is a dated history; an entry naming a number that was later
renumbered is a correct record of what happened, and rewriting it is the one thing this repository forbids.

## 3. What was actually wrong

**Two live citations**, both in `T-268`, and neither number has ever existed:

- `P2` cited `CH-0132` for *"the others could not have obeyed it by any edit at their own call sites"*.
  The finding is [`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md),
  *"'the rule lives once' was true of one package"*.
- `P1` cited `CH-0133` for the integral-number rendering. That finding is
  [`C-0138`](C-0138-departure-rule-scope.md)'s, and `C-0162` had already struck it.

Both are struck rather than deleted, with the correct owner named.

**And thirteen reservations released by nobody.** `CH-0134`–`CH-0146` were reserved across iterations 24–26
and never filed, and the rule that governs them is stated *in the very block that reserves them*:

> A reservation is not a claim on a number; an unused one has to be released in writing, or the register
> credits a challenge to the claim that did not raise it.

That sentence was written in iteration 29 and applied to **that iteration's** numbers and to no earlier
block's. Fifteen iterations later the gate found them. They are released by name.

## 4. A release note written as a RANGE does not release the numbers inside it

The first repair wrote *"`CH-0137` through `CH-0142` were never filed and are RELEASED UNUSED"*.
The gate went from 13 defects to **three**: `CH-0138`, `CH-0139` and `CH-0140` — the numbers a range *implies*
and does not **name**.

That is the finding rather than a nuisance, and it is the same shape as the rule it enforces: **the register
credits a number nobody wrote down to nobody**. A range is a summary, and a summary is what this whole class
of defect is made of. Every released number is now named.

---

## 5. Validity range

- This is a claim about **cross-references and a checker**, not about the device. It carries no physics.
- The gate checks that a cited identifier **resolves**. It does **not** check that the citation is *apt* —
  that `CH-0154` is the challenge the sentence means. That is the drift class `C-0080` describes, where a
  number's owner still states it and a later claim has superseded it, and no corpus comparison reaches it.
- The two context classes are measured over **this** corpus at this ref. A third legitimate way of naming an
  absent identifier would make the gate fire on a correct sentence, and the honest response then is
  `C-0129`'s: widen the classifier with a named test, or print the residue ungated.
- `JOURNAL.md` and `tools/` are out of scope by decision, stated above and in the checker's own header — so
  a dangling identifier in either is **not** caught, and that is deliberate rather than an oversight.
