# Ajure

Ajure is a highly customizable programmers' text editor.

## Installing

If there is a compiled download for your operating system, you can download it from https://github.com/onlyafly/ajure/downloads

After downloading and uncompressing the archive to a folder, execute the appropriate "start" file for your system (start.bat for Windows, etc.).

## Running from Source (Option 1: Using Leiningen)

### Run

`lein run`

### Start the REPL

`lein repl`

### Build an Uberjar

Using Leiningen, we can create a single standalone JAR
which can be executed.

`lein uberjar`

### Running Unit Tests

1. Run: `lein test`

## Running from Source (Option 2: Manually)

### Step 1: Download third-party libraries

Create a lib directory in Ajure's root folder. Then download the
following JARs and place them in lib:

clojure-1.4.0.jar:
http://clojure.org/downloads

(On Windows) swt-3.5.2-windows.jar:
http://www.eclipse.org/swt/

(On Mac) swt-4.2.1-cocoa-macosx-x86_64.jar:
http://www.eclipse.org/swt/

### Step 2: Compile

### Step 3a: Run

`run.bat`

### Step 3b: Start the REPL

`repl.bat`

### Step 3b: Start the REPL in Debug Mode

`repl-debug.bat`

### Running Ajure's Unit Tests

## Debugging

#### From the REPL:

1. Start the REPL (see one of the "Start the REPL" sections above).
2. In order to enter Clojure statements while the GUI is running, you will need to start
the GUI in a separate thread, using the following commands:
 * `(use 'ajure.repl)` or `(use 'ajure.repl :reload-all)`
 * `(thread-gui)`
3. You can now execute commands in the GUI's thread by using the `exec` and `pexec` forms. Example:
```clojure
(use 'ajure.public)
(exec (show-msg-box "foo" "bar"))
```
4. If a exception occurs, you can view its stacktrace like this:
```clojure
(use 'clojure.stacktrace)
(print-stack-trace *e)
```

#### Using a visual debugger:

1. Start the REPL in debug mode (see one of the "Start the REPL in Debug Mode" sections above). Either one of these commands will start the JVM as a debug server.
2. Follow the instructions on [Debugging Clojure](http://dev.clojure.org/display/doc/Debugging) to use a visual debugger like JSwat.
3. You can now start Ajure using the commands in step 2 in the "From the REPL" section, above.

## License

Copyright (C) 2009-2012 Kevin P. Albrecht

Distributed under the New BSD License (http://www.opensource.org/licenses/bsd-license.php). See included LICENSE.txt file for details.
