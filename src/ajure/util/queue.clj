(ns ajure.util.queue)

(defn dequeue
  "Dequeue an element from a vector, treating it as a queue."
  [v]
  (if (zero? (count v))
    nil
    (subvec v 1)))

(defn enqueue
  "Enqueue an element onto a possibly nil vector, treating it as a queue."
  [v element]
  (vec (conj v element)))