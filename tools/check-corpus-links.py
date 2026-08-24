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
# T-203 / C-0122 -- every relative link in the corpus's Markdown resolves to a path that exists.
# T-313 / C-0209 -- of ANY target kind, not only `.md`; and the run states what it does NOT reach.
#
#     tools/check-corpus-links.py            checks the corpus, exit 1 on any defect
#     tools/check-corpus-links.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. A claim cites its neighbours by relative link, and a claim FILENAME carries a
# slug that the writer reconstructs from memory -- `C-0006-tile-flatness.md` for what is actually
# `C-0006-tile-load-distribution-and-flatness.md`, `C-0034-far-anchorage.md` for
# `C-0034-guided-arm-anchorage.md`. Nothing in the tree checked it, and a sweep found **15** broken
# references accumulated across many iterations, in claims, challenges and task files alike.
#
# It is the same class as `C-0083`'s Markdown tables: invisible to every numeric, status and
# self-consistency check, silent at the point of writing, and cheap to mechanise. `CLAUDE.md`
# already records the general form -- *"a defect's LOCATION is a number like any other, and two
# records can agree with each other and both be wrong about it ... grep the string out of the tree
# before accepting a filename from a claim."* This is that rule, made a gate.
import contextlib as _contextlib
import io as _io
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# ANY relative link, of ANY target kind. Resolved against the FILE'S OWN DIRECTORY with
# `normpath`, which is the only correct rule and the one the first version of this checker got
# wrong: it special-cased `../../` as "the repository root", which is true for `gpd/claims/x.md`
# and false for `gpd/data/T-161-sources/MANIFEST.md`, where the same prefix means `gpd/`. The
# checker reported 15 false positives on its first real run, all of them manifests two levels
# deep. One resolver, no depth assumptions.
#
# `T-313`/`C-0209`. The pattern ended `\.md` until iteration 48, so a `.py`, `.kt`, `.json`, `.sh`
# or DIRECTORY target was invisible -- to this checker and therefore to every gate in the tree,
# since `tools/check-result-path-references.py` reads a claim's `Provenance` row, which is a BARE
# path and not a link. Two agents relocated a mutation harness out from under a Markdown link in
# ONE iteration, each leaving a dangling reference neither noticed. Widening cost nothing: over
# the checker's own file set the widening adds 594 targets and reports 0, and replayed over every
# commit reachable from `HEAD` it fires on 6 distinct (file, link) pairs, ALL SIX genuine dangling
# references and none a false positive. The 86 % false-positive rate the raising claim measured is
# a property of ITS census -- which did not blank code and scanned `tools/` -- and not of this
# predicate: see `scope_note` below for what a clean run here is clean ABOUT.
_LINK = re.compile(r"\]\(([^)\s]+?)(?:#[^)]*)?\)")

# A link inside CODE is not a link, it is a description of one -- and the one place in this
# repository that describes link SHAPES is `CLAUDE.md`'s own entry about this checker's first
# blind spot, which quotes `](C-0006-....md)` verbatim. Scanning the root documents without
# stripping code first would therefore report the sentence that records the defect as a defect.
# Fences first, then inline spans; both are replaced by same-length blanks so that nothing below
# moves and a reported position stays true.
_FENCE = re.compile(r"```.*?```", re.DOTALL)
_CODE_SPAN = re.compile(r"`[^`\n]*`")


def _without_code(text):
    """`text` with fenced blocks and inline code spans blanked, length-preserving."""
    def blank(match):
        return "".join(" " if character != "\n" else "\n" for character in match.group(0))
    return _CODE_SPAN.sub(blank, _FENCE.sub(blank, text))


def relative_links_in(text):
    """Every inline link target in `text` that names a path in this repository.

    Factored out so that the resolver and the scope line cannot disagree about what a relative
    link is: the count the summary reports and the set the gate checks are the same set.
    """
    links = []
    for match in _LINK.finditer(_without_code(text)):
        link = match.group(1)
        # An external URL and an absolute path are not this checker's business, and an
        # ANCHOR-ONLY link (`](#a-heading)`) is not a path at all -- it points inside the file it
        # is written in. The old pattern excluded that third case only as a SIDE EFFECT of ending
        # in `.md`, so the guard is owed the moment the pattern is widened; three of them are in
        # the corpus today.
        if "://" in link or link.startswith("/") or link.startswith("#"):
            continue
        links.append(link)
    return links


