#!/usr/bin/env python3
"""Re-centre an oxDNA configuration in a larger cubic box.

scadnano writes a box just big enough to hold the design. A 38 x 38 nm tile in a
69 nm box can see its own periodic image once it fluctuates, which would put a
spurious interaction into exactly the bending modes being measured.
"""
import sys

inp, out, box = sys.argv[1], sys.argv[2], float(sys.argv[3])
lines = open(inp).read().splitlines()
header, body = lines[:3], lines[3:]
pos = [[float(v) for v in ln.split()[:3]] for ln in body]
c = [sum(p[i] for p in pos) / len(pos) for i in range(3)]
shift = [box / 2 - c[i] for i in range(3)]
with open(out, 'w') as f:
    f.write(header[0] + '\n')
    f.write(f'b = {box} {box} {box}\n')
    f.write(header[2] + '\n')
    for ln in body:
        v = ln.split()
        for i in range(3):
            v[i] = repr(float(v[i]) + shift[i])
        f.write(' '.join(v) + '\n')
extent = [max(p[i] for p in pos) - min(p[i] for p in pos) for i in range(3)]
print(f'{len(pos)} nucleotides, extent {extent[0]:.1f} x {extent[1]:.1f} x {extent[2]:.1f} '
      f'oxDNA units -> box {box}')
