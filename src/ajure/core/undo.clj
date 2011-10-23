;; ajure.core.undo

(ns ajure.core.undo
  (:require (ajure.core [document :as document])
            (ajure.gui [text-editor :as text-editor])))

(defn add-change [a b pos len]
  (dosync
    (commute (document/this :undostack) conj {:a a :b b :pos pos :len len})

    ;; Clear the redo stack when a change occurs
    (ref-set (document/this :redostack) [])))

(defn do-redo [text-box
               before-change-action
               after-change-action]
  (let [change (last @(document/this :redostack))]
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
       (commute (document/this :undostack) conj change)
       (commute (document/this :redostack) pop)))))

(defn do-undo [text-box
               before-change-action
               after-change-action]
  (let [change (last @(document/this :undostack))]
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
        (commute (document/this :redostack) conj change)
        (commute (document/this :undostack) pop)))))