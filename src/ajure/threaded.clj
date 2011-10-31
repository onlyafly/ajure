(ns ajure.threaded
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [init :as init])))

(defn start-gui []
  (init/launch-gui default-modules/init))

;;---------- Repl debugging support

;; How to use:
;; - From the REPL, execute (require 'ajure.threaded)
;; - Execute (ajure.threaded/thread-gui)
(defn thread-gui []
  "Launch GUI interface in a thread to enable repl debugging"
  (let [thread (Thread. start-gui)]
    (.start thread)))

;; To simplify debugging on the REPL, this function allows you to
;; execute a command within the thread started above.
(defmacro exec [& body]
  `(.syncExec (deref hooks/display)
     (proxy [Runnable] []
       (run []
            ~@body))))

;; Does the same as "exec" above, but also prints the result.
(defmacro pexec [& body]
  `(.syncExec (deref hooks/display)
     (proxy [Runnable] []
       (run []
            (println ~@body)))))