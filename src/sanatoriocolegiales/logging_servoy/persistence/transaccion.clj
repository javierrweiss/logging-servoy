(ns sanatoriocolegiales.logging-servoy.persistence.transaccion
  (:require [datomic.api :as d]
            [sanatoriocolegiales.logging-servoy.persistence.esquema :refer [log-schema]]))

(defn registrar-esquema!
  [conn]
  @(d/transact conn log-schema))

(defn ejecutar!
  [conn datos]
  @(d/transact conn datos))
 
(comment
   
 (def cnn (-> (::ds/instances (system-repl/system))
              :env
              :persistence
              :conn))
  
  (registrar-esquema! cnn)

  )