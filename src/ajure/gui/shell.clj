;; shell
;;
;; Shell (window) wrapper.

(ns ajure.gui.shell
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Shell)
           (org.eclipse.swt.layout GridLayout GridData)
           (org.eclipse.swt.events ShellAdapter)
           (org.eclipse.swt.graphics Image GC))
  (:require (ajure.gui [application :as application]
                       [info-dialogs :as info-dialogs]
                       [file-tree :as file-tree]
                       [file-dialogs :as file-dialogs]
                       [sash-form :as sash-form]
                       [status-bar :as status-bar]
                       [fonts :as fonts]
                       [resources :as resources])
            (ajure.state [hooks :as hooks])
            (ajure.util [platform :as platform]
                        [swt :as swt]
                        [other :as other]
                        [info :as info])))

(defn- create-shell-grid-layout []
  (let [layout (GridLayout.)]
    (set! (. layout numColumns) 1)
    (set! (. layout marginHeight) 0)
    (set! (. layout marginWidth) 0)
    (set! (. layout verticalSpacing) 0)
    (set! (. layout horizontalSpacing) 0)
    layout))

(defn show-shell! [shell]
  (io!
   (.open shell)))

(defn create-shell! [display
                     double-click-file-in-tree-action
                     close-tab-action
                     last-tab-closing-action
                     tab-selected-action
                     create-popup-menu-action
                     create-menu-bar-action
                     verify-everything-saved-then-close?]
  (io!
   (let [shell (Shell. display)
         sash-form-controls (sash-form/create-sash-form! shell
                                                         double-click-file-in-tree-action
                                                         close-tab-action
                                                         last-tab-closing-action
                                                         tab-selected-action)
         [sash-form tab-folder] sash-form-controls
         [status-bar app-label doc-label] (status-bar/create-status-bar! shell)
         popup-menu (create-popup-menu-action shell)
         menu-bar (create-menu-bar-action shell)]

     ;; Setup layout
     (doto shell
       (.setLayout (create-shell-grid-layout)))
     (doto sash-form
       (.setLayoutData (GridData. SWT/FILL SWT/FILL true true)))

     (doto status-bar
       (.setLayoutData (let [data (GridData. SWT/FILL SWT/END true false)]
                         ;; This would allow the item to span 2 columns
                         ;; (set! (. data horizontalSpan) 2)
                         data)))

     (doto shell
       (.setText info/application-name)

       (.setImage (@hooks/images :logo))

       ;; Program exit point
       ;; This is called when the shell is closed using the X at the top
       ;;  or when Alt+F4 is pressed in Windows
       (.addShellListener
        (proxy [ShellAdapter] []
          (shellClosed [event]
            (let [should-close (verify-everything-saved-then-close?)]
              (set! (. event doit) should-close)))))
       
       (.setSize 880 700))

     {:shell shell
      :sash-form sash-form
      :tab-folder tab-folder
      :status-bar status-bar
      :app-label app-label
      :doc-label doc-label})))