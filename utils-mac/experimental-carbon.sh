#!/bin/bash

SRC_DIR=~/src
CLOJURE_JAR=$SRC_DIR/clojure/clojure.jar
SWT_JAR=$SRC_DIR/swt-3.5M5-carbon-macosx/swt-debug.jar

java \
  -XstartOnFirstThread \
  -cp .:$CLOJURE_JAR:$SWT_JAR \
  clojure.main \
  ajure/start.clj \
  "$@"
