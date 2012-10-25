;; swt
;;
;; Should
;;  - Provide access to basic SWT functionality

(ns ajure.cwt.swt
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.dnd DND DropTarget Transfer FileTransfer
                                DropTargetAdapter DropTargetEvent
                                DropTargetEffect)
           (org.eclipse.swt.widgets Control Display Menu MenuItem
                                    MessageBox FontDialog Shell)
           (org.eclipse.swt.layout GridLayout)
           (org.eclipse.swt.events SelectionAdapter VerifyEvent)
           (org.eclipse.swt.graphics RGB Color Point))
  (:require (ajure.other [misc :as misc])
            (ajure.os [platform :as platform])))

(defn basic-loop! [^Display display
                   ^Shell shell
                   & {:keys [on-release on-exception]}]
  (io!
   (loop []
     (if (.isDisposed shell)
       
       ;; Program ending
       (do
         (.dispose display)
         (when on-release
           (on-release)))
       
       ;; Loop until program ends
       (do
         (try
           (when-not (.readAndDispatch display)
             (.sleep display))
           (catch Exception ex
             (if on-exception
               (on-exception ex)
               (throw ex))))
         (recur))))))

(defn create-menu-bar! [shell]
  (io!
   (Menu. shell SWT/BAR)))

(defn create-popup-menu! [shell]
  (io!
   (Menu. shell SWT/POP_UP)))

(defn create-sub-menu!
  ([shell parent-menu title]
     (io!
      (let [menu-item (MenuItem. parent-menu SWT/CASCADE)
            sub-menu (Menu. shell SWT/DROP_DOWN)]
        (.setText menu-item title)
        (.setMenu menu-item sub-menu)
        sub-menu)))
  ([shell parent-menu title index]
     (io!
      (let [menu-item (MenuItem. parent-menu SWT/CASCADE index)
            sub-menu (Menu. shell SWT/DROP_DOWN)]
        (.setText menu-item title)
        (.setMenu menu-item sub-menu)
        sub-menu))))

(defn create-cascading-sub-menu! [parent-menu title]
  (io!
   (let [menu-item (MenuItem. parent-menu SWT/CASCADE)
         sub-menu (Menu. parent-menu)]
     (.setText menu-item title)
     (.setMenu menu-item sub-menu)
     sub-menu)))

(defn create-menu-separator! [parent-menu]
  (io!
   (MenuItem. parent-menu SWT/SEPARATOR)))

(defn create-menu-item! [parent-menu title action]
  (io!
   (let [menu-item (MenuItem. parent-menu SWT/PUSH)]
     (.setText menu-item title)
     (.addSelectionListener menu-item
                            (proxy [SelectionAdapter] []
                              (widgetSelected [evt] (action))))
     menu-item)))

(defn create-check-menu-item! [parent-menu title action]
  (io!
   (let [menu-item (MenuItem. parent-menu SWT/CHECK)]
     (.setText menu-item title)
     (.addSelectionListener menu-item
                            (proxy [SelectionAdapter] []
                              (widgetSelected [evt] (action))))
     menu-item)))

;;TODO can the loop be replaced by doseq?
(defn get-child-menu-item [parent-menu title]
  (when parent-menu
    (let [children (seq (.getItems parent-menu))]
      (loop [child (first children)
             more-children (next children)]
        (when child
          (if (= title (.getText child))
            child
            (recur (first more-children)
                   (next more-children))))))))

(defn get-child-sub-menu [parent-menu title]
  (when parent-menu
    (let [item (get-child-menu-item parent-menu title)]
      (when item
        (.getMenu item)))))

(defn show-warning-dialog! [shell title msg]
  (io!
   (let [box (MessageBox. shell (misc/bit-or-many SWT/ICON_WARNING
                                                   SWT/OK))]
     (doto box
       (.setText title)
       (.setMessage msg))
     (.open box))))

(defn show-info-dialog! [shell title msg]
  (io!
   (let [box (MessageBox. shell (misc/bit-or-many SWT/ICON_INFORMATION
                                                   SWT/OK))]
     (doto box
       (.setText title)
       (.setMessage msg))
     (.open box))))

(defn show-confirmation-dialog! [shell title msg]
  (io!
   (let [box (MessageBox. shell (misc/bit-or-many SWT/ICON_WARNING SWT/YES
                                                   SWT/NO SWT/CANCEL))]
     (doto box
       (.setText title)
       (.setMessage msg))
     (.open box))))

(defn show-font-dialog! [shell title default-font-data]
  (io!
   (let [dialog (FontDialog. shell SWT/NONE)]
     (doto dialog
       (.setText title)
       (.setRGB (RGB. 0 0 0))
       (.setFontData default-font-data))
     (let [new-font-data (.open dialog)
           new-color (.getRGB dialog)]
       new-font-data))))

