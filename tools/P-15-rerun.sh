#!/usr/bin/env bash
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
# The re-run-and-diff half of task P-15.
#
#     tools/P-15-rerun.sh <tree> [study...]
#
# `P-15` repairs `bracketedRoot`, and `C-0019` states the condition for closing it: the routine is
# consumed by `C-0003`, `C-0011` and `C-0016`, so the repair is not complete until every result
# file that consumes it has been re-run and diffed. A solver defect that is invisible in the
# residual cannot be detected by inspecting an emitted file, so the diff *is* the measurement.
#
# The baseline is `HEAD`'s committed `gpd/results/`, which is why only the repaired tree is run:
# re-running the broken one would reproduce what is already in git, at twice the compute.
#
# <tree> must be a snapshot carrying the repair and NOTHING ELSE on top of HEAD. Running this
# against the working tree would fold three concurrent agents' in-flight edits into the diff and
# measure something other than the repair.
#
# Every emitted file is compared byte-for-byte. That is meaningful here only because the project
# already rounds the whole result tree at the serialisation boundary — `Double` results are not
# reproducible across runs of the same JVM otherwise, because the JIT compiles a hot reduction
# part-way through and moves the last ulp.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [ $# -lt 1 ]; then
    echo "usage: tools/P-15-rerun.sh <tree> [study...]" >&2
    exit 2
fi

tree="$1"; shift

# Every study whose result depends, directly or through a model, on `bracketedRoot`. Ordered
# cheapest first so that a moved number is found early rather than after the 33-minute sweep.
studies=(
    window.DesignWindowStudyKt
    window.WindowResynthesisStudyKt
    material.PegMaterialStudyKt
    material.SolventQualitySaltStudyKt
    brush.FluctuationCorrectionStudyKt
    coupling.OutputCouplingStudyKt
    anchoring.ZeroBiasRestingPositionStudyKt
    anchoring.TwoSidedCouplingStudyKt
    structure.TilePositionalVarianceStudyKt
    brush.CrossoverLayerStudyKt
    actuator.StrokeAndBlockingForceStudyKt
    actuator.MaximumUsableBiasStudyKt
    brush.ScfDensityProfileStudyKt
)
if [ $# -gt 0 ]; then
    studies=("$@")
fi

moved=0
unchanged=0

# Which files a study wrote is established by MTIME, not by content. A study that rewrites a file
# with byte-identical content is the *interesting* case here — it is the "no number moved" verdict
# — and a content comparison against the pre-run state reports it as "wrote nothing", which is the
# same string a study that failed to emit anything produces. `tools/study.sh` compares content
# because it runs in a tree other agents write into; this tree is static, so mtime is both
# available and the right question.
for study in "${studies[@]}"; do
    echo "=== $study"
    stamp="$tree/.P-15-stamp"
    : > "$stamp"
    touch -d '1 second ago' "$stamp"

    if ! (cd "$tree" && ./gradlew --quiet study -Pstudy="$study" > /dev/null 2>&1); then
        echo "  RUN FAILED: $study"
        continue
    fi

    wrote=0
    for file in "$tree"/gpd/results/*; do
        [ -f "$file" ] || continue
        [ "$file" -nt "$stamp" ] || continue
        name="$(basename "$file")"
        wrote=$((wrote + 1))
        if git -C "$root" cat-file -e "HEAD:gpd/results/$name" 2>/dev/null; then
            if git -C "$root" show "HEAD:gpd/results/$name" | cmp -s - "$file"; then
                echo "  unchanged  $name"
                unchanged=$((unchanged + 1))
            else
                echo "  MOVED      $name"
                git -C "$root" show "HEAD:gpd/results/$name" > "$tree/.P-15-head-$name"
                diff <(python3 -m json.tool "$tree/.P-15-head-$name") \
                     <(python3 -m json.tool "$file") \
                     > "$tree/.P-15-diff-$name" || true
                echo "    $(grep -c '^[<>]' "$tree/.P-15-diff-$name") changed JSON lines" \
                     "-> $tree/.P-15-diff-$name"
                moved=$((moved + 1))
            fi
        else
            echo "  NEW        $name (not in HEAD)"
        fi
    done
    [ "$wrote" = 0 ] && echo "  WROTE NOTHING (the study emitted no result file at all)"
done

echo
echo "P-15 re-run: $unchanged result files unchanged, $moved moved"
[ "$moved" = 0 ]
