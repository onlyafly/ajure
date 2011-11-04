;; ajure.gui.resources

(ns ajure.gui.resources
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Color Image FontData GC))
  (:require (ajure.gui [fonts :as fonts])))

;;---------- Images

(def images (ref {}))

(defn resource-file-path [file-name]
  (.getFile (clojure.java.io/resource file-name)))

;;TODO temporary
(defn create-icon-image [display]
  (let [image (Image. display 32 32)
        gc (GC. image)]
    (doto gc
      (.setBackground (.getSystemColor display SWT/COLOR_RED))
      (.fillArc 0 0 32 32 45 270)
      (.dispose))
    image))

;;TODO removed due to unable to find file errors on Mac
(defn allocate-images [display]
  (dosync
   (commute images assoc
            :logo (Image. display (resource-file-path "logo.png")))))

;;TODO removed due to unable to find file errors on Mac
(defn release-images []
  (.dispose (@images :logo)))

;;---------- Colors

(def colors (ref {}))

(defn allocate-colors [display]
  (dosync
    (commute colors assoc 
             :light-gray (Color. display 200 200 200)
             :gray (Color. display 150 150 150)
             :std-gray (.getSystemColor display SWT/COLOR_GRAY)
             :black (.getSystemColor display SWT/COLOR_BLACK)
             :white (.getSystemColor display SWT/COLOR_WHITE)
             :yellow (.getSystemColor display SWT/COLOR_YELLOW)
             :red (.getSystemColor display SWT/COLOR_RED)
             :azure-light (Color. display 179 205 229)
             :azure-medium (Color. display 139 187 229)
             :azure-dark (Color. display 100 169 229))))

(defn release-colors []
  (.dispose (@colors :light-gray))
  (.dispose (@colors :gray))
  (.dispose (@colors :azure-light))
  (.dispose (@colors :azure-medium))
  (.dispose (@colors :azure-dark)))

;;---------- General

(defn allocate-all [display]
  (allocate-images display)
  (allocate-colors display))

(defn release-all []
  (release-images)
  (release-colors))