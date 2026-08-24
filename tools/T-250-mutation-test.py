#!/usr/bin/env python3
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
"""Mutation coverage of the `T-250` PROMOTION -- the two objects `T-249` did not have.

`tools/T-249-mutation-test.py` mutates the `--prose` PREDICATE, and it still does; nothing here
replaces it.  What `T-250` adds is a **gate**, and a gate is two further decisions that the
predicate's tests cannot reach:

* the **exit policy** -- `prose_exit_code`, which is what "promoted from the audit list to the
  gate list" actually means.  Restoring the audit-only behaviour changes no predicate and no
  number; it changes one `return`.
* the **allowlist** -- and specifically that it is keyed on `(file, literal)` rather than on
  `file`.  A per-file allowlist passes any test written about the file it exempts, and fails
  only on the token it should NOT have exempted.

`C-0127`'s standard is that restoring the old behaviour must fail a **named** test, and `C-0150`
raised it: the tests must bite in both directions, so half of these mutations widen the
gate and half narrow it.

    tools/T-250-mutation-test.py
"""

import importlib.util
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))


def _module():
    spec = importlib.util.spec_from_file_location(
        "hygiene", os.path.join(HERE, "check-result-file-hygiene.py")
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def exit_mutations(module):
    """`(name, direction, policy)` -- each written the way a plausible author would have."""

    def audit_only(found, census=False):
        # `T-249`'s behaviour: report and never fail the build.
        return 0

    def gates_the_census_too(found, census=False):
        return 1 if found else 0

    def always_fails(found, census=False):
        return 0 if census else 1

    def gates_on_the_count_of_files(found, census=False):
        # a plausible slip: a single defect in a single file rounds to "nothing to report"
        return 1 if len(found) > 1 and not census else 0

    return [
        ("the AUDIT-ONLY exit policy of T-249 restored", "narrowing", audit_only),
        ("the gate applied under --census as well", "widening", gates_the_census_too),
        ("any run fails, defects or not", "widening", always_fails),
        ("a single defect treated as noise", "narrowing", gates_on_the_count_of_files),
    ]


def allowlist_mutations(module):
    """`(name, direction, predicate)` with the signature of `prose_allowlisted`."""
    allowlist = module.PROSE_ALLOWLIST

    def per_file(path, literal, _=None):
        # the shape `--conversions` uses: exempt the whole FILE
        return os.path.basename(path) in allowlist

    def any_file(path, literal, _=None):
        # exempt the token wherever it appears
        return any(literal in tokens for tokens in allowlist.values())

    def no_allowlist(path, literal, _=None):
        return False

    def shared_with_conversions(path, literal, _=None):
        return os.path.basename(path) in module.ALLOWLIST or module.prose_allowlisted(
            path, literal)

    return [
        ("a per-FILE allowlist, the shape --conversions uses", "widening", per_file),
        ("the token exempted in any file", "widening", any_file),
        ("no allowlist at all", "narrowing", no_allowlist),
        ("the conversions allowlist reused for prose", "widening", shared_with_conversions),
    ]


def _baseline(module):
    """The named tests the SHIPPED policy and allowlist fail (`CH-0237`).

    A mutation table's killer counts are evidence only if the unmutated subject passes.
    """
    failed = [description for found, census, expected, description in module.PROSE_EXIT_TESTS
              if module.prose_exit_code(found, census) != expected]
    failed += [description
               for basename, literal, admitted, description in module.PROSE_ALLOWLIST_TESTS
               if module.prose_allowlisted(
                   os.path.join(module.RESULTS, basename), literal) != admitted]
    return failed


def main():
    module = _module()
    exit_tests = module.PROSE_EXIT_TESTS
    allow_tests = module.PROSE_ALLOWLIST_TESTS
    baseline = _baseline(module)
    print(f"# baseline: the shipped policy fails {len(baseline)} named test(s)")
    if baseline:
        for description in baseline:
            print(f"  BASELINE FAILS  {description}")
        return 1
    total_tests = len(exit_tests) + len(allow_tests)
    print(f"-- T-250 mutation coverage of the --prose GATE, over {total_tests} named tests "
          f"({len(exit_tests)} exit-policy rows + {len(allow_tests)} allowlist rows) --")

    reached = {("exit", i): 0 for i in range(len(exit_tests))}
    reached.update({("allow", i): 0 for i in range(len(allow_tests))})
    survivors = 0

    for name, direction, policy in exit_mutations(module):
        failed = []
        for index, (found, census, expected, description) in enumerate(exit_tests):
            if policy(found, census) != expected:
                failed.append(description)
                reached[("exit", index)] += 1
        if not failed:
            survivors += 1
        print(f"  {len(failed):3d} named test(s) fail  <-  {name} ({direction})")
        for description in failed:
            print(f"          {description}")

    for name, direction, predicate in allowlist_mutations(module):
        failed = []
        for index, (basename, literal, admitted, description) in enumerate(allow_tests):
            path = os.path.join(module.RESULTS, basename)
            if predicate(path, literal) != admitted:
                failed.append(description)
                reached[("allow", index)] += 1
        if not failed:
            survivors += 1
        print(f"  {len(failed):3d} named test(s) fail  <-  {name} ({direction})")
        for description in failed:
            print(f"          {description}")

    silent = [key for key, count in reached.items() if count == 0]
    # `T-306`: the row count, for `tools/T-295-mutation-input-census.py`, which cannot see a
    # partial shape change in a harness that states none.
    print(f"# {len(exit_mutations(module)) + len(allowlist_mutations(module))} mutation(s), "
          f"{survivors} survivor(s)")
    print(f"-- {total_tests - len(silent)} of {total_tests} rows are reached by some mutation --")
    for kind, index in silent:
        table = exit_tests if kind == "exit" else allow_tests
        print(f"     UNREACHED: {table[index][-1]}")
    if survivors:
        print(f"MUTATION TEST FAILED — {survivors} mutation(s) pass every named test")
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main())
