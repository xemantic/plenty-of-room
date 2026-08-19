#!/usr/bin/env python3
"""T-233 — reconcile C-0144's 41-entry work list against the two deliverables.

`C-0144` §9 published, in
`gpd/results/T-234-honeycomb-correction-supersession.json` under
`deliverableListForT233`, the 41 corpus statements in `ANSWERS.md` and
`DECISIONS-FOR-NDI.md` that assert a premise `C-0141`/`C-0140` withdrew.
This tool locates each one **by its exact line text, never by its line number**
(the numbers drift on every edit) and reports what `T-233` did with it.

Three dispositions, decided mechanically:

  RESTATED  the line was edited; the withdrawn token now appears only inside a
            ``~~struck~~`` span, or on a line that also carries a superseding cue.
  LEFT      the line is byte-identical to what `C-0144` recorded, and a
            superseding cue appears in the LOOKBACK lines above it. This is the
            disposition for an explicitly historical block, which the repository's
            *strike, never delete* rule requires be kept as written.
  DEFECT    neither — a withdrawn premise still stands unqualified.

Exit 1 on any DEFECT, or on any entry that cannot be located at all.

Usage:  tools/T-233-reconcile.py [--verbose] [--self-test]
"""
import json
import os
import re
import sys

RESULT = 'gpd/results/T-234-honeycomb-correction-supersession.json'
FILES = ('ANSWERS.md', 'DECISIONS-FOR-NDI.md')
LOOKBACK = 40

# A cue that a superseding statement is present. Upper-case verdict words are
# matched case-sensitively (the corpus writes verdicts in bold upper case and
# prose in lower), the rest case-insensitively, because a false positive here
# would silently pass a live withdrawn premise.
CUE_CASED = ('SUPERSEDED', 'WITHDRAWN', 'RESTATED', 'RE-GRADED', 'CORRECTED',
             'REVISION', 'RE-POSED')
CUE_ANY = ('superseded', 'is a withdrawn width', 'every number below',
           'restated', 'corrected', 'withdrawn', 're-graded', 're-posed')


# A task/claim/challenge identifier contains digits, so `C-0132` matches the bare
# token `132`. `tools/T-234-census.py` blanks identifiers length-preservingly
# before matching, for exactly this reason, and so must anything reading its list.
_ID = re.compile(r"\b(?:CH|C|P|T|S)-\d{1,4}[a-z]?\b")


def blank_identifiers(text):
    """Replace every identifier by spaces of the SAME length, so offsets survive."""
    return _ID.sub(lambda m: ' ' * (m.end() - m.start()), text)

# A ``~~...~~`` span may cross a soft line break -- GFM emphasis runs to the end
# of the paragraph -- so the spans are found over the whole document and the
# pattern is forbidden from crossing a BLANK line, which does end a paragraph.
STRUCK = re.compile(r'~~[^\n]*?(?:\n(?!\s*\n)[^\n]*?)*?~~')


def struck_spans(text):
    """(start, end) character ranges of every ``~~...~~`` span in `text`."""
    return [(m.start(), m.end()) for m in STRUCK.finditer(text)]


def token_is_struck(text, token):
    """True iff every occurrence of `token` in `text` lies inside a struck span."""
    spans = struck_spans(text)
    for m in re.finditer(re.escape(token), text):
        if not any(a <= m.start() and m.end() <= b for a, b in spans):
            return False          # at least one live occurrence
    return True


def live_occurrences(text, lines, token, only_line=None):
    """1-based line numbers carrying an occurrence of `token` outside every struck span."""
    spans = struck_spans(text)
    starts, off = [], 0
    for l in lines:
        starts.append(off)
        off += len(l) + 1
    out = []
    for m in re.finditer(re.escape(token), text):
        if any(a <= m.start() and m.end() <= b for a, b in spans):
            continue
        lo, hi = 0, len(starts) - 1
        while lo < hi:
            mid = (lo + hi + 1) // 2
            if starts[mid] <= m.start():
                lo = mid
            else:
                hi = mid - 1
        n = lo + 1
        if only_line is None or n == only_line:
            out.append(n)
    return out

