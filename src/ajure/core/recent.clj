(ns ajure.core.recent
  (:require (ajure.state [hooks :as hooks])
            (ajure.util [queue :as queue])))

(defn- object-not-recent? [settings-key file-path]
  (if (some #{file-path} (@hooks/settings settings-key))
    false
    true))

(defn- add-recent-object [settings-key file-path]
  (when (object-not-recent? settings-key file-path)
    (dosync
     
     ;; Enqueue a new file path
     (commute hooks/settings
              update-in [settings-key] queue/enqueue file-path)
     
     (when (> (count (@hooks/settings settings-key)) 10)
       
       ;; Dequeue the oldest file-path
       (commute hooks/settings
                update-in [settings-key] queue/dequeue)))))

(defn- add-recent-objects [settings-key file-paths]
  (dosync
   (doseq [file-path file-paths]
     (add-recent-object settings-key file-path))))

(defn add-recent-file [file-path]
  (add-recent-object :recent-files file-path))

(defn add-recent-files [file-paths]
  (add-recent-objects :recent-files file-paths))

(defn add-recent-project [file-path]
  (add-recent-object :recent-projects file-path))

(defn add-recent-projects [file-paths]
  (add-recent-objects :recent-projects file-paths))