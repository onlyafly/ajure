;; ajure.gui.fonts
;;
;; Encapsulates SWT operations on fonts

(ns ajure.gui.fonts
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.graphics Font FontData))
  (:require (ajure.state [hooks :as hooks])))

(defn get-name-and-height [^FontData font-data]
  (vector (.getName font-data)
          (.getHeight font-data)))

(defn create-font [^FontData font-data]
  (Font. @hooks/display font-data))