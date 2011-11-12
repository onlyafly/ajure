;; ajure.core.undo

(ns ajure.core.undo
  (:require (ajure.state [document-state :as document-state])
            (ajure.gui [text-editor :as text-editor])))

(defn add-change [a b pos len]
  (dosync
    (commute (document-state/this :undostack) conj {:a a :b b :pos pos :len len})

    ;; Clear the redo stack when a change occurs
    (ref-set (document-state/this :redostack) [])))

(defn do-redo [text-box
               before-change-action
               after-change-action]
  (let [change (last @(document-state/this :redostack))]
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
       (commute (document-state/this :undostack) conj change)
       (commute (document-state/this :redostack) pop)))))

(defn do-undo [text-box
               before-change-action
               after-change-action]
  (let [change (last @(document-state/this :undostack))]
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
        (commute (document-state/this :redostack) conj change)
        (commute (document-state/this :undostack) pop)))))