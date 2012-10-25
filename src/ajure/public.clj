;; public
;; - Simple public API.

(ns ajure.public
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Font FontData))
  (:require (ajure.core [settings :as settings]
                        [file-utils :as file-utils]
                        )
            (ajure.ui [editors :as editors]
                      [info :as info]
                      [project :as project]
                      [recent :as recent]
                      [scripts :as scripts]
                      [status-bar :as status-bar]
                      [tabs :as tabs]
                      [window :as window])
			(ajure.state [hooks :as hooks])
            (ajure.cwt [swt :as swt])))

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
    (swt/show-info-dialog! shell title msg)))

(defn set-status-msg [msg]
  (status-bar/set-message! msg))

(defn set-word-wrap [enable]
  (editors/set-current-word-wrap enable))

(defn open-file [file-path]
  (tabs/open-file-in-new-tab! file-path))