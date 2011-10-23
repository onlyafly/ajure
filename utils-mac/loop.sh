#!/bin/bash

SRC_DIR=~/src
CLOJURE_JAR=$SRC_DIR/clojure/clojure.jar
JLINE_JAR=$SRC_DIR/jline-0.9.94/jline-0.9.94.jar
SWT_JAR=$SRC_DIR/swt-3.4-mac/swt-debug.jar

java \
  -XstartOnFirstThread \
  -cp .:$JLINE_JAR:$CLOJURE_JAR:$SWT_JAR \
  jline.ConsoleRunner \
  clojure.main \
  "$@"