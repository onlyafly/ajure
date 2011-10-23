#!/bin/bash

SRC_DIR=~/src
CLOJURE_JAR=$SRC_DIR/clojure/clojure.jar
SWT_JAR=$SRC_DIR/swt-3.4-mac/swt-debug.jar

java \
  -cp .:$CLOJURE_JAR:$SWT_JAR \
  clojure.main \
  ajure/start.clj \
  "$@"