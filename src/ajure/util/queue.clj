;; ajure.util.queue
;;  - Queue operations on a vector.
;;
;; Used over clojure.lang.PersistentQueue because this queue needs to
;; be printed out as a vector when the settings file is output. The
;; inefficiency here is okay because these operations are rarely performed.

(ns ajure.util.queue)

(defn dequeue
  "Dequeue an element from a vector, treating it as a queue."
  [v]
  {:pre [(vector? v)]}
  (if (zero? (count v))
    nil
    (subvec v 1)))

(defn enqueue
  "Enqueue an element onto a possibly nil vector, treating it as a queue."
  [v element]
  {:pre [(vector? v)]}
  (vec (conj v element)))