;; display
;; (refactored to layered API)

(ns ajure.gui.display
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Event Display Listener))
  (:require (ajure.util [swt :as swt])))

(declare key-down-handle-event!
         make-display-fn!)

;;---------- Public

(def example-make-display
  '(make-display
    :application-name "Ajure"
    :on-quit-should-close? (fn [] (print 2))
    :on-get-key-combos (fn [] {:a 2})))

(defmacro make-display [& body]
  (let [body-map (apply hash-map body)
        {name :application-name
         on-quit-should-close? :on-quit-should-close?
         ;; TODO maybe change key combos so they don't need to
         ;; reference SWT directly
         on-get-key-combos :on-get-key-combos} body-map]
    
    `(make-display-fn! ~name
                       ~on-quit-should-close?
                       ~on-get-key-combos)))

;;---------- Helper functions

(defn- key-down-handle-event! [event key-combo-mapping]
  (swt/execute-key-combo-in-mappings! event
                                      key-combo-mapping
                                      #(do
                                         ;; Consume the event
                                         (set! (. event doit) false)
                                         ;; Cancel the event
                                         (set! (. event type) SWT/None))))

;; PUBLIC FOR MACRO USE
(defn make-display-fn! [application-name
                        on-quit-should-close?
                        on-get-key-combos]
  (io!
     ;; The app name must be set before the display is created for
     ;; it to take affect on Mac
     (Display/setAppName application-name)

     (let [display (Display.)]

       (when on-quit-should-close?
         ;; This is called when app is closed with Command-Q in Mac or
         ;; Alt+F4 in Windows
         (.addListener display
                       SWT/Close
                       (reify Listener
                         (handleEvent [this event]
                           (set! (. event doit) (on-quit-should-close?))))))

       (when on-get-key-combos
         ;; Optimized because this is called before every key event
         ;; in the application
         (.addFilter display
                     SWT/KeyDown
                     (reify Listener
                       (handleEvent [this event]
                         (key-down-handle-event! event
                                                 (on-get-key-combos))))))

       display)))