;; queue
;; - Queue operations on a vector.
;;
;; Used over clojure.lang.PersistentQueue because this queue needs to
;; be printed out as a vector when the settings file is output. The
;; inefficiency here is okay because these operations are rarely performed.

(ns ajure.other.queue)

(defn- vector-or-nil? [v]
  (or (vector? v)
      (nil? v)))

(defn dequeue
  "Dequeue an element from a vector, treating it as a queue."
  [v]
  {:pre [(vector-or-nil? v)]
   :post [(vector-or-nil? %)]}
  (if (zero? (count v))
    nil
    (subvec v 1)))

(defn enqueue
  "Enqueue an element onto a possibly nil vector, treating it as a queue."
  [v element]
  {:pre [(vector-or-nil? v)]
   :post [(vector-or-nil? %)]}
  (vec (conj v element)))