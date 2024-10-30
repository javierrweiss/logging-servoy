(ns sanatoriocolegiales.especificaciones.paciente
  (:require [clojure.spec.alpha :as s]
            [hyperfiddle.rcf :refer [tests]]))

(def tipo-admision #{"internado" "ambulatorio"})
(s/def ::historia-clinica int?)
(s/def ::historia-clinica-unica int?)
(s/def ::tipo tipo-admision)

(tests

 (s/valid? ::tipo 'afla)  := false
 (s/valid? ::tipo "internado") := true
 (s/valid? ::tipo "ambulatorio") := true
 (s/valid? ::tipo :ambulatorio) := false
 (s/valid? ::historia-clinica 12.23 false)
 (s/valid? ::historia-clinica-unica 1223 true)
 
 :rcf)