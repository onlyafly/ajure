(ns ajure.state.document-state
  (:require (ajure.state [document :as document])))

(def current (ref nil))
(def creation-actions (ref {}))

(defn add-creation-action [key action]
  (dosync
    (commute creation-actions assoc
             key action)))

(defn set-current [d]
  (dosync
    (ref-set current d)))

;;TODO

(defn this [key]
  ; Check if current has not been set yet.
  (if (= @current nil)
    nil
    (@@current key)))

;; FIXME make what can be private so
(def document-number (ref 1))

(defn get-unique-name []
  (let [name "Untitled "
        number @document-number
        combined (str name number)]
    (dosync
      (commute document-number #(+ 1 %)))
    combined))

(defn make-blank-document [textbox numbering canvas document-name]
  (let [doc (document/create-blank-document textbox
                                            numbering
                                            canvas
                                            document-name)]
    (doseq [action (vals @creation-actions)]
      (action doc))
    doc))

(defn make-document [textbox numbering canvas document-name
                     file-name file-directory line-ending
                     charset]
  (let [doc (document/create-document textbox
                                      numbering
                                      canvas
                                      document-name
                                      file-name
                                      file-directory
                                      line-ending
                                      charset)]
    (doseq [action (vals @creation-actions)]
      (action doc))
    doc))