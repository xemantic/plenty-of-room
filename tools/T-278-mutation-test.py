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
# T-278 -- every classification of the two `CH-0223` predicates, mutation-tested in BOTH directions.
#
#     tools/T-278-mutation-test.py
#
# WHY. `C-0127`'s standard is that restoring the old, narrow predicate must fail a NAMED test, and
# `C-0138`'s addition is that a predicate carrying exclusions has TWO directions and the second is
# the one that is never written. Both predicates here carry exclusions:
#
#   `rounds`             counts `roundedForResult` and NOT `roundedForProse` -- which renders a
#                        SENTENCE and reaches no leaf of the numeric tree. Counting it would have
#                        declared `brush/CrossoverLayerStudy` rounded on four calls that move
#                        nothing, which is `CH-0223`'s own §6.
#   `simulate`'s walk    exempts a parameter record, an integral JSON number, a string, a boolean
#                        and a null, and applies a `record/spelling` map most-specific-first.
#
# A mutation that fails NO named test is the finding, not a gap in the list (`C-0161`).
import importlib.util
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _module(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _run_selftest(module):
    """True when the module's own `--selftest` passes."""
    import io
    import contextlib
    buffer = io.StringIO()
    with contextlib.redirect_stdout(buffer):
        code = module._selftest()
    return code == 0, buffer.getvalue()


MUTATIONS = []


def mutation(name):
    def register(function):
        MUTATIONS.append((name, function))
        return function
    return register


@mutation("the rounding-call set is widened to count a PROSE rendering")
def _widen_prose(census, simulation):
    census.ROUNDING_CALLS = census.ROUNDING_CALLS + ("roundedForProse",)


@mutation("the rounding-call set is narrowed to the boundary call alone")
def _narrow_calls(census, simulation):
    census.ROUNDING_CALLS = ("roundedForResult",)


@mutation("comments are not blanked, so a KDoc naming the call counts as one")
def _no_blanking(census, simulation):
    census._blank_comments = lambda text: text


@mutation("the parameter-record exemption is dropped, so an INPUT is rounded")
def _no_parameter_exemption(census, simulation):
    simulation.PARAMETER_RECORDS = ()


@mutation("the departure map is keyed on the LEAF name, losing the record qualifier")
def _leaf_keyed_departures(census, simulation):
    simulation.DEPARTURE_DIGITS_BY_KEY = {
        spelling: simulation.DEPARTURE_SIGNIFICANT_DIGITS
        for spelling in simulation.DEPARTURE_SPELLINGS
    }


@mutation("the absolute floor is dropped, so a value below it is not flattened")
def _no_floor(census, simulation):
    original = simulation.round_for_result
    simulation.round_for_result = lambda v, d=9, f=0.0: original(v, d, 0.0)


@mutation("half-even rounding replaces the JVM's half-up on the magnitude")
def _half_even(census, simulation):
    import math

    def half_even(value, digits=9, floor=1e-9):
        if not math.isfinite(value) or value == 0.0:
            return value if math.isfinite(value) else value
        if abs(value) < floor:
            return 0.0
        scale = 10.0 ** (digits - 1 - math.floor(math.log10(abs(value))))
        return round(value * scale) / scale

    simulation.round_for_result = half_even


@mutation("a per-key declaration in the source is ignored")
def _ignore_declarations(census, simulation):
    census.declared_precision = lambda text: None


def _baseline():
    """The named self-tests an UNMUTATED pair of modules fails (`CH-0237`).

    A mutation table's killer counts are evidence only if the unmutated subject passes, and
    nothing here was asserting that.  Two modules, so two readings.
    """
    failures = []
    for label, path in (("simulation", "T-278-rounding-simulation.py"),
                        ("census", "T-278-emitter-rounding-census.py")):
        passed, output = _run_selftest(_module("t278_base_" + label, path))
        if not passed:
            failures += ["%s: %s" % (label, line.strip())
                         for line in output.splitlines() if line.startswith("FAIL")]
    return failures


def main():
    baseline = _baseline()
    print("# baseline: %d pre-existing named failure(s) in an unmutated pair" % len(baseline))
    if baseline:
        for line in baseline:
            print("  BASELINE FAILS  %s" % line)
        return 1
    survivors = []
    for name, mutate in MUTATIONS:
        simulation = _module("t278_sim_mut", "T-278-rounding-simulation.py")
        census = _module("t278_cen_mut", "T-278-emitter-rounding-census.py")
        # The census loads its own copy of the simulation; point it at the mutated one.
        census._module = lambda alias, path, _s=simulation: (
            _s if "rounding-simulation" in path else _module(alias, path)
        )
        mutate(census, simulation)
        killers = []
        for label, module in (("simulation", simulation), ("census", census)):
            try:
                passed, output = _run_selftest(module)
            except Exception as failure:  # a mutation that makes a named test THROW is killed by it
                killers.append("FAIL %s self-test raised %s: %s"
                               % (label, type(failure).__name__, failure))
                continue
            if not passed:
                killers += [
                    line.strip() for line in output.splitlines() if line.startswith("FAIL")
                ]
        if killers:
            print("killed by %d named test(s)  %s" % (len(killers), name))
            for killer in killers[:3]:
                print("            %s" % killer)
        else:
            print("SURVIVED                    %s" % name)
            survivors.append(name)
    print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), len(survivors)))
    return 1 if survivors else 0


if __name__ == "__main__":
    sys.exit(main())
