;; ajure.core.text
;;
;; - Performs text operations on the current textbox

(ns ajure.core.text
  (:import (org.eclipse.swt.custom ST))
  (:require (ajure.core [document :as document]))
  (:use (ajure.core [document :only (this)])
        (ajure.gui [access :only (def-menu)])))

;; TODO there are many more actions in ST that can be used
(defn do-select-word-next []
  (.invokeAction (this :textbox) ST/SELECT_WORD_NEXT))

(defn get-selection []
  (.getSelectionText (this :textbox)))

(defn replace-selection [replacement-text]
  (when replacement-text
    (let [range (.getSelectionRange (this :textbox))
          current-start (. range x)
          current-length (. range y)]
      (.replaceTextRange (this :textbox)
                         current-start
                         current-length
                         replacement-text)
      ;; .setSelection also scrolls the selection into view
      (.setSelection (this :textbox)
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
  (.cut (this :textbox)))
(defn do-copy-text []
  (.copy (this :textbox)))
(defn do-paste-text [] 
  (.paste (this :textbox)))
(defn do-select-all-text []
  (.selectAll (this :textbox)))

;;---------- Text Menu

(defn build-text-menu []
  (def-menu "Text"
    (:item "Change to Lowercase"
           (convert-selection-to-lowercase))
    (:item "Change to Uppercase"
           (convert-selection-to-uppercase))))