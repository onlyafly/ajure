;; window

(ns ajure.ui.window
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [file-utils :as file-utils]
                        [text :as text]                        
                        )
            (ajure.ui [editors :as editors]
                      [file-tree :as file-tree]
                      [info-dialogs :as info-dialogs]
                      [project :as project]
                      [sash-form :as sash-form]
                      [scripts :as scripts]
                      [status-bar :as status-bar]
                      [tabs :as tabs]
                      [text-editor :as text-editor]
                      [undo :as undo])
            (ajure.state [hooks :as hooks]
                         [doc-state :as doc-state])
            (ajure.cwt [display :as display]
                       [resources :as resources]
                       [shell :as shell])
            (ajure.util [platform :as platform]
                        [swt :as swt]
                        [text-format :as text-format]))
  (:use (ajure.ui [access :only (def-menu def-append-sub-menu remove-menu-children)])
        ajure.util.other))

;;---------- Private

(defn- toggle-word-wrap []
  (let [new-state (not (@hooks/settings :word-wrap-enabled))]
    (editors/set-current-word-wrap new-state)))

(defn- do-choose-font []
  (let [original @hooks/editor-font-data
        updated (swt/show-font-dialog! @hooks/shell
                                       "Choose Editor Font"
                                       original)]
    (when updated
      (editors/set-editor-font-data updated)
      (editors/update-settings-from-editor-font))))

