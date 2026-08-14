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
#     drop_files    <target> <path>...       remove named files, nothing else
#
# Why a snapshot at all: see the header of tools/verify.sh (tasks P-7, P-10).
# Why `drop_packages`: `-PbuildDirectory` isolates build-*/test-results but *not*
# build-*/classes, and a sibling package another agent has left mid-TDD fails
# `compileKotlin` for the whole project — which surfaces as `NoClassDefFoundError`
# on classes that plainly exist, in a package the failing agent never touched.
# Dropping the half-written package from the *copy* leaves the other agent's
# working tree untouched and lets this agent's run proceed.
#
# Why `drop_files` as well (task P-16): a *package* is the wrong granularity whenever
# your own work depends on the broken one. `coupling` imports six symbols from
# `anchoring`, so `--drop anchoring` turns one half-written file into eighty broken
# references, and `C-0027` lost time to exactly that. Removing the single file that
# fails to compile is both smaller and safer, and it is what an agent actually wants
# in the common case, which is one sibling's unfinished *test*.
#
# Both helpers are covered by tools/test-snapshot.sh — they delete, and one of them has
# already deleted from a live checkout (`S-94`).

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

# Removes named files from a snapshot, and nothing else. Paths are relative to the snapshot
# root, exactly as they would be written in a `git status` line — for example
# `src/test/kotlin/structure/TileEdgeTest.kt`.
#
# This is `drop_packages` at the granularity the problem actually has. A package drop takes
# the package's **main** sources with it, so it cannot be used when your own package imports
# the broken one; `--drop anchoring` breaks `coupling`, and dropping both then breaks whatever
# imports *those*. One file is the smallest thing that can fail `compileKotlin`, and it is
# usually the whole of what a sibling has left mid-TDD.
#
# **This function deletes**, so it carries the same checkout guard as `drop_packages`, plus
# two more that the package form does not need because a package name cannot express them:
# a path may not be absolute and may not climb out of the snapshot with `..`. A directory
# argument is refused rather than silently recursing — that is what `drop_packages` is for,
# and a `rm -rf` reached by a typo is exactly the failure `S-94` already produced once.
#
# A path that matches nothing is a warning, not an error: an agent naming the file it believes
# is broken should not have its verification run aborted because the sibling fixed it first.
drop_files() {
    local target="$1"; shift
    if [ -e "$target/.git" ]; then
        echo "refusing to drop files from $target: it is a checkout, not a snapshot" >&2
        return 1
    fi
    local path file
    for path in "$@"; do
        case "$path" in
            /*)      echo "refusing absolute path: $path" >&2; return 1 ;;
            ..|../*|*/../*|*/..)
                     echo "refusing path outside the snapshot: $path" >&2; return 1 ;;
        esac
        file="$target/$path"
        if [ -d "$file" ]; then
            echo "refusing to drop a directory: $path (use --drop <pkg> for a whole package)" >&2
            return 1
        fi
        if [ -f "$file" ]; then
            rm -f "$file"
            echo "dropped $path"
        else
            echo "warning: --drop-file $path matched nothing" >&2
        fi
    done
}
