(ns ajure.state.document
  (:require (ajure.util [text-format :as text-format])))

(defstruct document-struct
  :textbox
  :numbering
  :canvas
  :docname
  :filepath
  :directory
  :modified
  :undostack
  :redostack
  :endings
  :charset
  :style-range-function-map)

;TODO remove all refs from this structure

(defn create-blank-document [textbox numbering canvas document-name]
  (struct document-struct
          textbox
          numbering
          canvas
          document-name
          nil ;filepath
          nil ;directory
          false ;modified
          (ref []) ;undostack
          (ref []) ;redostack
          text-format/line-ending-default
          "UTF-8"
          {}))

(defn create-document [textbox numbering canvas document-name
                     file-name file-directory line-ending
                     charset]
  (struct document-struct
          textbox
          numbering
          canvas
          document-name
          file-name
          file-directory
          false ;modified
          (ref []) ;undostack
          (ref []) ;redostack
          line-ending
          charset
          {}))