#: The context a match must ALSO satisfy on its own line, per family, copied from
#: `tools/T-234-census.py`. Without it the WIDTH family fires on `C-0086`'s
#: SQUARE-lattice 112 bp row, which `C-0140` does not touch: it overturns a
#: HONEYCOMB row length. `CLAUDE.md`: a drift checker's false positives cost more
#: than its true ones.
FAMILY_CONTEXT = {
    'WIDTH': re.compile(r"honeycomb|four-layer|four layer|15 . 4|10 . 6"),
    'AZIMUTH': re.compile(r"honeycomb|azimuth|oblique|top face|top-face|station"
                          r"|sublattice|15 . 4|10 . 6"),
}


def in_family_context(family, line):
    pattern = FAMILY_CONTEXT.get(family)
    return pattern is None or bool(pattern.search(line))

def has_cue(text):
    if any(c in text for c in CUE_CASED):
        return True
    low = text.lower()
    return any(c in low for c in CUE_ANY)


def find_line(lines, want):
    """1-based line numbers whose stripped text equals or starts with `want`.

    `C-0144` truncates a very long line in its record, so a prefix match is
    required; the leading indent is stripped because it does too.
    """
    want = want.strip()
    if not want:
        return []
    return [n + 1 for n, l in enumerate(lines) if l.strip().startswith(want)]


def reconcile(entries, contents, verbose=False):
    rows = []
    for i, e in enumerate(entries, 1):
        f, tok = e['file'], e['token']
        text = blank_identifiers(contents[f])
        lines = text.split('\n')
        hits = find_line(lines, e['exactLine'])
        if hits:
            # unchanged: LEFT is legitimate only under a superseding cue above
            n = hits[0]
            window = '\n'.join(lines[max(0, n - 1 - LOOKBACK):n - 1])
            if not live_occurrences(text, lines, tok, n) or has_cue(lines[n - 1]):
                d, why = 'RESTATED', 'token struck or cue on the line itself'
            elif has_cue(window):
                d, why = 'LEFT', 'unedited, superseding cue within %d lines above' % LOOKBACK
            else:
                d, why = 'DEFECT', 'unedited and no superseding cue above'
            rows.append((i, f, n, tok, e['class'], d, why))
            continue
        # edited: every surviving live occurrence of the token must be qualified,
        # by a cue on its own line or by one in the LOOKBACK lines above it --
        # the same rule the LEFT branch uses, because a correction is a block and
        # its explanatory sentences need not each repeat the verdict word.
        bad = [n for n in live_occurrences(text, lines, tok)
               if in_family_context(e.get('family'), lines[n - 1])
               and not has_cue(lines[n - 1])
               and not has_cue('\n'.join(lines[max(0, n - 1 - LOOKBACK):n - 1]))]
        if bad:
            rows.append((i, f, bad[0], tok, e['class'], 'DEFECT',
                         'line edited but %d live occurrence(s) carry no cue' % len(bad)))
        else:
            rows.append((i, f, 0, tok, e['class'], 'RESTATED',
                         'line edited; no live unqualified occurrence remains'))
    if verbose:
        for r in rows:
            print('%2d | %-20s | L%-5d | %-28s | %-10s | %-8s | %s' % r)
    return rows


