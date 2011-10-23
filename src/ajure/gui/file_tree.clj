;; ajure.gui.file-tree
;;
;; Implements a lazy file tree.  Children are only loaded when parent
;; is expanded.

(ns ajure.gui.file-tree
  (:import (java.io File)
           (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Point)
           (org.eclipse.swt.events TreeListener SelectionAdapter MouseAdapter)
           (org.eclipse.swt.widgets Tree TreeItem TreeColumn))
  (:require (ajure.gui [hooks :as hooks])
            (ajure.util [io :as io])))

(defn show-file-tree [show]
  (if show
    (.setMaximizedControl @hooks/sash-form nil)
    (.setMaximizedControl @hooks/sash-form @hooks/tab-folder)))

(defn remove-all-tree-items [tree-control]
  (let [children (seq (.getItems tree-control))]
    (doseq [child children]
      (.dispose child))))

(defn add-files-to-tree [parent files]
  (doseq [file files]
    (when (io/file-visible? file)
      (let [item (TreeItem. parent 0)
            file-name (io/get-file-name-only file)]
        (if (zero? (count file-name))
          (.setText item (str file))
          (.setText item file-name))
        (doto item
          (.setData file))
        (when (.isDirectory file)
          (TreeItem. item 0))))))

(defn add-file-name-to-tree [parent file-name]
  (add-files-to-tree parent [(File. file-name)]))

(defn on-tree-expanded [root]
  (let [items (seq (.getItems root))]
    (when items
      (when-not (.getData (first items))
        (doseq [item items]
          (.dispose item))
        (let [file (.getData root)
              files (seq (.listFiles file))]
          (when files
            (add-files-to-tree root files)))))))

(def tree-listener
     (proxy [TreeListener] []
       (treeCollapsed [event]
         nil)
       ; Called when node is expanded by user
       (treeExpanded [event]
         (on-tree-expanded (. event item)))))

(defn toggle-expanded-state [tree-item]
  (if (.getExpanded tree-item)
    (.setExpanded tree-item false)
    (do
      (on-tree-expanded tree-item)
      (.setExpanded tree-item true))))

(defn create-file-tree [parent double-click-file-action]
  (let [tree (Tree. parent SWT/SINGLE)
        horizontal-scrollbar (.getHorizontalBar tree)
        column (TreeColumn. tree SWT/LEFT)]
    (dosync
     (ref-set hooks/file-tree tree))

    ;; Not all platforms allow making scrollbar invisible, so it
    ;; must be disabled as well
    (doto horizontal-scrollbar
      (.setVisible false)
      (.setEnabled false))

    (doto tree
      (.setHeaderVisible true)
      (.setLinesVisible true)

      ; This disables expand on double click
      (.addSelectionListener
       (proxy [SelectionAdapter] []
         (widgetSelected [event]
           nil)))
      
      (.addMouseListener
       (proxy [MouseAdapter] []
         (mouseDoubleClick [event]
           (let [x (. event x)
                 y (. event y)
                 point (Point. x y)
                 item (.getItem tree point)]
             (when item
               (let [file (.getData item)]
                 (if (io/file-not-directory? file)
                   (double-click-file-action file)
                   (toggle-expanded-state item))))))))
      
      (.addTreeListener tree-listener))

    (doto column
      (.setResizable false)
      (.setWidth 2000)
      (.setText "Project"))

    tree))