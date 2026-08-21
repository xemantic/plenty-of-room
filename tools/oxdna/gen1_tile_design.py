#!/usr/bin/env python3
"""
Generate the C-0086 Gen-1 tile as a scadnano design and export it to
oxDNA (.top/.dat), caDNAno v2 (.json) and scadnano (.sc) formats.

The tile is the single-layer square-lattice Rothemund raster this repository's
`structure/` package models as an orthotropic plate:

  * 15 duplexes (`T-10` geometry: beamCount = 15)
  * 112 bp per row -- the only buildable SEAMLESS raster width near the 40 nm
    of the problem definition, because a boustrophedon needs an ODD number of
    half turns across its row and 112 = 7 x 16 bp (`C-0086`).
    112 bp x 0.34 nm = 38.08 nm along the helices.
  * 15 x 2.69 nm = 40.35 nm across the helices (Fischer et al. 2016 SAXS).
  * Staple crossovers every 16 bp along a helix, ALTERNATING between its two
    neighbours, so a given adjacent pair is linked every 32 bp
    (`CLAUDE.md`: "The crossover spacing is 32 bp, not 16 bp").
  * Crossover columns at x = 8 + 16k, k = 0..6 -- crossover phase 8, one of the
    two centro-symmetric phases a seamless 112 bp row admits (`C-0063`).

Interface b (between helix b and b+1) takes the columns with k = b (mod 2), so
the seven columns split 4/3 between the two crossover parities exactly as
`CLAUDE.md` records for a seven-column sheet. That gives 7 x 4 + 7 x 3 = 49
crossovers, inside the 49-56 the corpus reports the tile builds.

The scaffold is a seamless boustrophedon: the ONLY scaffold crossovers are the
14 raster turns at the row ends, so the design has no seam.

Usage:
    python tools/oxdna/gen1_tile_design.py --out build-oxdna/design
"""

import argparse
import json
import os

import scadnano as sc

RISE_NM = 0.34           # B-DNA rise, nm per bp
INTERHELICAL_NM = 2.69   # Fischer et al. 2016 SAXS, single-layer sheet


def crossover_columns(row_bp: int, phase: int, period: int):
    """Columns at which a staple crossover plane falls, x = phase + k*period."""
    return [x for x in range(phase, row_bp, period)]


def interface_columns(columns, interface: int):
    """Columns belonging to interface `b` -- k = b (mod 2), so the two
    parities interleave every 16 bp along a helix and any one adjacent PAIR is
    linked every 32 bp."""
    return [c for k, c in enumerate(columns) if k % 2 == interface % 2]


