;; application

(ns ajure.core.application
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [info :as info])
            (ajure.ui [sash-form :as sash-form]
                      [status-bar :as status-bar])
            (ajure.state [hooks :as hooks])
            (ajure.cwt [display :as display]
                       [resources :as resources]
                       [shell :as shell])
            (ajure.util [swt :as swt])))

(declare start-without-exception-handling!)

;;---------- External

(defn start! []
  (try
    (start-without-exception-handling!)
    (catch Exception exc
      ;;FIX (file-utils/log-exception exc)
      ;;FIX remove throws before deployment so that exceptions are caught
      (throw exc)
      )))

;;---------- Internal

(defn- get-key-combos []
  ;;FIX @hooks/application-key-combos
  )

;; Program exit point, called when using Application->Quit or Cmd+Q on Mac
(defn- quit-should-close!? []
  ;;FIX
  #_(let [should-close (window/verify-everything-saved-then-close!?)]
      should-close)
  true ;FIX only until above is fixed
  )

;; Action to take on main loop exception
(defn handle-exception [exception]
  (io!
   ;;FIX
   #_(status-bar/set-message!
    (str "Error occured. For details, view error log at "
         "<" file/error-log-file-path ">"))
   #_(file/log-exception exception)
   ;;FIXME remove throws before deployment so that exceptions are caught
   (throw exception)))

(defn- start-without-exception-handling! []
  (let [main-display (display/make!
                      :application-name info/application-name
                      :on-get-key-combos get-key-combos
                      :on-quit-should-close? quit-should-close!?)
        empty-bank (resources/make-bank! main-display)
        {bank-with-logo :bank logo-image :image} (resources/get-resource-image-and-bank!
                                                  empty-bank
                                                  "logo.png")

        main-shell (shell/make! :display main-display
                                :title info/application-name
                                :icon logo-image
                                :size [880 700]
                                :on-quit-should-close? quit-should-close!?)
        sash-form-layout-data (GridData. SWT/FILL SWT/FILL true true)
        sash-form (sash-form/make! :parent main-shell
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
        status-bar (status-bar/make! :shell main-shell
                                     :layout-data status-bar-layout-data)
       ]
    
    (println "test")

    ;; TODO move to window
    (shell/show! main-shell)
    

    (swt/basic-loop! main-display
                     main-shell
                     :on-release #(println "release")
                     :on-exception handle-exception)
    
    ))