(ns ajuretest.util.other
  (:use [ajure.util.other])
  (:use [clojure.test]))

(deftest test-any-true?
  (testing "all true"
    (is (any-true? [1 "yo" true :dude])))
  (testing "all false"
    (is (not (any-true? [false false nil false]))))
  (testing "some true"
    (is (any-true? [false true nil true]))))
