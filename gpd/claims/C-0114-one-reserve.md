# C-0114 — **The two spends NDI can make once are one decision, and between the queue row and the measurement the ranking REVERSED.** `T-194` was queued to say *"spend the reserve on the layer"* — the layer buys a whole clause of §3, the buffer buys 1.35–1.75× inside a 123–214 % error bar. `C-0110`, filed in the same iteration, measures the layer and it buys **neither** clause: §3's 100 pN stops arriving at **13.6989179 nm** at 0.5 mM, **below the bottom** of NDI's own 17–26 nm band, so a tall layer is refused at **96 of 96** states on the acceptable clause and admitted at **1 of 96** on the desired one. **The reserve has one claimant, not two**, and the re-issued question is no longer *which* — it is whether the buffer's **1.75×** at the operating point is worth the stabilisation work, which is a **price**, i.e. NDI's column and not ours

| | |
|---|---|
| **Task** | [`T-194`](../tasks/T-194-one-reserve.md) — the buffer and the tall layer are one reserve; rank the two spends and re-issue them as one question |
| **Leaf** | `A2.2` |
| **Verification type** | **logical** — a ranking assembled from two upstream result files by a retained emitter, with no new physics |
| **Verdict** | **PASS on all four predicates, and the second declared falsifier FIRED.** The re-issue is not a re-issue: it is a **correction**, and it is labelled as one. `T-194`'s own queue row asserts *"the tall layer is the only route to a whole clause of §3 (`C-0050`)"* — measured, it is a route to **no** clause of §3. What `C-0050` priced is real and is a **displacement**: the uncoupled tile reaches a 10 nm stroke at **52 of 96** tall states. The force does not follow it. That split is [`CH-0127`](../challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md), raised by `C-0110` against `C-0050` in this iteration, and this claim is its consequence for the deliverable. |
| **Maturity** | **TRL 1–3.** Nothing here is measured; every number is model-consistent and traceable to a claim. This claim derives no physics at all — it ranks two numbers other claims derived. |
| **Provenance** | [`gpd/results/T-194-one-reserve.json`](../results/T-194-one-reserve.json), emitted by [`tools/T-194-emit-result.py`](../../tools/T-194-emit-result.py), which reads [`T-156-buffer-route-census.json`](../results/T-156-buffer-route-census.json) (`C-0091`) and [`T-192-device-b-tall-gap.json`](../results/T-192-device-b-tall-gap.json) (`C-0110`) at run time. **No number in this claim is transcribed by hand.** |
| **Conditions** | The corpus at iteration 23, after `C-0109`–`C-0113`. NDI's answers of 2026-08-18 as reproduced verbatim in `DECISIONS-FOR-NDI.md`. |
| **Consumes** | [`C-0110`](C-0110-device-b-tall-gap.md) (the layer spend, measured), [`C-0091`](C-0091-buffer-route-census.md) (the buffer spend, and the common-mode qualifier), [`C-0050`](C-0050-desired-stroke-reach.md) (the premise that falls), [`C-0005`](C-0005-mean-field-screening-validity.md) (the 123–214 % one-loop correction that is common mode to all three buffer routes) |
| **Constrains** | [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md), whose decisions 1 and 2 become one entry, and whose *"spend it on the layer"* recommendation is **withdrawn**. **No claim is contradicted and no challenge is raised** — `C-0110` already raised `CH-0126` and `CH-0127` against `C-0050`, and this claim is downstream of them. |

---

## 1. Why the two are one

Both of NDI's answers name the same reserve, and the second says so in one word:

> *"…an interesting regime we've been reserving, **again**, for low MgCl₂ concentrations we'd buy with
> additional work on stabilizing DNA origami at low salt."*

The tall layer is available **at 0.5 mM**, and 0.5 mM is bought with origami-stabilisation work.
So the two decisions are one budget line with two claimants,
and the programme presented them as independent asks.

That is `C-0091`'s finding — *counting routes that are one number* — reaching the **deliverable**.
`C-0091` found it inside the corpus and mechanised nothing that could catch it outside,
because the instrument that finds it (`tools/result-transfers.py`) compares **result files**,
and a document that asks two questions has no result file.

---

## 2. What each spend buys, read at the state the device occupies