def build_design(num_helices: int, row_bp: int, phase: int, period: int,
                 nick_period: int, rise: float = RISE_NM,
                 interhelical: float = INTERHELICAL_NM,
                 bases_per_turn: float = 10.67,
                 crossing_rule: str = 'alternate-interface') -> sc.Design:
    columns = crossover_columns(row_bp, phase, period)

    # crossovers[(helix, x)] = neighbour helix, for every crossover the sheet builds
    # A Rothemund staple crossover is a SINGLE strand crossing. Registering the
    # site from BOTH sides puts two reciprocal crossings at the same base
    # offset, which is geometrically over-constrained -- the two backbones would
    # have to face each other simultaneously in opposite senses -- and it does
    # not relax: minimisation stalls with those bonds at 1.56 oxDNA units where
    # the FENE is only defined out to 1.00, so the real potential then diverges.
    # Register each site once; the neighbouring helix passes through it.
    crossovers = {}
    for b in range(num_helices - 1):
        for k, c in enumerate(interface_columns(columns, b)):
            if crossing_rule == 'up':
                donor, receiver = b, b + 1
            elif crossing_rule == 'alternate':
                donor, receiver = (b, b + 1) if k % 2 == 0 else (b + 1, b)
            elif crossing_rule == 'alternate-interface':
                donor, receiver = (b, b + 1) if b % 2 == 0 else (b + 1, b)
            else:
                raise ValueError(crossing_rule)
            crossovers[(donor, c)] = receiver

    helices = [sc.Helix(max_offset=row_bp, grid_position=(0, i))
               for i in range(num_helices)]
    # The geometry is the corpus's own: Fischer et al. 2016 SAXS interhelical
    # distance and B-DNA's rise. `bases_per_turn` is caDNAno's SQUARE-lattice
    # design twist, 10.67 = 32 bp / 3 turns -- deliberately NOT B-DNA's 10.5, so
    # that the starting structure carries the register mismatch the corpus says
    # an untwist-corrected raster accumulates, and oxDNA relaxes against it.
    geometry = sc.Geometry(rise_per_base_pair=rise,
                           helix_radius=1.0,
                           inter_helix_gap=interhelical - 2.0,
                           bases_per_turn=bases_per_turn,
                           minor_groove_angle=150.0)
    design = sc.Design(helices=helices, grid=sc.square, geometry=geometry)

    # ---------------------------------------------------------------- scaffold
    # Seamless boustrophedon. Even helices carry the scaffold 5'->3' left to
    # right, odd helices right to left, so the raster turns at alternating ends
    # and every scaffold crossover is a row end.
    builder = design.draw_strand(0, 0).move(row_bp)
    for i in range(1, num_helices):
        builder = builder.cross(i)
        builder = builder.move(-row_bp if i % 2 == 1 else row_bp)
    builder.as_scaffold()

    # ----------------------------------------------------------------- staples
    # Staple direction is opposite the scaffold on every helix.
    #
    # The staple side of the sheet is first assembled IGNORING nicks, giving the
    # maximal chains the crossover lattice defines; the chains are then cut into
    # staples. A cut is legal only at a boundary that is NOT a crossover -- a cut
    # at a crossover would delete that crossover, which is the whole quantity
    # under measurement. Boundaries alternate crossover / midpoint along an
    # interior helix, so the legal cuts are the domain midpoints and the 16 bp
    # stagger between adjacent helices emerges rather than being imposed.
    half = period // 2
    segments = [(a, a + half) for a in range(0, row_bp, half)]

    def staple_dx(helix):
        return -1 if helix % 2 == 0 else +1

    def exit_boundary(helix, a, b):
        return b if staple_dx(helix) > 0 else a

    def entry_boundary(helix, a, b):
        return a if staple_dx(helix) > 0 else b

    # A crossover INTERRUPTS the receiving helix: the strand arriving from the
    # donor takes the bases downstream of the crossing point, so whatever was
    # running along the receiver towards that point must end there. That break
    # is the nick a real crossover carries, and without it two strands claim the
    # same bases.
    receiving = {(nb, x) for (h, x), nb in crossovers.items()}

    def step(helix, a, b):
        """Follow the staple path one segment, 5' -> 3'."""
        x = exit_boundary(helix, a, b)
        if x == 0 or x == row_bp:
            return None
        if (helix, x) in receiving:
            return None
        nb = crossovers.get((helix, x), helix)
        d = staple_dx(nb)
        na = x if d > 0 else x - half
        return (nb, na, na + half)

    segments_all = [(h, a, b) for h in range(num_helices) for (a, b) in segments]

    # Crossings are ONE-WAY, so the predecessor of a segment cannot be found by
    # running `step` backwards -- it is found by inverting the successor map.
    succ = {s: step(*s) for s in segments_all}
    pred = {}
    for s, t in succ.items():
        if t is not None:
            assert t not in pred, f'two staple segments both feed {t}'
            pred[t] = s

    chains = []
    visited = set()
    for s in segments_all:
        if s in pred or s in visited:
            continue
        chain, cur = [], s
        while cur is not None:
            assert cur not in visited, f'staple path revisits {cur}'
            chain.append(cur)
            visited.add(cur)
            cur = succ[cur]
        chains.append(chain)

    # Anything unvisited lies on a CLOSED staple cycle -- a path with no 5' end.
    # A narrow strip does this (its staples circulate between two helices), where
    # a wide raster does not, because there every path terminates at a tile edge.
    # Break each cycle at a legal (non-crossover) boundary.
    for s in segments_all:
        if s in visited:
            continue
        cycle, cur = [], s
        while cur is not None and cur not in visited:
            cycle.append(cur)
            visited.add(cur)
            cur = succ[cur]
        breaks = [i for i in range(len(cycle))
                  if (cycle[i - 1][0],
                      exit_boundary(*cycle[i - 1])) not in crossovers]
        assert breaks, f'closed staple cycle with no legal nick: {cycle}'
        k = breaks[0]
        chains.append(cycle[k:] + cycle[:k])

    expected = {(h, a, b) for h in range(num_helices) for (a, b) in segments}
    assert visited == expected, (
        f'coverage gap: {len(expected - visited)} segments unclaimed, '
        f'{len(visited - expected)} spurious')

    target_segments = nick_period // half   # 4 segments = 32 nt at the default

    def cut_legal(chain, i):
        """May the chain be cut between segment i-1 and segment i? Only where the
        shared boundary is not a crossover -- cutting at one would delete it."""
        h, a, b = chain[i - 1]
        return (h, exit_boundary(h, a, b)) not in crossovers

    paths = []
    for chain in chains:
        cuts = [0]
        for i in range(1, len(chain)):
            if i - cuts[-1] >= target_segments and cut_legal(chain, i):
                cuts.append(i)
        cuts.append(len(chain))
        # a trailing piece shorter than 3 segments (24 nt) is merged back
        if len(cuts) > 2 and cuts[-1] - cuts[-2] < 3:
            cuts.pop(-2)
        for lo, hi in zip(cuts, cuts[1:]):
            paths.append(chain[lo:hi])

    for path in paths:
        runs = []
        for (h, a, b) in path:
            if runs and runs[-1][0] == h:
                runs[-1][1] = min(runs[-1][1], a)
                runs[-1][2] = max(runs[-1][2], b)
            else:
                runs.append([h, a, b])
        h0, a0, b0 = runs[0]
        start = a0 if staple_dx(h0) > 0 else b0
        sb = design.draw_strand(h0, start)
        for idx, (h, a, b) in enumerate(runs):
            if idx > 0:
                sb = sb.cross(h)
            sb = sb.to(b if staple_dx(h) > 0 else a)
        sb.with_color(sc.Color(hex_string='#0066cc'))

    # Every helix starts at roll 0, which leaves the two backbones at a crossover
    # pointing away from each other: the crossover bond is then ~1.3 nm where the
    # FENE reaches ~0.7, and no amount of minimisation closes it. Relaxing the
    # rolls turns each helix so its backbone faces the neighbour it crosses to,
    # which is what caDNAno's 8 bp / 270 deg rule expresses for the square lattice.
    design.relax_helix_rolls()
    return design


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--helices', type=int, default=15)
    p.add_argument('--row-bp', type=int, default=112)
    p.add_argument('--phase', type=int, default=8)
    p.add_argument('--period', type=int, default=16)
    p.add_argument('--nick-period', type=int, default=32,
                   help='32 gives Rothemund 32-mer staples (8+16+8), nicks '
                        'staggered by 16 bp between adjacent helices')
    p.add_argument('--rise', type=float, default=RISE_NM)
    p.add_argument('--interhelical', type=float, default=INTERHELICAL_NM)
    p.add_argument('--bases-per-turn', type=float, default=10.67,
                   help="caDNAno's square-lattice design twist (32 bp = 3 turns)")
    p.add_argument('--crossing-rule', default='alternate-interface',
                   choices=['up', 'alternate', 'alternate-interface'],
                   help='which side of each interface donates the crossing '
                        'strand; alternate-interface gives the fewest short '
                        'staple domains (28 of 113 against 70 of 134 for up)')
    p.add_argument('--out', default='build-oxdna/design')
    args = p.parse_args()

    design = build_design(args.helices, args.row_bp, args.phase,
                          args.period, args.nick_period, args.rise,
                          args.interhelical, args.bases_per_turn,
                          args.crossing_rule)

    scaffolds = [s for s in design.strands if s.is_scaffold]
    staples = [s for s in design.strands if not s.is_scaffold]
    assert len(scaffolds) == 1, f'expected 1 scaffold, got {len(scaffolds)}'
    scaffold = scaffolds[0]

    seq = sc.m13()
    design.assign_dna(scaffold, seq[:scaffold.dna_length()])

    os.makedirs(os.path.dirname(args.out) or '.', exist_ok=True)
    directory = os.path.dirname(args.out) or '.'
    stem = os.path.basename(args.out)
    design.write_oxdna_files(directory=directory, filename_no_extension=stem)
    design.write_scadnano_file(directory=directory, filename=stem + '.sc')
    design.write_cadnano_v2_file(directory=directory, filename=stem + '.json')

    # Nucleotide index -> (helix, offset, forward), in scadnano's own oxDNA
    # emission order: strands in order, domains in order, offsets start..end-1
    # reversed on a reverse domain so the strand reads 5' -> 3'. This is what
    # lets the analysis address the lattice instead of guessing it from geometry.
    nucleotides = []
    for strand in design.strands:
        for domain in strand.domains:
            offsets = list(range(domain.start, domain.end))
            if not domain.forward:
                offsets.reverse()
            for off in offsets:
                nucleotides.append([domain.helix, off, domain.forward])
    assert len(nucleotides) == sum(s.dna_length() for s in design.strands)
    seen = {}
    for i, (h, o, fwd) in enumerate(nucleotides):
        key = (h, o, fwd)
        assert key not in seen, f'two nucleotides at {key}'
        seen[key] = i
    for h in range(args.helices):
        for o in range(args.row_bp):
            for fwd in (True, False):
                assert (h, o, fwd) in seen, f'lattice site {(h, o, fwd)} unoccupied'
    with open(os.path.join(directory, stem + '-nucleotides.json'), 'w') as f:
        json.dump(nucleotides, f)

    lengths = sorted(s.dna_length() for s in staples)
    domains = [len(d) for s in staples for d in s.domains]
    columns = crossover_columns(args.row_bp, args.phase, args.period)
    n_cross = sum(len(interface_columns(columns, b))
                  for b in range(args.helices - 1))

    summary = {
        'helices': args.helices,
        'rowBasePairs': args.row_bp,
        'crossoverPhase': args.phase,
        'crossoverPlanePeriodBp': args.period,
        'crossoverColumns': columns,
        'crossoversBuilt': n_cross,
        'crossoversPerInterface': [len(interface_columns(columns, b))
                                   for b in range(args.helices - 1)],
        'scaffoldLengthNt': scaffold.dna_length(),
        'stapleCount': len(staples),
        'stapleLengthMinNt': lengths[0],
        'stapleLengthMaxNt': lengths[-1],
        'totalNucleotides': sum(s.dna_length() for s in design.strands),
        'shortestDomainNt': min(domains),
        'edgeXNm': round(args.row_bp * args.rise, 4),
        'edgeYNm': round((args.helices - 1) * args.interhelical, 4),
        'risePerBasePairNm': args.rise,
        'interhelicalDistanceNm': args.interhelical,
        'designBasesPerTurn': args.bases_per_turn,
    }
    with open(os.path.join(directory, stem + '-summary.json'), 'w') as f:
        json.dump(summary, f, indent=1)
    print(json.dumps(summary, indent=1))


if __name__ == '__main__':
    main()
