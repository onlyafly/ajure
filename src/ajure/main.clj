;; ajure.main
;;
;; The main entry point of the application.

(ns ajure.main
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [application :as application]))
  (:gen-class))

(defn -main [& args]
  (application/launch-gui default-modules/init))
