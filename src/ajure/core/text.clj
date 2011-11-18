;; ajure.core.text
;;
;; - Performs text operations on the current textbox

(ns ajure.core.text
  (:import (org.eclipse.swt.custom ST))
  (:require (ajure.state [document-state :as document-state]))
  (:use (ajure.state [document-state :only (current)])
        (ajure.gui [access :only (def-menu)])))

;; TODO there are many more actions in ST that can be used
(defn do-select-word-next []
  (.invokeAction (current :textbox) ST/SELECT_WORD_NEXT))

(defn get-selection []
  (.getSelectionText (current :textbox)))

(defn replace-selection [replacement-text]
  (when replacement-text
    (let [range (.getSelectionRange (current :textbox))
          current-start (. range x)
          current-length (. range y)]
      (.replaceTextRange (current :textbox)
                         current-start
                         current-length
                         replacement-text)
      ;; .setSelection also scrolls the selection into view
      (.setSelection (current :textbox)
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
  (.cut (current :textbox)))
(defn do-copy-text []
  (.copy (current :textbox)))
(defn do-paste-text [] 
  (.paste (current :textbox)))
(defn do-select-all-text []
  (.selectAll (current :textbox)))

;;---------- Text Menu

(defn build-text-menu []
  (def-menu "Text"
    (:item "Change to Lowercase"
           (convert-selection-to-lowercase))
    (:item "Change to Uppercase"
           (convert-selection-to-uppercase))))