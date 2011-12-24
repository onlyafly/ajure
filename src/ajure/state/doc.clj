;; doc
;; - Document structure and creation helper functions.

(ns ajure.state.doc
  (:require (ajure.util [text-format :as text-format])))

(defstruct Doc
  :text-box
  :numbering
  :canvas
  :doc-name
  :file-path
  :directory
  :is-modified
  :undo-stack
  :redo-stack
  :endings
  :character-set
  :style-range-function-map)

(defn create-blank-doc [text-box numbering canvas doc-name]
  (struct Doc
          text-box
          numbering
          canvas
          doc-name
          nil    ;file-path
          nil    ;directory
          false  ;is-modified
          []     ;undo-stack
          []     ;redo-stack
          text-format/line-ending-default
          "UTF-8"
          {}     ;style-range-function-map
          ))

(defn create-doc [text-box numbering canvas doc-name
                  file-path directory line-ending
                  character-set]
  (struct Doc
          text-box
          numbering
          canvas
          doc-name
          file-path        ;file-path
          directory
          false            ;is-modified
          []               ;undo-stack
          []               ;redo-stack
          line-ending
          character-set
          {}               ;style-range-function-map
          ))