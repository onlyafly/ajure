(ns ajure.default-modules
  (:require (ajure.module [replace-bar :as rb])))

(defn init []
  (rb/init)
  )