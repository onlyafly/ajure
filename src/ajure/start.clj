;; ajure.start

(ns ajure.start
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [init :as init])))

(init/launch-gui default-modules/init)
