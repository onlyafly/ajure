;; window
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

(defn verify-everything-saved-then-close!? []
  (and (tabs/verify-all-tabs-saved-then-close!?)
       (project/verify-project-saved-then-close!?)))

(defn- on-before-history-change! []
  (text-editor/pause-change-listening! (doc-state/current :text-box)))

(defn- on-after-history-change! []
  (text-editor/resume-change-listening! (doc-state/current :text-box)
                                        tabs/on-text-box-change))

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

(defn- create-popup-menu! [shell]
  (io!
   (let [popup-menu (swt/create-popup-menu! shell)]
     (attach-edit-popup-menu-items! popup-menu)
     (.setMenu shell popup-menu)
     popup-menu)))

(defn- create-menu-bar! [shell]
  (io!
   (let [menu-bar (swt/create-menu-bar! shell)]
     (.setMenuBar shell menu-bar)
     menu-bar)))

(defn- on-double-click-file-in-tree! [file-object]
  (io!
   (tabs/open-file-in-new-tab (.getPath file-object))))

(defn- do-hookup-controls
  [{shell :shell
    status-bar :status-bar
    app-label :app-label
    doc-label :doc-label
    tab-folder :tab-folder
    sash-form :sash-form
    file-tree :file-tree
    popup-menu :popup-menu
    menu-bar :menu-bar}]
  
  (dosync
   (ref-set hooks/shell shell)
   (ref-set hooks/status-bar status-bar)
   (ref-set hooks/app-status-label app-label)
   (ref-set hooks/doc-status-label doc-label)
   (ref-set hooks/sash-form sash-form)
   (ref-set hooks/tab-folder tab-folder)
   (ref-set hooks/file-tree file-tree)
   (ref-set hooks/menu-bar menu-bar)
   (ref-set hooks/popup-menu popup-menu)))

(defn do-show-window! [display]
  (io!
   (let [shell-controls (shell/create-shell! display
                                             on-double-click-file-in-tree!
                                             tabs/verify-tab-saved-then-close?
                                             tabs/open-blank-file-in-new-tab
                                             tabs/tab-selected-action
                                             verify-everything-saved-then-close!?)
         shell (:shell shell-controls)
         tab-folder (:tab-folder shell-controls)
         sash-form (:sash-form shell-controls)
         popup-menu (create-popup-menu! shell)
         menu-bar (create-menu-bar! shell)
         all-controls (assoc shell-controls
                        :menu-bar menu-bar
                        :popup-menu popup-menu)]

     ;;----- Actions directly on locals

     (swt/center-shell! display shell)
     (swt/add-file-dropping-to-control! tab-folder
                                        tabs/open-file-paths-in-tabs)
     
     (file-tree/show-file-tree! sash-form tab-folder false)
     
     ;;----- Actions on globals, so must come after hookup of shell controls
     
     (do-hookup-controls all-controls)

     ;; Note that java.io.File does not understand that "~"
     ;; equals home directory
     (if *command-line-args*
       (tabs/open-file-paths-in-tabs *command-line-args*))

     (tabs/open-blank-file-in-new-tab)
     
     (shell/show-shell! shell)

     shell)))