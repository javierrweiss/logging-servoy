(ns sanatoriocolegiales.especificaciones.estado
  (:require [clojure.spec.alpha :as s]))

(s/def ::ok (s/coll-of string?))
(s/def ::excepcion (s/coll-of string?))