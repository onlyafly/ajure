#!/bin/bash

CLOJURE_JAR_SRC=../../clojure/clojure.jar
SWT_JAR_SRC=../../swt-3.4-mac/swt.jar
BASE_DIR=..
DEPLOY_DIR=.
TEMP_DIR=Temp
DMG_NAME=Ajure_0.4.dmg

INSTALL_SRC_DIR=AjureInstall
INSTALL_DIR=$TEMP_DIR/AjureInstall

CODE_SRC_DIR=$BASE_DIR/ajure
CODE_DEST_DIR=$INSTALL_DIR/Ajure.app/Contents/MacOS

# Copy the source install dir to the temporary one
mkdir $TEMP_DIR
cp -R $INSTALL_SRC_DIR $TEMP_DIR

# Copy the latest license and readme files
cp $BASE_DIR/license.txt $INSTALL_DIR
cp $BASE_DIR/readme.txt $INSTALL_DIR

# Copy the necessary resources
cp $BASE_DIR/logo.png $CODE_DEST_DIR

# Copy the latest source tree
cp -R $CODE_SRC_DIR $CODE_DEST_DIR

# Copy the swt and clojure jar files
cp $CLOJURE_JAR_SRC $CODE_DEST_DIR/clojure.jar
cp $SWT_JAR_SRC $CODE_DEST_DIR/swt-3.4-mac.jar

# Remove all of the source control directories and their children
find $TEMP_DIR/* -name .svn -exec rm -fR {} \;

# Create the DMG
hdiutil create -srcfolder $INSTALL_DIR $DMG_NAME
hdiutil internet-enable -yes $DMG_NAME

# Remove the created files
rm -fR $TEMP_DIR