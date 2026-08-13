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
#     tools/verify.sh              # test the working tree, including uncommitted work
#     tools/verify.sh --committed  # test HEAD instead, ignoring uncommitted work
#
# Why this exists (task P-10). Several agents run the GPD loop against one checkout at a
# time. `-PbuildDirectory=<dir>` (task P-7) stops them sharing `build/test-results`, but it
# does not go far enough: at four or five concurrent agents the Gradle *project lock* and the
# shared Kotlin daemon still serialise and race, and the run dies with `NoClassDefFoundError`
# on classes nobody touched, `EOFException`, or `NoSuchFileException` on the in-progress
# results binary. Those are harness failures that read exactly like broken tests, and one
# agent was observed losing six consecutive full-suite attempts to them.
#
# A copy of the tree in its own directory has its own project lock, so it runs clean the
# first time. `--committed` additionally answers the question the coordinator actually needs
# before pushing: does *the commit* pass, independently of whatever four agents have
# half-written into the working tree.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
target="${TMPDIR:-/tmp}/plenty-of-room-verify.$$"
mode="${1:-working-tree}"

cleanup() { rm -rf "$target"; }
trap cleanup EXIT

mkdir -p "$target"

if [ "$mode" = "--committed" ]; then
    echo "verifying HEAD ($(git -C "$root" rev-parse --short HEAD)) in $target"
    git -C "$root" archive HEAD | tar -x -C "$target"
else
    echo "verifying the working tree in $target"
    # note: --exclude='build*' would also drop build.gradle.kts
    tar -c -C "$root" \
        --exclude='./build' \
        --exclude='./build-*' \
        --exclude='./.git' \
        --exclude='./.gradle' \
        --exclude='./.kotlin' \
        . | tar -x -C "$target"
fi

cd "$target"
./gradlew test "${@:2}"
