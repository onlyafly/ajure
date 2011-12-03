;; ajure.state.doc-state
;;
;; State related to open documents.

(ns ajure.state.doc-state
  (:require (ajure.state [doc :as doc])))

(def docs (ref {}))
(def doc-initializations (ref {}))
(def next-available-doc-id (ref 0))
(def current-doc-id (ref nil))

(defn do-add-doc-initialization [key action]
  {:pre [(keyword? key)]}
  (dosync
   (commute doc-initializations assoc
            key action)))

(defn do-set-current-doc-id [doc-id]
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

(defn- do-increment-next-available-doc-id []
  (dosync
   (commute next-available-doc-id inc)))

(defn- do-doc-state-initialization [doc-id doc]
  (dosync
   (do-increment-next-available-doc-id)
   (commute docs
            assoc doc-id doc)
   (doseq [action (vals @doc-initializations)]
     (commute docs
              update-in [doc-id] action))))

(defn get-next-available-doc-name []
  (str "Untitled "
       (inc @next-available-doc-id)))

(defn do-make-blank-doc [textbox numbering canvas doc-name]
  (let [doc-id @next-available-doc-id
        doc (doc/create-blank-doc textbox
                                  numbering
                                  canvas
                                  doc-name)]
    (do-doc-state-initialization doc-id doc)
    doc-id))

(defn do-make-doc [textbox numbering canvas doc-name
                   file-name file-directory line-ending
                   charset]
  (let [doc-id @next-available-doc-id
        doc (doc/create-doc textbox
                            numbering
                            canvas
                            doc-name
                            file-name
                            file-directory
                            line-ending
                            charset)]
    (do-doc-state-initialization doc-id doc)
    doc-id))