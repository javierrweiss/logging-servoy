(ns sanatoriocolegiales.especificaciones.convenios
  (:require [clojure.spec.alpha :as s]))

(s/def ::nro_lote int?)
(s/def ::contador_registros int?)