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
# T-278 -- `tools/study-batch.sh`'s loop run inside a snapshot the CALLER already owns.
#
#     tools/T-278-batch.sh <snapshot dir> <list file> [<log file>]
#
# WHY THIS EXISTS RATHER THAN A FLAG ON `study-batch.sh`. That script takes its own snapshot of the
# working tree, which on a shared checkout is whatever the siblings have half-written at that
# instant: this iteration's run would have inherited a sibling's `HoneycombRasterTurnTies.kt` with
# three unresolved references and failed `compileKotlin` for all 61 studies. `CLAUDE.md`'s cure is
# `drop_files` on a snapshot you own, and a snapshot you own is also a snapshot already compiled --
# one cold build instead of two.
#
# THE COPY-BACK IS STILL SCOPED PER RUN, and that is the whole safety argument, unchanged and
# copied deliberately rather than reinvented: `CLAUDE.md` records a private runner repeating
# exactly the mistake (`S-95`) that `study-batch.sh` had already been fixed for. The snapshot's own
# `gpd/results/` is re-checksummed IMMEDIATELY BEFORE each individual study and exactly the files
# that study changed are copied back. A sibling's fresh output elsewhere in `gpd/results/` is never
# touched.
set -euo pipefail

target="${1:?usage: tools/T-278-batch.sh <snapshot dir> <list file> [<log file>]}"
list="${2:?usage: tools/T-278-batch.sh <snapshot dir> <list file> [<log file>]}"
log="${3:-/dev/stdout}"
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

[ -d "$target/gpd/results" ] || { echo "not a snapshot: $target" >&2; exit 2; }
[ -e "$target/.git" ] && { echo "refusing to run against a real checkout: $target" >&2; exit 2; }

mapfile -t studies < <(grep -v '^[[:space:]]*\(#\|$\)' "$list")
baseline="$target/.results-baseline"

cd "$target"
failed=()
{
    echo "=== T-278 batch: ${#studies[@]} studies in $target"
    date -Is
    for study in "${studies[@]}"; do
        rm -rf "$baseline"; mkdir -p "$baseline"
        cp -a "$target"/gpd/results/. "$baseline"/ 2>/dev/null || true
        echo "=== study $study  $(date -Is)"
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
    echo "=== T-278 batch done $(date -Is)"
    if [ ${#failed[@]} -gt 0 ]; then
        echo "FAILED studies: ${failed[*]}"
    else
        echo "FAILED studies: none"
    fi
} >> "$log" 2>&1
[ ${#failed[@]} -eq 0 ]
