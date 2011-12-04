;; file-dialogs
;;
;; Should:
;;  - Provide functions for getting information back from file dialogs

(ns ajure.gui.file-dialogs
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets FileDialog DirectoryDialog))
  (:use (ajure.state [hooks :as hooks])
        (ajure.util [platform :as platform]))) 

(defn open-dialog!
  ([shell title]
     (open-dialog! shell title nil ""))
  ([shell title initial-dir]
     (open-dialog! shell title initial-dir ""))
  ([shell title initial-dir initial-file]
     (open-dialog! shell
                   title
                   initial-dir
                   initial-file
                   ["All Files (*.*)"]
                   ["*.*"]))
  ([shell title initial-dir initial-file filter-names filter-exts]
     (io!
      (let [dialog (FileDialog. shell SWT/OPEN)
            initial-path (if initial-dir
                           initial-dir
                           platform/home-dir)]
        (doto dialog
          (.setText title)
          (.setFilterNames (into-array filter-names))
          (.setFilterExtensions (into-array filter-exts))
          (.setFilterPath initial-path)
          (.setFileName initial-file))
        
        (.open dialog)))))

(defn save-dialog!
  ([shell]
     (save-dialog! shell "Save As..." nil ""))
  ([shell title initial-dir initial-file-name]
     (save-dialog! shell
                   title
                   initial-dir
                   initial-file-name
                   ["All Files (*.*)"]
                   ["*.*"]))
  ([shell title initial-dir initial-file-name filter-names filter-exts]
     (io!
      (let [dialog (FileDialog. shell SWT/SAVE)
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
        
        (.open dialog)))))

(defn dir-dialog! [shell msg]
  (io!
   (let [dialog (DirectoryDialog. shell SWT/NONE)]
     (doto dialog
       (.setFilterPath "")
       (.setMessage msg))
     (.open dialog))))