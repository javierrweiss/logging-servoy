(ns sanatoriocolegiales.logging-servoy.service-test
  (:require [clojure.test :refer [deftest is testing]]
            [sanatoriocolegiales.logging-servoy.service :as logging-servoy]))

(deftest service-test
  (testing "TODO: Start with a failing test, make it pass, then refactor"

    ;; TODO: fix greet function to pass test
    (is (= "sanatoriocolegiales logging-servoy service developed by the secret engineering team"
           (logging-servoy/greet)))

    ;; TODO: fix test by calling greet with {:team-name "Practicalli Engineering"}
    (is (= (logging-servoy/greet "Practicalli Engineering")
           "sanatoriocolegiales logging-servoy service developed by the Practicalli Engineering team"))))


(comment
  (clojure.test/run-all-tests)
  )