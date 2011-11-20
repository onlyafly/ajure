;; ajure.core.text
;;
;; - Performs text operations on the current textbox

(ns ajure.core.text
  (:import (org.eclipse.swt.custom ST))
  (:require (ajure.state [doc-state :as doc-state]))
  (:use (ajure.state [doc-state :only (current)])
        (ajure.gui [access :only (def-menu)])))

;; TODO there are many more actions in ST that can be used
(defn do-select-word-next []
  (.invokeAction (current :text-box) ST/SELECT_WORD_NEXT))

(defn get-selection []
  (.getSelectionText (current :text-box)))

(defn replace-selection [replacement-text]
  (when replacement-text
    (let [range (.getSelectionRange (current :text-box))
          current-start (. range x)
          current-length (. range y)]
      (.replaceTextRange (current :text-box)
                         current-start
                         current-length
                         replacement-text)
      ;; .setSelection also scrolls the selection into view
      (.setSelection (current :text-box)
                     current-start
                     (+ current-start
                        (.length replacement-text))))))

(defn convert-selection-to-lowercase []
  (let [lowered-selection (.toLowerCase (get-selection))]
    (replace-selection lowered-selection)))

(defn convert-selection-to-uppercase []
  (let [uppered-selection (.toUpperCase (get-selection))]
    (replace-selection uppered-selection)))

(defn do-cut-text []
  (.cut (current :text-box)))
(defn do-copy-text []
  (.copy (current :text-box)))
(defn do-paste-text [] 
  (.paste (current :text-box)))
(defn do-select-all-text []
  (.selectAll (current :text-box)))

;;---------- Text Menu

(defn build-text-menu []
  (def-menu "Text"
    (:item "Change to Lowercase"
           (convert-selection-to-lowercase))
    (:item "Change to Uppercase"
           (convert-selection-to-uppercase))))