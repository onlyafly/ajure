#!/bin/bash

SRC_DIR=~/src
CLOJURE_JAR=$SRC_DIR/clojure/clojure.jar
SWT_JAR=$SRC_DIR/swt-3.4-mac/swt-debug.jar
#SWT_JAR=$SRC_DIR/swt-3.5M4-carbon-macosx/swt-debug.jar
#SWT_JAR=$SRC_DIR/swt-3.5M4-cocoa-macosx/swt-debug.jar

java \
  -agentpath:/Applications/YourKit_Java_Profiler_8.0.1.app/bin/mac/libyjpagent.jnilib \
  -XstartOnFirstThread \
  -cp .:$CLOJURE_JAR:$SWT_JAR \
  clojure.main \
  ajure/start.clj \
  "$@"
