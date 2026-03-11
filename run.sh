#!/bin/sh

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

javac -encoding UTF-8 -cp "$SCRIPT_DIR/lib/*" @"$SCRIPT_DIR/compile.list" -d "$SCRIPT_DIR/class" || exit $?

cd "$SCRIPT_DIR/class" || exit $?
java -cp ".:$SCRIPT_DIR/lib/*" Main