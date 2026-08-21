# T-269 — `tools/` has nothing to do with DNA, and should leave

| | |
|---|---|
| **Leaf** | none — step 7 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### It is a decision before it is a task

`result-transfers.py`, `reemission-order.py`, `trace-answers.py`, `result-reader-census.py` and the
`check-*.py` family check **provenance, drift, staleness and transfer between published numbers**.
Nothing in them knows what a duplex is.
Their relatives are **statcheck and GRIM**, not ReproZip.

They also encode findings that are not about this device at all, and that is the argument for extraction:

- a number is traceable to a claim, or it is not;
- a status word is a vocabulary, and an unknown one reads OPEN;
- a gate that cannot come clean is not a gate;
- a checker without self-tests is an assertion;
- a false-positive rate is not a completeness argument.

### Acceptance

| | predicate |
|---|---|
| **P1** | **either** the extraction — a repository of their own, self-tests intact, this tree consuming them — **or** the stated decision that a corpus-integrity toolkit belongs beside the corpus it checks |
| **P2** | if extracted, every gate in `tools/verify.sh` still runs and still fails on the same inputs; the self-tests are the contract |
| **P3** | if not extracted, the reason recorded where a reader will meet it, because the question will be asked again |

---

## 2. Plan

Deliberately **unreserved** in the iteration-39 number block: this should not consume a claim number until
somebody has decided it is this programme's work at all rather than a by-product worth giving away.

The honest counter-argument to extraction is that several of these tools are only correct **because** they
were written against this corpus's conventions — `C-0117`'s topological sort, the struck-span blanking, the
per-record departure keys — and a general tool that has to be configured for each corpus may be worse than a
specific one that is right about this one. `P1` admits that answer.

### What would falsify this approach

- **The self-tests do not survive the move.** Then the tools are coupled to this corpus's layout more deeply
  than the extraction assumes, and `P1`'s second branch is the answer.
