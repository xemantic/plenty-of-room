# T-131 — Reconcile `ANSWERS.md` against the claims, line by line

| | |
|---|---|
| **Leaf** | — (a process task; it audits the deliverable that reports every leaf) |
| **Predecessor** | none. `ANSWERS.md` has never been reconciled end to end; it has been edited piecemeal by several agents and by the coordinator across seven iterations |
| **Verification type** | **logical** (a statement-by-statement trace to owning claims, with each owning claim's current status read from the challenge that stands against it) **+ in-silico** (a numeric tracer with its own executable tests, [`tools/trace-answers.py`](../../tools/trace-answers.py) / [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py)) |
| **Status** | **DONE** (iteration 12) — claim [`C-0067`](../claims/C-0067-answers-reconciliation.md) |

---

## Formulate

### The question

`ANSWERS.md` is this repository's **primary deliverable** — the file NDI reads first — and its own header
says what it is:

> It is a **synthesis, not a source**.
> Every number here belongs to a claim in `gpd/claims/`, and the claim carries the provenance,
> the validity range and the verdict.

Nothing has ever checked that sentence. **Does every number in it belong to a claim, and does the claim
still say what the file says it says?**

Three drifts were already found by hand before this task was written, which is what motivated it:
the short version once contradicted its own §3 on whether `C-0014`'s substrate tethers were still in the
design; the `E5g16` passage predated `C-0040`, `C-0039` and `C-0049`; and the *"≥ 34 load paths"* floor was
quoted as a material property when `CH-0059` shows it to be a property of the placement convention.
Three by hand, in one file, with no systematic pass ever run.

### Why this is not a rewrite

The physics is not re-derived here. A reconciliation asks exactly three questions of each statement:

1. **Which claim owns it?**
2. **Does that claim still state it** — the same number, the same sense?
3. **Has a later claim superseded it?**

Where the answer is *yes, yes, no*, the statement is left alone and recorded as traced.
Where it is not, the statement is corrected **and what it said before is preserved in the file**, because
this project does not overwrite silently.
Where no claim states the number at all, the statement is **listed rather than deleted** — an untraceable
number in the primary deliverable is exactly what this loop exists to catch.

### Locked units and conventions

- SI throughout; lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly),
  energies in `k_BT` at `T = 300 K`, `k_BT = 4.142 pN·nm`. Nothing here computes a new quantity, so the
  units matter only as the frame every quoted number is read in.
- A **statement** is one assertion carrying at least one number or one verdict — a sentence, a clause set
  off by an em dash, or one cell of a table. Statements are the unit of adjudication.
- A **numeric token** is a maximal run of digits and decimal point after claim, task, leaf, section, date
  and URL identifiers have been stripped. Tokens are the unit of the machine check.
- **TRACED**: the numbers appear in a claim, and the claim still asserts them in the sense the passage uses.
- **DRIFTED**: the owning claim no longer says it, or a later standing claim supersedes it, or the passage
  contradicts another passage of the same file.
