;; window

(ns ajure.ui.window
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.ui [sash-form :as sash-form]
                      [status-bar :as status-bar])
            (ajure.state [hooks :as hooks])
            (ajure.cwt [display :as display]
                       [resources :as resources]
                       [shell :as shell])
            (ajure.util [swt :as swt])))

;;---------- Private

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
                                         :layout-data status-bar-layout-data)]

    (dosync
     (ref-set hooks/shell main-shell)
     (ref-set hooks/status-bar status-bar-map)
     (ref-set hooks/app-status-label (:app-label status-bar-map))
     (ref-set hooks/doc-status-label (:doc-label status-bar-map))
     (ref-set hooks/sash-form (:sash-form sash-form-map))
     (ref-set hooks/tab-folder (:tab-folder sash-form-map))
     (ref-set hooks/file-tree (:file-tree sash-form-map))
     ;;FIX(ref-set hooks/menu-bar menu-bar)
     ;;FIX(ref-set hooks/popup-menu popup-menu)
     )
    
    {:shell main-shell}))