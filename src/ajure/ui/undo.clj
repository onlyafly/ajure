;; undo

(ns ajure.ui.undo
  (:require (ajure.state [doc-state :as doc-state])))

(defn- update-for-redo [doc doc-id change]
  (->
   doc
   (update-in [doc-id :undo-stack] conj change)
   (update-in [doc-id :redo-stack] pop)))

(defn- update-for-undo [doc doc-id change]
  (->
   doc
   (update-in [doc-id :redo-stack] conj change)
   (update-in [doc-id :undo-stack] pop)))

(defn- update-for-change [doc doc-id change]
  (->
   doc
   (update-in [doc-id :undo-stack] conj change)

   ;; Clear the redo stack when a change occurs
   (assoc-in [doc-id :redo-stack] [])))

(defn- do-redo-update [change]
  (dosync
   (alter doc-state/docs
          update-for-redo @doc-state/current-doc-id change)))

(defn- redo-action! [text-box
                     change
                     before-change-action
                     after-change-action]
  (io!
   (before-change-action)
   
   (.replaceTextRange text-box
                      (:pos change)
                      (count (:a change))
                      (:b change))

   (when (> (.length (:b change)) 1)
     (.setSelection text-box
                    (:pos change)
                    (+ (:pos change)
                       (.length (:b change)))))

   ;; Move the caret to the correct position
   (if (zero? (count (:b change)))          
     (.setCaretOffset text-box (:pos change))
     (.setCaretOffset text-box (+ (:pos change)
                                  (count (:b change)))))

   (after-change-action)))

(defn- undo-action! [text-box
                     change
                     before-change-action
                     after-change-action]
  (io!
   (before-change-action)
   (.replaceTextRange text-box
                      (:pos change)
                      (:len change)
                      (:a change))

   (when (> (.length (:a change)) 1)
     (.setSelection text-box
                    (:pos change)
                    (+ (:pos change)
                       (.length (:a change)))))

   ;; Move the caret to the correct position
   (if (zero? (count (:a change)))          
     (.setCaretOffset text-box (:pos change))
     (.setCaretOffset text-box (+ (:pos change)
                                  (count (:a change)))))

   (after-change-action)))

(defn- do-undo-update [change]
  (dosync
   (commute doc-state/docs
            update-for-undo @doc-state/current-doc-id change)))

(defn do-record-text-change [a b pos len]
  (dosync
   (alter doc-state/docs
          update-for-change @doc-state/current-doc-id {:a a
                                                       :b b
                                                       :pos pos
                                                       :len len})))

(defn do-redo! [text-box
                before-change-action
                after-change-action]
  (when-let [change (last (doc-state/current :redo-stack))]
    (redo-action! text-box change before-change-action after-change-action)
    (do-redo-update change)))

(defn do-undo! [text-box
                before-change-action
                after-change-action]
  (when-let [change (last (doc-state/current :undo-stack))]
    (undo-action! text-box change before-change-action after-change-action)
    (do-undo-update change)))

