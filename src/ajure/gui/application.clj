;; ajure.gui.application

(ns ajure.gui.application
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Event Display Listener))
  (:require (ajure.state [hooks :as hooks])
            (ajure.util [info :as info])))

(defn create-display [close-action key-down-action]
  ; The app name must be set before the display is created for
  ; it to take affect on Mac
  (Display/setAppName info/application-name)

  (let [display (Display.)]
    (dosync
      (ref-set hooks/display display))

    ; This is called when app is closed with Command-Q in Mac or Alt+F4 in Windows
    (.addListener display SWT/Close
      (reify Listener
        (handleEvent [this event]
          (close-action event))))

    ; Optimized because this is called before every key event in the application
    (.addFilter display SWT/KeyDown
      (reify Listener
        (handleEvent [this event]
          (key-down-action event))))

    display))

