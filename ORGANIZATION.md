# Ajure Package and Module Organization

_Describes the organization of the project source files._

## General Rules

1. Nothing should call anything in a package above itself in this hierarchy.
2. State should be contained in this package only:
   * ajure.state - for state of the application's data

## Description of Packages and Namespaces (experimental branch)

### ajure

_Entry points into application._

* **main** - The main entry point to the application.
* **public** - Simple public API.
* **repl** - Allows the application to be started from the REPL.
* **start** - Starts the application as a Clojure script.
* **default-modules** - Initialization of default modules.

### ajure.module

_Modules._

### ajure.core

_Major application logic. Should not use/import any SWT libraries directly.
As much as possible, this layer should behave functionally (avoid modifying
state)._

* **application** - Application start-up and initialization
* **info** - Informational resource strings

### ajure.ui

_Combination of CWT functionality with state._

* **window** - The window
* **status-bar** - The main status bar

### ajure.state

_State of the application._

* **hooks** - References to global state
* **doc** - Document structure
* **doc-state** - State related to open documents

### ajure.cwt

_A generalized wrapper around SWT. No Ajure-specific functionality nor ties to
application state._

* **display** - A display represents an application
* **resources** - Management of resources.
* **shell** - Shell (window) wrapper

### ajure.util

_Functionality non-specific to Ajure or resource strings._

* **io** - File operations
* **mac** - Mac-specific functionality
* **other** - Utility functions
* **platform** - Manage OS differences
* **queue** - Functions on vectors as queues
* **swt** - Basic SWT functionality
* **text-format** - Line-ending and charset support

## Description of Packages and Namespaces (master branch)

### ajure

_Entry points into application._

* **main** - The main entry point to the application.
* **public** - Simple public API.
* **repl** - Allows the application to be started from the REPL.
* **start** - Starts the application as a Clojure script.
* **default-modules** - Initialization of default modules.

### ajure.module

_Modules._

* **replace-bar** - UI for search and replace.
* **syntax-highlighting** - Support for syntax highlighting.

### ajure.core

_Major application logic. Should not use/import any SWT libraries directly.
As much as possible, this layer should behave functionally (avoid modifying
state)._

* **editors** - Editor settings (font, word wrap, etc.)
* **file-utils** - Error logging & startup script support
* **project** - Project support
* **recent** - Support for recent files and projects
* **scripts** - Support for executing scripts
* **settings** - Manage settings file
* **tabs** - Manage tabs
* **text** - Text manipulation operations
* **undo** - Undo/redo support
* **window** - Actions on the window

### ajure.state

_State of the application._

* **hooks** - References to global state
* **doc** - Document structure
* **doc-state** - State related to open documents

### ajure.gui (being refactored out)

_GUI abstraction layer. Wraps all SWT-specific functionality._

* **access** - Key-combo mappings, menu-items, and menu-item accelerators
* **file-dialogs** - File dialog wrappers
* **file-tree** - File tree used by projects
* **find-dialog** - NOT CURRENTLY USED
* **fonts** - Font support wrapper
* **info-dialogs** - Informational dialog wrappers
* **sash-form** - Form used by file tree in projects
* **search-text-box** - Search text box wrapper
* **status-bar** - Status bar wrapper
* **tab** - Tab wrapper
* **tab-folder** - Tab folder wrapper
* **text-editor** - Text editor wrapper