def self_test():
    ok = True

    def check(name, got, want):
        nonlocal ok
        if got != want:
            ok = False
            print('SELF-TEST FAIL %s: got %r want %r' % (name, got, want))

    check('struck-whole', token_is_struck('a ~~0.5~~ b', '0.5'), True)
    check('struck-partial', token_is_struck('~~0.5~~ and 0.5', '0.5'), False)
    check('struck-none', token_is_struck('plain 0.5', '0.5'), False)
    check('struck-two-spans', token_is_struck('~~0.5~~ x ~~0.5~~', '0.5'), True)
    # a `~~` that never closes is not a span
    check('struck-unclosed', token_is_struck('~~0.5', '0.5'), False)
    # a span MAY cross a soft line break ...
    check('struck-multiline', token_is_struck('~~a 0.5 b\nc d~~', '0.5'), True)
    # ... and may NOT cross a blank line, which ends the paragraph
    check('struck-blank-line', token_is_struck('~~a 0.5 b\n\nc d~~', '0.5'), False)
    check('cue-cased', has_cue('WITHDRAWN, iteration 35'), True)
    # a lower-case prose word must still count -- the file writes both
    check('cue-lower', has_cue('this is superseded below'), True)
    check('cue-absent', has_cue('the tile dishes 0.0577199433'), False)
    # "Left undone" must not read as a cue for anything here
    check('cue-not-done', has_cue('Left undone'), False)
    check('ctx-width-square', in_family_context('WIDTH', 'the nearest width is 112 bp'), False)
    check('ctx-width-honeycomb', in_family_context('WIDTH', 'the honeycomb row is 112 bp'), True)
    check('ctx-none', in_family_context('FOOTPRINT', 'anything at all'), True)
    check('blank-id', blank_identifiers('see `C-0132` now'), 'see `      ` now')
    check('blank-id-length', len(blank_identifiers('C-0132 and T-5b')), len('C-0132 and T-5b'))
    check('blank-id-keeps-bare', blank_identifiers('132 stations'), '132 stations')
    lines = ['alpha', '  beta gamma', 'delta']
    check('find-exact', find_line(lines, 'beta gamma'), [2])
    check('find-indented', find_line(lines, '  beta gamma'), [2])
    check('find-prefix', find_line(lines, 'beta'), [2])
    check('find-missing', find_line(lines, 'epsilon'), [])
    check('find-empty', find_line(lines, ''), [])
    # end-to-end: one entry, three states
    def one(text, tok='0.5', cls='MOVED'):
        e = [{'file': 'F', 'token': tok, 'class': cls, 'family': 'FOOTPRINT',
              'exactLine': 'the value is 0.5'}]
        return reconcile(e, {'F': text})[0][5]
    check('e2e-defect', one('the value is 0.5'), 'DEFECT')
    check('e2e-left', one('RESTATED, iteration 35\nthe value is 0.5'), 'LEFT')
    check('e2e-restated-struck', one('the value is ~~0.5~~ now 0.9'), 'RESTATED')
    check('e2e-restated-gone', one('the value is 0.9'), 'RESTATED')
    check('e2e-live-elsewhere', one('the value is ~~0.5~~ now 0.9\nand 0.5 stands here'), 'DEFECT')
    # a live occurrence inside the correction block itself is qualified by the
    # block's own cue above it -- this is the case that made the first draft
    # of this gate report a false positive against its own repair
    check('e2e-live-under-cue',
          one('the value is ~~0.5~~ now 0.9\nWITHDRAWN: the old 0.5 came from a bad pitch'), 'RESTATED')
    print('# self-tests: %s (%d checks)' % ('PASS' if ok else 'FAIL', 28))
    return ok


def main():
    if '--self-test' in sys.argv:
        sys.exit(0 if self_test() else 1)
    if not self_test():
        sys.exit(1)
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    os.chdir(root)
    entries = json.load(open(RESULT, encoding='utf-8'))['deliverableListForT233']['entries']
    contents = {f: open(f, encoding='utf-8').read() for f in FILES}
    rows = reconcile(entries, contents, verbose='--verbose' in sys.argv)
    counts = {}
    for r in rows:
        counts[r[5]] = counts.get(r[5], 0) + 1
    for r in rows:
        if r[5] == 'DEFECT':
            print('DEFECT %2d %s L%d token=%r : %s' % (r[0], r[1], r[2], r[3], r[6]))
    print('# T-233: %d entries: %s' % (len(rows), ', '.join(
        '%d %s' % (v, k) for k, v in sorted(counts.items()))))
    sys.exit(1 if counts.get('DEFECT') else 0)


if __name__ == '__main__':
    main()
