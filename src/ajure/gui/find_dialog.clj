;; TODO this is not currently used

(ns ajure.gui.find-dialog
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Shell)
           (org.eclipse.swt.layout GridLayout GridData)
           (org.eclipse.swt.events ShellAdapter)))

(defn create-find-dialog [#^Shell parent-shell]
  (let [dialog-shell (Shell. parent-shell (bit-or SWT/DIALOG_TRIM SWT/MODELESS))]
    
    (doto dialog-shell
      (.setText "Search")
      (.setSize 300 300))
    
    (.open dialog-shell)
    dialog-shell))