;; See
;; http://stackoverflow.com/questions/4688336/what-is-an-elegant-way-to-set-up-a-leiningen-project-that-requires-different-dep
(let [properties (select-keys (into {} (System/getProperties))
                              ["os.arch" "os.name"])
      platform (apply format "%s (%s)" (vals properties))
      swt (case platform
            ;; See http://stackoverflow.com/questions/5096299/maven-project-swt-3-5-dependency-any-official-public-repo
            "Windows 7 (x86)" '[org.eclipse.swt/org.eclipse.swt.win32.win32.x86        "3.8"]
            "Windows 7 (x86_64)" '[org.eclipse.swt/org.eclipse.swt.win32.win32.x86_64  "3.8"]
            "Mac OS X (x86_64)"  '[org.eclipse.swt/org.eclipse.swt.cocoa.macosx.x86_64 "3.8"])
      start-on-first-thread (case platform
                              "Mac OS (x86_64)" "-XstartOnFirstThread"
                              "")] 

  (defproject ajure "0.5.1-dev"
    
    :description "Ajure Text Editor"
    
    :dependencies
    [
     [org.clojure/clojure "1.3.0"]
     [org.clojure/clojure-contrib "1.2.0"]
     ~swt
     ]

    :jvm-opts
    [
     ~start-on-first-thread
     ]
    
    :plugins
    [
     ;; Documentation generator, call using `lein marg`
     [lein-marginalia "0.7.1"]
     ]
    
    :repositories [["swt-repo" "https://swt-repo.googlecode.com/svn/repo/"]]
    
    ;; Specifies the main entry point of the program
    :main ajure.main))