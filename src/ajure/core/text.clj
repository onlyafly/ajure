;; text
;;
;; - Performs text operations on the current textbox

(ns ajure.core.text
  (:import (org.eclipse.swt.custom ST))
  (:require (ajure.state [doc-state :as doc-state]))
  (:use (ajure.state [doc-state :only (current)])
        (ajure.ui [access :only (def-menu)])
        ))

;; TODO there are many more actions in ST that can be used
(defn select-word-next! []
  (io!
   (.invokeAction (current :text-box) ST/SELECT_WORD_NEXT)))

(defn get-selection []
  (.getSelectionText (current :text-box)))

(defn replace-selection! [replacement-text]
  (io!
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
                         (.length replacement-text)))))))

(defn convert-selection-to-lowercase! []
  (io!
   (let [lowered-selection (.toLowerCase (get-selection))]
     (replace-selection! lowered-selection))))

(defn convert-selection-to-uppercase! []
  (io!
   (let [uppered-selection (.toUpperCase (get-selection))]
     (replace-selection! uppered-selection))))

(defn cut-text! []
  (io!
   (.cut (current :text-box))))

(defn copy-text! []
  (io!
   (.copy (current :text-box))))

(defn paste-text! []
  (io!
   (.paste (current :text-box))))

(defn select-all-text! []
  (io!
   (.selectAll (current :text-box))))

;;---------- Text Menu

(defn build-text-menu! []
  (io!
   (def-menu "Text"
     (:item "Change to Lowercase"
            (convert-selection-to-lowercase!))
     (:item "Change to Uppercase"
            (convert-selection-to-uppercase!)))))