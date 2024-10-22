(ns sanatoriocolegiales.logging-servoy.api.scoreboard
  "Gameboard API Scoreboard across all games"
  (:require
   [ring.util.response :refer [response]]))

(def scores
  "Simple status report for external monitoring services, e.g. Pingdom
  Return:
  - `constantly` returns an anonymous function that returns a ring response hash-map"
  (constantly (response {::game-id "347938472938439487492"
                         ::game-name "Polymous"
                         ::high-score "344398799666"})))

(defn routes 
  [system-config]
  ["/scoreboard"
   {:swagger {:tags ["Scoreboard Endpoints"]}
    :get {:summary "Scoreboard across all games"
          :description "Return all the high scores for every game registered"
          :handler scores}}])
;; --------------------------------------------------
