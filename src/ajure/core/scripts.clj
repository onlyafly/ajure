;; ajure.core.scripts

(ns ajure.core.scripts
  (:require (ajure.core [document :as document])
            (ajure.gui [hooks :as hooks]
                       [status-bar :as status-bar])
            (ajure.util [swt :as swt])))

(defn try-load-file [name]
  (try
    (load-file name)
    (catch Exception e
      ;TODO the message should be output somewhere the user can see
      (println (str "While loading <" name ">:\n" (.getMessage e)))
      (status-bar/set-message (str "Failure loading " name)))))

(defn run-document []
  (if (document/this :modified)
    (swt/show-warning-dialog @hooks/shell "Run This Document"
                             "Please save this document before running it as a script.")
    (try-load-file (document/this :filepath))))
