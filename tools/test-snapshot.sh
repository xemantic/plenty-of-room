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
# Self-test for tools/snapshot.sh (task P-16).
#
#     tools/test-snapshot.sh
#
# `drop_packages` and `drop_files` DELETE, and one of them has already removed a package from
# a live working tree (`S-94`). Their guards are the only thing standing between a mistyped
# argument and another agent's unfinished work, so they get executable tests of their own —
# the same rule the Kotlin side runs under, applied to the harness that protects it.
#
# The fixtures are throwaway directories under $TMPDIR; nothing here touches the checkout.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$root/tools/snapshot.sh"

failures=0
checks=0

check() {
    local label="$1" condition="$2"
    checks=$((checks + 1))
    if eval "$condition"; then
        echo "  ok   $label"
    else
        echo "  FAIL $label   [$condition]"
        failures=$((failures + 1))
    fi
}

# A fixture in this project's FLAT source layout, plus one file in the Maven-style layout so
# that the two-layout search in both drop helpers is exercised.
fixture() {
    local dir="$1"
    rm -rf "$dir"
    mkdir -p "$dir/src/main/kotlin/coupling" \
             "$dir/src/test/kotlin/coupling" \
             "$dir/src/main/kotlin/anchoring" \
             "$dir/src/main/kotlin/com/xemantic/nano/plentyofroom/legacy" \
             "$dir/gpd/results"
    echo "package coupling"  > "$dir/src/main/kotlin/coupling/Coupling.kt"
    echo "package coupling"  > "$dir/src/main/kotlin/coupling/Placement.kt"
    echo "package coupling"  > "$dir/src/test/kotlin/coupling/CouplingTest.kt"
    echo "package anchoring" > "$dir/src/main/kotlin/anchoring/Anchoring.kt"
    echo "package legacy"    > "$dir/src/main/kotlin/com/xemantic/nano/plentyofroom/legacy/Legacy.kt"
    echo '{}'               > "$dir/gpd/results/T-0-fixture.json"
}

target="${TMPDIR:-/tmp}/plenty-of-room-snapshot-test.$$"
trap 'rm -rf "$target" "$target.checkout"' EXIT

echo "drop_files removes one file and leaves its siblings"
fixture "$target"
drop_files "$target" src/test/kotlin/coupling/CouplingTest.kt > /dev/null
check "the named test file is gone"     '[ ! -e "$target/src/test/kotlin/coupling/CouplingTest.kt" ]'
check "the package's main sources stay" '[ -e "$target/src/main/kotlin/coupling/Coupling.kt" ]'
check "the sibling package stays"       '[ -e "$target/src/main/kotlin/anchoring/Anchoring.kt" ]'

echo "drop_files takes several paths at once"
fixture "$target"
drop_files "$target" src/main/kotlin/coupling/Placement.kt \
                     src/main/kotlin/com/xemantic/nano/plentyofroom/legacy/Legacy.kt > /dev/null
check "the first path is gone"  '[ ! -e "$target/src/main/kotlin/coupling/Placement.kt" ]'
check "the second path is gone" '[ ! -e "$target/src/main/kotlin/com/xemantic/nano/plentyofroom/legacy/Legacy.kt" ]'
check "the untouched sibling stays" '[ -e "$target/src/main/kotlin/coupling/Coupling.kt" ]'

echo "drop_files refuses a checkout, an escape and an absolute path"
fixture "$target"
mkdir -p "$target.checkout/.git" "$target.checkout/src"
echo "package coupling" > "$target.checkout/src/Coupling.kt"
check "a directory containing .git is refused" \
      '! drop_files "$target.checkout" src/Coupling.kt > /dev/null 2>&1'
check "and nothing was deleted from it" '[ -e "$target.checkout/src/Coupling.kt" ]'
check "a .. escape is refused"  '! drop_files "$target" ../escape.kt > /dev/null 2>&1'
check "an absolute path is refused" '! drop_files "$target" /etc/hosts > /dev/null 2>&1'
check "the fixture is intact"   '[ -e "$target/src/main/kotlin/coupling/Coupling.kt" ]'

echo "drop_files warns rather than failing when a path matches nothing"
fixture "$target"
check "an absent path is not an error" \
      'drop_files "$target" src/main/kotlin/coupling/Absent.kt > /dev/null 2>&1'

echo "drop_files refuses to delete a directory"
fixture "$target"
check "a directory argument is refused" \
      '! drop_files "$target" src/main/kotlin/coupling > /dev/null 2>&1'
check "the directory is intact" '[ -d "$target/src/main/kotlin/coupling" ]'

echo "drop_packages still removes main and test together, in both layouts"
fixture "$target"
drop_packages "$target" coupling legacy > /dev/null
check "flat main is gone"        '[ ! -d "$target/src/main/kotlin/coupling" ]'
check "flat test is gone"        '[ ! -d "$target/src/test/kotlin/coupling" ]'
check "maven-style main is gone" '[ ! -d "$target/src/main/kotlin/com/xemantic/nano/plentyofroom/legacy" ]'
check "the sibling package stays" '[ -e "$target/src/main/kotlin/anchoring/Anchoring.kt" ]'

echo "drop_packages still refuses a checkout"
check "a directory containing .git is refused" \
      '! drop_packages "$target.checkout" coupling > /dev/null 2>&1'

echo
if [ "$failures" = 0 ]; then
    echo "$checks checks passed"
else
    echo "$failures of $checks checks FAILED"
    exit 1
fi
