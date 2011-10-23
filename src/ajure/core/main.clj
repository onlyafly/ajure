;; ajure.core.main

(ns ajure.core.main
  (:import (org.eclipse.swt SWT))
  (:require (ajure.core [settings :as settings]
                        [file-utils :as file]
                        [window :as window]
                        [tabs :as tabs]
                        [undo :as undo]
                        [document :as document]
                        [scripts :as scripts]
                        [project :as project]
                        [editors :as editors]
                        [text :as text])
            (ajure.gui [application :as application]
                       [fonts :as fonts]
                       [hooks :as hooks]
                       [resources :as resources]
                       [status-bar :as status-bar]
                       [text-editor :as text-editor]
                       [info-dialogs :as info-dialogs]
                       [access :as access])
            (ajure.util [swt :as swt]
                        [platform :as platform]
                        [text-format :as text-format]))
  (:use (ajure.gui [access :only (def-menu def-append-sub-menu
                                   remove-menu-children)])
        ajure.util.other))

(defn verify-everything-saved-before-action [action]
  (tabs/verify-all-tabs-saved-before-action
   #(project/verify-project-saved-before-action action)))

;; Called immediately before program closes
(defn on-program-closing []
  (settings/save-current-settings))

;; Program exit point, called by Exit menu item
(defn do-exit []
  (verify-everything-saved-before-action #(.dispose @hooks/shell)))

;; Program exit point, called when using Application->Quit or Cmd+Q on Mac
(defn application-close-action [event]
  (let [should-close (window/verify-everything-saved-then-close?)]
    (set! (. event doit) should-close)))

(defn application-key-down-action [event]
  (swt/execute-key-combo-in-mappings event @access/application-key-combos
                                     #(do
                                        ; Consume the event
                                        (set! (. event doit) false)
                                        ; Cancel the event
                                        (set! (. event type) SWT/None))))

(defn on-before-history-change []
  (text-editor/pause-change-listening (document/this :textbox)))

(defn on-after-history-change []
  (text-editor/resume-change-listening (document/this :textbox)
                                       tabs/on-text-box-change))

(defn toggle-word-wrap []
  (let [new-state (not (@hooks/settings :word-wrap-enabled))]
    (editors/set-current-word-wrap new-state)))

(defn do-choose-font []
  (let [original @hooks/editor-font-data
        updated (swt/show-font-dialog @hooks/shell
                                      "Choose Editor Font"
                                      original)]
    (when updated
      (editors/set-editor-font-data updated)
      (editors/update-settings-from-editor-font))))

(defn setup-key-combos []
  ;; Setup key combos here that are not tied to menus
  nil)

(defn setup-menus []  
  (def-menu "File"
    (:app-combo "New"
                [MOD1] \n
                (tabs/do-new))
    (:app-combo "Open"
                [MOD1] \o
                (tabs/do-open))
    (:app-combo "Save"
                [MOD1] \s
                (tabs/do-save))
    (:item "Save As..."
           (tabs/do-save-as))
    (:app-combo "Save All"
                [SHIFT MOD1] \s
                (tabs/do-save-all))
    (:app-combo "Close Tab"
                [MOD1] \w
                (tabs/verify-tab-saved-and-close))
    (:sep)
    (:item "New Project"
           (project/do-new-project))
    (:item "Open Project"
           (project/do-open-project))
    (:item "Save Project"
           (project/do-save-project))
    (:item "Save Project As..."
           (project/do-save-project-as))
    (:item "Close Project"
           (project/do-close-project))

    (:sep)
    (:cascade "Recent Files")
    (:cascade "Recent Projects")

    (:cond (not platform/is-mac-os)
           (:sep)
           (:item "Exit"
                  (do-exit))))

  (tabs/update-recent-files-menu)
  (project/update-recent-projects-menu)

  (def-menu "Edit"
    (:editor-combo "Undo"
                   [MOD1] \z
                   (undo/do-undo (document/this :textbox)
                                 on-before-history-change
                                 on-after-history-change))
    (:editor-combo "Redo"
                   [SHIFT MOD1] \z
                   (undo/do-redo (document/this :textbox)
                                 on-before-history-change
                                 on-after-history-change))
    (:sep)
    (:editor-combo "Cut"
                   [MOD1] \x
                   (text/do-cut-text))
    (:editor-combo "Copy"
                   [MOD1] \c
                   (text/do-copy-text))
    (:editor-combo "Paste"
                   [MOD1] \v
                   (text/do-paste-text))
    (:sep)
    (:editor-combo "Select All"
                   [MOD1] \a
                   (text/do-select-all-text))
    (:sep)
    (:cascade "Convert File Endings"
              (:item "CRLF (Windows)"
                     (tabs/change-current-tab-line-endings
                      text-format/line-ending-crlf))
              (:item "LF (Unix)"
                     (tabs/change-current-tab-line-endings
                      text-format/line-ending-lf))
              (:item "CR (Mac Classic)"
                     (tabs/change-current-tab-line-endings
                      text-format/line-ending-cr))))

  (text/build-text-menu)

  (def-menu "Settings"
    (:item "Editor Font..."
           (do-choose-font))
    (:item "Choose Startup Script..."
           (file/choose-startup-script))
    (:item "Toggle Word Wrap"
           (toggle-word-wrap)))
      
  (def-menu "Script"
    (:editor-combo "Run This Document"
                   [MOD1] \r
                   (scripts/run-document)))
      
  (def-menu "Help"
    (:item "About Ajure"
           (info-dialogs/show-about-box))
    (:item "Open Error Log"
           (tabs/open-file-in-new-tab file/error-log-file-path))))

;;---------- Main entry point to application

; Action to take on display disposal
(defn release-action []
  (on-program-closing)
  (resources/release-all))

; Action to take on main loop exception
(defn exception-action [exception]
  (status-bar/set-message 
   (str "Error occured. For details, view error log at "
        "<" file/error-log-file-path ">"))
  (file/log-exception exception)
  ;;FIXME remove throws before deployment so that exceptions are caught
  (throw exception)
  )

(defn launch-gui [modules-init-action]
  (try
    (let [display (application/create-display application-close-action
                                              application-key-down-action)]

      ;; Settings should be run first so that the GUI has the settings
      ;; available when it is rendered
      (settings/load-settings)

      (resources/allocate-all display)

      (let [shell (window/show-window display)]
        (setup-key-combos)
        (setup-menus)

        (modules-init-action)

        ;; Update the GUI from settings where applicable here
        (editors/update-editor-font-from-settings)
        
        ;; Run the custom script here so that the script can modify the GUI
        ;; if desired
        (when (str-not-empty? (@hooks/settings :custom-script-file-path))
          (scripts/try-load-file (@hooks/settings :custom-script-file-path)))

        (swt/basic-loop display shell release-action exception-action)))
    (catch Exception exc
      (file/log-exception exc)
      ;;FIXME remove throws before deployment so that exceptions are caught
      (throw exc)
      )))
