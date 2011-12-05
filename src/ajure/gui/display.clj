;; display

(ns ajure.gui.display
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Event Display Listener))
  (:require (ajure.state [hooks :as hooks])
            (ajure.util [info :as info])))

(def t1
  '(make-display
    :application-name "Ajure"
    :on-application-quit (window/verify-everything-saved-then-close!?)
    :on-key-down (foo)))

(defmacro make-display [& body]
  (let [body-map (apply hash-map body)
        {name :application-name
         on-quit :on-application-quit
         on-key-down :on-key-down} body-map]
    `(io!
      
      ;; The app name must be set before the display is created for
      ;; it to take affect on Mac
      (Display/setAppName ~name)

      (let [display# (Display.)]
        (doto display#

          ;; This is called when app is closed with Command-Q in Mac or Alt+F4 in Windows
          (.addListener SWT/Close
                        (reify Listener
                          (handleEvent [this# event#]
                            (set! (. event# doit) ~on-quit))))

          ;; Optimized because this is called before every key event in the application
          (.addFilter SWT/KeyDown
                      (reify Listener
                        (handleEvent [this# event#]
                          ;;FIXME
                          ~on-key-down))))

        display#))))

(defn create-display! [close-action key-down-action]
  (io!
   ;; The app name must be set before the display is created for
   ;; it to take affect on Mac
   (Display/setAppName info/application-name)

   (let [display (Display.)]

     ;; This is called when app is closed with Command-Q in Mac or Alt+F4 in Windows
     (.addListener display SWT/Close
                   (reify Listener
                     (handleEvent [this event]
                       (close-action event))))

     ;; Optimized because this is called before every key event in the application
     (.addFilter display SWT/KeyDown
                 (reify Listener
                   (handleEvent [this event]
                     (key-down-action event))))

     display)))