(defn- verify-everything-saved-before-action [action]
  (tabs/verify-all-tabs-saved-before-action!
   #(project/verify-project-saved-before-action action)))

;; Program exit point, called by Exit menu item
(defn- do-exit []
  (verify-everything-saved-before-action #(.dispose @hooks/shell)))

(defn- on-before-history-change! []
  (text-editor/pause-change-listening! (doc-state/current :text-box))
  )

(defn- on-after-history-change! []
  (text-editor/resume-change-listening! (doc-state/current :text-box)
                                        tabs/on-text-box-change!)
  )

(defn- on-double-click-file-in-tree! [file-object]
  (io!
   (tabs/open-file-in-new-tab! (.getPath file-object))))

(defn- attach-edit-popup-menu-items! [parent-menu]
  (io!
   (swt/create-menu-item! parent-menu "Undo"
                          #(undo/do-undo! (doc-state/current :text-box)
                                         on-before-history-change!
                                         on-after-history-change!))
   (swt/create-menu-item! parent-menu "Redo"
                          #(undo/do-redo! (doc-state/current :text-box)
                                         on-before-history-change!
                                         on-after-history-change!))
   (swt/create-menu-separator! parent-menu)
   (swt/create-menu-item! parent-menu "Cut"
                          text/cut-text!)
   (swt/create-menu-item! parent-menu "Copy"
                          text/copy-text!)
   (swt/create-menu-item! parent-menu "Paste"
                          text/paste-text!)
   (swt/create-menu-separator! parent-menu)
   (swt/create-menu-item! parent-menu "Select All"
                          text/select-all-text!)
   
   nil))

(defn- make-popup-menu! [shell]
  (io!
   (let [popup-menu (swt/create-popup-menu! shell)]
     (attach-edit-popup-menu-items! popup-menu)
     (.setMenu shell popup-menu)
     popup-menu)))

(defn- make-menu-bar! [shell]
  (io!
   (let [menu-bar (swt/create-menu-bar! shell)]
     (.setMenuBar shell menu-bar)
     menu-bar)))

(defn- setup-key-combos! []
  ;; Setup key combos here that are not tied to menus
  nil)

(defn- setup-menus! []  
  (def-menu "File"
    (:app-combo "New"
                [MOD1] \n
                (tabs/new!))
    (:app-combo "Open"
                [MOD1] \o
                (tabs/open!))
    (:app-combo "Save"
                [MOD1] \s
                (tabs/save!))
    (:item "Save As..."
           (tabs/save-as!))
    (:app-combo "Save All"
                [SHIFT MOD1] \s
                (tabs/save-all!))
    (:app-combo "Close Tab"
                [MOD1] \w
                (tabs/verify-tab-saved-and-close!))
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

  (tabs/update-recent-files-menu!)
  (project/update-recent-projects-menu)

  (def-menu "Edit"
    (:editor-combo "Undo"
                   [MOD1] \z
                   (undo/do-undo! (doc-state/current :text-box)
                                  on-before-history-change!
                                  on-after-history-change!))
    (:editor-combo "Redo"
                   [SHIFT MOD1] \z
                   (undo/do-redo! (doc-state/current :text-box)
                                  on-before-history-change!
                                  on-after-history-change!))
    (:sep)
    (:editor-combo "Cut"
                   [MOD1] \x
                   (text/cut-text!))
    (:editor-combo "Copy"
                   [MOD1] \c
                   (text/copy-text!))
    (:editor-combo "Paste"
                   [MOD1] \v
                   (text/paste-text!))
    (:sep)
    (:editor-combo "Select All"
                   [MOD1] \a
                   (text/select-all-text!))
    (:sep)
    (:cascade "Convert File Endings"
              (:item "CRLF (Windows)"
                     (tabs/change-current-tab-line-endings!
                      text-format/line-ending-crlf))
              (:item "LF (Unix)"
                     (tabs/change-current-tab-line-endings!
                      text-format/line-ending-lf))
              (:item "CR (Mac Classic)"
                     (tabs/change-current-tab-line-endings!
                      text-format/line-ending-cr))))

  (text/build-text-menu!)

  (def-menu "Settings"
    (:item "Editor Font..."
           (do-choose-font))
    (:item "Choose Startup Script..."
           (file-utils/choose-startup-script))
    (:item "Toggle Word Wrap"
           (toggle-word-wrap)))
  
  (def-menu "Script"
    (:editor-combo "Run This Document"
                   [MOD1] \r
                   (scripts/run-doc)))
  
  (def-menu "Help"
    (:item "About Ajure"
           (info-dialogs/show-about-box!))
    (:item "Open Error Log"
           (tabs/open-file-in-new-tab! file-utils/error-log-file-path))))

;;---------- Public

(defn show! [{main-shell :shell}]
  (shell/show! main-shell))

(defn make! [& {:keys [display
                       title
                       icon
                       on-quit-should-close?]}]
  (let [main-shell (shell/make! :display display
                                :title title
                                :icon icon
                                :size [880 700]
                                :on-quit-should-close? on-quit-should-close?)
        sash-form-layout-data (GridData. SWT/FILL SWT/FILL true true)
        sash-form-map (sash-form/make! :parent main-shell
                                       :layout-data sash-form-layout-data
                                       :on-double-click-file-in-tree on-double-click-file-in-tree!
                                       :on-close-tab tabs/verify-tab-saved-then-close!?
                                       :on-close-last-tab tabs/open-blank-file-in-new-tab!
                                       :on-tab-selected tabs/tab-selected-action!
                                       )
        status-bar-layout-data (let [data (GridData. SWT/FILL SWT/END true false)]
                                 ;; This would allow the item to span 2 columns
                                 ;; (set! (. data horizontalSpan) 2)
                                 data)
        status-bar-map (status-bar/make! :shell main-shell
                                         :layout-data status-bar-layout-data)
        popup-menu (make-popup-menu! main-shell)
        menu-bar (make-menu-bar! main-shell)]

    ;;----- Actions directly on locals
    
    (swt/center-shell! display main-shell)
    (swt/add-file-dropping-to-control! (:tab-folder sash-form-map)
                                       tabs/open-file-paths-in-tabs!)
    
    (file-tree/show-file-tree! (:sash-form sash-form-map)
                               (:tab-folder sash-form-map)
                               false)
    
    ;;----- Setup globals
    
    (dosync
     (ref-set hooks/display display)
     (ref-set hooks/shell main-shell)
     (ref-set hooks/status-bar-widget (:canvas status-bar-map))
     (ref-set hooks/app-status-label (:app-label status-bar-map))
     (ref-set hooks/doc-status-label (:doc-label status-bar-map))
     (ref-set hooks/sash-form (:sash-form sash-form-map))
     (ref-set hooks/tab-folder (:tab-folder sash-form-map))
     (ref-set hooks/file-tree (:file-tree sash-form-map))
     (ref-set hooks/menu-bar menu-bar)
     (ref-set hooks/popup-menu popup-menu))
    
    ;;----- Actions on globals
    
    (setup-key-combos!)
    (setup-menus!)

    ;; Note that java.io.File does not understand that "~"
    ;; equals home directory
    (if *command-line-args*
      (tabs/open-file-paths-in-tabs! *command-line-args*))
    
    (tabs/open-blank-file-in-new-tab!)
    
    {:shell main-shell}))