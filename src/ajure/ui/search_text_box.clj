;; search-text-box

(ns ajure.ui.search-text-box
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Text)
           (org.eclipse.swt.events SelectionAdapter KeyAdapter KeyEvent))
  (:require (ajure.cwt [swt :as swt])))

(defn create-search-text-box! [parent
                               hint-text
                               escape-pressed-action
                               cancel-clicked-action
                               enter-pressed-action]
  (io!
   (let [box (Text. parent (swt/options SEARCH CANCEL))]
     (doto box

       ;; This text is used as a hint
       (.setMessage hint-text)

       (.addKeyListener
        (proxy [KeyAdapter] []
          (keyPressed [^KeyEvent event]
            ;; Runs when <Esc> is pressed in the box
            (when (= (. event keyCode) (int SWT/ESC))
              (escape-pressed-action)))))

       (.addSelectionListener
        (proxy [SelectionAdapter] []
          (widgetDefaultSelected [event]
            (if (= SWT/CANCEL (. event detail))
              ;; Runs when box's cancel button is clicked (only visible on Mac)
              (cancel-clicked-action)
              ;; Runs when <Enter> is pressed in the text
              (enter-pressed-action))))))
     
     box)))