;; application

(ns ajure.core.application
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [info :as info])
            (ajure.ui [sash-form :as sash-form]
                      [status-bar :as status-bar]
                      [window :as window])
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
   ;;FIX remove throws before deployment so that exceptions are caught
   (throw exception)))

(defn- start-without-exception-handling! []
  (let [main-display (display/make!
                      :application-name info/application-name
                      :on-get-key-combos get-key-combos
                      :on-quit-should-close? quit-should-close!?)
        empty-bank (resources/make-bank! main-display)
        {bank-with-logo :bank logo-image :image} (resources/get-resource-image-and-bank!
                                                  empty-bank
                                                  "logo.png")]

    ;; Setup resources
    
    (dosync
     (ref-set hooks/bank bank-with-logo))

    ;; Setup GUI
    
    (let [window (window/make! :display main-display
                               :title info/application-name
                               :icon logo-image
                               :on-quit-should-close? quit-should-close!?)]
      (println "test")
      
      (window/show! window)
      
      (swt/basic-loop! main-display
                       (:shell window)
                       :on-release #(println "release")
                       :on-exception handle-exception))))