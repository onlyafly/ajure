# Ajure Version History / Changelog

_These are the notes for each release._

## Planned for Version 0.5

* Release Date: 2011-11-05
* Version Goal: Customization and scripting improvements.

Windows OS Integration

* Icon
* Easy installation and usage on Windows

User Interface

* File Menu
 * List of recent files and projects
* Text Menu
 * Change selected text to lower or upper case

Customization Support for User Scripts

* Menus
* Key combos

Configurations

* Configurations auto-saved when program closes
* Remove "Save Current Configurations" menu item
* "Configure" menu now called "Settings"

Module Support

* Refactor code to make future GUI components more modular
* Modules
 * Find/Replace Bar

Bugs Fixed

* Paste keyboard shortcut performed the action twice

Technical

* Settings file renamed and stores settings as a map.

## Version 0.4

* Release Date: March 1, 2009
* Code Revision: 238
* Version Goal: Document editing improvements.

Document Editing

* Support for different text character sets such as UTF-16 (see [http://code.google.com/p/ajure/issues/detail?id=11 Issue #11])
* Line ending type support
  * Status bar displays line endings of current document
  * Edit menu options to change line ending type

Current Document Search

* Find Bar
  * Supports Firefox-style highlighting of matches
  * Supports toggling of case-sensitive and case-insensitive searching

User Interface

* File menu
  * Project menu merged into File menu
* Edit menu
  * "Redo" item
  * "Find Bar" item
* Improved choice of default fonts for each platform
* Status bar
  * Display document line-ending type
  * Display document character set type
* Logo displayed in the window in Windows

Bug Fixes

* Trailing newline no longer added when opening a file
* File dialogs now default to the last accessed directory
* Dragging a file onto the text editor would cause the program to crash later
* "Undo" could undo the initial opening of a file

## Version 0.3

* Release Date: February 14, 2009
* Code Revision: 183
* Version Goal: Multiple document support feature complete.

Multiple Document Editing

* Tree-view of current project directory
* Project files

File Management

* Drag and drop: dropping a file on the editor opens the document
* Command-line arguments for opening a file
* Save All menu item in File menu

User Interface

* Application-wide keyboard shortcuts for non-text-specific commands (see [http://code.google.com/p/ajure/issues/detail?id=7 Issue #7])

Help Menu

* Menu item to display the error log in a new window

Technical Aspects

* Refactor code

## Version 0.2.1

* Release Date: February 4, 2009
* Code Revision: 131

Multiple Document Editing

* Tabs for multiple document editing
* Keyboard Shortcuts
  * Close current tab

Editing

* Line numbering (see [http://code.google.com/p/ajure/issues/detail?id=2 Issue #2])
* Word wrapping
  * Toggling
  * Line numbering that takes wrapping into account
* Popup menu on right click (see [http://code.google.com/p/ajure/issues/detail?id=2 Issue #2])

User Interface

* About Dialog
  * More complete About dialog

## Version 0.2

* Release Date: January 26, 2009
* Code Revision: 78

User Interface

* New icon on Mac
* Platform specific compatibility
  * "Quit" in Mac application menu that prompts for unsaved changes
  * File -> Exit menu item not displayed on Mac
* Center window on screen on opening
* File menu
  * Open and Save dialogs default to the home directory
  * Keyboard shortcuts for New, Open, and Save menu options

Scripts

* Configuration script stored in home directory (ajure_config.clj)
  * Create an empty one if not present on launch
  * Auto-load on launch
  * Ability to save configurations to it
* Customization script chosen by user
* Ability to run the current document as a script

Configure Menu

* "Choose Font"
* "Choose Customizations Script" to choose a customized script to run at start up
* "Save Current Configurations"

Other

* Error logging

## Version 0.1

* Release Date: January 21, 2009
* Code Revision: 51

Customizations

* Font choosing

File Handling

* Prompt to save modified file on close
* File dialogs should preserve last directory
* Common file operations
  * New
  * Open...
  * Save, Save As...

Edit Menu

* Common Windows and Mac keyboard shortcuts for:
  * Copy, Cut, Paste, Select All
  * Undo

Other

* About dialog
* Status bar
  * Display key combos
