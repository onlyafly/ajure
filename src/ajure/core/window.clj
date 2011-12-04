;; ajure.core.window
;;
;; Should not:
;;  - Call SWT directly

(ns ajure.core.window
  (:require (ajure.core [undo :as undo]
                        [scripts :as scripts]
                        [file-utils :as file]
                        [tabs :as tabs]
                        [project :as project]
                        [editors :as editors]
                        [text :as text])
            (ajure.gui [access :as access]
                       [info-dialogs :as info-dialogs]
                       [file-tree :as file-tree]
                       [sash-form :as sash-form]
                       [status-bar :as status-bar]
                       [fonts :as fonts]
                       [resources :as resources]
                       [shell :as shell]
                       [text-editor :as text-editor])
            (ajure.state [doc-state :as doc-state]
                         [hooks :as hooks])
            (ajure.util [platform :as platform]
                        [swt :as swt]
                        [other :as other]
                        [info :as info]))
  (:use (ajure.util other)))

(defn verify-everything-saved-then-close? []
  (and (tabs/verify-all-tabs-saved-then-close?)
       (project/verify-project-saved-then-close?)))

(defn on-before-history-change []
  (text-editor/pause-change-listening! (doc-state/current :text-box)))

(defn on-after-history-change []
  (text-editor/resume-change-listening! (doc-state/current :text-box)
                                        tabs/on-text-box-change))

(defn create-edit-popup-menu-items [parent-menu]
  (vector
   (swt/create-menu-item! parent-menu "Undo"
                          #(undo/do-undo (doc-state/current :text-box)
                                         on-before-history-change
                                         on-after-history-change))
   (swt/create-menu-item! parent-menu "Redo"
                          #(undo/do-redo (doc-state/current :text-box)
                                         on-before-history-change
                                         on-after-history-change))
   (swt/create-menu-separator! parent-menu)
   (swt/create-menu-item! parent-menu "Cut"
                          text/do-cut-text)
   (swt/create-menu-item! parent-menu "Copy"
                          text/do-copy-text)
   (swt/create-menu-item! parent-menu "Paste"
                          text/do-paste-text)
   (swt/create-menu-separator! parent-menu)
   (swt/create-menu-item! parent-menu "Select All"
                          text/do-select-all-text)))

(defn create-popup-menu [shell]
  (let [menu (swt/create-popup-menu! shell)
        menu-items (create-edit-popup-menu-items menu)]
    (.setMenu shell menu)
    (dosync (ref-set hooks/popup-menu menu))))

(defn create-menu-bar [shell]
  (let [menu-bar (swt/create-menu-bar! shell)]
    (dosync (ref-set hooks/menu-bar menu-bar))
    (.setMenuBar shell menu-bar)
    menu-bar))

(defn on-double-click-file-in-tree [file-object]
  (tabs/open-file-in-new-tab (.getPath file-object)))

(defn- do-hookup-shell-controls
  [{shell :shell
    status-bar :status-bar
    app-label :app-label
    doc-label :doc-label
    tab-folder :tab-folder
    sash-form :sash-form
    file-tree :file-tree}]
  
  (dosync
   (ref-set hooks/shell shell)
   (ref-set hooks/status-bar status-bar)
   (ref-set hooks/app-status-label app-label)
   (ref-set hooks/doc-status-label doc-label)
   (ref-set hooks/sash-form sash-form)
   (ref-set hooks/tab-folder tab-folder)
   (ref-set hooks/file-tree file-tree)))

(defn show-window [display]
  (let [shell-controls (shell/create-shell! display
                                            on-double-click-file-in-tree
                                            tabs/verify-tab-saved-then-close?
                                            tabs/open-blank-file-in-new-tab
                                            tabs/tab-selected-action
                                            create-popup-menu
                                            create-menu-bar
                                            verify-everything-saved-then-close?)
        shell (:shell shell-controls)
        tab-folder (:tab-folder shell-controls)
        sash-form (:sash-form shell-controls)]

    ;;----- Actions directly on locals
    
    (swt/center-shell! display shell)
    (swt/add-file-dropping-to-control! tab-folder
                                       tabs/open-file-paths-in-tabs)
    
    (file-tree/show-file-tree! sash-form tab-folder false)
    
    ;;----- Actions on globals, so must come after hookup of shell controls
    
    (do-hookup-shell-controls shell-controls)

    ;; Note that java.io.File does not understand that "~"
    ;; equals home directory
    (if *command-line-args*
      (tabs/open-file-paths-in-tabs *command-line-args*))

    (tabs/open-blank-file-in-new-tab)
    
    (shell/show-shell! shell)

    shell))