- **UNTRACEABLE**: no claim or challenge in the repository states the number or the range.
  Arithmetic performed *in* the synthesis (a min/max over a claim's own table, a complement, a rounding) is
  **not** automatically untraceable — but it is recorded, because it is a place where the file is a source
  and says it is not.

### Acceptance predicates

| | predicate | falsifiable how |
|---|---|---|
| **`P1`** | Every numeric token of `ANSWERS.md` is classified against the claim corpus by a tool with its own tests, and the count of tokens appearing in **no** claim is reported. | A token the tool calls absent that a claim in fact states, or the reverse. Both are tested directly. |
| **`P2`** | Every statement of `ANSWERS.md` is adjudicated TRACED / DRIFTED / UNTRACEABLE against the owning claim's **current** status, with the challenge that stands against the owner named. | A statement whose adjudication a reader can overturn by reading the named claim. |
| **`P3`** | Every DRIFTED statement is corrected **in place**, with its previous wording preserved and the correcting claim named. | A correction that deletes rather than supersedes. |
| **`P4`** | The iteration 5–11 results that change what the file should say are carried in, or their absence is stated as a coverage gap rather than left silent. | A claim from `C-0031`–`C-0064` that moves a statement and is neither carried in nor recorded as uncarried. |
| **`P5`** | The open-questions list is complete against `TASKS.md`'s own *Open questions for Kazik*, current, and stated as **questions** rather than as findings. | A specification question in `TASKS.md` that `ANSWERS.md` does not ask. |

### What would falsify the approach

If the tracer's ABSENT count came back large — dozens of numbers in no claim — the conclusion would not be
*"`ANSWERS.md` is full of invented numbers"* but *"the tracer's normalisation is wrong"*, and the tool
would have to be fixed before any statement was touched. The tool is therefore tested against both
failure directions (a false ABSENT and a false CITED) before it is run on the file.

---

## Plan

### Method, and why this one

Two halves, cheap first.

**The cheap half is mechanical and it runs first.** A number that appears in **no** claim cannot be traced
by any amount of reading, so finding those costs one pass and bounds the size of the problem before any
adjudication starts. That is `tools/trace-answers.py`: strip identifiers, normalise the typography
(`ANSWERS.md` uses en dashes, U+2212 and `×` where the claims sometimes use hyphens), tokenise, and test
each token against every claim and challenge with a substring guard so `45` does not match inside `1.45`.

**The expensive half is the adjudication, and only it can find the drift that matters**, because drift in
this file is overwhelmingly *semantic*: the number is still in the claim and the claim has been superseded.
No tool sees that. It is read against a claim-status index built from every challenge's own **Against** and
**Status** fields.

### Justification against cost

The alternative — re-deriving the physics of each statement — is the wrong instrument twice over. It costs
orders more, and it cannot find the failure mode that actually occurs here: `ANSWERS.md`'s errors are not
arithmetic, they are **stale verdicts and missing supersessions**, and re-deriving a number confirms the
number while leaving the staleness untouched. The three drifts already found by hand were all of that kind.

The tool is built rather than grepped by hand because the file carries ~560 numeric tokens against 60
claims and 69 challenges; by hand that is ~38 000 comparisons, and the one that is missed is the one that
matters.

### What is deliberately not done

- **No claim is re-run and no number is re-derived.** Where a claim and `ANSWERS.md` disagree, the claim
  wins by construction — `ANSWERS.md` is the synthesis.
- **No claim is challenged for being wrong** unless the reconciliation found it wrong, as opposed to
  finding `ANSWERS.md` misreporting it. (None was; see the claim's Part 5.)
- **The window is not re-synthesised.** Iterations 8–11 have not been intersected against `C-0027`/`C-0051`
  and this task does not do it — it records the gap in `ANSWERS.md` so it is visible rather than assumed.

---

## Execute — the trace table

Statuses are as of iteration 12. **T** = traced, **D** = drifted (corrected in place), **U** = untraceable
(listed, not deleted). Owners are the claim the number is grepped out of, which is not always the claim the
passage cites — that difference is itself a finding and is marked.

### §1 — the short version

| # | statement | owner | status |
|---|---|---|---|
| 1 | rigid *exactly* under a uniform load, dishes under every departure | `C-0006`, `C-0009` | **T** |
| 2 | lever and area sensor differ by **32 %** of the stroke | `C-0022` (`0.321`) | **T** as the free-tile / uniform-coupling figure |
| 3 | *"that part is **irreducible** … which no coupling choice can remove"* | `C-0022` | **D** — `C-0058` reaches **0.0753** by distributing the same total, `C-0063` **0.0706** with equal springs by placement alone, both against **0.3079** with no coupling. Corrected in place |
| 4 | lateral restoring stiffness exactly zero; **63 nm** in one 1 kHz period, **21×** | `C-0010` (62.8 nm) | **T** |
| 5 | van der Waals well **0.2–5.7 `k_BT`**, stable but not confining | `C-0021` | **T** |
| 6 | one-sided coupling supplies **exactly zero**; eight tethers for **0.07–0.38 nm** | `C-0021`, `C-0023` | **T** |
| 7 | `k_BT/σ` = **1.38 pN** vs `k_BT/σ²` = **0.4602 pN/nm**; 33.333 exceeds it **72×** | `C-0023` | **T** |
| 8 | **1.4–5.4** → **959–7582 `k_BT`**, 0/18 → 18/18 | `C-0023` | **T** |
| 9 | 100 pN at ≤ 2 V reachable; drainage clears 1 kHz by **22×** | `C-0012`, `C-0004` | **T**, with `C-0061`'s 20.73× noted at Task 7 |
| 10 | not the **5–277 pN/nm** the stability table suggested | `C-0016`, `CH-0016` | **T** |
| 11 | **33.333 pN/nm** by arithmetic; floor **0** at 5 and 7 nm, **23.4–27.9** at 10 nm | `C-0017` | **T** |
| 12 | 45 attachments, duplex standoff + **13 nt** ssDNA spacer, **2.2 pN** per path, *"supplies it"* | `C-0017` (`K2`) | **D** in status, not in number — `K2` is one-sided, which `C-0023` had to replace, and `C-0041` places only **15**. Qualified in place |
| 13 | margin at 2 mM **1.19–1.42×** | `C-0017` | **T** |
| 14 | **1.23–1.53×** carrying both corrections; **1.11–1.25×**; **1.34–1.67×**; `C-0019`'s ≥ 1.07× half a correction | `C-0027` (`1.231–1.528`), `C-0019` | **T** |
| 15 | electrostatic one-loop error **123–214 %**, nothing narrows it | `C-0005`, `CH-0019` | **T** |
| 16 | tangent minimum **22.88 pN/nm at 4.55 nm**, *"inside the operating range"* | `C-0032` | **D** — `C-0049` settles the convention as `[0, s*]`, so 4.55 nm is a stroke the placed device never occupies; at the placement stroke it is **25.227 pN/nm**, clearing **4 of 6** floors. Qualified in place |
| 17 | **216** states, margin **1.0000–1.0019** | `C-0032` | **T**, and `C-0051` re-affirms it against `C-0033`'s collar |
| 18 | fold stroke **3.41–4.13 → 2.80–3.17 nm**, through 3 nm at **two of six** models | `C-0032` | **T** |
| 19 | adverse mounting *"**42.4–61.0** pN/nm"* | `C-0032` says **42.38–61.04** | **U/D** — the only token in the file appearing in no claim. Corrected to the claim's own figure |
| 20 | *"past the **40 pN/nm** compliance ceiling at 0 of 8 lengths"* | `C-0023` declared it | **D** — `C-0049` withdrew the ceiling (it is `1.2 ×` a *placement* mandate). The conclusion survives on `C-0035`'s ground instead: both adverse mountings cannot place §3's effort point. Qualified in place |
| 21 | shorter standoff lands **2.2 %** short | `C-0032` | **T** |
| 22 | at 0.5 mM **1.44–5.93×** and **1.038–2.327×** | `C-0032` | **T** |
| 23 | window survived nine claims and ten challenges, one edge outward | `C-0027` | **T** |
| 24 | **ten of twelve** axes; `C-0031`–`C-0050`, `CH-0043`–`CH-0062`; **0 of 6** edges, worst departure **0.0**; exactly **one** `σ`-function; **1.71–3.11×** | `C-0051` | **T** |
| 25 | `C-0049` withdrew the 40 pN/nm ceiling, replaced by `n·a/s` | `C-0049` | **T** |
| 26 | deliverable is a height plus **five** specification questions | `C-0051` | **T** in the count, **incomplete** in the file — the five were named nowhere. Added |
| 27 | coverage of the re-synthesis | — | **gap** — `C-0051` reaches `C-0050`/`CH-0062`; iterations 8–11 are unsynthesised. Stated in place |
| 28 | stability wants thin, window/stroke/force want thick; **13.3×** crossing at 5 nm | `C-0016`, `C-0027` | **T** (`C-0027`'s verdict row: *"`C-0016`'s 13.3× crossing stands"*) |
| 29 | flexure span **31.82 nm = 94 bp**, 8 nm standoffs, two-crossover favourable base, tangent **25.23 pN/nm**, window **ℓ = 5–10 nm** | `C-0030` | **T** |
| 30 | draw-in supplied, **3.09×** the demand, compression, buckling **1.41 → 2.18×** (1.64× measured) | `C-0030` | **T** |
| 31 | *"its sign is decided by which body carries the standoffs … §3 does not say which"* | `C-0030` | **D** — `C-0035` settles it: a product of **two** binaries, so *"which body"* decides nothing alone, and exactly one of four mountings is buildable (`Su`). Corrected in place |
| 32 | a single crossover buckles at every length; base orientation worth **9.65×** | `C-0028` | **T** |
| 33 | `T-67`: the 90° routing exists, the optimum is a scaffold excursion | `C-0029` | **D** — `C-0057` finds the **dihedrals do not close** at any of the three reported optima (`CH-0070`); the existence survives, the routing does not. Qualified in place |
| 34 | two strand termini, arm **1.0 nm** vs **1.345**, **3.34×** short, free axis is the buckling axis | `C-0029` | **T** |
| 35 | `E5g16`'s 16 withdrawn; hinge line of **four**; **163.2 nm = 4.08 tiles**; series worth **14.6 %**; `E5a1` | `C-0040` | **T** |
| 36 | `C-0034`'s bracket premise false, composition lands outside the span | `C-0039` | **T** |
| 37 | 40 pN/nm is `1.2 × (100/3)`; desired clause gives **12**; no upper bound; `n·allowable/s` | `C-0049` | **T** |
| 38 | no duplex normal to a single-layer sheet in print; every built body held by a **pin**; the only rigid mounting **triangulated** | `C-0028` | **T** |
| 39 | four azimuths at 8 bp; **270.0°** exactly; **4.286°** vs **8.571°**; **161–176** sites, builds **49–56**; ceiling **52–60**; buildable **34**, 25 in plane, 45 at **49.25 nm**; **62** queries | `C-0055` | **T** |
| 40 | orientation decides twice — `C-0014` across the layer, `C-0020` within the plane, **11.75×** | `C-0014`, `C-0020` | **T** |

### §2 — the eight tasks

| # | statement | owner | status |
|---|---|---|---|
| 41 | task table rows 1, 3, 4, 5, 5b, 6, 7, 8 | as cited | **T** |
| 42 | row 2: *"`P2` closed by `C-0017`"* | `C-0017` | **D** — `C-0051`: `P2` stands for the **affine mandate** and **fails** for the realised coupling (`C-0032`). Corrected in place |
| 43 | Task 1: height law replaced; marginal solvent; **0.02–0.10** thermal blobs; three of six models zero at `L₀` | `C-0003`, `C-0011` | **T** |
| 44 | Task 1: **47.7–64.1 pN/nm** at the working point | `C-0010` — **not** the three claims in the row above it | **T**, owner named in place |
| 45 | Task 2: `[0.0116, 0.2885]`, **24.8×**; `[0.0296, 0.0496]`; **0.0751** vs **0.00563**, **13.3×**; **183** points | `C-0016`, `C-0027` | **T** |
| 46 | Task 2: force-onset convention; **1.6–3.3 kDa** / **1.1–1.2 kDa**; 2–9 nm spacing; **8–9 kDa** in the other convention | `C-0016` (1.61–3.29 / 1.11–1.23) | **T** |
| 47 | Task 2: *"**Seven of the eleven** axes"* | `C-0027` | **D** — `C-0051` re-ran the census: **ten of twelve**. Corrected in place |
| 48 | Task 2: peak per-path force **3.9–8.9 pN** inside the window | `C-0016`'s table (7 nm 3.90–6.90, 10 nm 4.04–8.90) | **U** as a range — no claim states `3.9–8.9`; it is a min/max over `C-0016`'s own table. Owner named in place, number kept |
| 49 | Task 2: `η = 1`, **33.5 nm**, ~107 nm; **39.4** / **27.7 nm**; **54.9 pN**, 55 %; **25–186×**; **116.6 nm**; **115.9 nm** | `C-0020`, `C-0024`, `CH-0029` (27.7) | **T** |
| 50 | Task 2: *"`T-1f` … is now the binding uncertainty"* | — | **D** — `T-1f` is done (`C-0019`) and `CH-0019` shows it bounds the **wrong** expansion at ≤ 9.4 %. It is `T-50`. Corrected in place |
| 51 | Task 2: `C-0050`'s **9.790 / 8.959 / 7.424 nm**, **0 of 14** and **3 of 14**, taller layer **16.63–26.12 nm** | `C-0050` | **T** |
| 52 | Task 2: `Gi` **0.30–1.71**, **1.30** at the design point, **9.4 %**, windows widen **13.4 %** / **1.8 %** | `C-0019` | **T** |
| 53 | Task 3: **0.065–0.699 V**, **0.082–0.368 V**, 5–12× margin, saturated above ~0.5 V, blocking understates by **20×** | `C-0012`, `C-0018` | **T** |
| 54 | Task 4: **0.097–0.425 V**; **43 of 54**; **11 of 54**; **0.130–0.184 V**; **1.007–1.032**; 19–42 % → 0.7–3.2 %; 0.5 mM **1.29–2.36×**; **49 of 54**; **0.085–0.595 V**; **2.3e−3**; **324** states; **1.9–5×** | `C-0018`, `CH-0017` | **T** |
| 55 | Task 4: delivered stroke **2.973–2.982 nm**, **0.6–0.9 %**; tethers worth **four** grid steps and three; **1.230×** | `CH-0036`, `C-0027` | **T** |
| 56 | Task 4: **5–19 %**, **14.7 %**, **25.8 %**, **1.65 nm** collar, force-pinned **1.34–1.67×** | `C-0022`, `CH-0035` | **T** |
| 57 | Task 6: **123–214 %**, **1.46 nm**, **12.9 nm**, 3.0 vs **24** | `C-0005` | **T** |
| 58 | Task 7: **22×** and **5.6×**, not binding | `C-0004` | **T**; `C-0061`'s **22.81× → 20.73×** added, still discharged |
| 59 | Task 8: **0.87–0.96 nm**, **0.069–0.110 nm**, vs 3.0; corner `√7`; `A1.2`'s CI not discharged | `C-0010` | **T** |
| 60 | zero bias: **0.360–0.501** / **0.019–0.041 nm**, **53 %** of the time above `L₀`, **2.6×**; **2.56–12.98 nm**, 15 of 18; **0.217–0.352** / **0.012–0.035 nm** | `C-0021`, `C-0023` | **T** |

### §3 — the open questions of §4

| # | statement | owner | status |
|---|---|---|---|
| 61 | (a) four brush criteria failed; `L₀/R₀ ≥ 1` admits all **183**; `Σ ≥ 1` owns every lower edge | `C-0016`, `C-0011` | **T** |
| 62 | (b) empty at 5 nm only; a trade rather than an ordering | `C-0011`, `C-0016` | **T** |
| 63 | (c) excludes **23–48 %** of the salt (complement of `C-0005`'s *"admits only 52–77 %"*), **1.14–1.39×**, amplifies **1.15–1.60×**, decrement **3.9 %**, **97 % water**, bound one-sided | `C-0005`, `C-0008` | **T** |
| 64 | (d) not binding | `C-0004` | **T** |
| 65 | (e) decay length **1.8–2.8 nm**, bias-dependent; 0.5 mM vindicated twice | `C-0008`, `C-0012` | **T** |
| 66 | (f) 35–60 pN is not a per-path allowable; **48–65** shear, **10–15** unzip, **65** ceiling | `C-0006`, `CH-0029` | **T** |
| 67 | (f) *"**45** … are **needed for flatness**"* | `C-0006`, `C-0015` | **D** — `CH-0034`: 45 is where further attachments **stop buying** flatness, not where the tile becomes flat. Corrected in place |
| 68 | (f) **2.3–7.6×** out of plane; **3.9–8.9 pN** inside the window; `C-0026`'s **0.150 / 0.332 / 2.222 / 0.883 pN** and 12× clear; `η = 1.0000`/2.33 | `C-0009`, `C-0026`, `C-0020` | **T** |
| 69 | (f) the ≥ 34 load-path floor, struck through and marked withdrawn; **150 → 45** and **50 → 15**; **48 / 34.8 / 18.8 pN**; **14.3 bp**; `1/m`, `1/D`, **720 pN** | `CH-0059`, `C-0049`, `C-0024`, `CH-0029` | **T** — this correction was made before this task and it holds |
| 70 | (g) **26–369 %**; **32 %** irreducible; **0.149** saturation; `C-0058`'s **0.0753 / 0.0544 / 2.762 / 0.187 / 0.071 / 13 % / 1.96×**; `C-0060`'s **1.0–19.1 % / 3.5–20 / 25× / 8.3× / 0.0715–0.0815 / 5.44 % / 1.3e−4 / 34.6 % / 2.04× / 1.71× / 52.36 nm / 0–30 of 45 / 0.0653** | `C-0058`, `CH-0071`, `C-0060` | **T** |
| 71 | (g) iteration 11 | — | **gap** — `C-0063` (0.0706 by placement, equal springs) and `C-0064`/`CH-0077` (five states are four devices; all four inside 0.10 over their own ranges) were absent. Added |

### §4 and §5

| # | statement | owner | status |
|---|---|---|---|
| 72 | §7 criteria rows: re-derivation, premises, cost, validity, maturity, the dissolved `χ`, the unanswerables | as cited | **T** |
| 73 | *"**Twenty-nine** challenges"* | — | **D** — there are **69**, against 60 claims. Corrected in place |
| 74 | 95 % CI of `A1.2`; Mg²⁺/PEG constant; `k_θ`; correlation direction for opposite walls; no compression measurement inside the window | `C-0010`, `P-8`, `T-9`, `C-0005`, `P-9` | **T** |
| 75 | *"**Two** paywalled papers"* | — | **D** — Lee et al. **was obtained free** from NIST's repository; one remains (Boucher & Hines). Corrected in place |
| 76 | fluctuation-corrected density profile, bracket ≤ 9.4 % | `C-0019` | **T** |
| 77 | *"Whether `C-0018`'s pull-in bias itself moves … the only unresolved margin a cheap calculation can close"* | `C-0027` | **D** — **answered** by `C-0033` (`T-60`) in iteration 5 and composed by `C-0051`. Marked answered, with the numbers carried in |
| 78 | thermal force in a crossover, **2.78–115.8 pN** | `C-0026` | **T** |
| 79 | whether a duplex can be routed at 90° out of a single-layer sheet | `C-0028` | **T** as a literature answer; narrowed by `C-0029`, `C-0057`, `C-0055` and now says so |
| 80 | *"Which body carries the standoffs, and what sits under the flexure's midspan … a specification gap"* | `C-0030` | **D** — **answered** by `C-0035` (`T-75`, `T-78`) in iteration 5. Marked answered; the specification gap it hands on is `T-95` |
| 81 | *"Whether a strain-softening coupling still satisfies the stability clause"* | `CH-0042` | **D** — **answered** by `C-0032` (`T-76`) and `C-0049` (`T-107`). Marked answered |
| 82 | flexure array on a shared superstructure (`T-31`) | `C-0023` | **T** — still open |
| 83 | electrode material **2.6×**; PZC **0.9–5.1 mV** | `C-0021` | **T** |
| 84 | the specification questions for NDI | `TASKS.md` | **incomplete** — of six (`T-63`, `P-13`, `T-95`, `T-102`, `T-112`, `T-115`) only the electrode was asked. A table of all six added |

---

## Verify

The five gates, as they apply to a logical audit.

1. **Dimensional consistency** — nothing is computed; every quantity is transferred with the unit its claim
   states, and the tracer strips identifiers precisely so a leaf ID or a section number is never read as a
   quantity (tested).
2. **Limiting cases** — the tracer is tested at both failure directions: a number present only in an
   uncited claim must not read as CITED, and a number in no claim must read as ABSENT.
3. **Symmetry and conservation** — the audit's conservation law is that **no statement is deleted**: every
   DRIFTED row is corrected in place with its prior wording retained, so the file's history is recoverable
   from the file.
4. **Numerical convergence** — the substring guard is the convergence question here (does `45` match inside
   `1.45` or `450`?) and it is tested in both directions.
5. **Literature cross-check** — not applicable; the "literature" of a reconciliation is the claim corpus,
   and every number is grepped out of the claim that owns it per `SESSION-PROMPT.md` step 9.

## What was left undone

- **The window is not re-synthesised against iterations 8–11.** `C-0051` reaches `C-0050`/`CH-0062`.
  Recorded in `ANSWERS.md` as a coverage statement rather than assumed away; it is a task, not an edit.
- **`C-0016`'s two readings of the 5 nm crossing are not reconciled with each other.** `C-0027` says both
  *"the crossing widens from 13.32× to 24.80×"* (under the descent term) and, in its verdict row,
  *"`C-0016`'s 13.3× crossing stands"*. `ANSWERS.md` quotes 13.3×, which is what the verdict row endorses.
  This is an ambiguity inside `C-0027`, not drift in `ANSWERS.md`, and it does not move a verdict — 5 nm is
  empty on either reading.
- **Seventeen claims were cited nowhere in `ANSWERS.md`** and only some of them needed to be. Ten are now
  cited — `C-0033`, `C-0035`, `C-0042`, `C-0052`, `C-0057`, `C-0059`, `C-0061`, `C-0062`, `C-0063`,
  `C-0064` — because each moves a statement. The remaining **seven** (`C-0036`, `C-0037`, `C-0047`,
  `C-0048`, `C-0053`, `C-0054`, `C-0056`) are deliberately left to `TASKS.md`: they are branch interior —
  a crossover convention, a truss standoff, a single-column flatness reading, a cap, two packing counts and
  a connectivity criterion — and `ANSWERS.md` answers NDI's questions rather than the programme's internal
  structure. Listed here so the omission is a decision and not an oversight.
