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
# Runs one study entry point on an isolated copy of the working tree, then copies the
# result JSON it emitted back into this checkout.
#
#     tools/study.sh brush.CrossoverLayerStudyKt
#     tools/study.sh --drop coupling window.DesignWindowStudyKt
#     tools/study.sh --keep brush.ScfDensityProfileStudyKt   # leave the copy for inspection
#
# Why this exists (task P-12). `-PbuildDirectory=<dir>` isolates `build-*/test-results` but
# **not** `build-*/classes`: a concurrent agent's Gradle invocation can delete another's
# compiled classes mid-run, and a multi-minute study then dies with `ClassNotFoundException`
# on its own types, having burnt the whole run. `C-0015` lost a study that way, and `T-1d`'s
# profile sweep is 33 minutes of exposure. A copy of the tree has its own project lock and
# its own classes directory, so a long study cannot be shot down by a sibling agent.
#
# Only files under gpd/results/ are copied back, and only the ones **this study wrote** —
# established by checksumming the snapshot's own `gpd/results/` before the run and copying
# back exactly the files the run changed. Comparing the snapshot against the *checkout*
# instead is not the same thing and is not safe: a concurrent agent that emitted its own
# result while this study ran would have it reverted to the version in this snapshot, which
# is what happened to `gpd/results/T-13-...json` the first time this script was used (`S-95`).
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$root/tools/snapshot.sh"

target="${TMPDIR:-/tmp}/plenty-of-room-study.$$"
drops=()
keep=0

while [ $# -gt 0 ]; do
    case "$1" in
        --drop) drops+=("$2"); shift 2 ;;
        --keep) keep=1; shift ;;
        *) break ;;
    esac
done

if [ $# -lt 1 ]; then
    echo "usage: tools/study.sh [--drop <pkg>]... [--keep] <study main class> [gradle args...]" >&2
    exit 2
fi

study="$1"; shift

cleanup() {
    if [ "$keep" = 1 ]; then
        echo "copy kept at $target"
    else
        rm -rf "$target"
    fi
}
trap cleanup EXIT

echo "running study $study in $target"
snapshot_tree "$root" "$target" "working-tree"
if [ ${#drops[@]} -gt 0 ]; then
    drop_packages "$target" "${drops[@]}"
fi

# Baseline the snapshot's own results, so that what the study wrote can be told apart from
# what the rest of the checkout has been doing meanwhile.
baseline="$target/.results-baseline"
mkdir -p "$baseline"
cp -a "$target"/gpd/results/. "$baseline"/ 2>/dev/null || true

cd "$target"
./gradlew study -Pstudy="$study" "$@"

# Copy back only the files this run changed, and say which they were.
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
if [ "$copied" = 0 ]; then
    echo "no result file changed"
fi
