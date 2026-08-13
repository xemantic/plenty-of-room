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
# Shared snapshot helper for `tools/verify.sh` and `tools/study.sh` (task P-12).
#
# Sourced, not executed. Provides:
#
#     snapshot_tree <root> <target> <mode>   mode = --committed | working-tree
#     drop_packages <target> <pkg>...        remove one Kotlin package, main *and* test
#
# Why a snapshot at all: see the header of tools/verify.sh (tasks P-7, P-10).
# Why `drop_packages`: `-PbuildDirectory` isolates build-*/test-results but *not*
# build-*/classes, and a sibling package another agent has left mid-TDD fails
# `compileKotlin` for the whole project — which surfaces as `NoClassDefFoundError`
# on classes that plainly exist, in a package the failing agent never touched.
# Dropping the half-written package from the *copy* leaves the other agent's
# working tree untouched and lets this agent's run proceed.

# Copies the tree at <root> into <target>, excluding build output and VCS/daemon state.
# mode `--committed` archives HEAD instead of the working tree.
snapshot_tree() {
    local root="$1" target="$2" mode="$3"
    mkdir -p "$target"
    if [ "$mode" = "--committed" ]; then
        git -C "$root" archive HEAD | tar -x -C "$target"
    else
        # note: --exclude='build*' would also drop build.gradle.kts
        tar -c -C "$root" \
            --exclude='./build' \
            --exclude='./build-*' \
            --exclude='./.git' \
            --exclude='./.gradle' \
            --exclude='./.kotlin' \
            . | tar -x -C "$target"
    fi
}

# Removes the named Kotlin packages from a snapshot, main sources and tests together.
# A package name is the last component of the Kotlin package, for example `coupling`
# or `brush`.
#
# Both source layouts are tried, because this project uses the FLAT one — the directory
# is `src/main/kotlin/<pkg>` and the `package com.xemantic.nano.plentyofroom.<pkg>`
# declaration carries the qualification. `T-1f` found `--drop` silently matching nothing
# because it assumed the Maven-style `src/main/kotlin/com/xemantic/nano/plentyofroom/<pkg>`;
# a warning went to stderr and `compileKotlin` then failed exactly as if the flag had not
# been passed. Trying both keeps the helper correct if the layout is ever changed.
#
# **This function deletes.** It refuses to run against anything that looks like a real
# checkout — a directory containing `.git` — because it has no dry-run mode and its
# argument is just a path. An agent probing whether `--drop` matched this project's layout
# called it with the checkout root and removed `src/{main,test}/kotlin/brush` from the
# working tree while another agent was writing into it (`S-94`). The guard costs one `[ -e ]`.
drop_packages() {
    local target="$1"; shift
    if [ -e "$target/.git" ]; then
        echo "refusing to drop packages from $target: it is a checkout, not a snapshot" >&2
        return 1
    fi
    local base="com/xemantic/nano/plentyofroom"
    local pkg source_set dir relative
    for pkg in "$@"; do
        local found=0
        for source_set in main test; do
            for relative in "$base/$pkg" "$pkg"; do
                dir="$target/src/$source_set/kotlin/$relative"
                if [ -d "$dir" ]; then
                    rm -rf "$dir"
                    echo "dropped src/$source_set/kotlin/$relative"
                    found=1
                fi
            done
        done
        if [ "$found" = 0 ]; then
            echo "warning: --drop $pkg matched nothing under src/{main,test}/kotlin" >&2
        fi
    done
}
