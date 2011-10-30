(defproject ajure "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]]

  ;; Necessary to prevent Leiningen from deleting the SWT jars, since
  ;; they are not in Clojars
  :disable-implicit-clean true)