;; ajure.main

(ns ajure.main
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [init :as init]))
  (:gen-class))

(defn -main [& args]
  (init/launch-gui default-modules/init))
