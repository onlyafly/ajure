;; display

(ns ajure.cwt.display
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Event Display Listener))
  (:require (ajure.util [swt :as swt])))

(declare key-down-handle-event!)

;;---------- External

(defn make! [& {:keys [application-name
                       on-quit-should-close?
                       on-get-key-combos]}]
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

;;---------- Internal

(defn- key-down-handle-event! [event key-combo-mapping]
  (swt/execute-key-combo-in-mappings! event
                                      key-combo-mapping
                                      #(do
                                         ;; Consume the event
                                         (set! (. event doit) false)
                                         ;; Cancel the event
                                         (set! (. event type) SWT/None))))