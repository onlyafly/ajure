;; ajure.gui.status-bar

(ns ajure.gui.status-bar
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Label Canvas)
           (org.eclipse.swt.layout GridLayout GridData))
  (:require (ajure.state [hooks :as hooks])))

;;---------- Status Labels

(def status-bar (ref nil))
(def app-status-label (ref nil))
(def doc-status-label (ref nil))

;;---------- Document Label Information

(def current-endings (ref ""))

;;---------- Operations

(defn update-doc-status []
  (if @doc-status-label
    (doto @doc-status-label
      (.setText @current-endings)
      (.update))))

(defn update-current-endings [endings]
  (dosync
    (ref-set current-endings endings))
  (update-doc-status))

(defn set-message [& messages]
  (doto @app-status-label
    (.setText (apply str messages))
    (.update)))

(defn create-status-bar [shell]
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
      ; The right margin is setup to prevent the doc label's text from being
      ; covered by Mac's resize drag box
      (.setLayoutData (let [data (GridData. SWT/END SWT/END false true)]
                        (set! (. data widthHint) 30)
                        data)))

    (dosync
      (ref-set status-bar status-bar-canvas)
      (ref-set app-status-label app-label)
      (ref-set doc-status-label doc-label))

    (update-doc-status)

    status-bar-canvas))