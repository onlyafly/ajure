@echo off
set BASE_DIR=C:\Users\Kevin\code\ajure
set SRC_DIR=%BASE_DIR%\src
set LIBS_DIR=%BASE_DIR%\lib
set CLOJURE_JAR=%LIBS_DIR%\clojure-1.0.0.jar
set JLINE_JAR=%LIBS_DIR%\jline-0.9.94.jar
set SWT_JAR=%LIBS_DIR%\swt-win32-win32-x86_64-3.7.1-debug.jar

java -cp .;%SRC_DIR%;%CLOJURE_JAR%;%SWT_JAR% clojure.main src\ajure\start.clj