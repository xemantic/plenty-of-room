# CH-0215 — **the committed block does not state its scaffold TOPOLOGY, and the seam question is a topology question** — so the one field that would have settled `CH-0212` on the artifact instead of on the argument is a field the writer does not emit and the reader does not parse. scadnano's strand carries a `circular` property; `gpd/designs/gen1-block-honeycomb-10x6-102-109.sc` carries `domains`, `is_scaffold` and `color`, so **the file asserts a LINEAR scaffold by default** while the budget it was drawn to is **M13's circle** — 6 330 of 7 249 nt with **919 spare** (`C-0160`). Both readings give the same 60 domains and the same verdict (`C-0168`), which is why nothing is wrong with the artifact; what is missing is the **statement**, and it is the difference between *"a linear scaffold"* and *"a circular scaffold closing through a 919 nt remainder"* — two different fabrication orders that this file cannot tell apart

| | |
|---|---|
| **Against** | [`C-0160`](../claims/C-0160-scadnano-writer.md) — its three stated omissions (§6) are *no staple set*, *no turn loopouts*, *no sequence*; the scaffold's **topology** is a fourth and is not among them |
| **Raised by** | [`C-0168`](../claims/C-0168-recommended-block-seam.md) (`T-274`), while closing [`CH-0212`](CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md) |
| **Grounds** | **a census of a committed artifact** — the emitted scaffold strand's own field list, against the scadnano 0.21.1 schema and against `C-0160`'s own nucleotide budget |
| **Status** | **OPEN — filed, not repaired. No number moves and no verdict moves.** What moves is whether a reader of the file can tell which of two fabrication specifications it is drawn to |

---

## The observation

```
python3 -c "import json; d=json.load(open('gpd/designs/gen1-block-honeycomb-10x6-102-109.sc'));
s=[x for x in d['strands'] if x.get('is_scaffold')][0]; print(list(s.keys()))"
# ['domains', 'is_scaffold', 'color']
```

No `circular`. scadnano's default is `False`, so the file **says linear**.

`C-0160`'s own §5 says the design is drawn against M13mp18: *"6 330 nt, 919 spare on M13"*. M13mp18 is
circular. A circular scaffold drawn as 60 domains on 60 helices closes through its **own unpaired
remainder**, which the file also does not draw — `C-0160` §6 states that omission for the raster
**turns** (*"No turn loopouts"*) and gives the reason (a schema step the reader does not parse), and the
remainder is the same schema step at the other end of the strand.

## Why it matters, and why it is small

`C-0168` settles `CH-0212` on the **argument**: the seam is forced only on a **fully folded** circular
scaffold, and 919 nt close the circle at 1.03–2.69 `k_BT` against the 8.0 `k_BT` the host sheet pays
per crossover column. That derivation is **indifferent** to which of the two topologies the file means,
because a linear scaffold and a remainder-closed circular one both supply a Hamiltonian path. So:

- **no number moves**, on either reading;
- and the artifact is therefore **not wrong** — it is **silent**.

What the silence costs is one recorded consequence: `CH-0212` proposed settling the fork *"on the
artifact rather than on the argument"*, and neither of its two proposed readings can be taken from
this file. The second (a column parity sequence) is unavailable because the block carries no staple
set; the first (which premise the theorem needs) is unavailable because the file does not say whether
the scaffold is a circle. **A design file that cannot state its own scaffold topology cannot answer a
topology question about itself**, which is the general form.

It also touches a **specification** row: `DECISIONS-FOR-NDI.md` decision 5 records NDI's answer as
*"CIRCULAR M13, ~7–8 k nt — so circular-with-remainder is the DEFAULT"*. The committed artifact for
the recommended tile currently defaults to the other one.

## What would settle it

Three steps, in increasing cost, and the first two are schema rather than science:

1. **Emit `circular` on the scaffold strand** and parse it in `ScadnanoDesign`. One boolean, one field
   in `ScadnanoStrand`, and the writer's round trip already gates it.
2. **Draw the 919 nt remainder as a loopout.** This is the same heterogeneous-`domains` schema step
   `C-0160` §6 declines for the turn loops, so the two are one task and not two.
3. **State the choice in [`gpd/designs/README.md`](../designs/README.md)** beside the two width
   readings it already carries, with `C-0168`'s closure price, so a reader of the artifact meets the
   fabrication order rather than inferring it.

## What is *not* claimed

- **Not** that the emitted routing is wrong. 60 domains on 60 helices is a Hamiltonian path and it is
  what `C-0168` shows the block needs.
- **Not** that the reference implementation objects. It loads the file with zero warnings either way.
- **Not** that a `circular` flag would have changed `C-0168`'s verdict. It would have changed which
  *half* of `CH-0212` could be answered from the file.