(defn center-shell! [^Display display
                     ^Shell shell]
  (io!
   (let [monitor (.getPrimaryMonitor display)
         monitor-bounds (.getBounds monitor)
         shell-bounds (.getBounds shell)
         x (+ (. monitor-bounds x)
              (/ (- (. monitor-bounds width)
                    (. shell-bounds width))
                 2))
         y (+ (. monitor-bounds y)
              (/ (- (. monitor-bounds height)
                    (. shell-bounds height))
                 2))]
     (.setLocation shell x y))))

(defn event->mask-and-char-vector [event]
  (let [mask (int (. event stateMask))
        
        ; The keyCode field can be an integer corresponding to a
        ; character or an SWT constant, so need to test that it is a
        ; character before converting to char.
        keycode (. event keyCode)
        is-keycode-valid (and (>= keycode (int Character/MIN_VALUE))
                              (<= keycode (int Character/MAX_VALUE)))
        
        actual-char (if is-keycode-valid
                      (char keycode)
                      (. event character))]
    
    [mask actual-char]))

;TODO optimize this function to improve performance of entering text
(defn key-combo? [^VerifyEvent event
                  modifier-vector
                  expected-char]
  (let [[mask actual-char] (event->mask-and-char-vector event)
        is-matched-char (= expected-char actual-char)
        mapped-mask (map #(bit-and mask %) modifier-vector)
        is-matched-mask (every? #(not= 0 %) mapped-mask)]
    (and is-matched-mask is-matched-char)))

;;TODO optimize this function to improve performance of entering text
(defn execute-key-combo-in-mappings!
  "Finds key combo in mappings and executes the mapped action.
  mappings example:
  {[[SWT/MOD1] \\o] action1
   [[SWT/MOD2] \\a] action2}"
  
  ;; Cannot annotate event as a ^VerifyEvent or a ^Event since it can
  ;; be either one as passed from main/application-key-down-action
  [event mappings match-found-action]

  (io!
   (let [[mask actual-char] (event->mask-and-char-vector event)]
     (loop [remaining-mappings mappings]
       (if remaining-mappings
         (let [[[mod-vector expected-char] action] (first remaining-mappings)
               is-matched-char (= expected-char actual-char)
               is-matched-mask (= 0 (apply misc/bit-xor-many mask mod-vector))]
           (if (and is-matched-mask is-matched-char)
             (do
               (action)
               (match-found-action))
             (recur (next remaining-mappings)))))))))

(defn add-file-dropping-to-control! [^Control control
                                     drop-action]
  (io!
    (let [drop-target (DropTarget. control (misc/bit-or-many DND/DROP_DEFAULT
                                                              DND/DROP_MOVE))
          file-transfer (FileTransfer/getInstance)
          transfer-array (into-array FileTransfer [file-transfer])]
      (doto drop-target
        (.setTransfer transfer-array)
        
        ;; This is necessary to override the default drop effect for the
        ;; control.  Without this, the cursor in the StyledText tries to
        ;; follow the file dropping
        (.setDropTargetEffect
         (DropTargetEffect. control))

        (.addDropListener
         (proxy [DropTargetAdapter] []
           (drop [#^DropTargetEvent event]
             (let [current-transfer (FileTransfer/getInstance)
                   current-type (. event currentDataType)]
               (when (.isSupportedType current-transfer current-type)
                 (drop-action (seq (. event data))))))))))))

(defn get-combo-string
  ([modifiers c]
     (let [match? (fn [modifier] (some #{modifier} modifiers))]
       (str (if (match? 'CTRL) platform/ctrl-string "")
            (if (match? 'ALT) platform/alt-string "")
            (if (match? 'SHIFT) platform/shift-string "")
            (if (match? 'COMMAND) platform/command-string "")
            (if (match? 'MOD1) platform/mod1-string "")
            (.toUpperCase (str c)))))
  ([event]
     (let [mask (. event stateMask)
           c (char (. event keyCode))
           key? (fn [modifier] (not= 0 (bit-and mask modifier)))]
       (if (zero? mask)
         ""
         (str (if (key? SWT/CTRL) platform/ctrl-string "")
              (if (key? SWT/ALT) platform/alt-string "")
              (if (key? SWT/SHIFT) platform/shift-string "")
              (if (key? SWT/COMMAND) platform/command-string "")
              (.toUpperCase (str c)))))))

(defmacro options [& options]
  `(misc/bit-or-many 
    ~@(map (fn [x] (symbol "org.eclipse.swt.SWT" (str x)))
           options)))

(defn dynamically-show-control! [control]
  (io!
   (when control
     (let [layout-data (.getLayoutData control)]
       (when layout-data
         (set! (. layout-data exclude) false)))
     (.setVisible control true)
     (let [parent (.getParent control)]
       (.layout parent)))))

(defn dynamically-hide-control! [control]
  (io!
   (when control
     (let [layout-data (.getLayoutData control)]
       (when layout-data
         (set! (. layout-data exclude) true)))
     (.setVisible control false)
     (let [parent (.getParent control)]
       (.layout parent)))))

(defn point->vector [#^Point point]
  (if point
    [(. point x) (. point y)]
    []))