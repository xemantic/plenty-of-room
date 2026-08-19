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
# T-215 — draw an ensemble of runs of ONE study, each into its own output file.
#
#     tools/T-215-ensemble.sh <snapshot dir> <label> <count> <study main class> <out dir> \
#         [result path, default T-129's]
#
# Why a script rather than two `tools/study.sh` invocations: `study.sh` snapshots afresh
# every time (a cold compile per member), and — the trap this task fell into — two runs
# started against ONE snapshot write the SAME `gpd/results/` path, so the second silently
# overwrites the first and a copy taken while a run is still in flight returns the
# snapshot's own *input* copy, which is byte-identical to the committed file and reads as
# a perfect reproduction. Runs here are strictly SEQUENTIAL within a snapshot and the
# emitted file is moved out before the next run starts.
set -euo pipefail

snapshot="$1"; label="$2"; count="$3"; study="$4"; outdir="$5"
result="${6:-gpd/results/T-129-range-robust-placement.json}"
mkdir -p "$outdir"

for i in $(seq 1 "$count"); do
    rm -f "$snapshot/$result"
    ( cd "$snapshot" && ./gradlew --console=plain -PbuildDirectory=build-s \
        study -Pstudy="$study" ) > "$outdir/$label$i.log" 2>&1
    mv "$snapshot/$result" "$outdir/$label$i.json"
    echo "$label$i emitted"
done
