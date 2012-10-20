;; sash-form

(ns ajure.ui.sash-form
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.custom SashForm)
           (org.eclipse.swt.layout FillLayout))
  (:require
   (ajure.ui [file-tree :as file-tree]
             ;;FIX[tab-folder :as tab-folder]
             )
   (ajure.state [hooks :as hooks])
   (ajure.util [other :as other])))

(defn make! [& {:keys [parent
                      layout-data
                      on-double-click-file-in-tree
                      on-close-tab
                      on-close-last-tab
                      on-tab-selected]}]
  
  ;; SWT/SMOOTH only affects Windows, dragging is always smooth in Mac
  (let [sash-form (SashForm. parent (other/bit-or-many SWT/SMOOTH
                                                       SWT/HORIZONTAL))

        file-tree (file-tree/make! sash-form
                                   on-double-click-file-in-tree)

        ;; ;;FIX
        ;; tab-folder (tab-folder/create-tab-folder! sash-form
        ;;                                           on-close-tab
        ;;                                           on-close-last-tab
        ;;                                           on-tab-selected)
        
        ]
    
    ;; Setup Layout
    (doto sash-form
      (.setLayout (FillLayout.))
      ;;FIX (.setWeights (int-array [30 70]))
      (.setLayoutData layout-data))

    {:sash-form sash-form
     :file-tree file-tree
     ;;FIX:tab-folder tab-folder
     }))