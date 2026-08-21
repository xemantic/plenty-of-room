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
# T-266 -- load a `.sc` file written by this repository's scadnano WRITER with the REFERENCE
# implementation, and report what it derives.
#
#     python3 tools/scadnano/validate-sc.py gpd/designs/*.sc
#     python3 tools/scadnano/validate-sc.py --selftest
#
# WHY THIS EXISTS. `ScadnanoWriterTest`'s round trip proves the writer is the inverse of THIS
# repository's reader. It cannot prove that the file is a scadnano file, because both halves of
# that test are ours: a shared misreading of the format would pass it silently. The only honest
# test of a compatibility claim is somebody else's parser, and scadnano ships one.
#
# The check is deliberately strict about WARNINGS as well as errors -- `warnings.simplefilter`
# turns the reference implementation's own advisory output into something this script counts,
# because "loads without warnings" is the acceptance predicate `T-266` was written to.
#
# ENVIRONMENT. `python3 -m pip install --break-system-packages scadnano` (0.21.1 here; the box
# ships no `pip` by default, `sudo apt-get install -y python3-pip` first). The reference
# implementation is pure Python and needs nothing else.

import json
import sys
import warnings


def facts(design):
    """The lattice facts, derived by the REFERENCE implementation rather than by ours."""
    scaffolds = [s for s in design.strands if s.is_scaffold]
    staples = [s for s in design.strands if not s.is_scaffold]

    def crossings(strands):
        n = 0
        for strand in strands:
            domains = [d for d in strand.domains if hasattr(d, 'helix')]
            for a, b in zip(domains, domains[1:]):
                if a.helix != b.helix:
                    n += 1
        return n

    return {
        'grid': str(design.grid),
        'helices': len(design.helices),
        'strands': len(design.strands),
        'scaffolds': len(scaffolds),
        'staples': len(staples),
        'scaffoldDomains': sum(len(s.domains) for s in scaffolds),
        'scaffoldCrossings': crossings(scaffolds),
        'stapleCrossings': crossings(staples),
    }


def validate(path):
    import scadnano as sc
    with warnings.catch_warnings(record=True) as caught:
        warnings.simplefilter('always')
        try:
            design = sc.Design.from_scadnano_file(path)
        except Exception as error:  # noqa: BLE001 -- any refusal is the finding
            return {'path': path, 'loaded': False, 'error': repr(error), 'warnings': []}
        result = {'path': path, 'loaded': True,
                  'warnings': [str(w.message) for w in caught]}
        result.update(facts(design))
        return result


def selftest():
    """The script's own predicates, on files it constructs -- no repository state involved."""
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    import tempfile
    import os
    good = {
        'version': '0.21.1', 'grid': 'square',
        'helices': [{'grid_position': [0, 0]}, {'grid_position': [0, 1]}],
        'strands': [
            {'domains': [{'helix': 0, 'forward': True, 'start': 0, 'end': 16},
                         {'helix': 1, 'forward': False, 'start': 0, 'end': 16}],
             'is_scaffold': True},
            {'domains': [{'helix': 0, 'forward': False, 'start': 0, 'end': 16}]},
        ],
    }
    directory = tempfile.mkdtemp()
    good_path = os.path.join(directory, 'good.sc')
    with open(good_path, 'w') as handle:
        json.dump(good, handle)
    report = validate(good_path)
    check('a well formed design loads', report['loaded'])
    check('and it loads without warnings', report['warnings'] == [])
    check('the scaffold is counted', report['scaffolds'] == 1)
    check('the staple is counted', report['staples'] == 1)
    check('the scaffold crossing is counted', report['scaffoldCrossings'] == 1)
    check('a staple with one domain crosses nothing', report['stapleCrossings'] == 0)

    bad = json.loads(json.dumps(good))
    bad['strands'][0]['domains'][1]['helix'] = 7      # no such helix
    bad_path = os.path.join(directory, 'bad.sc')
    with open(bad_path, 'w') as handle:
        json.dump(bad, handle)
    report = validate(bad_path)
    check('a domain on a helix that does not exist is REFUSED', not report['loaded'])

    overlap = json.loads(json.dumps(good))
    overlap['strands'].append(
        {'domains': [{'helix': 0, 'forward': True, 'start': 4, 'end': 8}]})
    overlap_path = os.path.join(directory, 'overlap.sc')
    with open(overlap_path, 'w') as handle:
        json.dump(overlap, handle)
    report = validate(overlap_path)
    check('two strands claiming one base are REFUSED', not report['loaded'])

    for failure in failures:
        print('SELFTEST FAILED: ' + failure)
    print(('%d of %d self-tests pass' % (8 - len(failures), 8)))
    return 1 if failures else 0


def main(argv):
    if '--selftest' in argv:
        return selftest()
    paths = [a for a in argv if not a.startswith('-')]
    if not paths:
        print('usage: validate-sc.py <design.sc> [...]  |  --selftest')
        return 2
    reports = [validate(path) for path in paths]
    print(json.dumps(reports, indent=1))
    defects = [r for r in reports if not r['loaded'] or r['warnings']]
    for report in defects:
        print('DEFECT: ' + report['path'] + ' ' +
              (report.get('error') or '; '.join(report['warnings'])))
    print('# %d of %d file(s) load in the reference implementation without warnings'
          % (len(reports) - len(defects), len(reports)))
    return 1 if defects else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