| | buffer: 2 mM → 0.5 mM | tall layer: 10 nm → 17–26 nm |
|---|---|---|
| owner | `C-0091` (`T-156`) | `C-0110` (`T-192`) |
| routes named | 6 | 1 |
| routes surviving | **3** — 1 withdrawn, **2 are the others read again** | — |
| best advantage at the operating point | **1.75104168×** | — |
| weakest surviving advantage | **1.2825845×** | — |
| the same clause read at **zero stroke** | 4.96557132× — **not the device's state** | — |
| §3's 100 pN reaches | — | **13.6989179 nm** at 0.5 mM, 11.8724439 at 1 mM, 10.1299463 at 2 mM |
| against NDI's band | — | **1.241× short at 17 nm, 1.898× short at 26 nm** |
| §3 **acceptable** clause (3 nm at 33.333 pN/nm) | delivered | **0 of 96** states |
| §3 **desired** clause (device B, 10 pN/nm) | — | **1 of 96** states — a bracket disagreement, not a design |
| stroke alone, uncoupled | — | **52 of 96** — real, and empty of force |

**Both columns are greppable from `gpd/results/T-194-one-reserve.json`**, which derives them from the two
upstream files rather than restating them.

---

## 3. The qualifier the deliverable has to carry, stated rather than implied

The three surviving buffer routes are **not three pieces of evidence**.

- Two of the six named routes are **transfers**: `C-0016` carries `C-0012`'s own number at 15 of 15 states
  (departure exactly `0.0`) and `C-0027` carries `C-0017`'s and `C-0018`'s at 20 of 20 (`2.7e−8`, which is
  one file printing eight significant digits where the other prints nine).
- One is **withdrawn** (`C-0032`, `CH-0098`).
- The three survivors run through **two** distinct mechanisms and **one** field model, and
  `C-0005`'s one-loop correction — **123–214 %** of the leading term over this gap range — is **common mode
  to all three and larger than every one of the advantages**.

So the honest statement is *"a preference worth up to 1.75× at the operating point, inside an error bar
larger than itself, on three readings that do not diversify the exposure"* —
not *"three independent routes recommend it"*.

**And one number in that headline cannot be grepped.**
`C-0091`'s triple *"1.35, 1.57, 1.75"* is `1.75104168` and `1.57034099` from fields, and **`1.3480` from a
prose string** inside its own result file — the `Q5` re-read, a bias margin of 1.8706 against 1.3877.
Recorded in the result file as what it is, because a headline number with no field behind it is precisely the
class this repository's checkers cannot see.

---

## 4. Which column this programme can rank, and which it cannot

**It can rank what the two spends BUY.** It has just done so, and the answer is not close: one of them
buys nothing.

**It cannot rank what they COST.** Both are bought with the same unpriced currency — origami stabilisation
work at low salt — and this programme has no column for it. That is not a gap to be closed by a better
calculation: `CLAUDE.md` records the distinction the hard way, because *"it costs nothing"* was written of
0.5 mM on `C-0007`'s ≤ 0.4 % layer sensitivity — **a statement about the modulus standing in for a statement
about the cost** — and NDI's answer priced it in origami stability at low salt, a fabrication cost with no
column here.

The distinction that keeps this tractable: **the loop can see fabrication YIELD wherever it is published**
(Rothemund's 63 % → 11 %, Ke's 8 bp domains, Strauss's 48–95 % incorporation) **and can see fabrication COST
nowhere at all.**

---

## 5. What the re-issued question actually is now

Not *"which of these two do we spend the reserve on"*.

One claimant has been **withdrawn by measurement**, so the reserve has one, and the question is:

> **Is a preference worth up to 1.75× at the operating point — inside a 123–214 % error bar that is common
> mode to every route that recommends it — worth the origami-stabilisation work that buying 0.5 mM costs?**

That is a **price** question. This programme cannot answer it and should not pretend the ranking of the
buys settles it.

**And the second thing to carry back is not a question at all**: NDI's own objection to decision 2 —
*"the debye length of operation in 2 mM MgCl₂ is only about 4 nm"* — is now **answered and upheld**, and
the concession is ours. This programme's standing rebuttal was that *"the Debye length is three numbers here
and the gap's is counterion-set"*; `C-0110` measures that this is about ion **content** (still true) and
never about the **decay**, and that diluting to 0.5 mM makes NDI's own estimate **optimistic** rather than
conservative, because the far field is reached in `κh` and not in `h`.

---

## 6. Validity range, and what this claim does NOT do

- **It re-derives nothing.** Every number is another claim's, read from that claim's result file.
  If `C-0110` or `C-0091` moves, this moves with them and the emitter re-derives it in one run.
- **It ranks two spends, not three.** The four-layer tile (`C-0109`, same iteration) is *not* a claimant on
  this reserve — it is paid for by scaffold NDI already has — and folding it in here would repeat the
  error the claim is about.
- **`T-50` is untouched and stays open.** 2 mM remains the nominal, so the last unbounded exposure on the
  critical path is unaffected by any of this.
- **The 1 of 96 survivor is a bracket disagreement**, and this claim does not upgrade it to a design.
