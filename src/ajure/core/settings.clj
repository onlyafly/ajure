;; settings
;; (Refactored for layering)
;;
;; Should:
;;  - Manage settings file
;;  - Store settings
;;  - Provide accessors for settings
;; 
;; Should not:
;;  - Interface with GUI components at all

(ns ajure.core.settings
  (:require (ajure.ui [info :as info])
            (ajure.state [hooks :as hooks])
            
            (ajure.io [file-io :as file-io])
            (ajure.os [platform :as platform]))
  (:use ajure.other.misc))

(def stored-settings-file-path (str platform/home-dir
                                    platform/file-separator
                                    "ajure-settings.clj"))

(defn load-settings! []
  (io!
   (file-io/create-empty-file-unless-exists! stored-settings-file-path)
   (let [content (file-io/read-text-file! stored-settings-file-path)]
     (when (str-not-empty? content)
       (let [loaded-object (read-string content)]
         (dosync
          (commute hooks/settings merge loaded-object)))))))

(defn save-settings! []
  (io!
   (file-io/write-text-file! stored-settings-file-path
                        (with-out-str
                          (println ";" info/application-name info/version-number-string)
                          (println "; Automatically generated file.  Modify with care.")
                          (println "{")
                          (doseq [pair @hooks/settings]
                            (pr (key pair) (val pair))
                            (println))
                          (println "}")))))