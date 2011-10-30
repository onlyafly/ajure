@echo off
set BASE_DIR=C:\Users\Kevin\code\ajure
set SRC_DIR=%BASE_DIR%\src
set LIBS_DIR=%BASE_DIR%\lib
set CLOJURE_JAR=%LIBS_DIR%\clojure-1.3.0.jar
set JLINE_JAR=%LIBS_DIR%\jline-0.9.94.jar
set SWT_JAR=%LIBS_DIR%\swt-win32-win32-x86_64-3.7.1-debug.jar

java -cp .;%SRC_DIR%;%JLINE_JAR%;%CLOJURE_JAR%;%SWT_JAR% jline.ConsoleRunner clojure.main