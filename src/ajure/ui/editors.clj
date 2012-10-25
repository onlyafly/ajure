;; editors
;;
;; Should Do:
;;  - Operations related to the text editors

(ns ajure.ui.editors
  (:import (org.eclipse.swt.graphics FontData))
  (:require (ajure.ui [tabs :as tabs]
                      [text-editor :as text-editor]
                      [fonts :as fonts])
            (ajure.state [hooks :as hooks]
                         [doc-state :as doc-state])
            (ajure.cwt [swt :as swt]))
  (:use ajure.other.misc))

(defn set-current-word-wrap [enable]
  (dosync
    (commute hooks/settings assoc
             :word-wrap-enabled enable))
  (tabs/for-each-textbox! #(.setWordWrap % enable)))

(defn update-settings-from-editor-font []
  (let [font-data @hooks/editor-font-data
        name (.getName font-data)
        size (.getHeight font-data)]
    (dosync
      (commute hooks/settings assoc
               :font-name name
               :font-size size))))

(defn set-editor-font-data [font-data]
  ;; Redraw line numbers to reflect change in font
  (dosync
    (ref-set hooks/editor-font-data font-data))
  (text-editor/redraw-line-numbering! (doc-state/current :numbering))
  (let [font (fonts/create-font font-data)]
    (tabs/for-each-textbox! #(.setFont % font))))

(defn update-editor-font-from-settings []
  (let [name (@hooks/settings :font-name)
        size (@hooks/settings :font-size)]
    (when (and (str-not-empty? name)
               size)
      (let [font-data (FontData. name size (swt/options NORMAL))]
        (set-editor-font-data font-data)))))

