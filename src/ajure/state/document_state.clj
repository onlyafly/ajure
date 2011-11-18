(ns ajure.state.document-state
  (:require (ajure.state [document :as document])))

(def docs (ref {}))
(def creation-actions (ref {}))
(def last-doc-id (ref -1))
(def document-number (ref 1))
(def current-doc-id (ref nil))

(defn add-creation-action [key action]
  (dosync
   (commute creation-actions assoc
            key action)))

(defn set-current-doc-id [doc-id]
  {:pre [(integer? doc-id)]}
  (dosync
   (ref-set current-doc-id doc-id)))

(defn current
  ([]
     (if @current-doc-id
       (@docs @current-doc-id)
       nil))
  ([key]
     (if @current-doc-id
       (let [doc (@docs @current-doc-id)]
         (if doc
           (doc key)
           nil))
       nil)))

(defn- get-next-doc-id []
  (dosync
   (commute last-doc-id inc))
  @last-doc-id)

(defn- do-doc-state-initialization [doc]
  (let [doc-id (get-next-doc-id)]
    (dosync
     (commute docs
              assoc doc-id doc)
     (doseq [action (vals @creation-actions)]
       (commute docs
                update-in [doc-id] action)))
    doc-id))

(defn make-blank-doc [textbox numbering canvas document-name]
  (let [doc (document/create-blank-document textbox
                                            numbering
                                            canvas
                                            document-name)]
    (do-doc-state-initialization doc)))

(defn make-doc [textbox numbering canvas document-name
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
    (do-doc-state-initialization doc)))

;; FIXME make what can be private so

(defn get-unique-name []
  (let [name "Untitled "
        number @document-number
        combined (str name number)]
    (dosync
      (commute document-number #(+ 1 %)))
    combined))