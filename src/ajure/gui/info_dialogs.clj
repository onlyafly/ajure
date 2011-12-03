;; ajure.gui.info-dialogs

(ns ajure.gui.info-dialogs
  (:import (org.eclipse.swt SWT))
  (:require (ajure.state [hooks :as hooks])
            (ajure.util [swt :as swt]
                        [info :as info])))

(defn show-about-box []
  (swt/show-info-dialog! @hooks/shell
    "About Ajure"
    (str "Ajure Editor " info/version-number-string "\n"
         info/version-date-string "\n"
         "\n"
         info/copyright-string "\n"
         info/website-string)))

(defn confirm-action [title msg yes-action no-action cancel-action]
  (let [result (swt/show-confirmation-dialog! @hooks/shell title msg)]
    (cond
      (= result SWT/YES) (yes-action)
      (= result SWT/NO) (no-action)
      (= result SWT/CANCEL) (cancel-action))))

(defn warn-file-not-readable [file-path]
  (swt/show-warning-dialog! @hooks/shell "Unable to open file"
                           (str "Permission denied on file \""
                                file-path 
                                "\".")))

(defn warn-file-not-exists [file-path]
  (swt/show-warning-dialog! @hooks/shell "Unable to open file"
                           (str "File \""
                                file-path 
                                "\" does not exist.")))