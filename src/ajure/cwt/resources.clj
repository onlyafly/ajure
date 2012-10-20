;; resources

(ns ajure.cwt.resources
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Color Image FontData GC)))

(declare image-in-bank?
         create-resource-image!
         dispose-images!
         create-system-colors!
         create-custom-colors!
         dispose-custom-colors!)

;;---------- Public

(defn make-bank! [display]
  {:display display
   :images {}
   :system-colors (create-system-colors! display)
   :custom-colors (create-custom-colors! display)})

(defn dispose-bank! [bank]
  (dispose-images! bank)
  (dispose-custom-colors! bank))

(defn get-resource-image-and-bank!
  "This allows images to be created from resources. The image is not
  created directly from the file so that it will work from JAR files,
  as well."
  [bank resource-file-name]
  (io!
   (if (image-in-bank? bank resource-file-name)
     (let [image (get-in bank [:images resource-file-name])]
       {:bank bank :image image})
     (let [display (bank :display)
           image (create-resource-image! display resource-file-name)
           updated-bank (assoc-in bank [:images resource-file-name] image)]
       {:bank updated-bank :image image}))))

(defn get-named-color
  ([bank color-keyword]
     (if (contains? (bank :system-colors) color-keyword)
       (get-in bank [:system-colors color-keyword])
       (get-in bank [:custom-colors color-keyword]))))

;;---------- Private

(defn- image-in-bank? [bank file-name]
  (boolean (get-in bank [:images file-name])))

(defn- create-resource-image!
  "This allows images to be created from resources. The image is not
  created directly from the file so that it will work from JAR files,
  as well."
  [display resource-file-name]
  (io!
   (let [inputStream (ClassLoader/getSystemResourceAsStream resource-file-name)]
     (Image. display inputStream))))

(defn- dispose-images! [bank]
  (io!
   (doseq [image (bank :images)]
     (.dispose image))))

(defn- create-system-colors! [display]
  (io!
   {:std-gray (.getSystemColor display SWT/COLOR_GRAY)
    :black (.getSystemColor display SWT/COLOR_BLACK)
    :white (.getSystemColor display SWT/COLOR_WHITE)
    :yellow (.getSystemColor display SWT/COLOR_YELLOW)
    :red (.getSystemColor display SWT/COLOR_RED)}))

;; Any new instantiations of the Color class must be disposed of.
(defn- create-custom-colors! [display]
  (io!
   {:light-gray (Color. display 200 200 200)
    :gray (Color. display 150 150 150)
    :azure-light (Color. display 179 205 229)
    :azure-medium (Color. display 139 187 229)
    :azure-dark (Color. display 100 169 229)}))

(defn- dispose-custom-colors! [bank]
  (io!
   (doseq [color (bank :custom-colors)]
     (.dispose color))))

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