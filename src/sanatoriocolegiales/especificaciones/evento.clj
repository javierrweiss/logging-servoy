(ns sanatoriocolegiales.especificaciones.evento
  (:require [clojure.spec.alpha :as s]
            [hyperfiddle.rcf :refer [tests]]))

(def patron-fecha #"\d{4}-\d{2}-\d{2}")
(s/def ::nombre string?)
(s/def ::origen #{"uti" "uco" "cirugia" "convenios" "hcdm"})
(s/def ::fecha (s/and string? #(re-matches patron-fecha %)))
 
(tests
 (s/valid? ::origen ::uti) := false
 (s/valid? ::origen "res")  := false
 (s/valid? ::fecha "1990-12-21") := true
 (s/valid? ::fecha "1000/12/21") := false
 (s/valid? ::fecha "100/12/21") := false
 (s/valid? ::fecha "1000/12/1") := false
 (s/valid? ::fecha "1000/12-21") := false
 (s/valid? ::fecha "Cualquier otra cosa 2024-10/10 escsree 2024-09/10") := false
 (s/valid? ::origen "uti") := true)