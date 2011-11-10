(ns ajuretest.util.queue
  (:use [ajure.util.queue])
  (:use [clojure.test]))

(deftest test-enqueue
  (testing "with empty or nil vectors"
    (is (= [1]
           (enqueue nil 1)))
    (is (= [1]
           (enqueue [] 1))))
  (testing "with non-empty queues"
    (is (= [1 2]
           (enqueue [1] 2)))))

(deftest test-dequeue
  (testing "with empty or nil vectors"
    (is (= nil
           (dequeue nil)))
    (is (= nil
           (dequeue []))))
  (testing "with non-empty queues"
    (is (= [1 2]
           (dequeue [0 1 2])))))

