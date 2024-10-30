(ns sanatoriocolegiales.especificaciones.convenios
  (:require [clojure.spec.alpha :as s]))

(s/def ::nro-lote int?)
(s/def ::contador-registros int?)