;; ---------------------------------------------------------
;; sanatoriocolegiales.logging-servoy.spec
;;
;; Value specifications for logging-servoy
;; - API request / response validation
;;
;; Used in
;; - `sanatoriocolegiales.logging-servoy.api/scoreboard`
;; ---------------------------------------------------------


(ns sanatoriocolegiales.logging-servoy.spec
  (:require [clojure.spec.alpha :as spec]))

;; ---------------------------------------------------
;; Value specifications

(spec/def ::game-id string?)
(spec/def ::game-name string?)
(spec/def ::high-score string?)
(spec/def ::comment string?)


(spec/def ::scoreboard
  (spec/coll-of
   (spec/keys
    :req [::game-id ::game-name ::high-score]
    :opt [::comment])))
;; ---------------------------------------------------


(comment

  ;; true example
  (spec/valid? ::scoreboard [{::game-id "12345" ::game-name "Polybus" ::high-score "99999999997"}])

  #_()) ; End of rich comment
