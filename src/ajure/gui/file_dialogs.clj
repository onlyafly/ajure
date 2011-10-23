;; ajure.gui.file-dialogs
;;
;; Should:
;;  - Provide functions for getting information back from file dialogs

(ns ajure.gui.file-dialogs
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets FileDialog DirectoryDialog))
  (:use (ajure.gui [hooks :as hooks])
        (ajure.util [platform :as platform]))) 

(defn open-dialog
  ([title]
     (open-dialog title nil ""))
  ([title initial-dir]
     (open-dialog title initial-dir ""))
  ([title initial-dir initial-file]
     (open-dialog title initial-dir initial-file
                  ["All Files (*.*)"]
                  ["*.*"]))
  ([title initial-dir initial-file filter-names filter-exts]
     (let [dialog (FileDialog. @hooks/shell SWT/OPEN)
           initial-path (if initial-dir
                          initial-dir
                          platform/home-dir)]
       (doto dialog
         (.setText title)
         (.setFilterNames (into-array filter-names))
         (.setFilterExtensions (into-array filter-exts))
         (.setFilterPath initial-path)
         (.setFileName initial-file))
       (.open dialog))))

(defn save-dialog
  ([]
     (save-dialog "Save As..." nil ""))
  ([title initial-dir initial-file-name]
     (save-dialog title initial-dir initial-file-name
                  ["All Files (*.*)"] ["*.*"]))
  ([title initial-dir initial-file-name filter-names filter-exts]
     (let [dialog (FileDialog. @hooks/shell SWT/SAVE)
           initial-path (if initial-dir
                          initial-dir
                          platform/home-dir)]
       (doto dialog
         (.setOverwrite true)
         (.setText title)
         (.setFilterNames (into-array filter-names))
         (.setFilterExtensions (into-array filter-exts))
         (.setFilterPath initial-path)
         (.setFileName initial-file-name))
       (.open dialog))))

(defn dir-dialog [msg]
  (let [dialog (DirectoryDialog. @hooks/shell SWT/NONE)]
    (doto dialog
      (.setFilterPath "")
      (.setMessage msg))
    (.open dialog)))