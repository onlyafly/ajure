;; start
;; - Allows the application to be started as a Clojure script.
;;
;; How to use:
;;
;;  - From the REPL, enter the following to start the application:
;;      (use 'ajure.start)
;;
;;  - From the command line, launch the application (classpath details
;;    left out):
;;      java -cp .... clojure.main src\ajure\start.clj

(ns ajure.start
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [application :as application])))

;;FIX (default-modules/init)
(application/start!)
