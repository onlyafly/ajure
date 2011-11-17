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

(defn create-blank-document [textbox numbering canvas document-name]
  (struct document-struct
          textbox
          numbering
          canvas
          document-name
          nil ;filepath
          nil ;directory
          false ;modified
          [] ;undostack
          [] ;redostack
          text-format/line-ending-default
          "UTF-8"
          {}))

(defn create-document [textbox numbering canvas document-name
                     file-path file-directory line-ending
                     charset]
  (struct document-struct
          textbox
          numbering
          canvas
          document-name
          file-path ;filepath
          file-directory
          false ;modified
          [] ;undostack
          [] ;redostack
          line-ending
          charset
          {}))