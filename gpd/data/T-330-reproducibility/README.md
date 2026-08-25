# `T-330` — the two emissions `F9` fired on, retained

`F9` — *two independent emissions of `T-330` are not byte-identical* — was declared **OPEN** and **FIRED on its first run**.
These are the two files it fired on, retained so that the firing stays checkable after the repair (`C-0092`: *a repair must leave the defect measurable*).

```
diff run-a-before-the-repair.json run-b-before-the-repair.json
```

gives **three** fields, and every one of them is a comparison of two quantities that are meant to be zero — `CLAUDE.md`'s own *comparing two quantities that are both meant to be zero relatively compares their noise*, met three times in one study:

| field | run A | run B | what it is |
|---|---|---|---|
| `upstream/3/reproductionDeparture` | `1.05274977E-9` | `1.05274933E-9` | a **departure** in a record type `DEPARTURE_DIGITS_BY_KEY` cannot see — it is keyed on `reproductions` and `convergence`. `C-0218`'s own `F14` fired on the same key in the same block |
| `residue/6/relativeGap` | `3.50153928E-4` | `3.50053749E-4` | the fit-versus-sample gap under a **uniform** pressure at an orthogonal basis, where both readings are the solver's own noise |
| `verdict/worstCorrectedUniformLoadDishing` | `1.25325047E-11` | `1.2532487E-11` | the corrected uniform-load dishing, which is exactly zero in exact arithmetic |

The repairs, all three in `tile/FaceRigidBasisStudy.kt`:

- a per-study `T330_DEPARTURE_DIGITS` map keying `upstream/*` and `residue/*` departures at **two** significant digits, passed to `roundedForResult`'s `digitsByKey`;
- `residue[*].relativeGap` emitted as `0.0` with a `wellPosed` flag where the ratio is a ratio of two noises;
- the corrected uniform-load dishing emitted as a **threshold and a boolean** (`correctedUniformLoadDishingBelow`, `correctedUniformLoadDishingIsZeroAtEveryParity`) rather than as a value.

After them two independent emissions are **byte-identical**, and the committed
[`gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json`](../../results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json) is that file.

**These two are NOT result files** and are not re-runnable: they are the artifact of a source state that no longer exists, kept as evidence.
