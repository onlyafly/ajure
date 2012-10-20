;; tab

(ns ajure.ui.tab
  (:import (org.eclipse.swt.widgets Canvas)
           (org.eclipse.swt.custom CTabItem)
           (org.eclipse.swt.layout FillLayout))
  (:require (ajure.ui [status-bar :as status-bar]
                      [text-editor :as text-editor])
            (ajure.state [hooks :as hooks])
			(ajure.util [swt :as swt])))

;;TODO change to use :keys
(defn make! [display
             shell
             tab-folder
             title
             text-modified-action
             text-box-verify-key-action
             text-box-change-action
             dropped-file-paths-action
             get-style-range-functions]
  (io!
   (let [tab-item (CTabItem. tab-folder (swt/options NONE))
         tab-canvas (Canvas. tab-folder (swt/options NONE))
         text-editor-controls (text-editor/make! display
                                                 shell
                                                 tab-canvas
                                                 text-modified-action
                                                 text-box-verify-key-action
                                                 text-box-change-action
                                                 dropped-file-paths-action
                                                 get-style-range-functions)
         [text-canvas textbox numbering] text-editor-controls]
     
     (doto tab-canvas
       (.setLayout (FillLayout.)))
     
     (doto tab-item
       (.setText title)
       (.setControl tab-canvas))
     
     (.setSelection tab-folder tab-item)
     
     [tab-item text-canvas textbox numbering])))