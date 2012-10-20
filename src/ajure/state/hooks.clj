;; hooks
;; - Variables to hold objects that need to be accessed globally.

(ns ajure.state.hooks
  (:require (ajure.util [platform :as platform])))

;;---------- Unique per application

(def display (ref nil))

;; This cannot be left nil
(def editor-font-data (ref (platform/get-default-font-data)))

;; All persistent storage saved in this map
(def settings (ref {}))

(def bank (ref {}))

(def editor-key-combos (ref {}))
(def application-key-combos (ref {}))

;;---------- Unique per shell

(def shell (ref nil))
(def menu-bar (ref nil))
(def sash-form (ref nil))
(def tab-folder (ref nil))
(def popup-menu (ref nil))
(def file-tree (ref nil))

;; Find
(def find-text (ref ""))
(def find-case-sensitive (ref false))

;; Status Bar
(def status-bar (ref nil))
(def app-status-label (ref nil))
(def doc-status-label (ref nil))

;; Document Label Information
(def current-endings (ref ""))
