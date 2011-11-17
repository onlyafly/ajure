;; ajure.core.undo

(ns ajure.core.undo
  (:require (ajure.state [document-state :as document-state])
            (ajure.gui [text-editor :as text-editor])))

(defn- update-for-redo [doc change]
  (->
   doc
   (update-in [:undostack] conj change)
   (update-in [:redostack] pop)))

(defn- update-for-undo [doc change]
  (->
   doc
   (update-in [:redostack] conj change)
   (update-in [:undostack] pop)))

(defn- update-for-change [doc change]
  (->
   doc
   (update-in [:undostack] conj change)

   ;; Clear the redo stack when a change occurs
   (assoc :redostack [])))

(defn do-text-change [a b pos len]
  (dosync
   (commute @document-state/current
            update-for-change {:a a
                               :b b
                               :pos pos
                               :len len})))

(defn do-redo [text-box
               before-change-action
               after-change-action]
  (let [change (last (document-state/this :redostack))]
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
       (commute @document-state/current
                update-for-redo change)))))

(defn do-undo [text-box
               before-change-action
               after-change-action]
  (let [change (last (document-state/this :undostack))]
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
       (commute @document-state/current
                update-for-undo change)))))

