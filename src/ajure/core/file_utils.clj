;; ajure.core.file-utils

(ns ajure.core.file-utils
  (:require (ajure.core [settings :as settings])
            (ajure.gui [file-dialogs :as file-dialogs]
                       [status-bar :as status-bar])
			(ajure.state [doc-state :as doc-state]
			             [hooks :as hooks])
            (ajure.util [io :as io]
                        [swt :as swt]
                        [platform :as platform]))
  (:use ajure.util.other))

;;---------- Error logging

(def error-log-file-path
  (str platform/home-dir platform/file-separator "ajure-errors.txt"))

(defn log-exception [ex]
  (let [trace-vec (vec (.getStackTrace ex))
        trace-string (apply str (map #(str % "\n") trace-vec))]
    (io/append-text-file! error-log-file-path
                         (str "CURRENT TIME: " (java.util.Date.) "\n"
                              "CURRENT DOC: " (doc-state/current) "\n"
                              "EXCEPTION: " ex "\n"
                              "MESSAGE: "(.getMessage ex) "\n"
                              "TRACE: " trace-string "\n"
                              "-------------------\n"))))

;;---------- Other

(defn choose-startup-script []
  (let [[dir name] (if (str-not-empty? (@hooks/settings :custom-script-file-path))
                     (io/get-file-name-parts! (@hooks/settings :custom-script-file-path))
                     ["" ""])
        file-path (file-dialogs/open-dialog!
		             @hooks/shell
                     "Choose Startup Script"
                     dir
                     name)]
    (if file-path
      (do
        (io/create-empty-file-unless-exists! file-path)
        (dosync
          (commute hooks/settings assoc
                   :custom-script-file-path file-path))
        (status-bar/set-message! (str "Startup script set to"
                                     "<" file-path ">"))))))

