(ns ajure.threaded
  (:require (ajure [default-modules :as default-modules])
            (ajure.core [main :as main])))

(defn start-gui []
  (main/launch-gui default-modules/init))

;;---------- Repl debugging support

(defn thread-gui []
  "Launch GUI interface in a thread to enable repl debugging"
  (let [thread (Thread. start-gui)]
    (.start thread)))

(defmacro exec [& body]
  `(.syncExec (deref hooks/display)
     (proxy [Runnable] []
       (run []
            ~@body))))

(defmacro pexec [& body]
  `(.syncExec (deref hooks/display)
     (proxy [Runnable] []
       (run []
            (println ~@body)))))