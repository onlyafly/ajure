;; application

(ns ajure.core.application
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.core [info :as info]
                        [file-utils :as file-utils])
            (ajure.ui [project :as project]
                      [sash-form :as sash-form]
                      [scripts :as scripts]
                      [status-bar :as status-bar]
                      [tabs :as tabs]
                      [window :as window])
            (ajure.state [hooks :as hooks])
            (ajure.cwt [display :as display]
                       [resources :as resources]
                       [shell :as shell])
            (ajure.util [swt :as swt]))
  (:use (ajure.util other)))

(declare start-without-exception-handling!)

;;---------- External

(defn start! [& {:keys [on-ui-ready]}]
  (try
    (start-without-exception-handling! :on-ui-ready on-ui-ready)
    (catch Exception exc
      (file-utils/log-exception exc)
      ;;FIX remove throws before deployment so that exceptions are caught
      (throw exc)
      )))

;;---------- Internal

(defn- get-key-combos []
  @hooks/application-key-combos)

(defn verify-everything-saved-then-close!? []
  (and (tabs/verify-all-tabs-saved-then-close!?)
       (project/verify-project-saved-then-close!?)))

;; Program exit point, called when using Application->Quit or Cmd+Q on Mac
(defn- quit-should-close!? []
  (let [should-close (verify-everything-saved-then-close!?)]
      should-close))

;; Action to take on main loop exception
(defn handle-exception! [exception]
  (io!
   (status-bar/set-message!
    (str "Error occured. For details, view error log at "
         "<" file-utils/error-log-file-path ">"))
   (file-utils/log-exception exception)
   ;;FIX remove throws before deployment so that exceptions are caught
   (throw exception)))

(defn- start-without-exception-handling! [& {:keys [on-ui-ready]}]
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

      (on-ui-ready)

      ;;FIX
      #_(
          
          ;; Update the GUI from settings where applicable here
         
         (editors/update-editor-font-from-settings))
          
      ;; Run the custom script here so that the script can modify the GUI
      ;; if desired
          
      (when (str-not-empty? (@hooks/settings :custom-script-file-path))
        (scripts/try-load-file (@hooks/settings :custom-script-file-path)))
      
      (window/show! window)
      
      (swt/basic-loop! main-display
                       (:shell window)
                       :on-release #(println "release")
                       :on-exception handle-exception!))))