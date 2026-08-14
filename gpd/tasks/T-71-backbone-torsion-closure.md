# T-71 — A backbone-torsion check of `C-0029`'s closed routing

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (measured crystallographic backbone geometry placed into this project's own solved junction placements, and the phosphodiester closed by exact inverse kinematics) **+ logical** (two closed-form reach bounds that run before any solve) **+ literature** (a measured torsion distribution derived from the PDB, and the covalent restraint targets read off the same coordinates rather than cited) |
| **Blocks** | nothing. **Ceilings** [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md), [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md), [`C-0052`](../claims/C-0052-crossbar-junction-trio.md), and through them [`C-0048`](../claims/C-0048-truss-cap.md) and [`C-0037`](../claims/C-0037-triangulated-standoff.md) |

---

## Formulate

### The question, and why it is the standing ceiling on the whole truss branch

Every closure result in this programme tests one **necessary** condition and says so:

> *"a phosphate pair inside the measured `[0.60, 0.70]` nm step with no van der Waals overlap.
> **No backbone torsion angle is checked** and no sequence is designed. A "closes" verdict is an
> **upper bound** on buildability."* — `C-0029`, validity range

That caveat now carries three claims and it has been getting heavier, not lighter:

| claim | junctions | links | binding link | pucker the link demands |
|---|---|---|---|---|
| `C-0029` | 1 | 2 | **0.600 nm** | C3′-endo, the **north** end of the window |
| `C-0042` | 2 | 4 | **0.6969 nm** | C2′-endo, the **south** end |
| `C-0052` | 3 | 6 | **0.679 nm** | C2′-endo |

`C-0042` states the exposure exactly: *"the aligned pair sits at the C2′-endo end of that window …
which is where the torsion check is least comfortable. Whoever runs `T-71` must run it at the
pucker the alignment demands, not at `C-0029`'s."*

### Acceptance predicate

**`P1` — the readout.** For every covalent link of every solved junction, at all three scales,
report the six backbone torsions **α, β, γ, δ, ε, ζ** and the glycosidic **χ** at and around the
exchange point, together with the covalent geometry (`O3′–P` bond, `C3′–O3′–P`, `O3′–P–O5′`,
`P–O5′–C5′`) the closure would have to hold.

**`P2` — the verdict.** A link **closes at torsion level** when
1. every solved torsion lies inside the **measured** populated region of the B-DNA backbone —
   defined here as within the 99th-percentile angular radius of its nearest observed conformer
   class, and never in a region with zero observed population; **and**
2. every covalent bond length and bond angle lies within **3 measured standard deviations** of the
   crystallographic mean.

A junction closes when **all** its links do; a scale closes when **all** its junctions do.

**`P3` — the pucker coupling is stated, not assumed away.** δ is the torsion that *carries* the
sugar pucker: it is not an independent variable but a function of the pseudorotation phase, so a
link pinned at C2′-endo by its own P–P distance has δ pinned with it. The check is run at both
puckers and the coupling is reported as a measured relation, not asserted.

**`P4` — the calibration.** The same pipeline run on a step **inside** one of this project's own
stylised duplexes must return canonical B-DNA torsions and covalent geometry. A junction's strain
is quoted **against that baseline**, because a departure the stylised duplex already carries is a
property of the duplex model and not of the junction.

### Units, conventions and sign

- Lengths **nm** in this project's own geometry; **Å** where crystallographic coordinates and
  covalent restraints are quoted, and the conversion is stated at every boundary (1 nm = 10 Å).
- Torsions in **degrees**, IUPAC sign convention, folded to `(−180, 180]`.
- The six backbone torsions, in the 5′→3′ direction across the exchange point:
  `α = O3′(i)–P(i+1)–O5′(i+1)–C5′(i+1)`, `β = P–O5′–C5′–C4′`, `γ = O5′–C5′–C4′–C3′`,
  `δ = C5′–C4′–C3′–O3′`, `ε = C4′(i)–C3′(i)–O3′(i)–P(i+1)`, `ζ = C3′(i)–O3′(i)–P(i+1)–O5′(i+1)`.
- `χ` purine `O4′–C1′–N9–C4`, pyrimidine `O4′–C1′–N1–C2`.
- Pseudorotation phase `P` by Altona–Sundaralingam from `ν0…ν4`; **north** = C3′-endo
  (`P ≲ 40°`), **south** = C2′-endo (`P ≈ 130–190°`).
- A residue's **local frame** is `(ê_r, ê_t, ê_z)` anchored on its own phosphorus, with `ê_z` the
  duplex's helical axis oriented **5′→3′ for that strand**, `ê_r` radially outward from the axis to
  the phosphorus and `ê_t = ê_z × ê_r`. The two strands of a duplex differ by the sign of `ê_z`,
  which flips `ê_t` with it — a proper rotation, so **one** template serves both.
- **Polarity is a design freedom and is swept**, not assumed: for each link either body may carry
  the donor `O3′`, and each residue's strand may run either way.

### Falsifiers, declared before the run

1. **The baseline failing.** If a step *inside* a stylised duplex does not return canonical
   torsions and near-ideal covalent geometry, the template placement is wrong and no junction
   verdict may be read off it. This is `P4` and it is the gate that licenses everything else.
2. **The cheap reach bound not binding and not clearing** — if it neither excludes nor admits, the
   solve is the only instrument and the task costs a solve.
3. **The verdict being an artefact of the template's pucker.** If a junction closes at one pucker
   and fails at the other, the answer is the pucker's and must be reported as such.
4. **The verdict being an artefact of the phosphate radius.** This project's geometry puts P at
   **1.00 nm** (`C-0029`, Hedley et al.); the crystallographic survey measures its own. If the
   verdict moves across that bracket, it is not a verdict.
5. **A closing verdict at all three scales.** That would *not* discharge the caveat — a torsion
   check is still necessary and not sufficient — and the claim must say so as plainly as `C-0029`
   said it about distances.

---

## Plan

### Method, and the cost justification §5 asks for

**The cheap bounds run first, and they are closed form.**

> **Bound 1 — the pinned-phosphate bond.** The closure searches pin *both* phosphorus atoms: one
> is a sheet/crossbar phosphate, the other a standoff terminus. If those are literally the two
> phosphorus atoms of one phosphodiester step, then the donor residue's `O3′` — which is rigid on
> its own sugar — must sit **1.595 Å** from the acceptor's `P`. That is a single distance per link
> and it costs no solve at all.
>
> **Bound 2 — the free-phosphate reach.** The generous reading lets the bridging phosphorus sit
> where chemistry wants it rather than where the model marked it. Then the only geometric demand is
> that the donor's `O3′` and the acceptor's `C5′` be separated by a distance the chain
> `O3′–P–O5′–C5′` can span. With ideal bonds and angles that span is a closed-form interval in the
> single torsion `α`, evaluated in microseconds — and a link outside it cannot close **at any
> torsion**, which is a proof and not a search.

Only if bound 2 admits is the inverse-kinematic solve worth running, and that is the declared
justification: **an oxDNA or atomistic minimisation is the natural instrument and it is also the
wrong first spend**, because a coarse-grained model cannot answer a question about dihedrals it
does not represent, and an all-atom minimisation would report a *local* minimum where a reach
bound reports an *impossibility*. If bound 2 clears everywhere, the solve is a two-parameter
inverse kinematics and still costs seconds — at which point a molecular-dynamics run buys only a
force field's opinion about a geometry already determined.

**The measured geometry comes from coordinates, not from a citation.** `tools/T-71-bdna-backbone-survey.py`
queries the RCSB for X-ray, DNA-only entries under a stated resolution ceiling, downloads them,
and computes for every deoxyribonucleotide the seven torsions, the pseudorotation phase, and the
covalent geometry of the phosphodiester it makes. It fits the **screw** transform carrying a
three-residue window onto the next — a single dinucleotide step is a poor estimator of a helical
axis — and expresses each residue's atoms in the local frame above. Averaging over a filtered
B-form, south-pucker population gives the rigid **nucleotide template**; k-means on the septet
gives the **populated regions**. Nothing here is a remembered number: the restraint targets, the
torsion distribution, the phosphate radius and the intrastrand P–P step are all measured on the
same coordinates, in one pass, and the entry list is recorded so the sample cannot be curated.

**The solve.** Both residues are rigid and placed by the junction's own solved geometry. The
phosphodiester between them has five torsions and the relative placement has six degrees of
freedom, so the closure is over-determined by exactly one — an *exact* statement, and the reason a
residual is expected even for a good junction. Reading **A** (phosphorus pinned, the model's own
criterion taken literally) leaves one unknown, γ, against five residuals; reading **B** (phosphorus
free) leaves two, (γ, β), against three. Both are solved by a deterministic grid plus fixed
refinement, with no tolerance in any control flow.

### What would falsify the approach

- If reading B's residual at the **baseline** — a step inside a stylised duplex — is as large as at
  a junction, the instrument cannot discriminate and the task must report that instead of a verdict.
- If the answer moves between the 1.00 nm phosphate radius this project uses and the radius the
  survey measures, the answer belongs to the radius.
- If it moves between the two pucker templates, it belongs to the pucker.

All three are run and reported as sensitivities rather than assumed away.
