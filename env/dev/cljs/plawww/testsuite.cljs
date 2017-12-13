(ns plawww.testsuite
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [plawww.medialist.core :as medialist]))





(deftest test-medialist
  (is (= 1 1)))

(enable-console-print!)
(cljs.test/run-tests)