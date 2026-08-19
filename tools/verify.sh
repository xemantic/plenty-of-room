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
# Runs an authoritative `./gradlew test` on an isolated copy of the working tree.
#
#     tools/verify.sh                    # test the working tree, including uncommitted work
#     tools/verify.sh --committed        # test HEAD instead, ignoring uncommitted work
#     tools/verify.sh --drop coupling    # test without a sibling package left mid-TDD
#     tools/verify.sh --drop-file src/test/kotlin/coupling/PlacementTest.kt   # one file only
#     tools/verify.sh --no-checks        # Gradle only, without the harness's own tests
#
# Options may be combined and repeated; everything after them is passed to Gradle.
#
# Why this exists (task P-10). Several agents run the GPD loop against one checkout at a
# time. `-PbuildDirectory=<dir>` (task P-7) stops them sharing `build/test-results`, but it
# does not go far enough: at four or five concurrent agents the Gradle *project lock* and the
# shared Kotlin daemon still serialise and race, and the run dies with `NoClassDefFoundError`
# on classes nobody touched, `EOFException`, or `NoSuchFileException` on the in-progress
# results binary. Those are harness failures that read exactly like broken tests, and one
# agent was observed losing fourteen consecutive full-suite attempts to them.
#
# A copy of the tree in its own directory has its own project lock, so it runs clean the
# first time. `--committed` additionally answers the question the coordinator actually needs
# before pushing: does *the commit* pass, independently of whatever four agents have
# half-written into the working tree.
#
# `--drop <pkg>` (task P-12) covers the third cause of `NoClassDefFoundError`: a sibling
# package another agent has left mid-TDD, which fails `compileKotlin` for the whole project.
# Dropping it from the copy tests everything else; it is a *diagnosis* of somebody else's
# unfinished work, so name the dropped package whenever you report such a run.
#
# **Prefer `--drop-file` (task P-16).** A package drop removes the package's *main* sources
# too, so it is unusable whenever your own package imports the broken one — `coupling` imports
# `anchoring`, and `--drop anchoring` turns one half-written file into eighty broken
# references. Name the single file that fails to compile instead; it is smaller, safer, and
# it is what the common case (a sibling's unfinished *test*) actually needs.
#
# The result-reader census (task P-22) runs after Gradle. It is here rather than in
# `build.gradle.kts` for the reason that file already states about `testHarness`: a task hung
# off `test` must touch nothing under `src/`, so that a `--drop`/`--drop-file` on the snapshot
# cannot make it fail. The census *reads* `src/`, by construction — so it hangs off the script
# that knows whether a drop was requested, and it is skipped when one was. `--no-checks` skips
# it by hand. It runs *after* the suite so a failure never hides a Gradle result, and it is
# under a second.
#
# `tools/check-markdown-tables.py` (`P-23`) is here too, and it was not at first: **a snapshot
# has no `.git`**, so the checker's `git ls-files` fails, its fallback walked the tree with a
# `./` prefix, and that prefix defeated its own `third-party/` exclusion — the one directory
# whose table defect must be preserved. Run inside a snapshot it reported a defect it could
# never be rid of, and a gate that can never come clean is not a gate, so `P-22` removed it and
# said why. The defect was in the checker, not in this script: `is_excluded` now normalises the
# path and the fallback no longer emits the prefix, both asserted as tests, and the gate is
# clean in a real `.git`-less snapshot. Restored here by `P-23` on that evidence.
#
# The three SELF-tests are wired in `build.gradle.kts` instead, because they read only
# fixtures: `tools/test-snapshot.sh` since `P-16`, and `tools/test-trace-answers.py` and
# `tools/test-check-markdown-tables.py` since `P-22`.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$root/tools/snapshot.sh"

target="${TMPDIR:-/tmp}/plenty-of-room-verify.$$"
mode="working-tree"
drops=()
dropped_files=()
checks="yes"

while [ $# -gt 0 ]; do
    case "$1" in
        --committed) mode="--committed"; shift ;;
        --drop) drops+=("$2"); shift 2 ;;
        --drop-file) dropped_files+=("$2"); shift 2 ;;
        --no-checks) checks="no"; shift ;;
        *) break ;;
    esac
done

cleanup() { rm -rf "$target"; }
trap cleanup EXIT

if [ "$mode" = "--committed" ]; then
    echo "verifying HEAD ($(git -C "$root" rev-parse --short HEAD)) in $target"
else
    echo "verifying the working tree in $target"
fi

snapshot_tree "$root" "$target" "$mode"
if [ ${#drops[@]} -gt 0 ]; then
    drop_packages "$target" "${drops[@]}"
fi
if [ ${#dropped_files[@]} -gt 0 ]; then
    drop_files "$target" "${dropped_files[@]}"
fi
if [ ${#drops[@]} -gt 0 ] || [ ${#dropped_files[@]} -gt 0 ]; then
    checks="no"
fi

cd "$target"
./gradlew test "$@"

if [ "$checks" = "yes" ]; then
    echo
    echo "--- the result-reader census over gpd/results/ (P-22) ---"
    echo "    skip with: tools/verify.sh --no-checks"
    tools/test-result-reader-census.py
    tools/result-reader-census.py --check
    echo
    echo "--- every Markdown table renders (P-23) ---"
    tools/check-markdown-tables.py
    echo
    echo "--- every relative link in gpd/ resolves (T-203) ---"
    tools/check-corpus-links.py --selftest > /dev/null
    tools/check-corpus-links.py
    echo
    echo "--- every String.format call balances its conversions (T-207) ---"
    tools/check-kotlin-format-strings.py
    echo
    echo "--- every challenge is in its own index (P-26) ---"
    tools/check-challenge-index.py --selftest > /dev/null
    tools/check-challenge-index.py
    echo
    echo "--- no committed result file carries a raw format conversion (T-208) ---"
    tools/check-result-file-hygiene.py
    echo
    echo "--- the two audits that are NOT gates: departure precision, saturated statistics ---"
    tools/check-result-file-hygiene.py --departures --saturated
fi
