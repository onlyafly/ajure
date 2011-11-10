(ns ajuretest.test-helper)

;; For testing private functions, use this macro (courtesy of
;; chouser), then wrap your tests with with-private-fns:
;;
;; (with-private-fns [org.foo.bar [fn1 fn2]]
;;   (deftest test-fn1..)
;;   (deftest test-fn2..))
(defmacro with-private-fns [[ns fns] & tests]
  "Refers private fns from ns and runs tests in context."
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))

