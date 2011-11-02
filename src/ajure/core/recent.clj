(ns ajure.core.recent
  (:require (ajure.gui [hooks :as hooks])))

(defn- apply-in-map
  "Same as 'apply' but applies on the contents of a particular key on a
  map."
  [hashed-map coll-key f & values]
  (assoc hashed-map coll-key (apply f (hashed-map coll-key) values)))

(defn- object-not-recent? [settings-key file-path]
  (if (some #{file-path} (@hooks/settings settings-key))
    false
    true))

(defn- dequeue
  "Dequeue an element from a vector, treating it as a queue."
  [v]
  (subvec v 1))

(defn- enqueue
  "Enqueue an element onto a possibly nil vector, treating it as a queue."
  [v element]
  (vec (conj v element)))

(defn- add-recent-object [settings-key file-path]
  (when (object-not-recent? settings-key file-path)
    (dosync
     
     ;; Enqueue a new file path
     (commute hooks/settings apply-in-map settings-key enqueue file-path)
     
     (when (> (count (@hooks/settings settings-key)) 10)
       
       ;; Dequeue the oldest file-path
       (commute hooks/settings apply-in-map settings-key dequeue)))))

(defn- add-recent-objects [settings-key file-paths]
  (doseq [file-path file-paths]
    (add-recent-object settings-key file-path)))

(defn add-recent-file [file-path]
  (add-recent-object :recent-files file-path))

(defn add-recent-files [file-paths]
  (add-recent-objects :recent-files file-paths))

(defn add-recent-project [file-path]
  (add-recent-object :recent-projects file-path))

(defn add-recent-projects [file-paths]
  (add-recent-objects :recent-projects file-paths))