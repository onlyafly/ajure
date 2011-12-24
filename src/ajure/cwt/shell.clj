;; shell
;; - Shell (window) wrapper.

(ns ajure.cwt.shell
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.widgets Shell)
           (org.eclipse.swt.layout GridLayout GridData)
           (org.eclipse.swt.events ShellAdapter)
           (org.eclipse.swt.graphics Image GC))
  (:require (ajure.util [platform :as platform]
                        [swt :as swt]
                        [other :as other])))

(defn- create-shell-grid-layout []
  (let [layout (GridLayout.)]
    (set! (. layout numColumns) 1)
    (set! (. layout marginHeight) 0)
    (set! (. layout marginWidth) 0)
    (set! (. layout verticalSpacing) 0)
    (set! (. layout horizontalSpacing) 0)
    layout))

;;---------- Internal

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

       ;; Program exit point.
       ;; This is called when the shell is closed using the X at the top
       ;; or when Alt+F4 is pressed in Windows
       (.addShellListener
        (proxy [ShellAdapter] []
          (shellClosed [event]
            (set! (. event doit) (on-quit-should-close?)))))
       
       (.setSize x y)))))

;; TODO transition to new versionP
#_(defn make! [display
             double-click-file-in-tree-action
             close-tab-action
             last-tab-closing-action
             tab-selected-action
             verify-everything-saved-then-close?]
  (io!
   (let [shell (Shell. display)
         sash-form-controls (sash-form/create-sash-form! shell
                                                         double-click-file-in-tree-action
                                                         close-tab-action
                                                         last-tab-closing-action
                                                         tab-selected-action)
         [sash-form file-tree tab-folder] sash-form-controls
         [status-bar app-label doc-label] (status-bar/create-status-bar! shell)]

     ;; Setup layout
     (doto shell
       (.setLayout (create-shell-grid-layout)))
     (doto sash-form
       (.setLayoutData (GridData. SWT/FILL SWT/FILL true true)))

     (doto status-bar
       (.setLayoutData (let [data (GridData. SWT/FILL SWT/END true false)]
                         ;; This would allow the item to span 2 columns
                         ;; (set! (. data horizontalSpan) 2)
                         data)))

     (doto shell
       (.setText info/application-name)

       (.setImage (@hooks/images :logo))

       ;; Program exit point
       ;; This is called when the shell is closed using the X at the top
       ;;  or when Alt+F4 is pressed in Windows
       (.addShellListener
        (proxy [ShellAdapter] []
          (shellClosed [event]
            (let [should-close (verify-everything-saved-then-close?)]
              (set! (. event doit) should-close)))))
       
       (.setSize 880 700))

     {:shell shell
      :sash-form sash-form
      :tab-folder tab-folder
      :status-bar status-bar
      :app-label app-label
      :file-tree file-tree
      :doc-label doc-label})))