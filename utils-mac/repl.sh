#!/bin/bash

LIB_DIR=lib
SRC_DIR=src
RES_DIR=resources
CLOJURE_JAR=$LIB_DIR/clojure-1.4.0.jar
SWT_JAR=$LIB_DIR/swt-4.2.1-cocoa-macosx-x86_64.jar

java \
  -XstartOnFirstThread \
  -cp .:$SRC_DIR:$RES_DIR:$CLOJURE_JAR:$SWT_JAR \
  clojure.main \
  "$@"
