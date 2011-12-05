;; status-bar

(ns ajure.gui.status-bar
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Label Canvas)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.state [hooks :as hooks])))

(defn- update-doc-status! []
  (io!
   (if @hooks/doc-status-label
     (doto @hooks/doc-status-label
       (.setText @hooks/current-endings)
       (.update)))))

(defn update-current-endings! [endings]
  (dosync
    (ref-set hooks/current-endings endings))
  (update-doc-status!))

(defn set-message! [& messages]
  (io!
   (doto @hooks/app-status-label
     (.setText (apply str messages))
     (.update))))

(defn create-status-bar! [shell]
  (io!
   (let [status-bar-canvas (Canvas. shell SWT/NONE)
         app-label (Label. status-bar-canvas SWT/BORDER)
         doc-label (Label. status-bar-canvas (bit-or SWT/BORDER SWT/RIGHT))
         right-margin (Label. status-bar-canvas SWT/BORDER)]

     ;; Setup layout
     (doto status-bar-canvas
       (.setLayout (let [layout (GridLayout.)]
                     (set! (. layout numColumns) 3)
                     (set! (. layout marginHeight) 0)
                     (set! (. layout marginWidth) 0)
                     (set! (. layout verticalSpacing) 0)
                     (set! (. layout horizontalSpacing) 0)
                     layout)))
     
     (doto app-label
       (.setLayoutData (GridData. SWT/FILL SWT/END true true)))
     
     (doto doc-label
       (.setLayoutData (GridData. SWT/FILL SWT/END true true)))
     
     (doto right-margin
       ;; The right margin is setup to prevent the doc label's text from being
       ;; covered by Mac's resize drag box
       (.setLayoutData (let [data (GridData. SWT/END SWT/END false true)]
                         (set! (. data widthHint) 30)
                         data)))

     (update-doc-status!)

     [status-bar-canvas app-label doc-label])))