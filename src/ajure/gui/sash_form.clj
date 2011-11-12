(ns ajure.gui.sash-form
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.custom SashForm)
           (org.eclipse.swt.layout FillLayout))
  (:require (ajure.gui [file-tree :as file-tree]
                       [tab-folder :as tab-folder])
			(ajure.state [hooks :as hooks])
            (ajure.util [other :as other])))

(defn create-sash-form [parent
                        double-click-file-in-tree-action
                        close-tab-action
                        last-tab-closing-action
                        tab-selected-action]
  ; SWT/SMOOTH only affects Windows, dragging is always smooth in Mac
  (let [sash-form (SashForm. parent (other/bit-or-many SWT/SMOOTH SWT/HORIZONTAL))]
    
    (dosync (ref-set hooks/sash-form sash-form))

    (let [tree (file-tree/create-file-tree sash-form
                                           double-click-file-in-tree-action)
          tab-folder (tab-folder/create-tab-folder sash-form
                                                   close-tab-action
                                                   last-tab-closing-action
                                                   tab-selected-action)]

      ;; Setup Layout
      (doto sash-form
        (.setLayout (FillLayout.))
        (.setWeights (int-array [30 70])))

      sash-form)))