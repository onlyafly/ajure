;; ajure.gui.tab

(ns ajure.gui.tab
  (:import (org.eclipse.swt.widgets Canvas)
           (org.eclipse.swt.custom CTabItem)
           (org.eclipse.swt.layout FillLayout))
  (:require (ajure.gui [hooks :as hooks]
                       [status-bar :as status-bar]
                       [text-editor :as text-editor])
            (ajure.util [swt :as swt])))

(defn create-tab [title
                  text-modified-action
                  text-box-verify-key-action
                  text-box-change-action
                  dropped-file-paths-action]
  (let [shell @hooks/shell
        display @hooks/display
        tab-folder @hooks/tab-folder
        tab-item (CTabItem. tab-folder (swt/options NONE))
        tab-canvas (Canvas. tab-folder (swt/options NONE))
        [text-canvas textbox numbering]
          (text-editor/create-text-editor
            display shell tab-canvas
            text-modified-action
            text-box-verify-key-action
            text-box-change-action
            dropped-file-paths-action)]
    (doto tab-canvas
      (.setLayout (FillLayout.)))
    (doto tab-item
      (.setText title)
      (.setControl tab-canvas))
    (.setSelection tab-folder tab-item)
    [tab-item text-canvas textbox numbering]))