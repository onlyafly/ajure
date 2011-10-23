;; ajure.core.document

(ns ajure.core.document
  (:require (ajure.util [text-format :as text-format])))

(defstruct document-data-structure
  :textbox :numbering :canvas
  :docname :filepath  :directory
  :modified :undostack :redostack
  :endings :charset)

(def current (ref nil))
(def creation-actions (ref {}))

(defn add-creation-action [key action]
  (dosync
    (commute creation-actions assoc
             key action)))

(defn set-current [d]
  (dosync
    (ref-set current d)))

(defn this [key]
  (@@current key))

(def document-number (ref 1))

(defn get-unique-name []
  (let [name "Untitled "
        number @document-number
        combined (str name number)]
    (dosync
      (commute document-number #(+ 1 %)))
    combined))

(defn make-blank-document [textbox numbering canvas document-name]
  (let [doc (struct document-data-structure
                    textbox numbering canvas
                    document-name nil nil
                    false (ref []) (ref [])
                    text-format/line-ending-default "UTF-8")]
    (doseq [action (vals @creation-actions)]
      (action doc))
    doc))

(defn make-document [textbox numbering canvas document-name
                     file-name file-directory line-ending
                     charset]
  (let [doc (struct document-data-structure
                    textbox numbering canvas
                    document-name file-name file-directory
                    false (ref []) (ref [])
                    line-ending charset)]
    (doseq [action (vals @creation-actions)]
      (action doc))
    doc))

