;; ajure.core.undo

(ns ajure.core.undo
  (:require (ajure.state [document-state :as document-state])
            (ajure.gui [text-editor :as text-editor])))

(defn- update-for-redo [doc doc-id change]
  (->
   doc
   (update-in [doc-id :undostack] conj change)
   (update-in [doc-id :redostack] pop)))

(defn- update-for-undo [doc doc-id change]
  (->
   doc
   (update-in [doc-id :redostack] conj change)
   (update-in [doc-id :undostack] pop)))

(defn- update-for-change [doc doc-id change]
  (->
   doc
   (update-in [doc-id :undostack] conj change)

   ;; Clear the redo stack when a change occurs
   (assoc-in [doc-id :redostack] [])))

(defn do-text-change [a b pos len]
  (dosync
   (commute document-state/docs
            update-for-change @document-state/current-doc-id {:a a :b b :pos pos :len len})))

(defn do-redo [text-box
               before-change-action
               after-change-action]
  (let [change (last (document-state/current :redostack))]
    (when change

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

      (after-change-action)

      (dosync
       (commute document-state/docs
                update-for-redo @document-state/current-doc-id change)))))

(defn do-undo [text-box
               before-change-action
               after-change-action]
  (let [change (last (document-state/current :undostack))]
    (when change

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

      (after-change-action)

      (dosync
       (commute document-state/docs
                update-for-undo @document-state/current-doc-id change)))))

