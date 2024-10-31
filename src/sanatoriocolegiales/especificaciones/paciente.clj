(ns sanatoriocolegiales.especificaciones.paciente
  (:require [clojure.spec.alpha :as s]
            [hyperfiddle.rcf :refer [tests]]))

(def tipo-admision #{"internado" "ambulatorio"})
(s/def ::historia_clinica int?)
(s/def ::historia_clinica_unica int?)
(s/def ::tipo tipo-admision)

(tests

 (s/valid? ::tipo 'afla)  := false
 (s/valid? ::tipo "internado") := true
 (s/valid? ::tipo "ambulatorio") := true
 (s/valid? ::tipo :ambulatorio) := false
 (s/valid? ::historia_clinica 12.23 false)
 (s/valid? ::historia_clinica_unica 1223 true)
 
 :rcf)