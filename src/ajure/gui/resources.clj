;; resources

(ns ajure.gui.resources
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Color Image FontData GC))
  (:require (ajure.gui [fonts :as fonts])))

;;---------- Images

(defn- create-image!
  "This allows images to be created from resources. The image is not
  created directly from the file so that it will work from JAR files,
  as well."
  [display resource-file-name]
  (io!
   (let [inputStream (ClassLoader/getSystemResourceAsStream resource-file-name)]
     (Image. display inputStream))))

;; TODO Only used as example.
(defn- create-icon-image! [display]
  (io!
   (let [image (Image. display 32 32)
         gc (GC. image)]
     (doto gc
       (.setBackground (.getSystemColor display SWT/COLOR_RED))
       (.fillArc 0 0 32 32 45 270)
       (.dispose))
     image)))

(defn- do-allocate-images! [display images-ref]
  (io!
   (let [images-map {:logo (create-image! display "logo.png")}]
     (dosync
      (ref-set images-ref images-map)))))

(defn- do-release-images! [images-ref]
  (io!
   (.dispose (@images-ref :logo)))
  (dosync
   (ref-set images-ref {})))

;;---------- Colors

;; Any new instantiations of the Color class must be disposed of.
(defn- do-allocate-colors! [display colors-ref]
  (io!
   (let [colors-map {:light-gray (Color. display 200 200 200)
                     :gray (Color. display 150 150 150)
                     :std-gray (.getSystemColor display SWT/COLOR_GRAY)
                     :black (.getSystemColor display SWT/COLOR_BLACK)
                     :white (.getSystemColor display SWT/COLOR_WHITE)
                     :yellow (.getSystemColor display SWT/COLOR_YELLOW)
                     :red (.getSystemColor display SWT/COLOR_RED)
                     :azure-light (Color. display 179 205 229)
                     :azure-medium (Color. display 139 187 229)
                     :azure-dark (Color. display 100 169 229)}]
     (dosync
      (ref-set colors-ref colors-map)))))

(defn- do-release-colors! [colors-ref]
  (io!
   (.dispose (colors-ref :light-gray))
   (.dispose (colors-ref :gray))
   (.dispose (colors-ref :azure-light))
   (.dispose (colors-ref :azure-medium))
   (.dispose (colors-ref :azure-dark)))
  (dosync
   (ref-set colors-ref {})))

;;---------- General

(defn do-allocate-all! [display images-ref colors-ref]
  (do-allocate-images! display images-ref)
  (do-allocate-colors! display colors-ref))

(defn do-release-all! [images-ref colors-ref]
  (do-release-images! images-ref)
  (do-release-colors! colors-ref))