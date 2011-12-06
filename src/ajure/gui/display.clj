;; display
;; (refactored to layered API)

(ns ajure.gui.display
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Event Display Listener))
  (:require (ajure.util [info :as info]
                        [swt :as swt])))

;; NOTE: Public only for macro use.
(defn key-down-handle-event! [event key-combo-mapping]
  (swt/execute-key-combo-in-mappings! event
                                      key-combo-mapping
                                      #(do
                                         ;; Consume the event
                                         (set! (. event doit) false)
                                         ;; Cancel the event
                                         (set! (. event type) SWT/None))))

(def example-make-display
  '(make-display
    :application-name "Ajure"
    :on-quit-should-close? window/verify-everything-saved-then-close!?
    :on-get-key-combos (fn [] {[[SWT/MOD1] \o] open!})))

(defmacro make-display [& body]
  (let [body-map (apply hash-map body)
        {name :application-name
         on-quit :on-quit-should-close?
         ;; TODO maybe change key combos so they don't need to
         ;; reference SWT directly
         on-get-key-combos :on-get-key-combos} body-map
        display-sym (gensym "display")]
    `(io!
      
      ;; The app name must be set before the display is created for
      ;; it to take affect on Mac
      (Display/setAppName ~name)

      (let [~display-sym (Display.)]

        ;; Allow :on-quit to be nil
        ~(when on-quit
           ;; This is called when app is closed with Command-Q in Mac or Alt+F4 in Windows
           `(.addListener ~display-sym
                          SWT/Close
                          (reify Listener
                            (handleEvent [this# event#]
                              (set! (. event# doit) (~on-quit))))))

        ;; Allow on-get-key-combos to be nil
        ~(when on-get-key-combos
           ;; Optimized because this is called before every key event
           ;; in the application
           
           `(.addFilter ~display-sym
                        SWT/KeyDown
                        (reify Listener
                          (handleEvent [this# event#]
                            (key-down-handle-event! event#
                                                    (~on-get-key-combos))))))

        ~display-sym))))