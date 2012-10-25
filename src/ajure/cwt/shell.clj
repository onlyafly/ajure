;; shell
;; - Shell (window) wrapper.

(ns ajure.cwt.shell
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Shell)
           (org.eclipse.swt.layout GridLayout GridData)
           (org.eclipse.swt.events ShellAdapter)
           (org.eclipse.swt.graphics Image GC))
  (:require (ajure.os [platform :as platform])
            (ajure.cwt [swt :as swt])
            (ajure.other [misc :as misc])))

;;---------- Private

(defn- create-shell-grid-layout []
  (let [layout (GridLayout.)]
    (set! (. layout numColumns) 1)
    (set! (. layout marginHeight) 0)
    (set! (. layout marginWidth) 0)
    (set! (. layout verticalSpacing) 0)
    (set! (. layout horizontalSpacing) 0)
    layout))

;;---------- Public

(defn show! [shell]
  (io!
   (.open shell)))

(defn make! [& {:keys [display
                       title
                       icon
                       size
                       on-quit-should-close?]}]
  (io!
   (let [shell (Shell. display)
         [x y] size]
     (doto shell
       
       (.setText title)
       (.setImage icon)
       (.setSize x y)

       (.setLayout (create-shell-grid-layout))

       ;; Program exit point.
       ;; This is called when the shell is closed using the X at the top
       ;; or when Alt+F4 is pressed in Windows
       (.addShellListener
        (proxy [ShellAdapter] []
          (shellClosed [event]
            (set! (. event doit) (on-quit-should-close?)))))))))