# T-281 — a census must NAME the claim that discharges each family, and refuse a family whose discharge it cannot name

| | |
|---|---|
| **Raised by** | [`CH-0229`](../challenges/CH-0229-a-census-assumes-a-premise-is-withdrawn-once.md), from [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) / [`T-260`](T-260-partial-discharge-predicate.md) |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation test, and a retrospective measurement over the census's own revision history |
| **Units** | none; this is a document-integrity task |

## Formulate

`CH-0182` established that a census is dated by its **premise set**.
`CH-0229` is the same observation on the other axis: a census is dated by its **discharge**, and a discharge can be **partial** —
`C-0141` discharged the station lattice, the plan ceiling and the placement family, and did **not** discharge the grillage,
which `C-0154`/`C-0167` supplied two iterations later.
One token, `single-layer square-lattice`, therefore carries two statements with two correcting claims and **two dates**,
and the census inherited the earlier one.

`C-0176` supplied the **representation** — `DISCHARGES`, `FAMILY_DISCHARGE` and a per-family discharge — and explicitly not the **discovery**.
Nothing tells an author that the family they are adding has two correcting claims rather than one,
because the registry still **defaults**:

```python
def discharge_of(family):
    return FAMILY_DISCHARGE.get(family, SUBJECT)
```

A family absent from the map silently becomes a debt of this census's own subject.
That is the defect in one line: **an absence is read as an answer.**

**The cheap bound, which decides the shape before any code is written.**
`tools/T-234-census.py` emits **8** family names over the corpus (`FOOTPRINT`, `WIDTH`, `AZIMUTH`, `SCAFFOLD`, `PLACEMENT`, `GRILLAGE`, `SQUARE`, `ROW_SPAN`)
and `FAMILY_DISCHARGE` declares **3** of them.
**5 of 8 families are answered by a default that nobody wrote down** — and the two that needed the split, `PLACEMENT` and `WIDTH`, were among the defaulted ones for eight iterations.

**Numeric target.**
Every family the census can emit **names** its discharge, and its discharge **names** at least one claim;
the getter **refuses** an undeclared family; the report has a **third** verdict state so that a clean reading is distinguishable from an unanswerable one;
and the gate's pre-existing defect count is **unchanged at 18**, all 18 being `T-282`'s.

**Acceptance predicates, falsifiable.**

- **F1 — the getter refuses.** `discharge_of` on a family that is not declared raises, rather than returning the subject discharge. Asserted in **both** directions: a declared family returns its discharge (including the explicit `None`), an undeclared one raises.
- **F2 — a discharge must name a claim.** A family may not name a discharge that names no pointer. Constructing a registry with a non-`None` discharge whose pointer tuple is empty is refused at construction, and so is a family naming a discharge the registry does not define.
- **F3 — the report has three states and does not refuse.** `DECLARED`, `VACUOUS`, `UNDECLARED`. An empty violation list on an **unanswerable** family must be distinguishable from a clean one, so `UNDECLARED` is a state of the report and not an omission from it. And a rule with an **empty domain** is **vacuous, not withheld**: a declared family the census finds **zero** occurrences of reads `VACUOUS`, which is clean, and must never be reported as `UNDECLARED`.
- **F4 — the census refuses to be written.** With an undeclared family in play, `census()` raises and `--check` exits **1** naming the family, rather than emitting a census in which that family is silently a subject debt.
- **F5 — nothing else moves.** The 391 census records, their families, their indices and their `discharge` fields are **identical** before and after, and `python3 tools/T-234-census.py --check` reports the same **18** pre-existing defects it reports at `HEAD`. This task adds none and repairs none: the 18 are `T-282`'s, and a regeneration mid-iteration is refused on `C-0176` §1b's own ground.
- **F6 — the mutation test.** Every rule of the registry fails at least one **named** test when mutated; every mutation **replaces** a rule wholesale rather than widening it to `original|mutant` (`C-0177` measured that trap at 9 of 22 rows, and `C-0176`'s own first table had it); the count of mutations that fail **nothing** is reported, in **both** directions where a predicate carries exclusions.
- **F7 — the retrospective is measured, not argued.** The rule is run over **every** revision of `tools/T-234-census.py`, against the corpus at that same revision, and every refusal is reported with its ground: how many refusals, on how many distinct families, and how many of those had a **default the corpus later contradicted**. `CLAUDE.md`: a drift checker's false positives cost more than its true ones, and that is a **rate**.

**What would falsify the approach.**
If a family's discharge could not be named at the moment the family is written — if the corpus routinely censused a premise **before** any claim corrected it —
then the rule would refuse legitimate work and would have to become an advisory.
The measurement that settles it is F7: a refusal whose answer does not exist yet is a false positive, and one whose answer is a single claim already in the corpus is a one-line prompt.

## Plan

**Where the rule lives, and why it is not an entry in `CLAUDE.md`.**
`CLAUDE.md` records four times that *a convention is not a mechanism*, and once — `C-0177`'s cold-start note — that a convention written down **twice** still drifted.
So the deliverable is a rule that **fails**, not a sentence: `tools/census_discharges.py`, a module whose `DischargeRegistry` cannot be constructed with a discharge that names no claim and cannot be **asked** about a family that names no discharge.
It is a separate module rather than more lines in `tools/T-234-census.py` because the rule's subject is *"whatever writes the next census"* and there is exactly **one** census today —
`CLAUDE.md`'s own instruction is to enumerate the implementations a rule has to reach before writing *"the rule now lives once"*, and enumerating them here gives **1**, which is precisely when a shared module is cheap.

**Cheap bound first, and it is the whole method.**
The registry is a data structure, not a predicate, so it has no false-positive rate against a *declared* registry: it reads a declaration and cannot mistake a declared family for an undeclared one.
What it does have is a **firing count** against the corpus's past, and that is three `git archive` extractions —
`tools/T-234-census.py` has **3** revisions, so the retrospective costs three corpus scans and no JVM.

**The getter/report split, which is `CLAUDE.md`'s own rule read twice.**
*A getter must refuse and a report must not*, and *a report then needs a third verdict state* — an empty violation list on an unanswerable family is indistinguishable from a clean one.
`discharge_of` is the getter and raises `UndeclaredFamily`; `report` is the report and returns a verdict per family.
The second half of the same entry is the trap on the other side: *a rule with an EMPTY DOMAIN is vacuous, not withheld*.
A family declared and matching nothing is **clean**; reporting it as unanswerable would make a correct declaration look like a defect, which is the direction that gets a gate switched off.

**What is deliberately not done.**
The 18 defects `--check` reports at `HEAD` are `T-282`'s and are **not** repaired here.
Regenerating `tools/T-234-classification.json` mid-iteration sweeps in whatever is in flight — `C-0176` §1b — and two other agents hold the tree.
The number is reported before and after so that the coordinator can see that this task added none.

**TDD.** The registry's tests are written first, in `tools/census_discharges.py --self-test`, and are watched to fail against a stub.
The census's own `--self-test` then calls them, so the rule is covered by the Gradle task that already exists (`censusSelfTest`) and **nothing new has to be wired**.