def broken_links_in(text, directory, root=ROOT):
    """[(link, resolved path)] for every relative link in `text` that does not resolve.

    The target may be of any kind -- `.md`, `.py`, `.kt`, `.json`, `.sh`, a directory -- because a
    cross-reference is a filename whatever it names.

    `directory` is the file's own directory relative to the repository root, and every link is
    resolved against it — the same slug is valid in one directory and not in another, which is how
    the live defects got in. Absolute URLs and anchors-only links are not this checker's business.
    """
    missing = []
    for link in relative_links_in(text):
        resolved = os.path.normpath(os.path.join(directory, link))
        if not os.path.exists(os.path.join(root, resolved)):
            missing.append((link, resolved))
    return missing


# The two OUTWARD-FACING documents. `T-184` found `DECISIONS-FOR-NDI.md` -- the one NDI actually
# reads -- checked by nothing at all, this checker included: it scanned `gpd/` and stopped there,
# so a mistyped claim slug in either deliverable was invisible. They carry more claim links than
# most claims do, and a broken one in them is read by the customer rather than by an agent.
def root_documents(root=ROOT):
    """Every Markdown file at the repository root, DERIVED rather than listed.

    It was a hand-written list of two, then three, and `C-0160` found the fourth and fifth —
    `ARCHITECTURE.md` and `CLAUDE.md`, both carrying relative links and neither scanned. A list
    that has to be extended by hand every time somebody adds a root document is the same decay
    this checker exists to stop, one level up: *a checker's DEFAULT is part of its logic*. There
    is no root Markdown file that should be exempt from having working links, so the set is the
    directory listing.
    """
    return sorted(name for name in os.listdir(root)
                  if name.endswith(".md") and os.path.isfile(os.path.join(root, name)))
# `TOOLING-NOVELTY.md` was added by the repository owner in iteration 37 and was checked by
# NOTHING, exactly as `T-184`/`C-0124` found for `DECISIONS-FOR-NDI.md`: *a checker's DEFAULT
# is part of its logic, and the document nobody checks is the one the customer reads*. It is a
# third outward-facing deliverable in the same house style (SURVIVES / CORRECTED / WITHDRAWN,
# a read-flag per source), so it belongs in the same set. It was clean when added — the point
# is to keep it so, not to repair it.


