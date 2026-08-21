#!/usr/bin/env python3
"""
Lattice invariants of the generated Gen-1 tile.

Every number here is one this repository already states, so the test is a check
that the structure handed to oxDNA IS the structure the corpus models -- not a
restatement of whatever the generator happens to produce.
"""
import collections
import sys

from gen1_tile_design import build_design, crossover_columns, interface_columns

HELICES, ROW_BP, PHASE, PERIOD, NICK = 15, 112, 8, 16, 32


def check(name, got, want):
    ok = got == want
    print(f'  {"ok  " if ok else "FAIL"} {name}: {got}' + ('' if ok else f' (want {want})'))
    return ok


def main():
    ok = True
    columns = crossover_columns(ROW_BP, PHASE, PERIOD)
    per_interface = [len(interface_columns(columns, b)) for b in range(HELICES - 1)]

    print('lattice (C-0086 / C-0063 / CLAUDE.md):')
    # 112 bp is an ODD multiple of 16 bp -- the seamless-raster width condition
    ok &= check('row is an odd multiple of 16 bp', ROW_BP % 32, 16)
    ok &= check('crossover columns', columns, [8, 24, 40, 56, 72, 88, 104])
    # "seven columns split 4/3 between the two crossover parities"
    ok &= check('per-interface counts', sorted(set(per_interface)), [3, 4])
    ok &= check('interfaces with 4', per_interface.count(4), 7)
    ok &= check('interfaces with 3', per_interface.count(3), 7)
    ok &= check('total crossovers', sum(per_interface), 49)

    d = build_design(HELICES, ROW_BP, PHASE, PERIOD, NICK)
    scaffolds = [s for s in d.strands if s.is_scaffold]
    staples = [s for s in d.strands if not s.is_scaffold]

    print('\nstrands:')
    ok &= check('one scaffold', len(scaffolds), 1)
    ok &= check('scaffold length', scaffolds[0].dna_length(), HELICES * ROW_BP)
    ok &= check('total nucleotides',
                sum(s.dna_length() for s in d.strands), 2 * HELICES * ROW_BP)

    # every lattice site covered exactly once on the staple side
    seen = collections.Counter()
    for s in staples:
        for dom in s.domains:
            for o in range(dom.start, dom.end):
                seen[(dom.helix, o)] += 1
    print('\ncoverage:')
    ok &= check('staple sites covered', len(seen), HELICES * ROW_BP)
    ok &= check('sites covered more than once',
                sum(1 for v in seen.values() if v != 1), 0)

    # A crossover must be a single strand crossing: two reciprocal crossings at
    # one offset cannot relax, which is what broke the first run.
    crossings = collections.Counter()
    for s in staples:
        for a, b in zip(s.domains, s.domains[1:]):
            if a.helix != b.helix:
                lo, hi = sorted((a.helix, b.helix))
                off = a.end if a.forward else a.start
                crossings[(lo, hi, off)] += 1
    print('\ncrossings:')
    ok &= check('staple crossings', sum(crossings.values()), 49)
    ok &= check('sites with a double crossing',
                sum(1 for v in crossings.values() if v > 1), 0)
    ok &= check('crossings on a column',
                all(off in columns for _, _, off in crossings), True)

    print('\nstaple domains:')
    dl = collections.Counter(len(x) for s in staples for x in s.domains)
    print(f'  domain length histogram: {dict(sorted(dl.items()))}')
    # a domain shorter than 8 nt is below anything origami builds
    ok &= check('shortest domain >= 8 nt', min(dl), 8)
    # the 8 nt domains that remain must not be the only tie holding a crossover
    lone = [s for s in staples if s.dna_length() <= 8]
    print(f'  lone 8-mers (carry no crossover, so fraying costs no crossover): {len(lone)}')
    ok &= check('lone 8-mers have exactly one domain',
                all(len(s.domains) == 1 for s in lone), True)

    print('\nPASS' if ok else '\nFAIL')
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
