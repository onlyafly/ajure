;; ajure.default-modules
;;
;; All modules that are installed by default are initialized
;; here.

(ns ajure.default-modules
  (:require ajure.module.replace-bar
            ajure.module.syntax-highlighting))

;; Use this function to initialize all default modules.
(defn init []

  ;; The replace-bar module
  (ajure.module.replace-bar/init)

  (ajure.module.syntax-highlighting/init)
  )