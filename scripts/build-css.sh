#!/bin/bash

# Compiles the LESS sources to resources/public/css/crt/crt.css and minifies
# it to crt.min.css (referenced by the prod handler in plawww.handler/main-page).
#
# Replaces `lein less once` + the lein-asset-minifier uberjar hook, both of
# which are broken on JDK 15+ (Nashorn removal / ancient core.async).
# Requires Node (npx); tool versions are pinned for reproducible output.
#
# Usage: scripts/build-css.sh [--watch]
#   --watch  recompile crt.css on changes under src/less (no minification)

set -e
cd "$(dirname "$0")/.."

SRC=src/less/crt/crt.less
OUT_DIR=resources/public/css/crt
OUT=$OUT_DIR/crt.css
MIN=$OUT_DIR/crt.min.css

mkdir -p "$OUT_DIR"

if [[ "$1" == "--watch" ]]; then
	echo "Watching src/less (crt.css only)..."
	exec npx --yes less-watch-compiler@1.16.3 --main-file crt.less src/less/crt "$OUT_DIR"
fi

echo "Compiling $SRC -> $OUT"
npx --yes less@4 "$SRC" "$OUT"

echo "Minifying $OUT -> $MIN"
npx --yes clean-css-cli@5 -o "$MIN" "$OUT"

echo "Done: $(du -h "$OUT" | cut -f1) crt.css, $(du -h "$MIN" | cut -f1) crt.min.css"