def tracked_markdown(root=ROOT):
    """The Markdown under `gpd/` — tracked AND untracked — plus the outward-facing root documents.

    `C-0083` records why the fallback matters: a verification SNAPSHOT has no `.git`, so a checker
    that only knows `git ls-files` silently checks nothing there.
    """
    extra = root_documents(root)
    try:
        listed = subprocess.run(
            ["git", "ls-files", "gpd/**/*.md", "gpd/*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        # `git ls-files` lists TRACKED files only, so a claim an agent has just written and not
        # yet committed is invisible to it. `C-0127` found exactly that: two broken links in its
        # own `Consumes` row passed a run reporting *"0 broken link(s) in 376 file(s)"*, and were
        # caught only by `verify.sh`, which runs in a `.git`-less snapshot where the walk below
        # sees everything. **The blind instrument was the one an agent uses on its OWN work
        # mid-iteration** — the moment the check is worth most. Untracked Markdown is added here so
        # a checkout run and the gate see the same set.
        untracked = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard", "gpd/**/*.md", "gpd/*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        if listed or untracked:
            return sorted(set(listed) | set(untracked) | set(extra))
    except (OSError, subprocess.CalledProcessError):
        pass
    found = list(extra)
    for base, _dirs, files in os.walk(os.path.join(root, "gpd")):
        for name in files:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(base, name), root))
    return sorted(set(found))


# --- the scope line (`T-313`) ---------------------------------------------------------------
#
# WHAT A CLEAN RUN HERE IS CLEAN ABOUT. `C-0083`'s standard is that a gate that cannot come clean
# is not a gate, and `CH-0236`'s corollary is that *"cannot be made clean"* is a statement about a
# PREDICATE. The honest converse is owed too: a predicate that CAN come clean must say what it
# does not reach, or the clean run is read as a statement about the whole corpus.
#
# Two residues, and both are MEASURED on every run rather than declared once, because a declared
# list is a dated object (`C-0176`) and this one would be dated by the first titled link anybody
# writes:
#
#   1. LINK SHAPES the pattern cannot see. `_LINK` reads an inline `](target)` whose target has no
#      whitespace in it. A titled link (`](path "title")`), an angle-bracket link (`](<path>)`)
#      and a reference-style link (`[text][label]`) are all legal Markdown and all invisible to
#      it. They are legal, so they are COUNTED and not gated.
#   2. MARKDOWN FILES outside the scanned set. `tracked_markdown` is `gpd/**/*.md` plus the root
#      documents; `tools/**/*.md` and `third-party/**/*.md` are not in it. That residue is the
#      interesting half of `T-313`'s row and it is not closed here: `tools/C-0156-claim-template.md`
#      carries 23 relative links, EVERY ONE of which resolves against `gpd/claims` -- the directory
#      the template is copied TO -- and none against `tools/`, where it sits. A relative link's
#      correctness is a property of the file the text will END UP IN, which is a DECLARATION
#      problem and not a predicate problem. `third-party/` is the problem definition as received
#      and must not be edited at all, so it needs the treatment `tools/check-markdown-tables.py`
#      already gives it.
_TITLED = re.compile(r"\]\([^)\s]+\s[^)]*\)")
_ANGLE_BRACKET = re.compile(r"\]\(<[^>\n]*>\)")
_REFERENCE_STYLE = re.compile(r"\]\[[^\]\n]+\]")

# Directories a tree walk must not descend into when counting what is NOT scanned: version
# control, build output, and the per-agent build directories `CLAUDE.md` mandates.
_NOT_A_CORPUS = (".git", ".gradle", ".idea", ".kotlin", "__pycache__", "node_modules")


def unmatched_shapes(text):
    """Counts of the link shapes `_LINK` cannot see, in `text`, ignoring code.

    Not defects -- all three are legal Markdown. They are the checker's own blind spot, reported
    so that a reader of a clean run knows how much of the corpus the run spoke for.
    """
    stripped = _without_code(text)
    return {
        "titled": len(_TITLED.findall(stripped)),
        "angleBracket": len(_ANGLE_BRACKET.findall(stripped)),
        "referenceStyle": len(_REFERENCE_STYLE.findall(stripped)),
    }


def unscanned_markdown(root=ROOT):
    """Every Markdown file in the tree that `tracked_markdown` does NOT scan."""
    scanned = set(tracked_markdown(root))
    found = []
    for base, directories, files in os.walk(root):
        directories[:] = [name for name in directories
                          if name not in _NOT_A_CORPUS and not name.startswith("build")]
        for name in files:
            if not name.endswith(".md"):
                continue
            relative = os.path.relpath(os.path.join(base, name), root)
            if relative not in scanned:
                found.append(relative)
    return sorted(found)


def scope_note(root=ROOT, shapes=None, non_markdown=None):
    """One line saying what a clean run is clean ABOUT. Derived, never declared."""
    unscanned = unscanned_markdown(root)
    directories = sorted({relative.split(os.sep)[0] for relative in unscanned}) or ["none"]
    shapes = shapes or {"titled": 0, "angleBracket": 0, "referenceStyle": 0}
    return ("# scope: ANY relative target kind{}; NOT scanned: {} .md outside gpd/ and the root "
            "({}); NOT matched: {} titled, {} angle-bracket, {} reference-style link(s)".format(
                "" if non_markdown is None else " ({} non-.md today)".format(non_markdown),
                len(unscanned), ", ".join(directories),
                shapes["titled"], shapes["angleBracket"], shapes["referenceStyle"]))


# --- the false-positive measurement (`T-313`) --------------------------------------------------
#
# `CLAUDE.md`: *a drift checker's false positives cost more than its true ones, because the tool
# exists in order to be believed*, and *an unmeasured false-positive rate is what makes a checker
# stop being believed*. A rate measured at `HEAD` measures nothing -- `HEAD` is a corpus somebody
# has just repaired. So the rate is measured over the repository's OWN HISTORY: the shipped
# predicate is replayed over every commit reachable from a revision, each commit's own tree
# supplying the existence set, and every distinct `(file, link)` pair it ever reported is printed
# for a human to read. Blobs are cached by SHA, so 256 commits cost about a minute rather than 256
# checkouts.
#
# Measured for the widening at `a166544`: 256 commits, 19 with at least one hit, **6** distinct
# pairs, **all six genuine dangling references and none a false positive** -- two harnesses moved
# out from under a link (`T-305`, `T-308`) and two artifacts named in a document one or eleven
# commits before they were added, which `C-0198` records as the gate working rather than a false
# positive (*"an artifact named and never added is exactly the defect, and the only thing
# distinguishing it from an in-flight one is time"*).


def tree_defects(files, exists, only_non_markdown=False):
    """[(path, link)] for every relative link in `files` absent from `exists`.

    `files` maps a repository-relative path to its text and `exists` is the set of paths present
    in that tree, including its directories. Pure, so the measurement can be tested without git.
    """
    found = []
    for path in sorted(files):
        directory = os.path.dirname(path)
        for link in relative_links_in(files[path]):
            if only_non_markdown and link.endswith(".md"):
                continue
            if os.path.normpath(os.path.join(directory, link)) not in exists:
                found.append((path, link))
    return found


def _in_scanned_scope(path):
    """The scope `tracked_markdown` would give this path -- `gpd/**/*.md` or a root document."""
    return path.endswith(".md") and ("/" not in path or path.startswith("gpd/"))


def history(root=ROOT, revision="HEAD", only_non_markdown=True):
    """Replay the predicate over every commit reachable from `revision`.

    Returns `(commits swept, commits with at least one hit, {(path, link): (n commits, first)})`.
    """
    def git(*arguments):
        return subprocess.run(["git"] + list(arguments), cwd=root,
                              capture_output=True, text=True, check=True).stdout

    blobs = {}
    pairs = {}
    swept = 0
    fired = 0
    for commit in git("rev-list", "--reverse", revision).split():
        swept += 1
        entries = {}
        for line in git("ls-tree", "-r", commit).splitlines():
            meta, path = line.split("\t", 1)
            _mode, kind, sha = meta.split()
            if kind == "blob":
                entries[path] = sha
        exists = set(entries)
        for path in list(exists):
            parts = path.split("/")
            for depth in range(1, len(parts)):
                exists.add("/".join(parts[:depth]))
        files = {}
        for path, sha in entries.items():
            if not _in_scanned_scope(path):
                continue
            if sha not in blobs:
                blobs[sha] = subprocess.run(["git", "cat-file", "blob", sha], cwd=root,
                                            capture_output=True, check=True
                                            ).stdout.decode("utf-8", "replace")
            files[path] = blobs[sha]
        hits = tree_defects(files, exists, only_non_markdown)
        if hits:
            fired += 1
        for pair in hits:
            count, first = pairs.get(pair, (0, commit))
            pairs[pair] = (count + 1, first)
    return swept, fired, pairs


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    # A sibling link resolves against the file's OWN directory -- the distinction the live defects
    # turned on, since the same slug can be valid in claims/ and absent from challenges/.
    check("a sibling link that exists is not reported",
          broken_links_in("see [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)",
                          "gpd/claims"), [])
    check("a sibling link that does not exist is reported",
          [l for l, _ in broken_links_in("[`C-0006`](C-0006-tile-flatness.md)", "gpd/claims")],
          ["C-0006-tile-flatness.md"])
    check("the SAME slug is judged against the directory it is written in",
          [l for l, _ in broken_links_in(
              "[`C-0006`](C-0006-tile-load-distribution-and-flatness.md)", "gpd/challenges")],
          ["C-0006-tile-load-distribution-and-flatness.md"])
    check("a sideways link that exists is not reported",
          broken_links_in("[`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md)",
                          "gpd/challenges"), [])
    check("a sideways link that does not exist is reported",
          [l for l, _ in broken_links_in("[`x`](../claims/C-9999-nope.md)", "gpd/challenges")],
          ["../claims/C-9999-nope.md"])
    check("a rootward link that exists is not reported",
          broken_links_in("[`TASKS.md`](../../TASKS.md)", "gpd/claims"), [])
    check("a rootward link that does not exist is reported",
          [l for l, _ in broken_links_in("[`x`](../../NOPE.md)", "gpd/claims")], ["../../NOPE.md"])
    # An anchor must not defeat the resolution.
    check("a link with an anchor still resolves",
          broken_links_in("[`x`](../../TASKS.md#start-here)", "gpd/claims"), [])
    # Absolute and external links are not this checker's business.
    check("an http link is ignored",
          broken_links_in("[`x`](https://example.org/C-0001-nope.md)", "gpd/claims"), [])
    check("an absolute path is ignored",
          broken_links_in("[`x`](/gpd/claims/C-9999-nope.md)", "gpd/claims"), [])
    # `T-313`. A target that is not a `.md` file is a cross-reference like any other, and until
    # this task the pattern ended `\.md` -- so a `.py`, `.kt`, `.json`, `.sh` or directory target
    # was invisible to every gate in this tree. Two agents relocated a mutation harness out from
    # under a Markdown link in ONE iteration and neither move was caught.
    check("a link to a `.py` file that exists is not reported",
          broken_links_in("[`h`](../../tools/check-corpus-links.py)", "gpd/claims"), [])
    check("a link to a `.py` file that does NOT exist is reported",
          [l for l, _ in broken_links_in("[`h`](../data/T-299-mutation/mutate.py)", "gpd/claims")],
          ["../data/T-299-mutation/mutate.py"])
    check("and from a root document too, which is where both live defects were",
          [l for l, _ in broken_links_in("[`h`](tools/T-999-mutation-test.py)", "")],
          ["tools/T-999-mutation-test.py"])
    check("a link to a result file that does not exist is reported",
          [l for l, _ in broken_links_in("[`r`](../results/T-9999-nope.json)", "gpd/claims")],
          ["../results/T-9999-nope.json"])
    check("a link to a Kotlin source that does not exist is reported",
          [l for l, _ in broken_links_in("[`k`](../../src/main/kotlin/Nope.kt)", "gpd/claims")],
          ["../../src/main/kotlin/Nope.kt"])
    # A DIRECTORY target resolves like any other path; 17 of the corpus's 594 non-`.md` targets
    # are one, and `os.path.exists` is the right test for both.
    check("a directory link that exists is not reported",
          broken_links_in("[`claims`](../claims/)", "gpd/challenges"), [])
    check("a directory link that does not exist is reported",
          [l for l, _ in broken_links_in("[`x`](../nowhere/)", "gpd/challenges")], ["../nowhere/"])
    # The guard the widening NEEDS. An anchor-only link points inside the file it is written in,
    # so it is not a path at all -- and the old pattern excluded it only as a side effect of
    # ending in `.md`. Three of them in the corpus.
    check("an anchor-only link is not a path",
          broken_links_in("[`x`](#the-lateral-mode-not-bounded-by-the-layer)", "gpd/claims"), [])
    check("prose naming a claim without linking it is ignored",
          broken_links_in("see C-0006-tile-flatness.md for the detail", "gpd/claims"), [])
    check("the corpus listing finds files", len(tracked_markdown()) > 0, True)
    # `T-313`, branch 2 -- THE SCOPE LINE, and it is MEASURED on every run rather than declared
    # once, because a declared list is a dated object. Two residues, both derivable: the link
    # SHAPES this pattern cannot see, and the Markdown files this checker does not scan.
    check("a link with a TITLE is a shape the pattern cannot see, and it is counted",
          unmatched_shapes('see [`x`](nope.py "a title")')["titled"], 1)
    check("a titled link is NOT reported as broken -- it is not matched at all",
          broken_links_in('see [`x`](nope.py "a title")', "gpd/claims"), [])
    check("an angle-bracket link is a shape the pattern cannot see, and it is counted",
          unmatched_shapes("see [`x`](<nope.py>)")["angleBracket"], 1)
    check("a reference-style link is a shape the pattern cannot see, and it is counted",
          unmatched_shapes("see [`x`][nope]\n\n[nope]: nope.py")["referenceStyle"], 1)
    check("an ordinary inline link is none of the three shapes",
          sum(unmatched_shapes("see [`x`](C-0006-tile-load-distribution-and-flatness.md)").values()),
          0)
    check("a shape inside code is not a shape either",
          sum(unmatched_shapes("`[x](nope.py \"t\")`").values()), 0)
    # `T-313`, the false-positive measurement. The pure half is tested here; the git plumbing
    # above it is a thin shell over `rev-list` / `ls-tree` / `cat-file`.
    check("a tree whose links all resolve has no defects",
          tree_defects({"TASKS.md": "[`h`](tools/h.py)"}, {"TASKS.md", "tools", "tools/h.py"}), [])
    check("a tree missing the target reports it",
          tree_defects({"TASKS.md": "[`h`](tools/h.py)"}, {"TASKS.md"}),
          [("TASKS.md", "tools/h.py")])
    check("a DIRECTORY present in the tree resolves",
          tree_defects({"a/b.md": "[`d`](../c/)"}, {"a/b.md", "c", "c/d.txt"}), [])
    check("the ADDED class alone can be measured, which is what the rate is about",
          tree_defects({"a.md": "[`x`](gone.md)[`y`](gone.py)"}, {"a.md"}, only_non_markdown=True),
          [("a.md", "gone.py")])
    check("and the scanned scope is gpd/ plus the root documents",
          [_in_scanned_scope(path) for path in
           ["TASKS.md", "gpd/claims/C-0001-x.md", "tools/C-0156-claim-template.md",
            "third-party/x.md", "gpd/results/T-1.json"]],
          [True, True, False, False, False])
    # `T-184`: the two outward-facing documents are IN the listing. They were not, and a broken
    # claim slug in the file NDI reads is worse than one in a claim, not better.
    check("the outward-facing set is DERIVED, and it carries the three deliverables",
          [name for name in ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md"]
           if name in root_documents()],
          ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md"])
    # `C-0160` found these two scanned by nothing; a derived set cannot lose them again.
    check("and the two documents a hand-written list had missed",
          [name for name in ["ARCHITECTURE.md", "CLAUDE.md"] if name in root_documents()],
          ["ARCHITECTURE.md", "CLAUDE.md"])
    # A link inside code is a DESCRIPTION of a link. `CLAUDE.md` quotes one verbatim, in the very
    # entry that records this checker's first blind spot.
    check("a link inside an inline code span is not a link",
          broken_links_in("a check that matched `](C-0006-nope.md)` and not ...", ""), [])
    check("a link inside a fenced block is not a link",
          broken_links_in("```\n[`x`](gpd/claims/C-0001-nope.md)\n```", ""), [])
    check("a real link whose TEXT is a code span is still a link",
          [l for l, _ in broken_links_in("[`C-0006`](gpd/claims/C-0006-nope.md)", "")],
          ["gpd/claims/C-0006-nope.md"])
    # On a SYNTHETIC root, so the check does not depend on this checkout: `tools/verify.sh` runs
    # these in a snapshot, and a self-test that reads the real tree is not a fixture test.
    import tempfile
    with tempfile.TemporaryDirectory() as fake:
        os.makedirs(os.path.join(fake, "gpd", "claims"))
        for name in ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md",
                     os.path.join("gpd", "claims", "C-0001-x.md")]:
            open(os.path.join(fake, name), "w").close()
        listed = tracked_markdown(root=fake)
        check("a root document is in the listing", "ANSWERS.md" in listed, True)
        check("and so is the decision file", "DECISIONS-FOR-NDI.md" in listed, True)
        check("the corpus is still listed beside them",
              os.path.join("gpd", "claims", "C-0001-x.md") in listed, True)
        check("the listing has no duplicates", len(listed), len(set(listed)))
        check("a root document that does not exist is not listed",
              tracked_markdown(root=os.path.join(fake, "gpd")), [])
        # `T-313`. The FILE SET is the residue the widening does not close, so the checker has
        # to be able to say how big it is. Hermetic: `tools/x.md` and `third-party/y.md` exist in
        # the fixture and are not scanned, so they are exactly what `unscanned_markdown` returns.
        os.makedirs(os.path.join(fake, "tools"))
        os.makedirs(os.path.join(fake, "third-party"))
        open(os.path.join(fake, "tools", "x.md"), "w").close()
        open(os.path.join(fake, "third-party", "y.md"), "w").close()
        check("Markdown outside the scanned set is COUNTED, not silently ignored",
              unscanned_markdown(root=fake),
              [os.path.join("third-party", "y.md"), os.path.join("tools", "x.md")])
        check("and nothing the checker DOES scan is counted as unscanned",
              [name for name in unscanned_markdown(root=fake)
               if name in set(tracked_markdown(root=fake))], [])
        check("the scope note names the unscanned count",
              "2 .md outside" in scope_note(root=fake), True)
        # `C-0161`: a mutation that fails nothing is the finding, so the scope line -- which is
        # OUTPUT and not a return value -- gets a named test that reads the output. Hermetic: the
        # synthetic root above, so this asserts the checker and not this checkout.
        captured = _io.StringIO()
        with _contextlib.redirect_stderr(captured):
            code = main([], root=fake)
        check("a clean synthetic corpus exits 0", code, 0)
        check("and the run PRINTS its scope beside the count",
              all(token in captured.getvalue()
                  for token in ["broken link(s)", "NOT scanned", "NOT matched"]), True)
        # And a defect in it exits 1, with the resolved path named.
        with open(os.path.join(fake, "gpd", "claims", "C-0001-x.md"), "w") as handle:
            handle.write("[`h`](../../tools/T-9999-mutation-test.py)\n")
        out = _io.StringIO()
        err = _io.StringIO()
        with _contextlib.redirect_stdout(out), _contextlib.redirect_stderr(err):
            code = main([], root=fake)
        check("a dangling NON-`.md` target exits 1", code, 1)
        check("and it is named on stdout with what it resolved to",
              "tools/T-9999-mutation-test.py" in out.getvalue(), True)
    # A root document's links resolve against the ROOT, not against `gpd/` -- the same one-resolver
    # rule the manifest defects taught, at the other end of the tree.
    check("a root-relative link from a root document resolves",
          broken_links_in("[`C-0006`](gpd/claims/C-0006-tile-load-distribution-and-flatness.md)", ""), [])
    check("and a mistyped one from a root document is reported",
          [l for l, _ in broken_links_in("[`C-0006`](gpd/claims/C-0006-tile-flatness.md)", "")],
          ["gpd/claims/C-0006-tile-flatness.md"])

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv, root=ROOT):
    if "--selftest" in argv:
        return _selftest()
    if "--history" in argv:
        swept, fired, pairs = history(root)
        for (path, link), (count, first) in sorted(pairs.items(), key=lambda kv: -kv[1][0]):
            print("{:>4} commit(s)\t{}\t{}\tfirst {}".format(count, path, link, first[:9]))
        print("# {} commit(s) swept, {} with at least one hit, {} distinct (file, link) pair(s)"
              .format(swept, fired, len(pairs)), file=sys.stderr)
        return 0
    defects = 0
    non_markdown = 0
    shapes = {"titled": 0, "angleBracket": 0, "referenceStyle": 0}
    files = tracked_markdown(root)
    for relative in files:
        path = os.path.join(root, relative)
        try:
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        for name, count in unmatched_shapes(text).items():
            shapes[name] += count
        non_markdown += sum(1 for link in relative_links_in(text) if not link.endswith(".md"))
        for link, resolved in broken_links_in(text, os.path.dirname(relative), root):
            print("{}\tBROKEN-LINK\t{}\t-> {}".format(relative, link, resolved))
            defects += 1
    sys.stdout.flush()
    print("# {} broken link(s) in {} file(s)".format(defects, len(files)), file=sys.stderr)
    # `T-313`, branch 2. The scope line runs on every invocation, beside the count, because a
    # clean run is otherwise read as a statement about the whole corpus. It is derived from the
    # same pass, so it costs one tree walk.
    print(scope_note(root, shapes=shapes, non_markdown=non_markdown), file=sys.stderr)
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
