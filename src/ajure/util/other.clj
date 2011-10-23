;; ajure.util.other
;; (Fully refactored)
;;
;; Should:
;;  - Provide generally useful functions

(ns ajure.util.other)

(defn bit-or-many
  ([x] x)
  ([x y] (bit-or x y))
  ([x y & more] (bit-or x (reduce bit-or y more))))

(defn bit-xor-many
  ([x] x)
  ([x y] (bit-xor x y))
  ([x y & more] (bit-xor x (reduce bit-xor y more))))

(defn any-true? [coll]
  (loop [c coll]
    (if c
      (if (first c)
        true
        (recur (next c)))
      false)))

(defn str-not-empty? [s]
  (and s
       (> (.length s) 0)))

(defn str-join [sep sequence]
  (apply str (interpose sep sequence)))

; From http://groups.google.com/group/clojure/browse_frm/thread/4732a53752302fa5?hl=en
(defmacro map-method [method coll & args]
  "Calls the given method on each item in the collection."
  `(map (fn [x#] (. x# ~@(if args
                           (concat (list method) args)
                           (list method)))) ~coll))

(defn str-contains? [s search]
  (>= (.indexOf s search) 0))

(defn vector->byte-array [v]
  (into-array Byte/TYPE (map byte v)))