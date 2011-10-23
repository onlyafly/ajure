#!/bin/bash

SRC_DIR=~/src
CLOJURE_JAR=$SRC_DIR/clojure/clojure.jar
SWT_JAR=$SRC_DIR/swt-3.4-mac/swt-debug.jar
#SWT_JAR=$SRC_DIR/swt-3.5M4-carbon-macosx/swt-debug.jar
#SWT_JAR=$SRC_DIR/swt-3.5M4-cocoa-macosx/swt-debug.jar

java \
  -XstartOnFirstThread \
  -cp .:$CLOJURE_JAR:$SWT_JAR \
  clojure.main \
  ajure/start.clj \
  "$@"
