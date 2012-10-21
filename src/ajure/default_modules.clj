;; default-modules
;; - Initialize default modules.

(ns ajure.default-modules
  (:require ajure.module.replace-bar
            ajure.module.syntax-highlighting))

;; Use this function to initialize all default modules.
(defn init []
  (ajure.module.replace-bar/init)
  (ajure.module.syntax-highlighting/init)
  )