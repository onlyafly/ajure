;; mac
;;
;; - Mac OS X specific functionality that should not be part of non-mac
;;   releases

(ns ajure.util.mac
  (import (org.eclipse.swt.internal.carbon OS HICommand)
          (org.eclipse.swt.internal Callback)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mac Application Menu
;; TODO: not complete

(def kHICommandAbout
     (+ (bit-shift-left (int \a) 24)
        (bit-shift-left (int \b) 16)
        (bit-shift-left (int \o) 8)
        (int \u)))

(def callback-proxy
  (proxy [Object] []
    (commandProc [next-handler the-event user-data]
      (if (= (OS/GetEventKind the-event) (OS/kEventProcessCommand))
        (let [command (HICommand.)]
          (OS/GetEventParameter the-event OS/kEventParamDirectObject
                                OS/typeHICommand nil HICommand/sizeof
                                nil command)
          (if (= (. command commandID) kHICommandAbout)
            (handleAboutCommand)
            OS/eventNotHandledErr))))))

(defn handleAboutCommand []
  (println "about clicked")
  OS/noErr)

(defn hook-app-menu! [^org.eclipse.swt.widgets.Display display
                      ^org.eclipse.swt.widgets.Shell shell]
  (let [about-menu-item-name "About"
        command-callback (Callback. callback-proxy "commandProc" 3)
        command-proc-handle (.getAddress command-callback)]
    (if (zero? command-proc-handle)
      (.dispose command-callback)
      ()
      )))