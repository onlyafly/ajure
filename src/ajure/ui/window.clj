;; window

(ns ajure.ui.window
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [text :as text]
                        [undo :as undo])
            (ajure.ui [sash-form :as sash-form]
                      [status-bar :as status-bar])
            (ajure.state [hooks :as hooks]
                         [doc-state :as doc-state])
            (ajure.cwt [display :as display]
                       [resources :as resources]
                       [shell :as shell])
            (ajure.util [swt :as swt])))

;;---------- Private

(defn- on-before-history-change! []
  ;;FIX(text-editor/pause-change-listening! (doc-state/current :text-box))
  )

(defn- on-after-history-change! []
  ;;FIX
  #_(text-editor/resume-change-listening! (doc-state/current :text-box)
                                        tabs/on-text-box-change!)
  )

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
                                       :on-double-click-file-in-tree nil ;FIX
                                       :on-close-tab nil ;FIX
                                       :on-close-last-tab nil ;FIX
                                       :on-tab-selected nil ;FIX
                                       )
        status-bar-layout-data (let [data (GridData. SWT/FILL SWT/END true false)]
                                 ;; This would allow the item to span 2 columns
                                 ;; (set! (. data horizontalSpan) 2)
                                 data)
        status-bar-map (status-bar/make! :shell main-shell
                                         :layout-data status-bar-layout-data)
        popup-menu (make-popup-menu! main-shell)
        menu-bar (make-menu-bar! main-shell)]

    (dosync
     (ref-set hooks/shell main-shell)
     (ref-set hooks/status-bar status-bar-map)
     (ref-set hooks/app-status-label (:app-label status-bar-map))
     (ref-set hooks/doc-status-label (:doc-label status-bar-map))
     (ref-set hooks/sash-form (:sash-form sash-form-map))
     (ref-set hooks/tab-folder (:tab-folder sash-form-map))
     (ref-set hooks/file-tree (:file-tree sash-form-map))
     (ref-set hooks/menu-bar menu-bar)
     (ref-set hooks/popup-menu popup-menu)
     )
    
    {:shell main-shell}))