(defproject ajure "0.4.1-dev"
  :description "Ajure Text Editor"
  :dependencies [[org.clojure/clojure "1.3.0"]]

  ;; Necessary to prevent Leiningen from deleting the SWT jars, since
  ;; they are not in Clojars
  :disable-implicit-clean true

  ;; Where to put the JARs
  :target-dir "bin"
  
  :dev-dependencies
  [
   ;; Documentation generator, call using `lein marg`
   [lein-marginalia "0.6.0"]

   ;; The canonical autodoc, which has dependencies missing
   ;; [autodoc "0.7.1"]
  ]

  ;; Specifies the main entry point of the program
  :main ajure.main)