#!/usr/bin/env bash
# Run every analysis on a finished tile pipeline and print the comparison.
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
RUN=${RUN:-$(pwd)}
OUT=${OUT:-$RUN}
SKIP=${SKIP:-10}
export PYTHONPATH="$HERE"

cd "$RUN"
trajs=$(ls prod*/traj.dat 2>/dev/null || true)
[ -z "$trajs" ] && { echo "no production trajectories in $RUN"; exit 1; }
echo "trajectories: $trajs"
echo "frames: $(cat $trajs | grep -c '^t =')"

python "$HERE/analyse_tile.py" --traj $trajs \
  --nucleotides ../gen1_tile-nucleotides.json --skip "$SKIP" \
  --out "$OUT/tile.json" > "$OUT/tile-stdout.txt"
python "$HERE/interduplex_roll.py" --traj $trajs \
  --nucleotides ../gen1_tile-nucleotides.json --skip "$SKIP" \
  --out "$OUT/roll.json" > /dev/null

args=(--tile "$OUT/tile.json" --roll "$OUT/roll.json")
[ -f "$OUT/../duplex-lp.json" ] && args+=(--duplex "$OUT/../duplex-lp.json")
python "$HERE/compare_with_corpus.py" --t10 ../../gpd/results/T-10-discrete-lattice-tile.json \
  "${args[@]}" --out "$OUT/comparison.json"
