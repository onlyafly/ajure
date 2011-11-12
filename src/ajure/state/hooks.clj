;; hooks
;;
;; Should:
;;  - Variables to hold objects that need to be accessed globally

(ns ajure.state.hooks
  (:require (ajure.util [platform :as platform])))

;;---------- Unique per application

(def display (ref nil))

;; This cannot be left nil
(def editor-font-data (ref (platform/get-default-font-data)))

;; All persistent storage saved in this map
(def settings (ref {}))

;;---------- Unique per shell

(def shell (ref nil))
(def menu-bar (ref nil))
(def sash-form (ref nil))
(def tab-folder (ref nil))
(def popup-menu (ref nil))
(def file-tree (ref nil))
(def find-text (ref ""))
(def find-case-sensitive (ref false))