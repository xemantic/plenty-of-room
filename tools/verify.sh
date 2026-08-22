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
# `tools/trace-answers.py` (`T-277`) was the ONLY retained checker not run here, and it is the only
# one that reads the two outward-facing documents BY NAME -- so the numeric, task-status,
# challenge-status and self-consistency checks ran only when somebody remembered. `C-0171` measured
# that and it is `C-0078`'s own finding about itself: a check nobody remembers to ask for is not a
# check. It already exits with its defect count, so wiring it cost one line per document.
#
# `tools/check-cold-start-note.py` (`P-29`) is the smallest gate here and it exists because the
# convention was written down TWICE and broke a third time. `P-28` retitled `## Start here -- the
# state after iteration N` from 26 to 38 and recorded that a mis-titled note is worse than a stale
# one; four iterations later it read 38 again. A date in a heading has no owner: it is not a number
# a tracer can attribute, not a status word a queue checker reads, and not a link. `CLAUDE.md`'s
# *a convention is not a mechanism*, fifth instance.
#
# # `tools/T-278-emitter-rounding-census.py` (`T-278`, `C-0174`) gates the SOURCE half only: every study
# that writes a committed result file must write it through a rounding function. `CH-0223` measured
# seven that did not, carrying 41 297 of the corpus's 41 369 over-precise numeric leaves -- 99.83 % --
# because `C-0138` counted rounding IMPLEMENTATIONS (six, all correctly delegated) where the number
# that decides whether the rule holds is the count of WRITES. Wired here with its reading recorded,
# per `C-0158`: at the commit that lands it the check is **0 of 130 emitting studies**, exit 0.
#
# The ARTIFACT half is deliberately not gated. Its residue is 15 over-precise leaves in 5 files, every
# one written by a Python emitter in `tools/` that no rule in the Kotlin serialisation layer reaches;
# gating it would fail the build on files this tree cannot yet repair, and `C-0083`'s rule is that a
# gate which cannot come clean is not a gate. The census prints the residue beside the gated half.
#
# # `tools/check-queue-vocabulary.py` (`P-29`) runs immediately BEFORE the tracer, and the order is
# the point: the tracer compares the two deliverables against the queue, so a queue row whose
# verdict the tracer misreads makes the tracer's own verdict meaningless. Iteration 41 coined
# `**SECOND DELIVERABLE ANSWERED**` on `T-9`; `queue_status` read the row CLOSED, and an OPEN task
# left the register. Its self-test runs first for the same reason the census's does.
#
# AND THE TRACER COULD NOT FAIL WHEN IT WAS WIRED HERE. `main` accumulated its defect count into a
# dead local and ended `return 0`, from iteration 12 -- so `C-0173` added two lines under
# `set -euo pipefail` on the stated ground that the tool *"already returned its defect count"*,
# which is true of the inner `check_document` and false of the function `sys.exit` reads. Repaired
# by `C-0177` (`CH-0231`), which is why this block is worth more than it was yesterday.
#
# `tools/check-corpus-identifiers.py` (`T-273`) closes the one cross-reference class the other two
# could not reach. `C-0083` gates a claim's FILENAME and `check-corpus-links.py` a relative LINK; a
# bare `CH-0133` in a sentence is neither, and `T-268` cited two numbers that had been reserved in
# iteration 24 and never filed. It can be a gate only because the corpus's legitimate mentions of a
# non-existent identifier are exactly two kinds -- released, and declared absent -- and the
# exemption is per (document, identifier) so that the sentence recording the defect does not fire.
#
# `tools/check-entry-points.py` (`P-28`) is here for the same reason as the census: it reads
# `src/`, so it must be skipped when a drop was requested. It answers the one question a cold
# session asks first — *how do I run this?* — and it was written after the table decayed to 99 rows
# against 122 emitting studies, three of the missing being the emitters of `P-27`'s own eight red
# result files. Its self-test runs first because its first draft reported two CORRECT rows as
# defects: the write path is the binding that reaches `.writeText`, not the last path in the file.
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
    echo "--- the cold-start heading is not behind the journal (P-29) ---"
    tools/check-cold-start-note.py --selftest > /dev/null
    tools/check-cold-start-note.py
    echo
    echo "--- every emitting study writes through a rounding function (T-278) ---"
    tools/T-278-emitter-rounding-census.py --self-test > /dev/null
    tools/T-278-emitter-rounding-census.py --check
    echo
    echo "--- the queue's own status vocabulary is closed and agrees with the reader (P-29) ---"
    tools/check-queue-vocabulary.py --selftest > /dev/null
    tools/check-queue-vocabulary.py
    echo
    echo "--- both deliverables trace to the corpus and agree with the queue (T-277) ---"
    tools/trace-answers.py
    tools/trace-answers.py --answers DECISIONS-FOR-NDI.md
    echo
    echo "--- every claim and challenge IDENTIFIER cited in the corpus exists (T-273) ---"
    tools/check-corpus-identifiers.py --selftest > /dev/null
    tools/check-corpus-identifiers.py
    echo
    echo "--- every study that writes a committed result file has an Entry points row (P-28) ---"
    tools/check-entry-points.py --selftest > /dev/null
    tools/check-entry-points.py
    echo
    echo "--- every committed result file has a typed input handle (T-272) ---"
    tools/T-272-emit-result-inputs.py --selftest > /dev/null
    tools/T-272-emit-result-inputs.py --check
    echo
    # A GATE ON THE REGRESSION STATE ONLY, and that is the whole design. `T-272`'s sweep is a
    # measured seven hours, so a partial delivery is expected and DECLARED-NOT-EMITTED is a
    # residue rather than a defect -- the plain run prints all four states and their counts.
    # What must never be silent is the other one-sided state: a result file carrying a header its
    # study no longer declares is a source that was reverted under a committed artifact, which is
    # `C-0101`'s `T-157` staleness with the arrow reversed.
    echo "--- no result file carries an emission header its study no longer declares (T-272) ---"
    tools/T-272-header-census.py --selftest > /dev/null
    tools/T-272-header-census.py --check
    echo
    echo "--- no main source reads a result file by path rather than by handle (T-272) ---"
    tools/T-272-header-census.py --reads
    echo
    echo "--- every challenge is in its own index (P-26) ---"
    tools/check-challenge-index.py --selftest > /dev/null
    tools/check-challenge-index.py
    echo
    echo "--- no committed result file carries a raw format conversion (T-208) ---"
    tools/check-result-file-hygiene.py
    echo
    echo "--- every departure is emitted at two significant digits (T-212) ---"
    tools/check-result-file-hygiene.py --departures
    echo
    echo "--- the audit that is NOT a gate: saturated statistics (T-213) ---"
    tools/check-result-file-hygiene.py --saturated
    echo
    # `T-249`/`C-0153` raised it as an AUDIT, because the corpus read 748 tokens in 48 files and
    # `C-0083`'s rule is that a gate which cannot come clean is not a gate. `T-250`/`C-0156` swept
    # the 47 studies that carried them, in one `tools/reemission-order.py` topological sort, and
    # promoted the line to a GATE. No `head` here: a gate must show what it found.
    #
    # `P-27`/`C-0158`: this comment used to certify a READING -- "so the line reads 0 in 0" -- and
    # that reading was false at the very commit that wrote it. `git archive 49b1a01 | tar -x` and
    # running THAT tree's own copy of the checker exits 1 on 69 tokens in 8 files, so `verify.sh`
    # was never run after the gate line was added: 62 of the 69 were artifacts the sweep repaired
    # in source and did not re-emit, and 7 were source-side call sites at four shapes the sweep's
    # mechanical rule cannot match. `CLAUDE.md`'s own rule applies -- a guard's justification is a
    # statement about a STATE, and it expires when the state moves -- so the comment now states
    # the RULE and the line below states the reading, which is the only place a reading belongs.
    #
    # Note which corpus is read: this script scans the SNAPSHOT, so the default mode reads the
    # working tree and `--committed` reads `HEAD`. Only the second answers "does the commit come
    # clean", which is the control `P-27` recommends running before any push that moves a result
    # file.
    echo "--- no result file in this snapshot carries an unrounded number inside PROSE (T-250) ---"
    tools/check-result-file-hygiene.py --prose
fi
