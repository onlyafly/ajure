;; repl
;; - Allows the application to be executed and debugged from the REPL.
;;
;; How to use:
;;
;;  - From the REPL, enter the following to load this namespace:
;;    (use 'ajure.repl)
;;
;;  - To launch the GUI:
;;    (start-gui)
;;
;;  - To debug the GUI by launching it in a separate thread:
;;    (thread-gui)

(ns ajure.repl
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [application :as application])
            (ajure.state [hooks :as hooks])))

;;---------- REPL launch support

(defn start-gui []
  (application/start! :on-ui-ready default-modules/init))

;;---------- REPL debugging support

;; How to use:
;; - From the REPL, execute (require 'ajure.threaded)
;; - Execute (ajure.threaded/thread-gui)
(defn thread-gui []
  "Launch GUI interface in a thread to enable repl debugging"
  (let [thread (Thread. ^Runnable start-gui)]
    (.start thread)))

;; To simplify debugging on the REPL, this function allows you to
;; execute a command within the thread started above.
(defmacro exec [& body]
  `(.syncExec (deref hooks/display)
              (reify Runnable
                (run [this]
                  ~@body))))

;; Does the same as "exec" above, but also prints the result.
(defmacro pexec [& body]
  `(.syncExec (deref hooks/display)
              (reify Runnable
                (run [this]
                  (println ~@body)))))