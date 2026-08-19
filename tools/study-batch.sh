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
# Runs SEVERAL study entry points on ONE isolated copy of the working tree (task T-212).
#
#     tools/study-batch.sh anchoring.HingeLineCensusStudyKt anchoring.SeamWeaveStudyKt
#     tools/study-batch.sh --keep --list gpd/scratch/order.txt
#
# WHY THIS EXISTS. `tools/study.sh` snapshots the tree PER STUDY, which is exactly right when a
# study is a one-off: the snapshot is what stops a sibling agent's Gradle invocation deleting the
# run's own classes half way through (`P-12`). A RE-EMISSION SWEEP is not a one-off — `T-212` owes
# thirty studies — and thirty snapshots is thirty cold Gradle builds of the same unchanged tree.
# One snapshot, one build, thirty runs.
#
# THE COPY-BACK IS STILL SCOPED PER RUN, and that is the whole safety argument. `CLAUDE.md` records
# that a snapshot is a view of the PAST of every other file in the tree, so copying back
# "everything that differs" reverts a concurrent agent's fresh output — it bit twice in one
# iteration (`S-95`, and again in a private runner that repeated the mistake after the fix). This
# script therefore re-checksums the snapshot's own `gpd/results/` IMMEDIATELY BEFORE each
# individual study and copies back exactly the files that study changed. Batching changes how many
# times the tree is copied; it does not change what is copied back.
#
# The order is the caller's. It is meant to be `tools/reemission-order.py`'s output over the whole
# set at once (`CH-0131`: a re-emission sweep is a topological sort, not a list).
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$root/tools/snapshot.sh"

target="${TMPDIR:-/tmp}/plenty-of-room-batch.$$"
keep=0
studies=()

while [ $# -gt 0 ]; do
    case "$1" in
        --keep) keep=1; shift ;;
        --list) mapfile -t studies < <(grep -v '^[[:space:]]*\(#\|$\)' "$2"); shift 2 ;;
        *) studies+=("$1"); shift ;;
    esac
done

if [ ${#studies[@]} -lt 1 ]; then
    echo "usage: tools/study-batch.sh [--keep] [--list <file>] <study main class>..." >&2
    exit 2
fi

cleanup() {
    if [ "$keep" = 1 ]; then
        echo "copy kept at $target"
    else
        rm -rf "$target"
    fi
}
trap cleanup EXIT

echo "batch of ${#studies[@]} studies in $target"
snapshot_tree "$root" "$target" "working-tree"

baseline="$target/.results-baseline"
mkdir -p "$baseline"

cd "$target"
failed=()
for study in "${studies[@]}"; do
    # Re-baseline immediately before THIS run: the copy-back must be scoped to what this run wrote.
    rm -rf "$baseline"; mkdir -p "$baseline"
    cp -a "$target"/gpd/results/. "$baseline"/ 2>/dev/null || true

    echo "=== study $study"
    if ! ./gradlew study -Pstudy="$study" </dev/null; then
        echo "!!! FAILED $study"
        failed+=("$study")
        continue
    fi

    copied=0
    for file in "$target"/gpd/results/*; do
        [ -f "$file" ] || continue
        name="$(basename "$file")"
        if ! cmp -s "$file" "$baseline/$name"; then
            cp "$file" "$root/gpd/results/$name"
            echo "emitted gpd/results/$name"
            copied=$((copied + 1))
        fi
    done
    [ "$copied" = 0 ] && echo "no result file changed by $study"
done

if [ ${#failed[@]} -gt 0 ]; then
    echo "FAILED studies: ${failed[*]}" >&2
    exit 1
fi
