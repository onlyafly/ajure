;TODO document the public API

(ns ajure.public
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Font FontData))
  (:require (ajure.core [settings :as settings]
                        [file-utils :as file]
                        [scripts :as scripts]
                        [window :as window]
                        [editors :as editors]
                        [tabs :as tabs]
                        [recent :as recent]
                        [project :as project])
            (ajure.gui [hooks :as hooks]
                       [status-bar :as status-bar])
            (ajure.util [swt :as swt]
                        [info :as info])))

(defn set-font [name size]
  (dosync
    (commute hooks/settings assoc
             :font-name name
             :font-size size))
  (editors/update-editor-font-from-settings))

(defn run-script [file-name]
  (scripts/try-load-file file-name))

(defn show-msg-box [title msg]
  (let [shell @hooks/shell]
    (swt/show-info-dialog shell title msg)))

(defn set-status-msg [msg]
  (status-bar/set-message msg))

(defn set-word-wrap [enable]
  (editors/set-current-word-wrap enable))

(defn open-file [file-path]
  (tabs/open-file-in-new-tab file-path))