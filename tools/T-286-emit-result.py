#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Emit `gpd/results/T-286-a-regime-is-a-set.json` -- `CH-0224`'s census, and what a gate can reach.

    tools/T-286-emit-result.py [--ref <git-ref>] [--self-test]

WHY PYTHON. The subject of this file is the **corpus**: how many committed result files state a
regime, how many studies have an environment coordinate at all, and how many result-file read
edges a regime gate could reach. That is `C-0174`'s own reason for a Python emitter -- *the
subject is the corpus rather than a model* -- and it carries `C-0174`'s obligation with it: a
result file whose subject is a mutable tree must NAME the tree it measured, so the ref is an
argument, it defaults to `HEAD`, and the **resolved** sha is recorded as `baselineRef`.

The behaviour of the type this task lands is measured in Kotlin, by `environment/RegimeSetTest`
and `structure/ResultEmissionTest`, and is deliberately not mirrored here: a mirror of a numeric
rule is a numeric claim and would need its own tests (`CH-0226`).
"""

import argparse
import importlib.util
import json
import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-286-a-regime-is-a-set.json")


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


header = _load("t284header", "emission_header.py")
rounding = _load("t284rounding", "T-278-rounding-simulation.py")
readers = _load("t284readers", "result-reader-census.py")

#: The 22 studies that name `MagnesiumChlorideBuffer`, classified by READING rather than by regex.
#:
#: A mechanical scan for `listOf(0.5, 2.0, ...)` inside an electrolyte study infers a ROLE from a
#: TYPE, which is `CLAUDE.md`'s standing trap: `anchoring/GoldElectrodePzcStudy` carries
#: `listOf(2.0, 10.0)` and those are `C-0021`'s two readings of the tile THICKNESS in nm. So the
#: classification is read, the evidence line is recorded, and the *membership* of this table is
#: asserted against the tree at the measured ref -- a study that starts or stops naming the buffer
#: fails the emitter rather than ageing quietly out of the census.
ELECTROLYTE_STUDIES = (
    ("actuator/CollarEquilibriumPathStudy.kt", (2.0, 10.0),
     "two collar states, `concentration = 2.0` and `concentration = 10.0`"),
    ("actuator/MaximumUsableBiasStudy.kt", (0.5, 2.0, 10.0),
     "`listOf(0.5, 2.0, 10.0)`"),
    ("actuator/StrokeAndBlockingForceStudy.kt", (0.5, 1.0, 2.0, 5.0, 10.0),
     "`listOf(0.5, 1.0, 2.0, 5.0, 10.0)`"),
    ("actuator/TallGapDeviceBStudy.kt", (0.5, 1.0, 2.0),
     "TWO lists: `TALL_GAP_REACH_BUFFERS = listOf(0.5, 1.0, 2.0)` over the tall heights and "
     "`TALL_GAP_BUFFERS = listOf(0.5, 2.0)` over the fold heights"),
    ("anchoring/GoldElectrodePzcStudy.kt", (2.0,),
     "`T193_BUFFER = 2.0`; its `listOf(2.0, 10.0)` is `T193_TILE_THICKNESSES`, in nm"),
    ("anchoring/TwoSidedCouplingStudy.kt", (2.0,), "`BUFFER = 2.0`"),
    ("anchoring/ZeroBiasRestingPositionStudy.kt", (2.0,), "`BUFFER = 2.0`"),
    ("coupling/OutputCouplingStudy.kt", (0.5, 1.0, 2.0), "`listOf(0.5, 1.0, 2.0)`"),
    ("crossover/ConcentratedCrossoverStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("electrostatics/BeyondMeanFieldGapStudy.kt", (0.5, 1.0, 2.0, 10.0),
     "`listOf(0.5, 1.0, 2.0, 10.0)`"),
    ("electrostatics/CutRimChargeStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("electrostatics/EdgeWidthDependenceStudy.kt", (2.0,),
     "selects `value(\"concentration\") == 2.0` out of `T-3b`"),
    ("electrostatics/MeanFieldValidityStudy.kt", (2.0, 5.0, 10.0), "`BUFFERS = listOf(2.0, 5.0, 10.0)`"),
    ("electrostatics/NonlinearPbProfileStudy.kt", (2.0, 5.0, 10.0),
     "`BUFFERS = listOf(2.0, 5.0, 10.0)`; its `listOf(5.0, 10.0)` is a height pair"),
    ("electrostatics/PlanarCouplingWallStudy.kt", (2.0,), "`OPERATING_BUFFER = 2.0`"),
    ("electrostatics/ScaffoldRemainderStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("electrostatics/TileEdgeLoadProfileStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("stability/LargeRotationArmBranchStudy.kt", (0.5, 2.0), "`listOf(0.5, 2.0)`"),
    ("stability/RecommendedElementFoldStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("stability/SofteningCouplingStabilityStudy.kt", (0.5, 2.0, 10.0), "`listOf(0.5, 2.0, 10.0)`"),
    ("structure/DrawableRaggedFaceStudy.kt", (0.5, 1.0, 2.0), "`listOf(0.5, 1.0, 2.0)`"),
    ("structure/RaggedFaceCostStudy.kt", (0.5, 1.0, 2.0), "`listOf(0.5, 1.0, 2.0)`"),
)

#: The three studies this task actually lands the declaration on, one per emitted value.
DEMONSTRATED = (
    ("brush/BrushStiffnessStudy.kt", "T-1-layer-stiffness.json", "STATED, buffer null",
     "Regime.neutralLayer -- the documented physical claim that ideal mobile salt cancels out of "
     "a neutral grafted layer exactly, and a THIRD value distinct from both absences"),
    ("electrostatics/MeanFieldValidityStudy.kt", "T-6-mean-field-screening-validity.json",
     "STATED, three members",
     "one Regime per molarity of BUFFERS = {2.0, 5.0, 10.0}, over GAP_HEIGHTS = [5, 10] nm at "
     "zero applied bias"),
    ("anchoring/TwoSidedCouplingStudy.kt", "T-23-two-sided-coupling.json", "STATED, one member",
     "BUFFER = 2.0 mM over fieldGaps = [3, 10] nm, bias [0, 1] V, band 1000 Hz"),
)

_BUFFER_NAME = "MagnesiumChlorideBuffer"


def _resolve(ref):
    return subprocess.check_output(
        ["git", "-C", ROOT, "rev-parse", ref], text=True
    ).strip()


def _tree_at(ref, target):
    subprocess.check_call(
        "git -C {root} archive {ref} | tar -x -C {target}".format(
            root=ROOT, ref=ref, target=target
        ),
        shell=True,
    )


def _artifact_census(results_dir):
    """The four states of `emission.regime` over one directory of result files."""
    counts = {"resultFiles": 0, "withHeader": 0, "regimeNotStated": 0,
              "regimeEmptySet": 0, "regimeStated": 0, "withoutHeader": 0}
    stated = []
    for name in sorted(os.listdir(results_dir)):
        if not name.endswith(".json"):
            continue
        counts["resultFiles"] += 1
        with open(os.path.join(results_dir, name), encoding="utf-8") as handle:
            try:
                document = json.load(handle)
            except ValueError:
                continue
        emission = document.get("emission") if isinstance(document, dict) else None
        if not isinstance(emission, dict) or "regime" not in emission:
            counts["withoutHeader"] += 1
            continue
        counts["withHeader"] += 1
        regime = emission["regime"]
        if regime is None:
            counts["regimeNotStated"] += 1
        elif isinstance(regime, list) and not regime:
            counts["regimeEmptySet"] += 1
        else:
            counts["regimeStated"] += 1
            stated.append({"file": name, "states": len(regime) if isinstance(regime, list) else 1})
    return counts, stated


def _source_census(root):
    """Which studies name the buffer at this tree, so the declared table cannot age quietly."""
    found = []
    base = os.path.join(root, "src", "main", "kotlin")
    for directory, _, files in os.walk(base):
        for name in sorted(files):
            if not name.endswith("Study.kt"):
                continue
            path = os.path.join(directory, name)
            with open(path, encoding="utf-8") as handle:
                if _BUFFER_NAME in handle.read():
                    found.append(os.path.relpath(path, base).replace(os.sep, "/"))
    return sorted(found)


def build(ref):
    resolved = _resolve(ref)
    with tempfile.TemporaryDirectory(prefix="t284-") as tree:
        _tree_at(resolved, tree)
        committed, stated_files = _artifact_census(os.path.join(tree, "gpd", "results"))
        naming = _source_census(tree)
        graph = readers.census_of_tree(tree)
    working, working_stated = _artifact_census(os.path.join(ROOT, "gpd", "results"))

    declared = sorted(study for study, _, _ in ELECTROLYTE_STUDIES)
    if declared != naming:
        raise SystemExit(
            "the declared electrolyte-study table and the tree at {} disagree; added {}, "
            "removed {}".format(resolved, sorted(set(naming) - set(declared)),
                                sorted(set(declared) - set(naming)))
        )

    written_by = graph["writtenBy"]
    emits = {}
    for name, writers in written_by.items():
        for writer in writers:
            emits.setdefault(writer, []).append(name)

    study_rows = []
    for study, buffers, evidence in ELECTROLYTE_STUDIES:
        own = sorted(emits.get(study, []))
        study_rows.append({
            "study": study,
            "emits": own,
            "buffersMillimolar": list(buffers),
            "sweepsTheBuffer": len(buffers) > 1,
            "evidence": evidence,
            "studyReadEdges": sum(len(graph["readersOf"].get(f, [])) for f in own),
            "testReadEdges": sum(len(graph["testReadersOf"].get(f, [])) for f in own),
        })

    total_study_edges = sum(len(v) for v in graph["readersOf"].values())
    total_test_edges = sum(len(v) for v in graph["testReadersOf"].values())
    onto_swept = sum(r["studyReadEdges"] for r in study_rows if r["sweepsTheBuffer"])
    onto_single = sum(r["studyReadEdges"] for r in study_rows if not r["sweepsTheBuffer"])
    busiest = max(study_rows, key=lambda r: r["studyReadEdges"])

    document = {
        "task": "T-286",
        "claim": "C-0181",
        "leaf": "A8.2",
        "title": "A regime is a SET of solved states, and the two absences are different values",
        "verificationType": "logical (a design claim about this repository's schema) + in-silico "
                            "(the census is derived from the committed tree at a named ref)",
        "acceptance": "The census that decides the design, and the consumer-side measurement that "
                      "says what a file-granular regime gate can and cannot reach.",
        "maturity": "TRL 1-3. This claim is about SOFTWARE STRUCTURE and PROVENANCE, not about "
                    "the device. No physics is asserted and no object is measured.",
        "units": {
            "concentration": "mM",
            "length": "nm",
            "bias": "V",
            "bandwidth": "Hz",
            "count": "dimensionless",
        },
        "conventions": [
            "a READ EDGE is one (study, result file) pair of tools/result-reader-census.py, "
            "derived at baselineRef and not from the working tree",
            "an ELECTROLYTE STUDY is one whose source names MagnesiumChlorideBuffer",
            "SWEEPS means the study solves every state at more than one molarity",
        ],
        "parameters": {
            "baselineRef": resolved,
            "refArgument": ref,
            "censusTool": "tools/result-reader-census.py",
            "declaredElectrolyteStudies": len(ELECTROLYTE_STUDIES),
        },
        "committedCorpus": committed,
        "committedStatedFiles": stated_files,
        "workingTreeCorpus": working,
        "workingTreeStatedFiles": working_stated,
        "electrolyteStudies": study_rows,
        "consumerSide": {
            "totalStudyReadEdges": total_study_edges,
            "totalTestReadEdges": total_test_edges,
            "edgesOntoElectrolyteFiles": onto_swept + onto_single,
            "edgesOntoSweptFiles": onto_swept,
            "edgesOntoSingleStateFiles": onto_single,
            "busiestElectrolyteFile": busiest["emits"],
            "busiestElectrolyteFileEdges": busiest["studyReadEdges"],
            "sweepingStudies": sum(1 for r in study_rows if r["sweepsTheBuffer"]),
            "singleStateStudies": sum(1 for r in study_rows if not r["sweepsTheBuffer"]),
        },
        "demonstration": [
            {"study": s, "emits": f, "state": state, "declares": why}
            for s, f, state, why in DEMONSTRATED
        ],
        "findings": {
            "theBlockIsNullOnEveryHeadedFile": (
                "At baselineRef the emission header is carried by {withHeader} of {resultFiles} "
                "committed result files and its regime is null on {regimeNotStated} of them, "
                "stated on {regimeStated}. That is CH-0224's own assertion, re-derived: the key "
                "exists everywhere and refuses nothing anywhere."
            ).format(**committed),
            "theArityIsTheDefect": (
                "{swept} of the {total} studies naming MagnesiumChlorideBuffer solve every state "
                "at more than one molarity and {single} fix one. A block that holds one molarity "
                "is therefore null on the majority of the studies whose results a gate exists to "
                "refuse -- and CH-0224's own falsifier partly fires: five single-buffer studies "
                "exist, all at 2.0 mM, and each is a row rather than a type."
            ).format(swept=sum(1 for r in study_rows if r["sweepsTheBuffer"]),
                     total=len(study_rows),
                     single=sum(1 for r in study_rows if not r["sweepsTheBuffer"])),
            "theConsumerSideIsDominatedByOneSweptFile": (
                "{onto} of the corpus's {total} study read edges land on an electrolyte study's "
                "result file, and {swept} of those {onto} land on a file whose buffer is a SET. "
                "{busy} alone carries {busyEdges}. So the coordinate the block could not express "
                "is the coordinate almost every gateable read edge is in."
            ).format(onto=onto_swept + onto_single, total=total_study_edges, swept=onto_swept,
                     busy=", ".join(busiest["emits"]), busyEdges=busiest["studyReadEdges"]),
            "whatTheGateCannotDo": (
                "A file-granular set is a NECESSARY condition and not a sufficient one. It "
                "refuses a consumer asking a file for a state no record of it carries; it cannot "
                "refuse a consumer that picks the WRONG RECORD inside a file whose set contains "
                "the state -- which is the defect CLAUDE.md already records against the busiest "
                "file in this corpus: T-3b carries two solved profiles per (concentration, gap), "
                "one per operating bias, so firstOrNull { c && h } silently takes whichever is "
                "listed first. Only a regime on the RECORD closes that, and it is T-272's sweep "
                "with a wider edit."
            ),
            "whyNotAWiderField": (
                "actuator/TallGapDeviceBStudy solves {0.5, 1.0, 2.0} mM over its tall heights and "
                "{0.5, 2.0} mM over its fold heights. A single Regime with a set-valued buffer "
                "would carry the UNION of both height ranges and would then admit 1.0 mM at a "
                "fold height, which no record of that file carries. A set of two regimes does "
                "not. That is CH-0224's repair 2 falsified by the corpus, at the cost of one grep."
            ),
        },
        "openQuestions": [
            "The 132 call sites that pass null keep passing null, and their JSON does not move. "
            "What moves is the MEANING: null is now `the study has not stated`, which is a "
            "residue to be counted, where the KDoc previously read it as `no solved range`. "
            "Closing the residue is T-272's sweep and is priced there.",
            "Regime.reasonToRefuse compares buffers by EQUALITY and refuses a neutral-layer "
            "source against an electrolyte consumer. That is C-0159's claim and this task does "
            "not touch it; whether a layer pressure and a gap force are `consumed` in one "
            "another's regime, or merely summed, is a question about the coupled model.",
        ],
    }
    return header.with_emission_header(
        rounding.walk(document, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0), "none", regime=[]
    )


def _self_test():
    failures = []
    run = []

    def ok(name, condition):
        run.append(name)
        if not condition:
            failures.append(name)

    ok("T-286 the declared table has 22 studies", len(ELECTROLYTE_STUDIES) == 22)
    ok("T-286 every declared study is listed once",
       len({study for study, _, _ in ELECTROLYTE_STUDIES}) == len(ELECTROLYTE_STUDIES))
    ok("T-286 every declared molarity is positive",
       all(m > 0 for _, buffers, _ in ELECTROLYTE_STUDIES for m in buffers))
    ok("T-286 the tile-thickness false positive is classified as single-buffer",
       dict((s, b) for s, b, _ in ELECTROLYTE_STUDIES)["anchoring/GoldElectrodePzcStudy.kt"]
       == (2.0,))
    ok("T-286 every single-buffer study is at 2.0 mM",
       all(buffers == (2.0,) for _, buffers, _ in ELECTROLYTE_STUDIES if len(buffers) == 1))

    # the artifact census reads FOUR states apart, and an empty list is not a null
    with tempfile.TemporaryDirectory(prefix="t284-selftest-") as directory:
        def write(name, document):
            with open(os.path.join(directory, name), "w", encoding="utf-8") as handle:
                json.dump(document, handle)
        write("a.json", {"emission": {"lattice": "none", "regime": None}})
        write("b.json", {"emission": {"lattice": "none", "regime": []}})
        write("c.json", {"emission": {"lattice": "none", "regime": [{"bufferMillimolar": 2.0}]}})
        write("d.json", {"answer": 1})
        counts, stated = _artifact_census(directory)
    ok("T-286 a null regime is NOT STATED", counts["regimeNotStated"] == 1)
    ok("T-286 an empty list is the no-environment CLAIM", counts["regimeEmptySet"] == 1)
    ok("T-286 a non-empty list is STATED", counts["regimeStated"] == 1)
    ok("T-286 a file with no header is counted apart", counts["withoutHeader"] == 1)
    ok("T-286 the four states partition the files",
       counts["regimeNotStated"] + counts["regimeEmptySet"] + counts["regimeStated"]
       + counts["withoutHeader"] == counts["resultFiles"])
    ok("T-286 a stated file names how many states it carries",
       stated == [{"file": "c.json", "states": 1}])

    ok("T-286 the emitted document rounds at the serialisation boundary, floor ZERO",
       rounding.walk({"r": 1 / 3}, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)["r"] == 0.333333333)
    ok("T-286 a count is an integer and survives the rounding",
       rounding.walk({"n": 22}, 9, rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)["n"] == 22)
    ok("T-286 this emitter's own regime is the empty-set CLAIM, not a null",
       header.with_emission_header({"a": 1}, "none", regime=[])["emission"]["regime"] == [])

    for failure in failures:
        print("FAIL " + failure)
    print("%d of %d self-test(s) failed" % (len(failures), len(run)) if failures else
          "%d self-test(s) pass" % len(run))
    return 1 if failures else 0


def _parser():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD",
                        help="the corpus state to measure; the RESOLVED sha is recorded")
    parser.add_argument("--self-test", action="store_true")
    return parser


def main(argv=None):
    args = _parser().parse_args(argv)
    if args.self_test:
        return _self_test()
    document = build(args.ref)
    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(RESULT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
