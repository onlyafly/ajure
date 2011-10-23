;; ajure.start

(ns ajure.start
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [main :as main])))

(main/launch-gui default-modules/init)
