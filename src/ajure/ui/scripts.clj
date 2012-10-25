;; scripts

(ns ajure.ui.scripts
  (:require (ajure.ui [status-bar :as status-bar])
			(ajure.state [doc-state :as doc-state]
			             [hooks :as hooks])
            (ajure.cwt [swt :as swt])))

(defn try-load-file [name]
  (try
    (load-file name)
    (status-bar/set-message! (str "Finished executing " name))
    (catch Exception e
      ;TODO the message should be output somewhere the user can see
      (println (str "While loading <" name ">:\n" (.getMessage e)))
      (status-bar/set-message! (str "Failure loading " name)))))

(defn run-doc []
  (if (doc-state/current :is-modified)
    (swt/show-warning-dialog! @hooks/shell "Run This Document"
                              "Please save this doc before running it as a script.")
    (try-load-file (doc-state/current :file-path))))
